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
    private static final String MISC_ASSEMBLE_CONDITION = "brreg_assemble_condition";
    private static final String MISC_BIGDATA_THRESHOLD = "bigdata_threshold";
    private static final String SPLIT_VALUE = ",";
    private static final String TAG = "AwareBroadcastRegister brreg";
    private static AwareBroadcastRegister mBroadcastRegister = null;
    private final HashMap<String, HashMap<String, String>> mBRAssembleConditons;
    private final HashMap<String, Integer> mBRCounts;
    private int mBrRegisterReportThreshold;
    private final Object mConfigLock;
    private HwActivityManagerService mHwAMS;
    private final HashMap<String, HashMap<String, String>> mPkgBRsWithCondition;

    public static synchronized AwareBroadcastRegister getInstance() {
        AwareBroadcastRegister awareBroadcastRegister;
        synchronized (AwareBroadcastRegister.class) {
            if (mBroadcastRegister == null) {
                mBroadcastRegister = new AwareBroadcastRegister();
                mBroadcastRegister.updateConfigData();
            }
            awareBroadcastRegister = mBroadcastRegister;
        }
        return awareBroadcastRegister;
    }

    private AwareBroadcastRegister() {
        this.mBRCounts = new HashMap<>();
        this.mPkgBRsWithCondition = new HashMap<>();
        this.mBRAssembleConditons = new HashMap<>();
        this.mHwAMS = null;
        this.mConfigLock = new Object();
        this.mBrRegisterReportThreshold = 30;
        this.mHwAMS = HwActivityManagerService.self();
    }

    public void updateConfigData() {
        if (this.mHwAMS == null) {
            AwareLog.e(TAG, "failed to get HwAMS");
            return;
        }
        DecisionMaker.getInstance().updateRule(AppMngConstant.AppMngFeature.BROADCAST, this.mHwAMS.getUiContext());
        synchronized (this.mConfigLock) {
            ArrayList<String> defaultThresholdList = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), MISC_BIGDATA_THRESHOLD);
            char c = 0;
            int i = 2;
            char c2 = 1;
            if (defaultThresholdList != null) {
                int size = defaultThresholdList.size();
                for (int i2 = 0; i2 < size; i2++) {
                    String item = defaultThresholdList.get(i2);
                    if (item != null && !item.isEmpty()) {
                        String[] configList = item.split(",");
                        if (configList.length < 2) {
                            AwareLog.e(TAG, "invalid config: " + item);
                        } else {
                            try {
                                if (configList[0].trim().equals(ITEM_REPORT_THRESHOLD)) {
                                    this.mBrRegisterReportThreshold = Integer.parseInt(configList[1].trim());
                                }
                            } catch (NumberFormatException e) {
                                AwareLog.e(TAG, "invalid config: " + item);
                            }
                        }
                    }
                }
            }
            this.mBRAssembleConditons.clear();
            this.mPkgBRsWithCondition.clear();
            ArrayList<String> brAssembleCondition = DecisionMaker.getInstance().getRawConfig(AppMngConstant.AppMngFeature.BROADCAST.getDesc(), MISC_ASSEMBLE_CONDITION);
            if (brAssembleCondition != null) {
                int i3 = 0;
                int size2 = brAssembleCondition.size();
                while (i3 < size2) {
                    String item2 = brAssembleCondition.get(i3);
                    if (item2 != null && !item2.isEmpty()) {
                        String[] configList2 = item2.split(",");
                        if (configList2.length < i) {
                            AwareLog.e(TAG, "invalid config: " + item2);
                        } else {
                            String acId = configList2[c].trim();
                            String condition = configList2[c2].trim();
                            String action = getActionFromAssembleCondition(condition);
                            String pkg = getPackageNameFromId(acId);
                            String receiver = getReceiverFromAcID(acId);
                            if (receiver != null && !receiver.startsWith(".")) {
                                receiver = "." + receiver;
                            }
                            if (!(action == null || pkg == null)) {
                                HashMap<String, String> receiverMap = this.mPkgBRsWithCondition.get(pkg);
                                if (receiverMap == null) {
                                    receiverMap = new HashMap<>();
                                    this.mPkgBRsWithCondition.put(pkg, receiverMap);
                                }
                                receiverMap.put(receiver, acId);
                                HashMap<String, String> innerMap = this.mBRAssembleConditons.get(acId);
                                if (innerMap == null) {
                                    innerMap = new HashMap<>();
                                    this.mBRAssembleConditons.put(acId, innerMap);
                                }
                                innerMap.put(action, condition);
                            }
                        }
                    }
                    i3++;
                    c = 0;
                    i = 2;
                    c2 = 1;
                }
            }
        }
    }

    public int getBRRegisterReportThreshold() {
        int i;
        synchronized (this.mConfigLock) {
            i = this.mBrRegisterReportThreshold;
        }
        return i;
    }

    public HashMap<String, Integer> getBRCounts() {
        HashMap<String, Integer> hashMap;
        synchronized (this.mBRCounts) {
            hashMap = this.mBRCounts;
        }
        return hashMap;
    }

    public int countReceiverRegister(boolean isRegister, String brId) {
        int intValue;
        synchronized (this.mBRCounts) {
            if (isRegister) {
                if (brId != null) {
                    try {
                        if (!brId.isEmpty()) {
                            Integer count = this.mBRCounts.get(brId);
                            if (count == null) {
                                this.mBRCounts.put(brId, 1);
                            } else {
                                this.mBRCounts.put(brId, Integer.valueOf(count.intValue() + 1));
                            }
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            } else if (brId != null && !brId.isEmpty()) {
                Integer count2 = this.mBRCounts.get(brId);
                if (count2 != null) {
                    Integer newCount = Integer.valueOf(count2.intValue() - 1);
                    if (newCount.intValue() == 0) {
                        this.mBRCounts.remove(brId);
                    } else {
                        this.mBRCounts.put(brId, newCount);
                    }
                }
            }
            intValue = this.mBRCounts.get(brId) == null ? 0 : this.mBRCounts.get(brId).intValue();
        }
        return intValue;
    }

    public static String removeBRIdUncommonData(String brId) {
        StringBuffer sb = new StringBuffer(brId);
        int pidPositionStart = sb.indexOf("+");
        if (pidPositionStart != -1) {
            int pidPositionEnd = sb.indexOf("+", pidPositionStart + 1);
            if (pidPositionEnd != -1) {
                sb.delete(pidPositionStart, pidPositionEnd);
            }
        }
        int objectAddressPositionStart = sb.indexOf("@");
        if (objectAddressPositionStart != -1) {
            int objectAddressPositionEnd = sb.indexOf("+", objectAddressPositionStart);
            if (objectAddressPositionEnd != -1) {
                sb.delete(objectAddressPositionStart, objectAddressPositionEnd);
            }
        }
        return sb.toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007a, code lost:
        return null;
     */
    public String findMatchedAssembleConditionId(String brId) {
        if (brId == null) {
            return null;
        }
        String pkg = getPackageNameFromId(brId);
        if (pkg == null || pkg.isEmpty()) {
            return null;
        }
        synchronized (this.mConfigLock) {
            HashMap<String, String> receiverMap = this.mPkgBRsWithCondition.get(pkg);
            if (receiverMap != null) {
                for (Map.Entry<String, String> entry : receiverMap.entrySet()) {
                    String receiver = entry.getKey();
                    if (receiver != null && !receiver.isEmpty() && brId.contains(receiver)) {
                        if (AwareBroadcastDebug.getDebugDetail()) {
                            AwareLog.i(TAG, "Hit !  brId:" + brId + " match  acId: " + entry.getValue());
                        }
                        String value = entry.getValue();
                        return value;
                    }
                }
            }
        }
    }

    public String getBRAssembleCondition(String acId, String action) {
        if (acId == null || action == null) {
            return null;
        }
        synchronized (this.mConfigLock) {
            HashMap<String, String> conditions = this.mBRAssembleConditons.get(acId);
            if (conditions == null) {
                return null;
            }
            String str = conditions.get(action);
            return str;
        }
    }

    private static String getPackageNameFromId(String brId) {
        if (brId == null) {
            return null;
        }
        int pkgPositionEnd = brId.indexOf("+");
        if (pkgPositionEnd != -1) {
            return brId.substring(0, pkgPositionEnd);
        }
        return null;
    }

    private static String getReceiverFromAcID(String acId) {
        if (acId == null) {
            return null;
        }
        int receiverPositionStart = acId.lastIndexOf("+");
        if (receiverPositionStart == -1) {
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
        if (condition == null) {
            return null;
        }
        String action = null;
        int actionEndPosition = condition.indexOf("@");
        if (actionEndPosition != -1) {
            action = condition.substring(0, actionEndPosition);
        }
        return action;
    }

    public void dumpBRRegConfig(PrintWriter pw) {
        updateConfigData();
        synchronized (this.mConfigLock) {
            pw.println("brreg_report_threshold: " + this.mBrRegisterReportThreshold);
            pw.println("Assemble conditions:");
            for (Map.Entry<String, HashMap<String, String>> entry : this.mBRAssembleConditons.entrySet()) {
                if (entry.getValue() != null) {
                    Iterator it = entry.getValue().entrySet().iterator();
                    while (it.hasNext()) {
                        pw.println(entry.getKey() + ", " + ((Map.Entry) it.next()).getValue());
                    }
                }
            }
        }
    }

    public void dumpIawareBRRegInfo(PrintWriter pw) {
        synchronized (this.mBRCounts) {
            pw.println("BR register ID and count:" + this.mBRCounts.size());
            for (Map.Entry<String, Integer> entry : this.mBRCounts.entrySet()) {
                pw.println("" + entry.getKey() + "," + entry.getValue());
            }
        }
    }
}
