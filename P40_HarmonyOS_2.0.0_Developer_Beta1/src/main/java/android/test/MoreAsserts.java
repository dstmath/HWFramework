package android.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.Assert;

@Deprecated
public final class MoreAsserts {
    private MoreAsserts() {
    }

    public static void assertAssignableFrom(Class<?> expected, Object actual) {
        assertAssignableFrom(expected, actual.getClass());
    }

    public static void assertAssignableFrom(Class<?> expected, Class<?> actual) {
        Assert.assertTrue("Expected " + expected.getCanonicalName() + " to be assignable from actual class " + actual.getCanonicalName(), expected.isAssignableFrom(actual));
    }

    public static void assertNotEqual(String message, Object unexpected, Object actual) {
        if (equal(unexpected, actual)) {
            failEqual(message, unexpected);
        }
    }

    public static void assertNotEqual(Object unexpected, Object actual) {
        assertNotEqual(null, unexpected, actual);
    }

    public static void assertEquals(String message, byte[] expected, byte[] actual) {
        if (expected.length != actual.length) {
            failWrongLength(message, expected.length, actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                failWrongElement(message, i, Byte.valueOf(expected[i]), Byte.valueOf(actual[i]));
            }
        }
    }

    public static void assertEquals(byte[] expected, byte[] actual) {
        assertEquals((String) null, expected, actual);
    }

    public static void assertEquals(String message, int[] expected, int[] actual) {
        if (expected.length != actual.length) {
            failWrongLength(message, expected.length, actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                failWrongElement(message, i, Integer.valueOf(expected[i]), Integer.valueOf(actual[i]));
            }
        }
    }

    public static void assertEquals(int[] expected, int[] actual) {
        assertEquals((String) null, expected, actual);
    }

    public static void assertEquals(String message, long[] expected, long[] actual) {
        if (expected.length != actual.length) {
            failWrongLength(message, expected.length, actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                failWrongElement(message, i, Long.valueOf(expected[i]), Long.valueOf(actual[i]));
            }
        }
    }

    public static void assertEquals(long[] expected, long[] actual) {
        assertEquals((String) null, expected, actual);
    }

    public static void assertEquals(String message, double[] expected, double[] actual) {
        if (expected.length != actual.length) {
            failWrongLength(message, expected.length, actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                failWrongElement(message, i, Double.valueOf(expected[i]), Double.valueOf(actual[i]));
            }
        }
    }

    public static void assertEquals(double[] expected, double[] actual) {
        assertEquals((String) null, expected, actual);
    }

