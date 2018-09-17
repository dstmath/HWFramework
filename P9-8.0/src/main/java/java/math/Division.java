package java.math;

class Division {
    Division() {
    }

    static int divideArrayByInt(int[] quotient, int[] dividend, int dividendLength, int divisor) {
        long rem = 0;
        long bLong = ((long) divisor) & 4294967295L;
        for (int i = dividendLength - 1; i >= 0; i--) {
            long quot;
            long temp = (rem << 32) | (((long) dividend[i]) & 4294967295L);
            if (temp >= 0) {
                quot = temp / bLong;
                rem = temp % bLong;
            } else {
                long aPos = temp >>> 1;
                long bPos = (long) (divisor >>> 1);
                quot = aPos / bPos;
                rem = ((aPos % bPos) << 1) + (1 & temp);
                if ((divisor & 1) != 0) {
                    if (quot <= rem) {
                        rem -= quot;
                    } else if (quot - rem <= bLong) {
                        rem += bLong - quot;
                        quot--;
                    } else {
                        rem += (bLong << 1) - quot;
                        quot -= 2;
                    }
                }
            }
            quotient[i] = (int) (4294967295L & quot);
        }
        return (int) rem;
    }
}
