package org.bouncycastle.pqc.crypto.xmss;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.pqc.crypto.xmss.HashTreeAddress;
import org.bouncycastle.pqc.crypto.xmss.LTreeAddress;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;

public final class BDS implements Serializable {
    private static final long serialVersionUID = 1;
    private List<XMSSNode> authenticationPath;
    private int index;
    private int k;
    private Map<Integer, XMSSNode> keep;
    private transient int maxIndex;
    private Map<Integer, LinkedList<XMSSNode>> retain;
    private XMSSNode root;
    private Stack<XMSSNode> stack;
    private final List<BDSTreeHash> treeHashInstances;
    private final int treeHeight;
    private boolean used;
    private transient WOTSPlus wotsPlus;

    BDS(BDS bds) {
        this.wotsPlus = new WOTSPlus(bds.wotsPlus.getParams());
        this.treeHeight = bds.treeHeight;
        this.k = bds.k;
        this.root = bds.root;
        this.authenticationPath = new ArrayList();
        this.authenticationPath.addAll(bds.authenticationPath);
        this.retain = new TreeMap();
        for (Integer num : bds.retain.keySet()) {
            this.retain.put(num, (LinkedList) bds.retain.get(num).clone());
        }
        this.stack = new Stack<>();
        this.stack.addAll(bds.stack);
        this.treeHashInstances = new ArrayList();
        for (BDSTreeHash bDSTreeHash : bds.treeHashInstances) {
            this.treeHashInstances.add(bDSTreeHash.clone());
        }
        this.keep = new TreeMap(bds.keep);
        this.index = bds.index;
        this.maxIndex = bds.maxIndex;
        this.used = bds.used;
    }

    private BDS(BDS bds, int i, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.wotsPlus = new WOTSPlus(new WOTSPlusParameters(aSN1ObjectIdentifier));
        this.treeHeight = bds.treeHeight;
        this.k = bds.k;
        this.root = bds.root;
        this.authenticationPath = new ArrayList();
        this.authenticationPath.addAll(bds.authenticationPath);
        this.retain = new TreeMap();
        for (Integer num : bds.retain.keySet()) {
            this.retain.put(num, (LinkedList) bds.retain.get(num).clone());
        }
        this.stack = new Stack<>();
        this.stack.addAll(bds.stack);
        this.treeHashInstances = new ArrayList();
        for (BDSTreeHash bDSTreeHash : bds.treeHashInstances) {
            this.treeHashInstances.add(bDSTreeHash.clone());
        }
        this.keep = new TreeMap(bds.keep);
        this.index = bds.index;
        this.maxIndex = i;
        this.used = bds.used;
        validate();
    }

    private BDS(BDS bds, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        this.wotsPlus = new WOTSPlus(new WOTSPlusParameters(aSN1ObjectIdentifier));
        this.treeHeight = bds.treeHeight;
        this.k = bds.k;
        this.root = bds.root;
        this.authenticationPath = new ArrayList();
        this.authenticationPath.addAll(bds.authenticationPath);
        this.retain = new TreeMap();
        for (Integer num : bds.retain.keySet()) {
            this.retain.put(num, (LinkedList) bds.retain.get(num).clone());
        }
        this.stack = new Stack<>();
        this.stack.addAll(bds.stack);
        this.treeHashInstances = new ArrayList();
        for (BDSTreeHash bDSTreeHash : bds.treeHashInstances) {
            this.treeHashInstances.add(bDSTreeHash.clone());
        }
        this.keep = new TreeMap(bds.keep);
        this.index = bds.index;
        this.maxIndex = bds.maxIndex;
        this.used = bds.used;
        validate();
    }

    private BDS(BDS bds, byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        this.wotsPlus = new WOTSPlus(bds.wotsPlus.getParams());
        this.treeHeight = bds.treeHeight;
        this.k = bds.k;
        this.root = bds.root;
        this.authenticationPath = new ArrayList();
        this.authenticationPath.addAll(bds.authenticationPath);
        this.retain = new TreeMap();
        for (Integer num : bds.retain.keySet()) {
            this.retain.put(num, (LinkedList) bds.retain.get(num).clone());
        }
        this.stack = new Stack<>();
        this.stack.addAll(bds.stack);
        this.treeHashInstances = new ArrayList();
        for (BDSTreeHash bDSTreeHash : bds.treeHashInstances) {
            this.treeHashInstances.add(bDSTreeHash.clone());
        }
        this.keep = new TreeMap(bds.keep);
        this.index = bds.index;
        this.maxIndex = bds.maxIndex;
        this.used = false;
        nextAuthenticationPath(bArr, bArr2, oTSHashAddress);
    }

