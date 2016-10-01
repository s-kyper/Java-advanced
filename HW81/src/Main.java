import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Result;
import ru.ifmo.ctddev.kupriyanov.crawler.WebCrawler;

import java.io.IOException;

/**
 * test
 */
public class Main {
    public static void main(String[] args) throws IOException {
        try (Crawler crawler = new WebCrawler(new CachingDownloader(), 10, 10, 10)) {
            Result links = crawler.download("http://neerc.ifmo.ru/~sta", 2);

            System.out.println(links.getDownloaded().size());
        }
    }
}
