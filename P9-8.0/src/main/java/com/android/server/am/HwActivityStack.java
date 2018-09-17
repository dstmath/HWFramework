package com.android.server.am;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareConstant.ResourceType;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract.Apps;
import android.rms.iaware.DataContract.Apps.Builder;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Slog;
import android.widget.Toast;
import com.android.server.UiThread;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.wm.IntelliServiceManager;
import java.util.ArrayList;

public class HwActivityStack extends ActivityStack implements IHwActivityStack {
    static final int MAX_TASK_NUM = SystemProperties.getInt("ro.config.pc_mode_win_num", 8);
    private static IBinder mAudioService = null;
    private static final boolean mIsHwNaviBar = SystemProperties.getBoolean("ro.config.hw_navigationbar", false);
    boolean mHiddenFromHome = false;
    private boolean mStackVisible = true;

    public HwActivityStack(ActivityContainer activityContainer, RecentTasks recentTasks, boolean onTop) {
        super(activityContainer, recentTasks, onTop);
    }

    public int getInvalidFlag(int changes, Configuration newConfig, Configuration naviConfig) {
        if (newConfig == null || naviConfig == null) {
            return changes;
        }
        if (mIsHwNaviBar) {
            int newChanges = naviConfig.diff(newConfig);
            if ((newChanges & 1280) == 0) {
                changes &= -1281;
            } else if ((newChanges & 128) != 0) {
                if (changes == 1280 || changes == 1024) {
                    changes &= -1025;
                }
                changes &= -257;
            }
        }
        return changes;
    }

