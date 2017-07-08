package android.rms.resource;

import android.app.mtm.MultiTaskPolicy;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.HwSysResImpl;
import android.rms.HwSysResManager;
import android.rms.IUpdateWhiteListCallback;
import android.rms.IUpdateWhiteListCallback.Stub;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceFlowControl;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class AppResource extends HwSysResImpl {
    private static final boolean DEBUG = false;
    private static final String TAG = "RMS.AppResource";
    private static final int TYPE_APP_PERMIT = 5;
    private static final int TYPE_CLEAR_DATA = 1;
    private static final int TYPE_DISABLE_APP = 2;
    private static final int TYPE_NOTIFY_CRASHINFO = 3;
    private static final int TYPE_NOTIFY_CRASHINFO_SYSAPP = 4;
    private static AppResource mAppResource;
    private HashSet<String> mAppLaunchedInfo;
    private HashMap<String, Integer> mAppResourceDoPolicyConfigs;
    private long mLifeTime;
    private int mOverloadNumber;
    private int mOverloadPeroid;
    private ResourceConfig[] mResourceConfig;
    private ResourceFlowControl mResourceFlowControl;
    private HwSysResManager mResourceManger;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback;

    public AppResource() {
        this.mAppLaunchedInfo = new HashSet();
        this.mUpdateWhiteListCallback = new Stub() {
            public void update() throws RemoteException {
                AppResource resource = AppResource.getInstance();
                if (resource == null) {
                    Log.e(AppResource.TAG, "Notification Resource update get the instance is null");
                    return;
                }
                ArrayList<String> whiteList = resource.getResWhiteList(19, 0);
                if (whiteList.size() > 0) {
                    AppResource.this.mAppResourceDoPolicyConfigs.clear();
                    AppResource.this.initAppResourceDoPolicyConfigs(whiteList);
                } else {
                    Log.e(AppResource.TAG, "APP Resource update nameList failed!!!");
                }
            }
        };
        this.mResourceFlowControl = new ResourceFlowControl();
        if (!registerResourceCallback(this.mUpdateWhiteListCallback)) {
            Log.e(TAG, "APP Resource register callback failed");
        }
        getConfig(19);
        this.mAppResourceDoPolicyConfigs = new HashMap();
        ArrayList<String> whiteList = super.getResWhiteList(19, 0);
        if (whiteList.size() > 0) {
            initAppResourceDoPolicyConfigs(whiteList);
        }
    }

    public static synchronized AppResource getInstance() {
        AppResource appResource;
        synchronized (AppResource.class) {
            if (mAppResource == null) {
                mAppResource = new AppResource();
            }
            appResource = mAppResource;
        }
        return appResource;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        int strategy = TYPE_CLEAR_DATA;
        if (this.mResourceConfig == null) {
            return TYPE_CLEAR_DATA;
        }
        Bundle data;
        String pkg = args.getString("pkg");
        int callingUid = args.getInt("callingUid");
        Long startTime = Long.valueOf(args.getLong("startTime"));
        int typeID = args.getInt("processType");
        boolean launchfromActivity = args.getBoolean("launchfromActivity");
        int crachTimeInterval = this.mResourceConfig[TYPE_NOTIFY_CRASHINFO].getLoopInterval();
        this.mLifeTime = SystemClock.elapsedRealtime() - startTime.longValue();
        Integer doPolicyType = null;
        boolean blaunched = DEBUG;
        synchronized (this.mAppLaunchedInfo) {
            if (pkg != null) {
                blaunched = this.mAppLaunchedInfo.contains(pkg) ? true : DEBUG;
                if (blaunched && !launchfromActivity) {
                    this.mAppLaunchedInfo.remove(pkg);
                }
            }
        }
        if (typeID == 0 && launchfromActivity) {
            if (this.mLifeTime < ((long) crachTimeInterval) && blaunched) {
                doPolicyType = (Integer) this.mAppResourceDoPolicyConfigs.get(pkg);
                if (doPolicyType != null && doPolicyType.intValue() == TYPE_APP_PERMIT) {
                    return TYPE_CLEAR_DATA;
                }
                doPolicyType = Integer.valueOf(TYPE_NOTIFY_CRASHINFO);
                if (doPolicyType != null && isResourceSpeedOverload(callingUid, pkg, typeID)) {
                    data = new Bundle();
                    data.putInt("callingUid", callingUid);
                    data.putString("pkg", pkg);
                    this.mResourceManger.dispatch(19, new MultiTaskPolicy(doPolicyType.intValue(), data));
                    strategy = getSpeedOverloadStrategy(typeID);
                }
                return strategy;
            }
        }
        if (typeID == TYPE_DISABLE_APP) {
            doPolicyType = (Integer) this.mAppResourceDoPolicyConfigs.get(pkg);
            if (doPolicyType == null) {
                doPolicyType = Integer.valueOf(TYPE_NOTIFY_CRASHINFO_SYSAPP);
            }
        }
        data = new Bundle();
        data.putInt("callingUid", callingUid);
        data.putString("pkg", pkg);
        this.mResourceManger.dispatch(19, new MultiTaskPolicy(doPolicyType.intValue(), data));
        strategy = getSpeedOverloadStrategy(typeID);
        return strategy;
    }

    public int acquire(int callingUid, String pkg, int processType) {
        synchronized (this.mAppLaunchedInfo) {
            if (pkg != null) {
                if (processType > 0) {
                    this.mAppLaunchedInfo.add(pkg);
                } else if (this.mAppLaunchedInfo.contains(pkg)) {
                    this.mAppLaunchedInfo.remove(pkg);
                }
            }
        }
        return TYPE_CLEAR_DATA;
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
    }

    private void getConfig(int resourceType) {
        this.mResourceManger = HwSysResManager.getInstance();
        this.mResourceConfig = this.mResourceManger.getResourceConfig(resourceType);
        if (this.mResourceConfig == null) {
        }
    }

    private int getSpeedOverloadStrategy(int typeID) {
        return this.mResourceConfig[typeID].getResourceStrategy();
    }

    private boolean isResourceSpeedOverload(int callingUid, String pkg, int typeID) {
        long id = super.getResourceId(callingUid, pkg, typeID);
        ResourceConfig config = this.mResourceConfig[typeID];
        int threshold = config.getResourceThreshold();
        int loopInterval = config.getLoopInterval();
        int maxPeroid = config.getResourceMaxPeroid();
        if (this.mResourceFlowControl.checkSpeedOverload(id, threshold, loopInterval)) {
            this.mOverloadPeroid = this.mResourceFlowControl.getOverloadPeroid(id);
            if (this.mOverloadPeroid >= maxPeroid) {
                if (typeID == 0) {
                    this.mOverloadNumber = (int) (this.mLifeTime / 1000);
                } else {
                    this.mOverloadNumber = this.mResourceFlowControl.getOverloadNumber(id);
                }
                this.mResourceManger.recordResourceOverloadStatus(callingUid, pkg, 19, this.mOverloadNumber, this.mOverloadPeroid, 0);
                if (Log.HWINFO) {
                    Log.i(TAG, "speedOverload pkg=" + pkg + " id=" + id + " Threshold=" + threshold + " OverloadNum=" + this.mOverloadNumber + " MaxPeroid=" + maxPeroid + " OverloadPeroid=" + this.mOverloadPeroid);
                }
                return true;
            }
        }
        return DEBUG;
    }

    private void initAppResourceDoPolicyConfigs(ArrayList<String> whiteList) {
        int whiteListCount = whiteList.size();
        for (int i = 0; i < whiteListCount; i += TYPE_CLEAR_DATA) {
            String[] list = ((String) whiteList.get(i)).split(":");
            if (list.length == TYPE_DISABLE_APP) {
                Integer policy = Integer.valueOf(Integer.parseInt(list[TYPE_CLEAR_DATA]));
                if (((Integer) this.mAppResourceDoPolicyConfigs.get(list[0])) != null) {
                    this.mAppResourceDoPolicyConfigs.remove(list[0]);
                }
                this.mAppResourceDoPolicyConfigs.put(list[0], policy);
            }
        }
    }

    public int queryPkgPolicy(int type, int value, String key) {
        if (key == null) {
            return 0;
        }
        Integer policy = (Integer) this.mAppResourceDoPolicyConfigs.get(key);
        if (policy == null) {
            return 0;
        }
        return policy.intValue();
    }
}
