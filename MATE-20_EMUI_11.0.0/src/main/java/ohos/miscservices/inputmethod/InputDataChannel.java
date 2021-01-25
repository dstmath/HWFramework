package ohos.miscservices.inputmethod;

import ohos.multimodalinput.event.KeyEvent;
import ohos.utils.PacMap;

public interface InputDataChannel {
    public static final int EDITING_TEXT_MONITOR = 1;
    public static final int NOTIFY_CURSOR_CONTEXT_IMMEDIATE = 1;
    public static final int NOTIFY_CURSOR_CONTEXT_MONITOR = 16;

    boolean beginPendingText(int i, int i2);

    boolean clearNoncharacterKeyState(int i);

    void close();

    boolean deleteBackward(int i);

    boolean deleteForward(int i);

    boolean endPendingText();

    int getAutoCapitalizeMode(int i);

    String getBackward(int i);

    EditingText getEditingText(EditingTextRequest editingTextRequest, int i);

    String getForward(int i);

    String getSelectedText(int i);

    String getTextAfterCursor(int i);

    String getTextBeforeCursor(int i);

    boolean insertCompletionText(CompletionText completionText);

    boolean insertCorrectionText(CorrectionText correctionText);

    boolean insertRichContent(RichContent richContent);

    boolean insertText(String str);

    boolean lock();

    boolean lockEditableBuffer();

    boolean markText(int i, int i2);

    boolean performSpecialCommand(String str, PacMap pacMap);

    boolean recommendText(RecommendationInfo recommendationInfo);

    boolean replaceMarkedText(String str);

    boolean reviseText(int i, String str, String str2);

    boolean selectText(int i, int i2);

    boolean sendCustomizedData(String str, PacMap pacMap);

    boolean sendEditMenuAction(int i);

    boolean sendEditorAction(int i);

    boolean sendKeyEvent(KeyEvent keyEvent);

    boolean sendKeyFunction(int i);

    boolean sendMenuFunction(int i);

    boolean setPendingText(String str, int i);

    boolean setSelection(int i, int i2);

    boolean subscribeCaretContext(int i);

    boolean subscribeCursorContext(int i);

    EditingText subscribeEditingText(EditingCapability editingCapability);

    boolean unlock();

    boolean unlockEditableBuffer();

    boolean unmarkText();
}
