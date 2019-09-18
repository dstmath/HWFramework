package sun.security.jca;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.List;

public class GetInstance {

    public static final class Instance {
        public final Object impl;
        public final Provider provider;

        private Instance(Provider provider2, Object impl2) {
            this.provider = provider2;
            this.impl = impl2;
        }

        public Object[] toArray() {
            return new Object[]{this.impl, this.provider};
        }
    }

    private GetInstance() {
    }

    public static Provider.Service getService(String type, String algorithm) throws NoSuchAlgorithmException {
        Provider.Service s = Providers.getProviderList().getService(type, algorithm);
        if (s != null) {
            return s;
        }
        throw new NoSuchAlgorithmException(algorithm + " " + type + " not available");
    }

    public static Provider.Service getService(String type, String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null || provider.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        Provider p = Providers.getProviderList().getProvider(provider);
        if (p != null) {
            Provider.Service s = p.getService(type, algorithm);
            if (s != null) {
                return s;
            }
            throw new NoSuchAlgorithmException("no such algorithm: " + algorithm + " for provider " + provider);
        }
        throw new NoSuchProviderException("no such provider: " + provider);
    }

    public static Provider.Service getService(String type, String algorithm, Provider provider) throws NoSuchAlgorithmException {
        if (provider != null) {
            Provider.Service s = provider.getService(type, algorithm);
            if (s != null) {
                return s;
            }
            throw new NoSuchAlgorithmException("no such algorithm: " + algorithm + " for provider " + provider.getName());
        }
        throw new IllegalArgumentException("missing provider");
    }

    public static List<Provider.Service> getServices(String type, String algorithm) {
        return Providers.getProviderList().getServices(type, algorithm);
    }

    @Deprecated
    public static List<Provider.Service> getServices(String type, List<String> algorithms) {
        return Providers.getProviderList().getServices(type, algorithms);
    }

    public static List<Provider.Service> getServices(List<ServiceId> ids) {
        return Providers.getProviderList().getServices(ids);
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm) throws NoSuchAlgorithmException {
        ProviderList list = Providers.getProviderList();
        Provider.Service firstService = list.getService(type, algorithm);
        if (firstService != null) {
            try {
                return getInstance(firstService, clazz);
            } catch (NoSuchAlgorithmException e) {
                failure = e;
                for (Provider.Service s : list.getServices(type, algorithm)) {
                    if (s != firstService) {
                        try {
                            return getInstance(s, clazz);
                        } catch (NoSuchAlgorithmException e2) {
                            failure = e2;
                        }
                    }
                }
                throw failure;
            }
        } else {
            throw new NoSuchAlgorithmException(algorithm + " " + type + " not available");
        }
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, Object param) throws NoSuchAlgorithmException {
        NoSuchAlgorithmException failure = null;
        for (Provider.Service s : getServices(type, algorithm)) {
            try {
                return getInstance(s, clazz, param);
            } catch (NoSuchAlgorithmException e) {
                failure = e;
            }
        }
        if (failure != null) {
            throw failure;
        }
        throw new NoSuchAlgorithmException(algorithm + " " + type + " not available");
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return getInstance(getService(type, algorithm, provider), clazz);
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, Object param, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        return getInstance(getService(type, algorithm, provider), clazz, param);
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, Provider provider) throws NoSuchAlgorithmException {
        return getInstance(getService(type, algorithm, provider), clazz);
    }

    public static Instance getInstance(String type, Class<?> clazz, String algorithm, Object param, Provider provider) throws NoSuchAlgorithmException {
        return getInstance(getService(type, algorithm, provider), clazz, param);
    }

    public static Instance getInstance(Provider.Service s, Class<?> clazz) throws NoSuchAlgorithmException {
        Object instance = s.newInstance(null);
        checkSuperClass(s, instance.getClass(), clazz);
        return new Instance(s.getProvider(), instance);
    }

    public static Instance getInstance(Provider.Service s, Class<?> clazz, Object param) throws NoSuchAlgorithmException {
        Object instance = s.newInstance(param);
        checkSuperClass(s, instance.getClass(), clazz);
        return new Instance(s.getProvider(), instance);
    }

    public static void checkSuperClass(Provider.Service s, Class<?> subClass, Class<?> superClass) throws NoSuchAlgorithmException {
        if (superClass != null && !superClass.isAssignableFrom(subClass)) {
            throw new NoSuchAlgorithmException("class configured for " + s.getType() + ": " + s.getClassName() + " not a " + s.getType());
        }
    }
}
