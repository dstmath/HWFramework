package huawei.com.android.server.policy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.AnimatorSet;
import android.app.AbsWallpaperManagerInner;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.HwWallpaperManager;
import android.app.KeyguardManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
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
import android.os.Bundle;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.gesture.GestureNavConst;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HwGlobalActionsView extends RelativeLayout implements AbsWallpaperManagerInner.IBlurWallpaperCallback {
    private static final String AIRPLANEMODE_TAG = "airplane_mode";
    private static final int BEEP_DURATION = 150;
    public static final boolean BLUR_SCREENSHOT = true;
    public static final boolean DEBUG = false;
    private static final String DESKCLOCK_PACKAGENAME = "com.android.deskclock";
    private static final int DISABLE_ACCESSIBILITY_DELAY_MILLIS = 3000;
    private static final int DISMISSS_DELAY_0 = 0;
    private static final int DISMISSS_DELAY_1000 = 1000;
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final int FIX_BUG_ALPHA = 254;
    private static final int FREE_DELAY = 10000;
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
    private static final int ID_STR_AIRPLANEMODE = 33685734;
    private static final int ID_STR_AIRPLANEMODE_OFF = 33685744;
    private static final int ID_STR_AIRPLANEMODE_ON = 33685743;
    private static final int ID_STR_LOCKDOWN = 33686054;
    private static final int ID_STR_LOCKDOWN_CONFIRM = 33686055;
    private static final int ID_STR_REBOOT = 33685740;
    private static final int ID_STR_REBOOTING = 33685784;
    private static final int ID_STR_REBOOT_CONFIRM = 33685741;
    private static final int ID_STR_SHUTDOWN = 33685738;
    private static final int ID_STR_SHUTDOWNING = 33685783;
    private static final int ID_STR_SHUTDOWN_CONFIRM = 33685739;
    private static final int ID_STR_SILENTMODE = 33685735;
    private static final int ID_STR_SOUNDMODE = 33685736;
    private static final int ID_STR_TOUCH_TO_GO_BACK = 33685742;
    private static final int ID_STR_VIBRATIONMODE = 33685737;
    private static final boolean IS_POWEROFF_ALARM_ENABLED = "true".equals(SystemProperties.get("ro.poweroff_alarm", "true"));
    static final String LOCKDOWN_TAG = "lockdown";
    private static final int MAX_VOLUME = 100;
    private static final int MESSAGE_DISABLE_ACCESSIBILITY = 7;
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
    private static final String OTHERAREA_TAG = "other_area";
    private static final int PLAY_SOUND_DELAY = 300;
    static final String REBOOT_TAG = "reboot";
    private static final String SETTINGS_PACKAGENAME = "com.android.providers.settings";
    static final String SHUTDOWN_TAG = "shutdown";
    private static final String SILENTMODE_TAG = "silent_mode";
    private static final String TAG = "HwGlobalActions";
    private static final String TALKBACK_COMPONENT_NAME = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final int TWO_BOOT_DLISTVIEW = 3;
    private static final int TWO_DESKCLOCK_DLISTVIEW = 2;
    /* access modifiers changed from: private */
    public static int TYPEDESKCLOCK = 1;
    /* access modifiers changed from: private */
    public static int TYPESETTINGS = 2;
    public static final int VIBRATE_DELAY = 300;
    private static final int VIBRATE_DURATION = 300;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    /* access modifiers changed from: private */
    public static long mShowBootOnTime = -1;
    /* access modifiers changed from: private */
    public static long mShowDeskClockTime = -1;
    /* access modifiers changed from: private */
    public static Boolean[] mState = {true, true};
    private static final Set<ComponentName> sInstalledServices = new HashSet();
    private static final TextUtils.SimpleStringSplitter sStringColonSplitter = new TextUtils.SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
    private boolean isFirstShow;
    private View.AccessibilityDelegate mAccessibilityDelegate;
    private TextView mAccessibilityTip;
    private View.OnClickListener mActionClickListener;
    private View mAirplanemodeView;
    /* access modifiers changed from: private */
    public ActionPressedCallback mCallback;
    private boolean mCanceled;
    private boolean mDisableIntercepted;
    private Drawable mDrawable;
    private AnimatorSet mEnterSet;
    private boolean mEventAfterActionDown;
    private float mFirstPointerDownX;
    private float mFirstPointerDownY;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private boolean mInitedConfirm;
    /* access modifiers changed from: private */
    public boolean mIsBootOnTimeClose;
    /* access modifiers changed from: private */
    public boolean mIsDeskClockClose;
    /* access modifiers changed from: private */
    public long mLastClickTime;
    /* access modifiers changed from: private */
    public int mListviewState;
    private View mLockdownView;
    private MyActionStateObserver mObserver;
    private View mRebootView;
    /* access modifiers changed from: private */
    public ArrayList<String> mShutdownListViewText;
    /* access modifiers changed from: private */
    public MyAdapter mShutdownListviewAdapter;
    private View mShutdownView;
    /* access modifiers changed from: private */
    public boolean mSilentModeButtonTouched;
    private View mSilentmodeView;
    /* access modifiers changed from: private */
    public Ringtone mTone;
    private ToneGenerator[] mToneGenerators;
    private float mTouchSlop;
    private Vibrator mVibrator;
    ArrayList<View> mViewList;
    private WallpaperManager mWallpaperManager;
    private int mWindowTouchSlop;
    private ListView shutdown_listview;
    private View view_confirm_action;
    /* access modifiers changed from: private */
    public View view_four_action;
    private View view_two_action;

    public interface ActionPressedCallback {
        void dismissShutdownMenu(int i);

        void onAirplaneModeActionPressed();

        void onLockdownActionPressed();

        void onOtherAreaPressed();

        void onRebootActionPressed();

        void onShutdownActionPressed(boolean z, boolean z2, int i);

        void onSilentModeActionPressed();
    }

    private final class MyActionStateObserver implements HwGlobalActionsData.ActionStateObserver {
        public MyActionStateObserver() {
        }

        public void onAirplaneModeActionStateChanged() {
            if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.updateAirplaneModeUI(HwGlobalActionsView.this.view_four_action);
            }
        }

        public void onSilentModeActionStateChanged() {
            if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.updateSilentModeUI(HwGlobalActionsView.this.view_four_action);
            }
        }

        public void onRebootActionStateChanged() {
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & 512) != 0) {
                HwGlobalActionsView.this.initConfirmAction(512);
            } else if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.showNormalUI();
            } else {
                HwGlobalActionsView.this.showBeforeProvisioningUI();
            }
        }

        public void onShutdownActionStateChanged() {
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & 8192) != 0) {
                HwGlobalActionsView.this.initConfirmAction(8192);
            } else if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.showNormalUI();
            } else {
                HwGlobalActionsView.this.showBeforeProvisioningUI();
            }
        }

        public void onLockdownActionStateChanged() {
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & 131072) != 0) {
                HwGlobalActionsView.this.initConfirmAction(131072);
            } else if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.showNormalUI();
            } else {
                HwGlobalActionsView.this.showBeforeProvisioningUI();
            }
        }
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater = null;
        private ArrayList<String> list;

        public MyAdapter(ArrayList<String> list2, Context context) {
            this.list = list2;
            this.inflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return this.list.size();
        }

        public Object getItem(int position) {
            return this.list.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: huawei.com.android.server.policy.HwGlobalActionsView$ViewHolder} */
        /* JADX WARNING: Multi-variable type inference failed */
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                if (HwGlobalActionsView.this.isHwGlobalTwoActionsSupport()) {
                    convertView = this.inflater.inflate(34013227, null);
                } else {
                    convertView = this.inflater.inflate(34013223, null);
                }
                holder.checkBox = (CheckBox) convertView.findViewById(34603173);
                convertView.setTag(holder);
            } else {
                holder = convertView.getTag();
            }
            holder.checkBox.setText(this.list.get(position));
            holder.checkBox.setOnCheckedChangeListener(new SearchItemOnCheckedChangeListener(position, HwGlobalActionsView.mState));
            return convertView;
        }
    }

    public class SearchItemOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        private int id;
        private Boolean[] state;

        public SearchItemOnCheckedChangeListener(int id2, Boolean[] state2) {
            this.id = id2;
            this.state = state2;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(HwGlobalActionsView.TAG, "-----SearchItemOnCheckedChangeListener---" + isChecked + ", id = " + this.id);
            switch (HwGlobalActionsView.this.mListviewState) {
                case 1:
                    boolean unused = HwGlobalActionsView.this.mIsDeskClockClose = false;
                    boolean unused2 = HwGlobalActionsView.this.mIsBootOnTimeClose = false;
                    break;
                case 2:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    boolean unused3 = HwGlobalActionsView.this.mIsDeskClockClose = this.state[0].booleanValue();
                    if (!isChecked && this.id == 0) {
                        int unused4 = HwGlobalActionsView.this.mListviewState = 6;
                        boolean unused5 = HwGlobalActionsView.this.mIsBootOnTimeClose = true;
                        HwGlobalActionsView.this.mShutdownListViewText.clear();
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowDeskClockTime, HwGlobalActionsView.TYPEDESKCLOCK));
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowBootOnTime, HwGlobalActionsView.TYPESETTINGS));
                        break;
                    }
                case 3:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    boolean unused6 = HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[0].booleanValue();
                    if (!isChecked && this.id == 0) {
                        int unused7 = HwGlobalActionsView.this.mListviewState = 7;
                        boolean unused8 = HwGlobalActionsView.this.mIsDeskClockClose = true;
                        HwGlobalActionsView.this.mShutdownListViewText.clear();
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowBootOnTime, HwGlobalActionsView.TYPESETTINGS));
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowDeskClockTime, HwGlobalActionsView.TYPEDESKCLOCK));
                        break;
                    }
                case 4:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    boolean unused9 = HwGlobalActionsView.this.mIsDeskClockClose = this.state[0].booleanValue();
                    boolean unused10 = HwGlobalActionsView.this.mIsBootOnTimeClose = false;
                    break;
                case 5:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    boolean unused11 = HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[0].booleanValue();
                    boolean unused12 = HwGlobalActionsView.this.mIsDeskClockClose = false;
                    break;
                case 6:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    boolean unused13 = HwGlobalActionsView.this.mIsDeskClockClose = this.state[0].booleanValue();
                    boolean unused14 = HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[1].booleanValue();
                    break;
                case 7:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    boolean unused15 = HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[0].booleanValue();
                    boolean unused16 = HwGlobalActionsView.this.mIsDeskClockClose = this.state[1].booleanValue();
                    break;
            }
            Boolean[] unused17 = HwGlobalActionsView.mState = this.state;
            Log.d(HwGlobalActionsView.TAG, "SearchItemOnCheckedChangeListener,,mListviewState = " + HwGlobalActionsView.this.mListviewState + ",mIsBootOnTimeClose =" + HwGlobalActionsView.this.mIsBootOnTimeClose + ",mIsDeskClockClose = " + HwGlobalActionsView.this.mIsDeskClockClose);
            HwGlobalActionsView.this.mShutdownListviewAdapter.notifyDataSetChanged();
            StatisticalUtils.reporte(HwGlobalActionsView.this.mContext, 23, String.format("{DeskClock:%s, BootOnTime:%s}", new Object[]{Boolean.valueOf(HwGlobalActionsView.this.mIsDeskClockClose), Boolean.valueOf(HwGlobalActionsView.this.mIsBootOnTimeClose)}));
        }
    }

    static class ViewHolder {
        public CheckBox checkBox;

        ViewHolder() {
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
        this.isFirstShow = true;
        this.mIsDeskClockClose = true;
        this.mIsBootOnTimeClose = true;
        this.mListviewState = 0;
        this.mShutdownListViewText = new ArrayList<>();
        this.mLastClickTime = 0;
        this.mDisableIntercepted = false;
        this.mCanceled = false;
        this.mTouchSlop = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mFirstPointerDownX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mFirstPointerDownY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mAccessibilityTip = null;
        this.mTone = null;
        this.mEventAfterActionDown = false;
        this.mInitedConfirm = false;
        this.mAccessibilityDelegate = new View.AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                if (host == HwGlobalActionsView.this) {
                    HwGlobalActionsView.this.setContentDescription(HwGlobalActionsView.this.getContext().getResources().getString(HwGlobalActionsView.ID_STR_TOUCH_TO_GO_BACK));
                }
            }

            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                return super.performAccessibilityAction(host, action, args);
            }
        };
        this.mActionClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (HwGlobalActionsView.this.mHandler != null) {
                    HwGlobalActionsView.this.mHandler.removeMessages(6);
                }
                if (HwGlobalActionsView.this.mCallback != null && !ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).getIsAnimRunning() && SystemClock.elapsedRealtime() - HwGlobalActionsView.this.mLastClickTime >= 200) {
                    long unused = HwGlobalActionsView.this.mLastClickTime = SystemClock.elapsedRealtime();
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
                        boolean unused2 = HwGlobalActionsView.this.mSilentModeButtonTouched = true;
                        HwGlobalActionsView.this.mCallback.onSilentModeActionPressed();
                    } else if (tag.equals(HwGlobalActionsView.REBOOT_TAG)) {
                        if (ActivityManager.isUserAMonkey()) {
                            Log.w(HwGlobalActionsView.TAG, "ignoring monkey's attempt to reboot");
                            return;
                        }
                        HwGlobalActionsView.this.mCallback.onRebootActionPressed();
                    } else if (tag.equals(HwGlobalActionsView.SHUTDOWN_TAG)) {
                        if (ActivityManager.isUserAMonkey()) {
                            Log.w(HwGlobalActionsView.TAG, "ignoring monkey's attempt to shutdown");
                            return;
                        }
                        HwGlobalActionsView.this.mCallback.onShutdownActionPressed(HwGlobalActionsView.this.mIsDeskClockClose, HwGlobalActionsView.this.mIsBootOnTimeClose, HwGlobalActionsView.this.mListviewState);
                    } else if (tag.equals(HwGlobalActionsView.LOCKDOWN_TAG)) {
                        HwGlobalActionsView.this.mCallback.onLockdownActionPressed();
                    }
                }
            }
        };
        if (this.mContext != null) {
            this.mTouchSlop = (float) this.mContext.getResources().getDimensionPixelSize(17104904);
            this.mTone = RingtoneManager.getRingtone(this.mContext, Settings.System.DEFAULT_NOTIFICATION_URI);
            if (this.mTone != null) {
                this.mTone.setStreamType(3);
            }
        }
    }

    private void initHandler(Looper looper) {
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
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
                    default:
                        return;
                }
            }
        };
    }

    public void onBlurWallpaperChanged() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    private void setBlurWallpaperBackground() {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        loc[0] = loc[0] % getContext().getResources().getDisplayMetrics().widthPixels;
        Rect rect = new Rect(loc[0], loc[1], loc[0] + getWidth(), loc[1] + getHeight());
        if (rect.width() > 0 && rect.height() > 0) {
            this.mDrawable = new BitmapDrawable(this.mWallpaperManager.getBlurBitmap(rect));
            if (this.mDrawable != null) {
                setBackgroundDrawable(this.mDrawable);
            }
        }
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && this.mHandler != null) {
            this.mHandler.sendEmptyMessage(1);
        }
    }

    public void registerActionPressedCallback(ActionPressedCallback callback) {
        this.mCallback = callback;
    }

    public void unregisterActionPressedCallback() {
        this.mCallback = null;
    }

    /* access modifiers changed from: private */
    public void initConfirmAction(int flag) {
        boolean hwNewGlobalAction = isHwGlobalTwoActionsSupport();
        if (hwNewGlobalAction) {
            this.mInitedConfirm = true;
        }
        TextView textView = (TextView) this.view_confirm_action.findViewById(34603129);
        ImageView imageView = (ImageView) this.view_confirm_action.findViewById(34603172);
        View icon_frame = this.view_confirm_action.findViewById(34603108);
        this.shutdown_listview = (ListView) findViewById(34603154);
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
        this.shutdown_listview.setAdapter(this.mShutdownListviewAdapter);
        if (flag == 512) {
            textView.setText(ID_STR_REBOOT_CONFIRM);
            imageView.setImageResource(hwNewGlobalAction ? 33751230 : ID_IC_REBOOT_CONFIRM);
            if (hwNewGlobalAction) {
                imageView.setImageAlpha(FIX_BUG_ALPHA);
            }
            imageView.setContentDescription(getContext().getResources().getString(ID_STR_REBOOT_CONFIRM));
            icon_frame.setTag(REBOOT_TAG);
            this.shutdown_listview.setVisibility(8);
            if (hwNewGlobalAction) {
                ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.mRebootView);
            } else {
                ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.mRebootView, true);
            }
        } else if (flag == 8192) {
            textView.setText(ID_STR_SHUTDOWN_CONFIRM);
            imageView.setImageResource(hwNewGlobalAction ? 33751229 : ID_IC_SHUTDOWN_CONFIRM);
            if (hwNewGlobalAction) {
                imageView.setImageAlpha(FIX_BUG_ALPHA);
            }
            imageView.setContentDescription(getContext().getResources().getString(ID_STR_SHUTDOWN_CONFIRM));
            icon_frame.setTag(SHUTDOWN_TAG);
            this.shutdown_listview.setVisibility(0);
            this.shutdown_listview.setTag("disable-multi-select-move");
            this.shutdown_listview.setOverScrollMode(2);
            if (ret == 0 || ret == 1 || ActivityManager.getCurrentUser() != 0) {
                this.shutdown_listview.setVisibility(8);
            }
            if (hwNewGlobalAction) {
                ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.mShutdownView);
            } else {
                ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.mShutdownView, false);
            }
        } else if (flag == 131072) {
            textView.setText(ID_STR_LOCKDOWN_CONFIRM);
            imageView.setImageResource(33751785);
            imageView.setContentDescription(getContext().getResources().getString(ID_STR_LOCKDOWN_CONFIRM));
            icon_frame.setTag(LOCKDOWN_TAG);
            this.shutdown_listview.setVisibility(8);
            if (hwNewGlobalAction) {
                ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.mLockdownView);
            } else {
                ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.mLockdownView, false);
            }
        }
        icon_frame.setOnClickListener(this.mActionClickListener);
    }

    private int initAdapterDate() {
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService("alarm");
        if (alarmManager != null) {
            mShowDeskClockTime = alarmManager.checkHasHwRTCAlarm(DESKCLOCK_PACKAGENAME);
            mShowBootOnTime = alarmManager.checkHasHwRTCAlarm(SETTINGS_PACKAGENAME);
        }
        if (mShowDeskClockTime == -1 && mShowBootOnTime == -1) {
            this.mIsBootOnTimeClose = false;
            this.mIsDeskClockClose = false;
            this.mListviewState = 1;
        } else if (mShowDeskClockTime > 0 && mShowBootOnTime > 0) {
            this.mIsBootOnTimeClose = true;
            this.mIsDeskClockClose = true;
            if (mShowDeskClockTime <= mShowBootOnTime) {
                this.mListviewState = 2;
                this.mShutdownListViewText.add(formateTime(mShowDeskClockTime, TYPEDESKCLOCK));
            } else {
                this.mListviewState = 3;
                this.mShutdownListViewText.add(formateTime(mShowBootOnTime, TYPESETTINGS));
            }
        } else if (mShowDeskClockTime > 0 && mShowBootOnTime == -1) {
            this.mListviewState = 4;
            this.mShutdownListViewText.add(formateTime(mShowDeskClockTime, TYPEDESKCLOCK));
            this.mIsDeskClockClose = true;
            this.mIsBootOnTimeClose = false;
        } else if (mShowBootOnTime > 0 && mShowDeskClockTime == -1) {
            this.mListviewState = 5;
            this.mShutdownListViewText.add(formateTime(mShowBootOnTime, TYPESETTINGS));
            this.mIsBootOnTimeClose = true;
            this.mIsDeskClockClose = false;
        }
        Log.d(TAG, "initAdapterDate---,mListviewState = " + this.mListviewState + ",mIsBootOnTimeClose =" + this.mIsBootOnTimeClose + ",mIsDeskClockClose = " + this.mIsDeskClockClose);
        return this.mListviewState;
    }

    /* access modifiers changed from: private */
    public String formateTime(long alarmTime, int type) {
        long delta = alarmTime - System.currentTimeMillis();
        int hours = (int) (delta / WifiProCommonUtils.RECHECK_DELAYED_MS);
        int minutes = (int) ((delta / AppHibernateCst.DELAY_ONE_MINS) % 60);
        int days = hours / 24;
        int hours2 = hours % 24;
        String daySeq = getContext().getResources().getQuantityString(34406402, days, new Object[]{Integer.valueOf(days)});
        String hourSeq = getContext().getResources().getQuantityString(34406403, hours2, new Object[]{Integer.valueOf(hours2)});
        String minutesSeq = getContext().getResources().getQuantityString(34406404, minutes, new Object[]{Integer.valueOf(minutes)});
        int index = (days > 0 ? 4 : 0) | (hours2 > 0 ? 2 : 0) | (minutes > 0 ? 1 : 0);
        if (type != TYPEDESKCLOCK) {
            if (index == 0) {
                return getContext().getResources().getString(33685794);
            }
            if (index == 7) {
                return getContext().getResources().getString(33685787, new Object[]{daySeq, hourSeq, minutesSeq});
            } else if (index == 6) {
                return getContext().getResources().getString(33685788, new Object[]{daySeq, hourSeq});
            } else if (index == 5) {
                return getContext().getResources().getString(33685789, new Object[]{daySeq, minutesSeq});
            } else if (index == 4) {
                return getContext().getResources().getString(33685790, new Object[]{daySeq});
            } else if (index == 3) {
                return getContext().getResources().getString(33685791, new Object[]{hourSeq, minutesSeq});
            } else if (index == 2) {
                return getContext().getResources().getString(33685792, new Object[]{hourSeq});
            } else {
                return getContext().getResources().getString(33685793, new Object[]{minutesSeq});
            }
        } else if (index == 0) {
            long j = delta;
            return getContext().getResources().getString(33685545);
        } else {
            if (index == 7) {
                return getContext().getResources().getString(33685769, new Object[]{daySeq, hourSeq, minutesSeq});
            } else if (index == 6) {
                return getContext().getResources().getString(33685770, new Object[]{daySeq, hourSeq});
            } else if (index == 5) {
                return getContext().getResources().getString(33685771, new Object[]{daySeq, minutesSeq});
            } else if (index == 4) {
                return getContext().getResources().getString(33685772, new Object[]{daySeq});
            } else if (index == 3) {
                return getContext().getResources().getString(33685773, new Object[]{hourSeq, minutesSeq});
            } else if (index == 2) {
                return getContext().getResources().getString(33685774, new Object[]{hourSeq});
            } else {
                return getContext().getResources().getString(33685775, new Object[]{minutesSeq});
            }
        }
    }

    public void initUI(Looper looper) {
        if (isHwGlobalTwoActionsSupport()) {
            initNewUI(looper);
            return;
        }
        this.mWallpaperManager = (WallpaperManager) getContext().getSystemService("wallpaper");
        if (this.mWallpaperManager instanceof HwWallpaperManager) {
            this.mWallpaperManager.setCallback(this);
        }
        initHandler(looper);
        this.view_two_action = findViewById(34603174);
        if (this.view_two_action == null) {
            Log.e(TAG, "view_two_action is null");
        } else {
            ShutdownMenuAnimations.getInstance(getContext()).setTwoActionView(this.view_two_action);
        }
        this.view_four_action = findViewById(34603175);
        if (this.view_four_action == null) {
            Log.e(TAG, "view_four_action is null");
        } else {
            this.view_four_action.setVisibility(4);
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
        setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mWindowTouchSlop = ViewConfiguration.get(this.mContext).getScaledWindowTouchSlop();
    }

    private void tryToAddLockdownAction() {
        if (Settings.Secure.getInt(getContext().getContentResolver(), "lockdown_in_power_menu", 0) != 0 && shouldDisplayLockdown()) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (inflater == null) {
                Log.w(TAG, "inflater is null!");
                return;
            }
            this.mLockdownView = inflater.inflate(34013226, null);
            if (this.mLockdownView == null) {
                Log.w(TAG, "mLockdownView is null!");
                return;
            }
            RelativeLayout shutdownMenuLayout = (RelativeLayout) this.view_two_action.findViewById(34603206);
            if (shutdownMenuLayout == null) {
                Log.w(TAG, "shutdownMenuLayout is null!");
                return;
            }
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getContext().getResources().getDimensionPixelSize(34472254), -2);
            lp.setMarginStart(getContext().getResources().getDimensionPixelSize(34472253));
            lp.addRule(6, 34603152);
            lp.addRule(17, 34603152);
            this.mLockdownView.setLayoutParams(lp);
            this.mLockdownView.setId(34603205);
            shutdownMenuLayout.addView(this.mLockdownView);
        }
    }

    public void initNewUI(Looper looper) {
        this.mWallpaperManager = (WallpaperManager) getContext().getSystemService("wallpaper");
        if (this.mWallpaperManager instanceof HwWallpaperManager) {
            this.mWallpaperManager.setCallback(this);
        }
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
        showNewUI();
        HwGlobalActionsData.getSingletoneInstance().registerActionStateObserver(this.mObserver);
        setTag(OTHERAREA_TAG);
        setOnClickListener(this.mActionClickListener);
        synchronized (this) {
            this.mToneGenerators = new ToneGenerator[AudioSystem.getNumStreamTypes()];
            this.mVibrator = (Vibrator) getContext().getSystemService("vibrator");
            this.mSilentModeButtonTouched = false;
        }
        setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mWindowTouchSlop = ViewConfiguration.get(this.mContext).getScaledWindowTouchSlop();
    }

    public void deinitUI() {
        HwGlobalActionsData.getSingletoneInstance().unregisterActionStateObserver();
        if (this.mHandler != null) {
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(4);
            onStopSounds();
            this.mHandler.removeMessages(5);
            this.mHandler.sendEmptyMessage(5);
        }
        this.isFirstShow = true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0043, code lost:
        r2.setContentDescription(getContext().getResources().getString(ID_STR_REBOOT));
     */
    private void updateActionUI(View v, int actionID, int idText, int idImage) {
        if (v != null) {
            LinearLayout actionLayout = (LinearLayout) v.findViewById(actionID);
            if (actionLayout != null) {
                TextView actionTextView = (TextView) actionLayout.findViewById(34603129);
                if (actionTextView != null) {
                    actionTextView.setText(idText);
                    ImageView actionImageView = (ImageView) actionLayout.findViewById(34603172);
                    if (actionImageView != null) {
                        if (this.mHandler != null) {
                            this.mHandler.removeMessages(6);
                        }
                        if (idImage != ID_IC_SHUTDOWN) {
                            if (idImage != 33751785) {
                                switch (idImage) {
                                    case ID_IC_AIRPLANEMODE /*33751059*/:
                                        actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_AIRPLANEMODE_OFF));
                                        break;
                                    case ID_IC_AIRPLANEMODE_ON /*33751060*/:
                                        actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_AIRPLANEMODE_ON));
                                        break;
                                    case ID_IC_SILENTMODE_SILENT /*33751061*/:
                                        actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_SILENTMODE));
                                        break;
                                    case ID_IC_SILENTMODE_VIBRATE /*33751062*/:
                                        actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_VIBRATIONMODE));
                                        break;
                                    case ID_IC_SILENTMODE_NORMAL /*33751063*/:
                                        actionImageView.setContentDescription(getContext().getResources().getString(ID_STR_SOUNDMODE));
                                        break;
                                    case ID_IC_REBOOT /*33751064*/:
                                        break;
                                    default:
                                        switch (idImage) {
                                            case 33751229:
                                                break;
                                            case 33751230:
                                                break;
                                        }
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
                            } else {
                                final ImageView mImage = actionImageView;
                                final int mId = idImage;
                                Animation animRotation = ShutdownMenuAnimations.getInstance(getContext()).setSoundModeRotate();
                                animRotation.setAnimationListener(new Animation.AnimationListener() {
                                    public void onAnimationStart(Animation arg0) {
                                        ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).setIsAnimRunning(true);
                                        if (mImage != null) {
                                            mImage.setImageResource(mId);
                                        }
                                    }

                                    public void onAnimationRepeat(Animation arg0) {
                                    }

                                    public void onAnimationEnd(Animation arg0) {
                                        ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).setIsAnimRunning(false);
                                        if (HwGlobalActionsView.this.mHandler != null) {
                                            HwGlobalActionsView.this.mHandler.removeMessages(6);
                                            HwGlobalActionsView.this.mHandler.sendEmptyMessageDelayed(6, 1000);
                                        }
                                    }
                                });
                                mImage.startAnimation(animRotation);
                            }
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
        }
    }

    /* access modifiers changed from: private */
    public void updateAirplaneModeUI(View v) {
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            updateActionUI(this.view_four_action, 34603145, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE_OFF);
        } else if ((HwGlobalActionsData.getSingletoneInstance().getState() & 1) != 0) {
            updateActionUI(this.view_four_action, 34603145, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE_ON);
        } else {
            updateActionUI(this.view_four_action, 34603145, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE);
        }
    }

    /* access modifiers changed from: private */
    public void updateSilentModeUI(View v) {
        if (this.mHandler != null && this.mSilentModeButtonTouched) {
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(4);
        }
        if ((HwGlobalActionsData.getSingletoneInstance().getState() & 16) != 0) {
            updateActionUI(this.view_four_action, 34603146, ID_STR_SILENTMODE, ID_IC_SILENTMODE_SILENT);
        } else if ((HwGlobalActionsData.getSingletoneInstance().getState() & 32) != 0) {
            updateActionUI(this.view_four_action, 34603146, ID_STR_VIBRATIONMODE, ID_IC_SILENTMODE_VIBRATE);
            if (this.mHandler != null && this.mSilentModeButtonTouched) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), 300);
            }
        } else {
            updateActionUI(this.view_four_action, 34603146, ID_STR_SOUNDMODE, ID_IC_SILENTMODE_NORMAL);
            if (this.mHandler != null && this.mSilentModeButtonTouched) {
                this.mHandler.removeMessages(2);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, 2, 0), 300);
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
    }

    private void showRebootAndShutdownUI(View v) {
        updateActionUI(v, 34603151, ID_STR_REBOOT, ID_IC_REBOOT);
        setActionClickListener(v, 34603151, REBOOT_TAG);
        updateActionUI(v, 34603152, ID_STR_SHUTDOWN, ID_IC_SHUTDOWN);
        setActionClickListener(v, 34603152, SHUTDOWN_TAG);
    }

    private void setActionClickListener(View v, int actionID, Object actionTag) {
        if (v != null) {
            LinearLayout action = (LinearLayout) v.findViewById(actionID);
            if (action != null) {
                View icon_frame = action.findViewById(34603108);
                if (icon_frame != null) {
                    icon_frame.setTag(actionTag);
                    icon_frame.setOnClickListener(this.mActionClickListener);
                }
            }
        }
    }

    private UserInfo getCurrentUser() {
        try {
            return ActivityManager.getService().getCurrentUser();
        } catch (RemoteException re) {
            Log.e(TAG, "error happen while getCurrentUser(): ");
            re.printStackTrace();
            return null;
        }
    }

    private boolean shouldDisplayLockdown() {
        UserInfo userInfo = getCurrentUser();
        boolean z = false;
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
                z = true;
            }
            return z;
        }
    }

    /* access modifiers changed from: protected */
    public void showNewUI() {
        if (!this.mInitedConfirm) {
            this.view_two_action.setVisibility(0);
            this.view_confirm_action.setVisibility(4);
            this.isFirstShow = true;
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
            this.isFirstShow = false;
        }
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
        if (this.mHandler != null && this.mHandler.hasMessages(3)) {
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
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(5), MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
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

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (handleKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean handleKeyEvent(KeyEvent event) {
        View view;
        View view2;
        if (event.getAction() == 1) {
            int keyCode = event.getKeyCode();
            if (keyCode != 4) {
                if (keyCode != 66 && keyCode != 160) {
                    switch (keyCode) {
                        case 21:
                            if (isHwGlobalTwoActionsSupport() && this.view_two_action != null && this.view_two_action.getVisibility() == 0) {
                                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 0) {
                                    view = this.mRebootView;
                                } else {
                                    view = this.mShutdownView;
                                }
                                if (view != null) {
                                    view.findViewById(34603108).performClick();
                                    return true;
                                }
                            }
                            break;
                        case 22:
                            if (isHwGlobalTwoActionsSupport() && this.view_two_action != null && this.view_two_action.getVisibility() == 0) {
                                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 0) {
                                    view2 = this.mShutdownView;
                                } else {
                                    view2 = this.mRebootView;
                                }
                                if (view2 != null) {
                                    view2.findViewById(34603108).performClick();
                                    return true;
                                }
                            }
                            break;
                    }
                } else {
                    if (isHwGlobalTwoActionsSupport() && this.view_confirm_action != null && this.view_confirm_action.getVisibility() == 0) {
                        View icon_frame = this.view_confirm_action.findViewById(34603108);
                        if (icon_frame != null) {
                            icon_frame.performClick();
                        }
                    }
                    return true;
                }
            } else if (this.mCallback != null) {
                this.mCallback.onOtherAreaPressed();
                return true;
            }
        }
        return false;
    }

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
        if (this.view_confirm_action != null) {
            TextView textView = (TextView) this.view_confirm_action.findViewById(34603129);
            if (textView != null) {
                textView.setText(ID_STR_REBOOTING);
            }
        }
    }

    public void showShutdongingHint() {
        if (this.view_confirm_action != null) {
            TextView textView = (TextView) this.view_confirm_action.findViewById(34603129);
            if (textView != null) {
                textView.setText(ID_STR_SHUTDOWNING);
            }
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

    private boolean onDisableTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        switch (event.getActionMasked()) {
            case 1:
            case 3:
            case 6:
                cancelDisableAccssibility();
                break;
            case 2:
                if (Math.abs(MathUtils.dist(event.getX(0), event.getY(0), this.mFirstPointerDownX, this.mFirstPointerDownY)) <= this.mTouchSlop) {
                    if (this.mHandler != null && this.mEventAfterActionDown) {
                        this.mHandler.sendEmptyMessageDelayed(7, 3000);
                        break;
                    }
                } else {
                    cancelDisableAccssibility();
                    break;
                }
            case 5:
                if (pointerCount > 2) {
                    cancelDisableAccssibility();
                    break;
                }
                break;
        }
        this.mEventAfterActionDown = false;
        if (!this.mCanceled) {
            return true;
        }
        this.mCanceled = false;
        return false;
    }

    private void configureTalkbackView(Object mView) {
        this.mAccessibilityTip = (TextView) ((HwGlobalActionsView) mView).findViewById(34603179);
        if (this.mAccessibilityTip == null) {
            return;
        }
        if (canDisableAccessibilityViaGesture()) {
            this.mAccessibilityTip.setVisibility(0);
        } else {
            this.mAccessibilityTip.setVisibility(8);
        }
    }

    private void cancelDisableAccssibility() {
        this.mCanceled = true;
        this.mDisableIntercepted = false;
        if (this.mHandler != null) {
            this.mHandler.removeMessages(7);
        }
    }

    /* access modifiers changed from: private */
    public void disableTalkBackService() {
        if (this.mContext != null) {
            boolean accessibilityEnabled = false;
            loadInstalledServices();
            Set<ComponentName> enabledServices = getEnabledServicesFromSettings();
            enabledServices.remove(ComponentName.unflattenFromString(TALKBACK_COMPONENT_NAME));
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
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_enabled", accessibilityEnabled ? 1 : 0, -2);
            if (this.mAccessibilityTip != null) {
                this.mAccessibilityTip.setVisibility(8);
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

    private boolean isTalkBackServicesOn() {
        boolean z = false;
        if (this.mContext == null) {
            return false;
        }
        boolean accessibilityEnabled = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_enabled", 0, -2) == 1;
        String enabledSerices = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", -2);
        boolean isContainsTalkBackService = enabledSerices != null && enabledSerices.contains(TALKBACK_COMPONENT_NAME);
        if (accessibilityEnabled && isContainsTalkBackService) {
            z = true;
        }
        return z;
    }

    private void replacePoweroffHint(TextView poweroffHintView) {
        if (poweroffHintView != null) {
            Resources tmpResources = this.mContext.getResources();
            int poweroffHintTime = tmpResources.getInteger(34275331);
            poweroffHintView.setText(String.format(tmpResources.getString(33685845), new Object[]{Integer.valueOf(poweroffHintTime)}));
        }
    }

    public boolean isHwGlobalTwoActionsSupport() {
        return this.mContext.getResources().getBoolean(34537474);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mTone != null) {
            this.mTone.stop();
        }
    }
}
