package android.text.method;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;

public abstract class NumberKeyListener extends BaseKeyListener implements InputFilter {
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
            return "";
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
        int repeatCount = 0;
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
        if (event != null) {
            repeatCount = event.getRepeatCount();
        }
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
}
