package huawei.android.hwutil;

import android.icu.text.Transliterator;
import android.text.TextUtils;
import java.util.ArrayList;

public class HanziToBopomofo {
    private static HanziToBopomofo mInstance;
    private Transliterator mAsciiTransliterator;
    private Transliterator mZhuyinTransliterator;

    public static class Token {
        public static final int LATIN = 1;
        public static final String SEPARATOR = " ";
        public static final int UNKNOWN = 3;
        public static final int ZHUYIN = 2;
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

    private HanziToBopomofo() {
        try {
            this.mZhuyinTransliterator = Transliterator.getInstance("Han-Latin/Names; Latin-Ascii; Latin-Bopomofo");
            this.mAsciiTransliterator = Transliterator.getInstance("Latin-Ascii");
        } catch (RuntimeException e) {
        }
    }

    public boolean hasChineseTransliterator() {
        return this.mZhuyinTransliterator != null;
    }

    public static HanziToBopomofo getInstance() {
        HanziToBopomofo hanziToBopomofo;
        synchronized (HanziToBopomofo.class) {
            if (mInstance == null) {
                mInstance = new HanziToBopomofo();
            }
            hanziToBopomofo = mInstance;
        }
        return hanziToBopomofo;
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
            token.target = formatTransliteration(token.source);
            if (TextUtils.isEmpty(token.target) || TextUtils.equals(token.source, token.target)) {
                token.type = 3;
                token.target = token.source;
            }
        }
    }

    private String formatTransliteration(String input) {
        String result = this.mZhuyinTransliterator.transliterate(input);
        if (TextUtils.isEmpty(result) || (result.charAt(result.length() - 1) != ' ' && result.charAt(result.length() - 1) != 729)) {
            return result;
        }
        return result.substring(0, result.length() - 1);
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

    private void addToken(StringBuilder sb, ArrayList<Token> tokens, int tokenType) {
        String str = sb.toString();
        tokens.add(new Token(tokenType, str, str));
        sb.setLength(0);
    }
}
