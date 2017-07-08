package javax.net.ssl;

import java.security.KeyStore.Builder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KeyStoreBuilderParameters implements ManagerFactoryParameters {
    private final List<Builder> parameters;

    public KeyStoreBuilderParameters(Builder builder) {
        this.parameters = Collections.singletonList((Builder) Objects.requireNonNull(builder));
    }

    public KeyStoreBuilderParameters(List<Builder> parameters) {
        if (parameters.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.parameters = Collections.unmodifiableList(new ArrayList((Collection) parameters));
    }

    public List<Builder> getParameters() {
        return this.parameters;
    }
}
