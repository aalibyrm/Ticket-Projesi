package com.ticketapp.ticket.util;

public final class SlaCalculator {

    private SlaCalculator() {}

    public static String getSlaDuration(String priority) {
        if ("HIGH".equals(priority)) {
            return "PT4H";
        } else if ("MEDIUM".equals(priority)) {
            return "PT8H";
        } else {
            return "PT24H";
        }
    }

    public static String getSlaWarningDuration(String priority) {
        if ("HIGH".equals(priority)) {
            return "PT2H";
        } else if ("MEDIUM".equals(priority)) {
            return "PT4H";
        } else {
            return "PT12H";
        }
    }
}
