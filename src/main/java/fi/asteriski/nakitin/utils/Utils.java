/*
Copyright Juhani Vähä-Mäkilä (juhani@fmail.co.uk) 2025.
Licenced under EUPL-1.2 or later.
 */
package fi.asteriski.nakitin.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
}
