package com.android.server.hidata.wavemapping.chr;

import android.text.TextUtils;
import android.util.IMonitor;
import com.android.server.hidata.wavemapping.chr.entity.ModelInvalidChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.IdentifyResultDao;
import com.android.server.hidata.wavemapping.entity.IdentifyResult;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.List;

public class ModelInvalidChrService {
    private static final String KEY_LABEL = "lab";
    private static final String KEY_LOC = "loc";
    private static final String KEY_MODEL_ALL = "modA";
    private static final String KEY_MODEL_CELL = "modC";
    private static final String KEY_MODEL_MAIN = "modM";
    private static final String KEY_PASS_ALL = "passA";
    private static final String KEY_PASS_CELL = "passC";
    private static final String KEY_PASS_MAIN = "passM";
    private static final String KEY_RECOG_ALL = "recogA";
    private static final String KEY_RECOG_CELL = "recogC";
    private static final String KEY_RECOG_MAIN = "recogM";
    private static final String KEY_REF = "ref";
    private static final String KEY_UPDATE_ALL = "updA";
    private static final String KEY_UPDATE_CELL = "updC";
    private static final String KEY_UPDATE_MAIN = "updM";

    public boolean commitModelInvalidChrInfo(RegularPlaceInfo placeInfo, String place, ParameterInfo param, byte checkAgNeedRet) {
        if (placeInfo != null && !TextUtils.isEmpty(place)) {
            if (param != null) {
                ModelInvalidChrInfo modelInvalidChrInfo = new ModelInvalidChrInfo();
                TimeUtil timeUtil = new TimeUtil();
                IdentifyResultDao identifyResultDao = new IdentifyResultDao();
                modelInvalidChrInfo.setIdentifyAll(identifyResultDao.findAllCount());
                List<IdentifyResult> identifyResultList = identifyResultDao.findBySsid(place, param.isMainAp());
                modelInvalidChrInfo.setLoc(place);
                modelInvalidChrInfo.setUpdateAll(timeUtil.getTimeIntPattern02());
                modelInvalidChrInfo.setLabel(param.isMainAp());
                modelInvalidChrInfo.setModelAll(placeInfo.getModelName());
                modelInvalidChrInfo.setIsPassAll(checkAgNeedRet);
                modelInvalidChrInfo.setModelCell(param.getConfigVer());
                int size = identifyResultList.size();
                int unknownCnt = 0;
                int knownCnt = 0;
                for (IdentifyResult identifyResult : identifyResultList) {
                    if (identifyResult.getPreLabel() > 0) {
                        knownCnt++;
                    } else {
                        unknownCnt++;
                    }
                }
                modelInvalidChrInfo.setIdentifyMain(size);
                modelInvalidChrInfo.setIdentifyCell(knownCnt);
                modelInvalidChrInfo.setUpdateCell(unknownCnt);
                if (commitChr(modelInvalidChrInfo)) {
                    LogUtil.d(false, "commitModelInvalidChrInfo success,%{public}s", modelInvalidChrInfo.toString());
                    FileUtils.writeFile(Constant.getLogPath() + Constant.LOG_FILE, TimeUtil.getTime() + ",commitModelInvalidChrInfo success," + modelInvalidChrInfo.toString() + Constant.getLineSeparator());
                } else {
                    LogUtil.d(false, "commitModelInvalidChrInfo failure,%{public}s", modelInvalidChrInfo.toString());
                    FileUtils.writeFile(Constant.getLogPath() + Constant.LOG_FILE, TimeUtil.getTime() + ",commitModelInvalidChrInfo failure," + modelInvalidChrInfo.toString() + Constant.getLineSeparator());
                }
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x00ac, code lost:
        if (r0 == null) goto L_0x00af;
     */
    public boolean commitChr(ModelInvalidChrInfo modelInvalidChrInfo) {
        boolean isCommitted;
        IMonitor.EventStream modelInvalidChrEstream = null;
        if (modelInvalidChrInfo == null) {
            return false;
        }
        try {
            modelInvalidChrEstream = IMonitor.openEventStream((int) ChrConst.MSG_WAVEMAPPING_MODELINVALID_EVENTID);
            modelInvalidChrEstream.setParam("loc", modelInvalidChrInfo.getLoc());
            modelInvalidChrEstream.setParam(KEY_RECOG_ALL, modelInvalidChrInfo.getIdentifyAll());
            modelInvalidChrEstream.setParam(KEY_PASS_ALL, modelInvalidChrInfo.getIsPassAll());
            modelInvalidChrEstream.setParam(KEY_UPDATE_ALL, modelInvalidChrInfo.getUpdateAll());
            modelInvalidChrEstream.setParam(KEY_MODEL_ALL, modelInvalidChrInfo.getModelAll());
            modelInvalidChrEstream.setParam(KEY_RECOG_MAIN, modelInvalidChrInfo.getIdentifyMain());
            modelInvalidChrEstream.setParam(KEY_PASS_MAIN, modelInvalidChrInfo.getIsPassMain());
            modelInvalidChrEstream.setParam(KEY_UPDATE_MAIN, modelInvalidChrInfo.getUpdateMain());
            modelInvalidChrEstream.setParam(KEY_MODEL_MAIN, modelInvalidChrInfo.getModelMain());
            modelInvalidChrEstream.setParam(KEY_RECOG_CELL, modelInvalidChrInfo.getIdentifyCell());
            modelInvalidChrEstream.setParam(KEY_PASS_CELL, modelInvalidChrInfo.getIsPassCell());
            modelInvalidChrEstream.setParam(KEY_UPDATE_CELL, modelInvalidChrInfo.getUpdateCell());
            modelInvalidChrEstream.setParam(KEY_MODEL_CELL, modelInvalidChrInfo.getModelCell());
            modelInvalidChrEstream.setParam(KEY_LABEL, modelInvalidChrInfo.getLabel());
            modelInvalidChrEstream.setParam(KEY_REF, modelInvalidChrInfo.getRef());
            IMonitor.sendEvent(modelInvalidChrEstream);
            isCommitted = true;
        } catch (Exception e) {
            LogUtil.e(false, "commitBuildModelChr failed by Exception", new Object[0]);
            isCommitted = false;
        } catch (Throwable th) {
            if (modelInvalidChrEstream != null) {
                IMonitor.closeEventStream(modelInvalidChrEstream);
            }
            throw th;
        }
        IMonitor.closeEventStream(modelInvalidChrEstream);
        return isCommitted;
    }
}
