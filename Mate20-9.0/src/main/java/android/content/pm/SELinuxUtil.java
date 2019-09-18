package android.content.pm;

public final class SELinuxUtil {
    public static final String COMPLETE_STR = ":complete";
    private static final String INSTANT_APP_STR = ":ephemeralapp";

    public static String assignSeinfoUser(PackageUserState userState) {
        if (userState.instantApp) {
            return ":ephemeralapp:complete";
        }
        return COMPLETE_STR;
    }
}
