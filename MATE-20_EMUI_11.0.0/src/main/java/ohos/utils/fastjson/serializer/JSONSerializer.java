package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;

public class JSONSerializer {
    protected List<AfterFilter> afterFilters;
    protected List<BeforeFilter> beforeFilters;
    public final SerializeConfig config;
    protected SerialContext context;
    private DateFormat dateFormat;
    private String dateFormatPattern;
    private int indentCount;
    public Locale locale;
    protected List<NameFilter> nameFilters;
    public final SerializeWriter out;
    protected List<PropertyFilter> propertyFilters;
    protected List<PropertyPreFilter> propertyPreFilters;
    protected IdentityHashMap<Object, SerialContext> references;
    public TimeZone timeZone;
    protected List<ValueFilter> valueFilters;

    public JSONSerializer() {
        this(new SerializeWriter(null, JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.EMPTY), SerializeConfig.globalInstance);
    }

    public JSONSerializer(SerializeWriter serializeWriter) {
        this(serializeWriter, SerializeConfig.globalInstance);
    }

    public JSONSerializer(SerializeConfig serializeConfig) {
        this(new SerializeWriter(null, JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.EMPTY), serializeConfig);
    }

    public JSONSerializer(SerializeWriter serializeWriter, SerializeConfig serializeConfig) {
        this.beforeFilters = null;
        this.afterFilters = null;
        this.propertyFilters = null;
        this.valueFilters = null;
        this.nameFilters = null;
        this.propertyPreFilters = null;
        this.indentCount = 0;
        this.references = null;
        this.timeZone = JSON.defaultTimeZone;
        this.locale = JSON.defaultLocale;
        this.out = serializeWriter;
        this.config = serializeConfig;
        this.timeZone = JSON.defaultTimeZone;
    }

    public String getDateFormatPattern() {
        DateFormat dateFormat2 = this.dateFormat;
        if (dateFormat2 instanceof SimpleDateFormat) {
            return ((SimpleDateFormat) dateFormat2).toPattern();
        }
        return this.dateFormatPattern;
    }

    public DateFormat getDateFormat() {
        String str;
        if (this.dateFormat == null && (str = this.dateFormatPattern) != null) {
            this.dateFormat = new SimpleDateFormat(str, this.locale);
            this.dateFormat.setTimeZone(this.timeZone);
        }
        return this.dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat2) {
        this.dateFormat = dateFormat2;
        if (this.dateFormatPattern != null) {
            this.dateFormatPattern = null;
        }
    }

    public void setDateFormat(String str) {
        this.dateFormatPattern = str;
        if (this.dateFormat != null) {
            this.dateFormat = null;
        }
    }

    public void setContext(SerialContext serialContext, Object obj, Object obj2, int i) {
        if ((this.out.features & SerializerFeature.DisableCircularReferenceDetect.mask) == 0) {
            this.context = new SerialContext(serialContext, obj, obj2, i);
            if (this.references == null) {
                this.references = new IdentityHashMap<>();
            }
            this.references.put(obj, this.context);
        }
    }

    public void writeReference(Object obj) {
        SerialContext serialContext = this.context;
        if (obj == serialContext.object) {
            this.out.write("{\"$ref\":\"@\"}");
            return;
        }
        SerialContext serialContext2 = serialContext.parent;
        if (serialContext2 == null || obj != serialContext2.object) {
            while (serialContext.parent != null) {
                serialContext = serialContext.parent;
            }
            if (obj == serialContext.object) {
                this.out.write("{\"$ref\":\"$\"}");
                return;
            }
            String serialContext3 = this.references.get(obj).toString();
            this.out.write("{\"$ref\":\"");
            this.out.write(serialContext3);
            this.out.write("\"}");
            return;
        }
        this.out.write("{\"$ref\":\"..\"}");
    }

    public List<ValueFilter> getValueFilters() {
        if (this.valueFilters == null) {
            this.valueFilters = new ArrayList();
        }
        return this.valueFilters;
    }

    public void incrementIndent() {
        this.indentCount++;
    }

    public void decrementIdent() {
        this.indentCount--;
    }

    public void println() {
        this.out.write(10);
        for (int i = 0; i < this.indentCount; i++) {
            this.out.write(9);
        }
    }

    public List<BeforeFilter> getBeforeFilters() {
        if (this.beforeFilters == null) {
            this.beforeFilters = new ArrayList();
        }
        return this.beforeFilters;
    }

    public List<AfterFilter> getAfterFilters() {
        if (this.afterFilters == null) {
            this.afterFilters = new ArrayList();
        }
        return this.afterFilters;
    }

    public List<NameFilter> getNameFilters() {
        if (this.nameFilters == null) {
            this.nameFilters = new ArrayList();
        }
        return this.nameFilters;
    }

