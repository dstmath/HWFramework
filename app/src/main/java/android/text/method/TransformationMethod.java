package android.text.method;

import android.graphics.Rect;
import android.view.View;

public interface TransformationMethod {
    CharSequence getTransformation(CharSequence charSequence, View view);

    void onFocusChanged(View view, CharSequence charSequence, boolean z, int i, Rect rect);
}
