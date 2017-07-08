package java.security.acl;

import java.security.Principal;
import java.util.Enumeration;

public interface AclEntry extends Cloneable {
    boolean addPermission(Permission permission);

    boolean checkPermission(Permission permission);

    Object clone();

    Principal getPrincipal();

    boolean isNegative();

    Enumeration<Permission> permissions();

    boolean removePermission(Permission permission);

    void setNegativePermissions();

    boolean setPrincipal(Principal principal);

    String toString();
}
