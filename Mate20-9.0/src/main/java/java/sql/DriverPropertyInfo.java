package java.sql;

public class DriverPropertyInfo {
    public String[] choices = null;
    public String description = null;
    public String name;
    public boolean required = false;
    public String value = null;

    public DriverPropertyInfo(String name2, String value2) {
        this.name = name2;
        this.value = value2;
    }
}
