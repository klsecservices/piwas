import com.sun.tools.attach.VirtualMachine;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.io.File;
import java.util.Formatter;
import java.io.FileInputStream;


public class Loader {

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static void main(String[] args) throws Exception {
        String pid = args[0];
        File file = new File(
                Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI()
                );
        String agent = file.getPath();
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        fis.read(bytes);
        fis.close();

        MessageDigest md = MessageDigest.getInstance("SHA-1"); 
        String hash = byteArray2Hex(md.digest(bytes));

        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(agent, hash);
        vm.detach();
    }
}
