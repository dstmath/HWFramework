package android.text.method;

import android.text.AutoText;
import android.text.Editable;
import android.text.NoCopySpan;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.TextKeyListener.Capitalize;
import android.util.SparseArray;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import com.android.internal.os.HwBootFail;
import com.android.internal.util.Protocol;

public class QwertyKeyListener extends BaseKeyListener {
    private static SparseArray<String> PICKER_SETS;
    private static QwertyKeyListener sFullKeyboardInstance;
    private static QwertyKeyListener[] sInstance;
    private Capitalize mAutoCap;
    private boolean mAutoText;
    private boolean mFullKeyboard;

    static class Replaced implements NoCopySpan {
        private char[] mText;

        public Replaced(char[] text) {
            this.mText = text;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.method.QwertyKeyListener.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.method.QwertyKeyListener.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.QwertyKeyListener.<clinit>():void");
    }

    private QwertyKeyListener(Capitalize cap, boolean autoText, boolean fullKeyboard) {
        this.mAutoCap = cap;
        this.mAutoText = autoText;
        this.mFullKeyboard = fullKeyboard;
    }

    public QwertyKeyListener(Capitalize cap, boolean autoText) {
        this(cap, autoText, false);
    }

    public static QwertyKeyListener getInstance(boolean autoText, Capitalize cap) {
        int off = (cap.ordinal() * 2) + (autoText ? 1 : 0);
        if (sInstance[off] == null) {
            sInstance[off] = new QwertyKeyListener(cap, autoText);
        }
        return sInstance[off];
    }

    public static QwertyKeyListener getInstanceForFullKeyboard() {
        if (sFullKeyboardInstance == null) {
            sFullKeyboardInstance = new QwertyKeyListener(Capitalize.NONE, false, true);
        }
        return sFullKeyboardInstance;
    }

