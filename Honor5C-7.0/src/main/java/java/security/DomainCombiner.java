package java.security;

public interface DomainCombiner {
    ProtectionDomain[] combine(ProtectionDomain[] protectionDomainArr, ProtectionDomain[] protectionDomainArr2);
}
