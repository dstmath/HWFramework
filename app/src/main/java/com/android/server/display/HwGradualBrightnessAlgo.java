package com.android.server.display;

import android.animation.ValueAnimator;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.input.HwCircleAnimation;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

final class HwGradualBrightnessAlgo {
    private static final float CRITERION_TIME = 40.0f;
    private static final float DEFAULT_AMOUNT = 157.0f;
    private static final float FAST_TIME = 0.5f;
    private static final String HW_LABC_CONFIG_FILE = "LABCConfig.xml";
    private boolean DEBUG;
    private String TAG;
    private float mAnimatedStep;
    private boolean mAnimatedStepRoundEnabled;
    private float mAnimatedValue;
    public boolean mAutoBrightnessIntervened;
    public boolean mAutoBrightnessMode;
    private float mAutoFastTimeFor255;
    private float mBrightenFixStepsThreshold;
    private float mBrightenGradualTime;
    private int mBrightenThresholdFor255;
    private String mConfigFilePath;
    private boolean mCoverModeAnimationFast;
    private float mCoverModeAnimationTime;
    private int mCurrentValue;
    private int mDarkenCurrentFor255;
    private float mDarkenFixStepsThreshold;
    private float mDarkenGradualTime;
    private float mDarkenGradualTimeMax;
    private float mDarkenGradualTimeMin;
    private int mDarkenTargetFor255;
    private float mDecreaseFixAmount;
    private final int mDeviceActualBrightnessLevel;
    private float mDimTime;
    private float mDuration;
    private boolean mFirstTimeCalculateAmount;
    public boolean mFirstValidAutoBrightness;
    private float mManualFastTimeFor255;
    private boolean mPowerDimRecoveryState;
    private boolean mPowerDimState;
    private int mRate;
    private int mState;
    private float mStepAdjValue;
    private int mTargetValue;
    private boolean mUseVariableSteps;
    private boolean mfastAnimtionFlag;

