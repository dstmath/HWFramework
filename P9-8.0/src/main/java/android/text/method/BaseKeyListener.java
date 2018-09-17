package android.text.method;

import android.graphics.Paint;
import android.icu.lang.UCharacter;
import android.text.Editable;
import android.text.Emoji;
import android.text.Layout;
import android.text.NoCopySpan.Concrete;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.TextKeyListener.Capitalize;
import android.text.style.ReplacementSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.android.internal.annotations.GuardedBy;
import javax.microedition.khronos.opengles.GL10;

public abstract class BaseKeyListener extends MetaKeyKeyListener implements KeyListener {
    private static final /* synthetic */ int[] -android-text-method-TextKeyListener$CapitalizeSwitchesValues = null;
    private static final int CARRIAGE_RETURN = 13;
    private static final int LINE_FEED = 10;
    static final Object OLD_SEL_START = new Concrete();
    @GuardedBy("mLock")
    static Paint sCachedPaint = null;
    private final Object mLock = new Object();

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
                case 0:
                    deleteCharCount = Character.charCount(codePoint);
                    if (codePoint != 10) {
                        if (!isVariationSelector(codePoint)) {
                            if (!Emoji.isRegionalIndicatorSymbol(codePoint)) {
                                if (!Emoji.isEmojiModifier(codePoint)) {
                                    if (codePoint != Emoji.COMBINING_ENCLOSING_KEYCAP) {
                                        if (!Emoji.isEmoji(codePoint)) {
                                            if (codePoint != Emoji.CANCEL_TAG) {
                                                state = 13;
                                                break;
                                            }
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
                            state = 10;
                            break;
                        }
                        state = 6;
                        break;
                    }
                    state = 1;
                    break;
                case 1:
                    if (codePoint == 13) {
                        deleteCharCount++;
                    }
                    state = 13;
                    break;
                case 2:
                    if (!isVariationSelector(codePoint)) {
                        if (Emoji.isKeycapBase(codePoint)) {
                            deleteCharCount += Character.charCount(codePoint);
                        }
                        state = 13;
                        break;
                    }
                    lastSeenVSCharCount = Character.charCount(codePoint);
                    state = 3;
                    break;
                case 3:
                    if (Emoji.isKeycapBase(codePoint)) {
                        deleteCharCount += Character.charCount(codePoint) + lastSeenVSCharCount;
                    }
                    state = 13;
                    break;
                case 4:
                    if (!isVariationSelector(codePoint)) {
                        if (Emoji.isEmojiModifierBase(codePoint)) {
                            deleteCharCount += Character.charCount(codePoint);
                        }
                        state = 13;
                        break;
                    }
                    lastSeenVSCharCount = Character.charCount(codePoint);
                    state = 5;
                    break;
                case 5:
                    if (Emoji.isEmojiModifierBase(codePoint)) {
                        deleteCharCount += Character.charCount(codePoint) + lastSeenVSCharCount;
                    }
                    state = 13;
                    break;
                case 6:
                    if (!Emoji.isEmoji(codePoint)) {
                        if (!isVariationSelector(codePoint) && UCharacter.getCombiningClass(codePoint) == 0) {
                            deleteCharCount += Character.charCount(codePoint);
                        }
                        state = 13;
                        break;
                    }
                    deleteCharCount += Character.charCount(codePoint);
                    state = 7;
                    break;
                case 7:
                    if (codePoint != Emoji.ZERO_WIDTH_JOINER) {
                        state = 13;
                        break;
                    }
                    state = 8;
                    break;
                case 8:
                    if (!Emoji.isEmoji(codePoint)) {
                        if (!isVariationSelector(codePoint)) {
                            state = 13;
                            break;
                        }
                        lastSeenVSCharCount = Character.charCount(codePoint);
                        state = 9;
                        break;
                    }
                    deleteCharCount += Character.charCount(codePoint) + 1;
                    if (!Emoji.isEmojiModifier(codePoint)) {
                        state = 7;
                        break;
                    }
                    state = 4;
                    break;
                case 9:
                    if (!Emoji.isEmoji(codePoint)) {
                        state = 13;
                        break;
                    }
                    deleteCharCount += (lastSeenVSCharCount + 1) + Character.charCount(codePoint);
                    lastSeenVSCharCount = 0;
                    state = 7;
                    break;
                case 10:
                    if (!Emoji.isRegionalIndicatorSymbol(codePoint)) {
                        state = 13;
                        break;
                    }
                    deleteCharCount += 2;
                    state = 11;
                    break;
                case 11:
                    if (!Emoji.isRegionalIndicatorSymbol(codePoint)) {
                        state = 13;
                        break;
                    }
                    deleteCharCount -= 2;
                    state = 10;
                    break;
                case 12:
                    if (!Emoji.isTagSpecChar(codePoint)) {
                        if (!Emoji.isEmoji(codePoint)) {
                            deleteCharCount = 2;
                            state = 13;
                            break;
                        }
                        deleteCharCount += Character.charCount(codePoint);
                        state = 13;
                        break;
                    }
                    deleteCharCount += 2;
                    break;
                default:
                    throw new IllegalArgumentException("state " + state + " is unknown");
            }
            if (tmpOffset > 0) {
            }
            return adjustReplacementSpan(text, offset - deleteCharCount, true);
        } while (state != 13);
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
        boolean isCtrlActive = (event.getMetaState() & 4096) != 0;
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
            case 1:
                contentType = 4097;
                break;
            case 2:
                contentType = GL10.GL_LIGHT1;
                break;
            case 3:
                contentType = 8193;
                break;
        }
        if (autoText) {
            return contentType | 32768;
        }
        return contentType;
    }

    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        boolean handled;
        switch (keyCode) {
            case 67:
                handled = backspace(view, content, keyCode, event);
                break;
            case 112:
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
