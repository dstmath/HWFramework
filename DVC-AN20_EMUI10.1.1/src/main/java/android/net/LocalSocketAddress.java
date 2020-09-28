package android.net;

public class LocalSocketAddress {
    private final String name;
    private final Namespace namespace;

    public enum Namespace {
        ABSTRACT(0),
        RESERVED(1),
        FILESYSTEM(2);
        
        private int id;

        private Namespace(int id2) {
            this.id = id2;
        }

        /* access modifiers changed from: package-private */
        public int getId() {
            return this.id;
        }
    }

    public LocalSocketAddress(String name2, Namespace namespace2) {
        this.name = name2;
        this.namespace = namespace2;
    }

    public LocalSocketAddress(String name2) {
        this(name2, Namespace.ABSTRACT);
    }

    public String getName() {
        return this.name;
    }

    public Namespace getNamespace() {
        return this.namespace;
    }
}
