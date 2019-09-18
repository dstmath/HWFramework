package com.android.server.audio;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.cover.HallState;
import android.cover.IHallCallback;
import android.cover.IHwCoverManager;
import android.media.AudioSystem;
import android.media.IAudioModeDispatcher;
import android.media.audiofx.AudioEffect;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IHwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.server.location.HwLocalLocationProvider;
import com.android.server.pfw.autostartup.comm.XmlConst;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.pgmng.IPGPlugCallbacks;
import com.huawei.pgmng.PGAction;
import com.huawei.pgmng.PGPlug;
import com.huawei.pgmng.plug.PGSdk;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import vendor.huawei.hardware.dolby.dms.V1_0.IDms;
import vendor.huawei.hardware.dolby.dms.V1_0.IDmsCallbacks;

public final class HwAudioServiceEx implements IHwAudioServiceEx {
    private static final String ACTION_ANALOG_TYPEC_NOTIFY = "ACTION_TYPEC_NONOTIFY";
    private static final String ACTION_DEVICE_OUT_USB_DEVICE_EXTEND = "huawei.intent.action.OUT_USB_DEVICE_EXTEND";
    private static final String ACTION_DIGITAL_TYPEC_NOTIFY = "ACTION_DIGITAL_TYPEC_NONOTIFY";
    private static final String ACTION_KILLED_APP_FOR_KARAOKE = "huawei.intent.action.APP_KILLED_FOR_KARAOKE_ACTION";
    private static final String ACTION_SEND_AUDIO_RECORD_STATE = "huawei.media.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final String ACTION_START_APP_FOR_KARAOKE = "huawei.media.ACTIVITY_STARTING_FOR_KARAOKE_ACTION";
    private static final String ACTION_SWS_EQ = "huawei.intent.action.SWS_EQ";
    private static final int ANALOG_TYPEC = 1;
    private static final int ANALOG_TYPEC_CONNECTED_DISABLE = 0;
    private static final int ANALOG_TYPEC_CONNECTED_ENABLE = 1;
    private static final int ANALOG_TYPEC_CONNECTED_ID = 1;
    private static final int ANALOG_TYPEC_DEVICES = 131084;
    private static final String ANALOG_TYPEC_FLAG = "audio_capability#usb_analog_hs_report";
    private static final String BOOT_VOLUME_PROPERTY = "persist.sys.volume.ringIndex";
    private static final int BYTES_PER_INT = 4;
    private static final String CMDINTVALUE = "Integer Value";
    private static final String CONCURRENT_CAPTURE_PROPERTY = "ro.config.concurrent_capture";
    private static final String[] CONCURRENT_RECORD_OTHER = {"com.baidu.BaiduMap", "com.autonavi.minimap"};
    private static final String[] CONCURRENT_RECORD_SYSTEM = {"com.huawei.vassistant"};
    private static final String CONFIG_FILE_WHITE_BLACK_APP = "xml/hw_karaokeeffect_app_config.xml";
    /* access modifiers changed from: private */
    public static final boolean DEBUG = SystemProperties.getBoolean("ro.media.debuggable", false);
    private static final int DIGITAL_TYPEC_CONNECTED_DISABLE = 0;
    private static final int DIGITAL_TYPEC_CONNECTED_ENABLE = 1;
    private static final int DIGITAL_TYPEC_CONNECTED_ID = 2;
    private static final String DIGITAL_TYPEC_FLAG = "typec_compatibility_check";
    private static final String DIGITAL_TYPEC_REPORT_FLAG = "audio_capability#usb_compatibility_report";
    private static final int DIGTIAL_TYPEC = 2;
    private static final int DOLBY_HIGH_PRIO = 1;
    private static final int DOLBY_LOW_PRIO = -1;
    private static final String DOLBY_MODE_HEADSET_STATE = "persist.sys.dolby.state";
    private static final String DOLBY_MODE_OFF_PARA = "dolby_profile=off";
    private static final String DOLBY_MODE_ON_PARA = "dolby_profile=on";
    private static final String DOLBY_MODE_PRESTATE = "persist.sys.dolby.prestate";
    private static final String[] DOLBY_PROFILE = {"off", "smart", "movie", "music"};
    /* access modifiers changed from: private */
    public static final boolean DOLBY_SOUND_EFFECTS_SUPPORT = SystemProperties.getBoolean("ro.config.dolby_dap", false);
    private static final String DOLBY_UPDATE_EVENT = "dolby_dap_params_update";
    private static final String DOLBY_UPDATE_EVENT_DS_STATE = "ds_state_change";
    private static final String DOLBY_UPDATE_EVENT_PROFILE = "profile_change";
    private static final String DTS_MODE_OFF_PARA = "srs_cfg:trumedia_enable=0";
    private static final String DTS_MODE_ON_PARA = "srs_cfg:trumedia_enable=1";
    private static final String DTS_MODE_PARA = "srs_cfg:trumedia_enable";
    private static final String DTS_MODE_PRESTATE = "persist.sys.dts.prestate";
    private static final int DTS_OFF = 0;
    private static final int DTS_ON = 3;
    /* access modifiers changed from: private */
    public static final boolean DTS_SOUND_EFFECTS_SUPPORT = SystemProperties.getBoolean("ro.config.hw_dts", false);
    private static final int EFFECT_PARAM_EFF_ENAB = 0;
    private static final int EFFECT_PARAM_EFF_PROF = 167772160;
    private static final int EFFECT_SUPPORT_DEVICE = 603980172;
    private static final UUID EFFECT_TYPE_DS = UUID.fromString("9d4921da-8225-4f29-aefa-39537a04bcaa");
    private static final UUID EFFECT_TYPE_NULL = UUID.fromString("ec7178ec-e5e1-4432-a3f4-4657e6795210");
    private static final String EVENTNAME = "event name";
    private static final String[] FORBIDDEN_SOUND_EFFECT_WHITE_LIST = {"com.jawbone.up", "com.codoon.gps", "com.lakala.cardwatch", "com.hoolai.magic", "com.android.bankabc", "com.icbc", "com.icbc.wapc", "com.chinamworld.klb", "com.yitong.bbw.mbank.android", "com.chinamworld.bocmbci", "com.cloudcore.emobile.szrcb", "com.chinamworld.main", "com.nbbank", "com.cmb.ubank.UBUI", "com.nxy.mobilebank.ln", "cn.com.cmbc.newmbank"};
    private static final int GLOBAL_SESSION_ID = 0;
    private static final String HIDE_HIRES_ICON = "huawei.intent.action.hideHiResIcon";
    private static final String HIRES_REPORT_FLAG = "typec_need_show_hires";
    private static final String HS_NO_CHARGE_OFF = "hs_no_charge=off";
    private static final String HS_NO_CHARGE_ON = "hs_no_charge=on";
    private static final int HW_DOBBLY_SOUND_EFFECT_BIT = 4;
    private static final int HW_KARAOKE_EFFECT_BIT = 2;
    private static final boolean HW_KARAOKE_EFFECT_ENABLED = ((SystemProperties.getInt("ro.config.hw_media_flags", 0) & 2) != 0);
    private static final boolean HW_KARAOKE_THIRD_ENABLED = SystemProperties.getBoolean("ro.config.third_hw_karaoke", true);
    private static final String ISONTOP = "isOnTop";
    /* access modifiers changed from: private */
    public static final boolean IS_SUPPORT_SLIDE = ((SystemProperties.getInt("ro.config.hw_hall_prop", 0) & 1) != 0);
    private static final int MSG_CONNECT_DOLBY_SERVICE = 93;
    private static final int MSG_RECORD_ACTIVE = 61;
    private static final int MSG_SEND_DOLBYUPDATE_BROADCAST = 94;
    private static final int MSG_SET_DEVICE_STATE = 97;
    private static final int MSG_SET_SOUND_EFFECT_STATE = 95;
    private static final int MSG_START_DOLBY_DMS = 96;
    private static final int MSG_START_DOLBY_DMS_DELAY = 200;
    private static final String NODE_ATTR_PACKAGE = "package";
    private static final String NODE_WHITEAPP = "whiteapp";
    private static final String PACKAGENAME = "packageName";
    private static final String PACKAGE_PACKAGEINSTALLER = "com.android.packageinstaller";
    private static final String PERMISSION_DEVICE_OUT_USB_DEVICE_EXTEND = "huawei.permission.OUT_USB_DEVICE_EXTEND";
    private static final String PERMISSION_HIRES_CHANGE = "huawei.permission.HIRES_ICON_CHANGE_ACTION";
    private static final String PERMISSION_KILLED_APP_FOR_KARAOKE = "huawei.permission.APP_KILLED_FOR_KARAOKE_ACTION";
    private static final String PERMISSION_SEND_AUDIO_RECORD_STATE = "com.huawei.permission.AUDIO_RECORD_STATE_CHANGED_ACTION";
    private static final String PERMISSION_START_APP_FOR_KARAOKE = "com.huawei.permission.ACTIVITY_STARTING_FOR_KARAOKE_ACTION";
    private static final String PERMISSION_SWS_EQ = "android.permission.SWS_EQ";
    private static final String[] RECORD_ACTIVE_APP_LIST = {"com.realvnc.android.remote"};
    private static final String[] RECORD_REQUEST_APP_LIST = {"com.google.android", "com.google.android.googlequicksearchbox:search", "com.google.android.googlequicksearchbox:interactor"};
    private static final int RECORD_TYPE_OTHER = 536870912;
    private static final int RECORD_TYPE_SYSTEM = 1073741824;
    private static final String RESERVED = "reserved";
    private static final String RESTORE = "restore";
    private static final int SENDMSG_NOOP = 1;
    private static final int SENDMSG_QUEUE = 2;
    private static final int SENDMSG_REPLACE = 0;
    private static final String SHOW_HIRES_ICON = "huawei.intent.action.showHiResIcon";
    private static final int SHOW_OR_HIDE_HIRES = 15;
    private static final int SHOW_OR_HIDE_HIRES_DELAY = 500;
    private static final boolean SOUND_EFFECTS_SUPPORT = (DTS_SOUND_EFFECTS_SUPPORT || SWS_SOUND_EFFECTS_SUPPORT || DOLBY_SOUND_EFFECTS_SUPPORT);
    private static final int SOUND_EFFECT_CLOSE = 1;
    private static final int SOUND_EFFECT_OPEN = 2;
    private static final String SWS_MODE_OFF_PARA = "HIFIPARA=STEREOWIDEN_Enable=false";
    private static final String SWS_MODE_ON_PARA = "HIFIPARA=STEREOWIDEN_Enable=true";
    private static final String SWS_MODE_PARA = "HIFIPARA=STEREOWIDEN_Enable";
    private static final String SWS_MODE_PRESTATE = "persist.sys.sws.prestate";
    private static final int SWS_OFF = 0;
    private static final int SWS_ON = 3;
    private static final int SWS_SCENE_AUDIO = 0;
    private static final int SWS_SCENE_VIDEO = 1;
    /* access modifiers changed from: private */
    public static final boolean SWS_SOUND_EFFECTS_SUPPORT = SystemProperties.getBoolean("ro.config.hw_sws", false);
    private static final String SWS_VERSION = SystemProperties.get("ro.config.sws_version", "sws2");
    private static final String SYSTEMSERVER_START = "com.huawei.systemserver.START";
    private static final String TAG = "HwAudioServiceEx";
    private static final long THREE_SEC_LIMITED = 3000;
    private static final int UNKNOWN_DEVICE = -1;
    private static final int VOLUME_INDEX_UNIT = 10;
    private static byte[] btArrayDsDisable = {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0};
    private static byte[] btArrayDsEnable = {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0};
    private static byte[] btArrayDsGame = {0, 0, 0, 10, 1, 0, 0, 0, 3, 0, 0, 0};
    private static byte[] btArrayDsMovie = {0, 0, 0, 10, 1, 0, 0, 0, 1, 0, 0, 0};
    private static byte[] btArrayDsMusic = {0, 0, 0, 10, 1, 0, 0, 0, 2, 0, 0, 0};
    private static byte[] btArrayDsSmart = {0, 0, 0, 10, 1, 0, 0, 0, 0, 0, 0, 0};
    /* access modifiers changed from: private */
    public static final boolean mIsHuaweiSWS31Config;
    private static final boolean mIsSWSVideoMode = ("sws3".equalsIgnoreCase(SWS_VERSION) || "sws3_1".equalsIgnoreCase(SWS_VERSION) || SystemProperties.getBoolean("ro.config.sws_moviemode", false));
    /* access modifiers changed from: private */
    public static final boolean mIsUsbPowercosumeTips = SystemProperties.getBoolean("ro.config.usb_power_tips", false);
    /* access modifiers changed from: private */
    public static int mSwsScene = 0;
    private static final int mUsbSecurityVolumeIndex = SystemProperties.getInt("ro.config.hw.usb_security_volume", 0);
    private static final int propvalue_not_support_slide = 0;
    private static final int propvalue_product_support_slide = 1;
    private static final String receiverName = "audioserver";
    /* access modifiers changed from: private */
    public int PGSdkAppType = -1;
    int PGSdk_flag = 0;
    /* access modifiers changed from: private */
    public IHwCoverManager coverManager;
    private long dialogShowTime = 0;
    /* access modifiers changed from: private */
    public boolean isShowingDialog = false;
    private BroadcastReceiver mAnalogTypecReceiver = null;
    /* access modifiers changed from: private */
    public IHallCallback.Stub mCallback;
    private final ArrayList<AudioModeClient> mClients = new ArrayList<>();
    private boolean mConcurrentCaptureEnable;
    private ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public final Context mContext;
    private BroadcastReceiver mDigitalTypecReceiver = null;
    /* access modifiers changed from: private */
    public IDms mDms;
    /* access modifiers changed from: private */
    public IHwBinder.DeathRecipient mDmsDeathRecipient = new IHwBinder.DeathRecipient() {
        public void serviceDied(long cookie) {
            if (HwAudioServiceEx.this.mHwAudioHandlerEx != null) {
                Slog.e(HwAudioServiceEx.TAG, "Dolby service has died, try to reconnect 1s later.");
                HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 93, 0, 0, 0, null, 1000);
            }
        }
    };
    private boolean mDmsStarted = false;
    /* access modifiers changed from: private */
    public IDmsCallbacks mDolbyClient = new IDmsCallbacks.Stub() {
        public void onDapParamUpdate(ArrayList<Byte> params) {
            if (params.size() != 0) {
                byte[] buf = new byte[params.size()];
                for (int i = 0; i < buf.length; i++) {
                    buf[i] = params.get(i).byteValue();
                }
                int dlbParam = HwAudioServiceEx.byteArrayToInt32(buf, 0);
                int status = HwAudioServiceEx.byteArrayToInt32(buf, 4);
                Slog.d(HwAudioServiceEx.TAG, "Got dap param update, param = " + dlbParam + " status = " + status);
                if (dlbParam == 0 || dlbParam == HwAudioServiceEx.EFFECT_PARAM_EFF_PROF) {
                    if (!HwAudioServiceEx.this.mSystemReady) {
                        Slog.w(HwAudioServiceEx.TAG, "onDapParamUpdate() called before boot complete");
                    } else {
                        HwAudioServiceEx.sendMsgEx(HwAudioServiceEx.this.mHwAudioHandlerEx, 94, 0, dlbParam, status, null, 1000);
                    }
                }
                if (dlbParam == 0) {
                    int unused = HwAudioServiceEx.this.mDolbyEnable = status;
                    if (HwAudioServiceEx.this.mHeadSetPlugState) {
                        SystemProperties.set(HwAudioServiceEx.DOLBY_MODE_HEADSET_STATE, HwAudioServiceEx.this.mDolbyEnable > 0 ? XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_ : "off");
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mDolbyEnable = 0;
    private int mDtsStatus = 0;
    private AudioEffect mEffect = null;
    /* access modifiers changed from: private */
    public boolean mHeadSetPlugState = false;
    /* access modifiers changed from: private */
    public HwAudioHandlerEx mHwAudioHandlerEx;
    private HwAudioHandlerExThread mHwAudioHandlerExThread;
    private IHwAudioServiceInner mIAsInner = null;
    private boolean mIsAnalogTypecNotifyAllowed = true;
    /* access modifiers changed from: private */
    public boolean mIsAnalogTypecReceiverRegisterd = false;
    private boolean mIsDigitalTypecNotifyAllowed = true;
    private boolean mIsDigitalTypecOn = false;
    /* access modifiers changed from: private */
    public boolean mIsDigitalTypecReceiverRegisterd = false;
    private ArrayList<String> mKaraokeWhiteList = null;
    private Handler mMyHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (AudioSystem.getDeviceConnectionState(8, "") == 1 || AudioSystem.getDeviceConnectionState(4, "") == 1 || AudioSystem.getDeviceConnectionState(67108864, "") == 1 || AudioSystem.getDeviceConnectionState(HwAudioServiceEx.RECORD_TYPE_OTHER, "") == 1) {
                        if (HwAudioServiceEx.DTS_SOUND_EFFECTS_SUPPORT) {
                            String curDTSState = AudioSystem.getParameters(HwAudioServiceEx.DTS_MODE_PARA);
                            if (curDTSState != null && curDTSState.contains(HwAudioServiceEx.DTS_MODE_ON_PARA)) {
                                AudioSystem.setParameters(HwAudioServiceEx.DTS_MODE_OFF_PARA);
                                SystemProperties.set(HwAudioServiceEx.DTS_MODE_PRESTATE, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                                if (HwAudioServiceEx.DEBUG) {
                                    Slog.i(HwAudioServiceEx.TAG, "Cur DTS mode = " + curDTSState + " force set DTS off");
                                }
                            }
                        }
                        if (HwAudioServiceEx.SWS_SOUND_EFFECTS_SUPPORT && HwAudioServiceEx.mIsHuaweiSWS31Config) {
                            String curSWSState = AudioSystem.getParameters(HwAudioServiceEx.SWS_MODE_PARA);
                            if (curSWSState != null && curSWSState.contains(HwAudioServiceEx.SWS_MODE_ON_PARA)) {
                                AudioSystem.setParameters(HwAudioServiceEx.SWS_MODE_OFF_PARA);
                                SystemProperties.set(HwAudioServiceEx.SWS_MODE_PRESTATE, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                                if (HwAudioServiceEx.DEBUG) {
                                    Slog.i(HwAudioServiceEx.TAG, "Cur SWS mode = " + curSWSState + " force set SWS off");
                                }
                            }
                        }
                        if (HwAudioServiceEx.DOLBY_SOUND_EFFECTS_SUPPORT && HwAudioServiceEx.this.mDolbyEnable > 0) {
                            SystemProperties.set(HwAudioServiceEx.DOLBY_MODE_PRESTATE, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                            HwAudioServiceEx.this.setDolbyEnable(false);
                            return;
                        }
                        return;
                    }
                    return;
                case 2:
                    if (HwAudioServiceEx.DTS_SOUND_EFFECTS_SUPPORT) {
                        String preDTSState = SystemProperties.get(HwAudioServiceEx.DTS_MODE_PRESTATE, "unknown");
                        if (preDTSState != null && preDTSState.equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                            AudioSystem.setParameters(HwAudioServiceEx.DTS_MODE_ON_PARA);
                            SystemProperties.set(HwAudioServiceEx.DTS_MODE_PRESTATE, "unknown");
                            if (HwAudioServiceEx.DEBUG) {
                                Slog.i(HwAudioServiceEx.TAG, "set DTS on");
                            }
                        }
                    }
                    if (HwAudioServiceEx.SWS_SOUND_EFFECTS_SUPPORT && HwAudioServiceEx.mIsHuaweiSWS31Config) {
                        String preSWSState = SystemProperties.get(HwAudioServiceEx.SWS_MODE_PRESTATE, "unknown");
                        if (preSWSState != null && preSWSState.equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                            AudioSystem.setParameters(HwAudioServiceEx.SWS_MODE_ON_PARA);
                            SystemProperties.set(HwAudioServiceEx.SWS_MODE_PRESTATE, "unknown");
                            if (HwAudioServiceEx.DEBUG) {
                                Slog.i(HwAudioServiceEx.TAG, "set SWS on");
                            }
                        }
                    }
                    if (HwAudioServiceEx.DOLBY_SOUND_EFFECTS_SUPPORT) {
                        String preDolbyState = SystemProperties.get(HwAudioServiceEx.DOLBY_MODE_PRESTATE, "unknown");
                        if (preDolbyState != null && preDolbyState.equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                            HwAudioServiceEx.this.setDolbyEnable(true);
                            SystemProperties.set(HwAudioServiceEx.DOLBY_MODE_PRESTATE, "unknown");
                            if (HwAudioServiceEx.DEBUG) {
                                Slog.i(HwAudioServiceEx.TAG, "set DOLBY on");
                                return;
                            }
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public NotificationManager mNotificationManager = null;
    /* access modifiers changed from: private */
    public PGSdk mPGSdk = null;
    HashMap<String, Integer> mPackageUidMap = new HashMap<>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.REBOOT") || action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                    if (HwAudioServiceEx.DOLBY_SOUND_EFFECTS_SUPPORT && HwAudioServiceEx.this.mDms == null) {
                        HwAudioServiceEx.this.setDolbyServiceClient();
                    }
                    Slog.i(HwAudioServiceEx.TAG, "Release Dolby service HIDL client");
                    if (!(HwAudioServiceEx.this.mDms == null || HwAudioServiceEx.this.mDolbyClient == null)) {
                        try {
                            HwAudioServiceEx.this.mDms.unregisterClient(HwAudioServiceEx.this.mDolbyClient, 3, HwAudioServiceEx.this.mDolbyClient.hashCode());
                            HwAudioServiceEx.this.mDms.unlinkToDeath(HwAudioServiceEx.this.mDmsDeathRecipient);
                        } catch (RuntimeException e) {
                            Slog.e(HwAudioServiceEx.TAG, "Release Dolby RuntimeException");
                        } catch (Exception e2) {
                            Slog.e(HwAudioServiceEx.TAG, "Release Dolby error");
                        }
                    }
                    SystemProperties.set(HwAudioServiceEx.BOOT_VOLUME_PROPERTY, Integer.toString(AudioSystem.getStreamVolumeIndex(7, 2)));
                } else if (HwAudioServiceEx.IS_SUPPORT_SLIDE && action.equals(HwAudioServiceEx.SYSTEMSERVER_START)) {
                    boolean ret = HwAudioServiceEx.this.coverManager.registerHallCallback(HwAudioServiceEx.receiverName, 1, HwAudioServiceEx.this.mCallback);
                    Slog.d(HwAudioServiceEx.TAG, "registerHallCallback return " + ret);
                    if (2 == HwAudioServiceEx.this.coverManager.getHallState(1)) {
                        AudioSystem.setParameters("action_slide=true");
                        Slog.i(HwAudioServiceEx.TAG, "originSlideOnStatus True");
                    } else if (HwAudioServiceEx.this.coverManager.getHallState(1) == 0) {
                        AudioSystem.setParameters("action_slide=false");
                        Slog.i(HwAudioServiceEx.TAG, "originSlideOnStatus false");
                    } else {
                        Slog.e(HwAudioServiceEx.TAG, "no support hall");
                    }
                }
            }
        }
    };
    private String mStartedPackageName = null;
    /* access modifiers changed from: private */
    public PGSdk.Sink mStateRecognitionListener = new PGSdk.Sink() {
        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            if (pid != Process.myPid() && stateType == 2) {
                if (eventType == 1) {
                    int unused = HwAudioServiceEx.this.PGSdkAppType = HwAudioServiceEx.this.getAppTypeForVBR(uid);
                    Slog.i(HwAudioServiceEx.TAG, "VBR HwAudioService:music app enter " + HwAudioServiceEx.this.PGSdkAppType);
                    if (HwAudioServiceEx.this.PGSdkAppType == 12) {
                        AudioSystem.setParameters("VBRMuiscState=enter");
                    } else if (HwAudioServiceEx.this.PGSdkAppType == 11) {
                        AudioSystem.setParameters("VBRIMState=enter");
                    }
                } else if (eventType == 2) {
                    int unused2 = HwAudioServiceEx.this.PGSdkAppType = HwAudioServiceEx.this.getAppTypeForVBR(uid);
                    Slog.i(HwAudioServiceEx.TAG, "VBR HwAudioService:music app exit " + HwAudioServiceEx.this.PGSdkAppType);
                    if (HwAudioServiceEx.this.PGSdkAppType == 12) {
                        AudioSystem.setParameters("VBRMuiscState=exit");
                    } else if (HwAudioServiceEx.this.PGSdkAppType == 11) {
                        AudioSystem.setParameters("VBRIMState=exit");
                    }
                }
            }
        }
    };
    private int mSupportDevcieRef = 0;
    private int mSwsStatus = 0;
    /* access modifiers changed from: private */
    public boolean mSystemReady;

    private static final class AudioModeClient implements IBinder.DeathRecipient {
        static HwAudioServiceEx sHwAudioService;
        final IAudioModeDispatcher mDispatcherCb;

        AudioModeClient(IAudioModeDispatcher pcdb) {
            this.mDispatcherCb = pcdb;
        }

        public void binderDied() {
            Log.w(HwAudioServiceEx.TAG, "client died");
            sHwAudioService.unregisterAudioModeCallback(this.mDispatcherCb);
        }

        /* access modifiers changed from: package-private */
        public boolean init() {
            Log.w(HwAudioServiceEx.TAG, "client init");
            try {
                this.mDispatcherCb.asBinder().linkToDeath(this, 0);
                return true;
            } catch (RemoteException e) {
                Log.w(HwAudioServiceEx.TAG, "Could not link to client death");
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void release() {
            Log.w(HwAudioServiceEx.TAG, "client release");
            this.mDispatcherCb.asBinder().unlinkToDeath(this, 0);
        }
    }

    private class HwAudioHandlerEx extends Handler {
        private HwAudioHandlerEx() {
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 15) {
                boolean needReportHiRes = "true".equals(AudioSystem.getParameters(HwAudioServiceEx.HIRES_REPORT_FLAG));
                Slog.i(HwAudioServiceEx.TAG, "check HiRes Icon " + needReportHiRes);
                if (HwAudioServiceEx.this.mPGSdk == null) {
                    PGSdk unused = HwAudioServiceEx.this.mPGSdk = PGSdk.getInstance();
                    if (HwAudioServiceEx.this.mPGSdk != null) {
                        Slog.i(HwAudioServiceEx.TAG, "VBR getInstance ok!");
                        HwAudioServiceEx.this.PGSdk_flag = 1;
                        try {
                            HwAudioServiceEx.this.mPGSdk.enableStateEvent(HwAudioServiceEx.this.mStateRecognitionListener, 1);
                            HwAudioServiceEx.this.mPGSdk.enableStateEvent(HwAudioServiceEx.this.mStateRecognitionListener, 2);
                        } catch (RemoteException e) {
                            Slog.i(HwAudioServiceEx.TAG, "VBR PG Exception e: initialize pgdskd error!");
                            HwAudioServiceEx.this.PGSdk_flag = 0;
                        }
                    } else {
                        Slog.i(HwAudioServiceEx.TAG, "VBR getInstance fails!");
                        HwAudioServiceEx.this.PGSdk_flag = 2;
                    }
                }
                if (needReportHiRes) {
                    HwAudioServiceEx.this.broadcastHiresIntent(true);
                } else {
                    HwAudioServiceEx.this.broadcastHiresIntent(false);
                }
            } else if (i != 61) {
                switch (i) {
                    case 93:
                        HwAudioServiceEx.this.setDolbyServiceClient();
                        break;
                    case 94:
                        HwAudioServiceEx.this.sendDolbyUpdateBroadcast(msg.arg1, msg.arg2);
                        break;
                    case 95:
                        Bundle b = (Bundle) msg.obj;
                        HwAudioServiceEx.this.setSoundEffectStateAsynch(b.getBoolean(HwAudioServiceEx.RESTORE), b.getString("packageName"), b.getBoolean(HwAudioServiceEx.ISONTOP), b.getString(HwAudioServiceEx.RESERVED));
                        break;
                    case 96:
                        boolean musicIsActive = AudioSystem.isStreamActive(3, 0);
                        boolean ringIsActive = AudioSystem.isStreamActive(2, 0);
                        boolean alarmIsActive = AudioSystem.isStreamActive(4, 0);
                        Slog.i(HwAudioServiceEx.TAG, "restart dms musicIsActive : " + musicIsActive + ", ringIsActive : " + ringIsActive + ", alarmIsActive : " + alarmIsActive);
                        if (musicIsActive || ringIsActive || alarmIsActive) {
                            HwAudioServiceEx.this.setDolbyStatus();
                            break;
                        } else {
                            return;
                        }
                    case HwAudioServiceEx.MSG_SET_DEVICE_STATE /*97*/:
                        HwAudioServiceEx.this.checkAndSetSoundEffectState(msg.arg1, msg.arg2);
                        break;
                    default:
                        Slog.i(HwAudioServiceEx.TAG, "HwAudioHandlerEx receive unknown msg");
                        break;
                }
            } else if (HwAudioServiceEx.this.isShowingDialog) {
                Slog.i(HwAudioServiceEx.TAG, "MSG_RECORD_ACTIVE should not show record warn dialog");
            } else {
                HwAudioServiceEx.this.showRecordWarnDialog(msg.arg1, msg.arg2);
            }
        }
    }

    private class HwAudioHandlerExThread extends Thread {
        HwAudioHandlerExThread() {
        }

        public void run() {
            Looper.prepare();
            synchronized (HwAudioServiceEx.this) {
                HwAudioHandlerEx unused = HwAudioServiceEx.this.mHwAudioHandlerEx = new HwAudioHandlerEx();
                HwAudioServiceEx.this.notify();
            }
            Looper.loop();
        }
    }

    private static class IPGPClient implements IPGPlugCallbacks {
        private PGPlug mPGPlug = new PGPlug(this, HwAudioServiceEx.TAG);

        public IPGPClient() {
            new Thread(this.mPGPlug, HwAudioServiceEx.TAG).start();
        }

        public void onDaemonConnected() {
            Slog.i(HwAudioServiceEx.TAG, "HwAudioService:IPGPClient connected success!");
        }

        public boolean onEvent(int actionID, String value) {
            if (1 != PGAction.checkActionType(actionID)) {
                if (HwAudioServiceEx.DEBUG) {
                    Slog.i(HwAudioServiceEx.TAG, "HwAudioService:Filter application event id : " + actionID);
                }
                return true;
            }
            int subFlag = PGAction.checkActionFlag(actionID);
            if (HwAudioServiceEx.DEBUG) {
                Slog.i(HwAudioServiceEx.TAG, "HwAudioService:IPGP onEvent actionID=" + actionID + ", value=" + value + ",  subFlag=" + subFlag);
            }
            if (subFlag != 3) {
                if (HwAudioServiceEx.DEBUG) {
                    Slog.i(HwAudioServiceEx.TAG, "Not used non-parent scene , ignore it");
                }
                return true;
            }
            if (actionID == 10009) {
                if (HwAudioServiceEx.mSwsScene == 0) {
                    int unused = HwAudioServiceEx.mSwsScene = 1;
                    Slog.i(HwAudioServiceEx.TAG, "HwAudioService:Video_Front");
                    AudioSystem.setParameters("IPGPMode=video");
                }
            } else if (HwAudioServiceEx.mSwsScene != 0) {
                int unused2 = HwAudioServiceEx.mSwsScene = 0;
                Slog.i(HwAudioServiceEx.TAG, "HwAudioService:Video_Not_Front");
                AudioSystem.setParameters("IPGPMode=audio");
            }
            return true;
        }

        public void onConnectedTimeout() {
            Slog.e(HwAudioServiceEx.TAG, "HwAudioService:Client connect timeout!");
        }
    }

    private static class TipRes {
        public String mChannelName = null;
        public String mContent = null;
        public Context mContext = null;
        public String mTip = null;
        public String mTitle = null;
        public int mTypecNotificationId = 0;

        public TipRes(int device, Context context) {
            this.mContext = context;
            switch (device) {
                case 1:
                    this.mChannelName = this.mContext.getResources().getString(33685982);
                    this.mTitle = this.mContext.getResources().getString(33685983);
                    this.mContent = this.mContext.getResources().getString(33685984);
                    this.mTip = this.mContext.getResources().getString(33685985);
                    this.mTypecNotificationId = 1;
                    return;
                case 2:
                    this.mChannelName = this.mContext.getResources().getString(33685982);
                    if (HwAudioServiceEx.mIsUsbPowercosumeTips) {
                        Slog.i(HwAudioServiceEx.TAG, "Tips for pad, mIsUsbPowercosumeTips = " + HwAudioServiceEx.mIsUsbPowercosumeTips);
                        this.mTitle = this.mContext.getResources().getString(33686159);
                        this.mContent = this.mContext.getResources().getString(33686160);
                    } else {
                        this.mTitle = this.mContext.getResources().getString(33686004);
                        this.mContent = this.mContext.getResources().getString(33686005);
                    }
                    this.mTip = this.mContext.getResources().getString(33685985);
                    this.mTypecNotificationId = 2;
                    return;
                default:
                    Slog.e(HwAudioServiceEx.TAG, "TipRes constructor unKnown device");
                    return;
            }
        }
    }

    static {
        boolean z = true;
        if ("sws2".equalsIgnoreCase(SWS_VERSION) || "sws3".equalsIgnoreCase(SWS_VERSION)) {
            z = false;
        }
        mIsHuaweiSWS31Config = z;
    }

    public HwAudioServiceEx(IHwAudioServiceInner ias, Context context) {
        this.mIAsInner = ias;
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.REBOOT");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction(SYSTEMSERVER_START);
        context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
        if (IS_SUPPORT_SLIDE) {
            this.coverManager = HwFrameworkFactory.getCoverManager();
            this.mCallback = new IHallCallback.Stub() {
                public void onStateChange(HallState hallState) {
                    Slog.d(HwAudioServiceEx.TAG, "HallState=" + hallState);
                    if (hallState.state == 2) {
                        AudioSystem.setParameters("VOICE_SLIDE_STATUS=open");
                        Slog.i(HwAudioServiceEx.TAG, "set VOICE_SLIDE_STATUS open");
                    } else if (hallState.state == 0) {
                        AudioSystem.setParameters("VOICE_SLIDE_STATUS=close");
                        Slog.i(HwAudioServiceEx.TAG, "set VOICE_SLIDE_STATUS close");
                    } else {
                        Slog.e(HwAudioServiceEx.TAG, "hallState is not recognized");
                    }
                }
            };
        }
        createHwAudioHandlerExThread();
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        if (SWS_SOUND_EFFECTS_SUPPORT && mIsSWSVideoMode) {
            Slog.i(TAG, "HwAudioService: Start SWS3.0 IPGPClient.");
            new IPGPClient();
        }
        AudioModeClient.sHwAudioService = this;
    }

    private void createHwAudioHandlerExThread() {
        this.mHwAudioHandlerExThread = new HwAudioHandlerExThread();
        this.mHwAudioHandlerExThread.start();
        waitForHwAudioHandlerExCreation();
    }

    private void waitForHwAudioHandlerExCreation() {
        synchronized (this) {
            while (this.mHwAudioHandlerEx == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Slog.e(TAG, "Interrupted while waiting on HwAudioHandlerEx.");
                }
            }
        }
    }

    private void readPersistedSettingsEx(ContentResolver cr) {
        if (SOUND_EFFECTS_SUPPORT) {
            getEffectsState(cr);
        }
    }

    private void setAudioSystemParameters() {
        if (SOUND_EFFECTS_SUPPORT) {
            setEffectsState();
        }
    }

    public void setSystemReady() {
        this.mSystemReady = true;
        try {
            Slog.i(TAG, "try set drm hidl stop prop");
            SystemProperties.set("odm.drm.stop", "true");
            SystemProperties.set("odm.drm.stop", "false");
            this.mConcurrentCaptureEnable = SystemProperties.getBoolean(CONCURRENT_CAPTURE_PROPERTY, false);
        } catch (Exception e) {
            Slog.e(TAG, "set drm stop prop fail");
        }
    }

    public void processAudioServerRestart() {
        Slog.i(TAG, "audioserver restart, resume audio settings and parameters");
        readPersistedSettingsEx(this.mContentResolver);
        setAudioSystemParameters();
        setKaraokeWhiteListUID(this.mKaraokeWhiteList);
    }

    private Intent creatIntentByMic(boolean isMic) {
        Intent intent = new Intent();
        intent.setAction(ACTION_DEVICE_OUT_USB_DEVICE_EXTEND);
        intent.putExtra("microphone", isMic);
        return intent;
    }

    private Intent getNewMicIconIntent() {
        if (this.mIAsInner.isConnectedHeadSetEx()) {
            return creatIntentByMic(true);
        }
        if (this.mIAsInner.isConnectedHeadPhoneEx()) {
            return creatIntentByMic(false);
        }
        if (!this.mIAsInner.isConnectedUsbOutDeviceEx()) {
            return null;
        }
        if (this.mIAsInner.isConnectedUsbInDeviceEx()) {
            return creatIntentByMic(true);
        }
        return creatIntentByMic(false);
    }

    private void sendNewMicIconIntent(Intent intent) {
        if (intent == null) {
            Slog.i(TAG, "intent is null");
            return;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManagerNative.broadcastStickyIntent(intent, PERMISSION_DEVICE_OUT_USB_DEVICE_EXTEND, -1);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void updateMicIcon() {
        sendNewMicIconIntent(getNewMicIconIntent());
    }

    private int identifyAudioDevice(int type) {
        int device = -1;
        if ((ANALOG_TYPEC_DEVICES & type) != 0) {
            if ("true".equals(AudioSystem.getParameters(ANALOG_TYPEC_FLAG))) {
                device = 1;
            }
        } else if (type == 67108864) {
            device = 2;
        }
        Slog.e(TAG, "identifyAudioDevice return device: " + device);
        return device;
    }

    /* access modifiers changed from: private */
    public void broadcastHiresIntent(boolean showHiResIcon) {
        Intent intent = new Intent();
        intent.setAction(showHiResIcon ? SHOW_HIRES_ICON : HIDE_HIRES_ICON);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION_HIRES_CHANGE);
    }

    public void notifyHiResIcon(int event) {
        if (!this.mIsDigitalTypecOn) {
            return;
        }
        if (event == 2 || event == 3 || event == 4) {
            this.mHwAudioHandlerEx.sendEmptyMessageDelayed(15, 500);
        }
    }

    public void notifyStartDolbyDms(int event) {
        if (!this.mDmsStarted && event == 2) {
            this.mHwAudioHandlerEx.sendEmptyMessageDelayed(96, 200);
        }
    }

    public void updateTypeCNotify(int type, int state) {
        int recognizedDevice = identifyAudioDevice(type);
        Slog.i(TAG, "updateTypeCNotify recognizedDevice: " + recognizedDevice);
        if (state != 0) {
            this.mHwAudioHandlerEx.sendEmptyMessageDelayed(15, 1000);
            switch (recognizedDevice) {
                case 1:
                    if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "typec_analog_enabled", 1, -2) == 1) {
                        notifyTypecConnected(1);
                        if (!this.mIsAnalogTypecReceiverRegisterd) {
                            registerTypecReceiver(recognizedDevice);
                            this.mIsAnalogTypecReceiverRegisterd = true;
                        }
                    }
                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "typec_digital_enabled", 1, -2);
                    return;
                case 2:
                    this.mIsDigitalTypecOn = true;
                    boolean needTip = needTipForDigitalTypeC();
                    Slog.i(TAG, "updateTypeCNotify needTip: " + needTip);
                    if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "typec_digital_enabled", 1, -2) == 1 && needTip) {
                        notifyTypecConnected(2);
                        if (!this.mIsDigitalTypecReceiverRegisterd) {
                            registerTypecReceiver(recognizedDevice);
                            this.mIsDigitalTypecReceiverRegisterd = true;
                        }
                    }
                    Settings.System.putIntForUser(this.mContext.getContentResolver(), "typec_analog_enabled", 1, -2);
                    return;
                default:
                    Slog.e(TAG, "updateTypeCNotify unknown device: " + recognizedDevice);
                    return;
            }
        } else {
            if (recognizedDevice == 2) {
                this.mIsDigitalTypecOn = false;
            }
            broadcastHiresIntent(false);
            Slog.i(TAG, "updateTypeCNotify plug out device: " + recognizedDevice);
            dismissNotification(recognizedDevice);
        }
    }

    private boolean needTipForDigitalTypeC() {
        boolean isGoodTypec = "true".equals(AudioSystem.getParameters(DIGITAL_TYPEC_FLAG));
        boolean isNeedTip = "true".equals(AudioSystem.getParameters(DIGITAL_TYPEC_REPORT_FLAG));
        if (mIsUsbPowercosumeTips) {
            return isNeedTip;
        }
        return !isGoodTypec && isNeedTip;
    }

    private void notifyTypecConnected(int device) {
        if (this.mContext == null) {
            Slog.i(TAG, "context is null");
            return;
        }
        Slog.i(TAG, "notifyTypecConnected device: " + device);
        PendingIntent pi = getNeverNotifyIntent(device);
        TipRes tipRes = new TipRes(device, this.mContext);
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mNotificationManager.createNotificationChannel(new NotificationChannel(tipRes.mChannelName, tipRes.mChannelName, 1));
        this.mNotificationManager.notifyAsUser(TAG, tipRes.mTypecNotificationId, new Notification.Builder(this.mContext).setSmallIcon(33751741).setContentTitle(tipRes.mTitle).setContentText(tipRes.mContent).setStyle(new Notification.BigTextStyle().bigText(tipRes.mContent)).addAction(0, tipRes.mTip, pi).setAutoCancel(true).setChannelId(tipRes.mChannelName).setDefaults(-1).build(), UserHandle.ALL);
    }

    private PendingIntent getNeverNotifyIntent(int device) {
        Intent intent = null;
        switch (device) {
            case 1:
                intent = new Intent(ACTION_ANALOG_TYPEC_NOTIFY);
                break;
            case 2:
                intent = new Intent(ACTION_DIGITAL_TYPEC_NOTIFY);
                break;
            default:
                Slog.e(TAG, "getNeverNotifyIntentAndUpdateTextId unKnown device");
                break;
        }
        return PendingIntent.getBroadcast(this.mContext, 0, intent, 268435456);
    }

    private void dismissNotification(int device) {
        if (this.mNotificationManager != null) {
            switch (device) {
                case 1:
                    this.mNotificationManager.cancel(TAG, 1);
                    break;
                case 2:
                    this.mNotificationManager.cancel(TAG, 2);
                    break;
                default:
                    Slog.e(TAG, "dismissNotification unKnown device: " + device);
                    break;
            }
        }
    }

    private String getTypecAction(int device) {
        switch (device) {
            case 1:
                return ACTION_ANALOG_TYPEC_NOTIFY;
            case 2:
                return ACTION_DIGITAL_TYPEC_NOTIFY;
            default:
                Slog.e(TAG, "getTypecAction unKnown device: " + device);
                return null;
        }
    }

    private void registerTypecReceiver(int device) {
        Slog.i(TAG, "registerTypecReceiver device: " + device);
        BroadcastReceiver typecReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    String action = intent.getAction();
                    char c = 65535;
                    int hashCode = action.hashCode();
                    if (hashCode != -2146785072) {
                        if (hashCode == 1549278377 && action.equals(HwAudioServiceEx.ACTION_ANALOG_TYPEC_NOTIFY)) {
                            c = 0;
                        }
                    } else if (action.equals(HwAudioServiceEx.ACTION_DIGITAL_TYPEC_NOTIFY)) {
                        c = 1;
                    }
                    switch (c) {
                        case 0:
                            Settings.System.putIntForUser(HwAudioServiceEx.this.mContext.getContentResolver(), "typec_analog_enabled", 0, -2);
                            HwAudioServiceEx.this.mNotificationManager.cancel(HwAudioServiceEx.TAG, 1);
                            boolean unused = HwAudioServiceEx.this.mIsAnalogTypecReceiverRegisterd = false;
                            break;
                        case 1:
                            Settings.System.putIntForUser(HwAudioServiceEx.this.mContext.getContentResolver(), "typec_digital_enabled", 0, -2);
                            HwAudioServiceEx.this.mNotificationManager.cancel(HwAudioServiceEx.TAG, 2);
                            boolean unused2 = HwAudioServiceEx.this.mIsDigitalTypecReceiverRegisterd = false;
                            break;
                        default:
                            Slog.e(HwAudioServiceEx.TAG, "registerTypecReceiver unKnown action");
                            break;
                    }
                    NotificationManager unused3 = HwAudioServiceEx.this.mNotificationManager = null;
                    if (HwAudioServiceEx.this.mContext != null) {
                        HwAudioServiceEx.this.mContext.unregisterReceiver(this);
                    }
                }
            }
        };
        switch (device) {
            case 1:
                this.mAnalogTypecReceiver = typecReceiver;
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(getTypecAction(device));
                this.mContext.registerReceiver(this.mAnalogTypecReceiver, intentFilter);
                return;
            case 2:
                this.mDigitalTypecReceiver = typecReceiver;
                IntentFilter intentFilter2 = new IntentFilter();
                intentFilter2.addAction(getTypecAction(device));
                this.mContext.registerReceiver(this.mDigitalTypecReceiver, intentFilter2);
                return;
            default:
                Slog.e(TAG, "dismissNotification unKnown device: " + device);
                return;
        }
    }

    /* access modifiers changed from: private */
    public static void sendMsgEx(Handler handler, int msg, int existingMsgPolicy, int arg1, int arg2, Object obj, int delay) {
        if (existingMsgPolicy == 0) {
            handler.removeMessages(msg);
        } else if (existingMsgPolicy == 1 && handler.hasMessages(msg)) {
            return;
        }
        handler.sendMessageAtTime(handler.obtainMessage(msg, arg1, arg2, obj), SystemClock.uptimeMillis() + ((long) delay));
    }

    public boolean checkRecordActive(int requestRecordPid) {
        boolean isRecordOccupied;
        if (TextUtils.isEmpty(AudioSystem.getParameters("active_record_pid"))) {
            return false;
        }
        String requestPkgName = getPackageNameByPidEx(requestRecordPid);
        int activeRecordPid = Integer.parseInt(AudioSystem.getParameters("active_record_pid"));
        boolean isSourceActive = AudioSystem.isSourceActive(1999) || AudioSystem.isSourceActive(8);
        Slog.i(TAG, "AudioException checkRecordActive requestRecordPid = " + requestRecordPid + ", activeRecordPid = " + activeRecordPid);
        if (activeRecordPid == -1 || requestRecordPid == activeRecordPid || isSourceActive) {
            isRecordOccupied = false;
        } else {
            isRecordOccupied = true;
        }
        if (isRecordOccupied) {
            if (!isInRequestAppList(requestPkgName)) {
                String activePkgName = getPackageNameByPidEx(activeRecordPid);
                if (isInActiveAppList(activePkgName) || isConcurrentAllow(requestPkgName, activePkgName)) {
                    return isRecordOccupied;
                }
                sendMsgEx(this.mHwAudioHandlerEx, 61, 2, requestRecordPid, activeRecordPid, null, 0);
            }
        } else if (requestPkgName != null && !requestPkgName.equals("")) {
            AudioSystem.setParameters("RecordCallingAppName=" + requestPkgName);
        }
        return isRecordOccupied;
    }

    private String getPackageNameByPid(int pid) {
        if (pid <= 0) {
            return null;
        }
        ActivityManager activityManager = (ActivityManager) this.mContext.getSystemService("activity");
        if (activityManager == null) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return null;
        }
        String packageName = null;
        Iterator<ActivityManager.RunningAppProcessInfo> it = appProcesses.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ActivityManager.RunningAppProcessInfo appProcess = it.next();
            if (appProcess.pid == pid) {
                packageName = appProcess.processName;
                break;
            }
        }
        int indexProcessFlag = -1;
        if (packageName != null) {
            indexProcessFlag = packageName.indexOf(58);
        }
        return indexProcessFlag > 0 ? packageName.substring(0, indexProcessFlag) : packageName;
    }

    private String getPackageNameByPidEx(int pid) {
        String res = null;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            ActivityManagerNative.getDefault().asBinder().transact(504, data, reply, 0);
            reply.readException();
            res = reply.readString();
            data.recycle();
            reply.recycle();
            return res;
        } catch (Exception e) {
            Slog.e(TAG, "getPackageNameForPid " + pid, e);
            return res;
        }
    }

    private boolean isInRequestAppList(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return true;
        }
        String topPackageName = getTopActivityPackageName();
        if (topPackageName != null && !pkgName.startsWith(topPackageName)) {
            return true;
        }
        boolean isValidPkgName = false;
        int i = 0;
        while (true) {
            if (i >= RECORD_REQUEST_APP_LIST.length) {
                break;
            } else if (pkgName.contains(RECORD_REQUEST_APP_LIST[i])) {
                isValidPkgName = true;
                break;
            } else {
                i++;
            }
        }
        return isValidPkgName;
    }

    private boolean isInActiveAppList(String pkgName) {
        for (String equals : RECORD_ACTIVE_APP_LIST) {
            if (equals.equals(pkgName)) {
                Slog.i(TAG, "isInActiveAppList " + pkgName);
                return true;
            }
        }
        return false;
    }

    private String getTopActivityPackageName() {
        try {
            List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
            if (tasks != null) {
                if (!tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    if (topActivity != null) {
                        return topActivity.getPackageName();
                    }
                    return null;
                }
            }
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "Failure to get topActivity PackageName " + e);
        }
    }

    private String getApplicationLabel(String pkgName) {
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            return packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkgName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Slog.e(TAG, "getApplicationLabel exception", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void showRecordWarnDialog(int requestRecordPid, int activeRecordPid) {
        String requestPkgName = getPackageNameByPidEx(requestRecordPid);
        String requestPkgLabel = getApplicationLabel(requestPkgName);
        String activePkgName = getPackageNameByPidEx(activeRecordPid);
        String activePkgLabel = getApplicationLabel(activePkgName);
        if (activePkgName == null) {
            activePkgName = this.mContext.getString(17039914);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("showRecordWarnDialog activePkgLabel=");
        sb.append(activePkgLabel != null ? activePkgLabel : activePkgName);
        sb.append("requestPkgLabel=");
        sb.append(requestPkgLabel != null ? requestPkgLabel : requestPkgName);
        Slog.i(TAG, sb.toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null));
        String string = this.mContext.getString(33685818);
        Object[] objArr = new Object[2];
        objArr[0] = activePkgLabel != null ? activePkgLabel : activePkgName;
        objArr[1] = requestPkgLabel != null ? requestPkgLabel : requestPkgName;
        builder.setMessage(String.format(string, objArr));
        builder.setCancelable(false);
        builder.setPositiveButton(33685817, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                boolean unused = HwAudioServiceEx.this.isShowingDialog = false;
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setType(2003);
        if (SystemClock.elapsedRealtime() - this.dialogShowTime > 3000 || SystemClock.elapsedRealtime() - this.dialogShowTime < 0) {
            dialog.show();
            this.dialogShowTime = SystemClock.elapsedRealtime();
            this.isShowingDialog = true;
        }
    }

    private void getAppInWhiteBlackList(List<String> whiteAppList) {
        InputStream in = null;
        XmlPullParser xmlParser = null;
        try {
            File configFile = HwCfgFilePolicy.getCfgFile(CONFIG_FILE_WHITE_BLACK_APP, 0);
            if (configFile != null) {
                Slog.v(TAG, "HwCfgFilePolicy getCfgFile not null, path = " + configFile.getPath());
                in = new FileInputStream(configFile.getPath());
                xmlParser = Xml.newPullParser();
                xmlParser.setInput(in, null);
            } else {
                Slog.e(TAG, "HwCfgFilePolicy getCfgFile is null");
            }
            if (xmlParser != null) {
                parseXmlForWhiteBlackList(xmlParser, whiteAppList);
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Slog.e(TAG, "Karaoke IO Close Fail");
                }
            }
        } catch (NoExtAPIException e2) {
            Slog.e(TAG, "Karaoke NoExtAPIException");
            if (in != null) {
                in.close();
            }
        } catch (FileNotFoundException e3) {
            Slog.e(TAG, "Karaoke FileNotFoundException");
            if (in != null) {
                in.close();
            }
        } catch (XmlPullParserException e4) {
            Slog.e(TAG, "Karaoke XmlPullParserException");
            if (in != null) {
                in.close();
            }
        } catch (Exception e5) {
            Slog.e(TAG, "Karaoke getAppInWhiteBlackList Exception ", e5);
            if (in != null) {
                in.close();
            }
        } catch (Throwable th) {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e6) {
                    Slog.e(TAG, "Karaoke IO Close Fail");
                }
            }
            throw th;
        }
    }

    private void parseXmlForWhiteBlackList(XmlPullParser parser, List<String> whiteAppList) {
        while (true) {
            try {
                int next = parser.next();
                int eventType = next;
                if (next == 1) {
                    return;
                }
                if (eventType == 2 && parser.getName().equals(NODE_WHITEAPP)) {
                    String packageName = parser.getAttributeValue(null, "package");
                    if (isValidCharSequence(packageName)) {
                        whiteAppList.add(packageName);
                    }
                }
            } catch (XmlPullParserException e) {
                Slog.e(TAG, "Karaoke XmlPullParserException");
                return;
            } catch (IOException e2) {
                Slog.e(TAG, "Karaoke IOException");
                return;
            }
        }
    }

    public boolean isValidCharSequence(CharSequence charSeq) {
        if (charSeq == null || charSeq.length() == 0) {
            return false;
        }
        return true;
    }

    private void sendStartIntentForKaraoke(String packageName, boolean isOnTop) {
        if (HW_KARAOKE_EFFECT_ENABLED) {
            if (!this.mSystemReady) {
                Slog.e(TAG, "Start Karaoke system is not ready! ");
                return;
            }
            initHwKaraokeWhiteList();
            if (isOnTop && !packageName.equals(this.mStartedPackageName)) {
                this.mStartedPackageName = packageName;
                if (this.mKaraokeWhiteList.contains(packageName)) {
                    startIntentForKaraoke(packageName);
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private static int getCurrentUserId() {
        long ident = Binder.clearCallingIdentity();
        try {
            int i = ActivityManagerNative.getDefault().getCurrentUser().id;
            Binder.restoreCallingIdentity(ident);
            return i;
        } catch (RemoteException e) {
            Slog.w(TAG, "Activity manager not running, nothing we can do assume user 0.");
            Binder.restoreCallingIdentity(ident);
            return 0;
        } catch (Throwable currentUser) {
            Binder.restoreCallingIdentity(ident);
            throw currentUser;
        }
    }

    private int getUidByPkg(String pkgName) {
        int uid = -1;
        if (pkgName == null) {
            return -1;
        }
        try {
            uid = this.mContext.getPackageManager().getPackageUidAsUser(pkgName, getCurrentUserId());
            Slog.i(TAG, "get current user id: " + uid);
        } catch (Exception e) {
            Slog.w(TAG, "not found uid pkgName:" + pkgName);
        }
        return uid;
    }

    public boolean isKaraokeWhiteListApp(String pkgName) {
        Slog.w(TAG, "[audio cdmnr] isKaraokeWhiteListApp start");
        if (pkgName == null || "".equals(pkgName) || this.mKaraokeWhiteList == null) {
            Slog.w(TAG, "[audio cdmnr] isKaraokeWhiteListApp param error");
            return false;
        }
        int arraySize = this.mKaraokeWhiteList.size();
        for (int i = 0; i < arraySize; i++) {
            if (pkgName.equals(this.mKaraokeWhiteList.get(i))) {
                return true;
            }
        }
        Slog.w(TAG, "[audio cdmnr] isKaraokeWhiteListApp end");
        return false;
    }

    public void setKaraokeWhiteAppUIDByPkgName(String pkgName) {
        Slog.w(TAG, "[audio cdmnr] setKaraokeWhiteAppUIDByPkgName start");
        if (pkgName != null && !"".equals(pkgName)) {
            int uid = getUidByPkg(pkgName);
            if (-1 == uid) {
                Slog.w(TAG, "[audio cdmnr] get karaoke uid by package name error");
            } else {
                Slog.w(TAG, "AddKaraokeWhiteUID=" + String.valueOf(uid));
                AudioSystem.setParameters("AddKaraokeWhiteUID=" + String.valueOf(uid));
            }
            Slog.w(TAG, "[audio cdmnr] setKaraokeWhiteAppUIDByPkgName end");
        }
    }

    private void setKaraokeWhiteListUID(ArrayList<String> whiteList) {
        if (whiteList == null) {
            Slog.w(TAG, "[audio cdmnr] karaoke white list is null");
            return;
        }
        int arraySize = whiteList.size();
        for (int i = 0; i < arraySize; i++) {
            setKaraokeWhiteAppUIDByPkgName(whiteList.get(i));
        }
    }

    private void initHwKaraokeWhiteList() {
        if (this.mKaraokeWhiteList == null) {
            this.mKaraokeWhiteList = new ArrayList<>();
            getAppInWhiteBlackList(this.mKaraokeWhiteList);
            Slog.i(TAG, "karaoke white list =" + this.mKaraokeWhiteList.toString());
            setKaraokeWhiteListUID(this.mKaraokeWhiteList);
        }
    }

    public boolean isHwKaraokeEffectEnable(String packageName) {
        if (!HW_KARAOKE_THIRD_ENABLED) {
            Slog.v(TAG, "prop for third apps is closed");
            return false;
        } else if (!HW_KARAOKE_EFFECT_ENABLED) {
            Slog.e(TAG, "karaoke prop do not support");
            return false;
        } else if (!this.mSystemReady) {
            Slog.e(TAG, "Start Karaoke system is not ready! ");
            return false;
        } else {
            initHwKaraokeWhiteList();
            if (this.mKaraokeWhiteList.contains(packageName)) {
                Slog.v(TAG, "app in white list");
                return true;
            }
            Slog.v(TAG, "not in white list");
            return false;
        }
    }

    public void sendAudioRecordStateChangedIntent(String sender, int state, int pid, String packageName) {
        long ident;
        String str = sender;
        int i = state;
        int i2 = pid;
        if (HW_KARAOKE_EFFECT_ENABLED) {
            if (this.mContext.checkCallingPermission("android.permission.RECORD_AUDIO") == -1) {
                Slog.e(TAG, "sendAudioRecordStateChangedIntent dennied from " + packageName);
                return;
            }
            String str2 = packageName;
            if (DEBUG) {
                Slog.i(TAG, "sendAudioRecordStateChangedIntent=" + str + " " + i + " " + i2);
            }
            Intent intent = new Intent();
            intent.setAction(ACTION_SEND_AUDIO_RECORD_STATE);
            intent.addFlags(268435456);
            intent.putExtra("sender", str);
            intent.putExtra("state", i);
            intent.putExtra("packagename", getPackageNameByPid(i2));
            long ident2 = Binder.clearCallingIdentity();
            try {
                long ident3 = ident2;
                try {
                    ActivityManagerNative.getDefault().broadcastIntent(null, intent, null, null, -1, null, null, new String[]{PERMISSION_SEND_AUDIO_RECORD_STATE}, -1, null, false, false, -2);
                    Binder.restoreCallingIdentity(ident3);
                } catch (RemoteException e) {
                    ident = ident3;
                    try {
                        Slog.e(TAG, "sendAudioRecordStateChangedIntent failed: catch RemoteException!");
                        Binder.restoreCallingIdentity(ident);
                    } catch (Throwable th) {
                        th = th;
                        Binder.restoreCallingIdentity(ident);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    ident = ident3;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } catch (RemoteException e2) {
                ident = ident2;
                Slog.e(TAG, "sendAudioRecordStateChangedIntent failed: catch RemoteException!");
                Binder.restoreCallingIdentity(ident);
            } catch (Throwable th3) {
                th = th3;
                ident = ident2;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        }
    }

    private void startIntentForKaraoke(String packageName) {
        long ident;
        String str = packageName;
        Slog.v(TAG, "sendBroadcast : activity starting for karaoke =" + str);
        Intent startBroadcast = new Intent();
        startBroadcast.setAction(ACTION_START_APP_FOR_KARAOKE);
        startBroadcast.addFlags(268435456);
        startBroadcast.putExtra("packagename", str);
        startBroadcast.setPackage("com.huawei.android.karaoke");
        long ident2 = Binder.clearCallingIdentity();
        try {
            long ident3 = ident2;
            Intent intent = startBroadcast;
            try {
                ActivityManagerNative.getDefault().broadcastIntent(null, startBroadcast, null, null, -1, null, null, new String[]{PERMISSION_SEND_AUDIO_RECORD_STATE}, -1, null, false, false, -2);
                Binder.restoreCallingIdentity(ident3);
            } catch (RemoteException e) {
                e = e;
                ident = ident3;
                try {
                    Slog.e(TAG, "Karaoke broadcast fail", e);
                    Binder.restoreCallingIdentity(ident);
                } catch (Throwable th) {
                    th = th;
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                ident = ident3;
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } catch (RemoteException e2) {
            e = e2;
            ident = ident2;
            Intent intent2 = startBroadcast;
            Slog.e(TAG, "Karaoke broadcast fail", e);
            Binder.restoreCallingIdentity(ident);
        } catch (Throwable th3) {
            th = th3;
            ident = ident2;
            Intent intent3 = startBroadcast;
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    private void sendAppKilledIntentForKaraoke(boolean restore, String packageName, boolean isOnTop, String reserved) {
        if (HW_KARAOKE_EFFECT_ENABLED) {
            if (!this.mSystemReady) {
                Slog.e(TAG, "KILLED_APP system is not ready! ");
            } else if (this.mKaraokeWhiteList == null || this.mKaraokeWhiteList.size() < 1) {
                Slog.e(TAG, "Karaoke white list is empty! ");
            } else {
                if (DEBUG) {
                    Slog.i(TAG, "restore:" + restore + " packageName:" + packageName + " Top:" + isOnTop);
                }
                if (this.mKaraokeWhiteList.contains(packageName) || PACKAGE_PACKAGEINSTALLER.equals(packageName)) {
                    Intent intent = new Intent();
                    intent.setAction(ACTION_KILLED_APP_FOR_KARAOKE);
                    intent.putExtra(RESTORE, restore);
                    intent.putExtra("packageName", packageName);
                    intent.putExtra(ISONTOP, isOnTop);
                    intent.putExtra(RESERVED, reserved);
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, PERMISSION_KILLED_APP_FOR_KARAOKE);
                }
            }
        }
    }

    private boolean isScreenOff() {
        PowerManager power = (PowerManager) this.mContext.getSystemService("power");
        if (power != null) {
            return !power.isScreenOn();
        }
        return false;
    }

    private boolean isKeyguardLocked() {
        KeyguardManager keyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        if (keyguard != null) {
            return keyguard.isKeyguardLocked();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public int getAppTypeForVBR(int uid) {
        int type = -1;
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            return -1;
        }
        Slog.i(TAG, "VBR Enter package length " + packages.length);
        int i = 0;
        while (true) {
            if (i >= packages.length) {
                break;
            }
            String packageName = packages[i];
            Slog.i(TAG, "VBR Enter packageName:" + packageName);
            try {
                type = this.mPGSdk.getPkgType(this.mContext, packageName);
                break;
            } catch (RemoteException e) {
                Slog.i(TAG, "VBR getPkgType failed!");
                i++;
            }
        }
        return type;
    }

    private void getEffectsState(ContentResolver contentResolver) {
        if (DTS_SOUND_EFFECTS_SUPPORT) {
            getDtsStatus(contentResolver);
        }
        if (SWS_SOUND_EFFECTS_SUPPORT) {
            getSwsStatus(contentResolver);
        }
    }

    private void setEffectsState() {
        if (DTS_SOUND_EFFECTS_SUPPORT) {
            setDtsStatus();
        }
        if (SWS_SOUND_EFFECTS_SUPPORT) {
            if (mIsHuaweiSWS31Config) {
                sendSwsEQBroadcast();
            } else {
                setSwsStatus();
            }
        }
        if (this.mDmsStarted && DOLBY_SOUND_EFFECTS_SUPPORT) {
            setDolbyStatus();
        }
    }

    private void getDtsStatus(ContentResolver contentResolver) {
        this.mDtsStatus = Settings.Secure.getInt(contentResolver, "dts_mode", 0);
    }

    private void getSwsStatus(ContentResolver contentResolver) {
        this.mSwsStatus = Settings.System.getInt(contentResolver, "sws_mode", 0);
    }

    private void setDtsStatus() {
        if (this.mDtsStatus == 3) {
            AudioSystem.setParameters(DTS_MODE_ON_PARA);
            Slog.i(TAG, "setDtsStatus = on");
        } else if (this.mDtsStatus == 0) {
            AudioSystem.setParameters(DTS_MODE_OFF_PARA);
            Slog.i(TAG, "setDtsStatus = off");
        }
    }

    private void setSwsStatus() {
        if (this.mSwsStatus == 3) {
            AudioSystem.setParameters(SWS_MODE_ON_PARA);
        } else if (this.mSwsStatus == 0) {
            AudioSystem.setParameters(SWS_MODE_OFF_PARA);
        }
    }

    private void sendSwsEQBroadcast() {
        try {
            Intent intent = new Intent(ACTION_SWS_EQ);
            intent.setPackage("com.huawei.imedia.sws");
            this.mContext.sendBroadcastAsUser(intent, UserHandle.getUserHandleForUid(UserHandle.getCallingUserId()), PERMISSION_SWS_EQ);
        } catch (Exception e) {
            Slog.e(TAG, "sendSwsEQBroadcast exception: " + e);
        }
    }

    /* access modifiers changed from: package-private */
    public void onSetSoundEffectAndHSState(String packageName, boolean isOnTOP) {
        for (String equals : FORBIDDEN_SOUND_EFFECT_WHITE_LIST) {
            if (equals.equals(packageName)) {
                AudioSystem.setParameters(isOnTOP ? HS_NO_CHARGE_ON : HS_NO_CHARGE_OFF);
                this.mMyHandler.sendEmptyMessage(isOnTOP ? 1 : 2);
                if (DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("onSetSoundEffectAndHSState message: ");
                    sb.append(isOnTOP ? "HS_NO_CHARGE_ON + SOUND_EFFECT_CLOSE" : "HS_NO_CHARGE_OFF + SOUND_EFFECT_OPEN");
                    Slog.i(TAG, sb.toString());
                }
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void restoreSoundEffectAndHSState(String processName) {
        String preDTSState = null;
        String preSWSState = null;
        String preDOLBYState = null;
        if (DTS_SOUND_EFFECTS_SUPPORT) {
            preDTSState = SystemProperties.get(DTS_MODE_PRESTATE, "unknown");
        }
        if (SWS_SOUND_EFFECTS_SUPPORT && mIsHuaweiSWS31Config) {
            preSWSState = SystemProperties.get(SWS_MODE_PRESTATE, "unknown");
        }
        if (DOLBY_SOUND_EFFECTS_SUPPORT) {
            preDOLBYState = SystemProperties.get(DOLBY_MODE_PRESTATE, "unknown");
        }
        for (String equals : FORBIDDEN_SOUND_EFFECT_WHITE_LIST) {
            if (equals.equals(processName)) {
                AudioSystem.setParameters(HS_NO_CHARGE_OFF);
                if (DTS_SOUND_EFFECTS_SUPPORT && preDTSState != null && preDTSState.equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                    AudioSystem.setParameters(DTS_MODE_ON_PARA);
                    SystemProperties.set(DTS_MODE_PRESTATE, "unknown");
                    if (DEBUG) {
                        Slog.i(TAG, "restoreDTSAndHSState success!");
                    }
                }
                if (SWS_SOUND_EFFECTS_SUPPORT && mIsHuaweiSWS31Config && preSWSState != null && preSWSState.equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                    AudioSystem.setParameters(SWS_MODE_ON_PARA);
                    SystemProperties.set(SWS_MODE_PRESTATE, "unknown");
                    if (DEBUG) {
                        Slog.i(TAG, "restoreSWSAndHSState success!");
                    }
                }
                if (DOLBY_SOUND_EFFECTS_SUPPORT && preDOLBYState != null && preDOLBYState.equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
                    setDolbyEnable(true);
                    SystemProperties.set(DOLBY_MODE_PRESTATE, "unknown");
                    if (DEBUG) {
                        Slog.i(TAG, "restoreDOLBYAndHSState success!");
                    }
                }
                return;
            }
        }
    }

    public void onSetSoundEffectState(int device, int state) {
        sendMsgEx(this.mHwAudioHandlerEx, MSG_SET_DEVICE_STATE, 2, device, state, null, 0);
    }

    /* access modifiers changed from: private */
    public void checkAndSetSoundEffectState(int device, int state) {
        if (this.mIAsInner.checkAudioSettingsPermissionEx("onSetSoundEffectState()") && SOUND_EFFECTS_SUPPORT && (EFFECT_SUPPORT_DEVICE & device) != 0) {
            if (state == 1) {
                this.mSupportDevcieRef++;
                if (this.mSupportDevcieRef > 1) {
                    return;
                }
            } else {
                this.mSupportDevcieRef--;
                if (this.mSupportDevcieRef > 0) {
                    return;
                }
            }
            if (DOLBY_SOUND_EFFECTS_SUPPORT) {
                resetDolbyStateForHeadset(state);
            }
            if (!isScreenOff() && !isKeyguardLocked() && isTopActivity(FORBIDDEN_SOUND_EFFECT_WHITE_LIST)) {
                if (DTS_SOUND_EFFECTS_SUPPORT) {
                    onSetDtsState(state);
                }
                if (SWS_SOUND_EFFECTS_SUPPORT && mIsHuaweiSWS31Config) {
                    onSetSwsstate(state);
                }
                if (DOLBY_SOUND_EFFECTS_SUPPORT) {
                    onSetDolbyState(state);
                }
            }
        }
    }

    private boolean isTopActivity(String[] appnames) {
        String topPackageName = getTopActivityPackageName();
        if (topPackageName == null) {
            return false;
        }
        for (String equals : appnames) {
            if (equals.equals(topPackageName)) {
                return true;
            }
        }
        return false;
    }

    private void onSetDtsState(int state) {
        String preDTSState = SystemProperties.get(DTS_MODE_PRESTATE, "unknown");
        if (state == 1) {
            String curDTSState = AudioSystem.getParameters(DTS_MODE_PARA);
            if (curDTSState != null && curDTSState.contains(DTS_MODE_ON_PARA)) {
                AudioSystem.setParameters(DTS_MODE_OFF_PARA);
                SystemProperties.set(DTS_MODE_PRESTATE, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                if (DEBUG) {
                    Slog.i(TAG, "onSetDTSState cur DTS mode = " + curDTSState + " force set DTS off");
                }
            }
        } else if (state == 0 && preDTSState != null && preDTSState.equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
            AudioSystem.setParameters(DTS_MODE_ON_PARA);
            SystemProperties.set(DTS_MODE_PRESTATE, "unknown");
            if (DEBUG) {
                Slog.i(TAG, "onSetDTSState set DTS on");
            }
        }
    }

    private void onSetSwsstate(int state) {
        String preSWSState = SystemProperties.get(SWS_MODE_PRESTATE, "unknown");
        if (state == 1) {
            String curSWSState = AudioSystem.getParameters(SWS_MODE_PARA);
            if (curSWSState != null && curSWSState.contains(SWS_MODE_ON_PARA)) {
                AudioSystem.setParameters(SWS_MODE_OFF_PARA);
                SystemProperties.set(SWS_MODE_PRESTATE, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                if (DEBUG) {
                    Slog.i(TAG, "onSetSwsstate cur SWS mode = " + curSWSState + " force set SWS off");
                }
            }
        } else if (state == 0 && preSWSState != null && preSWSState.equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
            AudioSystem.setParameters(SWS_MODE_ON_PARA);
            SystemProperties.set(SWS_MODE_PRESTATE, "unknown");
            if (DEBUG) {
                Slog.i(TAG, "onSetSwsstate set SWS on");
            }
        }
    }

    /* access modifiers changed from: private */
    public static int byteArrayToInt32(byte[] ba, int index) {
        return ((ba[index + 3] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 24) | ((ba[index + 2] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 16) | ((ba[index + 1] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 8) | (ba[index] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY);
    }

    /* access modifiers changed from: private */
    public void sendDolbyUpdateBroadcast(int dlbParam, int status) {
        Intent intent = new Intent(DOLBY_UPDATE_EVENT);
        intent.putExtra(EVENTNAME, dlbParam == 0 ? DOLBY_UPDATE_EVENT_DS_STATE : DOLBY_UPDATE_EVENT_PROFILE);
        intent.putExtra(CMDINTVALUE, status);
        this.mContext.sendBroadcastAsUser(intent, new UserHandle(ActivityManager.getCurrentUser()), "com.huawei.permission.DOLBYCONTROL");
    }

    private void resetDolbyStateForHeadset(int headsetState) {
        this.mHeadSetPlugState = headsetState == 1;
        if (headsetState != 1) {
            SystemProperties.set(DOLBY_MODE_HEADSET_STATE, this.mDolbyEnable > 0 ? XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_ : "off");
            setDolbyEnable(true);
        } else if (!this.mDmsStarted) {
            setDolbyStatus();
        } else if (SystemProperties.get(DOLBY_MODE_HEADSET_STATE, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_).equals("off")) {
            setDolbyEnable(false);
        }
    }

    private void onSetDolbyState(int state) {
        String preDolbyState = SystemProperties.get(DOLBY_MODE_PRESTATE, "unknown");
        if (state == 1) {
            if (this.mDolbyEnable == 1) {
                SystemProperties.set(DOLBY_MODE_PRESTATE, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                if (DEBUG) {
                    Slog.i(TAG, "onSetDolbyState cur DOLBY mode = " + this.mDolbyEnable + " force set DOLBY off");
                }
                setDolbyEnable(false);
            }
        } else if (state == 0 && preDolbyState != null && preDolbyState.equals(XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_)) {
            SystemProperties.set(DOLBY_MODE_HEADSET_STATE, preDolbyState);
            SystemProperties.set(DOLBY_MODE_PRESTATE, "unknown");
        }
    }

    /* access modifiers changed from: private */
    public void setDolbyEnable(boolean state) {
        if (this.mDms != null) {
            ArrayList<Byte> params = new ArrayList<>();
            if (!state) {
                int i = 0;
                while (i < 12) {
                    try {
                        params.add(Byte.valueOf(btArrayDsDisable[i]));
                        i++;
                    } catch (Exception e) {
                        Slog.e(TAG, "setDolbyEnable exception: " + e);
                        return;
                    }
                }
                this.mDms.setDapParam(params);
                AudioSystem.setParameters(DOLBY_MODE_OFF_PARA);
                this.mDolbyEnable = 0;
                if (this.mEffect.hasControl()) {
                    this.mEffect.setEnabled(false);
                    return;
                }
                AudioEffect dolbyEffect = new AudioEffect(EFFECT_TYPE_NULL, EFFECT_TYPE_DS, 1, 0);
                dolbyEffect.setEnabled(false);
                dolbyEffect.release();
                return;
            }
            for (int i2 = 0; i2 < 12; i2++) {
                params.add(Byte.valueOf(btArrayDsEnable[i2]));
            }
            this.mDms.setDapParam(params);
            AudioSystem.setParameters(DOLBY_MODE_ON_PARA);
            this.mDolbyEnable = 1;
            if (this.mEffect.hasControl()) {
                this.mEffect.setEnabled(true);
                return;
            }
            AudioEffect dolbyEffect2 = new AudioEffect(EFFECT_TYPE_NULL, EFFECT_TYPE_DS, 1, 0);
            dolbyEffect2.setEnabled(true);
            dolbyEffect2.release();
        }
    }

    private static int byteArrayToInt(Byte[] ba, int index) {
        return ((ba[3 + index].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 24) | ((ba[2 + index].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 16) | ((ba[1 + index].byteValue() & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY) << 8) | (ba[index].byteValue() & 255);
    }

    private static int int32ToByteArray(int src, Byte[] dst, int index) {
        int index2 = index + 1;
        dst[index] = Byte.valueOf((byte) (src & 255));
        int index3 = index2 + 1;
        dst[index2] = Byte.valueOf((byte) ((src >>> 8) & 255));
        dst[index3] = Byte.valueOf((byte) ((src >>> 16) & 255));
        dst[index3 + 1] = Byte.valueOf((byte) ((src >>> 24) & 255));
        return 4;
    }

    public boolean setDolbyEffect(int mode) {
        Slog.i(TAG, "setDolbyEffect1 mode = " + mode);
        if (this.mDms == null) {
            setDolbyStatus();
        }
        int i = 0;
        if (this.mDms == null) {
            return false;
        }
        ArrayList<Byte> params = new ArrayList<>();
        switch (mode) {
            case 0:
                AudioSystem.setParameters("dolbyMultich=off");
                AudioSystem.setParameters("dolby_game_mode=off");
                while (i < 12) {
                    params.add(Byte.valueOf(btArrayDsSmart[i]));
                    i++;
                }
                this.mDms.setDapParam(params);
                break;
            case 1:
                AudioSystem.setParameters("dolbyMultich=off");
                AudioSystem.setParameters("dolby_game_mode=off");
                while (i < 12) {
                    params.add(Byte.valueOf(btArrayDsMovie[i]));
                    i++;
                }
                this.mDms.setDapParam(params);
                break;
            case 2:
                AudioSystem.setParameters("dolbyMultich=off");
                AudioSystem.setParameters("dolby_game_mode=off");
                while (i < 12) {
                    params.add(Byte.valueOf(btArrayDsMusic[i]));
                    i++;
                }
                this.mDms.setDapParam(params);
                break;
            case 3:
                try {
                    final Byte[] byteArray = new Byte[12];
                    for (int i2 = 0; i2 < 12; i2++) {
                        byteArray[i2] = (byte) 0;
                    }
                    int index = 0 + int32ToByteArray(EFFECT_PARAM_EFF_PROF, byteArray, 0);
                    try {
                        this.mDms.getDapParam(new ArrayList<>(Arrays.asList(byteArray)), new IDms.getDapParamCallback() {
                            public void onValues(int retval, ArrayList<Byte> outVal) {
                                int i = 0;
                                Iterator<Byte> it = outVal.iterator();
                                while (it.hasNext()) {
                                    byteArray[i] = it.next();
                                    i++;
                                }
                            }
                        });
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Failed to receive byte array to DMS.");
                    }
                    if (byteArrayToInt(byteArray, 0) == 0) {
                        AudioSystem.setParameters("dolbyMultich=on");
                        while (i < 12) {
                            params.add(Byte.valueOf(btArrayDsGame[i]));
                            i++;
                        }
                        this.mDms.setDapParam(params);
                        AudioSystem.setParameters("dolby_game_mode=on");
                        break;
                    } else {
                        return false;
                    }
                } catch (Exception e2) {
                    Slog.e(TAG, "setDolbyEffect exception: " + e2);
                    break;
                }
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void setDolbyStatus() {
        if (DOLBY_SOUND_EFFECTS_SUPPORT) {
            Slog.d(TAG, "Init Dolby effect on system ready");
            try {
                this.mEffect = new AudioEffect(EFFECT_TYPE_NULL, EFFECT_TYPE_DS, -1, 0);
                this.mEffect.setEnabled(true);
                setDolbyServiceClient();
                if (SystemProperties.get(DOLBY_MODE_HEADSET_STATE, "unknow").equals("unknow")) {
                    SystemProperties.set(DOLBY_MODE_HEADSET_STATE, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                }
            } catch (Exception e) {
                Slog.e(TAG, "create dolby effect failed, fatal problem!!!");
                this.mEffect = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDolbyServiceClient() {
        try {
            if (this.mDms != null) {
                this.mDms.unlinkToDeath(this.mDmsDeathRecipient);
            }
            this.mDms = IDms.getService();
            if (this.mDms != null) {
                AudioSystem.setParameters(DOLBY_MODE_ON_PARA);
                String mHeadSetDolbyState = SystemProperties.get(DOLBY_MODE_HEADSET_STATE, XmlConst.PreciseIgnore.COMP_SCREEN_ON_VALUE_);
                if (!this.mHeadSetPlugState || !mHeadSetDolbyState.equals("off")) {
                    setDolbyEnable(true);
                } else {
                    setDolbyEnable(false);
                }
                this.mDms.linkToDeath(this.mDmsDeathRecipient, (long) this.mDolbyClient.hashCode());
                this.mDms.registerClient(this.mDolbyClient, 3, this.mDolbyClient.hashCode());
                this.mDmsStarted = true;
                return;
            }
            Slog.e(TAG, "Dolby service is not ready, try to reconnect 1s later.");
            sendMsgEx(this.mHwAudioHandlerEx, 93, 0, 0, 0, null, 1000);
        } catch (Exception e) {
            Slog.e(TAG, "Connect Dolby service caught exception, try to reconnect 1s later.");
            sendMsgEx(this.mHwAudioHandlerEx, 93, 0, 0, 0, null, 1000);
        }
    }

    public void hideHiResIconDueKilledAPP(boolean killed, String packageName) {
        if (killed && packageName != null) {
            this.mHwAudioHandlerEx.sendEmptyMessageDelayed(15, 500);
        }
    }

    public int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        if (DEBUG) {
            Slog.i(TAG, "in setSoundEffectState");
        }
        Bundle b = new Bundle();
        b.putBoolean(RESTORE, restore);
        b.putString("packageName", packageName);
        b.putBoolean(ISONTOP, isOnTop);
        b.putString(RESERVED, reserved);
        this.mHwAudioHandlerEx.sendMessage(this.mHwAudioHandlerEx.obtainMessage(95, b));
        if (DEBUG) {
            Slog.i(TAG, "out setSoundEffectState");
        }
        return 0;
    }

    public int setSoundEffectStateAsynch(boolean restore, String packageName, boolean isOnTop, String reserved) {
        if (DEBUG) {
            Slog.i(TAG, "restore:" + restore + " packageName:" + packageName + " Top:" + isOnTop);
        }
        sendStartIntentForKaraoke(packageName, isOnTop);
        sendAppKilledIntentForKaraoke(restore, packageName, isOnTop, reserved);
        if (!SOUND_EFFECTS_SUPPORT || !this.mIAsInner.checkAudioSettingsPermissionEx("setSoundEffectState()") || isScreenOff() || isKeyguardLocked()) {
            return -1;
        }
        if (!restore) {
            onSetSoundEffectAndHSState(packageName, isOnTop);
        } else {
            restoreSoundEffectAndHSState(packageName);
        }
        return 0;
    }

    private int getRecordConcurrentTypeInternal(String pkgName) {
        if (pkgName == null) {
            return -1;
        }
        for (String name : CONCURRENT_RECORD_OTHER) {
            if (name.equals(pkgName)) {
                return RECORD_TYPE_OTHER;
            }
        }
        for (String name2 : CONCURRENT_RECORD_SYSTEM) {
            if (name2.equals(pkgName)) {
                return RECORD_TYPE_SYSTEM;
            }
        }
        return -1;
    }

    private boolean isConcurrentAllow(String requestPkgName, String activePkgName) {
        if (!this.mConcurrentCaptureEnable) {
            return false;
        }
        int requestIdx = getRecordConcurrentTypeInternal(requestPkgName);
        int activeIdx = getRecordConcurrentTypeInternal(activePkgName);
        boolean res = false;
        if ((requestIdx == RECORD_TYPE_OTHER && activeIdx == RECORD_TYPE_SYSTEM) || (requestIdx == RECORD_TYPE_SYSTEM && activeIdx == RECORD_TYPE_OTHER && this.mPackageUidMap.containsKey(requestPkgName) && this.mPackageUidMap.containsKey(activePkgName))) {
            res = true;
        }
        Slog.i(TAG, "ConcurrentAllow result " + res + " with [pkg:" + requestPkgName + " whitelist:" + requestIdx + "] and [pkg:" + activePkgName + " whitelist:" + activeIdx + "]");
        return res;
    }

    private boolean checkConcurRecList(String recordApk, int concurrentType) {
        boolean authenticate = false;
        if (concurrentType == RECORD_TYPE_OTHER) {
            try {
                PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(recordApk, 134217728);
                Signature[] signs = null;
                if (!(packageInfo == null || packageInfo.signingInfo == null)) {
                    signs = packageInfo.signingInfo.getSigningCertificateHistory();
                }
                byte[] verifySignByte = new ConcurrentRecSignatures().getSignByPkg(recordApk);
                byte[] recordApkSignByte = null;
                if (signs != null) {
                    recordApkSignByte = signs[0].toByteArray();
                }
                if (verifySignByte != null && recordApkSignByte != null && verifySignByte.length == recordApkSignByte.length && Arrays.equals(verifySignByte, recordApkSignByte)) {
                    authenticate = true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Slog.e(TAG, "can't resolve concurrent whitelist " + e);
            }
        }
        if (concurrentType == RECORD_TYPE_SYSTEM || authenticate) {
            try {
                int uid = this.mContext.getPackageManager().getPackageUidAsUser(recordApk, 0);
                if (uid > 0) {
                    this.mPackageUidMap.put(recordApk, Integer.valueOf(uid));
                    Slog.i(TAG, "add " + recordApk + " into conCurrent record list");
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e2) {
                Slog.e(TAG, "can't find apkinfo for " + recordApk);
            }
        } else {
            Slog.v(TAG, "can't trust " + recordApk + " in record white list");
        }
        return false;
    }

    public int getRecordConcurrentType(String pkgName) {
        if (!this.mConcurrentCaptureEnable) {
            return 0;
        }
        int concurrentType = getRecordConcurrentTypeInternal(pkgName);
        if (concurrentType == -1) {
            return 0;
        }
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        boolean allow = true;
        if (!this.mPackageUidMap.containsKey(pkgName) || this.mPackageUidMap.get(pkgName).intValue() != uid) {
            allow = checkConcurRecList(pkgName, concurrentType);
        }
        if (!allow) {
            return 0;
        }
        int activeRecordPid = Integer.parseInt(AudioSystem.getParameters("active_record_pid"));
        if (activeRecordPid == -1) {
            Slog.i(TAG, "Allow concurrent capture first for " + pkgName);
            return concurrentType;
        } else if (activeRecordPid == pid && concurrentType == RECORD_TYPE_SYSTEM) {
            Slog.i(TAG, "Allow concurrent capture self for " + pkgName);
            return concurrentType;
        } else if (isConcurrentAllow(pkgName, getPackageNameByPidEx(activeRecordPid))) {
            return concurrentType;
        } else {
            return 0;
        }
    }

    public void registerAudioModeCallback(IAudioModeDispatcher pcdb) {
        Log.v(TAG, "registerAudioModeCallback. ");
        if (pcdb != null) {
            synchronized (this.mClients) {
                AudioModeClient pmc = new AudioModeClient(pcdb);
                if (pmc.init()) {
                    this.mClients.add(pmc);
                }
            }
        }
    }

    public void unregisterAudioModeCallback(IAudioModeDispatcher pcdb) {
        Log.v(TAG, "unregisterAudioModeCallback. ");
        if (pcdb != null) {
            synchronized (this.mClients) {
                Iterator<AudioModeClient> clientIterator = this.mClients.iterator();
                while (clientIterator.hasNext()) {
                    AudioModeClient pmc = clientIterator.next();
                    if (!(pmc == null || pmc.mDispatcherCb == null || !pcdb.equals(pmc.mDispatcherCb))) {
                        pmc.release();
                        clientIterator.remove();
                    }
                }
            }
        }
    }

    public void dipatchAudioModeChanged(int actualMode) {
        Log.v(TAG, "dipatchAudioModeChanged mode = " + actualMode);
        synchronized (this.mClients) {
            if (!this.mClients.isEmpty()) {
                Iterator<AudioModeClient> clientIterator = this.mClients.iterator();
                while (clientIterator.hasNext()) {
                    AudioModeClient pmc = clientIterator.next();
                    if (pmc != null) {
                        try {
                            if (pmc.mDispatcherCb != null) {
                                pmc.mDispatcherCb.dispatchAudioModeChange(actualMode);
                            }
                        } catch (RemoteException e) {
                            Log.v(TAG, "failed to dispatch audio mode changes");
                        }
                    }
                }
            }
        }
    }

    public int getHwSafeUsbMediaVolumeIndex() {
        return mUsbSecurityVolumeIndex * 10;
    }

    public boolean isHwSafeUsbMediaVolumeEnabled() {
        return mUsbSecurityVolumeIndex > 0;
    }
}
