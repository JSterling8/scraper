import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anon on 29/10/2015.
 */
public class Scraper {

    public List<Result> getResults() {
        List<Result> results = new ArrayList<Result>();

        for(int i = 0; i < 250; i += 50){
            try {
                Document document;
                if(i == 0) {
                     document = Jsoup.connect("http://www.hltv.org/results/")
                             .timeout(7000)
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                            .get();
                } else {
                    document = Jsoup.connect("http://www.hltv.org/results/" + i + "/")
                            .timeout(7000)
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                            .get();
                }
                Elements matchType = document.select("div.matchTimeCell");
                Elements teamOneNames = document.select(".matchTeam1Cell a");
                Elements teamTwoNames = document.select(".matchTeam2Cell a");
                Elements teamOneScores = document.select(".matchScoreCell span:nth-of-type(1)");
                Elements teamTwoScores = document.select(".matchScoreCell span:nth-of-type(2)");

                if(teamOneNames.size() != teamTwoNames.size()
                        || teamTwoNames.size() != teamOneScores.size()
                        || teamOneScores.size() != teamTwoScores.size()){
                    System.out.println("Elements size mismatch...");
                }

                for(int j = 0; j < teamOneNames.size(); j++){
                    boolean oneMatch = !matchType.get(j).text().startsWith("Best of");
                    results.add(new Result(teamOneNames.get(j).text(),
                                                teamTwoNames.get(j).text(),
                                                Integer.parseInt(teamOneScores.get(j).text()),
                                                Integer.parseInt(teamTwoScores.get(j).text()),
                                                oneMatch)
                                );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Scraper scraper = new Scraper();
        java.sql.Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/csgo-scores", "postgres", "postgres");

        for(Result result : scraper.getResults()){
            System.out.println("Team One Name: " + result.getNameTeamOne() + "\n" +
                    "Team One Score: " + result.getScoreTeamOne() + "\n" +
                    "Team Two Name: " + result.getNameTeamTwo() + "\n" +
                    "Team Two Score: " + result.getScoreTeamTwo() + "\n" +
                    "One Match?: " + result.isOneMatch() + "\n" + "\n");

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO result (" +
                            "team_one, " +
                            "team_two, " +
                            "score_team_one, " +
                            "score_team_two, " +
                            "one_match" +
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
            statement.setBoolean(5, result.isOneMatch());

            statement.execute();
        }
    }
}
