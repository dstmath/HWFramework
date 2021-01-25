package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import ohos.com.sun.org.apache.bcel.internal.classfile.ClassParser;
import ohos.com.sun.org.apache.bcel.internal.classfile.JavaClass;
import ohos.global.icu.text.PluralRules;

public class SyntheticRepository implements Repository {
    private static final String DEFAULT_PATH = ClassPath.getClassPath();
    private static HashMap _instances = new HashMap();
    private HashMap _loadedClasses = new HashMap();
    private ClassPath _path = null;

    private SyntheticRepository(ClassPath classPath) {
        this._path = classPath;
    }

    public static SyntheticRepository getInstance() {
        return getInstance(ClassPath.SYSTEM_CLASS_PATH);
    }

    public static SyntheticRepository getInstance(ClassPath classPath) {
        SyntheticRepository syntheticRepository = (SyntheticRepository) _instances.get(classPath);
        if (syntheticRepository != null) {
            return syntheticRepository;
        }
        SyntheticRepository syntheticRepository2 = new SyntheticRepository(classPath);
        _instances.put(classPath, syntheticRepository2);
        return syntheticRepository2;
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public void storeClass(JavaClass javaClass) {
        this._loadedClasses.put(javaClass.getClassName(), javaClass);
        javaClass.setRepository(this);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public void removeClass(JavaClass javaClass) {
        this._loadedClasses.remove(javaClass.getClassName());
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public JavaClass findClass(String str) {
        return (JavaClass) this._loadedClasses.get(str);
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public JavaClass loadClass(String str) throws ClassNotFoundException {
        if (str == null || str.equals("")) {
            throw new IllegalArgumentException("Invalid class name " + str);
        }
        String replace = str.replace('/', '.');
        try {
            return loadClass(this._path.getInputStream(replace), replace);
        } catch (IOException e) {
            throw new ClassNotFoundException("Exception while looking for class " + replace + PluralRules.KEYWORD_RULE_SEPARATOR + e.toString());
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public JavaClass loadClass(Class cls) throws ClassNotFoundException {
        String name = cls.getName();
        int lastIndexOf = name.lastIndexOf(46);
        String substring = lastIndexOf > 0 ? name.substring(lastIndexOf + 1) : name;
        return loadClass(cls.getResourceAsStream(substring + ".class"), name);
    }

    private JavaClass loadClass(InputStream inputStream, String str) throws ClassNotFoundException {
        JavaClass findClass = findClass(str);
        if (findClass != null) {
            return findClass;
        }
        if (inputStream != null) {
            try {
                JavaClass parse = new ClassParser(inputStream, str).parse();
                storeClass(parse);
                return parse;
            } catch (IOException e) {
                throw new ClassNotFoundException("Exception while looking for class " + str + PluralRules.KEYWORD_RULE_SEPARATOR + e.toString());
            }
        } else {
            throw new ClassNotFoundException("SyntheticRepository could not load " + str);
        }
    }

    @Override // ohos.com.sun.org.apache.bcel.internal.util.Repository
    public void clear() {
        this._loadedClasses.clear();
    }
}
