package org.bouncycastle.tsp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.util.Arrays;

public class DataGroup {
    private List<byte[]> dataObjects;
    private byte[] groupHash;
    private TreeSet<byte[]> hashes;

    /* access modifiers changed from: private */
    public class ByteArrayComparator implements Comparator {
        private ByteArrayComparator() {
        }

        @Override // java.util.Comparator
        public int compare(Object obj, Object obj2) {
            byte[] bArr = (byte[]) obj;
            byte[] bArr2 = (byte[]) obj2;
            int length = bArr.length < bArr2.length ? bArr.length : bArr2.length;
            for (int i = 0; i != length; i++) {
                int i2 = bArr[i] & 255;
                int i3 = bArr2[i] & 255;
                if (i2 != i3) {
                    return i2 - i3;
                }
            }
            return bArr.length - bArr2.length;
        }
    }

    public DataGroup(List<byte[]> list) {
        this.dataObjects = list;
    }

    public DataGroup(byte[] bArr) {
        this.dataObjects = new ArrayList();
        this.dataObjects.add(bArr);
    }

    static byte[] calcDigest(DigestCalculator digestCalculator, byte[] bArr) {
        try {
            OutputStream outputStream = digestCalculator.getOutputStream();
            outputStream.write(bArr);
            outputStream.close();
            return digestCalculator.getDigest();
        } catch (IOException e) {
            throw new IllegalStateException("digest calculator failure: " + e.getMessage());
        }
    }

    private TreeSet<byte[]> getHashes(DigestCalculator digestCalculator, byte[] bArr) {
        if (this.hashes == null) {
            this.hashes = new TreeSet<>(new ByteArrayComparator());
            for (int i = 0; i != this.dataObjects.size(); i++) {
                TreeSet<byte[]> treeSet = this.hashes;
                byte[] calcDigest = calcDigest(digestCalculator, this.dataObjects.get(i));
                if (bArr != null) {
                    calcDigest = calcDigest(digestCalculator, Arrays.concatenate(calcDigest, bArr));
                }
                treeSet.add(calcDigest);
            }
        }
        return this.hashes;
    }

    public byte[] getHash(DigestCalculator digestCalculator) {
        byte[] bArr;
        if (this.groupHash == null) {
            TreeSet<byte[]> hashes2 = getHashes(digestCalculator);
            if (hashes2.size() > 1) {
                byte[] bArr2 = new byte[0];
                Iterator<byte[]> it = hashes2.iterator();
                while (it.hasNext()) {
                    bArr2 = Arrays.concatenate(bArr2, it.next());
                }
                bArr = calcDigest(digestCalculator, bArr2);
            } else {
                bArr = hashes2.first();
            }
            this.groupHash = bArr;
        }
        return this.groupHash;
    }

    public TreeSet<byte[]> getHashes(DigestCalculator digestCalculator) {
        return getHashes(digestCalculator, null);
    }
}