    private BDS(WOTSPlus wOTSPlus, int i, int i2, int i3) {
        this.wotsPlus = wOTSPlus;
        this.treeHeight = i;
        this.maxIndex = i3;
        this.k = i2;
        if (i2 <= i && i2 >= 2) {
            int i4 = i - i2;
            if (i4 % 2 == 0) {
                this.authenticationPath = new ArrayList();
                this.retain = new TreeMap();
                this.stack = new Stack<>();
                this.treeHashInstances = new ArrayList();
                for (int i5 = 0; i5 < i4; i5++) {
                    this.treeHashInstances.add(new BDSTreeHash(i5));
                }
                this.keep = new TreeMap();
                this.index = 0;
                this.used = false;
                return;
            }
        }
        throw new IllegalArgumentException("illegal value for BDS parameter k");
    }

    BDS(XMSSParameters xMSSParameters, int i, int i2) {
        this(xMSSParameters.getWOTSPlus(), xMSSParameters.getHeight(), xMSSParameters.getK(), i2);
        this.maxIndex = i;
        this.index = i2;
        this.used = true;
    }

    BDS(XMSSParameters xMSSParameters, byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        this(xMSSParameters.getWOTSPlus(), xMSSParameters.getHeight(), xMSSParameters.getK(), (1 << xMSSParameters.getHeight()) - 1);
        initialize(bArr, bArr2, oTSHashAddress);
    }

    BDS(XMSSParameters xMSSParameters, byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress, int i) {
        this(xMSSParameters.getWOTSPlus(), xMSSParameters.getHeight(), xMSSParameters.getK(), (1 << xMSSParameters.getHeight()) - 1);
        initialize(bArr, bArr2, oTSHashAddress);
        while (this.index < i) {
            nextAuthenticationPath(bArr, bArr2, oTSHashAddress);
            this.used = false;
        }
    }

    private BDSTreeHash getBDSTreeHashInstanceForUpdate() {
        BDSTreeHash bDSTreeHash = null;
        for (BDSTreeHash bDSTreeHash2 : this.treeHashInstances) {
            if (!bDSTreeHash2.isFinished() && bDSTreeHash2.isInitialized()) {
                if (bDSTreeHash == null || bDSTreeHash2.getHeight() < bDSTreeHash.getHeight() || (bDSTreeHash2.getHeight() == bDSTreeHash.getHeight() && bDSTreeHash2.getIndexLeaf() < bDSTreeHash.getIndexLeaf())) {
                    bDSTreeHash = bDSTreeHash2;
                }
            }
        }
        return bDSTreeHash;
    }

