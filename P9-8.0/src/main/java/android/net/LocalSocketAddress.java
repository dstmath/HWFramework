package android.net;

public class LocalSocketAddress {
    private final String name;
    private final Namespace namespace;

    public enum Namespace {
        ABSTRACT(0),
        RESERVED(1),
        FILESYSTEM(2);
        
        private int id;

        private Namespace(int id) {
            this.id = id;
        }

        int getId() {
            return this.id;
        }
    }

    public LocalSocketAddress(String name, Namespace namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    public LocalSocketAddress(String name) {
        this(name, Namespace.ABSTRACT);
    }

    public String getName() {
        return this.name;
    }

    public Namespace getNamespace() {
        return this.namespace;
    }
}
