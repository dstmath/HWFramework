package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.ActivityThread;
import android.app.servertransaction.PendingTransactionActions;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import com.android.internal.content.ReferrerIntent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class LocalActivityManager {
    static final int CREATED = 2;
    static final int DESTROYED = 5;
    static final int INITIALIZING = 1;
    static final int RESTORED = 0;
    static final int RESUMED = 4;
    static final int STARTED = 3;
    private static final String TAG = "LocalActivityManager";
    private static final boolean localLOGV = false;
    @UnsupportedAppUsage
    private final Map<String, LocalActivityRecord> mActivities = new HashMap();
    @UnsupportedAppUsage
    private final ArrayList<LocalActivityRecord> mActivityArray = new ArrayList<>();
    private final ActivityThread mActivityThread = ActivityThread.currentActivityThread();
    private int mCurState = 1;
    private boolean mFinishing;
    @UnsupportedAppUsage
    private final Activity mParent;
    @UnsupportedAppUsage
    private LocalActivityRecord mResumed;
    @UnsupportedAppUsage
    private boolean mSingleMode;

    /* access modifiers changed from: private */
    public static class LocalActivityRecord extends Binder {
        Activity activity;
        ActivityInfo activityInfo;
        int curState = 0;
        final String id;
        Bundle instanceState;
        Intent intent;
        Window window;

        LocalActivityRecord(String _id, Intent _intent) {
            this.id = _id;
            this.intent = _intent;
        }
    }

    public LocalActivityManager(Activity parent, boolean singleMode) {
        this.mParent = parent;
        this.mSingleMode = singleMode;
    }

    @UnsupportedAppUsage
    private void moveToState(LocalActivityRecord r, int desiredState) {
        Object instanceObj;
        Activity.NonConfigurationInstances instance;
        PendingTransactionActions pendingActions;
        if (r.curState != 0 && r.curState != 5) {
            if (r.curState == 1) {
                HashMap<String, Object> lastNonConfigurationInstances = this.mParent.getLastNonConfigurationChildInstances();
                if (lastNonConfigurationInstances != null) {
                    instanceObj = lastNonConfigurationInstances.get(r.id);
                } else {
                    instanceObj = null;
                }
                if (instanceObj != null) {
                    Activity.NonConfigurationInstances instance2 = new Activity.NonConfigurationInstances();
                    instance2.activity = instanceObj;
                    instance = instance2;
                } else {
                    instance = null;
                }
                if (r.activityInfo == null) {
                    r.activityInfo = this.mActivityThread.resolveActivityInfo(r.intent);
                }
                r.activity = this.mActivityThread.startActivityNow(this.mParent, r.id, r.intent, r.activityInfo, r, r.instanceState, instance, r);
                if (r.activity != null) {
                    r.window = r.activity.getWindow();
                    r.instanceState = null;
                    ActivityThread.ActivityClientRecord clientRecord = this.mActivityThread.getActivityClient(r);
                    if (!r.activity.mFinished) {
                        pendingActions = new PendingTransactionActions();
                        pendingActions.setOldState(clientRecord.state);
                        pendingActions.setRestoreInstanceState(true);
                        pendingActions.setCallOnPostCreate(true);
                    } else {
                        pendingActions = null;
                    }
                    this.mActivityThread.handleStartActivity(clientRecord, pendingActions);
                    r.curState = 3;
                    if (desiredState == 4) {
                        this.mActivityThread.performResumeActivity(r, true, "moveToState-INITIALIZING");
                        r.curState = 4;
                        return;
                    }
                    return;
                }
                return;
            }
            int i = r.curState;
            if (i == 2) {
                if (desiredState == 3) {
                    this.mActivityThread.performRestartActivity(r, true);
                    r.curState = 3;
                }
                if (desiredState == 4) {
                    this.mActivityThread.performRestartActivity(r, true);
                    this.mActivityThread.performResumeActivity(r, true, "moveToState-CREATED");
                    r.curState = 4;
                }
            } else if (i == 3) {
                if (desiredState == 4) {
                    this.mActivityThread.performResumeActivity(r, true, "moveToState-STARTED");
                    r.instanceState = null;
                    r.curState = 4;
                }
                if (desiredState == 2) {
                    this.mActivityThread.performStopActivity(r, false, "moveToState-STARTED");
                    r.curState = 2;
                }
            } else if (i == 4) {
                if (desiredState == 3) {
                    performPause(r, this.mFinishing);
                    r.curState = 3;
                }
                if (desiredState == 2) {
                    performPause(r, this.mFinishing);
                    this.mActivityThread.performStopActivity(r, false, "moveToState-RESUMED");
                    r.curState = 2;
                }
            }
        }
    }

    private void performPause(LocalActivityRecord r, boolean finishing) {
        boolean needState = r.instanceState == null;
        Bundle instanceState = this.mActivityThread.performPauseActivity(r, finishing, "performPause", (PendingTransactionActions) null);
        if (needState) {
            r.instanceState = instanceState;
        }
    }

    public Window startActivity(String id, Intent intent) {
        LocalActivityRecord old;
        if (this.mCurState != 1) {
            boolean adding = false;
            boolean sameIntent = false;
            ActivityInfo aInfo = null;
            LocalActivityRecord r = this.mActivities.get(id);
            if (r == null) {
                r = new LocalActivityRecord(id, intent);
                adding = true;
            } else if (r.intent != null && (sameIntent = r.intent.filterEquals(intent))) {
                aInfo = r.activityInfo;
            }
            if (aInfo == null) {
                aInfo = this.mActivityThread.resolveActivityInfo(intent);
            }
            if (this.mSingleMode && (old = this.mResumed) != null && old != r && this.mCurState == 4) {
                moveToState(old, 3);
            }
            if (adding) {
                this.mActivities.put(id, r);
                this.mActivityArray.add(r);
            } else if (r.activityInfo != null) {
                if (aInfo == r.activityInfo || (aInfo.name.equals(r.activityInfo.name) && aInfo.packageName.equals(r.activityInfo.packageName))) {
                    if (aInfo.launchMode != 0 || (intent.getFlags() & 536870912) != 0) {
                        ArrayList<ReferrerIntent> intents = new ArrayList<>(1);
                        intents.add(new ReferrerIntent(intent, this.mParent.getPackageName()));
                        this.mActivityThread.handleNewIntent(r, intents);
                        r.intent = intent;
                        moveToState(r, this.mCurState);
                        if (this.mSingleMode) {
                            this.mResumed = r;
                        }
                        return r.window;
                    } else if (sameIntent && (intent.getFlags() & 67108864) == 0) {
                        r.intent = intent;
                        moveToState(r, this.mCurState);
                        if (this.mSingleMode) {
                            this.mResumed = r;
                        }
                        return r.window;
                    }
                }
                performDestroy(r, true);
            }
            r.intent = intent;
            r.curState = 1;
            r.activityInfo = aInfo;
            moveToState(r, this.mCurState);
            if (this.mSingleMode) {
                this.mResumed = r;
            }
            return r.window;
        }
        throw new IllegalStateException("Activities can't be added until the containing group has been created.");
    }

    private Window performDestroy(LocalActivityRecord r, boolean finish) {
        Window win = r.window;
        if (r.curState == 4 && !finish) {
            performPause(r, finish);
        }
        this.mActivityThread.performDestroyActivity(r, finish, 0, false, "LocalActivityManager::performDestroy");
        r.activity = null;
        r.window = null;
        if (finish) {
            r.instanceState = null;
        }
        r.curState = 5;
        return win;
    }

    public Window destroyActivity(String id, boolean finish) {
        LocalActivityRecord r = this.mActivities.get(id);
        Window win = null;
        if (r != null) {
            win = performDestroy(r, finish);
            if (finish) {
                this.mActivities.remove(id);
                this.mActivityArray.remove(r);
            }
        }
        return win;
    }

    public Activity getCurrentActivity() {
        LocalActivityRecord localActivityRecord = this.mResumed;
        if (localActivityRecord != null) {
            return localActivityRecord.activity;
        }
        return null;
    }

    public String getCurrentId() {
        LocalActivityRecord localActivityRecord = this.mResumed;
        if (localActivityRecord != null) {
            return localActivityRecord.id;
        }
        return null;
    }

    public Activity getActivity(String id) {
        LocalActivityRecord r = this.mActivities.get(id);
        if (r != null) {
            return r.activity;
        }
        return null;
    }

    public void dispatchCreate(Bundle state) {
        if (state != null) {
            for (String id : state.keySet()) {
                try {
                    Bundle astate = state.getBundle(id);
                    LocalActivityRecord r = this.mActivities.get(id);
                    if (r != null) {
                        r.instanceState = astate;
                    } else {
                        LocalActivityRecord r2 = new LocalActivityRecord(id, null);
                        r2.instanceState = astate;
                        this.mActivities.put(id, r2);
                        this.mActivityArray.add(r2);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception thrown when restoring LocalActivityManager state", e);
                }
            }
        }
        this.mCurState = 2;
    }

    public Bundle saveInstanceState() {
        Bundle state = null;
        int N = this.mActivityArray.size();
        for (int i = 0; i < N; i++) {
            LocalActivityRecord r = this.mActivityArray.get(i);
            if (state == null) {
                state = new Bundle();
            }
            if ((r.instanceState != null || r.curState == 4) && r.activity != null) {
                Bundle childState = new Bundle();
                r.activity.performSaveInstanceState(childState);
                r.instanceState = childState;
            }
            if (r.instanceState != null) {
                state.putBundle(r.id, r.instanceState);
            }
        }
        return state;
    }

    public void dispatchResume() {
        this.mCurState = 4;
        if (this.mSingleMode) {
            LocalActivityRecord localActivityRecord = this.mResumed;
            if (localActivityRecord != null) {
                moveToState(localActivityRecord, 4);
                return;
            }
            return;
        }
        int N = this.mActivityArray.size();
        for (int i = 0; i < N; i++) {
            moveToState(this.mActivityArray.get(i), 4);
        }
    }

    public void dispatchPause(boolean finishing) {
        if (finishing) {
            this.mFinishing = true;
        }
        this.mCurState = 3;
        if (this.mSingleMode) {
            LocalActivityRecord localActivityRecord = this.mResumed;
            if (localActivityRecord != null) {
                moveToState(localActivityRecord, 3);
                return;
            }
            return;
        }
        int N = this.mActivityArray.size();
        for (int i = 0; i < N; i++) {
            LocalActivityRecord r = this.mActivityArray.get(i);
            if (r.curState == 4) {
                moveToState(r, 3);
            }
        }
    }

    public void dispatchStop() {
        this.mCurState = 2;
        int N = this.mActivityArray.size();
        for (int i = 0; i < N; i++) {
            moveToState(this.mActivityArray.get(i), 2);
        }
    }

    public HashMap<String, Object> dispatchRetainNonConfigurationInstance() {
        Object instance;
        HashMap<String, Object> instanceMap = null;
        int N = this.mActivityArray.size();
        for (int i = 0; i < N; i++) {
            LocalActivityRecord r = this.mActivityArray.get(i);
            if (!(r == null || r.activity == null || (instance = r.activity.onRetainNonConfigurationInstance()) == null)) {
                if (instanceMap == null) {
                    instanceMap = new HashMap<>();
                }
                instanceMap.put(r.id, instance);
            }
        }
        return instanceMap;
    }

    public void removeAllActivities() {
        dispatchDestroy(true);
    }

    public void dispatchDestroy(boolean finishing) {
        int N = this.mActivityArray.size();
        for (int i = 0; i < N; i++) {
            this.mActivityThread.performDestroyActivity(this.mActivityArray.get(i), finishing, 0, false, "LocalActivityManager::dispatchDestroy");
        }
        this.mActivities.clear();
        this.mActivityArray.clear();
    }

    public static boolean isLocalActivityRecord(IBinder token) {
        return token instanceof LocalActivityRecord;
    }
}
