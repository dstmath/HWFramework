package com.android.server.wifi;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SarInfo {
    public static final int INITIAL_SAR_SCENARIO = -2;
    public static final int RESET_SAR_SCENARIO = -1;
    public static final int SAR_SENSOR_FREE_SPACE = 1;
    private static final String SAR_SENSOR_FREE_SPACE_STR = "SAR_SENSOR_FREE_SPACE";
    public static final int SAR_SENSOR_NEAR_BODY = 4;
    private static final String SAR_SENSOR_NEAR_BODY_STR = "SAR_SENSOR_NEAR_BODY";
    public static final int SAR_SENSOR_NEAR_HAND = 2;
    private static final String SAR_SENSOR_NEAR_HAND_STR = "SAR_SENSOR_NEAR_HAND";
    public static final int SAR_SENSOR_NEAR_HEAD = 3;
    private static final String SAR_SENSOR_NEAR_HEAD_STR = "SAR_SENSOR_NEAR_HEAD";
    private static final String TAG = "WifiSarInfo";
    public int attemptedSarScenario = -1;
    public boolean isEarPieceActive = false;
    public boolean isVoiceCall = false;
    public boolean isWifiClientEnabled = false;
    public boolean isWifiSapEnabled = false;
    public boolean isWifiScanOnlyEnabled = false;
    private boolean mAllWifiDisabled = true;
    private boolean mLastReportedIsEarPieceActive = false;
    private boolean mLastReportedIsVoiceCall = false;
    private boolean mLastReportedIsWifiSapEnabled = false;
    private int mLastReportedScenario = -2;
    private long mLastReportedScenarioTs = 0;
    private int mLastReportedSensorState = 1;
    public boolean sarSapSupported;
    public boolean sarSensorSupported;
    public boolean sarVoiceCallSupported;
    public int sensorState = 1;

    public boolean shouldReport() {
        if (this.isWifiClientEnabled || this.isWifiSapEnabled || this.isWifiScanOnlyEnabled) {
            return (!this.mAllWifiDisabled && this.sensorState == this.mLastReportedSensorState && this.isWifiSapEnabled == this.mLastReportedIsWifiSapEnabled && this.isVoiceCall == this.mLastReportedIsVoiceCall && this.isEarPieceActive == this.mLastReportedIsEarPieceActive) ? false : true;
        }
        this.mAllWifiDisabled = true;
        return false;
    }

    public void reportingSuccessful() {
        this.mLastReportedSensorState = this.sensorState;
        this.mLastReportedIsWifiSapEnabled = this.isWifiSapEnabled;
        this.mLastReportedIsVoiceCall = this.isVoiceCall;
        this.mLastReportedIsEarPieceActive = this.isEarPieceActive;
        this.mLastReportedScenario = this.attemptedSarScenario;
        this.mLastReportedScenarioTs = System.currentTimeMillis();
        this.mAllWifiDisabled = false;
    }

    public boolean resetSarScenarioNeeded() {
        return setSarScenarioNeeded(-1);
    }

    public boolean setSarScenarioNeeded(int scenario) {
        this.attemptedSarScenario = scenario;
        if (this.mAllWifiDisabled || this.mLastReportedScenario != scenario) {
            return true;
        }
        return false;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of SarInfo");
        pw.println("Current values:");
        pw.println("    Sensor state is: " + sensorStateToString(this.sensorState));
        pw.println("    Voice Call state is: " + this.isVoiceCall);
        pw.println("    Wifi Client state is: " + this.isWifiClientEnabled);
        pw.println("    Wifi Soft AP state is: " + this.isWifiSapEnabled);
        pw.println("    Wifi ScanOnly state is: " + this.isWifiScanOnlyEnabled);
        pw.println("    Earpiece state is : " + this.isEarPieceActive);
        pw.println("Last reported values:");
        pw.println("    Sensor state is: " + sensorStateToString(this.mLastReportedSensorState));
        pw.println("    Soft AP state is: " + this.mLastReportedIsWifiSapEnabled);
        pw.println("    Voice Call state is: " + this.mLastReportedIsVoiceCall);
        pw.println("    Earpiece state is: " + this.mLastReportedIsEarPieceActive);
        pw.println("Last reported scenario: " + this.mLastReportedScenario);
        pw.println("Reported " + ((System.currentTimeMillis() - this.mLastReportedScenarioTs) / 1000) + " seconds ago");
    }

    public static String sensorStateToString(int sensorState2) {
        if (sensorState2 == 1) {
            return SAR_SENSOR_FREE_SPACE_STR;
        }
        if (sensorState2 == 2) {
            return SAR_SENSOR_NEAR_HAND_STR;
        }
        if (sensorState2 == 3) {
            return SAR_SENSOR_NEAR_HEAD_STR;
        }
        if (sensorState2 != 4) {
            return "Invalid SAR sensor state";
        }
        return SAR_SENSOR_NEAR_BODY_STR;
    }
}
