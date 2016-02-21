package com.jms.scraper.service.result;

import com.google.common.base.Stopwatch;
import com.jms.scraper.model.Result;
import com.jms.scraper.repository.ResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jonathan Sterling
 */
@Service
public class ResultBulkHistoricalResultScraperService extends ResultScraper {
    @Autowired
    private ResultRepository resultRepository;

    public void start() throws ClassNotFoundException, SQLException, ParseException, IOException, InterruptedException {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();

        long totalTime = 0;

        for (int i = 13850; i >= 0; i -= 50) {
            System.out.println("Getting games " + i + " - " + (i + 50));
            List<Result> results = getResultsInRange(i, i + 50);

            // This is so they're loaded into the database from the bottom to the top (i.e., newest will have highest id in db)
            Collections.reverse(results);

            resultRepository.save(results);

            System.out.println("50 match series (" + results.size() + " matches) saved to DB in " + stopwatch.elapsedTime(TimeUnit.SECONDS) + " seconds.");

            totalTime += stopwatch.elapsedMillis();
            stopwatch.reset();
            stopwatch.start();

            if(i!= 0 && i % 1000 == 0) {
                System.out.println("Total time for all matches so far: " + totalTime/1000l + " seconds.");
            }
        }

        System.out.println("All matches scraped and saved to DB in " + (totalTime / 1000l) + " seconds.");
    }
}
