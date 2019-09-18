package java.nio.file.attribute;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class AclEntry {
    private final Set<AclEntryFlag> flags;
    private volatile int hash;
    private final Set<AclEntryPermission> perms;
    private final AclEntryType type;
    private final UserPrincipal who;

    public static final class Builder {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private Set<AclEntryFlag> flags;
        private Set<AclEntryPermission> perms;
        private AclEntryType type;
        private UserPrincipal who;

        static {
            Class<AclEntry> cls = AclEntry.class;
        }

        private Builder(AclEntryType type2, UserPrincipal who2, Set<AclEntryPermission> perms2, Set<AclEntryFlag> flags2) {
            this.type = type2;
            this.who = who2;
            this.perms = perms2;
            this.flags = flags2;
        }

        public AclEntry build() {
            if (this.type == null) {
                throw new IllegalStateException("Missing type component");
            } else if (this.who != null) {
                AclEntry aclEntry = new AclEntry(this.type, this.who, this.perms, this.flags);
                return aclEntry;
            } else {
                throw new IllegalStateException("Missing who component");
            }
        }

        public Builder setType(AclEntryType type2) {
            if (type2 != null) {
                this.type = type2;
                return this;
            }
            throw new NullPointerException();
        }

        public Builder setPrincipal(UserPrincipal who2) {
            if (who2 != null) {
                this.who = who2;
                return this;
            }
            throw new NullPointerException();
        }

        private static void checkSet(Set<?> set, Class<?> type2) {
            for (Object e : set) {
                if (e != null) {
                    type2.cast(e);
                } else {
                    throw new NullPointerException();
                }
            }
        }

        /* JADX WARNING: type inference failed for: r2v0, types: [java.util.Collection, java.util.Set<java.nio.file.attribute.AclEntryPermission>, java.util.Set] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public Builder setPermissions(Set<AclEntryPermission> r2) {
            Set<AclEntryPermission> perms2;
            if (r2.isEmpty()) {
                perms2 = Collections.emptySet();
            } else {
                perms2 = EnumSet.copyOf(r2);
                checkSet(perms2, AclEntryPermission.class);
            }
            this.perms = perms2;
            return this;
        }

        public Builder setPermissions(AclEntryPermission... perms2) {
            Set<AclEntryPermission> set = EnumSet.noneOf(AclEntryPermission.class);
            int length = perms2.length;
            int i = 0;
            while (i < length) {
                AclEntryPermission p = perms2[i];
                if (p != null) {
                    set.add(p);
                    i++;
                } else {
                    throw new NullPointerException();
                }
            }
            this.perms = set;
            return this;
        }

        /* JADX WARNING: type inference failed for: r2v0, types: [java.util.Set<java.nio.file.attribute.AclEntryFlag>, java.util.Collection, java.util.Set] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public Builder setFlags(Set<AclEntryFlag> r2) {
            Set<AclEntryFlag> flags2;
            if (r2.isEmpty()) {
                flags2 = Collections.emptySet();
            } else {
                flags2 = EnumSet.copyOf(r2);
                checkSet(flags2, AclEntryFlag.class);
            }
            this.flags = flags2;
            return this;
        }

        public Builder setFlags(AclEntryFlag... flags2) {
            Set<AclEntryFlag> set = EnumSet.noneOf(AclEntryFlag.class);
            int length = flags2.length;
            int i = 0;
            while (i < length) {
                AclEntryFlag f = flags2[i];
                if (f != null) {
                    set.add(f);
                    i++;
                } else {
                    throw new NullPointerException();
                }
            }
            this.flags = set;
            return this;
        }
    }

    private AclEntry(AclEntryType type2, UserPrincipal who2, Set<AclEntryPermission> perms2, Set<AclEntryFlag> flags2) {
        this.type = type2;
        this.who = who2;
        this.perms = perms2;
        this.flags = flags2;
    }

    public static Builder newBuilder() {
        Builder builder = new Builder(null, null, Collections.emptySet(), Collections.emptySet());
        return builder;
    }

    public static Builder newBuilder(AclEntry entry) {
        Builder builder = new Builder(entry.type, entry.who, entry.perms, entry.flags);
        return builder;
    }

    public AclEntryType type() {
        return this.type;
    }

    public UserPrincipal principal() {
        return this.who;
    }

    public Set<AclEntryPermission> permissions() {
        return new HashSet(this.perms);
    }

    public Set<AclEntryFlag> flags() {
        return new HashSet(this.flags);
    }

    public boolean equals(Object ob) {
        if (ob == this) {
            return true;
        }
        if (ob == null || !(ob instanceof AclEntry)) {
            return false;
        }
        AclEntry other = (AclEntry) ob;
        if (this.type == other.type && this.who.equals(other.who) && this.perms.equals(other.perms) && this.flags.equals(other.flags)) {
            return true;
        }
        return false;
    }

    private static int hash(int h, Object o) {
        return (h * 127) + o.hashCode();
    }

    public int hashCode() {
        if (this.hash != 0) {
            return this.hash;
        }
        this.hash = hash(hash(hash(this.type.hashCode(), this.who), this.perms), this.flags);
        return this.hash;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.who.getName());
        sb.append(':');
        for (AclEntryPermission perm : this.perms) {
            sb.append(perm.name());
            sb.append('/');
        }
        sb.setLength(sb.length() - 1);
        sb.append(':');
        if (!this.flags.isEmpty()) {
            for (AclEntryFlag flag : this.flags) {
                sb.append(flag.name());
                sb.append('/');
            }
            sb.setLength(sb.length() - 1);
            sb.append(':');
        }
        sb.append(this.type.name());
        return sb.toString();
    }
}
