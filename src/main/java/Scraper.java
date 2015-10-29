import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
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
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                            .get();
                } else {
                    document = Jsoup.connect("http://www.hltv.org/results/" + i + "/")
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                            .get();
                }
                Elements matchType = document.select("div.matchTimeCell");
                Elements teamNamesOne = document.select(".matchTeam1Cell a");
                Elements teamNamesTwo = document.select(".matchTeam2Cell a");
                Elements teamScoresOne = document.select(".matchScoreCell span:nth-of-type(1)");
                Elements teamScoresTwo = document.select(".matchScoreCell span:nth-of-type(2)");

                if(teamNamesOne.size() != teamNamesTwo.size()
                        || teamNamesTwo.size() != teamScoresOne.size()
                        || teamScoresOne.size() != teamScoresTwo.size()){
                    System.out.println("Elements size mismatch...");
                }

                for(int j = 0; j < teamNamesOne.size(); j++){
                    boolean oneMatch = !matchType.get(j).text().startsWith("Best of");
                    results.add(new Result(teamNamesOne.get(j).text(),
                                                teamNamesTwo.get(j).text(),
                                                Integer.parseInt(teamScoresOne.get(j).text()),
                                                Integer.parseInt(teamScoresTwo.get(j).text()),
                                                oneMatch)
                                );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    public static void main(String[] args){
        Scraper scraper = new Scraper();
        for(Result result : scraper.getResults()){
            System.out.println("Team One Name: " + result.getNameTeamOne() + "\n" +
                    "Team One Score: " + result.getScoreTeamOne() + "\n" +
                    "Team Two Name: " + result.getNameTeamTwo() + "\n" +
                    "Team Two Score: " + result.getScoreTeamTwo() + "\n" +
                    "One Match?: " + result.isOneMatch() + "\n" + "\n");
        }
    }
}
