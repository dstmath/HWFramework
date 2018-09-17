package android.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.security.keymaster.KeymasterDefs;
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
    private final Map<String, LocalActivityRecord> mActivities;
    private final ArrayList<LocalActivityRecord> mActivityArray;
    private final ActivityThread mActivityThread;
    private int mCurState;
    private boolean mFinishing;
    private final Activity mParent;
    private LocalActivityRecord mResumed;
    private boolean mSingleMode;

    private static class LocalActivityRecord extends Binder {
        Activity activity;
        ActivityInfo activityInfo;
        int curState;
        final String id;
        Bundle instanceState;
        Intent intent;
        Window window;

        LocalActivityRecord(String _id, Intent _intent) {
            this.curState = LocalActivityManager.RESTORED;
            this.id = _id;
            this.intent = _intent;
        }
    }

    public LocalActivityManager(Activity parent, boolean singleMode) {
        this.mActivities = new HashMap();
        this.mActivityArray = new ArrayList();
        this.mCurState = INITIALIZING;
        this.mActivityThread = ActivityThread.currentActivityThread();
        this.mParent = parent;
        this.mSingleMode = singleMode;
    }

    private void moveToState(LocalActivityRecord r, int desiredState) {
        if (r.curState != 0 && r.curState != DESTROYED) {
            if (r.curState == INITIALIZING) {
                HashMap<String, Object> lastNonConfigurationInstances = this.mParent.getLastNonConfigurationChildInstances();
                Object instanceObj = null;
                if (lastNonConfigurationInstances != null) {
                    instanceObj = lastNonConfigurationInstances.get(r.id);
                }
                NonConfigurationInstances nonConfigurationInstances = null;
                if (instanceObj != null) {
                    nonConfigurationInstances = new NonConfigurationInstances();
                    nonConfigurationInstances.activity = instanceObj;
                }
                if (r.activityInfo == null) {
                    r.activityInfo = this.mActivityThread.resolveActivityInfo(r.intent);
                }
                r.activity = this.mActivityThread.startActivityNow(this.mParent, r.id, r.intent, r.activityInfo, r, r.instanceState, nonConfigurationInstances);
                if (r.activity != null) {
                    r.window = r.activity.getWindow();
                    r.instanceState = null;
                    r.curState = STARTED;
                    if (desiredState == RESUMED) {
                        this.mActivityThread.performResumeActivity(r, true, "moveToState-INITIALIZING");
                        r.curState = RESUMED;
                    }
                    return;
                }
                return;
            }
            switch (r.curState) {
                case CREATED /*2*/:
                    if (desiredState == STARTED) {
                        this.mActivityThread.performRestartActivity(r);
                        r.curState = STARTED;
                    }
                    if (desiredState == RESUMED) {
                        this.mActivityThread.performRestartActivity(r);
                        this.mActivityThread.performResumeActivity(r, true, "moveToState-CREATED");
                        r.curState = RESUMED;
                    }
                case STARTED /*3*/:
                    if (desiredState == RESUMED) {
                        this.mActivityThread.performResumeActivity(r, true, "moveToState-STARTED");
                        r.instanceState = null;
                        r.curState = RESUMED;
                    }
                    if (desiredState == CREATED) {
                        this.mActivityThread.performStopActivity(r, false, "moveToState-STARTED");
                        r.curState = CREATED;
                    }
                case RESUMED /*4*/:
                    if (desiredState == STARTED) {
                        performPause(r, this.mFinishing);
                        r.curState = STARTED;
                    }
                    if (desiredState == CREATED) {
                        performPause(r, this.mFinishing);
                        this.mActivityThread.performStopActivity(r, false, "moveToState-RESUMED");
                        r.curState = CREATED;
                    }
                default:
            }
        }
    }

    private void performPause(LocalActivityRecord r, boolean finishing) {
        boolean needState = r.instanceState == null;
        Bundle instanceState = this.mActivityThread.performPauseActivity((IBinder) r, finishing, needState, "performPause");
        if (needState) {
            r.instanceState = instanceState;
        }
    }

    public Window startActivity(String id, Intent intent) {
        if (this.mCurState == INITIALIZING) {
            throw new IllegalStateException("Activities can't be added until the containing group has been created.");
        }
        boolean adding = false;
        boolean sameIntent = false;
        ActivityInfo aInfo = null;
        LocalActivityRecord r = (LocalActivityRecord) this.mActivities.get(id);
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
            if (!(old == null || old == r || this.mCurState != RESUMED)) {
                moveToState(old, STARTED);
            }
        }
        if (adding) {
            this.mActivities.put(id, r);
            this.mActivityArray.add(r);
        } else if (r.activityInfo != null) {
            if (aInfo == r.activityInfo || (aInfo.name.equals(r.activityInfo.name) && aInfo.packageName.equals(r.activityInfo.packageName))) {
                if (aInfo.launchMode != 0 || (intent.getFlags() & KeymasterDefs.KM_ENUM_REP) != 0) {
                    ArrayList<ReferrerIntent> intents = new ArrayList(INITIALIZING);
                    intents.add(new ReferrerIntent(intent, this.mParent.getPackageName()));
                    this.mActivityThread.performNewIntents(r, intents);
                    r.intent = intent;
                    moveToState(r, this.mCurState);
                    if (this.mSingleMode) {
                        this.mResumed = r;
                    }
                    return r.window;
                } else if (sameIntent && (intent.getFlags() & StrictMode.PENALTY_DEATH_ON_FILE_URI_EXPOSURE) == 0) {
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
        r.curState = INITIALIZING;
        r.activityInfo = aInfo;
        moveToState(r, this.mCurState);
        if (this.mSingleMode) {
            this.mResumed = r;
        }
        return r.window;
    }

    private Window performDestroy(LocalActivityRecord r, boolean finish) {
        Window win = r.window;
        if (r.curState == RESUMED && !finish) {
            performPause(r, finish);
        }
        this.mActivityThread.performDestroyActivity(r, finish);
        r.activity = null;
        r.window = null;
        if (finish) {
            r.instanceState = null;
        }
        r.curState = DESTROYED;
        return win;
    }

    public Window destroyActivity(String id, boolean finish) {
        LocalActivityRecord r = (LocalActivityRecord) this.mActivities.get(id);
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
        return this.mResumed != null ? this.mResumed.activity : null;
    }

    public String getCurrentId() {
        return this.mResumed != null ? this.mResumed.id : null;
    }

    public Activity getActivity(String id) {
        LocalActivityRecord r = (LocalActivityRecord) this.mActivities.get(id);
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
                    LocalActivityRecord r = (LocalActivityRecord) this.mActivities.get(id);
                    if (r != null) {
                        r.instanceState = astate;
                    } else {
                        r = new LocalActivityRecord(id, null);
                        r.instanceState = astate;
                        this.mActivities.put(id, r);
                        this.mActivityArray.add(r);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception thrown when restoring LocalActivityManager state", e);
                }
            }
        }
        this.mCurState = CREATED;
    }

    public Bundle saveInstanceState() {
        Bundle state = null;
        int N = this.mActivityArray.size();
        for (int i = RESTORED; i < N; i += INITIALIZING) {
            LocalActivityRecord r = (LocalActivityRecord) this.mActivityArray.get(i);
            if (state == null) {
                state = new Bundle();
            }
            if ((r.instanceState != null || r.curState == RESUMED) && r.activity != null) {
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
        this.mCurState = RESUMED;
        if (!this.mSingleMode) {
            int N = this.mActivityArray.size();
            for (int i = RESTORED; i < N; i += INITIALIZING) {
                moveToState((LocalActivityRecord) this.mActivityArray.get(i), RESUMED);
            }
        } else if (this.mResumed != null) {
            moveToState(this.mResumed, RESUMED);
        }
    }

    public void dispatchPause(boolean finishing) {
        if (finishing) {
            this.mFinishing = true;
        }
        this.mCurState = STARTED;
        if (!this.mSingleMode) {
            int N = this.mActivityArray.size();
            for (int i = RESTORED; i < N; i += INITIALIZING) {
                LocalActivityRecord r = (LocalActivityRecord) this.mActivityArray.get(i);
                if (r.curState == RESUMED) {
                    moveToState(r, STARTED);
                }
            }
        } else if (this.mResumed != null) {
            moveToState(this.mResumed, STARTED);
        }
    }

    public void dispatchStop() {
        this.mCurState = CREATED;
        int N = this.mActivityArray.size();
        for (int i = RESTORED; i < N; i += INITIALIZING) {
            moveToState((LocalActivityRecord) this.mActivityArray.get(i), CREATED);
        }
    }

    public HashMap<String, Object> dispatchRetainNonConfigurationInstance() {
        HashMap<String, Object> instanceMap = null;
        int N = this.mActivityArray.size();
        for (int i = RESTORED; i < N; i += INITIALIZING) {
            LocalActivityRecord r = (LocalActivityRecord) this.mActivityArray.get(i);
            if (!(r == null || r.activity == null)) {
                Object instance = r.activity.onRetainNonConfigurationInstance();
                if (instance != null) {
                    if (instanceMap == null) {
                        instanceMap = new HashMap();
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
        for (int i = RESTORED; i < N; i += INITIALIZING) {
            this.mActivityThread.performDestroyActivity((LocalActivityRecord) this.mActivityArray.get(i), finishing);
        }
        this.mActivities.clear();
        this.mActivityArray.clear();
    }
}
