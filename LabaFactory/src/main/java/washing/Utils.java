package washing;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

class Crypto {
    public static String md5(String text) throws Exception {
        byte[] hash = md5Bytes(text);
        String hex = HexFormat.of().formatHex(hash);
        return hex;
    }

    public static String encrypt(String text, String key) throws Exception {
        SecretKeySpec secretKey = keySpec(key);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] plainBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = cipher.doFinal(plainBytes);
        String result = Base64.getEncoder().encodeToString(encryptedBytes);
        return result;
    }

    public static String decrypt(String text, String key) throws Exception {
        SecretKeySpec secretKey = keySpec(key);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(text.trim());
        byte[] plainBytes = cipher.doFinal(encryptedBytes);
        String result = new String(plainBytes, StandardCharsets.UTF_8);
        return result;
    }

    private static SecretKeySpec keySpec(String key) throws Exception {
        byte[] keyBytes = md5Bytes(key);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
        return secretKey;
    }

    private static byte[] md5Bytes(String text) throws Exception {
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] hash = digest.digest(textBytes);
        return hash;
    }
}

class Zipper {
    public static void zip(String zipPath, String... files) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipPath))) {
            for (String file : files) {
                Path path = Path.of(file);

                if (!Files.exists(path)) {
                    continue;
                }
                out.putNextEntry(new ZipEntry(path.getFileName().toString()));

                Files.copy(path, out);
                out.closeEntry();
            }
        }
    }
}

class Log {
    public static String file = "errors.log";

    public static void error(String message, String line) {
        String text = "ОШИБКА: " + message + " | строка: " + line;
        System.out.println(text);

        try {
            Files.writeString(Path.of(file), text + '\n', StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
