package com.android.server.am;

import android.app.ActivityManager.TaskDescription;
import android.app.ActivityManager.TaskThumbnailInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.service.voice.IVoiceInteractionSession;
import com.android.internal.app.IVoiceInteractor;
import java.util.ArrayList;

public class HwTaskRecord extends TaskRecord {
    public HwTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, IVoiceInteractionSession _voiceSession, IVoiceInteractor _voiceInteractor) {
        super(service, _taskId, info, _intent, _voiceSession, _voiceInteractor);
    }

    public HwTaskRecord(ActivityManagerService service, int _taskId, ActivityInfo info, Intent _intent, TaskDescription _taskDescription, TaskThumbnailInfo _thumbnailInfo) {
        super(service, _taskId, info, _intent, _taskDescription, _thumbnailInfo);
    }

    public HwTaskRecord(ActivityManagerService service, int _taskId, Intent _intent, Intent _affinityIntent, String _affinity, String _rootAffinity, ComponentName _realActivity, ComponentName _origActivity, boolean _rootWasReset, boolean _autoRemoveRecents, boolean _askedCompatMode, int _taskType, int _userId, int _effectiveUid, String _lastDescription, ArrayList<ActivityRecord> activities, long _firstActiveTime, long _lastActiveTime, long lastTimeMoved, boolean neverRelinquishIdentity, TaskDescription _lastTaskDescription, TaskThumbnailInfo lastThumbnailInfo, int taskAffiliation, int prevTaskId, int nextTaskId, int taskAffiliationColor, int callingUid, String callingPackage, int resizeMode, boolean privileged, boolean _realActivitySuspended, boolean userSetupComplete, int minWidth, int minHeight) {
        super(service, _taskId, _intent, _affinityIntent, _affinity, _rootAffinity, _realActivity, _origActivity, _rootWasReset, _autoRemoveRecents, _askedCompatMode, _taskType, _userId, _effectiveUid, _lastDescription, activities, _firstActiveTime, _lastActiveTime, lastTimeMoved, neverRelinquishIdentity, _lastTaskDescription, lastThumbnailInfo, taskAffiliation, prevTaskId, nextTaskId, taskAffiliationColor, callingUid, callingPackage, resizeMode, privileged, _realActivitySuspended, userSetupComplete, minWidth, minHeight);
    }

    public void overrideConfigOrienForFreeForm(Configuration config) {
        int i = 1;
        ActivityRecord topActivity = getTopActivity();
        if (topActivity != null) {
            if (this.stack.mStackId == 2) {
                ApplicationInfo info = this.mService.getPackageManagerInternalLocked().getApplicationInfo(topActivity.packageName, this.userId);
                if (info != null && (info.flags & 1) != 0) {
                    Resources res = this.mService.mContext.getResources();
                    Configuration serviceConfig = this.mService.mConfiguration;
                    if (res.getConfiguration().orientation == 1) {
                        config.orientation = 1;
                    } else {
                        if (config.screenWidthDp > Math.min(serviceConfig.screenWidthDp, serviceConfig.screenHeightDp)) {
                            i = 2;
                        }
                        config.orientation = i;
                    }
                }
            } else if (!this.mFullscreen && ((this.stack.mStackId == 1 || this.stack.mStackId == 3) && this.mService.getPackageManagerInternalLocked().isInMWPortraitWhiteList(topActivity.packageName))) {
                config.orientation = 1;
            }
        }
    }
}
