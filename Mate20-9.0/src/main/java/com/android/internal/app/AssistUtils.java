package com.android.internal.app;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.app.IVoiceInteractionManagerService;
import com.android.internal.util.Protocol;

public class AssistUtils {
    private static final String TAG = "AssistUtils";
    private final Context mContext;
    private final IVoiceInteractionManagerService mVoiceInteractionManagerService = IVoiceInteractionManagerService.Stub.asInterface(ServiceManager.getService("voiceinteraction"));

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
            if (this.mVoiceInteractionManagerService != null && this.mVoiceInteractionManagerService.activeServiceSupportsAssist()) {
                z = true;
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
            if (this.mVoiceInteractionManagerService != null && this.mVoiceInteractionManagerService.activeServiceSupportsLaunchFromKeyguard()) {
                z = true;
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
            if (this.mVoiceInteractionManagerService != null && this.mVoiceInteractionManagerService.isSessionRunning()) {
                z = true;
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
        String setting = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "assistant", userId);
        if (setting != null) {
            return ComponentName.unflattenFromString(setting);
        }
        String defaultSetting = this.mContext.getResources().getString(17039783);
        if (defaultSetting != null) {
            return ComponentName.unflattenFromString(defaultSetting);
        }
        if (activeServiceSupportsAssistGesture()) {
            return getActiveServiceComponentName();
        }
        SearchManager searchManager = (SearchManager) this.mContext.getSystemService("search");
        if (searchManager == null) {
            return null;
        }
        ResolveInfo info = this.mContext.getPackageManager().resolveActivityAsUser(searchManager.getAssistIntent(false), Protocol.BASE_SYSTEM_RESERVED, userId);
        if (info != null) {
            return new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
        }
        return null;
    }

    public static boolean isPreinstalledAssistant(Context context, ComponentName assistant) {
        boolean z = false;
        if (assistant == null) {
            return false;
        }
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(assistant.getPackageName(), 0);
            if (applicationInfo.isSystemApp() || applicationInfo.isUpdatedSystemApp()) {
                z = true;
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static boolean isDisclosureEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "assist_disclosure_enabled", 0) != 0;
    }

    public static boolean shouldDisclose(Context context, ComponentName assistant) {
        boolean z = true;
        if (!allowDisablingAssistDisclosure(context)) {
            return true;
        }
        if (!isDisclosureEnabled(context) && isPreinstalledAssistant(context, assistant)) {
            z = false;
        }
        return z;
    }

    public static boolean allowDisablingAssistDisclosure(Context context) {
        return context.getResources().getBoolean(17956873);
    }
}
