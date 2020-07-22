package utils;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class DHUtils {


    public static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
            keyPairGenerator.initialize(keySize);
            return keyPairGenerator.generateKeyPair();
    }

    public static KeyAgreement createKeyAgreement(KeyPair keyPair) throws NoSuchAlgorithmException, InvalidKeyException {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(keyPair.getPrivate());
            return keyAgreement;
    }

    public static PublicKey getPublicKeyFromEncodedBytes(byte[] encodedPubKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
            KeyFactory keyFactory  = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(encodedPubKey);
            PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
            return publicKey;
    }


}
