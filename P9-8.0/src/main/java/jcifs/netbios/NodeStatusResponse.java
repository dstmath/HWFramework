package jcifs.netbios;

import java.io.UnsupportedEncodingException;

class NodeStatusResponse extends NameServicePacket {
    NbtAddress[] addressArray;
    private byte[] macAddress;
    private int numberOfNames;
    private NbtAddress queryAddress;
    private byte[] stats;

    NodeStatusResponse(NbtAddress queryAddress) {
        this.queryAddress = queryAddress;
        this.recordName = new Name();
        this.macAddress = new byte[6];
    }

    int writeBodyWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readBodyWireFormat(byte[] src, int srcIndex) {
        return readResourceRecordWireFormat(src, srcIndex);
    }

    int writeRDataWireFormat(byte[] dst, int dstIndex) {
        return 0;
    }

    int readRDataWireFormat(byte[] src, int srcIndex) {
        int start = srcIndex;
        this.numberOfNames = src[srcIndex] & 255;
        int namesLength = this.numberOfNames * 18;
        int statsLength = (this.rDataLength - namesLength) - 1;
        int srcIndex2 = srcIndex + 1;
        this.numberOfNames = src[srcIndex] & 255;
        System.arraycopy(src, srcIndex2 + namesLength, this.macAddress, 0, 6);
        srcIndex = srcIndex2 + readNodeNameArray(src, srcIndex2);
        this.stats = new byte[statsLength];
        System.arraycopy(src, srcIndex, this.stats, 0, statsLength);
        return (srcIndex + statsLength) - start;
    }

    private int readNodeNameArray(byte[] src, int srcIndex) {
        int start = srcIndex;
        this.addressArray = new NbtAddress[this.numberOfNames];
        String scope = this.queryAddress.hostName.scope;
        boolean addrFound = false;
        int i = 0;
        while (i < this.numberOfNames) {
            try {
                int j = srcIndex + 14;
                while (src[j] == (byte) 32) {
                    j--;
                }
                String str = new String(src, srcIndex, (j - srcIndex) + 1, Name.OEM_ENCODING);
                int hexCode = src[srcIndex + 15] & 255;
                boolean groupName = (src[srcIndex + 16] & 128) == 128;
                int ownerNodeType = (src[srcIndex + 16] & 96) >> 5;
                boolean isBeingDeleted = (src[srcIndex + 16] & 16) == 16;
                boolean isInConflict = (src[srcIndex + 16] & 8) == 8;
                boolean isActive = (src[srcIndex + 16] & 4) == 4;
                boolean isPermanent = (src[srcIndex + 16] & 2) == 2;
                if (!addrFound && this.queryAddress.hostName.hexCode == hexCode && (this.queryAddress.hostName == NbtAddress.UNKNOWN_NAME || this.queryAddress.hostName.name.equals(str))) {
                    if (this.queryAddress.hostName == NbtAddress.UNKNOWN_NAME) {
                        this.queryAddress.hostName = new Name(str, hexCode, scope);
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
                } else {
                    this.addressArray[i] = new NbtAddress(new Name(str, hexCode, scope), this.queryAddress.address, groupName, ownerNodeType, isBeingDeleted, isInConflict, isActive, isPermanent, this.macAddress);
                }
                srcIndex += 18;
                i++;
            } catch (UnsupportedEncodingException e) {
            }
        }
        return srcIndex - start;
    }

    public String toString() {
        return new String("NodeStatusResponse[" + super.toString() + "]");
    }
}
