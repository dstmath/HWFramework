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

    static class Replaced implements NoCopySpan {
        /* access modifiers changed from: private */
        public char[] mText;

        public Replaced(char[] text) {
            this.mText = text;
        }
    }

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
        int off = (cap.ordinal() * 2) + (autoText);
        if (sInstance[off] == null) {
            sInstance[off] = new QwertyKeyListener(cap, autoText);
        }
        return sInstance[off];
    }

    public static QwertyKeyListener getInstanceForFullKeyboard() {
        if (sFullKeyboardInstance == null) {
            sFullKeyboardInstance = new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false, true);
        }
        return sFullKeyboardInstance;
    }

    public int getInputType() {
        return makeTextContentType(this.mAutoCap, this.mAutoText);
    }

    /* JADX WARNING: Removed duplicated region for block: B:138:0x023d  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x008c  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x009d  */
    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        int i;
        int activeEnd;
        int i2;
        int selEnd;
        int j;
        int start;
        View view2 = view;
        Editable editable = content;
        KeyEvent keyEvent = event;
        int pref = 0;
        if (view2 != null) {
            pref = TextKeyListener.getInstance().getPrefs(view.getContext());
        }
        int pref2 = pref;
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        int selStart = Math.min(a, b);
        int selEnd2 = Math.max(a, b);
        if (selStart < 0 || selEnd2 < 0) {
            selEnd2 = 0;
            selStart = 0;
            Selection.setSelection(editable, 0, 0);
        }
        int selStart2 = selStart;
        int selEnd3 = selEnd2;
        int activeStart = editable.getSpanStart(TextKeyListener.ACTIVE);
        int activeEnd2 = editable.getSpanEnd(TextKeyListener.ACTIVE);
        int i3 = keyEvent.getUnicodeChar(getMetaState((CharSequence) editable, keyEvent));
        if (!this.mFullKeyboard) {
            int count = event.getRepeatCount();
            if (count > 0 && selStart2 == selEnd3 && selStart2 > 0) {
                char c = editable.charAt(selStart2 - 1);
                if (c != i3 && c != Character.toUpperCase(i3)) {
                    i = i3;
                    activeEnd = activeEnd2;
                    if (i == 61185) {
                    }
                } else if (view2 != null) {
                    char c2 = c;
                    i = i3;
                    activeEnd = activeEnd2;
                    if (showCharacterPicker(view2, editable, c, false, count)) {
                        resetMetaState(content);
                        return true;
                    }
                    if (i == 61185) {
                        if (view2 != null) {
                            showCharacterPicker(view2, editable, KeyCharacterMap.PICKER_DIALOG_INPUT, true, 1);
                        }
                        resetMetaState(content);
                        return true;
                    }
                    if (i == 61184) {
                        if (selStart2 == selEnd3) {
                            start = selEnd3;
                            while (start > 0 && selEnd3 - start < 4 && Character.digit(editable.charAt(start - 1), 16) >= 0) {
                                start--;
                            }
                        } else {
                            start = selStart2;
                        }
                        int ch = -1;
                        try {
                            ch = Integer.parseInt(TextUtils.substring(editable, start, selEnd3), 16);
                        } catch (NumberFormatException e) {
                        }
                        if (ch >= 0) {
                            selStart2 = start;
                            Selection.setSelection(editable, selStart2, selEnd3);
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
                        if (activeStart == selStart2 && activeEnd == selEnd3) {
                            boolean replace = false;
                            if ((selEnd3 - selStart2) - 1 == 0) {
                                int composed = KeyEvent.getDeadChar(editable.charAt(selStart2), i2);
                                if (composed != 0) {
                                    i2 = composed;
                                    replace = true;
                                    dead = false;
                                }
                            }
                            if (!replace) {
                                Selection.setSelection(editable, selEnd3);
                                editable.removeSpan(TextKeyListener.ACTIVE);
                                selStart2 = selEnd3;
                            }
                        }
                        if ((pref2 != false && true) && Character.isLowerCase(i2) && TextKeyListener.shouldCap(this.mAutoCap, editable, selStart2)) {
                            int where = editable.getSpanEnd(TextKeyListener.CAPPED);
                            int flags = editable.getSpanFlags(TextKeyListener.CAPPED);
                            if (where == selStart2 && ((flags >> 16) & 65535) == i2) {
                                editable.removeSpan(TextKeyListener.CAPPED);
                            } else {
                                int flags2 = i2 << 16;
                                i2 = Character.toUpperCase(i2);
                                if (selStart2 == 0) {
                                    editable.setSpan(TextKeyListener.CAPPED, 0, 0, 17 | flags2);
                                } else {
                                    editable.setSpan(TextKeyListener.CAPPED, selStart2 - 1, selStart2, 33 | flags2);
                                }
                            }
                        }
                        if (selStart2 != selEnd3) {
                            Selection.setSelection(editable, selEnd3);
                        }
                        editable.setSpan(OLD_SEL_START, selStart2, selStart2, 17);
                        editable.replace(selStart2, selEnd3, String.valueOf((char) i2));
                        int oldStart = editable.getSpanStart(OLD_SEL_START);
                        int selEnd4 = Selection.getSelectionEnd(content);
                        if (oldStart < selEnd4) {
                            editable.setSpan(TextKeyListener.LAST_TYPED, oldStart, selEnd4, 33);
                            if (dead) {
                                Selection.setSelection(editable, oldStart, selEnd4);
                                editable.setSpan(TextKeyListener.ACTIVE, oldStart, selEnd4, 33);
                            }
                        }
                        adjustMetaAfterKeypress((Spannable) content);
                        if ((pref2 & 2) != 0 && this.mAutoText) {
                            if (i2 != 32 && i2 != 9 && i2 != 10 && i2 != 44 && i2 != 46 && i2 != 33 && i2 != 63 && i2 != 34 && Character.getType(i2) != 22) {
                                int i4 = selEnd4;
                                selEnd = Selection.getSelectionEnd(content);
                                char c3 = editable.charAt(selEnd - 3);
                                while (j > 0) {
                                }
                                editable.replace(selEnd - 2, selEnd - 1, ".");
                                int i5 = selEnd;
                                return true;
                            } else if (editable.getSpanEnd(TextKeyListener.INHIBIT_REPLACEMENT) != oldStart) {
                                int x = oldStart;
                                while (x > 0) {
                                    char c4 = editable.charAt(x - 1);
                                    if (c4 != '\'' && !Character.isLetter(c4)) {
                                        break;
                                    }
                                    x--;
                                }
                                String rep = getReplacement(editable, x, oldStart, view2);
                                if (rep != null) {
                                    Replaced[] repl = (Replaced[]) editable.getSpans(0, content.length(), Replaced.class);
                                    for (Replaced removeSpan : repl) {
                                        editable.removeSpan(removeSpan);
                                    }
                                    char[] orig = new char[(oldStart - x)];
                                    TextUtils.getChars(editable, x, oldStart, orig, 0);
                                    int i6 = selEnd4;
                                    editable.setSpan(new Replaced(orig), x, oldStart, 33);
                                    editable.replace(x, oldStart, rep);
                                    if ((pref2 & 4) != 0 && this.mAutoText) {
                                        selEnd = Selection.getSelectionEnd(content);
                                        if (selEnd - 3 >= 0 && editable.charAt(selEnd - 1) == ' ' && editable.charAt(selEnd - 2) == ' ') {
                                            char c32 = editable.charAt(selEnd - 3);
                                            for (j = selEnd - 3; j > 0; j--) {
                                                if (c32 != '\"') {
                                                    if (Character.getType(c32) != 22) {
                                                        break;
                                                    }
                                                }
                                                c32 = editable.charAt(j - 1);
                                            }
                                            if (Character.isLetter(c32) != 0 || Character.isDigit(c32)) {
                                                editable.replace(selEnd - 2, selEnd - 1, ".");
                                            }
                                        }
                                        int i52 = selEnd;
                                    }
                                    return true;
                                }
                            }
                        }
                        selEnd = Selection.getSelectionEnd(content);
                        char c322 = editable.charAt(selEnd - 3);
                        while (j > 0) {
                        }
                        editable.replace(selEnd - 2, selEnd - 1, ".");
                        int i522 = selEnd;
                        return true;
                    }
                    if (keyCode == 67) {
                        int activeEnd3 = activeEnd;
                        KeyEvent keyEvent2 = event;
                        if (event.hasNoModifiers() || keyEvent2.hasModifiers(2)) {
                            if (selStart2 == selEnd3) {
                                int consider = 1;
                                if (editable.getSpanEnd(TextKeyListener.LAST_TYPED) == selStart2 && editable.charAt(selStart2 - 1) != 10) {
                                    consider = 2;
                                }
                                Replaced[] repl2 = (Replaced[]) editable.getSpans(selStart2 - consider, selStart2, Replaced.class);
                                if (repl2.length > 0) {
                                    int st = editable.getSpanStart(repl2[0]);
                                    int en = editable.getSpanEnd(repl2[0]);
                                    int i7 = consider;
                                    int i8 = activeEnd3;
                                    String old = new String(repl2[0].mText);
                                    editable.removeSpan(repl2[0]);
                                    if (selStart2 >= en) {
                                        editable.setSpan(TextKeyListener.INHIBIT_REPLACEMENT, en, en, 34);
                                        editable.replace(st, en, old);
                                        if (editable.getSpanStart(TextKeyListener.INHIBIT_REPLACEMENT) - 1 >= 0) {
                                            String str = old;
                                            editable.setSpan(TextKeyListener.INHIBIT_REPLACEMENT, editable.getSpanStart(TextKeyListener.INHIBIT_REPLACEMENT) - 1, editable.getSpanStart(TextKeyListener.INHIBIT_REPLACEMENT), 33);
                                        } else {
                                            editable.removeSpan(TextKeyListener.INHIBIT_REPLACEMENT);
                                        }
                                        adjustMetaAfterKeypress((Spannable) content);
                                        return true;
                                    }
                                    adjustMetaAfterKeypress((Spannable) content);
                                    return super.onKeyDown(view, content, keyCode, event);
                                }
                            }
                        } else {
                            int i9 = activeEnd3;
                        }
                    } else {
                        KeyEvent keyEvent3 = event;
                    }
                    return super.onKeyDown(view, content, keyCode, event);
                }
            }
        }
        i = i3;
        activeEnd = activeEnd2;
        if (i == 61185) {
        }
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
        int j = 0;
        if (changecase) {
            int caps = 0;
            for (int j2 = start; j2 < end; j2++) {
                if (Character.isUpperCase(src.charAt(j2))) {
                    caps++;
                }
            }
            j = caps;
        }
        if (j == 0) {
            out = replacement;
        } else if (j == 1) {
            out = toTitleCase(replacement);
        } else if (j == len) {
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
        Replaced[] repl = (Replaced[]) content.getSpans(0, content.length(), Replaced.class);
        for (Replaced removeSpan : repl) {
            content.removeSpan(removeSpan);
        }
        int a = original.length();
        char[] orig = new char[a];
        original.getChars(0, a, orig, 0);
        content.setSpan(new Replaced(orig), start, end, 33);
    }

    private boolean showCharacterPicker(View view, Editable content, char c, boolean insert, int count) {
        String set = PICKER_SETS.get(c);
        if (set == null) {
            return false;
        }
        if (count == 1) {
            CharacterPickerDialog characterPickerDialog = new CharacterPickerDialog(view.getContext(), view, content, set, insert);
            characterPickerDialog.show();
        }
        return true;
    }

    private static String toTitleCase(String src) {
        return Character.toUpperCase(src.charAt(0)) + src.substring(1);
    }
}
