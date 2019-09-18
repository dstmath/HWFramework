package org.bouncycastle.pqc.crypto.xmss;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import org.bouncycastle.pqc.crypto.xmss.HashTreeAddress;
import org.bouncycastle.pqc.crypto.xmss.LTreeAddress;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;

public final class BDS implements Serializable {
    private static final long serialVersionUID = 1;
    private List<XMSSNode> authenticationPath;
    private int index;
    private int k;
    private Map<Integer, XMSSNode> keep;
    private Map<Integer, LinkedList<XMSSNode>> retain;
    private XMSSNode root;
    private Stack<XMSSNode> stack;
    private final List<BDSTreeHash> treeHashInstances;
    private final int treeHeight;
    private boolean used;
    private transient WOTSPlus wotsPlus;

    private BDS(BDS bds, byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        this.wotsPlus = bds.wotsPlus;
        this.treeHeight = bds.treeHeight;
        this.k = bds.k;
        this.root = bds.root;
        this.authenticationPath = new ArrayList(bds.authenticationPath);
        this.retain = bds.retain;
        this.stack = (Stack) bds.stack.clone();
        this.treeHashInstances = bds.treeHashInstances;
        this.keep = new TreeMap(bds.keep);
        this.index = bds.index;
        nextAuthenticationPath(bArr, bArr2, oTSHashAddress);
        bds.used = true;
    }

    private BDS(WOTSPlus wOTSPlus, int i, int i2) {
        this.wotsPlus = wOTSPlus;
        this.treeHeight = i;
        this.k = i2;
        if (i2 <= i && i2 >= 2) {
            int i3 = i - i2;
            if (i3 % 2 == 0) {
                this.authenticationPath = new ArrayList();
                this.retain = new TreeMap();
                this.stack = new Stack<>();
                this.treeHashInstances = new ArrayList();
                for (int i4 = 0; i4 < i3; i4++) {
                    this.treeHashInstances.add(new BDSTreeHash(i4));
                }
                this.keep = new TreeMap();
                this.index = 0;
                this.used = false;
                return;
            }
        }
        throw new IllegalArgumentException("illegal value for BDS parameter k");
    }

    BDS(XMSSParameters xMSSParameters, int i) {
        this(xMSSParameters.getWOTSPlus(), xMSSParameters.getHeight(), xMSSParameters.getK());
        this.index = i;
        this.used = true;
    }

    BDS(XMSSParameters xMSSParameters, byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        this(xMSSParameters.getWOTSPlus(), xMSSParameters.getHeight(), xMSSParameters.getK());
        initialize(bArr, bArr2, oTSHashAddress);
    }

    BDS(XMSSParameters xMSSParameters, byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress, int i) {
        this(xMSSParameters.getWOTSPlus(), xMSSParameters.getHeight(), xMSSParameters.getK());
        initialize(bArr, bArr2, oTSHashAddress);
        while (this.index < i) {
            nextAuthenticationPath(bArr, bArr2, oTSHashAddress);
            this.used = false;
        }
    }

