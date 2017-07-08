package java.sql;

public class DriverPropertyInfo {
    public String[] choices;
    public String description;
    public String name;
    public boolean required;
    public String value;

    public DriverPropertyInfo(String name, String value) {
        this.description = null;
        this.required = false;
        this.value = null;
        this.choices = null;
        this.name = name;
        this.value = value;
    }
}
