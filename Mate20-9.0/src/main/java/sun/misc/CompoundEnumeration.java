package sun.misc;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class CompoundEnumeration<E> implements Enumeration<E> {
    private Enumeration<E>[] enums;
    private int index = 0;

    public CompoundEnumeration(Enumeration<E>[] enums2) {
        this.enums = enums2;
    }

    private boolean next() {
        while (this.index < this.enums.length) {
            if (this.enums[this.index] != null && this.enums[this.index].hasMoreElements()) {
                return true;
            }
            this.index++;
        }
        return false;
    }

    public boolean hasMoreElements() {
        return next();
    }

    public E nextElement() {
        if (next()) {
            return this.enums[this.index].nextElement();
        }
        throw new NoSuchElementException();
    }
}
