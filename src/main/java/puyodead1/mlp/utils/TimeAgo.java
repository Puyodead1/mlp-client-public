package puyodead1.mlp.utils;

import java.time.Instant;

public class TimeAgo {
    public static final Instant PLAYER_EPOCH = Instant.parse("2025-05-01T00:00:00Z");

    public static String timeAgo(long timestampMillis) {
        if (timestampMillis == 0) {
            return "Never";
        }

        long nowMillis = Instant.now().toEpochMilli();

        if (timestampMillis > nowMillis) {
            return "In the future";
        }

        long diffMillis = nowMillis - timestampMillis;
        long seconds = diffMillis / 1000;

        if (seconds < 60) {
            return plural(seconds, "second") + " ago";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return plural(minutes, "minute") + " ago";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return plural(hours, "hour") + " ago";
        }

        long days = hours / 24;
        if (days < 30) {
            return plural(days, "day") + " ago";
        }

        long months = days / 30;
        if (months < 12) {
            return plural(months, "month") + " ago";
        }

        long years = months / 12;
        return plural(years, "year") + " ago";
    }

    private static String plural(long count, String unit) {
        if (count == 1) {
            return "1 " + unit;
        } else {
            return count + " " + unit + "s";
        }
    }

    public static String playerTimeAgo(long timestampMillis) {
        Instant actualInstant = PLAYER_EPOCH.plusSeconds(timestampMillis);
        return TimeAgo.timeAgo(actualInstant.toEpochMilli());
    }
}
