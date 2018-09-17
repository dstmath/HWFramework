package android_maps_conflict_avoidance.com.google.common.geom;

public final class Point {
    public int x;
    public int y;

    public boolean equals(Object o) {
        boolean z = false;
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        Point p = (Point) o;
        if (this.x == p.x && this.y == p.y) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.x + this.y;
    }

    public String toString() {
        return getClass().getName() + "[" + this.x + "," + this.y + "]";
    }
}
