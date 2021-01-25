package huawei.com.android.server.policy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.AnimatorSet;
import android.app.AbsWallpaperManagerInner;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioSystem;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.HwAlarmManager;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HwGlobalActionsView extends RelativeLayout implements AbsWallpaperManagerInner.IBlurWallpaperCallback {
    private static final int ACCESSIBILITY_TIP_DISMISS_TIME = 3;
    private static final String AIRPLANEMODE_TAG = "airplane_mode";
    private static final int BEEP_DURATION = 150;
    public static final boolean BLUR_SCREENSHOT = true;
    private static final int BOTH_TALKBACK_SCREENREADER_SERVICE_OFF = 0;
    private static final int BOTH_TALKBACK_SCREENREADER_SERVICE_ON = 3;
    private static final long COUNT_DOWN_INTERVAL = 1000;
    public static final boolean DEBUG = false;
    private static final int DEFAULT_ENTER_MAX_TIMES = 3;
    private static final String DESKCLOCK_PACKAGENAME_NEW = "com.huawei.deskclock";
    private static final String DESKCLOCK_PACKAGENAME_OLD = "com.android.deskclock";
    private static final int DEVICE_TYPE_TELEVISION = 2;
    private static final int DEVICE_TYPE_WATCH = 8;
    private static final int DISABLE_ACCESSIBILITY_DELAY_MILLIS = 3000;
    private static final int DISMISSS_DELAY_0 = 0;
    private static final int DISMISSS_DELAY_1000 = 1000;
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final int FACTOR_MAP = 2;
    private static final int FIX_BUG_ALPHA = 254;
    private static final int FREE_DELAY = 10000;
    private static final String HWGLOBALACTIONS_ENTRY_COUNT = "hwglobalactions_entry_count";
    private static final int ID_DRAWABLE_BACKGROUND_DEFAULT = 33751228;
    private static final int ID_DRAWABLE_BACKGROUND_FOCUS = 33751978;
    private static final int ID_DRAWABLE_REBOOT_FOCUS = 33751980;
    private static final int ID_DRAWABLE_SCREENOFF_FOCUS = 33751982;
    private static final int ID_DRAWABLE_SHUTDOWN_FOCUS = 33751979;
    private static final int ID_IC_AIRPLANEMODE = 33751059;
    private static final int ID_IC_AIRPLANEMODE_OFF = 33751636;
    private static final int ID_IC_AIRPLANEMODE_ON = 33751060;
    private static final int ID_IC_NEW_LOCKDOWN = 33751785;
    private static final int ID_IC_NEW_LOCKDOWN_CONFIRM = 33751785;
    private static final int ID_IC_NEW_REBOOT = 33751230;
    private static final int ID_IC_NEW_REBOOT_CONFIRM = 33751230;
    private static final int ID_IC_NEW_SHUTDOWN = 33751229;
    private static final int ID_IC_NEW_SHUTDOWN_CONFIRM = 33751229;
    private static final int ID_IC_REBOOT = 33751064;
    private static final int ID_IC_REBOOT_CONFIRM = 33751065;
    private static final int ID_IC_SHUTDOWN = 33751072;
    private static final int ID_IC_SHUTDOWN_CONFIRM = 33751073;
    private static final int ID_IC_SILENTMODE_NORMAL = 33751063;
    private static final int ID_IC_SILENTMODE_SILENT = 33751061;
    private static final int ID_IC_SILENTMODE_VIBRATE = 33751062;
    private static final int ID_IC_TV_SCREENOFF = 33751981;
    private static final int ID_STR_AIRPLANEMODE = 33685734;
    private static final int ID_STR_AIRPLANEMODE_OFF = 33685744;
    private static final int ID_STR_AIRPLANEMODE_ON = 33685743;
    private static final int ID_STR_LOCKDOWN = 33686054;
    private static final int ID_STR_LOCKDOWN_CONFIRM = 33686055;
    private static final int ID_STR_REBOOT = 33685740;
    private static final int ID_STR_REBOOTING = 33685784;
    private static final int ID_STR_REBOOT_CONFIRM = 33685741;
    private static final int ID_STR_SCREENOFF = 33686202;
    private static final int ID_STR_SCREENOFF_COUNT = 33686200;
    private static final int ID_STR_SHUTDOWN = 33685738;
    private static final int ID_STR_SHUTDOWNING = 33685783;
    private static final int ID_STR_SHUTDOWN_CONFIRM = 33685739;
    private static final int ID_STR_SILENTMODE = 33685735;
    private static final int ID_STR_SOUNDMODE = 33685736;
    private static final int ID_STR_TOUCH_TO_GO_BACK = 33685742;
    private static final int ID_STR_VIBRATIONMODE = 33685737;
    private static final int INDEX_REBOOT_FOCUS = 1;
    private static final int INDEX_REBOOT_UNFOCUS = 0;
    private static final int INDEX_SCREENOFF_FOCUS = 5;
    private static final int INDEX_SCREENOFF_UNFOCUS = 4;
    private static final int INDEX_SHUTDOWN_FOCUS = 3;
    private static final int INDEX_SHUTDOWN_UNFOCUS = 2;
    private static final boolean IS_POWEROFF_ALARM_ENABLED = AppActConstant.VALUE_TRUE.equals(SystemProperties.get("ro.poweroff_alarm", AppActConstant.VALUE_TRUE));
    private static final String KIDS_MODE_IS_OPEN = "kids_mode_is_open";
    static final String LOCKDOWN_TAG = "lockdown";
    private static final int MAX_VOLUME = 100;
    private static final int MESSAGE_DISABLE_ACCESSIBILITY = 7;
    private static final long MILLIS_IN_FUTURE = 15300;
    private static final int MIN_INTERVAL_BETWEEN_CLICKS = 200;
    private static final int MSG_DISMISS_DELAY = 6;
    private static final int MSG_FREE_RESOURCES = 5;
    private static final int MSG_PLAY_SOUND = 2;
    private static final int MSG_RESET_BLURWALLPAPER = 1;
    private static final int MSG_STOP_SOUNDS = 3;
    private static final int MSG_VIBRATE = 4;
    private static final int NONE_LISTVIEW = 1;
    private static final int ONE_BOOT_LISTVIEW = 5;
    private static final int ONE_DESKCLOCK_LISTVIEW = 4;
    private static final int ONE_TWO_BOOT_LISTVIEW = 7;
    private static final int ONE_TWO_DESKCLOCK_LISTVIEW = 6;
    private static final int ONLY_SCREENREADER_SERVICE_ON = 2;
    private static final int ONLY_TALKBACK_SERVICE_ON = 1;
    private static final String OTHERAREA_TAG = "other_area";
    private static final int PLAY_SOUND_DELAY = 300;
    protected static final int POWERKEY_RESTART_ONLY_POWER = 0;
    protected static final int POWERKEY_RESTART_POWER_AND_VOLUME_DOWN = 1;
    static final String REBOOT_TAG = "reboot";
    private static final int REBOOT_TAG_MAP_INDEX = 0;
    private static final String RO_BOOT_POWERKEY_RESTART_TYPE = "ro.boot.powerkey_restart_type";
    private static final String SCREENOFF_TAG = "screenoff";
    private static final int SCREENOFF_TAG_MAP_INDEX = 2;
    private static final String SCREENREADER_SERVICE_CLASS_NAME = "com.bjbyhd.screenreader_huawei.ScreenReaderService";
    private static final String SCREENREADER_SERVICE_PACKAGE_NAME = "com.bjbyhd.screenreader_huawei";
    private static final String SETTINGS_PACKAGENAME = "com.android.providers.settings";
    static final String SHUTDOWN_TAG = "shutdown";
    private static final int SHUTDOWN_TAG_MAP_INDEX = 1;
    private static final String SILENTMODE_TAG = "silent_mode";
    private static final String TAG = "HwGlobalActions";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final String TALKBACK_SERVICE_CLASS_NAME = "com.google.android.marvin.talkback.TalkBackService";
    private static final String TALKBACK_SERVICE_PACKAGE_NAME = "com.google.android.marvin.talkback";
    private static final String TELEVISION_TAG = "television";
    private static final int TWO_BOOT_DLISTVIEW = 3;
    private static final int TWO_DESKCLOCK_DLISTVIEW = 2;
    private static int TYPEDESKCLOCK = 1;
    private static int TYPESETTINGS = 2;
    public static final int VIBRATE_DELAY = 300;
    private static final int VIBRATE_DURATION = 300;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private static long mShowBootOnTime = -1;
    private static long mShowDeskClockTime = -1;
    private static Boolean[] mState = {true, true};
    private static final Set<ComponentName> sInstalledServices = new HashSet();
    private static final TextUtils.SimpleStringSplitter sStringColonSplitter = new TextUtils.SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
    private boolean isFirstShow;
    private String mAccessibilityDescription;
    private TextView mAccessibilityTip;
    private View.OnClickListener mActionClickListener;
    private View mAirplanemodeView;
    private ActionPressedCallback mCallback;
    private boolean mCanceled;
    private String mDeskClockName;
    private boolean mDisableIntercepted;
    private Drawable mDrawable;
    private AnimatorSet mEnterSet;
    private boolean mEventAfterActionDown;
    private float mFirstPointerDownX;
    private float mFirstPointerDownY;
    private View.OnFocusChangeListener mFocusListener;
    private Handler mHandler;
    private boolean mInitedConfirm;
    private boolean mIsBootOnTimeClose;
    private boolean mIsCountDownTick;
    private boolean mIsDeskClockClose;
    private boolean mIsKidsMode;
    private boolean mIsTelevisionMode;
    private boolean mIsWatchMode;
    private View mKeyCombinationHintView;
    private View.OnKeyListener mKeyListener;
    private long mLastClickTime;
    private int mListviewState;
    private View mLockdownView;
    private MyActionStateObserver mObserver;
    private View mRebootView;
    private Map<String, Integer> mResIdMap;
    private Map<Integer, InnerResCollection> mResMap;
    private View mScreenoffView;
    private ListView mShutdownListView;
    private ArrayList<String> mShutdownListViewText;
    private MyAdapter mShutdownListviewAdapter;
    private View mShutdownView;
    private boolean mSilentModeButtonTouched;
    private View mSilentmodeView;
    private int mTalkBackScreenReaderStatus;
    private HwGlobalActionsCountDownTimer mTimeCount;
    private Ringtone mTone;
    private ToneGenerator[] mToneGenerators;
    private float mTouchSlop;
    private Vibrator mVibrator;
    ArrayList<View> mViewList;
    private WallpaperManager mWallpaperManager;
    private int mWindowTouchSlop;
    private View view_confirm_action;
    private View view_four_action;
    private View view_two_action;

    public interface ActionPressedCallback {
        void dismissShutdownMenu(int i);

        void onAirplaneModeActionPressed();

        void onKeyCombinationActionStateChanged();

        void onLockdownActionPressed();

        void onOtherAreaPressed();

        void onRebootActionPressed();

        void onScreenoffActionPressed(boolean z);

        void onShutdownActionPressed(boolean z, boolean z2, int i);

        void onSilentModeActionPressed();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setTipMessageVisibility(int calcIndex) {
        if (calcIndex == 4 || calcIndex == 5) {
            View screenoffView = this.mScreenoffView;
            if (screenoffView == null && (screenoffView = this.view_two_action.findViewById(34603468)) == null) {
                Log.e(TAG, "setTipMessageVisibility, screenoffView is null!");
                return;
            }
            View tipMessageView = screenoffView.findViewById(34603469);
            Log.w(TAG, "setTipMessageVisibility scccalcIndex = " + calcIndex);
            if (tipMessageView == null) {
                Log.w(TAG, "setTipMessageVisibility tipMessageView is null!");
            } else if (calcIndex == 4) {
                tipMessageView.setVisibility(4);
                cancelCountDownTimer();
            } else {
                createCountDownTick(screenoffView);
                Log.w(TAG, "setTipMessageVisibility mIsKidsMode = " + this.mIsKidsMode);
                if (!this.mIsKidsMode) {
                    tipMessageView.setVisibility(0);
                } else {
                    tipMessageView.setVisibility(4);
                }
            }
        } else {
            Log.w(TAG, "setTipMessageVisibility calcIndex = " + calcIndex);
        }
    }

    private void createCountDownTick(View screenoffView) {
        if (this.mIsCountDownTick && screenoffView != null) {
            this.mIsCountDownTick = false;
            TextView messageView = (TextView) screenoffView.findViewById(34603129);
            if (messageView == null || this.mContext == null) {
                Log.e(TAG, "setTipMessageVisibility mContext or messageView is null!");
            } else {
                this.mTimeCount = new HwGlobalActionsCountDownTimer(MILLIS_IN_FUTURE, COUNT_DOWN_INTERVAL, messageView, this.mContext.getString(ID_STR_SCREENOFF_COUNT), this.mContext.getString(ID_STR_SCREENOFF));
            }
        }
    }

    public HwGlobalActionsView(Context context) {
        this(context, null);
    }

    public HwGlobalActionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mObserver = new MyActionStateObserver();
        this.view_four_action = null;
        this.view_two_action = null;
        this.view_confirm_action = null;
        this.mCallback = null;
        this.mViewList = new ArrayList<>();
        boolean z = true;
        this.isFirstShow = true;
        this.mIsDeskClockClose = true;
        this.mIsBootOnTimeClose = true;
        this.mListviewState = 0;
        this.mShutdownListViewText = new ArrayList<>();
        this.mLastClickTime = 0;
        this.mDisableIntercepted = false;
        this.mCanceled = false;
        this.mTouchSlop = 0.0f;
        this.mFirstPointerDownX = 0.0f;
        this.mFirstPointerDownY = 0.0f;
        this.mTalkBackScreenReaderStatus = 0;
        this.mAccessibilityTip = null;
        this.mTone = null;
        this.mEventAfterActionDown = false;
        this.mInitedConfirm = false;
        this.mKeyCombinationHintView = null;
        this.mDeskClockName = "";
        this.mFocusListener = new View.OnFocusChangeListener() {
            /* class huawei.com.android.server.policy.HwGlobalActionsView.AnonymousClass1 */

            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean hasFocus) {
                Object obj = view.getTag();
                String tag = "";
                if (obj instanceof String) {
                    tag = (String) obj;
                }
                View shutdownMenuLayout = HwGlobalActionsView.this.view_two_action.findViewById(34603206);
                if (shutdownMenuLayout == null) {
                    Log.e(HwGlobalActionsView.TAG, "shutdownMenuLayout or screenoffView is null!");
                } else if (HwGlobalActionsView.this.mResIdMap == null || HwGlobalActionsView.this.mResMap == null) {
                    Log.e(HwGlobalActionsView.TAG, "mResIdMap or mResMap  is null!");
                } else if (HwGlobalActionsView.this.mResIdMap.containsKey(tag)) {
                    int calcIndex = (((Integer) HwGlobalActionsView.this.mResIdMap.get(tag)).intValue() * 2) + (hasFocus ? 1 : 0);
                    if (calcIndex < 0 || calcIndex >= HwGlobalActionsView.this.mResMap.size()) {
                        Log.e(HwGlobalActionsView.TAG, "onFocusChange calcIndex is error and the value of calcIndex is " + calcIndex);
                        return;
                    }
                    HwGlobalActionsView.this.setTipMessageVisibility(calcIndex);
                    ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.getContext()).setFocusChanageAnim(HwGlobalActionsView.this.mContext, shutdownMenuLayout, (InnerResCollection) HwGlobalActionsView.this.mResMap.get(Integer.valueOf(calcIndex)), hasFocus);
                } else {
                    Log.e(HwGlobalActionsView.TAG, "onFocusChange tag is illegal and the tag = " + tag);
                }
            }
        };
        this.mKeyListener = new View.OnKeyListener() {
            /* class huawei.com.android.server.policy.HwGlobalActionsView.AnonymousClass2 */

            @Override // android.view.View.OnKeyListener
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                boolean isAnimRunning = ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).getIsAnimRunning();
                if (HwGlobalActionsView.this.mCallback == null || isAnimRunning) {
                    Log.w(HwGlobalActionsView.TAG, "onKey, The operation is too frequent causing mCallback to be null and isAnimRunning = " + isAnimRunning);
                    return false;
                }
                long timeInterval = SystemClock.elapsedRealtime() - HwGlobalActionsView.this.mLastClickTime;
                if (timeInterval < 200) {
                    Log.w(HwGlobalActionsView.TAG, "onKey, The operation is too frequent and time interval is " + timeInterval);
                    return false;
                }
                HwGlobalActionsView.this.mLastClickTime = SystemClock.elapsedRealtime();
                Object obj = view.getTag();
                String tag = "";
                if (obj instanceof String) {
                    tag = (String) obj;
                }
                if (keyCode == 4 || keyCode == 3) {
                    HwGlobalActionsView.this.mCallback.onOtherAreaPressed();
                    return true;
                }
                if (keyCode == 23 || keyCode == 66) {
                    if (ActivityManager.isUserAMonkey()) {
                        Log.w(HwGlobalActionsView.TAG, "ignoring monkey's attempt to reboot");
                        return true;
                    } else if (HwGlobalActionsView.REBOOT_TAG.equals(tag)) {
                        HwGlobalActionsView.this.mCallback.onRebootActionPressed();
                        return true;
                    } else if (HwGlobalActionsView.SHUTDOWN_TAG.equals(tag)) {
                        HwGlobalActionsView.this.mCallback.onShutdownActionPressed(true, true, 1);
                        return true;
                    } else if (HwGlobalActionsView.SCREENOFF_TAG.equals(tag)) {
                        HwGlobalActionsView.this.mCallback.onScreenoffActionPressed(false);
                        return true;
                    } else {
                        Log.w(HwGlobalActionsView.TAG, "onKey other tags = " + tag);
                    }
                } else if (keyCode == 26 && event.getAction() == 0) {
                    Log.w(HwGlobalActionsView.TAG, "onKey power double click");
                    HwGlobalActionsView.this.mCallback.onScreenoffActionPressed(true);
                    return true;
                } else {
                    Log.w(HwGlobalActionsView.TAG, "onKey key is " + keyCode);
                }
                return false;
            }
        };
        this.mActionClickListener = new View.OnClickListener() {
            /* class huawei.com.android.server.policy.HwGlobalActionsView.AnonymousClass5 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (HwGlobalActionsView.this.mHandler != null) {
                    HwGlobalActionsView.this.mHandler.removeMessages(6);
                }
                if (HwGlobalActionsView.this.mCallback != null && !ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).getIsAnimRunning() && SystemClock.elapsedRealtime() - HwGlobalActionsView.this.mLastClickTime >= 200) {
                    HwGlobalActionsView.this.mLastClickTime = SystemClock.elapsedRealtime();
                    Object obj = v.getTag();
                    String tag = "";
                    if (obj instanceof String) {
                        tag = (String) obj;
                    }
                    if (tag.equals(HwGlobalActionsView.OTHERAREA_TAG)) {
                        HwGlobalActionsView.this.mCallback.onOtherAreaPressed();
                    } else if (tag.equals(HwGlobalActionsView.AIRPLANEMODE_TAG)) {
                        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                            Log.i(HwGlobalActionsView.TAG, " factory mode  ");
                        } else if ("normal".equals(SystemProperties.get("ro.runmode", "normal"))) {
                            Log.i(HwGlobalActionsView.TAG, " normal mode  ");
                            HwGlobalActionsView.this.mCallback.onAirplaneModeActionPressed();
                        }
                    } else if (tag.equals(HwGlobalActionsView.SILENTMODE_TAG)) {
                        HwGlobalActionsView.this.mSilentModeButtonTouched = true;
                        HwGlobalActionsView.this.mCallback.onSilentModeActionPressed();
                    } else if (tag.equals(HwGlobalActionsView.REBOOT_TAG)) {
                        if (ActivityManager.isUserAMonkey()) {
                            Log.w(HwGlobalActionsView.TAG, "ignoring monkey's attempt to reboot");
                        } else {
                            HwGlobalActionsView.this.mCallback.onRebootActionPressed();
                        }
                    } else if (tag.equals(HwGlobalActionsView.SHUTDOWN_TAG)) {
                        if (ActivityManager.isUserAMonkey()) {
                            Log.w(HwGlobalActionsView.TAG, "ignoring monkey's attempt to shutdown");
                        } else {
                            HwGlobalActionsView.this.mCallback.onShutdownActionPressed(HwGlobalActionsView.this.mIsDeskClockClose, HwGlobalActionsView.this.mIsBootOnTimeClose, HwGlobalActionsView.this.mListviewState);
                        }
                    } else if (tag.equals(HwGlobalActionsView.LOCKDOWN_TAG)) {
                        HwGlobalActionsView.this.mCallback.onLockdownActionPressed();
                    }
                }
            }
        };
        if (this.mContext != null) {
            this.mTouchSlop = (float) this.mContext.getResources().getDimensionPixelSize(17104905);
            this.mIsTelevisionMode = this.mContext.getResources().getInteger(34275378) == 2;
            this.mIsWatchMode = this.mContext.getResources().getInteger(34275378) != 8 ? false : z;
            if (!this.mIsTelevisionMode) {
                this.mTone = RingtoneManager.getRingtone(this.mContext, Settings.System.DEFAULT_NOTIFICATION_URI);
                Ringtone ringtone = this.mTone;
                if (ringtone != null) {
                    ringtone.setStreamType(3);
                }
                this.mIsKidsMode = false;
            } else {
                Log.w(TAG, "HwGlobalActionsVie, mTone is null in televisionMode");
                this.mIsKidsMode = AppActConstant.VALUE_TRUE.equalsIgnoreCase(Settings.System.getStringForUser(this.mContext.getContentResolver(), KIDS_MODE_IS_OPEN, ActivityManager.getCurrentUser()));
                this.mTone = null;
            }
            this.mAccessibilityDescription = getContext().getResources().getString(33686187);
            return;
        }
        Log.e(TAG, "HwGlobalActionsView, mContext is null");
        this.mIsTelevisionMode = false;
        this.mIsKidsMode = false;
        this.mIsWatchMode = false;
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            /* class huawei.com.android.server.policy.HwGlobalActionsView.AnonymousClass3 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                    default:
                        return;
                    case 2:
                        HwGlobalActionsView.this.onPlaySound(msg.arg1, msg.arg2);
                        return;
                    case 3:
                        HwGlobalActionsView.this.onStopSounds();
                        return;
                    case 4:
                        HwGlobalActionsView.this.onVibrate();
                        return;
                    case 5:
                        HwGlobalActionsView.this.onFreeResources();
                        return;
                    case 6:
                        if (HwGlobalActionsView.this.mCallback != null && HwGlobalActionsView.this.mSilentModeButtonTouched) {
                            HwGlobalActionsView.this.mCallback.dismissShutdownMenu(0);
                            return;
                        }
                        return;
                    case 7:
                        if (HwGlobalActionsView.this.mTone != null) {
                            HwGlobalActionsView.this.mTone.play();
                        }
                        HwGlobalActionsView.this.disableTalkBackService();
                        return;
                }
            }
        };
    }

    public void onBlurWallpaperChanged() {
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.sendEmptyMessage(1);
        }
    }

    private void setBlurWallpaperBackground() {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        loc[0] = loc[0] % getContext().getResources().getDisplayMetrics().widthPixels;
        Rect rect = new Rect(loc[0], loc[1], loc[0] + getWidth(), loc[1] + getHeight());
        if (rect.width() > 0 && rect.height() > 0) {
            this.mDrawable = new BitmapDrawable(this.mWallpaperManager.getBlurBitmap(rect));
            Drawable drawable = this.mDrawable;
            if (drawable != null) {
                setBackgroundDrawable(drawable);
            }
        }
    }

    @Override // android.widget.RelativeLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        Handler handler;
        super.onLayout(changed, l, t, r, b);
        if (changed && (handler = this.mHandler) != null) {
            handler.sendEmptyMessage(1);
        }
    }

    public void registerActionPressedCallback(ActionPressedCallback callback) {
        HwGlobalActionsCountDownTimer hwGlobalActionsCountDownTimer;
        this.mCallback = callback;
        if (!this.mIsTelevisionMode || (hwGlobalActionsCountDownTimer = this.mTimeCount) == null) {
            Log.w(TAG, "registerActionPressedCallback  mIsTelevisionMode = " + this.mIsTelevisionMode);
            return;
        }
        hwGlobalActionsCountDownTimer.registerActionPressedCallback(this.mCallback);
        this.mTimeCount.start();
    }

    public void unregisterActionPressedCallback() {
        this.mCallback = null;
        if (this.mIsTelevisionMode) {
            cancelCountDownTimer();
        }
    }

    public void cancelCountDownTimer() {
        HwGlobalActionsCountDownTimer hwGlobalActionsCountDownTimer = this.mTimeCount;
        if (hwGlobalActionsCountDownTimer != null) {
            hwGlobalActionsCountDownTimer.cancelCount();
            this.mTimeCount.cancel();
            this.mTimeCount = null;
        }
    }

    /* access modifiers changed from: private */
    public final class MyActionStateObserver implements HwGlobalActionsData.ActionStateObserver {
        public MyActionStateObserver() {
        }

        @Override // huawei.com.android.server.policy.HwGlobalActionsData.ActionStateObserver
        public void onAirplaneModeActionStateChanged() {
            if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView hwGlobalActionsView = HwGlobalActionsView.this;
                hwGlobalActionsView.updateAirplaneModeUI(hwGlobalActionsView.view_four_action);
            }
        }

        @Override // huawei.com.android.server.policy.HwGlobalActionsData.ActionStateObserver
        public void onSilentModeActionStateChanged() {
            if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView hwGlobalActionsView = HwGlobalActionsView.this;
                hwGlobalActionsView.updateSilentModeUI(hwGlobalActionsView.view_four_action);
            }
        }

        @Override // huawei.com.android.server.policy.HwGlobalActionsData.ActionStateObserver
        public void onRebootActionStateChanged() {
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & 512) != 0) {
                HwGlobalActionsView.this.initConfirmAction(512);
            } else if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.showNormalUI();
            } else {
                HwGlobalActionsView.this.showBeforeProvisioningUI();
            }
        }

        @Override // huawei.com.android.server.policy.HwGlobalActionsData.ActionStateObserver
        public void onShutdownActionStateChanged() {
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & 8192) != 0) {
                HwGlobalActionsView.this.initConfirmAction(8192);
            } else if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.showNormalUI();
            } else {
                HwGlobalActionsView.this.showBeforeProvisioningUI();
            }
        }

        @Override // huawei.com.android.server.policy.HwGlobalActionsData.ActionStateObserver
        public void onLockdownActionStateChanged() {
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & 131072) != 0) {
                HwGlobalActionsView.this.initConfirmAction(131072);
            } else if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.showNormalUI();
            } else {
                HwGlobalActionsView.this.showBeforeProvisioningUI();
            }
        }

        @Override // huawei.com.android.server.policy.HwGlobalActionsData.ActionStateObserver
        public void onKeyCombinationActionStateChanged() {
            if (!HwGlobalActionsData.getSingletoneInstance().isKeyCombinationEnterAnimationNeeds()) {
                return;
            }
            if (ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).getIsAnimRunning()) {
                Log.w(HwGlobalActionsView.TAG, "onKeyCombinationActionStateChanged: animation is running and conflict");
                return;
            }
            ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.getContext()).setKeyCombinationHintViewAnimation(true, HwGlobalActionsData.getSingletoneInstance().isNowConfirmView()).start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initConfirmAction(int flag) {
        boolean hwNewGlobalAction = isHwGlobalTwoActionsSupport();
        if (hwNewGlobalAction) {
            this.mInitedConfirm = true;
        }
        TextView textView = (TextView) this.view_confirm_action.findViewById(34603129);
        ImageView imageView = (ImageView) this.view_confirm_action.findViewById(34603172);
        this.mShutdownListView = null;
        Object objectView = findViewById(34603154);
        if (objectView instanceof ListView) {
            this.mShutdownListView = (ListView) objectView;
        } else {
            Log.e(TAG, "initConfirmAction(), objectView not instanceof ListView!");
        }
        if (hwNewGlobalAction) {
            setTag(OTHERAREA_TAG);
        }
        this.mListviewState = 0;
        this.mIsDeskClockClose = true;
        this.mIsBootOnTimeClose = true;
        this.mShutdownListViewText.clear();
        mState[0] = true;
        mState[1] = true;
        int ret = initAdapterDate();
        this.mShutdownListviewAdapter = new MyAdapter(this.mShutdownListViewText, getContext());
        ListView listView = this.mShutdownListView;
        if (listView != null) {
            listView.setAdapter((ListAdapter) this.mShutdownListviewAdapter);
        } else {
            Log.e(TAG, "initConfirmAction(), mShutdownListView is null!");
        }
        View iconFrame = this.view_confirm_action.findViewById(34603108);
        if (flag == 512) {
            initConfirmRebootState(textView, imageView, iconFrame, hwNewGlobalAction);
        } else if (flag == 8192) {
            initConfirmShutdownState(textView, imageView, iconFrame, hwNewGlobalAction, ret);
        } else if (flag == 131072) {
            initConfirmLockdownState(textView, imageView, iconFrame, hwNewGlobalAction);
        }
        iconFrame.setOnClickListener(this.mActionClickListener);
    }

    private void initConfirmRebootState(TextView textView, ImageView imageView, View iconFrame, boolean isHwNewGlobalAction) {
        textView.setText(ID_STR_REBOOT_CONFIRM);
        imageView.setImageResource(isHwNewGlobalAction ? 33751230 : ID_IC_REBOOT_CONFIRM);
        if (isHwNewGlobalAction) {
            imageView.setImageAlpha(FIX_BUG_ALPHA);
        }
        imageView.setContentDescription(getContext().getResources().getString(ID_STR_REBOOT_CONFIRM));
        iconFrame.setTag(REBOOT_TAG);
        ListView listView = this.mShutdownListView;
        if (listView != null) {
            listView.setVisibility(8);
        }
        if (isHwNewGlobalAction) {
            ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.mRebootView);
        } else {
            ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.mRebootView, true);
        }
    }

    private void initConfirmShutdownState(TextView textView, ImageView imageView, View iconFrame, boolean isHwNewGlobalAction, int ret) {
        textView.setText(ID_STR_SHUTDOWN_CONFIRM);
        imageView.setImageResource(isHwNewGlobalAction ? 33751229 : ID_IC_SHUTDOWN_CONFIRM);
        if (isHwNewGlobalAction) {
            imageView.setImageAlpha(FIX_BUG_ALPHA);
        }
        imageView.setContentDescription(getContext().getResources().getString(ID_STR_SHUTDOWN_CONFIRM));
        iconFrame.setTag(SHUTDOWN_TAG);
        ListView listView = this.mShutdownListView;
        if (listView != null) {
            listView.setVisibility(0);
            this.mShutdownListView.setTag("disable-multi-select-move");
            this.mShutdownListView.setOverScrollMode(2);
            if (ret == 0 || ret == 1 || ActivityManager.getCurrentUser() != 0) {
                this.mShutdownListView.setVisibility(8);
            }
        }
        if (isHwNewGlobalAction) {
            ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.mShutdownView);
        } else {
            ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.mShutdownView, false);
        }
    }

    private void initConfirmLockdownState(TextView textView, ImageView imageView, View iconFrame, boolean isHwNewGlobalAction) {
        textView.setText(ID_STR_LOCKDOWN_CONFIRM);
        imageView.setImageResource(33751785);
        imageView.setContentDescription(getContext().getResources().getString(ID_STR_LOCKDOWN_CONFIRM));
        iconFrame.setTag(LOCKDOWN_TAG);
        ListView listView = this.mShutdownListView;
        if (listView != null) {
            listView.setVisibility(8);
        }
        if (isHwNewGlobalAction) {
            ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.mLockdownView);
        } else {
            ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.mLockdownView, false);
        }
    }

    public class SearchItemOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        private int id;
        private Boolean[] state;

        public SearchItemOnCheckedChangeListener(int id2, Boolean[] state2) {
            this.id = id2;
            this.state = state2;
        }

        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(HwGlobalActionsView.TAG, "-----SearchItemOnCheckedChangeListener---" + isChecked + ", id = " + this.id);
            switch (HwGlobalActionsView.this.mListviewState) {
                case 1:
                    HwGlobalActionsView.this.mIsDeskClockClose = false;
                    HwGlobalActionsView.this.mIsBootOnTimeClose = false;
                    break;
                case 2:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsDeskClockClose = this.state[0].booleanValue();
                    if (!isChecked && this.id == 0) {
                        HwGlobalActionsView.this.mListviewState = 6;
                        HwGlobalActionsView.this.mIsBootOnTimeClose = true;
                        HwGlobalActionsView.this.mShutdownListViewText.clear();
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowDeskClockTime, HwGlobalActionsView.TYPEDESKCLOCK));
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowBootOnTime, HwGlobalActionsView.TYPESETTINGS));
                        break;
                    }
                case 3:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[0].booleanValue();
                    if (!isChecked && this.id == 0) {
                        HwGlobalActionsView.this.mListviewState = 7;
                        HwGlobalActionsView.this.mIsDeskClockClose = true;
                        HwGlobalActionsView.this.mShutdownListViewText.clear();
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowBootOnTime, HwGlobalActionsView.TYPESETTINGS));
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowDeskClockTime, HwGlobalActionsView.TYPEDESKCLOCK));
                        break;
                    }
                case 4:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsDeskClockClose = this.state[0].booleanValue();
                    HwGlobalActionsView.this.mIsBootOnTimeClose = false;
                    break;
                case 5:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[0].booleanValue();
                    HwGlobalActionsView.this.mIsDeskClockClose = false;
                    break;
                case 6:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsDeskClockClose = this.state[0].booleanValue();
                    HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[1].booleanValue();
                    break;
                case 7:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[0].booleanValue();
                    HwGlobalActionsView.this.mIsDeskClockClose = this.state[1].booleanValue();
                    break;
            }
            Boolean[] unused = HwGlobalActionsView.mState = this.state;
            Log.d(HwGlobalActionsView.TAG, "SearchItemOnCheckedChangeListener,,mListviewState = " + HwGlobalActionsView.this.mListviewState + ",mIsBootOnTimeClose =" + HwGlobalActionsView.this.mIsBootOnTimeClose + ",mIsDeskClockClose = " + HwGlobalActionsView.this.mIsDeskClockClose);
            HwGlobalActionsView.this.mShutdownListviewAdapter.notifyDataSetChanged();
            StatisticalUtils.reporte(HwGlobalActionsView.this.mContext, 23, String.format("{DeskClock:%s, BootOnTime:%s}", Boolean.valueOf(HwGlobalActionsView.this.mIsDeskClockClose), Boolean.valueOf(HwGlobalActionsView.this.mIsBootOnTimeClose)));
        }
    }

    private int initAdapterDate() {
        mShowDeskClockTime = HwAlarmManager.checkHasHwRTCAlarm(getDeskClockName());
        mShowBootOnTime = HwAlarmManager.checkHasHwRTCAlarm(SETTINGS_PACKAGENAME);
        if (mShowDeskClockTime == -1 && mShowBootOnTime == -1) {
            this.mIsBootOnTimeClose = false;
            this.mIsDeskClockClose = false;
            this.mListviewState = 1;
        } else {
            long j = mShowDeskClockTime;
            if (j > 0) {
                long j2 = mShowBootOnTime;
                if (j2 > 0) {
                    this.mIsBootOnTimeClose = true;
                    this.mIsDeskClockClose = true;
                    if (j <= j2) {
                        this.mListviewState = 2;
                        this.mShutdownListViewText.add(formateTime(j, TYPEDESKCLOCK));
                    } else {
                        this.mListviewState = 3;
                        this.mShutdownListViewText.add(formateTime(j2, TYPESETTINGS));
                    }
                }
            }
            long j3 = mShowDeskClockTime;
            if (j3 <= 0 || mShowBootOnTime != -1) {
                long j4 = mShowBootOnTime;
                if (j4 > 0 && mShowDeskClockTime == -1) {
                    this.mListviewState = 5;
                    this.mShutdownListViewText.add(formateTime(j4, TYPESETTINGS));
                    this.mIsBootOnTimeClose = true;
                    this.mIsDeskClockClose = false;
                }
            } else {
                this.mListviewState = 4;
                this.mShutdownListViewText.add(formateTime(j3, TYPEDESKCLOCK));
                this.mIsDeskClockClose = true;
                this.mIsBootOnTimeClose = false;
            }
        }
        Log.d(TAG, "initAdapterDate---,mListviewState = " + this.mListviewState + ",mIsBootOnTimeClose =" + this.mIsBootOnTimeClose + ",mIsDeskClockClose = " + this.mIsDeskClockClose);
        return this.mListviewState;
    }

    private static class ViewHolder {
        public CheckBox checkBox;

        private ViewHolder() {
        }
    }

    /* access modifiers changed from: private */
    public class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater = null;
        private ArrayList<String> list;

        public MyAdapter(ArrayList<String> list2, Context context) {
            this.list = list2;
            this.inflater = LayoutInflater.from(context);
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return this.list.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int position) {
            return this.list.get(position);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            return (long) position;
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                if (HwGlobalActionsView.this.isHwGlobalTwoActionsSupport()) {
                    convertView = this.inflater.inflate(34013227, (ViewGroup) null);
                } else {
                    convertView = this.inflater.inflate(34013223, (ViewGroup) null);
                }
                holder.checkBox = (CheckBox) convertView.findViewById(34603173);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkBox.setText(this.list.get(position));
            holder.checkBox.setOnCheckedChangeListener(new SearchItemOnCheckedChangeListener(position, HwGlobalActionsView.mState));
            return convertView;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String formateTime(long alarmTime, int type) {
        long delta = alarmTime - System.currentTimeMillis();
        int hours = (int) (delta / WifiProCommonUtils.RECHECK_DELAYED_MS);
        int minutes = (int) ((delta / 60000) % 60);
        int days = hours / 24;
        int hours2 = hours % 24;
        String daySeq = getContext().getResources().getQuantityString(34406402, days, Integer.valueOf(days));
        String hourSeq = getContext().getResources().getQuantityString(34406403, hours2, Integer.valueOf(hours2));
        String minutesSeq = getContext().getResources().getQuantityString(34406404, minutes, Integer.valueOf(minutes));
        int index = (days > 0 ? (char) 4 : 0) | (hours2 > 0 ? (char) 2 : 0) | (minutes > 0 ? 1 : 0);
        if (type == TYPEDESKCLOCK) {
            if (index == 0) {
                return getContext().getResources().getString(33685545);
            }
            if (index == 7) {
                return getContext().getResources().getString(33685769, daySeq, hourSeq, minutesSeq);
            }
            if (index == 6) {
                return getContext().getResources().getString(33685770, daySeq, hourSeq);
            }
            if (index == 5) {
                return getContext().getResources().getString(33685771, daySeq, minutesSeq);
            }
            if (index == 4) {
                return getContext().getResources().getString(33685772, daySeq);
            }
            if (index == 3) {
                return getContext().getResources().getString(33685773, hourSeq, minutesSeq);
            }
            if (index == 2) {
                return getContext().getResources().getString(33685774, hourSeq);
            }
            return getContext().getResources().getString(33685775, minutesSeq);
        } else if (index == 0) {
            return getContext().getResources().getString(33685794);
        } else {
            if (index == 7) {
                return getContext().getResources().getString(33685787, daySeq, hourSeq, minutesSeq);
            }
            if (index == 6) {
                return getContext().getResources().getString(33685788, daySeq, hourSeq);
            }
            if (index == 5) {
                return getContext().getResources().getString(33685789, daySeq, minutesSeq);
            }
            if (index == 4) {
                return getContext().getResources().getString(33685790, daySeq);
            }
            if (index == 3) {
                return getContext().getResources().getString(33685791, hourSeq, minutesSeq);
            }
            if (index == 2) {
                return getContext().getResources().getString(33685792, hourSeq);
            }
            return getContext().getResources().getString(33685793, minutesSeq);
        }
    }

    public void initUI(Looper looper) {
        if (isHwGlobalTwoActionsSupport()) {
            initNewUI(looper);
            return;
        }
        Log.i(TAG, "HwGlobalActionsView initialize original layout");
        this.mWallpaperManager = (WallpaperManager) getContext().getSystemService("wallpaper");
        this.mWallpaperManager.setCallback(this);
        initHandler(looper);
        this.view_two_action = findViewById(34603174);
        if (this.view_two_action == null) {
            Log.e(TAG, "view_two_action is null");
        } else {
            ShutdownMenuAnimations.getInstance(getContext()).setTwoActionView(this.view_two_action);
        }
        this.view_four_action = findViewById(34603175);
        View view = this.view_four_action;
        if (view == null) {
            Log.e(TAG, "view_four_action is null");
        } else {
            view.setVisibility(4);
            ShutdownMenuAnimations.getInstance(getContext()).setFourActionView(this.view_four_action);
        }
        this.view_confirm_action = findViewById(34603176);
        if (this.view_confirm_action == null) {
            Log.e(TAG, "view_confirm_action is null");
        } else {
            ShutdownMenuAnimations.getInstance(getContext()).setConfirmActionView(this.view_confirm_action);
        }
        if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
            showNormalUI();
        } else {
            showBeforeProvisioningUI();
        }
        HwGlobalActionsData.getSingletoneInstance().registerActionStateObserver(this.mObserver);
        setTag(OTHERAREA_TAG);
        setOnClickListener(this.mActionClickListener);
        synchronized (this) {
            this.mToneGenerators = new ToneGenerator[AudioSystem.getNumStreamTypes()];
            this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
            this.mSilentModeButtonTouched = false;
        }
        this.mWindowTouchSlop = ViewConfiguration.get(this.mContext).getScaledWindowTouchSlop();
    }

    private void tryToAddLockdownAction() {
        if (Settings.Secure.getInt(getContext().getContentResolver(), "lockdown_in_power_menu", 0) != 0 && shouldDisplayLockdown()) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (inflater == null) {
                Log.w(TAG, "inflater is null!");
                return;
            }
            this.mLockdownView = inflater.inflate(34013226, (ViewGroup) null);
            if (this.mLockdownView == null) {
                Log.w(TAG, "mLockdownView is null!");
                return;
            }
            RelativeLayout shutdownMenuLayout = (RelativeLayout) this.view_two_action.findViewById(34603206);
            if (shutdownMenuLayout == null) {
                Log.w(TAG, "shutdownMenuLayout is null!");
                return;
            }
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getContext().getResources().getDimensionPixelSize(34472254), -1);
            lp.setMarginStart(getContext().getResources().getDimensionPixelSize(34472253));
            lp.addRule(6, 34603152);
            lp.addRule(17, 34603152);
            this.mLockdownView.setLayoutParams(lp);
            this.mLockdownView.setId(34603205);
            shutdownMenuLayout.addView(this.mLockdownView);
        }
    }

    public void initNewUI(Looper looper) {
        Log.i(TAG, "HwGlobalActionsView initialize new layout");
        this.mWallpaperManager = (WallpaperManager) getContext().getSystemService("wallpaper");
        this.mWallpaperManager.setCallback(this);
        initHandler(looper);
        this.view_two_action = findViewById(34603177);
        if (this.view_two_action == null) {
            Log.e(TAG, "view_two_action is null");
        } else {
            tryToAddLockdownAction();
            ShutdownMenuAnimations.getInstance(getContext()).setTwoActionView(this.view_two_action);
        }
        this.view_confirm_action = findViewById(34603178);
        if (this.view_confirm_action == null) {
            Log.e(TAG, "view_confirm_action is null");
        } else {
            ShutdownMenuAnimations.getInstance(getContext()).setConfirmActionView(this.view_confirm_action);
        }
        initKeyCombinationPowerOffHintView();
        showNewUI();
        HwGlobalActionsData.getSingletoneInstance().registerActionStateObserver(this.mObserver);
        setTag(OTHERAREA_TAG);
        setOnClickListener(this.mActionClickListener);
        synchronized (this) {
            this.mToneGenerators = new ToneGenerator[AudioSystem.getNumStreamTypes()];
            this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
            this.mSilentModeButtonTouched = false;
        }
        this.mWindowTouchSlop = ViewConfiguration.get(this.mContext).getScaledWindowTouchSlop();
    }

    private void initKeyCombinationPowerOffHintView() {
        if (getPowerKeyRestartType() == 1) {
            this.mKeyCombinationHintView = addPowerOffWithKeyCombinationView();
            if (this.mKeyCombinationHintView == null) {
                Log.e(TAG, "mKeyCombinationHintView is null");
            } else {
                ShutdownMenuAnimations.getInstance(getContext()).setKeyCombinationHintView(this.mKeyCombinationHintView);
            }
        }
    }

    public void deinitUI() {
        HwGlobalActionsData.getSingletoneInstance().unregisterActionStateObserver();
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(2);
            this.mHandler.removeMessages(4);
            onStopSounds();
            this.mHandler.removeMessages(5);
            this.mHandler.sendEmptyMessage(5);
        }
        this.isFirstShow = true;
    }

    private void updateActionUI(View v, int actionID, int idText, final int idImage) {
        RelativeLayout actionLayout;
        TextView actionTextView;
        if (v != null && (actionLayout = (RelativeLayout) v.findViewById(actionID)) != null && (actionTextView = (TextView) actionLayout.findViewById(34603129)) != null) {
            actionTextView.setText(idText);
            final ImageView actionImageView = (ImageView) actionLayout.findViewById(34603172);
            if (actionImageView != null) {
                Handler handler = this.mHandler;
                if (handler != null) {
                    handler.removeMessages(6);
                }
                if (idImage != ID_IC_SHUTDOWN) {
                    if (idImage != 33751785) {
                        switch (idImage) {
                            case ID_IC_AIRPLANEMODE /* 33751059 */:
                                actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_AIRPLANEMODE_OFF));
                                break;
                            case ID_IC_AIRPLANEMODE_ON /* 33751060 */:
                                actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_AIRPLANEMODE_ON));
                                break;
                            case ID_IC_SILENTMODE_SILENT /* 33751061 */:
                                actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_SILENTMODE));
                                break;
                            case ID_IC_SILENTMODE_VIBRATE /* 33751062 */:
                                actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_VIBRATIONMODE));
                                break;
                            case ID_IC_SILENTMODE_NORMAL /* 33751063 */:
                                actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_SOUNDMODE));
                                break;
                            default:
                                switch (idImage) {
                                }
                            case ID_IC_REBOOT /* 33751064 */:
                                actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_REBOOT));
                                break;
                        }
                    } else {
                        actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_LOCKDOWN));
                    }
                    Log.d(TAG, "updateActionUI mSilentModeButtonTouched = " + this.mSilentModeButtonTouched);
                    if ("factory".equals(SystemProperties.get("ro.runmode", "normal")) && actionID == 34603145) {
                        actionTextView.setTextColor(getContext().getResources().getColor(33882221));
                    }
                    if (actionID == 34603146 || this.isFirstShow) {
                        actionImageView.setImageResource(idImage);
                    }
                    Animation animRotation = ShutdownMenuAnimations.getInstance(getContext()).setSoundModeRotate();
                    animRotation.setAnimationListener(new Animation.AnimationListener() {
                        /* class huawei.com.android.server.policy.HwGlobalActionsView.AnonymousClass4 */

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationStart(Animation arg0) {
                            ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).setIsAnimRunning(true);
                            ImageView imageView = actionImageView;
                            if (imageView != null) {
                                imageView.setImageResource(idImage);
                            }
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationRepeat(Animation arg0) {
                        }

                        @Override // android.view.animation.Animation.AnimationListener
                        public void onAnimationEnd(Animation arg0) {
                            ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).setIsAnimRunning(false);
                            if (HwGlobalActionsView.this.mHandler != null) {
                                HwGlobalActionsView.this.mHandler.removeMessages(6);
                                HwGlobalActionsView.this.mHandler.sendEmptyMessageDelayed(6, HwGlobalActionsView.COUNT_DOWN_INTERVAL);
                            }
                        }
                    });
                    actionImageView.startAnimation(animRotation);
                    return;
                }
                actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_SHUTDOWN));
                Log.d(TAG, "updateActionUI mSilentModeButtonTouched = " + this.mSilentModeButtonTouched);
                actionTextView.setTextColor(getContext().getResources().getColor(33882221));
                if (actionID == 34603146) {
                }
                actionImageView.setImageResource(idImage);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAirplaneModeUI(View v) {
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            updateActionUI(this.view_four_action, 34603145, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE_OFF);
        } else if ((HwGlobalActionsData.getSingletoneInstance().getState() & 1) != 0) {
            updateActionUI(this.view_four_action, 34603145, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE_ON);
        } else {
            updateActionUI(this.view_four_action, 34603145, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSilentModeUI(View v) {
        Handler handler = this.mHandler;
        if (handler != null && this.mSilentModeButtonTouched) {
            handler.removeMessages(2);
            this.mHandler.removeMessages(4);
        }
        if ((HwGlobalActionsData.getSingletoneInstance().getState() & 16) != 0) {
            updateActionUI(this.view_four_action, 34603146, ID_STR_SILENTMODE, ID_IC_SILENTMODE_SILENT);
        } else if ((HwGlobalActionsData.getSingletoneInstance().getState() & 32) != 0) {
            updateActionUI(this.view_four_action, 34603146, ID_STR_VIBRATIONMODE, ID_IC_SILENTMODE_VIBRATE);
            Handler handler2 = this.mHandler;
            if (handler2 != null && this.mSilentModeButtonTouched) {
                handler2.sendMessageDelayed(handler2.obtainMessage(4), 300);
            }
        } else {
            updateActionUI(this.view_four_action, 34603146, ID_STR_SOUNDMODE, ID_IC_SILENTMODE_NORMAL);
            Handler handler3 = this.mHandler;
            if (handler3 != null && this.mSilentModeButtonTouched) {
                handler3.removeMessages(2);
                Handler handler4 = this.mHandler;
                handler4.sendMessageDelayed(handler4.obtainMessage(2, 2, 0), 300);
            }
        }
    }

    private void showNewRebootAndShutdownUI(View v) {
        updateActionUI(v, 34603151, ID_STR_REBOOT, 33751230);
        setActionClickListener(v, 34603151, REBOOT_TAG);
        updateActionUI(v, 34603152, ID_STR_SHUTDOWN, 33751229);
        setActionClickListener(v, 34603152, SHUTDOWN_TAG);
        updateActionUI(v, 34603205, ID_STR_LOCKDOWN, 33751785);
        setActionClickListener(v, 34603205, LOCKDOWN_TAG);
        if (this.mIsTelevisionMode) {
            updateActionUI(v, 34603468, ID_STR_SCREENOFF, ID_IC_TV_SCREENOFF);
            setActionClickListener(v, 34603468, SCREENOFF_TAG);
            createResMap();
        }
    }

    private void showRebootAndShutdownUI(View v) {
        updateActionUI(v, 34603151, ID_STR_REBOOT, ID_IC_REBOOT);
        setActionClickListener(v, 34603151, REBOOT_TAG);
        updateActionUI(v, 34603152, ID_STR_SHUTDOWN, ID_IC_SHUTDOWN);
        setActionClickListener(v, 34603152, SHUTDOWN_TAG);
    }

    private void setActionClickListener(View v, int actionID, Object actionTag) {
        RelativeLayout action;
        View icon_frame;
        if (v != null && (action = (RelativeLayout) v.findViewById(actionID)) != null && (icon_frame = action.findViewById(34603108)) != null) {
            icon_frame.setTag(actionTag);
            icon_frame.setOnClickListener(this.mActionClickListener);
            if (this.mIsTelevisionMode) {
                icon_frame.setOnFocusChangeListener(this.mFocusListener);
                icon_frame.setOnKeyListener(this.mKeyListener);
            }
        }
    }

    private UserInfo getCurrentUser() {
        try {
            return ActivityManager.getService().getCurrentUser();
        } catch (RemoteException e) {
            Log.e(TAG, "error happen while getCurrentUser(): ");
            return null;
        }
    }

    private boolean shouldDisplayLockdown() {
        UserInfo userInfo = getCurrentUser();
        if (userInfo == null) {
            Log.w(TAG, "shouldDisplayLockdown: get user info failed!");
            return false;
        }
        int userId = userInfo.id;
        KeyguardManager keyguardManager = (KeyguardManager) getContext().getSystemService("keyguard");
        if (keyguardManager == null) {
            Log.w(TAG, "shouldDisplayLockdown: keyguardManager is null!");
            return false;
        } else if ((!userInfo.isPrimary() && !keyguardManager.isDeviceSecure(0)) || !keyguardManager.isDeviceSecure(userId)) {
            return false;
        } else {
            int state = new LockPatternUtils(getContext()).getStrongAuthForUser(userId);
            if (state == 0 || state == 4) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void showNewUI() {
        View view;
        boolean z = true;
        if (this.mInitedConfirm || (view = this.view_two_action) == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("showNewUI: mInitedConfirm is true or view_two_action is null ");
            if (this.view_two_action != null) {
                z = false;
            }
            sb.append(z);
            Log.e(TAG, sb.toString());
            return;
        }
        view.setVisibility(0);
        View view2 = this.view_confirm_action;
        if (view2 == null) {
            Log.e(TAG, "showNewUI: view_confirm_action is null");
        } else {
            view2.setVisibility(4);
        }
        this.isFirstShow = true;
        this.mIsCountDownTick = true;
        this.mSilentModeButtonTouched = false;
        this.mRebootView = this.view_two_action.findViewById(34603151);
        this.mShutdownView = this.view_two_action.findViewById(34603152);
        replacePoweroffHint((TextView) this.view_two_action.findViewById(34603150));
        this.mViewList.clear();
        configureTalkbackView(this);
        if (!ShutdownMenuAnimations.isSuperLiteMode()) {
            this.mEnterSet = ShutdownMenuAnimations.getInstance(getContext()).setNewShutdownViewAnimation(true);
            this.mEnterSet.start();
        }
        showNewRebootAndShutdownUI(this.view_two_action);
        if (this.mIsTelevisionMode) {
            this.mScreenoffView = this.view_two_action.findViewById(34603468);
            View view3 = this.mScreenoffView;
            if (view3 != null) {
                view3.setNextFocusRightId(34603152);
                this.mScreenoffView.requestFocus();
            } else {
                Log.e(TAG, "television mode, but layout don't inflater right！");
            }
            this.mShutdownView.setNextFocusLeftId(34603468);
            this.mShutdownView.setNextFocusRightId(34603151);
            this.mRebootView.setNextFocusLeftId(34603152);
        } else {
            Log.w(TAG, "Non television mode");
        }
        this.isFirstShow = false;
    }

    /* access modifiers changed from: protected */
    public void showBeforeProvisioningUI() {
        if (isHwGlobalTwoActionsSupport()) {
            showNewUI();
            return;
        }
        this.view_four_action.setVisibility(4);
        this.view_two_action.setVisibility(0);
        this.view_confirm_action.setVisibility(4);
        this.mViewList.clear();
        ShutdownMenuAnimations.getInstance(getContext()).setMenuViewList(this.mViewList);
        this.mRebootView = this.view_two_action.findViewById(34603151);
        this.mShutdownView = this.view_two_action.findViewById(34603152);
        replacePoweroffHint((TextView) this.view_two_action.findViewById(34603150));
        showRebootAndShutdownUI(this.view_two_action);
    }

    /* access modifiers changed from: protected */
    public void showNormalUI() {
        if (isHwGlobalTwoActionsSupport()) {
            showNewUI();
            return;
        }
        this.isFirstShow = true;
        this.mSilentModeButtonTouched = false;
        this.mAirplanemodeView = this.view_four_action.findViewById(34603145);
        this.mSilentmodeView = this.view_four_action.findViewById(34603146);
        this.mRebootView = this.view_four_action.findViewById(34603151);
        this.mShutdownView = this.view_four_action.findViewById(34603152);
        replacePoweroffHint((TextView) this.view_four_action.findViewById(34603150));
        this.mViewList.clear();
        this.mViewList.add(this.mAirplanemodeView);
        this.mViewList.add(this.mSilentmodeView);
        this.mViewList.add(this.mRebootView);
        this.mViewList.add(this.mShutdownView);
        configureTalkbackView(this);
        ShutdownMenuAnimations.getInstance(getContext()).setMenuViewList(this.mViewList);
        this.mEnterSet = ShutdownMenuAnimations.getInstance(getContext()).setImageAnimation(true);
        this.view_four_action.setVisibility(0);
        this.mEnterSet.start();
        updateAirplaneModeUI(this.view_four_action);
        setActionClickListener(this.view_four_action, 34603145, AIRPLANEMODE_TAG);
        updateSilentModeUI(this.view_four_action);
        setActionClickListener(this.view_four_action, 34603146, SILENTMODE_TAG);
        showRebootAndShutdownUI(this.view_four_action);
        this.isFirstShow = false;
    }

    /* access modifiers changed from: protected */
    public void onPlaySound(int streamType, int flags) {
        Handler handler = this.mHandler;
        if (handler != null && handler.hasMessages(3)) {
            this.mHandler.removeMessages(3);
            onStopSounds();
        }
        synchronized (this) {
            ToneGenerator toneGen = getOrCreateToneGenerator(streamType);
            if (toneGen != null) {
                toneGen.startTone(24);
                if (this.mHandler != null) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 150);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onStopSounds() {
        synchronized (this) {
            for (int i = AudioSystem.getNumStreamTypes() - 1; i >= 0; i--) {
                ToneGenerator toneGen = this.mToneGenerators[i];
                if (toneGen != null) {
                    toneGen.stopTone();
                }
            }
            if (this.mHandler != null) {
                this.mHandler.removeMessages(5);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5), HwArbitrationDEFS.TIMEOUT_FOR_QUERY_QOE_WM);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onVibrate() {
        this.mVibrator.vibrate(300, VIBRATION_ATTRIBUTES);
    }

    private ToneGenerator getOrCreateToneGenerator(int streamType) {
        ToneGenerator toneGenerator;
        synchronized (this) {
            if (this.mToneGenerators[streamType] == null) {
                try {
                    this.mToneGenerators[streamType] = new ToneGenerator(streamType, 100);
                } catch (RuntimeException e) {
                }
            }
            toneGenerator = this.mToneGenerators[streamType];
        }
        return toneGenerator;
    }

    /* access modifiers changed from: protected */
    public void onFreeResources() {
        synchronized (this) {
            for (int i = this.mToneGenerators.length - 1; i >= 0; i--) {
                if (this.mToneGenerators[i] != null) {
                    this.mToneGenerators[i].release();
                }
                this.mToneGenerators[i] = null;
            }
        }
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (this.mIsTelevisionMode || !handleKeyEvent(event)) {
            return super.dispatchKeyEvent(event);
        }
        return true;
    }

    private boolean handleKeyEvent(KeyEvent event) {
        View view;
        View icon_frame;
        View view2;
        View view3;
        View view4;
        View view5;
        if (event.getAction() == 1) {
            int keyCode = event.getKeyCode();
            if (keyCode == 4) {
                ActionPressedCallback actionPressedCallback = this.mCallback;
                if (actionPressedCallback != null) {
                    actionPressedCallback.onOtherAreaPressed();
                    return true;
                }
            } else if (keyCode == 66 || keyCode == 160) {
                if (isHwGlobalTwoActionsSupport() && (view = this.view_confirm_action) != null && view.getVisibility() == 0 && (icon_frame = this.view_confirm_action.findViewById(34603108)) != null) {
                    icon_frame.performClick();
                }
                return true;
            } else if (keyCode != 21) {
                if (keyCode == 22 && isHwGlobalTwoActionsSupport() && (view4 = this.view_two_action) != null && view4.getVisibility() == 0) {
                    if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 0) {
                        view5 = this.mShutdownView;
                    } else {
                        view5 = this.mRebootView;
                    }
                    if (view5 != null) {
                        view5.findViewById(34603108).performClick();
                        return true;
                    }
                }
            } else if (isHwGlobalTwoActionsSupport() && (view2 = this.view_two_action) != null && view2.getVisibility() == 0) {
                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 0) {
                    view3 = this.mRebootView;
                } else {
                    view3 = this.mShutdownView;
                }
                if (view3 != null) {
                    view3.findViewById(34603108).performClick();
                    return true;
                }
            }
        }
        return false;
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (canDisableAccessibilityViaGesture()) {
            if (!this.mDisableIntercepted) {
                onDisableInterceptTouchEvent(event);
            } else if (onDisableTouchEvent(event)) {
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void showRestartingHint() {
        TextView textView;
        View view = this.view_confirm_action;
        if (view != null && (textView = (TextView) view.findViewById(34603129)) != null) {
            textView.setText(ID_STR_REBOOTING);
        }
    }

    public void showShutdongingHint() {
        TextView textView;
        View view = this.view_confirm_action;
        if (view != null && (textView = (TextView) view.findViewById(34603129)) != null) {
            textView.setText(ID_STR_SHUTDOWNING);
        }
    }

    private boolean canDisableAccessibilityViaGesture() {
        if (!SystemProperties.getBoolean(TALKBACK_CONFIG, true) || !isTalkBackServicesOn()) {
            return false;
        }
        return true;
    }

    private boolean onDisableInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != 0) {
            return false;
        }
        this.mFirstPointerDownX = event.getX(0);
        this.mFirstPointerDownY = event.getY(0);
        this.mDisableIntercepted = true;
        this.mEventAfterActionDown = true;
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0016, code lost:
        if (r1 != 6) goto L_0x0050;
     */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0059 A[RETURN] */
    private boolean onDisableTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();
        if (action != 1) {
            if (action != 2) {
                if (action != 3) {
                    if (action == 5) {
                        if (pointerCount > 2) {
                            cancelDisableAccssibility();
                        }
                    }
                }
            } else if (Math.abs(MathUtils.dist(event.getX(0), event.getY(0), this.mFirstPointerDownX, this.mFirstPointerDownY)) > this.mTouchSlop) {
                cancelDisableAccssibility();
            } else {
                Handler handler = this.mHandler;
                if (handler != null && this.mEventAfterActionDown) {
                    handler.sendEmptyMessageDelayed(7, 3000);
                }
            }
            this.mEventAfterActionDown = false;
            if (this.mCanceled) {
                return true;
            }
            this.mCanceled = false;
            return false;
        }
        cancelDisableAccssibility();
        this.mEventAfterActionDown = false;
        if (this.mCanceled) {
        }
    }

    private void configureTalkbackView(Object mView) {
        this.mAccessibilityTip = (TextView) ((HwGlobalActionsView) mView).findViewById(34603179);
        if (this.mAccessibilityTip != null && this.mContext != null) {
            if (canDisableAccessibilityViaGesture()) {
                int i = this.mTalkBackScreenReaderStatus;
                if (i == 1) {
                    this.mAccessibilityTip.setText(this.mContext.getString(33685525, 3));
                } else if (i == 2) {
                    this.mAccessibilityTip.setText(this.mContext.getString(33685524, 3));
                } else if (i == 3) {
                    this.mAccessibilityTip.setText(this.mContext.getString(33685527, 3));
                }
                this.mAccessibilityTip.setVisibility(0);
                return;
            }
            this.mAccessibilityTip.setVisibility(8);
        }
    }

    private void cancelDisableAccssibility() {
        this.mCanceled = true;
        this.mDisableIntercepted = false;
        Handler handler = this.mHandler;
        if (handler != null) {
            handler.removeMessages(7);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disableTalkBackService() {
        if (this.mContext != null) {
            boolean accessibilityEnabled = false;
            loadInstalledServices();
            Set<ComponentName> enabledServices = getEnabledServicesFromSettings();
            ComponentName talkBackComponentName = new ComponentName(TALKBACK_SERVICE_PACKAGE_NAME, TALKBACK_SERVICE_CLASS_NAME);
            ComponentName screenReaderComponentName = new ComponentName(SCREENREADER_SERVICE_PACKAGE_NAME, SCREENREADER_SERVICE_CLASS_NAME);
            int i = this.mTalkBackScreenReaderStatus;
            if (i != 0) {
                int i2 = 1;
                if (i == 1) {
                    enabledServices.remove(talkBackComponentName);
                } else if (i == 2) {
                    enabledServices.remove(screenReaderComponentName);
                } else {
                    enabledServices.remove(talkBackComponentName);
                    enabledServices.remove(screenReaderComponentName);
                }
                Set<ComponentName> installedServices = sInstalledServices;
                StatisticalUtils.reportc(this.mContext, 24);
                Iterator<ComponentName> it = enabledServices.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (installedServices.contains(it.next())) {
                            accessibilityEnabled = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                StringBuilder enabledServicesBuilder = new StringBuilder();
                for (ComponentName enabledService : enabledServices) {
                    enabledServicesBuilder.append(enabledService.flattenToString());
                    enabledServicesBuilder.append(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
                }
                int enabledServicesBuilderLength = enabledServicesBuilder.length();
                if (enabledServicesBuilderLength > 0) {
                    enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
                }
                Settings.Secure.putStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", enabledServicesBuilder.toString(), -2);
                ContentResolver contentResolver = this.mContext.getContentResolver();
                if (!accessibilityEnabled) {
                    i2 = 0;
                }
                Settings.Secure.putIntForUser(contentResolver, "accessibility_enabled", i2, -2);
                TextView textView = this.mAccessibilityTip;
                if (textView != null) {
                    textView.setVisibility(8);
                }
            }
        }
    }

    private Set<ComponentName> getEnabledServicesFromSettings() {
        String enabledServicesSetting = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", -2);
        if (enabledServicesSetting == null) {
            enabledServicesSetting = "";
        }
        Set<ComponentName> enabledServices = new HashSet<>();
        TextUtils.SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enabledServicesSetting);
        while (colonSplitter.hasNext()) {
            ComponentName enabledService = ComponentName.unflattenFromString(colonSplitter.next());
            if (enabledService != null) {
                enabledServices.add(enabledService);
            }
        }
        return enabledServices;
    }

    private void loadInstalledServices() {
        Set<ComponentName> installedServices = sInstalledServices;
        installedServices.clear();
        List<AccessibilityServiceInfo> installedServiceInfos = AccessibilityManager.getInstance(this.mContext).getInstalledAccessibilityServiceList();
        if (installedServiceInfos != null) {
            int installedServiceInfoCount = installedServiceInfos.size();
            for (int i = 0; i < installedServiceInfoCount; i++) {
                ResolveInfo resolveInfo = installedServiceInfos.get(i).getResolveInfo();
                if (!(resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.packageName == null || resolveInfo.serviceInfo.name == null)) {
                    installedServices.add(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name));
                }
            }
        }
    }

    private Integer getTalkBackScreenReaderStatus(Context context) {
        String enabledServices;
        boolean accessibilityEnabled = false;
        if (context == null) {
            return 0;
        }
        if (Settings.Secure.getIntForUser(context.getContentResolver(), "accessibility_enabled", 0, -2) == 1) {
            accessibilityEnabled = true;
        }
        if (!accessibilityEnabled || (enabledServices = Settings.Secure.getStringForUser(context.getContentResolver(), "enabled_accessibility_services", -2)) == null) {
            return 0;
        }
        ComponentName talkBackComponentName = new ComponentName(TALKBACK_SERVICE_PACKAGE_NAME, TALKBACK_SERVICE_CLASS_NAME);
        ComponentName screenReaderComponentName = new ComponentName(SCREENREADER_SERVICE_PACKAGE_NAME, SCREENREADER_SERVICE_CLASS_NAME);
        boolean isContainsTalkBackService = enabledServices.contains(talkBackComponentName.flattenToString());
        boolean isContainsScreenReaderService = enabledServices.contains(screenReaderComponentName.flattenToString());
        if (isContainsTalkBackService && isContainsScreenReaderService) {
            return 3;
        }
        if (isContainsTalkBackService) {
            return 1;
        }
        if (isContainsScreenReaderService) {
            return 2;
        }
        return 0;
    }

    private boolean isTalkBackServicesOn() {
        if (this.mContext == null) {
            return false;
        }
        this.mTalkBackScreenReaderStatus = getTalkBackScreenReaderStatus(this.mContext).intValue();
        if (this.mTalkBackScreenReaderStatus != 0) {
            return true;
        }
        return false;
    }

    private void replacePoweroffHint(TextView poweroffHintView) {
        if (poweroffHintView != null) {
            Resources tmpResources = this.mContext.getResources();
            int poweroffHintTime = tmpResources.getInteger(34275331);
            if (getPowerKeyRestartType() == 1) {
                poweroffHintView.setText(String.format(tmpResources.getString(33685668), Integer.valueOf(poweroffHintTime)));
                updatePoweroffHintColor(poweroffHintView, tmpResources);
            } else {
                poweroffHintView.setText(String.format(tmpResources.getString(33685845), Integer.valueOf(poweroffHintTime)));
            }
            if (this.mIsWatchMode) {
                poweroffHintView.setText(String.format(tmpResources.getString(33685667), Integer.valueOf(poweroffHintTime)));
            }
        }
    }

    private void updatePoweroffHintColor(TextView hintView, Resources resources) {
        int entryCount = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), HWGLOBALACTIONS_ENTRY_COUNT, 0, ActivityManager.getCurrentUser());
        if (entryCount < 3) {
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), HWGLOBALACTIONS_ENTRY_COUNT, entryCount + 1, ActivityManager.getCurrentUser());
            TypedValue outValue = new TypedValue();
            this.mContext.getTheme().resolveAttribute(33620227, outValue, true);
            try {
                hintView.setTextColor(resources.getColor(outValue.resourceId));
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "color not found.");
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getPowerKeyRestartType() {
        return SystemProperties.getInt(RO_BOOT_POWERKEY_RESTART_TYPE, 0);
    }

    private View addPowerOffWithKeyCombinationView() {
        View hintView = LayoutInflater.from(this.mContext).inflate(34013461, (ViewGroup) null, false);
        if (hintView == null) {
            Log.e(TAG, "viewstub_new_poweroff_with_key_combination_hint is null");
            return null;
        }
        hintView.setVisibility(4);
        Resources tmpResources = this.mContext.getResources();
        int poweroffHintTime = tmpResources.getInteger(34275331);
        ((TextView) hintView.findViewById(34603450)).setText(33685670);
        ((TextView) hintView.findViewById(34603449)).setText(String.format(tmpResources.getString(33685669), Integer.valueOf(poweroffHintTime)));
        addView(hintView, new LinearLayout.LayoutParams(-1, -1));
        return hintView;
    }

    public boolean isHwGlobalTwoActionsSupport() {
        return this.mContext.getResources().getBoolean(34537474);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Ringtone ringtone = this.mTone;
        if (ringtone != null) {
            ringtone.stop();
        }
        if (this.mIsTelevisionMode) {
            cancelCountDownTimer();
        }
    }

    private boolean isSystemApp(String packageName) {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            return false;
        }
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            if (appInfo == null || (appInfo.flags & 1) == 0) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "package not found.");
        }
    }

    private String getSystemAppForDeskClock(String packageNameNew, String packageNameOld) {
        if (isSystemApp(packageNameNew)) {
            return packageNameNew;
        }
        if (isSystemApp(packageNameOld)) {
            return packageNameOld;
        }
        return "";
    }

    private String getDeskClockName() {
        if ("".equals(this.mDeskClockName)) {
            this.mDeskClockName = getSystemAppForDeskClock(DESKCLOCK_PACKAGENAME_NEW, DESKCLOCK_PACKAGENAME_OLD);
        }
        return this.mDeskClockName;
    }

    /* access modifiers changed from: package-private */
    public static class InnerResCollection {
        private int mResBackgroundId;
        private int mResDrawableId;
        private int mResLayoutId;

        public InnerResCollection(int resLayoutId, int resDrawableId, int resBackgroundId) {
            this.mResLayoutId = resLayoutId;
            this.mResDrawableId = resDrawableId;
            this.mResBackgroundId = resBackgroundId;
        }

        public int getResBackgroundId() {
            return this.mResBackgroundId;
        }

        public int getResLayoutId() {
            return this.mResLayoutId;
        }

        public int getResDrawableId() {
            return this.mResDrawableId;
        }
    }

    private void createResMap() {
        this.mResMap = new HashMap(3);
        this.mResIdMap = new HashMap(6);
        this.mResIdMap.put(REBOOT_TAG, 0);
        this.mResIdMap.put(SHUTDOWN_TAG, 1);
        this.mResIdMap.put(SCREENOFF_TAG, 2);
        this.mResMap.put(0, new InnerResCollection(34603151, 33751230, ID_DRAWABLE_BACKGROUND_DEFAULT));
        this.mResMap.put(1, new InnerResCollection(34603151, ID_DRAWABLE_REBOOT_FOCUS, ID_DRAWABLE_BACKGROUND_FOCUS));
        this.mResMap.put(2, new InnerResCollection(34603152, 33751229, ID_DRAWABLE_BACKGROUND_DEFAULT));
        this.mResMap.put(3, new InnerResCollection(34603152, ID_DRAWABLE_SHUTDOWN_FOCUS, ID_DRAWABLE_BACKGROUND_FOCUS));
        this.mResMap.put(4, new InnerResCollection(34603468, ID_IC_TV_SCREENOFF, ID_DRAWABLE_BACKGROUND_DEFAULT));
        this.mResMap.put(5, new InnerResCollection(34603468, ID_DRAWABLE_SCREENOFF_FOCUS, ID_DRAWABLE_BACKGROUND_FOCUS));
    }

    @Override // android.view.View
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event != null) {
            if (event.getEventType() == 32) {
                event.getText().add(this.mAccessibilityDescription);
            }
            super.onPopulateAccessibilityEvent(event);
        }
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return false;
        }
        onPopulateAccessibilityEvent(event);
        return false;
    }
}
