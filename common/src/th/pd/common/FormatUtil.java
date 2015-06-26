package th.pd.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtil {

    public static String formatBytes(long size) {
        String[] units = {
                " B", " KB", " MB", " GB", " TB", " PB"
        };

        float number = size;
        int suffix = 0;
        while (number > 900f && suffix < units.length) {
            number = number / 1024;
            ++suffix;
        }

        if (suffix == 0) {
            return (int) number + units[suffix];
        }

        if (number > 100f) {
            return String.format("%.1f", number) + units[suffix];
        }

        if (number > 0f) {
            return String.format("%.2f", number) + units[suffix];
        }

        return null;
    }

    public static String formatTime(long timestamp) {
        return new SimpleDateFormat("HH:mm").format(new Date(timestamp));
    }

    public static String formatTimespan(long timespan) {
        long s = timespan;
        long m = s / 60;
        s -= m * 60;
        long h = m / 60;
        m -= h * 60;

        if (h > 0) {
            return String.format("%dh%dm", h, m);
        } else if (m > 0) {
            return String.format("%dm%ds", m, s);
        } else {
            return String.format("%ds", s);
        }
    }
}
