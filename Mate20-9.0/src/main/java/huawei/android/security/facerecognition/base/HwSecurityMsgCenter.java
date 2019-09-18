package huawei.android.security.facerecognition.base;

import huawei.android.security.facerecognition.FaceRecognizeEvent;
import huawei.android.security.facerecognition.base.HwSecurityTaskBase;
import huawei.android.security.facerecognition.utils.LogUtil;
import java.util.HashMap;
import java.util.Map;

public class HwSecurityMsgCenter {
    private static final String TAG = HwSecurityMsgCenter.class.getSimpleName();
    private static HwSecurityMsgCenter gInstance = null;
    private static Object mInstanceLock = new Object();
    private HashMap<Integer, HashMap<HwSecurityTaskBase, EventRegInfo>> mEvMaps = new HashMap<>();
    private Object mLock = new Object();

    private static class EventRegInfo {
        /* access modifiers changed from: private */
        public boolean mEnable;
        /* access modifiers changed from: private */
        public HwSecurityTaskBase.EventListener mListener;

        private EventRegInfo(HwSecurityTaskBase.EventListener listener, boolean enable) {
            this.mListener = listener;
            this.mEnable = enable;
        }
    }

    /* access modifiers changed from: protected */
    public HashMap<HwSecurityTaskBase, EventRegInfo> getEventMap(int evId, boolean createWhileEmpty) {
        if (this.mEvMaps == null) {
            LogUtil.e(TAG, "mEvMaps is empty!!!");
            return null;
        } else if (this.mEvMaps.containsKey(Integer.valueOf(evId))) {
            return this.mEvMaps.get(Integer.valueOf(evId));
        } else {
            if (!createWhileEmpty) {
                return null;
            }
            this.mEvMaps.put(Integer.valueOf(evId), new HashMap());
            return this.mEvMaps.get(Integer.valueOf(evId));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003d, code lost:
        return false;
     */
    public boolean registerEvent(int evId, HwSecurityTaskBase task, HwSecurityTaskBase.EventListener evListener) {
        synchronized (this.mLock) {
            if (task != null && evListener != null) {
                HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, true);
                if (maps == null) {
                    return false;
                }
                if (maps.containsKey(task)) {
                    return false;
                }
                HwSecurityTaskBase parent = task.getParent();
                if (parent != null && maps.containsKey(parent)) {
                    EventRegInfo parentInfo = maps.get(parent);
                    if (parentInfo != null) {
                        boolean unused = parentInfo.mEnable = false;
                    }
                }
                maps.put(task, new EventRegInfo(evListener, true));
                return true;
            }
        }
    }

    public void unregisterEvent(int evId, HwSecurityTaskBase task) {
        synchronized (this.mLock) {
            HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, false);
            if (maps != null) {
                HwSecurityTaskBase parent = task.getParent();
                if (parent != null && maps.containsKey(parent)) {
                    EventRegInfo parentInfo = maps.get(parent);
                    if (parentInfo != null) {
                        boolean unused = parentInfo.mEnable = true;
                    }
                }
                maps.remove(task);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0052, code lost:
        r2 = r0.values().iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005e, code lost:
        if (r2.hasNext() == false) goto L_0x0080;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0060, code lost:
        r3 = r2.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0066, code lost:
        if (r3 == null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x006c, code lost:
        if (huawei.android.security.facerecognition.base.HwSecurityMsgCenter.EventRegInfo.access$000(r3) == false) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0072, code lost:
        if (huawei.android.security.facerecognition.base.HwSecurityMsgCenter.EventRegInfo.access$200(r3) == null) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007c, code lost:
        if (huawei.android.security.facerecognition.base.HwSecurityMsgCenter.EventRegInfo.access$200(r3).onEvent(r9) == false) goto L_0x005a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0080, code lost:
        return;
     */
    public void processEvent(FaceRecognizeEvent ev) {
        HashMap<HwSecurityTaskBase, EventRegInfo> copyMaps = new HashMap<>();
        synchronized (this.mLock) {
            int evId = ev.getType();
            String str = TAG;
            LogUtil.d(str, "processEvent: " + ev);
            HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, false);
            if (maps != null) {
                for (Map.Entry<HwSecurityTaskBase, EventRegInfo> entry : maps.entrySet()) {
                    copyMaps.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public static boolean staticRegisterEvent(int evId, HwSecurityTaskBase task, HwSecurityTaskBase.EventListener evListener) {
        HwSecurityMsgCenter gMsgCenter = getInstance();
        if (gMsgCenter != null) {
            return gMsgCenter.registerEvent(evId, task, evListener);
        }
        return false;
    }

    public static boolean staticUnregisterEvent(int evId, HwSecurityTaskBase task) {
        HwSecurityMsgCenter gMsgCenter = getInstance();
        if (gMsgCenter == null) {
            return false;
        }
        gMsgCenter.unregisterEvent(evId, task);
        return true;
    }

    public static void createInstance() {
        synchronized (mInstanceLock) {
            if (gInstance == null) {
                gInstance = new HwSecurityMsgCenter();
            }
        }
    }

    public static HwSecurityMsgCenter getInstance() {
        HwSecurityMsgCenter hwSecurityMsgCenter;
        synchronized (mInstanceLock) {
            hwSecurityMsgCenter = gInstance;
        }
        return hwSecurityMsgCenter;
    }

    public static void destroyInstance() {
        synchronized (mInstanceLock) {
            if (gInstance != null) {
                gInstance = null;
            }
        }
    }
}
