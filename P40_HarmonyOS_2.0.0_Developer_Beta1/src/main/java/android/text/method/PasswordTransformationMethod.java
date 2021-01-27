package android.text.method;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.GetChars;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UpdateLayout;
import android.view.View;
import java.lang.ref.WeakReference;

public class PasswordTransformationMethod implements TransformationMethod, TextWatcher {
    private static final char ARABIC_DOT = 1645;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private static char DOT = ENGLISH_DOT;
    private static final char ENGLISH_DOT = 8226;
    private static final char FIRST_RIGHT_TO_LEFT = 1424;
    private static final char LAST_RIGHT_TO_LEFT = 1791;
    @UnsupportedAppUsage
    private static PasswordTransformationMethod sInstance;

    @Override // android.text.method.TransformationMethod
    public CharSequence getTransformation(CharSequence source, View view) {
        ViewReference[] vr;
        if (source instanceof Spannable) {
            Spannable sp = (Spannable) source;
            for (ViewReference viewReference : (ViewReference[]) sp.getSpans(0, sp.length(), ViewReference.class)) {
                sp.removeSpan(viewReference);
            }
            removeVisibleSpans(sp);
            sp.setSpan(new ViewReference(view), 0, 0, 34);
        }
        return new PasswordCharSequence(source);
    }

    public static PasswordTransformationMethod getInstance() {
        PasswordTransformationMethod passwordTransformationMethod = sInstance;
        if (passwordTransformationMethod != null) {
            return passwordTransformationMethod;
        }
        sInstance = new PasswordTransformationMethod();
        return sInstance;
    }

    @Override // android.text.TextWatcher
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override // android.text.TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s instanceof Spannable) {
            Spannable sp = (Spannable) s;
            ViewReference[] vr = (ViewReference[]) sp.getSpans(0, s.length(), ViewReference.class);
            if (vr.length != 0) {
                View v = null;
                int i = 0;
                while (v == null && i < vr.length) {
                    v = (View) vr[i].get();
                    i++;
                }
                if (v != null && (TextKeyListener.getInstance().getPrefs(v.getContext()) & 8) != 0 && count > 0) {
                    removeVisibleSpans(sp);
                    if (count == 1) {
                        sp.setSpan(new Visible(sp, this), start, start + count, 33);
                    }
                }
            }
        }
    }

    @Override // android.text.TextWatcher
    public void afterTextChanged(Editable s) {
    }

    @Override // android.text.method.TransformationMethod
    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {
        if (!focused && (sourceText instanceof Spannable)) {
            removeVisibleSpans((Spannable) sourceText);
        }
    }

    private static void removeVisibleSpans(Spannable sp) {
        Visible[] old;
        for (Visible visible : (Visible[]) sp.getSpans(0, sp.length(), Visible.class)) {
            sp.removeSpan(visible);
        }
    }

    private static class PasswordCharSequence implements CharSequence, GetChars {
        private CharSequence mSource;

        public PasswordCharSequence(CharSequence source) {
            this.mSource = source;
        }

        @Override // java.lang.CharSequence
        public int length() {
            return this.mSource.length();
        }

        @Override // java.lang.CharSequence
        public char charAt(int i) {
            CharSequence charSequence = this.mSource;
            if (charSequence instanceof Spanned) {
                Spanned sp = (Spanned) charSequence;
                int st = sp.getSpanStart(TextKeyListener.ACTIVE);
                int en = sp.getSpanEnd(TextKeyListener.ACTIVE);
                if (i >= st && i < en) {
                    return this.mSource.charAt(i);
                }
                Visible[] visible = (Visible[]) sp.getSpans(0, sp.length(), Visible.class);
                for (int a = 0; a < visible.length; a++) {
                    if (sp.getSpanStart(visible[a].mTransformer) >= 0) {
                        int st2 = sp.getSpanStart(visible[a]);
                        int en2 = sp.getSpanEnd(visible[a]);
                        if (i >= st2 && i < en2) {
                            return this.mSource.charAt(i);
                        }
                    }
                }
            }
            return PasswordTransformationMethod.DOT;
        }

        @Override // java.lang.CharSequence
        public CharSequence subSequence(int start, int end) {
            char[] buf = new char[(end - start)];
            getChars(start, end, buf, 0);
            return new String(buf);
        }

        @Override // java.lang.CharSequence, java.lang.Object
        public String toString() {
            return subSequence(0, length()).toString();
        }

        @Override // android.text.GetChars
        public void getChars(int start, int end, char[] dest, int off) {
            TextUtils.getChars(this.mSource, start, end, dest, off);
            if (this.mSource.length() > 0) {
                CharSequence charSequence = this.mSource;
                char c = charSequence.charAt(charSequence.length() - 1);
                if (c < 1424 || c > 1791) {
                    char unused = PasswordTransformationMethod.DOT = PasswordTransformationMethod.ENGLISH_DOT;
                } else {
                    char unused2 = PasswordTransformationMethod.DOT = PasswordTransformationMethod.ARABIC_DOT;
                }
            }
            int st = -1;
            int en = -1;
            int nvisible = 0;
            int[] starts = null;
            int[] ends = null;
            CharSequence charSequence2 = this.mSource;
            if (charSequence2 instanceof Spanned) {
                Spanned sp = (Spanned) charSequence2;
                st = sp.getSpanStart(TextKeyListener.ACTIVE);
                en = sp.getSpanEnd(TextKeyListener.ACTIVE);
                Visible[] visible = (Visible[]) sp.getSpans(0, sp.length(), Visible.class);
                nvisible = visible.length;
                starts = new int[nvisible];
                ends = new int[nvisible];
                for (int i = 0; i < nvisible; i++) {
                    if (sp.getSpanStart(visible[i].mTransformer) >= 0) {
                        starts[i] = sp.getSpanStart(visible[i]);
                        ends[i] = sp.getSpanEnd(visible[i]);
                    }
                }
            }
            for (int i2 = start; i2 < end; i2++) {
                if (i2 < st || i2 >= en) {
                    boolean visible2 = false;
                    int a = 0;
                    while (true) {
                        if (a >= nvisible) {
                            break;
                        }
                        if (i2 >= starts[a] && i2 < ends[a]) {
                            visible2 = true;
                            break;
                        }
                        a++;
                    }
                    if (!visible2) {
                        dest[(i2 - start) + off] = PasswordTransformationMethod.DOT;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static class Visible extends Handler implements UpdateLayout, Runnable {
        private Spannable mText;
        private PasswordTransformationMethod mTransformer;

        public Visible(Spannable sp, PasswordTransformationMethod ptm) {
            this.mText = sp;
            this.mTransformer = ptm;
            postAtTime(this, SystemClock.uptimeMillis() + 1500);
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mText.removeSpan(this);
        }
    }

    private static class ViewReference extends WeakReference<View> implements NoCopySpan {
        public ViewReference(View v) {
            super(v);
        }
    }
}
