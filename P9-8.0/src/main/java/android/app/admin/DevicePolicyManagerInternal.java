package android.app.admin;

import android.content.Intent;
import java.util.List;

public abstract class DevicePolicyManagerInternal {

    public interface OnCrossProfileWidgetProvidersChangeListener {
        void onCrossProfileWidgetProvidersChanged(int i, List<String> list);
    }

    public abstract void addOnCrossProfileWidgetProvidersChangeListener(OnCrossProfileWidgetProvidersChangeListener onCrossProfileWidgetProvidersChangeListener);

    public abstract Intent createShowAdminSupportIntent(int i, boolean z);

    public abstract Intent createUserRestrictionSupportIntent(int i, String str);

    public abstract List<String> getCrossProfileWidgetProviders(int i);

    public abstract boolean isActiveAdminWithPolicy(int i, int i2);
}
