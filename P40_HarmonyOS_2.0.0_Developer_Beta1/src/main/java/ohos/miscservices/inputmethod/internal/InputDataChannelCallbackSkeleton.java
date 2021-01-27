package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingText;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class InputDataChannelCallbackSkeleton extends RemoteObject implements IInputDataChannelCallback {
    private static final int COMMAND_NOTIFY_CARET_CONTEXT_SUBSCRIPTION = 6;
    private static final int COMMAND_NOTIFY_EDITING_TEXT = 3;
    private static final int COMMAND_NOTIFY_INSERT_RICH_CONTENT = 7;
    private static final int COMMAND_SET_AUTO_CAPITALIZE_MODE = 4;
    private static final int COMMAND_SET_BACKWARD = 2;
    private static final int COMMAND_SET_FORWARD = 1;
    private static final int COMMAND_SET_SELECTED_TEXT = 5;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputDataChannelCallback";
    private static final int READ_MSG_ERROR = -3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputDataChannelCallbackSkeleton");

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public InputDataChannelCallbackSkeleton(String str) {
        super(str);
    }

    public static IInputDataChannelCallback asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new InputDataChannelCallbackProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IInputDataChannelCallback) {
            return (IInputDataChannelCallback) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        switch (i) {
            case 1:
                HiLog.info(TAG, "COMMAND_SET_FORWARD", new Object[0]);
                return transactToSetForward(messageParcel, messageParcel2);
            case 2:
                HiLog.info(TAG, "COMMAND_SET_BACKWARD", new Object[0]);
                return transactToSetBackward(messageParcel, messageParcel2);
            case 3:
                HiLog.info(TAG, "COMMAND_NOTIFY_EDITING_TEXT", new Object[0]);
                return transactToNotifyEditingText(messageParcel, messageParcel2);
            case 4:
                HiLog.info(TAG, "COMMAND_SET_AUTO_CAPITALIZE_MODE", new Object[0]);
                return transactToSetAutoCapitalizeMode(messageParcel, messageParcel2);
            case 5:
                HiLog.info(TAG, "COMMAND_SET_SELECTED_TEXT", new Object[0]);
                return transactToSetSelectedText(messageParcel, messageParcel2);
            case 6:
                HiLog.info(TAG, "COMMAND_NOTIFY_CARET_CONTEXT_SUBSCRIPTION", new Object[0]);
                return transactToNotifySubscribeCaretContextResult(messageParcel, messageParcel2);
            case 7:
                HiLog.info(TAG, "COMMAND_NOTIFY_INSERT_RICH_CONTENT", new Object[0]);
                return transactToNotifyInsertRichContentResult(messageParcel, messageParcel2);
            default:
                return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
    }

    private boolean transactToSetForward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "setForward readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            setForward(messageParcel.readString());
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setForward RemoteException", new Object[0]);
            return true;
        }
    }

    private boolean transactToSetBackward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "setBackward readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            setBackward(messageParcel.readString());
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setBackward RemoteException", new Object[0]);
            return true;
        }
    }

    private boolean transactToNotifyEditingText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "notifyEditingText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        EditingText editingText = new EditingText();
        if (messageParcel.readInt() != 0) {
            editingText.unmarshalling(messageParcel);
        }
        try {
            notifyEditingText(editingText);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "notifyEditingText RemoteException", new Object[0]);
            return true;
        }
    }

    private boolean transactToSetAutoCapitalizeMode(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "setAutoCapitalizeMode readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            setAutoCapitalizeMode(messageParcel.readInt());
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setAutoCapitalizeMode RemoteException", new Object[0]);
            return true;
        }
    }

    private boolean transactToSetSelectedText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "setSelectedText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            setSelectedText(messageParcel.readString());
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setSelectedText RemoteException", new Object[0]);
            return true;
        }
    }

    private boolean transactToNotifySubscribeCaretContextResult(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "notifySubscribeCaretContextResult readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            notifySubscribeCaretContextResult(messageParcel.readBoolean());
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "notifyCaretContextSubscriptionResult RemoteException", new Object[0]);
            return true;
        }
    }

    private boolean transactToNotifyInsertRichContentResult(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "notifyInsertRichContentResult readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            notifyInsertRichContentResult(messageParcel.readBoolean());
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "notifyInsertRichContentResult RemoteException", new Object[0]);
            return true;
        }
    }
}
