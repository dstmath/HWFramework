package com.android.server.mtm.policy;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MultiTaskPolicyList {
    static boolean DEBUG = false;
    private static String MULTITASK_POLICY_FILEPATH_CUST = null;
    private static String MULTITASK_POLICY_FILEPATH_DEFAULT = null;
    static final String TAG = "MultiTaskPolicyList";
    private static final String TAG_CONDITION = "condition";
    private static final String TAG_POLICY = "policy";
    private static final String TAG_RESOURCE = "resource";
    private static final String XML_ATTR_CONDITIONATTRIBUTE = "conditionattribute";
    private static final String XML_ATTR_CONDITIONCOMBINED = "combinedcondition";
    private static final String XML_ATTR_CONDITIONEXTEND = "conditionextend";
    private static final String XML_ATTR_CONDITIONNAME = "conditionname";
    private static final String XML_ATTR_CONDITIONTYPE = "conditiontype";
    private static final String XML_ATTR_POLICYNAME = "policyname";
    private static final String XML_ATTR_POLICYTYPE = "policytype";
    private static final String XML_ATTR_POLICYVALUE = "policyvalue";
    private static final String XML_ATTR_RESOURCEEXTEND = "resourceextend";
    private static final String XML_ATTR_RESOURCENAME = "resourcename";
    private static final String XML_ATTR_RESOURCESTATUS = "resourcestatus";
    private static final String XML_ATTR_RESOURCESTATUSNAME = "statusname";
    private static final String XML_ATTR_RESOURCETYPE = "resourcetype";
    private static MultiTaskPolicyList instance;
    static final Object mLock = null;
    HashMap<String, MultiTaskResourceConfig> multiTaskPolicy;

    public static class MultiTaskResourceConfig {
        ArrayList<PolicyConfig> policys;
        public String resourceName;
        public int resourceStatus;
        public int resourceType;
        public String resourceextend;
        public String statusname;

        MultiTaskResourceConfig(int _resourcetype, String _resourceName, String _resourceextend, int _resourceStatus, String _statusname) {
            this.resourceType = _resourcetype;
            this.resourceName = _resourceName;
            this.resourceextend = _resourceextend;
            this.resourceStatus = _resourceStatus;
            this.statusname = _statusname;
            if (this.policys == null) {
                this.policys = new ArrayList();
            }
        }

        private String getKeyCode() {
            String rt = Integer.toString(this.resourceType);
            return rt + this.resourceextend + Integer.toString(this.resourceStatus);
        }

        public ArrayList<PolicyConfig> getPolicy() {
            return this.policys;
        }
    }

    public static class PolicyConditionConfig {
        public String combinedcondition;
        public int conditionattribute;
        public String conditionextend;
        public String conditionname;
        public int conditiontype;

        public PolicyConditionConfig(int _conditiontype, String _conditionname, int _conditionattribute, String _conditionextend, String _combinedcondition) {
            this.conditiontype = _conditiontype;
            this.conditionname = _conditionname;
            this.conditionattribute = _conditionattribute;
            this.conditionextend = _conditionextend;
            this.combinedcondition = _combinedcondition;
        }
    }

    public static class PolicyConfig {
        ArrayList<PolicyConditionConfig> conditions;
        public String policyname;
        public int policytype;
        public int policyvalue;
        public boolean policyvalueflag;

        public PolicyConfig(int _policytype, String _policyname, int _policyvalue, boolean _policyvalueflag) {
            this.conditions = null;
            this.policytype = _policytype;
            this.policyname = _policyname;
            this.policyvalue = _policyvalue;
            this.policyvalueflag = _policyvalueflag;
            this.conditions = new ArrayList();
        }

        public ArrayList<PolicyConditionConfig> getPolicycondition() {
            return this.conditions;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.mtm.policy.MultiTaskPolicyList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.mtm.policy.MultiTaskPolicyList.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.mtm.policy.MultiTaskPolicyList.<clinit>():void");
    }

    private MultiTaskPolicyList() {
        this.multiTaskPolicy = new HashMap();
    }

    public static MultiTaskPolicyList getInstance() {
        MultiTaskPolicyList multiTaskPolicyList;
        synchronized (mLock) {
            if (instance == null) {
                instance = new MultiTaskPolicyList();
            }
            multiTaskPolicyList = instance;
        }
        return multiTaskPolicyList;
    }

    public void init(Context context) {
        loadMultiTaskConfig();
    }

    private void loadMultiTaskConfig() {
        XmlPullParserException e;
        IOException e2;
        Throwable th;
        File file = new File(MULTITASK_POLICY_FILEPATH_CUST);
        InputStream inputStream = null;
        XmlPullParser xmlPullParser = null;
        try {
            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = new FileInputStream(new File(MULTITASK_POLICY_FILEPATH_DEFAULT));
            }
            xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(inputStream, null);
            XmlUtils.beginDocument(xmlPullParser, "multitaskconfig");
            PolicyConditionConfig currentCondition = null;
            PolicyConfig currentPolicy = null;
            MultiTaskResourceConfig currentResource = null;
            while (true) {
                PolicyConditionConfig currentCondition2;
                PolicyConfig currentPolicy2;
                MultiTaskResourceConfig currentResource2;
                try {
                    XmlUtils.nextElement(xmlPullParser);
                    String element = xmlPullParser.getName();
                    if (!TAG_RESOURCE.equals(element)) {
                        if (!TAG_POLICY.equals(element)) {
                            if (!TAG_CONDITION.equals(element)) {
                                break;
                            }
                            int ctype;
                            int cvalue;
                            String conditiontype = xmlPullParser.getAttributeValue(null, XML_ATTR_CONDITIONTYPE);
                            String conditionname = xmlPullParser.getAttributeValue(null, XML_ATTR_CONDITIONNAME);
                            String conditionattribute = xmlPullParser.getAttributeValue(null, XML_ATTR_CONDITIONATTRIBUTE);
                            String conditionextend = xmlPullParser.getAttributeValue(null, XML_ATTR_CONDITIONEXTEND);
                            String combinedcondition = xmlPullParser.getAttributeValue(null, XML_ATTR_CONDITIONCOMBINED);
                            try {
                                ctype = Integer.parseInt(conditiontype);
                            } catch (NumberFormatException e3) {
                                ctype = -1;
                            }
                            try {
                                cvalue = Integer.parseInt(conditionattribute);
                            } catch (NumberFormatException e4) {
                                cvalue = -1;
                            }
                            if (DEBUG) {
                                Slog.d(TAG, "conditiontype = " + conditiontype + " conditionname=" + conditionname + " conditionattribute=" + cvalue + " conditionextend=" + conditionextend + " combinedcondition = " + combinedcondition);
                            }
                            currentCondition2 = new PolicyConditionConfig(ctype, conditionname, cvalue, conditionextend, combinedcondition);
                            if (currentPolicy != null) {
                                try {
                                    currentPolicy.conditions.add(currentCondition2);
                                    currentPolicy2 = currentPolicy;
                                    currentResource2 = currentResource;
                                } catch (XmlPullParserException e5) {
                                    e = e5;
                                    currentPolicy2 = currentPolicy;
                                    currentResource2 = currentResource;
                                } catch (IOException e6) {
                                    e2 = e6;
                                    currentPolicy2 = currentPolicy;
                                    currentResource2 = currentResource;
                                } catch (Throwable th2) {
                                    th = th2;
                                    currentPolicy2 = currentPolicy;
                                    currentResource2 = currentResource;
                                }
                            } else {
                                currentPolicy2 = currentPolicy;
                                currentResource2 = currentResource;
                            }
                        } else {
                            int ptype;
                            int pvalue;
                            boolean policyvalueflag = true;
                            String policy = xmlPullParser.getAttributeValue(null, XML_ATTR_POLICYTYPE);
                            String policyname = xmlPullParser.getAttributeValue(null, XML_ATTR_POLICYNAME);
                            String policyvalue = xmlPullParser.getAttributeValue(null, XML_ATTR_POLICYVALUE);
                            if (DEBUG) {
                                Slog.d(TAG, "policy=" + policy + " policyvalue=" + policyvalue + " policyname=" + policyname);
                            }
                            try {
                                ptype = Integer.parseInt(policy);
                            } catch (NumberFormatException e7) {
                                ptype = -1;
                            }
                            try {
                                pvalue = Integer.parseInt(policyvalue);
                            } catch (NumberFormatException e8) {
                                policyvalueflag = false;
                                pvalue = -1;
                            }
                            PolicyConfig policyConfig = new PolicyConfig(ptype, policyname, pvalue, policyvalueflag);
                            if (currentResource != null) {
                                try {
                                    currentResource.policys.add(policyConfig);
                                    currentCondition2 = currentCondition;
                                    currentResource2 = currentResource;
                                } catch (XmlPullParserException e9) {
                                    e = e9;
                                    currentCondition2 = currentCondition;
                                    currentResource2 = currentResource;
                                } catch (IOException e10) {
                                    e2 = e10;
                                    currentCondition2 = currentCondition;
                                    currentResource2 = currentResource;
                                } catch (Throwable th3) {
                                    th = th3;
                                    currentCondition2 = currentCondition;
                                    currentResource2 = currentResource;
                                }
                            } else {
                                currentCondition2 = currentCondition;
                                currentResource2 = currentResource;
                            }
                        }
                    } else {
                        currentResource2 = new MultiTaskResourceConfig(XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_RESOURCETYPE, -1), xmlPullParser.getAttributeValue(null, XML_ATTR_RESOURCENAME), xmlPullParser.getAttributeValue(null, XML_ATTR_RESOURCEEXTEND), XmlUtils.readIntAttribute(xmlPullParser, XML_ATTR_RESOURCESTATUS, -1), xmlPullParser.getAttributeValue(null, XML_ATTR_RESOURCESTATUSNAME));
                        try {
                            this.multiTaskPolicy.put(currentResource2.getKeyCode(), currentResource2);
                            currentCondition2 = currentCondition;
                            currentPolicy2 = currentPolicy;
                        } catch (XmlPullParserException e11) {
                            e = e11;
                            currentCondition2 = currentCondition;
                            currentPolicy2 = currentPolicy;
                        } catch (IOException e12) {
                            e2 = e12;
                            currentCondition2 = currentCondition;
                            currentPolicy2 = currentPolicy;
                        } catch (Throwable th4) {
                            th = th4;
                            currentCondition2 = currentCondition;
                            currentPolicy2 = currentPolicy;
                        }
                    }
                    currentCondition = currentCondition2;
                    currentPolicy = currentPolicy2;
                    currentResource = currentResource2;
                } catch (XmlPullParserException e13) {
                    e = e13;
                    currentCondition2 = currentCondition;
                    currentPolicy2 = currentPolicy;
                    currentResource2 = currentResource;
                } catch (IOException e14) {
                    e2 = e14;
                    currentCondition2 = currentCondition;
                    currentPolicy2 = currentPolicy;
                    currentResource2 = currentResource;
                } catch (Throwable th5) {
                    th = th5;
                }
            }
            if (xmlPullParser instanceof XmlResourceParser) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
        } catch (XmlPullParserException e15) {
            e = e15;
            Slog.e(TAG, "load xml exception  e=" + e);
            if (xmlPullParser instanceof XmlResourceParser) {
                ((XmlResourceParser) xmlPullParser).close();
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e222) {
                    e222.printStackTrace();
                }
            }
        } catch (IOException e16) {
            e222 = e16;
            try {
                Slog.e(TAG, "load xml IO exception e=" + e222);
                if (xmlPullParser instanceof XmlResourceParser) {
                    ((XmlResourceParser) xmlPullParser).close();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                    }
                }
            } catch (Throwable th6) {
                th = th6;
                if (xmlPullParser instanceof XmlResourceParser) {
                    ((XmlResourceParser) xmlPullParser).close();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                throw th;
            }
        }
    }

    public MultiTaskResourceConfig getStaticPolicy(int resourcetype, String resourceextend, int resourcestatus) {
        MultiTaskResourceConfig config = (MultiTaskResourceConfig) this.multiTaskPolicy.get(Integer.toString(resourcetype) + resourceextend + Integer.toString(resourcestatus));
        if (config != null) {
            return config;
        }
        return null;
    }

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void disableDebug() {
        DEBUG = false;
    }

    public void dump(PrintWriter pw) {
        pw.println("**Multitask policy static list dump : ");
        if (this.multiTaskPolicy.size() > 0) {
            for (MultiTaskResourceConfig r : this.multiTaskPolicy.values()) {
                pw.println("<Resource type= " + r.resourceType + " resourcename= " + r.resourceName + " resouceextend=" + r.resourceextend + " Resoucestatus= " + r.resourceStatus + " StatusName=" + r.statusname);
                ArrayList<PolicyConfig> policylist = r.getPolicy();
                if (policylist != null) {
                    for (int i = policylist.size() - 1; i >= 0; i--) {
                        PolicyConfig pconfig = (PolicyConfig) policylist.get(i);
                        if (pconfig != null) {
                            ArrayList<PolicyConditionConfig> conditions = pconfig.getPolicycondition();
                            if (conditions == null || conditions.size() <= 0) {
                                pw.println("    <Policys type=" + pconfig.policytype + " name=" + pconfig.policyname + " value=" + pconfig.policyvalue + " policyvalueflag=" + pconfig.policyvalueflag + " >");
                            } else {
                                pw.println("    <Policys type=" + pconfig.policytype + " name=" + pconfig.policyname + " value=" + pconfig.policyvalue + " policyvalueflag=" + pconfig.policyvalueflag);
                                for (int k = conditions.size() - 1; k >= 0; k--) {
                                    PolicyConditionConfig condition = (PolicyConditionConfig) conditions.get(k);
                                    pw.println("       <conditiontype= " + condition.conditiontype + " conditionname= " + condition.conditionname + " attribute=" + condition.conditionattribute + " conditionextend is " + condition.conditionextend + " combinedcondition is " + condition.combinedcondition + " >");
                                }
                                pw.println("    >");
                            }
                        }
                    }
                }
                pw.println(">");
            }
        }
    }
}
