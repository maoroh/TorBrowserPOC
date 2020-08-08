package utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.util.Base64;

/*
AES-CBC 128Bit Utils
 */
public class AESUtils {

    public static AESEncryptionResult AESEncrypt(byte[] clearText, byte[] secret) throws Throwable {

        SecretKeySpec aesKey = new SecretKeySpec(secret, 0, 16, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte [] cipherText = cipher.doFinal(clearText);
        return new AESEncryptionResult(Base64.getEncoder().encode(cipherText), cipher.getParameters().getEncoded());
    }

    public static byte [] AESDecrypt(AESEncryptionResult encryptionResult, byte[] secret) throws Throwable {

        SecretKeySpec aesKey = new SecretKeySpec(secret, 0, 16, "AES");
        AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
        aesParams.init(encryptionResult.getParams());
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, aesParams);
        byte[] recovered = cipher.doFinal(Base64.getDecoder().decode(encryptionResult.getCipherText()));
        return recovered;
    }

    public static class AESEncryptionResult implements Serializable {

        private byte[] _cipherText;
        private byte[] _params;

        public AESEncryptionResult(byte [] cipherText , byte[] params){
            _cipherText = cipherText;
            _params = params;
        }

        public byte [] getParams(){
            return _params;
        }

        public byte [] getCipherText(){
            return _cipherText;
        }


    }
}
