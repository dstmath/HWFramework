package android.view.inputmethod;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

public class InputConnectionWrapper implements InputConnection {
    private int mMissingMethodFlags;
    final boolean mMutable;
    private InputConnection mTarget;

    public InputConnectionWrapper(InputConnection target, boolean mutable) {
        this.mMutable = mutable;
        this.mTarget = target;
        this.mMissingMethodFlags = InputConnectionInspector.getMissingMethodFlags(target);
    }

    public void setTarget(InputConnection target) {
        if (this.mTarget == null || this.mMutable) {
            this.mTarget = target;
            this.mMissingMethodFlags = InputConnectionInspector.getMissingMethodFlags(target);
            return;
        }
        throw new SecurityException("not mutable");
    }

    public int getMissingMethodFlags() {
        return this.mMissingMethodFlags;
    }

    @Override // android.view.inputmethod.InputConnection
    public CharSequence getTextBeforeCursor(int n, int flags) {
        return this.mTarget.getTextBeforeCursor(n, flags);
    }

    @Override // android.view.inputmethod.InputConnection
    public CharSequence getTextAfterCursor(int n, int flags) {
        return this.mTarget.getTextAfterCursor(n, flags);
    }

    @Override // android.view.inputmethod.InputConnection
    public CharSequence getSelectedText(int flags) {
        return this.mTarget.getSelectedText(flags);
    }

    @Override // android.view.inputmethod.InputConnection
    public int getCursorCapsMode(int reqModes) {
        return this.mTarget.getCursorCapsMode(reqModes);
    }

    @Override // android.view.inputmethod.InputConnection
    public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
        return this.mTarget.getExtractedText(request, flags);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean deleteSurroundingTextInCodePoints(int beforeLength, int afterLength) {
        return this.mTarget.deleteSurroundingTextInCodePoints(beforeLength, afterLength);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        return this.mTarget.deleteSurroundingText(beforeLength, afterLength);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean setComposingText(CharSequence text, int newCursorPosition) {
        return this.mTarget.setComposingText(text, newCursorPosition);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean setComposingRegion(int start, int end) {
        return this.mTarget.setComposingRegion(start, end);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean finishComposingText() {
        return this.mTarget.finishComposingText();
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean commitText(CharSequence text, int newCursorPosition) {
        return this.mTarget.commitText(text, newCursorPosition);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean commitCompletion(CompletionInfo text) {
        return this.mTarget.commitCompletion(text);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        return this.mTarget.commitCorrection(correctionInfo);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean setSelection(int start, int end) {
        return this.mTarget.setSelection(start, end);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean performEditorAction(int editorAction) {
        return this.mTarget.performEditorAction(editorAction);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean performContextMenuAction(int id) {
        return this.mTarget.performContextMenuAction(id);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean beginBatchEdit() {
        return this.mTarget.beginBatchEdit();
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean endBatchEdit() {
        return this.mTarget.endBatchEdit();
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean sendKeyEvent(KeyEvent event) {
        return this.mTarget.sendKeyEvent(event);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean clearMetaKeyStates(int states) {
        return this.mTarget.clearMetaKeyStates(states);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean reportFullscreenMode(boolean enabled) {
        return this.mTarget.reportFullscreenMode(enabled);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean performPrivateCommand(String action, Bundle data) {
        return this.mTarget.performPrivateCommand(action, data);
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean requestCursorUpdates(int cursorUpdateMode) {
        return this.mTarget.requestCursorUpdates(cursorUpdateMode);
    }

    @Override // android.view.inputmethod.InputConnection
    public Handler getHandler() {
        return this.mTarget.getHandler();
    }

    @Override // android.view.inputmethod.InputConnection
    public void closeConnection() {
        this.mTarget.closeConnection();
    }

    @Override // android.view.inputmethod.InputConnection
    public boolean commitContent(InputContentInfo inputContentInfo, int flags, Bundle opts) {
        return this.mTarget.commitContent(inputContentInfo, flags, opts);
    }
}
