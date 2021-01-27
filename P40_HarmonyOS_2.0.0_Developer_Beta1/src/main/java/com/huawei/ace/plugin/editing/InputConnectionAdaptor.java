package com.huawei.ace.plugin.editing;

import android.text.DynamicLayout;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.TextPaint;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import com.huawei.ace.runtime.ALog;

/* access modifiers changed from: package-private */
public class InputConnectionAdaptor extends BaseInputConnection {
    private static final String LOG_TAG = "Ace_IME";
    private final View aceView;
    private int batchCount = 0;
    private final int clientId;
    private final TextInputDelegate delegate;
    private final Editable editable;
    private InputMethodManager imm;
    private final Layout layout = new DynamicLayout(this.editable, new TextPaint(), Integer.MAX_VALUE, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

    public InputConnectionAdaptor(View view, int i, TextInputDelegate textInputDelegate, Editable editable2) {
        super(view, true);
        this.aceView = view;
        this.clientId = i;
        this.delegate = textInputDelegate;
        this.editable = editable2;
        Object systemService = view.getContext().getSystemService("input_method");
        if (systemService instanceof InputMethodManager) {
            this.imm = (InputMethodManager) systemService;
        }
    }

    private void updateEditingState() {
        if (this.batchCount <= 0 && this.imm != null) {
            int selectionStart = Selection.getSelectionStart(this.editable);
            int selectionEnd = Selection.getSelectionEnd(this.editable);
            int composingSpanStart = BaseInputConnection.getComposingSpanStart(this.editable);
            int composingSpanEnd = BaseInputConnection.getComposingSpanEnd(this.editable);
            this.imm.updateSelection(this.aceView, selectionStart, selectionEnd, composingSpanStart, composingSpanEnd);
            this.delegate.updateEditingState(this.clientId, this.editable.toString(), selectionStart, selectionEnd, composingSpanStart, composingSpanEnd);
        }
    }

    @Override // android.view.inputmethod.BaseInputConnection
    public Editable getEditable() {
        return this.editable;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean beginBatchEdit() {
        this.batchCount++;
        return super.beginBatchEdit();
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean endBatchEdit() {
        boolean endBatchEdit = super.endBatchEdit();
        this.batchCount--;
        updateEditingState();
        return endBatchEdit;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean commitText(CharSequence charSequence, int i) {
        boolean commitText = super.commitText(charSequence, i);
        updateEditingState();
        return commitText;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean deleteSurroundingText(int i, int i2) {
        if (Selection.getSelectionStart(this.editable) == -1) {
            return true;
        }
        boolean deleteSurroundingText = super.deleteSurroundingText(i, i2);
        updateEditingState();
        return deleteSurroundingText;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setComposingRegion(int i, int i2) {
        boolean composingRegion = super.setComposingRegion(i, i2);
        updateEditingState();
        return composingRegion;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setComposingText(CharSequence charSequence, int i) {
        boolean z;
        if (charSequence.length() == 0) {
            z = super.commitText(charSequence, i);
        } else {
            z = super.setComposingText(charSequence, i);
        }
        updateEditingState();
        return z;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setSelection(int i, int i2) {
        boolean selection = super.setSelection(i, i2);
        updateEditingState();
        return selection;
    }

    private static int clampIndexToEditable(int i, Editable editable2) {
        int max = Math.max(0, Math.min(editable2.length(), i));
        if (max != i) {
            ALog.d(LOG_TAG, "Text selection index was clamped (" + i + "->" + max + ") to remain in bounds. This may not be your fault, as some keyboards may select outside of bounds.");
        }
        return max;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean sendKeyEvent(KeyEvent keyEvent) {
        ALog.d(LOG_TAG, "action & keycode: " + keyEvent.getAction() + " , " + keyEvent.getKeyCode());
        if (keyEvent.getAction() == 0) {
            if (keyEvent.getKeyCode() == 67) {
                int clampIndexToEditable = clampIndexToEditable(Selection.getSelectionStart(this.editable), this.editable);
                int clampIndexToEditable2 = clampIndexToEditable(Selection.getSelectionEnd(this.editable), this.editable);
                if (clampIndexToEditable2 > clampIndexToEditable) {
                    Selection.setSelection(this.editable, clampIndexToEditable);
                    this.editable.delete(clampIndexToEditable, clampIndexToEditable2);
                    updateEditingState();
                    return true;
                } else if (clampIndexToEditable > 0) {
                    reverseDeleteSelection(clampIndexToEditable);
                    return true;
                } else {
                    ALog.d(LOG_TAG, "illegal selection.");
                }
            } else if (keyEvent.getKeyCode() == 21) {
                int max = Math.max(Selection.getSelectionStart(this.editable) - 1, 0);
                setSelection(max, max);
                return true;
            } else if (keyEvent.getKeyCode() == 22) {
                int min = Math.min(Selection.getSelectionStart(this.editable) + 1, this.editable.length());
                setSelection(min, min);
                return true;
            } else {
                enterCharacter(keyEvent);
                return true;
            }
        }
        return false;
    }

    private void reverseDeleteSelection(int i) {
        Layout layout2 = this.layout;
        if (layout2.isRtlCharAt(layout2.getLineForOffset(i))) {
            try {
                Selection.extendRight(this.editable, this.layout);
            } catch (IndexOutOfBoundsException unused) {
                Selection.setSelection(this.editable, i, i - 1);
            }
        } else {
            Selection.extendLeft(this.editable, this.layout);
        }
        int clampIndexToEditable = clampIndexToEditable(Selection.getSelectionStart(this.editable), this.editable);
        int clampIndexToEditable2 = clampIndexToEditable(Selection.getSelectionEnd(this.editable), this.editable);
        Selection.setSelection(this.editable, Math.min(clampIndexToEditable, clampIndexToEditable2));
        this.editable.delete(Math.min(clampIndexToEditable, clampIndexToEditable2), Math.max(clampIndexToEditable, clampIndexToEditable2));
        updateEditingState();
    }

    private void enterCharacter(KeyEvent keyEvent) {
        int unicodeChar = keyEvent.getUnicodeChar();
        if (unicodeChar != 0) {
            int max = Math.max(0, Selection.getSelectionStart(this.editable));
            int max2 = Math.max(0, Selection.getSelectionEnd(this.editable));
            if (max2 != max) {
                this.editable.delete(max, max2);
            }
            this.editable.insert(max, String.valueOf((char) unicodeChar));
            int i = max + 1;
            setSelection(i, i);
            updateEditingState();
        }
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean performEditorAction(int i) {
        TextInputAction textInputAction;
        ALog.d(LOG_TAG, "performEditorAction: " + i);
        if (i == 0) {
            textInputAction = TextInputAction.UNSPECIFIED;
        } else if (i == 1) {
            textInputAction = TextInputAction.NONE;
        } else if (i == 2) {
            textInputAction = TextInputAction.GO;
        } else if (i == 3) {
            textInputAction = TextInputAction.SEARCH;
        } else if (i == 4) {
            textInputAction = TextInputAction.SEND;
        } else if (i == 5) {
            textInputAction = TextInputAction.NEXT;
        } else if (i != 7) {
            textInputAction = TextInputAction.DONE;
        } else {
            textInputAction = TextInputAction.PREVIOUS;
        }
        this.delegate.performAction(this.clientId, textInputAction);
        return true;
    }
}
