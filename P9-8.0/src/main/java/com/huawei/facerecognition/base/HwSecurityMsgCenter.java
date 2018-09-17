package com.huawei.facerecognition.base;

import com.huawei.facerecognition.FaceRecognizeEvent;
import com.huawei.facerecognition.base.HwSecurityTaskBase.EventListener;
import com.huawei.facerecognition.utils.LogUtil;
import java.util.HashMap;
import java.util.Map.Entry;

public class HwSecurityMsgCenter {
    private static final String TAG = HwSecurityMsgCenter.class.getSimpleName();
    private static HwSecurityMsgCenter gInstance = null;
    private static Object mInstanceLock = new Object();
    private HashMap<Integer, HashMap<HwSecurityTaskBase, EventRegInfo>> mEvMaps = new HashMap();
    private Object mLock = new Object();

    private static class EventRegInfo {
        private boolean mEnable;
        private EventListener mListener;

        /* synthetic */ EventRegInfo(EventListener listener, boolean enable, EventRegInfo -this2) {
            this(listener, enable);
        }

        private EventRegInfo(EventListener listener, boolean enable) {
            this.mListener = listener;
            this.mEnable = enable;
        }
    }

    protected HashMap<HwSecurityTaskBase, EventRegInfo> getEventMap(int evId, boolean createWhileEmpty) {
        if (this.mEvMaps == null) {
            LogUtil.e(TAG, "mEvMaps is empty!!!");
            return null;
        } else if (this.mEvMaps.containsKey(Integer.valueOf(evId))) {
            return (HashMap) this.mEvMaps.get(Integer.valueOf(evId));
        } else {
            if (!createWhileEmpty) {
                return null;
            }
            this.mEvMaps.put(Integer.valueOf(evId), new HashMap());
            return (HashMap) this.mEvMaps.get(Integer.valueOf(evId));
        }
    }

    /* JADX WARNING: Missing block: B:5:0x000a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean registerEvent(int evId, HwSecurityTaskBase task, EventListener evListener) {
        synchronized (this.mLock) {
            if (task == null || evListener == null) {
            } else {
                HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, true);
                if (maps == null) {
                    return false;
                } else if (maps.containsKey(task)) {
                    return false;
                } else {
                    HwSecurityTaskBase parent = task.getParent();
                    if (parent != null && maps.containsKey(parent)) {
                        EventRegInfo parentInfo = (EventRegInfo) maps.get(parent);
                        if (parentInfo != null) {
                            parentInfo.mEnable = false;
                        }
                    }
                    maps.put(task, new EventRegInfo(evListener, true, null));
                    return true;
                }
            }
        }
    }

    public void unregisterEvent(int evId, HwSecurityTaskBase task) {
        synchronized (this.mLock) {
            HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, false);
            if (maps == null) {
                return;
            }
            HwSecurityTaskBase parent = task.getParent();
            if (parent != null && maps.containsKey(parent)) {
                EventRegInfo parentInfo = (EventRegInfo) maps.get(parent);
                if (parentInfo != null) {
                    parentInfo.mEnable = true;
                }
            }
            maps.remove(task);
        }
    }

    /* JADX WARNING: Missing block: B:17:0x0057, code:
            r5 = r0.values().iterator();
     */
    /* JADX WARNING: Missing block: B:19:0x0063, code:
            if (r5.hasNext() == false) goto L_0x0083;
     */
    /* JADX WARNING: Missing block: B:20:0x0065, code:
            r4 = (com.huawei.facerecognition.base.HwSecurityMsgCenter.EventRegInfo) r5.next();
     */
    /* JADX WARNING: Missing block: B:21:0x006b, code:
            if (r4 == null) goto L_0x005f;
     */
    /* JADX WARNING: Missing block: B:23:0x0071, code:
            if (com.huawei.facerecognition.base.HwSecurityMsgCenter.EventRegInfo.-get0(r4) == false) goto L_0x005f;
     */
    /* JADX WARNING: Missing block: B:25:0x0077, code:
            if (com.huawei.facerecognition.base.HwSecurityMsgCenter.EventRegInfo.-get1(r4) == null) goto L_0x005f;
     */
    /* JADX WARNING: Missing block: B:27:0x0081, code:
            if (com.huawei.facerecognition.base.HwSecurityMsgCenter.EventRegInfo.-get1(r4).onEvent(r12) == false) goto L_0x005f;
     */
    /* JADX WARNING: Missing block: B:28:0x0083, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void processEvent(FaceRecognizeEvent ev) {
        HashMap<HwSecurityTaskBase, EventRegInfo> copyMaps = new HashMap();
        synchronized (this.mLock) {
            int evId = ev.getType();
            LogUtil.d(TAG, "processEvent: " + ev);
            HashMap<HwSecurityTaskBase, EventRegInfo> maps = getEventMap(evId, false);
            if (maps == null) {
                return;
            }
            for (Entry<HwSecurityTaskBase, EventRegInfo> entry : maps.entrySet()) {
                copyMaps.put((HwSecurityTaskBase) entry.getKey(), (EventRegInfo) entry.getValue());
            }
        }
    }

    public static boolean staticRegisterEvent(int evId, HwSecurityTaskBase task, EventListener evListener) {
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
