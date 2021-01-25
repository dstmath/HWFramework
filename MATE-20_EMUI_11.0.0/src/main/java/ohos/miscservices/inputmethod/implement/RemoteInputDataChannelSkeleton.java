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
        MultimodalEvent multimodalEvent;
        switch (i) {
            case 1:
                try {
                    boolean insertText = insertText(messageParcel.readString());
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(insertText ? 1 : 0);
                    return true;
                } catch (RemoteException unused) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 2:
                try {
                    boolean insertRichContent = insertRichContent(RichContent.CREATOR.createFromParcel(messageParcel));
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(insertRichContent ? 1 : 0);
                    return true;
                } catch (RemoteException unused2) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 3:
                try {
                    boolean deleteBackward = deleteBackward(messageParcel.readInt());
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(deleteBackward ? 1 : 0);
                    return true;
                } catch (RemoteException unused3) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 4:
                try {
                    boolean deleteForward = deleteForward(messageParcel.readInt());
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(deleteForward ? 1 : 0);
                    return true;
                } catch (RemoteException unused4) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 5:
                try {
                    String forward = getForward(messageParcel.readInt());
                    messageParcel2.writeInt(0);
                    messageParcel2.writeString(forward);
                    return true;
                } catch (RemoteException unused5) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 6:
                try {
                    String backward = getBackward(messageParcel.readInt());
                    messageParcel2.writeInt(0);
                    messageParcel2.writeString(backward);
                    return true;
                } catch (RemoteException unused6) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 7:
                try {
                    boolean markText = markText(messageParcel.readInt(), messageParcel.readInt());
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(markText ? 1 : 0);
                    return true;
                } catch (RemoteException unused7) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 8:
                try {
                    boolean unmarkText = unmarkText();
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(unmarkText ? 1 : 0);
                    return true;
                } catch (RemoteException unused8) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 9:
                try {
                    boolean replaceMarkedText = replaceMarkedText(messageParcel.readString());
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(replaceMarkedText ? 1 : 0);
                    return true;
                } catch (RemoteException unused9) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 10:
                EditingCapability editingCapability = new EditingCapability();
                editingCapability.unmarshalling(messageParcel);
                try {
                    EditingText subscribeEditingText = subscribeEditingText(editingCapability);
                    messageParcel2.writeInt(0);
                    if (subscribeEditingText != null) {
                        subscribeEditingText.marshalling(messageParcel2);
                    }
                    return true;
                } catch (RemoteException unused10) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 11:
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
                } catch (RemoteException unused11) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 12:
                try {
                    close();
                    messageParcel2.writeInt(0);
                    return true;
                } catch (RemoteException unused12) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 13:
                PacMap pacMap = new PacMap();
                String readString = messageParcel.readString();
                pacMap.unmarshalling(messageParcel);
                try {
                    boolean sendCustomizedData = sendCustomizedData(readString, pacMap);
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(sendCustomizedData ? 1 : 0);
                    return true;
                } catch (RemoteException unused13) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 14:
                try {
                    boolean sendKeyFunction = sendKeyFunction(messageParcel.readInt());
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(sendKeyFunction ? 1 : 0);
                    return true;
                } catch (RemoteException unused14) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            case 15:
                try {
                    boolean selectText = selectText(messageParcel.readInt(), messageParcel.readInt());
                    messageParcel2.writeInt(0);
                    messageParcel2.writeInt(selectText ? 1 : 0);
                    return true;
                } catch (RemoteException unused15) {
                    messageParcel2.writeInt(-1);
                    return true;
                }
            default:
                return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
    }
}
