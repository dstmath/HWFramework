package java.time.zone;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ZoneRulesProvider {
    private static final CopyOnWriteArrayList<ZoneRulesProvider> PROVIDERS = new CopyOnWriteArrayList<>();
    private static final ConcurrentMap<String, ZoneRulesProvider> ZONES = new ConcurrentHashMap(512, 0.75f, 2);

    /* access modifiers changed from: protected */
    public abstract ZoneRules provideRules(String str, boolean z);

    /* access modifiers changed from: protected */
    public abstract NavigableMap<String, ZoneRules> provideVersions(String str);

    /* access modifiers changed from: protected */
    public abstract Set<String> provideZoneIds();

    static {
        registerProvider(new IcuZoneRulesProvider());
    }

    public static Set<String> getAvailableZoneIds() {
        return new HashSet(ZONES.keySet());
    }

    public static ZoneRules getRules(String zoneId, boolean forCaching) {
        Objects.requireNonNull(zoneId, "zoneId");
        return getProvider(zoneId).provideRules(zoneId, forCaching);
    }

    public static NavigableMap<String, ZoneRules> getVersions(String zoneId) {
        Objects.requireNonNull(zoneId, "zoneId");
        return getProvider(zoneId).provideVersions(zoneId);
    }

    private static ZoneRulesProvider getProvider(String zoneId) {
        ZoneRulesProvider provider = ZONES.get(zoneId);
        if (provider != null) {
            return provider;
        }
        if (ZONES.isEmpty()) {
            throw new ZoneRulesException("No time-zone data files registered");
        }
        throw new ZoneRulesException("Unknown time-zone ID: " + zoneId);
    }

    public static void registerProvider(ZoneRulesProvider provider) {
        Objects.requireNonNull(provider, "provider");
        registerProvider0(provider);
        PROVIDERS.add(provider);
    }

    private static void registerProvider0(ZoneRulesProvider provider) {
        for (String zoneId : provider.provideZoneIds()) {
            Objects.requireNonNull(zoneId, "zoneId");
            if (ZONES.putIfAbsent(zoneId, provider) != null) {
                throw new ZoneRulesException("Unable to register zone as one already registered with that ID: " + zoneId + ", currently loading from provider: " + provider);
            }
        }
    }

    public static boolean refresh() {
        boolean changed = false;
        Iterator<ZoneRulesProvider> it = PROVIDERS.iterator();
        while (it.hasNext()) {
            changed |= it.next().provideRefresh();
        }
        return changed;
    }

    protected ZoneRulesProvider() {
    }

    /* access modifiers changed from: protected */
    public boolean provideRefresh() {
        return false;
    }
}
