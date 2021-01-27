package com.android.internal.app;

import android.app.usage.UsageStats;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.metrics.LogMaker;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.resolver.IResolverRankerResult;
import android.service.resolver.IResolverRankerService;
import android.service.resolver.ResolverRankerService;
import android.service.resolver.ResolverTarget;
import android.util.Log;
import com.android.internal.app.AbstractResolverComparator;
import com.android.internal.app.ResolverActivity;
import com.android.internal.logging.MetricsLogger;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/* access modifiers changed from: package-private */
public class ResolverRankerServiceResolverComparator extends AbstractResolverComparator {
    private static final int CONNECTION_COST_TIMEOUT_MILLIS = 200;
    private static final boolean DEBUG = false;
    private static final float RECENCY_MULTIPLIER = 2.0f;
    private static final long RECENCY_TIME_PERIOD = 43200000;
    private static final String TAG = "RRSResolverComparator";
    private static final long USAGE_STATS_PERIOD = 604800000;
    private String mAction;
    private final Collator mCollator;
    private CountDownLatch mConnectSignal;
    private ResolverRankerServiceConnection mConnection;
    private Context mContext;
    private final long mCurrentTime;
    private final Object mLock = new Object();
    private IResolverRankerService mRanker;
    private ComponentName mRankerServiceName;
    private final String mReferrerPackage;
    private ComponentName mResolvedRankerName;
    private final long mSinceTime;
    private final Map<String, UsageStats> mStats;
    private ArrayList<ResolverTarget> mTargets;
    private final LinkedHashMap<ComponentName, ResolverTarget> mTargetsDict = new LinkedHashMap<>();

    public ResolverRankerServiceResolverComparator(Context context, Intent intent, String referrerPackage, AbstractResolverComparator.AfterCompute afterCompute) {
        super(context, intent);
        this.mCollator = Collator.getInstance(context.getResources().getConfiguration().locale);
        this.mReferrerPackage = referrerPackage;
        this.mContext = context;
        this.mCurrentTime = System.currentTimeMillis();
        this.mSinceTime = this.mCurrentTime - 604800000;
        this.mStats = this.mUsm.queryAndAggregateUsageStats(this.mSinceTime, this.mCurrentTime);
        this.mAction = intent.getAction();
        this.mRankerServiceName = new ComponentName(this.mContext, getClass());
        setCallBack(afterCompute);
    }

    @Override // com.android.internal.app.AbstractResolverComparator
    public void handleResultMessage(Message msg) {
        if (msg.what == 0) {
            if (msg.obj == null) {
                Log.e(TAG, "Receiving null prediction results.");
                return;
            }
            List<ResolverTarget> receivedTargets = (List) msg.obj;
            if (receivedTargets == null || this.mTargets == null || receivedTargets.size() != this.mTargets.size()) {
                Log.e(TAG, "Sizes of sent and received ResolverTargets diff.");
                return;
            }
            int size = this.mTargets.size();
            boolean isUpdated = false;
            for (int i = 0; i < size; i++) {
                float predictedProb = receivedTargets.get(i).getSelectProbability();
                if (predictedProb != this.mTargets.get(i).getSelectProbability()) {
                    this.mTargets.get(i).setSelectProbability(predictedProb);
                    isUpdated = true;
                }
            }
            if (isUpdated) {
                this.mRankerServiceName = this.mResolvedRankerName;
            }
        }
    }

