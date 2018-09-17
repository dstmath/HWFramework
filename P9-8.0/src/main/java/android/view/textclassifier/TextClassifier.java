package android.view.textclassifier;

import android.os.LocaleList;
import android.view.textclassifier.TextSelection.Builder;

public interface TextClassifier {
    public static final TextClassifier NO_OP = new TextClassifier() {
        public TextSelection suggestSelection(CharSequence text, int selectionStartIndex, int selectionEndIndex, LocaleList defaultLocales) {
            return new Builder(selectionStartIndex, selectionEndIndex).build();
        }

        public TextClassification classifyText(CharSequence text, int startIndex, int endIndex, LocaleList defaultLocales) {
            return TextClassification.EMPTY;
        }
    };
    public static final String TYPE_ADDRESS = "address";
    public static final String TYPE_EMAIL = "email";
    public static final String TYPE_OTHER = "other";
    public static final String TYPE_PHONE = "phone";
    public static final String TYPE_URL = "url";

    TextClassification classifyText(CharSequence charSequence, int i, int i2, LocaleList localeList);

    TextSelection suggestSelection(CharSequence charSequence, int i, int i2, LocaleList localeList);

    LinksInfo getLinks(CharSequence text, int linkMask, LocaleList defaultLocales) {
        return LinksInfo.NO_OP;
    }

    void logEvent(String source, String event) {
    }
}
