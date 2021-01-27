package ohos.com.sun.org.apache.xpath.internal.compiler;

import java.util.HashMap;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncBoolean;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncCeiling;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncConcat;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncContains;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncCount;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncCurrent;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncDoclocation;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncExtElementAvailable;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncExtFunctionAvailable;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncFalse;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncFloor;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncGenerateId;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncId;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncLang;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncLast;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncLocalPart;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncNamespace;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncNormalizeSpace;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncNot;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncNumber;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncPosition;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncQname;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncRound;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncStartsWith;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncString;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncStringLength;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncSubstring;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncSubstringAfter;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncSubstringBefore;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncSum;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncSystemProperty;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncTranslate;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncTrue;
import ohos.com.sun.org.apache.xpath.internal.functions.FuncUnparsedEntityURI;
import ohos.com.sun.org.apache.xpath.internal.functions.Function;
import ohos.javax.xml.transform.TransformerException;

public class FunctionTable {
    public static final int FUNC_BOOLEAN = 14;
    public static final int FUNC_CEILING = 17;
    public static final int FUNC_CONCAT = 27;
    public static final int FUNC_CONTAINS = 22;
    public static final int FUNC_COUNT = 3;
    public static final int FUNC_CURRENT = 0;
    public static final int FUNC_DOCLOCATION = 35;
    public static final int FUNC_EXT_ELEM_AVAILABLE = 34;
    public static final int FUNC_EXT_FUNCTION_AVAILABLE = 33;
    public static final int FUNC_FALSE = 13;
    public static final int FUNC_FLOOR = 16;
    public static final int FUNC_GENERATE_ID = 10;
    public static final int FUNC_ID = 4;
    public static final int FUNC_KEY = 5;
    public static final int FUNC_LANG = 32;
    public static final int FUNC_LAST = 1;
    public static final int FUNC_LOCAL_PART = 7;
    public static final int FUNC_NAMESPACE = 8;
    public static final int FUNC_NORMALIZE_SPACE = 25;
    public static final int FUNC_NOT = 11;
    public static final int FUNC_NUMBER = 15;
    public static final int FUNC_POSITION = 2;
    public static final int FUNC_QNAME = 9;
    public static final int FUNC_ROUND = 18;
    public static final int FUNC_STARTS_WITH = 21;
    public static final int FUNC_STRING = 20;
    public static final int FUNC_STRING_LENGTH = 30;
    public static final int FUNC_SUBSTRING = 29;
    public static final int FUNC_SUBSTRING_AFTER = 24;
    public static final int FUNC_SUBSTRING_BEFORE = 23;
    public static final int FUNC_SUM = 19;
    public static final int FUNC_SYSTEM_PROPERTY = 31;
    public static final int FUNC_TRANSLATE = 26;
    public static final int FUNC_TRUE = 12;
    public static final int FUNC_UNPARSED_ENTITY_URI = 36;
    private static final int NUM_ALLOWABLE_ADDINS = 30;
    private static final int NUM_BUILT_IN_FUNCS = 37;
    private static HashMap m_functionID = new HashMap();
    private static Class[] m_functions = new Class[37];
    private int m_funcNextFreeIndex = 37;
    private HashMap m_functionID_customer = new HashMap();
    private Class[] m_functions_customer = new Class[30];

