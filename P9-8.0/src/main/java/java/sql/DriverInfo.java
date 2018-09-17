package java.sql;

/* compiled from: DriverManager */
class DriverInfo {
    final Driver driver;

    DriverInfo(Driver driver) {
        this.driver = driver;
    }

    public boolean equals(Object other) {
        if ((other instanceof DriverInfo) && this.driver == ((DriverInfo) other).driver) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.driver.hashCode();
    }

    public String toString() {
        return "driver[className=" + this.driver + "]";
    }
}
