package android.test;

import com.google.android.collect.Sets;
import java.util.Collections;
import java.util.Set;

@Deprecated
public class ClassPathPackageInfo {
    private final String packageName;
    private final ClassPathPackageInfoSource source;
    private final Set<String> subpackageNames;
    private final Set<Class<?>> topLevelClasses;

    ClassPathPackageInfo(ClassPathPackageInfoSource source, String packageName, Set<String> subpackageNames, Set<Class<?>> topLevelClasses) {
        this.source = source;
        this.packageName = packageName;
        this.subpackageNames = Collections.unmodifiableSet(subpackageNames);
        this.topLevelClasses = Collections.unmodifiableSet(topLevelClasses);
    }

    public Set<ClassPathPackageInfo> getSubpackages() {
        Set<ClassPathPackageInfo> info = Sets.newHashSet();
        for (String name : this.subpackageNames) {
            info.add(this.source.getPackageInfo(name));
        }
        return info;
    }

    public Set<Class<?>> getTopLevelClassesRecursive() {
        Set<Class<?>> set = Sets.newHashSet();
        addTopLevelClassesTo(set);
        return set;
    }

    private void addTopLevelClassesTo(Set<Class<?>> set) {
        set.addAll(this.topLevelClasses);
        for (ClassPathPackageInfo info : getSubpackages()) {
            info.addTopLevelClassesTo(set);
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ClassPathPackageInfo)) {
            return false;
        }
        return this.packageName.equals(((ClassPathPackageInfo) obj).packageName);
    }

    public int hashCode() {
        return this.packageName.hashCode();
    }
}
