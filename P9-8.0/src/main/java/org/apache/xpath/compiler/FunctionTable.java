package org.apache.xpath.compiler;

import java.util.HashMap;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.FuncKey;
import org.apache.xpath.functions.FuncBoolean;
import org.apache.xpath.functions.FuncCeiling;
import org.apache.xpath.functions.FuncConcat;
import org.apache.xpath.functions.FuncContains;
import org.apache.xpath.functions.FuncCount;
import org.apache.xpath.functions.FuncCurrent;
import org.apache.xpath.functions.FuncDoclocation;
import org.apache.xpath.functions.FuncExtElementAvailable;
import org.apache.xpath.functions.FuncExtFunctionAvailable;
import org.apache.xpath.functions.FuncFalse;
import org.apache.xpath.functions.FuncFloor;
import org.apache.xpath.functions.FuncGenerateId;
import org.apache.xpath.functions.FuncId;
import org.apache.xpath.functions.FuncLang;
import org.apache.xpath.functions.FuncLast;
import org.apache.xpath.functions.FuncLocalPart;
import org.apache.xpath.functions.FuncNamespace;
import org.apache.xpath.functions.FuncNormalizeSpace;
import org.apache.xpath.functions.FuncNot;
import org.apache.xpath.functions.FuncNumber;
import org.apache.xpath.functions.FuncPosition;
import org.apache.xpath.functions.FuncQname;
import org.apache.xpath.functions.FuncRound;
import org.apache.xpath.functions.FuncStartsWith;
import org.apache.xpath.functions.FuncString;
import org.apache.xpath.functions.FuncStringLength;
import org.apache.xpath.functions.FuncSubstring;
import org.apache.xpath.functions.FuncSubstringAfter;
import org.apache.xpath.functions.FuncSubstringBefore;
import org.apache.xpath.functions.FuncSum;
import org.apache.xpath.functions.FuncSystemProperty;
import org.apache.xpath.functions.FuncTranslate;
import org.apache.xpath.functions.FuncTrue;
import org.apache.xpath.functions.FuncUnparsedEntityURI;
import org.apache.xpath.functions.Function;

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
        m_functions[0] = FuncCurrent.class;
        m_functions[1] = FuncLast.class;
        m_functions[2] = FuncPosition.class;
        m_functions[3] = FuncCount.class;
        m_functions[4] = FuncId.class;
        m_functions[5] = FuncKey.class;
        m_functions[7] = FuncLocalPart.class;
        m_functions[8] = FuncNamespace.class;
        m_functions[9] = FuncQname.class;
        m_functions[10] = FuncGenerateId.class;
        m_functions[11] = FuncNot.class;
        m_functions[12] = FuncTrue.class;
        m_functions[13] = FuncFalse.class;
        m_functions[14] = FuncBoolean.class;
        m_functions[32] = FuncLang.class;
        m_functions[15] = FuncNumber.class;
        m_functions[16] = FuncFloor.class;
        m_functions[17] = FuncCeiling.class;
        m_functions[18] = FuncRound.class;
        m_functions[19] = FuncSum.class;
        m_functions[20] = FuncString.class;
        m_functions[21] = FuncStartsWith.class;
        m_functions[22] = FuncContains.class;
        m_functions[23] = FuncSubstringBefore.class;
        m_functions[24] = FuncSubstringAfter.class;
        m_functions[25] = FuncNormalizeSpace.class;
        m_functions[26] = FuncTranslate.class;
        m_functions[27] = FuncConcat.class;
        m_functions[31] = FuncSystemProperty.class;
        m_functions[33] = FuncExtFunctionAvailable.class;
        m_functions[34] = FuncExtElementAvailable.class;
        m_functions[29] = FuncSubstring.class;
        m_functions[30] = FuncStringLength.class;
        m_functions[35] = FuncDoclocation.class;
        m_functions[36] = FuncUnparsedEntityURI.class;
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
        m_functionID.put(Keywords.FUNC_TRUE_STRING, new Integer(12));
        m_functionID.put(Keywords.FUNC_FALSE_STRING, new Integer(13));
        m_functionID.put(Keywords.FUNC_BOOLEAN_STRING, new Integer(14));
        m_functionID.put("lang", new Integer(32));
        m_functionID.put("number", new Integer(15));
        m_functionID.put(Keywords.FUNC_FLOOR_STRING, new Integer(16));
        m_functionID.put(Keywords.FUNC_CEILING_STRING, new Integer(17));
        m_functionID.put(Keywords.FUNC_ROUND_STRING, new Integer(18));
        m_functionID.put(Keywords.FUNC_SUM_STRING, new Integer(19));
        m_functionID.put(Keywords.FUNC_STRING_STRING, new Integer(20));
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

    String getFunctionName(int funcID) {
        if (funcID < 37) {
            return m_functions[funcID].getName();
        }
        return this.m_functions_customer[funcID - 37].getName();
    }

    Function getFunction(int which) throws TransformerException {
        if (which >= 37) {
            return (Function) this.m_functions_customer[which - 37].newInstance();
        }
        try {
            return (Function) m_functions[which].newInstance();
        } catch (IllegalAccessException ex) {
            throw new TransformerException(ex.getMessage());
        } catch (InstantiationException ex2) {
            throw new TransformerException(ex2.getMessage());
        }
    }

    Object getFunctionID(String key) {
        Object id = this.m_functionID_customer.get(key);
        if (id == null) {
            return m_functionID.get(key);
        }
        return id;
    }

    public int installFunction(String name, Class func) {
        Object funcIndexObj = getFunctionID(name);
        int funcIndex;
        if (funcIndexObj != null) {
            funcIndex = ((Integer) funcIndexObj).intValue();
            if (funcIndex < 37) {
                funcIndex = this.m_funcNextFreeIndex;
                this.m_funcNextFreeIndex = funcIndex + 1;
                this.m_functionID_customer.put(name, new Integer(funcIndex));
            }
            this.m_functions_customer[funcIndex - 37] = func;
            return funcIndex;
        }
        funcIndex = this.m_funcNextFreeIndex;
        this.m_funcNextFreeIndex = funcIndex + 1;
        this.m_functions_customer[funcIndex - 37] = func;
        this.m_functionID_customer.put(name, new Integer(funcIndex));
        return funcIndex;
    }

    public boolean functionAvailable(String methName) {
        boolean z = true;
        if (m_functionID.get(methName) != null) {
            return true;
        }
        if (this.m_functionID_customer.get(methName) == null) {
            z = false;
        }
        return z;
    }
}
