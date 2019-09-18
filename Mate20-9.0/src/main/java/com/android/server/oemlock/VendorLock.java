package com.android.server.oemlock;

import android.content.Context;
import android.hardware.oemlock.V1_0.IOemLock;
import android.os.RemoteException;
import android.util.Slog;
import java.util.ArrayList;
import java.util.NoSuchElementException;

class VendorLock extends OemLock {
    private static final String TAG = "OemLock";
    private Context mContext;
    private IOemLock mOemLock;

    static IOemLock getOemLockHalService() {
        try {
            return IOemLock.getService();
        } catch (NoSuchElementException e) {
            Slog.i(TAG, "OemLock HAL not present on device");
            return null;
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    VendorLock(Context context, IOemLock oemLock) {
        this.mContext = context;
        this.mOemLock = oemLock;
    }

    /* access modifiers changed from: package-private */
    public void setOemUnlockAllowedByCarrier(boolean allowed, byte[] signature) {
        try {
            switch (this.mOemLock.setOemUnlockAllowedByCarrier(allowed, toByteArrayList(signature))) {
                case 0:
                    Slog.i(TAG, "Updated carrier allows OEM lock state to: " + allowed);
                    return;
                case 1:
                    break;
                case 2:
                    throw new SecurityException("Invalid signature used in attempt to carrier unlock");
                default:
                    Slog.e(TAG, "Unknown return value indicates code is out of sync with HAL");
                    break;
            }
            throw new RuntimeException("Failed to set carrier OEM unlock state");
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to set carrier state with HAL", e);
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isOemUnlockAllowedByCarrier() {
        Integer[] requestStatus = new Integer[1];
        Boolean[] allowedByCarrier = new Boolean[1];
        try {
            this.mOemLock.isOemUnlockAllowedByCarrier(new IOemLock.isOemUnlockAllowedByCarrierCallback(requestStatus, allowedByCarrier) {
                private final /* synthetic */ Integer[] f$0;
                private final /* synthetic */ Boolean[] f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(int i, boolean z) {
                    VendorLock.lambda$isOemUnlockAllowedByCarrier$0(this.f$0, this.f$1, i, z);
                }
            });
            switch (requestStatus[0].intValue()) {
                case 0:
                    return allowedByCarrier[0].booleanValue();
                case 1:
                    break;
                default:
                    Slog.e(TAG, "Unknown return value indicates code is out of sync with HAL");
                    break;
            }
            throw new RuntimeException("Failed to get carrier OEM unlock state");
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to get carrier state from HAL");
            throw e.rethrowFromSystemServer();
        }
    }

    static /* synthetic */ void lambda$isOemUnlockAllowedByCarrier$0(Integer[] requestStatus, Boolean[] allowedByCarrier, int status, boolean allowed) {
        requestStatus[0] = Integer.valueOf(status);
        allowedByCarrier[0] = Boolean.valueOf(allowed);
    }

    /* access modifiers changed from: package-private */
    public void setOemUnlockAllowedByDevice(boolean allowedByDevice) {
        try {
            switch (this.mOemLock.setOemUnlockAllowedByDevice(allowedByDevice)) {
                case 0:
                    Slog.i(TAG, "Updated device allows OEM lock state to: " + allowedByDevice);
                    return;
                case 1:
                    break;
                default:
                    Slog.e(TAG, "Unknown return value indicates code is out of sync with HAL");
                    break;
            }
            throw new RuntimeException("Failed to set device OEM unlock state");
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to set device state with HAL", e);
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isOemUnlockAllowedByDevice() {
        Integer[] requestStatus = new Integer[1];
        Boolean[] allowedByDevice = new Boolean[1];
        try {
            this.mOemLock.isOemUnlockAllowedByDevice(new IOemLock.isOemUnlockAllowedByDeviceCallback(requestStatus, allowedByDevice) {
                private final /* synthetic */ Integer[] f$0;
                private final /* synthetic */ Boolean[] f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void onValues(int i, boolean z) {
                    VendorLock.lambda$isOemUnlockAllowedByDevice$1(this.f$0, this.f$1, i, z);
                }
            });
            switch (requestStatus[0].intValue()) {
                case 0:
                    return allowedByDevice[0].booleanValue();
                case 1:
                    break;
                default:
                    Slog.e(TAG, "Unknown return value indicates code is out of sync with HAL");
                    break;
            }
            throw new RuntimeException("Failed to get device OEM unlock state");
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to get devie state from HAL");
            throw e.rethrowFromSystemServer();
        }
    }

    static /* synthetic */ void lambda$isOemUnlockAllowedByDevice$1(Integer[] requestStatus, Boolean[] allowedByDevice, int status, boolean allowed) {
        requestStatus[0] = Integer.valueOf(status);
        allowedByDevice[0] = Boolean.valueOf(allowed);
    }

    private ArrayList toByteArrayList(byte[] data) {
        if (data == null) {
            return null;
        }
        ArrayList<Byte> result = new ArrayList<>(data.length);
        for (byte b : data) {
            result.add(Byte.valueOf(b));
        }
        return result;
    }
}
