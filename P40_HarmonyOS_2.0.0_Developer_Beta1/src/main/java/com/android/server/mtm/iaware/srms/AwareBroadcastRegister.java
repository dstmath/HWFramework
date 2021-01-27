package com.android.server.mtm.iaware.srms;

import android.app.mtm.iaware.appmng.AppMngConstant;
import android.rms.iaware.AwareLog;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.iaware.appmng.DecisionMaker;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AwareBroadcastRegister {
    private static final String ITEM_REPORT_THRESHOLD = "brreg_report_threshold";
    private static final Object LOCK = new Object();
    private static final String MISC_ASSEMBLE_CONDITION = "brreg_assemble_condition";
    private static final String MISC_BIGDATA_THRESHOLD = "bigdata_threshold";
    private static final String SPLIT_VALUE = ",";
    private static final String TAG = "AwareBroadcastRegister brreg";
    private static AwareBroadcastRegister sBroadcastRegister = null;
    private final HashMap<String, HashMap<String, String>> mBrAssembleConditons;
    private final HashMap<String, Integer> mBrCounts;
    private int mBrRegisterReportThreshold;
    private final Object mConfigLock;
    private HwActivityManagerService mHwAms;
    private final HashMap<String, HashMap<String, String>> mPkgBrsWithCondition;

    public static AwareBroadcastRegister getInstance() {
        AwareBroadcastRegister awareBroadcastRegister;
        synchronized (LOCK) {
            if (sBroadcastRegister == null) {
                sBroadcastRegister = new AwareBroadcastRegister();
                sBroadcastRegister.updateConfigData();
            }
            awareBroadcastRegister = sBroadcastRegister;
        }
        return awareBroadcastRegister;
    }

    private AwareBroadcastRegister() {
        this.mBrAssembleConditons = new HashMap<>();
        this.mPkgBrsWithCondition = new HashMap<>();
        this.mBrCounts = new HashMap<>();
        this.mConfigLock = new Object();
        this.mHwAms = null;
        this.mBrRegisterReportThreshold = 30;
        this.mHwAms = HwActivityManagerService.self();
    }

    private void updateBrAssembleConditionOne(String item) {
        if (item != null && !item.isEmpty()) {
            String[] configList = item.split(SPLIT_VALUE);
            if (configList.length < 2) {
                AwareLog.e(TAG, "invalid config: " + item);
                return;
            }
            String acId = configList[0].trim();
            String condition = configList[1].trim();
            String action = getActionFromAssembleCondition(condition);
            String pkg = getPackageNameFromId(acId);
            String receiver = getReceiverFromAcId(acId);
            if (receiver != null && !receiver.startsWith(".")) {
                receiver = "." + receiver;
            }
            if (action != null && pkg != null) {
                HashMap<String, String> receiverMap = this.mPkgBrsWithCondition.get(pkg);
                if (receiverMap == null) {
                    receiverMap = new HashMap<>();
                    this.mPkgBrsWithCondition.put(pkg, receiverMap);
                }
                receiverMap.put(receiver, acId);
                HashMap<String, String> innerMap = this.mBrAssembleConditons.get(acId);
                if (innerMap == null) {
                    innerMap = new HashMap<>();
                    this.mBrAssembleConditons.put(acId, innerMap);
                }
                innerMap.put(action, condition);
            }
        }
    }

    private void updateBrAssembleCondition() {
        ArrayList<String> brAssembleCondition = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), MISC_ASSEMBLE_CONDITION);
        if (brAssembleCondition != null) {
            int size = brAssembleCondition.size();
            for (int i = 0; i < size; i++) {
                updateBrAssembleConditionOne(brAssembleCondition.get(i));
            }
        }
    }

    private void updateConfigDataOne(String item) {
        if (item != null && !item.isEmpty()) {
            String[] configList = item.split(SPLIT_VALUE);
            if (configList.length < 2) {
                AwareLog.e(TAG, "invalid config: " + item);
                return;
            }
            try {
                if (configList[0].trim().equals(ITEM_REPORT_THRESHOLD)) {
                    this.mBrRegisterReportThreshold = Integer.parseInt(configList[1].trim());
                }
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "invalid config: " + item);
            }
        }
    }

    public void updateConfigData() {
        if (this.mHwAms == null) {
            AwareLog.e(TAG, "failed to get HwAMS");
            return;
        }
        DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.BROADCAST, this.mHwAms.getUiContext());
        synchronized (this.mConfigLock) {
            ArrayList<String> defaultThresholdList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), MISC_BIGDATA_THRESHOLD);
            if (defaultThresholdList != null) {
                int size = defaultThresholdList.size();
                for (int i = 0; i < size; i++) {
                    updateConfigDataOne(defaultThresholdList.get(i));
                }
            }
            this.mBrAssembleConditons.clear();
            this.mPkgBrsWithCondition.clear();
            updateBrAssembleCondition();
        }
    }

    public int getBrRegisterReportThreshold() {
        int i;
        synchronized (this.mConfigLock) {
            i = this.mBrRegisterReportThreshold;
        }
        return i;
    }

    public HashMap<String, Integer> getBrCounts() {
        HashMap<String, Integer> brCounts;
        synchronized (this.mBrCounts) {
            brCounts = new HashMap<>(this.mBrCounts);
        }
        return brCounts;
    }

    private void countReceiverUnRegister(String brId) {
        Integer count;
        if (brId != null && !brId.isEmpty() && (count = this.mBrCounts.get(brId)) != null) {
            Integer newCount = Integer.valueOf(count.intValue() - 1);
            if (newCount.intValue() == 0) {
                this.mBrCounts.remove(brId);
            } else {
                this.mBrCounts.put(brId, newCount);
            }
        }
    }

    private void countReceiverRegister(String brId) {
        if (brId != null && !brId.isEmpty()) {
            Integer count = this.mBrCounts.get(brId);
            if (count == null) {
                this.mBrCounts.put(brId, 1);
            } else {
                this.mBrCounts.put(brId, Integer.valueOf(count.intValue() + 1));
            }
        }
    }

    public int countReceiver(boolean isRegister, String brId) {
        int intValue;
        synchronized (this.mBrCounts) {
            if (!isRegister) {
                countReceiverUnRegister(brId);
            } else {
                countReceiverRegister(brId);
            }
            intValue = this.mBrCounts.get(brId) == null ? 0 : this.mBrCounts.get(brId).intValue();
        }
        return intValue;
    }

    public static String removeBrIdUncommonData(String brId) {
        int objectAddressPositionEnd;
        int pidPositionEnd;
        if (brId == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(brId);
        int pidPositionStart = sb.indexOf("+");
        if (!(pidPositionStart == -1 || (pidPositionEnd = sb.indexOf("+", pidPositionStart + 1)) == -1)) {
            sb.delete(pidPositionStart, pidPositionEnd);
        }
        int objectAddressPositionStart = sb.indexOf("@");
        if (!(objectAddressPositionStart == -1 || (objectAddressPositionEnd = sb.indexOf("+", objectAddressPositionStart)) == -1)) {
            sb.delete(objectAddressPositionStart, objectAddressPositionEnd);
        }
        return sb.toString();
    }

    private String getMatchedAssembleConditionId(HashMap<String, String> receiverMap, String brId) {
        for (Map.Entry<String, String> entry : receiverMap.entrySet()) {
            String receiver = entry.getKey();
            if (!(receiver == null || receiver.isEmpty() || !brId.contains(receiver))) {
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.i(TAG, "Hit !  brId:" + brId + " match  acId: " + entry.getValue());
                }
                return entry.getValue();
            }
        }
        return null;
    }

    public String findMatchedAssembleConditionId(String brId) {
        String pkg;
        String conditionId;
        if (brId == null || (pkg = getPackageNameFromId(brId)) == null || pkg.isEmpty()) {
            return null;
        }
        synchronized (this.mConfigLock) {
            HashMap<String, String> receiverMap = this.mPkgBrsWithCondition.get(pkg);
            if (receiverMap == null || (conditionId = getMatchedAssembleConditionId(receiverMap, brId)) == null) {
                return null;
            }
            return conditionId;
        }
    }

    public String getBrAssembleCondition(String acId, String action) {
        if (acId == null || action == null) {
            return null;
        }
        synchronized (this.mConfigLock) {
            HashMap<String, String> conditions = this.mBrAssembleConditons.get(acId);
            if (conditions == null) {
                return null;
            }
            return conditions.get(action);
        }
    }

    private static String getPackageNameFromId(String brId) {
        int pkgPositionEnd;
        if (brId == null || (pkgPositionEnd = brId.indexOf("+")) == -1) {
            return null;
        }
        return brId.substring(0, pkgPositionEnd);
    }

    private static String getReceiverFromAcId(String acId) {
        int receiverPositionStart;
        if (acId == null || (receiverPositionStart = acId.lastIndexOf("+")) == -1) {
            return null;
        }
        try {
            return acId.substring(receiverPositionStart + 1, acId.length());
        } catch (IndexOutOfBoundsException e) {
            AwareLog.e(TAG, "acId process error");
            return null;
        }
    }

    private static String getActionFromAssembleCondition(String condition) {
        int actionEndPosition;
        if (condition == null || (actionEndPosition = condition.indexOf("@")) == -1) {
            return null;
        }
        return condition.substring(0, actionEndPosition);
    }

    public void dumpBrRegConfig(PrintWriter pw) {
        updateConfigData();
        synchronized (this.mConfigLock) {
            pw.println("brreg_report_threshold: " + this.mBrRegisterReportThreshold);
            pw.println("Assemble conditions:");
            for (Map.Entry<String, HashMap<String, String>> entry : this.mBrAssembleConditons.entrySet()) {
                if (entry.getValue() != null) {
                    Iterator<Map.Entry<String, String>> it = entry.getValue().entrySet().iterator();
                    while (it.hasNext()) {
                        pw.println(entry.getKey() + ", " + it.next().getValue());
                    }
                }
            }
        }
    }

    public void dumpAwareBrRegInfo(PrintWriter pw) {
        synchronized (this.mBrCounts) {
            pw.println("BR register ID and count:" + this.mBrCounts.size());
            for (Map.Entry<String, Integer> entry : this.mBrCounts.entrySet()) {
                pw.println("" + entry.getKey() + SPLIT_VALUE + entry.getValue());
            }
        }
    }
}