    public static void assertEquals(String message, Object[] expected, Object[] actual) {
        if (expected.length != actual.length) {
            failWrongLength(message, expected.length, actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            Object exp = expected[i];
            Object act = actual[i];
            if (exp == null) {
                if (act == null) {
                }
            } else if (exp.equals(act)) {
            }
            failWrongElement(message, i, exp, act);
        }
    }

    public static void assertEquals(Object[] expected, Object[] actual) {
        assertEquals((String) null, expected, actual);
    }

    public static void assertEquals(String message, Set<? extends Object> expected, Set<? extends Object> actual) {
        Set<Object> onlyInExpected = new HashSet<>(expected);
        onlyInExpected.removeAll(actual);
        Set<Object> onlyInActual = new HashSet<>(actual);
        onlyInActual.removeAll(expected);
        if (onlyInExpected.size() != 0 || onlyInActual.size() != 0) {
            Set<Object> intersection = new HashSet<>(expected);
            intersection.retainAll(actual);
            failWithMessage(message, "Sets do not match.\nOnly in expected: " + onlyInExpected + "\nOnly in actual: " + onlyInActual + "\nIntersection: " + intersection);
        }
    }

    public static void assertEquals(Set<? extends Object> expected, Set<? extends Object> actual) {
        assertEquals((String) null, expected, actual);
    }

    public static MatchResult assertMatchesRegex(String message, String expectedRegex, String actual) {
        if (actual == null) {
            failNotMatches(message, expectedRegex, actual);
        }
        Matcher matcher = getMatcher(expectedRegex, actual);
        if (!matcher.matches()) {
            failNotMatches(message, expectedRegex, actual);
        }
        return matcher;
    }

    public static MatchResult assertMatchesRegex(String expectedRegex, String actual) {
        return assertMatchesRegex(null, expectedRegex, actual);
    }

    public static MatchResult assertContainsRegex(String message, String expectedRegex, String actual) {
        if (actual == null) {
            failNotContains(message, expectedRegex, actual);
        }
        Matcher matcher = getMatcher(expectedRegex, actual);
        if (!matcher.find()) {
            failNotContains(message, expectedRegex, actual);
        }
        return matcher;
    }

    public static MatchResult assertContainsRegex(String expectedRegex, String actual) {
        return assertContainsRegex(null, expectedRegex, actual);
    }

    public static void assertNotMatchesRegex(String message, String expectedRegex, String actual) {
        if (getMatcher(expectedRegex, actual).matches()) {
            failMatch(message, expectedRegex, actual);
        }
    }

    public static void assertNotMatchesRegex(String expectedRegex, String actual) {
        assertNotMatchesRegex(null, expectedRegex, actual);
    }

    public static void assertNotContainsRegex(String message, String expectedRegex, String actual) {
        if (getMatcher(expectedRegex, actual).find()) {
            failContains(message, expectedRegex, actual);
        }
    }

    public static void assertNotContainsRegex(String expectedRegex, String actual) {
        assertNotContainsRegex(null, expectedRegex, actual);
    }

    public static void assertContentsInOrder(String message, Iterable<?> actual, Object... expected) {
        ArrayList actualList = new ArrayList();
        Iterator<?> it = actual.iterator();
        while (it.hasNext()) {
            actualList.add(it.next());
        }
        Assert.assertEquals(message, Arrays.asList(expected), actualList);
    }

    public static void assertContentsInOrder(Iterable<?> actual, Object... expected) {
        assertContentsInOrder(null, actual, expected);
    }

    public static void assertContentsInAnyOrder(String message, Iterable<?> actual, Object... expected) {
        HashMap<Object, Object> expectedMap = new HashMap<>(expected.length);
        for (Object expectedObj : expected) {
            expectedMap.put(expectedObj, expectedObj);
        }
        for (Object actualObj : actual) {
            if (expectedMap.remove(actualObj) == null) {
                failWithMessage(message, "Extra object in actual: (" + actualObj.toString() + ")");
            }
        }
        if (expectedMap.size() > 0) {
            failWithMessage(message, "Extra objects in expected.");
        }
    }

    public static void assertContentsInAnyOrder(Iterable<?> actual, Object... expected) {
        assertContentsInAnyOrder(null, actual, expected);
    }

    public static void assertEmpty(String message, Iterable<?> iterable) {
        if (iterable.iterator().hasNext()) {
            failNotEmpty(message, iterable.toString());
        }
    }

    public static void assertEmpty(Iterable<?> iterable) {
        assertEmpty((String) null, iterable);
    }

    public static void assertEmpty(String message, Map<?, ?> map) {
        if (!map.isEmpty()) {
            failNotEmpty(message, map.toString());
        }
    }

    public static void assertEmpty(Map<?, ?> map) {
        assertEmpty((String) null, map);
    }

    public static void assertNotEmpty(String message, Iterable<?> iterable) {
        if (!iterable.iterator().hasNext()) {
            failEmpty(message);
        }
    }

    public static void assertNotEmpty(Iterable<?> iterable) {
        assertNotEmpty((String) null, iterable);
    }

    public static void assertNotEmpty(String message, Map<?, ?> map) {
        if (map.isEmpty()) {
            failEmpty(message);
        }
    }

    public static void assertNotEmpty(Map<?, ?> map) {
        assertNotEmpty((String) null, map);
    }

    public static void checkEqualsAndHashCodeMethods(String message, Object lhs, Object rhs, boolean expectedResult) {
        if (lhs == null && rhs == null) {
            Assert.assertTrue("Your check is dubious...why would you expect null != null?", expectedResult);
            return;
        }
        if (lhs == null || rhs == null) {
            Assert.assertFalse("Your check is dubious...why would you expect an object to be equal to null?", expectedResult);
        }
        if (lhs != null) {
            Assert.assertEquals(message, expectedResult, lhs.equals(rhs));
        }
        if (rhs != null) {
            Assert.assertEquals(message, expectedResult, rhs.equals(lhs));
        }
        if (expectedResult) {
            String hashMessage = "hashCode() values for equal objects should be the same";
            if (message != null) {
                hashMessage = hashMessage + ": " + message;
            }
            Assert.assertTrue(hashMessage, lhs.hashCode() == rhs.hashCode());
        }
    }

    public static void checkEqualsAndHashCodeMethods(Object lhs, Object rhs, boolean expectedResult) {
        checkEqualsAndHashCodeMethods(null, lhs, rhs, expectedResult);
    }

    private static Matcher getMatcher(String expectedRegex, String actual) {
        return Pattern.compile(expectedRegex).matcher(actual);
    }

    private static void failEqual(String message, Object unexpected) {
        failWithMessage(message, "expected not to be:<" + unexpected + ">");
    }

    private static void failWrongLength(String message, int expected, int actual) {
        failWithMessage(message, "expected array length:<" + expected + "> but was:<" + actual + '>');
    }

    private static void failWrongElement(String message, int index, Object expected, Object actual) {
        failWithMessage(message, "expected array element[" + index + "]:<" + expected + "> but was:<" + actual + '>');
    }

    private static void failNotMatches(String message, String expectedRegex, String actual) {
        String actualDesc;
        if (actual == null) {
            actualDesc = "null";
        } else {
            actualDesc = '<' + actual + '>';
        }
        failWithMessage(message, "expected to match regex:<" + expectedRegex + "> but was:" + actualDesc);
    }

    private static void failNotContains(String message, String expectedRegex, String actual) {
        String actualDesc;
        if (actual == null) {
            actualDesc = "null";
        } else {
            actualDesc = '<' + actual + '>';
        }
        failWithMessage(message, "expected to contain regex:<" + expectedRegex + "> but was:" + actualDesc);
    }

    private static void failMatch(String message, String expectedRegex, String actual) {
        failWithMessage(message, "expected not to match regex:<" + expectedRegex + "> but was:<" + actual + '>');
    }

    private static void failContains(String message, String expectedRegex, String actual) {
        failWithMessage(message, "expected not to contain regex:<" + expectedRegex + "> but was:<" + actual + '>');
    }

    private static void failNotEmpty(String message, String actual) {
        failWithMessage(message, "expected to be empty, but contained: <" + actual + ">");
    }

    private static void failEmpty(String message) {
        failWithMessage(message, "expected not to be empty, but was");
    }

    private static void failWithMessage(String userMessage, String ourMessage) {
        String str;
        if (userMessage == null) {
            str = ourMessage;
        } else {
            str = userMessage + ' ' + ourMessage;
        }
        Assert.fail(str);
    }

    private static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
}
