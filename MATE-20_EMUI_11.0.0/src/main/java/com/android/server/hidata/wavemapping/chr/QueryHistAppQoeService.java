package com.android.server.hidata.wavemapping.chr;

import com.android.server.hidata.wavemapping.chr.entity.HistAppQoeChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.SpaceUserDao;
import com.android.server.hidata.wavemapping.util.LogUtil;
import java.util.HashMap;

public class QueryHistAppQoeService {
    private static final int DEFAULT_CAPACITY = 16;
    private static QueryHistAppQoeService mService = null;
    private HistAppQoeChrInfo currQoeInfo = new HistAppQoeChrInfo();
    private SpaceUserDao mSpaceUserDao = new SpaceUserDao();
    private HashMap<Integer, HistAppQoeChrInfo> savedQoeRecords = new HashMap<>(16);

    public static QueryHistAppQoeService getInstance() {
        if (mService == null) {
            mService = new QueryHistAppQoeService();
        }
        return mService;
    }

    public void setSpaceInfoOfAll(int spaceIdOfAll, int modelVerOfAll) {
        this.currQoeInfo.setSpaceInfoOfAll((short) spaceIdOfAll, modelVerOfAll);
    }

    public void setSpaceInfoOfMain(int spaceIdOfMain, int modelVerOfMain) {
        this.currQoeInfo.setSpaceInfoOfMain((short) spaceIdOfMain, modelVerOfMain);
    }

    public void setSpaceInfoOfCell(int spaceIdOfCell, int modelVerOfCell) {
        this.currQoeInfo.setSpaceInfoOfCell((short) spaceIdOfCell, modelVerOfCell);
    }

    public void setNetInfo(int netId, String name, int freq, int type) {
        this.currQoeInfo.setNetInfo(netId, name, (short) freq, (byte) type);
    }

    public void setTimeRecords(int days, int dur) {
        this.currQoeInfo.setTimeRecords((short) days, dur);
    }

    public void setQualityRecords(int good, int poor) {
        this.currQoeInfo.setQualityRecords(good, poor);
    }

    public void setDataRecords(int rx, int tx) {
        this.currQoeInfo.setDataRecords(rx, tx);
    }

    public void saveRecordByApp(int appName) {
        this.savedQoeRecords.put(Integer.valueOf(appName), this.currQoeInfo);
        LogUtil.i(false, "saveRecordByApp:%{public}s", this.currQoeInfo.toString());
    }

    public void resetRecordByApp(int appName) {
        if (this.savedQoeRecords.containsKey(Integer.valueOf(appName))) {
            this.savedQoeRecords.remove(Integer.valueOf(appName));
        }
    }

    public HistAppQoeChrInfo queryRecordByApp(int appType, int appId, int scenesId) {
        int appName;
        if (appType == 2000) {
            appName = Constant.transferGameId2FullId(appId, scenesId);
        } else {
            appName = scenesId;
        }
        HistAppQoeChrInfo result = this.savedQoeRecords.get(Integer.valueOf(appName));
        if (result != null) {
            LogUtil.d(false, "queryRecordByApp:%{public}s", result.toString());
        }
        return result;
    }
}
