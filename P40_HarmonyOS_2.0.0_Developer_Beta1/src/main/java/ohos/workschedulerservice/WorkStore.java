package ohos.workschedulerservice;

import android.content.Context;
import android.content.SharedPreferences;
import com.huawei.ohos.workscheduleradapter.WorkschedulerAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.workscheduler.WorkInfo;
import ohos.workschedulerservice.controller.WorkStatus;

public final class WorkStore {
    private static final int ABILITY_NAME_INDEX = 2;
    private static final int BATTERY_LEVEL_INDEX = 4;
    private static final int BATTERY_STATUS_INDEX = 5;
    private static final int BUNDLE_NAME_INDEX = 1;
    private static final int CHARGE_TYPE_INDEX = 3;
    private static final int CONDITION_CHARGE = 4;
    private static final int CONDITION_DEVICE_IDLE = 2;
    private static final int CONDITION_PERSIST = 32;
    private static final int CONDITION_REPEAT = 8;
    private static final int CONDITION_STORAGE = 64;
    private static final int CYCLETIME_INDEX = 8;
    private static final String FILE_NAME = "persisted_work";
    private static final int IDLE_WAIT_INDEX = 2;
    private static final int KEY_SIZE = 5;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, TAG);
    private static final int NETWORK_INDEX = 1;
    private static final int REPEAT_COUNTER_INDEX = 6;
    private static final int STATUE_INDEX = 0;
    private static final int STORAGE_INDEX = 7;
    private static final String TAG = "WorkStore";
    private static final int UID_INDEX = 3;
    private static final int USERID_INDEX = 4;
    private static final int VALUE_SIZE = 9;
    private static final int WORKID_INDEX = 0;
    private SharedPreferences preferences;
    private Context workContext;

    public WorkStore(Context context) {
        if (context == null) {
            HiLog.warn(LOG_LABEL, "context is null.", new Object[0]);
            return;
        }
        this.workContext = context;
        this.preferences = this.workContext.createDeviceProtectedStorageContext().getSharedPreferences(FILE_NAME, 32768);
    }

    public void readWorkStatusAsync(WorkQueueManager workQueueManager) {
        if (workQueueManager != null) {
            List<WorkStatus> readWorkStatus = readWorkStatus();
            HiLog.info(LOG_LABEL, "Read work start.", new Object[0]);
            for (WorkStatus workStatus : readWorkStatus) {
                workQueueManager.tryStartSignWork(workStatus.getWork(), workStatus.getUid(), workStatus.getUserId());
            }
        }
    }

    public void writeWorkStatusAsync(WorkStatus workStatus) {
        if (workStatus != null && workStatus.hasPersistCondition()) {
            HiLog.info(LOG_LABEL, "write work.", new Object[0]);
            writeWorkStatus(workStatus);
        }
    }

    public void removeWorkStatusAsync(WorkStatus workStatus) {
        if (workStatus != null && workStatus.hasPersistCondition()) {
            removeWorkStatus(workStatus);
        }
    }

    public void clearWorkStatusAsync() {
        clearWorkStatus();
    }

    private void writeWorkStatus(WorkStatus workStatus) {
        SharedPreferences sharedPreferences = this.preferences;
        if (sharedPreferences != null) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            HiLog.debug(LOG_LABEL, "writeWorkStatus workid : %{public}d", Integer.valueOf(workStatus.getWorkId()));
            HiLog.debug(LOG_LABEL, "commit ret %{public}s", Boolean.valueOf(edit.putString(generateKey(workStatus), generateValue(workStatus)).commit()));
        }
    }

    private void removeWorkStatus(WorkStatus workStatus) {
        SharedPreferences sharedPreferences = this.preferences;
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(generateKey(workStatus)).commit();
        }
    }

    private void clearWorkStatus() {
        SharedPreferences sharedPreferences = this.preferences;
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().commit();
        }
    }

    private List<WorkStatus> readWorkStatus() {
        SharedPreferences sharedPreferences = this.preferences;
        if (sharedPreferences == null || this.workContext == null) {
            HiLog.warn(LOG_LABEL, "readWorkStatus failed, maybe context is null.", new Object[0]);
            return new ArrayList();
        }
        Map<String, ?> all = sharedPreferences.getAll();
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            BaseWorkInfo buildBaseWorkInfo = buildBaseWorkInfo(entry.getKey());
            if (buildBaseWorkInfo == null) {
                HiLog.debug(LOG_LABEL, "readWorkStatus BaseWorkInfo invalid, continue.", new Object[0]);
            } else if (!(entry.getValue() instanceof String)) {
                HiLog.debug(LOG_LABEL, "readWorkStatus map value invalid, continue.", new Object[0]);
            } else {
                WorkInfo buildWorkInfo = buildWorkInfo((String) entry.getValue(), buildBaseWorkInfo);
                if (buildWorkInfo == null) {
                    HiLog.debug(LOG_LABEL, "readWorkStatus workInfo invalid, continue.", new Object[0]);
                } else {
                    WorkStatus generateWorkStatus = WorkStatus.generateWorkStatus(buildWorkInfo, buildBaseWorkInfo.getUid(), buildBaseWorkInfo.getUserId());
                    generateWorkStatus.updateActiveLevel(WorkschedulerAdapter.getStandbyLevel(this.workContext, generateWorkStatus.getBundleName()));
                    arrayList.add(generateWorkStatus);
                }
            }
        }
        return arrayList;
    }

    private BaseWorkInfo buildBaseWorkInfo(String str) {
        String[] split = str.split("#");
        if (split.length < 5) {
            return null;
        }
        int parseInt = Integer.parseInt(split[0]);
        String str2 = split[1];
        String str3 = split[2];
        int parseInt2 = Integer.parseInt(split[3]);
        int parseInt3 = Integer.parseInt(split[4]);
        if (str2 != null && str3 != null && !str2.isEmpty() && !str3.isEmpty()) {
            return new BaseWorkInfo(new WorkInfo.Builder().setAbilityInfo(parseInt, str2, str3), parseInt2, parseInt3);
        }
        HiLog.error(LOG_LABEL, "buildBaseWorkInfo bundleName or abilityName invalid.", new Object[0]);
        return null;
    }

    private WorkInfo buildWorkInfo(String str, BaseWorkInfo baseWorkInfo) {
        String[] split = str.split("#");
        if (split.length < 9) {
            HiLog.error(LOG_LABEL, "buildWorkInfo values invalid.", new Object[0]);
            return null;
        }
        WorkInfo.Builder builder = baseWorkInfo.getBuilder();
        WorkCondition resolveWorkCondition = resolveWorkCondition(Integer.parseInt(split[0]));
        requestNetwork(builder, Integer.parseInt(split[1]));
        requestDeviceIdleType(builder, resolveWorkCondition.isIdle(), Integer.parseInt(split[2]));
        requestChargingType(builder, resolveWorkCondition.isCharging(), Integer.parseInt(split[3]));
        requestBatteryStatus(builder, Integer.parseInt(split[5]));
        requestStorageStatus(builder, Integer.parseInt(split[7]));
        builder.requestPersisted(resolveWorkCondition.isPersisted());
        requestRepeatCycle(builder, Long.parseLong(split[8]), Integer.parseInt(split[6]));
        return builder.build();
    }

    private void requestDeviceIdleType(WorkInfo.Builder builder, boolean z, int i) {
        if (i >= 60000 && i <= 1200000) {
            builder.requestDeviceIdleType(z, i);
        }
    }

    private void requestBatteryStatus(WorkInfo.Builder builder, int i) {
        if (i > 0 && i <= 2) {
            builder.requestBatteryStatus(i);
        }
    }

    private void requestStorageStatus(WorkInfo.Builder builder, int i) {
        if (i > 0 && i <= 2) {
            builder.requestStorageStatus(i);
        }
    }

    private void requestRepeatCycle(WorkInfo.Builder builder, long j, int i) {
        if (j >= 1200000 && i >= 1) {
            builder.requestRepeatCycle(j, i);
        }
    }

    private void requestNetwork(WorkInfo.Builder builder, int i) {
        if (i >= 0) {
            int i2 = 0;
            while (i != 0) {
                if ((i & 1) != 0) {
                    if (i2 <= 5) {
                        builder.requestNetworkType(i2);
                    } else {
                        return;
                    }
                }
                i >>= 1;
                i2++;
            }
        }
    }

    private void requestChargingType(WorkInfo.Builder builder, boolean z, int i) {
        if (i >= 0) {
            int i2 = 0;
            while (i != 0) {
                if ((i & 1) != 0) {
                    if (i2 <= 3) {
                        builder.requestChargingType(z, i2);
                    } else {
                        return;
                    }
                }
                i >>= 1;
                i2++;
            }
        }
    }

    private WorkCondition resolveWorkCondition(int i) {
        WorkCondition workCondition = new WorkCondition();
        if ((i & 2) != 0) {
            workCondition.setIdle(true);
        }
        if ((i & 4) != 0) {
            workCondition.setCharging(true);
        }
        if ((i & 8) != 0) {
            workCondition.setRepeated(true);
        }
        if ((i & 32) != 0) {
            workCondition.setPersisted(true);
        }
        return workCondition;
    }

    private String generateKey(WorkStatus workStatus) {
        return workStatus.getWorkId() + '#' + workStatus.getWork().getBundleName() + '#' + workStatus.getWork().getAbilityName() + '#' + workStatus.getUid() + '#' + workStatus.getUserId();
    }

    /* JADX DEBUG: TODO: convert one arg to string using `String.valueOf()`, args: [(wrap: int : 0x0005: INVOKE  (r0v0 int) = (r3v0 ohos.workschedulerservice.controller.WorkStatus) type: VIRTUAL call: ohos.workschedulerservice.controller.WorkStatus.getRequestStatus():int), ('#' char), (wrap: int : 0x0011: INVOKE  (r1v0 int) = (r3v0 ohos.workschedulerservice.controller.WorkStatus) type: VIRTUAL call: ohos.workschedulerservice.controller.WorkStatus.getNetworkType():int), ('#' char), (wrap: int : 0x001f: INVOKE  (r1v2 int) = 
      (wrap: ohos.workscheduler.WorkInfo : 0x001b: INVOKE  (r1v1 ohos.workscheduler.WorkInfo) = (r3v0 ohos.workschedulerservice.controller.WorkStatus) type: VIRTUAL call: ohos.workschedulerservice.controller.WorkStatus.getWork():ohos.workscheduler.WorkInfo)
     type: VIRTUAL call: ohos.workscheduler.WorkInfo.getRequestIdleWaitTime():int), ('#' char), (wrap: int : 0x002d: INVOKE  (r1v4 int) = 
      (wrap: ohos.workscheduler.WorkInfo : 0x0029: INVOKE  (r1v3 ohos.workscheduler.WorkInfo) = (r3v0 ohos.workschedulerservice.controller.WorkStatus) type: VIRTUAL call: ohos.workschedulerservice.controller.WorkStatus.getWork():ohos.workscheduler.WorkInfo)
     type: VIRTUAL call: ohos.workscheduler.WorkInfo.getRequestChargeType():int), ('#' char), (wrap: int : 0x003b: INVOKE  (r1v6 int) = 
      (wrap: ohos.workscheduler.WorkInfo : 0x0037: INVOKE  (r1v5 ohos.workscheduler.WorkInfo) = (r3v0 ohos.workschedulerservice.controller.WorkStatus) type: VIRTUAL call: ohos.workschedulerservice.controller.WorkStatus.getWork():ohos.workscheduler.WorkInfo)
     type: VIRTUAL call: ohos.workscheduler.WorkInfo.getRequestBatteryLevel():int), ('#' char), (wrap: int : 0x0049: INVOKE  (r1v8 int) = 
      (wrap: ohos.workscheduler.WorkInfo : 0x0045: INVOKE  (r1v7 ohos.workscheduler.WorkInfo) = (r3v0 ohos.workschedulerservice.controller.WorkStatus) type: VIRTUAL call: ohos.workschedulerservice.controller.WorkStatus.getWork():ohos.workscheduler.WorkInfo)
     type: VIRTUAL call: ohos.workscheduler.WorkInfo.getRequestBatteryStatus():int), ('#' char), (wrap: int : 0x0057: INVOKE  (r1v10 int) = 
      (wrap: ohos.workscheduler.WorkInfo : 0x0053: INVOKE  (r1v9 ohos.workscheduler.WorkInfo) = (r3v0 ohos.workschedulerservice.controller.WorkStatus) type: VIRTUAL call: ohos.workschedulerservice.controller.WorkStatus.getWork():ohos.workscheduler.WorkInfo)
     type: VIRTUAL call: ohos.workscheduler.WorkInfo.getRepeatCounter():int), ('#' char), (wrap: int : 0x0065: INVOKE  (r1v12 int) = 
      (wrap: ohos.workscheduler.WorkInfo : 0x0061: INVOKE  (r1v11 ohos.workscheduler.WorkInfo) = (r3v0 ohos.workschedulerservice.controller.WorkStatus) type: VIRTUAL call: ohos.workschedulerservice.controller.WorkStatus.getWork():ohos.workscheduler.WorkInfo)
     type: VIRTUAL call: ohos.workscheduler.WorkInfo.getRequestStorageType():int), ('#' char), (wrap: long : 0x0073: INVOKE  (r0v2 long) = 
      (wrap: ohos.workscheduler.WorkInfo : 0x006f: INVOKE  (r3v1 ohos.workscheduler.WorkInfo) = (r3v0 ohos.workschedulerservice.controller.WorkStatus) type: VIRTUAL call: ohos.workschedulerservice.controller.WorkStatus.getWork():ohos.workscheduler.WorkInfo)
     type: VIRTUAL call: ohos.workscheduler.WorkInfo.getRepeatCycleTime():long)] */
    private String generateValue(WorkStatus workStatus) {
        StringBuilder sb = new StringBuilder();
        sb.append(workStatus.getRequestStatus());
        sb.append('#');
        sb.append(workStatus.getNetworkType());
        sb.append('#');
        sb.append(workStatus.getWork().getRequestIdleWaitTime());
        sb.append('#');
        sb.append(workStatus.getWork().getRequestChargeType());
        sb.append('#');
        sb.append(workStatus.getWork().getRequestBatteryLevel());
        sb.append('#');
        sb.append(workStatus.getWork().getRequestBatteryStatus());
        sb.append('#');
        sb.append(workStatus.getWork().getRepeatCounter());
        sb.append('#');
        sb.append(workStatus.getWork().getRequestStorageType());
        sb.append('#');
        sb.append(workStatus.getWork().getRepeatCycleTime());
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public static final class BaseWorkInfo {
        private final WorkInfo.Builder builder;
        private final int uid;
        private final int userId;

        public BaseWorkInfo(WorkInfo.Builder builder2, int i, int i2) {
            this.builder = builder2;
            this.uid = i;
            this.userId = i2;
        }

        public WorkInfo.Builder getBuilder() {
            return this.builder;
        }

        public int getUid() {
            return this.uid;
        }

        public int getUserId() {
            return this.userId;
        }
    }

    /* access modifiers changed from: private */
    public static final class WorkCondition {
        private boolean requestCharging = false;
        private boolean requestIdle = false;
        private boolean requestPersisted = false;
        private boolean requestRepeated = false;

        public boolean isIdle() {
            return this.requestIdle;
        }

        public boolean isCharging() {
            return this.requestCharging;
        }

        public boolean isRepeated() {
            return this.requestRepeated;
        }

        public boolean isPersisted() {
            return this.requestPersisted;
        }

        public void setIdle(boolean z) {
            this.requestIdle = z;
        }

        public void setCharging(boolean z) {
            this.requestCharging = z;
        }

        public void setRepeated(boolean z) {
            this.requestRepeated = z;
        }

        public void setPersisted(boolean z) {
            this.requestPersisted = z;
        }
    }
}
