package ohos.com.sun.org.apache.regexp.internal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import ohos.com.sun.org.apache.xml.internal.serializer.CharInfo;
import ohos.global.icu.impl.locale.LanguageTag;

public class RETest {
    static final String NEW_LINE = System.getProperty("line.separator");
    static final boolean showSuccesses = false;
    REDebugCompiler compiler = new REDebugCompiler();
    int failures = 0;
    int testCount = 0;

    public static void main(String[] strArr) {
        try {
            if (!test(strArr)) {
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static boolean test(String[] strArr) throws Exception {
        RETest rETest = new RETest();
        if (strArr.length == 2) {
            rETest.runInteractiveTests(strArr[1]);
        } else if (strArr.length == 1) {
            rETest.runAutomatedTests(strArr[0]);
        } else {
            System.out.println("Usage: RETest ([-i] [regex]) ([/path/to/testfile.txt])");
            System.out.println("By Default will run automated tests from file 'docs/RETest.txt' ...");
            System.out.println();
            rETest.runAutomatedTests("docs/RETest.txt");
        }
        return rETest.failures == 0;
    }

    /* access modifiers changed from: package-private */
    public void runInteractiveTests(String str) {
        RE re = new RE();
        try {
            re.setProgram(this.compiler.compile(str));
            say("" + NEW_LINE + "" + str + "" + NEW_LINE + "");
            PrintWriter printWriter = new PrintWriter(System.out);
            this.compiler.dumpProgram(printWriter);
            printWriter.flush();
            boolean z = true;
            while (z) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("> ");
                System.out.flush();
                String readLine = bufferedReader.readLine();
                if (readLine != null) {
                    if (re.match(readLine)) {
                        say("Match successful.");
                    } else {
                        say("Match failed.");
                    }
                    showParens(re);
                } else {
                    z = false;
                    System.out.println();
                }
            }
        } catch (Exception e) {
            say("Error: " + e.toString());
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: package-private */
    public void die(String str) {
        say("FATAL ERROR: " + str);
        System.exit(-1);
    }

    /* access modifiers changed from: package-private */
    public void fail(StringBuffer stringBuffer, String str) {
        System.out.print(stringBuffer.toString());
        fail(str);
    }

    /* access modifiers changed from: package-private */
    public void fail(String str) {
        this.failures++;
        say("" + NEW_LINE + "");
        say("*******************************************************");
        say("*********************  FAILURE!  **********************");
        say("*******************************************************");
        say("" + NEW_LINE + "");
        say(str);
        say("");
        if (this.compiler != null) {
            PrintWriter printWriter = new PrintWriter(System.out);
            this.compiler.dumpProgram(printWriter);
            printWriter.flush();
            say("" + NEW_LINE + "");
        }
    }

    /* access modifiers changed from: package-private */
    public void say(String str) {
        System.out.println(str);
    }

    /* access modifiers changed from: package-private */
    public void showParens(RE re) {
        for (int i = 0; i < re.getParenCount(); i++) {
            say("$" + i + " = " + re.getParen(i));
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void runAutomatedTests(String str) throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        testPrecompiledRE();
        testSplitAndGrep();
        testSubst();
        testOther();
        File file = new File(str);
        if (file.exists()) {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            while (bufferedReader.ready()) {
                try {
                    RETestCase nextTestCase = getNextTestCase(bufferedReader);
                    if (nextTestCase != null) {
                        nextTestCase.runTest();
                    }
                } catch (Throwable th) {
                    bufferedReader.close();
                    throw th;
                }
            }
            bufferedReader.close();
            say(NEW_LINE + NEW_LINE + "Match time = " + (System.currentTimeMillis() - currentTimeMillis) + " ms.");
            if (this.failures > 0) {
                say("*************** THERE ARE FAILURES! *******************");
            }
            say("Tests complete.  " + this.testCount + " tests, " + this.failures + " failure(s).");
            return;
        }
        throw new Exception("Could not find: " + str);
    }

    /* access modifiers changed from: package-private */
    public void testOther() throws Exception {
        RE re = new RE("(a*)b");
        say("Serialized/deserialized (a*)b");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(128);
        new ObjectOutputStream(byteArrayOutputStream).writeObject(re);
        RE re2 = (RE) new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())).readObject();
        if (!re2.match("aaab")) {
            fail("Did not match 'aaab' with deserialized RE.");
        } else {
            say("aaaab = true");
            showParens(re2);
        }
        byteArrayOutputStream.reset();
        say("Deserialized (a*)b");
        new ObjectOutputStream(byteArrayOutputStream).writeObject(re2);
        RE re3 = (RE) new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())).readObject();
        if (re3.getParenCount() != 0) {
            fail("Has parens after deserialization.");
        }
        if (!re3.match("aaab")) {
            fail("Did not match 'aaab' with deserialized RE.");
        } else {
            say("aaaab = true");
            showParens(re3);
        }
        RE re4 = new RE("abc(\\w*)");
        say("MATCH_CASEINDEPENDENT abc(\\w*)");
        re4.setMatchFlags(1);
        say("abc(d*)");
        if (!re4.match("abcddd")) {
            fail("Did not match 'abcddd'.");
        } else {
            say("abcddd = true");
            showParens(re4);
        }
        if (!re4.match("aBcDDdd")) {
            fail("Did not match 'aBcDDdd'.");
        } else {
            say("aBcDDdd = true");
            showParens(re4);
        }
        if (!re4.match("ABCDDDDD")) {
            fail("Did not match 'ABCDDDDD'.");
        } else {
            say("ABCDDDDD = true");
            showParens(re4);
        }
        RE re5 = new RE("(A*)b\\1");
        re5.setMatchFlags(1);
        if (!re5.match("AaAaaaBAAAAAA")) {
            fail("Did not match 'AaAaaaBAAAAAA'.");
        } else {
            say("AaAaaaBAAAAAA = true");
            showParens(re5);
        }
        RE re6 = new RE("[A-Z]*");
        re6.setMatchFlags(1);
        if (!re6.match("CaBgDe12")) {
            fail("Did not match 'CaBgDe12'.");
        } else {
            say("CaBgDe12 = true");
            showParens(re6);
        }
        RE re7 = new RE("^abc$", 2);
        if (!re7.match("\nabc")) {
            fail("\"\\nabc\" doesn't match \"^abc$\"");
        }
        if (!re7.match("\rabc")) {
            fail("\"\\rabc\" doesn't match \"^abc$\"");
        }
        if (!re7.match("\r\nabc")) {
            fail("\"\\r\\nabc\" doesn't match \"^abc$\"");
        }
        if (!re7.match("abc")) {
            fail("\"\\u0085abc\" doesn't match \"^abc$\"");
        }
        if (!re7.match(" abc")) {
            fail("\"\\u2028abc\" doesn't match \"^abc$\"");
        }
        if (!re7.match(" abc")) {
            fail("\"\\u2029abc\" doesn't match \"^abc$\"");
        }
        RE re8 = new RE("^a.*b$", 2);
        if (re8.match("a\nb")) {
            fail("\"a\\nb\" matches \"^a.*b$\"");
        }
        if (re8.match("a\rb")) {
            fail("\"a\\rb\" matches \"^a.*b$\"");
        }
        if (re8.match("a\r\nb")) {
            fail("\"a\\r\\nb\" matches \"^a.*b$\"");
        }
        if (re8.match("ab")) {
            fail("\"a\\u0085b\" matches \"^a.*b$\"");
        }
        if (re8.match("a b")) {
            fail("\"a\\u2028b\" matches \"^a.*b$\"");
        }
        if (re8.match("a b")) {
            fail("\"a\\u2029b\" matches \"^a.*b$\"");
        }
    }

    private void testPrecompiledRE() {
        RE re = new RE(new REProgram(new char[]{'|', 0, 26, '|', 0, CharInfo.S_CARRIAGERETURN, 'A', 1, 4, 'a', '|', 0, 3, 'G', 0, 65526, '|', 0, 3, 'N', 0, 3, 'A', 1, 4, 'b', 'E', 0, 0}));
        say("a*b");
        boolean match = re.match("aaab");
        say("aaab = " + match);
        showParens(re);
        if (!match) {
            fail("\"aaab\" doesn't match to precompiled \"a*b\"");
        }
        boolean match2 = re.match("b");
        say("b = " + match2);
        showParens(re);
        if (!match2) {
            fail("\"b\" doesn't match to precompiled \"a*b\"");
        }
        boolean match3 = re.match("c");
        say("c = " + match3);
        showParens(re);
        if (match3) {
            fail("\"c\" matches to precompiled \"a*b\"");
        }
        boolean match4 = re.match("ccccaaaaab");
        say("ccccaaaaab = " + match4);
        showParens(re);
        if (!match4) {
            fail("\"ccccaaaaab\" doesn't match to precompiled \"a*b\"");
        }
    }

    private void testSplitAndGrep() {
        String[] strArr = {"xxxx", "xxxx", "yyyy", "zzz"};
        String[] split = new RE("a*b").split("xxxxaabxxxxbyyyyaaabzzz");
        int i = 0;
        while (i < strArr.length && i < split.length) {
            assertEquals("Wrong splitted part", strArr[i], split[i]);
            i++;
        }
        assertEquals("Wrong number of splitted parts", strArr.length, split.length);
        String[] strArr2 = {"xxxx", "xxxx"};
        String[] grep = new RE("x+").grep(split);
        for (int i2 = 0; i2 < grep.length; i2++) {
            say("s[" + i2 + "] = " + grep[i2]);
            assertEquals("Grep fails", strArr2[i2], grep[i2]);
        }
        assertEquals("Wrong number of string found by grep", strArr2.length, grep.length);
    }

    private void testSubst() {
        assertEquals("Wrong result of substitution in \"a*b\"", "-foo-garply-wacky-", new RE("a*b").subst("aaaabfooaaabgarplyaaabwackyb", LanguageTag.SEP));
        assertEquals("Wrong subst() result", "visit us: 1234<a href=\"http://www.apache.org\">http://www.apache.org</a>!", new RE("http://[\\.\\w\\-\\?/~_@&=%]+").subst("visit us: http://www.apache.org!", "1234<a href=\"$0\">$0</a>", 2));
        assertEquals("Wrong subst() result", "variable_test_value12", new RE("(.*?)=(.*)").subst("variable=value", "$1_test_$212", 2));
        assertEquals("Wrong subst() result", "b", new RE("^a$").subst("a", "b", 2));
        assertEquals("Wrong subst() result", "\r\nb\r\n", new RE("^a$", 2).subst("\r\na\r\n", "b", 2));
    }

    public void assertEquals(String str, String str2, String str3) {
        if ((str2 != null && !str2.equals(str3)) || (str3 != null && !str3.equals(str2))) {
            fail(str + " (expected \"" + str2 + "\", actual \"" + str3 + "\")");
        }
    }

    public void assertEquals(String str, int i, int i2) {
        if (i != i2) {
            fail(str + " (expected \"" + i + "\", actual \"" + i2 + "\")");
        }
    }

    private boolean getExpectedResult(String str) {
        if ("NO".equals(str)) {
            return false;
        }
        if ("YES".equals(str)) {
            return true;
        }
        die("Test script error!");
        return false;
    }

    private String findNextTest(BufferedReader bufferedReader) throws IOException {
        String str = "";
        while (bufferedReader.ready() && (str = bufferedReader.readLine()) != null) {
            str = str.trim();
            if (str.startsWith("#")) {
                break;
            } else if (!str.equals("")) {
                say("Script error.  Line = " + str);
                System.exit(-1);
            }
        }
        return str;
    }

    private RETestCase getNextTestCase(BufferedReader bufferedReader) throws IOException {
        String[] strArr;
        boolean z;
        String findNextTest = findNextTest(bufferedReader);
        if (!bufferedReader.ready()) {
            return null;
        }
        String readLine = bufferedReader.readLine();
        String readLine2 = bufferedReader.readLine();
        boolean equals = "ERR".equals(readLine2);
        if (!equals) {
            z = getExpectedResult(bufferedReader.readLine().trim());
            if (z) {
                int parseInt = Integer.parseInt(bufferedReader.readLine().trim());
                strArr = new String[parseInt];
                for (int i = 0; i < parseInt; i++) {
                    strArr[i] = bufferedReader.readLine();
                }
                return new RETestCase(this, findNextTest, readLine, readLine2, equals, z, strArr);
            }
        } else {
            z = false;
        }
        strArr = null;
        return new RETestCase(this, findNextTest, readLine, readLine2, equals, z, strArr);
    }
}
