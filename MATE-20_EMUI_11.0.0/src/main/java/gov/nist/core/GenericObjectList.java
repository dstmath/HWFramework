package gov.nist.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public abstract class GenericObjectList extends LinkedList<GenericObject> implements Serializable, Cloneable {
    protected static final String AND = "&";
    protected static final String AT = "@";
    protected static final String COLON = ":";
    protected static final String COMMA = ",";
    protected static final String DOT = ".";
    protected static final String DOUBLE_QUOTE = "\"";
    protected static final String EQUALS = "=";
    protected static final String GREATER_THAN = ">";
    protected static final String HT = "\t";
    protected static final String LESS_THAN = "<";
    protected static final String LPAREN = "(";
    protected static final String NEWLINE = "\r\n";
    protected static final String PERCENT = "%";
    protected static final String POUND = "#";
    protected static final String QUESTION = "?";
    protected static final String QUOTE = "'";
    protected static final String RETURN = "\n";
    protected static final String RPAREN = ")";
    protected static final String SEMICOLON = ";";
    protected static final String SLASH = "/";
    protected static final String SP = " ";
    protected static final String STAR = "*";
    protected int indentation;
    protected String listName;
    protected Class<?> myClass;
    private ListIterator<? extends GenericObject> myListIterator;
    protected String separator;
    private String stringRep;

    /* access modifiers changed from: protected */
    public String getIndentation() {
        char[] chars = new char[this.indentation];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    protected static boolean isCloneable(Object obj) {
        return obj instanceof Cloneable;
    }

    public static boolean isMySubclass(Class<?> other) {
        return GenericObjectList.class.isAssignableFrom(other);
    }

    @Override // java.util.LinkedList, java.lang.Object
    public Object clone() {
        GenericObjectList retval = (GenericObjectList) super.clone();
        ListIterator<GenericObject> iter = retval.listIterator();
        while (iter.hasNext()) {
            iter.set((GenericObject) iter.next().clone());
        }
        return retval;
    }

    public void setMyClass(Class cl) {
        this.myClass = cl;
    }

    protected GenericObjectList() {
        this.listName = null;
        this.stringRep = "";
        this.separator = ";";
    }

    protected GenericObjectList(String lname) {
        this();
        this.listName = lname;
    }

    protected GenericObjectList(String lname, String classname) {
        this(lname);
        try {
            this.myClass = Class.forName(classname);
        } catch (ClassNotFoundException ex) {
            InternalErrorHandler.handleException(ex);
        }
    }

    protected GenericObjectList(String lname, Class objclass) {
        this(lname);
        this.myClass = objclass;
    }

    /* access modifiers changed from: protected */
    public GenericObject next(ListIterator iterator) {
        try {
            return (GenericObject) iterator.next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public GenericObject first() {
        this.myListIterator = listIterator(0);
        try {
            return (GenericObject) this.myListIterator.next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public GenericObject next() {
        if (this.myListIterator == null) {
            this.myListIterator = listIterator(0);
        }
        try {
            return (GenericObject) this.myListIterator.next();
        } catch (NoSuchElementException e) {
            this.myListIterator = null;
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void concatenate(GenericObjectList objList) {
        concatenate(objList, false);
    }

    /* access modifiers changed from: protected */
    public void concatenate(GenericObjectList objList, boolean topFlag) {
        if (!topFlag) {
            addAll(objList);
        } else {
            addAll(0, objList);
        }
    }

    private void sprint(String s) {
        if (s == null) {
            this.stringRep += getIndentation();
            this.stringRep += "<null>\n";
            return;
        }
        if (s.compareTo("}") == 0 || s.compareTo("]") == 0) {
            this.indentation--;
        }
        this.stringRep += getIndentation();
        this.stringRep += s;
        this.stringRep += "\n";
        if (s.compareTo("{") == 0 || s.compareTo("[") == 0) {
            this.indentation++;
        }
    }

    public String debugDump() {
        this.stringRep = "";
        Object obj = first();
        if (obj == null) {
            return "<null>";
        }
        sprint("listName:");
        sprint(this.listName);
        sprint("{");
        while (obj != null) {
            sprint("[");
            sprint(((GenericObject) obj).debugDump(this.indentation));
            obj = next();
            sprint("]");
        }
        sprint("}");
        return this.stringRep;
    }

    public String debugDump(int indent) {
        int save = this.indentation;
        this.indentation = indent;
        String retval = debugDump();
        this.indentation = save;
        return retval;
    }

    public void addFirst(GenericObject objToAdd) {
        if (this.myClass == null) {
            this.myClass = objToAdd.getClass();
        } else {
            super.addFirst((GenericObjectList) objToAdd);
        }
    }

    public void mergeObjects(GenericObjectList mergeList) {
        if (mergeList != null) {
            Iterator it1 = listIterator();
            Iterator it2 = mergeList.listIterator();
            while (it1.hasNext()) {
                GenericObject outerObj = (GenericObject) it1.next();
                while (it2.hasNext()) {
                    outerObj.merge(it2.next());
                }
            }
        }
    }

    public String encode() {
        if (isEmpty()) {
            return "";
        }
        StringBuffer encoding = new StringBuffer();
        ListIterator iterator = listIterator();
        if (iterator.hasNext()) {
            while (true) {
                Object obj = iterator.next();
                if (obj instanceof GenericObject) {
                    encoding.append(((GenericObject) obj).encode());
                } else {
                    encoding.append(obj.toString());
                }
                if (!iterator.hasNext()) {
                    break;
                }
                encoding.append(this.separator);
            }
        }
        return encoding.toString();
    }

    @Override // java.util.AbstractCollection, java.lang.Object
    public String toString() {
        return encode();
    }

    public void setSeparator(String sep) {
        this.separator = sep;
    }

    @Override // java.util.List, java.util.Collection, java.util.AbstractList, java.lang.Object
    public int hashCode() {
        return 42;
    }

    @Override // java.util.List, java.util.Collection, java.util.AbstractList, java.lang.Object
    public boolean equals(Object other) {
        if (other == null || !getClass().equals(other.getClass())) {
            return false;
        }
        GenericObjectList that = (GenericObjectList) other;
        if (size() != that.size()) {
            return false;
        }
        ListIterator myIterator = listIterator();
        while (myIterator.hasNext()) {
            do {
                try {
                } catch (NoSuchElementException e) {
                    return false;
                }
            } while (!myIterator.next().equals(that.listIterator().next()));
        }
        ListIterator hisIterator = that.listIterator();
        while (hisIterator.hasNext()) {
            do {
                try {
                } catch (NoSuchElementException e2) {
                    return false;
                }
            } while (!hisIterator.next().equals(listIterator().next()));
        }
        return true;
    }

    public boolean match(Object other) {
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        ListIterator hisIterator = ((GenericObjectList) other).listIterator();
        if (!hisIterator.hasNext()) {
            return true;
        }
        Object hisobj = hisIterator.next();
        ListIterator myIterator = listIterator();
        while (myIterator.hasNext()) {
            Object myobj = myIterator.next();
            if (myobj instanceof GenericObject) {
                PrintStream printStream = System.out;
                printStream.println("Trying to match  = " + ((GenericObject) myobj).encode());
            }
            if (GenericObject.isMySubclass(myobj.getClass()) && ((GenericObject) myobj).match(hisobj)) {
                return true;
            }
            if (isMySubclass(myobj.getClass()) && ((GenericObjectList) myobj).match(hisobj)) {
                return true;
            }
        }
        System.out.println(((GenericObject) hisobj).encode());
        return false;
    }
}
