package com.huawei.server.rme.hyperhold;

public final class SceneConst {

    public enum SceneEvent {
        SWITCH_TO_FOREGROUND,
        SWITCH_TO_FOREGROUND_COLD,
        SWITCH_TO_BACKGROUND,
        APP_START,
        FREEZE,
        UNFREEZE,
        PROLOAD,
        APP_START_FINISH,
        APP_EXIT,
        SCREEN_ON,
        SCREEN_OFF,
        NAP,
        PROCESS_CREATE,
        PROCESS_DIED,
        SCENE_INIT,
        APP_SWITCH_TO,
        APP_SWITCH_FROM
    }

    public static class KillCheckScene {
        private int checkKillTime = 0;
        private int level = 0;

        public KillCheckScene(int checkKillTime2, int level2) {
            this.checkKillTime = checkKillTime2;
            this.level = level2;
        }

        public int getCheckKillTime() {
            return this.checkKillTime;
        }

        public int getLevel() {
            return this.level;
        }

        public void setCheckKillTime(int checkKillTime2) {
            this.checkKillTime = checkKillTime2;
        }

        public void addCheckKillTime() {
            this.checkKillTime++;
        }
    }

    public static class ScenePara {
        private String activityName;
        private String appName;
        private SceneEvent eventType;
        private boolean isVisable;
        private int pid;
        private String testTime;
        private int uid;

        public ScenePara() {
            this("", 0, 0);
        }

        public ScenePara(String appName2) {
            this(appName2, 0, 0);
        }

        public ScenePara(String appName2, int uid2) {
            this(appName2, 0, uid2);
        }

        public ScenePara(String appName2, int pid2, int uid2) {
            this.appName = appName2;
            this.pid = pid2;
            this.uid = uid2;
            this.isVisable = true;
        }

        public String getActivityName() {
            return this.activityName;
        }

        public String getAppName() {
            return this.appName;
        }

        public int getUid() {
            return this.uid;
        }

        public int getPid() {
            return this.pid;
        }

        public boolean getIsVisable() {
            return this.isVisable;
        }

        public String getTestTime() {
            return this.testTime;
        }

        public SceneEvent getEventType() {
            return this.eventType;
        }

        public void setActivityName(String activityName2) {
            this.activityName = activityName2;
        }

        public void setEventType(SceneEvent eventType2) {
            this.eventType = eventType2;
        }
    }
}
