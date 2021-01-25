package ohos.utils.fastjson.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.JSONObject;
import ohos.utils.fastjson.parser.deserializer.ExtraProcessable;
import ohos.utils.fastjson.parser.deserializer.ExtraProcessor;
import ohos.utils.fastjson.parser.deserializer.ExtraTypeProvider;
import ohos.utils.fastjson.parser.deserializer.FieldDeserializer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.util.FieldInfo;
import ohos.utils.fastjson.util.TypeUtils;

public class JavaBeanDeserializer implements ObjectDeserializer {
    private final Map<String, FieldDeserializer> alterNameFieldDeserializers;
    public final JavaBeanInfo beanInfo;
    protected final Class<?> clazz;
    private ConcurrentMap<String, Object> extraFieldDeserializers;
    private final FieldDeserializer[] fieldDeserializers;
    private transient long[] smartMatchHashArray;
    private transient int[] smartMatchHashArrayMapping;
    private final FieldDeserializer[] sortedFieldDeserializers;

    public JavaBeanDeserializer(ParserConfig parserConfig, Class<?> cls, Type type) {
        this(parserConfig, cls, type, JavaBeanInfo.build(cls, cls.getModifiers(), type, false, true, true, true, parserConfig.propertyNamingStrategy));
    }

    public JavaBeanDeserializer(ParserConfig parserConfig, Class<?> cls, Type type, JavaBeanInfo javaBeanInfo) {
        this.clazz = cls;
        this.beanInfo = javaBeanInfo;
        this.sortedFieldDeserializers = new FieldDeserializer[javaBeanInfo.sortedFields.length];
        int length = javaBeanInfo.sortedFields.length;
        HashMap hashMap = null;
        int i = 0;
        while (i < length) {
            FieldInfo fieldInfo = javaBeanInfo.sortedFields[i];
            FieldDeserializer createFieldDeserializer = parserConfig.createFieldDeserializer(parserConfig, cls, fieldInfo);
            this.sortedFieldDeserializers[i] = createFieldDeserializer;
            String[] strArr = fieldInfo.alternateNames;
            HashMap hashMap2 = hashMap;
            for (String str : strArr) {
                if (hashMap2 == null) {
                    hashMap2 = new HashMap();
                }
                hashMap2.put(str, createFieldDeserializer);
            }
            i++;
            hashMap = hashMap2;
        }
        this.alterNameFieldDeserializers = hashMap;
        this.fieldDeserializers = new FieldDeserializer[javaBeanInfo.fields.length];
        int length2 = javaBeanInfo.fields.length;
        for (int i2 = 0; i2 < length2; i2++) {
            this.fieldDeserializers[i2] = getFieldDeserializer(javaBeanInfo.fields[i2].name);
        }
    }

    /* access modifiers changed from: protected */
    public Object createInstance(DefaultJSONParser defaultJSONParser, Type type) {
        Object obj;
        if ((type instanceof Class) && this.clazz.isInterface()) {
            return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{(Class) type}, new JSONObject((defaultJSONParser.lexer.features & Feature.OrderedField.mask) != 0));
        } else if (this.beanInfo.defaultConstructor == null && this.beanInfo.factoryMethod == null) {
            return null;
        } else {
            if (this.beanInfo.factoryMethod != null && this.beanInfo.defaultConstructorParameterSize > 0) {
                return null;
            }
            try {
                Constructor<?> constructor = this.beanInfo.defaultConstructor;
                if (this.beanInfo.defaultConstructorParameterSize != 0) {
                    obj = constructor.newInstance(defaultJSONParser.contex.object);
                } else if (constructor != null) {
                    obj = constructor.newInstance(new Object[0]);
                } else {
                    obj = this.beanInfo.factoryMethod.invoke(null, new Object[0]);
                }
                if (!(defaultJSONParser == null || (defaultJSONParser.lexer.features & Feature.InitStringFieldAsEmpty.mask) == 0)) {
                    FieldInfo[] fieldInfoArr = this.beanInfo.fields;
                    for (FieldInfo fieldInfo : fieldInfoArr) {
                        if (fieldInfo.fieldClass == String.class) {
                            fieldInfo.set(obj, "");
                        }
                    }
                }
                return obj;
            } catch (Exception e) {
                throw new JSONException("create instance error, class " + this.clazz.getName(), e);
            }
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        return (T) deserialze(defaultJSONParser, type, obj, null);
    }

