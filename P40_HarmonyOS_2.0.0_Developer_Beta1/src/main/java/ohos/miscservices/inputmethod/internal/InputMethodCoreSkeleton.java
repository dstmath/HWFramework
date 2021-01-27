package ohos.miscservices.inputmethod.internal;

import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditorAttribute;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class InputMethodCoreSkeleton extends RemoteObject implements IInputMethodCore {
    private static final int COMMAND_CREATE_AGENT = 2;
    private static final int COMMAND_HIDE_KEYBOARD = 5;
    private static final int COMMAND_INITIALIZE_INPUT = 1;
    private static final int COMMAND_SHOW_KEYBOARD = 4;
    private static final int COMMAND_START_INPUT = 3;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputMethodCore";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;
    private static final int READ_MSG_ERROR = -3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodCoreSkeleton");

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public InputMethodCoreSkeleton(String str) {
        super(str);
    }

    public static Optional<IInputMethodCore> asInterface(IRemoteObject iRemoteObject) {
        Object obj;
        if (iRemoteObject == null) {
            return Optional.empty();
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            obj = new InputMethodCoreProxy(iRemoteObject);
        } else if (!(queryLocalInterface instanceof IInputMethodCore)) {
            return Optional.empty();
        } else {
            obj = (IInputMethodCore) queryLocalInterface;
        }
        return Optional.of(obj);
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i == 1) {
            HiLog.debug(TAG, "COMMAND_INITIALIZE_INPUT", new Object[0]);
            return transactToInitializeInput(messageParcel, messageParcel2);
        } else if (i == 2) {
            HiLog.debug(TAG, "COMMAND_CREATE_AGENT", new Object[0]);
            return transactToCreateAgent(messageParcel, messageParcel2);
        } else if (i == 3) {
            HiLog.debug(TAG, "COMMAND_START_INPUT", new Object[0]);
            return transactToStartInput(messageParcel, messageParcel2);
        } else if (i == 4) {
            HiLog.debug(TAG, "COMMAND_SHOW_KEYBOARD", new Object[0]);
            return transactToShowKeyboard(messageParcel, messageParcel2);
        } else if (i != 5) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            HiLog.debug(TAG, "COMMAND_HIDE_KEYBOARD", new Object[0]);
            return transactToHideKeyboard(messageParcel, messageParcel2);
        }
    }

    private boolean transactToInitializeInput(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "initializeInput readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            initializeInput(messageParcel.readRemoteObject(), messageParcel.readInt(), messageParcel.readRemoteObject());
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToCreateAgent(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "createAgent readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            createAgent(messageParcel.readRemoteObject());
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToStartInput(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "startInput readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        IRemoteObject readRemoteObject = messageParcel.readRemoteObject();
        EditorAttribute editorAttribute = new EditorAttribute();
        if (messageParcel.readInt() != 0) {
            editorAttribute.unmarshalling(messageParcel);
        } else {
            HiLog.warn(TAG, "startInput InputAttribute unmarshalling error.", new Object[0]);
        }
        try {
            boolean startInput = startInput(readRemoteObject, editorAttribute, messageParcel.readRemoteObject());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(startInput ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToShowKeyboard(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "showKeyboard readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean showKeyboard = showKeyboard(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(showKeyboard ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToHideKeyboard(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "hideKeyboard readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean hideKeyboard = hideKeyboard(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(hideKeyboard ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }
}
