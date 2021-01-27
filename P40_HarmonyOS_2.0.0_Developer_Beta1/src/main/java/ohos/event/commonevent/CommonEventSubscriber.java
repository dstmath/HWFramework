package ohos.event.commonevent;

import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.idn.BasicInfo;
import ohos.idn.DeviceManager;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.tools.Bytrace;
import ohos.utils.Parcel;
import sun.misc.Cleaner;

public abstract class CommonEventSubscriber {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String TAG = "CommonEventSubscriber";
    private AsyncCommonEventResult asyncCommonEventResult = null;
    private final CommonEventSubscriberHost innerSubscriber = new CommonEventSubscriberHost(this) {
        /* class ohos.event.commonevent.CommonEventSubscriber.AnonymousClass1 */

        @Override // ohos.event.commonevent.ICommonEventSubscriber
        public void onReceive(CommonEventData commonEventData) {
            CommonEventSubscriber.this.onReceiveEvent(commonEventData);
        }
    };
    final long nativeSubscriber;
    private CommonEventSubscribeInfo subscribeInfo = null;
    private CommonEventSubscriber subscriber;

    private native long nativeCreateSubscriber(Parcel parcel);

    /* access modifiers changed from: private */
    public static native void nativeDeleteSubscriber(long j);

    private native void nativeSubscribeCommonEvent(long j);

    private native void nativeUnsubscribeCommonEvent(long j);

    public abstract void onReceiveEvent(CommonEventData commonEventData);

    static {
        try {
            HiLog.info(LABEL, "inner Load libces_client_jni.z.so", new Object[0]);
            System.loadLibrary("ces_client_jni.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LABEL, "ERROR: Could not load common event jni so ", new Object[0]);
        }
    }

    public CommonEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
        if (commonEventSubscribeInfo != null) {
            String deviceId = commonEventSubscribeInfo.getDeviceId();
            boolean z = false;
            if (deviceId != null && !deviceId.isEmpty()) {
                BasicInfo basicInfo = (BasicInfo) new DeviceManager().getLocalNodeBasicInfo().orElse(null);
                if (basicInfo == null) {
                    HiLog.warn(LABEL, "Gets local device id failed.", new Object[0]);
                } else if (!deviceId.equals(basicInfo.getNodeId())) {
                    z = true;
                }
            }
            if (z) {
                this.subscriber = this;
                Parcel create = Parcel.create();
                create.writeSequenceable(commonEventSubscribeInfo);
                this.nativeSubscriber = nativeCreateSubscriber(create);
                Cleaner.create(this, new DestroyNativeObjectTask(this.nativeSubscriber));
                return;
            }
            this.nativeSubscriber = 0;
            this.subscribeInfo = new CommonEventSubscribeInfo(commonEventSubscribeInfo);
            return;
        }
        throw new IllegalArgumentException("The subscribeInfo param is illegal.");
    }

    private static class DestroyNativeObjectTask implements Runnable {
        private final long nativeSubscriber;

        public DestroyNativeObjectTask(long j) {
            this.nativeSubscriber = j;
        }

        @Override // java.lang.Runnable
        public void run() {
            CommonEventSubscriber.nativeDeleteSubscriber(this.nativeSubscriber);
        }
    }

    public CommonEventSubscribeInfo getSubscribeInfo() {
        return this.subscribeInfo;
    }

    public final AsyncCommonEventResult goAsyncCommonEvent() {
        AsyncCommonEventResult asyncCommonEventResult2 = this.asyncCommonEventResult;
        this.asyncCommonEventResult = null;
        if (asyncCommonEventResult2 != null) {
            asyncCommonEventResult2.async();
        }
        return asyncCommonEventResult2;
    }

    public final void setCode(int i) {
        checkResult();
        this.asyncCommonEventResult.setCode(i);
    }

