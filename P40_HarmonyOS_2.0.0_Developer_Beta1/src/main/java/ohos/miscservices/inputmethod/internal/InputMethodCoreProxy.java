package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditorAttribute;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class InputMethodCoreProxy implements IInputMethodCore {
    private static final int COMMAND_CREATE_AGENT = 2;
    private static final int COMMAND_HIDE_KEYBOARD = 5;
    private static final int COMMAND_INITIALIZE_INPUT = 1;
    private static final int COMMAND_SHOW_KEYBOARD = 4;
    private static final int COMMAND_START_INPUT = 3;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputMethodCore";
    private static final int ERR_OK = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodCoreProxy");
    private static final int WRITE_MSG_ERROR = -2;
    private final IRemoteObject remote;

    public InputMethodCoreProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
    public void initializeInput(IRemoteObject iRemoteObject, int i, IRemoteObject iRemoteObject2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "initializeInput writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeRemoteObject(iRemoteObject);
        obtain.writeInt(i);
        obtain.writeRemoteObject(iRemoteObject2);
        try {
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                obtain.reclaim();
                obtain2.reclaim();
                return;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
    public void createAgent(IRemoteObject iRemoteObject) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "createAgent writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeRemoteObject(iRemoteObject);
        try {
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                obtain.reclaim();
                obtain2.reclaim();
                return;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
    public boolean startInput(IRemoteObject iRemoteObject, EditorAttribute editorAttribute, IRemoteObject iRemoteObject2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "startInput writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeRemoteObject(iRemoteObject);
        boolean z = true;
        if (editorAttribute != null) {
            obtain.writeInt(1);
            editorAttribute.marshalling(obtain);
        } else {
            obtain.writeInt(0);
        }
        obtain.writeRemoteObject(iRemoteObject2);
        try {
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
    public boolean showKeyboard(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "showKeyboard writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodCore
    public boolean hideKeyboard(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "hideKeyboard writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(5, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                boolean z = true;
                if (obtain2.readInt() != 1) {
                    z = false;
                }
                obtain.reclaim();
                obtain2.reclaim();
                return z;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }
}
