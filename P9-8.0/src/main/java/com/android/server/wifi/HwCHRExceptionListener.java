package com.android.server.wifi;

import android.content.Context;
import android.util.Log;
import com.android.server.HwServiceFactory;
import com.android.server.location.IHwGpsLogServices;
import java.io.File;

public class HwCHRExceptionListener {
    private static String DEVICE_ERR_PATH = "/dev/chrKmsgPlat";
    protected static final boolean HWFLOW;
    private static final int SLEEP_TIME = 30000;
    private static final int SUBSYS_BT = 4;
    private static final int SUBSYS_GPS = 3;
    private static final int SUBSYS_WIFI = 2;
    private static final String TAG = "HwChrExceptionListener";
    private static HwCHRExceptionListener gHwChrExpListener = null;
    private ExceptionListenerServer els = null;
    private HwWifiCHRStateManagerImpl hwWifiCHRManagerImpl = null;
    private Context mContext = null;
    private IHwGpsLogServices mHwGpsLogServices = null;

    private class ExceptionListenerServer extends Thread {
        private boolean keepRunning;

        /* synthetic */ ExceptionListenerServer(HwCHRExceptionListener this$0, ExceptionListenerServer -this1) {
            this();
        }

        private ExceptionListenerServer() {
            this.keepRunning = true;
            if (!new File(HwCHRExceptionListener.DEVICE_ERR_PATH).exists()) {
                setRun(false);
            }
        }

        public void setRun(boolean bRun) {
            this.keepRunning = bRun;
        }

        public void run() {
            while (this.keepRunning) {
                byte[] buffer = HwCHRWifiFile.getDevFileResult(HwCHRExceptionListener.DEVICE_ERR_PATH);
                if (buffer == null) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        Log.e(HwCHRExceptionListener.TAG, "Thread sleep InterruptedException");
                    }
                    Log.e(HwCHRExceptionListener.TAG, "read chrKmsgPlat Exception");
                } else {
                    HwCHRExceptionListener.this.processException(buffer);
                }
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public static HwCHRExceptionListener getInstance(Context context) {
        if (gHwChrExpListener == null) {
            gHwChrExpListener = new HwCHRExceptionListener(context);
            if (HWFLOW) {
                Log.d(TAG, "new HwChrWifiExceptionListener");
            }
        }
        return gHwChrExpListener;
    }

    public void startChrWifiListener() {
        this.els = new ExceptionListenerServer(this, null);
        this.els.start();
        if (HWFLOW) {
            Log.d(TAG, "startChrWifiListener");
        }
    }

    private HwCHRExceptionListener(Context context) {
        this.mContext = context;
        this.hwWifiCHRManagerImpl = (HwWifiCHRStateManagerImpl) HwWifiServiceFactory.getHwWifiCHRStateManager();
        this.mHwGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
    }

    private void dispatchException(int subsys, String devErrData) {
        switch (subsys) {
            case 2:
                processWifiEvent(devErrData);
                return;
            case 3:
                processGnssEvent(devErrData);
                return;
            case 4:
                processBtEvent(devErrData);
                return;
            default:
                if (HWFLOW) {
                    Log.d(TAG, "unknown exception module");
                    return;
                }
                return;
        }
    }

    private void processWifiEvent(String devErrData) {
        if (HWFLOW) {
            Log.d(TAG, "processWifiEvent, " + devErrData);
        }
        this.hwWifiCHRManagerImpl.processWifiHalDriverEvent(devErrData);
    }

    private void processGnssEvent(String strJsonExp) {
        if (this.mHwGpsLogServices == null) {
            this.mHwGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
        }
        if (HWFLOW) {
            Log.d(TAG, "processGnssEvent, " + strJsonExp);
        }
        this.mHwGpsLogServices.processGnssHalDriverEvent(strJsonExp);
    }

    private void processBtEvent(String strJsonExp) {
        if (HWFLOW) {
            Log.d(TAG, "processBtEvent, " + strJsonExp);
        }
    }

    public void processException(byte[] devErrData) {
        String strErrData = bytesToHexString(devErrData);
        int subsys = (devErrData[2] & 248) >> 3;
        Log.d(TAG, "subsys " + subsys + ", strErrData " + strErrData);
        dispatchException(subsys, strErrData);
    }

    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = src.length - 1; i >= 0; i--) {
            String hv = Integer.toHexString(src[i] & 255);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
