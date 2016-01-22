import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jonathan Sterling
 */
public class BulkHistoricalScraper extends Scraper {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException, IOException, InterruptedException {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        BulkHistoricalScraper bulkHistoricalScraper = new BulkHistoricalScraper();

        java.sql.Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/csgo-scores", "postgres", "postgres");
        DatabaseLink databaseLink = new DatabaseLink(connection);

        long totalTime = 0;

        for (int i = 4900; i <= 14000; i += 50) {
            System.out.println("Getting games " + i + " - " + (i + 50));
            List<Result> results = bulkHistoricalScraper.getResultsInRange(i, i + 50);

            // This is so they're loaded into the database from the bottom to the top (i.e., newest will have highest id in db)
            Collections.reverse(results);

            databaseLink.insertResults(results);

            System.out.println("50 match series (" + results.size() + " matches) saved to DB in " + stopwatch.elapsedTime(TimeUnit.SECONDS) + " seconds.");

            totalTime += stopwatch.elapsedMillis();
            stopwatch.reset();
            stopwatch.start();
        }

        System.out.println("All matches scraped and saved to DB in " + (totalTime / 1000l) + " seconds.");
    }
}
