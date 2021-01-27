package ohos.utils.fastjson.parser;

import java.io.Closeable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.fastjson.parser.deserializer.ExtraProcessor;
import ohos.utils.fastjson.parser.deserializer.ExtraTypeProvider;
import ohos.utils.fastjson.parser.deserializer.FieldDeserializer;
import ohos.utils.fastjson.parser.deserializer.FieldTypeResolver;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.serializer.IntegerCodec;
import ohos.utils.fastjson.serializer.StringCodec;
import ohos.utils.fastjson.util.TypeUtils;

public class DefaultJSONParser implements Closeable {
    public static final int NONE = 0;
    public static final int NeedToResolve = 1;
    public static final int TypeNameRedirect = 2;
    public ParserConfig config;
    protected ParseContext contex;
    private ParseContext[] contextArray;
    private int contextArrayIndex;
    private DateFormat dateFormat;
    private String dateFormatPattern;
    protected List<ExtraProcessor> extraProcessors;
    protected List<ExtraTypeProvider> extraTypeProviders;
    public FieldTypeResolver fieldTypeResolver;
    public final JSONLexer lexer;
    public int resolveStatus;
    private List<ResolveTask> resolveTaskList;
    public final SymbolTable symbolTable;

    public String getDateFomartPattern() {
        return this.dateFormatPattern;
    }

    public DateFormat getDateFormat() {
        if (this.dateFormat == null) {
            this.dateFormat = new SimpleDateFormat(this.dateFormatPattern, this.lexer.locale);
            this.dateFormat.setTimeZone(this.lexer.timeZone);
        }
        return this.dateFormat;
    }

    public void setDateFormat(String str) {
        this.dateFormatPattern = str;
        this.dateFormat = null;
    }

    public void setDateFomrat(DateFormat dateFormat2) {
        this.dateFormat = dateFormat2;
    }

    public DefaultJSONParser(String str) {
        this(str, ParserConfig.global, JSON.DEFAULT_PARSER_FEATURE);
    }

    public DefaultJSONParser(String str, ParserConfig parserConfig) {
        this(new JSONLexer(str, JSON.DEFAULT_PARSER_FEATURE), parserConfig);
    }

    public DefaultJSONParser(String str, ParserConfig parserConfig, int i) {
        this(new JSONLexer(str, i), parserConfig);
    }

    public DefaultJSONParser(char[] cArr, int i, ParserConfig parserConfig, int i2) {
        this(new JSONLexer(cArr, i, i2), parserConfig);
    }

    public DefaultJSONParser(JSONLexer jSONLexer) {
        this(jSONLexer, ParserConfig.global);
    }