    private void initialize(byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        if (oTSHashAddress != null) {
            LTreeAddress lTreeAddress = (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
            HashTreeAddress hashTreeAddress = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
            for (int i = 0; i < (1 << this.treeHeight); i++) {
                oTSHashAddress = (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).withOTSAddress(i).withChainAddress(oTSHashAddress.getChainAddress()).withHashAddress(oTSHashAddress.getHashAddress()).withKeyAndMask(oTSHashAddress.getKeyAndMask())).build();
                WOTSPlus wOTSPlus = this.wotsPlus;
                wOTSPlus.importKeys(wOTSPlus.getWOTSPlusSecretKey(bArr2, oTSHashAddress), bArr);
                WOTSPlusPublicKeyParameters publicKey = this.wotsPlus.getPublicKey(oTSHashAddress);
                lTreeAddress = (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress.getLayerAddress())).withTreeAddress(lTreeAddress.getTreeAddress())).withLTreeAddress(i).withTreeHeight(lTreeAddress.getTreeHeight()).withTreeIndex(lTreeAddress.getTreeIndex()).withKeyAndMask(lTreeAddress.getKeyAndMask())).build();
                XMSSNode lTree = XMSSNodeUtil.lTree(this.wotsPlus, publicKey, lTreeAddress);
                hashTreeAddress = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeIndex(i).withKeyAndMask(hashTreeAddress.getKeyAndMask())).build();
                while (!this.stack.isEmpty() && this.stack.peek().getHeight() == lTree.getHeight()) {
                    int height = i / (1 << lTree.getHeight());
                    if (height == 1) {
                        this.authenticationPath.add(lTree);
                    }
                    if (height == 3 && lTree.getHeight() < this.treeHeight - this.k) {
                        this.treeHashInstances.get(lTree.getHeight()).setNode(lTree);
                    }
                    if (height >= 3 && (height & 1) == 1 && lTree.getHeight() >= this.treeHeight - this.k && lTree.getHeight() <= this.treeHeight - 2) {
                        if (this.retain.get(Integer.valueOf(lTree.getHeight())) == null) {
                            LinkedList<XMSSNode> linkedList = new LinkedList<>();
                            linkedList.add(lTree);
                            this.retain.put(Integer.valueOf(lTree.getHeight()), linkedList);
                        } else {
                            this.retain.get(Integer.valueOf(lTree.getHeight())).add(lTree);
                        }
                    }
                    HashTreeAddress hashTreeAddress2 = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeHeight(hashTreeAddress.getTreeHeight()).withTreeIndex((hashTreeAddress.getTreeIndex() - 1) / 2).withKeyAndMask(hashTreeAddress.getKeyAndMask())).build();
                    XMSSNode randomizeHash = XMSSNodeUtil.randomizeHash(this.wotsPlus, this.stack.pop(), lTree, hashTreeAddress2);
                    XMSSNode xMSSNode = new XMSSNode(randomizeHash.getHeight() + 1, randomizeHash.getValue());
                    hashTreeAddress = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress2.getLayerAddress())).withTreeAddress(hashTreeAddress2.getTreeAddress())).withTreeHeight(hashTreeAddress2.getTreeHeight() + 1).withTreeIndex(hashTreeAddress2.getTreeIndex()).withKeyAndMask(hashTreeAddress2.getKeyAndMask())).build();
                    lTree = xMSSNode;
                }
                this.stack.push(lTree);
            }
            this.root = this.stack.pop();
            return;
        }
        throw new NullPointerException("otsHashAddress == null");
    }

    private void nextAuthenticationPath(byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        XMSSNode xMSSNode;
        List<XMSSNode> list;
        if (oTSHashAddress == null) {
            throw new NullPointerException("otsHashAddress == null");
        } else if (!this.used) {
            int i = this.index;
            if (i <= this.maxIndex - 1) {
                int calculateTau = XMSSUtil.calculateTau(i, this.treeHeight);
                if (((this.index >> (calculateTau + 1)) & 1) == 0 && calculateTau < this.treeHeight - 1) {
                    this.keep.put(Integer.valueOf(calculateTau), this.authenticationPath.get(calculateTau));
                }
                LTreeAddress lTreeAddress = (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
                HashTreeAddress hashTreeAddress = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
                if (calculateTau == 0) {
                    oTSHashAddress = (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).withOTSAddress(this.index).withChainAddress(oTSHashAddress.getChainAddress()).withHashAddress(oTSHashAddress.getHashAddress()).withKeyAndMask(oTSHashAddress.getKeyAndMask())).build();
                    WOTSPlus wOTSPlus = this.wotsPlus;
                    wOTSPlus.importKeys(wOTSPlus.getWOTSPlusSecretKey(bArr2, oTSHashAddress), bArr);
                    this.authenticationPath.set(0, XMSSNodeUtil.lTree(this.wotsPlus, this.wotsPlus.getPublicKey(oTSHashAddress), (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress.getLayerAddress())).withTreeAddress(lTreeAddress.getTreeAddress())).withLTreeAddress(this.index).withTreeHeight(lTreeAddress.getTreeHeight()).withTreeIndex(lTreeAddress.getTreeIndex()).withKeyAndMask(lTreeAddress.getKeyAndMask())).build()));
                } else {
                    int i2 = calculateTau - 1;
                    WOTSPlus wOTSPlus2 = this.wotsPlus;
                    wOTSPlus2.importKeys(wOTSPlus2.getWOTSPlusSecretKey(bArr2, oTSHashAddress), bArr);
                    XMSSNode randomizeHash = XMSSNodeUtil.randomizeHash(this.wotsPlus, this.authenticationPath.get(i2), this.keep.get(Integer.valueOf(i2)), (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeHeight(i2).withTreeIndex(this.index >> calculateTau).withKeyAndMask(hashTreeAddress.getKeyAndMask())).build());
                    this.authenticationPath.set(calculateTau, new XMSSNode(randomizeHash.getHeight() + 1, randomizeHash.getValue()));
                    this.keep.remove(Integer.valueOf(i2));
                    for (int i3 = 0; i3 < calculateTau; i3++) {
                        if (i3 < this.treeHeight - this.k) {
                            list = this.authenticationPath;
                            xMSSNode = this.treeHashInstances.get(i3).getTailNode();
                        } else {
                            list = this.authenticationPath;
                            xMSSNode = this.retain.get(Integer.valueOf(i3)).removeFirst();
                        }
                        list.set(i3, xMSSNode);
                    }
                    int min = Math.min(calculateTau, this.treeHeight - this.k);
                    for (int i4 = 0; i4 < min; i4++) {
                        int i5 = this.index + 1 + ((1 << i4) * 3);
                        if (i5 < (1 << this.treeHeight)) {
                            this.treeHashInstances.get(i4).initialize(i5);
                        }
                    }
                }
                for (int i6 = 0; i6 < ((this.treeHeight - this.k) >> 1); i6++) {
                    BDSTreeHash bDSTreeHashInstanceForUpdate = getBDSTreeHashInstanceForUpdate();
                    if (bDSTreeHashInstanceForUpdate != null) {
                        bDSTreeHashInstanceForUpdate.update(this.stack, this.wotsPlus, bArr, bArr2, oTSHashAddress);
                    }
                }
                this.index++;
                return;
            }
            throw new IllegalStateException("index out of bounds");
        } else {
            throw new IllegalStateException("index already used");
        }
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.maxIndex = objectInputStream.available() != 0 ? objectInputStream.readInt() : (1 << this.treeHeight) - 1;
        int i = this.maxIndex;
        if (i > (1 << this.treeHeight) - 1 || this.index > i + 1 || objectInputStream.available() != 0) {
            throw new IOException("inconsistent BDS data detected");
        }
    }

    private void validate() {
        if (this.authenticationPath == null) {
            throw new IllegalStateException("authenticationPath == null");
        } else if (this.retain == null) {
            throw new IllegalStateException("retain == null");
        } else if (this.stack == null) {
            throw new IllegalStateException("stack == null");
        } else if (this.treeHashInstances == null) {
            throw new IllegalStateException("treeHashInstances == null");
        } else if (this.keep == null) {
            throw new IllegalStateException("keep == null");
        } else if (!XMSSUtil.isIndexValid(this.treeHeight, (long) this.index)) {
            throw new IllegalStateException("index in BDS state out of bounds");
        }
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeInt(this.maxIndex);
    }

    /* access modifiers changed from: protected */
    public List<XMSSNode> getAuthenticationPath() {
        ArrayList arrayList = new ArrayList();
        for (XMSSNode xMSSNode : this.authenticationPath) {
            arrayList.add(xMSSNode);
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public int getIndex() {
        return this.index;
    }

    public int getMaxIndex() {
        return this.maxIndex;
    }

    public BDS getNextState(byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        return new BDS(this, bArr, bArr2, oTSHashAddress);
    }

    /* access modifiers changed from: protected */
    public XMSSNode getRoot() {
        return this.root;
    }

    /* access modifiers changed from: protected */
    public int getTreeHeight() {
        return this.treeHeight;
    }

    /* access modifiers changed from: package-private */
    public boolean isUsed() {
        return this.used;
    }

    /* access modifiers changed from: package-private */
    public void markUsed() {
        this.used = true;
    }

    public BDS withMaxIndex(int i, ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return new BDS(this, i, aSN1ObjectIdentifier);
    }

    public BDS withWOTSDigest(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return new BDS(this, aSN1ObjectIdentifier);
    }
}
