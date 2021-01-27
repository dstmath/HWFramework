package ohos.miscservices.inputmethod.adapter;

import ohos.rpc.RemoteException;

public class InputMethodManagerAdapterControlImp extends InputMethodManagerAdapterControlSkeleton {
    public InputMethodManagerAdapterControlImp(String str) {
        super(str);
    }

    @Override // ohos.miscservices.inputmethod.adapter.IInputMethodManagerAdapterControl
    public boolean notifyClientDisconnect() throws RemoteException {
        InputMethodManagerAdapter.getInstance().stopRemoteInput();
        return true;
    }
}
