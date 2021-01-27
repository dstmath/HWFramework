package ohos.global.icu.text;

public abstract class UnicodeFilter implements UnicodeMatcher {
    public abstract boolean contains(int i);

    @Override // ohos.global.icu.text.UnicodeMatcher
    public int matches(Replaceable replaceable, int[] iArr, int i, boolean z) {
        if (iArr[0] < i) {
            int char32At = replaceable.char32At(iArr[0]);
            if (contains(char32At)) {
                iArr[0] = iArr[0] + UTF16.getCharCount(char32At);
                return 2;
            }
        }
        if (iArr[0] > i && contains(replaceable.char32At(iArr[0]))) {
            iArr[0] = iArr[0] - 1;
            if (iArr[0] >= 0) {
                iArr[0] = iArr[0] - (UTF16.getCharCount(replaceable.char32At(iArr[0])) - 1);
            }
            return 2;
        } else if (!z || iArr[0] != i) {
            return 0;
        } else {
            return 1;
        }
    }

    @Deprecated
    protected UnicodeFilter() {
    }
}
