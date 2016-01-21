import java.sql.Date;

/**
 * @author Jonathan Sterling
 */
public class Result {
    private String nameTeamOne;
    private String nameTeamTwo;
    private int scoreTeamOne;
    private int scoreTeamTwo;
    private MatchType matchType;
    private Date date;
    private Long seriesIdentifier;

    public Result(String nameTeamOne, String nameTeamTwo, int scoreTeamOne, int scoreTeamTwo, MatchType matchType, Date date, Long seriesIdentifier) {
        this.nameTeamOne = nameTeamOne;
        nameTeamOne.replace("'", "\\'");
        this.nameTeamTwo = nameTeamTwo;
        nameTeamTwo.replace("'", "\\'");
        this.scoreTeamOne = scoreTeamOne;
        this.scoreTeamTwo = scoreTeamTwo;
        this.matchType = matchType;
        this.date = date;
        this.seriesIdentifier = seriesIdentifier;
    }

    public String getNameTeamOne() {
        return nameTeamOne;
    }

    public void setNameTeamOne(String nameTeamOne) {
        this.nameTeamOne = nameTeamOne;
    }

    public String getNameTeamTwo() {
        return nameTeamTwo;
    }

    public void setNameTeamTwo(String nameTeamTwo) {
        this.nameTeamTwo = nameTeamTwo;
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

    public MatchType getMatchType() {
        return matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getSeriesIdentifier() {
        return seriesIdentifier;
    }

    public void setSeriesIdentifier(Long seriesIdentifier) {
        this.seriesIdentifier = seriesIdentifier;
    }
}
