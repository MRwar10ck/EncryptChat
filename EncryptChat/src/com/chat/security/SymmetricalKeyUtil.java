package com.chat.security;

import java.security.Signature;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.commons.codec.binary.Base64;

public class SymmetricalKeyUtil {
	private String KEY_ALGORITHM = "DES";
	private SecretKey secretKey = null;
	private String keyStr = null;

	public static String getRandomKeyStr() {
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 8; i++) {
			char c = (char) random.nextInt(26);
			sb.append(c);
		}
		return sb.toString();
	}

	public SymmetricalKeyUtil(String keyStr) throws Exception {
		byte[] input = keyStr.getBytes();
		DESKeySpec desKey = new DESKeySpec(input);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
		SecretKey securekey = keyFactory.generateSecret(desKey);
		secretKey = securekey;
		this.keyStr = keyStr;
	}

	public String getKeyStr() {
		return keyStr;
	}

	public byte[] encryptText(String plainText) throws Exception {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return Base64.encodeBase64(cipher.doFinal(plainText.getBytes("UTF8")));
	}

	public String decryptText(byte[] encryptedTextBytes) throws Exception {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return new String(cipher.doFinal(encryptedTextBytes));
	}

	public static void main(String[] args) throws Exception {
		System.out.println(SymmetricalKeyUtil.getRandomKeyStr());

		SymmetricalKeyUtil sSecurity = new SymmetricalKeyUtil(SymmetricalKeyUtil.getRandomKeyStr());
		byte[] en = sSecurity.encryptText("asdfasfasdfasfasdfasdfasdfasdfas闃夸笁鍦版柟鍟婃墜鍔ㄥ彂澹ぇ澶� ");
		String de = sSecurity.decryptText(Base64.decodeBase64(en));
		System.out.println(de);
		SymmetricalKeyUtil sSecurity1 = new SymmetricalKeyUtil("A1B2C3D4");
		byte[] en1 = sSecurity.encryptText("asdfasfasdfasfasdfasdfasdfasdfas闃夸笁鍦版柟鍟婃墜鍔ㄥ彂澹ぇ澶� ");
		for (int i = 0; i < en1.length; i++) {
			if (en[i] != en1[i])
				System.out.println("false");
		}

	}
}