    static {
        Class[] clsArr = m_functions;
        clsArr[0] = FuncCurrent.class;
        clsArr[1] = FuncLast.class;
        clsArr[2] = FuncPosition.class;
        clsArr[3] = FuncCount.class;
        clsArr[4] = FuncId.class;
        clsArr[7] = FuncLocalPart.class;
        clsArr[8] = FuncNamespace.class;
        clsArr[9] = FuncQname.class;
        clsArr[10] = FuncGenerateId.class;
        clsArr[11] = FuncNot.class;
        clsArr[12] = FuncTrue.class;
        clsArr[13] = FuncFalse.class;
        clsArr[14] = FuncBoolean.class;
        clsArr[32] = FuncLang.class;
        clsArr[15] = FuncNumber.class;
        clsArr[16] = FuncFloor.class;
        clsArr[17] = FuncCeiling.class;
        clsArr[18] = FuncRound.class;
        clsArr[19] = FuncSum.class;
        clsArr[20] = FuncString.class;
        clsArr[21] = FuncStartsWith.class;
        clsArr[22] = FuncContains.class;
        clsArr[23] = FuncSubstringBefore.class;
        clsArr[24] = FuncSubstringAfter.class;
        clsArr[25] = FuncNormalizeSpace.class;
        clsArr[26] = FuncTranslate.class;
        clsArr[27] = FuncConcat.class;
        clsArr[31] = FuncSystemProperty.class;
        clsArr[33] = FuncExtFunctionAvailable.class;
        clsArr[34] = FuncExtElementAvailable.class;
        clsArr[29] = FuncSubstring.class;
        clsArr[30] = FuncStringLength.class;
        clsArr[35] = FuncDoclocation.class;
        clsArr[36] = FuncUnparsedEntityURI.class;
        m_functionID.put(Keywords.FUNC_CURRENT_STRING, new Integer(0));
        m_functionID.put(Keywords.FUNC_LAST_STRING, new Integer(1));
        m_functionID.put(Keywords.FUNC_POSITION_STRING, new Integer(2));
        m_functionID.put("count", new Integer(3));
        m_functionID.put("id", new Integer(4));
        m_functionID.put("key", new Integer(5));
        m_functionID.put(Keywords.FUNC_LOCAL_PART_STRING, new Integer(7));
        m_functionID.put(Keywords.FUNC_NAMESPACE_STRING, new Integer(8));
        m_functionID.put("name", new Integer(9));
        m_functionID.put(Keywords.FUNC_GENERATE_ID_STRING, new Integer(10));
        m_functionID.put(Keywords.FUNC_NOT_STRING, new Integer(11));
        m_functionID.put("true", new Integer(12));
        m_functionID.put("false", new Integer(13));
        m_functionID.put("boolean", new Integer(14));
        m_functionID.put("lang", new Integer(32));
        m_functionID.put("number", new Integer(15));
        m_functionID.put(Keywords.FUNC_FLOOR_STRING, new Integer(16));
        m_functionID.put(Keywords.FUNC_CEILING_STRING, new Integer(17));
        m_functionID.put(Keywords.FUNC_ROUND_STRING, new Integer(18));
        m_functionID.put(Keywords.FUNC_SUM_STRING, new Integer(19));
        m_functionID.put("string", new Integer(20));
        m_functionID.put(Keywords.FUNC_STARTS_WITH_STRING, new Integer(21));
        m_functionID.put(Keywords.FUNC_CONTAINS_STRING, new Integer(22));
        m_functionID.put(Keywords.FUNC_SUBSTRING_BEFORE_STRING, new Integer(23));
        m_functionID.put(Keywords.FUNC_SUBSTRING_AFTER_STRING, new Integer(24));
        m_functionID.put(Keywords.FUNC_NORMALIZE_SPACE_STRING, new Integer(25));
        m_functionID.put(Keywords.FUNC_TRANSLATE_STRING, new Integer(26));
        m_functionID.put(Keywords.FUNC_CONCAT_STRING, new Integer(27));
        m_functionID.put(Keywords.FUNC_SYSTEM_PROPERTY_STRING, new Integer(31));
        m_functionID.put(Keywords.FUNC_EXT_FUNCTION_AVAILABLE_STRING, new Integer(33));
        m_functionID.put(Keywords.FUNC_EXT_ELEM_AVAILABLE_STRING, new Integer(34));
        m_functionID.put(Keywords.FUNC_SUBSTRING_STRING, new Integer(29));
        m_functionID.put(Keywords.FUNC_STRING_LENGTH_STRING, new Integer(30));
        m_functionID.put(Keywords.FUNC_UNPARSED_ENTITY_URI_STRING, new Integer(36));
        m_functionID.put(Keywords.FUNC_DOCLOCATION_STRING, new Integer(35));
    }

    /* access modifiers changed from: package-private */
    public String getFunctionName(int i) {
        if (i < 37) {
            return m_functions[i].getName();
        }
        return this.m_functions_customer[i - 37].getName();
    }

    /* access modifiers changed from: package-private */
    public Function getFunction(int i) throws TransformerException {
        if (i >= 37) {
            return (Function) this.m_functions_customer[i - 37].newInstance();
        }
        try {
            return (Function) m_functions[i].newInstance();
        } catch (IllegalAccessException e) {
            throw new TransformerException(e.getMessage());
        } catch (InstantiationException e2) {
            throw new TransformerException(e2.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public Object getFunctionID(String str) {
        Object obj = this.m_functionID_customer.get(str);
        return obj == null ? m_functionID.get(str) : obj;
    }

    public int installFunction(String str, Class cls) {
        Object functionID = getFunctionID(str);
        if (functionID != null) {
            int intValue = ((Integer) functionID).intValue();
            if (intValue < 37) {
                intValue = this.m_funcNextFreeIndex;
                this.m_funcNextFreeIndex = intValue + 1;
                this.m_functionID_customer.put(str, new Integer(intValue));
            }
            this.m_functions_customer[intValue - 37] = cls;
            return intValue;
        }
        int i = this.m_funcNextFreeIndex;
        this.m_funcNextFreeIndex = i + 1;
        this.m_functions_customer[i - 37] = cls;
        this.m_functionID_customer.put(str, new Integer(i));
        return i;
    }

    public boolean functionAvailable(String str) {
        if (m_functionID.get(str) == null && this.m_functionID_customer.get(str) == null) {
            return false;
        }
        return true;
    }
}
