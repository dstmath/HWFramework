package ohos.com.sun.org.apache.xerces.internal.impl.xs.identity;

public interface FieldActivator {
    XPathMatcher activateField(Field field, int i);

    void endValueScopeFor(IdentityConstraint identityConstraint, int i);

    Boolean mayMatch(Field field);

    void setMayMatch(Field field, Boolean bool);

    void startValueScopeFor(IdentityConstraint identityConstraint, int i);
}
