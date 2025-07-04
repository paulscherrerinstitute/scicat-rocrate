package ch.psi.scicat;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DoiUtilsTest {
    String validDoi = "10.000/abc";

    @Test
    @DisplayName("Null input")
    public void test00() {
        Assertions.assertEquals(Optional.empty(), DoiUtils.extractDoi(null));
    }

    @Test
    @DisplayName("Valid DOI")
    public void test01() {
        Assertions.assertEquals(validDoi, DoiUtils.extractDoi(validDoi).get());
    }

    @Test
    @DisplayName("doi.org URL")
    public void test02() {
        Assertions.assertEquals(validDoi, DoiUtils.extractDoi("https://doi.org/" + validDoi).get());
    }

    @Test
    @DisplayName("Official display format")
    public void test03() {
        Assertions.assertEquals(validDoi, DoiUtils.extractDoi("doi: " + validDoi).get());
    }
}
