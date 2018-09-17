package java.security;

public final class AllPermission extends Permission {
    public AllPermission() {
        super("");
    }

    public AllPermission(String name, String actions) {
        super("");
    }

    public boolean implies(Permission p) {
        return true;
    }

    public String getActions() {
        return null;
    }
}
