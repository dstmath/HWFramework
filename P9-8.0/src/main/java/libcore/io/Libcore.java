package libcore.io;

public final class Libcore {
    public static Os os = new BlockGuardOs(rawOs);
    public static Os rawOs = new Linux();

    private Libcore() {
    }
}
