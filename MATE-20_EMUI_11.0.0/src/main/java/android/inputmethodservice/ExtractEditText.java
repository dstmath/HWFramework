package android.inputmethodservice;

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
        super(context, attrs, 16842862);
    }

    public ExtractEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ExtractEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: package-private */
    public void setIME(InputMethodService ime) {
        this.mIME = ime;
    }

    public void startInternalChanges() {
        this.mSettingExtractedText++;
    }

    public void finishInternalChanges() {
        this.mSettingExtractedText--;
    }

    @Override // android.widget.TextView
    public void setExtractedText(ExtractedText text) {
        try {
            this.mSettingExtractedText++;
            super.setExtractedText(text);
        } finally {
            this.mSettingExtractedText--;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public void onSelectionChanged(int selStart, int selEnd) {
        InputMethodService inputMethodService;
        if (this.mSettingExtractedText == 0 && (inputMethodService = this.mIME) != null && selStart >= 0 && selEnd >= 0) {
            inputMethodService.onExtractedSelectionChanged(selStart, selEnd);
        }
    }

    @Override // android.view.View
    public boolean performClick() {
        InputMethodService inputMethodService;
        if (super.performClick() || (inputMethodService = this.mIME) == null) {
            return false;
        }
        inputMethodService.onExtractedTextClicked();
        return true;
    }

    @Override // android.widget.TextView
    public boolean onTextContextMenuItem(int id) {
        if (id == 16908319 || id == 16908340) {
            return super.onTextContextMenuItem(id);
        }
        InputMethodService inputMethodService = this.mIME;
        if (inputMethodService == null || !inputMethodService.onExtractTextContextMenuItem(id)) {
            return super.onTextContextMenuItem(id);
        }
        if (id != 16908321 && id != 16908322) {
            return true;
        }
        stopTextActionMode();
        return true;
    }

    @Override // android.widget.TextView
    public boolean isInputMethodTarget() {
        return true;
    }

    public boolean hasVerticalScrollBar() {
        return computeVerticalScrollRange() > computeVerticalScrollExtent();
    }

    @Override // android.view.View
    public boolean hasWindowFocus() {
        return isEnabled();
    }

    @Override // android.view.View
    public boolean isFocused() {
        return isEnabled();
    }

    @Override // android.view.View
    public boolean hasFocus() {
        return isEnabled();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public void viewClicked(InputMethodManager imm) {
        InputMethodService inputMethodService = this.mIME;
        if (inputMethodService != null) {
            inputMethodService.onViewClicked(false);
        }
    }

    @Override // android.widget.TextView
    public boolean isInExtractedMode() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public void deleteText_internal(int start, int end) {
        this.mIME.onExtractedDeleteText(start, end);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public void replaceText_internal(int start, int end, CharSequence text) {
        this.mIME.onExtractedReplaceText(start, end, text);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public void setSpan_internal(Object span, int start, int end, int flags) {
        this.mIME.onExtractedSetSpan(span, start, end, flags);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public void setCursorPosition_internal(int start, int end) {
        this.mIME.onExtractedSelectionChanged(start, end);
    }
}
