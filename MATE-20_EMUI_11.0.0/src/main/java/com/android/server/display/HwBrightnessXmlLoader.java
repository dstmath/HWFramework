package com.android.server.display;

import android.graphics.PointF;
import android.hardware.display.HwFoldScreenState;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.gesture.DefaultGestureNavConst;
import com.android.server.hidata.appqoe.HwAPPQoEUtils;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.huawei.displayengine.HwXmlAmPoint;
import com.huawei.displayengine.HwXmlElement;
import com.huawei.displayengine.HwXmlParser;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import com.huawei.hiai.awareness.AwarenessConstants;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* access modifiers changed from: package-private */
public final class HwBrightnessXmlLoader {
    private static final int FAILED_RETURN_VALUE = -1;
    private static final boolean HWDEBUG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    private static final boolean HWFLOW;
    private static final boolean IS_DEFAULT_POWER_SAVING_RATIO = SystemProperties.getBoolean("hw_mc.power.powersaving_mode", false);
    private static final int POINTS_MAX_SIZE = 100;
    private static final int POINTS_MIN_SIZE = 3;
    private static final float SMALL_VALUE = 1.0E-6f;
    private static final int SUCCESS_RETURN_VALUE = 0;
    private static final String TAG = "HwBrightnessXmlLoader";
    private static final String TOUCH_OEM_INFO_PATH = "/sys/touchscreen/touch_oem_info";
    private static final String XML_EXT = ".xml";
    private static final String XML_NAME_NOEXT = "LABCConfig";
    private static Data mData = new Data();
    private static HwBrightnessXmlLoader mLoader;
    private static final Object mLock = new Object();
    private static String sDefaultPanelName = null;
    private static String sDefaultPanelVersion = null;
    private static String sInwardPanelName = null;
    private static String sInwardPanelVersion = null;
    private static boolean sIsNeedMaxAmbientLuxPoint = false;
    private static String sOutwardPanelName = null;
    private static String sOutwardPanelVersion = null;
    private final int mDeviceActualBrightnessLevel;

    static {
        boolean z = true;
        if (!Log.HWINFO && (!Log.HWModuleLog || !Log.isLoggable(TAG, 4))) {
            z = false;
        }
        HWFLOW = z;
    }

