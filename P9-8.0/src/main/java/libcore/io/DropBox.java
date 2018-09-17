package libcore.io;

import android.icu.text.PluralRules;
import java.util.Base64;

public final class DropBox {
    private static volatile Reporter REPORTER = new DefaultReporter();

    public interface Reporter {
        void addData(String str, byte[] bArr, int i);

        void addText(String str, String str2);
    }

    private static final class DefaultReporter implements Reporter {
        /* synthetic */ DefaultReporter(DefaultReporter -this0) {
            this();
        }

        private DefaultReporter() {
        }

        public void addData(String tag, byte[] data, int flags) {
            System.out.println(tag + PluralRules.KEYWORD_RULE_SEPARATOR + Base64.getEncoder().encodeToString(data));
        }

        public void addText(String tag, String data) {
            System.out.println(tag + PluralRules.KEYWORD_RULE_SEPARATOR + data);
        }
    }

    public static void setReporter(Reporter reporter) {
        if (reporter == null) {
            throw new NullPointerException("reporter == null");
        }
        REPORTER = reporter;
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
