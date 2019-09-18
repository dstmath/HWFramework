package java.text;

final class EntryPair {
    public String entryName;
    public boolean fwd;
    public int value;

    public EntryPair(String name, int value2) {
        this(name, value2, true);
    }

    public EntryPair(String name, int value2, boolean fwd2) {
        this.entryName = name;
        this.value = value2;
        this.fwd = fwd2;
    }
}
