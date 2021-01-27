package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.net.Uri;

public abstract class IInputControlChannelSkeleton extends RemoteObject implements IInputControlChannel {
    private static final int COMMAND_CREATE_URI_PERMISSION = 4;
    private static final int COMMAND_HIDE_KEY_BOARD_SELF = 1;
    private static final int COMMAND_REPORT_SCREEN_MODE = 3;
    private static final int COMMAND_SWITCH_TO_NEXT_INPUTMETHOD = 2;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputControlChannel";
    private static final int ERR_OK = 0;
    private static final int READ_MSG_ERROR = -2;
    private static final int REMOTE_ERR = -3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "IInputControlChannelSkeleton");

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public IInputControlChannelSkeleton(String str) {
        super(str);
    }

    public static IInputControlChannel asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new IInputControlChannelProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IInputControlChannel) {
            return (IInputControlChannel) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i == 1) {
            return processHideKeyboardSelf(messageParcel, messageParcel2);
        }
        if (i == 2) {
            return processToNextInputMethod(messageParcel, messageParcel2);
        }
        if (i == 3) {
            return processReportScreenMode(messageParcel, messageParcel2);
        }
        if (i != 4) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        return processCreateUriPermission(messageParcel, messageParcel2);
    }

    private boolean processHideKeyboardSelf(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.debug(TAG, "COMMAND_HIDE_KEY_BOARD_SELF", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "hideKeyboardSelf readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-2);
            return false;
        }
        try {
            hideKeyboardSelf(messageParcel.readInt());
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "hideKeyboardSelf RemoteException", new Object[0]);
            messageParcel2.writeInt(-3);
            return true;
        }
    }

    private boolean processToNextInputMethod(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.debug(TAG, "COMMAND_SWITCH_TO_NEXT_INPUTMETHOD", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "toNextInputMethod readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-2);
            return false;
        }
        try {
            boolean nextInputMethod = toNextInputMethod();
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(nextInputMethod ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "toNextInputMethod RemoteException", new Object[0]);
            messageParcel2.writeInt(-3);
            return true;
        }
    }

    private boolean processReportScreenMode(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.debug(TAG, "COMMAND_REPORT_SCREEN_MODE", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "reportScreenMode readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-2);
            return false;
        }
        try {
            reportScreenMode(messageParcel.readInt());
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "reportScreenMode RemoteException", new Object[0]);
            messageParcel2.writeInt(-3);
            return true;
        }
    }

    private boolean processCreateUriPermission(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.debug(TAG, "COMMAND_CREATE_URI_PERMISSION", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "createUriPermission readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-2);
            return false;
        }
        IRemoteObject iRemoteObject = null;
        try {
            IUriPermission createUriPermission = createUriPermission(messageParcel.readInt() != 0 ? Uri.readFromParcel(messageParcel) : null, messageParcel.readString());
            messageParcel2.writeInt(0);
            if (createUriPermission != null) {
                iRemoteObject = createUriPermission.asObject();
            }
            messageParcel2.writeRemoteObject(iRemoteObject);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "createUriPermission RemoteException", new Object[0]);
            messageParcel2.writeInt(-3);
            return true;
        }
    }
}
