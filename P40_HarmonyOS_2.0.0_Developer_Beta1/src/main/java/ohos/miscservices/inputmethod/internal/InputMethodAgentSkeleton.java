package ohos.miscservices.inputmethod.internal;

import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.IRemoteInputMethodAgent;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.implement.InputMethodAgent;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class InputMethodAgentSkeleton extends RemoteObject implements IInputMethodAgent {
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputMethodAgent";
    private static final int DISPATCH_EVENT = 31;
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;
    private static final int MATRIX_LEN = 9;
    private static final int NOTIFY_CURSOR_CHANGED = 4;
    private static final int NOTIFY_EDITING_TEXT_CHANGED = 2;
    private static final int NOTIFY_SELECTION_CHANGED = 1;
    private static final int READ_MSG_ERROR = -3;
    private static final int SEND_RECOMMENDATION_INFO = 3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodAgentSkeleton");
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
        if (i == 1) {
            return transactToNotifySelectionChanged(messageParcel, messageParcel2);
        }
        if (i == 2) {
            return transactToNotifyEditingTextChanged(messageParcel, messageParcel2);
        }
        if (i == 3) {
            return transactToSendRecommendationInfo(messageParcel, messageParcel2);
        }
        if (i == 4) {
            return transactToNotifyCursorCoordinateChanged(messageParcel, messageParcel2);
        }
        if (i != 31) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        return transactToDispatchMultimodalEvent(messageParcel, messageParcel2);
    }

    private boolean transactToNotifyEditingTextChanged(MessageParcel messageParcel, MessageParcel messageParcel2) {
        EditingText editingText;
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "notifyEditingTextChanged readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        int readInt = messageParcel.readInt();
        if (messageParcel.readBoolean()) {
            editingText = null;
        } else {
            EditingText editingText2 = new EditingText();
            editingText2.unmarshalling(messageParcel);
            editingText = editingText2;
        }
        try {
            notifyEditingTextChanged(readInt, editingText);
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToNotifySelectionChanged(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "notifySelectionChanged readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            notifySelectionChanged(messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt(), messageParcel.readInt());
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToSendRecommendationInfo(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "sendRecommendationInfo readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        RecommendationInfo[] recommendationInfoArr = null;
        if (!messageParcel.readBoolean()) {
            recommendationInfoArr = unmarshallingRecommendationInfos(messageParcel);
        }
        try {
            sendRecommendationInfo(recommendationInfoArr);
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToNotifyCursorCoordinateChanged(MessageParcel messageParcel, MessageParcel messageParcel2) {
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "notifyCursorCoordinateChanged readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        float readFloat = messageParcel.readFloat();
        float readFloat2 = messageParcel.readFloat();
        float readFloat3 = messageParcel.readFloat();
        int readInt = messageParcel.readInt();
        if (readInt != 9) {
            HiLog.error(TAG, "notifyCursorCoordinateChanged: matrix length is invalid.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        float[] fArr = new float[readInt];
        messageParcel.readFloatArray(fArr);
        try {
            notifyCursorCoordinateChanged(readFloat, readFloat2, readFloat3, fArr);
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToDispatchMultimodalEvent(MessageParcel messageParcel, MessageParcel messageParcel2) {
        Optional<MultimodalEvent> optional;
        HiLog.info(TAG, "dispatchMultimodalEvent", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "dispatchMultimodalEvent readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        if (messageParcel.readInt() != 0) {
            optional = MultimodalEventFactory.createEvent(messageParcel);
        } else {
            HiLog.warn(TAG, "dispatchMultimodalEvent MultimodalEvent unmarshalling error.", new Object[0]);
            optional = Optional.empty();
        }
        if (!optional.isPresent()) {
            HiLog.info(TAG, "dispatchMultimodalEvent, unmarshalling event failed", new Object[0]);
            return false;
        }
        try {
            messageParcel2.writeBoolean(dispatchMultimodalEvent(optional.get()));
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private RecommendationInfo[] unmarshallingRecommendationInfos(MessageParcel messageParcel) {
        int readInt = messageParcel.readInt();
        if (readInt > messageParcel.getSize() - messageParcel.getReadPosition()) {
            HiLog.error(TAG, "unmarshalling recommendationInfos failed, invalid size", new Object[0]);
            return new RecommendationInfo[0];
        }
        RecommendationInfo[] recommendationInfoArr = new RecommendationInfo[readInt];
        for (int i = 0; i < readInt; i++) {
            if (messageParcel.readBoolean()) {
                recommendationInfoArr[i] = null;
            } else {
                recommendationInfoArr[i] = new RecommendationInfo(0, "", 0, "");
                recommendationInfoArr[i].unmarshalling(messageParcel);
            }
        }
        return recommendationInfoArr;
    }
}
