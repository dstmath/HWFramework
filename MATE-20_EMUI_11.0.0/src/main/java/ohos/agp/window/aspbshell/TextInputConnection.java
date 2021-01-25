package ohos.agp.window.aspbshell;

import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputContentInfo;

public class TextInputConnection extends BaseInputConnection {
    private ITextViewListener mTextViewListener;

    public interface ITextViewListener {
        boolean beginBatchEdit();

        boolean clearMetaKeyStates(int i);

        void closeConnection();

        boolean commitCompletion(CompletionInfo completionInfo);

        boolean commitContent(InputContentInfo inputContentInfo, int i, Bundle bundle);

        boolean commitCorrection(CorrectionInfo correctionInfo);

        boolean commitText(CharSequence charSequence, int i);

        boolean deleteSurroundingText(int i, int i2);

        boolean endBatchEdit();

        boolean finishComposingText();

        int getCursorCapsMode(int i);

        ExtractedText getExtractedText(ExtractedTextRequest extractedTextRequest, int i);

        CharSequence getSelectedText(int i);

        CharSequence getTextAfterCursor(int i, int i2);

        CharSequence getTextBeforeCursor(int i, int i2);

        boolean performContextMenuAction(int i);

        boolean performEditorAction(int i);

        boolean requestCursorUpdates(int i);

        boolean sendKeyEvent(KeyEvent keyEvent);

        boolean setComposingRegion(int i, int i2);

        boolean setComposingText(CharSequence charSequence, int i);

        boolean setSelection(int i, int i2);

        void updateEditorInfo(EditorInfo editorInfo);
    }

    public TextInputConnection(View view, Editable editable, ITextViewListener iTextViewListener) {
        super(view, true);
        this.mTextViewListener = iTextViewListener;
    }

    public ITextViewListener getTextViewListener() {
        return this.mTextViewListener;
    }

    public void setTextViewListener(ITextViewListener iTextViewListener) {
        this.mTextViewListener = iTextViewListener;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean commitText(CharSequence charSequence, int i) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return false;
        }
        return iTextViewListener.commitText(charSequence, i);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean commitContent(InputContentInfo inputContentInfo, int i, Bundle bundle) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return false;
        }
        return iTextViewListener.commitContent(inputContentInfo, i, bundle);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean deleteSurroundingText(int i, int i2) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return false;
        }
        return iTextViewListener.deleteSurroundingText(i, i2);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean deleteSurroundingTextInCodePoints(int i, int i2) {
        return deleteSurroundingText(i, i2);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public CharSequence getTextBeforeCursor(int i, int i2) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return "";
        }
        return iTextViewListener.getTextBeforeCursor(i, i2);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public CharSequence getTextAfterCursor(int i, int i2) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return "";
        }
        return iTextViewListener.getTextAfterCursor(i, i2);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean sendKeyEvent(KeyEvent keyEvent) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return false;
        }
        return iTextViewListener.sendKeyEvent(keyEvent);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setComposingRegion(int i, int i2) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return false;
        }
        return iTextViewListener.setComposingRegion(i, i2);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean finishComposingText() {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return false;
        }
        return iTextViewListener.finishComposingText();
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setComposingText(CharSequence charSequence, int i) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return false;
        }
        return iTextViewListener.setComposingText(charSequence, i);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public ExtractedText getExtractedText(ExtractedTextRequest extractedTextRequest, int i) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return new ExtractedText();
        }
        return iTextViewListener.getExtractedText(extractedTextRequest, i);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean performEditorAction(int i) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return false;
        }
        return iTextViewListener.performEditorAction(i);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean setSelection(int i, int i2) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return false;
        }
        return iTextViewListener.setSelection(i, i2);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public void closeConnection() {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            iTextViewListener.closeConnection();
        }
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public int getCursorCapsMode(int i) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            return iTextViewListener.getCursorCapsMode(i);
        }
        return 0;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean beginBatchEdit() {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            return iTextViewListener.beginBatchEdit();
        }
        return false;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean endBatchEdit() {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            return iTextViewListener.endBatchEdit();
        }
        return false;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public CharSequence getSelectedText(int i) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener == null) {
            return "";
        }
        return iTextViewListener.getSelectedText(i);
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean clearMetaKeyStates(int i) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            return iTextViewListener.clearMetaKeyStates(i);
        }
        return false;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean commitCompletion(CompletionInfo completionInfo) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            return iTextViewListener.commitCompletion(completionInfo);
        }
        return false;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            return iTextViewListener.commitCorrection(correctionInfo);
        }
        return false;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean performContextMenuAction(int i) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            return iTextViewListener.performEditorAction(i);
        }
        return false;
    }

    @Override // android.view.inputmethod.BaseInputConnection, android.view.inputmethod.InputConnection
    public boolean requestCursorUpdates(int i) {
        ITextViewListener iTextViewListener = this.mTextViewListener;
        if (iTextViewListener != null) {
            return iTextViewListener.requestCursorUpdates(i);
        }
        return false;
    }
}
