package ch.psi.scicat;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoiUtils {
    private static final Pattern DOI_PATTERN = Pattern
            .compile("(10[.][0-9]{2,}(?:[.][0-9]+)*/(?:(?![%" + "\"#? ])\\S)+)");

    public static Optional<String> extractDoi(String input) {
        Matcher matcher = DOI_PATTERN.matcher(input);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }
}
