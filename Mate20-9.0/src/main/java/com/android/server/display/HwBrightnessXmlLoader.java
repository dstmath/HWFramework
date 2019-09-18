package com.android.server.display;

import android.graphics.PointF;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.net.HwNetworkStatsService;
import com.android.server.rms.iaware.cpu.CPUFeature;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

final class HwBrightnessXmlLoader {
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    /* access modifiers changed from: private */
    public static final boolean HWFLOW;
    private static final String LCD_PANEL_TYPE_PATH = "/sys/class/graphics/fb0/lcd_model";
    private static final String TAG = "HwBrightnessXmlLoader";
    private static final String TOUCH_OEM_INFO_PATH = "/sys/touchscreen/touch_oem_info";
    private static final String XML_EXT = ".xml";
    private static final String XML_NAME = "LABCConfig.xml";
    private static final String XML_NAME_NOEXT = "LABCConfig";
    /* access modifiers changed from: private */
    public static Data mData = new Data();
    private static HwBrightnessXmlLoader mLoader;
    private static final Object mLock = new Object();
    /* access modifiers changed from: private */
    public final int mDeviceActualBrightnessLevel;

    public static final class Data {
        public int QRCodeBrightnessminLimit = -1;
        public float adapted2UnadaptShortFilterLux = 5.0f;
        public int adapting2AdaptedOffDurationFilterSec = 1800;
        public int adapting2AdaptedOffDurationMaxSec = 28800;
        public int adapting2AdaptedOffDurationMinSec = 10;
        public int adapting2AdaptedOnClockNoFilterBeginHour = 21;
        public int adapting2AdaptedOnClockNoFilterEndHour = 7;
        public float adapting2UnadaptShortFilterLux = 5.0f;
        public boolean allowLabcUseProximity = false;
        public List<HwXmlAmPoint> ambientLuxValidBrightnessPoints = new ArrayList();
        public boolean animatedStepRoundEnabled = false;
        public boolean animatingForRGBWEnable = false;
        public boolean animationEqualRatioEnable = false;
        public float autoFastTimeFor255 = 0.5f;
        public boolean autoModeInOutDoorLimitEnble = false;
        public boolean autoPowerSavingBrighnessLineDisableForDemo = false;
        public boolean autoPowerSavingUseManualAnimationTimeEnable = false;
        public List<PointF> backSensorCoverModeBrighnessLinePoints = new ArrayList();
        public boolean backSensorCoverModeEnable = false;
        public int backSensorCoverModeMinLuxInRing = 0;
        public int brighenDebounceTime = 1000;
        public int brighenDebounceTimeForSmallThr = HwAPPQoEUtils.APP_TYPE_STREAMING;
        public int brightTimeDelay = 1000;
        public boolean brightTimeDelayEnable = false;
        public float brightTimeDelayLuxThreshold = 30.0f;
        public float brightenDebounceTimeParaBig = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float brightenDeltaLuxPara = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float brightenGradualTime = 1.0f;
        public boolean brightenOffsetEffectMinLuxEnable = false;
        public float brightenOffsetLuxTh1 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float brightenOffsetLuxTh2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float brightenOffsetLuxTh3 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float brightenOffsetNoValidBrightenLuxTh1 = -1.0f;
        public float brightenOffsetNoValidBrightenLuxTh2 = -1.0f;
        public float brightenOffsetNoValidBrightenLuxTh3 = -1.0f;
        public float brightenOffsetNoValidBrightenLuxTh4 = -1.0f;
        public float brightenOffsetNoValidDarkenLuxTh1 = -1.0f;
        public float brightenOffsetNoValidDarkenLuxTh2 = -1.0f;
        public float brightenOffsetNoValidDarkenLuxTh3 = -1.0f;
        public float brightenOffsetNoValidDarkenLuxTh4 = -1.0f;
        public float brightenOffsetNoValidSavedLuxTh1 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float brightenOffsetNoValidSavedLuxTh2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public int brightenThresholdFor255 = 1254;
        public List<PointF> brightenlinePoints = new ArrayList();
        public List<PointF> brightenlinePointsForBrightnessLevel = new ArrayList();
        public List<PointF> brightenlinePointsForDcMode = new ArrayList();
        public List<PointF> brightenlinePointsForGameMode = new ArrayList();
        public List<PointF> brightenlinePointsForLandScapeMode = new ArrayList();
        public boolean brightnessCalibrationEnabled = false;
        public int brightnessChageDefaultDelayTime = 500;
        public int brightnessChageDownDelayTime = 200;
        public int brightnessChageUpDelayTime = 50;
        public List<PointF> brightnessLevelToNitLinePoints = new ArrayList();
        public boolean brightnessLevelToNitMappingEnable = false;
        public List<PointF> brightnessMappingPoints = new ArrayList();
        public boolean brightnessOffsetLuxModeEnable = false;
        public boolean brightnessOffsetTmpValidEnable = false;
        public float cameraAnimationTime = 3.0f;
        public boolean cameraModeEnable = false;
        public int converModeDayBeginTime = 6;
        public List<PointF> coverModeBrighnessLinePoints = new ArrayList();
        public long coverModeBrightenResponseTime = 1000;
        public long coverModeDarkenResponseTime = 1000;
        public int coverModeDayBrightness = CPUFeature.MSG_RESET_ON_FIRE;
        public boolean coverModeDayEnable = false;
        public int coverModeDayEndTime = 18;
        public float coverModeFirstLux = 2210.0f;
        public boolean coverModelastCloseScreenEnable = false;
        public long cryogenicActiveScreenOffIntervalInMillis = HwNetworkStatsService.UPLOAD_INTERVAL;
        public boolean cryogenicEnable = false;
        public long cryogenicLagTimeInMillis = HwNetworkStatsService.UPLOAD_INTERVAL;
        public long cryogenicMaxBrightnessTimeOut = 9000;
        public boolean cryogenicModeBrightnessMappingEnable = false;
        public boolean darkAdapterEnable = false;
        public int darkLightLevelMaxThreshold = 0;
        public int darkLightLevelMinThreshold = 0;
        public float darkLightLevelRatio = 1.0f;
        public float darkLightLuxDelta = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float darkLightLuxMaxThreshold = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float darkLightLuxMinThreshold = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public int darkTimeDelay = 10000;
        public float darkTimeDelayBeta0 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float darkTimeDelayBeta1 = 1.0f;
        public float darkTimeDelayBeta2 = 0.333f;
        public float darkTimeDelayBrightness = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public boolean darkTimeDelayEnable = false;
        public float darkTimeDelayLuxThreshold = 50.0f;
        public int darkenCurrentFor255 = DeviceStatusConstant.TYPE_HEAD_DOWN;
        public int darkenDebounceTime = HwAPPQoEUtils.APP_TYPE_STREAMING;
        public int darkenDebounceTimeForSmallThr = 8000;
        public float darkenDebounceTimeParaBig = 1.0f;
        public float darkenDeltaLuxPara = 1.0f;
        public float darkenGradualTime = 3.0f;
        public float darkenGradualTimeMax = 3.0f;
        public float darkenGradualTimeMin = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float darkenOffsetLuxTh1 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float darkenOffsetLuxTh2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float darkenOffsetLuxTh3 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float darkenOffsetNoValidBrightenLuxTh1 = -1.0f;
        public float darkenOffsetNoValidBrightenLuxTh2 = -1.0f;
        public float darkenOffsetNoValidBrightenLuxTh3 = -1.0f;
        public float darkenOffsetNoValidBrightenLuxTh4 = -1.0f;
        public int darkenTargetFor255 = 1254;
        public List<PointF> darkenlinePoints = new ArrayList();
        public List<PointF> darkenlinePointsForBrightnessLevel = new ArrayList();
        public List<PointF> darkenlinePointsForDcMode = new ArrayList();
        public List<PointF> darkenlinePointsForGameMode = new ArrayList();
        public List<PointF> darkenlinePointsForLandScapeMode = new ArrayList();
        public boolean dayModeAlgoEnable = false;
        public int dayModeBeginTime = 5;
        public float dayModeDarkenMinLux = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public int dayModeEndTime = 23;
        public int dayModeModifyMinBrightness = 6;
        public int dayModeModifyNumPoint = 3;
        public int dayModeSwitchTime = 30;
        public long dcModeBrightenDebounceTime = 1000;
        public long dcModeDarkenDebounceTime = 1000;
        public boolean dcModeEnable = false;
        public boolean dcModeLuxThresholdEnable = false;
        public List<PointF> defaultBrighnessLinePoints = new ArrayList();
        public float defaultBrightness = 100.0f;
        public float dimTime = 3.0f;
        public List<HwXmlAmPoint> gameModeAmbientLuxValidBrightnessPoints = new ArrayList();
        public float gameModeBrightenAnimationTime = 0.5f;
        public long gameModeBrightenDebounceTime = 1000;
        public float gameModeBrightnessFloor = 4.0f;
        public boolean gameModeBrightnessLimitationEnable = false;
        public long gameModeClearOffsetTime = MemoryConstant.MIN_INTERVAL_OP_TIMEOUT;
        public long gameModeDarkenDebounceTime = MemoryConstant.MIN_INTERVAL_OP_TIMEOUT;
        public float gameModeDarkentenAnimationTime = 0.5f;
        public float gameModeDarkentenLongAnimationTime = 0.5f;
        public int gameModeDarkentenLongCurrent = 0;
        public int gameModeDarkentenLongTarget = 0;
        public boolean gameModeEnable = false;
        public boolean gameModeLuxThresholdEnable = false;
        public boolean gameModeOffsetValidAmbientLuxEnable = false;
        public int inDoorThreshold = HwAPPQoEUtils.APP_TYPE_STREAMING;
        public float initDoubleSensorInterfere = 8.0f;
        public int initNumLastBuffer = 10;
        public float initSigmoidFuncSlope = 0.75f;
        public int initSlowReponseBrightTime = 0;
        public int initSlowReponseUpperLuxThreshold = 20;
        public int initUpperLuxThreshold = 20;
        public long initValidCloseTime = -1;
        public float keyguardAnimationBrightenTime = 0.5f;
        public float keyguardAnimationDarkenTime = -1.0f;
        public float keyguardLuxThreshold = 20.0f;
        public int keyguardResponseBrightenTime = 500;
        public int keyguardResponseDarkenTime = -1;
        public boolean landScapeBrightnessModeEnable = false;
        public int landScapeModeBrightenDebounceTime = 1000;
        public int landScapeModeDarkenDebounceTime = 1000;
        public int landScapeModeEnterDelayTime = 500;
        public int landScapeModeQuitDelayTime = 500;
        public boolean landScapeModeUseTouchProximity = false;
        public boolean lastCloseScreenEnable = false;
        public int lightSensorRateMills = 300;
        public boolean luxlinePointsForBrightnessLevelEnable = false;
        public float manualAnimationBrightenTime = 0.5f;
        public float manualAnimationDarkenTime = 0.5f;
        public int manualBrighenDebounceTime = 3000;
        public List<PointF> manualBrightenlinePoints = new ArrayList();
        public int manualBrightnessMaxLimit = 255;
        public int manualBrightnessMinLimit = 4;
        public int manualDarkenDebounceTime = 3000;
        public List<PointF> manualDarkenlinePoints = new ArrayList();
        public float manualFastTimeFor255 = 0.5f;
        public boolean manualMode = false;
        public float manualPowerSavingAnimationBrightenTime = 0.5f;
        public float manualPowerSavingAnimationDarkenTime = 0.5f;
        public boolean manualPowerSavingBrighnessLineDisableForDemo = false;
        public boolean manualPowerSavingBrighnessLineEnable = false;
        public float manualThermalModeAnimationBrightenTime = 0.5f;
        public float manualThermalModeAnimationDarkenTime = 0.5f;
        public float minAnimatingStep = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public boolean monitorEnable = false;
        public int offsetBrightenDebounceTime = 1000;
        public int offsetDarkenDebounceTime = 1000;
        public boolean offsetResetEnable = false;
        public int offsetResetShortLuxDelta = 50000;
        public int offsetResetShortSwitchTime = 10;
        public int offsetResetSwitchTime = 10;
        public boolean offsetValidAmbientLuxEnable = false;
        public int outDoorThreshold = 8000;
        public float outdoorAnimationBrightenTime = 1.5f;
        public float outdoorAnimationDarkenTime = -1.0f;
        public int outdoorLowerLuxThreshold = 1000;
        public float outdoorResponseBrightenRatio = -1.0f;
        public int outdoorResponseBrightenTime = -1;
        public int outdoorResponseCount = 5;
        public float outdoorResponseDarkenRatio = -1.0f;
        public int outdoorResponseDarkenTime = -1;
        public boolean pgModeBrightnessMappingEnable = false;
        public boolean pgReregisterScene = false;
        public int pgSceneDetectionBrightenDelayTime = 500;
        public int pgSceneDetectionDarkenDelayTime = GestureNavConst.CHECK_AFT_TIMEOUT;
        public int postMaxMinAvgFilterNoFilterNum = 6;
        public int postMaxMinAvgFilterNum = 5;
        public int postMeanFilterNoFilterNum = 4;
        public int postMeanFilterNum = 3;
        public int postMethodNum = 2;
        public int powerOnBrightenDebounceTime = 500;
        public int powerOnDarkenDebounceTime = 1000;
        public int powerOnFastResponseLuxNum = 8;
        public int preMeanFilterNoFilterNum = 7;
        public int preMeanFilterNum = 3;
        public int preMethodNum = 0;
        public float preWeightedMeanFilterAlpha = 0.5f;
        public float preWeightedMeanFilterLuxTh = 12.0f;
        public int preWeightedMeanFilterMaxFuncLuxNum = 3;
        public int preWeightedMeanFilterNoFilterNum = 7;
        public int preWeightedMeanFilterNum = 3;
        public float proximityLuxThreshold = 20.0f;
        public int proximityNegativeDebounceTime = 3000;
        public int proximityPositiveDebounceTime = 150;
        public int proximityResponseBrightenTime = 3000;
        public boolean proximitySceneModeEnable = false;
        public float ratioForBrightnenSmallThr = 1.0f;
        public float ratioForDarkenSmallThr = 1.0f;
        public float readingAnimationTime = 3.0f;
        public boolean readingModeEnable = false;
        public boolean rebootAutoModeEnable = false;
        public int rebootFirstBrightness = 10000;
        public boolean rebootFirstBrightnessAnimationEnable = false;
        public float rebootFirstBrightnessAutoTime = 3.0f;
        public float rebootFirstBrightnessManualTime = 3.0f;
        public boolean reportValueWhenSensorOnChange = true;
        public int resetAmbientLuxBrightenDebounceTime = 1000;
        public int resetAmbientLuxDarkenDebounceTime = 1000;
        public float resetAmbientLuxDarkenRatio = 1.0f;
        public int resetAmbientLuxDisableBrightnessOffset = 45;
        public boolean resetAmbientLuxEnable = false;
        public float resetAmbientLuxFastDarkenDimmingTime = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public int resetAmbientLuxFastDarkenValidTime = 0;
        public float resetAmbientLuxGraTime = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float resetAmbientLuxStartBrightness = 200.0f;
        public float resetAmbientLuxStartBrightnessMax = 200.0f;
        public float resetAmbientLuxTh = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float resetAmbientLuxThMax = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float resetAmbientLuxThMin = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float sceneAmbientLuxMaxWeight = 0.5f;
        public float sceneAmbientLuxMinWeight = 0.5f;
        public int sceneGapPoints = 29;
        public int sceneMaxPoints = 0;
        public int sceneMinPoints = 29;
        public float screenBrightnessMaxNit = 530.0f;
        public float screenBrightnessMinNit = 2.0f;
        public long secondDarkenModeDarkenDebounceTime = 0;
        public float secondDarkenModeDarkenDeltaLuxRatio = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public boolean secondDarkenModeEanble = false;
        public float secondDarkenModeMaxLuxTh = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public float secondDarkenModeMinLuxTh = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public int stabilityConstant = 5;
        public int stabilityTime1 = 20;
        public int stabilityTime2 = 10;
        public boolean thermalModeBrightnessMappingEnable = false;
        public boolean touchProximityEnable = false;
        public float touchProximityYNearbyRatioMax = 1.0f;
        public float touchProximityYNearbyRatioMin = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        public int unadapt2AdaptedOffDurationMinSec = 2700;
        public int unadapt2AdaptedOnClockNoFilterBeginHour = 21;
        public int unadapt2AdaptedOnClockNoFilterEndHour = 7;
        public float unadapt2AdaptedShortFilterLux = 5.0f;
        public int unadapt2AdaptingDimSec = 60;
        public float unadapt2AdaptingLongFilterLux = 1.0f;
        public int unadapt2AdaptingLongFilterSec = GestureNavConst.GESTURE_GO_HOME_MIN_DISTANCE_THRESHOLD;
        public float unadapt2AdaptingShortFilterLux = 5.0f;
        public boolean useVariableStep = false;
        public long vehicleModeDisableTimeMillis = MemoryConstant.MIN_INTERVAL_OP_TIMEOUT;
        public boolean vehicleModeEnable = false;
        public long vehicleModeEnterTimeForPowerOn = 200;
        public long vehicleModeQuitTimeForPowerOn = 200;

        public Data() {
            loadDefaultConfig();
        }

