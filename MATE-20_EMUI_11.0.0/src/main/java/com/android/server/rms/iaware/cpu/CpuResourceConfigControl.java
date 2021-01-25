package com.android.server.rms.iaware.cpu;

import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import android.util.ArrayMap;
import java.util.List;
import java.util.Map;

public class CpuResourceConfigControl {
    private static final String CONFIG_CONTROL_GROUP = "control_group";
    private static final String CONFIG_GROUP_TYPE = "type";
    private static final String FEATURE_NAME = "CPU";
    private static final int GET_CMS_RETRY_TIME = 5;
    private static final int GET_CMS_SLEEP_TIME = 200;
    private static final String GROUP_BG_TYPE = "group_bg";
    public static final int GROUP_BG_VALUE = 1;
    private static final String GROUP_WHITELIST = "whitelist";
    private static final String SEPARATOR = ";";
    private static final Object SLOCK = new Object();
    private static final String TAG = "CpuResourceConfigControl";
    private static CpuResourceConfigControl sInstance;
    private IBinder mCmsManager;
    private boolean mHasReadXml = false;
    private final Map<String, Integer> mProcessWhiteListMap = new ArrayMap();

    private CpuResourceConfigControl() {
    }

    public static CpuResourceConfigControl getInstance() {
        CpuResourceConfigControl cpuResourceConfigControl;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new CpuResourceConfigControl();
            }
            cpuResourceConfigControl = sInstance;
        }
        return cpuResourceConfigControl;
    }

    private void initialize() {
        setWhiteListFromXml();
    }

    private void deInitialize() {
        synchronized (SLOCK) {
            this.mHasReadXml = false;
            this.mProcessWhiteListMap.clear();
        }
    }

    public AwareConfig getAwareCustConfig(String featureName, String configName) {
        try {
            if (this.mCmsManager == null) {
                int retry = 5;
                do {
                    this.mCmsManager = IAwareCMSManager.getICMSManager();
                    if (this.mCmsManager != null) {
                        break;
                    }
                    retry--;
                    try {
                        Thread.sleep(200);
                        continue;
                    } catch (InterruptedException e) {
                        AwareLog.e(TAG, "getAwareCustConfig InterruptedException occured");
                        continue;
                    }
                } while (retry > 0);
            }
            if (this.mCmsManager != null) {
                return IAwareCMSManager.getCustConfig(this.mCmsManager, featureName, configName);
            }
            AwareLog.i(TAG, "getAwareCustConfig can not find service awareService.");
            return null;
        } catch (RemoteException e2) {
            AwareLog.e(TAG, "getAwareCustConfig RemoteException");
            this.mCmsManager = null;
            return null;
        }
    }

    private AwareConfig getAwareConfig(String featureName, String configName) {
        try {
            IBinder awareService = IAwareCMSManager.getICMSManager();
            if (awareService != null) {
                return IAwareCMSManager.getConfig(awareService, featureName, configName);
            }
            AwareLog.i(TAG, "getAwareConfig can not find service awareService.");
            return null;
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareConfig RemoteException");
            return null;
        }
    }

    private String getWhiteListItem(AwareConfig.Item item) {
        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
        if (subItemList == null) {
            return null;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            String itemName = subItem.getName();
            String itemValue = subItem.getValue();
            if (GROUP_WHITELIST.equals(itemName)) {
                return itemValue;
            }
        }
        return null;
    }

    private void setWhiteListFromXml() {
        List<AwareConfig.Item> awareConfigItemList;
        String str;
        synchronized (SLOCK) {
            if (this.mHasReadXml) {
                return;
            }
        }
        AwareConfig awareConfig = getAwareConfig(FEATURE_NAME, CONFIG_CONTROL_GROUP);
        if (!(awareConfig == null || (awareConfigItemList = awareConfig.getConfigList()) == null)) {
            ArrayMap<Integer, String> whitelistStrMap = new ArrayMap<>();
            for (AwareConfig.Item item : awareConfigItemList) {
                Map<String, String> configPropertries = item.getProperties();
                if (!(configPropertries == null || !GROUP_BG_TYPE.equals(configPropertries.get(CONFIG_GROUP_TYPE)) || (str = getWhiteListItem(item)) == null)) {
                    whitelistStrMap.put(1, str);
                }
            }
            synchronized (SLOCK) {
                if (!this.mHasReadXml) {
                    int size = whitelistStrMap.size();
                    for (int i = 0; i < size; i++) {
                        String whiteListStr = whitelistStrMap.valueAt(i);
                        if (!whiteListStr.isEmpty()) {
                            Integer groupType = whitelistStrMap.keyAt(i);
                            for (String content : whiteListStr.split(";")) {
                                String content2 = content.trim();
                                if (!content2.isEmpty()) {
                                    this.mProcessWhiteListMap.put(content2, groupType);
                                }
                            }
                        }
                    }
                    this.mHasReadXml = true;
                }
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
        synchronized (SLOCK) {
            Integer groupType = this.mProcessWhiteListMap.get(processName);
            if (groupType == null) {
                return -1;
            }
            return groupType.intValue();
        }
    }
}
