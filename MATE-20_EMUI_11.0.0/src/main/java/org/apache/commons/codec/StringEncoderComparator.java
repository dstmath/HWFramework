package org.apache.commons.codec;

import java.util.Comparator;

@Deprecated
public class StringEncoderComparator implements Comparator {
    private StringEncoder stringEncoder;

    public StringEncoderComparator() {
    }

    public StringEncoderComparator(StringEncoder stringEncoder2) {
        this.stringEncoder = stringEncoder2;
    }

    @Override // java.util.Comparator
    public int compare(Object o1, Object o2) {
        try {
            return ((Comparable) this.stringEncoder.encode(o1)).compareTo((Comparable) this.stringEncoder.encode(o2));
        } catch (EncoderException e) {
            return 0;
        }
    }
}
