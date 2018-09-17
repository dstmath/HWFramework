package android.text.method;

import android.icu.text.DecimalFormatSymbols;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.LogException;
import android.view.KeyEvent;
import android.view.View;
import java.util.Collection;
import java.util.Locale;
import libcore.icu.LocaleData;

public abstract class NumberKeyListener extends BaseKeyListener implements InputFilter {
    private static final String DATE_TIME_FORMAT_SYMBOLS = "GyYuUrQqMLlwWdDFgEecabBhHKkjJCmsSAzZOvVXx";
    private static final char SINGLE_QUOTE = '\'';

    protected abstract char[] getAcceptedChars();

    protected int lookup(KeyEvent event, Spannable content) {
        return event.getMatch(getAcceptedChars(), MetaKeyKeyListener.getMetaState((CharSequence) content, event));
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
            return LogException.NO_VALUE;
        }
        SpannableStringBuilder filtered = new SpannableStringBuilder(source, start, end);
        i -= start;
        end -= start;
        int len = end - start;
        for (int j = end - 1; j >= i; j--) {
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
        if (selStart < 0 || selEnd < 0) {
            selEnd = 0;
            selStart = 0;
            Selection.setSelection(content, 0);
        }
        int i = event != null ? lookup(event, content) : 0;
        int repeatCount = event != null ? event.getRepeatCount() : 0;
        if (repeatCount == 0) {
            if (i != 0) {
                if (selStart != selEnd) {
                    Selection.setSelection(content, selEnd);
                }
                content.replace(selStart, selEnd, String.valueOf((char) i));
                MetaKeyKeyListener.adjustMetaAfterKeypress((Spannable) content);
                return true;
            }
        } else if (i == 48 && repeatCount == 1 && selStart == selEnd && selEnd > 0 && content.charAt(selStart - 1) == '0') {
            content.replace(selStart - 1, selEnd, String.valueOf('+'));
            MetaKeyKeyListener.adjustMetaAfterKeypress((Spannable) content);
            return true;
        }
        MetaKeyKeyListener.adjustMetaAfterKeypress((Spannable) content);
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
        int outsideQuotes = 1;
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (Character.isSurrogate(ch)) {
                return false;
            }
            if (ch == '\'') {
                outsideQuotes ^= 1;
                if (i == 0) {
                    continue;
                } else if (pattern.charAt(i - 1) != '\'') {
                    continue;
                }
            }
            if (outsideQuotes != 0) {
                if (symbolsToIgnore.indexOf(ch) != -1) {
                    continue;
                } else if (DATE_TIME_FORMAT_SYMBOLS.indexOf(ch) != -1) {
                    return false;
                }
            }
            collection.add(Character.valueOf(ch));
        }
        return true;
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
            int i2 = i + 1;
            result[i] = ch.charValue();
            i = i2;
        }
        return result;
    }
}
