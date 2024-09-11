package com.example.springsecex.util;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

public final class RsaUtil {

    private static final String ALGORITHM_NAME = "RSA";
    private static final String SIGNATURE_NAME = "SHA256withRSA";

    /*
     * RSA/NONE/NoPadding RSA/NONE/PKCS1Padding RSA/NONE/OAEPWithMD5AndMGF1Padding
     * RSA/NONE/OAEPWithSHA1AndMGF1Padding RSA/NONE/OAEPWithSHA224AndMGF1Padding
     * RSA/NONE/OAEPWithSHA256AndMGF1Padding RSA/NONE/OAEPWithSHA384AndMGF1Padding
     * RSA/NONE/OAEPWithSHA512AndMGF1Padding RSA/NONE/OAEPWithSHA3-224AndMGF1Padding
     * RSA/NONE/OAEPWithSHA3-256AndMGF1Padding
     * RSA/NONE/OAEPWithSHA3-384AndMGF1Padding
     * RSA/NONE/OAEPWithSHA3-512AndMGF1Padding RSA/NONE/ISO9796-1Padding
     */
    private static final String ENCRYPT_NAME = "RSA/NONE/PKCS1Padding";

    private RsaUtil() {
        // do nothig
    }

    public static KeyPair makeKeyPair() throws GeneralSecurityException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM_NAME);
        keyGen.initialize(2048);

        return keyGen.generateKeyPair();
    }

    public static String encodePublicKey(PublicKey publicKey) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_NAME);
        RSAPublicKeySpec publicSpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
        String publicKeyModulus = publicSpec.getModulus().toString(16);
        String publicKeyExponent = publicSpec.getPublicExponent().toString(16);

        return publicKeyModulus + '|' + publicKeyExponent;
    }

    public static PublicKey decodePublicKey(String encodeKey) throws GeneralSecurityException {
        String[] pubKey = encodeKey.split("\\|");
        BigInteger modulus = new BigInteger(pubKey[0], 16);
        BigInteger exponent = new BigInteger(pubKey[1], 16);
        RSAPublicKeySpec pubks = new RSAPublicKeySpec(modulus, exponent);

        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_NAME);

        return keyFactory.generatePublic(pubks);
    }

    public static String encodePrivateKey(PrivateKey privateKey) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_NAME);

        RSAPrivateKeySpec privateSpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec.class);
        String privateKeyModulus = privateSpec.getModulus().toString(16);
        String privateKeyExponent = privateSpec.getPrivateExponent().toString(16);

        return privateKeyModulus + '|' + privateKeyExponent;
    }

    public static PrivateKey decodePrivateKey(String encodeKey) throws GeneralSecurityException {
        String[] priKey = encodeKey.split("\\|");
        BigInteger modulus = new BigInteger(priKey[0], 16);
        BigInteger exponent = new BigInteger(priKey[1], 16);
        RSAPrivateKeySpec priks = new RSAPrivateKeySpec(modulus, exponent);

        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_NAME);

        return keyFactory.generatePrivate(priks);
    }

    public static byte[] sign(PrivateKey privateKey, byte[] plaintext)
            throws GeneralSecurityException {
        Signature signature = Signature.getInstance(SIGNATURE_NAME);
        signature.initSign(privateKey, new SecureRandom());

        signature.update(plaintext);

        return signature.sign();
    }

    public static boolean verify(PublicKey publicKey, byte[] sign, byte[] plaintext)
            throws GeneralSecurityException {
        Signature signature = Signature.getInstance(SIGNATURE_NAME);
        signature.initVerify(publicKey);
        signature.update(plaintext);

        return signature.verify(sign);
    }

    public static byte[] encrypt(PublicKey pubKey, byte[] plaintext)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ENCRYPT_NAME);

        // encrypt the plaintext using the public key
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(plaintext);
    }

    public static byte[] decrypt(PrivateKey priKey, byte[] encrypted)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ENCRYPT_NAME);

        // encrypt the plaintext using the public key
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        return cipher.doFinal(encrypted);
    }

    public static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance("RSA");
    }

}
