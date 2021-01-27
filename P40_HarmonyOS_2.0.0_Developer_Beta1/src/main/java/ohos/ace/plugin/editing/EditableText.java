package ohos.ace.plugin.editing;

import com.huawei.ace.runtime.ALog;

/* access modifiers changed from: package-private */
public class EditableText {
    private static final String LOG_TAG = "Ace_IME";
    private int composingEnd;
    private int composingStart;
    private String content;
    private int selectionEnd;
    private int selectionStart;

    public EditableText() {
        this("");
    }

    public EditableText(String str) {
        this.selectionStart = 0;
        this.selectionEnd = 0;
        this.composingStart = 0;
        this.composingEnd = 0;
        this.content = str;
    }

    private int clampIndex(int i) {
        return Math.max(0, Math.min(this.content.length(), i));
    }

    public void replace(String str, int i, int i2) {
        this.content = str;
        setSelection(i, i2);
    }

    public EditableText deleteSurroundingTextInCodePoints(int i, int i2) {
        int i3;
        int i4 = this.selectionEnd;
        if (i > 0) {
            i3 = i4 - 1;
            while (i3 >= 0) {
                try {
                    if (this.content.codePointCount(i3, this.selectionEnd) >= i + 1) {
                        break;
                    }
                    i3--;
                } catch (IndexOutOfBoundsException unused) {
                    ALog.w(LOG_TAG, "delete exceed bounds.");
                }
            }
            i3++;
        } else {
            i3 = i4;
        }
        if (i2 > 0) {
            i4 = this.content.offsetByCodePoints(this.selectionEnd, i2);
        }
        ALog.d(LOG_TAG, "before index " + i3 + " after index " + i4);
        return i4 > i3 ? delete(i3, i4) : this;
    }

    public EditableText deleteSurroundingText(int i, int i2) {
        int i3 = this.selectionEnd;
        int i4 = this.selectionStart;
        if (i3 > i4) {
            return delete(i4, i3);
        }
        int clampIndex = clampIndex(i3 - i);
        int min = Math.min(this.content.length(), this.selectionEnd + i2);
        return min > clampIndex ? delete(clampIndex, min) : this;
    }

    public EditableText commit(String str, int i) {
        int i2 = this.selectionStart;
        int i3 = this.selectionEnd;
        if (i2 != i3) {
            delete(i2, i3);
        }
        int i4 = this.composingStart;
        int i5 = this.composingEnd;
        if (i4 != i5) {
            delete(i4, i5);
            this.composingStart = 0;
            this.composingEnd = 0;
        }
        append(str, this.selectionEnd);
        return this;
    }

    public EditableText delete(int i, int i2) {
        ALog.d(LOG_TAG, "EditableText delete from " + i + " to " + i2);
        int clampIndex = clampIndex(i);
        int clampIndex2 = clampIndex(i2);
        int min = Math.min(clampIndex, clampIndex2);
        int max = Math.max(clampIndex, clampIndex2);
        if (min < max) {
            this.content = this.content.substring(0, min) + this.content.substring(max);
            setSelection(min);
        }
        return this;
    }

    public EditableText append(String str, int i) {
        ALog.d(LOG_TAG, "EditableText append text in place " + i);
        int clampIndex = clampIndex(i);
        this.content = this.content.substring(0, clampIndex) + str + this.content.substring(clampIndex);
        setSelection(clampIndex + str.length());
        return this;
    }

    public int length() {
        return this.content.length();
    }

    public void setSelection(int i) {
        setSelection(i, i);
    }

    public void setSelection(int i, int i2) {
        this.selectionStart = clampIndex(i);
        this.selectionEnd = clampIndex(i2);
    }

    public int getSelectionStart() {
        return this.selectionStart;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public String getContent() {
        return this.content;
    }

    public void setComposingIndex(int i, int i2) {
        this.composingStart = clampIndex(i);
        this.composingEnd = clampIndex(i2);
    }
}
