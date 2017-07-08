package com.android.location.provider;

import java.security.InvalidParameterException;
import java.util.List;

public class ActivityChangedEvent {
    private final List<ActivityRecognitionEvent> mActivityRecognitionEvents;

    public ActivityChangedEvent(List<ActivityRecognitionEvent> activityRecognitionEvents) {
        if (activityRecognitionEvents == null) {
            throw new InvalidParameterException("Parameter 'activityRecognitionEvents' must not be null.");
        }
        this.mActivityRecognitionEvents = activityRecognitionEvents;
    }

    public Iterable<ActivityRecognitionEvent> getActivityRecognitionEvents() {
        return this.mActivityRecognitionEvents;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ ActivityChangedEvent:");
        for (ActivityRecognitionEvent event : this.mActivityRecognitionEvents) {
            builder.append("\n    ");
            builder.append(event.toString());
        }
        builder.append("\n]");
        return builder.toString();
    }
}
