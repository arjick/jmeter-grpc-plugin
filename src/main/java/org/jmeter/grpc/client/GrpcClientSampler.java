package org.jmeter.grpc.client;

import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jmeter.grpc.compiler.DynamicCompiler;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;


public class GrpcClientSampler extends AbstractSampler implements TestBean {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggingManager.getLoggerForClass();
	private String hostname = "localhost";
	private int port = 3220;
	private String service = "TargetAdaptor";
	private String networkID = "1751328505";
	private String userID = "2105443881";
	private String method = "listTargetings";
	private String request = "ListTargetingRequest";
	private String requestBuilderCode = "";

	private ManagedChannel channel = null;
	private AbstractStub<?> blockingStub = null;
	private DynamicCompiler dynaCompiler = new DynamicCompiler();

	private final ClassLoader classLoader = GrpcClientSampler.class.getClassLoader();

	public GrpcClientSampler() {
		log.info("Created " + this);
		setName("Grpc Client Sampler");
	}

	private void initGrpcClient() {
		log.info("initGrpcClient");
		try {
			this.channel = ManagedChannelBuilder.forAddress(this.getHostname(), this.getPort()).usePlaintext(true)
					.build();
			Channel channel1 = ClientInterceptors.intercept(channel, new UserAuthClientInterceptor(this.getNetworkID(), this.getUserID()));

			Class<?> serviceGrpcClass = classLoader.loadClass(this.getService() + "Grpc");
			Method newBlockingStubMethod = serviceGrpcClass.getMethod("newBlockingStub", Channel.class);
			log.info("newBlockingStubMethod " +  newBlockingStubMethod);
			this.blockingStub = (AbstractStub<?>) newBlockingStubMethod.invoke(null, channel1);
			log.info("blockingStub " +  blockingStub);
		} catch (Exception e) {
			log.error("Exception", e);
		}
	}

	private void shutdown() throws InterruptedException {
		this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	@Override
	public SampleResult sample(Entry entry) {
		log.info("sample");

		if (this.channel == null) {
			this.initGrpcClient();
		}
		return this.grpcCallMethod();
	}

	public SampleResult grpcCallMethod() {
		log.info("grpcCallMethod");

		long start = System.currentTimeMillis();
		SampleResult result = new SampleResult();
		result.setSampleLabel(getName());
		result.sampleStart();

		try {
			Method apiMethod = this.blockingStub.getClass().getMethod(this.getMethod(),
					Class.forName(this.getRequest()));

			log.info("the channel info " + this.blockingStub.getChannel());

			Object obj = dynaCompiler.buildRequest("RequestFactory", this.getRequestBuilderCode());

			Method method = obj.getClass().getMethod("buildRequest", null);

			Object req = method.invoke(obj, null);
			log.info("getCallOptions " + this.blockingStub.getCallOptions());
			Object resp = apiMethod.invoke(this.blockingStub, req);


			// TODO: get the response message from resp
			log.info("grpcCallMethod response=" + resp);

			result.setSamplerData(getHostname() + ":" + getPort() + "\n" + this.getService() + "#" + this.getMethod()
					+ "\nRequestData:\n" + req.toString());
			result.setResponseData(resp.toString());
			result.setResponseCodeOK();
			result.setResponseMessage("OK");
			result.setResponseCode("200");

		} catch (Exception e) {
			log.error("grpcCallMethod Exception", e);
			result.setResponseData(e.getMessage());
			result.setResponseMessage("Failure");
			result.setResponseCode("500");
		} finally {
			result.setLatency(System.currentTimeMillis() - start);
			result.sampleEnd();
		}

		return result;
	}

	public String getNetworkID() {
		return this.networkID;
	}

	public void setNetworkID(String networkID) {
		this.networkID = networkID;
	}

	public String getUserID() {
		return this.userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getHostname() {
		return this.hostname;
	}

	public int getPort() {
		return this.port;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getRequestBuilderCode() {
		return requestBuilderCode;
	}

	public void setRequestBuilderCode(String requestBuilderCode) {
		this.requestBuilderCode = requestBuilderCode;
	}
}

class UserAuthClientInterceptor implements ClientInterceptor {
	private String nID = "";
	private String uID = "";
	Metadata.Key networkID = Metadata.Key.of("x-freewheel-network-id", Metadata.ASCII_STRING_MARSHALLER);
	Metadata.Key userID = Metadata.Key.of("x-freewheel-user-id", Metadata.ASCII_STRING_MARSHALLER);
	Metadata.Key grpcToken = Metadata.Key.of("Content-Type", Metadata.ASCII_STRING_MARSHALLER);
	UserAuthClientInterceptor(String networkID, String userID) {
		this.nID = networkID;
		this.uID = userID;
	}
	public ClientCall interceptCall(MethodDescriptor method,
									CallOptions callOptions, Channel next) {
		return new SimpleForwardingClientCall (next.newCall(method, callOptions)) {
			public void start(Listener responseListener, Metadata headers) {
				headers.put(networkID, nID);
				headers.put(userID, uID);
				headers.put(grpcToken, "application/grpc");
				super.start(new SimpleForwardingClientCallListener (responseListener) {
					public void onHeaders(Metadata headers) {
						super.onHeaders(headers);
					}
				}, headers);
			}
		};
	}
}
