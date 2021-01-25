package com.android.server.fingerprint;

import android.content.Context;
import android.hardware.biometrics.IBiometricServiceReceiver;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.IFingerprintServiceReceiver;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.SystemService;
import java.util.Collections;
import java.util.List;

public abstract class AbsFingerprintService extends SystemService {
    public AbsFingerprintService(Context context) {
        super(context);
    }

    public void updateFingerprints(int userId) {
    }

    public boolean shouldAuthBothSpaceFingerprints(String opPackageName, int flags) {
        return true;
    }

    public int removeUserData(int groupId, byte[] path) {
        return 0;
    }

    public boolean onHwTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        return false;
    }

    public boolean checkPrivacySpaceEnroll(int userId, int currentUserId) {
        return false;
    }

    public boolean checkNeedPowerpush() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void stopPickupTrunOff() {
    }

    public void setFingerprintMaskView(Bundle bundle) {
    }

    public void showFingerprintView() {
    }

    public void notifyAuthenticationStarted(String pkgName, IFingerprintServiceReceiver receiver, int flag, int userID, Bundle bundle, IBiometricServiceReceiver dialogReceiver) {
    }

    public void notifyAuthenticationCanceled(String pkgName) {
    }

    public void notifyFingerDown(int type) {
    }

    public void notifyFingerCalibrarion(int value) {
    }

    public void notifyAuthenticationFinished(String opName, int type, int failtime) {
    }

    public void notifyEnrollmentStarted(int flags) {
    }

    public void notifyEnrollmentCanceled() {
    }

    public void notifyCaptureFinished(int type) {
    }

    /* access modifiers changed from: protected */
    public void triggerFaceRecognization() {
    }

    public void notifyEnrollingFingerUp() {
    }

    /* access modifiers changed from: protected */
    public void setKidsFingerprint(int userID, boolean isKeygusrd) {
    }

    /* access modifiers changed from: protected */
    public int sendCommandToHal(int command) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public boolean canUseUdFingerprint(String opPackageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isSupportDualFingerprint() {
        return false;
    }

    /* access modifiers changed from: protected */
    public List<Fingerprint> getEnrolledFingerprintsEx(String opPackageName, int targetDevice, int userId) {
        return Collections.emptyList();
    }

    /* access modifiers changed from: protected */
    public boolean isHardwareDetectedEx(String opPackageName, int targetDevice) {
        return true;
    }

    public void setPowerState(int powerState) {
    }
}
