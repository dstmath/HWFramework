package com.android.server.rms.iaware.resource;

import android.os.IBinder;
import android.os.RemoteException;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareLog;
import android.rms.iaware.IAwareCMSManager;
import com.android.server.rms.iaware.cpu.AuxRtgSched;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class StartResParallelManager {
    private static final String BINDAPP_STAGE_NAME = "bindApplication";
    private static final String CLASS_LOADER = "classLoader";
    private static final String CONFIG_NAME = "StartResParallelConfig";
    private static final String FEATURE_NAME = "AppQuickStart";
    private static final Object LOCK = new Object();
    private static final String PRELOADTHREAD_AUXTYPE = "preload_thread";
    private static final String STAGE = "stage";
    private static final int SWITCH_OPEN = 1;
    private static final String TAG = "StartResParallelManager";
    private static StartResParallelManager sInstance = null;
    private AtomicBoolean mClassLoaderSwitch = new AtomicBoolean(false);
    private AtomicBoolean mFeatureSwicth = new AtomicBoolean(false);

    public static StartResParallelManager getInstance() {
        StartResParallelManager startResParallelManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new StartResParallelManager();
            }
            startResParallelManager = sInstance;
        }
        return startResParallelManager;
    }

    public void enable() {
        parseProductAndPlatformConfig();
        if (!this.mClassLoaderSwitch.get()) {
            AwareLog.d(TAG, " subswitches disable");
        } else {
            this.mFeatureSwicth.set(true);
        }
    }

    public void disable() {
        this.mFeatureSwicth.set(false);
        resetSubSwitches();
    }

    private void parseProductAndPlatformConfig() {
        try {
            IBinder awareservice = IAwareCMSManager.getICMSManager();
            if (awareservice != null) {
                parseStartResParallelConfig(IAwareCMSManager.getCustConfig(awareservice, FEATURE_NAME, CONFIG_NAME));
            } else {
                AwareLog.w(TAG, "getAwareConfig can not find service awareService.");
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "getAwareConfig RemoteException");
        }
    }

    private void parseStartResParallelConfig(AwareConfig config) {
        Map<String, String> configProperties;
        if (config == null) {
            AwareLog.w(TAG, "both aware product and platform is not configurated!");
            return;
        }
        List<AwareConfig.Item> itemList = config.getConfigList();
        if (itemList != null) {
            for (AwareConfig.Item item : itemList) {
                if (!(item == null || (configProperties = item.getProperties()) == null || !BINDAPP_STAGE_NAME.equals(configProperties.get(STAGE)))) {
                    parseBindApplicationConfig(item);
                }
            }
        }
    }

    private void parseBindApplicationConfig(AwareConfig.Item item) {
        List<AwareConfig.SubItem> subItemList = item.getSubItemList();
        if (subItemList == null) {
            AwareLog.w(TAG, "get bindApplication config item fialed!");
            return;
        }
        for (AwareConfig.SubItem subItem : subItemList) {
            if (subItem != null) {
                String itemName = subItem.getName();
                String itemValue = subItem.getValue();
                if (CLASS_LOADER.equals(itemName)) {
                    this.mClassLoaderSwitch.set(parseConfigSwitch(itemValue));
                }
            }
        }
    }

    private boolean parseConfigSwitch(String data) {
        if (data == null) {
            return false;
        }
        try {
            if (Integer.parseInt(data) == 1) {
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            AwareLog.w(TAG, "parseNetQosConfigValue parseInt value failed!");
            return false;
        }
    }

    private void resetSubSwitches() {
        this.mClassLoaderSwitch.set(false);
    }

    public void applyRtgPolicy(int tid, int enable) {
        if (this.mFeatureSwicth.get() && tid > 0) {
            AuxRtgSched.getInstance().setAuxRtgThread(tid, enable, PRELOADTHREAD_AUXTYPE);
            AwareLog.d(TAG, "add tid: " + tid + " to rtg, enable: " + enable);
        }
    }

    public boolean isPreloadEnable() {
        return false;
    }

    public void dumpSetPreloadEnable(PrintWriter pw, boolean enable) {
        if (pw != null) {
            if (!this.mFeatureSwicth.get()) {
                pw.println("StartResParallelManager disable.");
                return;
            }
            this.mClassLoaderSwitch.set(enable);
            pw.println("classLoader switch status: " + this.mClassLoaderSwitch.get());
        }
    }
}
