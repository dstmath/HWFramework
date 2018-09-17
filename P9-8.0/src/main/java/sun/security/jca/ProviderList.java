package sun.security.jca;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import sun.security.util.Debug;

public final class ProviderList {
    static final ProviderList EMPTY = new ProviderList(PC0, true);
    private static final Provider EMPTY_PROVIDER = new Provider("##Empty##", 1.0d, "initialization in progress") {
        private static final long serialVersionUID = 1151354171352296389L;

        public Service getService(String type, String algorithm) {
            return null;
        }
    };
    private static final Provider[] P0 = new Provider[0];
    private static final ProviderConfig[] PC0 = new ProviderConfig[0];
    static final Debug debug = Debug.getInstance("jca", "ProviderList");
    private volatile boolean allLoaded;
    private final ProviderConfig[] configs;
    private final List<Provider> userList;

    private final class ServiceList extends AbstractList<Service> {
        private final String algorithm;
        private Service firstService;
        private final List<ServiceId> ids;
        private int providerIndex;
        private List<Service> services;
        private final String type;

        ServiceList(String type, String algorithm) {
            this.type = type;
            this.algorithm = algorithm;
            this.ids = null;
        }

        ServiceList(List<ServiceId> ids) {
            this.type = null;
            this.algorithm = null;
            this.ids = ids;
        }

        private void addService(Service s) {
            if (this.firstService == null) {
                this.firstService = s;
                return;
            }
            if (this.services == null) {
                this.services = new ArrayList(4);
                this.services.-java_util_stream_Collectors-mthref-2(this.firstService);
            }
            this.services.-java_util_stream_Collectors-mthref-2(s);
        }

        private Service tryGet(int index) {
            while (true) {
                if (index == 0 && this.firstService != null) {
                    return this.firstService;
                }
                if (this.services != null && this.services.size() > index) {
                    return (Service) this.services.get(index);
                }
                if (this.providerIndex >= ProviderList.this.configs.length) {
                    return null;
                }
                ProviderList providerList = ProviderList.this;
                int i = this.providerIndex;
                this.providerIndex = i + 1;
                Provider p = providerList.getProvider(i);
                Service s;
                if (this.type != null) {
                    s = p.getService(this.type, this.algorithm);
                    if (s != null) {
                        addService(s);
                    }
                } else {
                    for (ServiceId id : this.ids) {
                        s = p.getService(id.type, id.algorithm);
                        if (s != null) {
                            addService(s);
                        }
                    }
                }
            }
        }

        public Service get(int index) {
            Service s = tryGet(index);
            if (s != null) {
                return s;
            }
            throw new IndexOutOfBoundsException();
        }

        public int size() {
            int n = this.services != null ? this.services.size() : this.firstService != null ? 1 : 0;
            while (tryGet(n) != null) {
                n++;
            }
            return n;
        }

        public boolean isEmpty() {
            return tryGet(0) == null;
        }

