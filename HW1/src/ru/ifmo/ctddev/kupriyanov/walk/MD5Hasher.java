package ru.ifmo.ctddev.kupriyanov.walk;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * Created by pinkdonut on 01.03.2016.
 */
public class MD5Hasher {
    public static String getHash(Path path) {
        byte[] digest = new byte[16];
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(path)) {
                DigestInputStream dis = new DigestInputStream(is, md);
                while (dis.available() > 0) {
                    dis.read();
                }
            }
            digest = md.digest();
        } catch (Exception e) {
        }

        StringBuilder result = new StringBuilder();
        for (byte b : digest) {
            result.append(String.format("%02X", b));
        }
        result.append(" ").append(path.toString()).append("\n");
        return result.toString();
    }
}
