package sun.nio.fs;

import java.io.IOException;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalNotFoundException;

class UnixUserPrincipals {
    static final User SPECIAL_EVERYONE = createSpecial("EVERYONE@");
    static final User SPECIAL_GROUP = createSpecial("GROUP@");
    static final User SPECIAL_OWNER = createSpecial("OWNER@");

    static class Group extends User implements GroupPrincipal {
        Group(int id, String name) {
            super(id, true, name);
        }
    }

    static class User implements UserPrincipal {
        private final int id;
        private final boolean isGroup;
        private final String name;

        private User(int id2, boolean isGroup2, String name2) {
            this.id = id2;
            this.isGroup = isGroup2;
            this.name = name2;
        }

        User(int id2, String name2) {
            this(id2, false, name2);
        }

        /* access modifiers changed from: package-private */
        public int uid() {
            if (!this.isGroup) {
                return this.id;
            }
            throw new AssertionError();
        }

        /* access modifiers changed from: package-private */
        public int gid() {
            if (this.isGroup) {
                return this.id;
            }
            throw new AssertionError();
        }

        /* access modifiers changed from: package-private */
        public boolean isSpecial() {
            return this.id == -1;
        }

        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof User)) {
                return false;
            }
            User other = (User) obj;
            if (this.id != other.id || this.isGroup != other.isGroup) {
                return false;
            }
            if (this.id == -1 && other.id == -1) {
                return this.name.equals(other.name);
            }
            return true;
        }

        public int hashCode() {
            return this.id != -1 ? this.id : this.name.hashCode();
        }
    }

    UnixUserPrincipals() {
    }

    private static User createSpecial(String name) {
        return new User(-1, name);
    }

    static User fromUid(int uid) {
        String name;
        try {
            name = Util.toString(UnixNativeDispatcher.getpwuid(uid));
        } catch (UnixException e) {
            name = Integer.toString(uid);
        }
        return new User(uid, name);
    }

    static Group fromGid(int gid) {
        String name;
        try {
            name = Util.toString(UnixNativeDispatcher.getgrgid(gid));
        } catch (UnixException e) {
            name = Integer.toString(gid);
        }
        return new Group(gid, name);
    }

    private static int lookupName(String name, boolean isGroup) throws IOException {
        int i;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("lookupUserInformation"));
        }
        if (isGroup) {
            try {
                i = UnixNativeDispatcher.getgrnam(name);
            } catch (UnixException x) {
                throw new IOException(name + ": " + x.errorString());
            }
        } else {
            i = UnixNativeDispatcher.getpwnam(name);
        }
        int id = i;
        if (id != -1) {
            return id;
        }
        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            throw new UserPrincipalNotFoundException(name);
        }
    }

    static UserPrincipal lookupUser(String name) throws IOException {
        if (name.equals(SPECIAL_OWNER.getName())) {
            return SPECIAL_OWNER;
        }
        if (name.equals(SPECIAL_GROUP.getName())) {
            return SPECIAL_GROUP;
        }
        if (name.equals(SPECIAL_EVERYONE.getName())) {
            return SPECIAL_EVERYONE;
        }
        return new User(lookupName(name, false), name);
    }

    static GroupPrincipal lookupGroup(String group) throws IOException {
        return new Group(lookupName(group, true), group);
    }
}
