package ohos.miscservices.inputmethod.adapter;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.internal.InputMethodAgentSkeleton;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class InputMethodAgentImpl extends InputMethodAgentSkeleton {
    private static final String DESCRIPTOR = "InputMethodAgentSkeleton";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodAgentImpl");
    private static volatile InputMethodAgentImpl instance;
    private InputMethodAgentAdapter agentAdapter = InputMethodAgentAdapter.getInstance();

    private InputMethodAgentImpl(String str) {
        super(str);
    }

    public static InputMethodAgentImpl getInstance() {
        if (instance == null) {
            synchronized (InputMethodAgentImpl.class) {
                if (instance == null) {
                    instance = new InputMethodAgentImpl(DESCRIPTOR);
                }
            }
        }
        return instance;
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifySelectionChanged(int i, int i2, int i3, int i4) throws RemoteException {
        HiLog.debug(TAG, "notifySelectionChanged start.", new Object[0]);
        this.agentAdapter.notifySelectionChanged(i, i2, i3, i4);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void sendRecommendationInfo(RecommendationInfo[] recommendationInfoArr) throws RemoteException {
        HiLog.debug(TAG, "sendRecommendationInfo start.", new Object[0]);
        this.agentAdapter.sendRecommendationInfo(recommendationInfoArr);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyEditingTextChanged(int i, EditingText editingText) throws RemoteException {
        HiLog.debug(TAG, "notifyEditingTextChanged start.", new Object[0]);
        this.agentAdapter.notifyEditingTextChanged(i, editingText);
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyCaretCoordinateChanged(float f, float f2, float[] fArr) throws RemoteException {
        HiLog.debug(TAG, "notifyCaretCoordinateChanged start.", new Object[0]);
        this.agentAdapter.notifyCaretCoordinateChanged(f, f2, fArr);
    }

    @Override // ohos.miscservices.inputmethod.internal.InputMethodAgentSkeleton, ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
    }
}
