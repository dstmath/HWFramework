package android.debug;

import java.io.File;

public abstract class AdbManagerInternal {
    public abstract File getAdbKeysFile();

    public abstract File getAdbTempKeysFile();

    public abstract boolean isAdbDisabledByDevicePolicy();

    public abstract boolean isAdbEnabled();

    public abstract void registerTransport(IAdbTransport iAdbTransport);

    public abstract void unregisterTransport(IAdbTransport iAdbTransport);
}
