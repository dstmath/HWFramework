package com.android.server.hidata.wavemapping.chr;

import android.database.SQLException;
import android.util.IMonitor;
import com.android.server.hidata.wavemapping.chr.entity.BuildModelChrInfo;
import com.android.server.hidata.wavemapping.chr.entity.StgUsageChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.EnterpriseApDao;
import com.android.server.hidata.wavemapping.dao.MobileApDao;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.io.File;

public class BuildModelChrService {
    private static final float DEFAULT_BYTE_UNIT = 1024.0f;
    private static final String KEY_AP_TYPE = "apRec";
    private static final String KEY_BACKUP = "back";
    private static final String KEY_BATCH_ALL = "batchA";
    private static final String KEY_BATCH_CELL = "batchC";
    private static final String KEY_BATCH_MAIN = "batchM";
    private static final String KEY_CONFIG_VER_ALL = "cfgA";
    private static final String KEY_CONFIG_VER_CELL = "cfgC";
    private static final String KEY_CONFIG_VER_MAIN = "cfgM";
    private static final String KEY_DATA = "data";
    private static final String KEY_DB = "db";
    private static final String KEY_ENTERPRISE_AP = "enterpriseAp";
    private static final String KEY_FINAL_USED = "finalUsed";
    private static final String KEY_FINGER_ALL = "fingA";
    private static final String KEY_FINGER_CELL = "fingC";
    private static final String KEY_FINGER_MAIN = "fingM";
    private static final String KEY_FIRST_TIME_ALL = "firstA";
    private static final String KEY_FIRST_TIME_CELL = "firstC";
    private static final String KEY_FIRST_TIME_MAIN = "firstM";
    private static final String KEY_LABEL = "lab";
    private static final String KEY_LOC = "loc";
    private static final String KEY_LOG = "log";
    private static final String KEY_MAX_DIST_ALL = "mDistA";
    private static final String KEY_MAX_DIST_CELL = "mDistC";
    private static final String KEY_MAX_DIST_MAIN = "mDistM";
    private static final String KEY_MOBILE_AP_SRC1 = "mobileApSrc1";
    private static final String KEY_MOBILE_AP_SRC2 = "mobileApSrc2";
    private static final String KEY_MOBILE_AP_SRC3 = "ApSrc3";
    private static final String KEY_MODEL = "mod";
    private static final String KEY_MODEL_ALL = "modA";
    private static final String KEY_MODEL_CELL = "modC";
    private static final String KEY_MODEL_MAIN = "modM";
    private static final String KEY_RAW_DATA = "raw";
    private static final String KEY_REF = "ref";
    private static final String KEY_RET_ALL = "retA";
    private static final String KEY_RET_CELL = "retC";
    private static final String KEY_RET_MAIN = "retM";
    private static final String KEY_STG_USAGE = "stor";
    private static final String KEY_TEST_DATA_ALL = "testA";
    private static final String KEY_TEST_DATA_CELL = "testC";
    private static final String KEY_TEST_DATA_MAIN = "testM";
    private static final String KEY_TOTAL_FOUND = "totalFound";
    private static final String KEY_TRAIN_DATA_ALL = "trainA";
    private static final String KEY_TRAIN_DATA_CELL = "trainC";
    private static final String KEY_TRAIN_DATA_MAIN = "trainM";
    private static final String KEY_UPDATE = "update";
    private static final String KEY_UPDATE_ALL = "updA";
    private static final String KEY_UPDATE_CELL = "updC";
    private static final String KEY_UPDATE_MAIN = "updM";

