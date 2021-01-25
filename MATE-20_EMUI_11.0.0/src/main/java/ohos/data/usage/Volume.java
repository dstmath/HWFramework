package ohos.data.usage;

import java.io.File;
import ohos.rpc.MessageParcel;

public final class Volume {
    private final String description;
    private final boolean emulated;
    private final File internalPath;
    private final boolean pluggable;
    private final boolean primary;
    private final MountState state;
    private final String volUuid;
    private final String volumeId;
    private final File volumePath;

    public Volume(MessageParcel messageParcel) {
        this.volumeId = messageParcel.readString();
        this.volumePath = new File(messageParcel.readString());
        this.internalPath = new File(messageParcel.readString());
        this.description = messageParcel.readString();
        boolean z = true;
        this.primary = messageParcel.readInt() != 0;
        this.pluggable = messageParcel.readInt() != 0;
        this.emulated = messageParcel.readInt() == 0 ? false : z;
        this.volUuid = messageParcel.readString();
        this.state = MountState.getStatus(messageParcel.readString());
    }

    public boolean isPrimary() {
        return this.primary;
    }

    public boolean isEmulated() {
        return this.emulated;
    }

    public boolean isPluggable() {
        return this.pluggable;
    }

    public String getDescription() {
        return this.description;
    }

    public MountState getState() {
        return this.state;
    }

    public String getVolUuid() {
        return this.volUuid;
    }
}
