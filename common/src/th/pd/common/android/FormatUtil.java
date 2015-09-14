package th.pd.common.android;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;

import th.pd.common.dataProcess.FormatCodecBase64;
import th.pd.common.dataProcess.FormatCodecUri;

public final class FormatUtil {

    public static final class Url extends FormatCodecUri {
    }

    public static final class Base64 extends FormatCodecBase64 {
    }

    public static String formatBytes(long size) {
        String[] units = {
                " B",
                " KB",
                " MB",
                " GB",
                " TB",
                " PB"
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
        if (timestamp < 0) {
            return null;
        }
        return new SimpleDateFormat("HH:mm", Locale.ROOT).format(new Date(
                timestamp));
    }

    public static String formatTimespan(long timespan) {
        long s = timespan;
        long m = s / 60;
        s -= m * 60;
        long h = m / 60;
        m -= h * 60;

        if (h > 0) {
            return String.format(Locale.ROOT, "%dh%dm", h, m);
        } else if (m > 0) {
            return String.format(Locale.ROOT, "%dm%ds", m, s);
        } else {
            return String.format(Locale.ROOT, "%ds", s);
        }
    }

    private static Spannable getSmallcapsSpan(CharSequence s) {
        SpannableString span = new SpannableString(s.toString()
                .toUpperCase(Locale.ROOT));
        span.setSpan(new RelativeSizeSpan(0.8f), 0, span.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    public static Spannable toSmallcaps(CharSequence s) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int start = -1;
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c >= 'a' && c <= 'z') {
                if (start < 0) {
                    start = i;
                }
            } else {
                if (start >= 0) {
                    Spannable span = getSmallcapsSpan(s.subSequence(start,
                            i));
                    start = -1;
                    builder.append(span);
                } else {
                    builder.append(c);
                }
            }
        }
        if (start >= 0) {
            int i = s.length();
            Spannable span = getSmallcapsSpan(s.subSequence(start, i));
            start = -1;
            builder.append(span);
        }
        return builder;
    }

    public static String toSmallcaps(String s) {
        char[] smallCaps = new char[] {
                '\uf761',
                '\uf762',
                '\uf763',
                '\uf764',
                '\uf765',
                '\uf766',
                '\uf767',
                '\uf768',
                '\uf769',
                '\uf76A',
                '\uf76B',
                '\uf76C',
                '\uf76D',
                '\uf76E',
                '\uf76F',
                '\uf770',
                '\uf771',
                '\uf772',
                '\uf773',
                '\uf774',
                '\uf775',
                '\uf776',
                '\uf777',
                '\uf778',
                '\uf779',
                '\uf77A'
        };

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] >= 'a' && chars[i] <= 'z') {
                chars[i] = smallCaps[chars[i] - 'a'];
            }
        }
        return String.valueOf(chars);
    }
}
