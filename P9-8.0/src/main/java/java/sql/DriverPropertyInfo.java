package java.sql;

public class DriverPropertyInfo {
    public String[] choices = null;
    public String description = null;
    public String name;
    public boolean required = false;
    public String value = null;

    public DriverPropertyInfo(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
