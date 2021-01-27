package android.provider;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.internal.util.ArrayUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;

public class SettingsStringUtil {
    public static final String DELIMITER = ":";

    private SettingsStringUtil() {
    }

    public static abstract class ColonDelimitedSet<T> extends HashSet<T> {
        /* access modifiers changed from: protected */
        public abstract T itemFromString(String str);

        public ColonDelimitedSet(String colonSeparatedItems) {
            for (String cn : TextUtils.split(TextUtils.emptyIfNull(colonSeparatedItems), SettingsStringUtil.DELIMITER)) {
                add(itemFromString(cn));
            }
        }

        /* access modifiers changed from: protected */
        public String itemToString(T item) {
            return String.valueOf(item);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: android.provider.SettingsStringUtil$ColonDelimitedSet<T> */
        /* JADX WARN: Multi-variable type inference failed */
        @Override // java.util.AbstractCollection, java.lang.Object
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Iterator it = iterator();
            if (it.hasNext()) {
                sb.append(itemToString(it.next()));
                while (it.hasNext()) {
                    sb.append(SettingsStringUtil.DELIMITER);
                    sb.append(itemToString(it.next()));
                }
            }
            return sb.toString();
        }

        public static class OfStrings extends ColonDelimitedSet<String> {
            public OfStrings(String colonSeparatedItems) {
                super(colonSeparatedItems);
            }

            /* access modifiers changed from: protected */
            @Override // android.provider.SettingsStringUtil.ColonDelimitedSet
            public String itemFromString(String s) {
                return s;
            }

            public static String addAll(String delimitedElements, Collection<String> elements) {
                ColonDelimitedSet<String> set = new OfStrings(delimitedElements);
                return set.addAll(elements) ? set.toString() : delimitedElements;
            }

            public static String add(String delimitedElements, String element) {
                ColonDelimitedSet<String> set = new OfStrings(delimitedElements);
                if (set.contains(element)) {
                    return delimitedElements;
                }
                set.add(element);
                return set.toString();
            }

            public static String remove(String delimitedElements, String element) {
                ColonDelimitedSet<String> set = new OfStrings(delimitedElements);
                if (!set.contains(element)) {
                    return delimitedElements;
                }
                set.remove(element);
                return set.toString();
            }

            public static boolean contains(String delimitedElements, String element) {
                return ArrayUtils.indexOf(TextUtils.split(delimitedElements, SettingsStringUtil.DELIMITER), element) != -1;
            }
        }
    }

    public static class ComponentNameSet extends ColonDelimitedSet<ComponentName> {
        public ComponentNameSet(String colonSeparatedPackageNames) {
            super(colonSeparatedPackageNames);
        }

        /* access modifiers changed from: protected */
        @Override // android.provider.SettingsStringUtil.ColonDelimitedSet
        public ComponentName itemFromString(String s) {
            return ComponentName.unflattenFromString(s);
        }

        /* access modifiers changed from: protected */
        public String itemToString(ComponentName item) {
            return item.flattenToString();
        }

        public static String add(String delimitedElements, ComponentName element) {
            ComponentNameSet set = new ComponentNameSet(delimitedElements);
            if (set.contains(element)) {
                return delimitedElements;
            }
            set.add(element);
            return set.toString();
        }

        public static String remove(String delimitedElements, ComponentName element) {
            ComponentNameSet set = new ComponentNameSet(delimitedElements);
            if (!set.contains(element)) {
                return delimitedElements;
            }
            set.remove(element);
            return set.toString();
        }

        public static boolean contains(String delimitedElements, ComponentName element) {
            return ColonDelimitedSet.OfStrings.contains(delimitedElements, element.flattenToString());
        }
    }

    public static class SettingStringHelper {
        private final ContentResolver mContentResolver;
        private final String mSettingName;
        private final int mUserId;

        public SettingStringHelper(ContentResolver contentResolver, String name, int userId) {
            this.mContentResolver = contentResolver;
            this.mUserId = userId;
            this.mSettingName = name;
        }

        public String read() {
            return Settings.Secure.getStringForUser(this.mContentResolver, this.mSettingName, this.mUserId);
        }

        public boolean write(String value) {
            return Settings.Secure.putStringForUser(this.mContentResolver, this.mSettingName, value, this.mUserId);
        }

        public boolean modify(Function<String, String> change) {
            return write(change.apply(read()));
        }
    }
}
