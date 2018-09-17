package com.android.internal.widget;

import android.os.Bundle;
import android.text.Editable;
import android.text.Spanned;
import android.text.method.KeyListener;
import android.text.style.SuggestionSpan;
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

    public EditableInputConnection(TextView textview) {
        super((View) textview, true);
        this.mTextView = textview;
    }

    public Editable getEditable() {
        TextView tv = this.mTextView;
        if (tv != null) {
            return tv.getEditableText();
        }
        return null;
    }

    public boolean beginBatchEdit() {
        synchronized (this) {
            if (this.mBatchEditNesting >= 0) {
                this.mTextView.beginBatchEdit();
                this.mBatchEditNesting++;
                return true;
            }
            return false;
        }
    }

    public boolean endBatchEdit() {
        synchronized (this) {
            if (this.mBatchEditNesting > 0) {
                this.mTextView.endBatchEdit();
                this.mBatchEditNesting--;
                return true;
            }
            return false;
        }
    }

    public void closeConnection() {
        super.closeConnection();
        synchronized (this) {
            while (this.mBatchEditNesting > 0) {
                endBatchEdit();
            }
            this.mBatchEditNesting = -1;
        }
    }

    public boolean clearMetaKeyStates(int states) {
        Editable content = getEditable();
        if (content == null) {
            return false;
        }
        KeyListener kl = this.mTextView.getKeyListener();
        if (kl != null) {
            try {
                kl.clearMetaKeyState(this.mTextView, content, states);
            } catch (AbstractMethodError e) {
            }
        }
        return true;
    }

    public boolean commitCompletion(CompletionInfo text) {
        this.mTextView.beginBatchEdit();
        this.mTextView.onCommitCompletion(text);
        this.mTextView.endBatchEdit();
        return true;
    }

    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        this.mTextView.beginBatchEdit();
        this.mTextView.onCommitCorrection(correctionInfo);
        this.mTextView.endBatchEdit();
        return true;
    }

    public boolean performEditorAction(int actionCode) {
        this.mTextView.onEditorAction(actionCode);
        return true;
    }

    public boolean performContextMenuAction(int id) {
        this.mTextView.beginBatchEdit();
        this.mTextView.onTextContextMenuItem(id);
        this.mTextView.endBatchEdit();
        return true;
    }

    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        if (this.mTextView != null) {
            ExtractedText et = new ExtractedText();
            if (this.mTextView.extractText(request, et)) {
                if ((flags & 1) != 0) {
                    this.mTextView.setExtracting(request);
                }
                return et;
            }
        }
        return null;
    }

    public boolean performPrivateCommand(String action, Bundle data) {
        this.mTextView.onPrivateIMECommand(action, data);
        return true;
    }

    public boolean commitText(CharSequence text, int newCursorPosition) {
        if (this.mTextView == null) {
            return super.commitText(text, newCursorPosition);
        }
        if (text instanceof Spanned) {
            this.mIMM.registerSuggestionSpansForNotification((SuggestionSpan[]) ((Spanned) text).getSpans(0, text.length(), SuggestionSpan.class));
        }
        this.mTextView.resetErrorChangedFlag();
        boolean success = super.commitText(text, newCursorPosition);
        this.mTextView.hideErrorIfUnchanged();
        return success;
    }

    public boolean requestCursorUpdates(int cursorUpdateMode) {
        if ((cursorUpdateMode & -4) != 0 || this.mIMM == null) {
            return false;
        }
        this.mIMM.setUpdateCursorAnchorInfoMode(cursorUpdateMode);
        if (!((cursorUpdateMode & 1) == 0 || this.mTextView == null || this.mTextView.isInLayout())) {
            this.mTextView.requestLayout();
        }
        return true;
    }
}
