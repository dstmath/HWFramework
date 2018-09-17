package android.icu.impl.coll;

import android.icu.impl.ICUBinary;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.MissingResourceException;

public final class CollationRoot {
    private static final RuntimeException exception;
    private static final CollationTailoring rootSingleton;

    public static final CollationTailoring getRoot() {
        if (exception == null) {
            return rootSingleton;
        }
        throw exception;
    }

    public static final CollationData getData() {
        return getRoot().data;
    }

    static final CollationSettings getSettings() {
        return (CollationSettings) getRoot().settings.readOnly();
    }

    static {
        CollationTailoring t = null;
        RuntimeException e2 = null;
        try {
            ByteBuffer bytes = ICUBinary.getRequiredData("coll/ucadata.icu");
            CollationTailoring t2 = new CollationTailoring(null);
            CollationDataReader.read(null, bytes, t2);
            t = t2;
        } catch (IOException e) {
            e2 = new MissingResourceException("IOException while reading CLDR root data", "CollationRoot", "data/icudt58b/coll/ucadata.icu");
        } catch (RuntimeException e3) {
            e2 = e3;
        }
        rootSingleton = t;
        exception = e2;
    }
}
