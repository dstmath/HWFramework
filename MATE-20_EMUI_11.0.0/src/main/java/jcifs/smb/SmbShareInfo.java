package jcifs.smb;

import jcifs.util.Hexdump;

public class SmbShareInfo implements FileEntry {
    protected String netName;
    protected String remark;
    protected int type;

    public SmbShareInfo() {
    }

    public SmbShareInfo(String netName2, int type2, String remark2) {
        this.netName = netName2;
        this.type = type2;
        this.remark = remark2;
    }

    @Override // jcifs.smb.FileEntry
    public String getName() {
        return this.netName;
    }

    @Override // jcifs.smb.FileEntry
    public int getType() {
        switch (this.type & 65535) {
            case 1:
                return 32;
            case 2:
            default:
                return 8;
            case 3:
                return 16;
        }
    }

    @Override // jcifs.smb.FileEntry
    public int getAttributes() {
        return 17;
    }

    @Override // jcifs.smb.FileEntry
    public long createTime() {
        return 0;
    }

    @Override // jcifs.smb.FileEntry
    public long lastModified() {
        return 0;
    }

    @Override // jcifs.smb.FileEntry
    public long length() {
        return 0;
    }

    public boolean equals(Object obj) {
        if (obj instanceof SmbShareInfo) {
            return this.netName.equals(((SmbShareInfo) obj).netName);
        }
        return false;
    }

    public int hashCode() {
        return this.netName.hashCode();
    }

    public String toString() {
        return new String("SmbShareInfo[netName=" + this.netName + ",type=0x" + Hexdump.toHexString(this.type, 8) + ",remark=" + this.remark + "]");
    }
}
