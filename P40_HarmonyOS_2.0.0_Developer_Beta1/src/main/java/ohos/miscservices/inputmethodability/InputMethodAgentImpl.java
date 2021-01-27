package ohos.miscservices.inputmethodability;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.internal.InputMethodAgentSkeleton;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.rpc.RemoteException;

public class InputMethodAgentImpl extends InputMethodAgentSkeleton {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodAgentImpl");
    private InputMethodEngine mEngine;

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void sendRecommendationInfo(RecommendationInfo[] recommendationInfoArr) throws RemoteException {
    }

    public InputMethodAgentImpl(InputMethodEngine inputMethodEngine) {
        super("AbstractInputMethodAgent");
        this.mEngine = inputMethodEngine;
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifySelectionChanged(int i, int i2, int i3, int i4) throws RemoteException {
        this.mEngine.notifySelectionChanged(i, i2, i3, i4);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyEditingTextChanged(int i, EditingText editingText) throws RemoteException {
        this.mEngine.notifyEditingTextChanged(i, editingText);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyCursorCoordinateChanged(float f, float f2, float f3, float[] fArr) throws RemoteException {
        this.mEngine.notifyCursorCoordinateChanged(f, f2, f3, fArr);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public boolean dispatchMultimodalEvent(MultimodalEvent multimodalEvent) throws RemoteException {
        if (multimodalEvent instanceof KeyEvent) {
            return this.mEngine.dispatchKeyBoardEvent((KeyEvent) multimodalEvent);
        }
        HiLog.debug(TAG, "Not supported event type", new Object[0]);
        return false;
    }
}
