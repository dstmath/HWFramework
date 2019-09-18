package com.android.server.usb;

public final class UsbAlsaJackDetector implements Runnable {
    private static final String TAG = "UsbAlsaJackDetector";
    private UsbAlsaDevice mAlsaDevice;
    private boolean mStopJackDetect = false;

    private static native boolean nativeHasJackDetect(int i);

    private native boolean nativeInputJackConnected(int i);

    private native boolean nativeJackDetect(int i);

    private native boolean nativeOutputJackConnected(int i);

    private UsbAlsaJackDetector(UsbAlsaDevice device) {
        this.mAlsaDevice = device;
    }

    public static UsbAlsaJackDetector startJackDetect(UsbAlsaDevice device) {
        if (!nativeHasJackDetect(device.getCardNum())) {
            return null;
        }
        UsbAlsaJackDetector jackDetector = new UsbAlsaJackDetector(device);
        new Thread(jackDetector, "USB jack detect thread").start();
        return jackDetector;
    }

    public boolean isInputJackConnected() {
        return nativeInputJackConnected(this.mAlsaDevice.getCardNum());
    }

    public boolean isOutputJackConnected() {
        return nativeOutputJackConnected(this.mAlsaDevice.getCardNum());
    }

    public void pleaseStop() {
        synchronized (this) {
            this.mStopJackDetect = true;
        }
    }

    public boolean jackDetectCallback() {
        synchronized (this) {
            if (this.mStopJackDetect) {
                return false;
            }
            this.mAlsaDevice.updateWiredDeviceConnectionState(true);
            return true;
        }
    }

    public void run() {
        nativeJackDetect(this.mAlsaDevice.getCardNum());
    }
}
