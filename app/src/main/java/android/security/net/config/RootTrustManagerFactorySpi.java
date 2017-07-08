package android.security.net.config;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

public class RootTrustManagerFactorySpi extends TrustManagerFactorySpi {
    private ApplicationConfig mApplicationConfig;
    private NetworkSecurityConfig mConfig;

    public static final class ApplicationConfigParameters implements ManagerFactoryParameters {
        public final ApplicationConfig config;

        public ApplicationConfigParameters(ApplicationConfig config) {
            this.config = config;
        }
    }

    public void engineInit(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        if (spec instanceof ApplicationConfigParameters) {
            this.mApplicationConfig = ((ApplicationConfigParameters) spec).config;
            return;
        }
        throw new InvalidAlgorithmParameterException("Unsupported spec: " + spec + ". Only " + ApplicationConfigParameters.class.getName() + " supported");
    }

    public void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks != null) {
            this.mApplicationConfig = new ApplicationConfig(new KeyStoreConfigSource(ks));
        } else {
            this.mApplicationConfig = ApplicationConfig.getDefaultInstance();
        }
    }

    public TrustManager[] engineGetTrustManagers() {
        if (this.mApplicationConfig == null) {
            throw new IllegalStateException("TrustManagerFactory not initialized");
        }
        return new TrustManager[]{this.mApplicationConfig.getTrustManager()};
    }
}
