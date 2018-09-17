package android.icu.text;

@Deprecated
public interface RbnfLenientScanner {
    @Deprecated
    boolean allIgnorable(String str);

    @Deprecated
    int[] findText(String str, String str2, int i);

    @Deprecated
    int prefixLength(String str, String str2);
}
