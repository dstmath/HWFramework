package com.android.internal.app;

import android.Manifest;
import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.ActivityThread;
import android.app.VoiceInteractor;
import android.content.ComponentName;
import android.content.ContentResolver;
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
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Insets;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PatternMatcher;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.SettingsStringUtil;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.content.PackageMonitor;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.widget.ResolverDrawerLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ResolverActivity extends Activity {
    private static final boolean DEBUG = false;
    private static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
    private static final String EXTRA_SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args";
    private static final String OPEN_LINKS_COMPONENT_KEY = "app_link_state";
    private static final String TAG = "ResolverActivity";
    @UnsupportedAppUsage
    protected ResolveListAdapter mAdapter;
    protected AbsListView mAdapterView;
    private Button mAlwaysButton;
    private int mDefaultTitleResId;
    boolean mEnableChooserDelegate = true;
    private Space mFooterSpacer = null;
    private int mIconDpi;
    private final ArrayList<Intent> mIntents = new ArrayList<>();
    protected boolean mIsClonedProfile;
    private int mLastSelected = -1;
    protected int mLaunchedFromUid;
    private int mLayoutId;
    private Button mOnceButton;
    private final PackageMonitor mPackageMonitor = createPackageMonitor();
    private PickTargetOptionRequest mPickOptionRequest;
    @UnsupportedAppUsage
    protected PackageManager mPm;
    protected PackageManager mPmForParent;
    private Runnable mPostListReadyRunnable;
    private int mProfileSwitchMessageId = -1;
    protected View mProfileView;
    private String mReferrerPackage;
    private boolean mRegistered;
    protected ResolverDrawerLayout mResolverDrawerLayout;
    private boolean mResolvingHome = false;
    private boolean mRetainInOnStop;
    private boolean mSafeForwardingMode;
    private boolean mSupportsAlwaysUseOption;
    private ColorMatrixColorFilter mSuspendedMatrixColorFilter;
    protected Insets mSystemWindowInsets = null;
    private CharSequence mTitle;
    private boolean mUseLayoutForBrowsables;

    public interface TargetInfo {
        TargetInfo cloneFilledIn(Intent intent, int i);

        List<Intent> getAllSourceIntents();

        Drawable getDisplayIcon();

        CharSequence getDisplayLabel();

        CharSequence getExtendedInfo();

        ResolveInfo getResolveInfo();

        ComponentName getResolvedComponentName();

        Intent getResolvedIntent();

        boolean isSuspended();

        boolean start(Activity activity, Bundle bundle);

        boolean startAsCaller(ResolverActivity resolverActivity, Bundle bundle, int i);

        boolean startAsUser(Activity activity, Bundle bundle, UserHandle userHandle);
    }

    public static int getLabelRes(String action) {
        return ActionTitle.forAction(action).labelRes;
    }

    /* access modifiers changed from: private */
    public enum ActionTitle {
        VIEW("android.intent.action.VIEW", R.string.whichViewApplication, R.string.whichViewApplicationNamed, R.string.whichViewApplicationLabel),
        EDIT(Intent.ACTION_EDIT, R.string.whichEditApplication, R.string.whichEditApplicationNamed, R.string.whichEditApplicationLabel),
        SEND(Intent.ACTION_SEND, R.string.whichSendApplication, R.string.whichSendApplicationNamed, R.string.whichSendApplicationLabel),
        SENDTO(Intent.ACTION_SENDTO, R.string.whichSendToApplication, R.string.whichSendToApplicationNamed, R.string.whichSendToApplicationLabel),
        SEND_MULTIPLE(Intent.ACTION_SEND_MULTIPLE, R.string.whichSendApplication, R.string.whichSendApplicationNamed, R.string.whichSendApplicationLabel),
        CAPTURE_IMAGE(MediaStore.ACTION_IMAGE_CAPTURE, R.string.whichImageCaptureApplication, R.string.whichImageCaptureApplicationNamed, R.string.whichImageCaptureApplicationLabel),
        DEFAULT(null, R.string.whichApplication, R.string.whichApplicationNamed, R.string.whichApplicationLabel),
        HOME(Intent.ACTION_MAIN, R.string.whichHomeApplication, R.string.whichHomeApplicationNamed, R.string.whichHomeApplicationLabel);
        
        public static final int BROWSABLE_APP_TITLE_RES = 17041521;
        public static final int BROWSABLE_HOST_APP_TITLE_RES = 17041519;
        public static final int BROWSABLE_HOST_TITLE_RES = 17041518;
        public static final int BROWSABLE_TITLE_RES = 17041520;
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
            ActionTitle[] values = values();
            for (ActionTitle title : values) {
                if (!(title == HOME || action2 == null || !action2.equals(title.action))) {
                    return title;
                }
            }
            return DEFAULT;
        }
    }

    /* access modifiers changed from: protected */
    public PackageMonitor createPackageMonitor() {
        return new PackageMonitor() {
            /* class com.android.internal.app.ResolverActivity.AnonymousClass1 */

            @Override // com.android.internal.content.PackageMonitor
            public void onSomePackagesChanged() {
                ResolverActivity.this.mAdapter.handlePackagesChanged();
                ResolverActivity.this.bindProfileView();
            }

            @Override // com.android.internal.content.PackageMonitor
            public boolean onPackageChanged(String packageName, int uid, String[] components) {
                return true;
            }
        };
    }

    private Intent makeMyIntent() {
        Intent intent = new Intent(getIntent());
        intent.setComponent(null);
        intent.setFlags(intent.getFlags() & -8388609);
        return intent;
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = makeMyIntent();
        Set<String> categories = intent.getCategories();
        if (Intent.ACTION_MAIN.equals(intent.getAction()) && categories != null && categories.size() == 1 && categories.contains(Intent.CATEGORY_HOME)) {
            this.mResolvingHome = true;
        }
        setSafeForwardingMode(true);
        onCreate(savedInstanceState, intent, null, 0, null, null, true);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, Intent[] initialIntents, List<ResolveInfo> rList, boolean supportsAlwaysUseOption) {
        onCreate(savedInstanceState, intent, title, 0, initialIntents, rList, supportsAlwaysUseOption);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState, Intent intent, CharSequence title, int defaultTitleRes, Intent[] initialIntents, List<ResolveInfo> rList, boolean supportsAlwaysUseOption) {
        boolean z;
        int i;
        setTheme(R.style.Theme_DeviceDefault_Resolver);
        super.onCreate(savedInstanceState);
        setProfileSwitchMessageId(intent.getContentUserHint());
        try {
            this.mLaunchedFromUid = ActivityTaskManager.getService().getLaunchedFromUid(getActivityToken());
        } catch (RemoteException e) {
            this.mLaunchedFromUid = -1;
        }
        int i2 = this.mLaunchedFromUid;
        if (i2 < 0 || UserHandle.isIsolated(i2)) {
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
        this.mIconDpi = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getLauncherLargeIconDensity();
        this.mIntents.add(0, new Intent(intent));
        this.mTitle = title;
        this.mDefaultTitleResId = defaultTitleRes;
        if (getTargetIntent() == null) {
            z = false;
        } else {
            z = isHttpSchemeAndViewAction(getTargetIntent());
        }
        this.mUseLayoutForBrowsables = z;
        this.mSupportsAlwaysUseOption = supportsAlwaysUseOption;
        if (!configureContentView(this.mIntents, initialIntents, rList)) {
            ResolverDrawerLayout rdl = (ResolverDrawerLayout) findViewById(R.id.contentPanel);
            if (rdl != null) {
                rdl.setOnDismissedListener(new ResolverDrawerLayout.OnDismissedListener() {
                    /* class com.android.internal.app.ResolverActivity.AnonymousClass2 */

                    @Override // com.android.internal.widget.ResolverDrawerLayout.OnDismissedListener
                    public void onDismissed() {
                        ResolverActivity.this.finish();
                    }
                });
                if (isVoiceInteraction()) {
                    rdl.setCollapsed(false);
                }
                rdl.setSystemUiVisibility(768);
                rdl.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    /* class com.android.internal.app.$$Lambda$yRChrJGmMwuDQFgBsC_mE_wmc */

                    @Override // android.view.View.OnApplyWindowInsetsListener
                    public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                        return ResolverActivity.this.onApplyWindowInsets(view, windowInsets);
                    }
                });
                this.mResolverDrawerLayout = rdl;
            }
            this.mProfileView = findViewById(R.id.profile_button);
            View view = this.mProfileView;
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    /* class com.android.internal.app.$$Lambda$fPZctSH683BQhFNSBKdl6Wz99qg */

                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        ResolverActivity.this.onProfileClick(view);
                    }
                });
                bindProfileView();
            }
            initSuspendedColorMatrix();
            if (isVoiceInteraction()) {
                onSetupVoiceInteraction();
            }
            Set<String> categories = intent.getCategories();
            if (this.mAdapter.hasFilteredItem()) {
                i = 451;
            } else {
                i = 453;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(intent.getAction());
            sb.append(SettingsStringUtil.DELIMITER);
            sb.append(intent.getType());
            sb.append(SettingsStringUtil.DELIMITER);
            sb.append(categories != null ? Arrays.toString(categories.toArray()) : "");
            MetricsLogger.action(this, i, sb.toString());
        }
    }

    /* access modifiers changed from: protected */
    public void onProfileClick(View v) {
        DisplayResolveInfo dri = this.mAdapter.getOtherProfile();
        if (dri != null) {
            this.mProfileSwitchMessageId = -1;
            onTargetSelected(dri, false);
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        this.mSystemWindowInsets = insets.getSystemWindowInsets();
        this.mResolverDrawerLayout.setPadding(this.mSystemWindowInsets.left, this.mSystemWindowInsets.top, this.mSystemWindowInsets.right, 0);
        View emptyView = findViewById(16908292);
        if (emptyView != null) {
            emptyView.setPadding(0, 0, 0, this.mSystemWindowInsets.bottom + (getResources().getDimensionPixelSize(R.dimen.chooser_edge_margin_normal) * 2));
        }
        Space space = this.mFooterSpacer;
        if (space == null) {
            this.mFooterSpacer = new Space(getApplicationContext());
        } else {
            ((ListView) this.mAdapterView).removeFooterView(space);
        }
        this.mFooterSpacer.setLayoutParams(new AbsListView.LayoutParams(-1, this.mSystemWindowInsets.bottom));
        ((ListView) this.mAdapterView).addFooterView(this.mFooterSpacer);
        resetButtonBar();
        return insets.consumeSystemWindowInsets();
    }

    @Override // android.app.Activity, android.content.ComponentCallbacks
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mAdapter.handlePackagesChanged();
        Insets insets = this.mSystemWindowInsets;
        if (insets != null) {
            this.mResolverDrawerLayout.setPadding(insets.left, this.mSystemWindowInsets.top, this.mSystemWindowInsets.right, 0);
        }
    }

    private void initSuspendedColorMatrix() {
        ColorMatrix tempBrightnessMatrix = new ColorMatrix();
        float[] mat = tempBrightnessMatrix.getArray();
        mat[0] = 0.5f;
        mat[6] = 0.5f;
        mat[12] = 0.5f;
        mat[4] = (float) 127;
        mat[9] = (float) 127;
        mat[14] = (float) 127;
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.0f);
        matrix.preConcat(tempBrightnessMatrix);
        this.mSuspendedMatrixColorFilter = new ColorMatrixColorFilter(matrix);
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
        return R.layout.resolver_list;
    }

    /* access modifiers changed from: protected */
    public void bindProfileView() {
        if (this.mProfileView != null) {
            DisplayResolveInfo dri = this.mAdapter.getOtherProfile();
            if (dri != null) {
                this.mProfileView.setVisibility(0);
                View text = this.mProfileView.findViewById(R.id.profile_button);
                if (!(text instanceof TextView)) {
                    text = this.mProfileView.findViewById(16908308);
                }
                ((TextView) text).setText(dri.getDisplayLabel());
                return;
            }
            this.mProfileView.setVisibility(8);
        }
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
                this.mProfileSwitchMessageId = R.string.forward_intent_to_owner;
            } else if (!originIsManaged && targetIsManaged) {
                this.mProfileSwitchMessageId = R.string.forward_intent_to_work;
            }
        }
    }

    public void setSafeForwardingMode(boolean safeForwarding) {
        this.mSafeForwardingMode = safeForwarding;
    }

    /* access modifiers changed from: protected */
    public CharSequence getTitleForAction(Intent intent, int defaultTitleRes) {
        ActionTitle title;
        if (this.mResolvingHome) {
            title = ActionTitle.HOME;
        } else {
            title = ActionTitle.forAction(intent.getAction());
        }
        boolean named = this.mAdapter.getFilteredPosition() >= 0;
        if (title == ActionTitle.DEFAULT && defaultTitleRes != 0) {
            return getString(defaultTitleRes);
        }
        if (isHttpSchemeAndViewAction(intent)) {
            if (named && !this.mUseLayoutForBrowsables) {
                return getString(17041521, this.mAdapter.getFilteredItem().getDisplayLabel());
            }
            if (named && this.mUseLayoutForBrowsables) {
                return getString(17041519, intent.getData().getHost(), this.mAdapter.getFilteredItem().getDisplayLabel());
            }
            if (this.mAdapter.areAllTargetsBrowsers()) {
                return getString(17041520);
            }
            return getString(17041518, intent.getData().getHost());
        } else if (named) {
            return getString(title.namedTitleRes, this.mAdapter.getFilteredItem().getDisplayLabel());
        } else {
            return getString(title.titleRes);
        }
    }

    /* access modifiers changed from: package-private */
    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }

    /* access modifiers changed from: private */
    public static abstract class TargetPresentationGetter {
        private final ApplicationInfo mAi;
        private Context mCtx;
        private final boolean mHasSubstitutePermission;
        private final int mIconDpi;
        protected boolean mIsClonedProfileInner;
        protected PackageManager mPm;
        protected PackageManager mPmForParentInner;

        /* access modifiers changed from: package-private */
        public abstract String getAppSubLabelInternal();

        /* access modifiers changed from: package-private */
        public abstract Drawable getIconSubstituteInternal();

        TargetPresentationGetter(Context ctx, int iconDpi, ApplicationInfo ai) {
            this.mCtx = ctx;
            this.mPm = ctx.getPackageManager();
            this.mAi = ai;
            this.mIconDpi = iconDpi;
            this.mHasSubstitutePermission = this.mPm.checkPermission(Manifest.permission.SUBSTITUTE_SHARE_TARGET_APP_NAME_AND_ICON, this.mAi.packageName) == 0;
            if (this.mCtx.getUserId() != 0) {
                UserInfo ui = ((UserManager) this.mCtx.getSystemService("user")).getUserInfo(this.mCtx.getUserId());
                this.mIsClonedProfileInner = ui.isClonedProfile();
                if (this.mIsClonedProfileInner) {
                    try {
                        this.mPmForParentInner = this.mCtx.createPackageContextAsUser(this.mCtx.getPackageName(), 0, new UserHandle(ui.profileGroupId)).getPackageManager();
                    } catch (PackageManager.NameNotFoundException e) {
                        this.mPmForParentInner = this.mCtx.getPackageManager();
                    }
                }
            }
        }

        public Drawable getIcon(UserHandle userHandle) {
            return new BitmapDrawable(this.mCtx.getResources(), getIconBitmap(userHandle));
        }

        public Bitmap getIconBitmap(UserHandle userHandle) {
            Drawable dr = null;
            if (this.mHasSubstitutePermission) {
                dr = getIconSubstituteInternal();
            }
            if (dr == null) {
                try {
                    if (this.mAi.icon != 0) {
                        dr = loadIconFromResource((this.mIsClonedProfileInner ? this.mPmForParentInner : this.mPm).getResourcesForApplication(this.mAi), this.mAi.icon);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
            if (dr == null) {
                dr = this.mAi.loadIcon(this.mIsClonedProfileInner ? this.mPmForParentInner : this.mPm);
            }
            SimpleIconFactory sif = SimpleIconFactory.obtain(this.mCtx);
            Bitmap icon = sif.createUserBadgedIconBitmap(dr, this.mIsClonedProfileInner ? new UserHandle(this.mPmForParentInner.getUserId()) : userHandle);
            sif.recycle();
            return icon;
        }

        public String getLabel() {
            String label = null;
            if (this.mHasSubstitutePermission) {
                label = getAppSubLabelInternal();
            }
            if (label == null) {
                return (String) this.mAi.loadLabel(this.mPm);
            }
            return label;
        }

        public String getSubLabel() {
            if (this.mHasSubstitutePermission) {
                return null;
            }
            return getAppSubLabelInternal();
        }

        /* access modifiers changed from: protected */
        public String loadLabelFromResource(Resources res, int resId) {
            return res.getString(resId);
        }

        /* access modifiers changed from: protected */
        public Drawable loadIconFromResource(Resources res, int resId) {
            return res.getDrawableForDensity(resId, this.mIconDpi);
        }
    }

    @VisibleForTesting
    public static class ResolveInfoPresentationGetter extends ActivityInfoPresentationGetter {
        private final ResolveInfo mRi;

        public ResolveInfoPresentationGetter(Context ctx, int iconDpi, ResolveInfo ri) {
            super(ctx, iconDpi, ri.activityInfo);
            this.mRi = ri;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.internal.app.ResolverActivity.ActivityInfoPresentationGetter, com.android.internal.app.ResolverActivity.TargetPresentationGetter
        public Drawable getIconSubstituteInternal() {
            Drawable dr = null;
            try {
                if (!(this.mRi.resolvePackageName == null || this.mRi.icon == 0)) {
                    dr = loadIconFromResource((this.mIsClonedProfileInner ? this.mPmForParentInner : this.mPm).getResourcesForApplication(this.mRi.resolvePackageName), this.mRi.icon);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(ResolverActivity.TAG, "SUBSTITUTE_SHARE_TARGET_APP_NAME_AND_ICON permission granted but couldn't find resources for package", e);
            }
            if (dr == null) {
                return super.getIconSubstituteInternal();
            }
            return dr;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.internal.app.ResolverActivity.ActivityInfoPresentationGetter, com.android.internal.app.ResolverActivity.TargetPresentationGetter
        public String getAppSubLabelInternal() {
            return (String) this.mRi.loadLabel(this.mPm);
        }
    }

    /* access modifiers changed from: package-private */
    public ResolveInfoPresentationGetter makePresentationGetter(ResolveInfo ri) {
        return new ResolveInfoPresentationGetter(this, this.mIconDpi, ri);
    }

    @VisibleForTesting
    public static class ActivityInfoPresentationGetter extends TargetPresentationGetter {
        private final ActivityInfo mActivityInfo;

        @Override // com.android.internal.app.ResolverActivity.TargetPresentationGetter
        public /* bridge */ /* synthetic */ Drawable getIcon(UserHandle userHandle) {
            return super.getIcon(userHandle);
        }

        @Override // com.android.internal.app.ResolverActivity.TargetPresentationGetter
        public /* bridge */ /* synthetic */ Bitmap getIconBitmap(UserHandle userHandle) {
            return super.getIconBitmap(userHandle);
        }

        @Override // com.android.internal.app.ResolverActivity.TargetPresentationGetter
        public /* bridge */ /* synthetic */ String getLabel() {
            return super.getLabel();
        }

        @Override // com.android.internal.app.ResolverActivity.TargetPresentationGetter
        public /* bridge */ /* synthetic */ String getSubLabel() {
            return super.getSubLabel();
        }

        public ActivityInfoPresentationGetter(Context ctx, int iconDpi, ActivityInfo activityInfo) {
            super(ctx, iconDpi, activityInfo.applicationInfo);
            this.mActivityInfo = activityInfo;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.internal.app.ResolverActivity.TargetPresentationGetter
        public Drawable getIconSubstituteInternal() {
            try {
                if (this.mActivityInfo.icon == 0) {
                    return null;
                }
                return loadIconFromResource((this.mIsClonedProfileInner ? this.mPmForParentInner : this.mPm).getResourcesForApplication(this.mActivityInfo.applicationInfo), this.mActivityInfo.icon);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(ResolverActivity.TAG, "SUBSTITUTE_SHARE_TARGET_APP_NAME_AND_ICON permission granted but couldn't find resources for package", e);
                return null;
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.internal.app.ResolverActivity.TargetPresentationGetter
        public String getAppSubLabelInternal() {
            return (String) this.mActivityInfo.loadLabel(this.mPm);
        }
    }

    /* access modifiers changed from: protected */
    public ActivityInfoPresentationGetter makePresentationGetter(ActivityInfo ai) {
        return new ActivityInfoPresentationGetter(this, this.mIconDpi, ai);
    }

    /* access modifiers changed from: package-private */
    public Drawable loadIconForResolveInfo(ResolveInfo ri) {
        return makePresentationGetter(ri).getIcon(Process.myUserHandle());
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onRestart() {
        super.onRestart();
        if (!this.mRegistered) {
            this.mPackageMonitor.register(this, getMainLooper(), false);
            this.mRegistered = true;
        }
        this.mAdapter.handlePackagesChanged();
        bindProfileView();
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
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
    @Override // android.app.Activity
    public void onDestroy() {
        PickTargetOptionRequest pickTargetOptionRequest;
        super.onDestroy();
        if (!isChangingConfigurations() && (pickTargetOptionRequest = this.mPickOptionRequest) != null) {
            pickTargetOptionRequest.cancel();
        }
        if (this.mPostListReadyRunnable != null) {
            getMainThreadHandler().removeCallbacks(this.mPostListReadyRunnable);
            this.mPostListReadyRunnable = null;
        }
        ResolveListAdapter resolveListAdapter = this.mAdapter;
        if (resolveListAdapter != null && resolveListAdapter.mResolverListController != null) {
            this.mAdapter.mResolverListController.destroy();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        resetButtonBar();
    }

    private boolean isHttpSchemeAndViewAction(Intent intent) {
        return (IntentFilter.SCHEME_HTTP.equals(intent.getScheme()) || IntentFilter.SCHEME_HTTPS.equals(intent.getScheme())) && "android.intent.action.VIEW".equals(intent.getAction());
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
        try {
            if (getPackageManager().getApplicationInfo(resolveInfo.activityInfo.packageName, this.mIsClonedProfile ? PackageManager.MATCH_KNOWN_PACKAGES : 0).targetSdkVersion >= 21) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
                if (!this.mUseLayoutForBrowsables || ri.handleAllWebDataURI) {
                    this.mAlwaysButton.setText(getResources().getString(R.string.activity_resolver_use_always));
                } else {
                    this.mAlwaysButton.setText(getResources().getString(R.string.activity_resolver_set_always));
                }
            }
        }
        Button button = this.mAlwaysButton;
        if (button != null) {
            button.setEnabled(enabled);
        }
    }

    public void onButtonClick(View v) {
        int which;
        int id = v.getId();
        if (this.mAdapter.hasFilteredItem()) {
            which = this.mAdapter.getFilteredPosition();
        } else {
            which = this.mAdapterView.getCheckedItemPosition();
        }
        boolean z = true;
        boolean hasIndexBeenFiltered = !this.mAdapter.hasFilteredItem();
        ResolveInfo ri = this.mAdapter.resolveInfoForPosition(which, hasIndexBeenFiltered);
        if (!this.mUseLayoutForBrowsables || ri.handleAllWebDataURI || id != 16945154) {
            if (id != 16945154) {
                z = false;
            }
            startSelected(which, z, hasIndexBeenFiltered);
            return;
        }
        showSettingsForSelected(ri);
    }

    private void showSettingsForSelected(ResolveInfo ri) {
        Intent intent = new Intent();
        String packageName = ri.activityInfo.packageName;
        Bundle showFragmentArgs = new Bundle();
        showFragmentArgs.putString(EXTRA_FRAGMENT_ARG_KEY, OPEN_LINKS_COMPONENT_KEY);
        showFragmentArgs.putString("package", packageName);
        intent.setAction(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS).setData(Uri.fromParts("package", packageName, null)).addFlags(524288).putExtra(EXTRA_FRAGMENT_ARG_KEY, OPEN_LINKS_COMPONENT_KEY).putExtra(EXTRA_SHOW_FRAGMENT_ARGS, showFragmentArgs);
        startActivity(intent);
    }

    public void startSelected(int which, boolean always, boolean hasIndexBeenFiltered) {
        int i;
        if (!isFinishing()) {
            ResolveInfo ri = this.mAdapter.resolveInfoForPosition(which, hasIndexBeenFiltered);
            if (!this.mResolvingHome || !hasManagedProfile() || supportsManagedProfiles(ri)) {
                TargetInfo target = this.mAdapter.targetInfoForPosition(which, hasIndexBeenFiltered);
                if (target != null && onTargetSelected(target, always)) {
                    if (always && this.mSupportsAlwaysUseOption) {
                        MetricsLogger.action(this, 455);
                    } else if (this.mSupportsAlwaysUseOption) {
                        MetricsLogger.action(this, 456);
                    } else {
                        MetricsLogger.action(this, 457);
                    }
                    if (this.mAdapter.hasFilteredItem()) {
                        i = 452;
                    } else {
                        i = 454;
                    }
                    MetricsLogger.action(this, i);
                    finish();
                    return;
                }
                return;
            }
            Toast.makeText(this, String.format(getResources().getString(R.string.activity_resolver_work_profiles_support), ri.activityInfo.loadLabel(getPackageManager()).toString()), 1).show();
        }
    }

    public Intent getReplacementIntent(ActivityInfo aInfo, Intent defIntent) {
        return defIntent;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0209  */
    /* JADX WARNING: Removed duplicated region for block: B:111:0x0215  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x021a  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0226  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x022b A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x0239  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x0274 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:134:0x0276 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01b3  */
    public boolean onTargetSelected(TargetInfo target, boolean alwaysCheck) {
        Intent filterIntent;
        ComponentName[] set;
        int otherProfileMatch;
        boolean isHttpOrHttps;
        boolean isViewAction;
        boolean hasCategoryBrowsable;
        String mimeType;
        ResolveInfo ri = target.getResolveInfo();
        Intent intent = target.getResolvedIntent();
        if (intent != null) {
            if (!this.mSupportsAlwaysUseOption && !this.mAdapter.hasFilteredItem()) {
                safelyStartActivity(target);
                if (target.isSuspended()) {
                }
            } else if (this.mAdapter.mUnfilteredResolveList != null) {
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
                filter.addCategory(Intent.CATEGORY_DEFAULT);
                int cat2 = 268369920 & ri.match;
                Uri data = filterIntent.getData();
                String str = TAG;
                if (cat2 == 6291456 && (mimeType = filterIntent.resolveType(this)) != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        Log.w(str, e);
                        filter = null;
                    }
                }
                if (data != null && data.getScheme() != null && (cat2 != 6291456 || (!ContentResolver.SCHEME_FILE.equals(data.getScheme()) && !"content".equals(data.getScheme())))) {
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
                                filter.addDataAuthority(a.getHost(), port >= 0 ? Integer.toString(port) : null);
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
                    int bestMatch = 0;
                    int i = 0;
                    while (i < N) {
                        ResolveInfo r = this.mAdapter.mUnfilteredResolveList.get(i).getResolveInfoAt(0);
                        set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
                        if (r.match > bestMatch) {
                            bestMatch = r.match;
                        }
                        i++;
                        filterIntent = filterIntent;
                        cat2 = cat2;
                        str = str;
                    }
                    if (needToAddBackProfileForwardingComponent) {
                        set[N] = this.mAdapter.mOtherProfile.getResolvedComponentName();
                        otherProfileMatch = this.mAdapter.mOtherProfile.getResolveInfo().match;
                        if (otherProfileMatch > bestMatch) {
                            if (!alwaysCheck) {
                                int userId = getUserId();
                                PackageManager pm = getPackageManager();
                                pm.addPreferredActivity(filter, otherProfileMatch, set, intent.getComponent());
                                if (!ri.handleAllWebDataURI) {
                                    String packageName = intent.getComponent().getPackageName();
                                    String dataScheme = data != null ? data.getScheme() : null;
                                    if (dataScheme != null) {
                                        if (dataScheme.equals(IntentFilter.SCHEME_HTTP) || dataScheme.equals(IntentFilter.SCHEME_HTTPS)) {
                                            isHttpOrHttps = true;
                                            if (action == null) {
                                                if (action.equals("android.intent.action.VIEW")) {
                                                    isViewAction = true;
                                                    if (categories != null) {
                                                        if (categories.contains(Intent.CATEGORY_BROWSABLE)) {
                                                            hasCategoryBrowsable = true;
                                                            if (!isHttpOrHttps && isViewAction && hasCategoryBrowsable) {
                                                                pm.updateIntentVerificationStatusAsUser(packageName, 2, userId);
                                                            }
                                                        }
                                                    }
                                                    hasCategoryBrowsable = false;
                                                    if (!isHttpOrHttps) {
                                                    }
                                                }
                                            }
                                            isViewAction = false;
                                            if (categories != null) {
                                            }
                                            hasCategoryBrowsable = false;
                                            if (!isHttpOrHttps) {
                                            }
                                        }
                                    }
                                    isHttpOrHttps = false;
                                    if (action == null) {
                                    }
                                    isViewAction = false;
                                    if (categories != null) {
                                    }
                                    hasCategoryBrowsable = false;
                                    if (!isHttpOrHttps) {
                                    }
                                } else if (TextUtils.isEmpty(pm.getDefaultBrowserPackageNameAsUser(userId))) {
                                    pm.setDefaultBrowserPackageNameAsUser(ri.activityInfo.packageName, userId);
                                }
                            } else {
                                try {
                                    this.mAdapter.mResolverListController.setLastChosen(intent, filter, otherProfileMatch);
                                } catch (RemoteException re) {
                                    Log.d(str, "Error calling setLastChosenActivity\n" + re);
                                }
                            }
                        }
                    }
                    otherProfileMatch = bestMatch;
                    if (!alwaysCheck) {
                    }
                }
                safelyStartActivity(target);
                if (target.isSuspended()) {
                    return false;
                }
                return true;
            }
        }
        safelyStartActivity(target);
        if (target.isSuspended()) {
        }
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
        int i = this.mProfileSwitchMessageId;
        if (i != -1) {
            Toast.makeText(this, getString(i), 1).show();
        }
        if (this.mSafeForwardingMode) {
            try {
                if (cti.startAsCaller(this, null, -10000)) {
                    onActivityStarted(cti);
                }
            } catch (RuntimeException e) {
                try {
                    launchedFromPackage = ActivityTaskManager.getService().getLaunchedFromPackage(getActivityToken());
                } catch (RemoteException e2) {
                    launchedFromPackage = "??";
                }
                Slog.wtf(TAG, "Unable to launch as uid " + this.mLaunchedFromUid + " package " + launchedFromPackage + ", while running in " + ActivityThread.currentProcessName(), e);
            }
        } else if (cti.start(this, null)) {
            onActivityStarted(cti);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean startAsCallerImpl(Intent intent, Bundle options, boolean ignoreTargetSecurity, int userId) {
        try {
            IBinder permissionToken = ActivityTaskManager.getService().requestStartActivityPermissionToken(getActivityToken());
            Intent chooserIntent = new Intent();
            ComponentName delegateActivity = ComponentName.unflattenFromString(Resources.getSystem().getString(R.string.config_chooserActivity));
            chooserIntent.setClassName(delegateActivity.getPackageName(), delegateActivity.getClassName());
            chooserIntent.putExtra(ActivityTaskManager.EXTRA_PERMISSION_TOKEN, permissionToken);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, intent);
            chooserIntent.putExtra(ActivityTaskManager.EXTRA_OPTIONS, options);
            chooserIntent.putExtra(ActivityTaskManager.EXTRA_IGNORE_TARGET_SECURITY, ignoreTargetSecurity);
            chooserIntent.putExtra(Intent.EXTRA_USER_ID, userId);
            chooserIntent.addFlags(View.SCROLLBARS_OUTSIDE_INSET);
            startActivity(chooserIntent);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
            return true;
        }
    }

    public void onActivityStarted(TargetInfo cti) {
    }

    public boolean shouldGetActivityMetadata() {
        return false;
    }

    public boolean shouldAutoLaunchSingleChoice(TargetInfo target) {
        return !target.isSuspended();
    }

    public void showTargetDetails(ResolveInfo ri) {
        startActivity(new Intent().setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", ri.activityInfo.packageName, null)).addFlags(524288));
    }

    public ResolveListAdapter createAdapter(Context context, List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid, boolean filterLastUsed) {
        return new ResolveListAdapter(context, payloadIntents, initialIntents, rList, launchedFromUid, filterLastUsed, createListController());
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public ResolverListController createListController() {
        return new ResolverListController(this, this.mPm, getTargetIntent(), getReferrerPackageName(), this.mLaunchedFromUid);
    }

    public boolean configureContentView(List<Intent> payloadIntents, Intent[] initialIntents, List<ResolveInfo> rList) {
        this.mAdapter = createAdapter(this, payloadIntents, initialIntents, rList, this.mLaunchedFromUid, this.mSupportsAlwaysUseOption && !isVoiceInteraction());
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
        if (this.mSupportsAlwaysUseOption || this.mUseLayoutForBrowsables) {
            listView.setChoiceMode(1);
        }
        if (useHeader && listView != null && listView.getHeaderViewsCount() == 0) {
            listView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.resolver_different_item_header, (ViewGroup) listView, false));
        }
    }

    public void setHeader() {
        TextView titleView;
        if (this.mAdapter.getCount() == 0 && this.mAdapter.mPlaceholderCount == 0 && (titleView = (TextView) findViewById(16908310)) != null) {
            titleView.setVisibility(8);
        }
        CharSequence title = this.mTitle;
        if (title == null) {
            title = getTitleForAction(getTargetIntent(), this.mDefaultTitleResId);
        }
        if (!TextUtils.isEmpty(title)) {
            TextView titleView2 = (TextView) findViewById(16908310);
            if (titleView2 != null) {
                titleView2.setText(title);
            }
            setTitle(title);
        }
        ImageView iconView = (ImageView) findViewById(16908294);
        DisplayResolveInfo iconInfo = this.mAdapter.getFilteredItem();
        if (iconView != null && iconInfo != null) {
            new LoadIconTask(iconInfo, iconView).execute(new Void[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetButtonBar() {
        if (this.mSupportsAlwaysUseOption || this.mUseLayoutForBrowsables) {
            ViewGroup buttonLayout = (ViewGroup) findViewById(16945153);
            if (buttonLayout != null) {
                int inset = 0;
                buttonLayout.setVisibility(0);
                Insets insets = this.mSystemWindowInsets;
                if (insets != null) {
                    inset = insets.bottom;
                }
                buttonLayout.setPadding(buttonLayout.getPaddingLeft(), buttonLayout.getPaddingTop(), buttonLayout.getPaddingRight(), getResources().getDimensionPixelSize(R.dimen.resolver_button_bar_spacing) + inset);
                this.mOnceButton = (Button) buttonLayout.findViewById(16945155);
                this.mAlwaysButton = (Button) buttonLayout.findViewById(16945154);
                resetAlwaysOrOnceButtonBar();
                return;
            }
            Log.e(TAG, "Layout unexpectedly does not have a button bar");
        }
    }

    private void resetAlwaysOrOnceButtonBar() {
        if (!useLayoutWithDefault() || this.mAdapter.getFilteredPosition() == -1) {
            AbsListView absListView = this.mAdapterView;
            if (absListView != null && absListView.getCheckedItemPosition() != -1) {
                setAlwaysButtonEnabled(true, this.mAdapterView.getCheckedItemPosition(), true);
                this.mOnceButton.setEnabled(true);
                return;
            }
            return;
        }
        setAlwaysButtonEnabled(true, this.mAdapter.getFilteredPosition(), false);
        this.mOnceButton.setEnabled(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean useLayoutWithDefault() {
        return this.mSupportsAlwaysUseOption && this.mAdapter.hasFilteredItem();
    }

    /* access modifiers changed from: protected */
    public void setRetainInOnStop(boolean retainInOnStop) {
        this.mRetainInOnStop = retainInOnStop;
    }

    static boolean resolveInfoMatch(ResolveInfo lhs, ResolveInfo rhs) {
        return lhs == null ? rhs == null : lhs.activityInfo == null ? rhs.activityInfo == null : Objects.equals(lhs.activityInfo.name, rhs.activityInfo.name) && Objects.equals(lhs.activityInfo.packageName, rhs.activityInfo.packageName);
    }

    public final class DisplayResolveInfo implements TargetInfo {
        private Drawable mBadge;
        private Drawable mDisplayIcon;
        private final CharSequence mDisplayLabel;
        private final CharSequence mExtendedInfo;
        private boolean mIsSuspended;
        private final ResolveInfo mResolveInfo;
        private final Intent mResolvedIntent;
        private final List<Intent> mSourceIntents = new ArrayList();

        public DisplayResolveInfo(Intent originalIntent, ResolveInfo pri, CharSequence pLabel, CharSequence pInfo, Intent pOrigIntent) {
            Intent intent;
            this.mSourceIntents.add(originalIntent);
            this.mResolveInfo = pri;
            this.mDisplayLabel = pLabel;
            this.mExtendedInfo = pInfo;
            if (pOrigIntent != null) {
                intent = pOrigIntent;
            } else {
                intent = ResolverActivity.this.getReplacementIntent(pri.activityInfo, ResolverActivity.this.getTargetIntent());
            }
            Intent intent2 = new Intent(intent);
            intent2.addFlags(View.SCROLLBARS_OUTSIDE_INSET);
            ActivityInfo ai = this.mResolveInfo.activityInfo;
            intent2.setComponent(new ComponentName(ai.applicationInfo.packageName, ai.name));
            this.mIsSuspended = (ai.applicationInfo.flags & 1073741824) != 0;
            this.mResolvedIntent = intent2;
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
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public ResolveInfo getResolveInfo() {
            return this.mResolveInfo;
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public CharSequence getDisplayLabel() {
            return this.mDisplayLabel;
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public Drawable getDisplayIcon() {
            return this.mDisplayIcon;
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public TargetInfo cloneFilledIn(Intent fillInIntent, int flags) {
            return new DisplayResolveInfo(this, fillInIntent, flags);
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
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

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public CharSequence getExtendedInfo() {
            return this.mExtendedInfo;
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public Intent getResolvedIntent() {
            return this.mResolvedIntent;
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public ComponentName getResolvedComponentName() {
            return new ComponentName(this.mResolveInfo.activityInfo.packageName, this.mResolveInfo.activityInfo.name);
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public boolean start(Activity activity, Bundle options) {
            activity.startActivity(this.mResolvedIntent, options);
            return true;
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public boolean startAsCaller(ResolverActivity activity, Bundle options, int userId) {
            if (ResolverActivity.this.mEnableChooserDelegate) {
                return activity.startAsCallerImpl(this.mResolvedIntent, options, false, userId);
            }
            activity.startActivityAsCaller(this.mResolvedIntent, options, null, false, userId);
            return true;
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public boolean startAsUser(Activity activity, Bundle options, UserHandle user) {
            activity.startActivityAsUser(this.mResolvedIntent, options, user);
            return false;
        }

        @Override // com.android.internal.app.ResolverActivity.TargetInfo
        public boolean isSuspended() {
            return this.mIsSuspended;
        }
    }

    /* access modifiers changed from: package-private */
    public List<DisplayResolveInfo> getDisplayList() {
        return this.mAdapter.mDisplayList;
    }

    public class ResolveListAdapter extends BaseAdapter {
        private boolean mAllTargetsAreBrowsers = false;
        private final List<ResolveInfo> mBaseResolveList;
        List<DisplayResolveInfo> mDisplayList;
        private boolean mFilterLastUsed;
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
            int i;
            if (!this.mFilterLastUsed || (i = this.mLastChosenPosition) < 0) {
                return null;
            }
            return this.mDisplayList.get(i);
        }

        public DisplayResolveInfo getOtherProfile() {
            return this.mOtherProfile;
        }

        public int getFilteredPosition() {
            int i;
            if (!this.mFilterLastUsed || (i = this.mLastChosenPosition) < 0) {
                return -1;
            }
            return i;
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

        public boolean areAllTargetsBrowsers() {
            return this.mAllTargetsAreBrowsers;
        }

        /* access modifiers changed from: protected */
        public boolean rebuildList() {
            List<ResolvedComponentInfo> currentResolveList;
            this.mOtherProfile = null;
            this.mLastChosen = null;
            this.mLastChosenPosition = -1;
            this.mAllTargetsAreBrowsers = false;
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
            if (currentResolveList.size() > 0) {
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
                        /* class com.android.internal.app.ResolverActivity.ResolveListAdapter.AnonymousClass1 */

                        /* access modifiers changed from: protected */
                        public List<ResolvedComponentInfo> doInBackground(List<ResolvedComponentInfo>... params) {
                            ResolveListAdapter.this.mResolverListController.sort(params[0]);
                            return params[0];
                        }

                        /* access modifiers changed from: protected */
                        public void onPostExecute(List<ResolvedComponentInfo> sortedComponents) {
                            ResolveListAdapter.this.processSortedList(sortedComponents);
                            ResolverActivity.this.bindProfileView();
                            ResolveListAdapter.this.notifyDataSetChanged();
                        }
                    }.execute(currentResolveList);
                    postListReadyRunnable();
                    return false;
                }
                processSortedList(currentResolveList);
                return true;
            }
            processSortedList(currentResolveList);
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void processSortedList(List<ResolvedComponentInfo> sortedComponents) {
            if (!(sortedComponents == null || sortedComponents.size() == 0)) {
                this.mAllTargetsAreBrowsers = ResolverActivity.this.mUseLayoutForBrowsables;
                if (this.mInitialIntents != null) {
                    int i = 0;
                    while (true) {
                        Intent[] intentArr = this.mInitialIntents;
                        if (i >= intentArr.length) {
                            break;
                        }
                        Intent ii = intentArr[i];
                        if (ii != null) {
                            ActivityInfo ai = ii.resolveActivityInfo(ResolverActivity.this.getPackageManager(), ResolverActivity.this.mIsClonedProfile ? PackageManager.MATCH_KNOWN_PACKAGES : 0);
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
                                    ri.noResourceId = true;
                                    ri.icon = 0;
                                }
                                this.mAllTargetsAreBrowsers &= ri.handleAllWebDataURI;
                                ResolverActivity resolverActivity = ResolverActivity.this;
                                addResolveInfo(new DisplayResolveInfo(ii, ri, ri.loadLabel(resolverActivity.getPackageManager()), null, ii));
                            }
                        }
                        i++;
                    }
                }
                for (ResolvedComponentInfo rci : sortedComponents) {
                    ResolveInfo ri2 = rci.getResolveInfoAt(0);
                    if (ri2 != null) {
                        this.mAllTargetsAreBrowsers &= ri2.handleAllWebDataURI;
                        ResolveInfoPresentationGetter pg = ResolverActivity.this.makePresentationGetter(ri2);
                        addResolveInfoWithAlternates(rci, pg.getSubLabel(), pg.getLabel());
                    }
                }
            }
            postListReadyRunnable();
        }

        private void postListReadyRunnable() {
            if (ResolverActivity.this.mPostListReadyRunnable == null) {
                ResolverActivity.this.mPostListReadyRunnable = new Runnable() {
                    /* class com.android.internal.app.ResolverActivity.ResolveListAdapter.AnonymousClass2 */

                    @Override // java.lang.Runnable
                    public void run() {
                        ResolverActivity.this.setHeader();
                        ResolverActivity.this.resetButtonBar();
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

        private void addResolveInfoWithAlternates(ResolvedComponentInfo rci, CharSequence extraInfo, CharSequence roLabel) {
            int count = rci.getCount();
            Intent intent = rci.getIntentAt(0);
            ResolveInfo add = rci.getResolveInfoAt(0);
            Intent replaceIntent = ResolverActivity.this.getReplacementIntent(add.activityInfo, intent);
            DisplayResolveInfo dri = new DisplayResolveInfo(intent, add, roLabel, extraInfo, replaceIntent);
            addResolveInfo(dri);
            if (replaceIntent == intent) {
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
            ResolveInfo resolveInfo = this.mLastChosen;
            if (resolveInfo != null && resolveInfo.activityInfo.packageName.equals(info.activityInfo.packageName) && this.mLastChosen.activityInfo.name.equals(info.activityInfo.name)) {
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

        @Override // android.widget.Adapter
        public int getCount() {
            int totalSize;
            List<DisplayResolveInfo> list = this.mDisplayList;
            if (list == null || list.isEmpty()) {
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

        @Override // android.widget.Adapter
        public TargetInfo getItem(int position) {
            int i;
            if (this.mFilterLastUsed && (i = this.mLastChosenPosition) >= 0 && position >= i) {
                position++;
            }
            if (this.mDisplayList.size() > position) {
                return this.mDisplayList.get(position);
            }
            return null;
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return (long) position;
        }

        public int getDisplayResolveInfoCount() {
            return this.mDisplayList.size();
        }

        public DisplayResolveInfo getDisplayResolveInfo(int index) {
            return this.mDisplayList.get(index);
        }

        @Override // android.widget.Adapter
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
            return this.mInflater.inflate(R.layout.resolve_list_item, parent, false);
        }

        public final void bindView(int position, View view) {
            onBindView(view, getItem(position));
        }

        /* access modifiers changed from: protected */
        public void onBindView(View view, TargetInfo info) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (info == null) {
                holder.icon.setImageDrawable(ResolverActivity.this.getDrawable(R.drawable.resolver_icon_placeholder));
            } else if (holder == null || holder.text == null || holder.text2 == null) {
                Log.d(ResolverActivity.TAG, "TextView canot be null");
            } else {
                CharSequence label = info.getDisplayLabel();
                if (!TextUtils.equals(holder.text.getText(), label)) {
                    holder.text.setText(info.getDisplayLabel());
                }
                CharSequence subLabel = info.getExtendedInfo();
                if (TextUtils.equals(label, subLabel)) {
                    subLabel = null;
                }
                if (!TextUtils.equals(holder.text2.getText(), subLabel)) {
                    holder.text2.setText(subLabel);
                }
                if (info.isSuspended()) {
                    holder.icon.setColorFilter(ResolverActivity.this.mSuspendedMatrixColorFilter);
                } else {
                    holder.icon.setColorFilter((ColorFilter) null);
                }
                if (!(info instanceof DisplayResolveInfo) || ((DisplayResolveInfo) info).hasDisplayIcon()) {
                    holder.icon.setImageDrawable(info.getDisplayIcon());
                } else {
                    new LoadIconTask((DisplayResolveInfo) info, holder.icon).execute(new Void[0]);
                }
            }
        }
    }

    @VisibleForTesting
    public static final class ResolvedComponentInfo {
        private final List<Intent> mIntents = new ArrayList();
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
    }

    /* access modifiers changed from: package-private */
    public static class ViewHolder {
        public Drawable defaultItemViewBackground;
        public ImageView icon;
        public View itemView;
        public TextView text;
        public TextView text2;

        public ViewHolder(View view) {
            this.itemView = view;
            this.defaultItemViewBackground = view.getBackground();
            this.text = (TextView) view.findViewById(16908308);
            this.text2 = (TextView) view.findViewById(16908309);
            this.icon = (ImageView) view.findViewById(16908294);
        }
    }

    /* access modifiers changed from: package-private */
    public class ItemClickListener implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
        ItemClickListener() {
        }

        @Override // android.widget.AdapterView.OnItemClickListener
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
                    return;
                }
                ResolverActivity.this.setAlwaysButtonEnabled(hasValidSelection, checkedPos, true);
                ResolverActivity.this.mOnceButton.setEnabled(hasValidSelection);
                if (hasValidSelection) {
                    ResolverActivity.this.mAdapterView.smoothScrollToPosition(checkedPos);
                }
                ResolverActivity.this.mLastSelected = checkedPos;
            }
        }

        @Override // android.widget.AdapterView.OnItemLongClickListener
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

    /* access modifiers changed from: package-private */
    public class LoadIconTask extends AsyncTask<Void, Void, Drawable> {
        protected final DisplayResolveInfo mDisplayResolveInfo;
        private final ResolveInfo mResolveInfo;
        private final ImageView mTargetView;

        LoadIconTask(DisplayResolveInfo dri, ImageView target) {
            this.mDisplayResolveInfo = dri;
            this.mResolveInfo = dri.getResolveInfo();
            this.mTargetView = target;
        }

        /* access modifiers changed from: protected */
        public Drawable doInBackground(Void... params) {
            return ResolverActivity.this.loadIconForResolveInfo(this.mResolveInfo);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Drawable d) {
            DisplayResolveInfo otherProfile = ResolverActivity.this.mAdapter.getOtherProfile();
            DisplayResolveInfo displayResolveInfo = this.mDisplayResolveInfo;
            if (otherProfile == displayResolveInfo) {
                ResolverActivity.this.bindProfileView();
                return;
            }
            displayResolveInfo.setDisplayIcon(d);
            this.mTargetView.setImageDrawable(d);
        }
    }

    static final boolean isSpecificUriMatch(int match) {
        int match2 = match & IntentFilter.MATCH_CATEGORY_MASK;
        return match2 >= 3145728 && match2 <= 5242880;
    }

    /* access modifiers changed from: package-private */
    public static class PickTargetOptionRequest extends VoiceInteractor.PickOptionRequest {
        public PickTargetOptionRequest(VoiceInteractor.Prompt prompt, VoiceInteractor.PickOptionRequest.Option[] options, Bundle extras) {
            super(prompt, options, extras);
        }

        @Override // android.app.VoiceInteractor.Request
        public void onCancel() {
            super.onCancel();
            ResolverActivity ra = (ResolverActivity) getActivity();
            if (ra != null) {
                ra.mPickOptionRequest = null;
                ra.finish();
            }
        }

        @Override // android.app.VoiceInteractor.PickOptionRequest
        public void onPickOptionResult(boolean finished, VoiceInteractor.PickOptionRequest.Option[] selections, Bundle result) {
            ResolverActivity ra;
            super.onPickOptionResult(finished, selections, result);
            if (selections.length == 1 && (ra = (ResolverActivity) getActivity()) != null && ra.onTargetSelected(ra.mAdapter.getItem(selections[0].getIndex()), false)) {
                ra.mPickOptionRequest = null;
                ra.finish();
            }
        }
    }
}
