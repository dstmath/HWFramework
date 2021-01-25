package ohos.agp.window.wmc;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputContentInfo;
import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.window.aspbshell.TextInputConnection;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.adapter.CompletionInfoAdapter;
import ohos.miscservices.inputmethod.adapter.ContextMenuActionIdAdapter;
import ohos.miscservices.inputmethod.adapter.ExtractedTextAdapter;
import ohos.miscservices.inputmethod.adapter.ExtractedTextRequestAdapter;
import ohos.miscservices.inputmethod.adapter.InputContentInfoAdapter;
import ohos.miscservices.inputmethod.adapter.InputParam;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;

public class TextViewListenerWrapper implements TextInputConnection.ITextViewListener {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "TextViewListenerWrapper");
    private IAGPTextViewListener listener;

    public TextViewListenerWrapper(IAGPTextViewListener iAGPTextViewListener) {
        if (iAGPTextViewListener != null) {
            this.listener = iAGPTextViewListener;
            return;
        }
        throw new AGPWindowManager.BadWindowException("TextViewListenerWrapper: listener is null");
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean commitText(CharSequence charSequence, int i) {
        return this.listener.commitText(charSequence, i);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean commitContent(InputContentInfo inputContentInfo, int i, Bundle bundle) {
        return this.listener.insertRichContent(InputContentInfoAdapter.convertToRichContent(inputContentInfo));
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean deleteSurroundingText(int i, int i2) {
        return this.listener.deleteSurroundingText(i, i2);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public CharSequence getTextBeforeCursor(int i, int i2) {
        return this.listener.getTextBeforeCursor(i, i2);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public CharSequence getTextAfterCursor(int i, int i2) {
        return this.listener.getTextAfterCursor(i, i2);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean sendKeyEvent(KeyEvent keyEvent) {
        Optional createEvent = MultimodalEventFactory.createEvent(keyEvent);
        if (!createEvent.isPresent()) {
            HiLog.debug(LABEL, "Multi modal createEvent failed!", new Object[0]);
            return false;
        }
        ohos.multimodalinput.event.KeyEvent keyEvent2 = (MultimodalEvent) createEvent.get();
        if (keyEvent2 instanceof ohos.multimodalinput.event.KeyEvent) {
            return this.listener.sendKeyEvent(keyEvent2);
        }
        HiLog.debug(LABEL, "Multi modal createEvent is not instance of KeyEvent!", new Object[0]);
        return false;
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean setComposingRegion(int i, int i2) {
        return this.listener.setComposingRegion(i, i2);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean finishComposingText() {
        return this.listener.finishComposingText();
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean setComposingText(CharSequence charSequence, int i) {
        return this.listener.setComposingText(charSequence, i);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public ExtractedText getExtractedText(ExtractedTextRequest extractedTextRequest, int i) {
        return ExtractedTextAdapter.convertToExtractedText(this.listener.subscribeEditingText(ExtractedTextRequestAdapter.convertToEditingCapability(extractedTextRequest)));
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean performEditorAction(int i) {
        return this.listener.performEditorAction(i);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean setSelection(int i, int i2) {
        return this.listener.setSelection(i, i2);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public void closeConnection() {
        this.listener.closeConnection();
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public void updateEditorInfo(EditorInfo editorInfo) {
        InputParam editorInputParam = this.listener.getEditorInputParam();
        editorInfo.inputType = editorInputParam.getInputType();
        editorInfo.imeOptions = editorInputParam.getImeOption();
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public int getCursorCapsMode(int i) {
        return this.listener.getAutoCapitalizeMode(i);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean beginBatchEdit() {
        return this.listener.beginBatchEdit();
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean endBatchEdit() {
        return this.listener.endBatchEdit();
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public CharSequence getSelectedText(int i) {
        return this.listener.getSelectedText(i);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean clearMetaKeyStates(int i) {
        return this.listener.clearMetaKeyStates(i);
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean commitCompletion(CompletionInfo completionInfo) {
        return this.listener.recommendText(CompletionInfoAdapter.convertToRecommendationInfo(completionInfo));
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean commitCorrection(CorrectionInfo correctionInfo) {
        if (correctionInfo == null) {
            return false;
        }
        return this.listener.reviseText(correctionInfo.getOffset(), correctionInfo.getOldText().toString(), correctionInfo.getNewText().toString());
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean performContextMenuAction(int i) {
        return this.listener.performContextMenuAction(ContextMenuActionIdAdapter.convertToEditMenuActionId(i));
    }

    @Override // ohos.agp.window.aspbshell.TextInputConnection.ITextViewListener
    public boolean requestCursorUpdates(int i) {
        return this.listener.requestCursorUpdates(i);
    }
}
