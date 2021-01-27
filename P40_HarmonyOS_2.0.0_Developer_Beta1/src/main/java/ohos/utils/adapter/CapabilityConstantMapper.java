package ohos.utils.adapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CapabilityConstantMapper {
    private static final Map<String, String> CAPABILITY_MAP = Collections.unmodifiableMap(new HashMap<String, String>() {
        /* class ohos.utils.adapter.CapabilityConstantMapper.AnonymousClass1 */
    });

    public static Optional<String> convertToFeature(String str) {
        return Optional.ofNullable(CAPABILITY_MAP.get(str));
    }

    public static Optional<String> convertToCapability(String str) {
        for (Map.Entry<String, String> entry : CAPABILITY_MAP.entrySet()) {
            if (entry.getValue().equals(str)) {
                return Optional.ofNullable(entry.getKey());
            }
        }
        return Optional.empty();
    }

    private CapabilityConstantMapper() {
    }
}
