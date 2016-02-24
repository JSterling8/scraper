package com.jms.scraper.service.odd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.jms.scraper.model.Odd;
import com.jms.scraper.repository.OddRepository;
import com.jms.scraper.service.helper.DateHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.sql.Date;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by anon on 21/02/2016.
 */
@Service
public class OddScraper {
    private static final long THIRTY_MINUTES_IN_MILLIS = 1000l * 60l * 30l;
    private static final long FIVE_MINUTES_IN_MILLIS = 1000l * 60L * 5l;
    @Autowired
    private DateHelper dateHelper;

    @Autowired
    private OddRepository oddRepository;

    private static final String HOME_TEAM_ODDS_CSS_SELECTOR = "div.col-md-7 div div:nth-of-type(1) h4 span:nth-of-type(1)";
    private static final Logger LOGGER = LoggerFactory.getLogger(OddScraper.class);

    //TODO Name mappings.  G2 == Gamers2, ldlc white == ldlcwhite, ? = %3F == qm, natus vincere == natusvincere
    private ArrayList<String> allTeams = new ArrayList<String>() {{
        add("fnatic");
        add("natusvincere");
        add("luminosity");
        add("virtus.pro");
        add("nip");
        add("dignitas");
        add("mousesports");
        add("gamers2");
        add("liquid");
        add("flipsid3");
        add("optic");
        add("clg");
        add("sk");
        add("e-frag.net");
        add("renegades");
        add("vexed");
        add("ldlcwhite");
        add("qm");
    }};

    public List<Odd> scrapeAll() throws InterruptedException, ParseException {
        LOGGER.info("Beginning scrape for all teams in team list.  Number of teams in list: " + allTeams.size());
        oddRepository.deleteAll();

        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();

        List<Odd> allOdds = getOddsOnWebsiteForSpecifiedTeams(allTeams);

        LOGGER.info("Gathered all odds.  Sorting them so the oldest is first...");
        sortOdds(allOdds);

        LOGGER.info("Removing duplicate entries...");
        removeDuplicates(allOdds);

        LOGGER.info("Saving " + allOdds.size() + " odds to database...");
        oddRepository.save(allOdds);

        LOGGER.info("All odds retrieved and saved to database.  Time taken: " +
                stopwatch.elapsedTime(TimeUnit.SECONDS) + " seconds.");

        return allOdds;
    }

    @Scheduled(fixedRate = Long.MAX_VALUE)
    public void addLatestOddsToDb() throws ParseException, InterruptedException {
        while (true) {
            Stopwatch stopwatch = new Stopwatch();
            stopwatch.start();

            List<Odd> allOddsInDb = Lists.newArrayList(oddRepository.findAll());
            List<Odd> allOddsOnWebsite = getOddsOnWebsiteForSpecifiedTeams(allTeams);

            HashMap<String, List<Odd>> oddsByTeamInDb = splitOddsIntoPerTeamLists(allOddsInDb);

            List<Odd> oddsToAddToDb = new ArrayList<Odd>();

            for(Odd webOdd : allOddsOnWebsite) {
                List<Odd> currentTeamsOddsInDb = oddsByTeamInDb.get(webOdd.getTeamOne());
                List<Odd> currentViceVersaTeamOddsInDb = oddsByTeamInDb.get(webOdd.getTeamTwo());

                if(!currentTeamsOddsInDb.contains(webOdd) && !currentViceVersaTeamOddsInDb.contains(getViceVersaOdd(webOdd))) {
                    oddsToAddToDb.add(webOdd);
                }
            }

            removeDuplicates(oddsToAddToDb);
            sortOdds(oddsToAddToDb);

            LOGGER.info("Finished pass.  Found " + oddsToAddToDb.size() + " new odds.  " +
                    "Saving to db if any, then sleeping 30 minutes.  Process took: " +
                    stopwatch.elapsedTime(TimeUnit.SECONDS) + " seconds.");

            oddRepository.save(oddsToAddToDb);

            Thread.sleep(THIRTY_MINUTES_IN_MILLIS);
        }
    }

    public Iterable<Odd> getAllFromDb() {
        return oddRepository.findAll();
    }

    public Iterable<Odd> getAllForDateFromDb(String dateText) throws ParseException {
        Date date = dateHelper.getDateFromHyphenatedText(dateText);

        return oddRepository.findByMatchDate(date);
    }

    public Iterable<Odd> getAllForTeamFromDb(String team) {
        return oddRepository.findByTeamOneOrTeamTwo(team, team);
    }

