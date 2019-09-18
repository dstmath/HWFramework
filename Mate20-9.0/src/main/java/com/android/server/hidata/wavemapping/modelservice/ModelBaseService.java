package com.android.server.hidata.wavemapping.modelservice;

import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.dao.IdentifyResultDAO;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class ModelBaseService {
    public static final int DISCRIMINATE_RET_CODE_ERROR1 = -21;
    public static final int DISCRIMINATE_RET_CODE_ERROR2 = -22;
    public static final int DISCRIMINATE_RET_CODE_ERROR3 = -23;
    public static final int DISCRIMINATE_RET_CODE_ERROR4 = -24;
    public static final int DISCRIMINATE_RET_CODE_ERROR5 = -25;
    public static final int DISCRIMINATE_RET_CODE_ERROR6 = -26;
    public static final int DISCRIMINATE_RET_CODE_ERROR7 = -27;
    public static final int DISCRIMINATE_RET_CODE_ERROR8 = -28;
    public static final int DISCRIMINATE_RET_CODE_ERROR9 = -29;
    public static final int DISCRIMINATE_RET_CODE_SUCCESS = 1;
    public static final int FILTERMOILEAP_RET_ERROR_CODE_57 = -57;
    public static final int FILTERMOILEAP_RET_ERROR_CODE_58 = -58;
    public static final int FILTERMOILEAP_RET_ERROR_CODE_59 = -59;
    public static final int FILTERMOILEAP_RET_ERROR_CODE_60 = -60;
    public static final int IDENTIFY_ERROR_CODE_1 = -1;
    public static final int IDENTIFY_ERROR_CODE_2 = -2;
    public static final int IDENTIFY_ERROR_CODE_3 = -3;
    public static final int IDENTIFY_ERROR_CODE_4 = -4;
    public static final int IDENTIFY_ERROR_CODE_5 = -5;
    public static final int IDENTIFY_ERROR_CODE_6 = -6;
    public static final int SUCCESS_RET_CODE = 1;
    public static final int TRAINMODEL_RET_ERROR_CODE_01 = -1;
    public static final int TRAINMODEL_RET_ERROR_CODE_02 = -2;
    public static final int TRAINMODEL_RET_ERROR_CODE_03 = -3;
    public static final int TRAINMODEL_RET_ERROR_CODE_04 = -4;
    public static final int TRAINMODEL_RET_ERROR_CODE_05 = -5;
    public static final int TRAINMODEL_RET_ERROR_CODE_06 = -6;
    public static final int TRAINMODEL_RET_ERROR_CODE_07 = -7;
    public static final int TRAINMODEL_RET_ERROR_CODE_08 = -8;
    public static final int TRAINMODEL_RET_ERROR_CODE_09 = -9;
    public static final int TRAINMODEL_RET_ERROR_CODE_10 = -10;
    public static final int TRAINMODEL_RET_ERROR_CODE_11 = -11;
    public static final int TRAINMODEL_RET_ERROR_CODE_12 = -12;
    public static final int TRAINMODEL_RET_ERROR_CODE_13 = -13;
    public static final int TRAINMODEL_RET_ERROR_CODE_14 = -14;
    public static final int TRAINMODEL_RET_ERROR_CODE_15 = -15;
    public static final int TRAINMODEL_RET_ERROR_CODE_16 = -16;
    public static final int TRAINMODEL_RET_ERROR_CODE_17 = -17;
    public static final int TRAINMODEL_RET_ERROR_CODE_18 = -18;
    public static final int TRAINMODEL_RET_ERROR_CODE_19 = -19;
    public static final int TRAINMODEL_RET_ERROR_CODE_20 = -20;
    public static final int TRAINMODEL_RET_ERROR_CODE_52 = -52;
    public static final int TRAINMODEL_RET_ERROR_CODE_53 = -53;
    public static final int TRAINMODEL_RET_ERROR_CODE_54 = -54;
    public static final int TRAINMODEL_RET_ERROR_CODE_55 = -55;
    public static final int TRAINMODEL_RET_ERROR_CODE_56 = -56;
    public static final int TRAINMODEL_RET_ERROR_CODE_61 = -61;
    public static final int TRAINMODEL_RET_ERROR_CODE_62 = -62;
    public static final int TRAINMODEL_RET_ERROR_CODE_63 = -63;
    public String UNKONW_IDENTIFY_RET = "unknown";

    public String getRawFilePath(String place, ParameterInfo param) {
        if (param.isMainAp()) {
            return Constant.getRawDataPath() + place + Constant.MAINAP_RAW_FILE_EXTENSION;
        }
        return Constant.getRawDataPath() + place + Constant.RAW_FILE_EXTENSION;
    }

    public String getStdFilePath(String place, ParameterInfo param) {
        if (param.isMainAp()) {
            return Constant.getDataPath() + place + Constant.MAINAP_STA_DATA_FILE_EXTENSION;
        }
        return Constant.getDataPath() + place + Constant.STA_DATA_FILE_EXTENSION;
    }

    public String getModelFilePath(RegularPlaceInfo placeInfo, ParameterInfo param) {
        String fName = placeInfo.getPlace().replace(":", "").replace("-", "") + "." + placeInfo.getModelName();
        if (param.isMainAp()) {
            return Constant.getModelPath() + fName + Constant.MAINAP_MODEL_FILE_EXTENSION;
        }
        return Constant.getModelPath() + fName + Constant.MODEL_FILE_EXTENSION;
    }

    public String getLogFilePath(String place, ParameterInfo param) {
        if (param.isMainAp()) {
            return Constant.getLogPath() + place + Constant.MAINAP_LOG_FILE_EXTENSION;
        }
        return Constant.getLogPath() + place + Constant.LOG_FILE_EXTENSION;
    }

    public String getIdentifyLogFilePath(String place, ParameterInfo param) {
        if (param.isMainAp()) {
            return Constant.getLogPath() + place + Constant.MAINAP_IDENTIFY_LOG_EXTENSION;
        }
        return Constant.getLogPath() + place + Constant.IDENTIFY_LOG_EXTENSION;
    }

    public String getTestDataFilePath(String place, ParameterInfo param) {
        if (param.isMainAp()) {
            return Constant.getRawDataPath() + place + Constant.MAINAP_TEST_DATA_FILE_EXTENSION;
        }
        return Constant.getRawDataPath() + place + Constant.TEST_DATA_FILE_EXTENSION;
    }

    public String getTrainDataFilePath(String place, ParameterInfo param) {
        if (param.isMainAp()) {
            return Constant.getRawDataPath() + place + Constant.MAINAP_TRAIN_DATA_FILE_EXTENSION;
        }
        return Constant.getRawDataPath() + place + Constant.TRAIN_DATA_FILE_EXTENSION;
    }

    public boolean checkMacFormat(String mac) {
        if (mac != null && !mac.equals("00:00:00:00:00:00") && mac.contains(":") && mac.split(":").length >= 6) {
            return true;
        }
        return false;
    }

    public RegularPlaceInfo updateModel(RegularPlaceInfo placeInfo, String place, ParameterInfo param) {
        if (place == null || place.equals("") || placeInfo == null) {
            return placeInfo;
        }
        LogUtil.d("AgingService,updateModel begin." + placeInfo.toString() + ",place:" + place);
        try {
            String rawDataFilePath = getRawFilePath(place, param);
            if (!FileUtils.delFile(rawDataFilePath)) {
                LogUtil.d(" updateModel  ,FileUtils.delFile(rawDataFilePath) failure,rawDataFilePath:" + rawDataFilePath);
            }
            RegularPlaceInfo regularPlaceInfo = new RegularPlaceInfo(place, 3, 1, 0, 0, 0, 0, "", param.isMainAp());
            placeInfo = regularPlaceInfo;
            if (!new IdentifyResultDAO().remove(place, param.isMainAp())) {
                LogUtil.d("updateModel identifyResultDAO.deleteAll failure.");
            }
        } catch (Exception e) {
            LogUtil.e("updateModel:" + e);
        }
        return placeInfo;
    }
}
