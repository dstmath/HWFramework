package com.android.server.hidata.wavemapping.chr;

import com.android.server.hidata.wavemapping.chr.entity.HistAppQoeChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.SpaceUserDAO;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.HashMap;

public class QueryHistAppQoeService {
    private static QueryHistAppQoeService mService = null;
    private HistAppQoeChrInfo currQoeInfo = new HistAppQoeChrInfo();
    private SpaceUserDAO mSpaceUserDAO = SpaceUserDAO.getInstance();
    private HashMap<Integer, HistAppQoeChrInfo> savedQoeRecords = new HashMap<>();

    public static QueryHistAppQoeService getInstance() {
        if (mService == null) {
            mService = new QueryHistAppQoeService();
        }
        return mService;
    }

    public void setSpaceInfo(int spaceId_a, int modelVer_a, int spaceId_m, int modelVer_m, int spaceId_c, int modelVer_c) {
        this.currQoeInfo.setSpaceInfo((short) spaceId_a, modelVer_a, (short) spaceId_m, modelVer_m, (short) spaceId_c, modelVer_c);
    }

    public void setNetInfo(int netId, String name, int freq, int type) {
        this.currQoeInfo.setNetInfo(netId, name, (short) freq, (byte) type);
    }

    public void setRecords(int days, int dur, int good, int poor, int rx, int tx) {
        this.currQoeInfo.setRecords((short) days, dur, good, poor, rx, tx);
    }

    public void saveRecordByApp(int appName) {
        this.savedQoeRecords.put(Integer.valueOf(appName), this.currQoeInfo);
        LogUtil.i("saveRecordByApp:" + this.currQoeInfo.toString());
    }

    public void resetRecordByApp(int appName) {
        if (this.savedQoeRecords.containsKey(Integer.valueOf(appName))) {
            this.savedQoeRecords.remove(Integer.valueOf(appName));
        }
    }

    public HistAppQoeChrInfo queryRecordByApp(int AppType, int AppId, int ScenceId) {
        int appName;
        if (2000 == AppType) {
            appName = Constant.transferGameId2FullId(AppId, ScenceId);
        } else {
            appName = ScenceId;
        }
        HistAppQoeChrInfo result = this.savedQoeRecords.get(Integer.valueOf(appName));
        if (result != null) {
            LogUtil.d("queryRecordByApp:" + result.toString());
        }
        return result;
    }
}
