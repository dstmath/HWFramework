package com.android.server.hidata.wavemapping.modelservice;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import com.android.server.hidata.wavemapping.chr.BuildModelChrService;
import com.android.server.hidata.wavemapping.chr.entity.ApChrStatInfo;
import com.android.server.hidata.wavemapping.chr.entity.BuildModelChrInfo;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.IdentifyResultDao;
import com.android.server.hidata.wavemapping.dao.RegularPlaceDao;
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
import com.android.server.hidata.wavemapping.util.CheckTemperatureUtil;
import com.android.server.hidata.wavemapping.util.FileUtils;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.ShowToast;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ModelService5 extends ModelBaseService {
    private static final int DEFAULT_CAPACITY = 16;
    private static final int DEFAULT_LENGTH = 4;
    private static final int DEFAULT_LOOP = 9;
    private static final long DELAY_HANDLER = 300000;
    public static final String TAG = ("WMapping." + ModelService5.class.getSimpleName());
    private static ModelService5 modelService;
    private AgingService agingService;
    private ClusterResult clusterResult;
    private HandlerThread handlerThread;
    private IdentifyResultDao identifyResultDao;
    private IdentifyService identifyService;
    private CheckTemperatureUtil mCheckTemperatureUtil;
    private Context mContext;
    private Handler mHandler;
    private Handler mMachineHandler;
    private long mTrainBeginTime;
    private ModelInfo mainApModelInfo;
    private ParameterInfo mainParameterInfo;
    private ModelInfo modelInfo;
    private ParameterInfo parameterInfo;
    private RegularPlaceDao rgLocationDao;
    private HashMap<String, RegularPlaceInfo> rgLocations;
    private TrainModelService trainModelService;
    private UiInfo uiInfo;

    private ModelService5(Context context, Handler handler) {
        this.modelInfo = null;
        this.mainApModelInfo = null;
        this.parameterInfo = null;
        this.mainParameterInfo = null;
        this.mTrainBeginTime = 0;
        this.mCheckTemperatureUtil = null;
        this.mContext = null;
        this.parameterInfo = ParamManager.getInstance().getParameterInfo();
        this.mainParameterInfo = ParamManager.getInstance().getMainApParameterInfo();
        this.uiInfo = UiService.getUiInfo();
        this.trainModelService = new TrainModelService();
        this.identifyService = new IdentifyService();
        this.rgLocationDao = new RegularPlaceDao();
        this.identifyResultDao = new IdentifyResultDao();
        this.agingService = new AgingService();
        this.mMachineHandler = handler;
        this.mCheckTemperatureUtil = CheckTemperatureUtil.getInstance(handler);
        this.mContext = context;
    }

    public static synchronized ModelService5 getInstance(Context context, Handler handler) {
        ModelService5 modelService5;
        synchronized (ModelService5.class) {
            if (modelService == null) {
                modelService = new ModelService5(context, handler);
                modelService.initControllerHandler();
            }
            modelService5 = modelService;
        }
        return modelService5;
    }

    public void initControllerHandler() {
        this.handlerThread = new HandlerThread("wave_mapping_trainmodel");
        this.handlerThread.start();
        this.mHandler = new Handler(this.handlerThread.getLooper()) {
            /* class com.android.server.hidata.wavemapping.modelservice.ModelService5.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    ModelService5.this.processBuildModel(msg);
                } else if (i == 2) {
                    ModelService5.this.processMobileAp(msg);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processMobileAp(Message msg) {
        if (this.mMachineHandler == null) {
            LogUtil.e(false, "MSG_FILTER_MOBILE_AP mMachineHandler == null", new Object[0]);
            return;
        }
        Bundle bundle = msg.getData();
        if (bundle == null) {
            LogUtil.e(false, "MSG_FILTER_MOBILE_AP bundle == null", new Object[0]);
            return;
        }
        String freqLoc = bundle.getString(Constant.NAME_FREQLACATION);
        if (freqLoc == null) {
            LogUtil.e(false, "MSG_FILTER_MOBILE_AP freqLoc == null", new Object[0]);
            return;
        }
        Map<String, String> apChrStatInfoMp = filterMobileAp(freqLoc);
        if (apChrStatInfoMp == null) {
            LogUtil.e(false, "MSG_FILTER_MOBILE_AP apChrStatInfoMp == null", new Object[0]);
            return;
        }
        String mainApStatChrInfo = apChrStatInfoMp.get(Constant.MAIN_AP);
        if (mainApStatChrInfo == null) {
            LogUtil.e(false, "MSG_FILTER_MOBILE_AP mainApStatChrInfo == null", new Object[0]);
            return;
        }
        String allApStatChrInfo = apChrStatInfoMp.get(Constant.ALL_AP);
        if (allApStatChrInfo == null) {
            LogUtil.e(false, "MSG_FILTER_MOBILE_AP allApStatChrInfo == null", new Object[0]);
            return;
        }
        bundle.putString(Constant.MAIN_AP, mainApStatChrInfo);
        bundle.putString(Constant.ALL_AP, allApStatChrInfo);
        Message buildModelMsg = Message.obtain(this.mHandler, 1);
        buildModelMsg.setData(bundle);
        if (LogUtil.getDebugFlag()) {
            this.mHandler.sendMessage(buildModelMsg);
        } else {
            this.mHandler.sendMessageDelayed(buildModelMsg, 300000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processBuildModel(Message msg) {
        if (this.mMachineHandler == null) {
            LogUtil.e(false, "MSG_BUILD_MODEL mMachineHandler == null", new Object[0]);
            return;
        }
        CheckTemperatureUtil checkTemperatureUtil = this.mCheckTemperatureUtil;
        if ((checkTemperatureUtil == null || !checkTemperatureUtil.isExceedMaxTemperature()) && isCharging()) {
            Bundle bundle = msg.getData();
            if (bundle == null) {
                LogUtil.e(false, "MSG_BUILD_MODEL bundle == null", new Object[0]);
                return;
            }
            String freqLoc = bundle.getString(Constant.NAME_FREQLACATION);
            String mainApStatChrInfo = bundle.getString(Constant.MAIN_AP);
            String allApStatChrInfo = bundle.getString(Constant.ALL_AP);
            if (freqLoc == null) {
                LogUtil.e(false, "MSG_BUILD_MODEL freqLoc == null", new Object[0]);
            } else if (allApStatChrInfo == null) {
                LogUtil.e(false, "MSG_BUILD_MODEL allApStatChrInfo == null", new Object[0]);
            } else if (mainApStatChrInfo == null) {
                LogUtil.e(false, "MSG_BUILD_MODEL mainApStatChrInfo == null", new Object[0]);
            } else {
                ApChrStatInfo apChrStatInfoService = new ApChrStatInfo();
                setClusterResult(startTraining(freqLoc, apChrStatInfoService.str2ApChrStatInfo(allApStatChrInfo), apChrStatInfoService.str2ApChrStatInfo(mainApStatChrInfo)));
                this.mMachineHandler.sendEmptyMessage(81);
            }
        } else {
            LogUtil.e(false, "MSG_BUILD_MODEL Stop", new Object[0]);
        }
    }

    public Handler getHandler() {
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
        String place = placeInfo.getPlace().replace(AwarenessInnerConstants.COLON_KEY, "").replace(AwarenessInnerConstants.DASH_KEY, "");
        ModelInfo modelInfo2 = this.modelInfo;
        if (modelInfo2 == null || modelInfo2.getModelName() == null || placeInfo.getModelName() == 0 || !this.modelInfo.getModelName().equals(Integer.toString(placeInfo.getModelName())) || !this.modelInfo.getPlace().equals(place)) {
            this.modelInfo = this.trainModelService.loadModel(this.parameterInfo, placeInfo);
        }
        if (this.modelInfo != null) {
            return true;
        }
        LogUtil.d(false, "loadModels failure, this.modelInfo == null", new Object[0]);
        return false;
    }

    public boolean loadMainApModel(RegularPlaceInfo placeInfo) {
        if (placeInfo == null || placeInfo.getPlace() == null) {
            return false;
        }
        String place = placeInfo.getPlace().replace(AwarenessInnerConstants.COLON_KEY, "").replace(AwarenessInnerConstants.DASH_KEY, "");
        ModelInfo modelInfo2 = this.mainApModelInfo;
        if (modelInfo2 == null || modelInfo2.getModelName() == null || placeInfo.getModelName() == 0 || !this.mainApModelInfo.getModelName().equals(Integer.toString(placeInfo.getModelName())) || !this.mainApModelInfo.getPlace().equals(place)) {
            this.mainApModelInfo = this.trainModelService.loadModel(this.mainParameterInfo, placeInfo);
        }
        if (this.mainApModelInfo != null) {
            return true;
        }
        LogUtil.d(false, "loadModels failure, this.mainApModelInfo == null", new Object[0]);
        return false;
    }

    /* JADX INFO: Multiple debug info for r2v34 'trainRet'  int: [D('trainRet' int), D('buildModelChrService' com.android.server.hidata.wavemapping.chr.BuildModelChrService)] */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x02f2: APUT  
      (r4v28 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x02ed: INVOKE  (r3v42 java.lang.Integer) = (r11v6 'trainRet' int A[D('discriminateRet' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0373: APUT  
      (r6v7 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x036e: INVOKE  (r5v10 java.lang.String) = 
      (r2v7 'placeInfo' com.android.server.hidata.wavemapping.entity.RegularPlaceInfo A[D('placeInfo' com.android.server.hidata.wavemapping.entity.RegularPlaceInfo)])
     type: VIRTUAL call: com.android.server.hidata.wavemapping.entity.RegularPlaceInfo.toString():java.lang.String)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x02d4, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x02d5, code lost:
        r7 = r2;
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x02da, code lost:
        r7 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x02f9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:119:0x02fa, code lost:
        r7 = r2;
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:137:0x037b, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:138:0x037c, code lost:
        r7 = r2;
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:143:0x038d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:144:0x038e, code lost:
        r7 = r2;
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:163:0x03d7, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x03d9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x03da, code lost:
        r7 = 0;
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x03de, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x03df, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x03ee, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x03ef, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x03f2, code lost:
        r7 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x014b, code lost:
        r7 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x02ae, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x02af, code lost:
        r7 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x02b4, code lost:
        r7 = r19;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x02d9 A[ExcHandler: Exception (e java.lang.Exception), PHI: r2 
      PHI: (r2v19 'trainRet' int) = (r2v23 'trainRet' int), (r2v23 'trainRet' int), (r2v23 'trainRet' int), (r2v23 'trainRet' int), (r2v23 'trainRet' int), (r2v23 'trainRet' int), (r2v23 'trainRet' int), (r2v23 'trainRet' int), (r2v34 'trainRet' int), (r2v34 'trainRet' int) binds: [B:140:0x0387, B:141:?, B:134:0x0373, B:135:?, B:115:0x02f2, B:116:?, B:104:0x02cd, B:105:?, B:76:0x022b, B:77:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:76:0x022b] */
    /* JADX WARNING: Removed duplicated region for block: B:176:0x03f1 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:2:0x0019] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x014a A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:41:0x013f] */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x02b3 A[ExcHandler: Exception (e java.lang.Exception), PHI: r19 
      PHI: (r19v2 'trainRet' int) = (r19v3 'trainRet' int), (r19v3 'trainRet' int), (r19v6 'trainRet' int), (r19v6 'trainRet' int) binds: [B:92:0x02a7, B:93:?, B:66:0x01ff, B:67:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:66:0x01ff] */
    public int trainModelRe(String place, ParameterInfo param, ApChrStatInfo apChrStatInfo) {
        int trainRet;
        boolean z;
        RuntimeException e;
        BuildModelChrService buildModelChrService;
        BuildModelChrInfo buildModelChrInfo;
        CoreTrainData coreTrainData;
        RegularPlaceInfo placeInfo;
        int trainRet2;
        int trainRet3;
        int trainRet4;
        if (place != null) {
            try {
                if (!"".equals(place)) {
                    if (param == null) {
                        LogUtil.d(false, "trainModelRe,param == null,place:%{private}s", place);
                        return -1;
                    }
                    RegularPlaceInfo placeInfo2 = this.rgLocationDao.findBySsid(place, param.isMainAp());
                    if (placeInfo2 == null) {
                        LogUtil.d(false, "trainModelRe,placeInfo == null,place:%{private}s,isMainAp:%{public}s", place, String.valueOf(param.isMainAp()));
                        return -2;
                    } else if (placeInfo2.getDisNum() > param.getAccumulateCount()) {
                        LogUtil.d(false, "trainModelRe,placeInfo.getDisNum() > param.getAccumulateCount()", new Object[0]);
                        return -3;
                    } else {
                        BuildModelChrInfo buildModelChrInfo2 = new BuildModelChrInfo();
                        buildModelChrInfo2.setApType(apChrStatInfo);
                        LogUtil.i(false, "trainModelRe,begin,place:%{private}s", placeInfo2.toString());
                        BuildModelChrService buildModelChrService2 = new BuildModelChrService();
                        buildModelChrService2.setBuildModelChrInfo(place, param, placeInfo2, buildModelChrInfo2, buildModelChrService2);
                        CoreTrainData coreTrainData2 = this.trainModelService.getWmpCoreTrainData(place, param, placeInfo2, buildModelChrInfo2);
                        int checkRunResult = checkTrainRunSatisfiedResult();
                        if (checkRunResult != 1) {
                            LogUtil.e(false, "trainModelRe,stop getWmpCoreTrainData", new Object[0]);
                            return checkRunResult;
                        }
                        LogUtil.wtLogFile(TimeUtil.getTime() + ",place:" + place + ",model name:" + placeInfo2.getModelName() + ",isMainAp:" + String.valueOf(param.isMainAp()) + ",getWmpCoreTrainData result :" + coreTrainData2.getResult());
                        handleByCoreTrainDataResult(coreTrainData2, placeInfo2, place, param);
                        if (coreTrainData2.getDatas() == null || coreTrainData2.getDatas().length == 0) {
                            buildModelChrService = buildModelChrService2;
                            coreTrainData = coreTrainData2;
                            buildModelChrInfo = buildModelChrInfo2;
                            placeInfo = placeInfo2;
                        } else if (coreTrainData2.getResult() <= 0) {
                            buildModelChrService = buildModelChrService2;
                            coreTrainData = coreTrainData2;
                            buildModelChrInfo = buildModelChrInfo2;
                            placeInfo = placeInfo2;
                        } else {
                            int maxLoop = 9;
                            int trainRet5 = this.trainModelService.wmpCoreTrainData(coreTrainData2, place, param, placeInfo2, buildModelChrInfo2);
                            try {
                                int discriminateRet = discriminateModel(placeInfo2, param, trainRet5);
                                int checkRunResult2 = checkTrainRunSatisfiedResult();
                                if (checkRunResult2 != 1) {
                                    try {
                                        LogUtil.e(false, "trainModelRe,stop discriminateModel", new Object[0]);
                                        return checkRunResult2;
                                    } catch (RuntimeException e2) {
                                        e = e2;
                                        z = false;
                                        trainRet = trainRet5;
                                        Object[] objArr = new Object[1];
                                        String message = e.getMessage();
                                        char c = z ? 1 : 0;
                                        char c2 = z ? 1 : 0;
                                        char c3 = z ? 1 : 0;
                                        char c4 = z ? 1 : 0;
                                        char c5 = z ? 1 : 0;
                                        char c6 = z ? 1 : 0;
                                        char c7 = z ? 1 : 0;
                                        char c8 = z ? 1 : 0;
                                        char c9 = z ? 1 : 0;
                                        char c10 = z ? 1 : 0;
                                        char c11 = z ? 1 : 0;
                                        char c12 = z ? 1 : 0;
                                        char c13 = z ? 1 : 0;
                                        char c14 = z ? 1 : 0;
                                        char c15 = z ? 1 : 0;
                                        char c16 = z ? 1 : 0;
                                        char c17 = z ? 1 : 0;
                                        char c18 = z ? 1 : 0;
                                        char c19 = z ? 1 : 0;
                                        char c20 = z ? 1 : 0;
                                        char c21 = z ? 1 : 0;
                                        objArr[c] = message;
                                        LogUtil.e(z, "trainModelRe,e %{public}s", objArr);
                                        return trainRet;
                                    } catch (Exception e3) {
                                    }
                                } else {
                                    try {
                                        buildModelChrService2.buildModelChrInfo(param, trainRet5, placeInfo2, buildModelChrInfo2, discriminateRet);
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(TimeUtil.getTime());
                                        sb.append(",place:");
                                        sb.append(place);
                                        sb.append(",model name:");
                                        sb.append(placeInfo2.getModelName());
                                        sb.append(",isMainAp:");
                                        sb.append(String.valueOf(param.isMainAp()));
                                        String str = ",trainRet :";
                                        sb.append(str);
                                        sb.append(trainRet5);
                                        sb.append(",discriminateRet:");
                                        sb.append(discriminateRet);
                                        sb.append(Constant.getLineSeparator());
                                        LogUtil.wtLogFile(sb.toString());
                                        param.setMaxDistBak(param.getMaxDist());
                                        trainRet = trainRet5;
                                        int trainRet6 = discriminateRet;
                                        while (trainRet6 < 0 && maxLoop > 0) {
                                            try {
                                                if (param.getMaxDist() <= param.getMaxDistMinLimit()) {
                                                    break;
                                                }
                                                maxLoop--;
                                                param.setMaxDist(param.getMaxDist() * param.getMaxDistDecayRatio());
                                                if (param.getMaxDist() < param.getMaxDistMinLimit()) {
                                                    try {
                                                        param.setMaxDist(param.getMaxDistMinLimit());
                                                    } catch (RuntimeException e4) {
                                                        e = e4;
                                                        z = false;
                                                    } catch (Exception e5) {
                                                        LogUtil.e(false, "trainModelRe failed by Exception", new Object[0]);
                                                        return trainRet;
                                                    }
                                                }
                                                trainRet3 = trainRet;
                                                try {
                                                    trainRet4 = this.trainModelService.wmpCoreTrainData(coreTrainData2, place, param, placeInfo2, buildModelChrInfo2);
                                                } catch (RuntimeException e6) {
                                                    e = e6;
                                                    trainRet = trainRet3;
                                                    z = false;
                                                    Object[] objArr2 = new Object[1];
                                                    String message2 = e.getMessage();
                                                    char c22 = z ? 1 : 0;
                                                    char c23 = z ? 1 : 0;
                                                    char c32 = z ? 1 : 0;
                                                    char c42 = z ? 1 : 0;
                                                    char c52 = z ? 1 : 0;
                                                    char c62 = z ? 1 : 0;
                                                    char c72 = z ? 1 : 0;
                                                    char c82 = z ? 1 : 0;
                                                    char c92 = z ? 1 : 0;
                                                    char c102 = z ? 1 : 0;
                                                    char c112 = z ? 1 : 0;
                                                    char c122 = z ? 1 : 0;
                                                    char c132 = z ? 1 : 0;
                                                    char c142 = z ? 1 : 0;
                                                    char c152 = z ? 1 : 0;
                                                    char c162 = z ? 1 : 0;
                                                    char c172 = z ? 1 : 0;
                                                    char c182 = z ? 1 : 0;
                                                    char c192 = z ? 1 : 0;
                                                    char c202 = z ? 1 : 0;
                                                    char c212 = z ? 1 : 0;
                                                    objArr2[c22] = message2;
                                                    LogUtil.e(z, "trainModelRe,e %{public}s", objArr2);
                                                    return trainRet;
                                                } catch (Exception e7) {
                                                }
                                                try {
                                                    int discriminateRet2 = discriminateModel(placeInfo2, param, trainRet4);
                                                    int checkRunResult3 = checkTrainRunSatisfiedResult();
                                                    if (checkRunResult3 != 1) {
                                                        LogUtil.e(false, "trainModelRe,stop discriminateModel the loop", new Object[0]);
                                                        return checkRunResult3;
                                                    }
                                                    trainRet2 = trainRet4;
                                                    try {
                                                        buildModelChrService2.buildModelChrInfo(param, trainRet4, placeInfo2, buildModelChrInfo2, discriminateRet2);
                                                        LogUtil.wtLogFile(TimeUtil.getTime() + ",place:" + place + ",model name:" + placeInfo2.getModelName() + ",isMainAp:" + String.valueOf(param.isMainAp()) + str + trainRet2 + ",discriminateRet:" + discriminateRet2 + Constant.getLineSeparator());
                                                        trainRet = trainRet2;
                                                        str = str;
                                                        buildModelChrService2 = buildModelChrService2;
                                                        trainRet6 = discriminateRet2;
                                                    } catch (RuntimeException e8) {
                                                        e = e8;
                                                        trainRet = trainRet2;
                                                        z = false;
                                                        Object[] objArr22 = new Object[1];
                                                        String message22 = e.getMessage();
                                                        char c222 = z ? 1 : 0;
                                                        char c232 = z ? 1 : 0;
                                                        char c322 = z ? 1 : 0;
                                                        char c422 = z ? 1 : 0;
                                                        char c522 = z ? 1 : 0;
                                                        char c622 = z ? 1 : 0;
                                                        char c722 = z ? 1 : 0;
                                                        char c822 = z ? 1 : 0;
                                                        char c922 = z ? 1 : 0;
                                                        char c1022 = z ? 1 : 0;
                                                        char c1122 = z ? 1 : 0;
                                                        char c1222 = z ? 1 : 0;
                                                        char c1322 = z ? 1 : 0;
                                                        char c1422 = z ? 1 : 0;
                                                        char c1522 = z ? 1 : 0;
                                                        char c1622 = z ? 1 : 0;
                                                        char c1722 = z ? 1 : 0;
                                                        char c1822 = z ? 1 : 0;
                                                        char c1922 = z ? 1 : 0;
                                                        char c2022 = z ? 1 : 0;
                                                        char c2122 = z ? 1 : 0;
                                                        objArr22[c222] = message22;
                                                        LogUtil.e(z, "trainModelRe,e %{public}s", objArr22);
                                                        return trainRet;
                                                    } catch (Exception e9) {
                                                    }
                                                } catch (RuntimeException e10) {
                                                    e = e10;
                                                    trainRet = trainRet4;
                                                    z = false;
                                                    Object[] objArr222 = new Object[1];
                                                    String message222 = e.getMessage();
                                                    char c2222 = z ? 1 : 0;
                                                    char c2322 = z ? 1 : 0;
                                                    char c3222 = z ? 1 : 0;
                                                    char c4222 = z ? 1 : 0;
                                                    char c5222 = z ? 1 : 0;
                                                    char c6222 = z ? 1 : 0;
                                                    char c7222 = z ? 1 : 0;
                                                    char c8222 = z ? 1 : 0;
                                                    char c9222 = z ? 1 : 0;
                                                    char c10222 = z ? 1 : 0;
                                                    char c11222 = z ? 1 : 0;
                                                    char c12222 = z ? 1 : 0;
                                                    char c13222 = z ? 1 : 0;
                                                    char c14222 = z ? 1 : 0;
                                                    char c15222 = z ? 1 : 0;
                                                    char c16222 = z ? 1 : 0;
                                                    char c17222 = z ? 1 : 0;
                                                    char c18222 = z ? 1 : 0;
                                                    char c19222 = z ? 1 : 0;
                                                    char c20222 = z ? 1 : 0;
                                                    char c21222 = z ? 1 : 0;
                                                    objArr222[c2222] = message222;
                                                    LogUtil.e(z, "trainModelRe,e %{public}s", objArr222);
                                                    return trainRet;
                                                } catch (Exception e11) {
                                                    trainRet = trainRet4;
                                                    LogUtil.e(false, "trainModelRe failed by Exception", new Object[0]);
                                                    return trainRet;
                                                }
                                            } catch (RuntimeException e12) {
                                                e = e12;
                                                z = false;
                                                Object[] objArr2222 = new Object[1];
                                                String message2222 = e.getMessage();
                                                char c22222 = z ? 1 : 0;
                                                char c23222 = z ? 1 : 0;
                                                char c32222 = z ? 1 : 0;
                                                char c42222 = z ? 1 : 0;
                                                char c52222 = z ? 1 : 0;
                                                char c62222 = z ? 1 : 0;
                                                char c72222 = z ? 1 : 0;
                                                char c82222 = z ? 1 : 0;
                                                char c92222 = z ? 1 : 0;
                                                char c102222 = z ? 1 : 0;
                                                char c112222 = z ? 1 : 0;
                                                char c122222 = z ? 1 : 0;
                                                char c132222 = z ? 1 : 0;
                                                char c142222 = z ? 1 : 0;
                                                char c152222 = z ? 1 : 0;
                                                char c162222 = z ? 1 : 0;
                                                char c172222 = z ? 1 : 0;
                                                char c182222 = z ? 1 : 0;
                                                char c192222 = z ? 1 : 0;
                                                char c202222 = z ? 1 : 0;
                                                char c212222 = z ? 1 : 0;
                                                objArr2222[c22222] = message2222;
                                                LogUtil.e(z, "trainModelRe,e %{public}s", objArr2222);
                                                return trainRet;
                                            } catch (Exception e13) {
                                                LogUtil.e(false, "trainModelRe failed by Exception", new Object[0]);
                                                return trainRet;
                                            }
                                        }
                                        trainRet3 = trainRet;
                                        try {
                                            param.setMaxDist(param.getMaxDistBak());
                                            if (maxLoop <= 0) {
                                                afterFailTrainModel(placeInfo2);
                                                z = false;
                                                LogUtil.i(false, "the last trainModel failure,maxLoop more than 9.", new Object[0]);
                                                return -4;
                                            }
                                            trainRet2 = trainRet3;
                                            if (trainRet2 < param.getMinModelTypes()) {
                                                afterFailTrainModel(placeInfo2);
                                                LogUtil.i(false, "the last trainModel failure,train model ret smaller than min model types.", new Object[0]);
                                                return -5;
                                            } else if (trainRet6 < 0) {
                                                afterFailTrainModel(placeInfo2);
                                                Object[] objArr3 = new Object[1];
                                                objArr3[0] = Integer.valueOf(trainRet6);
                                                LogUtil.i(false, "the last trainModel failure,disCriminate failure:%{public}d", objArr3);
                                                return -6;
                                            } else {
                                                if (param.isMainAp()) {
                                                    loadMainApModel(placeInfo2);
                                                } else {
                                                    loadCommModels(placeInfo2);
                                                }
                                                ModelInfo model = param.isMainAp() ? this.mainApModelInfo : this.modelInfo;
                                                if (model == null || model.getSetBssids() == null || model.getSetBssids().size() == 0) {
                                                    LogUtil.d(false, "trainModelRe, model == null", new Object[0]);
                                                    return 0;
                                                }
                                                placeInfo2.setDisNum(0);
                                                placeInfo2.setTestDataNum(0);
                                                placeInfo2.setIdentifyNum(0);
                                                placeInfo2.setState(4);
                                                model.getSetBssids().remove("prelabel");
                                                placeInfo2.setNoOcurBssids(model.getSetBssids().toString().replace("[", "").replace(" ", "").replace("]", "").trim());
                                                this.rgLocationDao.update(placeInfo2);
                                                Object[] objArr4 = new Object[1];
                                                objArr4[0] = placeInfo2.toString();
                                                LogUtil.i(false, "trainModelRe,rgLocationDao.update placeInfo:%{private}s", objArr4);
                                                return trainRet2;
                                            }
                                        } catch (RuntimeException e14) {
                                            e = e14;
                                            trainRet = trainRet3;
                                            z = false;
                                            Object[] objArr22222 = new Object[1];
                                            String message22222 = e.getMessage();
                                            char c222222 = z ? 1 : 0;
                                            char c232222 = z ? 1 : 0;
                                            char c322222 = z ? 1 : 0;
                                            char c422222 = z ? 1 : 0;
                                            char c522222 = z ? 1 : 0;
                                            char c622222 = z ? 1 : 0;
                                            char c722222 = z ? 1 : 0;
                                            char c822222 = z ? 1 : 0;
                                            char c922222 = z ? 1 : 0;
                                            char c1022222 = z ? 1 : 0;
                                            char c1122222 = z ? 1 : 0;
                                            char c1222222 = z ? 1 : 0;
                                            char c1322222 = z ? 1 : 0;
                                            char c1422222 = z ? 1 : 0;
                                            char c1522222 = z ? 1 : 0;
                                            char c1622222 = z ? 1 : 0;
                                            char c1722222 = z ? 1 : 0;
                                            char c1822222 = z ? 1 : 0;
                                            char c1922222 = z ? 1 : 0;
                                            char c2022222 = z ? 1 : 0;
                                            char c2122222 = z ? 1 : 0;
                                            objArr22222[c222222] = message22222;
                                            LogUtil.e(z, "trainModelRe,e %{public}s", objArr22222);
                                            return trainRet;
                                        } catch (Exception e15) {
                                            trainRet = trainRet3;
                                            LogUtil.e(false, "trainModelRe failed by Exception", new Object[0]);
                                            return trainRet;
                                        }
                                    } catch (RuntimeException e16) {
                                        e = e16;
                                        trainRet = trainRet5;
                                        z = false;
                                        Object[] objArr222222 = new Object[1];
                                        String message222222 = e.getMessage();
                                        char c2222222 = z ? 1 : 0;
                                        char c2322222 = z ? 1 : 0;
                                        char c3222222 = z ? 1 : 0;
                                        char c4222222 = z ? 1 : 0;
                                        char c5222222 = z ? 1 : 0;
                                        char c6222222 = z ? 1 : 0;
                                        char c7222222 = z ? 1 : 0;
                                        char c8222222 = z ? 1 : 0;
                                        char c9222222 = z ? 1 : 0;
                                        char c10222222 = z ? 1 : 0;
                                        char c11222222 = z ? 1 : 0;
                                        char c12222222 = z ? 1 : 0;
                                        char c13222222 = z ? 1 : 0;
                                        char c14222222 = z ? 1 : 0;
                                        char c15222222 = z ? 1 : 0;
                                        char c16222222 = z ? 1 : 0;
                                        char c17222222 = z ? 1 : 0;
                                        char c18222222 = z ? 1 : 0;
                                        char c19222222 = z ? 1 : 0;
                                        char c20222222 = z ? 1 : 0;
                                        char c21222222 = z ? 1 : 0;
                                        objArr222222[c2222222] = message222222;
                                        LogUtil.e(z, "trainModelRe,e %{public}s", objArr222222);
                                        return trainRet;
                                    } catch (Exception e17) {
                                        trainRet = trainRet5;
                                        LogUtil.e(false, "trainModelRe failed by Exception", new Object[0]);
                                        return trainRet;
                                    }
                                }
                            } catch (RuntimeException e18) {
                                e = e18;
                                trainRet = trainRet5;
                                z = false;
                                Object[] objArr2222222 = new Object[1];
                                String message2222222 = e.getMessage();
                                char c22222222 = z ? 1 : 0;
                                char c23222222 = z ? 1 : 0;
                                char c32222222 = z ? 1 : 0;
                                char c42222222 = z ? 1 : 0;
                                char c52222222 = z ? 1 : 0;
                                char c62222222 = z ? 1 : 0;
                                char c72222222 = z ? 1 : 0;
                                char c82222222 = z ? 1 : 0;
                                char c92222222 = z ? 1 : 0;
                                char c102222222 = z ? 1 : 0;
                                char c112222222 = z ? 1 : 0;
                                char c122222222 = z ? 1 : 0;
                                char c132222222 = z ? 1 : 0;
                                char c142222222 = z ? 1 : 0;
                                char c152222222 = z ? 1 : 0;
                                char c162222222 = z ? 1 : 0;
                                char c172222222 = z ? 1 : 0;
                                char c182222222 = z ? 1 : 0;
                                char c192222222 = z ? 1 : 0;
                                char c202222222 = z ? 1 : 0;
                                char c212222222 = z ? 1 : 0;
                                objArr2222222[c22222222] = message2222222;
                                LogUtil.e(z, "trainModelRe,e %{public}s", objArr2222222);
                                return trainRet;
                            } catch (Exception e19) {
                                trainRet = trainRet5;
                                LogUtil.e(false, "trainModelRe failed by Exception", new Object[0]);
                                return trainRet;
                            }
                        }
                        z = false;
                        LogUtil.d(false, "trainModelRe coreTrainData == null", new Object[0]);
                        buildModelChrService.buildModelChrInfo(param, coreTrainData.getResult(), placeInfo, buildModelChrInfo, 0);
                        return -19;
                    }
                }
            } catch (RuntimeException e20) {
                e = e20;
                z = false;
                trainRet = 0;
                Object[] objArr22222222 = new Object[1];
                String message22222222 = e.getMessage();
                char c222222222 = z ? 1 : 0;
                char c232222222 = z ? 1 : 0;
                char c322222222 = z ? 1 : 0;
                char c422222222 = z ? 1 : 0;
                char c522222222 = z ? 1 : 0;
                char c622222222 = z ? 1 : 0;
                char c722222222 = z ? 1 : 0;
                char c822222222 = z ? 1 : 0;
                char c922222222 = z ? 1 : 0;
                char c1022222222 = z ? 1 : 0;
                char c1122222222 = z ? 1 : 0;
                char c1222222222 = z ? 1 : 0;
                char c1322222222 = z ? 1 : 0;
                char c1422222222 = z ? 1 : 0;
                char c1522222222 = z ? 1 : 0;
                char c1622222222 = z ? 1 : 0;
                char c1722222222 = z ? 1 : 0;
                char c1822222222 = z ? 1 : 0;
                char c1922222222 = z ? 1 : 0;
                char c2022222222 = z ? 1 : 0;
                char c2122222222 = z ? 1 : 0;
                objArr22222222[c222222222] = message22222222;
                LogUtil.e(z, "trainModelRe,e %{public}s", objArr22222222);
                return trainRet;
            } catch (Exception e21) {
            }
        }
        LogUtil.d(false, "trainModelRe,place==null", new Object[0]);
        return -1;
    }

    private int checkTrainRunSatisfiedResult() {
        if (SystemClock.elapsedRealtime() - this.mTrainBeginTime > Constant.MAX_TRAIN_MODEL_TIME) {
            LogUtil.d(false, "train model run,runTime > MAX_TRAINMODEL_TIME", new Object[0]);
            return -61;
        }
        CheckTemperatureUtil checkTemperatureUtil = this.mCheckTemperatureUtil;
        if (checkTemperatureUtil != null && checkTemperatureUtil.isExceedMaxTemperature()) {
            LogUtil.d(false, "train model run,the temperature is too high.", new Object[0]);
            return -62;
        } else if (isCharging()) {
            return 1;
        } else {
            LogUtil.d(false, "train model run,has stopped charging,so stop train model.", new Object[0]);
            return -63;
        }
    }

    private void handleByCoreTrainDataResult(CoreTrainData coreTrainData, RegularPlaceInfo placeInfo, String place, ParameterInfo param) {
        if (coreTrainData != null) {
            int result = coreTrainData.getResult();
            if (result == -54 || result == -51 || result == -8) {
                updateModel(placeInfo, place, param);
            }
        }
    }

    private void afterFailTrainModel(RegularPlaceInfo placeInfo) {
        placeInfo.setState(3);
        placeInfo.setTestDataNum(0);
        placeInfo.setDisNum(placeInfo.getDisNum() + 1);
        this.rgLocationDao.update(placeInfo);
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x02a9: APUT  
      (r12v17 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x02a4: INVOKE  (r16v8 java.lang.Integer) = 
      (wrap: int : 0x02a0: INVOKE  (r16v7 int) = (r7v3 'setPreLabel' java.util.Set<java.lang.Integer> A[D('setPreLabel' java.util.Set<java.lang.Integer>)]) type: INTERFACE call: java.util.Set.size():int)
     type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x02d3: APUT  
      (r12v15 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x02cd: INVOKE  (r18v3 java.lang.Integer) = (r0v22 'unknownCnt' int A[D('unknownCnt' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0314: APUT  
      (r12v13 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x030e: INVOKE  (r19v0 java.lang.Integer) = (r4v4 'checkShatterRatioCount' int A[D('checkShatterRatioCount' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x02b9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x02ba, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:122:0x02f2, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:123:0x02f4, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:124:0x02f5, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:135:0x0333, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x0334, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:139:0x033d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:140:0x033e, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0153, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0154, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x01e4, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x01e5, code lost:
        r1 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x01ec, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01ed, code lost:
        r1 = false;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:138:0x033b A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:55:0x0112] */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x0340 A[ExcHandler: Exception (e java.lang.Exception), PHI: r7 r8 
      PHI: (r7v1 'testDataFilePath' java.lang.String) = (r7v0 'testDataFilePath' java.lang.String), (r7v0 'testDataFilePath' java.lang.String), (r7v2 'testDataFilePath' java.lang.String), (r7v2 'testDataFilePath' java.lang.String), (r7v2 'testDataFilePath' java.lang.String) binds: [B:18:0x0057, B:19:?, B:33:0x00b6, B:34:?, B:38:0x00ca] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r8v1 'fileContent' java.lang.String) = (r8v0 'fileContent' java.lang.String), (r8v0 'fileContent' java.lang.String), (r8v2 'fileContent' java.lang.String), (r8v2 'fileContent' java.lang.String), (r8v2 'fileContent' java.lang.String) binds: [B:18:0x0057, B:19:?, B:33:0x00b6, B:34:?, B:38:0x00ca] A[DONT_GENERATE, DONT_INLINE], Splitter:B:18:0x0057] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01ec A[ExcHandler: RuntimeException (e java.lang.RuntimeException), Splitter:B:33:0x00b6] */
    public int discriminateModel(RegularPlaceInfo placeInfo, ParameterInfo param, int trainRet) {
        boolean z;
        RuntimeException e;
        String fileContent;
        String testDataFilePath;
        String str;
        TMapList<Integer, IdentifyResult> resultTMapList;
        int i;
        if (placeInfo == null) {
            return -28;
        }
        String str2 = "";
        if (str2.equals(placeInfo.getPlace())) {
            return -28;
        }
        if (param == null) {
            return -29;
        }
        if (trainRet < param.getMinModelTypes()) {
            return -21;
        }
        if (param.isMainAp() && trainRet >= param.getMinModelTypes()) {
            return 1;
        }
        String testDataFilePath2 = getTestDataFilePath(placeInfo.getPlace(), param);
        String fileContent2 = FileUtils.getFileContent(testDataFilePath2);
        if (((long) fileContent2.length()) > Constant.MAX_FILE_SIZE) {
            LogUtil.d(false, "discriminateModel ,file content is too bigger than max_file_size.", new Object[0]);
            return -22;
        }
        try {
            String[] fileLines = fileContent2.split(Constant.getLineSeparator());
            FingerInfo fingerInfo = new FingerInfo();
            HashMap<String, Integer> ssidDatas = new HashMap<>(16);
            StringBuilder resultFileBd = new StringBuilder(16);
            int size = fileLines.length;
            loadCommModels(placeInfo);
            ModelInfo model = getModelByParam(param);
            if (model == null) {
                try {
                    LogUtil.e(false, "discriminateModel failure,model == null ", new Object[0]);
                    return -23;
                } catch (RuntimeException e2) {
                    e = e2;
                    z = false;
                    Object[] objArr = new Object[1];
                    String message = e.getMessage();
                    char c = z ? 1 : 0;
                    char c2 = z ? 1 : 0;
                    char c3 = z ? 1 : 0;
                    char c4 = z ? 1 : 0;
                    char c5 = z ? 1 : 0;
                    char c6 = z ? 1 : 0;
                    char c7 = z ? 1 : 0;
                    char c8 = z ? 1 : 0;
                    char c9 = z ? 1 : 0;
                    char c10 = z ? 1 : 0;
                    char c11 = z ? 1 : 0;
                    char c12 = z ? 1 : 0;
                    char c13 = z ? 1 : 0;
                    objArr[c] = message;
                    LogUtil.e(z, "discriminateModel,e %{public}s", objArr);
                    return 1;
                } catch (Exception e3) {
                    LogUtil.e(false, "discriminateModel failed by Exception", new Object[0]);
                    return 1;
                }
            } else {
                TMapList<Integer, IdentifyResult> resultTMapList2 = new TMapList<>();
                int count = 0;
                int i2 = 0;
                while (i2 < size) {
                    String[] wds = fileLines[i2].split(",");
                    try {
                        if (wds.length < param.getScanWifiStart()) {
                            str = str2;
                            testDataFilePath = testDataFilePath2;
                            fileContent = fileContent2;
                            resultTMapList = resultTMapList2;
                            i = count;
                        } else if (wds[0] == null) {
                            str = str2;
                            testDataFilePath = testDataFilePath2;
                            fileContent = fileContent2;
                            resultTMapList = resultTMapList2;
                            i = count;
                        } else if (str2.equals(wds[0])) {
                            str = str2;
                            testDataFilePath = testDataFilePath2;
                            fileContent = fileContent2;
                            resultTMapList = resultTMapList2;
                            i = count;
                        } else {
                            int tempBatch = Integer.parseInt(wds[param.getBatchId()]);
                            ssidDatas.clear();
                            int tempSize = wds.length;
                            str = str2;
                            int k = param.getScanWifiStart();
                            while (k < tempSize) {
                                try {
                                    String[] tempScanWifiInfo = wds[k].split(param.getWifiSeperator());
                                    if (tempScanWifiInfo.length >= 4) {
                                        try {
                                            if (checkMacFormat(tempScanWifiInfo[param.getScanMac()])) {
                                                int tempRssi = Integer.parseInt(tempScanWifiInfo[param.getScanRssi()].split("\\.")[0]);
                                                if (tempRssi != 0) {
                                                    ssidDatas.put(tempScanWifiInfo[param.getScanMac()], Integer.valueOf(tempRssi));
                                                }
                                            }
                                        } catch (RuntimeException e4) {
                                            e = e4;
                                            z = false;
                                        } catch (Exception e5) {
                                        }
                                    }
                                    k++;
                                    tempSize = tempSize;
                                    testDataFilePath2 = testDataFilePath2;
                                    fileContent2 = fileContent2;
                                } catch (RuntimeException e6) {
                                    e = e6;
                                    z = false;
                                    Object[] objArr2 = new Object[1];
                                    String message2 = e.getMessage();
                                    char c14 = z ? 1 : 0;
                                    char c22 = z ? 1 : 0;
                                    char c32 = z ? 1 : 0;
                                    char c42 = z ? 1 : 0;
                                    char c52 = z ? 1 : 0;
                                    char c62 = z ? 1 : 0;
                                    char c72 = z ? 1 : 0;
                                    char c82 = z ? 1 : 0;
                                    char c92 = z ? 1 : 0;
                                    char c102 = z ? 1 : 0;
                                    char c112 = z ? 1 : 0;
                                    char c122 = z ? 1 : 0;
                                    char c132 = z ? 1 : 0;
                                    objArr2[c14] = message2;
                                    LogUtil.e(z, "discriminateModel,e %{public}s", objArr2);
                                    return 1;
                                } catch (Exception e7) {
                                    LogUtil.e(false, "discriminateModel failed by Exception", new Object[0]);
                                    return 1;
                                }
                            }
                            testDataFilePath = testDataFilePath2;
                            fileContent = fileContent2;
                            fingerInfo.setBssidDatas(ssidDatas);
                            int result = this.identifyService.identifyLocation(placeInfo.getPlace(), fingerInfo, param, model);
                            resultFileBd.append(TimeUtil.getTime());
                            resultFileBd.append(",");
                            resultFileBd.append(placeInfo.getModelName());
                            resultFileBd.append(",");
                            resultFileBd.append(result);
                            resultFileBd.append(",");
                            resultFileBd.append(fileLines[i2]);
                            resultFileBd.append(Constant.getLineSeparator());
                            count++;
                            resultTMapList = resultTMapList2;
                            resultTMapList.add((TMapList<Integer, IdentifyResult>) Integer.valueOf(tempBatch), (Integer) new IdentifyResult(tempBatch, result, 0));
                            i2++;
                            resultTMapList2 = resultTMapList;
                            str2 = str;
                            testDataFilePath2 = testDataFilePath;
                            fileContent2 = fileContent;
                        }
                    } catch (Exception e8) {
                        str = str2;
                        testDataFilePath = testDataFilePath2;
                        fileContent = fileContent2;
                        resultTMapList = resultTMapList2;
                        i = count;
                        LogUtil.e(false, "discriminateModel parseInt failed by Exception", new Object[0]);
                    } catch (RuntimeException e9) {
                    }
                    count = i;
                    i2++;
                    resultTMapList2 = resultTMapList;
                    str2 = str;
                    testDataFilePath2 = testDataFilePath;
                    fileContent2 = fileContent;
                }
                if (count == 0) {
                    return -24;
                }
                int unknownCnt = 0;
                int batchCount = 0;
                int checkShatterRatioCount = 0;
                Set<Integer> setPreLabel = new HashSet<>(16);
                for (Map.Entry<Integer, List<IdentifyResult>> entry2 : resultTMapList2.entrySet()) {
                    List<IdentifyResult> tempIdentifyResultLst = entry2.getValue();
                    for (IdentifyResult tempIdentifyResult : tempIdentifyResultLst) {
                        if (tempIdentifyResult.getPreLabel() < 0) {
                            unknownCnt++;
                        } else {
                            setPreLabel.add(Integer.valueOf(tempIdentifyResult.getPreLabel()));
                            unknownCnt = unknownCnt;
                        }
                    }
                    batchCount++;
                    if (checkShatterRatio(tempIdentifyResultLst, param)) {
                        checkShatterRatioCount++;
                    }
                    unknownCnt = unknownCnt;
                }
                FileUtils.saveFile(Constant.getTestResultPath() + placeInfo.getModelName() + "." + String.valueOf(System.currentTimeMillis()) + Constant.DISCRI_LOG_FILE_EXTENSION, resultFileBd.toString());
                if (setPreLabel.size() <= 1) {
                    Object[] objArr3 = new Object[2];
                    objArr3[0] = Integer.valueOf(setPreLabel.size());
                    objArr3[1] = setPreLabel.toString();
                    LogUtil.d(false, "discriminateModel failure,prelabel size:%{public}d,prelabel:%{public}s", objArr3);
                    return -25;
                } else if (((float) unknownCnt) / ((float) count) > param.getMinUnknownRatio()) {
                    Object[] objArr4 = new Object[3];
                    objArr4[0] = Integer.valueOf(unknownCnt);
                    objArr4[1] = Integer.valueOf(count);
                    objArr4[2] = Float.valueOf(param.getMinUnknownRatio());
                    z = false;
                    LogUtil.d(false, "discriminateModel failure, unknownCnt:%{public}d,count:%{public}d,tMinUnkwnRatio:%{public}f", objArr4);
                    return -26;
                } else {
                    float totalShatterRatio = 0.0f;
                    if (batchCount != 0) {
                        totalShatterRatio = ((float) checkShatterRatioCount) / ((float) batchCount);
                    }
                    if (totalShatterRatio >= param.getTotalShatterRatio()) {
                        return 1;
                    }
                    Object[] objArr5 = new Object[3];
                    objArr5[0] = Integer.valueOf(checkShatterRatioCount);
                    objArr5[1] = Integer.valueOf(batchCount);
                    objArr5[2] = Float.valueOf(param.getTotalShatterRatio());
                    LogUtil.d(false, "discriminateModel failure, checkShatterRatioCount:%{public}d,batchCount:%{public}d,TotalShatterRatio:%{public}f", objArr5);
                    return -27;
                }
            }
        } catch (RuntimeException e10) {
            e = e10;
            z = false;
            Object[] objArr22 = new Object[1];
            String message22 = e.getMessage();
            char c142 = z ? 1 : 0;
            char c222 = z ? 1 : 0;
            char c322 = z ? 1 : 0;
            char c422 = z ? 1 : 0;
            char c522 = z ? 1 : 0;
            char c622 = z ? 1 : 0;
            char c722 = z ? 1 : 0;
            char c822 = z ? 1 : 0;
            char c922 = z ? 1 : 0;
            char c1022 = z ? 1 : 0;
            char c1122 = z ? 1 : 0;
            char c1222 = z ? 1 : 0;
            char c1322 = z ? 1 : 0;
            objArr22[c142] = message22;
            LogUtil.e(z, "discriminateModel,e %{public}s", objArr22);
            return 1;
        } catch (Exception e11) {
        }
    }

    private boolean checkShatterRatio(List<IdentifyResult> identifyResults, ParameterInfo param) {
        if (identifyResults == null || identifyResults.size() == 0) {
            return false;
        }
        try {
            HashMap<Integer, AtomicInteger> stat = new HashMap<>(16);
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
            LogUtil.e(false, "checkShatterRatio failed by Exception", new Object[0]);
        }
    }

    public Map<String, String> filterMobileAp(String place) {
        Map<String, String> apChrStatInfoMp = new HashMap<>(16);
        try {
            apChrStatInfoMp.put(Constant.ALL_AP, this.trainModelService.filterMobileAp(place, this.parameterInfo));
            apChrStatInfoMp.put(Constant.MAIN_AP, this.trainModelService.filterMobileAp(place, this.mainParameterInfo));
        } catch (Exception e) {
            LogUtil.e(false, "filterMobileAp failed by Exception", new Object[0]);
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
        ClusterResult clusterResult2 = new ClusterResult(place);
        if (place == null) {
            LogUtil.d(false, "startTraining,place == null", new Object[0]);
            return clusterResult2;
        }
        try {
            this.mTrainBeginTime = SystemClock.elapsedRealtime();
            int result = trainModelRe(place, this.parameterInfo, allApChrStatInfo);
            LogUtil.i(false, "startTraining, begin allAP trainModelRe ,result:%{public}d", Integer.valueOf(result));
            clusterResult2.setClusterNum(result);
            if (result <= 0) {
                String toastInfo = "startTraining,train model failure, place :" + place;
                ShowToast.showToast(toastInfo);
                LogUtil.d(false, "%{public}s", toastInfo);
            } else {
                String toastInfo2 = "startTraining,train model success, place :" + place + ",cluster count:" + result;
                ShowToast.showToast(toastInfo2);
                LogUtil.d(false, "%{public}s", toastInfo2);
            }
            int mainApResult = trainModelRe(place, this.mainParameterInfo, mainApChrStatInfo);
            clusterResult2.setMainApClusterNum(mainApResult);
            LogUtil.i(false, "startTraining, begin mainAp trainModelRe ,result:%{public}d", Integer.valueOf(mainApResult));
            if (mainApResult <= 0) {
                String toastInfo3 = "startTraining,train mainAp model failure, place :" + place;
                ShowToast.showToast(toastInfo3);
                LogUtil.d(false, "%{public}s", toastInfo3);
            } else {
                String toastInfo4 = "startTraining,train mainAp model success, place :" + place + ",mainAp cluster count:" + mainApResult;
                ShowToast.showToast(toastInfo4);
                LogUtil.i(false, "%{public}s", toastInfo4);
            }
            LogUtil.d(false, "startTraining,result:%{public}s", clusterResult2.toString());
        } catch (Exception e) {
            LogUtil.e(false, "startTraining failed by Exception", new Object[0]);
        }
        return clusterResult2;
    }

    private boolean setNoCurBssids(FingerInfo fingerInfo, RegularPlaceInfo placeInfo, ParameterInfo param, ModelInfo model) {
        Set<String> curBssids = new HashSet<>(16);
        if (!(placeInfo == null || fingerInfo == null)) {
            try {
                if (fingerInfo.getBssidDatas() != null) {
                    if (fingerInfo.getBssidDatas().size() != 0) {
                        if (param != null) {
                            if (!param.isMainAp()) {
                                if (!(model == null || model.getBssidList() == null)) {
                                    if (model.getBssidList().length != 0) {
                                        curBssids.addAll(Arrays.asList(placeInfo.getNoOcurBssids().split(",")));
                                        for (String key : fingerInfo.getBssidDatas().keySet()) {
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
                LogUtil.e(false, "setNoCurBssids failed by Exception", new Object[0]);
                return true;
            }
        }
        return false;
    }

    public int identifyLocation(String place, FingerInfo fingerInfo, ParameterInfo param) {
        if (place == null) {
            LogUtil.d(false, "identifyLocation failure,place == null", new Object[0]);
            return -5;
        } else if (fingerInfo == null) {
            LogUtil.d(false, "identifyLocation failure,fingerInfo == null ", new Object[0]);
            return -5;
        } else if (param == null) {
            LogUtil.d(false, "identifyLocation failure,param == null ", new Object[0]);
            return -5;
        } else {
            ModelInfo model = getModelByParam(param);
            if (model == null) {
                LogUtil.d(false, "identifyLocation failure,model == null ", new Object[0]);
                return -5;
            }
            int result = 0;
            try {
                result = this.identifyService.identifyLocation(place, fingerInfo, param, model);
                IdentifyResult identifyResult = new IdentifyResult();
                identifyResult.setSsid(place);
                identifyResult.setPreLabel(result);
                identifyResult.setServeMac(fingerInfo.getServeMac());
                processPlaceInfo(identifyResult, place, fingerInfo, param, model);
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
                LogUtil.e(false, "identifyLocation failed by Exception", new Object[0]);
            }
        }
    }

    private void processPlaceInfo(IdentifyResult identifyResult, String place, FingerInfo fingerInfo, ParameterInfo param, ModelInfo model) {
        RegularPlaceInfo placeInfo = this.rgLocationDao.findBySsid(place, param.isMainAp());
        if (placeInfo != null) {
            identifyResult.setModelName(Integer.toString(placeInfo.getModelName()));
            this.identifyResultDao.insert(identifyResult, param.isMainAp());
            if (placeInfo.getIdentifyNum() > param.getCheckAgingAccumulateCount()) {
                LogUtil.d(false, "begin agingAction,identifyNum:%{public}d", Integer.valueOf(placeInfo.getIdentifyNum()));
                placeInfo = this.agingService.agingAction(placeInfo, place, param, model.getBssidList(), this.mMachineHandler);
            } else {
                setNoCurBssids(fingerInfo, placeInfo, param, model);
                placeInfo.setIdentifyNum(placeInfo.getIdentifyNum() + 1);
            }
            this.rgLocationDao.update(placeInfo);
        }
    }

    private boolean isCharging() {
        Context context = this.mContext;
        if (context == null) {
            LogUtil.e(false, "mContext is null error", new Object[0]);
            return false;
        }
        BatteryManager batteryManager = (BatteryManager) context.getSystemService("batterymanager");
        if (batteryManager != null) {
            return batteryManager.isCharging();
        }
        return false;
    }
}
