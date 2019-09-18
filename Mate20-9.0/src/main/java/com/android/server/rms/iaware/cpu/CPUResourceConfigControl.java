package com.android.server.rms.iaware.cpu;

import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import android.util.ArrayMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CPUResourceConfigControl {
    private static final String CONFIG_CONTROL_GROUP = "control_group";
    private static final String CONFIG_GROUP_TYPE = "type";
    private static final String FEATURENAME = "CPU";
    private static final int GET_CMS_RETRY_TIME = 5;
    private static final int GET_CMS_SLEEP_TIME = 200;
    private static final String GROUP_BG_TYPE = "group_bg";
    public static final int GROUP_BG_VALUE = 1;
    private static final String GROUP_WHITELIST = "whitelist";
    private static final String SEPARATOR = ";";
    private static final String TAG = "CPUResourceConfigControl";
    private static CPUResourceConfigControl sInstance;
    private IBinder mCMSManager;
    private boolean mHasReadXml = false;
    private final Map<String, Integer> mProcessWhiteListMap = new ArrayMap();

    private CPUResourceConfigControl() {
    }

    public static synchronized CPUResourceConfigControl getInstance() {
        CPUResourceConfigControl cPUResourceConfigControl;
        synchronized (CPUResourceConfigControl.class) {
            if (sInstance == null) {
                sInstance = new CPUResourceConfigControl();
            }
            cPUResourceConfigControl = sInstance;
        }
        return cPUResourceConfigControl;
    }

    private void initialize() {
        setWhiteListFromXml();
    }

    private void deInitialize() {
        synchronized (this) {
            this.mHasReadXml = false;
            this.mProcessWhiteListMap.clear();
        }
    }

    public AwareConfig getAwareCustConfig(String featureName, String configName) {
        try {
            if (this.mCMSManager == null) {
                int retry = 5;
                do {
                    this.mCMSManager = IAwareCMSManager.getICMSManager();
                    if (this.mCMSManager != null) {
                        break;
                    }
                    retry--;
                    try {
                        Thread.sleep(200);
                        continue;
                    } catch (InterruptedException e) {
                        AwareLog.e(TAG, "InterruptedException occured");
                        continue;
                    }
                } while (retry > 0);
            }
            if (this.mCMSManager != null) {
                return IAwareCMSManager.getCustConfig(this.mCMSManager, featureName, configName);
            }
            AwareLog.i(TAG, "getAwareCustConfig can not find service awareservice.");
            return null;
        } catch (RemoteException e2) {
            AwareLog.e(TAG, "getAwareCustConfig RemoteException");
            this.mCMSManager = null;
            return null;
        }
    }

    private AwareConfig getAwareConfig(String featureName, String configName) {
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                return IAwareCMSManager.getConfig(awareservice, featureName, configName);
            }
            AwareLog.i(TAG, "getAwareConfig can not find service awareservice.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareConfig RemoteException");
            return null;
        }
    }

    private String getWhiteListItem(AwareConfig.Item item) {
        String whiteList = null;
        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
        if (subItemList == null) {
            return null;
        }
        Iterator<AwareConfig.SubItem> it = subItemList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            AwareConfig.SubItem subItem = it.next();
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (GROUP_WHITELIST.equals(itemName)) {
                whiteList = itemValue;
                break;
            }
        }
        return whiteList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0013, code lost:
        r1 = r0.getConfigList();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0017, code lost:
        if (r1 != null) goto L_0x001a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0019, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001a, code lost:
        r2 = new android.util.ArrayMap<>();
        r3 = r1.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        if (r3.hasNext() == false) goto L_0x0056;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        r4 = r3.next();
        r6 = r4.getProperties();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0034, code lost:
        if (r6 != null) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0046, code lost:
        if (GROUP_BG_TYPE.equals(r6.get("type")) == false) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0048, code lost:
        r8 = getWhiteListItem(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004c, code lost:
        if (r8 == null) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004e, code lost:
        r2.put(1, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0056, code lost:
        monitor-enter(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0059, code lost:
        if (r14.mHasReadXml == false) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005b, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005c, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005d, code lost:
        r3 = r2.size();
        r6 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0063, code lost:
        if (r6 >= r3) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0065, code lost:
        r7 = r2.valueAt(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006f, code lost:
        if (r7.isEmpty() == false) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0072, code lost:
        r8 = r2.keyAt(r6);
        r9 = r7.split(";");
        r10 = r9.length;
        r11 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0080, code lost:
        if (r11 >= r10) goto L_0x0097;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0082, code lost:
        r12 = r9[r11].trim();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x008d, code lost:
        if (r12.isEmpty() != false) goto L_0x0094;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x008f, code lost:
        r14.mProcessWhiteListMap.put(r12, r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0094, code lost:
        r11 = r11 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0097, code lost:
        r6 = r6 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x009a, code lost:
        r14.mHasReadXml = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x009c, code lost:
        monitor-exit(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x009d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0008, code lost:
        r0 = getAwareConfig(FEATURENAME, CONFIG_CONTROL_GROUP);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        if (r0 != null) goto L_0x0013;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        return;
     */
    private void setWhiteListFromXml() {
        synchronized (this) {
            if (this.mHasReadXml) {
            }
        }
    }

    public void enable() {
        initialize();
    }

    public void disable() {
        deInitialize();
    }

    public int isWhiteList(String processName) {
        if (processName == null) {
            return -1;
        }
        synchronized (this) {
            Integer groupType = this.mProcessWhiteListMap.get(processName);
            if (groupType == null) {
                return -1;
            }
            int intValue = groupType.intValue();
            return intValue;
        }
    }
}
