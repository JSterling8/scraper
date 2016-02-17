package com.jms.scraper.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by anon on 31/01/2016.
 */
@Service
public class RatingService {
    public RatingService(){

    }

    public void handleRatingForLatestResult() {
        boolean rated = false;

        while (!rated) {
            try {
                Document document = Jsoup.connect("http://localhost:8080/rater/rating/parse-latest-result/")
                        .timeout(7000)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                        .ignoreContentType(true)
                        .get();

                // Give the other app time to parse the result before continuing
                Thread.sleep(1000l);

                rated = true;
            } catch (IOException e) {
                System.err.println("Failed to connect to rating service.  Waiting 5 minutes before retrying.");

                try {
                    Thread.sleep(1000l * 60l * 5l);
                } catch (InterruptedException e1) {
                    System.err.println("Exception thrown whilst waiting to retry connecting to parsing app");

                    break;
                }
            } catch (InterruptedException e) {
                System.err.println("Exception thrown whilst waiting for latest result to be parsed by rating service webapp.");

                rated = true;
            }
        }
    }
}