package android.icu.impl;

import android.icu.util.TimeZone;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeSet;

public class JavaTimeZone extends TimeZone {
    private static final TreeSet<String> AVAILABLESET = null;
    private static Method mObservesDaylightTime = null;
    private static final long serialVersionUID = 6977448185543929364L;
    private volatile transient boolean isFrozen;
    private transient Calendar javacal;
    private java.util.TimeZone javatz;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.JavaTimeZone.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.JavaTimeZone.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.JavaTimeZone.<clinit>():void");
    }

    public JavaTimeZone() {
        this(java.util.TimeZone.getDefault(), null);
    }

    public JavaTimeZone(java.util.TimeZone jtz, String id) {
        this.isFrozen = false;
        if (id == null) {
            id = jtz.getID();
        }
        this.javatz = jtz;
        setID(id);
        this.javacal = new GregorianCalendar(this.javatz);
    }

    public static JavaTimeZone createTimeZone(String id) {
        java.util.TimeZone jtz = null;
        if (AVAILABLESET.contains(id)) {
            jtz = java.util.TimeZone.getTimeZone(id);
        }
        if (jtz == null) {
            boolean[] isSystemID = new boolean[1];
            String canonicalID = TimeZone.getCanonicalID(id, isSystemID);
            if (isSystemID[0] && AVAILABLESET.contains(canonicalID)) {
                jtz = java.util.TimeZone.getTimeZone(canonicalID);
            }
        }
        if (jtz == null) {
            return null;
        }
        return new JavaTimeZone(jtz, id);
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
        return this.javatz.getOffset(era, year, month, day, dayOfWeek, milliseconds);
    }

    public void getOffset(long date, boolean local, int[] offsets) {
        synchronized (this.javacal) {
            if (local) {
                int[] fields = new int[6];
                Grego.timeToFields(date, fields);
                int tmp = fields[5];
                int mil = tmp % Grego.MILLIS_PER_SECOND;
                tmp /= Grego.MILLIS_PER_SECOND;
                int sec = tmp % 60;
                tmp /= 60;
                int min = tmp % 60;
                int hour = tmp / 60;
                this.javacal.clear();
                this.javacal.set(fields[0], fields[1], fields[2], hour, min, sec);
                this.javacal.set(14, mil);
                int doy1 = this.javacal.get(6);
                int hour1 = this.javacal.get(11);
                int min1 = this.javacal.get(12);
                int sec1 = this.javacal.get(13);
                int mil1 = this.javacal.get(14);
                if (fields[4] == doy1 && hour == hour1 && min == min1 && sec == sec1) {
                    if (mil != mil1) {
                    }
                }
                this.javacal.setTimeInMillis((this.javacal.getTimeInMillis() - ((long) (((((((((((((Math.abs(doy1 - fields[4]) > 1 ? 1 : doy1 - fields[4]) * 24) + hour1) - hour) * 60) + min1) - min) * 60) + sec1) - sec) * Grego.MILLIS_PER_SECOND) + mil1) - mil))) - 1);
            } else {
                this.javacal.setTimeInMillis(date);
            }
            offsets[0] = this.javacal.get(15);
            offsets[1] = this.javacal.get(16);
        }
    }

    public int getRawOffset() {
        return this.javatz.getRawOffset();
    }

    public boolean inDaylightTime(Date date) {
        return this.javatz.inDaylightTime(date);
    }

    public void setRawOffset(int offsetMillis) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen JavaTimeZone instance.");
        }
        this.javatz.setRawOffset(offsetMillis);
    }

    public boolean useDaylightTime() {
        return this.javatz.useDaylightTime();
    }

    public boolean observesDaylightTime() {
        if (mObservesDaylightTime != null) {
            try {
                return ((Boolean) mObservesDaylightTime.invoke(this.javatz, (Object[]) null)).booleanValue();
            } catch (IllegalAccessException e) {
            } catch (IllegalArgumentException e2) {
            } catch (InvocationTargetException e3) {
            }
        }
        return super.observesDaylightTime();
    }

    public int getDSTSavings() {
        return this.javatz.getDSTSavings();
    }

    public java.util.TimeZone unwrap() {
        return this.javatz;
    }

    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    public int hashCode() {
        return super.hashCode() + this.javatz.hashCode();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.javacal = new GregorianCalendar(this.javatz);
    }

    public boolean isFrozen() {
        return this.isFrozen;
    }

    public TimeZone freeze() {
        this.isFrozen = true;
        return this;
    }

    public TimeZone cloneAsThawed() {
        JavaTimeZone tz = (JavaTimeZone) super.cloneAsThawed();
        tz.javatz = (java.util.TimeZone) this.javatz.clone();
        tz.javacal = (GregorianCalendar) this.javacal.clone();
        tz.isFrozen = false;
        return tz;
    }
}