    public int getInputType() {
        return BaseKeyListener.makeTextContentType(this.mAutoCap, this.mAutoText);
    }

    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        char c;
        int pref = 0;
        if (view != null) {
            pref = TextKeyListener.getInstance().getPrefs(view.getContext());
        }
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);
        if (selStart < 0 || selEnd < 0) {
            selEnd = 0;
            selStart = 0;
            Selection.setSelection(content, 0, 0);
        }
        int activeStart = content.getSpanStart(TextKeyListener.ACTIVE);
        int activeEnd = content.getSpanEnd(TextKeyListener.ACTIVE);
        int i = event.getUnicodeChar(MetaKeyKeyListener.getMetaState((CharSequence) content, event));
        if (!this.mFullKeyboard) {
            int count = event.getRepeatCount();
            if (count > 0 && selStart == selEnd && selStart > 0) {
                c = content.charAt(selStart - 1);
                if ((c == i || c == Character.toUpperCase(i)) && view != null && showCharacterPicker(view, content, c, false, count)) {
                    MetaKeyKeyListener.resetMetaState(content);
                    return true;
                }
            }
        }
        if (i == '\uef01') {
            if (view != null) {
                showCharacterPicker(view, content, KeyCharacterMap.PICKER_DIALOG_INPUT, true, 1);
            }
            MetaKeyKeyListener.resetMetaState(content);
            return true;
        }
        if (i == '\uef00') {
            int start;
            if (selStart == selEnd) {
                start = selEnd;
                while (start > 0 && selEnd - start < 4) {
                    if (Character.digit(content.charAt(start - 1), 16) < 0) {
                        break;
                    }
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
                i = ch;
            } else {
                i = 0;
            }
        }
        Replaced[] repl;
        if (i != 0) {
            boolean dead = false;
            if ((RtlSpacingHelper.UNDEFINED & i) != 0) {
                dead = true;
                i &= HwBootFail.STAGE_BOOT_SUCCESS;
            }
            if (activeStart == selStart && activeEnd == selEnd) {
                boolean replace = false;
                if ((selEnd - selStart) - 1 == 0) {
                    int composed = KeyEvent.getDeadChar(content.charAt(selStart), i);
                    if (composed != 0) {
                        i = composed;
                        replace = true;
                        dead = false;
                    }
                }
                if (!replace) {
                    Selection.setSelection(content, selEnd);
                    content.removeSpan(TextKeyListener.ACTIVE);
                    selStart = selEnd;
                }
            }
            if ((pref & 1) != 0 && Character.isLowerCase(i) && TextKeyListener.shouldCap(this.mAutoCap, content, selStart)) {
                int where = content.getSpanEnd(TextKeyListener.CAPPED);
                int flags = content.getSpanFlags(TextKeyListener.CAPPED);
                if (where == selStart && ((flags >> 16) & Protocol.MAX_MESSAGE) == i) {
                    content.removeSpan(TextKeyListener.CAPPED);
                } else {
                    flags = i << 16;
                    i = Character.toUpperCase(i);
                    if (selStart == 0) {
                        content.setSpan(TextKeyListener.CAPPED, 0, 0, flags | 17);
                    } else {
                        content.setSpan(TextKeyListener.CAPPED, selStart - 1, selStart, flags | 33);
                    }
                }
            }
            if (selStart != selEnd) {
                Selection.setSelection(content, selEnd);
            }
            content.setSpan(OLD_SEL_START, selStart, selStart, 17);
            content.replace(selStart, selEnd, String.valueOf((char) i));
            int oldStart = content.getSpanStart(OLD_SEL_START);
            selEnd = Selection.getSelectionEnd(content);
            if (oldStart < selEnd) {
                content.setSpan(TextKeyListener.LAST_TYPED, oldStart, selEnd, 33);
                if (dead) {
                    Selection.setSelection(content, oldStart, selEnd);
                    content.setSpan(TextKeyListener.ACTIVE, oldStart, selEnd, 33);
                }
            }
            MetaKeyKeyListener.adjustMetaAfterKeypress((Spannable) content);
            if ((pref & 2) != 0 && this.mAutoText && (i == 32 || i == 9 || i == 10 || i == 44 || i == 46 || i == 33 || i == 63 || i == 34 || Character.getType(i) == 22)) {
                if (content.getSpanEnd(TextKeyListener.INHIBIT_REPLACEMENT) != oldStart) {
                    int x = oldStart;
                    while (x > 0) {
                        c = content.charAt(x - 1);
                        if (c != DateFormat.QUOTE && !Character.isLetter(c)) {
                            break;
                        }
                        x--;
                    }
                    String rep = getReplacement(content, x, oldStart, view);
                    if (rep != null) {
                        repl = (Replaced[]) content.getSpans(0, content.length(), Replaced.class);
                        for (Object removeSpan : repl) {
                            content.removeSpan(removeSpan);
                        }
                        char[] orig = new char[(oldStart - x)];
                        TextUtils.getChars(content, x, oldStart, orig, 0);
                        content.setSpan(new Replaced(orig), x, oldStart, 33);
                        content.replace(x, oldStart, rep);
                    }
                }
            }
            if ((pref & 4) != 0 && this.mAutoText) {
                selEnd = Selection.getSelectionEnd(content);
                if (selEnd - 3 >= 0) {
                    if (content.charAt(selEnd - 1) == ' ') {
                        if (content.charAt(selEnd - 2) == ' ') {
                            c = content.charAt(selEnd - 3);
                            for (int j = selEnd - 3; j > 0 && (c == '\"' || Character.getType(c) == 22); j--) {
                                c = content.charAt(j - 1);
                            }
                            if (Character.isLetter(c) || Character.isDigit(c)) {
                                content.replace(selEnd - 2, selEnd - 1, ".");
                            }
                        }
                    }
                }
            }
            return true;
        }
        if (keyCode == 67 && ((event.hasNoModifiers() || event.hasModifiers(2)) && selStart == selEnd)) {
            int consider = 1;
            if (content.getSpanEnd(TextKeyListener.LAST_TYPED) == selStart) {
                if (content.charAt(selStart - 1) != '\n') {
                    consider = 2;
                }
            }
            repl = (Replaced[]) content.getSpans(selStart - consider, selStart, Replaced.class);
            if (repl.length > 0) {
                int st = content.getSpanStart(repl[0]);
                int en = content.getSpanEnd(repl[0]);
                String str = new String(repl[0].mText);
                content.removeSpan(repl[0]);
                if (selStart >= en) {
                    content.setSpan(TextKeyListener.INHIBIT_REPLACEMENT, en, en, 34);
                    content.replace(st, en, str);
                    en = content.getSpanStart(TextKeyListener.INHIBIT_REPLACEMENT);
                    if (en - 1 >= 0) {
                        content.setSpan(TextKeyListener.INHIBIT_REPLACEMENT, en - 1, en, 33);
                    } else {
                        content.removeSpan(TextKeyListener.INHIBIT_REPLACEMENT);
                    }
                    MetaKeyKeyListener.adjustMetaAfterKeypress((Spannable) content);
                    return true;
                }
                MetaKeyKeyListener.adjustMetaAfterKeypress((Spannable) content);
                return super.onKeyDown(view, content, keyCode, event);
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
        if (out.length() == len && TextUtils.regionMatches(src, start, out, 0, len)) {
            return null;
        }
        return out;
    }

    public static void markAsReplaced(Spannable content, int start, int end, String original) {
        Replaced[] repl = (Replaced[]) content.getSpans(0, content.length(), Replaced.class);
        for (Object removeSpan : repl) {
            content.removeSpan(removeSpan);
        }
        int len = original.length();
        char[] orig = new char[len];
        original.getChars(0, len, orig, 0);
        content.setSpan(new Replaced(orig), start, end, 33);
    }

    private boolean showCharacterPicker(View view, Editable content, char c, boolean insert, int count) {
        String set = (String) PICKER_SETS.get(c);
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
}
