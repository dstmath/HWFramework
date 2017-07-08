package com.android.server.am;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import java.util.Set;

public class HwActivityRecord extends ActivityRecord {
    private boolean mSplitMode;

    public HwActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityContainer container, ActivityOptions options, ActivityRecord sourceRecord) {
        super(_service, _caller, _launchedFromUid, _launchedFromPackage, _intent, _resolvedType, aInfo, _configuration, _resultTo, _resultWho, _reqCode, _componentSpecified, _rootVoiceInteraction, supervisor, container, options, sourceRecord);
    }

    private boolean isLaunchIntentForCamera(String shortComponentName, Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories == null || ((!categories.contains("android.intent.category.INFO") && !categories.contains("android.intent.category.LAUNCHER")) || shortComponentName == null || !shortComponentName.contains("camera"))) {
            return false;
        }
        return true;
    }

    private boolean isChooserActivity(Intent aIntent) {
        if (aIntent == null || aIntent.getComponent() == null || aIntent.getComponent().getClassName() == null || !"com.huawei.android.internal.app.HwResolverActivity".equalsIgnoreCase(aIntent.getComponent().getClassName())) {
            return false;
        }
        return true;
    }

    void scheduleMultiWindowModeChanged() {
        super.scheduleMultiWindowModeChanged();
        this.service.onMultiWindowModeChanged(!this.task.mFullscreen);
    }

    protected void initSplitMode(Intent intent) {
        boolean z = false;
        if (intent != null) {
            if ((intent.getHwFlags() & 4) != 0) {
                z = true;
            }
            this.mSplitMode = z;
        }
    }

    protected boolean isSplitMode() {
        return this.mSplitMode;
    }
}
