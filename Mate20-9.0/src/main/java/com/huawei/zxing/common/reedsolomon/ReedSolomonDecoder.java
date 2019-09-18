package com.huawei.zxing.common.reedsolomon;

public final class ReedSolomonDecoder {
    private final GenericGF field;

    public ReedSolomonDecoder(GenericGF field2) {
        this.field = field2;
    }

    public void decode(int[] received, int twoS) throws ReedSolomonException {
        int i;
        int[] iArr = received;
        int i2 = twoS;
        GenericGFPoly poly = new GenericGFPoly(this.field, iArr);
        int[] syndromeCoefficients = new int[i2];
        int i3 = 0;
        boolean noError = true;
        int i4 = 0;
        while (true) {
            i = 1;
            if (i4 >= i2) {
                break;
            }
            int eval = poly.evaluateAt(this.field.exp(this.field.getGeneratorBase() + i4));
            syndromeCoefficients[(syndromeCoefficients.length - 1) - i4] = eval;
            if (eval != 0) {
                noError = false;
            }
            i4++;
        }
        if (!noError) {
            GenericGFPoly[] sigmaOmega = runEuclideanAlgorithm(this.field.buildMonomial(i2, 1), new GenericGFPoly(this.field, syndromeCoefficients), i2);
            GenericGFPoly sigma = sigmaOmega[0];
            GenericGFPoly omega = sigmaOmega[1];
            int[] errorLocations = findErrorLocations(sigma);
            int[] errorMagnitudes = findErrorMagnitudes(omega, errorLocations);
            while (i3 < errorLocations.length) {
                int position = (iArr.length - i) - this.field.log(errorLocations[i3]);
                if (position >= 0) {
                    iArr[position] = GenericGF.addOrSubtract(iArr[position], errorMagnitudes[i3]);
                    i3++;
                    i = 1;
                } else {
                    throw new ReedSolomonException("Bad error location");
                }
            }
        }
    }

    private GenericGFPoly[] runEuclideanAlgorithm(GenericGFPoly a, GenericGFPoly b, int R) throws ReedSolomonException {
        if (a.getDegree() < b.getDegree()) {
            GenericGFPoly temp = a;
            a = b;
            b = temp;
        }
        GenericGFPoly rLast = a;
        GenericGFPoly r = b;
        GenericGFPoly tLast = this.field.getZero();
        GenericGFPoly t = this.field.getOne();
        while (r.getDegree() >= R / 2) {
            GenericGFPoly rLastLast = rLast;
            GenericGFPoly tLastLast = tLast;
            rLast = r;
            tLast = t;
            if (!rLast.isZero()) {
                r = rLastLast;
                GenericGFPoly q = this.field.getZero();
                int dltInverse = this.field.inverse(rLast.getCoefficient(rLast.getDegree()));
                while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
                    int degreeDiff = r.getDegree() - rLast.getDegree();
                    int scale = this.field.multiply(r.getCoefficient(r.getDegree()), dltInverse);
                    q = q.addOrSubtract(this.field.buildMonomial(degreeDiff, scale));
                    r = r.addOrSubtract(rLast.multiplyByMonomial(degreeDiff, scale));
                }
                t = q.multiply(tLast).addOrSubtract(tLastLast);
                if (r.getDegree() >= rLast.getDegree()) {
                    throw new IllegalStateException("Division algorithm failed to reduce polynomial?");
                }
            } else {
                throw new ReedSolomonException("r_{i-1} was zero");
            }
        }
        int sigmaTildeAtZero = t.getCoefficient(0);
        if (sigmaTildeAtZero != 0) {
            int inverse = this.field.inverse(sigmaTildeAtZero);
            return new GenericGFPoly[]{t.multiply(inverse), r.multiply(inverse)};
        }
        throw new ReedSolomonException("sigmaTilde(0) was zero");
    }

    private int[] findErrorLocations(GenericGFPoly errorLocator) throws ReedSolomonException {
        int numErrors = errorLocator.getDegree();
        if (numErrors == 1) {
            return new int[]{errorLocator.getCoefficient(1)};
        }
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
        throw new ReedSolomonException("Error locator degree does not match number of roots");
    }

    private int[] findErrorMagnitudes(GenericGFPoly errorEvaluator, int[] errorLocations) {
        int s = errorLocations.length;
        int[] result = new int[s];
        for (int i = 0; i < s; i++) {
            int xiInverse = this.field.inverse(errorLocations[i]);
            int denominator = 1;
            for (int j = 0; j < s; j++) {
                if (i != j) {
                    int term = this.field.multiply(errorLocations[j], xiInverse);
                    denominator = this.field.multiply(denominator, (term & 1) == 0 ? term | 1 : term & -2);
                }
            }
            result[i] = this.field.multiply(errorEvaluator.evaluateAt(xiInverse), this.field.inverse(denominator));
            if (this.field.getGeneratorBase() != 0) {
                result[i] = this.field.multiply(result[i], xiInverse);
            }
        }
        return result;
    }
}
