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
    private static final int IS_ACTION = 1;
    private static int MAX_NUM_BIGDATAINFOS_IN_LIST = (Utils.IS_DEBUG_VERSION ? 1 : 20);
    private static final String TAG = "RMS.ResourceRecordStore";
    private static ResourceRecordStore mResourceRecordStore = null;
    private List<BigDataInfo> mBigDataInfos = new ArrayList();
    private Context mContext = null;
    private Handler mHandler = null;
    private final HashMap<Long, ResourceOverloadRecord> mResourceStatusMap = new HashMap<>();
    private SharedPreferences mSharedPreferences = null;

    private static final class BigDataInfo {
        public String mPkg;
        public int mResourceType;
        public int mUid;
        public int overloadNum;
        public int speedOverLoadPeroid;
        public int totalNum;

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
            for (Map.Entry<Long, ResourceOverloadRecord> entry : this.mResourceStatusMap.entrySet()) {
                ResourceOverloadRecord record = entry.getValue();
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
            record = this.mResourceStatusMap.get(Long.valueOf(id));
            if (record == null) {
                record = createResourceStatusRecord(id);
            }
        }
        return record;
    }

    public void setMessageHandler(Handler mHandler2) {
        this.mHandler = mHandler2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0052, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0071, code lost:
        r0 = com.android.server.rms.record.ResourceUtils.getProcessTypeId(r2, r10, -1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0078, code lost:
        if (android.rms.utils.Utils.DEBUG == 0) goto L_0x00b0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x007a, code lost:
        android.util.Log.w(TAG, "getResourceOverloadMax: pkg=" + r10 + ", hardThreshold=" + r7 + ", overLoadNum=" + r6 + ", type=" + r3 + ", killingprocessType=" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00b0, code lost:
        if (r9 != false) goto L_0x011a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00b2, code lost:
        if (r6 <= r7) goto L_0x011a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00b4, code lost:
        if (r0 != r3) goto L_0x011a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00b6, code lost:
        r8 = android.app.mtm.MultiTaskManager.getInstance();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00ba, code lost:
        if (r8 == null) goto L_0x00f2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00c2, code lost:
        if (r8.forcestopApps(r19) == false) goto L_0x00f4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00c4, code lost:
        cleanResRecordAppDied(r18, r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00c9, code lost:
        if (android.rms.utils.Utils.DEBUG != false) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00cd, code lost:
        if (android.rms.utils.Utils.HWFLOW == false) goto L_0x00d0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d0, code lost:
        r16 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00d3, code lost:
        r15 = new java.lang.StringBuilder();
        r16 = r0;
        r15.append("killOverloadApp ");
        r15.append(r10);
        r15.append("successfully!");
        android.util.Log.d(TAG, r15.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00f2, code lost:
        r13 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00f4, code lost:
        r16 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00f8, code lost:
        if (android.rms.utils.Utils.DEBUG != 0) goto L_0x00fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00fc, code lost:
        if (android.rms.utils.Utils.HWFLOW == false) goto L_0x011e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00fe, code lost:
        android.util.Log.d(TAG, "killOverloadApp " + r10 + "failed!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x011a, code lost:
        r13 = r19;
        r16 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x011e, code lost:
        return;
     */
    public void handleOverloadResource(int callingUid, int pid, int resourceType, int type) {
        int i = callingUid;
        int i2 = type;
        long id = ResourceUtils.getResourceId(callingUid, pid, resourceType);
        synchronized (this.mResourceStatusMap) {
            try {
                ResourceOverloadRecord record = this.mResourceStatusMap.get(Long.valueOf(id));
                if (record == null) {
                    try {
                        if (Utils.DEBUG) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("get record failed uid ");
                            sb.append(i);
                            sb.append(" id ");
                            sb.append(id);
                            sb.append(" resourceType ");
                            sb.append(resourceType);
                            Log.d(TAG, sb.toString());
                        } else {
                            int i3 = resourceType;
                        }
                    } catch (Throwable th) {
                        th = th;
                        int i4 = pid;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                } else {
                    int i5 = resourceType;
                    boolean isInWhiteList = record.isInWhiteList();
                    int overLoadNum = record.getCountOverLoadNum();
                    int hardThreshold = record.getHardThreshold();
                    String packageName = record.getPackageName();
                }
            } catch (Throwable th2) {
                th = th2;
                int i6 = pid;
                int i7 = resourceType;
                while (true) {
                    break;
                }
                throw th;
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
        Message message = msg;
        int uid = message.arg1;
        int resourceType = message.arg2;
        SomeArgs args = (SomeArgs) message.obj;
        String pkg = (String) args.arg1;
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
        String pkg2 = pkg;
        Bundle bundle = msg.getData();
        String pkg3 = pkg2;
        SomeArgs someArgs = args;
        int pid2 = pid;
        createUploadBigDataInfos(uid, resourceType, pkg2, overloadNum, speedOverLoadPeroid, totalNum, bundle);
        checkUploadBigDataInfos();
        int hardThreshold = 0;
        Bundle bundle2 = bundle;
        if (bundle2.containsKey("hard_threshold")) {
            hardThreshold = bundle2.getInt("hard_threshold");
            if (Utils.DEBUG) {
                Log.d(TAG, "recordResourceOverloadStatus! hardThreshold:" + hardThreshold);
            }
        }
        if (bundle2.containsKey("current_count")) {
            totalNum = bundle2.getInt("current_count");
            if (Utils.DEBUG) {
                Log.d(TAG, "recordResourceOverloadStatus! curOverloadCount:" + totalNum);
            }
        }
        ResourceOverloadRecord record = getResourceStatusRecord(uid, pid2, resourceType);
        record.setInWhiteList(bundle2.getBoolean("isInWhiteList", false));
        record.setUid(uid);
        record.setPid(pid2);
        record.setPackageName(pkg3);
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
        Message message = msg;
        if (message != null) {
            int uid = message.arg1;
            int resourceType = message.arg2;
            SomeArgs args = (SomeArgs) message.obj;
            int overloadNum = args.argi1;
            int speedOverLoadPeriod = args.argi2;
            int totalNum = args.argi3;
            args.recycle();
            createUploadBigDataInfos(uid, resourceType, (String) args.arg1, overloadNum, speedOverLoadPeriod, totalNum, msg.getData());
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
            uploadInfo.speedOverLoadPeroid = uploadInfo.speedOverLoadPeroid > speedOverLoadPeroid ? uploadInfo.speedOverLoadPeroid : speedOverLoadPeroid;
            uploadInfo.totalNum = uploadInfo.totalNum > totalNum ? uploadInfo.totalNum : totalNum;
        } else {
            BigDataInfo uploadInfo2 = new BigDataInfo();
            uploadInfo2.mPkg = pkg == null ? "" : pkg;
            uploadInfo2.mUid = uid;
            uploadInfo2.mResourceType = resourceType;
            uploadInfo2.overloadNum = overloadNum;
            uploadInfo2.speedOverLoadPeroid = speedOverLoadPeroid;
            uploadInfo2.totalNum = totalNum;
            this.mBigDataInfos.add(uploadInfo2);
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
        IMonitor.EventStream eStream = IMonitor.openEventStream(ResourceUtils.FAULT_CODE_BIGDATA);
        if (eStream != null) {
            eStream.setParam(0, bigDataInfo.mPkg);
            eStream.setParam(1, bigDataInfo.mResourceType);
            eStream.setParam(2, bigDataInfo.overloadNum);
            eStream.setParam(3, bigDataInfo.totalNum);
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
        SomeArgs args = (SomeArgs) msg.obj;
        String resourceName = (String) args.arg1;
        Bundle bd = (Bundle) args.arg2;
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
