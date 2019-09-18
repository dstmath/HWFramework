package android.text.method;

import android.icu.text.DecimalFormatSymbols;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import java.util.Collection;
import java.util.Locale;
import libcore.icu.LocaleData;

public abstract class NumberKeyListener extends BaseKeyListener implements InputFilter {
    private static final String DATE_TIME_FORMAT_SYMBOLS = "GyYuUrQqMLlwWdDFgEecabBhHKkjJCmsSAzZOvVXx";
    private static final char SINGLE_QUOTE = '\'';

    /* access modifiers changed from: protected */
    public abstract char[] getAcceptedChars();

    /* access modifiers changed from: protected */
    public int lookup(KeyEvent event, Spannable content) {
        return event.getMatch(getAcceptedChars(), getMetaState((CharSequence) content, event));
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        char[] accept = getAcceptedChars();
        int i = start;
        while (i < end && ok(accept, source.charAt(i))) {
            i++;
        }
        if (i == end) {
            return null;
        }
        if (end - start == 1) {
            return "";
        }
        SpannableStringBuilder filtered = new SpannableStringBuilder(source, start, end);
        int i2 = i - start;
        int end2 = end - start;
        int i3 = end2 - start;
        for (int j = end2 - 1; j >= i2; j--) {
            if (!ok(accept, source.charAt(j))) {
                filtered.delete(j, j + 1);
            }
        }
        return filtered;
    }

    protected static boolean ok(char[] accept, char c) {
        for (int i = accept.length - 1; i >= 0; i--) {
            if (accept[i] == c) {
                return true;
            }
        }
        return false;
    }

    public boolean onKeyDown(View view, Editable content, int keyCode, KeyEvent event) {
        int a = Selection.getSelectionStart(content);
        int b = Selection.getSelectionEnd(content);
        int selStart = Math.min(a, b);
        int selEnd = Math.max(a, b);
        int repeatCount = 0;
        if (selStart < 0 || selEnd < 0) {
            selEnd = 0;
            selStart = 0;
            Selection.setSelection(content, 0);
        }
        int i = event != null ? lookup(event, content) : 0;
        if (event != null) {
            repeatCount = event.getRepeatCount();
        }
        if (repeatCount == 0) {
            if (i != 0) {
                if (selStart != selEnd) {
                    Selection.setSelection(content, selEnd);
                }
                content.replace(selStart, selEnd, String.valueOf((char) i));
                adjustMetaAfterKeypress((Spannable) content);
                return true;
            }
        } else if (i == 48 && repeatCount == 1 && selStart == selEnd && selEnd > 0 && content.charAt(selStart - 1) == '0') {
            content.replace(selStart - 1, selEnd, String.valueOf('+'));
            adjustMetaAfterKeypress((Spannable) content);
            return true;
        }
        adjustMetaAfterKeypress((Spannable) content);
        return super.onKeyDown(view, content, keyCode, event);
    }

    static boolean addDigits(Collection<Character> collection, Locale locale) {
        if (locale == null) {
            return false;
        }
        String[] digits = DecimalFormatSymbols.getInstance(locale).getDigitStrings();
        for (int i = 0; i < 10; i++) {
            if (digits[i].length() > 1) {
                return false;
            }
            collection.add(Character.valueOf(digits[i].charAt(0)));
        }
        return true;
    }

    static boolean addFormatCharsFromSkeleton(Collection<Character> collection, Locale locale, String skeleton, String symbolsToIgnore) {
        if (locale == null) {
            return false;
        }
        String pattern = DateFormat.getBestDateTimePattern(locale, skeleton);
        boolean outsideQuotes = true;
        int i = 0;
        while (true) {
            boolean z = true;
            if (i >= pattern.length()) {
                return true;
            }
            char ch = pattern.charAt(i);
            if (Character.isSurrogate(ch)) {
                return false;
            }
            if (ch == '\'') {
                if (outsideQuotes) {
                    z = false;
                }
                outsideQuotes = z;
                if (i == 0) {
                    continue;
                } else if (pattern.charAt(i - 1) != '\'') {
                    continue;
                }
                i++;
            }
            if (outsideQuotes) {
                if (symbolsToIgnore.indexOf(ch) != -1) {
                    continue;
                    i++;
                } else if (DATE_TIME_FORMAT_SYMBOLS.indexOf(ch) != -1) {
                    return false;
                }
            }
            collection.add(Character.valueOf(ch));
            i++;
        }
    }

    static boolean addFormatCharsFromSkeletons(Collection<Character> collection, Locale locale, String[] skeletons, String symbolsToIgnore) {
        for (String addFormatCharsFromSkeleton : skeletons) {
            if (!addFormatCharsFromSkeleton(collection, locale, addFormatCharsFromSkeleton, symbolsToIgnore)) {
                return false;
            }
        }
        return true;
    }

    static boolean addAmPmChars(Collection<Character> collection, Locale locale) {
        if (locale == null) {
            return false;
        }
        String[] amPm = LocaleData.get(locale).amPm;
        for (int i = 0; i < amPm.length; i++) {
            for (int j = 0; j < amPm[i].length(); j++) {
                char ch = amPm[i].charAt(j);
                if (!Character.isBmpCodePoint(ch)) {
                    return false;
                }
                collection.add(Character.valueOf(ch));
            }
        }
        return true;
    }

    static char[] collectionToArray(Collection<Character> chars) {
        char[] result = new char[chars.size()];
        int i = 0;
        for (Character ch : chars) {
            result[i] = ch.charValue();
            i++;
        }
        return result;
    }
}
