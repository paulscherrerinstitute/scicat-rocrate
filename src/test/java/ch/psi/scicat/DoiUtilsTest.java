package ch.psi.scicat;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DoiUtilsTest {

    @Test
    @DisplayName("Null input")
    public void test00() {
        Assertions.assertEquals(Optional.empty(), DoiUtils.extractDoi(null));
    }

    @Test
    @DisplayName("Valid DOI")
    public void test01() {
        Assertions.assertEquals(TestData.validDoi, DoiUtils.extractDoi(TestData.validDoi).get());
    }

    @Test
    @DisplayName("doi.org URL")
    public void test02() {
        Assertions.assertEquals(TestData.validDoi,
                DoiUtils.extractDoi("https://doi.org/" + TestData.validDoi).get());
    }

    @Test
    @DisplayName("Official display format")
    public void test03() {
        Assertions.assertEquals(TestData.validDoi, DoiUtils.extractDoi("doi: " + TestData.validDoi).get());
    }
}
