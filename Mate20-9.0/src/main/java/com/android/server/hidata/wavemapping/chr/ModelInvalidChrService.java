package com.android.server.hidata.wavemapping.chr;

import android.util.IMonitor;
import com.android.server.hidata.wavemapping.chr.entity.ModelInvalidChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.IdentifyResultDAO;
import com.android.server.hidata.wavemapping.entity.IdentifyResult;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.List;

public class ModelInvalidChrService {
    public boolean commitModelInvalidChrInfo(RegularPlaceInfo placeInfo, String place, ParameterInfo param, byte checkAgNeedRet) {
        if (placeInfo == null || place == null || "".equals(place) || param == null) {
            return false;
        }
        ModelInvalidChrInfo modelInvalidChrInfo = new ModelInvalidChrInfo();
        TimeUtil timeUtil = new TimeUtil();
        IdentifyResultDAO identifyResultDAO = new IdentifyResultDAO();
        modelInvalidChrInfo.setIdentifyAll(identifyResultDAO.findAllCount());
        List<IdentifyResult> identifyResultList = identifyResultDAO.findBySsid(place, param.isMainAp());
        modelInvalidChrInfo.setLoc(place);
        modelInvalidChrInfo.setUpdateAll(timeUtil.getTimeIntPATTERN02());
        modelInvalidChrInfo.setLabel(param.isMainAp());
        modelInvalidChrInfo.setModelAll(placeInfo.getModelName());
        modelInvalidChrInfo.setIsPassAll(checkAgNeedRet);
        modelInvalidChrInfo.setModelCell(param.getConfig_ver());
        int size = identifyResultList.size();
        int unknownCnt = 0;
        int knownCnt = 0;
        for (int i = 0; i < size; i++) {
            if (identifyResultList.get(i).getPreLabel() > 0) {
                knownCnt++;
            } else {
                unknownCnt++;
            }
        }
        modelInvalidChrInfo.setIdentifyMain(size);
        modelInvalidChrInfo.setIdentifyCell(knownCnt);
        modelInvalidChrInfo.setUpdatetCell(unknownCnt);
        if (commitChr(modelInvalidChrInfo)) {
            LogUtil.d("commitModelInvalidChrInfo success," + modelInvalidChrInfo.toString());
            FileUtils.writeFile(Constant.getLogPath() + Constant.LOG_FILE, TimeUtil.getTime() + ",commitModelInvalidChrInfo success," + modelInvalidChrInfo.toString() + Constant.getLineSeperate());
        } else {
            LogUtil.d("commitModelInvalidChrInfo failure," + modelInvalidChrInfo.toString());
            FileUtils.writeFile(Constant.getLogPath() + Constant.LOG_FILE, TimeUtil.getTime() + ",commitModelInvalidChrInfo failure," + modelInvalidChrInfo.toString() + Constant.getLineSeperate());
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x00c7, code lost:
        if (r0 == null) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x00ca, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x00a2, code lost:
        if (r0 != null) goto L_0x00a4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x00a4, code lost:
        android.util.IMonitor.closeEventStream(r0);
     */
    public boolean commitChr(ModelInvalidChrInfo modelInvalidChrInfo) {
        boolean ret;
        IMonitor.EventStream modelInvalidChrEstream = null;
        if (modelInvalidChrInfo == null) {
            return false;
        }
        try {
            modelInvalidChrEstream = IMonitor.openEventStream(CHRConst.MSG_WAVEMAPPING_MODELINVALID_EVENTID);
            modelInvalidChrEstream.setParam(BuildBenefitStatisticsChrInfo.E909002049_LOCATION_TINYINT, modelInvalidChrInfo.getLoc());
            modelInvalidChrEstream.setParam("recogA", modelInvalidChrInfo.getIdentifyAll());
            modelInvalidChrEstream.setParam("passA", modelInvalidChrInfo.getIsPassAll());
            modelInvalidChrEstream.setParam("updA", modelInvalidChrInfo.getUpdateAll());
            modelInvalidChrEstream.setParam("modA", modelInvalidChrInfo.getModelAll());
            modelInvalidChrEstream.setParam("recogM", modelInvalidChrInfo.getIdentifyMain());
            modelInvalidChrEstream.setParam("passM", modelInvalidChrInfo.getIsPassMain());
            modelInvalidChrEstream.setParam("updM", modelInvalidChrInfo.getUpdatetMain());
            modelInvalidChrEstream.setParam("modM", modelInvalidChrInfo.getModelMain());
            modelInvalidChrEstream.setParam("recogC", modelInvalidChrInfo.getIdentifyCell());
            modelInvalidChrEstream.setParam("passC", modelInvalidChrInfo.getIsPassCell());
            modelInvalidChrEstream.setParam("updC", modelInvalidChrInfo.getUpdatetCell());
            modelInvalidChrEstream.setParam("modC", modelInvalidChrInfo.getModelCell());
            modelInvalidChrEstream.setParam("lab", modelInvalidChrInfo.getLabel());
            modelInvalidChrEstream.setParam("ref", modelInvalidChrInfo.getRef());
            IMonitor.sendEvent(modelInvalidChrEstream);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e("commitBuildModelChr,e:" + e.getMessage());
            ret = false;
        } catch (Throwable th) {
            if (modelInvalidChrEstream != null) {
                IMonitor.closeEventStream(modelInvalidChrEstream);
            }
            throw th;
        }
    }
}
