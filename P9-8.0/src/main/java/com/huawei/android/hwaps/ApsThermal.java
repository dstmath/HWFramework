package com.huawei.android.hwaps;

import android.os.SystemProperties;
import android.util.Log;
import com.huawei.android.hwaps.FpsRequest.SceneTypeE;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ApsThermal {
    private static final int APS_INT_ERROR = -99;
    private static final String TAG = "ApsThermal";
    private static final int THERMAL_ADJ_FPS = -5;
    private static final int THERMAL_ADJ_FPS_MIN = -30;
    private static final int THERMAL_CHECK_PEROID = 60;
    private static String THERMAL_ROOT_DIR = "/sys/devices/virtual/thermal/";
    private static final int THERMAL_TYPE_AP = 1;
    private static final int THERMAL_TYPE_DEFAULT = 0;
    private static final int THERMAL_TYPE_GPU = 2;
    private static final int THERMAL_WARNING_HIGH = 65;
    private static final int THERMAL_WARNING_LOW = 60;
    private static ApsThermalThread sApsThermalThread = null;
    private static ApsThermal sInstance = null;
    private int mAdjFps = THERMAL_ADJ_FPS;
    private int mAdjFpsMin = THERMAL_ADJ_FPS_MIN;
    private int mAdjFpsTotalVal = 0;
    public long mCheckPeroid = 60;
    private FpsRequest mFpsRequest = null;
    private String mThermalNodePath = null;
    private int mThermalRatio = 1;
    private int mThermalType = 2;
    private int mThermalValue = APS_INT_ERROR;
    private int mThermalWarningHigh = THERMAL_WARNING_HIGH;
    private int mThermalWarningLow = 60;

    private static class ApsThermalThread implements Runnable {
        public boolean mRunOver;
        public boolean mStop;

        /* synthetic */ ApsThermalThread(ApsThermalThread -this0) {
            this();
        }

        private ApsThermalThread() {
            this.mStop = false;
            this.mRunOver = false;
        }

        public void run() {
            ApsCommon.logI(ApsThermal.TAG, "ApsThermalThread-run-start");
            this.mRunOver = false;
            while (!this.mStop) {
                ApsThermal.getInstance().thermalFeedbackCtrl();
                sleep(ApsThermal.sInstance.mCheckPeroid);
            }
            this.mRunOver = true;
            ApsCommon.logI(ApsThermal.TAG, "ApsThermalThread-run-end");
        }

        private void sleep(long second) {
            try {
                Thread.sleep(1000 * second);
            } catch (InterruptedException e) {
                Log.w("ApsThermalThread", "APS: sleep exception");
            }
        }
    }

    public static synchronized ApsThermal getInstance() {
        ApsThermal apsThermal;
        synchronized (ApsThermal.class) {
            if (sInstance == null) {
                sInstance = new ApsThermal();
            }
            apsThermal = sInstance;
        }
        return apsThermal;
    }

    public static boolean isSupportAPSThermal() {
        if (4 == (SystemProperties.getInt("sys.aps.support", 0) & 4)) {
            return true;
        }
        ApsCommon.logI(TAG, "APS: Thermal module is not supported");
        return false;
    }

    private ApsThermal() {
        if (this.mFpsRequest == null) {
            this.mFpsRequest = new FpsRequest(SceneTypeE.THERMO_CONTROL);
        }
        initThermalConfig();
        startApsThermalThread();
        ApsCommon.logI(TAG, "Thermal module create success");
    }

    public void stop() {
        if (!sApsThermalThread.mStop) {
            ApsCommon.logI(TAG, "stop thermal check.");
            if (sApsThermalThread != null) {
                sApsThermalThread.mStop = true;
            }
        }
    }

    public void resume() {
        if (sApsThermalThread.mStop) {
            ApsCommon.logI(TAG, "resume thermal check.");
            if (sApsThermalThread == null) {
                Log.w(TAG, "APS: resume ApsThermalThread is null");
                return;
            }
            sApsThermalThread.mStop = false;
            if (sApsThermalThread.mRunOver) {
                startApsThermalThread();
            }
        }
    }

    private void startApsThermalThread() {
        if (sApsThermalThread == null) {
            sApsThermalThread = new ApsThermalThread();
        }
        new Thread(sApsThermalThread, TAG).start();
        ApsCommon.logI(TAG, "APS thermal thread start");
    }

    private void adjustFps(int adjustValue) {
        if (this.mFpsRequest != null) {
            this.mAdjFpsTotalVal += adjustValue;
            if (this.mAdjFpsTotalVal < this.mAdjFpsMin) {
                this.mAdjFpsTotalVal = this.mAdjFpsMin;
            }
            this.mFpsRequest.startFeedback(this.mAdjFpsTotalVal);
            ApsCommon.logI(TAG, "adjustFps-per:" + adjustValue + ", control fps:" + this.mAdjFpsTotalVal);
        }
    }

    private void stopCtrlFps() {
        if (this.mFpsRequest != null && this.mAdjFpsTotalVal != 0) {
            this.mFpsRequest.stop();
            this.mAdjFpsTotalVal = 0;
            ApsCommon.logI(TAG, "stopCtrlFps-stop thermal control fps");
        }
    }

    private int getGpuThermal() {
        int result = APS_INT_ERROR;
        if (this.mThermalNodePath == null) {
            String strNodePath = "";
            File thermalDir = new File(THERMAL_ROOT_DIR);
            if (thermalDir.exists()) {
                String strType = "";
                for (File dir : thermalDir.listFiles()) {
                    if (dir.getName().startsWith("thermal_zone")) {
                        ApsCommon.logD(TAG, "getGpuThermal-dir name in thermal:" + dir.getName());
                        strNodePath = THERMAL_ROOT_DIR + dir.getName() + "/type";
                        strType = getNodeValue(strNodePath);
                        ApsCommon.logD(TAG, "getGpuThermal-type:" + strType + ", path:" + strNodePath);
                        if ("tsens2_tz_sensor".equals(strType)) {
                            strNodePath = THERMAL_ROOT_DIR + dir.getName() + "/temp";
                            String strValue = getNodeValue(strNodePath);
                            result = convertStringToInt(strValue);
                            ApsCommon.logI(TAG, "getGpuThermal-temp:" + strValue + ", path:" + strNodePath);
                            if (APS_INT_ERROR != result) {
                                this.mThermalNodePath = strNodePath;
                                return result;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                this.mThermalNodePath = "";
                ApsCommon.logI(TAG, "getGpuThermal-not find thermal node path.");
                return result;
            }
            ApsCommon.logI(TAG, "getGpuThermal-not find thermal root dir.");
            return APS_INT_ERROR;
        } else if (this.mThermalNodePath.isEmpty()) {
            return APS_INT_ERROR;
        } else {
            return convertStringToInt(getNodeValue(this.mThermalNodePath));
        }
    }

    private int getApThermal() {
        int result = APS_INT_ERROR;
        if (this.mThermalNodePath == null) {
            File thermalDir = new File(THERMAL_ROOT_DIR);
            if (thermalDir.exists()) {
                String strNodePath = "";
                for (File dir : thermalDir.listFiles()) {
                    String strValue;
                    if (dir.getName().startsWith("temp")) {
                        if ("temp:systemh".equals(dir.getName())) {
                            strNodePath = THERMAL_ROOT_DIR + dir.getName() + "/val";
                            strValue = getNodeValue(strNodePath);
                            result = convertStringToInt(strValue);
                            ApsCommon.logI(TAG, "getApThermal-temp:" + strValue + ", path:" + strNodePath);
                            if (APS_INT_ERROR != result) {
                                this.mThermalNodePath = strNodePath;
                                this.mThermalRatio = 10;
                                ApsCommon.logI(TAG, "getApThermal-set thermal ratio:" + this.mThermalRatio);
                                return result;
                            }
                        } else {
                            continue;
                        }
                    } else if (dir.getName().startsWith("thermal_zone")) {
                        String strType = getNodeValue(THERMAL_ROOT_DIR + dir.getName() + "/type");
                        if ("tsens_tz_sensor2".equals(strType) || "tsens5_tz_sensor".equals(strType)) {
                            strNodePath = THERMAL_ROOT_DIR + dir.getName() + "/temp";
                            strValue = getNodeValue(strNodePath);
                            result = convertStringToInt(strValue);
                            ApsCommon.logI(TAG, "getApThermal-temp:" + strValue + ", path:" + strNodePath);
                            if (APS_INT_ERROR != result) {
                                this.mThermalNodePath = strNodePath;
                                return result;
                            }
                        }
                    } else {
                        continue;
                    }
                }
                this.mThermalNodePath = "";
                ApsCommon.logI(TAG, "getApThermal-not find thermal node path, set saved path empty.");
                return result;
            }
            ApsCommon.logI(TAG, "getApThermal-not find thermal root dir.");
            return APS_INT_ERROR;
        } else if (this.mThermalNodePath.isEmpty()) {
            return APS_INT_ERROR;
        } else {
            return convertStringToInt(getNodeValue(this.mThermalNodePath));
        }
    }

    private int getThermalValue() {
        if (this.mThermalNodePath == null) {
            int result;
            if (2 == this.mThermalType) {
                result = getGpuThermal();
            } else if (1 == this.mThermalType) {
                result = getApThermal();
            } else {
                result = getGpuThermal();
                if (APS_INT_ERROR == result) {
                    this.mThermalNodePath = null;
                    result = getApThermal();
                }
            }
            return calcThermalRatio(result);
        } else if (this.mThermalNodePath.isEmpty()) {
            return APS_INT_ERROR;
        } else {
            return calcThermalRatio(convertStringToInt(getNodeValue(this.mThermalNodePath)));
        }
    }

    private String getNodeValue(String nodePath) {
        String result;
        if (nodePath == null) {
            try {
                ApsCommon.logI(TAG, "getNodeValue-node path is null");
                return null;
            } catch (IOException e) {
                result = null;
                Log.w(TAG, "APS: getNodeValue exception ioe");
            } catch (Exception e2) {
                result = null;
                Log.w(TAG, "APS: getNodeValue exception");
            }
        } else {
            File fileNode = new File(nodePath);
            if (!fileNode.exists()) {
                return null;
            }
            BufferedReader br = new BufferedReader(new FileReader(fileNode));
            result = br.readLine();
            br.close();
            return result;
        }
    }

    private int calcThermalRatio(int nTemp) {
        int result = nTemp;
        if (1 == this.mThermalRatio || this.mThermalRatio == 0) {
            return result;
        }
        return (nTemp + 5) / this.mThermalRatio;
    }

    private int convertStringToInt(String strValue) {
        if (strValue == null) {
            return APS_INT_ERROR;
        }
        int nRetValue;
        try {
            nRetValue = Integer.parseInt(strValue);
        } catch (NumberFormatException e) {
            nRetValue = APS_INT_ERROR;
            Log.w(TAG, "APS: convertStringToInt exception nfe");
        } catch (Exception e2) {
            nRetValue = APS_INT_ERROR;
            Log.w(TAG, "APS: convertStringToInt exception");
        }
        return nRetValue;
    }

    private int checkWarningThermal(int thermal) {
        if (thermal > this.mThermalWarningHigh) {
            return 1;
        }
        if (thermal < this.mThermalWarningLow) {
            return -1;
        }
        return 0;
    }

    public void thermalFeedbackCtrl() {
        ApsCommon.logI(TAG, "thermalFeedbackCtrl-start");
        this.mThermalValue = getThermalValue();
        if (APS_INT_ERROR == this.mThermalValue) {
            Log.w(TAG, "APS: thermalFeedbackCtrl-not find thermal node path, stop thermal check");
            stop();
            return;
        }
        ApsCommon.logI(TAG, "thermalFeedbackCtrl-temp:" + this.mThermalValue);
        int checkResult = checkWarningThermal(this.mThermalValue);
        if (checkResult > 0) {
            adjustFps(this.mAdjFps);
            this.mCheckPeroid = 30;
        } else if (checkResult < 0) {
            stopCtrlFps();
            this.mCheckPeroid = 60;
        } else {
            this.mCheckPeroid = 30;
        }
        ApsCommon.logI(TAG, "thermalFeedbackCtrl-end-sleep(s):" + this.mCheckPeroid);
    }

    private void initThermalConfig() {
        this.mThermalWarningHigh = getThermalWarningHighProp();
        this.mThermalWarningLow = getThermalWarningLowProp();
        this.mCheckPeroid = (long) getThermalCheckPeroidProp();
        this.mAdjFps = getThermalAdjFpsProp();
        this.mAdjFpsMin = getThermalAdjFpsMinProp();
        this.mThermalType = getThermalTypeProp();
    }

    private static int getThermalWarningHighProp() {
        return SystemProperties.getInt("sys.aps.thermal_warning_high", THERMAL_WARNING_HIGH);
    }

    private static int getThermalWarningLowProp() {
        return SystemProperties.getInt("sys.aps.thermal_warning_low", 60);
    }

    private static int getThermalCheckPeroidProp() {
        return SystemProperties.getInt("sys.aps.thermal_check_peroid", 60);
    }

    private static int getThermalAdjFpsProp() {
        return SystemProperties.getInt("sys.aps.thermal_adj_fps", THERMAL_ADJ_FPS);
    }

    private static int getThermalAdjFpsMinProp() {
        return SystemProperties.getInt("sys.aps.thermal_adj_fps_min", THERMAL_ADJ_FPS_MIN);
    }

    private static int getThermalTypeProp() {
        return SystemProperties.getInt("sys.aps.thermal_type", 0);
    }
}
