package org.bouncycastle.pqc.crypto.xmss;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;
import org.bouncycastle.util.Integers;

public class BDSStateMap implements Serializable {
    private final Map<Integer, BDS> bdsState = new TreeMap();

    BDSStateMap() {
    }

    BDSStateMap(BDSStateMap bDSStateMap, XMSSMTParameters xMSSMTParameters, long j, byte[] bArr, byte[] bArr2) {
        for (Integer next : bDSStateMap.bdsState.keySet()) {
            this.bdsState.put(next, bDSStateMap.bdsState.get(next));
        }
        updateState(xMSSMTParameters, j, bArr, bArr2);
    }

    BDSStateMap(XMSSMTParameters xMSSMTParameters, long j, byte[] bArr, byte[] bArr2) {
        for (long j2 = 0; j2 < j; j2++) {
            updateState(xMSSMTParameters, j2, bArr, bArr2);
        }
    }

    private void updateState(XMSSMTParameters xMSSMTParameters, long j, byte[] bArr, byte[] bArr2) {
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
            if (leafIndex2 < i && XMSSUtil.isNewAuthenticationPathNeeded(j, height, i2)) {
                if (get(i2) == null) {
                    put(i2, new BDS(xMSSMTParameters.getXMSSParameters(), bArr, bArr2, oTSHashAddress2));
                }
                update(i2, bArr, bArr2, oTSHashAddress2);
            }
        }
    }

    public BDS get(int i) {
        return this.bdsState.get(Integers.valueOf(i));
    }

    public boolean isEmpty() {
        return this.bdsState.isEmpty();
    }

    public void put(int i, BDS bds) {
        this.bdsState.put(Integers.valueOf(i), bds);
    }

    /* access modifiers changed from: package-private */
    public void setXMSS(XMSSParameters xMSSParameters) {
        for (Integer num : this.bdsState.keySet()) {
            BDS bds = this.bdsState.get(num);
            bds.setXMSS(xMSSParameters);
            bds.validate();
        }
    }

    public BDS update(int i, byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        return this.bdsState.put(Integers.valueOf(i), this.bdsState.get(Integers.valueOf(i)).getNextState(bArr, bArr2, oTSHashAddress));
    }
}
