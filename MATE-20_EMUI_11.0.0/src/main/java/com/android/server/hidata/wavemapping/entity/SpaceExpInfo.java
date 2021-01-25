package com.android.server.hidata.wavemapping.entity;

import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SpaceExpInfo {
    private static final String BACK_SLASH = "' ";
    private static final String COLON = ":'";
    private static final String COMMA = ", ";
    private static final int DEFAULT_CAPACITY = 16;
    private static final String EQUAL = "'='";
    private static final float RSSI_WEIGHT_OLD = 0.8f;
    private long dataRx = 0;
    private long dataTx = 0;
    private HashMap<String, Long> durationApp = new HashMap<>(16);
    private long durationConnected = 0;
    private String networkFreq = "";
    private String networkId = "";
    private String networkName = "";
    private int networkType = 8;
    private long powerConsumption = 0;
    private HashMap<String, Integer> qoeAppGood = new HashMap<>(16);
    private HashMap<String, Integer> qoeAppPoor = new HashMap<>(16);
    private int qoeWifiProCommon = 0;
    private int qoeWifiProGood = 0;
    private int qoeWifiProPoor = 0;
    private int signalValue = 0;
    private StringBuilder spaceId = new StringBuilder("0");
    private StringBuilder spaceIdMainAp = new StringBuilder("0");
    private int userPrefOptIn = 0;
    private int userPrefOptOut = 0;
    private int userPrefStay = 0;
    private int userPrefTotalCount = 0;

    public SpaceExpInfo(StringBuilder spaceId2, StringBuilder spaceIdMainAp2, String networkId2, String networkName2, String networkFreq2, int qoeWifiProGood2, int qoeWifiProCommon2, int qoeWifiProPoor2, int signalValue2, int userPrefOptIn2, int userPrefOptOut2, int userPrefStay2, int userPrefTotalCount2, long powerConsumption2, long durationConnected2, int networkType2) {
        this.spaceId = spaceId2;
        this.spaceIdMainAp = spaceIdMainAp2;
        this.networkId = networkId2;
        this.networkName = networkName2;
        this.networkFreq = networkFreq2;
        this.qoeWifiProGood = qoeWifiProGood2;
        this.qoeWifiProCommon = qoeWifiProCommon2;
        this.qoeWifiProPoor = qoeWifiProPoor2;
        this.signalValue = signalValue2;
        this.powerConsumption = powerConsumption2;
        this.userPrefOptIn = userPrefOptIn2;
        this.userPrefOptOut = userPrefOptOut2;
        this.userPrefStay = userPrefStay2;
        this.userPrefTotalCount = userPrefTotalCount2;
        this.durationConnected = durationConnected2;
        this.networkType = networkType2;
    }

    public SpaceExpInfo(StringBuilder spaceId2, StringBuilder spaceIdMainAp2, String networkId2, String networkName2, String networkFreq2, HashMap<String, Integer> appPoor, HashMap<String, Integer> appGood, HashMap<String, Long> appDuration, int qoeWifiProGood2, int qoeWifiProCommon2, int qoeWifiProPoor2, int signalValue2, long powerConsumption2, int userPrefOptIn2, int userPrefOptOut2, int userPrefStay2, int userPrefTotalCount2, long durationConnected2, int networkType2) {
        this.spaceId = spaceId2;
        this.spaceIdMainAp = spaceIdMainAp2;
        this.networkId = networkId2;
        this.networkName = networkName2;
        this.networkFreq = networkFreq2;
        this.qoeAppPoor = appPoor;
        this.qoeAppGood = appGood;
        this.durationApp = appDuration;
        this.qoeWifiProGood = qoeWifiProGood2;
        this.qoeWifiProCommon = qoeWifiProCommon2;
        this.qoeWifiProPoor = qoeWifiProPoor2;
        this.signalValue = signalValue2;
        this.powerConsumption = powerConsumption2;
        this.userPrefOptIn = userPrefOptIn2;
        this.userPrefOptOut = userPrefOptOut2;
        this.userPrefStay = userPrefStay2;
        this.userPrefTotalCount = userPrefTotalCount2;
        this.durationConnected = durationConnected2;
        this.networkType = networkType2;
    }

    public SpaceExpInfo(StringBuilder spaceId2, StringBuilder spaceIdMainAp2, String networkId2, String networkName2, String networkFreq2, int networkType2) {
        this.spaceId = spaceId2;
        this.spaceIdMainAp = spaceIdMainAp2;
        this.networkId = networkId2;
        this.networkName = networkName2;
        this.networkFreq = networkFreq2;
        this.networkType = networkType2;
    }

    public String getNetworkName() {
        return this.networkName;
    }

    public void setNetworkName(String networkname) {
        this.networkName = networkname;
    }

    public String getNetworkId() {
        return this.networkId;
    }

    public String getNetworkFreq() {
        return this.networkFreq;
    }

    public void setNetworkFreq(String networkfreq) {
        this.networkFreq = networkfreq;
    }

    public int getSpaceId() {
        if (this.spaceId == null || !Constant.PATTERN_STR2INT.matcher(this.spaceId).matches()) {
            return 0;
        }
        try {
            return Integer.parseInt(this.spaceId.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getSpaceIdMain() {
        if (this.spaceIdMainAp == null || !Constant.PATTERN_STR2INT.matcher(this.spaceIdMainAp).matches()) {
            return 0;
        }
        try {
            return Integer.parseInt(this.spaceIdMainAp.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getQoeWifiProPoor() {
        return this.qoeWifiProPoor;
    }

    public void setQoeWifiProPoor(int qoeWifiProPoor2) {
        this.qoeWifiProPoor = qoeWifiProPoor2;
    }

    public int getQoeWifiProCommon() {
        return this.qoeWifiProCommon;
    }

    public void setQoeWifiProCommon(int qoeWifiProCommon2) {
        this.qoeWifiProCommon = qoeWifiProCommon2;
    }

    public int getQoeWifiProGood() {
        return this.qoeWifiProGood;
    }

    public void setQoeWifiProGood(int qoeWifiProGood2) {
        this.qoeWifiProGood = qoeWifiProGood2;
    }

    public int getSignalValue() {
        return this.signalValue;
    }

    public int getUserPrefOptIn() {
        return this.userPrefOptIn;
    }

    public void accUserPrefOptIn() {
        this.userPrefOptIn++;
    }

    public int getUserPrefOptOut() {
        return this.userPrefOptOut;
    }

    public void accUserPrefOptOut() {
        this.userPrefOptOut++;
    }

    public int getUserPrefStay() {
        return this.userPrefStay;
    }

    public void accUserPrefStay() {
        this.userPrefStay++;
    }

    public int getUserPrefTotalCount() {
        return this.userPrefTotalCount;
    }

    public void accUserPrefTotalCount() {
        this.userPrefTotalCount++;
    }

    public HashMap<String, Integer> getMapAppQoePoor() {
        return this.qoeAppPoor;
    }

    public HashMap<String, Integer> getMapAppQoeGood() {
        return this.qoeAppGood;
    }

    public HashMap<String, Long> getMapAppDuration() {
        return this.durationApp;
    }

    public int getAppQoePoor(String app) {
        if (this.qoeAppPoor.containsKey(app)) {
            return this.qoeAppPoor.get(app).intValue();
        }
        return 0;
    }

    public int getAppQoeGood(String app) {
        if (this.qoeAppGood.containsKey(app)) {
            return this.qoeAppGood.get(app).intValue();
        }
        return 0;
    }

    public long getAppDuration(String app) {
        if (this.durationApp.containsKey(app)) {
            return this.durationApp.get(app).longValue();
        }
        return 0;
    }

    public long getDuration() {
        return this.durationConnected;
    }

    public int getNetworkType() {
        return this.networkType;
    }

    public long getPowerConsumption() {
        return this.powerConsumption;
    }

    public void accPowerConsumption(long power) {
        this.powerConsumption += power;
    }

    public long getDataRx() {
        return this.dataRx;
    }

    public long getDataTx() {
        return this.dataTx;
    }

    public void accDataTraffic(long newDataRx, long newDataTx) {
        this.dataRx += newDataRx;
        this.dataTx += newDataTx;
        LogUtil.i(false, "update data traffic: acc_rx=%{public}s, acc_tx=%{public}s", String.valueOf(this.dataRx), String.valueOf(this.dataTx));
    }

    public void accQoeWifiProPoor() {
        this.qoeWifiProPoor++;
    }

    public void accQoeWifiProCommon() {
        this.qoeWifiProCommon++;
    }

    public void accQoeWifiProGood() {
        this.qoeWifiProGood++;
    }

    public void accAppPoor(String app) {
        if (this.qoeAppPoor.containsKey(app)) {
            this.qoeAppPoor.put(app, Integer.valueOf(this.qoeAppPoor.get(app).intValue() + 1));
            return;
        }
        this.qoeAppPoor.put(app, 1);
    }

    public void accAppGood(String app) {
        if (this.qoeAppGood.containsKey(app)) {
            this.qoeAppGood.put(app, Integer.valueOf(this.qoeAppGood.get(app).intValue() + 1));
            return;
        }
        this.qoeAppGood.put(app, 1);
    }

    private void accAppDuration(String app, long newDuration) {
        if (this.durationApp.containsKey(app)) {
            this.durationApp.put(app, Long.valueOf(this.durationApp.get(app).longValue() + newDuration));
            return;
        }
        this.durationApp.put(app, Long.valueOf(newDuration));
    }

    public int accSignalValue(int newRssi) {
        if (newRssi < 0) {
            int i = this.signalValue;
            if (i < 0) {
                this.signalValue = Math.round((((float) i) * 0.8f) + (((float) newRssi) * 0.19999999f));
            } else {
                this.signalValue = newRssi;
            }
        }
        return this.signalValue;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0046  */
    public void accDuration(String app, long newDuration) {
        char c;
        int hashCode = app.hashCode();
        if (hashCode != -2015525726) {
            if (hashCode == 2664213 && app.equals(Constant.USERDB_APP_NAME_WIFI)) {
                c = 0;
                if (c == 0) {
                    LogUtil.i(false, "update WIFI duration", new Object[0]);
                    this.durationConnected += newDuration;
                    this.networkType = 1;
                    return;
                } else if (c != 1) {
                    LogUtil.i(false, "update APP duration", new Object[0]);
                    accAppDuration(app, newDuration);
                    return;
                } else {
                    LogUtil.i(false, "update MOBILE duration", new Object[0]);
                    this.durationConnected += newDuration;
                    this.networkType = 0;
                    return;
                }
            }
        } else if (app.equals(Constant.USERDB_APP_NAME_MOBILE)) {
            c = 1;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    public void mergeAllRecords(SpaceExpInfo input) {
        this.qoeWifiProPoor += input.getQoeWifiProPoor();
        this.qoeWifiProCommon += input.getQoeWifiProCommon();
        this.qoeWifiProGood += input.getQoeWifiProGood();
        accSignalValue(input.getSignalValue());
        this.userPrefOptIn += input.getUserPrefOptIn();
        this.userPrefOptOut += input.getUserPrefOptOut();
        this.userPrefStay += input.getUserPrefStay();
        this.userPrefTotalCount += input.getUserPrefTotalCount();
        this.durationConnected += input.getDuration();
        this.dataRx += input.getDataRx();
        this.dataTx += input.getDataTx();
        this.powerConsumption += input.getPowerConsumption();
        if (input.getNetworkType() == 1 || input.getNetworkType() == 0) {
            this.networkType = input.getNetworkType();
        }
        input.getMapAppQoePoor().forEach(new BiConsumer() {
            /* class com.android.server.hidata.wavemapping.entity.$$Lambda$SpaceExpInfo$QI7zLW8XkAu7n4wXBYEAENZDzo */

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                SpaceExpInfo.this.lambda$mergeAllRecords$0$SpaceExpInfo((String) obj, (Integer) obj2);
            }
        });
        input.getMapAppQoeGood().forEach(new BiConsumer() {
            /* class com.android.server.hidata.wavemapping.entity.$$Lambda$SpaceExpInfo$cPw96sVJ3DDEjslELgkVohq8jSA */

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                SpaceExpInfo.this.lambda$mergeAllRecords$1$SpaceExpInfo((String) obj, (Integer) obj2);
            }
        });
        input.getMapAppDuration().forEach(new BiConsumer() {
            /* class com.android.server.hidata.wavemapping.entity.$$Lambda$SpaceExpInfo$JzzV2KLKzGPchCYgN8JC_0Y1reM */

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                SpaceExpInfo.this.lambda$mergeAllRecords$2$SpaceExpInfo((String) obj, (Long) obj2);
            }
        });
    }

    public /* synthetic */ void lambda$mergeAllRecords$0$SpaceExpInfo(String k, Integer v) {
        this.qoeAppPoor.merge(k, v, $$Lambda$LrkJFe4YP5gsc0rXJgTGXS3PRE.INSTANCE);
    }

    public /* synthetic */ void lambda$mergeAllRecords$1$SpaceExpInfo(String k, Integer v) {
        this.qoeAppGood.merge(k, v, $$Lambda$LrkJFe4YP5gsc0rXJgTGXS3PRE.INSTANCE);
    }

    public /* synthetic */ void lambda$mergeAllRecords$2$SpaceExpInfo(String k, Long v) {
        this.durationApp.merge(k, v, $$Lambda$UNDzBq9YY5vB0tVYGv7BxBnAJ8Y.INSTANCE);
    }

    public String toString() {
        StringBuffer appString = new StringBuffer(16);
        int poorCnt = 0;
        int goodCnt = 0;
        for (Map.Entry<String, Long> entry : this.durationApp.entrySet()) {
            String app = entry.getKey();
            long duration = entry.getValue().longValue();
            if (this.qoeAppPoor.containsKey(app)) {
                poorCnt = this.qoeAppPoor.get(app).intValue();
            }
            if (this.qoeAppGood.containsKey(app)) {
                goodCnt = this.qoeAppGood.get(app).intValue();
            }
            appString.append(COMMA);
            appString.append(app);
            appString.append(COLON);
            appString.append(Constant.USERDB_APP_NAME_DURATION);
            appString.append(EQUAL);
            appString.append(duration);
            appString.append(BACK_SLASH);
            appString.append(Constant.USERDB_APP_NAME_POOR);
            appString.append(EQUAL);
            appString.append(poorCnt);
            appString.append(BACK_SLASH);
            appString.append(Constant.USERDB_APP_NAME_GOOD);
            appString.append(EQUAL);
            appString.append(goodCnt);
            appString.append(BACK_SLASH);
            appString.append("");
        }
        String netIdPrint = "";
        if (LogUtil.getDebugFlag()) {
            netIdPrint = this.networkId;
        }
        return "SpaceExpInfo{spaceid='" + this.spaceId.toString() + "', spaceid_mainap='" + ((Object) this.spaceIdMainAp) + "', networkname='" + this.networkName + "', networkid='" + netIdPrint + "', networkfreq='" + this.networkFreq + "', qoeWifiProPoor=" + this.qoeWifiProPoor + ", qoeWifiProCommon=" + this.qoeWifiProCommon + ", qoeWifiProGood=" + this.qoeWifiProGood + ", signalValue=" + this.signalValue + ", powerConsumption=" + this.powerConsumption + ", dataRx=" + this.dataRx + ", dataTx=" + this.dataTx + ", userPrefOptIn=" + this.userPrefOptIn + ", userPrefOptOut=" + this.userPrefOptOut + ", userPrefStay=" + this.userPrefStay + ", userPrefTotalCount=" + this.userPrefTotalCount + ", duration=" + this.durationConnected + ", nw_type=" + this.networkType + appString.toString() + '}';
    }
}
