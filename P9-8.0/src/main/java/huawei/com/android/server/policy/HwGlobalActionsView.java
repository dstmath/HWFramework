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
import android.media.AudioAttributes.Builder;
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
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.app.HwWallpaperManager;
import huawei.com.android.server.policy.HwGlobalActionsData.ActionStateObserver;
import huawei.com.android.server.policy.recsys.HwLog;
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
    private static final String DONOTNEED_TAG = "do_not_need";
    private static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final int FREE_DELAY = 10000;
    private static final String GOTOSET_TAG = "go_to_set";
    private static final int ID_IC_AIRPLANEMODE = 33751059;
    private static final int ID_IC_AIRPLANEMODE_OFF = 33751636;
    private static final int ID_IC_AIRPLANEMODE_ON = 33751060;
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
    private static final boolean IS_POWEROFF_ALARM_ENABLED = StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get("ro.poweroff_alarm", StorageUtils.SDCARD_ROMOUNTED_STATE));
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
    private static final String RECOMMEND_TAG = "recommend";
    private static final String SETTINGS_PACKAGENAME = "com.android.providers.settings";
    private static final String SHUTDOWN_TAG = "shutdown";
    private static final String SILENTMODE_TAG = "silent_mode";
    private static final String TAG = "HwGlobalActions";
    private static final String TALKBACK_COMPONENT_NAME = "com.google.android.marvin.talkback/com.google.android.marvin.talkback.TalkBackService";
    private static final String TALKBACK_CONFIG = "ro.config.hw_talkback_btn";
    private static final int TWO_BOOT_DLISTVIEW = 3;
    private static final int TWO_DESKCLOCK_DLISTVIEW = 2;
    private static int TYPEDESKCLOCK = 1;
    private static int TYPESETTINGS = 2;
    public static final int VIBRATE_DELAY = 300;
    private static final int VIBRATE_DURATION = 300;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private static long mShowBootOnTime = -1;
    private static long mShowDeskClockTime = -1;
    private static Boolean[] mState = new Boolean[]{Boolean.valueOf(true), Boolean.valueOf(true)};
    private static final Set<ComponentName> sInstalledServices = new HashSet();
    private static final SimpleStringSplitter sStringColonSplitter = new SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
    private View airplanemode_view;
    private boolean isFirstShow;
    private AccessibilityDelegate mAccessibilityDelegate;
    private TextView mAccessibilityTip;
    private OnClickListener mActionClickListener;
    private ActionPressedCallback mCallback;
    private boolean mCanceled;
    private boolean mDisableIntercepted;
    private View mDoNotNeedView;
    private Drawable mDrawable;
    private AnimatorSet mEnterSet;
    private boolean mEventAfterActionDown;
    private float mFirstPointerDownX;
    private float mFirstPointerDownY;
    private View mGoToSetView;
    private Handler mHandler;
    private boolean mInitedConfirm;
    private boolean mIsBootOnTimeClose;
    private boolean mIsDeskClockClose;
    private long mLastClickTime;
    private int mListviewState;
    private MyActionStateObserver mObserver;
    private View mRecommendView;
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

        void onDoNotNeedActionPressed();

        void onGoToSetActionPressed();

        void onOtherAreaPressed();

        void onRebootActionPressed();

        void onRecommendAreaPressed();

        void onShutdownActionPressed(boolean z, boolean z2, int i);

        void onSilentModeActionPressed();
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
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & 131072) != 0) {
                HwGlobalActionsView.this.initConfirmAction(true);
            } else if (HwGlobalActionsData.getSingletoneInstance().isDeviceProvisioned()) {
                HwGlobalActionsView.this.showNormalUI();
            } else {
                HwGlobalActionsView.this.showBeforeProvisioningUI();
            }
        }

        public void onShutdownActionStateChanged() {
            if ((HwGlobalActionsData.getSingletoneInstance().getState() & HwGlobalActionsData.FLAG_SHUTDOWN_CONFIRM) != 0) {
                HwGlobalActionsView.this.initConfirmAction(false);
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

        public MyAdapter(ArrayList<String> list, Context context) {
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
                holder.checkBox = (CheckBox) convertView.findViewById(34603173);
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
            HwGlobalActionsView.mState = this.state;
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
        this.mViewList = new ArrayList();
        this.isFirstShow = true;
        this.mIsDeskClockClose = true;
        this.mIsBootOnTimeClose = true;
        this.mListviewState = 0;
        this.mShutdownListViewText = new ArrayList();
        this.mLastClickTime = 0;
        this.mDisableIntercepted = false;
        this.mCanceled = false;
        this.mTouchSlop = 0.0f;
        this.mFirstPointerDownX = 0.0f;
        this.mFirstPointerDownY = 0.0f;
        this.mAccessibilityTip = null;
        this.mTone = null;
        this.mEventAfterActionDown = false;
        this.mInitedConfirm = false;
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
            /* JADX WARNING: Missing block: B:7:0x002a, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onClick(View v) {
                if (HwGlobalActionsView.this.mHandler != null) {
                    HwGlobalActionsView.this.mHandler.removeMessages(6);
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
                        HwGlobalActionsView.this.mSilentModeButtonTouched = true;
                        HwGlobalActionsView.this.mCallback.onSilentModeActionPressed();
                    } else if (obj == HwGlobalActionsView.REBOOT_TAG) {
                        if (ActivityManager.isUserAMonkey()) {
                            Log.w(HwGlobalActionsView.TAG, "ignoring monkey's attempt to reboot");
                            return;
                        }
                        HwGlobalActionsView.this.mCallback.onRebootActionPressed();
                    } else if (obj == HwGlobalActionsView.SHUTDOWN_TAG) {
                        if (ActivityManager.isUserAMonkey()) {
                            Log.w(HwGlobalActionsView.TAG, "ignoring monkey's attempt to shutdown");
                            return;
                        }
                        HwGlobalActionsView.this.mCallback.onShutdownActionPressed(HwGlobalActionsView.this.mIsDeskClockClose, HwGlobalActionsView.this.mIsBootOnTimeClose, HwGlobalActionsView.this.mListviewState);
                    } else if (obj == HwGlobalActionsView.RECOMMEND_TAG) {
                        HwGlobalActionsView.this.mCallback.onRecommendAreaPressed();
                    } else if (obj == HwGlobalActionsView.GOTOSET_TAG) {
                        HwGlobalActionsView.this.mCallback.onGoToSetActionPressed();
                    } else if (obj == HwGlobalActionsView.DONOTNEED_TAG) {
                        HwGlobalActionsView.this.mCallback.onDoNotNeedActionPressed();
                    }
                }
            }
        };
        if (this.mContext != null) {
            this.mTouchSlop = (float) this.mContext.getResources().getDimensionPixelSize(17104904);
            this.mTone = RingtoneManager.getRingtone(this.mContext, System.DEFAULT_NOTIFICATION_URI);
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

    private void initConfirmAction(boolean isReboot) {
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
        mState[0] = Boolean.valueOf(true);
        mState[1] = Boolean.valueOf(true);
        int ret = initAdapterDate();
        this.mShutdownListviewAdapter = new MyAdapter(this.mShutdownListViewText, getContext());
        this.shutdown_listview.setAdapter(this.mShutdownListviewAdapter);
        if (isReboot) {
            textView.setText(ID_STR_REBOOT_CONFIRM);
            imageView.setImageResource(hwNewGlobalAction ? 33751230 : ID_IC_REBOOT_CONFIRM);
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
            imageView.setImageResource(hwNewGlobalAction ? 33751229 : ID_IC_SHUTDOWN_CONFIRM);
            imageView.setContentDescription(getContext().getResources().getString(ID_STR_SHUTDOWN_CONFIRM));
            icon_frame.setTag(SHUTDOWN_TAG);
            this.shutdown_listview.setVisibility(0);
            this.shutdown_listview.setTag("disable-multi-select-move");
            this.shutdown_listview.setOverScrollMode(2);
            if (ret == 0 || ret == 1 || ActivityManager.getCurrentUser() != 0) {
                this.shutdown_listview.setVisibility(8);
            }
            if (hwNewGlobalAction) {
                ShutdownMenuAnimations.getInstance(getContext()).newShutdownOrRebootEnterAnim(this.shutdown_view, isReboot);
            } else {
                ShutdownMenuAnimations.getInstance(getContext()).shutdownOrRebootEnterAnim(this.shutdown_view, isReboot);
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

    private String formateTime(long alarmTime, int type) {
        long delta = alarmTime - System.currentTimeMillis();
        int hours = (int) (delta / WifiProCommonUtils.RECHECK_DELAYED_MS);
        int minutes = (int) ((delta / AppHibernateCst.DELAY_ONE_MINS) % 60);
        int days = hours / 24;
        hours %= 24;
        String daySeq = getContext().getResources().getQuantityString(34406402, days, new Object[]{Integer.valueOf(days)});
        String hourSeq = getContext().getResources().getQuantityString(34406403, hours, new Object[]{Integer.valueOf(hours)});
        String minutesSeq = getContext().getResources().getQuantityString(34406404, minutes, new Object[]{Integer.valueOf(minutes)});
        int index = ((days > 0 ? 4 : 0) | (hours > 0 ? 2 : 0)) | (minutes > 0 ? 1 : 0);
        if (type == TYPEDESKCLOCK) {
            if (index == 0) {
                return getContext().getResources().getString(33685545);
            }
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
        } else if (index == 0) {
            return getContext().getResources().getString(33685794);
        } else {
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

    public void initNewUI(Looper looper) {
        this.mWallpaperManager = (WallpaperManager) getContext().getSystemService("wallpaper");
        if (this.mWallpaperManager instanceof HwWallpaperManager) {
            ((HwWallpaperManager) this.mWallpaperManager).setCallback(this);
        }
        initHandler(looper);
        this.view_two_action = findViewById(34603177);
        if (this.view_two_action == null) {
            Log.e(TAG, "view_two_action is null");
        } else {
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

    private void updateActionUI(View v, int actionID, int idText, final int idImage) {
        if (v != null) {
            LinearLayout rebootAction = (LinearLayout) v.findViewById(actionID);
            TextView rebootTextView = (TextView) rebootAction.findViewById(34603129);
            rebootTextView.setText(idText);
            final ImageView rebootImageView = (ImageView) rebootAction.findViewById(34603172);
            if (this.mHandler != null) {
                this.mHandler.removeMessages(6);
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
                case 33751230:
                    rebootImageView.setContentDescription(getContext().getResources().getString(ID_STR_REBOOT));
                    break;
                case ID_IC_SHUTDOWN /*33751072*/:
                case 33751229:
                    rebootImageView.setContentDescription(getContext().getResources().getString(ID_STR_SHUTDOWN));
                    break;
            }
            Log.d(TAG, "updateActionUI mSilentModeButtonTouched = " + this.mSilentModeButtonTouched);
            if ("factory".equals(SystemProperties.get("ro.runmode", "normal")) && actionID == 34603145) {
                rebootTextView.setTextColor(getContext().getResources().getColor(33882221));
            }
            if (actionID != 34603146 || (this.isFirstShow ^ 1) == 0) {
                rebootImageView.setImageResource(idImage);
            } else {
                ImageView mImage = rebootImageView;
                int mId = idImage;
                Animation animRotation = ShutdownMenuAnimations.getInstance(getContext()).setSoundModeRotate();
                animRotation.setAnimationListener(new AnimationListener() {
                    public void onAnimationStart(Animation arg0) {
                        ShutdownMenuAnimations.getInstance(HwGlobalActionsView.this.mContext).setIsAnimRunning(true);
                        if (rebootImageView != null) {
                            rebootImageView.setImageResource(idImage);
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
                if (rebootImageView != null) {
                    rebootImageView.startAnimation(animRotation);
                }
            }
        }
    }

    private void updateAirplaneModeUI(View v) {
        if ("factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            updateActionUI(this.view_four_action, 34603145, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE_OFF);
        } else if ((HwGlobalActionsData.getSingletoneInstance().getState() & 1) != 0) {
            updateActionUI(this.view_four_action, 34603145, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE_ON);
        } else {
            updateActionUI(this.view_four_action, 34603145, ID_STR_AIRPLANEMODE, ID_IC_AIRPLANEMODE);
        }
    }

    private void updateSilentModeUI(View v) {
        if (this.mHandler != null && this.mSilentModeButtonTouched) {
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(4);
        }
        if ((HwGlobalActionsData.getSingletoneInstance().getState() & 256) != 0) {
            updateActionUI(this.view_four_action, 34603146, ID_STR_SILENTMODE, ID_IC_SILENTMODE_SILENT);
        } else if ((HwGlobalActionsData.getSingletoneInstance().getState() & 512) != 0) {
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
    }

    private void showRebootAndShutdownUI(View v) {
        updateActionUI(v, 34603151, ID_STR_REBOOT, ID_IC_REBOOT);
        setActionClickListener(v, 34603151, REBOOT_TAG);
        updateActionUI(v, 34603152, ID_STR_SHUTDOWN, ID_IC_SHUTDOWN);
        setActionClickListener(v, 34603152, SHUTDOWN_TAG);
    }

    public void showRecommendView() {
        HwLog.d(TAG, "HwGlobalActionsView--showRecommendView");
        if (this.mRecommendView == null) {
            HwLog.e(TAG, "RecommendView is null when show recommend");
            return;
        }
        this.mRecommendView.clearAnimation();
        this.mRecommendView.startAnimation(ShutdownMenuAnimations.getInstance(this.mContext).getRecommendAnimationEntry());
        this.mRecommendView.setVisibility(0);
        HwLog.d(TAG, "showRecommendView: recommend view set visible");
    }

    public void hideRecommendView(boolean isAnimation) {
        HwLog.d(TAG, "hideRecommendView, is anim: " + isAnimation);
        if (this.mRecommendView == null || this.mGoToSetView == null || this.mDoNotNeedView == null) {
            HwLog.e(TAG, "RecommendView or child view is null when hide recommend");
            return;
        }
        if (isAnimation) {
            this.mRecommendView.clearAnimation();
            Animation animation = ShutdownMenuAnimations.getInstance(this.mContext).getRecommendAnimationExit();
            animation.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    HwGlobalActionsView.this.mRecommendView.setClickable(false);
                    HwGlobalActionsView.this.mGoToSetView.setClickable(false);
                    HwGlobalActionsView.this.mDoNotNeedView.setClickable(false);
                }

                public void onAnimationEnd(Animation animation) {
                    HwGlobalActionsView.this.mRecommendView.setVisibility(8);
                    HwLog.d(HwGlobalActionsView.TAG, "onAnimationEnd: recommend view set gone");
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            this.mRecommendView.startAnimation(animation);
        } else {
            this.mRecommendView.setVisibility(8);
            HwLog.d(TAG, "hideRecommendView: recommend view set gone");
        }
    }

    private void setRecommendClickListener(View v, Object actionTag) {
        v.setTag(actionTag);
        v.setOnClickListener(this.mActionClickListener);
    }

    private void setActionClickListener(View v, int actionID, Object actionTag) {
        View icon_frame = ((LinearLayout) v.findViewById(actionID)).findViewById(34603108);
        icon_frame.setTag(actionTag);
        icon_frame.setOnClickListener(this.mActionClickListener);
    }

    protected void showNewUI() {
        if (!this.mInitedConfirm) {
            this.view_two_action.setVisibility(0);
            this.view_confirm_action.setVisibility(4);
            this.isFirstShow = true;
            this.mSilentModeButtonTouched = false;
            this.reboot_view = this.view_two_action.findViewById(34603151);
            this.shutdown_view = this.view_two_action.findViewById(34603152);
            this.mRecommendView = this.view_two_action.findViewById(34603189);
            if (this.mRecommendView != null) {
                this.mGoToSetView = this.mRecommendView.findViewById(34603190);
                this.mDoNotNeedView = this.mRecommendView.findViewById(34603191);
            }
            replacePoweroffHint((TextView) this.view_two_action.findViewById(34603150));
            this.mViewList.clear();
            configureTalkbackView(this);
            this.mEnterSet = ShutdownMenuAnimations.getInstance(getContext()).setNewShutdownViewAnimation(true);
            this.mEnterSet.start();
            showNewRebootAndShutdownUI(this.view_two_action);
            if (!(this.mRecommendView == null || this.mGoToSetView == null || this.mDoNotNeedView == null)) {
                setRecommendClickListener(this.mRecommendView, RECOMMEND_TAG);
                setRecommendClickListener(this.mGoToSetView, GOTOSET_TAG);
                setRecommendClickListener(this.mDoNotNeedView, DONOTNEED_TAG);
            }
            this.isFirstShow = false;
        }
    }

    protected void showBeforeProvisioningUI() {
        if (isHwGlobalTwoActionsSupport()) {
            showNewUI();
            return;
        }
        this.view_four_action.setVisibility(4);
        this.view_two_action.setVisibility(0);
        this.view_confirm_action.setVisibility(4);
        this.mViewList.clear();
        ShutdownMenuAnimations.getInstance(getContext()).setMenuViewList(this.mViewList);
        this.reboot_view = this.view_two_action.findViewById(34603151);
        this.shutdown_view = this.view_two_action.findViewById(34603152);
        replacePoweroffHint((TextView) this.view_two_action.findViewById(34603150));
        showRebootAndShutdownUI(this.view_two_action);
    }

    protected void showNormalUI() {
        if (isHwGlobalTwoActionsSupport()) {
            showNewUI();
            return;
        }
        this.isFirstShow = true;
        this.mSilentModeButtonTouched = false;
        this.airplanemode_view = this.view_four_action.findViewById(34603145);
        this.silentmode_view = this.view_four_action.findViewById(34603146);
        this.reboot_view = this.view_four_action.findViewById(34603151);
        this.shutdown_view = this.view_four_action.findViewById(34603152);
        replacePoweroffHint((TextView) this.view_four_action.findViewById(34603150));
        this.mViewList.clear();
        this.mViewList.add(this.airplanemode_view);
        this.mViewList.add(this.silentmode_view);
        this.mViewList.add(this.reboot_view);
        this.mViewList.add(this.shutdown_view);
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

    protected void onPlaySound(int streamType, int flags) {
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

    protected void onStopSounds() {
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

    protected void onVibrate() {
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
        if (handleKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean handleKeyEvent(KeyEvent event) {
        if (event.getAction() == 1) {
            switch (event.getKeyCode()) {
                case 4:
                    if (this.mCallback != null) {
                        this.mCallback.onOtherAreaPressed();
                        return true;
                    }
                    break;
                case 21:
                    if (isHwGlobalTwoActionsSupport() && this.view_two_action != null && this.view_two_action.getVisibility() == 0 && this.reboot_view != null) {
                        this.reboot_view.findViewById(34603108).performClick();
                        return true;
                    }
                case 22:
                    if (isHwGlobalTwoActionsSupport() && this.view_two_action != null && this.view_two_action.getVisibility() == 0 && this.shutdown_view != null) {
                        this.shutdown_view.findViewById(34603108).performClick();
                        return true;
                    }
                case 66:
                case 160:
                    if (isHwGlobalTwoActionsSupport() && this.view_confirm_action != null && this.view_confirm_action.getVisibility() == 0) {
                        View icon_frame = this.view_confirm_action.findViewById(34603108);
                        if (icon_frame != null) {
                            icon_frame.performClick();
                        }
                    }
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
        if (SystemProperties.getBoolean(TALKBACK_CONFIG, true) && isTalkBackServicesOn()) {
            return true;
        }
        return false;
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
                }
                cancelDisableAccssibility();
                break;
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

    private void disableTalkBackService() {
        int i = 0;
        if (this.mContext != null) {
            boolean accessibilityEnabled = false;
            loadInstalledServices();
            Set<ComponentName> enabledServices = getEnabledServicesFromSettings();
            enabledServices.remove(ComponentName.unflattenFromString(TALKBACK_COMPONENT_NAME));
            Set<ComponentName> installedServices = sInstalledServices;
            StatisticalUtils.reportc(this.mContext, 24);
            for (ComponentName enabledService : enabledServices) {
                if (installedServices.contains(enabledService)) {
                    accessibilityEnabled = true;
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
                i = 1;
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
            enabledServicesSetting = "";
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
            for (int i = 0; i < installedServiceInfoCount; i++) {
                ResolveInfo resolveInfo = ((AccessibilityServiceInfo) installedServiceInfos.get(i)).getResolveInfo();
                if (!(resolveInfo == null || resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.packageName == null || resolveInfo.serviceInfo.name == null)) {
                    installedServices.add(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name));
                }
            }
        }
    }

    private boolean isTalkBackServicesOn() {
        if (this.mContext == null) {
            return false;
        }
        boolean accessibilityEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_enabled", 0, -2) == 1;
        String enabledSerices = Secure.getStringForUser(this.mContext.getContentResolver(), "enabled_accessibility_services", -2);
        boolean isContainsTalkBackService = enabledSerices != null ? enabledSerices.contains(TALKBACK_COMPONENT_NAME) : false;
        if (!accessibilityEnabled) {
            isContainsTalkBackService = false;
        }
        return isContainsTalkBackService;
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

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mTone != null) {
            this.mTone.stop();
        }
    }
}