    public List<PropertyPreFilter> getPropertyPreFilters() {
        if (this.propertyPreFilters == null) {
            this.propertyPreFilters = new ArrayList();
        }
        return this.propertyPreFilters;
    }

    public List<PropertyFilter> getPropertyFilters() {
        if (this.propertyFilters == null) {
            this.propertyFilters = new ArrayList();
        }
        return this.propertyFilters;
    }

    public String toString() {
        return this.out.toString();
    }

    public void config(SerializerFeature serializerFeature, boolean z) {
        this.out.config(serializerFeature, z);
    }

    public static final void write(Writer writer, Object obj) {
        SerializeWriter serializeWriter = new SerializeWriter(null, JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.EMPTY);
        try {
            new JSONSerializer(serializeWriter, SerializeConfig.globalInstance).write(obj);
            serializeWriter.writeTo(writer);
            serializeWriter.close();
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        } catch (Throwable th) {
            serializeWriter.close();
            throw th;
        }
    }

    public static final void write(SerializeWriter serializeWriter, Object obj) {
        new JSONSerializer(serializeWriter, SerializeConfig.globalInstance).write(obj);
    }

    public final void write(Object obj) {
        if (obj == null) {
            this.out.writeNull();
            return;
        }
        try {
            this.config.get(obj.getClass()).write(this, obj, null, null);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    public final void writeWithFieldName(Object obj, Object obj2) {
        writeWithFieldName(obj, obj2, null, 0);
    }

    /* access modifiers changed from: protected */
    public final void writeKeyValue(char c, String str, Object obj) {
        if (c != 0) {
            this.out.write(c);
        }
        this.out.writeFieldName(str, true);
        write(obj);
    }

    public final void writeWithFieldName(Object obj, Object obj2, Type type, int i) {
        if (obj == null) {
            try {
                this.out.writeNull();
            } catch (IOException e) {
                throw new JSONException(e.getMessage(), e);
            }
        } else {
            this.config.get(obj.getClass()).write(this, obj, obj2, type);
        }
    }

    public final void writeWithFormat(Object obj, String str) {
        if (obj instanceof Date) {
            DateFormat dateFormat2 = getDateFormat();
            if (dateFormat2 == null) {
                dateFormat2 = new SimpleDateFormat(str, this.locale);
                dateFormat2.setTimeZone(this.timeZone);
            }
            this.out.writeString(dateFormat2.format((Date) obj));
            return;
        }
        write(obj);
    }

    public final void write(String str) {
        if (str == null) {
            if ((this.out.features & SerializerFeature.WriteNullStringAsEmpty.mask) != 0) {
                this.out.writeString("");
            } else {
                this.out.writeNull();
            }
        } else if ((this.out.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
            this.out.writeStringWithSingleQuote(str);
        } else {
            this.out.writeStringWithDoubleQuote(str, 0, true);
        }
    }

    public void close() {
        this.out.close();
    }

    public static Object processValue(JSONSerializer jSONSerializer, Object obj, Object obj2, Object obj3) {
        List<ValueFilter> list = jSONSerializer.valueFilters;
        if (list != null) {
            if (obj2 != null && !(obj2 instanceof String)) {
                obj2 = JSON.toJSONString(obj2);
            }
            for (ValueFilter valueFilter : list) {
                obj3 = valueFilter.process(obj, (String) obj2, obj3);
            }
        }
        return obj3;
    }

    public Object processKey(Object obj, Object obj2, Object obj3) {
        List<NameFilter> list = this.nameFilters;
        if (list != null) {
            if (obj2 != null && !(obj2 instanceof String)) {
                obj2 = JSON.toJSONString(obj2);
            }
            for (NameFilter nameFilter : list) {
                obj2 = nameFilter.process(obj, (String) obj2, obj3);
            }
        }
        return obj2;
    }

    public boolean applyName(Object obj, Object obj2) {
        List<PropertyPreFilter> list = this.propertyPreFilters;
        if (list == null) {
            return true;
        }
        for (PropertyPreFilter propertyPreFilter : list) {
            if (obj2 != null && !(obj2 instanceof String)) {
                obj2 = JSON.toJSONString(obj2);
            }
            if (!propertyPreFilter.apply(this, obj, (String) obj2)) {
                return false;
            }
        }
        return true;
    }

    public boolean apply(Object obj, Object obj2, Object obj3) {
        List<PropertyFilter> list = this.propertyFilters;
        if (list == null) {
            return true;
        }
        if (obj2 != null && !(obj2 instanceof String)) {
            obj2 = JSON.toJSONString(obj2);
        }
        for (PropertyFilter propertyFilter : list) {
            if (!propertyFilter.apply(obj, (String) obj2, obj3)) {
                return false;
            }
        }
        return true;
    }

    public SerializeWriter getWriter() {
        return this.out;
    }

    public SerialContext getContext() {
        return this.context;
    }
}
