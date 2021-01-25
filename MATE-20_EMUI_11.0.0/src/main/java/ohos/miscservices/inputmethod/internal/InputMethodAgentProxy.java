package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class InputMethodAgentProxy implements IInputMethodAgent {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodAgentProxy");
    private final IRemoteObject remote;

    public InputMethodAgentProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifySelectionChanged(int i, int i2, int i3, int i4) throws RemoteException {
        HiLog.debug(TAG, "notifySelectionChanged", new Object[0]);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void sendRecommendationInfo(RecommendationInfo[] recommendationInfoArr) throws RemoteException {
        HiLog.debug(TAG, "sendRecommendationInfo", new Object[0]);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyEditingTextChanged(int i, EditingText editingText) throws RemoteException {
        HiLog.debug(TAG, "notifyEditingTextChanged", new Object[0]);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyCaretCoordinateChanged(float f, float f2, float[] fArr) throws RemoteException {
        HiLog.debug(TAG, "notifyCaretCoordinateChanged", new Object[0]);
    }
}
