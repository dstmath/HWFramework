package javax.security.auth;

import java.security.DomainCombiner;
import java.security.ProtectionDomain;

public class SubjectDomainCombiner implements DomainCombiner {
    public SubjectDomainCombiner(Subject subject) {
    }

    public Subject getSubject() {
        return null;
    }

    public ProtectionDomain[] combine(ProtectionDomain[] currentDomains, ProtectionDomain[] assignedDomains) {
        return null;
    }
}
