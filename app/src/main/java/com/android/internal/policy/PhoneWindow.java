package com.android.internal.policy;

import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionLegacyHelper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AndroidRuntimeException;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.IRotationWatcher.Stub;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputQueue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.SurfaceHolder.Callback2;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewManager;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;
import com.android.internal.view.menu.ContextMenuBuilder;
import com.android.internal.view.menu.IconMenuPresenter;
import com.android.internal.view.menu.ListMenuPresenter;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuBuilder.Callback;
import com.android.internal.view.menu.MenuDialogHelper;
import com.android.internal.view.menu.MenuHelper;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;
import com.android.internal.widget.DecorContentParent;
import com.android.internal.widget.FloatingToolbar;
import com.android.internal.widget.SwipeDismissLayout;
import com.android.internal.widget.SwipeDismissLayout.OnDismissedListener;
import com.android.internal.widget.SwipeDismissLayout.OnSwipeProgressChangedListener;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.log.LogPower;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

public class PhoneWindow extends Window implements Callback {
    private static final String ACCESSIBILITY_FOCUSED_ID_TAG = "android:accessibilityFocusedViewId";
    private static final String ACCESSIBILITY_FOCUSED_VIRTUAL_ID_TAG = "android:accessibilityFocusedVirtualViewId";
    private static final String ACTION_BAR_TAG = "android:ActionBar";
    private static final int CUSTOM_TITLE_COMPATIBLE_FEATURES = 13505;
    private static final boolean DEBUG = false;
    static final boolean DEBUG_IMMERSION = false;
    private static final int DEFAULT_BACKGROUND_FADE_DURATION_MS = 300;
    static final int FLAG_RESOURCE_SET_ICON = 1;
    static final int FLAG_RESOURCE_SET_ICON_FALLBACK = 4;
    static final int FLAG_RESOURCE_SET_LOGO = 2;
    private static final String FOCUSED_ID_TAG = "android:focusedViewId";
    static final boolean HISI_PERF_OPT = false;
    private static final String PANELS_TAG = "android:Panels";
    private static final String PERMISSION_USE_SMARTKEY = "huawei.permission.USE_SMARTKEY";
    private static final boolean SWEEP_OPEN_MENU = false;
    private static final String TAG = "PhoneWindow";
    private static final Transition USE_DEFAULT_TRANSITION = null;
    private static final String VIEWS_TAG = "android:views";
    static final RotationWatcher sRotationWatcher = null;
    private ActionMenuPresenterCallback mActionMenuPresenterCallback;
    private Boolean mAllowEnterTransitionOverlap;
    private Boolean mAllowReturnTransitionOverlap;
    private boolean mAlwaysReadCloseOnTouchAttr;
    private boolean mAlwaysSplit;
    private AudioManager mAudioManager;
    private Drawable mBackgroundDrawable;
    private long mBackgroundFadeDurationMillis;
    int mBackgroundFallbackResource;
    int mBackgroundResource;
    private int mBlurHeight;
    private Rect mBlurRect;
    private int mBlurWidth;
    private ProgressBar mCircularProgressBar;
    private boolean mClipToOutline;
    private boolean mClosingActionMenu;
    ViewGroup mContentParent;
    private boolean mContentParentExplicitlySet;
    private Scene mContentScene;
    ContextMenuBuilder mContextMenu;
    final PhoneWindowMenuCallback mContextMenuCallback;
    MenuHelper mContextMenuHelper;
    private DecorView mDecor;
    private int mDecorCaptionShade;
    DecorContentParent mDecorContentParent;
    private DrawableFeatureState[] mDrawables;
    private float mElevation;
    private Transition mEnterTransition;
    private Transition mExitTransition;
    TypedValue mFixedHeightMajor;
    TypedValue mFixedHeightMinor;
    TypedValue mFixedWidthMajor;
    TypedValue mFixedWidthMinor;
    private boolean mForceDecorInstall;
    private boolean mForcedDrawSysBarBackground;
    private boolean mForcedNavigationBarColor;
    private boolean mForcedStatusBarColor;
    private int mFrameResource;
    private ProgressBar mHorizontalProgressBar;
    int mIconRes;
    private int mInvalidatePanelMenuFeatures;
    private boolean mInvalidatePanelMenuPosted;
    private final Runnable mInvalidatePanelMenuRunnable;
    protected boolean mIsFloating;
    private boolean mIsStartingWindow;
    private boolean mIsTranslucent;
    private boolean mIsTransparent;
    private KeyguardManager mKeyguardManager;
    private LayoutInflater mLayoutInflater;
    private ImageView mLeftIconView;
    private boolean mLoadElevation;
    int mLogoRes;
    private int mMaxSize;
    private MediaController mMediaController;
    final TypedValue mMinWidthMajor;
    final TypedValue mMinWidthMinor;
    private int mNaviWidth;
    int mNavigationBarColor;
    int mPanelChordingKey;
    private PanelMenuPresenterCallback mPanelMenuPresenterCallback;
    private PanelFeatureState[] mPanels;
    PanelFeatureState mPreparedPanel;
    private Transition mReenterTransition;
    int mResourcesSetFlags;
    private Transition mReturnTransition;
    private ImageView mRightIconView;
    private Transition mSharedElementEnterTransition;
    private Transition mSharedElementExitTransition;
    private Transition mSharedElementReenterTransition;
    private Transition mSharedElementReturnTransition;
    private Boolean mSharedElementsUseOverlay;
    int mStatusBarColor;
    private boolean mSupportsPictureInPicture;
    InputQueue.Callback mTakeInputQueueCallback;
    Callback2 mTakeSurfaceCallback;
    private int mTextColor;
    private int mTheme;
    private CharSequence mTitle;
    private int mTitleColor;
    private TextView mTitleView;
    private TransitionManager mTransitionManager;
    private int mUiOptions;
    private boolean mUseDecorContext;
    private int mVolumeControlStreamType;
    private WallpaperManager mWallpaperManager;

    private final class ActionMenuPresenterCallback implements MenuPresenter.Callback {
        private ActionMenuPresenterCallback() {
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            Window.Callback cb = PhoneWindow.this.getCallback();
            if (cb == null) {
                return PhoneWindow.SWEEP_OPEN_MENU;
            }
            cb.onMenuOpened(8, subMenu);
            return true;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            PhoneWindow.this.checkCloseActionMenu(menu);
        }
    }

    private static final class DrawableFeatureState {
        int alpha;
        Drawable child;
        Drawable cur;
        int curAlpha;
        Drawable def;
        final int featureId;
        Drawable local;
        int resid;
        Uri uri;

        DrawableFeatureState(int _featureId) {
            this.alpha = MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
            this.curAlpha = MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
            this.featureId = _featureId;
        }
    }

    static final class PanelFeatureState {
        int background;
        View createdPanelView;
        DecorView decorView;
        int featureId;
        Bundle frozenActionViewState;
        Bundle frozenMenuState;
        int fullBackground;
        int gravity;
        IconMenuPresenter iconMenuPresenter;
        boolean isCompact;
        boolean isHandled;
        boolean isInExpandedMode;
        boolean isOpen;
        boolean isPrepared;
        ListMenuPresenter listMenuPresenter;
        int listPresenterTheme;
        MenuBuilder menu;
        public boolean qwertyMode;
        boolean refreshDecorView;
        boolean refreshMenuContent;
        View shownPanelView;
        boolean wasLastExpanded;
        boolean wasLastOpen;
        int windowAnimations;
        int x;
        int y;

        private static class SavedState implements Parcelable {
            public static final Creator<SavedState> CREATOR = null;
            int featureId;
            boolean isInExpandedMode;
            boolean isOpen;
            Bundle menuState;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.policy.PhoneWindow.PanelFeatureState.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.policy.PhoneWindow.PanelFeatureState.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 9 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 10 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.policy.PhoneWindow.PanelFeatureState.SavedState.<clinit>():void");
            }

            private SavedState() {
            }

            public int describeContents() {
                return 0;
            }

            public void writeToParcel(Parcel dest, int flags) {
                int i;
                int i2 = PhoneWindow.FLAG_RESOURCE_SET_ICON;
                dest.writeInt(this.featureId);
                if (this.isOpen) {
                    i = PhoneWindow.FLAG_RESOURCE_SET_ICON;
                } else {
                    i = 0;
                }
                dest.writeInt(i);
                if (!this.isInExpandedMode) {
                    i2 = 0;
                }
                dest.writeInt(i2);
                if (this.isOpen) {
                    dest.writeBundle(this.menuState);
                }
            }

            private static SavedState readFromParcel(Parcel source) {
                boolean z = true;
                SavedState savedState = new SavedState();
                savedState.featureId = source.readInt();
                savedState.isOpen = source.readInt() == PhoneWindow.FLAG_RESOURCE_SET_ICON ? true : PhoneWindow.SWEEP_OPEN_MENU;
                if (source.readInt() != PhoneWindow.FLAG_RESOURCE_SET_ICON) {
                    z = PhoneWindow.SWEEP_OPEN_MENU;
                }
                savedState.isInExpandedMode = z;
                if (savedState.isOpen) {
                    savedState.menuState = source.readBundle();
                }
                return savedState;
            }
        }

        PanelFeatureState(int featureId) {
            this.featureId = featureId;
            this.refreshDecorView = PhoneWindow.SWEEP_OPEN_MENU;
        }

        public boolean isInListMode() {
            return !this.isInExpandedMode ? this.isCompact : true;
        }

        public boolean hasPanelItems() {
            boolean z = true;
            if (this.shownPanelView == null) {
                return PhoneWindow.SWEEP_OPEN_MENU;
            }
            if (this.createdPanelView != null) {
                return true;
            }
            if (this.isCompact || this.isInExpandedMode) {
                return this.listMenuPresenter.getAdapter().getCount() > 0 ? true : PhoneWindow.SWEEP_OPEN_MENU;
            }
            if (((ViewGroup) this.shownPanelView).getChildCount() <= 0) {
                z = PhoneWindow.SWEEP_OPEN_MENU;
            }
            return z;
        }

        public void clearMenuPresenters() {
            if (this.menu != null) {
                this.menu.removeMenuPresenter(this.iconMenuPresenter);
                this.menu.removeMenuPresenter(this.listMenuPresenter);
            }
            this.iconMenuPresenter = null;
            this.listMenuPresenter = null;
        }

        void setStyle(Context context) {
            TypedArray a = context.obtainStyledAttributes(R.styleable.Theme);
            this.background = a.getResourceId(46, 0);
            this.fullBackground = a.getResourceId(47, 0);
            this.windowAnimations = a.getResourceId(93, 0);
            this.isCompact = a.getBoolean(MetricsEvent.ACTION_BUGREPORT_FROM_SETTINGS_INTERACTIVE, PhoneWindow.SWEEP_OPEN_MENU);
            this.listPresenterTheme = a.getResourceId(MetricsEvent.ACTION_BUGREPORT_NOTIFICATION_ACTION_CANCEL, R.style.Theme_ExpandedMenu);
            a.recycle();
        }

        void setMenu(MenuBuilder menu) {
            if (menu != this.menu) {
                if (this.menu != null) {
                    this.menu.removeMenuPresenter(this.iconMenuPresenter);
                    this.menu.removeMenuPresenter(this.listMenuPresenter);
                }
                this.menu = menu;
                if (menu != null) {
                    if (this.iconMenuPresenter != null) {
                        menu.addMenuPresenter(this.iconMenuPresenter);
                    }
                    if (this.listMenuPresenter != null) {
                        menu.addMenuPresenter(this.listMenuPresenter);
                    }
                }
            }
        }

        MenuView getListMenuView(Context context, MenuPresenter.Callback cb) {
            if (this.menu == null) {
                return null;
            }
            if (!this.isCompact) {
                getIconMenuView(context, cb);
            }
            if (this.listMenuPresenter == null) {
                this.listMenuPresenter = new ListMenuPresenter((int) R.layout.list_menu_item_layout, this.listPresenterTheme);
                this.listMenuPresenter.setCallback(cb);
                this.listMenuPresenter.setId(R.id.list_menu_presenter);
                this.menu.addMenuPresenter(this.listMenuPresenter);
            }
            if (this.iconMenuPresenter != null) {
                this.listMenuPresenter.setItemIndexOffset(this.iconMenuPresenter.getNumActualItemsShown());
            }
            return this.listMenuPresenter.getMenuView(this.decorView);
        }

        MenuView getIconMenuView(Context context, MenuPresenter.Callback cb) {
            if (this.menu == null) {
                return null;
            }
            if (this.iconMenuPresenter == null) {
                this.iconMenuPresenter = new IconMenuPresenter(context);
                this.iconMenuPresenter.setCallback(cb);
                this.iconMenuPresenter.setId(R.id.icon_menu_presenter);
                this.menu.addMenuPresenter(this.iconMenuPresenter);
            }
            return this.iconMenuPresenter.getMenuView(this.decorView);
        }

