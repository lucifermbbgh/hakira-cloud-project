package com.hakira.common.util;


import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class DateUtilsTest {
    @Test
    public void testCustomFormat() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        String result1 = dateUtils.customFormat(null);
        assertNull(result1);

        // Test with valid input
        String result2 = dateUtils.customFormat(date);
        assertEquals("yyyy-MM-dd", result2);
    }

    @Test
    public void testDetailFormat() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        String result1 = dateUtils.detailFormat(null);
        assertNull(result1);

        // Test with valid input
        String result2 = dateUtils.detailFormat(date);
        assertEquals("yyyy-MM-dd HH:mm:ss", result2);
    }

    @Test
    public void testLshFormat() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        String result1 = dateUtils.lshFormat(null);
        assertNull(result1);

        // Test with valid input
        String result2 = dateUtils.lshFormat(date);
        assertEquals("yyyyMMdd", result2);
    }

    @Test
    public void testDetailLSHFormat() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        String result1 = dateUtils.detailLSHFormat(null);
        assertNull(result1);

        // Test with valid input
        String result2 = dateUtils.detailLSHFormat(date);
        assertEquals("yyyyMMddHHmmss", result2);
    }

    @Test
    public void testShortFormat() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        String result1 = dateUtils.shortFormat(null);
        assertNull(result1);

        // Test with valid input
        String result2 = dateUtils.shortFormat(date);
        assertEquals("yyMMdd", result2);
    }

    @Test
    public void testShortYearFormat() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        String result1 = dateUtils.shortYearFormat(null);
        assertNull(result1);

        // Test with valid input
        String result2 = dateUtils.shortYearFormat(date);
        assertEquals("yyMMdd", result2);
    }

    @Test
    public void testNsFormat() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        String result1 = dateUtils.nsFormat(null);
        assertNull(result1);

        // Test with valid input
        String result2 = dateUtils.nsFormat(date);
        assertEquals("yyyy-MM-dd HH:mm:ss.S", result2);
    }

    @Test
    public void testAddYear() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.addYear(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.addYear(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.addYear(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testAddDay() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.addDay(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.addDay(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.addDay(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testAddMonth() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.addMonth(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.addMonth(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.addMonth(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testAddHour() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.addHour(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.addHour(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.addHour(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testAddMinute() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.addMinute(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.addMinute(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.addMinute(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testAddSecond() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.addSecond(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.addSecond(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.addSecond(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testSetDay() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.setDay(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.setDay(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.setDay(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testSetHour() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.setHour(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.setHour(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.setHour(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testSetMinute() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.setMinute(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.setMinute(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.setMinute(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testSetSecond() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.setSecond(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.setSecond(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.setSecond(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testSetMillisecond() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.setMillisecond(null, 1);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.setMillisecond(date, 1);
        assertNotNull(result2);

        // Test with negative input
        Date result3 = dateUtils.setMillisecond(date, -1);
        assertNotNull(result3);
    }

    @Test
    public void testRound() {
        DateUtils dateUtils = new DateUtils();
        Date date = new Date();

        // Test with null input
        Date result1 = dateUtils.round(null);
        assertNull(result1);

        // Test with valid input
        Date result2 = dateUtils.round(date);
        assertNotNull(result2);
    }

    @Test
    public void testParseDate() {
        DateUtils dateUtils = new DateUtils();

        // Test with valid input
        try {
            String validDate = "2022-01-01 10:10:10";
            Date result = dateUtils.parseDate(validDate);
            assertNotNull(result);
        } catch (ParseException e) {
            fail("Should not throw exception");
        }

        // Test with null input
        try {
            String nullDate = null;
            dateUtils.parseDate(nullDate);
            fail("Should throw exception");
        } catch (ParseException e) {
            // Expected exception
        }
    }

    @Test
    public void testNsDate() {
        DateUtils dateUtils = new DateUtils();

        // Test with valid input
        try {
            String validDate = "2022-01-01 10:10:10";
            Date result = dateUtils.nsDate(validDate);
            assertNotNull(result);
        } catch (ParseException e) {
            fail("Should not throw exception");
        }

        // Test with null input
        try {
            String nullDate = null;
            dateUtils.nsDate(nullDate);
            fail("Should throw exception");
        } catch (ParseException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetTimestamp() {
        String dateStr = "2024-05-13-17.51.10.545345";
        String pattern = "yyyy-MM-dd-HH.mm.ss.SSSSSS";
        System.out.println(DateUtils.getTimestamp(dateStr, pattern));
    }

    @Test
    public void testDivideTime2() {
        String dateStr = "2024-05-07-17.10.10.123456";
        String pattern = "yyyy-MM-dd-HH.mm.ss.SSSSSS";
//        String dateStr = "2024-05-07-17.10.10.123";
//        String pattern = "yyyy-MM-dd-HH.mm.ss.SSS";
//        String dateStr = "2022-01-01 10:10:10";
//        String pattern = "yyyy-MM-dd HH:mm:ss";
        System.out.println(DateUtils.divideTimeForMinutes(dateStr, pattern));
    }
}
