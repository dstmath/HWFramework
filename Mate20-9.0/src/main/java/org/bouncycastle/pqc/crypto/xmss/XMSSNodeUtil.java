package org.bouncycastle.pqc.crypto.xmss;

import org.bouncycastle.pqc.crypto.xmss.HashTreeAddress;
import org.bouncycastle.pqc.crypto.xmss.LTreeAddress;

class XMSSNodeUtil {
    XMSSNodeUtil() {
    }

    static XMSSNode lTree(WOTSPlus wOTSPlus, WOTSPlusPublicKeyParameters wOTSPlusPublicKeyParameters, LTreeAddress lTreeAddress) {
        double d;
        if (wOTSPlusPublicKeyParameters == null) {
            throw new NullPointerException("publicKey == null");
        } else if (lTreeAddress != null) {
            int len = wOTSPlus.getParams().getLen();
            byte[][] byteArray = wOTSPlusPublicKeyParameters.toByteArray();
            XMSSNode[] xMSSNodeArr = new XMSSNode[byteArray.length];
            for (int i = 0; i < byteArray.length; i++) {
                xMSSNodeArr[i] = new XMSSNode(0, byteArray[i]);
            }
            LTreeAddress.Builder withTreeIndex = ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress.getLayerAddress())).withTreeAddress(lTreeAddress.getTreeAddress())).withLTreeAddress(lTreeAddress.getLTreeAddress()).withTreeHeight(0).withTreeIndex(lTreeAddress.getTreeIndex());
            int keyAndMask = lTreeAddress.getKeyAndMask();
            while (true) {
                LTreeAddress lTreeAddress2 = (LTreeAddress) ((LTreeAddress.Builder) withTreeIndex.withKeyAndMask(keyAndMask)).build();
                if (len <= 1) {
                    return xMSSNodeArr[0];
                }
                LTreeAddress lTreeAddress3 = lTreeAddress2;
                int i2 = 0;
                while (true) {
                    d = (double) (len / 2);
                    if (i2 >= ((int) Math.floor(d))) {
                        break;
                    }
                    lTreeAddress3 = (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress3.getLayerAddress())).withTreeAddress(lTreeAddress3.getTreeAddress())).withLTreeAddress(lTreeAddress3.getLTreeAddress()).withTreeHeight(lTreeAddress3.getTreeHeight()).withTreeIndex(i2).withKeyAndMask(lTreeAddress3.getKeyAndMask())).build();
                    int i3 = 2 * i2;
                    xMSSNodeArr[i2] = randomizeHash(wOTSPlus, xMSSNodeArr[i3], xMSSNodeArr[i3 + 1], lTreeAddress3);
                    i2++;
                }
                if (len % 2 == 1) {
                    xMSSNodeArr[(int) Math.floor(d)] = xMSSNodeArr[len - 1];
                }
                len = (int) Math.ceil(((double) len) / 2.0d);
                withTreeIndex = ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress3.getLayerAddress())).withTreeAddress(lTreeAddress3.getTreeAddress())).withLTreeAddress(lTreeAddress3.getLTreeAddress()).withTreeHeight(lTreeAddress3.getTreeHeight() + 1).withTreeIndex(lTreeAddress3.getTreeIndex());
                keyAndMask = lTreeAddress3.getKeyAndMask();
            }
        } else {
            throw new NullPointerException("address == null");
        }
    }

    static XMSSNode randomizeHash(WOTSPlus wOTSPlus, XMSSNode xMSSNode, XMSSNode xMSSNode2, XMSSAddress xMSSAddress) {
        if (xMSSNode == null) {
            throw new NullPointerException("left == null");
        } else if (xMSSNode2 == null) {
            throw new NullPointerException("right == null");
        } else if (xMSSNode.getHeight() != xMSSNode2.getHeight()) {
            throw new IllegalStateException("height of both nodes must be equal");
        } else if (xMSSAddress != null) {
            byte[] publicSeed = wOTSPlus.getPublicSeed();
            if (xMSSAddress instanceof LTreeAddress) {
                LTreeAddress lTreeAddress = (LTreeAddress) xMSSAddress;
                xMSSAddress = (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress.getLayerAddress())).withTreeAddress(lTreeAddress.getTreeAddress())).withLTreeAddress(lTreeAddress.getLTreeAddress()).withTreeHeight(lTreeAddress.getTreeHeight()).withTreeIndex(lTreeAddress.getTreeIndex()).withKeyAndMask(0)).build();
            } else if (xMSSAddress instanceof HashTreeAddress) {
                HashTreeAddress hashTreeAddress = (HashTreeAddress) xMSSAddress;
                xMSSAddress = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress.getLayerAddress())).withTreeAddress(hashTreeAddress.getTreeAddress())).withTreeHeight(hashTreeAddress.getTreeHeight()).withTreeIndex(hashTreeAddress.getTreeIndex()).withKeyAndMask(0)).build();
            }
            byte[] PRF = wOTSPlus.getKhf().PRF(publicSeed, xMSSAddress.toByteArray());
            if (xMSSAddress instanceof LTreeAddress) {
                LTreeAddress lTreeAddress2 = (LTreeAddress) xMSSAddress;
                xMSSAddress = (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress2.getLayerAddress())).withTreeAddress(lTreeAddress2.getTreeAddress())).withLTreeAddress(lTreeAddress2.getLTreeAddress()).withTreeHeight(lTreeAddress2.getTreeHeight()).withTreeIndex(lTreeAddress2.getTreeIndex()).withKeyAndMask(1)).build();
            } else if (xMSSAddress instanceof HashTreeAddress) {
                HashTreeAddress hashTreeAddress2 = (HashTreeAddress) xMSSAddress;
                xMSSAddress = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress2.getLayerAddress())).withTreeAddress(hashTreeAddress2.getTreeAddress())).withTreeHeight(hashTreeAddress2.getTreeHeight()).withTreeIndex(hashTreeAddress2.getTreeIndex()).withKeyAndMask(1)).build();
            }
            byte[] PRF2 = wOTSPlus.getKhf().PRF(publicSeed, xMSSAddress.toByteArray());
            if (xMSSAddress instanceof LTreeAddress) {
                LTreeAddress lTreeAddress3 = (LTreeAddress) xMSSAddress;
                xMSSAddress = (LTreeAddress) ((LTreeAddress.Builder) ((LTreeAddress.Builder) ((LTreeAddress.Builder) new LTreeAddress.Builder().withLayerAddress(lTreeAddress3.getLayerAddress())).withTreeAddress(lTreeAddress3.getTreeAddress())).withLTreeAddress(lTreeAddress3.getLTreeAddress()).withTreeHeight(lTreeAddress3.getTreeHeight()).withTreeIndex(lTreeAddress3.getTreeIndex()).withKeyAndMask(2)).build();
            } else if (xMSSAddress instanceof HashTreeAddress) {
                HashTreeAddress hashTreeAddress3 = (HashTreeAddress) xMSSAddress;
                xMSSAddress = (HashTreeAddress) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) ((HashTreeAddress.Builder) new HashTreeAddress.Builder().withLayerAddress(hashTreeAddress3.getLayerAddress())).withTreeAddress(hashTreeAddress3.getTreeAddress())).withTreeHeight(hashTreeAddress3.getTreeHeight()).withTreeIndex(hashTreeAddress3.getTreeIndex()).withKeyAndMask(2)).build();
            }
            byte[] PRF3 = wOTSPlus.getKhf().PRF(publicSeed, xMSSAddress.toByteArray());
            int digestSize = wOTSPlus.getParams().getDigestSize();
            byte[] bArr = new byte[(2 * digestSize)];
            for (int i = 0; i < digestSize; i++) {
                bArr[i] = (byte) (xMSSNode.getValue()[i] ^ PRF2[i]);
            }
            for (int i2 = 0; i2 < digestSize; i2++) {
                bArr[i2 + digestSize] = (byte) (xMSSNode2.getValue()[i2] ^ PRF3[i2]);
            }
            return new XMSSNode(xMSSNode.getHeight(), wOTSPlus.getKhf().H(PRF, bArr));
        } else {
            throw new NullPointerException("address == null");
        }
    }
}
