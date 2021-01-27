package ohos.miscservices.inputmethod;

import ohos.multimodalinput.event.KeyEvent;
import ohos.utils.PacMap;

public interface InputDataChannel {
    public static final int EDITING_TEXT_MONITOR = 1;
    public static final int NOTIFY_CURSOR_CONTEXT_IMMEDIATE = 1;
    public static final int NOTIFY_CURSOR_CONTEXT_MONITOR = 2;

    boolean clearNoncharacterKeyState(int i);

    void close();

    boolean deleteBackward(int i);

    boolean deleteForward(int i);

    int getAutoCapitalizeMode(int i);

    String getBackward(int i);

    String getForward(int i);

    String getSelectedText(int i);

    boolean insertRichContent(RichContent richContent);

    boolean insertText(String str);

    boolean lock();

    boolean markText(int i, int i2);

    boolean recommendText(RecommendationInfo recommendationInfo);

    boolean replaceMarkedText(String str);

    default boolean requestCurrentCursorContext() {
        return false;
    }

    boolean reviseText(int i, String str, String str2);

    boolean selectText(int i, int i2);

    boolean sendCustomizedData(String str, PacMap pacMap);

    boolean sendKeyEvent(KeyEvent keyEvent);

    boolean sendKeyFunction(int i);

    boolean sendMenuFunction(int i);

    boolean subscribeCaretContext(int i);

    default boolean subscribeCursorContext() {
        return false;
    }

    EditingText subscribeEditingText(EditingCapability editingCapability);

    default boolean subscribeEditingText(int i, EditingCapability editingCapability) {
        return false;
    }

    boolean unlock();

    boolean unmarkText();

    default boolean unsubscribeCursorContext() {
        return false;
    }

    default boolean unsubscribeEditingText(int i) {
        return false;
    }

    default EditingText getEditingText(int i, EditingCapability editingCapability) {
        return new EditingText();
    }
}
