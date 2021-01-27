package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.net.Uri;

public class IInputControlChannelProxy implements IInputControlChannel {
    private static final int COMMAND_CREATE_URI_PERMISSION = 4;
    private static final int COMMAND_HIDE_KEY_BOARD_SELF = 1;
    private static final int COMMAND_REPORT_SCREEN_MODE = 3;
    private static final int COMMAND_SWITCH_TO_NEXT_INPUTMETHOD = 2;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputControlChannel";
    private static final int ERR_OK = 0;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "IInputControlChannelProxy");
    private static final int WRITE_MSG_ERROR = -2;
    private final IRemoteObject remote;

    public IInputControlChannelProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputControlChannel
    public void hideKeyboardSelf(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "hideKeyboardSelf writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeInt(i);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputControlChannel
    public boolean toNextInputMethod() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "toNextInputMethod writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        try {
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputControlChannel
    public void reportScreenMode(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "reportScreenMode writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputControlChannel
    public IUriPermission createUriPermission(Uri uri, String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "createUriPermission writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return null;
        }
        if (uri != null) {
            obtain.writeInt(1);
            obtain.writeSequenceable(uri);
        } else {
            obtain.writeInt(0);
        }
        obtain.writeString(str);
        try {
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                IUriPermission asInterface = UriPermissionSkeleton.asInterface(obtain2.readRemoteObject());
                obtain.reclaim();
                obtain2.reclaim();
                return asInterface;
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