        Parcelable onSaveInstanceState() {
            SavedState savedState = new SavedState();
            savedState.featureId = this.featureId;
            savedState.isOpen = this.isOpen;
            savedState.isInExpandedMode = this.isInExpandedMode;
            if (this.menu != null) {
                savedState.menuState = new Bundle();
                this.menu.savePresenterStates(savedState.menuState);
            }
            return savedState;
        }

        void onRestoreInstanceState(Parcelable state) {
            SavedState savedState = (SavedState) state;
            this.featureId = savedState.featureId;
            this.wasLastOpen = savedState.isOpen;
            this.wasLastExpanded = savedState.isInExpandedMode;
            this.frozenMenuState = savedState.menuState;
            this.createdPanelView = null;
            this.shownPanelView = null;
            this.decorView = null;
        }

        void applyFrozenState() {
            if (this.menu != null && this.frozenMenuState != null) {
                this.menu.restorePresenterStates(this.frozenMenuState);
                this.frozenMenuState = null;
            }
        }
    }

    private class PanelMenuPresenterCallback implements MenuPresenter.Callback {
        final /* synthetic */ PhoneWindow this$0;

        /* synthetic */ PanelMenuPresenterCallback(PhoneWindow this$0, PanelMenuPresenterCallback panelMenuPresenterCallback) {
            this(this$0);
        }

        private PanelMenuPresenterCallback(PhoneWindow this$0) {
            this.this$0 = this$0;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            MenuBuilder parentMenu = menu.getRootMenu();
            boolean isSubMenu = parentMenu != menu ? true : PhoneWindow.SWEEP_OPEN_MENU;
            PhoneWindow phoneWindow = this.this$0;
            if (isSubMenu) {
                menu = parentMenu;
            }
            PanelFeatureState panel = phoneWindow.findMenuPanel(menu);
            if (panel == null) {
                return;
            }
            if (isSubMenu) {
                this.this$0.callOnPanelClosed(panel.featureId, panel, parentMenu);
                this.this$0.closePanel(panel, true);
                return;
            }
            this.this$0.closePanel(panel, allMenusAreClosing);
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (subMenu == null && this.this$0.hasFeature(8)) {
                Window.Callback cb = this.this$0.getCallback();
                if (!(cb == null || this.this$0.isDestroyed())) {
                    cb.onMenuOpened(8, subMenu);
                }
            }
            return true;
        }
    }

    public static final class PhoneWindowMenuCallback implements Callback, MenuPresenter.Callback {
        private static final int FEATURE_ID = 6;
        private boolean mShowDialogForSubmenu;
        private MenuDialogHelper mSubMenuHelper;
        private final PhoneWindow mWindow;

        public PhoneWindowMenuCallback(PhoneWindow window) {
            this.mWindow = window;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (menu.getRootMenu() != menu) {
                onCloseSubMenu(menu);
            }
            if (allMenusAreClosing) {
                Window.Callback callback = this.mWindow.getCallback();
                if (!(callback == null || this.mWindow.isDestroyed())) {
                    callback.onPanelClosed(FEATURE_ID, menu);
                }
                if (menu == this.mWindow.mContextMenu) {
                    this.mWindow.dismissContextMenu();
                }
                if (this.mSubMenuHelper != null) {
                    this.mSubMenuHelper.dismiss();
                    this.mSubMenuHelper = null;
                }
            }
        }

        private void onCloseSubMenu(MenuBuilder menu) {
            Window.Callback callback = this.mWindow.getCallback();
            if (callback != null && !this.mWindow.isDestroyed()) {
                callback.onPanelClosed(FEATURE_ID, menu.getRootMenu());
            }
        }

        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            Window.Callback callback = this.mWindow.getCallback();
            if (callback == null || this.mWindow.isDestroyed()) {
                return PhoneWindow.SWEEP_OPEN_MENU;
            }
            return callback.onMenuItemSelected(FEATURE_ID, item);
        }

        public void onMenuModeChange(MenuBuilder menu) {
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (subMenu == null) {
                return PhoneWindow.SWEEP_OPEN_MENU;
            }
            subMenu.setCallback(this);
            if (!this.mShowDialogForSubmenu) {
                return PhoneWindow.SWEEP_OPEN_MENU;
            }
            this.mSubMenuHelper = new MenuDialogHelper(subMenu);
            this.mSubMenuHelper.show(null);
            return true;
        }

        public void setShowDialogForSubmenu(boolean enabled) {
            this.mShowDialogForSubmenu = enabled;
        }
    }

    static class RotationWatcher extends Stub {
        private Handler mHandler;
        private boolean mIsWatching;
        private final Runnable mRotationChanged;
        private final ArrayList<WeakReference<PhoneWindow>> mWindows;

        /* renamed from: com.android.internal.policy.PhoneWindow.RotationWatcher.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ RotationWatcher this$1;

            AnonymousClass1(RotationWatcher this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                this.this$1.dispatchRotationChanged();
            }
        }

        RotationWatcher() {
            this.mRotationChanged = new AnonymousClass1(this);
            this.mWindows = new ArrayList();
        }

        public void onRotationChanged(int rotation) throws RemoteException {
            this.mHandler.post(this.mRotationChanged);
        }

        public void addWindow(PhoneWindow phoneWindow) {
            synchronized (this.mWindows) {
                if (!this.mIsWatching) {
                    try {
                        WindowManagerHolder.sWindowManager.watchRotation(this);
                        this.mHandler = new Handler();
                        this.mIsWatching = true;
                    } catch (RemoteException ex) {
                        Log.e(PhoneWindow.TAG, "Couldn't start watching for device rotation", ex);
                    }
                }
                this.mWindows.add(new WeakReference(phoneWindow));
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void removeWindow(PhoneWindow phoneWindow) {
            synchronized (this.mWindows) {
                int i = 0;
                while (true) {
                    if (i < this.mWindows.size()) {
                        PhoneWindow win = (PhoneWindow) ((WeakReference) this.mWindows.get(i)).get();
                        if (win == null || win == phoneWindow) {
                            this.mWindows.remove(i);
                        } else {
                            i += PhoneWindow.FLAG_RESOURCE_SET_ICON;
                        }
                    }
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void dispatchRotationChanged() {
            synchronized (this.mWindows) {
                int i = 0;
                while (true) {
                    if (i < this.mWindows.size()) {
                        PhoneWindow win = (PhoneWindow) ((WeakReference) this.mWindows.get(i)).get();
                        if (win != null) {
                            win.onOptionsPanelRotationChanged();
                            i += PhoneWindow.FLAG_RESOURCE_SET_ICON;
                        } else {
                            this.mWindows.remove(i);
                        }
                    }
                }
            }
        }
    }

    static class WindowManagerHolder {
        static final IWindowManager sWindowManager = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.policy.PhoneWindow.WindowManagerHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.policy.PhoneWindow.WindowManagerHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.policy.PhoneWindow.WindowManagerHolder.<clinit>():void");
        }

        WindowManagerHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.policy.PhoneWindow.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.policy.PhoneWindow.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.policy.PhoneWindow.<clinit>():void");
    }

    protected android.view.ViewGroup generateLayout(com.android.internal.policy.DecorView r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.policy.PhoneWindow.generateLayout(com.android.internal.policy.DecorView):android.view.ViewGroup
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.policy.PhoneWindow.generateLayout(com.android.internal.policy.DecorView):android.view.ViewGroup");
    }

    public void setUiOptions(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.policy.PhoneWindow.setUiOptions(int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.policy.PhoneWindow.setUiOptions(int, int):void");
    }

    public PhoneWindow(Context context) {
        super(context);
        this.mContextMenuCallback = new PhoneWindowMenuCallback(this);
        this.mMinWidthMajor = new TypedValue();
        this.mMinWidthMinor = new TypedValue();
        this.mForceDecorInstall = SWEEP_OPEN_MENU;
        this.mContentParentExplicitlySet = SWEEP_OPEN_MENU;
        this.mBackgroundResource = 0;
        this.mBackgroundFallbackResource = 0;
        this.mLoadElevation = true;
        this.mFrameResource = 0;
        this.mTextColor = 0;
        this.mStatusBarColor = 0;
        this.mNavigationBarColor = 0;
        this.mForcedStatusBarColor = SWEEP_OPEN_MENU;
        this.mForcedNavigationBarColor = SWEEP_OPEN_MENU;
        this.mTitle = null;
        this.mTitleColor = 0;
        this.mAlwaysReadCloseOnTouchAttr = SWEEP_OPEN_MENU;
        this.mVolumeControlStreamType = RtlSpacingHelper.UNDEFINED;
        this.mUiOptions = 0;
        this.mInvalidatePanelMenuRunnable = new Runnable() {
            public void run() {
                for (int i = 0; i <= 13; i += PhoneWindow.FLAG_RESOURCE_SET_ICON) {
                    if ((PhoneWindow.this.mInvalidatePanelMenuFeatures & (PhoneWindow.FLAG_RESOURCE_SET_ICON << i)) != 0) {
                        PhoneWindow.this.doInvalidatePanelMenu(i);
                    }
                }
                PhoneWindow.this.mInvalidatePanelMenuPosted = PhoneWindow.SWEEP_OPEN_MENU;
                PhoneWindow.this.mInvalidatePanelMenuFeatures = 0;
            }
        };
        this.mEnterTransition = null;
        this.mReturnTransition = USE_DEFAULT_TRANSITION;
        this.mExitTransition = null;
        this.mReenterTransition = USE_DEFAULT_TRANSITION;
        this.mSharedElementEnterTransition = null;
        this.mSharedElementReturnTransition = USE_DEFAULT_TRANSITION;
        this.mSharedElementExitTransition = null;
        this.mSharedElementReenterTransition = USE_DEFAULT_TRANSITION;
        this.mBackgroundFadeDurationMillis = -1;
        this.mTheme = -1;
        this.mDecorCaptionShade = 0;
        this.mUseDecorContext = SWEEP_OPEN_MENU;
        this.mAlwaysSplit = SWEEP_OPEN_MENU;
        this.mMaxSize = 0;
        this.mNaviWidth = 0;
        this.mForcedDrawSysBarBackground = SWEEP_OPEN_MENU;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mNaviWidth = context.getResources().getDimensionPixelSize(R.dimen.navigation_bar_width);
        if (HISI_PERF_OPT) {
            this.mWallpaperManager = (WallpaperManager) context.getSystemService("wallpaper");
            this.mBlurWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            this.mBlurHeight = getContext().getResources().getDisplayMetrics().heightPixels;
            this.mBlurRect = new Rect(0, 0, this.mBlurWidth, this.mBlurHeight);
        }
    }

    public PhoneWindow(Context context, Window preservedWindow) {
        boolean forceResizable = SWEEP_OPEN_MENU;
        this(context);
        this.mUseDecorContext = true;
        if (preservedWindow != null) {
            this.mDecor = (DecorView) preservedWindow.getDecorView();
            this.mElevation = preservedWindow.getElevation();
            this.mLoadElevation = SWEEP_OPEN_MENU;
            this.mForceDecorInstall = true;
            getAttributes().token = preservedWindow.getAttributes().token;
        }
        if (Global.getInt(context.getContentResolver(), "force_resizable_activities", 0) != 0) {
            forceResizable = true;
        }
        this.mSupportsPictureInPicture = !forceResizable ? context.getPackageManager().hasSystemFeature("android.software.picture_in_picture") : true;
    }

    public final void setContainer(Window container) {
        super.setContainer(container);
    }

    public boolean requestFeature(int featureId) {
        if (this.mContentParentExplicitlySet) {
            throw new AndroidRuntimeException("requestFeature() must be called before adding content");
        }
        int features = getFeatures();
        int newFeatures = features | (FLAG_RESOURCE_SET_ICON << featureId);
        if ((newFeatures & LogPower.START_CHG_ROTATION) != 0 && (newFeatures & -13506) != 0) {
            throw new AndroidRuntimeException("You cannot combine custom titles with other title features");
        } else if ((features & FLAG_RESOURCE_SET_LOGO) != 0 && featureId == 8) {
            return SWEEP_OPEN_MENU;
        } else {
            if ((features & GL10.GL_DEPTH_BUFFER_BIT) != 0 && featureId == FLAG_RESOURCE_SET_ICON) {
                removeFeature(8);
            }
            if ((features & GL10.GL_DEPTH_BUFFER_BIT) != 0 && featureId == 11) {
                throw new AndroidRuntimeException("You cannot combine swipe dismissal and the action bar.");
            } else if ((features & GL10.GL_EXP) != 0 && featureId == 8) {
                throw new AndroidRuntimeException("You cannot combine swipe dismissal and the action bar.");
            } else if (featureId != 5 || !getContext().getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                return super.requestFeature(featureId);
            } else {
                throw new AndroidRuntimeException("You cannot use indeterminate progress on a watch.");
            }
        }
    }

    public void setUiOptions(int uiOptions) {
        this.mUiOptions = uiOptions;
    }

    public TransitionManager getTransitionManager() {
        return this.mTransitionManager;
    }

    public void setTransitionManager(TransitionManager tm) {
        this.mTransitionManager = tm;
    }

    public Scene getContentScene() {
        return this.mContentScene;
    }

    public void setContentView(int layoutResID) {
        if (this.mContentParent == null) {
            installDecor();
        } else if (!hasFeature(12)) {
            this.mContentParent.removeAllViews();
        }
        initHwDrawerFeature();
        if (hasFeature(12)) {
            transitionTo(Scene.getSceneForLayout(this.mContentParent, layoutResID, getContext()));
        } else {
            this.mLayoutInflater.inflate(layoutResID, this.mContentParent);
        }
        this.mContentParent.requestApplyInsets();
        Window.Callback cb = getCallback();
        if (!(cb == null || isDestroyed())) {
            cb.onContentChanged();
        }
        this.mContentParentExplicitlySet = true;
    }

    public void setContentView(View view) {
        setContentView(view, new LayoutParams(-1, -1));
    }

    public void setContentView(View view, LayoutParams params) {
        if (this.mContentParent == null) {
            installDecor();
        } else if (!hasFeature(12)) {
            this.mContentParent.removeAllViews();
        }
        if (hasFeature(12)) {
            view.setLayoutParams(params);
            transitionTo(new Scene(this.mContentParent, view));
        } else {
            this.mContentParent.addView(view, params);
        }
        this.mContentParent.requestApplyInsets();
        Window.Callback cb = getCallback();
        if (!(cb == null || isDestroyed())) {
            cb.onContentChanged();
        }
        this.mContentParentExplicitlySet = true;
    }

    public void addContentView(View view, LayoutParams params) {
        if (this.mContentParent == null) {
            installDecor();
        }
        if (hasFeature(12)) {
            Log.v(TAG, "addContentView does not support content transitions");
        }
        this.mContentParent.addView(view, params);
        this.mContentParent.requestApplyInsets();
        Window.Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
    }

    public void clearContentView() {
        if (this.mDecor != null) {
            this.mDecor.clearContentView();
        }
    }

    private void transitionTo(Scene scene) {
        if (this.mContentScene == null) {
            scene.enter();
        } else {
            this.mTransitionManager.transitionTo(scene);
        }
        this.mContentScene = scene;
    }

    public View getCurrentFocus() {
        return this.mDecor != null ? this.mDecor.findFocus() : null;
    }

    public void takeSurface(Callback2 callback) {
        this.mTakeSurfaceCallback = callback;
    }

    public void takeInputQueue(InputQueue.Callback callback) {
        this.mTakeInputQueueCallback = callback;
    }

    public boolean isFloating() {
        return this.mIsFloating;
    }

    public boolean isTranslucent() {
        return this.mIsTranslucent;
    }

    boolean isShowingWallpaper() {
        return (getAttributes().flags & AccessibilityNodeInfo.ACTION_DISMISS) != 0 ? true : SWEEP_OPEN_MENU;
    }

    public LayoutInflater getLayoutInflater() {
        return this.mLayoutInflater;
    }

    public void setTitle(CharSequence title) {
        setTitle(title, true);
    }

    public void setTitle(CharSequence title, boolean updateAccessibilityTitle) {
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
        } else if (this.mDecorContentParent != null) {
            this.mDecorContentParent.setWindowTitle(title);
        }
        this.mTitle = title;
        if (updateAccessibilityTitle) {
            WindowManager.LayoutParams params = getAttributes();
            if (!TextUtils.equals(title, params.accessibilityTitle)) {
                params.accessibilityTitle = TextUtils.stringOrSpannedString(title);
                dispatchWindowAttributesChanged(getAttributes());
            }
        }
    }

    @Deprecated
    public void setTitleColor(int textColor) {
        if (this.mTitleView != null) {
            this.mTitleView.setTextColor(textColor);
        }
        this.mTitleColor = textColor;
    }

    public final boolean preparePanel(PanelFeatureState st, KeyEvent event) {
        if (isDestroyed()) {
            return SWEEP_OPEN_MENU;
        }
        if (st.isPrepared) {
            return true;
        }
        boolean isActionBarMenu;
        if (!(this.mPreparedPanel == null || this.mPreparedPanel == st)) {
            closePanel(this.mPreparedPanel, SWEEP_OPEN_MENU);
        }
        Window.Callback cb = getCallback();
        if (cb != null) {
            st.createdPanelView = cb.onCreatePanelView(st.featureId);
        }
        if (st.featureId == 0 || st.featureId == 8) {
            isActionBarMenu = true;
        } else {
            isActionBarMenu = SWEEP_OPEN_MENU;
        }
        if (isActionBarMenu && this.mDecorContentParent != null) {
            this.mDecorContentParent.setMenuPrepared();
        }
        if (st.createdPanelView == null) {
            if (st.menu == null || st.refreshMenuContent) {
                if (st.menu == null && (!initializePanelMenu(st) || st.menu == null)) {
                    return SWEEP_OPEN_MENU;
                }
                if (isActionBarMenu && this.mDecorContentParent != null) {
                    if (this.mActionMenuPresenterCallback == null) {
                        this.mActionMenuPresenterCallback = new ActionMenuPresenterCallback();
                    }
                    this.mDecorContentParent.setMenu(st.menu, this.mActionMenuPresenterCallback);
                }
                st.menu.stopDispatchingItemsChanged();
                if (cb == null || !cb.onCreatePanelMenu(st.featureId, st.menu)) {
                    st.setMenu(null);
                    if (isActionBarMenu && this.mDecorContentParent != null) {
                        this.mDecorContentParent.setMenu(null, this.mActionMenuPresenterCallback);
                    }
                    return SWEEP_OPEN_MENU;
                }
                st.refreshMenuContent = SWEEP_OPEN_MENU;
            }
            st.menu.stopDispatchingItemsChanged();
            if (st.frozenActionViewState != null) {
                st.menu.restoreActionViewStates(st.frozenActionViewState);
                st.frozenActionViewState = null;
            }
            if (cb.onPreparePanel(st.featureId, st.createdPanelView, st.menu)) {
                boolean z;
                if (KeyCharacterMap.load(event != null ? event.getDeviceId() : -1).getKeyboardType() != FLAG_RESOURCE_SET_ICON) {
                    z = true;
                } else {
                    z = SWEEP_OPEN_MENU;
                }
                st.qwertyMode = z;
                st.menu.setQwertyMode(st.qwertyMode);
                st.menu.startDispatchingItemsChanged();
            } else {
                if (isActionBarMenu && this.mDecorContentParent != null) {
                    this.mDecorContentParent.setMenu(null, this.mActionMenuPresenterCallback);
                }
                st.menu.startDispatchingItemsChanged();
                return SWEEP_OPEN_MENU;
            }
        }
        st.isPrepared = true;
        st.isHandled = SWEEP_OPEN_MENU;
        this.mPreparedPanel = st;
        return true;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mDecorContentParent == null) {
            PanelFeatureState st = getPanelState(0, SWEEP_OPEN_MENU);
            if (st != null && st.menu != null) {
                if (st.isOpen) {
                    Bundle state = new Bundle();
                    if (st.iconMenuPresenter != null) {
                        st.iconMenuPresenter.saveHierarchyState(state);
                    }
                    if (st.listMenuPresenter != null) {
                        st.listMenuPresenter.saveHierarchyState(state);
                    }
                    clearMenuViews(st);
                    reopenMenu(SWEEP_OPEN_MENU);
                    if (st.iconMenuPresenter != null) {
                        st.iconMenuPresenter.restoreHierarchyState(state);
                    }
                    if (st.listMenuPresenter != null) {
                        st.listMenuPresenter.restoreHierarchyState(state);
                        return;
                    }
                    return;
                }
                clearMenuViews(st);
            }
        }
    }

    public void onMultiWindowModeChanged() {
        if (this.mDecor != null) {
            this.mDecor.onConfigurationChanged(getContext().getResources().getConfiguration());
        }
    }

    public void reportActivityRelaunched() {
        if (this.mDecor != null && this.mDecor.getViewRootImpl() != null) {
            this.mDecor.getViewRootImpl().reportActivityRelaunched();
        }
    }

    private static void clearMenuViews(PanelFeatureState st) {
        st.createdPanelView = null;
        st.refreshDecorView = true;
        st.clearMenuPresenters();
    }

    public final void openPanel(int featureId, KeyEvent event) {
        if (featureId == 0 && this.mDecorContentParent != null && this.mDecorContentParent.canShowOverflowMenu() && CheckPermanentMenuKey()) {
            this.mDecorContentParent.showOverflowMenu();
        } else {
            openPanel(getPanelState(featureId, true), event);
        }
    }

    private void openPanel(PanelFeatureState st, KeyEvent event) {
        if (!st.isOpen && !isDestroyed()) {
            if (st.featureId == 0) {
                Context context = getContext();
                boolean isXLarge = (context.getResources().getConfiguration().screenLayout & 15) == FLAG_RESOURCE_SET_ICON_FALLBACK ? true : SWEEP_OPEN_MENU;
                boolean isHoneycombApp = context.getApplicationInfo().targetSdkVersion >= 11 ? true : SWEEP_OPEN_MENU;
                if (isXLarge && isHoneycombApp) {
                    return;
                }
            }
            Window.Callback cb = getCallback();
            if (cb == null || cb.onMenuOpened(st.featureId, st.menu)) {
                WindowManager wm = getWindowManager();
                if (wm != null && preparePanel(st, event)) {
                    int width = -2;
                    LayoutParams lp;
                    if (st.decorView == null || st.refreshDecorView) {
                        if (st.decorView == null) {
                            if (!initializePanelDecor(st) || st.decorView == null) {
                                return;
                            }
                        } else if (st.refreshDecorView && st.decorView.getChildCount() > 0) {
                            st.decorView.removeAllViews();
                        }
                        if (initializePanelContent(st) && st.hasPanelItems()) {
                            int backgroundResId;
                            lp = st.shownPanelView.getLayoutParams();
                            if (lp == null) {
                                LayoutParams layoutParams = new LayoutParams(-2, -2);
                            }
                            if (lp.width == -1) {
                                backgroundResId = st.fullBackground;
                                width = -1;
                            } else {
                                backgroundResId = st.background;
                            }
                            st.decorView.setWindowBackground(getContext().getDrawable(backgroundResId));
                            ViewParent shownPanelParent = st.shownPanelView.getParent();
                            if (shownPanelParent != null && (shownPanelParent instanceof ViewGroup)) {
                                ((ViewGroup) shownPanelParent).removeView(st.shownPanelView);
                            }
                            st.decorView.addView(st.shownPanelView, lp);
                            if (!st.shownPanelView.hasFocus()) {
                                st.shownPanelView.requestFocus();
                            }
                        } else {
                            return;
                        }
                    } else if (!st.isInListMode()) {
                        width = -1;
                    } else if (st.createdPanelView != null) {
                        lp = st.createdPanelView.getLayoutParams();
                        if (lp != null && lp.width == -1) {
                            width = -1;
                        }
                    }
                    st.isHandled = SWEEP_OPEN_MENU;
                    WindowManager.LayoutParams lp2 = new WindowManager.LayoutParams(width, -2, st.x, st.y, RILConstants.RIL_UNSOL_RESPONSE_NEW_SMS, 8519680, st.decorView.mDefaultOpacity);
                    if (st.isCompact) {
                        lp2.gravity = getOptionsPanelGravity();
                        sRotationWatcher.addWindow(this);
                    } else {
                        lp2.gravity = st.gravity;
                    }
                    lp2.windowAnimations = st.windowAnimations;
                    wm.addView(st.decorView, lp2);
                    st.isOpen = true;
                    return;
                }
                return;
            }
            closePanel(st, true);
        }
    }

    public final void closePanel(int featureId) {
        if (featureId == 0 && this.mDecorContentParent != null && this.mDecorContentParent.canShowOverflowMenu() && CheckPermanentMenuKey()) {
            this.mDecorContentParent.hideOverflowMenu();
        } else if (featureId == 6) {
            closeContextMenu();
        } else {
            closePanel(getPanelState(featureId, true), true);
        }
    }

    public final void closePanel(PanelFeatureState st, boolean doCallback) {
        if (doCallback && st.featureId == 0 && this.mDecorContentParent != null && this.mDecorContentParent.isOverflowMenuShowing()) {
            checkCloseActionMenu(st.menu);
            return;
        }
        ViewManager wm = getWindowManager();
        if (wm != null && st.isOpen) {
            if (st.decorView != null) {
                wm.removeView(st.decorView);
                if (st.isCompact) {
                    sRotationWatcher.removeWindow(this);
                }
            }
            if (doCallback) {
                callOnPanelClosed(st.featureId, st, null);
            }
        }
        st.isPrepared = SWEEP_OPEN_MENU;
        st.isHandled = SWEEP_OPEN_MENU;
        st.isOpen = SWEEP_OPEN_MENU;
        st.shownPanelView = null;
        if (st.isInExpandedMode) {
            st.refreshDecorView = true;
            st.isInExpandedMode = SWEEP_OPEN_MENU;
        }
        if (this.mPreparedPanel == st) {
            this.mPreparedPanel = null;
            this.mPanelChordingKey = 0;
        }
    }

    void checkCloseActionMenu(Menu menu) {
        if (!this.mClosingActionMenu) {
            this.mClosingActionMenu = true;
            this.mDecorContentParent.dismissPopups();
            Window.Callback cb = getCallback();
            if (!(cb == null || isDestroyed())) {
                cb.onPanelClosed(8, menu);
            }
            this.mClosingActionMenu = SWEEP_OPEN_MENU;
        }
    }

    public final void togglePanel(int featureId, KeyEvent event) {
        PanelFeatureState st = getPanelState(featureId, true);
        if (st.isOpen) {
            closePanel(st, true);
        } else {
            openPanel(st, event);
        }
    }

    public void invalidatePanelMenu(int featureId) {
        this.mInvalidatePanelMenuFeatures |= FLAG_RESOURCE_SET_ICON << featureId;
        if (!this.mInvalidatePanelMenuPosted && this.mDecor != null) {
            this.mDecor.postOnAnimation(this.mInvalidatePanelMenuRunnable);
            this.mInvalidatePanelMenuPosted = true;
        }
    }

    void doPendingInvalidatePanelMenu() {
        if (this.mInvalidatePanelMenuPosted) {
            this.mDecor.removeCallbacks(this.mInvalidatePanelMenuRunnable);
            this.mInvalidatePanelMenuRunnable.run();
        }
    }

    void doInvalidatePanelMenu(int featureId) {
        PanelFeatureState st = getPanelState(featureId, SWEEP_OPEN_MENU);
        if (st != null) {
            if (st.menu != null) {
                Bundle savedActionViewStates = new Bundle();
                st.menu.saveActionViewStates(savedActionViewStates);
                if (savedActionViewStates.size() > 0) {
                    st.frozenActionViewState = savedActionViewStates;
                }
                st.menu.stopDispatchingItemsChanged();
                st.menu.clear();
            }
            st.refreshMenuContent = true;
            st.refreshDecorView = true;
            if ((featureId == 8 || featureId == 0) && this.mDecorContentParent != null) {
                st = getPanelState(0, SWEEP_OPEN_MENU);
                if (st != null) {
                    st.isPrepared = SWEEP_OPEN_MENU;
                    preparePanel(st, null);
                }
            }
        }
    }

    public final boolean onKeyDownPanel(int featureId, KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0) {
            this.mPanelChordingKey = keyCode;
            PanelFeatureState st = getPanelState(featureId, SWEEP_OPEN_MENU);
            if (!(st == null || st.isOpen)) {
                return preparePanel(st, event);
            }
        }
        return SWEEP_OPEN_MENU;
    }

    public final void onKeyUpPanel(int featureId, KeyEvent event) {
        int keyCode = event.getScanCode();
        if (this.mPanelChordingKey != 0) {
            this.mPanelChordingKey = 0;
            PanelFeatureState st = getPanelState(featureId, SWEEP_OPEN_MENU);
            if (!event.isCanceled() && ((this.mDecor == null || this.mDecor.mPrimaryActionMode == null) && st != null)) {
                boolean playSoundEffect = SWEEP_OPEN_MENU;
                if (featureId == 0 && this.mDecorContentParent != null && this.mDecorContentParent.canShowOverflowMenu() && CheckPermanentMenuKey()) {
                    if (this.mDecorContentParent.isOverflowMenuShowing()) {
                        playSoundEffect = this.mDecorContentParent.hideOverflowMenu();
                    } else if (!isDestroyed() && preparePanel(st, event)) {
                        playSoundEffect = this.mDecorContentParent.showOverflowMenu();
                    }
                } else if (st.isOpen || st.isHandled) {
                    playSoundEffect = (!st.isOpen || keyCode == 0) ? SWEEP_OPEN_MENU : true;
                    closePanel(st, true);
                } else if (st.isPrepared) {
                    boolean show = true;
                    if (st.refreshMenuContent) {
                        st.isPrepared = SWEEP_OPEN_MENU;
                        show = preparePanel(st, event);
                    }
                    if (show) {
                        EventLog.writeEvent(50001, 0);
                        openPanel(st, event);
                        playSoundEffect = true;
                    }
                }
                if (playSoundEffect) {
                    AudioManager audioManager = (AudioManager) getContext().getSystemService("audio");
                    if (audioManager != null) {
                        audioManager.playSoundEffect(0);
                    } else {
                        Log.w(TAG, "Couldn't get audio manager");
                    }
                }
            }
        }
    }

    public final void closeAllPanels() {
        if (getWindowManager() != null) {
            PanelFeatureState[] panels = this.mPanels;
            int N = panels != null ? panels.length : 0;
            for (int i = 0; i < N; i += FLAG_RESOURCE_SET_ICON) {
                PanelFeatureState panel = panels[i];
                if (panel != null) {
                    closePanel(panel, true);
                }
            }
            closeContextMenu();
            closeSubMenu();
        }
    }

    private void closeSubMenu() {
        if (this.mDecorContentParent != null) {
            this.mDecorContentParent.dismissPopups();
        }
    }

    private synchronized void closeContextMenu() {
        if (this.mContextMenu != null) {
            this.mContextMenu.close();
            dismissContextMenu();
        }
    }

    private synchronized void dismissContextMenu() {
        this.mContextMenu = null;
        if (this.mContextMenuHelper != null) {
            this.mContextMenuHelper.dismiss();
            this.mContextMenuHelper = null;
        }
    }

    public boolean performPanelShortcut(int featureId, int keyCode, KeyEvent event, int flags) {
        return performPanelShortcut(getPanelState(featureId, SWEEP_OPEN_MENU), keyCode, event, flags);
    }

    boolean performPanelShortcut(PanelFeatureState st, int keyCode, KeyEvent event, int flags) {
        if (event.isSystem() || st == null) {
            return SWEEP_OPEN_MENU;
        }
        boolean handled = SWEEP_OPEN_MENU;
        if ((st.isPrepared || preparePanel(st, event)) && st.menu != null) {
            handled = st.menu.performShortcut(keyCode, event, flags);
        }
        if (handled) {
            st.isHandled = true;
            if ((flags & FLAG_RESOURCE_SET_ICON) == 0 && this.mDecorContentParent == null) {
                closePanel(st, true);
            }
        }
        return handled;
    }

    public boolean performPanelIdentifierAction(int featureId, int id, int flags) {
        PanelFeatureState st = getPanelState(featureId, true);
        if (!preparePanel(st, new KeyEvent(0, 82)) || st.menu == null) {
            return SWEEP_OPEN_MENU;
        }
        boolean res = st.menu.performIdentifierAction(id, flags);
        if (this.mDecorContentParent == null) {
            closePanel(st, true);
        }
        return res;
    }

    public PanelFeatureState findMenuPanel(Menu menu) {
        PanelFeatureState[] panels = this.mPanels;
        int N = panels != null ? panels.length : 0;
        for (int i = 0; i < N; i += FLAG_RESOURCE_SET_ICON) {
            PanelFeatureState panel = panels[i];
            if (panel != null && panel.menu == menu) {
                return panel;
            }
        }
        return null;
    }

    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
        Window.Callback cb = getCallback();
        if (!(cb == null || isDestroyed())) {
            PanelFeatureState panel = findMenuPanel(menu.getRootMenu());
            if (panel != null) {
                return cb.onMenuItemSelected(panel.featureId, item);
            }
        }
        return SWEEP_OPEN_MENU;
    }

    public void onMenuModeChange(MenuBuilder menu) {
        reopenMenu(true);
    }

    private void reopenMenu(boolean toggleMenuMode) {
        PanelFeatureState st;
        if (this.mDecorContentParent != null && this.mDecorContentParent.canShowOverflowMenu() && (CheckPermanentMenuKey() || this.mDecorContentParent.isOverflowMenuShowPending())) {
            Window.Callback cb = getCallback();
            if (this.mDecorContentParent.isOverflowMenuShowing() && toggleMenuMode) {
                this.mDecorContentParent.hideOverflowMenu();
                st = getPanelState(0, SWEEP_OPEN_MENU);
                if (!(st == null || cb == null || isDestroyed())) {
                    cb.onPanelClosed(8, st.menu);
                }
            } else if (!(cb == null || isDestroyed())) {
                if (this.mInvalidatePanelMenuPosted && (this.mInvalidatePanelMenuFeatures & FLAG_RESOURCE_SET_ICON) != 0) {
                    this.mDecor.removeCallbacks(this.mInvalidatePanelMenuRunnable);
                    this.mInvalidatePanelMenuRunnable.run();
                }
                st = getPanelState(0, SWEEP_OPEN_MENU);
                if (!(st == null || st.menu == null || st.refreshMenuContent || !cb.onPreparePanel(0, st.createdPanelView, st.menu))) {
                    cb.onMenuOpened(8, st.menu);
                    this.mDecorContentParent.showOverflowMenu();
                }
            }
            return;
        }
        st = getPanelState(0, SWEEP_OPEN_MENU);
        if (st != null) {
            boolean z = toggleMenuMode ? st.isInExpandedMode ? SWEEP_OPEN_MENU : true : st.isInExpandedMode;
            st.refreshDecorView = true;
            closePanel(st, SWEEP_OPEN_MENU);
            st.isInExpandedMode = z;
            openPanel(st, null);
        }
    }

    protected boolean initializePanelMenu(PanelFeatureState st) {
        Context context = getContext();
        if ((st.featureId == 0 || st.featureId == 8) && this.mDecorContentParent != null) {
            TypedValue outValue = new TypedValue();
            Theme baseTheme = context.getTheme();
            baseTheme.resolveAttribute(R.attr.actionBarTheme, outValue, true);
            Theme theme = null;
            if (outValue.resourceId != 0) {
                theme = context.getResources().newTheme();
                theme.setTo(baseTheme);
                theme.applyStyle(outValue.resourceId, true);
                theme.resolveAttribute(R.attr.actionBarWidgetTheme, outValue, true);
            } else {
                baseTheme.resolveAttribute(R.attr.actionBarWidgetTheme, outValue, true);
            }
            if (outValue.resourceId != 0) {
                if (theme == null) {
                    theme = context.getResources().newTheme();
                    theme.setTo(baseTheme);
                }
                theme.applyStyle(outValue.resourceId, true);
            }
            if (theme != null) {
                Context context2 = new ContextThemeWrapper(context, 0);
                context2.getTheme().setTo(theme);
                context = context2;
            }
        }
        MenuBuilder menu = new MenuBuilder(context);
        menu.setCallback(this);
        st.setMenu(menu);
        return true;
    }

    protected boolean initializePanelDecor(PanelFeatureState st) {
        st.decorView = generateDecor(st.featureId);
        st.gravity = 81;
        st.setStyle(getContext());
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.Window, 0, st.listPresenterTheme);
        float elevation = a.getDimension(38, 0.0f);
        if (elevation != 0.0f) {
            st.decorView.setElevation(elevation);
        }
        a.recycle();
        return true;
    }

    private int getOptionsPanelGravity() {
        try {
            return WindowManagerHolder.sWindowManager.getPreferredOptionsPanelGravity();
        } catch (RemoteException ex) {
            Log.e(TAG, "Couldn't getOptionsPanelGravity; using default", ex);
            return 81;
        }
    }

    void onOptionsPanelRotationChanged() {
        WindowManager.LayoutParams lp = null;
        PanelFeatureState st = getPanelState(0, SWEEP_OPEN_MENU);
        if (st != null) {
            if (st.decorView != null) {
                lp = (WindowManager.LayoutParams) st.decorView.getLayoutParams();
            }
            if (lp != null) {
                lp.gravity = getOptionsPanelGravity();
                ViewManager wm = getWindowManager();
                if (wm != null) {
                    wm.updateViewLayout(st.decorView, lp);
                }
            }
        }
    }

    protected boolean initializePanelContent(PanelFeatureState st) {
        if (st.createdPanelView != null) {
            st.shownPanelView = st.createdPanelView;
            return true;
        } else if (st.menu == null) {
            return SWEEP_OPEN_MENU;
        } else {
            MenuView menuView;
            if (this.mPanelMenuPresenterCallback == null) {
                this.mPanelMenuPresenterCallback = new PanelMenuPresenterCallback();
            }
            if (st.isInListMode()) {
                menuView = st.getListMenuView(getContext(), this.mPanelMenuPresenterCallback);
            } else {
                menuView = st.getIconMenuView(getContext(), this.mPanelMenuPresenterCallback);
            }
            st.shownPanelView = (View) menuView;
            if (st.shownPanelView == null) {
                return SWEEP_OPEN_MENU;
            }
            int defaultAnimations = menuView.getWindowAnimations();
            if (defaultAnimations != 0) {
                st.windowAnimations = defaultAnimations;
            }
            return true;
        }
    }

    public boolean performContextMenuIdentifierAction(int id, int flags) {
        return this.mContextMenu != null ? this.mContextMenu.performIdentifierAction(id, flags) : SWEEP_OPEN_MENU;
    }

    public final void setElevation(float elevation) {
        this.mElevation = elevation;
        WindowManager.LayoutParams attrs = getAttributes();
        if (this.mDecor != null) {
            this.mDecor.setElevation(elevation);
            attrs.setSurfaceInsets(this.mDecor, true, SWEEP_OPEN_MENU);
        }
        dispatchWindowAttributesChanged(attrs);
    }

    public float getElevation() {
        return this.mElevation;
    }

    public final void setClipToOutline(boolean clipToOutline) {
        this.mClipToOutline = clipToOutline;
        if (this.mDecor != null) {
            this.mDecor.setClipToOutline(clipToOutline);
        }
    }

    public final void setBackgroundDrawable(Drawable drawable) {
        int i = 0;
        if (drawable != this.mBackgroundDrawable || this.mBackgroundResource != 0) {
            this.mBackgroundResource = 0;
            this.mBackgroundDrawable = drawable;
            if (this.mDecor != null) {
                this.mDecor.setWindowBackground(drawable);
            }
            if (this.mBackgroundFallbackResource != 0) {
                DecorView decorView = this.mDecor;
                if (drawable == null) {
                    i = this.mBackgroundFallbackResource;
                }
                decorView.setBackgroundFallback(i);
            }
        }
    }

    public final void setFeatureDrawableResource(int featureId, int resId) {
        if (resId != 0) {
            DrawableFeatureState st = getDrawableState(featureId, true);
            if (st.resid != resId) {
                st.resid = resId;
                st.uri = null;
                st.local = getContext().getDrawable(resId);
                updateDrawable(featureId, st, SWEEP_OPEN_MENU);
                return;
            }
            return;
        }
        setFeatureDrawable(featureId, null);
    }

    public final void setFeatureDrawableUri(int featureId, Uri uri) {
        if (uri != null) {
            DrawableFeatureState st = getDrawableState(featureId, true);
            if (st.uri == null || !st.uri.equals(uri)) {
                st.resid = 0;
                st.uri = uri;
                st.local = loadImageURI(uri);
                updateDrawable(featureId, st, SWEEP_OPEN_MENU);
                return;
            }
            return;
        }
        setFeatureDrawable(featureId, null);
    }

    public final void setFeatureDrawable(int featureId, Drawable drawable) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        st.resid = 0;
        st.uri = null;
        if (st.local != drawable) {
            st.local = drawable;
            updateDrawable(featureId, st, SWEEP_OPEN_MENU);
        }
    }

    public void setFeatureDrawableAlpha(int featureId, int alpha) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        if (st.alpha != alpha) {
            st.alpha = alpha;
            updateDrawable(featureId, st, SWEEP_OPEN_MENU);
        }
    }

    protected final void setFeatureDefaultDrawable(int featureId, Drawable drawable) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        if (st.def != drawable) {
            st.def = drawable;
            updateDrawable(featureId, st, SWEEP_OPEN_MENU);
        }
    }

    public final void setFeatureInt(int featureId, int value) {
        updateInt(featureId, value, SWEEP_OPEN_MENU);
    }

    protected final void updateDrawable(int featureId, boolean fromActive) {
        DrawableFeatureState st = getDrawableState(featureId, SWEEP_OPEN_MENU);
        if (st != null) {
            updateDrawable(featureId, st, fromActive);
        }
    }

    protected void onDrawableChanged(int featureId, Drawable drawable, int alpha) {
        ImageView view;
        if (featureId == 3) {
            view = getLeftIconView();
        } else if (featureId == FLAG_RESOURCE_SET_ICON_FALLBACK) {
            view = getRightIconView();
        } else {
            return;
        }
        if (drawable != null) {
            drawable.setAlpha(alpha);
            view.setImageDrawable(drawable);
            view.setVisibility(0);
        } else {
            view.setVisibility(8);
        }
    }

    protected void onIntChanged(int featureId, int value) {
        if (featureId == FLAG_RESOURCE_SET_LOGO || featureId == 5) {
            updateProgressBars(value);
        } else if (featureId == 7) {
            ViewGroup titleContainer = (FrameLayout) findViewById(R.id.title_container);
            if (titleContainer != null) {
                this.mLayoutInflater.inflate(value, titleContainer);
            }
        }
    }

    private void updateProgressBars(int value) {
        ProgressBar circularProgressBar = getCircularProgressBar(true);
        ProgressBar horizontalProgressBar = getHorizontalProgressBar(true);
        int features = getLocalFeatures();
        if (value == -1) {
            if ((features & FLAG_RESOURCE_SET_ICON_FALLBACK) != 0) {
                if (horizontalProgressBar != null) {
                    int visibility;
                    int level = horizontalProgressBar.getProgress();
                    if (horizontalProgressBar.isIndeterminate() || level < PGAction.PG_ID_DEFAULT_FRONT) {
                        visibility = 0;
                    } else {
                        visibility = FLAG_RESOURCE_SET_ICON_FALLBACK;
                    }
                    horizontalProgressBar.setVisibility(visibility);
                } else {
                    Log.e(TAG, "Horizontal progress bar not located in current window decor");
                }
            }
            if ((features & 32) == 0) {
                return;
            }
            if (circularProgressBar != null) {
                circularProgressBar.setVisibility(0);
            } else {
                Log.e(TAG, "Circular progress bar not located in current window decor");
            }
        } else if (value == -2) {
            if ((features & FLAG_RESOURCE_SET_ICON_FALLBACK) != 0) {
                if (horizontalProgressBar != null) {
                    horizontalProgressBar.setVisibility(8);
                } else {
                    Log.e(TAG, "Horizontal progress bar not located in current window decor");
                }
            }
            if ((features & 32) == 0) {
                return;
            }
            if (circularProgressBar != null) {
                circularProgressBar.setVisibility(8);
            } else {
                Log.e(TAG, "Circular progress bar not located in current window decor");
            }
        } else if (value == -3) {
            if (horizontalProgressBar != null) {
                horizontalProgressBar.setIndeterminate(true);
            } else {
                Log.e(TAG, "Horizontal progress bar not located in current window decor");
            }
        } else if (value == -4) {
            if (horizontalProgressBar != null) {
                horizontalProgressBar.setIndeterminate(SWEEP_OPEN_MENU);
            } else {
                Log.e(TAG, "Horizontal progress bar not located in current window decor");
            }
        } else if (value >= 0 && value <= PGAction.PG_ID_DEFAULT_FRONT) {
            if (horizontalProgressBar != null) {
                horizontalProgressBar.setProgress(value + 0);
            } else {
                Log.e(TAG, "Horizontal progress bar not located in current window decor");
            }
            if (value < PGAction.PG_ID_DEFAULT_FRONT) {
                showProgressBars(horizontalProgressBar, circularProgressBar);
            } else {
                hideProgressBars(horizontalProgressBar, circularProgressBar);
            }
        } else if (PGAction.PG_ID_ALARM_START <= value && value <= PGAction.PG_ID_USER_SLEEPING) {
            if (horizontalProgressBar != null) {
                horizontalProgressBar.setSecondaryProgress(value - 20000);
            } else {
                Log.e(TAG, "Horizontal progress bar not located in current window decor");
            }
            showProgressBars(horizontalProgressBar, circularProgressBar);
        }
    }

    private void showProgressBars(ProgressBar horizontalProgressBar, ProgressBar spinnyProgressBar) {
        int features = getLocalFeatures();
        if (!((features & 32) == 0 || spinnyProgressBar == null || spinnyProgressBar.getVisibility() != FLAG_RESOURCE_SET_ICON_FALLBACK)) {
            spinnyProgressBar.setVisibility(0);
        }
        if ((features & FLAG_RESOURCE_SET_ICON_FALLBACK) != 0 && horizontalProgressBar != null && horizontalProgressBar.getProgress() < PGAction.PG_ID_DEFAULT_FRONT) {
            horizontalProgressBar.setVisibility(0);
        }
    }

    private void hideProgressBars(ProgressBar horizontalProgressBar, ProgressBar spinnyProgressBar) {
        int features = getLocalFeatures();
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        anim.setDuration(1000);
        if (!((features & 32) == 0 || spinnyProgressBar == null || spinnyProgressBar.getVisibility() != 0)) {
            spinnyProgressBar.startAnimation(anim);
            spinnyProgressBar.setVisibility(FLAG_RESOURCE_SET_ICON_FALLBACK);
        }
        if ((features & FLAG_RESOURCE_SET_ICON_FALLBACK) != 0 && horizontalProgressBar != null && horizontalProgressBar.getVisibility() == 0) {
            horizontalProgressBar.startAnimation(anim);
            horizontalProgressBar.setVisibility(FLAG_RESOURCE_SET_ICON_FALLBACK);
        }
    }

    public void setIcon(int resId) {
        this.mIconRes = resId;
        this.mResourcesSetFlags |= FLAG_RESOURCE_SET_ICON;
        this.mResourcesSetFlags &= -5;
        if (this.mDecorContentParent != null) {
            this.mDecorContentParent.setIcon(resId);
        }
    }

    public void setDefaultIcon(int resId) {
        if ((this.mResourcesSetFlags & FLAG_RESOURCE_SET_ICON) == 0) {
            this.mIconRes = resId;
            if (!(this.mDecorContentParent == null || (this.mDecorContentParent.hasIcon() && (this.mResourcesSetFlags & FLAG_RESOURCE_SET_ICON_FALLBACK) == 0))) {
                if (resId != 0) {
                    this.mDecorContentParent.setIcon(resId);
                    this.mResourcesSetFlags &= -5;
                } else {
                    this.mDecorContentParent.setIcon(getContext().getPackageManager().getDefaultActivityIcon());
                    this.mResourcesSetFlags |= FLAG_RESOURCE_SET_ICON_FALLBACK;
                }
            }
        }
    }

    public void setLogo(int resId) {
        this.mLogoRes = resId;
        this.mResourcesSetFlags |= FLAG_RESOURCE_SET_LOGO;
        if (this.mDecorContentParent != null) {
            this.mDecorContentParent.setLogo(resId);
        }
    }

    public void setDefaultLogo(int resId) {
        if ((this.mResourcesSetFlags & FLAG_RESOURCE_SET_LOGO) == 0) {
            this.mLogoRes = resId;
            if (!(this.mDecorContentParent == null || this.mDecorContentParent.hasLogo())) {
                this.mDecorContentParent.setLogo(resId);
            }
        }
    }

    public void setLocalFocus(boolean hasFocus, boolean inTouchMode) {
        getViewRootImpl().windowFocusChanged(hasFocus, inTouchMode);
    }

    public void injectInputEvent(InputEvent event) {
        getViewRootImpl().dispatchInputEvent(event);
    }

    private ViewRootImpl getViewRootImpl() {
        if (this.mDecor != null) {
            ViewRootImpl viewRootImpl = this.mDecor.getViewRootImpl();
            if (viewRootImpl != null) {
                return viewRootImpl;
            }
        }
        throw new IllegalStateException("view not added");
    }

    public void takeKeyEvents(boolean get) {
        this.mDecor.setFocusable(get);
    }

    public boolean superDispatchKeyEvent(KeyEvent event) {
        return this.mDecor.superDispatchKeyEvent(event);
    }

    public boolean superDispatchKeyShortcutEvent(KeyEvent event) {
        return this.mDecor.superDispatchKeyShortcutEvent(event);
    }

    public boolean superDispatchTouchEvent(MotionEvent event) {
        return this.mDecor.superDispatchTouchEvent(event);
    }

    public boolean superDispatchTrackballEvent(MotionEvent event) {
        return this.mDecor.superDispatchTrackballEvent(event);
    }

    public boolean superDispatchGenericMotionEvent(MotionEvent event) {
        return this.mDecor.superDispatchGenericMotionEvent(event);
    }

    protected boolean onKeyDown(int featureId, int keyCode, KeyEvent event) {
        DispatcherState keyDispatcherState = this.mDecor != null ? this.mDecor.getKeyDispatcherState() : null;
        switch (keyCode) {
            case FLAG_RESOURCE_SET_ICON_FALLBACK /*4*/:
                if (event.getRepeatCount() <= 0 && featureId >= 0) {
                    if (keyDispatcherState != null) {
                        keyDispatcherState.startTracking(event, this);
                    }
                    return true;
                }
            case HwPerformance.PERF_TAG_DEF_B_CPU_MAX /*24*/:
            case PerfHub.PERF_TAG_DEF_GPU_MIN /*25*/:
            case LogPower.AUDIO_SESSION_START /*164*/:
                int direction = 0;
                switch (keyCode) {
                    case HwPerformance.PERF_TAG_DEF_B_CPU_MAX /*24*/:
                        direction = FLAG_RESOURCE_SET_ICON;
                        break;
                    case PerfHub.PERF_TAG_DEF_GPU_MIN /*25*/:
                        direction = -1;
                        break;
                    case LogPower.AUDIO_SESSION_START /*164*/:
                        direction = LogPower.ACTIVITY_PAUSED;
                        break;
                }
                if (this.mMediaController != null) {
                    this.mMediaController.adjustVolume(direction, FLAG_RESOURCE_SET_ICON);
                } else {
                    MediaSessionLegacyHelper.getHelper(getContext()).sendAdjustVolumeBy(this.mVolumeControlStreamType, direction, 4113);
                }
                return true;
            case RILConstants.RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE /*79*/:
            case RILConstants.RIL_REQUEST_CDMA_BURST_DTMF /*85*/:
            case RILConstants.RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY /*86*/:
            case RILConstants.RIL_REQUEST_CDMA_SEND_SMS /*87*/:
            case RILConstants.RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE /*88*/:
            case RILConstants.RIL_REQUEST_GSM_GET_BROADCAST_CONFIG /*89*/:
            case StatisticalConstant.TYPE_TOUCH_FORCE_END /*90*/:
            case StatisticalConstant.TYPE_SHARED_TARGET /*91*/:
            case LogPower.TOUCH_UP /*126*/:
            case LogPower.MIME_TYPE /*127*/:
            case LogPower.END_CHG_ROTATION /*130*/:
                return (this.mMediaController == null || !this.mMediaController.dispatchMediaButtonEvent(event)) ? SWEEP_OPEN_MENU : true;
            case StatisticalConstant.TYPE_TOUCH_FORCE_OPEAN_APPLICATION /*82*/:
                if (featureId < 0) {
                    featureId = 0;
                }
                onKeyDownPanel(featureId, event);
                return true;
        }
        return SWEEP_OPEN_MENU;
    }

    private KeyguardManager getKeyguardManager() {
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) getContext().getSystemService("keyguard");
        }
        return this.mKeyguardManager;
    }

    AudioManager getAudioManager() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
        }
        return this.mAudioManager;
    }

    protected boolean onKeyUp(int featureId, int keyCode, KeyEvent event) {
        DispatcherState dispatcher = null;
        if (this.mDecor != null) {
            dispatcher = this.mDecor.getKeyDispatcherState();
        }
        if (dispatcher != null) {
            dispatcher.handleUpEvent(event);
        }
        switch (keyCode) {
            case FLAG_RESOURCE_SET_ICON_FALLBACK /*4*/:
                if (featureId >= 0 && event.isTracking() && !event.isCanceled()) {
                    if (featureId == 0) {
                        PanelFeatureState st = getPanelState(featureId, SWEEP_OPEN_MENU);
                        if (st != null && st.isInExpandedMode) {
                            reopenMenu(true);
                            return true;
                        }
                    }
                    closePanel(featureId);
                    return true;
                }
            case HwPerformance.PERF_TAG_DEF_B_CPU_MAX /*24*/:
            case PerfHub.PERF_TAG_DEF_GPU_MIN /*25*/:
                if (this.mMediaController != null) {
                    this.mMediaController.adjustVolume(0, 4116);
                } else {
                    MediaSessionLegacyHelper.getHelper(getContext()).sendAdjustVolumeBy(this.mVolumeControlStreamType, 0, 4116);
                }
                return true;
            case RILConstants.RIL_REQUEST_CDMA_QUERY_ROAMING_PREFERENCE /*79*/:
            case RILConstants.RIL_REQUEST_CDMA_BURST_DTMF /*85*/:
            case RILConstants.RIL_REQUEST_CDMA_VALIDATE_AND_WRITE_AKEY /*86*/:
            case RILConstants.RIL_REQUEST_CDMA_SEND_SMS /*87*/:
            case RILConstants.RIL_REQUEST_CDMA_SMS_ACKNOWLEDGE /*88*/:
            case RILConstants.RIL_REQUEST_GSM_GET_BROADCAST_CONFIG /*89*/:
            case StatisticalConstant.TYPE_TOUCH_FORCE_END /*90*/:
            case StatisticalConstant.TYPE_SHARED_TARGET /*91*/:
            case LogPower.TOUCH_UP /*126*/:
            case LogPower.MIME_TYPE /*127*/:
            case LogPower.END_CHG_ROTATION /*130*/:
                return (this.mMediaController == null || !this.mMediaController.dispatchMediaButtonEvent(event)) ? SWEEP_OPEN_MENU : true;
            case StatisticalConstant.TYPE_TOUCH_FORCE_OPEAN_APPLICATION /*82*/:
                if (featureId < 0) {
                    featureId = 0;
                }
                onKeyUpPanel(featureId, event);
                return true;
            case RILConstants.RIL_REQUEST_CDMA_FLASH /*84*/:
                if (!getKeyguardManager().inKeyguardRestrictedInputMode()) {
                    if (event.isTracking() && !event.isCanceled()) {
                        launchDefaultSearch(event);
                    }
                    return true;
                }
                break;
            case LogPower.AUDIO_SESSION_START /*164*/:
                getAudioManager().handleKeyUp(event, this.mVolumeControlStreamType);
                return true;
            case LogPower.LOW_POWER_AUDIO_RESET /*171*/:
                if (this.mSupportsPictureInPicture && !event.isCanceled()) {
                    getWindowControllerCallback().enterPictureInPictureModeIfPossible();
                }
                return true;
        }
        return SWEEP_OPEN_MENU;
    }

    protected void onActive() {
    }

    public final View getDecorView() {
        if (this.mDecor == null || this.mForceDecorInstall) {
            installDecor();
        }
        return this.mDecor;
    }

    public final View peekDecorView() {
        return this.mDecor;
    }

    public Bundle saveHierarchyState() {
        Bundle outState = new Bundle();
        if (this.mContentParent == null) {
            return outState;
        }
        SparseArray<Parcelable> states = new SparseArray();
        this.mContentParent.saveHierarchyState(states);
        outState.putSparseParcelableArray(VIEWS_TAG, states);
        View focusedView = this.mContentParent.findFocus();
        if (!(focusedView == null || focusedView.getId() == -1)) {
            outState.putInt(FOCUSED_ID_TAG, focusedView.getId());
        }
        if (this.mDecor != null) {
            ViewRootImpl viewRootImpl = this.mDecor.getViewRootImpl();
            if (viewRootImpl != null) {
                View accessFocusHost = viewRootImpl.getAccessibilityFocusedHost();
                if (!(accessFocusHost == null || accessFocusHost.getId() == -1)) {
                    outState.putInt(ACCESSIBILITY_FOCUSED_ID_TAG, accessFocusHost.getId());
                    AccessibilityNodeInfo accessFocusedNode = viewRootImpl.getAccessibilityFocusedVirtualView();
                    if (accessFocusedNode != null) {
                        outState.putInt(ACCESSIBILITY_FOCUSED_VIRTUAL_ID_TAG, AccessibilityNodeInfo.getVirtualDescendantId(accessFocusedNode.getSourceNodeId()));
                    }
                }
            }
        }
        SparseArray<Parcelable> panelStates = new SparseArray();
        savePanelState(panelStates);
        if (panelStates.size() > 0) {
            outState.putSparseParcelableArray(PANELS_TAG, panelStates);
        }
        if (this.mDecorContentParent != null) {
            SparseArray<Parcelable> actionBarStates = new SparseArray();
            this.mDecorContentParent.saveToolbarHierarchyState(actionBarStates);
            outState.putSparseParcelableArray(ACTION_BAR_TAG, actionBarStates);
        }
        return outState;
    }

    public void restoreHierarchyState(Bundle savedInstanceState) {
        if (this.mContentParent != null) {
            SparseArray<Parcelable> savedStates = savedInstanceState.getSparseParcelableArray(VIEWS_TAG);
            if (savedStates != null) {
                this.mContentParent.restoreHierarchyState(savedStates);
            }
            int focusedViewId = savedInstanceState.getInt(FOCUSED_ID_TAG, -1);
            if (focusedViewId != -1) {
                View needsFocus = this.mContentParent.findViewById(focusedViewId);
                if (needsFocus != null) {
                    needsFocus.requestFocus();
                } else {
                    Log.w(TAG, "Previously focused view reported id " + focusedViewId + " during save, but can't be found during restore.");
                }
            }
            tryRestoreAccessibilityFocus(savedInstanceState.getInt(ACCESSIBILITY_FOCUSED_ID_TAG, -1), savedInstanceState.getInt(ACCESSIBILITY_FOCUSED_VIRTUAL_ID_TAG, HwBootFail.STAGE_BOOT_SUCCESS));
            SparseArray<Parcelable> panelStates = savedInstanceState.getSparseParcelableArray(PANELS_TAG);
            if (panelStates != null) {
                restorePanelState(panelStates);
            }
            if (this.mDecorContentParent != null) {
                SparseArray<Parcelable> actionBarStates = savedInstanceState.getSparseParcelableArray(ACTION_BAR_TAG);
                if (actionBarStates != null) {
                    doPendingInvalidatePanelMenu();
                    this.mDecorContentParent.restoreToolbarHierarchyState(actionBarStates);
                } else {
                    Log.w(TAG, "Missing saved instance states for action bar views! State will not be restored.");
                }
            }
        }
    }

    private void tryRestoreAccessibilityFocus(int hostViewId, int virtualViewId) {
        if (hostViewId != -1 && this.mDecor != null) {
            View needsAccessFocus = this.mDecor.findViewById(hostViewId);
            if (needsAccessFocus == null) {
                Log.w(TAG, "Previously accessibility focused view reported id " + hostViewId + " during save, but can't be found during restore.");
            } else if (!tryFocusingVirtualView(needsAccessFocus, virtualViewId) && !needsAccessFocus.requestAccessibilityFocus()) {
                Log.w(TAG, "Failed to restore focus to previously accessibility focused view with id " + hostViewId);
            }
        }
    }

    private boolean tryFocusingVirtualView(View host, int virtualViewId) {
        if (virtualViewId != HwBootFail.STAGE_BOOT_SUCCESS) {
            AccessibilityNodeProvider nodeProvider = host.getAccessibilityNodeProvider();
            if (nodeProvider != null) {
                return nodeProvider.performAction(virtualViewId, 64, null);
            }
        }
        return SWEEP_OPEN_MENU;
    }

    private void savePanelState(SparseArray<Parcelable> icicles) {
        PanelFeatureState[] panels = this.mPanels;
        if (panels != null) {
            for (int curFeatureId = panels.length - 1; curFeatureId >= 0; curFeatureId--) {
                if (panels[curFeatureId] != null) {
                    icicles.put(curFeatureId, panels[curFeatureId].onSaveInstanceState());
                }
            }
        }
    }

    private void restorePanelState(SparseArray<Parcelable> icicles) {
        for (int i = icicles.size() - 1; i >= 0; i--) {
            int curFeatureId = icicles.keyAt(i);
            PanelFeatureState st = getPanelState(curFeatureId, SWEEP_OPEN_MENU);
            if (st != null) {
                st.onRestoreInstanceState((Parcelable) icicles.get(curFeatureId));
                invalidatePanelMenu(curFeatureId);
            }
        }
    }

    void openPanelsAfterRestore() {
        PanelFeatureState[] panels = this.mPanels;
        if (panels != null) {
            for (int i = panels.length - 1; i >= 0; i--) {
                PanelFeatureState st = panels[i];
                if (st != null) {
                    st.applyFrozenState();
                    if (!st.isOpen && st.wasLastOpen) {
                        st.isInExpandedMode = st.wasLastExpanded;
                        openPanel(st, null);
                    }
                }
            }
        }
    }

    protected DecorView generateDecor(int featureId) {
        Context context;
        if (this.mUseDecorContext) {
            Context applicationContext = getContext().getApplicationContext();
            if (applicationContext == null) {
                context = getContext();
            } else {
                context = new DecorContext(applicationContext, getContext().getResources());
                if (this.mTheme != -1) {
                    context.setTheme(this.mTheme);
                }
            }
        } else {
            context = getContext();
        }
        return new DecorView(context, featureId, this, getAttributes());
    }

    public void alwaysReadCloseOnTouchAttr() {
        this.mAlwaysReadCloseOnTouchAttr = true;
    }

    public void setSplitActionBarAlways(boolean bAlwaysSplit) {
        this.mAlwaysSplit = bAlwaysSplit;
    }

    private void installDecor() {
        this.mForceDecorInstall = SWEEP_OPEN_MENU;
        if (this.mDecor == null) {
            this.mDecor = generateDecor(-1);
            this.mDecor.setDescendantFocusability(Protocol.BASE_DATA_CONNECTION);
            this.mDecor.setIsRootNamespace(true);
            if (!(this.mInvalidatePanelMenuPosted || this.mInvalidatePanelMenuFeatures == 0)) {
                this.mDecor.postOnAnimation(this.mInvalidatePanelMenuRunnable);
            }
        } else {
            this.mDecor.setWindow(this);
        }
        if (this.mContentParent == null) {
            this.mContentParent = generateLayout(this.mDecor);
            this.mDecor.makeOptionalFitsSystemWindows();
            DecorContentParent decorContentParent = (DecorContentParent) this.mDecor.findViewById(R.id.decor_content_parent);
            if (decorContentParent != null) {
                this.mDecorContentParent = decorContentParent;
                this.mDecorContentParent.setWindowCallback(getCallback());
                if (this.mDecorContentParent.getTitle() == null) {
                    this.mDecorContentParent.setWindowTitle(this.mTitle);
                }
                int localFeatures = getLocalFeatures();
                for (int i = 0; i < 13; i += FLAG_RESOURCE_SET_ICON) {
                    if (((FLAG_RESOURCE_SET_ICON << i) & localFeatures) != 0) {
                        this.mDecorContentParent.initFeature(i);
                    }
                }
                if (this.mAlwaysSplit) {
                    this.mDecorContentParent.setSplitActionBarAlways(true);
                }
                this.mDecorContentParent.setUiOptions(this.mUiOptions);
                if ((this.mResourcesSetFlags & FLAG_RESOURCE_SET_ICON) != 0 || (this.mIconRes != 0 && !this.mDecorContentParent.hasIcon())) {
                    this.mDecorContentParent.setIcon(this.mIconRes);
                } else if ((this.mResourcesSetFlags & FLAG_RESOURCE_SET_ICON) == 0 && this.mIconRes == 0 && !this.mDecorContentParent.hasIcon()) {
                    this.mDecorContentParent.setIcon(getContext().getPackageManager().getDefaultActivityIcon());
                    this.mResourcesSetFlags |= FLAG_RESOURCE_SET_ICON_FALLBACK;
                }
                if (!((this.mResourcesSetFlags & FLAG_RESOURCE_SET_LOGO) == 0 && (this.mLogoRes == 0 || this.mDecorContentParent.hasLogo()))) {
                    this.mDecorContentParent.setLogo(this.mLogoRes);
                }
                PanelFeatureState st = getPanelState(0, SWEEP_OPEN_MENU);
                if (!isDestroyed() && ((st == null || st.menu == null) && !this.mIsStartingWindow)) {
                    invalidatePanelMenu(8);
                }
            } else {
                this.mTitleView = (TextView) findViewById(R.id.title);
                if (this.mTitleView != null) {
                    if ((getLocalFeatures() & FLAG_RESOURCE_SET_LOGO) != 0) {
                        View titleContainer = findViewById(R.id.title_container);
                        if (titleContainer != null) {
                            titleContainer.setVisibility(8);
                        } else {
                            this.mTitleView.setVisibility(8);
                        }
                        this.mContentParent.setForeground(null);
                    } else {
                        this.mTitleView.setText(this.mTitle);
                    }
                }
            }
            if (this.mDecor.getBackground() == null && this.mBackgroundFallbackResource != 0) {
                this.mDecor.setBackgroundFallback(this.mBackgroundFallbackResource);
            }
            if (hasFeature(13)) {
                if (this.mTransitionManager == null) {
                    int transitionRes = getWindowStyle().getResourceId(27, 0);
                    if (transitionRes != 0) {
                        this.mTransitionManager = TransitionInflater.from(getContext()).inflateTransitionManager(transitionRes, this.mContentParent);
                    } else {
                        this.mTransitionManager = new TransitionManager();
                    }
                }
                this.mEnterTransition = getTransition(this.mEnterTransition, null, 28);
                this.mReturnTransition = getTransition(this.mReturnTransition, USE_DEFAULT_TRANSITION, 40);
                this.mExitTransition = getTransition(this.mExitTransition, null, 29);
                this.mReenterTransition = getTransition(this.mReenterTransition, USE_DEFAULT_TRANSITION, 41);
                this.mSharedElementEnterTransition = getTransition(this.mSharedElementEnterTransition, null, 30);
                this.mSharedElementReturnTransition = getTransition(this.mSharedElementReturnTransition, USE_DEFAULT_TRANSITION, 42);
                this.mSharedElementExitTransition = getTransition(this.mSharedElementExitTransition, null, 31);
                this.mSharedElementReenterTransition = getTransition(this.mSharedElementReenterTransition, USE_DEFAULT_TRANSITION, 43);
                if (this.mAllowEnterTransitionOverlap == null) {
                    this.mAllowEnterTransitionOverlap = Boolean.valueOf(getWindowStyle().getBoolean(33, true));
                }
                if (this.mAllowReturnTransitionOverlap == null) {
                    this.mAllowReturnTransitionOverlap = Boolean.valueOf(getWindowStyle().getBoolean(32, true));
                }
                if (this.mBackgroundFadeDurationMillis < 0) {
                    this.mBackgroundFadeDurationMillis = (long) getWindowStyle().getInteger(37, DEFAULT_BACKGROUND_FADE_DURATION_MS);
                }
                if (this.mSharedElementsUseOverlay == null) {
                    this.mSharedElementsUseOverlay = Boolean.valueOf(getWindowStyle().getBoolean(44, true));
                }
            }
        }
    }

    private Transition getTransition(Transition currentValue, Transition defaultValue, int id) {
        if (currentValue != defaultValue) {
            return currentValue;
        }
        int transitionId = getWindowStyle().getResourceId(id, -1);
        Transition transition = defaultValue;
        if (!(transitionId == -1 || transitionId == R.transition.no_transition)) {
            transition = TransitionInflater.from(getContext()).inflateTransition(transitionId);
            if ((transition instanceof TransitionSet) && ((TransitionSet) transition).getTransitionCount() == 0) {
                transition = null;
            }
        }
        return transition;
    }

    private Drawable loadImageURI(Uri uri) {
        try {
            return Drawable.createFromStream(getContext().getContentResolver().openInputStream(uri), null);
        } catch (Exception e) {
            Log.w(TAG, "Unable to open content: " + uri);
            return null;
        }
    }

    private DrawableFeatureState getDrawableState(int featureId, boolean required) {
        if ((getFeatures() & (FLAG_RESOURCE_SET_ICON << featureId)) != 0) {
            DrawableFeatureState[] ar = this.mDrawables;
            if (ar == null || ar.length <= featureId) {
                DrawableFeatureState[] nar = new DrawableFeatureState[(featureId + FLAG_RESOURCE_SET_ICON)];
                if (ar != null) {
                    System.arraycopy(ar, 0, nar, 0, ar.length);
                }
                ar = nar;
                this.mDrawables = nar;
            }
            DrawableFeatureState st = ar[featureId];
            if (st == null) {
                st = new DrawableFeatureState(featureId);
                ar[featureId] = st;
            }
            return st;
        } else if (!required) {
            return null;
        } else {
            throw new RuntimeException("The feature has not been requested");
        }
    }

    PanelFeatureState getPanelState(int featureId, boolean required) {
        return getPanelState(featureId, required, null);
    }

    private PanelFeatureState getPanelState(int featureId, boolean required, PanelFeatureState convertPanelState) {
        if ((getFeatures() & (FLAG_RESOURCE_SET_ICON << featureId)) != 0) {
            PanelFeatureState[] ar = this.mPanels;
            if (ar == null || ar.length <= featureId) {
                PanelFeatureState[] nar = new PanelFeatureState[(featureId + FLAG_RESOURCE_SET_ICON)];
                if (ar != null) {
                    System.arraycopy(ar, 0, nar, 0, ar.length);
                }
                ar = nar;
                this.mPanels = nar;
            }
            PanelFeatureState st = ar[featureId];
            if (st == null) {
                if (convertPanelState != null) {
                    st = convertPanelState;
                } else {
                    st = new PanelFeatureState(featureId);
                }
                ar[featureId] = st;
            }
            return st;
        } else if (!required) {
            return null;
        } else {
            throw new RuntimeException("The feature has not been requested");
        }
    }

    public final void setChildDrawable(int featureId, Drawable drawable) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        st.child = drawable;
        updateDrawable(featureId, st, SWEEP_OPEN_MENU);
    }

    public final void setChildInt(int featureId, int value) {
        updateInt(featureId, value, SWEEP_OPEN_MENU);
    }

    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        PanelFeatureState st = getPanelState(0, SWEEP_OPEN_MENU);
        if (st == null || st.menu == null) {
            return SWEEP_OPEN_MENU;
        }
        return st.menu.isShortcutKey(keyCode, event);
    }

    private void updateDrawable(int featureId, DrawableFeatureState st, boolean fromResume) {
        if (this.mContentParent != null) {
            int featureMask = FLAG_RESOURCE_SET_ICON << featureId;
            if ((getFeatures() & featureMask) != 0 || fromResume) {
                Drawable drawable = null;
                if (st != null) {
                    drawable = st.child;
                    if (drawable == null) {
                        drawable = st.local;
                    }
                    if (drawable == null) {
                        drawable = st.def;
                    }
                }
                if ((getLocalFeatures() & featureMask) == 0) {
                    if (getContainer() != null && (isActive() || fromResume)) {
                        getContainer().setChildDrawable(featureId, drawable);
                    }
                } else if (!(st == null || (st.cur == drawable && st.curAlpha == st.alpha))) {
                    st.cur = drawable;
                    st.curAlpha = st.alpha;
                    onDrawableChanged(featureId, drawable, st.alpha);
                }
            }
        }
    }

    private void updateInt(int featureId, int value, boolean fromResume) {
        if (this.mContentParent != null) {
            int featureMask = FLAG_RESOURCE_SET_ICON << featureId;
            if ((getFeatures() & featureMask) != 0 || fromResume) {
                if ((getLocalFeatures() & featureMask) != 0) {
                    onIntChanged(featureId, value);
                } else if (getContainer() != null) {
                    getContainer().setChildInt(featureId, value);
                }
            }
        }
    }

    private ImageView getLeftIconView() {
        if (this.mLeftIconView != null) {
            return this.mLeftIconView;
        }
        if (this.mContentParent == null) {
            installDecor();
        }
        ImageView imageView = (ImageView) findViewById(R.id.left_icon);
        this.mLeftIconView = imageView;
        return imageView;
    }

    protected void dispatchWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        super.dispatchWindowAttributesChanged(attrs);
        if (this.mDecor != null) {
            this.mDecor.updateColorViews(null, true);
        }
    }

    private ProgressBar getCircularProgressBar(boolean shouldInstallDecor) {
        if (this.mCircularProgressBar != null) {
            return this.mCircularProgressBar;
        }
        if (this.mContentParent == null && shouldInstallDecor) {
            installDecor();
        }
        this.mCircularProgressBar = (ProgressBar) findViewById(R.id.progress_circular);
        if (this.mCircularProgressBar != null) {
            this.mCircularProgressBar.setVisibility(FLAG_RESOURCE_SET_ICON_FALLBACK);
        }
        return this.mCircularProgressBar;
    }

    private ProgressBar getHorizontalProgressBar(boolean shouldInstallDecor) {
        if (this.mHorizontalProgressBar != null) {
            return this.mHorizontalProgressBar;
        }
        if (this.mContentParent == null && shouldInstallDecor) {
            installDecor();
        }
        this.mHorizontalProgressBar = (ProgressBar) findViewById(R.id.progress_horizontal);
        if (this.mHorizontalProgressBar != null) {
            this.mHorizontalProgressBar.setVisibility(FLAG_RESOURCE_SET_ICON_FALLBACK);
        }
        return this.mHorizontalProgressBar;
    }

    private ImageView getRightIconView() {
        if (this.mRightIconView != null) {
            return this.mRightIconView;
        }
        if (this.mContentParent == null) {
            installDecor();
        }
        ImageView imageView = (ImageView) findViewById(R.id.right_icon);
        this.mRightIconView = imageView;
        return imageView;
    }

    private void registerSwipeCallbacks() {
        SwipeDismissLayout swipeDismiss = (SwipeDismissLayout) findViewById(R.id.content);
        swipeDismiss.setOnDismissedListener(new OnDismissedListener() {
            public void onDismissed(SwipeDismissLayout layout) {
                PhoneWindow.this.dispatchOnWindowDismissed(PhoneWindow.SWEEP_OPEN_MENU);
            }
        });
        swipeDismiss.setOnSwipeProgressChangedListener(new OnSwipeProgressChangedListener() {
            private static final float ALPHA_DECREASE = 0.5f;
            private boolean mIsTranslucent;

            {
                this.mIsTranslucent = PhoneWindow.SWEEP_OPEN_MENU;
            }

            public void onSwipeProgressChanged(SwipeDismissLayout layout, float progress, float translate) {
                int flags;
                WindowManager.LayoutParams newParams = PhoneWindow.this.getAttributes();
                newParams.x = (int) translate;
                newParams.alpha = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL - (ALPHA_DECREASE * progress);
                PhoneWindow.this.setAttributes(newParams);
                if (newParams.x == 0) {
                    flags = GL10.GL_STENCIL_BUFFER_BIT;
                } else {
                    flags = GL10.GL_NEVER;
                }
                PhoneWindow.this.setFlags(flags, View.SYSTEM_UI_LAYOUT_FLAGS);
            }

            public void onSwipeCancelled(SwipeDismissLayout layout) {
                WindowManager.LayoutParams newParams = PhoneWindow.this.getAttributes();
                newParams.x = 0;
                newParams.alpha = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                PhoneWindow.this.setAttributes(newParams);
                PhoneWindow.this.setFlags(GL10.GL_STENCIL_BUFFER_BIT, View.SYSTEM_UI_LAYOUT_FLAGS);
            }
        });
    }

    private void callOnPanelClosed(int featureId, PanelFeatureState panel, Menu menu) {
        Window.Callback cb = getCallback();
        if (cb != null) {
            if (menu == null) {
                if (panel == null && featureId >= 0 && featureId < this.mPanels.length) {
                    panel = this.mPanels[featureId];
                }
                if (panel != null) {
                    menu = panel.menu;
                }
            }
            if ((panel == null || panel.isOpen) && !isDestroyed()) {
                cb.onPanelClosed(featureId, menu);
            }
        }
    }

    private boolean launchDefaultSearch(KeyEvent event) {
        boolean result;
        Window.Callback cb = getCallback();
        if (cb == null || isDestroyed()) {
            result = SWEEP_OPEN_MENU;
        } else {
            sendCloseSystemWindows("search");
            int deviceId = event.getDeviceId();
            SearchEvent searchEvent = null;
            if (deviceId != 0) {
                searchEvent = new SearchEvent(InputDevice.getDevice(deviceId));
            }
            try {
                result = cb.onSearchRequested(searchEvent);
            } catch (AbstractMethodError e) {
                Log.e(TAG, "WindowCallback " + cb.getClass().getName() + " does not implement" + " method onSearchRequested(SearchEvent); fa", e);
                result = cb.onSearchRequested();
            }
        }
        if (result || (getContext().getResources().getConfiguration().uiMode & 15) != FLAG_RESOURCE_SET_ICON_FALLBACK) {
            return result;
        }
        Bundle args = new Bundle();
        args.putInt("android.intent.extra.ASSIST_INPUT_DEVICE_ID", event.getDeviceId());
        return ((SearchManager) getContext().getSystemService("search")).launchLegacyAssist(null, UserHandle.myUserId(), args);
    }

    public void setVolumeControlStream(int streamType) {
        this.mVolumeControlStreamType = streamType;
    }

    public int getVolumeControlStream() {
        return this.mVolumeControlStreamType;
    }

    public void setMediaController(MediaController controller) {
        this.mMediaController = controller;
    }

    public MediaController getMediaController() {
        return this.mMediaController;
    }

    public void setEnterTransition(Transition enterTransition) {
        this.mEnterTransition = enterTransition;
    }

    public void setReturnTransition(Transition transition) {
        this.mReturnTransition = transition;
    }

    public void setExitTransition(Transition exitTransition) {
        this.mExitTransition = exitTransition;
    }

    public void setReenterTransition(Transition transition) {
        this.mReenterTransition = transition;
    }

    public void setSharedElementEnterTransition(Transition sharedElementEnterTransition) {
        this.mSharedElementEnterTransition = sharedElementEnterTransition;
    }

    public void setSharedElementReturnTransition(Transition transition) {
        this.mSharedElementReturnTransition = transition;
    }

    public void setSharedElementExitTransition(Transition sharedElementExitTransition) {
        this.mSharedElementExitTransition = sharedElementExitTransition;
    }

    public void setSharedElementReenterTransition(Transition transition) {
        this.mSharedElementReenterTransition = transition;
    }

    public Transition getEnterTransition() {
        return this.mEnterTransition;
    }

    public Transition getReturnTransition() {
        if (this.mReturnTransition == USE_DEFAULT_TRANSITION) {
            return getEnterTransition();
        }
        return this.mReturnTransition;
    }

    public Transition getExitTransition() {
        return this.mExitTransition;
    }

    public Transition getReenterTransition() {
        if (this.mReenterTransition == USE_DEFAULT_TRANSITION) {
            return getExitTransition();
        }
        return this.mReenterTransition;
    }

    public Transition getSharedElementEnterTransition() {
        return this.mSharedElementEnterTransition;
    }

    public Transition getSharedElementReturnTransition() {
        return this.mSharedElementReturnTransition == USE_DEFAULT_TRANSITION ? getSharedElementEnterTransition() : this.mSharedElementReturnTransition;
    }

    public Transition getSharedElementExitTransition() {
        return this.mSharedElementExitTransition;
    }

    public Transition getSharedElementReenterTransition() {
        return this.mSharedElementReenterTransition == USE_DEFAULT_TRANSITION ? getSharedElementExitTransition() : this.mSharedElementReenterTransition;
    }

    public void setAllowEnterTransitionOverlap(boolean allow) {
        this.mAllowEnterTransitionOverlap = Boolean.valueOf(allow);
    }

    public boolean getAllowEnterTransitionOverlap() {
        return this.mAllowEnterTransitionOverlap == null ? true : this.mAllowEnterTransitionOverlap.booleanValue();
    }

    public void setAllowReturnTransitionOverlap(boolean allowExitTransitionOverlap) {
        this.mAllowReturnTransitionOverlap = Boolean.valueOf(allowExitTransitionOverlap);
    }

    public boolean getAllowReturnTransitionOverlap() {
        return this.mAllowReturnTransitionOverlap == null ? true : this.mAllowReturnTransitionOverlap.booleanValue();
    }

    public long getTransitionBackgroundFadeDuration() {
        if (this.mBackgroundFadeDurationMillis < 0) {
            return 300;
        }
        return this.mBackgroundFadeDurationMillis;
    }

    public void setTransitionBackgroundFadeDuration(long fadeDurationMillis) {
        if (fadeDurationMillis < 0) {
            throw new IllegalArgumentException("negative durations are not allowed");
        }
        this.mBackgroundFadeDurationMillis = fadeDurationMillis;
    }

    public void setSharedElementsUseOverlay(boolean sharedElementsUseOverlay) {
        this.mSharedElementsUseOverlay = Boolean.valueOf(sharedElementsUseOverlay);
    }

    public boolean getSharedElementsUseOverlay() {
        return this.mSharedElementsUseOverlay == null ? true : this.mSharedElementsUseOverlay.booleanValue();
    }

    int getLocalFeaturesPrivate() {
        return super.getLocalFeatures();
    }

    protected void setDefaultWindowFormat(int format) {
        super.setDefaultWindowFormat(format);
    }

    void sendCloseSystemWindows() {
        sendCloseSystemWindows(getContext(), null);
    }

    void sendCloseSystemWindows(String reason) {
        sendCloseSystemWindows(getContext(), reason);
    }

    public static void sendCloseSystemWindows(Context context, String reason) {
        if (ActivityManagerNative.isSystemReady()) {
            try {
                ActivityManagerNative.getDefault().closeSystemDialogs(reason);
            } catch (RemoteException e) {
            }
        }
    }

    public int getStatusBarColor() {
        return this.mStatusBarColor;
    }

    public void setStatusBarColor(int color) {
        this.mStatusBarColor = color;
        this.mForcedStatusBarColor = true;
        updateLayoutParamsColor();
        if (this.mDecor != null) {
            this.mDecor.updateColorViews(null, SWEEP_OPEN_MENU);
        }
    }

    public boolean getIsForcedStatusBarColor() {
        return this.mForcedStatusBarColor;
    }

    public int getForcedStatusBarColor() {
        if (this.mForcedStatusBarColor) {
            return this.mStatusBarColor;
        }
        return 0;
    }

    public int getNavigationBarColor() {
        return this.mNavigationBarColor;
    }

    public void setNavigationBarColor(int color) {
        this.mNavigationBarColor = color;
        updateLayoutParamsColor();
        this.mForcedNavigationBarColor = true;
        if (this.mDecor != null) {
            this.mDecor.updateColorViews(null, SWEEP_OPEN_MENU);
            this.mDecor.updateNavigationGuardColor();
        }
    }

    public void setIsStartingWindow(boolean isStartingWindow) {
        this.mIsStartingWindow = isStartingWindow;
    }

    public void setTheme(int resid) {
        this.mTheme = resid;
        if (this.mDecor != null) {
            Context context = this.mDecor.getContext();
            if (context instanceof DecorContext) {
                context.setTheme(resid);
            }
        }
    }

    public void setResizingCaptionDrawable(Drawable drawable) {
        this.mDecor.setUserCaptionBackgroundDrawable(drawable);
    }

    public void setDecorCaptionShade(int decorCaptionShade) {
        this.mDecorCaptionShade = decorCaptionShade;
        if (this.mDecor != null) {
            this.mDecor.updateDecorCaptionShade();
        }
    }

    int getDecorCaptionShade() {
        return this.mDecorCaptionShade;
    }

    public void setAttributes(WindowManager.LayoutParams params) {
        super.setAttributes(params);
        if (this.mDecor != null) {
            this.mDecor.updateLogTag(params);
        }
    }

    protected int getEmuiActionBarLayout(int layoutResource) {
        return layoutResource;
    }

    protected void setEmuiActionModeBar(ViewStub viewStub) {
    }

    protected int getHeightMeasureSpec(int fixh, int heightSize, int defaultHeightMeasureSpec) {
        return defaultHeightMeasureSpec;
    }

    protected void updateLayoutParamsColor() {
    }

    protected int getScreenWidth() {
        boolean minNavigationBar = SWEEP_OPEN_MENU;
        getFullScreenWidth();
        String navBarOverride = SystemProperties.get("qemu.hw.mainkeys", "0");
        boolean hasNavigationBar = true;
        if ("1".equals(navBarOverride)) {
            hasNavigationBar = SWEEP_OPEN_MENU;
        } else if ("0".equals(navBarOverride)) {
            hasNavigationBar = true;
        }
        if (!hasNavigationBar || this.mMaxSize == 0 || getContext() == null) {
            return this.mMaxSize;
        }
        int w = this.mMaxSize - this.mNaviWidth;
        if (Global.getInt(getContext().getContentResolver(), "navigationbar_is_min", 0) != 0) {
            minNavigationBar = true;
        }
        if (minNavigationBar) {
            return this.mMaxSize;
        }
        return w;
    }

    protected int getFullScreenWidth() {
        if (this.mMaxSize == 0) {
            WindowManager wm = getWindowManager();
            if (wm != null) {
                this.mMaxSize = wm.getDefaultDisplay().getMaximumSizeDimension();
            }
        }
        return this.mMaxSize;
    }

    protected boolean getTryForcedCloseAnimation(IWindowManager wm, boolean animate, Object tag) {
        return SWEEP_OPEN_MENU;
    }

    protected boolean CheckPermanentMenuKey() {
        return ViewConfiguration.get(getContext()).hasPermanentMenuKey() ? SWEEP_OPEN_MENU : true;
    }

    protected FloatingToolbar getFloatingToolbar(Context context, Window window) {
        return new FloatingToolbar(context, window);
    }

    protected boolean windowIsTranslucent() {
        return !this.mIsTransparent ? isSplitMode() : true;
    }

    protected void initTranslucentImmersion() {
    }

    protected boolean isTranslucentImmersion() {
        return SWEEP_OPEN_MENU;
    }

    protected void initHwDrawerFeature() {
    }

    protected void initSplitMode() {
    }

    protected boolean isSplitMode() {
        return SWEEP_OPEN_MENU;
    }
}
