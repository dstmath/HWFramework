package android.media.dtv;

public class TunerClass1Info {
    private int mDepth;
    private int mInfoType;
    private int mLatFlag;
    private int mLatitude;
    private int mLongiFlag;
    private int mLongitude;
    private int mOccurTime;
    private int mWarId;

    public int getInfoType() {
        return this.mInfoType;
    }

    public void setInfoType(int infoType) {
        this.mInfoType = infoType;
    }

    public int getLatFlag() {
        return this.mLatFlag;
    }

    public void setLatFlag(int latFlag) {
        this.mLatFlag = latFlag;
    }

    public int getLatitude() {
        return this.mLatitude;
    }

    public void setLatitude(int latitude) {
        this.mLatitude = latitude;
    }

    public int getLongiFlag() {
        return this.mLongiFlag;
    }

    public void setLongiFlag(int longiFlag) {
        this.mLongiFlag = longiFlag;
    }

    public int getLongitude() {
        return this.mLongitude;
    }

    public void setLongitude(int longitude) {
        this.mLongitude = longitude;
    }

    public int getDepth() {
        return this.mDepth;
    }

    public void setDepth(int depth) {
        this.mDepth = depth;
    }

    public int getOccurTime() {
        return this.mOccurTime;
    }

    public void setOccurTime(int occurTime) {
        this.mOccurTime = occurTime;
    }

    public int getWarId() {
        return this.mWarId;
    }

    public void setWarId(int warId) {
        this.mWarId = warId;
    }
}
