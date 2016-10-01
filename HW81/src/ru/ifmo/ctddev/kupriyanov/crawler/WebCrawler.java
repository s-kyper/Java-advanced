package ru.ifmo.ctddev.kupriyanov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import javafx.util.Pair;
import ru.ifmo.ctddev.kupriyanov.mapper.ParallelMapperImpl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 *  Crawler that recursively walks the websites and downloads pages
 *
 *  @author pinkdonut
 */
public class WebCrawler implements Crawler {
    private Downloader downloader;
    private ParallelMapper downloadMapper;
    private ParallelMapper extractMapper;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        downloaders = Math.min(downloaders, 1000);
        extractors = Math.min(extractors, 1000);
        downloadMapper = new ParallelMapperImpl(downloaders);
        extractMapper = new ParallelMapperImpl(extractors);
    }

    public static void main(String[] args) {
        Downloader downloader;
        try {
            downloader = new CachingDownloader(Paths.get("C:\\Users\\pinkdonut\\IdeaProjects\\HW8\\cache").toFile());
        } catch (IOException e) {
            System.out.println("Exception occurred while creating downloader, cause: " + e.getMessage());
            return;
        }

        int downloaders = Integer.MAX_VALUE;
        int extractors = Integer.MAX_VALUE;
        int perHost = Integer.MAX_VALUE;
        String url;
        switch (args.length) {
            case 4:
                perHost = Integer.valueOf(args[3]);
            case 3:
                extractors = Integer.valueOf(args[2]);
            case 2:
                downloaders = Integer.valueOf(args[1]);
            case 1:
                url = args[0];
                break;
            default:
                System.out.println("Usage: WebCrawler url [downloaders [extractors [perHost]]]");
                return;
        }

        new WebCrawler(downloader, downloaders, extractors, perHost).download(url, 1);
    }

    @Override
    public Result download(String url, int depth) {
        System.out.println("####\tinitial URL: " + url + " depth: " + depth);

        Set<String> downloadedUrls = new HashSet<>();
        Map<String, IOException> exceptionMap = new HashMap<>();
        List<String> pendingDownload = new ArrayList<>();
        pendingDownload.add(url);

        for (int i = 0; i < depth; i++) {
            List<Document> documents = new ArrayList<>();
            try {
                documents = downloadMapper.map((o) -> {
                    try {
                        return downloader.download(o);
                    } catch (IOException e) {
                        exceptionMap.put(o, e);
                        return null;
                    }
                }, pendingDownload);
            } catch (InterruptedException e) {
            }

            documents.removeIf(o -> o == null);

            List<List<String>> extractedForEach = new ArrayList<>();
            try {
                extractedForEach = extractMapper.map((o) -> {
                    try {
                        return o.extractLinks();
                    } catch (IOException e) {
                        return null;
                    }
                }, documents);
            } catch (InterruptedException e) {
            }

            extractedForEach.removeIf(v -> v == null);

            List<String> extractedUrls = new ArrayList<>();
            for (List<String> list : extractedForEach) {
                extractedUrls.addAll(list);
            }

            extractedUrls = extractedUrls.stream().distinct().collect(Collectors.toList());
            downloadedUrls.addAll(pendingDownload);
            pendingDownload.clear();
            pendingDownload.addAll(extractedUrls.stream().filter(o -> !downloadedUrls.contains(o)).collect(Collectors.toList()));
        }

        System.out.println("****\tDownloaded " + downloadedUrls.size() + "  urls: " + exceptionMap.size() + " errors");

        List<String> result = new ArrayList<>(downloadedUrls);
        result.removeAll(exceptionMap.keySet());
        return new Result(result, exceptionMap);
    }

    @Override
    public void close() {
        try {
            downloadMapper.close();
            extractMapper.close();
        } catch (InterruptedException e) {
        }
    }
}
