package ohos.com.sun.org.apache.regexp.internal;

import java.io.StringBufferInputStream;
import java.io.StringReader;

/* compiled from: RETest */
final class RETestCase {
    private final boolean badPattern;
    private final StringBuffer log = new StringBuffer();
    private final int number;
    private final String[] parens;
    private final String pattern;
    private RE regexp;
    private final boolean shouldMatch;
    private final String tag;
    private final RETest test;
    private final String toMatch;

    /* access modifiers changed from: package-private */
    public void success(String str) {
    }

    public RETestCase(RETest rETest, String str, String str2, String str3, boolean z, boolean z2, String[] strArr) {
        int i = rETest.testCount + 1;
        rETest.testCount = i;
        this.number = i;
        this.test = rETest;
        this.tag = str;
        this.pattern = str2;
        this.toMatch = str3;
        this.badPattern = z;
        this.shouldMatch = z2;
        if (strArr != null) {
            this.parens = new String[strArr.length];
            for (int i2 = 0; i2 < strArr.length; i2++) {
                this.parens[i2] = strArr[i2];
            }
            return;
        }
        this.parens = null;
    }

    public void runTest() {
        RETest rETest = this.test;
        rETest.say(this.tag + "(" + this.number + "): " + this.pattern);
        if (testCreation()) {
            testMatch();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean testCreation() {
        try {
            this.regexp = new RE();
            this.regexp.setProgram(this.test.compiler.compile(this.pattern));
            if (!this.badPattern) {
                return true;
            }
            this.test.fail(this.log, "Was expected to be an error, but wasn't.");
            return false;
        } catch (Exception e) {
            if (this.badPattern) {
                this.log.append("   Match: ERR\n");
                success("Produces an error (" + e.toString() + "), as expected.");
                return false;
            }
            String exc = e.getMessage() == null ? e.toString() : e.getMessage();
            RETest rETest = this.test;
            StringBuffer stringBuffer = this.log;
            rETest.fail(stringBuffer, "Produces an unexpected exception \"" + exc + "\"");
            e.printStackTrace();
            return false;
        } catch (Error e2) {
            RETest rETest2 = this.test;
            StringBuffer stringBuffer2 = this.log;
            rETest2.fail(stringBuffer2, "Compiler threw fatal error \"" + e2.getMessage() + "\"");
            e2.printStackTrace();
            return false;
        }
    }

    private void testMatch() {
        StringBuffer stringBuffer = this.log;
        stringBuffer.append("   Match against: '" + this.toMatch + "'\n");
        try {
            boolean match = this.regexp.match(this.toMatch);
            StringBuffer stringBuffer2 = this.log;
            StringBuilder sb = new StringBuilder();
            sb.append("   Matched: ");
            sb.append(match ? "YES" : "NO");
            sb.append("\n");
            stringBuffer2.append(sb.toString());
            if (!checkResult(match)) {
                return;
            }
            if (!this.shouldMatch || checkParens()) {
                this.log.append("   Match using StringCharacterIterator\n");
                if (tryMatchUsingCI(new StringCharacterIterator(this.toMatch))) {
                    this.log.append("   Match using CharacterArrayCharacterIterator\n");
                    if (tryMatchUsingCI(new CharacterArrayCharacterIterator(this.toMatch.toCharArray(), 0, this.toMatch.length()))) {
                        this.log.append("   Match using StreamCharacterIterator\n");
                        if (tryMatchUsingCI(new StreamCharacterIterator(new StringBufferInputStream(this.toMatch)))) {
                            this.log.append("   Match using ReaderCharacterIterator\n");
                            if (tryMatchUsingCI(new ReaderCharacterIterator(new StringReader(this.toMatch)))) {
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            RETest rETest = this.test;
            StringBuffer stringBuffer3 = this.log;
            rETest.fail(stringBuffer3, "Matcher threw exception: " + e.toString());
            e.printStackTrace();
        } catch (Error e2) {
            RETest rETest2 = this.test;
            StringBuffer stringBuffer4 = this.log;
            rETest2.fail(stringBuffer4, "Matcher threw fatal error \"" + e2.getMessage() + "\"");
            e2.printStackTrace();
        }
    }

    private boolean checkResult(boolean z) {
        boolean z2 = this.shouldMatch;
        if (z == z2) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.shouldMatch ? "Matched" : "Did not match");
            sb.append(" \"");
            sb.append(this.toMatch);
            sb.append("\", as expected:");
            success(sb.toString());
            return true;
        } else if (z2) {
            RETest rETest = this.test;
            StringBuffer stringBuffer = this.log;
            rETest.fail(stringBuffer, "Did not match \"" + this.toMatch + "\", when expected to.");
            return false;
        } else {
            RETest rETest2 = this.test;
            StringBuffer stringBuffer2 = this.log;
            rETest2.fail(stringBuffer2, "Matched \"" + this.toMatch + "\", when not expected to.");
            return false;
        }
    }

    private boolean checkParens() {
        this.log.append("   Paren count: " + this.regexp.getParenCount() + "\n");
        if (!assertEquals(this.log, "Wrong number of parens", this.parens.length, this.regexp.getParenCount())) {
            return false;
        }
        for (int i = 0; i < this.regexp.getParenCount(); i++) {
            this.log.append("   Paren " + i + ": " + this.regexp.getParen(i) + "\n");
            if (!"null".equals(this.parens[i]) || this.regexp.getParen(i) != null) {
                if (!assertEquals(this.log, "Wrong register " + i, this.parens[i], this.regexp.getParen(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean tryMatchUsingCI(CharacterIterator characterIterator) {
        try {
            boolean match = this.regexp.match(characterIterator, 0);
            StringBuffer stringBuffer = this.log;
            StringBuilder sb = new StringBuilder();
            sb.append("   Match: ");
            sb.append(match ? "YES" : "NO");
            sb.append("\n");
            stringBuffer.append(sb.toString());
            if (!checkResult(match)) {
                return false;
            }
            if (!this.shouldMatch || checkParens()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            RETest rETest = this.test;
            StringBuffer stringBuffer2 = this.log;
            rETest.fail(stringBuffer2, "Matcher threw exception: " + e.toString());
            e.printStackTrace();
            return false;
        } catch (Error e2) {
            RETest rETest2 = this.test;
            StringBuffer stringBuffer3 = this.log;
            rETest2.fail(stringBuffer3, "Matcher threw fatal error \"" + e2.getMessage() + "\"");
            e2.printStackTrace();
            return false;
        }
    }

    public boolean assertEquals(StringBuffer stringBuffer, String str, String str2, String str3) {
        if ((str2 == null || str2.equals(str3)) && (str3 == null || str3.equals(str2))) {
            return true;
        }
        RETest rETest = this.test;
        rETest.fail(stringBuffer, str + " (expected \"" + str2 + "\", actual \"" + str3 + "\")");
        return false;
    }

    public boolean assertEquals(StringBuffer stringBuffer, String str, int i, int i2) {
        if (i == i2) {
            return true;
        }
        RETest rETest = this.test;
        rETest.fail(stringBuffer, str + " (expected \"" + i + "\", actual \"" + i2 + "\")");
        return false;
    }
}