    private HashMap<String, List<Odd>> splitOddsIntoPerTeamLists(List<Odd> odds) {
        HashMap<String, List<Odd>> splitByTeam = new HashMap<String, List<Odd>>();

        for(Odd odd : odds) {
            if(!splitByTeam.containsKey(odd.getTeamOne()) && !splitByTeam.containsKey(odd.getTeamOne())) {
                // CASE - Neither team in map - Add odd for both
                List<Odd> teamOneOddList = new ArrayList<Odd>();
                teamOneOddList.add(odd);
                splitByTeam.put(odd.getTeamOne(), teamOneOddList);

                List<Odd> teamTwoOddList = new ArrayList<Odd>();
                teamTwoOddList.add(getViceVersaOdd(odd));
                splitByTeam.put(odd.getTeamTwo(), teamTwoOddList);

            } else if(splitByTeam.containsKey(odd.getTeamOne()) && !splitByTeam.containsKey(odd.getTeamTwo())) {
                // CASE - Only one team in map - Add odd for one if not there, always add odd for other
                List<Odd> teamOneOddList = splitByTeam.get(odd.getTeamOne());
                if(!teamOneOddList.contains(odd)) {
                    teamOneOddList.add(odd);
                    splitByTeam.put(odd.getTeamOne(), teamOneOddList);
                }

                List<Odd> teamTwoOddList = new ArrayList<Odd>();
                teamTwoOddList.add(getViceVersaOdd(odd));
                splitByTeam.put(odd.getTeamTwo(), teamTwoOddList);

            } else if(splitByTeam.containsKey(odd.getTeamTwo()) && !splitByTeam.containsKey(odd.getTeamOne())) {
                // CASE - Only one team in map - Add odd for one if not there, always add odd for other
                List<Odd> teamTwoOddList = splitByTeam.get(odd.getTeamTwo());
                if(!teamTwoOddList.contains(getViceVersaOdd(odd))) {
                    teamTwoOddList.add(getViceVersaOdd(odd));
                    splitByTeam.put(odd.getTeamTwo(), teamTwoOddList);
                }

                List<Odd> teamOneOddList = new ArrayList<Odd>();
                teamOneOddList.add(odd);
                splitByTeam.put(odd.getTeamOne(), teamOneOddList);
            } else {
                // CASE - Both teams in map already - Add odd for each if not there
                List<Odd> teamOneOddList = splitByTeam.get(odd.getTeamOne());
                List<Odd> teamTwoOddList = splitByTeam.get(odd.getTeamTwo());

                if(!teamOneOddList.contains(odd)) {
                    teamOneOddList.add(odd);
                    splitByTeam.put(odd.getTeamOne(), teamOneOddList);
                }

                if(!teamTwoOddList.contains(getViceVersaOdd(odd))) {
                    teamTwoOddList.add(getViceVersaOdd(odd));
                    splitByTeam.put(odd.getTeamTwo(), teamTwoOddList);
                }
            }
        }

        return splitByTeam;
    }

    private Odd getViceVersaOdd(Odd odd) {
        Odd viceVersaOdd = new Odd();

        viceVersaOdd.setTeamOne(odd.getTeamTwo());
        viceVersaOdd.setTeamTwo(odd.getTeamOne());
        viceVersaOdd.setOddsTeamOne(odd.getOddsTeamTwo());
        viceVersaOdd.setOddsTeamTwo(odd.getOddsTeamOne());
        viceVersaOdd.setWinningTeam(odd.getWinningTeam());
        viceVersaOdd.setMatchDate(odd.getMatchDate());

        return viceVersaOdd;
    }

    private void sortOdds(List<Odd> allOdds) {
        Comparator<Odd> oddComparator = getOddComparator();

        Collections.sort(allOdds, oddComparator);
    }

    private List<Odd> getOddsOnWebsiteForSpecifiedTeams(List<String> teams) throws ParseException, InterruptedException {
        List<Odd> allOdds = new ArrayList<Odd>();

        for (String team : teams) {
            Document document = null;
            String url = "http://www.csgonuts.com/odds?t1=" + team;

            document = loadUrl(document, url);

            List<String> opponentNames = getOpponentsNames(document);
            List<Double> homeTeamOdds = getHomeTeamOdds(document);
            List<Date> matchDates = getMatchDates(document);

            Assert.isTrue(opponentNames.size() == homeTeamOdds.size() && homeTeamOdds.size() == matchDates.size(),
                    "ERROR: Have not found the same number of names, odds, and dates for team: " + team);

            List<String> winners = getWinners(document, team, opponentNames);

            allOdds.addAll(getOddDaos(team, opponentNames, homeTeamOdds, matchDates, winners));
        }

        return allOdds;
    }

