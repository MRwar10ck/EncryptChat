package com.chat.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base64;

public class HashUtil {

	private static MessageDigest messageDigest = null;

	static {
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static String GenerateHash(String str) {
		messageDigest.update(str.getBytes());
		String encryptedString = new String(Base64.encodeBase64(messageDigest.digest()));
		return encryptedString;
	}

	public static boolean isUnchanged(String plainText, String hash) {
		return GenerateHash(plainText).equals(hash);
	}

}