    public StgUsageChrInfo getStgUsageChrInfo() {
        try {
            return new StgUsageChrInfo(((float) FileUtils.getDirSize(new File(Constant.getDbPath()))) / DEFAULT_BYTE_UNIT, ((float) FileUtils.getDirSize(new File(Constant.getRawDataPath()))) / DEFAULT_BYTE_UNIT, ((float) FileUtils.getDirSize(new File(Constant.getModelPath()))) / DEFAULT_BYTE_UNIT, ((float) FileUtils.getDirSize(new File(Constant.getLogPath()))) / DEFAULT_BYTE_UNIT, ((float) FileUtils.getDirSize(new File(Constant.getDataPath()))) / DEFAULT_BYTE_UNIT, ((float) FileUtils.getDirSize(new File(Constant.getRootPath()))) / DEFAULT_BYTE_UNIT);
        } catch (SecurityException e) {
            LogUtil.e(false, "getStgUsageChrInfo failed by Exception", new Object[0]);
            return null;
        }
    }

    public void setApTableChrStatInfo(BuildModelChrInfo buildModelChrInfo) {
        if (buildModelChrInfo != null) {
            try {
                EnterpriseApDao enterpriseApDao = new EnterpriseApDao();
                MobileApDao mobileApDao = new MobileApDao();
                buildModelChrInfo.setTrainDataMain(enterpriseApDao.findAllCount());
                buildModelChrInfo.setTestDataMain(mobileApDao.findAllCountBySrcType(1));
                buildModelChrInfo.setUpdateMain(mobileApDao.findAllCountBySrcType(2));
            } catch (SQLException e) {
                LogUtil.e(false, "setApTableChrStatInfo failed by Exception", new Object[0]);
            }
        }
    }

    public void buildModelChrInfo(ParameterInfo param, int trainRet, RegularPlaceInfo placeInfo, BuildModelChrInfo buildModelChrInfo, int discriminateRet) {
        if (buildModelChrInfo != null && param != null && placeInfo != null) {
            try {
                ModelBaseService modelBaseService = new ModelBaseService();
                buildModelChrInfo.setRetAll(trainRet);
                buildModelChrInfo.setRetMain(discriminateRet);
                buildModelChrInfo.setMaxDistAll(param.getMaxDist());
                buildModelChrInfo.setModelAll(placeInfo.getModelName());
                buildModelChrInfo.setMaxDistCell(((float) FileUtils.getDirSize(new File(modelBaseService.getModelFilePath(placeInfo, param)))) / DEFAULT_BYTE_UNIT);
                if (commitChr(buildModelChrInfo)) {
                    LogUtil.d(false, "buildModelChrInfo success,%{public}s", buildModelChrInfo.toString());
                    LogUtil.wtLogFile(TimeUtil.getTime() + ",buildModelChrInfo success," + buildModelChrInfo.toString() + Constant.getLineSeparator());
                    return;
                }
                LogUtil.d(false, "buildModelChrInfo failure,%{public}s", buildModelChrInfo.toString());
                LogUtil.wtLogFile(TimeUtil.getTime() + ",buildModelChrInfo failure," + buildModelChrInfo.toString() + Constant.getLineSeparator());
            } catch (SecurityException e) {
                LogUtil.e(false, "buildModelChrInfo failed by Exception", new Object[0]);
            }
        }
    }

