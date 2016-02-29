package ru.ifmo.ctddev.kupriyanov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pinkdonut on 17.02.2016.
 */
public class Walk {
    public static void main(String[] args) {
        try (BufferedReader in = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8);
             BufferedWriter out = Files.newBufferedWriter(Paths.get(args[1]), StandardCharsets.UTF_8)) {
            String temp;
            while ((temp = in.readLine()) != null) {
                out.write(getMD5(Paths.get(temp)));
            }
        } catch (IOException e) {
            System.err.println("Wrong input");
        }
    }

    public static String getMD5(Path path) {
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
