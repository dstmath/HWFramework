package android.text.method;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.Locale;

public class AllCapsTransformationMethod implements TransformationMethod2 {
    private static final String TAG = "AllCapsTransformationMethod";
    private boolean mEnabled;
    private Locale mLocale;

    public AllCapsTransformationMethod(Context context) {
        this.mLocale = context.getResources().getConfiguration().locale;
    }

    public CharSequence getTransformation(CharSequence source, View view) {
        if (!this.mEnabled) {
            Log.w(TAG, "Caller did not enable length changes; not transforming text");
            return source;
        } else if (source == null) {
            return null;
        } else {
            Locale locale = null;
            if (view instanceof TextView) {
                locale = ((TextView) view).getTextLocale();
            }
            if (locale == null) {
                locale = this.mLocale;
            }
            return source.toString().toUpperCase(locale);
        }
    }

    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {
    }

    public void setLengthChangesAllowed(boolean allowLengthChanges) {
        this.mEnabled = allowLengthChanges;
    }
}
