package org.bouncycastle.pqc.crypto.gmss;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Vector;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.gmss.util.GMSSRandom;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.encoders.Hex;

public class Treehash {
    private byte[] firstNode;
    private int firstNodeHeight;
    private Vector heightOfNodes;
    private boolean isFinished;
    private boolean isInitialized;
    private int maxHeight;
    private Digest messDigestTree;
    private byte[] seedActive;
    private boolean seedInitialized;
    private byte[] seedNext;
    private int tailLength;
    private Vector tailStack;

    public Treehash(Vector vector, int i, Digest digest) {
        this.tailStack = vector;
        this.maxHeight = i;
        this.firstNode = null;
        this.isInitialized = false;
        this.isFinished = false;
        this.seedInitialized = false;
        this.messDigestTree = digest;
        this.seedNext = new byte[this.messDigestTree.getDigestSize()];
        this.seedActive = new byte[this.messDigestTree.getDigestSize()];
    }

    public Treehash(Digest digest, byte[][] bArr, int[] iArr) {
        this.messDigestTree = digest;
        this.maxHeight = iArr[0];
        this.tailLength = iArr[1];
        this.firstNodeHeight = iArr[2];
        if (iArr[3] == 1) {
            this.isFinished = true;
        } else {
            this.isFinished = false;
        }
        if (iArr[4] == 1) {
            this.isInitialized = true;
        } else {
            this.isInitialized = false;
        }
        if (iArr[5] == 1) {
            this.seedInitialized = true;
        } else {
            this.seedInitialized = false;
        }
        this.heightOfNodes = new Vector();
        for (int i = 0; i < this.tailLength; i++) {
            this.heightOfNodes.addElement(Integers.valueOf(iArr[i + 6]));
        }
        this.firstNode = bArr[0];
        this.seedActive = bArr[1];
        this.seedNext = bArr[2];
        this.tailStack = new Vector();
        for (int i2 = 0; i2 < this.tailLength; i2++) {
            this.tailStack.addElement(bArr[i2 + 3]);
        }
    }

    public void destroy() {
        this.isInitialized = false;
        this.isFinished = false;
        this.firstNode = null;
        this.tailLength = 0;
        this.firstNodeHeight = -1;
    }

    public byte[] getFirstNode() {
        return this.firstNode;
    }

    public int getFirstNodeHeight() {
        return this.firstNode == null ? this.maxHeight : this.firstNodeHeight;
    }

    public int getLowestNodeHeight() {
        return this.firstNode == null ? this.maxHeight : this.tailLength == 0 ? this.firstNodeHeight : Math.min(this.firstNodeHeight, ((Integer) this.heightOfNodes.lastElement()).intValue());
    }

    public byte[] getSeedActive() {
        return this.seedActive;
    }

    public byte[][] getStatByte() {
        byte[][] bArr = (byte[][]) Array.newInstance(byte.class, this.tailLength + 3, this.messDigestTree.getDigestSize());
        bArr[0] = this.firstNode;
        bArr[1] = this.seedActive;
        bArr[2] = this.seedNext;
        for (int i = 0; i < this.tailLength; i++) {
            bArr[i + 3] = (byte[]) this.tailStack.elementAt(i);
        }
        return bArr;
    }

    public int[] getStatInt() {
        int i = this.tailLength;
        int[] iArr = new int[(i + 6)];
        iArr[0] = this.maxHeight;
        iArr[1] = i;
        iArr[2] = this.firstNodeHeight;
        if (this.isFinished) {
            iArr[3] = 1;
        } else {
            iArr[3] = 0;
        }
        if (this.isInitialized) {
            iArr[4] = 1;
        } else {
            iArr[4] = 0;
        }
        if (this.seedInitialized) {
            iArr[5] = 1;
        } else {
            iArr[5] = 0;
        }
        for (int i2 = 0; i2 < this.tailLength; i2++) {
            iArr[i2 + 6] = ((Integer) this.heightOfNodes.elementAt(i2)).intValue();
        }
        return iArr;
    }

    public Vector getTailStack() {
        return this.tailStack;
    }

    public void initialize() {
        if (!this.seedInitialized) {
            PrintStream printStream = System.err;
            printStream.println("Seed " + this.maxHeight + " not initialized");
            return;
        }
        this.heightOfNodes = new Vector();
        this.tailLength = 0;
        this.firstNode = null;
        this.firstNodeHeight = -1;
        this.isInitialized = true;
        System.arraycopy(this.seedNext, 0, this.seedActive, 0, this.messDigestTree.getDigestSize());
    }

