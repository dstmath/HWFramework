package com.android.internal.app;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ResolverComparator implements Comparator<ResolvedComponentInfo> {
    private static final boolean DEBUG = false;
    private static final float RECENCY_MULTIPLIER = 2.0f;
    private static final long RECENCY_TIME_PERIOD = 43200000;
    private static final String TAG = "ResolverComparator";
    private static final long USAGE_STATS_PERIOD = 604800000;
    private final Collator mCollator;
    private final long mCurrentTime;
    private final boolean mHttp;
    private final PackageManager mPm;
    private final String mReferrerPackage;
    private final LinkedHashMap<ComponentName, ScoredTarget> mScoredTargets;
    private final long mSinceTime;
    private final Map<String, UsageStats> mStats;
    private final UsageStatsManager mUsm;

    static class ScoredTarget {
        public final ComponentInfo componentInfo;
        public long lastTimeUsed;
        public long launchCount;
        public float score;
        public long timeSpent;

        public ScoredTarget(ComponentInfo ci) {
            this.componentInfo = ci;
        }

        public String toString() {
            return "ScoredTarget{" + this.componentInfo + " score: " + this.score + " lastTimeUsed: " + this.lastTimeUsed + " timeSpent: " + this.timeSpent + " launchCount: " + this.launchCount + "}";
        }
    }

    public ResolverComparator(Context context, Intent intent, String referrerPackage) {
        this.mScoredTargets = new LinkedHashMap();
        this.mCollator = Collator.getInstance(context.getResources().getConfiguration().locale);
        String scheme = intent.getScheme();
        this.mHttp = !"http".equals(scheme) ? "https".equals(scheme) : true;
        this.mReferrerPackage = referrerPackage;
        this.mPm = context.getPackageManager();
        this.mUsm = (UsageStatsManager) context.getSystemService("usagestats");
        this.mCurrentTime = System.currentTimeMillis();
        this.mSinceTime = this.mCurrentTime - USAGE_STATS_PERIOD;
        this.mStats = this.mUsm.queryAndAggregateUsageStats(this.mSinceTime, this.mCurrentTime);
    }

    public void compute(List<ResolvedComponentInfo> targets) {
        this.mScoredTargets.clear();
        long recentSinceTime = this.mCurrentTime - RECENCY_TIME_PERIOD;
        long mostRecentlyUsedTime = recentSinceTime + 1;
        long mostTimeSpent = 1;
        int mostLaunched = 1;
        for (ResolvedComponentInfo target : targets) {
            ScoredTarget scoredTarget = new ScoredTarget(target.getResolveInfoAt(0).activityInfo);
            this.mScoredTargets.put(target.name, scoredTarget);
            UsageStats pkStats = (UsageStats) this.mStats.get(target.name.getPackageName());
            if (pkStats != null) {
                ComponentName componentName = target.name;
                if (!(r0.getPackageName().equals(this.mReferrerPackage) || isPersistentProcess(target))) {
                    long lastTimeUsed = pkStats.getLastTimeUsed();
                    scoredTarget.lastTimeUsed = lastTimeUsed;
                    if (lastTimeUsed > mostRecentlyUsedTime) {
                        mostRecentlyUsedTime = lastTimeUsed;
                    }
                }
                long timeSpent = pkStats.getTotalTimeInForeground();
                scoredTarget.timeSpent = timeSpent;
                if (timeSpent > mostTimeSpent) {
                    mostTimeSpent = timeSpent;
                }
                int launched = pkStats.mLaunchCount;
                scoredTarget.launchCount = (long) launched;
                if (launched > mostLaunched) {
                    mostLaunched = launched;
                }
            }
        }
        for (ScoredTarget target2 : this.mScoredTargets.values()) {
            float recency = ((float) Math.max(target2.lastTimeUsed - recentSinceTime, 0)) / ((float) (mostRecentlyUsedTime - recentSinceTime));
            float recencyScore = (recency * recency) * RECENCY_MULTIPLIER;
            float usageTimeScore = ((float) target2.timeSpent) / ((float) mostTimeSpent);
            target2.score = (recencyScore + usageTimeScore) + (((float) target2.launchCount) / ((float) mostLaunched));
        }
    }

    static boolean isPersistentProcess(ResolvedComponentInfo rci) {
        boolean z = DEBUG;
        if (rci == null || rci.getCount() <= 0) {
            return DEBUG;
        }
        if ((rci.getResolveInfoAt(0).activityInfo.applicationInfo.flags & 8) != 0) {
            z = true;
        }
        return z;
    }

    public int compare(ResolvedComponentInfo lhsp, ResolvedComponentInfo rhsp) {
        ResolveInfo lhs = lhsp.getResolveInfoAt(0);
        ResolveInfo rhs = rhsp.getResolveInfoAt(0);
        if (lhs.targetUserId != -2) {
            return 1;
        }
        if (this.mHttp) {
            boolean lhsSpecific = ResolverActivity.isSpecificUriMatch(lhs.match);
            if (lhsSpecific != ResolverActivity.isSpecificUriMatch(rhs.match)) {
                return lhsSpecific ? -1 : 1;
            }
        }
        boolean lPinned = lhsp.isPinned();
        boolean rPinned = rhsp.isPinned();
        if (lPinned && !rPinned) {
            return -1;
        }
        if (!lPinned && rPinned) {
            return 1;
        }
        if (!(lPinned || rPinned || this.mStats == null)) {
            float diff = ((ScoredTarget) this.mScoredTargets.get(new ComponentName(rhs.activityInfo.packageName, rhs.activityInfo.name))).score - ((ScoredTarget) this.mScoredTargets.get(new ComponentName(lhs.activityInfo.packageName, lhs.activityInfo.name))).score;
            if (diff != 0.0f) {
                return diff > 0.0f ? 1 : -1;
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

    public float getScore(ComponentName name) {
        ScoredTarget target = (ScoredTarget) this.mScoredTargets.get(name);
        if (target != null) {
            return target.score;
        }
        return 0.0f;
    }
}
