package huawei.android.provider;

import android.icu.text.Transliterator;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.storage.StorageManagerExt;
import huawei.android.provider.HwSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllformedLocaleException;

public class HanziToPinyin {
    private static final String TAG = "HanziToPinyin";
    private static final Object lock = new Object();
    private static HanziToPinyin sInstance;
    private Transliterator mAsciiTransliterator;
    private final HashMap<String, String> mMultiPinyin = new HashMap<String, String>() {
        /* class huawei.android.provider.HanziToPinyin.AnonymousClass1 */

        {
            put("沈", "SHEN");
            put("曾", "ZENG");
            put("贾", "JIA");
            put("俞", "YU");
            put("儿", "ER");
            put("呵", "HE");
            put("长", "CHANG");
            put("略", "LUE");
            put("掠", "LUE");
            put("乾", "QIAN");
            put("秘", "bi");
            put("薄", "bo");
            put("种", "chong");
            put("褚", "chu");
            put("啜", "chuai");
            put("句", "gou");
            put("莞", "guan");
            put("炔", "gui");
            put("藉", "ji");
            put("圈", "juan");
            put("角", "jue");
            put("阚", "kan");
            put("陆", "lu");
            put("缪", "miao");
            put("佴", "nai");
            put("兒", "ni");
            put("乜", "nie");
            put("区", "ou");
            put("朴", "piao");
            put("繁", "po");
            put("仇", "qiu");
            put("单", "shan");
            put("盛", "sheng");
            put("折", "she");
            put("宿", "su");
            put("洗", "xian");
            put("解", "xie");
            put("员", "yun");
            put("笮", "ze");
            put("直", "zha");
            put("翟", "zhai");
            put("祭", "zhai");
            put("阿", HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_E_SUFFIX);
            put("宓", "fu");
            put("那", "nuo");
            put("尉", "yu");
            put("蛾", "yi");
            put("查", "zha");
        }
    };
    private Transliterator mPinyinTransliterator;

    private HanziToPinyin() {
        try {
            this.mPinyinTransliterator = Transliterator.getInstance("Han-Latin/Names; Latin-Ascii; Any-Upper");
            this.mAsciiTransliterator = Transliterator.getInstance("Latin-Ascii");
        } catch (IllformedLocaleException e) {
            Log.w(TAG, "Han-Latin/Names transliterator data is missing, HanziToPinyin is disabled");
        }
    }

    public static class Token {
        public static final int LATIN = 1;
        public static final int PINYIN = 2;
        public static final String SEPARATOR = " ";
        public static final int UNKNOWN = 3;
        public String source;
        public String target;
        public int type;

        public Token() {
            this(1, StorageManagerExt.INVALID_KEY_DESC, StorageManagerExt.INVALID_KEY_DESC);
        }

        public Token(int type2, String source2, String target2) {
            this.type = type2;
            this.source = source2;
            this.target = target2;
        }
    }

    public static HanziToPinyin getInstance() {
        HanziToPinyin hanziToPinyin;
        synchronized (lock) {
            if (sInstance == null) {
                sInstance = new HanziToPinyin();
            }
            hanziToPinyin = sInstance;
        }
        return hanziToPinyin;
    }

    public boolean hasChineseTransliterator() {
        return this.mPinyinTransliterator != null;
    }

    public ArrayList<Token> get(String input) {
        ArrayList<Token> tokens = new ArrayList<>();
        if (!hasChineseTransliterator() || TextUtils.isEmpty(input)) {
            return tokens;
        }
        int inputLength = input.length();
        StringBuilder sb = new StringBuilder();
        int tokenType = 1;
        Token token = new Token();
        for (int i = 0; i < inputLength; i++) {
            char character = input.charAt(i);
            if (!Character.isSpaceChar(character)) {
                tokenize(character, token);
                if (token.type == 2 && sb.length() > 0) {
                    checkMultiPinyin(token);
                    addToken(sb, tokens, tokenType);
                    tokens.add(token);
                    token = new Token();
                } else if (token.type == 2 && sb.length() <= 0) {
                    checkMultiPinyin(token);
                    tokens.add(token);
                    token = new Token();
                } else if (tokenType == token.type || sb.length() <= 0) {
                    sb.append(token.target);
                } else {
                    addToken(sb, tokens, tokenType);
                    sb.append(token.target);
                }
                tokenType = token.type;
            } else if (sb.length() > 0) {
                addToken(sb, tokens, tokenType);
            }
        }
        if (sb.length() > 0) {
            addToken(sb, tokens, tokenType);
        }
        return tokens;
    }

    private void tokenize(char character, Token token) {
        token.source = Character.toString(character);
        if (character < 128) {
            token.type = 1;
            token.target = token.source;
        } else if (character < 592 || (character >= 7680 && character < 7935)) {
            token.type = 1;
            Transliterator transliterator = this.mAsciiTransliterator;
            token.target = transliterator == null ? token.source : transliterator.transliterate(token.source);
        } else {
            token.type = 2;
            token.target = this.mPinyinTransliterator.transliterate(token.source);
            if (TextUtils.isEmpty(token.target) || TextUtils.equals(token.source, token.target)) {
                token.type = 3;
                token.target = token.source;
            }
        }
    }

    private void checkMultiPinyin(Token token) {
        if (token != null && token.type == 2) {
            String src = token.source;
            String tgt = token.target;
            String pinyin = this.mMultiPinyin.get(src);
            if (pinyin != null && !pinyin.equals(tgt)) {
                token.target = pinyin;
                Log.i(TAG, "set new pinyin for " + src + " from " + tgt + " to " + pinyin);
            }
        }
    }

    private void addToken(StringBuilder sb, ArrayList<Token> tokens, int tokenType) {
        String str = sb.toString();
        tokens.add(new Token(tokenType, str, str));
        sb.setLength(0);
    }
}
