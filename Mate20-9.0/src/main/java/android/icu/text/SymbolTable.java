package android.icu.text;

import java.text.ParsePosition;

public interface SymbolTable {
    public static final char SYMBOL_REF = '$';

    char[] lookup(String str);

    UnicodeMatcher lookupMatcher(int i);

    String parseReference(String str, ParsePosition parsePosition, int i);
}
