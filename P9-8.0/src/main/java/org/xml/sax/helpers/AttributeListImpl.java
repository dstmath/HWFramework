package org.xml.sax.helpers;

import java.util.ArrayList;
import org.xml.sax.AttributeList;

@Deprecated
public class AttributeListImpl implements AttributeList {
    private ArrayList<String> names = new ArrayList();
    private ArrayList<String> types = new ArrayList();
    private ArrayList<String> values = new ArrayList();

    public AttributeListImpl(AttributeList atts) {
        setAttributeList(atts);
    }

    public void setAttributeList(AttributeList atts) {
        int count = atts.getLength();
        clear();
        for (int i = 0; i < count; i++) {
            addAttribute(atts.getName(i), atts.getType(i), atts.getValue(i));
        }
    }

    public void addAttribute(String name, String type, String value) {
        this.names.add(name);
        this.types.add(type);
        this.values.add(value);
    }

    public void removeAttribute(String name) {
        int i = this.names.indexOf(name);
        if (i != -1) {
            this.names.remove(i);
            this.types.remove(i);
            this.values.remove(i);
        }
    }

    public void clear() {
        this.names.clear();
        this.types.clear();
        this.values.clear();
    }

    public int getLength() {
        return this.names.size();
    }

    public String getName(int i) {
        if (i < 0 || i >= this.names.size()) {
            return null;
        }
        return (String) this.names.get(i);
    }

    public String getType(int i) {
        if (i < 0 || i >= this.types.size()) {
            return null;
        }
        return (String) this.types.get(i);
    }

    public String getValue(int i) {
        if (i < 0 || i >= this.values.size()) {
            return null;
        }
        return (String) this.values.get(i);
    }

    public String getType(String name) {
        return getType(this.names.indexOf(name));
    }

    public String getValue(String name) {
        return getValue(this.names.indexOf(name));
    }
}
