package android.net;

import java.util.Locale;

public class NetworkConfig {
    public boolean dependencyMet;
    public String name;
    public int priority;
    public int radio;
    public int restoreTime;
    public int type;

    public NetworkConfig(String init) {
        String[] fragments = init.split(",");
        this.name = fragments[0].trim().toLowerCase(Locale.ROOT);
        this.type = Integer.parseInt(fragments[1]);
        this.radio = Integer.parseInt(fragments[2]);
        this.priority = Integer.parseInt(fragments[3]);
        this.restoreTime = Integer.parseInt(fragments[4]);
        this.dependencyMet = Boolean.parseBoolean(fragments[5]);
    }

    public boolean isDefault() {
        return this.type == this.radio;
    }
}
