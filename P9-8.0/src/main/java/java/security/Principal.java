package java.security;

import javax.security.auth.Subject;

public interface Principal {
    boolean equals(Object obj);

    String getName();

    int hashCode();

    String toString();

    boolean implies(Subject subject) {
        if (subject == null) {
            return false;
        }
        return subject.getPrincipals().contains(this);
    }
}
