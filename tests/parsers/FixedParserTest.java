package de.focus_shift.tests.parsers;

import de.focus_shift.Holiday;
import de.focus_shift.jaxb.mapping.Fixed;
import de.focus_shift.jaxb.mapping.Holidays;
import de.focus_shift.jaxb.mapping.Month;
import de.focus_shift.jaxb.mapping.MovingCondition;
import de.focus_shift.jaxb.mapping.Weekday;
import de.focus_shift.jaxb.mapping.With;
import de.focus_shift.parser.impl.FixedParser;
import de.focus_shift.util.CalendarUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sven
 */
class FixedParserTest {

  private FixedParser fixedParser = new FixedParser();
  private CalendarUtil calendarUtil = new CalendarUtil();

  @Test
  void testFixedWithValidity() {
    Holidays h = createHolidays(createFixed(1, Month.JANUARY), createFixed(3, Month.MARCH),
      createFixed(5, Month.MAY, 2011, null));
    Set<Holiday> set = new HashSet<>();
    fixedParser.parse(2010, set, h);
    containsAll(new ArrayList<>(set), calendarUtil.create(2010, 1, 1), calendarUtil.create(2010, 3, 3));
  }

  @Test
  void testFixedWithMoving() {
    Holidays h = createHolidays(
      createFixed(8, Month.JANUARY, createMoving(Weekday.SATURDAY, With.PREVIOUS, Weekday.FRIDAY)),
      createFixed(23, Month.JANUARY, createMoving(Weekday.SUNDAY, With.NEXT, Weekday.MONDAY)));
    Set<Holiday> set = new HashSet<>();
    fixedParser.parse(2011, set, h);
    containsAll(new ArrayList<>(set), calendarUtil.create(2011, 1, 7), calendarUtil.create(2011, 1, 24));
  }

  @Test
  void testCyle2YearsInvalid() {
    Fixed fixed = createFixed(4, Month.JANUARY);
    fixed.setValidFrom(2010);
    fixed.setEvery("2_YEARS");
    Holidays holidays = createHolidays(fixed);
    Set<Holiday> set = new HashSet<>();
    fixedParser.parse(2011, set, holidays);
    assertTrue(set.isEmpty(), "Expected to be empty.");
  }

  @Test
  void testCyle3Years() {
    Fixed fixed = createFixed(4, Month.JANUARY);
    fixed.setValidFrom(2010);
    fixed.setEvery("3_YEARS");
    Holidays holidays = createHolidays(fixed);
    Set<Holiday> set = new HashSet<>();
    fixedParser.parse(2013, set, holidays);
    assertEquals(1, set.size(), "Wrong number of holidays.");
  }

  private void containsAll(List<Holiday> list, LocalDate... dates) {
    assertEquals(dates.length, list.size(), "Number of holidays.");
    List<LocalDate> expected = new ArrayList<>(Arrays.asList(dates));
    Collections.sort(expected);
    Collections.sort(list, new HolidayComparator());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), list.get(i).getDate(), "Missing date.");
    }
  }

  Holidays createHolidays(Fixed... fs) {
    Holidays h = new Holidays();
    h.getFixed().addAll(Arrays.asList(fs));
    return h;
  }

  /**
   * @return
   */
  Fixed createFixed(int day, Month m, MovingCondition... mc) {
    Fixed f = new Fixed();
    f.setDay(day);
    f.setMonth(m);
    f.getMovingCondition().addAll(Arrays.asList(mc));
    return f;
  }

  Fixed createFixed(int day, Month m, Integer validFrom, Integer validUntil, MovingCondition... mc) {
    Fixed f = createFixed(day, m, mc);
    f.setValidFrom(validFrom);
    f.setValidTo(validUntil);
    return f;
  }

  MovingCondition createMoving(Weekday substitute, With with, Weekday weekday) {
    MovingCondition mc = new MovingCondition();
    mc.setSubstitute(substitute);
    mc.setWith(with);
    mc.setWeekday(weekday);
    return mc;
  }

}