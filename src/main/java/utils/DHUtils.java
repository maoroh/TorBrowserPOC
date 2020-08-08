package utils;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;

public class DHUtils {


    public static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        paramGen.init(keySize, new SecureRandom());
        AlgorithmParameters params = paramGen.generateParameters();
        DHParameterSpec dhSpec = params.getParameterSpec(DHParameterSpec.class);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(dhSpec);
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
