package android.text.method;

import android.text.AutoText;
import android.text.Editable;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.SparseArray;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;

public class QwertyKeyListener extends BaseKeyListener {
    private static SparseArray<String> PICKER_SETS = new SparseArray<>();
    private static QwertyKeyListener sFullKeyboardInstance;
    private static QwertyKeyListener[] sInstance = new QwertyKeyListener[(TextKeyListener.Capitalize.values().length * 2)];
    private TextKeyListener.Capitalize mAutoCap;
    private boolean mAutoText;
    private boolean mFullKeyboard;

    static {
        PICKER_SETS.put(65, "ÀÁÂÄÆÃÅĄĀ");
        PICKER_SETS.put(67, "ÇĆČ");
        PICKER_SETS.put(68, "Ď");
        PICKER_SETS.put(69, "ÈÉÊËĘĚĒ");
        PICKER_SETS.put(71, "Ğ");
        PICKER_SETS.put(76, "Ł");
        PICKER_SETS.put(73, "ÌÍÎÏĪİ");
        PICKER_SETS.put(78, "ÑŃŇ");
        PICKER_SETS.put(79, "ØŒÕÒÓÔÖŌ");
        PICKER_SETS.put(82, "Ř");
        PICKER_SETS.put(83, "ŚŠŞ");
        PICKER_SETS.put(84, "Ť");
        PICKER_SETS.put(85, "ÙÚÛÜŮŪ");
        PICKER_SETS.put(89, "ÝŸ");
        PICKER_SETS.put(90, "ŹŻŽ");
        PICKER_SETS.put(97, "àáâäæãåąā");
        PICKER_SETS.put(99, "çćč");
        PICKER_SETS.put(100, "ď");
        PICKER_SETS.put(101, "èéêëęěē");
        PICKER_SETS.put(103, "ğ");
        PICKER_SETS.put(105, "ìíîïīı");
        PICKER_SETS.put(108, "ł");
        PICKER_SETS.put(110, "ñńň");
        PICKER_SETS.put(111, "øœõòóôöō");
        PICKER_SETS.put(114, "ř");
        PICKER_SETS.put(115, "§ßśšş");
        PICKER_SETS.put(116, "ť");
        PICKER_SETS.put(117, "ùúûüůū");
        PICKER_SETS.put(121, "ýÿ");
        PICKER_SETS.put(122, "źżž");
        PICKER_SETS.put(61185, "…¥•®©±[]{}\\|");
        PICKER_SETS.put(47, "\\");
        PICKER_SETS.put(49, "¹½⅓¼⅛");
        PICKER_SETS.put(50, "²⅔");
        PICKER_SETS.put(51, "³¾⅜");
        PICKER_SETS.put(52, "⁴");
        PICKER_SETS.put(53, "⅝");
        PICKER_SETS.put(55, "⅞");
        PICKER_SETS.put(48, "ⁿ∅");
        PICKER_SETS.put(36, "¢£€¥₣₤₱");
        PICKER_SETS.put(37, "‰");
        PICKER_SETS.put(42, "†‡");
        PICKER_SETS.put(45, "–—");
        PICKER_SETS.put(43, "±");
        PICKER_SETS.put(40, "[{<");
        PICKER_SETS.put(41, "]}>");
        PICKER_SETS.put(33, "¡");
        PICKER_SETS.put(34, "“”«»˝");
        PICKER_SETS.put(63, "¿");
        PICKER_SETS.put(44, "‚„");
        PICKER_SETS.put(61, "≠≈∞");
        PICKER_SETS.put(60, "≤«‹");
        PICKER_SETS.put(62, "≥»›");
    }

    private QwertyKeyListener(TextKeyListener.Capitalize cap, boolean autoText, boolean fullKeyboard) {
        this.mAutoCap = cap;
        this.mAutoText = autoText;
        this.mFullKeyboard = fullKeyboard;
    }

    public QwertyKeyListener(TextKeyListener.Capitalize cap, boolean autoText) {
        this(cap, autoText, false);
    }

    public static QwertyKeyListener getInstance(boolean autoText, TextKeyListener.Capitalize cap) {
        int off = (cap.ordinal() * 2) + (autoText ? 1 : 0);
        QwertyKeyListener[] qwertyKeyListenerArr = sInstance;
        if (qwertyKeyListenerArr[off] == null) {
            qwertyKeyListenerArr[off] = new QwertyKeyListener(cap, autoText);
        }
        return sInstance[off];
    }

