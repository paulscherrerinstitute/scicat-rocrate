package ch.psi.ord.core;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoiUtils {
    private static final Logger logger = LoggerFactory.getLogger(DoiUtils.class);

    private static final Pattern DOI_PATTERN = Pattern
            .compile("(10[.][0-9]{2,}(?:[.][0-9]+)*/(?:(?![%" + "\"#? ])\\S)+)");

    public static Optional<String> extractDoi(String input) {
        return Optional.ofNullable(input)
                .map(DOI_PATTERN::matcher)
                .filter(Matcher::find)
                .map(Matcher::group);
    }

    public static boolean isDoi(String input) {
        return extractDoi(input).isPresent();
    }

    public static String buildStandardUrl(String doi) {
        Optional<String> extractedDoi = extractDoi(doi);
        if (extractedDoi.isEmpty()) {
            logger.warn("Constructing DOI standard URL with an invalid DOI: {}", doi);
        }
        return String.format("https://doi.org/%s", extractedDoi.orElse(doi));
    }
}
