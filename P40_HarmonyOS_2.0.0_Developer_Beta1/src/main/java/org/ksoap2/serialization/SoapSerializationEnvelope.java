package org.ksoap2.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.SoapFault12;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SoapSerializationEnvelope extends SoapEnvelope {
    private static final String ANY_TYPE_LABEL = "anyType";
    private static final String ARRAY_MAPPING_NAME = "Array";
    private static final String ARRAY_TYPE_LABEL = "arrayType";
    static final Marshal DEFAULT_MARSHAL = new DM();
    private static final String HREF_LABEL = "href";
    private static final String ID_LABEL = "id";
    private static final String ITEM_LABEL = "item";
    protected static final String NIL_LABEL = "nil";
    protected static final String NULL_LABEL = "null";
    protected static final int QNAME_MARSHAL = 3;
    protected static final int QNAME_NAMESPACE = 0;
    protected static final int QNAME_TYPE = 1;
    private static final String ROOT_LABEL = "root";
    private static final String TYPE_LABEL = "type";
    protected boolean addAdornments = true;
    public boolean avoidExceptionForUnknownProperty;
    protected Hashtable classToQName = new Hashtable();
    public boolean dotNet;
    Hashtable idMap = new Hashtable();
    public boolean implicitTypes;
    Vector multiRef;
    public Hashtable properties = new Hashtable();
    protected Hashtable qNameToClass = new Hashtable();
    public boolean skipNullProperties;

    public SoapSerializationEnvelope(int version) {
        super(version);
        addMapping(this.enc, ARRAY_MAPPING_NAME, PropertyInfo.VECTOR_CLASS);
        DEFAULT_MARSHAL.register(this);
    }

    public boolean isAddAdornments() {
        return this.addAdornments;
    }

    public void setAddAdornments(boolean addAdornments2) {
        this.addAdornments = addAdornments2;
    }

    public void setBodyOutEmpty(boolean emptyBody) {
        if (emptyBody) {
            this.bodyOut = null;
        }
    }

    @Override // org.ksoap2.SoapEnvelope
    public void parseBody(XmlPullParser parser) throws IOException, XmlPullParserException {
        SoapFault fault;
        this.bodyIn = null;
        parser.nextTag();
        if (parser.getEventType() != 2 || !parser.getNamespace().equals(this.env) || !parser.getName().equals("Fault")) {
            while (parser.getEventType() == 2) {
                String rootAttr = parser.getAttributeValue(this.enc, ROOT_LABEL);
                Object o = read(parser, null, -1, parser.getNamespace(), parser.getName(), PropertyInfo.OBJECT_TYPE);
                if ("1".equals(rootAttr) || this.bodyIn == null) {
                    this.bodyIn = o;
                }
                parser.nextTag();
            }
            return;
        }
        if (this.version < 120) {
            fault = new SoapFault(this.version);
        } else {
            fault = new SoapFault12(this.version);
        }
        fault.parse(parser);
        this.bodyIn = fault;
    }

    /* access modifiers changed from: protected */
    public void readSerializable(XmlPullParser parser, SoapObject obj) throws IOException, XmlPullParserException {
        for (int counter = 0; counter < parser.getAttributeCount(); counter++) {
            obj.addAttribute(parser.getAttributeName(counter), parser.getAttributeValue(counter));
        }
        readSerializable(parser, (KvmSerializable) obj);
    }

    /* access modifiers changed from: protected */
    public void readSerializable(XmlPullParser parser, KvmSerializable obj) throws IOException, XmlPullParserException {
        int tag;
        String str;
        int i;
        String str2;
        try {
            tag = parser.nextTag();
        } catch (XmlPullParserException e) {
            if (obj instanceof HasInnerText) {
                HasInnerText hasInnerText = (HasInnerText) obj;
                if (parser.getText() != null) {
                    str2 = parser.getText();
                } else {
                    str2 = "";
                }
                hasInnerText.setInnerText(str2);
            }
            tag = parser.nextTag();
        }
        while (tag != 3) {
            String name = parser.getName();
            if (!this.implicitTypes || !(obj instanceof SoapObject)) {
                PropertyInfo info = new PropertyInfo();
                int propertyCount = obj.getPropertyCount();
                boolean propertyFound = false;
                int i2 = 0;
                while (i2 < propertyCount && !propertyFound) {
                    info.clear();
                    obj.getPropertyInfo(i2, this.properties, info);
                    if ((!name.equals(info.name) || info.namespace != null) && (!name.equals(info.name) || !parser.getNamespace().equals(info.namespace))) {
                        i = i2;
                    } else {
                        i = i2;
                        obj.setProperty(i, read(parser, obj, i2, null, null, info));
                        propertyFound = true;
                    }
                    i2 = i + 1;
                }
                if (!propertyFound) {
                    if (this.avoidExceptionForUnknownProperty) {
                        while (true) {
                            if (parser.next() != 3 || !name.equals(parser.getName())) {
                            }
                        }
                        tag = parser.nextTag();
                    } else {
                        throw new RuntimeException("Unknown Property: " + name);
                    }
                } else if (obj instanceof HasAttributes) {
                    HasAttributes soapObject = (HasAttributes) obj;
                    int cnt = parser.getAttributeCount();
                    for (int counter = 0; counter < cnt; counter++) {
                        AttributeInfo attributeInfo = new AttributeInfo();
                        attributeInfo.setName(parser.getAttributeName(counter));
                        attributeInfo.setValue(parser.getAttributeValue(counter));
                        attributeInfo.setNamespace(parser.getAttributeNamespace(counter));
                        attributeInfo.setType(parser.getAttributeType(counter));
                        soapObject.setAttribute(attributeInfo);
                    }
                }
            } else {
                ((SoapObject) obj).addProperty(parser.getName(), read(parser, obj, obj.getPropertyCount(), ((SoapObject) obj).getNamespace(), name, PropertyInfo.OBJECT_TYPE));
            }
            try {
                tag = parser.nextTag();
            } catch (XmlPullParserException e2) {
                if (obj instanceof HasInnerText) {
                    HasInnerText hasInnerText2 = (HasInnerText) obj;
                    if (parser.getText() != null) {
                        str = parser.getText();
                    } else {
                        str = "";
                    }
                    hasInnerText2.setInnerText(str);
                }
                tag = parser.nextTag();
            }
        }
        parser.require(3, null, null);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v4, resolved type: org.ksoap2.serialization.SoapPrimitive */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x009b  */
    public Object readUnknown(XmlPullParser parser, String typeNamespace, String typeName) throws IOException, XmlPullParserException {
        Object result;
        String text;
        String name = parser.getName();
        String namespace = parser.getNamespace();
        Vector attributeInfoVector = new Vector();
        for (int attributeCount = 0; attributeCount < parser.getAttributeCount(); attributeCount++) {
            AttributeInfo attributeInfo = new AttributeInfo();
            attributeInfo.setName(parser.getAttributeName(attributeCount));
            attributeInfo.setValue(parser.getAttributeValue(attributeCount));
            attributeInfo.setNamespace(parser.getAttributeNamespace(attributeCount));
            attributeInfo.setType(parser.getAttributeType(attributeCount));
            attributeInfoVector.addElement(attributeInfo);
        }
        parser.next();
        Object result2 = null;
        String text2 = null;
        if (parser.getEventType() == 4) {
            text2 = parser.getText();
            SoapPrimitive sp = new SoapPrimitive(typeNamespace, typeName, text2);
            result2 = sp;
            for (int i = 0; i < attributeInfoVector.size(); i++) {
                sp.addAttribute((AttributeInfo) attributeInfoVector.elementAt(i));
            }
            parser.next();
        } else if (parser.getEventType() == 3) {
            SoapObject so = new SoapObject(typeNamespace, typeName);
            for (int i2 = 0; i2 < attributeInfoVector.size(); i2++) {
                so.addAttribute((AttributeInfo) attributeInfoVector.elementAt(i2));
            }
            result = so;
            text = null;
            if (parser.getEventType() == 2) {
                if (text == null || text.trim().length() == 0) {
                    SoapObject so2 = new SoapObject(typeNamespace, typeName);
                    for (int i3 = 0; i3 < attributeInfoVector.size(); i3++) {
                        so2.addAttribute((AttributeInfo) attributeInfoVector.elementAt(i3));
                    }
                    for (int i4 = 3; parser.getEventType() != i4; i4 = 3) {
                        so2.addProperty(parser.getNamespace(), parser.getName(), read(parser, so2, so2.getPropertyCount(), null, null, PropertyInfo.OBJECT_TYPE));
                        parser.nextTag();
                        so2 = so2;
                    }
                    result = so2;
                } else {
                    throw new RuntimeException("Malformed input: Mixed content");
                }
            }
            parser.require(3, namespace, name);
            return result;
        }
        result = result2;
        text = text2;
        if (parser.getEventType() == 2) {
        }
        parser.require(3, namespace, name);
        return result;
    }

    private int getIndex(String value, int start, int dflt) {
        if (value == null) {
            return dflt;
        }
        try {
            return value.length() - start < 3 ? dflt : Integer.parseInt(value.substring(start + 1, value.length() - 1));
        } catch (Exception e) {
            return dflt;
        }
    }

    /* access modifiers changed from: protected */
    public void readVector(XmlPullParser parser, Vector v, PropertyInfo elementType) throws IOException, XmlPullParserException {
        String name;
        String namespace;
        String prefix;
        PropertyInfo elementType2;
        int size;
        int size2 = v.size();
        String type = parser.getAttributeValue(this.enc, ARRAY_TYPE_LABEL);
        int i = 0;
        if (type != null) {
            int cut0 = type.indexOf(58);
            int cut1 = type.indexOf("[", cut0);
            String name2 = type.substring(cut0 + 1, cut1);
            String namespace2 = parser.getNamespace(cut0 == -1 ? "" : type.substring(0, cut0));
            size2 = getIndex(type, cut1, -1);
            if (size2 != -1) {
                v.setSize(size2);
                namespace = namespace2;
                name = name2;
                prefix = null;
            } else {
                namespace = namespace2;
                name = name2;
                prefix = 1;
            }
        } else {
            namespace = null;
            name = null;
            prefix = 1;
        }
        if (elementType == null) {
            elementType2 = PropertyInfo.OBJECT_TYPE;
        } else {
            elementType2 = elementType;
        }
        parser.nextTag();
        int position = getIndex(parser.getAttributeValue(this.enc, "offset"), 0, 0);
        while (parser.getEventType() != 3) {
            int position2 = getIndex(parser.getAttributeValue(this.enc, "position"), i, position);
            if (prefix == null || position2 < size2) {
                size = size2;
            } else {
                int size3 = position2 + 1;
                v.setSize(size3);
                size = size3;
            }
            v.setElementAt(read(parser, v, position2, namespace, name, elementType2), position2);
            position = position2 + 1;
            parser.nextTag();
            size2 = size;
            i = 0;
        }
        parser.require(3, null, null);
    }

    /* access modifiers changed from: protected */
    public String getIdFromHref(String hrefValue) {
        return hrefValue.substring(1);
    }

    public Object read(XmlPullParser parser, Object owner, int index, String namespace, String name, PropertyInfo expected) throws IOException, XmlPullParserException {
        Object obj;
        String name2;
        String prefix;
        String elementName = parser.getName();
        String href = parser.getAttributeValue(null, HREF_LABEL);
        if (href == null) {
            String nullAttr = parser.getAttributeValue(this.xsi, NIL_LABEL);
            String id = parser.getAttributeValue(null, ID_LABEL);
            if (nullAttr == null) {
                nullAttr = parser.getAttributeValue(this.xsi, NULL_LABEL);
            }
            if (nullAttr == null || !SoapEnvelope.stringToBoolean(nullAttr)) {
                String type = parser.getAttributeValue(this.xsi, TYPE_LABEL);
                if (type != null) {
                    int cut = type.indexOf(58);
                    String name3 = type.substring(cut + 1);
                    prefix = parser.getNamespace(cut == -1 ? "" : type.substring(0, cut));
                    name2 = name3;
                } else if (name != null || namespace != null) {
                    prefix = namespace;
                    name2 = name;
                } else if (parser.getAttributeValue(this.enc, ARRAY_TYPE_LABEL) != null) {
                    prefix = this.enc;
                    name2 = ARRAY_MAPPING_NAME;
                } else {
                    Object[] names = getInfo(expected.type, null);
                    prefix = (String) names[0];
                    name2 = (String) names[1];
                }
                if (type == null) {
                    this.implicitTypes = true;
                }
                obj = readInstance(parser, prefix, name2, expected);
                if (obj == null) {
                    obj = readUnknown(parser, prefix, name2);
                }
            } else {
                parser.nextTag();
                parser.require(3, null, elementName);
                obj = null;
            }
            if (id != null) {
                resolveReference(id, obj);
            }
        } else if (owner != null) {
            String href2 = getIdFromHref(href);
            Object obj2 = this.idMap.get(href2);
            if (obj2 == null || (obj2 instanceof FwdRef)) {
                FwdRef f = new FwdRef();
                f.next = (FwdRef) obj2;
                f.obj = owner;
                f.index = index;
                this.idMap.put(href2, f);
                obj2 = null;
            }
            parser.nextTag();
            parser.require(3, null, elementName);
            obj = obj2;
        } else {
            throw new RuntimeException("href at root level?!?");
        }
        parser.require(3, null, elementName);
        return obj;
    }

    /* access modifiers changed from: protected */
    public void resolveReference(String id, Object obj) {
        Object hlp = this.idMap.get(id);
        if (hlp instanceof FwdRef) {
            FwdRef f = (FwdRef) hlp;
            do {
                if (f.obj instanceof KvmSerializable) {
                    ((KvmSerializable) f.obj).setProperty(f.index, obj);
                } else {
                    ((Vector) f.obj).setElementAt(obj, f.index);
                }
                f = f.next;
            } while (f != null);
        } else if (hlp != null) {
            throw new RuntimeException("double ID");
        }
        this.idMap.put(id, obj);
    }

    public Object readInstance(XmlPullParser parser, String namespace, String name, PropertyInfo expected) throws IOException, XmlPullParserException {
        Object obj;
        Object obj2 = this.qNameToClass.get(new SoapPrimitive(namespace, name, null));
        if (obj2 == null) {
            return null;
        }
        if (obj2 instanceof Marshal) {
            return ((Marshal) obj2).readInstance(parser, namespace, name, expected);
        }
        if (obj2 instanceof SoapObject) {
            obj = ((SoapObject) obj2).newInstance();
        } else if (obj2 == SoapObject.class) {
            obj = new SoapObject(namespace, name);
        } else {
            try {
                obj = ((Class) obj2).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e.toString());
            }
        }
        if (obj instanceof HasAttributes) {
            HasAttributes soapObject = (HasAttributes) obj;
            int cnt = parser.getAttributeCount();
            for (int counter = 0; counter < cnt; counter++) {
                AttributeInfo attributeInfo = new AttributeInfo();
                attributeInfo.setName(parser.getAttributeName(counter));
                attributeInfo.setValue(parser.getAttributeValue(counter));
                attributeInfo.setNamespace(parser.getAttributeNamespace(counter));
                attributeInfo.setType(parser.getAttributeType(counter));
                soapObject.setAttribute(attributeInfo);
            }
        }
        if (obj instanceof SoapObject) {
            readSerializable(parser, (SoapObject) obj);
        } else if (obj instanceof KvmSerializable) {
            if (obj instanceof HasInnerText) {
                ((HasInnerText) obj).setInnerText(parser.getText() != null ? parser.getText() : "");
            }
            readSerializable(parser, (KvmSerializable) obj);
        } else if (obj instanceof Vector) {
            readVector(parser, (Vector) obj, expected.elementType);
        } else {
            throw new RuntimeException("no deserializer for " + obj.getClass());
        }
        return obj;
    }

    public Object[] getInfo(Object type, Object instance) {
        Object[] tmp;
        if (type == null) {
            if ((instance instanceof SoapObject) || (instance instanceof SoapPrimitive)) {
                type = instance;
            } else {
                type = instance.getClass();
            }
        }
        if (type instanceof SoapObject) {
            SoapObject so = (SoapObject) type;
            return new Object[]{so.getNamespace(), so.getName(), null, null};
        } else if (!(type instanceof SoapPrimitive)) {
            return (!(type instanceof Class) || type == PropertyInfo.OBJECT_CLASS || (tmp = (Object[]) this.classToQName.get(((Class) type).getName())) == null) ? new Object[]{this.xsd, ANY_TYPE_LABEL, null, null} : tmp;
        } else {
            SoapPrimitive sp = (SoapPrimitive) type;
            return new Object[]{sp.getNamespace(), sp.getName(), null, DEFAULT_MARSHAL};
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r9v0, resolved type: org.ksoap2.serialization.Marshal */
    /* JADX WARN: Multi-variable type inference failed */
    public void addMapping(String namespace, String name, Class clazz, Marshal marshal) {
        this.qNameToClass.put(new SoapPrimitive(namespace, name, null), marshal == 0 ? clazz : marshal);
        this.classToQName.put(clazz.getName(), new Object[]{namespace, name, null, marshal});
    }

    public void addMapping(String namespace, String name, Class clazz) {
        addMapping(namespace, name, clazz, null);
    }

    public void addTemplate(SoapObject so) {
        this.qNameToClass.put(new SoapPrimitive(so.namespace, so.name, null), so);
    }

    public Object getResponse() throws SoapFault {
        if (this.bodyIn == null) {
            return null;
        }
        if (!(this.bodyIn instanceof SoapFault)) {
            KvmSerializable ks = (KvmSerializable) this.bodyIn;
            if (ks.getPropertyCount() == 0) {
                return null;
            }
            if (ks.getPropertyCount() == 1) {
                return ks.getProperty(0);
            }
            Vector ret = new Vector();
            for (int i = 0; i < ks.getPropertyCount(); i++) {
                ret.add(ks.getProperty(i));
            }
            return ret;
        }
        throw ((SoapFault) this.bodyIn);
    }

    @Override // org.ksoap2.SoapEnvelope
    public void writeBody(XmlSerializer writer) throws IOException {
        if (this.bodyOut != null) {
            this.multiRef = new Vector();
            this.multiRef.addElement(this.bodyOut);
            Object[] qName = getInfo(null, this.bodyOut);
            String str = "";
            writer.startTag(this.dotNet ? str : (String) qName[0], (String) qName[1]);
            if (this.dotNet) {
                writer.attribute(null, "xmlns", (String) qName[0]);
            }
            if (this.addAdornments) {
                writer.attribute(null, ID_LABEL, qName[2] == null ? "o0" : (String) qName[2]);
                writer.attribute(this.enc, ROOT_LABEL, "1");
            }
            writeElement(writer, this.bodyOut, null, qName[3]);
            if (!this.dotNet) {
                str = (String) qName[0];
            }
            writer.endTag(str, (String) qName[1]);
        }
    }

    private void writeAttributes(XmlSerializer writer, HasAttributes obj) throws IOException {
        int cnt = obj.getAttributeCount();
        for (int counter = 0; counter < cnt; counter++) {
            AttributeInfo attributeInfo = new AttributeInfo();
            obj.getAttributeInfo(counter, attributeInfo);
            obj.getAttribute(counter, attributeInfo);
            if (attributeInfo.getValue() != null) {
                writer.attribute(attributeInfo.getNamespace(), attributeInfo.getName(), attributeInfo.getValue().toString());
            }
        }
    }

    public void writeArrayListBodyWithAttributes(XmlSerializer writer, KvmSerializable obj) throws IOException {
        if (obj instanceof HasAttributes) {
            writeAttributes(writer, (HasAttributes) obj);
        }
        writeArrayListBody(writer, (ArrayList) obj);
    }

    public void writeObjectBodyWithAttributes(XmlSerializer writer, KvmSerializable obj) throws IOException {
        if (obj instanceof HasAttributes) {
            writeAttributes(writer, (HasAttributes) obj);
        }
        writeObjectBody(writer, obj);
    }

    public void writeObjectBody(XmlSerializer writer, KvmSerializable obj) throws IOException {
        String name;
        String namespace;
        int cnt = obj.getPropertyCount();
        PropertyInfo propertyInfo = new PropertyInfo();
        for (int i = 0; i < cnt; i++) {
            Object prop = obj.getProperty(i);
            obj.getPropertyInfo(i, this.properties, propertyInfo);
            if (prop instanceof SoapObject) {
                SoapObject nestedSoap = (SoapObject) prop;
                Object[] qName = getInfo(null, nestedSoap);
                String str = (String) qName[0];
                String type = (String) qName[1];
                if (propertyInfo.name == null || propertyInfo.name.length() <= 0) {
                    name = (String) qName[1];
                } else {
                    name = propertyInfo.name;
                }
                if (propertyInfo.namespace == null || propertyInfo.namespace.length() <= 0) {
                    namespace = (String) qName[0];
                } else {
                    namespace = propertyInfo.namespace;
                }
                writer.startTag(namespace, name);
                if (!this.implicitTypes) {
                    String prefix = writer.getPrefix(namespace, true);
                    String str2 = this.xsi;
                    writer.attribute(str2, TYPE_LABEL, prefix + ":" + type);
                }
                writeObjectBodyWithAttributes(writer, nestedSoap);
                writer.endTag(namespace, name);
            } else if ((propertyInfo.flags & 1) == 0) {
                Object objValue = obj.getProperty(i);
                if ((prop != null || !this.skipNullProperties) && objValue != SoapPrimitive.NullSkip) {
                    writer.startTag(propertyInfo.namespace, propertyInfo.name);
                    writeProperty(writer, objValue, propertyInfo);
                    writer.endTag(propertyInfo.namespace, propertyInfo.name);
                }
            }
        }
        writeInnerText(writer, obj);
    }

    private void writeInnerText(XmlSerializer writer, KvmSerializable obj) throws IOException {
        Object value;
        if ((obj instanceof HasInnerText) && (value = ((HasInnerText) obj).getInnerText()) != null) {
            if (value instanceof ValueWriter) {
                ((ValueWriter) value).write(writer);
            } else {
                writer.cdsect(value.toString());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void writeProperty(XmlSerializer writer, Object obj, PropertyInfo type) throws IOException {
        String str;
        if (obj == null || obj == SoapPrimitive.NullNilElement) {
            writer.attribute(this.xsi, this.version >= 120 ? NIL_LABEL : NULL_LABEL, "true");
            return;
        }
        Object[] qName = getInfo(null, obj);
        if (type.multiRef || qName[2] != null) {
            int i = this.multiRef.indexOf(obj);
            if (i == -1) {
                i = this.multiRef.size();
                this.multiRef.addElement(obj);
            }
            if (qName[2] == null) {
                str = "#o" + i;
            } else {
                str = "#" + qName[2];
            }
            writer.attribute(null, HREF_LABEL, str);
            return;
        }
        if (!this.implicitTypes || obj.getClass() != type.type) {
            String prefix = writer.getPrefix((String) qName[0], true);
            writer.attribute(this.xsi, TYPE_LABEL, prefix + ":" + qName[1]);
        }
        writeElement(writer, obj, type, qName[3]);
    }

    /* access modifiers changed from: protected */
    public void writeElement(XmlSerializer writer, Object element, PropertyInfo type, Object marshal) throws IOException {
        if (marshal != null) {
            ((Marshal) marshal).writeInstance(writer, element);
        } else if ((element instanceof KvmSerializable) || element == SoapPrimitive.NullNilElement || element == SoapPrimitive.NullSkip) {
            if (element instanceof ArrayList) {
                writeArrayListBodyWithAttributes(writer, (KvmSerializable) element);
            } else {
                writeObjectBodyWithAttributes(writer, (KvmSerializable) element);
            }
        } else if (element instanceof HasAttributes) {
            writeAttributes(writer, (HasAttributes) element);
        } else if (element instanceof Vector) {
            writeVectorBody(writer, (Vector) element, type.elementType);
        } else {
            throw new RuntimeException("Cannot serialize: " + element);
        }
    }

    /* access modifiers changed from: protected */
    public void writeArrayListBody(XmlSerializer writer, ArrayList list) throws IOException {
        String name;
        String namespace;
        KvmSerializable obj = (KvmSerializable) list;
        int cnt = list.size();
        PropertyInfo propertyInfo = new PropertyInfo();
        for (int i = 0; i < cnt; i++) {
            Object prop = obj.getProperty(i);
            obj.getPropertyInfo(i, this.properties, propertyInfo);
            if (prop instanceof SoapObject) {
                SoapObject nestedSoap = (SoapObject) prop;
                Object[] qName = getInfo(null, nestedSoap);
                String str = (String) qName[0];
                String type = (String) qName[1];
                if (propertyInfo.name == null || propertyInfo.name.length() <= 0) {
                    name = (String) qName[1];
                } else {
                    name = propertyInfo.name;
                }
                if (propertyInfo.namespace == null || propertyInfo.namespace.length() <= 0) {
                    namespace = (String) qName[0];
                } else {
                    namespace = propertyInfo.namespace;
                }
                writer.startTag(namespace, name);
                if (!this.implicitTypes) {
                    String prefix = writer.getPrefix(namespace, true);
                    String str2 = this.xsi;
                    writer.attribute(str2, TYPE_LABEL, prefix + ":" + type);
                }
                writeObjectBodyWithAttributes(writer, nestedSoap);
                writer.endTag(namespace, name);
            } else if ((propertyInfo.flags & 1) == 0) {
                Object objValue = obj.getProperty(i);
                if ((prop != null || !this.skipNullProperties) && objValue != SoapPrimitive.NullSkip) {
                    writer.startTag(propertyInfo.namespace, propertyInfo.name);
                    writeProperty(writer, objValue, propertyInfo);
                    writer.endTag(propertyInfo.namespace, propertyInfo.name);
                }
            }
        }
        writeInnerText(writer, obj);
    }

    /* access modifiers changed from: protected */
    public void writeVectorBody(XmlSerializer writer, Vector vector, PropertyInfo elementType) throws IOException {
        String itemsTagName = ITEM_LABEL;
        String itemsNamespace = null;
        if (elementType == null) {
            elementType = PropertyInfo.OBJECT_TYPE;
        } else if ((elementType instanceof PropertyInfo) && elementType.name != null) {
            itemsTagName = elementType.name;
            itemsNamespace = elementType.namespace;
        }
        int cnt = vector.size();
        Object[] arrType = getInfo(elementType.type, null);
        if (!this.implicitTypes) {
            String str = this.enc;
            writer.attribute(str, ARRAY_TYPE_LABEL, writer.getPrefix((String) arrType[0], false) + ":" + arrType[1] + "[" + cnt + "]");
        } else if (itemsNamespace == null) {
            itemsNamespace = (String) arrType[0];
        }
        boolean skipped = false;
        for (int i = 0; i < cnt; i++) {
            if (vector.elementAt(i) == null) {
                skipped = true;
            } else {
                writer.startTag(itemsNamespace, itemsTagName);
                if (skipped) {
                    String str2 = this.enc;
                    writer.attribute(str2, "position", "[" + i + "]");
                    skipped = false;
                }
                writeProperty(writer, vector.elementAt(i), elementType);
                writer.endTag(itemsNamespace, itemsTagName);
            }
        }
    }
}
