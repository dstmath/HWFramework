package gov.nist.javax.sip.header;

import java.util.Arrays;

class Indentation {
    private int indentation;

    protected Indentation() {
        this.indentation = 0;
    }

    protected Indentation(int initval) {
        this.indentation = initval;
    }

    protected void setIndentation(int initval) {
        this.indentation = initval;
    }

    protected int getCount() {
        return this.indentation;
    }

    protected void increment() {
        this.indentation++;
    }

    protected void decrement() {
        this.indentation--;
    }

    protected String getIndentation() {
        char[] chars = new char[this.indentation];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }
}
