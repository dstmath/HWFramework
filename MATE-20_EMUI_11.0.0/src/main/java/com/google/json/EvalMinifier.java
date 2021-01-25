package com.google.json;

import com.huawei.odmf.model.ARelationship;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EvalMinifier {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int BOILERPLATE_COST = 22;
    private static final String ENVELOPE_P1 = "(function(";
    private static final String ENVELOPE_P2 = "){return";
    private static final String ENVELOPE_P3 = "}(";
    private static final String ENVELOPE_P4 = "))";
    private static final int MARGINAL_VAR_COST = 2;
    private static final String[][] RESERVED_KEYWORDS = {new String[0], new String[0], new String[]{"do", "if", "in"}, new String[]{"for", "let", "new", "try", "var"}, new String[]{"case", "else", "enum", "eval", "null", "this", "true", "void", "with"}, new String[]{"catch", "class", "const", "false", "super", "throw", "while", "yield"}, new String[]{ARelationship.DELETE_CASCADE, "export", "import", "return", "switch", "static", "typeof"}, new String[]{"default", "extends", "public", "private"}, new String[]{"continue", "function"}, new String[]{"arguments"}, new String[]{"implements", "instanceof"}};
    private static final int SAVINGS_THRESHOLD = 32;

    private static boolean isLetterOrNumberChar(char c) {
        if ('0' <= c && c <= '9') {
            return true;
        }
        char c2 = (char) (c | ' ');
        return ('a' <= c2 && c2 <= 'z') || c == '_' || c == '$' || c == '-' || c == '.';
    }

    static int nextIdentChar(char c, boolean z) {
        if (c == 'z') {
            return 65;
        }
        if (c == 'Z') {
            return 95;
        }
        if (c == '_') {
            return 36;
        }
        if (c == '$') {
            return z ? 48 : -1;
        }
        if (c == '9') {
            return -1;
        }
        return (char) (c + 1);
    }

    public static String minify(String str) {
        JsonSanitizer jsonSanitizer = new JsonSanitizer(str);
        jsonSanitizer.sanitize();
        return minify(jsonSanitizer.toCharSequence()).toString();
    }

    public static String minify(String str, int i) {
        JsonSanitizer jsonSanitizer = new JsonSanitizer(str, i);
        jsonSanitizer.sanitize();
        return minify(jsonSanitizer.toCharSequence()).toString();
    }

    private static CharSequence minify(CharSequence charSequence) {
        char c;
        Token token;
        int i;
        int i2;
        HashMap hashMap = new HashMap();
        int length = charSequence.length();
        int i3 = 0;
        while (true) {
            c = '\n';
            if (i3 >= length) {
                break;
            }
            char charAt = charSequence.charAt(i3);
            if (charAt == '\"') {
                i2 = i3 + 1;
                while (true) {
                    if (i2 >= length) {
                        break;
                    }
                    char charAt2 = charSequence.charAt(i2);
                    if (charAt2 == '\\') {
                        i2++;
                    } else if (charAt2 == '\"') {
                        i2++;
                        break;
                    }
                    i2++;
                }
            } else if (isLetterOrNumberChar(charAt)) {
                i2 = i3 + 1;
                while (i2 < length && isLetterOrNumberChar(charSequence.charAt(i2))) {
                    i2++;
                }
            } else {
                i3++;
            }
            int i4 = i2;
            while (i4 < length) {
                char charAt3 = charSequence.charAt(i4);
                if (!(charAt3 == '\t' || charAt3 == '\n' || charAt3 == '\r' || charAt3 == ' ')) {
                    break;
                }
                i4++;
            }
            if (i4 == length || (':' != charSequence.charAt(i4) && i2 - i3 >= 4)) {
                Token token2 = new Token(i3, i2, charSequence);
                Token token3 = (Token) hashMap.put(token2, token2);
                if (token3 != null) {
                    token2.prev = token3;
                }
            }
            i3 = i4 - 1;
            i3++;
        }
        ArrayList arrayList = new ArrayList();
        Iterator it = hashMap.values().iterator();
        int i5 = 0;
        while (it.hasNext()) {
            Token token4 = (Token) it.next();
            if (token4.prev == null) {
                it.remove();
            } else {
                int i6 = 0;
                for (Token token5 = token4; token5 != null; token5 = token5.prev) {
                    i6++;
                }
                int i7 = ((i6 - 1) * (token4.end - token4.start)) - MARGINAL_VAR_COST;
                if (i7 > 0) {
                    i5 += i7;
                    while (token4 != null) {
                        arrayList.add(token4);
                        token4 = token4.prev;
                    }
                }
            }
        }
        if (i5 <= BOILERPLATE_COST + 32) {
            return charSequence;
        }
        Collections.sort(arrayList);
        int size = arrayList.size();
        StringBuilder sb = new StringBuilder(length);
        sb.append(ENVELOPE_P1);
        NameGenerator nameGenerator = new NameGenerator();
        boolean z = true;
        for (Token token6 : hashMap.values()) {
            String next = nameGenerator.next();
            for (; token6 != null; token6 = token6.prev) {
                token6.name = next;
            }
            if (z) {
                z = false;
            } else {
                sb.append(',');
            }
            sb.append(next);
        }
        sb.append(ENVELOPE_P2);
        int length2 = sb.length();
        int i8 = 0;
        int i9 = 0;
        while (true) {
            if (i8 < size) {
                token = (Token) arrayList.get(i8);
                i8++;
            } else {
                token = null;
            }
            int i10 = token != null ? token.start : length;
            int i11 = i9;
            boolean z2 = false;
            while (i9 < i10) {
                char charAt4 = charSequence.charAt(i9);
                if (z2) {
                    if (charAt4 == '\"') {
                        i = 1;
                        z2 = false;
                        i9 += i;
                        c = '\n';
                    } else if (charAt4 == '\\') {
                        i9++;
                    }
                } else if (charAt4 == '\t' || charAt4 == c || charAt4 == '\r' || charAt4 == ' ') {
                    if (i11 != i9) {
                        sb.append(charSequence, i11, i9);
                    }
                    i11 = i9 + 1;
                } else if (charAt4 == '\"') {
                    i = 1;
                    z2 = true;
                    i9 += i;
                    c = '\n';
                }
                i = 1;
                i9 += i;
                c = '\n';
            }
            if (i11 != i10) {
                sb.append(charSequence, i11, i10);
            }
            if (token == null) {
                break;
            }
            sb.append(token.name);
            i9 = token.end;
            c = '\n';
        }
        char charAt5 = sb.charAt(length2);
        if (!(charAt5 == '{' || charAt5 == '[' || charAt5 == '\"')) {
            sb.insert(length2, ' ');
        }
        sb.append(ENVELOPE_P3);
        boolean z3 = true;
        for (Token token7 : hashMap.values()) {
            if (z3) {
                z3 = false;
            } else {
                sb.append(',');
            }
            sb.append(token7.seq, token7.start, token7.end);
        }
        sb.append(ENVELOPE_P4);
        return sb;
    }

    /* access modifiers changed from: private */
    public static final class Token implements Comparable<Token> {
        private final int end;
        private final int hashCode;
        @Nullable
        String name;
        @Nullable
        Token prev;
        @Nonnull
        private final CharSequence seq;
        private final int start;

        Token(int i, int i2, CharSequence charSequence) {
            this.start = i;
            this.end = i2;
            this.seq = charSequence;
            int i3 = 0;
            while (i < i2) {
                i3 = (i3 * 31) + charSequence.charAt(i);
                i++;
            }
            this.hashCode = i3;
        }

        @Override // java.lang.Object
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof Token)) {
                return false;
            }
            Token token = (Token) obj;
            if (this.hashCode != token.hashCode) {
                return false;
            }
            return EvalMinifier.regionMatches(this.seq, this.start, this.end, token.seq, token.start, token.end);
        }

        @Override // java.lang.Object
        public int hashCode() {
            return this.hashCode;
        }

        public int compareTo(Token token) {
            return this.start - token.start;
        }
    }

    static boolean regionMatches(CharSequence charSequence, int i, int i2, CharSequence charSequence2, int i3, int i4) {
        if (i4 - i3 != i2 - i) {
            return false;
        }
        while (i < i2) {
            if (charSequence.charAt(i) != charSequence2.charAt(i3)) {
                return false;
            }
            i++;
            i3++;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public static final class NameGenerator {
        private final StringBuilder sb = new StringBuilder("a");

        NameGenerator() {
        }

        public String next() {
            String sb2;
            int length;
            do {
                sb2 = this.sb.toString();
                int length2 = this.sb.length();
                while (true) {
                    length2--;
                    if (length2 < 0) {
                        break;
                    }
                    int nextIdentChar = EvalMinifier.nextIdentChar(this.sb.charAt(length2), length2 != 0);
                    if (nextIdentChar >= 0) {
                        this.sb.setCharAt(length2, (char) nextIdentChar);
                        break;
                    }
                    this.sb.setCharAt(length2, 'a');
                    if (length2 == 0) {
                        this.sb.append('a');
                    }
                }
                length = sb2.length();
                if (length >= EvalMinifier.RESERVED_KEYWORDS.length) {
                    break;
                }
            } while (Arrays.binarySearch(EvalMinifier.RESERVED_KEYWORDS[length], sb2) >= 0);
            return sb2;
        }
    }
}
