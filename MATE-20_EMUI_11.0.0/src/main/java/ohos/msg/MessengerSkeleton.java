package ohos.msg;

import java.util.concurrent.ConcurrentHashMap;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.sysability.samgr.SysAbilityManager;

public class MessengerSkeleton {
    private static final int FAILED = -1;
    private static final int RETRY_MIDDLE = 3;
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "MessengerSkeleton");
    private static final int WHILE_SLEEP = 1000;
    private static volatile MessengerSkeleton sInstance = null;
    private final ConcurrentHashMap<String, IRemoteObject> mMessengerMap = new ConcurrentHashMap<>();

    static MessengerSkeleton getInstance() {
        if (sInstance == null) {
            synchronized (MessengerSkeleton.class) {
                if (sInstance == null) {
                    sInstance = new MessengerSkeleton();
                }
            }
        }
        return sInstance;
    }

    public int startMessenger(String str, IMessengerHandler iMessengerHandler) {
        if (str == null || str.length() == 0 || iMessengerHandler == null) {
            return -1;
        }
        MessengerStub messengerStub = new MessengerStub(str);
        messengerStub.setMessageHandler(iMessengerHandler);
        return SysAbilityManager.addSysAbility(Integer.parseInt(str), messengerStub);
    }

    public int sendMessage(String str, Message message) throws MessengerException {
        if (str == null || str.length() == 0 || message == null) {
            return -1;
        }
        IRemoteObject remoteObject = getRemoteObject(str);
        if (remoteObject == null) {
            HiLog.error(TAG, "getRemoteObject failed", new Object[0]);
            return -1;
        }
        try {
            return new MessengerProxy(remoteObject).sendMessage(message);
        } catch (MessengerException unused) {
            HiLog.error(TAG, "catch exception in sendMessage", new Object[0]);
            return -1;
        }
    }

    public int removeMessenger(String str) {
        this.mMessengerMap.remove(str);
        return this.mMessengerMap.size();
    }

    private IRemoteObject getRemoteObject(String str) {
        IRemoteObject iRemoteObject = this.mMessengerMap.get(str);
        if (iRemoteObject != null) {
            return iRemoteObject;
        }
        int i = 3;
        while (iRemoteObject == null) {
            int i2 = i - 1;
            if (i <= 0) {
                break;
            }
            iRemoteObject = SysAbilityManager.getSysAbility(Integer.parseInt(str));
            if (iRemoteObject == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException unused) {
                    HiLog.error(TAG, "fail to sleep in MessengerSkeleton SendMessage", new Object[0]);
                }
            }
            i = i2;
        }
        if (iRemoteObject != null) {
            HiLog.info(TAG, "get ability success", new Object[0]);
            this.mMessengerMap.put(str, iRemoteObject);
        }
        return iRemoteObject;
    }
}
