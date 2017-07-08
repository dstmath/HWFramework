package java.text;

final class EntryPair {
    public String entryName;
    public boolean fwd;
    public int value;

    public EntryPair(String name, int value) {
        this(name, value, true);
    }

    public EntryPair(String name, int value, boolean fwd) {
        this.entryName = name;
        this.value = value;
        this.fwd = fwd;
    }
}
