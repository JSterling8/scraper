import com.google.common.base.Stopwatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by anon on 29/10/2015.
 */
public class Scraper {

    //TODO HANDLE ERRORS BETTER
    public List<Result> getResultsInRange(int min, int max) throws ParseException {
        List<Result> results = new ArrayList<Result>();

        // 13,950 is max as of 20/1/16 (hltv.org/results/13950/
        for(int i = min; i < max; i += 50){
            try {
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
                List<Integer> numOfResultsPerDateIndex = getNumOfResultsPerDateHeading(entireResultsBox, dateHeadings.size());

                if(teamOneNames.size() != teamTwoNames.size()
                        || teamTwoNames.size() != teamOneScores.size()
                        || teamOneScores.size() != teamTwoScores.size()){
                    System.out.println("Elements size mismatch...");

                    return new ArrayList<Result>(0); //TODO Throw exception
                }

                List<Date> matchDates = getDateFromHeadings(dateHeadings);

                int resultsLimitForDate = numOfResultsPerDateIndex.get(0);
                int dateIndex = 0;

                for(int j = 0; j < teamOneNames.size(); j++){
                    if(j == resultsLimitForDate) {
                        dateIndex++;

                        resultsLimitForDate = resultsLimitForDate + numOfResultsPerDateIndex.get(dateIndex);
                    }

                    MatchType matchType = MatchType.BEST_OF_ONE;
                    if(matchTypeText.get(j).text().startsWith("Best of 2")) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    private List<Date> getDateFromHeadings(Elements dateHeadings) throws ParseException {
        DateFormat format = new SimpleDateFormat("E, MMMM d yyyy");

        List<Date> dates = new ArrayList<Date>(dateHeadings.size());
        for(Element element : dateHeadings) {
            String dateText = stripDateOrdinal(element.text());
            Long dateTime = format.parse(dateText).getTime();

            dates.add(new Date(dateTime));
        }

        return  dates;
    }

    private String stripDateOrdinal(String text) {
        text = text.replace("0th", "0"); // 10th, 20th, 30th
        text = text.replace("1st", "1");
        text = text.replace("1th", "1"); // 11th
        text = text.replace("2nd", "2");
        text = text.replace("2th", "2"); // 12th
        text = text.replace("3rd", "3");
        text = text.replace("3th", "3"); // 13th
        text = text.replace("4th", "4");
        text = text.replace("5th", "5");
        text = text.replace("6th", "6");
        text = text.replace("7th", "7");
        text = text.replace("8th", "8");
        text = text.replace("9th", "9");

        return text;
    }

    private List<Integer> getNumOfResultsPerDateHeading(String entireResultsBox, int numDates) {
        List<Integer> resultsPerDate = new ArrayList<Integer>(numDates);

        int indexOfDateBoxCurrentlyOn = 0;
        // Loop through every character.
        for(int i = 0; i < entireResultsBox.length() - 16; i++) {
            // When we hit a date box, count the number of results below it until the next date box
            if(entireResultsBox.substring(i, i + 16).equals("matchListDateBox")) {
                int numOfResultsForThisDate = 0;

                i++;
                // Count every instance of matchBox from here until the next <matchListDateBox>
                while( i < entireResultsBox.length() - 16 &&
                        !entireResultsBox.substring(i, i + 16).equals("matchListDateBox")) {
                    if(entireResultsBox.substring(i, i + 12).equals("matchListBox")){
                        numOfResultsForThisDate++;
                    }

                    i++;
                }

                resultsPerDate.add(numOfResultsForThisDate);
                indexOfDateBoxCurrentlyOn++;
                i--; // Needs decerementing so the surrounding for loop picks up that a new matchListDateBox has been found
            }

            if(indexOfDateBoxCurrentlyOn > numDates) {
                break;
            }
        }

        return resultsPerDate;
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, ParseException {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        Scraper scraper = new Scraper();
        java.sql.Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/csgo-scores", "postgres", "postgres");

        for(int i = 0; i <= 13950; i += 1050) {
            System.out.println("Getting games " + i + " - " + i + 1000);
            List<Result> results = scraper.getResultsInRange(i, i + 1000);

            for(Result result : results) {
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO result (" +
                                "team_one, " +
                                "team_two, " +
                                "score_team_one, " +
                                "score_team_two, " +
                                "match_type, " +
                                "match_date" +
                                ") " +

                                " values (" +
                                "?, " +
                                "?, " +
                                "?, " +
                                "?, " +
                                "?, " +
                                "?" +
                                ");");

                statement.setString(1, result.getNameTeamOne());
                statement.setString(2, result.getNameTeamTwo());
                statement.setInt(3, result.getScoreTeamOne());
                statement.setInt(4, result.getScoreTeamTwo());
                statement.setString(5, result.getMatchType().name());
                statement.setDate(6, result.getDate());

                statement.execute();
            }
        }

        System.out.println("All matches scraped and saved to DB in " + stopwatch.elapsedTime(TimeUnit.SECONDS) + " seconds.");
    }
}
