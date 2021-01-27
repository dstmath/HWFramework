package ohos.miscservices.inputmethod.internal;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.inputmethod.EditingText;
import ohos.miscservices.inputmethod.RecommendationInfo;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class InputMethodAgentProxy implements IInputMethodAgent {
    private static final String DESCRIPTOR = "ohos.miscservices.inputmethod.internal.IInputMethodAgent";
    private static final int DISPATCH_EVENT = 31;
    private static final int ERR_OK = 0;
    private static final int NOTIFY_CURSOR_CHANGED = 4;
    private static final int NOTIFY_EDITING_TEXT_CHANGED = 2;
    private static final int NOTIFY_SELECTION_CHANGED = 1;
    private static final int SEND_RECOMMENDATION_INFO = 3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "InputMethodAgentProxy");
    private static final int WRITE_MSG_ERROR = -2;
    private final IRemoteObject remote;

    public InputMethodAgentProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifySelectionChanged(int i, int i2, int i3, int i4) throws RemoteException {
        HiLog.debug(TAG, "notifySelectionChanged", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "notifySelectionChanged writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeInt(i);
        obtain.writeInt(i2);
        obtain.writeInt(i3);
        obtain.writeInt(i4);
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

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void sendRecommendationInfo(RecommendationInfo[] recommendationInfoArr) throws RemoteException {
        HiLog.debug(TAG, "sendRecommendationInfo", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "sendRecommendationInfo writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        if (recommendationInfoArr == null) {
            obtain.writeBoolean(true);
        } else {
            obtain.writeBoolean(false);
            obtain.writeInt(recommendationInfoArr.length);
            marshallingRecommendationInfos(recommendationInfoArr, obtain);
        }
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

    private void marshallingRecommendationInfos(RecommendationInfo[] recommendationInfoArr, MessageParcel messageParcel) {
        for (RecommendationInfo recommendationInfo : recommendationInfoArr) {
            if (recommendationInfo == null) {
                messageParcel.writeBoolean(true);
            } else {
                messageParcel.writeBoolean(false);
                recommendationInfo.marshalling(messageParcel);
            }
        }
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyEditingTextChanged(int i, EditingText editingText) throws RemoteException {
        HiLog.debug(TAG, "notifyEditingTextChanged", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "notifyEditingTextChanged writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeInt(i);
        if (editingText == null) {
            obtain.writeBoolean(true);
        } else {
            obtain.writeBoolean(false);
            editingText.marshalling(obtain);
        }
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

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public void notifyCursorCoordinateChanged(float f, float f2, float f3, float[] fArr) throws RemoteException {
        HiLog.debug(TAG, "notifyCursorCoordinateChanged", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "notifyCursorCoordinateChanged writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return;
        }
        obtain.writeFloat(f);
        obtain.writeFloat(f2);
        obtain.writeFloat(f3);
        obtain.writeInt(fArr.length);
        obtain.writeFloatArray(fArr);
        MessageOption messageOption = new MessageOption(0);
        try {
            HiLog.info(TAG, "sendRequest:NOTIFY_CURSOR_CHANGED", new Object[0]);
            this.remote.sendRequest(4, obtain, obtain2, messageOption);
            obtain.reclaim();
            obtain2.reclaim();
        } catch (RemoteException unused) {
            HiLog.error(TAG, "notifyCursorCoordinateChanged send msg failed", new Object[0]);
            throw new RemoteException();
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.miscservices.inputmethod.internal.IInputMethodAgent
    public boolean dispatchMultimodalEvent(MultimodalEvent multimodalEvent) throws RemoteException {
        HiLog.debug(TAG, "dispatchMultimodalEvent", new Object[0]);
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        if (!obtain.writeInterfaceToken(DESCRIPTOR)) {
            HiLog.error(TAG, "dispatchMultimodalEvent writeInterfaceToken failed.", new Object[0]);
            obtain2.writeInt(-2);
            return false;
        }
        if (multimodalEvent != null) {
            obtain.writeInt(1);
            multimodalEvent.marshalling(obtain);
        } else {
            obtain.writeInt(0);
        }
        try {
            HiLog.info(TAG, "sendRequest:dispatchMultimodalEvent", new Object[0]);
            this.remote.sendRequest(31, obtain, obtain2, messageOption);
            return obtain2.readBoolean();
        } catch (RemoteException unused) {
            HiLog.debug(TAG, "send msg failed", new Object[0]);
            return false;
        } finally {
            obtain.reclaim();
            obtain2.reclaim();
        }
    }
}
