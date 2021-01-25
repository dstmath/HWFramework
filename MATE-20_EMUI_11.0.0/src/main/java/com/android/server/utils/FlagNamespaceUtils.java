package com.android.server.utils;

import android.provider.DeviceConfig;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FlagNamespaceUtils {
    @VisibleForTesting
    public static final String ALL_KNOWN_NAMESPACES_FLAG = "all_known_namespaces";
    private static final String DELIMITER = ":";
    private static final int MAX_COUNTER_VALUE = 50;
    public static final String NAMESPACE_NO_PACKAGE = "no_package";
    @VisibleForTesting
    public static final String NAMESPACE_RESCUE_PARTY = "rescue_party_namespace";
    @VisibleForTesting
    public static final String RESET_PLATFORM_PACKAGE_FLAG = "reset_platform_package";
    private static int sKnownResetNamespacesFlagCounter = -1;

    public static void addToKnownResetNamespaces(List<String> namespacesList) {
        if (namespacesList != null) {
            for (String namespace : namespacesList) {
                addToKnownResetNamespaces(namespace);
            }
        }
    }

    public static void addToKnownResetNamespaces(String namespace) {
        int nextFlagCounter = incrementAndRetrieveResetNamespacesFlagCounter();
        DeviceConfig.setProperty(NAMESPACE_RESCUE_PARTY, RESET_PLATFORM_PACKAGE_FLAG + nextFlagCounter, namespace, true);
    }

    public static void resetDeviceConfig(int resetMode) {
        resetDeviceConfig(resetMode, getAllKnownDeviceConfigNamespacesList());
    }

    public static void resetDeviceConfig(int resetMode, List<String> namespacesList) {
        for (String namespace : namespacesList) {
            DeviceConfig.resetToDefaults(resetMode, namespace);
        }
        addToKnownResetNamespaces(namespacesList);
    }

    @VisibleForTesting
    public static void resetKnownResetNamespacesFlagCounterForTest() {
        sKnownResetNamespacesFlagCounter = -1;
    }

    private static List<String> getAllKnownDeviceConfigNamespacesList() {
        List<String> namespacesList = toStringList(DeviceConfig.getProperty(NAMESPACE_RESCUE_PARTY, ALL_KNOWN_NAMESPACES_FLAG));
        namespacesList.remove(NAMESPACE_RESCUE_PARTY);
        return namespacesList;
    }

    private static List<String> toStringList(String serialized) {
        if (serialized == null || serialized.length() == 0) {
            return new ArrayList();
        }
        return Arrays.asList(serialized.split(DELIMITER));
    }

    private static int incrementAndRetrieveResetNamespacesFlagCounter() {
        sKnownResetNamespacesFlagCounter++;
        if (sKnownResetNamespacesFlagCounter == 50) {
            sKnownResetNamespacesFlagCounter = 0;
        }
        return sKnownResetNamespacesFlagCounter;
    }
}
