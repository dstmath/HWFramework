package ohos.ace.plugin.editing;

import com.huawei.ace.plugin.editing.TextInputAction;
import com.huawei.ace.plugin.editing.TextInputDelegate;
import com.huawei.ace.runtime.ALog;
import ohos.ace.plugin.editing.TextInputPlugin;
import ohos.agp.window.wmc.IAGPTextViewListener;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.InputAttribute;
import ohos.miscservices.inputmethod.InputMethodController;
import ohos.miscservices.inputmethod.adapter.EditorInfoAdapter;
import ohos.miscservices.inputmethod.adapter.InputParam;
import ohos.multimodalinput.event.KeyBoardEvent;
import ohos.multimodalinput.event.KeyEvent;

public class TextViewListener implements IAGPTextViewListener, TextInputPlugin.Listener {
    private static final String LOG_TAG = "Ace_IME";
    private int clientId = -1;
    private TextInputDelegate delegate;
    private EditableText editable = new EditableText();
    private final InputMethodController imc = InputMethodController.getInstance();
    private InputAttribute inputAttr = new InputAttribute();

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public boolean setComposingText(CharSequence charSequence, int i) {
        return false;
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public EditingText subscribeEditingText(EditingCapability editingCapability) {
        return null;
    }

    public TextViewListener(TextInputDelegate textInputDelegate) {
        this.delegate = textInputDelegate;
    }

    @Override // ohos.ace.plugin.editing.TextInputPlugin.Listener
    public void onInited(int i, EditableText editableText, InputAttribute inputAttribute) {
        this.clientId = i;
        this.editable = editableText;
        this.inputAttr = inputAttribute;
    }

    @Override // ohos.ace.plugin.editing.TextInputPlugin.Listener
    public void onClosed(int i) {
        this.clientId = i;
    }

    private void updateEditingState() {
        int selectionStart = this.editable.getSelectionStart();
        int selectionEnd = this.editable.getSelectionEnd();
        this.imc.notifySelectionChanged(selectionStart, selectionEnd);
        this.delegate.updateEditingState(this.clientId, this.editable.getContent(), selectionStart, selectionEnd, 0, 0);
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public boolean commitText(CharSequence charSequence, int i) {
        this.editable.commit(charSequence.toString(), i);
        updateEditingState();
        return false;
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public boolean deleteSurroundingText(int i, int i2) {
        ALog.d(LOG_TAG, "deleteSurroundingText before " + i + " after " + i2);
        this.editable.deleteSurroundingText(i, i2);
        updateEditingState();
        return true;
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public CharSequence getTextBeforeCursor(int i, int i2) {
        ALog.d(LOG_TAG, "getTextBeforeCursor count " + i + " with flags " + i2);
        int selectionEnd = this.editable.getSelectionEnd();
        return this.editable.getContent().substring(Math.min(clampIndexToEditableText(selectionEnd - i, this.editable), selectionEnd), selectionEnd);
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public CharSequence getTextAfterCursor(int i, int i2) {
        ALog.d(LOG_TAG, "getTextAfterCursor count " + i + " with flags " + i2);
        int selectionEnd = this.editable.getSelectionEnd();
        return this.editable.getContent().substring(selectionEnd, Math.max(clampIndexToEditableText(i + selectionEnd, this.editable), selectionEnd));
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public boolean setComposingRegion(int i, int i2) {
        ALog.d(LOG_TAG, "setComposingRegion start " + i + " end " + i2);
        this.editable.setComposingIndex(i, i2);
        return false;
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public boolean finishComposingText() {
        ALog.d(LOG_TAG, "finishComposingText");
        return false;
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public boolean performEditorAction(int i) {
        ALog.d(LOG_TAG, "performEditorAction " + i);
        TextInputDelegate textInputDelegate = this.delegate;
        if (textInputDelegate == null) {
            return false;
        }
        textInputDelegate.performAction(this.clientId, TextInputAction.of(Integer.valueOf(i)));
        return true;
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public boolean setSelection(int i, int i2) {
        ALog.d(LOG_TAG, "setSelection " + i + "," + i2);
        int length = this.editable.length();
        if (i >= 0 && i2 <= length && i <= i2) {
            this.editable.setSelection(i, i2);
            updateEditingState();
        }
        return false;
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public void closeConnection() {
        ALog.d(LOG_TAG, "closeConnection");
        finishComposingText();
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public boolean sendKeyEvent(KeyEvent keyEvent) {
        ALog.d(LOG_TAG, "isKeyDown & keycode: " + keyEvent.isKeyDown() + " , " + keyEvent.getKeyCode());
        if (keyEvent.isKeyDown()) {
            if (keyEvent.getKeyCode() == 2055) {
                int clampIndexToEditableText = clampIndexToEditableText(this.editable.getSelectionStart(), this.editable);
                int clampIndexToEditableText2 = clampIndexToEditableText(this.editable.getSelectionEnd(), this.editable);
                if (clampIndexToEditableText2 > clampIndexToEditableText) {
                    this.editable.setSelection(clampIndexToEditableText);
                    this.editable.delete(clampIndexToEditableText, clampIndexToEditableText2);
                    updateEditingState();
                    return true;
                } else if (clampIndexToEditableText > 0) {
                    reverseDeleteSelection(clampIndexToEditableText);
                    return true;
                } else {
                    ALog.w(LOG_TAG, "illegal selection.");
                }
            } else if (keyEvent.getKeyCode() == 2014) {
                int max = Math.max(this.editable.getSelectionStart() - 1, 0);
                setSelection(max, max);
                return true;
            } else if (keyEvent.getKeyCode() == 2015) {
                int min = Math.min(this.editable.getSelectionStart() + 1, this.editable.length());
                setSelection(min, min);
                return true;
            } else {
                enterCharacter(keyEvent);
                return true;
            }
        }
        return false;
    }

    private void reverseDeleteSelection(int i) {
        this.editable.deleteSurroundingTextInCodePoints(1, 0);
        updateEditingState();
    }

    private void enterCharacter(KeyEvent keyEvent) {
        int unicode;
        if ((keyEvent instanceof KeyBoardEvent) && (unicode = ((KeyBoardEvent) keyEvent).getUnicode()) != 0) {
            int max = Math.max(0, this.editable.getSelectionStart());
            int max2 = Math.max(0, this.editable.getSelectionEnd());
            if (max2 != max) {
                this.editable.delete(max, max2);
            }
            this.editable.append(String.valueOf((char) unicode), max);
            int i = max + 1;
            setSelection(i, i);
        }
    }

    private static int clampIndexToEditableText(int i, EditableText editableText) {
        return Math.max(0, Math.min(editableText.length(), i));
    }

    @Override // ohos.agp.window.wmc.IAGPTextViewListener
    public InputParam getEditorInputParam() {
        return convertToInputParam(this.inputAttr);
    }

    private InputParam convertToInputParam(InputAttribute inputAttribute) {
        InputParam inputParam = new InputParam(0, 0);
        inputParam.setInputType(EditorInfoAdapter.convertToInputType(inputAttribute.getInputPattern(), inputAttribute.getInputOption()));
        inputParam.setImeOption(EditorInfoAdapter.convertToImeOption(inputAttribute.getEnterKeyType(), inputAttribute.getInputOption()));
        return inputParam;
    }
}
