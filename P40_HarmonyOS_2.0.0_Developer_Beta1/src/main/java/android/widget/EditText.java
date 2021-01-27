package android.widget;

import android.content.Context;
import android.hwcontrol.HwWidgetFactory;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import huawei.android.widget.HwOnSearchEventListener;

public class EditText extends TextView {
    private HwKeyEventDetector mHwKeyEventDetector;
    private TextWatcher mInsertTextWatcher;
    private boolean mIsExtendedEditEnabled;
    private boolean mIsInsertMode;

    public EditText(Context context) {
        this(context, null);
    }

    public EditText(Context context, AttributeSet attrs) {
        this(context, attrs, 16842862);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHwKeyEventDetector = null;
        this.mIsInsertMode = false;
        this.mIsExtendedEditEnabled = false;
        this.mInsertTextWatcher = new TextWatcher() {
            /* class android.widget.EditText.AnonymousClass1 */

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (text != null && EditText.this.mIsInsertMode) {
                    if (start >= text.length() || text.charAt(start) != '\n') {
                        int length = text.length();
                        Object[] builders = EditText.this.getText().getSpans(start, start + count, UnderlineSpan.class);
                        if (before == 0) {
                            if (builders == null || builders.length == 0) {
                                replace(length, start, count);
                            }
                        } else if (builders == null || builders.length == 0) {
                            replace(length, start, count);
                        }
                    }
                }
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable text) {
            }

            private void replace(int newLen, int start, int count) {
                int begin = start + count;
                int end = begin + count;
                if (end > newLen) {
                    end = newLen;
                }
                if (end > begin) {
                    EditText.this.getText().replace(begin, end, "");
                }
            }
        };
    }

    public void setExtendedEditEnabled(boolean isEnabled) {
        this.mIsExtendedEditEnabled = isEnabled;
        if (isAttachedToWindow()) {
            if (isEnabled) {
                addTextChangedListener(this.mInsertTextWatcher);
            } else {
                removeTextChangedListener(this.mInsertTextWatcher);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mIsExtendedEditEnabled) {
            addTextChangedListener(this.mInsertTextWatcher);
        } else {
            removeTextChangedListener(this.mInsertTextWatcher);
        }
    }

    public boolean isExtendedEditEnabled() {
        return this.mIsExtendedEditEnabled;
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event == null) {
            return false;
        }
        if (keyCode == 124 && event.getAction() == 0) {
            this.mIsInsertMode = !this.mIsInsertMode;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override // android.widget.TextView
    public boolean getFreezesText() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public boolean getDefaultEditable() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    @Override // android.widget.TextView
    public Editable getText() {
        CharSequence text = super.getText();
        if (text == null) {
            return null;
        }
        if (text instanceof Editable) {
            return (Editable) super.getText();
        }
        super.setText(text, TextView.BufferType.EDITABLE);
        return (Editable) super.getText();
    }

    @Override // android.widget.TextView
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(text, TextView.BufferType.EDITABLE);
    }

    public void setSelection(int start, int stop) {
        Selection.setSelection(getText(), start, stop);
    }

    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }

    public void selectAll() {
        Selection.selectAll(getText());
    }

    public void extendSelection(int index) {
        Selection.extendSelection(getText(), index);
    }

    @Override // android.widget.TextView
    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        if (ellipsis != TextUtils.TruncateAt.MARQUEE) {
            super.setEllipsize(ellipsis);
            return;
        }
        throw new IllegalArgumentException("EditText cannot use the ellipsize mode TextUtils.TruncateAt.MARQUEE");
    }

    @Override // android.widget.TextView, android.view.View
    public CharSequence getAccessibilityClassName() {
        return EditText.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView
    public boolean supportsAutoSizeText() {
        return false;
    }

    @Override // android.widget.TextView, android.view.View
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled()) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT);
        }
    }

    @Override // android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event == null) {
            return false;
        }
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector == null || !hwKeyEventDetector.onKeyEvent(event.getKeyCode(), event)) {
            return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    public void setOnSearchEventListener(HwOnSearchEventListener listener) {
        if (this.mHwKeyEventDetector == null) {
            this.mHwKeyEventDetector = HwWidgetFactory.getKeyEventDetector(getContext());
        }
        this.mHwKeyEventDetector.setOnSearchEventListener(listener);
    }

    public HwOnSearchEventListener getOnSearchEventListener() {
        HwKeyEventDetector hwKeyEventDetector = this.mHwKeyEventDetector;
        if (hwKeyEventDetector != null) {
            return hwKeyEventDetector.getOnSearchEventListener();
        }
        return null;
    }
}
