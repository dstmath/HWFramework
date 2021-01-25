package com.android.server.hidata.arbitration;

public class HwPackageInfo implements Comparable {
    private int mAudioStartTimeInCell;
    private int mAudioStartTimeInWiFi;
    private int mAudioUsedTimeInCell;
    private int mAudioUsedTimeInWiFi;
    private int mLastUsedDataInCell;
    private int mLastUsedDataInWiFi;
    private String mPackageName;
    private int mStartTimeInCell;
    private int mStartTimeInWiFi;
    private int mUid;
    private int mUsedCount;
    private int mUsedDataInCell;
    private int mUsedDataInWiFi;
    private int mUsedTimeInCell;
    private int mUsedTimeInWiFi;
    private int mVideoStartTimeInCell;
    private int mVideoStartTimeInWiFi;
    private int mVideoUsedTimeInCell;
    private int mVideoUsedTimeInWiFi;

    public HwPackageInfo(String packageName, int usedCount, int uid) {
        this.mUsedCount = usedCount;
        this.mPackageName = packageName;
        this.mUid = uid;
    }

    public int getUsedTimeInWiFi() {
        return this.mUsedTimeInWiFi;
    }

    public int getUsedDataInCell() {
        return this.mUsedDataInCell;
    }

    public int getUsedDataInWiFi() {
        return this.mUsedDataInWiFi;
    }

    public int getUsedTimeInCell() {
        return this.mUsedTimeInCell;
    }

    public int getAudioUsedTimeInWiFi() {
        return this.mAudioUsedTimeInWiFi;
    }

    public int getAudioUsedTimeInCell() {
        return this.mAudioUsedTimeInCell;
    }

    public int getVideoUsedTimeInWiFi() {
        return this.mVideoUsedTimeInWiFi;
    }

