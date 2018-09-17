package com.android.internal.app;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.service.chooser.ChooserTarget;
import android.service.chooser.IChooserTargetResult;
import android.service.chooser.IChooserTargetResult.Stub;
import android.service.chooser.IChooserTargetService;
import android.text.TextUtils;
import android.util.FloatProperty;
import android.util.Log;
import android.util.Property;
import android.util.Slog;
import android.util.TimedRemoteCaller;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.android.ims.ImsConferenceState;
import com.android.internal.R;
import com.android.internal.app.ResolverActivity.DisplayResolveInfo;
import com.android.internal.app.ResolverActivity.ResolveListAdapter;
import com.android.internal.app.ResolverActivity.TargetInfo;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.util.Protocol;
import com.google.android.collect.Lists;
import com.huawei.pgmng.plug.PGSdk;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChooserActivity extends ResolverActivity {
    private static final float CALLER_TARGET_SCORE_BOOST = 900.0f;
    private static final int CHOOSER_TARGET_SERVICE_RESULT = 1;
    private static final int CHOOSER_TARGET_SERVICE_WATCHDOG_TIMEOUT = 2;
    private static final boolean DEBUG = false;
    private static final String PINNED_SHARED_PREFS_NAME = "chooser_pin_settings";
    private static final float PINNED_TARGET_SCORE_BOOST = 1000.0f;
    private static final int QUERY_TARGET_SERVICE_LIMIT = 5;
    private static final String TAG = "ChooserActivity";
    private static final String TARGET_DETAILS_FRAGMENT_TAG = "targetDetailsFragment";
    private static final int WATCHDOG_TIMEOUT_MILLIS = 5000;
    private ChooserTarget[] mCallerChooserTargets;
    private final Handler mChooserHandler;
    private ChooserListAdapter mChooserListAdapter;
    private ChooserRowAdapter mChooserRowAdapter;
    private IntentSender mChosenComponentSender;
    private SharedPreferences mPinnedSharedPrefs;
    private Intent mReferrerFillInIntent;
    private IntentSender mRefinementIntentSender;
    private RefinementResultReceiver mRefinementResultReceiver;
    private Bundle mReplacementExtras;
    private final List<ChooserTargetServiceConnection> mServiceConnections;

    static class BaseChooserTargetComparator implements Comparator<ChooserTarget> {
        BaseChooserTargetComparator() {
        }

        public int compare(ChooserTarget lhs, ChooserTarget rhs) {
            return (int) Math.signum(rhs.getScore() - lhs.getScore());
        }
    }

    public class ChooserListAdapter extends ResolveListAdapter {
        private static final int MAX_SERVICE_TARGETS = 8;
        private static final int MAX_TARGETS_PER_SERVICE = 4;
        public static final int TARGET_BAD = -1;
        public static final int TARGET_CALLER = 0;
        public static final int TARGET_SERVICE = 1;
        public static final int TARGET_STANDARD = 2;
        private final BaseChooserTargetComparator mBaseTargetComparator;
        private final List<TargetInfo> mCallerTargets;
        private float mLateFee;
        private final List<ChooserTargetInfo> mServiceTargets;
        private boolean mShowServiceTargets;

        public ChooserListAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
            super(context, payloadIntents, null, rList, launchedFromUid, filterLastUsed);
            this.mServiceTargets = new ArrayList();
            this.mCallerTargets = new ArrayList();
            this.mLateFee = LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            this.mBaseTargetComparator = new BaseChooserTargetComparator();
            if (initialIntents != null) {
                PackageManager pm = ChooserActivity.this.getPackageManager();
                for (int i = TARGET_CALLER; i < initialIntents.length; i += TARGET_SERVICE) {
                    Intent ii = initialIntents[i];
                    if (ii != null) {
                        ResolveInfo resolveInfo = null;
                        ActivityInfo activityInfo = null;
                        if (ii.getComponent() != null) {
                            try {
                                activityInfo = pm.getActivityInfo(ii.getComponent(), TARGET_CALLER);
                                ResolveInfo ri = new ResolveInfo();
                                try {
                                    ri.activityInfo = activityInfo;
                                    resolveInfo = ri;
                                } catch (NameNotFoundException e) {
                                    resolveInfo = ri;
                                }
                            } catch (NameNotFoundException e2) {
                            }
                        }
                        if (activityInfo == null) {
                            resolveInfo = pm.resolveActivity(ii, Protocol.BASE_SYSTEM_RESERVED);
                            activityInfo = resolveInfo != null ? resolveInfo.activityInfo : null;
                        }
                        if (activityInfo == null) {
                            Log.w(ChooserActivity.TAG, "No activity found for " + ii);
                        } else {
                            UserManager userManager = (UserManager) ChooserActivity.this.getSystemService(ImsConferenceState.USER);
                            if (ii instanceof LabeledIntent) {
                                LabeledIntent li = (LabeledIntent) ii;
                                resolveInfo.resolvePackageName = li.getSourcePackage();
                                resolveInfo.labelRes = li.getLabelResource();
                                resolveInfo.nonLocalizedLabel = li.getNonLocalizedLabel();
                                resolveInfo.icon = li.getIconResource();
                                resolveInfo.iconResourceId = resolveInfo.icon;
                            }
                            if (userManager.isManagedProfile()) {
                                resolveInfo.noResourceId = true;
                                resolveInfo.icon = TARGET_CALLER;
                            }
                            this.mCallerTargets.add(new DisplayResolveInfo(ChooserActivity.this, ii, resolveInfo, resolveInfo.loadLabel(pm), null, ii));
                        }
                    }
                }
            }
        }

        public boolean showsExtendedInfo(TargetInfo info) {
            return ChooserActivity.DEBUG;
        }

        public boolean isComponentPinned(ComponentName name) {
            return ChooserActivity.this.mPinnedSharedPrefs.getBoolean(name.flattenToString(), ChooserActivity.DEBUG);
        }

        public float getScore(DisplayResolveInfo target) {
            if (target == null) {
                return ChooserActivity.CALLER_TARGET_SCORE_BOOST;
            }
            float score = super.getScore(target);
            if (target.isPinned()) {
                score += ChooserActivity.PINNED_TARGET_SCORE_BOOST;
            }
            return score;
        }

        public View onCreateView(ViewGroup parent) {
            return this.mInflater.inflate((int) R.layout.resolve_grid_item, parent, (boolean) ChooserActivity.DEBUG);
        }

        public void onListRebuilt() {
            if (this.mServiceTargets != null) {
                pruneServiceTargets();
            }
        }

        public boolean shouldGetResolvedFilter() {
            return true;
        }

        public int getCount() {
            return (super.getCount() + getServiceTargetCount()) + getCallerTargetCount();
        }

        public int getUnfilteredCount() {
            return (super.getUnfilteredCount() + getServiceTargetCount()) + getCallerTargetCount();
        }

        public int getCallerTargetCount() {
            return this.mCallerTargets.size();
        }

        public int getServiceTargetCount() {
            if (this.mShowServiceTargets) {
                return Math.min(this.mServiceTargets.size(), MAX_SERVICE_TARGETS);
            }
            return TARGET_CALLER;
        }

        public int getStandardTargetCount() {
            return super.getCount();
        }

        public int getPositionTargetType(int position) {
            int callerTargetCount = getCallerTargetCount();
            if (position < callerTargetCount) {
                return TARGET_CALLER;
            }
            int offset = callerTargetCount + TARGET_CALLER;
            int serviceTargetCount = getServiceTargetCount();
            if (position - offset < serviceTargetCount) {
                return TARGET_SERVICE;
            }
            if (position - (offset + serviceTargetCount) < super.getCount()) {
                return TARGET_STANDARD;
            }
            return TARGET_BAD;
        }

        public TargetInfo getItem(int position) {
            return targetInfoForPosition(position, true);
        }

        public TargetInfo targetInfoForPosition(int position, boolean filtered) {
            int callerTargetCount = getCallerTargetCount();
            if (position < callerTargetCount) {
                return (TargetInfo) this.mCallerTargets.get(position);
            }
            int offset = callerTargetCount + TARGET_CALLER;
            int serviceTargetCount = getServiceTargetCount();
            if (position - offset < serviceTargetCount) {
                return (TargetInfo) this.mServiceTargets.get(position - offset);
            }
            TargetInfo item;
            offset += serviceTargetCount;
            if (filtered) {
                item = super.getItem(position - offset);
            } else {
                item = getDisplayInfoAt(position - offset);
            }
            return item;
        }

        public void addServiceResults(DisplayResolveInfo origTarget, List<ChooserTarget> targets) {
            float parentScore = getScore(origTarget);
            Collections.sort(targets, this.mBaseTargetComparator);
            float lastScore = 0.0f;
            int N = Math.min(targets.size(), MAX_TARGETS_PER_SERVICE);
            for (int i = TARGET_CALLER; i < N; i += TARGET_SERVICE) {
                ChooserTarget target = (ChooserTarget) targets.get(i);
                float targetScore = (target.getScore() * parentScore) * this.mLateFee;
                if (i > 0 && targetScore >= lastScore) {
                    targetScore = lastScore * 0.95f;
                }
                insertServiceTarget(new ChooserTargetInfo(origTarget, target, targetScore));
                lastScore = targetScore;
            }
            this.mLateFee *= 0.95f;
            notifyDataSetChanged();
        }

        public void setShowServiceTargets(boolean show) {
            this.mShowServiceTargets = show;
            notifyDataSetChanged();
        }

        private void insertServiceTarget(ChooserTargetInfo chooserTargetInfo) {
            float newScore = chooserTargetInfo.getModifiedScore();
            int N = this.mServiceTargets.size();
            for (int i = TARGET_CALLER; i < N; i += TARGET_SERVICE) {
                if (newScore > ((ChooserTargetInfo) this.mServiceTargets.get(i)).getModifiedScore()) {
                    this.mServiceTargets.add(i, chooserTargetInfo);
                    return;
                }
            }
            this.mServiceTargets.add(chooserTargetInfo);
        }

        private void pruneServiceTargets() {
            for (int i = this.mServiceTargets.size() + TARGET_BAD; i >= 0; i += TARGET_BAD) {
                if (!hasResolvedTarget(((ChooserTargetInfo) this.mServiceTargets.get(i)).getResolveInfo())) {
                    this.mServiceTargets.remove(i);
                }
            }
        }
    }

    class ChooserRowAdapter extends BaseAdapter {
        private ChooserListAdapter mChooserListAdapter;
        private final int mColumnCount;
        private final Interpolator mInterpolator;
        private final LayoutInflater mLayoutInflater;
        private RowScale[] mServiceTargetScale;

        /* renamed from: com.android.internal.app.ChooserActivity.ChooserRowAdapter.2 */
        class AnonymousClass2 implements OnClickListener {
            final /* synthetic */ int val$column;
            final /* synthetic */ RowViewHolder val$holder;

            AnonymousClass2(RowViewHolder val$holder, int val$column) {
                this.val$holder = val$holder;
                this.val$column = val$column;
            }

            public void onClick(View v) {
                ChooserActivity.this.startSelected(this.val$holder.itemIndices[this.val$column], ChooserActivity.DEBUG, true);
            }
        }

        /* renamed from: com.android.internal.app.ChooserActivity.ChooserRowAdapter.3 */
        class AnonymousClass3 implements OnLongClickListener {
            final /* synthetic */ int val$column;
            final /* synthetic */ RowViewHolder val$holder;

            AnonymousClass3(RowViewHolder val$holder, int val$column) {
                this.val$holder = val$holder;
                this.val$column = val$column;
            }

            public boolean onLongClick(View v) {
                ChooserActivity.this.showTargetDetails(ChooserRowAdapter.this.mChooserListAdapter.resolveInfoForPosition(this.val$holder.itemIndices[this.val$column], true));
                return true;
            }
        }

        public ChooserRowAdapter(ChooserListAdapter wrappedAdapter) {
            this.mColumnCount = 4;
            this.mChooserListAdapter = wrappedAdapter;
            this.mLayoutInflater = LayoutInflater.from(ChooserActivity.this);
            this.mInterpolator = AnimationUtils.loadInterpolator(ChooserActivity.this, R.interpolator.decelerate_quint);
            wrappedAdapter.registerDataSetObserver(new DataSetObserver() {
                public void onChanged() {
                    super.onChanged();
                    int rcount = ChooserRowAdapter.this.getServiceTargetRowCount();
                    if (ChooserRowAdapter.this.mServiceTargetScale == null || ChooserRowAdapter.this.mServiceTargetScale.length != rcount) {
                        int i;
                        RowScale[] old = ChooserRowAdapter.this.mServiceTargetScale;
                        int oldRCount = old != null ? old.length : 0;
                        ChooserRowAdapter.this.mServiceTargetScale = new RowScale[rcount];
                        if (old != null && rcount > 0) {
                            System.arraycopy(old, 0, ChooserRowAdapter.this.mServiceTargetScale, 0, Math.min(old.length, rcount));
                        }
                        for (i = rcount; i < oldRCount; i += ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT) {
                            old[i].cancelAnimation();
                        }
                        for (i = oldRCount; i < rcount; i += ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT) {
                            ChooserRowAdapter.this.mServiceTargetScale[i] = new RowScale(ChooserRowAdapter.this, 0.0f, LayoutParams.BRIGHTNESS_OVERRIDE_FULL).setInterpolator(ChooserRowAdapter.this.mInterpolator);
                        }
                        for (i = oldRCount; i < rcount; i += ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT) {
                            ChooserRowAdapter.this.mServiceTargetScale[i].startAnimation();
                        }
                    }
                    ChooserRowAdapter.this.notifyDataSetChanged();
                }

                public void onInvalidated() {
                    super.onInvalidated();
                    ChooserRowAdapter.this.notifyDataSetInvalidated();
                    if (ChooserRowAdapter.this.mServiceTargetScale != null) {
                        RowScale[] -get2 = ChooserRowAdapter.this.mServiceTargetScale;
                        int length = -get2.length;
                        for (int i = 0; i < length; i += ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT) {
                            -get2[i].cancelAnimation();
                        }
                    }
                }
            });
        }

        private float getRowScale(int rowPosition) {
            int start = getCallerTargetRowCount();
            int end = start + getServiceTargetRowCount();
            if (rowPosition < start || rowPosition >= end) {
                return LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
            }
            return this.mServiceTargetScale[rowPosition - start].get();
        }

        public int getCount() {
            return (int) (((double) (getCallerTargetRowCount() + getServiceTargetRowCount())) + Math.ceil((double) (((float) this.mChooserListAdapter.getStandardTargetCount()) / 4.0f)));
        }

        public int getCallerTargetRowCount() {
            return (int) Math.ceil((double) (((float) this.mChooserListAdapter.getCallerTargetCount()) / 4.0f));
        }

        public int getServiceTargetRowCount() {
            return (int) Math.ceil((double) (((float) this.mChooserListAdapter.getServiceTargetCount()) / 4.0f));
        }

        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            RowViewHolder holder;
            if (convertView == null) {
                holder = createViewHolder(parent);
            } else {
                holder = (RowViewHolder) convertView.getTag();
            }
            bindViewHolder(position, holder);
            return holder.row;
        }

        RowViewHolder createViewHolder(ViewGroup parent) {
            ViewGroup.LayoutParams lp;
            ViewGroup row = (ViewGroup) this.mLayoutInflater.inflate((int) R.layout.chooser_row, parent, (boolean) ChooserActivity.DEBUG);
            RowViewHolder holder = new RowViewHolder(row, 4);
            int spec = MeasureSpec.makeMeasureSpec(0, 0);
            for (int i = 0; i < 4; i += ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT) {
                View v = this.mChooserListAdapter.createView(row);
                int column = i;
                v.setOnClickListener(new AnonymousClass2(holder, column));
                v.setOnLongClickListener(new AnonymousClass3(holder, column));
                row.addView(v);
                holder.cells[i] = v;
                lp = v.getLayoutParams();
                v.measure(spec, spec);
                if (lp == null) {
                    row.setLayoutParams(new ViewGroup.LayoutParams(-1, v.getMeasuredHeight()));
                } else {
                    lp.height = v.getMeasuredHeight();
                }
            }
            holder.measure();
            lp = row.getLayoutParams();
            if (lp == null) {
                row.setLayoutParams(new ViewGroup.LayoutParams(-1, holder.measuredRowHeight));
            } else {
                lp.height = holder.measuredRowHeight;
            }
            row.setTag(holder);
            return holder;
        }

        void bindViewHolder(int rowPosition, RowViewHolder holder) {
            int start = getFirstRowPosition(rowPosition);
            int startType = this.mChooserListAdapter.getPositionTargetType(start);
            int end = (start + 4) - 1;
            while (this.mChooserListAdapter.getPositionTargetType(end) != startType && end >= start) {
                end--;
            }
            if (startType == ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT) {
                holder.row.setBackgroundColor(ChooserActivity.this.getColor(R.color.chooser_service_row_background_color));
            } else {
                holder.row.setBackgroundColor(0);
            }
            int oldHeight = holder.row.getLayoutParams().height;
            holder.row.getLayoutParams().height = Math.max(ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT, (int) (((float) holder.measuredRowHeight) * getRowScale(rowPosition)));
            if (holder.row.getLayoutParams().height != oldHeight) {
                holder.row.requestLayout();
            }
            for (int i = 0; i < 4; i += ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT) {
                View v = holder.cells[i];
                if (start + i <= end) {
                    v.setVisibility(0);
                    holder.itemIndices[i] = start + i;
                    this.mChooserListAdapter.bindView(holder.itemIndices[i], v);
                } else {
                    v.setVisibility(8);
                }
            }
        }

        int getFirstRowPosition(int row) {
            int callerCount = this.mChooserListAdapter.getCallerTargetCount();
            int callerRows = (int) Math.ceil((double) (((float) callerCount) / 4.0f));
            if (row < callerRows) {
                return row * 4;
            }
            int serviceCount = this.mChooserListAdapter.getServiceTargetCount();
            int serviceRows = (int) Math.ceil((double) (((float) serviceCount) / 4.0f));
            if (row < callerRows + serviceRows) {
                return ((row - callerRows) * 4) + callerCount;
            }
            return (callerCount + serviceCount) + (((row - callerRows) - serviceRows) * 4);
        }
    }

    final class ChooserTargetInfo implements TargetInfo {
        private final ResolveInfo mBackupResolveInfo;
        private CharSequence mBadgeContentDescription;
        private Drawable mBadgeIcon;
        private final ChooserTarget mChooserTarget;
        private Drawable mDisplayIcon;
        private final int mFillInFlags;
        private final Intent mFillInIntent;
        private final float mModifiedScore;
        private final DisplayResolveInfo mSourceInfo;

        public ChooserTargetInfo(DisplayResolveInfo sourceInfo, ChooserTarget chooserTarget, float modifiedScore) {
            this.mBadgeIcon = null;
            this.mSourceInfo = sourceInfo;
            this.mChooserTarget = chooserTarget;
            this.mModifiedScore = modifiedScore;
            if (sourceInfo != null) {
                ResolveInfo ri = sourceInfo.getResolveInfo();
                if (ri != null) {
                    ActivityInfo ai = ri.activityInfo;
                    if (!(ai == null || ai.applicationInfo == null)) {
                        PackageManager pm = ChooserActivity.this.getPackageManager();
                        this.mBadgeIcon = pm.getApplicationIcon(ai.applicationInfo);
                        this.mBadgeContentDescription = pm.getApplicationLabel(ai.applicationInfo);
                    }
                }
            }
            Icon icon = chooserTarget.getIcon();
            this.mDisplayIcon = icon != null ? icon.loadDrawable(ChooserActivity.this) : null;
            if (sourceInfo != null) {
                this.mBackupResolveInfo = null;
            } else {
                this.mBackupResolveInfo = ChooserActivity.this.getPackageManager().resolveActivity(getResolvedIntent(), 0);
            }
            this.mFillInIntent = null;
            this.mFillInFlags = 0;
        }

        private ChooserTargetInfo(ChooserTargetInfo other, Intent fillInIntent, int flags) {
            this.mBadgeIcon = null;
            this.mSourceInfo = other.mSourceInfo;
            this.mBackupResolveInfo = other.mBackupResolveInfo;
            this.mChooserTarget = other.mChooserTarget;
            this.mBadgeIcon = other.mBadgeIcon;
            this.mBadgeContentDescription = other.mBadgeContentDescription;
            this.mDisplayIcon = other.mDisplayIcon;
            this.mFillInIntent = fillInIntent;
            this.mFillInFlags = flags;
            this.mModifiedScore = other.mModifiedScore;
        }

        public float getModifiedScore() {
            return this.mModifiedScore;
        }

        public Intent getResolvedIntent() {
            if (this.mSourceInfo != null) {
                return this.mSourceInfo.getResolvedIntent();
            }
            Intent targetIntent = new Intent(ChooserActivity.this.getTargetIntent());
            targetIntent.setComponent(this.mChooserTarget.getComponentName());
            targetIntent.putExtras(this.mChooserTarget.getIntentExtras());
            return targetIntent;
        }

        public ComponentName getResolvedComponentName() {
            if (this.mSourceInfo != null) {
                return this.mSourceInfo.getResolvedComponentName();
            }
            if (this.mBackupResolveInfo != null) {
                return new ComponentName(this.mBackupResolveInfo.activityInfo.packageName, this.mBackupResolveInfo.activityInfo.name);
            }
            return null;
        }

        private Intent getBaseIntentToSend() {
            Intent result = getResolvedIntent();
            if (result == null) {
                Log.e(ChooserActivity.TAG, "ChooserTargetInfo: no base intent available to send");
                return result;
            }
            Intent result2 = new Intent(result);
            if (this.mFillInIntent != null) {
                result2.fillIn(this.mFillInIntent, this.mFillInFlags);
            }
            result2.fillIn(ChooserActivity.this.mReferrerFillInIntent, 0);
            return result2;
        }

        public boolean start(Activity activity, Bundle options) {
            throw new RuntimeException("ChooserTargets should be started as caller.");
        }

        public boolean startAsCaller(Activity activity, Bundle options, int userId) {
            boolean ignoreTargetSecurity = ChooserActivity.DEBUG;
            Intent intent = getBaseIntentToSend();
            if (intent == null) {
                return ChooserActivity.DEBUG;
            }
            intent.setComponent(this.mChooserTarget.getComponentName());
            intent.putExtras(this.mChooserTarget.getIntentExtras());
            if (this.mSourceInfo != null) {
                ignoreTargetSecurity = this.mSourceInfo.getResolvedComponentName().getPackageName().equals(this.mChooserTarget.getComponentName().getPackageName());
            }
            activity.startActivityAsCaller(intent, options, ignoreTargetSecurity, userId);
            return true;
        }

        public boolean startAsUser(Activity activity, Bundle options, UserHandle user) {
            throw new RuntimeException("ChooserTargets should be started as caller.");
        }

        public ResolveInfo getResolveInfo() {
            return this.mSourceInfo != null ? this.mSourceInfo.getResolveInfo() : this.mBackupResolveInfo;
        }

        public CharSequence getDisplayLabel() {
            return this.mChooserTarget.getTitle();
        }

        public CharSequence getExtendedInfo() {
            return null;
        }

        public Drawable getDisplayIcon() {
            return this.mDisplayIcon;
        }

        public Drawable getBadgeIcon() {
            return this.mBadgeIcon;
        }

        public CharSequence getBadgeContentDescription() {
            return this.mBadgeContentDescription;
        }

        public TargetInfo cloneFilledIn(Intent fillInIntent, int flags) {
            return new ChooserTargetInfo(this, fillInIntent, flags);
        }

        public List<Intent> getAllSourceIntents() {
            List<Intent> results = new ArrayList();
            if (this.mSourceInfo != null) {
                results.add((Intent) this.mSourceInfo.getAllSourceIntents().get(0));
            }
            return results;
        }

        public boolean isPinned() {
            return this.mSourceInfo != null ? this.mSourceInfo.isPinned() : ChooserActivity.DEBUG;
        }
    }

    static class ChooserTargetServiceConnection implements ServiceConnection {
        private ChooserActivity mChooserActivity;
        private final IChooserTargetResult mChooserTargetResult;
        private ComponentName mConnectedComponent;
        private final Object mLock;
        private DisplayResolveInfo mOriginalTarget;

        public ChooserTargetServiceConnection(ChooserActivity chooserActivity, DisplayResolveInfo dri) {
            this.mLock = new Object();
            this.mChooserTargetResult = new Stub() {
                public void sendResult(List<ChooserTarget> targets) throws RemoteException {
                    synchronized (ChooserTargetServiceConnection.this.mLock) {
                        if (ChooserTargetServiceConnection.this.mChooserActivity == null) {
                            Log.e(ChooserActivity.TAG, "destroyed ChooserTargetServiceConnection received result from " + ChooserTargetServiceConnection.this.mConnectedComponent + "; ignoring...");
                            return;
                        }
                        ChooserTargetServiceConnection.this.mChooserActivity.filterServiceTargets(ChooserTargetServiceConnection.this.mOriginalTarget.getResolveInfo().activityInfo.packageName, targets);
                        Message msg = Message.obtain();
                        msg.what = ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT;
                        msg.obj = new ServiceResultInfo(ChooserTargetServiceConnection.this.mOriginalTarget, targets, ChooserTargetServiceConnection.this);
                        ChooserTargetServiceConnection.this.mChooserActivity.mChooserHandler.sendMessage(msg);
                    }
                }
            };
            this.mChooserActivity = chooserActivity;
            this.mOriginalTarget = dri;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (this.mLock) {
                if (this.mChooserActivity == null) {
                    Log.e(ChooserActivity.TAG, "destroyed ChooserTargetServiceConnection got onServiceConnected");
                    return;
                }
                try {
                    IChooserTargetService.Stub.asInterface(service).getChooserTargets(this.mOriginalTarget.getResolvedComponentName(), this.mOriginalTarget.getResolveInfo().filter, this.mChooserTargetResult);
                } catch (RemoteException e) {
                    Log.e(ChooserActivity.TAG, "Querying ChooserTargetService " + name + " failed.", e);
                    this.mChooserActivity.unbindService(this);
                    destroy();
                    this.mChooserActivity.mServiceConnections.remove(this);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (this.mLock) {
                if (this.mChooserActivity == null) {
                    Log.e(ChooserActivity.TAG, "destroyed ChooserTargetServiceConnection got onServiceDisconnected");
                    return;
                }
                this.mChooserActivity.unbindService(this);
                destroy();
                this.mChooserActivity.mServiceConnections.remove(this);
                if (this.mChooserActivity.mServiceConnections.isEmpty()) {
                    this.mChooserActivity.mChooserHandler.removeMessages(ChooserActivity.CHOOSER_TARGET_SERVICE_WATCHDOG_TIMEOUT);
                    this.mChooserActivity.sendVoiceChoicesIfNeeded();
                }
                this.mConnectedComponent = null;
            }
        }

        public void destroy() {
            synchronized (this.mLock) {
                this.mChooserActivity = null;
                this.mOriginalTarget = null;
            }
        }

        public String toString() {
            String activityInfo;
            StringBuilder append = new StringBuilder().append("ChooserTargetServiceConnection{service=").append(this.mConnectedComponent).append(", activity=");
            if (this.mOriginalTarget != null) {
                activityInfo = this.mOriginalTarget.getResolveInfo().activityInfo.toString();
            } else {
                activityInfo = "<connection destroyed>";
            }
            return append.append(activityInfo).append("}").toString();
        }
    }

    class OffsetDataSetObserver extends DataSetObserver {
        private View mCachedView;
        private int mCachedViewType;
        private final AbsListView mListView;

        public OffsetDataSetObserver(AbsListView listView) {
            this.mCachedViewType = -1;
            this.mListView = listView;
        }

        public void onChanged() {
            if (ChooserActivity.this.mResolverDrawerLayout != null) {
                int chooserTargetRows = ChooserActivity.this.mChooserRowAdapter.getServiceTargetRowCount();
                int offset = 0;
                for (int i = 0; i < chooserTargetRows; i += ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT) {
                    int pos = ChooserActivity.this.mChooserRowAdapter.getCallerTargetRowCount() + i;
                    int vt = ChooserActivity.this.mChooserRowAdapter.getItemViewType(pos);
                    if (vt != this.mCachedViewType) {
                        this.mCachedView = null;
                    }
                    View v = ChooserActivity.this.mChooserRowAdapter.getView(pos, this.mCachedView, this.mListView);
                    offset += (int) (((float) ((RowViewHolder) v.getTag()).measuredRowHeight) * ChooserActivity.this.mChooserRowAdapter.getRowScale(pos));
                    if (vt >= 0) {
                        this.mCachedViewType = vt;
                        this.mCachedView = v;
                    } else {
                        this.mCachedViewType = -1;
                    }
                }
                ChooserActivity.this.mResolverDrawerLayout.setCollapsibleHeightReserved(offset);
            }
        }
    }

    static class RefinementResultReceiver extends ResultReceiver {
        private ChooserActivity mChooserActivity;
        private TargetInfo mSelectedTarget;

        public RefinementResultReceiver(ChooserActivity host, TargetInfo target, Handler handler) {
            super(handler);
            this.mChooserActivity = host;
            this.mSelectedTarget = target;
        }

        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (this.mChooserActivity == null) {
                Log.e(ChooserActivity.TAG, "Destroyed RefinementResultReceiver received a result");
            } else if (resultData == null) {
                Log.e(ChooserActivity.TAG, "RefinementResultReceiver received null resultData");
            } else {
                switch (resultCode) {
                    case PGSdk.TYPE_UNKNOW /*-1*/:
                        Parcelable intentParcelable = resultData.getParcelable("android.intent.extra.INTENT");
                        if (!(intentParcelable instanceof Intent)) {
                            Log.e(ChooserActivity.TAG, "RefinementResultReceiver received RESULT_OK but no Intent in resultData with key Intent.EXTRA_INTENT");
                            break;
                        } else {
                            this.mChooserActivity.onRefinementResult(this.mSelectedTarget, (Intent) intentParcelable);
                            break;
                        }
                    case HwCfgFilePolicy.GLOBAL /*0*/:
                        this.mChooserActivity.onRefinementCanceled();
                        break;
                    default:
                        Log.w(ChooserActivity.TAG, "Unknown result code " + resultCode + " sent to RefinementResultReceiver");
                        break;
                }
            }
        }

        public void destroy() {
            this.mChooserActivity = null;
            this.mSelectedTarget = null;
        }
    }

    static class RowScale {
        private static final int DURATION = 400;
        public static final FloatProperty<RowScale> PROPERTY = null;
        ChooserRowAdapter mAdapter;
        private final ObjectAnimator mAnimator;
        float mScale;

        /* renamed from: com.android.internal.app.ChooserActivity.RowScale.1 */
        static class AnonymousClass1 extends FloatProperty<RowScale> {
            AnonymousClass1(String $anonymous0) {
                super($anonymous0);
            }

            public void setValue(RowScale object, float value) {
                object.mScale = value;
                object.mAdapter.notifyDataSetChanged();
            }

            public Float get(RowScale object) {
                return Float.valueOf(object.mScale);
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.ChooserActivity.RowScale.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.ChooserActivity.RowScale.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ChooserActivity.RowScale.<clinit>():void");
        }

        public RowScale(ChooserRowAdapter adapter, float from, float to) {
            this.mAdapter = adapter;
            this.mScale = from;
            if (from == to) {
                this.mAnimator = null;
                return;
            }
            Property property = PROPERTY;
            float[] fArr = new float[ChooserActivity.CHOOSER_TARGET_SERVICE_WATCHDOG_TIMEOUT];
            fArr[0] = from;
            fArr[ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT] = to;
            this.mAnimator = ObjectAnimator.ofFloat(this, property, fArr).setDuration(400);
        }

        public RowScale setInterpolator(Interpolator interpolator) {
            if (this.mAnimator != null) {
                this.mAnimator.setInterpolator(interpolator);
            }
            return this;
        }

        public float get() {
            return this.mScale;
        }

        public void startAnimation() {
            if (this.mAnimator != null) {
                this.mAnimator.start();
            }
        }

        public void cancelAnimation() {
            if (this.mAnimator != null) {
                this.mAnimator.cancel();
            }
        }
    }

    static class RowViewHolder {
        final View[] cells;
        int[] itemIndices;
        int measuredRowHeight;
        final ViewGroup row;

        public RowViewHolder(ViewGroup row, int cellCount) {
            this.row = row;
            this.cells = new View[cellCount];
            this.itemIndices = new int[cellCount];
        }

        public void measure() {
            int spec = MeasureSpec.makeMeasureSpec(0, 0);
            this.row.measure(spec, spec);
            this.measuredRowHeight = this.row.getMeasuredHeight();
        }
    }

    static class ServiceResultInfo {
        public final ChooserTargetServiceConnection connection;
        public final DisplayResolveInfo originalTarget;
        public final List<ChooserTarget> resultTargets;

        public ServiceResultInfo(DisplayResolveInfo ot, List<ChooserTarget> rt, ChooserTargetServiceConnection c) {
            this.originalTarget = ot;
            this.resultTargets = rt;
            this.connection = c;
        }
    }

    public ChooserActivity() {
        this.mServiceConnections = new ArrayList();
        this.mChooserHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ChooserActivity.CHOOSER_TARGET_SERVICE_RESULT /*1*/:
                        if (!ChooserActivity.this.isDestroyed()) {
                            ServiceResultInfo sri = msg.obj;
                            if (ChooserActivity.this.mServiceConnections.contains(sri.connection)) {
                                if (sri.resultTargets != null) {
                                    ChooserActivity.this.mChooserListAdapter.addServiceResults(sri.originalTarget, sri.resultTargets);
                                }
                                ChooserActivity.this.unbindService(sri.connection);
                                sri.connection.destroy();
                                ChooserActivity.this.mServiceConnections.remove(sri.connection);
                                if (ChooserActivity.this.mServiceConnections.isEmpty()) {
                                    ChooserActivity.this.mChooserHandler.removeMessages(ChooserActivity.CHOOSER_TARGET_SERVICE_WATCHDOG_TIMEOUT);
                                    ChooserActivity.this.sendVoiceChoicesIfNeeded();
                                    ChooserActivity.this.mChooserListAdapter.setShowServiceTargets(true);
                                    return;
                                }
                                return;
                            }
                            Log.w(ChooserActivity.TAG, "ChooserTargetServiceConnection " + sri.connection + " returned after being removed from active connections." + " Have you considered returning results faster?");
                        }
                    case ChooserActivity.CHOOSER_TARGET_SERVICE_WATCHDOG_TIMEOUT /*2*/:
                        ChooserActivity.this.unbindRemainingServices();
                        ChooserActivity.this.sendVoiceChoicesIfNeeded();
                        ChooserActivity.this.mChooserListAdapter.setShowServiceTargets(true);
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        Parcelable targetParcelable = intent.getParcelableExtra("android.intent.extra.INTENT");
        if (targetParcelable instanceof Intent) {
            int i;
            Intent target = (Intent) targetParcelable;
            if (target != null) {
                modifyTargetIntent(target);
            }
            Parcelable[] targetsParcelable = intent.getParcelableArrayExtra("android.intent.extra.ALTERNATE_INTENTS");
            if (targetsParcelable != null) {
                boolean offset = target == null ? true : DEBUG;
                Intent[] additionalTargets = new Intent[(offset ? targetsParcelable.length - 1 : targetsParcelable.length)];
                i = 0;
                while (i < targetsParcelable.length) {
                    if (targetsParcelable[i] instanceof Intent) {
                        Intent additionalTarget = targetsParcelable[i];
                        if (i == 0 && target == null) {
                            target = additionalTarget;
                            modifyTargetIntent(additionalTarget);
                        } else {
                            int i2;
                            if (offset) {
                                i2 = i - 1;
                            } else {
                                i2 = i;
                            }
                            additionalTargets[i2] = additionalTarget;
                            modifyTargetIntent(additionalTarget);
                        }
                        i += CHOOSER_TARGET_SERVICE_RESULT;
                    } else {
                        Log.w(TAG, "EXTRA_ALTERNATE_INTENTS array entry #" + i + " is not an Intent: " + targetsParcelable[i]);
                        finish();
                        super.onCreate(null);
                        return;
                    }
                }
                setAdditionalTargets(additionalTargets);
            }
            this.mReplacementExtras = intent.getBundleExtra("android.intent.extra.REPLACEMENT_EXTRAS");
            CharSequence title = intent.getCharSequenceExtra("android.intent.extra.TITLE");
            int defaultTitleRes = 0;
            if (title == null) {
                defaultTitleRes = R.string.chooseActivity;
            }
            Parcelable[] pa = intent.getParcelableArrayExtra("android.intent.extra.INITIAL_INTENTS");
            Intent[] intentArr = null;
            if (pa != null) {
                intentArr = new Intent[pa.length];
                i = 0;
                while (i < pa.length) {
                    if (pa[i] instanceof Intent) {
                        Intent in = pa[i];
                        modifyTargetIntent(in);
                        intentArr[i] = in;
                        i += CHOOSER_TARGET_SERVICE_RESULT;
                    } else {
                        Log.w(TAG, "Initial intent #" + i + " not an Intent: " + pa[i]);
                        finish();
                        super.onCreate(null);
                        return;
                    }
                }
            }
            this.mReferrerFillInIntent = new Intent().putExtra("android.intent.extra.REFERRER", getReferrer());
            this.mChosenComponentSender = (IntentSender) intent.getParcelableExtra("android.intent.extra.CHOSEN_COMPONENT_INTENT_SENDER");
            this.mRefinementIntentSender = (IntentSender) intent.getParcelableExtra("android.intent.extra.CHOOSER_REFINEMENT_INTENT_SENDER");
            setSafeForwardingMode(true);
            pa = intent.getParcelableArrayExtra("android.intent.extra.EXCLUDE_COMPONENTS");
            if (pa != null) {
                ComponentName[] names = new ComponentName[pa.length];
                for (i = 0; i < pa.length; i += CHOOSER_TARGET_SERVICE_RESULT) {
                    if (!(pa[i] instanceof ComponentName)) {
                        Log.w(TAG, "Filtered component #" + i + " not a ComponentName: " + pa[i]);
                        names = null;
                        break;
                    }
                    names[i] = (ComponentName) pa[i];
                }
                setFilteredComponents(names);
            }
            pa = intent.getParcelableArrayExtra("android.intent.extra.CHOOSER_TARGETS");
            if (pa != null) {
                ChooserTarget[] targets = new ChooserTarget[pa.length];
                for (i = 0; i < pa.length; i += CHOOSER_TARGET_SERVICE_RESULT) {
                    if (!(pa[i] instanceof ChooserTarget)) {
                        Log.w(TAG, "Chooser target #" + i + " not a ChooserTarget: " + pa[i]);
                        targets = null;
                        break;
                    }
                    targets[i] = (ChooserTarget) pa[i];
                }
                this.mCallerChooserTargets = targets;
            }
            this.mPinnedSharedPrefs = getPinnedSharedPrefs(this);
            super.onCreate(savedInstanceState, target, title, defaultTitleRes, intentArr, null, DEBUG);
            MetricsLogger.action(this, MetricsEvent.ACTION_ACTIVITY_CHOOSER_SHOWN);
            return;
        }
        Log.w(TAG, "Target is not an intent: " + targetParcelable);
        finish();
        super.onCreate(null);
    }

    static SharedPreferences getPinnedSharedPrefs(Context context) {
        return context.getSharedPreferences(new File(new File(Environment.getDataUserCePackageDirectory(StorageManager.UUID_PRIVATE_INTERNAL, context.getUserId(), context.getPackageName()), "shared_prefs"), "chooser_pin_settings.xml"), 0);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mRefinementResultReceiver != null) {
            this.mRefinementResultReceiver.destroy();
            this.mRefinementResultReceiver = null;
        }
        unbindRemainingServices();
        this.mChooserHandler.removeMessages(CHOOSER_TARGET_SERVICE_RESULT);
    }

    public Intent getReplacementIntent(ActivityInfo aInfo, Intent defIntent) {
        Intent result = defIntent;
        if (this.mReplacementExtras != null) {
            Bundle replExtras = this.mReplacementExtras.getBundle(aInfo.packageName);
            if (replExtras != null) {
                result = new Intent(defIntent);
                result.putExtras(replExtras);
            }
        }
        if (aInfo.name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_PARENT) || aInfo.name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_MANAGED_PROFILE)) {
            return Intent.createChooser(result, getIntent().getCharSequenceExtra("android.intent.extra.TITLE"));
        }
        return result;
    }

    public void onActivityStarted(TargetInfo cti) {
        if (this.mChosenComponentSender != null) {
            ComponentName target = cti.getResolvedComponentName();
            if (target != null) {
                try {
                    this.mChosenComponentSender.sendIntent(this, -1, new Intent().putExtra("android.intent.extra.CHOSEN_COMPONENT", target), null, null);
                } catch (SendIntentException e) {
                    Slog.e(TAG, "Unable to launch supplied IntentSender to report the chosen component: " + e);
                }
            }
        }
    }

    public void onPrepareAdapterView(AbsListView adapterView, ResolveListAdapter adapter, boolean alwaysUseOption) {
        ListView listView = adapterView instanceof ListView ? (ListView) adapterView : null;
        this.mChooserListAdapter = (ChooserListAdapter) adapter;
        if (this.mCallerChooserTargets != null && this.mCallerChooserTargets.length > 0) {
            this.mChooserListAdapter.addServiceResults(null, Lists.newArrayList(this.mCallerChooserTargets));
        }
        this.mChooserRowAdapter = new ChooserRowAdapter(this.mChooserListAdapter);
        this.mChooserRowAdapter.registerDataSetObserver(new OffsetDataSetObserver(adapterView));
        adapterView.setAdapter(this.mChooserRowAdapter);
        if (listView != null) {
            listView.setItemsCanFocus(true);
        }
    }

    public int getLayoutResource() {
        return R.layout.chooser_grid;
    }

    public boolean shouldGetActivityMetadata() {
        return true;
    }

    public void showTargetDetails(ResolveInfo ri) {
        ComponentName name = ri.activityInfo.getComponentName();
        new ResolverTargetActionsDialogFragment(ri.loadLabel(getPackageManager()), name, this.mPinnedSharedPrefs.getBoolean(name.flattenToString(), DEBUG)).show(getFragmentManager(), TARGET_DETAILS_FRAGMENT_TAG);
    }

    private void modifyTargetIntent(Intent in) {
        String action = in.getAction();
        if ("android.intent.action.SEND".equals(action) || "android.intent.action.SEND_MULTIPLE".equals(action)) {
            in.addFlags(134742016);
        }
    }

    protected boolean onTargetSelected(TargetInfo target, boolean alwaysCheck) {
        if (this.mRefinementIntentSender != null) {
            Intent fillIn = new Intent();
            List<Intent> sourceIntents = target.getAllSourceIntents();
            if (!sourceIntents.isEmpty()) {
                fillIn.putExtra("android.intent.extra.INTENT", (Parcelable) sourceIntents.get(0));
                if (sourceIntents.size() > CHOOSER_TARGET_SERVICE_RESULT) {
                    Intent[] alts = new Intent[(sourceIntents.size() - 1)];
                    int N = sourceIntents.size();
                    for (int i = CHOOSER_TARGET_SERVICE_RESULT; i < N; i += CHOOSER_TARGET_SERVICE_RESULT) {
                        alts[i - 1] = (Intent) sourceIntents.get(i);
                    }
                    fillIn.putExtra("android.intent.extra.ALTERNATE_INTENTS", alts);
                }
                if (this.mRefinementResultReceiver != null) {
                    this.mRefinementResultReceiver.destroy();
                }
                this.mRefinementResultReceiver = new RefinementResultReceiver(this, target, null);
                fillIn.putExtra("android.intent.extra.RESULT_RECEIVER", this.mRefinementResultReceiver);
                try {
                    this.mRefinementIntentSender.sendIntent(this, 0, fillIn, null, null);
                    return DEBUG;
                } catch (SendIntentException e) {
                    Log.e(TAG, "Refinement IntentSender failed to send", e);
                }
            }
        }
        return super.onTargetSelected(target, alwaysCheck);
    }

    public void startSelected(int which, boolean always, boolean filtered) {
        super.startSelected(which, always, filtered);
        if (this.mChooserListAdapter != null) {
            int cat = 0;
            int value = which;
            switch (this.mChooserListAdapter.getPositionTargetType(which)) {
                case HwCfgFilePolicy.GLOBAL /*0*/:
                    cat = MetricsEvent.ACTION_ACTIVITY_CHOOSER_PICKED_APP_TARGET;
                    break;
                case CHOOSER_TARGET_SERVICE_RESULT /*1*/:
                    cat = MetricsEvent.ACTION_ACTIVITY_CHOOSER_PICKED_SERVICE_TARGET;
                    value = which - this.mChooserListAdapter.getCallerTargetCount();
                    break;
                case CHOOSER_TARGET_SERVICE_WATCHDOG_TIMEOUT /*2*/:
                    cat = MetricsEvent.ACTION_ACTIVITY_CHOOSER_PICKED_STANDARD_TARGET;
                    value = which - (this.mChooserListAdapter.getCallerTargetCount() + this.mChooserListAdapter.getServiceTargetCount());
                    break;
            }
            if (cat != 0) {
                MetricsLogger.action((Context) this, cat, value);
            }
        }
    }

    void queryTargetServices(ChooserListAdapter adapter) {
        PackageManager pm = getPackageManager();
        int targetsToQuery = 0;
        int N = adapter.getDisplayResolveInfoCount();
        for (int i = 0; i < N; i += CHOOSER_TARGET_SERVICE_RESULT) {
            DisplayResolveInfo dri = adapter.getDisplayResolveInfo(i);
            if (adapter.getScore(dri) != 0.0f) {
                String serviceName;
                ActivityInfo ai = dri.getResolveInfo().activityInfo;
                Bundle md = ai.metaData;
                if (md != null) {
                    serviceName = convertServiceName(ai.packageName, md.getString("android.service.chooser.chooser_target_service"));
                } else {
                    serviceName = null;
                }
                if (serviceName != null) {
                    ComponentName serviceComponent = new ComponentName(ai.packageName, serviceName);
                    Intent serviceIntent = new Intent("android.service.chooser.ChooserTargetService").setComponent(serviceComponent);
                    try {
                        if ("android.permission.BIND_CHOOSER_TARGET_SERVICE".equals(pm.getServiceInfo(serviceComponent, 0).permission)) {
                            ChooserTargetServiceConnection conn = new ChooserTargetServiceConnection(this, dri);
                            if (bindServiceAsUser(serviceIntent, conn, QUERY_TARGET_SERVICE_LIMIT, Process.myUserHandle())) {
                                this.mServiceConnections.add(conn);
                                targetsToQuery += CHOOSER_TARGET_SERVICE_RESULT;
                            }
                        } else {
                            Log.w(TAG, "ChooserTargetService " + serviceComponent + " does not require" + " permission " + "android.permission.BIND_CHOOSER_TARGET_SERVICE" + " - this service will not be queried for ChooserTargets." + " add android:permission=\"" + "android.permission.BIND_CHOOSER_TARGET_SERVICE" + "\"" + " to the <service> tag for " + serviceComponent + " in the manifest.");
                        }
                    } catch (NameNotFoundException e) {
                        Log.e(TAG, "Could not look up service " + serviceComponent + "; component name not found");
                    }
                }
                if (targetsToQuery >= QUERY_TARGET_SERVICE_LIMIT) {
                    break;
                }
            }
        }
        if (this.mServiceConnections.isEmpty()) {
            sendVoiceChoicesIfNeeded();
            return;
        }
        this.mChooserHandler.sendEmptyMessageDelayed(CHOOSER_TARGET_SERVICE_WATCHDOG_TIMEOUT, TimedRemoteCaller.DEFAULT_CALL_TIMEOUT_MILLIS);
    }

    private String convertServiceName(String packageName, String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            return null;
        }
        String str;
        if (serviceName.startsWith(".")) {
            str = packageName + serviceName;
        } else if (serviceName.indexOf(46) >= 0) {
            str = serviceName;
        } else {
            str = null;
        }
        return str;
    }

    void unbindRemainingServices() {
        int N = this.mServiceConnections.size();
        for (int i = 0; i < N; i += CHOOSER_TARGET_SERVICE_RESULT) {
            ChooserTargetServiceConnection conn = (ChooserTargetServiceConnection) this.mServiceConnections.get(i);
            unbindService(conn);
            conn.destroy();
        }
        this.mServiceConnections.clear();
        this.mChooserHandler.removeMessages(CHOOSER_TARGET_SERVICE_WATCHDOG_TIMEOUT);
    }

    public void onSetupVoiceInteraction() {
    }

    void onRefinementResult(TargetInfo selectedTarget, Intent matchingIntent) {
        if (this.mRefinementResultReceiver != null) {
            this.mRefinementResultReceiver.destroy();
            this.mRefinementResultReceiver = null;
        }
        if (selectedTarget == null) {
            Log.e(TAG, "Refinement result intent did not match any known targets; canceling");
        } else if (!checkTargetSourceIntent(selectedTarget, matchingIntent)) {
            Log.e(TAG, "onRefinementResult: Selected target " + selectedTarget + " cannot match refined source intent " + matchingIntent);
        } else if (super.onTargetSelected(selectedTarget.cloneFilledIn(matchingIntent, 0), DEBUG)) {
            finish();
            return;
        }
        onRefinementCanceled();
    }

    void onRefinementCanceled() {
        if (this.mRefinementResultReceiver != null) {
            this.mRefinementResultReceiver.destroy();
            this.mRefinementResultReceiver = null;
        }
        finish();
    }

    boolean checkTargetSourceIntent(TargetInfo target, Intent matchingIntent) {
        List<Intent> targetIntents = target.getAllSourceIntents();
        int N = targetIntents.size();
        for (int i = 0; i < N; i += CHOOSER_TARGET_SERVICE_RESULT) {
            if (((Intent) targetIntents.get(i)).filterEquals(matchingIntent)) {
                return true;
            }
        }
        return DEBUG;
    }

    void filterServiceTargets(String packageName, List<ChooserTarget> targets) {
        if (targets != null) {
            PackageManager pm = getPackageManager();
            for (int i = targets.size() - 1; i >= 0; i--) {
                ChooserTarget target = (ChooserTarget) targets.get(i);
                ComponentName targetName = target.getComponentName();
                if (packageName == null || !packageName.equals(targetName.getPackageName())) {
                    boolean remove;
                    try {
                        ActivityInfo ai = pm.getActivityInfo(targetName, 0);
                        remove = (ai.exported && ai.permission == null) ? DEBUG : true;
                    } catch (NameNotFoundException e) {
                        Log.e(TAG, "Target " + target + " returned by " + packageName + " component not found");
                        remove = true;
                    }
                    if (remove) {
                        targets.remove(i);
                    }
                }
            }
        }
    }

    public ResolveListAdapter createAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
        ChooserListAdapter adapter = new ChooserListAdapter(context, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed);
        queryTargetServices(adapter);
        return adapter;
    }
}
