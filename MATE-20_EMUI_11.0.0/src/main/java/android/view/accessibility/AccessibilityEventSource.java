package android.view.accessibility;

public interface AccessibilityEventSource {
    void sendAccessibilityEvent(int i);

    void sendAccessibilityEventUnchecked(AccessibilityEvent accessibilityEvent);
}
