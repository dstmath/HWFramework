package ohos.com.sun.java_cup.internal.runtime;

public class Symbol {
    public int left;
    public int parse_state;
    public int right;
    public int sym;
    boolean used_by_parser;
    public Object value;

    public Symbol(int i, int i2, int i3, Object obj) {
        this(i);
        this.left = i2;
        this.right = i3;
        this.value = obj;
    }

    public Symbol(int i, Object obj) {
        this(i);
        this.left = -1;
        this.right = -1;
        this.value = obj;
    }

    public Symbol(int i, int i2, int i3) {
        this.used_by_parser = false;
        this.sym = i;
        this.left = i2;
        this.right = i3;
        this.value = null;
    }

    public Symbol(int i) {
        this(i, -1);
        this.left = -1;
        this.right = -1;
        this.value = null;
    }

    public Symbol(int i, int i2) {
        this.used_by_parser = false;
        this.sym = i;
        this.parse_state = i2;
    }

    public String toString() {
        return "#" + this.sym;
    }
}
