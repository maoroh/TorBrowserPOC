import org.junit.Test;
import utils.AESUtils;
import static org.junit.Assert.*;


public class AESTest {

    @Test
    public void testAES() throws Throwable {

        byte [] key = new byte [128];
        byte [] clearText = "Hello00000000000kljsfksadnfmansbdjkasdkljnjk".getBytes();
        for(int i = 0; i < key.length; i++){
            key[i] = (byte)i;
        }
        AESUtils.AESEncryptionResult aesEncryptionResult = AESUtils.AESEncrypt(clearText, key);

        byte[] recovered = AESUtils.AESDecrypt(aesEncryptionResult,key);
        assertArrayEquals(clearText, recovered);

    }
}
