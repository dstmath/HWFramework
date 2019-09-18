package android.net.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

public class InterfaceSet {
    public final Set<String> ifnames;

    public InterfaceSet(String... names) {
        Set<String> nameSet = new HashSet<>();
        for (String name : names) {
            if (name != null) {
                nameSet.add(name);
            }
        }
        this.ifnames = Collections.unmodifiableSet(nameSet);
    }

    public String toString() {
        StringJoiner sj = new StringJoiner(",", "[", "]");
        for (String ifname : this.ifnames) {
            sj.add(ifname);
        }
        return sj.toString();
    }

    public boolean equals(Object obj) {
        return obj != null && (obj instanceof InterfaceSet) && this.ifnames.equals(((InterfaceSet) obj).ifnames);
    }
}
