package ohos.miscservices.inputmethodability;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.EditorAttribute;
import ohos.miscservices.inputmethod.InputDataChannel;
import ohos.miscservices.inputmethod.RichContent;
import ohos.multimodalinput.event.KeyEvent;

public final class TextInputClient {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "TextInputClient");
    private InputMethodEngine mInputMethodEngine;

    public TextInputClient(InputMethodEngine inputMethodEngine) {
        if (inputMethodEngine != null) {
            this.mInputMethodEngine = inputMethodEngine;
            return;
        }
        throw new IllegalArgumentException("The given InputMethodEngine for TextInputClient must not be null.");
    }

    private InputDataChannel getInputDataChannel() {
        return this.mInputMethodEngine.getInputDataChannel();
    }

    public EditorAttribute getEditorAttribute() {
        return this.mInputMethodEngine.getEditorAttribute();
    }

    public boolean insertText(String str) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.insertText(str);
        }
        HiLog.error(TAG, "insertText failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public boolean insertRichContent(RichContent richContent) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        this.mInputMethodEngine.setContentPermission(richContent);
        if (inputDataChannel != null) {
            return inputDataChannel.insertRichContent(richContent);
        }
        HiLog.error(TAG, "insertRichContent failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public boolean deleteBackward(int i) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.deleteBackward(i);
        }
        HiLog.error(TAG, "deleteBackward failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public boolean deleteForward(int i) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.deleteForward(i);
        }
        HiLog.error(TAG, "deleteForward failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public String getForward(int i) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.getForward(i);
        }
        HiLog.error(TAG, "getForward failed, input data channel is not valid.", new Object[0]);
        return "";
    }

    public String getBackward(int i) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.getBackward(i);
        }
        HiLog.error(TAG, "getBackward failed, input data channel is not valid.", new Object[0]);
        return "";
    }

    public boolean subscribeEditingText(int i, EditingCapability editingCapability) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.subscribeEditingText(i, editingCapability);
        }
        HiLog.error(TAG, "subscribeEditingText failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public EditingText getEditingText(int i, EditingCapability editingCapability) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.getEditingText(i, editingCapability);
        }
        HiLog.error(TAG, "getEditingText failed, input data channel is not valid.", new Object[0]);
        return new EditingText();
    }

    public boolean unsubscribeEditingText(int i) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.unsubscribeEditingText(i);
        }
        HiLog.error(TAG, "unsubscribeEditingText failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public boolean sendKeyEvent(KeyEvent keyEvent) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.sendKeyEvent(keyEvent);
        }
        HiLog.error(TAG, "sendKeyEvent failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public boolean sendKeyFunction(int i) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.sendKeyFunction(i);
        }
        HiLog.error(TAG, "sendKeyFunction failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public boolean selectText(int i, int i2) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.selectText(i, i2);
        }
        HiLog.error(TAG, "selectText failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public boolean requestCurrentCursorContext() {
        return getInputDataChannel().requestCurrentCursorContext();
    }

    public boolean subscribeCursorContext() {
        return getInputDataChannel().subscribeCursorContext();
    }

    public boolean unsubscribeCursorContext() {
        return getInputDataChannel().unsubscribeCursorContext();
    }

    public int getAutoCapitalizeMode(int i) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.getAutoCapitalizeMode(i);
        }
        HiLog.error(TAG, "getAutoCapitalizeMode failed, input data channel is not valid.", new Object[0]);
        return 8;
    }

    public boolean reviseText(int i, String str, String str2) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.reviseText(i, str, str2);
        }
        HiLog.error(TAG, "reviseText failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public String getSelectedText(int i) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.getSelectedText(i);
        }
        HiLog.error(TAG, "reviseText failed, input data channel is not valid.", new Object[0]);
        return "";
    }

    public boolean markText(int i, int i2) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.markText(i, i2);
        }
        HiLog.error(TAG, "markText failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public boolean unmarkText() {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.unmarkText();
        }
        HiLog.error(TAG, "unmarkText failed, input data channel is not valid.", new Object[0]);
        return false;
    }

    public boolean replaceMarkedText(String str) {
        InputDataChannel inputDataChannel = getInputDataChannel();
        if (inputDataChannel != null) {
            return inputDataChannel.replaceMarkedText(str);
        }
        HiLog.error(TAG, "replaceMarkedText failed, input data channel is not valid.", new Object[0]);
        return false;
    }
}
