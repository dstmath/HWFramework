package org.bouncycastle.pqc.crypto.mceliece;

import org.bouncycastle.pqc.math.linearalgebra.GF2Matrix;

public class McEliecePublicKeyParameters extends McElieceKeyParameters {
    private GF2Matrix g;
    private int n;
    private int t;

    public McEliecePublicKeyParameters(int i, int i2, GF2Matrix gF2Matrix) {
        super(false, null);
        this.n = i;
        this.t = i2;
        this.g = new GF2Matrix(gF2Matrix);
    }

    public GF2Matrix getG() {
        return this.g;
    }

    public int getK() {
        return this.g.getNumRows();
    }

    public int getN() {
        return this.n;
    }

    public int getT() {
        return this.t;
    }
}
