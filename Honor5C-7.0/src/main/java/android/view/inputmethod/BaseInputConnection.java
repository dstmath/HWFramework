package android.view.inputmethod;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Editable.Factory;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import javax.microedition.khronos.opengles.GL10;

public class BaseInputConnection implements InputConnection {
    static final Object COMPOSING = null;
    private static final boolean DEBUG = false;
    private static int INVALID_INDEX = 0;
    private static final String TAG = "BaseInputConnection";
    private Object[] mDefaultComposingSpans;
    final boolean mDummyMode;
    Editable mEditable;
    protected final InputMethodManager mIMM;
    KeyCharacterMap mKeyCharacterMap;
    final View mTargetView;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.inputmethod.BaseInputConnection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.inputmethod.BaseInputConnection.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.inputmethod.BaseInputConnection.<clinit>():void");
    }

    BaseInputConnection(InputMethodManager mgr, boolean fullEditor) {
        this.mIMM = mgr;
        this.mTargetView = null;
        this.mDummyMode = fullEditor ? DEBUG : true;
    }

    public BaseInputConnection(View targetView, boolean fullEditor) {
        this.mIMM = (InputMethodManager) targetView.getContext().getSystemService("input_method");
        this.mTargetView = targetView;
        this.mDummyMode = fullEditor ? DEBUG : true;
    }

    public static final void removeComposingSpans(Spannable text) {
        text.removeSpan(COMPOSING);
        Object[] sps = text.getSpans(0, text.length(), Object.class);
        if (sps != null) {
            for (int i = sps.length - 1; i >= 0; i--) {
                Object o = sps[i];
                if ((text.getSpanFlags(o) & GL10.GL_DEPTH_BUFFER_BIT) != 0) {
                    text.removeSpan(o);
                }
            }
        }
    }

    public static void setComposingSpans(Spannable text) {
        setComposingSpans(text, 0, text.length());
    }

    public static void setComposingSpans(Spannable text, int start, int end) {
        Object[] sps = text.getSpans(start, end, Object.class);
        if (sps != null) {
            for (int i = sps.length - 1; i >= 0; i--) {
                Object o = sps[i];
                if (o == COMPOSING) {
                    text.removeSpan(o);
                } else {
                    int fl = text.getSpanFlags(o);
                    if ((fl & MetricsEvent.ACTION_TUNER_CALIBRATE_DISPLAY_CHANGED) != MetricsEvent.OVERVIEW_DISMISS) {
                        text.setSpan(o, text.getSpanStart(o), text.getSpanEnd(o), ((fl & -52) | GL10.GL_DEPTH_BUFFER_BIT) | 33);
                    }
                }
            }
        }
        text.setSpan(COMPOSING, start, end, MetricsEvent.OVERVIEW_DISMISS);
    }

    public static int getComposingSpanStart(Spannable text) {
        return text.getSpanStart(COMPOSING);
    }

    public static int getComposingSpanEnd(Spannable text) {
        return text.getSpanEnd(COMPOSING);
    }

    public Editable getEditable() {
        if (this.mEditable == null) {
            this.mEditable = Factory.getInstance().newEditable("");
            Selection.setSelection(this.mEditable, 0);
        }
        return this.mEditable;
    }

    public boolean beginBatchEdit() {
        return DEBUG;
    }

    public boolean endBatchEdit() {
        return DEBUG;
    }

    public void closeConnection() {
        finishComposingText();
    }

    public boolean clearMetaKeyStates(int states) {
        Editable content = getEditable();
        if (content == null) {
            return DEBUG;
        }
        MetaKeyKeyListener.clearMetaKeyState(content, states);
        return true;
    }

    public boolean commitCompletion(CompletionInfo text) {
        return DEBUG;
    }

    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        return DEBUG;
    }

    public boolean commitText(CharSequence text, int newCursorPosition) {
        replaceText(text, newCursorPosition, DEBUG);
        sendCurrentText();
        return true;
    }

    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        Editable content = getEditable();
        if (content == null) {
            return DEBUG;
        }
        beginBatchEdit();
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        if (a < 0 || b < 0) {
            return DEBUG;
        }
        if (a > b) {
            int tmp = a;
            a = b;
            b = tmp;
        }
        int ca = getComposingSpanStart(content);
        int cb = getComposingSpanEnd(content);
        if (cb < ca) {
            tmp = ca;
            ca = cb;
            cb = tmp;
        }
        if (!(ca == -1 || cb == -1)) {
            if (ca < a) {
                a = ca;
            }
            if (cb > b) {
                b = cb;
            }
        }
        int deleted = 0;
        if (beforeLength > 0) {
            int start = a - beforeLength;
            if (start < 0) {
                start = 0;
            }
            content.delete(start, a);
            deleted = a - start;
        }
        if (afterLength > 0) {
            b -= deleted;
            int end = b + afterLength;
            if (end > content.length()) {
                end = content.length();
            }
            content.delete(b, end);
        }
        endBatchEdit();
        return true;
    }

    private static int findIndexBackward(CharSequence cs, int from, int numCodePoints) {
        int currentIndex = from;
        boolean waitingHighSurrogate = DEBUG;
        int N = cs.length();
        if (from < 0 || N < from) {
            return INVALID_INDEX;
        }
        if (numCodePoints < 0) {
            return INVALID_INDEX;
        }
        int remainingCodePoints = numCodePoints;
        while (remainingCodePoints != 0) {
            currentIndex--;
            if (currentIndex >= 0) {
                char c = cs.charAt(currentIndex);
                if (waitingHighSurrogate) {
                    if (!Character.isHighSurrogate(c)) {
                        return INVALID_INDEX;
                    }
                    waitingHighSurrogate = DEBUG;
                    remainingCodePoints--;
                } else if (!Character.isSurrogate(c)) {
                    remainingCodePoints--;
                } else if (Character.isHighSurrogate(c)) {
                    return INVALID_INDEX;
                } else {
                    waitingHighSurrogate = true;
                }
            } else if (waitingHighSurrogate) {
                return INVALID_INDEX;
            } else {
                return 0;
            }
        }
        return currentIndex;
    }

    private static int findIndexForward(CharSequence cs, int from, int numCodePoints) {
        int currentIndex = from;
        boolean waitingLowSurrogate = DEBUG;
        int N = cs.length();
        if (from < 0 || N < from) {
            return INVALID_INDEX;
        }
        if (numCodePoints < 0) {
            return INVALID_INDEX;
        }
        int remainingCodePoints = numCodePoints;
        while (remainingCodePoints != 0) {
            if (currentIndex < N) {
                char c = cs.charAt(currentIndex);
                if (waitingLowSurrogate) {
                    if (!Character.isLowSurrogate(c)) {
                        return INVALID_INDEX;
                    }
                    remainingCodePoints--;
                    waitingLowSurrogate = DEBUG;
                    currentIndex++;
                } else if (!Character.isSurrogate(c)) {
                    remainingCodePoints--;
                    currentIndex++;
                } else if (Character.isLowSurrogate(c)) {
                    return INVALID_INDEX;
                } else {
                    waitingLowSurrogate = true;
                    currentIndex++;
                }
            } else if (waitingLowSurrogate) {
                return INVALID_INDEX;
            } else {
                return N;
            }
        }
        return currentIndex;
    }

    public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
        Editable content = getEditable();
        if (content == null) {
            return DEBUG;
        }
        beginBatchEdit();
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        if (a > b) {
            int tmp = a;
            a = b;
            b = tmp;
        }
        int ca = getComposingSpanStart(content);
        int cb = getComposingSpanEnd(content);
        if (cb < ca) {
            tmp = ca;
            ca = cb;
            cb = tmp;
        }
        if (!(ca == -1 || cb == -1)) {
            if (ca < a) {
                a = ca;
            }
            if (cb > b) {
                b = cb;
            }
        }
        if (a >= 0 && b >= 0) {
            int start = findIndexBackward(content, a, Math.max(beforeLength, 0));
            if (start != INVALID_INDEX) {
                int end = findIndexForward(content, b, Math.max(afterLength, 0));
                if (end != INVALID_INDEX) {
                    int numDeleteBefore = a - start;
                    if (numDeleteBefore > 0) {
                        content.delete(start, a);
                    }
                    if (end - b > 0) {
                        content.delete(b - numDeleteBefore, end - numDeleteBefore);
                    }
                }
            }
        }
        endBatchEdit();
        return true;
    }

    public boolean finishComposingText() {
        Editable content = getEditable();
        if (content != null) {
            beginBatchEdit();
            removeComposingSpans(content);
            sendCurrentText();
            endBatchEdit();
        }
        return true;
    }

    public int getCursorCapsMode(int reqModes) {
        if (this.mDummyMode) {
            return 0;
        }
        Editable content = getEditable();
        if (content == null) {
            return 0;
        }
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        if (a > b) {
            int tmp = a;
            a = b;
            b = tmp;
        }
        return TextUtils.getCapsMode(content, a, reqModes);
    }

    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        return null;
    }

    public CharSequence getTextBeforeCursor(int length, int flags) {
        Editable content = getEditable();
        if (content == null) {
            return null;
        }
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        if (a > b) {
            int tmp = a;
            a = b;
            b = tmp;
        }
        if (a <= 0) {
            return "";
        }
        if (length > a) {
            length = a;
        }
        if ((flags & 1) != 0) {
            return content.subSequence(a - length, a);
        }
        return TextUtils.substring(content, a - length, a);
    }

    public CharSequence getSelectedText(int flags) {
        Editable content = getEditable();
        if (content == null) {
            return null;
        }
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        if (a > b) {
            int tmp = a;
            a = b;
            b = tmp;
        }
        if (a == b) {
            return null;
        }
        if ((flags & 1) != 0) {
            return content.subSequence(a, b);
        }
        return TextUtils.substring(content, a, b);
    }

    public CharSequence getTextAfterCursor(int length, int flags) {
        Editable content = getEditable();
        if (content == null) {
            return null;
        }
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        if (a > b) {
            int tmp = a;
            a = b;
            b = tmp;
        }
        if (b < 0) {
            b = 0;
        }
        if (b + length > content.length()) {
            length = content.length() - b;
        }
        if ((flags & 1) != 0) {
            return content.subSequence(b, b + length);
        }
        return TextUtils.substring(content, b, b + length);
    }

    public boolean performEditorAction(int actionCode) {
        long eventTime = SystemClock.uptimeMillis();
        sendKeyEvent(new KeyEvent(eventTime, eventTime, 0, 66, 0, 0, -1, 0, 22));
        sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime, 1, 66, 0, 0, -1, 0, 22));
        return true;
    }

    public boolean performContextMenuAction(int id) {
        return DEBUG;
    }

    public boolean performPrivateCommand(String action, Bundle data) {
        return DEBUG;
    }

    public boolean requestCursorUpdates(int cursorUpdateMode) {
        return DEBUG;
    }

    public Handler getHandler() {
        return null;
    }

    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        replaceText(text, newCursorPosition, true);
        return true;
    }

    public boolean setComposingRegion(int start, int end) {
        Editable content = getEditable();
        if (content != null) {
            beginBatchEdit();
            removeComposingSpans(content);
            int a = start;
            int b = end;
            if (start > end) {
                int tmp = start;
                a = end;
                b = start;
            }
            int length = content.length();
            if (a < 0) {
                a = 0;
            }
            if (b < 0) {
                b = 0;
            }
            if (a > length) {
                a = length;
            }
            if (b > length) {
                b = length;
            }
            ensureDefaultComposingSpans();
            if (this.mDefaultComposingSpans != null) {
                for (Object span : this.mDefaultComposingSpans) {
                    content.setSpan(span, a, b, MetricsEvent.OVERVIEW_DISMISS);
                }
            }
            content.setSpan(COMPOSING, a, b, MetricsEvent.OVERVIEW_DISMISS);
            sendCurrentText();
            endBatchEdit();
        }
        return true;
    }

    public boolean setSelection(int start, int end) {
        CharSequence content = getEditable();
        if (content == null) {
            return DEBUG;
        }
        int len = content.length();
        if (start > len || end > len || start < 0 || end < 0) {
            return true;
        }
        if (start != end || MetaKeyKeyListener.getMetaState(content, (int) GL10.GL_EXP) == 0) {
            Selection.setSelection(content, start, end);
        } else {
            Selection.extendSelection(content, start);
        }
        return true;
    }

    public boolean sendKeyEvent(KeyEvent event) {
        this.mIMM.dispatchKeyEventFromInputMethod(this.mTargetView, event);
        return DEBUG;
    }

    public boolean reportFullscreenMode(boolean enabled) {
        return true;
    }

    private void sendCurrentText() {
        if (this.mDummyMode) {
            Editable content = getEditable();
            if (content != null) {
                int N = content.length();
                if (N != 0) {
                    if (N == 1) {
                        if (this.mKeyCharacterMap == null) {
                            this.mKeyCharacterMap = KeyCharacterMap.load(-1);
                        }
                        char[] chars = new char[1];
                        content.getChars(0, 1, chars, 0);
                        KeyEvent[] events = this.mKeyCharacterMap.getEvents(chars);
                        if (events != null) {
                            for (KeyEvent sendKeyEvent : events) {
                                sendKeyEvent(sendKeyEvent);
                            }
                            content.clear();
                            return;
                        }
                    }
                    sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), content.toString(), -1, 0));
                    content.clear();
                }
            }
        }
    }

    private void ensureDefaultComposingSpans() {
        if (this.mDefaultComposingSpans == null) {
            Context context;
            if (this.mTargetView != null) {
                context = this.mTargetView.getContext();
            } else if (this.mIMM.mServedView != null) {
                context = this.mIMM.mServedView.getContext();
            } else {
                context = null;
            }
            if (context != null) {
                TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{R.attr.candidatesTextStyleSpans});
                CharSequence style = ta.getText(0);
                ta.recycle();
                if (style != null && (style instanceof Spanned)) {
                    this.mDefaultComposingSpans = ((Spanned) style).getSpans(0, style.length(), Object.class);
                }
            }
        }
    }

    private void replaceText(CharSequence text, int newCursorPosition, boolean composing) {
        Editable content = getEditable();
        if (content != null) {
            int tmp;
            beginBatchEdit();
            int a = getComposingSpanStart(content);
            int b = getComposingSpanEnd(content);
            if (b < a) {
                tmp = a;
                a = b;
                b = tmp;
            }
            if (a == -1 || b == -1) {
                a = Selection.getSelectionStart(content);
                b = Selection.getSelectionEnd(content);
                if (a < 0) {
                    a = 0;
                }
                if (b < 0) {
                    b = 0;
                }
                if (b < a) {
                    tmp = a;
                    a = b;
                    b = tmp;
                }
            } else {
                removeComposingSpans(content);
            }
            if (composing) {
                Spannable sp;
                if (text instanceof Spannable) {
                    sp = (Spannable) text;
                } else {
                    sp = new SpannableStringBuilder(text);
                    text = sp;
                    ensureDefaultComposingSpans();
                    if (this.mDefaultComposingSpans != null) {
                        for (Object span : this.mDefaultComposingSpans) {
                            sp.setSpan(span, 0, sp.length(), MetricsEvent.OVERVIEW_DISMISS);
                        }
                    }
                }
                setComposingSpans(sp);
            }
            if (newCursorPosition > 0) {
                newCursorPosition += b - 1;
            } else {
                newCursorPosition += a;
            }
            if (newCursorPosition < 0) {
                newCursorPosition = 0;
            }
            if (newCursorPosition > content.length()) {
                newCursorPosition = content.length();
            }
            Selection.setSelection(content, newCursorPosition);
            content.replace(a, b, text);
            endBatchEdit();
        }
    }
}
