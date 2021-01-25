package ohos.data.usage;

import ohos.rpc.MessageParcel;

public class VolumeView {
    private int blockedUserId = -1;
    private DiskView diskView = null;
    private String fsLabel;
    private String fsType;
    private String fsUuid;
    private String id;
    private String internalPath;
    private int mountFlags = 0;
    private int mountUserId = -10000;
    private String partGuid;
    private String path;
    private int state = 0;
    private int type;

    public VolumeView(MessageParcel messageParcel) {
        this.id = messageParcel.readString();
        this.type = messageParcel.readInt();
        if (messageParcel.readInt() == 1) {
            this.diskView = new DiskView(messageParcel);
        }
        this.partGuid = messageParcel.readString();
        this.mountFlags = messageParcel.readInt();
        this.mountUserId = messageParcel.readInt();
        this.blockedUserId = messageParcel.readInt();
        this.state = messageParcel.readInt();
        this.fsType = messageParcel.readString();
        this.fsUuid = messageParcel.readString();
        this.fsLabel = messageParcel.readString();
        this.path = messageParcel.readString();
        this.internalPath = messageParcel.readString();
    }

    public String getId() {
        return this.id;
    }

    public int getType() {
        return this.type;
    }

    public DiskView getDiskView() {
        return this.diskView;
    }

    public String getPartGuid() {
        return this.partGuid;
    }

    public int getMountFlags() {
        return this.mountFlags;
    }

    public int getMountUserId() {
        return this.mountUserId;
    }

    public int getBlockedUserId() {
        return this.blockedUserId;
    }

    public int getState() {
        return this.state;
    }

    public String getFsType() {
        return this.fsType;
    }

    public String getFsUuid() {
        return this.fsUuid;
    }

    public String getFsLabel() {
        return this.fsLabel;
    }

    public String getPath() {
        return this.path;
    }

    public String getInternalPath() {
        return this.internalPath;
    }
}