    public void initializeSeed(byte[] bArr) {
        System.arraycopy(bArr, 0, this.seedNext, 0, this.messDigestTree.getDigestSize());
        this.seedInitialized = true;
    }

    public void setFirstNode(byte[] bArr) {
        if (!this.isInitialized) {
            initialize();
        }
        this.firstNode = bArr;
        this.firstNodeHeight = this.maxHeight;
        this.isFinished = true;
    }

    public String toString() {
        StringBuilder sb;
        String str = "Treehash    : ";
        for (int i = 0; i < this.tailLength + 6; i++) {
            str = str + getStatInt()[i] + " ";
        }
        for (int i2 = 0; i2 < this.tailLength + 3; i2++) {
            if (getStatByte()[i2] != null) {
                sb = new StringBuilder();
                sb.append(str);
                sb.append(new String(Hex.encode(getStatByte()[i2])));
                sb.append(" ");
            } else {
                sb = new StringBuilder();
                sb.append(str);
                sb.append("null ");
            }
            str = sb.toString();
        }
        return str + "  " + this.messDigestTree.getDigestSize();
    }

    public void update(GMSSRandom gMSSRandom, byte[] bArr) {
        PrintStream printStream;
        String str;
        if (this.isFinished) {
            printStream = System.err;
            str = "No more update possible for treehash instance!";
        } else if (!this.isInitialized) {
            printStream = System.err;
            str = "Treehash instance not initialized before update";
        } else {
            byte[] bArr2 = new byte[this.messDigestTree.getDigestSize()];
            gMSSRandom.nextSeed(this.seedActive);
            if (this.firstNode == null) {
                this.firstNode = bArr;
                this.firstNodeHeight = 0;
            } else {
                int i = 0;
                while (this.tailLength > 0 && i == ((Integer) this.heightOfNodes.lastElement()).intValue()) {
                    byte[] bArr3 = new byte[(this.messDigestTree.getDigestSize() << 1)];
                    System.arraycopy(this.tailStack.lastElement(), 0, bArr3, 0, this.messDigestTree.getDigestSize());
                    Vector vector = this.tailStack;
                    vector.removeElementAt(vector.size() - 1);
                    Vector vector2 = this.heightOfNodes;
                    vector2.removeElementAt(vector2.size() - 1);
                    System.arraycopy(bArr, 0, bArr3, this.messDigestTree.getDigestSize(), this.messDigestTree.getDigestSize());
                    this.messDigestTree.update(bArr3, 0, bArr3.length);
                    bArr = new byte[this.messDigestTree.getDigestSize()];
                    this.messDigestTree.doFinal(bArr, 0);
                    i++;
                    this.tailLength--;
                }
                this.tailStack.addElement(bArr);
                this.heightOfNodes.addElement(Integers.valueOf(i));
                this.tailLength++;
                if (((Integer) this.heightOfNodes.lastElement()).intValue() == this.firstNodeHeight) {
                    byte[] bArr4 = new byte[(this.messDigestTree.getDigestSize() << 1)];
                    System.arraycopy(this.firstNode, 0, bArr4, 0, this.messDigestTree.getDigestSize());
                    System.arraycopy(this.tailStack.lastElement(), 0, bArr4, this.messDigestTree.getDigestSize(), this.messDigestTree.getDigestSize());
                    Vector vector3 = this.tailStack;
                    vector3.removeElementAt(vector3.size() - 1);
                    Vector vector4 = this.heightOfNodes;
                    vector4.removeElementAt(vector4.size() - 1);
                    this.messDigestTree.update(bArr4, 0, bArr4.length);
                    this.firstNode = new byte[this.messDigestTree.getDigestSize()];
                    this.messDigestTree.doFinal(this.firstNode, 0);
                    this.firstNodeHeight++;
                    this.tailLength = 0;
                }
            }
            if (this.firstNodeHeight == this.maxHeight) {
                this.isFinished = true;
                return;
            }
            return;
        }
        printStream.println(str);
    }

    public void updateNextSeed(GMSSRandom gMSSRandom) {
        gMSSRandom.nextSeed(this.seedNext);
    }

    public boolean wasFinished() {
        return this.isFinished;
    }

    public boolean wasInitialized() {
        return this.isInitialized;
    }
}
