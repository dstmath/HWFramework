package com.huawei.android.icu.text;

import android.icu.text.Transliterator;

public class TransliteratorEx {
    private Transliterator mTransliterator;

    private TransliteratorEx(Transliterator transliterator) {
        this.mTransliterator = transliterator;
    }

    public static final TransliteratorEx getInstance(String ID) {
        return new TransliteratorEx(Transliterator.getInstance(ID));
    }

    public final String transliterate(String text) {
        return this.mTransliterator.transliterate(text);
    }
}
