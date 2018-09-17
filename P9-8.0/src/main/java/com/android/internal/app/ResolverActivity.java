package com.android.internal.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.VoiceInteractor.PickOptionRequest;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.app.VoiceInteractor.Prompt;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.AuthorityEntry;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.provider.Telephony.BaseMmsColumns;
import android.provider.Telephony.Sms.Intents;
import android.rms.iaware.DataContract.Apps.LaunchMode;
import android.service.voice.VoiceInteractionSession;
import android.text.TextUtils;
import android.util.Log;
import android.util.LogException;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.widget.ResolverDrawerLayout;
import com.android.internal.widget.ResolverDrawerLayout.OnDismissedListener;
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
    private AbsListView mAdapterView;
    private Button mAlwaysButton;
    private int mDefaultTitleResId;
    private int mIconDpi;
    private final ArrayList<Intent> mIntents = new ArrayList();
    protected boolean mIsClonedProfile;
    private int mLastSelected = -1;
    protected int mLaunchedFromUid;
    private int mLayoutId;
    private Button mOnceButton;
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
    private PickTargetOptionRequest mPickOptionRequest;
    protected PackageManager mPm;
    protected PackageManager mPmForParent;
    private Runnable mPostListReadyRunnable;
    private int mProfileSwitchMessageId = -1;
    private View mProfileView;
    private String mReferrerPackage;
    private boolean mRegistered;
    protected ResolverDrawerLayout mResolverDrawerLayout;
    private boolean mResolvingHome = false;
    private boolean mRetainInOnStop;
    private boolean mSafeForwardingMode;
    private boolean mSupportsAlwaysUseOption;
    private CharSequence mTitle;

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
        private DisplayResolveInfo mOtherProfile;
        private int mPlaceholderCount;
        private ResolverListController mResolverListController;
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
            return (DisplayResolveInfo) this.mDisplayList.get(this.mLastChosenPosition);
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

        protected boolean rebuildList() {
            List<ResolvedComponentInfo> currentResolveList;
            List<ResolvedComponentInfo> originalList;
            this.mOtherProfile = null;
            this.mLastChosen = null;
            this.mLastChosenPosition = -1;
            this.mDisplayList.clear();
            if (this.mBaseResolveList != null) {
                currentResolveList = new ArrayList();
                this.mUnfilteredResolveList = currentResolveList;
                this.mResolverListController.addResolveListDedupe(currentResolveList, ResolverActivity.this.getTargetIntent(), this.mBaseResolveList);
            } else {
                currentResolveList = this.mResolverListController.getResolversForIntent(shouldGetResolvedFilter(), ResolverActivity.this.shouldGetActivityMetadata(), this.mIntents);
                this.mUnfilteredResolveList = currentResolveList;
                if (currentResolveList == null) {
                    processSortedList(currentResolveList);
                    return true;
                }
                originalList = this.mResolverListController.filterIneligibleActivities(currentResolveList, true);
                if (originalList != null) {
                    this.mUnfilteredResolveList = originalList;
                }
            }
            for (ResolvedComponentInfo info : currentResolveList) {
                if (info.getResolveInfoAt(0).targetUserId != -2) {
                    this.mOtherProfile = new DisplayResolveInfo(info.getIntentAt(0), info.getResolveInfoAt(0), info.getResolveInfoAt(0).loadLabel(ResolverActivity.this.mPm), info.getResolveInfoAt(0).loadLabel(ResolverActivity.this.mPm), ResolverActivity.this.getReplacementIntent(info.getResolveInfoAt(0).activityInfo, info.getIntentAt(0)));
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
            if (currentResolveList == null || currentResolveList.size() <= 0) {
                processSortedList(currentResolveList);
                return true;
            }
            originalList = this.mResolverListController.filterLowPriority(currentResolveList, this.mUnfilteredResolveList == currentResolveList);
            if (originalList != null) {
                this.mUnfilteredResolveList = originalList;
            }
            if (currentResolveList.size() > 1) {
                int placeholderCount = currentResolveList.size();
                if (ResolverActivity.this.useLayoutWithDefault()) {
                    placeholderCount--;
                }
                setPlaceholderCount(placeholderCount);
                new AsyncTask<List<ResolvedComponentInfo>, Void, List<ResolvedComponentInfo>>() {
                    protected List<ResolvedComponentInfo> doInBackground(List<ResolvedComponentInfo>... params) {
                        ResolveListAdapter.this.mResolverListController.sort(params[0]);
                        return params[0];
                    }

                    protected void onPostExecute(List<ResolvedComponentInfo> sortedComponents) {
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

        private void processSortedList(List<ResolvedComponentInfo> sortedComponents) {
            if (sortedComponents != null) {
                int N = sortedComponents.size();
                if (N != 0) {
                    int i;
                    ResolveInfo ri;
                    if (this.mInitialIntents != null) {
                        for (Intent ii : this.mInitialIntents) {
                            if (ii != null) {
                                ActivityInfo ai = ii.resolveActivityInfo(ResolverActivity.this.getPackageManager(), ResolverActivity.this.mIsClonedProfile ? 4202496 : 0);
                                if (ai == null) {
                                    Log.w(ResolverActivity.TAG, "No activity found for " + ii);
                                } else {
                                    ri = new ResolveInfo();
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
                                        ri.noResourceId = true;
                                        ri.icon = 0;
                                    }
                                    addResolveInfo(new DisplayResolveInfo(ii, ri, ri.loadLabel(ResolverActivity.this.getPackageManager()), null, ii));
                                }
                            }
                        }
                    }
                    ResolvedComponentInfo rci0 = (ResolvedComponentInfo) sortedComponents.get(0);
                    ResolveInfo r0 = rci0.getResolveInfoAt(0);
                    int start = 0;
                    CharSequence r0Label = r0.loadLabel(ResolverActivity.this.mPm);
                    this.mHasExtendedInfo = false;
                    for (i = 1; i < N; i++) {
                        if (r0Label == null) {
                            r0Label = r0.activityInfo.packageName;
                        }
                        ResolvedComponentInfo rci = (ResolvedComponentInfo) sortedComponents.get(i);
                        ri = rci.getResolveInfoAt(0);
                        CharSequence riLabel = ri.loadLabel(ResolverActivity.this.mPm);
                        if (riLabel == null) {
                            riLabel = ri.activityInfo.packageName;
                        }
                        if (!riLabel.equals(r0Label)) {
                            processGroup(sortedComponents, start, i - 1, rci0, r0Label);
                            rci0 = rci;
                            r0 = ri;
                            r0Label = riLabel;
                            start = i;
                        }
                    }
                    processGroup(sortedComponents, start, N - 1, rci0, r0Label);
                }
            }
            postListReadyRunnable();
        }

        private void postListReadyRunnable() {
            if (ResolverActivity.this.mPostListReadyRunnable == null) {
                ResolverActivity.this.mPostListReadyRunnable = new Runnable() {
                    public void run() {
                        ResolverActivity.this.setTitleAndIcon();
                        ResolverActivity.this.resetAlwaysOrOnceButtonBar();
                        ResolveListAdapter.this.onListRebuilt();
                        ResolverActivity.this.mPostListReadyRunnable = null;
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
                HashSet<CharSequence> duplicates = new HashSet();
                duplicates.add(startApp);
                int j = start + 1;
                while (j <= end) {
                    CharSequence jApp = ((ResolvedComponentInfo) rList.get(j)).getResolveInfoAt(0).activityInfo.applicationInfo.loadLabel(ResolverActivity.this.mPm);
                    if (jApp == null || duplicates.contains(jApp)) {
                        usePkg = true;
                        break;
                    } else {
                        duplicates.add(jApp);
                        j++;
                    }
                }
                duplicates.clear();
            }
            for (int k = start; k <= end; k++) {
                CharSequence extraInfo;
                ResolvedComponentInfo rci = (ResolvedComponentInfo) rList.get(k);
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
                int N = count;
                for (int i = 1; i < count; i++) {
                    dri.addAlternateSourceIntent(rci.getIntentAt(i));
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
            if (dri.mResolveInfo.targetUserId == -2) {
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
                return (TargetInfo) this.mDisplayList.get(position);
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
            return (DisplayResolveInfo) this.mDisplayList.get(index);
        }

        public TargetInfo getItem(int position) {
            if (this.mFilterLastUsed && this.mLastChosenPosition >= 0 && position >= this.mLastChosenPosition) {
                position++;
            }
            if (this.mDisplayList.size() > position) {
                return (TargetInfo) this.mDisplayList.get(position);
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
                if (ResolverActivity.resolveInfoMatch(info, ((DisplayResolveInfo) this.mDisplayList.get(i)).getResolveInfo())) {
                    return true;
                }
            }
            return false;
        }

        public int getDisplayResolveInfoCount() {
            return this.mDisplayList.size();
        }

        public DisplayResolveInfo getDisplayResolveInfo(int index) {
            return (DisplayResolveInfo) this.mDisplayList.get(index);
        }

        public final View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
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
            return this.mInflater.inflate((int) R.layout.resolve_list_item, parent, false);
        }

        public boolean showsExtendedInfo(TargetInfo info) {
            return TextUtils.isEmpty(info.getExtendedInfo()) ^ 1;
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
                holder.icon.setImageDrawable(ResolverActivity.this.getDrawable(R.drawable.resolver_icon_placeholder));
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
            if ((info instanceof DisplayResolveInfo) && (((DisplayResolveInfo) info).hasDisplayIcon() ^ 1) != 0) {
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

    private enum ActionTitle {
        VIEW("android.intent.action.VIEW", R.string.whichViewApplication, R.string.whichViewApplicationNamed, R.string.whichViewApplicationLabel),
        EDIT("android.intent.action.EDIT", R.string.whichEditApplication, R.string.whichEditApplicationNamed, R.string.whichEditApplicationLabel),
        SEND("android.intent.action.SEND", R.string.whichSendApplication, R.string.whichSendApplicationNamed, R.string.whichSendApplicationLabel),
        SENDTO("android.intent.action.SENDTO", R.string.whichSendToApplication, R.string.whichSendToApplicationNamed, R.string.whichSendToApplicationLabel),
        SEND_MULTIPLE("android.intent.action.SEND_MULTIPLE", R.string.whichSendApplication, R.string.whichSendApplicationNamed, R.string.whichSendApplicationLabel),
        CAPTURE_IMAGE(MediaStore.ACTION_IMAGE_CAPTURE, R.string.whichImageCaptureApplication, R.string.whichImageCaptureApplicationNamed, R.string.whichImageCaptureApplicationLabel),
        DEFAULT(null, R.string.whichApplication, R.string.whichApplicationNamed, R.string.whichApplicationLabel),
        HOME("android.intent.action.MAIN", R.string.whichHomeApplication, R.string.whichHomeApplicationNamed, R.string.whichHomeApplicationLabel);
        
        public final String action;
        public final int labelRes;
        public final int namedTitleRes;
        public final int titleRes;

        private ActionTitle(String action, int titleRes, int namedTitleRes, int labelRes) {
            this.action = action;
            this.titleRes = titleRes;
            this.namedTitleRes = namedTitleRes;
            this.labelRes = labelRes;
        }

        public static ActionTitle forAction(String action) {
            for (ActionTitle title : values()) {
                if (title != HOME && action != null && action.equals(title.action)) {
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
        private final ResolveInfo mResolveInfo;
        private final Intent mResolvedIntent;
        private final List<Intent> mSourceIntents = new ArrayList();

        public DisplayResolveInfo(Intent originalIntent, ResolveInfo pri, CharSequence pLabel, CharSequence pInfo, Intent pOrigIntent) {
            this.mSourceIntents.add(originalIntent);
            this.mResolveInfo = pri;
            this.mDisplayLabel = pLabel;
            this.mExtendedInfo = pInfo;
            if (pOrigIntent == null) {
                pOrigIntent = ResolverActivity.this.getReplacementIntent(pri.activityInfo, ResolverActivity.this.getTargetIntent());
            }
            Intent intent = new Intent(pOrigIntent);
            intent.addFlags(View.SCROLLBARS_OUTSIDE_INSET);
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
                Log.e(ResolverActivity.TAG, BaseMmsColumns.START, e);
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

    class ItemClickListener implements OnItemClickListener, OnItemLongClickListener {
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
                if (!ResolverActivity.this.mSupportsAlwaysUseOption || (ResolverActivity.this.mAdapter.hasFilteredItem() ^ 1) == 0 || (hasValidSelection && ResolverActivity.this.mLastSelected == checkedPos)) {
                    ResolverActivity.this.startSelected(position, false, true);
                } else {
                    ResolverActivity.this.setAlwaysButtonEnabled(hasValidSelection, checkedPos, true);
                    ResolverActivity.this.mOnceButton.setEnabled(hasValidSelection);
                    if (hasValidSelection) {
                        ResolverActivity.this.mAdapterView.smoothScrollToPosition(checkedPos);
                    }
                    ResolverActivity.this.mLastSelected = checkedPos;
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

    abstract class LoadIconTask extends AsyncTask<Void, Void, Drawable> {
        protected final DisplayResolveInfo mDisplayResolveInfo;
        private final ResolveInfo mResolveInfo;

        public LoadIconTask(DisplayResolveInfo dri) {
            this.mDisplayResolveInfo = dri;
            this.mResolveInfo = dri.getResolveInfo();
        }

        protected Drawable doInBackground(Void... params) {
            return ResolverActivity.this.loadIconForResolveInfo(this.mResolveInfo);
        }

        protected void onPostExecute(Drawable d) {
            this.mDisplayResolveInfo.setDisplayIcon(d);
        }
    }

    class LoadAdapterIconTask extends LoadIconTask {
        public LoadAdapterIconTask(DisplayResolveInfo dri) {
            super(dri);
        }

        protected void onPostExecute(Drawable d) {
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

        protected void onPostExecute(Drawable d) {
            super.onPostExecute(d);
            this.mTargetView.setImageDrawable(d);
        }
    }

    static class PickTargetOptionRequest extends PickOptionRequest {
        public PickTargetOptionRequest(Prompt prompt, Option[] options, Bundle extras) {
            super(prompt, options, extras);
        }

        public void onCancel() {
            super.onCancel();
            ResolverActivity ra = (ResolverActivity) getActivity();
            if (ra != null) {
                ra.mPickOptionRequest = null;
                ra.finish();
            }
        }

        public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
            super.onPickOptionResult(finished, selections, result);
            if (selections.length == 1) {
                ResolverActivity ra = (ResolverActivity) getActivity();
                if (ra != null && ra.onTargetSelected(ra.mAdapter.getItem(selections[0].getIndex()), false)) {
                    ra.mPickOptionRequest = null;
                    ra.finish();
                }
            }
        }
    }

    public static final class ResolvedComponentInfo {
        private final List<Intent> mIntents = new ArrayList();
        private boolean mPinned;
        private final List<ResolveInfo> mResolveInfos = new ArrayList();
        public final ComponentName name;

        public ResolvedComponentInfo(ComponentName name, Intent intent, ResolveInfo info) {
            this.name = name;
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
            return index >= 0 ? (Intent) this.mIntents.get(index) : null;
        }

        public ResolveInfo getResolveInfoAt(int index) {
            return index >= 0 ? (ResolveInfo) this.mResolveInfos.get(index) : null;
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

    static class ViewHolder {
        public ImageView badge;
        public ImageView icon;
        public TextView text;
        public TextView text2;

        public ViewHolder(View view) {
            this.text = (TextView) view.findViewById(R.id.text1);
            this.text2 = (TextView) view.findViewById(R.id.text2);
            this.icon = (ImageView) view.findViewById(R.id.icon);
            this.badge = (ImageView) view.findViewById(R.id.target_badge);
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

    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = makeMyIntent();
        Set<String> categories = intent.getCategories();
        if ("android.intent.action.MAIN".equals(intent.getAction()) && categories != null && categories.size() == 1 && categories.contains("android.intent.category.HOME")) {
            this.mResolvingHome = true;
        }
        setSafeForwardingMode(true);
        onCreate(savedInstanceState, intent, null, 0, null, null, true);
    }

    protected void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, Intent[] initialIntents, List<ResolveInfo> rList, boolean supportsAlwaysUseOption) {
        onCreate(savedInstanceState, intent, title, 0, initialIntents, rList, supportsAlwaysUseOption);
    }

    protected void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, int defaultTitleRes, Intent[] initialIntents, List<ResolveInfo> rList, boolean supportsAlwaysUseOption) {
        setTheme(R.style.Theme_DeviceDefault_Resolver);
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
                } catch (NameNotFoundException e2) {
                    this.mPmForParent = getPackageManager();
                }
            }
        }
        this.mPackageMonitor.register(this, getMainLooper(), false);
        this.mRegistered = true;
        this.mReferrerPackage = getReferrerPackageName();
        this.mSupportsAlwaysUseOption = supportsAlwaysUseOption;
        this.mIconDpi = ((ActivityManager) getSystemService(LaunchMode.ACTIVITY)).getLauncherLargeIconDensity();
        this.mIntents.add(0, new Intent(intent));
        this.mTitle = title;
        this.mDefaultTitleResId = defaultTitleRes;
        if (!configureContentView(this.mIntents, initialIntents, rList)) {
            int i;
            ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(R.id.contentPanel);
            if (rdl != null) {
                rdl.setOnDismissedListener(new OnDismissedListener() {
                    public void onDismissed() {
                        ResolverActivity.this.finish();
                    }
                });
                if (isVoiceInteraction()) {
                    rdl.setCollapsed(false);
                }
                this.mResolverDrawerLayout = rdl;
            }
            this.mProfileView = findViewById(R.id.profile_button);
            if (this.mProfileView != null) {
                this.mProfileView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        DisplayResolveInfo dri = ResolverActivity.this.mAdapter.getOtherProfile();
                        if (dri != null) {
                            ResolverActivity.this.mProfileSwitchMessageId = -1;
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
                i = MetricsEvent.ACTION_SHOW_APP_DISAMBIG_APP_FEATURED;
            } else {
                i = MetricsEvent.ACTION_SHOW_APP_DISAMBIG_NONE_FEATURED;
            }
            MetricsLogger.action((Context) this, i, intent.getAction() + SettingsStringUtil.DELIMITER + intent.getType() + SettingsStringUtil.DELIMITER + (categories != null ? Arrays.toString(categories.toArray()) : LogException.NO_VALUE));
        }
    }

    public void onSetupVoiceInteraction() {
        sendVoiceChoicesIfNeeded();
    }

    public void sendVoiceChoicesIfNeeded() {
        if (isVoiceInteraction()) {
            Option[] options = new Option[this.mAdapter.getCount()];
            int N = options.length;
            for (int i = 0; i < N; i++) {
                options[i] = optionForChooserTarget(this.mAdapter.getItem(i), i);
            }
            this.mPickOptionRequest = new PickTargetOptionRequest(new Prompt(getTitle()), options, null);
            getVoiceInteractor().submitRequest(this.mPickOptionRequest);
        }
    }

    Option optionForChooserTarget(TargetInfo target, int index) {
        return new Option(target.getDisplayLabel(), index);
    }

    protected final void setAdditionalTargets(Intent[] intents) {
        if (intents != null) {
            for (Intent intent : intents) {
                this.mIntents.add(intent);
            }
        }
    }

    public Intent getTargetIntent() {
        return this.mIntents.isEmpty() ? null : (Intent) this.mIntents.get(0);
    }

    protected String getReferrerPackageName() {
        Uri referrer = getReferrer();
        if (referrer == null || !"android-app".equals(referrer.getScheme())) {
            return null;
        }
        return referrer.getHost();
    }

    public int getLayoutResource() {
        return R.layout.resolver_list;
    }

    void bindProfileView() {
        DisplayResolveInfo dri = this.mAdapter.getOtherProfile();
        if (dri != null) {
            this.mProfileView.setVisibility(0);
            View text = this.mProfileView.findViewById(R.id.profile_button);
            if (!(text instanceof TextView)) {
                text = this.mProfileView.findViewById(R.id.text1);
            }
            ((TextView) text).setText(dri.getDisplayLabel());
            return;
        }
        this.mProfileView.setVisibility(8);
    }

    private void setProfileSwitchMessageId(int contentUserHint) {
        if (contentUserHint != -2 && contentUserHint != UserHandle.myUserId()) {
            boolean originIsManaged;
            UserManager userManager = (UserManager) getSystemService("user");
            UserInfo originUserInfo = userManager.getUserInfo(contentUserHint);
            if (originUserInfo != null) {
                originIsManaged = originUserInfo.isManagedProfile();
            } else {
                originIsManaged = false;
            }
            boolean targetIsManaged = userManager.isManagedProfile();
            if (originIsManaged && (targetIsManaged ^ 1) != 0) {
                this.mProfileSwitchMessageId = R.string.forward_intent_to_owner;
            } else if (!originIsManaged && targetIsManaged) {
                this.mProfileSwitchMessageId = R.string.forward_intent_to_work;
            }
        }
    }

    public void setSafeForwardingMode(boolean safeForwarding) {
        this.mSafeForwardingMode = safeForwarding;
    }

    protected CharSequence getTitleForAction(String action, int defaultTitleRes) {
        ActionTitle title = this.mResolvingHome ? ActionTitle.HOME : ActionTitle.forAction(action);
        boolean named = this.mAdapter.getFilteredPosition() >= 0;
        if (title == ActionTitle.DEFAULT && defaultTitleRes != 0) {
            return getString(defaultTitleRes);
        }
        CharSequence string;
        if (named) {
            string = getString(title.namedTitleRes, new Object[]{this.mAdapter.getFilteredItem().getDisplayLabel()});
        } else {
            string = getString(title.titleRes);
        }
        return string;
    }

    void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    Drawable getIcon(Resources res, int resId) {
        try {
            return res.getDrawableForDensity(resId, this.mIconDpi);
        } catch (NotFoundException e) {
            return null;
        }
    }

    Drawable loadIconForResolveInfo(ResolveInfo ri) {
        try {
            Drawable dr;
            if (!(ri.resolvePackageName == null || ri.icon == 0)) {
                dr = getIcon((this.mIsClonedProfile ? this.mPmForParent : this.mPm).getResourcesForApplication(ri.resolvePackageName), ri.icon);
                if (dr != null) {
                    return dr;
                }
            }
            int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                dr = getIcon((this.mIsClonedProfile ? this.mPmForParent : this.mPm).getResourcesForApplication(ri.activityInfo.packageName), iconRes);
                if (dr != null) {
                    return dr;
                }
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Couldn't find resources for package", e);
        }
        return ri.loadIcon(this.mIsClonedProfile ? this.mPmForParent : this.mPm);
    }

    protected void onRestart() {
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

    protected void onStop() {
        super.onStop();
        if (this.mRegistered) {
            this.mPackageMonitor.unregister();
            this.mRegistered = false;
        }
        if ((getIntent().getFlags() & 268435456) != 0 && (isVoiceInteraction() ^ 1) != 0 && (this.mResolvingHome ^ 1) != 0 && (this.mRetainInOnStop ^ 1) != 0 && !isChangingConfigurations()) {
            finish();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (!(isChangingConfigurations() || this.mPickOptionRequest == null)) {
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

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
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
        boolean z = false;
        try {
            int i;
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
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void setAlwaysButtonEnabled(boolean hasValidSelection, int checkedPos, boolean filtered) {
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
        int filteredPosition;
        int id = v.getId();
        if (this.mAdapter.hasFilteredItem()) {
            filteredPosition = this.mAdapter.getFilteredPosition();
        } else {
            filteredPosition = this.mAdapterView.getCheckedItemPosition();
        }
        startSelected(filteredPosition, id == R.id.button_always, this.mAdapter.hasFilteredItem() ^ 1);
    }

    public void startSelected(int which, boolean always, boolean hasIndexBeenFiltered) {
        if (!isFinishing()) {
            ResolveInfo ri = this.mAdapter.resolveInfoForPosition(which, hasIndexBeenFiltered);
            if (this.mResolvingHome && hasManagedProfile() && (supportsManagedProfiles(ri) ^ 1) != 0) {
                Toast.makeText((Context) this, String.format(getResources().getString(R.string.activity_resolver_work_profiles_support), new Object[]{ri.activityInfo.loadLabel(getPackageManager()).toString()}), 1).show();
                return;
            }
            TargetInfo target = this.mAdapter.targetInfoForPosition(which, hasIndexBeenFiltered);
            if (target != null && onTargetSelected(target, always)) {
                int i;
                if (always && this.mSupportsAlwaysUseOption) {
                    MetricsLogger.action((Context) this, (int) MetricsEvent.ACTION_APP_DISAMBIG_ALWAYS);
                } else if (this.mSupportsAlwaysUseOption) {
                    MetricsLogger.action((Context) this, (int) MetricsEvent.ACTION_APP_DISAMBIG_JUST_ONCE);
                } else {
                    MetricsLogger.action((Context) this, (int) MetricsEvent.ACTION_APP_DISAMBIG_TAP);
                }
                if (this.mAdapter.hasFilteredItem()) {
                    i = MetricsEvent.ACTION_HIDE_APP_DISAMBIG_APP_FEATURED;
                } else {
                    i = MetricsEvent.ACTION_HIDE_APP_DISAMBIG_NONE_FEATURED;
                }
                MetricsLogger.action((Context) this, i);
                finish();
            }
        }
    }

    public Intent getReplacementIntent(ActivityInfo aInfo, Intent defIntent) {
        return defIntent;
    }

    protected boolean onTargetSelected(TargetInfo target, boolean alwaysCheck) {
        ResolveInfo ri = target.getResolveInfo();
        Intent intent = target != null ? target.getResolvedIntent() : null;
        if (intent != null && ((this.mSupportsAlwaysUseOption || this.mAdapter.hasFilteredItem()) && this.mAdapter.mUnfilteredResolveList != null)) {
            Intent filterIntent;
            IntentFilter filter = new IntentFilter();
            if (intent.getSelector() != null) {
                filterIntent = intent.getSelector();
            } else {
                filterIntent = intent;
            }
            String action = filterIntent.getAction();
            if (action != null) {
                filter.addAction(action);
            }
            Set<String> categories = filterIntent.getCategories();
            if (categories != null) {
                for (String cat : categories) {
                    filter.addCategory(cat);
                }
            }
            filter.addCategory("android.intent.category.DEFAULT");
            int cat2 = ri.match & 268369920;
            Uri data = filterIntent.getData();
            if (cat2 == 6291456) {
                String mimeType = filterIntent.resolveType(this);
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (Throwable e) {
                        Log.w(TAG, e);
                        filter = null;
                    }
                }
            }
            if (data != null && data.getScheme() != null && (cat2 != 6291456 || (!"file".equals(data.getScheme()) && (VoiceInteractionSession.KEY_CONTENT.equals(data.getScheme()) ^ 1) != 0))) {
                PatternMatcher p;
                filter.addDataScheme(data.getScheme());
                Iterator<PatternMatcher> pIt = ri.filter.schemeSpecificPartsIterator();
                if (pIt != null) {
                    String ssp = data.getSchemeSpecificPart();
                    while (ssp != null && pIt.hasNext()) {
                        p = (PatternMatcher) pIt.next();
                        if (p.match(ssp)) {
                            filter.addDataSchemeSpecificPart(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
                Iterator<AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                if (aIt != null) {
                    while (aIt.hasNext()) {
                        AuthorityEntry a = (AuthorityEntry) aIt.next();
                        if (a.match(data) >= 0) {
                            int port = a.getPort();
                            filter.addDataAuthority(a.getHost(), port >= 0 ? Integer.toString(port) : null);
                        }
                    }
                }
                pIt = ri.filter.pathsIterator();
                if (pIt != null) {
                    String path = data.getPath();
                    while (path != null && pIt.hasNext()) {
                        p = (PatternMatcher) pIt.next();
                        if (p.match(path)) {
                            filter.addDataPath(p.getPath(), p.getType());
                            break;
                        }
                    }
                }
            }
            if (filter != null) {
                ComponentName[] set;
                int N = this.mAdapter.mUnfilteredResolveList.size();
                boolean needToAddBackProfileForwardingComponent = this.mAdapter.mOtherProfile != null;
                if (needToAddBackProfileForwardingComponent) {
                    set = new ComponentName[(N + 1)];
                } else {
                    set = new ComponentName[N];
                }
                int bestMatch = 0;
                for (int i = 0; i < N; i++) {
                    ResolveInfo r = ((ResolvedComponentInfo) this.mAdapter.mUnfilteredResolveList.get(i)).getResolveInfoAt(0);
                    set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                    if (r.match > bestMatch) {
                        bestMatch = r.match;
                    }
                }
                if (needToAddBackProfileForwardingComponent) {
                    set[N] = this.mAdapter.mOtherProfile.getResolvedComponentName();
                    int otherProfileMatch = this.mAdapter.mOtherProfile.getResolveInfo().match;
                    if (otherProfileMatch > bestMatch) {
                        bestMatch = otherProfileMatch;
                    }
                }
                if (alwaysCheck) {
                    int userId = getUserId();
                    PackageManager pm = getPackageManager();
                    pm.addPreferredActivity(filter, bestMatch, set, intent.getComponent());
                    if (!ri.handleAllWebDataURI) {
                        boolean isHttpOrHttps;
                        String packageName = intent.getComponent().getPackageName();
                        String dataScheme = data != null ? data.getScheme() : null;
                        if (dataScheme == null) {
                            isHttpOrHttps = false;
                        } else if (dataScheme.equals("http")) {
                            isHttpOrHttps = true;
                        } else {
                            isHttpOrHttps = dataScheme.equals("https");
                        }
                        boolean isViewAction = action != null ? action.equals("android.intent.action.VIEW") : false;
                        boolean hasCategoryBrowsable;
                        if (categories != null) {
                            hasCategoryBrowsable = categories.contains("android.intent.category.BROWSABLE");
                        } else {
                            hasCategoryBrowsable = false;
                        }
                        if (isHttpOrHttps && isViewAction && hasCategoryBrowsable) {
                            pm.updateIntentVerificationStatusAsUser(packageName, 2, userId);
                        }
                    } else if (TextUtils.isEmpty(pm.getDefaultBrowserPackageNameAsUser(userId))) {
                        pm.setDefaultBrowserPackageNameAsUser(ri.activityInfo.packageName, userId);
                    }
                } else {
                    try {
                        this.mAdapter.mResolverListController.setLastChosen(intent, filter, bestMatch);
                    } catch (RemoteException re) {
                        Log.d(TAG, "Error calling setLastChosenActivity\n" + re);
                    }
                }
            }
        }
        if (target != null) {
            safelyStartActivity(target);
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
        if (this.mProfileSwitchMessageId != -1) {
            Toast.makeText((Context) this, getString(this.mProfileSwitchMessageId), 1).show();
        }
        if (this.mSafeForwardingMode) {
            try {
                if (cti.startAsCaller(this, null, -10000)) {
                    onActivityStarted(cti);
                }
            } catch (RuntimeException e) {
                String launchedFromPackage;
                try {
                    launchedFromPackage = ActivityManager.getService().getLaunchedFromPackage(getActivityToken());
                } catch (RemoteException e2) {
                    launchedFromPackage = "??";
                }
                Slog.wtf(TAG, "Unable to launch as uid " + this.mLaunchedFromUid + " package " + launchedFromPackage + ", while running in " + ActivityThread.currentProcessName(), e);
            }
            return;
        }
        if (cti.start(this, null)) {
            onActivityStarted(cti);
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
        startActivity(new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts(Intents.EXTRA_PACKAGE_NAME, ri.activityInfo.packageName, null)).addFlags(524288));
    }

    public ResolveListAdapter createAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
        return new ResolveListAdapter(context, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed, createListController());
    }

    protected ResolverListController createListController() {
        return new ResolverListController(this, this.mPm, getTargetIntent(), getReferrerPackageName(), this.mLaunchedFromUid);
    }

    public boolean configureContentView(List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList) {
        this.mAdapter = createAdapter(this, payloadIntents, initialIntents, rList, this.mLaunchedFromUid, this.mSupportsAlwaysUseOption ? isVoiceInteraction() ^ 1 : false);
        boolean rebuildCompleted = this.mAdapter.rebuildList();
        if (useLayoutWithDefault()) {
            this.mLayoutId = R.layout.resolver_list_with_default;
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
        this.mAdapterView = (AbsListView) findViewById(R.id.resolver_list);
        if (count == 0 && this.mAdapter.mPlaceholderCount == 0) {
            ((TextView) findViewById(R.id.empty)).setVisibility(0);
            this.mAdapterView.setVisibility(8);
        } else {
            this.mAdapterView.setVisibility(0);
            onPrepareAdapterView(this.mAdapterView, this.mAdapter);
        }
        return false;
    }

    public void onPrepareAdapterView(AbsListView adapterView, ResolveListAdapter adapter) {
        boolean useHeader = adapter.hasFilteredItem();
        ViewGroup listView = adapterView instanceof ListView ? (ListView) adapterView : null;
        adapterView.setAdapter(this.mAdapter);
        ItemClickListener listener = new ItemClickListener();
        adapterView.setOnItemClickListener(listener);
        adapterView.setOnItemLongClickListener(listener);
        if (this.mSupportsAlwaysUseOption && listView != null) {
            listView.setChoiceMode(1);
        }
        if (useHeader && listView != null && listView.getHeaderViewsCount() == 0) {
            listView.addHeaderView(LayoutInflater.from(this).inflate((int) R.layout.resolver_different_item_header, listView, false));
        }
    }

    public void setTitleAndIcon() {
        TextView titleView;
        CharSequence title;
        if (this.mAdapter.getCount() == 0 && this.mAdapter.mPlaceholderCount == 0) {
            titleView = (TextView) findViewById(R.id.title);
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
            titleView = (TextView) findViewById(R.id.title);
            if (titleView != null) {
                titleView.setText(title);
            }
            setTitle(title);
            ImageView titleIcon = (ImageView) findViewById(R.id.title_icon);
            if (titleIcon != null) {
                ApplicationInfo ai = null;
                try {
                    if (!TextUtils.isEmpty(this.mReferrerPackage)) {
                        int i;
                        PackageManager packageManager = this.mPm;
                        String str = this.mReferrerPackage;
                        if (this.mIsClonedProfile) {
                            i = 4202496;
                        } else {
                            i = 0;
                        }
                        ai = packageManager.getApplicationInfo(str, i);
                    }
                } catch (NameNotFoundException e) {
                    Log.e(TAG, "Could not find referrer package " + this.mReferrerPackage);
                }
                if (ai != null) {
                    titleIcon.setImageDrawable(ai.loadIcon(this.mIsClonedProfile ? this.mPmForParent : this.mPm));
                }
            }
        }
        ImageView iconView = (ImageView) findViewById(R.id.icon);
        DisplayResolveInfo iconInfo = this.mAdapter.getFilteredItem();
        if (iconView != null && iconInfo != null) {
            new LoadIconIntoViewTask(iconInfo, iconView).execute(new Void[0]);
        }
    }

    public void resetAlwaysOrOnceButtonBar() {
        if (this.mSupportsAlwaysUseOption) {
            ViewGroup buttonLayout = (ViewGroup) findViewById(R.id.button_bar);
            if (buttonLayout != null) {
                buttonLayout.setVisibility(0);
                this.mAlwaysButton = (Button) buttonLayout.findViewById(R.id.button_always);
                this.mOnceButton = (Button) buttonLayout.findViewById(R.id.button_once);
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

    private boolean useLayoutWithDefault() {
        return this.mSupportsAlwaysUseOption ? this.mAdapter.hasFilteredItem() : false;
    }

    protected void setRetainInOnStop(boolean retainInOnStop) {
        this.mRetainInOnStop = retainInOnStop;
    }

    static boolean resolveInfoMatch(ResolveInfo lhs, ResolveInfo rhs) {
        if (lhs == null) {
            return rhs == null;
        } else {
            if (lhs.activityInfo != null) {
                return Objects.equals(lhs.activityInfo.name, rhs.activityInfo.name) ? Objects.equals(lhs.activityInfo.packageName, rhs.activityInfo.packageName) : false;
            } else {
                if (rhs.activityInfo != null) {
                    return false;
                }
                return true;
            }
        }
    }

    static final boolean isSpecificUriMatch(int match) {
        match &= 268369920;
        if (match < 3145728 || match > 5242880) {
            return false;
        }
        return true;
    }
}
