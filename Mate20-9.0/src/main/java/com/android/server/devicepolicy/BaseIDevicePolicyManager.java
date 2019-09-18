package com.android.server.devicepolicy;

import android.app.admin.IDevicePolicyManager;
import android.content.ComponentName;
import android.os.PersistableBundle;
import android.security.keymaster.KeymasterCertificateChain;
import android.security.keystore.ParcelableKeyGenParameterSpec;
import android.telephony.data.ApnSetting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class BaseIDevicePolicyManager extends IDevicePolicyManager.Stub {
    /* access modifiers changed from: package-private */
    public abstract void handleStartUser(int i);

    /* access modifiers changed from: package-private */
    public abstract void handleStopUser(int i);

    /* access modifiers changed from: package-private */
    public abstract void handleUnlockUser(int i);

    /* access modifiers changed from: package-private */
    public abstract void systemReady(int i);

    BaseIDevicePolicyManager() {
    }

    public void setSystemSetting(ComponentName who, String setting, String value) {
    }

    public void transferOwnership(ComponentName admin, ComponentName target, PersistableBundle bundle) {
    }

    public PersistableBundle getTransferOwnershipBundle() {
        return null;
    }

    public boolean generateKeyPair(ComponentName who, String callerPackage, String algorithm, ParcelableKeyGenParameterSpec keySpec, int idAttestationFlags, KeymasterCertificateChain attestationChain) {
        return false;
    }

    public boolean isUsingUnifiedPassword(ComponentName who) {
        return true;
    }

    public boolean setKeyPairCertificate(ComponentName who, String callerPackage, String alias, byte[] cert, byte[] chain, boolean isUserSelectable) {
        return false;
    }

    public void setStartUserSessionMessage(ComponentName admin, CharSequence startUserSessionMessage) {
    }

    public void setEndUserSessionMessage(ComponentName admin, CharSequence endUserSessionMessage) {
    }

    public String getStartUserSessionMessage(ComponentName admin) {
        return null;
    }

    public String getEndUserSessionMessage(ComponentName admin) {
        return null;
    }

    public List<String> setMeteredDataDisabledPackages(ComponentName admin, List<String> packageNames) {
        return packageNames;
    }

    public List<String> getMeteredDataDisabledPackages(ComponentName admin) {
        return new ArrayList();
    }

    public int addOverrideApn(ComponentName admin, ApnSetting apnSetting) {
        return -1;
    }

    public boolean updateOverrideApn(ComponentName admin, int apnId, ApnSetting apnSetting) {
        return false;
    }

    public boolean removeOverrideApn(ComponentName admin, int apnId) {
        return false;
    }

    public List<ApnSetting> getOverrideApns(ComponentName admin) {
        return Collections.emptyList();
    }

    public void setOverrideApnsEnabled(ComponentName admin, boolean enabled) {
    }

    public boolean isOverrideApnEnabled(ComponentName admin) {
        return false;
    }

    public void clearSystemUpdatePolicyFreezePeriodRecord() {
    }

    public boolean isMeteredDataDisabledPackageForUser(ComponentName admin, String packageName, int userId) {
        return false;
    }

    public long forceSecurityLogs() {
        return 0;
    }

    public void setDefaultSmsApplication(ComponentName admin, String packageName) {
    }
}
