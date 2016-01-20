import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anon on 29/10/2015.
 */
public class Scraper {

    public List<Result> getResultsInRange(int min, int max) {
        List<Result> results = new ArrayList<Result>();

        // 14,000 is max as of 20/1/16 (hltv.org/results/14000
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
                HashMap<String, Integer> dateAndNumOfEntries = getNumOfResultsPerDateIndex(entireResultsBox, dateHeadings);

                if(teamOneNames.size() != teamTwoNames.size()
                        || teamTwoNames.size() != teamOneScores.size()
                        || teamOneScores.size() != teamTwoScores.size()){
                    System.out.println("Elements size mismatch...");
                }

                for(int j = 0; j < teamOneNames.size(); j++){
                    MatchType matchType = MatchType.UNDEFINED;
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
                                                matchType)
                                );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    private HashMap<String, Integer> getNumOfResultsPerDateIndex(String entireResultsBox, Elements dateHeadings) {
        HashMap<String, Integer> resultsPerDate = new HashMap<String, Integer>(dateHeadings.size());

        int indexOfDateBoxCurrentlyOn = 0;
        // Loop through every character.
        for(int i = 0; i < entireResultsBox.length() - 16; i++) {
            // When we hit a date box, count the number of results below it until the next date box
            if(entireResultsBox.substring(i, i + 16).equals("matchListDateBox")) {
                int numOfResultsForThisDate = 0;

                i++;
                // Count every instance of matchBox from here on out, until the next <matchListDateBox>
                while( i < entireResultsBox.length() - 16 && !entireResultsBox.substring(i, i + 16).equals("matchListDateBox")) {
                    if(entireResultsBox.substring(i, i + 12).equals("matchListBox")){
                        numOfResultsForThisDate++;
                    }

                    i++;
                }

                resultsPerDate.put(dateHeadings.get(indexOfDateBoxCurrentlyOn).text(), numOfResultsForThisDate);
                indexOfDateBoxCurrentlyOn++;
            }
        }

        return resultsPerDate;
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Scraper scraper = new Scraper();
        java.sql.Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/csgo-scores", "postgres", "postgres");

        //TODO Get match date! Important!

        for(int i = 0; i < 14000; i += 1050){
            for(Result result : scraper.getResultsInRange(i, i + 1000)) {
                System.out.println("Team One Name: " + result.getNameTeamOne() + "\n" +
                        "Team One Score: " + result.getScoreTeamOne() + "\n" +
                        "Team Two Name: " + result.getNameTeamTwo() + "\n" +
                        "Team Two Score: " + result.getScoreTeamTwo() + "\n" +
                        "One Match?: " + result.getMatchType() + "\n" + "\n");

                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO result (" +
                                "team_one, " +
                                "team_two, " +
                                "score_team_one, " +
                                "score_team_two, " +
                                "match_types" +
                                ") " +

                                " values (" +
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

                statement.execute();
            }
        }
    }
}
