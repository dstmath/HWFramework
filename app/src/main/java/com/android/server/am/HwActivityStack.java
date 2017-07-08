package com.android.server.am;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.Set;

public class HwActivityStack extends ActivityStack implements IHwActivityStack {
    private static IBinder mAudioService;
    private static final boolean mIsHwNaviBar;

    static {
        mIsHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
        mAudioService = null;
    }

    public HwActivityStack(ActivityContainer activityContainer, RecentTasks recentTasks) {
        super(activityContainer, recentTasks);
    }

    public int getInvalidFlag(int changes, Configuration newConfig, Configuration naviConfig) {
        if (newConfig == null || naviConfig == null) {
            return changes;
        }
        if (mIsHwNaviBar) {
            int newChanges = naviConfig.diff(newConfig);
            if ((newChanges & 1280) == 0) {
                changes &= -1281;
            } else if ((newChanges & HwSecDiagnoseConstant.BIT_VERIFYBOOT) != 0) {
                if (changes == 1280 || changes == HwGlobalActionsData.FLAG_SILENTMODE_NORMAL) {
                    changes &= -1025;
                }
                changes &= -257;
            }
        }
        return changes;
    }

    void moveHomeStackTaskToTop(int homeStackTaskType) {
        super.moveHomeStackTaskToTop(homeStackTaskType);
        this.mService.checkIfScreenStatusRequestAndSendBroadcast();
    }

    private static IBinder getAudioService() {
        if (mAudioService != null) {
            return mAudioService;
        }
        mAudioService = ServiceManager.getService("audio");
        return mAudioService;
    }

    protected int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        int i = 1;
        IBinder b = getAudioService();
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        int _result = 0;
        try {
            _data.writeInterfaceToken("android.media.IAudioService");
            _data.writeInt(restore ? 1 : 0);
            _data.writeString(packageName);
            if (!isOnTop) {
                i = 0;
            }
            _data.writeInt(i);
            _data.writeString(reserved);
            b.transact(EventTracker.TRACK_TYPE_KILL, _data, _reply, 0);
            _reply.readException();
            _result = _reply.readInt();
        } catch (RemoteException e) {
            Slog.e(TAG, "setHeadsetRevertSequenceState transact e: " + e);
        } finally {
            _reply.recycle();
            _data.recycle();
        }
        return _result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean finishDisabledPackageActivitiesLocked(String packageName, Set<String> filterByClasses, boolean doit, boolean evenPersistent, int userId) {
        if (2147383647 != userId) {
            return super.finishDisabledPackageActivitiesLocked(packageName, filterByClasses, doit, evenPersistent, userId);
        }
        boolean didSomething = false;
        TaskRecord lastTask = null;
        ComponentName homeActivity = null;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            ArrayList<ActivityRecord> activities = ((TaskRecord) this.mTaskHistory.get(taskNdx)).mActivities;
            int numActivities = activities.size();
            int activityNdx = 0;
            while (activityNdx < numActivities) {
                boolean sameComponent;
                ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                if (r.packageName.equals(packageName)) {
                    if (filterByClasses != null) {
                    }
                    sameComponent = true;
                    if (r.userId == 0 && ((sameComponent || r.task == r11) && ((r.app == null || evenPersistent || !r.app.persistent) && !(sameComponent && r.multiLaunchId == 0)))) {
                        if (!doit) {
                            if (r.isHomeActivity()) {
                                if (homeActivity == null && homeActivity.equals(r.realActivity)) {
                                    Slog.i(TAG, "Skip force-stop again " + r);
                                } else {
                                    homeActivity = r.realActivity;
                                }
                            }
                            didSomething = true;
                            Slog.i(TAG, "  Force finishing activity " + r);
                            if (sameComponent) {
                                if (r.app != null) {
                                    r.app.removed = true;
                                }
                                r.app = null;
                            }
                            lastTask = r.task;
                            if (finishActivityLocked(r, 0, null, "force-stop", true)) {
                                numActivities--;
                                activityNdx--;
                            }
                        } else if (!r.finishing) {
                            return true;
                        }
                    }
                    activityNdx++;
                }
                sameComponent = packageName == null && r.userId == 0;
                if (!doit) {
                    if (r.isHomeActivity()) {
                        if (homeActivity == null) {
                        }
                        homeActivity = r.realActivity;
                    }
                    didSomething = true;
                    Slog.i(TAG, "  Force finishing activity " + r);
                    if (sameComponent) {
                        if (r.app != null) {
                            r.app.removed = true;
                        }
                        r.app = null;
                    }
                    lastTask = r.task;
                    if (finishActivityLocked(r, 0, null, "force-stop", true)) {
                        numActivities--;
                        activityNdx--;
                    }
                } else if (!r.finishing) {
                    return true;
                }
                activityNdx++;
            }
        }
        return didSomething;
    }

    public boolean isSplitActivity(Intent intent) {
        return (intent == null || (intent.getHwFlags() & 4) == 0) ? false : true;
    }
}
