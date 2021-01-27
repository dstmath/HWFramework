package ohos.utils.fastjson.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;

/* access modifiers changed from: package-private */
public class MapDeserializer implements ObjectDeserializer {
    public static MapDeserializer instance = new MapDeserializer();

    MapDeserializer() {
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        if (type == JSONObject.class && defaultJSONParser.fieldTypeResolver == null) {
            return (T) defaultJSONParser.parseObject();
        }
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if (jSONLexer.token == 8) {
            jSONLexer.nextToken(16);
            return null;
        }
        Map<?, ?> createMap = createMap(type);
        ParseContext parseContext = defaultJSONParser.contex;
        try {
            defaultJSONParser.setContext(parseContext, createMap, obj);
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type type2 = parameterizedType.getActualTypeArguments()[0];
                Type type3 = parameterizedType.getActualTypeArguments()[1];
                if (String.class == type2) {
                    return (T) parseMap(defaultJSONParser, createMap, type3, obj);
                }
                T t = (T) parseMap(defaultJSONParser, createMap, type2, type3, obj);
                defaultJSONParser.setContext(parseContext);
                return t;
            }
            T t2 = (T) defaultJSONParser.parseObject(createMap, obj);
            defaultJSONParser.setContext(parseContext);
            return t2;
        } finally {
            defaultJSONParser.setContext(parseContext);
        }
    }

    public static Map parseMap(DefaultJSONParser defaultJSONParser, Map<String, Object> map, Type type, Object obj) {
        String str;
        Object obj2;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if (jSONLexer.token == 12) {
            ParseContext parseContext = defaultJSONParser.contex;
            while (true) {
                try {
                    jSONLexer.skipWhitespace();
                    char c = jSONLexer.ch;
                    while (c == ',') {
                        jSONLexer.next();
                        jSONLexer.skipWhitespace();
                        c = jSONLexer.ch;
                    }
                    if (c == '\"') {
                        str = jSONLexer.scanSymbol(defaultJSONParser.symbolTable, '\"');
                        jSONLexer.skipWhitespace();
                        if (jSONLexer.ch != ':') {
                            throw new JSONException("syntax error, " + jSONLexer.info());
                        }
                    } else if (c == '}') {
                        jSONLexer.next();
                        jSONLexer.sp = 0;
                        jSONLexer.nextToken(16);
                        defaultJSONParser.setContext(parseContext);
                        return map;
                    } else if (c == '\'') {
                        str = jSONLexer.scanSymbol(defaultJSONParser.symbolTable, '\'');
                        jSONLexer.skipWhitespace();
                        if (jSONLexer.ch != ':') {
                            throw new JSONException("syntax error, " + jSONLexer.info());
                        }
                    } else {
                        str = jSONLexer.scanSymbolUnQuoted(defaultJSONParser.symbolTable);
                        jSONLexer.skipWhitespace();
                        char c2 = jSONLexer.ch;
                        if (c2 != ':') {
                            throw new JSONException("expect ':' at " + jSONLexer.pos + ", actual " + c2);
                        }
                    }
                    jSONLexer.next();
                    jSONLexer.skipWhitespace();
                    char c3 = jSONLexer.ch;
                    jSONLexer.sp = 0;
                    obj2 = null;
                } finally {
                    defaultJSONParser.setContext(parseContext);
                }
                if (str != JSON.DEFAULT_TYPE_KEY || jSONLexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                    jSONLexer.nextToken();
                    defaultJSONParser.setContext(parseContext);
                    if (jSONLexer.token == 8) {
                        jSONLexer.nextToken();
                    } else {
                        obj2 = defaultJSONParser.parseObject(type, str);
                    }
                    map.put(str, obj2);
                    if (defaultJSONParser.resolveStatus == 1) {
                        defaultJSONParser.checkMapResolve(map, str);
                    }
                    defaultJSONParser.setContext(parseContext, obj2, str);
                    int i = jSONLexer.token;
                    if (i == 20 || i == 15) {
                        break;
                    } else if (i == 13) {
                        jSONLexer.nextToken();
                        defaultJSONParser.setContext(parseContext);
                        return map;
                    }
                } else {
                    Class<?> checkAutoType = defaultJSONParser.config.checkAutoType(jSONLexer.scanSymbol(defaultJSONParser.symbolTable, '\"'), null, jSONLexer.features);
                    if (checkAutoType == map.getClass()) {
                        jSONLexer.nextToken(16);
                        if (jSONLexer.token == 13) {
                            jSONLexer.nextToken(16);
                            return map;
                        }
                    } else {
                        ObjectDeserializer deserializer = defaultJSONParser.config.getDeserializer(checkAutoType);
                        jSONLexer.nextToken(16);
                        defaultJSONParser.resolveStatus = 2;
                        if (parseContext != null && !(obj instanceof Integer)) {
                            defaultJSONParser.popContext();
                        }
                        Map map2 = (Map) deserializer.deserialze(defaultJSONParser, checkAutoType, obj);
                        defaultJSONParser.setContext(parseContext);
                        return map2;
                    }
                }
            }
            defaultJSONParser.setContext(parseContext);
            return map;
        }
        throw new JSONException("syntax error, expect {, actual " + jSONLexer.token);
    }

    public static Object parseMap(DefaultJSONParser defaultJSONParser, Map<Object, Object> map, Type type, Type type2, Object obj) {
        Object obj2;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token;
        int i2 = 16;
        if (i == 12 || i == 16) {
            ObjectDeserializer deserializer = defaultJSONParser.config.getDeserializer(type);
            ObjectDeserializer deserializer2 = defaultJSONParser.config.getDeserializer(type2);
            jSONLexer.nextToken();
            ParseContext parseContext = defaultJSONParser.contex;
            while (true) {
                try {
                    int i3 = jSONLexer.token;
                    if (i3 == 13) {
                        jSONLexer.nextToken(i2);
                        defaultJSONParser.setContext(parseContext);
                        return map;
                    } else if (i3 != 4 || jSONLexer.sp != 4 || !jSONLexer.text.startsWith("$ref", jSONLexer.np + 1) || jSONLexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                        if (map.size() == 0 && i3 == 4 && JSON.DEFAULT_TYPE_KEY.equals(jSONLexer.stringVal()) && !jSONLexer.isEnabled(Feature.DisableSpecialKeyDetect)) {
                            jSONLexer.nextTokenWithChar(':');
                            jSONLexer.nextToken(16);
                            if (jSONLexer.token == 13) {
                                jSONLexer.nextToken();
                                return map;
                            }
                            jSONLexer.nextToken();
                        }
                        Object deserialze = deserializer.deserialze(defaultJSONParser, type, null);
                        if (jSONLexer.token == 17) {
                            jSONLexer.nextToken();
                            Object deserialze2 = deserializer2.deserialze(defaultJSONParser, type2, deserialze);
                            if (defaultJSONParser.resolveStatus == 1) {
                                defaultJSONParser.checkMapResolve(map, deserialze);
                            }
                            map.put(deserialze, deserialze2);
                            if (jSONLexer.token == 16) {
                                jSONLexer.nextToken();
                            }
                            i2 = 16;
                        } else {
                            throw new JSONException("syntax error, expect :, actual " + jSONLexer.token);
                        }
                    } else {
                        jSONLexer.nextTokenWithChar(':');
                        if (jSONLexer.token == 4) {
                            String stringVal = jSONLexer.stringVal();
                            if ("..".equals(stringVal)) {
                                obj2 = parseContext.parent.object;
                            } else if ("$".equals(stringVal)) {
                                ParseContext parseContext2 = parseContext;
                                while (parseContext2.parent != null) {
                                    parseContext2 = parseContext2.parent;
                                }
                                obj2 = parseContext2.object;
                            } else {
                                defaultJSONParser.addResolveTask(new DefaultJSONParser.ResolveTask(parseContext, stringVal));
                                defaultJSONParser.resolveStatus = 1;
                                obj2 = null;
                            }
                            jSONLexer.nextToken(13);
                            if (jSONLexer.token == 13) {
                                jSONLexer.nextToken(16);
                                defaultJSONParser.setContext(parseContext);
                                return obj2;
                            }
                            throw new JSONException("illegal ref");
                        }
                        throw new JSONException("illegal ref, " + JSONToken.name(i3));
                    }
                } finally {
                    defaultJSONParser.setContext(parseContext);
                }
            }
        } else {
            throw new JSONException("syntax error, expect {, actual " + JSONToken.name(i));
        }
    }

    /* access modifiers changed from: protected */
    public Map<?, ?> createMap(Type type) {
        if (type == Properties.class) {
            return new Properties();
        }
        if (type == Hashtable.class) {
            return new Hashtable();
        }
        if (type == IdentityHashMap.class) {
            return new IdentityHashMap();
        }
        if (type == SortedMap.class || type == TreeMap.class) {
            return new TreeMap();
        }
        if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
            return new ConcurrentHashMap();
        }
        if (type == Map.class || type == HashMap.class) {
            return new HashMap();
        }
        if (type == LinkedHashMap.class) {
            return new LinkedHashMap();
        }
        if (type == JSONObject.class) {
            return new JSONObject();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (EnumMap.class.equals(rawType)) {
                return new EnumMap((Class) parameterizedType.getActualTypeArguments()[0]);
            }
            return createMap(rawType);
        }
        Class cls = (Class) type;
        if (!cls.isInterface()) {
            try {
                return (Map) cls.newInstance();
            } catch (Exception e) {
                throw new JSONException("unsupport type " + type, e);
            }
        } else {
            throw new JSONException("unsupport type " + type);
        }
    }
}
