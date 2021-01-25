package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;
import org.bouncycastle.util.Integers;

public class BDSStateMap implements Serializable {
    private static final long serialVersionUID = -3464451825208522308L;
    private final Map<Integer, BDS> bdsState = new TreeMap();
    private transient long maxIndex;

    BDSStateMap(long j) {
        this.maxIndex = j;
    }

    BDSStateMap(BDSStateMap bDSStateMap, long j) {
        for (Integer num : bDSStateMap.bdsState.keySet()) {
            this.bdsState.put(num, new BDS(bDSStateMap.bdsState.get(num)));
        }
        this.maxIndex = j;
    }

    BDSStateMap(XMSSMTParameters xMSSMTParameters, long j, byte[] bArr, byte[] bArr2) {
        this.maxIndex = (1 << xMSSMTParameters.getHeight()) - 1;
        for (long j2 = 0; j2 < j; j2++) {
            updateState(xMSSMTParameters, j2, bArr, bArr2);
        }
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.maxIndex = objectInputStream.available() != 0 ? objectInputStream.readLong() : 0;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeLong(this.maxIndex);
    }

    /* access modifiers changed from: package-private */
    public BDS get(int i) {
        return this.bdsState.get(Integers.valueOf(i));
    }

    public long getMaxIndex() {
        return this.maxIndex;
    }

    public boolean isEmpty() {
        return this.bdsState.isEmpty();
    }

    /* access modifiers changed from: package-private */
    public void put(int i, BDS bds) {
        this.bdsState.put(Integers.valueOf(i), bds);
    }

    /* access modifiers changed from: package-private */
    public BDS update(int i, byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        return this.bdsState.put(Integers.valueOf(i), this.bdsState.get(Integers.valueOf(i)).getNextState(bArr, bArr2, oTSHashAddress));
    }

    /* access modifiers changed from: package-private */
    public void updateState(XMSSMTParameters xMSSMTParameters, long j, byte[] bArr, byte[] bArr2) {
        XMSSParameters xMSSParameters = xMSSMTParameters.getXMSSParameters();
        int height = xMSSParameters.getHeight();
        long treeIndex = XMSSUtil.getTreeIndex(j, height);
        int leafIndex = XMSSUtil.getLeafIndex(j, height);
        OTSHashAddress oTSHashAddress = (OTSHashAddress) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withTreeAddress(treeIndex)).withOTSAddress(leafIndex).build();
        int i = (1 << height) - 1;
        if (leafIndex < i) {
            if (get(0) == null || leafIndex == 0) {
                put(0, new BDS(xMSSParameters, bArr, bArr2, oTSHashAddress));
            }
            update(0, bArr, bArr2, oTSHashAddress);
        }
        for (int i2 = 1; i2 < xMSSMTParameters.getLayers(); i2++) {
            int leafIndex2 = XMSSUtil.getLeafIndex(treeIndex, height);
            treeIndex = XMSSUtil.getTreeIndex(treeIndex, height);
            OTSHashAddress oTSHashAddress2 = (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(i2)).withTreeAddress(treeIndex)).withOTSAddress(leafIndex2).build();
            if (this.bdsState.get(Integer.valueOf(i2)) == null || XMSSUtil.isNewBDSInitNeeded(j, height, i2)) {
                this.bdsState.put(Integer.valueOf(i2), new BDS(xMSSParameters, bArr, bArr2, oTSHashAddress2));
            }
            if (leafIndex2 < i && XMSSUtil.isNewAuthenticationPathNeeded(j, height, i2)) {
                update(i2, bArr, bArr2, oTSHashAddress2);
            }
        }
    }

    public BDSStateMap withWOTSDigest(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        BDSStateMap bDSStateMap = new BDSStateMap(this.maxIndex);
        for (Integer num : this.bdsState.keySet()) {
            bDSStateMap.bdsState.put(num, this.bdsState.get(num).withWOTSDigest(aSN1ObjectIdentifier));
        }
        return bDSStateMap;
    }
}
