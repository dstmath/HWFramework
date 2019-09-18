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
    private int flag = 3;
    private int mCurAppScene = -1;
    private String pkgName;
    private int type = 1;

    public PGAction(int actionid, String pkg, String e1, String e2) {
        this.actionId = actionid;
        this.pkgName = pkg;
        this.extend1 = e1;
        this.extend2 = e2;
    }

    protected static boolean isValid(int id) {
        return (id >= 10000 && id <= 10017) || (id >= 20000 && id <= 20003) || ((id >= 30000 && id <= 30004) || (id >= 40000 && id <= 40000));
    }

    public static int checkActionFlag(int actionId2) {
        if (actionId2 == 10007 || actionId2 == 10015 || actionId2 == 10005 || actionId2 == 10015) {
            return 1;
        }
        if (actionId2 == 10017 || actionId2 == 10016 || actionId2 == 10006 || actionId2 == 10016) {
            return 2;
        }
        return 3;
    }

    public static int checkActionType(int actionId2) {
        if (actionId2 < 20000) {
            return 1;
        }
        if (actionId2 >= 20000 && actionId2 < 30000) {
            return 2;
        }
        if (actionId2 >= 30000 && actionId2 < 40000) {
            return 3;
        }
        if (actionId2 >= 40000) {
            return 4;
        }
        return 1;
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
        return super.toString() + " actionId =" + this.actionId + " pkg =" + this.pkgName + " extend1 =" + this.extend1 + " extend2 =" + this.extend2 + " flag =" + this.flag + " type =" + this.type;
    }
}
