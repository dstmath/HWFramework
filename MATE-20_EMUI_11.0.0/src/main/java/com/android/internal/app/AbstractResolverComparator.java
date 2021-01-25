package com.android.internal.app;

import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.app.ResolverActivity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

abstract class AbstractResolverComparator implements Comparator<ResolverActivity.ResolvedComponentInfo> {
    private static final boolean DEBUG = false;
    private static final int NUM_OF_TOP_ANNOTATIONS_TO_USE = 3;
    static final int RANKER_RESULT_TIMEOUT = 1;
    static final int RANKER_SERVICE_RESULT = 0;
    private static final String TAG = "AbstractResolverComp";
    private static final int WATCHDOG_TIMEOUT_MILLIS = 500;
    protected AfterCompute mAfterCompute;
    protected String[] mAnnotations;
    protected String mContentType;
    private final String mDefaultBrowserPackageName;
    protected final Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.internal.app.AbstractResolverComparator.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i != 1) {
                    super.handleMessage(msg);
                    return;
                }
                AbstractResolverComparator.this.mHandler.removeMessages(0);
                AbstractResolverComparator.this.afterCompute();
            } else if (AbstractResolverComparator.this.mHandler.hasMessages(1)) {
                AbstractResolverComparator.this.handleResultMessage(msg);
                AbstractResolverComparator.this.mHandler.removeMessages(1);
                AbstractResolverComparator.this.afterCompute();
            }
        }
    };
    private final boolean mHttp;
    protected final PackageManager mPm;
    protected final UsageStatsManager mUsm;

    /* access modifiers changed from: package-private */
    public interface AfterCompute {
        void afterCompute();
    }

    /* access modifiers changed from: package-private */
    public abstract int compare(ResolveInfo resolveInfo, ResolveInfo resolveInfo2);

    /* access modifiers changed from: package-private */
    public abstract void doCompute(List<ResolverActivity.ResolvedComponentInfo> list);

    /* access modifiers changed from: package-private */
    public abstract float getScore(ComponentName componentName);

    /* access modifiers changed from: package-private */
    public abstract void handleResultMessage(Message message);

    AbstractResolverComparator(Context context, Intent intent) {
        String str;
        String scheme = intent.getScheme();
        this.mHttp = IntentFilter.SCHEME_HTTP.equals(scheme) || IntentFilter.SCHEME_HTTPS.equals(scheme);
        this.mContentType = intent.getType();
        getContentAnnotations(intent);
        this.mPm = context.getPackageManager();
        this.mUsm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (this.mHttp) {
            str = this.mPm.getDefaultBrowserPackageNameAsUser(UserHandle.myUserId());
        } else {
            str = null;
        }
        this.mDefaultBrowserPackageName = str;
    }

    private void getContentAnnotations(Intent intent) {
        ArrayList<String> annotations = intent.getStringArrayListExtra(Intent.EXTRA_CONTENT_ANNOTATIONS);
        if (annotations != null) {
            int size = annotations.size();
            if (size > 3) {
                size = 3;
            }
            this.mAnnotations = new String[size];
            for (int i = 0; i < size; i++) {
                this.mAnnotations[i] = annotations.get(i);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setCallBack(AfterCompute afterCompute) {
        this.mAfterCompute = afterCompute;
    }

    /* access modifiers changed from: protected */
    public final void afterCompute() {
        AfterCompute afterCompute = this.mAfterCompute;
        if (afterCompute != null) {
            afterCompute.afterCompute();
        }
    }

    public final int compare(ResolverActivity.ResolvedComponentInfo lhsp, ResolverActivity.ResolvedComponentInfo rhsp) {
        ResolveInfo lhs = lhsp.getResolveInfoAt(0);
        ResolveInfo rhs = rhsp.getResolveInfoAt(0);
        if (lhs.targetUserId != -2) {
            if (rhs.targetUserId != -2) {
                return 0;
            }
            return 1;
        } else if (rhs.targetUserId != -2) {
            return -1;
        } else {
            if (this.mHttp) {
                if (isDefaultBrowser(lhs)) {
                    return -1;
                }
                if (isDefaultBrowser(rhs)) {
                    return 1;
                }
                boolean lhsSpecific = ResolverActivity.isSpecificUriMatch(lhs.match);
                if (lhsSpecific != ResolverActivity.isSpecificUriMatch(rhs.match)) {
                    if (lhsSpecific) {
                        return -1;
                    }
                    return 1;
                }
            }
            return compare(lhs, rhs);
        }
    }

    /* access modifiers changed from: package-private */
    public final void compute(List<ResolverActivity.ResolvedComponentInfo> targets) {
        beforeCompute();
        doCompute(targets);
    }

    /* access modifiers changed from: package-private */
    public final void updateChooserCounts(String packageName, int userId, String action) {
        UsageStatsManager usageStatsManager = this.mUsm;
        if (usageStatsManager != null) {
            usageStatsManager.reportChooserSelection(packageName, userId, this.mContentType, this.mAnnotations, action);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateModel(ComponentName componentName) {
    }

    /* access modifiers changed from: package-private */
    public void beforeCompute() {
        Handler handler = this.mHandler;
        if (handler == null) {
            Log.d(TAG, "Error: Handler is Null; Needs to be initialized.");
        } else {
            handler.sendEmptyMessageDelayed(1, 500);
        }
    }

    /* access modifiers changed from: package-private */
    public void destroy() {
        this.mHandler.removeMessages(0);
        this.mHandler.removeMessages(1);
        afterCompute();
    }

    private boolean isDefaultBrowser(ResolveInfo ri) {
        if (ri.targetUserId == -2 && ri.activityInfo.packageName != null && ri.activityInfo.packageName.equals(this.mDefaultBrowserPackageName)) {
            return true;
        }
        return false;
    }
}
