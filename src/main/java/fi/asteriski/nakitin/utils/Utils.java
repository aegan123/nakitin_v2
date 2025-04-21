/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.utils;

import static fi.asteriski.nakitin.utils.Constants.*;

import com.mgnt.utils.TextUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static <T> List<List<T>> partition(final List<T> list, final Predicate<T> predicate) {
        if (list == null) {
            return Collections.emptyList();
        }
        var result = new ArrayList<List<T>>();
        var matches = new ArrayList<T>();
        var doesNotMatch = new ArrayList<T>();
        for (var value : list) {
            if (predicate.test(value)) {
                matches.add(value);
            } else {
                doesNotMatch.add(value);
            }
        }
        result.add(matches);
        result.add(doesNotMatch);

        return result;
    }

    /**
     * Converts special characters to their HTML equivalents. Currently handles: - newlines to HTML break tags - tabs to
     * non-breaking spaces (standard HTML tab equivalent) Input string is trimmed before conversion.
     *
     * @param text the text to convert, may be null
     * @return the converted text with HTML entities, or null if input is null
     */
    public static String convertWhitespaceToHtml(final String text) {
        return Optional.ofNullable(text)
                .map(String::trim)
                .map(t -> t.replace(NEWLINE, HTML_BREAK))
                .map(t -> t.replace(TAB, HTML_TAB))
                .orElse(null);
    }

    /**
     * Extracts the path and query components from a URL string. For example, from
     * 'http://localhost:8080/admin/errors?page=1' it will return 'admin/errors?page=1'
     *
     * @param url the URL string to parse
     * @return the path + query components of the URL, or null if the URL is invalid
     */
    public static String extractUrlPath(final String url) {
        if (url == null) {
            return null;
        }

        try {
            var uri = new URI(url);
            var path = uri.getPath();
            var query = uri.getQuery();
            return query != null ? path.substring(1) + "?" + query : path.substring(1);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Trims stack trace to only show relevant parts.
     *
     * @param t Throwable to trim.
     * @return Trimmed stack trace.
     */
    public static String trimStackTrace(final Throwable t) {
        return TextUtils.getStacktrace(t, true, "fi.asteriski");
    }
}
