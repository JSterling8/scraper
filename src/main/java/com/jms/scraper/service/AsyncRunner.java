package com.jms.scraper.service;

import com.jms.scraper.service.odd.OddScraper;
import com.jms.scraper.service.result.ResultContinualResultScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

/**
 * Created by anon on 24/02/2016.
 */
@Component
public class AsyncRunner {
    @Autowired
    private ResultContinualResultScraperService continualResultScraperService;

    @Autowired
    private OddScraper oddScraper;

    @Autowired
    private TaskExecutor taskExecutor;

    @Async
    @Scheduled(fixedRate = Long.MAX_VALUE)
    public void keepAlive() throws InterruptedException, SQLException, ParseException, IOException {
        taskExecutor.execute(continualResultScraperService);
        taskExecutor.execute(oddScraper);
    }


}