        public Iterator<Service> iterator() {
            return new Iterator<Service>() {
                int index;

                public boolean hasNext() {
                    return ServiceList.this.tryGet(this.index) != null;
                }

                public Service next() {
                    Service s = ServiceList.this.tryGet(this.index);
                    if (s == null) {
                        throw new NoSuchElementException();
                    }
                    this.index++;
                    return s;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /* synthetic */ ProviderList(ProviderList -this0) {
        this();
    }

    static ProviderList fromSecurityProperties() {
        return (ProviderList) AccessController.doPrivileged(new PrivilegedAction<ProviderList>() {
            public ProviderList run() {
                return new ProviderList();
            }
        });
    }

    public static ProviderList add(ProviderList providerList, Provider p) {
        return insertAt(providerList, p, -1);
    }

    public static ProviderList insertAt(ProviderList providerList, Provider p, int position) {
        if (providerList.getProvider(p.getName()) != null) {
            return providerList;
        }
        List<ProviderConfig> list = new ArrayList(Arrays.asList(providerList.configs));
        int n = list.size();
        if (position < 0 || position > n) {
            position = n;
        }
        list.add(position, new ProviderConfig(p));
        return new ProviderList((ProviderConfig[]) list.toArray(PC0), true);
    }

    public static ProviderList remove(ProviderList providerList, String name) {
        if (providerList.getProvider(name) == null) {
            return providerList;
        }
        ProviderConfig[] configs = new ProviderConfig[(providerList.size() - 1)];
        ProviderConfig[] providerConfigArr = providerList.configs;
        int i = 0;
        int length = providerConfigArr.length;
        int j = 0;
        while (i < length) {
            int j2;
            ProviderConfig config = providerConfigArr[i];
            if (config.getProvider().getName().equals(name)) {
                j2 = j;
            } else {
                j2 = j + 1;
                configs[j] = config;
            }
            i++;
            j = j2;
        }
        return new ProviderList(configs, true);
    }

    public static ProviderList newList(Provider... providers) {
        ProviderConfig[] configs = new ProviderConfig[providers.length];
        for (int i = 0; i < providers.length; i++) {
            configs[i] = new ProviderConfig(providers[i]);
        }
        return new ProviderList(configs, true);
    }

    private ProviderList(ProviderConfig[] configs, boolean allLoaded) {
        this.userList = new AbstractList<Provider>() {
            public int size() {
                return ProviderList.this.configs.length;
            }

            public Provider get(int index) {
                return ProviderList.this.getProvider(index);
            }
        };
        this.configs = configs;
        this.allLoaded = allLoaded;
    }

    private ProviderList() {
        this.userList = /* anonymous class already generated */;
        Object configList = new ArrayList();
        int i = 1;
        while (true) {
            String entry = Security.getProperty("security.provider." + i);
            if (entry == null) {
                break;
            }
            entry = entry.trim();
            if (entry.length() == 0) {
                System.err.println("invalid entry for security.provider." + i);
                break;
            }
            ProviderConfig config;
            int k = entry.indexOf(32);
            if (k == -1) {
                config = new ProviderConfig(entry);
            } else {
                config = new ProviderConfig(entry.substring(0, k), entry.substring(k + 1).trim());
            }
            if (!configList.contains(config)) {
                configList.-java_util_stream_Collectors-mthref-2(config);
            }
            i++;
        }
        this.configs = (ProviderConfig[]) configList.toArray(PC0);
        if (debug != null) {
            debug.println("provider configuration: " + configList);
        }
    }

    ProviderList getJarList(String[] jarClassNames) {
        List<ProviderConfig> newConfigs = new ArrayList();
        for (String className : jarClassNames) {
            ProviderConfig newConfig = new ProviderConfig(className);
            for (ProviderConfig config : this.configs) {
                if (config.equals(newConfig)) {
                    newConfig = config;
                    break;
                }
            }
            newConfigs.-java_util_stream_Collectors-mthref-2(newConfig);
        }
        return new ProviderList((ProviderConfig[]) newConfigs.toArray(PC0), false);
    }

    public int size() {
        return this.configs.length;
    }

    Provider getProvider(int index) {
        Provider p = this.configs[index].getProvider();
        return p != null ? p : EMPTY_PROVIDER;
    }

    public List<Provider> providers() {
        return this.userList;
    }

    private ProviderConfig getProviderConfig(String name) {
        int index = getIndex(name);
        return index != -1 ? this.configs[index] : null;
    }

    public Provider getProvider(String name) {
        ProviderConfig config = getProviderConfig(name);
        if (config == null) {
            return null;
        }
        return config.getProvider();
    }

    public int getIndex(String name) {
        for (int i = 0; i < this.configs.length; i++) {
            if (getProvider(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private int loadAll() {
        if (this.allLoaded) {
            return this.configs.length;
        }
        if (debug != null) {
            debug.println("Loading all providers");
            new Exception("Call trace").printStackTrace();
        }
        int n = 0;
        for (ProviderConfig provider : this.configs) {
            if (provider.getProvider() != null) {
                n++;
            }
        }
        if (n == this.configs.length) {
            this.allLoaded = true;
        }
        return n;
    }

    ProviderList removeInvalid() {
        int n = loadAll();
        if (n == this.configs.length) {
            return this;
        }
        ProviderConfig[] newConfigs = new ProviderConfig[n];
        int j = 0;
        for (ProviderConfig config : this.configs) {
            if (config.isLoaded()) {
                int j2 = j + 1;
                newConfigs[j] = config;
                j = j2;
            }
        }
        return new ProviderList(newConfigs, true);
    }

    public Provider[] toArray() {
        return (Provider[]) providers().toArray(P0);
    }

    public String toString() {
        return Arrays.asList(this.configs).toString();
    }

    public Service getService(String type, String name) {
        for (int i = 0; i < this.configs.length; i++) {
            Service s = getProvider(i).getService(type, name);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    public List<Service> getServices(String type, String algorithm) {
        return new ServiceList(type, algorithm);
    }

    @Deprecated
    public List<Service> getServices(String type, List<String> algorithms) {
        List<ServiceId> ids = new ArrayList();
        for (String alg : algorithms) {
            ids.-java_util_stream_Collectors-mthref-2(new ServiceId(type, alg));
        }
        return getServices(ids);
    }

    public List<Service> getServices(List<ServiceId> ids) {
        return new ServiceList(ids);
    }
}
