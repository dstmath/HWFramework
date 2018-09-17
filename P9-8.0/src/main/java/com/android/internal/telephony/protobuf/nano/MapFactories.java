package com.android.internal.telephony.protobuf.nano;

import java.util.HashMap;
import java.util.Map;

public final class MapFactories {
    private static volatile MapFactory mapFactory = new DefaultMapFactory();

    public interface MapFactory {
        <K, V> Map<K, V> forMap(Map<K, V> map);
    }

    private static class DefaultMapFactory implements MapFactory {
        /* synthetic */ DefaultMapFactory(DefaultMapFactory -this0) {
            this();
        }

        private DefaultMapFactory() {
        }

        public <K, V> Map<K, V> forMap(Map<K, V> oldMap) {
            if (oldMap == null) {
                return new HashMap();
            }
            return oldMap;
        }
    }

    static void setMapFactory(MapFactory newMapFactory) {
        mapFactory = newMapFactory;
    }

    public static MapFactory getMapFactory() {
        return mapFactory;
    }

    private MapFactories() {
    }
}
