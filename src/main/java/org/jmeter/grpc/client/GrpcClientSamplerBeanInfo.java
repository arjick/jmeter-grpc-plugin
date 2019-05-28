package org.jmeter.grpc.client;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TypeEditor;

public class GrpcClientSamplerBeanInfo extends BeanInfoSupport{
	public GrpcClientSamplerBeanInfo() {
		super(GrpcClientSampler.class);

		createPropertyGroup("Server", new String[]{"hostname", "port"});
		createPropertyGroup("Service", new String[]{"service", "networkID", "userID"});
		createPropertyGroup("Execute", new String[]{"method", "request","requestBuilderCode"});

		PropertyDescriptor localPropertyDescriptor = property("hostname");
		localPropertyDescriptor.setValue("notUndefined", Boolean.TRUE);
		localPropertyDescriptor.setValue("default", "localhost");

		localPropertyDescriptor = property("port");
		localPropertyDescriptor.setValue("notUndefined", Boolean.TRUE);
		localPropertyDescriptor.setValue("default", new Integer(3220));

		localPropertyDescriptor = property("service");
		localPropertyDescriptor.setValue("notUndefined", Boolean.TRUE);
		localPropertyDescriptor.setValue("default", "TargetAdaptor");

		localPropertyDescriptor = property("networkID");
		localPropertyDescriptor.setValue("notUndefined", Boolean.TRUE);
		localPropertyDescriptor.setValue("default", "1751328505");

		localPropertyDescriptor = property("userID");
		localPropertyDescriptor.setValue("notUndefined", Boolean.TRUE);
		localPropertyDescriptor.setValue("default", "2105443881");
		
		localPropertyDescriptor = property("method");
		localPropertyDescriptor.setValue("notUndefined", Boolean.TRUE);
		localPropertyDescriptor.setValue("default", "listTargetings");
		
		localPropertyDescriptor = property("request");
		localPropertyDescriptor.setValue("notUndefined", Boolean.TRUE);
		localPropertyDescriptor.setValue("default", "proto.ListTargetingRequest");

		localPropertyDescriptor = property("requestBuilderCode", TypeEditor.TextAreaEditor);
		localPropertyDescriptor.setValue("notUndefined", Boolean.TRUE);
		localPropertyDescriptor.setValue("textLanguage", "java");
		localPropertyDescriptor.setValue("default", "proto.ListTargetingRequest");
	}

}
