package ohos.com.sun.org.apache.xml.internal.resolver;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CatalogEntry {
    protected static Vector entryArgs = new Vector();
    protected static final Map<String, Integer> entryTypes = new ConcurrentHashMap();
    protected static AtomicInteger nextEntry = new AtomicInteger(0);
    protected Vector args = null;
    protected int entryType = 0;

    static int addEntryType(String str, int i) {
        int andIncrement = nextEntry.getAndIncrement();
        entryTypes.put(str, Integer.valueOf(andIncrement));
        entryArgs.add(andIncrement, Integer.valueOf(i));
        return andIncrement;
    }

    public static int getEntryType(String str) throws CatalogException {
        if (entryTypes.containsKey(str)) {
            Integer num = entryTypes.get(str);
            if (num != null) {
                return num.intValue();
            }
            throw new CatalogException(3);
        }
        throw new CatalogException(3);
    }

    public static int getEntryArgCount(String str) throws CatalogException {
        return getEntryArgCount(getEntryType(str));
    }

    public static int getEntryArgCount(int i) throws CatalogException {
        try {
            return ((Integer) entryArgs.get(i)).intValue();
        } catch (ArrayIndexOutOfBoundsException unused) {
            throw new CatalogException(3);
        }
    }

    public CatalogEntry() {
    }

    public CatalogEntry(String str, Vector vector) throws CatalogException {
        Integer num = entryTypes.get(str);
        if (num != null) {
            int intValue = num.intValue();
            try {
                if (((Integer) entryArgs.get(intValue)).intValue() == vector.size()) {
                    this.entryType = intValue;
                    this.args = vector;
                    return;
                }
                throw new CatalogException(2);
            } catch (ArrayIndexOutOfBoundsException unused) {
                throw new CatalogException(3);
            }
        } else {
            throw new CatalogException(3);
        }
    }

    public CatalogEntry(int i, Vector vector) throws CatalogException {
        try {
            if (((Integer) entryArgs.get(i)).intValue() == vector.size()) {
                this.entryType = i;
                this.args = vector;
                return;
            }
            throw new CatalogException(2);
        } catch (ArrayIndexOutOfBoundsException unused) {
            throw new CatalogException(3);
        }
    }

    public int getEntryType() {
        return this.entryType;
    }

    public String getEntryArg(int i) {
        try {
            return (String) this.args.get(i);
        } catch (ArrayIndexOutOfBoundsException unused) {
            return null;
        }
    }

    public void setEntryArg(int i, String str) throws ArrayIndexOutOfBoundsException {
        this.args.set(i, str);
    }
}