    void moveHomeStackTaskToTop() {
        super.moveHomeStackTaskToTop();
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
            b.transact(1002, _data, _reply, 0);
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

    public boolean isSplitActivity(Intent intent) {
        return (intent == null || (intent.getHwFlags() & 4) == 0) ? false : true;
    }

    public void resumeCustomActivity(ActivityRecord next) {
        if (next != null) {
            this.mService.customActivityResuming(next.packageName);
        }
    }

    void setLaunchTime(ActivityRecord r) {
        super.setLaunchTime(r);
        noteActivityDisplayedStart(r);
    }

    private final void noteActivityDisplayedStart(ActivityRecord r) {
        if (AwareConstant.CURRENT_USER_TYPE == 3 && this.mService.mSystemReady && r != null && r.app != null && r.shortComponentName != null && r.app.pid > 0) {
            HwSysResManager resManager = HwSysResManager.getInstance();
            if (resManager != null && resManager.isResourceNeeded(ResourceType.getReousrceId(ResourceType.RES_APP))) {
                Builder builder = Apps.builder();
                builder.addEvent(15013);
                builder.addActivityDisplayedInfo(r.shortComponentName, r.app.pid, 0);
                CollectData appsData = builder.build();
                long id = Binder.clearCallingIdentity();
                resManager.reportData(appsData);
                Binder.restoreCallingIdentity(id);
                Slog.d(TAG, "EVENT_APP_ACTIVITY_DISPLAYED_BEGIN: " + r.shortComponentName);
            }
        }
    }

    public void makeStackVisible(boolean visible) {
        synchronized (this.mService) {
            this.mStackVisible = visible;
            if (visible) {
                this.mHiddenFromHome = false;
                if (this.mTaskHistory.size() > 0) {
                    this.mService.getHwTaskChangeController().notifyTaskMovedToFront(((TaskRecord) this.mTaskHistory.get(0)).taskId);
                }
            } else {
                this.mService.getHwTaskChangeController().notifyTaskMovedToFront(-1);
            }
        }
    }

    protected boolean moveTaskToBackLocked(int taskId) {
        if (!HwPCUtils.isPcDynamicStack(this.mStackId)) {
            return super.moveTaskToBackLocked(taskId);
        }
        makeStackVisible(false);
        adjustFocusToNextFocusableStackLocked("minTask");
        this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
        return true;
    }

    protected void moveToFront(String reason, TaskRecord task) {
        if (isAttached()) {
            if (HwPCUtils.isPcDynamicStack(this.mStackId)) {
                makeStackVisible(true);
            }
            super.moveToFront(reason, task);
            minimalLRUTaskIfNeed();
        }
    }

    protected void setKeepPortraitFR() {
        IntelliServiceManager.getInstance(this.mService.mContext).setKeepPortrait(true);
    }

    protected int shouldBeVisible(ActivityRecord starting) {
        if (!HwPCUtils.isPcDynamicStack(this.mStackId) || (this.mStackVisible && !this.mTaskHistory.isEmpty())) {
            return super.shouldBeVisible(starting);
        }
        return 0;
    }

    protected void moveTaskToFrontLocked(TaskRecord tr, boolean noAnimation, ActivityOptions options, AppTimeTracker timeTracker, String reason) {
        if (HwPCUtils.isPcDynamicStack(this.mStackId)) {
            makeStackVisible(true);
        }
        super.moveTaskToFrontLocked(tr, noAnimation, options, timeTracker, reason);
        minimalLRUTaskIfNeed();
    }

    private void minimalLRUTaskIfNeed() {
        if (HwPCUtils.isPcDynamicStack(this.mStackId)) {
            ArrayList<ActivityStack> stacks = this.mActivityContainer.mActivityDisplay.mStacks;
            int visibleNum = 0;
            TaskRecord lastVisibleTask = null;
            int N = stacks.size();
            for (int i = 0; i < N; i++) {
                ActivityStack stack = (ActivityStack) stacks.get(i);
                if (stack.shouldBeVisible(null) == 1) {
                    if (lastVisibleTask == null && stack.topTask() != null) {
                        lastVisibleTask = stack.topTask();
                    }
                    if (lastVisibleTask != null) {
                        visibleNum++;
                        if (visibleNum > MAX_TASK_NUM && lastVisibleTask.mStack != null) {
                            HwPCUtils.log(TAG, "max task num, minimial the task: " + lastVisibleTask.taskId);
                            this.mService.moveTaskBackwards(lastVisibleTask.taskId);
                            final Context context = HwPCUtils.getDisplayContext(this.mService.mContext, lastVisibleTask.mStack.mDisplayId);
                            if (context != null) {
                                Object title;
                                ActivityRecord ar = lastVisibleTask.getRootActivity();
                                if (ar == null || ar.info == null) {
                                    title = null;
                                } else {
                                    title = ar.info.loadLabel(context.getPackageManager()).toString();
                                }
                                if (!TextUtils.isEmpty(title)) {
                                    UiThread.getHandler().post(new Runnable() {
                                        public void run() {
                                            Toast.makeText(context, context.getString(33685972, new Object[]{title}), 0).show();
                                        }
                                    });
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                    }
                    return;
                }
            }
        }
    }

    public boolean isVisibleLocked(String packageName) {
        if (packageName == null) {
            return false;
        }
        TaskRecord lastTask = null;
        HwTaskRecord hwTask = null;
        for (int taskNdx = this.mTaskHistory.size() - 1; taskNdx >= 0; taskNdx--) {
            TaskRecord task = (TaskRecord) this.mTaskHistory.get(taskNdx);
            if (task instanceof HwTaskRecord) {
                hwTask = (HwTaskRecord) task;
            }
            if (hwTask != null) {
                ArrayList<ActivityRecord> activities = hwTask.getActivities();
                if (activities != null) {
                    int numActivities = activities.size();
                    for (int activityNdx = 0; activityNdx < numActivities; activityNdx++) {
                        try {
                            ActivityRecord r = (ActivityRecord) activities.get(activityNdx);
                            if (r != null && ((r instanceof HwActivityRecord) ^ 1) == 0 && (packageName.equals(((HwActivityRecord) r).getPackageName()) || r.getTask() == lastTask)) {
                                if (r.visible) {
                                    return true;
                                }
                                if (r.visibleIgnoringKeyguard) {
                                    return true;
                                }
                                lastTask = r.getTask();
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Slog.e(TAG, "IndexOutOfBoundsException: Index: +" + activityNdx + ", Size: " + activities.size());
                        }
                    }
                    continue;
                } else {
                    continue;
                }
            }
        }
        return false;
    }
}
