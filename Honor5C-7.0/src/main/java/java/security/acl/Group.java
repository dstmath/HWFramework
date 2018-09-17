package java.security.acl;

import java.security.Principal;
import java.util.Enumeration;

public interface Group extends Principal {
    boolean addMember(Principal principal);

    boolean isMember(Principal principal);

    Enumeration<? extends Principal> members();

    boolean removeMember(Principal principal);
}