        public void printData() {
            if (HwBrightnessXmlLoader.HWFLOW) {
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() lightSensorRateMills=" + this.lightSensorRateMills + ", brighenDebounceTime=" + this.brighenDebounceTime + ", darkenDebounceTime=" + this.darkenDebounceTime + ", brightenDebounceTimeParaBig=" + this.brightenDebounceTimeParaBig + ", darkenDebounceTimeParaBig=" + this.darkenDebounceTimeParaBig + ", brightenDeltaLuxPara=" + this.brightenDeltaLuxPara + ", darkenDeltaLuxPara=" + this.darkenDeltaLuxPara);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() stabilityConstant=" + this.stabilityConstant + ", stabilityTime1=" + this.stabilityTime1 + ", stabilityTime2=" + this.stabilityTime2 + ", brighenDebounceTimeForSmallThr=" + this.brighenDebounceTimeForSmallThr + ", darkenDebounceTimeForSmallThr=" + this.darkenDebounceTimeForSmallThr + ", ratioForBrightnenSmallThr=" + this.ratioForBrightnenSmallThr + ", ratioForDarkenSmallThr=" + this.ratioForDarkenSmallThr + ", rebootAutoModeEnable=" + this.rebootAutoModeEnable);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkTimeDelayEnable=" + this.darkTimeDelayEnable + ", darkTimeDelay=" + this.darkTimeDelay + ", darkTimeDelayLuxThreshold=" + this.darkTimeDelayLuxThreshold + ", coverModeFirstLux=" + this.coverModeFirstLux + ", lastCloseScreenEnable=" + this.lastCloseScreenEnable + ", coverModeBrightenResponseTime=" + this.coverModeBrightenResponseTime + ", coverModeDarkenResponseTime=" + this.coverModeDarkenResponseTime + ", coverModelastCloseScreenEnable=" + this.coverModelastCloseScreenEnable + ", coverModeDayEnable =" + this.coverModeDayEnable + ", coverModeDayBrightness =" + this.coverModeDayBrightness + ", converModeDayBeginTime =" + this.converModeDayBeginTime + ", coverModeDayEndTime =" + this.coverModeDayEndTime + ", postMaxMinAvgFilterNoFilterNum=" + this.postMaxMinAvgFilterNoFilterNum + ", postMaxMinAvgFilterNum=" + this.postMaxMinAvgFilterNum);
                StringBuilder sb = new StringBuilder();
                sb.append("printData() brightTimeDelayEnable=");
                sb.append(this.brightTimeDelayEnable);
                sb.append(", brightTimeDelay=");
                sb.append(this.brightTimeDelay);
                sb.append(", brightTimeDelayLuxThreshold=");
                sb.append(this.brightTimeDelayLuxThreshold);
                sb.append(", preMethodNum=");
                sb.append(this.preMethodNum);
                sb.append(", preMeanFilterNoFilterNum=");
                sb.append(this.preMeanFilterNoFilterNum);
                sb.append(", preMeanFilterNum=");
                sb.append(this.preMeanFilterNum);
                sb.append(", postMethodNum=");
                sb.append(this.postMethodNum);
                sb.append(", postMeanFilterNoFilterNum=");
                sb.append(this.postMeanFilterNoFilterNum);
                sb.append(", postMeanFilterNum=");
                sb.append(this.postMeanFilterNum);
                Slog.i(HwBrightnessXmlLoader.TAG, sb.toString());
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() preWeightedMeanFilterNoFilterNum=" + this.preWeightedMeanFilterNoFilterNum + ",preWeightedMeanFilterNum=" + this.preWeightedMeanFilterNum + ",preWeightedMeanFilterMaxFuncLuxNum=" + this.preWeightedMeanFilterMaxFuncLuxNum + ",preWeightedMeanFilterAlpha=" + this.preWeightedMeanFilterAlpha + ",preWeightedMeanFilterLuxTh=" + this.preWeightedMeanFilterLuxTh);
                StringBuilder sb2 = new StringBuilder();
                sb2.append("printData() darkTimeDelayBeta0=");
                sb2.append(this.darkTimeDelayBeta0);
                sb2.append(",darkTimeDelayBeta1=");
                sb2.append(this.darkTimeDelayBeta1);
                sb2.append(",darkTimeDelayBeta2=");
                sb2.append(this.darkTimeDelayBeta2);
                sb2.append(",darkTimeDelayBrightness=");
                sb2.append(this.darkTimeDelayBrightness);
                Slog.i(HwBrightnessXmlLoader.TAG, sb2.toString());
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() powerOnFastResponseLuxNum=" + this.powerOnFastResponseLuxNum);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() sceneMaxPoints=" + this.sceneMaxPoints + ",sceneGapPoints=" + this.sceneGapPoints + ",sceneMinPoints=" + this.sceneMinPoints + ",sceneAmbientLuxMaxWeight=" + this.sceneAmbientLuxMaxWeight + ",sceneAmbientLuxMinWeight=" + this.sceneAmbientLuxMinWeight);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() animationEqualRatioEnable=" + this.animationEqualRatioEnable + ",screenBrightnessMinNit=" + this.screenBrightnessMinNit + ",screenBrightnessMaxNit=" + this.screenBrightnessMaxNit + ",powerOnBrightenDebounceTime=" + this.powerOnBrightenDebounceTime + ",powerOnDarkenDebounceTime=" + this.powerOnDarkenDebounceTime + ",cameraModeEnable=" + this.cameraModeEnable + ",cameraAnimationTime=" + this.cameraAnimationTime + ",readingModeEnable=" + this.readingModeEnable + ",readingAnimationTime=" + this.readingAnimationTime + ",keyguardLuxThreshold=" + this.keyguardLuxThreshold + ",keyguardResponseBrightenTime=" + this.keyguardResponseBrightenTime + ",keyguardResponseDarkenTime=" + this.keyguardResponseDarkenTime + ",keyguardAnimationBrightenTime=" + this.keyguardAnimationBrightenTime + ",keyguardAnimationDarkenTime=" + this.keyguardAnimationDarkenTime);
                StringBuilder sb3 = new StringBuilder();
                sb3.append("printData() outdoorLowerLuxThreshold=");
                sb3.append(this.outdoorLowerLuxThreshold);
                sb3.append(", outdoorAnimationBrightenTime=");
                sb3.append(this.outdoorAnimationBrightenTime);
                sb3.append(", outdoorAnimationDarkenTime=");
                sb3.append(this.outdoorAnimationDarkenTime);
                sb3.append(", outdoorResponseBrightenRatio=");
                sb3.append(this.outdoorResponseBrightenRatio);
                sb3.append(", outdoorResponseDarkenRatio=");
                sb3.append(this.outdoorResponseDarkenRatio);
                sb3.append(", outdoorResponseBrightenTime=");
                sb3.append(this.outdoorResponseBrightenTime);
                sb3.append(", outdoorResponseDarkenTime=");
                sb3.append(this.outdoorResponseDarkenTime);
                sb3.append(", outdoorResponseCount=");
                sb3.append(this.outdoorResponseCount);
                Slog.i(HwBrightnessXmlLoader.TAG, sb3.toString());
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() proximityLuxThreshold=" + this.proximityLuxThreshold + ", proximityResponseBrightenTime= " + this.proximityResponseBrightenTime + ", initDoubleSensorInterfere =" + this.initDoubleSensorInterfere + ", initNumLastBuffer =" + this.initNumLastBuffer + ", initValidCloseTime =" + this.initValidCloseTime + ", initUpperLuxThreshold =" + this.initUpperLuxThreshold + ", initSigmoidFuncSlope  =" + this.initSigmoidFuncSlope + ", initSlowReponseUpperLuxThreshold  =" + this.initSlowReponseUpperLuxThreshold + ", initSlowReponseBrightTime  =" + this.initSlowReponseBrightTime);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() manualAnimationBrightenTime=" + this.manualAnimationBrightenTime + ", manualAnimationDarkenTime=" + this.manualAnimationDarkenTime + ", autoPowerSavingUseManualAnimationTimeEnable=" + this.autoPowerSavingUseManualAnimationTimeEnable + ", pgSceneDetectionDarkenDelayTime=" + this.pgSceneDetectionDarkenDelayTime + ", pgSceneDetectionBrightenDelayTime=" + this.pgSceneDetectionBrightenDelayTime + ", manualPowerSavingBrighnessLineEnable=" + this.manualPowerSavingBrighnessLineEnable + ", manualPowerSavingAnimationBrightenTime=" + this.manualPowerSavingAnimationBrightenTime + ", manualPowerSavingAnimationDarkenTime=" + this.manualPowerSavingAnimationDarkenTime + ", manualThermalModeAnimationBrightenTime=" + this.manualThermalModeAnimationBrightenTime + ", manualThermalModeAnimationDarkenTime=" + this.manualThermalModeAnimationDarkenTime + ", thermalModeBrightnessMappingEnable=" + this.thermalModeBrightnessMappingEnable + ", pgModeBrightnessMappingEnable=" + this.pgModeBrightnessMappingEnable + ", manualPowerSavingBrighnessLineDisableForDemo=" + this.manualPowerSavingBrighnessLineDisableForDemo + ", autoPowerSavingBrighnessLineDisableForDemo=" + this.autoPowerSavingBrighnessLineDisableForDemo);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() dayModeAlgoEnable=" + this.dayModeAlgoEnable + ", dayModeSwitchTime=" + this.dayModeSwitchTime + ", dayModeBeginTime=" + this.dayModeBeginTime + ", dayModeEndTime=" + this.dayModeEndTime + ", dayModeModifyNumPoint=" + this.dayModeModifyNumPoint + ", dayModeModifyMinBrightness=" + this.dayModeModifyMinBrightness + ", dayModeDarkenMinLux=" + this.dayModeDarkenMinLux + ", offsetResetSwitchTime =" + this.offsetResetSwitchTime + ", offsetResetEnable=" + this.offsetResetEnable + ", offsetResetShortSwitchTime=" + this.offsetResetShortSwitchTime + ", offsetResetShortLuxDelta=" + this.offsetResetShortLuxDelta + ", offsetBrightenDebounceTime=" + this.offsetBrightenDebounceTime + ", offsetDarkenDebounceTime=" + this.offsetDarkenDebounceTime + ", offsetValidAmbientLuxEnable=" + this.offsetValidAmbientLuxEnable);
                StringBuilder sb4 = new StringBuilder();
                sb4.append("printData() autoModeInOutDoorLimitEnble=");
                sb4.append(this.autoModeInOutDoorLimitEnble);
                sb4.append(", darkLightLevelMinThreshold=");
                sb4.append(this.darkLightLevelMinThreshold);
                sb4.append(", darkLightLevelMaxThreshold=");
                sb4.append(this.darkLightLevelMaxThreshold);
                sb4.append(", darkLightLevelRatio=");
                sb4.append(this.darkLightLevelRatio);
                sb4.append(", darkLightLuxMinThreshold=");
                sb4.append(this.darkLightLuxMinThreshold);
                sb4.append(", darkLightLuxMaxThreshold=");
                sb4.append(this.darkLightLuxMaxThreshold);
                sb4.append(", darkLightLuxDelta=");
                sb4.append(this.darkLightLuxDelta);
                sb4.append(", animatingForRGBWEnable=");
                sb4.append(this.animatingForRGBWEnable);
                sb4.append(", rebootFirstBrightnessAnimationEnable=");
                sb4.append(this.rebootFirstBrightnessAnimationEnable);
                sb4.append(", rebootFirstBrightness=");
                sb4.append(this.rebootFirstBrightness);
                sb4.append(", rebootFirstBrightnessAutoTime=");
                sb4.append(this.rebootFirstBrightnessAutoTime);
                sb4.append(", rebootFirstBrightnessManualTime=");
                sb4.append(this.rebootFirstBrightnessManualTime);
                Slog.i(HwBrightnessXmlLoader.TAG, sb4.toString());
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenlinePoints=" + this.brightenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkenlinePoints=" + this.darkenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() defaultBrightness=" + this.defaultBrightness + ", brightnessCalibrationEnabled=" + this.brightnessCalibrationEnabled);
                StringBuilder sb5 = new StringBuilder();
                sb5.append("printData() defaultBrighnessLinePoints=");
                sb5.append(this.defaultBrighnessLinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, sb5.toString());
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() coverModeBrighnessLinePoints=" + this.coverModeBrighnessLinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenGradualTime=" + this.brightenGradualTime + ", darkenGradualTime=" + this.darkenGradualTime + ", brightenThresholdFor255=" + this.brightenThresholdFor255 + ", darkenTargetFor255=" + this.darkenTargetFor255 + ", minAnimatingStep=" + this.minAnimatingStep + ", darkenCurrentFor255=" + this.darkenCurrentFor255 + ", autoFastTimeFor255=" + this.autoFastTimeFor255 + ", manualFastTimeFor255=" + this.manualFastTimeFor255);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() dimTime=" + this.dimTime + ", useVariableStep=" + this.useVariableStep + ", darkenGradualTimeMax=" + this.darkenGradualTimeMax + ", darkenGradualTimeMin=" + this.darkenGradualTimeMin + ", animatedStepRoundEnabled=" + this.animatedStepRoundEnabled + ", reportValueWhenSensorOnChange=" + this.reportValueWhenSensorOnChange + ", allowLabcUseProximity=" + this.allowLabcUseProximity + ", proximityPositiveDebounceTime=" + this.proximityPositiveDebounceTime + ", proximityNegativeDebounceTime=" + this.proximityNegativeDebounceTime);
                StringBuilder sb6 = new StringBuilder();
                sb6.append("printData() manualMode=");
                sb6.append(this.manualMode);
                sb6.append(", manualBrightnessMaxLimit=");
                sb6.append(this.manualBrightnessMaxLimit);
                sb6.append(", manualBrightnessMinLimit=");
                sb6.append(this.manualBrightnessMinLimit);
                sb6.append(", outDoorThreshold=");
                sb6.append(this.outDoorThreshold);
                sb6.append(", inDoorThreshold=");
                sb6.append(this.inDoorThreshold);
                sb6.append(", manualBrighenDebounceTime=");
                sb6.append(this.manualBrighenDebounceTime);
                sb6.append(", manualDarkenDebounceTime=");
                sb6.append(this.manualDarkenDebounceTime);
                Slog.i(HwBrightnessXmlLoader.TAG, sb6.toString());
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() manualBrightenlinePoints=" + this.manualBrightenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() manualDarkenlinePoints=" + this.manualDarkenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightnessMappingPoints=" + this.brightnessMappingPoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() ambientLuxValidBrightnessPoints=" + this.ambientLuxValidBrightnessPoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() QRCodeBrightnessminLimit=" + this.QRCodeBrightnessminLimit);
                if (this.darkAdapterEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() unadapt2AdaptingShortFilterLux=" + this.unadapt2AdaptingShortFilterLux + ", unadapt2AdaptingLongFilterLux=" + this.unadapt2AdaptingLongFilterLux + ", unadapt2AdaptingLongFilterSec=" + this.unadapt2AdaptingLongFilterSec + ", unadapt2AdaptingDimSec=" + this.unadapt2AdaptingDimSec + ", adapting2UnadaptShortFilterLux=" + this.adapting2UnadaptShortFilterLux + ", adapting2AdaptedOffDurationMinSec=" + this.adapting2AdaptedOffDurationMinSec + ", adapting2AdaptedOffDurationFilterSec=" + this.adapting2AdaptedOffDurationFilterSec + ", adapting2AdaptedOffDurationMaxSec=" + this.adapting2AdaptedOffDurationMaxSec + ", adapting2AdaptedOnClockNoFilterBeginHour=" + this.adapting2AdaptedOnClockNoFilterBeginHour + ", adapting2AdaptedOnClockNoFilterEndHour=" + this.adapting2AdaptedOnClockNoFilterEndHour + ", unadapt2AdaptedShortFilterLux=" + this.unadapt2AdaptedShortFilterLux + ", unadapt2AdaptedOffDurationMinSec=" + this.unadapt2AdaptedOffDurationMinSec + ", unadapt2AdaptedOnClockNoFilterBeginHour=" + this.unadapt2AdaptedOnClockNoFilterBeginHour + ", unadapt2AdaptedOnClockNoFilterEndHour=" + this.unadapt2AdaptedOnClockNoFilterEndHour + ", adapted2UnadaptShortFilterLux=" + this.adapted2UnadaptShortFilterLux);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() pgReregisterScene=" + this.pgReregisterScene);
                if (this.touchProximityEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() touchProximityYNearbyRatio=" + this.touchProximityYNearbyRatioMin + AwarenessConstants.SECOND_ACTION_SPLITE_TAG + this.touchProximityYNearbyRatioMax);
                    StringBuilder sb7 = new StringBuilder();
                    sb7.append("printData() proximitySceneModeEnable=");
                    sb7.append(this.proximitySceneModeEnable);
                    Slog.i(HwBrightnessXmlLoader.TAG, sb7.toString());
                }
                if (this.vehicleModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() vehicleModeEnable=" + this.vehicleModeEnable + ",vehicleModeDisableTimeMillis=" + this.vehicleModeDisableTimeMillis + ",vehicleModeQuitTimeForPowerOn=" + this.vehicleModeQuitTimeForPowerOn + ",vehicleModeEnterTimeForPowerOn=" + this.vehicleModeEnterTimeForPowerOn);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() gameModeEnable=" + this.gameModeEnable + ",gameModeBrightenAnimationTime=" + this.gameModeBrightenAnimationTime + ",gameModeDarkentenAnimationTime=" + this.gameModeDarkentenAnimationTime + ",gameModeDarkentenLongAnimationTime=" + this.gameModeDarkentenLongAnimationTime + ",gameModeDarkentenLongTarget=" + this.gameModeDarkentenLongTarget + ",gameModeDarkentenLongCurrent=" + this.gameModeDarkentenLongCurrent + ",gameModeClearOffsetTime=" + this.gameModeClearOffsetTime + ",gameModeBrightenDebounceTime=" + this.gameModeBrightenDebounceTime + ",gameModeDarkenDebounceTime=" + this.gameModeDarkenDebounceTime + ",gameModeLuxThresholdEnable=" + this.gameModeLuxThresholdEnable);
                if (this.dcModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() dcModeEnable=" + this.dcModeEnable + ",dcModeBrightenDebounceTime=" + this.dcModeBrightenDebounceTime + ",dcModeDarkenDebounceTime" + this.dcModeDarkenDebounceTime + ",dcModeLuxThresholdEnable=" + this.dcModeLuxThresholdEnable);
                }
                if (this.dcModeLuxThresholdEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenlinePointsForDcMode=" + this.brightenlinePointsForDcMode + ",darkenlinePointsForDcMode=" + this.darkenlinePointsForDcMode);
                }
                if (this.gameModeLuxThresholdEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenlinePointsForGameMode=" + this.brightenlinePointsForGameMode);
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkenlinePointsForGameMode=" + this.darkenlinePointsForGameMode);
                }
                if (this.gameModeOffsetValidAmbientLuxEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() gameModeAmbientLuxValidBrightnessPoints=" + this.gameModeAmbientLuxValidBrightnessPoints);
                }
                if (this.gameModeBrightnessLimitationEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() gameModeBrightnessFloor=" + this.gameModeBrightnessFloor);
                }
                if (this.backSensorCoverModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() backSensorCoverModeBrighnessLinePoints=" + this.backSensorCoverModeBrighnessLinePoints);
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() backSensorCoverModeMinLuxInRing=" + this.backSensorCoverModeMinLuxInRing);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() cryogenicEnable=" + this.cryogenicEnable + ", cryogenicModeBrightnessMappingEnable=" + this.cryogenicModeBrightnessMappingEnable + ", cryogenicMaxBrightnessTimeOut=" + this.cryogenicMaxBrightnessTimeOut + ", cryogenicActiveScreenOffIntervalInMillis=" + this.cryogenicActiveScreenOffIntervalInMillis + ", cryogenicLagTimeInMillis=" + this.cryogenicLagTimeInMillis);
                if (this.landScapeBrightnessModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() landScapeBrightnessModeEnable=" + this.landScapeBrightnessModeEnable + ",landScapeModeBrightenDebounceTime=" + this.landScapeModeBrightenDebounceTime + ",landScapeModeDarkenDebounceTime=" + this.landScapeModeDarkenDebounceTime + ",landScapeModeEnterDelayTime=" + this.landScapeModeEnterDelayTime + ",landScapeModeQuitDelayTime=" + this.landScapeModeQuitDelayTime + ",landScapeModeUseTouchProximity=" + this.landScapeModeUseTouchProximity);
                    StringBuilder sb8 = new StringBuilder();
                    sb8.append("printData() brightenlinePointsForLandScapeMode=");
                    sb8.append(this.brightenlinePointsForLandScapeMode);
                    Slog.i(HwBrightnessXmlLoader.TAG, sb8.toString());
                    StringBuilder sb9 = new StringBuilder();
                    sb9.append("printData() darkenlinePointsForLandScapeMode=");
                    sb9.append(this.darkenlinePointsForLandScapeMode);
                    Slog.i(HwBrightnessXmlLoader.TAG, sb9.toString());
                }
                if (this.brightnessLevelToNitMappingEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightnessLevelToNitMappingEnable=" + this.brightnessLevelToNitMappingEnable + ",brightnessLevelToNitLinePoints=" + this.brightnessLevelToNitLinePoints);
                }
                if (this.resetAmbientLuxEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() resetAmbientLuxTh=" + this.resetAmbientLuxTh + ",resetAmbientLuxThMin=" + this.resetAmbientLuxThMin + ",resetAmbientLuxDarkenDebounceTime=" + this.resetAmbientLuxDarkenDebounceTime + ",resetAmbientLuxBrightenDebounceTime=" + this.resetAmbientLuxBrightenDebounceTime + ",resetAmbientLuxFastDarkenValidTime=" + this.resetAmbientLuxFastDarkenValidTime + ",resetAmbientLuxDarkenRatio=" + this.resetAmbientLuxDarkenRatio + ",resetAmbientLuxGraTime=" + this.resetAmbientLuxGraTime + ",resetAmbientLuxFastDarkenDimmingTime=" + this.resetAmbientLuxFastDarkenDimmingTime + ",resetAmbientLuxStartBrightness=" + this.resetAmbientLuxStartBrightness + ",resetAmbientLuxThMax=" + this.resetAmbientLuxThMax + ",resetAmbientLuxStartBrightnessMax=" + this.resetAmbientLuxStartBrightnessMax + ",resetAmbientLuxDisableBrightnessOffset=" + this.resetAmbientLuxDisableBrightnessOffset);
                }
                if (this.luxlinePointsForBrightnessLevelEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightnessChageUpDelayTime=" + this.brightnessChageUpDelayTime + ",brightnessChageDownDelayTime=" + this.brightnessChageDownDelayTime + ",brightnessChageDefaultDelayTime=" + this.brightnessChageDefaultDelayTime + ",brightenlinePointsForBrightnessLevel=" + this.brightenlinePointsForBrightnessLevel + ",darkenlinePointsForBrightnessLevel=" + this.darkenlinePointsForBrightnessLevel);
                }
                if (this.secondDarkenModeEanble) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() secondDarkenModeMinLuxTh=" + this.secondDarkenModeMinLuxTh + ",secondDarkenModeMaxLuxTh=" + this.secondDarkenModeMaxLuxTh + ",secondDarkenModeDarkenDeltaLuxRatio=" + this.secondDarkenModeDarkenDeltaLuxRatio + ",secondDarkenModeDarkenDebounceTime=" + this.secondDarkenModeDarkenDebounceTime);
                }
                if (this.brightnessOffsetLuxModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenOffsetLuxTh1=" + this.brightenOffsetLuxTh1 + ",brightenOffsetLuxTh2=" + this.brightenOffsetLuxTh2 + ",brightenOffsetLuxTh3=" + this.brightenOffsetLuxTh3 + ",brightenOffsetNoValidDarkenLuxTh1=" + this.brightenOffsetNoValidDarkenLuxTh1 + ",brightenOffsetNoValidDarkenLuxTh2=" + this.brightenOffsetNoValidDarkenLuxTh2 + ",brightenOffsetNoValidDarkenLuxTh3=" + this.brightenOffsetNoValidDarkenLuxTh3 + ",brightenOffsetNoValidDarkenLuxTh4=" + this.brightenOffsetNoValidDarkenLuxTh4 + ",brightenOffsetNoValidBrightenLuxTh1=" + this.brightenOffsetNoValidBrightenLuxTh1 + ",brightenOffsetNoValidBrightenLuxTh2=" + this.brightenOffsetNoValidBrightenLuxTh2 + ",brightenOffsetNoValidBrightenLuxTh3=" + this.brightenOffsetNoValidBrightenLuxTh3 + ",brightenOffsetNoValidBrightenLuxTh4=" + this.brightenOffsetNoValidBrightenLuxTh4 + ",darkenOffsetLuxTh1=" + this.darkenOffsetLuxTh1 + ",darkenOffsetLuxTh2=" + this.darkenOffsetLuxTh2 + ",darkenOffsetLuxTh3=" + this.darkenOffsetLuxTh3 + ",darkenOffsetNoValidBrightenLuxTh1=" + this.darkenOffsetNoValidBrightenLuxTh1 + ",darkenOffsetNoValidBrightenLuxTh2=" + this.darkenOffsetNoValidBrightenLuxTh2 + ",darkenOffsetNoValidBrightenLuxTh3=" + this.darkenOffsetNoValidBrightenLuxTh3 + ",darkenOffsetNoValidBrightenLuxTh4=" + this.darkenOffsetNoValidBrightenLuxTh4 + ",brightenOffsetEffectMinLuxEnable=" + this.brightenOffsetEffectMinLuxEnable + ",brightnessOffsetTmpValidEnable=" + this.brightnessOffsetTmpValidEnable + ",brightenOffsetNoValidSavedLuxTh1=" + this.brightenOffsetNoValidSavedLuxTh1 + ",brightenOffsetNoValidSavedLuxTh2=" + this.brightenOffsetNoValidSavedLuxTh2);
                }
            }
        }

        public void loadDefaultConfig() {
            if (HwBrightnessXmlLoader.HWFLOW) {
                Slog.i(HwBrightnessXmlLoader.TAG, "loadDefaultConfig()");
            }
            this.lightSensorRateMills = 300;
            this.brighenDebounceTime = 1000;
            this.darkenDebounceTime = HwAPPQoEUtils.APP_TYPE_STREAMING;
            this.brightenDebounceTimeParaBig = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkenDebounceTimeParaBig = 1.0f;
            this.brightenDeltaLuxPara = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkenDeltaLuxPara = 1.0f;
            this.stabilityConstant = 5;
            this.stabilityTime1 = 20;
            this.stabilityTime2 = 10;
            this.brighenDebounceTimeForSmallThr = HwAPPQoEUtils.APP_TYPE_STREAMING;
            this.darkenDebounceTimeForSmallThr = 8000;
            this.ratioForBrightnenSmallThr = 1.0f;
            this.ratioForDarkenSmallThr = 1.0f;
            this.rebootAutoModeEnable = false;
            this.darkTimeDelayEnable = false;
            this.darkTimeDelay = 10000;
            this.darkTimeDelayLuxThreshold = 50.0f;
            this.coverModeFirstLux = 2210.0f;
            this.lastCloseScreenEnable = false;
            this.coverModeBrightenResponseTime = 1000;
            this.coverModeDarkenResponseTime = 1000;
            this.coverModelastCloseScreenEnable = false;
            this.coverModeDayEnable = false;
            this.coverModeDayBrightness = CPUFeature.MSG_RESET_ON_FIRE;
            this.converModeDayBeginTime = 6;
            this.coverModeDayEndTime = 18;
            this.postMaxMinAvgFilterNoFilterNum = 6;
            this.postMaxMinAvgFilterNum = 5;
            this.brightTimeDelayEnable = false;
            this.brightTimeDelay = 1000;
            this.brightTimeDelayLuxThreshold = 30.0f;
            this.preMethodNum = 0;
            this.preMeanFilterNoFilterNum = 7;
            this.preMeanFilterNum = 3;
            this.postMethodNum = 2;
            this.postMeanFilterNoFilterNum = 4;
            this.postMeanFilterNum = 3;
            this.preWeightedMeanFilterNoFilterNum = 7;
            this.preWeightedMeanFilterNum = 3;
            this.preWeightedMeanFilterMaxFuncLuxNum = 3;
            this.preWeightedMeanFilterAlpha = 0.5f;
            this.preWeightedMeanFilterLuxTh = 12.0f;
            this.darkTimeDelayBeta0 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkTimeDelayBeta1 = 1.0f;
            this.darkTimeDelayBeta2 = 0.333f;
            this.darkTimeDelayBrightness = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.powerOnFastResponseLuxNum = 8;
            this.sceneMaxPoints = 0;
            this.sceneGapPoints = 29;
            this.sceneMinPoints = 29;
            this.sceneAmbientLuxMaxWeight = 0.5f;
            this.sceneAmbientLuxMinWeight = 0.5f;
            this.animationEqualRatioEnable = false;
            this.screenBrightnessMinNit = 2.0f;
            this.screenBrightnessMaxNit = 530.0f;
            this.powerOnBrightenDebounceTime = 500;
            this.powerOnDarkenDebounceTime = 1000;
            this.cameraModeEnable = false;
            this.cameraAnimationTime = 3.0f;
            this.readingModeEnable = false;
            this.readingAnimationTime = 3.0f;
            this.keyguardResponseBrightenTime = 500;
            this.keyguardResponseDarkenTime = -1;
            this.keyguardAnimationBrightenTime = 0.5f;
            this.keyguardAnimationDarkenTime = -1.0f;
            this.keyguardLuxThreshold = 20.0f;
            this.outdoorLowerLuxThreshold = 1000;
            this.outdoorAnimationBrightenTime = 1.5f;
            this.outdoorAnimationDarkenTime = -1.0f;
            this.outdoorResponseBrightenRatio = -1.0f;
            this.outdoorResponseDarkenRatio = -1.0f;
            this.outdoorResponseBrightenTime = -1;
            this.outdoorResponseDarkenTime = -1;
            this.outdoorResponseCount = 5;
            this.proximityLuxThreshold = 20.0f;
            this.proximityResponseBrightenTime = 3000;
            this.initDoubleSensorInterfere = 8.0f;
            this.initNumLastBuffer = 10;
            this.initValidCloseTime = -1;
            this.initUpperLuxThreshold = 20;
            this.initSigmoidFuncSlope = 0.75f;
            this.initSlowReponseUpperLuxThreshold = 20;
            this.initSlowReponseBrightTime = 0;
            this.manualAnimationBrightenTime = 0.5f;
            this.manualAnimationDarkenTime = 0.5f;
            this.autoPowerSavingUseManualAnimationTimeEnable = false;
            this.pgSceneDetectionDarkenDelayTime = GestureNavConst.CHECK_AFT_TIMEOUT;
            this.pgSceneDetectionBrightenDelayTime = 500;
            this.manualPowerSavingBrighnessLineEnable = false;
            this.manualPowerSavingAnimationBrightenTime = 0.5f;
            this.manualPowerSavingAnimationDarkenTime = 0.5f;
            this.manualThermalModeAnimationBrightenTime = 0.5f;
            this.manualThermalModeAnimationDarkenTime = 0.5f;
            this.thermalModeBrightnessMappingEnable = false;
            this.pgModeBrightnessMappingEnable = false;
            this.manualPowerSavingBrighnessLineDisableForDemo = false;
            this.autoPowerSavingBrighnessLineDisableForDemo = false;
            this.dayModeAlgoEnable = false;
            this.dayModeSwitchTime = 30;
            this.dayModeBeginTime = 5;
            this.dayModeEndTime = 23;
            this.dayModeModifyNumPoint = 3;
            this.dayModeModifyMinBrightness = 6;
            this.dayModeDarkenMinLux = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.offsetResetSwitchTime = 10;
            this.offsetResetEnable = false;
            this.offsetResetShortSwitchTime = 10;
            this.offsetResetShortLuxDelta = 50000;
            this.offsetBrightenDebounceTime = 1000;
            this.offsetDarkenDebounceTime = 1000;
            this.offsetValidAmbientLuxEnable = false;
            this.autoModeInOutDoorLimitEnble = false;
            this.darkLightLevelMinThreshold = 0;
            this.darkLightLevelMaxThreshold = 0;
            this.darkLightLevelRatio = 1.0f;
            this.darkLightLuxMinThreshold = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkLightLuxMaxThreshold = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkLightLuxDelta = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.animatingForRGBWEnable = false;
            this.rebootFirstBrightnessAnimationEnable = false;
            this.rebootFirstBrightness = 10000;
            this.rebootFirstBrightnessAutoTime = 3.0f;
            this.rebootFirstBrightnessManualTime = 3.0f;
            this.monitorEnable = false;
            this.brightenlinePoints.clear();
            this.brightenlinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 15.0f));
            this.brightenlinePoints.add(new PointF(2.0f, 15.0f));
            this.brightenlinePoints.add(new PointF(10.0f, 19.0f));
            this.brightenlinePoints.add(new PointF(20.0f, 219.0f));
            this.brightenlinePoints.add(new PointF(100.0f, 539.0f));
            this.brightenlinePoints.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePoints.add(new PointF(40000.0f, 989.0f));
            this.darkenlinePoints.clear();
            this.darkenlinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f));
            this.darkenlinePoints.add(new PointF(1.0f, 1.0f));
            this.darkenlinePoints.add(new PointF(20.0f, 20.0f));
            this.darkenlinePoints.add(new PointF(40.0f, 20.0f));
            this.darkenlinePoints.add(new PointF(100.0f, 80.0f));
            this.darkenlinePoints.add(new PointF(600.0f, 580.0f));
            this.darkenlinePoints.add(new PointF(1180.0f, 580.0f));
            this.darkenlinePoints.add(new PointF(1200.0f, 600.0f));
            this.darkenlinePoints.add(new PointF(1800.0f, 600.0f));
            this.darkenlinePoints.add(new PointF(40000.0f, 38800.0f));
            this.defaultBrightness = 100.0f;
            this.brightnessCalibrationEnabled = false;
            this.defaultBrighnessLinePoints.clear();
            this.defaultBrighnessLinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 4.0f));
            this.defaultBrighnessLinePoints.add(new PointF(25.0f, 46.5f));
            this.defaultBrighnessLinePoints.add(new PointF(1995.0f, 140.7f));
            this.defaultBrighnessLinePoints.add(new PointF(4000.0f, 255.0f));
            this.defaultBrighnessLinePoints.add(new PointF(40000.0f, 255.0f));
            this.coverModeBrighnessLinePoints.clear();
            this.coverModeBrighnessLinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 20.0f));
            this.coverModeBrighnessLinePoints.add(new PointF(25.0f, 46.5f));
            this.coverModeBrighnessLinePoints.add(new PointF(250.0f, 100.0f));
            this.coverModeBrighnessLinePoints.add(new PointF(1995.0f, 154.7f));
            this.coverModeBrighnessLinePoints.add(new PointF(4000.0f, 255.0f));
            this.coverModeBrighnessLinePoints.add(new PointF(40000.0f, 255.0f));
            this.brightenGradualTime = 1.0f;
            this.darkenGradualTime = 3.0f;
            this.brightenThresholdFor255 = 1254;
            this.darkenTargetFor255 = 1254;
            this.minAnimatingStep = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkenCurrentFor255 = DeviceStatusConstant.TYPE_HEAD_DOWN;
            this.autoFastTimeFor255 = 0.5f;
            this.manualFastTimeFor255 = 0.5f;
            this.dimTime = 3.0f;
            this.useVariableStep = false;
            this.darkenGradualTimeMax = 3.0f;
            this.darkenGradualTimeMin = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.animatedStepRoundEnabled = false;
            this.reportValueWhenSensorOnChange = true;
            this.allowLabcUseProximity = false;
            this.proximityPositiveDebounceTime = 150;
            this.proximityNegativeDebounceTime = 3000;
            this.manualMode = false;
            this.manualBrightnessMaxLimit = 255;
            this.manualBrightnessMinLimit = 4;
            this.QRCodeBrightnessminLimit = -1;
            this.outDoorThreshold = 8000;
            this.inDoorThreshold = HwAPPQoEUtils.APP_TYPE_STREAMING;
            this.manualBrighenDebounceTime = 3000;
            this.manualDarkenDebounceTime = 3000;
            this.manualBrightenlinePoints.clear();
            this.manualBrightenlinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1000.0f));
            this.manualBrightenlinePoints.add(new PointF(1000.0f, 5000.0f));
            this.manualBrightenlinePoints.add(new PointF(40000.0f, 10000.0f));
            this.manualDarkenlinePoints.clear();
            this.manualDarkenlinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f));
            this.manualDarkenlinePoints.add(new PointF(500.0f, 10.0f));
            this.manualDarkenlinePoints.add(new PointF(1000.0f, 500.0f));
            this.manualDarkenlinePoints.add(new PointF(2000.0f, 1000.0f));
            this.manualDarkenlinePoints.add(new PointF(40000.0f, 30000.0f));
            this.brightnessMappingPoints.clear();
            this.brightnessMappingPoints.add(new PointF(4.0f, 4.0f));
            this.brightnessMappingPoints.add(new PointF(25.0f, 25.0f));
            this.brightnessMappingPoints.add(new PointF(245.0f, 245.0f));
            this.brightnessMappingPoints.add(new PointF(255.0f, 255.0f));
            this.ambientLuxValidBrightnessPoints.clear();
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 4.0f, 55.0f));
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(100.0f, 4.0f, 255.0f));
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(1000.0f, 4.0f, 255.0f));
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(5000.0f, 50.0f, 255.0f));
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(40000.0f, 50.0f, 255.0f));
            this.darkAdapterEnable = false;
            this.pgReregisterScene = false;
            this.touchProximityEnable = false;
            this.touchProximityYNearbyRatioMin = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.touchProximityYNearbyRatioMax = 1.0f;
            this.proximitySceneModeEnable = false;
            this.vehicleModeEnable = false;
            this.vehicleModeDisableTimeMillis = MemoryConstant.MIN_INTERVAL_OP_TIMEOUT;
            this.vehicleModeQuitTimeForPowerOn = 200;
            this.vehicleModeEnterTimeForPowerOn = 200;
            this.gameModeEnable = false;
            this.gameModeBrightenAnimationTime = 0.5f;
            this.gameModeDarkentenAnimationTime = 0.5f;
            this.gameModeDarkentenLongAnimationTime = 0.5f;
            this.gameModeDarkentenLongTarget = 0;
            this.gameModeDarkentenLongCurrent = 0;
            this.gameModeClearOffsetTime = MemoryConstant.MIN_INTERVAL_OP_TIMEOUT;
            this.gameModeBrightenDebounceTime = 1000;
            this.gameModeDarkenDebounceTime = MemoryConstant.MIN_INTERVAL_OP_TIMEOUT;
            this.gameModeLuxThresholdEnable = false;
            this.gameModeBrightnessLimitationEnable = false;
            this.gameModeBrightnessFloor = 4.0f;
            this.brightenlinePointsForGameMode.clear();
            this.brightenlinePointsForGameMode.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 15.0f));
            this.brightenlinePointsForGameMode.add(new PointF(20.0f, 219.0f));
            this.brightenlinePointsForGameMode.add(new PointF(100.0f, 539.0f));
            this.brightenlinePointsForGameMode.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePointsForGameMode.add(new PointF(40000.0f, 989.0f));
            this.darkenlinePointsForGameMode.clear();
            this.darkenlinePointsForGameMode.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f));
            this.darkenlinePointsForGameMode.add(new PointF(1.0f, 1.0f));
            this.darkenlinePointsForGameMode.add(new PointF(20.0f, 20.0f));
            this.darkenlinePointsForGameMode.add(new PointF(100.0f, 80.0f));
            this.darkenlinePointsForGameMode.add(new PointF(1800.0f, 600.0f));
            this.darkenlinePointsForGameMode.add(new PointF(40000.0f, 38800.0f));
            this.gameModeOffsetValidAmbientLuxEnable = false;
            this.gameModeAmbientLuxValidBrightnessPoints.clear();
            this.dcModeEnable = false;
            this.dcModeBrightenDebounceTime = 1000;
            this.dcModeDarkenDebounceTime = 1000;
            this.dcModeLuxThresholdEnable = false;
            this.brightenlinePointsForDcMode.clear();
            this.brightenlinePointsForDcMode.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 15.0f));
            this.brightenlinePointsForDcMode.add(new PointF(20.0f, 219.0f));
            this.brightenlinePointsForDcMode.add(new PointF(100.0f, 539.0f));
            this.brightenlinePointsForDcMode.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePointsForDcMode.add(new PointF(40000.0f, 989.0f));
            this.darkenlinePointsForDcMode.clear();
            this.darkenlinePointsForDcMode.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f));
            this.darkenlinePointsForDcMode.add(new PointF(1.0f, 1.0f));
            this.darkenlinePointsForDcMode.add(new PointF(20.0f, 20.0f));
            this.darkenlinePointsForDcMode.add(new PointF(100.0f, 80.0f));
            this.darkenlinePointsForDcMode.add(new PointF(1800.0f, 600.0f));
            this.darkenlinePointsForDcMode.add(new PointF(40000.0f, 38800.0f));
            this.backSensorCoverModeEnable = false;
            this.backSensorCoverModeBrighnessLinePoints.clear();
            this.backSensorCoverModeMinLuxInRing = 0;
            this.cryogenicEnable = false;
            this.cryogenicModeBrightnessMappingEnable = false;
            this.cryogenicMaxBrightnessTimeOut = 5000;
            this.cryogenicActiveScreenOffIntervalInMillis = HwNetworkStatsService.UPLOAD_INTERVAL;
            this.cryogenicLagTimeInMillis = HwNetworkStatsService.UPLOAD_INTERVAL;
            this.landScapeBrightnessModeEnable = false;
            this.landScapeModeBrightenDebounceTime = 1000;
            this.landScapeModeDarkenDebounceTime = 1000;
            this.landScapeModeEnterDelayTime = 500;
            this.landScapeModeQuitDelayTime = 1000;
            this.landScapeModeUseTouchProximity = false;
            this.brightenlinePointsForLandScapeMode.clear();
            this.brightenlinePointsForLandScapeMode.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 15.0f));
            this.brightenlinePointsForLandScapeMode.add(new PointF(20.0f, 219.0f));
            this.brightenlinePointsForLandScapeMode.add(new PointF(100.0f, 539.0f));
            this.brightenlinePointsForLandScapeMode.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePointsForLandScapeMode.add(new PointF(40000.0f, 989.0f));
            this.darkenlinePointsForLandScapeMode.clear();
            this.darkenlinePointsForLandScapeMode.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f));
            this.darkenlinePointsForLandScapeMode.add(new PointF(1.0f, 1.0f));
            this.darkenlinePointsForLandScapeMode.add(new PointF(20.0f, 20.0f));
            this.darkenlinePointsForLandScapeMode.add(new PointF(100.0f, 80.0f));
            this.darkenlinePointsForLandScapeMode.add(new PointF(1800.0f, 600.0f));
            this.darkenlinePointsForLandScapeMode.add(new PointF(40000.0f, 38800.0f));
            this.brightnessLevelToNitMappingEnable = false;
            this.brightnessLevelToNitLinePoints.clear();
            this.brightnessLevelToNitLinePoints.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 2.0f));
            this.brightnessLevelToNitLinePoints.add(new PointF(255.0f, 500.0f));
            this.resetAmbientLuxEnable = false;
            this.resetAmbientLuxTh = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.resetAmbientLuxThMin = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.resetAmbientLuxDarkenDebounceTime = 10000;
            this.resetAmbientLuxBrightenDebounceTime = 1000;
            this.resetAmbientLuxFastDarkenValidTime = 0;
            this.resetAmbientLuxDarkenRatio = 1.0f;
            this.resetAmbientLuxGraTime = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.resetAmbientLuxFastDarkenDimmingTime = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.resetAmbientLuxStartBrightness = 200.0f;
            this.resetAmbientLuxThMax = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.resetAmbientLuxStartBrightnessMax = 200.0f;
            this.resetAmbientLuxDisableBrightnessOffset = 40;
            this.brightnessChageUpDelayTime = 50;
            this.brightnessChageDownDelayTime = 200;
            this.brightnessChageDefaultDelayTime = 500;
            this.luxlinePointsForBrightnessLevelEnable = false;
            this.brightenlinePointsForBrightnessLevel.clear();
            this.brightenlinePointsForBrightnessLevel.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 2.0f));
            this.brightenlinePointsForBrightnessLevel.add(new PointF(255.0f, 5.0f));
            this.darkenlinePointsForBrightnessLevel.clear();
            this.darkenlinePointsForBrightnessLevel.add(new PointF(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 2.0f));
            this.darkenlinePointsForBrightnessLevel.add(new PointF(255.0f, 5.0f));
            this.secondDarkenModeEanble = false;
            this.secondDarkenModeMinLuxTh = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.secondDarkenModeMaxLuxTh = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.secondDarkenModeDarkenDeltaLuxRatio = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.secondDarkenModeDarkenDebounceTime = 0;
            this.brightnessOffsetLuxModeEnable = false;
            this.brightenOffsetLuxTh1 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.brightenOffsetLuxTh2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.brightenOffsetLuxTh3 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.brightenOffsetNoValidDarkenLuxTh1 = -1.0f;
            this.brightenOffsetNoValidDarkenLuxTh2 = -1.0f;
            this.brightenOffsetNoValidDarkenLuxTh3 = -1.0f;
            this.brightenOffsetNoValidDarkenLuxTh4 = -1.0f;
            this.brightenOffsetNoValidBrightenLuxTh1 = -1.0f;
            this.brightenOffsetNoValidBrightenLuxTh2 = -1.0f;
            this.brightenOffsetNoValidBrightenLuxTh3 = -1.0f;
            this.brightenOffsetNoValidBrightenLuxTh4 = -1.0f;
            this.darkenOffsetLuxTh1 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkenOffsetLuxTh2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkenOffsetLuxTh3 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.darkenOffsetNoValidBrightenLuxTh1 = -1.0f;
            this.darkenOffsetNoValidBrightenLuxTh2 = -1.0f;
            this.darkenOffsetNoValidBrightenLuxTh3 = -1.0f;
            this.darkenOffsetNoValidBrightenLuxTh4 = -1.0f;
            this.brightenOffsetEffectMinLuxEnable = false;
            this.brightnessOffsetTmpValidEnable = false;
            this.brightenOffsetNoValidSavedLuxTh1 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.brightenOffsetNoValidSavedLuxTh2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class ElementBrightenlinePointsForDcMode extends HwXmlElement {
        private ElementBrightenlinePointsForDcMode() {
        }

        public String getName() {
            return "BrightenlinePointsForDcMode";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class ElementBrightenlinePointsForDcModePoint extends HwXmlElement {
        private ElementBrightenlinePointsForDcModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightenlinePointsForDcMode.clear();
            }
            HwBrightnessXmlLoader.mData.brightenlinePointsForDcMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightenlinePointsForDcMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePointsForDcMode);
        }
    }

    private static class ElementDarkenlinePointsForDcMode extends HwXmlElement {
        private ElementDarkenlinePointsForDcMode() {
        }

        public String getName() {
            return "DarkenlinePointsForDcMode";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class ElementDarkenlinePointsForDcModePoint extends HwXmlElement {
        private ElementDarkenlinePointsForDcModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.darkenlinePointsForDcMode.clear();
            }
            HwBrightnessXmlLoader.mData.darkenlinePointsForDcMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.darkenlinePointsForDcMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePointsForDcMode);
        }
    }

    private static class ElementDcModeGroup extends HwXmlElement {
        private ElementDcModeGroup() {
        }

        public String getName() {
            return "DCModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"DcModeEnable", "DcModeBrightenDebounceTime", "DcModeDarkenDebounceTime", "DcModeLuxThresholdEnable"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0072  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0081  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x0090  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -387239045) {
                if (hashCode != 60040257) {
                    if (hashCode != 165315447) {
                        if (hashCode == 1378430725 && valueName.equals("DcModeEnable")) {
                            c = 0;
                            switch (c) {
                                case 0:
                                    HwBrightnessXmlLoader.mData.dcModeEnable = string2Boolean(parser.nextText());
                                    break;
                                case 1:
                                    HwBrightnessXmlLoader.mData.dcModeBrightenDebounceTime = string2Long(parser.nextText());
                                    break;
                                case 2:
                                    HwBrightnessXmlLoader.mData.dcModeDarkenDebounceTime = string2Long(parser.nextText());
                                    break;
                                case 3:
                                    HwBrightnessXmlLoader.mData.dcModeLuxThresholdEnable = string2Boolean(parser.nextText());
                                    break;
                                default:
                                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                                    return false;
                            }
                            return true;
                        }
                    } else if (valueName.equals("DcModeDarkenDebounceTime")) {
                        c = 2;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                        return true;
                    }
                } else if (valueName.equals("DcModeLuxThresholdEnable")) {
                    c = 3;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("DcModeBrightenDebounceTime")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.dcModeBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.dcModeDarkenDebounceTime >= 0;
        }
    }

    private static class Element_AmbientLuxValidBrightnessPoints extends HwXmlElement {
        private Element_AmbientLuxValidBrightnessPoints() {
        }

        public String getName() {
            return "AmbientLuxValidBrightnessPoints";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_AmbientLuxValidBrightnessPoints_Point extends HwXmlElement {
        private Element_AmbientLuxValidBrightnessPoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.ambientLuxValidBrightnessPoints.clear();
            }
            HwBrightnessXmlLoader.mData.ambientLuxValidBrightnessPoints = parseAmPointList(parser, HwBrightnessXmlLoader.mData.ambientLuxValidBrightnessPoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkAmPointsListIsOK(HwBrightnessXmlLoader.mData.ambientLuxValidBrightnessPoints);
        }
    }

    private static class Element_AnimateGroup extends HwXmlElement {
        private Element_AnimateGroup() {
        }

        public String getName() {
            return "AnimateGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"BrightenGradualTime", "DarkenGradualTime", "BrightenThresholdFor255", "DarkenTargetFor255", "DarkenCurrentFor255"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1498609295:
                    if (valueName.equals("BrightenThresholdFor255")) {
                        c = 2;
                        break;
                    }
                case 884036153:
                    if (valueName.equals("DarkenTargetFor255")) {
                        c = 3;
                        break;
                    }
                case 1455932156:
                    if (valueName.equals("BrightenGradualTime")) {
                        c = 0;
                        break;
                    }
                case 1739695104:
                    if (valueName.equals("DarkenGradualTime")) {
                        c = 1;
                        break;
                    }
                case 1877080899:
                    if (valueName.equals("DarkenCurrentFor255")) {
                        c = 4;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.brightenGradualTime = string2Float(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.darkenGradualTime = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.brightenThresholdFor255 = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.darkenTargetFor255 = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.darkenCurrentFor255 = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brightenGradualTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.darkenGradualTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_AnimateOptionalGroup extends HwXmlElement {
        private Element_AnimateOptionalGroup() {
        }

        public String getName() {
            return "AnimateOptionalGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"AutoFastTimeFor255", "ManualFastTimeFor255", "DimTime", "AnimationEqualRatioEnable", "ScreenBrightnessMinNit", "ScreenBrightnessMaxNit", "MinAnimatingStep"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1857716415:
                    if (valueName.equals("AutoFastTimeFor255")) {
                        c = 0;
                        break;
                    }
                case -964927659:
                    if (valueName.equals("DimTime")) {
                        c = 2;
                        break;
                    }
                case -935718286:
                    if (valueName.equals("ScreenBrightnessMaxNit")) {
                        c = 5;
                        break;
                    }
                case -928628028:
                    if (valueName.equals("ScreenBrightnessMinNit")) {
                        c = 4;
                        break;
                    }
                case -322892520:
                    if (valueName.equals("ManualFastTimeFor255")) {
                        c = 1;
                        break;
                    }
                case 1325505822:
                    if (valueName.equals("AnimationEqualRatioEnable")) {
                        c = 3;
                        break;
                    }
                case 1740888632:
                    if (valueName.equals("MinAnimatingStep")) {
                        c = 6;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.autoFastTimeFor255 = string2Float(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.manualFastTimeFor255 = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.dimTime = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.animationEqualRatioEnable = string2Boolean(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.screenBrightnessMinNit = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.screenBrightnessMaxNit = string2Float(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.minAnimatingStep = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.screenBrightnessMinNit > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.screenBrightnessMaxNit > HwBrightnessXmlLoader.mData.screenBrightnessMinNit;
        }
    }

    private static class Element_BackSensorCoverModeBrighnessLinePoints extends HwXmlElement {
        private Element_BackSensorCoverModeBrighnessLinePoints() {
        }

        public String getName() {
            return "BackSensorCoverModeBrighnessLinePoints";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.backSensorCoverModeEnable = true;
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }
    }

    private static class Element_BackSensorCoverModeBrighnessLinePoints_Point extends HwXmlElement {
        private Element_BackSensorCoverModeBrighnessLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.backSensorCoverModeBrighnessLinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.backSensorCoverModeBrighnessLinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.backSensorCoverModeBrighnessLinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.backSensorCoverModeBrighnessLinePoints);
        }
    }

    private static class Element_BrightenlinePoints extends HwXmlElement {
        private Element_BrightenlinePoints() {
        }

        public String getName() {
            return "BrightenlinePoints";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_BrightenlinePointsForBrightnessLevel extends HwXmlElement {
        private Element_BrightenlinePointsForBrightnessLevel() {
        }

        public String getName() {
            return "BrightenlinePointsForBrightnessLevel";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_BrightenlinePointsForBrightnessLevel_Point extends HwXmlElement {
        private Element_BrightenlinePointsForBrightnessLevel_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightenlinePointsForBrightnessLevel.clear();
            }
            HwBrightnessXmlLoader.mData.brightenlinePointsForBrightnessLevel = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightenlinePointsForBrightnessLevel);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePointsForBrightnessLevel);
        }
    }

    private static class Element_BrightenlinePointsForGameMode extends HwXmlElement {
        private Element_BrightenlinePointsForGameMode() {
        }

        public String getName() {
            return "BrightenlinePointsForGameMode";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_BrightenlinePointsForGameMode_Point extends HwXmlElement {
        private Element_BrightenlinePointsForGameMode_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightenlinePointsForGameMode.clear();
            }
            HwBrightnessXmlLoader.mData.brightenlinePointsForGameMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightenlinePointsForGameMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePointsForGameMode);
        }
    }

    private static class Element_BrightenlinePointsForLandScapeMode extends HwXmlElement {
        private Element_BrightenlinePointsForLandScapeMode() {
        }

        public String getName() {
            return "BrightenlinePointsForLandScapeMode";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_BrightenlinePointsForLandScapeMode_Point extends HwXmlElement {
        private Element_BrightenlinePointsForLandScapeMode_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightenlinePointsForLandScapeMode.clear();
            }
            HwBrightnessXmlLoader.mData.brightenlinePointsForLandScapeMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightenlinePointsForLandScapeMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePointsForLandScapeMode);
        }
    }

    private static class Element_BrightenlinePoints_Point extends HwXmlElement {
        private Element_BrightenlinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightenlinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.brightenlinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightenlinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePoints);
        }
    }

    private static class Element_BrightnessChangeGroup extends HwXmlElement {
        private Element_BrightnessChangeGroup() {
        }

        public String getName() {
            return "BrightnessChangeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"BrightnessChageUpDelayTime", "BrightnessChageDownDelayTime", "BrightnessChageDefaultDelayTime", "LuxlinePointsForBrightnessLevelEnable", "ResetAmbientLuxThMax", "ResetAmbientLuxStartBrightnessMax"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -648897270:
                    if (valueName.equals("ResetAmbientLuxThMax")) {
                        c = 4;
                        break;
                    }
                case -367820212:
                    if (valueName.equals("BrightnessChageUpDelayTime")) {
                        c = 0;
                        break;
                    }
                case -342481645:
                    if (valueName.equals("LuxlinePointsForBrightnessLevelEnable")) {
                        c = 3;
                        break;
                    }
                case -249351579:
                    if (valueName.equals("BrightnessChageDownDelayTime")) {
                        c = 1;
                        break;
                    }
                case -55027624:
                    if (valueName.equals("BrightnessChageDefaultDelayTime")) {
                        c = 2;
                        break;
                    }
                case 1208396183:
                    if (valueName.equals("ResetAmbientLuxStartBrightnessMax")) {
                        c = 5;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.brightnessChageUpDelayTime = string2Int(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.brightnessChageDownDelayTime = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.brightnessChageDefaultDelayTime = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.luxlinePointsForBrightnessLevelEnable = string2Boolean(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxThMax = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxStartBrightnessMax = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brightnessChageUpDelayTime >= 0 && HwBrightnessXmlLoader.mData.brightnessChageDownDelayTime >= 0 && HwBrightnessXmlLoader.mData.brightnessChageDefaultDelayTime >= 0 && HwBrightnessXmlLoader.mData.resetAmbientLuxThMax >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.resetAmbientLuxStartBrightnessMax > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_BrightnessLevelToNitLinePoints extends HwXmlElement {
        private Element_BrightnessLevelToNitLinePoints() {
        }

        public String getName() {
            return "BrightnessLevelToNitLinePoints";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_BrightnessLevelToNitLinePoints_Point extends HwXmlElement {
        private Element_BrightnessLevelToNitLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightnessLevelToNitLinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.brightnessLevelToNitLinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightnessLevelToNitLinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightnessLevelToNitLinePoints);
        }
    }

    private static class Element_BrightnessMappingPoints extends HwXmlElement {
        private Element_BrightnessMappingPoints() {
        }

        public String getName() {
            return "BrightnessMappingPoints";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_BrightnessMappingPoints_Point extends HwXmlElement {
        private Element_BrightnessMappingPoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightnessMappingPoints.clear();
            }
            HwBrightnessXmlLoader.mData.brightnessMappingPoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightnessMappingPoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightnessMappingPoints);
        }
    }

    private static class Element_BrightnessOffsetLuxGroup1 extends HwXmlElement {
        private Element_BrightnessOffsetLuxGroup1() {
        }

        public String getName() {
            return "BrightnessOffsetLuxGroup1";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"BrightnessOffsetLuxModeEnable", "BrightenOffsetLuxTh1", "BrightenOffsetLuxTh2", "BrightenOffsetLuxTh3", "BrightenOffsetNoValidDarkenLuxTh1", "BrightenOffsetNoValidDarkenLuxTh2", "BrightenOffsetNoValidDarkenLuxTh3", "BrightenOffsetNoValidDarkenLuxTh4", "BrightenOffsetNoValidBrightenLuxTh1", "BrightenOffsetNoValidBrightenLuxTh2", "BrightenOffsetNoValidBrightenLuxTh3", "BrightenOffsetNoValidBrightenLuxTh4"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x009c  */
        /* JADX WARNING: Removed duplicated region for block: B:45:0x00b4  */
        /* JADX WARNING: Removed duplicated region for block: B:46:0x00c4  */
        /* JADX WARNING: Removed duplicated region for block: B:47:0x00d4  */
        /* JADX WARNING: Removed duplicated region for block: B:48:0x00e4  */
        /* JADX WARNING: Removed duplicated region for block: B:49:0x00f4  */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x0103  */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0112  */
        /* JADX WARNING: Removed duplicated region for block: B:52:0x0121  */
        /* JADX WARNING: Removed duplicated region for block: B:53:0x0130  */
        /* JADX WARNING: Removed duplicated region for block: B:54:0x013f  */
        /* JADX WARNING: Removed duplicated region for block: B:55:0x014e  */
        /* JADX WARNING: Removed duplicated region for block: B:56:0x015d  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1845841807) {
                switch (hashCode) {
                    case -358379978:
                        if (valueName.equals("BrightenOffsetNoValidBrightenLuxTh1")) {
                            c = 8;
                            break;
                        }
                    case -358379977:
                        if (valueName.equals("BrightenOffsetNoValidBrightenLuxTh2")) {
                            c = 9;
                            break;
                        }
                    case -358379976:
                        if (valueName.equals("BrightenOffsetNoValidBrightenLuxTh3")) {
                            c = 10;
                            break;
                        }
                    case -358379975:
                        if (valueName.equals("BrightenOffsetNoValidBrightenLuxTh4")) {
                            c = 11;
                            break;
                        }
                    default:
                        switch (hashCode) {
                            case 52244466:
                                if (valueName.equals("BrightenOffsetNoValidDarkenLuxTh1")) {
                                    c = 4;
                                    break;
                                }
                            case 52244467:
                                if (valueName.equals("BrightenOffsetNoValidDarkenLuxTh2")) {
                                    c = 5;
                                    break;
                                }
                            case 52244468:
                                if (valueName.equals("BrightenOffsetNoValidDarkenLuxTh3")) {
                                    c = 6;
                                    break;
                                }
                            case 52244469:
                                if (valueName.equals("BrightenOffsetNoValidDarkenLuxTh4")) {
                                    c = 7;
                                    break;
                                }
                            default:
                                switch (hashCode) {
                                    case 1958157572:
                                        if (valueName.equals("BrightenOffsetLuxTh1")) {
                                            c = 1;
                                            break;
                                        }
                                    case 1958157573:
                                        if (valueName.equals("BrightenOffsetLuxTh2")) {
                                            c = 2;
                                            break;
                                        }
                                    case 1958157574:
                                        if (valueName.equals("BrightenOffsetLuxTh3")) {
                                            c = 3;
                                            break;
                                        }
                                }
                        }
                }
            } else if (valueName.equals("BrightnessOffsetLuxModeEnable")) {
                c = 0;
                switch (c) {
                    case 0:
                        HwBrightnessXmlLoader.mData.brightnessOffsetLuxModeEnable = string2Boolean(parser.nextText());
                        break;
                    case 1:
                        HwBrightnessXmlLoader.mData.brightenOffsetLuxTh1 = string2Float(parser.nextText());
                        break;
                    case 2:
                        HwBrightnessXmlLoader.mData.brightenOffsetLuxTh2 = string2Float(parser.nextText());
                        break;
                    case 3:
                        HwBrightnessXmlLoader.mData.brightenOffsetLuxTh3 = string2Float(parser.nextText());
                        break;
                    case 4:
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidDarkenLuxTh1 = string2Float(parser.nextText());
                        break;
                    case 5:
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidDarkenLuxTh2 = string2Float(parser.nextText());
                        break;
                    case 6:
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidDarkenLuxTh3 = string2Float(parser.nextText());
                        break;
                    case 7:
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidDarkenLuxTh4 = string2Float(parser.nextText());
                        break;
                    case 8:
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidBrightenLuxTh1 = string2Float(parser.nextText());
                        break;
                    case 9:
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidBrightenLuxTh2 = string2Float(parser.nextText());
                        break;
                    case 10:
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidBrightenLuxTh3 = string2Float(parser.nextText());
                        break;
                    case 11:
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidBrightenLuxTh4 = string2Float(parser.nextText());
                        break;
                    default:
                        Slog.e(this.TAG, "unknow valueName=" + valueName);
                        return false;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    break;
                case 9:
                    break;
                case 10:
                    break;
                case 11:
                    break;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brightenOffsetLuxTh1 >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.brightenOffsetLuxTh2 >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.brightenOffsetLuxTh3 >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_BrightnessOffsetLuxGroup2 extends HwXmlElement {
        private Element_BrightnessOffsetLuxGroup2() {
        }

        public String getName() {
            return "BrightnessOffsetLuxGroup2";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"DarkenOffsetLuxTh1", "DarkenOffsetLuxTh2", "DarkenOffsetLuxTh3", "DarkenOffsetNoValidBrightenLuxTh1", "DarkenOffsetNoValidBrightenLuxTh2", "DarkenOffsetNoValidBrightenLuxTh3", "DarkenOffsetNoValidBrightenLuxTh4", "BrightenOffsetEffectMinLuxEnable", "BrightnessOffsetTmpValidEnable", "BrightenOffsetNoValidSavedLuxTh1", "BrightenOffsetNoValidSavedLuxTh2"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2130092928:
                    if (valueName.equals("DarkenOffsetLuxTh1")) {
                        c = 0;
                        break;
                    }
                case -2130092927:
                    if (valueName.equals("DarkenOffsetLuxTh2")) {
                        c = 1;
                        break;
                    }
                case -2130092926:
                    if (valueName.equals("DarkenOffsetLuxTh3")) {
                        c = 2;
                        break;
                    }
                case -1739397318:
                    if (valueName.equals("DarkenOffsetNoValidBrightenLuxTh1")) {
                        c = 3;
                        break;
                    }
                case -1739397317:
                    if (valueName.equals("DarkenOffsetNoValidBrightenLuxTh2")) {
                        c = 4;
                        break;
                    }
                case -1739397316:
                    if (valueName.equals("DarkenOffsetNoValidBrightenLuxTh3")) {
                        c = 5;
                        break;
                    }
                case -1739397315:
                    if (valueName.equals("DarkenOffsetNoValidBrightenLuxTh4")) {
                        c = 6;
                        break;
                    }
                case -801130809:
                    if (valueName.equals("BrightenOffsetEffectMinLuxEnable")) {
                        c = 7;
                        break;
                    }
                case -48883792:
                    if (valueName.equals("BrightenOffsetNoValidSavedLuxTh1")) {
                        c = 9;
                        break;
                    }
                case -48883791:
                    if (valueName.equals("BrightenOffsetNoValidSavedLuxTh2")) {
                        c = 10;
                        break;
                    }
                case 1324590220:
                    if (valueName.equals("BrightnessOffsetTmpValidEnable")) {
                        c = 8;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.darkenOffsetLuxTh1 = string2Float(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.darkenOffsetLuxTh2 = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.darkenOffsetLuxTh3 = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.darkenOffsetNoValidBrightenLuxTh1 = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.darkenOffsetNoValidBrightenLuxTh2 = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.darkenOffsetNoValidBrightenLuxTh3 = string2Float(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.darkenOffsetNoValidBrightenLuxTh4 = string2Float(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.brightenOffsetEffectMinLuxEnable = string2Boolean(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.brightnessOffsetTmpValidEnable = string2Boolean(parser.nextText());
                    break;
                case 9:
                    HwBrightnessXmlLoader.mData.brightenOffsetNoValidSavedLuxTh1 = string2Float(parser.nextText());
                    break;
                case 10:
                    HwBrightnessXmlLoader.mData.brightenOffsetNoValidSavedLuxTh2 = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenOffsetLuxTh1 >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.darkenOffsetLuxTh2 >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.darkenOffsetLuxTh3 >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.brightenOffsetNoValidSavedLuxTh1 >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.brightenOffsetNoValidSavedLuxTh2 >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_CoverModeBrighnessLinePoints extends HwXmlElement {
        private Element_CoverModeBrighnessLinePoints() {
        }

        public String getName() {
            return "CoverModeBrighnessLinePoints";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }
    }

    private static class Element_CoverModeBrighnessLinePoints_Point extends HwXmlElement {
        private Element_CoverModeBrighnessLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.coverModeBrighnessLinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.coverModeBrighnessLinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.coverModeBrighnessLinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.coverModeBrighnessLinePoints);
        }
    }

    private static class Element_CoverModeGroup extends HwXmlElement {
        private Element_CoverModeGroup() {
        }

        public String getName() {
            return "CoverModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"CoverModeFirstLux", "LastCloseScreenEnable", "CoverModeBrightenResponseTime", "CoverModeDarkenResponseTime", "CoverModelastCloseScreenEnable", "CoverModeDayEnable", "CoverModeDayBrightness", "ConverModeDayBeginTime", "CoverModeDayEndTime", "BackSensorCoverModeMinLuxInRing"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2135500393:
                    if (valueName.equals("CoverModelastCloseScreenEnable")) {
                        c = 4;
                        break;
                    }
                case -2016036730:
                    if (valueName.equals("CoverModeDayEndTime")) {
                        c = 8;
                        break;
                    }
                case -1487844013:
                    if (valueName.equals("CoverModeDayBrightness")) {
                        c = 6;
                        break;
                    }
                case -1073894082:
                    if (valueName.equals("ConverModeDayBeginTime")) {
                        c = 7;
                        break;
                    }
                case -163823189:
                    if (valueName.equals("BackSensorCoverModeMinLuxInRing")) {
                        c = 9;
                        break;
                    }
                case 1181816709:
                    if (valueName.equals("CoverModeDayEnable")) {
                        c = 5;
                        break;
                    }
                case 1364232977:
                    if (valueName.equals("LastCloseScreenEnable")) {
                        c = 1;
                        break;
                    }
                case 1460433721:
                    if (valueName.equals("CoverModeFirstLux")) {
                        c = 0;
                        break;
                    }
                case 1871618155:
                    if (valueName.equals("CoverModeBrightenResponseTime")) {
                        c = 2;
                        break;
                    }
                case 2140032615:
                    if (valueName.equals("CoverModeDarkenResponseTime")) {
                        c = 3;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.coverModeFirstLux = string2Float(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.lastCloseScreenEnable = string2Boolean(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.coverModeBrightenResponseTime = string2Long(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.coverModeDarkenResponseTime = string2Long(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.coverModelastCloseScreenEnable = string2Boolean(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.coverModeDayEnable = string2Boolean(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.coverModeDayBrightness = string2Int(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.converModeDayBeginTime = string2Int(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.coverModeDayEndTime = string2Int(parser.nextText());
                    break;
                case 9:
                    HwBrightnessXmlLoader.mData.backSensorCoverModeMinLuxInRing = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.coverModeBrightenResponseTime >= 0 && HwBrightnessXmlLoader.mData.coverModeDarkenResponseTime >= 0 && HwBrightnessXmlLoader.mData.coverModeDayBrightness > 0 && HwBrightnessXmlLoader.mData.converModeDayBeginTime >= 0 && HwBrightnessXmlLoader.mData.converModeDayBeginTime < 24 && HwBrightnessXmlLoader.mData.coverModeDayEndTime >= 0 && HwBrightnessXmlLoader.mData.coverModeDayEndTime < 24 && HwBrightnessXmlLoader.mData.backSensorCoverModeMinLuxInRing >= 0;
        }
    }

    private static class Element_CryogenicGroup extends HwXmlElement {
        private Element_CryogenicGroup() {
        }

        public String getName() {
            return "CryogenicGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"CryogenicEnable", "CryogenicBrightnessMappingEnable", "CryogenicMaxBrightnessTimeOut", "CryogenicActiveScreenOffIntervalInMillis", "CryogenicLagTimeInMillis"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1946012229:
                    if (valueName.equals("CryogenicBrightnessMappingEnable")) {
                        c = 1;
                        break;
                    }
                case -1243020920:
                    if (valueName.equals("CryogenicEnable")) {
                        c = 0;
                        break;
                    }
                case 971881064:
                    if (valueName.equals("CryogenicActiveScreenOffIntervalInMillis")) {
                        c = 3;
                        break;
                    }
                case 1229222385:
                    if (valueName.equals("CryogenicMaxBrightnessTimeOut")) {
                        c = 2;
                        break;
                    }
                case 1592001029:
                    if (valueName.equals("CryogenicLagTimeInMillis")) {
                        c = 4;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.cryogenicEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.cryogenicModeBrightnessMappingEnable = string2Boolean(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.cryogenicMaxBrightnessTimeOut = string2Long(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.cryogenicActiveScreenOffIntervalInMillis = string2Long(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.cryogenicLagTimeInMillis = string2Long(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.cryogenicMaxBrightnessTimeOut > 0 && HwBrightnessXmlLoader.mData.cryogenicActiveScreenOffIntervalInMillis > 0 && HwBrightnessXmlLoader.mData.cryogenicLagTimeInMillis > 0;
        }
    }

    private static class Element_DarkAdapter extends HwXmlElement {
        private Element_DarkAdapter() {
        }

        public String getName() {
            return "DarkAdapter";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.darkAdapterEnable = true;
            return true;
        }
    }

    private static class Element_DarkAdapterGroup1 extends HwXmlElement {
        private Element_DarkAdapterGroup1() {
        }

        public String getName() {
            return "DarkAdapterGroup1";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"Unadapt2AdaptingShortFilterLux", "Unadapt2AdaptingLongFilterLux", "Unadapt2AdaptingLongFilterSec", "Unadapt2AdaptingDimSec", "Adapting2UnadaptShortFilterLux", "Adapting2AdaptedOffDurationMinSec", "Adapting2AdaptedOffDurationFilterSec", "Adapting2AdaptedOffDurationMaxSec", "Adapting2AdaptedOnClockNoFilterBeginHour", "Adapting2AdaptedOnClockNoFilterEndHour"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1713045852:
                    if (valueName.equals("Unadapt2AdaptingShortFilterLux")) {
                        c = 0;
                        break;
                    }
                case -1656034879:
                    if (valueName.equals("Adapting2AdaptedOffDurationMaxSec")) {
                        c = 7;
                        break;
                    }
                case -1648944621:
                    if (valueName.equals("Adapting2AdaptedOffDurationMinSec")) {
                        c = 5;
                        break;
                    }
                case -1292961038:
                    if (valueName.equals("Unadapt2AdaptingLongFilterLux")) {
                        c = 1;
                        break;
                    }
                case -1292954828:
                    if (valueName.equals("Unadapt2AdaptingLongFilterSec")) {
                        c = 2;
                        break;
                    }
                case 557886292:
                    if (valueName.equals("Adapting2AdaptedOnClockNoFilterBeginHour")) {
                        c = 8;
                        break;
                    }
                case 758345798:
                    if (valueName.equals("Adapting2AdaptedOnClockNoFilterEndHour")) {
                        c = 9;
                        break;
                    }
                case 824735218:
                    if (valueName.equals("Unadapt2AdaptingDimSec")) {
                        c = 3;
                        break;
                    }
                case 896190834:
                    if (valueName.equals("Adapting2UnadaptShortFilterLux")) {
                        c = 4;
                        break;
                    }
                case 1064291269:
                    if (valueName.equals("Adapting2AdaptedOffDurationFilterSec")) {
                        c = 6;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.unadapt2AdaptingShortFilterLux = string2Float(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.unadapt2AdaptingLongFilterLux = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.unadapt2AdaptingLongFilterSec = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.unadapt2AdaptingDimSec = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.adapting2UnadaptShortFilterLux = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMinSec = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationFilterSec = string2Int(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMaxSec = string2Int(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterBeginHour = string2Int(parser.nextText());
                    break;
                case 9:
                    HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterEndHour = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.unadapt2AdaptingShortFilterLux > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.unadapt2AdaptingLongFilterLux > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.unadapt2AdaptingLongFilterSec > 0 && HwBrightnessXmlLoader.mData.unadapt2AdaptingDimSec > 0 && HwBrightnessXmlLoader.mData.adapting2UnadaptShortFilterLux > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMinSec > 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationFilterSec > 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationFilterSec > HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMinSec && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMaxSec > 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMaxSec > HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationFilterSec && HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterBeginHour >= 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterBeginHour < 24 && HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterEndHour >= 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterEndHour < 24;
        }
    }

    private static class Element_DarkAdapterGroup2 extends HwXmlElement {
        private Element_DarkAdapterGroup2() {
        }

        public String getName() {
            return "DarkAdapterGroup2";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"Unadapt2AdaptedShortFilterLux", "Unadapt2AdaptedOffDurationMinSec", "Unadapt2AdaptedOnClockNoFilterBeginHour", "Unadapt2AdaptedOnClockNoFilterEndHour", "Adapted2UnadaptShortFilterLux"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1872793361:
                    if (valueName.equals("Unadapt2AdaptedOnClockNoFilterEndHour")) {
                        c = 3;
                        break;
                    }
                case -966441933:
                    if (valueName.equals("Adapted2UnadaptShortFilterLux")) {
                        c = 4;
                        break;
                    }
                case -698098637:
                    if (valueName.equals("Unadapt2AdaptedShortFilterLux")) {
                        c = 0;
                        break;
                    }
                case 603296714:
                    if (valueName.equals("Unadapt2AdaptedOffDurationMinSec")) {
                        c = 1;
                        break;
                    }
                case 1768891837:
                    if (valueName.equals("Unadapt2AdaptedOnClockNoFilterBeginHour")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.unadapt2AdaptedShortFilterLux = string2Float(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.unadapt2AdaptedOffDurationMinSec = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterBeginHour = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterEndHour = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.adapted2UnadaptShortFilterLux = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.unadapt2AdaptedShortFilterLux > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.unadapt2AdaptedOffDurationMinSec > 0 && HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterBeginHour >= 0 && HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterBeginHour < 24 && HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterEndHour >= 0 && HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterEndHour < 24 && HwBrightnessXmlLoader.mData.adapted2UnadaptShortFilterLux > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_DarkLightGroup extends HwXmlElement {
        private Element_DarkLightGroup() {
        }

        public String getName() {
            return "DarkLightGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"DarkLightLevelMinThreshold", "DarkLightLevelMaxThreshold", "DarkLightLevelRatio", "DarkLightLuxMinThreshold", "DarkLightLuxMaxThreshold", "DarkLightLuxDelta"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1821009752:
                    if (valueName.equals("DarkLightLuxMinThreshold")) {
                        c = 3;
                        break;
                    }
                case -1305576921:
                    if (valueName.equals("DarkLightLevelRatio")) {
                        c = 2;
                        break;
                    }
                case 739225035:
                    if (valueName.equals("DarkLightLevelMaxThreshold")) {
                        c = 1;
                        break;
                    }
                case 1213651101:
                    if (valueName.equals("DarkLightLevelMinThreshold")) {
                        c = 0;
                        break;
                    }
                case 1899056169:
                    if (valueName.equals("DarkLightLuxDelta")) {
                        c = 5;
                        break;
                    }
                case 1999531478:
                    if (valueName.equals("DarkLightLuxMaxThreshold")) {
                        c = 4;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.darkLightLevelMinThreshold = string2Int(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.darkLightLevelMaxThreshold = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.darkLightLevelRatio = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.darkLightLuxMinThreshold = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.darkLightLuxMaxThreshold = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.darkLightLuxDelta = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkLightLevelMinThreshold >= 0 && HwBrightnessXmlLoader.mData.darkLightLevelMaxThreshold >= 0 && HwBrightnessXmlLoader.mData.darkLightLevelRatio > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.darkLightLuxMinThreshold >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.darkLightLuxMaxThreshold >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_DarkenlinePoints extends HwXmlElement {
        private Element_DarkenlinePoints() {
        }

        public String getName() {
            return "DarkenlinePoints";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_DarkenlinePointsForBrightnessLevel extends HwXmlElement {
        private Element_DarkenlinePointsForBrightnessLevel() {
        }

        public String getName() {
            return "DarkenlinePointsForBrightnessLevel";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_DarkenlinePointsForBrightnessLevel_Point extends HwXmlElement {
        private Element_DarkenlinePointsForBrightnessLevel_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.darkenlinePointsForBrightnessLevel.clear();
            }
            HwBrightnessXmlLoader.mData.darkenlinePointsForBrightnessLevel = parsePointFList(parser, HwBrightnessXmlLoader.mData.darkenlinePointsForBrightnessLevel);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePointsForBrightnessLevel);
        }
    }

    private static class Element_DarkenlinePointsForGameMode extends HwXmlElement {
        private Element_DarkenlinePointsForGameMode() {
        }

        public String getName() {
            return "DarkenlinePointsForGameMode";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_DarkenlinePointsForGameMode_Point extends HwXmlElement {
        private Element_DarkenlinePointsForGameMode_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.darkenlinePointsForGameMode.clear();
            }
            HwBrightnessXmlLoader.mData.darkenlinePointsForGameMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.darkenlinePointsForGameMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePointsForGameMode);
        }
    }

    private static class Element_DarkenlinePointsForLandScapeMode extends HwXmlElement {
        private Element_DarkenlinePointsForLandScapeMode() {
        }

        public String getName() {
            return "DarkenlinePointsForLandScapeMode";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_DarkenlinePointsForLandScapeMode_Point extends HwXmlElement {
        private Element_DarkenlinePointsForLandScapeMode_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.darkenlinePointsForLandScapeMode.clear();
            }
            HwBrightnessXmlLoader.mData.darkenlinePointsForLandScapeMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.darkenlinePointsForLandScapeMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePointsForLandScapeMode);
        }
    }

    private static class Element_DarkenlinePoints_Point extends HwXmlElement {
        private Element_DarkenlinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.darkenlinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.darkenlinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.darkenlinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePoints);
        }
    }

    private static class Element_DayModeGroup extends HwXmlElement {
        private Element_DayModeGroup() {
        }

        public String getName() {
            return "DayModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"DayModeAlgoEnable", "DayModeSwitchTime", "DayModeBeginTime", "DayModeEndTime", "DayModeModifyNumPoint", "DayModeModifyMinBrightness", "DayModeDarkenMinLux"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1913291113:
                    if (valueName.equals("DayModeBeginTime")) {
                        c = 2;
                        break;
                    }
                case -1608117751:
                    if (valueName.equals("DayModeEndTime")) {
                        c = 3;
                        break;
                    }
                case -818760726:
                    if (valueName.equals("DayModeModifyMinBrightness")) {
                        c = 5;
                        break;
                    }
                case -680246085:
                    if (valueName.equals("DayModeDarkenMinLux")) {
                        c = 6;
                        break;
                    }
                case -631473984:
                    if (valueName.equals("DayModeSwitchTime")) {
                        c = 1;
                        break;
                    }
                case 1126774357:
                    if (valueName.equals("DayModeAlgoEnable")) {
                        c = 0;
                        break;
                    }
                case 1716702627:
                    if (valueName.equals("DayModeModifyNumPoint")) {
                        c = 4;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.dayModeAlgoEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.dayModeSwitchTime = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.dayModeBeginTime = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.dayModeEndTime = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.dayModeModifyNumPoint = string2Int(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.dayModeModifyMinBrightness = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.dayModeDarkenMinLux = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.dayModeSwitchTime > 0 && HwBrightnessXmlLoader.mData.dayModeBeginTime >= 0 && HwBrightnessXmlLoader.mData.dayModeBeginTime < 24 && HwBrightnessXmlLoader.mData.dayModeEndTime >= 0 && HwBrightnessXmlLoader.mData.dayModeEndTime < 24 && HwBrightnessXmlLoader.mData.dayModeModifyNumPoint > 0 && HwBrightnessXmlLoader.mData.dayModeModifyMinBrightness >= 4 && HwBrightnessXmlLoader.mData.dayModeDarkenMinLux >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_DefaultBrightnessPoints extends HwXmlElement {
        private Element_DefaultBrightnessPoints() {
        }

        public String getName() {
            return "DefaultBrightnessPoints";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_DefaultBrightnessPoints_Point extends HwXmlElement {
        private Element_DefaultBrightnessPoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.defaultBrighnessLinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.defaultBrighnessLinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.defaultBrighnessLinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.defaultBrighnessLinePoints);
        }
    }

    private static class Element_GameModeAmbientLuxValidBrightnessPoints extends HwXmlElement {
        private Element_GameModeAmbientLuxValidBrightnessPoints() {
        }

        public String getName() {
            return "GameModeAmbientLuxValidBrightnessPoints";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.gameModeOffsetValidAmbientLuxEnable = true;
            return true;
        }
    }

    private static class Element_GameModeAmbientLuxValidBrightnessPoints_Point extends HwXmlElement {
        private Element_GameModeAmbientLuxValidBrightnessPoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.gameModeAmbientLuxValidBrightnessPoints.clear();
            }
            HwBrightnessXmlLoader.mData.gameModeAmbientLuxValidBrightnessPoints = parseAmPointList(parser, HwBrightnessXmlLoader.mData.gameModeAmbientLuxValidBrightnessPoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkAmPointsListIsOK(HwBrightnessXmlLoader.mData.gameModeAmbientLuxValidBrightnessPoints);
        }
    }

    private static class Element_GameModeGroup extends HwXmlElement {
        private Element_GameModeGroup() {
        }

        public String getName() {
            return "VehicleModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"GameModeEnable", "GameModeBrightenAnimationTime", "GameModeDarkentenAnimationTime", "GameModeDarkentenLongAnimationTime", "GameModeDarkentenLongTarget", "GameModeDarkentenLongCurrent", "GameModeClearOffsetTime", "GameModeBrightenDebounceTime", "GameModeDarkenDebounceTime", "GameModeLuxThresholdEnable", "GameModeBrightnessLimitationEnable", "GameModeBrightnessFloor"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1916112012:
                    if (valueName.equals("GameModeLuxThresholdEnable")) {
                        c = 9;
                        break;
                    }
                case -1810836822:
                    if (valueName.equals("GameModeDarkenDebounceTime")) {
                        c = 8;
                        break;
                    }
                case -1148292424:
                    if (valueName.equals("GameModeEnable")) {
                        c = 0;
                        break;
                    }
                case -1094024722:
                    if (valueName.equals("GameModeBrightenDebounceTime")) {
                        c = 7;
                        break;
                    }
                case -963189976:
                    if (valueName.equals("GameModeDarkentenAnimationTime")) {
                        c = 2;
                        break;
                    }
                case -407732679:
                    if (valueName.equals("GameModeBrightenAnimationTime")) {
                        c = 1;
                        break;
                    }
                case -75444058:
                    if (valueName.equals("GameModeBrightnessFloor")) {
                        c = 11;
                        break;
                    }
                case 1171599316:
                    if (valueName.equals("GameModeDarkentenLongCurrent")) {
                        c = 5;
                        break;
                    }
                case 1174892899:
                    if (valueName.equals("GameModeBrightnessLimitationEnable")) {
                        c = 10;
                        break;
                    }
                case 1419525784:
                    if (valueName.equals("GameModeClearOffsetTime")) {
                        c = 6;
                        break;
                    }
                case 1832157836:
                    if (valueName.equals("GameModeDarkentenLongAnimationTime")) {
                        c = 3;
                        break;
                    }
                case 2030028758:
                    if (valueName.equals("GameModeDarkentenLongTarget")) {
                        c = 4;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.gameModeEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.gameModeBrightenAnimationTime = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.gameModeDarkentenAnimationTime = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.gameModeDarkentenLongAnimationTime = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.gameModeDarkentenLongTarget = string2Int(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.gameModeDarkentenLongCurrent = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.gameModeClearOffsetTime = string2Long(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.gameModeBrightenDebounceTime = string2Long(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.gameModeDarkenDebounceTime = string2Long(parser.nextText());
                    break;
                case 9:
                    HwBrightnessXmlLoader.mData.gameModeLuxThresholdEnable = string2Boolean(parser.nextText());
                    break;
                case 10:
                    HwBrightnessXmlLoader.mData.gameModeBrightnessLimitationEnable = string2Boolean(parser.nextText());
                    break;
                case 11:
                    HwBrightnessXmlLoader.mData.gameModeBrightnessFloor = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.gameModeBrightenAnimationTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.gameModeDarkentenAnimationTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.gameModeDarkentenLongAnimationTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.gameModeDarkentenLongTarget >= 0 && HwBrightnessXmlLoader.mData.gameModeDarkentenLongCurrent >= 0 && HwBrightnessXmlLoader.mData.gameModeClearOffsetTime >= 0 && HwBrightnessXmlLoader.mData.gameModeBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.gameModeDarkenDebounceTime >= 0;
        }
    }

    private static class Element_InitGroup extends HwXmlElement {
        private Element_InitGroup() {
        }

        public String getName() {
            return "InitGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"InitDoubleSensorInterfere", "InitNumLastBuffer", "InitValidCloseTime", "InitUpperLuxThreshold", "InitSigmoidFuncSlope", "InitSlowReponseUpperLuxThreshold", "InitSlowReponseBrightTime"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2087819444:
                    if (valueName.equals("InitNumLastBuffer")) {
                        c = 1;
                        break;
                    }
                case -2027950061:
                    if (valueName.equals("InitDoubleSensorInterfere")) {
                        c = 0;
                        break;
                    }
                case -1707591794:
                    if (valueName.equals("InitUpperLuxThreshold")) {
                        c = 3;
                        break;
                    }
                case 416004804:
                    if (valueName.equals("InitSlowReponseBrightTime")) {
                        c = 6;
                        break;
                    }
                case 672175713:
                    if (valueName.equals("InitSlowReponseUpperLuxThreshold")) {
                        c = 5;
                        break;
                    }
                case 1178571689:
                    if (valueName.equals("InitSigmoidFuncSlope")) {
                        c = 4;
                        break;
                    }
                case 1260225945:
                    if (valueName.equals("InitValidCloseTime")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.initDoubleSensorInterfere = string2Float(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.initNumLastBuffer = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.initValidCloseTime = (long) string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.initUpperLuxThreshold = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.initSigmoidFuncSlope = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.initSlowReponseUpperLuxThreshold = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.initSlowReponseBrightTime = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.initDoubleSensorInterfere > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.initUpperLuxThreshold > 0 && HwBrightnessXmlLoader.mData.initSigmoidFuncSlope > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.initSlowReponseUpperLuxThreshold > 0;
        }
    }

    private static class Element_KeyguardResponseGroup extends HwXmlElement {
        private Element_KeyguardResponseGroup() {
        }

        public String getName() {
            return "KeyguardResponseGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"KeyguardResponseBrightenTime", "KeyguardResponseDarkenTime", "KeyguardAnimationBrightenTime", "KeyguardAnimationDarkenTime", "KeyguardLuxThreshold"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1788786797:
                    if (valueName.equals("KeyguardResponseDarkenTime")) {
                        c = 1;
                        break;
                    }
                case -662444278:
                    if (valueName.equals("KeyguardAnimationDarkenTime")) {
                        c = 3;
                        break;
                    }
                case 884106242:
                    if (valueName.equals("KeyguardLuxThreshold")) {
                        c = 4;
                        break;
                    }
                case 1083838039:
                    if (valueName.equals("KeyguardResponseBrightenTime")) {
                        c = 0;
                        break;
                    }
                case 1167240206:
                    if (valueName.equals("KeyguardAnimationBrightenTime")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.keyguardResponseBrightenTime = string2Int(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.keyguardResponseDarkenTime = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.keyguardAnimationBrightenTime = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.keyguardAnimationDarkenTime = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.keyguardLuxThreshold = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }
    }

    private class Element_LABCConfig extends HwXmlElement {
        private boolean mParseStarted;

        private Element_LABCConfig() {
        }

        public String getName() {
            return HwBrightnessXmlLoader.XML_NAME_NOEXT;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (this.mParseStarted) {
                return false;
            }
            if (HwBrightnessXmlLoader.this.mDeviceActualBrightnessLevel == 0) {
                if (this.HWFLOW) {
                    Slog.i(this.TAG, "actualDeviceLevel = 0, load started");
                }
                this.mParseStarted = true;
                return true;
            }
            String deviceLevelString = parser.getAttributeValue(null, MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
            if (deviceLevelString == null || deviceLevelString.length() == 0) {
                if (this.HWFLOW) {
                    String str = this.TAG;
                    Slog.i(str, "actualDeviceLevel = " + HwBrightnessXmlLoader.this.mDeviceActualBrightnessLevel + ", but can't find level in XML, load start");
                }
                this.mParseStarted = true;
                return true;
            } else if (string2Int(deviceLevelString) != HwBrightnessXmlLoader.this.mDeviceActualBrightnessLevel) {
                return false;
            } else {
                if (this.HWFLOW) {
                    String str2 = this.TAG;
                    Slog.i(str2, "actualDeviceLevel = " + HwBrightnessXmlLoader.this.mDeviceActualBrightnessLevel + ", find matched level in XML, load start");
                }
                this.mParseStarted = true;
                return true;
            }
        }
    }

    private static class Element_LandScapeModeGroup extends HwXmlElement {
        private Element_LandScapeModeGroup() {
        }

        public String getName() {
            return "OrientationStateGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"LandScapeBrightnessModeEnable", "LandScapeModeBrightenDebounceTime", "LandScapeModeDarkenDebounceTime", "LandScapeModeEnterDelayTime", "LandScapeModeQuitDelayTime", "LandScapeModeUseTouchProximity"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1717137325:
                    if (valueName.equals("LandScapeModeDarkenDebounceTime")) {
                        c = 2;
                        break;
                    }
                case -1348496042:
                    if (valueName.equals("LandScapeModeEnterDelayTime")) {
                        c = 3;
                        break;
                    }
                case -1243121321:
                    if (valueName.equals("LandScapeModeBrightenDebounceTime")) {
                        c = 1;
                        break;
                    }
                case 169027410:
                    if (valueName.equals("LandScapeBrightnessModeEnable")) {
                        c = 0;
                        break;
                    }
                case 294528835:
                    if (valueName.equals("LandScapeModeQuitDelayTime")) {
                        c = 4;
                        break;
                    }
                case 1531419209:
                    if (valueName.equals("LandScapeModeUseTouchProximity")) {
                        c = 5;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.landScapeBrightnessModeEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.landScapeModeBrightenDebounceTime = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.landScapeModeDarkenDebounceTime = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.landScapeModeEnterDelayTime = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.landScapeModeQuitDelayTime = string2Int(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.landScapeModeUseTouchProximity = string2Boolean(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.landScapeModeBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.landScapeModeDarkenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.landScapeModeEnterDelayTime >= 0 && HwBrightnessXmlLoader.mData.landScapeModeQuitDelayTime >= 0;
        }
    }

    private static class Element_ManualBrightenLinePoints extends HwXmlElement {
        private Element_ManualBrightenLinePoints() {
        }

        public String getName() {
            return "ManualBrightenLinePoints";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_ManualBrightenLinePoints_Point extends HwXmlElement {
        private Element_ManualBrightenLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.manualBrightenlinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.manualBrightenlinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.manualBrightenlinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.manualBrightenlinePoints);
        }
    }

    private static class Element_ManualDarkenLinePoints extends HwXmlElement {
        private Element_ManualDarkenLinePoints() {
        }

        public String getName() {
            return "ManualDarkenLinePoints";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_ManualDarkenLinePoints_Point extends HwXmlElement {
        private Element_ManualDarkenLinePoints_Point() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.manualDarkenlinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.manualDarkenlinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.manualDarkenlinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.manualDarkenlinePoints);
        }
    }

    private static class Element_ManualGroup extends HwXmlElement {
        private Element_ManualGroup() {
        }

        public String getName() {
            return "ManualGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"ManualMode", "ManualBrightnessMaxLimit", "ManualBrightnessMinLimit", "OutDoorThreshold", "InDoorThreshold", "ManualBrighenDebounceTime", "ManualDarkenDebounceTime", "QRCodeBrightnessminLimit"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            boolean z = false;
            switch (valueName.hashCode()) {
                case -1693268306:
                    if (valueName.equals("ManualBrightnessMaxLimit")) {
                        c = 1;
                        break;
                    }
                case -790346984:
                    if (valueName.equals("InDoorThreshold")) {
                        c = 4;
                        break;
                    }
                case -585412741:
                    if (valueName.equals("ManualDarkenDebounceTime")) {
                        c = 6;
                        break;
                    }
                case -139934680:
                    if (valueName.equals("QRCodeBrightnessminLimit")) {
                        c = 7;
                        break;
                    }
                case 346705267:
                    if (valueName.equals("ManualBrighenDebounceTime")) {
                        c = 5;
                        break;
                    }
                case 597434025:
                    if (valueName.equals("ManualMode")) {
                        c = 0;
                        break;
                    }
                case 701544399:
                    if (valueName.equals("OutDoorThreshold")) {
                        c = 3;
                        break;
                    }
                case 825502336:
                    if (valueName.equals("ManualBrightnessMinLimit")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    Data access$8600 = HwBrightnessXmlLoader.mData;
                    if (string2Int(parser.nextText()) == 1) {
                        z = true;
                    }
                    access$8600.manualMode = z;
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.manualBrightnessMaxLimit = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.manualBrightnessMinLimit = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.outDoorThreshold = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.inDoorThreshold = string2Int(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.manualBrighenDebounceTime = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.manualDarkenDebounceTime = string2Int(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.QRCodeBrightnessminLimit = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.manualBrightnessMaxLimit > 0 && HwBrightnessXmlLoader.mData.manualBrightnessMinLimit <= 255 && HwBrightnessXmlLoader.mData.inDoorThreshold <= HwBrightnessXmlLoader.mData.outDoorThreshold && HwBrightnessXmlLoader.mData.manualBrighenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.manualDarkenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.QRCodeBrightnessminLimit <= 255;
        }
    }

    private static class Element_ManualOptionalGroup extends HwXmlElement {
        private Element_ManualOptionalGroup() {
        }

        public String getName() {
            return "ManualOptionalGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"ManualAnimationBrightenTime", "ManualAnimationDarkenTime"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0054  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1800575254) {
                if (hashCode == -1654934546 && valueName.equals("ManualAnimationBrightenTime")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            HwBrightnessXmlLoader.mData.manualAnimationBrightenTime = string2Float(parser.nextText());
                            break;
                        case 1:
                            HwBrightnessXmlLoader.mData.manualAnimationDarkenTime = string2Float(parser.nextText());
                            break;
                        default:
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                    }
                    return true;
                }
            } else if (valueName.equals("ManualAnimationDarkenTime")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.manualAnimationBrightenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.manualAnimationDarkenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_MiscGroup extends HwXmlElement {
        private Element_MiscGroup() {
        }

        public String getName() {
            return "MiscGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"lightSensorRateMills", "DefaultBrightness"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
        /* JADX WARNING: Removed duplicated region for block: B:14:0x0045  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x0054  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != 604635474) {
                if (hashCode == 803831879 && valueName.equals("lightSensorRateMills")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            HwBrightnessXmlLoader.mData.lightSensorRateMills = string2Int(parser.nextText());
                            break;
                        case 1:
                            HwBrightnessXmlLoader.mData.defaultBrightness = string2Float(parser.nextText());
                            break;
                        default:
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                    }
                    return true;
                }
            } else if (valueName.equals("DefaultBrightness")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.defaultBrightness > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_MiscOptionalGroup1 extends HwXmlElement {
        private Element_MiscOptionalGroup1() {
        }

        public String getName() {
            return "MiscOptionalGroup1";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"RebootAutoModeEnable", "PostMaxMinAvgFilterNoFilterNum", "PostMaxMinAvgFilterNum", "PowerOnFastResponseLuxNum", "PowerOnBrightenDebounceTime", "PowerOnDarkenDebounceTime", "CameraModeEnable", "CameraAnimationTime", "ProximityLuxThreshold", "ProximityResponseBrightenTime", "AnimatingForRGBWEnable", "MonitorEnable", "BrightnessCalibrationEnabled"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2080828579:
                    if (valueName.equals("MonitorEnable")) {
                        c = 11;
                        break;
                    }
                case -1912197839:
                    if (valueName.equals("PostMaxMinAvgFilterNoFilterNum")) {
                        c = 1;
                        break;
                    }
                case -1540132752:
                    if (valueName.equals("ProximityResponseBrightenTime")) {
                        c = 9;
                        break;
                    }
                case -1498785192:
                    if (valueName.equals("AnimatingForRGBWEnable")) {
                        c = 10;
                        break;
                    }
                case -1271211816:
                    if (valueName.equals("PowerOnFastResponseLuxNum")) {
                        c = 3;
                        break;
                    }
                case -457425365:
                    if (valueName.equals("CameraModeEnable")) {
                        c = 6;
                        break;
                    }
                case -416433876:
                    if (valueName.equals("CameraAnimationTime")) {
                        c = 7;
                        break;
                    }
                case -70231992:
                    if (valueName.equals("BrightnessCalibrationEnabled")) {
                        c = 12;
                        break;
                    }
                case 782050106:
                    if (valueName.equals("RebootAutoModeEnable")) {
                        c = 0;
                        break;
                    }
                case 1554640189:
                    if (valueName.equals("PowerOnBrightenDebounceTime")) {
                        c = 4;
                        break;
                    }
                case 1784147739:
                    if (valueName.equals("ProximityLuxThreshold")) {
                        c = 8;
                        break;
                    }
                case 1960905866:
                    if (valueName.equals("PostMaxMinAvgFilterNum")) {
                        c = 2;
                        break;
                    }
                case 1981859257:
                    if (valueName.equals("PowerOnDarkenDebounceTime")) {
                        c = 5;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.rebootAutoModeEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNoFilterNum = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNum = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.powerOnFastResponseLuxNum = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.powerOnBrightenDebounceTime = string2Int(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.powerOnDarkenDebounceTime = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.cameraModeEnable = string2Boolean(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.cameraAnimationTime = string2Float(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.proximityLuxThreshold = string2Float(parser.nextText());
                    break;
                case 9:
                    HwBrightnessXmlLoader.mData.proximityResponseBrightenTime = string2Int(parser.nextText());
                    break;
                case 10:
                    HwBrightnessXmlLoader.mData.animatingForRGBWEnable = string2Boolean(parser.nextText());
                    break;
                case 11:
                    HwBrightnessXmlLoader.mData.monitorEnable = string2Boolean(parser.nextText());
                    break;
                case 12:
                    HwBrightnessXmlLoader.mData.brightnessCalibrationEnabled = string2Boolean(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNum > 0 && HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNum <= HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNoFilterNum && HwBrightnessXmlLoader.mData.powerOnFastResponseLuxNum > 0 && HwBrightnessXmlLoader.mData.powerOnBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.powerOnDarkenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.cameraAnimationTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && ((double) HwBrightnessXmlLoader.mData.proximityLuxThreshold) > 1.0E-6d;
        }
    }

    private static class Element_MiscOptionalGroup2 extends HwXmlElement {
        private Element_MiscOptionalGroup2() {
        }

        public String getName() {
            return "MiscOptionalGroup2";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"ReportValueWhenSensorOnChange", "PgReregisterScene", "BrightnessLevelToNitMappingEnable"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x0054  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0072  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -2012946113) {
                if (hashCode != 582429158) {
                    if (hashCode == 1259478400 && valueName.equals("ReportValueWhenSensorOnChange")) {
                        c = 0;
                        switch (c) {
                            case 0:
                                HwBrightnessXmlLoader.mData.reportValueWhenSensorOnChange = string2Boolean(parser.nextText());
                                break;
                            case 1:
                                HwBrightnessXmlLoader.mData.pgReregisterScene = string2Boolean(parser.nextText());
                                break;
                            case 2:
                                HwBrightnessXmlLoader.mData.brightnessLevelToNitMappingEnable = string2Boolean(parser.nextText());
                                break;
                            default:
                                Slog.e(this.TAG, "unknow valueName=" + valueName);
                                return false;
                        }
                        return true;
                    }
                } else if (valueName.equals("BrightnessLevelToNitMappingEnable")) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("PgReregisterScene")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
            return true;
        }
    }

    private static class Element_OffsetGroup extends HwXmlElement {
        private Element_OffsetGroup() {
        }

        public String getName() {
            return "OffsetGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"OffsetBrightenDebounceTime", "OffsetDarkenDebounceTime", "OffsetValidAmbientLuxEnable", "AutoModeInOutDoorLimitEnble"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0072  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0081  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x0090  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1841552692) {
                if (hashCode != -1168465949) {
                    if (hashCode != 110170888) {
                        if (hashCode == 1853282876 && valueName.equals("AutoModeInOutDoorLimitEnble")) {
                            c = 3;
                            switch (c) {
                                case 0:
                                    HwBrightnessXmlLoader.mData.offsetBrightenDebounceTime = string2Int(parser.nextText());
                                    break;
                                case 1:
                                    HwBrightnessXmlLoader.mData.offsetDarkenDebounceTime = string2Int(parser.nextText());
                                    break;
                                case 2:
                                    HwBrightnessXmlLoader.mData.offsetValidAmbientLuxEnable = string2Boolean(parser.nextText());
                                    break;
                                case 3:
                                    HwBrightnessXmlLoader.mData.autoModeInOutDoorLimitEnble = string2Boolean(parser.nextText());
                                    break;
                                default:
                                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                                    return false;
                            }
                            return true;
                        }
                    } else if (valueName.equals("OffsetDarkenDebounceTime")) {
                        c = 1;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                        return true;
                    }
                } else if (valueName.equals("OffsetValidAmbientLuxEnable")) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("OffsetBrightenDebounceTime")) {
                c = 0;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.offsetBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.offsetDarkenDebounceTime >= 0;
        }
    }

    private static class Element_OffsetResetGroup extends HwXmlElement {
        private Element_OffsetResetGroup() {
        }

        public String getName() {
            return "OffsetResetGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"OffsetResetSwitchTime", "OffsetResetEnable", "OffsetResetShortSwitchTime", "OffsetResetShortLuxDelta"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0072  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0081  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x0090  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1142640385) {
                if (hashCode != -454240631) {
                    if (hashCode != 82139585) {
                        if (hashCode == 1655845277 && valueName.equals("OffsetResetSwitchTime")) {
                            c = 0;
                            switch (c) {
                                case 0:
                                    HwBrightnessXmlLoader.mData.offsetResetSwitchTime = string2Int(parser.nextText());
                                    break;
                                case 1:
                                    HwBrightnessXmlLoader.mData.offsetResetEnable = string2Boolean(parser.nextText());
                                    break;
                                case 2:
                                    HwBrightnessXmlLoader.mData.offsetResetShortSwitchTime = string2Int(parser.nextText());
                                    break;
                                case 3:
                                    HwBrightnessXmlLoader.mData.offsetResetShortLuxDelta = string2Int(parser.nextText());
                                    break;
                                default:
                                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                                    return false;
                            }
                            return true;
                        }
                    } else if (valueName.equals("OffsetResetShortSwitchTime")) {
                        c = 2;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                        return true;
                    }
                } else if (valueName.equals("OffsetResetShortLuxDelta")) {
                    c = 3;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("OffsetResetEnable")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.offsetResetSwitchTime >= 0 && HwBrightnessXmlLoader.mData.offsetResetShortSwitchTime >= 0 && HwBrightnessXmlLoader.mData.offsetResetShortLuxDelta >= 0;
        }
    }

    private static class Element_OutdoorResponseGroup extends HwXmlElement {
        private Element_OutdoorResponseGroup() {
        }

        public String getName() {
            return "OutdoorResponseGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"OutdoorLowerLuxThreshold", "OutdoorAnimationBrightenTime", "OutdoorAnimationDarkenTime", "OutdoorResponseBrightenRatio", "OutdoorResponseDarkenRatio", "OutdoorResponseBrightenTime", "OutdoorResponseDarkenTime", "OutdoorResponseCount"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1820473548:
                    if (valueName.equals("OutdoorAnimationDarkenTime")) {
                        c = 2;
                        break;
                    }
                case -1232436373:
                    if (valueName.equals("OutdoorResponseBrightenRatio")) {
                        c = 3;
                        break;
                    }
                case -856311255:
                    if (valueName.equals("OutdoorResponseDarkenTime")) {
                        c = 6;
                        break;
                    }
                case -777923537:
                    if (valueName.equals("OutdoorResponseDarkenRatio")) {
                        c = 4;
                        break;
                    }
                case -455330963:
                    if (valueName.equals("OutdoorResponseBrightenTime")) {
                        c = 5;
                        break;
                    }
                case -110162222:
                    if (valueName.equals("OutdoorResponseCount")) {
                        c = 7;
                        break;
                    }
                case 697641400:
                    if (valueName.equals("OutdoorAnimationBrightenTime")) {
                        c = 1;
                        break;
                    }
                case 2114610753:
                    if (valueName.equals("OutdoorLowerLuxThreshold")) {
                        c = 0;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.outdoorLowerLuxThreshold = string2Int(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.outdoorAnimationBrightenTime = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.outdoorAnimationDarkenTime = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.outdoorResponseBrightenRatio = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.outdoorResponseDarkenRatio = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.outdoorResponseBrightenTime = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.outdoorResponseDarkenTime = string2Int(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.outdoorResponseCount = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return ((float) HwBrightnessXmlLoader.mData.outdoorLowerLuxThreshold) > -1.0E-6f && ((double) HwBrightnessXmlLoader.mData.outdoorResponseCount) > 1.0E-6d;
        }
    }

    private static class Element_PowerAndThermalGroup extends HwXmlElement {
        private Element_PowerAndThermalGroup() {
        }

        public String getName() {
            return "PowerAndThermalGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"ManualPowerSavingBrighnessLineEnable", "ManualPowerSavingAnimationBrightenTime", "ManualPowerSavingAnimationDarkenTime", "ManualThermalModeAnimationBrightenTime", "ManualThermalModeAnimationDarkenTime", "ThermalModeBrightnessMappingEnable", "PgModeBrightnessMappingEnable", "AutoPowerSavingUseManualAnimationTimeEnable", "PgSceneDetectionDarkenDelayTime", "PgSceneDetectionBrightenDelayTime", "ManualPowerSavingBrighnessLineDisableForDemo", "AutoPowerSavingBrighnessLineDisableForDemo"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1332786641:
                    if (valueName.equals("ManualPowerSavingBrighnessLineEnable")) {
                        c = 0;
                        break;
                    }
                case -1199462784:
                    if (valueName.equals("ManualThermalModeAnimationBrightenTime")) {
                        c = 3;
                        break;
                    }
                case -785273573:
                    if (valueName.equals("ManualPowerSavingAnimationBrightenTime")) {
                        c = 1;
                        break;
                    }
                case -687994128:
                    if (valueName.equals("ManualPowerSavingBrighnessLineDisableForDemo")) {
                        c = 10;
                        break;
                    }
                case -578235879:
                    if (valueName.equals("AutoPowerSavingBrighnessLineDisableForDemo")) {
                        c = 11;
                        break;
                    }
                case 397849367:
                    if (valueName.equals("AutoPowerSavingUseManualAnimationTimeEnable")) {
                        c = 7;
                        break;
                    }
                case 1064002566:
                    if (valueName.equals("ThermalModeBrightnessMappingEnable")) {
                        c = 5;
                        break;
                    }
                case 1131515809:
                    if (valueName.equals("PgSceneDetectionDarkenDelayTime")) {
                        c = 8;
                        break;
                    }
                case 1254090086:
                    if (valueName.equals("PgModeBrightnessMappingEnable")) {
                        c = 6;
                        break;
                    }
                case 1265817084:
                    if (valueName.equals("ManualThermalModeAnimationDarkenTime")) {
                        c = 4;
                        break;
                    }
                case 1914292055:
                    if (valueName.equals("ManualPowerSavingAnimationDarkenTime")) {
                        c = 2;
                        break;
                    }
                case 1952764317:
                    if (valueName.equals("PgSceneDetectionBrightenDelayTime")) {
                        c = 9;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.manualPowerSavingBrighnessLineEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.manualPowerSavingAnimationBrightenTime = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.manualPowerSavingAnimationDarkenTime = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.manualThermalModeAnimationBrightenTime = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.manualThermalModeAnimationDarkenTime = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.thermalModeBrightnessMappingEnable = string2Boolean(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.pgModeBrightnessMappingEnable = string2Boolean(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.autoPowerSavingUseManualAnimationTimeEnable = string2Boolean(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.pgSceneDetectionDarkenDelayTime = string2Int(parser.nextText());
                    break;
                case 9:
                    HwBrightnessXmlLoader.mData.pgSceneDetectionBrightenDelayTime = string2Int(parser.nextText());
                    break;
                case 10:
                    HwBrightnessXmlLoader.mData.manualPowerSavingBrighnessLineDisableForDemo = string2Boolean(parser.nextText());
                    break;
                case 11:
                    HwBrightnessXmlLoader.mData.autoPowerSavingBrighnessLineDisableForDemo = string2Boolean(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.manualPowerSavingAnimationBrightenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.manualPowerSavingAnimationDarkenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.manualThermalModeAnimationBrightenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.manualThermalModeAnimationDarkenTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.pgSceneDetectionDarkenDelayTime >= 0 && HwBrightnessXmlLoader.mData.pgSceneDetectionBrightenDelayTime >= 0;
        }
    }

    private static class Element_PreProcessing extends HwXmlElement {
        private Element_PreProcessing() {
        }

        public String getName() {
            return "PreProcessing";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_PreProcessingGroup extends HwXmlElement {
        private Element_PreProcessingGroup() {
        }

        public String getName() {
            return "PreProcessingGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"BrightTimeDelayEnable", "BrightTimeDelay", "BrightTimeDelayLuxThreshold", "PreMethodNum", "PreMeanFilterNoFilterNum", "PreMeanFilterNum", "PostMethodNum", "PostMeanFilterNoFilterNum", "PostMeanFilterNum"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1216473595:
                    if (valueName.equals("PostMethodNum")) {
                        c = 6;
                        break;
                    }
                case -591649441:
                    if (valueName.equals("BrightTimeDelayEnable")) {
                        c = 0;
                        break;
                    }
                case 157937894:
                    if (valueName.equals("PreMeanFilterNum")) {
                        c = 5;
                        break;
                    }
                case 620980681:
                    if (valueName.equals("PostMeanFilterNum")) {
                        c = 8;
                        break;
                    }
                case 700394488:
                    if (valueName.equals("BrightTimeDelayLuxThreshold")) {
                        c = 2;
                        break;
                    }
                case 1373278396:
                    if (valueName.equals("BrightTimeDelay")) {
                        c = 1;
                        break;
                    }
                case 1431493488:
                    if (valueName.equals("PostMeanFilterNoFilterNum")) {
                        c = 7;
                        break;
                    }
                case 1443034509:
                    if (valueName.equals("PreMeanFilterNoFilterNum")) {
                        c = 4;
                        break;
                    }
                case 1524020130:
                    if (valueName.equals("PreMethodNum")) {
                        c = 3;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.brightTimeDelayEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.brightTimeDelay = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.brightTimeDelayLuxThreshold = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.preMethodNum = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.preMeanFilterNoFilterNum = string2Int(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.preMeanFilterNum = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.postMethodNum = string2Int(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.postMeanFilterNoFilterNum = string2Int(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.postMeanFilterNum = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.preMeanFilterNum > 0 && HwBrightnessXmlLoader.mData.preMeanFilterNum <= HwBrightnessXmlLoader.mData.preMeanFilterNoFilterNum && HwBrightnessXmlLoader.mData.postMeanFilterNum > 0 && HwBrightnessXmlLoader.mData.postMeanFilterNum <= HwBrightnessXmlLoader.mData.postMeanFilterNoFilterNum;
        }
    }

    private static class Element_PreWeightedGroup extends HwXmlElement {
        private Element_PreWeightedGroup() {
        }

        public String getName() {
            return "PreWeightedGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"PreWeightedMeanFilterNoFilterNum", "PreWeightedMeanFilterMaxFuncLuxNum", "PreWeightedMeanFilterNum", "PreWeightedMeanFilterAlpha", "PreWeightedMeanFilterLuxTh"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1627681866:
                    if (valueName.equals("PreWeightedMeanFilterNoFilterNum")) {
                        c = 0;
                        break;
                    }
                case 246136847:
                    if (valueName.equals("PreWeightedMeanFilterNum")) {
                        c = 2;
                        break;
                    }
                case 302040999:
                    if (valueName.equals("PreWeightedMeanFilterAlpha")) {
                        c = 3;
                        break;
                    }
                case 312474924:
                    if (valueName.equals("PreWeightedMeanFilterLuxTh")) {
                        c = 4;
                        break;
                    }
                case 1095802536:
                    if (valueName.equals("PreWeightedMeanFilterMaxFuncLuxNum")) {
                        c = 1;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.preWeightedMeanFilterNoFilterNum = string2Int(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.preWeightedMeanFilterNum = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.preWeightedMeanFilterAlpha = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.preWeightedMeanFilterLuxTh = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.preWeightedMeanFilterNoFilterNum > 0 && HwBrightnessXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum > 0 && HwBrightnessXmlLoader.mData.preWeightedMeanFilterNum > 0 && HwBrightnessXmlLoader.mData.preWeightedMeanFilterNum <= HwBrightnessXmlLoader.mData.preWeightedMeanFilterNoFilterNum;
        }
    }

    private static class Element_Proximity extends HwXmlElement {
        private Element_Proximity() {
        }

        public String getName() {
            return "Proximity";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_ProximityGroup extends HwXmlElement {
        private Element_ProximityGroup() {
        }

        public String getName() {
            return "ProximityGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"AllowLabcUseProximity", "ProximityPositiveDebounceTime", "ProximityNegativeDebounceTime"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x0054  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0072  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -636219254) {
                if (hashCode != 1277664151) {
                    if (hashCode == 1847924814 && valueName.equals("ProximityPositiveDebounceTime")) {
                        c = 1;
                        switch (c) {
                            case 0:
                                HwBrightnessXmlLoader.mData.allowLabcUseProximity = string2Boolean(parser.nextText());
                                break;
                            case 1:
                                HwBrightnessXmlLoader.mData.proximityPositiveDebounceTime = string2Int(parser.nextText());
                                break;
                            case 2:
                                HwBrightnessXmlLoader.mData.proximityNegativeDebounceTime = string2Int(parser.nextText());
                                break;
                            default:
                                Slog.e(this.TAG, "unknow valueName=" + valueName);
                                return false;
                        }
                        return true;
                    }
                } else if (valueName.equals("AllowLabcUseProximity")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("ProximityNegativeDebounceTime")) {
                c = 2;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
            return true;
        }
    }

    private static class Element_ReadingAnimationTime extends HwXmlElement {
        private Element_ReadingAnimationTime() {
        }

        public String getName() {
            return "ReadingAnimationTime";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.readingAnimationTime = string2Float(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.readingAnimationTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_ReadingModeEnable extends HwXmlElement {
        private Element_ReadingModeEnable() {
        }

        public String getName() {
            return "ReadingModeEnable";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.readingModeEnable = string2Boolean(parser.nextText());
            return true;
        }
    }

    private static class Element_RebootGroup extends HwXmlElement {
        private Element_RebootGroup() {
        }

        public String getName() {
            return "RebootGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"RebootFirstBrightnessAnimationEnable", "RebootFirstBrightness", "RebootFirstBrightnessAutoTime", "RebootFirstBrightnessManualTime"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0072  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0081  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x0090  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -2008780644) {
                if (hashCode != -1474056744) {
                    if (hashCode != -752945685) {
                        if (hashCode == -260795025 && valueName.equals("RebootFirstBrightnessManualTime")) {
                            c = 3;
                            switch (c) {
                                case 0:
                                    HwBrightnessXmlLoader.mData.rebootFirstBrightnessAnimationEnable = string2Boolean(parser.nextText());
                                    break;
                                case 1:
                                    HwBrightnessXmlLoader.mData.rebootFirstBrightness = string2Int(parser.nextText());
                                    break;
                                case 2:
                                    HwBrightnessXmlLoader.mData.rebootFirstBrightnessAutoTime = string2Float(parser.nextText());
                                    break;
                                case 3:
                                    HwBrightnessXmlLoader.mData.rebootFirstBrightnessManualTime = string2Float(parser.nextText());
                                    break;
                                default:
                                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                                    return false;
                            }
                            return true;
                        }
                    } else if (valueName.equals("RebootFirstBrightnessAnimationEnable")) {
                        c = 0;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                        return true;
                    }
                } else if (valueName.equals("RebootFirstBrightnessAutoTime")) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("RebootFirstBrightness")) {
                c = 1;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.rebootFirstBrightness >= 0 && HwBrightnessXmlLoader.mData.rebootFirstBrightnessAutoTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.rebootFirstBrightnessManualTime > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_ResponseTimeGroup extends HwXmlElement {
        private Element_ResponseTimeGroup() {
        }

        public String getName() {
            return "ResponseTimeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"BrighenDebounceTime", "DarkenDebounceTime", "BrightenDebounceTimeParaBig", "DarkenDebounceTimeParaBig", "BrightenDeltaLuxPara", "DarkenDeltaLuxPara", "StabilityConstant", "StabilityTime1", "StabilityTime2"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1403734451:
                    if (valueName.equals("StabilityConstant")) {
                        c = 6;
                        break;
                    }
                case -599365355:
                    if (valueName.equals("DarkenDebounceTime")) {
                        c = 1;
                        break;
                    }
                case -85825767:
                    if (valueName.equals("BrighenDebounceTime")) {
                        c = 0;
                        break;
                    }
                case -37334485:
                    if (valueName.equals("DarkenDebounceTimeParaBig")) {
                        c = 3;
                        break;
                    }
                case 1002334874:
                    if (valueName.equals("BrightenDeltaLuxPara")) {
                        c = 4;
                        break;
                    }
                case 1209051670:
                    if (valueName.equals("DarkenDeltaLuxPara")) {
                        c = 5;
                        break;
                    }
                case 1578851579:
                    if (valueName.equals("StabilityTime1")) {
                        c = 7;
                        break;
                    }
                case 1578851580:
                    if (valueName.equals("StabilityTime2")) {
                        c = 8;
                        break;
                    }
                case 1866183975:
                    if (valueName.equals("BrightenDebounceTimeParaBig")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.brighenDebounceTime = string2Int(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.darkenDebounceTime = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.brightenDebounceTimeParaBig = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.darkenDebounceTimeParaBig = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.brightenDeltaLuxPara = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.darkenDeltaLuxPara = string2Float(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.stabilityConstant = string2Int(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.stabilityTime1 = string2Int(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.stabilityTime2 = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brighenDebounceTime > 0 && HwBrightnessXmlLoader.mData.darkenDebounceTime > 0;
        }
    }

    private static class Element_ResponseTimeOptionalGroup extends HwXmlElement {
        private Element_ResponseTimeOptionalGroup() {
        }

        public String getName() {
            return "ResponseTimeOptionalGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"BrighenDebounceTimeForSmallThr", "DarkenDebounceTimeForSmallThr", "RatioForBrightnenSmallThr", "RatioForDarkenSmallThr", "DarkTimeDelayEnable", "DarkTimeDelay", "DarkTimeDelayLuxThreshold", "DarkTimeDelayBeta0", "DarkTimeDelayBeta1", "DarkTimeDelayBeta2", "DarkTimeDelayBrightness"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2041716473:
                    if (valueName.equals("BrighenDebounceTimeForSmallThr")) {
                        c = 0;
                        break;
                    }
                case -1203123972:
                    if (valueName.equals("DarkTimeDelayLuxThreshold")) {
                        c = 6;
                        break;
                    }
                case -785220512:
                    if (valueName.equals("DarkTimeDelayBeta0")) {
                        c = 7;
                        break;
                    }
                case -785220511:
                    if (valueName.equals("DarkTimeDelayBeta1")) {
                        c = 8;
                        break;
                    }
                case -785220510:
                    if (valueName.equals("DarkTimeDelayBeta2")) {
                        c = 9;
                        break;
                    }
                case -487716469:
                    if (valueName.equals("DarkenDebounceTimeForSmallThr")) {
                        c = 1;
                        break;
                    }
                case 254012465:
                    if (valueName.equals("DarkTimeDelayBrightness")) {
                        c = 10;
                        break;
                    }
                case 633705408:
                    if (valueName.equals("DarkTimeDelay")) {
                        c = 5;
                        break;
                    }
                case 1174414900:
                    if (valueName.equals("RatioForDarkenSmallThr")) {
                        c = 3;
                        break;
                    }
                case 1521603939:
                    if (valueName.equals("DarkTimeDelayEnable")) {
                        c = 4;
                        break;
                    }
                case 1843038262:
                    if (valueName.equals("RatioForBrightnenSmallThr")) {
                        c = 2;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.brighenDebounceTimeForSmallThr = string2Int(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.darkenDebounceTimeForSmallThr = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.ratioForBrightnenSmallThr = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.ratioForDarkenSmallThr = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.darkTimeDelayEnable = string2Boolean(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.darkTimeDelay = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.darkTimeDelayLuxThreshold = string2Float(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.darkTimeDelayBeta0 = string2Float(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.darkTimeDelayBeta1 = string2Float(parser.nextText());
                    break;
                case 9:
                    HwBrightnessXmlLoader.mData.darkTimeDelayBeta2 = string2Float(parser.nextText());
                    break;
                case 10:
                    HwBrightnessXmlLoader.mData.darkTimeDelayBrightness = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brighenDebounceTimeForSmallThr > 0 && HwBrightnessXmlLoader.mData.darkenDebounceTimeForSmallThr > 0 && HwBrightnessXmlLoader.mData.ratioForBrightnenSmallThr > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.ratioForDarkenSmallThr > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_SceneProcessing extends HwXmlElement {
        private Element_SceneProcessing() {
        }

        public String getName() {
            return "SceneProcessing";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_SceneProcessingGroup extends HwXmlElement {
        private Element_SceneProcessingGroup() {
        }

        public String getName() {
            return "SceneProcessingGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"SceneMaxPoints", "SceneGapPoints", "SceneMinPoints", "SceneAmbientLuxMaxWeight", "SceneAmbientLuxMinWeight"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1975443769:
                    if (valueName.equals("SceneAmbientLuxMinWeight")) {
                        c = 4;
                        break;
                    }
                case 180392013:
                    if (valueName.equals("SceneGapPoints")) {
                        c = 1;
                        break;
                    }
                case 730661979:
                    if (valueName.equals("SceneMaxPoints")) {
                        c = 0;
                        break;
                    }
                case 1503140553:
                    if (valueName.equals("SceneMinPoints")) {
                        c = 2;
                        break;
                    }
                case 1547044953:
                    if (valueName.equals("SceneAmbientLuxMaxWeight")) {
                        c = 3;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.sceneMaxPoints = string2Int(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.sceneGapPoints = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.sceneMinPoints = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.sceneAmbientLuxMaxWeight = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.sceneAmbientLuxMinWeight = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }
    }

    private static class Element_SecondDarkenModeGroup extends HwXmlElement {
        private Element_SecondDarkenModeGroup() {
        }

        public String getName() {
            return "SecondDarkenMode";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"SecondDarkenModeEanble", "SecondDarkenModeMinLuxTh", "SecondDarkenModeMaxLuxTh", "SecondDarkenModeDarkenDeltaLuxRatio", "SecondDarkenModeDarkenDebounceTime"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1674305899:
                    if (valueName.equals("SecondDarkenModeMaxLuxTh")) {
                        c = 2;
                        break;
                    }
                case 240634943:
                    if (valueName.equals("SecondDarkenModeDarkenDeltaLuxRatio")) {
                        c = 3;
                        break;
                    }
                case 416043051:
                    if (valueName.equals("SecondDarkenModeDarkenDebounceTime")) {
                        c = 4;
                        break;
                    }
                case 844464743:
                    if (valueName.equals("SecondDarkenModeMinLuxTh")) {
                        c = 1;
                        break;
                    }
                case 1087381759:
                    if (valueName.equals("SecondDarkenModeEanble")) {
                        c = 0;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.secondDarkenModeEanble = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.secondDarkenModeMinLuxTh = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.secondDarkenModeMaxLuxTh = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.secondDarkenModeDarkenDeltaLuxRatio = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.secondDarkenModeDarkenDebounceTime = (long) string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.secondDarkenModeMinLuxTh >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.secondDarkenModeMaxLuxTh >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.secondDarkenModeDarkenDeltaLuxRatio >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.secondDarkenModeDarkenDebounceTime >= 0;
        }
    }

    private static class Element_SensorGroup extends HwXmlElement {
        private Element_SensorGroup() {
        }

        public String getName() {
            return "SensorGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"ResetAmbientLuxEnable", "ResetAmbientLuxTh", "ResetAmbientLuxThMin", "ResetAmbientLuxDarkenDebounceTime", "ResetAmbientLuxBrightenDebounceTime", "ResetAmbientLuxFastDarkenValidTime", "ResetAmbientLuxDarkenRatio", "ResetAmbientLuxGraTime", "ResetAmbientLuxFastDarkenDimmingTime", "ResetAmbientLuxStartBrightness", "ResetAmbientLuxDisableBrightnessOffset"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1859381766:
                    if (valueName.equals("ResetAmbientLuxTh")) {
                        c = 1;
                        break;
                    }
                case -1730863987:
                    if (valueName.equals("ResetAmbientLuxStartBrightness")) {
                        c = 9;
                        break;
                    }
                case -1428569114:
                    if (valueName.equals("ResetAmbientLuxDarkenRatio")) {
                        c = 6;
                        break;
                    }
                case -648897032:
                    if (valueName.equals("ResetAmbientLuxThMin")) {
                        c = 2;
                        break;
                    }
                case -646228247:
                    if (valueName.equals("ResetAmbientLuxFastDarkenDimmingTime")) {
                        c = 8;
                        break;
                    }
                case -110496184:
                    if (valueName.equals("ResetAmbientLuxFastDarkenValidTime")) {
                        c = 5;
                        break;
                    }
                case 95086715:
                    if (valueName.equals("ResetAmbientLuxDarkenDebounceTime")) {
                        c = 3;
                        break;
                    }
                case 335797126:
                    if (valueName.equals("ResetAmbientLuxDisableBrightnessOffset")) {
                        c = 10;
                        break;
                    }
                case 831699197:
                    if (valueName.equals("ResetAmbientLuxGraTime")) {
                        c = 7;
                        break;
                    }
                case 842426239:
                    if (valueName.equals("ResetAmbientLuxBrightenDebounceTime")) {
                        c = 4;
                        break;
                    }
                case 935721481:
                    if (valueName.equals("ResetAmbientLuxEnable")) {
                        c = 0;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxTh = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxThMin = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxDarkenDebounceTime = string2Int(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxBrightenDebounceTime = string2Int(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxFastDarkenValidTime = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxDarkenRatio = string2Float(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxGraTime = string2Float(parser.nextText());
                    break;
                case 8:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxFastDarkenDimmingTime = string2Float(parser.nextText());
                    break;
                case 9:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxStartBrightness = string2Float(parser.nextText());
                    break;
                case 10:
                    HwBrightnessXmlLoader.mData.resetAmbientLuxDisableBrightnessOffset = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.resetAmbientLuxTh >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.resetAmbientLuxThMin >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.resetAmbientLuxDarkenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.resetAmbientLuxBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.resetAmbientLuxFastDarkenValidTime >= 0 && HwBrightnessXmlLoader.mData.resetAmbientLuxDarkenRatio > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.resetAmbientLuxFastDarkenDimmingTime >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.resetAmbientLuxStartBrightness > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.resetAmbientLuxDisableBrightnessOffset > 0;
        }
    }

    private static class Element_TouchProximity extends HwXmlElement {
        private Element_TouchProximity() {
        }

        public String getName() {
            return "TouchProximity";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    private static class Element_TouchProximityGroup extends HwXmlElement {
        private Element_TouchProximityGroup() {
        }

        public String getName() {
            return "TouchProximityGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"Enable", "YNearbyRatio", "YRatioMin", "YRatioMax", "ProximitySceneModeEnable"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1458251566:
                    if (valueName.equals("YRatioMax")) {
                        c = 3;
                        break;
                    }
                case -1458251328:
                    if (valueName.equals("YRatioMin")) {
                        c = 2;
                        break;
                    }
                case -1214228397:
                    if (valueName.equals("ProximitySceneModeEnable")) {
                        c = 4;
                        break;
                    }
                case -286453069:
                    if (valueName.equals("YNearbyRatio")) {
                        c = 1;
                        break;
                    }
                case 2079986083:
                    if (valueName.equals("Enable")) {
                        c = 0;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.touchProximityEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin = 1.0f - string2Float(parser.nextText());
                    HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax = 1.0f;
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.proximitySceneModeEnable = string2Boolean(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            boolean z = true;
            if (!HwBrightnessXmlLoader.mData.touchProximityEnable) {
                return true;
            }
            if (HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin >= 1.0f || HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax <= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax > 1.0f || HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin >= HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax) {
                z = false;
            }
            return z;
        }
    }

    private static class Element_VariableStep extends HwXmlElement {
        private Element_VariableStep() {
        }

        public String getName() {
            return "VariableStep";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.useVariableStep = true;
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenGradualTimeMin <= HwBrightnessXmlLoader.mData.darkenGradualTimeMax;
        }
    }

    private static class Element_VariableStepGroup extends HwXmlElement {
        private Element_VariableStepGroup() {
        }

        public String getName() {
            return "VariableStepGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"DarkenGradualTimeMax", "DarkenGradualTimeMin", "AnimatedStepRoundEnabled"});
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:19:0x0054  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x0072  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1259361630) {
                if (hashCode != -113440444) {
                    if (hashCode == -113440206 && valueName.equals("DarkenGradualTimeMin")) {
                        c = 1;
                        switch (c) {
                            case 0:
                                HwBrightnessXmlLoader.mData.darkenGradualTimeMax = string2Float(parser.nextText());
                                break;
                            case 1:
                                HwBrightnessXmlLoader.mData.darkenGradualTimeMin = string2Float(parser.nextText());
                                break;
                            case 2:
                                HwBrightnessXmlLoader.mData.animatedStepRoundEnabled = string2Boolean(parser.nextText());
                                break;
                            default:
                                Slog.e(this.TAG, "unknow valueName=" + valueName);
                                return false;
                        }
                        return true;
                    }
                } else if (valueName.equals("DarkenGradualTimeMax")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("AnimatedStepRoundEnabled")) {
                c = 2;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenGradualTimeMax > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO && HwBrightnessXmlLoader.mData.darkenGradualTimeMin >= GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }
    }

    private static class Element_VehicleModeGroup extends HwXmlElement {
        private Element_VehicleModeGroup() {
        }

        public String getName() {
            return "VehicleModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList(new String[]{"VehicleModeEnable", "VehicleModeDisableTimeMillis", "VehicleModeQuitTimeForPowerOn", "VehicleModeEnterTimeForPowerOn"});
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:22:0x004b  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x0063  */
        /* JADX WARNING: Removed duplicated region for block: B:25:0x0072  */
        /* JADX WARNING: Removed duplicated region for block: B:26:0x0081  */
        /* JADX WARNING: Removed duplicated region for block: B:27:0x0090  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -946673391) {
                if (hashCode != 349236422) {
                    if (hashCode != 689432882) {
                        if (hashCode == 939700588 && valueName.equals("VehicleModeDisableTimeMillis")) {
                            c = 1;
                            switch (c) {
                                case 0:
                                    HwBrightnessXmlLoader.mData.vehicleModeEnable = string2Boolean(parser.nextText());
                                    break;
                                case 1:
                                    HwBrightnessXmlLoader.mData.vehicleModeDisableTimeMillis = string2Long(parser.nextText());
                                    break;
                                case 2:
                                    HwBrightnessXmlLoader.mData.vehicleModeQuitTimeForPowerOn = string2Long(parser.nextText());
                                    break;
                                case 3:
                                    HwBrightnessXmlLoader.mData.vehicleModeEnterTimeForPowerOn = string2Long(parser.nextText());
                                    break;
                                default:
                                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                                    return false;
                            }
                            return true;
                        }
                    } else if (valueName.equals("VehicleModeEnable")) {
                        c = 0;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }
                        return true;
                    }
                } else if (valueName.equals("VehicleModeQuitTimeForPowerOn")) {
                    c = 2;
                    switch (c) {
                        case 0:
                            break;
                        case 1:
                            break;
                        case 2:
                            break;
                        case 3:
                            break;
                    }
                    return true;
                }
            } else if (valueName.equals("VehicleModeEnterTimeForPowerOn")) {
                c = 3;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                return true;
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.vehicleModeDisableTimeMillis >= 0 && HwBrightnessXmlLoader.mData.vehicleModeQuitTimeForPowerOn >= 0 && HwBrightnessXmlLoader.mData.vehicleModeEnterTimeForPowerOn >= 0;
        }
    }

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        if (0 == 0) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0043, code lost:
        new com.android.server.display.HwBrightnessXmlLoader.Data().loadDefaultConfig();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x004f, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0051, code lost:
        throw r2;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:9:0x0014, B:15:0x0023] */
    public static Data getData() {
        Data data;
        Data retData = null;
        synchronized (mLock) {
            try {
                if (mLoader == null) {
                    mLoader = new HwBrightnessXmlLoader();
                }
                retData = mData;
                if (retData == null) {
                    data = new Data();
                    retData = data;
                    retData.loadDefaultConfig();
                }
            } catch (RuntimeException e) {
                Slog.e(TAG, "getData() failed! " + e);
                if (0 == 0) {
                    data = new Data();
                }
            }
        }
        return retData;
    }

    private static int getDeviceActualBrightnessLevel() {
        LightsManager lightsManager = (LightsManager) LocalServices.getService(LightsManager.class);
        if (lightsManager == null) {
            Slog.e(TAG, "getDeviceActualBrightnessLevel() can't get LightsManager");
            return 0;
        }
        Light lcdLight = lightsManager.getLight(0);
        if (lcdLight != null) {
            return lcdLight.getDeviceActualBrightnessLevel();
        }
        Slog.e(TAG, "getDeviceActualBrightnessLevel() can't get Light");
        return 0;
    }

    private HwBrightnessXmlLoader() {
        if (HWDEBUG) {
            Slog.d(TAG, "HwBrightnessXmlLoader()");
        }
        this.mDeviceActualBrightnessLevel = getDeviceActualBrightnessLevel();
        if (!parseXml(getXmlPath())) {
            mData.loadDefaultConfig();
        }
        mData.printData();
    }

    private boolean parseXml(String xmlPath) {
        if (xmlPath == null) {
            Slog.e(TAG, "parseXml() error! xmlPath is null");
            return false;
        }
        HwXmlParser xmlParser = new HwXmlParser(xmlPath);
        registerElement(xmlParser);
        if (!xmlParser.parse()) {
            Slog.e(TAG, "parseXml() error! xmlParser.parse() failed!");
            return false;
        } else if (!xmlParser.check()) {
            Slog.e(TAG, "parseXml() error! xmlParser.check() failed!");
            return false;
        } else {
            if (HWFLOW) {
                Slog.i(TAG, "parseXml() load success!");
            }
            return true;
        }
    }

    private void registerElement(HwXmlParser parser) {
        HwXmlElement rootElement = parser.registerRootElement(new Element_LABCConfig());
        rootElement.registerChildElement(new Element_MiscGroup());
        rootElement.registerChildElement(new Element_MiscOptionalGroup1());
        rootElement.registerChildElement(new Element_MiscOptionalGroup2());
        rootElement.registerChildElement(new Element_ReadingModeEnable());
        rootElement.registerChildElement(new Element_ReadingAnimationTime());
        rootElement.registerChildElement(new Element_ResponseTimeGroup());
        rootElement.registerChildElement(new Element_ResponseTimeOptionalGroup());
        rootElement.registerChildElement(new Element_CoverModeGroup());
        rootElement.registerChildElement(new Element_CoverModeBrighnessLinePoints()).registerChildElement(new Element_CoverModeBrighnessLinePoints_Point());
        rootElement.registerChildElement(new Element_BackSensorCoverModeBrighnessLinePoints()).registerChildElement(new Element_BackSensorCoverModeBrighnessLinePoints_Point());
        rootElement.registerChildElement(new Element_PreWeightedGroup());
        rootElement.registerChildElement(new Element_KeyguardResponseGroup());
        rootElement.registerChildElement(new Element_OutdoorResponseGroup());
        rootElement.registerChildElement(new Element_InitGroup());
        rootElement.registerChildElement(new Element_PowerAndThermalGroup());
        rootElement.registerChildElement(new Element_DayModeGroup());
        rootElement.registerChildElement(new Element_OffsetResetGroup());
        rootElement.registerChildElement(new Element_OffsetGroup());
        rootElement.registerChildElement(new Element_DarkLightGroup());
        rootElement.registerChildElement(new Element_RebootGroup());
        rootElement.registerChildElement(new Element_SceneProcessing()).registerChildElement(new Element_SceneProcessingGroup());
        rootElement.registerChildElement(new Element_PreProcessing()).registerChildElement(new Element_PreProcessingGroup());
        rootElement.registerChildElement(new Element_BrightenlinePoints()).registerChildElement(new Element_BrightenlinePoints_Point());
        rootElement.registerChildElement(new Element_DarkenlinePoints()).registerChildElement(new Element_DarkenlinePoints_Point());
        rootElement.registerChildElement(new Element_DefaultBrightnessPoints()).registerChildElement(new Element_DefaultBrightnessPoints_Point());
        rootElement.registerChildElement(new Element_AnimateGroup());
        rootElement.registerChildElement(new Element_AnimateOptionalGroup());
        rootElement.registerChildElement(new Element_VariableStep()).registerChildElement(new Element_VariableStepGroup());
        rootElement.registerChildElement(new Element_Proximity()).registerChildElement(new Element_ProximityGroup());
        rootElement.registerChildElement(new Element_ManualGroup());
        rootElement.registerChildElement(new Element_ManualOptionalGroup());
        rootElement.registerChildElement(new Element_ManualBrightenLinePoints()).registerChildElement(new Element_ManualBrightenLinePoints_Point());
        rootElement.registerChildElement(new Element_ManualDarkenLinePoints()).registerChildElement(new Element_ManualDarkenLinePoints_Point());
        rootElement.registerChildElement(new Element_BrightnessMappingPoints()).registerChildElement(new Element_BrightnessMappingPoints_Point());
        HwXmlElement ambientLuxValidBrightnessPoints = rootElement.registerChildElement(new Element_AmbientLuxValidBrightnessPoints());
        ambientLuxValidBrightnessPoints.registerChildElement(new Element_AmbientLuxValidBrightnessPoints_Point());
        HwXmlElement darkAdapter = rootElement.registerChildElement(new Element_DarkAdapter());
        HwXmlElement hwXmlElement = ambientLuxValidBrightnessPoints;
        darkAdapter.registerChildElement(new Element_DarkAdapterGroup1());
        darkAdapter.registerChildElement(new Element_DarkAdapterGroup2());
        HwXmlElement touchProximity = rootElement.registerChildElement(new Element_TouchProximity());
        HwXmlElement hwXmlElement2 = darkAdapter;
        touchProximity.registerChildElement(new Element_TouchProximityGroup());
        rootElement.registerChildElement(new Element_VehicleModeGroup());
        rootElement.registerChildElement(new Element_GameModeGroup());
        HwXmlElement brightenlinePointsForGameModeElement = rootElement.registerChildElement(new Element_BrightenlinePointsForGameMode());
        HwXmlElement hwXmlElement3 = touchProximity;
        brightenlinePointsForGameModeElement.registerChildElement(new Element_BrightenlinePointsForGameMode_Point());
        HwXmlElement darkenlinePointsForGameModeElement = rootElement.registerChildElement(new Element_DarkenlinePointsForGameMode());
        HwXmlElement hwXmlElement4 = brightenlinePointsForGameModeElement;
        darkenlinePointsForGameModeElement.registerChildElement(new Element_DarkenlinePointsForGameMode_Point());
        rootElement.registerChildElement(new ElementDcModeGroup());
        HwXmlElement brightenlinePointsForDcModeElement = rootElement.registerChildElement(new ElementBrightenlinePointsForDcMode());
        HwXmlElement hwXmlElement5 = darkenlinePointsForGameModeElement;
        brightenlinePointsForDcModeElement.registerChildElement(new ElementBrightenlinePointsForDcModePoint());
        HwXmlElement darkenlinePointsForDcModeElement = rootElement.registerChildElement(new ElementDarkenlinePointsForDcMode());
        HwXmlElement hwXmlElement6 = brightenlinePointsForDcModeElement;
        darkenlinePointsForDcModeElement.registerChildElement(new ElementDarkenlinePointsForDcModePoint());
        rootElement.registerChildElement(new Element_CryogenicGroup());
        HwXmlElement gameModeAmbientLuxValidBrightnessPoints = rootElement.registerChildElement(new Element_GameModeAmbientLuxValidBrightnessPoints());
        HwXmlElement hwXmlElement7 = darkenlinePointsForDcModeElement;
        gameModeAmbientLuxValidBrightnessPoints.registerChildElement(new Element_GameModeAmbientLuxValidBrightnessPoints_Point());
        rootElement.registerChildElement(new Element_LandScapeModeGroup());
        HwXmlElement brightenlinePointsForLandScapeMode = rootElement.registerChildElement(new Element_BrightenlinePointsForLandScapeMode());
        HwXmlElement hwXmlElement8 = gameModeAmbientLuxValidBrightnessPoints;
        brightenlinePointsForLandScapeMode.registerChildElement(new Element_BrightenlinePointsForLandScapeMode_Point());
        HwXmlElement darkenlinePointsForLandScapeMode = rootElement.registerChildElement(new Element_DarkenlinePointsForLandScapeMode());
        HwXmlElement hwXmlElement9 = brightenlinePointsForLandScapeMode;
        darkenlinePointsForLandScapeMode.registerChildElement(new Element_DarkenlinePointsForLandScapeMode_Point());
        HwXmlElement brightnessLevelToNitLinePoints = rootElement.registerChildElement(new Element_BrightnessLevelToNitLinePoints());
        HwXmlElement hwXmlElement10 = darkenlinePointsForLandScapeMode;
        brightnessLevelToNitLinePoints.registerChildElement(new Element_BrightnessLevelToNitLinePoints_Point());
        rootElement.registerChildElement(new Element_SensorGroup());
        rootElement.registerChildElement(new Element_BrightnessChangeGroup());
        HwXmlElement brightenlinePointsForBrightnessLevel = rootElement.registerChildElement(new Element_BrightenlinePointsForBrightnessLevel());
        HwXmlElement hwXmlElement11 = brightnessLevelToNitLinePoints;
        brightenlinePointsForBrightnessLevel.registerChildElement(new Element_BrightenlinePointsForBrightnessLevel_Point());
        HwXmlElement hwXmlElement12 = brightenlinePointsForBrightnessLevel;
        rootElement.registerChildElement(new Element_DarkenlinePointsForBrightnessLevel()).registerChildElement(new Element_DarkenlinePointsForBrightnessLevel_Point());
        rootElement.registerChildElement(new Element_SecondDarkenModeGroup());
        rootElement.registerChildElement(new Element_BrightnessOffsetLuxGroup1());
        rootElement.registerChildElement(new Element_BrightnessOffsetLuxGroup2());
    }

    private File getFactoryXmlFile() {
        String xmlPath = String.format("/xml/lcd/%s_%s%s", new Object[]{XML_NAME_NOEXT, "factory", XML_EXT});
        File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
        if (xmlFile != null) {
            return xmlFile;
        }
        Slog.e(TAG, "get xmlFile :" + xmlPath + " failed!");
        return null;
    }

    private String getLcdPanelName() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        String panelName = null;
        if (binder == null) {
            Slog.i(TAG, "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = mService.getEffect(14, 0, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                panelName = new String(name, "UTF-8").trim().replace(' ', '_');
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type!");
            }
            return panelName;
        } catch (RemoteException e2) {
            Slog.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }

    private String getVersionFromTouchOemInfo() {
        String touch_oem_info;
        int productMonth;
        int productDay;
        String version;
        String version2 = null;
        try {
            File file = new File(String.format("%s", new Object[]{TOUCH_OEM_INFO_PATH}));
            if (file.exists()) {
                Slog.i(TAG, "touch_oem_info=" + touch_oem_info);
                String[] versionInfo = touch_oem_info.split(",");
                if (versionInfo.length > 15) {
                    try {
                        int productYear = Integer.parseInt(versionInfo[12]);
                        int productMonth2 = Integer.parseInt(versionInfo[13]);
                        int productDay2 = Integer.parseInt(versionInfo[14]);
                        Slog.i(TAG, "lcdversionInfo orig productYear=" + productYear + ",productMonth=" + productMonth2 + ",productDay=" + productDay2);
                        if (productYear < 48 || productYear > 57) {
                            Slog.i(TAG, "lcdversionInfo not valid productYear=" + productYear);
                            return null;
                        }
                        int productYear2 = productYear - 48;
                        if (productMonth2 >= 48 && productMonth2 <= 57) {
                            productMonth = productMonth2 - 48;
                        } else if (productMonth2 < 65 || productMonth2 > 67) {
                            Slog.i(TAG, "lcdversionInfo not valid productMonth=" + productMonth2);
                            return null;
                        } else {
                            productMonth = (productMonth2 - 65) + 10;
                        }
                        if (productDay2 >= 48 && productDay2 <= 57) {
                            productDay = productDay2 - 48;
                        } else if (productDay2 < 65 || productDay2 > 88) {
                            Slog.i(TAG, "lcdversionInfo not valid productDay=" + productDay2);
                            return null;
                        } else {
                            productDay = (productDay2 - 65) + 10;
                        }
                        if (productYear2 > 8) {
                            version = "vn2";
                        } else if (productYear2 == 8 && productMonth > 1) {
                            version = "vn2";
                        } else if (productYear2 == 8 && productMonth == 1 && productDay >= 22) {
                            version = "vn2";
                        } else {
                            Slog.i(TAG, "lcdversionInfo not valid version;productYear=" + productYear2 + ",productMonth=" + productMonth + ",productDay=" + productDay);
                            return null;
                        }
                        version2 = version;
                        Slog.i(TAG, "lcdversionInfo real vn2,productYear=" + productYear2 + ",productMonth=" + productMonth + ",productDay=" + productDay);
                    } catch (NumberFormatException e) {
                        Slog.i(TAG, "lcdversionInfo versionfile num is not valid,no need version");
                        return null;
                    }
                } else {
                    Slog.i(TAG, "lcdversionInfo versionfile info length is not valid,no need version");
                }
            } else {
                Slog.i(TAG, "lcdversionInfo versionfile is not exists, no need version,filePath=/sys/touchscreen/touch_oem_info");
            }
        } catch (IOException e2) {
            Slog.w(TAG, "Error reading touch_oem_info", e2);
        }
        return version2;
    }

    private String getVersionFromLCD() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        String panelVersion = null;
        if (binder == null) {
            Slog.i(TAG, "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] name = new byte[32];
        try {
            int ret = mService.getEffect(14, 3, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                String lcdVersion = new String(name, "UTF-8").trim();
                int index = lcdVersion.indexOf("VER:");
                Slog.i(TAG, "getVersionFromLCD() index=" + index + ",lcdVersion=" + lcdVersion);
                if (index != -1) {
                    panelVersion = lcdVersion.substring("VER:".length() + index);
                }
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type!");
            }
            Slog.i(TAG, "getVersionFromLCD() panelVersion=" + panelVersion);
            return panelVersion;
        } catch (RemoteException e2) {
            Slog.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }

    private File getNormalXmlFile() {
        String lcdname = getLcdPanelName();
        String lcdversion = getVersionFromTouchOemInfo();
        String lcdversionNew = getVersionFromLCD();
        String screenColor = SystemProperties.get("ro.config.devicecolor");
        Slog.i(TAG, "screenColor=" + screenColor);
        ArrayList<String> xmlPathList = new ArrayList<>();
        if (lcdversion != null) {
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, lcdversion, screenColor, XML_EXT}));
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, lcdversion, XML_EXT}));
        }
        if (lcdversionNew != null) {
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, lcdversionNew, screenColor, XML_EXT}));
            xmlPathList.add(String.format("/xml/lcd/%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, lcdversionNew, XML_EXT}));
        }
        xmlPathList.add(String.format("/xml/lcd/%s_%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, screenColor, XML_EXT}));
        xmlPathList.add(String.format("/xml/lcd/%s_%s%s", new Object[]{XML_NAME_NOEXT, lcdname, XML_EXT}));
        xmlPathList.add(String.format("/xml/lcd/%s_%s%s", new Object[]{XML_NAME_NOEXT, screenColor, XML_EXT}));
        xmlPathList.add(String.format("/xml/lcd/%s", new Object[]{XML_NAME}));
        File xmlFile = null;
        Iterator<String> it = xmlPathList.iterator();
        while (it.hasNext()) {
            xmlFile = HwCfgFilePolicy.getCfgFile(it.next(), 2);
            if (xmlFile != null) {
                return xmlFile;
            }
        }
        Slog.e(TAG, "get failed!");
        return xmlFile;
    }

    private String getXmlPath() {
        File xmlFile;
        String currentMode = SystemProperties.get("ro.runmode");
        Slog.i(TAG, "currentMode=" + currentMode);
        if (currentMode == null) {
            xmlFile = getNormalXmlFile();
            if (xmlFile == null) {
                return null;
            }
        } else if (currentMode.equals("factory")) {
            xmlFile = getFactoryXmlFile();
            if (xmlFile == null) {
                return null;
            }
        } else if (currentMode.equals("normal")) {
            xmlFile = getNormalXmlFile();
            if (xmlFile == null) {
                return null;
            }
        } else {
            xmlFile = getNormalXmlFile();
            if (xmlFile == null) {
                return null;
            }
        }
        return xmlFile.getAbsolutePath();
    }

    /* access modifiers changed from: private */
    public static boolean checkPointsListIsOK(List<PointF> list) {
        if (list == null) {
            Slog.e(TAG, "checkPointsListIsOK() error! list is null");
            return false;
        } else if (list.size() < 3 || list.size() >= 100) {
            Slog.e(TAG, "checkPointsListIsOK() error! list size=" + list.size() + " is out of range");
            return false;
        } else {
            PointF lastPoint = null;
            for (PointF point : list) {
                if (lastPoint == null || point.x > lastPoint.x) {
                    lastPoint = point;
                } else {
                    Slog.e(TAG, "checkPointsListIsOK() error! x in list isn't a increasing sequence, " + point.x + "<=" + lastPoint.x);
                    return false;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static boolean checkAmPointsListIsOK(List<HwXmlAmPoint> list) {
        if (list == null) {
            Slog.e(TAG, "checkPointsListIsOK() error! list is null");
            return false;
        } else if (list.size() < 3 || list.size() >= 100) {
            Slog.e(TAG, "checkPointsListIsOK() error! list size=" + list.size() + " is out of range");
            return false;
        } else {
            HwXmlAmPoint lastPoint = null;
            for (HwXmlAmPoint point : list) {
                if (lastPoint == null || point.x > lastPoint.x) {
                    lastPoint = point;
                } else {
                    Slog.e(TAG, "checkPointsListIsOK() error! x in list isn't a increasing sequence, " + point.x + "<=" + lastPoint.x);
                    return false;
                }
            }
            return true;
        }
    }
}