    public static final class Data {
        public static final float DEFAULT_VALID_MAX_LUX = 40000.0f;
        public static final int HOUR_VALID_MAX_VALUE = 24;
        public int QRCodeBrightnessminLimit = -1;
        public float adapted2UnadaptShortFilterLux = 5.0f;
        public int adapting2AdaptedOffDurationFilterSec = 1800;
        public int adapting2AdaptedOffDurationMaxSec = 28800;
        public int adapting2AdaptedOffDurationMinSec = 10;
        public int adapting2AdaptedOnClockNoFilterBeginHour = 21;
        public int adapting2AdaptedOnClockNoFilterEndHour = 7;
        public float adapting2UnadaptShortFilterLux = 5.0f;
        public boolean allowLabcUseProximity = false;
        public float ambientLuxMinRound = -1.0f;
        public List<HwXmlAmPoint> ambientLuxValidBrightnessPoints = new ArrayList();
        public boolean animatedStepRoundEnabled = false;
        public boolean animatingForRGBWEnable = false;
        public boolean animationEqualRatioEnable = false;
        public float autoFastTimeFor255 = 0.5f;
        public boolean autoModeInOutDoorLimitEnble = false;
        public float autoModeSeekBarMaxBrightnessLuxTh = 2000.0f;
        public int autoModeSeekBarMaxBrightnessValue = 255;
        public boolean autoPowerSavingBrighnessLineDisableForDemo = false;
        public boolean autoPowerSavingUseManualAnimationTimeEnable = false;
        public List<PointF> backSensorCoverModeBrighnessLinePoints = new ArrayList();
        public boolean backSensorCoverModeEnable = false;
        public int backSensorCoverModeMinLuxInRing = 0;
        public int batteryLowLevelMaxBrightness = 255;
        public int batteryLowLevelTh = 0;
        public boolean batteryModeEnable = false;
        public int brighenDebounceTime = 1000;
        public int brighenDebounceTimeForSmallThr = HwAPPQoEUtils.APP_TYPE_STREAMING;
        public int brightTimeDelay = 1000;
        public boolean brightTimeDelayEnable = false;
        public float brightTimeDelayLuxThreshold = 30.0f;
        public float brightenDebounceTimeParaBig = 0.0f;
        public float brightenDeltaLuxPara = 0.0f;
        public int brightenFlickerAmountMin = 41;
        public float brightenFlickerGradualTimeMax = 3.0f;
        public float brightenFlickerGradualTimeMin = 0.5f;
        public int brightenFlickerTargetMin = 0;
        public float brightenGradualTime = 1.0f;
        public float brightenGradualTimeLong = 1.0f;
        public List<PointF> brightenLinePointsForLandscapeGameMode = new ArrayList();
        public boolean brightenOffsetEffectMinLuxEnable = false;
        public float brightenOffsetLuxTh1 = 0.0f;
        public float brightenOffsetLuxTh2 = 0.0f;
        public float brightenOffsetLuxTh3 = 0.0f;
        public float brightenOffsetNoValidBrightenLuxTh1 = -1.0f;
        public float brightenOffsetNoValidBrightenLuxTh2 = -1.0f;
        public float brightenOffsetNoValidBrightenLuxTh3 = -1.0f;
        public float brightenOffsetNoValidBrightenLuxTh4 = -1.0f;
        public float brightenOffsetNoValidDarkenLuxTh1 = -1.0f;
        public float brightenOffsetNoValidDarkenLuxTh2 = -1.0f;
        public float brightenOffsetNoValidDarkenLuxTh3 = -1.0f;
        public float brightenOffsetNoValidDarkenLuxTh4 = -1.0f;
        public float brightenOffsetNoValidSavedLuxTh1 = 0.0f;
        public float brightenOffsetNoValidSavedLuxTh2 = 0.0f;
        public int brightenThresholdFor255 = 1254;
        public int brightenTimeLongAmountMin = 0;
        public int brightenTimeLongCurrentTh = 0;
        public List<PointF> brightenlinePoints = new ArrayList();
        public List<PointF> brightenlinePointsForBrightnessLevel = new ArrayList();
        public List<PointF> brightenlinePointsForDcMode = new ArrayList();
        public List<PointF> brightenlinePointsForGameMode = new ArrayList();
        public List<PointF> brightenlinePointsForLandscapeMode = new ArrayList();
        public boolean brightnessCalibrationEnabled = false;
        public int brightnessChageDefaultDelayTime = 500;
        public int brightnessChageDownDelayTime = 200;
        public int brightnessChageUpDelayTime = 50;
        public List<PointF> brightnessLevelToNitLinePoints = new ArrayList();
        public boolean brightnessLevelToNitMappingEnable = false;
        public List<PointF> brightnessLineForDarkMode = new ArrayList();
        public List<PointF> brightnessLineForHdrMode = new ArrayList();
        public List<PointF> brightnessLineForVideoFullScreenMode = new ArrayList();
        public boolean brightnessMappingForWindowBrightnessEnable = false;
        public List<PointF> brightnessMappingPoints = new ArrayList();
        public List<PointF> brightnessMappingPointsForHdrMode = new ArrayList();
        public List<PointF> brightnessMappingPointsForWindowBrightness = new ArrayList();
        public boolean brightnessOffsetLuxModeEnable = false;
        public boolean brightnessOffsetTmpValidEnable = false;
        public float cameraAnimationTime = 3.0f;
        public boolean cameraModeEnable = false;
        public int converModeDayBeginTime = 6;
        public long coverModeBrightenResponseTime = 1000;
        public List<PointF> coverModeBrightnessLinePoints = new ArrayList();
        public long coverModeDarkenResponseTime = 1000;
        public int coverModeDayBrightness = 154;
        public boolean coverModeDayEnable = false;
        public int coverModeDayEndTime = 18;
        public float coverModeFirstLux = 2210.0f;
        public boolean coverModelastCloseScreenEnable = false;
        public long cryogenicActiveScreenOffIntervalInMillis = 1800000;
        public boolean cryogenicEnable = false;
        public long cryogenicLagTimeInMillis = 1800000;
        public long cryogenicMaxBrightnessTimeOut = 9000;
        public boolean cryogenicModeBrightnessMappingEnable = false;
        public boolean darkAdapterEnable = false;
        public int darkLightLevelMaxThreshold = 0;
        public int darkLightLevelMinThreshold = 0;
        public float darkLightLevelRatio = 1.0f;
        public float darkLightLuxDelta = 0.0f;
        public float darkLightLuxMaxThreshold = 0.0f;
        public float darkLightLuxMinThreshold = 0.0f;
        public boolean darkModeEnable = false;
        public int darkModeOffsetMinBrightness = 4;
        public int darkTimeDelay = 10000;
        public float darkTimeDelayBeta0 = 0.0f;
        public float darkTimeDelayBeta1 = 1.0f;
        public float darkTimeDelayBeta2 = 0.333f;
        public float darkTimeDelayBrightness = 0.0f;
        public boolean darkTimeDelayEnable = false;
        public float darkTimeDelayLuxThreshold = 50.0f;
        public int darkenCurrentFor255 = DeviceStatusConstant.TYPE_HEAD_DOWN;
        public int darkenDebounceTime = HwAPPQoEUtils.APP_TYPE_STREAMING;
        public int darkenDebounceTimeForSmallThr = 8000;
        public float darkenDebounceTimeParaBig = 1.0f;
        public float darkenDeltaLuxPara = 1.0f;
        public float darkenGradualTime = 3.0f;
        public float darkenGradualTimeMax = 3.0f;
        public float darkenGradualTimeMin = 0.0f;
        public List<PointF> darkenLinePointsForLandscapeGameMode = new ArrayList();
        public float darkenNoFlickerTarget = 0.0f;
        public float darkenNoFlickerTargetGradualTimeMin = 15.0f;
        public float darkenOffsetLuxTh1 = 0.0f;
        public float darkenOffsetLuxTh2 = 0.0f;
        public float darkenOffsetLuxTh3 = 0.0f;
        public float darkenOffsetNoValidBrightenLuxTh1 = -1.0f;
        public float darkenOffsetNoValidBrightenLuxTh2 = -1.0f;
        public float darkenOffsetNoValidBrightenLuxTh3 = -1.0f;
        public float darkenOffsetNoValidBrightenLuxTh4 = -1.0f;
        public int darkenTargetFor255 = 1254;
        public float darkenTargetForKeyguard = 0.0f;
        public List<PointF> darkenlinePoints = new ArrayList();
        public List<PointF> darkenlinePointsForBrightnessLevel = new ArrayList();
        public List<PointF> darkenlinePointsForDcMode = new ArrayList();
        public List<PointF> darkenlinePointsForGameMode = new ArrayList();
        public List<PointF> darkenlinePointsForLandscapeMode = new ArrayList();
        public boolean dayModeAlgoEnable = false;
        public int dayModeBeginTime = 5;
        public List<PointF> dayModeBrightnessLinePoints = new ArrayList();
        public float dayModeDarkenMinLux = 0.0f;
        public int dayModeEndTime = 23;
        public int dayModeModifyMinBrightness = 6;
        public int dayModeModifyNumPoint = 3;
        public boolean dayModeNewCurveEnable = false;
        public int dayModeSwitchTime = 30;
        public long dcModeBrightenDebounceTime = 1000;
        public long dcModeDarkenDebounceTime = 1000;
        public boolean dcModeEnable = false;
        public boolean dcModeLuxThresholdEnable = false;
        public float defaultBrightness = 100.0f;
        public List<PointF> defaultBrightnessLinePoints = new ArrayList();
        public float dimTime = 3.0f;
        public int disableProximityDelayTime = 10000;
        public int enableProximityDelayTime = 10;
        public boolean foldScreenModeEnable = false;
        public int frontCameraBrighenDebounceTime = 3000;
        public float frontCameraBrightenLuxThreshold = 0.0f;
        public int frontCameraDarkenDebounceTime = 3000;
        public float frontCameraDarkenLuxThreshold = 0.0f;
        public float frontCameraDimmingBrightenTime = 4.0f;
        public float frontCameraDimmingDarkenTime = 6.0f;
        public float frontCameraLuxThreshold = 0.0f;
        public int frontCameraMaxBrightness = 255;
        public boolean frontCameraMaxBrightnessEnable = false;
        public int frontCameraUpdateBrightnessDelayTime = 100;
        public int frontCameraUpdateDimmingEnableTime = 500;
        public boolean gameDisableAutoBrightnessModeEnable = true;
        public List<HwXmlAmPoint> gameModeAmbientLuxValidBrightnessPoints = new ArrayList();
        public float gameModeBrightenAnimationTime = 0.5f;
        public long gameModeBrightenDebounceTime = 1000;
        public float gameModeBrightnessFloor = 4.0f;
        public boolean gameModeBrightnessLimitationEnable = false;
        public long gameModeClearOffsetTime = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
        public long gameModeDarkenDebounceTime = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
        public float gameModeDarkentenAnimationTime = 0.5f;
        public float gameModeDarkentenLongAnimationTime = 0.5f;
        public int gameModeDarkentenLongCurrent = 0;
        public int gameModeDarkentenLongTarget = 0;
        public boolean gameModeEnable = false;
        public boolean gameModeLuxThresholdEnable = false;
        public boolean gameModeOffsetValidAmbientLuxEnable = false;
        public boolean hdrModeCurveEnable = false;
        public boolean hdrModeEnable = false;
        public boolean hdrModeWindowMappingEnable = false;
        public float highBrightenOffsetNoValidBrightenLuxTh = 10000.0f;
        public float highBrightenOffsetNoValidDarkenLuxTh = 20.0f;
        public float highDarkenOffsetNoValidBrightenLuxTh = 10000.0f;
        public float highDarkenOffsetNoValidDarkenLuxTh = 20.0f;
        public float highEqualDarkenMaxTime = 20.0f;
        public float highEqualDarkenMinTime = 8.0f;
        public int homeModeDayBeginTime = 5;
        public int homeModeDayEndTime = 23;
        public boolean homeModeEnable = false;
        public int homeModeSwitchTime = 30;
        public int inDoorThreshold = HwAPPQoEUtils.APP_TYPE_STREAMING;
        public float initDoubleSensorInterfere = 8.0f;
        public int initNumLastBuffer = 10;
        public float initSigmoidFuncSlope = 0.75f;
        public int initSlowReponseBrightTime = 0;
        public int initSlowReponseUpperLuxThreshold = 20;
        public int initUpperLuxThreshold = 20;
        public long initValidCloseTime = -1;
        public boolean isAutoModeSeekBarMaxBrightnessBasedLux = false;
        public boolean isDarkModeCurveEnable = false;
        public boolean isDarkenAnimatingStepForHbm = false;
        public boolean isDynamicEnableProximity = false;
        public boolean isHighEqualDarkenEnable = false;
        public boolean isInwardFoldScreenLuxEnable = false;
        public boolean isLandscapeGameModeEnable = false;
        public boolean isLandscapeGameModeLuxThresholdEnable = false;
        public boolean isLowEqualDarkenEnable = false;
        public boolean isLowPowerMappingEnable = false;
        public boolean isSeekBarManualDimTimeEnable = false;
        public boolean isWalkModeEnable = false;
        public float keyguardAnimationBrightenTime = 0.5f;
        public float keyguardAnimationDarkenTime = -1.0f;
        public float keyguardBrightenLuxDeltaMin = 0.0f;
        public int keyguardFastDimBrightness = 0;
        public float keyguardFastDimTime = 0.5f;
        public float keyguardLuxThreshold = 20.0f;
        public int keyguardResponseBrightenTime = 500;
        public int keyguardResponseDarkenTime = -1;
        public long keyguardUnlockedDarkenDebounceTime = 1000;
        public float keyguardUnlockedDarkenRatio = 1.0f;
        public float keyguardUnlockedDimmingTime = 1.0f;
        public boolean keyguardUnlockedFastDarkenEnable = false;
        public float keyguardUnlockedFastDarkenMaxLux = 20.0f;
        public long keyguardUnlockedFastDarkenValidTime = 5000;
        public long keyguardUnlockedLuxDeltaValidTime = 2000;
        public boolean landscapeBrightnessModeEnable = false;
        public long landscapeGameModeBrightenDebounceTime = 1000;
        public long landscapeGameModeDarkenDebounceTime = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
        public int landscapeGameModeEnterDelayTime = 200;
        public int landscapeGameModeQuitDelayTime = 600;
        public int landscapeModeBrightenDebounceTime = 1000;
        public int landscapeModeDarkenDebounceTime = 1000;
        public int landscapeModeEnterDelayTime = 500;
        public int landscapeModeQuitDelayTime = 500;
        public boolean landscapeModeUseTouchProximity = false;
        public boolean lastCloseScreenEnable = false;
        public int lightSensorRateMills = 300;
        public int linearDimmingValueTh = 0;
        public boolean longTimeFilterEnable = false;
        public float longTimeFilterLuxTh = 500.0f;
        public int longTimeFilterNum = 3;
        public int longTimeNoFilterNum = 7;
        public float lowBrightenOffsetNoValidBrightenLuxTh = 500.0f;
        public float lowBrightenOffsetNoValidDarkenLuxTh = 0.0f;
        public float lowDarkenOffsetDarkenBrightnessRatio = 0.2f;
        public float lowDarkenOffsetNoValidBrightenLuxTh = 500.0f;
        public float lowDarkenOffsetNoValidDarkenLuxTh = 0.0f;
        public float lowEqualDarkenLevel = 666.0f;
        public float lowEqualDarkenMaxTime = 25.0f;
        public float lowEqualDarkenMinLevel = 200.0f;
        public float lowEqualDarkenMinTime = 8.0f;
        public float lowEqualDarkenNit = 30.0f;
        public float lowPowerMappingLevel = 784.0f;
        public float lowPowerMappingLevelRatio = 1.0f;
        public float lowPowerMappingNit = 20.0f;
        public boolean luxMinMaxBrightnessEnable = false;
        public List<HwXmlAmPoint> luxMinMaxBrightnessPoints = new ArrayList();
        public boolean luxRoundEnable = true;
        public float luxThNoResponseForSmallThr = -1.0f;
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
        public float maxDarkenAnimatingStepForHbm = 10.0f;
        public float maxValidAmbientLux = 40000.0f;
        public float minAnimatingStep = 0.0f;
        public float minAnimatingStepForKeyguard = 0.0f;
        public float minDarkenBrightnessLevelForHbm = 8824.0f;
        public boolean monitorEnable = false;
        public int nightUpModeBeginHourTime = 0;
        public boolean nightUpModeEnable = false;
        public int nightUpModeEndHourTime = 5;
        public float nightUpModeLuxThreshold = 0.0f;
        public float nightUpModePowOnDimTime = 0.5f;
        public int nightUpModeSwitchTimeMillis = 1800000;
        public int offsetBrightenDebounceTime = 1000;
        public int offsetDarkenDebounceTime = 1000;
        public boolean offsetResetEnable = false;
        public int offsetResetShortLuxDelta = 50000;
        public int offsetResetShortSwitchTime = 10;
        public int offsetResetSwitchTime = 10;
        public int offsetResetSwitchTimeForDarkMode = -1;
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
        public int pgSceneDetectionDarkenDelayTime = DefaultGestureNavConst.CHECK_AFT_TIMEOUT;
        public int postMaxMinAvgFilterNoFilterNum = 6;
        public int postMaxMinAvgFilterNum = 5;
        public int postMeanFilterNoFilterNum = 4;
        public int postMeanFilterNum = 3;
        public int postMethodNum = 2;
        public int powerOnBrightenDebounceTime = 500;
        public int powerOnDarkenDebounceTime = 1000;
        public int powerOnFastResponseLuxNum = 8;
        public long powerOnFastResponseTime = 2000;
        public int powerSavingModeBatteryLowLevelBrightnessRatio = 60;
        public boolean powerSavingModeBatteryLowLevelEnable = true;
        public int powerSavingModeBatteryLowLevelThreshold = 10;
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
        public int proximityPositiveDebounceTime = WMStateCons.MSG_POWER_CONNECTED;
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
        public float resetAmbientLuxFastDarkenDimmingTime = 0.0f;
        public int resetAmbientLuxFastDarkenValidTime = 0;
        public float resetAmbientLuxGraTime = 0.0f;
        public float resetAmbientLuxStartBrightness = 200.0f;
        public float resetAmbientLuxStartBrightnessMax = 200.0f;
        public float resetAmbientLuxTh = 0.0f;
        public float resetAmbientLuxThMax = 0.0f;
        public float resetAmbientLuxThMin = 0.0f;
        public float sceneAmbientLuxMaxWeight = 0.5f;
        public float sceneAmbientLuxMinWeight = 0.5f;
        public int sceneGapPoints = 29;
        public int sceneMaxPoints = 0;
        public int sceneMinPoints = 29;
        public float screenBrightnessMaxNit = 530.0f;
        public float screenBrightnessMinNit = 2.0f;
        public float screenBrightnessNormalModeMaxLevel = 8824.0f;
        public float screenBrightnessNormalModeMaxNit = 500.0f;
        public long secondDarkenModeAfterNoResponseCheckTime = 2000;
        public long secondDarkenModeDarkenDebounceTime = 0;
        public float secondDarkenModeDarkenDeltaLuxRatio = 0.0f;
        public boolean secondDarkenModeEnable = false;
        public float secondDarkenModeMaxLuxTh = 0.0f;
        public float secondDarkenModeMinLuxTh = 0.0f;
        public long secondDarkenModeNoResponseDarkenTime = 0;
        public long secondDarkenModeNoResponseDarkenTimeMin = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
        public float seekBarDimTime = 0.5f;
        public float seekBarManualBrightenDimTime = 0.5f;
        public float seekBarManualDarkenDimTime = 0.5f;
        public int stabilityConstant = 5;
        public int stabilityTime1 = 20;
        public int stabilityTime2 = 10;
        public float targetFor255BrightenTime = 0.0f;
        public boolean thermalModeBrightnessMappingEnable = false;
        public boolean touchProximityEnable = false;
        public float touchProximityYNearbyRatioMax = 1.0f;
        public float touchProximityYNearbyRatioMin = 0.0f;
        public float twoPointOffsetAdjionLuxTh = 50.0f;
        public float twoPointOffsetLuxTh = 50.0f;
        public boolean twoPointOffsetModeEnable = false;
        public float twoPointOffsetNoValidLuxTh = 50.0f;
        public int unadapt2AdaptedOffDurationMinSec = 2700;
        public int unadapt2AdaptedOnClockNoFilterBeginHour = 21;
        public int unadapt2AdaptedOnClockNoFilterEndHour = 7;
        public float unadapt2AdaptedShortFilterLux = 5.0f;
        public int unadapt2AdaptingDimSec = 60;
        public float unadapt2AdaptingLongFilterLux = 1.0f;
        public int unadapt2AdaptingLongFilterSec = DefaultGestureNavConst.GESTURE_GO_HOME_MIN_DISTANCE_THRESHOLD;
        public float unadapt2AdaptingShortFilterLux = 5.0f;
        public boolean updateBrightnessViewAlphaEnable = false;
        public int updateDarkAmbientLuxDelayTime = 300;
        public boolean useVariableStep = false;
        public long vehicleModeDisableTimeMillis = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
        public boolean vehicleModeEnable = false;
        public long vehicleModeEnterTimeForPowerOn = 200;
        public long vehicleModeQuitTimeForPowerOn = 200;
        public int videoFullScreenModeDelayTime = 10;
        public boolean videoFullScreenModeEnable = false;
        public float walkModeMinLux = 1.0f;

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
                if (this.longTimeFilterEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "longTimeNoFilterNum=" + this.longTimeFilterEnable + ",longTimeNoFilterNum=" + this.longTimeNoFilterNum + ",longTimeFilterNum=" + this.longTimeFilterNum + ",longTimeFilterLuxTh=" + this.longTimeFilterLuxTh + ",luxRoundEnable=" + this.luxRoundEnable + ",ambientLuxMinRound=" + this.ambientLuxMinRound + ",luxThNoResponseForSmallThr=" + this.luxThNoResponseForSmallThr);
                }
                if (this.homeModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "hoModeDayBeginTime=" + this.homeModeDayBeginTime + ",hoModeDayEndTime=" + this.homeModeDayEndTime + ",hoModeSwitchTime=" + this.homeModeSwitchTime);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkTimeDelayBeta0=" + this.darkTimeDelayBeta0 + ",darkTimeDelayBeta1=" + this.darkTimeDelayBeta1 + ",darkTimeDelayBeta2=" + this.darkTimeDelayBeta2 + ",darkTimeDelayBrightness=" + this.darkTimeDelayBrightness);
                StringBuilder sb2 = new StringBuilder();
                sb2.append("printData() powerOnFastResponseLuxNum=");
                sb2.append(this.powerOnFastResponseLuxNum);
                sb2.append(",powerOnFastResponseTime=");
                sb2.append(this.powerOnFastResponseTime);
                Slog.i(HwBrightnessXmlLoader.TAG, sb2.toString());
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
                if (this.nightUpModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() nightUpModeEnable=" + this.nightUpModeEnable + ",nightUpModeSwitchTimeMillis=" + this.nightUpModeSwitchTimeMillis + ",nightUpModeBeginHourTime=" + this.nightUpModeBeginHourTime + ",nightUpModeEndHourTime=" + this.nightUpModeEndHourTime + ",nightUpModeLuxThreshold=" + this.nightUpModeLuxThreshold + ",nightUpModePowOnDimTime=" + this.nightUpModePowOnDimTime);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() dayModeAlgoEnable=" + this.dayModeAlgoEnable + ", dayModeSwitchTime=" + this.dayModeSwitchTime + ", dayModeBeginTime=" + this.dayModeBeginTime + ", dayModeEndTime=" + this.dayModeEndTime + ", dayModeModifyNumPoint=" + this.dayModeModifyNumPoint + ", dayModeModifyMinBrightness=" + this.dayModeModifyMinBrightness + ", dayModeDarkenMinLux=" + this.dayModeDarkenMinLux + ", offsetResetSwitchTime =" + this.offsetResetSwitchTime + ", offsetResetSwitchTimeForDarkMode =" + this.offsetResetSwitchTimeForDarkMode + ", offsetResetEnable=" + this.offsetResetEnable + ", offsetResetShortSwitchTime=" + this.offsetResetShortSwitchTime + ", offsetResetShortLuxDelta=" + this.offsetResetShortLuxDelta + ", offsetBrightenDebounceTime=" + this.offsetBrightenDebounceTime + ", offsetDarkenDebounceTime=" + this.offsetDarkenDebounceTime + ", offsetValidAmbientLuxEnable=" + this.offsetValidAmbientLuxEnable);
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
                if (this.isAutoModeSeekBarMaxBrightnessBasedLux) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() isAutoModeSeekBarMaxBrightnessBasedLux=" + this.isAutoModeSeekBarMaxBrightnessBasedLux + ",autoModeSeekBarMaxBrightnessLuxTh=" + this.autoModeSeekBarMaxBrightnessLuxTh + ",autoModeSeekBarMaxBrightnessValue=" + this.autoModeSeekBarMaxBrightnessValue);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenlinePoints=" + this.brightenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkenlinePoints=" + this.darkenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() defaultBrightness=" + this.defaultBrightness + ", brightnessCalibrationEnabled=" + this.brightnessCalibrationEnabled);
                StringBuilder sb5 = new StringBuilder();
                sb5.append("printData() defaultBrightnessLinePoints=");
                sb5.append(this.defaultBrightnessLinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, sb5.toString());
                if (this.dayModeNewCurveEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() dayModeBrightnessLinePoints=" + this.dayModeBrightnessLinePoints);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() coverModeBrightnessLinePoints=" + this.coverModeBrightnessLinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenGradualTime=" + this.brightenGradualTime + ", darkenGradualTime=" + this.darkenGradualTime + ", brightenThresholdFor255=" + this.brightenThresholdFor255 + ", darkenTargetFor255=" + this.darkenTargetFor255 + ", targetFor255BrightenTime=" + this.targetFor255BrightenTime + ", minAnimatingStep=" + this.minAnimatingStep + ", darkenCurrentFor255=" + this.darkenCurrentFor255 + ", autoFastTimeFor255=" + this.autoFastTimeFor255 + ", manualFastTimeFor255=" + this.manualFastTimeFor255);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() seekBarDimTime=" + this.seekBarDimTime + ",isLowEqualDarkenEnable=" + this.isLowEqualDarkenEnable + ",screenBrightnessNormalModeMaxNit=" + this.screenBrightnessNormalModeMaxNit + ",screenBrightnessNormalModeMaxLevel=" + this.screenBrightnessNormalModeMaxLevel + ",lowEqualDarkenLevel=" + this.lowEqualDarkenLevel + ",lowEqualDarkenNit=" + this.lowEqualDarkenNit + ",lowEqualDarkenMinLevel=" + this.lowEqualDarkenMinLevel + ",lowEqualDarkenMinTime=" + this.lowEqualDarkenMinTime + ",lowEqualDarkenMaxTime=" + this.lowEqualDarkenMaxTime);
                if (this.isSeekBarManualDimTimeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() isSeekBarManualDimTimeEnable=" + this.isSeekBarManualDimTimeEnable + ",seekBarManualBrightenDimTime=" + this.seekBarManualBrightenDimTime + ",seekBarManualDarkenDimTime=" + this.seekBarManualDarkenDimTime);
                }
                if (this.isLowPowerMappingEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() lowPowerMappingLevel=" + this.lowPowerMappingLevel + ",lowPowerMappingNit=" + this.lowPowerMappingNit + ",lowPowerMappingLevelRatio=" + this.lowPowerMappingLevelRatio);
                }
                if (this.isHighEqualDarkenEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() isHighEqualDarkenEnable=" + this.isHighEqualDarkenEnable + ",highEqualDarkenMinTime=" + this.highEqualDarkenMinTime + ",highEqualDarkenMaxTime=" + this.highEqualDarkenMaxTime);
                }
                if (this.isDarkenAnimatingStepForHbm) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() maxDarkenAnimatingStepForHbm=" + this.maxDarkenAnimatingStepForHbm + ",minDarkenBrightnessLevelForHbm=" + this.minDarkenBrightnessLevelForHbm);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "brightenFlickerTargetMin=" + this.brightenFlickerTargetMin + ",brightenFlickerAmountMin=" + this.brightenFlickerAmountMin + ",brightenFlickerGradualTimeMin=" + this.brightenFlickerGradualTimeMin + ",brightenFlickerGradualTimeMax=" + this.brightenFlickerGradualTimeMax + ",darkenNoFlickerTargetGradualTimeMin=" + this.darkenNoFlickerTargetGradualTimeMin + ",darkenNoFlickerTarget=" + this.darkenNoFlickerTarget + ",linearDimmingValueTh=" + this.linearDimmingValueTh + ",brightenTimeLongCurrentTh=" + this.brightenTimeLongCurrentTh + ",brightenTimeLongAmountMin=" + this.brightenTimeLongAmountMin + ",brightenGradualTimeLong=" + this.brightenGradualTimeLong);
                StringBuilder sb6 = new StringBuilder();
                sb6.append("printData() dimTime=");
                sb6.append(this.dimTime);
                sb6.append(", useVariableStep=");
                sb6.append(this.useVariableStep);
                sb6.append(", darkenGradualTimeMax=");
                sb6.append(this.darkenGradualTimeMax);
                sb6.append(", darkenGradualTimeMin=");
                sb6.append(this.darkenGradualTimeMin);
                sb6.append(", animatedStepRoundEnabled=");
                sb6.append(this.animatedStepRoundEnabled);
                sb6.append(", reportValueWhenSensorOnChange=");
                sb6.append(this.reportValueWhenSensorOnChange);
                sb6.append(", allowLabcUseProximity=");
                sb6.append(this.allowLabcUseProximity);
                sb6.append(", proximityPositiveDebounceTime=");
                sb6.append(this.proximityPositiveDebounceTime);
                sb6.append(", proximityNegativeDebounceTime=");
                sb6.append(this.proximityNegativeDebounceTime);
                Slog.i(HwBrightnessXmlLoader.TAG, sb6.toString());
                if (this.isDynamicEnableProximity) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() isDynamicEnableProximity=" + this.isDynamicEnableProximity + ",enableProximityDelayTime=" + this.enableProximityDelayTime + ",disableProximityDelayTime=" + this.disableProximityDelayTime + ",updateDarkAmbientLuxDelayTime=" + this.updateDarkAmbientLuxDelayTime);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() manualMode=" + this.manualMode + ", manualBrightnessMaxLimit=" + this.manualBrightnessMaxLimit + ", manualBrightnessMinLimit=" + this.manualBrightnessMinLimit + ", outDoorThreshold=" + this.outDoorThreshold + ", inDoorThreshold=" + this.inDoorThreshold + ", manualBrighenDebounceTime=" + this.manualBrighenDebounceTime + ", manualDarkenDebounceTime=" + this.manualDarkenDebounceTime);
                StringBuilder sb7 = new StringBuilder();
                sb7.append("printData() manualBrightenlinePoints=");
                sb7.append(this.manualBrightenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, sb7.toString());
                StringBuilder sb8 = new StringBuilder();
                sb8.append("printData() manualDarkenlinePoints=");
                sb8.append(this.manualDarkenlinePoints);
                Slog.i(HwBrightnessXmlLoader.TAG, sb8.toString());
                StringBuilder sb9 = new StringBuilder();
                sb9.append("printData() brightnessMappingPoints=");
                sb9.append(this.brightnessMappingPoints);
                Slog.i(HwBrightnessXmlLoader.TAG, sb9.toString());
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() ambientLuxValidBrightnessPoints=" + this.ambientLuxValidBrightnessPoints);
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() QRCodeBrightnessminLimit=" + this.QRCodeBrightnessminLimit);
                if (this.darkAdapterEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() unadapt2AdaptingShortFilterLux=" + this.unadapt2AdaptingShortFilterLux + ", unadapt2AdaptingLongFilterLux=" + this.unadapt2AdaptingLongFilterLux + ", unadapt2AdaptingLongFilterSec=" + this.unadapt2AdaptingLongFilterSec + ", unadapt2AdaptingDimSec=" + this.unadapt2AdaptingDimSec + ", adapting2UnadaptShortFilterLux=" + this.adapting2UnadaptShortFilterLux + ", adapting2AdaptedOffDurationMinSec=" + this.adapting2AdaptedOffDurationMinSec + ", adapting2AdaptedOffDurationFilterSec=" + this.adapting2AdaptedOffDurationFilterSec + ", adapting2AdaptedOffDurationMaxSec=" + this.adapting2AdaptedOffDurationMaxSec + ", adapting2AdaptedOnClockNoFilterBeginHour=" + this.adapting2AdaptedOnClockNoFilterBeginHour + ", adapting2AdaptedOnClockNoFilterEndHour=" + this.adapting2AdaptedOnClockNoFilterEndHour + ", unadapt2AdaptedShortFilterLux=" + this.unadapt2AdaptedShortFilterLux + ", unadapt2AdaptedOffDurationMinSec=" + this.unadapt2AdaptedOffDurationMinSec + ", unadapt2AdaptedOnClockNoFilterBeginHour=" + this.unadapt2AdaptedOnClockNoFilterBeginHour + ", unadapt2AdaptedOnClockNoFilterEndHour=" + this.unadapt2AdaptedOnClockNoFilterEndHour + ", adapted2UnadaptShortFilterLux=" + this.adapted2UnadaptShortFilterLux);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() pgReregisterScene=" + this.pgReregisterScene);
                if (this.updateBrightnessViewAlphaEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() updateBrightnessViewAlphaEnable=" + this.updateBrightnessViewAlphaEnable + ",darkenTargetForKeyguard=" + this.darkenTargetForKeyguard + ",minAnimatingStepForKeyguard=" + this.minAnimatingStepForKeyguard);
                }
                if (this.touchProximityEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() touchProximityYNearbyRatio=" + this.touchProximityYNearbyRatioMin + AwarenessConstants.SECOND_ACTION_SPLITE_TAG + this.touchProximityYNearbyRatioMax);
                    StringBuilder sb10 = new StringBuilder();
                    sb10.append("printData() proximitySceneModeEnable=");
                    sb10.append(this.proximitySceneModeEnable);
                    Slog.i(HwBrightnessXmlLoader.TAG, sb10.toString());
                }
                if (this.vehicleModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() veModeEnable=" + this.vehicleModeEnable + ",veModeDisableTimeMillis=" + this.vehicleModeDisableTimeMillis + ",veModeQuitTimeForPowerOn=" + this.vehicleModeQuitTimeForPowerOn + ",veModeEnterTimeForPowerOn=" + this.vehicleModeEnterTimeForPowerOn);
                }
                if (this.isWalkModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() isWaModeEnable=" + this.isWalkModeEnable + ",waModeMinLux=" + this.walkModeMinLux);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() gameModeEnable=" + this.gameModeEnable + ",gameModeBrightenAnimationTime=" + this.gameModeBrightenAnimationTime + ",gameModeDarkentenAnimationTime=" + this.gameModeDarkentenAnimationTime + ",gameModeDarkentenLongAnimationTime=" + this.gameModeDarkentenLongAnimationTime + ",gameModeDarkentenLongTarget=" + this.gameModeDarkentenLongTarget + ",gameModeDarkentenLongCurrent=" + this.gameModeDarkentenLongCurrent + ",gameModeClearOffsetTime=" + this.gameModeClearOffsetTime + ",gameModeBrightenDebounceTime=" + this.gameModeBrightenDebounceTime + ",gameModeDarkenDebounceTime=" + this.gameModeDarkenDebounceTime + ",gameModeLuxThresholdEnable=" + this.gameModeLuxThresholdEnable);
                if (this.isLandscapeGameModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() isLandscapeGameModeEnable=" + this.isLandscapeGameModeEnable + ",landscapeGameModeEnterDelayTime=" + this.landscapeGameModeEnterDelayTime + ",landscapeGameModeQuitDelayTime=" + this.landscapeGameModeQuitDelayTime + ",isLandscapeGameModeLuxThresholdEnable=" + this.isLandscapeGameModeLuxThresholdEnable + ",landscapeGameModeBrightenDebounceTime=" + this.landscapeGameModeBrightenDebounceTime + ",landscapeGameModeDarkenDebounceTime=" + this.landscapeGameModeDarkenDebounceTime);
                }
                if (this.isLandscapeGameModeEnable && this.isLandscapeGameModeLuxThresholdEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenLinePointsForLandscapeGameMode=" + this.brightenLinePointsForLandscapeGameMode);
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkenLinePointsForLandscapeGameMode=" + this.darkenLinePointsForLandscapeGameMode);
                }
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
                if (this.landscapeBrightnessModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() landscapeBrightnessModeEnable=" + this.landscapeBrightnessModeEnable + ",landscapeModeBrightenDebounceTime=" + this.landscapeModeBrightenDebounceTime + ",landscapeModeDarkenDebounceTime=" + this.landscapeModeDarkenDebounceTime + ",landscapeModeEnterDelayTime=" + this.landscapeModeEnterDelayTime + ",landscapeModeQuitDelayTime=" + this.landscapeModeQuitDelayTime + ",landscapeModeUseTouchProximity=" + this.landscapeModeUseTouchProximity);
                    StringBuilder sb11 = new StringBuilder();
                    sb11.append("printData() brightenlinePointsForLandscapeMode=");
                    sb11.append(this.brightenlinePointsForLandscapeMode);
                    Slog.i(HwBrightnessXmlLoader.TAG, sb11.toString());
                    StringBuilder sb12 = new StringBuilder();
                    sb12.append("printData() darkenlinePointsForLandscapeMode=");
                    sb12.append(this.darkenlinePointsForLandscapeMode);
                    Slog.i(HwBrightnessXmlLoader.TAG, sb12.toString());
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
                if (this.secondDarkenModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() secondDarkenModeMinLuxTh=" + this.secondDarkenModeMinLuxTh + ",secondDarkenModeMaxLuxTh=" + this.secondDarkenModeMaxLuxTh + ",secondDarkenModeDarkenDeltaLuxRatio=" + this.secondDarkenModeDarkenDeltaLuxRatio + ",secondDarkenModeDarkenDebounceTime=" + this.secondDarkenModeDarkenDebounceTime + ",secondDarkenModeNoResponseDarkenTime=" + this.secondDarkenModeNoResponseDarkenTime + ",secondDarkenModeNoResponseDarkenTimeMin=" + this.secondDarkenModeNoResponseDarkenTimeMin + ",secondDarkenModeAfterNoResponseCheckTime=" + this.secondDarkenModeAfterNoResponseCheckTime);
                }
                if (this.brightnessOffsetLuxModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightenOffsetLuxTh1=" + this.brightenOffsetLuxTh1 + ",brightenOffsetLuxTh2=" + this.brightenOffsetLuxTh2 + ",brightenOffsetLuxTh3=" + this.brightenOffsetLuxTh3 + ",brightenOffsetNoValidDarkenLuxTh1=" + this.brightenOffsetNoValidDarkenLuxTh1 + ",brightenOffsetNoValidDarkenLuxTh2=" + this.brightenOffsetNoValidDarkenLuxTh2 + ",brightenOffsetNoValidDarkenLuxTh3=" + this.brightenOffsetNoValidDarkenLuxTh3 + ",brightenOffsetNoValidDarkenLuxTh4=" + this.brightenOffsetNoValidDarkenLuxTh4 + ",brightenOffsetNoValidBrightenLuxTh1=" + this.brightenOffsetNoValidBrightenLuxTh1 + ",brightenOffsetNoValidBrightenLuxTh2=" + this.brightenOffsetNoValidBrightenLuxTh2 + ",brightenOffsetNoValidBrightenLuxTh3=" + this.brightenOffsetNoValidBrightenLuxTh3 + ",brightenOffsetNoValidBrightenLuxTh4=" + this.brightenOffsetNoValidBrightenLuxTh4 + ",darkenOffsetLuxTh1=" + this.darkenOffsetLuxTh1 + ",darkenOffsetLuxTh2=" + this.darkenOffsetLuxTh2 + ",darkenOffsetLuxTh3=" + this.darkenOffsetLuxTh3 + ",darkenOffsetNoValidBrightenLuxTh1=" + this.darkenOffsetNoValidBrightenLuxTh1 + ",darkenOffsetNoValidBrightenLuxTh2=" + this.darkenOffsetNoValidBrightenLuxTh2 + ",darkenOffsetNoValidBrightenLuxTh3=" + this.darkenOffsetNoValidBrightenLuxTh3 + ",darkenOffsetNoValidBrightenLuxTh4=" + this.darkenOffsetNoValidBrightenLuxTh4 + ",brightenOffsetEffectMinLuxEnable=" + this.brightenOffsetEffectMinLuxEnable + ",brightnessOffsetTmpValidEnable=" + this.brightnessOffsetTmpValidEnable + ",brightenOffsetNoValidSavedLuxTh1=" + this.brightenOffsetNoValidSavedLuxTh1 + ",brightenOffsetNoValidSavedLuxTh2=" + this.brightenOffsetNoValidSavedLuxTh2);
                }
                if (this.twoPointOffsetModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() twoPointOffsetLuxTh=" + this.twoPointOffsetLuxTh + ",twoPointOffsetAdjionLuxTh=" + this.twoPointOffsetAdjionLuxTh + ",twoPointOffsetNoValidLuxTh=" + this.twoPointOffsetNoValidLuxTh + ",lowBrightenOffsetNoValidBrightenLuxTh=" + this.lowBrightenOffsetNoValidBrightenLuxTh + ",lowDarkenOffsetNoValidBrightenLuxTh=" + this.lowDarkenOffsetNoValidBrightenLuxTh + ",lowBrightenOffsetNoValidDarkenLuxTh=" + this.lowBrightenOffsetNoValidDarkenLuxTh + ",lowDarkenOffsetNoValidDarkenLuxTh=" + this.lowDarkenOffsetNoValidDarkenLuxTh + ",lowDarkenOffsetDarkenBrightnessRatio=" + this.lowDarkenOffsetDarkenBrightnessRatio + ",highBrightenOffsetNoValidBrightenLuxTh=" + this.highBrightenOffsetNoValidBrightenLuxTh + ",highDarkenOffsetNoValidBrightenLuxTh=" + this.highDarkenOffsetNoValidBrightenLuxTh + ",highBrightenOffsetNoValidDarkenLuxTh=" + this.highBrightenOffsetNoValidDarkenLuxTh + ",highDarkenOffsetNoValidDarkenLuxTh=" + this.highDarkenOffsetNoValidDarkenLuxTh);
                } else {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() twoPointOffsetModeEnable=" + this.twoPointOffsetModeEnable);
                }
                if (this.videoFullScreenModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() videoFullScreenModeEnable=" + this.videoFullScreenModeEnable + ",videoFullScreenModeDelayTime=" + this.videoFullScreenModeDelayTime + ",brightnessLineForVideoFullScreenMode=" + this.brightnessLineForVideoFullScreenMode);
                }
                if (this.keyguardUnlockedFastDarkenEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() keyguardUnlockedFastDarkenEnable=" + this.keyguardUnlockedFastDarkenEnable + ",keyguardUnlockedFastDarkenMaxLux=" + this.keyguardUnlockedFastDarkenMaxLux + ",keyguardUnlockedDarkenDebounceTime=" + this.keyguardUnlockedDarkenDebounceTime + ",keyguardUnlockedDarkenRatio=" + this.keyguardUnlockedDarkenRatio + ",keyguardUnlockedFastDarkenValidTime=" + this.keyguardUnlockedFastDarkenValidTime + ",keyguardUnlockedLuxDeltaValidTime=" + this.keyguardUnlockedLuxDeltaValidTime + ",keyguardBrightenLuxDeltaMin=" + this.keyguardBrightenLuxDeltaMin + ",keyguardUnlockedDimmingTime=" + this.keyguardUnlockedDimmingTime + ",keyguardFastDimBrightness=" + this.keyguardFastDimBrightness + ",keyguardFastDimTime=" + this.keyguardFastDimTime);
                }
                if (this.foldScreenModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() foldScreenModeEnable=" + this.foldScreenModeEnable + ",isInwardFoldScreenLuxEnable=" + this.isInwardFoldScreenLuxEnable);
                }
                if (this.darkModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() darkModeEnable=" + this.darkModeEnable + ",darkModeOffsetMinBrightness=" + this.darkModeOffsetMinBrightness);
                }
                if (this.darkModeEnable && this.isDarkModeCurveEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightnessLineForDarkMode=" + this.brightnessLineForDarkMode);
                }
                if (this.batteryModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() batteryLowLevelTh=" + this.batteryLowLevelTh + ",batteryLowLevelMaxBrightness=" + this.batteryLowLevelMaxBrightness);
                }
                if (this.powerSavingModeBatteryLowLevelEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() powerSavingModeBatteryLowLevelThreshold=" + this.powerSavingModeBatteryLowLevelThreshold + ",powerSavingModeBatteryLowLevelBrightnessRatio=" + this.powerSavingModeBatteryLowLevelBrightnessRatio);
                }
                if (this.luxMinMaxBrightnessEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() luxMinMaxBrightnessPoints=" + this.luxMinMaxBrightnessPoints);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() gameDisableAutoBrightnessModeEnable=" + this.gameDisableAutoBrightnessModeEnable);
                if (this.frontCameraMaxBrightnessEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() frontCameraLuxThreshold=" + this.frontCameraLuxThreshold + ",frontCameraBrightenLuxThreshold=" + this.frontCameraBrightenLuxThreshold + ",frontCameraDarkenLuxThreshold=" + this.frontCameraDarkenLuxThreshold + ",frontCameraBrighenDebounceTime=" + this.frontCameraBrighenDebounceTime + ",frontCameraDarkenDebounceTime" + this.frontCameraDarkenDebounceTime + ",frontCameraDimmingBrightenTime=" + this.frontCameraDimmingBrightenTime + ",frontCameraDimmingDarkenTime=" + this.frontCameraDimmingDarkenTime + ",frontCameraUpdateBrightnessDelayTime=" + this.frontCameraUpdateBrightnessDelayTime + ",frontCameraUpdateDimmingEnableTime=" + this.frontCameraUpdateDimmingEnableTime + ",frontCameraMaxBrightness=" + this.frontCameraMaxBrightness);
                }
                if (this.brightnessMappingForWindowBrightnessEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightnessMappingPointsForWindowBrightness=" + this.brightnessMappingPointsForWindowBrightness);
                }
                if (this.hdrModeEnable) {
                    Slog.i(HwBrightnessXmlLoader.TAG, "printData() brightnessMappingPointsForHdrMode=" + this.brightnessMappingPointsForHdrMode + ",hdrModeCurveEnable=" + this.hdrModeCurveEnable + ",hdrModeWindowMappingEnable=" + this.hdrModeWindowMappingEnable + ",brightnessLineForHdrMode=" + this.brightnessLineForHdrMode);
                }
                Slog.i(HwBrightnessXmlLoader.TAG, "printData() maxValidAmbientLux=" + this.maxValidAmbientLux + ",sIsNeedMaxAmbientLuxPoint=" + HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint);
            }
        }

        public void loadDefaultConfig() {
            if (HwBrightnessXmlLoader.HWFLOW) {
                Slog.i(HwBrightnessXmlLoader.TAG, "loadDefaultConfig()");
            }
            this.lightSensorRateMills = 300;
            this.brighenDebounceTime = 1000;
            this.darkenDebounceTime = HwAPPQoEUtils.APP_TYPE_STREAMING;
            this.brightenDebounceTimeParaBig = 0.0f;
            this.darkenDebounceTimeParaBig = 1.0f;
            this.brightenDeltaLuxPara = 0.0f;
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
            this.coverModeDayBrightness = 154;
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
            this.longTimeFilterEnable = false;
            this.longTimeNoFilterNum = 7;
            this.longTimeFilterNum = 3;
            this.longTimeFilterLuxTh = 500.0f;
            this.luxRoundEnable = true;
            this.ambientLuxMinRound = -1.0f;
            this.luxThNoResponseForSmallThr = -1.0f;
            this.homeModeEnable = false;
            this.homeModeDayBeginTime = 5;
            this.homeModeDayEndTime = 23;
            this.homeModeSwitchTime = 30;
            this.darkTimeDelayBeta0 = 0.0f;
            this.darkTimeDelayBeta1 = 1.0f;
            this.darkTimeDelayBeta2 = 0.333f;
            this.darkTimeDelayBrightness = 0.0f;
            this.powerOnFastResponseLuxNum = 8;
            this.powerOnFastResponseTime = 2000;
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
            this.pgSceneDetectionDarkenDelayTime = DefaultGestureNavConst.CHECK_AFT_TIMEOUT;
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
            this.nightUpModeEnable = false;
            this.nightUpModeSwitchTimeMillis = 1800000;
            this.nightUpModeBeginHourTime = 0;
            this.nightUpModeEndHourTime = 5;
            this.nightUpModeLuxThreshold = 0.0f;
            this.nightUpModePowOnDimTime = 0.5f;
            this.dayModeAlgoEnable = false;
            this.dayModeSwitchTime = 30;
            this.dayModeBeginTime = 5;
            this.dayModeEndTime = 23;
            this.dayModeModifyNumPoint = 3;
            this.dayModeModifyMinBrightness = 6;
            this.dayModeDarkenMinLux = 0.0f;
            this.offsetResetSwitchTime = 10;
            this.offsetResetSwitchTimeForDarkMode = -1;
            this.offsetResetEnable = false;
            this.offsetResetShortSwitchTime = 10;
            this.offsetResetShortLuxDelta = 50000;
            this.offsetBrightenDebounceTime = 1000;
            this.offsetDarkenDebounceTime = 1000;
            this.offsetValidAmbientLuxEnable = false;
            this.autoModeInOutDoorLimitEnble = false;
            this.isAutoModeSeekBarMaxBrightnessBasedLux = false;
            this.autoModeSeekBarMaxBrightnessLuxTh = 2000.0f;
            this.autoModeSeekBarMaxBrightnessValue = 255;
            this.darkLightLevelMinThreshold = 0;
            this.darkLightLevelMaxThreshold = 0;
            this.darkLightLevelRatio = 1.0f;
            this.darkLightLuxMinThreshold = 0.0f;
            this.darkLightLuxMaxThreshold = 0.0f;
            this.darkLightLuxDelta = 0.0f;
            this.animatingForRGBWEnable = false;
            this.rebootFirstBrightnessAnimationEnable = false;
            this.rebootFirstBrightness = 10000;
            this.rebootFirstBrightnessAutoTime = 3.0f;
            this.rebootFirstBrightnessManualTime = 3.0f;
            this.monitorEnable = false;
            this.brightenlinePoints.clear();
            this.brightenlinePoints.add(new PointF(0.0f, 15.0f));
            this.brightenlinePoints.add(new PointF(2.0f, 15.0f));
            this.brightenlinePoints.add(new PointF(10.0f, 19.0f));
            this.brightenlinePoints.add(new PointF(20.0f, 219.0f));
            this.brightenlinePoints.add(new PointF(100.0f, 539.0f));
            this.brightenlinePoints.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePoints.add(new PointF(40000.0f, 989.0f));
            this.darkenlinePoints.clear();
            this.darkenlinePoints.add(new PointF(0.0f, 1.0f));
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
            this.defaultBrightnessLinePoints.clear();
            this.defaultBrightnessLinePoints.add(new PointF(0.0f, 4.0f));
            this.defaultBrightnessLinePoints.add(new PointF(25.0f, 46.5f));
            this.defaultBrightnessLinePoints.add(new PointF(1995.0f, 140.7f));
            this.defaultBrightnessLinePoints.add(new PointF(4000.0f, 255.0f));
            this.defaultBrightnessLinePoints.add(new PointF(40000.0f, 255.0f));
            this.dayModeNewCurveEnable = false;
            this.dayModeBrightnessLinePoints.clear();
            this.dayModeBrightnessLinePoints.add(new PointF(0.0f, 4.0f));
            this.dayModeBrightnessLinePoints.add(new PointF(25.0f, 46.5f));
            this.dayModeBrightnessLinePoints.add(new PointF(1995.0f, 140.7f));
            this.dayModeBrightnessLinePoints.add(new PointF(4000.0f, 255.0f));
            this.dayModeBrightnessLinePoints.add(new PointF(40000.0f, 255.0f));
            this.coverModeBrightnessLinePoints.clear();
            this.coverModeBrightnessLinePoints.add(new PointF(0.0f, 20.0f));
            this.coverModeBrightnessLinePoints.add(new PointF(25.0f, 46.5f));
            this.coverModeBrightnessLinePoints.add(new PointF(250.0f, 100.0f));
            this.coverModeBrightnessLinePoints.add(new PointF(1995.0f, 154.7f));
            this.coverModeBrightnessLinePoints.add(new PointF(4000.0f, 255.0f));
            this.coverModeBrightnessLinePoints.add(new PointF(40000.0f, 255.0f));
            this.brightenGradualTime = 1.0f;
            this.darkenGradualTime = 3.0f;
            this.brightenThresholdFor255 = 1254;
            this.darkenTargetFor255 = 1254;
            this.targetFor255BrightenTime = 0.0f;
            this.minAnimatingStep = 0.0f;
            this.darkenCurrentFor255 = DeviceStatusConstant.TYPE_HEAD_DOWN;
            this.autoFastTimeFor255 = 0.5f;
            this.manualFastTimeFor255 = 0.5f;
            this.dimTime = 3.0f;
            this.seekBarDimTime = 0.5f;
            this.isSeekBarManualDimTimeEnable = false;
            this.seekBarManualBrightenDimTime = 0.5f;
            this.seekBarManualDarkenDimTime = 0.5f;
            this.isLowEqualDarkenEnable = false;
            this.screenBrightnessNormalModeMaxNit = 500.0f;
            this.screenBrightnessNormalModeMaxLevel = 8824.0f;
            this.lowEqualDarkenLevel = 666.0f;
            this.lowEqualDarkenNit = 30.0f;
            this.lowEqualDarkenMinLevel = 200.0f;
            this.lowEqualDarkenMinTime = 8.0f;
            this.lowEqualDarkenMaxTime = 25.0f;
            this.isLowPowerMappingEnable = false;
            this.lowPowerMappingLevel = 784.0f;
            this.lowPowerMappingNit = 20.0f;
            this.lowPowerMappingLevelRatio = 1.0f;
            this.isHighEqualDarkenEnable = false;
            this.highEqualDarkenMinTime = 8.0f;
            this.highEqualDarkenMaxTime = 20.0f;
            this.isDarkenAnimatingStepForHbm = false;
            this.maxDarkenAnimatingStepForHbm = 10.0f;
            this.minDarkenBrightnessLevelForHbm = 8824.0f;
            this.brightenFlickerTargetMin = 0;
            this.brightenFlickerAmountMin = 41;
            this.brightenFlickerGradualTimeMin = 0.5f;
            this.brightenFlickerGradualTimeMax = 3.0f;
            this.darkenNoFlickerTargetGradualTimeMin = 15.0f;
            this.darkenNoFlickerTarget = 0.0f;
            this.linearDimmingValueTh = 0;
            this.brightenTimeLongCurrentTh = 0;
            this.brightenTimeLongAmountMin = 0;
            this.brightenGradualTimeLong = 1.0f;
            this.useVariableStep = false;
            this.darkenGradualTimeMax = 3.0f;
            this.darkenGradualTimeMin = 0.0f;
            this.animatedStepRoundEnabled = false;
            this.reportValueWhenSensorOnChange = true;
            this.allowLabcUseProximity = false;
            this.proximityPositiveDebounceTime = WMStateCons.MSG_POWER_CONNECTED;
            this.proximityNegativeDebounceTime = 3000;
            this.isDynamicEnableProximity = false;
            this.enableProximityDelayTime = 10;
            this.disableProximityDelayTime = 10000;
            this.updateDarkAmbientLuxDelayTime = 300;
            this.manualMode = false;
            this.manualBrightnessMaxLimit = 255;
            this.manualBrightnessMinLimit = 4;
            this.QRCodeBrightnessminLimit = -1;
            this.outDoorThreshold = 8000;
            this.inDoorThreshold = HwAPPQoEUtils.APP_TYPE_STREAMING;
            this.manualBrighenDebounceTime = 3000;
            this.manualDarkenDebounceTime = 3000;
            this.manualBrightenlinePoints.clear();
            this.manualBrightenlinePoints.add(new PointF(0.0f, 1000.0f));
            this.manualBrightenlinePoints.add(new PointF(1000.0f, 5000.0f));
            this.manualBrightenlinePoints.add(new PointF(40000.0f, 10000.0f));
            this.manualDarkenlinePoints.clear();
            this.manualDarkenlinePoints.add(new PointF(0.0f, 1.0f));
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
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(0.0f, 4.0f, 55.0f));
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(100.0f, 4.0f, 255.0f));
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(1000.0f, 4.0f, 255.0f));
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(5000.0f, 50.0f, 255.0f));
            this.ambientLuxValidBrightnessPoints.add(new HwXmlAmPoint(40000.0f, 50.0f, 255.0f));
            this.darkAdapterEnable = false;
            this.pgReregisterScene = false;
            this.updateBrightnessViewAlphaEnable = false;
            this.darkenTargetForKeyguard = 0.0f;
            this.minAnimatingStepForKeyguard = 0.0f;
            this.keyguardUnlockedFastDarkenEnable = false;
            this.keyguardUnlockedFastDarkenMaxLux = 20.0f;
            this.keyguardUnlockedDarkenDebounceTime = 1000;
            this.keyguardUnlockedDarkenRatio = 1.0f;
            this.keyguardUnlockedFastDarkenValidTime = 5000;
            this.keyguardUnlockedLuxDeltaValidTime = 2000;
            this.keyguardBrightenLuxDeltaMin = 0.0f;
            this.keyguardUnlockedDimmingTime = 1.0f;
            this.keyguardFastDimBrightness = 0;
            this.keyguardFastDimTime = 0.5f;
            this.touchProximityEnable = false;
            this.touchProximityYNearbyRatioMin = 0.0f;
            this.touchProximityYNearbyRatioMax = 1.0f;
            this.proximitySceneModeEnable = false;
            this.vehicleModeEnable = false;
            this.vehicleModeDisableTimeMillis = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
            this.vehicleModeQuitTimeForPowerOn = 200;
            this.vehicleModeEnterTimeForPowerOn = 200;
            this.isWalkModeEnable = false;
            this.walkModeMinLux = 1.0f;
            this.gameModeEnable = false;
            this.gameModeBrightenAnimationTime = 0.5f;
            this.gameModeDarkentenAnimationTime = 0.5f;
            this.gameModeDarkentenLongAnimationTime = 0.5f;
            this.gameModeDarkentenLongTarget = 0;
            this.gameModeDarkentenLongCurrent = 0;
            this.gameModeClearOffsetTime = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
            this.gameModeBrightenDebounceTime = 1000;
            this.gameModeDarkenDebounceTime = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
            this.gameModeLuxThresholdEnable = false;
            this.gameModeBrightnessLimitationEnable = false;
            this.gameModeBrightnessFloor = 4.0f;
            this.brightenlinePointsForGameMode.clear();
            this.brightenlinePointsForGameMode.add(new PointF(0.0f, 15.0f));
            this.brightenlinePointsForGameMode.add(new PointF(20.0f, 219.0f));
            this.brightenlinePointsForGameMode.add(new PointF(100.0f, 539.0f));
            this.brightenlinePointsForGameMode.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePointsForGameMode.add(new PointF(40000.0f, 989.0f));
            this.darkenlinePointsForGameMode.clear();
            this.darkenlinePointsForGameMode.add(new PointF(0.0f, 1.0f));
            this.darkenlinePointsForGameMode.add(new PointF(1.0f, 1.0f));
            this.darkenlinePointsForGameMode.add(new PointF(20.0f, 20.0f));
            this.darkenlinePointsForGameMode.add(new PointF(100.0f, 80.0f));
            this.darkenlinePointsForGameMode.add(new PointF(1800.0f, 600.0f));
            this.darkenlinePointsForGameMode.add(new PointF(40000.0f, 38800.0f));
            this.gameModeOffsetValidAmbientLuxEnable = false;
            this.gameModeAmbientLuxValidBrightnessPoints.clear();
            this.isLandscapeGameModeEnable = false;
            this.landscapeGameModeEnterDelayTime = 200;
            this.landscapeGameModeQuitDelayTime = 600;
            this.landscapeGameModeBrightenDebounceTime = 1000;
            this.landscapeGameModeDarkenDebounceTime = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
            this.isLandscapeGameModeLuxThresholdEnable = false;
            this.brightenLinePointsForLandscapeGameMode.clear();
            this.brightenLinePointsForLandscapeGameMode.add(new PointF(0.0f, 15.0f));
            this.brightenLinePointsForLandscapeGameMode.add(new PointF(20.0f, 219.0f));
            this.brightenLinePointsForLandscapeGameMode.add(new PointF(100.0f, 539.0f));
            this.brightenLinePointsForLandscapeGameMode.add(new PointF(1000.0f, 989.0f));
            this.brightenLinePointsForLandscapeGameMode.add(new PointF(40000.0f, 989.0f));
            this.darkenLinePointsForLandscapeGameMode.clear();
            this.darkenLinePointsForLandscapeGameMode.add(new PointF(0.0f, 1.0f));
            this.darkenLinePointsForLandscapeGameMode.add(new PointF(1.0f, 1.0f));
            this.darkenLinePointsForLandscapeGameMode.add(new PointF(20.0f, 20.0f));
            this.darkenLinePointsForLandscapeGameMode.add(new PointF(100.0f, 80.0f));
            this.darkenLinePointsForLandscapeGameMode.add(new PointF(1800.0f, 600.0f));
            this.darkenLinePointsForLandscapeGameMode.add(new PointF(40000.0f, 38800.0f));
            this.dcModeEnable = false;
            this.dcModeBrightenDebounceTime = 1000;
            this.dcModeDarkenDebounceTime = 1000;
            this.dcModeLuxThresholdEnable = false;
            this.brightenlinePointsForDcMode.clear();
            this.brightenlinePointsForDcMode.add(new PointF(0.0f, 15.0f));
            this.brightenlinePointsForDcMode.add(new PointF(20.0f, 219.0f));
            this.brightenlinePointsForDcMode.add(new PointF(100.0f, 539.0f));
            this.brightenlinePointsForDcMode.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePointsForDcMode.add(new PointF(40000.0f, 989.0f));
            this.darkenlinePointsForDcMode.clear();
            this.darkenlinePointsForDcMode.add(new PointF(0.0f, 1.0f));
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
            this.cryogenicActiveScreenOffIntervalInMillis = 1800000;
            this.cryogenicLagTimeInMillis = 1800000;
            this.landscapeBrightnessModeEnable = false;
            this.landscapeModeBrightenDebounceTime = 1000;
            this.landscapeModeDarkenDebounceTime = 1000;
            this.landscapeModeEnterDelayTime = 500;
            this.landscapeModeQuitDelayTime = 1000;
            this.landscapeModeUseTouchProximity = false;
            this.brightenlinePointsForLandscapeMode.clear();
            this.brightenlinePointsForLandscapeMode.add(new PointF(0.0f, 15.0f));
            this.brightenlinePointsForLandscapeMode.add(new PointF(20.0f, 219.0f));
            this.brightenlinePointsForLandscapeMode.add(new PointF(100.0f, 539.0f));
            this.brightenlinePointsForLandscapeMode.add(new PointF(1000.0f, 989.0f));
            this.brightenlinePointsForLandscapeMode.add(new PointF(40000.0f, 989.0f));
            this.darkenlinePointsForLandscapeMode.clear();
            this.darkenlinePointsForLandscapeMode.add(new PointF(0.0f, 1.0f));
            this.darkenlinePointsForLandscapeMode.add(new PointF(1.0f, 1.0f));
            this.darkenlinePointsForLandscapeMode.add(new PointF(20.0f, 20.0f));
            this.darkenlinePointsForLandscapeMode.add(new PointF(100.0f, 80.0f));
            this.darkenlinePointsForLandscapeMode.add(new PointF(1800.0f, 600.0f));
            this.darkenlinePointsForLandscapeMode.add(new PointF(40000.0f, 38800.0f));
            this.brightnessLevelToNitMappingEnable = false;
            this.brightnessLevelToNitLinePoints.clear();
            this.brightnessLevelToNitLinePoints.add(new PointF(0.0f, 2.0f));
            this.brightnessLevelToNitLinePoints.add(new PointF(255.0f, 500.0f));
            this.resetAmbientLuxEnable = false;
            this.resetAmbientLuxTh = 0.0f;
            this.resetAmbientLuxThMin = 0.0f;
            this.resetAmbientLuxDarkenDebounceTime = 10000;
            this.resetAmbientLuxBrightenDebounceTime = 1000;
            this.resetAmbientLuxFastDarkenValidTime = 0;
            this.resetAmbientLuxDarkenRatio = 1.0f;
            this.resetAmbientLuxGraTime = 0.0f;
            this.resetAmbientLuxFastDarkenDimmingTime = 0.0f;
            this.resetAmbientLuxStartBrightness = 200.0f;
            this.resetAmbientLuxThMax = 0.0f;
            this.resetAmbientLuxStartBrightnessMax = 200.0f;
            this.resetAmbientLuxDisableBrightnessOffset = 40;
            this.brightnessChageUpDelayTime = 50;
            this.brightnessChageDownDelayTime = 200;
            this.brightnessChageDefaultDelayTime = 500;
            this.luxlinePointsForBrightnessLevelEnable = false;
            this.brightenlinePointsForBrightnessLevel.clear();
            this.brightenlinePointsForBrightnessLevel.add(new PointF(0.0f, 2.0f));
            this.brightenlinePointsForBrightnessLevel.add(new PointF(255.0f, 5.0f));
            this.darkenlinePointsForBrightnessLevel.clear();
            this.darkenlinePointsForBrightnessLevel.add(new PointF(0.0f, 2.0f));
            this.darkenlinePointsForBrightnessLevel.add(new PointF(255.0f, 5.0f));
            this.secondDarkenModeEnable = false;
            this.secondDarkenModeMinLuxTh = 0.0f;
            this.secondDarkenModeMaxLuxTh = 0.0f;
            this.secondDarkenModeDarkenDeltaLuxRatio = 0.0f;
            this.secondDarkenModeDarkenDebounceTime = 0;
            this.secondDarkenModeNoResponseDarkenTime = 0;
            this.secondDarkenModeNoResponseDarkenTimeMin = HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM;
            this.secondDarkenModeAfterNoResponseCheckTime = 2000;
            this.brightnessOffsetLuxModeEnable = false;
            this.brightenOffsetLuxTh1 = 0.0f;
            this.brightenOffsetLuxTh2 = 0.0f;
            this.brightenOffsetLuxTh3 = 0.0f;
            this.brightenOffsetNoValidDarkenLuxTh1 = -1.0f;
            this.brightenOffsetNoValidDarkenLuxTh2 = -1.0f;
            this.brightenOffsetNoValidDarkenLuxTh3 = -1.0f;
            this.brightenOffsetNoValidDarkenLuxTh4 = -1.0f;
            this.brightenOffsetNoValidBrightenLuxTh1 = -1.0f;
            this.brightenOffsetNoValidBrightenLuxTh2 = -1.0f;
            this.brightenOffsetNoValidBrightenLuxTh3 = -1.0f;
            this.brightenOffsetNoValidBrightenLuxTh4 = -1.0f;
            this.darkenOffsetLuxTh1 = 0.0f;
            this.darkenOffsetLuxTh2 = 0.0f;
            this.darkenOffsetLuxTh3 = 0.0f;
            this.darkenOffsetNoValidBrightenLuxTh1 = -1.0f;
            this.darkenOffsetNoValidBrightenLuxTh2 = -1.0f;
            this.darkenOffsetNoValidBrightenLuxTh3 = -1.0f;
            this.darkenOffsetNoValidBrightenLuxTh4 = -1.0f;
            this.brightenOffsetEffectMinLuxEnable = false;
            this.brightnessOffsetTmpValidEnable = false;
            this.brightenOffsetNoValidSavedLuxTh1 = 0.0f;
            this.brightenOffsetNoValidSavedLuxTh2 = 0.0f;
            this.twoPointOffsetModeEnable = false;
            this.twoPointOffsetLuxTh = 50.0f;
            this.twoPointOffsetAdjionLuxTh = 50.0f;
            this.twoPointOffsetNoValidLuxTh = 50.0f;
            this.lowBrightenOffsetNoValidBrightenLuxTh = 500.0f;
            this.lowDarkenOffsetNoValidBrightenLuxTh = 500.0f;
            this.lowBrightenOffsetNoValidDarkenLuxTh = 0.0f;
            this.lowDarkenOffsetNoValidDarkenLuxTh = 0.0f;
            this.lowDarkenOffsetDarkenBrightnessRatio = 0.2f;
            this.highBrightenOffsetNoValidBrightenLuxTh = 10000.0f;
            this.highDarkenOffsetNoValidBrightenLuxTh = 10000.0f;
            this.highBrightenOffsetNoValidDarkenLuxTh = 10.0f;
            this.highDarkenOffsetNoValidDarkenLuxTh = 10.0f;
            this.videoFullScreenModeEnable = false;
            this.videoFullScreenModeDelayTime = 10;
            this.brightnessLineForVideoFullScreenMode.clear();
            this.brightnessLineForVideoFullScreenMode.add(new PointF(0.0f, 15.0f));
            this.brightnessLineForVideoFullScreenMode.add(new PointF(20.0f, 219.0f));
            this.brightnessLineForVideoFullScreenMode.add(new PointF(100.0f, 539.0f));
            this.brightnessLineForVideoFullScreenMode.add(new PointF(1000.0f, 989.0f));
            this.brightnessLineForVideoFullScreenMode.add(new PointF(40000.0f, 989.0f));
            this.keyguardUnlockedFastDarkenEnable = false;
            this.keyguardUnlockedFastDarkenMaxLux = 20.0f;
            this.keyguardUnlockedDarkenDebounceTime = 1000;
            this.keyguardUnlockedDarkenRatio = 1.0f;
            this.keyguardUnlockedFastDarkenValidTime = 5000;
            this.keyguardUnlockedLuxDeltaValidTime = 2000;
            this.keyguardBrightenLuxDeltaMin = 0.0f;
            this.keyguardUnlockedDimmingTime = 1.0f;
            this.keyguardFastDimBrightness = 0;
            this.keyguardFastDimTime = 0.5f;
            this.foldScreenModeEnable = false;
            this.isInwardFoldScreenLuxEnable = false;
            this.darkModeEnable = false;
            this.darkModeOffsetMinBrightness = 4;
            this.isDarkModeCurveEnable = false;
            this.brightnessLineForDarkMode.clear();
            this.brightnessLineForDarkMode.add(new PointF(0.0f, 20.0f));
            this.brightnessLineForDarkMode.add(new PointF(25.0f, 46.5f));
            this.brightnessLineForDarkMode.add(new PointF(250.0f, 100.0f));
            this.brightnessLineForDarkMode.add(new PointF(1995.0f, 154.7f));
            this.brightnessLineForDarkMode.add(new PointF(10000.0f, 225.0f));
            this.brightnessLineForDarkMode.add(new PointF(40000.0f, 255.0f));
            this.batteryModeEnable = false;
            this.powerSavingModeBatteryLowLevelEnable = true;
            this.powerSavingModeBatteryLowLevelThreshold = 10;
            this.powerSavingModeBatteryLowLevelBrightnessRatio = 60;
            this.batteryLowLevelTh = 0;
            this.batteryLowLevelMaxBrightness = 255;
            this.luxMinMaxBrightnessEnable = false;
            this.luxMinMaxBrightnessPoints.clear();
            this.luxMinMaxBrightnessPoints.add(new HwXmlAmPoint(0.0f, 4.0f, 255.0f));
            this.luxMinMaxBrightnessPoints.add(new HwXmlAmPoint(100.0f, 4.0f, 255.0f));
            this.luxMinMaxBrightnessPoints.add(new HwXmlAmPoint(40000.0f, 4.0f, 255.0f));
            this.gameDisableAutoBrightnessModeEnable = true;
            this.frontCameraMaxBrightnessEnable = false;
            this.frontCameraLuxThreshold = 0.0f;
            this.frontCameraBrightenLuxThreshold = 0.0f;
            this.frontCameraDarkenLuxThreshold = 0.0f;
            this.frontCameraBrighenDebounceTime = 3000;
            this.frontCameraDarkenDebounceTime = 3000;
            this.frontCameraDimmingBrightenTime = 4.0f;
            this.frontCameraDimmingDarkenTime = 6.0f;
            this.frontCameraUpdateBrightnessDelayTime = 100;
            this.frontCameraUpdateDimmingEnableTime = 500;
            this.frontCameraMaxBrightness = 255;
            this.brightnessMappingForWindowBrightnessEnable = false;
            this.brightnessMappingPointsForWindowBrightness.clear();
            this.brightnessMappingPointsForWindowBrightness.add(new PointF(4.0f, 4.0f));
            this.brightnessMappingPointsForWindowBrightness.add(new PointF(225.0f, 225.0f));
            this.brightnessMappingPointsForWindowBrightness.add(new PointF(255.0f, 255.0f));
            this.hdrModeEnable = false;
            this.hdrModeCurveEnable = false;
            this.hdrModeWindowMappingEnable = false;
            this.brightnessMappingPointsForHdrMode.clear();
            this.brightnessMappingPointsForHdrMode.add(new PointF(4.0f, 4.0f));
            this.brightnessMappingPointsForHdrMode.add(new PointF(225.0f, 225.0f));
            this.brightnessMappingPointsForHdrMode.add(new PointF(255.0f, 255.0f));
            this.brightnessLineForHdrMode.clear();
            this.brightnessLineForHdrMode.add(new PointF(0.0f, 20.0f));
            this.brightnessLineForHdrMode.add(new PointF(25.0f, 46.5f));
            this.brightnessLineForHdrMode.add(new PointF(250.0f, 100.0f));
            this.brightnessLineForHdrMode.add(new PointF(1995.0f, 154.7f));
            this.brightnessLineForHdrMode.add(new PointF(10000.0f, 225.0f));
            this.brightnessLineForHdrMode.add(new PointF(40000.0f, 255.0f));
            this.maxValidAmbientLux = 40000.0f;
        }
    }

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
            } catch (Throwable th) {
                if (0 == 0) {
                    new Data().loadDefaultConfig();
                }
                throw th;
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
        } else if (!HWFLOW) {
            return true;
        } else {
            Slog.i(TAG, "parseXml() load success!");
            return true;
        }
    }

    private void registerElement(HwXmlParser parser) {
        HwXmlElement rootElement = parser.registerRootElement(new Element_LABCConfig());
        rootElement.registerChildElement(new Element_MiscGroup());
        rootElement.registerChildElement(new ElementAmbientLuxGroup());
        rootElement.registerChildElement(new Element_MiscOptionalGroup1());
        rootElement.registerChildElement(new Element_MiscOptionalGroup2());
        rootElement.registerChildElement(new Element_ReadingModeEnable());
        rootElement.registerChildElement(new Element_ReadingAnimationTime());
        rootElement.registerChildElement(new Element_ResponseTimeGroup());
        rootElement.registerChildElement(new Element_ResponseTimeOptionalGroup());
        rootElement.registerChildElement(new Element_CoverModeGroup());
        rootElement.registerChildElement(new ElementCoverModeBrightnessLinePoints()).registerChildElement(new ElementCoverModeBrightnessLinePointsPoint());
        rootElement.registerChildElement(new Element_BackSensorCoverModeBrighnessLinePoints()).registerChildElement(new Element_BackSensorCoverModeBrighnessLinePoints_Point());
        rootElement.registerChildElement(new Element_PreWeightedGroup());
        rootElement.registerChildElement(new ElementLongTimeFilterGroup());
        rootElement.registerChildElement(new ElementHomeModeGroup());
        rootElement.registerChildElement(new Element_KeyguardResponseGroup());
        rootElement.registerChildElement(new Element_OutdoorResponseGroup());
        rootElement.registerChildElement(new Element_InitGroup());
        rootElement.registerChildElement(new Element_PowerAndThermalGroup());
        rootElement.registerChildElement(new ElementNightUpModeGroup());
        rootElement.registerChildElement(new Element_DayModeGroup());
        rootElement.registerChildElement(new Element_OffsetResetGroup());
        rootElement.registerChildElement(new Element_OffsetGroup());
        rootElement.registerChildElement(new ElementAutoMaxBrightnessGroup());
        rootElement.registerChildElement(new Element_DarkLightGroup());
        rootElement.registerChildElement(new Element_RebootGroup());
        rootElement.registerChildElement(new Element_SceneProcessing()).registerChildElement(new Element_SceneProcessingGroup());
        rootElement.registerChildElement(new Element_PreProcessing()).registerChildElement(new Element_PreProcessingGroup());
        rootElement.registerChildElement(new Element_BrightenlinePoints()).registerChildElement(new Element_BrightenlinePoints_Point());
        rootElement.registerChildElement(new Element_DarkenlinePoints()).registerChildElement(new Element_DarkenlinePoints_Point());
        rootElement.registerChildElement(new ElementDefaultBrightnessPoints()).registerChildElement(new ElementDefaultBrightnessPointsPoint());
        rootElement.registerChildElement(new ElementDayModeBrightnessPoints()).registerChildElement(new ElementDayModeBrightnessPointsPoint());
        rootElement.registerChildElement(new Element_AnimateGroup());
        rootElement.registerChildElement(new Element_AnimateOptionalGroup());
        rootElement.registerChildElement(new ElementAnimateManulSeekbarGroup());
        rootElement.registerChildElement(new ElementAnimateExtendedGroup());
        rootElement.registerChildElement(new ElementAnimateForLowPowerMappingGroup());
        rootElement.registerChildElement(new ElementAnimateForHighEqualDarkenGroup());
        rootElement.registerChildElement(new ElementAnimateForHbmGroup());
        rootElement.registerChildElement(new ElementAnimateFlickerGroup());
        rootElement.registerChildElement(new Element_VariableStep()).registerChildElement(new Element_VariableStepGroup());
        rootElement.registerChildElement(new Element_Proximity()).registerChildElement(new Element_ProximityGroup());
        rootElement.registerChildElement(new ElementDynamicProximityGroup());
        rootElement.registerChildElement(new Element_ManualGroup());
        rootElement.registerChildElement(new Element_ManualOptionalGroup());
        rootElement.registerChildElement(new Element_ManualBrightenLinePoints()).registerChildElement(new Element_ManualBrightenLinePoints_Point());
        rootElement.registerChildElement(new Element_ManualDarkenLinePoints()).registerChildElement(new Element_ManualDarkenLinePoints_Point());
        rootElement.registerChildElement(new Element_BrightnessMappingPoints()).registerChildElement(new Element_BrightnessMappingPoints_Point());
        rootElement.registerChildElement(new Element_AmbientLuxValidBrightnessPoints()).registerChildElement(new Element_AmbientLuxValidBrightnessPoints_Point());
        HwXmlElement darkAdapter = rootElement.registerChildElement(new Element_DarkAdapter());
        darkAdapter.registerChildElement(new Element_DarkAdapterGroup1());
        darkAdapter.registerChildElement(new Element_DarkAdapterGroup2());
        rootElement.registerChildElement(new Element_TouchProximity()).registerChildElement(new Element_TouchProximityGroup());
        rootElement.registerChildElement(new Element_VehicleModeGroup());
        rootElement.registerChildElement(new ElementWalkModeGroup());
        rootElement.registerChildElement(new Element_GameModeGroup());
        rootElement.registerChildElement(new ElementLandscapGameGroup());
        rootElement.registerChildElement(new ElementBrightenLinePointsForLandscapeGameMode()).registerChildElement(new ElementBrightenLinePointsForLandscapeGameModePoint());
        rootElement.registerChildElement(new ElementDarkenLinePointsForLandscapeGameMode()).registerChildElement(new ElementDarkenLinePointsForLandscapeGameModePoint());
        rootElement.registerChildElement(new Element_BrightenlinePointsForGameMode()).registerChildElement(new Element_BrightenlinePointsForGameMode_Point());
        rootElement.registerChildElement(new Element_DarkenlinePointsForGameMode()).registerChildElement(new Element_DarkenlinePointsForGameMode_Point());
        rootElement.registerChildElement(new ElementDcModeGroup());
        rootElement.registerChildElement(new ElementBrightenlinePointsForDcMode()).registerChildElement(new ElementBrightenlinePointsForDcModePoint());
        rootElement.registerChildElement(new ElementDarkenlinePointsForDcMode()).registerChildElement(new ElementDarkenlinePointsForDcModePoint());
        rootElement.registerChildElement(new Element_CryogenicGroup());
        rootElement.registerChildElement(new Element_GameModeAmbientLuxValidBrightnessPoints()).registerChildElement(new Element_GameModeAmbientLuxValidBrightnessPoints_Point());
        rootElement.registerChildElement(new ElementLandscapeModeGroup());
        rootElement.registerChildElement(new ElementBrightenlinePointsForLandscapeMode()).registerChildElement(new ElementBrightenlinePointsForLandscapeModePoint());
        rootElement.registerChildElement(new ElementDarkenlinePointsForLandscapeMode()).registerChildElement(new ElementDarkenlinePointsForLandscapeModePoint());
        rootElement.registerChildElement(new Element_BrightnessLevelToNitLinePoints()).registerChildElement(new Element_BrightnessLevelToNitLinePoints_Point());
        rootElement.registerChildElement(new Element_SensorGroup());
        rootElement.registerChildElement(new Element_BrightnessChangeGroup());
        rootElement.registerChildElement(new Element_BrightenlinePointsForBrightnessLevel()).registerChildElement(new Element_BrightenlinePointsForBrightnessLevel_Point());
        rootElement.registerChildElement(new Element_DarkenlinePointsForBrightnessLevel()).registerChildElement(new Element_DarkenlinePointsForBrightnessLevel_Point());
        rootElement.registerChildElement(new ElementVideoGroup());
        rootElement.registerChildElement(new ElementBrightnessLineForVideoFullScreenMode()).registerChildElement(new ElementBrightnessLineForVideoFullScreenModePoint());
        rootElement.registerChildElement(new Element_SecondDarkenModeGroup());
        rootElement.registerChildElement(new Element_BrightnessOffsetLuxGroup1());
        rootElement.registerChildElement(new Element_BrightnessOffsetLuxGroup2());
        rootElement.registerChildElement(new ElementTwoPoitOffsetGroup1());
        rootElement.registerChildElement(new ElementTwoPoitOffsetGroup2());
        rootElement.registerChildElement(new ElementTwoPoitOffsetGroup3());
        rootElement.registerChildElement(new Element_KeyguardUnlockedGroup());
        rootElement.registerChildElement(new ElementFoldScreenGroup());
        rootElement.registerChildElement(new ElementDarkModeGroup());
        rootElement.registerChildElement(new ElementBrightnessLineForDarkMode()).registerChildElement(new ElementBrightnessLineForDarkModePoint());
        rootElement.registerChildElement(new ElementBatteryModeGroup());
        rootElement.registerChildElement(new ElementLuxMinMaxBrightnessGroup());
        rootElement.registerChildElement(new ElementCameraLimitBrightnessModeGroup());
        rootElement.registerChildElement(new ElementLuxMinMaxBrightnessPoints()).registerChildElement(new ElementLuxMinMaxBrightnessPointsPoint());
        rootElement.registerChildElement(new ElementBrightnessMappingForWindowBrightnessGroup());
        rootElement.registerChildElement(new ElementBrightnessMappingPointsForWindowBrightness()).registerChildElement(new ElementBrightnessMappingPointsForWindowBrightnessPoint());
        rootElement.registerChildElement(new ElementHdrModeGroup());
        rootElement.registerChildElement(new ElementBrightnessMappingPointsForHdrMode()).registerChildElement(new ElementBrightnessMappingPointsForHdrModePoint());
        rootElement.registerChildElement(new ElementBrightnessLineForHdrMode()).registerChildElement(new ElementBrightnessLineForHdrModePoint());
    }

    private File getFactoryXmlFile() {
        ArrayList<String> xmlPathList = new ArrayList<>();
        updatePanelName();
        if (HwFoldScreenState.isInwardFoldDevice()) {
            if (sInwardPanelName != null) {
                xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s%s", XML_NAME_NOEXT, sInwardPanelName, "factory", XML_EXT));
            }
        } else if (sDefaultPanelName != null) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s%s", XML_NAME_NOEXT, sDefaultPanelName, "factory", XML_EXT));
        }
        xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s%s", XML_NAME_NOEXT, "factory", XML_EXT));
        File xmlFile = null;
        int listsize = xmlPathList.size();
        for (int i = 0; i < listsize; i++) {
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPathList.get(i), 2);
            if (xmlFile != null) {
                Slog.i(TAG, "get factory xmlFile :" + xmlPathList.get(i));
                return xmlFile;
            }
        }
        Slog.w(TAG, "get factory xmlFile :LABCConfig failed!");
        return xmlFile;
    }

    private void updatePanelName() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        if (binder == null) {
            Slog.w(TAG, "updatePanelName() binder is null!");
            return;
        }
        IDisplayEngineServiceEx displayEngineExService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (displayEngineExService == null) {
            Slog.w(TAG, "updatePanelName() displayEngineExService is null!");
            return;
        }
        Bundle data = new Bundle();
        try {
            int ret = displayEngineExService.getEffectEx(14, 13, data);
            if (ret != 0) {
                Slog.w(TAG, "updatePanelName() getEffect failed! ret=" + ret);
                return;
            }
            updatePanelNameFromPanelInfos(data);
        } catch (RemoteException e) {
            Slog.w(TAG, "updatePanelName() RemoteException ");
        }
    }

    private void updatePanelNameFromPanelInfos(Bundle data) {
        if (data == null) {
            Slog.w(TAG, "updatePanelNameFromPanelInfos() failed! data=null");
        } else if (HwFoldScreenState.isInwardFoldDevice()) {
            sInwardPanelName = data.getString("FullpanelName");
            sOutwardPanelName = data.getString("MainpanelName");
            sInwardPanelVersion = data.getString("FulllcdPanelVersion");
            sOutwardPanelVersion = data.getString("MainlcdPanelVersion");
            sInwardPanelName = parsePanelName(sInwardPanelName);
            sOutwardPanelName = parsePanelName(sOutwardPanelName);
            sInwardPanelVersion = parsePanelVersion(sInwardPanelVersion);
            sOutwardPanelVersion = parsePanelVersion(sOutwardPanelVersion);
            if (sOutwardPanelVersion != null) {
                sOutwardPanelVersion = "O" + sOutwardPanelVersion;
            }
            if (HWDEBUG) {
                Slog.i(TAG, "sInwardPanelName=" + sInwardPanelName + ",sOutwardPanelName=" + sOutwardPanelName + ",sInwardPanelVersion=" + sInwardPanelVersion + ",sOutwardPanelVersion=" + sOutwardPanelVersion);
            }
            Slog.i(TAG, "sInwardPanelVersion=" + sInwardPanelVersion + ",sOutwardPanelVersion=" + sOutwardPanelVersion);
        } else {
            sDefaultPanelName = data.getString("panelName");
            sDefaultPanelVersion = data.getString("lcdPanelVersion");
            sDefaultPanelName = parsePanelName(sDefaultPanelName);
            sDefaultPanelVersion = parsePanelVersion(sDefaultPanelVersion);
            if (HWDEBUG) {
                Slog.i(TAG, "sDefaultPanelName=" + sDefaultPanelName + ",sDefaultPanelVersion=" + sDefaultPanelVersion);
            }
            Slog.i(TAG, "sDefaultPanelVersion=" + sDefaultPanelVersion);
        }
    }

    private String parsePanelName(String name) {
        if (name == null) {
            return null;
        }
        return name.trim().replace(' ', '_');
    }

    private String parsePanelVersion(String name) {
        String lcdVersion;
        int index;
        if (name == null || (index = (lcdVersion = name.trim()).indexOf("VER:")) == -1) {
            return null;
        }
        return lcdVersion.substring("VER:".length() + index);
    }

    /* access modifiers changed from: package-private */
    public List<String> getXmlPathList() {
        List<String> xmlPathList = new ArrayList<>();
        if (!HwFoldScreenState.isInwardFoldDevice()) {
            return getDefaultXmlPathList();
        }
        if (!(sInwardPanelName == null || sInwardPanelVersion == null || sOutwardPanelVersion == null)) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, sInwardPanelName, sInwardPanelVersion, sOutwardPanelVersion));
        }
        if (!(sInwardPanelName == null || sInwardPanelVersion == null)) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, sInwardPanelName, sInwardPanelVersion));
        }
        if (!(sInwardPanelName == null || sOutwardPanelVersion == null)) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, sInwardPanelName, sOutwardPanelVersion));
        }
        if (sInwardPanelName != null) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s", XML_NAME_NOEXT, sInwardPanelName));
        }
        xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s", XML_NAME_NOEXT));
        return xmlPathList;
    }

    /* access modifiers changed from: package-private */
    public List<String> getDefaultXmlPathList() {
        List<String> xmlPathList = new ArrayList<>();
        String lcdVersion = getVersionFromTouchOemInfo();
        String screenColor = SystemProperties.get("ro.config.devicecolor");
        String lcdIcName = getLcdIcName();
        Slog.i(TAG, "screenColor=" + screenColor);
        Slog.i(TAG, "lcdIcName=" + lcdIcName);
        if (!(sDefaultPanelName == null || sDefaultPanelVersion == null || lcdIcName == null)) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, sDefaultPanelName, sDefaultPanelVersion, lcdIcName, screenColor));
        }
        if (!(sDefaultPanelName == null || lcdVersion == null)) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, sDefaultPanelName, lcdVersion, screenColor));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, sDefaultPanelName, lcdVersion));
        }
        if (!(sDefaultPanelName == null || sDefaultPanelVersion == null)) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, sDefaultPanelName, sDefaultPanelVersion, screenColor));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, sDefaultPanelName, sDefaultPanelVersion));
        }
        if (!(sDefaultPanelName == null || lcdIcName == null)) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s_%s", XML_NAME_NOEXT, sDefaultPanelName, lcdIcName, screenColor));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, sDefaultPanelName, lcdIcName));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s", XML_NAME_NOEXT, sDefaultPanelName));
        }
        if (sDefaultPanelName != null) {
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s_%s", XML_NAME_NOEXT, sDefaultPanelName, screenColor));
            xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s", XML_NAME_NOEXT, sDefaultPanelName));
        }
        xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s_%s", XML_NAME_NOEXT, screenColor));
        xmlPathList.add(String.format(Locale.ENGLISH, "/xml/lcd/%s", XML_NAME_NOEXT));
        return xmlPathList;
    }

    private String getVersionFromTouchOemInfo() {
        int productMonth;
        int productDay;
        String version = null;
        try {
            File file = new File(String.format("%s", TOUCH_OEM_INFO_PATH));
            if (file.exists()) {
                String touch_oem_info = FileUtils.readTextFile(file, 0, null).trim();
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
        return version;
    }

    private String getLcdIcName() {
        IBinder binder = ServiceManager.getService("DisplayEngineExService");
        if (binder == null) {
            Slog.e(TAG, "getLcdIcName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdIcName() mService is null!");
            return null;
        }
        byte[] name = new byte[128];
        try {
            int ret = mService.getEffect(14, 5, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdIcName() getEffect failed! ret=" + ret);
                return null;
            }
            String[] lcdNameAllArray = new String(name, StandardCharsets.UTF_8).trim().split("#");
            if (lcdNameAllArray.length < 3) {
                return null;
            }
            int index = lcdNameAllArray[2].indexOf("ICTYPE:");
            Slog.i(TAG, "getLcdIcName() index=" + index + ",LcdIcNameALL=" + lcdNameAllArray[2]);
            if (index != -1) {
                return lcdNameAllArray[2].substring("ICTYPE:".length() + index);
            }
            return null;
        } catch (RemoteException e) {
            Slog.e(TAG, "getLcdIcName() RemoteException " + e);
            return null;
        }
    }

    private File getNormalXmlFile() {
        new ArrayList();
        updatePanelName();
        List<String> xmlPathList = getXmlPathList();
        File xmlFile = null;
        if (xmlPathList == null) {
            Slog.e(TAG, "get xmlPathList failed!");
            return null;
        }
        int xmlPathListSize = xmlPathList.size();
        for (int i = 0; i < xmlPathListSize; i++) {
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPathList.get(i) + XML_EXT, 2);
            if (xmlFile != null) {
                File xmlFileForABTest = getABTestXmlFile(xmlPathList.get(i));
                return xmlFileForABTest != null ? xmlFileForABTest : xmlFile;
            }
        }
        Slog.e(TAG, "get failed!");
        return xmlFile;
    }

    private File getABTestXmlFile(String xmlPath) {
        if (SystemProperties.getInt("ro.logsystem.usertype", 0) != 3) {
            return null;
        }
        try {
            String serial = Build.getSerial();
            if (serial == null || serial.isEmpty() || serial.equals(ModelBaseService.UNKONW_IDENTIFY_RET)) {
                Slog.i(TAG, "getABTestXmlFile, get number failed, skip AB test.");
                return null;
            }
            try {
                if ((serial.charAt(serial.length() - 1) & 1) == 1) {
                    Slog.i(TAG, "getABTestXmlFile, using orginal xml.");
                    return null;
                }
                File xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath + "_Test" + XML_EXT, 2);
                if (xmlFile == null) {
                    Slog.i(TAG, "getABTestXmlFile, using orginal xml.");
                } else {
                    Slog.i(TAG, "getABTestXmlFile, using B test xml!");
                }
                return xmlFile;
            } catch (IndexOutOfBoundsException e) {
                Slog.i(TAG, "getABTestXmlFile, IndexOutOfBoundsException, skip AB test: " + e);
                return null;
            }
        } catch (SecurityException e2) {
            Slog.w(TAG, "SecurityException in getABTestXmlFile, " + e2);
            return null;
        }
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
        String xmlCanonicalPath = null;
        try {
            xmlCanonicalPath = xmlFile.getCanonicalPath();
        } catch (IOException e) {
            Slog.e(TAG, "get xmlCanonicalPath error IOException!");
        }
        if (HWDEBUG) {
            Slog.i(TAG, "get xmlCanonicalPath=" + xmlCanonicalPath);
        }
        return xmlCanonicalPath;
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
    public static boolean isMaxAmbientLuxPointValid(List<PointF> brightnessList, String listName) {
        if (brightnessList == null) {
            Slog.e(TAG, "isMaxAmbientLuxPointValid() error! list is null, listName=" + listName);
            return false;
        }
        int listSize = brightnessList.size();
        if (listSize < 3 || listSize >= 100) {
            Slog.e(TAG, "isMaxAmbientLuxPointValid() error! list size=" + listSize + " is out of range,listName=" + listName);
            return false;
        }
        PointF curPoit = brightnessList.get(listSize - 1);
        if (curPoit.x >= mData.maxValidAmbientLux) {
            return true;
        }
        Slog.w(TAG, "brightnessList need maxAmbientLux point,maxValidAmbientLux=" + mData.maxValidAmbientLux + ",listName=" + listName + ",curPoitLux=" + curPoit.x + ",curPoitValue=" + curPoit.y);
        return true;
    }

    /* access modifiers changed from: private */
    public static boolean isMaxAmbientLuxAmPointValid(List<HwXmlAmPoint> brightnessAmList, String listName) {
        if (brightnessAmList == null) {
            Slog.e(TAG, "isMaxAmbientLuxAmPointValid() error! list is null, listName=" + listName);
            return false;
        }
        int listSize = brightnessAmList.size();
        if (listSize < 3 || listSize >= 100) {
            Slog.e(TAG, "isMaxAmbientLuxAmPointValid() error! list size=" + listSize + " is out of range,listName=" + listName);
            return false;
        }
        HwXmlAmPoint curPoit = brightnessAmList.get(listSize - 1);
        if (curPoit.x >= mData.maxValidAmbientLux) {
            return true;
        }
        Slog.w(TAG, "brightnessAmList need maxAmbientLux point,maxValidAmbientLux=" + mData.maxValidAmbientLux + ",listName=" + listName + ",curPoitLux=" + curPoit.x + ",curPoitValueY=" + curPoit.y + ",curPoitValueZ=" + curPoit.z);
        return true;
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

    /* access modifiers changed from: private */
    public class Element_LABCConfig extends HwXmlElement {
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
            String deviceLevelString = parser.getAttributeValue(null, "level");
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

    /* access modifiers changed from: private */
    public static class Element_MiscGroup extends HwXmlElement {
        private Element_MiscGroup() {
        }

        public String getName() {
            return "MiscGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("lightSensorRateMills", "DefaultBrightness");
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002c  */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x0055  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != 604635474) {
                if (hashCode == 803831879 && valueName.equals("lightSensorRateMills")) {
                    c = 0;
                    if (c == 0) {
                        HwBrightnessXmlLoader.mData.lightSensorRateMills = string2Int(parser.nextText());
                    } else if (c != 1) {
                        Slog.e(this.TAG, "unknow valueName=" + valueName);
                        return false;
                    } else {
                        HwBrightnessXmlLoader.mData.defaultBrightness = string2Float(parser.nextText());
                    }
                    return true;
                }
            } else if (valueName.equals("DefaultBrightness")) {
                c = 1;
                if (c == 0) {
                }
                return true;
            }
            c = 65535;
            if (c == 0) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.defaultBrightness > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_MiscOptionalGroup1 extends HwXmlElement {
        private Element_MiscOptionalGroup1() {
        }

        public String getName() {
            return "MiscOptionalGroup1";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("RebootAutoModeEnable", "PostMaxMinAvgFilterNoFilterNum", "PostMaxMinAvgFilterNum", "PowerOnFastResponseLuxNum", "PowerOnBrightenDebounceTime", "PowerOnDarkenDebounceTime", "CameraModeEnable", "CameraAnimationTime", "ProximityLuxThreshold", "ProximityResponseBrightenTime", "AnimatingForRGBWEnable", "MonitorEnable", "BrightnessCalibrationEnabled");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2080828579:
                    if (valueName.equals("MonitorEnable")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case -1912197839:
                    if (valueName.equals("PostMaxMinAvgFilterNoFilterNum")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1540132752:
                    if (valueName.equals("ProximityResponseBrightenTime")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -1498785192:
                    if (valueName.equals("AnimatingForRGBWEnable")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case -1271211816:
                    if (valueName.equals("PowerOnFastResponseLuxNum")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -457425365:
                    if (valueName.equals("CameraModeEnable")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -416433876:
                    if (valueName.equals("CameraAnimationTime")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -70231992:
                    if (valueName.equals("BrightnessCalibrationEnabled")) {
                        c = '\f';
                        break;
                    }
                    c = 65535;
                    break;
                case 782050106:
                    if (valueName.equals("RebootAutoModeEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1554640189:
                    if (valueName.equals("PowerOnBrightenDebounceTime")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1784147739:
                    if (valueName.equals("ProximityLuxThreshold")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1960905866:
                    if (valueName.equals("PostMaxMinAvgFilterNum")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1981859257:
                    if (valueName.equals("PowerOnDarkenDebounceTime")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
                    HwBrightnessXmlLoader.mData.proximityLuxThreshold = string2Float(parser.nextText());
                    break;
                case '\t':
                    HwBrightnessXmlLoader.mData.proximityResponseBrightenTime = string2Int(parser.nextText());
                    break;
                case '\n':
                    HwBrightnessXmlLoader.mData.animatingForRGBWEnable = string2Boolean(parser.nextText());
                    break;
                case 11:
                    HwBrightnessXmlLoader.mData.monitorEnable = string2Boolean(parser.nextText());
                    break;
                case '\f':
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
            return HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNum > 0 && HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNum <= HwBrightnessXmlLoader.mData.postMaxMinAvgFilterNoFilterNum && HwBrightnessXmlLoader.mData.powerOnFastResponseLuxNum > 0 && HwBrightnessXmlLoader.mData.powerOnBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.powerOnDarkenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.cameraAnimationTime > 0.0f && ((double) HwBrightnessXmlLoader.mData.proximityLuxThreshold) > 1.0E-6d;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_MiscOptionalGroup2 extends HwXmlElement {
        private Element_MiscOptionalGroup2() {
        }

        public String getName() {
            return "MiscOptionalGroup2";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("ReportValueWhenSensorOnChange", "PgReregisterScene", "UpdateBrightnessViewAlphaEnable", "DarkenTargetForKeyguard", "MinAnimatingStepForKeyguard", "BrightnessLevelToNitMappingEnable", "PowerOnFastResponseTime", "GameDisableAutoBrightnessModeEnable");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2012946113:
                    if (valueName.equals("PgReregisterScene")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1246287070:
                    if (valueName.equals("UpdateBrightnessViewAlphaEnable")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1089469156:
                    if (valueName.equals("GameDisableAutoBrightnessModeEnable")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -357937089:
                    if (valueName.equals("DarkenTargetForKeyguard")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -250579401:
                    if (valueName.equals("MinAnimatingStepForKeyguard")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 582429158:
                    if (valueName.equals("BrightnessLevelToNitMappingEnable")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1259478400:
                    if (valueName.equals("ReportValueWhenSensorOnChange")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1290522350:
                    if (valueName.equals("PowerOnFastResponseTime")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.reportValueWhenSensorOnChange = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.pgReregisterScene = string2Boolean(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.updateBrightnessViewAlphaEnable = string2Boolean(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.darkenTargetForKeyguard = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.minAnimatingStepForKeyguard = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.brightnessLevelToNitMappingEnable = string2Boolean(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.powerOnFastResponseTime = string2Long(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.gameDisableAutoBrightnessModeEnable = string2Boolean(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_ReadingModeEnable extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_ReadingAnimationTime extends HwXmlElement {
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
            return HwBrightnessXmlLoader.mData.readingAnimationTime > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_ResponseTimeGroup extends HwXmlElement {
        private Element_ResponseTimeGroup() {
        }

        public String getName() {
            return "ResponseTimeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("BrighenDebounceTime", "DarkenDebounceTime", "BrightenDebounceTimeParaBig", "DarkenDebounceTimeParaBig", "BrightenDeltaLuxPara", "DarkenDeltaLuxPara", "StabilityConstant", "StabilityTime1", "StabilityTime2");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1403734451:
                    if (valueName.equals("StabilityConstant")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -599365355:
                    if (valueName.equals("DarkenDebounceTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -85825767:
                    if (valueName.equals("BrighenDebounceTime")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -37334485:
                    if (valueName.equals("DarkenDebounceTimeParaBig")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1002334874:
                    if (valueName.equals("BrightenDeltaLuxPara")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1209051670:
                    if (valueName.equals("DarkenDeltaLuxPara")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1578851579:
                    if (valueName.equals("StabilityTime1")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 1578851580:
                    if (valueName.equals("StabilityTime2")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1866183975:
                    if (valueName.equals("BrightenDebounceTimeParaBig")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
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

    /* access modifiers changed from: private */
    public static class Element_ResponseTimeOptionalGroup extends HwXmlElement {
        private Element_ResponseTimeOptionalGroup() {
        }

        public String getName() {
            return "ResponseTimeOptionalGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("BrighenDebounceTimeForSmallThr", "DarkenDebounceTimeForSmallThr", "RatioForBrightnenSmallThr", "RatioForDarkenSmallThr", "DarkTimeDelayEnable", "DarkTimeDelay", "DarkTimeDelayLuxThreshold", "DarkTimeDelayBeta0", "DarkTimeDelayBeta1", "DarkTimeDelayBeta2", "DarkTimeDelayBrightness");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2041716473:
                    if (valueName.equals("BrighenDebounceTimeForSmallThr")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1203123972:
                    if (valueName.equals("DarkTimeDelayLuxThreshold")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -785220512:
                    if (valueName.equals("DarkTimeDelayBeta0")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -785220511:
                    if (valueName.equals("DarkTimeDelayBeta1")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case -785220510:
                    if (valueName.equals("DarkTimeDelayBeta2")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -487716469:
                    if (valueName.equals("DarkenDebounceTimeForSmallThr")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 254012465:
                    if (valueName.equals("DarkTimeDelayBrightness")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 633705408:
                    if (valueName.equals("DarkTimeDelay")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1174414900:
                    if (valueName.equals("RatioForDarkenSmallThr")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1521603939:
                    if (valueName.equals("DarkTimeDelayEnable")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1843038262:
                    if (valueName.equals("RatioForBrightnenSmallThr")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
                    HwBrightnessXmlLoader.mData.darkTimeDelayBeta1 = string2Float(parser.nextText());
                    break;
                case '\t':
                    HwBrightnessXmlLoader.mData.darkTimeDelayBeta2 = string2Float(parser.nextText());
                    break;
                case '\n':
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
            return HwBrightnessXmlLoader.mData.brighenDebounceTimeForSmallThr > 0 && HwBrightnessXmlLoader.mData.darkenDebounceTimeForSmallThr > 0 && HwBrightnessXmlLoader.mData.ratioForBrightnenSmallThr > 0.0f && HwBrightnessXmlLoader.mData.ratioForDarkenSmallThr > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_CoverModeGroup extends HwXmlElement {
        private Element_CoverModeGroup() {
        }

        public String getName() {
            return "CoverModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("CoverModeFirstLux", "LastCloseScreenEnable", "CoverModeBrightenResponseTime", "CoverModeDarkenResponseTime", "CoverModelastCloseScreenEnable", "CoverModeDayEnable", "CoverModeDayBrightness", "ConverModeDayBeginTime", "CoverModeDayEndTime", "BackSensorCoverModeMinLuxInRing");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2135500393:
                    if (valueName.equals("CoverModelastCloseScreenEnable")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -2016036730:
                    if (valueName.equals("CoverModeDayEndTime")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case -1487844013:
                    if (valueName.equals("CoverModeDayBrightness")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -1073894082:
                    if (valueName.equals("ConverModeDayBeginTime")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -163823189:
                    if (valueName.equals("BackSensorCoverModeMinLuxInRing")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 1181816709:
                    if (valueName.equals("CoverModeDayEnable")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1364232977:
                    if (valueName.equals("LastCloseScreenEnable")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1460433721:
                    if (valueName.equals("CoverModeFirstLux")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1871618155:
                    if (valueName.equals("CoverModeBrightenResponseTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 2140032615:
                    if (valueName.equals("CoverModeDarkenResponseTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
                    HwBrightnessXmlLoader.mData.coverModeDayEndTime = string2Int(parser.nextText());
                    break;
                case '\t':
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

    /* access modifiers changed from: private */
    public static class Element_BackSensorCoverModeBrighnessLinePoints extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_BackSensorCoverModeBrighnessLinePoints_Point extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.backSensorCoverModeBrighnessLinePoints, "backSensorCoverModeBrighnessLinePoints")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.backSensorCoverModeBrighnessLinePoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementCoverModeBrightnessLinePoints extends HwXmlElement {
        private ElementCoverModeBrightnessLinePoints() {
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

    /* access modifiers changed from: private */
    public static class ElementCoverModeBrightnessLinePointsPoint extends HwXmlElement {
        private ElementCoverModeBrightnessLinePointsPoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.coverModeBrightnessLinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.coverModeBrightnessLinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.coverModeBrightnessLinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.coverModeBrightnessLinePoints, "coverModeBrightnessLinePoints")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.coverModeBrightnessLinePoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_PreProcessing extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_PreProcessingGroup extends HwXmlElement {
        private Element_PreProcessingGroup() {
        }

        public String getName() {
            return "PreProcessingGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("BrightTimeDelayEnable", "BrightTimeDelay", "BrightTimeDelayLuxThreshold", "PreMethodNum", "PreMeanFilterNoFilterNum", "PreMeanFilterNum", "PostMethodNum", "PostMeanFilterNoFilterNum", "PostMeanFilterNum");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1216473595:
                    if (valueName.equals("PostMethodNum")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -591649441:
                    if (valueName.equals("BrightTimeDelayEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 157937894:
                    if (valueName.equals("PreMeanFilterNum")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 620980681:
                    if (valueName.equals("PostMeanFilterNum")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 700394488:
                    if (valueName.equals("BrightTimeDelayLuxThreshold")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1373278396:
                    if (valueName.equals("BrightTimeDelay")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1431493488:
                    if (valueName.equals("PostMeanFilterNoFilterNum")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 1443034509:
                    if (valueName.equals("PreMeanFilterNoFilterNum")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1524020130:
                    if (valueName.equals("PreMethodNum")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
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

    /* access modifiers changed from: private */
    public static class Element_PreWeightedGroup extends HwXmlElement {
        private Element_PreWeightedGroup() {
        }

        public String getName() {
            return "PreWeightedGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("PreWeightedMeanFilterNoFilterNum", "PreWeightedMeanFilterMaxFuncLuxNum", "PreWeightedMeanFilterNum", "PreWeightedMeanFilterAlpha", "PreWeightedMeanFilterLuxTh");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1627681866:
                    if (valueName.equals("PreWeightedMeanFilterNoFilterNum")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 246136847:
                    if (valueName.equals("PreWeightedMeanFilterNum")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 302040999:
                    if (valueName.equals("PreWeightedMeanFilterAlpha")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 312474924:
                    if (valueName.equals("PreWeightedMeanFilterLuxTh")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1095802536:
                    if (valueName.equals("PreWeightedMeanFilterMaxFuncLuxNum")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.preWeightedMeanFilterNoFilterNum = string2Int(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.preWeightedMeanFilterNum = string2Int(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.preWeightedMeanFilterAlpha = string2Float(parser.nextText());
            } else if (c != 4) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.preWeightedMeanFilterLuxTh = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.preWeightedMeanFilterNoFilterNum > 0 && HwBrightnessXmlLoader.mData.preWeightedMeanFilterMaxFuncLuxNum > 0 && HwBrightnessXmlLoader.mData.preWeightedMeanFilterNum > 0 && HwBrightnessXmlLoader.mData.preWeightedMeanFilterNum <= HwBrightnessXmlLoader.mData.preWeightedMeanFilterNoFilterNum;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementLongTimeFilterGroup extends HwXmlElement {
        private ElementLongTimeFilterGroup() {
        }

        public String getName() {
            return "LongTimeFilterGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("LongTimeFilterEnable", "LongTimeNoFilterNum", "LongTimeFilterNum", "LongTimeFilterLuxTh", "LuxRoundEnable", "AmbientLuxMinRound", "LuxThNoResponseForSmallThr");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1767953933:
                    if (valueName.equals("AmbientLuxMinRound")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1733457150:
                    if (valueName.equals("LongTimeFilterLuxTh")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1320236187:
                    if (valueName.equals("LongTimeFilterNum")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -737550014:
                    if (valueName.equals("LuxRoundEnable")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -137755420:
                    if (valueName.equals("LongTimeNoFilterNum")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1877238875:
                    if (valueName.equals("LuxThNoResponseForSmallThr")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1889862980:
                    if (valueName.equals("LongTimeFilterEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.longTimeFilterEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.longTimeNoFilterNum = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.longTimeFilterNum = string2Int(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.longTimeFilterLuxTh = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.luxRoundEnable = string2Boolean(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.ambientLuxMinRound = string2Float(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.luxThNoResponseForSmallThr = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.longTimeNoFilterNum > 0 && HwBrightnessXmlLoader.mData.longTimeFilterNum > 0 && HwBrightnessXmlLoader.mData.longTimeFilterLuxTh > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementHomeModeGroup extends HwXmlElement {
        private ElementHomeModeGroup() {
        }

        public String getName() {
            return "HomeModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("HomeModeEnable", "HomeModeDayBeginTime", "HomeModeDayEndTime", "HomeModeSwitchTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1466525851:
                    if (valueName.equals("HomeModeEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 164243438:
                    if (valueName.equals("HomeModeDayEndTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 518762300:
                    if (valueName.equals("HomeModeDayBeginTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1033794819:
                    if (valueName.equals("HomeModeSwitchTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.homeModeEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.homeModeDayBeginTime = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.homeModeDayEndTime = string2Int(parser.nextText());
            } else if (c != 3) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.homeModeSwitchTime = string2Int(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.homeModeDayBeginTime >= 0 && HwBrightnessXmlLoader.mData.homeModeDayBeginTime < 24 && HwBrightnessXmlLoader.mData.homeModeDayEndTime >= 0 && HwBrightnessXmlLoader.mData.homeModeDayEndTime < 24 && HwBrightnessXmlLoader.mData.homeModeSwitchTime >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_KeyguardResponseGroup extends HwXmlElement {
        private Element_KeyguardResponseGroup() {
        }

        public String getName() {
            return "KeyguardResponseGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("KeyguardResponseBrightenTime", "KeyguardResponseDarkenTime", "KeyguardAnimationBrightenTime", "KeyguardAnimationDarkenTime", "KeyguardLuxThreshold");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1788786797:
                    if (valueName.equals("KeyguardResponseDarkenTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -662444278:
                    if (valueName.equals("KeyguardAnimationDarkenTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 884106242:
                    if (valueName.equals("KeyguardLuxThreshold")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1083838039:
                    if (valueName.equals("KeyguardResponseBrightenTime")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1167240206:
                    if (valueName.equals("KeyguardAnimationBrightenTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.keyguardResponseBrightenTime = string2Int(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.keyguardResponseDarkenTime = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.keyguardAnimationBrightenTime = string2Float(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.keyguardAnimationDarkenTime = string2Float(parser.nextText());
            } else if (c != 4) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.keyguardLuxThreshold = string2Float(parser.nextText());
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_OutdoorResponseGroup extends HwXmlElement {
        private Element_OutdoorResponseGroup() {
        }

        public String getName() {
            return "OutdoorResponseGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("OutdoorLowerLuxThreshold", "OutdoorAnimationBrightenTime", "OutdoorAnimationDarkenTime", "OutdoorResponseBrightenRatio", "OutdoorResponseDarkenRatio", "OutdoorResponseBrightenTime", "OutdoorResponseDarkenTime", "OutdoorResponseCount");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1820473548:
                    if (valueName.equals("OutdoorAnimationDarkenTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1232436373:
                    if (valueName.equals("OutdoorResponseBrightenRatio")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -856311255:
                    if (valueName.equals("OutdoorResponseDarkenTime")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -777923537:
                    if (valueName.equals("OutdoorResponseDarkenRatio")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -455330963:
                    if (valueName.equals("OutdoorResponseBrightenTime")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -110162222:
                    if (valueName.equals("OutdoorResponseCount")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 697641400:
                    if (valueName.equals("OutdoorAnimationBrightenTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 2114610753:
                    if (valueName.equals("OutdoorLowerLuxThreshold")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
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

    /* access modifiers changed from: private */
    public static class Element_InitGroup extends HwXmlElement {
        private Element_InitGroup() {
        }

        public String getName() {
            return "InitGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("InitDoubleSensorInterfere", "InitNumLastBuffer", "InitValidCloseTime", "InitUpperLuxThreshold", "InitSigmoidFuncSlope", "InitSlowReponseUpperLuxThreshold", "InitSlowReponseBrightTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2087819444:
                    if (valueName.equals("InitNumLastBuffer")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -2027950061:
                    if (valueName.equals("InitDoubleSensorInterfere")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1707591794:
                    if (valueName.equals("InitUpperLuxThreshold")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 416004804:
                    if (valueName.equals("InitSlowReponseBrightTime")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 672175713:
                    if (valueName.equals("InitSlowReponseUpperLuxThreshold")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1178571689:
                    if (valueName.equals("InitSigmoidFuncSlope")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1260225945:
                    if (valueName.equals("InitValidCloseTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
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
            return HwBrightnessXmlLoader.mData.initDoubleSensorInterfere > 0.0f && HwBrightnessXmlLoader.mData.initUpperLuxThreshold > 0 && HwBrightnessXmlLoader.mData.initSigmoidFuncSlope > 0.0f && HwBrightnessXmlLoader.mData.initSlowReponseUpperLuxThreshold > 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_PowerAndThermalGroup extends HwXmlElement {
        private Element_PowerAndThermalGroup() {
        }

        public String getName() {
            return "PowerAndThermalGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("ManualPowerSavingBrighnessLineEnable", "ManualPowerSavingAnimationBrightenTime", "ManualPowerSavingAnimationDarkenTime", "ManualThermalModeAnimationBrightenTime", "ManualThermalModeAnimationDarkenTime", "ThermalModeBrightnessMappingEnable", "PgModeBrightnessMappingEnable", "AutoPowerSavingUseManualAnimationTimeEnable", "PgSceneDetectionDarkenDelayTime", "PgSceneDetectionBrightenDelayTime", "ManualPowerSavingBrighnessLineDisableForDemo", "AutoPowerSavingBrighnessLineDisableForDemo");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1332786641:
                    if (valueName.equals("ManualPowerSavingBrighnessLineEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1199462784:
                    if (valueName.equals("ManualThermalModeAnimationBrightenTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -785273573:
                    if (valueName.equals("ManualPowerSavingAnimationBrightenTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -687994128:
                    if (valueName.equals("ManualPowerSavingBrighnessLineDisableForDemo")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case -578235879:
                    if (valueName.equals("AutoPowerSavingBrighnessLineDisableForDemo")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case 397849367:
                    if (valueName.equals("AutoPowerSavingUseManualAnimationTimeEnable")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 1064002566:
                    if (valueName.equals("ThermalModeBrightnessMappingEnable")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1131515809:
                    if (valueName.equals("PgSceneDetectionDarkenDelayTime")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1254090086:
                    if (valueName.equals("PgModeBrightnessMappingEnable")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1265817084:
                    if (valueName.equals("ManualThermalModeAnimationDarkenTime")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1914292055:
                    if (valueName.equals("ManualPowerSavingAnimationDarkenTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1952764317:
                    if (valueName.equals("PgSceneDetectionBrightenDelayTime")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
                    HwBrightnessXmlLoader.mData.pgSceneDetectionDarkenDelayTime = string2Int(parser.nextText());
                    break;
                case '\t':
                    HwBrightnessXmlLoader.mData.pgSceneDetectionBrightenDelayTime = string2Int(parser.nextText());
                    break;
                case '\n':
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
            return HwBrightnessXmlLoader.mData.manualPowerSavingAnimationBrightenTime > 0.0f && HwBrightnessXmlLoader.mData.manualPowerSavingAnimationDarkenTime > 0.0f && HwBrightnessXmlLoader.mData.manualThermalModeAnimationBrightenTime > 0.0f && HwBrightnessXmlLoader.mData.manualThermalModeAnimationDarkenTime > 0.0f && HwBrightnessXmlLoader.mData.pgSceneDetectionDarkenDelayTime >= 0 && HwBrightnessXmlLoader.mData.pgSceneDetectionBrightenDelayTime >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementNightUpModeGroup extends HwXmlElement {
        private ElementNightUpModeGroup() {
        }

        public String getName() {
            return "NightUpModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("NightUpModeEnable", "NightUpModeSwitchTimeMillis", "NightUpModeBeginHourTime", "NightUpModeEndHourTime", "NightUpModeLuxThreshold", "NightUpModePowOnDimTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2051446476:
                    if (valueName.equals("NightUpModePowOnDimTime")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1799577098:
                    if (valueName.equals("NightUpModeEndHourTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1178795299:
                    if (valueName.equals("NightUpModeSwitchTimeMillis")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -913272526:
                    if (valueName.equals("NightUpModeLuxThreshold")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1186021700:
                    if (valueName.equals("NightUpModeBeginHourTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 2045279769:
                    if (valueName.equals("NightUpModeEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.nightUpModeEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.nightUpModeSwitchTimeMillis = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.nightUpModeBeginHourTime = string2Int(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.nightUpModeEndHourTime = string2Int(parser.nextText());
            } else if (c == 4) {
                HwBrightnessXmlLoader.mData.nightUpModeLuxThreshold = string2Float(parser.nextText());
            } else if (c != 5) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.nightUpModePowOnDimTime = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (HwBrightnessXmlLoader.mData.nightUpModeSwitchTimeMillis >= 0 && HwBrightnessXmlLoader.mData.nightUpModeBeginHourTime >= 0) {
                int i = HwBrightnessXmlLoader.mData.nightUpModeBeginHourTime;
                Data unused = HwBrightnessXmlLoader.mData;
                if (i < 24 && HwBrightnessXmlLoader.mData.nightUpModeEndHourTime >= 0) {
                    int i2 = HwBrightnessXmlLoader.mData.nightUpModeEndHourTime;
                    Data unused2 = HwBrightnessXmlLoader.mData;
                    if (i2 < 24 && HwBrightnessXmlLoader.mData.nightUpModePowOnDimTime > 0.0f) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_DayModeGroup extends HwXmlElement {
        private Element_DayModeGroup() {
        }

        public String getName() {
            return "DayModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("DayModeAlgoEnable", "DayModeSwitchTime", "DayModeBeginTime", "DayModeEndTime", "DayModeModifyNumPoint", "DayModeModifyMinBrightness", "DayModeDarkenMinLux");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1913291113:
                    if (valueName.equals("DayModeBeginTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1608117751:
                    if (valueName.equals("DayModeEndTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -818760726:
                    if (valueName.equals("DayModeModifyMinBrightness")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -680246085:
                    if (valueName.equals("DayModeDarkenMinLux")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -631473984:
                    if (valueName.equals("DayModeSwitchTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1126774357:
                    if (valueName.equals("DayModeAlgoEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1716702627:
                    if (valueName.equals("DayModeModifyNumPoint")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
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
            return HwBrightnessXmlLoader.mData.dayModeSwitchTime > 0 && HwBrightnessXmlLoader.mData.dayModeBeginTime >= 0 && HwBrightnessXmlLoader.mData.dayModeBeginTime < 24 && HwBrightnessXmlLoader.mData.dayModeEndTime >= 0 && HwBrightnessXmlLoader.mData.dayModeEndTime < 24 && HwBrightnessXmlLoader.mData.dayModeModifyNumPoint > 0 && HwBrightnessXmlLoader.mData.dayModeModifyMinBrightness >= 4 && HwBrightnessXmlLoader.mData.dayModeDarkenMinLux >= 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_OffsetResetGroup extends HwXmlElement {
        private Element_OffsetResetGroup() {
        }

        public String getName() {
            return "OffsetResetGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("OffsetResetSwitchTime", "OffsetResetSwitchTimeForDarkMode", "OffsetResetEnable", "OffsetResetShortSwitchTime", "OffsetResetShortLuxDelta");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1142640385:
                    if (valueName.equals("OffsetResetEnable")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -454240631:
                    if (valueName.equals("OffsetResetShortLuxDelta")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 82139585:
                    if (valueName.equals("OffsetResetShortSwitchTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 656362565:
                    if (valueName.equals("OffsetResetSwitchTimeForDarkMode")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1655845277:
                    if (valueName.equals("OffsetResetSwitchTime")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.offsetResetSwitchTime = string2Int(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.offsetResetSwitchTimeForDarkMode = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.offsetResetEnable = string2Boolean(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.offsetResetShortSwitchTime = string2Int(parser.nextText());
            } else if (c != 4) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.offsetResetShortLuxDelta = string2Int(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.offsetResetSwitchTime >= 0 && HwBrightnessXmlLoader.mData.offsetResetShortSwitchTime >= 0 && HwBrightnessXmlLoader.mData.offsetResetShortLuxDelta >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_OffsetGroup extends HwXmlElement {
        private Element_OffsetGroup() {
        }

        public String getName() {
            return "OffsetGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("OffsetBrightenDebounceTime", "OffsetDarkenDebounceTime", "OffsetValidAmbientLuxEnable", "AutoModeInOutDoorLimitEnble");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1841552692:
                    if (valueName.equals("OffsetBrightenDebounceTime")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1168465949:
                    if (valueName.equals("OffsetValidAmbientLuxEnable")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 110170888:
                    if (valueName.equals("OffsetDarkenDebounceTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1853282876:
                    if (valueName.equals("AutoModeInOutDoorLimitEnble")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.offsetBrightenDebounceTime = string2Int(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.offsetDarkenDebounceTime = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.offsetValidAmbientLuxEnable = string2Boolean(parser.nextText());
            } else if (c != 3) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.autoModeInOutDoorLimitEnble = string2Boolean(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.offsetBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.offsetDarkenDebounceTime >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementAutoMaxBrightnessGroup extends HwXmlElement {
        private ElementAutoMaxBrightnessGroup() {
        }

        public String getName() {
            return "AutoMaxBrightnessGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("IsAutoModeSeekBarMaxBrightnessBasedLux", "AutoModeSeekBarMaxBrightnessLuxTh", "AutoModeSeekBarMaxBrightnessValue");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                Slog.e(this.TAG, "parser AutoMaxBrightnessGroup is null");
                return false;
            }
            String valueName = parser.getName();
            char c = 65535;
            int hashCode = valueName.hashCode();
            if (hashCode != -1351655150) {
                if (hashCode != -783949577) {
                    if (hashCode == -775320699 && valueName.equals("AutoModeSeekBarMaxBrightnessValue")) {
                        c = 2;
                    }
                } else if (valueName.equals("AutoModeSeekBarMaxBrightnessLuxTh")) {
                    c = 1;
                }
            } else if (valueName.equals("IsAutoModeSeekBarMaxBrightnessBasedLux")) {
                c = 0;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.isAutoModeSeekBarMaxBrightnessBasedLux = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.autoModeSeekBarMaxBrightnessLuxTh = string2Float(parser.nextText());
            } else if (c != 2) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.autoModeSeekBarMaxBrightnessValue = string2Int(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.autoModeSeekBarMaxBrightnessValue > 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_DarkLightGroup extends HwXmlElement {
        private Element_DarkLightGroup() {
        }

        public String getName() {
            return "DarkLightGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("DarkLightLevelMinThreshold", "DarkLightLevelMaxThreshold", "DarkLightLevelRatio", "DarkLightLuxMinThreshold", "DarkLightLuxMaxThreshold", "DarkLightLuxDelta");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1821009752:
                    if (valueName.equals("DarkLightLuxMinThreshold")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1305576921:
                    if (valueName.equals("DarkLightLevelRatio")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 739225035:
                    if (valueName.equals("DarkLightLevelMaxThreshold")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1213651101:
                    if (valueName.equals("DarkLightLevelMinThreshold")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1899056169:
                    if (valueName.equals("DarkLightLuxDelta")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1999531478:
                    if (valueName.equals("DarkLightLuxMaxThreshold")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.darkLightLevelMinThreshold = string2Int(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.darkLightLevelMaxThreshold = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.darkLightLevelRatio = string2Float(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.darkLightLuxMinThreshold = string2Float(parser.nextText());
            } else if (c == 4) {
                HwBrightnessXmlLoader.mData.darkLightLuxMaxThreshold = string2Float(parser.nextText());
            } else if (c != 5) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.darkLightLuxDelta = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkLightLevelMinThreshold >= 0 && HwBrightnessXmlLoader.mData.darkLightLevelMaxThreshold >= 0 && HwBrightnessXmlLoader.mData.darkLightLevelRatio > 0.0f && HwBrightnessXmlLoader.mData.darkLightLuxMinThreshold >= 0.0f && HwBrightnessXmlLoader.mData.darkLightLuxMaxThreshold >= 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_RebootGroup extends HwXmlElement {
        private Element_RebootGroup() {
        }

        public String getName() {
            return "RebootGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("RebootFirstBrightnessAnimationEnable", "RebootFirstBrightness", "RebootFirstBrightnessAutoTime", "RebootFirstBrightnessManualTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2008780644:
                    if (valueName.equals("RebootFirstBrightness")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1474056744:
                    if (valueName.equals("RebootFirstBrightnessAutoTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -752945685:
                    if (valueName.equals("RebootFirstBrightnessAnimationEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -260795025:
                    if (valueName.equals("RebootFirstBrightnessManualTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.rebootFirstBrightnessAnimationEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.rebootFirstBrightness = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.rebootFirstBrightnessAutoTime = string2Float(parser.nextText());
            } else if (c != 3) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.rebootFirstBrightnessManualTime = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.rebootFirstBrightness >= 0 && HwBrightnessXmlLoader.mData.rebootFirstBrightnessAutoTime > 0.0f && HwBrightnessXmlLoader.mData.rebootFirstBrightnessManualTime > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_SceneProcessing extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_SceneProcessingGroup extends HwXmlElement {
        private Element_SceneProcessingGroup() {
        }

        public String getName() {
            return "SceneProcessingGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("SceneMaxPoints", "SceneGapPoints", "SceneMinPoints", "SceneAmbientLuxMaxWeight", "SceneAmbientLuxMinWeight");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1975443769:
                    if (valueName.equals("SceneAmbientLuxMinWeight")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 180392013:
                    if (valueName.equals("SceneGapPoints")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 730661979:
                    if (valueName.equals("SceneMaxPoints")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1503140553:
                    if (valueName.equals("SceneMinPoints")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1547044953:
                    if (valueName.equals("SceneAmbientLuxMaxWeight")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.sceneMaxPoints = string2Int(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.sceneGapPoints = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.sceneMinPoints = string2Int(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.sceneAmbientLuxMaxWeight = string2Float(parser.nextText());
            } else if (c != 4) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.sceneAmbientLuxMinWeight = string2Float(parser.nextText());
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_BrightenlinePoints extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_BrightenlinePoints_Point extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.brightenlinePoints, "brightenlinePoints")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_DarkenlinePoints extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_DarkenlinePoints_Point extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.darkenlinePoints, "darkenlinePoints")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDefaultBrightnessPoints extends HwXmlElement {
        private ElementDefaultBrightnessPoints() {
        }

        public String getName() {
            return "DefaultBrightnessPoints";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDefaultBrightnessPointsPoint extends HwXmlElement {
        private ElementDefaultBrightnessPointsPoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.defaultBrightnessLinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.defaultBrightnessLinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.defaultBrightnessLinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.defaultBrightnessLinePoints, "defaultBrightnessLinePoints")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.defaultBrightnessLinePoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDayModeBrightnessPoints extends HwXmlElement {
        private ElementDayModeBrightnessPoints() {
        }

        public String getName() {
            return "DayModeBrightnessPoints";
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            HwBrightnessXmlLoader.mData.dayModeNewCurveEnable = true;
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDayModeBrightnessPointsPoint extends HwXmlElement {
        private ElementDayModeBrightnessPointsPoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.dayModeBrightnessLinePoints.clear();
            }
            HwBrightnessXmlLoader.mData.dayModeBrightnessLinePoints = parsePointFList(parser, HwBrightnessXmlLoader.mData.dayModeBrightnessLinePoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.dayModeBrightnessLinePoints, "dayModeBrightnessLinePoints")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.dayModeBrightnessLinePoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_AnimateGroup extends HwXmlElement {
        private Element_AnimateGroup() {
        }

        public String getName() {
            return "AnimateGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("BrightenGradualTime", "DarkenGradualTime", "BrightenThresholdFor255", "DarkenTargetFor255", "DarkenCurrentFor255");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1498609295:
                    if (valueName.equals("BrightenThresholdFor255")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 884036153:
                    if (valueName.equals("DarkenTargetFor255")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1455932156:
                    if (valueName.equals("BrightenGradualTime")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1739695104:
                    if (valueName.equals("DarkenGradualTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1877080899:
                    if (valueName.equals("DarkenCurrentFor255")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.brightenGradualTime = string2Float(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.darkenGradualTime = string2Float(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.brightenThresholdFor255 = string2Int(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.darkenTargetFor255 = string2Int(parser.nextText());
            } else if (c != 4) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.darkenCurrentFor255 = string2Int(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brightenGradualTime > 0.0f && HwBrightnessXmlLoader.mData.darkenGradualTime > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_AnimateOptionalGroup extends HwXmlElement {
        private Element_AnimateOptionalGroup() {
        }

        public String getName() {
            return "AnimateOptionalGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("AutoFastTimeFor255", "ManualFastTimeFor255", "DimTime", "AnimationEqualRatioEnable", "ScreenBrightnessMinNit", "ScreenBrightnessMaxNit", "TargetFor255BrightenTime", "MinAnimatingStep");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1857716415:
                    if (valueName.equals("AutoFastTimeFor255")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -964927659:
                    if (valueName.equals("DimTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -935718286:
                    if (valueName.equals("ScreenBrightnessMaxNit")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -928628028:
                    if (valueName.equals("ScreenBrightnessMinNit")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -322892520:
                    if (valueName.equals("ManualFastTimeFor255")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 964830634:
                    if (valueName.equals("TargetFor255BrightenTime")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1325505822:
                    if (valueName.equals("AnimationEqualRatioEnable")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1740888632:
                    if (valueName.equals("MinAnimatingStep")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
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
                    HwBrightnessXmlLoader.mData.targetFor255BrightenTime = string2Float(parser.nextText());
                    break;
                case 7:
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
            return HwBrightnessXmlLoader.mData.screenBrightnessMinNit > 0.0f && HwBrightnessXmlLoader.mData.screenBrightnessMaxNit > HwBrightnessXmlLoader.mData.screenBrightnessMinNit;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementAnimateManulSeekbarGroup extends HwXmlElement {
        private ElementAnimateManulSeekbarGroup() {
        }

        public String getName() {
            return "AnimateManulSeekbarGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("IsSeekBarManualDimTimeEnable", "SeekBarManualBrightenDimTime", "SeekBarManualDarkenDimTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                Slog.e(this.TAG, "parser animateManulSeekbarGroup is null");
                return false;
            }
            String valueName = parser.getName();
            char c = 65535;
            int hashCode = valueName.hashCode();
            if (hashCode != -836484267) {
                if (hashCode != -393948015) {
                    if (hashCode == 422185377 && valueName.equals("IsSeekBarManualDimTimeEnable")) {
                        c = 0;
                    }
                } else if (valueName.equals("SeekBarManualBrightenDimTime")) {
                    c = 1;
                }
            } else if (valueName.equals("SeekBarManualDarkenDimTime")) {
                c = 2;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.isSeekBarManualDimTimeEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.seekBarManualBrightenDimTime = string2Float(parser.nextText());
            } else if (c != 2) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.seekBarManualDarkenDimTime = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.seekBarManualBrightenDimTime > 0.0f && HwBrightnessXmlLoader.mData.seekBarManualDarkenDimTime > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementAnimateExtendedGroup extends HwXmlElement {
        private ElementAnimateExtendedGroup() {
        }

        public String getName() {
            return "AnimateExtendedGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("SeekBarDimTime", "IsLowEqualDarkenEnable", "ScreenBrightnessNormalModeMaxNit", "ScreenBrightnessNormalModeMaxLevel", "LowEqualDarkenLevel", "LowEqualDarkenNit", "LowEqualDarkenMinLevel", "LowEqualDarkenMinTime", "LowEqualDarkenMaxTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1250188678:
                    if (valueName.equals("LowEqualDarkenNit")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -890865510:
                    if (valueName.equals("SeekBarDimTime")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -681080153:
                    if (valueName.equals("ScreenBrightnessNormalModeMaxLevel")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -416348676:
                    if (valueName.equals("ScreenBrightnessNormalModeMaxNit")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 795146802:
                    if (valueName.equals("LowEqualDarkenMaxTime")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1014944800:
                    if (valueName.equals("LowEqualDarkenMinTime")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 1157562277:
                    if (valueName.equals("LowEqualDarkenLevel")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1391019153:
                    if (valueName.equals("LowEqualDarkenMinLevel")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1413268940:
                    if (valueName.equals("IsLowEqualDarkenEnable")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.seekBarDimTime = string2Float(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.isLowEqualDarkenEnable = string2Boolean(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.screenBrightnessNormalModeMaxNit = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.screenBrightnessNormalModeMaxLevel = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.lowEqualDarkenLevel = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.lowEqualDarkenNit = string2Float(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.lowEqualDarkenMinLevel = string2Float(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.lowEqualDarkenMinTime = string2Float(parser.nextText());
                    break;
                case '\b':
                    HwBrightnessXmlLoader.mData.lowEqualDarkenMaxTime = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.seekBarDimTime > 0.0f && HwBrightnessXmlLoader.mData.screenBrightnessNormalModeMaxNit > 0.0f && HwBrightnessXmlLoader.mData.screenBrightnessNormalModeMaxLevel > 0.0f && HwBrightnessXmlLoader.mData.lowEqualDarkenLevel > 0.0f && HwBrightnessXmlLoader.mData.lowEqualDarkenMinTime > 0.0f && HwBrightnessXmlLoader.mData.lowEqualDarkenMaxTime > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementAnimateForLowPowerMappingGroup extends HwXmlElement {
        private ElementAnimateForLowPowerMappingGroup() {
        }

        public String getName() {
            return "AnimateForLowPowerMappingGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("IsLowPowerMappingEnable", "LowPowerMappingLevel", "LowPowerMappingNit", "LowPowerMappingLevelRatio");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                Slog.e(this.TAG, "parser AnimateForLowPowerMappingGroup is null");
                return false;
            }
            String valueName = parser.getName();
            char c = 65535;
            switch (valueName.hashCode()) {
                case -1967678922:
                    if (valueName.equals("IsLowPowerMappingEnable")) {
                        c = 0;
                        break;
                    }
                    break;
                case -277101529:
                    if (valueName.equals("LowPowerMappingLevel")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1115495460:
                    if (valueName.equals("LowPowerMappingLevelRatio")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1644404604:
                    if (valueName.equals("LowPowerMappingNit")) {
                        c = 2;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.isLowPowerMappingEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.lowPowerMappingLevel = string2Float(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.lowPowerMappingNit = string2Float(parser.nextText());
            } else if (c != 3) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.lowPowerMappingLevelRatio = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.lowPowerMappingLevel > 0.0f && HwBrightnessXmlLoader.mData.lowPowerMappingNit > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementAnimateForHighEqualDarkenGroup extends HwXmlElement {
        private ElementAnimateForHighEqualDarkenGroup() {
        }

        public String getName() {
            return "AnimateForHighEqualDarkenGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("IsHighEqualDarkenEnable", "HighEqualDarkenMinTime", "HighEqualDarkenMaxTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0076  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1139354198) {
                if (hashCode != 675993920) {
                    if (hashCode == 895791918 && valueName.equals("HighEqualDarkenMinTime")) {
                        c = 1;
                        if (c != 0) {
                            HwBrightnessXmlLoader.mData.isHighEqualDarkenEnable = string2Boolean(parser.nextText());
                        } else if (c == 1) {
                            HwBrightnessXmlLoader.mData.highEqualDarkenMinTime = string2Float(parser.nextText());
                        } else if (c != 2) {
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                        } else {
                            HwBrightnessXmlLoader.mData.highEqualDarkenMaxTime = string2Float(parser.nextText());
                        }
                        return true;
                    }
                } else if (valueName.equals("HighEqualDarkenMaxTime")) {
                    c = 2;
                    if (c != 0) {
                    }
                    return true;
                }
            } else if (valueName.equals("IsHighEqualDarkenEnable")) {
                c = 0;
                if (c != 0) {
                }
                return true;
            }
            c = 65535;
            if (c != 0) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.highEqualDarkenMinTime > 0.0f && HwBrightnessXmlLoader.mData.highEqualDarkenMaxTime > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementAnimateForHbmGroup extends HwXmlElement {
        private ElementAnimateForHbmGroup() {
        }

        public String getName() {
            return "AnimateForHbmGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("IsDarkenAnimatingStepForHbm", "MaxDarkenAnimatingStepForHbm", "MinDarkenBrightnessLevelForHbm");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0076  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1587998959) {
                if (hashCode != -1386349108) {
                    if (hashCode == -912153141 && valueName.equals("IsDarkenAnimatingStepForHbm")) {
                        c = 0;
                        if (c != 0) {
                            HwBrightnessXmlLoader.mData.isDarkenAnimatingStepForHbm = string2Boolean(parser.nextText());
                        } else if (c == 1) {
                            HwBrightnessXmlLoader.mData.maxDarkenAnimatingStepForHbm = string2Float(parser.nextText());
                        } else if (c != 2) {
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                        } else {
                            HwBrightnessXmlLoader.mData.minDarkenBrightnessLevelForHbm = string2Float(parser.nextText());
                        }
                        return true;
                    }
                } else if (valueName.equals("MinDarkenBrightnessLevelForHbm")) {
                    c = 2;
                    if (c != 0) {
                    }
                    return true;
                }
            } else if (valueName.equals("MaxDarkenAnimatingStepForHbm")) {
                c = 1;
                if (c != 0) {
                }
                return true;
            }
            c = 65535;
            if (c != 0) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.maxDarkenAnimatingStepForHbm > 0.0f && HwBrightnessXmlLoader.mData.minDarkenBrightnessLevelForHbm > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementAnimateFlickerGroup extends HwXmlElement {
        private ElementAnimateFlickerGroup() {
        }

        public String getName() {
            return "AnimateFlickerGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("BrightenFlickerTargetMin", "BrightenFlickerAmountMin", "BrightenFlickerGradualTimeMin", "BrightenFlickerGradualTimeMax", "DarkenNoFlickerTargetGradualTimeMin", "DarkenNoFlickerTarget", "LinearDimmingValueTh", "BrightenTimeLongCurrentTh", "BrightenTimeLongAmountMin", "BrightenGradualTimeLong");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2022875443:
                    if (valueName.equals("LinearDimmingValueTh")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -1784341723:
                    if (valueName.equals("BrightenFlickerAmountMin")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -337208594:
                    if (valueName.equals("BrightenTimeLongAmountMin")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case -326366751:
                    if (valueName.equals("BrightenTimeLongCurrentTh")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 72853673:
                    if (valueName.equals("DarkenNoFlickerTarget")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 138956602:
                    if (valueName.equals("BrightenFlickerGradualTimeMax")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 138956840:
                    if (valueName.equals("BrightenFlickerGradualTimeMin")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 428945052:
                    if (valueName.equals("DarkenNoFlickerTargetGradualTimeMin")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1048265548:
                    if (valueName.equals("BrightenFlickerTargetMin")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1461329816:
                    if (valueName.equals("BrightenGradualTimeLong")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.brightenFlickerTargetMin = string2Int(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.brightenFlickerAmountMin = string2Int(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.brightenFlickerGradualTimeMin = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.brightenFlickerGradualTimeMax = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.darkenNoFlickerTargetGradualTimeMin = string2Float(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.darkenNoFlickerTarget = string2Float(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.linearDimmingValueTh = string2Int(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.brightenTimeLongCurrentTh = string2Int(parser.nextText());
                    break;
                case '\b':
                    HwBrightnessXmlLoader.mData.brightenTimeLongAmountMin = string2Int(parser.nextText());
                    break;
                case '\t':
                    HwBrightnessXmlLoader.mData.brightenGradualTimeLong = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brightenFlickerTargetMin >= 0 && HwBrightnessXmlLoader.mData.brightenFlickerAmountMin >= 0 && HwBrightnessXmlLoader.mData.linearDimmingValueTh >= 0 && HwBrightnessXmlLoader.mData.brightenTimeLongCurrentTh >= 0 && HwBrightnessXmlLoader.mData.brightenTimeLongAmountMin >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_VariableStep extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_VariableStepGroup extends HwXmlElement {
        private Element_VariableStepGroup() {
        }

        public String getName() {
            return "VariableStepGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("DarkenGradualTimeMax", "DarkenGradualTimeMin", "AnimatedStepRoundEnabled");
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0076  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1259361630) {
                if (hashCode != -113440444) {
                    if (hashCode == -113440206 && valueName.equals("DarkenGradualTimeMin")) {
                        c = 1;
                        if (c != 0) {
                            HwBrightnessXmlLoader.mData.darkenGradualTimeMax = string2Float(parser.nextText());
                        } else if (c == 1) {
                            HwBrightnessXmlLoader.mData.darkenGradualTimeMin = string2Float(parser.nextText());
                        } else if (c != 2) {
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                        } else {
                            HwBrightnessXmlLoader.mData.animatedStepRoundEnabled = string2Boolean(parser.nextText());
                        }
                        return true;
                    }
                } else if (valueName.equals("DarkenGradualTimeMax")) {
                    c = 0;
                    if (c != 0) {
                    }
                    return true;
                }
            } else if (valueName.equals("AnimatedStepRoundEnabled")) {
                c = 2;
                if (c != 0) {
                }
                return true;
            }
            c = 65535;
            if (c != 0) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkenGradualTimeMax > 0.0f && HwBrightnessXmlLoader.mData.darkenGradualTimeMin >= 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_Proximity extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_ProximityGroup extends HwXmlElement {
        private Element_ProximityGroup() {
        }

        public String getName() {
            return "ProximityGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("AllowLabcUseProximity", "ProximityPositiveDebounceTime", "ProximityNegativeDebounceTime");
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0076  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -636219254) {
                if (hashCode != 1277664151) {
                    if (hashCode == 1847924814 && valueName.equals("ProximityPositiveDebounceTime")) {
                        c = 1;
                        if (c != 0) {
                            HwBrightnessXmlLoader.mData.allowLabcUseProximity = string2Boolean(parser.nextText());
                        } else if (c == 1) {
                            HwBrightnessXmlLoader.mData.proximityPositiveDebounceTime = string2Int(parser.nextText());
                        } else if (c != 2) {
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                        } else {
                            HwBrightnessXmlLoader.mData.proximityNegativeDebounceTime = string2Int(parser.nextText());
                        }
                        return true;
                    }
                } else if (valueName.equals("AllowLabcUseProximity")) {
                    c = 0;
                    if (c != 0) {
                    }
                    return true;
                }
            } else if (valueName.equals("ProximityNegativeDebounceTime")) {
                c = 2;
                if (c != 0) {
                }
                return true;
            }
            c = 65535;
            if (c != 0) {
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDynamicProximityGroup extends HwXmlElement {
        private ElementDynamicProximityGroup() {
        }

        public String getName() {
            return "DynamicProximityGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("IsDynamicEnableProximity", "EnableProximityDelayTime", "DisableProximityDelayTime", "UpdateDarkAmbientLuxDelayTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                Slog.e(this.TAG, "parser DynamicProximityGroup is null");
                return false;
            }
            String valueName = parser.getName();
            char c = 65535;
            switch (valueName.hashCode()) {
                case -1363457062:
                    if (valueName.equals("UpdateDarkAmbientLuxDelayTime")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1597669428:
                    if (valueName.equals("EnableProximityDelayTime")) {
                        c = 1;
                        break;
                    }
                    break;
                case 1597966873:
                    if (valueName.equals("DisableProximityDelayTime")) {
                        c = 2;
                        break;
                    }
                    break;
                case 2074034663:
                    if (valueName.equals("IsDynamicEnableProximity")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.isDynamicEnableProximity = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.enableProximityDelayTime = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.disableProximityDelayTime = string2Int(parser.nextText());
            } else if (c != 3) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.updateDarkAmbientLuxDelayTime = string2Int(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_ManualGroup extends HwXmlElement {
        private Element_ManualGroup() {
        }

        public String getName() {
            return "ManualGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("ManualMode", "ManualBrightnessMaxLimit", "ManualBrightnessMinLimit", "OutDoorThreshold", "InDoorThreshold", "ManualBrighenDebounceTime", "ManualDarkenDebounceTime", "QRCodeBrightnessminLimit");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return !HwBrightnessXmlLoader.mData.manualMode;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
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
                    c = 65535;
                    break;
                case -790346984:
                    if (valueName.equals("InDoorThreshold")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -585412741:
                    if (valueName.equals("ManualDarkenDebounceTime")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -139934680:
                    if (valueName.equals("QRCodeBrightnessminLimit")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 346705267:
                    if (valueName.equals("ManualBrighenDebounceTime")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 597434025:
                    if (valueName.equals("ManualMode")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 701544399:
                    if (valueName.equals("OutDoorThreshold")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 825502336:
                    if (valueName.equals("ManualBrightnessMinLimit")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    Data data = HwBrightnessXmlLoader.mData;
                    if (string2Int(parser.nextText()) == 1) {
                        z = true;
                    }
                    data.manualMode = z;
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

    /* access modifiers changed from: private */
    public static class Element_ManualOptionalGroup extends HwXmlElement {
        private Element_ManualOptionalGroup() {
        }

        public String getName() {
            return "ManualOptionalGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("ManualAnimationBrightenTime", "ManualAnimationDarkenTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002c  */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x0055  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1800575254) {
                if (hashCode == -1654934546 && valueName.equals("ManualAnimationBrightenTime")) {
                    c = 0;
                    if (c == 0) {
                        HwBrightnessXmlLoader.mData.manualAnimationBrightenTime = string2Float(parser.nextText());
                    } else if (c != 1) {
                        Slog.e(this.TAG, "unknow valueName=" + valueName);
                        return false;
                    } else {
                        HwBrightnessXmlLoader.mData.manualAnimationDarkenTime = string2Float(parser.nextText());
                    }
                    return true;
                }
            } else if (valueName.equals("ManualAnimationDarkenTime")) {
                c = 1;
                if (c == 0) {
                }
                return true;
            }
            c = 65535;
            if (c == 0) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.manualAnimationBrightenTime > 0.0f && HwBrightnessXmlLoader.mData.manualAnimationDarkenTime > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_ManualBrightenLinePoints extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_ManualBrightenLinePoints_Point extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.manualBrightenlinePoints, "manualBrightenlinePoints")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.manualBrightenlinePoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_ManualDarkenLinePoints extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_ManualDarkenLinePoints_Point extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.manualDarkenlinePoints, "manualDarkenlinePoints")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.manualDarkenlinePoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_BrightnessMappingPoints extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_BrightnessMappingPoints_Point extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_AmbientLuxValidBrightnessPoints extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_AmbientLuxValidBrightnessPoints_Point extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxAmPointValid(HwBrightnessXmlLoader.mData.ambientLuxValidBrightnessPoints, "ambientLuxValidBrightnessPoints")) {
                return HwBrightnessXmlLoader.checkAmPointsListIsOK(HwBrightnessXmlLoader.mData.ambientLuxValidBrightnessPoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_GameModeAmbientLuxValidBrightnessPoints extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_GameModeAmbientLuxValidBrightnessPoints_Point extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxAmPointValid(HwBrightnessXmlLoader.mData.gameModeAmbientLuxValidBrightnessPoints, "gameModeAmbientLuxValidBrightnessPoints")) {
                return HwBrightnessXmlLoader.checkAmPointsListIsOK(HwBrightnessXmlLoader.mData.gameModeAmbientLuxValidBrightnessPoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_DarkAdapter extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_DarkAdapterGroup1 extends HwXmlElement {
        private Element_DarkAdapterGroup1() {
        }

        public String getName() {
            return "DarkAdapterGroup1";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("Unadapt2AdaptingShortFilterLux", "Unadapt2AdaptingLongFilterLux", "Unadapt2AdaptingLongFilterSec", "Unadapt2AdaptingDimSec", "Adapting2UnadaptShortFilterLux", "Adapting2AdaptedOffDurationMinSec", "Adapting2AdaptedOffDurationFilterSec", "Adapting2AdaptedOffDurationMaxSec", "Adapting2AdaptedOnClockNoFilterBeginHour", "Adapting2AdaptedOnClockNoFilterEndHour");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1713045852:
                    if (valueName.equals("Unadapt2AdaptingShortFilterLux")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1656034879:
                    if (valueName.equals("Adapting2AdaptedOffDurationMaxSec")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -1648944621:
                    if (valueName.equals("Adapting2AdaptedOffDurationMinSec")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1292961038:
                    if (valueName.equals("Unadapt2AdaptingLongFilterLux")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1292954828:
                    if (valueName.equals("Unadapt2AdaptingLongFilterSec")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 557886292:
                    if (valueName.equals("Adapting2AdaptedOnClockNoFilterBeginHour")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 758345798:
                    if (valueName.equals("Adapting2AdaptedOnClockNoFilterEndHour")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 824735218:
                    if (valueName.equals("Unadapt2AdaptingDimSec")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 896190834:
                    if (valueName.equals("Adapting2UnadaptShortFilterLux")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1064291269:
                    if (valueName.equals("Adapting2AdaptedOffDurationFilterSec")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
                    HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterBeginHour = string2Int(parser.nextText());
                    break;
                case '\t':
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
            return HwBrightnessXmlLoader.mData.unadapt2AdaptingShortFilterLux > 0.0f && HwBrightnessXmlLoader.mData.unadapt2AdaptingLongFilterLux > 0.0f && HwBrightnessXmlLoader.mData.unadapt2AdaptingLongFilterSec > 0 && HwBrightnessXmlLoader.mData.unadapt2AdaptingDimSec > 0 && HwBrightnessXmlLoader.mData.adapting2UnadaptShortFilterLux > 0.0f && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMinSec > 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationFilterSec > 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationFilterSec > HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMinSec && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMaxSec > 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationMaxSec > HwBrightnessXmlLoader.mData.adapting2AdaptedOffDurationFilterSec && HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterBeginHour >= 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterBeginHour < 24 && HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterEndHour >= 0 && HwBrightnessXmlLoader.mData.adapting2AdaptedOnClockNoFilterEndHour < 24;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_DarkAdapterGroup2 extends HwXmlElement {
        private Element_DarkAdapterGroup2() {
        }

        public String getName() {
            return "DarkAdapterGroup2";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("Unadapt2AdaptedShortFilterLux", "Unadapt2AdaptedOffDurationMinSec", "Unadapt2AdaptedOnClockNoFilterBeginHour", "Unadapt2AdaptedOnClockNoFilterEndHour", "Adapted2UnadaptShortFilterLux");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1872793361:
                    if (valueName.equals("Unadapt2AdaptedOnClockNoFilterEndHour")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -966441933:
                    if (valueName.equals("Adapted2UnadaptShortFilterLux")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -698098637:
                    if (valueName.equals("Unadapt2AdaptedShortFilterLux")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 603296714:
                    if (valueName.equals("Unadapt2AdaptedOffDurationMinSec")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1768891837:
                    if (valueName.equals("Unadapt2AdaptedOnClockNoFilterBeginHour")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.unadapt2AdaptedShortFilterLux = string2Float(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.unadapt2AdaptedOffDurationMinSec = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterBeginHour = string2Int(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterEndHour = string2Int(parser.nextText());
            } else if (c != 4) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.adapted2UnadaptShortFilterLux = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.unadapt2AdaptedShortFilterLux > 0.0f && HwBrightnessXmlLoader.mData.unadapt2AdaptedOffDurationMinSec > 0 && HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterBeginHour >= 0 && HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterBeginHour < 24 && HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterEndHour >= 0 && HwBrightnessXmlLoader.mData.unadapt2AdaptedOnClockNoFilterEndHour < 24 && HwBrightnessXmlLoader.mData.adapted2UnadaptShortFilterLux > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_TouchProximity extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_TouchProximityGroup extends HwXmlElement {
        private Element_TouchProximityGroup() {
        }

        public String getName() {
            return "TouchProximityGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("Enable", "YNearbyRatio", "YRatioMin", "YRatioMax", "ProximitySceneModeEnable");
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1458251566:
                    if (valueName.equals("YRatioMax")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1458251328:
                    if (valueName.equals("YRatioMin")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1214228397:
                    if (valueName.equals("ProximitySceneModeEnable")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -286453069:
                    if (valueName.equals("YNearbyRatio")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 2079986083:
                    if (valueName.equals("Enable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.touchProximityEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin = 1.0f - string2Float(parser.nextText());
                HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax = 1.0f;
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin = string2Float(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax = string2Float(parser.nextText());
            } else if (c != 4) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.proximitySceneModeEnable = string2Boolean(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.mData.touchProximityEnable) {
                return true;
            }
            if (HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin < 0.0f || HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin >= 1.0f || HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax <= 0.0f || HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax > 1.0f || HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMin >= HwBrightnessXmlLoader.mData.touchProximityYNearbyRatioMax) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_VehicleModeGroup extends HwXmlElement {
        private Element_VehicleModeGroup() {
        }

        public String getName() {
            return "VehicleModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("VehicleModeEnable", "VehicleModeDisableTimeMillis", "VehicleModeQuitTimeForPowerOn", "VehicleModeEnterTimeForPowerOn");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -946673391:
                    if (valueName.equals("VehicleModeEnterTimeForPowerOn")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 349236422:
                    if (valueName.equals("VehicleModeQuitTimeForPowerOn")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 689432882:
                    if (valueName.equals("VehicleModeEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 939700588:
                    if (valueName.equals("VehicleModeDisableTimeMillis")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.vehicleModeEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.vehicleModeDisableTimeMillis = string2Long(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.vehicleModeQuitTimeForPowerOn = string2Long(parser.nextText());
            } else if (c != 3) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.vehicleModeEnterTimeForPowerOn = string2Long(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.vehicleModeDisableTimeMillis >= 0 && HwBrightnessXmlLoader.mData.vehicleModeQuitTimeForPowerOn >= 0 && HwBrightnessXmlLoader.mData.vehicleModeEnterTimeForPowerOn >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementWalkModeGroup extends HwXmlElement {
        private ElementWalkModeGroup() {
        }

        public String getName() {
            return "WalkModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("IsWalkModeEnable", "WalkModeMinLux");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002c  */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x0055  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -12675175) {
                if (hashCode == 1551597225 && valueName.equals("WalkModeMinLux")) {
                    c = 1;
                    if (c == 0) {
                        HwBrightnessXmlLoader.mData.isWalkModeEnable = string2Boolean(parser.nextText());
                    } else if (c != 1) {
                        Slog.e(this.TAG, "unknow valueName=" + valueName);
                        return false;
                    } else {
                        HwBrightnessXmlLoader.mData.walkModeMinLux = string2Float(parser.nextText());
                    }
                    return true;
                }
            } else if (valueName.equals("IsWalkModeEnable")) {
                c = 0;
                if (c == 0) {
                }
                return true;
            }
            c = 65535;
            if (c == 0) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.walkModeMinLux >= 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_GameModeGroup extends HwXmlElement {
        private Element_GameModeGroup() {
        }

        public String getName() {
            return "VehicleModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("GameModeEnable", "GameModeBrightenAnimationTime", "GameModeDarkentenAnimationTime", "GameModeDarkentenLongAnimationTime", "GameModeDarkentenLongTarget", "GameModeDarkentenLongCurrent", "GameModeClearOffsetTime", "GameModeBrightenDebounceTime", "GameModeDarkenDebounceTime", "GameModeLuxThresholdEnable", "GameModeBrightnessLimitationEnable", "GameModeBrightnessFloor");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1916112012:
                    if (valueName.equals("GameModeLuxThresholdEnable")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -1810836822:
                    if (valueName.equals("GameModeDarkenDebounceTime")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case -1148292424:
                    if (valueName.equals("GameModeEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1094024722:
                    if (valueName.equals("GameModeBrightenDebounceTime")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -963189976:
                    if (valueName.equals("GameModeDarkentenAnimationTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -407732679:
                    if (valueName.equals("GameModeBrightenAnimationTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -75444058:
                    if (valueName.equals("GameModeBrightnessFloor")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                case 1171599316:
                    if (valueName.equals("GameModeDarkentenLongCurrent")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1174892899:
                    if (valueName.equals("GameModeBrightnessLimitationEnable")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 1419525784:
                    if (valueName.equals("GameModeClearOffsetTime")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1832157836:
                    if (valueName.equals("GameModeDarkentenLongAnimationTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 2030028758:
                    if (valueName.equals("GameModeDarkentenLongTarget")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
                    HwBrightnessXmlLoader.mData.gameModeDarkenDebounceTime = string2Long(parser.nextText());
                    break;
                case '\t':
                    HwBrightnessXmlLoader.mData.gameModeLuxThresholdEnable = string2Boolean(parser.nextText());
                    break;
                case '\n':
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
            return HwBrightnessXmlLoader.mData.gameModeBrightenAnimationTime > 0.0f && HwBrightnessXmlLoader.mData.gameModeDarkentenAnimationTime > 0.0f && HwBrightnessXmlLoader.mData.gameModeDarkentenLongAnimationTime > 0.0f && HwBrightnessXmlLoader.mData.gameModeDarkentenLongTarget >= 0 && HwBrightnessXmlLoader.mData.gameModeDarkentenLongCurrent >= 0 && HwBrightnessXmlLoader.mData.gameModeClearOffsetTime >= 0 && HwBrightnessXmlLoader.mData.gameModeBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.gameModeDarkenDebounceTime >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_BrightenlinePointsForGameMode extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_BrightenlinePointsForGameMode_Point extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.brightenlinePointsForGameMode, "brightenlinePointsForGameMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePointsForGameMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_DarkenlinePointsForGameMode extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_DarkenlinePointsForGameMode_Point extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.darkenlinePointsForGameMode, "darkenlinePointsForGameMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePointsForGameMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDcModeGroup extends HwXmlElement {
        private ElementDcModeGroup() {
        }

        public String getName() {
            return "DCModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("DcModeEnable", "DcModeBrightenDebounceTime", "DcModeDarkenDebounceTime", "DcModeLuxThresholdEnable");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -387239045:
                    if (valueName.equals("DcModeBrightenDebounceTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 60040257:
                    if (valueName.equals("DcModeLuxThresholdEnable")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 165315447:
                    if (valueName.equals("DcModeDarkenDebounceTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1378430725:
                    if (valueName.equals("DcModeEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.dcModeEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.dcModeBrightenDebounceTime = string2Long(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.dcModeDarkenDebounceTime = string2Long(parser.nextText());
            } else if (c != 3) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.dcModeLuxThresholdEnable = string2Boolean(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.dcModeBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.dcModeDarkenDebounceTime >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBrightenlinePointsForDcMode extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class ElementBrightenlinePointsForDcModePoint extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.brightenlinePointsForDcMode, "brightenlinePointsForDcMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePointsForDcMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDarkenlinePointsForDcMode extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class ElementDarkenlinePointsForDcModePoint extends HwXmlElement {
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
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.darkenlinePointsForDcMode, "darkenlinePointsForDcMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePointsForDcMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_CryogenicGroup extends HwXmlElement {
        private Element_CryogenicGroup() {
        }

        public String getName() {
            return "CryogenicGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("CryogenicEnable", "CryogenicBrightnessMappingEnable", "CryogenicMaxBrightnessTimeOut", "CryogenicActiveScreenOffIntervalInMillis", "CryogenicLagTimeInMillis");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1946012229:
                    if (valueName.equals("CryogenicBrightnessMappingEnable")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1243020920:
                    if (valueName.equals("CryogenicEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 971881064:
                    if (valueName.equals("CryogenicActiveScreenOffIntervalInMillis")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1229222385:
                    if (valueName.equals("CryogenicMaxBrightnessTimeOut")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1592001029:
                    if (valueName.equals("CryogenicLagTimeInMillis")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.cryogenicEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.cryogenicModeBrightnessMappingEnable = string2Boolean(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.cryogenicMaxBrightnessTimeOut = string2Long(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.cryogenicActiveScreenOffIntervalInMillis = string2Long(parser.nextText());
            } else if (c != 4) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.cryogenicLagTimeInMillis = string2Long(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.cryogenicMaxBrightnessTimeOut > 0 && HwBrightnessXmlLoader.mData.cryogenicActiveScreenOffIntervalInMillis > 0 && HwBrightnessXmlLoader.mData.cryogenicLagTimeInMillis > 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementLandscapeModeGroup extends HwXmlElement {
        private ElementLandscapeModeGroup() {
        }

        public String getName() {
            return "OrientationStateGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("LandScapeBrightnessModeEnable", "LandScapeModeBrightenDebounceTime", "LandScapeModeDarkenDebounceTime", "LandScapeModeEnterDelayTime", "LandScapeModeQuitDelayTime", "LandScapeModeUseTouchProximity");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1717137325:
                    if (valueName.equals("LandScapeModeDarkenDebounceTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1348496042:
                    if (valueName.equals("LandScapeModeEnterDelayTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1243121321:
                    if (valueName.equals("LandScapeModeBrightenDebounceTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 169027410:
                    if (valueName.equals("LandScapeBrightnessModeEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 294528835:
                    if (valueName.equals("LandScapeModeQuitDelayTime")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1531419209:
                    if (valueName.equals("LandScapeModeUseTouchProximity")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.landscapeBrightnessModeEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.landscapeModeBrightenDebounceTime = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.landscapeModeDarkenDebounceTime = string2Int(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.landscapeModeEnterDelayTime = string2Int(parser.nextText());
            } else if (c == 4) {
                HwBrightnessXmlLoader.mData.landscapeModeQuitDelayTime = string2Int(parser.nextText());
            } else if (c != 5) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.landscapeModeUseTouchProximity = string2Boolean(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.landscapeModeBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.landscapeModeDarkenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.landscapeModeEnterDelayTime >= 0 && HwBrightnessXmlLoader.mData.landscapeModeQuitDelayTime >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBrightenlinePointsForLandscapeMode extends HwXmlElement {
        private ElementBrightenlinePointsForLandscapeMode() {
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

    /* access modifiers changed from: private */
    public static class ElementBrightenlinePointsForLandscapeModePoint extends HwXmlElement {
        private ElementBrightenlinePointsForLandscapeModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightenlinePointsForLandscapeMode.clear();
            }
            HwBrightnessXmlLoader.mData.brightenlinePointsForLandscapeMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightenlinePointsForLandscapeMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.brightenlinePointsForLandscapeMode, "brightenlinePointsForLandscapeMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenlinePointsForLandscapeMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDarkenlinePointsForLandscapeMode extends HwXmlElement {
        private ElementDarkenlinePointsForLandscapeMode() {
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

    /* access modifiers changed from: private */
    public static class ElementDarkenlinePointsForLandscapeModePoint extends HwXmlElement {
        private ElementDarkenlinePointsForLandscapeModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.darkenlinePointsForLandscapeMode.clear();
            }
            HwBrightnessXmlLoader.mData.darkenlinePointsForLandscapeMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.darkenlinePointsForLandscapeMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.darkenlinePointsForLandscapeMode, "darkenlinePointsForLandscapeMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenlinePointsForLandscapeMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_BrightnessLevelToNitLinePoints extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_BrightnessLevelToNitLinePoints_Point extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_SensorGroup extends HwXmlElement {
        private Element_SensorGroup() {
        }

        public String getName() {
            return "SensorGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("ResetAmbientLuxEnable", "ResetAmbientLuxTh", "ResetAmbientLuxThMin", "ResetAmbientLuxDarkenDebounceTime", "ResetAmbientLuxBrightenDebounceTime", "ResetAmbientLuxFastDarkenValidTime", "ResetAmbientLuxDarkenRatio", "ResetAmbientLuxGraTime", "ResetAmbientLuxFastDarkenDimmingTime", "ResetAmbientLuxStartBrightness", "ResetAmbientLuxDisableBrightnessOffset");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1859381766:
                    if (valueName.equals("ResetAmbientLuxTh")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1730863987:
                    if (valueName.equals("ResetAmbientLuxStartBrightness")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -1428569114:
                    if (valueName.equals("ResetAmbientLuxDarkenRatio")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -648897032:
                    if (valueName.equals("ResetAmbientLuxThMin")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -646228247:
                    if (valueName.equals("ResetAmbientLuxFastDarkenDimmingTime")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case -110496184:
                    if (valueName.equals("ResetAmbientLuxFastDarkenValidTime")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 95086715:
                    if (valueName.equals("ResetAmbientLuxDarkenDebounceTime")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 335797126:
                    if (valueName.equals("ResetAmbientLuxDisableBrightnessOffset")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 831699197:
                    if (valueName.equals("ResetAmbientLuxGraTime")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 842426239:
                    if (valueName.equals("ResetAmbientLuxBrightenDebounceTime")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 935721481:
                    if (valueName.equals("ResetAmbientLuxEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
                    HwBrightnessXmlLoader.mData.resetAmbientLuxFastDarkenDimmingTime = string2Float(parser.nextText());
                    break;
                case '\t':
                    HwBrightnessXmlLoader.mData.resetAmbientLuxStartBrightness = string2Float(parser.nextText());
                    break;
                case '\n':
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
            return HwBrightnessXmlLoader.mData.resetAmbientLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.resetAmbientLuxThMin >= 0.0f && HwBrightnessXmlLoader.mData.resetAmbientLuxDarkenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.resetAmbientLuxBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.resetAmbientLuxFastDarkenValidTime >= 0 && HwBrightnessXmlLoader.mData.resetAmbientLuxDarkenRatio > 0.0f && HwBrightnessXmlLoader.mData.resetAmbientLuxFastDarkenDimmingTime >= 0.0f && HwBrightnessXmlLoader.mData.resetAmbientLuxStartBrightness > 0.0f && HwBrightnessXmlLoader.mData.resetAmbientLuxDisableBrightnessOffset > 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_BrightnessChangeGroup extends HwXmlElement {
        private Element_BrightnessChangeGroup() {
        }

        public String getName() {
            return "BrightnessChangeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("BrightnessChageUpDelayTime", "BrightnessChageDownDelayTime", "BrightnessChageDefaultDelayTime", "LuxlinePointsForBrightnessLevelEnable", "ResetAmbientLuxThMax", "ResetAmbientLuxStartBrightnessMax");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -648897270:
                    if (valueName.equals("ResetAmbientLuxThMax")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -367820212:
                    if (valueName.equals("BrightnessChageUpDelayTime")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -342481645:
                    if (valueName.equals("LuxlinePointsForBrightnessLevelEnable")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -249351579:
                    if (valueName.equals("BrightnessChageDownDelayTime")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -55027624:
                    if (valueName.equals("BrightnessChageDefaultDelayTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1208396183:
                    if (valueName.equals("ResetAmbientLuxStartBrightnessMax")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.brightnessChageUpDelayTime = string2Int(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.brightnessChageDownDelayTime = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.brightnessChageDefaultDelayTime = string2Int(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.luxlinePointsForBrightnessLevelEnable = string2Boolean(parser.nextText());
            } else if (c == 4) {
                HwBrightnessXmlLoader.mData.resetAmbientLuxThMax = string2Float(parser.nextText());
            } else if (c != 5) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.resetAmbientLuxStartBrightnessMax = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brightnessChageUpDelayTime >= 0 && HwBrightnessXmlLoader.mData.brightnessChageDownDelayTime >= 0 && HwBrightnessXmlLoader.mData.brightnessChageDefaultDelayTime >= 0 && HwBrightnessXmlLoader.mData.resetAmbientLuxThMax >= 0.0f && HwBrightnessXmlLoader.mData.resetAmbientLuxStartBrightnessMax > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_BrightenlinePointsForBrightnessLevel extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_BrightenlinePointsForBrightnessLevel_Point extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_DarkenlinePointsForBrightnessLevel extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_DarkenlinePointsForBrightnessLevel_Point extends HwXmlElement {
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

    /* access modifiers changed from: private */
    public static class Element_SecondDarkenModeGroup extends HwXmlElement {
        private Element_SecondDarkenModeGroup() {
        }

        public String getName() {
            return "SecondDarkenMode";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("SecondDarkenModeEanble", "SecondDarkenModeMinLuxTh", "SecondDarkenModeMaxLuxTh", "SecondDarkenModeDarkenDeltaLuxRatio", "SecondDarkenModeDarkenDebounceTime", "SecondDarkenModeNoResponseDarkenTime", "SecondDarkenModeNoResponseDarkenTimeMin", "SecondDarkenModeAfterNoResponseCheckTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1674305899:
                    if (valueName.equals("SecondDarkenModeMaxLuxTh")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 240634943:
                    if (valueName.equals("SecondDarkenModeDarkenDeltaLuxRatio")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 416043051:
                    if (valueName.equals("SecondDarkenModeDarkenDebounceTime")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 572232324:
                    if (valueName.equals("SecondDarkenModeNoResponseDarkenTime")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 648043822:
                    if (valueName.equals("SecondDarkenModeNoResponseDarkenTimeMin")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 844464743:
                    if (valueName.equals("SecondDarkenModeMinLuxTh")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case 1087381759:
                    if (valueName.equals("SecondDarkenModeEanble")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1677277229:
                    if (valueName.equals("SecondDarkenModeAfterNoResponseCheckTime")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.secondDarkenModeEnable = string2Boolean(parser.nextText());
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
                case 5:
                    HwBrightnessXmlLoader.mData.secondDarkenModeNoResponseDarkenTime = string2Long(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.secondDarkenModeNoResponseDarkenTimeMin = string2Long(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.secondDarkenModeAfterNoResponseCheckTime = (long) string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.secondDarkenModeMinLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.secondDarkenModeMaxLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.secondDarkenModeDarkenDeltaLuxRatio >= 0.0f && HwBrightnessXmlLoader.mData.secondDarkenModeDarkenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.secondDarkenModeAfterNoResponseCheckTime >= 0 && HwBrightnessXmlLoader.mData.secondDarkenModeNoResponseDarkenTime >= 0 && HwBrightnessXmlLoader.mData.secondDarkenModeNoResponseDarkenTimeMin >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_BrightnessOffsetLuxGroup1 extends HwXmlElement {
        private Element_BrightnessOffsetLuxGroup1() {
        }

        public String getName() {
            return "BrightnessOffsetLuxGroup1";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("BrightnessOffsetLuxModeEnable", "BrightenOffsetLuxTh1", "BrightenOffsetLuxTh2", "BrightenOffsetLuxTh3", "BrightenOffsetNoValidDarkenLuxTh1", "BrightenOffsetNoValidDarkenLuxTh2", "BrightenOffsetNoValidDarkenLuxTh3", "BrightenOffsetNoValidDarkenLuxTh4", "BrightenOffsetNoValidBrightenLuxTh1", "BrightenOffsetNoValidBrightenLuxTh2", "BrightenOffsetNoValidBrightenLuxTh3", "BrightenOffsetNoValidBrightenLuxTh4");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
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
                            c = '\b';
                            break;
                        }
                        break;
                    case -358379977:
                        if (valueName.equals("BrightenOffsetNoValidBrightenLuxTh2")) {
                            c = '\t';
                            break;
                        }
                        break;
                    case -358379976:
                        if (valueName.equals("BrightenOffsetNoValidBrightenLuxTh3")) {
                            c = '\n';
                            break;
                        }
                        break;
                    case -358379975:
                        if (valueName.equals("BrightenOffsetNoValidBrightenLuxTh4")) {
                            c = 11;
                            break;
                        }
                        break;
                    default:
                        switch (hashCode) {
                            case 52244466:
                                if (valueName.equals("BrightenOffsetNoValidDarkenLuxTh1")) {
                                    c = 4;
                                    break;
                                }
                                break;
                            case 52244467:
                                if (valueName.equals("BrightenOffsetNoValidDarkenLuxTh2")) {
                                    c = 5;
                                    break;
                                }
                                break;
                            case 52244468:
                                if (valueName.equals("BrightenOffsetNoValidDarkenLuxTh3")) {
                                    c = 6;
                                    break;
                                }
                                break;
                            case 52244469:
                                if (valueName.equals("BrightenOffsetNoValidDarkenLuxTh4")) {
                                    c = 7;
                                    break;
                                }
                                break;
                            default:
                                switch (hashCode) {
                                    case 1958157572:
                                        if (valueName.equals("BrightenOffsetLuxTh1")) {
                                            c = 1;
                                            break;
                                        }
                                        break;
                                    case 1958157573:
                                        if (valueName.equals("BrightenOffsetLuxTh2")) {
                                            c = 2;
                                            break;
                                        }
                                        break;
                                    case 1958157574:
                                        if (valueName.equals("BrightenOffsetLuxTh3")) {
                                            c = 3;
                                            break;
                                        }
                                        break;
                                }
                        }
                }
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
                    case '\b':
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidBrightenLuxTh1 = string2Float(parser.nextText());
                        break;
                    case '\t':
                        HwBrightnessXmlLoader.mData.brightenOffsetNoValidBrightenLuxTh2 = string2Float(parser.nextText());
                        break;
                    case '\n':
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
            } else if (valueName.equals("BrightnessOffsetLuxModeEnable")) {
                c = 0;
                switch (c) {
                }
                return true;
            }
            c = 65535;
            switch (c) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.brightenOffsetLuxTh1 >= 0.0f && HwBrightnessXmlLoader.mData.brightenOffsetLuxTh2 >= 0.0f && HwBrightnessXmlLoader.mData.brightenOffsetLuxTh3 >= 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_BrightnessOffsetLuxGroup2 extends HwXmlElement {
        private Element_BrightnessOffsetLuxGroup2() {
        }

        public String getName() {
            return "BrightnessOffsetLuxGroup2";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("DarkenOffsetLuxTh1", "DarkenOffsetLuxTh2", "DarkenOffsetLuxTh3", "DarkenOffsetNoValidBrightenLuxTh1", "DarkenOffsetNoValidBrightenLuxTh2", "DarkenOffsetNoValidBrightenLuxTh3", "DarkenOffsetNoValidBrightenLuxTh4", "BrightenOffsetEffectMinLuxEnable", "BrightnessOffsetTmpValidEnable", "BrightenOffsetNoValidSavedLuxTh1", "BrightenOffsetNoValidSavedLuxTh2");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -2130092928:
                    if (valueName.equals("DarkenOffsetLuxTh1")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -2130092927:
                    if (valueName.equals("DarkenOffsetLuxTh2")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -2130092926:
                    if (valueName.equals("DarkenOffsetLuxTh3")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1739397318:
                    if (valueName.equals("DarkenOffsetNoValidBrightenLuxTh1")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1739397317:
                    if (valueName.equals("DarkenOffsetNoValidBrightenLuxTh2")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -1739397316:
                    if (valueName.equals("DarkenOffsetNoValidBrightenLuxTh3")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1739397315:
                    if (valueName.equals("DarkenOffsetNoValidBrightenLuxTh4")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -801130809:
                    if (valueName.equals("BrightenOffsetEffectMinLuxEnable")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -48883792:
                    if (valueName.equals("BrightenOffsetNoValidSavedLuxTh1")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -48883791:
                    if (valueName.equals("BrightenOffsetNoValidSavedLuxTh2")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 1324590220:
                    if (valueName.equals("BrightnessOffsetTmpValidEnable")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
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
                case '\b':
                    HwBrightnessXmlLoader.mData.brightnessOffsetTmpValidEnable = string2Boolean(parser.nextText());
                    break;
                case '\t':
                    HwBrightnessXmlLoader.mData.brightenOffsetNoValidSavedLuxTh1 = string2Float(parser.nextText());
                    break;
                case '\n':
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
            return HwBrightnessXmlLoader.mData.darkenOffsetLuxTh1 >= 0.0f && HwBrightnessXmlLoader.mData.darkenOffsetLuxTh2 >= 0.0f && HwBrightnessXmlLoader.mData.darkenOffsetLuxTh3 >= 0.0f && HwBrightnessXmlLoader.mData.brightenOffsetNoValidSavedLuxTh1 >= 0.0f && HwBrightnessXmlLoader.mData.brightenOffsetNoValidSavedLuxTh2 >= 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementTwoPoitOffsetGroup1 extends HwXmlElement {
        private ElementTwoPoitOffsetGroup1() {
        }

        public String getName() {
            return "TwoPoitOffsetGroup1";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("TwoPointOffsetModeEnable", "TwoPointOffsetLuxTh", "TwoPointOffsetAdjionLuxTh", "TwoPointOffsetNoValidLuxTh");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1807063892:
                    if (valueName.equals("TwoPointOffsetLuxTh")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -17236771:
                    if (valueName.equals("TwoPointOffsetModeEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 739002475:
                    if (valueName.equals("TwoPointOffsetAdjionLuxTh")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1308921599:
                    if (valueName.equals("TwoPointOffsetNoValidLuxTh")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.twoPointOffsetModeEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.twoPointOffsetLuxTh = string2Float(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.twoPointOffsetAdjionLuxTh = string2Float(parser.nextText());
            } else if (c != 3) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.twoPointOffsetNoValidLuxTh = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.twoPointOffsetLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.twoPointOffsetAdjionLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.twoPointOffsetNoValidLuxTh >= 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementTwoPoitOffsetGroup2 extends HwXmlElement {
        private ElementTwoPoitOffsetGroup2() {
        }

        public String getName() {
            return "TwoPoitOffsetGroup2";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("LowBrightenOffsetNoValidBrightenLuxTh", "LowDarkenOffsetNoValidBrightenLuxTh", "LowBrightenOffsetNoValidDarkenLuxTh", "LowDarkenOffsetNoValidDarkenLuxTh", "LowDarkenOffsetDarkenBrightnessRatio");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -685977137:
                    if (valueName.equals("LowDarkenOffsetNoValidDarkenLuxTh")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 600896469:
                    if (valueName.equals("LowDarkenOffsetDarkenBrightnessRatio")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1297972111:
                    if (valueName.equals("LowBrightenOffsetNoValidBrightenLuxTh")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1458876243:
                    if (valueName.equals("LowBrightenOffsetNoValidDarkenLuxTh")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1678176011:
                    if (valueName.equals("LowDarkenOffsetNoValidBrightenLuxTh")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.lowBrightenOffsetNoValidBrightenLuxTh = string2Float(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.lowDarkenOffsetNoValidBrightenLuxTh = string2Float(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.lowBrightenOffsetNoValidDarkenLuxTh = string2Float(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.lowDarkenOffsetNoValidDarkenLuxTh = string2Float(parser.nextText());
            } else if (c != 4) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.lowDarkenOffsetDarkenBrightnessRatio = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.lowBrightenOffsetNoValidBrightenLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.lowDarkenOffsetNoValidBrightenLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.lowBrightenOffsetNoValidDarkenLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.lowDarkenOffsetNoValidDarkenLuxTh >= 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementTwoPoitOffsetGroup3 extends HwXmlElement {
        private ElementTwoPoitOffsetGroup3() {
        }

        public String getName() {
            return "TwoPoitOffsetGroup3";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("HighBrightenOffsetNoValidBrightenLuxTh", "HighDarkenOffsetNoValidBrightenLuxTh", "HighBrightenOffsetNoValidDarkenLuxTh", "HighDarkenOffsetNoValidDarkenLuxTh");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -833188899:
                    if (valueName.equals("HighDarkenOffsetNoValidDarkenLuxTh")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1039105693:
                    if (valueName.equals("HighBrightenOffsetNoValidBrightenLuxTh")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1722293729:
                    if (valueName.equals("HighBrightenOffsetNoValidDarkenLuxTh")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1941593497:
                    if (valueName.equals("HighDarkenOffsetNoValidBrightenLuxTh")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.highBrightenOffsetNoValidBrightenLuxTh = string2Float(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.highDarkenOffsetNoValidBrightenLuxTh = string2Float(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.highBrightenOffsetNoValidDarkenLuxTh = string2Float(parser.nextText());
            } else if (c != 3) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.highDarkenOffsetNoValidDarkenLuxTh = string2Float(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.highBrightenOffsetNoValidBrightenLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.highDarkenOffsetNoValidBrightenLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.highBrightenOffsetNoValidDarkenLuxTh >= 0.0f && HwBrightnessXmlLoader.mData.highDarkenOffsetNoValidDarkenLuxTh >= 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementVideoGroup extends HwXmlElement {
        private ElementVideoGroup() {
        }

        public String getName() {
            return "VideoGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("VideoFullScreenModeEnable", "VideoFullScreenModeDelayTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:12:0x002c  */
        /* JADX WARNING: Removed duplicated region for block: B:16:0x0055  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -635154372) {
                if (hashCode == 1904653719 && valueName.equals("VideoFullScreenModeDelayTime")) {
                    c = 1;
                    if (c == 0) {
                        HwBrightnessXmlLoader.mData.videoFullScreenModeEnable = string2Boolean(parser.nextText());
                    } else if (c != 1) {
                        Slog.e(this.TAG, "unknow valueName=" + valueName);
                        return false;
                    } else {
                        HwBrightnessXmlLoader.mData.videoFullScreenModeDelayTime = string2Int(parser.nextText());
                    }
                    return true;
                }
            } else if (valueName.equals("VideoFullScreenModeEnable")) {
                c = 0;
                if (c == 0) {
                }
                return true;
            }
            c = 65535;
            if (c == 0) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.videoFullScreenModeDelayTime >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBrightnessLineForVideoFullScreenMode extends HwXmlElement {
        private ElementBrightnessLineForVideoFullScreenMode() {
        }

        public String getName() {
            return "BrightnessLineForVideoFullScreenMode";
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

    /* access modifiers changed from: private */
    public static class ElementBrightnessLineForVideoFullScreenModePoint extends HwXmlElement {
        private ElementBrightnessLineForVideoFullScreenModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightnessLineForVideoFullScreenMode.clear();
            }
            HwBrightnessXmlLoader.mData.brightnessLineForVideoFullScreenMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightnessLineForVideoFullScreenMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.brightnessLineForVideoFullScreenMode, "brightnessLineForVideoFullScreenMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightnessLineForVideoFullScreenMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class Element_KeyguardUnlockedGroup extends HwXmlElement {
        private Element_KeyguardUnlockedGroup() {
        }

        public String getName() {
            return "KeyguardUnlockedGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("KeyguardUnlockedFastDarkenEnable", "KeyguardUnlockedFastDarkenMaxLux", "KeyguardUnlockedDarkenDebounceTime", "KeyguardUnlockedDarkenRatio", "KeyguardUnlockedFastDarkenValidTime", "KeyguardUnlockedLuxDeltaValidTime", "KeyguardBrightenLuxDeltaMin", "KeyguardUnlockedDimmingTime", "KeyguardFastDimBrightness", "KeyguardFastDimTime");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1802359328:
                    if (valueName.equals("KeyguardBrightenLuxDeltaMin")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case -1288070754:
                    if (valueName.equals("KeyguardUnlockedDarkenDebounceTime")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -985316159:
                    if (valueName.equals("KeyguardUnlockedDimmingTime")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -38704763:
                    if (valueName.equals("KeyguardUnlockedFastDarkenValidTime")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -10466089:
                    if (valueName.equals("KeyguardFastDimBrightness")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1210109907:
                    if (valueName.equals("KeyguardFastDimTime")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 1212813367:
                    if (valueName.equals("KeyguardUnlockedLuxDeltaValidTime")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1607653991:
                    if (valueName.equals("KeyguardUnlockedFastDarkenEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1674757155:
                    if (valueName.equals("KeyguardUnlockedDarkenRatio")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1825345775:
                    if (valueName.equals("KeyguardUnlockedFastDarkenMaxLux")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.keyguardUnlockedFastDarkenEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.keyguardUnlockedFastDarkenMaxLux = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.keyguardUnlockedDarkenDebounceTime = string2Long(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.keyguardUnlockedDarkenRatio = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.keyguardUnlockedFastDarkenValidTime = string2Long(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.keyguardUnlockedLuxDeltaValidTime = string2Long(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.keyguardBrightenLuxDeltaMin = string2Float(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.keyguardUnlockedDimmingTime = string2Float(parser.nextText());
                    break;
                case '\b':
                    HwBrightnessXmlLoader.mData.keyguardFastDimBrightness = string2Int(parser.nextText());
                    break;
                case '\t':
                    HwBrightnessXmlLoader.mData.keyguardFastDimTime = string2Float(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.keyguardUnlockedDarkenRatio > 0.0f && HwBrightnessXmlLoader.mData.keyguardUnlockedDimmingTime > 0.0f && HwBrightnessXmlLoader.mData.keyguardFastDimBrightness >= 0 && HwBrightnessXmlLoader.mData.keyguardFastDimTime > 0.0f;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementFoldScreenGroup extends HwXmlElement {
        private ElementFoldScreenGroup() {
        }

        public String getName() {
            return "FoldScreenGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("FoldScreenModeEnable", "IsInwardFoldScreenLuxEnable");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                Slog.e(this.TAG, "parser FoldScreenGroup is null");
                return false;
            }
            String valueName = parser.getName();
            char c = 65535;
            int hashCode = valueName.hashCode();
            if (hashCode != -2030332902) {
                if (hashCode == 964996307 && valueName.equals("FoldScreenModeEnable")) {
                    c = 0;
                }
            } else if (valueName.equals("IsInwardFoldScreenLuxEnable")) {
                c = 1;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.foldScreenModeEnable = string2Boolean(parser.nextText());
            } else if (c != 1) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.isInwardFoldScreenLuxEnable = string2Boolean(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDarkModeGroup extends HwXmlElement {
        private ElementDarkModeGroup() {
        }

        public String getName() {
            return "DarkModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("DarkModeEnable", "DarkModeOffsetMinBrightness", "IsDarkModeCurveEnable");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0076  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != -1652376745) {
                if (hashCode != -1177976516) {
                    if (hashCode == 6537647 && valueName.equals("IsDarkModeCurveEnable")) {
                        c = 2;
                        if (c != 0) {
                            HwBrightnessXmlLoader.mData.darkModeEnable = string2Boolean(parser.nextText());
                        } else if (c == 1) {
                            HwBrightnessXmlLoader.mData.darkModeOffsetMinBrightness = string2Int(parser.nextText());
                        } else if (c != 2) {
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                        } else {
                            HwBrightnessXmlLoader.mData.isDarkModeCurveEnable = string2Boolean(parser.nextText());
                        }
                        return true;
                    }
                } else if (valueName.equals("DarkModeEnable")) {
                    c = 0;
                    if (c != 0) {
                    }
                    return true;
                }
            } else if (valueName.equals("DarkModeOffsetMinBrightness")) {
                c = 1;
                if (c != 0) {
                }
                return true;
            }
            c = 65535;
            if (c != 0) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.darkModeOffsetMinBrightness > 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBrightnessLineForDarkMode extends HwXmlElement {
        private ElementBrightnessLineForDarkMode() {
        }

        public String getName() {
            return "BrightnessLineForDarkMode";
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

    /* access modifiers changed from: private */
    public static class ElementBrightnessLineForDarkModePoint extends HwXmlElement {
        private ElementBrightnessLineForDarkModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightnessLineForDarkMode.clear();
            }
            HwBrightnessXmlLoader.mData.brightnessLineForDarkMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightnessLineForDarkMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.brightnessLineForDarkMode, "brightnessLineForDarkMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightnessLineForDarkMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBatteryModeGroup extends HwXmlElement {
        private ElementBatteryModeGroup() {
        }

        public String getName() {
            return "BatteryModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("BatteryModeEnable", "BatteryLowLevelTh", "BatteryLowLevelMaxBrightness", "PowerSavingModeBatteryLowLevelEnable", "PowerSavingModeBatteryLowLevelThreshold", "PowerSavingModeBatteryLowLevelBrightnessRatio");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1232984002:
                    if (valueName.equals("PowerSavingModeBatteryLowLevelEnable")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1150779151:
                    if (valueName.equals("BatteryLowLevelTh")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -311844141:
                    if (valueName.equals("BatteryModeEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 1371229752:
                    if (valueName.equals("BatteryLowLevelMaxBrightness")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 1845370335:
                    if (valueName.equals("PowerSavingModeBatteryLowLevelBrightnessRatio")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 2115150800:
                    if (valueName.equals("PowerSavingModeBatteryLowLevelThreshold")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.batteryModeEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.batteryLowLevelTh = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.batteryLowLevelMaxBrightness = string2Int(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.powerSavingModeBatteryLowLevelEnable = string2Boolean(parser.nextText());
                if (HwBrightnessXmlLoader.IS_DEFAULT_POWER_SAVING_RATIO) {
                    HwBrightnessXmlLoader.mData.powerSavingModeBatteryLowLevelEnable = false;
                    Slog.i(this.TAG, "IS_DEFAULT_POWER_SAVING_RATIO set powerSavingModeBatteryLowLevelEnable=" + HwBrightnessXmlLoader.mData.powerSavingModeBatteryLowLevelEnable);
                }
            } else if (c == 4) {
                HwBrightnessXmlLoader.mData.powerSavingModeBatteryLowLevelThreshold = string2Int(parser.nextText());
            } else if (c != 5) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.powerSavingModeBatteryLowLevelBrightnessRatio = string2Int(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.batteryLowLevelTh >= 0 && HwBrightnessXmlLoader.mData.batteryLowLevelMaxBrightness > 0 && HwBrightnessXmlLoader.mData.powerSavingModeBatteryLowLevelThreshold >= 0 && HwBrightnessXmlLoader.mData.powerSavingModeBatteryLowLevelBrightnessRatio > 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementLuxMinMaxBrightnessGroup extends HwXmlElement {
        private ElementLuxMinMaxBrightnessGroup() {
        }

        public String getName() {
            return "LuxMinMaxBrightnessGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("LuxMinMaxBrightnessEnable");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            String valueName = parser.getName();
            if (((valueName.hashCode() == 1071338965 && valueName.equals("LuxMinMaxBrightnessEnable")) ? (char) 0 : 65535) != 0) {
                String str = this.TAG;
                Slog.e(str, "unknow valueName=" + valueName);
                return false;
            }
            HwBrightnessXmlLoader.mData.luxMinMaxBrightnessEnable = string2Boolean(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementLuxMinMaxBrightnessPoints extends HwXmlElement {
        private ElementLuxMinMaxBrightnessPoints() {
        }

        public String getName() {
            return "LuxMinMaxBrightnessPoints";
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

    /* access modifiers changed from: private */
    public static class ElementLuxMinMaxBrightnessPointsPoint extends HwXmlElement {
        private ElementLuxMinMaxBrightnessPointsPoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.luxMinMaxBrightnessPoints.clear();
            }
            HwBrightnessXmlLoader.mData.luxMinMaxBrightnessPoints = parseAmPointList(parser, HwBrightnessXmlLoader.mData.luxMinMaxBrightnessPoints);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxAmPointValid(HwBrightnessXmlLoader.mData.luxMinMaxBrightnessPoints, "luxMinMaxBrightnessPoints")) {
                return HwBrightnessXmlLoader.checkAmPointsListIsOK(HwBrightnessXmlLoader.mData.luxMinMaxBrightnessPoints);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementCameraLimitBrightnessModeGroup extends HwXmlElement {
        private ElementCameraLimitBrightnessModeGroup() {
        }

        public String getName() {
            return "CameraLimitBrightnessModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("FrontCameraMaxBrightnessEnable", "FrontCameraLuxThreshold", "FrontCameraBrightenLuxThreshold", "FrontCameraDarkenLuxThreshold", "FrontCameraBrighenDebounceTime", "FrontCameraDarkenDebounceTime", "FrontCameraDimmingBrightenTime", "FrontCameraDimmingDarkenTime", "FrontCameraUpdateBrightnessDelayTime", "FrontCameraUpdateDimmingEnableTime", "FrontCameraMaxBrightness");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            switch (valueName.hashCode()) {
                case -1859527221:
                    if (valueName.equals("FrontCameraBrighenDebounceTime")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -925719862:
                    if (valueName.equals("FrontCameraLuxThreshold")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -854688631:
                    if (valueName.equals("FrontCameraDarkenLuxThreshold")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -523895946:
                    if (valueName.equals("FrontCameraUpdateDimmingEnableTime")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -350330405:
                    if (valueName.equals("FrontCameraDimmingDarkenTime")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -194292505:
                    if (valueName.equals("FrontCameraMaxBrightness")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 460961439:
                    if (valueName.equals("FrontCameraDimmingBrightenTime")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1421628451:
                    if (valueName.equals("FrontCameraDarkenDebounceTime")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 1574219560:
                    if (valueName.equals("FrontCameraUpdateBrightnessDelayTime")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 2062397837:
                    if (valueName.equals("FrontCameraBrightenLuxThreshold")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 2069224778:
                    if (valueName.equals("FrontCameraMaxBrightnessEnable")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    HwBrightnessXmlLoader.mData.frontCameraMaxBrightnessEnable = string2Boolean(parser.nextText());
                    break;
                case 1:
                    HwBrightnessXmlLoader.mData.frontCameraLuxThreshold = string2Float(parser.nextText());
                    break;
                case 2:
                    HwBrightnessXmlLoader.mData.frontCameraBrightenLuxThreshold = string2Float(parser.nextText());
                    break;
                case 3:
                    HwBrightnessXmlLoader.mData.frontCameraDarkenLuxThreshold = string2Float(parser.nextText());
                    break;
                case 4:
                    HwBrightnessXmlLoader.mData.frontCameraBrighenDebounceTime = string2Int(parser.nextText());
                    break;
                case 5:
                    HwBrightnessXmlLoader.mData.frontCameraDarkenDebounceTime = string2Int(parser.nextText());
                    break;
                case 6:
                    HwBrightnessXmlLoader.mData.frontCameraDimmingBrightenTime = string2Float(parser.nextText());
                    break;
                case 7:
                    HwBrightnessXmlLoader.mData.frontCameraDimmingDarkenTime = string2Float(parser.nextText());
                    break;
                case '\b':
                    HwBrightnessXmlLoader.mData.frontCameraUpdateBrightnessDelayTime = string2Int(parser.nextText());
                    break;
                case '\t':
                    HwBrightnessXmlLoader.mData.frontCameraUpdateDimmingEnableTime = string2Int(parser.nextText());
                    break;
                case '\n':
                    HwBrightnessXmlLoader.mData.frontCameraMaxBrightness = string2Int(parser.nextText());
                    break;
                default:
                    Slog.e(this.TAG, "unknow valueName=" + valueName);
                    return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.frontCameraBrighenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.frontCameraDarkenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.frontCameraLuxThreshold <= HwBrightnessXmlLoader.mData.frontCameraBrightenLuxThreshold && HwBrightnessXmlLoader.mData.frontCameraLuxThreshold >= HwBrightnessXmlLoader.mData.frontCameraDarkenLuxThreshold && HwBrightnessXmlLoader.mData.frontCameraDimmingBrightenTime > 0.0f && HwBrightnessXmlLoader.mData.frontCameraDimmingDarkenTime > 0.0f && HwBrightnessXmlLoader.mData.frontCameraUpdateBrightnessDelayTime >= 0 && HwBrightnessXmlLoader.mData.frontCameraUpdateDimmingEnableTime >= 0 && HwBrightnessXmlLoader.mData.frontCameraMaxBrightness > 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBrightnessMappingForWindowBrightnessGroup extends HwXmlElement {
        private ElementBrightnessMappingForWindowBrightnessGroup() {
        }

        public String getName() {
            return "BrightnessMappingForWindowBrightnessGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("BrightnessMappingForWindowBrightnessEnable");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            String valueName = parser.getName();
            if (((valueName.hashCode() == 707067088 && valueName.equals("BrightnessMappingForWindowBrightnessEnable")) ? (char) 0 : 65535) != 0) {
                String str = this.TAG;
                Slog.e(str, "unknow valueName=" + valueName);
                return false;
            }
            HwBrightnessXmlLoader.mData.brightnessMappingForWindowBrightnessEnable = string2Boolean(parser.nextText());
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBrightnessMappingPointsForWindowBrightness extends HwXmlElement {
        private ElementBrightnessMappingPointsForWindowBrightness() {
        }

        public String getName() {
            return "BrightnessMappingPointsForWindowBrightness";
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

    /* access modifiers changed from: private */
    public static class ElementBrightnessMappingPointsForWindowBrightnessPoint extends HwXmlElement {
        private ElementBrightnessMappingPointsForWindowBrightnessPoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightnessMappingPointsForWindowBrightness.clear();
            }
            HwBrightnessXmlLoader.mData.brightnessMappingPointsForWindowBrightness = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightnessMappingPointsForWindowBrightness);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightnessMappingPointsForWindowBrightness);
        }
    }

    /* access modifiers changed from: private */
    public static class ElementHdrModeGroup extends HwXmlElement {
        private ElementHdrModeGroup() {
        }

        public String getName() {
            return "HdrModeGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("HdrModeEnable", "HdrModeCurveEnable", "HdrModeWindowMappingEnable");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:23:0x0076  */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            char c;
            String valueName = parser.getName();
            int hashCode = valueName.hashCode();
            if (hashCode != 379463804) {
                if (hashCode != 1229214713) {
                    if (hashCode == 1987537800 && valueName.equals("HdrModeWindowMappingEnable")) {
                        c = 2;
                        if (c != 0) {
                            HwBrightnessXmlLoader.mData.hdrModeEnable = string2Boolean(parser.nextText());
                        } else if (c == 1) {
                            HwBrightnessXmlLoader.mData.hdrModeCurveEnable = string2Boolean(parser.nextText());
                        } else if (c != 2) {
                            Slog.e(this.TAG, "unknow valueName=" + valueName);
                            return false;
                        } else {
                            HwBrightnessXmlLoader.mData.hdrModeWindowMappingEnable = string2Boolean(parser.nextText());
                        }
                        return true;
                    }
                } else if (valueName.equals("HdrModeCurveEnable")) {
                    c = 1;
                    if (c != 0) {
                    }
                    return true;
                }
            } else if (valueName.equals("HdrModeEnable")) {
                c = 0;
                if (c != 0) {
                }
                return true;
            }
            c = 65535;
            if (c != 0) {
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBrightnessMappingPointsForHdrMode extends HwXmlElement {
        private ElementBrightnessMappingPointsForHdrMode() {
        }

        public String getName() {
            return "BrightnessMappingPointsForHdrMode";
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

    /* access modifiers changed from: private */
    public static class ElementBrightnessMappingPointsForHdrModePoint extends HwXmlElement {
        private ElementBrightnessMappingPointsForHdrModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightnessMappingPointsForHdrMode.clear();
            }
            HwBrightnessXmlLoader.mData.brightnessMappingPointsForHdrMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightnessMappingPointsForHdrMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightnessMappingPointsForHdrMode);
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBrightnessLineForHdrMode extends HwXmlElement {
        private ElementBrightnessLineForHdrMode() {
        }

        public String getName() {
            return "BrightnessLineForHdrMode";
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

    /* access modifiers changed from: private */
    public static class ElementBrightnessLineForHdrModePoint extends HwXmlElement {
        private ElementBrightnessLineForHdrModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightnessLineForHdrMode.clear();
            }
            HwBrightnessXmlLoader.mData.brightnessLineForHdrMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightnessLineForHdrMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.brightnessLineForHdrMode, "brightnessLineForHdrMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightnessLineForHdrMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementAmbientLuxGroup extends HwXmlElement {
        private ElementAmbientLuxGroup() {
        }

        public String getName() {
            return "AmbientLuxGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("MaxValidAmbientLux");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                Slog.e(this.TAG, "parser AmbientLuxGroup is null");
                return false;
            }
            String valueName = parser.getName();
            char c = 65535;
            if (valueName.hashCode() == 1927187855 && valueName.equals("MaxValidAmbientLux")) {
                c = 0;
            }
            if (c != 0) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            }
            HwBrightnessXmlLoader.mData.maxValidAmbientLux = string2Float(parser.nextText());
            float f = HwBrightnessXmlLoader.mData.maxValidAmbientLux;
            Data unused = HwBrightnessXmlLoader.mData;
            if (f > 40000.0f) {
                boolean unused2 = HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint = true;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            float f = HwBrightnessXmlLoader.mData.maxValidAmbientLux;
            Data unused = HwBrightnessXmlLoader.mData;
            if (f <= 40000.0f) {
                float f2 = HwBrightnessXmlLoader.mData.maxValidAmbientLux;
                Data unused2 = HwBrightnessXmlLoader.mData;
                if (Math.abs(f2 - 40000.0f) >= HwBrightnessXmlLoader.SMALL_VALUE) {
                    return false;
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementLandscapGameGroup extends HwXmlElement {
        private ElementLandscapGameGroup() {
        }

        public String getName() {
            return "LandscapGameGroup";
        }

        /* access modifiers changed from: protected */
        public List<String> getNameList() {
            return Arrays.asList("IsLandscapeGameModeEnable", "LandscapeGameModeEnterDelayTime", "LandscapeGameModeQuitDelayTime", "LandscapeGameModeBrightenDebounceTime", "LandscapeGameModeDarkenDebounceTime", "IsLandscapeGameModeLuxThresholdEnable");
        }

        /* access modifiers changed from: protected */
        public boolean isOptional() {
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                Slog.e(this.TAG, "parser LandscapGameGroup is null");
                return false;
            }
            String valueName = parser.getName();
            char c = 65535;
            switch (valueName.hashCode()) {
                case -1698367608:
                    if (valueName.equals("LandscapeGameModeEnterDelayTime")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1626944315:
                    if (valueName.equals("IsLandscapeGameModeLuxThresholdEnable")) {
                        c = 5;
                        break;
                    }
                    break;
                case -1575912055:
                    if (valueName.equals("IsLandscapeGameModeEnable")) {
                        c = 0;
                        break;
                    }
                    break;
                case -770995835:
                    if (valueName.equals("LandscapeGameModeDarkenDebounceTime")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1760751113:
                    if (valueName.equals("LandscapeGameModeBrightenDebounceTime")) {
                        c = 3;
                        break;
                    }
                    break;
                case 1945810641:
                    if (valueName.equals("LandscapeGameModeQuitDelayTime")) {
                        c = 2;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                HwBrightnessXmlLoader.mData.isLandscapeGameModeEnable = string2Boolean(parser.nextText());
            } else if (c == 1) {
                HwBrightnessXmlLoader.mData.landscapeGameModeEnterDelayTime = string2Int(parser.nextText());
            } else if (c == 2) {
                HwBrightnessXmlLoader.mData.landscapeGameModeQuitDelayTime = string2Int(parser.nextText());
            } else if (c == 3) {
                HwBrightnessXmlLoader.mData.landscapeGameModeBrightenDebounceTime = string2Long(parser.nextText());
            } else if (c == 4) {
                HwBrightnessXmlLoader.mData.landscapeGameModeDarkenDebounceTime = string2Long(parser.nextText());
            } else if (c != 5) {
                Slog.e(this.TAG, "unknow valueName=" + valueName);
                return false;
            } else {
                HwBrightnessXmlLoader.mData.isLandscapeGameModeLuxThresholdEnable = string2Boolean(parser.nextText());
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            return HwBrightnessXmlLoader.mData.landscapeGameModeEnterDelayTime >= 0 && HwBrightnessXmlLoader.mData.landscapeGameModeQuitDelayTime >= 0 && HwBrightnessXmlLoader.mData.landscapeGameModeBrightenDebounceTime >= 0 && HwBrightnessXmlLoader.mData.landscapeGameModeDarkenDebounceTime >= 0;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementBrightenLinePointsForLandscapeGameMode extends HwXmlElement {
        private ElementBrightenLinePointsForLandscapeGameMode() {
        }

        public String getName() {
            return "BrightenLinePointsForLandscapeGameMode";
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

    /* access modifiers changed from: private */
    public static class ElementBrightenLinePointsForLandscapeGameModePoint extends HwXmlElement {
        private ElementBrightenLinePointsForLandscapeGameModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                Slog.e(this.TAG, "parser BrightenLinePointsForLandscapeGameModePoint is null");
                return false;
            }
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.brightenLinePointsForLandscapeGameMode.clear();
            }
            HwBrightnessXmlLoader.mData.brightenLinePointsForLandscapeGameMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.brightenLinePointsForLandscapeGameMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.brightenLinePointsForLandscapeGameMode, "brightenLinePointsForLandscapeGameMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.brightenLinePointsForLandscapeGameMode);
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class ElementDarkenLinePointsForLandscapeGameMode extends HwXmlElement {
        private ElementDarkenLinePointsForLandscapeGameMode() {
        }

        public String getName() {
            return "DarkenLinePointsForLandscapeGameMode";
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

    /* access modifiers changed from: private */
    public static class ElementDarkenLinePointsForLandscapeGameModePoint extends HwXmlElement {
        private ElementDarkenLinePointsForLandscapeGameModePoint() {
        }

        public String getName() {
            return "Point";
        }

        /* access modifiers changed from: protected */
        public boolean parseValue(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser == null) {
                Slog.e(this.TAG, "parser DarkenLinePointsForLandscapeGameModePoint is null");
                return false;
            }
            if (!this.mIsParsed) {
                HwBrightnessXmlLoader.mData.darkenLinePointsForLandscapeGameMode.clear();
            }
            HwBrightnessXmlLoader.mData.darkenLinePointsForLandscapeGameMode = parsePointFList(parser, HwBrightnessXmlLoader.mData.darkenLinePointsForLandscapeGameMode);
            return true;
        }

        /* access modifiers changed from: protected */
        public boolean checkValue() {
            if (!HwBrightnessXmlLoader.sIsNeedMaxAmbientLuxPoint || HwBrightnessXmlLoader.isMaxAmbientLuxPointValid(HwBrightnessXmlLoader.mData.darkenLinePointsForLandscapeGameMode, "darkenLinePointsForLandscapeGameMode")) {
                return HwBrightnessXmlLoader.checkPointsListIsOK(HwBrightnessXmlLoader.mData.darkenLinePointsForLandscapeGameMode);
            }
            return false;
        }
    }
}
