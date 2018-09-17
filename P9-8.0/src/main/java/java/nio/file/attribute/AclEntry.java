package java.nio.file.attribute;

import java.util.Collection;
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
        static final /* synthetic */ boolean -assertionsDisabled = (Builder.class.desiredAssertionStatus() ^ 1);
        private Set<AclEntryFlag> flags;
        private Set<AclEntryPermission> perms;
        private AclEntryType type;
        private UserPrincipal who;

        /* synthetic */ Builder(AclEntryType type, UserPrincipal who, Set perms, Set flags, Builder -this4) {
            this(type, who, perms, flags);
        }

        private Builder(AclEntryType type, UserPrincipal who, Set<AclEntryPermission> perms, Set<AclEntryFlag> flags) {
            if (-assertionsDisabled || !(perms == null || flags == null)) {
                this.type = type;
                this.who = who;
                this.perms = perms;
                this.flags = flags;
                return;
            }
            throw new AssertionError();
        }

        public AclEntry build() {
            if (this.type == null) {
                throw new IllegalStateException("Missing type component");
            } else if (this.who != null) {
                return new AclEntry(this.type, this.who, this.perms, this.flags, null);
            } else {
                throw new IllegalStateException("Missing who component");
            }
        }

        public Builder setType(AclEntryType type) {
            if (type == null) {
                throw new NullPointerException();
            }
            this.type = type;
            return this;
        }

        public Builder setPrincipal(UserPrincipal who) {
            if (who == null) {
                throw new NullPointerException();
            }
            this.who = who;
            return this;
        }

        private static void checkSet(Set<?> set, Class<?> type) {
            for (Object e : set) {
                if (e == null) {
                    throw new NullPointerException();
                }
                type.cast(e);
            }
        }

        public Builder setPermissions(Set<AclEntryPermission> perms) {
            if (perms.isEmpty()) {
                perms = Collections.emptySet();
            } else {
                perms = EnumSet.copyOf((Collection) perms);
                checkSet(perms, AclEntryPermission.class);
            }
            this.perms = perms;
            return this;
        }

        public Builder setPermissions(AclEntryPermission... perms) {
            Set<AclEntryPermission> set = EnumSet.noneOf(AclEntryPermission.class);
            for (AclEntryPermission p : perms) {
                if (p == null) {
                    throw new NullPointerException();
                }
                set.add(p);
            }
            this.perms = set;
            return this;
        }

        public Builder setFlags(Set<AclEntryFlag> flags) {
            if (flags.isEmpty()) {
                flags = Collections.emptySet();
            } else {
                flags = EnumSet.copyOf((Collection) flags);
                checkSet(flags, AclEntryFlag.class);
            }
            this.flags = flags;
            return this;
        }

        public Builder setFlags(AclEntryFlag... flags) {
            Set<AclEntryFlag> set = EnumSet.noneOf(AclEntryFlag.class);
            for (AclEntryFlag f : flags) {
                if (f == null) {
                    throw new NullPointerException();
                }
                set.add(f);
            }
            this.flags = set;
            return this;
        }
    }

    /* synthetic */ AclEntry(AclEntryType type, UserPrincipal who, Set perms, Set flags, AclEntry -this4) {
        this(type, who, perms, flags);
    }

    private AclEntry(AclEntryType type, UserPrincipal who, Set<AclEntryPermission> perms, Set<AclEntryFlag> flags) {
        this.type = type;
        this.who = who;
        this.perms = perms;
        this.flags = flags;
    }

    public static Builder newBuilder() {
        return new Builder(null, null, Collections.emptySet(), Collections.emptySet(), null);
    }

    public static Builder newBuilder(AclEntry entry) {
        return new Builder(entry.type, entry.who, entry.perms, entry.flags, null);
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
        if (ob == null || ((ob instanceof AclEntry) ^ 1) != 0) {
            return false;
        }
        AclEntry other = (AclEntry) ob;
        return this.type == other.type && this.who.lambda$-java_util_function_Predicate_4628(other.who) && this.perms.equals(other.perms) && this.flags.equals(other.flags);
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
