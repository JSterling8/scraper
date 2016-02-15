package com.jms.scraper.controller;

import com.jms.scraper.repository.ResultRepository;
import com.jms.scraper.service.BulkHistoricalScraperService;
import com.jms.scraper.service.ContinualScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

/**
 * Created by anon on 30/01/2016.
 */
@RestController
public class ScraperController {
    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private BulkHistoricalScraperService bulkService;

    @Autowired
    private ContinualScraperService continualScraperService;

    @RequestMapping("/count")
    public Long count (){
        return resultRepository.count();
    }

    @RequestMapping("/start/bulk")
    public String startBulk() throws ClassNotFoundException, SQLException, InterruptedException, ParseException, IOException {
        bulkService.start();

        return "done";
    }

    @RequestMapping("/start/continual")
    public String startContinual() throws ClassNotFoundException, SQLException, InterruptedException, ParseException, IOException {
        continualScraperService.start();

        return "done";
    }
}
