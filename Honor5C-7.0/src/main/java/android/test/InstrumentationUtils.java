package android.test;

@Deprecated
public class InstrumentationUtils {
    public static int getMenuIdentifier(Class cls, String identifier) {
        int id = -1;
        try {
            id = ((Integer) cls.getDeclaredField(identifier).get(cls)).intValue();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
        return id;
    }
}
