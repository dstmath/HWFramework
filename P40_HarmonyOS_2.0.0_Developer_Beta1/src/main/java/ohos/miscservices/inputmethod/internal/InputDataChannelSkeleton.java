package ohos.miscservices.inputmethod.internal;

import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.RichContent;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.eventimpl.MultimodalEventFactory;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.PacMap;

public abstract class InputDataChannelSkeleton extends RemoteObject implements IInputDataChannel {
    private static final int COMMAND_CLEAR_NONCHARACTER_KEY_STATE = 34;
    private static final int COMMAND_DELETE_BACKWARD = 4;
    private static final int COMMAND_DELETE_FORWARD = 3;
    private static final int COMMAND_GET_AUTO_CAPITALIZE_MODE = 23;
    private static final int COMMAND_GET_BACKWARD = 6;
    private static final int COMMAND_GET_EDITING_TEXT = 21;
    private static final int COMMAND_GET_FORWARD = 5;
    private static final int COMMAND_GET_SELECTED_TEXT = 24;
    private static final int COMMAND_INSERT_RICH_CONTENT = 2;
    private static final int COMMAND_INSERT_TEXT = 1;
    private static final int COMMAND_MARK_TEXT = 11;
    private static final int COMMAND_RECOMMEND_TEXT = 14;
    private static final int COMMAND_REPLACE_MARKED_TEXT = 13;
    private static final int COMMAND_REQUEST_CURSOR_CONTEXT_ONCE = 41;
    private static final int COMMAND_REVISE_TEXT = 15;
    private static final int COMMAND_SELECT_TEXT = 16;
    private static final int COMMAND_SEND_CUSTOMIZED_DATA = 32;
    private static final int COMMAND_SEND_KEY_EVENT = 31;
    private static final int COMMAND_SEND_KEY_FUNCTION = 33;
    private static final int COMMAND_SEND_MENU_FUNCTION = 35;
    private static final int COMMAND_SUBSCRIBE_CURSOR_CONTEXT_CHANGED = 42;
    private static final int COMMAND_SUBSCRIBE_EDITING_TEXT = 25;
    private static final int COMMAND_UNMARK_TEXT = 12;
    private static final int COMMAND_UNSUBSCRIBE_CURSOR_CONTEXT_CHANGED = 43;
    private static final int COMMAND_UNSUBSCRIBE_EDITING_TEXT = 26;
    private static final int COMMAND_VARIATION_FIVE = 50;
    private static final int COMMAND_VARIATION_FOUR = 40;
    private static final int COMMAND_VARIATION_ONE = 10;
    private static final int COMMAND_VARIATION_THREE = 30;
    private static final int COMMAND_VARIATION_TWO = 20;
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputDataChannel";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;
    private static final int READ_MSG_ERROR = -3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputDataChannelSkeleton");

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public InputDataChannelSkeleton(String str) {
        super(str);
    }

