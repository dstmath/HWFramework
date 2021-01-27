package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingCapability;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.miscservices.inputmethod.RichContent;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.utils.PacMap;

public class InputDataChannelProxy implements IInputDataChannel {
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
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputDataChannelProxy");
    private static final int WRITE_MSG_ERROR = -2;
    private final IRemoteObject remote;

    public InputDataChannelProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean insertText(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "insertText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeString(str);
        try {
            boolean z = true;
            this.remote.sendRequest(1, obtain, obtain2, messageOption);
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

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean insertRichContent(RichContent richContent, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        boolean z = false;
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "insertRichContent writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        if (richContent != null) {
            obtain.writeInt(1);
            richContent.marshalling(obtain);
        } else {
            obtain.writeInt(0);
        }
        obtain.writeRemoteObject(iInputDataChannelCallback != null ? iInputDataChannelCallback.asObject() : null);
        try {
            this.remote.sendRequest(2, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                if (obtain2.readInt() == 1) {
                    z = true;
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean deleteForward(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "deleteForward writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(3, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean deleteBackward(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "deleteBackward writeInterfaceToken failed.", new Object[0]);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public void getForward(int i, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "getForward writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeInt(i);
        obtain.writeRemoteObject(iInputDataChannelCallback != null ? iInputDataChannelCallback.asObject() : null);
        try {
            this.remote.sendRequest(5, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public void getBackward(int i, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "getBackward writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeInt(i);
        obtain.writeRemoteObject(iInputDataChannelCallback != null ? iInputDataChannelCallback.asObject() : null);
        try {
            this.remote.sendRequest(6, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean markText(int i, int i2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "markText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        obtain.writeInt(i2);
        try {
            this.remote.sendRequest(11, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean unmarkText() throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "unmarkText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        try {
            this.remote.sendRequest(12, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean replaceMarkedText(String str) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "replaceMarkedText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeString(str);
        try {
            this.remote.sendRequest(13, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public void getEditingText(int i, EditingCapability editingCapability, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "getEditingText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeInt(i);
        if (editingCapability != null) {
            obtain.writeInt(1);
            editingCapability.marshalling(obtain);
        } else {
            obtain.writeInt(0);
        }
        obtain.writeRemoteObject(iInputDataChannelCallback != null ? iInputDataChannelCallback.asObject() : null);
        try {
            this.remote.sendRequest(21, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean subscribeEditingText(int i, EditingCapability editingCapability) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "subscribeEditingText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        boolean z = true;
        if (editingCapability != null) {
            obtain.writeInt(1);
            editingCapability.marshalling(obtain);
        } else {
            obtain.writeInt(0);
        }
        try {
            this.remote.sendRequest(25, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean unsubscribeEditingText(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "subscribeEditingText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(26, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean sendKeyEvent(KeyEvent keyEvent) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        boolean z = false;
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "sendKeyEvent writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        if (keyEvent != null) {
            obtain.writeInt(1);
            keyEvent.marshalling(obtain);
        } else {
            obtain.writeInt(0);
        }
        try {
            this.remote.sendRequest(31, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                if (obtain2.readInt() == 1) {
                    z = true;
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean sendCustomizedData(String str, PacMap pacMap) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "sendCustomizedData writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeString(str);
        boolean z = true;
        if (pacMap != null) {
            obtain.writeInt(1);
            pacMap.marshalling(obtain);
        } else {
            obtain.writeInt(0);
        }
        try {
            this.remote.sendRequest(32, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean sendKeyFunction(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "sendKeyFunction writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(33, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean selectText(int i, int i2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "selectText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        obtain.writeInt(i2);
        try {
            this.remote.sendRequest(16, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean clearNoncharacterKeyState(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "clearNoncharacterKeyState writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(34, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean recommendText(RecommendationInfo recommendationInfo) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        boolean z = false;
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "recommendText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        if (recommendationInfo != null) {
            obtain.writeInt(1);
            recommendationInfo.marshalling(obtain);
        } else {
            obtain.writeInt(0);
        }
        try {
            this.remote.sendRequest(14, obtain, obtain2, messageOption);
            if (obtain2.readInt() == 0) {
                if (obtain2.readInt() == 1) {
                    z = true;
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean reviseText(int i, String str, String str2) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "reviseText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        obtain.writeString(str);
        obtain.writeString(str2);
        try {
            this.remote.sendRequest(15, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean sendMenuFunction(int i) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "sendMenuFunction writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        obtain.writeInt(i);
        try {
            this.remote.sendRequest(35, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean requestCurrentCursorContext(IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "requestCurrentCursorContext writeInterfaceToken failed.", new Object[0]);
            obtain.writeInt(-2);
            return false;
        }
        obtain.writeRemoteObject(iInputDataChannelCallback != null ? iInputDataChannelCallback.asObject() : null);
        try {
            this.remote.sendRequest(41, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean subscribeCursorContext(IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "subscribeCursorContext writeInterfaceToken failed.", new Object[0]);
            obtain.writeInt(-2);
            return false;
        }
        obtain.writeRemoteObject(iInputDataChannelCallback != null ? iInputDataChannelCallback.asObject() : null);
        try {
            this.remote.sendRequest(42, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public boolean unsubscribeCursorContext(IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "unsubscribeCursorContext writeInterfaceToken failed.", new Object[0]);
            obtain.writeInt(-2);
            return false;
        }
        obtain.writeRemoteObject(iInputDataChannelCallback != null ? iInputDataChannelCallback.asObject() : null);
        try {
            this.remote.sendRequest(43, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public void getAutoCapitalizeMode(int i, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "getAutoCapitalizeMode writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeInt(i);
        obtain.writeRemoteObject(iInputDataChannelCallback != null ? iInputDataChannelCallback.asObject() : null);
        try {
            this.remote.sendRequest(23, obtain, obtain2, messageOption);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputDataChannel
    public void getSelectedText(int i, IInputDataChannelCallback iInputDataChannelCallback) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "getSelectedText writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeInt(i);
        obtain.writeRemoteObject(iInputDataChannelCallback != null ? iInputDataChannelCallback.asObject() : null);
        try {
            this.remote.sendRequest(24, obtain, obtain2, messageOption);
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
}
