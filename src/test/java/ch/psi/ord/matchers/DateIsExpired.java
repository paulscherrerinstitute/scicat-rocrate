package ch.psi.ord.matchers;

import java.time.Instant;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class DateIsExpired extends TypeSafeMatcher<String> {
  public static DateIsExpired isDateExpired() {
    return new DateIsExpired();
  }

  @Override
  public void describeTo(Description description) {}

  @Override
  protected boolean matchesSafely(String date) {
    return Instant.parse(date).isBefore(Instant.now());
  }
}
