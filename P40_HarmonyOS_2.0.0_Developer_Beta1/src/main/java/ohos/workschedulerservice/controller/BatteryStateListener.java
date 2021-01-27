package ohos.workschedulerservice.controller;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import ohos.aafwk.content.Intent;
import ohos.batterymanager.BatteryInfo;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.event.commonevent.MatchingSkills;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.workschedulerservice.WorkQueueManager;

public final class BatteryStateListener extends StateListener {
    private static final int BATTERY_INFO_DEFAULT_VALUE = -1;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, TAG);
    private static final String TAG = "BatteryStateListener";
    private BatteryStateEventSubscriber batteryEventSubscriber;
    private int batteryLevel;
    private int batteryStatus;
    private int chargerType;
    private boolean isCharging;
    private boolean isPowerConnect;
    private boolean listenerEnable;
    private final Object lock = new Object();
    private final ArrayList<WorkStatus> trackedTasks = new ArrayList<>();

    public BatteryStateListener(WorkQueueManager workQueueManager) {
        super(workQueueManager);
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x004c  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x004f  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0079  */
    public void init() {
        BatteryInfo.BatteryChargeState chargingStatus;
        if (this.batteryEventSubscriber != null || this.workQueueMgr == null) {
            HiLog.error(LOG_LABEL, "batteryStatus has been init", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            BatteryInfo batteryInfo = new BatteryInfo();
            BatteryInfo.BatteryLevel batteryLevel2 = batteryInfo.getBatteryLevel();
            if (batteryLevel2 != BatteryInfo.BatteryLevel.HIGH) {
                if (batteryLevel2 != BatteryInfo.BatteryLevel.NORMAL) {
                    this.batteryStatus = 1;
                    HiLog.debug(LOG_LABEL, "batteryStatus %d:%s", Integer.valueOf(this.batteryStatus), batteryLevel2.name());
                    BatteryInfo.BatteryPluggedType pluggedType = batteryInfo.getPluggedType();
                    this.chargerType = convertFromBatteryPluggedType(pluggedType);
                    if (this.chargerType == 0) {
                        this.isPowerConnect = true;
                    } else {
                        this.isPowerConnect = false;
                    }
                    HiLog.debug(LOG_LABEL, "chargerType %d:%s, isPowerConnect %s", Integer.valueOf(this.chargerType), pluggedType.name(), Boolean.valueOf(this.isPowerConnect));
                    chargingStatus = batteryInfo.getChargingStatus();
                    if (chargingStatus != BatteryInfo.BatteryChargeState.ENABLE) {
                        if (chargingStatus != BatteryInfo.BatteryChargeState.FULL) {
                            this.isCharging = false;
                            this.batteryLevel = batteryInfo.getCapacity();
                            HiLog.debug(LOG_LABEL, "isCharging %s:%s, batteryLevel %d", Boolean.valueOf(this.isCharging), chargingStatus.name(), Integer.valueOf(this.batteryLevel));
                            this.listenerEnable = enableBatteryListener();
                        }
                    }
                    this.isCharging = true;
                    this.batteryLevel = batteryInfo.getCapacity();
                    HiLog.debug(LOG_LABEL, "isCharging %s:%s, batteryLevel %d", Boolean.valueOf(this.isCharging), chargingStatus.name(), Integer.valueOf(this.batteryLevel));
                    this.listenerEnable = enableBatteryListener();
                }
            }
            this.batteryStatus = 2;
            HiLog.debug(LOG_LABEL, "batteryStatus %d:%s", Integer.valueOf(this.batteryStatus), batteryLevel2.name());
            BatteryInfo.BatteryPluggedType pluggedType2 = batteryInfo.getPluggedType();
            this.chargerType = convertFromBatteryPluggedType(pluggedType2);
            if (this.chargerType == 0) {
            }
            HiLog.debug(LOG_LABEL, "chargerType %d:%s, isPowerConnect %s", Integer.valueOf(this.chargerType), pluggedType2.name(), Boolean.valueOf(this.isPowerConnect));
            chargingStatus = batteryInfo.getChargingStatus();
            if (chargingStatus != BatteryInfo.BatteryChargeState.ENABLE) {
            }
            this.isCharging = true;
            this.batteryLevel = batteryInfo.getCapacity();
            HiLog.debug(LOG_LABEL, "isCharging %s:%s, batteryLevel %d", Boolean.valueOf(this.isCharging), chargingStatus.name(), Integer.valueOf(this.batteryLevel));
            this.listenerEnable = enableBatteryListener();
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void dumpStateListenerStatus(PrintWriter printWriter, String str) {
        if (printWriter == null || str == null) {
            HiLog.error(LOG_LABEL, "error dump PrintWriter or prefix input", new Object[0]);
            return;
        }
        printWriter.println();
        printWriter.println("BatteryStateListener:");
        synchronized (this.lock) {
            printWriter.println(str + "isCharging:" + this.isCharging);
            printWriter.println(str + "isPowerConnect:" + this.isPowerConnect);
            printWriter.println(str + "chargerType:" + this.chargerType);
            printWriter.println(str + "batteryLevel:" + this.batteryLevel);
            printWriter.println(str + "batteryStatus:" + this.batteryStatus);
            printWriter.println(str + "listenerEnable:" + this.listenerEnable);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.workschedulerservice.controller.BatteryStateListener$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$batterymanager$BatteryInfo$BatteryPluggedType = new int[BatteryInfo.BatteryPluggedType.values().length];

        static {
            try {
                $SwitchMap$ohos$batterymanager$BatteryInfo$BatteryPluggedType[BatteryInfo.BatteryPluggedType.AC.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$batterymanager$BatteryInfo$BatteryPluggedType[BatteryInfo.BatteryPluggedType.USB.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$batterymanager$BatteryInfo$BatteryPluggedType[BatteryInfo.BatteryPluggedType.WIRELESS.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private int convertFromBatteryPluggedType(BatteryInfo.BatteryPluggedType batteryPluggedType) {
        int i = AnonymousClass1.$SwitchMap$ohos$batterymanager$BatteryInfo$BatteryPluggedType[batteryPluggedType.ordinal()];
        int i2 = 2;
        if (i != 1) {
            i2 = i != 2 ? i != 3 ? 0 : 8 : 4;
        }
        return i2 != 0 ? i2 | 1 : i2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int convertFromCommonEventChargeType(int i) {
        int i2;
        if (i == PluggedTypeFromCommonEvent.WIRELESS.getValue()) {
            i2 = 8;
        } else {
            i2 = i == PluggedTypeFromCommonEvent.NONE.getValue() ? 0 : 1 << i;
        }
        return i2 != 0 ? i2 | 1 : i2;
    }

    /* access modifiers changed from: private */
    public enum PluggedTypeFromCommonEvent {
        NONE(0),
        AC(1),
        USB(2),
        WIRELESS(4);
        
        private final int value;

        private PluggedTypeFromCommonEvent(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    private boolean enableBatteryListener() {
        MatchingSkills matchingSkills = new MatchingSkills();
        matchingSkills.addEvent("usual.event.BATTERY_CHANGED");
        matchingSkills.addEvent("usual.event.BATTERY_LOW");
        matchingSkills.addEvent("usual.event.BATTERY_OKAY");
        matchingSkills.addEvent("usual.event.POWER_CONNECTED");
        matchingSkills.addEvent("usual.event.POWER_DISCONNECTED");
        matchingSkills.addEvent("usual.event.DISCHARGING");
        matchingSkills.addEvent("usual.event.CHARGING");
        CommonEventSubscribeInfo commonEventSubscribeInfo = new CommonEventSubscribeInfo(matchingSkills);
        commonEventSubscribeInfo.setUserId(-1);
        this.batteryEventSubscriber = new BatteryStateEventSubscriber(commonEventSubscribeInfo);
        try {
            CommonEventManager.subscribeCommonEvent(this.batteryEventSubscriber);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "subscribeCommonEvent occur exception.", new Object[0]);
            this.batteryEventSubscriber = null;
            return false;
        }
    }

    private boolean disableBatteryListener() {
        BatteryStateEventSubscriber batteryStateEventSubscriber = this.batteryEventSubscriber;
        if (batteryStateEventSubscriber == null) {
            return true;
        }
        try {
            CommonEventManager.unsubscribeCommonEvent(batteryStateEventSubscriber);
            return true;
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "unsubscribeCommonEvent occur exception", new Object[0]);
            return false;
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStartSignWork(WorkStatus workStatus) {
        synchronized (this.lock) {
            if (workStatus.hasBatteryCondition() || workStatus.hasChargeCondition()) {
                this.trackedTasks.add(workStatus);
                workStatus.changeChargingSatisfiedCondition(isGoodPower(), this.chargerType);
                workStatus.changeBatteryLevelSatisfiedCondition(this.batteryLevel);
                workStatus.changeBatteryStatusSatisfiedCondition(this.batteryStatus);
                updateListenerStatus();
            }
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStopSignWork(WorkStatus workStatus) {
        synchronized (this.lock) {
            this.trackedTasks.remove(workStatus);
            updateListenerStatus();
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void updateTrackedTasks(WorkStatus workStatus) {
        synchronized (this.lock) {
            updateTasks(this.trackedTasks, workStatus);
        }
    }

    private void updateListenerStatus() {
        if (this.trackedTasks.isEmpty()) {
            if (this.listenerEnable) {
                this.listenerEnable = !disableBatteryListener();
            }
        } else if (!this.listenerEnable) {
            this.listenerEnable = enableBatteryListener();
        }
    }

    public final class BatteryStateEventSubscriber extends CommonEventSubscriber {
        BatteryStateEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        public void onReceiveEvent(CommonEventData commonEventData) {
            synchronized (BatteryStateListener.this.lock) {
                if (commonEventData != null) {
                    Intent intent = commonEventData.getIntent();
                    if (intent != null) {
                        String action = intent.getAction();
                        if ("usual.event.BATTERY_CHANGED".equals(action)) {
                            int intParam = intent.getIntParam("chargeType", -1);
                            if (intParam != -1) {
                                BatteryStateListener.this.chargerType = BatteryStateListener.this.convertFromCommonEventChargeType(intParam);
                            }
                            int intParam2 = intent.getIntParam("batteryCapacity", -1);
                            if (intParam2 != -1) {
                                BatteryStateListener.this.batteryLevel = intParam2;
                            }
                            BatteryStateListener.this.updateStateFromCommonEvent(intent);
                        } else if ("usual.event.BATTERY_LOW".equals(action)) {
                            BatteryStateListener.this.batteryStatus = 1;
                        } else if ("usual.event.BATTERY_OKAY".equals(action)) {
                            BatteryStateListener.this.batteryStatus = 2;
                        } else if ("usual.event.POWER_CONNECTED".equals(action)) {
                            BatteryStateListener.this.isPowerConnect = true;
                        } else if ("usual.event.POWER_DISCONNECTED".equals(action)) {
                            BatteryStateListener.this.isPowerConnect = false;
                        } else if ("usual.event.DISCHARGING".equals(action)) {
                            BatteryStateListener.this.isCharging = false;
                        } else if ("usual.event.CHARGING".equals(action)) {
                            BatteryStateListener.this.isCharging = true;
                        } else {
                            HiLog.error(BatteryStateListener.LOG_LABEL, "Action %s is not used but received ", action);
                        }
                        HiLog.info(BatteryStateListener.LOG_LABEL, "Action %{public}s Battery status:%{public}s,type:%{public}d,isCharging:%{public}s level:%{public}d,isPowerConnect %{public}s", action, Integer.valueOf(BatteryStateListener.this.batteryStatus), Integer.valueOf(BatteryStateListener.this.chargerType), Boolean.valueOf(BatteryStateListener.this.isCharging), Integer.valueOf(BatteryStateListener.this.batteryLevel), Boolean.valueOf(BatteryStateListener.this.isPowerConnect));
                        BatteryStateListener.this.updateTrackedWorks();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStateFromCommonEvent(Intent intent) {
        if (intent.getBooleanParam("batteryLow", false)) {
            this.batteryStatus = 1;
        } else {
            this.batteryStatus = 2;
        }
        int intParam = intent.getIntParam("chargeState", -1);
        if (intParam == BatteryInfo.BatteryChargeState.ENABLE.ordinal() || intParam == BatteryInfo.BatteryChargeState.FULL.ordinal()) {
            this.isCharging = true;
        } else {
            this.isCharging = false;
        }
    }

    private boolean isGoodPower() {
        if (this.batteryStatus == 2) {
            return this.isCharging;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTrackedWorks() {
        boolean z = false;
        HiLog.info(LOG_LABEL, "updateTrackedWorks", new Object[0]);
        int i = this.chargerType;
        if (this.isPowerConnect) {
            i |= 1;
        }
        Iterator<WorkStatus> it = this.trackedTasks.iterator();
        while (it.hasNext()) {
            WorkStatus next = it.next();
            z = z | next.changeChargingSatisfiedCondition(isGoodPower(), i) | next.changeBatteryLevelSatisfiedCondition(this.batteryLevel) | next.changeBatteryStatusSatisfiedCondition(this.batteryStatus);
            if (z) {
                this.workQueueMgr.onDeviceStateChanged(next, 5);
            }
        }
    }
}
