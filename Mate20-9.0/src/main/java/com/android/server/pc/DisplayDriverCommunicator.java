package com.android.server.pc;

import android.util.HwPCUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class DisplayDriverCommunicator {
    private static final String FILE_PATH = "/sys/class/dp/source/source_mode";
    private static String KEYBOARD_STATE_INFO = "/sys/devices/platform/hwsw_kb/stateinfo";
    private static final int KEYBOARD_STATE_INFO_MAX_LENGTH = 100;
    public static final String START_DESKTOP_MODE_VALUE = "0";
    public static final String START_PHONE_MODE_VALUE = "1";
    private static final String TAG = "DisplayDriverCommunicator";
    private static volatile DisplayDriverCommunicator mInstance = null;
    private boolean isFeatureSupport;

    private DisplayDriverCommunicator() {
        this.isFeatureSupport = false;
        this.isFeatureSupport = isFeatureSupport();
    }

    private boolean isFeatureSupport() {
        if (new File(FILE_PATH).exists()) {
            return true;
        }
        return false;
    }

    public synchronized void resetProjectionMode() {
        Log.v(TAG, "resetProjectionMode isFeatureSupport = " + this.isFeatureSupport);
        if (this.isFeatureSupport) {
            setProjectionModeValue("1");
        }
    }

    public synchronized void enableProjectionMode() {
        Log.v(TAG, "enableProjectionMode isFeatureSupport = " + this.isFeatureSupport);
        if (this.isFeatureSupport) {
            setProjectionModeValue("0");
        }
    }

    private void setProjectionModeValue(String val) {
        Log.v(TAG, "setProjectionModeValue " + val);
        FileOutputStream fileOutWriteMode = null;
        try {
            fileOutWriteMode = new FileOutputStream(new File(FILE_PATH));
            fileOutWriteMode.write(val.getBytes("utf-8"));
            try {
                fileOutWriteMode.close();
            } catch (IOException e) {
                Log.e(TAG, "fail to setProjectionModeValue");
            }
        } catch (FileNotFoundException e2) {
            if (fileOutWriteMode != null) {
                fileOutWriteMode.close();
            }
        } catch (IOException e3) {
            if (fileOutWriteMode != null) {
                fileOutWriteMode.close();
            }
        } catch (Throwable th) {
            if (fileOutWriteMode != null) {
                try {
                    fileOutWriteMode.close();
                } catch (IOException e4) {
                    Log.e(TAG, "fail to setProjectionModeValue");
                }
            }
            throw th;
        }
        Log.v(TAG, "setProjectionModeValue end");
    }

    public static synchronized DisplayDriverCommunicator getInstance() {
        DisplayDriverCommunicator displayDriverCommunicator;
        synchronized (DisplayDriverCommunicator.class) {
            if (mInstance == null) {
                mInstance = new DisplayDriverCommunicator();
            }
            displayDriverCommunicator = mInstance;
        }
        return displayDriverCommunicator;
    }

    public static boolean isExclusiveKeyboardOnline() {
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(KEYBOARD_STATE_INFO), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            while (true) {
                int read = reader2.read();
                int intC = read;
                if (read == -1) {
                    break;
                }
                char c = (char) intC;
                if (c == 10) {
                    break;
                } else if (sb.length() >= 100) {
                    break;
                } else {
                    sb.append(c);
                }
            }
            String keyboardStatus = sb.toString();
            HwPCUtils.log(TAG, "Exclisive status:" + keyboardStatus);
            if (keyboardStatus == null || !keyboardStatus.trim().equals("keyboard is online".trim())) {
                try {
                    reader2.close();
                } catch (IOException e) {
                    HwPCUtils.log(TAG, "isExclisiveKeyboardOnline() close():IOException");
                }
                return false;
            }
            HwPCUtils.log(TAG, "isExclisiveKeyboardOnline():true");
            try {
                reader2.close();
            } catch (IOException e2) {
                HwPCUtils.log(TAG, "isExclisiveKeyboardOnline() close():IOException");
            }
            return true;
        } catch (IOException e3) {
            HwPCUtils.log(TAG, "isExclisiveKeyboardOnline():IOException");
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    HwPCUtils.log(TAG, "isExclisiveKeyboardOnline() close():IOException");
                }
            }
            throw th;
        }
    }
}