    public HwGradualBrightnessAlgo(int deviceActualBrightnessLevel) {
        this.TAG = "HwGradualBrightnessAlgo";
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(this.TAG, 4) : false : true;
        this.DEBUG = isLoggable;
        this.mAutoBrightnessIntervened = false;
        this.mFirstValidAutoBrightness = false;
        this.mFirstTimeCalculateAmount = false;
        this.mConfigFilePath = null;
        this.mPowerDimState = false;
        this.mPowerDimRecoveryState = false;
        this.mAutoFastTimeFor255 = FAST_TIME;
        this.mManualFastTimeFor255 = FAST_TIME;
        this.mDimTime = CustomGestureDetector.TOUCH_TOLERANCE;
        this.mAnimatedStepRoundEnabled = false;
        this.mUseVariableSteps = false;
        this.mDarkenFixStepsThreshold = 20.0f;
        this.mBrightenFixStepsThreshold = 2.0f;
        this.mDarkenGradualTimeMax = CustomGestureDetector.TOUCH_TOLERANCE;
        this.mDarkenGradualTimeMin = 0.0f;
        this.mDuration = 0.0f;
        this.mAnimatedStep = HwCircleAnimation.SMALL_ALPHA;
        this.mStepAdjValue = HwCircleAnimation.SMALL_ALPHA;
        this.mfastAnimtionFlag = false;
        this.mCoverModeAnimationFast = false;
        this.mCoverModeAnimationTime = HwCircleAnimation.SMALL_ALPHA;
        this.mDeviceActualBrightnessLevel = deviceActualBrightnessLevel;
        try {
            if (!getConfig()) {
                Slog.e(this.TAG, "getConfig failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultConfig();
        }
    }

    private boolean getConfig() throws IOException {
        FileNotFoundException e;
        IOException e2;
        Exception e3;
        Throwable th;
        String version = SystemProperties.get("ro.build.version.emui", null);
        if (version == null || version.length() == 0) {
            Slog.e(this.TAG, "get ro.build.version.emui failed!");
            return false;
        }
        String[] versionSplited = version.split("EmotionUI_");
        if (versionSplited.length < 2) {
            Slog.e(this.TAG, "split failed! version = " + version);
            return false;
        }
        String emuiVersion = versionSplited[1];
        if (emuiVersion == null || emuiVersion.length() == 0) {
            Slog.e(this.TAG, "get emuiVersion failed!");
            return false;
        }
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", new Object[]{emuiVersion, HW_LABC_CONFIG_FILE}), 0);
        if (xmlFile == null) {
            String xmlPath = String.format("/xml/lcd/%s", new Object[]{HW_LABC_CONFIG_FILE});
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, 0);
            if (xmlFile == null) {
                Slog.e(this.TAG, "get xmlFile :" + xmlPath + " failed!");
                return false;
            }
        }
        FileInputStream fileInputStream = null;
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            try {
                if (getConfigFromXML(inputStream)) {
                    if (checkConfigLoadedFromXML()) {
                        printConfigFromXML();
                    }
                    this.mConfigFilePath = xmlFile.getAbsolutePath();
                    if (this.DEBUG) {
                        Slog.i(this.TAG, "get xmlFile :" + this.mConfigFilePath);
                    }
                    inputStream.close();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    return true;
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                fileInputStream = inputStream;
                return false;
            } catch (FileNotFoundException e4) {
                e = e4;
                fileInputStream = inputStream;
                e.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (IOException e5) {
                e2 = e5;
                fileInputStream = inputStream;
                e2.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return false;
            } catch (Exception e6) {
                e3 = e6;
                fileInputStream = inputStream;
                try {
                    e3.printStackTrace();
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = inputStream;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            e.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        } catch (IOException e8) {
            e2 = e8;
            e2.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        } catch (Exception e9) {
            e3 = e9;
            e3.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return false;
        }
    }

    private boolean checkConfigLoadedFromXML() {
        if (this.mBrightenGradualTime <= 0.0f) {
            loadDefaultConfig();
            Slog.e(this.TAG, "LoadXML false for mBrightenGradualTime <= 0, LoadDefaultConfig!");
            return false;
        } else if (this.mDarkenGradualTime <= 0.0f) {
            loadDefaultConfig();
            Slog.e(this.TAG, "LoadXML false for mDarkenGradualTime <= 0, LoadDefaultConfig!");
            return false;
        } else if (this.mDarkenGradualTimeMax <= 0.0f) {
            loadDefaultConfig();
            Slog.e(this.TAG, "LoadXML false for mDarkenGradualTimeMax <= 0, LoadDefaultConfig!");
            return false;
        } else if (this.mDarkenGradualTimeMin < 0.0f) {
            loadDefaultConfig();
            Slog.e(this.TAG, "LoadXML false for mDarkenGradualTimeMin < 0, LoadDefaultConfig!");
            return false;
        } else if (this.mDarkenGradualTimeMin > this.mDarkenGradualTimeMax) {
            loadDefaultConfig();
            Slog.e(this.TAG, "LoadXML false for mDarkenGradualTimeMin > mDarkenGradualTimeMax, LoadDefaultConfig!");
            return false;
        } else {
            if (this.DEBUG) {
                Slog.i(this.TAG, "checkConfigLoadedFromXML success!");
            }
            return true;
        }
    }

    private void loadDefaultConfig() {
        this.mBrightenGradualTime = HwCircleAnimation.SMALL_ALPHA;
        this.mDarkenGradualTime = CustomGestureDetector.TOUCH_TOLERANCE;
        this.mDarkenGradualTimeMax = CustomGestureDetector.TOUCH_TOLERANCE;
        this.mDarkenGradualTimeMin = 0.0f;
        this.mBrightenThresholdFor255 = 1254;
        this.mDarkenTargetFor255 = 1254;
        this.mDarkenCurrentFor255 = 1300;
        this.mAutoFastTimeFor255 = FAST_TIME;
        this.mManualFastTimeFor255 = FAST_TIME;
        this.mDimTime = CustomGestureDetector.TOUCH_TOLERANCE;
        if (this.DEBUG) {
            printConfigFromXML();
        }
    }

    private void printConfigFromXML() {
        Slog.i(this.TAG, "LoadXMLConfig_BrightenGradualTime=" + this.mBrightenGradualTime);
        Slog.i(this.TAG, "LoadXMLConfig_DarkenGradualTime=" + this.mDarkenGradualTime);
        Slog.i(this.TAG, "LoadXMLConfig_BrightenThresholdFor255=" + this.mBrightenThresholdFor255);
        Slog.i(this.TAG, "LoadXMLConfig_DarkenTargetFor255=" + this.mDarkenTargetFor255);
        Slog.i(this.TAG, "LoadXMLConfig_DarkenCurrentFor255=" + this.mDarkenCurrentFor255);
        Slog.i(this.TAG, "LoadXMLConfig_AutoFastTimeFor255=" + this.mAutoFastTimeFor255);
        Slog.i(this.TAG, "LoadXMLConfig_ManualFastTimeFor255=" + this.mManualFastTimeFor255);
        Slog.i(this.TAG, "LoadXMLConfig_DimTime=" + this.mDimTime);
        Slog.i(this.TAG, "LoadXMLConfig_DarkenGradualTimeMax=" + this.mDarkenGradualTimeMax);
        Slog.i(this.TAG, "LoadXMLConfig_DarkenGradualTimeMin=" + this.mDarkenGradualTimeMin);
        Slog.i(this.TAG, "LoadXMLConfig_AnimatedStepRoundEnabled=" + this.mAnimatedStepRoundEnabled);
    }

    private boolean getConfigFromXML(InputStream inStream) {
        if (this.DEBUG) {
            Slog.i(this.TAG, "getConfigFromeXML");
        }
        boolean BrightenGradualTimeLoaded = false;
        boolean DarkenGradualTimeLoaded = false;
        boolean DarkenGradualTimeMaxLoaded = false;
        boolean DarkenGradualTimeMinLoaded = false;
        boolean BrightenThresholdFor255Loaded = false;
        boolean DarkenTargetFor255Loaded = false;
        boolean DarkenCurrentFor255Loaded = false;
        boolean configGroupLoadStarted = false;
        boolean loadFinished = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                switch (eventType) {
                    case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                        String name = parser.getName();
                        if (!name.equals("LABCConfig")) {
                            if (configGroupLoadStarted) {
                                if (!name.equals("BrightenGradualTime")) {
                                    if (!name.equals("DarkenGradualTime")) {
                                        if (!name.equals("BrightenThresholdFor255")) {
                                            if (!name.equals("DarkenTargetFor255")) {
                                                if (!name.equals("DarkenCurrentFor255")) {
                                                    if (!name.equals("AutoFastTimeFor255")) {
                                                        if (!name.equals("ManualFastTimeFor255")) {
                                                            if (!name.equals("DimTime")) {
                                                                if (!name.equals("DarkenGradualTimeMax")) {
                                                                    if (!name.equals("DarkenGradualTimeMin")) {
                                                                        if (name.equals("AnimatedStepRoundEnabled")) {
                                                                            this.mAnimatedStepRoundEnabled = Boolean.parseBoolean(parser.nextText());
                                                                            break;
                                                                        }
                                                                    }
                                                                    this.mDarkenGradualTimeMin = Float.parseFloat(parser.nextText());
                                                                    DarkenGradualTimeMinLoaded = true;
                                                                    break;
                                                                }
                                                                this.mDarkenGradualTimeMax = Float.parseFloat(parser.nextText());
                                                                DarkenGradualTimeMaxLoaded = true;
                                                                break;
                                                            }
                                                            this.mDimTime = Float.parseFloat(parser.nextText());
                                                            break;
                                                        }
                                                        this.mManualFastTimeFor255 = Float.parseFloat(parser.nextText());
                                                        break;
                                                    }
                                                    this.mAutoFastTimeFor255 = Float.parseFloat(parser.nextText());
                                                    break;
                                                }
                                                this.mDarkenCurrentFor255 = Integer.parseInt(parser.nextText());
                                                DarkenCurrentFor255Loaded = true;
                                                break;
                                            }
                                            this.mDarkenTargetFor255 = Integer.parseInt(parser.nextText());
                                            DarkenTargetFor255Loaded = true;
                                            break;
                                        }
                                        this.mBrightenThresholdFor255 = Integer.parseInt(parser.nextText());
                                        BrightenThresholdFor255Loaded = true;
                                        break;
                                    }
                                    this.mDarkenGradualTime = Float.parseFloat(parser.nextText());
                                    DarkenGradualTimeLoaded = true;
                                    break;
                                }
                                this.mBrightenGradualTime = Float.parseFloat(parser.nextText());
                                BrightenGradualTimeLoaded = true;
                                break;
                            }
                        } else if (this.mDeviceActualBrightnessLevel != 0) {
                            String deviceLevelString = parser.getAttributeValue(null, MemoryConstant.MEM_FILECACHE_ITEM_LEVEL);
                            if (deviceLevelString != null && deviceLevelString.length() != 0) {
                                int deviceLevel = Integer.parseInt(deviceLevelString);
                                int i = this.mDeviceActualBrightnessLevel;
                                if (deviceLevel == r0) {
                                    if (this.DEBUG) {
                                        Slog.i(this.TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", find matched level in XML, load start");
                                    }
                                    configGroupLoadStarted = true;
                                    break;
                                }
                            }
                            if (this.DEBUG) {
                                Slog.i(this.TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", but can't find level in XML, load start");
                            }
                            configGroupLoadStarted = true;
                            break;
                        } else {
                            if (this.DEBUG) {
                                Slog.i(this.TAG, "actualDeviceLevel = 0, load started");
                            }
                            configGroupLoadStarted = true;
                            break;
                        }
                        break;
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        if (parser.getName().equals("LABCConfig") && configGroupLoadStarted) {
                            loadFinished = true;
                            break;
                        }
                }
                if (loadFinished) {
                    if (DarkenGradualTimeMaxLoaded && DarkenGradualTimeMinLoaded) {
                        if (this.DEBUG) {
                            Slog.i(this.TAG, "use variable steps!");
                        }
                        this.mUseVariableSteps = true;
                    }
                    if (!BrightenGradualTimeLoaded && DarkenGradualTimeLoaded && BrightenThresholdFor255Loaded && DarkenTargetFor255Loaded && DarkenCurrentFor255Loaded) {
                        if (this.DEBUG) {
                            Slog.i(this.TAG, "getConfigFromeXML success!");
                        }
                        return true;
                    }
                    if (!configGroupLoadStarted) {
                        Slog.e(this.TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", can't find matched level in XML, load failed!");
                        return false;
                    }
                    Slog.e(this.TAG, "getConfig failed!");
                    return false;
                }
            }
            if (this.DEBUG) {
                Slog.i(this.TAG, "use variable steps!");
            }
            this.mUseVariableSteps = true;
            if (!BrightenGradualTimeLoaded) {
            }
            if (configGroupLoadStarted) {
                Slog.e(this.TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", can't find matched level in XML, load failed!");
                return false;
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        Slog.e(this.TAG, "getConfig failed!");
        return false;
    }

    private float getAnimatedStepByEyeSensitiveCurve(float currentValue, float targetValue, float duration) {
        if (currentValue == 0.0f) {
            Slog.e(this.TAG, "currentValue is 0, set step to default value!");
            return DEFAULT_AMOUNT;
        }
        if (duration <= 0.116f && currentValue > targetValue) {
            Slog.e(this.TAG, "duration is not valid, set to 3.0!");
            duration = CustomGestureDetector.TOUCH_TOLERANCE;
        }
        if (this.mFirstTimeCalculateAmount) {
            float avgTime;
            float avgPara = ((((float) Math.pow((double) (targetValue / 10000.0f), 0.09000000357627869d)) * 0.0029f) * CRITERION_TIME) / duration;
            if (currentValue > targetValue) {
                avgTime = ((float) (Math.log((double) (targetValue / currentValue)) / Math.log((double) (HwCircleAnimation.SMALL_ALPHA - avgPara)))) * 0.016540745f;
                this.mStepAdjValue = avgTime < this.mDarkenGradualTimeMin ? avgTime / this.mDarkenGradualTimeMin : HwCircleAnimation.SMALL_ALPHA;
            } else {
                avgTime = ((float) (Math.log((double) (targetValue / currentValue)) / Math.log((double) (HwCircleAnimation.SMALL_ALPHA + avgPara)))) * 0.016540745f;
                this.mStepAdjValue = avgTime < duration ? avgTime / duration : HwCircleAnimation.SMALL_ALPHA;
            }
            if (this.DEBUG) {
                Slog.d(this.TAG, "getAnimatedStep avgTime= " + avgTime + ",avgPara" + avgPara + ",mStepAdjValue=" + this.mStepAdjValue + ",duration=" + duration);
            }
        }
        this.mAnimatedStep = 0.0029f * currentValue;
        this.mAnimatedStep *= (float) Math.pow((double) (targetValue / 10000.0f), 0.09000000357627869d);
        if (duration >= 20.0f && duration < 30.0f) {
            duration += HwCircleAnimation.SMALL_ALPHA;
        }
        this.mAnimatedStep = ((this.mAnimatedStep * this.mStepAdjValue) * CRITERION_TIME) / duration;
        if (this.mAnimatedStep >= HwCircleAnimation.SMALL_ALPHA && this.mAnimatedStepRoundEnabled) {
            this.mAnimatedStep = (float) Math.round(this.mAnimatedStep);
        } else if (this.mAnimatedStep < HwCircleAnimation.SMALL_ALPHA && this.mAnimatedStep >= FAST_TIME && this.mStepAdjValue == HwCircleAnimation.SMALL_ALPHA) {
            this.mAnimatedStep = FAST_TIME;
        } else if (this.mAnimatedStep < FAST_TIME && this.mStepAdjValue == HwCircleAnimation.SMALL_ALPHA) {
            this.mAnimatedStep = 0.25f;
        }
        return this.mAnimatedStep;
    }

    public float getAnimatedValue() {
        if (ValueAnimator.getDurationScale() == 0.0f || this.mRate == 0) {
            this.mAnimatedValue = (float) this.mTargetValue;
        } else {
            float amount;
            if (this.mAutoBrightnessMode) {
                amount = getAutoModeAnimtionAmount();
            } else {
                amount = getManualModeAnimtionAmount();
            }
            if (this.mTargetValue > this.mCurrentValue) {
                this.mAnimatedValue = Math.min(this.mAnimatedValue + amount, (float) this.mTargetValue);
            } else {
                this.mAnimatedValue = Math.max(this.mAnimatedValue - amount, (float) this.mTargetValue);
            }
        }
        return this.mAnimatedValue;
    }

    public float getAutoModeAnimtionAmount() {
        if (this.mFirstTimeCalculateAmount) {
            float duration;
            float amount;
            if (this.mCoverModeAnimationFast) {
                duration = this.mCoverModeAnimationTime;
                if (this.DEBUG) {
                    Slog.i(this.TAG, "LabcCoverMode mCoverModeFast=" + this.mCoverModeAnimationFast);
                }
            } else if (this.mFirstValidAutoBrightness || this.mAutoBrightnessIntervened || this.mfastAnimtionFlag) {
                duration = FAST_TIME;
                if (this.DEBUG) {
                    Slog.i(this.TAG, "mFirstValidAuto=" + this.mFirstValidAutoBrightness + ",mAutoIntervened=" + this.mAutoBrightnessIntervened + "mfastAnimtionFlag=" + this.mfastAnimtionFlag);
                }
            } else {
                duration = this.mTargetValue < this.mCurrentValue ? getAutoModeDarkTime() : getAutoModeBrightTime();
            }
            if (this.mUseVariableSteps && duration >= this.mDarkenFixStepsThreshold && this.mTargetValue < this.mCurrentValue) {
                amount = getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, duration);
            } else if (!this.mUseVariableSteps || duration < this.mBrightenFixStepsThreshold || this.mTargetValue < this.mCurrentValue) {
                amount = (((float) Math.abs(this.mCurrentValue - this.mTargetValue)) / duration) * 0.016540745f;
            } else {
                amount = getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, duration);
            }
            this.mDuration = duration;
            this.mDecreaseFixAmount = amount;
            this.mFirstTimeCalculateAmount = false;
            if (!this.DEBUG) {
                return amount;
            }
            Slog.d(this.TAG, "AutoMode=" + this.mAutoBrightnessMode + ",Target=" + this.mTargetValue + ",Current=" + this.mCurrentValue + ",amount=" + amount + ",duration=" + duration);
            return amount;
        } else if (this.mUseVariableSteps && this.mDuration >= this.mDarkenFixStepsThreshold && this.mTargetValue < this.mCurrentValue) {
            return getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, this.mDuration);
        } else {
            if (!this.mUseVariableSteps || this.mDuration < this.mBrightenFixStepsThreshold || this.mTargetValue < this.mCurrentValue) {
                return this.mDecreaseFixAmount;
            }
            return getAnimatedStepByEyeSensitiveCurve(this.mAnimatedValue, (float) this.mTargetValue, this.mDuration);
        }
    }

    public float getAutoModeDarkTime() {
        float duration;
        if (this.mUseVariableSteps) {
            duration = this.mDarkenGradualTimeMax;
        } else {
            duration = this.mDarkenGradualTime;
        }
        if (this.mTargetValue < this.mDarkenTargetFor255 && this.mCurrentValue < this.mDarkenCurrentFor255 && (Math.abs(duration - this.mDarkenGradualTime) < 1.0E-7f || Math.abs(duration - this.mDarkenGradualTimeMax) < 1.0E-7f)) {
            duration = FAST_TIME;
        }
        if (this.mPowerDimState) {
            if (this.mDarkenGradualTime > this.mDimTime || (this.mUseVariableSteps && this.mDarkenGradualTimeMax > this.mDimTime)) {
                duration = this.mDimTime;
            }
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state");
            }
        }
        if (this.mPowerDimRecoveryState) {
            this.mPowerDimRecoveryState = false;
            duration = FAST_TIME;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state Recovery");
            }
        }
        return duration;
    }

    public float getAutoModeBrightTime() {
        float duration;
        if (this.mPowerDimRecoveryState) {
            this.mPowerDimRecoveryState = false;
            duration = FAST_TIME;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state Recovery");
            }
        } else {
            duration = this.mBrightenGradualTime;
        }
        if (this.mTargetValue < this.mDarkenTargetFor255) {
            return FAST_TIME;
        }
        return duration;
    }

    public float getManualModeTime() {
        float duration = FAST_TIME;
        if (this.mPowerDimState) {
            if (this.mDarkenGradualTime > this.mDimTime || (this.mUseVariableSteps && this.mDarkenGradualTimeMax > this.mDimTime)) {
                duration = this.mDimTime;
            }
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state");
            }
        }
        if (this.mPowerDimRecoveryState) {
            this.mPowerDimRecoveryState = false;
            duration = this.mManualFastTimeFor255;
            if (this.DEBUG) {
                Slog.d(this.TAG, "The Dim state Recovery");
            }
        }
        return duration;
    }

    public float getManualModeAnimtionAmount() {
        if (!this.mFirstTimeCalculateAmount) {
            return this.mDecreaseFixAmount;
        }
        float duration = getManualModeTime();
        float amount = (((float) Math.abs(this.mCurrentValue - this.mTargetValue)) / duration) * 0.016540745f;
        this.mDecreaseFixAmount = amount;
        this.mFirstTimeCalculateAmount = false;
        if (!this.DEBUG) {
            return amount;
        }
        Slog.d(this.TAG, "AutoMode=" + this.mAutoBrightnessMode + ",Target=" + this.mTargetValue + ",Current=" + this.mCurrentValue + ",amount=" + amount + ",duration=" + duration);
        return amount;
    }

    public void updateTargetAndRate(int target, int rate) {
        if (this.mTargetValue != target) {
            this.mFirstTimeCalculateAmount = true;
        }
        this.mTargetValue = target;
        this.mRate = rate;
    }

    public void updateCurrentBrightnessValue(float currentValue) {
        this.mCurrentValue = Math.round(currentValue);
        this.mAnimatedValue = currentValue;
    }

    public void setPowerDimState(int state) {
        boolean z;
        if (state == 2) {
            z = true;
        } else {
            z = false;
        }
        this.mPowerDimState = z;
        if (this.mPowerDimState) {
            this.mFirstValidAutoBrightness = false;
        }
        if (this.mState == 2 && state == 3) {
            this.mPowerDimRecoveryState = true;
        }
        this.mState = state;
    }

    public void updateAdjustMode(boolean automode) {
        this.mAutoBrightnessMode = automode;
    }

    public void autoModeIsIntervened(boolean intervened) {
        this.mAutoBrightnessIntervened = intervened;
    }

    public void isFirstValidAutoBrightness(boolean firstValidAutoBrightness) {
        this.mFirstValidAutoBrightness = firstValidAutoBrightness;
    }

    public void updateFastAnimationFlag(boolean fastAnimtionFlag) {
        this.mfastAnimtionFlag = fastAnimtionFlag;
    }

    public void updateCoverModeFastAnimationFlag(boolean coverModeAmitionFast) {
        this.mCoverModeAnimationFast = coverModeAmitionFast;
    }

    public void clearAnimatedValuePara() {
        this.mFirstValidAutoBrightness = false;
        this.mAutoBrightnessIntervened = false;
        this.mPowerDimState = false;
        this.mPowerDimRecoveryState = false;
        this.mfastAnimtionFlag = false;
        this.mCoverModeAnimationFast = false;
    }
}
