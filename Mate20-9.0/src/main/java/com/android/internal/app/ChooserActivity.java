package com.android.internal.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
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
import android.service.chooser.IChooserTargetService;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.ResolverActivity;
import com.android.internal.logging.MetricsLogger;
import com.google.android.collect.Lists;
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
    public static final String EXTRA_PRIVATE_RETAIN_IN_ON_STOP = "com.android.internal.app.ChooserActivity.EXTRA_PRIVATE_RETAIN_IN_ON_STOP";
    private static final String PINNED_SHARED_PREFS_NAME = "chooser_pin_settings";
    private static final float PINNED_TARGET_SCORE_BOOST = 1000.0f;
    private static final int QUERY_TARGET_SERVICE_LIMIT = 5;
    private static final String TAG = "ChooserActivity";
    private static final String TARGET_DETAILS_FRAGMENT_TAG = "targetDetailsFragment";
    private static final int WATCHDOG_TIMEOUT_MILLIS = 2000;
    private ChooserTarget[] mCallerChooserTargets;
    /* access modifiers changed from: private */
    public final Handler mChooserHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (!ChooserActivity.this.isDestroyed()) {
                        ServiceResultInfo sri = (ServiceResultInfo) msg.obj;
                        if (!ChooserActivity.this.mServiceConnections.contains(sri.connection)) {
                            Log.w(ChooserActivity.TAG, "ChooserTargetServiceConnection " + sri.connection + " returned after being removed from active connections. Have you considered returning results faster?");
                            return;
                        }
                        if (sri.resultTargets != null) {
                            ChooserActivity.this.mChooserListAdapter.addServiceResults(sri.originalTarget, sri.resultTargets);
                        }
                        ChooserActivity.this.unbindService(sri.connection);
                        sri.connection.destroy();
                        ChooserActivity.this.mServiceConnections.remove(sri.connection);
                        if (ChooserActivity.this.mServiceConnections.isEmpty()) {
                            ChooserActivity.this.mChooserHandler.removeMessages(2);
                            ChooserActivity.this.sendVoiceChoicesIfNeeded();
                            ChooserActivity.this.mChooserListAdapter.setShowServiceTargets(true);
                            return;
                        }
                        return;
                    }
                    return;
                case 2:
                    ChooserActivity.this.unbindRemainingServices();
                    ChooserActivity.this.sendVoiceChoicesIfNeeded();
                    ChooserActivity.this.mChooserListAdapter.setShowServiceTargets(true);
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public ChooserListAdapter mChooserListAdapter;
    /* access modifiers changed from: private */
    public ChooserRowAdapter mChooserRowAdapter;
    private long mChooserShownTime;
    private IntentSender mChosenComponentSender;
    /* access modifiers changed from: private */
    public ComponentName[] mFilteredComponentNames;
    protected boolean mIsSuccessfullySelected;
    /* access modifiers changed from: private */
    public SharedPreferences mPinnedSharedPrefs;
    /* access modifiers changed from: private */
    public Intent mReferrerFillInIntent;
    private IntentSender mRefinementIntentSender;
    private RefinementResultReceiver mRefinementResultReceiver;
    private Bundle mReplacementExtras;
    /* access modifiers changed from: private */
    public final List<ChooserTargetServiceConnection> mServiceConnections = new ArrayList();

    static class BaseChooserTargetComparator implements Comparator<ChooserTarget> {
        BaseChooserTargetComparator() {
        }

        public int compare(ChooserTarget lhs, ChooserTarget rhs) {
            return (int) Math.signum(rhs.getScore() - lhs.getScore());
        }
    }

    public class ChooserListAdapter extends ResolverActivity.ResolveListAdapter {
        private static final int MAX_SERVICE_TARGETS = 4;
        private static final int MAX_TARGETS_PER_SERVICE = 2;
        public static final int TARGET_BAD = -1;
        public static final int TARGET_CALLER = 0;
        public static final int TARGET_SERVICE = 1;
        public static final int TARGET_STANDARD = 2;
        private final BaseChooserTargetComparator mBaseTargetComparator;
        private final List<ResolverActivity.TargetInfo> mCallerTargets = new ArrayList();
        private float mLateFee = 1.0f;
        private final List<ChooserTargetInfo> mServiceTargets = new ArrayList();
        private boolean mShowServiceTargets;
        private boolean mTargetsNeedPruning;
        final /* synthetic */ ChooserActivity this$0;

        /* JADX WARNING: Illegal instructions before constructor call */
        public ChooserListAdapter(ChooserActivity this$02, Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed, ResolverListController resolverListController) {
            super(context, payloadIntents, null, rList, launchedFromUid, filterLastUsed, resolverListController);
            ResolveInfo ri;
            ChooserActivity chooserActivity = this$02;
            Intent[] intentArr = initialIntents;
            this.this$0 = chooserActivity;
            int i = 0;
            this.mTargetsNeedPruning = false;
            this.mBaseTargetComparator = new BaseChooserTargetComparator();
            if (intentArr != null) {
                PackageManager pm = this$02.getPackageManager();
                int i2 = 0;
                while (true) {
                    int i3 = i2;
                    if (i3 < intentArr.length) {
                        Intent ii = intentArr[i3];
                        if (ii != null) {
                            ResolveInfo ri2 = null;
                            ActivityInfo ai = null;
                            if (ii.getComponent() != null) {
                                try {
                                    ai = pm.getActivityInfo(ii.getComponent(), i);
                                    ri2 = new ResolveInfo();
                                    ri2.activityInfo = ai;
                                } catch (PackageManager.NameNotFoundException e) {
                                }
                            }
                            if (ai == null) {
                                ResolveInfo ri3 = pm.resolveActivity(ii, 65536 | (chooserActivity.mIsClonedProfile ? 4202496 : i));
                                ai = ri3 != null ? ri3.activityInfo : null;
                                ri = ri3;
                            } else {
                                ri = ri2;
                            }
                            ActivityInfo ai2 = ai;
                            if (ai2 == null) {
                                Log.w(ChooserActivity.TAG, "No activity found for " + ii);
                            } else {
                                UserManager userManager = (UserManager) chooserActivity.getSystemService("user");
                                if (ii instanceof LabeledIntent) {
                                    LabeledIntent li = (LabeledIntent) ii;
                                    ri.resolvePackageName = li.getSourcePackage();
                                    ri.labelRes = li.getLabelResource();
                                    ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                                    ri.icon = li.getIconResource();
                                    ri.iconResourceId = ri.icon;
                                }
                                if (userManager.isManagedProfile()) {
                                    ri.noResourceId = true;
                                    ri.icon = i;
                                }
                                ResolverActivity.DisplayResolveInfo displayResolveInfo = r1;
                                ActivityInfo activityInfo = ai2;
                                List<ResolverActivity.TargetInfo> list = this.mCallerTargets;
                                UserManager userManager2 = userManager;
                                ResolveInfo resolveInfo = ri;
                                ResolverActivity.DisplayResolveInfo displayResolveInfo2 = new ResolverActivity.DisplayResolveInfo(ii, ri, ri.loadLabel(pm), null, ii);
                                list.add(displayResolveInfo);
                            }
                        }
                        i2 = i3 + 1;
                        i = 0;
                    } else {
                        return;
                    }
                }
            }
        }

        public boolean showsExtendedInfo(ResolverActivity.TargetInfo info) {
            return false;
        }

        public boolean isComponentPinned(ComponentName name) {
            return this.this$0.mPinnedSharedPrefs.getBoolean(name.flattenToString(), false);
        }

        public View onCreateView(ViewGroup parent) {
            return this.mInflater.inflate(17367250, parent, false);
        }

        public void onListRebuilt() {
            if (!ActivityManager.isLowRamDeviceStatic()) {
                if (this.mServiceTargets != null && getDisplayInfoCount() == 0) {
                    this.mTargetsNeedPruning = true;
                }
                this.this$0.queryTargetServices(this);
            }
        }

        public boolean shouldGetResolvedFilter() {
            return true;
        }

        public int getCount() {
            return super.getCount() + getServiceTargetCount() + getCallerTargetCount();
        }

        public int getUnfilteredCount() {
            return super.getUnfilteredCount() + getServiceTargetCount() + getCallerTargetCount();
        }

        public int getCallerTargetCount() {
            return this.mCallerTargets.size();
        }

        public int getServiceTargetCount() {
            if (!this.mShowServiceTargets) {
                return 0;
            }
            return Math.min(this.mServiceTargets.size(), 4);
        }

        public int getStandardTargetCount() {
            return super.getCount();
        }

        public int getPositionTargetType(int position) {
            int callerTargetCount = getCallerTargetCount();
            if (position < callerTargetCount) {
                return 0;
            }
            int offset = 0 + callerTargetCount;
            int serviceTargetCount = getServiceTargetCount();
            if (position - offset < serviceTargetCount) {
                return 1;
            }
            if (position - (offset + serviceTargetCount) < super.getCount()) {
                return 2;
            }
            return -1;
        }

        public ResolverActivity.TargetInfo getItem(int position) {
            return targetInfoForPosition(position, true);
        }

        public ResolverActivity.TargetInfo targetInfoForPosition(int position, boolean filtered) {
            ResolverActivity.TargetInfo targetInfo;
            int callerTargetCount = getCallerTargetCount();
            if (position < callerTargetCount) {
                return this.mCallerTargets.get(position);
            }
            int offset = 0 + callerTargetCount;
            int serviceTargetCount = getServiceTargetCount();
            if (position - offset < serviceTargetCount) {
                return this.mServiceTargets.get(position - offset);
            }
            int offset2 = offset + serviceTargetCount;
            if (filtered) {
                targetInfo = super.getItem(position - offset2);
            } else {
                targetInfo = getDisplayInfoAt(position - offset2);
            }
            return targetInfo;
        }

        public void addServiceResults(ResolverActivity.DisplayResolveInfo origTarget, List<ChooserTarget> targets) {
            if (this.mTargetsNeedPruning && targets.size() > 0) {
                this.mServiceTargets.clear();
                this.mTargetsNeedPruning = false;
            }
            float parentScore = getScore(origTarget);
            Collections.sort(targets, this.mBaseTargetComparator);
            float lastScore = 0.0f;
            int N = Math.min(targets.size(), 2);
            for (int i = 0; i < N; i++) {
                ChooserTarget target = targets.get(i);
                float targetScore = target.getScore() * parentScore * this.mLateFee;
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
            if (show != this.mShowServiceTargets) {
                this.mShowServiceTargets = show;
                notifyDataSetChanged();
            }
        }

        private void insertServiceTarget(ChooserTargetInfo chooserTargetInfo) {
            float newScore = chooserTargetInfo.getModifiedScore();
            int N = this.mServiceTargets.size();
            for (int i = 0; i < N; i++) {
                if (newScore > this.mServiceTargets.get(i).getModifiedScore()) {
                    this.mServiceTargets.add(i, chooserTargetInfo);
                    return;
                }
            }
            this.mServiceTargets.add(chooserTargetInfo);
        }
    }

    public class ChooserListController extends ResolverListController {
        public ChooserListController(Context context, PackageManager pm, Intent targetIntent, String referrerPackageName, int launchedFromUid) {
            super(context, pm, targetIntent, referrerPackageName, launchedFromUid);
        }

        /* access modifiers changed from: package-private */
        public boolean isComponentPinned(ComponentName name) {
            return ChooserActivity.this.mPinnedSharedPrefs.getBoolean(name.flattenToString(), false);
        }

        /* access modifiers changed from: package-private */
        public boolean isComponentFiltered(ComponentName name) {
            if (ChooserActivity.this.mFilteredComponentNames == null) {
                return false;
            }
            for (ComponentName filteredComponentName : ChooserActivity.this.mFilteredComponentNames) {
                if (name.equals(filteredComponentName)) {
                    return true;
                }
            }
            return false;
        }

        public float getScore(ResolverActivity.DisplayResolveInfo target) {
            if (target == null) {
                return ChooserActivity.CALLER_TARGET_SCORE_BOOST;
            }
            float score = super.getScore(target);
            if (target.isPinned()) {
                score += ChooserActivity.PINNED_TARGET_SCORE_BOOST;
            }
            return score;
        }
    }

    class ChooserRowAdapter extends BaseAdapter {
        private int mAnimationCount = 0;
        /* access modifiers changed from: private */
        public ChooserListAdapter mChooserListAdapter;
        private final int mColumnCount = 4;
        private final LayoutInflater mLayoutInflater;

        public ChooserRowAdapter(ChooserListAdapter wrappedAdapter) {
            this.mChooserListAdapter = wrappedAdapter;
            this.mLayoutInflater = LayoutInflater.from(ChooserActivity.this);
            wrappedAdapter.registerDataSetObserver(new DataSetObserver(ChooserActivity.this) {
                public void onChanged() {
                    super.onChanged();
                    ChooserRowAdapter.this.notifyDataSetChanged();
                }

                public void onInvalidated() {
                    super.onInvalidated();
                    ChooserRowAdapter.this.notifyDataSetInvalidated();
                }
            });
        }

        public int getCount() {
            return (int) (((double) (getCallerTargetRowCount() + getServiceTargetRowCount())) + Math.ceil((double) (((float) this.mChooserListAdapter.getStandardTargetCount()) / 4.0f)));
        }

        public int getCallerTargetRowCount() {
            return (int) Math.ceil((double) (((float) this.mChooserListAdapter.getCallerTargetCount()) / 4.0f));
        }

        public int getServiceTargetRowCount() {
            return this.mChooserListAdapter.getServiceTargetCount() == 0 ? 0 : 1;
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

        /* access modifiers changed from: package-private */
        public RowViewHolder createViewHolder(ViewGroup parent) {
            ViewGroup row = (ViewGroup) this.mLayoutInflater.inflate(17367116, parent, false);
            final RowViewHolder holder = new RowViewHolder(row, 4);
            int spec = View.MeasureSpec.makeMeasureSpec(0, 0);
            for (int i = 0; i < 4; i++) {
                View v = this.mChooserListAdapter.createView(row);
                final int column = i;
                v.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ChooserActivity.this.startSelected(holder.itemIndices[column], false, true);
                    }
                });
                v.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        ChooserActivity.this.showTargetDetails(ChooserRowAdapter.this.mChooserListAdapter.resolveInfoForPosition(holder.itemIndices[column], true));
                        return true;
                    }
                });
                row.addView(v);
                holder.cells[i] = v;
                ViewGroup.LayoutParams lp = v.getLayoutParams();
                v.measure(spec, spec);
                if (lp == null) {
                    row.setLayoutParams(new ViewGroup.LayoutParams(-1, v.getMeasuredHeight()));
                } else {
                    lp.height = v.getMeasuredHeight();
                }
                if (i != 3) {
                    row.addView(new Space(ChooserActivity.this), new LinearLayout.LayoutParams(0, 0, 1.0f));
                }
            }
            holder.measure();
            ViewGroup.LayoutParams lp2 = row.getLayoutParams();
            if (lp2 == null) {
                row.setLayoutParams(new ViewGroup.LayoutParams(-1, holder.measuredRowHeight));
            } else {
                lp2.height = holder.measuredRowHeight;
            }
            row.setTag(holder);
            return holder;
        }

        /* access modifiers changed from: package-private */
        public void bindViewHolder(int rowPosition, RowViewHolder holder) {
            int start = getFirstRowPosition(rowPosition);
            int startType = this.mChooserListAdapter.getPositionTargetType(start);
            int end = (start + 4) - 1;
            while (this.mChooserListAdapter.getPositionTargetType(end) != startType && end >= start) {
                end--;
            }
            if (startType == 1) {
                holder.row.setBackgroundColor(ChooserActivity.this.getColor(17170532));
                int nextStartType = this.mChooserListAdapter.getPositionTargetType(getFirstRowPosition(rowPosition + 1));
                int serviceSpacing = holder.row.getContext().getResources().getDimensionPixelSize(17104955);
                if (rowPosition != 0 || nextStartType == 1) {
                    int top = rowPosition == 0 ? serviceSpacing : 0;
                    if (nextStartType != 1) {
                        setVertPadding(holder, top, serviceSpacing);
                    } else {
                        setVertPadding(holder, top, 0);
                    }
                } else {
                    setVertPadding(holder, 0, 0);
                }
            } else {
                holder.row.setBackgroundColor(0);
                if (this.mChooserListAdapter.getPositionTargetType(getFirstRowPosition(rowPosition - 1)) == 1 || rowPosition == 0) {
                    setVertPadding(holder, holder.row.getContext().getResources().getDimensionPixelSize(17104955), 0);
                } else {
                    setVertPadding(holder, 0, 0);
                }
            }
            int oldHeight = holder.row.getLayoutParams().height;
            holder.row.getLayoutParams().height = Math.max(1, holder.measuredRowHeight);
            if (holder.row.getLayoutParams().height != oldHeight) {
                holder.row.requestLayout();
            }
            for (int i = 0; i < 4; i++) {
                View v = holder.cells[i];
                if (start + i <= end) {
                    v.setVisibility(0);
                    holder.itemIndices[i] = start + i;
                    this.mChooserListAdapter.bindView(holder.itemIndices[i], v);
                } else {
                    v.setVisibility(4);
                }
            }
        }

        private void setVertPadding(RowViewHolder holder, int top, int bottom) {
            holder.row.setPadding(holder.row.getPaddingLeft(), top, holder.row.getPaddingRight(), bottom);
        }

        /* access modifiers changed from: package-private */
        public int getFirstRowPosition(int row) {
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
            return callerCount + serviceCount + (((row - callerRows) - serviceRows) * 4);
        }
    }

    final class ChooserTargetInfo implements ResolverActivity.TargetInfo {
        private final ResolveInfo mBackupResolveInfo;
        private CharSequence mBadgeContentDescription;
        private Drawable mBadgeIcon = null;
        private final ChooserTarget mChooserTarget;
        private Drawable mDisplayIcon;
        private final int mFillInFlags;
        private final Intent mFillInIntent;
        private final float mModifiedScore;
        private final ResolverActivity.DisplayResolveInfo mSourceInfo;

        public ChooserTargetInfo(ResolverActivity.DisplayResolveInfo sourceInfo, ChooserTarget chooserTarget, float modifiedScore) {
            this.mSourceInfo = sourceInfo;
            this.mChooserTarget = chooserTarget;
            this.mModifiedScore = modifiedScore;
            if (sourceInfo != null) {
                ResolveInfo ri = sourceInfo.getResolveInfo();
                if (ri != null) {
                    ActivityInfo ai = ri.activityInfo;
                    if (!(ai == null || ai.applicationInfo == null)) {
                        PackageManager pm = ChooserActivity.this.getPackageManager();
                        this.mBadgeIcon = (ChooserActivity.this.mIsClonedProfile ? ChooserActivity.this.mPmForParent : pm).getApplicationIcon(ai.applicationInfo);
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
            } else {
                result = new Intent(result);
                if (this.mFillInIntent != null) {
                    result.fillIn(this.mFillInIntent, this.mFillInFlags);
                }
                result.fillIn(ChooserActivity.this.mReferrerFillInIntent, 0);
            }
            return result;
        }

        public boolean start(Activity activity, Bundle options) {
            throw new RuntimeException("ChooserTargets should be started as caller.");
        }

        public boolean startAsCaller(Activity activity, Bundle options, int userId) {
            Intent intent = getBaseIntentToSend();
            boolean ignoreTargetSecurity = false;
            if (intent == null) {
                return false;
            }
            intent.setComponent(this.mChooserTarget.getComponentName());
            intent.putExtras(this.mChooserTarget.getIntentExtras());
            if (this.mSourceInfo != null && this.mSourceInfo.getResolvedComponentName().getPackageName().equals(this.mChooserTarget.getComponentName().getPackageName())) {
                ignoreTargetSecurity = true;
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

        public ResolverActivity.TargetInfo cloneFilledIn(Intent fillInIntent, int flags) {
            return new ChooserTargetInfo(this, fillInIntent, flags);
        }

        public List<Intent> getAllSourceIntents() {
            List<Intent> results = new ArrayList<>();
            if (this.mSourceInfo != null) {
                results.add(this.mSourceInfo.getAllSourceIntents().get(0));
            }
            return results;
        }

        public boolean isPinned() {
            if (this.mSourceInfo != null) {
                return this.mSourceInfo.isPinned();
            }
            return false;
        }
    }

    static class ChooserTargetServiceConnection implements ServiceConnection {
        /* access modifiers changed from: private */
        public ChooserActivity mChooserActivity;
        private final IChooserTargetResult mChooserTargetResult = new IChooserTargetResult.Stub() {
            public void sendResult(List<ChooserTarget> targets) throws RemoteException {
                synchronized (ChooserTargetServiceConnection.this.mLock) {
                    if (ChooserTargetServiceConnection.this.mChooserActivity == null) {
                        Log.e(ChooserActivity.TAG, "destroyed ChooserTargetServiceConnection received result from " + ChooserTargetServiceConnection.this.mConnectedComponent + "; ignoring...");
                        return;
                    }
                    ChooserTargetServiceConnection.this.mChooserActivity.filterServiceTargets(ChooserTargetServiceConnection.this.mOriginalTarget.getResolveInfo().activityInfo.packageName, targets);
                    Message msg = Message.obtain();
                    msg.what = 1;
                    msg.obj = new ServiceResultInfo(ChooserTargetServiceConnection.this.mOriginalTarget, targets, ChooserTargetServiceConnection.this);
                    ChooserTargetServiceConnection.this.mChooserActivity.mChooserHandler.sendMessage(msg);
                }
            }
        };
        /* access modifiers changed from: private */
        public ComponentName mConnectedComponent;
        /* access modifiers changed from: private */
        public final Object mLock = new Object();
        /* access modifiers changed from: private */
        public ResolverActivity.DisplayResolveInfo mOriginalTarget;

        public ChooserTargetServiceConnection(ChooserActivity chooserActivity, ResolverActivity.DisplayResolveInfo dri) {
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
                    this.mChooserActivity.mServiceConnections.remove(this);
                    destroy();
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
                this.mChooserActivity.mServiceConnections.remove(this);
                if (this.mChooserActivity.mServiceConnections.isEmpty()) {
                    this.mChooserActivity.mChooserHandler.removeMessages(2);
                    this.mChooserActivity.sendVoiceChoicesIfNeeded();
                }
                this.mConnectedComponent = null;
                destroy();
            }
        }

        public void destroy() {
            synchronized (this.mLock) {
                this.mChooserActivity = null;
                this.mOriginalTarget = null;
            }
        }

        public String toString() {
            String str;
            StringBuilder sb = new StringBuilder();
            sb.append("ChooserTargetServiceConnection{service=");
            sb.append(this.mConnectedComponent);
            sb.append(", activity=");
            if (this.mOriginalTarget != null) {
                str = this.mOriginalTarget.getResolveInfo().activityInfo.toString();
            } else {
                str = "<connection destroyed>";
            }
            sb.append(str);
            sb.append("}");
            return sb.toString();
        }
    }

    class OffsetDataSetObserver extends DataSetObserver {
        private View mCachedView;
        private int mCachedViewType = -1;
        private final AbsListView mListView;

        public OffsetDataSetObserver(AbsListView listView) {
            this.mListView = listView;
        }

        public void onChanged() {
            if (ChooserActivity.this.mResolverDrawerLayout != null) {
                int chooserTargetRows = ChooserActivity.this.mChooserRowAdapter.getServiceTargetRowCount();
                int offset = 0;
                for (int i = 0; i < chooserTargetRows; i++) {
                    int pos = ChooserActivity.this.mChooserRowAdapter.getCallerTargetRowCount() + i;
                    int vt = ChooserActivity.this.mChooserRowAdapter.getItemViewType(pos);
                    if (vt != this.mCachedViewType) {
                        this.mCachedView = null;
                    }
                    View v = ChooserActivity.this.mChooserRowAdapter.getView(pos, this.mCachedView, this.mListView);
                    offset += ((RowViewHolder) v.getTag()).measuredRowHeight;
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
        private ResolverActivity.TargetInfo mSelectedTarget;

        public RefinementResultReceiver(ChooserActivity host, ResolverActivity.TargetInfo target, Handler handler) {
            super(handler);
            this.mChooserActivity = host;
            this.mSelectedTarget = target;
        }

        /* access modifiers changed from: protected */
        public void onReceiveResult(int resultCode, Bundle resultData) {
            if (this.mChooserActivity == null) {
                Log.e(ChooserActivity.TAG, "Destroyed RefinementResultReceiver received a result");
            } else if (resultData == null) {
                Log.e(ChooserActivity.TAG, "RefinementResultReceiver received null resultData");
            } else {
                switch (resultCode) {
                    case -1:
                        Parcelable intentParcelable = resultData.getParcelable("android.intent.extra.INTENT");
                        if (!(intentParcelable instanceof Intent)) {
                            Log.e(ChooserActivity.TAG, "RefinementResultReceiver received RESULT_OK but no Intent in resultData with key Intent.EXTRA_INTENT");
                            break;
                        } else {
                            this.mChooserActivity.onRefinementResult(this.mSelectedTarget, (Intent) intentParcelable);
                            break;
                        }
                    case 0:
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

    static class RowViewHolder {
        final View[] cells;
        int[] itemIndices;
        int measuredRowHeight;
        final ViewGroup row;

        public RowViewHolder(ViewGroup row2, int cellCount) {
            this.row = row2;
            this.cells = new View[cellCount];
            this.itemIndices = new int[cellCount];
        }

        public void measure() {
            int spec = View.MeasureSpec.makeMeasureSpec(0, 0);
            this.row.measure(spec, spec);
            this.measuredRowHeight = this.row.getMeasuredHeight();
        }
    }

    static class ServiceResultInfo {
        public final ChooserTargetServiceConnection connection;
        public final ResolverActivity.DisplayResolveInfo originalTarget;
        public final List<ChooserTarget> resultTargets;

        public ServiceResultInfo(ResolverActivity.DisplayResolveInfo ot, List<ChooserTarget> rt, ChooserTargetServiceConnection c) {
            this.originalTarget = ot;
            this.resultTargets = rt;
            this.connection = c;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        Intent target;
        long intentReceivedTime = System.currentTimeMillis();
        this.mIsSuccessfullySelected = false;
        Intent intent = getIntent();
        Parcelable targetParcelable = intent.getParcelableExtra("android.intent.extra.INTENT");
        if (!(targetParcelable instanceof Intent)) {
            Log.w(TAG, "Target is not an intent: " + targetParcelable);
            finish();
            super.onCreate(null);
            return;
        }
        Intent target2 = (Intent) targetParcelable;
        if (target2 != null) {
            modifyTargetIntent(target2);
        }
        Parcelable[] targetsParcelable = intent.getParcelableArrayExtra("android.intent.extra.ALTERNATE_INTENTS");
        if (targetsParcelable != null) {
            boolean offset = target2 == null;
            Intent[] additionalTargets = new Intent[(offset ? targetsParcelable.length - 1 : targetsParcelable.length)];
            Intent target3 = target2;
            for (int i = 0; i < targetsParcelable.length; i++) {
                if (!(targetsParcelable[i] instanceof Intent)) {
                    Log.w(TAG, "EXTRA_ALTERNATE_INTENTS array entry #" + i + " is not an Intent: " + targetsParcelable[i]);
                    finish();
                    super.onCreate(null);
                    return;
                }
                Intent additionalTarget = (Intent) targetsParcelable[i];
                if (i == 0 && target3 == null) {
                    target3 = additionalTarget;
                    modifyTargetIntent(target3);
                } else {
                    additionalTargets[offset ? i - 1 : i] = additionalTarget;
                    modifyTargetIntent(additionalTarget);
                }
            }
            setAdditionalTargets(additionalTargets);
            target = target3;
        } else {
            target = target2;
        }
        this.mReplacementExtras = intent.getBundleExtra("android.intent.extra.REPLACEMENT_EXTRAS");
        CharSequence title = intent.getCharSequenceExtra("android.intent.extra.TITLE");
        int defaultTitleRes = 0;
        if (title == null) {
            defaultTitleRes = 17039747;
        }
        int defaultTitleRes2 = defaultTitleRes;
        Parcelable[] pa = intent.getParcelableArrayExtra("android.intent.extra.INITIAL_INTENTS");
        Intent[] initialIntents = null;
        if (pa != null) {
            initialIntents = new Intent[pa.length];
            for (int i2 = 0; i2 < pa.length; i2++) {
                if (!(pa[i2] instanceof Intent)) {
                    Log.w(TAG, "Initial intent #" + i2 + " not an Intent: " + pa[i2]);
                    finish();
                    super.onCreate(null);
                    return;
                }
                Intent in = (Intent) pa[i2];
                modifyTargetIntent(in);
                initialIntents[i2] = in;
            }
        }
        Intent[] initialIntents2 = initialIntents;
        this.mReferrerFillInIntent = new Intent().putExtra("android.intent.extra.REFERRER", getReferrer());
        this.mChosenComponentSender = (IntentSender) intent.getParcelableExtra("android.intent.extra.CHOSEN_COMPONENT_INTENT_SENDER");
        this.mRefinementIntentSender = (IntentSender) intent.getParcelableExtra("android.intent.extra.CHOOSER_REFINEMENT_INTENT_SENDER");
        setSafeForwardingMode(true);
        Parcelable[] pa2 = intent.getParcelableArrayExtra("android.intent.extra.EXCLUDE_COMPONENTS");
        if (pa2 != null) {
            ComponentName[] names = new ComponentName[pa2.length];
            int i3 = 0;
            while (true) {
                if (i3 >= pa2.length) {
                    break;
                } else if (!(pa2[i3] instanceof ComponentName)) {
                    Log.w(TAG, "Filtered component #" + i3 + " not a ComponentName: " + pa2[i3]);
                    names = null;
                    break;
                } else {
                    names[i3] = (ComponentName) pa2[i3];
                    i3++;
                }
            }
            this.mFilteredComponentNames = names;
        }
        Parcelable[] pa3 = intent.getParcelableArrayExtra("android.intent.extra.CHOOSER_TARGETS");
        if (pa3 != null) {
            ChooserTarget[] targets = new ChooserTarget[pa3.length];
            int i4 = 0;
            while (true) {
                if (i4 >= pa3.length) {
                    break;
                } else if (!(pa3[i4] instanceof ChooserTarget)) {
                    Log.w(TAG, "Chooser target #" + i4 + " not a ChooserTarget: " + pa3[i4]);
                    targets = null;
                    break;
                } else {
                    targets[i4] = (ChooserTarget) pa3[i4];
                    i4++;
                }
            }
            this.mCallerChooserTargets = targets;
        }
        this.mPinnedSharedPrefs = getPinnedSharedPrefs(this);
        setRetainInOnStop(intent.getBooleanExtra(EXTRA_PRIVATE_RETAIN_IN_ON_STOP, false));
        Parcelable[] parcelableArr = pa3;
        super.onCreate(savedInstanceState, target, title, defaultTitleRes2, initialIntents2, null, false);
        MetricsLogger.action((Context) this, 214);
        this.mChooserShownTime = System.currentTimeMillis();
        MetricsLogger.histogram(null, "system_cost_for_smart_sharing", (int) (this.mChooserShownTime - intentReceivedTime));
    }

    static SharedPreferences getPinnedSharedPrefs(Context context) {
        return context.getSharedPreferences(new File(new File(Environment.getDataUserCePackageDirectory(StorageManager.UUID_PRIVATE_INTERNAL, context.getUserId(), context.getPackageName()), "shared_prefs"), "chooser_pin_settings.xml"), 0);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.mRefinementResultReceiver != null) {
            this.mRefinementResultReceiver.destroy();
            this.mRefinementResultReceiver = null;
        }
        unbindRemainingServices();
        this.mChooserHandler.removeMessages(1);
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
        if (!aInfo.name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_PARENT) && !aInfo.name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_MANAGED_PROFILE)) {
            return result;
        }
        Intent result2 = Intent.createChooser(result, getIntent().getCharSequenceExtra("android.intent.extra.TITLE"));
        result2.putExtra("android.intent.extra.AUTO_LAUNCH_SINGLE_CHOICE", false);
        return result2;
    }

    public void onActivityStarted(ResolverActivity.TargetInfo cti) {
        if (this.mChosenComponentSender != null) {
            ComponentName target = cti.getResolvedComponentName();
            if (target != null) {
                try {
                    this.mChosenComponentSender.sendIntent(this, -1, new Intent().putExtra("android.intent.extra.CHOSEN_COMPONENT", target), null, null);
                } catch (IntentSender.SendIntentException e) {
                    Slog.e(TAG, "Unable to launch supplied IntentSender to report the chosen component: " + e);
                }
            }
        }
    }

    public void onPrepareAdapterView(AbsListView adapterView, ResolverActivity.ResolveListAdapter adapter) {
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
        return 17367115;
    }

    public boolean shouldGetActivityMetadata() {
        return true;
    }

    public boolean shouldAutoLaunchSingleChoice(ResolverActivity.TargetInfo target) {
        return getIntent().getBooleanExtra("android.intent.extra.AUTO_LAUNCH_SINGLE_CHOICE", super.shouldAutoLaunchSingleChoice(target));
    }

    public void showTargetDetails(ResolveInfo ri) {
        if (ri != null) {
            ComponentName name = ri.activityInfo.getComponentName();
            new ResolverTargetActionsDialogFragment(ri.loadLabel(getPackageManager()), name, this.mPinnedSharedPrefs.getBoolean(name.flattenToString(), false)).show(getFragmentManager(), TARGET_DETAILS_FRAGMENT_TAG);
        }
    }

    private void modifyTargetIntent(Intent in) {
        String action = in.getAction();
        if ("android.intent.action.SEND".equals(action) || "android.intent.action.SEND_MULTIPLE".equals(action)) {
            in.addFlags(134742016);
        }
    }

    /* access modifiers changed from: protected */
    public boolean onTargetSelected(ResolverActivity.TargetInfo target, boolean alwaysCheck) {
        if (this.mRefinementIntentSender != null) {
            Intent fillIn = new Intent();
            List<Intent> sourceIntents = target.getAllSourceIntents();
            if (!sourceIntents.isEmpty()) {
                fillIn.putExtra("android.intent.extra.INTENT", sourceIntents.get(0));
                if (sourceIntents.size() > 1) {
                    Intent[] alts = new Intent[(sourceIntents.size() - 1)];
                    int N = sourceIntents.size();
                    for (int i = 1; i < N; i++) {
                        alts[i - 1] = sourceIntents.get(i);
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
                    return false;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Refinement IntentSender failed to send", e);
                }
            }
        }
        updateModelAndChooserCounts(target);
        return super.onTargetSelected(target, alwaysCheck);
    }

    public void startSelected(int which, boolean always, boolean filtered) {
        long selectionCost = System.currentTimeMillis() - this.mChooserShownTime;
        super.startSelected(which, always, filtered);
        if (this.mChooserListAdapter != null) {
            int cat = 0;
            int value = which;
            switch (this.mChooserListAdapter.getPositionTargetType(which)) {
                case 0:
                    cat = 215;
                    break;
                case 1:
                    cat = 216;
                    value -= this.mChooserListAdapter.getCallerTargetCount();
                    break;
                case 2:
                    cat = 217;
                    value -= this.mChooserListAdapter.getCallerTargetCount() + this.mChooserListAdapter.getServiceTargetCount();
                    break;
            }
            if (cat != 0) {
                MetricsLogger.action((Context) this, cat, value);
            }
            if (this.mIsSuccessfullySelected) {
                MetricsLogger.histogram(null, "user_selection_cost_for_smart_sharing", (int) selectionCost);
                MetricsLogger.histogram(null, "app_position_for_smart_sharing", value);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void queryTargetServices(ChooserListAdapter adapter) {
        String serviceName;
        PackageManager pm = getPackageManager();
        int targetsToQuery = 0;
        int N = adapter.getDisplayResolveInfoCount();
        for (int i = 0; i < N; i++) {
            ResolverActivity.DisplayResolveInfo dri = adapter.getDisplayResolveInfo(i);
            if (adapter.getScore(dri) != 0.0f) {
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
                        if (!"android.permission.BIND_CHOOSER_TARGET_SERVICE".equals(pm.getServiceInfo(serviceComponent, 0).permission)) {
                            Log.w(TAG, "ChooserTargetService " + serviceComponent + " does not require permission " + "android.permission.BIND_CHOOSER_TARGET_SERVICE" + " - this service will not be queried for ChooserTargets. add android:permission=\"" + "android.permission.BIND_CHOOSER_TARGET_SERVICE" + "\" to the <service> tag for " + serviceComponent + " in the manifest.");
                        } else {
                            ChooserTargetServiceConnection conn = new ChooserTargetServiceConnection(this, dri);
                            if (bindServiceAsUser(serviceIntent, conn, 5, Process.myUserHandle())) {
                                this.mServiceConnections.add(conn);
                                targetsToQuery++;
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "Could not look up service " + serviceComponent + "; component name not found");
                    }
                }
                if (targetsToQuery >= 5) {
                    break;
                }
            }
        }
        if (!this.mServiceConnections.isEmpty()) {
            this.mChooserHandler.sendEmptyMessageDelayed(2, 2000);
        } else {
            sendVoiceChoicesIfNeeded();
        }
    }

    private String convertServiceName(String packageName, String serviceName) {
        String fullName = null;
        if (TextUtils.isEmpty(serviceName)) {
            return null;
        }
        if (serviceName.startsWith(".")) {
            fullName = packageName + serviceName;
        } else if (serviceName.indexOf(46) >= 0) {
            fullName = serviceName;
        }
        return fullName;
    }

    /* access modifiers changed from: package-private */
    public void unbindRemainingServices() {
        int N = this.mServiceConnections.size();
        for (int i = 0; i < N; i++) {
            ChooserTargetServiceConnection conn = this.mServiceConnections.get(i);
            unbindService(conn);
            conn.destroy();
        }
        this.mServiceConnections.clear();
        this.mChooserHandler.removeMessages(2);
    }

    public void onSetupVoiceInteraction() {
    }

    /* access modifiers changed from: package-private */
    public void updateModelAndChooserCounts(ResolverActivity.TargetInfo info) {
        if (info != null) {
            ResolveInfo ri = info.getResolveInfo();
            Intent targetIntent = getTargetIntent();
            if (!(ri == null || ri.activityInfo == null || targetIntent == null || this.mAdapter == null)) {
                this.mAdapter.updateModel(info.getResolvedComponentName());
                this.mAdapter.updateChooserCounts(ri.activityInfo.packageName, getUserId(), targetIntent.getAction());
            }
        }
        this.mIsSuccessfullySelected = true;
    }

    /* access modifiers changed from: package-private */
    public void onRefinementResult(ResolverActivity.TargetInfo selectedTarget, Intent matchingIntent) {
        if (this.mRefinementResultReceiver != null) {
            this.mRefinementResultReceiver.destroy();
            this.mRefinementResultReceiver = null;
        }
        if (selectedTarget == null) {
            Log.e(TAG, "Refinement result intent did not match any known targets; canceling");
        } else if (!checkTargetSourceIntent(selectedTarget, matchingIntent)) {
            Log.e(TAG, "onRefinementResult: Selected target " + selectedTarget + " cannot match refined source intent " + matchingIntent);
        } else {
            ResolverActivity.TargetInfo clonedTarget = selectedTarget.cloneFilledIn(matchingIntent, 0);
            if (super.onTargetSelected(clonedTarget, false)) {
                updateModelAndChooserCounts(clonedTarget);
                finish();
                return;
            }
        }
        onRefinementCanceled();
    }

    /* access modifiers changed from: package-private */
    public void onRefinementCanceled() {
        if (this.mRefinementResultReceiver != null) {
            this.mRefinementResultReceiver.destroy();
            this.mRefinementResultReceiver = null;
        }
        finish();
    }

    /* access modifiers changed from: package-private */
    public boolean checkTargetSourceIntent(ResolverActivity.TargetInfo target, Intent matchingIntent) {
        List<Intent> targetIntents = target.getAllSourceIntents();
        int N = targetIntents.size();
        for (int i = 0; i < N; i++) {
            if (targetIntents.get(i).filterEquals(matchingIntent)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void filterServiceTargets(String packageName, List<ChooserTarget> targets) {
        if (targets != null) {
            PackageManager pm = getPackageManager();
            for (int i = targets.size() - 1; i >= 0; i--) {
                ComponentName targetName = targets.get(i).getComponentName();
                if (packageName == null || !packageName.equals(targetName.getPackageName())) {
                    boolean remove = false;
                    try {
                        ActivityInfo ai = pm.getActivityInfo(targetName, 0);
                        if (!ai.exported || ai.permission != null) {
                            remove = true;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
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

    public ResolverActivity.ResolveListAdapter createAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
        ChooserListAdapter adapter = new ChooserListAdapter(this, context, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed, createListController());
        return adapter;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public ResolverListController createListController() {
        ChooserListController chooserListController = new ChooserListController(this, this.mPm, getTargetIntent(), getReferrerPackageName(), this.mLaunchedFromUid);
        return chooserListController;
    }
}
