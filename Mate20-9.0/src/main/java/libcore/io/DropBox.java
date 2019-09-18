package libcore.io;

import android.icu.text.PluralRules;
import java.io.PrintStream;
import java.util.Base64;

public final class DropBox {
    private static volatile Reporter REPORTER = new DefaultReporter();

    private static final class DefaultReporter implements Reporter {
        private DefaultReporter() {
        }

        public void addData(String tag, byte[] data, int flags) {
            PrintStream printStream = System.out;
            printStream.println(tag + PluralRules.KEYWORD_RULE_SEPARATOR + Base64.getEncoder().encodeToString(data));
        }

        public void addText(String tag, String data) {
            PrintStream printStream = System.out;
            printStream.println(tag + PluralRules.KEYWORD_RULE_SEPARATOR + data);
        }
    }

    public interface Reporter {
        void addData(String str, byte[] bArr, int i);

        void addText(String str, String str2);
    }

    public static void setReporter(Reporter reporter) {
        if (reporter != null) {
            REPORTER = reporter;
            return;
        }
        throw new NullPointerException("reporter == null");
    }

    public static Reporter getReporter() {
        return REPORTER;
    }

    public static void addData(String tag, byte[] data, int flags) {
        getReporter().addData(tag, data, flags);
    }

    public static void addText(String tag, String data) {
        getReporter().addText(tag, data);
    }
}
