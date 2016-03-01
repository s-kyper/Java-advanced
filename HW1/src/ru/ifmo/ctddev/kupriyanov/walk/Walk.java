package ru.ifmo.ctddev.kupriyanov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by pinkdonut on 17.02.2016.
 */
public class Walk {
    public static void main(String[] args) {
        try (BufferedReader in = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8);
             BufferedWriter out = Files.newBufferedWriter(Paths.get(args[1]), StandardCharsets.UTF_8)) {
            MD5Hasher hasher = new MD5Hasher();
            String temp;
            while ((temp = in.readLine()) != null) {
                out.write(hasher.getHash(Paths.get(temp)));
            }
        } catch (IOException e) {
            System.err.println("Wrong input");
        }
    }
}