package com.android.internal.app;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.service.resolver.IResolverRankerResult;
import android.service.resolver.IResolverRankerResult.Stub;
import android.service.resolver.IResolverRankerService;
import android.service.resolver.ResolverRankerService;
import android.service.resolver.ResolverTarget;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.app.ResolverActivity.ResolvedComponentInfo;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class ResolverComparator implements Comparator<ResolvedComponentInfo> {
    private static final int CONNECTION_COST_TIMEOUT_MILLIS = 200;
    private static final boolean DEBUG = false;
    private static final int NUM_OF_TOP_ANNOTATIONS_TO_USE = 3;
    private static final float RECENCY_MULTIPLIER = 2.0f;
    private static final long RECENCY_TIME_PERIOD = 43200000;
    private static final int RESOLVER_RANKER_RESULT_TIMEOUT = 1;
    private static final int RESOLVER_RANKER_SERVICE_RESULT = 0;
    private static final String TAG = "ResolverComparator";
    private static final long USAGE_STATS_PERIOD = 604800000;
    private static final int WATCHDOG_TIMEOUT_MILLIS = 500;
    private String mAction;
    private AfterCompute mAfterCompute;
    private String[] mAnnotations;
    private final Collator mCollator;
    private CountDownLatch mConnectSignal;
    private ResolverRankerServiceConnection mConnection;
    private String mContentType;
    private Context mContext;
    private final long mCurrentTime;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (ResolverComparator.this.mHandler.hasMessages(1)) {
                        if (msg.obj != null) {
                            List<ResolverTarget> receivedTargets = msg.obj;
                            if (receivedTargets == null || ResolverComparator.this.mTargets == null || receivedTargets.size() != ResolverComparator.this.mTargets.size()) {
                                Log.e(ResolverComparator.TAG, "Sizes of sent and received ResolverTargets diff.");
                            } else {
                                int size = ResolverComparator.this.mTargets.size();
                                for (int i = 0; i < size; i++) {
                                    ((ResolverTarget) ResolverComparator.this.mTargets.get(i)).setSelectProbability(((ResolverTarget) receivedTargets.get(i)).getSelectProbability());
                                }
                            }
                        } else {
                            Log.e(ResolverComparator.TAG, "Receiving null prediction results.");
                        }
                        ResolverComparator.this.mHandler.removeMessages(1);
                        ResolverComparator.this.mAfterCompute.afterCompute();
                        return;
                    }
                    return;
                case 1:
                    ResolverComparator.this.mHandler.removeMessages(0);
                    ResolverComparator.this.mAfterCompute.afterCompute();
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    };
    private final boolean mHttp;
    private final Object mLock = new Object();
    private final PackageManager mPm;
    private IResolverRankerService mRanker;
    private final String mReferrerPackage;
    private final long mSinceTime;
    private final Map<String, UsageStats> mStats;
    private ArrayList<ResolverTarget> mTargets;
    private final LinkedHashMap<ComponentName, ResolverTarget> mTargetsDict = new LinkedHashMap();
    private final UsageStatsManager mUsm;

    public interface AfterCompute {
        void afterCompute();
    }

    private class ResolverRankerServiceConnection implements ServiceConnection {
        private final CountDownLatch mConnectSignal;
        public final IResolverRankerResult resolverRankerResult = new Stub() {
            public void sendResult(List<ResolverTarget> targets) throws RemoteException {
                synchronized (ResolverComparator.this.mLock) {
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = targets;
                    ResolverComparator.this.mHandler.sendMessage(msg);
                }
            }
        };

        public ResolverRankerServiceConnection(CountDownLatch connectSignal) {
            this.mConnectSignal = connectSignal;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (ResolverComparator.this.mLock) {
                ResolverComparator.this.mRanker = IResolverRankerService.Stub.asInterface(service);
                this.mConnectSignal.countDown();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (ResolverComparator.this.mLock) {
                destroy();
            }
        }

        public void destroy() {
            synchronized (ResolverComparator.this.mLock) {
                ResolverComparator.this.mRanker = null;
            }
        }
    }

    public ResolverComparator(Context context, Intent intent, String referrerPackage, AfterCompute afterCompute) {
        this.mCollator = Collator.getInstance(context.getResources().getConfiguration().locale);
        String scheme = intent.getScheme();
        this.mHttp = !"http".equals(scheme) ? "https".equals(scheme) : true;
        this.mReferrerPackage = referrerPackage;
        this.mAfterCompute = afterCompute;
        this.mContext = context;
        this.mPm = context.getPackageManager();
        this.mUsm = (UsageStatsManager) context.getSystemService("usagestats");
        this.mCurrentTime = System.currentTimeMillis();
        this.mSinceTime = this.mCurrentTime - 604800000;
        this.mStats = this.mUsm.queryAndAggregateUsageStats(this.mSinceTime, this.mCurrentTime);
        this.mContentType = intent.getType();
        getContentAnnotations(intent);
        this.mAction = intent.getAction();
    }

    public void getContentAnnotations(Intent intent) {
        ArrayList<String> annotations = intent.getStringArrayListExtra("android.intent.extra.CONTENT_ANNOTATIONS");
        if (annotations != null) {
            int size = annotations.size();
            if (size > 3) {
                size = 3;
            }
            this.mAnnotations = new String[size];
            for (int i = 0; i < size; i++) {
                this.mAnnotations[i] = (String) annotations.get(i);
            }
        }
    }

    public void setCallBack(AfterCompute afterCompute) {
        this.mAfterCompute = afterCompute;
    }

    public void compute(List<ResolvedComponentInfo> targets) {
        reset();
        long recentSinceTime = this.mCurrentTime - RECENCY_TIME_PERIOD;
        float mostRecencyScore = 1.0f;
        float mostTimeSpentScore = 1.0f;
        float mostLaunchScore = 1.0f;
        float mostChooserScore = 1.0f;
        for (ResolvedComponentInfo target : targets) {
            ResolverTarget resolverTarget = new ResolverTarget();
            this.mTargetsDict.put(target.name, resolverTarget);
            UsageStats pkStats = (UsageStats) this.mStats.get(target.name.getPackageName());
            if (pkStats != null) {
                if (!(target.name.getPackageName().equals(this.mReferrerPackage) || (isPersistentProcess(target) ^ 1) == 0)) {
                    float recencyScore = (float) Math.max(pkStats.getLastTimeUsed() - recentSinceTime, 0);
                    resolverTarget.setRecencyScore(recencyScore);
                    if (recencyScore > mostRecencyScore) {
                        mostRecencyScore = recencyScore;
                    }
                }
                float timeSpentScore = (float) pkStats.getTotalTimeInForeground();
                resolverTarget.setTimeSpentScore(timeSpentScore);
                if (timeSpentScore > mostTimeSpentScore) {
                    mostTimeSpentScore = timeSpentScore;
                }
                float launchScore = (float) pkStats.mLaunchCount;
                resolverTarget.setLaunchScore(launchScore);
                if (launchScore > mostLaunchScore) {
                    mostLaunchScore = launchScore;
                }
                float chooserScore = 0.0f;
                if (!(pkStats.mChooserCounts == null || this.mAction == null || pkStats.mChooserCounts.get(this.mAction) == null)) {
                    chooserScore = (float) ((Integer) ((ArrayMap) pkStats.mChooserCounts.get(this.mAction)).getOrDefault(this.mContentType, Integer.valueOf(0))).intValue();
                    if (this.mAnnotations != null) {
                        for (Object orDefault : this.mAnnotations) {
                            chooserScore += (float) ((Integer) ((ArrayMap) pkStats.mChooserCounts.get(this.mAction)).getOrDefault(orDefault, Integer.valueOf(0))).intValue();
                        }
                    }
                }
                resolverTarget.setChooserScore(chooserScore);
                if (chooserScore > mostChooserScore) {
                    mostChooserScore = chooserScore;
                }
            }
        }
        this.mTargets = new ArrayList(this.mTargetsDict.values());
        for (ResolverTarget target2 : this.mTargets) {
            float recency = target2.getRecencyScore() / mostRecencyScore;
            setFeatures(target2, RECENCY_MULTIPLIER * (recency * recency), target2.getLaunchScore() / mostLaunchScore, target2.getTimeSpentScore() / mostTimeSpentScore, target2.getChooserScore() / mostChooserScore);
            addDefaultSelectProbability(target2);
        }
        predictSelectProbabilities(this.mTargets);
    }

    public int compare(ResolvedComponentInfo lhsp, ResolvedComponentInfo rhsp) {
        ResolveInfo lhs = lhsp.getResolveInfoAt(0);
        ResolveInfo rhs = rhsp.getResolveInfoAt(0);
        if (lhs.targetUserId != -2) {
            return rhs.targetUserId != -2 ? 0 : 1;
        } else if (rhs.targetUserId != -2) {
            return -1;
        } else {
            if (this.mHttp) {
                boolean lhsSpecific = ResolverActivity.isSpecificUriMatch(lhs.match);
                if (lhsSpecific != ResolverActivity.isSpecificUriMatch(rhs.match)) {
                    return lhsSpecific ? -1 : 1;
                }
            }
            boolean lPinned = lhsp.isPinned();
            boolean rPinned = rhsp.isPinned();
            if (lPinned && (rPinned ^ 1) != 0) {
                return -1;
            }
            if (!lPinned && rPinned) {
                return 1;
            }
            if (!(lPinned || (rPinned ^ 1) == 0 || this.mStats == null)) {
                int selectProbabilityDiff = Float.compare(((ResolverTarget) this.mTargetsDict.get(new ComponentName(rhs.activityInfo.packageName, rhs.activityInfo.name))).getSelectProbability(), ((ResolverTarget) this.mTargetsDict.get(new ComponentName(lhs.activityInfo.packageName, lhs.activityInfo.name))).getSelectProbability());
                if (selectProbabilityDiff != 0) {
                    return selectProbabilityDiff > 0 ? 1 : -1;
                }
            }
            CharSequence sa = lhs.loadLabel(this.mPm);
            if (sa == null) {
                sa = lhs.activityInfo.name;
            }
            CharSequence sb = rhs.loadLabel(this.mPm);
            if (sb == null) {
                sb = rhs.activityInfo.name;
            }
            return this.mCollator.compare(sa.toString().trim(), sb.toString().trim());
        }
    }

    public float getScore(ComponentName name) {
        ResolverTarget target = (ResolverTarget) this.mTargetsDict.get(name);
        if (target != null) {
            return target.getSelectProbability();
        }
        return 0.0f;
    }

    public void updateChooserCounts(String packageName, int userId, String action) {
        if (this.mUsm != null) {
            this.mUsm.reportChooserSelection(packageName, userId, this.mContentType, this.mAnnotations, action);
        }
    }

    public void updateModel(ComponentName componentName) {
        synchronized (this.mLock) {
            if (this.mRanker != null) {
                try {
                    int selectedPos = new ArrayList(this.mTargetsDict.keySet()).indexOf(componentName);
                    if (selectedPos > 0) {
                        this.mRanker.train(this.mTargets, selectedPos);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error in Train: " + e);
                }
            }
        }
        return;
    }

    public void destroy() {
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        if (this.mConnection != null) {
            this.mContext.unbindService(this.mConnection);
            this.mConnection.destroy();
        }
    }

    /* JADX WARNING: Missing block: B:10:0x000f, code:
            r0 = resolveRankerService();
     */
    /* JADX WARNING: Missing block: B:11:0x0013, code:
            if (r0 != null) goto L_0x0019;
     */
    /* JADX WARNING: Missing block: B:12:0x0015, code:
            return;
     */
    /* JADX WARNING: Missing block: B:16:0x0019, code:
            r4.mConnectSignal = new java.util.concurrent.CountDownLatch(1);
            r4.mConnection = new com.android.internal.app.ResolverComparator.ResolverRankerServiceConnection(r4, r4.mConnectSignal);
            r5.bindServiceAsUser(r0, r4.mConnection, 1, android.os.UserHandle.SYSTEM);
     */
    /* JADX WARNING: Missing block: B:17:0x0030, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initRanker(Context context) {
        synchronized (this.mLock) {
            if (this.mConnection == null || this.mRanker == null) {
            }
        }
    }

    private Intent resolveRankerService() {
        Intent intent = new Intent(ResolverRankerService.SERVICE_INTERFACE);
        for (ResolveInfo resolveInfo : this.mPm.queryIntentServices(intent, 0)) {
            if (!(resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.applicationInfo == null)) {
                ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.applicationInfo.packageName, resolveInfo.serviceInfo.name);
                try {
                    if (!ResolverRankerService.BIND_PERMISSION.equals(this.mPm.getServiceInfo(componentName, 0).permission)) {
                        Log.w(TAG, "ResolverRankerService " + componentName + " does not require" + " permission " + ResolverRankerService.BIND_PERMISSION + " - this service will not be queried for ResolverComparator." + " add android:permission=\"" + ResolverRankerService.BIND_PERMISSION + "\"" + " to the <service> tag for " + componentName + " in the manifest.");
                    } else if (this.mPm.checkPermission(ResolverRankerService.HOLD_PERMISSION, resolveInfo.serviceInfo.packageName) != 0) {
                        Log.w(TAG, "ResolverRankerService " + componentName + " does not hold" + " permission " + ResolverRankerService.HOLD_PERMISSION + " - this service will not be queried for ResolverComparator.");
                    } else {
                        intent.setComponent(componentName);
                        return intent;
                    }
                } catch (NameNotFoundException e) {
                    Log.e(TAG, "Could not look up service " + componentName + "; component name not found");
                }
            }
        }
        return null;
    }

    private void startWatchDog(int timeOutLimit) {
        if (this.mHandler == null) {
            Log.d(TAG, "Error: Handler is Null; Needs to be initialized.");
        }
        this.mHandler.sendEmptyMessageDelayed(1, (long) timeOutLimit);
    }

    private void reset() {
        this.mTargetsDict.clear();
        this.mTargets = null;
        startWatchDog(500);
        initRanker(this.mContext);
    }

    private void predictSelectProbabilities(List<ResolverTarget> targets) {
        if (this.mConnection != null) {
            try {
                this.mConnectSignal.await(200, TimeUnit.MILLISECONDS);
                synchronized (this.mLock) {
                    if (this.mRanker != null) {
                        this.mRanker.predict(targets, this.mConnection.resolverRankerResult);
                        return;
                    }
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Error in Wait for Service Connection.");
            } catch (RemoteException e2) {
                Log.e(TAG, "Error in Predict: " + e2);
            }
        } else {
            return;
        }
        this.mAfterCompute.afterCompute();
    }

    private void addDefaultSelectProbability(ResolverTarget target) {
        target.setSelectProbability((float) (1.0d / (Math.exp((double) (1.6568f - ((((target.getLaunchScore() * 2.5543f) + (target.getTimeSpentScore() * 2.8412f)) + (target.getRecencyScore() * 0.269f)) + (target.getChooserScore() * 4.2222f)))) + 1.0d)));
    }

    private void setFeatures(ResolverTarget target, float recencyScore, float launchScore, float timeSpentScore, float chooserScore) {
        target.setRecencyScore(recencyScore);
        target.setLaunchScore(launchScore);
        target.setTimeSpentScore(timeSpentScore);
        target.setChooserScore(chooserScore);
    }

    static boolean isPersistentProcess(ResolvedComponentInfo rci) {
        boolean z = false;
        if (rci == null || rci.getCount() <= 0) {
            return false;
        }
        if ((rci.getResolveInfoAt(0).activityInfo.applicationInfo.flags & 8) != 0) {
            z = true;
        }
        return z;
    }
}
