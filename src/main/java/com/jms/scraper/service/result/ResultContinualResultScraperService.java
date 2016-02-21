package com.jms.scraper.service.result;

import com.jms.scraper.model.Result;
import com.jms.scraper.repository.ResultRepository;
import com.jms.scraper.service.RatingService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by anon on 20/01/2016.
 */
@Service
public class ResultContinualResultScraperService extends ResultScraper {
    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private RatingService ratingService;

    private static final long FIVE_MINUTES_IN_MILLIS = 1000l * 60l * 5l;
    private static final Logger logger = LoggerFactory.getLogger(ResultContinualResultScraperService.class);
    private static boolean needToRunStartupMethod = true;

    public ResultContinualResultScraperService() throws SQLException {
    }

    public void listenForLatestResults() throws InterruptedException, SQLException, IOException, ParseException {
        long checkNum = 1;

        while (true) {
            System.out.println("Performing check: " + checkNum);

            List<String> mostRecentMatchInDb = getLatestTeams();

            boolean successfullyRetrieved = false;
            Document document = null;

            while (!successfullyRetrieved) {

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

                    Thread.sleep(1000l * 60l);
                }
            }

            Elements teamOneNames = document.select(".matchTeam1Cell a");
            Elements teamTwoNames = document.select(".matchTeam2Cell a");

            String latestTeamOneNameOnPage = teamOneNames.get(0).text();
            String latestTeamTwoNameOnPage = teamTwoNames.get(0).text();

            if (!latestTeamOneNameOnPage.equalsIgnoreCase(mostRecentMatchInDb.get(0)) ||
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
            if(!result.getTeamOne().equalsIgnoreCase(teamOneInDb) ||
                    !result.getTeamTwo().equalsIgnoreCase(teamTwoInDb)) {
                resultsToAddToDb.add(result);
            } else {
                break;
            }
        }

        // This is so they're loaded into the database from the bottom to the top.
        Collections.reverse(resultsToAddToDb);

        for(Result result : resultsToAddToDb) {
            resultRepository.save(result);
            ratingService.handleRatingForLatestResult();
        }

        System.out.println("The following results were added to the database: ");

        for(Result result : resultsToAddToDb) {
            System.out.println(
                    result.getTeamOne() + "  " + result.getScoreTeamOne() +
                    "  vs  " +
                    result.getScoreTeamTwo() + "  " + result.getTeamTwo() +
                    "  -  " + result.getMatchDate());
        }
    }

    @Scheduled(fixedRate = Long.MAX_VALUE)
    public void keepAlive() throws InterruptedException, SQLException, ParseException, IOException {
        if (needToRunStartupMethod) {
            start();
            needToRunStartupMethod = false;
        }
    }

    public void start() throws SQLException, IOException, InterruptedException, ParseException {
        if(needToRunStartupMethod) {
            logger.info("Starting continual scraper");
            listenForLatestResults();
        } else {
            logger.error("Attempted to start contintinual scraper but it's already running...");
        }
    }

    public List<String> getLatestTeams() {
        Page<Result> resultPage = resultRepository.findAll(new PageRequest(0, 1, Sort.Direction.DESC, "id"));
        Result result = resultPage.getContent().get(0);

        List<String> teams = new ArrayList<String>(2);
        teams.add(result.getTeamOne());
        teams.add(result.getTeamTwo());

        return teams;
    }
}
