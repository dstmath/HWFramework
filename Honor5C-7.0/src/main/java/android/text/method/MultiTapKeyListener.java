package android.text.method;

import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.method.TextKeyListener.Capitalize;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;

public class MultiTapKeyListener extends BaseKeyListener implements SpanWatcher {
    private static MultiTapKeyListener[] sInstance;
    private static final SparseArray<String> sRecs = null;
    private boolean mAutoText;
    private Capitalize mCapitalize;

    private class Timeout extends Handler implements Runnable {
        private Editable mBuffer;

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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.method.MultiTapKeyListener.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.method.MultiTapKeyListener.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.MultiTapKeyListener.<clinit>():void");
    }

    public MultiTapKeyListener(Capitalize cap, boolean autotext) {
        this.mCapitalize = cap;
        this.mAutoText = autotext;
    }

    public static MultiTapKeyListener getInstance(boolean autotext, Capitalize cap) {
        int off = (cap.ordinal() * 2) + (autotext ? 1 : 0);
        if (sInstance[off] == null) {
            sInstance[off] = new MultiTapKeyListener(cap, autotext);
        }
        return sInstance[off];
    }

    public int getInputType() {
        return BaseKeyListener.makeTextContentType(this.mCapitalize, this.mAutoText);
    }

    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        Timeout timeout;
        String val;
        int pref = 0;
        if (view != null) {
            pref = TextKeyListener.getInstance().getPrefs(view.getContext());
        }
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);
        int activeStart = content.getSpanStart(TextKeyListener.ACTIVE);
        int activeEnd = content.getSpanEnd(TextKeyListener.ACTIVE);
        int rec = (content.getSpanFlags(TextKeyListener.ACTIVE) & View.MEASURED_STATE_MASK) >>> 24;
        if (activeStart == selStart && activeEnd == selEnd && selEnd - selStart == 1 && rec >= 0 && rec < sRecs.size()) {
            if (keyCode == 17) {
                char current = content.charAt(selStart);
                if (Character.isLowerCase(current)) {
                    content.replace(selStart, selEnd, String.valueOf(current).toUpperCase());
                    removeTimeouts(content);
                    timeout = new Timeout(content);
                    return true;
                } else if (Character.isUpperCase(current)) {
                    content.replace(selStart, selEnd, String.valueOf(current).toLowerCase());
                    removeTimeouts(content);
                    timeout = new Timeout(content);
                    return true;
                }
            }
            if (sRecs.indexOfKey(keyCode) == rec) {
                val = (String) sRecs.valueAt(rec);
                int ix = val.indexOf(content.charAt(selStart));
                if (ix >= 0) {
                    ix = (ix + 1) % val.length();
                    content.replace(selStart, selEnd, val, ix, ix + 1);
                    removeTimeouts(content);
                    timeout = new Timeout(content);
                    return true;
                }
            }
            rec = sRecs.indexOfKey(keyCode);
            if (rec >= 0) {
                Selection.setSelection(content, selEnd, selEnd);
                selStart = selEnd;
            }
        } else {
            rec = sRecs.indexOfKey(keyCode);
        }
        if (rec < 0) {
            return super.onKeyDown(view, content, keyCode, event);
        }
        val = (String) sRecs.valueAt(rec);
        int off = 0;
        if ((pref & 1) != 0 && TextKeyListener.shouldCap(this.mCapitalize, content, selStart)) {
            for (int i = 0; i < val.length(); i++) {
                if (Character.isUpperCase(val.charAt(i))) {
                    off = i;
                    break;
                }
            }
        }
        if (selStart != selEnd) {
            Selection.setSelection(content, selEnd);
        }
        content.setSpan(OLD_SEL_START, selStart, selStart, 17);
        content.replace(selStart, selEnd, val, off, off + 1);
        int oldStart = content.getSpanStart(OLD_SEL_START);
        selEnd = Selection.getSelectionEnd(content);
        if (selEnd != oldStart) {
            Selection.setSelection(content, oldStart, selEnd);
            content.setSpan(TextKeyListener.LAST_TYPED, oldStart, selEnd, 33);
            content.setSpan(TextKeyListener.ACTIVE, oldStart, selEnd, (rec << 24) | 33);
        }
        removeTimeouts(content);
        timeout = new Timeout(content);
        if (content.getSpanStart(this) < 0) {
            for (Object removeSpan : (KeyListener[]) content.getSpans(0, content.length(), KeyListener.class)) {
                content.removeSpan(removeSpan);
            }
            content.setSpan(this, 0, content.length(), 18);
        }
        return true;
    }

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

    public void onSpanAdded(Spannable s, Object what, int start, int end) {
    }

    public void onSpanRemoved(Spannable s, Object what, int start, int end) {
    }
}