    public DefaultJSONParser(JSONLexer jSONLexer, ParserConfig parserConfig) {
        this.dateFormatPattern = JSON.DEFFAULT_DATE_FORMAT;
        this.contextArrayIndex = 0;
        this.resolveStatus = 0;
        this.extraTypeProviders = null;
        this.extraProcessors = null;
        this.fieldTypeResolver = null;
        this.lexer = jSONLexer;
        this.config = parserConfig;
        this.symbolTable = parserConfig.symbolTable;
        char c = jSONLexer.ch;
        char c2 = JSONLexer.EOI;
        if (c == '{') {
            int i = jSONLexer.bp + 1;
            jSONLexer.bp = i;
            jSONLexer.ch = i < jSONLexer.len ? jSONLexer.text.charAt(i) : c2;
            jSONLexer.token = 12;
        } else if (jSONLexer.ch == '[') {
            int i2 = jSONLexer.bp + 1;
            jSONLexer.bp = i2;
            jSONLexer.ch = i2 < jSONLexer.len ? jSONLexer.text.charAt(i2) : c2;
            jSONLexer.token = 14;
        } else {
            jSONLexer.nextToken();
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:68:0x0139 */
    /* JADX DEBUG: Multi-variable search result rejected for r15v7, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x01f9  */
    /* JADX WARNING: Removed duplicated region for block: B:211:0x0380  */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x038a  */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x03d5  */
    /* JADX WARNING: Removed duplicated region for block: B:236:0x03fa  */
    /* JADX WARNING: Removed duplicated region for block: B:299:0x04f4  */
    /* JADX WARNING: Removed duplicated region for block: B:305:0x0503  */
    /* JADX WARNING: Removed duplicated region for block: B:307:0x050c  */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0510  */
    /* JADX WARNING: Removed duplicated region for block: B:310:0x0515  */
    /* JADX WARNING: Removed duplicated region for block: B:319:0x052c  */
    /* JADX WARNING: Removed duplicated region for block: B:341:0x059e  */
    /* JADX WARNING: Removed duplicated region for block: B:415:0x051e A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:419:0x05b9 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01c5  */
    public final Object parseObject(Map map, Object obj) {
        Map<String, Object> map2;
        boolean z;
        char c;
        char c2;
        String str;
        boolean z2;
        char c3;
        char c4;
        boolean z3;
        char c5;
        char c6;
        char c7;
        char c8;
        char c9;
        char c10;
        char c11;
        char c12;
        Map map3;
        Object obj2;
        boolean z4;
        int i;
        String str2;
        char c13;
        Object obj3;
        boolean z5;
        Object obj4;
        Class<?> cls;
        Object obj5;
        FieldDeserializer fieldDeserializer;
        String str3;
        String str4;
        JSONLexer jSONLexer = this.lexer;
        int i2 = jSONLexer.token;
        if (i2 == 8) {
            jSONLexer.nextToken();
            return null;
        } else if (i2 == 12 || i2 == 16) {
            if (map instanceof JSONObject) {
                map2 = ((JSONObject) map).getInnerMap();
                z = true;
            } else {
                map2 = map;
                z = false;
            }
            boolean z6 = (jSONLexer.features & Feature.AllowISO8601DateFormat.mask) != 0;
            boolean z7 = jSONLexer.disableCircularReferenceDetect;
            ParseContext parseContext = this.contex;
            boolean z8 = false;
            while (true) {
                try {
                    c = jSONLexer.ch;
                    if (!(c == '\"' || c == '}')) {
                        jSONLexer.skipWhitespace();
                        c = jSONLexer.ch;
                    }
                    while (c == ',') {
                        jSONLexer.next();
                        jSONLexer.skipWhitespace();
                        c = jSONLexer.ch;
                    }
                    c2 = JSONLexer.EOI;
                    z2 = false;
                    str = str4;
                    if (z2) {
                    }
                    jSONLexer.sp = 0;
                    if (str != JSON.DEFAULT_TYPE_KEY) {
                    }
                    if (str == "$ref") {
                    }
                    if (!z7) {
                    }
                    c4 = '\"';
                    if (c3 != c4) {
                    }
                    z3 = z;
                    c6 = jSONLexer.ch;
                    jSONLexer.skipWhitespace();
                    c6 = jSONLexer.ch;
                    if (c6 == ',') {
                    }
                } finally {
                    if (!z7) {
                        this.contex = parseContext;
                    }
                }
                if (c == '\"') {
                    String scanSymbol = jSONLexer.scanSymbol(this.symbolTable, '\"');
                    str4 = scanSymbol;
                    if (jSONLexer.ch != ':') {
                        jSONLexer.skipWhitespace();
                        str4 = scanSymbol;
                        if (jSONLexer.ch != ':') {
                            throw new JSONException("expect ':' at " + jSONLexer.pos + ", name " + ((Object) scanSymbol));
                        }
                    }
                } else if (c == '}') {
                    int i3 = jSONLexer.bp + 1;
                    jSONLexer.bp = i3;
                    if (i3 < jSONLexer.len) {
                        c2 = jSONLexer.text.charAt(i3);
                    }
                    jSONLexer.ch = c2;
                    jSONLexer.sp = 0;
                    jSONLexer.nextToken(16);
                    if (!z7) {
                        this.contex = parseContext;
                    }
                    return map;
                } else if (c == '\'') {
                    String scanSymbol2 = jSONLexer.scanSymbol(this.symbolTable, '\'');
                    if (jSONLexer.ch != ':') {
                        jSONLexer.skipWhitespace();
                    }
                    str4 = scanSymbol2;
                    if (jSONLexer.ch != ':') {
                        throw new JSONException("expect ':' at " + jSONLexer.pos);
                    }
                } else if (c == 26) {
                    throw new JSONException("syntax error, " + jSONLexer.info());
                } else if (c == ',') {
                    throw new JSONException("syntax error, " + jSONLexer.info());
                } else if ((c >= '0' && c <= '9') || c == '-') {
                    jSONLexer.sp = 0;
                    jSONLexer.scanNumber();
                    try {
                        if (jSONLexer.token == 2) {
                            str3 = jSONLexer.integerValue();
                        } else {
                            str3 = jSONLexer.decimalValue(true);
                        }
                        if (z) {
                            str3 = str3.toString();
                        }
                        str4 = str3;
                        if (jSONLexer.ch != ':') {
                            throw new JSONException("parse number key error, " + jSONLexer.info());
                        }
                    } catch (NumberFormatException unused) {
                        throw new JSONException("parse number key error, " + jSONLexer.info());
                    }
                } else if (c == '{' || c == '[') {
                    jSONLexer.nextToken();
                    z2 = true;
                    str = parse();
                    if (z2) {
                        int i4 = jSONLexer.bp + 1;
                        jSONLexer.bp = i4;
                        c3 = i4 >= jSONLexer.len ? 26 : jSONLexer.text.charAt(i4);
                        jSONLexer.ch = c3;
                        while (c3 <= ' ' && (c3 == ' ' || c3 == '\n' || c3 == '\r' || c3 == '\t' || c3 == '\f' || c3 == '\b')) {
                            jSONLexer.next();
                            c3 = jSONLexer.ch;
                        }
                    } else {
                        c3 = jSONLexer.ch;
                    }
                    jSONLexer.sp = 0;
                    if (str != JSON.DEFAULT_TYPE_KEY && !jSONLexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                        String scanSymbol3 = jSONLexer.scanSymbol(this.symbolTable, '\"');
                        int i5 = 0;
                        while (true) {
                            if (i5 >= scanSymbol3.length()) {
                                z5 = true;
                                break;
                            }
                            char charAt = scanSymbol3.charAt(i5);
                            if (charAt < '0' || charAt > '9') {
                                break;
                            }
                            i5++;
                        }
                        z5 = false;
                        if (!z5) {
                            obj4 = null;
                            cls = this.config.checkAutoType(scanSymbol3, null, jSONLexer.features);
                        } else {
                            obj4 = null;
                            cls = null;
                        }
                        if (cls == null) {
                            map.put(JSON.DEFAULT_TYPE_KEY, scanSymbol3);
                        } else {
                            jSONLexer.nextToken(16);
                            if (jSONLexer.token == 13) {
                                jSONLexer.nextToken(16);
                                try {
                                    ObjectDeserializer deserializer = this.config.getDeserializer(cls);
                                    if (deserializer instanceof JavaBeanDeserializer) {
                                        JavaBeanDeserializer javaBeanDeserializer = (JavaBeanDeserializer) deserializer;
                                        obj5 = javaBeanDeserializer.createInstance(this, cls);
                                        for (Map.Entry entry : map.entrySet()) {
                                            Object key = entry.getKey();
                                            if ((key instanceof String) && (fieldDeserializer = javaBeanDeserializer.getFieldDeserializer((String) key)) != null) {
                                                fieldDeserializer.setValue(obj5, entry.getValue());
                                            }
                                        }
                                    } else {
                                        obj5 = obj4;
                                    }
                                    if (obj5 == null) {
                                        if (cls == Cloneable.class) {
                                            obj5 = new HashMap();
                                        } else if ("java.util.Collections$EmptyMap".equals(scanSymbol3)) {
                                            obj5 = Collections.emptyMap();
                                        } else {
                                            obj5 = cls.newInstance();
                                        }
                                    }
                                    return obj5;
                                } catch (Exception e) {
                                    throw new JSONException("create instance error", e);
                                }
                            } else {
                                this.resolveStatus = 2;
                                if (this.contex != null && !(obj instanceof Integer)) {
                                    popContext();
                                }
                                if (map.size() > 0) {
                                    Object cast = TypeUtils.cast((Object) map, (Class<Object>) cls, this.config);
                                    parseObject(cast);
                                    if (!z7) {
                                        this.contex = parseContext;
                                    }
                                    return cast;
                                }
                                ObjectDeserializer deserializer2 = this.config.getDeserializer(cls);
                                Object deserialze = deserializer2.deserialze(this, cls, obj);
                                if (deserializer2 instanceof MapDeserializer) {
                                    this.resolveStatus = 0;
                                }
                                if (!z7) {
                                    this.contex = parseContext;
                                }
                                return deserialze;
                            }
                        }
                    } else if (str == "$ref" || parseContext == null || jSONLexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                        if (!z7 || z8) {
                            c4 = '\"';
                        } else {
                            ParseContext context = setContext(this.contex, map, obj);
                            if (parseContext == null) {
                                parseContext = context;
                            }
                            c4 = '\"';
                            z8 = true;
                        }
                        if (c3 != c4) {
                            String scanStringValue = jSONLexer.scanStringValue(c4);
                            String str5 = scanStringValue;
                            if (z6) {
                                JSONLexer jSONLexer2 = new JSONLexer(scanStringValue);
                                Date date = scanStringValue;
                                if (jSONLexer2.scanISO8601DateIfMatch(true)) {
                                    date = jSONLexer2.calendar.getTime();
                                }
                                jSONLexer2.close();
                                str5 = date;
                            }
                            if (map2 != null) {
                                map2.put(str, str5 == 1 ? 1 : 0);
                            } else {
                                map.put(str, str5 == 1 ? 1 : 0);
                            }
                        } else if ((c3 < '0' || c3 > '9') && c3 != '-') {
                            if (c3 == '[') {
                                jSONLexer.token = 14;
                                int i6 = jSONLexer.bp + 1;
                                jSONLexer.bp = i6;
                                if (i6 >= jSONLexer.len) {
                                    c13 = JSONLexer.EOI;
                                } else {
                                    c13 = jSONLexer.text.charAt(i6);
                                }
                                jSONLexer.ch = c13;
                                ArrayList arrayList = new ArrayList();
                                if (!(obj != null && obj.getClass() == Integer.class)) {
                                    setContext(parseContext);
                                }
                                parseArray(arrayList, str);
                                JSONArray jSONArray = new JSONArray(arrayList);
                                if (map2 != null) {
                                    map2.put(str, jSONArray);
                                } else {
                                    map.put(str, jSONArray);
                                }
                                int i7 = jSONLexer.token;
                                if (i7 == 13) {
                                    jSONLexer.nextToken(16);
                                    if (!z7) {
                                        this.contex = parseContext;
                                    }
                                    return map;
                                }
                                c5 = 16;
                                if (i7 == 16) {
                                    z3 = z;
                                } else {
                                    throw new JSONException("syntax error, " + jSONLexer.info());
                                }
                            } else if (c3 == '{') {
                                int i8 = jSONLexer.bp + 1;
                                jSONLexer.bp = i8;
                                if (i8 >= jSONLexer.len) {
                                    c12 = JSONLexer.EOI;
                                } else {
                                    c12 = jSONLexer.text.charAt(i8);
                                }
                                jSONLexer.ch = c12;
                                jSONLexer.token = 12;
                                boolean z9 = obj instanceof Integer;
                                if ((Feature.OrderedField.mask & jSONLexer.features) != 0) {
                                    map3 = new JSONObject(new LinkedHashMap());
                                } else {
                                    map3 = new JSONObject();
                                }
                                ParseContext context2 = (z7 || z9) ? null : setContext(parseContext, map3, str);
                                if (this.fieldTypeResolver != null) {
                                    if (str != null) {
                                        str2 = str.toString();
                                        z3 = z;
                                    } else {
                                        z3 = z;
                                        str2 = null;
                                    }
                                    Type resolve = this.fieldTypeResolver.resolve(map, str2);
                                    if (resolve != null) {
                                        obj2 = this.config.getDeserializer(resolve).deserialze(this, resolve, str);
                                        z4 = true;
                                        if (!z4) {
                                            obj2 = parseObject(map3, str);
                                        }
                                        if (!(context2 == null || map3 == obj2)) {
                                            context2.object = map;
                                        }
                                        if (this.resolveStatus == 1) {
                                            checkMapResolve(map, str.toString());
                                        }
                                        if (map2 == null) {
                                            map2.put(str, obj2);
                                        } else {
                                            map.put(str, obj2);
                                        }
                                        if (z9) {
                                            setContext(parseContext, obj2, str);
                                        }
                                        i = jSONLexer.token;
                                        if (i != 13) {
                                            jSONLexer.nextToken(16);
                                            if (!z7) {
                                                this.contex = parseContext;
                                            }
                                            if (!z7) {
                                                this.contex = parseContext;
                                            }
                                            return map;
                                        } else if (i == 16) {
                                            c5 = 16;
                                        } else {
                                            throw new JSONException("syntax error, " + jSONLexer.info());
                                        }
                                    }
                                } else {
                                    z3 = z;
                                }
                                z4 = false;
                                obj2 = null;
                                if (!z4) {
                                }
                                context2.object = map;
                                if (this.resolveStatus == 1) {
                                }
                                if (map2 == null) {
                                }
                                if (z9) {
                                }
                                i = jSONLexer.token;
                                if (i != 13) {
                                }
                            } else {
                                z3 = z;
                                if (c3 == 't') {
                                    if (jSONLexer.text.startsWith("true", jSONLexer.bp)) {
                                        jSONLexer.bp += 3;
                                        jSONLexer.next();
                                        map.put(str, Boolean.TRUE);
                                    }
                                } else if (c3 != 'f') {
                                    jSONLexer.nextToken();
                                    Object parse = parse();
                                    String str6 = str;
                                    if (map.getClass() == JSONObject.class) {
                                        str6 = str.toString();
                                    }
                                    map.put(str6, parse);
                                    if (jSONLexer.token == 13) {
                                        jSONLexer.nextToken(16);
                                        if (!z7) {
                                            this.contex = parseContext;
                                        }
                                        return map;
                                    }
                                    c5 = 16;
                                    if (jSONLexer.token != 16) {
                                        throw new JSONException("syntax error, " + jSONLexer.info());
                                    }
                                    z = z3;
                                } else if (jSONLexer.text.startsWith("false", jSONLexer.bp)) {
                                    jSONLexer.bp += 4;
                                    jSONLexer.next();
                                    map.put(str, Boolean.FALSE);
                                }
                                c6 = jSONLexer.ch;
                                if (!(c6 == ',' || c6 == '}')) {
                                    jSONLexer.skipWhitespace();
                                    c6 = jSONLexer.ch;
                                }
                                if (c6 == ',') {
                                    int i9 = jSONLexer.bp + 1;
                                    jSONLexer.bp = i9;
                                    if (i9 >= jSONLexer.len) {
                                        c11 = JSONLexer.EOI;
                                    } else {
                                        c11 = jSONLexer.text.charAt(i9);
                                    }
                                    jSONLexer.ch = c11;
                                    c5 = 16;
                                    z = z3;
                                } else if (c6 == '}') {
                                    int i10 = jSONLexer.bp + 1;
                                    jSONLexer.bp = i10;
                                    if (i10 >= jSONLexer.len) {
                                        c7 = JSONLexer.EOI;
                                    } else {
                                        c7 = jSONLexer.text.charAt(i10);
                                    }
                                    jSONLexer.ch = c7;
                                    jSONLexer.sp = 0;
                                    if (c7 == ',') {
                                        int i11 = jSONLexer.bp + 1;
                                        jSONLexer.bp = i11;
                                        if (i11 >= jSONLexer.len) {
                                            c10 = JSONLexer.EOI;
                                        } else {
                                            c10 = jSONLexer.text.charAt(i11);
                                        }
                                        jSONLexer.ch = c10;
                                        jSONLexer.token = 16;
                                    } else if (c7 == '}') {
                                        int i12 = jSONLexer.bp + 1;
                                        jSONLexer.bp = i12;
                                        if (i12 >= jSONLexer.len) {
                                            c9 = JSONLexer.EOI;
                                        } else {
                                            c9 = jSONLexer.text.charAt(i12);
                                        }
                                        jSONLexer.ch = c9;
                                        jSONLexer.token = 13;
                                    } else if (c7 == ']') {
                                        int i13 = jSONLexer.bp + 1;
                                        jSONLexer.bp = i13;
                                        if (i13 >= jSONLexer.len) {
                                            c8 = JSONLexer.EOI;
                                        } else {
                                            c8 = jSONLexer.text.charAt(i13);
                                        }
                                        jSONLexer.ch = c8;
                                        jSONLexer.token = 15;
                                    } else {
                                        jSONLexer.nextToken();
                                    }
                                    if (!z7) {
                                        setContext(this.contex, map, obj);
                                    }
                                    if (!z7) {
                                        this.contex = parseContext;
                                    }
                                    return map;
                                } else {
                                    throw new JSONException("syntax error, " + jSONLexer.info());
                                }
                            }
                            z = z3;
                        } else {
                            map2.put(str, jSONLexer.scanNumberValue());
                        }
                        z3 = z;
                        c6 = jSONLexer.ch;
                        jSONLexer.skipWhitespace();
                        c6 = jSONLexer.ch;
                        if (c6 == ',') {
                        }
                    } else {
                        jSONLexer.nextToken(4);
                        if (jSONLexer.token == 4) {
                            String stringVal = jSONLexer.stringVal();
                            jSONLexer.nextToken(13);
                            if ("@".equals(stringVal)) {
                                ParseContext parseContext2 = this.contex;
                                obj3 = parseContext2.object;
                                if (!(obj3 instanceof Object[]) && !(obj3 instanceof Collection)) {
                                    if (parseContext2.parent != null) {
                                        obj3 = parseContext2.parent.object;
                                    }
                                }
                                if (jSONLexer.token != 13) {
                                    jSONLexer.nextToken(16);
                                    if (!z7) {
                                        this.contex = parseContext;
                                    }
                                    return obj3;
                                }
                                throw new JSONException("syntax error, " + jSONLexer.info());
                            } else if ("..".equals(stringVal)) {
                                if (parseContext.object != null) {
                                    obj3 = parseContext.object;
                                    if (jSONLexer.token != 13) {
                                    }
                                } else {
                                    addResolveTask(new ResolveTask(parseContext, stringVal));
                                    this.resolveStatus = 1;
                                }
                            } else if ("$".equals(stringVal)) {
                                ParseContext parseContext3 = parseContext;
                                while (parseContext3.parent != null) {
                                    parseContext3 = parseContext3.parent;
                                }
                                if (parseContext3.object != null) {
                                    obj3 = parseContext3.object;
                                    if (jSONLexer.token != 13) {
                                    }
                                } else {
                                    addResolveTask(new ResolveTask(parseContext3, stringVal));
                                    this.resolveStatus = 1;
                                }
                            } else {
                                addResolveTask(new ResolveTask(parseContext, stringVal));
                                this.resolveStatus = 1;
                            }
                            obj3 = null;
                            if (jSONLexer.token != 13) {
                            }
                        } else {
                            throw new JSONException("illegal ref, " + JSONToken.name(jSONLexer.token));
                        }
                    }
                } else {
                    String scanSymbolUnQuoted = jSONLexer.scanSymbolUnQuoted(this.symbolTable);
                    jSONLexer.skipWhitespace();
                    char c14 = jSONLexer.ch;
                    str4 = scanSymbolUnQuoted;
                    if (c14 != ':') {
                        throw new JSONException("expect ':' at " + jSONLexer.bp + ", actual " + c14);
                    } else if (z) {
                        str4 = scanSymbolUnQuoted.toString();
                    }
                }
            }
        } else {
            throw new JSONException("syntax error, expect {, actual " + JSONToken.name(i2) + ", " + jSONLexer.info());
        }
    }

    public <T> T parseObject(Class<T> cls) {
        return (T) parseObject(cls, (Object) null);
    }

    public <T> T parseObject(Type type) {
        return (T) parseObject(type, (Object) null);
    }

    public <T> T parseObject(Type type, Object obj) {
        if (this.lexer.token == 8) {
            this.lexer.nextToken();
            return null;
        }
        if (this.lexer.token == 4) {
            if (type == byte[].class) {
                T t = (T) this.lexer.bytesValue();
                this.lexer.nextToken();
                return t;
            } else if (type == char[].class) {
                String stringVal = this.lexer.stringVal();
                this.lexer.nextToken();
                return (T) stringVal.toCharArray();
            }
        }
        try {
            return (T) this.config.getDeserializer(type).deserialze(this, type, obj);
        } catch (JSONException e) {
            throw e;
        } catch (Exception e2) {
            throw new JSONException(e2.getMessage(), e2);
        }
    }

    public <T> List<T> parseArray(Class<T> cls) {
        ArrayList arrayList = new ArrayList();
        parseArray((Class<?>) cls, (Collection) arrayList);
        return arrayList;
    }

    public void parseArray(Class<?> cls, Collection collection) {
        parseArray((Type) cls, collection);
    }

    public void parseArray(Type type, Collection collection) {
        parseArray(type, collection, null);
    }

    /* JADX INFO: finally extract failed */
    public void parseArray(Type type, Collection collection, Object obj) {
        ObjectDeserializer objectDeserializer;
        String str;
        if (this.lexer.token == 21 || this.lexer.token == 22) {
            this.lexer.nextToken();
        }
        if (this.lexer.token == 14) {
            if (Integer.TYPE == type) {
                objectDeserializer = IntegerCodec.instance;
                this.lexer.nextToken(2);
            } else if (String.class == type) {
                objectDeserializer = StringCodec.instance;
                this.lexer.nextToken(4);
            } else {
                objectDeserializer = this.config.getDeserializer(type);
                this.lexer.nextToken(12);
            }
            ParseContext parseContext = this.contex;
            if (!this.lexer.disableCircularReferenceDetect) {
                setContext(this.contex, collection, obj);
            }
            int i = 0;
            while (true) {
                try {
                    if (this.lexer.token == 16) {
                        this.lexer.nextToken();
                    } else if (this.lexer.token == 15) {
                        this.contex = parseContext;
                        this.lexer.nextToken(16);
                        return;
                    } else {
                        Object obj2 = null;
                        String str2 = null;
                        if (Integer.TYPE == type) {
                            collection.add(IntegerCodec.instance.deserialze(this, null, null));
                        } else if (String.class == type) {
                            if (this.lexer.token == 4) {
                                str = this.lexer.stringVal();
                                this.lexer.nextToken(16);
                            } else {
                                Object parse = parse();
                                if (parse != null) {
                                    str2 = parse.toString();
                                }
                                str = str2;
                            }
                            collection.add(str);
                        } else {
                            if (this.lexer.token == 8) {
                                this.lexer.nextToken();
                            } else {
                                obj2 = objectDeserializer.deserialze(this, type, Integer.valueOf(i));
                            }
                            collection.add(obj2);
                            if (this.resolveStatus == 1) {
                                checkListResolve(collection);
                            }
                        }
                        if (this.lexer.token == 16) {
                            this.lexer.nextToken();
                        }
                        i++;
                    }
                } catch (Throwable th) {
                    this.contex = parseContext;
                    throw th;
                }
            }
        } else {
            throw new JSONException("exepct '[', but " + JSONToken.name(this.lexer.token) + ", " + this.lexer.info());
        }
    }

    public Object[] parseArray(Type[] typeArr) {
        Object obj;
        boolean z;
        Class<?> cls;
        Object obj2;
        int i = 8;
        if (this.lexer.token == 8) {
            this.lexer.nextToken(16);
            return null;
        } else if (this.lexer.token == 14) {
            Object[] objArr = new Object[typeArr.length];
            if (typeArr.length == 0) {
                this.lexer.nextToken(15);
                if (this.lexer.token == 15) {
                    this.lexer.nextToken(16);
                    return new Object[0];
                }
                throw new JSONException("syntax error, " + this.lexer.info());
            }
            this.lexer.nextToken(2);
            int i2 = 0;
            while (i2 < typeArr.length) {
                if (this.lexer.token == i) {
                    this.lexer.nextToken(16);
                    obj = null;
                } else {
                    Type type = typeArr[i2];
                    if (type == Integer.TYPE || type == Integer.class) {
                        if (this.lexer.token == 2) {
                            obj = Integer.valueOf(this.lexer.intValue());
                            this.lexer.nextToken(16);
                        } else {
                            obj = TypeUtils.cast(parse(), type, this.config);
                        }
                    } else if (type == String.class) {
                        if (this.lexer.token == 4) {
                            obj2 = this.lexer.stringVal();
                            this.lexer.nextToken(16);
                        } else {
                            obj2 = TypeUtils.cast(parse(), type, this.config);
                        }
                        obj = obj2;
                    } else {
                        if (i2 != typeArr.length - 1 || !(type instanceof Class)) {
                            cls = null;
                            z = false;
                        } else {
                            Class cls2 = (Class) type;
                            z = cls2.isArray();
                            cls = cls2.getComponentType();
                        }
                        if (!z || this.lexer.token == 14) {
                            obj = this.config.getDeserializer(type).deserialze(this, type, null);
                        } else {
                            ArrayList arrayList = new ArrayList();
                            ObjectDeserializer deserializer = this.config.getDeserializer(cls);
                            if (this.lexer.token != 15) {
                                while (true) {
                                    arrayList.add(deserializer.deserialze(this, type, null));
                                    if (this.lexer.token != 16) {
                                        break;
                                    }
                                    this.lexer.nextToken(12);
                                }
                                if (this.lexer.token != 15) {
                                    throw new JSONException("syntax error, " + this.lexer.info());
                                }
                            }
                            obj = TypeUtils.cast(arrayList, type, this.config);
                        }
                    }
                }
                objArr[i2] = obj;
                if (this.lexer.token == 15) {
                    break;
                } else if (this.lexer.token == 16) {
                    if (i2 == typeArr.length - 1) {
                        this.lexer.nextToken(15);
                    } else {
                        this.lexer.nextToken(2);
                    }
                    i2++;
                    i = 8;
                } else {
                    throw new JSONException("syntax error, " + this.lexer.info());
                }
            }
            if (this.lexer.token == 15) {
                this.lexer.nextToken(16);
                return objArr;
            }
            throw new JSONException("syntax error, " + this.lexer.info());
        } else {
            throw new JSONException("syntax error, " + this.lexer.info());
        }
    }

    public void parseObject(Object obj) {
        Object obj2;
        Class<?> cls = obj.getClass();
        ObjectDeserializer deserializer = this.config.getDeserializer(cls);
        JavaBeanDeserializer javaBeanDeserializer = deserializer instanceof JavaBeanDeserializer ? (JavaBeanDeserializer) deserializer : null;
        int i = this.lexer.token;
        if (i == 12 || i == 16) {
            while (true) {
                String scanSymbol = this.lexer.scanSymbol(this.symbolTable);
                if (scanSymbol == null) {
                    if (this.lexer.token == 13) {
                        this.lexer.nextToken(16);
                        return;
                    } else if (this.lexer.token == 16) {
                        continue;
                    }
                }
                FieldDeserializer fieldDeserializer = javaBeanDeserializer != null ? javaBeanDeserializer.getFieldDeserializer(scanSymbol) : null;
                if (fieldDeserializer != null) {
                    Class<?> cls2 = fieldDeserializer.fieldInfo.fieldClass;
                    Type type = fieldDeserializer.fieldInfo.fieldType;
                    if (cls2 == Integer.TYPE) {
                        this.lexer.nextTokenWithChar(':');
                        obj2 = IntegerCodec.instance.deserialze(this, type, null);
                    } else if (cls2 == String.class) {
                        this.lexer.nextTokenWithChar(':');
                        obj2 = parseString();
                    } else if (cls2 == Long.TYPE) {
                        this.lexer.nextTokenWithChar(':');
                        obj2 = IntegerCodec.instance.deserialze(this, type, null);
                    } else {
                        ObjectDeserializer deserializer2 = this.config.getDeserializer(cls2, type);
                        this.lexer.nextTokenWithChar(':');
                        obj2 = deserializer2.deserialze(this, type, null);
                    }
                    fieldDeserializer.setValue(obj, obj2);
                    if (this.lexer.token != 16 && this.lexer.token == 13) {
                        this.lexer.nextToken(16);
                        return;
                    }
                } else if ((this.lexer.features & Feature.IgnoreNotMatch.mask) != 0) {
                    this.lexer.nextTokenWithChar(':');
                    parse();
                    if (this.lexer.token == 13) {
                        this.lexer.nextToken();
                        return;
                    }
                } else {
                    throw new JSONException("setter not found, class " + cls.getName() + ", property " + scanSymbol);
                }
            }
        } else {
            throw new JSONException("syntax error, expect {, actual " + JSONToken.name(i));
        }
    }

    public Object parseArrayWithType(Type type) {
        if (this.lexer.token == 8) {
            this.lexer.nextToken();
            return null;
        }
        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        if (actualTypeArguments.length == 1) {
            Type type2 = actualTypeArguments[0];
            if (type2 instanceof Class) {
                ArrayList arrayList = new ArrayList();
                parseArray((Class) type2, (Collection) arrayList);
                return arrayList;
            } else if (type2 instanceof WildcardType) {
                WildcardType wildcardType = (WildcardType) type2;
                Type type3 = wildcardType.getUpperBounds()[0];
                if (!Object.class.equals(type3)) {
                    ArrayList arrayList2 = new ArrayList();
                    parseArray((Class) type3, (Collection) arrayList2);
                    return arrayList2;
                } else if (wildcardType.getLowerBounds().length == 0) {
                    return parse();
                } else {
                    throw new JSONException("not support type : " + type);
                }
            } else {
                if (type2 instanceof TypeVariable) {
                    TypeVariable typeVariable = (TypeVariable) type2;
                    Type[] bounds = typeVariable.getBounds();
                    if (bounds.length == 1) {
                        Type type4 = bounds[0];
                        if (type4 instanceof Class) {
                            ArrayList arrayList3 = new ArrayList();
                            parseArray((Class) type4, (Collection) arrayList3);
                            return arrayList3;
                        }
                    } else {
                        throw new JSONException("not support : " + typeVariable);
                    }
                }
                if (type2 instanceof ParameterizedType) {
                    ArrayList arrayList4 = new ArrayList();
                    parseArray((ParameterizedType) type2, arrayList4);
                    return arrayList4;
                }
                throw new JSONException("TODO : " + type);
            }
        } else {
            throw new JSONException("not support type " + type);
        }
    }

    /* access modifiers changed from: protected */
    public void checkListResolve(Collection collection) {
        if (collection instanceof List) {
            ResolveTask lastResolveTask = getLastResolveTask();
            lastResolveTask.fieldDeserializer = new ResolveFieldDeserializer(this, (List) collection, collection.size() - 1);
            lastResolveTask.ownerContext = this.contex;
            this.resolveStatus = 0;
            return;
        }
        ResolveTask lastResolveTask2 = getLastResolveTask();
        lastResolveTask2.fieldDeserializer = new ResolveFieldDeserializer(collection);
        lastResolveTask2.ownerContext = this.contex;
        this.resolveStatus = 0;
    }

    /* access modifiers changed from: protected */
    public void checkMapResolve(Map map, Object obj) {
        ResolveFieldDeserializer resolveFieldDeserializer = new ResolveFieldDeserializer(map, obj);
        ResolveTask lastResolveTask = getLastResolveTask();
        lastResolveTask.fieldDeserializer = resolveFieldDeserializer;
        lastResolveTask.ownerContext = this.contex;
        this.resolveStatus = 0;
    }

    public Object parseObject(Map map) {
        return parseObject(map, (Object) null);
    }

    public JSONObject parseObject() {
        JSONObject jSONObject;
        if ((this.lexer.features & Feature.OrderedField.mask) != 0) {
            jSONObject = new JSONObject(new LinkedHashMap());
        } else {
            jSONObject = new JSONObject();
        }
        return (JSONObject) parseObject(jSONObject, (Object) null);
    }

    public final void parseArray(Collection collection) {
        parseArray(collection, (Object) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:109:0x0202  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0217  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0220  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00d5  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0112 A[LOOP:1: B:59:0x0110->B:60:0x0112, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0120  */
    public final void parseArray(Collection collection, Object obj) {
        boolean z;
        int i;
        char c;
        int i2;
        Number number;
        char c2;
        JSONObject jSONObject;
        char c3;
        char c4;
        char c5;
        char c6;
        int i3 = this.lexer.token;
        if (i3 == 21 || i3 == 22) {
            this.lexer.nextToken();
            i3 = this.lexer.token;
        }
        if (i3 == 14) {
            boolean z2 = this.lexer.disableCircularReferenceDetect;
            ParseContext parseContext = this.contex;
            if (!z2) {
                setContext(parseContext, collection, obj);
            }
            try {
                char c7 = this.lexer.ch;
                char c8 = ']';
                if (c7 != '\"') {
                    if (c7 == ']') {
                        this.lexer.next();
                        this.lexer.nextToken(16);
                        if (z2) {
                            return;
                        }
                        return;
                    } else if (c7 == '{') {
                        JSONLexer jSONLexer = this.lexer;
                        int i4 = jSONLexer.bp + 1;
                        jSONLexer.bp = i4;
                        JSONLexer jSONLexer2 = this.lexer;
                        if (i4 >= this.lexer.len) {
                            c6 = JSONLexer.EOI;
                        } else {
                            c6 = this.lexer.text.charAt(i4);
                        }
                        jSONLexer2.ch = c6;
                        this.lexer.token = 12;
                    } else {
                        this.lexer.nextToken(12);
                    }
                } else if ((this.lexer.features & Feature.AllowISO8601DateFormat.mask) == 0) {
                    z = true;
                    i = 0;
                    while (true) {
                        if (z && this.lexer.ch == '\"') {
                            String scanStringValue = this.lexer.scanStringValue('\"');
                            c3 = this.lexer.ch;
                            if (c3 != ',') {
                                JSONLexer jSONLexer3 = this.lexer;
                                int i5 = jSONLexer3.bp + 1;
                                jSONLexer3.bp = i5;
                                JSONLexer jSONLexer4 = this.lexer;
                                if (i5 >= this.lexer.len) {
                                    c5 = JSONLexer.EOI;
                                } else {
                                    c5 = this.lexer.text.charAt(i5);
                                }
                                jSONLexer4.ch = c5;
                                collection.add(scanStringValue);
                                if (this.resolveStatus == 1) {
                                    checkListResolve(collection);
                                }
                                if (c5 == '\"') {
                                    c = '{';
                                    i++;
                                    c8 = ']';
                                } else {
                                    this.lexer.nextToken();
                                    z = false;
                                }
                            } else if (c3 == c8) {
                                JSONLexer jSONLexer5 = this.lexer;
                                int i6 = jSONLexer5.bp + 1;
                                jSONLexer5.bp = i6;
                                JSONLexer jSONLexer6 = this.lexer;
                                if (i6 >= this.lexer.len) {
                                    c4 = JSONLexer.EOI;
                                } else {
                                    c4 = this.lexer.text.charAt(i6);
                                }
                                jSONLexer6.ch = c4;
                                collection.add(scanStringValue);
                                if (this.resolveStatus == 1) {
                                    checkListResolve(collection);
                                }
                                this.lexer.nextToken(16);
                                if (!z2) {
                                    this.contex = parseContext;
                                    return;
                                }
                                return;
                            } else {
                                this.lexer.nextToken();
                            }
                        }
                        i2 = this.lexer.token;
                        while (i2 == 16) {
                            this.lexer.nextToken();
                            i2 = this.lexer.token;
                        }
                        JSONArray jSONArray = null;
                        jSONArray = null;
                        if (i2 != 2) {
                            number = this.lexer.integerValue();
                            this.lexer.nextToken(16);
                        } else if (i2 != 3) {
                            if (i2 == 4) {
                                String stringVal = this.lexer.stringVal();
                                this.lexer.nextToken(16);
                                jSONArray = stringVal;
                                if ((this.lexer.features & Feature.AllowISO8601DateFormat.mask) != 0) {
                                    JSONLexer jSONLexer7 = new JSONLexer(stringVal);
                                    Date date = stringVal;
                                    if (jSONLexer7.scanISO8601DateIfMatch(true)) {
                                        date = jSONLexer7.calendar.getTime();
                                    }
                                    jSONLexer7.close();
                                    jSONArray = date;
                                }
                            } else if (i2 == 6) {
                                Boolean bool = Boolean.TRUE;
                                this.lexer.nextToken(16);
                                jSONArray = bool;
                            } else if (i2 == 7) {
                                Boolean bool2 = Boolean.FALSE;
                                this.lexer.nextToken(16);
                                jSONArray = bool2;
                            } else if (i2 == 8) {
                                this.lexer.nextToken(4);
                            } else if (i2 == 12) {
                                if ((this.lexer.features & Feature.OrderedField.mask) != 0) {
                                    jSONObject = new JSONObject(new LinkedHashMap());
                                } else {
                                    jSONObject = new JSONObject();
                                }
                                jSONArray = parseObject(jSONObject, Integer.valueOf(i));
                            } else if (i2 == 20) {
                                throw new JSONException("unclosed jsonArray");
                            } else if (i2 == 23) {
                                this.lexer.nextToken(4);
                            } else if (i2 == 14) {
                                JSONArray jSONArray2 = new JSONArray();
                                parseArray(jSONArray2, Integer.valueOf(i));
                                jSONArray = jSONArray2;
                            } else if (i2 != 15) {
                                jSONArray = parse();
                            } else {
                                this.lexer.nextToken(16);
                                if (!z2) {
                                    this.contex = parseContext;
                                    return;
                                }
                                return;
                            }
                            number = jSONArray;
                        } else {
                            if ((this.lexer.features & Feature.UseBigDecimal.mask) != 0) {
                                number = this.lexer.decimalValue(true);
                            } else {
                                number = this.lexer.decimalValue(false);
                            }
                            this.lexer.nextToken(16);
                        }
                        collection.add(number);
                        if (this.resolveStatus == 1) {
                            checkListResolve(collection);
                        }
                        if (this.lexer.token == 16) {
                            char c9 = this.lexer.ch;
                            if (c9 == '\"') {
                                this.lexer.pos = this.lexer.bp;
                                this.lexer.scanString();
                            } else if (c9 < '0' || c9 > '9') {
                                c = '{';
                                if (c9 == '{') {
                                    this.lexer.token = 12;
                                    JSONLexer jSONLexer8 = this.lexer;
                                    int i7 = jSONLexer8.bp + 1;
                                    jSONLexer8.bp = i7;
                                    JSONLexer jSONLexer9 = this.lexer;
                                    if (i7 >= this.lexer.len) {
                                        c2 = JSONLexer.EOI;
                                    } else {
                                        c2 = this.lexer.text.charAt(i7);
                                    }
                                    jSONLexer9.ch = c2;
                                } else {
                                    this.lexer.nextToken();
                                }
                                i++;
                                c8 = ']';
                            } else {
                                this.lexer.pos = this.lexer.bp;
                                this.lexer.scanNumber();
                            }
                        }
                        c = '{';
                        i++;
                        c8 = ']';
                    }
                } else {
                    this.lexer.nextToken(4);
                }
                z = false;
                i = 0;
                while (true) {
                    String scanStringValue2 = this.lexer.scanStringValue('\"');
                    c3 = this.lexer.ch;
                    if (c3 != ',') {
                    }
                    i2 = this.lexer.token;
                    while (i2 == 16) {
                    }
                    JSONArray jSONArray3 = null;
                    jSONArray3 = null;
                    if (i2 != 2) {
                    }
                    collection.add(number);
                    if (this.resolveStatus == 1) {
                    }
                    if (this.lexer.token == 16) {
                    }
                    c = '{';
                    i++;
                    c8 = ']';
                }
            } finally {
                if (!z2) {
                    this.contex = parseContext;
                }
            }
        } else {
            throw new JSONException("syntax error, expect [, actual " + JSONToken.name(i3) + ", pos " + this.lexer.pos);
        }
    }

    /* access modifiers changed from: protected */
    public void addResolveTask(ResolveTask resolveTask) {
        if (this.resolveTaskList == null) {
            this.resolveTaskList = new ArrayList(2);
        }
        this.resolveTaskList.add(resolveTask);
    }

    /* access modifiers changed from: protected */
    public ResolveTask getLastResolveTask() {
        List<ResolveTask> list = this.resolveTaskList;
        return list.get(list.size() - 1);
    }

    public List<ExtraProcessor> getExtraProcessors() {
        if (this.extraProcessors == null) {
            this.extraProcessors = new ArrayList(2);
        }
        return this.extraProcessors;
    }

    public List<ExtraTypeProvider> getExtraTypeProviders() {
        if (this.extraTypeProviders == null) {
            this.extraTypeProviders = new ArrayList(2);
        }
        return this.extraTypeProviders;
    }

    public void setContext(ParseContext parseContext) {
        if (!this.lexer.disableCircularReferenceDetect) {
            this.contex = parseContext;
        }
    }

    /* access modifiers changed from: protected */
    public void popContext() {
        this.contex = this.contex.parent;
        ParseContext[] parseContextArr = this.contextArray;
        int i = this.contextArrayIndex;
        parseContextArr[i - 1] = null;
        this.contextArrayIndex = i - 1;
    }

    /* access modifiers changed from: protected */
    public ParseContext setContext(ParseContext parseContext, Object obj, Object obj2) {
        if (this.lexer.disableCircularReferenceDetect) {
            return null;
        }
        this.contex = new ParseContext(parseContext, obj, obj2);
        int i = this.contextArrayIndex;
        this.contextArrayIndex = i + 1;
        ParseContext[] parseContextArr = this.contextArray;
        if (parseContextArr == null) {
            this.contextArray = new ParseContext[8];
        } else if (i >= parseContextArr.length) {
            ParseContext[] parseContextArr2 = new ParseContext[((parseContextArr.length * 3) / 2)];
            System.arraycopy(parseContextArr, 0, parseContextArr2, 0, parseContextArr.length);
            this.contextArray = parseContextArr2;
        }
        ParseContext[] parseContextArr3 = this.contextArray;
        ParseContext parseContext2 = this.contex;
        parseContextArr3[i] = parseContext2;
        return parseContext2;
    }

    public Object parse() {
        return parse(null);
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public Object parse(Object obj) {
        JSONObject jSONObject;
        int i = this.lexer.token;
        if (i != 2) {
            boolean z = true;
            if (i == 3) {
                if ((this.lexer.features & Feature.UseBigDecimal.mask) == 0) {
                    z = false;
                }
                Number decimalValue = this.lexer.decimalValue(z);
                this.lexer.nextToken();
                return decimalValue;
            } else if (i == 4) {
                String stringVal = this.lexer.stringVal();
                this.lexer.nextToken(16);
                if ((this.lexer.features & Feature.AllowISO8601DateFormat.mask) != 0) {
                    JSONLexer jSONLexer = new JSONLexer(stringVal);
                    try {
                        if (jSONLexer.scanISO8601DateIfMatch(true)) {
                            return jSONLexer.calendar.getTime();
                        }
                        jSONLexer.close();
                    } finally {
                        jSONLexer.close();
                    }
                }
                return stringVal;
            } else if (i == 12) {
                if ((this.lexer.features & Feature.OrderedField.mask) != 0) {
                    jSONObject = new JSONObject(new LinkedHashMap());
                } else {
                    jSONObject = new JSONObject();
                }
                return parseObject(jSONObject, obj);
            } else if (i != 14) {
                switch (i) {
                    case 6:
                        this.lexer.nextToken(16);
                        return Boolean.TRUE;
                    case 7:
                        this.lexer.nextToken(16);
                        return Boolean.FALSE;
                    case 8:
                        break;
                    case 9:
                        this.lexer.nextToken(18);
                        if (this.lexer.token == 18) {
                            this.lexer.nextToken(10);
                            accept(10);
                            long longValue = this.lexer.integerValue().longValue();
                            accept(2);
                            accept(11);
                            return new Date(longValue);
                        }
                        throw new JSONException("syntax error, " + this.lexer.info());
                    default:
                        switch (i) {
                            case 20:
                                if (this.lexer.isBlankInput()) {
                                    return null;
                                }
                                throw new JSONException("syntax error, " + this.lexer.info());
                            case 21:
                                this.lexer.nextToken();
                                HashSet hashSet = new HashSet();
                                parseArray(hashSet, obj);
                                return hashSet;
                            case 22:
                                this.lexer.nextToken();
                                TreeSet treeSet = new TreeSet();
                                parseArray(treeSet, obj);
                                return treeSet;
                            case 23:
                                break;
                            default:
                                throw new JSONException("syntax error, " + this.lexer.info());
                        }
                }
                this.lexer.nextToken();
                return null;
            } else {
                JSONArray jSONArray = new JSONArray();
                parseArray(jSONArray, obj);
                return jSONArray;
            }
        } else {
            Number integerValue = this.lexer.integerValue();
            this.lexer.nextToken();
            return integerValue;
        }
    }

    public void config(Feature feature, boolean z) {
        this.lexer.config(feature, z);
    }

    public final void accept(int i) {
        if (this.lexer.token == i) {
            this.lexer.nextToken();
            return;
        }
        throw new JSONException("syntax error, expect " + JSONToken.name(i) + ", actual " + JSONToken.name(this.lexer.token));
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        try {
            if (this.lexer.token != 20) {
                throw new JSONException("not close json text, token : " + JSONToken.name(this.lexer.token));
            }
        } finally {
            this.lexer.close();
        }
    }

    public void handleResovleTask(Object obj) {
        List<ResolveTask> list = this.resolveTaskList;
        if (list != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                ResolveTask resolveTask = this.resolveTaskList.get(i);
                FieldDeserializer fieldDeserializer = resolveTask.fieldDeserializer;
                if (fieldDeserializer != null) {
                    Object obj2 = null;
                    Object obj3 = resolveTask.ownerContext != null ? resolveTask.ownerContext.object : null;
                    String str = resolveTask.referenceValue;
                    if (str.startsWith("$")) {
                        for (int i2 = 0; i2 < this.contextArrayIndex; i2++) {
                            if (str.equals(this.contextArray[i2].toString())) {
                                obj2 = this.contextArray[i2].object;
                            }
                        }
                    } else {
                        obj2 = resolveTask.context.object;
                    }
                    fieldDeserializer.setValue(obj3, obj2);
                }
            }
        }
    }

    public String parseString() {
        int i = this.lexer.token;
        if (i == 4) {
            String stringVal = this.lexer.stringVal();
            char c = this.lexer.ch;
            char c2 = JSONLexer.EOI;
            if (c == ',') {
                JSONLexer jSONLexer = this.lexer;
                int i2 = jSONLexer.bp + 1;
                jSONLexer.bp = i2;
                JSONLexer jSONLexer2 = this.lexer;
                if (i2 < jSONLexer2.len) {
                    c2 = this.lexer.text.charAt(i2);
                }
                jSONLexer2.ch = c2;
                this.lexer.token = 16;
            } else if (this.lexer.ch == ']') {
                JSONLexer jSONLexer3 = this.lexer;
                int i3 = jSONLexer3.bp + 1;
                jSONLexer3.bp = i3;
                JSONLexer jSONLexer4 = this.lexer;
                if (i3 < jSONLexer4.len) {
                    c2 = this.lexer.text.charAt(i3);
                }
                jSONLexer4.ch = c2;
                this.lexer.token = 15;
            } else if (this.lexer.ch == '}') {
                JSONLexer jSONLexer5 = this.lexer;
                int i4 = jSONLexer5.bp + 1;
                jSONLexer5.bp = i4;
                JSONLexer jSONLexer6 = this.lexer;
                if (i4 < jSONLexer6.len) {
                    c2 = this.lexer.text.charAt(i4);
                }
                jSONLexer6.ch = c2;
                this.lexer.token = 13;
            } else {
                this.lexer.nextToken();
            }
            return stringVal;
        } else if (i == 2) {
            String numberString = this.lexer.numberString();
            this.lexer.nextToken(16);
            return numberString;
        } else {
            Object parse = parse();
            if (parse == null) {
                return null;
            }
            return parse.toString();
        }
    }

    public static class ResolveTask {
        private final ParseContext context;
        public FieldDeserializer fieldDeserializer;
        public ParseContext ownerContext;
        private final String referenceValue;

        public ResolveTask(ParseContext parseContext, String str) {
            this.context = parseContext;
            this.referenceValue = str;
        }
    }
}
