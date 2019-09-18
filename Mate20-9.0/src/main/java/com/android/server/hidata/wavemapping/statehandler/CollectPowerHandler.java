package com.android.server.hidata.wavemapping.statehandler;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.SpaceUserDAO;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.SpaceExpInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.huawei.dubai.DubaiData;
import com.huawei.dubai.IDubaiListener;
import com.huawei.dubai.IDubaiService;
import java.util.HashMap;
import java.util.Map;

public class CollectPowerHandler {
    private static final int APPS_TOP_20 = 20;
    private static final int DEFAULT_DATA_SIZE = 1;
    private static final int MAX_MODEM_TYPES = 4;
    private static final String TAG = ("WMapping." + CollectPowerHandler.class.getSimpleName());
    private static CollectPowerHandler mCollectPowerHandler = null;
    private long REQ_PERIOD_THREDHOLD = 180000;
    private Context mContext;
    private IDubaiService mDubaiService = null;
    private DubaiListener mListener = null;
    private Handler mMachineHandler;
    private SpaceExpInfo mPowerSaveSpaceExpInfo = null;
    private HashMap<String, SpaceExpInfo> mPowerSaveSpaceExperience = new HashMap<>();
    private String mRequestNetworkId = "UNKNOWN";
    private SpaceUserDAO mSpaceUserDAO;
    private StringBuilder mSpaceid = new StringBuilder("0");
    private boolean mSpaceidFlag = false;
    private StringBuilder mSpaceid_mainAp = new StringBuilder("0");
    private ParameterInfo param;

    private class DubaiListener extends IDubaiListener.Stub {
        private DubaiListener() {
        }

        public void onQueryResponse(DubaiData data) throws RemoteException {
            if (data == null) {
                LogUtil.e("Invalid DubaiData");
                return;
            }
            Parcel parcel = Parcel.obtain();
            data.writeToParcel(parcel, 0);
            CollectPowerHandler.this.handleQueryResult(parcel);
            parcel.recycle();
        }
    }

    public CollectPowerHandler(Handler handler) {
        LogUtil.i(" ,new CollectPowerHandler ");
        try {
            this.mContext = ContextManager.getInstance().getContext();
            this.param = ParamManager.getInstance().getParameterInfo();
            this.mMachineHandler = handler;
            this.mSpaceUserDAO = SpaceUserDAO.getInstance();
            if (this.mDubaiService == null || this.mListener == null) {
                this.mDubaiService = IDubaiService.Stub.asInterface(ServiceManager.getService("DubaiService"));
                if (this.mDubaiService != null) {
                    this.mListener = new DubaiListener();
                }
            }
        } catch (Exception e) {
            LogUtil.e(" CollectPowerHandler " + e.getMessage());
        }
    }

    public static synchronized CollectPowerHandler getInstance(Handler handler) {
        CollectPowerHandler collectPowerHandler;
        synchronized (CollectPowerHandler.class) {
            if (mCollectPowerHandler == null) {
                mCollectPowerHandler = new CollectPowerHandler(handler);
            }
            collectPowerHandler = mCollectPowerHandler;
        }
        return collectPowerHandler;
    }

    public void setSpaceId(int spaceId_all, int spaceId_main) {
        this.mSpaceid.setLength(0);
        this.mSpaceid.trimToSize();
        this.mSpaceid.append(spaceId_all);
        this.mSpaceid_mainAp.setLength(0);
        this.mSpaceid_mainAp.trimToSize();
        this.mSpaceid_mainAp.append(spaceId_main);
        this.mSpaceidFlag = true;
        LogUtil.i(" setSpaceId mSpaceidFlag = true");
    }

    private HashMap<String, SpaceExpInfo> mergeSumOfMaps(HashMap<String, SpaceExpInfo>... maps) {
        SpaceExpInfo value;
        HashMap<String, SpaceExpInfo> resultMap = new HashMap<>();
        for (HashMap<String, SpaceExpInfo> map : maps) {
            for (Map.Entry<String, SpaceExpInfo> entry : map.entrySet()) {
                String key = entry.getKey();
                LogUtil.v(" mergeSumOfMaps: nwId=" + key);
                if (resultMap.containsKey(key)) {
                    value = entry.getValue();
                    value.mergeAllRecords(resultMap.get(key));
                } else {
                    value = entry.getValue();
                }
                resultMap.put(key, value);
            }
        }
        return resultMap;
    }

