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
    private static String THERMAL_ROOT_DIR = null;
    private static final int THERMAL_TYPE_AP = 1;
    private static final int THERMAL_TYPE_DEFAULT = 0;
    private static final int THERMAL_TYPE_GPU = 2;
    private static final int THERMAL_WARNING_HIGH = 65;
    private static final int THERMAL_WARNING_LOW = 60;
    private static ApsThermalThread sApsThermalThread;
    private static ApsThermal sInstance;
    private int mAdjFps;
    private int mAdjFpsMin;
    private int mAdjFpsTotalVal;
    public long mCheckPeroid;
    private FpsRequest mFpsRequest;
    private String mThermalNodePath;
    private int mThermalRatio;
    private int mThermalType;
    private int mThermalValue;
    private int mThermalWarningHigh;
    private int mThermalWarningLow;

    private static class ApsThermalThread implements Runnable {
        public boolean mRunOver;
        public boolean mStop;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.ApsThermal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.ApsThermal.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.ApsThermal.<clinit>():void");
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
        if (4 == (SystemProperties.getInt("sys.aps.support", THERMAL_TYPE_DEFAULT) & 4)) {
            return true;
        }
        ApsCommon.logI(TAG, "APS: Thermal module is not supported");
        return false;
    }

    private ApsThermal() {
        this.mFpsRequest = null;
        this.mThermalValue = APS_INT_ERROR;
        this.mAdjFpsTotalVal = THERMAL_TYPE_DEFAULT;
        this.mThermalWarningHigh = THERMAL_WARNING_HIGH;
        this.mThermalWarningLow = THERMAL_WARNING_LOW;
        this.mAdjFps = THERMAL_ADJ_FPS;
        this.mAdjFpsMin = THERMAL_ADJ_FPS_MIN;
        this.mThermalType = THERMAL_TYPE_GPU;
        this.mThermalNodePath = null;
        this.mThermalRatio = THERMAL_TYPE_AP;
        this.mCheckPeroid = 60;
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
            this.mAdjFpsTotalVal = THERMAL_TYPE_DEFAULT;
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
                File[] dirsThermal = thermalDir.listFiles();
                int length = dirsThermal.length;
                for (int i = THERMAL_TYPE_DEFAULT; i < length; i += THERMAL_TYPE_AP) {
                    File dir = dirsThermal[i];
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
                File[] dirs = thermalDir.listFiles();
                int length = dirs.length;
                for (int i = THERMAL_TYPE_DEFAULT; i < length; i += THERMAL_TYPE_AP) {
                    File dir = dirs[i];
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
            if (THERMAL_TYPE_GPU == this.mThermalType) {
                result = getGpuThermal();
            } else if (THERMAL_TYPE_AP == this.mThermalType) {
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
        if (THERMAL_TYPE_AP == this.mThermalRatio || this.mThermalRatio == 0) {
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
            return THERMAL_TYPE_AP;
        }
        if (thermal < this.mThermalWarningLow) {
            return -1;
        }
        return THERMAL_TYPE_DEFAULT;
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
        return SystemProperties.getInt("sys.aps.thermal_warning_low", THERMAL_WARNING_LOW);
    }

    private static int getThermalCheckPeroidProp() {
        return SystemProperties.getInt("sys.aps.thermal_check_peroid", THERMAL_WARNING_LOW);
    }

    private static int getThermalAdjFpsProp() {
        return SystemProperties.getInt("sys.aps.thermal_adj_fps", THERMAL_ADJ_FPS);
    }

    private static int getThermalAdjFpsMinProp() {
        return SystemProperties.getInt("sys.aps.thermal_adj_fps_min", THERMAL_ADJ_FPS_MIN);
    }

    private static int getThermalTypeProp() {
        return SystemProperties.getInt("sys.aps.thermal_type", THERMAL_TYPE_DEFAULT);
    }
}
