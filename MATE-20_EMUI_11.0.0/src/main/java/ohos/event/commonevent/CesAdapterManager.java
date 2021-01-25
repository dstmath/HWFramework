package ohos.event.commonevent;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ohos.aafwk.content.IntentFilter;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.tools.Bytrace;
import ohos.utils.Parcel;

public class CesAdapterManager {
    private static final CesAdapterManager INSTANCE = new CesAdapterManager();
    private static final int INVALID_RECEIVER = 0;
    private static final int LOG_DOMAIN = 218108546;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218108546, TAG);
    private static final String RECEIVER_THREAD_NAME = "ces_receiver_thread";
    private static final String TAG = "CesAdapterManager";
    private static final int USER_CURRENT = -2;
    private static boolean nativeAvailable;
    private Context globalContext = null;
    private ConcurrentHashMap<Integer, CesProxyReceiver> proxyMap = new ConcurrentHashMap<>();
    private Handler receiverHandler = null;

    static native void nativeGetActionClassName(String str, Parcel parcel);

    private native void nativeInitCES();

    private native void nativeOnReceive(Parcel parcel, boolean z, int i);

    private native void nativeOnResultReceive(long j, Parcel parcel);

    private native void nativeRelease(long j);

    static {
        nativeAvailable = false;
        try {
            HiLog.info(LOG_LABEL, "CesAdapterManager load libces_jni.z.so", new Object[0]);
            System.loadLibrary("ces_jni.z");
            nativeAvailable = true;
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LOG_LABEL, "CesAdapterManager Could not load libces_jni.z.so", new Object[0]);
        }
    }

    public static CesAdapterManager getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        if (!nativeAvailable) {
            HiLog.error(LOG_LABEL, "init error! libces_jni.z.so is not available", new Object[0]);
            return;
        }
        this.globalContext = context;
        nativeInitCES();
    }

    static int subscribeCommonEvent(long j) {
        return getInstance().handleSubscribeCommonEvent(j);
    }

    static void unsubscribeCommonEvent(int i) {
        getInstance().handleUnsubscribeCommonEvent(i);
    }

    static boolean publishCommonEvent(long j, long j2, long j3) {
        return getInstance().handlePublishCommonEvent(j, j2, j3);
    }

    /* access modifiers changed from: package-private */
    public void onReceiveCommonEvent(Intent intent, int i, String str, boolean z, int i2) {
        Bytrace.startTrace(2, "JavaSubscriberOnReceive2");
        if (intent != null) {
            if (!nativeAvailable) {
                HiLog.error(LOG_LABEL, "onReceiveCommonEvent, libces_jni.z.so is not available", new Object[0]);
                return;
            }
            HiLog.debug(LOG_LABEL, "onReceiveCommonEvent action=%{public}s", new Object[]{intent.getAction()});
            Optional<Parcel> createEventParcel = createEventParcel(intent, i, str);
            if (!createEventParcel.isPresent()) {
                HiLog.error(LOG_LABEL, "onReceiveCommonEvent, createEventParcel failed!", new Object[0]);
                return;
            }
            nativeOnReceive(createEventParcel.get(), z, i2);
            createEventParcel.get().reclaim();
            Bytrace.finishTrace(2, "JavaSubscriberOnReceive2");
        }
    }

    /* access modifiers changed from: package-private */
    public void onResultReceiverReceive(long j, Intent intent, int i, String str) {
        if (intent != null) {
            if (!nativeAvailable) {
                HiLog.error(LOG_LABEL, "onResultReceiverReceive, libces_jni.z.so is not available", new Object[0]);
                return;
            }
            HiLog.debug(LOG_LABEL, "onResultReceiverReceive action=%{public}s", new Object[]{intent.getAction()});
            Optional<Parcel> createEventParcel = createEventParcel(intent, i, str);
            if (!createEventParcel.isPresent()) {
                HiLog.error(LOG_LABEL, "onResultReceiverReceive, createEventParcel failed!", new Object[0]);
                return;
            }
            nativeOnResultReceive(j, createEventParcel.get());
            createEventParcel.get().reclaim();
        }
    }

    /* access modifiers changed from: package-private */
    public void releaseNativeSubscriber(long j) {
        if (!nativeAvailable) {
            HiLog.error(LOG_LABEL, "releaseNativeSubscriber, libces_jni.z.so is not available", new Object[0]);
        } else {
            nativeRelease(j);
        }
    }

    private CesAdapterManager() {
        HandlerThread handlerThread = new HandlerThread(RECEIVER_THREAD_NAME);
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        if (looper != null) {
            this.receiverHandler = new Handler(looper);
        } else {
            HiLog.error(LOG_LABEL, "CesAdapterManager constructor looper is null, handler create failed!", new Object[0]);
        }
    }

    private boolean handlePublishCommonEvent(long j, long j2, long j3) {
        if (!isParamValid(j, j2)) {
            HiLog.error(LOG_LABEL, "handlePublishCommonEvent, param is invalid", new Object[0]);
            return false;
        }
        Parcel create = Parcel.create(j);
        CommonEventData commonEventData = new CommonEventData();
        if (!commonEventData.unmarshalling(create)) {
            HiLog.error(LOG_LABEL, "handlePublishCommonEvent, unmarshalling event failed!", new Object[0]);
            create.reclaim();
            return false;
        }
        create.reclaim();
        Optional<Intent> createAndroidIntent = createAndroidIntent(commonEventData.getIntent());
        if (!createAndroidIntent.isPresent()) {
            HiLog.error(LOG_LABEL, "handlePublishCommonEvent, createAndroidIntent failed!", new Object[0]);
            return false;
        }
        Parcel create2 = Parcel.create(j2);
        CommonEventPublishInfo commonEventPublishInfo = new CommonEventPublishInfo();
        if (!commonEventPublishInfo.unmarshalling(create2)) {
            HiLog.error(LOG_LABEL, "handlePublishCommonEvent, unmarshalling publish info failed!", new Object[0]);
            create2.reclaim();
            return false;
        }
        create2.reclaim();
        String[] subscriberPermissions = commonEventPublishInfo.getSubscriberPermissions();
        String str = subscriberPermissions.length > 0 ? subscriberPermissions[0] : null;
        HiLog.debug(LOG_LABEL, "handlePublishCommonEvent, send common event.", new Object[0]);
        sendBroadcast(commonEventPublishInfo, createAndroidIntent.get(), str, j3);
        return true;
    }

    private int handleSubscribeCommonEvent(long j) {
        if (this.globalContext == null || j == 0) {
            HiLog.error(LOG_LABEL, "globalContext or nativeParcel is null!", new Object[0]);
            return 0;
        }
        Parcel create = Parcel.create(j);
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(new IntentFilter());
        if (!commonEventSubscribeInfo.unmarshalling(create)) {
            HiLog.error(LOG_LABEL, "subscribeCommonEvent, unmarshalling subscribe info failed!", new Object[0]);
            create.reclaim();
            return 0;
        }
        create.reclaim();
        IntentFilter intentFilter = commonEventSubscribeInfo.getIntentFilter();
        if (intentFilter == null) {
            HiLog.error(LOG_LABEL, "subscribeCommonEvent, intent filter is null!", new Object[0]);
            return 0;
        }
        Optional<android.content.IntentFilter> createAndroidIntentFilter = DataConverter.createAndroidIntentFilter(true, commonEventSubscribeInfo.getPriority(), intentFilter);
        if (!createAndroidIntentFilter.isPresent()) {
            HiLog.error(LOG_LABEL, "subscribeCommonEvent, convert intent filter failed!", new Object[0]);
            return 0;
        }
        CesProxyReceiver cesProxyReceiver = new CesProxyReceiver();
        String permission = commonEventSubscribeInfo.getPermission();
        if (permission != null && permission.isEmpty()) {
            permission = null;
        }
        Bytrace.startTrace(2, "JavaSerSubscribe");
        HiLog.debug(LOG_LABEL, "handleSubscribeCommonEvent.", new Object[0]);
        if (!registerReceiver(commonEventSubscribeInfo, cesProxyReceiver, createAndroidIntentFilter.get(), permission)) {
            HiLog.error(LOG_LABEL, "subscribeCommonEvent, subscribe failed!", new Object[0]);
            Bytrace.finishTrace(2, "JavaSerSubscribe");
            return 0;
        }
        int hashCode = cesProxyReceiver.hashCode();
        this.proxyMap.put(Integer.valueOf(hashCode), cesProxyReceiver);
        Bytrace.finishTrace(2, "JavaSerSubscribe");
        return hashCode;
    }

    private void handleUnsubscribeCommonEvent(int i) {
        CesProxyReceiver remove = this.proxyMap.remove(Integer.valueOf(i));
        if (remove == null) {
            return;
        }
        if (this.globalContext == null) {
            HiLog.warn(LOG_LABEL, "handleUnsubscribeCommonEvent, globalContext is null!", new Object[0]);
            return;
        }
        Bytrace.startTrace(2, "JavaSerunSubscribe");
        this.globalContext.unregisterReceiver(remove);
        Bytrace.finishTrace(2, "JavaSerunSubscribe");
    }

    private boolean registerReceiver(CommonEventSubscribeInfo commonEventSubscribeInfo, CesProxyReceiver cesProxyReceiver, android.content.IntentFilter intentFilter, String str) {
        try {
            int userId = commonEventSubscribeInfo.getUserId();
            if (userId == -2) {
                this.globalContext.registerReceiver(cesProxyReceiver, intentFilter, str, this.receiverHandler);
            } else {
                this.globalContext.registerReceiverAsUser(cesProxyReceiver, getUserHandle(userId), intentFilter, str, this.receiverHandler);
            }
            return true;
        } catch (IllegalStateException unused) {
            HiLog.error(LOG_LABEL, "registerReceiver occur IllegalStateException.", new Object[0]);
            return false;
        }
    }

    private Optional<Intent> createAndroidIntent(ohos.aafwk.content.Intent intent) {
        if (intent == null) {
            return Optional.empty();
        }
        return DataConverter.createAndroidIntent(true, intent);
    }

    private Optional<Parcel> createEventParcel(Intent intent, int i, String str) {
        Optional<ohos.aafwk.content.Intent> createZidaneIntent = DataConverter.createZidaneIntent(true, intent);
        if (!createZidaneIntent.isPresent()) {
            HiLog.error(LOG_LABEL, "createEventParcel, createZidaneIntent failed!", new Object[0]);
            return Optional.empty();
        }
        CommonEventData commonEventData = new CommonEventData(createZidaneIntent.get(), i, str);
        Parcel create = Parcel.create();
        if (commonEventData.marshalling(create)) {
            return Optional.of(create);
        }
        HiLog.error(LOG_LABEL, "createEventParcel, event marshalling failed!", new Object[0]);
        create.reclaim();
        return Optional.empty();
    }

    private boolean isParamValid(long j, long j2) {
        if (this.globalContext == null) {
            HiLog.error(LOG_LABEL, "isParamValid, globalContext is null!", new Object[0]);
            return false;
        } else if (j != 0 && j2 != 0) {
            return true;
        } else {
            HiLog.error(LOG_LABEL, "isParamValid, param is invalid!", new Object[0]);
            return false;
        }
    }

    private void sendBroadcast(CommonEventPublishInfo commonEventPublishInfo, Intent intent, String str, long j) {
        int userId = commonEventPublishInfo.getUserId();
        if (commonEventPublishInfo.isOrdered()) {
            CesResultReceiver cesResultReceiver = null;
            if (j != 0) {
                cesResultReceiver = new CesResultReceiver(j);
            }
            if (userId == -2) {
                this.globalContext.sendOrderedBroadcast(intent, str, cesResultReceiver, null, -1, null, null);
            } else {
                this.globalContext.sendOrderedBroadcastAsUser(intent, getUserHandle(userId), str, cesResultReceiver, null, -1, null, null);
            }
        } else if (userId == -2) {
            this.globalContext.sendBroadcast(intent, str);
        } else {
            this.globalContext.sendBroadcastAsUser(intent, getUserHandle(userId), str);
        }
    }

    private UserHandle getUserHandle(int i) {
        return UserHandle.of(i);
    }
}