    private void removeDuplicates(List<Odd> allOdds) {
        for (int i = allOdds.size() - 1; i >= 1; i--) {
            Odd current = allOdds.get(i);
            for (int j = i - 1; j >= 0; j--) {
                Odd previous = allOdds.get(j);
                if (previous.getMatchDate().before(current.getMatchDate())) {
                    break;
                } else if (previous.getTeamOne().equalsIgnoreCase(current.getTeamTwo()) &&
                        previous.getTeamTwo().equalsIgnoreCase(current.getTeamOne()) &&
                        previous.getOddsTeamOne().equals(current.getOddsTeamTwo()) &&
                        previous.getOddsTeamTwo().equals(current.getOddsTeamOne()) &&
                        previous.getWinningTeam().equals(current.getWinningTeam())) {
                    // If there's another match with the same teams/odds, just flip-flopped, with the same winner,
                    // then remove it.  There should only be one duplicate, if any, so now we can break too.
                    allOdds.remove(i);
                    break;
                }
            }
        }
    }

    private Comparator<Odd> getOddComparator() {
        return new Comparator<Odd>() {
            public int compare(Odd o1, Odd o2) {
                if (o1.getMatchDate().after(o2.getMatchDate())) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };
    }

    private List<Odd> getOddDaos(String team, List<String> opponentNames, List<Double> homeTeamOdds, List<Date> matchDates, List<String> winners) {
        List<Odd> odds = new ArrayList<Odd>(winners.size());
        for (int i = 0; i < winners.size(); i++) {
            Odd odd = new Odd();
            odd.setTeamOne(team);
            odd.setTeamTwo(opponentNames.get(i));
            odd.setOddsTeamOne(homeTeamOdds.get(i));
            odd.setOddsTeamTwo(100d - homeTeamOdds.get(i));
            odd.setMatchDate(matchDates.get(i));
            odd.setWinningTeam(winners.get(i));

            odds.add(odd);
        }

        return odds;
    }

    private List<String> getWinners(Document document, String homeTeamName, List<String> opponentNames) {
        Elements homeTeamOdds = document.select(HOME_TEAM_ODDS_CSS_SELECTOR);
        Assert.isTrue(homeTeamOdds.size() == opponentNames.size(), "ERROR: Not as many odds as there are opponents.");

        List<String> winners = new ArrayList<String>(homeTeamOdds.size());
        for (int i = 0; i < homeTeamOdds.size(); i++) {
            if (homeTeamOdds.get(i).className().endsWith("success")) {
                winners.add(homeTeamName);
            } else {
                winners.add(opponentNames.get(i));
            }
        }

        return winners;
    }

    private List<Date> getMatchDates(Document document) throws ParseException {
        Elements dateElements = document.select("div.col-md-7 div > span.label[style*=background-color:#b8b8b8]");
        Iterator iterator = dateElements.iterator();
        List<Date> matchDates = new ArrayList<Date>();

        while (iterator.hasNext()) {
            Element element = (Element) iterator.next();
            String elementText = element.text();

            matchDates.add(dateHelper.getDateFromCsgoNutsText(elementText));
        }

        return matchDates;
    }

    private Document loadUrl(Document document, String url) throws InterruptedException {
        boolean successfullyRetrieved = false;

        while (!successfullyRetrieved) {
            try {
                document = Jsoup.connect(url)
                        .timeout(7000)
                        .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36")
                        .get();

                successfullyRetrieved = true;
            } catch (Exception e) {
                LOGGER.error("Failed to get load page " + url + " due to the following exception: " + e.getClass());

                successfullyRetrieved = false;

                Thread.sleep(5000l);
            }
        }
        return document;
    }

    private List<Double> getHomeTeamOdds(Document document) {
        Elements homeTeamOddsElements = document.select("div.col-md-7 div div:nth-of-type(1) h4 span:nth-of-type(1)");
        List<Double> homeTeamOdds = new ArrayList<Double>();

        for (Element homeTeamOddElement : homeTeamOddsElements) {
            String stringOdds = homeTeamOddElement.text();
            // Get the '%' off the end
            stringOdds = stringOdds.substring(0, stringOdds.length() - 1);

            homeTeamOdds.add(Double.parseDouble(stringOdds));
        }

        return homeTeamOdds;
    }

    private List<String> getOpponentsNames(Document document) {
        Elements allTeamNames = document.select("div.col-md-7 > div a:nth-of-type(1)");
        List<String> opponentNames = new ArrayList<String>();
        Iterator iterator = allTeamNames.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Element currentElement = (Element) iterator.next();

            // If it's not a team name (but rather, a BO1, BO3, etc.), remove it and move onto the next element
            if (currentElement.text().startsWith("BO")) {
                iterator.remove();
                continue;
            }

            // Every other element should be an opponent
            if (i % 2 == 1) {
                opponentNames.add(currentElement.text().toLowerCase());
            }

            i++;
        }
        return opponentNames;
    }
}
