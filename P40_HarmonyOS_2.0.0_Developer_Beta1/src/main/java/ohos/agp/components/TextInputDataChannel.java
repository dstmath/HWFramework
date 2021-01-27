package ohos.agp.components;

import ohos.aafwk.utils.log.LogDomain;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.InputDataChannel;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.RichContent;
import ohos.multimodalinput.event.KeyEvent;
import ohos.utils.PacMap;

public class TextInputDataChannel implements InputDataChannel {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_COMPONENT");

    public boolean lock() {
        return true;
    }

    public boolean unlock() {
        return true;
    }

    public TextInputDataChannel() {
        HiLog.debug(TAG, "TextInputDataChannel construct.", new Object[0]);
    }

    public boolean insertText(String str) {
        HiLog.debug(TAG, "insertText in TextInputDataChannel begin.", new Object[0]);
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

    public boolean insertRichContent(RichContent richContent) {
        HiLog.debug(TAG, "insertRichContent in TextInputDataChannel begin.", new Object[0]);
        return true;
    }

    public String getForward(int i) {
        int i2 = 0;
        HiLog.debug(TAG, "getForward in TextInputDataChannel begin", new Object[0]);
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

    public String getBackward(int i) {
        HiLog.debug(TAG, "getBackward in TextInputDataChannel begin", new Object[0]);
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

    public boolean sendKeyEvent(KeyEvent keyEvent) {
        HiLog.debug(TAG, "sendKeyEvent in TextInputDataChannel begin.", new Object[0]);
        return false;
    }

    public void close() {
        HiLog.debug(TAG, "close in TextInputDataChannel begin.", new Object[0]);
    }

    public boolean markText(int i, int i2) {
        HiLog.debug(TAG, "markText in TextInputDataChannel begin.", new Object[0]);
        return i <= i2 && i <= 8 && i2 <= 8 && i >= 0 && i2 >= 0;
    }

    public boolean unmarkText() {
        HiLog.debug(TAG, "unmarkText in TextInputDataChannel begin.", new Object[0]);
        return true;
    }

    public boolean replaceMarkedText(String str) {
        HiLog.debug(TAG, "replaceMarkedText in TextInputDataChannel begin.", new Object[0]);
        return true;
    }

    public EditingText subscribeEditingText(EditingCapability editingCapability) {
        HiLog.debug(TAG, "subscribeEditingText in TextInputDataChannel begin.", new Object[0]);
        if (editingCapability == null) {
            return new EditingText();
        }
        if (editingCapability.getMonitorFlag() != 0) {
            return new EditingText();
        }
        EditingText editingText = new EditingText();
        editingText.setTextContent("Empty String");
        editingText.setSelectionStart(1);
        editingText.setSelectionEnd(1);
        return editingText;
    }

    public boolean deleteBackward(int i) {
        HiLog.debug(TAG, "deleteBackward in TextInputDataChannel begin.", new Object[0]);
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

    public boolean deleteForward(int i) {
        HiLog.debug(TAG, "deleteForward in TextInputDataChannel begin.", new Object[0]);
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

    public boolean sendCustomizedData(String str, PacMap pacMap) {
        HiLog.debug(TAG, "sendCustomizedData.", new Object[0]);
        return true;
    }

    public boolean sendKeyFunction(int i) {
        HiLog.debug(TAG, "sendKeyFunction in TextInputDataChannel begin.", new Object[0]);
        return true;
    }

    public boolean selectText(int i, int i2) {
        HiLog.debug(TAG, "selectText in TextInputDataChannel begin.", new Object[0]);
        return true;
    }

    public boolean subscribeCaretContext(int i) {
        HiLog.debug(TAG, "subscribeCaretContext in TextInputDataChannel begin.", new Object[0]);
        return true;
    }

    public int getAutoCapitalizeMode(int i) {
        HiLog.debug(TAG, "getAutoCapitalizeMode in TextInputDataChannel begin.", new Object[0]);
        return 0;
    }

    public boolean clearNoncharacterKeyState(int i) {
        HiLog.debug(TAG, "clearNoncharacterKeyState in TextInputDataChannel begin.", new Object[0]);
        return true;
    }

    public boolean recommendText(RecommendationInfo recommendationInfo) {
        HiLog.debug(TAG, "recommendText in TextInputDataChannel begin.", new Object[0]);
        return recommendationInfo != null;
    }

    public boolean reviseText(int i, String str, String str2) {
        HiLog.debug(TAG, "reviseText in TextInputDataChannel begin.", new Object[0]);
        return true;
    }

    public boolean sendMenuFunction(int i) {
        HiLog.debug(TAG, "sendMenuFunction in TextInputDataChannel begin.", new Object[0]);
        return true;
    }

    public String getSelectedText(int i) {
        HiLog.debug(TAG, "getSelectedText in TextInputDataChannel begin.", new Object[0]);
        return "";
    }
}
