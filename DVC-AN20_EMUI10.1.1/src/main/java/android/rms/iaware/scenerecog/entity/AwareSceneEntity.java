package android.rms.iaware.scenerecog.entity;

public class AwareSceneEntity {
    private String activity;
    private int appListVersion;
    private String pkgName;
    private int sceneId;
    private int sysStatus;

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName2) {
        this.pkgName = pkgName2;
    }

    public int getSceneId() {
        return this.sceneId;
    }

    public void setSceneId(int sceneId2) {
        this.sceneId = sceneId2;
    }

    public String getActivity() {
        return this.activity;
    }

    public void setActivity(String activity2) {
        this.activity = activity2;
    }

    public int getAppListVersion() {
        return this.appListVersion;
    }

    public void setAppListVersion(int appListVersion2) {
        this.appListVersion = appListVersion2;
    }

    public int getSysStatus() {
        return this.sysStatus;
    }

    public void setSysStatus(int sysStatus2) {
        this.sysStatus = sysStatus2;
    }
}
