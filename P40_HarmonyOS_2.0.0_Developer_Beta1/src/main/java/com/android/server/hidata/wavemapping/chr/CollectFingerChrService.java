package com.android.server.hidata.wavemapping.chr;

import android.text.TextUtils;
import android.util.IMonitor;
import com.android.server.hidata.wavemapping.chr.entity.CollectFingerChrInfo;
import com.android.server.hidata.wavemapping.dao.RegularPlaceDao;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class CollectFingerChrService {
    private static final String KEY_BATCH_ALL = "batchAll";
    private static final String KEY_BATCH_CELL = "batchCell";
    private static final String KEY_BATCH_MAIN = "batchMain";
    private static final String KEY_FINGER_ACTIVE_ALL = "fingerActiveAll";
    private static final String KEY_FINGER_CELL = "fingersCell";
    private static final String KEY_FINGER_MAIN = "fingersMain";
    private static final String KEY_FINGER_PASSIVE_ALL = "fingersPassiveAll";
    private static final String KEY_UPDATE_ALL = "updateAll";
    private static final String KEY_UPDATE_CELL = "updateCell";
    private static final String KEY_UPDATE_MAIN = "updateMain";

    public IMonitor.EventStream getCollectFingerChrEventStreamByPlace(String place) {
        if (TextUtils.isEmpty(place)) {
            return null;
        }
        try {
            return getCollectFingerChrEventStream(getCollectFingerChrInfo(new RegularPlaceDao(), place));
        } catch (Exception e) {
            LogUtil.e(false, "getCollectFingerChrEventStreamByPlace failed by Exception", new Object[0]);
            return null;
        }
    }

    private CollectFingerChrInfo getCollectFingerChrInfo(RegularPlaceDao regularPlaceDao, String place) {
        CollectFingerChrInfo collectFingerChrInfo = new CollectFingerChrInfo();
        if (regularPlaceDao == null || TextUtils.isEmpty(place)) {
            return collectFingerChrInfo;
        }
        try {
            RegularPlaceInfo allApPlaceInfo = regularPlaceDao.findAllBySsid(place, false);
            RegularPlaceInfo mainApPlaceInfo = regularPlaceDao.findAllBySsid(place, true);
            if (allApPlaceInfo != null) {
                collectFingerChrInfo.setBatchAll(allApPlaceInfo.getBatch());
                collectFingerChrInfo.setFingersPassiveAll(allApPlaceInfo.getFingerNum());
                collectFingerChrInfo.setUpdateAll(allApPlaceInfo.getState());
                collectFingerChrInfo.setFingersCell(allApPlaceInfo.getDisNum());
                collectFingerChrInfo.setUpdateCell(allApPlaceInfo.getBeginTime());
            }
            if (mainApPlaceInfo != null) {
                collectFingerChrInfo.setBatchMain(mainApPlaceInfo.getBatch());
                collectFingerChrInfo.setFingersMain(mainApPlaceInfo.getFingerNum());
                collectFingerChrInfo.setUpdateMain(mainApPlaceInfo.getState());
                collectFingerChrInfo.setBatchCell(mainApPlaceInfo.getDisNum());
            }
        } catch (Exception e) {
            LogUtil.e(false, "getCollectFingerChrInfo failed by Exception", new Object[0]);
        }
        return collectFingerChrInfo;
    }

    public IMonitor.EventStream getCollectFingerChrEventStream(CollectFingerChrInfo collectFingerChrInfo) {
        if (collectFingerChrInfo == null) {
            return null;
        }
        IMonitor.EventStream estream = IMonitor.openEventStream(909009054);
        try {
            estream.setParam(KEY_BATCH_ALL, collectFingerChrInfo.getBatchAll());
            estream.setParam(KEY_FINGER_PASSIVE_ALL, collectFingerChrInfo.getFingersPassiveAll());
            estream.setParam(KEY_FINGER_ACTIVE_ALL, collectFingerChrInfo.getFingerActiveAll());
            estream.setParam(KEY_UPDATE_ALL, collectFingerChrInfo.getUpdateAll());
            estream.setParam(KEY_BATCH_MAIN, collectFingerChrInfo.getBatchMain());
            estream.setParam(KEY_FINGER_MAIN, collectFingerChrInfo.getFingersMain());
            estream.setParam(KEY_UPDATE_MAIN, collectFingerChrInfo.getUpdateMain());
            estream.setParam(KEY_BATCH_CELL, collectFingerChrInfo.getBatchCell());
            estream.setParam(KEY_FINGER_CELL, collectFingerChrInfo.getFingersCell());
            estream.setParam(KEY_UPDATE_CELL, collectFingerChrInfo.getUpdateCell());
        } catch (Exception e) {
            LogUtil.e(false, "getCollectFingerChrEventStream failed by Exception", new Object[0]);
        }
        return estream;
    }
}
