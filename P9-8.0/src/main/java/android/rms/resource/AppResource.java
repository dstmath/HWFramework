package android.rms.resource;

import android.app.mtm.MultiTaskPolicy;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.rms.HwSysSpeedRes;
import android.rms.config.ResourceConfig;
import android.rms.control.ResourceFlowControl;
import android.rms.utils.Utils;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class AppResource extends HwSysSpeedRes {
    private static final String TAG = "RMS.AppResource";
    private static final int TYPE_APP_PERMIT = 5;
    private static final int TYPE_CLEAR_DATA = 1;
    private static final int TYPE_DISABLE_APP = 2;
    private static final int TYPE_NOTIFY_CRASHINFO = 3;
    private static final int TYPE_NOTIFY_CRASHINFO_SYSAPP = 4;
    private static AppResource mAppResource;
    private HashSet<String> mAppLaunchedInfo = new HashSet();
    private HashMap<String, Integer> mAppResourceDoPolicyConfigs;
    private long mLifeTime;

    private AppResource() {
        super(18, TAG);
        getConfig();
        this.mAppResourceDoPolicyConfigs = new HashMap();
        ArrayList<String> whitelist = getResWhiteList(0);
        if (whitelist != null && whitelist.size() > 0) {
            initAppResourceDoPolicyConfigs(whitelist);
        }
    }

    public static synchronized AppResource getInstance() {
        AppResource appResource;
        synchronized (AppResource.class) {
            if (mAppResource == null) {
                mAppResource = new AppResource();
                if (Utils.DEBUG) {
                    Log.d(TAG, "getInstance create new AppResource");
                }
            }
            appResource = mAppResource;
        }
        return appResource;
    }

    protected void onWhiteListUpdate() {
        ArrayList<String> whitelist = getResWhiteList(0);
        if (whitelist != null && whitelist.size() > 0) {
            this.mAppResourceDoPolicyConfigs.clear();
            initAppResourceDoPolicyConfigs(whitelist);
        }
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        int strategy = 1;
        if (this.mResourceConfig == null) {
            return 1;
        }
        String pkg = args.getString("pkg");
        int callingUid = args.getInt("callingUid");
        Long startTime = Long.valueOf(args.getLong("startTime"));
        int typeID = args.getInt("processType");
        boolean launchfromActivity = args.getBoolean("launchfromActivity");
        boolean isTopProcess = args.getBoolean("topProcess");
        int crachTimeInterval = this.mResourceConfig[3].getLoopInterval();
        int shortTime = this.mResourceConfig[3].getResourceStrategy();
        this.mLifeTime = SystemClock.elapsedRealtime() - startTime.longValue();
        Integer doPolicyType = null;
        boolean blaunched = false;
        synchronized (this.mAppLaunchedInfo) {
            if (pkg != null) {
                blaunched = this.mAppLaunchedInfo.contains(pkg);
                if (blaunched && (launchfromActivity ^ 1) != 0) {
                    this.mAppLaunchedInfo.remove(pkg);
                }
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "acquire mLifeTime:" + this.mLifeTime + " pkg " + pkg + " blaunched " + blaunched + " launchfromActivity " + launchfromActivity + " inteval " + crachTimeInterval + " top " + isTopProcess);
        }
        if (typeID == 0 && launchfromActivity && ((this.mLifeTime <= ((long) shortTime) || (this.mLifeTime < ((long) crachTimeInterval) && this.mLifeTime > ((long) shortTime) && isTopProcess)) && blaunched)) {
            doPolicyType = (Integer) this.mAppResourceDoPolicyConfigs.get(pkg);
            if (doPolicyType != null && doPolicyType.intValue() == 5) {
                return 1;
            }
            doPolicyType = Integer.valueOf(3);
            if (Utils.DEBUG) {
                Log.d(TAG, "third app " + pkg + ", doPolicyType:" + doPolicyType);
            }
        } else if (typeID == 2) {
            doPolicyType = (Integer) this.mAppResourceDoPolicyConfigs.get(pkg);
            if (doPolicyType == null) {
                doPolicyType = Integer.valueOf(4);
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "system app " + pkg + ", doPolicyType:" + doPolicyType);
            }
        }
        if (doPolicyType != null && isResourceSpeedOverload(callingUid, pkg, typeID)) {
            Bundle data = new Bundle();
            data.putInt("callingUid", callingUid);
            data.putString("pkg", pkg);
            this.mResourceManger.dispatch(18, new MultiTaskPolicy(doPolicyType.intValue(), data));
            strategy = getSpeedOverloadStrategy(typeID);
        }
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
        return 1;
    }

    public void clear(int callingUid, String pkg, int processTpye) {
        this.mResourceFlowControl.removeResourceSpeedRecord(super.getResourceId(callingUid, pkg, processTpye));
    }

    protected int getSpeedOverloadStrategy(int typeID) {
        return this.mResourceConfig[typeID].getResourceStrategy();
    }

    protected Bundle createBundleForResource(long id, int typeID, ResourceConfig config, ResourceFlowControl resourceCountControl) {
        if (typeID == 0) {
            this.mOverloadNumber = resourceCountControl.getCountInPeroid(id);
        }
        Bundle bundle = new Bundle();
        bundle.putLong(Utils.BUNDLE_THIRD_PARTY_APP_LIFETIME, this.mLifeTime);
        return bundle;
    }

    private void initAppResourceDoPolicyConfigs(ArrayList<String> whiteList) {
        int whiteListCount = whiteList.size();
        for (int i = 0; i < whiteListCount; i++) {
            String[] list = ((String) whiteList.get(i)).split(":");
            if (list.length == 2) {
                Integer policy = Integer.valueOf(Integer.parseInt(list[1]));
                if (((Integer) this.mAppResourceDoPolicyConfigs.get(list[0])) != null) {
                    if (Utils.DEBUG) {
                        Log.d(TAG, " PolicyConfigs is already!");
                    }
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
