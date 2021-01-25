package ohos.media.common.sessioncore;

import java.util.Objects;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class AVCallerUserInfo {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AVCallerUserInfo.class);
    private final String callerPackageName;
    private final int callerPid;
    private final int callerUid;

    public AVCallerUserInfo(String str, int i, int i2) {
        if (str != null) {
            this.callerPackageName = str;
            this.callerPid = i;
            this.callerUid = i2;
            return;
        }
        LOGGER.error("callerPackageName should not be null", new Object[0]);
        throw new IllegalArgumentException("callerPackageName should not be null");
    }

    public int getCallerUid() {
        return this.callerUid;
    }

    public int getCallerPid() {
        return this.callerPid;
    }

    public String getCallerPackageName() {
        return this.callerPackageName;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof AVCallerUserInfo)) {
            LOGGER.error("AVCallerUserInfo type is incorrect", new Object[0]);
            return false;
        } else if (this == obj) {
            return true;
        } else {
            AVCallerUserInfo aVCallerUserInfo = (AVCallerUserInfo) obj;
            if (this.callerPackageName.equals(aVCallerUserInfo.getCallerPackageName()) && this.callerPid == aVCallerUserInfo.getCallerPid() && this.callerUid == aVCallerUserInfo.getCallerUid()) {
                return true;
            }
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(this.callerPackageName, Integer.valueOf(this.callerPid), Integer.valueOf(this.callerUid));
    }
}
