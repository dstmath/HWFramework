package sun.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class ResourceBundleEnumeration implements Enumeration<String> {
    Enumeration<String> enumeration;
    Iterator<String> iterator;
    String next = null;
    Set<String> set;

    public ResourceBundleEnumeration(Set<String> set, Enumeration<String> enumeration) {
        this.set = set;
        this.iterator = set.iterator();
        this.enumeration = enumeration;
    }

    public boolean hasMoreElements() {
        if (this.next == null) {
            if (this.iterator.hasNext()) {
                this.next = (String) this.iterator.next();
            } else if (this.enumeration != null) {
                while (this.next == null && this.enumeration.hasMoreElements()) {
                    this.next = (String) this.enumeration.nextElement();
                    if (this.set.contains(this.next)) {
                        this.next = null;
                    }
                }
            }
        }
        if (this.next != null) {
            return true;
        }
        return false;
    }

    public String nextElement() {
        if (hasMoreElements()) {
            String result = this.next;
            this.next = null;
            return result;
        }
        throw new NoSuchElementException();
    }
}
