package com.android.server.hidata.wavemapping.modelservice;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.wavemapping.chr.BuildModelChrService;
import com.android.server.hidata.wavemapping.chr.entity.ApChrStatInfo;
import com.android.server.hidata.wavemapping.chr.entity.BuildModelChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.IdentifyResultDAO;
import com.android.server.hidata.wavemapping.dao.RegularPlaceDAO;
import com.android.server.hidata.wavemapping.dataprovider.BehaviorReceiver;
import com.android.server.hidata.wavemapping.entity.ClusterResult;
import com.android.server.hidata.wavemapping.entity.CoreTrainData;
import com.android.server.hidata.wavemapping.entity.FingerInfo;
import com.android.server.hidata.wavemapping.entity.IdentifyResult;
import com.android.server.hidata.wavemapping.entity.ModelInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.entity.TMapList;
import com.android.server.hidata.wavemapping.entity.UiInfo;
import com.android.server.hidata.wavemapping.service.UiService;
import com.android.server.hidata.wavemapping.util.CheckTempertureUtil;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.ShowToast;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ModelService5 extends ModelBaseService {
    public static final String TAG = ("WMapping." + ModelService5.class.getSimpleName());
    private static ModelService5 modelService;
    /* access modifiers changed from: private */
    public long DELAY_HANDLER;
    private AgingService agingService;
    private CheckTempertureUtil checkTempertureUtil;
    private ClusterResult clusterResult;
    private HandlerThread handlerThread;
    private IdentifyResultDAO identifyResultDAO;
    private IdentifyService identifyService;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public Handler mMachineHandler;
    private ModelInfo mainApModelInfo;
    private ParameterInfo mainParameterInfo;
    private ModelInfo modelInfo;
    private ParameterInfo parameterInfo;
    private RegularPlaceDAO rgLocationDAO;
    private HashMap<String, RegularPlaceInfo> rgLocations;
    private TrainModelService trainModelService;
    private UiInfo uiInfo;

    private ModelService5(Handler handler) {
        this.modelInfo = null;
        this.mainApModelInfo = null;
        this.parameterInfo = null;
        this.mainParameterInfo = null;
        this.DELAY_HANDLER = HwArbitrationDEFS.DelayTimeMillisB;
        this.parameterInfo = ParamManager.getInstance().getParameterInfo();
        this.mainParameterInfo = ParamManager.getInstance().getMainApParameterInfo();
        this.uiInfo = UiService.getUiInfo();
        this.trainModelService = new TrainModelService();
        this.identifyService = new IdentifyService();
        this.rgLocationDAO = new RegularPlaceDAO();
        this.identifyResultDAO = new IdentifyResultDAO();
        this.agingService = new AgingService();
        this.mMachineHandler = handler;
        this.checkTempertureUtil = CheckTempertureUtil.getInstance();
    }

    public static ModelService5 getInstance(Handler handler) {
        if (modelService == null) {
            modelService = new ModelService5(handler);
            modelService.initControllerHandler();
        }
        return modelService;
    }

    public void initControllerHandler() {
        this.handlerThread = new HandlerThread("wave_mapping_trainmodel");
        this.handlerThread.start();
        this.mHandler = new Handler(this.handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (ModelService5.this.mMachineHandler == null) {
                            LogUtil.e("MSG_BUILD_MODEL null == mMachineHandler");
                            return;
                        } else if (ModelService5.this.isTempertureExceed()) {
                            LogUtil.e("MSG_BUILD_MODEL isTempertureExceed");
                            return;
                        } else {
                            Bundle bundle = msg.getData();
                            if (bundle == null) {
                                LogUtil.e("MSG_BUILD_MODEL null == bundle");
                                return;
                            }
                            String freqLoc = bundle.getString(Constant.NAME_FREQLACATION);
                            String mainApStatChrInfo = bundle.getString(Constant.MAIN_AP);
                            String allApStatChrInfo = bundle.getString(Constant.ALL_AP);
                            if (freqLoc == null) {
                                LogUtil.e("MSG_BUILD_MODEL null == freqLoc");
                                return;
                            } else if (allApStatChrInfo == null) {
                                LogUtil.e("MSG_BUILD_MODEL null == allApStatChrInfo");
                                return;
                            } else if (mainApStatChrInfo == null) {
                                LogUtil.e("MSG_BUILD_MODEL null == mainApStatChrInfo");
                                return;
                            } else {
                                ApChrStatInfo apChrStatInfoService = new ApChrStatInfo();
                                ApChrStatInfo allApStatChr = apChrStatInfoService.str2ApChrStatInfo(allApStatChrInfo);
                                ApChrStatInfo mainApStatChr = apChrStatInfoService.str2ApChrStatInfo(mainApStatChrInfo);
                                if (allApStatChr == null) {
                                    allApStatChr = new ApChrStatInfo();
                                }
                                if (mainApStatChr == null) {
                                    mainApStatChr = new ApChrStatInfo();
                                }
                                ModelService5.this.setClusterResult(ModelService5.this.startTraining(freqLoc, allApStatChr, mainApStatChr));
                                ModelService5.this.mMachineHandler.sendEmptyMessage(81);
                                return;
                            }
                        }
                    case 2:
                        if (ModelService5.this.mMachineHandler == null) {
                            LogUtil.e("MSG_FILTER_MOBILE_AP null == mMachineHandler");
                            return;
                        }
                        Bundle bundle2 = msg.getData();
                        if (bundle2 == null) {
                            LogUtil.e("MSG_FILTER_MOBILE_AP null == bundle");
                            return;
                        }
                        String freqLoc2 = bundle2.getString(Constant.NAME_FREQLACATION);
                        if (freqLoc2 == null) {
                            LogUtil.e("MSG_FILTER_MOBILE_AP null == freqLoc");
                            return;
                        }
                        Map<String, String> apChrStatInfoMp = ModelService5.this.filterMobileAp(freqLoc2);
                        if (apChrStatInfoMp == null) {
                            LogUtil.e("MSG_FILTER_MOBILE_AP null == apChrStatInfoMp");
                            return;
                        }
                        String mainApStatChrInfo2 = apChrStatInfoMp.get(Constant.MAIN_AP);
                        if (mainApStatChrInfo2 == null) {
                            LogUtil.e("MSG_FILTER_MOBILE_AP null == mainApStatChrInfo");
                            return;
                        }
                        String allApStatChrInfo2 = apChrStatInfoMp.get(Constant.ALL_AP);
                        if (allApStatChrInfo2 == null) {
                            LogUtil.e("MSG_FILTER_MOBILE_AP null == allApStatChrInfo");
                            return;
                        }
                        bundle2.putString(Constant.MAIN_AP, mainApStatChrInfo2);
                        bundle2.putString(Constant.ALL_AP, allApStatChrInfo2);
                        Message buildModelMsg = Message.obtain(ModelService5.this.mHandler, 1);
                        buildModelMsg.setData(bundle2);
                        if (LogUtil.getDebug_flag()) {
                            ModelService5.this.mHandler.sendMessage(buildModelMsg);
                            return;
                        } else {
                            ModelService5.this.mHandler.sendMessageDelayed(buildModelMsg, ModelService5.this.DELAY_HANDLER);
                            return;
                        }
                    default:
                        return;
                }
            }
        };
    }

    public Handler getmHandler() {
        return this.mHandler;
    }

    public ClusterResult getClusterResult() {
        return this.clusterResult;
    }

    public void setClusterResult(ClusterResult clusterResult2) {
        this.clusterResult = clusterResult2;
    }

    public ModelInfo getMainApModelInfo() {
        return this.mainApModelInfo;
    }

    public void setMainApModelInfo(ModelInfo mainApModelInfo2) {
        this.mainApModelInfo = mainApModelInfo2;
    }

    public ParameterInfo getParameterInfo() {
        return this.parameterInfo;
    }

    public void setParameterInfo(ParameterInfo parameterInfo2) {
        this.parameterInfo = parameterInfo2;
    }

    public ParameterInfo getMainParameterInfo() {
        return this.mainParameterInfo;
    }

    public void setMainParameterInfo(ParameterInfo mainParameterInfo2) {
        this.mainParameterInfo = mainParameterInfo2;
    }

    public ModelInfo getModelInfo() {
        return this.modelInfo;
    }

    public void setModelInfo(ModelInfo modelInfo2) {
        this.modelInfo = modelInfo2;
    }

    public boolean loadCommModels(RegularPlaceInfo placeInfo) {
        if (placeInfo == null || placeInfo.getPlace() == null) {
            return false;
        }
        String place = placeInfo.getPlace().replace(":", "").replace("-", "");
        if (this.modelInfo == null || this.modelInfo.getModelName() == null || placeInfo.getModelName() == 0 || !this.modelInfo.getModelName().equals(Integer.toString(placeInfo.getModelName())) || !this.modelInfo.getPlace().equals(place)) {
            this.modelInfo = this.trainModelService.loadModel(this.parameterInfo, placeInfo);
        }
        if (this.modelInfo != null) {
            return true;
        }
        LogUtil.d("loadModels failure,null == this.modelInfo");
        return false;
    }

    public boolean loadMainApModel(RegularPlaceInfo placeInfo) {
        if (placeInfo == null || placeInfo.getPlace() == null) {
            return false;
        }
        String place = placeInfo.getPlace().replace(":", "").replace("-", "");
        if (this.mainApModelInfo == null || this.mainApModelInfo.getModelName() == null || placeInfo.getModelName() == 0 || !this.mainApModelInfo.getModelName().equals(Integer.toString(placeInfo.getModelName())) || !this.mainApModelInfo.getPlace().equals(place)) {
            this.mainApModelInfo = this.trainModelService.loadModel(this.mainParameterInfo, placeInfo);
        }
        if (this.mainApModelInfo != null) {
            return true;
        }
        LogUtil.d("loadModels failure,null == this.mainApModelInfo");
        return false;
    }

    public int trainModelRe(String place, ParameterInfo param, ApChrStatInfo apChrStatInfo, long beginTime) {
        int trainRet;
        int trainRet2;
        int trainRet3;
        int trainRet4;
        int trainRet5;
        CoreTrainData coreTrainData;
        ParameterInfo parameterInfo2;
        int i;
        int i2;
        int checkIsStop;
        int trainRet6;
        String str = place;
        ParameterInfo parameterInfo3 = param;
        int i3 = 0;
        if (str != null) {
            try {
                BuildModelChrInfo trainRet7 = i3;
                BuildModelChrInfo trainRet8 = i3;
                if (!str.equals("")) {
                    if (parameterInfo3 == null) {
                        LogUtil.d("trainModelRe,null == param,place:" + str);
                        return -1;
                    }
                    RegularPlaceInfo placeInfo = this.rgLocationDAO.findBySsid(str, param.isMainAp());
                    if (placeInfo == null) {
                        LogUtil.d("trainModelRe,null == placeInfo,place:" + str + ",isMainAp:" + param.isMainAp());
                        return -2;
                    } else if (placeInfo.getDisNum() > param.getAcumlteCount()) {
                        LogUtil.d("trainModelRe,placeInfo.getDisNum() > param.getAcumlteCount()");
                        return -3;
                    } else {
                        BuildModelChrInfo buildModelChrInfo = new BuildModelChrInfo();
                        buildModelChrInfo.setAPType(apChrStatInfo);
                        LogUtil.i("trainModelRe,begin,place:" + placeInfo.toString());
                        BuildModelChrService buildModelChrService = new BuildModelChrService();
                        buildModelChrService.setBuildModelChrInfo(str, parameterInfo3, placeInfo, buildModelChrInfo, buildModelChrService);
                        CoreTrainData coreTrainData2 = this.trainModelService.getWmpCoreTrainData(str, parameterInfo3, placeInfo, buildModelChrInfo);
                        BuildModelChrInfo buildModelChrInfo2 = buildModelChrInfo;
                        RegularPlaceInfo placeInfo2 = placeInfo;
                        BuildModelChrInfo buildModelChrInfo3 = buildModelChrInfo2;
                        BuildModelChrInfo buildModelChrInfo4 = buildModelChrInfo2;
                        String str2 = str;
                        try {
                            trainRet7 = buildModelChrInfo3;
                            trainRet8 = buildModelChrInfo3;
                            int checkIsStop2 = checkIsStop(parameterInfo3, beginTime, coreTrainData2.getResult(), placeInfo, buildModelChrInfo3, 0, buildModelChrService);
                            if (checkIsStop2 < 0) {
                                try {
                                    LogUtil.wtLogFile(TimeUtil.getTime() + ",place:" + str2 + ",model name:" + placeInfo2.getModelName() + ",isMainAp:" + String.valueOf(param.isMainAp()) + ",trainRet :" + 0 + ",disCriminateRet:" + 0 + Constant.getLineSeperate());
                                    return checkIsStop2;
                                } catch (RuntimeException e) {
                                    e = e;
                                    ParameterInfo parameterInfo4 = param;
                                    trainRet2 = 0;
                                    LogUtil.e("trainModelRe,e" + e.getMessage());
                                    trainRet = trainRet2;
                                    return trainRet;
                                } catch (Exception e2) {
                                    e = e2;
                                    ParameterInfo parameterInfo5 = param;
                                    trainRet3 = 0;
                                    LogUtil.e("trainModelRe,e" + e.getMessage());
                                    trainRet = trainRet3;
                                    return trainRet;
                                }
                            } else {
                                LogUtil.wtLogFile(TimeUtil.getTime() + ",place:" + str2 + ",model name:" + placeInfo2.getModelName() + ",isMainAp:" + String.valueOf(param.isMainAp()) + ",getWmpCoreTrainData result :" + coreTrainData2.getResult());
                                ParameterInfo parameterInfo6 = param;
                                int i4 = 0;
                                try {
                                    trainRet4 = i4;
                                    trainRet5 = i4;
                                    trainRet7 = i4;
                                    trainRet8 = i4;
                                    handleByCoreTrainDataResult(coreTrainData2, placeInfo2, str2, parameterInfo6);
                                    if (coreTrainData2.getDatas() == null || coreTrainData2.getDatas().length == 0) {
                                        parameterInfo2 = parameterInfo6;
                                        coreTrainData = coreTrainData2;
                                    } else if (coreTrainData2.getResult() <= 0) {
                                        parameterInfo2 = parameterInfo6;
                                        coreTrainData = coreTrainData2;
                                    } else {
                                        int maxLoop = 9;
                                        int trainRet9 = this.trainModelService.wmpCoreTrainData(coreTrainData2, str2, parameterInfo6, placeInfo2, buildModelChrInfo4);
                                        try {
                                            trainRet7 = i4;
                                            trainRet8 = i4;
                                            int disCriminateRet = discriminateModel(placeInfo2, parameterInfo6, trainRet9);
                                            int result = coreTrainData2.getResult();
                                            BuildModelChrInfo buildModelChrInfo5 = buildModelChrInfo4;
                                            CoreTrainData coreTrainData3 = coreTrainData2;
                                            int trainRet10 = trainRet9;
                                            try {
                                                trainRet7 = buildModelChrInfo5;
                                                trainRet8 = buildModelChrInfo5;
                                                int checkIsStop3 = checkIsStop(parameterInfo6, beginTime, result, placeInfo2, buildModelChrInfo5, disCriminateRet, buildModelChrService);
                                                if (checkIsStop3 < 0) {
                                                    LogUtil.wtLogFile(TimeUtil.getTime() + ",place:" + str2 + ",model name:" + placeInfo2.getModelName() + ",isMainAp:" + String.valueOf(param.isMainAp()) + ",trainRet :" + trainRet10 + ",disCriminateRet:" + disCriminateRet + Constant.getLineSeperate());
                                                    return checkIsStop3;
                                                }
                                                buildModelChrService.buildModelChrInfo(param, trainRet10, placeInfo2, buildModelChrInfo4, disCriminateRet);
                                                LogUtil.wtLogFile(TimeUtil.getTime() + ",place:" + str2 + ",model name:" + placeInfo2.getModelName() + ",isMainAp:" + String.valueOf(param.isMainAp()) + ",trainRet :" + trainRet10 + ",disCriminateRet:" + disCriminateRet + Constant.getLineSeperate());
                                                parameterInfo6 = param;
                                                try {
                                                    trainRet7 = checkIsStop3;
                                                    trainRet8 = checkIsStop3;
                                                    parameterInfo6.setMaxDistBak(param.getMaxDist());
                                                    int checkIsStop4 = checkIsStop3;
                                                    while (true) {
                                                        int i5 = trainRet10;
                                                        checkIsStop = checkIsStop4;
                                                        trainRet6 = i5;
                                                        if (disCriminateRet >= 0 || maxLoop <= 0) {
                                                            break;
                                                        }
                                                        try {
                                                            trainRet7 = trainRet6;
                                                            trainRet8 = trainRet6;
                                                            if (param.getMaxDist() <= param.getMaxDistMinLimit()) {
                                                                break;
                                                            }
                                                            maxLoop--;
                                                            parameterInfo6.setMaxDist(param.getMaxDist() * param.getMaxDistDecayRatio());
                                                            if (param.getMaxDist() < param.getMaxDistMinLimit()) {
                                                                trainRet4 = trainRet6;
                                                                trainRet5 = trainRet6;
                                                                parameterInfo6.setMaxDist(param.getMaxDistMinLimit());
                                                                trainRet4 = trainRet6;
                                                                trainRet5 = trainRet6;
                                                            }
                                                            int trainRet11 = this.trainModelService.wmpCoreTrainData(coreTrainData3, str2, parameterInfo6, placeInfo2, buildModelChrInfo4);
                                                            try {
                                                                disCriminateRet = discriminateModel(placeInfo2, parameterInfo6, trainRet11);
                                                                CoreTrainData coreTrainData4 = coreTrainData3;
                                                                CoreTrainData coreTrainData5 = coreTrainData4;
                                                                int i6 = checkIsStop;
                                                                trainRet10 = trainRet11;
                                                                int checkIsStop5 = checkIsStop(parameterInfo6, beginTime, coreTrainData4.getResult(), placeInfo2, buildModelChrInfo4, disCriminateRet, buildModelChrService);
                                                                if (checkIsStop5 < 0) {
                                                                    LogUtil.wtLogFile(TimeUtil.getTime() + ",place:" + str2 + ",model name:" + placeInfo2.getModelName() + ",isMainAp:" + String.valueOf(param.isMainAp()) + ",trainRet :" + trainRet10 + ",disCriminateRet:" + disCriminateRet + Constant.getLineSeperate());
                                                                    return checkIsStop5;
                                                                }
                                                                buildModelChrService.buildModelChrInfo(param, trainRet10, placeInfo2, buildModelChrInfo4, disCriminateRet);
                                                                LogUtil.wtLogFile(TimeUtil.getTime() + ",place:" + str2 + ",model name:" + placeInfo2.getModelName() + ",isMainAp:" + String.valueOf(param.isMainAp()) + ",trainRet :" + trainRet10 + ",disCriminateRet:" + disCriminateRet + Constant.getLineSeperate());
                                                                parameterInfo6 = param;
                                                                coreTrainData3 = coreTrainData5;
                                                                checkIsStop4 = checkIsStop5;
                                                            } catch (RuntimeException e3) {
                                                                e = e3;
                                                                i = trainRet11;
                                                                ParameterInfo parameterInfo7 = param;
                                                                trainRet2 = i;
                                                                LogUtil.e("trainModelRe,e" + e.getMessage());
                                                                trainRet = trainRet2;
                                                                return trainRet;
                                                            } catch (Exception e4) {
                                                                e = e4;
                                                                i2 = trainRet11;
                                                                ParameterInfo parameterInfo8 = param;
                                                                trainRet3 = i2;
                                                                LogUtil.e("trainModelRe,e" + e.getMessage());
                                                                trainRet = trainRet3;
                                                                return trainRet;
                                                            }
                                                        } catch (RuntimeException e5) {
                                                            e = e5;
                                                            ParameterInfo parameterInfo9 = param;
                                                            trainRet2 = trainRet6;
                                                            LogUtil.e("trainModelRe,e" + e.getMessage());
                                                            trainRet = trainRet2;
                                                            return trainRet;
                                                        } catch (Exception e6) {
                                                            e = e6;
                                                            ParameterInfo parameterInfo10 = param;
                                                            trainRet3 = trainRet6;
                                                            LogUtil.e("trainModelRe,e" + e.getMessage());
                                                            trainRet = trainRet3;
                                                            return trainRet;
                                                        }
                                                    }
                                                    int i7 = checkIsStop;
                                                    CoreTrainData coreTrainData6 = coreTrainData3;
                                                } catch (RuntimeException e7) {
                                                    e = e7;
                                                    ParameterInfo parameterInfo11 = parameterInfo6;
                                                    trainRet2 = trainRet10;
                                                    LogUtil.e("trainModelRe,e" + e.getMessage());
                                                    trainRet = trainRet2;
                                                    return trainRet;
                                                } catch (Exception e8) {
                                                    e = e8;
                                                    ParameterInfo parameterInfo12 = parameterInfo6;
                                                    trainRet3 = trainRet10;
                                                    LogUtil.e("trainModelRe,e" + e.getMessage());
                                                    trainRet = trainRet3;
                                                    return trainRet;
                                                }
                                                try {
                                                    trainRet7 = trainRet6;
                                                    trainRet8 = trainRet6;
                                                    param.setMaxDist(param.getMaxDistBak());
                                                    if (maxLoop <= 0) {
                                                        afterFailTrainModel(placeInfo2);
                                                        LogUtil.i("the last trainModel failure,maxLoop more than 9.");
                                                        return -4;
                                                    } else if (trainRet6 < param.getMinModelTypes()) {
                                                        afterFailTrainModel(placeInfo2);
                                                        LogUtil.i("the last trainModel failure,train model ret smaller than min model types.");
                                                        return -5;
                                                    } else if (disCriminateRet < 0) {
                                                        afterFailTrainModel(placeInfo2);
                                                        LogUtil.i("the last trainModel failure,disCriminate failure:" + disCriminateRet);
                                                        return -6;
                                                    } else {
                                                        if (param.isMainAp()) {
                                                            loadMainApModel(placeInfo2);
                                                        } else {
                                                            loadCommModels(placeInfo2);
                                                        }
                                                        ModelInfo model = param.isMainAp() ? this.mainApModelInfo : this.modelInfo;
                                                        if (!(model == null || model.getSetBssids() == null)) {
                                                            if (model.getSetBssids().size() != 0) {
                                                                placeInfo2.setDisNum(0);
                                                                placeInfo2.setTestDataNum(0);
                                                                placeInfo2.setIdentifyNum(0);
                                                                placeInfo2.setState(4);
                                                                model.getSetBssids().remove("prelabel");
                                                                placeInfo2.setNoOcurBssids(model.getSetBssids().toString().replace("[", "").replace(" ", "").replace("]", "").trim());
                                                                this.rgLocationDAO.update(placeInfo2);
                                                                LogUtil.i("trainModelRe,rgLocationDAO.update placeInfo:" + placeInfo2.toString());
                                                                trainRet = trainRet6;
                                                                return trainRet;
                                                            }
                                                        }
                                                        LogUtil.d("trainModelRe,null == model...");
                                                        return 0;
                                                    }
                                                } catch (RuntimeException e9) {
                                                    e = e9;
                                                    trainRet2 = trainRet7;
                                                    LogUtil.e("trainModelRe,e" + e.getMessage());
                                                    trainRet = trainRet2;
                                                    return trainRet;
                                                } catch (Exception e10) {
                                                    e = e10;
                                                    trainRet3 = trainRet8;
                                                    LogUtil.e("trainModelRe,e" + e.getMessage());
                                                    trainRet = trainRet3;
                                                    return trainRet;
                                                }
                                            } catch (RuntimeException e11) {
                                                e = e11;
                                                ParameterInfo parameterInfo13 = param;
                                                trainRet2 = trainRet10;
                                                LogUtil.e("trainModelRe,e" + e.getMessage());
                                                trainRet = trainRet2;
                                                return trainRet;
                                            } catch (Exception e12) {
                                                e = e12;
                                                ParameterInfo parameterInfo14 = param;
                                                trainRet3 = trainRet10;
                                                LogUtil.e("trainModelRe,e" + e.getMessage());
                                                trainRet = trainRet3;
                                                return trainRet;
                                            }
                                        } catch (RuntimeException e13) {
                                            e = e13;
                                            i = trainRet9;
                                            ParameterInfo parameterInfo15 = parameterInfo6;
                                            trainRet2 = i;
                                            LogUtil.e("trainModelRe,e" + e.getMessage());
                                            trainRet = trainRet2;
                                            return trainRet;
                                        } catch (Exception e14) {
                                            e = e14;
                                            i2 = trainRet9;
                                            ParameterInfo parameterInfo16 = parameterInfo6;
                                            trainRet3 = i2;
                                            LogUtil.e("trainModelRe,e" + e.getMessage());
                                            trainRet = trainRet3;
                                            return trainRet;
                                        }
                                    }
                                    LogUtil.d("trainModelRe coreTrainData == null");
                                    buildModelChrService.buildModelChrInfo(parameterInfo2, coreTrainData.getResult(), placeInfo2, buildModelChrInfo4, 0);
                                    return -19;
                                } catch (RuntimeException e15) {
                                    e = e15;
                                    ParameterInfo parameterInfo17 = parameterInfo6;
                                    trainRet2 = trainRet4;
                                    LogUtil.e("trainModelRe,e" + e.getMessage());
                                    trainRet = trainRet2;
                                    return trainRet;
                                } catch (Exception e16) {
                                    e = e16;
                                    ParameterInfo parameterInfo18 = parameterInfo6;
                                    trainRet3 = trainRet5;
                                    LogUtil.e("trainModelRe,e" + e.getMessage());
                                    trainRet = trainRet3;
                                    return trainRet;
                                }
                            }
                        } catch (RuntimeException e17) {
                            e = e17;
                            ParameterInfo parameterInfo19 = param;
                            trainRet2 = 0;
                            LogUtil.e("trainModelRe,e" + e.getMessage());
                            trainRet = trainRet2;
                            return trainRet;
                        } catch (Exception e18) {
                            e = e18;
                            ParameterInfo parameterInfo20 = param;
                            trainRet3 = 0;
                            LogUtil.e("trainModelRe,e" + e.getMessage());
                            trainRet = trainRet3;
                            return trainRet;
                        }
                    }
                }
            } catch (RuntimeException e19) {
                e = e19;
                String str3 = str;
                trainRet2 = i3;
                LogUtil.e("trainModelRe,e" + e.getMessage());
                trainRet = trainRet2;
                return trainRet;
            } catch (Exception e20) {
                e = e20;
                String str4 = str;
                trainRet3 = i3;
                LogUtil.e("trainModelRe,e" + e.getMessage());
                trainRet = trainRet3;
                return trainRet;
            }
        }
        String str5 = str;
        LogUtil.d("trainModelRe,null == place");
        return -1;
    }

    private int checkIsStop(ParameterInfo param, long beginTime, int result, RegularPlaceInfo placeInfo, BuildModelChrInfo buildModelChrInfo, int disCriminateRet, BuildModelChrService buildModelChrService) {
        long j = beginTime;
        BuildModelChrInfo buildModelChrInfo2 = buildModelChrInfo;
        if (buildModelChrService == null || buildModelChrInfo2 == null || param == null || placeInfo == null || this.checkTempertureUtil == null) {
            return -1;
        }
        long now = System.currentTimeMillis();
        int runTime = ((int) (now - j)) / 1000;
        buildModelChrInfo2.setFirstTimeMain(runTime);
        if (((long) runTime) > 600) {
            buildModelChrService.buildModelChrInfo(param, result, placeInfo, buildModelChrInfo2, disCriminateRet);
            new TimeUtil();
            LogUtil.d("trainModelRe,runTime > MAX_TRAINMODEL_TIME,runTime:" + runTime + ",MAX_TRAINMODEL_TIME:" + 600 + ",beginTime:" + timeUtil.timeStamp2DateInt(String.valueOf(beginTime)) + "," + j + ";now:" + timeUtil.timeStamp2DateInt(String.valueOf(now)) + "," + now);
            return -61;
        } else if (isTempertureExceed()) {
            LogUtil.d("trainModelRe,the temperature is too high.");
            return -62;
        } else if (BehaviorReceiver.isCharging()) {
            return 1;
        } else {
            LogUtil.d("trainModelRe,has stopped charging,so stop train model.");
            return -63;
        }
    }

    private void handleByCoreTrainDataResult(CoreTrainData coreTrainData, RegularPlaceInfo placeInfo, String place, ParameterInfo param) {
        if (coreTrainData != null) {
            int result = coreTrainData.getResult();
            if (result == -54 || result == -8) {
                updateModel(placeInfo, place, param);
            }
        }
    }

    private void afterFailTrainModel(RegularPlaceInfo placeInfo) {
        placeInfo.setState(3);
        placeInfo.setTestDataNum(0);
        placeInfo.setDisNum(placeInfo.getDisNum() + 1);
        this.rgLocationDAO.update(placeInfo);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01be, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01bf, code lost:
        r18 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01c3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01c4, code lost:
        r18 = r6;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01c3 A[Catch:{ RuntimeException -> 0x0317, Exception -> 0x0315 }, ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:39:0x00a8] */
    public int discriminateModel(RegularPlaceInfo placeInfo, ParameterInfo param, int trainRet) {
        int unknownCnt;
        int size;
        String fileContent;
        ParameterInfo parameterInfo2 = param;
        int i = trainRet;
        if (placeInfo == null || placeInfo.getPlace().equals("")) {
            return -28;
        }
        if (parameterInfo2 == null) {
            return -29;
        }
        if (i < param.getMinModelTypes()) {
            return -21;
        }
        if (param.isMainAp() && i >= param.getMinModelTypes()) {
            return 1;
        }
        String testDataFilePath = getTestDataFilePath(placeInfo.getPlace(), parameterInfo2);
        String fileContent2 = FileUtils.getFileContent(testDataFilePath);
        if (((long) fileContent2.length()) > Constant.MAX_FILE_SIZE) {
            LogUtil.d("discriminateModel ,file content is too bigger than max_file_size.");
            return -22;
        }
        try {
            String[] fileLines = fileContent2.split(Constant.getLineSeperate());
            FingerInfo fingerInfo = new FingerInfo();
            StringBuilder resultFileBd = new StringBuilder();
            HashMap<String, Integer> ssidDatas = new HashMap<>();
            int size2 = fileLines.length;
            loadCommModels(placeInfo);
            ModelInfo model = getModelByParam(parameterInfo2);
            if (model == null) {
                try {
                    LogUtil.e("discriminateModel failure,model == null ");
                    return -23;
                } catch (RuntimeException e) {
                    e = e;
                    String str = testDataFilePath;
                    String str2 = fileContent2;
                    LogUtil.e("discriminateModel,e" + e.getMessage());
                    return 1;
                } catch (Exception e2) {
                    e = e2;
                    String str3 = testDataFilePath;
                    String str4 = fileContent2;
                    LogUtil.e("discriminateModel,e" + e.getMessage());
                    return 1;
                }
            } else {
                TMapList tMapList = new TMapList();
                int count = 0;
                int count2 = 0;
                while (true) {
                    int i2 = count2;
                    if (i2 >= size2) {
                        break;
                    }
                    String[] wds = fileLines[i2].split(",");
                    String testDataFilePath2 = testDataFilePath;
                    try {
                        if (wds.length >= param.getScanWifiStart()) {
                            if (wds[0] == null) {
                                fileContent = fileContent2;
                                size = size2;
                            } else if (!wds[0].equals("")) {
                                int tempBatch = Integer.parseInt(wds[param.getBatchID()]);
                                ssidDatas.clear();
                                int tempSize = wds.length;
                                int k = param.getScanWifiStart();
                                while (true) {
                                    fileContent = fileContent2;
                                    int k2 = k;
                                    if (k2 >= tempSize) {
                                        break;
                                    }
                                    int tempSize2 = tempSize;
                                    try {
                                        String[] wds2 = wds;
                                        String[] tempScanWifiInfo = wds[k2].split(param.getWifiSeperate());
                                        int size3 = size2;
                                        if (tempScanWifiInfo.length >= 4) {
                                            String tempMac = tempScanWifiInfo[param.getScanMAC()];
                                            if (checkMacFormat(tempMac)) {
                                                String str5 = tempMac;
                                                int tempRssi = Integer.parseInt(tempScanWifiInfo[param.getScanRSSI()].split("\\.")[0]);
                                                if (tempRssi != 0) {
                                                    String[] strArr = tempScanWifiInfo;
                                                    ssidDatas.put(tempScanWifiInfo[param.getScanMAC()], Integer.valueOf(tempRssi));
                                                    k = k2 + 1;
                                                    fileContent2 = fileContent;
                                                    tempSize = tempSize2;
                                                    wds = wds2;
                                                    size2 = size3;
                                                }
                                            }
                                        }
                                        k = k2 + 1;
                                        fileContent2 = fileContent;
                                        tempSize = tempSize2;
                                        wds = wds2;
                                        size2 = size3;
                                    } catch (RuntimeException e3) {
                                        e = e3;
                                        LogUtil.e("discriminateModel,e" + e.getMessage());
                                        return 1;
                                    } catch (Exception e4) {
                                        e = e4;
                                        LogUtil.e("discriminateModel,e" + e.getMessage());
                                        return 1;
                                    }
                                }
                                int i3 = tempSize;
                                size = size2;
                                fingerInfo.setBissiddatas(ssidDatas);
                                int result = this.identifyService.indentifyLocation(placeInfo.getPlace(), fingerInfo, parameterInfo2, model);
                                resultFileBd.append(TimeUtil.getTime());
                                resultFileBd.append(",");
                                resultFileBd.append(placeInfo.getModelName());
                                resultFileBd.append(",");
                                resultFileBd.append(result);
                                resultFileBd.append(",");
                                resultFileBd.append(fileLines[i2]);
                                resultFileBd.append(Constant.getLineSeperate());
                                count++;
                                tMapList.add(Integer.valueOf(tempBatch), new IdentifyResult(tempBatch, result, 0));
                            }
                            count2 = i2 + 1;
                            testDataFilePath = testDataFilePath2;
                            fileContent2 = fileContent;
                            size2 = size;
                            int i4 = trainRet;
                        }
                        fileContent = fileContent2;
                        size = size2;
                    } catch (Exception e5) {
                        String[] strArr2 = wds;
                        fileContent = fileContent2;
                        size = size2;
                        LogUtil.e("discriminateModel,e" + e5.getMessage());
                    } catch (RuntimeException e6) {
                    }
                    count2 = i2 + 1;
                    testDataFilePath = testDataFilePath2;
                    fileContent2 = fileContent;
                    size2 = size;
                    int i42 = trainRet;
                }
                String str6 = fileContent2;
                int i5 = size2;
                if (count == 0) {
                    return -24;
                }
                int unknownCnt2 = 0;
                int batchCount = 0;
                int checkShatterRatioCount = 0;
                Set<Integer> setPreLabel = new HashSet<>();
                for (Map.Entry<Integer, List<IdentifyResult>> entry2 : tMapList.entrySet()) {
                    String[] fileLines2 = fileLines;
                    FingerInfo fingerInfo2 = fingerInfo;
                    List<IdentifyResult> tempIdentifyResultLst = entry2.getValue();
                    Iterator<IdentifyResult> it = tempIdentifyResultLst.iterator();
                    while (it.hasNext()) {
                        Iterator<IdentifyResult> it2 = it;
                        IdentifyResult tempIdentifyResult = it.next();
                        if (tempIdentifyResult.getPreLabel() < 0) {
                            unknownCnt++;
                        } else {
                            setPreLabel.add(Integer.valueOf(tempIdentifyResult.getPreLabel()));
                            unknownCnt = unknownCnt;
                        }
                        it = it2;
                    }
                    int unknownCnt3 = unknownCnt;
                    batchCount++;
                    if (checkShatterRatio(tempIdentifyResultLst, parameterInfo2)) {
                        checkShatterRatioCount++;
                    }
                    fileLines = fileLines2;
                    fingerInfo = fingerInfo2;
                    unknownCnt2 = unknownCnt3;
                }
                FingerInfo fingerInfo3 = fingerInfo;
                StringBuilder sb = new StringBuilder();
                sb.append(Constant.getTestResultPath());
                sb.append(placeInfo.getModelName());
                sb.append(".");
                HashMap<String, Integer> hashMap = ssidDatas;
                sb.append(String.valueOf(System.currentTimeMillis()));
                sb.append(Constant.DISCRI_LOG_FILE_EXTENSION);
                FileUtils.saveFile(sb.toString(), resultFileBd.toString());
                if (setPreLabel.size() <= 1) {
                    LogUtil.d("discriminateModel failure,prelabel size:" + setPreLabel.size() + ",prelabel:" + setPreLabel.toString());
                    return -25;
                } else if (((float) unknownCnt) / ((float) count) > param.getMinUnkwnRatio()) {
                    LogUtil.d("discriminateModel failure, unknownCnt:" + unknownCnt + ",count:" + count + ",tMinUnkwnRatio:" + param.getMinUnkwnRatio());
                    return -26;
                } else {
                    if (((float) checkShatterRatioCount) / ((float) batchCount) < param.getTotalShatterRatio()) {
                        LogUtil.d("discriminateModel failure, checkShatterRatioCount:" + checkShatterRatioCount + ",batchCount:" + batchCount + ",TotalShatterRatio:" + param.getTotalShatterRatio());
                        return -27;
                    }
                    return 1;
                }
            }
        } catch (RuntimeException e7) {
            e = e7;
            String str7 = testDataFilePath;
            String str8 = fileContent2;
            LogUtil.e("discriminateModel,e" + e.getMessage());
            return 1;
        } catch (Exception e8) {
            e = e8;
            String str9 = testDataFilePath;
            String str10 = fileContent2;
            LogUtil.e("discriminateModel,e" + e.getMessage());
            return 1;
        }
    }

    private boolean checkShatterRatio(List<IdentifyResult> identifyResults, ParameterInfo param) {
        if (identifyResults == null || identifyResults.size() == 0) {
            return false;
        }
        try {
            HashMap<Integer, AtomicInteger> stat = new HashMap<>();
            for (IdentifyResult identifyResult : identifyResults) {
                if (!stat.containsKey(Integer.valueOf(identifyResult.getPreLabel()))) {
                    stat.put(Integer.valueOf(identifyResult.getPreLabel()), new AtomicInteger(1));
                } else {
                    stat.get(Integer.valueOf(identifyResult.getPreLabel())).incrementAndGet();
                }
            }
            int maxCnt = 0;
            for (Map.Entry<Integer, AtomicInteger> entry : stat.entrySet()) {
                if (entry.getValue().intValue() > maxCnt) {
                    maxCnt = entry.getValue().intValue();
                }
            }
            if (((float) maxCnt) / ((float) identifyResults.size()) < param.getMaxShatterRatio()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            LogUtil.e("LocatingState,e" + e.getMessage());
        }
    }

    public Map<String, String> filterMobileAp(String place) {
        Map<String, String> apChrStatInfoMp = new HashMap<>();
        try {
            apChrStatInfoMp.put(Constant.ALL_AP, this.trainModelService.filterMobileAp(place, this.parameterInfo));
            apChrStatInfoMp.put(Constant.MAIN_AP, this.trainModelService.filterMobileAp(place, this.mainParameterInfo));
        } catch (Exception e) {
            LogUtil.e("filterMobileAp,place:" + place + ",e:" + e.getMessage());
        }
        return apChrStatInfoMp;
    }

    public ModelInfo getModelByParam(ParameterInfo param) {
        if (param.isMainAp()) {
            return this.mainApModelInfo;
        }
        return this.modelInfo;
    }

    public ClusterResult startTraining(String place, ApChrStatInfo allApChrStatInfo, ApChrStatInfo mainApChrStatInfo) {
        String str = place;
        ClusterResult clusterResult2 = new ClusterResult(str);
        if (str == null) {
            LogUtil.d("startTraining,place == null");
            return clusterResult2;
        }
        try {
            long beginTime = System.currentTimeMillis();
            int result = trainModelRe(str, this.parameterInfo, allApChrStatInfo, beginTime);
            LogUtil.i("startTraining, begin allAP trainModelRe ,result:" + result);
            clusterResult2.setCluster_num(result);
            if (result <= 0) {
                String toastInfo = "startTraining,train model failure, place :" + str;
                ShowToast.showToast(toastInfo);
                LogUtil.d(toastInfo);
            } else {
                String toastInfo2 = "startTraining,train model success, place :" + str + ",cluster count:" + result;
                ShowToast.showToast(toastInfo2);
                LogUtil.d(toastInfo2);
            }
            int mainApResult = trainModelRe(str, this.mainParameterInfo, mainApChrStatInfo, beginTime);
            clusterResult2.setMainAp_cluster_num(mainApResult);
            LogUtil.i("startTraining, begin mainAp trainModelRe ,result:" + mainApResult);
            if (mainApResult <= 0) {
                String toastInfo3 = "startTraining,train mainAp model failure, place :" + str;
                ShowToast.showToast(toastInfo3);
                LogUtil.d(toastInfo3);
            } else {
                String toastInfo4 = "startTraining,train mainAp model success, place :" + str + ",mainAp cluster count:" + mainApResult;
                ShowToast.showToast(toastInfo4);
                LogUtil.i(toastInfo4);
            }
            LogUtil.d("startTraining,result:" + clusterResult2.toString());
        } catch (Exception e) {
            LogUtil.e("startTraining,e" + e.getMessage());
        }
        return clusterResult2;
    }

    private boolean setNoCurBssids(FingerInfo fingerInfo, RegularPlaceInfo placeInfo, ParameterInfo param, ModelInfo model) {
        Set<String> curBssids = new HashSet<>();
        if (!(placeInfo == null || fingerInfo == null)) {
            try {
                if (fingerInfo.getBissiddatas() != null) {
                    if (fingerInfo.getBissiddatas().size() != 0) {
                        if (param != null) {
                            if (!param.isMainAp()) {
                                if (!(model == null || model.getBssidLst() == null)) {
                                    if (model.getBssidLst().length != 0) {
                                        curBssids.addAll(Arrays.asList(placeInfo.getNoOcurBssids().split(",")));
                                        for (String key : fingerInfo.getBissiddatas().keySet()) {
                                            if (curBssids.contains(key)) {
                                                curBssids.remove(key);
                                            }
                                        }
                                        placeInfo.setNoOcurBssids(curBssids.toString().replace("[", "").replace(" ", "").replace("]", "").trim());
                                        return true;
                                    }
                                }
                                return false;
                            }
                        }
                        return false;
                    }
                }
            } catch (Exception e) {
                LogUtil.e("LocatingState,e" + e.getMessage());
            }
        }
        return false;
    }

    public int indentifyLocation(String place, FingerInfo fingerInfo, ParameterInfo param) {
        if (place == null) {
            LogUtil.d("indentifyLocation failure,place == null");
            return -5;
        } else if (fingerInfo == null) {
            LogUtil.d("indentifyLocation failure,fingerInfo == null ");
            return -5;
        } else if (param == null) {
            LogUtil.d("indentifyLocation failure,param == null ");
            return -5;
        } else {
            ModelInfo model = getModelByParam(param);
            if (model == null) {
                LogUtil.d("indentifyLocation failure,model == null ");
                return -5;
            }
            int result = 0;
            try {
                result = this.identifyService.indentifyLocation(place, fingerInfo, param, model);
                IdentifyResult identifyResult = new IdentifyResult();
                identifyResult.setSsid(place);
                identifyResult.setPreLabel(result);
                identifyResult.setServeMac(fingerInfo.getServeMac());
                RegularPlaceInfo placeInfo = this.rgLocationDAO.findBySsid(place, param.isMainAp());
                if (placeInfo != null) {
                    identifyResult.setModelName(Integer.toString(placeInfo.getModelName()));
                    this.identifyResultDAO.insert(identifyResult, param.isMainAp());
                    if (placeInfo.getIdentifyNum() > param.getCheckAgingAcumlteCount()) {
                        LogUtil.d("begin agingAction,identifyNum:" + placeInfo.getIdentifyNum());
                        placeInfo = this.agingService.agingAction(placeInfo, place, param, model.getBssidLst(), this.mMachineHandler);
                    } else {
                        setNoCurBssids(fingerInfo, placeInfo, param, model);
                        placeInfo.setIdentifyNum(placeInfo.getIdentifyNum() + 1);
                    }
                    this.rgLocationDAO.update(placeInfo);
                }
                if (result == -1) {
                    return -1;
                }
                if (result == -2) {
                    return -2;
                }
                if (result == -3) {
                    return -3;
                }
                return result;
            } catch (Exception e) {
                LogUtil.e("LocatingState,e" + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isTempertureExceed() {
        if (this.checkTempertureUtil == null) {
            this.checkTempertureUtil = CheckTempertureUtil.getInstance();
        }
        if (this.checkTempertureUtil == null) {
            LogUtil.e("trainModels null == checkTempertureUtil");
            return true;
        } else if (!this.checkTempertureUtil.exceedMaxTemperture()) {
            return false;
        } else {
            LogUtil.d("trainModels exceedMaxTemperture == true");
            return true;
        }
    }
}
