package android.view.autofill;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public final class Helper {
    public static boolean sDebug = false;
    public static boolean sVerbose = false;

    public static void appendRedacted(StringBuilder builder, CharSequence value) {
        builder.append(getRedacted(value));
    }

    public static String getRedacted(CharSequence value) {
        if (value == null) {
            return "null";
        }
        return value.length() + "_chars";
    }

    public static void appendRedacted(StringBuilder builder, String[] values) {
        if (values == null) {
            builder.append("N/A");
            return;
        }
        builder.append("[");
        for (String value : values) {
            builder.append(" '");
            appendRedacted(builder, value);
            builder.append("'");
        }
        builder.append(" ]");
    }

    public static AutofillId[] toArray(Collection<AutofillId> collection) {
        if (collection == null) {
            return new AutofillId[0];
        }
        AutofillId[] array = new AutofillId[collection.size()];
        collection.toArray(array);
        return array;
    }

    public static <T> ArrayList<T> toList(Set<T> set) {
        if (set == null) {
            return null;
        }
        return new ArrayList<>(set);
    }

    private Helper() {
        throw new UnsupportedOperationException("contains static members only");
    }
}
