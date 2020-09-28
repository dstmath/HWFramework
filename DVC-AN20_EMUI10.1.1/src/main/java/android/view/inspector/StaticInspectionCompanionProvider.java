package android.view.inspector;

public class StaticInspectionCompanionProvider implements InspectionCompanionProvider {
    private static final String COMPANION_SUFFIX = "$InspectionCompanion";

    @Override // android.view.inspector.InspectionCompanionProvider
    public <T> InspectionCompanion<T> provide(Class<T> cls) {
        try {
            Class<?> loadClass = cls.getClassLoader().loadClass(cls.getName() + COMPANION_SUFFIX);
            if (InspectionCompanion.class.isAssignableFrom(loadClass)) {
                return (InspectionCompanion) loadClass.newInstance();
            }
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (IllegalAccessException e2) {
            throw new RuntimeException(e2);
        } catch (InstantiationException e3) {
            Throwable cause = e3.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            } else if (cause instanceof Error) {
                throw ((Error) cause);
            } else {
                throw new RuntimeException(cause);
            }
        }
    }
}
