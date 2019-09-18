package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.crypto.StateAwareMessageSigner;
import org.bouncycastle.pqc.crypto.xmss.OTSHashAddress;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTSignature;
import org.bouncycastle.pqc.crypto.xmss.XMSSReducedSignature;
import org.bouncycastle.util.Arrays;

public class XMSSMTSigner implements StateAwareMessageSigner {
    private boolean hasGenerated;
    private boolean initSign;
    private XMSSMTPrivateKeyParameters nextKeyGenerator;
    private XMSSMTParameters params;
    private XMSSMTPrivateKeyParameters privateKey;
    private XMSSMTPublicKeyParameters publicKey;
    private WOTSPlus wotsPlus;
    private XMSSParameters xmssParams;

    private WOTSPlusSignature wotsSign(byte[] bArr, OTSHashAddress oTSHashAddress) {
        if (bArr.length != this.params.getDigestSize()) {
            throw new IllegalArgumentException("size of messageDigest needs to be equal to size of digest");
        } else if (oTSHashAddress != null) {
            this.wotsPlus.importKeys(this.wotsPlus.getWOTSPlusSecretKey(this.privateKey.getSecretKeySeed(), oTSHashAddress), this.privateKey.getPublicSeed());
            return this.wotsPlus.sign(bArr, oTSHashAddress);
        } else {
            throw new NullPointerException("otsHashAddress == null");
        }
    }

