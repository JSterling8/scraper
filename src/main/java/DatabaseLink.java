import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anon on 20/01/2016.
 */
public class DatabaseLink {
    private final Connection connection;

    public DatabaseLink(Connection connection) {
        this.connection = connection;
    }

    /**
     * @return A list whose first index contains the home team and the second indext the away team of the most recent
     * match in the db
     * @throws SQLException
     */
    public List<String> getLatestResult() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT team_one, team_two FROM result ORDER BY id desc LIMIT 1");
        ResultSet resultSet = statement.executeQuery();

        resultSet.next();

        String latestGameTeamOne = resultSet.getString(1);
        String latestGameTeamTwo = resultSet.getString(2);

        statement.close();

        List<String> teams = new ArrayList<String>(2);
        teams.add(latestGameTeamOne);
        teams.add(latestGameTeamTwo);

        return teams;
    }

    public void insertResults(List<Result> results) throws SQLException {
        for (Result result : results) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO result (" +
                            "team_one, " +
                            "team_two, " +
                            "score_team_one, " +
                            "score_team_two, " +
                            "match_type, " +
                            "match_date," +
                            "series_identifier" +
                            ") " +

                            " values (" +
                            "?, " +
                            "?, " +
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
            statement.setDate(6, result.getDate());
            statement.setLong(7, result.getSeriesIdentifier());

            statement.execute();
        }
    }
}
