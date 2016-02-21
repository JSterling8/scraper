package com.jms.scraper.model;

import com.jms.scraper.service.helper.MatchType;

import javax.persistence.*;
import java.sql.Date;

/**
 * Created by anon on 30/01/2016.
 */
@Table(name = "resul")
@Entity
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "team_one")
    private String teamOne;

    @Column(name = "team_two")
    private String teamTwo;

    @Column(name = "score_team_one")
    private int scoreTeamOne;

    @Column(name = "score_team_two")
    private int scoreTeamTwo;

    @Column(name = "match_type")
    private String matchType;

    @Column(name = "match_date")
    private Date matchDate;

    @Column(name = "series_identifier")
    private Long seriesIdentifier;

    public Result() {
    }

    public Result(Long id, String teamOne, String teamTwo, int scoreTeamOne, int scoreTeamTwo, String matchType, Date matchDate, Long seriesIdentifier) {
        this.id = id;
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        this.scoreTeamOne = scoreTeamOne;
        this.scoreTeamTwo = scoreTeamTwo;
        this.matchType = matchType;
        this.matchDate = matchDate;
        this.seriesIdentifier = seriesIdentifier;
    }

    public Result(String teamOne, String teamTwo, int scoreTeamOne, int scoreTeamTwo, MatchType matchType, Date matchDate, Long seriesIdentifier) {
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        this.scoreTeamOne = scoreTeamOne;
        this.scoreTeamTwo = scoreTeamTwo;
        this.matchType = matchType.toString();
        this.matchDate = matchDate;
        this.seriesIdentifier = seriesIdentifier;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTeamOne() {
        return teamOne;
    }

    public void setTeamOne(String teamOne) {
        this.teamOne = teamOne;
    }

    public String getTeamTwo() {
        return teamTwo;
    }

    public void setTeamTwo(String teamTwo) {
        this.teamTwo = teamTwo;
    }

    public int getScoreTeamOne() {
        return scoreTeamOne;
    }

    public void setScoreTeamOne(int scoreTeamOne) {
        this.scoreTeamOne = scoreTeamOne;
    }

    public int getScoreTeamTwo() {
        return scoreTeamTwo;
    }

    public void setScoreTeamTwo(int scoreTeamTwo) {
        this.scoreTeamTwo = scoreTeamTwo;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public Date getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(Date matchDate) {
        this.matchDate = matchDate;
    }

    public Long getSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(Long seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }

    @Override
    public String toString() {
        return teamOne + "  " + scoreTeamOne + "  vs  " + scoreTeamTwo + "  " + teamTwo + "  --  " + matchDate.toString();
    }
}