    public int getVideoUsedTimeInCell() {
        return this.mVideoUsedTimeInCell;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public void setPackageName(String packageName) {
        this.mPackageName = packageName;
    }

    public void addCount() {
        this.mUsedCount++;
    }

    public int getUsedCount() {
        return this.mUsedCount;
    }

    public void setUsedCount(int usedCount) {
        this.mUsedCount = usedCount;
    }

    public int getPkgUid() {
        return this.mUid;
    }

    public void setPkgUid(int uid) {
        this.mUid = uid;
    }

    public int getLastUsedDataInWiFi() {
        return this.mLastUsedDataInWiFi;
    }

    public void setLastUsedDataInWiFi(int lastPkgUsedData) {
        this.mLastUsedDataInWiFi = lastPkgUsedData;
    }

    public int getLastUsedDataInCell() {
        return this.mLastUsedDataInCell;
    }

    public void setLastUsedDataInCell(int lastPkgUsedData) {
        this.mLastUsedDataInCell = lastPkgUsedData;
    }

    private boolean isAudioScene(int scene) {
        if (scene == 100105 || scene == 100501 || scene == 100901 || scene == 100701) {
            return true;
        }
        return false;
    }

    private boolean isVideoScene(int scene) {
        if (scene == 100106) {
            return true;
        }
        return false;
    }

    public void setVideoStartTime(int networkType, int scene, int startTime) {
        if (isAudioScene(scene)) {
            if (networkType == 800) {
                this.mAudioStartTimeInWiFi = startTime;
            } else if (networkType == 801) {
                this.mAudioStartTimeInCell = startTime;
            }
        } else if (!isVideoScene(scene)) {
        } else {
            if (networkType == 800) {
                this.mVideoStartTimeInWiFi = startTime;
            } else if (networkType == 801) {
                this.mVideoStartTimeInCell = startTime;
            }
        }
    }

    public int getVideoStartTime(int networkType, int scene) {
        if (isAudioScene(scene)) {
            if (networkType == 800) {
                return this.mAudioStartTimeInWiFi;
            }
            if (networkType == 801) {
                return this.mAudioStartTimeInCell;
            }
            return 0;
        } else if (!isVideoScene(scene)) {
            return 0;
        } else {
            if (networkType == 800) {
                return this.mVideoStartTimeInWiFi;
            }
            if (networkType == 801) {
                return this.mVideoStartTimeInCell;
            }
            return 0;
        }
    }

    public void setVideoUsedTime(int networkType, int scene, int usedTime) {
        if (isAudioScene(scene)) {
            if (networkType == 800) {
                this.mAudioUsedTimeInWiFi = usedTime;
            } else if (networkType == 801) {
                this.mAudioUsedTimeInCell = usedTime;
            }
        } else if (!isVideoScene(scene)) {
        } else {
            if (networkType == 800) {
                this.mVideoUsedTimeInWiFi = usedTime;
            } else if (networkType == 801) {
                this.mVideoUsedTimeInCell = usedTime;
            }
        }
    }

    public int getVideoUsedTime(int networkType, int scene) {
        if (isAudioScene(scene)) {
            if (networkType == 800) {
                return this.mAudioUsedTimeInWiFi;
            }
            if (networkType == 801) {
                return this.mAudioUsedTimeInCell;
            }
            return 0;
        } else if (!isVideoScene(scene)) {
            return 0;
        } else {
            if (networkType == 800) {
                return this.mVideoUsedTimeInWiFi;
            }
            if (networkType == 801) {
                return this.mVideoUsedTimeInCell;
            }
            return 0;
        }
    }

    public int getStartTime(int networkType) {
        if (networkType == 800) {
            return this.mStartTimeInWiFi;
        }
        if (networkType == 801) {
            return this.mStartTimeInCell;
        }
        return 0;
    }

    public void setStartTime(int networkType, int startTime) {
        if (networkType == 800) {
            this.mStartTimeInWiFi = startTime;
        } else if (networkType == 801) {
            this.mStartTimeInCell = startTime;
        }
    }

    public void setUsedData(int networkType, int usedTime) {
        if (networkType == 800) {
            this.mUsedDataInWiFi = usedTime;
        } else if (networkType == 801) {
            this.mUsedDataInCell = usedTime;
        }
    }

    public int getUsedData(int networkType) {
        if (networkType == 800) {
            return this.mUsedDataInWiFi;
        }
        if (networkType == 801) {
            return this.mUsedDataInCell;
        }
        return 0;
    }

    public int getUsedTime(int networkType) {
        if (networkType == 800) {
            return this.mUsedTimeInWiFi;
        }
        if (networkType == 801) {
            return this.mUsedTimeInCell;
        }
        return 0;
    }

    public void setUsedTime(int networkType, int usedTime) {
        if (networkType == 800) {
            this.mUsedTimeInWiFi = usedTime;
        } else if (networkType == 801) {
            this.mUsedTimeInCell = usedTime;
        }
    }

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        HwPackageInfo comparePkg = (HwPackageInfo) obj;
        if (!(comparePkg instanceof HwPackageInfo)) {
            return 0;
        }
        return Long.compare((long) (comparePkg.getUsedTimeInWiFi() + comparePkg.getUsedTimeInCell()), (long) (this.mUsedTimeInWiFi + this.mUsedTimeInCell));
    }

    @Override // java.lang.Object
    public String toString() {
        return "PackageInfo{mPackageName='" + this.mPackageName + "', mUsedCount=" + this.mUsedCount + ", mUsedTimeInWiFi=" + this.mUsedTimeInWiFi + ", mUsedDataInWiFi=" + this.mUsedDataInWiFi + ", mVideoUsedTimeInWiFi=" + this.mVideoUsedTimeInWiFi + ", mAudioUsedTimeInWiFi=" + this.mAudioUsedTimeInWiFi + ", mUsedTimeInCell=" + this.mUsedTimeInCell + ", mUsedDataInCell=" + this.mUsedDataInCell + ", mVideoUsedTimeInCell=" + this.mVideoUsedTimeInCell + ", mAudioUsedTimeInCell=" + this.mAudioUsedTimeInCell + '}';
    }
}
