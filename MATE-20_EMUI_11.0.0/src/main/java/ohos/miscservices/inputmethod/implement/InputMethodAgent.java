package ohos.miscservices.inputmethod.implement;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.adapter.InputMethodAgentImpl;
import ohos.miscservices.inputmethod.internal.IInputMethodAgent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class InputMethodAgent implements IInputMethodAgent {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodAgent");
    private final InputMethodAgentImpl inputMethodAgentImpl = InputMethodAgentImpl.getInstance();
    private IRemoteObject remote;

    public InputMethodAgent(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifySelectionChanged(int i, int i2, int i3, int i4) throws RemoteException {
        HiLog.debug(TAG, "notifySelectionChanged begin.", new Object[0]);
        this.inputMethodAgentImpl.notifySelectionChanged(i, i2, i3, i4);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void sendRecommendationInfo(RecommendationInfo[] recommendationInfoArr) throws RemoteException {
        HiLog.debug(TAG, "sendRecommendationInfo begin.", new Object[0]);
        this.inputMethodAgentImpl.sendRecommendationInfo(recommendationInfoArr);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyEditingTextChanged(int i, EditingText editingText) throws RemoteException {
        HiLog.debug(TAG, "notifyEditingTextChanged begin.", new Object[0]);
        this.inputMethodAgentImpl.notifyEditingTextChanged(i, editingText);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyCaretCoordinateChanged(float f, float f2, float[] fArr) throws RemoteException {
        HiLog.debug(TAG, "notifyCaretCoordinateChanged begin.", new Object[0]);
        this.inputMethodAgentImpl.notifyCaretCoordinateChanged(f, f2, fArr);
    }
}
