package android.app;

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
    private final Map<String, LocalActivityRecord> mActivities = new HashMap();
    private final ArrayList<LocalActivityRecord> mActivityArray = new ArrayList<>();
    private final ActivityThread mActivityThread = ActivityThread.currentActivityThread();
    private int mCurState = 1;
    private boolean mFinishing;
    private final Activity mParent;
    private LocalActivityRecord mResumed;
    private boolean mSingleMode;

    private static class LocalActivityRecord extends Binder {
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

    private void moveToState(LocalActivityRecord r, int desiredState) {
        LocalActivityRecord localActivityRecord = r;
        int i = desiredState;
        if (localActivityRecord.curState != 0 && localActivityRecord.curState != 5) {
            PendingTransactionActions pendingActions = null;
            if (localActivityRecord.curState == 1) {
                HashMap<String, Object> lastNonConfigurationInstances = this.mParent.getLastNonConfigurationChildInstances();
                Object instanceObj = null;
                if (lastNonConfigurationInstances != null) {
                    instanceObj = lastNonConfigurationInstances.get(localActivityRecord.id);
                }
                Object instanceObj2 = instanceObj;
                Activity.NonConfigurationInstances instance = null;
                if (instanceObj2 != null) {
                    instance = new Activity.NonConfigurationInstances();
                    instance.activity = instanceObj2;
                }
                Activity.NonConfigurationInstances instance2 = instance;
                if (localActivityRecord.activityInfo == null) {
                    localActivityRecord.activityInfo = this.mActivityThread.resolveActivityInfo(localActivityRecord.intent);
                }
                Object obj = instanceObj2;
                localActivityRecord.activity = this.mActivityThread.startActivityNow(this.mParent, localActivityRecord.id, localActivityRecord.intent, localActivityRecord.activityInfo, localActivityRecord, localActivityRecord.instanceState, instance2);
                if (localActivityRecord.activity != null) {
                    localActivityRecord.window = localActivityRecord.activity.getWindow();
                    localActivityRecord.instanceState = null;
                    ActivityThread.ActivityClientRecord clientRecord = this.mActivityThread.getActivityClient(localActivityRecord);
                    if (!localActivityRecord.activity.mFinished) {
                        pendingActions = new PendingTransactionActions();
                        pendingActions.setOldState(clientRecord.state);
                        pendingActions.setRestoreInstanceState(true);
                        pendingActions.setCallOnPostCreate(true);
                    }
                    this.mActivityThread.handleStartActivity(clientRecord, pendingActions);
                    localActivityRecord.curState = 3;
                    if (i == 4) {
                        this.mActivityThread.performResumeActivity(localActivityRecord, true, "moveToState-INITIALIZING");
                        localActivityRecord.curState = 4;
                    }
                    return;
                }
                return;
            }
            switch (localActivityRecord.curState) {
                case 2:
                    if (i == 3) {
                        this.mActivityThread.performRestartActivity(localActivityRecord, true);
                        localActivityRecord.curState = 3;
                    }
                    if (i == 4) {
                        this.mActivityThread.performRestartActivity(localActivityRecord, true);
                        this.mActivityThread.performResumeActivity(localActivityRecord, true, "moveToState-CREATED");
                        localActivityRecord.curState = 4;
                    }
                    return;
                case 3:
                    if (i == 4) {
                        this.mActivityThread.performResumeActivity(localActivityRecord, true, "moveToState-STARTED");
                        localActivityRecord.instanceState = null;
                        localActivityRecord.curState = 4;
                    }
                    if (i == 2) {
                        this.mActivityThread.performStopActivity(localActivityRecord, false, "moveToState-STARTED");
                        localActivityRecord.curState = 2;
                    }
                    return;
                case 4:
                    if (i == 3) {
                        performPause(localActivityRecord, this.mFinishing);
                        localActivityRecord.curState = 3;
                    }
                    if (i == 2) {
                        performPause(localActivityRecord, this.mFinishing);
                        this.mActivityThread.performStopActivity(localActivityRecord, false, "moveToState-RESUMED");
                        localActivityRecord.curState = 2;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private void performPause(LocalActivityRecord r, boolean finishing) {
        boolean needState = r.instanceState == null;
        Bundle instanceState = this.mActivityThread.performPauseActivity((IBinder) r, finishing, "performPause", (PendingTransactionActions) null);
        if (needState) {
            r.instanceState = instanceState;
        }
    }

    public Window startActivity(String id, Intent intent) {
        if (this.mCurState != 1) {
            boolean adding = false;
            boolean sameIntent = false;
            ActivityInfo aInfo = null;
            LocalActivityRecord r = this.mActivities.get(id);
            if (r == null) {
                r = new LocalActivityRecord(id, intent);
                adding = true;
            } else if (r.intent != null) {
                sameIntent = r.intent.filterEquals(intent);
                if (sameIntent) {
                    aInfo = r.activityInfo;
                }
            }
            if (aInfo == null) {
                aInfo = this.mActivityThread.resolveActivityInfo(intent);
            }
            if (this.mSingleMode) {
                LocalActivityRecord old = this.mResumed;
                if (!(old == null || old == r || this.mCurState != 4)) {
                    moveToState(old, 3);
                }
            }
            if (adding) {
                this.mActivities.put(id, r);
                this.mActivityArray.add(r);
            } else if (r.activityInfo != null) {
                if (aInfo == r.activityInfo || (aInfo.name.equals(r.activityInfo.name) && aInfo.packageName.equals(r.activityInfo.packageName))) {
                    if (aInfo.launchMode != 0 || (intent.getFlags() & 536870912) != 0) {
                        ArrayList arrayList = new ArrayList(1);
                        arrayList.add(new ReferrerIntent(intent, this.mParent.getPackageName()));
                        this.mActivityThread.performNewIntents(r, arrayList, false);
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
        if (this.mResumed != null) {
            return this.mResumed.activity;
        }
        return null;
    }

    public String getCurrentId() {
        if (this.mResumed != null) {
            return this.mResumed.id;
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
        if (!this.mSingleMode) {
            int N = this.mActivityArray.size();
            for (int i = 0; i < N; i++) {
                moveToState(this.mActivityArray.get(i), 4);
            }
        } else if (this.mResumed != null) {
            moveToState(this.mResumed, 4);
        }
    }

    public void dispatchPause(boolean finishing) {
        if (finishing) {
            this.mFinishing = true;
        }
        this.mCurState = 3;
        if (!this.mSingleMode) {
            int N = this.mActivityArray.size();
            for (int i = 0; i < N; i++) {
                LocalActivityRecord r = this.mActivityArray.get(i);
                if (r.curState == 4) {
                    moveToState(r, 3);
                }
            }
        } else if (this.mResumed != null) {
            moveToState(this.mResumed, 3);
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
        HashMap<String, Object> instanceMap = null;
        int N = this.mActivityArray.size();
        for (int i = 0; i < N; i++) {
            LocalActivityRecord r = this.mActivityArray.get(i);
            if (!(r == null || r.activity == null)) {
                Object instance = r.activity.onRetainNonConfigurationInstance();
                if (instance != null) {
                    if (instanceMap == null) {
                        instanceMap = new HashMap<>();
                    }
                    instanceMap.put(r.id, instance);
                }
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
}
