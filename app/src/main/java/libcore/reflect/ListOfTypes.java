package libcore.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import libcore.util.EmptyArray;

public final class ListOfTypes {
    public static final ListOfTypes EMPTY = null;
    private Type[] resolvedTypes;
    private final ArrayList<Type> types;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.reflect.ListOfTypes.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.reflect.ListOfTypes.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: libcore.reflect.ListOfTypes.<clinit>():void");
    }

    ListOfTypes(int capacity) {
        this.types = new ArrayList(capacity);
    }

    ListOfTypes(Type[] types) {
        this.types = new ArrayList(types.length);
        for (Type type : types) {
            this.types.add(type);
        }
    }

    void add(Type type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        this.types.add(type);
    }

    int length() {
        return this.types.size();
    }

    public Type[] getResolvedTypes() {
        Type[] result = this.resolvedTypes;
        if (result != null) {
            return result;
        }
        result = resolveTypes(this.types);
        this.resolvedTypes = result;
        return result;
    }

    private Type[] resolveTypes(List<Type> unresolved) {
        int size = unresolved.size();
        if (size == 0) {
            return EmptyArray.TYPE;
        }
        Type[] result = new Type[size];
        for (int i = 0; i < size; i++) {
            Type type = (Type) unresolved.get(i);
            if (type instanceof ParameterizedTypeImpl) {
                result[i] = ((ParameterizedTypeImpl) type).getResolvedType();
            } else {
                result[i] = type;
            }
        }
        return result;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.types.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(this.types.get(i));
        }
        return result.toString();
    }
}
