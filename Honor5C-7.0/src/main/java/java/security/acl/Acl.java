package java.security.acl;

import java.security.Principal;
import java.util.Enumeration;

public interface Acl extends Owner {
    boolean addEntry(Principal principal, AclEntry aclEntry) throws NotOwnerException;

    boolean checkPermission(Principal principal, Permission permission);

    Enumeration<AclEntry> entries();

    String getName();

    Enumeration<Permission> getPermissions(Principal principal);

    boolean removeEntry(Principal principal, AclEntry aclEntry) throws NotOwnerException;

    void setName(Principal principal, String str) throws NotOwnerException;

    String toString();
}
