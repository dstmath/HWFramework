package ohos.miscservices.inputmethodability.implement;

import ohos.app.Context;
import ohos.miscservices.inputmethodability.InputMethodEngine;
import ohos.miscservices.inputmethodability.adapter.InputMethodAdapter;
import ohos.miscservices.inputmethodability.interfaces.IInputMethodInterfaceAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public class InputMethodInterfaceAdapterProxy implements IInputMethodInterfaceAdapter {
    private static final int COMMAND_ON_ABILITY_CONNECTED = 1;
    private static final int ERR_OK = 0;
    private InputMethodAdapter inputMethodAdapter;
    private final IRemoteObject remote;

    public InputMethodInterfaceAdapterProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethodability.interfaces.IInputMethodInterfaceAdapter
    public IRemoteObject onAbilityConnected(Context context, InputMethodEngine inputMethodEngine) throws RemoteException {
        this.inputMethodAdapter = new InputMethodAdapter(context, inputMethodEngine);
        return this.inputMethodAdapter.createInputMethodCoreImpl();
    }
}
