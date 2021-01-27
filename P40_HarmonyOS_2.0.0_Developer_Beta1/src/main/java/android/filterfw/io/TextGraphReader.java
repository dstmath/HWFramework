package android.filterfw.io;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterFactory;
import android.filterfw.core.FilterGraph;
import android.filterfw.core.KeyValueMap;
import android.filterfw.core.ProtocolException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

public class TextGraphReader extends GraphReader {
    private KeyValueMap mBoundReferences;
    private ArrayList<Command> mCommands = new ArrayList<>();
    private Filter mCurrentFilter;
    private FilterGraph mCurrentGraph;
    private FilterFactory mFactory;
    private KeyValueMap mSettings;

    /* access modifiers changed from: private */
    public interface Command {
        void execute(TextGraphReader textGraphReader) throws GraphIOException;
    }

    /* access modifiers changed from: private */
    public class ImportPackageCommand implements Command {
        private String mPackageName;

        public ImportPackageCommand(String packageName) {
            this.mPackageName = packageName;
        }

        @Override // android.filterfw.io.TextGraphReader.Command
        public void execute(TextGraphReader reader) throws GraphIOException {
            try {
                reader.mFactory.addPackage(this.mPackageName);
            } catch (IllegalArgumentException e) {
                throw new GraphIOException(e.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    public class AddLibraryCommand implements Command {
        private String mLibraryName;

        public AddLibraryCommand(String libraryName) {
            this.mLibraryName = libraryName;
        }

        @Override // android.filterfw.io.TextGraphReader.Command
        public void execute(TextGraphReader reader) {
            FilterFactory unused = reader.mFactory;
            FilterFactory.addFilterLibrary(this.mLibraryName);
        }
    }

    /* access modifiers changed from: private */
    public class AllocateFilterCommand implements Command {
        private String mClassName;
        private String mFilterName;

        public AllocateFilterCommand(String className, String filterName) {
            this.mClassName = className;
            this.mFilterName = filterName;
        }

        @Override // android.filterfw.io.TextGraphReader.Command
        public void execute(TextGraphReader reader) throws GraphIOException {
            try {
                reader.mCurrentFilter = reader.mFactory.createFilterByClassName(this.mClassName, this.mFilterName);
            } catch (IllegalArgumentException e) {
                throw new GraphIOException(e.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    public class InitFilterCommand implements Command {
        private KeyValueMap mParams;

        public InitFilterCommand(KeyValueMap params) {
            this.mParams = params;
        }

        @Override // android.filterfw.io.TextGraphReader.Command
        public void execute(TextGraphReader reader) throws GraphIOException {
            try {
                reader.mCurrentFilter.initWithValueMap(this.mParams);
                reader.mCurrentGraph.addFilter(TextGraphReader.this.mCurrentFilter);
            } catch (ProtocolException e) {
                throw new GraphIOException(e.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    public class ConnectCommand implements Command {
        private String mSourceFilter;
        private String mSourcePort;
        private String mTargetFilter;
        private String mTargetName;

        public ConnectCommand(String sourceFilter, String sourcePort, String targetFilter, String targetName) {
            this.mSourceFilter = sourceFilter;
            this.mSourcePort = sourcePort;
            this.mTargetFilter = targetFilter;
            this.mTargetName = targetName;
        }

        @Override // android.filterfw.io.TextGraphReader.Command
        public void execute(TextGraphReader reader) {
            reader.mCurrentGraph.connect(this.mSourceFilter, this.mSourcePort, this.mTargetFilter, this.mTargetName);
        }
    }

    @Override // android.filterfw.io.GraphReader
    public FilterGraph readGraphString(String graphString) throws GraphIOException {
        FilterGraph result = new FilterGraph();
        reset();
        this.mCurrentGraph = result;
        parseString(graphString);
        applySettings();
        executeCommands();
        reset();
        return result;
    }

    private void reset() {
        this.mCurrentGraph = null;
        this.mCurrentFilter = null;
        this.mCommands.clear();
        this.mBoundReferences = new KeyValueMap();
        this.mSettings = new KeyValueMap();
        this.mFactory = new FilterFactory();
    }

    /* JADX INFO: Multiple debug info for r7v5 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r7v6 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r7v7 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r7v8 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r10v9 'wordPattern'  java.util.regex.Pattern: [D('wordPattern' java.util.regex.Pattern), D('curClassName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r7v9 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r10v10 'wordPattern'  java.util.regex.Pattern: [D('wordPattern' java.util.regex.Pattern), D('curClassName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r7v10 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r7v11 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r7v12 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r7v13 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r10v14 'wordPattern'  java.util.regex.Pattern: [D('wordPattern' java.util.regex.Pattern), D('curClassName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r7v14 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r7v15 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r7v16 'scanner'  android.filterfw.io.PatternScanner: [D('commandPattern' java.util.regex.Pattern), D('scanner' android.filterfw.io.PatternScanner)] */
    /* JADX INFO: Multiple debug info for r10v17 'wordPattern'  java.util.regex.Pattern: [D('wordPattern' java.util.regex.Pattern), D('curClassName' java.lang.String)] */
    private void parseString(String graphString) throws GraphIOException {
        String str;
        Pattern semicolonPattern;
        Pattern wordPattern;
        PatternScanner scanner;
        Pattern semicolonPattern2;
        String curClassName;
        Pattern packageNamePattern;
        Pattern libraryNamePattern;
        Pattern commandPattern = Pattern.compile("@[a-zA-Z]+");
        Pattern curlyClosePattern = Pattern.compile("\\}");
        Pattern curlyOpenPattern = Pattern.compile("\\{");
        Pattern ignorePattern = Pattern.compile("(\\s+|//[^\\n]*\\n)+");
        Pattern packageNamePattern2 = Pattern.compile("[a-zA-Z\\.]+");
        Pattern libraryNamePattern2 = Pattern.compile("[a-zA-Z\\./:]+");
        Pattern portPattern = Pattern.compile("\\[[a-zA-Z0-9\\-_]+\\]");
        Pattern rightArrowPattern = Pattern.compile("=>");
        String str2 = ";";
        Pattern commandPattern2 = Pattern.compile(str2);
        Pattern wordPattern2 = Pattern.compile("[a-zA-Z0-9\\-_]+");
        int state = 0;
        PatternScanner scanner2 = new PatternScanner(graphString, ignorePattern);
        String curSourceFilterName = null;
        String curSourcePortName = null;
        String curTargetFilterName = null;
        String curClassName2 = null;
        while (!scanner2.atEnd()) {
            switch (state) {
                case 0:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    packageNamePattern = packageNamePattern2;
                    scanner = scanner2;
                    libraryNamePattern = libraryNamePattern2;
                    wordPattern = wordPattern2;
                    curClassName = curClassName2;
                    semicolonPattern2 = commandPattern;
                    String curCommand = scanner.eat(semicolonPattern2, "<command>");
                    if (curCommand.equals("@import")) {
                        state = 1;
                        break;
                    } else if (curCommand.equals("@library")) {
                        state = 2;
                        break;
                    } else if (curCommand.equals("@filter")) {
                        state = 3;
                        break;
                    } else if (curCommand.equals("@connect")) {
                        state = 8;
                        break;
                    } else if (curCommand.equals("@set")) {
                        state = 13;
                        break;
                    } else if (curCommand.equals("@external")) {
                        state = 14;
                        break;
                    } else if (curCommand.equals("@setting")) {
                        state = 15;
                        break;
                    } else {
                        throw new GraphIOException("Unknown command '" + curCommand + "'!");
                    }
                case 1:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    libraryNamePattern = libraryNamePattern2;
                    wordPattern = wordPattern2;
                    curClassName = curClassName2;
                    packageNamePattern = packageNamePattern2;
                    this.mCommands.add(new ImportPackageCommand(scanner.eat(packageNamePattern, "<package-name>")));
                    state = 16;
                    semicolonPattern2 = commandPattern;
                    break;
                case 2:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    curClassName = curClassName2;
                    libraryNamePattern = libraryNamePattern2;
                    this.mCommands.add(new AddLibraryCommand(scanner.eat(libraryNamePattern, "<library-name>")));
                    state = 16;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    break;
                case 3:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    curClassName = scanner.eat(wordPattern, "<class-name>");
                    state = 4;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    break;
                case 4:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    curClassName = curClassName2;
                    this.mCommands.add(new AllocateFilterCommand(curClassName, scanner.eat(wordPattern, "<filter-name>")));
                    state = 5;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    break;
                case 5:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    scanner.eat(curlyOpenPattern, "{");
                    state = 6;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    curClassName = curClassName2;
                    break;
                case 6:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    this.mCommands.add(new InitFilterCommand(readKeyValueAssignments(scanner, curlyClosePattern)));
                    state = 7;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    curClassName = curClassName2;
                    break;
                case 7:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    scanner.eat(curlyClosePattern, "}");
                    state = 0;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    curClassName = curClassName2;
                    break;
                case 8:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    curSourceFilterName = scanner.eat(wordPattern, "<source-filter-name>");
                    state = 9;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    curClassName = curClassName2;
                    break;
                case 9:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    String portString = scanner.eat(portPattern, "[<source-port-name>]");
                    curSourcePortName = portString.substring(1, portString.length() - 1);
                    state = 10;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    curClassName = curClassName2;
                    break;
                case 10:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    scanner.eat(rightArrowPattern, "=>");
                    state = 11;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    curClassName = curClassName2;
                    break;
                case 11:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    curTargetFilterName = scanner.eat(wordPattern, "<target-filter-name>");
                    state = 12;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    curClassName = curClassName2;
                    break;
                case 12:
                    String portString2 = scanner2.eat(portPattern, "[<target-port-name>]");
                    scanner = scanner2;
                    wordPattern = wordPattern2;
                    semicolonPattern = commandPattern2;
                    str = str2;
                    this.mCommands.add(new ConnectCommand(curSourceFilterName, curSourcePortName, curTargetFilterName, portString2.substring(1, portString2.length() - 1)));
                    state = 16;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    libraryNamePattern = libraryNamePattern2;
                    curClassName = curClassName2;
                    break;
                case 13:
                    this.mBoundReferences.putAll(readKeyValueAssignments(scanner2, commandPattern2));
                    state = 16;
                    semicolonPattern = commandPattern2;
                    str = str2;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    scanner = scanner2;
                    libraryNamePattern = libraryNamePattern2;
                    wordPattern = wordPattern2;
                    curClassName = curClassName2;
                    break;
                case 14:
                    bindExternal(scanner2.eat(wordPattern2, "<external-identifier>"));
                    state = 16;
                    semicolonPattern = commandPattern2;
                    str = str2;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    scanner = scanner2;
                    libraryNamePattern = libraryNamePattern2;
                    wordPattern = wordPattern2;
                    curClassName = curClassName2;
                    break;
                case 15:
                    this.mSettings.putAll(readKeyValueAssignments(scanner2, commandPattern2));
                    state = 16;
                    semicolonPattern = commandPattern2;
                    str = str2;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    scanner = scanner2;
                    libraryNamePattern = libraryNamePattern2;
                    wordPattern = wordPattern2;
                    curClassName = curClassName2;
                    break;
                case 16:
                    scanner2.eat(commandPattern2, str2);
                    state = 0;
                    semicolonPattern = commandPattern2;
                    str = str2;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    scanner = scanner2;
                    libraryNamePattern = libraryNamePattern2;
                    wordPattern = wordPattern2;
                    curClassName = curClassName2;
                    break;
                default:
                    semicolonPattern = commandPattern2;
                    str = str2;
                    semicolonPattern2 = commandPattern;
                    packageNamePattern = packageNamePattern2;
                    scanner = scanner2;
                    libraryNamePattern = libraryNamePattern2;
                    wordPattern = wordPattern2;
                    curClassName = curClassName2;
                    break;
            }
            libraryNamePattern2 = libraryNamePattern;
            packageNamePattern2 = packageNamePattern;
            scanner2 = scanner;
            str2 = str;
            commandPattern = semicolonPattern2;
            commandPattern2 = semicolonPattern;
            curClassName2 = curClassName;
            wordPattern2 = wordPattern;
        }
        if (state != 16 && state != 0) {
            throw new GraphIOException("Unexpected end of input!");
        }
    }

    @Override // android.filterfw.io.GraphReader
    public KeyValueMap readKeyValueAssignments(String assignments) throws GraphIOException {
        return readKeyValueAssignments(new PatternScanner(assignments, Pattern.compile("\\s+")), null);
    }

    private KeyValueMap readKeyValueAssignments(PatternScanner scanner, Pattern endPattern) throws GraphIOException {
        KeyValueMap newVals;
        int STATE_VALUE;
        int STATE_POST_VALUE;
        String str;
        KeyValueMap newVals2;
        Object referencedObject;
        int STATE_EQUALS = 1;
        int STATE_VALUE2 = 2;
        int STATE_POST_VALUE2 = 3;
        Pattern equalsPattern = Pattern.compile("=");
        String str2 = ";";
        Pattern semicolonPattern = Pattern.compile(str2);
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+[a-zA-Z0-9]*");
        Pattern stringPattern = Pattern.compile("'[^']*'|\\\"[^\\\"]*\\\"");
        Pattern intPattern = Pattern.compile("[0-9]+");
        Pattern floatPattern = Pattern.compile("[0-9]*\\.[0-9]+f?");
        Pattern referencePattern = Pattern.compile("\\$[a-zA-Z]+[a-zA-Z0-9]");
        Pattern booleanPattern = Pattern.compile("true|false");
        int state = 0;
        KeyValueMap newVals3 = new KeyValueMap();
        String curKey = null;
        while (true) {
            if (!scanner.atEnd()) {
                if (endPattern != null && scanner.peek(endPattern)) {
                    newVals = newVals3;
                    break;
                }
                if (state == 0) {
                    STATE_VALUE = STATE_VALUE2;
                    STATE_POST_VALUE = STATE_POST_VALUE2;
                    newVals2 = newVals3;
                    str = str2;
                    curKey = scanner.eat(wordPattern, "<identifier>");
                    state = 1;
                } else if (state == 1) {
                    STATE_VALUE = STATE_VALUE2;
                    STATE_POST_VALUE = STATE_POST_VALUE2;
                    newVals2 = newVals3;
                    str = str2;
                    scanner.eat(equalsPattern, "=");
                    state = 2;
                } else if (state == 2) {
                    String curValue = scanner.tryEat(stringPattern);
                    if (curValue != null) {
                        STATE_VALUE = STATE_VALUE2;
                        STATE_POST_VALUE = STATE_POST_VALUE2;
                        newVals3.put(curKey, curValue.substring(1, curValue.length() - 1));
                        newVals2 = newVals3;
                        str = str2;
                    } else {
                        STATE_VALUE = STATE_VALUE2;
                        STATE_POST_VALUE = STATE_POST_VALUE2;
                        newVals2 = newVals3;
                        String curValue2 = scanner.tryEat(referencePattern);
                        if (curValue2 != null) {
                            str = str2;
                            String refName = curValue2.substring(1, curValue2.length());
                            KeyValueMap keyValueMap = this.mBoundReferences;
                            if (keyValueMap != null) {
                                referencedObject = keyValueMap.get(refName);
                            } else {
                                referencedObject = null;
                            }
                            if (referencedObject != null) {
                                newVals2.put(curKey, referencedObject);
                            } else {
                                throw new GraphIOException("Unknown object reference to '" + refName + "'!");
                            }
                        } else {
                            str = str2;
                            String curValue3 = scanner.tryEat(booleanPattern);
                            if (curValue3 != null) {
                                newVals2.put(curKey, Boolean.valueOf(Boolean.parseBoolean(curValue3)));
                            } else {
                                String curValue4 = scanner.tryEat(floatPattern);
                                if (curValue4 != null) {
                                    newVals2.put(curKey, Float.valueOf(Float.parseFloat(curValue4)));
                                } else {
                                    String curValue5 = scanner.tryEat(intPattern);
                                    if (curValue5 != null) {
                                        newVals2.put(curKey, Integer.valueOf(Integer.parseInt(curValue5)));
                                    } else {
                                        throw new GraphIOException(scanner.unexpectedTokenMessage("<value>"));
                                    }
                                }
                            }
                        }
                    }
                    state = 3;
                } else if (state != 3) {
                    STATE_VALUE = STATE_VALUE2;
                    STATE_POST_VALUE = STATE_POST_VALUE2;
                    newVals2 = newVals3;
                    str = str2;
                } else {
                    scanner.eat(semicolonPattern, str2);
                    state = 0;
                    STATE_VALUE = STATE_VALUE2;
                    STATE_POST_VALUE = STATE_POST_VALUE2;
                    newVals2 = newVals3;
                    str = str2;
                }
                str2 = str;
                STATE_POST_VALUE2 = STATE_POST_VALUE;
                STATE_VALUE2 = STATE_VALUE;
                newVals3 = newVals2;
                STATE_EQUALS = STATE_EQUALS;
            } else {
                newVals = newVals3;
                break;
            }
        }
        if (state == 0 || state == 3) {
            return newVals;
        }
        throw new GraphIOException("Unexpected end of assignments on line " + scanner.lineNo() + "!");
    }

    private void bindExternal(String name) throws GraphIOException {
        if (this.mReferences.containsKey(name)) {
            this.mBoundReferences.put(name, this.mReferences.get(name));
            return;
        }
        throw new GraphIOException("Unknown external variable '" + name + "'! You must add a reference to this external in the host program using addReference(...)!");
    }

    private void checkReferences() throws GraphIOException {
        for (String reference : this.mReferences.keySet()) {
            if (!this.mBoundReferences.containsKey(reference)) {
                throw new GraphIOException("Host program specifies reference to '" + reference + "', which is not declared @external in graph file!");
            }
        }
    }

    private void applySettings() throws GraphIOException {
        for (String setting : this.mSettings.keySet()) {
            Object value = this.mSettings.get(setting);
            if (setting.equals("autoBranch")) {
                expectSettingClass(setting, value, String.class);
                if (value.equals("synced")) {
                    this.mCurrentGraph.setAutoBranchMode(1);
                } else if (value.equals("unsynced")) {
                    this.mCurrentGraph.setAutoBranchMode(2);
                } else if (value.equals("off")) {
                    this.mCurrentGraph.setAutoBranchMode(0);
                } else {
                    throw new GraphIOException("Unknown autobranch setting: " + value + "!");
                }
            } else if (setting.equals("discardUnconnectedOutputs")) {
                expectSettingClass(setting, value, Boolean.class);
                this.mCurrentGraph.setDiscardUnconnectedOutputs(((Boolean) value).booleanValue());
            } else {
                throw new GraphIOException("Unknown @setting '" + setting + "'!");
            }
        }
    }

    private void expectSettingClass(String setting, Object value, Class expectedClass) throws GraphIOException {
        if (value.getClass() != expectedClass) {
            throw new GraphIOException("Setting '" + setting + "' must have a value of type " + expectedClass.getSimpleName() + ", but found a value of type " + value.getClass().getSimpleName() + "!");
        }
    }

    private void executeCommands() throws GraphIOException {
        Iterator<Command> it = this.mCommands.iterator();
        while (it.hasNext()) {
            it.next().execute(this);
        }
    }
}
