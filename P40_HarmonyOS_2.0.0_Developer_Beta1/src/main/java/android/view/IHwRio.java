package android.view;

import android.content.Context;

public interface IHwRio {
    void attachRio(Context context, View view, CharSequence charSequence, Display display);

    void detachRio();

    void hookAttribute();
}
