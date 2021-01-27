package ohos.utils.fastjson.parser;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.parser.deserializer.FieldDeserializer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.util.FieldInfo;
import ohos.utils.fastjson.util.ParameterizedTypeImpl;
import ohos.utils.fastjson.util.TypeUtils;

/* access modifiers changed from: package-private */
public class ListTypeFieldDeserializer extends FieldDeserializer {
    private final boolean array;
    private ObjectDeserializer deserializer;
    private final Type itemType;

    public ListTypeFieldDeserializer(ParserConfig parserConfig, Class<?> cls, FieldInfo fieldInfo) {
        super(cls, fieldInfo, 14);
        Type type = fieldInfo.fieldType;
        Class<?> cls2 = fieldInfo.fieldClass;
        if (cls2.isArray()) {
            this.itemType = cls2.getComponentType();
            this.array = true;
            return;
        }
        this.itemType = TypeUtils.getCollectionItemType(type);
        this.array = false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v0, types: [ohos.utils.fastjson.parser.ListTypeFieldDeserializer] */
    /* JADX WARN: Type inference failed for: r0v5, types: [java.util.ArrayList] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // ohos.utils.fastjson.parser.deserializer.FieldDeserializer
    public void parseField(DefaultJSONParser defaultJSONParser, Object obj, Type type, Map<String, Object> map) {
        JSONArray jSONArray;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token();
        JSONArray jSONArray2 = null;
        if (i == 8 || (i == 4 && jSONLexer.stringVal().length() == 0)) {
            setValue(obj, null);
            defaultJSONParser.lexer.nextToken();
            return;
        }
        if (this.array) {
            JSONArray jSONArray3 = new JSONArray();
            jSONArray3.setComponentType(this.itemType);
            jSONArray = jSONArray3;
            jSONArray2 = jSONArray3;
        } else {
            jSONArray = new ArrayList();
        }
        ParseContext parseContext = defaultJSONParser.contex;
        defaultJSONParser.setContext(parseContext, obj, ((ListTypeFieldDeserializer) this).fieldInfo.name);
        parseArray(defaultJSONParser, type, jSONArray);
        defaultJSONParser.setContext(parseContext);
        Object[] objArr = jSONArray;
        if (this.array) {
            Object[] array2 = jSONArray.toArray((Object[]) Array.newInstance((Class) this.itemType, jSONArray.size()));
            jSONArray2.setRelatedArray(array2);
            objArr = array2;
        }
        if (obj == null) {
            map.put(((ListTypeFieldDeserializer) this).fieldInfo.name, objArr);
        } else {
            setValue(obj, objArr);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0051  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00b7  */
    public final void parseArray(DefaultJSONParser defaultJSONParser, Type type, Collection collection) {
        char c;
        char c2;
        char c3;
        char c4;
        char c5;
        int i;
        int i2;
        Type type2 = this.itemType;
        ObjectDeserializer objectDeserializer = this.deserializer;
        if (type instanceof ParameterizedType) {
            Class cls = null;
            if (type2 instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) type2;
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType() instanceof Class) {
                    cls = (Class) parameterizedType.getRawType();
                }
                if (cls != null) {
                    int length = cls.getTypeParameters().length;
                    i2 = 0;
                    while (true) {
                        if (i2 >= length) {
                            break;
                        } else if (cls.getTypeParameters()[i2].getName().equals(typeVariable.getName())) {
                            break;
                        } else {
                            i2++;
                        }
                    }
                    if (i2 != -1) {
                        type2 = parameterizedType.getActualTypeArguments()[i2];
                        if (!type2.equals(this.itemType)) {
                            objectDeserializer = defaultJSONParser.config.getDeserializer(type2);
                        }
                    }
                }
                i2 = -1;
                if (i2 != -1) {
                }
            } else if (type2 instanceof ParameterizedType) {
                ParameterizedType parameterizedType2 = (ParameterizedType) type2;
                Type[] actualTypeArguments = parameterizedType2.getActualTypeArguments();
                if (actualTypeArguments.length == 1 && (actualTypeArguments[0] instanceof TypeVariable)) {
                    TypeVariable typeVariable2 = (TypeVariable) actualTypeArguments[0];
                    ParameterizedType parameterizedType3 = (ParameterizedType) type;
                    if (parameterizedType3.getRawType() instanceof Class) {
                        cls = (Class) parameterizedType3.getRawType();
                    }
                    if (cls != null) {
                        int length2 = cls.getTypeParameters().length;
                        i = 0;
                        while (true) {
                            if (i >= length2) {
                                break;
                            } else if (cls.getTypeParameters()[i].getName().equals(typeVariable2.getName())) {
                                break;
                            } else {
                                i++;
                            }
                        }
                        if (i != -1) {
                            actualTypeArguments[0] = parameterizedType3.getActualTypeArguments()[i];
                            type2 = new ParameterizedTypeImpl(actualTypeArguments, parameterizedType2.getOwnerType(), parameterizedType2.getRawType());
                        }
                    }
                    i = -1;
                    if (i != -1) {
                    }
                }
            }
        } else if ((type2 instanceof TypeVariable) && (type instanceof Class)) {
            Class cls2 = (Class) type;
            TypeVariable typeVariable3 = (TypeVariable) type2;
            cls2.getTypeParameters();
            int length3 = cls2.getTypeParameters().length;
            int i3 = 0;
            while (true) {
                if (i3 >= length3) {
                    break;
                }
                TypeVariable typeVariable4 = cls2.getTypeParameters()[i3];
                if (typeVariable4.getName().equals(typeVariable3.getName())) {
                    Type[] bounds = typeVariable4.getBounds();
                    if (bounds.length == 1) {
                        type2 = bounds[0];
                    }
                } else {
                    i3++;
                }
            }
        }
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if (objectDeserializer == null) {
            objectDeserializer = defaultJSONParser.config.getDeserializer(type2);
            this.deserializer = objectDeserializer;
        }
        if (jSONLexer.token == 14) {
            int i4 = 0;
            char c6 = jSONLexer.ch;
            char c7 = JSONLexer.EOI;
            if (c6 == '[') {
                int i5 = jSONLexer.bp + 1;
                jSONLexer.bp = i5;
                if (i5 >= jSONLexer.len) {
                    c5 = 26;
                } else {
                    c5 = jSONLexer.text.charAt(i5);
                }
                jSONLexer.ch = c5;
                jSONLexer.token = 14;
            } else if (c6 == '{') {
                int i6 = jSONLexer.bp + 1;
                jSONLexer.bp = i6;
                if (i6 >= jSONLexer.len) {
                    c4 = 26;
                } else {
                    c4 = jSONLexer.text.charAt(i6);
                }
                jSONLexer.ch = c4;
                jSONLexer.token = 12;
            } else if (c6 == '\"') {
                jSONLexer.scanString();
            } else if (c6 == ']') {
                int i7 = jSONLexer.bp + 1;
                jSONLexer.bp = i7;
                if (i7 >= jSONLexer.len) {
                    c3 = 26;
                } else {
                    c3 = jSONLexer.text.charAt(i7);
                }
                jSONLexer.ch = c3;
                jSONLexer.token = 15;
            } else {
                jSONLexer.nextToken();
            }
            while (true) {
                if (jSONLexer.token == 16) {
                    jSONLexer.nextToken();
                } else if (jSONLexer.token == 15) {
                    break;
                } else {
                    collection.add(objectDeserializer.deserialze(defaultJSONParser, type2, Integer.valueOf(i4)));
                    if (defaultJSONParser.resolveStatus == 1) {
                        defaultJSONParser.checkListResolve(collection);
                    }
                    if (jSONLexer.token == 16) {
                        char c8 = jSONLexer.ch;
                        if (c8 == '[') {
                            int i8 = jSONLexer.bp + 1;
                            jSONLexer.bp = i8;
                            if (i8 >= jSONLexer.len) {
                                c2 = 26;
                            } else {
                                c2 = jSONLexer.text.charAt(i8);
                            }
                            jSONLexer.ch = c2;
                            jSONLexer.token = 14;
                        } else if (c8 == '{') {
                            int i9 = jSONLexer.bp + 1;
                            jSONLexer.bp = i9;
                            if (i9 >= jSONLexer.len) {
                                c = 26;
                            } else {
                                c = jSONLexer.text.charAt(i9);
                            }
                            jSONLexer.ch = c;
                            jSONLexer.token = 12;
                        } else if (c8 == '\"') {
                            jSONLexer.scanString();
                        } else {
                            jSONLexer.nextToken();
                        }
                    }
                    i4++;
                }
            }
            if (jSONLexer.ch == ',') {
                int i10 = jSONLexer.bp + 1;
                jSONLexer.bp = i10;
                if (i10 < jSONLexer.len) {
                    c7 = jSONLexer.text.charAt(i10);
                }
                jSONLexer.ch = c7;
                jSONLexer.token = 16;
                return;
            }
            jSONLexer.nextToken();
        } else if (jSONLexer.token == 12) {
            collection.add(objectDeserializer.deserialze(defaultJSONParser, type2, 0));
        } else {
            String str = "exepct '[', but " + JSONToken.name(jSONLexer.token);
            if (type != null) {
                str = str + ", type : " + type;
            }
            throw new JSONException(str);
        }
    }
}
