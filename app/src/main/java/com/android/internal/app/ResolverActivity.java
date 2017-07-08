package com.android.internal.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.AppGlobals;
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
import android.content.pm.ComponentInfo;
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
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import com.android.ims.ImsConferenceState;
import com.android.internal.R;
import com.android.internal.content.PackageMonitor;
import com.android.internal.util.Protocol;
import com.android.internal.widget.ResolverDrawerLayout;
import com.android.internal.widget.ResolverDrawerLayout.OnDismissedListener;
import com.huawei.pgmng.log.LogPower;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ResolverActivity extends Activity {
    private static final boolean DEBUG = false;
    private static final String TAG = "ResolverActivity";
    private ResolveListAdapter mAdapter;
    private AbsListView mAdapterView;
    private Button mAlwaysButton;
    private boolean mAlwaysUseOption;
    private ComponentName[] mFilteredComponents;
    private int mIconDpi;
    private final ArrayList<Intent> mIntents;
    private int mLastSelected;
    private int mLaunchedFromUid;
    private Button mOnceButton;
    private final PackageMonitor mPackageMonitor;
    private PickTargetOptionRequest mPickOptionRequest;
    private PackageManager mPm;
    private int mProfileSwitchMessageId;
    private View mProfileView;
    private boolean mRegistered;
    private ResolverComparator mResolverComparator;
    protected ResolverDrawerLayout mResolverDrawerLayout;
    private boolean mResolvingHome;
    private boolean mSafeForwardingMode;

    public class ResolveListAdapter extends BaseAdapter {
        private final List<ResolveInfo> mBaseResolveList;
        List<DisplayResolveInfo> mDisplayList;
        private boolean mFilterLastUsed;
        private boolean mHasExtendedInfo;
        protected final LayoutInflater mInflater;
        private final Intent[] mInitialIntents;
        private final List<Intent> mIntents;
        private ResolveInfo mLastChosen;
        private int mLastChosenPosition;
        private final int mLaunchedFromUid;
        List<ResolvedComponentInfo> mOrigResolveList;
        private DisplayResolveInfo mOtherProfile;

        public ResolveListAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
            this.mLastChosenPosition = -1;
            this.mIntents = payloadIntents;
            this.mInitialIntents = initialIntents;
            this.mBaseResolveList = rList;
            this.mLaunchedFromUid = launchedFromUid;
            this.mInflater = LayoutInflater.from(context);
            this.mDisplayList = new ArrayList();
            this.mFilterLastUsed = filterLastUsed;
            rebuildList();
        }

        public void handlePackagesChanged() {
            rebuildList();
            notifyDataSetChanged();
            if (getCount() == 0) {
                ResolverActivity.this.finish();
            }
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
            return (!this.mFilterLastUsed || this.mLastChosenPosition < 0) ? ResolverActivity.DEBUG : true;
        }

        public float getScore(DisplayResolveInfo target) {
            return ResolverActivity.this.mResolverComparator.getScore(target.getResolvedComponentName());
        }

        private void rebuildList() {
            int N;
            int i;
            List list = null;
            try {
                Intent primaryIntent = ResolverActivity.this.getTargetIntent();
                this.mLastChosen = AppGlobals.getPackageManager().getLastChosenActivity(primaryIntent, primaryIntent.resolveTypeIfNeeded(ResolverActivity.this.getContentResolver()), Protocol.BASE_SYSTEM_RESERVED);
            } catch (RemoteException re) {
                Log.d(ResolverActivity.TAG, "Error calling setLastChosenActivity\n" + re);
            }
            this.mOtherProfile = null;
            this.mDisplayList.clear();
            if (this.mBaseResolveList != null) {
                list = new ArrayList();
                this.mOrigResolveList = list;
                addResolveListDedupe(list, ResolverActivity.this.getTargetIntent(), this.mBaseResolveList);
            } else {
                boolean shouldGetResolvedFilter = shouldGetResolvedFilter();
                boolean shouldGetActivityMetadata = ResolverActivity.this.shouldGetActivityMetadata();
                N = this.mIntents.size();
                for (i = 0; i < N; i++) {
                    Intent intent = (Intent) this.mIntents.get(i);
                    List<ResolveInfo> infos = ResolverActivity.this.mPm.queryIntentActivities(intent, (shouldGetActivityMetadata ? LogPower.START_CHG_ROTATION : 0) | (Protocol.BASE_SYSTEM_RESERVED | (shouldGetResolvedFilter ? 64 : 0)));
                    if (infos != null) {
                        if (list == null) {
                            list = new ArrayList();
                            this.mOrigResolveList = list;
                        }
                        addResolveListDedupe(list, intent, infos);
                    }
                }
                if (list != null) {
                    for (i = list.size() - 1; i >= 0; i--) {
                        ActivityInfo ai = ((ResolvedComponentInfo) list.get(i)).getResolveInfoAt(0).activityInfo;
                        int granted = ActivityManager.checkComponentPermission(ai.permission, this.mLaunchedFromUid, ai.applicationInfo.uid, ai.exported);
                        boolean suspended = (ai.applicationInfo.flags & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0 ? true : ResolverActivity.DEBUG;
                        if (granted != 0 || suspended || ResolverActivity.this.isComponentFiltered(ai)) {
                            if (this.mOrigResolveList == list) {
                                this.mOrigResolveList = new ArrayList(this.mOrigResolveList);
                            }
                            list.remove(i);
                        }
                    }
                }
            }
            if (list != null) {
                N = list.size();
                if (N > 0) {
                    ResolveInfo ri;
                    ResolveInfo r0 = ((ResolvedComponentInfo) list.get(0)).getResolveInfoAt(0);
                    for (i = 1; i < N; i++) {
                        ri = ((ResolvedComponentInfo) list.get(i)).getResolveInfoAt(0);
                        if (r0.priority != ri.priority || r0.isDefault != ri.isDefault) {
                            while (i < N) {
                                if (this.mOrigResolveList == list) {
                                    this.mOrigResolveList = new ArrayList(this.mOrigResolveList);
                                }
                                list.remove(i);
                                N--;
                            }
                        }
                    }
                    if (N > 1) {
                        ResolverActivity.this.mResolverComparator.compute(list);
                        Collections.sort(list, ResolverActivity.this.mResolverComparator);
                    }
                    if (this.mInitialIntents != null) {
                        for (Intent ii : this.mInitialIntents) {
                            if (ii != null) {
                                ai = ii.resolveActivityInfo(ResolverActivity.this.getPackageManager(), 0);
                                if (ai == null) {
                                    Log.w(ResolverActivity.TAG, "No activity found for " + ii);
                                } else {
                                    ri = new ResolveInfo();
                                    ri.activityInfo = ai;
                                    UserManager userManager = (UserManager) ResolverActivity.this.getSystemService(ImsConferenceState.USER);
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
                                    addResolveInfo(new DisplayResolveInfo(ResolverActivity.this, ii, ri, ri.loadLabel(ResolverActivity.this.getPackageManager()), null, ii));
                                }
                            }
                        }
                    }
                    ResolvedComponentInfo rci0 = (ResolvedComponentInfo) list.get(0);
                    r0 = rci0.getResolveInfoAt(0);
                    int start = 0;
                    CharSequence r0Label = r0.loadLabel(ResolverActivity.this.mPm);
                    this.mHasExtendedInfo = ResolverActivity.DEBUG;
                    for (i = 1; i < N; i++) {
                        if (r0Label == null) {
                            r0Label = r0.activityInfo.packageName;
                        }
                        ResolvedComponentInfo rci = (ResolvedComponentInfo) list.get(i);
                        ri = rci.getResolveInfoAt(0);
                        CharSequence riLabel = ri.loadLabel(ResolverActivity.this.mPm);
                        if (riLabel == null) {
                            riLabel = ri.activityInfo.packageName;
                        }
                        if (!riLabel.equals(r0Label)) {
                            processGroup(list, start, i - 1, rci0, r0Label);
                            rci0 = rci;
                            r0 = ri;
                            r0Label = riLabel;
                            start = i;
                        }
                    }
                    processGroup(list, start, N - 1, rci0, r0Label);
                }
            }
            if (this.mOtherProfile != null && this.mLastChosenPosition >= 0) {
                this.mLastChosenPosition = -1;
                this.mFilterLastUsed = ResolverActivity.DEBUG;
            }
            onListRebuilt();
        }

        private void addResolveListDedupe(List<ResolvedComponentInfo> into, Intent intent, List<ResolveInfo> from) {
            int fromCount = from.size();
            int intoCount = into.size();
            for (int i = 0; i < fromCount; i++) {
                ResolveInfo newInfo = (ResolveInfo) from.get(i);
                boolean found = ResolverActivity.DEBUG;
                for (int j = 0; j < intoCount; j++) {
                    ResolvedComponentInfo rci = (ResolvedComponentInfo) into.get(i);
                    if (isSameResolvedComponent(newInfo, rci)) {
                        found = true;
                        rci.add(intent, newInfo);
                        break;
                    }
                }
                if (!found) {
                    ComponentName name = new ComponentName(newInfo.activityInfo.packageName, newInfo.activityInfo.name);
                    rci = new ResolvedComponentInfo(name, intent, newInfo);
                    rci.setPinned(isComponentPinned(name));
                    into.add(rci);
                }
            }
        }

        private boolean isSameResolvedComponent(ResolveInfo a, ResolvedComponentInfo b) {
            ActivityInfo ai = a.activityInfo;
            if (ai.packageName.equals(b.name.getPackageName())) {
                return ai.name.equals(b.name.getClassName());
            }
            return ResolverActivity.DEBUG;
        }

        public void onListRebuilt() {
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
            boolean usePkg = ResolverActivity.DEBUG;
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
            DisplayResolveInfo dri = new DisplayResolveInfo(ResolverActivity.this, intent, add, roLabel, extraInfo, replaceIntent);
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
            if (this.mLastChosen != null && this.mLastChosen.activityInfo.packageName.equals(info.activityInfo.packageName) && this.mLastChosen.activityInfo.name.equals(info.activityInfo.name)) {
                this.mLastChosenPosition = this.mDisplayList.size() - 1;
            }
        }

        private void addResolveInfo(DisplayResolveInfo dri) {
            if (dri.mResolveInfo.targetUserId == -2 || this.mOtherProfile != null) {
                this.mDisplayList.add(dri);
            } else {
                this.mOtherProfile = dri;
            }
        }

        public ResolveInfo resolveInfoForPosition(int position, boolean filtered) {
            return (filtered ? getItem(position) : (TargetInfo) this.mDisplayList.get(position)).getResolveInfo();
        }

        public TargetInfo targetInfoForPosition(int position, boolean filtered) {
            return filtered ? getItem(position) : (TargetInfo) this.mDisplayList.get(position);
        }

        public int getCount() {
            int result = this.mDisplayList.size();
            if (!this.mFilterLastUsed || this.mLastChosenPosition < 0) {
                return result;
            }
            return result - 1;
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
            return (TargetInfo) this.mDisplayList.get(position);
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
            return ResolverActivity.DEBUG;
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
            return this.mInflater.inflate((int) R.layout.resolve_list_item, parent, (boolean) ResolverActivity.DEBUG);
        }

        public boolean showsExtendedInfo(TargetInfo info) {
            return TextUtils.isEmpty(info.getExtendedInfo()) ? ResolverActivity.DEBUG : true;
        }

        public boolean isComponentPinned(ComponentName name) {
            return ResolverActivity.DEBUG;
        }

        public final void bindView(int position, View view) {
            onBindView(view, getItem(position));
        }

        private void onBindView(View view, TargetInfo info) {
            ViewHolder holder = (ViewHolder) view.getTag();
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
                new LoadAdapterIconTask(ResolverActivity.this, (DisplayResolveInfo) info).execute(new Void[0]);
            }
            holder.icon.setImageDrawable(info.getDisplayIcon());
            if (holder.badge != null) {
                Drawable badge = info.getBadgeIcon();
                if (badge != null) {
                    holder.badge.setImageDrawable(badge);
                    holder.badge.setContentDescription(info.getBadgeContentDescription());
                    holder.badge.setVisibility(0);
                    return;
                }
                holder.badge.setVisibility(8);
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
        ;
        
        public final String action;
        public final int labelRes;
        public final int namedTitleRes;
        public final int titleRes;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.ResolverActivity.ActionTitle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.ResolverActivity.ActionTitle.<clinit>():void
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.ResolverActivity.ActionTitle.<clinit>():void");
        }

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
        private final List<Intent> mSourceIntents;
        final /* synthetic */ ResolverActivity this$0;

        public DisplayResolveInfo(ResolverActivity this$0, Intent originalIntent, ResolveInfo pri, CharSequence pLabel, CharSequence pInfo, Intent pOrigIntent) {
            this.this$0 = this$0;
            this.mSourceIntents = new ArrayList();
            this.mSourceIntents.add(originalIntent);
            this.mResolveInfo = pri;
            this.mDisplayLabel = pLabel;
            this.mExtendedInfo = pInfo;
            if (pOrigIntent == null) {
                pOrigIntent = this$0.getReplacementIntent(pri.activityInfo, this$0.getTargetIntent());
            }
            Intent intent = new Intent(pOrigIntent);
            intent.addFlags(View.SCROLLBARS_OUTSIDE_INSET);
            ActivityInfo ai = this.mResolveInfo.activityInfo;
            intent.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
            this.mResolvedIntent = intent;
        }

        private DisplayResolveInfo(ResolverActivity this$0, DisplayResolveInfo other, Intent fillInIntent, int flags) {
            this.this$0 = this$0;
            this.mSourceIntents = new ArrayList();
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
                this.mBadge = this.mResolveInfo.activityInfo.applicationInfo.loadIcon(this.this$0.mPm);
            }
            return this.mBadge;
        }

        public CharSequence getBadgeContentDescription() {
            return null;
        }

        public TargetInfo cloneFilledIn(Intent fillInIntent, int flags) {
            return new DisplayResolveInfo(this.this$0, this, fillInIntent, flags);
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
            return this.mDisplayIcon != null ? true : ResolverActivity.DEBUG;
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
                return ResolverActivity.DEBUG;
            }
        }

        public boolean startAsCaller(Activity activity, Bundle options, int userId) {
            try {
                activity.startActivityAsCaller(this.mResolvedIntent, options, ResolverActivity.DEBUG, userId);
                return true;
            } catch (SecurityException e) {
                Log.e(ResolverActivity.TAG, "startAsCaller", e);
                return ResolverActivity.DEBUG;
            }
        }

        public boolean startAsUser(Activity activity, Bundle options, UserHandle user) {
            try {
                activity.startActivityAsUser(this.mResolvedIntent, options, user);
                return ResolverActivity.DEBUG;
            } catch (SecurityException e) {
                Log.e(ResolverActivity.TAG, "startAsUser", e);
                return ResolverActivity.DEBUG;
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
        final /* synthetic */ ResolverActivity this$0;

        ItemClickListener(ResolverActivity this$0) {
            this.this$0 = this$0;
        }

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = null;
            if (parent instanceof ListView) {
                listView = (ListView) parent;
            }
            if (listView != null) {
                position -= listView.getHeaderViewsCount();
            }
            if (position >= 0) {
                int checkedPos = this.this$0.mAdapterView.getCheckedItemPosition();
                boolean hasValidSelection = checkedPos != -1 ? true : ResolverActivity.DEBUG;
                if (!this.this$0.mAlwaysUseOption || (hasValidSelection && this.this$0.mLastSelected == checkedPos)) {
                    this.this$0.startSelected(position, ResolverActivity.DEBUG, true);
                } else {
                    this.this$0.setAlwaysButtonEnabled(hasValidSelection, checkedPos, true);
                    this.this$0.mOnceButton.setEnabled(hasValidSelection);
                    if (hasValidSelection) {
                        this.this$0.mAdapterView.smoothScrollToPosition(checkedPos);
                    }
                    this.this$0.mLastSelected = checkedPos;
                }
            }
        }

        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = null;
            if (parent instanceof ListView) {
                listView = (ListView) parent;
            }
            if (listView != null) {
                position -= listView.getHeaderViewsCount();
            }
            if (position < 0) {
                return ResolverActivity.DEBUG;
            }
            this.this$0.showTargetDetails(this.this$0.mAdapter.resolveInfoForPosition(position, true));
            return true;
        }
    }

    abstract class LoadIconTask extends AsyncTask<Void, Void, Drawable> {
        protected final DisplayResolveInfo mDisplayResolveInfo;
        private final ResolveInfo mResolveInfo;
        final /* synthetic */ ResolverActivity this$0;

        public LoadIconTask(ResolverActivity this$0, DisplayResolveInfo dri) {
            this.this$0 = this$0;
            this.mDisplayResolveInfo = dri;
            this.mResolveInfo = dri.getResolveInfo();
        }

        protected /* bridge */ /* synthetic */ Object doInBackground(Object[] params) {
            return doInBackground((Void[]) params);
        }

        protected Drawable doInBackground(Void... params) {
            return this.this$0.loadIconForResolveInfo(this.mResolveInfo);
        }

        protected /* bridge */ /* synthetic */ void onPostExecute(Object d) {
            onPostExecute((Drawable) d);
        }

        protected void onPostExecute(Drawable d) {
            this.mDisplayResolveInfo.setDisplayIcon(d);
        }
    }

    class LoadAdapterIconTask extends LoadIconTask {
        final /* synthetic */ ResolverActivity this$0;

        public LoadAdapterIconTask(ResolverActivity this$0, DisplayResolveInfo dri) {
            this.this$0 = this$0;
            super(this$0, dri);
        }

        protected void onPostExecute(Drawable d) {
            super.onPostExecute(d);
            if (this.this$0.mProfileView != null && this.this$0.mAdapter.getOtherProfile() == this.mDisplayResolveInfo) {
                this.this$0.bindProfileView();
            }
            this.this$0.mAdapter.notifyDataSetChanged();
        }
    }

    class LoadIconIntoViewTask extends LoadIconTask {
        private final ImageView mTargetView;
        final /* synthetic */ ResolverActivity this$0;

        public LoadIconIntoViewTask(ResolverActivity this$0, DisplayResolveInfo dri, ImageView target) {
            this.this$0 = this$0;
            super(this$0, dri);
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
                if (ra != null && ra.onTargetSelected(ra.mAdapter.getItem(selections[0].getIndex()), ResolverActivity.DEBUG)) {
                    ra.mPickOptionRequest = null;
                    ra.finish();
                }
            }
        }
    }

    static final class ResolvedComponentInfo {
        private final List<Intent> mIntents;
        private boolean mPinned;
        private final List<ResolveInfo> mResolveInfos;
        public final ComponentName name;

        public ResolvedComponentInfo(ComponentName name, Intent intent, ResolveInfo info) {
            this.mIntents = new ArrayList();
            this.mResolveInfos = new ArrayList();
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

    public ResolverActivity() {
        this.mLastSelected = -1;
        this.mResolvingHome = DEBUG;
        this.mProfileSwitchMessageId = -1;
        this.mIntents = new ArrayList();
        this.mPackageMonitor = new PackageMonitor() {
            public void onSomePackagesChanged() {
                ResolverActivity.this.mAdapter.handlePackagesChanged();
                if (ResolverActivity.this.mProfileView != null) {
                    ResolverActivity.this.bindProfileView();
                }
            }
        };
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

    protected void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, Intent[] initialIntents, List<ResolveInfo> rList, boolean alwaysUseOption) {
        onCreate(savedInstanceState, intent, title, 0, initialIntents, rList, alwaysUseOption);
    }

    protected void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, int defaultTitleRes, Intent[] initialIntents, List<ResolveInfo> rList, boolean alwaysUseOption) {
        setTheme(R.style.Theme_DeviceDefault_Resolver);
        super.onCreate(savedInstanceState);
        setProfileSwitchMessageId(intent.getContentUserHint());
        try {
            this.mLaunchedFromUid = ActivityManagerNative.getDefault().getLaunchedFromUid(getActivityToken());
        } catch (RemoteException e) {
            this.mLaunchedFromUid = -1;
        }
        if (this.mLaunchedFromUid < 0 || UserHandle.isIsolated(this.mLaunchedFromUid)) {
            finish();
            return;
        }
        this.mPm = getPackageManager();
        this.mPackageMonitor.register(this, getMainLooper(), DEBUG);
        this.mRegistered = true;
        this.mIconDpi = ((ActivityManager) getSystemService("activity")).getLauncherLargeIconDensity();
        this.mIntents.add(0, new Intent(intent));
        String referrerPackage = getReferrerPackageName();
        this.mResolverComparator = new ResolverComparator(this, getTargetIntent(), referrerPackage);
        if (!configureContentView(this.mIntents, initialIntents, rList, alwaysUseOption)) {
            ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(R.id.contentPanel);
            if (rdl != null) {
                rdl.setOnDismissedListener(new OnDismissedListener() {
                    public void onDismissed() {
                        ResolverActivity.this.finish();
                    }
                });
                if (isVoiceInteraction()) {
                    rdl.setCollapsed(DEBUG);
                }
                this.mResolverDrawerLayout = rdl;
            }
            if (title == null) {
                title = getTitleForAction(intent.getAction(), defaultTitleRes);
            }
            if (!TextUtils.isEmpty(title)) {
                TextView titleView = (TextView) findViewById(R.id.title);
                if (titleView != null) {
                    titleView.setText(title);
                }
                setTitle(title);
                ImageView titleIcon = (ImageView) findViewById(R.id.title_icon);
                if (titleIcon != null) {
                    ApplicationInfo ai = null;
                    try {
                        if (!TextUtils.isEmpty(referrerPackage)) {
                            ai = this.mPm.getApplicationInfo(referrerPackage, 0);
                        }
                    } catch (NameNotFoundException e2) {
                        Log.e(TAG, "Could not find referrer package " + referrerPackage);
                    }
                    if (ai != null) {
                        titleIcon.setImageDrawable(ai.loadIcon(this.mPm));
                    }
                }
            }
            ImageView iconView = (ImageView) findViewById(R.id.icon);
            DisplayResolveInfo iconInfo = this.mAdapter.getFilteredItem();
            if (!(iconView == null || iconInfo == null)) {
                new LoadIconIntoViewTask(this, iconInfo, iconView).execute(new Void[0]);
            }
            if (alwaysUseOption || this.mAdapter.hasFilteredItem()) {
                ViewGroup buttonLayout = (ViewGroup) findViewById(R.id.button_bar);
                if (buttonLayout != null) {
                    buttonLayout.setVisibility(0);
                    this.mAlwaysButton = (Button) buttonLayout.findViewById(R.id.button_always);
                    this.mOnceButton = (Button) buttonLayout.findViewById(R.id.button_once);
                } else {
                    this.mAlwaysUseOption = DEBUG;
                }
            }
            if (this.mAdapter.hasFilteredItem()) {
                setAlwaysButtonEnabled(true, this.mAdapter.getFilteredPosition(), DEBUG);
                this.mOnceButton.setEnabled(true);
            }
            this.mProfileView = findViewById(R.id.profile_button);
            if (this.mProfileView != null) {
                this.mProfileView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        DisplayResolveInfo dri = ResolverActivity.this.mAdapter.getOtherProfile();
                        if (dri != null) {
                            ResolverActivity.this.mProfileSwitchMessageId = -1;
                            ResolverActivity.this.onTargetSelected(dri, ResolverActivity.DEBUG);
                            ResolverActivity.this.finish();
                        }
                    }
                });
                bindProfileView();
            }
            if (isVoiceInteraction()) {
                onSetupVoiceInteraction();
            }
        }
    }

    public final void setFilteredComponents(ComponentName[] components) {
        this.mFilteredComponents = components;
    }

    public final boolean isComponentFiltered(ComponentInfo component) {
        if (this.mFilteredComponents == null) {
            return DEBUG;
        }
        ComponentName checkName = component.getComponentName();
        for (ComponentName name : this.mFilteredComponents) {
            if (name.equals(checkName)) {
                return true;
            }
        }
        return DEBUG;
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

    private String getReferrerPackageName() {
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
            ((TextView) this.mProfileView.findViewById(R.id.profile_button)).setText(dri.getDisplayLabel());
            return;
        }
        this.mProfileView.setVisibility(8);
    }

    private void setProfileSwitchMessageId(int contentUserHint) {
        if (contentUserHint != -2 && contentUserHint != UserHandle.myUserId()) {
            boolean isManagedProfile;
            UserManager userManager = (UserManager) getSystemService(ImsConferenceState.USER);
            UserInfo originUserInfo = userManager.getUserInfo(contentUserHint);
            if (originUserInfo != null) {
                isManagedProfile = originUserInfo.isManagedProfile();
            } else {
                isManagedProfile = DEBUG;
            }
            boolean targetIsManaged = userManager.isManagedProfile();
            if (isManagedProfile && !targetIsManaged) {
                this.mProfileSwitchMessageId = R.string.forward_intent_to_owner;
            } else if (!isManagedProfile && targetIsManaged) {
                this.mProfileSwitchMessageId = R.string.forward_intent_to_work;
            }
        }
    }

    public void setSafeForwardingMode(boolean safeForwarding) {
        this.mSafeForwardingMode = safeForwarding;
    }

    protected CharSequence getTitleForAction(String action, int defaultTitleRes) {
        ActionTitle title = this.mResolvingHome ? ActionTitle.HOME : ActionTitle.forAction(action);
        boolean named = this.mAdapter.hasFilteredItem();
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
                dr = getIcon(this.mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon);
                if (dr != null) {
                    return dr;
                }
            }
            int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                dr = getIcon(this.mPm.getResourcesForApplication(ri.activityInfo.packageName), iconRes);
                if (dr != null) {
                    return dr;
                }
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Couldn't find resources for package", e);
        }
        return ri.loadIcon(this.mPm);
    }

    protected void onRestart() {
        super.onRestart();
        if (!this.mRegistered) {
            this.mPackageMonitor.register(this, getMainLooper(), DEBUG);
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
            this.mRegistered = DEBUG;
        }
        if ((getIntent().getFlags() & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0 && !isVoiceInteraction() && !this.mResolvingHome && !isChangingConfigurations()) {
            finish();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations() && this.mPickOptionRequest != null) {
            this.mPickOptionRequest.cancel();
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (this.mAlwaysUseOption) {
            int checkedPos = this.mAdapterView.getCheckedItemPosition();
            boolean hasValidSelection = checkedPos != -1 ? true : DEBUG;
            this.mLastSelected = checkedPos;
            setAlwaysButtonEnabled(hasValidSelection, checkedPos, true);
            this.mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                this.mAdapterView.setSelection(checkedPos);
            }
        }
    }

    private boolean hasManagedProfile() {
        UserManager userManager = (UserManager) getSystemService(ImsConferenceState.USER);
        if (userManager == null) {
            return DEBUG;
        }
        try {
            for (UserInfo userInfo : userManager.getProfiles(getUserId())) {
                if (userInfo != null && userInfo.isManagedProfile()) {
                    return true;
                }
            }
            return DEBUG;
        } catch (SecurityException e) {
            return DEBUG;
        }
    }

    private boolean supportsManagedProfiles(ResolveInfo resolveInfo) {
        boolean z = DEBUG;
        try {
            if (getPackageManager().getApplicationInfo(resolveInfo.activityInfo.packageName, 0).targetSdkVersion >= 21) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return DEBUG;
        }
    }

    private void setAlwaysButtonEnabled(boolean hasValidSelection, int checkedPos, boolean filtered) {
        boolean enabled = DEBUG;
        if (hasValidSelection && this.mAdapter.resolveInfoForPosition(checkedPos, filtered).targetUserId == -2) {
            enabled = true;
        }
        if (this.mAlwaysButton != null) {
            this.mAlwaysButton.setEnabled(enabled);
        }
    }

    public void onButtonClick(View v) {
        startSelected(this.mAlwaysUseOption ? this.mAdapterView.getCheckedItemPosition() : this.mAdapter.getFilteredPosition(), v.getId() == R.id.button_always ? true : DEBUG, this.mAlwaysUseOption);
    }

    public void startSelected(int which, boolean always, boolean filtered) {
        if (!isFinishing()) {
            ResolveInfo ri = this.mAdapter.resolveInfoForPosition(which, filtered);
            if (this.mResolvingHome && hasManagedProfile() && !supportsManagedProfiles(ri)) {
                Toast.makeText((Context) this, String.format(getResources().getString(R.string.activity_resolver_work_profiles_support), new Object[]{ri.activityInfo.loadLabel(getPackageManager()).toString()}), 1).show();
                return;
            }
            if (onTargetSelected(this.mAdapter.targetInfoForPosition(which, filtered), always)) {
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
        if (intent != null && ((this.mAlwaysUseOption || this.mAdapter.hasFilteredItem()) && this.mAdapter.mOrigResolveList != null)) {
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
            if (!(data == null || data.getScheme() == null || (cat2 == 6291456 && ("file".equals(data.getScheme()) || "content".equals(data.getScheme()))))) {
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
                int N = this.mAdapter.mOrigResolveList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                for (int i = 0; i < N; i++) {
                    ResolveInfo r = ((ResolvedComponentInfo) this.mAdapter.mOrigResolveList.get(i)).getResolveInfoAt(0);
                    set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                    if (r.match > bestMatch) {
                        bestMatch = r.match;
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
                        if (dataScheme != null) {
                            if (dataScheme.equals("http")) {
                                isHttpOrHttps = true;
                            } else {
                                isHttpOrHttps = dataScheme.equals("https");
                            }
                        } else {
                            isHttpOrHttps = DEBUG;
                        }
                        boolean equals = action != null ? action.equals("android.intent.action.VIEW") : DEBUG;
                        boolean contains;
                        if (categories != null) {
                            contains = categories.contains("android.intent.category.BROWSABLE");
                        } else {
                            contains = DEBUG;
                        }
                        if (isHttpOrHttps && equals && r23) {
                            pm.updateIntentVerificationStatusAsUser(packageName, 2, userId);
                        }
                    } else if (TextUtils.isEmpty(pm.getDefaultBrowserPackageNameAsUser(userId))) {
                        pm.setDefaultBrowserPackageNameAsUser(ri.activityInfo.packageName, userId);
                    }
                } else {
                    try {
                        AppGlobals.getPackageManager().setLastChosenActivity(intent, intent.resolveType(getContentResolver()), Protocol.BASE_SYSTEM_RESERVED, filter, bestMatch, intent.getComponent());
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
                    launchedFromPackage = ActivityManagerNative.getDefault().getLaunchedFromPackage(getActivityToken());
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
        return DEBUG;
    }

    public boolean shouldAutoLaunchSingleChoice(TargetInfo target) {
        return true;
    }

    public void showTargetDetails(ResolveInfo ri) {
        startActivity(new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS").setData(Uri.fromParts("package", ri.activityInfo.packageName, null)).addFlags(Protocol.BASE_CONNECTIVITY_MANAGER));
    }

    public ResolveListAdapter createAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
        return new ResolveListAdapter(context, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed);
    }

    public boolean configureContentView(List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, boolean alwaysUseOption) {
        int layoutId;
        int i = this.mLaunchedFromUid;
        boolean z = (!alwaysUseOption || isVoiceInteraction()) ? DEBUG : true;
        this.mAdapter = createAdapter(this, payloadIntents, initialIntents, rList, i, z);
        if (this.mAdapter.hasFilteredItem()) {
            layoutId = R.layout.resolver_list_with_default;
            alwaysUseOption = DEBUG;
        } else {
            layoutId = getLayoutResource();
        }
        this.mAlwaysUseOption = alwaysUseOption;
        int count = this.mAdapter.getUnfilteredCount();
        if (count == 1 && this.mAdapter.getOtherProfile() == null) {
            TargetInfo target = this.mAdapter.targetInfoForPosition(0, DEBUG);
            if (shouldAutoLaunchSingleChoice(target)) {
                safelyStartActivity(target);
                this.mPackageMonitor.unregister();
                this.mRegistered = DEBUG;
                finish();
                return true;
            }
        }
        if (count > 0) {
            setContentView(layoutId);
            this.mAdapterView = (AbsListView) findViewById(R.id.resolver_list);
            onPrepareAdapterView(this.mAdapterView, this.mAdapter, alwaysUseOption);
        } else {
            setContentView(R.layout.resolver_list);
            ((TextView) findViewById(R.id.empty)).setVisibility(0);
            this.mAdapterView = (AbsListView) findViewById(R.id.resolver_list);
            this.mAdapterView.setVisibility(8);
        }
        return DEBUG;
    }

    public void onPrepareAdapterView(AbsListView adapterView, ResolveListAdapter adapter, boolean alwaysUseOption) {
        boolean useHeader = adapter.hasFilteredItem();
        ViewGroup viewGroup = adapterView instanceof ListView ? (ListView) adapterView : null;
        adapterView.setAdapter(this.mAdapter);
        ItemClickListener listener = new ItemClickListener(this);
        adapterView.setOnItemClickListener(listener);
        adapterView.setOnItemLongClickListener(listener);
        if (alwaysUseOption) {
            viewGroup.setChoiceMode(1);
        }
        if (useHeader && viewGroup != null) {
            viewGroup.addHeaderView(LayoutInflater.from(this).inflate((int) R.layout.resolver_different_item_header, viewGroup, (boolean) DEBUG));
        }
    }

    static boolean resolveInfoMatch(ResolveInfo lhs, ResolveInfo rhs) {
        if (lhs == null) {
            return rhs == null ? true : DEBUG;
        } else {
            if (lhs.activityInfo != null) {
                return Objects.equals(lhs.activityInfo.name, rhs.activityInfo.name) ? Objects.equals(lhs.activityInfo.packageName, rhs.activityInfo.packageName) : DEBUG;
            } else {
                if (rhs.activityInfo != null) {
                    return DEBUG;
                }
                return true;
            }
        }
    }

    static final boolean isSpecificUriMatch(int match) {
        match &= 268369920;
        if (match < 3145728 || match > 5242880) {
            return DEBUG;
        }
        return true;
    }
}
