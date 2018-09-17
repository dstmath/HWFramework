package android.sax;

class Children {
    Child[] children = new Child[16];

    static class Child extends Element {
        final int hash;
        Child next;

        Child(Element parent, String uri, String localName, int depth, int hash) {
            super(parent, uri, localName, depth);
            this.hash = hash;
        }
    }

    Children() {
    }

    Element getOrCreate(Element parent, String uri, String localName) {
        int hash = (uri.hashCode() * 31) + localName.hashCode();
        int index = hash & 15;
        Child current = this.children[index];
        if (current == null) {
            current = new Child(parent, uri, localName, parent.depth + 1, hash);
            this.children[index] = current;
            return current;
        }
        Child previous;
        do {
            if (current.hash == hash && current.uri.compareTo(uri) == 0 && current.localName.compareTo(localName) == 0) {
                return current;
            }
            previous = current;
            current = current.next;
        } while (current != null);
        current = new Child(parent, uri, localName, parent.depth + 1, hash);
        previous.next = current;
        return current;
    }

    Element get(String uri, String localName) {
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
