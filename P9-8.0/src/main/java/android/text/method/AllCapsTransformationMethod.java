package android.text.method;

import android.content.Context;
import android.graphics.Rect;
import android.icu.text.CaseMap;
import android.icu.text.Edits;
import android.icu.text.Edits.Iterator;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.Locale;

public class AllCapsTransformationMethod implements TransformationMethod2 {
    private static final String TAG = "AllCapsTransformationMethod";
    private boolean mEnabled;
    private Locale mLocale;

    public AllCapsTransformationMethod(Context context) {
        this.mLocale = context.getResources().getConfiguration().getLocales().get(0);
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
            if (source instanceof Spanned) {
                Edits edits = new Edits();
                SpannableStringBuilder result = (SpannableStringBuilder) CaseMap.toUpper().apply(locale, source, new SpannableStringBuilder(), edits);
                if (!edits.hasChanges()) {
                    return source;
                }
                Iterator iterator = edits.getFineIterator();
                Spanned spanned = (Spanned) source;
                int sourceLength = source.length();
                for (Object span : spanned.getSpans(0, sourceLength, Object.class)) {
                    int destStart;
                    int destEnd;
                    int sourceStart = spanned.getSpanStart(span);
                    int sourceEnd = spanned.getSpanEnd(span);
                    int flags = spanned.getSpanFlags(span);
                    if (sourceStart == sourceLength) {
                        destStart = result.length();
                    } else {
                        destStart = mapToDest(iterator, sourceStart);
                    }
                    if (sourceEnd == sourceLength) {
                        destEnd = result.length();
                    } else {
                        destEnd = mapToDest(iterator, sourceEnd);
                    }
                    result.setSpan(span, destStart, destEnd, flags);
                }
                return result;
            }
            return (CharSequence) CaseMap.toUpper().apply(locale, source, new StringBuilder(), null);
        }
    }

    private static int mapToDest(Iterator iterator, int sourceIndex) {
        iterator.findSourceIndex(sourceIndex);
        if (sourceIndex == iterator.sourceIndex()) {
            return iterator.destinationIndex();
        }
        if (iterator.hasChange()) {
            return iterator.destinationIndex() + iterator.newLength();
        }
        return iterator.destinationIndex() + (sourceIndex - iterator.sourceIndex());
    }

    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {
    }

    public void setLengthChangesAllowed(boolean allowLengthChanges) {
        this.mEnabled = allowLengthChanges;
    }
}
