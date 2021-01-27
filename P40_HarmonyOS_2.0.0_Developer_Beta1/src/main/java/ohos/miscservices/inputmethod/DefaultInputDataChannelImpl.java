package ohos.miscservices.inputmethod;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.multimodalinput.event.KeyEvent;
import ohos.utils.PacMap;

public class DefaultInputDataChannelImpl implements InputDataChannel {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "DefaultInputDataChannelImpl");

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean lock() {
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean requestCurrentCursorContext() {
        return false;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean subscribeCursorContext() {
        return false;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean unlock() {
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean unsubscribeCursorContext() {
        return false;
    }

    public DefaultInputDataChannelImpl() {
        HiLog.debug(TAG, "DefaultInputDataChannelImpl construct.", new Object[0]);
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean insertText(String str) {
        HiLog.debug(TAG, "insertText in DefaultInputDataChannelImpl begin.", new Object[0]);
        EditingText subscribeEditingText = subscribeEditingText(new EditingCapability());
        if (subscribeEditingText == null) {
            HiLog.error(TAG, "insertText fail", new Object[0]);
            return false;
        }
        String textContent = subscribeEditingText.getTextContent();
        if (textContent == null) {
            HiLog.error(TAG, "insertText fail", new Object[0]);
            return false;
        }
        int selectionStart = subscribeEditingText.getSelectionStart();
        int selectionEnd = subscribeEditingText.getSelectionEnd();
        if (selectionStart <= selectionEnd) {
            selectionStart = selectionEnd;
        }
        StringBuilder sb = new StringBuilder(textContent);
        sb.insert(selectionStart, str);
        subscribeEditingText.setTextContent(sb.toString());
        HiLog.debug(TAG, "insertText end", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean insertRichContent(RichContent richContent) {
        HiLog.debug(TAG, "insertRichContent in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public String getForward(int i) {
        int i2 = 0;
        HiLog.debug(TAG, "getForward in DefaultInputDataChannelImpl begin", new Object[0]);
        if (i <= 0) {
            return "";
        }
        EditingText subscribeEditingText = subscribeEditingText(new EditingCapability());
        if (subscribeEditingText == null) {
            HiLog.error(TAG, "getTextBeforeCursor fail", new Object[0]);
            return "";
        }
        String textContent = subscribeEditingText.getTextContent();
        if (textContent == null) {
            HiLog.error(TAG, "getTextBeforeCursor fail", new Object[0]);
            return "";
        }
        int selectionStart = subscribeEditingText.getSelectionStart();
        int selectionEnd = subscribeEditingText.getSelectionEnd();
        if (selectionStart < selectionEnd) {
            selectionEnd = selectionStart;
        }
        if (selectionEnd > i) {
            i2 = selectionEnd - i;
        }
        return textContent.substring(i2, selectionEnd);
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public String getBackward(int i) {
        HiLog.debug(TAG, "getBackward in DefaultInputDataChannelImpl begin", new Object[0]);
        if (i <= 0) {
            return "";
        }
        EditingText subscribeEditingText = subscribeEditingText(new EditingCapability());
        if (subscribeEditingText == null) {
            HiLog.error(TAG, "getTextAfterCursor fail", new Object[0]);
            return "";
        }
        String textContent = subscribeEditingText.getTextContent();
        if (textContent == null) {
            HiLog.error(TAG, "getTextAfterCursor fail", new Object[0]);
            return "";
        }
        int selectionStart = subscribeEditingText.getSelectionStart();
        int selectionEnd = subscribeEditingText.getSelectionEnd();
        if (selectionStart > selectionEnd) {
            selectionEnd = selectionStart;
        }
        int i2 = i + selectionEnd;
        if (i2 > textContent.length()) {
            i2 = textContent.length();
        }
        return textContent.substring(selectionEnd, i2);
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean sendKeyEvent(KeyEvent keyEvent) {
        HiLog.debug(TAG, "sendKeyEvent in DefaultInputDataChannelImpl begin.", new Object[0]);
        return false;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public void close() {
        HiLog.debug(TAG, "close in DefaultInputDataChannelImpl begin.", new Object[0]);
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean markText(int i, int i2) {
        HiLog.debug(TAG, "markText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return i <= i2 && i <= 8 && i2 <= 8 && i >= 0 && i2 >= 0;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean unmarkText() {
        HiLog.debug(TAG, "unmarkText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean replaceMarkedText(String str) {
        HiLog.debug(TAG, "replaceMarkedText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public EditingText subscribeEditingText(EditingCapability editingCapability) {
        HiLog.debug(TAG, "subscribeEditingText in DefaultInputDataChannelImpl begin.", new Object[0]);
        if (editingCapability == null) {
            return new EditingText();
        }
        EditingText editingText = new EditingText();
        editingText.setTextContent("Empty String");
        editingText.setSelectionStart(1);
        editingText.setSelectionEnd(1);
        return editingText;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public EditingText getEditingText(int i, EditingCapability editingCapability) {
        HiLog.debug(TAG, "getscribeEditingText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return new EditingText();
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean subscribeEditingText(int i, EditingCapability editingCapability) {
        HiLog.debug(TAG, "subscribeEditingText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean unsubscribeEditingText(int i) {
        HiLog.debug(TAG, "unsubscribeEditingText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean deleteBackward(int i) {
        HiLog.debug(TAG, "deleteBackward in DefaultInputDataChannelImpl begin.", new Object[0]);
        if (i < 0) {
            HiLog.debug(TAG, "Input parameter is invalid.", new Object[0]);
            return false;
        }
        EditingText subscribeEditingText = subscribeEditingText(new EditingCapability());
        if (subscribeEditingText == null) {
            HiLog.error(TAG, "deleteBackward fail", new Object[0]);
            return false;
        }
        String textContent = subscribeEditingText.getTextContent();
        if (textContent == null) {
            HiLog.error(TAG, "deleteBackward fail", new Object[0]);
            return false;
        }
        int selectionStart = subscribeEditingText.getSelectionStart();
        int selectionEnd = subscribeEditingText.getSelectionEnd();
        if (selectionStart <= selectionEnd) {
            selectionStart = selectionEnd;
        }
        StringBuilder sb = new StringBuilder(textContent);
        if (i > selectionStart) {
            sb.delete(selectionStart, textContent.length());
        } else {
            sb.delete(selectionStart, i + selectionStart);
        }
        subscribeEditingText.setTextContent(sb.toString());
        HiLog.debug(TAG, "deleteBackward end", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean deleteForward(int i) {
        HiLog.debug(TAG, "deleteForward in DefaultInputDataChannelImpl begin.", new Object[0]);
        if (i <= 0) {
            HiLog.debug(TAG, "Input parameter is invalid.", new Object[0]);
            return false;
        }
        EditingText subscribeEditingText = subscribeEditingText(new EditingCapability());
        if (subscribeEditingText == null) {
            HiLog.error(TAG, "deleteForward fail", new Object[0]);
            return false;
        }
        String textContent = subscribeEditingText.getTextContent();
        if (textContent == null) {
            HiLog.error(TAG, "deleteForward fail", new Object[0]);
            return false;
        }
        int selectionStart = subscribeEditingText.getSelectionStart();
        int selectionEnd = subscribeEditingText.getSelectionEnd();
        if (selectionStart >= selectionEnd) {
            selectionStart = selectionEnd;
        }
        StringBuilder sb = new StringBuilder(textContent);
        if (i > selectionStart) {
            sb.delete(0, selectionStart);
        } else {
            sb.delete(selectionStart - i, selectionStart);
        }
        subscribeEditingText.setTextContent(sb.toString());
        HiLog.debug(TAG, "deleteForward end", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean sendCustomizedData(String str, PacMap pacMap) {
        HiLog.debug(TAG, "sendCustomizedData.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean sendKeyFunction(int i) {
        HiLog.debug(TAG, "sendKeyFunction in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean selectText(int i, int i2) {
        HiLog.debug(TAG, "selectText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean subscribeCaretContext(int i) {
        HiLog.debug(TAG, "subscribeCaretContext in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public int getAutoCapitalizeMode(int i) {
        HiLog.debug(TAG, "getAutoCapitalizeMode in DefaultInputDataChannelImpl begin.", new Object[0]);
        return 0;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean clearNoncharacterKeyState(int i) {
        HiLog.debug(TAG, "clearNoncharacterKeyState in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean recommendText(RecommendationInfo recommendationInfo) {
        HiLog.debug(TAG, "recommendText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return recommendationInfo != null;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean reviseText(int i, String str, String str2) {
        HiLog.debug(TAG, "reviseText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public boolean sendMenuFunction(int i) {
        HiLog.debug(TAG, "sendMenuFunction in DefaultInputDataChannelImpl begin.", new Object[0]);
        return true;
    }

    @Override // ohos.miscservices.inputmethod.InputDataChannel
    public String getSelectedText(int i) {
        HiLog.debug(TAG, "getSelectedText in DefaultInputDataChannelImpl begin.", new Object[0]);
        return "";
    }
}
