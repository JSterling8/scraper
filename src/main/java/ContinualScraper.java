import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anon on 20/01/2016.
 */
public class ContinualScraper extends Scraper {
    private static final long FIVE_SECONDS_IN_MILLIS = 1000l * 60l * 5l;

    private DatabaseLink databaseLink;

    public ContinualScraper() throws SQLException {
        java.sql.Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/csgo-scores", "postgres", "postgres");

        databaseLink = new DatabaseLink(connection);
    }

    public void listenForLatestResults () throws InterruptedException, SQLException, IOException, ParseException {
        while(true) {
            List<String> mostRecentMatchInDb = databaseLink.getLatestResult();

            Document document = Jsoup.connect("http://www.hltv.org/results/0/")
                    .timeout(7000)
                    .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                    .get();

            Elements teamOneNames = document.select(".matchTeam1Cell a");
            Elements teamTwoNames = document.select(".matchTeam2Cell a");

            String latestTeamOneNameOnPage = teamOneNames.get(0).text();
            String latestTeamTwoNameOnPage = teamTwoNames.get(0).text();

            if(!latestTeamOneNameOnPage.equalsIgnoreCase(mostRecentMatchInDb.get(0)) &&
                    !latestTeamTwoNameOnPage.equalsIgnoreCase(mostRecentMatchInDb.get(1))) {
                addRecentlyAddedMatches(mostRecentMatchInDb);
            }

            Thread.sleep(FIVE_SECONDS_IN_MILLIS);
        }
    }

    private void addRecentlyAddedMatches(List<String> mostRecentTeamsInDb) throws IOException, ParseException, SQLException {
        String teamOneInDb = mostRecentTeamsInDb.get(0);
        String teamTwoInDb = mostRecentTeamsInDb.get(1);

        List<Result> resultsOnPage = getLatestFiftyResults();
        List<Result> resultsToAddToDb = new ArrayList<Result>(50);

        for(Result result : resultsOnPage) {
            if(!result.getNameTeamOne().equalsIgnoreCase(teamOneInDb) &&
                    !result.getNameTeamTwo().equalsIgnoreCase(teamTwoInDb)) {
                resultsToAddToDb.add(result);
            } else {
                break;
            }
        }

        databaseLink.insertResults(resultsToAddToDb);
    }

    public static void main(String[] args) throws SQLException, IOException, InterruptedException, ParseException {
        ContinualScraper scraper = new ContinualScraper();

        scraper.listenForLatestResults();
    }
}
