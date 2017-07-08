package huawei.com.android.server.policy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.AnimatorSet;
import android.app.AbsWallpaperManagerInner.IBlurWallpaperCallback;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ResolveInfo;
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
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.server.policy.EnableAccessibilityController;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.app.HwWallpaperManager;
import huawei.com.android.server.policy.HwGlobalActionsData.ActionStateObserver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwGlobalActionsView extends RelativeLayout implements IBlurWallpaperCallback {
    private static final String AIRPLANEMODE_TAG = "airplane_mode";
    private static final int BEEP_DURATION = 150;
    public static final boolean BLUR_SCREENSHOT = true;
    public static final boolean DEBUG = false;
    private static final String DESKCLOCK_PACKAGENAME = "com.android.deskclock";
    private static final int DISABLE_ACCESSIBILITY_DELAY_MILLIS = 3000;
    private static final int DISMISSS_DELAY_0 = 0;
    private static final int DISMISSS_DELAY_1000 = 1000;
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final int FREE_DELAY = 10000;
    private static final int ID_IC_AIRPLANEMODE = 33751059;
    private static final int ID_IC_AIRPLANEMODE_OFF = 33751317;
    private static final int ID_IC_AIRPLANEMODE_ON = 33751060;
    private static final int ID_IC_NEW_REBOOT = 33751331;
    private static final int ID_IC_NEW_REBOOT_CONFIRM = 33751331;
    private static final int ID_IC_NEW_SHUTDOWN = 33751330;
    private static final int ID_IC_NEW_SHUTDOWN_CONFIRM = 33751330;
    private static final int ID_IC_REBOOT = 33751064;
    private static final int ID_IC_REBOOT_CONFIRM = 33751065;
    private static final int ID_IC_SHUTDOWN = 33751072;
    private static final int ID_IC_SHUTDOWN_CONFIRM = 33751073;
    private static final int ID_IC_SILENTMODE_NORMAL = 33751063;
    private static final int ID_IC_SILENTMODE_SILENT = 33751061;
    private static final int ID_IC_SILENTMODE_VIBRATE = 33751062;
    private static final int ID_STR_AIRPLANEMODE = 33685727;
    private static final int ID_STR_AIRPLANEMODE_OFF = 33685737;
    private static final int ID_STR_AIRPLANEMODE_ON = 33685736;
    private static final int ID_STR_REBOOT = 33685733;
    private static final int ID_STR_REBOOTING = 33685777;
    private static final int ID_STR_REBOOT_CONFIRM = 33685734;
    private static final int ID_STR_SHUTDOWN = 33685731;
    private static final int ID_STR_SHUTDOWNING = 33685776;
    private static final int ID_STR_SHUTDOWN_CONFIRM = 33685732;
    private static final int ID_STR_SILENTMODE = 33685728;
    private static final int ID_STR_SOUNDMODE = 33685729;
    private static final int ID_STR_TOUCH_TO_GO_BACK = 33685735;
    private static final int ID_STR_VIBRATIONMODE = 33685730;
    private static final boolean IS_POWEROFF_ALARM_ENABLED = false;
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
    private static final String REBOOT_TAG = "reboot";
    private static final String SETTINGS_PACKAGENAME = "com.android.providers.settings";
    private static final String SHUTDOWN_TAG = "shutdown";
    private static final String SILENTMODE_TAG = "silent_mode";
    private static final String TAG = "HwGlobalActions";
    private static final String TALKBACK_COMPONENT_NAME = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final int TWO_BOOT_DLISTVIEW = 3;
    private static final int TWO_DESKCLOCK_DLISTVIEW = 2;
    private static int TYPEDESKCLOCK = 0;
    private static int TYPESETTINGS = 0;
    public static final int VIBRATE_DELAY = 300;
    private static final int VIBRATE_DURATION = 300;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = null;
    private static long mShowBootOnTime;
    private static long mShowDeskClockTime;
    private static Boolean[] mState;
    private static final Set<ComponentName> sInstalledServices = null;
    private static final SimpleStringSplitter sStringColonSplitter = null;
    private View airplanemode_view;
    private boolean isFirstShow;
    private AccessibilityDelegate mAccessibilityDelegate;
    private TextView mAccessibilityTip;
    private OnClickListener mActionClickListener;
    private ActionPressedCallback mCallback;
    private boolean mCancelOnUp;
    private boolean mCanceled;
    private boolean mDisableIntercepted;
    private Drawable mDrawable;
    private EnableAccessibilityController mEnableAccessibilityController;
    private AnimatorSet mEnterSet;
    private float mFirstPointerDownX;
    private float mFirstPointerDownY;
    private Handler mHandler;
    private boolean mInitedConfirm;
    private boolean mIntercepted;
    private boolean mIsBootOnTimeClose;
    private boolean mIsDeskClockClose;
    private long mLastClickTime;
    private int mListviewState;
    private MyActionStateObserver mObserver;
    private ArrayList<String> mShutdownListViewText;
    private MyAdapter mShutdownListviewAdapter;
    private boolean mSilentModeButtonTouched;
    private Ringtone mTone;
    private ToneGenerator[] mToneGenerators;
    private float mTouchSlop;
    private Vibrator mVibrator;
    ArrayList<View> mViewList;
    private WallpaperManager mWallpaperManager;
    private int mWindowTouchSlop;
    private View reboot_view;
    private ListView shutdown_listview;
    private View shutdown_view;
    private View silentmode_view;
    private View view_confirm_action;
    private View view_four_action;
    private View view_two_action;

    public interface ActionPressedCallback {
        void dismissShutdownMenu(int i);

        void onAirplaneModeActionPressed();

        void onOtherAreaPressed();

        void onRebootActionPressed();

        void onShutdownActionPressed(boolean z, boolean z2, int i);

        void onSilentModeActionPressed();
    }

    /* renamed from: huawei.com.android.server.policy.HwGlobalActionsView.3 */
    class AnonymousClass3 extends Handler {
        AnonymousClass3(Looper $anonymous0) {
            super($anonymous0);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwGlobalActionsView.TWO_DESKCLOCK_DLISTVIEW /*2*/:
                    HwGlobalActionsView.this.onPlaySound(msg.arg1, msg.arg2);
                case HwGlobalActionsView.TWO_BOOT_DLISTVIEW /*3*/:
                    HwGlobalActionsView.this.onStopSounds();
                case HwGlobalActionsView.ONE_DESKCLOCK_LISTVIEW /*4*/:
                    HwGlobalActionsView.this.onVibrate();
                case HwGlobalActionsView.ONE_BOOT_LISTVIEW /*5*/:
                    HwGlobalActionsView.this.onFreeResources();
                case HwGlobalActionsView.ONE_TWO_DESKCLOCK_LISTVIEW /*6*/:
                    if (HwGlobalActionsView.this.mCallback != null && HwGlobalActionsView.this.mSilentModeButtonTouched) {
                        HwGlobalActionsView.this.mCallback.dismissShutdownMenu(HwGlobalActionsView.DISMISSS_DELAY_0);
                    }
                case HwGlobalActionsView.ONE_TWO_BOOT_LISTVIEW /*7*/:
                    if (HwGlobalActionsView.this.mTone != null) {
                        HwGlobalActionsView.this.mTone.play();
                    }
                    HwGlobalActionsView.this.disableTalkBackService();
                default:
            }
        }
    }

    /* renamed from: huawei.com.android.server.policy.HwGlobalActionsView.4 */
    class AnonymousClass4 implements AnimationListener {
        final /* synthetic */ int val$mId;
        final /* synthetic */ ImageView val$mImage;

        AnonymousClass4(ImageView val$mImage, int val$mId) {
            this.val$mImage = val$mImage;
            this.val$mId = val$mId;
        }

        public void onAnimationStart(Animation arg0) {
            ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).setIsAnimRunning(HwGlobalActionsView.BLUR_SCREENSHOT);
            if (this.val$mImage != null) {
                this.val$mImage.setImageResource(this.val$mId);
            }
        }

        public void onAnimationRepeat(Animation arg0) {
        }

        public void onAnimationEnd(Animation arg0) {
            ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).setIsAnimRunning(HwGlobalActionsView.IS_POWEROFF_ALARM_ENABLED);
            if (HwGlobalActionsView.this.mHandler != null) {
                HwGlobalActionsView.this.mHandler.removeMessages(HwGlobalActionsView.ONE_TWO_DESKCLOCK_LISTVIEW);
                HwGlobalActionsView.this.mHandler.sendEmptyMessageDelayed(HwGlobalActionsView.ONE_TWO_DESKCLOCK_LISTVIEW, 1000);
            }
        }
    }

    private final class MyActionStateObserver implements ActionStateObserver {
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
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & HwGlobalActionsData.FLAG_REBOOT_CONFIRM) != 0) {
                HwGlobalActionsView.this.initConfirmAction(HwGlobalActionsView.BLUR_SCREENSHOT);
            } else if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.showNormalUI();
            } else {
                HwGlobalActionsView.this.showBeforeProvisioningUI();
            }
        }

        public void onShutdownActionStateChanged() {
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) != 0) {
                HwGlobalActionsView.this.initConfirmAction(HwGlobalActionsView.IS_POWEROFF_ALARM_ENABLED);
            } else if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.showNormalUI();
            } else {
                HwGlobalActionsView.this.showBeforeProvisioningUI();
            }
        }
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList<String> list;

        public MyAdapter(ArrayList<String> list, Context context) {
            this.inflater = null;
            this.list = list;
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

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                if (HwGlobalActionsView.this.isHwGlobalTwoActionsSupport()) {
                    convertView = this.inflater.inflate(34013227, null);
                } else {
                    convertView = this.inflater.inflate(34013223, null);
                }
                holder.checkBox = (CheckBox) convertView.findViewById(34603124);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkBox.setText((CharSequence) this.list.get(position));
            holder.checkBox.setOnCheckedChangeListener(new SearchItemOnCheckedChangeListener(position, HwGlobalActionsView.mState));
            return convertView;
        }
    }

    public class SearchItemOnCheckedChangeListener implements OnCheckedChangeListener {
        private int id;
        private Boolean[] state;

        public SearchItemOnCheckedChangeListener(int id, Boolean[] state) {
            this.id = id;
            this.state = state;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.d(HwGlobalActionsView.TAG, "-----SearchItemOnCheckedChangeListener---" + isChecked + ", id = " + this.id);
            switch (HwGlobalActionsView.this.mListviewState) {
                case HwGlobalActionsView.NONE_LISTVIEW /*1*/:
                    HwGlobalActionsView.this.mIsDeskClockClose = HwGlobalActionsView.IS_POWEROFF_ALARM_ENABLED;
                    HwGlobalActionsView.this.mIsBootOnTimeClose = HwGlobalActionsView.IS_POWEROFF_ALARM_ENABLED;
                    break;
                case HwGlobalActionsView.TWO_DESKCLOCK_DLISTVIEW /*2*/:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsDeskClockClose = this.state[HwGlobalActionsView.DISMISSS_DELAY_0].booleanValue();
                    if (!isChecked && this.id == 0) {
                        HwGlobalActionsView.this.mListviewState = HwGlobalActionsView.ONE_TWO_DESKCLOCK_LISTVIEW;
                        HwGlobalActionsView.this.mIsBootOnTimeClose = HwGlobalActionsView.BLUR_SCREENSHOT;
                        HwGlobalActionsView.this.mShutdownListViewText.clear();
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowDeskClockTime, HwGlobalActionsView.TYPEDESKCLOCK));
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowBootOnTime, HwGlobalActionsView.TYPESETTINGS));
                        break;
                    }
                case HwGlobalActionsView.TWO_BOOT_DLISTVIEW /*3*/:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[HwGlobalActionsView.DISMISSS_DELAY_0].booleanValue();
                    if (!isChecked && this.id == 0) {
                        HwGlobalActionsView.this.mListviewState = HwGlobalActionsView.ONE_TWO_BOOT_LISTVIEW;
                        HwGlobalActionsView.this.mIsDeskClockClose = HwGlobalActionsView.BLUR_SCREENSHOT;
                        HwGlobalActionsView.this.mShutdownListViewText.clear();
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowBootOnTime, HwGlobalActionsView.TYPESETTINGS));
                        HwGlobalActionsView.this.mShutdownListViewText.add(HwGlobalActionsView.this.formateTime(HwGlobalActionsView.mShowDeskClockTime, HwGlobalActionsView.TYPEDESKCLOCK));
                        break;
                    }
                case HwGlobalActionsView.ONE_DESKCLOCK_LISTVIEW /*4*/:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsDeskClockClose = this.state[HwGlobalActionsView.DISMISSS_DELAY_0].booleanValue();
                    HwGlobalActionsView.this.mIsBootOnTimeClose = HwGlobalActionsView.IS_POWEROFF_ALARM_ENABLED;
                    break;
                case HwGlobalActionsView.ONE_BOOT_LISTVIEW /*5*/:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[HwGlobalActionsView.DISMISSS_DELAY_0].booleanValue();
                    HwGlobalActionsView.this.mIsDeskClockClose = HwGlobalActionsView.IS_POWEROFF_ALARM_ENABLED;
                    break;
                case HwGlobalActionsView.ONE_TWO_DESKCLOCK_LISTVIEW /*6*/:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsDeskClockClose = this.state[HwGlobalActionsView.DISMISSS_DELAY_0].booleanValue();
                    HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[HwGlobalActionsView.NONE_LISTVIEW].booleanValue();
                    break;
                case HwGlobalActionsView.ONE_TWO_BOOT_LISTVIEW /*7*/:
                    HwGlobalActionsView.mState[this.id] = Boolean.valueOf(isChecked);
                    HwGlobalActionsView.this.mIsBootOnTimeClose = this.state[HwGlobalActionsView.DISMISSS_DELAY_0].booleanValue();
                    HwGlobalActionsView.this.mIsDeskClockClose = this.state[HwGlobalActionsView.NONE_LISTVIEW].booleanValue();
                    break;
            }
            HwGlobalActionsView.mState = this.state;
            Log.d(HwGlobalActionsView.TAG, "SearchItemOnCheckedChangeListener,,mListviewState = " + HwGlobalActionsView.this.mListviewState + ",mIsBootOnTimeClose =" + HwGlobalActionsView.this.mIsBootOnTimeClose + ",mIsDeskClockClose = " + HwGlobalActionsView.this.mIsDeskClockClose);
            HwGlobalActionsView.this.mShutdownListviewAdapter.notifyDataSetChanged();
            Context -get3 = HwGlobalActionsView.this.mContext;
            Object[] objArr = new Object[HwGlobalActionsView.TWO_DESKCLOCK_DLISTVIEW];
            objArr[HwGlobalActionsView.DISMISSS_DELAY_0] = Boolean.valueOf(HwGlobalActionsView.this.mIsDeskClockClose);
            objArr[HwGlobalActionsView.NONE_LISTVIEW] = Boolean.valueOf(HwGlobalActionsView.this.mIsBootOnTimeClose);
            StatisticalUtils.reporte(-get3, 23, String.format("{DeskClock:%s, BootOnTime:%s}", objArr));
        }
    }

    static class ViewHolder {
        public CheckBox checkBox;

        ViewHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.server.policy.HwGlobalActionsView.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.server.policy.HwGlobalActionsView.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.server.policy.HwGlobalActionsView.<clinit>():void");
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
        this.mViewList = new ArrayList();
        this.isFirstShow = BLUR_SCREENSHOT;
        this.mIsDeskClockClose = BLUR_SCREENSHOT;
        this.mIsBootOnTimeClose = BLUR_SCREENSHOT;
        this.mListviewState = DISMISSS_DELAY_0;
        this.mShutdownListViewText = new ArrayList();
        this.mLastClickTime = 0;
        this.mDisableIntercepted = IS_POWEROFF_ALARM_ENABLED;
        this.mCanceled = IS_POWEROFF_ALARM_ENABLED;
        this.mTouchSlop = 0.0f;
        this.mFirstPointerDownX = 0.0f;
        this.mFirstPointerDownY = 0.0f;
        this.mAccessibilityTip = null;
        this.mTone = null;
        this.mInitedConfirm = IS_POWEROFF_ALARM_ENABLED;
        this.mAccessibilityDelegate = new AccessibilityDelegate() {
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
        this.mActionClickListener = new OnClickListener() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onClick(View v) {
                if (HwGlobalActionsView.this.mHandler != null) {
                    HwGlobalActionsView.this.mHandler.removeMessages(HwGlobalActionsView.ONE_TWO_DESKCLOCK_LISTVIEW);
                }
                if (HwGlobalActionsView.this.mCallback != null && !ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).getIsAnimRunning() && SystemClock.elapsedRealtime() - HwGlobalActionsView.this.mLastClickTime >= 200) {
                    HwGlobalActionsView.this.mLastClickTime = SystemClock.elapsedRealtime();
                    String obj = v.getTag();
                    if (obj == HwGlobalActionsView.OTHERAREA_TAG) {
                        HwGlobalActionsView.this.mCallback.onOtherAreaPressed();
                    } else if (obj == HwGlobalActionsView.AIRPLANEMODE_TAG) {
                        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
                            Log.i(HwGlobalActionsView.TAG, " factory mode  ");
                        } else if ("normal".equals(SystemProperties.get("ro.runmode", "normal"))) {
                            Log.i(HwGlobalActionsView.TAG, " normal mode  ");
                            HwGlobalActionsView.this.mCallback.onAirplaneModeActionPressed();
                        }
                    } else if (obj == HwGlobalActionsView.SILENTMODE_TAG) {
                        HwGlobalActionsView.this.mSilentModeButtonTouched = HwGlobalActionsView.BLUR_SCREENSHOT;
                        HwGlobalActionsView.this.mCallback.onSilentModeActionPressed();
                    } else if (obj == HwGlobalActionsView.REBOOT_TAG) {
                        if (ActivityManager.isUserAMonkey()) {
                            Log.w(HwGlobalActionsView.TAG, "ignoring monkey's attempt to reboot");
                            return;
                        }
                        HwGlobalActionsView.this.mCallback.onRebootActionPressed();
                    } else if (ActivityManager.isUserAMonkey()) {
                        Log.w(HwGlobalActionsView.TAG, "ignoring monkey's attempt to shutdown");
                    } else {
                        HwGlobalActionsView.this.mCallback.onShutdownActionPressed(HwGlobalActionsView.this.mIsDeskClockClose, HwGlobalActionsView.this.mIsBootOnTimeClose, HwGlobalActionsView.this.mListviewState);
                    }
                }
            }
        };
        if (this.mContext != null) {
            this.mTouchSlop = (float) this.mContext.getResources().getDimensionPixelSize(17105030);
            this.mTone = RingtoneManager.getRingtone(this.mContext, System.DEFAULT_NOTIFICATION_URI);
            if (this.mTone != null) {
                this.mTone.setStreamType(TWO_BOOT_DLISTVIEW);
            }
        }
    }

    private void initHandler(Looper looper) {
        this.mHandler = new AnonymousClass3(looper);
    }

    public void onBlurWallpaperChanged() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessage(NONE_LISTVIEW);
        }
    }

    private void setBlurWallpaperBackground() {
        int[] loc = new int[TWO_DESKCLOCK_DLISTVIEW];
        getLocationOnScreen(loc);
        loc[DISMISSS_DELAY_0] = loc[DISMISSS_DELAY_0] % getContext().getResources().getDisplayMetrics().widthPixels;
        Rect rect = new Rect(loc[DISMISSS_DELAY_0], loc[NONE_LISTVIEW], loc[DISMISSS_DELAY_0] + getWidth(), loc[NONE_LISTVIEW] + getHeight());
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
            this.mHandler.sendEmptyMessage(NONE_LISTVIEW);
        }
    }

    public void registerActionPressedCallback(ActionPressedCallback callback) {
        this.mCallback = callback;
    }

    public void unregisterActionPressedCallback() {
        this.mCallback = null;
    }

    private void initConfirmAction(boolean isReboot) {
        boolean hwNewGlobalAction = isHwGlobalTwoActionsSupport();
        if (hwNewGlobalAction) {
            this.mInitedConfirm = BLUR_SCREENSHOT;
        }
        TextView textView = (TextView) this.view_confirm_action.findViewById(34603117);
        ImageView imageView = (ImageView) this.view_confirm_action.findViewById(34603123);
        View icon_frame = this.view_confirm_action.findViewById(34603122);
        this.shutdown_listview = (ListView) findViewById(34603208);
        if (hwNewGlobalAction) {
            setTag(OTHERAREA_TAG);
        }
        this.mListviewState = DISMISSS_DELAY_0;
        this.mIsDeskClockClose = BLUR_SCREENSHOT;
        this.mIsBootOnTimeClose = BLUR_SCREENSHOT;
        this.mShutdownListViewText.clear();
        mState[DISMISSS_DELAY_0] = Boolean.valueOf(BLUR_SCREENSHOT);
        mState[NONE_LISTVIEW] = Boolean.valueOf(BLUR_SCREENSHOT);
        int ret = initAdapterDate();
        this.mShutdownListviewAdapter = new MyAdapter(this.mShutdownListViewText, getContext());
        this.shutdown_listview.setAdapter(this.mShutdownListviewAdapter);
        if (isReboot) {
            textView.setText(ID_STR_REBOOT_CONFIRM);
            imageView.setImageResource(hwNewGlobalAction ? ID_IC_NEW_REBOOT_CONFIRM : ID_IC_REBOOT_CONFIRM);
            imageView.setContentDescription(getContext().getResources().getString(ID_STR_REBOOT_CONFIRM));
            icon_frame.setTag(REBOOT_TAG);
            this.shutdown_listview.setVisibility(8);
            if (hwNewGlobalAction) {
                ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.reboot_view, isReboot);
            } else {
                ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.reboot_view, isReboot);
            }
        } else {
            textView.setText(ID_STR_SHUTDOWN_CONFIRM);
            imageView.setImageResource(hwNewGlobalAction ? ID_IC_NEW_SHUTDOWN_CONFIRM : ID_IC_SHUTDOWN_CONFIRM);
            imageView.setContentDescription(getContext().getResources().getString(ID_STR_SHUTDOWN_CONFIRM));
            icon_frame.setTag(SHUTDOWN_TAG);
            this.shutdown_listview.setVisibility(DISMISSS_DELAY_0);
            if (!(ret == 0 || ret == NONE_LISTVIEW)) {
                if (ActivityManager.getCurrentUser() != 0) {
                }
                if (hwNewGlobalAction) {
                    ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.shutdown_view, isReboot);
                } else {
                    ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.shutdown_view, isReboot);
                }
            }
            this.shutdown_listview.setVisibility(8);
            if (hwNewGlobalAction) {
                ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.shutdown_view, isReboot);
            } else {
                ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.shutdown_view, isReboot);
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
            this.mIsBootOnTimeClose = IS_POWEROFF_ALARM_ENABLED;
            this.mIsDeskClockClose = IS_POWEROFF_ALARM_ENABLED;
            this.mListviewState = NONE_LISTVIEW;
        } else if (mShowDeskClockTime > 0 && mShowBootOnTime > 0) {
            this.mIsBootOnTimeClose = BLUR_SCREENSHOT;
            this.mIsDeskClockClose = BLUR_SCREENSHOT;
            if (mShowDeskClockTime <= mShowBootOnTime) {
                this.mListviewState = TWO_DESKCLOCK_DLISTVIEW;
                this.mShutdownListViewText.add(formateTime(mShowDeskClockTime, TYPEDESKCLOCK));
            } else {
                this.mListviewState = TWO_BOOT_DLISTVIEW;
                this.mShutdownListViewText.add(formateTime(mShowBootOnTime, TYPESETTINGS));
            }
        } else if (mShowDeskClockTime > 0 && mShowBootOnTime == -1) {
            this.mListviewState = ONE_DESKCLOCK_LISTVIEW;
            this.mShutdownListViewText.add(formateTime(mShowDeskClockTime, TYPEDESKCLOCK));
            this.mIsDeskClockClose = BLUR_SCREENSHOT;
            this.mIsBootOnTimeClose = IS_POWEROFF_ALARM_ENABLED;
        } else if (mShowBootOnTime > 0 && mShowDeskClockTime == -1) {
            this.mListviewState = ONE_BOOT_LISTVIEW;
            this.mShutdownListViewText.add(formateTime(mShowBootOnTime, TYPESETTINGS));
            this.mIsBootOnTimeClose = BLUR_SCREENSHOT;
            this.mIsDeskClockClose = IS_POWEROFF_ALARM_ENABLED;
        }
        Log.d(TAG, "initAdapterDate---,mListviewState = " + this.mListviewState + ",mIsBootOnTimeClose =" + this.mIsBootOnTimeClose + ",mIsDeskClockClose = " + this.mIsDeskClockClose);
        return this.mListviewState;
    }

    private String formateTime(long alarmTime, int type) {
        long delta = alarmTime - System.currentTimeMillis();
        int hours = (int) (delta / WifiProCommonUtils.RECHECK_DELAYED_MS);
        int minutes = (int) ((delta / AppHibernateCst.DELAY_ONE_MINS) % 60);
        int days = hours / 24;
        hours %= 24;
        Resources resources = getContext().getResources();
        Object[] objArr = new Object[NONE_LISTVIEW];
        objArr[DISMISSS_DELAY_0] = Integer.valueOf(days);
        String daySeq = resources.getQuantityString(34734083, days, objArr);
        resources = getContext().getResources();
        objArr = new Object[NONE_LISTVIEW];
        objArr[DISMISSS_DELAY_0] = Integer.valueOf(hours);
        String hourSeq = resources.getQuantityString(34734084, hours, objArr);
        resources = getContext().getResources();
        objArr = new Object[NONE_LISTVIEW];
        objArr[DISMISSS_DELAY_0] = Integer.valueOf(minutes);
        String minutesSeq = resources.getQuantityString(34734085, minutes, objArr);
        int index = ((days > 0 ? BLUR_SCREENSHOT : IS_POWEROFF_ALARM_ENABLED ? ONE_DESKCLOCK_LISTVIEW : DISMISSS_DELAY_0) | (hours > 0 ? BLUR_SCREENSHOT : IS_POWEROFF_ALARM_ENABLED ? TWO_DESKCLOCK_DLISTVIEW : DISMISSS_DELAY_0)) | (minutes > 0 ? BLUR_SCREENSHOT : IS_POWEROFF_ALARM_ENABLED ? NONE_LISTVIEW : DISMISSS_DELAY_0);
        if (type == TYPEDESKCLOCK) {
            if (index == 0) {
                return getContext().getResources().getString(33685545);
            }
            if (index == ONE_TWO_BOOT_LISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[TWO_BOOT_DLISTVIEW];
                objArr[DISMISSS_DELAY_0] = daySeq;
                objArr[NONE_LISTVIEW] = hourSeq;
                objArr[TWO_DESKCLOCK_DLISTVIEW] = minutesSeq;
                return resources.getString(33685762, objArr);
            } else if (index == ONE_TWO_DESKCLOCK_LISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[TWO_DESKCLOCK_DLISTVIEW];
                objArr[DISMISSS_DELAY_0] = daySeq;
                objArr[NONE_LISTVIEW] = hourSeq;
                return resources.getString(33685763, objArr);
            } else if (index == ONE_BOOT_LISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[TWO_DESKCLOCK_DLISTVIEW];
                objArr[DISMISSS_DELAY_0] = daySeq;
                objArr[NONE_LISTVIEW] = minutesSeq;
                return resources.getString(33685764, objArr);
            } else if (index == ONE_DESKCLOCK_LISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[NONE_LISTVIEW];
                objArr[DISMISSS_DELAY_0] = daySeq;
                return resources.getString(33685765, objArr);
            } else if (index == TWO_BOOT_DLISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[TWO_DESKCLOCK_DLISTVIEW];
                objArr[DISMISSS_DELAY_0] = hourSeq;
                objArr[NONE_LISTVIEW] = minutesSeq;
                return resources.getString(33685766, objArr);
            } else if (index == TWO_DESKCLOCK_DLISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[NONE_LISTVIEW];
                objArr[DISMISSS_DELAY_0] = hourSeq;
                return resources.getString(33685767, objArr);
            } else {
                resources = getContext().getResources();
                objArr = new Object[NONE_LISTVIEW];
                objArr[DISMISSS_DELAY_0] = minutesSeq;
                return resources.getString(33685768, objArr);
            }
        } else if (index == 0) {
            return getContext().getResources().getString(33685787);
        } else {
            if (index == ONE_TWO_BOOT_LISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[TWO_BOOT_DLISTVIEW];
                objArr[DISMISSS_DELAY_0] = daySeq;
                objArr[NONE_LISTVIEW] = hourSeq;
                objArr[TWO_DESKCLOCK_DLISTVIEW] = minutesSeq;
                return resources.getString(33685780, objArr);
            } else if (index == ONE_TWO_DESKCLOCK_LISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[TWO_DESKCLOCK_DLISTVIEW];
                objArr[DISMISSS_DELAY_0] = daySeq;
                objArr[NONE_LISTVIEW] = hourSeq;
                return resources.getString(33685781, objArr);
            } else if (index == ONE_BOOT_LISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[TWO_DESKCLOCK_DLISTVIEW];
                objArr[DISMISSS_DELAY_0] = daySeq;
                objArr[NONE_LISTVIEW] = minutesSeq;
                return resources.getString(33685782, objArr);
            } else if (index == ONE_DESKCLOCK_LISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[NONE_LISTVIEW];
                objArr[DISMISSS_DELAY_0] = daySeq;
                return resources.getString(33685783, objArr);
            } else if (index == TWO_BOOT_DLISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[TWO_DESKCLOCK_DLISTVIEW];
                objArr[DISMISSS_DELAY_0] = hourSeq;
                objArr[NONE_LISTVIEW] = minutesSeq;
                return resources.getString(33685784, objArr);
            } else if (index == TWO_DESKCLOCK_DLISTVIEW) {
                resources = getContext().getResources();
                objArr = new Object[NONE_LISTVIEW];
                objArr[DISMISSS_DELAY_0] = hourSeq;
                return resources.getString(33685785, objArr);
            } else {
                resources = getContext().getResources();
                objArr = new Object[NONE_LISTVIEW];
                objArr[DISMISSS_DELAY_0] = minutesSeq;
                return resources.getString(33685786, objArr);
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
            ((HwWallpaperManager) this.mWallpaperManager).setCallback(this);
        }
        initHandler(looper);
        this.view_two_action = findViewById(34603127);
        if (this.view_two_action == null) {
            Log.e(TAG, "view_two_action is null");
        } else {
            ShutdownMenuAnimations.getInstance(getContext()).setTwoActionView(this.view_two_action);
        }
        this.view_four_action = findViewById(34603126);
        if (this.view_four_action == null) {
            Log.e(TAG, "view_four_action is null");
        } else {
            this.view_four_action.setVisibility(ONE_DESKCLOCK_LISTVIEW);
            ShutdownMenuAnimations.getInstance(getContext()).setFourActionView(this.view_four_action);
        }
        this.view_confirm_action = findViewById(34603128);
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
            this.mSilentModeButtonTouched = IS_POWEROFF_ALARM_ENABLED;
        }
        setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mWindowTouchSlop = ViewConfiguration.get(this.mContext).getScaledWindowTouchSlop();
    }

    public void initNewUI(Looper looper) {
        this.mWallpaperManager = (WallpaperManager) getContext().getSystemService("wallpaper");
        if (this.mWallpaperManager instanceof HwWallpaperManager) {
            ((HwWallpaperManager) this.mWallpaperManager).setCallback(this);
        }
        initHandler(looper);
        this.view_two_action = findViewById(34603132);
        if (this.view_two_action == null) {
            Log.e(TAG, "view_two_action is null");
        } else {
            ShutdownMenuAnimations.getInstance(getContext()).setTwoActionView(this.view_two_action);
        }
        this.view_confirm_action = findViewById(34603133);
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
            this.mSilentModeButtonTouched = IS_POWEROFF_ALARM_ENABLED;
        }
        setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mWindowTouchSlop = ViewConfiguration.get(this.mContext).getScaledWindowTouchSlop();
    }

    public void deinitUI() {
        HwGlobalActionsData.getSingletoneInstance().unregisterActionStateObserver();
        if (this.mHandler != null) {
            this.mHandler.removeMessages(TWO_DESKCLOCK_DLISTVIEW);
            this.mHandler.removeMessages(ONE_DESKCLOCK_LISTVIEW);
            onStopSounds();
            this.mHandler.removeMessages(ONE_BOOT_LISTVIEW);
            this.mHandler.sendEmptyMessage(ONE_BOOT_LISTVIEW);
        }
        this.isFirstShow = BLUR_SCREENSHOT;
    }

    private void updateActionUI(View v, int actionID, int idText, int idImage) {
        if (v != null) {
            LinearLayout rebootAction = (LinearLayout) v.findViewById(actionID);
            TextView rebootTextView = (TextView) rebootAction.findViewById(34603117);
            rebootTextView.setText(idText);
            ImageView rebootImageView = (ImageView) rebootAction.findViewById(34603123);
            if (this.mHandler != null) {
                this.mHandler.removeMessages(ONE_TWO_DESKCLOCK_LISTVIEW);
            }
            switch (idImage) {
                case ID_IC_AIRPLANEMODE /*33751059*/:
                    rebootImageView.setContentDescription(getContext().getResources().getString(ID_STR_AIRPLANEMODE_OFF));
                    break;
                case ID_IC_AIRPLANEMODE_ON /*33751060*/:
                    rebootImageView.setContentDescription(getContext().getResources().getString(ID_STR_AIRPLANEMODE_ON));
                    break;
                case ID_IC_SILENTMODE_SILENT /*33751061*/:
                    rebootImageView.setContentDescription(getContext().getResources().getString(ID_STR_SILENTMODE));
                    break;
                case ID_IC_SILENTMODE_VIBRATE /*33751062*/:
                    rebootImageView.setContentDescription(getContext().getResources().getString(ID_STR_VIBRATIONMODE));
                    break;
                case ID_IC_SILENTMODE_NORMAL /*33751063*/:
                    rebootImageView.setContentDescription(getContext().getResources().getString(ID_STR_SOUNDMODE));
                    break;
                case ID_IC_REBOOT /*33751064*/:
                case ID_IC_NEW_REBOOT_CONFIRM /*33751331*/:
                    rebootImageView.setContentDescription(getContext().getResources().getString(ID_STR_REBOOT));
                    break;
                case ID_IC_SHUTDOWN /*33751072*/:
                case ID_IC_NEW_SHUTDOWN_CONFIRM /*33751330*/:
                    rebootImageView.setContentDescription(getContext().getResources().getString(ID_STR_SHUTDOWN));
                    break;
            }
            Log.d(TAG, "updateActionUI mSilentModeButtonTouched = " + this.mSilentModeButtonTouched);
            if ("factory".equals(SystemProperties.get("ro.runmode", "normal")) && actionID == 34603209) {
                rebootTextView.setTextColor(getContext().getResources().getColor(33882218));
            }
            if (actionID != 34603210 || this.isFirstShow) {
                rebootImageView.setImageResource(idImage);
            } else {
                ImageView mImage = rebootImageView;
                int mId = idImage;
                Animation animRotation = ShutdownMenuAnimations.getInstance(getContext()).setSoundModeRotate();
                animRotation.setAnimationListener(new AnonymousClass4(rebootImageView, idImage));
                if (rebootImageView != null) {
                    rebootImageView.startAnimation(animRotation);
                }
            }
        }
    }

    private void updateAirplaneModeUI(View v) {
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            updateActionUI(this.view_four_action, 34603209, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE_OFF);
        } else if ((HwGlobalActionsData.getSingletoneInstance().getState() & NONE_LISTVIEW) != 0) {
            updateActionUI(this.view_four_action, 34603209, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE_ON);
        } else {
            updateActionUI(this.view_four_action, 34603209, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE);
        }
    }

    private void updateSilentModeUI(View v) {
        if (this.mHandler != null && this.mSilentModeButtonTouched) {
            this.mHandler.removeMessages(TWO_DESKCLOCK_DLISTVIEW);
            this.mHandler.removeMessages(ONE_DESKCLOCK_LISTVIEW);
        }
        if ((HwGlobalActionsData.getSingletoneInstance().getState() & HwGlobalActionsData.FLAG_SILENTMODE_SILENT) != 0) {
            updateActionUI(this.view_four_action, 34603210, ID_STR_SILENTMODE, ID_IC_SILENTMODE_SILENT);
        } else if ((HwGlobalActionsData.getSingletoneInstance().getState() & HwGlobalActionsData.FLAG_SILENTMODE_VIBRATE) != 0) {
            updateActionUI(this.view_four_action, 34603210, ID_STR_VIBRATIONMODE, ID_IC_SILENTMODE_VIBRATE);
            if (this.mHandler != null && this.mSilentModeButtonTouched) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(ONE_DESKCLOCK_LISTVIEW), 300);
            }
        } else {
            updateActionUI(this.view_four_action, 34603210, ID_STR_SOUNDMODE, ID_IC_SILENTMODE_NORMAL);
            if (this.mHandler != null && this.mSilentModeButtonTouched) {
                this.mHandler.removeMessages(TWO_DESKCLOCK_DLISTVIEW);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(TWO_DESKCLOCK_DLISTVIEW, TWO_DESKCLOCK_DLISTVIEW, DISMISSS_DELAY_0), 300);
            }
        }
    }

    private void showNewRebootAndShutdownUI(View v) {
        updateActionUI(v, 34603211, ID_STR_REBOOT, ID_IC_NEW_REBOOT_CONFIRM);
        setActionClickListener(v, 34603211, REBOOT_TAG);
        updateActionUI(v, 34603212, ID_STR_SHUTDOWN, ID_IC_NEW_SHUTDOWN_CONFIRM);
        setActionClickListener(v, 34603212, SHUTDOWN_TAG);
    }

    private void showRebootAndShutdownUI(View v) {
        updateActionUI(v, 34603211, ID_STR_REBOOT, ID_IC_REBOOT);
        setActionClickListener(v, 34603211, REBOOT_TAG);
        updateActionUI(v, 34603212, ID_STR_SHUTDOWN, ID_IC_SHUTDOWN);
        setActionClickListener(v, 34603212, SHUTDOWN_TAG);
    }

    private void setActionClickListener(View v, int actionID, Object actionTag) {
        View icon_frame = ((LinearLayout) v.findViewById(actionID)).findViewById(34603122);
        icon_frame.setTag(actionTag);
        icon_frame.setOnClickListener(this.mActionClickListener);
    }

    protected void showNewUI() {
        if (!this.mInitedConfirm) {
            this.view_two_action.setVisibility(DISMISSS_DELAY_0);
            this.view_confirm_action.setVisibility(ONE_DESKCLOCK_LISTVIEW);
            this.isFirstShow = BLUR_SCREENSHOT;
            this.mSilentModeButtonTouched = IS_POWEROFF_ALARM_ENABLED;
            this.reboot_view = this.view_two_action.findViewById(34603211);
            this.shutdown_view = this.view_two_action.findViewById(34603212);
            replacePoweroffHint((TextView) this.view_two_action.findViewById(34603214));
            this.mViewList.clear();
            configureTalkbackView(this);
            this.mEnterSet = ShutdownMenuAnimations.getInstance(getContext()).setNewShutdownViewAnimation(BLUR_SCREENSHOT);
            this.mEnterSet.start();
            showNewRebootAndShutdownUI(this.view_two_action);
            this.isFirstShow = IS_POWEROFF_ALARM_ENABLED;
        }
    }

    protected void showBeforeProvisioningUI() {
        if (isHwGlobalTwoActionsSupport()) {
            showNewUI();
            return;
        }
        this.view_four_action.setVisibility(ONE_DESKCLOCK_LISTVIEW);
        this.view_two_action.setVisibility(DISMISSS_DELAY_0);
        this.view_confirm_action.setVisibility(ONE_DESKCLOCK_LISTVIEW);
        this.mViewList.clear();
        ShutdownMenuAnimations.getInstance(getContext()).setMenuViewList(this.mViewList);
        this.reboot_view = this.view_two_action.findViewById(34603211);
        this.shutdown_view = this.view_two_action.findViewById(34603212);
        replacePoweroffHint((TextView) this.view_two_action.findViewById(34603214));
        showRebootAndShutdownUI(this.view_two_action);
    }

    protected void showNormalUI() {
        if (isHwGlobalTwoActionsSupport()) {
            showNewUI();
            return;
        }
        this.isFirstShow = BLUR_SCREENSHOT;
        this.mSilentModeButtonTouched = IS_POWEROFF_ALARM_ENABLED;
        this.airplanemode_view = this.view_four_action.findViewById(34603209);
        this.silentmode_view = this.view_four_action.findViewById(34603210);
        this.reboot_view = this.view_four_action.findViewById(34603211);
        this.shutdown_view = this.view_four_action.findViewById(34603212);
        replacePoweroffHint((TextView) this.view_four_action.findViewById(34603214));
        this.mViewList.clear();
        this.mViewList.add(this.airplanemode_view);
        this.mViewList.add(this.silentmode_view);
        this.mViewList.add(this.reboot_view);
        this.mViewList.add(this.shutdown_view);
        configureTalkbackView(this);
        ShutdownMenuAnimations.getInstance(getContext()).setMenuViewList(this.mViewList);
        this.mEnterSet = ShutdownMenuAnimations.getInstance(getContext()).setImageAnimation(BLUR_SCREENSHOT);
        this.view_four_action.setVisibility(DISMISSS_DELAY_0);
        this.mEnterSet.start();
        updateAirplaneModeUI(this.view_four_action);
        setActionClickListener(this.view_four_action, 34603209, AIRPLANEMODE_TAG);
        updateSilentModeUI(this.view_four_action);
        setActionClickListener(this.view_four_action, 34603210, SILENTMODE_TAG);
        showRebootAndShutdownUI(this.view_four_action);
        this.isFirstShow = IS_POWEROFF_ALARM_ENABLED;
    }

    protected void onPlaySound(int streamType, int flags) {
        if (this.mHandler != null && this.mHandler.hasMessages(TWO_BOOT_DLISTVIEW)) {
            this.mHandler.removeMessages(TWO_BOOT_DLISTVIEW);
            onStopSounds();
        }
        synchronized (this) {
            ToneGenerator toneGen = getOrCreateToneGenerator(streamType);
            if (toneGen != null) {
                toneGen.startTone(24);
                if (this.mHandler != null) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(TWO_BOOT_DLISTVIEW), 150);
                }
            }
        }
    }

    protected void onStopSounds() {
        synchronized (this) {
            for (int i = AudioSystem.getNumStreamTypes() - 1; i >= 0; i--) {
                ToneGenerator toneGen = this.mToneGenerators[i];
                if (toneGen != null) {
                    toneGen.stopTone();
                }
            }
            if (this.mHandler != null) {
                this.mHandler.removeMessages(ONE_BOOT_LISTVIEW);
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(ONE_BOOT_LISTVIEW), MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            }
        }
    }

    protected void onVibrate() {
        this.mVibrator.vibrate(300, VIBRATION_ATTRIBUTES);
    }

    private ToneGenerator getOrCreateToneGenerator(int streamType) {
        ToneGenerator toneGenerator;
        synchronized (this) {
            if (this.mToneGenerators[streamType] == null) {
                try {
                    this.mToneGenerators[streamType] = new ToneGenerator(streamType, MAX_VOLUME);
                } catch (RuntimeException e) {
                }
            }
            toneGenerator = this.mToneGenerators[streamType];
        }
        return toneGenerator;
    }

    protected void onFreeResources() {
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
        if (event.getKeyCode() != ONE_DESKCLOCK_LISTVIEW || NONE_LISTVIEW != event.getAction() || this.mCallback == null) {
            return super.dispatchKeyEvent(event);
        }
        this.mCallback.onOtherAreaPressed();
        return BLUR_SCREENSHOT;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.mEnableAccessibilityController != null) {
            int action = event.getActionMasked();
            if (action == 0) {
                int eventX = (int) event.getX();
                int eventY = (int) event.getY();
                if (eventX >= (-this.mWindowTouchSlop) && eventY >= (-this.mWindowTouchSlop) && eventX < getWidth() + this.mWindowTouchSlop) {
                    if (eventY >= getHeight() + this.mWindowTouchSlop) {
                    }
                }
                this.mCancelOnUp = BLUR_SCREENSHOT;
            }
            try {
                if (this.mIntercepted) {
                    boolean onTouchEvent = this.mEnableAccessibilityController.onTouchEvent(event);
                    if (action == NONE_LISTVIEW) {
                        if (this.mCancelOnUp) {
                            this.mCancelOnUp = IS_POWEROFF_ALARM_ENABLED;
                            this.mIntercepted = IS_POWEROFF_ALARM_ENABLED;
                        } else {
                            this.mCancelOnUp = IS_POWEROFF_ALARM_ENABLED;
                            this.mIntercepted = IS_POWEROFF_ALARM_ENABLED;
                        }
                    }
                    return onTouchEvent;
                }
                this.mIntercepted = this.mEnableAccessibilityController.onInterceptTouchEvent(event);
                if (this.mIntercepted) {
                    long now = SystemClock.uptimeMillis();
                    event = MotionEvent.obtain(now, now, TWO_BOOT_DLISTVIEW, 0.0f, 0.0f, DISMISSS_DELAY_0);
                    event.setSource(4098);
                    this.mCancelOnUp = BLUR_SCREENSHOT;
                }
                if (action == NONE_LISTVIEW) {
                    if (this.mCancelOnUp) {
                        this.mCancelOnUp = IS_POWEROFF_ALARM_ENABLED;
                        this.mIntercepted = IS_POWEROFF_ALARM_ENABLED;
                    } else {
                        this.mCancelOnUp = IS_POWEROFF_ALARM_ENABLED;
                        this.mIntercepted = IS_POWEROFF_ALARM_ENABLED;
                    }
                }
            } catch (Throwable th) {
                if (action == NONE_LISTVIEW) {
                    if (this.mCancelOnUp) {
                        this.mCancelOnUp = IS_POWEROFF_ALARM_ENABLED;
                        this.mIntercepted = IS_POWEROFF_ALARM_ENABLED;
                    } else {
                        this.mCancelOnUp = IS_POWEROFF_ALARM_ENABLED;
                        this.mIntercepted = IS_POWEROFF_ALARM_ENABLED;
                    }
                }
            }
        }
        if (canDisableAccessibilityViaGesture()) {
            if (!this.mDisableIntercepted) {
                onDisableInterceptTouchEvent(event);
            } else if (onDisableTouchEvent(event)) {
                return BLUR_SCREENSHOT;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void setEnableAccessibilityController(EnableAccessibilityController mEnableAccessibilityController) {
        this.mEnableAccessibilityController = mEnableAccessibilityController;
    }

    public void showRestartingHint() {
        if (this.view_confirm_action != null) {
            TextView textView = (TextView) this.view_confirm_action.findViewById(34603117);
            if (textView != null) {
                textView.setText(ID_STR_REBOOTING);
            }
        }
    }

    public void showShutdongingHint() {
        if (this.view_confirm_action != null) {
            TextView textView = (TextView) this.view_confirm_action.findViewById(34603117);
            if (textView != null) {
                textView.setText(ID_STR_SHUTDOWNING);
            }
        }
    }

    private boolean canDisableAccessibilityViaGesture() {
        if (SystemProperties.getBoolean(TALKBACK_CONFIG, BLUR_SCREENSHOT) && isTalkBackServicesOn()) {
            return BLUR_SCREENSHOT;
        }
        return IS_POWEROFF_ALARM_ENABLED;
    }

    private boolean onDisableInterceptTouchEvent(MotionEvent event) {
        if (event.getActionMasked() != 0) {
            return IS_POWEROFF_ALARM_ENABLED;
        }
        this.mFirstPointerDownX = event.getX(DISMISSS_DELAY_0);
        this.mFirstPointerDownY = event.getY(DISMISSS_DELAY_0);
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(ONE_TWO_BOOT_LISTVIEW, 3000);
        }
        this.mDisableIntercepted = BLUR_SCREENSHOT;
        return BLUR_SCREENSHOT;
    }

    private boolean onDisableTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        switch (event.getActionMasked()) {
            case NONE_LISTVIEW /*1*/:
            case TWO_BOOT_DLISTVIEW /*3*/:
            case ONE_TWO_DESKCLOCK_LISTVIEW /*6*/:
                cancelDisableAccssibility();
                break;
            case TWO_DESKCLOCK_DLISTVIEW /*2*/:
                if (Math.abs(MathUtils.dist(event.getX(DISMISSS_DELAY_0), event.getY(DISMISSS_DELAY_0), this.mFirstPointerDownX, this.mFirstPointerDownY)) > this.mTouchSlop) {
                    cancelDisableAccssibility();
                    break;
                }
                break;
            case ONE_BOOT_LISTVIEW /*5*/:
                if (pointerCount > TWO_DESKCLOCK_DLISTVIEW) {
                    cancelDisableAccssibility();
                    break;
                }
                break;
        }
        if (!this.mCanceled) {
            return BLUR_SCREENSHOT;
        }
        this.mCanceled = IS_POWEROFF_ALARM_ENABLED;
        return IS_POWEROFF_ALARM_ENABLED;
    }

    private void configureTalkbackView(Object mView) {
        this.mAccessibilityTip = (TextView) ((HwGlobalActionsView) mView).findViewById(34603215);
        if (this.mAccessibilityTip == null) {
            return;
        }
        if (canDisableAccessibilityViaGesture()) {
            this.mAccessibilityTip.setVisibility(DISMISSS_DELAY_0);
        } else {
            this.mAccessibilityTip.setVisibility(8);
        }
    }

    private void cancelDisableAccssibility() {
        this.mCanceled = BLUR_SCREENSHOT;
        this.mDisableIntercepted = IS_POWEROFF_ALARM_ENABLED;
        if (this.mHandler != null) {
            this.mHandler.removeMessages(ONE_TWO_BOOT_LISTVIEW);
        }
    }

    private void disableTalkBackService() {
        int i = DISMISSS_DELAY_0;
        if (this.mContext != null) {
            boolean accessibilityEnabled = IS_POWEROFF_ALARM_ENABLED;
            loadInstalledServices();
            Set<ComponentName> enabledServices = getEnabledServicesFromSettings();
            enabledServices.remove(ComponentName.unflattenFromString(TALKBACK_COMPONENT_NAME));
            Set<ComponentName> installedServices = sInstalledServices;
            StatisticalUtils.reportc(this.mContext, 24);
            for (ComponentName enabledService : enabledServices) {
                if (installedServices.contains(enabledService)) {
                    accessibilityEnabled = BLUR_SCREENSHOT;
                    break;
                }
            }
            StringBuilder enabledServicesBuilder = new StringBuilder();
            for (ComponentName enabledService2 : enabledServices) {
                enabledServicesBuilder.append(enabledService2.flattenToString());
                enabledServicesBuilder.append(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
            }
            int enabledServicesBuilderLength = enabledServicesBuilder.length();
            if (enabledServicesBuilderLength > 0) {
                enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
            }
            Secure.putStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", enabledServicesBuilder.toString(), -2);
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String str = "accessibility_enabled";
            if (accessibilityEnabled) {
                i = NONE_LISTVIEW;
            }
            Secure.putIntForUser(contentResolver, str, i, -2);
            if (this.mAccessibilityTip != null) {
                this.mAccessibilityTip.setVisibility(8);
            }
        }
    }

    private Set<ComponentName> getEnabledServicesFromSettings() {
        String enabledServicesSetting = Secure.getStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", -2);
        if (enabledServicesSetting == null) {
            enabledServicesSetting = AppHibernateCst.INVALID_PKG;
        }
        Set<ComponentName> enabledServices = new HashSet();
        SimpleStringSplitter colonSplitter = sStringColonSplitter;
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
            for (int i = DISMISSS_DELAY_0; i < installedServiceInfoCount; i += NONE_LISTVIEW) {
                ResolveInfo resolveInfo = ((AccessibilityServiceInfo) installedServiceInfos.get(i)).getResolveInfo();
                if (!(resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.packageName == null || resolveInfo.serviceInfo.name == null)) {
                    installedServices.add(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name));
                }
            }
        }
    }

    private boolean isTalkBackServicesOn() {
        if (this.mContext == null) {
            return IS_POWEROFF_ALARM_ENABLED;
        }
        boolean accessibilityEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_enabled", DISMISSS_DELAY_0, -2) == NONE_LISTVIEW ? BLUR_SCREENSHOT : IS_POWEROFF_ALARM_ENABLED;
        String enabledSerices = Secure.getStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", -2);
        boolean contains = enabledSerices != null ? enabledSerices.contains(TALKBACK_COMPONENT_NAME) : IS_POWEROFF_ALARM_ENABLED;
        if (!accessibilityEnabled) {
            contains = IS_POWEROFF_ALARM_ENABLED;
        }
        return contains;
    }

    private void replacePoweroffHint(TextView poweroffHintView) {
        if (poweroffHintView != null) {
            Resources tmpResources = this.mContext.getResources();
            int poweroffHintTime = tmpResources.getInteger(34537474);
            String string = tmpResources.getString(33685838);
            Object[] objArr = new Object[NONE_LISTVIEW];
            objArr[DISMISSS_DELAY_0] = Integer.valueOf(poweroffHintTime);
            poweroffHintView.setText(String.format(string, objArr));
        }
    }

    public boolean isHwGlobalTwoActionsSupport() {
        return this.mContext.getResources().getBoolean(34406402);
    }
}
