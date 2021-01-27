package android.app.admin;

import android.content.Intent;
import java.util.List;

public abstract class DevicePolicyManagerInternal {

    public interface OnCrossProfileWidgetProvidersChangeListener {
        void onCrossProfileWidgetProvidersChanged(int i, List<String> list);
    }

    public abstract void addOnCrossProfileWidgetProvidersChangeListener(OnCrossProfileWidgetProvidersChangeListener onCrossProfileWidgetProvidersChangeListener);

    public abstract boolean canSilentlyInstallPackage(String str, int i);

    public abstract boolean canUserHaveUntrustedCredentialReset(int i);

    public abstract Intent createShowAdminSupportIntent(int i, boolean z);

    public abstract Intent createUserRestrictionSupportIntent(int i, String str);

    public abstract List<String> getCrossProfileWidgetProviders(int i);

    /* access modifiers changed from: protected */
    public abstract DevicePolicyCache getDevicePolicyCache();

    public abstract CharSequence getPrintingDisabledReasonForUser(int i);

    public abstract boolean isActiveAdminWithPolicy(int i, int i2);

    public abstract boolean isUserAffiliatedWithDevice(int i);

    public abstract void reportSeparateProfileChallengeChanged(int i);
}
