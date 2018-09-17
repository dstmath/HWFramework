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

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0062 A:{SYNTHETIC, Splitter: B:21:0x0062} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0050 A:{SYNTHETIC, Splitter: B:15:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0074 A:{SYNTHETIC, Splitter: B:27:0x0074} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setProjectionModeValue(String val) {
        Throwable th;
        Log.v(TAG, "setProjectionModeValue " + val);
        FileOutputStream fileOutWriteMode = null;
        try {
            FileOutputStream fileOutWriteMode2 = new FileOutputStream(new File(FILE_PATH));
            try {
                fileOutWriteMode2.write(val.getBytes("utf-8"));
                if (fileOutWriteMode2 != null) {
                    try {
                        fileOutWriteMode2.close();
                    } catch (IOException e) {
                        Log.e(TAG, "fail to setProjectionModeValue");
                    }
                }
                fileOutWriteMode = fileOutWriteMode2;
            } catch (FileNotFoundException e2) {
                fileOutWriteMode = fileOutWriteMode2;
                if (fileOutWriteMode != null) {
                }
                Log.v(TAG, "setProjectionModeValue end");
            } catch (IOException e3) {
                fileOutWriteMode = fileOutWriteMode2;
                if (fileOutWriteMode != null) {
                }
                Log.v(TAG, "setProjectionModeValue end");
            } catch (Throwable th2) {
                th = th2;
                fileOutWriteMode = fileOutWriteMode2;
                if (fileOutWriteMode != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e4) {
            if (fileOutWriteMode != null) {
                try {
                    fileOutWriteMode.close();
                } catch (IOException e5) {
                    Log.e(TAG, "fail to setProjectionModeValue");
                }
            }
            Log.v(TAG, "setProjectionModeValue end");
        } catch (IOException e6) {
            if (fileOutWriteMode != null) {
                try {
                    fileOutWriteMode.close();
                } catch (IOException e7) {
                    Log.e(TAG, "fail to setProjectionModeValue");
                }
            }
            Log.v(TAG, "setProjectionModeValue end");
        } catch (Throwable th3) {
            th = th3;
            if (fileOutWriteMode != null) {
                try {
                    fileOutWriteMode.close();
                } catch (IOException e8) {
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

    /* JADX WARNING: Removed duplicated region for block: B:29:0x007f A:{SYNTHETIC, Splitter: B:29:0x007f} */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00af A:{SYNTHETIC, Splitter: B:45:0x00af} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isExclusiveKeyboardOnline() {
        Throwable th;
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(KEYBOARD_STATE_INFO), StandardCharsets.UTF_8));
            try {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    int intC = reader2.read();
                    if (intC == -1) {
                        break;
                    }
                    char c = (char) intC;
                    if (c != 10) {
                        if (sb.length() >= 100) {
                            break;
                        }
                        sb.append(c);
                    } else {
                        break;
                    }
                }
                String keyboardStatus = sb.toString();
                HwPCUtils.log(TAG, "Exclisive status:" + keyboardStatus);
                if (keyboardStatus == null || !keyboardStatus.trim().equals("keyboard is online".trim())) {
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e) {
                            HwPCUtils.log(TAG, "isExclisiveKeyboardOnline() close():IOException");
                        }
                    }
                    return false;
                }
                HwPCUtils.log(TAG, "isExclisiveKeyboardOnline():true");
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e2) {
                        HwPCUtils.log(TAG, "isExclisiveKeyboardOnline() close():IOException");
                    }
                }
                return true;
            } catch (IOException e3) {
                reader = reader2;
                try {
                    HwPCUtils.log(TAG, "isExclisiveKeyboardOnline():IOException");
                    if (reader != null) {
                    }
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e4) {
                            HwPCUtils.log(TAG, "isExclisiveKeyboardOnline() close():IOException");
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            HwPCUtils.log(TAG, "isExclisiveKeyboardOnline():IOException");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e6) {
                    HwPCUtils.log(TAG, "isExclisiveKeyboardOnline() close():IOException");
                }
            }
            return false;
        }
    }
}