    private <T> T deserialzeArrayMapping(DefaultJSONParser defaultJSONParser, Type type, Object obj, Object obj2) {
        char c;
        char c2;
        char c3;
        char c4;
        char c5;
        Enum r8;
        char c6;
        char c7;
        char c8;
        char c9;
        char c10;
        char c11;
        String str;
        char c12;
        char c13;
        char c14;
        char c15;
        char c16;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        T t = (T) createInstance(defaultJSONParser, type);
        int length = this.sortedFieldDeserializers.length;
        int i = 0;
        while (i < length) {
            boolean z = i == length + -1 ? true : true;
            FieldDeserializer fieldDeserializer = this.sortedFieldDeserializers[i];
            FieldInfo fieldInfo = fieldDeserializer.fieldInfo;
            Class<?> cls = fieldInfo.fieldClass;
            try {
                if (cls == Integer.TYPE) {
                    int scanLongValue = (int) jSONLexer.scanLongValue();
                    if (fieldInfo.fieldAccess) {
                        fieldInfo.field.setInt(t, scanLongValue);
                    } else {
                        fieldDeserializer.setValue(t, new Integer(scanLongValue));
                    }
                    if (jSONLexer.ch == ',') {
                        int i2 = jSONLexer.bp + 1;
                        jSONLexer.bp = i2;
                        if (i2 >= jSONLexer.len) {
                            c16 = JSONLexer.EOI;
                        } else {
                            c16 = jSONLexer.text.charAt(i2);
                        }
                        jSONLexer.ch = c16;
                        jSONLexer.token = 16;
                    } else if (jSONLexer.ch == ']') {
                        int i3 = jSONLexer.bp + 1;
                        jSONLexer.bp = i3;
                        if (i3 >= jSONLexer.len) {
                            c15 = JSONLexer.EOI;
                        } else {
                            c15 = jSONLexer.text.charAt(i3);
                        }
                        jSONLexer.ch = c15;
                        jSONLexer.token = 15;
                    } else {
                        jSONLexer.nextToken();
                    }
                } else if (cls == String.class) {
                    if (jSONLexer.ch == '\"') {
                        str = jSONLexer.scanStringValue('\"');
                    } else if (jSONLexer.ch != 'n' || !jSONLexer.text.startsWith("null", jSONLexer.bp)) {
                        throw new JSONException("not match string. feild : " + obj);
                    } else {
                        jSONLexer.bp += 4;
                        int i4 = jSONLexer.bp;
                        if (jSONLexer.bp >= jSONLexer.len) {
                            c14 = JSONLexer.EOI;
                        } else {
                            c14 = jSONLexer.text.charAt(i4);
                        }
                        jSONLexer.ch = c14;
                        str = null;
                    }
                    if (fieldInfo.fieldAccess) {
                        fieldInfo.field.set(t, str);
                    } else {
                        fieldDeserializer.setValue(t, str);
                    }
                    if (jSONLexer.ch == ',') {
                        int i5 = jSONLexer.bp + 1;
                        jSONLexer.bp = i5;
                        if (i5 >= jSONLexer.len) {
                            c13 = JSONLexer.EOI;
                        } else {
                            c13 = jSONLexer.text.charAt(i5);
                        }
                        jSONLexer.ch = c13;
                        jSONLexer.token = 16;
                    } else if (jSONLexer.ch == ']') {
                        int i6 = jSONLexer.bp + 1;
                        jSONLexer.bp = i6;
                        if (i6 >= jSONLexer.len) {
                            c12 = JSONLexer.EOI;
                        } else {
                            c12 = jSONLexer.text.charAt(i6);
                        }
                        jSONLexer.ch = c12;
                        jSONLexer.token = 15;
                    } else {
                        jSONLexer.nextToken();
                    }
                } else {
                    if (cls == Long.TYPE) {
                        long scanLongValue2 = jSONLexer.scanLongValue();
                        if (fieldInfo.fieldAccess) {
                            fieldInfo.field.setLong(t, scanLongValue2);
                        } else {
                            fieldDeserializer.setValue(t, new Long(scanLongValue2));
                        }
                        if (jSONLexer.ch == ',') {
                            int i7 = jSONLexer.bp + 1;
                            jSONLexer.bp = i7;
                            if (i7 >= jSONLexer.len) {
                                c11 = JSONLexer.EOI;
                            } else {
                                c11 = jSONLexer.text.charAt(i7);
                            }
                            jSONLexer.ch = c11;
                            jSONLexer.token = 16;
                        } else if (jSONLexer.ch == ']') {
                            int i8 = jSONLexer.bp + 1;
                            jSONLexer.bp = i8;
                            if (i8 >= jSONLexer.len) {
                                c10 = JSONLexer.EOI;
                            } else {
                                c10 = jSONLexer.text.charAt(i8);
                            }
                            jSONLexer.ch = c10;
                            jSONLexer.token = 15;
                        } else {
                            jSONLexer.nextToken();
                        }
                    } else if (cls == Boolean.TYPE) {
                        boolean scanBoolean = jSONLexer.scanBoolean();
                        if (fieldInfo.fieldAccess) {
                            fieldInfo.field.setBoolean(t, scanBoolean);
                        } else {
                            fieldDeserializer.setValue(t, Boolean.valueOf(scanBoolean));
                        }
                        if (jSONLexer.ch == ',') {
                            int i9 = jSONLexer.bp + 1;
                            jSONLexer.bp = i9;
                            if (i9 >= jSONLexer.len) {
                                c9 = JSONLexer.EOI;
                            } else {
                                c9 = jSONLexer.text.charAt(i9);
                            }
                            jSONLexer.ch = c9;
                            jSONLexer.token = 16;
                        } else if (jSONLexer.ch == ']') {
                            int i10 = jSONLexer.bp + 1;
                            jSONLexer.bp = i10;
                            if (i10 >= jSONLexer.len) {
                                c8 = JSONLexer.EOI;
                            } else {
                                c8 = jSONLexer.text.charAt(i10);
                            }
                            jSONLexer.ch = c8;
                            jSONLexer.token = 15;
                        } else {
                            jSONLexer.nextToken();
                        }
                    } else if (cls.isEnum()) {
                        char c17 = jSONLexer.ch;
                        if (c17 == '\"') {
                            String scanSymbol = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
                            if (scanSymbol == null) {
                                r8 = null;
                            } else {
                                r8 = Enum.valueOf(cls, scanSymbol);
                            }
                        } else if (c17 < '0' || c17 > '9') {
                            throw new JSONException("illegal enum." + jSONLexer.info());
                        } else {
                            r8 = ((EnumDeserializer) ((DefaultFieldDeserializer) fieldDeserializer).getFieldValueDeserilizer(defaultJSONParser.config)).ordinalEnums[(int) jSONLexer.scanLongValue()];
                        }
                        fieldDeserializer.setValue(t, r8);
                        if (jSONLexer.ch == ',') {
                            int i11 = jSONLexer.bp + 1;
                            jSONLexer.bp = i11;
                            if (i11 >= jSONLexer.len) {
                                c7 = JSONLexer.EOI;
                            } else {
                                c7 = jSONLexer.text.charAt(i11);
                            }
                            jSONLexer.ch = c7;
                            jSONLexer.token = 16;
                        } else if (jSONLexer.ch == ']') {
                            int i12 = jSONLexer.bp + 1;
                            jSONLexer.bp = i12;
                            if (i12 >= jSONLexer.len) {
                                c6 = JSONLexer.EOI;
                            } else {
                                c6 = jSONLexer.text.charAt(i12);
                            }
                            jSONLexer.ch = c6;
                            jSONLexer.token = 15;
                        } else {
                            jSONLexer.nextToken();
                        }
                    } else if (cls == Date.class && jSONLexer.ch == '1') {
                        fieldDeserializer.setValue(t, new Date(jSONLexer.scanLongValue()));
                        if (jSONLexer.ch == ',') {
                            int i13 = jSONLexer.bp + 1;
                            jSONLexer.bp = i13;
                            if (i13 >= jSONLexer.len) {
                                c5 = JSONLexer.EOI;
                            } else {
                                c5 = jSONLexer.text.charAt(i13);
                            }
                            jSONLexer.ch = c5;
                            jSONLexer.token = 16;
                        } else if (jSONLexer.ch == ']') {
                            int i14 = jSONLexer.bp + 1;
                            jSONLexer.bp = i14;
                            if (i14 >= jSONLexer.len) {
                                c4 = JSONLexer.EOI;
                            } else {
                                c4 = jSONLexer.text.charAt(i14);
                            }
                            jSONLexer.ch = c4;
                            jSONLexer.token = 15;
                        } else {
                            jSONLexer.nextToken();
                        }
                    } else {
                        if (jSONLexer.ch == '[') {
                            int i15 = jSONLexer.bp + 1;
                            jSONLexer.bp = i15;
                            if (i15 >= jSONLexer.len) {
                                c3 = JSONLexer.EOI;
                            } else {
                                c3 = jSONLexer.text.charAt(i15);
                            }
                            jSONLexer.ch = c3;
                            jSONLexer.token = 14;
                        } else if (jSONLexer.ch == '{') {
                            int i16 = jSONLexer.bp + 1;
                            jSONLexer.bp = i16;
                            if (i16 >= jSONLexer.len) {
                                c2 = JSONLexer.EOI;
                            } else {
                                c2 = jSONLexer.text.charAt(i16);
                            }
                            jSONLexer.ch = c2;
                            jSONLexer.token = 12;
                        } else {
                            jSONLexer.nextToken();
                        }
                        fieldDeserializer.parseField(defaultJSONParser, t, fieldInfo.fieldType, null);
                        if (z) {
                            if (jSONLexer.token != 15) {
                                throw new JSONException("syntax error");
                            }
                        } else if (z && jSONLexer.token != 16) {
                            throw new JSONException("syntax error");
                        }
                    }
                    i++;
                }
                i++;
            } catch (IllegalAccessException e) {
                throw new JSONException("set " + fieldInfo.name + "error", e);
            }
        }
        if (jSONLexer.ch == ',') {
            int i17 = jSONLexer.bp + 1;
            jSONLexer.bp = i17;
            if (i17 >= jSONLexer.len) {
                c = JSONLexer.EOI;
            } else {
                c = jSONLexer.text.charAt(i17);
            }
            jSONLexer.ch = c;
            jSONLexer.token = 16;
        } else {
            jSONLexer.nextToken();
        }
        return t;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:222:0x02d0, code lost:
        if (r1 == 16) goto L_0x02d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x02e2, code lost:
        r10.nextTokenWithChar(':');
        r0 = r10.token;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:229:0x02e8, code lost:
        if (r0 != 4) goto L_0x036a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x02ea, code lost:
        r0 = r10.stringVal();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:231:0x02f4, code lost:
        if ("@".equals(r0) == false) goto L_0x02fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x02f6, code lost:
        r6 = (T) r2.object;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x0300, code lost:
        if ("..".equals(r0) == false) goto L_0x0317;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x0302, code lost:
        r1 = r2.parent;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x0306, code lost:
        if (r1.object == null) goto L_0x030b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x0308, code lost:
        r6 = (T) r1.object;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x030b, code lost:
        r35.addResolveTask(new ohos.utils.fastjson.parser.DefaultJSONParser.ResolveTask(r1, r0));
        r35.resolveStatus = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:240:0x031d, code lost:
        if ("$".equals(r0) == false) goto L_0x033a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x031f, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x0322, code lost:
        if (r1.parent == null) goto L_0x0327;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:244:0x0324, code lost:
        r1 = r1.parent;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:246:0x0329, code lost:
        if (r1.object == null) goto L_0x032e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:247:0x032b, code lost:
        r6 = (T) r1.object;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:248:0x032e, code lost:
        r35.addResolveTask(new ohos.utils.fastjson.parser.DefaultJSONParser.ResolveTask(r1, r0));
        r35.resolveStatus = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:249:0x033a, code lost:
        r35.addResolveTask(new ohos.utils.fastjson.parser.DefaultJSONParser.ResolveTask(r2, r0));
        r35.resolveStatus = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:250:0x0345, code lost:
        r10.nextToken(13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:251:0x034c, code lost:
        if (r10.token != 13) goto L_0x0360;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:252:0x034e, code lost:
        r10.nextToken(16);
        r35.setContext(r2, r6, r37);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:253:0x0358, code lost:
        if (r20 == null) goto L_0x035c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:254:0x035a, code lost:
        r20.object = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:255:0x035c, code lost:
        r35.setContext(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:256:0x035f, code lost:
        return (T) r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:257:0x0360, code lost:
        r1 = r20;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:260:0x0369, code lost:
        throw new ohos.utils.fastjson.JSONException("illegal ref");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:262:0x0386, code lost:
        throw new ohos.utils.fastjson.JSONException("illegal ref, " + ohos.utils.fastjson.parser.JSONToken.name(r0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:281:0x03c5, code lost:
        r12 = r1;
        r13 = (T) r6;
        r1 = r21;
        r19 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:283:0x03dc, code lost:
        r4 = getSeeAlso(r35.config, r34.beanInfo, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:284:0x03e4, code lost:
        if (r4 != null) goto L_0x040e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:285:0x03e6, code lost:
        r12 = r35.config.checkAutoType(r2, r34.clazz, r10.features);
        r0 = ohos.utils.fastjson.util.TypeUtils.getClass(r36);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:286:0x03f4, code lost:
        if (r0 == null) goto L_0x0407;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:287:0x03f6, code lost:
        if (r12 == null) goto L_0x03ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:289:0x03fc, code lost:
        if (r0.isAssignableFrom(r12) == false) goto L_0x03ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:292:0x0406, code lost:
        throw new ohos.utils.fastjson.JSONException("type not match");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:293:0x0407, code lost:
        r4 = r35.config.getDeserializer(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:294:0x040e, code lost:
        r12 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:296:0x0411, code lost:
        if ((r4 instanceof ohos.utils.fastjson.parser.JavaBeanDeserializer) == false) goto L_0x0426;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:297:0x0413, code lost:
        r4 = (ohos.utils.fastjson.parser.JavaBeanDeserializer) r4;
        r0 = (T) r4.deserialze(r35, r12, r37, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:298:0x041a, code lost:
        if (r3 == null) goto L_0x042a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:299:0x041c, code lost:
        r3 = r4.getFieldDeserializer(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:300:0x0420, code lost:
        if (r3 == null) goto L_0x042a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:301:0x0422, code lost:
        r3.setValue(r0, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:302:0x0426, code lost:
        r0 = (T) r4.deserialze(r35, r12, r37);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:303:0x042a, code lost:
        if (r1 == null) goto L_0x042e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:304:0x042c, code lost:
        r1.object = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:305:0x042e, code lost:
        r35.setContext(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:306:0x0431, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:309:0x043a, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:495:0x0700, code lost:
        throw new ohos.utils.fastjson.JSONException("syntax error, unexpect token " + ohos.utils.fastjson.parser.JSONToken.name(r10.token));
     */
    /* JADX WARNING: Removed duplicated region for block: B:215:0x02b3 A[SYNTHETIC, Splitter:B:215:0x02b3] */
    /* JADX WARNING: Removed duplicated region for block: B:274:0x03a2 A[Catch:{ all -> 0x043a }] */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0442  */
    /* JADX WARNING: Removed duplicated region for block: B:315:0x044b A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:329:0x047c  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x0599  */
    /* JADX WARNING: Removed duplicated region for block: B:427:0x05d1  */
    /* JADX WARNING: Removed duplicated region for block: B:428:0x05d6  */
    /* JADX WARNING: Removed duplicated region for block: B:503:0x0713  */
    /* JADX WARNING: Removed duplicated region for block: B:514:0x0432 A[SYNTHETIC] */
    private <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj, Object obj2) {
        Object obj3;
        Throwable th;
        String str;
        int length;
        int i;
        ParseContext parseContext;
        HashMap hashMap;
        long j;
        Object obj4;
        long j2;
        FieldInfo fieldInfo;
        FieldDeserializer fieldDeserializer;
        Class<?> cls;
        int i2;
        ParseContext parseContext2;
        double d;
        float f;
        long j3;
        int i3;
        boolean z;
        boolean z2;
        Object obj5;
        int i4;
        Object obj6;
        Object obj7;
        Object obj8;
        FieldInfo fieldInfo2;
        String str2;
        HashMap hashMap2;
        String str3;
        int i5;
        char c;
        char c2;
        int i6;
        String str4;
        Object obj9;
        Object obj10;
        HashMap hashMap3;
        char c3;
        if (type == JSON.class || type == JSONObject.class) {
            return (T) defaultJSONParser.parse();
        }
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i7 = jSONLexer.token;
        ParseContext parseContext3 = null;
        if (i7 == 8) {
            jSONLexer.nextToken(16);
            return null;
        }
        boolean z3 = jSONLexer.disableCircularReferenceDetect;
        ParseContext parseContext4 = defaultJSONParser.contex;
        if (!(obj2 == null || parseContext4 == null)) {
            parseContext4 = parseContext4.parent;
        }
        if (i7 == 13) {
            try {
                jSONLexer.nextToken(16);
                T t = obj2 == null ? (T) createInstance(defaultJSONParser, type) : (T) obj2;
                defaultJSONParser.setContext(parseContext4);
                return t;
            } catch (Throwable th2) {
                th = th2;
                obj3 = obj2;
                if (parseContext3 != null) {
                    parseContext3.object = obj3;
                }
                defaultJSONParser.setContext(parseContext4);
                throw th;
            }
        } else {
            if (i7 == 14) {
                if (this.beanInfo.supportBeanToArray || (jSONLexer.features & Feature.SupportArrayToBean.mask) != 0) {
                    T t2 = (T) deserialzeArrayMapping(defaultJSONParser, type, obj, obj2);
                    defaultJSONParser.setContext(parseContext4);
                    return t2;
                }
            }
            if (i7 == 12 || i7 == 16) {
                try {
                    if (defaultJSONParser.resolveStatus == 2) {
                        defaultJSONParser.resolveStatus = 0;
                    }
                    str = this.beanInfo.typeKey;
                    length = this.sortedFieldDeserializers.length;
                    i = 0;
                    parseContext = null;
                    hashMap = null;
                    j = 0;
                    obj4 = obj2;
                    parseContext3 = parseContext;
                    HashMap hashMap4 = hashMap;
                    if (obj6 == null) {
                        if (hashMap4 == null) {
                            try {
                                T t3 = (T) createInstance(defaultJSONParser, type);
                                if (parseContext3 == null) {
                                    try {
                                        parseContext3 = defaultJSONParser.setContext(parseContext4, t3, obj);
                                    } catch (Throwable th3) {
                                        th = th3;
                                        obj3 = t3;
                                    }
                                }
                                if (parseContext3 != null) {
                                    parseContext3.object = t3;
                                }
                                defaultJSONParser.setContext(parseContext4);
                                return t3;
                            } catch (Throwable th4) {
                                th = th4;
                                obj3 = obj6;
                                if (parseContext3 != null) {
                                }
                                defaultJSONParser.setContext(parseContext4);
                                throw th;
                            }
                        } else {
                            String[] strArr = this.beanInfo.creatorConstructorParameters;
                            int length2 = strArr != null ? strArr.length : this.fieldDeserializers.length;
                            Object[] objArr = new Object[length2];
                            for (int i8 = i4; i8 < length2; i8++) {
                                FieldInfo fieldInfo3 = this.fieldDeserializers[i8].fieldInfo;
                                if (strArr != null) {
                                    obj7 = hashMap4.remove(fieldInfo3.name);
                                } else {
                                    obj7 = hashMap4.get(fieldInfo3.name);
                                }
                                if (obj7 == null) {
                                    obj7 = TypeUtils.defaultValue(fieldInfo3.fieldClass);
                                }
                                objArr[i8] = obj7;
                            }
                            if (this.beanInfo.creatorConstructor != null) {
                                try {
                                    Object newInstance = this.beanInfo.creatorConstructor.newInstance(objArr);
                                    if (strArr != null) {
                                        try {
                                            for (Map.Entry<String, Object> entry : hashMap4.entrySet()) {
                                                FieldDeserializer fieldDeserializer2 = getFieldDeserializer(entry.getKey());
                                                if (fieldDeserializer2 != null) {
                                                    fieldDeserializer2.setValue(newInstance, entry.getValue());
                                                }
                                            }
                                        } catch (Throwable th5) {
                                            th = th5;
                                            obj3 = newInstance;
                                            if (parseContext3 != null) {
                                            }
                                            defaultJSONParser.setContext(parseContext4);
                                            throw th;
                                        }
                                    }
                                    obj6 = (T) newInstance;
                                } catch (Exception e) {
                                    throw new JSONException("create instance error, " + this.beanInfo.creatorConstructor.toGenericString(), e);
                                }
                            } else if (this.beanInfo.factoryMethod != null) {
                                try {
                                    obj6 = (T) this.beanInfo.factoryMethod.invoke(null, objArr);
                                } catch (Exception e2) {
                                    throw new JSONException("create factory method error, " + this.beanInfo.factoryMethod.toString(), e2);
                                }
                            }
                        }
                    }
                    if (parseContext3 != null) {
                        parseContext3.object = obj6;
                    }
                    defaultJSONParser.setContext(parseContext4);
                    return (T) obj6;
                } catch (Throwable th6) {
                    th = th6;
                    obj3 = obj2;
                    if (parseContext3 != null) {
                    }
                    defaultJSONParser.setContext(parseContext4);
                    throw th;
                }
                while (true) {
                    if (j != 0) {
                        try {
                            fieldDeserializer = getFieldDeserializerByHash(j);
                            if (fieldDeserializer != null) {
                                fieldInfo = fieldDeserializer.fieldInfo;
                                cls = fieldInfo.fieldClass;
                            } else {
                                cls = null;
                                fieldInfo = null;
                            }
                            j2 = 0;
                        } catch (Throwable th7) {
                            th = th7;
                            parseContext3 = parseContext;
                            obj3 = obj4;
                            if (parseContext3 != null) {
                            }
                            defaultJSONParser.setContext(parseContext4);
                            throw th;
                        }
                    } else {
                        j2 = j;
                        cls = null;
                        fieldDeserializer = null;
                        fieldInfo = null;
                    }
                    if (fieldDeserializer == null) {
                        if (i < length) {
                            fieldDeserializer = this.sortedFieldDeserializers[i];
                            fieldInfo = fieldDeserializer.fieldInfo;
                            cls = fieldInfo.fieldClass;
                        }
                        i++;
                    }
                    double d2 = 0.0d;
                    if (fieldDeserializer != null) {
                        i2 = length;
                        long j4 = fieldInfo.nameHashCode;
                        if (cls != Integer.TYPE) {
                            if (cls != Integer.class) {
                                if (cls != Long.TYPE) {
                                    if (cls != Long.class) {
                                        if (cls == String.class) {
                                            obj5 = jSONLexer.scanFieldString(j4);
                                            if (jSONLexer.matchStat <= 0) {
                                                if (jSONLexer.matchStat == -2) {
                                                    j = jSONLexer.fieldHash;
                                                    i = i;
                                                    length = i2;
                                                }
                                                d = 0.0d;
                                                z2 = false;
                                                z = false;
                                                i3 = 0;
                                                f = ConstantValue.MIN_ZOOM_VALUE;
                                                j3 = 0;
                                                if (z2) {
                                                    try {
                                                        str2 = jSONLexer.scanSymbol(defaultJSONParser.symbolTable);
                                                        if (str2 == null) {
                                                            obj8 = obj5;
                                                            int i9 = jSONLexer.token;
                                                            fieldInfo2 = fieldInfo;
                                                            if (i9 == 13) {
                                                                jSONLexer.nextToken(16);
                                                                parseContext2 = parseContext;
                                                                break;
                                                            }
                                                        } else {
                                                            obj8 = obj5;
                                                            fieldInfo2 = fieldInfo;
                                                        }
                                                    } catch (Throwable th8) {
                                                        th = th8;
                                                        ParseContext parseContext5 = parseContext;
                                                        parseContext3 = parseContext5;
                                                        obj3 = obj4;
                                                        if (parseContext3 != null) {
                                                        }
                                                        defaultJSONParser.setContext(parseContext4);
                                                        throw th;
                                                    }
                                                    if ("$ref" == str2 && parseContext4 != null) {
                                                        break;
                                                    }
                                                    parseContext2 = parseContext;
                                                    if (str != null) {
                                                        if (str.equals(str2)) {
                                                            c3 = ':';
                                                            jSONLexer.nextTokenWithChar(c3);
                                                            if (jSONLexer.token != 4) {
                                                                String stringVal = jSONLexer.stringVal();
                                                                jSONLexer.nextToken(16);
                                                                if (!(type instanceof Class) || !stringVal.equals(((Class) type).getName())) {
                                                                    break;
                                                                }
                                                                if (jSONLexer.token == 13) {
                                                                    jSONLexer.nextToken();
                                                                    break;
                                                                }
                                                                parseContext = parseContext2;
                                                                j = j2;
                                                                i = i;
                                                                length = i2;
                                                            } else {
                                                                throw new JSONException("syntax error");
                                                            }
                                                        }
                                                    }
                                                    if (JSON.DEFAULT_TYPE_KEY == str2) {
                                                        c3 = ':';
                                                        jSONLexer.nextTokenWithChar(c3);
                                                        if (jSONLexer.token != 4) {
                                                        }
                                                    }
                                                } else {
                                                    obj8 = obj5;
                                                    fieldInfo2 = fieldInfo;
                                                    parseContext2 = parseContext;
                                                    str2 = null;
                                                }
                                                if (obj4 == null || hashMap != null) {
                                                    parseContext = parseContext2;
                                                    obj6 = (T) obj4;
                                                    hashMap2 = hashMap;
                                                } else {
                                                    try {
                                                        Object createInstance = createInstance(defaultJSONParser, type);
                                                        if (createInstance == null) {
                                                            parseContext = parseContext2;
                                                            hashMap3 = new HashMap(this.fieldDeserializers.length);
                                                        } else {
                                                            parseContext = parseContext2;
                                                            hashMap3 = hashMap;
                                                        }
                                                        if (!z3) {
                                                            parseContext = defaultJSONParser.setContext(parseContext4, createInstance, obj);
                                                        }
                                                        obj6 = (T) createInstance;
                                                        hashMap2 = hashMap3;
                                                    } catch (Throwable th9) {
                                                        th = th9;
                                                        parseContext = parseContext2;
                                                        parseContext3 = parseContext;
                                                        obj3 = obj4;
                                                        if (parseContext3 != null) {
                                                        }
                                                        defaultJSONParser.setContext(parseContext4);
                                                        throw th;
                                                    }
                                                }
                                                if (z2) {
                                                    i5 = i2;
                                                    str3 = str;
                                                    i6 = 1;
                                                    hashMap = hashMap2;
                                                    i4 = 0;
                                                    if (!parseField(defaultJSONParser, str2, obj6, type, hashMap2)) {
                                                        if (jSONLexer.token == 13) {
                                                            jSONLexer.nextToken();
                                                            break;
                                                        }
                                                        c = '\r';
                                                        c2 = 16;
                                                        str = str3;
                                                        obj4 = (T) obj6;
                                                        j = j2;
                                                        i = i;
                                                        length = i5;
                                                    } else if (jSONLexer.token == 17) {
                                                        throw new JSONException("syntax error, unexpect token ':'");
                                                    }
                                                } else if (!z) {
                                                    try {
                                                        fieldDeserializer.parseField(defaultJSONParser, obj6, type, hashMap2);
                                                        str3 = str;
                                                        hashMap = hashMap2;
                                                        i5 = i2;
                                                        i6 = 1;
                                                        i4 = 0;
                                                    } catch (Throwable th10) {
                                                        th = th10;
                                                        obj4 = (T) obj6;
                                                        parseContext3 = parseContext;
                                                        obj3 = obj4;
                                                        if (parseContext3 != null) {
                                                        }
                                                        defaultJSONParser.setContext(parseContext4);
                                                        throw th;
                                                    }
                                                } else {
                                                    if (obj6 == null) {
                                                        if (cls != Integer.TYPE) {
                                                            if (cls != Integer.class) {
                                                                if (cls != Long.TYPE) {
                                                                    if (cls != Long.class) {
                                                                        if (cls != Float.TYPE) {
                                                                            if (cls != Float.class) {
                                                                                if (cls != Double.TYPE) {
                                                                                    if (cls != Double.class) {
                                                                                        obj9 = obj8;
                                                                                        hashMap2.put(fieldInfo2.name, obj9);
                                                                                        str4 = str;
                                                                                    }
                                                                                }
                                                                                obj10 = new Double(d);
                                                                                obj9 = obj10;
                                                                                hashMap2.put(fieldInfo2.name, obj9);
                                                                                str4 = str;
                                                                            }
                                                                        }
                                                                        obj10 = new Float(f);
                                                                        obj9 = obj10;
                                                                        hashMap2.put(fieldInfo2.name, obj9);
                                                                        str4 = str;
                                                                    }
                                                                }
                                                                obj10 = Long.valueOf(j3);
                                                                obj9 = obj10;
                                                                hashMap2.put(fieldInfo2.name, obj9);
                                                                str4 = str;
                                                            }
                                                        }
                                                        obj10 = Integer.valueOf(i3);
                                                        obj9 = obj10;
                                                        hashMap2.put(fieldInfo2.name, obj9);
                                                        str4 = str;
                                                    } else {
                                                        str4 = str;
                                                        if (obj8 == null) {
                                                            try {
                                                                if (cls != Integer.TYPE) {
                                                                    if (cls != Integer.class) {
                                                                        if (cls != Long.TYPE) {
                                                                            if (cls != Long.class) {
                                                                                if (cls != Float.TYPE) {
                                                                                    if (cls != Float.class) {
                                                                                        if (cls != Double.TYPE) {
                                                                                            if (cls != Double.class) {
                                                                                                fieldDeserializer.setValue(obj6, obj8);
                                                                                            }
                                                                                        }
                                                                                        if (!fieldInfo2.fieldAccess || cls != Double.TYPE) {
                                                                                            fieldDeserializer.setValue(obj6, new Double(d));
                                                                                        } else {
                                                                                            fieldDeserializer.setValue(obj6, d);
                                                                                        }
                                                                                    }
                                                                                }
                                                                                if (!fieldInfo2.fieldAccess || cls != Float.TYPE) {
                                                                                    fieldDeserializer.setValue(obj6, new Float(f));
                                                                                } else {
                                                                                    fieldDeserializer.setValue(obj6, f);
                                                                                }
                                                                            }
                                                                        }
                                                                        if (!fieldInfo2.fieldAccess || cls != Long.TYPE) {
                                                                            fieldDeserializer.setValue(obj6, Long.valueOf(j3));
                                                                        } else {
                                                                            fieldDeserializer.setValue(obj6, j3);
                                                                        }
                                                                    }
                                                                }
                                                                if (!fieldInfo2.fieldAccess || cls != Integer.TYPE) {
                                                                    fieldDeserializer.setValue(obj6, Integer.valueOf(i3));
                                                                } else {
                                                                    fieldDeserializer.setValue(obj6, i3);
                                                                }
                                                            } catch (IllegalAccessException e3) {
                                                                throw new JSONException("set property error, " + fieldInfo2.name, e3);
                                                            }
                                                        } else {
                                                            fieldDeserializer.setValue(obj6, obj8);
                                                        }
                                                    }
                                                    if (jSONLexer.matchStat == 4) {
                                                        hashMap = hashMap2;
                                                        i4 = 0;
                                                        break;
                                                    }
                                                    str3 = str4;
                                                    i5 = i2;
                                                    i6 = 1;
                                                    i4 = 0;
                                                    hashMap = hashMap2;
                                                }
                                                c2 = 16;
                                                if (jSONLexer.token == 16) {
                                                    c = '\r';
                                                    if (jSONLexer.token != 13) {
                                                        if (jSONLexer.token == 18 || jSONLexer.token == i6) {
                                                            break;
                                                        }
                                                    } else {
                                                        jSONLexer.nextToken(16);
                                                        break;
                                                    }
                                                } else {
                                                    c = '\r';
                                                }
                                                str = str3;
                                                obj4 = (T) obj6;
                                                j = j2;
                                                i = i;
                                                length = i5;
                                            }
                                        } else if (cls == Date.class) {
                                            obj5 = jSONLexer.scanFieldDate(j4);
                                            if (jSONLexer.matchStat <= 0) {
                                                if (jSONLexer.matchStat == -2) {
                                                    j = jSONLexer.fieldHash;
                                                    i = i;
                                                    length = i2;
                                                }
                                                d = 0.0d;
                                                z2 = false;
                                                z = false;
                                                i3 = 0;
                                                f = ConstantValue.MIN_ZOOM_VALUE;
                                                j3 = 0;
                                                if (z2) {
                                                }
                                                if (obj4 == null) {
                                                }
                                                parseContext = parseContext2;
                                                obj6 = (T) obj4;
                                                hashMap2 = hashMap;
                                                if (z2) {
                                                }
                                                c2 = 16;
                                                if (jSONLexer.token == 16) {
                                                }
                                                str = str3;
                                                obj4 = (T) obj6;
                                                j = j2;
                                                i = i;
                                                length = i5;
                                            }
                                        } else {
                                            if (cls != Boolean.TYPE) {
                                                if (cls != Boolean.class) {
                                                    if (cls != Float.TYPE) {
                                                        if (cls != Float.class) {
                                                            if (cls != Double.TYPE) {
                                                                if (cls != Double.class) {
                                                                    if (fieldInfo.isEnum && (defaultJSONParser.config.getDeserializer(cls) instanceof EnumDeserializer)) {
                                                                        long scanFieldSymbol = jSONLexer.scanFieldSymbol(j4);
                                                                        if (jSONLexer.matchStat > 0) {
                                                                            obj5 = fieldDeserializer.getEnumByHashCode(scanFieldSymbol);
                                                                            z2 = true;
                                                                            z = true;
                                                                        } else if (jSONLexer.matchStat == -2) {
                                                                            j = jSONLexer.fieldHash;
                                                                            i = i;
                                                                            length = i2;
                                                                        } else {
                                                                            obj5 = null;
                                                                            z2 = false;
                                                                            z = false;
                                                                        }
                                                                        d = 0.0d;
                                                                        i3 = 0;
                                                                        f = ConstantValue.MIN_ZOOM_VALUE;
                                                                        j3 = 0;
                                                                        if (z2) {
                                                                        }
                                                                        if (obj4 == null) {
                                                                        }
                                                                        parseContext = parseContext2;
                                                                        obj6 = (T) obj4;
                                                                        hashMap2 = hashMap;
                                                                        if (z2) {
                                                                        }
                                                                        c2 = 16;
                                                                        if (jSONLexer.token == 16) {
                                                                        }
                                                                        str = str3;
                                                                        obj4 = (T) obj6;
                                                                        j = j2;
                                                                        i = i;
                                                                        length = i5;
                                                                    } else if (cls == int[].class) {
                                                                        obj5 = jSONLexer.scanFieldIntArray(j4);
                                                                        if (jSONLexer.matchStat <= 0) {
                                                                            if (jSONLexer.matchStat == -2) {
                                                                                j = jSONLexer.fieldHash;
                                                                                i = i;
                                                                                length = i2;
                                                                            }
                                                                            d = 0.0d;
                                                                            z2 = false;
                                                                            z = false;
                                                                            i3 = 0;
                                                                            f = ConstantValue.MIN_ZOOM_VALUE;
                                                                            j3 = 0;
                                                                            if (z2) {
                                                                            }
                                                                            if (obj4 == null) {
                                                                            }
                                                                            parseContext = parseContext2;
                                                                            obj6 = (T) obj4;
                                                                            hashMap2 = hashMap;
                                                                            if (z2) {
                                                                            }
                                                                            c2 = 16;
                                                                            if (jSONLexer.token == 16) {
                                                                            }
                                                                            str = str3;
                                                                            obj4 = (T) obj6;
                                                                            j = j2;
                                                                            i = i;
                                                                            length = i5;
                                                                        }
                                                                    } else if (cls == float[].class) {
                                                                        obj5 = jSONLexer.scanFieldFloatArray(j4);
                                                                        if (jSONLexer.matchStat <= 0) {
                                                                            if (jSONLexer.matchStat == -2) {
                                                                                j = jSONLexer.fieldHash;
                                                                                i = i;
                                                                                length = i2;
                                                                            }
                                                                            d = 0.0d;
                                                                            z2 = false;
                                                                            z = false;
                                                                            i3 = 0;
                                                                            f = ConstantValue.MIN_ZOOM_VALUE;
                                                                            j3 = 0;
                                                                            if (z2) {
                                                                            }
                                                                            if (obj4 == null) {
                                                                            }
                                                                            parseContext = parseContext2;
                                                                            obj6 = (T) obj4;
                                                                            hashMap2 = hashMap;
                                                                            if (z2) {
                                                                            }
                                                                            c2 = 16;
                                                                            if (jSONLexer.token == 16) {
                                                                            }
                                                                            str = str3;
                                                                            obj4 = (T) obj6;
                                                                            j = j2;
                                                                            i = i;
                                                                            length = i5;
                                                                        }
                                                                    } else if (cls == double[].class) {
                                                                        obj5 = jSONLexer.scanFieldDoubleArray(j4);
                                                                        if (jSONLexer.matchStat <= 0) {
                                                                            if (jSONLexer.matchStat == -2) {
                                                                                j = jSONLexer.fieldHash;
                                                                                i = i;
                                                                                length = i2;
                                                                            }
                                                                            d = 0.0d;
                                                                            z2 = false;
                                                                            z = false;
                                                                            i3 = 0;
                                                                            f = ConstantValue.MIN_ZOOM_VALUE;
                                                                            j3 = 0;
                                                                            if (z2) {
                                                                            }
                                                                            if (obj4 == null) {
                                                                            }
                                                                            parseContext = parseContext2;
                                                                            obj6 = (T) obj4;
                                                                            hashMap2 = hashMap;
                                                                            if (z2) {
                                                                            }
                                                                            c2 = 16;
                                                                            if (jSONLexer.token == 16) {
                                                                            }
                                                                            str = str3;
                                                                            obj4 = (T) obj6;
                                                                            j = j2;
                                                                            i = i;
                                                                            length = i5;
                                                                        }
                                                                    } else if (cls == float[][].class) {
                                                                        obj5 = jSONLexer.scanFieldFloatArray2(j4);
                                                                        if (jSONLexer.matchStat <= 0) {
                                                                            if (jSONLexer.matchStat == -2) {
                                                                                j = jSONLexer.fieldHash;
                                                                                i = i;
                                                                                length = i2;
                                                                            }
                                                                            d = 0.0d;
                                                                            z2 = false;
                                                                            z = false;
                                                                            i3 = 0;
                                                                            f = ConstantValue.MIN_ZOOM_VALUE;
                                                                            j3 = 0;
                                                                            if (z2) {
                                                                            }
                                                                            if (obj4 == null) {
                                                                            }
                                                                            parseContext = parseContext2;
                                                                            obj6 = (T) obj4;
                                                                            hashMap2 = hashMap;
                                                                            if (z2) {
                                                                            }
                                                                            c2 = 16;
                                                                            if (jSONLexer.token == 16) {
                                                                            }
                                                                            str = str3;
                                                                            obj4 = (T) obj6;
                                                                            j = j2;
                                                                            i = i;
                                                                            length = i5;
                                                                        }
                                                                    } else if (cls == double[][].class) {
                                                                        obj5 = jSONLexer.scanFieldDoubleArray2(j4);
                                                                        if (jSONLexer.matchStat <= 0) {
                                                                            if (jSONLexer.matchStat == -2) {
                                                                                j = jSONLexer.fieldHash;
                                                                                i = i;
                                                                                length = i2;
                                                                            }
                                                                            d = 0.0d;
                                                                            z2 = false;
                                                                            z = false;
                                                                            i3 = 0;
                                                                            f = ConstantValue.MIN_ZOOM_VALUE;
                                                                            j3 = 0;
                                                                            if (z2) {
                                                                            }
                                                                            if (obj4 == null) {
                                                                            }
                                                                            parseContext = parseContext2;
                                                                            obj6 = (T) obj4;
                                                                            hashMap2 = hashMap;
                                                                            if (z2) {
                                                                            }
                                                                            c2 = 16;
                                                                            if (jSONLexer.token == 16) {
                                                                            }
                                                                            str = str3;
                                                                            obj4 = (T) obj6;
                                                                            j = j2;
                                                                            i = i;
                                                                            length = i5;
                                                                        }
                                                                    } else {
                                                                        if (jSONLexer.matchField(fieldInfo.nameHashCode)) {
                                                                            d = 0.0d;
                                                                            obj5 = null;
                                                                            z2 = true;
                                                                            z = false;
                                                                            i3 = 0;
                                                                            f = ConstantValue.MIN_ZOOM_VALUE;
                                                                            j3 = 0;
                                                                            if (z2) {
                                                                            }
                                                                            if (obj4 == null) {
                                                                            }
                                                                            parseContext = parseContext2;
                                                                            obj6 = (T) obj4;
                                                                            hashMap2 = hashMap;
                                                                            if (z2) {
                                                                            }
                                                                            c2 = 16;
                                                                            if (jSONLexer.token == 16) {
                                                                            }
                                                                            str = str3;
                                                                            obj4 = (T) obj6;
                                                                            j = j2;
                                                                            i = i;
                                                                            length = i5;
                                                                        }
                                                                        parseContext2 = parseContext;
                                                                        parseContext = parseContext2;
                                                                        j = j2;
                                                                        i = i;
                                                                        length = i2;
                                                                    }
                                                                }
                                                            }
                                                            d2 = jSONLexer.scanFieldDouble(j4);
                                                            if (jSONLexer.matchStat > 0) {
                                                                d = d2;
                                                                obj5 = null;
                                                                z2 = true;
                                                                z = true;
                                                                i3 = 0;
                                                                f = ConstantValue.MIN_ZOOM_VALUE;
                                                                j3 = 0;
                                                                if (z2) {
                                                                }
                                                                if (obj4 == null) {
                                                                }
                                                                parseContext = parseContext2;
                                                                obj6 = (T) obj4;
                                                                hashMap2 = hashMap;
                                                                if (z2) {
                                                                }
                                                                c2 = 16;
                                                                if (jSONLexer.token == 16) {
                                                                }
                                                                str = str3;
                                                                obj4 = (T) obj6;
                                                                j = j2;
                                                                i = i;
                                                                length = i5;
                                                            } else if (jSONLexer.matchStat == -2) {
                                                                j = jSONLexer.fieldHash;
                                                                i = i;
                                                                length = i2;
                                                            }
                                                        }
                                                    }
                                                    float scanFieldFloat = jSONLexer.scanFieldFloat(j4);
                                                    if (jSONLexer.matchStat > 0) {
                                                        f = scanFieldFloat;
                                                        d = 0.0d;
                                                        obj5 = null;
                                                        z2 = true;
                                                        z = true;
                                                    } else if (jSONLexer.matchStat == -2) {
                                                        j = jSONLexer.fieldHash;
                                                        i = i;
                                                        length = i2;
                                                    } else {
                                                        f = scanFieldFloat;
                                                        d = 0.0d;
                                                        obj5 = null;
                                                        z2 = false;
                                                        z = false;
                                                    }
                                                    i3 = 0;
                                                    j3 = 0;
                                                    if (z2) {
                                                    }
                                                    if (obj4 == null) {
                                                    }
                                                    parseContext = parseContext2;
                                                    obj6 = (T) obj4;
                                                    hashMap2 = hashMap;
                                                    if (z2) {
                                                    }
                                                    c2 = 16;
                                                    if (jSONLexer.token == 16) {
                                                    }
                                                    str = str3;
                                                    obj4 = (T) obj6;
                                                    j = j2;
                                                    i = i;
                                                    length = i5;
                                                }
                                            }
                                            obj5 = Boolean.valueOf(jSONLexer.scanFieldBoolean(j4));
                                            if (jSONLexer.matchStat <= 0) {
                                                if (jSONLexer.matchStat == -2) {
                                                    j = jSONLexer.fieldHash;
                                                    i = i;
                                                    length = i2;
                                                }
                                                d = 0.0d;
                                                z2 = false;
                                                z = false;
                                                i3 = 0;
                                                f = ConstantValue.MIN_ZOOM_VALUE;
                                                j3 = 0;
                                                if (z2) {
                                                }
                                                if (obj4 == null) {
                                                }
                                                parseContext = parseContext2;
                                                obj6 = (T) obj4;
                                                hashMap2 = hashMap;
                                                if (z2) {
                                                }
                                                c2 = 16;
                                                if (jSONLexer.token == 16) {
                                                }
                                                str = str3;
                                                obj4 = (T) obj6;
                                                j = j2;
                                                i = i;
                                                length = i5;
                                            }
                                        }
                                        d = 0.0d;
                                        z2 = true;
                                        z = true;
                                        i3 = 0;
                                        f = ConstantValue.MIN_ZOOM_VALUE;
                                        j3 = 0;
                                        if (z2) {
                                        }
                                        if (obj4 == null) {
                                        }
                                        parseContext = parseContext2;
                                        obj6 = (T) obj4;
                                        hashMap2 = hashMap;
                                        if (z2) {
                                        }
                                        c2 = 16;
                                        if (jSONLexer.token == 16) {
                                        }
                                        str = str3;
                                        obj4 = (T) obj6;
                                        j = j2;
                                        i = i;
                                        length = i5;
                                    }
                                }
                                long scanFieldLong = jSONLexer.scanFieldLong(j4);
                                if (jSONLexer.matchStat > 0) {
                                    d = 0.0d;
                                    z = true;
                                    i3 = 0;
                                    f = ConstantValue.MIN_ZOOM_VALUE;
                                    j3 = scanFieldLong;
                                    obj5 = null;
                                    z2 = true;
                                } else if (jSONLexer.matchStat == -2) {
                                    j = jSONLexer.fieldHash;
                                    i = i;
                                    length = i2;
                                } else {
                                    d = 0.0d;
                                    z = false;
                                    i3 = 0;
                                    f = ConstantValue.MIN_ZOOM_VALUE;
                                    j3 = scanFieldLong;
                                    obj5 = null;
                                    z2 = false;
                                }
                                if (z2) {
                                }
                                if (obj4 == null) {
                                }
                                parseContext = parseContext2;
                                obj6 = (T) obj4;
                                hashMap2 = hashMap;
                                if (z2) {
                                }
                                c2 = 16;
                                if (jSONLexer.token == 16) {
                                }
                                str = str3;
                                obj4 = (T) obj6;
                                j = j2;
                                i = i;
                                length = i5;
                            }
                        }
                        int scanFieldInt = jSONLexer.scanFieldInt(j4);
                        if (jSONLexer.matchStat > 0) {
                            i3 = scanFieldInt;
                            d = 0.0d;
                            obj5 = null;
                            z2 = true;
                            z = true;
                        } else if (jSONLexer.matchStat == -2) {
                            j = jSONLexer.fieldHash;
                            i = i;
                            length = i2;
                        } else {
                            i3 = scanFieldInt;
                            d = 0.0d;
                            obj5 = null;
                            z2 = false;
                            z = false;
                        }
                        f = ConstantValue.MIN_ZOOM_VALUE;
                        j3 = 0;
                        if (z2) {
                        }
                        if (obj4 == null) {
                        }
                        parseContext = parseContext2;
                        obj6 = (T) obj4;
                        hashMap2 = hashMap;
                        if (z2) {
                        }
                        c2 = 16;
                        if (jSONLexer.token == 16) {
                        }
                        str = str3;
                        obj4 = (T) obj6;
                        j = j2;
                        i = i;
                        length = i5;
                    } else {
                        i2 = length;
                    }
                    d = d2;
                    obj5 = null;
                    z2 = false;
                    z = false;
                    i3 = 0;
                    f = ConstantValue.MIN_ZOOM_VALUE;
                    j3 = 0;
                    if (z2) {
                    }
                    if (obj4 == null) {
                    }
                    parseContext = parseContext2;
                    obj6 = (T) obj4;
                    hashMap2 = hashMap;
                    if (z2) {
                    }
                    c2 = 16;
                    if (jSONLexer.token == 16) {
                    }
                    str = str3;
                    obj4 = (T) obj6;
                    j = j2;
                    i = i;
                    length = i5;
                }
            } else if (jSONLexer.isBlankInput()) {
                defaultJSONParser.setContext(parseContext4);
                return null;
            } else if (i7 == 4 && jSONLexer.stringVal().length() == 0) {
                jSONLexer.nextToken();
                defaultJSONParser.setContext(parseContext4);
                return null;
            } else {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("syntax error, expect {, actual ");
                stringBuffer.append(jSONLexer.info());
                if (obj instanceof String) {
                    stringBuffer.append(", fieldName ");
                    stringBuffer.append(obj);
                }
                throw new JSONException(stringBuffer.toString());
            }
        }
    }

    /* access modifiers changed from: protected */
    public FieldDeserializer getFieldDeserializerByHash(long j) {
        int i = 0;
        while (true) {
            FieldDeserializer[] fieldDeserializerArr = this.sortedFieldDeserializers;
            if (i >= fieldDeserializerArr.length) {
                return null;
            }
            FieldDeserializer fieldDeserializer = fieldDeserializerArr[i];
            if (fieldDeserializer.fieldInfo.nameHashCode == j) {
                return fieldDeserializer;
            }
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public FieldDeserializer getFieldDeserializer(String str) {
        if (str == null) {
            return null;
        }
        int i = 0;
        if (this.beanInfo.ordered) {
            while (true) {
                FieldDeserializer[] fieldDeserializerArr = this.sortedFieldDeserializers;
                if (i >= fieldDeserializerArr.length) {
                    return null;
                }
                FieldDeserializer fieldDeserializer = fieldDeserializerArr[i];
                if (fieldDeserializer.fieldInfo.name.equalsIgnoreCase(str)) {
                    return fieldDeserializer;
                }
                i++;
            }
        } else {
            int length = this.sortedFieldDeserializers.length - 1;
            while (i <= length) {
                int i2 = (i + length) >>> 1;
                int compareTo = this.sortedFieldDeserializers[i2].fieldInfo.name.compareTo(str);
                if (compareTo < 0) {
                    i = i2 + 1;
                } else if (compareTo <= 0) {
                    return this.sortedFieldDeserializers[i2];
                } else {
                    length = i2 - 1;
                }
            }
            Map<String, FieldDeserializer> map = this.alterNameFieldDeserializers;
            if (map != null) {
                return map.get(str);
            }
            return null;
        }
    }

    private boolean parseField(DefaultJSONParser defaultJSONParser, String str, Object obj, Type type, Map<String, Object> map) {
        boolean z;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        FieldDeserializer fieldDeserializer = getFieldDeserializer(str);
        if (fieldDeserializer == null) {
            long fnv_64_lower = TypeUtils.fnv_64_lower(str);
            if (this.smartMatchHashArray == null) {
                long[] jArr = new long[this.sortedFieldDeserializers.length];
                int i = 0;
                while (true) {
                    FieldDeserializer[] fieldDeserializerArr = this.sortedFieldDeserializers;
                    if (i >= fieldDeserializerArr.length) {
                        break;
                    }
                    jArr[i] = TypeUtils.fnv_64_lower(fieldDeserializerArr[i].fieldInfo.name);
                    i++;
                }
                Arrays.sort(jArr);
                this.smartMatchHashArray = jArr;
            }
            int binarySearch = Arrays.binarySearch(this.smartMatchHashArray, fnv_64_lower);
            if (binarySearch < 0) {
                z = str.startsWith("is");
                if (z) {
                    binarySearch = Arrays.binarySearch(this.smartMatchHashArray, TypeUtils.fnv_64_lower(str.substring(2)));
                }
            } else {
                z = false;
            }
            if (binarySearch >= 0) {
                if (this.smartMatchHashArrayMapping == null) {
                    int[] iArr = new int[this.smartMatchHashArray.length];
                    Arrays.fill(iArr, -1);
                    int i2 = 0;
                    while (true) {
                        FieldDeserializer[] fieldDeserializerArr2 = this.sortedFieldDeserializers;
                        if (i2 >= fieldDeserializerArr2.length) {
                            break;
                        }
                        int binarySearch2 = Arrays.binarySearch(this.smartMatchHashArray, TypeUtils.fnv_64_lower(fieldDeserializerArr2[i2].fieldInfo.name));
                        if (binarySearch2 >= 0) {
                            iArr[binarySearch2] = i2;
                        }
                        i2++;
                    }
                    this.smartMatchHashArrayMapping = iArr;
                }
                int i3 = this.smartMatchHashArrayMapping[binarySearch];
                if (i3 != -1) {
                    fieldDeserializer = this.sortedFieldDeserializers[i3];
                    Class<?> cls = fieldDeserializer.fieldInfo.fieldClass;
                    if (!(!z || cls == Boolean.TYPE || cls == Boolean.class)) {
                        fieldDeserializer = null;
                    }
                }
            }
        }
        int i4 = Feature.SupportNonPublicField.mask;
        if (fieldDeserializer == null && !((defaultJSONParser.lexer.features & i4) == 0 && (i4 & this.beanInfo.parserFeatures) == 0)) {
            if (this.extraFieldDeserializers == null) {
                ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap(1, 0.75f, 1);
                Class<?> cls2 = this.clazz;
                while (cls2 != null && cls2 != Object.class) {
                    Field[] declaredFields = cls2.getDeclaredFields();
                    for (Field field : declaredFields) {
                        String name = field.getName();
                        if (getFieldDeserializer(name) == null) {
                            int modifiers = field.getModifiers();
                            if ((modifiers & 16) == 0 && (modifiers & 8) == 0) {
                                concurrentHashMap.put(name, field);
                            }
                        }
                    }
                    cls2 = cls2.getSuperclass();
                }
                this.extraFieldDeserializers = concurrentHashMap;
            }
            Object obj2 = this.extraFieldDeserializers.get(str);
            if (obj2 != null) {
                if (obj2 instanceof FieldDeserializer) {
                    fieldDeserializer = (FieldDeserializer) obj2;
                } else {
                    Field field2 = (Field) obj2;
                    field2.setAccessible(true);
                    fieldDeserializer = new DefaultFieldDeserializer(defaultJSONParser.config, this.clazz, new FieldInfo(str, field2.getDeclaringClass(), field2.getType(), field2.getGenericType(), field2, 0, 0));
                    this.extraFieldDeserializers.put(str, fieldDeserializer);
                }
            }
        }
        if (fieldDeserializer == null) {
            parseExtra(defaultJSONParser, obj, str);
            return false;
        }
        jSONLexer.nextTokenWithChar(':');
        fieldDeserializer.parseField(defaultJSONParser, obj, type, map);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void parseExtra(DefaultJSONParser defaultJSONParser, Object obj, String str) {
        Object obj2;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        if ((defaultJSONParser.lexer.features & Feature.IgnoreNotMatch.mask) != 0) {
            jSONLexer.nextTokenWithChar(':');
            Type type = null;
            List<ExtraTypeProvider> list = defaultJSONParser.extraTypeProviders;
            if (list != null) {
                for (ExtraTypeProvider extraTypeProvider : list) {
                    type = extraTypeProvider.getExtraType(obj, str);
                }
            }
            if (type == null) {
                obj2 = defaultJSONParser.parse();
            } else {
                obj2 = defaultJSONParser.parseObject(type);
            }
            if (obj instanceof ExtraProcessable) {
                ((ExtraProcessable) obj).processExtra(str, obj2);
                return;
            }
            List<ExtraProcessor> list2 = defaultJSONParser.extraProcessors;
            if (list2 != null) {
                for (ExtraProcessor extraProcessor : list2) {
                    extraProcessor.processExtra(obj, str, obj2);
                }
                return;
            }
            return;
        }
        throw new JSONException("setter not found, class " + this.clazz.getName() + ", property " + str);
    }

    public Object createInstance(Map<String, Object> map, ParserConfig parserConfig) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Object obj;
        double d;
        float f;
        if (this.beanInfo.creatorConstructor == null) {
            Object createInstance = createInstance((DefaultJSONParser) null, this.clazz);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                FieldDeserializer fieldDeserializer = getFieldDeserializer(entry.getKey());
                if (fieldDeserializer != null) {
                    Object value = entry.getValue();
                    Method method = fieldDeserializer.fieldInfo.method;
                    if (method != null) {
                        method.invoke(createInstance, TypeUtils.cast(value, method.getGenericParameterTypes()[0], parserConfig));
                    } else {
                        Field field = fieldDeserializer.fieldInfo.field;
                        Type type = fieldDeserializer.fieldInfo.fieldType;
                        if (type == Boolean.TYPE) {
                            if (value == Boolean.FALSE) {
                                field.setBoolean(createInstance, false);
                            } else if (value == Boolean.TRUE) {
                                field.setBoolean(createInstance, true);
                            }
                        } else if (type == Integer.TYPE) {
                            if (value instanceof Number) {
                                field.setInt(createInstance, ((Number) value).intValue());
                            }
                        } else if (type == Long.TYPE) {
                            if (value instanceof Number) {
                                field.setLong(createInstance, ((Number) value).longValue());
                            }
                        } else if (type == Float.TYPE) {
                            if (value instanceof Number) {
                                field.setFloat(createInstance, ((Number) value).floatValue());
                            } else if (value instanceof String) {
                                String str = (String) value;
                                if (str.length() <= 10) {
                                    f = TypeUtils.parseFloat(str);
                                } else {
                                    f = Float.parseFloat(str);
                                }
                                field.setFloat(createInstance, f);
                            }
                        } else if (type == Double.TYPE) {
                            if (value instanceof Number) {
                                field.setDouble(createInstance, ((Number) value).doubleValue());
                            } else if (value instanceof String) {
                                String str2 = (String) value;
                                if (str2.length() <= 10) {
                                    d = TypeUtils.parseDouble(str2);
                                } else {
                                    d = Double.parseDouble(str2);
                                }
                                field.setDouble(createInstance, d);
                            }
                        } else if (value != null && type == value.getClass()) {
                            field.set(createInstance, value);
                        }
                        String str3 = fieldDeserializer.fieldInfo.format;
                        if (str3 == null || type != Date.class || !(value instanceof String)) {
                            obj = type instanceof ParameterizedType ? TypeUtils.cast(value, (ParameterizedType) type, parserConfig) : TypeUtils.cast(value, type, parserConfig);
                        } else {
                            try {
                                obj = new SimpleDateFormat(str3).parse((String) value);
                            } catch (ParseException unused) {
                                obj = null;
                            }
                        }
                        field.set(createInstance, obj);
                    }
                }
            }
            return createInstance;
        }
        FieldInfo[] fieldInfoArr = this.beanInfo.fields;
        int length = fieldInfoArr.length;
        Object[] objArr = new Object[length];
        for (int i = 0; i < length; i++) {
            FieldInfo fieldInfo = fieldInfoArr[i];
            Object obj2 = map.get(fieldInfo.name);
            if (obj2 == null) {
                obj2 = TypeUtils.defaultValue(fieldInfo.fieldClass);
            }
            objArr[i] = obj2;
        }
        if (this.beanInfo.creatorConstructor == null) {
            return null;
        }
        try {
            return this.beanInfo.creatorConstructor.newInstance(objArr);
        } catch (Exception e) {
            throw new JSONException("create instance error, " + this.beanInfo.creatorConstructor.toGenericString(), e);
        }
    }

    /* access modifiers changed from: protected */
    public JavaBeanDeserializer getSeeAlso(ParserConfig parserConfig, JavaBeanInfo javaBeanInfo, String str) {
        if (javaBeanInfo.jsonType == null) {
            return null;
        }
        for (Class<?> cls : javaBeanInfo.jsonType.seeAlso()) {
            ObjectDeserializer deserializer = parserConfig.getDeserializer(cls);
            if (deserializer instanceof JavaBeanDeserializer) {
                JavaBeanDeserializer javaBeanDeserializer = (JavaBeanDeserializer) deserializer;
                JavaBeanInfo javaBeanInfo2 = javaBeanDeserializer.beanInfo;
                if (javaBeanInfo2.typeName.equals(str)) {
                    return javaBeanDeserializer;
                }
                JavaBeanDeserializer seeAlso = getSeeAlso(parserConfig, javaBeanInfo2, str);
                if (seeAlso != null) {
                    return seeAlso;
                }
            }
        }
        return null;
    }
}