    public final int getCode() {
        AsyncCommonEventResult asyncCommonEventResult2 = this.asyncCommonEventResult;
        if (asyncCommonEventResult2 != null) {
            return asyncCommonEventResult2.getCode();
        }
        return 0;
    }

    public final void setData(String str) {
        checkResult();
        this.asyncCommonEventResult.setData(str);
    }

    public final String getData() {
        AsyncCommonEventResult asyncCommonEventResult2 = this.asyncCommonEventResult;
        if (asyncCommonEventResult2 != null) {
            return asyncCommonEventResult2.getData();
        }
        return null;
    }

    public final void setCodeAndData(int i, String str) {
        checkResult();
        this.asyncCommonEventResult.setCodeAndData(i, str);
    }

    public final void abortCommonEvent() {
        checkResult();
        this.asyncCommonEventResult.abortCommonEvent();
    }

    public final boolean getAbortCommonEvent() {
        AsyncCommonEventResult asyncCommonEventResult2 = this.asyncCommonEventResult;
        return asyncCommonEventResult2 != null && asyncCommonEventResult2.getAbortCommonEvent();
    }

    public final void clearAbortCommonEvent() {
        AsyncCommonEventResult asyncCommonEventResult2 = this.asyncCommonEventResult;
        if (asyncCommonEventResult2 != null) {
            asyncCommonEventResult2.clearAbortCommonEvent();
        }
    }

    public IRemoteObject getRemoteAbility(Context context, Intent intent) throws RemoteException {
        if (context == null || intent == null) {
            return null;
        }
        return CommonEventRemoteAbilityHelper.getRemoteAbility(context, intent, this.innerSubscriber.readObject());
    }

    public final boolean isOrderedCommonEvent() {
        AsyncCommonEventResult asyncCommonEventResult2 = this.asyncCommonEventResult;
        return asyncCommonEventResult2 != null && asyncCommonEventResult2.orderedCommonEvent;
    }

    public final boolean isStickyCommonEvent() {
        AsyncCommonEventResult asyncCommonEventResult2 = this.asyncCommonEventResult;
        return asyncCommonEventResult2 != null && asyncCommonEventResult2.stickyCommonEvent;
    }

    /* access modifiers changed from: package-private */
    public ICommonEventSubscriber getSubscriber() {
        return this.innerSubscriber;
    }

    /* access modifiers changed from: package-private */
    public void setAsyncCommonEventResult(AsyncCommonEventResult asyncCommonEventResult2) {
        this.asyncCommonEventResult = asyncCommonEventResult2;
    }

    /* access modifiers changed from: package-private */
    public void handleRemoteSubscribe() {
        nativeSubscribeCommonEvent(this.nativeSubscriber);
    }

    /* access modifiers changed from: package-private */
    public void handleRemoteUnsubscribe() {
        nativeUnsubscribeCommonEvent(this.nativeSubscriber);
    }

    private void checkResult() {
        AsyncCommonEventResult asyncCommonEventResult2 = this.asyncCommonEventResult;
        if (asyncCommonEventResult2 == null) {
            throw new IllegalStateException("Call while asyncCommonEventResult is null");
        } else if (!asyncCommonEventResult2.orderedCommonEvent && !this.asyncCommonEventResult.stickyCommonEvent) {
            throw new IllegalStateException("Call during a non-ordered common event");
        }
    }

    private void handleRemoteOnReceiveCallback(long j) {
        CommonEventSubscriber commonEventSubscriber;
        Bytrace.startTrace(2, "javaOnreceive");
        CommonEventData commonEventData = new CommonEventData();
        Parcel create = Parcel.create(j);
        if (commonEventData.unmarshalling(create) && (commonEventSubscriber = this.subscriber) != null) {
            commonEventSubscriber.onReceiveEvent(commonEventData);
        }
        create.reclaim();
        Bytrace.finishTrace(2, "javaOnreceive");
    }

    public String toString() {
        return "CommonEventSubscriber[ subscribeInfo = " + this.subscribeInfo + "]";
    }
}