    public void setBuildModelChrInfo(String place, ParameterInfo param, RegularPlaceInfo placeInfo, BuildModelChrInfo buildModelChrInfo, BuildModelChrService buildModelChrService) {
        if (buildModelChrInfo != null && param != null && placeInfo != null) {
            buildModelChrInfo.setLoc(place);
            buildModelChrInfo.setBatchAll(placeInfo.getBatch());
            buildModelChrInfo.setFingerAll(placeInfo.getFingerNum());
            buildModelChrInfo.setLabel(param.isMainAp());
            buildModelChrInfo.setConfigVerAll(param.getConfigVer());
            buildModelChrInfo.setStorage(getStgUsageChrInfo());
            setApTableChrStatInfo(buildModelChrInfo);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0059, code lost:
        if (r1 != null) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005c, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x003d, code lost:
        if (r1 != null) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x003f, code lost:
        android.util.IMonitor.closeEventStream(r1);
     */
    public boolean commitChr(BuildModelChrInfo buildModelChrInfo) {
        boolean isCommitted;
        IMonitor.EventStream buildModelEstream = null;
        IMonitor.EventStream apTypeEstream = null;
        IMonitor.EventStream stgUsageEstream = null;
        try {
            buildModelEstream = IMonitor.openEventStream((int) ChrConst.MSG_WAVEMAPPING_BUILDMODEL_EVENTID);
            apTypeEstream = IMonitor.openEventStream((int) ChrConst.MSG_WAVEMAPPING_APTYPE_CLASSID);
            stgUsageEstream = IMonitor.openEventStream((int) ChrConst.MSG_WAVEMAPPING_STGUSAGE_CLASSID);
            setBuildModelParam(buildModelEstream, buildModelChrInfo);
            setApTypeParam(apTypeEstream, buildModelChrInfo);
            setStgUsageParam(stgUsageEstream, buildModelChrInfo);
            buildModelEstream.setParam(KEY_AP_TYPE, apTypeEstream);
            buildModelEstream.setParam(KEY_STG_USAGE, stgUsageEstream);
            IMonitor.sendEvent(buildModelEstream);
            isCommitted = true;
            IMonitor.closeEventStream(buildModelEstream);
            if (stgUsageEstream != null) {
                IMonitor.closeEventStream(stgUsageEstream);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e2) {
            LogUtil.e(false, "commitBuildModelChr failed by Exception", new Object[0]);
            isCommitted = false;
            if (buildModelEstream != null) {
                IMonitor.closeEventStream(buildModelEstream);
            }
            if (stgUsageEstream != null) {
                IMonitor.closeEventStream(stgUsageEstream);
            }
        } catch (Throwable th) {
            if (buildModelEstream != null) {
                IMonitor.closeEventStream(buildModelEstream);
            }
            if (stgUsageEstream != null) {
                IMonitor.closeEventStream(stgUsageEstream);
            }
            if (apTypeEstream != null) {
                IMonitor.closeEventStream(apTypeEstream);
            }
            throw th;
        }
    }

    private void setBuildModelParam(IMonitor.EventStream buildModelEstream, BuildModelChrInfo buildModelChrInfo) {
        buildModelEstream.setParam("loc", buildModelChrInfo.getLoc());
        buildModelEstream.setParam(KEY_BATCH_ALL, buildModelChrInfo.getBatchAll());
        buildModelEstream.setParam(KEY_FINGER_ALL, buildModelChrInfo.getFingerAll());
        buildModelEstream.setParam(KEY_RET_ALL, buildModelChrInfo.getRetAll());
        buildModelEstream.setParam(KEY_TRAIN_DATA_ALL, buildModelChrInfo.getTrainDataAll());
        buildModelEstream.setParam(KEY_TEST_DATA_ALL, buildModelChrInfo.getTestDataAll());
        buildModelEstream.setParam(KEY_UPDATE_ALL, buildModelChrInfo.getUpdateAll());
        buildModelEstream.setParam(KEY_FIRST_TIME_ALL, buildModelChrInfo.getFirstTimeAll());
        buildModelEstream.setParam(KEY_MODEL_ALL, buildModelChrInfo.getModelAll());
        buildModelEstream.setParam(KEY_MAX_DIST_ALL, buildModelChrInfo.getMaxDistAll());
        buildModelEstream.setParam(KEY_CONFIG_VER_ALL, buildModelChrInfo.getConfigVerAll());
        buildModelEstream.setParam(KEY_BATCH_MAIN, buildModelChrInfo.getBatchMain());
        buildModelEstream.setParam(KEY_FINGER_MAIN, buildModelChrInfo.getFingerMain());
        buildModelEstream.setParam(KEY_RET_MAIN, buildModelChrInfo.getRetMain());
        buildModelEstream.setParam(KEY_TRAIN_DATA_MAIN, buildModelChrInfo.getTrainDataMain());
        buildModelEstream.setParam(KEY_TEST_DATA_MAIN, buildModelChrInfo.getTestDataMain());
        buildModelEstream.setParam(KEY_UPDATE_MAIN, buildModelChrInfo.getUpdateMain());
        buildModelEstream.setParam(KEY_FIRST_TIME_MAIN, buildModelChrInfo.getFirstTimeMain());
        buildModelEstream.setParam(KEY_MODEL_MAIN, buildModelChrInfo.getModelMain());
        buildModelEstream.setParam(KEY_MAX_DIST_MAIN, buildModelChrInfo.getMaxDistMain());
        buildModelEstream.setParam(KEY_CONFIG_VER_MAIN, buildModelChrInfo.getConfigVerMain());
        buildModelEstream.setParam(KEY_BATCH_CELL, buildModelChrInfo.getBatchCell());
        buildModelEstream.setParam(KEY_FINGER_CELL, buildModelChrInfo.getFingerCell());
        buildModelEstream.setParam(KEY_RET_CELL, buildModelChrInfo.getRetCell());
        buildModelEstream.setParam(KEY_TRAIN_DATA_CELL, buildModelChrInfo.getTrainDataCell());
        buildModelEstream.setParam(KEY_TEST_DATA_CELL, buildModelChrInfo.getTestDataCell());
        buildModelEstream.setParam(KEY_UPDATE_CELL, buildModelChrInfo.getUpdateCell());
        buildModelEstream.setParam(KEY_FIRST_TIME_CELL, buildModelChrInfo.getFirstTimeCell());
        buildModelEstream.setParam(KEY_MODEL_CELL, buildModelChrInfo.getModelCell());
        buildModelEstream.setParam(KEY_MAX_DIST_CELL, buildModelChrInfo.getMaxDistCell());
        buildModelEstream.setParam(KEY_CONFIG_VER_CELL, buildModelChrInfo.getConfigVerCell());
        buildModelEstream.setParam(KEY_LABEL, buildModelChrInfo.getLabel());
        buildModelEstream.setParam(KEY_REF, buildModelChrInfo.getRef());
    }

    private void setApTypeParam(IMonitor.EventStream apTypeEstream, BuildModelChrInfo buildModelChrInfo) {
        apTypeEstream.setParam(KEY_ENTERPRISE_AP, buildModelChrInfo.getApType().getEnterpriseAp());
        apTypeEstream.setParam(KEY_MOBILE_AP_SRC1, buildModelChrInfo.getApType().getMobileApSrc1());
        apTypeEstream.setParam(KEY_MOBILE_AP_SRC2, buildModelChrInfo.getApType().getMobileApSrc2());
        apTypeEstream.setParam(KEY_MOBILE_AP_SRC3, buildModelChrInfo.getApType().getMobileApSrc3());
        apTypeEstream.setParam(KEY_UPDATE, buildModelChrInfo.getApType().getUpdate());
        apTypeEstream.setParam(KEY_TOTAL_FOUND, buildModelChrInfo.getApType().getTotalFound());
        apTypeEstream.setParam(KEY_FINAL_USED, buildModelChrInfo.getApType().getFinalUsed());
    }

    private void setStgUsageParam(IMonitor.EventStream stgUsageEstream, BuildModelChrInfo buildModelChrInfo) {
        stgUsageEstream.setParam(KEY_DB, buildModelChrInfo.getStorage().getDbSize());
        stgUsageEstream.setParam(KEY_RAW_DATA, buildModelChrInfo.getStorage().getRawDataSize());
        stgUsageEstream.setParam("mod", buildModelChrInfo.getStorage().getModelSize());
        stgUsageEstream.setParam(KEY_LOG, buildModelChrInfo.getStorage().getLogSize());
        stgUsageEstream.setParam(KEY_DATA, buildModelChrInfo.getStorage().getDataSize());
        stgUsageEstream.setParam(KEY_BACKUP, buildModelChrInfo.getStorage().getBackupSize());
    }
}
