package ru.ifmo.ctddev.kupriyanov.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by pinkdonut on 29.02.2016.
 */
public class RecursiveWalk {

    private static MD5Hasher hasher = new MD5Hasher();

    public static void main(String[] args) {
        try (BufferedReader in = Files.newBufferedReader(Paths.get(args[0]), StandardCharsets.UTF_8);
             BufferedWriter out = Files.newBufferedWriter(Paths.get(args[1]), StandardCharsets.UTF_8)) {
            String temp;
            while ((temp = in.readLine()) != null) {
                out.write(recursive(Paths.get(temp)));
            }
        } catch (IOException e) {
            System.err.println("Wrong input");
        }
    }

    private static String recursive(Path path) {
        StringBuilder result = new StringBuilder();
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path p : stream) {
                    result.append(recursive(p));
                }
            } catch (IOException e) {
                result.append(hasher.getHash(path));
                System.err.println("Problems with directory");
            }
        } else {
            result.append(hasher.getHash(path));
        }
        return result.toString();
    }
}

