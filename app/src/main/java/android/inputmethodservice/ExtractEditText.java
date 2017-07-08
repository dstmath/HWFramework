package android.inputmethodservice;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ExtractEditText extends EditText {
    private InputMethodService mIME;
    private int mSettingExtractedText;

    public ExtractEditText(Context context) {
        super(context, null);
    }

    public ExtractEditText(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.editTextStyle);
    }

    public ExtractEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExtractEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    void setIME(InputMethodService ime) {
        this.mIME = ime;
    }

    public void startInternalChanges() {
        this.mSettingExtractedText++;
    }

    public void finishInternalChanges() {
        this.mSettingExtractedText--;
    }

    public void setExtractedText(ExtractedText text) {
        try {
            this.mSettingExtractedText++;
            super.setExtractedText(text);
        } finally {
            this.mSettingExtractedText--;
        }
    }

    protected void onSelectionChanged(int selStart, int selEnd) {
        if (this.mSettingExtractedText == 0 && this.mIME != null && selStart >= 0 && selEnd >= 0) {
            this.mIME.onExtractedSelectionChanged(selStart, selEnd);
        }
    }

    public boolean performClick() {
        if (super.performClick() || this.mIME == null) {
            return false;
        }
        this.mIME.onExtractedTextClicked();
        return true;
    }

    public boolean onTextContextMenuItem(int id) {
        if (id == R.id.selectAll || id == R.id.replaceText) {
            return super.onTextContextMenuItem(id);
        }
        if (this.mIME == null || !this.mIME.onExtractTextContextMenuItem(id)) {
            return super.onTextContextMenuItem(id);
        }
        if (id == R.id.copy || id == R.id.paste) {
            stopTextActionMode();
        }
        return true;
    }

    public boolean isInputMethodTarget() {
        return true;
    }

    public boolean hasVerticalScrollBar() {
        return computeVerticalScrollRange() > computeVerticalScrollExtent();
    }

    public boolean hasWindowFocus() {
        return isEnabled();
    }

    public boolean isFocused() {
        return isEnabled();
    }

    public boolean hasFocus() {
        return isEnabled();
    }

    protected void viewClicked(InputMethodManager imm) {
        if (this.mIME != null) {
            this.mIME.onViewClicked(false);
        }
    }

    public boolean isInExtractedMode() {
        return true;
    }

    protected void deleteText_internal(int start, int end) {
        this.mIME.onExtractedDeleteText(start, end);
    }

    protected void replaceText_internal(int start, int end, CharSequence text) {
        this.mIME.onExtractedReplaceText(start, end, text);
    }

    protected void setSpan_internal(Object span, int start, int end, int flags) {
        this.mIME.onExtractedSetSpan(span, start, end, flags);
    }

    protected void setCursorPosition_internal(int start, int end) {
        this.mIME.onExtractedSelectionChanged(start, end);
    }
}
