package ohos.data.usage;

import ohos.rpc.MessageParcel;

public class DiskView {
    private static final int FLAG_USB = 8;
    private int flags;
    private final String id;
    private String label;
    private long size;
    private String sysPath;
    private int volumeCount;

    public DiskView(MessageParcel messageParcel) {
        this.id = messageParcel.readString();
        this.flags = messageParcel.readInt();
        this.size = messageParcel.readLong();
        this.label = messageParcel.readString();
        this.volumeCount = messageParcel.readInt();
        this.sysPath = messageParcel.readString();
    }

    public String getId() {
        return this.id;
    }

    public int getFlags() {
        return this.flags;
    }

    public long getSize() {
        return this.size;
    }

    public String getLabel() {
        return this.label;
    }

    public int getVolumeCount() {
        return this.volumeCount;
    }

    public String getSysPath() {
        return this.sysPath;
    }

    public boolean isUsb() {
        return (this.flags & 8) != 0;
    }
}
