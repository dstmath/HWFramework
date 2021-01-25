package ohos.aafwk.ability.delegation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.Thread;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.AbilitySliceStack;
import ohos.aafwk.ability.LifecycleException;
import ohos.aafwk.ability.delegation.AbilityDelegationUtils;
import ohos.aafwk.content.Intent;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.bundle.AbilityInfo;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.TouchEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AbilityDelegationImpl extends AbilityDelegation {
    private static final int BACK_KEY_CODE = 2;
    private static final String COVERAGE_PATH = "CoveragePath";
    private static final String DATA_DIR = "/data/data/";
    private static final String DEVICE_ID = "LocalDeviceId";
    private static final String FINISH_TAG = "All test cases run completely.";
    private static final String GENERATE_COVERAGE = "GenerateCoverage";
    private static final String JACOCO_FILE = "jacoco.exec";
    private static final LogLabel LABEL = LogLabel.create();
    private static final int MAX_WAITING_TIME = 5000;
    private static final String NULL_ABILITY_LOG = "ability is null.";
    private static final String RESULT_DIR = "/files/test/result";
    private static final String RESULT_XML = "testcase_result.xml";
    private static final Object SEM_LOCK = new Object();
    private static final String SERVICE_ID = "ServiceId";
    private static final String XML_OUTPUT = "OutputToXml";
    private static final String XML_PATH = "XmlPath";
    private Ability ability = null;
    private String abilityTestCase = "";
    private int abilityToolsId = 0;
    private AbilityToolsProxy abilityToolsProxy = null;
    private String coveragePath;
    private int failureCounts = 0;
    private boolean generateCoverage;
    private int ignoreCounts = 0;
    private Intent intent = null;
    private ClassLoader loader = null;
    private boolean outputToXml;
    private long startTime = 0;
    private String testCaseLibPath = "";
    private boolean testCaseRunning = false;
    private List<Class<?>> testCases = new ArrayList();
    private int totalCounts = 0;
    private long totalTimes = 0;
    private String xmlPath;

    AbilityDelegationImpl() {
        instance(this);
    }

    private static void instance(AbilityDelegationImpl abilityDelegationImpl) {
        instance = abilityDelegationImpl;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x009d: APUT  (r7v3 java.lang.Object[]), (0 ??[int, short, byte, char]), (r2v3 java.lang.String) */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00c4: APUT  (r6v2 java.lang.Object[]), (0 ??[int, short, byte, char]), (r4v4 java.lang.String) */
    /* access modifiers changed from: package-private */
    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void init(Ability ability2, Intent intent2, ClassLoader classLoader) {
        String stringParam;
        Log.info(LABEL, "AbilityDelegation init start.", new Object[0]);
        if (ability2 == null || intent2 == null || classLoader == null) {
            throw new IllegalArgumentException("Ability/Intent/ClassLoader is null.");
        }
        this.ability = ability2;
        this.intent = intent2;
        this.loader = classLoader;
        String stringParam2 = intent2.getStringParam(AbilityDelegation.RUN_TEST);
        if (stringParam2 != null && !stringParam2.isEmpty()) {
            this.abilityTestCase = stringParam2;
        }
        int intParam = intent2.getIntParam(SERVICE_ID, 0);
        if (intParam == 0 && (stringParam = intent2.getStringParam(SERVICE_ID)) != null) {
            try {
                intParam = Integer.parseInt(stringParam);
            } catch (NumberFormatException unused) {
                Log.error(LABEL, "Get service Id failed.", new Object[0]);
                throw new IllegalArgumentException("get serviceid failed");
            }
        }
        Log.info(LABEL, "ServiceId is: %{public}d", Integer.valueOf(intParam));
        String stringParam3 = intent2.getStringParam(DEVICE_ID);
        if (intParam != 0) {
            this.abilityToolsId = intParam;
            this.abilityToolsProxy = new AbilityToolsProxy(this.abilityToolsId, stringParam3);
        }
        String stringParam4 = intent2.getStringParam(AbilityDelegation.LIB_PATH);
        if (stringParam4 != null && !stringParam4.isEmpty()) {
            this.testCaseLibPath = stringParam4;
        }
        this.outputToXml = intent2.getBooleanParam(XML_OUTPUT, true);
        if (this.outputToXml) {
            this.xmlPath = intent2.getStringParam(XML_PATH);
            LogLabel logLabel = LABEL;
            Object[] objArr = new Object[1];
            String str = this.xmlPath;
            if (str == null) {
                str = RESULT_XML;
            }
            objArr[0] = str;
            Log.info(logLabel, "output testcase result to: %{public}s", objArr);
        }
        this.generateCoverage = intent2.getBooleanParam(GENERATE_COVERAGE, false);
        if (this.generateCoverage) {
            this.coveragePath = intent2.getStringParam(COVERAGE_PATH);
            LogLabel logLabel2 = LABEL;
            Object[] objArr2 = new Object[1];
            String str2 = this.coveragePath;
            if (str2 == null) {
                str2 = JACOCO_FILE;
            }
            objArr2[0] = str2;
            Log.info(logLabel2, "output coverage result to: %{public}s", objArr2);
        }
        Log.info(LABEL, "AbilityDelegation init end.", new Object[0]);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void updateAbility(Ability ability2, Intent intent2) {
        if (ability2 == null) {
            Log.info(LABEL, "ability is null", new Object[0]);
            return;
        }
        this.ability = ability2;
        this.intent = intent2;
    }

    private void addTestCase(String str) {
        if (!str.isEmpty() && !str.equals(AbilityTestCase.class.getName())) {
            try {
                Class<?> loadClass = this.loader.loadClass(str);
                if (AbilityTestCase.class.isAssignableFrom(loadClass)) {
                    this.testCases.add(loadClass);
                    Log.info(LABEL, "Add testcase: %{public}s", str);
                    return;
                }
                Log.error(LABEL, "Testcase: %{public}s not extends AbilityTestCase", str);
            } catch (ClassNotFoundException unused) {
                Log.error(LABEL, "[Error] can not find case: %{public}s", str);
            }
        }
    }

    private void prepare() {
        Log.info(LABEL, "AbilityDelegation prepare start.", new Object[0]);
        if (this.testCaseLibPath.isEmpty() || this.abilityToolsId == 0 || this.ability == null) {
            Log.warn(LABEL, "abilityToolsName or ability is illegal.", new Object[0]);
        }
        AbilityToolsProxy abilityToolsProxy2 = this.abilityToolsProxy;
        if (abilityToolsProxy2 != null) {
            abilityToolsProxy2.start();
        }
        if (!this.abilityTestCase.isEmpty()) {
            for (String str : this.abilityTestCase.split(",")) {
                addTestCase(str);
            }
        }
        Log.info(LABEL, "AbilityDelegation prepare end.", new Object[0]);
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void runTestCase() {
        Log.info(LABEL, "AbilityDelegation runTestCase start.", new Object[0]);
        if (this.testCases.isEmpty()) {
            prepare();
            try {
                AbilityDelegatorRegistry.getArguments();
                Log.warn(LABEL, "Already runTestCase in AbilityDelegator.", new Object[0]);
            } catch (IllegalStateException unused) {
                Log.debug(LABEL, "RunTestCase in AbilityDelegation.", new Object[0]);
                if (this.testCases.isEmpty()) {
                    output("[error] No testcase exist: " + this.abilityTestCase);
                    return;
                }
                synchronized (SEM_LOCK) {
                    this.testCaseRunning = true;
                    Thread thread = new Thread(new Runnable() {
                        /* class ohos.aafwk.ability.delegation.AbilityDelegationImpl.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            try {
                                boolean z = true;
                                List executeTestCases = AbilityDelegationImpl.this.executeTestCases(Class.forName("org.junit.runner.JUnitCore", true, AbilityDelegationImpl.this.ability.getClass().getClassLoader()), Class.forName("org.junit.runner.Result", true, AbilityDelegationImpl.this.ability.getClass().getClassLoader()), Class.forName("org.junit.runner.notification.Failure", true, AbilityDelegationImpl.this.ability.getClass().getClassLoader()), Class.forName("org.junit.runner.Description", true, AbilityDelegationImpl.this.ability.getClass().getClassLoader()));
                                if (AbilityDelegationImpl.this.getAbilityInfo() != null) {
                                    File file = new File(AbilityDelegationImpl.DATA_DIR + AbilityDelegationImpl.this.getAbilityInfo().getBundleName() + AbilityDelegationImpl.RESULT_DIR);
                                    if (!file.exists()) {
                                        z = file.mkdirs();
                                    }
                                    if (AbilityDelegationImpl.this.outputToXml && z) {
                                        if (AbilityDelegationImpl.this.xmlPath == null) {
                                            AbilityDelegationImpl abilityDelegationImpl = AbilityDelegationImpl.this;
                                            abilityDelegationImpl.xmlPath = AbilityDelegationImpl.DATA_DIR + AbilityDelegationImpl.this.getAbilityInfo().getBundleName() + AbilityDelegationImpl.RESULT_DIR + PsuedoNames.PSEUDONAME_ROOT + AbilityDelegationImpl.RESULT_XML;
                                        }
                                        AbilityDelegationImpl abilityDelegationImpl2 = AbilityDelegationImpl.this;
                                        boolean outputToXml = abilityDelegationImpl2.outputToXml(executeTestCases, abilityDelegationImpl2.xmlPath);
                                        AbilityDelegationImpl abilityDelegationImpl3 = AbilityDelegationImpl.this;
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(outputToXml ? "success" : "fail");
                                        sb.append(" to generate testcase result report");
                                        abilityDelegationImpl3.output(sb.toString());
                                    }
                                    if (AbilityDelegationImpl.this.generateCoverage && z) {
                                        if (AbilityDelegationImpl.this.coveragePath == null) {
                                            AbilityDelegationImpl abilityDelegationImpl4 = AbilityDelegationImpl.this;
                                            abilityDelegationImpl4.coveragePath = AbilityDelegationImpl.DATA_DIR + AbilityDelegationImpl.this.getAbilityInfo().getBundleName() + AbilityDelegationImpl.RESULT_DIR + PsuedoNames.PSEUDONAME_ROOT + AbilityDelegationImpl.JACOCO_FILE;
                                        }
                                        AbilityDelegationImpl abilityDelegationImpl5 = AbilityDelegationImpl.this;
                                        boolean genJacocoExecData = abilityDelegationImpl5.genJacocoExecData(abilityDelegationImpl5.coveragePath);
                                        AbilityDelegationImpl abilityDelegationImpl6 = AbilityDelegationImpl.this;
                                        StringBuilder sb2 = new StringBuilder();
                                        sb2.append(genJacocoExecData ? "success" : "fail");
                                        sb2.append(" to generate java coverage report");
                                        abilityDelegationImpl6.output(sb2.toString());
                                    }
                                }
                                synchronized (AbilityDelegationImpl.SEM_LOCK) {
                                    AbilityDelegationImpl.this.testCaseRunning = false;
                                    AbilityDelegationImpl.SEM_LOCK.notifyAll();
                                    AbilityDelegationImpl.this.output(AbilityDelegationImpl.FINISH_TAG);
                                    if (AbilityDelegationImpl.this.abilityToolsProxy != null) {
                                        AbilityDelegationImpl.this.abilityToolsProxy.stop();
                                    }
                                }
                            } catch (ClassNotFoundException e) {
                                AbilityDelegationImpl abilityDelegationImpl7 = AbilityDelegationImpl.this;
                                abilityDelegationImpl7.output("[error] Fail to get org.junit.runner.JUnitCore or Result: " + e);
                            }
                        }
                    }, "DelegationRunner");
                    thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        /* class ohos.aafwk.ability.delegation.AbilityDelegationImpl.AnonymousClass2 */

                        @Override // java.lang.Thread.UncaughtExceptionHandler
                        public void uncaughtException(Thread thread, Throwable th) {
                            AbilityDelegationImpl abilityDelegationImpl = AbilityDelegationImpl.this;
                            abilityDelegationImpl.output("[error] testcase run exception: " + th);
                            synchronized (AbilityDelegationImpl.SEM_LOCK) {
                                AbilityDelegationImpl.this.testCaseRunning = false;
                                AbilityDelegationImpl.SEM_LOCK.notifyAll();
                                AbilityDelegationImpl.this.output(AbilityDelegationImpl.FINISH_TAG);
                                if (AbilityDelegationImpl.this.abilityToolsProxy != null) {
                                    AbilityDelegationImpl.this.abilityToolsProxy.stop();
                                }
                            }
                        }
                    });
                    thread.start();
                    Log.info(LABEL, "AbilityDelegation runTestCase end.", new Object[0]);
                }
            }
        } else {
            Log.warn(LABEL, "Already runTestCase completely.", new Object[0]);
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void runOnUIThreadSync(Runnable runnable) {
        Ability ability2 = this.ability;
        if (ability2 == null) {
            Log.error(LABEL, "Ability is NULL, dispatch task to UI thread failed.", new Object[0]);
            return;
        }
        TaskDispatcher uITaskDispatcher = ability2.getUITaskDispatcher();
        if (uITaskDispatcher == null) {
            Log.error(LABEL, "UITaskDispatcher is NULL, dispatch task failed.", new Object[0]);
        } else {
            uITaskDispatcher.syncDispatch(runnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<TestsuiteResult> executeTestCases(Class<?> cls, Class<?> cls2, Class<?> cls3, Class<?> cls4) {
        Class<?> cls5 = cls2;
        ArrayList arrayList = new ArrayList(this.testCases.size());
        for (Class<?> cls6 : this.testCases) {
            try {
                Object invoke = cls.getMethod("runClasses", Class[].class).invoke(null, new Class[]{cls6});
                TestsuiteResult testsuiteResult = new TestsuiteResult(cls6.getName());
                try {
                    testsuiteResult.totalTimes = ((Long) cls5.getMethod("getRunTime", new Class[0]).invoke(invoke, new Object[0])).longValue();
                    testsuiteResult.totalCounts = ((Integer) cls5.getMethod("getRunCount", new Class[0]).invoke(invoke, new Object[0])).intValue();
                    testsuiteResult.ignoreCounts = ((Integer) cls5.getMethod("getIgnoreCount", new Class[0]).invoke(invoke, new Object[0])).intValue();
                    testsuiteResult.failureCounts = ((Integer) cls5.getMethod("getFailureCount", new Class[0]).invoke(invoke, new Object[0])).intValue();
                    testsuiteResult.wasSuccessful = ((Boolean) cls5.getMethod("wasSuccessful", new Class[0]).invoke(invoke, new Object[0])).booleanValue();
                    this.totalTimes += testsuiteResult.totalTimes;
                    this.totalCounts += testsuiteResult.totalCounts;
                    this.ignoreCounts += testsuiteResult.ignoreCounts;
                    this.failureCounts += testsuiteResult.failureCounts;
                    Field declaredField = cls5.getDeclaredField("startTime");
                    AccessController.doPrivileged(new PrivilegedAction(declaredField) {
                        /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationImpl$4Rq5iN4_QL_oc__WUAflWrKVdU */
                        private final /* synthetic */ Field f$0;

                        {
                            this.f$0 = r1;
                        }

                        @Override // java.security.PrivilegedAction
                        public final Object run() {
                            return AbilityDelegationImpl.lambda$executeTestCases$0(this.f$0);
                        }
                    });
                    this.startTime = ((AtomicLong) declaredField.get(invoke)).get();
                    List<String> allTestCase = getAllTestCase(cls6);
                    testsuiteResult.testcaseResults = new ArrayList(allTestCase.size());
                    ArrayList arrayList2 = new ArrayList();
                    if (testsuiteResult.failureCounts > 0) {
                        for (Object obj : (List) cls5.getMethod("getFailures", new Class[0]).invoke(invoke, new Object[0])) {
                            Object invoke2 = cls3.getMethod("getDescription", new Class[0]).invoke(obj, new Object[0]);
                            if (invoke2 != null) {
                                String str = (String) cls4.getMethod("getMethodName", new Class[0]).invoke(invoke2, new Object[0]);
                                TestcaseResult testcaseResult = new TestcaseResult(str);
                                testcaseResult.failureMessage = (String) cls3.getMethod("getMessage", new Class[0]).invoke(obj, new Object[0]);
                                testsuiteResult.testcaseResults.add(testcaseResult);
                                arrayList2.add(str);
                            }
                        }
                    }
                    for (String str2 : allTestCase) {
                        if (!arrayList2.contains(str2)) {
                            TestcaseResult testcaseResult2 = new TestcaseResult(str2);
                            testcaseResult2.testSuccess = true;
                            testsuiteResult.testcaseResults.add(testcaseResult2);
                        }
                    }
                    outputResult(testsuiteResult);
                    arrayList.add(testsuiteResult);
                    cls5 = cls2;
                } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
                    output("[error] Fail to get testcase result: " + e);
                    return arrayList;
                }
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e2) {
                output("[error] Fail to call runClasses method in JUnitCore: " + e2);
            }
        }
        return arrayList;
    }

    private void outputResult(TestsuiteResult testsuiteResult) {
        output("[result] " + testsuiteResult.testsuiteName);
        output("[result] Total Times  : " + testsuiteResult.totalTimes);
        output("[result] Total Tests  : " + testsuiteResult.totalCounts);
        output("[result] Ignore Tests : " + testsuiteResult.ignoreCounts);
        output("[result] Fails Tests  : " + testsuiteResult.failureCounts);
        for (int i = 0; i < testsuiteResult.failureCounts; i++) {
            output("[result] [ FAILED ] " + testsuiteResult.testcaseResults.get(i));
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v3, resolved type: java.lang.reflect.Method[] */
    /* JADX DEBUG: Multi-variable search result rejected for r4v3, resolved type: java.lang.reflect.Method */
    /* JADX WARN: Multi-variable type inference failed */
    private List<String> getAllTestCase(Class<?> cls) {
        ArrayList arrayList = new ArrayList();
        try {
            Class<?> cls2 = Class.forName("org.junit.Ignore", true, this.ability.getClass().getClassLoader());
            Class<?> cls3 = Class.forName("org.junit.Test", true, this.ability.getClass().getClassLoader());
            Method[] declaredMethods = cls.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(cls3) && !method.isAnnotationPresent(cls2)) {
                    arrayList.add(method.getName());
                }
            }
            return arrayList;
        } catch (ClassNotFoundException e) {
            this.output("[error] Fail to get testcase result: " + e);
            return arrayList;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean outputToXml(List<TestsuiteResult> list, String str) {
        Log.error(LABEL, "print testcase result to xml: %{public}s", str);
        DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
        try {
            newInstance.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException unused) {
            Log.error(LABEL, "error set feature, ignore", new Object[0]);
        }
        try {
            Document newDocument = newInstance.newDocumentBuilder().newDocument();
            newDocument.appendChild(createTestResultElement(newDocument, list));
            TransformerFactory newInstance2 = TransformerFactory.newInstance();
            try {
                newInstance2.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
                Transformer newTransformer = newInstance2.newTransformer();
                newTransformer.setOutputProperty(Constants.ATTRNAME_OUTPUT_INDENT, "yes");
                newTransformer.transform(new DOMSource(newDocument), new StreamResult(new File(str)));
                return true;
            } catch (TransformerException e) {
                output("output testcase result to xml failed: " + e);
                return false;
            }
        } catch (ParserConfigurationException e2) {
            Log.error(LABEL, "dump layout info to xml failed: %{public}s", e2);
            return false;
        }
    }

    private Element createTestResultElement(Document document, List<TestsuiteResult> list) {
        Element createElement = document.createElement("testsuites");
        createElement.setAttribute("tests", String.valueOf(this.totalCounts));
        createElement.setAttribute("failures", String.valueOf(this.failureCounts));
        createElement.setAttribute("ignores", String.valueOf(this.ignoreCounts));
        createElement.setAttribute("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(this.startTime)));
        createElement.setAttribute("time", String.valueOf(this.totalTimes));
        if (getAbilityInfo() != null) {
            createElement.setAttribute("name", getAbilityInfo().getBundleName());
        } else {
            createElement.setAttribute("name", "null");
        }
        for (TestsuiteResult testsuiteResult : list) {
            Element createElement2 = document.createElement("testsuite");
            createElement2.setAttribute("name", testsuiteResult.testsuiteName);
            createElement2.setAttribute("tests", String.valueOf(testsuiteResult.totalCounts));
            createElement2.setAttribute("failures", String.valueOf(testsuiteResult.failureCounts));
            createElement2.setAttribute("ignores", String.valueOf(testsuiteResult.ignoreCounts));
            createElement2.setAttribute("time", String.valueOf(testsuiteResult.totalTimes));
            createElement2.setAttribute("result", String.valueOf(testsuiteResult.wasSuccessful));
            if (testsuiteResult.testcaseResults == null) {
                Log.warn(LABEL, "testsuiteResult testcaseResults is null, ignore this case.", new Object[0]);
            } else {
                for (TestcaseResult testcaseResult : testsuiteResult.testcaseResults) {
                    Element createElement3 = document.createElement("testcase");
                    createElement3.setAttribute("name", testcaseResult.testcaseName);
                    createElement3.setAttribute("time", String.valueOf(testcaseResult.totalTimes));
                    createElement3.setAttribute("result", String.valueOf(testcaseResult.testSuccess));
                    if (!testcaseResult.testSuccess) {
                        Element createElement4 = document.createElement("failure");
                        createElement4.setAttribute("message", testcaseResult.failureMessage);
                        createElement3.appendChild(createElement4);
                    }
                    createElement2.appendChild(createElement3);
                }
                createElement.appendChild(createElement2);
            }
        }
        return createElement;
    }

    /* access modifiers changed from: package-private */
    public boolean waitRunEnd() {
        synchronized (SEM_LOCK) {
            Log.info(LABEL, "AbilityDelegation waitRunEnd start.", new Object[0]);
            do {
                try {
                    if (this.testCaseRunning) {
                        SEM_LOCK.wait(5000);
                    } else {
                        Log.info(LABEL, "AbilityDelegation waitRunEnd end.", new Object[0]);
                        return true;
                    }
                } catch (InterruptedException e) {
                    Log.error(LABEL, "AbilityDelegation waitRunEnd failed. %{public}s", e);
                    return false;
                }
            } while (System.currentTimeMillis() <= System.currentTimeMillis() + 5000);
            Log.error(LABEL, "AbilityDelegation waitRunEnd timeout.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0015, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0016, code lost:
        r4.addSuppressed(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0019, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001a, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x001b, code lost:
        output("[error] GenJacocoExecData exception with message:" + r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0010, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x001a A[ExcHandler: IOException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException (r4v2 'e' java.lang.Object A[CUSTOM_DECLARE]), Splitter:B:3:0x000a] */
    private boolean genJacocoExecData(String str) {
        FileOutputStream fileOutputStream = new FileOutputStream(str, false);
        boolean saveJacocoExecData = saveJacocoExecData(fileOutputStream);
        try {
            fileOutputStream.close();
        } catch (IOException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
        }
        return saveJacocoExecData;
    }

    private boolean saveJacocoExecData(OutputStream outputStream) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        if (outputStream == null) {
            output("[error] saveJacocoExecData OutputStream out is null");
            return false;
        }
        Class<?> cls = Class.forName("org.jacoco.agent.rt.RT");
        if (cls == null) {
            output("[error] load class org.jacoco.agent.rt.RT failed");
            return false;
        }
        Object invoke = cls.getMethod("getAgent", new Class[0]).invoke(null, new Object[0]);
        if (invoke == null) {
            output("[error] saveJacocoExecData invoke method getAgent() of from org.jacoco.agent.rt.RT failed");
            return false;
        }
        Object invoke2 = invoke.getClass().getMethod("getExecutionData", Boolean.TYPE).invoke(invoke, false);
        if (invoke2 == null) {
            output("[error] saveJacocoExecData invoke getExecutionData is null");
            return false;
        } else if (invoke2 instanceof byte[]) {
            outputStream.write((byte[]) invoke2);
            return true;
        } else {
            output("[error] saveJacocoExecData invoke getExecutionData result is not byte[]");
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public Intent getIntent() {
        return this.intent;
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void output(String str) {
        Log.info(LABEL, "[INFO] %{public}s\n", str);
        AbilityToolsProxy abilityToolsProxy2 = this.abilityToolsProxy;
        if (abilityToolsProxy2 != null) {
            abilityToolsProxy2.output(str);
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public boolean triggerClickEvent(Component component) {
        if (this.ability == null) {
            Log.error(LABEL, "Ability is NULL, triggerClickEvent failed.", new Object[0]);
            return false;
        } else if (component instanceof Button) {
            component.performClick();
            return true;
        } else if (!(component instanceof Image)) {
            return false;
        } else {
            component.performClick();
            return true;
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public boolean triggerKeyEvent(KeyEvent keyEvent) {
        if (this.ability == null) {
            Log.error(LABEL, "Ability is NULL, triggerKeyEvent failed.", new Object[0]);
            return false;
        } else if (keyEvent.getKeyCode() != 2) {
            return AbilityDelegationUtils.reflectDispatchKeyEvent(this.ability, keyEvent);
        } else {
            AbilityDelegationUtils.reflectDispatchBackKey(this.ability);
            return true;
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public boolean triggerTouchEvent(TouchEvent touchEvent) {
        Ability ability2 = this.ability;
        if (ability2 == null) {
            Log.error(LABEL, "Ability is NULL, triggerKeyEvent failed.", new Object[0]);
            return false;
        } else if (touchEvent != null) {
            return AbilityDelegationUtils.reflectDispatchTouchEvent(ability2, touchEvent);
        } else {
            Log.error(LABEL, "null touch event, ignore", new Object[0]);
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public AbilitySlice getCurrentAbilitySlice() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            AbilitySlice reflectTopAbilitySlice = AbilityDelegationUtils.reflectTopAbilitySlice(ability2);
            if (reflectTopAbilitySlice != null) {
                return reflectTopAbilitySlice;
            }
            throw new IllegalArgumentException("topAbilitySliceImpl cannot be null");
        }
        throw new IllegalArgumentException("ability cannot be null");
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public List<AbilitySlice> getAbilitySlice(String str) {
        if (str != null) {
            List<AbilitySlice> allAbilitySlice = getAllAbilitySlice();
            allAbilitySlice.removeIf(new Predicate(str) {
                /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationImpl$EJQnOpaDTnberxWxtSOOpI5vvn8 */
                private final /* synthetic */ String f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return AbilityDelegationImpl.lambda$getAbilitySlice$1(this.f$0, (AbilitySlice) obj);
                }
            });
            return allAbilitySlice;
        }
        throw new IllegalArgumentException("slice name cannot be null");
    }

    static /* synthetic */ boolean lambda$getAbilitySlice$1(String str, AbilitySlice abilitySlice) {
        return !abilitySlice.getClass().getName().equals(str);
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public List<AbilitySlice> getAllAbilitySlice() {
        AbilitySlice reflectTopAbilitySlice = AbilityDelegationUtils.reflectTopAbilitySlice(this.ability);
        AbilitySliceStack reflectAbilitySliceStack = AbilityDelegationUtils.reflectAbilitySliceStack(this.ability);
        ArrayList arrayList = new ArrayList();
        if (reflectTopAbilitySlice != null) {
            arrayList.add(reflectTopAbilitySlice);
        }
        arrayList.addAll(AbilityDelegationUtils.reflectGetAllSlices(reflectAbilitySliceStack));
        return arrayList;
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public int getAbilitySliceState(AbilitySlice abilitySlice) {
        if (abilitySlice != null) {
            return AbilityDelegationUtils.reflectGetSliceState(abilitySlice);
        }
        throw new IllegalArgumentException("slice cannot be null");
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilitySliceStart(Intent intent2) {
        AbilitySlice reflectTopAbilitySlice = AbilityDelegationUtils.reflectTopAbilitySlice(this.ability);
        if (reflectTopAbilitySlice == null) {
            throw new IllegalStateException("topAbilitySlice is null, cannot do start");
        } else if (AbilityDelegationUtils.reflectGetSliceState(reflectTopAbilitySlice) == 0) {
            AbilityDelegationUtils.reflectSliceLifecycle(reflectTopAbilitySlice, intent2, AbilityDelegationUtils.LifecycleAction.START);
        } else {
            throw new LifecycleException("topAbilitySliceImpl state is not INITIAL, cannot do start");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilitySliceActive() {
        AbilitySlice reflectTopAbilitySlice = AbilityDelegationUtils.reflectTopAbilitySlice(this.ability);
        if (reflectTopAbilitySlice == null) {
            throw new IllegalStateException("topAbilitySliceImpl is null, cannot do active");
        } else if (AbilityDelegationUtils.reflectGetSliceState(reflectTopAbilitySlice) == 1) {
            AbilityDelegationUtils.reflectSliceLifecycle(reflectTopAbilitySlice, this.intent, AbilityDelegationUtils.LifecycleAction.ACTIVE);
        } else {
            throw new LifecycleException("topAbilitySliceImpl state is not INACTIVE, cannot do active");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilitySliceInactive() {
        AbilitySlice reflectTopAbilitySlice = AbilityDelegationUtils.reflectTopAbilitySlice(this.ability);
        if (reflectTopAbilitySlice == null) {
            throw new IllegalStateException("topAbilitySliceImpl is null, cannot do inactive");
        } else if (AbilityDelegationUtils.reflectGetSliceState(reflectTopAbilitySlice) == 2) {
            AbilityDelegationUtils.reflectSliceLifecycle(reflectTopAbilitySlice, this.intent, AbilityDelegationUtils.LifecycleAction.INACTIVE);
        } else {
            throw new LifecycleException("topAbilitySliceImpl state is not ACTIVE, cannot do inactive");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilitySliceBackground() {
        AbilitySlice reflectTopAbilitySlice = AbilityDelegationUtils.reflectTopAbilitySlice(this.ability);
        if (reflectTopAbilitySlice == null) {
            throw new IllegalStateException("topAbilitySliceImpl is null, cannot do background");
        } else if (AbilityDelegationUtils.reflectGetSliceState(reflectTopAbilitySlice) == 1) {
            AbilityDelegationUtils.reflectSliceLifecycle(reflectTopAbilitySlice, this.intent, AbilityDelegationUtils.LifecycleAction.BACKGROUND);
        } else {
            throw new LifecycleException("topAbilitySliceImpl state is not INACTIVE, cannot do background");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilitySliceForeground(Intent intent2) {
        AbilitySlice reflectTopAbilitySlice = AbilityDelegationUtils.reflectTopAbilitySlice(this.ability);
        if (reflectTopAbilitySlice == null) {
            throw new IllegalStateException("topAbilitySliceImpl is null, cannot do foreground");
        } else if (AbilityDelegationUtils.reflectGetSliceState(reflectTopAbilitySlice) == 3) {
            AbilityDelegationUtils.reflectSliceLifecycle(reflectTopAbilitySlice, intent2, AbilityDelegationUtils.LifecycleAction.FOREGROUND);
        } else {
            throw new LifecycleException("topAbilitySliceImpl state is not BACKGROUND, cannot do foreground");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilitySliceStop() {
        AbilitySlice reflectTopAbilitySlice = AbilityDelegationUtils.reflectTopAbilitySlice(this.ability);
        if (reflectTopAbilitySlice == null) {
            throw new IllegalStateException("topAbilitySliceImpl is null, cannot do stop");
        } else if (AbilityDelegationUtils.reflectGetSliceState(reflectTopAbilitySlice) == 3) {
            AbilityDelegationUtils.reflectSliceLifecycle(reflectTopAbilitySlice, this.intent, AbilityDelegationUtils.LifecycleAction.STOP);
        } else {
            throw new LifecycleException("topAbilitySliceImpl state is not BACKGROUND, cannot do stop");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public Ability getAbility() {
        return this.ability;
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public int getAbilityState() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return AbilityDelegationUtils.reflectGetAbilityState(ability2);
        }
        throw new IllegalArgumentException("the ability is null.");
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public AbilityInfo getAbilityInfo() {
        if (this.ability != null) {
            try {
                Field declaredField = Ability.class.getDeclaredField("abilityInfo");
                AccessController.doPrivileged(new PrivilegedAction(declaredField) {
                    /* class ohos.aafwk.ability.delegation.$$Lambda$AbilityDelegationImpl$d3z2mAHRF6uiJR_GUT9X2qDkIm4 */
                    private final /* synthetic */ Field f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.security.PrivilegedAction
                    public final Object run() {
                        return AbilityDelegationImpl.lambda$getAbilityInfo$2(this.f$0);
                    }
                });
                Object obj = declaredField.get(this.ability);
                if (obj instanceof AbilityInfo) {
                    return (AbilityInfo) obj;
                }
            } catch (IllegalAccessException | NoSuchFieldException unused) {
            }
            return null;
        }
        throw new IllegalArgumentException(NULL_ABILITY_LOG);
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilityStart(Intent intent2) {
        if (this.ability == null) {
            throw new IllegalStateException(NULL_ABILITY_LOG);
        } else if (intent2 == null) {
            throw new IllegalArgumentException("intent is null");
        } else if (getAbilityState() == 0) {
            AbilityDelegationUtils.reflectAbilityLifecycle(this.ability, intent2, AbilityDelegationUtils.LifecycleAction.START);
        } else {
            throw new LifecycleException("the lifecycle of current state is not initial.");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilityActive() {
        if (this.ability == null) {
            throw new IllegalStateException(NULL_ABILITY_LOG);
        } else if (getAbilityState() == 1) {
            AbilityDelegationUtils.reflectAbilityLifecycle(this.ability, this.intent, AbilityDelegationUtils.LifecycleAction.ACTIVE);
        } else {
            throw new LifecycleException("the lifecycle state of current ability is not inactive.");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilityInactive() {
        if (this.ability == null) {
            throw new IllegalStateException(NULL_ABILITY_LOG);
        } else if (getAbilityState() == 2) {
            AbilityDelegationUtils.reflectAbilityLifecycle(this.ability, this.intent, AbilityDelegationUtils.LifecycleAction.INACTIVE);
        } else {
            throw new LifecycleException("the lifecycle state of current ability is not active.");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilityForeground(Intent intent2) {
        if (this.ability == null) {
            throw new IllegalStateException(NULL_ABILITY_LOG);
        } else if (intent2 == null) {
            throw new IllegalArgumentException("intent is null");
        } else if (getAbilityState() == 3) {
            AbilityDelegationUtils.reflectAbilityLifecycle(this.ability, intent2, AbilityDelegationUtils.LifecycleAction.FOREGROUND);
        } else {
            throw new LifecycleException("the lifecycle state of current ability is not background.");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilityBackground() {
        if (this.ability == null) {
            throw new IllegalStateException(NULL_ABILITY_LOG);
        } else if (getAbilityState() == 1) {
            AbilityDelegationUtils.reflectAbilityLifecycle(this.ability, this.intent, AbilityDelegationUtils.LifecycleAction.BACKGROUND);
        } else {
            throw new LifecycleException("the lifecycle state of current ability is not inactive.");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public void doAbilityStop() {
        if (this.ability == null) {
            throw new IllegalStateException(NULL_ABILITY_LOG);
        } else if (getAbilityState() == 3) {
            AbilityDelegationUtils.reflectAbilityLifecycle(this.ability, this.intent, AbilityDelegationUtils.LifecycleAction.STOP);
        } else {
            throw new LifecycleException("the lifecycle of current ability is not background.");
        }
    }

    @Override // ohos.aafwk.ability.delegation.AbilityDelegation
    public Ability getCurrentTopAbility() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            Object reflectGetTopAbility = AbilityDelegationUtils.reflectGetTopAbility(ability2);
            if (reflectGetTopAbility instanceof Ability) {
                return (Ability) reflectGetTopAbility;
            }
            return null;
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: private */
    public static class TestsuiteResult {
        int failureCounts;
        int ignoreCounts;
        List<TestcaseResult> testcaseResults;
        String testsuiteName;
        int totalCounts;
        long totalTimes;
        boolean wasSuccessful;

        TestsuiteResult(String str) {
            this.testsuiteName = str;
        }
    }

    /* access modifiers changed from: private */
    public static class TestcaseResult {
        String failureMessage;
        boolean testSuccess = false;
        String testcaseName;
        long totalTimes = 0;

        TestcaseResult(String str) {
            this.testcaseName = str;
        }

        public String toString() {
            return this.testcaseName + " : " + this.failureMessage;
        }
    }
}
