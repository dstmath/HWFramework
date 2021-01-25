package ohos.thermallistener;

public interface ThermalConnection {

    public enum ThermalSeverityLevel {
        COOL,
        WARM,
        HOT,
        OVERHEATED,
        WARNING,
        EMERGENCY
    }

    default void thermalServiceDied() {
    }

    void thermalStatusChanged(ThermalSeverityLevel thermalSeverityLevel);
}
