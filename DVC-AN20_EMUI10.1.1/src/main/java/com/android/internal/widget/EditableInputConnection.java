package com.android.internal.widget;

import android.annotation.UnsupportedAppUsage;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.widget.TextView;

public class EditableInputConnection extends BaseInputConnection {
    private static final boolean DEBUG = false;
    private static final String TAG = "EditableInputConnection";
    private int mBatchEditNesting;
    private final TextView mTextView;

    @UnsupportedAppUsage
    public EditableInputConnection(TextView textview) {
        super((View) textview, true);
        this.mTextView = textview;
    }

    @Override // android.view.inputmethod.BaseInputConnection
    public Editable getEditable() {
        TextView tv = this.mTextView;
        if (tv != null) {
            return tv.getEditableText();
        }
        return null;
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean beginBatchEdit() {
        synchronized (this) {
            if (this.mBatchEditNesting < 0) {
                return false;
            }
            this.mTextView.beginBatchEdit();
            this.mBatchEditNesting++;
            return true;
        }
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean endBatchEdit() {
        synchronized (this) {
            if (this.mBatchEditNesting <= 0) {
                return false;
            }
            this.mTextView.endBatchEdit();
            this.mBatchEditNesting--;
            return true;
        }
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public void closeConnection() {
        super.closeConnection();
        synchronized (this) {
            while (this.mBatchEditNesting > 0) {
                endBatchEdit();
            }
            this.mBatchEditNesting = -1;
        }
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean clearMetaKeyStates(int states) {
        Editable content = getEditable();
        if (content == null) {
            return false;
        }
        KeyListener kl = this.mTextView.getKeyListener();
        if (kl == null) {
            return true;
        }
        try {
            kl.clearMetaKeyState(this.mTextView, content, states);
            return true;
        } catch (AbstractMethodError e) {
            return true;
        }
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean commitCompletion(CompletionInfo text) {
        this.mTextView.beginBatchEdit();
        this.mTextView.onCommitCompletion(text);
        this.mTextView.endBatchEdit();
        return true;
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        this.mTextView.beginBatchEdit();
        this.mTextView.onCommitCorrection(correctionInfo);
        this.mTextView.endBatchEdit();
        return true;
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean performEditorAction(int actionCode) {
        this.mTextView.onEditorAction(actionCode);
        return true;
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean performContextMenuAction(int id) {
        this.mTextView.beginBatchEdit();
        this.mTextView.onTextContextMenuItem(id);
        this.mTextView.endBatchEdit();
        return true;
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        if (this.mTextView == null) {
            return null;
        }
        ExtractedText et = new ExtractedText();
        if (!this.mTextView.extractText(request, et)) {
            return null;
        }
        if ((flags & 1) != 0) {
            this.mTextView.setExtracting(request);
        }
        return et;
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean performPrivateCommand(String action, Bundle data) {
        this.mTextView.onPrivateIMECommand(action, data);
        return true;
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean commitText(CharSequence text, int newCursorPosition) {
        if (text == null) {
            return false;
        }
        TextView textView = this.mTextView;
        if (textView == null) {
            return super.commitText(text, newCursorPosition);
        }
        textView.resetErrorChangedFlag();
        boolean success = super.commitText(text, newCursorPosition);
        this.mTextView.hideErrorIfUnchanged();
        return success;
    }

    @Override // android.view.inputmethod.InputConnection, android.view.inputmethod.BaseInputConnection
    public boolean requestCursorUpdates(int cursorUpdateMode) {
        TextView textView;
        if ((cursorUpdateMode & -4) != 0 || this.mIMM == null) {
            return false;
        }
        this.mIMM.setUpdateCursorAnchorInfoMode(cursorUpdateMode);
        if ((cursorUpdateMode & 1) == 0 || (textView = this.mTextView) == null || textView.isInLayout()) {
            return true;
        }
        this.mTextView.requestLayout();
        return true;
    }
}
