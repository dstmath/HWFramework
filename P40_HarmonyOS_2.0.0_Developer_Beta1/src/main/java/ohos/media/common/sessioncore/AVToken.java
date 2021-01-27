package ohos.media.common.sessioncore;

import java.util.Objects;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class AVToken {
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVToken.class);
    private final Object object;

    public AVToken(Object obj) {
        this.object = obj;
    }

    public Object getHostAVToken() {
        return this.object;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            return this.object.equals(((AVToken) obj).object);
        }
        LOGGER.error("obj is null or obj type incorrect", new Object[0]);
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.object);
    }
}
