package sun.nio.ch;

class OptionKey {
    private int level;
    private int name;

    OptionKey(int level2, int name2) {
        this.level = level2;
        this.name = name2;
    }

    /* access modifiers changed from: package-private */
    public int level() {
        return this.level;
    }

    /* access modifiers changed from: package-private */
    public int name() {
        return this.name;
    }
}