    private BDSTreeHash getBDSTreeHashInstanceForUpdate() {
        BDSTreeHash bDSTreeHash = null;
        for (BDSTreeHash next : this.treeHashInstances) {
            if (!next.isFinished() && next.isInitialized()) {
                if (bDSTreeHash == null || next.getHeight() < bDSTreeHash.getHeight() || (next.getHeight() == bDSTreeHash.getHeight() && next.getIndexLeaf() < bDSTreeHash.getIndexLeaf())) {
                    bDSTreeHash = next;
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
                this.wotsPlus.importKeys(this.wotsPlus.getWOTSPlusSecretKey(bArr2, oTSHashAddress), bArr);
                WOTSPlusPublicKeyParameters publicKey = this.wotsPlus.getPublicKey(oTSHashAddress);
                lTreeAddress = (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress.getLayerAddress())).withTreeAddress(lTreeAddress.getTreeAddress())).withLTreeAddress(i).withTreeHeight(lTreeAddress.getTreeHeight()).withTreeIndex(lTreeAddress.getTreeIndex()).withKeyAndMask(lTreeAddress.getKeyAndMask())).build();
                XMSSNode lTree = XMSSNodeUtil.lTree(this.wotsPlus, publicKey, lTreeAddress);
                hashTreeAddress = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeIndex(i).withKeyAndMask(hashTreeAddress.getKeyAndMask())).build();
                while (!this.stack.isEmpty() && this.stack.peek().getHeight() == lTree.getHeight()) {
                    int floor = (int) Math.floor((double) (i / (1 << lTree.getHeight())));
                    if (floor == 1) {
                        this.authenticationPath.add(lTree.clone());
                    }
                    if (floor == 3 && lTree.getHeight() < this.treeHeight - this.k) {
                        this.treeHashInstances.get(lTree.getHeight()).setNode(lTree.clone());
                    }
                    if (floor >= 3 && (floor & 1) == 1 && lTree.getHeight() >= this.treeHeight - this.k && lTree.getHeight() <= this.treeHeight - 2) {
                        if (this.retain.get(Integer.valueOf(lTree.getHeight())) == null) {
                            LinkedList linkedList = new LinkedList();
                            linkedList.add(lTree.clone());
                            this.retain.put(Integer.valueOf(lTree.getHeight()), linkedList);
                        } else {
                            this.retain.get(Integer.valueOf(lTree.getHeight())).add(lTree.clone());
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
        List<XMSSNode> list;
        Object removeFirst;
        if (oTSHashAddress == null) {
            throw new NullPointerException("otsHashAddress == null");
        } else if (this.used) {
            throw new IllegalStateException("index already used");
        } else if (this.index <= (1 << this.treeHeight) - 2) {
            LTreeAddress lTreeAddress = (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
            HashTreeAddress hashTreeAddress = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).build();
            int calculateTau = XMSSUtil.calculateTau(this.index, this.treeHeight);
            if (((this.index >> (calculateTau + 1)) & 1) == 0 && calculateTau < this.treeHeight - 1) {
                this.keep.put(Integer.valueOf(calculateTau), this.authenticationPath.get(calculateTau).clone());
            }
            if (calculateTau == 0) {
                oTSHashAddress = (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(oTSHashAddress.getLayerAddress())).withTreeAddress(oTSHashAddress.getTreeAddress())).withOTSAddress(this.index).withChainAddress(oTSHashAddress.getChainAddress()).withHashAddress(oTSHashAddress.getHashAddress()).withKeyAndMask(oTSHashAddress.getKeyAndMask())).build();
                this.wotsPlus.importKeys(this.wotsPlus.getWOTSPlusSecretKey(bArr2, oTSHashAddress), bArr);
                this.authenticationPath.set(0, XMSSNodeUtil.lTree(this.wotsPlus, this.wotsPlus.getPublicKey(oTSHashAddress), (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress.getLayerAddress())).withTreeAddress(lTreeAddress.getTreeAddress())).withLTreeAddress(this.index).withTreeHeight(lTreeAddress.getTreeHeight()).withTreeIndex(lTreeAddress.getTreeIndex()).withKeyAndMask(lTreeAddress.getKeyAndMask())).build()));
            } else {
                int i = calculateTau - 1;
                XMSSNode randomizeHash = XMSSNodeUtil.randomizeHash(this.wotsPlus, this.authenticationPath.get(i), this.keep.get(Integer.valueOf(i)), (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeHeight(i).withTreeIndex(this.index >> calculateTau).withKeyAndMask(hashTreeAddress.getKeyAndMask())).build());
                this.authenticationPath.set(calculateTau, new XMSSNode(randomizeHash.getHeight() + 1, randomizeHash.getValue()));
                this.keep.remove(Integer.valueOf(i));
                for (int i2 = 0; i2 < calculateTau; i2++) {
                    if (i2 < this.treeHeight - this.k) {
                        list = this.authenticationPath;
                        removeFirst = this.treeHashInstances.get(i2).getTailNode();
                    } else {
                        list = this.authenticationPath;
                        removeFirst = this.retain.get(Integer.valueOf(i2)).removeFirst();
                    }
                    list.set(i2, removeFirst);
                }
                int min = Math.min(calculateTau, this.treeHeight - this.k);
                for (int i3 = 0; i3 < min; i3++) {
                    int i4 = this.index + 1 + (3 * (1 << i3));
                    if (i4 < (1 << this.treeHeight)) {
                        this.treeHashInstances.get(i3).initialize(i4);
                    }
                }
            }
            for (int i5 = 0; i5 < ((this.treeHeight - this.k) >> 1); i5++) {
                BDSTreeHash bDSTreeHashInstanceForUpdate = getBDSTreeHashInstanceForUpdate();
                if (bDSTreeHashInstanceForUpdate != null) {
                    bDSTreeHashInstanceForUpdate.update(this.stack, this.wotsPlus, bArr, bArr2, oTSHashAddress);
                }
            }
            this.index++;
        } else {
            throw new IllegalStateException("index out of bounds");
        }
    }

    /* access modifiers changed from: protected */
    public List<XMSSNode> getAuthenticationPath() {
        ArrayList arrayList = new ArrayList();
        for (XMSSNode clone : this.authenticationPath) {
            arrayList.add(clone.clone());
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public int getIndex() {
        return this.index;
    }

    public BDS getNextState(byte[] bArr, byte[] bArr2, OTSHashAddress oTSHashAddress) {
        return new BDS(this, bArr, bArr2, oTSHashAddress);
    }

    /* access modifiers changed from: protected */
    public XMSSNode getRoot() {
        return this.root.clone();
    }

    /* access modifiers changed from: protected */
    public int getTreeHeight() {
        return this.treeHeight;
    }

    /* access modifiers changed from: package-private */
    public boolean isUsed() {
        return this.used;
    }

    /* access modifiers changed from: protected */
    public void setXMSS(XMSSParameters xMSSParameters) {
        if (this.treeHeight == xMSSParameters.getHeight()) {
            this.wotsPlus = xMSSParameters.getWOTSPlus();
            return;
        }
        throw new IllegalStateException("wrong height");
    }

    /* access modifiers changed from: protected */
    public void validate() {
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
}
