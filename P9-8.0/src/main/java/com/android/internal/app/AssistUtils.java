package com.android.internal.app;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.internal.R;
import com.android.internal.app.IVoiceInteractionManagerService.Stub;

public class AssistUtils {
    private static final String TAG = "AssistUtils";
    private final Context mContext;
    private final IVoiceInteractionManagerService mVoiceInteractionManagerService = Stub.asInterface(ServiceManager.getService("voiceinteraction"));

    public AssistUtils(Context context) {
        this.mContext = context;
    }

    public boolean showSessionForActiveService(Bundle args, int sourceFlags, IVoiceInteractionSessionShowCallback showCallback, IBinder activityToken) {
        try {
            if (this.mVoiceInteractionManagerService != null) {
                return this.mVoiceInteractionManagerService.showSessionForActiveService(args, sourceFlags, showCallback, activityToken);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to call showSessionForActiveService", e);
        }
        return false;
    }

    public void launchVoiceAssistFromKeyguard() {
        try {
            if (this.mVoiceInteractionManagerService != null) {
                this.mVoiceInteractionManagerService.launchVoiceAssistFromKeyguard();
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to call launchVoiceAssistFromKeyguard", e);
        }
    }

    public boolean activeServiceSupportsAssistGesture() {
        boolean z = false;
        try {
            if (this.mVoiceInteractionManagerService != null) {
                z = this.mVoiceInteractionManagerService.activeServiceSupportsAssist();
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to call activeServiceSupportsAssistGesture", e);
            return false;
        }
    }

    public boolean activeServiceSupportsLaunchFromKeyguard() {
        boolean z = false;
        try {
            if (this.mVoiceInteractionManagerService != null) {
                z = this.mVoiceInteractionManagerService.activeServiceSupportsLaunchFromKeyguard();
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to call activeServiceSupportsLaunchFromKeyguard", e);
            return false;
        }
    }

    public ComponentName getActiveServiceComponentName() {
        try {
            if (this.mVoiceInteractionManagerService != null) {
                return this.mVoiceInteractionManagerService.getActiveServiceComponentName();
            }
            return null;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to call getActiveServiceComponentName", e);
            return null;
        }
    }

    public boolean isSessionRunning() {
        boolean z = false;
        try {
            if (this.mVoiceInteractionManagerService != null) {
                z = this.mVoiceInteractionManagerService.isSessionRunning();
            }
            return z;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to call isSessionRunning", e);
            return false;
        }
    }

    public void hideCurrentSession() {
        try {
            if (this.mVoiceInteractionManagerService != null) {
                this.mVoiceInteractionManagerService.hideCurrentSession();
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to call hideCurrentSession", e);
        }
    }

    public void onLockscreenShown() {
        try {
            if (this.mVoiceInteractionManagerService != null) {
                this.mVoiceInteractionManagerService.onLockscreenShown();
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to call onLockscreenShown", e);
        }
    }

    public void registerVoiceInteractionSessionListener(IVoiceInteractionSessionListener listener) {
        try {
            if (this.mVoiceInteractionManagerService != null) {
                this.mVoiceInteractionManagerService.registerVoiceInteractionSessionListener(listener);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to register voice interaction listener", e);
        }
    }

    public ComponentName getAssistComponentForUser(int userId) {
        String setting = Secure.getStringForUser(this.mContext.getContentResolver(), Secure.ASSISTANT, userId);
        if (setting != null) {
            return ComponentName.unflattenFromString(setting);
        }
        if (activeServiceSupportsAssistGesture()) {
            return getActiveServiceComponentName();
        }
        ResolveInfo info = this.mContext.getPackageManager().resolveActivityAsUser(((SearchManager) this.mContext.getSystemService("search")).getAssistIntent(false), 65536, userId);
        if (info != null) {
            return new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
        }
        return null;
    }

    public static boolean isPreinstalledAssistant(Context context, ComponentName assistant) {
        if (assistant == null) {
            return false;
        }
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(assistant.getPackageName(), 0);
            return !applicationInfo.isSystemApp() ? applicationInfo.isUpdatedSystemApp() : true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private static boolean isDisclosureEnabled(Context context) {
        return Secure.getInt(context.getContentResolver(), Secure.ASSIST_DISCLOSURE_ENABLED, 0) != 0;
    }

    public static boolean shouldDisclose(Context context, ComponentName assistant) {
        boolean z = true;
        if (!allowDisablingAssistDisclosure(context)) {
            return true;
        }
        if (!isDisclosureEnabled(context)) {
            z = isPreinstalledAssistant(context, assistant) ^ 1;
        }
        return z;
    }

    public static boolean allowDisablingAssistDisclosure(Context context) {
        return context.getResources().getBoolean(R.bool.config_allowDisablingAssistDisclosure);
    }
}
