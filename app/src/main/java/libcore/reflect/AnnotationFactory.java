package libcore.reflect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AnnotationFactory implements InvocationHandler, Serializable {
    private static final transient Map<Class<? extends Annotation>, AnnotationMember[]> cache = null;
    private AnnotationMember[] elements;
    private final Class<? extends Annotation> klazz;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.reflect.AnnotationFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.reflect.AnnotationFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.reflect.AnnotationFactory.<clinit>():void");
    }

    public static AnnotationMember[] getElementsDescription(Class<? extends Annotation> annotationType) {
        synchronized (cache) {
            AnnotationMember[] desc = (AnnotationMember[]) cache.get(annotationType);
            if (desc != null) {
                return desc;
            }
            if (annotationType.isAnnotation()) {
                Method[] declaredMethods = annotationType.getDeclaredMethods();
                desc = new AnnotationMember[declaredMethods.length];
                for (int i = 0; i < declaredMethods.length; i++) {
                    Method element = declaredMethods[i];
                    String name = element.getName();
                    Class<?> type = element.getReturnType();
                    try {
                        desc[i] = new AnnotationMember(name, element.getDefaultValue(), type, element);
                    } catch (Throwable t) {
                        desc[i] = new AnnotationMember(name, t, type, element);
                    }
                }
                synchronized (cache) {
                    cache.put(annotationType, desc);
                }
                return desc;
            }
            throw new IllegalArgumentException("Type is not annotation: " + annotationType.getName());
        }
    }

    public static <A extends Annotation> A createAnnotation(Class<? extends Annotation> annotationType, AnnotationMember[] elements) {
        AnnotationFactory factory = new AnnotationFactory(annotationType, elements);
        return (Annotation) Proxy.newProxyInstance(annotationType.getClassLoader(), new Class[]{annotationType}, factory);
    }

    private AnnotationFactory(Class<? extends Annotation> klzz, AnnotationMember[] values) {
        this.klazz = klzz;
        AnnotationMember[] defs = getElementsDescription(this.klazz);
        if (values == null) {
            this.elements = defs;
            return;
        }
        this.elements = new AnnotationMember[defs.length];
        for (int i = this.elements.length - 1; i >= 0; i--) {
            for (AnnotationMember val : values) {
                if (val.name.equals(defs[i].name)) {
                    this.elements[i] = val.setDefinition(defs[i]);
                    break;
                }
            }
            this.elements[i] = defs[i];
        }
    }

    private void readObject(ObjectInputStream os) throws IOException, ClassNotFoundException {
        os.defaultReadObject();
        AnnotationMember[] defs = getElementsDescription(this.klazz);
        AnnotationMember[] old = this.elements;
        List<AnnotationMember> merged = new ArrayList(defs.length + old.length);
        for (AnnotationMember el1 : old) {
            for (AnnotationMember el2 : defs) {
                if (el2.name.equals(el1.name)) {
                    break;
                }
            }
            merged.add(el1);
        }
        for (AnnotationMember def : defs) {
            for (AnnotationMember val : old) {
                if (val.name.equals(def.name)) {
                    merged.add(val.setDefinition(def));
                    break;
                }
            }
            merged.add(def);
        }
        this.elements = (AnnotationMember[]) merged.toArray(new AnnotationMember[merged.size()]);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!this.klazz.isInstance(obj)) {
            return false;
        }
        int i;
        if (Proxy.isProxyClass(obj.getClass())) {
            AnnotationFactory handler = Proxy.getInvocationHandler(obj);
            if (handler instanceof AnnotationFactory) {
                AnnotationFactory other = handler;
                if (this.elements.length != other.elements.length) {
                    return false;
                }
                AnnotationMember[] annotationMemberArr = this.elements;
                int length = annotationMemberArr.length;
                int i2 = 0;
                while (i2 < length) {
                    AnnotationMember el1 = annotationMemberArr[i2];
                    AnnotationMember[] annotationMemberArr2 = other.elements;
                    i = 0;
                    int length2 = annotationMemberArr2.length;
                    while (i < length2) {
                        if (el1.equals(annotationMemberArr2[i])) {
                            i2++;
                        } else {
                            i++;
                        }
                    }
                    return false;
                }
                return true;
            }
        }
        AnnotationMember[] annotationMemberArr3 = this.elements;
        i = 0;
        int length3 = annotationMemberArr3.length;
        while (i < length3) {
            AnnotationMember el = annotationMemberArr3[i];
            if (el.tag == '!') {
                return false;
            }
            try {
                if (!el.definingMethod.isAccessible()) {
                    el.definingMethod.setAccessible(true);
                }
                Object otherValue = el.definingMethod.invoke(obj, new Object[0]);
                if (otherValue != null) {
                    if (el.tag == '[') {
                        if (!el.equalArrayValue(otherValue)) {
                            return false;
                        }
                    } else if (!el.value.equals(otherValue)) {
                        return false;
                    }
                } else if (el.value != AnnotationMember.NO_VALUE) {
                    return false;
                }
                i++;
            } catch (Throwable th) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int hash = 0;
        for (AnnotationMember element : this.elements) {
            hash += element.hashCode();
        }
        return hash;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('@');
        result.append(this.klazz.getName());
        result.append('(');
        for (int i = 0; i < this.elements.length; i++) {
            if (i != 0) {
                result.append(", ");
            }
            result.append(this.elements[i]);
        }
        result.append(')');
        return result.toString();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int i = 0;
        String name = method.getName();
        Class[] params = method.getParameterTypes();
        if (params.length == 0) {
            if ("annotationType".equals(name)) {
                return this.klazz;
            }
            if ("toString".equals(name)) {
                return toString();
            }
            if ("hashCode".equals(name)) {
                return Integer.valueOf(hashCode());
            }
            AnnotationMember element = null;
            AnnotationMember[] annotationMemberArr = this.elements;
            int length = annotationMemberArr.length;
            while (i < length) {
                AnnotationMember el = annotationMemberArr[i];
                if (name.equals(el.name)) {
                    element = el;
                    break;
                }
                i++;
            }
            if (element == null || !method.equals(element.definingMethod)) {
                throw new IllegalArgumentException(method.toString());
            }
            Object value = element.validateValue();
            if (value != null) {
                return value;
            }
            throw new IncompleteAnnotationException(this.klazz, name);
        } else if (params.length == 1 && params[0] == Object.class && "equals".equals(name)) {
            return Boolean.valueOf(equals(args[0]));
        } else {
            throw new IllegalArgumentException("Invalid method for annotation type: " + method);
        }
    }
}
