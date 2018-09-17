package android.app.admin;

import android.content.Intent;
import java.util.List;

public abstract class DevicePolicyManagerInternal {

    public interface OnCrossProfileWidgetProvidersChangeListener {
        void onCrossProfileWidgetProvidersChanged(int i, List<String> list);
    }

    public abstract void addOnCrossProfileWidgetProvidersChangeListener(OnCrossProfileWidgetProvidersChangeListener onCrossProfileWidgetProvidersChangeListener);

    public abstract Intent createPackageSuspendedDialogIntent(String str, int i);

    public abstract List<String> getCrossProfileWidgetProviders(int i);

    public abstract boolean isActiveAdminWithPolicy(int i, int i2);
}
