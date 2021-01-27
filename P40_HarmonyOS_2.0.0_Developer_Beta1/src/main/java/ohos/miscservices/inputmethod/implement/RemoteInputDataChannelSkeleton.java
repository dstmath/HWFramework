package ohos.miscservices.inputmethod.implement;

import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RichContent;
import ohos.miscservices.inputmethod.interfaces.IRemoteInputDataChannel;
import ohos.multimodalinput.event.KeyBoardEvent;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.PacMap;

public abstract class RemoteInputDataChannelSkeleton extends RemoteObject implements IRemoteInputDataChannel {
    private static final int COMMAND_CLOSE = 12;
    private static final int COMMAND_DELETE_BACKWARD = 3;
    private static final int COMMAND_DELETE_FORWARD = 4;
    private static final int COMMAND_GET_BACKWARD = 6;
    private static final int COMMAND_GET_FORWARD = 5;
    private static final int COMMAND_INSERT_RICH_CONTENT = 2;
    private static final int COMMAND_INSERT_TEXT = 1;
    private static final int COMMAND_MARK_TEXT = 7;
    private static final int COMMAND_REPLACE_MARKED_TEXT = 9;
    private static final int COMMAND_SELECT_TEXT = 15;
    private static final int COMMAND_SEND_CUSTOMIZED_DATA = 13;
    private static final int COMMAND_SEND_KEY_EVENT = 11;
    private static final int COMMAND_SEND_KEY_FUNCTION = 14;
    private static final int COMMAND_SUBSCRIBE_EDITING_TEXT = 10;
    private static final int COMMAND_UNMARK_TEXT = 8;
    private static final String DESCRIPTOR = "zidaneos.miscservices.inputmethod.interfaces.IInputDataChannelLocal";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "RemoteInputDataChannelSkeleton");

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public RemoteInputDataChannelSkeleton(String str) {
        super(str);
    }

    public static IRemoteInputDataChannel asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new RemoteInputDataChannelProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IRemoteInputDataChannel) {
            return (IRemoteInputDataChannel) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (i >= 1 && i <= 8) {
            return processTextOperation(i, messageParcel, messageParcel2);
        }
        if (i < 9 || i > 15) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        return processEventOperation(i, messageParcel, messageParcel2);
    }

    private boolean processTextOperation(int i, MessageParcel messageParcel, MessageParcel messageParcel2) {
        switch (i) {
            case 1:
                return processInsertText(messageParcel, messageParcel2);
            case 2:
                return processInsertRichContent(messageParcel, messageParcel2);
            case 3:
                return processDeleteBackward(messageParcel, messageParcel2);
            case 4:
                return processDeleteForward(messageParcel, messageParcel2);
            case 5:
                return processGetForward(messageParcel, messageParcel2);
            case 6:
                return processGetBackward(messageParcel, messageParcel2);
            case 7:
                return processMarkText(messageParcel, messageParcel2);
            case 8:
                return processUnmarkText(messageParcel2);
            default:
                HiLog.error(TAG, "processTextOperation: invalid code", new Object[0]);
                return true;
        }
    }

    private boolean processEventOperation(int i, MessageParcel messageParcel, MessageParcel messageParcel2) {
        switch (i) {
            case 9:
                return processReplaceMarkedText(messageParcel, messageParcel2);
            case 10:
                return processSubscribeEditingText(messageParcel, messageParcel2);
            case 11:
                return processSendKeyEvent(messageParcel, messageParcel2);
            case 12:
                return processClose(messageParcel2);
            case 13:
                return processSendCustomizedData(messageParcel, messageParcel2);
            case 14:
                return processSendKeyFunction(messageParcel, messageParcel2);
            case 15:
                return processSelectText(messageParcel, messageParcel2);
            default:
                HiLog.error(TAG, "processEventOperation: invalid code", new Object[0]);
                return true;
        }
    }

    private boolean processInsertText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            boolean insertText = insertText(messageParcel.readString());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(insertText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processInsertRichContent(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            boolean insertRichContent = insertRichContent(RichContent.PRODUCER.createFromParcel(messageParcel));
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(insertRichContent ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processDeleteBackward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            boolean deleteBackward = deleteBackward(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(deleteBackward ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processDeleteForward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            boolean deleteForward = deleteForward(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(deleteForward ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processGetForward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            String forward = getForward(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeString(forward);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processGetBackward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            String backward = getBackward(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeString(backward);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processMarkText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            boolean markText = markText(messageParcel.readInt(), messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(markText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processUnmarkText(MessageParcel messageParcel) {
        try {
            boolean unmarkText = unmarkText();
            messageParcel.writeInt(0);
            messageParcel.writeInt(unmarkText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel.writeInt(-1);
            return true;
        }
    }

    private boolean processReplaceMarkedText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            boolean replaceMarkedText = replaceMarkedText(messageParcel.readString());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(replaceMarkedText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processSubscribeEditingText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        EditingCapability editingCapability = new EditingCapability();
        editingCapability.unmarshalling(messageParcel);
        try {
            EditingText subscribeEditingText = subscribeEditingText(editingCapability);
            messageParcel2.writeInt(0);
            if (subscribeEditingText != null) {
                subscribeEditingText.marshalling(messageParcel2);
            }
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processClose(MessageParcel messageParcel) {
        try {
            close();
            messageParcel.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            messageParcel.writeInt(-1);
            return true;
        }
    }

    private boolean processSendCustomizedData(MessageParcel messageParcel, MessageParcel messageParcel2) {
        PacMap pacMap = new PacMap();
        String readString = messageParcel.readString();
        pacMap.unmarshalling(messageParcel);
        try {
            boolean sendCustomizedData = sendCustomizedData(readString, pacMap);
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(sendCustomizedData ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processSendKeyEvent(MessageParcel messageParcel, MessageParcel messageParcel2) {
        MultimodalEvent multimodalEvent;
        HiLog.debug(TAG, "Enter COMMAND_SEND_KEY_EVENT case: ", new Object[0]);
        Optional<MultimodalEvent> createEvent = MultimodalEventFactory.createEvent(messageParcel);
        KeyBoardEvent keyBoardEvent = null;
        if (createEvent.isPresent()) {
            multimodalEvent = createEvent.get();
        } else {
            HiLog.error(TAG, "optional is not present", new Object[0]);
            multimodalEvent = null;
        }
        if (multimodalEvent instanceof KeyBoardEvent) {
            keyBoardEvent = (KeyBoardEvent) multimodalEvent;
        } else {
            HiLog.error(TAG, "multimodalEvent is not the instance of KeyBoardEvent", new Object[0]);
        }
        try {
            boolean sendKeyEvent = sendKeyEvent(keyBoardEvent);
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(sendKeyEvent ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processSendKeyFunction(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            boolean sendKeyFunction = sendKeyFunction(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(sendKeyFunction ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean processSelectText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            boolean selectText = selectText(messageParcel.readInt(), messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(selectText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            messageParcel2.writeInt(-1);
            return true;
        }
    }
}
