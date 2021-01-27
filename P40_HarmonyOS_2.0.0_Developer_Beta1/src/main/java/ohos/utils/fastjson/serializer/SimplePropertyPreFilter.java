package ohos.utils.fastjson.serializer;

import java.util.HashSet;
import java.util.Set;

public class SimplePropertyPreFilter implements PropertyPreFilter {
    private final Class<?> clazz;
    private final Set<String> excludes;
    private final Set<String> includes;
    private int maxLevel;

    public SimplePropertyPreFilter(String... strArr) {
        this(null, strArr);
    }

    public SimplePropertyPreFilter(Class<?> cls, String... strArr) {
        this.includes = new HashSet();
        this.excludes = new HashSet();
        this.maxLevel = 0;
        this.clazz = cls;
        for (String str : strArr) {
            if (str != null) {
                this.includes.add(str);
            }
        }
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public void setMaxLevel(int i) {
        this.maxLevel = i;
    }

    public Class<?> getClazz() {
        return this.clazz;
    }

    public Set<String> getIncludes() {
        return this.includes;
    }

    public Set<String> getExcludes() {
        return this.excludes;
    }

    @Override // ohos.utils.fastjson.serializer.PropertyPreFilter
    public boolean apply(JSONSerializer jSONSerializer, Object obj, String str) {
        if (obj == null) {
            return true;
        }
        Class<?> cls = this.clazz;
        if (!(cls == null || cls.isInstance(obj))) {
            return true;
        }
        if (this.excludes.contains(str)) {
            return false;
        }
        if (this.maxLevel > 0) {
            int i = 0;
            for (SerialContext serialContext = jSONSerializer.context; serialContext != null; serialContext = serialContext.parent) {
                i++;
                if (i > this.maxLevel) {
                    return false;
                }
            }
        }
        if (this.includes.size() == 0 || this.includes.contains(str)) {
            return true;
        }
        return false;
    }
}
