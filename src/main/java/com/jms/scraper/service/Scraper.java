package com.jms.scraper.service;

import com.jms.scraper.model.Result;
import com.jms.scraper.service.helper.DateHelper;
import com.jms.scraper.service.helper.MatchType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anon on 20/01/2016.
 */
@Service
public class Scraper {
    @Autowired
    private DateHelper dateHelper = new DateHelper();

    public List<Result> getLatestFiftyResults() throws IOException, ParseException, InterruptedException {
        return getResultsInRange(0, 50);
    }

    public List<Result> getResultsInRange(int min, int max) throws ParseException, IOException, InterruptedException {
        List<Result> results = new ArrayList<Result>();

        // 13,950 is max as of 20/1/16 (hltv.org/results/13950/
        for (int i = min; i < max; i += 50) {

            boolean successfullyRetrieved = false;
            Document document = null;
            String url = "http://www.hltv.org/results/" + i + "/";

            while(!successfullyRetrieved) {
                try {
                    document = Jsoup.connect(url)
                            .timeout(7000)
                            .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                            .get();

                    successfullyRetrieved = true;
                } catch (Exception e) {
                    System.err.println("Failed to get load page " + url + " due to the following exception: " + e.getClass());

                    successfullyRetrieved = false;

                    Thread.sleep(5000l);
                }
            }
            Elements dateHeadings = document.select("div.matchListDateBox");
            Elements matchTypeText = document.select("div.matchTimeCell");
            Elements teamOneNames = document.select(".matchTeam1Cell a");
            Elements teamTwoNames = document.select(".matchTeam2Cell a");
            Elements teamOneScores = document.select(".matchScoreCell span:nth-of-type(1)");
            Elements teamTwoScores = document.select(".matchScoreCell span:nth-of-type(2)");
            Elements detailsLinks = document.select("div.matchActionCell a");

            String entireResultsBox = document.select("div.centerFade").get(0).outerHtml();
            List<Integer> numOfResultsPerDateIndex = dateHelper.getNumOfResultsPerDateHeading(entireResultsBox, dateHeadings.size());

            if (teamOneNames.size() != teamTwoNames.size()
                    || teamTwoNames.size() != teamOneScores.size()
                    || teamOneScores.size() != teamTwoScores.size()) {
                System.out.println("Elements size mismatch...");

                throw new RuntimeException("Unexpected response for team one names and scores.  The results page might've changed...");
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

                Date matchDate = matchDates.get(dateIndex);

                if(matchType == MatchType.BEST_OF_ONE) {
                    int teamOneScore = Integer.parseInt(teamOneScores.get(j).text());
                    int teamTwoScore = Integer.parseInt(teamTwoScores.get(j).text());

                    // If a best of one just shows as 1-0, record it as 16-8.
                    if(teamOneScore < 15 && teamTwoScore < 15) {
                        teamOneScore = (teamOneScore > 0) ? teamOneScore * 16 : 8;
                        teamTwoScore = (teamTwoScore > 0) ? teamTwoScore * 16 : teamOneScore / 2;
                    }

                    results.add(new Result(teamOneNames.get(j).text(),
                                    teamTwoNames.get(j).text(),
                                    teamOneScore,
                                    teamTwoScore,
                                    matchType,
                                    matchDate,
                                    matchDate.getTime() + Integer.toUnsignedLong(j))
                    );
                } else {
                    String urlToLoadForAllDetails = "http://www.hltv.org" + detailsLinks.get(j).attr("href");

                    results.addAll(
                            getAllResultsFromDetailsPage(
                                urlToLoadForAllDetails,
                                teamOneNames.get(j).text(),
                                teamTwoNames.get(j).text(),
                                    matchType,
                                    matchDate,
                                    matchDate.getTime() + Integer.toUnsignedLong(j),
                                    Integer.parseInt(teamOneScores.get(j).text()),
                                    Integer.parseInt(teamTwoScores.get(j).text())));
                }
            }
        }

        return results;
    }

    private List<Result> getAllResultsFromDetailsPage(String url, String teamOne, String teamTwo, MatchType matchType, Date date, long seriesIdentifier, int seriesScoreTeamOne, int seriesScoreTeamTwo) throws IOException, InterruptedException {
        List<Result> results = new ArrayList<Result>(5);

        boolean successfullyRetrieved = false;
        Document document = null;

        while(!successfullyRetrieved) {
            try {
                document = Jsoup.connect(url)
                        .timeout(7000)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                        .get();

                successfullyRetrieved = true;
            } catch (SocketTimeoutException e) {
                System.err.println("Failed to load URL: " + url);

                successfullyRetrieved = false;

                Thread.sleep(5000l);
            }
        }

        Elements teamOneScores = document.select("div.hotmatchbox div.hotmatchbox:nth-of-type(3n) > span:nth-of-type(1)");
        Elements teamTwoScores = document.select("div.hotmatchbox div.hotmatchbox:nth-of-type(3n) > span:nth-of-type(2)");

        for(int i = 0; i < teamOneScores.size(); i++) {
            try {
                int teamOneScore = Integer.parseInt(teamOneScores.get(i).text());
                int teamTwoScore = Integer.parseInt(teamTwoScores.get(i).text());

                if (teamOneScore < 15 && teamTwoScore < 15) {
                    // Invalid scores (maybe it's showing a 1-0 default win on a map)

                    continue;
                }

                    results.add(new Result(
                        teamOne,
                        teamTwo,
                        teamOneScore,
                        teamTwoScore,
                        matchType,
                        date,
                        seriesIdentifier
                ));
            } catch (NumberFormatException e) {
                // Probably caught an errant span

                break;
            }
        }

        // If the details page doesn't have individual map scores, just store the series score * 8 for the winner, and 8 for the loser,
        // so a 2-0 or 2-1 win is recorded as 16-8.  A 3-0 win would be 24-8.  Inaccurate, but a best guess measure.
        if (results.size() == 0){
            int teamOneScore = seriesScoreTeamOne > 0 ? seriesScoreTeamOne * 8 : 8;
            int teamTwoScore = seriesScoreTeamTwo > 0 ? seriesScoreTeamTwo * 8 : 8;

            results.add(new Result(
                    teamOne,
                    teamTwo,
                    teamOneScore,
                    teamTwoScore,
                    matchType,
                    date,
                    seriesIdentifier
            ));
        }

        return results;
    }
}
