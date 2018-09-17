package java.text;

import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface AttributedCharacterIterator extends CharacterIterator {

    public static class Attribute implements Serializable {
        public static final Attribute INPUT_METHOD_SEGMENT = null;
        public static final Attribute LANGUAGE = null;
        public static final Attribute READING = null;
        private static final Map instanceMap = null;
        private static final long serialVersionUID = -9142742483513960612L;
        private String name;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.text.AttributedCharacterIterator.Attribute.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.text.AttributedCharacterIterator.Attribute.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.text.AttributedCharacterIterator.Attribute.<clinit>():void");
        }

        protected Attribute(String name) {
            this.name = name;
            if (getClass() == Attribute.class) {
                instanceMap.put(name, this);
            }
        }

        public final boolean equals(Object obj) {
            return super.equals(obj);
        }

        public final int hashCode() {
            return super.hashCode();
        }

        public String toString() {
            return getClass().getName() + "(" + this.name + ")";
        }

        protected String getName() {
            return this.name;
        }

        protected Object readResolve() throws InvalidObjectException {
            if (getClass() != Attribute.class) {
                throw new InvalidObjectException("subclass didn't correctly implement readResolve");
            }
            Attribute instance = (Attribute) instanceMap.get(getName());
            if (instance != null) {
                return instance;
            }
            throw new InvalidObjectException("unknown attribute name");
        }
    }

    Set<Attribute> getAllAttributeKeys();

    Object getAttribute(Attribute attribute);

    Map<Attribute, Object> getAttributes();

    int getRunLimit();

    int getRunLimit(Attribute attribute);

    int getRunLimit(Set<? extends Attribute> set);

    int getRunStart();

    int getRunStart(Attribute attribute);

    int getRunStart(Set<? extends Attribute> set);
}
