package java.sql;

/* compiled from: DriverManager */
class DriverInfo {
    final Driver driver;

    DriverInfo(Driver driver2) {
        this.driver = driver2;
    }

    public boolean equals(Object other) {
        return (other instanceof DriverInfo) && this.driver == ((DriverInfo) other).driver;
    }

    public int hashCode() {
        return this.driver.hashCode();
    }

    public String toString() {
        return "driver[className=" + this.driver + "]";
    }
}
