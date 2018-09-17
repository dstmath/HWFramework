package com.android.server.rms.iaware.cpu;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareLog;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.util.ArrayMap;
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
    private ICMSManager mCMSManager;
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
                while (true) {
                    this.mCMSManager = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
                    if (this.mCMSManager == null) {
                        retry--;
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            AwareLog.e(TAG, "InterruptedException occured");
                        }
                        if (retry <= 0) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            if (this.mCMSManager != null) {
                return this.mCMSManager.getCustConfig(featureName, configName);
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
            ICMSManager awareservice = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (awareservice != null) {
                return awareservice.getConfig(featureName, configName);
            }
            AwareLog.i(TAG, "getAwareConfig can not find service awareservice.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareConfig RemoteException");
            return null;
        }
    }

    private String getWhiteListItem(Item item) {
        String whiteList = null;
        List<SubItem> subItemList = item.getSubItemList();
        if (subItemList == null) {
            return null;
        }
        for (SubItem subItem : subItemList) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (GROUP_WHITELIST.equals(itemName)) {
                whiteList = itemValue;
                break;
            }
        }
        return whiteList;
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            r3 = getAwareConfig(FEATURENAME, CONFIG_CONTROL_GROUP);
     */
    /* JADX WARNING: Missing block: B:8:0x001c, code:
            if (r3 != null) goto L_0x0022;
     */
    /* JADX WARNING: Missing block: B:9:0x001e, code:
            return;
     */
    /* JADX WARNING: Missing block: B:13:0x0022, code:
            r4 = r3.getConfigList();
     */
    /* JADX WARNING: Missing block: B:14:0x0026, code:
            if (r4 != null) goto L_0x0029;
     */
    /* JADX WARNING: Missing block: B:15:0x0028, code:
            return;
     */
    /* JADX WARNING: Missing block: B:16:0x0029, code:
            r16 = new android.util.ArrayMap();
            r12 = r4.iterator();
     */
    /* JADX WARNING: Missing block: B:18:0x0036, code:
            if (r12.hasNext() == false) goto L_0x0070;
     */
    /* JADX WARNING: Missing block: B:19:0x0038, code:
            r11 = (android.rms.iaware.AwareConfig.Item) r12.next();
            r5 = r11.getProperties();
     */
    /* JADX WARNING: Missing block: B:20:0x0042, code:
            if (r5 == null) goto L_0x0032;
     */
    /* JADX WARNING: Missing block: B:22:0x0058, code:
            if (GROUP_BG_TYPE.equals((java.lang.String) r5.get("type")) == false) goto L_0x0032;
     */
    /* JADX WARNING: Missing block: B:23:0x005a, code:
            r14 = getWhiteListItem(r11);
     */
    /* JADX WARNING: Missing block: B:24:0x0060, code:
            if (r14 == null) goto L_0x0032;
     */
    /* JADX WARNING: Missing block: B:25:0x0062, code:
            r16.put(java.lang.Integer.valueOf(1), r14);
     */
    /* JADX WARNING: Missing block: B:26:0x0070, code:
            monitor-enter(r20);
     */
    /* JADX WARNING: Missing block: B:29:0x0077, code:
            if (r20.mHasReadXml == false) goto L_0x007b;
     */
    /* JADX WARNING: Missing block: B:30:0x0079, code:
            monitor-exit(r20);
     */
    /* JADX WARNING: Missing block: B:31:0x007a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:33:?, code:
            r13 = r16.size();
            r10 = 0;
     */
    /* JADX WARNING: Missing block: B:34:0x0080, code:
            if (r10 >= r13) goto L_0x00c9;
     */
    /* JADX WARNING: Missing block: B:35:0x0082, code:
            r15 = (java.lang.String) r16.valueAt(r10);
     */
    /* JADX WARNING: Missing block: B:36:0x008e, code:
            if (r15.isEmpty() == false) goto L_0x0093;
     */
    /* JADX WARNING: Missing block: B:37:0x0090, code:
            r10 = r10 + 1;
     */
    /* JADX WARNING: Missing block: B:38:0x0093, code:
            r8 = (java.lang.Integer) r16.keyAt(r10);
            r7 = r15.split(";");
            r17 = 0;
            r18 = r7.length;
     */
    /* JADX WARNING: Missing block: B:40:0x00ad, code:
            if (r17 >= r18) goto L_0x0090;
     */
    /* JADX WARNING: Missing block: B:41:0x00af, code:
            r6 = r7[r17].trim();
     */
    /* JADX WARNING: Missing block: B:42:0x00b9, code:
            if (r6.isEmpty() != false) goto L_0x00c6;
     */
    /* JADX WARNING: Missing block: B:43:0x00bb, code:
            r20.mProcessWhiteListMap.put(r6, r8);
     */
    /* JADX WARNING: Missing block: B:44:0x00c6, code:
            r17 = r17 + 1;
     */
    /* JADX WARNING: Missing block: B:45:0x00c9, code:
            r20.mHasReadXml = true;
     */
    /* JADX WARNING: Missing block: B:46:0x00d1, code:
            monitor-exit(r20);
     */
    /* JADX WARNING: Missing block: B:47:0x00d2, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
            Integer groupType = (Integer) this.mProcessWhiteListMap.get(processName);
            if (groupType != null) {
                int intValue = groupType.intValue();
                return intValue;
            }
            return -1;
        }
    }
}