    public static IInputDataChannel asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new InputDataChannelProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IInputDataChannel) {
            return (IInputDataChannel) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (1 <= i && i <= 10) {
            return transactToEditText(i, messageParcel, messageParcel2);
        }
        if (11 <= i && i <= 20) {
            return transactToManipulateText(i, messageParcel, messageParcel2);
        }
        if (21 <= i && i <= 30) {
            return transactToGetEditingInfo(i, messageParcel, messageParcel2);
        }
        if (31 <= i && i <= 40) {
            return transactToSendFunctionEvent(i, messageParcel, messageParcel2);
        }
        if (41 > i || i > COMMAND_VARIATION_FIVE) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        return transactToCursorContext(i, messageParcel, messageParcel2);
    }

    private boolean transactToCursorContext(int i, MessageParcel messageParcel, MessageParcel messageParcel2) {
        switch (i) {
            case 41:
                return transactToRequestCurrentCursorContext(messageParcel, messageParcel2);
            case 42:
                return transactToSubscribeCursorChanged(messageParcel, messageParcel2);
            case 43:
                return transactToUnsubscribeCursorChanged(messageParcel, messageParcel2);
            default:
                HiLog.info(TAG, "Not a valid edit text transaction code.", new Object[0]);
                return false;
        }
    }

    private boolean transactToEditText(int i, MessageParcel messageParcel, MessageParcel messageParcel2) {
        switch (i) {
            case 1:
                return transactToInsertText(messageParcel, messageParcel2);
            case 2:
                return transactToInsertRichContent(messageParcel, messageParcel2);
            case 3:
                return transactToDeleteForward(messageParcel, messageParcel2);
            case 4:
                return transactToDeleteBackward(messageParcel, messageParcel2);
            case 5:
                return transactToGetForward(messageParcel, messageParcel2);
            case 6:
                return transactToGetBackward(messageParcel, messageParcel2);
            default:
                HiLog.info(TAG, "Not a valid edit text transaction code.", new Object[0]);
                return false;
        }
    }

    private boolean transactToManipulateText(int i, MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToManipulateText", new Object[0]);
        switch (i) {
            case 11:
                return transactToMarkText(messageParcel, messageParcel2);
            case 12:
                return transactToUnmarkText(messageParcel, messageParcel2);
            case 13:
                return transactToReplaceMarkedText(messageParcel, messageParcel2);
            case 14:
                return transactToRecommendText(messageParcel, messageParcel2);
            case 15:
                return transactToReviseText(messageParcel, messageParcel2);
            case 16:
                return transactToSelectText(messageParcel, messageParcel2);
            default:
                HiLog.info(TAG, "Not a valid manipulate text transaction code.", new Object[0]);
                return false;
        }
    }

    private boolean transactToGetEditingInfo(int i, MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToGetEditingInfo", new Object[0]);
        switch (i) {
            case 21:
                return transactToGetEditingText(messageParcel, messageParcel2);
            case 22:
            default:
                HiLog.info(TAG, "Not a valid get editing info transaction code.", new Object[0]);
                return false;
            case 23:
                return transactToGetAutoCapitalizeMode(messageParcel, messageParcel2);
            case 24:
                return transactToGetSelected(messageParcel, messageParcel2);
            case 25:
                return transactToSubscribeEditingText(messageParcel, messageParcel2);
            case 26:
                return transactToUnsubscribeEditingText(messageParcel, messageParcel2);
        }
    }

    private boolean transactToRequestCurrentCursorContext(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToRequestCurrentCursorContext", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "requestCurrentCursorContext readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean requestCurrentCursorContext = requestCurrentCursorContext(InputDataChannelCallbackSkeleton.asInterface(messageParcel.readRemoteObject()));
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(requestCurrentCursorContext ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "transactToRequestCurrentCursorContext RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToUnsubscribeCursorChanged(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToUnsubscribeCursorChanged", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "unsubscribeCursorChanged readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean unsubscribeCursorContext = unsubscribeCursorContext(InputDataChannelCallbackSkeleton.asInterface(messageParcel.readRemoteObject()));
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(unsubscribeCursorContext ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "transactToUnsubscribeCursorChanged RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToSubscribeCursorChanged(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToSubscribeCursorChanged", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "subscribeCursorChanged readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean subscribeCursorContext = subscribeCursorContext(InputDataChannelCallbackSkeleton.asInterface(messageParcel.readRemoteObject()));
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(subscribeCursorContext ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "subscribeCursorChanged RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToSendFunctionEvent(int i, MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToSendFunctionEvent", new Object[0]);
        switch (i) {
            case 31:
                return transactToSendKeyEvent(messageParcel, messageParcel2);
            case 32:
                return transactToSendCustomizedData(messageParcel, messageParcel2);
            case 33:
                return transactToSendKeyFunction(messageParcel, messageParcel2);
            case 34:
                return transactToClearNoncharacterKeyState(messageParcel, messageParcel2);
            case 35:
                return transactToSendMenuFunction(messageParcel, messageParcel2);
            default:
                HiLog.info(TAG, "Not a valid send function event transaction code.", new Object[0]);
                return false;
        }
    }

    private boolean transactToInsertText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToInsertText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "insertText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean insertText = insertText(messageParcel.readString());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(insertText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "insertText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToInsertRichContent(MessageParcel messageParcel, MessageParcel messageParcel2) {
        RichContent richContent;
        HiLog.info(TAG, "transactToInsertRichContent", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "insertRichContent readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        if (messageParcel.readInt() != 0) {
            richContent = RichContent.PRODUCER.createFromParcel(messageParcel);
        } else {
            HiLog.warn(TAG, "insertRichContent maybe something wrong: richContent is null.", new Object[0]);
            richContent = null;
        }
        try {
            boolean insertRichContent = insertRichContent(richContent, InputDataChannelCallbackSkeleton.asInterface(messageParcel.readRemoteObject()));
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(insertRichContent ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "insertRichContent RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToDeleteForward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToDeleteForward", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "deleteForward readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean deleteForward = deleteForward(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(deleteForward ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "deleteForward RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToDeleteBackward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToDeleteBackward", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "deleteBackward readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean deleteBackward = deleteBackward(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(deleteBackward ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "deleteBackward RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToGetForward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToGetForward", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "getForward readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            getForward(messageParcel.readInt(), InputDataChannelCallbackSkeleton.asInterface(messageParcel.readRemoteObject()));
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getForward RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToGetBackward(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToGetBackward", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "getBackward readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            getBackward(messageParcel.readInt(), InputDataChannelCallbackSkeleton.asInterface(messageParcel.readRemoteObject()));
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getBackward RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToMarkText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToMarkText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "markText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean markText = markText(messageParcel.readInt(), messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(markText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "markText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToUnmarkText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToUnmarkText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "unmarkText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean unmarkText = unmarkText();
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(unmarkText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "unmarkText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToReplaceMarkedText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToReplaceMarkedText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "replaceMarkedText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean replaceMarkedText = replaceMarkedText(messageParcel.readString());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(replaceMarkedText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "replaceMarkedText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToGetEditingText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToGetEditingText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "getEditingText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        EditingCapability editingCapability = new EditingCapability();
        int readInt = messageParcel.readInt();
        if (messageParcel.readInt() != 0) {
            editingCapability.unmarshalling(messageParcel);
        } else {
            HiLog.warn(TAG, "EditingCapability unmarshalling error.", new Object[0]);
        }
        try {
            getEditingText(readInt, editingCapability, InputDataChannelCallbackSkeleton.asInterface(messageParcel.readRemoteObject()));
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getEditingText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToSubscribeEditingText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToSubscribeEditingText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "subscribeEditingText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        EditingCapability editingCapability = new EditingCapability();
        int readInt = messageParcel.readInt();
        if (messageParcel.readInt() != 0) {
            editingCapability.unmarshalling(messageParcel);
        } else {
            HiLog.warn(TAG, "subscribeEditingText EditingCapability unmarshalling error.", new Object[0]);
        }
        try {
            boolean subscribeEditingText = subscribeEditingText(readInt, editingCapability);
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(subscribeEditingText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "subscribeEditingText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToUnsubscribeEditingText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToUnsubscribeEditingText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "unsubscribeEditingText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean unsubscribeEditingText = unsubscribeEditingText(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(unsubscribeEditingText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "unsubscribeEditingText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToSendKeyEvent(MessageParcel messageParcel, MessageParcel messageParcel2) {
        Optional<MultimodalEvent> optional;
        MultimodalEvent multimodalEvent;
        HiLog.info(TAG, "transactToSendKeyEvent", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "sendKeyEvent readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        if (messageParcel.readInt() != 0) {
            optional = MultimodalEventFactory.createEvent(messageParcel);
        } else {
            HiLog.warn(TAG, "sendKeyEvent KeyEvent unmarshalling error.", new Object[0]);
            optional = Optional.empty();
        }
        KeyEvent keyEvent = null;
        if (optional.isPresent()) {
            multimodalEvent = optional.get();
        } else {
            HiLog.error(TAG, "multimodalEvent Optional is not present", new Object[0]);
            multimodalEvent = null;
        }
        if (multimodalEvent instanceof KeyEvent) {
            keyEvent = (KeyEvent) multimodalEvent;
        } else {
            HiLog.error(TAG, "not the instance of KeyEvent", new Object[0]);
        }
        try {
            boolean sendKeyEvent = sendKeyEvent(keyEvent);
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(sendKeyEvent ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "sendKeyEvent RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToSendCustomizedData(MessageParcel messageParcel, MessageParcel messageParcel2) {
        PacMap pacMap;
        HiLog.info(TAG, "transactToSendCustomizedData", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "sendCustomizedData readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        String readString = messageParcel.readString();
        if (messageParcel.readInt() != 0) {
            pacMap = PacMap.PRODUCER.createFromParcel(messageParcel);
        } else {
            HiLog.warn(TAG, "sendCustomizedData PacMap createFromParcel error.", new Object[0]);
            pacMap = null;
        }
        try {
            boolean sendCustomizedData = sendCustomizedData(readString, pacMap);
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(sendCustomizedData ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "sendCustomizedData RemoteException", new Object[0]);
            return true;
        }
    }

    private boolean transactToSendKeyFunction(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToSendKeyFunction", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "sendKeyFunction readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean sendKeyFunction = sendKeyFunction(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(sendKeyFunction ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "sendKeyFunction RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToSelectText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToSelectText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "selectText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean selectText = selectText(messageParcel.readInt(), messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(selectText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "selectText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToClearNoncharacterKeyState(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToClearNoncharacterKeyState", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "clearNoncharacterKeyState readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean clearNoncharacterKeyState = clearNoncharacterKeyState(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(clearNoncharacterKeyState ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "clearNoncharacterKeyState RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToRecommendText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToRecommendText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "recommendText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        RecommendationInfo recommendationInfo = new RecommendationInfo(0, "", 0, "");
        if (messageParcel.readInt() != 0) {
            recommendationInfo.unmarshalling(messageParcel);
        } else {
            HiLog.warn(TAG, "recommendText RecommendationInfo unmarshalling error.", new Object[0]);
        }
        try {
            boolean recommendText = recommendText(recommendationInfo);
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(recommendText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "recommendText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToReviseText(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToReviseText", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "reviseText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean reviseText = reviseText(messageParcel.readInt(), messageParcel.readString(), messageParcel.readString());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(reviseText ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "reviseText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToSendMenuFunction(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToSendMenuFunction", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "sendMenuFunction readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            boolean sendMenuFunction = sendMenuFunction(messageParcel.readInt());
            messageParcel2.writeInt(0);
            messageParcel2.writeInt(sendMenuFunction ? 1 : 0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "sendMenuFunction RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToGetAutoCapitalizeMode(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToGetAutoCapitalizeMode", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "getAutoCapitalizeMode readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            getAutoCapitalizeMode(messageParcel.readInt(), InputDataChannelCallbackSkeleton.asInterface(messageParcel.readRemoteObject()));
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getAutoCapitalizeMode RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }

    private boolean transactToGetSelected(MessageParcel messageParcel, MessageParcel messageParcel2) {
        HiLog.info(TAG, "transactToGetSelected", new Object[0]);
        if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            HiLog.error(TAG, "getSelectedText readInterfaceToken failed.", new Object[0]);
            messageParcel2.writeInt(-3);
            return false;
        }
        try {
            getSelectedText(messageParcel.readInt(), InputDataChannelCallbackSkeleton.asInterface(messageParcel.readRemoteObject()));
            messageParcel2.writeInt(0);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "getSelectedText RemoteException", new Object[0]);
            messageParcel2.writeInt(-1);
            return true;
        }
    }
}
