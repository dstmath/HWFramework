package android_maps_conflict_avoidance.com.google.googlenav.labs;

public abstract class SimpleLab {
    private boolean active;

    public SimpleLab() {
        this.active = false;
    }

    public boolean isActive() {
        return this.active;
    }
}
