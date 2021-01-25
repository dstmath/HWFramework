package ohos.abilityshell.delegation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import ohos.aafwk.ability.delegation.AbilityDelegatorRegistry;
import ohos.aafwk.ability.delegation.IAbilityDelegatorArgs;
import ohos.aafwk.ability.delegation.TestRunner;
import ohos.appexecfwk.utils.AppLog;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.hiviewdfx.HiLogLabel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JunitTestRunner extends TestRunner {
    private static final String COVERAGE_PATH = "CoveragePath";
    private static final String DATA_DIR = "/data/data/";
    private static final String GENERATE_COVERAGE = "GenerateCoverage";
    private static final String JACOCO_FILE = "jacoco.exec";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "JunitTestRunner");
    private static final String RESULT_DIR = "/files/test/result";
    private static final String RESULT_XML = "testcase_result.xml";
    private static final String XML_PATH = "XmlPath";
    private Class<?> description;
    private Class<?> failure;
    private Class<?> junitCore;
    private File outputBaseDir;
    private Class<?> result;
    private Set<Class<?>> testCases = new LinkedHashSet();

    private boolean getBoolean(String str, boolean z) {
        Object obj = AbilityDelegatorRegistry.getArguments().getTestParameters().get(str);
        return obj instanceof Boolean ? ((Boolean) obj).booleanValue() : z;
    }

    private String getString(String str, String str2) {
        Object obj = AbilityDelegatorRegistry.getArguments().getTestParameters().get(str);
        return obj instanceof String ? (String) obj : str2;
    }

    @Override // ohos.aafwk.ability.delegation.TestRunner
    public boolean prepare() {
        AppLog.i(LABEL, "JunitTestRunner::prepare start", new Object[0]);
        IAbilityDelegatorArgs arguments = AbilityDelegatorRegistry.getArguments();
        String[] split = arguments.getTestCaseNames().split(",");
        for (String str : split) {
            try {
                this.testCases.add(arguments.getTestClassLoader().loadClass(str));
                AppLog.i(LABEL, "Add testcase: %{public}s", str);
            } catch (ClassNotFoundException unused) {
                AbilityDelegator.getInstance().print("[Error] can not find case: " + str + System.lineSeparator());
                AppLog.e(LABEL, "[Error] class %{public}s is undefined!", str);
            }
        }
        try {
            this.junitCore = Class.forName("org.junit.runner.JUnitCore", true, arguments.getTestClassLoader());
            this.result = Class.forName("org.junit.runner.Result", true, arguments.getTestClassLoader());
            this.failure = Class.forName("org.junit.runner.notification.Failure", true, arguments.getTestClassLoader());
            this.description = Class.forName("org.junit.runner.Description", true, arguments.getTestClassLoader());
            this.outputBaseDir = new File(DATA_DIR + arguments.getTestBundleName() + RESULT_DIR);
            return true;
        } catch (ClassNotFoundException e) {
            AbilityDelegator.getInstance().print("[ERROR] Fail to get org.junit.runner.JUnitCore or Result: " + e + System.lineSeparator());
            return false;
        }
    }

    @Override // ohos.aafwk.ability.delegation.TestRunner
    public void run() {
        AppLog.i(LABEL, "JunitTestRunner::run()", new Object[0]);
        ArrayList arrayList = new ArrayList();
        for (Class<?> cls : this.testCases) {
            AbilityDelegator instance = AbilityDelegator.getInstance();
            instance.print("[INFO] start run unittest " + cls.getName() + System.lineSeparator());
            AppLog.i(LABEL, "start run unittest %{public}s", cls.getName());
            arrayList.addAll(transformJunitResult(cls, executeTestCases(cls)));
            AbilityDelegator instance2 = AbilityDelegator.getInstance();
            instance2.print("[INFO] end run unittest " + cls.getName() + System.lineSeparator());
            AppLog.i(LABEL, "end run unittest %{public}s", cls.getName());
        }
        AppLog.i(LABEL, "start write unittest result", new Object[0]);
        writeTestResults(arrayList);
        AppLog.i(LABEL, "end write unittest result", new Object[0]);
        if (getBoolean(GENERATE_COVERAGE, false)) {
            AppLog.i(LABEL, "start generate coverage result", new Object[0]);
            boolean genJacocoExecData = genJacocoExecData();
            AbilityDelegator instance3 = AbilityDelegator.getInstance();
            StringBuilder sb = new StringBuilder();
            sb.append(genJacocoExecData ? "success" : "fail");
            sb.append(" to generate java coverage report");
            sb.append(System.lineSeparator());
            instance3.print(sb.toString());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0055, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005a, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005b, code lost:
        r6.addSuppressed(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005e, code lost:
        throw r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005f, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0060, code lost:
        r1 = ohos.abilityshell.delegation.AbilityDelegator.getInstance();
        r1.print("[ERROR] GenJacocoExecData exception with message:" + r6 + java.lang.System.lineSeparator());
     */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x005f A[ExcHandler: IOException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException (r6v3 'e' java.lang.Object A[CUSTOM_DECLARE]), Splitter:B:6:0x004f] */
    private boolean genJacocoExecData() {
        try {
            File file = new File(getString(COVERAGE_PATH, new File(this.outputBaseDir, JACOCO_FILE).getCanonicalPath()));
            AppLog.i(LABEL, "output coverage result to: %{public}s", file);
            AbilityDelegator instance = AbilityDelegator.getInstance();
            instance.print("output coverage result to: " + file + System.lineSeparator());
            FileOutputStream fileOutputStream = new FileOutputStream(file, false);
            boolean saveJacocoExecData = saveJacocoExecData(fileOutputStream);
            try {
                fileOutputStream.close();
            } catch (IOException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            }
            return saveJacocoExecData;
        } catch (IOException unused) {
            AppLog.e(LABEL, "error path", new Object[0]);
            return false;
        }
    }

    private boolean saveJacocoExecData(OutputStream outputStream) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        if (outputStream == null) {
            AbilityDelegator instance = AbilityDelegator.getInstance();
            instance.print("[ERROR] saveJacocoExecData OutputStream out is null" + System.lineSeparator());
            return false;
        }
        Class<?> cls = Class.forName("org.jacoco.agent.rt.RT");
        if (cls == null) {
            AbilityDelegator instance2 = AbilityDelegator.getInstance();
            instance2.print("[ERROR] load class org.jacoco.agent.rt.RT failed" + System.lineSeparator());
            return false;
        }
        Object invoke = cls.getMethod("getAgent", new Class[0]).invoke(null, new Object[0]);
        if (invoke == null) {
            AbilityDelegator instance3 = AbilityDelegator.getInstance();
            instance3.print("[error] saveJacocoExecData invoke method getAgent() of from org.jacoco.agent.rt.RT failed" + System.lineSeparator());
            return false;
        }
        Object invoke2 = invoke.getClass().getMethod("getExecutionData", Boolean.TYPE).invoke(invoke, false);
        if (invoke2 == null) {
            AbilityDelegator instance4 = AbilityDelegator.getInstance();
            instance4.print("[error] saveJacocoExecData invoke getExecutionData is null" + System.lineSeparator());
            return false;
        } else if (invoke2 instanceof byte[]) {
            outputStream.write((byte[]) invoke2);
            return true;
        } else {
            AbilityDelegator instance5 = AbilityDelegator.getInstance();
            instance5.print("[error] saveJacocoExecData invoke getExecutionData result is not byte[]" + System.lineSeparator());
            return false;
        }
    }

    private void writeTestResults(Collection<TestResult> collection) {
        AppLog.d(LABEL, "JunitTestRunner::writeTestResults()", new Object[0]);
        if (!this.outputBaseDir.exists() && !this.outputBaseDir.mkdirs()) {
            AppLog.e(LABEL, "create folder %{public}s failed!", this.outputBaseDir);
        } else if (!this.outputBaseDir.isDirectory()) {
            AppLog.e(LABEL, "folder %{public}s not exist", this.outputBaseDir);
            AbilityDelegator instance = AbilityDelegator.getInstance();
            instance.print("output testcase result to folder " + this.outputBaseDir + " failed: create folder failed!" + System.lineSeparator());
        } else {
            try {
                File file = new File(getString(XML_PATH, new File(this.outputBaseDir, RESULT_XML).getCanonicalPath()));
                AppLog.i(LABEL, "print testcase result to xml: %{public}s", file);
                DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
                try {
                    newInstance.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                } catch (ParserConfigurationException unused) {
                    AppLog.e(LABEL, "error set feature, ignore", new Object[0]);
                }
                try {
                    Document newDocument = newInstance.newDocumentBuilder().newDocument();
                    newDocument.appendChild(createTestResultElement(newDocument, collection));
                    TransformerFactory newInstance2 = TransformerFactory.newInstance();
                    try {
                        newInstance2.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
                        Transformer newTransformer = newInstance2.newTransformer();
                        newTransformer.setOutputProperty(Constants.ATTRNAME_OUTPUT_INDENT, "yes");
                        newTransformer.transform(new DOMSource(newDocument), new StreamResult(file));
                        AbilityDelegator instance2 = AbilityDelegator.getInstance();
                        instance2.print("output testcase result to file " + file + System.lineSeparator());
                    } catch (TransformerException e) {
                        AppLog.e(LABEL, "write result meet unknown error: %{public}s", e);
                        AbilityDelegator instance3 = AbilityDelegator.getInstance();
                        instance3.print("output testcase result to xml failed: " + e + System.lineSeparator());
                    }
                } catch (ParserConfigurationException e2) {
                    AppLog.e(LABEL, "dump layout info to xml failed: %{public}s", e2);
                }
            } catch (IOException unused2) {
                AppLog.e(LABEL, "error path", new Object[0]);
            }
        }
    }

    private Optional<Object> executeTestCases(Class<?> cls) {
        AppLog.d(LABEL, "JunitTestRunner::executeTestCases()", new Object[0]);
        try {
            return Optional.of(this.junitCore.getMethod("runClasses", Class[].class).invoke(null, new Class[]{cls}));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            AppLog.e(LABEL, "RunClasses failed.", new Object[0]);
            AbilityDelegator instance = AbilityDelegator.getInstance();
            instance.print("[error] Fail to call runClasses method in JUnitCore: " + e + System.lineSeparator());
            return Optional.empty();
        }
    }

    private <T> T convertTo(Object obj, Class<T> cls) {
        return cls.cast(obj);
    }

    private void copy(Class<?> cls, Optional<Object> optional, TestResult testResult) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        testResult.totalTimes = ((Long) convertTo(this.result.getMethod("getRunTime", new Class[0]).invoke(optional.get(), new Object[0]), Long.class)).longValue();
        testResult.totalCounts = ((Integer) convertTo(this.result.getMethod("getRunCount", new Class[0]).invoke(optional.get(), new Object[0]), Integer.class)).intValue();
        testResult.ignoreCounts = ((Integer) convertTo(this.result.getMethod("getIgnoreCount", new Class[0]).invoke(optional.get(), new Object[0]), Integer.class)).intValue();
        testResult.failureCounts = ((Integer) convertTo(this.result.getMethod("getFailureCount", new Class[0]).invoke(optional.get(), new Object[0]), Integer.class)).intValue();
        testResult.wasSuccessful = ((Boolean) convertTo(this.result.getMethod("wasSuccessful", new Class[0]).invoke(optional.get(), new Object[0]), Boolean.class)).booleanValue();
        Field declaredField = this.result.getDeclaredField("startTime");
        AccessController.doPrivileged(new PrivilegedAction(declaredField) {
            /* class ohos.abilityshell.delegation.$$Lambda$JunitTestRunner$YBq0CSsJOiCiQnEILloEDytj1II */
            private final /* synthetic */ Field f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.security.PrivilegedAction
            public final Object run() {
                return JunitTestRunner.lambda$copy$0(this.f$0);
            }
        });
        testResult.startTime = ((AtomicLong) convertTo(declaredField.get(optional.get()), AtomicLong.class)).get();
        List<String> allTestCase = getAllTestCase(cls);
        testResult.testcaseResults = new ArrayList(allTestCase.size());
        ArrayList arrayList = new ArrayList();
        if (testResult.failureCounts > 0) {
            for (Object obj : (List) convertTo(this.result.getMethod("getFailures", new Class[0]).invoke(optional.get(), new Object[0]), List.class)) {
                Object invoke = this.failure.getMethod("getDescription", new Class[0]).invoke(obj, new Object[0]);
                if (invoke != null) {
                    String str = (String) convertTo(this.description.getMethod("getMethodName", new Class[0]).invoke(invoke, new Object[0]), String.class);
                    TestcaseResult testcaseResult = new TestcaseResult(str);
                    testcaseResult.failureMessage = (String) convertTo(this.failure.getMethod("getMessage", new Class[0]).invoke(obj, new Object[0]), String.class);
                    testcaseResult.exception = (Throwable) convertTo(this.failure.getMethod("getException", new Class[0]).invoke(obj, new Object[0]), Throwable.class);
                    testResult.testcaseResults.add(testcaseResult);
                    arrayList.add(str);
                }
            }
        }
        for (String str2 : allTestCase) {
            if (!arrayList.contains(str2)) {
                TestcaseResult testcaseResult2 = new TestcaseResult(str2);
                testcaseResult2.testSuccess = true;
                testResult.testcaseResults.add(testcaseResult2);
            }
        }
    }

    private List<TestResult> transformJunitResult(Class<?> cls, Optional<Object> optional) {
        AppLog.d(LABEL, "JunitTestRunner::transformJunitResult()", new Object[0]);
        ArrayList arrayList = new ArrayList(this.testCases.size());
        TestResult testResult = new TestResult(cls.getName());
        try {
            copy(cls, optional, testResult);
            AbilityDelegator instance = AbilityDelegator.getInstance();
            instance.print("[result] " + testResult.testsuiteName + System.lineSeparator());
            AbilityDelegator instance2 = AbilityDelegator.getInstance();
            instance2.print("[result] Total Times  : " + testResult.totalTimes + System.lineSeparator());
            AbilityDelegator instance3 = AbilityDelegator.getInstance();
            instance3.print("[result] Total Tests  : " + testResult.totalCounts + System.lineSeparator());
            AbilityDelegator instance4 = AbilityDelegator.getInstance();
            instance4.print("[result] Ignore Tests : " + testResult.ignoreCounts + System.lineSeparator());
            AbilityDelegator instance5 = AbilityDelegator.getInstance();
            instance5.print("[result] Fails Tests  : " + testResult.failureCounts + System.lineSeparator());
            if (testResult.failureCounts > 0) {
                for (TestcaseResult testcaseResult : testResult.testcaseResults) {
                    if (!testcaseResult.testSuccess) {
                        AbilityDelegator instance6 = AbilityDelegator.getInstance();
                        instance6.print("[result] [ FAILED ] " + testcaseResult + System.lineSeparator());
                        AppLog.e(LABEL, "%{public}s failed for %{public}s", testcaseResult.testcaseName, testcaseResult.failureMessage);
                        if (testcaseResult.exception != null) {
                            testcaseResult.exception.printStackTrace();
                        }
                    }
                }
            }
            arrayList.add(testResult);
            return arrayList;
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            AbilityDelegator instance7 = AbilityDelegator.getInstance();
            instance7.print("[ERROR] Fail to get testcase result: " + e + System.lineSeparator());
            return arrayList;
        }
    }

    private List<String> getAllTestCase(Class<?> cls) {
        ArrayList arrayList = new ArrayList();
        try {
            Class<? extends U> asSubclass = Class.forName("org.junit.Ignore", true, cls.getClassLoader()).asSubclass(Annotation.class);
            Class<? extends U> asSubclass2 = Class.forName("org.junit.Test", true, cls.getClassLoader()).asSubclass(Annotation.class);
            Method[] declaredMethods = cls.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.isAnnotationPresent(asSubclass2) && !method.isAnnotationPresent(asSubclass)) {
                    arrayList.add(method.getName());
                }
            }
            return arrayList;
        } catch (ClassNotFoundException e) {
            AbilityDelegator.getInstance().print("[error] Fail to get testcase result: " + e + System.lineSeparator());
            return arrayList;
        }
    }

    private Element createTestResultElement(Document document, Collection<TestResult> collection) {
        TestResultsSummary testResultsSummary = new TestResultsSummary();
        for (TestResult testResult : collection) {
            testResultsSummary.totalCounts += testResult.totalCounts;
            testResultsSummary.failureCounts += testResult.failureCounts;
            testResultsSummary.ignoreCounts += testResult.ignoreCounts;
            testResultsSummary.totalTimes += testResult.totalTimes;
            if (testResultsSummary.startTime == 0) {
                testResultsSummary.startTime = testResult.startTime;
            } else {
                testResultsSummary.startTime = Math.min(testResult.startTime, testResultsSummary.startTime);
            }
        }
        Element createElement = document.createElement("testsuites");
        createElement.setAttribute("tests", String.valueOf(testResultsSummary.totalCounts));
        createElement.setAttribute("failures", String.valueOf(testResultsSummary.failureCounts));
        createElement.setAttribute("ignores", String.valueOf(testResultsSummary.ignoreCounts));
        createElement.setAttribute("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(testResultsSummary.startTime)));
        createElement.setAttribute("time", String.valueOf(testResultsSummary.totalTimes));
        createElement.setAttribute("name", AbilityDelegatorRegistry.getArguments().getTestBundleName());
        for (TestResult testResult2 : collection) {
            Element createElement2 = document.createElement("testsuite");
            createElement2.setAttribute("name", String.valueOf(testResult2.testsuiteName));
            createElement2.setAttribute("tests", String.valueOf(testResult2.totalCounts));
            createElement2.setAttribute("failures", String.valueOf(testResult2.failureCounts));
            createElement2.setAttribute("ignores", String.valueOf(testResult2.ignoreCounts));
            createElement2.setAttribute("time", String.valueOf(testResult2.totalTimes));
            createElement2.setAttribute("result", String.valueOf(testResult2.wasSuccessful));
            if (testResult2.testcaseResults == null) {
                AppLog.w(LABEL, "testResult testcaseResults is null, ignore this case.", new Object[0]);
            } else {
                for (TestcaseResult testcaseResult : testResult2.testcaseResults) {
                    Element createElement3 = document.createElement("testcase");
                    createElement3.setAttribute("name", String.valueOf(testcaseResult.testcaseName));
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

    /* access modifiers changed from: private */
    public static class TestResultsSummary {
        int failureCounts;
        int ignoreCounts;
        long startTime;
        int totalCounts;
        long totalTimes;

        private TestResultsSummary() {
            this.totalCounts = 0;
            this.ignoreCounts = 0;
            this.failureCounts = 0;
            this.totalTimes = 0;
            this.startTime = 0;
        }
    }

    /* access modifiers changed from: private */
    public static class TestResult {
        int failureCounts;
        int ignoreCounts;
        long startTime;
        List<TestcaseResult> testcaseResults;
        String testsuiteName;
        int totalCounts;
        long totalTimes;
        boolean wasSuccessful;

        TestResult(String str) {
            this.testsuiteName = str;
        }
    }

    /* access modifiers changed from: private */
    public static class TestcaseResult {
        Throwable exception;
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
