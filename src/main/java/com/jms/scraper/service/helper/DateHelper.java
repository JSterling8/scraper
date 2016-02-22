package com.jms.scraper.service.helper;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anon on 20/01/2016.
 */
@Component
public class DateHelper {

    public List<Date> getDateFromHltvHeadings(Elements dateHeadings) throws ParseException {
        DateFormat format = new SimpleDateFormat("E, MMMM d yyyy");

        List<Date> dates = new ArrayList<Date>(dateHeadings.size());
        for (Element element : dateHeadings) {
            String dateText = stripDateOrdinal(element.text());
            Long dateTime = format.parse(dateText).getTime();

            dates.add(new Date(dateTime));
        }

        return dates;
    }

    public String stripDateOrdinal(String text) {
        text = text.replace("0th", "0"); // 10th, 20th, 30th
        text = text.replace("1st", "1");
        text = text.replace("1th", "1"); // 11th
        text = text.replace("2nd", "2");
        text = text.replace("2th", "2"); // 12th
        text = text.replace("3rd", "3");
        text = text.replace("3th", "3"); // 13th
        text = text.replace("4th", "4");
        text = text.replace("5th", "5");
        text = text.replace("6th", "6");
        text = text.replace("7th", "7");
        text = text.replace("8th", "8");
        text = text.replace("9th", "9");

        return text;
    }

    public List<Integer> getNumOfResultsPerHltvDateHeading(String entireResultsBox, int numDates) {
        List<Integer> resultsPerDate = new ArrayList<Integer>(numDates);

        int indexOfDateBoxCurrentlyOn = 0;
        // Loop through every character.
        for (int i = 0; i < entireResultsBox.length() - 16; i++) {
            // When we hit a date box, count the number of results below it until the next date box
            if (entireResultsBox.substring(i, i + 16).equals("matchListDateBox")) {
                int numOfResultsForThisDate = 0;

                i++;
                // Count every instance of matchBox from here until the next <matchListDateBox>
                while (i < entireResultsBox.length() - 16 &&
                        !entireResultsBox.substring(i, i + 16).equals("matchListDateBox")) {
                    if (entireResultsBox.substring(i, i + 12).equals("matchListBox")) {
                        numOfResultsForThisDate++;
                    }

                    i++;
                }

                resultsPerDate.add(numOfResultsForThisDate);
                indexOfDateBoxCurrentlyOn++;
                i--; // Needs decerementing so the surrounding for loop picks up that a new matchListDateBox has been found
            }

            if (indexOfDateBoxCurrentlyOn > numDates) {
                break;
            }
        }

        return resultsPerDate;
    }

    public Date getDateFromCsgoNutsText(String dateText) throws ParseException {
        DateFormat format = new SimpleDateFormat("MMMM d yyyy");

        dateText = dateText.trim();
        dateText = stripDateOrdinal(dateText);
        Long dateTime = format.parse(dateText).getTime();

        return new Date(dateTime);
    }

    public Date getDateFromHyphenatedText(String dateText) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        dateText = dateText.trim();
        dateText = stripDateOrdinal(dateText);
        Long dateTime = format.parse(dateText).getTime();

        return new Date(dateTime);
    }
}
