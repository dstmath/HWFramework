package ohos.media.camera.mode.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ohos.media.camera.mode.adapter.key.ModeCharacteristicKey;
import ohos.media.camera.params.PropertyKey;
import ohos.utils.Pair;

public class FunctionConflictUtil {
    private static final int DEFAULT_MAP_SIZE = 16;
    private static Set<Pair<PropertyKey.Key<?>, PropertyKey.Key<?>>> conflictFunctionSet = new HashSet(16);

    static {
        conflictFunctionSet.add(new Pair<>(ModeCharacteristicKey.BEAUTY_FUNCTION, ModeCharacteristicKey.AI_MOVIE_FUNCTION));
        conflictFunctionSet.add(new Pair<>(ModeCharacteristicKey.BEAUTY_FUNCTION, ModeCharacteristicKey.FILTER_EFFECT_FUNCTION));
        conflictFunctionSet.add(new Pair<>(ModeCharacteristicKey.AI_MOVIE_FUNCTION, ModeCharacteristicKey.FILTER_EFFECT_FUNCTION));
        conflictFunctionSet.add(new Pair<>(ModeCharacteristicKey.BOKEHSPOT_FUNCTION, ModeCharacteristicKey.FAIRLIGHT_FUNCTION));
        conflictFunctionSet.add(new Pair<>(ModeCharacteristicKey.VIDEO_STABILIZATION_FUNCTION, ModeCharacteristicKey.AI_MOVIE_FUNCTION));
        conflictFunctionSet.add(new Pair<>(ModeCharacteristicKey.VIDEO_STABILIZATION_FUNCTION, ModeCharacteristicKey.FILTER_EFFECT_FUNCTION));
        conflictFunctionSet.add(new Pair<>(ModeCharacteristicKey.VIDEO_STABILIZATION_FUNCTION, ModeCharacteristicKey.BEAUTY_FUNCTION));
    }

    private FunctionConflictUtil() {
    }

    public static boolean isFunctionConflict(PropertyKey.Key<?> key, PropertyKey.Key<?> key2) {
        return conflictFunctionSet.contains(new Pair(key, key2)) || conflictFunctionSet.contains(new Pair(key2, key));
    }

    public static Set<PropertyKey.Key<?>> getConflictFunctions(List<PropertyKey.Key<?>> list) {
        if (list == null) {
            return Collections.emptySet();
        }
        HashSet hashSet = new HashSet(16);
        hashSet.addAll(list);
        HashSet hashSet2 = new HashSet(16);
        for (Pair<PropertyKey.Key<?>, PropertyKey.Key<?>> pair : conflictFunctionSet) {
            if (hashSet.contains(pair.f) && hashSet.contains(pair.s)) {
                hashSet2.add(pair.f);
                hashSet2.add(pair.s);
            }
        }
        return hashSet2;
    }
}
