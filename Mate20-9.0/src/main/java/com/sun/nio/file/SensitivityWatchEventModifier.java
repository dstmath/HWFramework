package com.sun.nio.file;

import java.nio.file.WatchEvent;

public enum SensitivityWatchEventModifier implements WatchEvent.Modifier {
    HIGH(2),
    MEDIUM(10),
    LOW(30);
    
    private final int sensitivity;

    public int sensitivityValueInSeconds() {
        return this.sensitivity;
    }

    private SensitivityWatchEventModifier(int sensitivity2) {
        this.sensitivity = sensitivity2;
    }
}
