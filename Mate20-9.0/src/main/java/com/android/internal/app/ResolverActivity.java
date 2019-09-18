package com.android.internal.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.VoiceInteractor;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.widget.ResolverDrawerLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ResolverActivity extends Activity {
    private static final boolean DEBUG = false;
    private static final String TAG = "ResolverActivity";
    protected ResolveListAdapter mAdapter;
    /* access modifiers changed from: private */
    public AbsListView mAdapterView;
    /* access modifiers changed from: private */
    public Button mAlwaysButton;
    private int mDefaultTitleResId;
    private int mIconDpi;
    IconDrawableFactory mIconFactory;
    private final ArrayList<Intent> mIntents = new ArrayList<>();
    protected boolean mIsClonedProfile;
    /* access modifiers changed from: private */
    public int mLastSelected = -1;
    protected int mLaunchedFromUid;
    private int mLayoutId;
    /* access modifiers changed from: private */
    public Button mOnceButton;
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        public void onSomePackagesChanged() {
            ResolverActivity.this.mAdapter.handlePackagesChanged();
            if (ResolverActivity.this.mProfileView != null) {
                ResolverActivity.this.bindProfileView();
            }
        }

        public boolean onPackageChanged(String packageName, int uid, String[] components) {
            return true;
        }
    };
    /* access modifiers changed from: private */
    public PickTargetOptionRequest mPickOptionRequest;
    protected PackageManager mPm;
    protected PackageManager mPmForParent;
    /* access modifiers changed from: private */
    public Runnable mPostListReadyRunnable;
    /* access modifiers changed from: private */
    public int mProfileSwitchMessageId = -1;
    /* access modifiers changed from: private */
    public View mProfileView;
    private String mReferrerPackage;
    private boolean mRegistered;
    protected ResolverDrawerLayout mResolverDrawerLayout;
    private boolean mResolvingHome = false;
    private boolean mRetainInOnStop;
    private boolean mSafeForwardingMode;
    /* access modifiers changed from: private */
    public boolean mSupportsAlwaysUseOption;
    private CharSequence mTitle;

    private enum ActionTitle {
        VIEW("android.intent.action.VIEW", 17041379, 17041381, 17041380),
        EDIT("android.intent.action.EDIT", 17041364, 17041366, 17041365),
        SEND("android.intent.action.SEND", 17041373, 17041375, 17041374),
        SENDTO("android.intent.action.SENDTO", 17041376, 17041378, 17041377),
        SEND_MULTIPLE("android.intent.action.SEND_MULTIPLE", 17041373, 17041375, 17041374),
        CAPTURE_IMAGE("android.media.action.IMAGE_CAPTURE", 17041370, 17041372, 17041371),
        DEFAULT(null, 17041361, 17041363, 17041362),
        HOME("android.intent.action.MAIN", 17041367, 17041369, 17041368);
        
        public final String action;
        public final int labelRes;
        public final int namedTitleRes;
        public final int titleRes;

        private ActionTitle(String action2, int titleRes2, int namedTitleRes2, int labelRes2) {
            this.action = action2;
            this.titleRes = titleRes2;
            this.namedTitleRes = namedTitleRes2;
            this.labelRes = labelRes2;
        }

        public static ActionTitle forAction(String action2) {
            for (ActionTitle title : values()) {
                if (title != HOME && action2 != null && action2.equals(title.action)) {
                    return title;
                }
            }
            return DEFAULT;
        }
    }

    public final class DisplayResolveInfo implements TargetInfo {
        private Drawable mBadge;
        private Drawable mDisplayIcon;
        private final CharSequence mDisplayLabel;
        private final CharSequence mExtendedInfo;
        private boolean mPinned;
        /* access modifiers changed from: private */
        public final ResolveInfo mResolveInfo;
        private final Intent mResolvedIntent;
        private final List<Intent> mSourceIntents = new ArrayList();

        public DisplayResolveInfo(Intent originalIntent, ResolveInfo pri, CharSequence pLabel, CharSequence pInfo, Intent pOrigIntent) {
            this.mSourceIntents.add(originalIntent);
            this.mResolveInfo = pri;
            this.mDisplayLabel = pLabel;
            this.mExtendedInfo = pInfo;
            Intent intent = new Intent(pOrigIntent != null ? pOrigIntent : ResolverActivity.this.getReplacementIntent(pri.activityInfo, ResolverActivity.this.getTargetIntent()));
            intent.addFlags(50331648);
            ActivityInfo ai = this.mResolveInfo.activityInfo;
            intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
            this.mResolvedIntent = intent;
            this.mResolvedIntent.addHwFlags(1024);
        }

        private DisplayResolveInfo(DisplayResolveInfo other, Intent fillInIntent, int flags) {
            this.mSourceIntents.addAll(other.getAllSourceIntents());
            this.mResolveInfo = other.mResolveInfo;
            this.mDisplayLabel = other.mDisplayLabel;
            this.mDisplayIcon = other.mDisplayIcon;
            this.mExtendedInfo = other.mExtendedInfo;
            this.mResolvedIntent = new Intent(other.mResolvedIntent);
            this.mResolvedIntent.fillIn(fillInIntent, flags);
            this.mPinned = other.mPinned;
        }

        public ResolveInfo getResolveInfo() {
            return this.mResolveInfo;
        }

        public CharSequence getDisplayLabel() {
            return this.mDisplayLabel;
        }

        public Drawable getDisplayIcon() {
            return this.mDisplayIcon;
        }

        public Drawable getBadgeIcon() {
            if (TextUtils.isEmpty(getExtendedInfo())) {
                return null;
            }
            if (!(this.mBadge != null || this.mResolveInfo == null || this.mResolveInfo.activityInfo == null || this.mResolveInfo.activityInfo.applicationInfo == null)) {
                if (this.mResolveInfo.activityInfo.icon == 0 || this.mResolveInfo.activityInfo.icon == this.mResolveInfo.activityInfo.applicationInfo.icon) {
                    return null;
                }
                this.mBadge = this.mResolveInfo.activityInfo.applicationInfo.loadIcon(ResolverActivity.this.mIsClonedProfile ? ResolverActivity.this.mPmForParent : ResolverActivity.this.mPm);
            }
            return this.mBadge;
        }

        public CharSequence getBadgeContentDescription() {
            return null;
        }

        public TargetInfo cloneFilledIn(Intent fillInIntent, int flags) {
            return new DisplayResolveInfo(this, fillInIntent, flags);
        }

        public List<Intent> getAllSourceIntents() {
            return this.mSourceIntents;
        }

        public void addAlternateSourceIntent(Intent alt) {
            this.mSourceIntents.add(alt);
        }

        public void setDisplayIcon(Drawable icon) {
            this.mDisplayIcon = icon;
        }

        public boolean hasDisplayIcon() {
            return this.mDisplayIcon != null;
        }

        public CharSequence getExtendedInfo() {
            return this.mExtendedInfo;
        }

        public Intent getResolvedIntent() {
            return this.mResolvedIntent;
        }

        public ComponentName getResolvedComponentName() {
            return new ComponentName(this.mResolveInfo.activityInfo.packageName, this.mResolveInfo.activityInfo.name);
        }

        public boolean start(Activity activity, Bundle options) {
            try {
                activity.startActivity(this.mResolvedIntent, options);
                return true;
            } catch (SecurityException e) {
                Log.e(ResolverActivity.TAG, "start", e);
                return false;
            }
        }

        public boolean startAsCaller(Activity activity, Bundle options, int userId) {
            try {
                activity.startActivityAsCaller(this.mResolvedIntent, options, false, userId);
                return true;
            } catch (SecurityException e) {
                Log.e(ResolverActivity.TAG, "startAsCaller", e);
                return false;
            }
        }

        public boolean startAsUser(Activity activity, Bundle options, UserHandle user) {
            try {
                activity.startActivityAsUser(this.mResolvedIntent, options, user);
                return false;
            } catch (SecurityException e) {
                Log.e(ResolverActivity.TAG, "startAsUser", e);
                return false;
            }
        }

        public boolean isPinned() {
            return this.mPinned;
        }

        public void setPinned(boolean pinned) {
            this.mPinned = pinned;
        }
    }

    class ItemClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        ItemClickListener() {
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = parent instanceof ListView ? (ListView) parent : null;
            if (listView != null) {
                position -= listView.getHeaderViewsCount();
            }
            if (position >= 0 && ResolverActivity.this.mAdapter.resolveInfoForPosition(position, true) != null) {
                int checkedPos = ResolverActivity.this.mAdapterView.getCheckedItemPosition();
                boolean hasValidSelection = checkedPos != -1;
                if (!ResolverActivity.this.mSupportsAlwaysUseOption || ResolverActivity.this.mAdapter.hasFilteredItem() || ((hasValidSelection && ResolverActivity.this.mLastSelected == checkedPos) || ResolverActivity.this.mAlwaysButton == null)) {
                    ResolverActivity.this.startSelected(position, false, true);
                } else {
                    ResolverActivity.this.setAlwaysButtonEnabled(hasValidSelection, checkedPos, true);
                    ResolverActivity.this.mOnceButton.setEnabled(hasValidSelection);
                    if (hasValidSelection) {
                        ResolverActivity.this.mAdapterView.smoothScrollToPosition(checkedPos);
                    }
                    int unused = ResolverActivity.this.mLastSelected = checkedPos;
                }
            }
        }

        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = parent instanceof ListView ? (ListView) parent : null;
            if (listView != null) {
                position -= listView.getHeaderViewsCount();
            }
            if (position < 0) {
                return false;
            }
            ResolverActivity.this.showTargetDetails(ResolverActivity.this.mAdapter.resolveInfoForPosition(position, true));
            return true;
        }
    }

    class LoadAdapterIconTask extends LoadIconTask {
        public LoadAdapterIconTask(DisplayResolveInfo dri) {
            super(dri);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Drawable d) {
            super.onPostExecute(d);
            if (ResolverActivity.this.mProfileView != null && ResolverActivity.this.mAdapter.getOtherProfile() == this.mDisplayResolveInfo) {
                ResolverActivity.this.bindProfileView();
            }
            ResolverActivity.this.mAdapter.notifyDataSetChanged();
        }
    }

    class LoadIconIntoViewTask extends LoadIconTask {
        private final ImageView mTargetView;

        public LoadIconIntoViewTask(DisplayResolveInfo dri, ImageView target) {
            super(dri);
            this.mTargetView = target;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Drawable d) {
            super.onPostExecute(d);
            this.mTargetView.setImageDrawable(d);
        }
    }

    abstract class LoadIconTask extends AsyncTask<Void, Void, Drawable> {
        protected final DisplayResolveInfo mDisplayResolveInfo;
        private final ResolveInfo mResolveInfo;

        public LoadIconTask(DisplayResolveInfo dri) {
            this.mDisplayResolveInfo = dri;
            this.mResolveInfo = dri.getResolveInfo();
        }

        /* access modifiers changed from: protected */
        public Drawable doInBackground(Void... params) {
            return ResolverActivity.this.loadIconForResolveInfo(this.mResolveInfo);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Drawable d) {
            this.mDisplayResolveInfo.setDisplayIcon(d);
        }
    }

    static class PickTargetOptionRequest extends VoiceInteractor.PickOptionRequest {
        public PickTargetOptionRequest(VoiceInteractor.Prompt prompt, VoiceInteractor.PickOptionRequest.Option[] options, Bundle extras) {
            super(prompt, options, extras);
        }

        public void onCancel() {
            super.onCancel();
            ResolverActivity ra = (ResolverActivity) getActivity();
            if (ra != null) {
                PickTargetOptionRequest unused = ra.mPickOptionRequest = null;
                ra.finish();
            }
        }

        public void onPickOptionResult(boolean finished, VoiceInteractor.PickOptionRequest.Option[] selections, Bundle result) {
            super.onPickOptionResult(finished, selections, result);
            if (selections.length == 1) {
                ResolverActivity ra = (ResolverActivity) getActivity();
                if (ra != null && ra.onTargetSelected(ra.mAdapter.getItem(selections[0].getIndex()), false)) {
                    PickTargetOptionRequest unused = ra.mPickOptionRequest = null;
                    ra.finish();
                }
            }
        }
    }

    public class ResolveListAdapter extends BaseAdapter {
        private final List<ResolveInfo> mBaseResolveList;
        List<DisplayResolveInfo> mDisplayList;
        private boolean mFilterLastUsed;
        private boolean mHasExtendedInfo;
        protected final LayoutInflater mInflater;
        private final Intent[] mInitialIntents;
        private final List<Intent> mIntents;
        protected ResolveInfo mLastChosen;
        private int mLastChosenPosition = -1;
        /* access modifiers changed from: private */
        public DisplayResolveInfo mOtherProfile;
        /* access modifiers changed from: private */
        public int mPlaceholderCount;
        /* access modifiers changed from: private */
        public ResolverListController mResolverListController;
        List<ResolvedComponentInfo> mUnfilteredResolveList;

        public ResolveListAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed, ResolverListController resolverListController) {
            this.mIntents = payloadIntents;
            this.mInitialIntents = initialIntents;
            this.mBaseResolveList = rList;
            ResolverActivity.this.mLaunchedFromUid = launchedFromUid;
            this.mInflater = LayoutInflater.from(context);
            this.mDisplayList = new ArrayList();
            this.mFilterLastUsed = filterLastUsed;
            this.mResolverListController = resolverListController;
        }

        public void handlePackagesChanged() {
            rebuildList();
            if (getCount() == 0) {
                ResolverActivity.this.finish();
            }
        }

        public void setPlaceholderCount(int count) {
            this.mPlaceholderCount = count;
        }

        public int getPlaceholderCount() {
            return this.mPlaceholderCount;
        }

        public DisplayResolveInfo getFilteredItem() {
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return null;
            }
            return this.mDisplayList.get(this.mLastChosenPosition);
        }

        public DisplayResolveInfo getOtherProfile() {
            return this.mOtherProfile;
        }

        public int getFilteredPosition() {
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return -1;
            }
            return this.mLastChosenPosition;
        }

        public boolean hasFilteredItem() {
            return this.mFilterLastUsed && this.mLastChosen != null;
        }

        public float getScore(DisplayResolveInfo target) {
            return this.mResolverListController.getScore(target);
        }

        public void updateModel(ComponentName componentName) {
            this.mResolverListController.updateModel(componentName);
        }

        public void updateChooserCounts(String packageName, int userId, String action) {
            this.mResolverListController.updateChooserCounts(packageName, userId, action);
        }

        /* access modifiers changed from: protected */
        public boolean rebuildList() {
            List<ResolvedComponentInfo> currentResolveList;
            this.mOtherProfile = null;
            this.mLastChosen = null;
            this.mLastChosenPosition = -1;
            this.mDisplayList.clear();
            if (this.mBaseResolveList != null) {
                List<ResolvedComponentInfo> arrayList = new ArrayList<>();
                this.mUnfilteredResolveList = arrayList;
                currentResolveList = arrayList;
                this.mResolverListController.addResolveListDedupe(currentResolveList, ResolverActivity.this.getTargetIntent(), this.mBaseResolveList);
            } else {
                List<ResolvedComponentInfo> resolversForIntent = this.mResolverListController.getResolversForIntent(shouldGetResolvedFilter(), ResolverActivity.this.shouldGetActivityMetadata(), this.mIntents);
                this.mUnfilteredResolveList = resolversForIntent;
                currentResolveList = resolversForIntent;
                if (currentResolveList == null) {
                    processSortedList(currentResolveList);
                    return true;
                }
                List<ResolvedComponentInfo> originalList = this.mResolverListController.filterIneligibleActivities(currentResolveList, true);
                if (originalList != null) {
                    this.mUnfilteredResolveList = originalList;
                }
            }
            Iterator<ResolvedComponentInfo> it = currentResolveList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ResolvedComponentInfo info = it.next();
                if (info.getResolveInfoAt(0).targetUserId != -2) {
                    DisplayResolveInfo displayResolveInfo = new DisplayResolveInfo(info.getIntentAt(0), info.getResolveInfoAt(0), info.getResolveInfoAt(0).loadLabel(ResolverActivity.this.mPm), info.getResolveInfoAt(0).loadLabel(ResolverActivity.this.mPm), ResolverActivity.this.getReplacementIntent(info.getResolveInfoAt(0).activityInfo, info.getIntentAt(0)));
                    this.mOtherProfile = displayResolveInfo;
                    currentResolveList.remove(info);
                    break;
                }
            }
            if (this.mOtherProfile == null) {
                try {
                    this.mLastChosen = this.mResolverListController.getLastChosen();
                } catch (RemoteException re) {
                    Log.d(ResolverActivity.TAG, "Error calling getLastChosenActivity\n" + re);
                }
            }
            if (currentResolveList != null) {
                int size = currentResolveList.size();
                int i = size;
                if (size > 0) {
                    List<ResolvedComponentInfo> originalList2 = this.mResolverListController.filterLowPriority(currentResolveList, this.mUnfilteredResolveList == currentResolveList);
                    if (originalList2 != null) {
                        this.mUnfilteredResolveList = originalList2;
                    }
                    if (currentResolveList.size() > 1) {
                        int placeholderCount = currentResolveList.size();
                        if (ResolverActivity.this.useLayoutWithDefault()) {
                            placeholderCount--;
                        }
                        setPlaceholderCount(placeholderCount);
                        new AsyncTask<List<ResolvedComponentInfo>, Void, List<ResolvedComponentInfo>>() {
                            /* access modifiers changed from: protected */
                            public List<ResolvedComponentInfo> doInBackground(List<ResolvedComponentInfo>... params) {
                                ResolveListAdapter.this.mResolverListController.sort(params[0]);
                                return params[0];
                            }

                            /* access modifiers changed from: protected */
                            public void onPostExecute(List<ResolvedComponentInfo> sortedComponents) {
                                ResolveListAdapter.this.processSortedList(sortedComponents);
                                if (ResolverActivity.this.mProfileView != null) {
                                    ResolverActivity.this.bindProfileView();
                                }
                                ResolveListAdapter.this.notifyDataSetChanged();
                            }
                        }.execute(new List[]{currentResolveList});
                        postListReadyRunnable();
                        return false;
                    }
                    processSortedList(currentResolveList);
                    return true;
                }
            }
            processSortedList(currentResolveList);
            return true;
        }

        /* access modifiers changed from: private */
        public void processSortedList(List<ResolvedComponentInfo> sortedComponents) {
            int i;
            List<ResolvedComponentInfo> list = sortedComponents;
            if (list != null) {
                int size = sortedComponents.size();
                int N = size;
                if (size != 0) {
                    boolean z = true;
                    if (this.mInitialIntents != null) {
                        int i2 = 0;
                        while (i2 < this.mInitialIntents.length) {
                            Intent ii = this.mInitialIntents[i2];
                            if (ii != null) {
                                PackageManager packageManager = ResolverActivity.this.getPackageManager();
                                if (ResolverActivity.this.mIsClonedProfile) {
                                    i = 4202496;
                                } else {
                                    i = 0;
                                }
                                ActivityInfo ai = ii.resolveActivityInfo(packageManager, i);
                                if (ai == null) {
                                    Log.w(ResolverActivity.TAG, "No activity found for " + ii);
                                } else {
                                    ResolveInfo ri = new ResolveInfo();
                                    ri.activityInfo = ai;
                                    UserManager userManager = (UserManager) ResolverActivity.this.getSystemService("user");
                                    if (ii instanceof LabeledIntent) {
                                        LabeledIntent li = (LabeledIntent) ii;
                                        ri.resolvePackageName = li.getSourcePackage();
                                        ri.labelRes = li.getLabelResource();
                                        ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                                        ri.icon = li.getIconResource();
                                        ri.iconResourceId = ri.icon;
                                    }
                                    if (userManager.isManagedProfile()) {
                                        ri.noResourceId = z;
                                        ri.icon = 0;
                                    }
                                    DisplayResolveInfo displayResolveInfo = r10;
                                    DisplayResolveInfo displayResolveInfo2 = new DisplayResolveInfo(ii, ri, ri.loadLabel(ResolverActivity.this.getPackageManager()), null, ii);
                                    addResolveInfo(displayResolveInfo);
                                }
                            }
                            i2++;
                            z = true;
                        }
                    }
                    ResolvedComponentInfo rci0 = list.get(0);
                    ResolveInfo r0 = rci0.getResolveInfoAt(0);
                    CharSequence r0Label = r0.loadLabel(ResolverActivity.this.mPm);
                    this.mHasExtendedInfo = false;
                    ResolvedComponentInfo rci02 = rci0;
                    ResolveInfo r02 = r0;
                    int start = 0;
                    CharSequence r0Label2 = r0Label;
                    int i3 = 1;
                    while (true) {
                        int i4 = i3;
                        if (i4 >= N) {
                            break;
                        }
                        if (r0Label2 == null) {
                            r0Label2 = r02.activityInfo.packageName;
                        }
                        ResolvedComponentInfo rci = list.get(i4);
                        ResolveInfo ri2 = rci.getResolveInfoAt(0);
                        CharSequence riLabel = ri2.loadLabel(ResolverActivity.this.mPm);
                        if (riLabel == null) {
                            riLabel = ri2.activityInfo.packageName;
                        }
                        CharSequence riLabel2 = riLabel;
                        if (!riLabel2.equals(r0Label2)) {
                            processGroup(list, start, i4 - 1, rci02, r0Label2);
                            rci02 = rci;
                            start = i4;
                            r02 = ri2;
                            r0Label2 = riLabel2;
                        }
                        i3 = i4 + 1;
                    }
                    processGroup(list, start, N - 1, rci02, r0Label2);
                }
            }
            postListReadyRunnable();
        }

        private void postListReadyRunnable() {
            if (ResolverActivity.this.mPostListReadyRunnable == null) {
                Runnable unused = ResolverActivity.this.mPostListReadyRunnable = new Runnable() {
                    public void run() {
                        ResolverActivity.this.setTitleAndIcon();
                        ResolverActivity.this.resetAlwaysOrOnceButtonBar();
                        ResolveListAdapter.this.onListRebuilt();
                        Runnable unused = ResolverActivity.this.mPostListReadyRunnable = null;
                    }
                };
                ResolverActivity.this.getMainThreadHandler().post(ResolverActivity.this.mPostListReadyRunnable);
            }
        }

        public void onListRebuilt() {
            if (getUnfilteredCount() == 1 && getOtherProfile() == null) {
                TargetInfo target = targetInfoForPosition(0, false);
                if (ResolverActivity.this.shouldAutoLaunchSingleChoice(target)) {
                    ResolverActivity.this.safelyStartActivity(target);
                    ResolverActivity.this.finish();
                }
            }
        }

        public boolean shouldGetResolvedFilter() {
            return this.mFilterLastUsed;
        }

        private void processGroup(List<ResolvedComponentInfo> rList, int start, int end, ResolvedComponentInfo ro, CharSequence roLabel) {
            CharSequence extraInfo;
            if ((end - start) + 1 == 1) {
                addResolveInfoWithAlternates(ro, null, roLabel);
                return;
            }
            this.mHasExtendedInfo = true;
            boolean usePkg = false;
            CharSequence startApp = ro.getResolveInfoAt(0).activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm);
            if (startApp == null) {
                usePkg = true;
            }
            if (!usePkg) {
                HashSet<CharSequence> duplicates = new HashSet<>();
                duplicates.add(startApp);
                int j = start + 1;
                while (true) {
                    if (j > end) {
                        break;
                    }
                    CharSequence jApp = rList.get(j).getResolveInfoAt(0).activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm);
                    if (jApp == null || duplicates.contains(jApp)) {
                        usePkg = true;
                    } else {
                        duplicates.add(jApp);
                        j++;
                    }
                }
                duplicates.clear();
            }
            for (int k = start; k <= end; k++) {
                ResolvedComponentInfo rci = rList.get(k);
                ResolveInfo add = rci.getResolveInfoAt(0);
                if (usePkg) {
                    extraInfo = add.activityInfo.packageName;
                } else {
                    extraInfo = add.activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm);
                }
                addResolveInfoWithAlternates(rci, extraInfo, roLabel);
            }
        }

        private void addResolveInfoWithAlternates(ResolvedComponentInfo rci, CharSequence extraInfo, CharSequence roLabel) {
            int count = rci.getCount();
            Intent intent = rci.getIntentAt(0);
            ResolveInfo add = rci.getResolveInfoAt(0);
            Intent replaceIntent = ResolverActivity.this.getReplacementIntent(add.activityInfo, intent);
            DisplayResolveInfo dri = new DisplayResolveInfo(intent, add, roLabel, extraInfo, replaceIntent);
            dri.setPinned(rci.isPinned());
            addResolveInfo(dri);
            if (replaceIntent == intent) {
                int i = count;
                for (int i2 = 1; i2 < i; i2++) {
                    dri.addAlternateSourceIntent(rci.getIntentAt(i2));
                }
            }
            updateLastChosenPosition(add);
        }

        private void updateLastChosenPosition(ResolveInfo info) {
            if (this.mOtherProfile != null) {
                this.mLastChosenPosition = -1;
                return;
            }
            if (this.mLastChosen != null && this.mLastChosen.activityInfo.packageName.equals(info.activityInfo.packageName) && this.mLastChosen.activityInfo.name.equals(info.activityInfo.name)) {
                this.mLastChosenPosition = this.mDisplayList.size() - 1;
            }
        }

        private void addResolveInfo(DisplayResolveInfo dri) {
            if (!(dri == null || dri.mResolveInfo == null || dri.mResolveInfo.targetUserId != -2)) {
                for (DisplayResolveInfo existingInfo : this.mDisplayList) {
                    if (ResolverActivity.resolveInfoMatch(dri.mResolveInfo, existingInfo.mResolveInfo)) {
                        return;
                    }
                }
                this.mDisplayList.add(dri);
            }
        }

        public ResolveInfo resolveInfoForPosition(int position, boolean filtered) {
            TargetInfo target = targetInfoForPosition(position, filtered);
            if (target != null) {
                return target.getResolveInfo();
            }
            return null;
        }

        public TargetInfo targetInfoForPosition(int position, boolean filtered) {
            if (filtered) {
                return getItem(position);
            }
            if (this.mDisplayList.size() > position) {
                return this.mDisplayList.get(position);
            }
            return null;
        }

        public int getCount() {
            int totalSize;
            if (this.mDisplayList == null || this.mDisplayList.isEmpty()) {
                totalSize = this.mPlaceholderCount;
            } else {
                totalSize = this.mDisplayList.size();
            }
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return totalSize;
            }
            return totalSize - 1;
        }

        public int getUnfilteredCount() {
            return this.mDisplayList.size();
        }

        public int getDisplayInfoCount() {
            return this.mDisplayList.size();
        }

        public DisplayResolveInfo getDisplayInfoAt(int index) {
            return this.mDisplayList.get(index);
        }

        public TargetInfo getItem(int position) {
            if (this.mFilterLastUsed && this.mLastChosenPosition >= 0 && position >= this.mLastChosenPosition) {
                position++;
            }
            if (this.mDisplayList.size() > position) {
                return this.mDisplayList.get(position);
            }
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public boolean hasExtendedInfo() {
            return this.mHasExtendedInfo;
        }

        public boolean hasResolvedTarget(ResolveInfo info) {
            int N = this.mDisplayList.size();
            for (int i = 0; i < N; i++) {
                if (ResolverActivity.resolveInfoMatch(info, this.mDisplayList.get(i).getResolveInfo())) {
                    return true;
                }
            }
            return false;
        }

        public int getDisplayResolveInfoCount() {
            return this.mDisplayList.size();
        }

        public DisplayResolveInfo getDisplayResolveInfo(int index) {
            return this.mDisplayList.get(index);
        }

        public final View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = createView(parent);
            }
            onBindView(view, getItem(position));
            return view;
        }

        public final View createView(ViewGroup parent) {
            View view = onCreateView(parent);
            view.setTag(new ViewHolder(view));
            return view;
        }

        public View onCreateView(ViewGroup parent) {
            return this.mInflater.inflate(17367251, parent, false);
        }

        public boolean showsExtendedInfo(TargetInfo info) {
            return !TextUtils.isEmpty(info.getExtendedInfo());
        }

        public boolean isComponentPinned(ComponentName name) {
            return false;
        }

        public final void bindView(int position, View view) {
            onBindView(view, getItem(position));
        }

        private void onBindView(View view, TargetInfo info) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (info == null) {
                holder.icon.setImageDrawable(ResolverActivity.this.getDrawable(17303344));
                return;
            }
            if (!TextUtils.equals(holder.text.getText(), info.getDisplayLabel())) {
                holder.text.setText(info.getDisplayLabel());
            }
            if (showsExtendedInfo(info)) {
                holder.text2.setVisibility(0);
                holder.text2.setText(info.getExtendedInfo());
            } else {
                holder.text2.setVisibility(8);
            }
            if ((info instanceof DisplayResolveInfo) && !((DisplayResolveInfo) info).hasDisplayIcon()) {
                new LoadAdapterIconTask((DisplayResolveInfo) info).execute(new Void[0]);
            }
            holder.icon.setImageDrawable(info.getDisplayIcon());
            if (holder.badge != null) {
                Drawable badge = info.getBadgeIcon();
                if (badge != null) {
                    holder.badge.setImageDrawable(badge);
                    holder.badge.setContentDescription(info.getBadgeContentDescription());
                    holder.badge.setVisibility(0);
                } else {
                    holder.badge.setVisibility(8);
                }
            }
        }
    }

    @VisibleForTesting
    public static final class ResolvedComponentInfo {
        private final List<Intent> mIntents = new ArrayList();
        private boolean mPinned;
        private final List<ResolveInfo> mResolveInfos = new ArrayList();
        public final ComponentName name;

        public ResolvedComponentInfo(ComponentName name2, Intent intent, ResolveInfo info) {
            this.name = name2;
            add(intent, info);
        }

        public void add(Intent intent, ResolveInfo info) {
            this.mIntents.add(intent);
            this.mResolveInfos.add(info);
        }

        public int getCount() {
            return this.mIntents.size();
        }

        public Intent getIntentAt(int index) {
            if (index >= 0) {
                return this.mIntents.get(index);
            }
            return null;
        }

        public ResolveInfo getResolveInfoAt(int index) {
            if (index >= 0) {
                return this.mResolveInfos.get(index);
            }
            return null;
        }

        public int findIntent(Intent intent) {
            int N = this.mIntents.size();
            for (int i = 0; i < N; i++) {
                if (intent.equals(this.mIntents.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        public int findResolveInfo(ResolveInfo info) {
            int N = this.mResolveInfos.size();
            for (int i = 0; i < N; i++) {
                if (info.equals(this.mResolveInfos.get(i))) {
                    return i;
                }
            }
            return -1;
        }

        public boolean isPinned() {
            return this.mPinned;
        }

        public void setPinned(boolean pinned) {
            this.mPinned = pinned;
        }
    }

    public interface TargetInfo {
        TargetInfo cloneFilledIn(Intent intent, int i);

        List<Intent> getAllSourceIntents();

        CharSequence getBadgeContentDescription();

        Drawable getBadgeIcon();

        Drawable getDisplayIcon();

        CharSequence getDisplayLabel();

        CharSequence getExtendedInfo();

        ResolveInfo getResolveInfo();

        ComponentName getResolvedComponentName();

        Intent getResolvedIntent();

        boolean isPinned();

        boolean start(Activity activity, Bundle bundle);

        boolean startAsCaller(Activity activity, Bundle bundle, int i);

        boolean startAsUser(Activity activity, Bundle bundle, UserHandle userHandle);
    }

    static class ViewHolder {
        public ImageView badge;
        public ImageView icon;
        public TextView text;
        public TextView text2;

        public ViewHolder(View view) {
            this.text = (TextView) view.findViewById(16908308);
            this.text2 = (TextView) view.findViewById(16908309);
            this.icon = (ImageView) view.findViewById(16908294);
            this.badge = (ImageView) view.findViewById(16909401);
        }
    }

    public static int getLabelRes(String action) {
        return ActionTitle.forAction(action).labelRes;
    }

    private Intent makeMyIntent() {
        Intent intent = new Intent(getIntent());
        intent.setComponent(null);
        intent.setFlags(intent.getFlags() & -8388609);
        return intent;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = makeMyIntent();
        Set<String> categories = intent.getCategories();
        if ("android.intent.action.MAIN".equals(intent.getAction()) && categories != null && categories.size() == 1 && categories.contains("android.intent.category.HOME")) {
            this.mResolvingHome = true;
        }
        setSafeForwardingMode(true);
        onCreate(savedInstanceState, intent, null, 0, null, null, true);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, Intent[] initialIntents, List<ResolveInfo> rList, boolean supportsAlwaysUseOption) {
        onCreate(savedInstanceState, intent, title, 0, initialIntents, rList, supportsAlwaysUseOption);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, int defaultTitleRes, Intent[] initialIntents, List<ResolveInfo> rList, boolean supportsAlwaysUseOption) {
        int i;
        setTheme(16974790);
        super.onCreate(savedInstanceState);
        setProfileSwitchMessageId(intent.getContentUserHint());
        try {
            this.mLaunchedFromUid = ActivityManager.getService().getLaunchedFromUid(getActivityToken());
        } catch (RemoteException e) {
            this.mLaunchedFromUid = -1;
        }
        if (this.mLaunchedFromUid < 0 || UserHandle.isIsolated(this.mLaunchedFromUid)) {
            finish();
            return;
        }
        this.mPm = getPackageManager();
        if (getUserId() != 0) {
            UserInfo ui = ((UserManager) getSystemService("user")).getUserInfo(getUserId());
            this.mIsClonedProfile = ui.isClonedProfile();
            if (this.mIsClonedProfile) {
                try {
                    this.mPmForParent = createPackageContextAsUser(getPackageName(), 0, new UserHandle(ui.profileGroupId)).getPackageManager();
                } catch (PackageManager.NameNotFoundException e2) {
                    this.mPmForParent = getPackageManager();
                }
            }
        }
        this.mPackageMonitor.register(this, getMainLooper(), false);
        this.mRegistered = true;
        this.mReferrerPackage = getReferrerPackageName();
        this.mSupportsAlwaysUseOption = supportsAlwaysUseOption;
        this.mIconDpi = ((ActivityManager) getSystemService("activity")).getLauncherLargeIconDensity();
        this.mIntents.add(0, new Intent(intent));
        this.mTitle = title;
        this.mDefaultTitleResId = defaultTitleRes;
        if (!configureContentView(this.mIntents, initialIntents, rList)) {
            ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(16908832);
            if (rdl != null) {
                rdl.setOnDismissedListener(new ResolverDrawerLayout.OnDismissedListener() {
                    public void onDismissed() {
                        ResolverActivity.this.finish();
                    }
                });
                if (isVoiceInteraction()) {
                    rdl.setCollapsed(false);
                }
                this.mResolverDrawerLayout = rdl;
            }
            this.mProfileView = findViewById(16909226);
            if (this.mProfileView != null) {
                this.mProfileView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        DisplayResolveInfo dri = ResolverActivity.this.mAdapter.getOtherProfile();
                        if (dri != null) {
                            int unused = ResolverActivity.this.mProfileSwitchMessageId = -1;
                            ResolverActivity.this.onTargetSelected(dri, false);
                            ResolverActivity.this.finish();
                        }
                    }
                });
                bindProfileView();
            }
            if (isVoiceInteraction()) {
                onSetupVoiceInteraction();
            }
            Set<String> categories = intent.getCategories();
            if (this.mAdapter.hasFilteredItem()) {
                i = MetricsProto.MetricsEvent.ACTION_SHOW_APP_DISAMBIG_APP_FEATURED;
            } else {
                i = MetricsProto.MetricsEvent.ACTION_SHOW_APP_DISAMBIG_NONE_FEATURED;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(intent.getAction());
            sb.append(":");
            sb.append(intent.getType());
            sb.append(":");
            sb.append(categories != null ? Arrays.toString(categories.toArray()) : "");
            MetricsLogger.action((Context) this, i, sb.toString());
            this.mIconFactory = IconDrawableFactory.newInstance(this, true);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mAdapter.handlePackagesChanged();
    }

    public void onSetupVoiceInteraction() {
        sendVoiceChoicesIfNeeded();
    }

    public void sendVoiceChoicesIfNeeded() {
        if (isVoiceInteraction()) {
            VoiceInteractor.PickOptionRequest.Option[] options = new VoiceInteractor.PickOptionRequest.Option[this.mAdapter.getCount()];
            int N = options.length;
            for (int i = 0; i < N; i++) {
                options[i] = optionForChooserTarget(this.mAdapter.getItem(i), i);
            }
            this.mPickOptionRequest = new PickTargetOptionRequest(new VoiceInteractor.Prompt(getTitle()), options, null);
            getVoiceInteractor().submitRequest(this.mPickOptionRequest);
        }
    }

    /* access modifiers changed from: package-private */
    public VoiceInteractor.PickOptionRequest.Option optionForChooserTarget(TargetInfo target, int index) {
        return new VoiceInteractor.PickOptionRequest.Option(target.getDisplayLabel(), index);
    }

    /* access modifiers changed from: protected */
    public final void setAdditionalTargets(Intent[] intents) {
        if (intents != null) {
            for (Intent intent : intents) {
                this.mIntents.add(intent);
            }
        }
    }

    public Intent getTargetIntent() {
        if (this.mIntents.isEmpty()) {
            return null;
        }
        return this.mIntents.get(0);
    }

    /* access modifiers changed from: protected */
    public String getReferrerPackageName() {
        Uri referrer = getReferrer();
        if (referrer == null || !"android-app".equals(referrer.getScheme())) {
            return null;
        }
        return referrer.getHost();
    }

    public int getLayoutResource() {
        return 17367254;
    }

    /* access modifiers changed from: package-private */
    public void bindProfileView() {
        DisplayResolveInfo dri = this.mAdapter.getOtherProfile();
        if (dri != null) {
            this.mProfileView.setVisibility(0);
            View text = this.mProfileView.findViewById(16909226);
            if (!(text instanceof TextView)) {
                text = this.mProfileView.findViewById(16908308);
            }
            ((TextView) text).setText(dri.getDisplayLabel());
            return;
        }
        this.mProfileView.setVisibility(8);
    }

    private void setProfileSwitchMessageId(int contentUserHint) {
        boolean originIsManaged;
        if (contentUserHint != -2 && contentUserHint != UserHandle.myUserId()) {
            UserManager userManager = (UserManager) getSystemService("user");
            UserInfo originUserInfo = userManager.getUserInfo(contentUserHint);
            if (originUserInfo != null) {
                originIsManaged = originUserInfo.isManagedProfile();
            } else {
                originIsManaged = false;
            }
            boolean targetIsManaged = userManager.isManagedProfile();
            if (originIsManaged && !targetIsManaged) {
                this.mProfileSwitchMessageId = 17040105;
            } else if (!originIsManaged && targetIsManaged) {
                this.mProfileSwitchMessageId = 17040106;
            }
        }
    }

    public void setSafeForwardingMode(boolean safeForwarding) {
        this.mSafeForwardingMode = safeForwarding;
    }

    /* access modifiers changed from: protected */
    public CharSequence getTitleForAction(String action, int defaultTitleRes) {
        String str;
        ActionTitle title = this.mResolvingHome ? ActionTitle.HOME : ActionTitle.forAction(action);
        boolean named = this.mAdapter.getFilteredPosition() >= 0;
        if (title == ActionTitle.DEFAULT && defaultTitleRes != 0) {
            return getString(defaultTitleRes);
        }
        if (named) {
            str = getString(title.namedTitleRes, new Object[]{this.mAdapter.getFilteredItem().getDisplayLabel()});
        } else {
            str = getString(title.titleRes);
        }
        return str;
    }

    /* access modifiers changed from: package-private */
    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    /* access modifiers changed from: package-private */
    public Drawable getIcon(Resources res, int resId) {
        try {
            return res.getDrawableForDensity(resId, this.mIconDpi);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public Drawable loadIconForResolveInfo(ResolveInfo ri) {
        try {
            if (!(ri.resolvePackageName == null || ri.icon == 0)) {
                Drawable dr = getIcon((this.mIsClonedProfile ? this.mPmForParent : this.mPm).getResourcesForApplication(ri.resolvePackageName), ri.icon);
                if (dr != null) {
                    return this.mIconFactory.getShadowedIcon(dr);
                }
            }
            int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                Drawable dr2 = getIcon((this.mIsClonedProfile ? this.mPmForParent : this.mPm).getResourcesForApplication(ri.activityInfo.packageName), iconRes);
                if (dr2 != null) {
                    return this.mIconFactory.getShadowedIcon(dr2);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Couldn't find resources for package", e);
        }
        return this.mIconFactory.getBadgedIcon(ri.activityInfo.applicationInfo);
    }

    /* access modifiers changed from: protected */
    public void onRestart() {
        super.onRestart();
        if (!this.mRegistered) {
            this.mPackageMonitor.register(this, getMainLooper(), false);
            this.mRegistered = true;
        }
        this.mAdapter.handlePackagesChanged();
        if (this.mProfileView != null) {
            bindProfileView();
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        if (this.mRegistered) {
            this.mPackageMonitor.unregister();
            this.mRegistered = false;
        }
        if ((getIntent().getFlags() & 268435456) != 0 && !isVoiceInteraction() && !this.mResolvingHome && !this.mRetainInOnStop && !isChangingConfigurations()) {
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations() && this.mPickOptionRequest != null) {
            this.mPickOptionRequest.cancel();
        }
        if (this.mPostListReadyRunnable != null) {
            getMainThreadHandler().removeCallbacks(this.mPostListReadyRunnable);
            this.mPostListReadyRunnable = null;
        }
        if (this.mAdapter != null && this.mAdapter.mResolverListController != null) {
            this.mAdapter.mResolverListController.destroy();
        }
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        resetAlwaysOrOnceButtonBar();
    }

    private boolean hasManagedProfile() {
        UserManager userManager = (UserManager) getSystemService("user");
        if (userManager == null) {
            return false;
        }
        try {
            for (UserInfo userInfo : userManager.getProfiles(getUserId())) {
                if (userInfo != null && userInfo.isManagedProfile()) {
                    return true;
                }
            }
            return false;
        } catch (SecurityException e) {
            return false;
        }
    }

    private boolean supportsManagedProfiles(ResolveInfo resolveInfo) {
        int i;
        boolean z = false;
        try {
            PackageManager packageManager = getPackageManager();
            String str = resolveInfo.activityInfo.packageName;
            if (this.mIsClonedProfile) {
                i = 4202496;
            } else {
                i = 0;
            }
            if (packageManager.getApplicationInfo(str, i).targetSdkVersion >= 21) {
                z = true;
            }
            return z;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void setAlwaysButtonEnabled(boolean hasValidSelection, int checkedPos, boolean filtered) {
        boolean enabled = false;
        if (hasValidSelection) {
            ResolveInfo ri = this.mAdapter.resolveInfoForPosition(checkedPos, filtered);
            if (ri == null) {
                Log.e(TAG, "Invalid position supplied to setAlwaysButtonEnabled");
                return;
            } else if (ri.targetUserId != -2) {
                Log.e(TAG, "Attempted to set selection to resolve info for another user");
                return;
            } else {
                enabled = true;
            }
        }
        if (this.mAlwaysButton != null) {
            this.mAlwaysButton.setEnabled(enabled);
        }
    }

    public void onButtonClick(View v) {
        int i;
        int id = v.getId();
        if (this.mAdapter.hasFilteredItem()) {
            i = this.mAdapter.getFilteredPosition();
        } else {
            i = this.mAdapterView.getCheckedItemPosition();
        }
        startSelected(i, id == 16945154, true ^ this.mAdapter.hasFilteredItem());
    }

    public void startSelected(int which, boolean always, boolean hasIndexBeenFiltered) {
        int i;
        if (!isFinishing()) {
            ResolveInfo ri = this.mAdapter.resolveInfoForPosition(which, hasIndexBeenFiltered);
            if (!this.mResolvingHome || !hasManagedProfile() || supportsManagedProfiles(ri)) {
                TargetInfo target = this.mAdapter.targetInfoForPosition(which, hasIndexBeenFiltered);
                if (target != null && onTargetSelected(target, always)) {
                    if (always && this.mSupportsAlwaysUseOption) {
                        MetricsLogger.action((Context) this, (int) MetricsProto.MetricsEvent.ACTION_APP_DISAMBIG_ALWAYS);
                    } else if (this.mSupportsAlwaysUseOption) {
                        MetricsLogger.action((Context) this, (int) MetricsProto.MetricsEvent.ACTION_APP_DISAMBIG_JUST_ONCE);
                    } else {
                        MetricsLogger.action((Context) this, (int) MetricsProto.MetricsEvent.ACTION_APP_DISAMBIG_TAP);
                    }
                    if (this.mAdapter.hasFilteredItem()) {
                        i = MetricsProto.MetricsEvent.ACTION_HIDE_APP_DISAMBIG_APP_FEATURED;
                    } else {
                        i = MetricsProto.MetricsEvent.ACTION_HIDE_APP_DISAMBIG_NONE_FEATURED;
                    }
                    MetricsLogger.action((Context) this, i);
                    finish();
                }
                return;
            }
            Toast.makeText(this, String.format(getResources().getString(17039546), new Object[]{ri.activityInfo.loadLabel(getPackageManager()).toString()}), 1).show();
        }
    }

    public Intent getReplacementIntent(ActivityInfo aInfo, Intent defIntent) {
        return defIntent;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x020a  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0216  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x021b  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0228  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x0265  */
    public boolean onTargetSelected(TargetInfo target, boolean alwaysCheck) {
        Intent filterIntent;
        ComponentName[] set;
        boolean isHttpOrHttps;
        boolean isViewAction;
        boolean z;
        String str;
        ResolveInfo ri = target.getResolveInfo();
        Intent intent = target != null ? target.getResolvedIntent() : null;
        if (intent != null) {
            if (!this.mSupportsAlwaysUseOption && !this.mAdapter.hasFilteredItem()) {
                ResolveInfo resolveInfo = ri;
                if (target != null) {
                }
                return true;
            } else if (this.mAdapter.mUnfilteredResolveList != null) {
                IntentFilter filter = new IntentFilter();
                if (intent.getSelector() != null) {
                    filterIntent = intent.getSelector();
                } else {
                    filterIntent = intent;
                }
                Intent filterIntent2 = filterIntent;
                String action = filterIntent2.getAction();
                if (action != null) {
                    filter.addAction(action);
                }
                Set<String> categories = filterIntent2.getCategories();
                if (categories != null) {
                    for (String cat : categories) {
                        filter.addCategory(cat);
                    }
                }
                filter.addCategory("android.intent.category.DEFAULT");
                int cat2 = 268369920 & ri.match;
                Uri data = filterIntent2.getData();
                if (cat2 == 6291456) {
                    String mimeType = filterIntent2.resolveType(this);
                    if (mimeType != null) {
                        try {
                            filter.addDataType(mimeType);
                        } catch (IntentFilter.MalformedMimeTypeException e) {
                            IntentFilter.MalformedMimeTypeException malformedMimeTypeException = e;
                            Log.w(TAG, e);
                            filter = null;
                        }
                    }
                }
                if (data != null && data.getScheme() != null && (cat2 != 6291456 || (!"file".equals(data.getScheme()) && !"content".equals(data.getScheme())))) {
                    filter.addDataScheme(data.getScheme());
                    Iterator<PatternMatcher> pIt = ri.filter.schemeSpecificPartsIterator();
                    if (pIt != null) {
                        String ssp = data.getSchemeSpecificPart();
                        while (true) {
                            if (ssp == null || !pIt.hasNext()) {
                                break;
                            }
                            PatternMatcher p = pIt.next();
                            if (p.match(ssp)) {
                                filter.addDataSchemeSpecificPart(p.getPath(), p.getType());
                                break;
                            }
                        }
                    }
                    Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                    if (aIt != null) {
                        while (true) {
                            if (!aIt.hasNext()) {
                                break;
                            }
                            IntentFilter.AuthorityEntry a = aIt.next();
                            if (a.match(data) >= 0) {
                                int port = a.getPort();
                                String host = a.getHost();
                                if (port >= 0) {
                                    str = Integer.toString(port);
                                } else {
                                    str = null;
                                }
                                filter.addDataAuthority(host, str);
                            }
                        }
                    }
                    Iterator<PatternMatcher> pIt2 = ri.filter.pathsIterator();
                    if (pIt2 != null) {
                        String path = data.getPath();
                        while (true) {
                            if (path == null || !pIt2.hasNext()) {
                                break;
                            }
                            PatternMatcher p2 = pIt2.next();
                            if (p2.match(path)) {
                                filter.addDataPath(p2.getPath(), p2.getType());
                                break;
                            }
                        }
                    }
                }
                if (filter != null) {
                    int N = this.mAdapter.mUnfilteredResolveList.size();
                    boolean needToAddBackProfileForwardingComponent = this.mAdapter.mOtherProfile != null;
                    if (!needToAddBackProfileForwardingComponent) {
                        set = new ComponentName[N];
                    } else {
                        set = new ComponentName[(N + 1)];
                    }
                    ComponentName[] set2 = set;
                    int bestMatch = 0;
                    int i = 0;
                    while (i < N) {
                        Intent filterIntent3 = filterIntent2;
                        ResolveInfo r = this.mAdapter.mUnfilteredResolveList.get(i).getResolveInfoAt(0);
                        int cat3 = cat2;
                        set2[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                        if (r.match > bestMatch) {
                            bestMatch = r.match;
                        }
                        i++;
                        filterIntent2 = filterIntent3;
                        cat2 = cat3;
                    }
                    int i2 = cat2;
                    if (needToAddBackProfileForwardingComponent) {
                        set2[N] = this.mAdapter.mOtherProfile.getResolvedComponentName();
                        int otherProfileMatch = this.mAdapter.mOtherProfile.getResolveInfo().match;
                        if (otherProfileMatch > bestMatch) {
                            bestMatch = otherProfileMatch;
                        }
                    }
                    if (alwaysCheck) {
                        int userId = getUserId();
                        PackageManager pm = getPackageManager();
                        pm.addPreferredActivity(filter, bestMatch, set2, intent.getComponent());
                        if (ri.handleAllWebDataURI) {
                            if (TextUtils.isEmpty(pm.getDefaultBrowserPackageNameAsUser(userId))) {
                                pm.setDefaultBrowserPackageNameAsUser(ri.activityInfo.packageName, userId);
                            }
                            ResolveInfo resolveInfo2 = ri;
                            int i3 = N;
                        } else {
                            String packageName = intent.getComponent().getPackageName();
                            String dataScheme = data != null ? data.getScheme() : null;
                            if (dataScheme != null) {
                                ResolveInfo resolveInfo3 = ri;
                                if (dataScheme.equals("http") || dataScheme.equals("https")) {
                                    isHttpOrHttps = true;
                                    if (action == null) {
                                        String str2 = dataScheme;
                                        if (action.equals("android.intent.action.VIEW")) {
                                            isViewAction = true;
                                            if (categories != null) {
                                                int i4 = N;
                                                if (categories.contains("android.intent.category.BROWSABLE") != 0) {
                                                    z = true;
                                                    boolean hasCategoryBrowsable = z;
                                                    if (isHttpOrHttps && isViewAction && hasCategoryBrowsable) {
                                                        boolean z2 = isViewAction;
                                                        pm.updateIntentVerificationStatusAsUser(packageName, 2, userId);
                                                    }
                                                }
                                            }
                                            z = false;
                                            boolean hasCategoryBrowsable2 = z;
                                            boolean z22 = isViewAction;
                                            pm.updateIntentVerificationStatusAsUser(packageName, 2, userId);
                                        }
                                    }
                                    isViewAction = false;
                                    if (categories != null) {
                                    }
                                    z = false;
                                    boolean hasCategoryBrowsable22 = z;
                                    boolean z222 = isViewAction;
                                    pm.updateIntentVerificationStatusAsUser(packageName, 2, userId);
                                }
                            }
                            isHttpOrHttps = false;
                            if (action == null) {
                            }
                            isViewAction = false;
                            if (categories != null) {
                            }
                            z = false;
                            boolean hasCategoryBrowsable222 = z;
                            boolean z2222 = isViewAction;
                            pm.updateIntentVerificationStatusAsUser(packageName, 2, userId);
                        }
                    } else {
                        int i5 = N;
                        try {
                            this.mAdapter.mResolverListController.setLastChosen(intent, filter, bestMatch);
                        } catch (RemoteException re) {
                            Log.d(TAG, "Error calling setLastChosenActivity\n" + re);
                        }
                    }
                    if (target != null) {
                        safelyStartActivity(target);
                    }
                    return true;
                }
            }
        }
        if (target != null) {
        }
        return true;
    }

    public void safelyStartActivity(TargetInfo cti) {
        StrictMode.disableDeathOnFileUriExposure();
        try {
            safelyStartActivityInternal(cti);
        } finally {
            StrictMode.enableDeathOnFileUriExposure();
        }
    }

    private void safelyStartActivityInternal(TargetInfo cti) {
        String launchedFromPackage;
        if (this.mProfileSwitchMessageId != -1) {
            Toast.makeText(this, getString(this.mProfileSwitchMessageId), 1).show();
        }
        if (!this.mSafeForwardingMode) {
            if (cti.start(this, null)) {
                onActivityStarted(cti);
            }
            return;
        }
        try {
            if (cti.startAsCaller(this, null, -10000)) {
                onActivityStarted(cti);
            }
        } catch (RuntimeException e) {
            try {
                launchedFromPackage = ActivityManager.getService().getLaunchedFromPackage(getActivityToken());
            } catch (RemoteException e2) {
                launchedFromPackage = "??";
            }
            Slog.wtf(TAG, "Unable to launch as uid " + this.mLaunchedFromUid + " package " + launchedFromPackage + ", while running in " + ActivityThread.currentProcessName(), e);
        }
    }

    public void onActivityStarted(TargetInfo cti) {
    }

    public boolean shouldGetActivityMetadata() {
        return false;
    }

    public boolean shouldAutoLaunchSingleChoice(TargetInfo target) {
        return true;
    }

    public void showTargetDetails(ResolveInfo ri) {
        startActivity(new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", ri.activityInfo.packageName, null)).addFlags(524288));
    }

    public ResolveListAdapter createAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
        ResolveListAdapter resolveListAdapter = new ResolveListAdapter(context, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed, createListController());
        return resolveListAdapter;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public ResolverListController createListController() {
        ResolverListController resolverListController = new ResolverListController(this, this.mPm, getTargetIntent(), getReferrerPackageName(), this.mLaunchedFromUid);
        return resolverListController;
    }

    public boolean configureContentView(List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList) {
        this.mAdapter = createAdapter(this, payloadIntents, initialIntents, rList, this.mLaunchedFromUid, this.mSupportsAlwaysUseOption && !isVoiceInteraction());
        boolean rebuildCompleted = this.mAdapter.rebuildList();
        if (useLayoutWithDefault()) {
            this.mLayoutId = 17367255;
        } else {
            this.mLayoutId = getLayoutResource();
        }
        setContentView(this.mLayoutId);
        int count = this.mAdapter.getUnfilteredCount();
        if (rebuildCompleted && count == 1 && this.mAdapter.getOtherProfile() == null) {
            TargetInfo target = this.mAdapter.targetInfoForPosition(0, false);
            if (shouldAutoLaunchSingleChoice(target)) {
                safelyStartActivity(target);
                this.mPackageMonitor.unregister();
                this.mRegistered = false;
                finish();
                return true;
            }
        }
        this.mAdapterView = (AbsListView) findViewById(16909254);
        if (count == 0 && this.mAdapter.mPlaceholderCount == 0) {
            ((TextView) findViewById(16908292)).setVisibility(0);
            this.mAdapterView.setVisibility(8);
        } else {
            this.mAdapterView.setVisibility(0);
            onPrepareAdapterView(this.mAdapterView, this.mAdapter);
        }
        return false;
    }

    public void onPrepareAdapterView(AbsListView adapterView, ResolveListAdapter adapter) {
        boolean useHeader = adapter.hasFilteredItem();
        ListView listView = adapterView instanceof ListView ? (ListView) adapterView : null;
        adapterView.setAdapter(this.mAdapter);
        ItemClickListener listener = new ItemClickListener();
        adapterView.setOnItemClickListener(listener);
        adapterView.setOnItemLongClickListener(listener);
        if (this.mSupportsAlwaysUseOption && listView != null) {
            listView.setChoiceMode(1);
        }
        if (useHeader && listView != null && listView.getHeaderViewsCount() == 0) {
            listView.addHeaderView(LayoutInflater.from(this).inflate(17367252, listView, false));
        }
    }

    public void setTitleAndIcon() {
        CharSequence title;
        if (this.mAdapter.getCount() == 0 && this.mAdapter.mPlaceholderCount == 0) {
            TextView titleView = (TextView) findViewById(16908310);
            if (titleView != null) {
                titleView.setVisibility(8);
            }
        }
        if (this.mTitle != null) {
            title = this.mTitle;
        } else {
            title = getTitleForAction(getTargetIntent().getAction(), this.mDefaultTitleResId);
        }
        if (!TextUtils.isEmpty(title)) {
            TextView titleView2 = (TextView) findViewById(16908310);
            if (titleView2 != null) {
                titleView2.setText(title);
            }
            setTitle(title);
            ImageView titleIcon = (ImageView) findViewById(16909444);
            if (titleIcon != null) {
                ApplicationInfo ai = null;
                try {
                    if (!TextUtils.isEmpty(this.mReferrerPackage)) {
                        ai = this.mPm.getApplicationInfo(this.mReferrerPackage, this.mIsClonedProfile ? 4202496 : 0);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Could not find referrer package " + this.mReferrerPackage);
                }
                if (ai != null) {
                    titleIcon.setImageDrawable(ai.loadIcon(this.mIsClonedProfile ? this.mPmForParent : this.mPm));
                }
            }
        }
        ImageView iconView = (ImageView) findViewById(16908294);
        DisplayResolveInfo iconInfo = this.mAdapter.getFilteredItem();
        if (iconView != null && iconInfo != null) {
            new LoadIconIntoViewTask(iconInfo, iconView).execute(new Void[0]);
        }
    }

    public void resetAlwaysOrOnceButtonBar() {
        if (this.mSupportsAlwaysUseOption) {
            ViewGroup buttonLayout = (ViewGroup) findViewById(16945153);
            if (buttonLayout != null) {
                buttonLayout.setVisibility(0);
                this.mAlwaysButton = (Button) buttonLayout.findViewById(16945154);
                this.mOnceButton = (Button) buttonLayout.findViewById(16945155);
            } else {
                Log.e(TAG, "Layout unexpectedly does not have a button bar");
            }
        }
        if (!useLayoutWithDefault() || this.mAdapter.getFilteredPosition() == -1) {
            if (!(this.mAdapterView == null || this.mAdapterView.getCheckedItemPosition() == -1)) {
                setAlwaysButtonEnabled(true, this.mAdapterView.getCheckedItemPosition(), true);
                this.mOnceButton.setEnabled(true);
            }
            return;
        }
        setAlwaysButtonEnabled(true, this.mAdapter.getFilteredPosition(), false);
        this.mOnceButton.setEnabled(true);
    }

    /* access modifiers changed from: private */
    public boolean useLayoutWithDefault() {
        return this.mSupportsAlwaysUseOption && this.mAdapter.hasFilteredItem();
    }

    /* access modifiers changed from: protected */
    public void setRetainInOnStop(boolean retainInOnStop) {
        this.mRetainInOnStop = retainInOnStop;
    }

    static boolean resolveInfoMatch(ResolveInfo lhs, ResolveInfo rhs) {
        if (lhs == null) {
            if (rhs != null) {
                return false;
            }
        } else if (lhs.activityInfo == null) {
            if (rhs.activityInfo != null) {
                return false;
            }
        } else if (!Objects.equals(lhs.activityInfo.name, rhs.activityInfo.name) || !Objects.equals(lhs.activityInfo.packageName, rhs.activityInfo.packageName)) {
            return false;
        }
        return true;
    }

    static final boolean isSpecificUriMatch(int match) {
        int match2 = match & 268369920;
        return match2 >= 3145728 && match2 <= 5242880;
    }
}
