package ohos.global.i18n.text;

public class Transliterator {
    private ohos.global.icu.text.Transliterator mTransliterator;

    private Transliterator(ohos.global.icu.text.Transliterator transliterator) {
        this.mTransliterator = transliterator;
    }

    public static final Transliterator getInstance(String str) {
        return new Transliterator(ohos.global.icu.text.Transliterator.getInstance(str));
    }

    public final String transliterate(String str) {
        ohos.global.icu.text.Transliterator transliterator = this.mTransliterator;
        if (transliterator != null) {
            return transliterator.transliterate(str);
        }
        return null;
    }
}
