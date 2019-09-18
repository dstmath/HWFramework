package android.content.res;

public final class ResourceId {
    public static final int ID_NULL = 0;

    public static boolean isValid(int id) {
        return (id == -1 || (-16777216 & id) == 0 || (16711680 & id) == 0) ? false : true;
    }
}
