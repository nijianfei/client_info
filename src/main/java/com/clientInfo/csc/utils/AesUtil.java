package com.clientInfo.csc.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesUtil {
    // 密钥必须是 16/24/32 位（128/192/256 bit）
    private static final String KEY = "1234567890ABCdef";

    // 加密
    public static String encrypt(String data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // 解密
    public static String decrypt(String data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(data));
        return new String(decrypted);
    }

    public static void main(String[] args) throws Exception {
        String original = "TEST_QR_T1";
        String enc = encrypt(original);
        String dec = decrypt(enc);

        System.out.println("AES 加密：" + enc);
        System.out.println("AES 解密：" + dec);
    }
}