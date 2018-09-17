package com.android.server.rms.record;

import android.app.mtm.MultiTaskManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.rms.utils.Utils;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import com.android.internal.os.SomeArgs;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class ResourceRecordStore {
    private static final int IS_ACTION = 1;
    private static int MAX_NUM_BIGDATAINFOS_IN_LIST = (Utils.IS_DEBUG_VERSION ? 1 : 20);
    private static final String TAG = "RMS.ResourceRecordStore";
    private static ResourceRecordStore mResourceRecordStore = null;
    private List<BigDataInfo> mBigDataInfos = new ArrayList();
    private Context mContext = null;
    private Handler mHandler = null;
    private final HashMap<Long, ResourceOverloadRecord> mResourceStatusMap = new HashMap();
    private SharedPreferences mSharedPreferences = null;

    private static final class BigDataInfo {
        public String mPkg;
        public int mResourceType;
        public int mUid;
        public int overloadNum;
        public int speedOverLoadPeroid;
        public int totalNum;

        /* synthetic */ BigDataInfo(BigDataInfo -this0) {
            this();
        }

        private BigDataInfo() {
            this.mPkg = "";
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
            if (mResourceRecordStore == null) {
                mResourceRecordStore = new ResourceRecordStore(context);
            }
            resourceRecordStore = mResourceRecordStore;
        }
        return resourceRecordStore;
    }

    public static synchronized ResourceRecordStore getInstance() {
        ResourceRecordStore resourceRecordStore;
        synchronized (ResourceRecordStore.class) {
            resourceRecordStore = mResourceRecordStore;
        }
        return resourceRecordStore;
    }

    public void dumpImpl(PrintWriter pw) {
        pw.println("System Resource Manager");
        synchronized (this.mResourceStatusMap) {
            for (Entry<Long, ResourceOverloadRecord> entry : this.mResourceStatusMap.entrySet()) {
                ResourceOverloadRecord record = (ResourceOverloadRecord) entry.getValue();
                pw.println("Process use resource overload:uid=" + record.getUid() + " pkg=" + record.getPackageName() + " resourceType=" + record.getResourceType() + " mSpeedOverloadNum=" + record.getSpeedOverloadNum() + " mSpeedOverLoadPeroid=" + record.getSpeedOverLoadPeroid() + " mCountOverLoadNum=" + record.getCountOverLoadNum() + " pid =" + record.getPid());
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
            record = (ResourceOverloadRecord) this.mResourceStatusMap.get(Long.valueOf(id));
            if (record == null) {
                record = createResourceStatusRecord(id);
            }
        }
        return record;
    }

    public void setMessageHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    /* JADX WARNING: Missing block: B:9:0x0056, code:
            return;
     */
    /* JADX WARNING: Missing block: B:13:0x0068, code:
            r9 = com.android.server.rms.record.ResourceUtils.getProcessTypeId(r16, r8, -1);
     */
    /* JADX WARNING: Missing block: B:14:0x0071, code:
            if (android.rms.utils.Utils.DEBUG == false) goto L_0x00bb;
     */
    /* JADX WARNING: Missing block: B:15:0x0073, code:
            android.util.Log.w(TAG, "getResourceOverloadMax: pkg=" + r8 + ", hardThreshold=" + r2 + ", overLoadNum=" + r7 + ", type=" + r19 + ", killingprocessType=" + r9);
     */
    /* JADX WARNING: Missing block: B:16:0x00bb, code:
            if (r3 != false) goto L_0x00fd;
     */
    /* JADX WARNING: Missing block: B:17:0x00bd, code:
            if (r7 <= r2) goto L_0x00fd;
     */
    /* JADX WARNING: Missing block: B:19:0x00c1, code:
            if (r9 != r19) goto L_0x00fd;
     */
    /* JADX WARNING: Missing block: B:20:0x00c3, code:
            r6 = android.app.mtm.MultiTaskManager.getInstance();
     */
    /* JADX WARNING: Missing block: B:21:0x00c7, code:
            if (r6 == null) goto L_0x0101;
     */
    /* JADX WARNING: Missing block: B:23:0x00cf, code:
            if (r6.forcestopApps(r17) == false) goto L_0x0101;
     */
    /* JADX WARNING: Missing block: B:24:0x00d1, code:
            cleanResRecordAppDied(r16, r17);
     */
    /* JADX WARNING: Missing block: B:25:0x00d6, code:
            if (android.rms.utils.Utils.DEBUG != false) goto L_0x00dc;
     */
    /* JADX WARNING: Missing block: B:27:0x00da, code:
            if (android.rms.utils.Utils.HWFLOW == false) goto L_0x00fd;
     */
    /* JADX WARNING: Missing block: B:28:0x00dc, code:
            android.util.Log.d(TAG, "killOverloadApp " + r8 + "successfully!");
     */
    /* JADX WARNING: Missing block: B:29:0x00fd, code:
            return;
     */
    /* JADX WARNING: Missing block: B:34:0x0103, code:
            if (android.rms.utils.Utils.DEBUG != false) goto L_0x0109;
     */
    /* JADX WARNING: Missing block: B:36:0x0107, code:
            if (android.rms.utils.Utils.HWFLOW == false) goto L_0x00fd;
     */
    /* JADX WARNING: Missing block: B:37:0x0109, code:
            android.util.Log.d(TAG, "killOverloadApp " + r8 + "failed!");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleOverloadResource(int callingUid, int pid, int resourceType, int type) {
        long id = ResourceUtils.getResourceId(callingUid, pid, resourceType);
        synchronized (this.mResourceStatusMap) {
            ResourceOverloadRecord record = (ResourceOverloadRecord) this.mResourceStatusMap.get(Long.valueOf(id));
            if (record != null) {
                boolean isInWhiteList = record.isInWhiteList();
                int overLoadNum = record.getCountOverLoadNum();
                int hardThreshold = record.getHardThreshold();
                String packageName = record.getPackageName();
            } else if (Utils.DEBUG) {
                Log.d(TAG, "get record failed uid " + callingUid + " id " + id + " resourceType " + resourceType);
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
        int uid = msg.arg1;
        int resourceType = msg.arg2;
        SomeArgs args = msg.obj;
        String pkg = args.arg1;
        int overloadNum = args.argi1;
        int speedOverLoadPeroid = args.argi2;
        int totalNum = args.argi3;
        int pid = args.argi4;
        args.recycle();
        if (resourceType == 16) {
            pkg = Utils.getPackageNameByUid(uid);
            if (pkg == null) {
                pkg = "";
            }
        }
        Bundle bundle = msg.getData();
        createUploadBigDataInfos(uid, resourceType, pkg, overloadNum, speedOverLoadPeroid, totalNum, bundle);
        checkUploadBigDataInfos();
        int hardThreshold = 0;
        if (bundle.containsKey("hard_threshold")) {
            hardThreshold = bundle.getInt("hard_threshold");
            if (Utils.DEBUG) {
                Log.d(TAG, "recordResourceOverloadStatus! hardThreshold:" + hardThreshold);
            }
        }
        if (bundle.containsKey("current_count")) {
            totalNum = bundle.getInt("current_count");
            if (Utils.DEBUG) {
                Log.d(TAG, "recordResourceOverloadStatus! curOverloadCount:" + totalNum);
            }
        }
        ResourceOverloadRecord record = getResourceStatusRecord(uid, pid, resourceType);
        record.setInWhiteList(bundle.getBoolean("isInWhiteList", false));
        record.setUid(uid);
        record.setPid(pid);
        record.setPackageName(pkg);
        record.setResourceType(resourceType);
        if (overloadNum > 0) {
            record.setSpeedOverloadNum(overloadNum);
        }
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

    public void createAndCheckUploadBigDataInfos(int uid, int resourceType, String pkg, int overloadNum, int speedOverLoadPeriod, int totalNum, Bundle bundle) {
        if (Utils.DEBUG) {
            Log.d(TAG, "createAndCheckUploadBigDataInfos!");
        }
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = 12;
            msg.arg1 = uid;
            msg.arg2 = resourceType;
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = pkg;
            args.argi1 = overloadNum;
            args.argi2 = speedOverLoadPeriod;
            args.argi3 = totalNum;
            msg.obj = args;
            if (bundle != null) {
                msg.setData(bundle);
            }
            this.mHandler.sendMessage(msg);
        }
    }

    public void createAndCheckUploadBigDataInfos(Message msg) {
        if (msg != null) {
            int uid = msg.arg1;
            int resourceType = msg.arg2;
            SomeArgs args = msg.obj;
            String pkg = args.arg1;
            int overloadNum = args.argi1;
            int speedOverLoadPeriod = args.argi2;
            int totalNum = args.argi3;
            args.recycle();
            createUploadBigDataInfos(uid, resourceType, pkg, overloadNum, speedOverLoadPeriod, totalNum, msg.getData());
            checkUploadBigDataInfos();
        }
    }

    private void createUploadBigDataInfos(int uid, int resourceType, String pkg, int overloadNum, int speedOverLoadPeroid, int totalNum, Bundle bundle) {
        BigDataInfo uploadInfo = findBigDataInfoInList(pkg, resourceType);
        if (bundle != null) {
            if (bundle.containsKey("third_party_app_lifetime") && Utils.DEBUG) {
                Log.d(TAG, "createUploadBigDataInfos! BUNDLE_THIRD_PARTY_APP_LIFETIME");
            }
            if (bundle.containsKey("third_party_app_usetime") && Utils.DEBUG) {
                Log.d(TAG, "createUploadBigDataInfos! BUNDLE_THIRD_PARTY_APP_USETIME");
            }
        }
        if (uploadInfo != null) {
            uploadInfo.overloadNum += overloadNum;
            if (uploadInfo.speedOverLoadPeroid > speedOverLoadPeroid) {
                speedOverLoadPeroid = uploadInfo.speedOverLoadPeroid;
            }
            uploadInfo.speedOverLoadPeroid = speedOverLoadPeroid;
            if (uploadInfo.totalNum > totalNum) {
                totalNum = uploadInfo.totalNum;
            }
            uploadInfo.totalNum = totalNum;
        } else {
            uploadInfo = new BigDataInfo();
            if (pkg == null) {
                pkg = "";
            }
            uploadInfo.mPkg = pkg;
            uploadInfo.mUid = uid;
            uploadInfo.mResourceType = resourceType;
            uploadInfo.overloadNum = overloadNum;
            uploadInfo.speedOverLoadPeroid = speedOverLoadPeroid;
            uploadInfo.totalNum = totalNum;
            this.mBigDataInfos.add(uploadInfo);
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "createUploadBigDataInfos!");
        }
    }

    private void checkUploadBigDataInfos() {
        if (this.mBigDataInfos != null) {
            if (this.mBigDataInfos.size() >= MAX_NUM_BIGDATAINFOS_IN_LIST) {
                uploadBigDataInfos();
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "checkUploadBigDataInfos!");
            }
        }
    }

    private BigDataInfo findBigDataInfoInList(String pkg, int resourceType) {
        for (BigDataInfo mBigDataInfo : this.mBigDataInfos) {
            if (mBigDataInfo.mPkg.equals(pkg) && mBigDataInfo.mResourceType == resourceType) {
                return mBigDataInfo;
            }
        }
        return null;
    }

    public void uploadBigDataInfos() {
        if (Utils.DEBUG) {
            Log.d(TAG, "uploadBigDataInfos!");
        }
        if (this.mBigDataInfos != null && this.mBigDataInfos.size() != 0) {
            if (Utils.IS_DEBUG_VERSION || !isExceedMonthlyUploadBigdataInfoCount()) {
                for (BigDataInfo bigDataInfo : this.mBigDataInfos) {
                    uploadBigDataInfoToIMonitor(bigDataInfo);
                }
                addToMonthlyUploadBigdataInfoCount(this.mBigDataInfos.size());
                this.mBigDataInfos.clear();
                return;
            }
            this.mBigDataInfos.clear();
        }
    }

    private boolean uploadBigDataInfoToIMonitor(BigDataInfo bigDataInfo) {
        if (bigDataInfo == null) {
            return false;
        }
        EventStream eStream = IMonitor.openEventStream(ResourceUtils.FAULT_CODE_BIGDATA);
        if (eStream != null) {
            eStream.setParam((short) 0, bigDataInfo.mPkg);
            eStream.setParam((short) 1, bigDataInfo.mResourceType);
            eStream.setParam((short) 2, bigDataInfo.overloadNum);
            eStream.setParam((short) 3, bigDataInfo.totalNum);
            IMonitor.sendEvent(eStream);
            IMonitor.closeEventStream(eStream);
            if (Utils.DEBUG) {
                Log.i(TAG, "uploadBigDataInfoToIMonitor! Bigdatainfo" + bigDataInfo.toString());
            }
            return true;
        }
        Log.w(TAG, "Send FAULT_CODE_BIGDATA failed for :" + bigDataInfo.toString());
        return false;
    }

    private void addToMonthlyUploadBigdataInfoCount(int uploadCount) {
        if (isSharedPrefsExist()) {
            int currentCount = this.mSharedPreferences.getInt(ResourceUtils.MONTHLY_BIGDATA_INFO_UPLOAD_LIMIT, 0);
            Log.i(TAG, "mSharedPreferences current count is " + currentCount);
            Editor editor = this.mSharedPreferences.edit();
            editor.putInt(ResourceUtils.MONTHLY_BIGDATA_INFO_UPLOAD_LIMIT, currentCount + uploadCount);
            editor.commit();
            return;
        }
        Log.e(TAG, "mSharedPreferences do not exist");
    }

    private boolean isExceedMonthlyUploadBigdataInfoCount() {
        if (isSharedPrefsExist()) {
            int storedMonth = this.mSharedPreferences.getInt(ResourceUtils.CURRENT_MONTH, -1);
            int currentMonth = Calendar.getInstance().get(2) + 1;
            if (storedMonth != currentMonth) {
                if (Utils.DEBUG) {
                    Log.i(TAG, "mSharedPreferences storedMonth is " + storedMonth + " currentMonth is " + currentMonth);
                }
                Editor editor = this.mSharedPreferences.edit();
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
        Log.e(TAG, "mSharedPreferences do not exist");
        return false;
    }

    private boolean isSharedPrefsExist() {
        if (this.mContext == null) {
            return false;
        }
        if (this.mSharedPreferences == null) {
            this.mSharedPreferences = ResourceUtils.getPinnedSharedPrefs(this.mContext);
        }
        if (this.mSharedPreferences != null) {
            return true;
        }
        Log.i(TAG, "mSharedPreferences equals Null");
        return false;
    }

    public void notifyResourceStatus(Message msg) {
        int resourceType = msg.arg1;
        int resourceStatus = msg.arg2;
        SomeArgs args = msg.obj;
        String resourceName = args.arg1;
        Bundle bd = args.arg2;
        args.recycle();
        MultiTaskManager instance = MultiTaskManager.getInstance();
        if (instance != null) {
            instance.notifyResourceStatusOverload(resourceType, resourceName, resourceStatus, bd);
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "notifyResourceStatus!");
        }
    }

    public void uploadResourceStatusRecord(long id, ResourceOverloadRecord record) {
        synchronized (this.mResourceStatusMap) {
            this.mResourceStatusMap.put(Long.valueOf(id), record);
        }
        uploadBigDataLog(record);
    }

    public void cleanResourceRecordMap(Message msg) {
        long id = ResourceUtils.getResourceId(msg.arg1, 0, msg.arg2);
        synchronized (this.mResourceStatusMap) {
            this.mResourceStatusMap.remove(Long.valueOf(id));
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "cleanResourceRecordMap!");
        }
    }

    public void cleanResRecordAppDied(int uid, int pid) {
        synchronized (this.mResourceStatusMap) {
            for (int i = 10; i <= 33; i++) {
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
        ResourceUtils.uploadBigDataLogToIMonitor(record.getResourceType(), record.getPackageName(), record.getSpeedOverloadNum(), record.getCountOverLoadNum());
    }

    public boolean isOverloadResourceRecord(int callingUid, int pid, int resourceType) {
        long id = ResourceUtils.getResourceId(callingUid, pid, resourceType);
        synchronized (this.mResourceStatusMap) {
            if (((ResourceOverloadRecord) this.mResourceStatusMap.get(Long.valueOf(id))) == null) {
                return false;
            }
            if (Utils.DEBUG) {
                Log.d(TAG, "isOverloadResourceRecord: has overload record!");
            }
            return true;
        }
    }
}
