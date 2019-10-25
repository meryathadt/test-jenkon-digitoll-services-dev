package com.digitoll.commons.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class DateTimeUtil {


    public static LocalDateTime calculateVignetteValidTill(LocalDateTime validFrom, String validityType) {
        LocalDateTime validTill = null;

        switch (validityType) {
            case "day":
                validTill = validFrom.plusDays(1);
                break;

            case "week":
                validTill = validFrom.plusWeeks(1);
                break;

            case "month":
                validTill = validFrom.plusMonths(1);
                break;

            case "quarter":
                validTill = validFrom.plusMonths(3);
                break;

            case "year":
                validTill = validFrom.plusYears(1);
                break;

            case "weekend":
                validTill = validFrom.plusDays(2);
                break;
        }

        return validTill;
    }

    // Date defaults to null, and
    public static ArrayList<Date> getHalfMonthDateRangeEET(Date date) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateTimeZone timeZone = DateTimeZone.forID("Europe/Sofia");
        DateTime now = DateTime.now(timeZone);
        // Only In case we want to generate the report again we can add atg
        if (date != null) {
            now = addDateTimePropertiesFromDate(now, date);
        }

        DateTime beginingDate = null;
        DateTime endDate = null;

        if (now.getDayOfMonth() >= 16) {
            beginingDate = now.withDayOfMonth(1).withTimeAtStartOfDay();
            endDate = beginingDate.plusDays(15).withTimeAtStartOfDay();
        } else {
            beginingDate = now.minusMonths(1).withDayOfMonth(16).withTimeAtStartOfDay();
            endDate = beginingDate.dayOfMonth().withMaximumValue().plusDays(1).withTimeAtStartOfDay();
        }

        java.util.Date fromDate = beginingDate.toDate();
        java.util.Date toDate = endDate.toDate();
        ArrayList<Date> result = new ArrayList<>();
        result.add(fromDate);
        result.add(toDate);
        return result;
    }

    public static Date getDateInEET(Date date) {
        LocalDateTime localDateFromEET = getLocalDateEET(date);
        return Date.from(localDateFromEET.toInstant(ZoneId.of("Europe/Sofia").getRules().getOffset(localDateFromEET)));
    }

    private static LocalDateTime getLocalDateEET(Date date) {
        return date.toInstant().atZone(ZoneId.of("Europe/Sofia")).toLocalDate().atTime(date.getHours(), date.getMinutes());
    }

    public static ArrayList<Date> getOneDayDateRangeEET(Date date) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateTimeZone timeZone = DateTimeZone.forID("Europe/Sofia");
        DateTime now = DateTime.now(timeZone);
        if (date != null) {
            now = addDateTimePropertiesFromDate(now, date);
        }
        DateTime yesterdayStart = now.minusDays(1).withTimeAtStartOfDay();
        DateTime yesterdayEnd = now.withTimeAtStartOfDay();
        java.util.Date fromDate = yesterdayStart.toDate();
        java.util.Date toDate = yesterdayEnd.toDate();
        ArrayList<Date> result = new ArrayList<>();
        result.add(fromDate);
        result.add(toDate);
        return result;
    }

    public static DateTime getStartOfDay(Date date) {
        DateTimeZone timeZone = DateTimeZone.forID("Europe/Sofia");

        DateTime now = DateTime.now(timeZone);
        // Only In case we want to generate the report again we can add atg
        if (date != null) {
            now = addDateTimePropertiesFromDate(now, date);
        }
        return now.withTimeAtStartOfDay();
    }

    public static DateTime getEndOfDay(Date date) {
        DateTimeZone timeZone = DateTimeZone.forID("Europe/Sofia");

        DateTime now = DateTime.now(timeZone);
        // Only In case we want to generate the report again we can add atg
        if (date != null) {
            now = addDateTimePropertiesFromDate(now, date);
        }
        return now.withTimeAtStartOfDay().plusDays(1).minusMinutes(1);
    }

    // DateTime is final, so we can't work with the ref
    private static DateTime addDateTimePropertiesFromDate(DateTime now, Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.of("Europe/Sofia")).toLocalDate();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();
        now = now.withMonthOfYear(month);
        now = now.withDayOfMonth(day);
        now = now.withYear(localDate.getYear());
        return now;
    }

}
