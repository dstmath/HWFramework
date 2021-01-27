package ohos.global.icu.impl.coll;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.MissingResourceException;
import ohos.global.icu.impl.ICUBinary;

public final class CollationRoot {
    private static final RuntimeException exception;
    private static final CollationTailoring rootSingleton;

    public static final CollationTailoring getRoot() {
        RuntimeException runtimeException = exception;
        if (runtimeException == null) {
            return rootSingleton;
        }
        throw runtimeException;
    }

    public static final CollationData getData() {
        return getRoot().data;
    }

    static final CollationSettings getSettings() {
        return getRoot().settings.readOnly();
    }

    static {
        RuntimeException e;
        CollationTailoring collationTailoring = null;
        try {
            ByteBuffer requiredData = ICUBinary.getRequiredData("coll/ucadata.icu");
            CollationTailoring collationTailoring2 = new CollationTailoring(null);
            CollationDataReader.read(null, requiredData, collationTailoring2);
            e = null;
            collationTailoring = collationTailoring2;
        } catch (IOException unused) {
            e = new MissingResourceException("IOException while reading CLDR root data", "CollationRoot", "data/icudt66b/coll/ucadata.icu");
        } catch (RuntimeException e2) {
            e = e2;
        }
        rootSingleton = collationTailoring;
        exception = e;
    }
}
