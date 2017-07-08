package java.security.acl;

import java.security.Principal;

public interface Owner {
    boolean addOwner(Principal principal, Principal principal2) throws NotOwnerException;

    boolean deleteOwner(Principal principal, Principal principal2) throws NotOwnerException, LastOwnerException;

    boolean isOwner(Principal principal);
}
