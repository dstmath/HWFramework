package com.android.server.oemlock;

import android.content.Context;
import android.hardware.oemlock.V1_0.IOemLock;
import android.os.RemoteException;
import android.util.Slog;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/* access modifiers changed from: package-private */
public class VendorLock extends OemLock {
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
    @Override // com.android.server.oemlock.OemLock
    public String getLockName() {
        Integer[] requestStatus = new Integer[1];
        String[] lockName = new String[1];
        try {
            this.mOemLock.getName(new IOemLock.getNameCallback(requestStatus, lockName) {
                /* class com.android.server.oemlock.$$Lambda$VendorLock$mE2wEMNMcvqMft72oSVABYa_mYs */
                private final /* synthetic */ Integer[] f$0;
                private final /* synthetic */ String[] f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // android.hardware.oemlock.V1_0.IOemLock.getNameCallback
                public final void onValues(int i, String str) {
                    VendorLock.lambda$getLockName$0(this.f$0, this.f$1, i, str);
                }
            });
            int intValue = requestStatus[0].intValue();
            if (intValue == 0) {
                return lockName[0];
            }
            if (intValue != 1) {
                Slog.e(TAG, "Unknown return value indicates code is out of sync with HAL");
                return null;
            }
            Slog.e(TAG, "Failed to get OEM lock name.");
            return null;
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to get name from HAL", e);
            throw e.rethrowFromSystemServer();
        }
    }

    static /* synthetic */ void lambda$getLockName$0(Integer[] requestStatus, String[] lockName, int status, String name) {
        requestStatus[0] = Integer.valueOf(status);
        lockName[0] = name;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.oemlock.OemLock
    public void setOemUnlockAllowedByCarrier(boolean allowed, byte[] signature) {
        try {
            ArrayList<Byte> signatureBytes = toByteArrayList(signature);
            int oemUnlockAllowedByCarrier = this.mOemLock.setOemUnlockAllowedByCarrier(allowed, signatureBytes);
            if (oemUnlockAllowedByCarrier != 0) {
                if (oemUnlockAllowedByCarrier != 1) {
                    if (oemUnlockAllowedByCarrier != 2) {
                        Slog.e(TAG, "Unknown return value indicates code is out of sync with HAL");
                    } else if (signatureBytes.isEmpty()) {
                        throw new IllegalArgumentException("Signature required for carrier unlock");
                    } else {
                        throw new SecurityException("Invalid signature used in attempt to carrier unlock");
                    }
                }
                throw new RuntimeException("Failed to set carrier OEM unlock state");
            }
            Slog.i(TAG, "Updated carrier allows OEM lock state to: " + allowed);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to set carrier state with HAL", e);
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.oemlock.OemLock
    public boolean isOemUnlockAllowedByCarrier() {
        Integer[] requestStatus = new Integer[1];
        Boolean[] allowedByCarrier = new Boolean[1];
        try {
            this.mOemLock.isOemUnlockAllowedByCarrier(new IOemLock.isOemUnlockAllowedByCarrierCallback(requestStatus, allowedByCarrier) {
                /* class com.android.server.oemlock.$$Lambda$VendorLock$HjegvthxXAHFarV_FukbaMGePGU */
                private final /* synthetic */ Integer[] f$0;
                private final /* synthetic */ Boolean[] f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // android.hardware.oemlock.V1_0.IOemLock.isOemUnlockAllowedByCarrierCallback
                public final void onValues(int i, boolean z) {
                    VendorLock.lambda$isOemUnlockAllowedByCarrier$1(this.f$0, this.f$1, i, z);
                }
            });
            int intValue = requestStatus[0].intValue();
            if (intValue == 0) {
                return allowedByCarrier[0].booleanValue();
            }
            if (intValue != 1) {
                Slog.e(TAG, "Unknown return value indicates code is out of sync with HAL");
            }
            throw new RuntimeException("Failed to get carrier OEM unlock state");
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to get carrier state from HAL");
            throw e.rethrowFromSystemServer();
        }
    }

    static /* synthetic */ void lambda$isOemUnlockAllowedByCarrier$1(Integer[] requestStatus, Boolean[] allowedByCarrier, int status, boolean allowed) {
        requestStatus[0] = Integer.valueOf(status);
        allowedByCarrier[0] = Boolean.valueOf(allowed);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.oemlock.OemLock
    public void setOemUnlockAllowedByDevice(boolean allowedByDevice) {
        try {
            int oemUnlockAllowedByDevice = this.mOemLock.setOemUnlockAllowedByDevice(allowedByDevice);
            if (oemUnlockAllowedByDevice != 0) {
                if (oemUnlockAllowedByDevice != 1) {
                    Slog.e(TAG, "Unknown return value indicates code is out of sync with HAL");
                }
                throw new RuntimeException("Failed to set device OEM unlock state");
            }
            Slog.i(TAG, "Updated device allows OEM lock state to: " + allowedByDevice);
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to set device state with HAL", e);
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.oemlock.OemLock
    public boolean isOemUnlockAllowedByDevice() {
        Integer[] requestStatus = new Integer[1];
        Boolean[] allowedByDevice = new Boolean[1];
        try {
            this.mOemLock.isOemUnlockAllowedByDevice(new IOemLock.isOemUnlockAllowedByDeviceCallback(requestStatus, allowedByDevice) {
                /* class com.android.server.oemlock.$$Lambda$VendorLock$8zNsLj4Jts5XEEk_KIC2zQR29g */
                private final /* synthetic */ Integer[] f$0;
                private final /* synthetic */ Boolean[] f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                @Override // android.hardware.oemlock.V1_0.IOemLock.isOemUnlockAllowedByDeviceCallback
                public final void onValues(int i, boolean z) {
                    VendorLock.lambda$isOemUnlockAllowedByDevice$2(this.f$0, this.f$1, i, z);
                }
            });
            int intValue = requestStatus[0].intValue();
            if (intValue == 0) {
                return allowedByDevice[0].booleanValue();
            }
            if (intValue != 1) {
                Slog.e(TAG, "Unknown return value indicates code is out of sync with HAL");
            }
            throw new RuntimeException("Failed to get device OEM unlock state");
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to get devie state from HAL");
            throw e.rethrowFromSystemServer();
        }
    }

    static /* synthetic */ void lambda$isOemUnlockAllowedByDevice$2(Integer[] requestStatus, Boolean[] allowedByDevice, int status, boolean allowed) {
        requestStatus[0] = Integer.valueOf(status);
        allowedByDevice[0] = Boolean.valueOf(allowed);
    }

    private ArrayList<Byte> toByteArrayList(byte[] data) {
        if (data == null) {
            return new ArrayList<>();
        }
        ArrayList<Byte> result = new ArrayList<>(data.length);
        for (byte b : data) {
            result.add(Byte.valueOf(b));
        }
        return result;
    }
}
