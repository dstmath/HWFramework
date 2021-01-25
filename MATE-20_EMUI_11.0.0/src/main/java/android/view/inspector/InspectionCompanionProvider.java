package android.view.inspector;

public interface InspectionCompanionProvider {
    <T> InspectionCompanion<T> provide(Class<T> cls);
}
