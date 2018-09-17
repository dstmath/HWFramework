package android.view;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Pair;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import java.util.List;

public abstract class ViewStructure {

    public static abstract class HtmlInfo {

        public static abstract class Builder {
            public abstract Builder addAttribute(String str, String str2);

            public abstract HtmlInfo build();
        }

        public abstract List<Pair<String, String>> getAttributes();

        public abstract String getTag();
    }

    public abstract int addChildCount(int i);

    public abstract void asyncCommit();

    public abstract ViewStructure asyncNewChild(int i);

    public abstract AutofillId getAutofillId();

    public abstract int getChildCount();

    public abstract Bundle getExtras();

    public abstract CharSequence getHint();

    public abstract Rect getTempRect();

    public abstract CharSequence getText();

    public abstract int getTextSelectionEnd();

    public abstract int getTextSelectionStart();

    public abstract boolean hasExtras();

    public abstract ViewStructure newChild(int i);

    public abstract Builder newHtmlInfoBuilder(String str);

    public abstract void setAccessibilityFocused(boolean z);

    public abstract void setActivated(boolean z);

    public abstract void setAlpha(float f);

    public abstract void setAssistBlocked(boolean z);

    public abstract void setAutofillHints(String[] strArr);

    public abstract void setAutofillId(AutofillId autofillId);

    public abstract void setAutofillId(AutofillId autofillId, int i);

    public abstract void setAutofillOptions(CharSequence[] charSequenceArr);

    public abstract void setAutofillType(int i);

    public abstract void setAutofillValue(AutofillValue autofillValue);

    public abstract void setCheckable(boolean z);

    public abstract void setChecked(boolean z);

    public abstract void setChildCount(int i);

    public abstract void setClassName(String str);

    public abstract void setClickable(boolean z);

    public abstract void setContentDescription(CharSequence charSequence);

    public abstract void setContextClickable(boolean z);

    public abstract void setDataIsSensitive(boolean z);

    public abstract void setDimens(int i, int i2, int i3, int i4, int i5, int i6);

    public abstract void setElevation(float f);

    public abstract void setEnabled(boolean z);

    public abstract void setFocusable(boolean z);

    public abstract void setFocused(boolean z);

    public abstract void setHint(CharSequence charSequence);

    public abstract void setHtmlInfo(HtmlInfo htmlInfo);

    public abstract void setId(int i, String str, String str2, String str3);

    public abstract void setInputType(int i);

    public abstract void setLocaleList(LocaleList localeList);

    public abstract void setLongClickable(boolean z);

    public abstract void setOpaque(boolean z);

    public abstract void setSelected(boolean z);

    public abstract void setText(CharSequence charSequence);

    public abstract void setText(CharSequence charSequence, int i, int i2);

    public abstract void setTextLines(int[] iArr, int[] iArr2);

    public abstract void setTextStyle(float f, int i, int i2, int i3);

    public abstract void setTransformation(Matrix matrix);

    public abstract void setVisibility(int i);

    public abstract void setWebDomain(String str);
}
