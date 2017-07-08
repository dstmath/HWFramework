package huawei.android.provider;

import android.icu.text.Transliterator;
import android.text.TextUtils;
import android.util.Log;
import huawei.android.provider.HwSettings.System;
import java.util.ArrayList;
import java.util.HashMap;

public class HanziToPinyin {
    private static final String TAG = "HanziToPinyin";
    private static HanziToPinyin sInstance;
    private Transliterator mAsciiTransliterator;
    final HashMap<String, String> mMultiPinyin;
    private Transliterator mPinyinTransliterator;

    public static class Token {
        public static final int LATIN = 1;
        public static final int PINYIN = 2;
        public static final String SEPARATOR = " ";
        public static final int UNKNOWN = 3;
        public String source;
        public String target;
        public int type;

        public Token(int type, String source, String target) {
            this.type = type;
            this.source = source;
            this.target = target;
        }
    }

    private HanziToPinyin() {
        this.mMultiPinyin = new HashMap<String, String>() {
            {
                put("\u6c88", "SHEN");
                put("\u66fe", "ZENG");
                put("\u8d3e", "JIA");
                put("\u4fde", "YU");
                put("\u513f", "ER");
                put("\u5475", "HE");
                put("\u957f", "CHANG");
                put("\u7565", "LUE");
                put("\u63a0", "LUE");
                put("\u4e7e", "QIAN");
                put("\u79d8", "bi");
                put("\u8584", "bo");
                put("\u79cd", "chong");
                put("\u891a", "chu");
                put("\u555c", "chuai");
                put("\u53e5", "gou");
                put("\u839e", "guan");
                put("\u7094", "gui");
                put("\u85c9", "ji");
                put("\u5708", "juan");
                put("\u89d2", "jue");
                put("\u961a", "kan");
                put("\u9646", "lu");
                put("\u7f2a", "miao");
                put("\u4f74", "nai");
                put("\u5152", "ni");
                put("\u4e5c", "nie");
                put("\u533a", "ou");
                put("\u6734", "piao");
                put("\u7e41", "po");
                put("\u4ec7", "qiu");
                put("\u5355", "shan");
                put("\u76db", "sheng");
                put("\u6298", "she");
                put("\u5bbf", "su");
                put("\u6d17", "xian");
                put("\u89e3", "xie");
                put("\u5458", "yun");
                put("\u7b2e", "ze");
                put("\u76f4", "zha");
                put("\u7fdf", "zhai");
                put("\u796d", "zhai");
                put("\u963f", System.FINGERSENSE_KNUCKLE_GESTURE_E_SUFFIX);
                put("\u5b93", "fu");
                put("\u90a3", "nuo");
                put("\u5c09", "yu");
                put("\u86fe", "yi");
                put("\u67e5", "zha");
            }
        };
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
        token.source = Character.toString(character);
        if (character < '\u0080') {
            token.type = 1;
            token.target = token.source;
        } else if (character < '\u0250' || ('\u1e00' <= character && character < '\u1eff')) {
            String str;
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
        ArrayList<Token> tokens = new ArrayList();
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
            String pinyin = (String) this.mMultiPinyin.get(src);
            if (!(pinyin == null || pinyin.equals(tgt))) {
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
