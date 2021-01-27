package com.android.server.rms.record;

import android.app.mtm.MultiTaskManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.rms.utils.Utils;
import android.util.IMonitor;
import android.util.Log;
import com.android.internal.os.SomeArgs;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceRecordStore {
    private static final int HASHMAP_INIT_SIZE = 16;
    private static final int IS_ACTION = 1;
    private static final int MAX_NUM_BIGDATAINFOS_IN_LIST = (Utils.IS_DEBUG_VERSION ? 1 : 20);
    private static final String TAG = "RMS.ResourceRecordStore";
    private static ResourceRecordStore sResourceRecordStore = null;
    private List<BigDataInfo> mBigDataInfos = new ArrayList(MAX_NUM_BIGDATAINFOS_IN_LIST);
    private Context mContext = null;
    private Handler mHandler = null;
    private final Map<Long, ResourceOverloadRecord> mResourceStatusMap = new HashMap(16);
    private SharedPreferences mSharedPreferences = null;

    /* access modifiers changed from: private */
    public static final class BigDataInfo {
        private String mPkg;
        private int mResourceType;
        private int mUid;
        private int overloadNum;
        private int speedOverLoadPeroid;
        private int totalNum;

        private BigDataInfo() {
            this.mPkg = "";
        }

        public int getUid() {
            return this.mUid;
        }

        public void setUid(int uid) {
            this.mUid = uid;
        }

        public String getPkg() {
            return this.mPkg;
        }

        public void setPkg(String pkg) {
            this.mPkg = pkg;
        }

        public int getResourceType() {
            return this.mResourceType;
        }

        public void setResourceType(int resourceType) {
            this.mResourceType = resourceType;
        }

        public int getOverloadNum() {
            return this.overloadNum;
        }

        public void setOverloadNum(int overloadNumber) {
            this.overloadNum = overloadNumber;
        }

        public int getSpeedOverLoadPeroid() {
            return this.speedOverLoadPeroid;
        }

        public void setSpeedOverLoadPeroid(int speedOverloadPeroid) {
            this.speedOverLoadPeroid = speedOverloadPeroid;
        }

        public int getTotalNum() {
            return this.totalNum;
        }

        public void setTotalNum(int totalNumber) {
            this.totalNum = totalNumber;
        }

        public String toString() {
            return " mUid:" + this.mUid + " mPkg:" + this.mPkg + " mResourceType:" + this.mResourceType + " overloadNum:" + this.overloadNum + " speedOverLoadPeroid:" + this.speedOverLoadPeroid + " totalNum:" + this.totalNum;
        }
    }

    private ResourceRecordStore(Context context) {
        this.mContext = context;
    }

    public static synchronized ResourceRecordStore getInstance(Context context) {
        ResourceRecordStore resourceRecordStore;
        synchronized (ResourceRecordStore.class) {
            if (sResourceRecordStore == null) {
                sResourceRecordStore = new ResourceRecordStore(context);
            }
            resourceRecordStore = sResourceRecordStore;
        }
        return resourceRecordStore;
    }

    public static synchronized ResourceRecordStore getInstance() {
        ResourceRecordStore resourceRecordStore;
        synchronized (ResourceRecordStore.class) {
            resourceRecordStore = sResourceRecordStore;
        }
        return resourceRecordStore;
    }

    public void dumpImpl(PrintWriter pw) {
        if (pw != null) {
            pw.println("System Resource Manager");
            synchronized (this.mResourceStatusMap) {
                for (Map.Entry<Long, ResourceOverloadRecord> entry : this.mResourceStatusMap.entrySet()) {
                    ResourceOverloadRecord record = entry.getValue();
                    pw.println("Process use resource overload:uid=" + record.getUid() + " pkg=" + record.getPackageName() + " resourceType=" + record.getResourceType() + " mSpeedOverloadNum=" + record.getSpeedOverloadNum() + " mSpeedOverLoadPeroid=" + record.getSpeedOverLoadPeroid() + " mCountOverLoadNum=" + record.getCountOverLoadNum() + " pid =" + record.getPid());
                }
            }
        }
    }

    public boolean hasResourceStatusRecord(long id) {
        boolean z;
        synchronized (this.mResourceStatusMap) {
            z = this.mResourceStatusMap.get(Long.valueOf(id)) != null;
        }
        return z;
    }

    private ResourceOverloadRecord getResourceStatusRecord(int callingUid, int pid, int resourceType) {
        ResourceOverloadRecord record;
        long id = ResourceUtils.getResourceId(callingUid, pid, resourceType);
        synchronized (this.mResourceStatusMap) {
            record = this.mResourceStatusMap.get(Long.valueOf(id));
            if (record == null) {
                record = createResourceStatusRecord(id);
            }
        }
        return record;
    }

    public void setMessageHandler(Handler myHandler) {
        this.mHandler = myHandler;
    }

    public void handleOverloadResource(int callingUid, int pid, int resourceType, int type) {
        Throwable th;
        boolean isInWhiteLists;
        int overLoadNum;
        int hardThreshold;
        String packageName;
        long id = ResourceUtils.getResourceId(callingUid, pid, resourceType);
        synchronized (this.mResourceStatusMap) {
            try {
                ResourceOverloadRecord record = this.mResourceStatusMap.get(Long.valueOf(id));
                if (record == null) {
                    try {
                        if (Utils.DEBUG) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("get record failed uid ");
                            sb.append(callingUid);
                            sb.append(" id ");
                            sb.append(id);
                            sb.append(" resourceType ");
                            sb.append(resourceType);
                            Log.d(TAG, sb.toString());
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } else {
                    isInWhiteLists = record.isInWhiteList();
                    overLoadNum = record.getCountOverLoadNum();
                    hardThreshold = record.getHardThreshold();
                    packageName = record.getPackageName();
                }
            } catch (Throwable th3) {
                th = th3;
                while (true) {
                    break;
                }
                throw th;
            }
        }
        int processTypeId = ResourceUtils.getProcessTypeId(callingUid, packageName, -1);
        if (Utils.DEBUG) {
            Log.w(TAG, "getResourceOverloadMax: pkg=" + packageName + ", hardThreshold=" + hardThreshold + ", overLoadNum=" + overLoadNum + ", type=" + type + ", killingprocessType=" + processTypeId);
        }
        if (!isInWhiteLists && overLoadNum > hardThreshold && processTypeId == type) {
            MultiTaskManager mulTaskManager = MultiTaskManager.getInstance();
            if (mulTaskManager != null) {
                if (mulTaskManager.forcestopApps(pid)) {
                    cleanResRecordAppDied(callingUid, pid);
                    if (Utils.DEBUG || Utils.HWFLOW) {
                        Log.d(TAG, "killOverloadApp " + packageName + "successfully!");
                        return;
                    }
                    return;
                }
            }
            if (Utils.DEBUG || Utils.HWFLOW) {
                Log.d(TAG, "killOverloadApp " + packageName + "failed!");
            }
        }
    }

    public ResourceOverloadRecord createResourceStatusRecord(long id) {
        ResourceOverloadRecord record;
        synchronized (this.mResourceStatusMap) {
            record = new ResourceOverloadRecord();
            this.mResourceStatusMap.put(Long.valueOf(id), record);
        }
        return record;
    }

    public void recordResourceOverloadStatus(Message msg) {
        String pkg;
        if (msg != null) {
            int uid = msg.arg1;
            int resourceType = msg.arg2;
            SomeArgs resourceRecordArgs = (SomeArgs) msg.obj;
            String pkg2 = (String) resourceRecordArgs.arg1;
            if (resourceType == 16) {
                String pkg3 = Utils.getPackageNameByUid(uid);
                if (pkg3 == null) {
                    pkg = "";
                } else {
                    pkg = pkg3;
                }
            } else {
                pkg = pkg2;
            }
            Bundle bundle = msg.getData();
            if (bundle == null) {
                resourceRecordArgs.recycle();
                return;
            }
            createUploadBigDataInfos(uid, resourceType, pkg, resourceRecordArgs, bundle);
            checkUploadBigDataInfos();
            buildBigDataRecord(bundle, uid, resourceType, pkg, resourceRecordArgs);
            resourceRecordArgs.recycle();
        }
    }

    private void buildBigDataRecord(Bundle bundle, int uid, int resourceType, String pkg, SomeArgs resourceRecordArgs) {
        int hardThreshold = 0;
        if (bundle != null) {
            if (bundle.containsKey("hard_threshold")) {
                hardThreshold = bundle.getInt("hard_threshold");
                if (Utils.DEBUG) {
                    Log.d(TAG, "recordResourceOverloadStatus! hardThreshold:" + hardThreshold);
                }
            }
            int totalNum = resourceRecordArgs.argi3;
            if (bundle.containsKey("current_count")) {
                totalNum = bundle.getInt("current_count");
                if (Utils.DEBUG) {
                    Log.d(TAG, "recordResourceOverloadStatus! curOverloadCount:" + totalNum);
                }
            }
            int pid = resourceRecordArgs.argi4;
            ResourceOverloadRecord record = getResourceStatusRecord(uid, pid, resourceType);
            record.setInWhiteList(bundle.getBoolean("isInWhiteList", false));
            record.setUid(uid);
            record.setPid(pid);
            record.setPackageName(pkg);
            record.setResourceType(resourceType);
            int overloadNum = resourceRecordArgs.argi1;
            if (overloadNum > 0) {
                record.setSpeedOverloadNum(overloadNum);
            }
            int speedOverLoadPeroid = resourceRecordArgs.argi2;
            if (speedOverLoadPeroid > 0) {
                record.setSpeedOverLoadPeroid(speedOverLoadPeroid);
            }
            if (totalNum > 0) {
                record.setCountOverLoadNum(totalNum);
            }
            if (hardThreshold > 0) {
                record.setHardThreshold(hardThreshold);
            }
            uploadBigDataLog(record);
            if (Utils.DEBUG) {
                Log.d(TAG, "recordResourceOverloadStatus!");
            }
        }
    }

    public void createAndCheckUploadBigDataInfos(int uid, int resourceType, SomeArgs args, Bundle bundle) {
        if (Utils.DEBUG) {
            Log.d(TAG, "createAndCheckUploadBigDataInfos!");
        }
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = 12;
            msg.arg1 = uid;
            msg.arg2 = resourceType;
            msg.obj = args;
            if (bundle != null) {
                msg.setData(bundle);
            }
            this.mHandler.sendMessage(msg);
        }
    }

    public void createAndCheckUploadBigDataInfos(Message msg) {
        String pkg;
        if (msg != null) {
            int uid = msg.arg1;
            int resourceType = msg.arg2;
            SomeArgs resourceRecordArgs = (SomeArgs) msg.obj;
            if (resourceRecordArgs.arg1 instanceof String) {
                pkg = (String) resourceRecordArgs.arg1;
            } else {
                pkg = "";
            }
            createUploadBigDataInfos(uid, resourceType, pkg, resourceRecordArgs, msg.getData());
            checkUploadBigDataInfos();
            resourceRecordArgs.recycle();
        }
    }

    private void createUploadBigDataInfos(int uid, int resourceType, String pkg, SomeArgs resourceRecordArgs, Bundle bundle) {
        BigDataInfo uploadInfo = findBigDataInfoInList(pkg, resourceType);
        int overloadNum = resourceRecordArgs.argi1;
        int speedOverLoadPeroid = resourceRecordArgs.argi2;
        int totalNum = resourceRecordArgs.argi3;
        if (bundle != null) {
            if (bundle.containsKey("third_party_app_lifetime") && Utils.DEBUG) {
                Log.d(TAG, "createUploadBigDataInfos! BUNDLE_THIRD_PARTY_APP_LIFETIME");
            }
            if (bundle.containsKey("third_party_app_usetime") && Utils.DEBUG) {
                Log.d(TAG, "createUploadBigDataInfos! BUNDLE_THIRD_PARTY_APP_USETIME");
            }
        }
        if (uploadInfo != null) {
            uploadInfo.setOverloadNum(uploadInfo.getOverloadNum() + overloadNum);
            uploadInfo.setSpeedOverLoadPeroid(uploadInfo.getSpeedOverLoadPeroid() > speedOverLoadPeroid ? uploadInfo.getSpeedOverLoadPeroid() : speedOverLoadPeroid);
            uploadInfo.setTotalNum(uploadInfo.getTotalNum() > totalNum ? uploadInfo.getTotalNum() : totalNum);
        } else {
            BigDataInfo uploadInfo2 = new BigDataInfo();
            uploadInfo2.setPkg(pkg == null ? "" : pkg);
            uploadInfo2.setUid(uid);
            uploadInfo2.setResourceType(resourceType);
            uploadInfo2.setOverloadNum(overloadNum);
            uploadInfo2.setSpeedOverLoadPeroid(speedOverLoadPeroid);
            uploadInfo2.setTotalNum(totalNum);
            this.mBigDataInfos.add(uploadInfo2);
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "createUploadBigDataInfos!");
        }
    }

    private void checkUploadBigDataInfos() {
        List<BigDataInfo> list = this.mBigDataInfos;
        if (list != null) {
            if (list.size() >= MAX_NUM_BIGDATAINFOS_IN_LIST) {
                uploadBigDataInfos();
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "checkUploadBigDataInfos!");
            }
        }
    }

    private BigDataInfo findBigDataInfoInList(String pkg, int resourceType) {
        for (BigDataInfo bigDataInfo : this.mBigDataInfos) {
            if (bigDataInfo.getPkg().equals(pkg) && bigDataInfo.getResourceType() == resourceType) {
                return bigDataInfo;
            }
        }
        return null;
    }

    public void uploadBigDataInfos() {
        if (Utils.DEBUG) {
            Log.d(TAG, "uploadBigDataInfos!");
        }
        List<BigDataInfo> list = this.mBigDataInfos;
        if (!(list == null || list.size() == 0)) {
            if (Utils.IS_DEBUG_VERSION || !isExceedMonthlyUploadBigdataInfoCount()) {
                for (BigDataInfo bigDataInfo : this.mBigDataInfos) {
                    uploadBigDataInfoToImonitor(bigDataInfo);
                }
                addToMonthlyUploadBigdataInfoCount(this.mBigDataInfos.size());
                this.mBigDataInfos.clear();
                return;
            }
            this.mBigDataInfos.clear();
        }
    }

    private boolean uploadBigDataInfoToImonitor(BigDataInfo bigDataInfo) {
        if (bigDataInfo == null) {
            return false;
        }
        IMonitor.EventStream eventSteam = IMonitor.openEventStream((int) ResourceUtils.FAULT_CODE_BIGDATA);
        if (eventSteam != null) {
            eventSteam.setParam(0, bigDataInfo.getPkg());
            eventSteam.setParam(1, bigDataInfo.getResourceType());
            eventSteam.setParam(2, bigDataInfo.getOverloadNum());
            eventSteam.setParam(3, bigDataInfo.getTotalNum());
            IMonitor.sendEvent(eventSteam);
            IMonitor.closeEventStream(eventSteam);
            if (Utils.DEBUG) {
                Log.i(TAG, "uploadBigDataInfoToImonitor! Bigdatainfo" + bigDataInfo.toString());
            }
            return true;
        }
        Log.w(TAG, "Send FAULT_CODE_BIGDATA failed for :" + bigDataInfo.toString());
        return false;
    }

    private void addToMonthlyUploadBigdataInfoCount(int uploadCount) {
        if (!isSharedPrefsExist()) {
            Log.e(TAG, "mSharedPreferences do not exist");
            return;
        }
        int currentCount = this.mSharedPreferences.getInt(ResourceUtils.MONTHLY_BIGDATA_INFO_UPLOAD_LIMIT, 0);
        Log.i(TAG, "mSharedPreferences current count is " + currentCount);
        SharedPreferences.Editor editor = this.mSharedPreferences.edit();
        editor.putInt(ResourceUtils.MONTHLY_BIGDATA_INFO_UPLOAD_LIMIT, currentCount + uploadCount);
        editor.commit();
    }

    private boolean isExceedMonthlyUploadBigdataInfoCount() {
        if (!isSharedPrefsExist()) {
            Log.e(TAG, "mSharedPreferences do not exist");
            return false;
        }
        int storedMonth = this.mSharedPreferences.getInt(ResourceUtils.CURRENT_MONTH, -1);
        int currentMonth = Calendar.getInstance().get(2) + 1;
        if (storedMonth != currentMonth) {
            if (Utils.DEBUG) {
                Log.i(TAG, "mSharedPreferences storedMonth is " + storedMonth + " currentMonth is " + currentMonth);
            }
            SharedPreferences.Editor editor = this.mSharedPreferences.edit();
            editor.putInt(ResourceUtils.CURRENT_MONTH, currentMonth);
            editor.putInt(ResourceUtils.MONTHLY_BIGDATA_INFO_UPLOAD_LIMIT, 0);
            editor.commit();
            return false;
        } else if (this.mSharedPreferences.getInt(ResourceUtils.MONTHLY_BIGDATA_INFO_UPLOAD_LIMIT, 0) <= ResourceUtils.MONTHLY_BIGDATA_INFO_UPLOAD_COUNT_LIMIT) {
            return false;
        } else {
            if (Utils.DEBUG) {
                Log.i(TAG, "ExceedMonthlyUploadBigdataInfoCount in SharedPrefs");
            }
            return true;
        }
    }

    private boolean isSharedPrefsExist() {
        Context context = this.mContext;
        if (context == null) {
            return false;
        }
        if (this.mSharedPreferences == null) {
            this.mSharedPreferences = ResourceUtils.getPinnedSharedPrefs(context);
        }
        if (this.mSharedPreferences != null) {
            return true;
        }
        Log.i(TAG, "mSharedPreferences equals Null");
        return false;
    }

    public void notifyResourceStatus(Message msg) {
        if (msg != null) {
            int resourceType = msg.arg1;
            int resourceStatus = msg.arg2;
            if (msg.obj instanceof SomeArgs) {
                SomeArgs args = (SomeArgs) msg.obj;
                String resourceName = "";
                if (args.arg1 instanceof String) {
                    resourceName = (String) args.arg1;
                }
                Bundle bd = new Bundle();
                if (args.arg2 instanceof Bundle) {
                    bd = (Bundle) args.arg2;
                }
                args.recycle();
                MultiTaskManager instance = MultiTaskManager.getInstance();
                if (instance != null) {
                    instance.notifyResourceStatusOverload(resourceType, resourceName, resourceStatus, bd);
                }
                if (Utils.DEBUG) {
                    Log.d(TAG, "notifyResourceStatus!");
                }
            } else if (Utils.DEBUG) {
                Log.d(TAG, "msg.obj type convert fail!");
            }
        }
    }

    public void uploadResourceStatusRecord(long id, ResourceOverloadRecord record) {
        if (record != null) {
            synchronized (this.mResourceStatusMap) {
                this.mResourceStatusMap.put(Long.valueOf(id), record);
            }
            uploadBigDataLog(record);
        }
    }

    public void cleanResourceRecordMap(Message msg) {
        if (msg != null) {
            long id = ResourceUtils.getResourceId(msg.arg1, 0, msg.arg2);
            synchronized (this.mResourceStatusMap) {
                this.mResourceStatusMap.remove(Long.valueOf(id));
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "cleanResourceRecordMap!");
            }
        }
    }

    public void cleanResRecordAppDied(int uid, int pid) {
        synchronized (this.mResourceStatusMap) {
            for (int i = 10; i <= 34; i++) {
                if (i != 18) {
                    this.mResourceStatusMap.remove(Long.valueOf(ResourceUtils.getResourceId(uid, pid, i)));
                }
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "cleanResourceRecordInAppDied!");
        }
    }

    public void uploadBigDataLog(ResourceOverloadRecord record) {
        if (record != null) {
            ResourceUtils.uploadBigDataLogToIMonitor(record.getResourceType(), record.getPackageName(), record.getSpeedOverloadNum(), record.getCountOverLoadNum());
        }
    }

    public boolean isOverloadResourceRecord(int callingUid, int pid, int resourceType) {
        long id = ResourceUtils.getResourceId(callingUid, pid, resourceType);
        synchronized (this.mResourceStatusMap) {
            if (this.mResourceStatusMap.get(Long.valueOf(id)) == null) {
                return false;
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "isOverloadResourceRecord: has overload record!");
            }
            return true;
        }
    }
}
