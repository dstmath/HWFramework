package android.net.shared;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public final class ParcelableUtil {
    public static <ParcelableType, BaseType> ParcelableType[] toParcelableArray(Collection<BaseType> base, Function<BaseType, ParcelableType> conv, Class<ParcelableType> parcelClass) {
        ParcelableType[] out = (ParcelableType[]) ((Object[]) Array.newInstance((Class<?>) parcelClass, base.size()));
        int i = 0;
        for (BaseType b : base) {
            out[i] = conv.apply(b);
            i++;
        }
        return out;
    }

    public static <ParcelableType, BaseType> ArrayList<BaseType> fromParcelableArray(ParcelableType[] parceled, Function<ParcelableType, BaseType> conv) {
        ArrayList<BaseType> out = new ArrayList<>(parceled.length);
        for (ParcelableType t : parceled) {
            out.add(conv.apply(t));
        }
        return out;
    }
}
