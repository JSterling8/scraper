package com.jms.scraper.controller;

import com.jms.scraper.service.odd.OddScraper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

/**
 * Created by anon on 21/02/2016.
 */
@RestController
public class OddScraperController {
    @Autowired
    private OddScraper oddScraper;

    @RequestMapping("/odd/scrape-all")
    public List<String> scrapeAll() throws ClassNotFoundException, SQLException, InterruptedException, ParseException, IOException {
        return oddScraper.scrapeAll();
    }
}
