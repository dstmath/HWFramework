package ohos.utils.fastjson.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.parser.deserializer.FieldDeserializer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.util.TypeUtils;

public class ThrowableDeserializer extends JavaBeanDeserializer {
    public ThrowableDeserializer(ParserConfig parserConfig, Class<?> cls) {
        super(parserConfig, cls, cls);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v5, types: [ohos.utils.fastjson.parser.JavaBeanDeserializer] */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
        if (java.lang.Throwable.class.isAssignableFrom(r2) != false) goto L_0x0036;
     */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x0185  */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // ohos.utils.fastjson.parser.JavaBeanDeserializer, ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        Class<?> cls;
        T t;
        ThrowableDeserializer throwableDeserializer;
        Object obj2;
        String str;
        ThrowableDeserializer throwableDeserializer2 = this;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if (jSONLexer.token == 8) {
            jSONLexer.nextToken();
            return null;
        }
        if (defaultJSONParser.resolveStatus == 2) {
            defaultJSONParser.resolveStatus = 0;
        } else if (jSONLexer.token != 12) {
            throw new JSONException("syntax error");
        }
        if (type != null && (type instanceof Class)) {
            cls = (Class) type;
        }
        cls = null;
        Class<?> cls2 = cls;
        HashMap hashMap = null;
        Throwable th = null;
        String str2 = null;
        StackTraceElement[] stackTraceElementArr = null;
        while (true) {
            String scanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
            if (scanSymbol == null) {
                if (jSONLexer.token == 13) {
                    jSONLexer.nextToken(16);
                    break;
                } else if (jSONLexer.token == 16) {
                    continue;
                }
            }
            jSONLexer.nextTokenWithChar(':');
            if (JSON.DEFAULT_TYPE_KEY.equals(scanSymbol)) {
                if (jSONLexer.token == 4) {
                    Class<?> loadClass = TypeUtils.loadClass(jSONLexer.stringVal(), defaultJSONParser.config.defaultClassLoader, false);
                    jSONLexer.nextToken(16);
                    cls2 = loadClass;
                } else {
                    throw new JSONException("syntax error");
                }
            } else if ("message".equals(scanSymbol)) {
                if (jSONLexer.token == 8) {
                    str = null;
                } else if (jSONLexer.token == 4) {
                    str = jSONLexer.stringVal();
                } else {
                    throw new JSONException("syntax error");
                }
                jSONLexer.nextToken();
                str2 = str;
            } else if ("cause".equals(scanSymbol)) {
                th = (Throwable) throwableDeserializer2.deserialze(defaultJSONParser, null, "cause");
            } else if ("stackTrace".equals(scanSymbol)) {
                stackTraceElementArr = (StackTraceElement[]) defaultJSONParser.parseObject((Class<Object>) StackTraceElement[].class);
            } else {
                if (hashMap == null) {
                    hashMap = new HashMap();
                }
                hashMap.put(scanSymbol, defaultJSONParser.parse());
            }
            if (jSONLexer.token == 13) {
                jSONLexer.nextToken(16);
                break;
            }
        }
        if (cls2 == null) {
            t = (T) new Exception(str2, th);
        } else {
            try {
                Constructor<?>[] constructors = cls2.getConstructors();
                Constructor<?> constructor = null;
                Constructor<?> constructor2 = null;
                Constructor<?> constructor3 = null;
                for (Constructor<?> constructor4 : constructors) {
                    if (constructor4.getParameterTypes().length == 0) {
                        constructor3 = constructor4;
                    } else if (constructor4.getParameterTypes().length == 1 && constructor4.getParameterTypes()[0] == String.class) {
                        constructor2 = constructor4;
                    } else if (constructor4.getParameterTypes().length == 2 && constructor4.getParameterTypes()[0] == String.class && constructor4.getParameterTypes()[1] == Throwable.class) {
                        constructor = constructor4;
                    }
                }
                if (constructor != null) {
                    obj2 = (Throwable) constructor.newInstance(str2, th);
                } else if (constructor2 != null) {
                    obj2 = (Throwable) constructor2.newInstance(str2);
                } else {
                    obj2 = constructor3 != null ? (Throwable) constructor3.newInstance(new Object[0]) : null;
                }
                t = obj2 == null ? (T) new Exception(str2, th) : (T) obj2;
            } catch (Exception e) {
                throw new JSONException("create instance error", e);
            }
        }
        if (stackTraceElementArr != null) {
            t.setStackTrace(stackTraceElementArr);
        }
        if (hashMap != null) {
            if (cls2 != null) {
                Class<?> cls3 = throwableDeserializer2.clazz;
                throwableDeserializer = throwableDeserializer2;
                if (cls2 != cls3) {
                    ObjectDeserializer deserializer = defaultJSONParser.config.getDeserializer(cls2);
                    if (deserializer instanceof JavaBeanDeserializer) {
                        throwableDeserializer = (JavaBeanDeserializer) deserializer;
                    }
                }
                if (throwableDeserializer != null) {
                    for (Map.Entry entry : hashMap.entrySet()) {
                        Object value = entry.getValue();
                        FieldDeserializer fieldDeserializer = throwableDeserializer.getFieldDeserializer((String) entry.getKey());
                        if (fieldDeserializer != null) {
                            fieldDeserializer.setValue(t, value);
                        }
                    }
                }
            }
            throwableDeserializer = null;
            if (throwableDeserializer != null) {
            }
        }
        return t;
    }
}
