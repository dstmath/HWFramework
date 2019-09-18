package huawei.android.provider;

import android.icu.text.Transliterator;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.provider.HwSettings;
import java.util.ArrayList;
import java.util.HashMap;

public class HanziToPinyin {
    private static final String TAG = "HanziToPinyin";
    private static HanziToPinyin sInstance;
    private Transliterator mAsciiTransliterator;
    final HashMap<String, String> mMultiPinyin = new HashMap<String, String>() {
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

    public static class Token {
        public static final int LATIN = 1;
        public static final int PINYIN = 2;
        public static final String SEPARATOR = " ";
        public static final int UNKNOWN = 3;
        public String source;
        public String target;
        public int type;

        public Token() {
        }

        public Token(int type2, String source2, String target2) {
            this.type = type2;
            this.source = source2;
            this.target = target2;
        }
    }

    private HanziToPinyin() {
        try {
            this.mPinyinTransliterator = Transliterator.getInstance("Han-Latin/Names; Latin-Ascii; Any-Upper");
            this.mAsciiTransliterator = Transliterator.getInstance("Latin-Ascii");
        } catch (RuntimeException e) {
            Log.w(TAG, "Han-Latin/Names transliterator data is missing, HanziToPinyin is disabled");
        }
    }

    public boolean hasChineseTransliterator() {
        return this.mPinyinTransliterator != null;
    }

    public static HanziToPinyin getInstance() {
        HanziToPinyin hanziToPinyin;
        synchronized (HanziToPinyin.class) {
            if (sInstance == null) {
                sInstance = new HanziToPinyin();
            }
            hanziToPinyin = sInstance;
        }
        return hanziToPinyin;
    }

    private void tokenize(char character, Token token) {
        String str;
        token.source = Character.toString(character);
        if (character < 128) {
            token.type = 1;
            token.target = token.source;
        } else if (character < 592 || (7680 <= character && character < 7935)) {
            token.type = 1;
            if (this.mAsciiTransliterator == null) {
                str = token.source;
            } else {
                str = this.mAsciiTransliterator.transliterate(token.source);
            }
            token.target = str;
        } else {
            token.type = 2;
            token.target = this.mPinyinTransliterator.transliterate(token.source);
            if (TextUtils.isEmpty(token.target) || TextUtils.equals(token.source, token.target)) {
                token.type = 3;
                token.target = token.source;
            }
        }
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
                if (token.type == 2) {
                    checkMultiPinyin(token);
                    if (sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokens.add(token);
                    token = new Token();
                } else {
                    if (tokenType != token.type && sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
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

    private void checkMultiPinyin(Token token) {
        if (token != null && 2 == token.type) {
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
