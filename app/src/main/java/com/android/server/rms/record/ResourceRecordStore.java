package com.android.server.rms.record;

import android.app.mtm.MultiTaskManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.android.internal.os.SomeArgs;
import com.android.server.rms.utils.Utils;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public class ResourceRecordStore {
    private static final int IS_ACTION = 1;
    private static final String TAG = "RMS.ResourceRecordStore";
    private final JankLogProxy mJankLogProxy;
    private final HashMap<Long, ResourceOverloadRecord> mResourceStatusMap;

    public ResourceRecordStore() {
        this.mResourceStatusMap = new HashMap();
        this.mJankLogProxy = JankLogProxy.getInstance();
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

    public void handleOverloadResource(int callingUid, int pid, int resourceType, int type) {
        long id = ResourceUtils.getResourceId(callingUid, pid, resourceType);
        synchronized (this.mResourceStatusMap) {
            ResourceOverloadRecord record = (ResourceOverloadRecord) this.mResourceStatusMap.get(Long.valueOf(id));
            if (record == null) {
                if (Utils.DEBUG) {
                    Log.d(TAG, "get record failed uid " + callingUid + " id " + id + " resourceType " + resourceType);
                }
                return;
            }
            int overLoadNum = record.getCountOverLoadNum();
            int hardThreshold = record.getSpeedOverLoadPeroid();
            String packageName = record.getPackageName();
            int processTypeId = ResourceUtils.getProcessTypeId(callingUid, packageName, -1);
            if (Utils.DEBUG) {
                Log.w(TAG, "getResourceOverloadMax: pkg=" + packageName + ", hardThreshold=" + hardThreshold + ", overLoadNum=" + overLoadNum + ", type=" + type + ", killingprocessType=" + processTypeId);
            }
            if (overLoadNum > hardThreshold && processTypeId == type) {
                MultiTaskManager mulTaskManager = MultiTaskManager.getInstance();
                if (mulTaskManager != null && mulTaskManager.forcestopApps(pid)) {
                    cleanResRecordAppDied(callingUid, pid);
                    if (Utils.DEBUG || Log.HWINFO) {
                        Log.d(TAG, "killOverloadApp " + packageName + "successfully!");
                    }
                } else if (Utils.DEBUG || Log.HWINFO) {
                    Log.d(TAG, "killOverloadApp " + packageName + "failed!");
                }
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
        int speedOverloadNum = args.argi1;
        int speedOverLoadPeroid = args.argi2;
        int countOverLoadNum = args.argi3;
        int pid = args.argi4;
        args.recycle();
        ResourceOverloadRecord record = getResourceStatusRecord(uid, pid, resourceType);
        record.setUid(uid);
        record.setPid(pid);
        record.setPackageName(pkg);
        record.setResourceType(resourceType);
        if (speedOverloadNum > 0) {
            record.setSpeedOverloadNum(speedOverloadNum);
        }
        if (speedOverLoadPeroid > 0) {
            record.setSpeedOverLoadPeroid(speedOverLoadPeroid);
        }
        if (countOverLoadNum > 0) {
            record.setCountOverLoadNum(countOverLoadNum);
        }
        uploadBigDataLog(record);
        if (Utils.DEBUG) {
            Log.d(TAG, "recordResourceOverloadStatus!");
        }
    }

    public void notifyResourceStatus(Message msg) {
        int resourceType = msg.arg1;
        int resourceStatus = msg.arg2;
        SomeArgs args = msg.obj;
        String resourceName = args.arg1;
        Bundle bd = args.arg2;
        args.recycle();
        if (MultiTaskManager.getInstance() != null) {
            MultiTaskManager.getInstance().notifyResourceStatusOverload(resourceType, resourceName, resourceStatus, bd);
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
            this.mResourceStatusMap.remove(Long.valueOf(ResourceUtils.getResourceId(uid, pid, 16)));
            this.mResourceStatusMap.remove(Long.valueOf(ResourceUtils.getResourceId(uid, pid, 35)));
            this.mResourceStatusMap.remove(Long.valueOf(ResourceUtils.getResourceId(uid, pid, 36)));
            for (int i = 12; i < 15; i += IS_ACTION) {
                this.mResourceStatusMap.remove(Long.valueOf(ResourceUtils.getResourceId(uid, pid, i)));
            }
        }
        if (Utils.DEBUG) {
            Log.d(TAG, "cleanResourceRecordInAppDied!");
        }
    }

    public void uploadBigDataLog(ResourceOverloadRecord record) {
        String arg1 = ResourceUtils.composeName(record.getPackageName(), record.getResourceType());
        if (record.getResourceType() == 37 && record.getSpeedOverLoadPeroid() == IS_ACTION) {
            arg1 = arg1 + "_ACTION";
        }
        int arg2 = record.getSpeedOverloadNum() + record.getCountOverLoadNum();
        String resourceLog = "#Uid:" + record.getUid() + "#Pid:" + record.getPid() + "#Pkg:" + record.getPackageName() + "#ResourceType:" + record.getResourceType() + "#SpeedOverloadNum:" + record.getSpeedOverloadNum() + "#SpeedOverLoadPeroid:" + record.getSpeedOverLoadPeroid() + "#CountOverLoadNum:" + record.getCountOverLoadNum();
        if (Utils.DEBUG || Utils.HWFLOW) {
            Log.w(TAG, "uploadBigDataLog: arg1:" + arg1 + " arg2:" + arg2);
        }
        this.mJankLogProxy.jlog_d(arg1, arg2, resourceLog);
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
