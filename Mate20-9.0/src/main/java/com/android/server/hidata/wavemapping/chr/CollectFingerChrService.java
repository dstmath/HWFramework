package com.android.server.hidata.wavemapping.chr;

import android.util.IMonitor;
import com.android.server.hidata.wavemapping.chr.entity.CollectFingerChrInfo;
import com.android.server.hidata.wavemapping.dao.RegularPlaceDAO;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class CollectFingerChrService {
    public IMonitor.EventStream getCollectFingerChrEventStreamByPlace(String place) {
        IMonitor.EventStream estream = null;
        if (place == null || place.equals("")) {
            return null;
        }
        try {
            estream = getCollectFingerChrEventStream(getCollectFingerChrInfo(new RegularPlaceDAO(), place));
        } catch (Exception e) {
            LogUtil.e("getCollectFingerChrEventStreamByPlace,e:" + e.getMessage());
        }
        return estream;
    }

    private CollectFingerChrInfo getCollectFingerChrInfo(RegularPlaceDAO regularPlaceDAO, String place) {
        CollectFingerChrInfo collectFingerChrInfo = new CollectFingerChrInfo();
        if (regularPlaceDAO == null || place == null || place.equals("")) {
            return collectFingerChrInfo;
        }
        try {
            RegularPlaceInfo allApPlaceInfo = regularPlaceDAO.findAllBySsid(place, false);
            RegularPlaceInfo mainApPlaceInfo = regularPlaceDAO.findAllBySsid(place, true);
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
            LogUtil.e("getCollectFingerChrInfo,e:" + e.getMessage());
        }
        return collectFingerChrInfo;
    }

    public IMonitor.EventStream getCollectFingerChrEventStream(CollectFingerChrInfo collectFingerChrInfo) {
        if (collectFingerChrInfo == null) {
            return null;
        }
        IMonitor.EventStream estream = IMonitor.openEventStream(909009054);
        try {
            estream.setParam("batchAll", collectFingerChrInfo.getBatchAll());
            estream.setParam("fingersPassiveAll", collectFingerChrInfo.getFingersPassiveAll());
            estream.setParam("fingerActiveAll", collectFingerChrInfo.getFingerActiveAll());
            estream.setParam("updateAll", collectFingerChrInfo.getUpdateAll());
            estream.setParam("batchMain", collectFingerChrInfo.getBatchMain());
            estream.setParam("fingersMain", collectFingerChrInfo.getFingersMain());
            estream.setParam("updateMain", collectFingerChrInfo.getUpdateMain());
            estream.setParam("batchCell", collectFingerChrInfo.getBatchCell());
            estream.setParam("fingersCell", collectFingerChrInfo.getFingersCell());
            estream.setParam("updateCell", collectFingerChrInfo.getUpdateCell());
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("getCollectFingerChrEventStream,e:" + e.getMessage());
        }
        return estream;
    }
}
