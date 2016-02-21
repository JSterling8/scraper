package com.jms.scraper.service.odd;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anon on 21/02/2016.
 */
@Service
public class OddScraper {
    public List<String> scrapeAll() throws InterruptedException {
        boolean successfullyRetrieved = false;
        Document document = null;
        String team = "fnatic";

        String url = "http://www.csgonuts.com/odds?t1=" + team;

        while(!successfullyRetrieved) {
            try {
                document = Jsoup.connect(url)
                        .timeout(7000)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                        .get();

                successfullyRetrieved = true;
            } catch (Exception e) {
                System.err.println("Failed to get load page " + url + " due to the following exception: " + e.getClass());

                successfullyRetrieved = false;

                Thread.sleep(5000l);
            }
        }

        Elements teamNames = document.select("div.col-md-7 > div a:nth-of-type(1)");

        List<String> names = new ArrayList();
        for(Element teamName : teamNames) {
            names.add(teamName.text());
        }

        return names;
    }
}
