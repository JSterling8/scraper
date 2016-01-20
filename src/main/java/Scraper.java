import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anon on 20/01/2016.
 */
public class Scraper {
    private DateHelper dateHelper = new DateHelper();

    public List<Result> getLatestFiftyResults() throws IOException, ParseException {
        return getResultsInRange(0, 50);
    }

    public List<Result> getResultsInRange(int min, int max) throws ParseException, IOException {
        List<Result> results = new ArrayList<Result>();

        // 13,950 is max as of 20/1/16 (hltv.org/results/13950/
        for (int i = min; i < max; i += 50) {
            Document document = Jsoup.connect("http://www.hltv.org/results/" + i + "/")
                    .timeout(7000)
                    .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                    .get();

            Elements dateHeadings = document.select("div.matchListDateBox");
            Elements matchTypeText = document.select("div.matchTimeCell");
            Elements teamOneNames = document.select(".matchTeam1Cell a");
            Elements teamTwoNames = document.select(".matchTeam2Cell a");
            Elements teamOneScores = document.select(".matchScoreCell span:nth-of-type(1)");
            Elements teamTwoScores = document.select(".matchScoreCell span:nth-of-type(2)");

            String entireResultsBox = document.select("div.centerFade").get(0).outerHtml();
            List<Integer> numOfResultsPerDateIndex = dateHelper.getNumOfResultsPerDateHeading(entireResultsBox, dateHeadings.size());

            if (teamOneNames.size() != teamTwoNames.size()
                    || teamTwoNames.size() != teamOneScores.size()
                    || teamOneScores.size() != teamTwoScores.size()) {
                System.out.println("Elements size mismatch...");

                return new ArrayList<Result>(0); //TODO Throw exception
            }

            List<Date> matchDates = dateHelper.getDateFromHeadings(dateHeadings);

            int resultsLimitForDate = numOfResultsPerDateIndex.get(0);
            int dateIndex = 0;

            for (int j = 0; j < teamOneNames.size(); j++) {
                if (j == resultsLimitForDate) {
                    dateIndex++;

                    resultsLimitForDate = resultsLimitForDate + numOfResultsPerDateIndex.get(dateIndex);
                }

                MatchType matchType = MatchType.BEST_OF_ONE;
                if (matchTypeText.get(j).text().startsWith("Best of 2")) {
                    matchType = MatchType.BEST_OF_TWO;
                } else if (matchTypeText.get(j).text().startsWith("Best of 3")) {
                    matchType = MatchType.BEST_OF_THREE;
                } else if (matchTypeText.get(j).text().startsWith("Best of 4")) {
                    matchType = MatchType.BEST_OF_FOUR;
                } else if (matchTypeText.get(j).text().startsWith("Best of 5")) {
                    matchType = MatchType.BEST_OF_FIVE;
                }

                results.add(new Result(teamOneNames.get(j).text(),
                                teamTwoNames.get(j).text(),
                                Integer.parseInt(teamOneScores.get(j).text()),
                                Integer.parseInt(teamTwoScores.get(j).text()),
                                matchType,
                                matchDates.get(dateIndex))
                );
            }
        }

        return results;
    }
}
