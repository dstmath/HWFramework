package android.sax;

class Children {
    Child[] children = new Child[16];

    static class Child extends Element {
        final int hash;
        Child next;

        Child(Element parent, String uri, String localName, int depth, int hash2) {
            super(parent, uri, localName, depth);
            this.hash = hash2;
        }
    }

    Children() {
    }

    /* access modifiers changed from: package-private */
    public Element getOrCreate(Element parent, String uri, String localName) {
        Child previous;
        int hash = (uri.hashCode() * 31) + localName.hashCode();
        int index = hash & 15;
        Child current = this.children[index];
        if (current == null) {
            Child current2 = new Child(parent, uri, localName, parent.depth + 1, hash);
            this.children[index] = current2;
            return current2;
        }
        do {
            if (current.hash == hash && current.uri.compareTo(uri) == 0 && current.localName.compareTo(localName) == 0) {
                return current;
            }
            previous = current;
            current = current.next;
        } while (current != null);
        Child current3 = new Child(parent, uri, localName, parent.depth + 1, hash);
        previous.next = current3;
        return current3;
    }

    /* access modifiers changed from: package-private */
    public Element get(String uri, String localName) {
        int hash = (uri.hashCode() * 31) + localName.hashCode();
        Child current = this.children[hash & 15];
        if (current == null) {
            return null;
        }
        do {
            if (current.hash == hash && current.uri.compareTo(uri) == 0 && current.localName.compareTo(localName) == 0) {
                return current;
            }
            current = current.next;
        } while (current != null);
        return null;
    }
}
