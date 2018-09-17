package android.text.method;

import android.graphics.Paint;
import android.icu.lang.UCharacter;
import android.text.Editable;
import android.text.Emoji;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.TextKeyListener.Capitalize;
import android.text.style.ReplacementSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.telephony.RILConstants;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import javax.microedition.khronos.opengles.GL10;

public abstract class BaseKeyListener extends MetaKeyKeyListener implements KeyListener {
    private static final /* synthetic */ int[] -android-text-method-TextKeyListener$CapitalizeSwitchesValues = null;
    private static final int CARRIAGE_RETURN = 13;
    private static final int LINE_FEED = 10;
    static final Object OLD_SEL_START = null;
    @GuardedBy("mLock")
    static Paint sCachedPaint;
    private final Object mLock;

    private static /* synthetic */ int[] -getandroid-text-method-TextKeyListener$CapitalizeSwitchesValues() {
        if (-android-text-method-TextKeyListener$CapitalizeSwitchesValues != null) {
            return -android-text-method-TextKeyListener$CapitalizeSwitchesValues;
        }
        int[] iArr = new int[Capitalize.values().length];
        try {
            iArr[Capitalize.CHARACTERS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Capitalize.NONE.ordinal()] = 4;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Capitalize.SENTENCES.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Capitalize.WORDS.ordinal()] = 3;
        } catch (NoSuchFieldError e4) {
        }
        -android-text-method-TextKeyListener$CapitalizeSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.method.BaseKeyListener.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.method.BaseKeyListener.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.BaseKeyListener.<clinit>():void");
    }

    public BaseKeyListener() {
        this.mLock = new Object();
    }

    public boolean backspace(View view, Editable content, int keyCode, KeyEvent event) {
        return backspaceOrForwardDelete(view, content, keyCode, event, false);
    }

    public boolean forwardDelete(View view, Editable content, int keyCode, KeyEvent event) {
        return backspaceOrForwardDelete(view, content, keyCode, event, true);
    }

    private static boolean isVariationSelector(int codepoint) {
        return UCharacter.hasBinaryProperty(codepoint, 36);
    }

    private static int adjustReplacementSpan(CharSequence text, int offset, boolean moveToStart) {
        if (!(text instanceof Spanned)) {
            return offset;
        }
        ReplacementSpan[] spans = (ReplacementSpan[]) ((Spanned) text).getSpans(offset, offset, ReplacementSpan.class);
        for (int i = 0; i < spans.length; i++) {
            int start = ((Spanned) text).getSpanStart(spans[i]);
            int end = ((Spanned) text).getSpanEnd(spans[i]);
            if (start < offset && end > offset) {
                if (moveToStart) {
                    offset = start;
                } else {
                    offset = end;
                }
            }
        }
        return offset;
    }

    private static int getOffsetForBackspaceKey(CharSequence text, int offset) {
        if (offset <= 1) {
            return 0;
        }
        int deleteCharCount = 0;
        int lastSeenVSCharCount = 0;
        int state = 0;
        int tmpOffset = offset;
        do {
            int codePoint = Character.codePointBefore(text, tmpOffset);
            tmpOffset -= Character.charCount(codePoint);
            switch (state) {
                case HwCfgFilePolicy.GLOBAL /*0*/:
                    deleteCharCount = Character.charCount(codePoint);
                    if (codePoint != LINE_FEED) {
                        if (!isVariationSelector(codePoint)) {
                            if (!Emoji.isRegionalIndicatorSymbol(codePoint)) {
                                if (!Emoji.isEmojiModifier(codePoint)) {
                                    if (codePoint != Emoji.COMBINING_ENCLOSING_KEYCAP) {
                                        if (!Emoji.isEmoji(codePoint)) {
                                            state = 12;
                                            break;
                                        }
                                        state = 7;
                                        break;
                                    }
                                    state = 2;
                                    break;
                                }
                                state = 4;
                                break;
                            }
                            state = LINE_FEED;
                            break;
                        }
                        state = 6;
                        break;
                    }
                    state = 1;
                    break;
                case HwCfgFilePolicy.EMUI /*1*/:
                    if (codePoint == CARRIAGE_RETURN) {
                        deleteCharCount++;
                    }
                    break;
                case HwCfgFilePolicy.PC /*2*/:
                    if (!isVariationSelector(codePoint)) {
                        if (Emoji.isKeycapBase(codePoint)) {
                            deleteCharCount += Character.charCount(codePoint);
                        }
                        state = 12;
                        break;
                    }
                    lastSeenVSCharCount = Character.charCount(codePoint);
                    state = 3;
                    break;
                case HwCfgFilePolicy.BASE /*3*/:
                    if (Emoji.isKeycapBase(codePoint)) {
                        deleteCharCount += Character.charCount(codePoint) + lastSeenVSCharCount;
                    }
                    state = 12;
                    break;
                case HwCfgFilePolicy.CUST /*4*/:
                    if (!isVariationSelector(codePoint)) {
                        if (Emoji.isEmojiModifierBase(codePoint)) {
                            deleteCharCount += Character.charCount(codePoint);
                        }
                        state = 12;
                        break;
                    }
                    lastSeenVSCharCount = Character.charCount(codePoint);
                    state = 5;
                    break;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    if (Emoji.isEmojiModifierBase(codePoint)) {
                        deleteCharCount += Character.charCount(codePoint) + lastSeenVSCharCount;
                    }
                    state = 12;
                    break;
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                    if (!Emoji.isEmoji(codePoint)) {
                        if (!isVariationSelector(codePoint) && UCharacter.getCombiningClass(codePoint) == 0) {
                            deleteCharCount += Character.charCount(codePoint);
                        }
                        state = 12;
                        break;
                    }
                    deleteCharCount += Character.charCount(codePoint);
                    state = 7;
                    break;
                case HwCfgFilePolicy.CLOUD_APN /*7*/:
                    if (codePoint != Emoji.ZERO_WIDTH_JOINER) {
                        state = 12;
                        break;
                    }
                    state = 8;
                    break;
                case PGSdk.TYPE_VIDEO /*8*/:
                    if (!Emoji.isEmoji(codePoint)) {
                        if (!isVariationSelector(codePoint)) {
                            state = 12;
                            break;
                        }
                        lastSeenVSCharCount = Character.charCount(codePoint);
                        state = 9;
                        break;
                    }
                    deleteCharCount += Character.charCount(codePoint) + 1;
                    state = 7;
                    break;
                case PGSdk.TYPE_SCRLOCK /*9*/:
                    if (!Emoji.isEmoji(codePoint)) {
                        state = 12;
                        break;
                    }
                    deleteCharCount += (lastSeenVSCharCount + 1) + Character.charCount(codePoint);
                    lastSeenVSCharCount = 0;
                    state = 7;
                    break;
                case LINE_FEED /*10*/:
                    break;
                case PGSdk.TYPE_IM /*11*/:
                    if (!Emoji.isRegionalIndicatorSymbol(codePoint)) {
                        state = 12;
                        break;
                    }
                    deleteCharCount -= 2;
                    state = LINE_FEED;
                    break;
                default:
                    throw new IllegalArgumentException("state " + state + " is unknown");
            }
            if (Emoji.isRegionalIndicatorSymbol(codePoint)) {
                deleteCharCount += 2;
                state = 11;
            } else {
                state = 12;
            }
            if (tmpOffset > 0) {
            }
            return adjustReplacementSpan(text, offset - deleteCharCount, true);
        } while (state != 12);
        return adjustReplacementSpan(text, offset - deleteCharCount, true);
    }

    private static int getOffsetForForwardDeleteKey(CharSequence text, int offset, Paint paint) {
        int len = text.length();
        if (offset >= len - 1) {
            return len;
        }
        return adjustReplacementSpan(text, paint.getTextRunCursor(text, offset, len, 0, offset, 0), false);
    }

    private boolean backspaceOrForwardDelete(View view, Editable content, int keyCode, KeyEvent event, boolean isForwardDelete) {
        if (!KeyEvent.metaStateHasNoModifiers(event.getMetaState() & -28916)) {
            return false;
        }
        if (deleteSelection(view, content)) {
            return true;
        }
        boolean isCtrlActive = (event.getMetaState() & HwPerformance.PERF_EVENT_RAW_REQ) != 0;
        boolean isShiftActive = MetaKeyKeyListener.getMetaState(content, 1, event) == 1;
        boolean isAltActive = MetaKeyKeyListener.getMetaState(content, 2, event) == 1;
        if (isCtrlActive) {
            if (isAltActive || isShiftActive) {
                return false;
            }
            return deleteUntilWordBoundary(view, content, isForwardDelete);
        } else if (isAltActive && deleteLine(view, content)) {
            return true;
        } else {
            int end;
            int start = Selection.getSelectionEnd(content);
            if (isForwardDelete) {
                Paint paint;
                if (view instanceof TextView) {
                    paint = ((TextView) view).getPaint();
                } else {
                    synchronized (this.mLock) {
                        if (sCachedPaint == null) {
                            sCachedPaint = new Paint();
                        }
                        paint = sCachedPaint;
                    }
                }
                end = getOffsetForForwardDeleteKey(content, start, paint);
            } else {
                end = getOffsetForBackspaceKey(content, start);
            }
            if (start == end) {
                return false;
            }
            content.delete(Math.min(start, end), Math.max(start, end));
            return true;
        }
    }

    private boolean deleteUntilWordBoundary(View view, Editable content, boolean isForwardDelete) {
        int currentCursorOffset = Selection.getSelectionStart(content);
        if (currentCursorOffset != Selection.getSelectionEnd(content)) {
            return false;
        }
        if ((!isForwardDelete && currentCursorOffset == 0) || (isForwardDelete && currentCursorOffset == content.length())) {
            return false;
        }
        int deleteFrom;
        int deleteTo;
        WordIterator wordIterator = null;
        if (view instanceof TextView) {
            wordIterator = ((TextView) view).getWordIterator();
        }
        if (wordIterator == null) {
            wordIterator = new WordIterator();
        }
        if (isForwardDelete) {
            deleteFrom = currentCursorOffset;
            wordIterator.setCharSequence(content, currentCursorOffset, content.length());
            deleteTo = wordIterator.following(currentCursorOffset);
            if (deleteTo == -1) {
                deleteTo = content.length();
            }
        } else {
            deleteTo = currentCursorOffset;
            wordIterator.setCharSequence(content, 0, currentCursorOffset);
            deleteFrom = wordIterator.preceding(currentCursorOffset);
            if (deleteFrom == -1) {
                deleteFrom = 0;
            }
        }
        content.delete(deleteFrom, deleteTo);
        return true;
    }

    private boolean deleteSelection(View view, Editable content) {
        int selectionStart = Selection.getSelectionStart(content);
        int selectionEnd = Selection.getSelectionEnd(content);
        if (selectionEnd < selectionStart) {
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }
        if (selectionStart == selectionEnd) {
            return false;
        }
        content.delete(selectionStart, selectionEnd);
        return true;
    }

    private boolean deleteLine(View view, Editable content) {
        if (view instanceof TextView) {
            Layout layout = ((TextView) view).getLayout();
            if (layout != null) {
                int line = layout.getLineForOffset(Selection.getSelectionStart(content));
                int start = layout.getLineStart(line);
                int end = layout.getLineEnd(line);
                if (end != start) {
                    content.delete(start, end);
                    return true;
                }
            }
        }
        return false;
    }

    static int makeTextContentType(Capitalize caps, boolean autoText) {
        int contentType = 1;
        switch (-getandroid-text-method-TextKeyListener$CapitalizeSwitchesValues()[caps.ordinal()]) {
            case HwCfgFilePolicy.EMUI /*1*/:
                contentType = HwPerformance.PERF_EVENT_PROBE;
                break;
            case HwCfgFilePolicy.PC /*2*/:
                contentType = GL10.GL_LIGHT1;
                break;
            case HwCfgFilePolicy.BASE /*3*/:
                contentType = 8193;
                break;
        }
        if (autoText) {
            return contentType | AccessibilityNodeInfo.ACTION_PASTE;
        }
        return contentType;
    }

    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        boolean handled;
        switch (keyCode) {
            case RILConstants.RIL_REQUEST_STK_GET_PROFILE /*67*/:
                handled = backspace(view, content, keyCode, event);
                break;
            case LogPower.APP_PROCESS_EXIT /*112*/:
                handled = forwardDelete(view, content, keyCode, event);
                break;
            default:
                handled = false;
                break;
        }
        if (!handled) {
            return super.onKeyDown(view, content, keyCode, event);
        }
        MetaKeyKeyListener.adjustMetaAfterKeypress((Spannable) content);
        return true;
    }

    public boolean onKeyOther(View view, Editable content, KeyEvent event) {
        if (event.getAction() != 2 || event.getKeyCode() != 0) {
            return false;
        }
        int selectionStart = Selection.getSelectionStart(content);
        int selectionEnd = Selection.getSelectionEnd(content);
        if (selectionEnd < selectionStart) {
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }
        CharSequence text = event.getCharacters();
        if (text == null) {
            return false;
        }
        content.replace(selectionStart, selectionEnd, text);
        return true;
    }
}
