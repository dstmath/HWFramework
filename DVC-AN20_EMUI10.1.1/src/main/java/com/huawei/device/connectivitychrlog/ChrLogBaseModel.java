package com.huawei.device.connectivitychrlog;

import com.huawei.uikit.effect.BuildConfig;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChrLogBaseModel {
    protected static final int ENCRYPT_LENGTH = 2;
    private static final String GET_LENGTH = "getLength";
    private static final String SET_BYTE_VALUE = "setByByteArray";
    private static final String TO_BYTE_ARRAY = "toByteArray";
    private final String LOG_TAG = ("ChrLogBaseModel_" + getClass().getSimpleName());
    Map<String, Object> fieldMap = new LinkedHashMap();
    Map<String, Integer> lengthMap = new LinkedHashMap();

    public byte[] toByteArray() {
        ByteBuffer bytebuf = ByteBuffer.wrap(new byte[getTotalBytes()]);
        Iterator<Map.Entry<String, Object>> iterator = this.fieldMap.entrySet().iterator();
        Iterator<Map.Entry<String, Integer>> lenthIterator = this.lengthMap.entrySet().iterator();
        while (iterator.hasNext() && lenthIterator.hasNext()) {
            Object key = iterator.next().getValue();
            int lengthKey = lenthIterator.next().getValue().intValue();
            if (key == null) {
                if (lengthKey == 1) {
                    try {
                        bytebuf.put((byte) -1);
                    } catch (Exception e) {
                        ChrLog.chrLogE(this.LOG_TAG, false, "toByteArray exception", new Object[0]);
                    }
                } else if (lengthKey == 2) {
                    bytebuf.put(ByteConvert.shortToBytes(-1));
                } else {
                    ChrLog.chrLogE(this.LOG_TAG, false, "toByteArray exception, invalid class key length  = %{public}d", Integer.valueOf(lengthKey));
                }
            } else if (key instanceof ArrayList) {
                if (((ArrayList) key).size() > 0) {
                    bytebuf.put(ByteConvert.shortToBytes((short) ((ArrayList) key).size()));
                } else {
                    bytebuf.put(ByteConvert.shortToBytes(-1));
                }
                Iterator it = ((ArrayList) key).iterator();
                while (it.hasNext()) {
                    Object item = it.next();
                    bytebuf.put((byte[]) item.getClass().getMethod(TO_BYTE_ARRAY, new Class[0]).invoke(item, new Object[0]));
                }
            } else {
                bytebuf.put((byte[]) key.getClass().getMethod(TO_BYTE_ARRAY, new Class[0]).invoke(key, new Object[0]));
            }
        }
        return bytebuf.array();
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:86:0x0033 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:92:0x0033 */
    /* JADX DEBUG: Multi-variable search result rejected for r7v1, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r4v3, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r4v5, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r4v7, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r4v9, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r4v17, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r4v19, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r4v20, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r4v21, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r7v0 */
    /* JADX WARN: Type inference failed for: r7v2 */
    /* JADX WARN: Type inference failed for: r7v3 */
    /* JADX WARN: Type inference failed for: r4v8 */
    /* JADX WARN: Type inference failed for: r4v10 */
    /* JADX WARN: Type inference failed for: r4v11 */
    /* JADX WARN: Type inference failed for: r4v13 */
    /* JADX WARN: Type inference failed for: r4v15 */
    /* JADX WARN: Type inference failed for: r4v18 */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x014f, code lost:
        r16 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0153, code lost:
        r16 = 1;
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0158, code lost:
        r17 = r4;
        r16 = 1;
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x015f, code lost:
        r17 = r4;
        r16 = 1;
        r4 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0166, code lost:
        r17 = r4;
        r16 = 1;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x014e A[ExcHandler: IllegalAccessException (e java.lang.IllegalAccessException), Splitter:B:53:0x012b] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0165 A[ExcHandler: IllegalAccessException (e java.lang.IllegalAccessException), PHI: r9 
      PHI: (r9v7 'i' int) = (r9v1 'i' int), (r9v1 'i' int), (r9v1 'i' int), (r9v1 'i' int), (r9v8 'i' int), (r9v8 'i' int) binds: [B:33:0x00dc, B:34:?, B:35:0x00e0, B:36:?, B:50:0x0120, B:51:?] A[DONT_GENERATE, DONT_INLINE], Splitter:B:33:0x00dc] */
    public void setByByteArray(byte[] src, int len, boolean bIsLittleEndian) {
        int totalLen;
        char c;
        int totalLen2;
        int totalLen3;
        char c2;
        int i = len;
        int totalLen4 = getLength();
        int i2 = 2;
        char c3 = 1;
        int i3 = 0;
        if (totalLen4 != i) {
            ChrLog.chrLogW(this.LOG_TAG, false, "setByByteArray totalLen = %{public}d, len = %{public}d", Integer.valueOf(totalLen4), Integer.valueOf(len));
        }
        if (totalLen4 <= i) {
            int i4 = 0;
            for (Map.Entry entry : this.fieldMap.entrySet()) {
                Object key = entry.getValue();
                if (i4 >= i) {
                    String str = this.LOG_TAG;
                    Object[] objArr = new Object[i2];
                    objArr[i3] = Integer.valueOf(i4);
                    objArr[c3] = Integer.valueOf(len);
                    ChrLog.chrLogE(str, i3, "setByByteArray error, please check the xml and the vcom data struct , i = %{public}d, len = %{public}d", objArr);
                    return;
                }
                if (key == null) {
                    ChrLog.chrLogD(this.LOG_TAG, i3, "setByByteArray handle class", new Object[i3]);
                    try {
                        Class<?> clazz = getObjectClassByHashMapKey(entry.getKey());
                        if (clazz == null) {
                            ChrLog.chrLogE(this.LOG_TAG, i3, "setByByteArray failed , can not find class when key is null", new Object[i3]);
                            return;
                        }
                        ChrLogBaseModel model = (ChrLogBaseModel) clazz.newInstance();
                        i4 += setObjectByteValue(src, model, i4);
                        setSubClass(entry.getKey(), model);
                        totalLen = totalLen4;
                        totalLen2 = i3;
                        c = 1;
                    } catch (IllegalAccessException e) {
                        ChrLog.chrLogE(this.LOG_TAG, i3, "setByByteArray IllegalAccessException", new Object[i3]);
                    } catch (InstantiationException e2) {
                        ChrLog.chrLogE(this.LOG_TAG, i3, "setByByteArray InstantiationException", new Object[i3]);
                    }
                } else if ("enSubEventId".equals(entry.getKey())) {
                    ChrLog.chrLogD(this.LOG_TAG, i3, "setByByteArray ingnore enSubEventId", new Object[i3]);
                    c3 = 1;
                } else if (key instanceof ArrayList) {
                    ChrLog.chrLogD(this.LOG_TAG, i3, "setByByteArray handle ArrayList", new Object[i3]);
                    int listSize = getArrayListLength((ArrayList) key);
                    if (listSize > 0) {
                        try {
                            Class<?> clazz2 = getListElementClassByHashMapKey(entry.getKey());
                            if (clazz2 == null) {
                                try {
                                    ChrLog.chrLogE(this.LOG_TAG, i3, "setByByteArray failed , can not find class when key is ArrayList", new Object[i3]);
                                    return;
                                } catch (InstantiationException e3) {
                                    totalLen = totalLen4;
                                    totalLen3 = i3;
                                    c = 1;
                                    ChrLog.chrLogE(this.LOG_TAG, totalLen3, "setByByteArray InstantiationException", new Object[totalLen3]);
                                    totalLen2 = totalLen3;
                                    i = len;
                                    i3 = totalLen2;
                                    c3 = c;
                                    totalLen4 = totalLen;
                                    i2 = 2;
                                } catch (IllegalAccessException e4) {
                                    totalLen = totalLen4;
                                    c2 = 1;
                                    totalLen2 = 0;
                                    ChrLog.chrLogE(this.LOG_TAG, false, "setByByteArray IllegalAccessException", new Object[0]);
                                    i = len;
                                    i3 = totalLen2;
                                    c3 = c;
                                    totalLen4 = totalLen;
                                    i2 = 2;
                                }
                            } else {
                                for (int k = 0; k < listSize; k++) {
                                    ChrLogBaseModel model2 = (ChrLogBaseModel) clazz2.newInstance();
                                    i4 += setObjectByteValue(src, model2, i4);
                                    setSubClass(entry.getKey(), model2);
                                }
                                String str2 = this.LOG_TAG;
                                Object[] objArr2 = new Object[2];
                                StringBuilder sb = new StringBuilder();
                                totalLen = totalLen4;
                                try {
                                    sb.append(BuildConfig.FLAVOR);
                                    sb.append(clazz2);
                                    objArr2[0] = sb.toString();
                                    c = 1;
                                    c = 1;
                                    c2 = 1;
                                    try {
                                        objArr2[1] = Integer.valueOf(listSize);
                                        ChrLog.chrLogI(str2, false, "setByByteArray handle ArrayList clazz = %{public}s, listSize = %{public}d", objArr2);
                                        totalLen2 = 0;
                                    } catch (InstantiationException e5) {
                                        totalLen3 = 0;
                                        ChrLog.chrLogE(this.LOG_TAG, totalLen3, "setByByteArray InstantiationException", new Object[totalLen3]);
                                        totalLen2 = totalLen3;
                                        i = len;
                                        i3 = totalLen2;
                                        c3 = c;
                                        totalLen4 = totalLen;
                                        i2 = 2;
                                    } catch (IllegalAccessException e6) {
                                        totalLen2 = 0;
                                        ChrLog.chrLogE(this.LOG_TAG, false, "setByByteArray IllegalAccessException", new Object[0]);
                                        i = len;
                                        i3 = totalLen2;
                                        c3 = c;
                                        totalLen4 = totalLen;
                                        i2 = 2;
                                    }
                                } catch (InstantiationException e7) {
                                    c = 1;
                                    totalLen3 = 0;
                                    ChrLog.chrLogE(this.LOG_TAG, totalLen3, "setByByteArray InstantiationException", new Object[totalLen3]);
                                    totalLen2 = totalLen3;
                                    i = len;
                                    i3 = totalLen2;
                                    c3 = c;
                                    totalLen4 = totalLen;
                                    i2 = 2;
                                } catch (IllegalAccessException e8) {
                                }
                            }
                        } catch (InstantiationException e9) {
                            totalLen = totalLen4;
                            totalLen3 = i3;
                            c = 1;
                            ChrLog.chrLogE(this.LOG_TAG, totalLen3, "setByByteArray InstantiationException", new Object[totalLen3]);
                            totalLen2 = totalLen3;
                            i = len;
                            i3 = totalLen2;
                            c3 = c;
                            totalLen4 = totalLen;
                            i2 = 2;
                        } catch (IllegalAccessException e10) {
                        }
                    } else {
                        ChrLog.chrLogE(this.LOG_TAG, i3, "setByByteArray handle null ArrayList, this should not happen, please check the xml", new Object[i3]);
                        return;
                    }
                } else {
                    totalLen = totalLen4;
                    int totalLen5 = i3;
                    c = 1;
                    ChrLog.chrLogD(this.LOG_TAG, totalLen5, "setByByteArray handle base element", new Object[totalLen5]);
                    i4 += setObjectByteValue(src, key, i4);
                    totalLen2 = totalLen5;
                }
                i = len;
                i3 = totalLen2;
                c3 = c;
                totalLen4 = totalLen;
                i2 = 2;
            }
        }
    }

    private int setObjectByteValue(byte[] src, Object key, int start) {
        int len = 0;
        try {
            len = ((Integer) key.getClass().getMethod(GET_LENGTH, new Class[0]).invoke(key, new Object[0])).intValue();
            if (len > 0) {
                byte[] b = new byte[len];
                Method methodSetByteValue = key.getClass().getMethod(SET_BYTE_VALUE, byte[].class, Integer.TYPE, Boolean.TYPE);
                System.arraycopy(src, start, b, 0, len);
                methodSetByteValue.invoke(key, b, Integer.valueOf(len), true);
            } else {
                String str = this.LOG_TAG;
                ChrLog.chrLogE(str, false, "setObjectByteValue failed len = %{public}d, class = %{public}s", Integer.valueOf(len), BuildConfig.FLAVOR + key.getClass());
            }
        } catch (NoSuchMethodException e) {
            ChrLog.chrLogE(this.LOG_TAG, false, "setObjectByteValue NoSuchMethodException", new Object[0]);
        } catch (IllegalAccessException e2) {
            ChrLog.chrLogE(this.LOG_TAG, false, "setObjectByteValue IllegalAccessException", new Object[0]);
        } catch (InvocationTargetException e3) {
            ChrLog.chrLogE(this.LOG_TAG, false, "setObjectByteValue InvocationTargetException", new Object[0]);
        }
        return len;
    }

    public int getLength() {
        int totalLen = 0;
        for (Map.Entry entry : this.fieldMap.entrySet()) {
            Object key = entry.getValue();
            if (key == null) {
                try {
                    Class<?> clazz = getObjectClassByHashMapKey(entry.getKey());
                    if (clazz == null) {
                        ChrLog.chrLogE(this.LOG_TAG, false, "getLength failed clazz is null", new Object[0]);
                        return totalLen;
                    }
                    totalLen += ((Integer) clazz.getMethod(GET_LENGTH, new Class[0]).invoke((ChrLogBaseModel) clazz.newInstance(), new Object[0])).intValue();
                } catch (NoSuchMethodException e) {
                    ChrLog.chrLogE(this.LOG_TAG, false, "getLength NoSuchMethodException", new Object[0]);
                } catch (IllegalAccessException e2) {
                    ChrLog.chrLogE(this.LOG_TAG, false, "getLength IllegalAccessException", new Object[0]);
                } catch (InstantiationException e3) {
                    ChrLog.chrLogE(this.LOG_TAG, false, "getLength InstantiationException", new Object[0]);
                } catch (InvocationTargetException e4) {
                    ChrLog.chrLogE(this.LOG_TAG, false, "getLength InvocationTargetException", new Object[0]);
                }
            } else if (!"enSubEventId".equals(entry.getKey())) {
                if (key instanceof ArrayList) {
                    int listSize = getArrayListLength((ArrayList) key);
                    if (listSize > 0) {
                        try {
                            Class<?> clazz2 = getListElementClassByHashMapKey(entry.getKey());
                            totalLen += ((Integer) clazz2.getMethod(GET_LENGTH, new Class[0]).invoke((ChrLogBaseModel) clazz2.newInstance(), new Object[0])).intValue() * listSize;
                        } catch (NoSuchMethodException e5) {
                            ChrLog.chrLogE(this.LOG_TAG, false, "getLength NoSuchMethodException", new Object[0]);
                        } catch (IllegalAccessException e6) {
                            ChrLog.chrLogE(this.LOG_TAG, false, "getLength IllegalAccessException", new Object[0]);
                        } catch (InvocationTargetException e7) {
                            ChrLog.chrLogE(this.LOG_TAG, false, "getLength InvocationTargetException", new Object[0]);
                        } catch (InstantiationException e8) {
                            ChrLog.chrLogE(this.LOG_TAG, false, "getLength InstantiationException", new Object[0]);
                        }
                    }
                } else {
                    try {
                        totalLen += ((Integer) key.getClass().getMethod(GET_LENGTH, new Class[0]).invoke(key, new Object[0])).intValue();
                    } catch (NoSuchMethodException e9) {
                        ChrLog.chrLogE(this.LOG_TAG, false, "getLength NoSuchMethodException", new Object[0]);
                    } catch (IllegalAccessException e10) {
                        ChrLog.chrLogE(this.LOG_TAG, false, "getLength IllegalAccessException", new Object[0]);
                    } catch (InvocationTargetException e11) {
                        ChrLog.chrLogE(this.LOG_TAG, false, "getLength InvocationTargetException", new Object[0]);
                    }
                }
            }
        }
        return totalLen;
    }

    /* access modifiers changed from: package-private */
    public int getTotalBytes() {
        int totalBytes = 0;
        Iterator<Map.Entry<String, Integer>> iterator = this.lengthMap.entrySet().iterator();
        while (iterator.hasNext()) {
            totalBytes += iterator.next().getValue().intValue();
        }
        return totalBytes;
    }

    private Class<?> getObjectClassByHashMapKey(String key) {
        String packageName = ChrLogBaseModel.class.getPackage().getName();
        try {
            return Class.forName(packageName + ".CSub" + key.substring(1));
        } catch (ClassNotFoundException e) {
            ChrLog.chrLogE(this.LOG_TAG, false, "getObjectClassByHashMapKey ClassNotFoundException", new Object[0]);
            ChrLog.chrLogE(this.LOG_TAG, false, "getObjectClassByHashMapKey failed , class is null", new Object[0]);
            return null;
        }
    }

    private Class<?> getListElementClassByHashMapKey(String key) {
        String packageName = ChrLogBaseModel.class.getPackage().getName();
        try {
            return Class.forName(packageName + ".CSub" + key.substring(1, key.length() - 4));
        } catch (ClassNotFoundException e) {
            ChrLog.chrLogE(this.LOG_TAG, false, "getListElementClassByHashMapKey ClassNotFoundException", new Object[0]);
            ChrLog.chrLogE(this.LOG_TAG, false, "getListElementClassByHashMapKey failed , class is null", new Object[0]);
            return null;
        }
    }

    private int getArrayListLength(ArrayList list) {
        if (list != null) {
            try {
                Field f = ArrayList.class.getDeclaredField("array");
                f.setAccessible(true);
                return ((Object[]) f.get(list)).length;
            } catch (IllegalAccessException e) {
                ChrLog.chrLogE(this.LOG_TAG, false, "getArrayListLength IllegalAccessException", new Object[0]);
            } catch (NoSuchFieldException e2) {
                ChrLog.chrLogE(this.LOG_TAG, false, "getArrayListLength NoSuchFieldException", new Object[0]);
            }
        }
        ChrLog.chrLogE(this.LOG_TAG, false, "getArrayListLength failed , please check the xml", new Object[0]);
        return 0;
    }

    private void setSubClass(String key, ChrLogBaseModel model) {
        try {
            getClass().getMethod("setCSub" + key.substring(1), model.getClass()).invoke(this, model);
        } catch (NoSuchMethodException e) {
            ChrLog.chrLogE(this.LOG_TAG, false, "setSubClass NoSuchMethodException", new Object[0]);
        } catch (IllegalAccessException e2) {
            ChrLog.chrLogE(this.LOG_TAG, false, "setSubClass IllegalAccessException", new Object[0]);
        } catch (InvocationTargetException e3) {
            ChrLog.chrLogE(this.LOG_TAG, false, "setSubClass InvocationTargetException", new Object[0]);
        }
    }

    public Map getfieldMap() {
        return this.fieldMap;
    }
}
