package com.example.springwebex.util

import java.math.BigInteger
import java.security.*
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher

object RsaUtil {

    private const val ALGORITHM_NAME = "RSA"
    private const val SIGNATURE_NAME = "SHA256withRSA"
    private const val ENCRYPT_NAME = "RSA/NONE/PKCS1Padding"

    fun makeKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance(ALGORITHM_NAME)
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

    fun encodePublicKey(publicKey: PublicKey): String {
        val keyFactory = KeyFactory.getInstance(ALGORITHM_NAME)
        val publicSpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec::class.java)
        val publicKeyModulus = publicSpec.modulus.toString(16)
        val publicKeyExponent = publicSpec.publicExponent.toString(16)
        return "$publicKeyModulus|$publicKeyExponent"
    }

    fun decodePublicKey(encodeKey: String): PublicKey {
        val pubKey = encodeKey.split("\\|".toRegex()).toTypedArray()
        val modulus = BigInteger(pubKey[0], 16)
        val exponent = BigInteger(pubKey[1], 16)
        val pubks = RSAPublicKeySpec(modulus, exponent)
        val keyFactory = KeyFactory.getInstance(ALGORITHM_NAME)
        return keyFactory.generatePublic(pubks)
    }

    fun encodePrivateKey(privateKey: PrivateKey): String {
        val keyFactory = KeyFactory.getInstance(ALGORITHM_NAME)
        val privateSpec = keyFactory.getKeySpec(privateKey, RSAPrivateKeySpec::class.java)
        val privateKeyModulus = privateSpec.modulus.toString(16)
        val privateKeyExponent = privateSpec.privateExponent.toString(16)
        return "$privateKeyModulus|$privateKeyExponent"
    }

    fun decodePrivateKey(encodeKey: String): PrivateKey {
        val priKey = encodeKey.split("\\|".toRegex()).toTypedArray()
        val modulus = BigInteger(priKey[0], 16)
        val exponent = BigInteger(priKey[1], 16)
        val priks = RSAPrivateKeySpec(modulus, exponent)
        val keyFactory = KeyFactory.getInstance(ALGORITHM_NAME)
        return keyFactory.generatePrivate(priks)
    }

    fun sign(privateKey: PrivateKey, plaintext: ByteArray): ByteArray {
        val signature = Signature.getInstance(SIGNATURE_NAME)
        signature.initSign(privateKey, SecureRandom())
        signature.update(plaintext)
        return signature.sign()
    }

    fun verify(publicKey: PublicKey, sign: ByteArray, plaintext: ByteArray): Boolean {
        val signature = Signature.getInstance(SIGNATURE_NAME)
        signature.initVerify(publicKey)
        signature.update(plaintext)
        return signature.verify(sign)
    }

    fun encrypt(publicKey: PublicKey, plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ENCRYPT_NAME)
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(plaintext)
    }

    fun decrypt(privateKey: PrivateKey, ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(ENCRYPT_NAME)
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(ciphertext)
    }
}
