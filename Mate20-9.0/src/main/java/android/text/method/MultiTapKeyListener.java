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

    private class Timeout extends Handler implements Runnable {
        /* access modifiers changed from: private */
        public Editable mBuffer;

        public Timeout(Editable buffer) {
            this.mBuffer = buffer;
            this.mBuffer.setSpan(this, 0, this.mBuffer.length(), 18);
            postAtTime(this, SystemClock.uptimeMillis() + 2000);
        }

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
        int off = (cap.ordinal() * 2) + (autotext);
        if (sInstance[off] == null) {
            sInstance[off] = new MultiTapKeyListener(cap, autotext);
        }
        return sInstance[off];
    }

    public int getInputType() {
        return makeTextContentType(this.mCapitalize, this.mAutoText);
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00e9  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x0180  */
    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        boolean z;
        int selStart;
        int selStart2;
        int rec;
        Editable editable = content;
        int i = keyCode;
        int pref = 0;
        if (view != null) {
            pref = TextKeyListener.getInstance().getPrefs(view.getContext());
        }
        int pref2 = pref;
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        int rec2 = Math.min(a, b);
        int selEnd = Math.max(a, b);
        int activeStart = editable.getSpanStart(TextKeyListener.ACTIVE);
        int activeEnd = editable.getSpanEnd(TextKeyListener.ACTIVE);
        int rec3 = (editable.getSpanFlags(TextKeyListener.ACTIVE) & -16777216) >>> 24;
        if (activeStart == rec2 && activeEnd == selEnd && selEnd - rec2 == 1 && rec3 >= 0 && rec3 < sRecs.size()) {
            if (i == 17) {
                char current = editable.charAt(rec2);
                if (Character.isLowerCase(current)) {
                    editable.replace(rec2, selEnd, String.valueOf(current).toUpperCase());
                    removeTimeouts(content);
                    new Timeout(editable);
                    return true;
                } else if (Character.isUpperCase(current)) {
                    editable.replace(rec2, selEnd, String.valueOf(current).toLowerCase());
                    removeTimeouts(content);
                    new Timeout(editable);
                    return true;
                }
            }
            if (sRecs.indexOfKey(i) == rec3) {
                String val = sRecs.valueAt(rec3);
                char ch = editable.charAt(rec2);
                int ix = val.indexOf(ch);
                if (ix >= 0) {
                    int ix2 = (ix + 1) % val.length();
                    char c = ch;
                    String str = val;
                    editable.replace(rec2, selEnd, val, ix2, ix2 + 1);
                    removeTimeouts(content);
                    new Timeout(editable);
                    return true;
                }
            }
            z = true;
            rec = sRecs.indexOfKey(i);
            if (rec >= 0) {
                Selection.setSelection(editable, selEnd, selEnd);
                selStart2 = rec;
                selStart = selEnd;
                if (selStart2 < 0) {
                    String val2 = sRecs.valueAt(selStart2);
                    int off = 0;
                    if ((pref2 & 1) != 0 && TextKeyListener.shouldCap(this.mCapitalize, editable, selStart)) {
                        int i2 = 0;
                        while (true) {
                            if (i2 >= val2.length()) {
                                break;
                            } else if (Character.isUpperCase(val2.charAt(i2))) {
                                off = i2;
                                break;
                            } else {
                                i2++;
                            }
                        }
                    }
                    int off2 = off;
                    if (selStart != selEnd) {
                        Selection.setSelection(editable, selEnd);
                    }
                    editable.setSpan(OLD_SEL_START, selStart, selStart, 17);
                    int i3 = pref2;
                    String str2 = val2;
                    editable.replace(selStart, selEnd, val2, off2, off2 + 1);
                    int oldStart = editable.getSpanStart(OLD_SEL_START);
                    int selEnd2 = Selection.getSelectionEnd(content);
                    if (selEnd2 != oldStart) {
                        Selection.setSelection(editable, oldStart, selEnd2);
                        editable.setSpan(TextKeyListener.LAST_TYPED, oldStart, selEnd2, 33);
                        editable.setSpan(TextKeyListener.ACTIVE, oldStart, selEnd2, 33 | (selStart2 << 24));
                    }
                    removeTimeouts(content);
                    new Timeout(editable);
                    if (editable.getSpanStart(this) < 0) {
                        for (Object method : (KeyListener[]) editable.getSpans(0, content.length(), KeyListener.class)) {
                            editable.removeSpan(method);
                        }
                        editable.setSpan(this, 0, content.length(), 18);
                    }
                    return z;
                }
                return super.onKeyDown(view, content, keyCode, event);
            }
        } else {
            z = true;
            rec = sRecs.indexOfKey(i);
        }
        selStart = rec2;
        selStart2 = rec;
        if (selStart2 < 0) {
        }
    }

    public void onSpanChanged(Spannable buf, Object what, int s, int e, int start, int stop) {
        if (what == Selection.SELECTION_END) {
            buf.removeSpan(TextKeyListener.ACTIVE);
            removeTimeouts(buf);
        }
    }

    private static void removeTimeouts(Spannable buf) {
        int i = 0;
        Timeout[] timeout = (Timeout[]) buf.getSpans(0, buf.length(), Timeout.class);
        while (true) {
            int i2 = i;
            if (i2 < timeout.length) {
                Timeout t = timeout[i2];
                t.removeCallbacks(t);
                Editable unused = t.mBuffer = null;
                buf.removeSpan(t);
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    public void onSpanAdded(Spannable s, Object what, int start, int end) {
    }

    public void onSpanRemoved(Spannable s, Object what, int start, int end) {
    }
}
