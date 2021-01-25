package jcifs.netbios;

import java.io.UnsupportedEncodingException;

/* access modifiers changed from: package-private */
public class NodeStatusResponse extends NameServicePacket {
    NbtAddress[] addressArray;
    private byte[] macAddress = new byte[6];
    private int numberOfNames;
    private NbtAddress queryAddress;
    private byte[] stats;

    NodeStatusResponse(NbtAddress queryAddress2) {
        this.queryAddress = queryAddress2;
        this.recordName = new Name();
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int writeBodyWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int readBodyWireFormat(byte[] src, int srcIndex) {
        return readResourceRecordWireFormat(src, srcIndex);
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int writeRDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    /* access modifiers changed from: package-private */
    @Override // jcifs.netbios.NameServicePacket
    public int readRDataWireFormat(byte[] src, int srcIndex) {
        this.numberOfNames = src[srcIndex] & 255;
        int namesLength = this.numberOfNames * 18;
        int statsLength = (this.rDataLength - namesLength) - 1;
        int srcIndex2 = srcIndex + 1;
        this.numberOfNames = src[srcIndex] & 255;
        System.arraycopy(src, srcIndex2 + namesLength, this.macAddress, 0, 6);
        int srcIndex3 = srcIndex2 + readNodeNameArray(src, srcIndex2);
        this.stats = new byte[statsLength];
        System.arraycopy(src, srcIndex3, this.stats, 0, statsLength);
        return (srcIndex3 + statsLength) - srcIndex;
    }

    private int readNodeNameArray(byte[] src, int srcIndex) {
        this.addressArray = new NbtAddress[this.numberOfNames];
        String scope = this.queryAddress.hostName.scope;
        boolean addrFound = false;
        for (int i = 0; i < this.numberOfNames; i++) {
            try {
                int j = srcIndex + 14;
                while (src[j] == 32) {
                    j--;
                }
                String n = new String(src, srcIndex, (j - srcIndex) + 1, Name.OEM_ENCODING);
                int hexCode = src[srcIndex + 15] & 255;
                boolean groupName = (src[srcIndex + 16] & 128) == 128;
                int ownerNodeType = (src[srcIndex + 16] & 96) >> 5;
                boolean isBeingDeleted = (src[srcIndex + 16] & 16) == 16;
                boolean isInConflict = (src[srcIndex + 16] & 8) == 8;
                boolean isActive = (src[srcIndex + 16] & 4) == 4;
                boolean isPermanent = (src[srcIndex + 16] & 2) == 2;
                if (addrFound || this.queryAddress.hostName.hexCode != hexCode || (this.queryAddress.hostName != NbtAddress.UNKNOWN_NAME && !this.queryAddress.hostName.name.equals(n))) {
                    this.addressArray[i] = new NbtAddress(new Name(n, hexCode, scope), this.queryAddress.address, groupName, ownerNodeType, isBeingDeleted, isInConflict, isActive, isPermanent, this.macAddress);
                } else {
                    if (this.queryAddress.hostName == NbtAddress.UNKNOWN_NAME) {
                        this.queryAddress.hostName = new Name(n, hexCode, scope);
                    }
                    this.queryAddress.groupName = groupName;
                    this.queryAddress.nodeType = ownerNodeType;
                    this.queryAddress.isBeingDeleted = isBeingDeleted;
                    this.queryAddress.isInConflict = isInConflict;
                    this.queryAddress.isActive = isActive;
                    this.queryAddress.isPermanent = isPermanent;
                    this.queryAddress.macAddress = this.macAddress;
                    this.queryAddress.isDataFromNodeStatus = true;
                    addrFound = true;
                    this.addressArray[i] = this.queryAddress;
                }
                srcIndex += 18;
            } catch (UnsupportedEncodingException e) {
            }
        }
        return srcIndex - srcIndex;
    }

    @Override // jcifs.netbios.NameServicePacket
    public String toString() {
        return new String("NodeStatusResponse[" + super.toString() + "]");
    }
}