    public static QwertyKeyListener getInstanceForFullKeyboard() {
        if (sFullKeyboardInstance == null) {
            sFullKeyboardInstance = new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false, true);
        }
        return sFullKeyboardInstance;
    }

    @Override // android.text.method.KeyListener
    public int getInputType() {
        return makeTextContentType(this.mAutoCap, this.mAutoText);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:161:0x0297, code lost:
        if (r25.hasModifiers(2) != false) goto L_0x029d;
     */
    @Override // android.text.method.KeyListener, android.text.method.MetaKeyKeyListener, android.text.method.BaseKeyListener
    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        int selEnd;
        int selStart;
        int i;
        int activeEnd;
        int i2;
        int selEnd2;
        Replaced[] repl;
        int composed;
        int start;
        int pref = view != null ? TextKeyListener.getInstance().getPrefs(view.getContext()) : 0;
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        int selStart2 = Math.min(a, b);
        int selEnd3 = Math.max(a, b);
        if (selStart2 < 0 || selEnd3 < 0) {
            Selection.setSelection(content, 0, 0);
            selStart = 0;
            selEnd = 0;
        } else {
            selStart = selStart2;
            selEnd = selEnd3;
        }
        int activeStart = content.getSpanStart(TextKeyListener.ACTIVE);
        int activeEnd2 = content.getSpanEnd(TextKeyListener.ACTIVE);
        int i3 = event.getUnicodeChar(getMetaState(content, event));
        if (!this.mFullKeyboard) {
            int count = event.getRepeatCount();
            if (count <= 0 || selStart != selEnd || selStart <= 0) {
                i = i3;
                activeEnd = activeEnd2;
            } else {
                char c = content.charAt(selStart - 1);
                if (c != i3 && c != Character.toUpperCase(i3)) {
                    i = i3;
                    activeEnd = activeEnd2;
                } else if (view != null) {
                    i = i3;
                    activeEnd = activeEnd2;
                    if (showCharacterPicker(view, content, c, false, count)) {
                        resetMetaState(content);
                        return true;
                    }
                } else {
                    i = i3;
                    activeEnd = activeEnd2;
                }
            }
        } else {
            i = i3;
            activeEnd = activeEnd2;
        }
        if (i == 61185) {
            if (view != null) {
                showCharacterPicker(view, content, KeyCharacterMap.PICKER_DIALOG_INPUT, true, 1);
            }
            resetMetaState(content);
            return true;
        }
        if (i == 61184) {
            if (selStart == selEnd) {
                start = selEnd;
                while (start > 0 && selEnd - start < 4 && Character.digit(content.charAt(start - 1), 16) >= 0) {
                    start--;
                }
            } else {
                start = selStart;
            }
            int ch = -1;
            try {
                ch = Integer.parseInt(TextUtils.substring(content, start, selEnd), 16);
            } catch (NumberFormatException e) {
            }
            if (ch >= 0) {
                selStart = start;
                Selection.setSelection(content, selStart, selEnd);
                i2 = ch;
            } else {
                i2 = 0;
            }
        } else {
            i2 = i;
        }
        if (i2 != 0) {
            boolean dead = false;
            if ((Integer.MIN_VALUE & i2) != 0) {
                dead = true;
                i2 &= Integer.MAX_VALUE;
            }
            if (activeStart == selStart && activeEnd == selEnd) {
                boolean replace = false;
                replace = false;
                if ((selEnd - selStart) - 1 == 0 && (composed = KeyEvent.getDeadChar(content.charAt(selStart), i2)) != 0) {
                    i2 = composed;
                    replace = true;
                    dead = false;
                }
                if (!replace) {
                    Selection.setSelection(content, selEnd);
                    content.removeSpan(TextKeyListener.ACTIVE);
                    selStart = selEnd;
                }
            }
            if ((pref & 1) != 0 && Character.isLowerCase(i2) && TextKeyListener.shouldCap(this.mAutoCap, content, selStart)) {
                int where = content.getSpanEnd(TextKeyListener.CAPPED);
                int flags = content.getSpanFlags(TextKeyListener.CAPPED);
                if (where == selStart && ((flags >> 16) & 65535) == i2) {
                    content.removeSpan(TextKeyListener.CAPPED);
                } else {
                    int flags2 = i2 << 16;
                    i2 = Character.toUpperCase(i2);
                    if (selStart == 0) {
                        content.setSpan(TextKeyListener.CAPPED, 0, 0, flags2 | 17);
                    } else {
                        content.setSpan(TextKeyListener.CAPPED, selStart - 1, selStart, flags2 | 33);
                    }
                }
            }
            if (selStart != selEnd) {
                Selection.setSelection(content, selEnd);
            }
            content.setSpan(OLD_SEL_START, selStart, selStart, 17);
            content.replace(selStart, selEnd, String.valueOf((char) i2));
            int oldStart = content.getSpanStart(OLD_SEL_START);
            int selEnd4 = Selection.getSelectionEnd(content);
            if (oldStart < selEnd4) {
                content.setSpan(TextKeyListener.LAST_TYPED, oldStart, selEnd4, 33);
                if (dead) {
                    Selection.setSelection(content, oldStart, selEnd4);
                    content.setSpan(TextKeyListener.ACTIVE, oldStart, selEnd4, 33);
                }
            }
            adjustMetaAfterKeypress(content);
            if ((pref & 2) == 0 || !this.mAutoText) {
                selEnd2 = selEnd4;
            } else if (i2 != 32 && i2 != 9 && i2 != 10 && i2 != 44 && i2 != 46 && i2 != 33 && i2 != 63 && i2 != 34 && Character.getType(i2) != 22) {
                selEnd2 = selEnd4;
            } else if (content.getSpanEnd(TextKeyListener.INHIBIT_REPLACEMENT) != oldStart) {
                int x = oldStart;
                while (x > 0) {
                    char c2 = content.charAt(x - 1);
                    if (!(c2 == '\'' || Character.isLetter(c2))) {
                        break;
                    }
                    x--;
                }
                String rep = getReplacement(content, x, oldStart, view);
                if (rep != null) {
                    for (Replaced replaced : (Replaced[]) content.getSpans(0, content.length(), Replaced.class)) {
                        content.removeSpan(replaced);
                    }
                    char[] orig = new char[(oldStart - x)];
                    TextUtils.getChars(content, x, oldStart, orig, 0);
                    selEnd2 = selEnd4;
                    content.setSpan(new Replaced(orig), x, oldStart, 33);
                    content.replace(x, oldStart, rep);
                } else {
                    selEnd2 = selEnd4;
                }
            } else {
                selEnd2 = selEnd4;
            }
            if ((pref & 4) != 0 && this.mAutoText) {
                int selEnd5 = Selection.getSelectionEnd(content);
                if (selEnd5 - 3 >= 0 && content.charAt(selEnd5 - 1) == ' ' && content.charAt(selEnd5 - 2) == ' ') {
                    char c3 = content.charAt(selEnd5 - 3);
                    for (int j = selEnd5 - 3; j > 0; j--) {
                        if (c3 != '\"') {
                            if (Character.getType(c3) != 22) {
                                break;
                            }
                        }
                        c3 = content.charAt(j - 1);
                    }
                    if (Character.isLetter(c3) || Character.isDigit(c3)) {
                        content.replace(selEnd5 - 2, selEnd5 - 1, ".");
                    }
                }
            }
            return true;
        }
        if (keyCode == 67) {
            if (!event.hasNoModifiers()) {
            }
            if (selStart == selEnd) {
                int consider = 1;
                consider = 1;
                if (content.getSpanEnd(TextKeyListener.LAST_TYPED) == selStart && content.charAt(selStart - 1) != '\n') {
                    consider = 2;
                }
                Replaced[] repl2 = (Replaced[]) content.getSpans(selStart - consider, selStart, Replaced.class);
                if (repl2.length > 0) {
                    int st = content.getSpanStart(repl2[0]);
                    int en = content.getSpanEnd(repl2[0]);
                    String old = new String(repl2[0].mText);
                    content.removeSpan(repl2[0]);
                    if (selStart >= en) {
                        content.setSpan(TextKeyListener.INHIBIT_REPLACEMENT, en, en, 34);
                        content.replace(st, en, old);
                        int en2 = content.getSpanStart(TextKeyListener.INHIBIT_REPLACEMENT);
                        if (en2 - 1 >= 0) {
                            content.setSpan(TextKeyListener.INHIBIT_REPLACEMENT, en2 - 1, en2, 33);
                        } else {
                            content.removeSpan(TextKeyListener.INHIBIT_REPLACEMENT);
                        }
                        adjustMetaAfterKeypress(content);
                        return true;
                    }
                    adjustMetaAfterKeypress(content);
                    return super.onKeyDown(view, content, keyCode, event);
                }
            }
        }
        return super.onKeyDown(view, content, keyCode, event);
    }

    private String getReplacement(CharSequence src, int start, int end, View view) {
        String out;
        int len = end - start;
        boolean changecase = false;
        String replacement = AutoText.get(src, start, end, view);
        if (replacement == null) {
            replacement = AutoText.get(TextUtils.substring(src, start, end).toLowerCase(), 0, end - start, view);
            changecase = true;
            if (replacement == null) {
                return null;
            }
        }
        int caps = 0;
        if (changecase) {
            for (int j = start; j < end; j++) {
                if (Character.isUpperCase(src.charAt(j))) {
                    caps++;
                }
            }
        }
        if (caps == 0) {
            out = replacement;
        } else if (caps == 1) {
            out = toTitleCase(replacement);
        } else if (caps == len) {
            out = replacement.toUpperCase();
        } else {
            out = toTitleCase(replacement);
        }
        if (out.length() != len || !TextUtils.regionMatches(src, start, out, 0, len)) {
            return out;
        }
        return null;
    }

    public static void markAsReplaced(Spannable content, int start, int end, String original) {
        Replaced[] repl;
        for (Replaced replaced : (Replaced[]) content.getSpans(0, content.length(), Replaced.class)) {
            content.removeSpan(replaced);
        }
        int len = original.length();
        char[] orig = new char[len];
        original.getChars(0, len, orig, 0);
        content.setSpan(new Replaced(orig), start, end, 33);
    }

    private boolean showCharacterPicker(View view, Editable content, char c, boolean insert, int count) {
        String set = PICKER_SETS.get(c);
        if (set == null) {
            return false;
        }
        if (count == 1) {
            new CharacterPickerDialog(view.getContext(), view, content, set, insert).show();
        }
        return true;
    }

    private static String toTitleCase(String src) {
        return Character.toUpperCase(src.charAt(0)) + src.substring(1);
    }

    /* access modifiers changed from: package-private */
    public static class Replaced implements NoCopySpan {
        private char[] mText;

        public Replaced(char[] text) {
            this.mText = text;
        }
    }
}