    @Override // com.android.internal.app.AbstractResolverComparator
    public void doCompute(List<ResolverActivity.ResolvedComponentInfo> targets) {
        Iterator<ResolverActivity.ResolvedComponentInfo> it;
        long recentSinceTime = this.mCurrentTime - 43200000;
        Iterator<ResolverActivity.ResolvedComponentInfo> it2 = targets.iterator();
        float mostRecencyScore = 1.0f;
        float mostTimeSpentScore = 1.0f;
        float mostLaunchScore = 1.0f;
        float mostChooserScore = 1.0f;
        while (it2.hasNext()) {
            ResolverActivity.ResolvedComponentInfo target = it2.next();
            ResolverTarget resolverTarget = new ResolverTarget();
            this.mTargetsDict.put(target.name, resolverTarget);
            UsageStats pkStats = this.mStats.get(target.name.getPackageName());
            if (pkStats != null) {
                if (target.name.getPackageName().equals(this.mReferrerPackage)) {
                    it = it2;
                } else if (!isPersistentProcess(target)) {
                    it = it2;
                    float recencyScore = (float) Math.max(pkStats.getLastTimeUsed() - recentSinceTime, 0L);
                    resolverTarget.setRecencyScore(recencyScore);
                    if (recencyScore > mostRecencyScore) {
                        mostRecencyScore = recencyScore;
                    }
                } else {
                    it = it2;
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
                if (pkStats.mChooserCounts != null && this.mAction != null) {
                    if (pkStats.mChooserCounts.get(this.mAction) != null) {
                        chooserScore = (float) pkStats.mChooserCounts.get(this.mAction).getOrDefault(this.mContentType, 0).intValue();
                        if (this.mAnnotations != null) {
                            int size = this.mAnnotations.length;
                            int i = 0;
                            while (i < size) {
                                chooserScore += (float) pkStats.mChooserCounts.get(this.mAction).getOrDefault(this.mAnnotations[i], 0).intValue();
                                i++;
                                size = size;
                                pkStats = pkStats;
                            }
                        }
                    }
                }
                resolverTarget.setChooserScore(chooserScore);
                if (chooserScore > mostChooserScore) {
                    mostChooserScore = chooserScore;
                }
            } else {
                it = it2;
            }
            it2 = it;
        }
        this.mTargets = new ArrayList<>(this.mTargetsDict.values());
        Iterator<ResolverTarget> it3 = this.mTargets.iterator();
        while (it3.hasNext()) {
            ResolverTarget target2 = it3.next();
            float recency = target2.getRecencyScore() / mostRecencyScore;
            setFeatures(target2, recency * recency * 2.0f, target2.getLaunchScore() / mostLaunchScore, target2.getTimeSpentScore() / mostTimeSpentScore, target2.getChooserScore() / mostChooserScore);
            addDefaultSelectProbability(target2);
        }
        predictSelectProbabilities(this.mTargets);
    }

    @Override // com.android.internal.app.AbstractResolverComparator
    public int compare(ResolveInfo lhs, ResolveInfo rhs) {
        int selectProbabilityDiff;
        if (this.mStats != null) {
            ResolverTarget lhsTarget = this.mTargetsDict.get(new ComponentName(lhs.activityInfo.packageName, lhs.activityInfo.name));
            ResolverTarget rhsTarget = this.mTargetsDict.get(new ComponentName(rhs.activityInfo.packageName, rhs.activityInfo.name));
            if (!(lhsTarget == null || rhsTarget == null || (selectProbabilityDiff = Float.compare(rhsTarget.getSelectProbability(), lhsTarget.getSelectProbability())) == 0)) {
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

    @Override // com.android.internal.app.AbstractResolverComparator
    public float getScore(ComponentName name) {
        ResolverTarget target = this.mTargetsDict.get(name);
        if (target != null) {
            return target.getSelectProbability();
        }
        return 0.0f;
    }

    @Override // com.android.internal.app.AbstractResolverComparator
    public void updateModel(ComponentName componentName) {
        synchronized (this.mLock) {
            if (this.mRanker != null) {
                try {
                    int selectedPos = new ArrayList(this.mTargetsDict.keySet()).indexOf(componentName);
                    if (selectedPos >= 0 && this.mTargets != null) {
                        float selectedProbability = getScore(componentName);
                        int order = 0;
                        Iterator<ResolverTarget> it = this.mTargets.iterator();
                        while (it.hasNext()) {
                            if (it.next().getSelectProbability() > selectedProbability) {
                                order++;
                            }
                        }
                        logMetrics(order);
                        this.mRanker.train(this.mTargets, selectedPos);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error in Train: " + e);
                }
            }
        }
    }

    @Override // com.android.internal.app.AbstractResolverComparator
    public void destroy() {
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        ResolverRankerServiceConnection resolverRankerServiceConnection = this.mConnection;
        if (resolverRankerServiceConnection != null) {
            this.mContext.unbindService(resolverRankerServiceConnection);
            this.mConnection.destroy();
        }
        afterCompute();
    }

    private void logMetrics(int selectedPos) {
        if (this.mRankerServiceName != null) {
            MetricsLogger metricsLogger = new MetricsLogger();
            LogMaker log = new LogMaker(1085);
            log.setComponentName(this.mRankerServiceName);
            log.addTaggedData(1086, Integer.valueOf(this.mAnnotations == null ? 0 : 1));
            log.addTaggedData(1087, Integer.valueOf(selectedPos));
            metricsLogger.write(log);
        }
    }

    private void initRanker(Context context) {
        synchronized (this.mLock) {
            if (this.mConnection != null && this.mRanker != null) {
                return;
            }
        }
        Intent intent = resolveRankerService();
        if (intent != null) {
            this.mConnectSignal = new CountDownLatch(1);
            this.mConnection = new ResolverRankerServiceConnection(this.mConnectSignal);
            context.bindServiceAsUser(intent, this.mConnection, 1, UserHandle.SYSTEM);
        }
    }

    private Intent resolveRankerService() {
        Intent intent = new Intent(ResolverRankerService.SERVICE_INTERFACE);
        for (ResolveInfo resolveInfo : this.mPm.queryIntentServices(intent, 0)) {
            if (!(resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.applicationInfo == null)) {
                ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.applicationInfo.packageName, resolveInfo.serviceInfo.name);
                try {
                    if (!"android.permission.BIND_RESOLVER_RANKER_SERVICE".equals(this.mPm.getServiceInfo(componentName, 0).permission)) {
                        Log.w(TAG, "ResolverRankerService " + componentName + " does not require permission android.permission.BIND_RESOLVER_RANKER_SERVICE - this service will not be queried for ResolverRankerServiceResolverComparator. add android:permission=\"android.permission.BIND_RESOLVER_RANKER_SERVICE\" to the <service> tag for " + componentName + " in the manifest.");
                    } else if (this.mPm.checkPermission("android.permission.PROVIDE_RESOLVER_RANKER_SERVICE", resolveInfo.serviceInfo.packageName) != 0) {
                        Log.w(TAG, "ResolverRankerService " + componentName + " does not hold permission android.permission.PROVIDE_RESOLVER_RANKER_SERVICE - this service will not be queried for ResolverRankerServiceResolverComparator.");
                    } else {
                        this.mResolvedRankerName = componentName;
                        intent.setComponent(componentName);
                        return intent;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Could not look up service " + componentName + "; component name not found");
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public class ResolverRankerServiceConnection implements ServiceConnection {
        private final CountDownLatch mConnectSignal;
        public final IResolverRankerResult resolverRankerResult = new IResolverRankerResult.Stub() {
            /* class com.android.internal.app.ResolverRankerServiceResolverComparator.ResolverRankerServiceConnection.AnonymousClass1 */

            @Override // android.service.resolver.IResolverRankerResult
            public void sendResult(List<ResolverTarget> targets) throws RemoteException {
                synchronized (ResolverRankerServiceResolverComparator.this.mLock) {
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.obj = targets;
                    ResolverRankerServiceResolverComparator.this.mHandler.sendMessage(msg);
                }
            }
        };

        public ResolverRankerServiceConnection(CountDownLatch connectSignal) {
            this.mConnectSignal = connectSignal;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (ResolverRankerServiceResolverComparator.this.mLock) {
                ResolverRankerServiceResolverComparator.this.mRanker = IResolverRankerService.Stub.asInterface(service);
                this.mConnectSignal.countDown();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            synchronized (ResolverRankerServiceResolverComparator.this.mLock) {
                destroy();
            }
        }

        public void destroy() {
            synchronized (ResolverRankerServiceResolverComparator.this.mLock) {
                ResolverRankerServiceResolverComparator.this.mRanker = null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.app.AbstractResolverComparator
    public void beforeCompute() {
        super.beforeCompute();
        this.mTargetsDict.clear();
        this.mTargets = null;
        this.mRankerServiceName = new ComponentName(this.mContext, getClass());
        this.mResolvedRankerName = null;
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
        }
        afterCompute();
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

    static boolean isPersistentProcess(ResolverActivity.ResolvedComponentInfo rci) {
        if (rci == null || rci.getCount() <= 0 || (rci.getResolveInfoAt(0).activityInfo.applicationInfo.flags & 8) == 0) {
            return false;
        }
        return true;
    }
}
