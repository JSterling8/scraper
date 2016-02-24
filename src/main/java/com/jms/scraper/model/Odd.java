package com.jms.scraper.model;

import javax.persistence.*;
import java.sql.Date;

/**
 * Created by anon on 21/02/2016.
 */
@Entity
public class Odd {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "team_one")
    private String teamOne;

    @Column(name = "team_two")
    private String teamTwo;

    @Column(name = "odds_team_one")
    private Double oddsTeamOne;

    @Column(name = "odds_team_two")
    private Double oddsTeamTwo;

    @Column(name = "winning_team")
    private String winningTeam;

    @Column(name = "match_date")
    private Date matchDate;

    public Odd() {
    }

    public Odd(Long id, String teamOne, String teamTwo, Double oddsTeamOne, Double oddsTeamTwo, String winningTeam, Date matchDate) {
        this.id = id;
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        this.oddsTeamOne = oddsTeamOne;
        this.oddsTeamTwo = oddsTeamTwo;
        this.winningTeam = winningTeam;
        this.matchDate = matchDate;
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

    public Double getOddsTeamOne() {
        return oddsTeamOne;
    }

    public void setOddsTeamOne(Double oddsTeamOne) {
        this.oddsTeamOne = oddsTeamOne;
    }

    public Double getOddsTeamTwo() {
        return oddsTeamTwo;
    }

    public void setOddsTeamTwo(Double oddsTeamTwo) {
        this.oddsTeamTwo = oddsTeamTwo;
    }

    public String getWinningTeam() {
        return winningTeam;
    }

    public void setWinningTeam(String winningTeam) {
        this.winningTeam = winningTeam;
    }

    public Date getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(Date matchDate) {
        this.matchDate = matchDate;
    }

    @Override
    public String toString() {
        return teamOne + "  " + oddsTeamOne + "  vs  " + oddsTeamTwo + "  " + teamTwo + "  --  " + matchDate.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof  Odd)) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        Odd odd = (Odd) obj;
        if(odd.getTeamOne().equals(this.getTeamOne()) &&
                odd.getTeamTwo().equals(this.getTeamTwo()) &&
                odd.getWinningTeam().equals(this.getWinningTeam()) &&
                odd.getOddsTeamOne().equals(this.getOddsTeamOne()) &&
                odd.getOddsTeamTwo().equals(this.getOddsTeamTwo()) &&
                odd.getMatchDate().equals(this.getMatchDate())) {
            return true;
        } else {
            return false;
        }
    }
}