package com.sogou.speech.wakeup.auth;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Base64;

public class EncryptDecrypt {

	public EncryptDecrypt() {

	}

	public String generateSign(String id, String key, String name) {
		String sign = null;
		String originalString = null;

		if (id == null || key == null || name == null || id.length() == 0
				|| key.length() == 0 || name.length() == 0) {
			return sign;
		}

		originalString = id + key + name;
		try {
			MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.reset();
			mDigest.update(originalString.getBytes("UTF-8"));
			byte[] digestStr = mDigest.digest();
			byte[] encryptStr;
			byte[] finalStr = new byte[10];

			int tmp = 0;
			StringBuffer mBuffer = new StringBuffer();
			String encryptMd5 = null;
			for (int offset = 0; offset < digestStr.length; offset++) {
				tmp = digestStr[offset];
				if (tmp < 0) {
					tmp += 256;
				}

				if (tmp < 16) {
					mBuffer.append("0");
				}

				mBuffer.append(Integer.toHexString(tmp));
			}

			encryptMd5 = mBuffer.toString();
			encryptStr = encryptMd5.getBytes();

			for (int i = 6; i < 16; i++) {
				// to lower case
				if (encryptStr[i] >= 'A' && encryptStr[i] <= 'Z') {
					encryptStr[i] += 32;
				}

				// reflect alphabet and digit
				if (encryptStr[i] >= 'a' && encryptStr[i] <= 'v') {
					encryptStr[i] += 4;
				} else if (encryptStr[i] >= 'w' && encryptStr[i] <= 'z') {
					encryptStr[i] -= 22;
				} else if (encryptStr[i] >= '0' && encryptStr[i] <= '5') {
					encryptStr[i] += 4;
				} else if (encryptStr[i] >= '6' && encryptStr[i] <= '9') {
					encryptStr[i] -= 6;
				}

				finalStr[i - 6] = encryptStr[i];
			}

			StringBuffer sBuffer = new StringBuffer(new String(finalStr));

			sign = sBuffer.reverse().toString();

		} catch (NoSuchAlgorithmException e0) {
			// TODO Auto-generated catch block
			e0.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return sign;
	}

	public String encryptTimes(int times) {
		String encryptStr = null;
		String tempStr = null;

		// int i = 0;
		// encode 1
		byte[] encodeBytes = Base64.encode((String.valueOf(times)).getBytes(),
				Base64.NO_WRAP);
		byte temp = 0;
		temp = encodeBytes[0];
		encodeBytes[0] = encodeBytes[2];
		encodeBytes[2] = temp;

		StringBuffer sBuffer = new StringBuffer(new String(encodeBytes));
		// for (i = 0; i < encodeBytes.length; i++) {
		// sBuffer.append(encodeBytes[i]);
		// }

		tempStr = sBuffer.reverse().toString();

		// encode 2
		byte[] encodeBytes2 = Base64.encode(tempStr.getBytes(), Base64.NO_WRAP);

		temp = encodeBytes2[0];
		encodeBytes2[0] = encodeBytes2[4];
		encodeBytes2[4] = temp;
		temp = encodeBytes2[1];
		encodeBytes2[1] = encodeBytes2[6];
		encodeBytes2[6] = temp;

		StringBuffer sBuffer2 = new StringBuffer(new String(encodeBytes2));
		// for (i = 0; i < encodeBytes2.length; i++) {
		// sBuffer2.append(encodeBytes2[i]);
		// }

		encryptStr = sBuffer2.reverse().toString();

		return encryptStr;
	}

	public int decryptTimes(String encryptStr) {
		int times = -1;
		// int i = 0;

		if (encryptStr == null || encryptStr.length() == 0) {
			return times;
		}

		// decode 1
		StringBuffer sBuffer = new StringBuffer(encryptStr);
		byte[] sourceBytes = (sBuffer.reverse().toString()).getBytes();
		byte temp = 0;

		temp = sourceBytes[0];
		sourceBytes[0] = sourceBytes[4];
		sourceBytes[4] = temp;
		temp = sourceBytes[1];
		sourceBytes[1] = sourceBytes[6];
		sourceBytes[6] = temp;

		byte[] decryptBytes = Base64.decode(sourceBytes, Base64.NO_WRAP);

		StringBuffer rBuffer = new StringBuffer(new String(decryptBytes));
		byte[] reversedBytes = (rBuffer.reverse().toString()).getBytes();

		temp = reversedBytes[0];
		reversedBytes[0] = reversedBytes[2];
		reversedBytes[2] = temp;

		// decode 2
		decryptBytes = Base64.decode(reversedBytes, Base64.NO_WRAP);
		String decryptResult = new String(decryptBytes);
		try {
			times = Integer.parseInt(decryptResult);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return times;
	}
}
