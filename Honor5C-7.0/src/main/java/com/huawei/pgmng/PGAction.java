package com.huawei.pgmng;

public class PGAction {
    public static final int FLAG_PARENT_SCENE = 3;
    public static final int FLAG_SUBSCENE_END = 2;
    public static final int FLAG_SUBSCENE_START = 1;
    private static final int PG_ACTION_STATE_BASE = 10000;
    private static final int PG_ACTION_STATE_TAIL = 10017;
    private static final int PG_EVENT_STATE_BASE = 40000;
    private static final int PG_EVENT_STATE_TAIL = 40000;
    public static final int PG_ID_2DGAME_FRONT = 10011;
    public static final int PG_ID_3DGAME_FRONT = 10002;
    public static final int PG_ID_ALARM_START = 20000;
    public static final int PG_ID_BATTERY_CHANGED = 20003;
    public static final int PG_ID_BROWSER_FRONT = 10001;
    public static final int PG_ID_CAMERA_END = 10017;
    public static final int PG_ID_CAMERA_FRONT = 10007;
    public static final int PG_ID_DEFAULT_FRONT = 10000;
    public static final int PG_ID_EBOOK_FRONT = 10003;
    public static final int PG_ID_GALLERY_FRONT = 10004;
    public static final int PG_ID_INPUT_END = 10006;
    public static final int PG_ID_INPUT_START = 10005;
    public static final int PG_ID_LAUNCHER_FRONT = 10010;
    public static final int PG_ID_LOG_EVENT = 40000;
    public static final int PG_ID_MMS_FRONT = 10013;
    public static final int PG_ID_OFFICE_FRONT = 10008;
    public static final int PG_ID_POWER_MODE = 20001;
    public static final int PG_ID_THERMAL_BACKLIGHT = 20002;
    public static final int PG_ID_USER_DRIVING = 30002;
    public static final int PG_ID_USER_RUNNING = 30003;
    public static final int PG_ID_USER_SLEEPING = 30000;
    public static final int PG_ID_USER_STATIONARY = 30004;
    public static final int PG_ID_USER_WALKING = 30001;
    public static final int PG_ID_VIDEO_END = 10016;
    public static final int PG_ID_VIDEO_FRONT = 10009;
    public static final int PG_ID_VIDEO_START = 10015;
    private static final int PG_SYSTEM_STATE_BASE = 20000;
    private static final int PG_SYSTEM_STATE_TAIL = 20003;
    private static final int PG_USER_STATE_BASE = 30000;
    private static final int PG_USER_STATE_TAIL = 30004;
    public static final int TYPE_APP_SCENE = 1;
    public static final int TYPE_PG_EVENT = 4;
    public static final int TYPE_SYSTEM_STATE = 2;
    public static final int TYPE_USER_SCENE = 3;
    private int actionId;
    private String extend1;
    private String extend2;
    private int flag;
    private int mCurAppScene;
    private String pkgName;
    private int type;

    public PGAction(int actionid, String pkg, String e1, String e2) {
        this.flag = TYPE_USER_SCENE;
        this.type = TYPE_APP_SCENE;
        this.mCurAppScene = -1;
        this.actionId = actionid;
        this.pkgName = pkg;
        this.extend1 = e1;
        this.extend2 = e2;
    }

    protected static boolean isValid(int id) {
        if (id >= PG_ID_DEFAULT_FRONT && id <= PG_ID_CAMERA_END) {
            return true;
        }
        if (id >= PG_SYSTEM_STATE_BASE && id <= PG_SYSTEM_STATE_TAIL) {
            return true;
        }
        if (id >= PG_USER_STATE_BASE && id <= PG_USER_STATE_TAIL) {
            return true;
        }
        if (id < PG_ID_LOG_EVENT || id > PG_ID_LOG_EVENT) {
            return false;
        }
        return true;
    }

    public static int checkActionFlag(int actionId) {
        if (actionId == PG_ID_CAMERA_FRONT || actionId == PG_ID_VIDEO_START || actionId == PG_ID_INPUT_START || actionId == PG_ID_VIDEO_START) {
            return TYPE_APP_SCENE;
        }
        if (!(actionId == PG_ID_CAMERA_END || actionId == PG_ID_VIDEO_END || actionId == PG_ID_INPUT_END)) {
            if (actionId != PG_ID_VIDEO_END) {
                return TYPE_USER_SCENE;
            }
        }
        return TYPE_SYSTEM_STATE;
    }

    public static int checkActionType(int actionId) {
        if (actionId < PG_SYSTEM_STATE_BASE) {
            return TYPE_APP_SCENE;
        }
        if (actionId >= PG_SYSTEM_STATE_BASE && actionId < PG_USER_STATE_BASE) {
            return TYPE_SYSTEM_STATE;
        }
        if (actionId >= PG_USER_STATE_BASE && actionId < PG_ID_LOG_EVENT) {
            return TYPE_USER_SCENE;
        }
        if (actionId >= PG_ID_LOG_EVENT) {
            return TYPE_PG_EVENT;
        }
        return TYPE_APP_SCENE;
    }

    public int getCurAppScene() {
        return this.mCurAppScene;
    }

    public void setCurAppScene(int id) {
        this.mCurAppScene = id;
    }

    public int getActionType() {
        return this.type;
    }

    public void setActionType(int tempType) {
        this.type = tempType;
    }

    public int getActionFlag() {
        return this.flag;
    }

    public void setActionFlag(int tempFlag) {
        this.flag = tempFlag;
    }

    public int getActionId() {
        return this.actionId;
    }

    public String getActionPkg() {
        return this.pkgName;
    }

    public String getActionExd1() {
        return this.extend1;
    }

    public String getActionExd2() {
        return this.extend2;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" actionId =").append(this.actionId);
        builder.append(" pkg =").append(this.pkgName);
        builder.append(" extend1 =").append(this.extend1);
        builder.append(" extend2 =").append(this.extend2);
        builder.append(" flag =").append(this.flag);
        builder.append(" type =").append(this.type);
        return builder.toString();
    }
}
