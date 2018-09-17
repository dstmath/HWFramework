package android.icu.impl;

import android.icu.util.ICUException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

public abstract class CacheValue<V> {
    private static final CacheValue NULL_VALUE = null;
    private static volatile Strength strength;

    private static final class NullValue<V> extends CacheValue<V> {
        private NullValue() {
        }

        public boolean isNull() {
            return true;
        }

        public V get() {
            return null;
        }

        public V resetIfCleared(V value) {
            if (value == null) {
                return null;
            }
            throw new ICUException("resetting a null value to a non-null value");
        }
    }

    private static final class SoftValue<V> extends CacheValue<V> {
        private Reference<V> ref;

        SoftValue(V value) {
            this.ref = new SoftReference(value);
        }

        public V get() {
            return this.ref.get();
        }

        public synchronized V resetIfCleared(V value) {
            V oldValue = this.ref.get();
            if (oldValue != null) {
                return oldValue;
            }
            this.ref = new SoftReference(value);
            return value;
        }
    }

    public enum Strength {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.CacheValue.Strength.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.CacheValue.Strength.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.CacheValue.Strength.<clinit>():void");
        }
    }

    private static final class StrongValue<V> extends CacheValue<V> {
        private V value;

        StrongValue(V value) {
            this.value = value;
        }

        public V get() {
            return this.value;
        }

        public V resetIfCleared(V v) {
            return this.value;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.CacheValue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.CacheValue.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.CacheValue.<clinit>():void");
    }

    public abstract V get();

    public abstract V resetIfCleared(V v);

    public CacheValue() {
    }

    public static void setStrength(Strength strength) {
        strength = strength;
    }

    public static boolean futureInstancesWillBeStrong() {
        return strength == Strength.STRONG;
    }

    public static <V> CacheValue<V> getInstance(V value) {
        if (value == null) {
            return NULL_VALUE;
        }
        return strength == Strength.STRONG ? new StrongValue(value) : new SoftValue(value);
    }

    public boolean isNull() {
        return false;
    }
}