    private void savePowerSpaceExptoDatabase() {
        try {
            if (this.mPowerSaveSpaceExperience.size() != 0) {
                int num = 0;
                for (Map.Entry<String, SpaceExpInfo> entry : this.mPowerSaveSpaceExperience.entrySet()) {
                    if (((SpaceExpInfo) entry.getValue()) != null) {
                        LogUtil.d(" mPowerSaveSpaceExperience:");
                        LogUtil.i("                            Records" + num + ": " + val.toString());
                        num++;
                    }
                }
            }
            if (this.mPowerSaveSpaceExperience.size() == 0 || this.mPowerSaveSpaceExpInfo == null) {
                LogUtil.d(" mPowerSaveSpaceExperience size=0");
            } else {
                LogUtil.i(" load PowerSpaceExp from Database");
                int num2 = 0;
                for (Map.Entry<String, SpaceExpInfo> entry2 : mergeSumOfMaps(this.mPowerSaveSpaceExperience, this.mSpaceUserDAO.findAllByTwoSpaces(this.mPowerSaveSpaceExpInfo.getSpaceID(), this.mPowerSaveSpaceExpInfo.getSpaceIDMain())).entrySet()) {
                    SpaceExpInfo val = (SpaceExpInfo) entry2.getValue();
                    if (val != null) {
                        LogUtil.d(" save PowerSpaceExp to Database:");
                        LogUtil.i("                            Records" + num2 + ": " + val.toString());
                        this.mSpaceUserDAO.insertBase(val);
                        num2++;
                    }
                }
            }
            this.mPowerSaveSpaceExperience.clear();
            this.mSpaceidFlag = false;
            LogUtil.i(" Clear mPowerSaveSpaceExperience and mSpaceidFlag is false");
        } catch (Exception e) {
            LogUtil.e(" savePowerSpaceExptoDatabase " + e.getMessage());
        }
    }

