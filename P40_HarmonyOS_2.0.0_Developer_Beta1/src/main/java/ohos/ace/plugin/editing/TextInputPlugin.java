package ohos.ace.plugin.editing;

import com.huawei.ace.plugin.editing.TextEditState;
import com.huawei.ace.plugin.editing.TextInputAction;
import com.huawei.ace.plugin.editing.TextInputConfiguration;
import com.huawei.ace.plugin.editing.TextInputPluginBase;
import com.huawei.ace.plugin.editing.TextInputType;
import com.huawei.ace.runtime.ALog;
import ohos.miscservices.inputmethod.InputAttribute;
import ohos.miscservices.inputmethod.InputMethodController;

public class TextInputPlugin extends TextInputPluginBase {
    private static final String LOG_TAG = "Ace_IME";
    private EditableText editable;
    private final InputMethodController imc = InputMethodController.getInstance();
    private boolean isInputTypeChanged = false;
    private int lastInputType = 1;
    private final Listener listener;
    private boolean restartInputPending = false;

    /* access modifiers changed from: package-private */
    public interface Listener {
        void onClosed(int i);

        void onInited(int i, EditableText editableText, InputAttribute inputAttribute);
    }

    public TextInputPlugin(Listener listener2) {
        this.listener = listener2;
    }

    @Override // com.huawei.ace.plugin.editing.TextInputPluginBase
    public void showTextInput(boolean z) {
        InputMethodController inputMethodController = this.imc;
        if (inputMethodController == null) {
            return;
        }
        if (this.isInputTypeChanged) {
            this.isInputTypeChanged = false;
            inputMethodController.startInput(1, true);
            return;
        }
        inputMethodController.startInput(1, z);
    }

    @Override // com.huawei.ace.plugin.editing.TextInputPluginBase
    public void hideTextInput() {
        InputMethodController inputMethodController = this.imc;
        if (inputMethodController != null) {
            inputMethodController.stopInput(1);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.ace.plugin.editing.TextInputPluginBase
    public void onSetTextEditingState(TextEditState textEditState) {
        if (this.editable == null) {
            ALog.w(LOG_TAG, "Editable is null, set text editing state failed.");
        } else if (this.restartInputPending || !textEditState.getText().equals(this.editable.getContent())) {
            this.restartInputPending = false;
            this.editable.replace(textEditState.getText(), textEditState.getSelectionStart(), textEditState.getSelectionEnd());
        } else {
            this.editable.setSelection(textEditState.getSelectionStart(), textEditState.getSelectionEnd());
            InputMethodController inputMethodController = this.imc;
            if (inputMethodController != null) {
                inputMethodController.notifySelectionChanged(this.editable.getSelectionStart(), this.editable.getSelectionEnd());
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.ace.plugin.editing.TextInputPluginBase
    public void onInited() {
        this.editable = new EditableText();
        this.restartInputPending = true;
        if (this.listener != null) {
            InputAttribute createInputAttribute = createInputAttribute(getConfiguration());
            if (createInputAttribute.getInputPattern() != this.lastInputType) {
                this.isInputTypeChanged = true;
                this.lastInputType = createInputAttribute.getInputPattern();
            }
            this.listener.onInited(clientId(), this.editable, createInputAttribute);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.ace.plugin.editing.TextInputPluginBase
    public void onClosed() {
        Listener listener2 = this.listener;
        if (listener2 != null) {
            listener2.onClosed(clientId());
        }
    }

    private InputAttribute createInputAttribute(TextInputConfiguration textInputConfiguration) {
        InputAttribute inputAttribute = new InputAttribute();
        if (textInputConfiguration != null) {
            inputAttribute.setInputPattern(inputTypeFromTextInputType(textInputConfiguration.getType()));
            inputAttribute.setInputOption(16);
            if (textInputConfiguration.getType() == TextInputType.MULTILINE) {
                inputAttribute.setInputOption(inputAttribute.getInputOption() | 1);
            }
            if (textInputConfiguration.getAction() != TextInputAction.UNSPECIFIED) {
                inputAttribute.setEnterKeyType(convertInputAction(textInputConfiguration.getAction()));
            }
        }
        return inputAttribute;
    }

    private static int convertInputAction(TextInputAction textInputAction) {
        switch (textInputAction) {
            case GO:
                return 2;
            case SEARCH:
                return 1;
            case SEND:
                return 3;
            case NEXT:
                return 5;
            case DONE:
                return 4;
            case PREVIOUS:
                return 6;
            default:
                return 0;
        }
    }

    protected static int inputTypeFromTextInputType(TextInputType textInputType) {
        switch (textInputType) {
            case DATETIME:
                return 4;
            case NUMBER:
                return 2;
            case PHONE:
                return 3;
            case EMAIL_ADDRESS:
                return 6;
            case URL:
                return 5;
            case VISIBLE_PASSWORD:
                return 7;
            default:
                return 1;
        }
    }
}
