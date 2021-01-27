package ohos.agp.window.wmc;

import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.RichContent;
import ohos.miscservices.inputmethod.adapter.InputParam;
import ohos.multimodalinput.event.KeyEvent;

public interface IAGPTextViewListener {
    public static final int DEFAULT_IME_OPTION = 0;
    public static final int DEFAULT_INPUT_TYPE = 0;

    default boolean beginBatchEdit() {
        return false;
    }

    default boolean clearMetaKeyStates(int i) {
        return false;
    }

    default void closeConnection() {
    }

    default boolean commitText(CharSequence charSequence, int i) {
        return false;
    }

    default boolean deleteSurroundingText(int i, int i2) {
        return false;
    }

    default boolean endBatchEdit() {
        return false;
    }

    default boolean finishComposingText() {
        return false;
    }

    default int getAutoCapitalizeMode(int i) {
        return 0;
    }

    default CharSequence getSelectedText(int i) {
        return "";
    }

    default CharSequence getTextAfterCursor(int i, int i2) {
        return "";
    }

    default CharSequence getTextBeforeCursor(int i, int i2) {
        return "";
    }

    default boolean insertRichContent(RichContent richContent) {
        return false;
    }

    default boolean performContextMenuAction(int i) {
        return false;
    }

    default boolean performEditorAction(int i) {
        return false;
    }

    default boolean recommendText(RecommendationInfo recommendationInfo) {
        return false;
    }

    default boolean requestCursorUpdates(int i) {
        return false;
    }

    default boolean reviseText(int i, String str, String str2) {
        return false;
    }

    default boolean sendKeyEvent(KeyEvent keyEvent) {
        return false;
    }

    default boolean setComposingRegion(int i, int i2) {
        return false;
    }

    default boolean setComposingText(CharSequence charSequence, int i) {
        return false;
    }

    default boolean setSelection(int i, int i2) {
        return false;
    }

    default EditingText subscribeEditingText(EditingCapability editingCapability) {
        EditingText editingText = new EditingText();
        editingText.setTextContent("");
        return editingText;
    }

    default InputParam getEditorInputParam() {
        return new InputParam(0, 0);
    }
}
