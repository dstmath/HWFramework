package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.IRemoteInputMethodAgent;
import ohos.miscservices.inputmethod.implement.InputMethodAgent;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class InputMethodAgentSkeleton extends RemoteObject implements IInputMethodAgent {
    private static final String DESCRIPTOR = "InputMethodAgentSkeleton";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, DESCRIPTOR);
    private static InputMethodAgent inputMethodAgent;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public InputMethodAgentSkeleton(String str) {
        super(str);
    }

    public static IInputMethodAgent asInterface(IRemoteObject iRemoteObject) {
        InputMethodAgent inputMethodAgent2;
        if (iRemoteObject == null) {
            synchronized (InputMethodAgentSkeleton.class) {
                if (inputMethodAgent == null) {
                    inputMethodAgent = new InputMethodAgent(null);
                }
                inputMethodAgent2 = inputMethodAgent;
            }
            return inputMethodAgent2;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new InputMethodAgentProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IRemoteInputMethodAgent) {
            return (IInputMethodAgent) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        HiLog.info(TAG, "onRemoteRequest remote request code=%{public}d", Integer.valueOf(i));
        super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        return true;
    }
}
