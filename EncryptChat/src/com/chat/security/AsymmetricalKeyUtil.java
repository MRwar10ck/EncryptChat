package com.chat.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.Cipher;
import org.apache.commons.codec.binary.Base64;

public class AsymmetricalKeyUtil {

	public static final String KEY_ALGORITHM = "RSA";
	private PublicKey public_key;
	private PrivateKey private_key;

	public AsymmetricalKeyUtil() {
	}

	public void createPair() throws NoSuchAlgorithmException {
		// initialize asymmetrical secret key
		// get the RSA generator
		KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		// get a random seed
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.setSeed("chatServer".getBytes());
		// initialize the key pair generator with random seed and 1024 key size
		kpg.initialize(1024, secureRandom);
		KeyPair kp = kpg.genKeyPair();
		// get public key
		public_key = kp.getPublic();
		// get private key
		private_key = kp.getPrivate();
	}

	public PublicKey getPublic_key() {
		return public_key;
	}

	public void setPublic_key(PublicKey public_key) {
		this.public_key = public_key;
	}

	public PrivateKey getPrivate_key() {
		return private_key;
	}

	public void setPrivate_key(PrivateKey private_key) {
		this.private_key = private_key;
	}

	/**
	 * encrypt the plainText with public key
	 * 
	 * @param plainText
	 * @return
	 * @throws Exception
	 */
	public byte[] encryptText(String plainText) throws Exception {
		byte[] plainTextBytes = plainText.getBytes("UTF8");
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, public_key);

		byte[] enBytes = null;
		for (int i = 0; i < plainTextBytes.length; i += 64) {
			byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(plainTextBytes, i, i + 64));
			enBytes = ArrayUtils.addAll(enBytes, doFinal);
		}
		return enBytes;
	}

	/**
	 * 
	 * @param encryptedTextBytes
	 * @return
	 * @throws Exception
	 */
	public String decryptText(byte[] encryptedTextBytes) throws Exception {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, private_key);

		byte[] deBytes = null;
		for (int i = 0; i < encryptedTextBytes.length; i += 128) {
			byte[] doFinal = cipher.doFinal(ArrayUtils.subarray(encryptedTextBytes, i, i + 128));
			deBytes = ArrayUtils.addAll(deBytes, doFinal);
		}
		return new String(deBytes);
	}

	/**
	 * sign the plain text with private key
	 * 
	 * @param plainText
	 * @return
	 */
	public byte[] sign(String plainText) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(private_key.getEncoded());
			KeyFactory keyf = KeyFactory.getInstance(KEY_ALGORITHM);
			PrivateKey prikey = keyf.generatePrivate(priPKCS8);

			// create sign
			java.security.Signature signet = java.security.Signature.getInstance("MD5withRSA");
			signet.initSign(prikey);
			signet.update(plainText.getBytes());

			byte[] signed = Base64.encodeBase64(signet.sign());
			return signed;
		} catch (java.lang.Exception e) {
			System.out.println("sign failed");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * verify the plainText and signText is match or not
	 * 
	 * @param plainText
	 * @param signText
	 * @return
	 */
	public boolean verify(String plainText, byte[] signText) {
		try {
			java.security.spec.X509EncodedKeySpec bobPubKeySpec = new java.security.spec.X509EncodedKeySpec(
					public_key.getEncoded());
			java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance(KEY_ALGORITHM);
			java.security.PublicKey pubKey = keyFactory.generatePublic(bobPubKeySpec);
			byte[] signed = Base64.decodeBase64(signText);
			java.security.Signature signatureChecker = java.security.Signature.getInstance("MD5withRSA");
			signatureChecker.initVerify(pubKey);
			signatureChecker.update(plainText.getBytes());
			// verify the signText
			if (signatureChecker.verify(signed))
				return true;
			else
				return false;
		} catch (Throwable e) {
			System.out.println("not match");
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		AsymmetricalKeyUtil sSecurity = new AsymmetricalKeyUtil();
		sSecurity.createPair();
		byte[] sign = sSecurity.sign("hello");
		System.out.println(sSecurity.verify("hello", sign));

		byte[] en = sSecurity.encryptText("asdfasfasdfasfasdfasdfasdfasdfas阿三地方啊手动发士大夫 ");
		String de = sSecurity.decryptText(en);
		System.out.println(de);

	}

	static class ArrayUtils {
		static byte[] subarray(byte[] origin, int start, int end) {
			byte[] subArray = new byte[end - start];
			int j = 0;
			for (int i = start; i < origin.length && i < end; i++) {
				subArray[j++] = origin[i];
			}
			return subArray;
		}

		static byte[] addAll(byte[] array1, byte[] array2) {
			if (array1 == null)
				return array2;
			if (array2 == null)
				return array1;
			byte[] parentArray = new byte[array1.length + array2.length];
			int j = 0;
			for (int i = 0; i < array1.length; i++) {
				parentArray[j++] = array1[i];
			}
			for (int i = 0; i < array2.length; i++) {
				parentArray[j++] = array2[i];
			}
			return parentArray;
		}
	}
}
