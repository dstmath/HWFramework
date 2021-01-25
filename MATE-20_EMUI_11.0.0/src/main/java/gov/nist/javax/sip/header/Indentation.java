package gov.nist.javax.sip.header;

import java.util.Arrays;

/* access modifiers changed from: package-private */
public class Indentation {
    private int indentation;

    protected Indentation() {
        this.indentation = 0;
    }

    protected Indentation(int initval) {
        this.indentation = initval;
    }

    /* access modifiers changed from: protected */
    public void setIndentation(int initval) {
        this.indentation = initval;
    }

    /* access modifiers changed from: protected */
    public int getCount() {
        return this.indentation;
    }

    /* access modifiers changed from: protected */
    public void increment() {
        this.indentation++;
    }

    /* access modifiers changed from: protected */
    public void decrement() {
        this.indentation--;
    }

    /* access modifiers changed from: protected */
    public String getIndentation() {
        char[] chars = new char[this.indentation];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }
}
