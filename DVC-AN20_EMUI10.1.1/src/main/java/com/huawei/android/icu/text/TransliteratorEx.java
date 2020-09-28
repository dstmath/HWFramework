package com.huawei.android.icu.text;

import android.icu.text.Transliterator;

public class TransliteratorEx {
    private Transliterator mTransliterator;

    private TransliteratorEx(Transliterator transliterator) {
        this.mTransliterator = transliterator;
    }

    public static final TransliteratorEx getInstance(String id) {
        return new TransliteratorEx(Transliterator.getInstance(id));
    }

    public final String transliterate(String text) {
        Transliterator transliterator = this.mTransliterator;
        if (transliterator != null) {
            return transliterator.transliterate(text);
        }
        return null;
    }
}
