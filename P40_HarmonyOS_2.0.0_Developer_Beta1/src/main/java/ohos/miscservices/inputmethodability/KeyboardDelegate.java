package ohos.miscservices.inputmethodability;

import ohos.miscservices.inputmethod.EditingText;
import ohos.multimodalinput.event.KeyEvent;

public interface KeyboardDelegate {
    void onCursorContextChanged(float f, float f2, float f3);

    boolean onKeyDown(KeyEvent keyEvent);

    boolean onKeyUp(KeyEvent keyEvent);

    void onSelectionChanged(int i, int i2, int i3, int i4);

    void onTextChanged(int i, EditingText editingText);
}
