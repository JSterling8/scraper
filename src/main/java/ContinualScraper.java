import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by anon on 20/01/2016.
 */
public class ContinualScraper extends Scraper {
    private static final long FIVE_MINUTES_IN_MILLIS = 1000l * 60l * 5l;

    private DatabaseLink databaseLink;

    public ContinualScraper() throws SQLException {
        java.sql.Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/csgo-scores", "postgres", "postgres");

        databaseLink = new DatabaseLink(connection);
    }

    public void listenForLatestResults () throws InterruptedException, SQLException, IOException, ParseException {
        while(true) {
            List<String> mostRecentMatchInDb = databaseLink.getLatestResult();

            boolean successfullyRetrieved = false;
            Document document = null;

            long checkNum = 1;
            while(!successfullyRetrieved) {
                System.out.println("Performing check: " + checkNum);

                String url = "http://www.hltv.org/results/0/";

                try {
                    document = Jsoup.connect(url)
                            .timeout(7000)
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                            .get();

                    successfullyRetrieved = true;
                } catch (Exception e) {
                    System.err.println("Failed to load " + url + " in continual scraper due to the following exception: " + e.getClass());

                    successfullyRetrieved = false;

                    Thread.sleep(5000l);
                }
            }

            Elements teamOneNames = document.select(".matchTeam1Cell a");
            Elements teamTwoNames = document.select(".matchTeam2Cell a");

            String latestTeamOneNameOnPage = teamOneNames.get(0).text();
            String latestTeamTwoNameOnPage = teamTwoNames.get(0).text();

            if(!latestTeamOneNameOnPage.equalsIgnoreCase(mostRecentMatchInDb.get(0)) &&
                    !latestTeamTwoNameOnPage.equalsIgnoreCase(mostRecentMatchInDb.get(1))) {
                System.out.println("Identified new match(es).  Adding to database...");
                addRecentlyAddedMatches(mostRecentMatchInDb);
            }

            System.out.println("No new matches found.  Sleeping for five minutes before reattempting.");
            Thread.sleep(FIVE_MINUTES_IN_MILLIS);

            checkNum++;
        }
    }

    private void addRecentlyAddedMatches(List<String> mostRecentTeamsInDb) throws IOException, ParseException, SQLException, InterruptedException {
        String teamOneInDb = mostRecentTeamsInDb.get(0);
        String teamTwoInDb = mostRecentTeamsInDb.get(1);

        List<Result> resultsOnPage = getLatestFiftyResults();
        List<Result> resultsToAddToDb = new ArrayList<Result>();

        for(Result result : resultsOnPage) {
            if(!result.getNameTeamOne().equalsIgnoreCase(teamOneInDb) &&
                    !result.getNameTeamTwo().equalsIgnoreCase(teamTwoInDb)) {
                resultsToAddToDb.add(result);
            } else {
                break;
            }
        }

        // This is so they're loaded into the database from the bottom to the top.
        Collections.reverse(resultsToAddToDb);

        databaseLink.insertResults(resultsToAddToDb);
    }

    public static void main(String[] args) throws SQLException, IOException, InterruptedException, ParseException {
        ContinualScraper scraper = new ContinualScraper();

        scraper.listenForLatestResults();
    }
}