    public void requestPowerData(long starttime, String nwId, String nwName, String nwFreq, int devicetype) {
        long j = starttime;
        String str = nwId;
        int i = devicetype;
        try {
            if (this.mDubaiService == null || this.mListener == null) {
                this.mDubaiService = IDubaiService.Stub.asInterface(ServiceManager.getService("DubaiService"));
                if (this.mDubaiService != null) {
                    this.mListener = new DubaiListener();
                }
            }
            if (this.mDubaiService == null || this.mListener == null) {
                LogUtil.e("Dubai Service aidl is not ready");
                return;
            }
            long now = System.currentTimeMillis();
            if (this.mSpaceidFlag) {
                savePowerSpaceExptoDatabase();
            }
            if (now - j > this.REQ_PERIOD_THREDHOLD) {
                LogUtil.d("Request Period - Start: " + j + " End :" + now + " Device Type:" + i);
                this.mRequestNetworkId = str;
                this.mPowerSaveSpaceExpInfo = null;
                if (this.mPowerSaveSpaceExperience.containsKey(str)) {
                    this.mPowerSaveSpaceExpInfo = this.mPowerSaveSpaceExperience.get(str);
                }
                if (this.mPowerSaveSpaceExpInfo == null) {
                    SpaceExpInfo spaceExpInfo = new SpaceExpInfo(this.mSpaceid, this.mSpaceid_mainAp, str, nwName, nwFreq, i);
                    this.mPowerSaveSpaceExpInfo = spaceExpInfo;
                }
                if (i == 1) {
                    long j2 = now;
                    this.mDubaiService.requestQueryData(j, now, 4096, 5, 20, this.mListener);
                } else {
                    long now2 = now;
                    if (i == 0) {
                        this.mDubaiService.requestQueryData(j, now2, 1024, 5, 20, this.mListener);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(" CollectPowerHandler " + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void handleQueryResult(Parcel data) {
        while (data.dataAvail() > 0) {
            int module = data.readInt();
            LogUtil.d("module=" + module);
            if (module == 10) {
                handleModemData(data);
                this.mPowerSaveSpaceExperience.put(this.mRequestNetworkId, this.mPowerSaveSpaceExpInfo);
            } else if (module != 12) {
                LogUtil.d("Unknown module: " + module);
            } else {
                handleWifiData(data);
                this.mPowerSaveSpaceExperience.put(this.mRequestNetworkId, this.mPowerSaveSpaceExpInfo);
            }
        }
    }

    private int getComponentModemTraffic(Parcel data) {
        int size = data.readInt();
        if (size <= -1) {
            LogUtil.e("Query modem component traffic data error");
            return -1;
        } else if (size == 0) {
            LogUtil.e("No modem component traffic data");
            return 0;
        } else {
            if (size != 1) {
                LogUtil.e("Data size may not right: " + size);
            }
            long startTime = data.readLong();
            long endTime = data.readLong();
            long fgTxBytes = data.readLong();
            long fgRxBytes = data.readLong();
            long fgEnergy = data.readLong();
            long bgTxBytes = data.readLong();
            long bgRxBytes = data.readLong();
            int i = size;
            long bgEnergy = data.readLong();
            long screenOnTxBytes = data.readLong();
            long screenOnRxBytes = data.readLong();
            long screenOnEnergy = data.readLong();
            long screenOffTxBytes = data.readLong();
            long screenOffRxBytes = data.readLong();
            long screenOffEnergy = data.readLong();
            StringBuilder sb = new StringBuilder();
            sb.append("[MODEM-DATA] startTime=");
            long screenOffEnergy2 = screenOffEnergy;
            long startTime2 = startTime;
            sb.append(startTime2);
            sb.append(", endTime=");
            sb.append(endTime);
            sb.append(", fgTxBytes=");
            sb.append(fgTxBytes);
            sb.append(", fgRxBytes=");
            sb.append(fgRxBytes);
            sb.append(", fgEnergy=");
            sb.append(fgEnergy);
            sb.append(", bgTxBytes=");
            sb.append(bgTxBytes);
            sb.append(", bgRxBytes=");
            sb.append(bgRxBytes);
            sb.append(", bgEnergy=");
            long j = startTime2;
            long startTime3 = bgEnergy;
            sb.append(startTime3);
            sb.append(", screenOnTxBytes=");
            long bgEnergy2 = startTime3;
            long screenOnTxBytes2 = screenOnTxBytes;
            sb.append(screenOnTxBytes2);
            sb.append(", screenOnRxBytes=");
            long j2 = bgRxBytes;
            long screenOnRxBytes2 = screenOnRxBytes;
            sb.append(screenOnRxBytes2);
            sb.append(", screenOnEnergy=");
            long j3 = endTime;
            long screenOnEnergy2 = screenOnEnergy;
            sb.append(screenOnEnergy2);
            sb.append(", screenOffTxBytes=");
            long j4 = fgTxBytes;
            long screenOffTxBytes2 = screenOffTxBytes;
            sb.append(screenOffTxBytes2);
            sb.append(", screenOffRxBytes=");
            long j5 = fgRxBytes;
            long screenOffRxBytes2 = screenOffRxBytes;
            sb.append(screenOffRxBytes2);
            sb.append(", screenOffEnergy=");
            long j6 = fgEnergy;
            long screenOffEnergy3 = screenOffEnergy2;
            sb.append(screenOffEnergy3);
            LogUtil.d(sb.toString());
            long j7 = bgTxBytes;
            long j8 = bgEnergy2;
            if (this.mPowerSaveSpaceExpInfo != null) {
                this.mPowerSaveSpaceExpInfo.accDubaiScreenOnTraffic(screenOnRxBytes2, screenOnTxBytes2);
                this.mPowerSaveSpaceExpInfo.accDubaiScreenOffTraffic(screenOffRxBytes2, screenOffTxBytes2);
                this.mPowerSaveSpaceExpInfo.accDubaiPower(screenOnEnergy2, screenOffEnergy3);
            }
            return 0;
        }
    }

    private int getComponentModemBasic(Parcel data) {
        long type;
        long duration;
        long energy;
        int size = data.readInt();
        if (size <= -1) {
            LogUtil.e("Query modem component basic data error");
            return -1;
        } else if (size == 0) {
            LogUtil.e("No modem component basic data");
            return 0;
        } else {
            if (size > 4) {
                LogUtil.e("Data size may not right: " + size);
            }
            for (int i = 0; i < size; i++) {
                long startTime = data.readLong();
                long endTime = data.readLong();
                LogUtil.d("[MODEM-BASIC] type=" + type + ", startTime=" + startTime + ", endTime=" + endTime + ", duration=" + duration + ", energy=" + energy);
                if (this.mPowerSaveSpaceExpInfo != null && type == 1) {
                    this.mPowerSaveSpaceExpInfo.accDubaiIdleDuration(duration);
                    this.mPowerSaveSpaceExpInfo.accDuabiIdlePower(energy);
                }
            }
            return 0;
        }
    }

    private void getApplicationsModem(Parcel data) {
        int size = data.readInt();
        if (size <= 0) {
            LogUtil.e("No modem applications data");
            return;
        }
        int i = 0;
        while (i < size) {
            long startTime = data.readLong();
            long endTime = data.readLong();
            String name = data.readString();
            long fgTxBytes = data.readLong();
            long fgRxBytes = data.readLong();
            long fgEnergy = data.readLong();
            long bgTxBytes = data.readLong();
            int size2 = size;
            long bgRxBytes = data.readLong();
            long bgEnergy = data.readLong();
            long screenOnTxBytes = data.readLong();
            long screenOnRxBytes = data.readLong();
            long screenOnEnergy = data.readLong();
            long screenOffTxBytes = data.readLong();
            long screenOffRxBytes = data.readLong();
            long screenOffEnergy = data.readLong();
            int size3 = size2;
            StringBuilder sb = new StringBuilder();
            long screenOffEnergy2 = screenOffEnergy;
            sb.append("[MODEM-DATA] startTime=");
            sb.append(startTime);
            sb.append(", endTime=");
            sb.append(endTime);
            sb.append(", name=");
            sb.append(name);
            sb.append(", fgTxBytes=");
            sb.append(fgTxBytes);
            sb.append(", fgRxBytes=");
            sb.append(fgRxBytes);
            sb.append(", fgEnergy=");
            sb.append(fgEnergy);
            sb.append(", bgTxBytes=");
            sb.append(bgTxBytes);
            sb.append(", bgRxBytes=");
            long bgRxBytes2 = bgRxBytes;
            sb.append(bgRxBytes2);
            long j = bgRxBytes2;
            sb.append(", bgEnergy=");
            long bgEnergy2 = bgEnergy;
            sb.append(bgEnergy2);
            long j2 = bgEnergy2;
            sb.append(", screenOnTxBytes=");
            long screenOnTxBytes2 = screenOnTxBytes;
            sb.append(screenOnTxBytes2);
            long j3 = screenOnTxBytes2;
            sb.append(", screenOnRxBytes=");
            long screenOnRxBytes2 = screenOnRxBytes;
            sb.append(screenOnRxBytes2);
            long j4 = screenOnRxBytes2;
            sb.append(", screenOnEnergy=");
            long screenOnEnergy2 = screenOnEnergy;
            sb.append(screenOnEnergy2);
            long j5 = screenOnEnergy2;
            sb.append(", screenOffTxBytes=");
            long screenOffTxBytes2 = screenOffTxBytes;
            sb.append(screenOffTxBytes2);
            long j6 = screenOffTxBytes2;
            sb.append(", screenOffRxBytes=");
            long screenOffRxBytes2 = screenOffRxBytes;
            sb.append(screenOffRxBytes2);
            long j7 = screenOffRxBytes2;
            sb.append(", screenOffEnergy=");
            sb.append(screenOffEnergy2);
            LogUtil.d(sb.toString());
            i++;
            size = size3;
        }
        int i2 = i;
    }

    private void handleModemData(Parcel data) {
        if ((!data.readBoolean() || (getComponentModemTraffic(data) >= 0 && getComponentModemBasic(data) >= 0)) && data.readBoolean() != 0) {
            getApplicationsModem(data);
        }
    }

    private int getComponentWifiAp(Parcel data) {
        int size = data.readInt();
        if (size <= -1) {
            LogUtil.e("Query wifi ap component data error");
            return -1;
        } else if (size == 0) {
            LogUtil.e("No wifi ap component data");
            return 0;
        } else {
            if (size != 1) {
                LogUtil.e("Data size may not right: " + size);
            }
            long startTime = data.readLong();
            long endTime = data.readLong();
            long screenOnDuration = data.readLong();
            long screenOnEnergy = data.readLong();
            long screenOffDuration = data.readLong();
            long screenOffEnergy = data.readLong();
            LogUtil.d("[WIFI-AP] startTime=" + startTime + ", endTime=" + endTime + ", screenOnDuration=" + screenOnDuration + ", screenOnEnergy=" + screenOnEnergy + ", screenOffDuration=" + screenOffDuration + ", screenOffEnergy=" + screenOffEnergy);
            return 0;
        }
    }

    private int getComponentWifiScan(Parcel data) {
        int size = data.readInt();
        if (size <= -1) {
            LogUtil.e("Query wifi scan component data error");
            return -1;
        } else if (size == 0) {
            LogUtil.e("No wifi scan component data");
            return 0;
        } else {
            if (size != 1) {
                LogUtil.e("Data size may not right: " + size);
            }
            long startTime = data.readLong();
            long endTime = data.readLong();
            long fgCount = data.readLong();
            long fgEnergy = data.readLong();
            long bgCount = data.readLong();
            long bgEnergy = data.readLong();
            long screenOnCount = data.readLong();
            long startTime2 = startTime;
            int i = size;
            long screenOffCount = data.readLong();
            long screenOffEnergy = data.readLong();
            StringBuilder sb = new StringBuilder();
            sb.append("[WIFI-SCAN] startTime=");
            long screenOffCount2 = screenOffCount;
            long startTime3 = startTime2;
            sb.append(startTime3);
            sb.append(", endTime=");
            sb.append(endTime);
            sb.append(", fgCount=");
            sb.append(fgCount);
            sb.append(", fgEnergy=");
            sb.append(fgEnergy);
            sb.append(", bgCount=");
            sb.append(bgCount);
            sb.append(", bgEnergy=");
            sb.append(bgEnergy);
            sb.append(", screenOnCount=");
            sb.append(screenOnCount);
            sb.append(", screenOnEnergy=");
            long j = startTime3;
            long screenOnEnergy = data.readLong();
            sb.append(screenOnEnergy);
            sb.append(", screenOffCount=");
            long j2 = screenOnEnergy;
            long screenOnEnergy2 = screenOffCount2;
            sb.append(screenOnEnergy2);
            sb.append(", screenOffEnergy=");
            long j3 = screenOnEnergy2;
            sb.append(screenOffEnergy);
            LogUtil.d(sb.toString());
            return 0;
        }
    }

    private int getComponentWifiTraffic(Parcel data) {
        int size = data.readInt();
        if (size <= -1) {
            LogUtil.e("Query wifi traffic component data error");
            return -1;
        } else if (size == 0) {
            LogUtil.e("No wifi traffic component data");
            return 0;
        } else {
            if (size != 1) {
                LogUtil.e("Data size may not right: " + size);
            }
            long startTime = data.readLong();
            long endTime = data.readLong();
            long fgTxBytes = data.readLong();
            long fgRxBytes = data.readLong();
            long fgEnergy = data.readLong();
            long bgTxBytes = data.readLong();
            long bgRxBytes = data.readLong();
            int i = size;
            long bgEnergy = data.readLong();
            long screenOnTxBytes = data.readLong();
            long screenOnRxBytes = data.readLong();
            long screenOnEnergy = data.readLong();
            long screenOffTxBytes = data.readLong();
            long screenOffRxBytes = data.readLong();
            long screenOffEnergy = data.readLong();
            StringBuilder sb = new StringBuilder();
            sb.append("[WIFI-DATA] startTime=");
            long screenOffEnergy2 = screenOffEnergy;
            long startTime2 = startTime;
            sb.append(startTime2);
            sb.append(", endTime=");
            sb.append(endTime);
            sb.append(", fgTxBytes=");
            sb.append(fgTxBytes);
            sb.append(", fgRxBytes=");
            sb.append(fgRxBytes);
            sb.append(", fgEnergy=");
            sb.append(fgEnergy);
            sb.append(", bgTxBytes=");
            sb.append(bgTxBytes);
            sb.append(", bgRxBytes=");
            sb.append(bgRxBytes);
            sb.append(", bgEnergy=");
            long j = startTime2;
            long startTime3 = bgEnergy;
            sb.append(startTime3);
            sb.append(", screenOnTxBytes=");
            long bgEnergy2 = startTime3;
            long screenOnTxBytes2 = screenOnTxBytes;
            sb.append(screenOnTxBytes2);
            sb.append(", screenOnRxBytes=");
            long j2 = bgRxBytes;
            long screenOnRxBytes2 = screenOnRxBytes;
            sb.append(screenOnRxBytes2);
            sb.append(", screenOnEnergy=");
            long j3 = endTime;
            long screenOnEnergy2 = screenOnEnergy;
            sb.append(screenOnEnergy2);
            sb.append(", screenOffTxBytes=");
            long j4 = fgTxBytes;
            long screenOffTxBytes2 = screenOffTxBytes;
            sb.append(screenOffTxBytes2);
            sb.append(", screenOffRxBytes=");
            long j5 = fgRxBytes;
            long screenOffRxBytes2 = screenOffRxBytes;
            sb.append(screenOffRxBytes2);
            sb.append(", screenOffEnergy=");
            long j6 = fgEnergy;
            long screenOffEnergy3 = screenOffEnergy2;
            sb.append(screenOffEnergy3);
            LogUtil.d(sb.toString());
            long j7 = bgTxBytes;
            long j8 = bgEnergy2;
            if (this.mPowerSaveSpaceExpInfo != null) {
                this.mPowerSaveSpaceExpInfo.accDubaiScreenOnTraffic(screenOnRxBytes2, screenOnTxBytes2);
                this.mPowerSaveSpaceExpInfo.accDubaiScreenOffTraffic(screenOffRxBytes2, screenOffTxBytes2);
                this.mPowerSaveSpaceExpInfo.accDubaiPower(screenOnEnergy2, screenOffEnergy3);
            }
            return 0;
        }
    }

    private int getApplicationsWifiScan(Parcel data) {
        int size = data.readInt();
        if (size <= -1) {
            LogUtil.e("Query wifi scan applications data error");
            return -1;
        } else if (size == 0) {
            LogUtil.e("No wifi scan applications data");
            return 0;
        } else {
            int i = 0;
            while (i < size) {
                long startTime = data.readLong();
                long endTime = data.readLong();
                String name = data.readString();
                long fgCount = data.readLong();
                long fgEnergy = data.readLong();
                long bgCount = data.readLong();
                long bgEnergy = data.readLong();
                int i2 = i;
                int size2 = size;
                long screenOnEnergy = data.readLong();
                long screenOffCount = data.readLong();
                long screenOffCount2 = data.readLong();
                StringBuilder sb = new StringBuilder();
                long screenOffEnergy = screenOffCount2;
                sb.append("[WIFI-SCAN] startTime=");
                sb.append(startTime);
                sb.append(", endTime=");
                sb.append(endTime);
                sb.append(", name=");
                sb.append(name);
                sb.append(", fgCount=");
                sb.append(fgCount);
                sb.append(", fgEnergy=");
                sb.append(fgEnergy);
                sb.append(", bgCount=");
                sb.append(bgCount);
                sb.append(", bgEnergy=");
                sb.append(bgEnergy);
                sb.append(", screenOnCount=");
                long screenOnCount = data.readLong();
                sb.append(screenOnCount);
                long j = screenOnCount;
                sb.append(", screenOnEnergy=");
                long screenOnEnergy2 = screenOnEnergy;
                sb.append(screenOnEnergy2);
                long j2 = screenOnEnergy2;
                sb.append(", screenOffCount=");
                long screenOffCount3 = screenOffCount;
                sb.append(screenOffCount3);
                long j3 = screenOffCount3;
                sb.append(", screenOffEnergy=");
                sb.append(screenOffEnergy);
                LogUtil.d(sb.toString());
                i = i2 + 1;
                size = size2;
            }
            int i3 = i;
            return 0;
        }
    }

    private void getApplicationsWifiTraffic(Parcel data) {
        int size = data.readInt();
        if (size <= 0) {
            LogUtil.e("No wifi traffic applications data");
            return;
        }
        int i = 0;
        while (i < size) {
            long startTime = data.readLong();
            long endTime = data.readLong();
            String name = data.readString();
            long fgTxBytes = data.readLong();
            long fgRxBytes = data.readLong();
            long fgEnergy = data.readLong();
            long bgTxBytes = data.readLong();
            int size2 = size;
            long bgRxBytes = data.readLong();
            long bgEnergy = data.readLong();
            long screenOnTxBytes = data.readLong();
            long screenOnRxBytes = data.readLong();
            long screenOnEnergy = data.readLong();
            long screenOffTxBytes = data.readLong();
            long screenOffRxBytes = data.readLong();
            long screenOffEnergy = data.readLong();
            int size3 = size2;
            StringBuilder sb = new StringBuilder();
            long screenOffEnergy2 = screenOffEnergy;
            sb.append("[WIFI-DATA] startTime=");
            sb.append(startTime);
            sb.append(", endTime=");
            sb.append(endTime);
            sb.append(", name=");
            sb.append(name);
            sb.append(", fgTxBytes=");
            sb.append(fgTxBytes);
            sb.append(", fgRxBytes=");
            sb.append(fgRxBytes);
            sb.append(", fgEnergy=");
            sb.append(fgEnergy);
            sb.append(", bgTxBytes=");
            sb.append(bgTxBytes);
            sb.append(", bgRxBytes=");
            long bgRxBytes2 = bgRxBytes;
            sb.append(bgRxBytes2);
            long j = bgRxBytes2;
            sb.append(", bgEnergy=");
            long bgEnergy2 = bgEnergy;
            sb.append(bgEnergy2);
            long j2 = bgEnergy2;
            sb.append(", screenOnTxBytes=");
            long screenOnTxBytes2 = screenOnTxBytes;
            sb.append(screenOnTxBytes2);
            long j3 = screenOnTxBytes2;
            sb.append(", screenOnRxBytes=");
            long screenOnRxBytes2 = screenOnRxBytes;
            sb.append(screenOnRxBytes2);
            long j4 = screenOnRxBytes2;
            sb.append(", screenOnEnergy=");
            long screenOnEnergy2 = screenOnEnergy;
            sb.append(screenOnEnergy2);
            long j5 = screenOnEnergy2;
            sb.append(", screenOffTxBytes=");
            long screenOffTxBytes2 = screenOffTxBytes;
            sb.append(screenOffTxBytes2);
            long j6 = screenOffTxBytes2;
            sb.append(", screenOffRxBytes=");
            long screenOffRxBytes2 = screenOffRxBytes;
            sb.append(screenOffRxBytes2);
            long j7 = screenOffRxBytes2;
            sb.append(", screenOffEnergy=");
            sb.append(screenOffEnergy2);
            LogUtil.d(sb.toString());
            i++;
            size = size3;
        }
        int i2 = i;
    }

    private void handleWifiData(Parcel data) {
        if ((!data.readBoolean() || (getComponentWifiAp(data) >= 0 && getComponentWifiScan(data) >= 0 && getComponentWifiTraffic(data) >= 0)) && data.readBoolean() != 0 && getApplicationsWifiScan(data) >= 0) {
            getApplicationsWifiTraffic(data);
        }
    }
}
