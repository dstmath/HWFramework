package com.android.internal.telephony.protobuf.nano;

import java.util.HashMap;
import java.util.Map;

public final class MapFactories {
    private static volatile MapFactory mapFactory = new DefaultMapFactory();

    private static class DefaultMapFactory implements MapFactory {
        private DefaultMapFactory() {
        }

        public <K, V> Map<K, V> forMap(Map<K, V> oldMap) {
            if (oldMap == null) {
                return new HashMap();
            }
            return oldMap;
        }
    }

    public interface MapFactory {
        <K, V> Map<K, V> forMap(Map<K, V> map);
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
