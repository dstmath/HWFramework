package com.huawei.ace.plugin.editing;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Selection;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import com.huawei.ace.plugin.editing.TextInputPluginBase;
import com.huawei.ace.runtime.AEventReport;
import com.huawei.ace.runtime.ALog;

public class TextInputPlugin extends TextInputPluginBase {
    private static final String LOG_TAG = "Ace_IME";
    private Editable editable;
    private final InputMethodManager imm;
    private InputConnection lastInputConnection;
    private final Handler mainHandler;
    private boolean restartInputPending = false;
    private final View view;

    public TextInputPlugin(View view2) {
        this.view = view2;
        Object systemService = view2.getContext().getSystemService("input_method");
        if (systemService instanceof InputMethodManager) {
            this.imm = (InputMethodManager) systemService;
        } else {
            AEventReport.sendComponentEvent(0);
            ALog.e(LOG_TAG, "Unable to get INPUT_METHOD_SERVICE");
            this.imm = null;
        }
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public InputMethodManager getInputMethodManager() {
        return this.imm;
    }

    public InputConnection createInputConnection(View view2, EditorInfo editorInfo) {
        int i;
        ALog.d(LOG_TAG, "createInputConnection");
        TextInputConfiguration configuration = getConfiguration();
        if (!hasClient() || configuration == null) {
            this.lastInputConnection = null;
            return null;
        }
        editorInfo.inputType = inputTypeFromTextInputType(configuration.getType(), configuration.isObscure());
        editorInfo.imeOptions = 33554432;
        if (configuration.getAction() != TextInputAction.UNSPECIFIED) {
            i = convertInputAction(configuration.getAction());
        } else {
            i = (editorInfo.inputType & 131072) != 0 ? 1 : 6;
        }
        if (configuration.getActionLabel() != null) {
            editorInfo.actionLabel = configuration.getActionLabel();
            editorInfo.actionId = i;
        }
        editorInfo.imeOptions |= i;
        InputConnectionAdaptor inputConnectionAdaptor = new InputConnectionAdaptor(view2, clientId(), new TextInputPluginBase.Delegate(), this.editable);
        editorInfo.initialSelStart = Selection.getSelectionStart(this.editable);
        editorInfo.initialSelEnd = Selection.getSelectionEnd(this.editable);
        this.lastInputConnection = inputConnectionAdaptor;
        return this.lastInputConnection;
    }

    private void runOnUIThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            this.mainHandler.post(runnable);
        }
    }

    @Override // com.huawei.ace.plugin.editing.TextInputPluginBase
    public void showTextInput(boolean z) {
        ALog.d(LOG_TAG, "showTextInput");
        runOnUIThread(new Runnable() {
            /* class com.huawei.ace.plugin.editing.$$Lambda$TextInputPlugin$EhWoSSYJrEfpcq7fgl0Zm7XPi_s */

            @Override // java.lang.Runnable
            public final void run() {
                TextInputPlugin.this.lambda$showTextInput$0$TextInputPlugin();
            }
        });
    }

    public /* synthetic */ void lambda$showTextInput$0$TextInputPlugin() {
        this.view.requestFocus();
        InputMethodManager inputMethodManager = this.imm;
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(this.view, 0);
        }
    }

    @Override // com.huawei.ace.plugin.editing.TextInputPluginBase
    public void hideTextInput() {
        ALog.d(LOG_TAG, "hideTextInput");
        runOnUIThread(new Runnable() {
            /* class com.huawei.ace.plugin.editing.$$Lambda$TextInputPlugin$YQm_m3Z6P269E59xdxDZM0WNOs4 */

            @Override // java.lang.Runnable
            public final void run() {
                TextInputPlugin.this.lambda$hideTextInput$1$TextInputPlugin();
            }
        });
    }

    public /* synthetic */ void lambda$hideTextInput$1$TextInputPlugin() {
        InputMethodManager inputMethodManager = this.imm;
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(this.view.getApplicationWindowToken(), 0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.ace.plugin.editing.TextInputPluginBase
    public void onInited() {
        this.editable = Editable.Factory.getInstance().newEditable("");
        this.restartInputPending = true;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.ace.plugin.editing.TextInputPluginBase
    public void onSetTextEditingState(TextEditState textEditState) {
        if (this.restartInputPending || !textEditState.getText().equals(this.editable.toString())) {
            Editable editable2 = this.editable;
            editable2.replace(0, editable2.length(), textEditState.getText());
            applyStateToSelection(textEditState);
            InputMethodManager inputMethodManager = this.imm;
            if (inputMethodManager != null) {
                inputMethodManager.restartInput(this.view);
            }
            this.restartInputPending = false;
            return;
        }
        applyStateToSelection(textEditState);
        InputMethodManager inputMethodManager2 = this.imm;
        if (inputMethodManager2 != null) {
            inputMethodManager2.updateSelection(this.view, Math.max(Selection.getSelectionStart(this.editable), 0), Math.max(Selection.getSelectionEnd(this.editable), 0), BaseInputConnection.getComposingSpanStart(this.editable), BaseInputConnection.getComposingSpanEnd(this.editable));
        }
    }

    private void applyStateToSelection(TextEditState textEditState) {
        int selectionStart = textEditState.getSelectionStart();
        int selectionEnd = textEditState.getSelectionEnd();
        if (selectionStart < 0 || selectionStart > this.editable.length() || selectionEnd < 0 || selectionEnd > this.editable.length()) {
            Selection.removeSelection(this.editable);
        } else {
            Selection.setSelection(this.editable, selectionStart, selectionEnd);
        }
    }

    private static int convertInputAction(TextInputAction textInputAction) {
        switch (textInputAction) {
            case NONE:
                return 1;
            case GO:
                return 2;
            case SEARCH:
                return 3;
            case SEND:
                return 4;
            case NEXT:
                return 5;
            case DONE:
                return 6;
            case PREVIOUS:
                return 7;
            default:
                return 0;
        }
    }

    private static int inputTypeFromTextInputType(TextInputType textInputType, boolean z) {
        if (textInputType == TextInputType.DATETIME) {
            return 4;
        }
        if (textInputType == TextInputType.NUMBER) {
            return 2;
        }
        if (textInputType == TextInputType.PHONE) {
            return 3;
        }
        ALog.d(LOG_TAG, "other text input type");
        int i = 1;
        if (textInputType == TextInputType.MULTILINE) {
            i = 131073;
        } else if (textInputType == TextInputType.EMAIL_ADDRESS) {
            i = 33;
        } else if (textInputType == TextInputType.URL) {
            i = 17;
        } else if (textInputType == TextInputType.VISIBLE_PASSWORD) {
            i = 145;
        } else {
            ALog.d(LOG_TAG, "only use class text type");
        }
        return z ? 524288 | i | 128 : i;
    }
}
