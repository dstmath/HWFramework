package org.apache.xpath.compiler;

import java.util.HashMap;
import javax.xml.transform.TransformerException;
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
    private static HashMap m_functionID;
    private static Class[] m_functions;
    private int m_funcNextFreeIndex;
    private HashMap m_functionID_customer;
    private Class[] m_functions_customer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.xpath.compiler.FunctionTable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.xpath.compiler.FunctionTable.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.xpath.compiler.FunctionTable.<clinit>():void");
    }

    public FunctionTable() {
        this.m_functions_customer = new Class[NUM_ALLOWABLE_ADDINS];
        this.m_functionID_customer = new HashMap();
        this.m_funcNextFreeIndex = NUM_BUILT_IN_FUNCS;
    }

    String getFunctionName(int funcID) {
        if (funcID < NUM_BUILT_IN_FUNCS) {
            return m_functions[funcID].getName();
        }
        return this.m_functions_customer[funcID - 37].getName();
    }

    Function getFunction(int which) throws TransformerException {
        if (which >= NUM_BUILT_IN_FUNCS) {
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
            if (funcIndex < NUM_BUILT_IN_FUNCS) {
                funcIndex = this.m_funcNextFreeIndex;
                this.m_funcNextFreeIndex = funcIndex + FUNC_LAST;
                this.m_functionID_customer.put(name, new Integer(funcIndex));
            }
            this.m_functions_customer[funcIndex - 37] = func;
            return funcIndex;
        }
        funcIndex = this.m_funcNextFreeIndex;
        this.m_funcNextFreeIndex = funcIndex + FUNC_LAST;
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