    public byte[] generateSignature(byte[] bArr) {
        if (bArr == null) {
            throw new NullPointerException("message == null");
        } else if (!this.initSign) {
            throw new IllegalStateException("signer not initialized for signature generation");
        } else if (this.privateKey == null) {
            throw new IllegalStateException("signing key no longer usable");
        } else if (!this.privateKey.getBDSState().isEmpty()) {
            BDSStateMap bDSState = this.privateKey.getBDSState();
            long index = this.privateKey.getIndex();
            int height = this.params.getHeight();
            int height2 = this.xmssParams.getHeight();
            if (XMSSUtil.isIndexValid(height, index)) {
                byte[] PRF = this.wotsPlus.getKhf().PRF(this.privateKey.getSecretKeyPRF(), XMSSUtil.toBytesBigEndian(index, 32));
                byte[] HMsg = this.wotsPlus.getKhf().HMsg(Arrays.concatenate(PRF, this.privateKey.getRoot(), XMSSUtil.toBytesBigEndian(index, this.params.getDigestSize())), bArr);
                XMSSMTSignature build = new XMSSMTSignature.Builder(this.params).withIndex(index).withRandom(PRF).build();
                long treeIndex = XMSSUtil.getTreeIndex(index, height2);
                int leafIndex = XMSSUtil.getLeafIndex(index, height2);
                this.wotsPlus.importKeys(new byte[this.params.getDigestSize()], this.privateKey.getPublicSeed());
                OTSHashAddress oTSHashAddress = (OTSHashAddress) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withTreeAddress(treeIndex)).withOTSAddress(leafIndex).build();
                if (bDSState.get(0) == null || leafIndex == 0) {
                    bDSState.put(0, new BDS(this.xmssParams, this.privateKey.getPublicSeed(), this.privateKey.getSecretKeySeed(), oTSHashAddress));
                }
                build.getReducedSignatures().add(new XMSSReducedSignature.Builder(this.xmssParams).withWOTSPlusSignature(wotsSign(HMsg, oTSHashAddress)).withAuthPath(bDSState.get(0).getAuthenticationPath()).build());
                long j = treeIndex;
                for (int i = 1; i < this.params.getLayers(); i++) {
                    XMSSNode root = bDSState.get(i - 1).getRoot();
                    int leafIndex2 = XMSSUtil.getLeafIndex(j, height2);
                    j = XMSSUtil.getTreeIndex(j, height2);
                    OTSHashAddress oTSHashAddress2 = (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(i)).withTreeAddress(j)).withOTSAddress(leafIndex2).build();
                    WOTSPlusSignature wotsSign = wotsSign(root.getValue(), oTSHashAddress2);
                    if (bDSState.get(i) == null || XMSSUtil.isNewBDSInitNeeded(index, height2, i)) {
                        bDSState.put(i, new BDS(this.xmssParams, this.privateKey.getPublicSeed(), this.privateKey.getSecretKeySeed(), oTSHashAddress2));
                    }
                    build.getReducedSignatures().add(new XMSSReducedSignature.Builder(this.xmssParams).withWOTSPlusSignature(wotsSign).withAuthPath(bDSState.get(i).getAuthenticationPath()).build());
                }
                this.hasGenerated = true;
                if (this.nextKeyGenerator != null) {
                    this.privateKey = this.nextKeyGenerator.getNextKey();
                    this.nextKeyGenerator = this.privateKey;
                } else {
                    this.privateKey = null;
                }
                return build.toByteArray();
            }
            throw new IllegalStateException("index out of bounds");
        } else {
            throw new IllegalStateException("not initialized");
        }
    }

    public AsymmetricKeyParameter getUpdatedPrivateKey() {
        XMSSMTPrivateKeyParameters nextKey;
        if (this.hasGenerated) {
            nextKey = this.privateKey;
            this.privateKey = null;
        } else {
            nextKey = this.nextKeyGenerator.getNextKey();
        }
        this.nextKeyGenerator = null;
        return nextKey;
    }

    public void init(boolean z, CipherParameters cipherParameters) {
        XMSSMTParameters parameters;
        if (z) {
            this.initSign = true;
            this.hasGenerated = false;
            this.privateKey = (XMSSMTPrivateKeyParameters) cipherParameters;
            this.nextKeyGenerator = this.privateKey;
            parameters = this.privateKey.getParameters();
        } else {
            this.initSign = false;
            this.publicKey = (XMSSMTPublicKeyParameters) cipherParameters;
            parameters = this.publicKey.getParameters();
        }
        this.params = parameters;
        this.xmssParams = this.params.getXMSSParameters();
        this.wotsPlus = new WOTSPlus(new WOTSPlusParameters(this.params.getDigest()));
    }

    public boolean verifySignature(byte[] bArr, byte[] bArr2) {
        if (bArr == null) {
            throw new NullPointerException("message == null");
        } else if (bArr2 == null) {
            throw new NullPointerException("signature == null");
        } else if (this.publicKey != null) {
            XMSSMTSignature build = new XMSSMTSignature.Builder(this.params).withSignature(bArr2).build();
            byte[] HMsg = this.wotsPlus.getKhf().HMsg(Arrays.concatenate(build.getRandom(), this.publicKey.getRoot(), XMSSUtil.toBytesBigEndian(build.getIndex(), this.params.getDigestSize())), bArr);
            long index = build.getIndex();
            int height = this.xmssParams.getHeight();
            long treeIndex = XMSSUtil.getTreeIndex(index, height);
            int leafIndex = XMSSUtil.getLeafIndex(index, height);
            this.wotsPlus.importKeys(new byte[this.params.getDigestSize()], this.publicKey.getPublicSeed());
            XMSSReducedSignature xMSSReducedSignature = build.getReducedSignatures().get(0);
            XMSSNode rootNodeFromSignature = XMSSVerifierUtil.getRootNodeFromSignature(this.wotsPlus, height, HMsg, xMSSReducedSignature, (OTSHashAddress) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withTreeAddress(treeIndex)).withOTSAddress(leafIndex).build(), leafIndex);
            int i = 1;
            while (i < this.params.getLayers()) {
                int leafIndex2 = XMSSUtil.getLeafIndex(treeIndex, height);
                long treeIndex2 = XMSSUtil.getTreeIndex(treeIndex, height);
                int i2 = height;
                rootNodeFromSignature = XMSSVerifierUtil.getRootNodeFromSignature(this.wotsPlus, i2, rootNodeFromSignature.getValue(), build.getReducedSignatures().get(i), (OTSHashAddress) ((OTSHashAddress.Builder) ((OTSHashAddress.Builder) new OTSHashAddress.Builder().withLayerAddress(i)).withTreeAddress(treeIndex2)).withOTSAddress(leafIndex2).build(), leafIndex2);
                i++;
                treeIndex = treeIndex2;
            }
            return Arrays.constantTimeAreEqual(rootNodeFromSignature.getValue(), this.publicKey.getRoot());
        } else {
            throw new NullPointerException("publicKey == null");
        }
    }
}
