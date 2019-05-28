package org.jmeter.grpc.util;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
 
public class ClassFileManager extends
        ForwardingJavaFileManager {
    public JavaClassObject getJavaClassObject() {
        return jclassObject;
    }
 
    private JavaClassObject jclassObject;
 
 
	@SuppressWarnings("unchecked")
	public ClassFileManager(StandardJavaFileManager
        standardManager) {
        super(standardManager);
    }
 
 
    @Override
    public JavaFileObject getJavaFileForOutput(Location location,
        String className, JavaFileObject.Kind kind, FileObject sibling) {
            jclassObject = new JavaClassObject(className, kind);
        return jclassObject;
    }
}
