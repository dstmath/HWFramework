package com.huawei.zxing.pdf417.decoder.ec;

import com.huawei.zxing.ChecksumException;

public final class ErrorCorrection {
    private final ModulusGF field = ModulusGF.PDF417_GF;

    public int decode(int[] received, int numECCodewords, int[] erasures) throws ChecksumException {
        ErrorCorrection errorCorrection = this;
        int[] iArr = received;
        int i = numECCodewords;
        int[] iArr2 = erasures;
        ModulusPoly poly = new ModulusPoly(errorCorrection.field, iArr);
        int[] S = new int[i];
        boolean error = false;
        for (int i2 = i; i2 > 0; i2--) {
            int eval = poly.evaluateAt(errorCorrection.field.exp(i2));
            S[i - i2] = eval;
            if (eval != 0) {
                error = true;
            }
        }
        int i3 = 0;
        if (!error) {
            return 0;
        }
        ModulusPoly knownErrors = errorCorrection.field.getOne();
        int length = iArr2.length;
        ModulusPoly knownErrors2 = knownErrors;
        int i4 = 0;
        while (i4 < length) {
            knownErrors2 = knownErrors2.multiply(new ModulusPoly(errorCorrection.field, new int[]{errorCorrection.field.subtract(0, errorCorrection.field.exp((iArr.length - 1) - iArr2[i4])), 1}));
            i4++;
            iArr2 = erasures;
        }
        ModulusPoly[] sigmaOmega = errorCorrection.runEuclideanAlgorithm(errorCorrection.field.buildMonomial(i, 1), new ModulusPoly(errorCorrection.field, S), i);
        ModulusPoly sigma = sigmaOmega[0];
        ModulusPoly omega = sigmaOmega[1];
        int[] errorLocations = errorCorrection.findErrorLocations(sigma);
        int[] errorMagnitudes = errorCorrection.findErrorMagnitudes(omega, sigma, errorLocations);
        while (i3 < errorLocations.length) {
            int position = (iArr.length - 1) - errorCorrection.field.log(errorLocations[i3]);
            if (position >= 0) {
                iArr[position] = errorCorrection.field.subtract(iArr[position], errorMagnitudes[i3]);
                i3++;
                errorCorrection = this;
                int i5 = numECCodewords;
            } else {
                throw ChecksumException.getChecksumInstance();
            }
        }
        return errorLocations.length;
    }

    private ModulusPoly[] runEuclideanAlgorithm(ModulusPoly a, ModulusPoly b, int R) throws ChecksumException {
        if (a.getDegree() < b.getDegree()) {
            ModulusPoly temp = a;
            a = b;
            b = temp;
        }
        ModulusPoly rLast = a;
        ModulusPoly r = b;
        ModulusPoly tLast = this.field.getZero();
        ModulusPoly t = this.field.getOne();
        while (r.getDegree() >= R / 2) {
            ModulusPoly rLastLast = rLast;
            ModulusPoly tLastLast = tLast;
            rLast = r;
            tLast = t;
            if (!rLast.isZero()) {
                r = rLastLast;
                ModulusPoly q = this.field.getZero();
                int dltInverse = this.field.inverse(rLast.getCoefficient(rLast.getDegree()));
                while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
                    int degreeDiff = r.getDegree() - rLast.getDegree();
                    int scale = this.field.multiply(r.getCoefficient(r.getDegree()), dltInverse);
                    q = q.add(this.field.buildMonomial(degreeDiff, scale));
                    r = r.subtract(rLast.multiplyByMonomial(degreeDiff, scale));
                }
                t = q.multiply(tLast).subtract(tLastLast).negative();
            } else {
                throw ChecksumException.getChecksumInstance();
            }
        }
        int sigmaTildeAtZero = t.getCoefficient(0);
        if (sigmaTildeAtZero != 0) {
            int inverse = this.field.inverse(sigmaTildeAtZero);
            return new ModulusPoly[]{t.multiply(inverse), r.multiply(inverse)};
        }
        throw ChecksumException.getChecksumInstance();
    }

    private int[] findErrorLocations(ModulusPoly errorLocator) throws ChecksumException {
        int numErrors = errorLocator.getDegree();
        int[] result = new int[numErrors];
        int e = 0;
        for (int i = 1; i < this.field.getSize() && e < numErrors; i++) {
            if (errorLocator.evaluateAt(i) == 0) {
                result[e] = this.field.inverse(i);
                e++;
            }
        }
        if (e == numErrors) {
            return result;
        }
        throw ChecksumException.getChecksumInstance();
    }

    private int[] findErrorMagnitudes(ModulusPoly errorEvaluator, ModulusPoly errorLocator, int[] errorLocations) {
        int errorLocatorDegree = errorLocator.getDegree();
        int[] formalDerivativeCoefficients = new int[errorLocatorDegree];
        for (int i = 1; i <= errorLocatorDegree; i++) {
            formalDerivativeCoefficients[errorLocatorDegree - i] = this.field.multiply(i, errorLocator.getCoefficient(i));
        }
        ModulusPoly formalDerivative = new ModulusPoly(this.field, formalDerivativeCoefficients);
        int s = errorLocations.length;
        int[] result = new int[s];
        for (int i2 = 0; i2 < s; i2++) {
            int xiInverse = this.field.inverse(errorLocations[i2]);
            result[i2] = this.field.multiply(this.field.subtract(0, errorEvaluator.evaluateAt(xiInverse)), this.field.inverse(formalDerivative.evaluateAt(xiInverse)));
        }
        return result;
    }
}
