package android.rms.iaware.scenerecog.entity;

public class AwareSceneEntity {
    private String mActivity;
    private int mAppListVersion;
    private String mPkgName;
    private int mSceneId;
    private int mSysStatus;

    public String getPkgName() {
        return this.mPkgName;
    }

    public void setPkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public int getSceneId() {
        return this.mSceneId;
    }

    public void setSceneId(int sceneId) {
        this.mSceneId = sceneId;
    }

    public String getActivity() {
        return this.mActivity;
    }

    public void setActivity(String activity) {
        this.mActivity = activity;
    }

    public int getAppListVersion() {
        return this.mAppListVersion;
    }

    public void setAppListVersion(int appListVersion) {
        this.mAppListVersion = appListVersion;
    }

    public int getSysStatus() {
        return this.mSysStatus;
    }

    public void setSysStatus(int sysStatus) {
        this.mSysStatus = sysStatus;
    }
}
