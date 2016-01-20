import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jonathan Sterling
 */
public class BulkHistoricalScraper extends Scraper {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException, IOException {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        BulkHistoricalScraper bulkHistoricalScraper = new BulkHistoricalScraper();

        java.sql.Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/csgo-scores", "postgres", "postgres");
        DatabaseLink databaseLink = new DatabaseLink(connection);

        for (int i = 0; i <= 13950; i += 1050) {
            System.out.println("Getting games " + i + " - " + i + 1000);
            List<Result> results = bulkHistoricalScraper.getResultsInRange(i, i + 1000);

            databaseLink.insertResults(results);
        }

        System.out.println("All matches scraped and saved to DB in " + stopwatch.elapsedTime(TimeUnit.SECONDS) + " seconds.");
    }
}
