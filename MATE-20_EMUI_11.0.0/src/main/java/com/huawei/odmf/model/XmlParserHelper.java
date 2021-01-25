package com.huawei.odmf.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Xml;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFXmlParserException;
import com.huawei.odmf.model.api.ObjectModel;
import com.huawei.odmf.utils.JudgeUtils;
import com.huawei.odmf.utils.LOG;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class XmlParserHelper {
    private static final String DEFAULT_DATABASE_NAME = "NaturalBase";
    private static final String DEFAULT_VERSION = "1.0.0";
    private static final int DEFAULT_VERSION_CODE = 1;
    private static final String XML_BLOB = "Blob";
    private static final String XML_BOOLEAN = "Boolean";
    private static final String XML_BYTE = "Byte";
    private static final String XML_CALENDAR = "Calendar";
    private static final String XML_CASCADE = "cascade";
    private static final String XML_CHARACTER = "Character";
    private static final String XML_CLASS = "class";
    private static final String XML_CLOB = "Clob";
    private static final String XML_COMPOSITE_ID = "composite-id";
    private static final String XML_COMPOSITE_INDEX = "composite-index";
    private static final String XML_DATABASE_NAME = "databaseName";
    private static final String XML_DATE = "Date";
    private static final String XML_DEFAULT = "default";
    private static final String XML_DOUBLE = "Double";
    private static final String XML_FALSE = "false";
    private static final String XML_FLOAT = "Float";
    private static final String XML_GENERATOR = "generator";
    private static final String XML_ID = "id";
    private static final String XML_INDEX = "index";
    private static final String XML_INDEX_PROPERTY = "index-property";
    private static final String XML_INT = "Integer";
    private static final String XML_KEY_PROPERTY = "key-property";
    private static final String XML_LAZY = "lazy";
    private static final String XML_LONG = "Long";
    private static final String XML_MAPPED_BY = "mapped-by";
    private static final String XML_NAME = "name";
    private static final String XML_NATURAL_BASE_MAPPING = "NaturalBase-mapping";
    private static final String XML_NOT_FOUND = "not-found";
    private static final String XML_NOT_NULL = "not_null";
    private static final String XML_PACKAGE = "package";
    private static final String XML_PRIM_BOOLEAN = "boolean";
    private static final String XML_PRIM_BYTE = "byte";
    private static final String XML_PRIM_CHAR = "char";
    private static final String XML_PRIM_DOUBLE = "double";
    private static final String XML_PRIM_FLOAT = "float";
    private static final String XML_PRIM_INT = "int";
    private static final String XML_PRIM_LONG = "long";
    private static final String XML_PRIM_SHORT = "short";
    private static final String XML_PROPERTY = "property";
    private static final String XML_PROPERTY_REF = "property-ref";
    private static final String XML_SHORT = "Short";
    private static final String XML_STRING = "String";
    private static final String XML_TIME = "Time";
    private static final String XML_TIMESTAMP = "Timestamp";
    private static final String XML_TO_MANY = "to-many";
    private static final String XML_TO_ONE = "to-one";
    private static final String XML_TRUE = "true";
    private static final String XML_TYPE = "type";
    private static final String XML_UNIQUE = "unique";
    private static final String XML_VERSION = "version";
    private static final String XML_VERSION_CODE = "versionCode";
    private AEntity currentEntity = null;
    private List<AEntityId> currentIds = null;
    private AIndex currentIndex = null;
    private String databaseName = DEFAULT_DATABASE_NAME;
    private Map<String, AEntity> entities;
    private String fileName;
    private boolean hasId = false;
    private InputStream inputStream;
    private AObjectModel model;
    private List<RelationDescription> relationDescriptions;
    private String version = DEFAULT_VERSION;
    private int versionCode = 1;
    private XmlPullParser xmlPullParser;

    XmlParserHelper(Context context, String str) {
        this.fileName = str;
        this.model = new AObjectModel();
        this.entities = new ArrayMap();
        this.relationDescriptions = new ArrayList();
        this.xmlPullParser = Xml.newPullParser();
        try {
            this.inputStream = context.getAssets().open(str);
            initXmlParser();
        } catch (FileNotFoundException unused) {
            throw new ODMFIllegalArgumentException("The xml file not found.");
        } catch (IOException unused2) {
            throw new ODMFXmlParserException("An IOException occurred when parser xml.");
        }
    }

    XmlParserHelper(InputStream inputStream2, String str) {
        this.fileName = str;
        this.inputStream = inputStream2;
        this.model = new AObjectModel();
        this.entities = new ArrayMap();
        this.relationDescriptions = new ArrayList();
        this.xmlPullParser = Xml.newPullParser();
        initXmlParser();
    }

    private void initXmlParser() {
        try {
            this.xmlPullParser.setInput(this.inputStream, "utf-8");
        } catch (XmlPullParserException unused) {
            throw new ODMFXmlParserException("An XmlPullParserException occurred when set input stream of the XmlPullParser.");
        }
    }

    public ObjectModel getModel() {
        try {
            int eventType = this.xmlPullParser.getEventType();
            while (eventType != 1) {
                if (eventType == 2) {
                    parserStartTag();
                } else if (eventType == 3) {
                    parserEndTag();
                }
                eventType = this.xmlPullParser.next();
            }
            generateModel();
            AObjectModel aObjectModel = this.model;
            try {
                if (this.inputStream != null) {
                    this.inputStream.close();
                }
            } catch (IOException unused) {
                LOG.logE("xml parser stream closed error.");
            }
            return aObjectModel;
        } catch (IOException | XmlPullParserException unused2) {
            try {
                if (this.inputStream != null) {
                    this.inputStream.close();
                }
            } catch (IOException unused3) {
                LOG.logE("xml parser stream closed error.");
            }
            return null;
        } catch (Throwable th) {
            try {
                if (this.inputStream != null) {
                    this.inputStream.close();
                }
            } catch (IOException unused4) {
                LOG.logE("xml parser stream closed error.");
            }
            throw th;
        }
    }

    private void parserStartTag() {
        if (XML_NATURAL_BASE_MAPPING.equals(this.xmlPullParser.getName())) {
            parserNaturalBaseMapping();
        } else if (XML_CLASS.equals(this.xmlPullParser.getName())) {
            parserClass();
        } else if (XML_ID.equals(this.xmlPullParser.getName()) || XML_COMPOSITE_ID.equals(this.xmlPullParser.getName())) {
            parserPrimaryKey();
        } else if (XML_PROPERTY.equals(this.xmlPullParser.getName())) {
            parserProperty();
        } else if (XML_GENERATOR.equals(this.xmlPullParser.getName())) {
            parserPrimaryKeyGenerator();
        } else if (XML_KEY_PROPERTY.equals(this.xmlPullParser.getName())) {
            parserPrimaryKeyProperty();
        } else if (isRelationship(this.xmlPullParser.getName())) {
            parserRelationship();
        } else if (XML_COMPOSITE_INDEX.equals(this.xmlPullParser.getName())) {
            parserCompositeIndex();
        } else if (XML_INDEX_PROPERTY.equals(this.xmlPullParser.getName())) {
            parserIndexProperty();
        } else {
            LOG.logE("The xml form is wrong, the name of start tag is mismatch.");
        }
    }

    private void parserEndTag() {
        if (XML_CLASS.equals(this.xmlPullParser.getName())) {
            AEntity aEntity = this.currentEntity;
            if (aEntity != null) {
                aEntity.setEntityIds(this.currentIds);
                if (this.entities.containsValue(this.currentEntity)) {
                    this.entities.put(this.currentEntity.getEntityName(), this.currentEntity);
                }
                if (this.hasId) {
                    this.currentEntity = null;
                    this.currentIds = null;
                } else {
                    throw new ODMFXmlParserException("The xml form is wrong, this class do not have a primary key.");
                }
            } else {
                throw new ODMFXmlParserException("The xml form is wrong, no start tag of a class match this end tag.");
            }
        }
        if (XML_COMPOSITE_INDEX.equals(this.xmlPullParser.getName())) {
            AEntity aEntity2 = this.currentEntity;
            if (aEntity2 != null) {
                aEntity2.getIndexes().add(this.currentIndex);
                this.currentIndex = null;
                return;
            }
            throw new ODMFXmlParserException("The xml form is wrong, the composite-index tag may not belong to any class.");
        }
    }

    private void generateModel() {
        this.relationDescriptions = sortRelationship();
        int size = this.relationDescriptions.size();
        for (int i = 0; i < size; i++) {
            RelationDescription relationDescription = this.relationDescriptions.get(i);
            if (this.entities.get(relationDescription.baseClass) != null) {
                this.entities.get(relationDescription.baseClass).getRelationships().add(relationshipParser(relationDescription));
            }
        }
        this.model.setModelName(this.fileName);
        this.model.setDatabaseVersion(this.version);
        this.model.setDatabaseVersionCode(this.versionCode);
        this.model.setDatabaseName(this.databaseName);
        this.model.setEntities(this.entities);
    }

    private void parserIndexProperty() {
        String attributeValue = this.xmlPullParser.getAttributeValue(null, XML_NAME);
        AEntity aEntity = this.currentEntity;
        if (aEntity == null || this.currentIndex == null) {
            throw new ODMFXmlParserException("The xml form is wrong, the index-property tag may not belong to any class or composite-index.");
        }
        this.currentIndex.addAttribute(aEntity.getAttribute(attributeValue));
    }

    private void parserCompositeIndex() {
        this.currentIndex = new AIndex(this.xmlPullParser.getAttributeValue(null, XML_NAME));
    }

    private void parserRelationship() {
        if (this.currentEntity != null) {
            this.relationDescriptions.add(relationDescriptionParser());
            return;
        }
        throw new ODMFXmlParserException("The xml form is wrong, the relationship tag may not belong to any class.");
    }

    private void parserPrimaryKeyProperty() {
        String attributeValue = this.xmlPullParser.getAttributeValue(null, XML_NAME);
        AEntity aEntity = this.currentEntity;
        if (aEntity == null || this.currentIds == null) {
            throw new ODMFXmlParserException("The xml form is wrong, the key-property tag may not belong to any class or id.");
        }
        AAttribute attribute = aEntity.getAttribute(attributeValue);
        if (attribute != null) {
            AEntityId aEntityId = new AEntityId(attribute.getFieldName(), attribute.getType(), attribute.hasIndex(), attribute.isUnique(), attribute.isNotNull(), attribute.isLazy(), attribute.getDefaultValue(), AEntityId.NATURAL_ID);
            this.currentIds.add(aEntityId);
            int indexOf = this.currentEntity.getAttributes().indexOf(attribute);
            this.currentEntity.getAttributes().remove(attribute);
            this.currentEntity.getAttributes().add(indexOf, aEntityId);
            return;
        }
        throw new ODMFXmlParserException("The xml form is wrong, the class do not have the property the key-property tag specified.");
    }

    private void parserPrimaryKeyGenerator() {
        List<AEntityId> list;
        String attributeValue = this.xmlPullParser.getAttributeValue(null, XML_CLASS);
        if (this.currentEntity == null || (list = this.currentIds) == null) {
            throw new ODMFXmlParserException("The xml form is wrong, the generator tag may not belong to any class or id.");
        }
        list.get(0).setGeneratorType(attributeValue);
        this.currentEntity.getAttributes().add(this.currentIds.get(0));
        if (attributeValue != null && attributeValue.equals(AEntityId.INCREMENT)) {
            this.currentEntity.setKeyAutoIncrement(true);
        }
    }

    private void parserProperty() {
        AAttribute attributeParser = attributeParser();
        AEntity aEntity = this.currentEntity;
        if (aEntity != null) {
            aEntity.getAttributes().add(attributeParser);
            return;
        }
        throw new ODMFXmlParserException("The xml form is wrong, the property tag may not belong to any class.");
    }

    private void parserPrimaryKey() {
        if (!this.hasId) {
            this.hasId = true;
            this.currentIds = entityIdParser();
            return;
        }
        throw new ODMFXmlParserException("The xml form is wrong, this class has too many primary key.");
    }

    private void parserClass() {
        this.currentEntity = entityParser();
        this.currentEntity.setModel(this.model);
        this.currentIds = new ArrayList();
        this.hasId = false;
        this.entities.put(this.currentEntity.getEntityName(), this.currentEntity);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        this.currentEntity.setAttributes(arrayList);
        this.currentEntity.setRelationships(arrayList2);
        this.currentEntity.setIndexes(arrayList3);
    }

    private void parserNaturalBaseMapping() {
        String attributeValue = this.xmlPullParser.getAttributeValue(null, XML_VERSION);
        if (!TextUtils.isEmpty(attributeValue)) {
            if (JudgeUtils.checkVersion(attributeValue)) {
                this.version = attributeValue;
            } else {
                LOG.logE("The databaseVersion form is wrong.");
                throw new ODMFXmlParserException("The databaseVersion form is wrong.");
            }
        }
        String attributeValue2 = this.xmlPullParser.getAttributeValue(null, XML_DATABASE_NAME);
        String attributeValue3 = this.xmlPullParser.getAttributeValue(null, XML_VERSION_CODE);
        if (!TextUtils.isEmpty(attributeValue3)) {
            try {
                this.versionCode = Integer.parseInt(attributeValue3);
            } catch (NumberFormatException e) {
                LOG.logE("The database version code form is wrong.");
                throw new ODMFXmlParserException("The database version code form is wrong : " + e.getMessage());
            }
        }
        if (!TextUtils.isEmpty(attributeValue2)) {
            this.databaseName = attributeValue2;
        }
    }

    private boolean isRelationship(String str) {
        return XML_TO_ONE.equals(str) || XML_TO_MANY.equals(str);
    }

    private int getType(String str) throws ODMFXmlParserException {
        if (XML_INT.equals(str)) {
            return 0;
        }
        if (XML_LONG.equals(str)) {
            return 1;
        }
        if (XML_SHORT.equals(str)) {
            return 8;
        }
        if (XML_STRING.equals(str)) {
            return 2;
        }
        if (XML_FLOAT.equals(str)) {
            return 4;
        }
        if (XML_DOUBLE.equals(str)) {
            return 5;
        }
        if (XML_BLOB.equals(str)) {
            return 6;
        }
        if (XML_CLOB.equals(str)) {
            return 7;
        }
        if (XML_TIME.equals(str)) {
            return 10;
        }
        if (XML_DATE.equals(str)) {
            return 9;
        }
        if (XML_BOOLEAN.equals(str)) {
            return 3;
        }
        if (XML_BYTE.equals(str)) {
            return 11;
        }
        if (XML_CALENDAR.equals(str)) {
            return 12;
        }
        if (XML_TIMESTAMP.equals(str)) {
            return 13;
        }
        if (XML_CHARACTER.equals(str)) {
            return 14;
        }
        if (XML_PRIM_INT.equals(str)) {
            return 15;
        }
        if (XML_PRIM_LONG.equals(str)) {
            return 16;
        }
        if (XML_PRIM_SHORT.equals(str)) {
            return 17;
        }
        if (XML_PRIM_FLOAT.equals(str)) {
            return 18;
        }
        if (XML_PRIM_DOUBLE.equals(str)) {
            return 19;
        }
        if (XML_PRIM_BOOLEAN.equals(str)) {
            return 20;
        }
        if (XML_PRIM_BYTE.equals(str)) {
            return 21;
        }
        if (XML_PRIM_CHAR.equals(str)) {
            return 22;
        }
        throw new ODMFXmlParserException("illegal type defined.");
    }

    private List<RelationDescription> sortRelationship() {
        ArrayList arrayList = new ArrayList();
        int size = this.relationDescriptions.size();
        for (int i = 0; i < size; i++) {
            RelationDescription relationDescription = this.relationDescriptions.get(i);
            if (relationDescription.inverseRelation == null) {
                arrayList.add(relationDescription);
            }
        }
        for (int i2 = 0; i2 < size; i2++) {
            RelationDescription relationDescription2 = this.relationDescriptions.get(i2);
            if (relationDescription2.inverseRelation != null) {
                arrayList.add(relationDescription2);
            }
        }
        return arrayList;
    }

    private AEntity entityParser() {
        int i;
        String attributeValue = this.xmlPullParser.getAttributeValue(null, XML_NAME);
        String attributeValue2 = this.xmlPullParser.getAttributeValue(null, XML_PACKAGE);
        String attributeValue3 = this.xmlPullParser.getAttributeValue(null, XML_VERSION);
        String attributeValue4 = this.xmlPullParser.getAttributeValue(null, XML_VERSION_CODE);
        if (attributeValue == null || attributeValue2 == null) {
            LOG.logE("Parser entity failed : The className and classPackage must be set.");
            throw new ODMFXmlParserException("Parser relationship failed : The name, class, property-ref must be set.");
        } else if (attributeValue3 == null || JudgeUtils.checkVersion(attributeValue3)) {
            if (!TextUtils.isEmpty(attributeValue4)) {
                try {
                    i = Integer.parseInt(attributeValue4);
                } catch (NumberFormatException e) {
                    LOG.logE("The entity version code form is wrong.");
                    throw new ODMFXmlParserException("The entity version code form is wrong : " + e.getMessage());
                }
            } else {
                i = 1;
            }
            AEntity aEntity = new AEntity();
            aEntity.setEntityName(attributeValue2 + "." + attributeValue);
            aEntity.setTableName(attributeValue);
            if (attributeValue3 == null) {
                attributeValue3 = DEFAULT_VERSION;
            }
            aEntity.setEntityVersion(attributeValue3);
            aEntity.setEntityVersionCode(i);
            return aEntity;
        } else {
            throw new ODMFXmlParserException("The entityVersion form is wrong.");
        }
    }

    private List<AEntityId> entityIdParser() throws ODMFXmlParserException {
        ArrayList arrayList = new ArrayList();
        if (XML_ID.equals(this.xmlPullParser.getName())) {
            AEntityId aEntityId = new AEntityId();
            String attributeValue = this.xmlPullParser.getAttributeValue(null, XML_NAME);
            String attributeValue2 = this.xmlPullParser.getAttributeValue(null, XML_TYPE);
            aEntityId.setFieldName(attributeValue);
            aEntityId.setColumnName(attributeValue);
            aEntityId.setType(getType(attributeValue2));
            aEntityId.setUnique(true);
            aEntityId.setNotNull(true);
            arrayList.add(aEntityId);
        }
        return arrayList;
    }

    private boolean checkDefaultValue(String str, String str2) {
        if (str.equals(XML_CHARACTER) || str.equals(XML_PRIM_CHAR)) {
            return str2.length() == 1;
        }
        if (str.equals(XML_INT) || str.equals(XML_SHORT) || str.equals(XML_LONG) || str.equals(XML_BYTE) || str.equals(XML_PRIM_INT) || str.equals(XML_PRIM_SHORT) || str.equals(XML_PRIM_LONG) || str.equals(XML_PRIM_BYTE)) {
            return str2.matches("^[+-]?[0-9]+$");
        }
        if (str.equals(XML_DOUBLE) || str.equals(XML_FLOAT) || str.equals(XML_PRIM_DOUBLE) || str.equals(XML_PRIM_FLOAT)) {
            return str2.matches("^[+-]?[0-9]+(.[0-9]+)?$");
        }
        if (str.equals(XML_STRING) || str.equals(XML_BLOB) || str.equals(XML_CLOB)) {
            return true;
        }
        if (str.equals(XML_BOOLEAN) || str.equals(XML_PRIM_BOOLEAN)) {
            return str2.equals(XML_TRUE) || str2.equals(XML_FALSE);
        }
        if (str.equals(XML_TIME)) {
            return str2.matches("[0-9]{2}:[0-9]{2}:[0-9]{2}");
        }
        if (str.equals(XML_TIMESTAMP) || str.equals(XML_CALENDAR) || str.equals(XML_DATE)) {
            return str2.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}\\s[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]+");
        }
        return false;
    }

    private AAttribute attributeParser() throws ODMFXmlParserException {
        String attributeValue = this.xmlPullParser.getAttributeValue(null, XML_NAME);
        String attributeValue2 = this.xmlPullParser.getAttributeValue(null, XML_TYPE);
        String attributeValue3 = this.xmlPullParser.getAttributeValue(null, XML_UNIQUE);
        String attributeValue4 = this.xmlPullParser.getAttributeValue(null, XML_NOT_NULL);
        String attributeValue5 = this.xmlPullParser.getAttributeValue(null, XML_LAZY);
        String attributeValue6 = this.xmlPullParser.getAttributeValue(null, XML_DEFAULT);
        String attributeValue7 = this.xmlPullParser.getAttributeValue(null, XML_INDEX);
        if (attributeValue == null || attributeValue2 == null) {
            LOG.logE("Parser attribute failed : The name and type must be set.");
            throw new ODMFXmlParserException("Parser relationship failed : The name, class, property-ref must be set.");
        } else if (attributeValue6 == null || checkDefaultValue(attributeValue2, attributeValue6)) {
            int type = getType(attributeValue2);
            boolean z = false;
            boolean z2 = !TextUtils.isEmpty(attributeValue7) && attributeValue7.equals(XML_TRUE);
            boolean z3 = !TextUtils.isEmpty(attributeValue3) && attributeValue3.equals(XML_TRUE);
            boolean z4 = !TextUtils.isEmpty(attributeValue4) && attributeValue4.equals(XML_TRUE);
            if (!TextUtils.isEmpty(attributeValue5) && attributeValue5.equals(XML_TRUE)) {
                z = true;
            }
            return new AAttribute(attributeValue, type, z2, z3, z4, z, attributeValue6);
        } else {
            throw new ODMFXmlParserException("default_value not match");
        }
    }

    private RelationDescription relationDescriptionParser() throws ODMFXmlParserException {
        String attributeValue = this.xmlPullParser.getAttributeValue(null, XML_NAME);
        String attributeValue2 = this.xmlPullParser.getAttributeValue(null, XML_CLASS);
        String attributeValue3 = this.xmlPullParser.getAttributeValue(null, XML_PROPERTY_REF);
        String attributeValue4 = this.xmlPullParser.getAttributeValue(null, XML_CASCADE);
        String attributeValue5 = this.xmlPullParser.getAttributeValue(null, XML_LAZY);
        String attributeValue6 = this.xmlPullParser.getAttributeValue(null, XML_NOT_FOUND);
        String attributeValue7 = this.xmlPullParser.getAttributeValue(null, XML_MAPPED_BY);
        String name = this.xmlPullParser.getName();
        if (attributeValue != null && attributeValue2 != null && attributeValue3 != null) {
            return new RelationDescription(attributeValue2, this.currentEntity.getEntityName(), attributeValue3, attributeValue, attributeValue4, attributeValue5, attributeValue6, name, attributeValue7);
        }
        LOG.logE("Parser relationship failed : The name, class, property-ref must be set.");
        throw new ODMFXmlParserException("Parser relationship failed : The name, class, property-ref must be set.");
    }

    private int getRelationType(RelationDescription relationDescription) {
        if (relationDescription.type.equals(XML_TO_ONE)) {
            return 2;
        }
        if (relationDescription.type.equals(XML_TO_MANY)) {
            return 0;
        }
        throw new ODMFXmlParserException("Illegal relationship defined:wrong type.");
    }

    private ARelationship relationshipParser(RelationDescription relationDescription) throws ODMFXmlParserException {
        String str = relationDescription.foreignKey;
        String str2 = relationDescription.refProperty;
        int relationType = getRelationType(relationDescription);
        String str3 = relationDescription.cascade;
        if (str3 == null) {
            str3 = ARelationship.NONE_CASCADE;
        }
        boolean z = !TextUtils.isEmpty(relationDescription.lazy) && relationDescription.lazy.equals(XML_TRUE);
        String str4 = relationDescription.notFound;
        if (str4 == null) {
            str4 = ARelationship.IGNORE;
        }
        String str5 = relationDescription.inverseRelation;
        AEntity aEntity = this.entities.get(relationDescription.baseClass);
        AEntity aEntity2 = this.entities.get(relationDescription.refClass);
        if (aEntity2 != null) {
            ARelationship aRelationship = new ARelationship(str, str2, relationType, aEntity, aEntity2, str3, z, str4, null, true);
            if (!(str5 == null || aEntity2.getRelationship(str5) == null)) {
                aRelationship.setInverseRelationship(aEntity2.getRelationship(str5));
                aRelationship.setRelationShipType((aEntity2.getRelationship(str5).getRelationShipType() * 2) + relationType);
                aEntity2.getRelationship(str5).setInverseRelationship(aRelationship);
                aEntity2.getRelationship(str5).setRelationShipType((relationType * 2) + aEntity2.getRelationship(str5).getRelationShipType());
                if (relationType == 2 && aEntity2.getRelationship(str5).getRelationShipType() == 4) {
                    aEntity2.getRelationship(str5).setMajor(false);
                } else {
                    aRelationship.setMajor(false);
                }
            }
            return aRelationship;
        }
        throw new ODMFXmlParserException("Illegal relationship defined:class not found.");
    }

    /* access modifiers changed from: private */
    public static class RelationDescription {
        String baseClass;
        String cascade;
        String foreignKey;
        String inverseRelation;
        String lazy;
        String notFound;
        String refClass;
        String refProperty;
        String type;

        RelationDescription(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9) {
            this.refClass = str;
            this.baseClass = str2;
            this.refProperty = str3;
            this.foreignKey = str4;
            this.cascade = str5;
            this.lazy = str6;
            this.notFound = str7;
            this.type = str8;
            this.inverseRelation = str9;
        }
    }
}
