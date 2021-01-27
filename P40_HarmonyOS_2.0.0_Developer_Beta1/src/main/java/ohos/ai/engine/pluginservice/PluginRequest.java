package ohos.ai.engine.pluginservice;

import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class PluginRequest implements Sequenceable {
    private static final int DEFAULT_INVALID_ENGINEID = 0;
    private static final int DEFAULT_VERSION = -1;
    private int engineId;
    private int version;

    public PluginRequest() {
        this(0);
    }

    public PluginRequest(int i) {
        this(i, -1);
    }

    public PluginRequest(int i, int i2) {
        this.version = -1;
        this.engineId = i;
        this.version = i2;
    }

    public int getEngineId() {
        return this.engineId;
    }

    public void setEngineId(int i) {
        this.engineId = i;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int i) {
        this.version = i;
    }

    public boolean marshalling(Parcel parcel) {
        parcel.writeInt(this.engineId);
        parcel.writeInt(this.version);
        return true;
    }

    public boolean unmarshalling(Parcel parcel) {
        this.engineId = parcel.readInt();
        this.version = parcel.readInt();
        return true;
    }
}
