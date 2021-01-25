package com.android.server.biometrics.iris;

import android.content.Context;
import android.hardware.biometrics.BiometricAuthenticator;
import com.android.server.biometrics.BiometricServiceBase;
import com.android.server.biometrics.BiometricUtils;
import com.android.server.biometrics.Constants;
import java.util.List;

public class IrisService extends BiometricServiceBase {
    private static final String TAG = "IrisService";

    public IrisService(Context context) {
        super(context);
    }

    @Override // com.android.server.biometrics.BiometricServiceBase, com.android.server.SystemService
    public void onStart() {
        super.onStart();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricServiceBase.DaemonWrapper getDaemonWrapper() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public BiometricUtils getBiometricUtils() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public Constants getConstants() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasReachedEnrollmentLimit(int userId) {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void updateActiveGroup(int userId, String clientPackage) {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getLockoutResetIntent() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getLockoutBroadcastPermission() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public long getHalDeviceId() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean hasEnrolledBiometrics(int userId) {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public String getManageBiometricPermission() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public void checkUseBiometricPermission() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public boolean checkAppOps(int uid, String opPackageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public List<? extends BiometricAuthenticator.Identifier> getEnrolledTemplates(int userId) {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int statsModality() {
        return 2;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.biometrics.BiometricServiceBase
    public int getLockoutMode() {
        return 0;
    }
}
