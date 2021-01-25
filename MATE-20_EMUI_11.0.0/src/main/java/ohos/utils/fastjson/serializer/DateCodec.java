package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.Feature;
import ohos.utils.fastjson.parser.JSONLexer;
import ohos.utils.fastjson.parser.JSONToken;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;

public final class DateCodec implements ObjectSerializer, ObjectDeserializer {
    public static final DateCodec instance = new DateCodec();

    private DateCodec() {
    }

    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        Date date;
        char[] cArr;
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (obj == null) {
            serializeWriter.writeNull();
        } else if ((serializeWriter.features & SerializerFeature.WriteClassName.mask) == 0 || obj.getClass() == type) {
            if (obj instanceof Calendar) {
                date = ((Calendar) obj).getTime();
            } else {
                date = (Date) obj;
            }
            if ((serializeWriter.features & SerializerFeature.WriteDateUseDateFormat.mask) != 0) {
                DateFormat dateFormat = jSONSerializer.getDateFormat();
                if (dateFormat == null) {
                    dateFormat = new SimpleDateFormat(JSON.DEFFAULT_DATE_FORMAT, jSONSerializer.locale);
                    dateFormat.setTimeZone(jSONSerializer.timeZone);
                }
                serializeWriter.writeString(dateFormat.format(date));
                return;
            }
            long time = date.getTime();
            if ((serializeWriter.features & SerializerFeature.UseISO8601DateFormat.mask) != 0) {
                if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                    serializeWriter.write(39);
                } else {
                    serializeWriter.write(34);
                }
                Calendar instance2 = Calendar.getInstance(jSONSerializer.timeZone, jSONSerializer.locale);
                instance2.setTimeInMillis(time);
                int i = instance2.get(1);
                int i2 = instance2.get(2) + 1;
                int i3 = instance2.get(5);
                int i4 = instance2.get(11);
                int i5 = instance2.get(12);
                int i6 = instance2.get(13);
                int i7 = instance2.get(14);
                if (i7 != 0) {
                    cArr = "0000-00-00T00:00:00.000".toCharArray();
                    SerializeWriter.getChars((long) i7, 23, cArr);
                    SerializeWriter.getChars((long) i6, 19, cArr);
                    SerializeWriter.getChars((long) i5, 16, cArr);
                    SerializeWriter.getChars((long) i4, 13, cArr);
                    SerializeWriter.getChars((long) i3, 10, cArr);
                    SerializeWriter.getChars((long) i2, 7, cArr);
                    SerializeWriter.getChars((long) i, 4, cArr);
                } else if (i6 == 0 && i5 == 0 && i4 == 0) {
                    cArr = "0000-00-00".toCharArray();
                    SerializeWriter.getChars((long) i3, 10, cArr);
                    SerializeWriter.getChars((long) i2, 7, cArr);
                    SerializeWriter.getChars((long) i, 4, cArr);
                } else {
                    cArr = "0000-00-00T00:00:00".toCharArray();
                    SerializeWriter.getChars((long) i6, 19, cArr);
                    SerializeWriter.getChars((long) i5, 16, cArr);
                    SerializeWriter.getChars((long) i4, 13, cArr);
                    SerializeWriter.getChars((long) i3, 10, cArr);
                    SerializeWriter.getChars((long) i2, 7, cArr);
                    SerializeWriter.getChars((long) i, 4, cArr);
                }
                serializeWriter.write(cArr);
                if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                    serializeWriter.write(39);
                } else {
                    serializeWriter.write(34);
                }
            } else {
                serializeWriter.writeLong(time);
            }
        } else if (obj.getClass() == Date.class) {
            serializeWriter.write("new Date(");
            serializeWriter.writeLong(((Date) obj).getTime());
            serializeWriter.write(41);
        } else {
            serializeWriter.write(123);
            serializeWriter.writeFieldName(JSON.DEFAULT_TYPE_KEY, false);
            jSONSerializer.write(obj.getClass().getName());
            serializeWriter.write(44);
            serializeWriter.writeFieldName("val", false);
            serializeWriter.writeLong(((Date) obj).getTime());
            serializeWriter.write(125);
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        return (T) deserialze(defaultJSONParser, type, obj, null);
    }

    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj, String str) {
        Object obj2;
        T t;
        Long l;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token();
        if (i == 2) {
            Long valueOf = Long.valueOf(jSONLexer.longValue());
            jSONLexer.nextToken(16);
            l = valueOf;
        } else if (i == 4) {
            String stringVal = jSONLexer.stringVal();
            jSONLexer.nextToken(16);
            l = stringVal;
            if ((jSONLexer.features & Feature.AllowISO8601DateFormat.mask) != 0) {
                JSONLexer jSONLexer2 = new JSONLexer(stringVal);
                Date date = stringVal;
                if (jSONLexer2.scanISO8601DateIfMatch(true)) {
                    T t2 = (T) jSONLexer2.calendar;
                    if (type == Calendar.class) {
                        jSONLexer2.close();
                        return t2;
                    }
                    date = t2.getTime();
                }
                jSONLexer2.close();
                l = date;
            }
        } else if (i == 8) {
            jSONLexer.nextToken();
            obj2 = null;
            t = (T) cast(defaultJSONParser, type, obj, obj2, str);
            if (type == Calendar.class || (t instanceof Calendar)) {
                return t;
            }
            T t3 = t;
            if (t3 == null) {
                return null;
            }
            T t4 = (T) Calendar.getInstance(jSONLexer.timeZone, jSONLexer.locale);
            t4.setTime(t3);
            return t4;
        } else if (i == 12) {
            jSONLexer.nextToken();
            if (jSONLexer.token() == 4) {
                if (JSON.DEFAULT_TYPE_KEY.equals(jSONLexer.stringVal())) {
                    jSONLexer.nextToken();
                    defaultJSONParser.accept(17);
                    Class<?> checkAutoType = defaultJSONParser.config.checkAutoType(jSONLexer.stringVal(), null, jSONLexer.features);
                    if (checkAutoType != null) {
                        type = checkAutoType;
                    }
                    defaultJSONParser.accept(4);
                    defaultJSONParser.accept(16);
                }
                jSONLexer.nextTokenWithChar(':');
                int i2 = jSONLexer.token();
                if (i2 == 2) {
                    long longValue = jSONLexer.longValue();
                    jSONLexer.nextToken();
                    Long valueOf2 = Long.valueOf(longValue);
                    defaultJSONParser.accept(13);
                    l = valueOf2;
                } else {
                    throw new JSONException("syntax error : " + JSONToken.name(i2));
                }
            } else {
                throw new JSONException("syntax error");
            }
        } else if (defaultJSONParser.resolveStatus == 2) {
            defaultJSONParser.resolveStatus = 0;
            defaultJSONParser.accept(16);
            if (jSONLexer.token() != 4) {
                throw new JSONException("syntax error");
            } else if ("val".equals(jSONLexer.stringVal())) {
                jSONLexer.nextToken();
                defaultJSONParser.accept(17);
                Object parse = defaultJSONParser.parse();
                defaultJSONParser.accept(13);
                l = parse;
            } else {
                throw new JSONException("syntax error");
            }
        } else {
            l = defaultJSONParser.parse();
        }
        obj2 = l;
        t = (T) cast(defaultJSONParser, type, obj, obj2, str);
        if (type == Calendar.class) {
            return t;
        }
        return t;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public <T> T cast(DefaultJSONParser defaultJSONParser, Type type, Object obj, Object obj2, String str) {
        DateFormat dateFormat;
        if (obj2 == 0) {
            return null;
        }
        if (obj2 instanceof Date) {
            return obj2;
        }
        if (obj2 instanceof BigDecimal) {
            return (T) new Date(((BigDecimal) obj2).longValueExact());
        }
        if (obj2 instanceof Number) {
            return (T) new Date(((Number) obj2).longValue());
        }
        if (obj2 instanceof String) {
            String str2 = (String) obj2;
            if (str2.length() == 0) {
                return null;
            }
            JSONLexer jSONLexer = new JSONLexer(str2);
            try {
                if (jSONLexer.scanISO8601DateIfMatch(false)) {
                    T t = (T) jSONLexer.calendar;
                    if (type == Calendar.class) {
                        return t;
                    }
                    T t2 = (T) t.getTime();
                    jSONLexer.close();
                    return t2;
                }
                jSONLexer.close();
                if ("0000-00-00".equals(str2) || "0000-00-00T00:00:00".equalsIgnoreCase(str2) || "0001-01-01T00:00:00+08:00".equalsIgnoreCase(str2)) {
                    return null;
                }
                if (str != null) {
                    dateFormat = new SimpleDateFormat(str);
                } else {
                    dateFormat = defaultJSONParser.getDateFormat();
                }
                try {
                    return (T) dateFormat.parse(str2);
                } catch (ParseException unused) {
                    return (T) new Date(Long.parseLong(str2));
                }
            } finally {
                jSONLexer.close();
            }
        } else {
            throw new JSONException("parse error");
        }
    }
}
