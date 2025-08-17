package com.cvshealth.digital.microservice.iqe.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CvsCrypto {
    static ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(CvsCrypto.class);

    public CvsCrypto() {
    }

    public static String encryptWithAESCBC256(String dataString, String key, Map<String, Object> events) {
        if (key == null) {
            return null;
        } else {
            try {
                String CONSTANT_VALUE = "CONSTANTVALUEADD";
                dataString = CONSTANT_VALUE + dataString;
                SecureRandom random = new SecureRandom();
                byte[] iv = random.generateSeed(16);
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                SecretKeySpec skeySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(1, skeySpec, ivspec);
                byte[] encrypted = cipher.doFinal(dataString.getBytes());
                String encryptedString = Base64.getEncoder().encodeToString(encrypted);
                events.put("isEncrypted", StringUtils.isNotBlank(encryptedString));
                return encryptedString;
            } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
                events.put("isEncrypted", false);
                events.put("Encrypted Response", ((GeneralSecurityException)e).getMessage());
                logger.error("Error while encrypting data: {}", ((GeneralSecurityException)e).getMessage());
                return null;
            }
        }
    }

    public static String encryptWithAESCBC256(String dataString, String key) {
        if (key == null) {
            return null;
        } else {
            try {
                String CONSTANT_VALUE = "CONSTANTVALUEADD";
                dataString = CONSTANT_VALUE + dataString;
                SecureRandom random = new SecureRandom();
                byte[] iv = random.generateSeed(16);
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                SecretKeySpec skeySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(1, skeySpec, ivspec);
                byte[] encrypted = cipher.doFinal(dataString.getBytes());
                return Base64.getEncoder().encodeToString(encrypted);
            } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
                logger.error("Error while encrypting data: {}", ((GeneralSecurityException)e).getMessage());
                return null;
            }
        }
    }

    public static String encryptLookUpWithAESCBC256(Object lookupId, String key, Map<String, Object> events) {
        if (key == null) {
            return null;
        } else {
            try {
                String CONSTANT_VALUE = "CONSTANTVALUEADD";
                String dataString = CONSTANT_VALUE + mapper.writeValueAsString(lookupId);
                SecureRandom random = new SecureRandom();
                byte[] iv = random.generateSeed(16);
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                SecretKeySpec skeySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(1, skeySpec, ivspec);
                byte[] encrypted = cipher.doFinal(dataString.getBytes());
                String encryptedString = Base64.getEncoder().encodeToString(encrypted);
                events.put("isEncrypted", StringUtils.isNotBlank(encryptedString));
                return encryptedString;
            } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | JsonProcessingException | NoSuchAlgorithmException e) {
                events.put("isEncrypted", false);
                events.put("Encrypted Response", ((Exception)e).getMessage());
                logger.error("Error while encrypting data: {}", ((Exception)e).getMessage());
                return null;
            }
        }
    }

    public static String encryptLookUpWithAESCBC256(Object lookupId, String key) {
        if (key == null) {
            return null;
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String CONSTANT_VALUE = "CONSTANTVALUEADD";
                String dataString = CONSTANT_VALUE + mapper.writeValueAsString(lookupId);
                SecureRandom random = new SecureRandom();
                byte[] iv = random.generateSeed(16);
                IvParameterSpec ivspec = new IvParameterSpec(iv);
                SecretKeySpec skeySpec = new SecretKeySpec(Base64.getDecoder().decode(key), "AES");
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(1, skeySpec, ivspec);
                byte[] encrypted = cipher.doFinal(dataString.getBytes());
                return Base64.getEncoder().encodeToString(encrypted);
            } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | JsonProcessingException | NoSuchAlgorithmException e) {
                logger.error("Error while encrypting data: {}", ((Exception)e).getMessage());
                return null;
            }
        }
    }

    public static String decryptWithAESCBC256(String encryptedText, String basekey, Map<String, Object> events) throws InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        if (basekey != null && encryptedText != null) {
            byte[] encryptedTextByte = Base64.getDecoder().decode(encryptedText);
            byte[] key = Base64.getDecoder().decode(basekey.getBytes());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secret = new SecretKeySpec(key, "AES");
            byte[] iv = new byte[cipher.getBlockSize()];
            int offset = 0;
            if (encryptedTextByte.length > iv.length) {
                System.arraycopy(encryptedTextByte, 0, iv, 0, iv.length);
                offset = iv.length;
            }

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(2, secret, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedTextByte);
            byte[] plainBytes = Arrays.copyOfRange(decryptedBytes, 16, decryptedBytes.length);
            String decryptedString = new String(plainBytes);
            events.put("isDecrypted", StringUtils.isNotBlank(decryptedString));
            return decryptedString;
        } else {
            return null;
        }
    }

    public static String decryptWithAESCBC256(String encryptedText, String basekey) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        if (basekey == null) {
            return null;
        } else {
            byte[] encryptedTextByte = Base64.getDecoder().decode(encryptedText);
            byte[] key = Base64.getDecoder().decode(basekey.getBytes());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secret = new SecretKeySpec(key, "AES");
            byte[] iv = new byte[cipher.getBlockSize()];
            int offset = 0;
            if (encryptedTextByte.length > iv.length) {
                System.arraycopy(encryptedTextByte, 0, iv, 0, iv.length);
                offset = iv.length;
            }

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(2, secret, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedTextByte, offset, encryptedTextByte.length - offset);
            return new String(decryptedBytes);
        }
    }

    public static String encrypt(String dataString, String key) {
        if (key == null) {
            return null;
        } else {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                byte[] keyBytes = Base64.getDecoder().decode(key);
                byte[] iv = new byte[cipher.getBlockSize()];
                byte[] ivZero = new byte[cipher.getBlockSize()];
                System.arraycopy(keyBytes, 0, iv, 0, iv.length);
                if (!Arrays.equals(iv, ivZero)) {
                    iv = ivZero;
                }

                IvParameterSpec ivspec = new IvParameterSpec(iv);
                SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
                cipher.init(1, skeySpec, ivspec);
                byte[] encrypted = cipher.doFinal(dataString.getBytes());
                return Base64.getEncoder().encodeToString(encrypted);
            } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
                logger.error("Error while encrypting data: {}", ((GeneralSecurityException)e).getMessage());
                return null;
            }
        }
    }

    public static String encrypt(String dataString, String key, Map<String, Object> events) {
        if (key == null) {
            return null;
        } else {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                byte[] keyBytes = Base64.getDecoder().decode(key);
                byte[] iv = new byte[cipher.getBlockSize()];
                byte[] ivZero = new byte[cipher.getBlockSize()];
                System.arraycopy(keyBytes, 0, iv, 0, iv.length);
                if (!Arrays.equals(iv, ivZero)) {
                    iv = ivZero;
                }

                IvParameterSpec ivspec = new IvParameterSpec(iv);
                SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
                cipher.init(1, skeySpec, ivspec);
                byte[] encrypted = cipher.doFinal(dataString.getBytes());
                String encryptedString = Base64.getEncoder().encodeToString(encrypted);
                events.put("isEncrypted", StringUtils.isNotBlank(encryptedString));
                return encryptedString;
            } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException e) {
                logger.error("Error while encrypting data: {}", ((GeneralSecurityException)e).getMessage());
                return null;
            }
        }
    }

    public static String decrypt(String encryptedText, String basekey) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        if (basekey == null) {
            return null;
        } else {
            byte[] encryptedTextByte = Base64.getDecoder().decode(encryptedText);
            byte[] key = Base64.getDecoder().decode(basekey.getBytes());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secret = new SecretKeySpec(key, "AES");
            int offset = 0;
            byte[] iv = new byte[cipher.getBlockSize()];
            byte[] ivZero = new byte[cipher.getBlockSize()];
            System.arraycopy(key, 0, iv, 0, iv.length);
            if (!Arrays.equals(iv, ivZero)) {
                iv = ivZero;
            }

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(2, secret, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedTextByte);
            return new String(decryptedBytes);
        }
    }

    public static String decrypt(String encryptedText, String basekey, Map<String, Object> events) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        if (basekey == null) {
            return null;
        } else {
            byte[] encryptedTextByte = Base64.getDecoder().decode(encryptedText);
            byte[] key = Base64.getDecoder().decode(basekey.getBytes());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secret = new SecretKeySpec(key, "AES");
            int offset = 0;
            byte[] iv = new byte[cipher.getBlockSize()];
            byte[] ivZero = new byte[cipher.getBlockSize()];
            System.arraycopy(key, 0, iv, 0, iv.length);
            if (!Arrays.equals(iv, ivZero)) {
                iv = ivZero;
            }

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(2, secret, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedTextByte);
            String decryptedString = new String(decryptedBytes);
            events.put("isDecrypted", StringUtils.isNotBlank(decryptedString));
            return decryptedString;
        }
    }

    public static String encryptionLogging(Object request, String key, Map<String, Object> events) {
        try {
            String dataString = mapper.writeValueAsString(request);
            return encryptWithAESCBC256(dataString, key, events);
        } catch (JsonProcessingException e) {
            events.put("encryptionLoggingError", e.getMessage());
            return null;
        }
    }
}