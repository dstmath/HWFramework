package javax.net.ssl;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KeyStoreBuilderParameters implements ManagerFactoryParameters {
    private final List<KeyStore.Builder> parameters;

    public KeyStoreBuilderParameters(KeyStore.Builder builder) {
        this.parameters = Collections.singletonList((KeyStore.Builder) Objects.requireNonNull(builder));
    }

    public KeyStoreBuilderParameters(List<KeyStore.Builder> parameters2) {
        if (!parameters2.isEmpty()) {
            this.parameters = Collections.unmodifiableList(new ArrayList(parameters2));
            return;
        }
        throw new IllegalArgumentException();
    }

    public List<KeyStore.Builder> getParameters() {
        return this.parameters;
    }
}
