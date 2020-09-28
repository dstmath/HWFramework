package android.rms;

import android.annotation.SuppressLint;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.IUpdateWhiteListCallback;
import android.rms.config.ResourceConfig;
import android.rms.resource.BroadcastResource;
import android.rms.resource.NotificationResource;
import android.rms.resource.OrderedBroadcastObserveResource;
import android.rms.resource.PidsResource;
import android.rms.resource.TelephonyManagerResource;
import android.rms.utils.Utils;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HwSysResImpl implements HwSysResource {
    private static final boolean IS_ENABLE_IAWARE = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
    private static final boolean IS_ENABLE_RMS = SystemProperties.getBoolean("ro.config.enable_rms", false);
    private static final int LIST_INITIAL_CAPCITY = 16;
    private static final String TAG = "RMS.HwSysResImpl";
    protected ResourceConfig[] mResourceConfig;
    protected HwSysResManager mResourceManger;
    protected int mResourceType = -1;
    private String mTag = TAG;
    private IUpdateWhiteListCallback mUpdateWhiteListCallback = new IUpdateWhiteListCallback.Stub() {
        /* class android.rms.HwSysResImpl.AnonymousClass1 */

        @Override // android.rms.IUpdateWhiteListCallback
        public void update() throws RemoteException {
            if (Utils.DEBUG) {
                Log.d(HwSysResImpl.this.mTag, "IUpdateWhiteListCallback was called");
            }
            HwSysResImpl.this.initWhiteLists();
            HwSysResImpl.this.onWhiteListUpdate();
        }
    };
    private Map<Integer, ArrayList<String>> mWhiteListMap = new HashMap(16);
    private int[] mWhiteListTypes;

    public HwSysResImpl(int resourceType, String tag, int[] whiteListTypes) {
        this.mTag = tag;
        this.mWhiteListTypes = copyWhiteListTypes(whiteListTypes);
        this.mResourceType = resourceType;
        this.mResourceManger = HwSysResManager.getInstance();
    }

    public static HwSysResource getResource(int resourceType) {
        HwSysResource resource = null;
        if (IS_ENABLE_RMS) {
            if (resourceType == 10) {
                resource = NotificationResource.getInstance();
            } else if (resourceType == 15) {
                resource = PidsResource.getInstance();
            } else if (resourceType == 34) {
                resource = TelephonyManagerResource.getInstance();
            }
        }
        if (!IS_ENABLE_IAWARE) {
            return resource;
        }
        if (resourceType == 11) {
            return BroadcastResource.getInstance();
        }
        if (resourceType != 31) {
            return resource;
        }
        return OrderedBroadcastObserveResource.getInstance();
    }

    public int acquire(int callingUid, String pkg, int processType) {
        return 1;
    }

    public int acquire(int callingUid, String pkg, int processType, int count) {
        return 1;
    }

    public int acquire(Uri uri, IContentObserver observer, Bundle args) {
        return 1;
    }

    public int queryPkgPolicy(int type, int value, String key) {
        return 0;
    }

    public void release(int callingUid, String pkg, int processType) {
    }

    public void clear(int callingUid, String pkg, int processType) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw) {
    }

    public Bundle query() {
        return new Bundle();
    }

    private static boolean isUidSystem(int uid, String pkg) {
        int appid = UserHandle.getAppId(uid);
        return appid == 1000 || appid == 1001 || uid == 0 || "android".equals(pkg);
    }

    private int getAppType(String pkg) {
        return pkg.contains("huawei") ? 1 : 0;
    }

    public int getTypeId(int callingUid, String pkg, int processType) {
        int typeId = processType;
        if (processType == -1) {
            if (isUidSystem(callingUid, pkg)) {
                typeId = 2;
            } else if (pkg != null) {
                typeId = getAppType(pkg);
            } else {
                typeId = 0;
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "getTypeId typeID/" + typeId);
        }
        return typeId;
    }

    public long getResourceId(int callingUid, String pkg, int processType) {
        int typeId = getTypeId(callingUid, pkg, processType);
        int uid = processType == 3 ? -1 : callingUid;
        long id = (((long) typeId) << 32) + ((long) uid);
        if (Utils.DEBUG) {
            Log.d(TAG, "getResourceId  typeID/" + typeId + " uid/" + uid + " id/" + id);
        }
        return id;
    }

    /* access modifiers changed from: protected */
    public boolean registerResourceCallback(IUpdateWhiteListCallback updateCallback) {
        return HwSysResManager.getInstance().registerResourceCallback(updateCallback);
    }

    /* access modifiers changed from: protected */
    public ArrayList<String> getResWhiteList(int type) {
        if (this.mWhiteListMap.get(Integer.valueOf(type)) == null) {
            this.mWhiteListMap.put(Integer.valueOf(type), new ArrayList<>(16));
        }
        return this.mWhiteListMap.get(Integer.valueOf(type));
    }

    private ArrayList<String> initResWhiteList(int type) {
        String[] whiteLists = null;
        ArrayList<String> mList = new ArrayList<>(16);
        String configWhiteList = HwSysResManager.getInstance().getWhiteList(this.mResourceType, type);
        if (Utils.DEBUG) {
            String str = this.mTag;
            Log.d(str, "getResWhiteList put" + this.mResourceType + type + configWhiteList);
        }
        if (configWhiteList != null) {
            whiteLists = configWhiteList.split(";");
        }
        if (whiteLists == null) {
            return mList;
        }
        for (int i = 0; i < whiteLists.length; i++) {
            if (!mList.contains(whiteLists[i]) && !whiteLists[i].isEmpty()) {
                mList.add(whiteLists[i]);
                if (Utils.DEBUG) {
                    Log.d(TAG, "getResWhiteList put the name into the list  type:" + this.mResourceType + ", name:" + whiteLists[i] + " , num:" + i);
                }
            }
        }
        return mList;
    }

    @SuppressLint({"PreferForInArrayList"})
    private boolean isInWhiteList(String pkg, ArrayList<String> whiteList) {
        if (pkg == null || whiteList == null || !whiteList.contains(pkg)) {
            return false;
        }
        if (!Utils.DEBUG) {
            return true;
        }
        String str = this.mTag;
        Log.i(str, "HwSysResImpl.isWhiteList pkg = " + pkg);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isInWhiteList(String pkg, int whiteListType) {
        return isInWhiteList(pkg, this.mWhiteListMap.get(Integer.valueOf(whiteListType)));
    }

    private int[] copyWhiteListTypes(int[] whiteListTypes) {
        if (whiteListTypes == null) {
            return null;
        }
        return Arrays.copyOf(whiteListTypes, whiteListTypes.length);
    }

    /* access modifiers changed from: protected */
    public boolean getConfig() {
        if (this.mResourceConfig != null) {
            return true;
        }
        this.mResourceConfig = this.mResourceManger.getResourceConfig(this.mResourceType);
        if (this.mResourceConfig == null && this.mResourceType == 11) {
            getWhiteListConfig();
            return true;
        } else if (this.mResourceConfig == null) {
            return false;
        } else {
            if (Utils.DEBUG) {
                for (int i = 0; i < this.mResourceConfig.length; i++) {
                    String str = this.mTag;
                    Log.d(str, "getConfig threshold" + this.mResourceConfig[i].getResourceThreshold());
                }
            }
            getWhiteListConfig();
            return true;
        }
    }

    private void getWhiteListConfig() {
        int[] iArr = this.mWhiteListTypes;
        if (iArr != null && iArr.length > 0) {
            if (needUpdateWhiteList() && !registerResourceCallback(this.mUpdateWhiteListCallback) && Utils.DEBUG) {
                String str = this.mTag;
                Log.e(str, "Resource[type=" + this.mResourceManger + "] register callback failed");
            }
            initWhiteLists();
        }
    }

    /* access modifiers changed from: protected */
    public void onWhiteListUpdate() {
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initWhiteLists() {
        if (this.mWhiteListMap != null) {
            int[] iArr = this.mWhiteListTypes;
            if (iArr.length > 0) {
                for (int type : iArr) {
                    ArrayList<String> whitelist = initResWhiteList(type);
                    if (whitelist.size() > 0) {
                        if (Utils.DEBUG) {
                            Log.d(this.mTag, "initWhiteLists was called" + whitelist);
                        }
                        this.mWhiteListMap.put(Integer.valueOf(type), whitelist);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean needUpdateWhiteList() {
        return true;
    }
}
