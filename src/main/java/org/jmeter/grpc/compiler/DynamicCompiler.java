package org.jmeter.grpc.compiler;

import java.lang.reflect.Method;

public class DynamicCompiler {
	public Object buildRequest(String fullName, String javaSrcCode)
			throws IllegalAccessException, InstantiationException {
		System.out.println("Compile and Execute JavaCode:\n" + javaSrcCode.toString());
		DynamicEngine de = DynamicEngine.getInstance();

		Object instance = de.javaCodeToObject(fullName, javaSrcCode.toString());
		System.out.println("instance:\n" + instance);
		return instance;
	}

	public static String test1() {
		StringBuilder src = new StringBuilder();
		src.append("public class DynaClass {\n");
		src.append("    public String toString() {\n");
		src.append("        return \"Hello, I am \" + ");
		src.append("this.getClass().getSimpleName();\n");
		src.append("    }\n");
		src.append("}\n");
		return src.toString();
	}

	public static String test2() {
		StringBuilder src = new StringBuilder();
		src.append("import io.grpc.examples.helloworld.MsgRequest;\n");
		src.append("import com.google.protobuf.Message; \n");
		src.append("public class RequestFactory {\n");
		src.append("public Message buildRequest() {\n");
		src.append("MsgRequest request = MsgRequest.newBuilder().setName(\"Name\").setId(10000).build();\n");
		src.append("return request;\n");
		src.append("    }\n");
		src.append("}\n");
		return src.toString();
	}

	public static void main(String[] args) throws Exception {
		DynamicCompiler test = new DynamicCompiler();
		//Object obj2 = test.buildRequest("DynaClass", DynaCompTest.test1());
		//System.out.println(obj2);

		Object obj = test.buildRequest("RequestFactory", DynamicCompiler.test2());
		Method method = obj.getClass().getMethod("buildRequest", null);
		Object request = method.invoke(obj, null);
		System.out.println(request.getClass() + ", toString=\n" + request.toString());
	}
}