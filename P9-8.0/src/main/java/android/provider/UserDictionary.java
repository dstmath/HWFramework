package android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import java.util.Locale;

public class UserDictionary {
    public static final String AUTHORITY = "user_dictionary";
    public static final Uri CONTENT_URI = Uri.parse("content://user_dictionary");
    private static final int FREQUENCY_MAX = 255;
    private static final int FREQUENCY_MIN = 0;

    public static class Words implements BaseColumns {
        public static final String APP_ID = "appid";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.userword";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.userword";
        public static final Uri CONTENT_URI = Uri.parse("content://user_dictionary/words");
        public static final String DEFAULT_SORT_ORDER = "frequency DESC";
        public static final String FREQUENCY = "frequency";
        public static final String LOCALE = "locale";
        @Deprecated
        public static final int LOCALE_TYPE_ALL = 0;
        @Deprecated
        public static final int LOCALE_TYPE_CURRENT = 1;
        public static final String SHORTCUT = "shortcut";
        public static final String WORD = "word";
        public static final String _ID = "_id";

        @Deprecated
        public static void addWord(Context context, String word, int frequency, int localeType) {
            if (localeType == 0 || localeType == 1) {
                Locale locale;
                if (localeType == 1) {
                    locale = Locale.getDefault();
                } else {
                    locale = null;
                }
                addWord(context, word, frequency, null, locale);
            }
        }

        public static void addWord(Context context, String word, int frequency, String shortcut, Locale locale) {
            String str = null;
            ContentResolver resolver = context.getContentResolver();
            if (!TextUtils.isEmpty(word)) {
                if (frequency < 0) {
                    frequency = 0;
                }
                if (frequency > 255) {
                    frequency = 255;
                }
                ContentValues values = new ContentValues(5);
                values.put(WORD, word);
                values.put(FREQUENCY, Integer.valueOf(frequency));
                String str2 = LOCALE;
                if (locale != null) {
                    str = locale.toString();
                }
                values.put(str2, str);
                values.put(APP_ID, Integer.valueOf(0));
                values.put("shortcut", shortcut);
                Uri result = resolver.insert(CONTENT_URI, values);
            }
        }
    }
}
