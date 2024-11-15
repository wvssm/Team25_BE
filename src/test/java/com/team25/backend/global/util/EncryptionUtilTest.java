package com.team25.backend.global.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() throws Exception {
        encryptionUtil = new EncryptionUtil();
        String secretKey = "TestSecretKey123"; // 테스트 용 키
        Field field = EncryptionUtil.class.getDeclaredField("secretKey");
        field.setAccessible(true);
        field.set(encryptionUtil, secretKey);
    }

    private String sha256Hex(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Test
    @DisplayName("올바른 sign data 생성 확인")
    void testGenerateSignData() throws Exception {
        String[] params = {"param1", "param2", "param3"};
        String concatenatedParams = "param1param2param3";
        String expectedHash = sha256Hex(concatenatedParams);

        String result = encryptionUtil.generateSignData(params);

        assertEquals(expectedHash, result);
    }

    @Test
    @DisplayName("암호화 및 복호화 확인")
    void testEncryptDecrypt() throws Exception {
        String plainText = "testPlainText";

        String encryptedText = encryptionUtil.encrypt(plainText);
        String decryptedText = encryptionUtil.decrypt(encryptedText);

        assertEquals(plainText, decryptedText);
    }

    @Test
    @DisplayName("암호화 결과가 평문과 다른지 확인")
    void testEncryptionProducesDifferentText() throws Exception {
        String plainText = "testPlainText";

        String encryptedText = encryptionUtil.encrypt(plainText);

        assertNotEquals(plainText, encryptedText);
    }

    @Test
    @DisplayName("다른 평문을 다르게 암호화하는지 확인")
    void testEncryptsDifferentTextsDifferently() throws Exception {
        String plainText1 = "testPlainText1";
        String plainText2 = "testPlainText2";

        String encryptedText1 = encryptionUtil.encrypt(plainText1);
        String encryptedText2 = encryptionUtil.encrypt(plainText2);

        assertNotEquals(encryptedText1, encryptedText2);
    }

    @Test
    @DisplayName("잘못된 암호문 복호화 시 예외를 발생하는지 확인")
    void testDecryptInvalidCipherText() {
        String invalidCipherText = "InvalidCipherText";

        assertThrows(Exception.class, () -> encryptionUtil.decrypt(invalidCipherText));
    }

    @Test
    @DisplayName("빈 문자열의 암호화 및 복호화를 처리하는지 확인")
    void testEncryptDecryptEmptyString() throws Exception {
        String plainText = "";

        String encryptedText = encryptionUtil.encrypt(plainText);
        String decryptedText = encryptionUtil.decrypt(encryptedText);

        assertEquals(plainText, decryptedText);
    }

    @Test
    @DisplayName("encrypt 메서드에서 null 입력을 처리하는지 확인")
    void testEncryptNullInput() {
        String plainText = null;

        assertThrows(NullPointerException.class, () -> encryptionUtil.encrypt(plainText));
    }

    @Test
    @DisplayName("decrypt 메서드에서 null 입력을 처리하는지 확인")
    void testDecryptNullInput() {
        String cipherText = null;

        assertThrows(NullPointerException.class, () -> encryptionUtil.decrypt(cipherText));
    }
}
