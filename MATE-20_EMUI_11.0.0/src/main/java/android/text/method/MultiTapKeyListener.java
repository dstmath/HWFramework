package android.text.method;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.method.TextKeyListener;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;

public class MultiTapKeyListener extends BaseKeyListener implements SpanWatcher {
    private static MultiTapKeyListener[] sInstance = new MultiTapKeyListener[(TextKeyListener.Capitalize.values().length * 2)];
    private static final SparseArray<String> sRecs = new SparseArray<>();
    private boolean mAutoText;
    private TextKeyListener.Capitalize mCapitalize;

    static {
        sRecs.put(8, ".,1!@#$%^&*:/?'=()");
        sRecs.put(9, "abc2ABC");
        sRecs.put(10, "def3DEF");
        sRecs.put(11, "ghi4GHI");
        sRecs.put(12, "jkl5JKL");
        sRecs.put(13, "mno6MNO");
        sRecs.put(14, "pqrs7PQRS");
        sRecs.put(15, "tuv8TUV");
        sRecs.put(16, "wxyz9WXYZ");
        sRecs.put(7, "0+");
        sRecs.put(18, WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
    }

    public MultiTapKeyListener(TextKeyListener.Capitalize cap, boolean autotext) {
        this.mCapitalize = cap;
        this.mAutoText = autotext;
    }

    public static MultiTapKeyListener getInstance(boolean autotext, TextKeyListener.Capitalize cap) {
        int off = (cap.ordinal() * 2) + (autotext ? 1 : 0);
        MultiTapKeyListener[] multiTapKeyListenerArr = sInstance;
        if (multiTapKeyListenerArr[off] == null) {
            multiTapKeyListenerArr[off] = new MultiTapKeyListener(cap, autotext);
        }
        return sInstance[off];
    }

    @Override // android.text.method.KeyListener
    public int getInputType() {
        return makeTextContentType(this.mCapitalize, this.mAutoText);
    }

    @Override // android.text.method.BaseKeyListener, android.text.method.MetaKeyKeyListener, android.text.method.KeyListener
    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        int rec;
        int selStart;
        int off;
        int pref = view != null ? TextKeyListener.getInstance().getPrefs(view.getContext()) : 0;
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        int selStart2 = Math.min(a, b);
        int selEnd = Math.max(a, b);
        int activeStart = content.getSpanStart(TextKeyListener.ACTIVE);
        int activeEnd = content.getSpanEnd(TextKeyListener.ACTIVE);
        int rec2 = (content.getSpanFlags(TextKeyListener.ACTIVE) & -16777216) >>> 24;
        if (activeStart == selStart2 && activeEnd == selEnd && selEnd - selStart2 == 1 && rec2 >= 0 && rec2 < sRecs.size()) {
            if (keyCode == 17) {
                char current = content.charAt(selStart2);
                if (Character.isLowerCase(current)) {
                    content.replace(selStart2, selEnd, String.valueOf(current).toUpperCase());
                    removeTimeouts(content);
                    new Timeout(content);
                    return true;
                } else if (Character.isUpperCase(current)) {
                    content.replace(selStart2, selEnd, String.valueOf(current).toLowerCase());
                    removeTimeouts(content);
                    new Timeout(content);
                    return true;
                }
            }
            if (sRecs.indexOfKey(keyCode) == rec2) {
                String val = sRecs.valueAt(rec2);
                int ix = val.indexOf(content.charAt(selStart2));
                if (ix >= 0) {
                    int ix2 = (ix + 1) % val.length();
                    content.replace(selStart2, selEnd, val, ix2, ix2 + 1);
                    removeTimeouts(content);
                    new Timeout(content);
                    return true;
                }
            }
            int rec3 = sRecs.indexOfKey(keyCode);
            if (rec3 >= 0) {
                Selection.setSelection(content, selEnd, selEnd);
                selStart = rec3;
                rec = selEnd;
            } else {
                rec = selStart2;
                selStart = rec3;
            }
        } else {
            rec = selStart2;
            selStart = sRecs.indexOfKey(keyCode);
        }
        if (selStart < 0) {
            return super.onKeyDown(view, content, keyCode, event);
        }
        String val2 = sRecs.valueAt(selStart);
        if ((pref & 1) != 0 && TextKeyListener.shouldCap(this.mCapitalize, content, rec)) {
            int i = 0;
            while (true) {
                if (i >= val2.length()) {
                    break;
                } else if (Character.isUpperCase(val2.charAt(i))) {
                    off = i;
                    break;
                } else {
                    i++;
                }
            }
        }
        off = 0;
        if (rec != selEnd) {
            Selection.setSelection(content, selEnd);
        }
        content.setSpan(OLD_SEL_START, rec, rec, 17);
        content.replace(rec, selEnd, val2, off, off + 1);
        int oldStart = content.getSpanStart(OLD_SEL_START);
        int selEnd2 = Selection.getSelectionEnd(content);
        if (selEnd2 != oldStart) {
            Selection.setSelection(content, oldStart, selEnd2);
            content.setSpan(TextKeyListener.LAST_TYPED, oldStart, selEnd2, 33);
            content.setSpan(TextKeyListener.ACTIVE, oldStart, selEnd2, 33 | (selStart << 24));
        }
        removeTimeouts(content);
        new Timeout(content);
        if (content.getSpanStart(this) < 0) {
            for (Object method : (KeyListener[]) content.getSpans(0, content.length(), KeyListener.class)) {
                content.removeSpan(method);
            }
            content.setSpan(this, 0, content.length(), 18);
        }
        return true;
    }

    @Override // android.text.SpanWatcher
    public void onSpanChanged(Spannable buf, Object what, int s, int e, int start, int stop) {
        if (what == Selection.SELECTION_END) {
            buf.removeSpan(TextKeyListener.ACTIVE);
            removeTimeouts(buf);
        }
    }

    private static void removeTimeouts(Spannable buf) {
        Timeout[] timeout = (Timeout[]) buf.getSpans(0, buf.length(), Timeout.class);
        for (Timeout t : timeout) {
            t.removeCallbacks(t);
            t.mBuffer = null;
            buf.removeSpan(t);
        }
    }

    /* access modifiers changed from: private */
    public class Timeout extends Handler implements Runnable {
        private Editable mBuffer;

        public Timeout(Editable buffer) {
            this.mBuffer = buffer;
            Editable editable = this.mBuffer;
            editable.setSpan(this, 0, editable.length(), 18);
            postAtTime(this, SystemClock.uptimeMillis() + 2000);
        }

        @Override // java.lang.Runnable
        public void run() {
            Spannable buf = this.mBuffer;
            if (buf != null) {
                int st = Selection.getSelectionStart(buf);
                int en = Selection.getSelectionEnd(buf);
                int start = buf.getSpanStart(TextKeyListener.ACTIVE);
                int end = buf.getSpanEnd(TextKeyListener.ACTIVE);
                if (st == start && en == end) {
                    Selection.setSelection(buf, Selection.getSelectionEnd(buf));
                }
                buf.removeSpan(this);
            }
        }
    }

    @Override // android.text.SpanWatcher
    public void onSpanAdded(Spannable s, Object what, int start, int end) {
    }

    @Override // android.text.SpanWatcher
    public void onSpanRemoved(Spannable s, Object what, int start, int end) {
    }
}
