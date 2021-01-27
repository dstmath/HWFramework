package com.huawei.server.pc.vassist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.view.Display;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.android.server.am.HwActivityManagerService;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.hardware.input.InputManagerEx;
import com.huawei.android.os.HandlerEx;
import com.huawei.android.view.DisplayEx;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.android.view.HwWindowManager;
import com.huawei.android.view.KeyEventEx;
import com.huawei.hwpartpowerofficeservices.BuildConfig;
import com.huawei.server.pc.HwPCManagerService;
import com.huawei.server.pc.HwPCMkManager;
import com.huawei.util.LogEx;
import com.huawei.utils.HwPartResourceUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class HwPCVAssistCmdExecutor {
    private static final String BROADCAST_NOTIFY_DESKTOP_MODE = "com.android.server.pc.action.desktop_mode";
    private static final String BROADCAST_WPS_OPEN_DOC = "com.huawei.audio.assist.action_open";
    private static final String BROADCAST_WPS_OPEN_DOC_RESULT = "com.huawei.audio.assist.action_open_result";
    private static final int CMD_BACK_APP = 5;
    private static final int CMD_CLOSE_APP = 1;
    private static final int CMD_FULL_SCREEN_APP = 4;
    private static final int CMD_INVALID = -1;
    private static final int CMD_MAX_APP = 3;
    private static final int CMD_MIN_APP = 2;
    private static final int CMD_NEXT_PAGE = 9;
    private static final int CMD_OPEN_DOC = 7;
    private static final int CMD_PREV_PAGE = 8;
    private static final int CMD_SHOW_HOME = 6;
    private static final int CMD_START_APP = 0;
    private static final int CMD_START_PLAY = 10;
    private static final int CMD_STOP_PLAY = 11;
    private static final String COLUME_CMD_CLOUD = "contexts";
    private static final String COLUME_CMD_TERMINAL = "command";
    private static final String COLUME_CMD_UNWAKED = "wake";
    private static final boolean DEBUG = LogEx.getLogHWInfo();
    static final int FLAG_FROM_CLOUD = 0;
    static final int FLAG_FROM_TERMINAL = 1;
    static final int FLAG_FROM_UNWAKED = 2;
    static final int FLAG_LAST = 2;
    private static final int IDX_NEXT_PAGE = 4;
    private static final int IDX_NEXT_PAGE1 = 5;
    private static final int IDX_NEXT_PAGE2 = 6;
    private static final int IDX_NEXT_PAGE3 = 7;
    private static final int IDX_PLAY_PPT = 8;
    private static final int IDX_PLAY_PPT1 = 9;
    private static final int IDX_PREV_PAGE = 0;
    private static final int IDX_PREV_PAGE1 = 1;
    private static final int IDX_PREV_PAGE2 = 2;
    private static final int IDX_PREV_PAGE3 = 3;
    static final int ID_VASSIST_SERVICE = 272;
    private static final int INDEX_INITIATED = 0;
    private static final int INDEX_LAST_NEXT_PAGE = 7;
    private static final int INDEX_LAST_PREV_PAGE = 3;
    private static final int INDEX_LAST_START_PLAY = 9;
    private static final String JSON_KEY_ACTION = "action";
    private static final String JSON_KEY_CMD = "intent";
    private static final String JSON_KEY_EXTRA = "ext";
    private static final String JSON_KEY_LABEL = "label";
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_NUMBER = "number";
    private static final String JSON_KEY_ORIG_VALUE = "origValue";
    private static final String JSON_KEY_PAY_LOAD = "payload";
    private static final String JSON_KEY_PKG_NAME = "packageName";
    private static final String JSON_KEY_SLOTS = "slots";
    private static final String JSON_KEY_SLOTS_APP = "targetApp";
    private static final String JSON_KEY_SLOTS_SCREEN = "targetScreen";
    private static final String JSON_KEY_SLOTS_SCREEN_EXT = "externalDisplay";
    private static final String JSON_KEY_SLOTS_SCREEN_PHONE = "phone";
    private static final String JSON_KEY_SLOTS_SEQ = "targetSeq";
    private static final String JSON_KEY_SYNONYM_WORD = "synonymWord";
    private static final String JSON_KEY_VALUE = "value";
    private static final int JSON_VALUE_DEFAULT_DISPLAY = 0;
    private static final int JSON_VALUE_EXT_DISPLAY = 1;
    private static final int MSG_DESKTOP_MODE_CHANGED = 3;
    private static final int MSG_INVALID = -1;
    private static final int MSG_LOAD_RES = 1;
    private static final int MSG_RESULT_OPEN_DOC = 2;
    private static final int MSG_VOICE_CMD = 0;
    private static final String PERMISSION_BROADCAST_VASSIST_DESKTOP = "com.huawei.permission.VASSIST_DESKTOP";
    private static final String PERMISSION_BROADCAST_VASSIST_DESKTOP_WPS = "com.huawei.permission.VASSIST_DESKTOP_WPS";
    static final int RESULT_CANNOT_DO_IT = 3;
    static final int RESULT_CANNOT_FOUND_APP = 4;
    static final int RESULT_FAILED = 1;
    static final int RESULT_MAX_FULLSC_CANNOT_LARGER = 6;
    static final int RESULT_MIN_MAX_FULLSC_NOT_OPERATION = 5;
    static final int RESULT_NOT_IN_DESKTOP_MODE = 2;
    static final int RESULT_OPEN_DOC_NOT_FOUND = 7;
    static final int RESULT_SUCCESS = 0;
    static final int RESULT_SUCCESS_BACK_SHOWHOME = -4;
    static final int RESULT_SUCCESS_CLOSE_APP = -2;
    static final int RESULT_SUCCESS_MIN_MAX_APP = -3;
    static final int RESULT_SUCCESS_PAGE_UP_DOWN_FULLSCN = -6;
    static final int RESULT_SUCCESS_START_OPEN = -1;
    static final int RESULT_SUCCESS_START_PLAY = -5;
    private static final String[] SPECIAL_APPS_FOR_PAGE = {"com.android.gallery3d", "com.huawei.photos", "cn.wps.moffice_eng", SPECIAL_APP_MOFFICE_PRO_HW, "com.microsoft.office.officehub"};
    private static final String[] SPECIAL_APPS_FOR_PPT = {"cn.wps.moffice_eng", SPECIAL_APP_MOFFICE_PRO_HW, "com.microsoft.office.officehub"};
    private static final String SPECIAL_APP_MOFFICE_PRO_HW = "com.kingsoft.moffice_pro_hw";
    private static final String TAG = "HwPCVAssistCmdExecutor";
    private HwActivityManagerService mAMS;
    final List<String> mAppStartSuccStrs = new ArrayList();
    private HwPCVAssistAppStarter mAppStarter;
    private volatile int mCastingDisplayId = -1;
    final List<String> mCloseSuccResultStrs = new ArrayList();
    private Context mContext;
    private volatile boolean mDesktopMode = false;
    private LocalHandler mHandler;
    private HandlerThread mHandlerThread;
    private InputManager mInputManager;
    final List<String> mNotConnDisplayResultStrs = new ArrayList();
    private OpenDocResultReceiver mOpenDocResultReceiver;
    final List<String> mOpenDocResultStrs = new ArrayList();
    private int mReplayCount = 0;
    private HwPCManagerService mService;
    final List<String> mStartPlayResultStrs = new ArrayList();
    private Map<String, Integer> mUnWakedStrs = new HashMap();
    private Map<String, Integer> mWakedCmdsMap = new HashMap();

    /* access modifiers changed from: package-private */
    public static class VoiceCmd {
        String action;
        int castingDisplay = 0;
        int cmd = -1;
        String extra;
        int flag;
        Messenger messenger;
        String pkgName;
        int sessionId;
        int targetDisplay = 0;

        VoiceCmd() {
        }

        public String toString() {
            return "cmd = " + this.cmd + ", sessionId = " + this.sessionId + ", flag = " + this.flag + ", targetDisplay = " + this.targetDisplay + ", castingDisplay = " + this.castingDisplay;
        }
    }

    /* access modifiers changed from: private */
    public class LocalHandler extends HandlerEx {
        private VoiceCmd mVoiceCmd = null;

        LocalHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public VoiceCmd getVoiceCmdForOpenDoc() {
            return this.mVoiceCmd;
        }

        public void setVoiceCmdForOpenDoc(VoiceCmd cmd) {
            this.mVoiceCmd = cmd;
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                boolean desktopMode = HwPCVAssistCmdExecutor.this.mDesktopMode;
                int displayId = HwPCVAssistCmdExecutor.this.mCastingDisplayId;
                HwPCUtils.log(HwPCVAssistCmdExecutor.TAG, "MSG_VOICE_CMD displayId = " + displayId + ", desktopMode = " + desktopMode);
                VoiceCmd voiceCmd = HwPCVAssistCmdExecutor.this.resolveJsonCmd(msg, displayId);
                if (voiceCmd == null) {
                    HwPCUtils.log(HwPCVAssistCmdExecutor.TAG, "MSG_VOICE_CMD refuse to exec voice command");
                } else if (!desktopMode || !HwPCUtils.isValidExtDisplayId(displayId)) {
                    HwPCUtils.log(HwPCVAssistCmdExecutor.TAG, "MSG_VOICE_CMD is not in desktop mode or is invalid displayid, abort this command");
                    HwPCVAssistCmdExecutor.this.replyResultToVAssist(2, voiceCmd);
                } else if (voiceCmd.cmd == -1) {
                    HwPCUtils.log(HwPCVAssistCmdExecutor.TAG, "MSG_VOICE_CMD invalid cmd");
                    HwPCVAssistCmdExecutor.this.replyResultToVAssist(3, voiceCmd);
                } else {
                    HwPCVAssistCmdExecutor.this.execVoiceCmdImpl(voiceCmd);
                }
            } else if (i == 1) {
                HwPCVAssistCmdExecutor.this.loadStrings();
            } else if (i == 2) {
                HwPCVAssistCmdExecutor.this.replyResultOfOpenDoc(msg);
            }
        }
    }

    public HwPCVAssistCmdExecutor(Context context, HwPCManagerService service, HwActivityManagerService ams) {
        createLocalizedContext(context, Locale.SIMPLIFIED_CHINESE);
        this.mService = service;
        this.mAMS = ams;
        this.mHandlerThread = new HandlerThread(TAG, RESULT_SUCCESS_CLOSE_APP);
        this.mHandlerThread.start();
        this.mHandler = new LocalHandler(this.mHandlerThread.getLooper());
        initCmdMap();
        this.mAppStarter = new HwPCVAssistAppStarter(this.mContext, this, service);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
        this.mOpenDocResultReceiver = new OpenDocResultReceiver();
        IntentFilter openDocResultFilter = new IntentFilter();
        openDocResultFilter.addAction(BROADCAST_WPS_OPEN_DOC_RESULT);
        this.mContext.registerReceiver(this.mOpenDocResultReceiver, openDocResultFilter, PERMISSION_BROADCAST_VASSIST_DESKTOP, null);
    }

    private void createLocalizedContext(Context context, Locale desiredLocale) {
        Configuration conf = new Configuration(context.getResources().getConfiguration());
        conf.setLocale(desiredLocale);
        this.mContext = context.createConfigurationContext(conf);
    }

    private void initCmdMap() {
        this.mWakedCmdsMap.put("startApp", 0);
        this.mWakedCmdsMap.put("closeApp", 1);
        this.mWakedCmdsMap.put("minApp", 2);
        this.mWakedCmdsMap.put("maxApp", 3);
        this.mWakedCmdsMap.put("fullscreenApp", 4);
        this.mWakedCmdsMap.put("backApp", 5);
        this.mWakedCmdsMap.put("showHome", 6);
        this.mWakedCmdsMap.put("openDoc", 7);
        this.mWakedCmdsMap.put("prevPage", 8);
        this.mWakedCmdsMap.put("nextPage", 9);
        this.mWakedCmdsMap.put("startPlay", Integer.valueOf((int) CMD_START_PLAY));
        this.mWakedCmdsMap.put("stopPlay", Integer.valueOf((int) CMD_STOP_PLAY));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loadStrings() {
        loadStringsForExecResults();
        loadStringsForUnWaked();
    }

    private void loadStringsForExecResults() {
        this.mAppStartSuccStrs.add(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_start_succ")));
        this.mAppStartSuccStrs.add(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_start_ok")));
        this.mCloseSuccResultStrs.add(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_close_succ")));
        this.mCloseSuccResultStrs.add(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_close_ok")));
        this.mStartPlayResultStrs.add(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_ok")));
        this.mStartPlayResultStrs.add(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_no_problem")));
        this.mOpenDocResultStrs.addAll(this.mAppStartSuccStrs);
        this.mNotConnDisplayResultStrs.add(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_conn_external_display_tip")));
        this.mNotConnDisplayResultStrs.add(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_disconn_external_display_tip")));
    }

    private void loadStringsForUnWaked() {
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_prev_page")), 0);
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_prev_page1")), 1);
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_prev_page2")), 2);
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_prev_page3")), 3);
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_next_page")), 4);
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_next_page1")), 5);
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_next_page2")), 6);
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_next_page3")), 7);
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_play_ppt")), 8);
        this.mUnWakedStrs.put(this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_play_ppt1")), 9);
    }

    public void execVoiceCmd(Message message) {
        HwPCUtils.log(TAG, "execVoiceCmd");
        this.mHandler.sendMessage(this.mHandler.obtainMessage(0, message));
    }

    private boolean isCmdConcernAppNotFound(VoiceCmd voiceCmd) {
        return voiceCmd.cmd == 2 || voiceCmd.cmd == 3 || voiceCmd.cmd == 4 || voiceCmd.cmd == 1;
    }

    private void resolveSlotsOfCloud(VoiceCmd voiceCmd, String name, JSONObject jsonObjSlots) {
        HwPCUtils.log(TAG, "resolveSlotsOfCloud");
        if (JSON_KEY_SLOTS_APP.equals(name)) {
            try {
                JSONObject jsonValue = (JSONObject) jsonObjSlots.opt(JSON_KEY_VALUE);
                if (jsonValue != null) {
                    String appName = String.valueOf(jsonValue.opt(JSON_KEY_SYNONYM_WORD));
                    HwPCVAssistAppStarter hwPCVAssistAppStarter = this.mAppStarter;
                    voiceCmd.pkgName = HwPCVAssistAppStarter.getPackageName(this.mContext, appName);
                    if (TextUtils.isEmpty(voiceCmd.pkgName) && isCmdConcernAppNotFound(voiceCmd)) {
                        HwPCUtils.log(TAG, "resolveSlotsOfCloud close special app but app not found.");
                        voiceCmd.extra = appName;
                    } else if (voiceCmd.cmd == 0) {
                        voiceCmd.extra = appName;
                    }
                }
            } catch (ClassCastException | JSONException e) {
                HwPCUtils.log(TAG, "resolveSlotsOfCloud ParseException occurred");
            }
        } else if (JSON_KEY_SLOTS_SCREEN.equals(name)) {
            HwPCUtils.log(TAG, "resolveSlotsOfCloud origValue = " + ((String) jsonObjSlots.opt(JSON_KEY_ORIG_VALUE)));
            String value = String.valueOf(jsonObjSlots.opt(JSON_KEY_VALUE));
            HwPCUtils.log(TAG, "resolveSlotsOfCloud value = " + value);
            if (JSON_KEY_SLOTS_SCREEN_PHONE.equals(value)) {
                voiceCmd.targetDisplay = 0;
            } else if (!TextUtils.isEmpty(value) && JSON_KEY_SLOTS_SCREEN_PHONE.equals((String) new JSONObject(value).opt(JSON_KEY_SYNONYM_WORD))) {
                voiceCmd.targetDisplay = 0;
            }
        } else if (JSON_KEY_SLOTS_SEQ.equals(name)) {
            JSONObject value2 = (JSONObject) jsonObjSlots.opt(JSON_KEY_VALUE);
            HwPCUtils.log(TAG, "resolveSlotsOfCloud value = " + value2);
            if (value2 == null) {
                HwPCUtils.log(TAG, "resolveSlotsOfCloud value = null");
                return;
            }
            voiceCmd.extra = (String) value2.opt(JSON_KEY_NUMBER);
            HwPCUtils.log(TAG, "resolveSlotsOfCloud extra = " + voiceCmd.extra);
        }
    }

    private VoiceCmd createVoiceCmdForCloud(VoiceCmd voiceCmd, Bundle bundle) {
        String jsonStr = bundle.getString(COLUME_CMD_CLOUD);
        if (jsonStr == null) {
            HwPCUtils.log(TAG, "createVoiceCmdForCloud null command");
            return voiceCmd;
        }
        try {
            JSONArray jsonA = new JSONArray(jsonStr);
            if (jsonA.length() <= 0) {
                HwPCUtils.log(TAG, "createVoiceCmdForCloud jsonA is empty");
                return voiceCmd;
            }
            JSONObject jsonCmd = jsonA.getJSONObject(0);
            if (DEBUG) {
                HwPCUtils.log(TAG, "createVoiceCmdForCloud jsonCmd = " + jsonCmd);
            }
            if (jsonCmd == null) {
                HwPCUtils.log(TAG, "createVoiceCmdForCloud jsonCmd is empty");
                return voiceCmd;
            }
            JSONObject jsonPayload = (JSONObject) jsonCmd.opt(JSON_KEY_PAY_LOAD);
            if (DEBUG) {
                HwPCUtils.log(TAG, "createVoiceCmdForCloud jsonPayload = " + jsonPayload);
            }
            if (jsonPayload == null) {
                HwPCUtils.log(TAG, "createVoiceCmdForCloud jsonPayload is empty");
                return voiceCmd;
            }
            Integer intCmd = this.mWakedCmdsMap.get((String) jsonPayload.opt(JSON_KEY_CMD));
            if (DEBUG) {
                HwPCUtils.log(TAG, "createVoiceCmdForCloud intCmd = " + intCmd);
            }
            if (intCmd == null) {
                HwPCUtils.log(TAG, "createVoiceCmdForCloud command not found");
                return voiceCmd;
            }
            voiceCmd.cmd = intCmd.intValue();
            JSONArray jsonArraySlots = (JSONArray) jsonPayload.opt(JSON_KEY_SLOTS);
            int slotsLength = jsonArraySlots.length();
            if (slotsLength <= 0) {
                HwPCUtils.log(TAG, "createVoiceCmdForCloud jsonArraySlots = " + jsonArraySlots);
            }
            int i = 0;
            while (i < slotsLength) {
                JSONObject jsonObjSlots = jsonArraySlots.getJSONObject(i);
                HwPCUtils.log(TAG, "createVoiceCmdForCloud jsonObjSlots = " + jsonObjSlots);
                String name = (String) jsonObjSlots.opt(JSON_KEY_NAME);
                HwPCUtils.log(TAG, "createVoiceCmdForCloud name = " + name);
                resolveSlotsOfCloud(voiceCmd, name, jsonObjSlots);
                i++;
                jsonA = jsonA;
            }
            if (DEBUG) {
                HwPCUtils.log(TAG, "createVoiceCmdForCloud voiceCmd = " + voiceCmd);
            }
            return voiceCmd;
        } catch (JSONException e) {
            HwPCUtils.log(TAG, "createVoiceCmdForCloud ParseException occurred");
            voiceCmd.cmd = -1;
            return voiceCmd;
        } catch (Exception e2) {
            HwPCUtils.log(TAG, "createVoiceCmdForCloud occured Exception");
            voiceCmd.cmd = -1;
            return voiceCmd;
        }
    }

    private VoiceCmd createVoiceCmdForTerminal(VoiceCmd voiceCmd, Bundle bundle) {
        String jsonStr = bundle.getString(COLUME_CMD_TERMINAL);
        if (jsonStr == null) {
            HwPCUtils.log(TAG, "createVoiceCmdForTerminal null command");
            return voiceCmd;
        }
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (DEBUG) {
                HwPCUtils.log(TAG, "createVoiceCmdForTerminal json = " + json);
            }
            String actionForTerminal = (String) json.opt(JSON_KEY_ACTION);
            Integer intCmd = this.mWakedCmdsMap.get(actionForTerminal);
            if (intCmd == null) {
                HwPCUtils.log(TAG, "createVoiceCmdForTerminal command not found");
                return voiceCmd;
            } else if (intCmd.intValue() != 0) {
                return voiceCmd;
            } else {
                String labelForTerminal = (String) json.opt(JSON_KEY_LABEL);
                int extraForTerminal = json.optInt(JSON_KEY_EXTRA, 1);
                if (DEBUG) {
                    HwPCUtils.log(TAG, "createVoiceCmdForTerminal action = " + actionForTerminal + ",label = " + labelForTerminal + ", extra = " + extraForTerminal);
                }
                voiceCmd.pkgName = HwPCVAssistAppStarter.getPackageName(this.mContext, labelForTerminal);
                voiceCmd.extra = labelForTerminal;
                voiceCmd.cmd = intCmd.intValue();
                if (extraForTerminal == 0) {
                    voiceCmd.targetDisplay = 0;
                }
                return voiceCmd;
            }
        } catch (JSONException e) {
            HwPCUtils.log(TAG, "createVoiceCmdForTerminal ParseException occurred");
            return voiceCmd;
        }
    }

    private VoiceCmd createVoiceCmdForUnwaked(VoiceCmd voiceCmd, Bundle bundle) {
        Integer intCmd;
        String cmdStr = bundle.getString(COLUME_CMD_UNWAKED);
        if (cmdStr == null) {
            HwPCUtils.log(TAG, "createVoiceCmdForUnwaked null command");
            return voiceCmd;
        }
        if (DEBUG) {
            HwPCUtils.log(TAG, "createVoiceCmdForUnwaked cmdStr = " + cmdStr);
        }
        Integer intCmd2 = this.mUnWakedStrs.get(cmdStr);
        if (intCmd2 == null) {
            HwPCUtils.log(TAG, "createVoiceCmdForUnwaked command not found");
            return voiceCmd;
        }
        if (intCmd2.intValue() >= 0 && intCmd2.intValue() <= 3) {
            intCmd = 8;
        } else if (intCmd2.intValue() > 3 && intCmd2.intValue() <= 7) {
            intCmd = 9;
        } else if (intCmd2.intValue() <= 7 || intCmd2.intValue() > 9) {
            intCmd = -1;
        } else {
            intCmd = Integer.valueOf((int) CMD_START_PLAY);
        }
        if (intCmd.intValue() == -1) {
            HwPCUtils.log(TAG, "createVoiceCmdForUnwaked invalid command");
            return voiceCmd;
        }
        voiceCmd.cmd = intCmd.intValue();
        return voiceCmd;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private VoiceCmd resolveJsonCmd(Message msg, int castingDisplay) {
        Message message = (Message) msg.obj;
        Bundle bundle = message.getData();
        Messenger messenger = message.replyTo;
        if (messenger == null || bundle == null) {
            HwPCUtils.log(TAG, "resolveJsonCmd messenger or data is null");
            return null;
        }
        int serviceId = message.what;
        int flag = message.arg1;
        int sessionId = message.arg2;
        VoiceCmd voiceCmd = new VoiceCmd();
        voiceCmd.flag = flag;
        voiceCmd.sessionId = serviceId;
        voiceCmd.messenger = messenger;
        voiceCmd.castingDisplay = castingDisplay;
        voiceCmd.targetDisplay = castingDisplay;
        HwPCUtils.log(TAG, "resolveJsonCmd serviceId = " + serviceId + ", flag = " + flag + ", sessionId = " + sessionId);
        if (serviceId != ID_VASSIST_SERVICE || flag > 2 || sessionId < 0) {
            HwPCUtils.log(TAG, "resolveJsonCmd is not vassist service id or invalid flag or sessionId");
            return voiceCmd;
        } else if (flag == 1) {
            return createVoiceCmdForTerminal(voiceCmd, bundle);
        } else {
            if (flag != 2) {
                return createVoiceCmdForCloud(voiceCmd, bundle);
            }
            return createVoiceCmdForUnwaked(voiceCmd, bundle);
        }
    }

    public void execVoiceCmdImpl(VoiceCmd cmd) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "execVoiceCmdImpl cmd = " + cmd);
        }
        switch (cmd.cmd) {
            case 0:
                HwPCUtils.log(TAG, "CMD_START_APP");
                HwPCUtils.bdReport(this.mContext, 10059, "START_APP");
                this.mAppStarter.startApp(cmd);
                return;
            case 1:
                HwPCUtils.log(TAG, "CMD_CLOSE_APP");
                HwPCUtils.bdReport(this.mContext, 10059, "CLOSE_APP");
                closeApp(cmd);
                return;
            case 2:
                HwPCUtils.log(TAG, "CMD_MIN_APP");
                HwPCUtils.bdReport(this.mContext, 10059, "MIN_APP");
                minTask(cmd);
                return;
            case 3:
                HwPCUtils.log(TAG, "CMD_MAX_APP");
                HwPCUtils.bdReport(this.mContext, 10059, "MAX_APP");
                maxTask(cmd);
                return;
            case 4:
                HwPCUtils.log(TAG, "CMD_FULL_SCREEN_APP");
                HwPCUtils.bdReport(this.mContext, 10059, "FULL_SCREEN_APP");
                fullScreenTask(cmd);
                return;
            case 5:
                HwPCUtils.log(TAG, "CMD_BACK_APP");
                HwPCUtils.bdReport(this.mContext, 10059, "BACK_APP");
                backWindow(cmd);
                return;
            case 6:
                HwPCUtils.log(TAG, "CMD_SHOW_HOME");
                HwPCUtils.bdReport(this.mContext, 10059, "SHOW_HOME");
                this.mAMS.toggleHome();
                replyResultToVAssist(RESULT_SUCCESS_BACK_SHOWHOME, cmd);
                return;
            case 7:
                HwPCUtils.log(TAG, "CMD_OPEN_DOC");
                HwPCUtils.bdReport(this.mContext, 10059, "OPEN_DOC");
                openDocByIndex(cmd);
                return;
            case 8:
                HwPCUtils.log(TAG, "CMD_PREV_PAGE");
                HwPCUtils.bdReport(this.mContext, 10059, "PREV_PAGE");
                pageUpDown(cmd);
                return;
            case 9:
                HwPCUtils.log(TAG, "CMD_NEXT_PAGE");
                HwPCUtils.bdReport(this.mContext, 10059, "NEXT_PAGE");
                pageUpDown(cmd);
                return;
            case CMD_START_PLAY /* 10 */:
                HwPCUtils.log(TAG, "CMD_START_PLAY");
                HwPCUtils.bdReport(this.mContext, 10059, "START_PLAY");
                startStopPlayPPT(cmd);
                return;
            case CMD_STOP_PLAY /* 11 */:
                HwPCUtils.log(TAG, "CMD_STOP_PLAY");
                HwPCUtils.bdReport(this.mContext, 10059, "STOP_PLAY");
                startStopPlayPPT(cmd);
                return;
            default:
                return;
        }
    }

    private boolean checkAppExists(VoiceCmd cmd) {
        if (TextUtils.isEmpty(cmd.extra) || !TextUtils.isEmpty(cmd.pkgName)) {
            return true;
        }
        HwPCUtils.log(TAG, "checkAppExists cannot found App " + cmd.extra);
        return false;
    }

    private int getHandledTaskId(VoiceCmd cmd, int displayId) {
        return getHandledTaskId(cmd, displayId, false, false);
    }

    private int getHandledTaskId(VoiceCmd cmd, int displayId, boolean findInDefaultDisplay, boolean invisibleAlso) {
        String packageName = cmd.pkgName;
        if (DEBUG) {
            HwPCUtils.log(TAG, "getHandledTaskId packageName = " + packageName + ", displayId = " + displayId);
        }
        boolean emptyPkg = TextUtils.isEmpty(cmd.pkgName);
        int taskId = HwActivityTaskManager.getTopTaskIdInDisplay(displayId, cmd.pkgName, invisibleAlso);
        if (emptyPkg || taskId >= 0 || displayId == 0 || !findInDefaultDisplay) {
            return taskId;
        }
        return HwActivityTaskManager.getTopTaskIdInDisplay(0, cmd.pkgName, invisibleAlso);
    }

    private void closeApp(VoiceCmd cmd) {
        if (TextUtils.isEmpty(cmd.extra) && TextUtils.isEmpty(cmd.pkgName)) {
            HwPCUtils.log(TAG, "PC mode VoiceAssist unsupport close WLAN BLUETOOTH and so on ");
            replyResultToVAssist(3, cmd);
        } else if (!checkAppExists(cmd)) {
            replyResultToVAssist(4, cmd);
        } else {
            int taskId = getHandledTaskId(cmd, cmd.targetDisplay, true, true);
            HwPCUtils.log(TAG, "closeApp taskId = " + taskId);
            if (taskId < 0) {
                HwPCUtils.log(TAG, "closeApp invalid taskId");
            }
            this.mAMS.removeTask(taskId);
            this.mService.setFocusedPCDisplayId("CMD_CLOSE_APP");
            replyResultToVAssist(RESULT_SUCCESS_CLOSE_APP, cmd);
        }
    }

    private void minTask(VoiceCmd cmd) {
        if (!checkAppExists(cmd)) {
            replyResultToVAssist(5, cmd);
            return;
        }
        int taskId = getHandledTaskId(cmd, cmd.castingDisplay, true, true);
        HwPCUtils.log(TAG, "minTask taskId = " + taskId);
        if (taskId < 0) {
            HwPCUtils.log(TAG, "minTask invalid taskId");
            replyResultToVAssist(5, cmd);
        } else if (!HwActivityTaskManager.isTaskVisible(taskId)) {
            HwPCUtils.log(TAG, "minTask task is invisible already");
            replyResultToVAssist(RESULT_SUCCESS_MIN_MAX_APP, cmd);
        } else {
            this.mAMS.moveTaskBackwards(taskId);
            this.mService.setFocusedPCDisplayId("CMD_MIN_APP");
            replyResultToVAssist(RESULT_SUCCESS_MIN_MAX_APP, cmd);
        }
    }

    private void maxTask(VoiceCmd cmd) {
        if (!checkAppExists(cmd)) {
            replyResultToVAssist(5, cmd);
            return;
        }
        int taskId = getHandledTaskId(cmd, cmd.castingDisplay);
        HwPCUtils.log(TAG, "maxTask taskId = " + taskId);
        if (taskId < 0) {
            replyResultToVAssist(5, cmd);
        } else if (!HwActivityTaskManager.isTaskSupportResize(taskId, false, true)) {
            HwPCUtils.log(TAG, "maxTask cannot larger");
            replyResultToVAssist(6, cmd);
        } else {
            this.mAMS.hwResizeTask(taskId, new Rect(0, 0, 0, 0));
            replyResultToVAssist(RESULT_SUCCESS_MIN_MAX_APP, cmd);
            this.mService.setFocusedPCDisplayId("CMD_MAX_APP");
        }
    }

    private void fullScreenTask(VoiceCmd cmd) {
        if (!checkAppExists(cmd)) {
            replyResultToVAssist(5, cmd);
            return;
        }
        int taskId = getHandledTaskId(cmd, cmd.castingDisplay);
        HwPCUtils.log(TAG, "fullScreenTask taskId = " + taskId);
        if (taskId < 0) {
            HwPCUtils.log(TAG, "invalid taskid");
            replyResultToVAssist(5, cmd);
        } else if (!HwActivityTaskManager.isTaskSupportResize(taskId, true, false)) {
            HwPCUtils.log(TAG, "maxTask cannot larger");
            replyResultToVAssist(6, cmd);
        } else if (!HwActivityTaskManager.isTaskVisible(taskId)) {
            HwPCUtils.log(TAG, "minTask task is invisible already");
            replyResultToVAssist(5, cmd);
        } else {
            this.mAMS.hwResizeTask(taskId, new Rect(-1, -1, -1, -1));
            this.mService.setFocusedPCDisplayId("CMD_FULL_SCREEN_APP");
            replyResultToVAssist(RESULT_SUCCESS_PAGE_UP_DOWN_FULLSCN, cmd);
        }
    }

    public static boolean specialAppFocused(int displayId, WindowStateData outData, boolean forPPT, boolean ignoreLaserView) {
        String[] specialApps;
        HwPCUtils.log(TAG, "specialAppFocused displayId = " + displayId);
        if (ignoreLaserView || !HwWindowManager.hasLighterViewInPCCastMode()) {
            Bundle outBundle = new Bundle();
            HwWindowManager.getCurrFocusedWinInExtDisplay(outBundle);
            String focusableAppPkg = outBundle.getString("pkgName", null);
            boolean topWinApp = outBundle.getBoolean("isApp", false);
            Rect bounds = (Rect) outBundle.getParcelable("bounds");
            if (DEBUG) {
                HwPCUtils.log(TAG, "specialAppFocused focusableAppPkg = " + focusableAppPkg + ",topWinApp = " + topWinApp + ", bounds = " + bounds);
            }
            boolean topSpecialApp = false;
            if (forPPT) {
                specialApps = SPECIAL_APPS_FOR_PPT;
            } else {
                specialApps = SPECIAL_APPS_FOR_PAGE;
            }
            if (focusableAppPkg != null) {
                int i = 0;
                while (true) {
                    if (i >= specialApps.length) {
                        break;
                    } else if (focusableAppPkg.contains(specialApps[i])) {
                        topSpecialApp = true;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (outData != null) {
                outData.pkgName = focusableAppPkg;
                outData.topWinApp = topSpecialApp;
                outData.bounds = bounds;
            }
            HwPCUtils.log(TAG, "specialAppFocused topWinApp = " + topWinApp + ", topSpecialApp = " + topSpecialApp);
            if (!topWinApp || !topSpecialApp) {
                return false;
            }
            return true;
        }
        HwPCUtils.log(TAG, "specialAppFocused has laser lighter view");
        return false;
    }

    private void sendKeyEvent(int code, int metaState, int action) {
        long downTime = SystemClock.uptimeMillis();
        KeyEvent ev = new KeyEvent(downTime, downTime, action, code, 0, metaState, -1, 0, 8, 257);
        KeyEventEx.setDisplayId(ev, this.mCastingDisplayId);
        HwPCUtils.log(TAG, "send key event, code:" + code + ",metaState:" + metaState);
        injectEvent(ev);
    }

    private boolean injectEvent(InputEvent event) {
        if (this.mInputManager == null) {
            this.mInputManager = (InputManager) this.mContext.getSystemService("input");
        }
        InputManager inputManager = this.mInputManager;
        if (inputManager != null) {
            return InputManagerEx.injectInputEvent(inputManager, event, InputManagerEx.getInjectInputEventModeAsync());
        }
        return false;
    }

    private void openDocByIndex(VoiceCmd cmd) {
        HwPCUtils.log(TAG, "openDocByIndex");
        if (!specialAppFocused(cmd.castingDisplay, new WindowStateData(), true, false)) {
            HwPCUtils.log(TAG, "openDocByIndex special app not focused");
            replyResultToVAssist(3, cmd);
            return;
        }
        boolean hasNoException = true;
        int index = -1;
        if (!TextUtils.isEmpty(cmd.extra)) {
            try {
                index = Integer.parseInt(cmd.extra);
            } catch (NumberFormatException e) {
                hasNoException = false;
                HwPCUtils.log(TAG, "openDocByIndex NumberFormatException ocurred.");
            }
        }
        HwPCUtils.log(TAG, "index = " + index);
        if (hasNoException) {
            this.mHandler.setVoiceCmdForOpenDoc(cmd);
            Intent intent = new Intent(BROADCAST_WPS_OPEN_DOC);
            intent.putExtra("file_index", index);
            intent.putExtra("session_id", cmd.sessionId);
            this.mContext.sendBroadcast(intent, PERMISSION_BROADCAST_VASSIST_DESKTOP);
            return;
        }
        replyResultToVAssist(5, cmd);
    }

    private class OpenDocResultReceiver extends BroadcastReceiver {
        private OpenDocResultReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwPCUtils.log(HwPCVAssistCmdExecutor.TAG, "OpenDocReusltReceiver received a null intent");
            } else if (HwPCVAssistCmdExecutor.BROADCAST_WPS_OPEN_DOC_RESULT.equals(intent.getAction())) {
                int result = intent.getIntExtra("result", 0);
                int sessionId = intent.getIntExtra("session_id", -1);
                HwPCUtils.log(HwPCVAssistCmdExecutor.TAG, "receive: BROADCAST_WPS_OPEN_DOC_RESULT, result = " + result + ", sessionId = " + sessionId);
                HwPCVAssistCmdExecutor.this.mHandler.removeMessages(2);
                Message msg = HwPCVAssistCmdExecutor.this.mHandler.obtainMessage(2);
                msg.arg1 = result;
                msg.arg2 = sessionId;
                HwPCVAssistCmdExecutor.this.mHandler.sendMessage(msg);
            }
        }
    }

    private void broadcastDesktopMode(boolean desktopMode) {
        HwPCUtils.log(TAG, "broadcastDesktopMode desktopMode = " + desktopMode);
        Intent intent = new Intent(BROADCAST_NOTIFY_DESKTOP_MODE);
        intent.putExtra("mode", desktopMode);
        this.mContext.sendBroadcast(intent, PERMISSION_BROADCAST_VASSIST_DESKTOP);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyResultOfOpenDoc(Message msg) {
        int result = msg.arg1;
        int sessionId = msg.arg2;
        HwPCUtils.log(TAG, "replyResultOfOpenDoc");
        VoiceCmd cmd = this.mHandler.getVoiceCmdForOpenDoc();
        if (cmd == null || sessionId != cmd.sessionId) {
            HwPCUtils.log(TAG, "replyResultOfOpenDoc invalid reply from WPS");
            return;
        }
        if (result <= 0) {
            replyResultToVAssist(-1, cmd);
        } else {
            replyResultToVAssist(7, cmd);
        }
        this.mHandler.setVoiceCmdForOpenDoc(null);
    }

    public static final class WindowStateData {
        public Rect bounds;
        public String pkgName;
        public boolean topWinApp;

        public WindowStateData() {
            reset();
        }

        public void reset() {
            this.pkgName = null;
            this.topWinApp = true;
            this.bounds = null;
        }

        public String toString() {
            return "topWinApp = " + this.topWinApp + ", bounds = " + this.bounds;
        }
    }

    private MotionEvent.PointerCoords getScollPoint(int displayId) {
        HwPCUtils.log(TAG, "getScollPoint displayId = " + displayId);
        MotionEvent.PointerCoords point = new MotionEvent.PointerCoords();
        point.x = -1.0f;
        point.y = -1.0f;
        DisplayManager dm = (DisplayManager) this.mContext.getSystemService("display");
        if (dm == null) {
            HwPCUtils.log(TAG, "getScollPoint dm = null");
            return point;
        }
        Display display = dm.getDisplay(displayId);
        if (display == null) {
            HwPCUtils.log(TAG, "getScollPoint display = null");
            return point;
        }
        DisplayInfoEx outDisplayInfo = new DisplayInfoEx();
        DisplayEx.getDisplayInfo(display, outDisplayInfo);
        Rect bound = HwActivityTaskManager.getPCTopTaskBounds(displayId);
        HwPCUtils.log(TAG, "getScollPoint bound = " + bound);
        if (bound == null) {
            point.x = ((float) outDisplayInfo.getLogicalWidth()) / 2.0f;
            point.y = ((float) outDisplayInfo.getLogicalHeight()) / 2.0f;
            HwPCUtils.log(TAG, "getScollPoint point.x = " + point.x + ", point.y = " + point.y);
            return point;
        }
        if (bound.right > outDisplayInfo.getLogicalWidth()) {
            bound.right = outDisplayInfo.getLogicalWidth();
        }
        if (bound.bottom > outDisplayInfo.getLogicalHeight()) {
            bound.bottom = outDisplayInfo.getLogicalHeight();
        }
        point.x = ((float) (bound.left + bound.right)) / 2.0f;
        point.y = ((float) (bound.top + bound.bottom)) / 2.0f;
        HwPCUtils.log(TAG, "getScollPoint point.x = " + point.x + ", point.y = " + point.y);
        if (point.x < 0.0f) {
            point.x = 0.0f;
        }
        return point;
    }

    private void pageUpDown(VoiceCmd voiceCmd) {
        HwPCUtils.log(TAG, "pageUpDown cmd = " + voiceCmd.cmd);
        WindowStateData wsd = new WindowStateData();
        if (!specialAppFocused(voiceCmd.castingDisplay, wsd, false, true)) {
            HwPCUtils.log(TAG, "pageUpDown special app not focused");
            replyResultToVAssist(3, voiceCmd);
            return;
        }
        boolean isPageDown = voiceCmd.cmd == 9;
        if (wsd.pkgName != null && wsd.pkgName.contains(SPECIAL_APP_MOFFICE_PRO_HW)) {
            int keyCode = isPageDown ? 93 : 92;
            sendKeyEvent(keyCode, 0, 0);
            sendKeyEvent(keyCode, 0, 1);
            replyResultToVAssist(RESULT_SUCCESS_PAGE_UP_DOWN_FULLSCN, voiceCmd);
        } else if (!wsd.topWinApp) {
            int keyCode2 = isPageDown ? 20 : 19;
            sendKeyEvent(keyCode2, 0, 0);
            sendKeyEvent(keyCode2, 0, 1);
            replyResultToVAssist(RESULT_SUCCESS_PAGE_UP_DOWN_FULLSCN, voiceCmd);
        } else {
            this.mService.setFocusedPCDisplayId("pageUpDown");
            MotionEvent.PointerCoords pointer = getScollPoint(voiceCmd.castingDisplay);
            if (pointer.x < 0.0f) {
                HwPCUtils.log(TAG, "pageUpDown special app not focused");
                replyResultToVAssist(3, voiceCmd);
                return;
            }
            onScrollEvent(pointer.x, pointer.y, isPageDown);
            replyResultToVAssist(RESULT_SUCCESS_PAGE_UP_DOWN_FULLSCN, voiceCmd);
        }
    }

    private void onScrollEvent(float x, float y, boolean pageDown) {
        HwPCUtils.log(TAG, "onScrollEvent x:" + x + ", y = " + y + ", pageDown = " + pageDown);
        injectEvent(HwPCMkManager.obtainMotionEvent(HwPCMkManager.obtainMouseEvent(8, 0, x, y, 0, 0), x, y, 8, 0, pageDown ? -1.0f : 1.0f, 0.0f));
    }

    private void backWindow(VoiceCmd cmd) {
        int taskId = getHandledTaskId(cmd, cmd.targetDisplay);
        HwPCUtils.log(TAG, "backWindow taskId = " + taskId);
        if (taskId < 0) {
            HwPCUtils.log(TAG, "backWindow invalid taskid");
            replyResultToVAssist(5, cmd);
        } else if (!HwActivityTaskManager.isTaskVisible(taskId)) {
            HwPCUtils.log(TAG, "backWindow task is invisible already");
            replyResultToVAssist(5, cmd);
        } else {
            sendKeyEvent(4, 0, 0);
            sendKeyEvent(4, 0, 1);
            replyResultToVAssist(RESULT_SUCCESS_BACK_SHOWHOME, cmd);
        }
    }

    private void startStopPlayPPT(VoiceCmd cmd) {
        boolean isStartPlay = cmd.cmd == CMD_START_PLAY;
        HwPCUtils.log(TAG, "startStopPlayPPT isStartPlay = " + isStartPlay);
        if (!specialAppFocused(cmd.targetDisplay, new WindowStateData(), true, false)) {
            HwPCUtils.log(TAG, "pageUpDown special app not focused");
            replyResultToVAssist(3, cmd);
            return;
        }
        int keyCode = isStartPlay ? 135 : 111;
        sendKeyEvent(keyCode, 0, 0);
        sendKeyEvent(keyCode, 0, 1);
        replyResultToVAssist(RESULT_SUCCESS_START_PLAY, cmd);
    }

    public void notifyDesktopModeChanged(boolean desktopMode, int displayId) {
        HwPCUtils.log(TAG, "notifyDesktopModeChanged desktopMode = " + desktopMode + ", mDesktopMode = " + this.mDesktopMode + ", displayId = " + displayId);
        this.mDesktopMode = desktopMode;
        this.mCastingDisplayId = displayId;
        broadcastDesktopMode(desktopMode);
    }

    private int getResponseErrCode(int errCode) {
        if (errCode > 0) {
            return 1;
        }
        return 0;
    }

    public String getRandomResponseStr(List<String> list) {
        if (list == null) {
            HwPCUtils.log(TAG, "getRandomResponseStr list = null");
            return BuildConfig.FLAVOR;
        }
        int reportIndex = getRadomReportStringIndex(list.size());
        if (reportIndex < 0) {
            return list.get(0);
        }
        HwPCUtils.log(TAG, "getRandomResponseStr reportIndex = " + reportIndex + ", size = " + list.size());
        return list.get(reportIndex);
    }

    private String getResponseMsg(int errCode) {
        switch (errCode) {
            case RESULT_SUCCESS_PAGE_UP_DOWN_FULLSCN /* -6 */:
                return BuildConfig.FLAVOR;
            case RESULT_SUCCESS_START_PLAY /* -5 */:
                return getRandomResponseStr(this.mStartPlayResultStrs);
            case RESULT_SUCCESS_BACK_SHOWHOME /* -4 */:
            case RESULT_SUCCESS_MIN_MAX_APP /* -3 */:
                return this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_ok_done"));
            case RESULT_SUCCESS_CLOSE_APP /* -2 */:
                return getRandomResponseStr(this.mCloseSuccResultStrs);
            case -1:
                return getRandomResponseStr(this.mAppStartSuccStrs);
            case 0:
            case 1:
            default:
                return this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_start_succ"));
            case 2:
                return getRandomResponseStr(this.mNotConnDisplayResultStrs);
            case 3:
                return this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_cannot_do_it"));
            case 4:
                return this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_app_not_found"));
            case 5:
                return this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_app_not_operation"));
            case 6:
                return this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_cannot_larger"));
            case 7:
                return this.mContext.getString(HwPartResourceUtils.getResourceId("pc_vassist_doc_not_found"));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void replyResultToVAssist(int errCode, VoiceCmd cmd) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "replyResultToVAssist errCode = " + errCode + ", cmd = " + cmd);
        }
        if (cmd == null || cmd.messenger == null) {
            HwPCUtils.log(TAG, "replyResultToVAssist messenger = null, cannot reply to vassist");
            return;
        }
        try {
            JSONObject obj = new JSONObject();
            obj.put("errorCode", getResponseErrCode(errCode));
            String response = getResponseMsg(errCode);
            obj.put("responseText", response);
            obj.put("ttsText", response);
            obj.put("isFinish", "true");
            replyResultToVAssist(obj, cmd);
        } catch (JSONException e) {
            HwPCUtils.log(TAG, "replyResultToVAssist JSONException occurred");
        }
    }

    public void replyResultToVAssist(JSONObject jObj, VoiceCmd cmd) {
        if (DEBUG) {
            HwPCUtils.log(TAG, "replyResultToVAssist jObj = " + jObj + ", cmd = " + cmd);
        }
        if (cmd == null || cmd.messenger == null) {
            HwPCUtils.log(TAG, "replyResultToVAssist messenger = null, cannot reply to vassist");
            return;
        }
        Message msg = Message.obtain(null, ID_VASSIST_SERVICE, cmd.sessionId, cmd.flag);
        Bundle bundle = new Bundle();
        bundle.putString("serviceReply", jObj.toString());
        msg.setData(bundle);
        try {
            cmd.messenger.send(msg);
        } catch (RemoteException e) {
            HwPCUtils.log(TAG, "replyResultToVAssist exception occurred");
        }
    }

    public int getRadomReportStringIndex(int size) {
        if (size <= 0) {
            HwPCUtils.log(TAG, "getResponseMsg size = 0");
            return -1;
        }
        if (this.mReplayCount == 2147483646) {
            this.mReplayCount = 0;
        }
        this.mReplayCount++;
        HwPCUtils.log(TAG, "getResponseMsg mReplayCount = " + this.mReplayCount + ", size = " + size);
        return this.mReplayCount % size;
    }
}
