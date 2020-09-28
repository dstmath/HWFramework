package android.text.method;

import android.graphics.Rect;
import android.text.Editable;
import android.text.GetChars;
import android.text.Spannable;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.view.View;

public abstract class ReplacementTransformationMethod implements TransformationMethod {
    /* access modifiers changed from: protected */
    public abstract char[] getOriginal();

    /* access modifiers changed from: protected */
    public abstract char[] getReplacement();

    @Override // android.text.method.TransformationMethod
    public CharSequence getTransformation(CharSequence source, View v) {
        char[] original = getOriginal();
        char[] replacement = getReplacement();
        if (!(source instanceof Editable)) {
            boolean doNothing = true;
            int n = original.length;
            int i = 0;
            while (true) {
                if (i >= n) {
                    break;
                } else if (TextUtils.indexOf(source, original[i]) >= 0) {
                    doNothing = false;
                    break;
                } else {
                    i++;
                }
            }
            if (doNothing) {
                return source;
            }
            if (!(source instanceof Spannable)) {
                if (source instanceof Spanned) {
                    return new SpannedString(new SpannedReplacementCharSequence((Spanned) source, original, replacement));
                }
                return new ReplacementCharSequence(source, original, replacement).toString();
            }
        }
        if (source instanceof Spanned) {
            return new SpannedReplacementCharSequence((Spanned) source, original, replacement);
        }
        return new ReplacementCharSequence(source, original, replacement);
    }

    @Override // android.text.method.TransformationMethod
    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {
    }

    private static class ReplacementCharSequence implements CharSequence, GetChars {
        private char[] mOriginal;
        private char[] mReplacement;
        private CharSequence mSource;

        public ReplacementCharSequence(CharSequence source, char[] original, char[] replacement) {
            this.mSource = source;
            this.mOriginal = original;
            this.mReplacement = replacement;
        }

        public int length() {
            return this.mSource.length();
        }

        public char charAt(int i) {
            char c = this.mSource.charAt(i);
            int n = this.mOriginal.length;
            for (int j = 0; j < n; j++) {
                if (c == this.mOriginal[j]) {
                    c = this.mReplacement[j];
                }
            }
            return c;
        }

        public CharSequence subSequence(int start, int end) {
            char[] c = new char[(end - start)];
            getChars(start, end, c, 0);
            return new String(c);
        }

        public String toString() {
            char[] c = new char[length()];
            getChars(0, length(), c, 0);
            return new String(c);
        }

        @Override // android.text.GetChars
        public void getChars(int start, int end, char[] dest, int off) {
            TextUtils.getChars(this.mSource, start, end, dest, off);
            int offend = (end - start) + off;
            int n = this.mOriginal.length;
            for (int i = off; i < offend; i++) {
                char c = dest[i];
                for (int j = 0; j < n; j++) {
                    if (c == this.mOriginal[j]) {
                        dest[i] = this.mReplacement[j];
                    }
                }
            }
        }
    }

    private static class SpannedReplacementCharSequence extends ReplacementCharSequence implements Spanned {
        private Spanned mSpanned;

        public SpannedReplacementCharSequence(Spanned source, char[] original, char[] replacement) {
            super(source, original, replacement);
            this.mSpanned = source;
        }

        @Override // android.text.method.ReplacementTransformationMethod.ReplacementCharSequence
        public CharSequence subSequence(int start, int end) {
            return new SpannedString(this).subSequence(start, end);
        }

        @Override // android.text.Spanned
        public <T> T[] getSpans(int start, int end, Class<T> type) {
            return (T[]) this.mSpanned.getSpans(start, end, type);
        }

        @Override // android.text.Spanned
        public int getSpanStart(Object tag) {
            return this.mSpanned.getSpanStart(tag);
        }

        @Override // android.text.Spanned
        public int getSpanEnd(Object tag) {
            return this.mSpanned.getSpanEnd(tag);
        }

        @Override // android.text.Spanned
        public int getSpanFlags(Object tag) {
            return this.mSpanned.getSpanFlags(tag);
        }

        @Override // android.text.Spanned
        public int nextSpanTransition(int start, int end, Class type) {
            return this.mSpanned.nextSpanTransition(start, end, type);
        }
    }
}
