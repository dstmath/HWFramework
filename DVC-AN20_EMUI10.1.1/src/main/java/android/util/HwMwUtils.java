package android.util;

import android.common.HwFrameworkFactory;
import android.magicwin.HwMagicWindow;
import android.os.Bundle;
import android.os.SystemProperties;
import com.huawei.android.fsm.HwFoldScreenManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwMwUtils {
    public static final int APP_CONFIG_MODE_ANAN = 0;
    public static final int APP_CONFIG_MODE_OPEN = 1;
    public static final int DURATION_MOVE_WINDOW_TOTAL = 750;
    public static final String EASYGOURL = "content://com.huawei.easygo.easygoprovider/v_function";
    public static final String EASYGO_SEV_PKG = "com.huawei.systemserver";
    public static final boolean ENABLED = SystemProperties.getBoolean("ro.config.hw_magic_window_enable", false);
    public static final int FLAG_VIEW_COUNT = 1;
    public static final boolean HWMULTIWIN_ENABLED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    public static final String HW_MAGICWINDOW_SERVICE = "HwMagicWindowService";
    public static final boolean IS_FOLD_SCREEN_DEVICE = HwFoldScreenManager.isFoldable();
    private static final boolean IS_MMI_RUNNING = SystemProperties.get("runtime.mmitest.isrunning", "false").equals("true");
    public static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", ""));
    public static final String MAGICWINDOW_TITLE = "MagicWindow";
    public static final boolean MAGICWIN_LOG_SWITCH = SystemProperties.getBoolean("ro.config.hw_magicwindow_log", false);
    public static final String MAGIC_WINDOW_DIALOG = "MagicWindowGuideDialog";
    public static final int MODE_A1AN = 1;
    public static final int MODE_ANAN = 2;
    public static final int MODE_NONE = -2;
    public static final int MODE_OFF = -1;
    public static final int MODE_OPEN = 3;
    public static final int MODE_SINGLE = 0;
    public static final int MSG_ADJWIN_TOFULL_WHEN_RESUME = 14;
    public static final int MSG_ALARM_UPDATE_CONFIG = 4;
    public static final int MSG_BACK_TO_FOLD_FULL_DISPLAY = 11;
    public static final int MSG_FORCE_STOP_PACKAGE = 6;
    public static final int MSG_GET_WALLPAPER = 1;
    public static final int MSG_LOCAL_CHANGE = 18;
    public static final int MSG_MOVE_LATEST_TO_TOP = 10;
    public static final int MSG_REPORT_USAGE_STATISTICS = 9;
    public static final int MSG_SCREEN_OFF = 8;
    public static final int MSG_SERVICE_INIT = 17;
    public static final int MSG_SET_BOUNDS = 0;
    public static final int MSG_SET_MUlTIWIN_CAMERA_PROP = 3;
    public static final int MSG_START_RELATE_ACT = 13;
    public static final int MSG_UPDATE_APP_CONFIG = 7;
    public static final int MSG_UPDATE_BOUND_FOR_DENSITY_CHANGE = 16;
    public static final int MSG_UPDATE_MAGIC_WINDOW_CONFIG = 2;
    public static final int MSG_UPDATE_STACK_VISIBILITY = 12;
    public static final int MSG_USER_SWITCH = 5;
    public static final int MSG_WRITE_SETTING_XML = 15;
    public static final int PAGE_TYPE_DEFAULT = 0;
    public static final int PAGE_TYPE_NORMAL = 1;
    public static final int PAGE_TYPE_TRANS = 2;
    public static final int POLICY_CHANGE_ORIENTATION = 80;
    public static final int POLICY_CHECK_ACTIVITY_FULLSCREEN = 108;
    public static final int POLICY_CLEAR_ANIMATION = 25;
    public static final int POLICY_COMPUTE_PIVOT = 105;
    public static final int POLICY_EVENT_BACK_PRESSED = 28;
    public static final int POLICY_EVENT_FINISH = 52;
    public static final int POLICY_EVENT_RESUME = 51;
    public static final int POLICY_EXIT_SPLIT = 15;
    public static final int POLICY_GESTURE_BACK_SWITCH_FOCUS = 137;
    public static final int POLICY_GET_HOSTPARAM = 31;
    public static final int POLICY_GET_HOSTRESULT = 32;
    public static final int POLICY_GET_MAGIC_POSITION = 134;
    public static final int POLICY_GET_MAGIC_RATIO = 20;
    public static final int POLICY_GET_ROTATION_ANIMATION = 70;
    public static final int POLICY_IN_APP_SPLITE = 132;
    public static final int POLICY_IS_COVERVIEW = 1001;
    public static final int POLICY_IS_NEED_SCALE = 21;
    public static final int POLICY_IS_NEED_SYNC = 71;
    public static final int POLICY_IS_ROTATION180 = 61;
    public static final int POLICY_JUDGE_ISRIGHT = 104;
    public static final int POLICY_NOTIFY_APP_DIED = 18;
    public static final int POLICY_RESIZE_BEFORE_RESUME = 136;
    public static final int POLICY_RESIZE_FOR_DRAG = 62;
    public static final int POLICY_RESIZE_WHEN_MOVEBACK = 133;
    public static final int POLICY_SET_FULLSCREEN = 41;
    public static final int POLICY_SET_HWMULTISTACK = 17;
    public static final int POLICY_SET_SCREEN_RECT = 1002;
    public static final int POLICY_UPDATE_ARGS = 2;
    public static final int POLICY_UPDATE_ATTRS = 103;
    public static final int POLICY_UPDATE_CONFIG = 6;
    public static final int POLICY_UPDATE_DISPLAY_INFO = 81;
    public static final int POLICY_UPDATE_FOCUS = 5;
    public static final int POLICY_UPDATE_FRAME = 102;
    public static final int POLICY_UPDATE_HOSTPARAM = 11;
    public static final int POLICY_UPDATE_HWMULTISTACK = 16;
    public static final int POLICY_UPDATE_INTENT = 0;
    public static final int POLICY_UPDATE_INTENTFLAG = 1;
    public static final int POLICY_UPDATE_LAUNCH_ANIMATION = 72;
    public static final int POLICY_UPDATE_MULTIRESUME = 14;
    public static final int POLICY_UPDATE_MW_ROUND_CORNER = 106;
    public static final int POLICY_UPDATE_ORIENTATION = 9;
    public static final int POLICY_UPDATE_PAGE_TYPE_BY_KEYEVENT = 128;
    public static final int POLICY_UPDATE_RESOURCE = 12;
    public static final int POLICY_UPDATE_ROTATION = 4;
    public static final int POLICY_UPDATE_SENSOR_CHANGE = 82;
    public static final int POLICY_UPDATE_SPLIT = 10;
    public static final int POLICY_UPDATE_STACK_VISIBILITY = 135;
    public static final int POLICY_UPDATE_STATUSBAR = 101;
    public static final int POLICY_UPDATE_SYSUIVISIBILITY = 107;
    public static final int POLICY_UPDATE_TASK = 7;
    public static final int POLICY_UPDATE_TOP = 13;
    public static final int POLICY_UPDATE_VISIBILITY = 3;
    public static final int POLICY_UPDATE_WALLPAPER = 8;
    public static final int POLICY_USING_SYSTEM_ANIMATION = 208;
    public static final int POS_DEFAULT = 0;
    public static final int POS_FULL = 5;
    public static final int POS_MASTER = 1;
    public static final int POS_MIDDLE = 3;
    public static final int POS_NOT_IN_MAGIC_WINDOW = -1;
    public static final int POS_SINGLE = 4;
    public static final int POS_SLAVE = 2;
    public static final String RESULT_ACTIVITY_FULLSCREEN = "ACTIVITY_FULLSCREEN";
    public static final String RESULT_BOUND_IS_LEFT = "RESULT_BOUND_IS_LEFT";
    public static final String RESULT_CAN_PAUSE = "CAN_PAUSE";
    public static final String RESULT_CLEAR_TASK = "RESULT_CLEAR_TASK";
    public static final String RESULT_DETECTED = "IS_RESULT_DETECT";
    public static final String RESULT_EXITANIM_PIVOTX = "BUNDLE_EXITANIM_PIVOTX";
    public static final String RESULT_EXITANIM_PIVOTY = "BUNDLE_EXITANIM_PIVOTY";
    public static final String RESULT_EXITANIM_SCALETOX = "BUNDLE_EXITANIM_SCALETOX";
    public static final String RESULT_EXITANIM_SCALETOY = "BUNDLE_EXITANIM_SCALETOY";
    public static final String RESULT_GET_RATIO = "RESULT_GET_RATIO";
    public static final String RESULT_HWMULTISTACK = "RESULT_HWMULTISTACK";
    public static final String RESULT_IN_APP_SPLIT = "RESULT_IN_APP_SPLIT";
    public static final String RESULT_ISLEFT_INMW = "RESULT_ISLEFT_INMW";
    public static final String RESULT_ISRIGHT_INMW = "BUNDLE_ISRIGHT_INMW";
    public static final String RESULT_IS_CLEAR_ANIMATION = "BUNDLE_IS_CLEAR_ANIMATION";
    public static final String RESULT_IS_DISABLE_SENSOR = "IS_DISABLE_SENSOR";
    public static final String RESULT_IS_MAGIC_ROTATION = "magicRotation";
    public static final String RESULT_IS_NEED_SCALE = "RESULT_IS_NEED_SCALE";
    public static final String RESULT_IS_NEED_SYNC = "IS_NEED_SYNC";
    public static final String RESULT_IS_ROTATION180 = "BUNDLE_IS_ROTATION_180";
    public static final String RESULT_IS_SHOW_STATUSBAR = "IS_SHOW_STATUSBAR";
    public static final String RESULT_LEFT_MIDDLE_OFFSET = "RESULT_LEFT_MIDDLE_OFFSET";
    public static final String RESULT_LEFT_POINT = "RESULT_LEFT_POINT";
    public static final String RESULT_LEFT_RIGHT_OFFSET = "RESULT_LEFT_RIGHT_OFFSET";
    public static final String RESULT_NEED_DETECT = "NEED_HOST_DETECT";
    public static final String RESULT_NEED_SYSTEM_ANIMATION = "RESULT_NEED_SYSTEM_ANIMATION";
    public static final String RESULT_ONBACKPRESSED = "BUNDLE_RESULT_ONBACKPRESSED";
    public static final String RESULT_ORIENTATION = "BUNDLE_RESULT_ORIENTATION";
    public static final String RESULT_OVERRIDE_REAL_CONFIG = "BUNDLE_RESULT_OVERRIDE_REAL_CONFIG";
    public static final String RESULT_REJECT_ORIENTATION = "RESULT_REJECT_ORIENTATION";
    public static final String RESULT_RESIZE_WHEN_MOVEBACK = "RESULT_RESIZE_WHEN_MOVEBACK";
    public static final String RESULT_RIGHT_LEFT_OFFSET = "RESULT_RIGHT_LEFT_OFFSET";
    public static final String RESULT_SCALE_ENABLE = "RESULT_SCALE_ENABLE";
    public static final String RESULT_SPLITE_SCREEN = "RESULT_SPLITE_SCREEN";
    public static final String RESULT_STACK_VISIBILITY = "RESULT_STACK_VISIBILITY";
    public static final String RESULT_STATUSBAR = "enableStatusBar";
    public static final String RESULT_UPDATE_RESOURCE = "UPDATE_RESOURCE";
    public static final String RESULT_UPDATE_SIZE = "RESULT_UPDATE_SIZE";
    public static final String RESULT_UPDATE_SYSUIVISIBILITY = "RESULT_UPDATE_SYSUIVISIBILITY";
    public static final String RESULT_UPDATE_VISIBILITY = "BUNDLE_RESULT_UPDATE_VISIBILITY";
    public static final String RESULT_VIEW_COUNT = "VIEW_COUNT";
    public static final String RESULT_WIN_LOCATION = "RESULT_WIN_LOCATION";
    public static final int SCREEN_ORIENTATION_ERROR = -3;
    public static final int SPLITSCREEN_INVALID_TASK_ID = -1;
    public static final int SPLITSCREEN_PRIMARY_TASK_INDEX = 0;
    public static final int SPLITSCREEN_SECONDARY_TASK_INDEX = 1;
    public static final int STATUS_BAR_HANDLE_PARAM_SIZE = 2;
    public static final int SUPPORT_FOREGROUND_TASK_SIZE = 2;
    private static final String TAG = "HwMwUtils";
    public static final int TYPE_IS_BACK_TO_MIDDLE = 4;
    public static final int TYPE_SUPPORT_ANIMATION = 1;
    public static final int TYPE_SUPPORT_BACKGROUND = 2;
    public static final int TYPE_SUPPORT_OPEN_CAPABILITY = 3;
    public static final int TYPE_SUPPORT_ROUND_ANGLE = 0;
    public static final int VIEW_THRESHOLD = 70;

    public interface IPolicyOperation {
        void execute(List list, Bundle bundle);
    }

    public static boolean isInSuitableScene(boolean isSystemServer) {
        boolean isPcMode;
        if (isSystemServer) {
            isPcMode = HwPCUtils.isPcCastModeInServer();
        } else {
            isPcMode = HwPCUtils.isPcCastMode();
        }
        return !isPcMode && !IS_MMI_RUNNING;
    }

    public static Bundle performPolicy(int policy, Object... params) {
        Bundle result = null;
        HwMagicWindow hwMagicWindow = HwFrameworkFactory.getHwMagicWindow();
        if (hwMagicWindow != null) {
            result = hwMagicWindow.performHwMagicWindowPolicy(policy, params);
        }
        return result != null ? result : Bundle.EMPTY;
    }

    public static class ModulePolicy {
        protected final Map<Integer, PolicySyntax> mPolicyOpetions = new HashMap();

        /* access modifiers changed from: protected */
        public void addPolicy(int policyId, IPolicyOperation op, Class<?>... paramTypes) {
            this.mPolicyOpetions.put(Integer.valueOf(policyId), new PolicySyntax(policyId, op, paramTypes));
        }

        public void performHwMagicWindowPolicy(int policyId, List params, Bundle result) {
            PolicySyntax policy = this.mPolicyOpetions.get(Integer.valueOf(policyId));
            if (policy == null || policy.operation == null) {
                Slog.e(HwMwUtils.TAG, "Invalid policy id=" + policyId);
            } else if (!policy.checkParameters((ArrayList) params)) {
                Slog.e(HwMwUtils.TAG, "Invalid param for policy=" + policyId);
            } else {
                policy.operation.execute(params, result);
            }
        }
    }

    public static class PolicySyntax {
        public IPolicyOperation operation;
        public int paramCnt;
        public List paramTypeSet = new ArrayList();
        public int policyId;

        public PolicySyntax(int id, IPolicyOperation op, Class<?>... paramTypes) {
            this.policyId = id;
            this.operation = op;
            this.paramCnt = paramTypes.length;
            for (int i = 0; i < this.paramCnt; i++) {
                this.paramTypeSet.add(paramTypes[i]);
            }
        }

        public boolean checkParameters(ArrayList<Object> params) {
            if (params.size() != this.paramCnt) {
                Slog.e(HwMwUtils.TAG, "Cnt error:   expected cnt=" + this.paramCnt + "   actual cnt=" + params.size());
                return false;
            }
            boolean hasError = false;
            for (int i = 0; i < this.paramCnt; i++) {
                if (params.get(i) == null) {
                    Slog.w(HwMwUtils.TAG, "Param null:    index=" + i);
                } else if (!((Class) this.paramTypeSet.get(i)).isInstance(params.get(i))) {
                    Slog.e(HwMwUtils.TAG, "Type error:   index=" + i + "   expected type=" + this.paramTypeSet.get(i) + "   actual type=" + params.get(i).getClass());
                    hasError = true;
                }
            }
            if (!hasError) {
                return true;
            }
            return false;
        }
    }
}
