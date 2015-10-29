/**
 * Created by anon on 29/10/2015.
 */
public class Result {
    private String nameTeamOne;
    private String nameTeamTwo;
    private int scoreTeamOne;
    private int scoreTeamTwo;
    private boolean oneMatch;

    public Result(String nameTeamOne, String nameTeamTwo, int scoreTeamOne, int scoreTeamTwo, boolean oneMatch) {
        this.nameTeamOne = nameTeamOne;
        nameTeamOne.replace("'", "\\'");
        this.nameTeamTwo = nameTeamTwo;
        nameTeamTwo.replace("'", "\\'");
        this.scoreTeamOne = scoreTeamOne;
        this.scoreTeamTwo = scoreTeamTwo;
        this.oneMatch = oneMatch;
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

    public boolean isOneMatch() {
        return oneMatch;
    }

    public void setOneMatch(boolean oneMatch) {
        this.oneMatch = oneMatch;
    }
}
