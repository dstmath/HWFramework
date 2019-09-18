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
    /* access modifiers changed from: private */
    public Filter mCurrentFilter;
    /* access modifiers changed from: private */
    public FilterGraph mCurrentGraph;
    /* access modifiers changed from: private */
    public FilterFactory mFactory;
    private KeyValueMap mSettings;

    private class AddLibraryCommand implements Command {
        private String mLibraryName;

        public AddLibraryCommand(String libraryName) {
            this.mLibraryName = libraryName;
        }

        public void execute(TextGraphReader reader) {
            FilterFactory unused = reader.mFactory;
            FilterFactory.addFilterLibrary(this.mLibraryName);
        }
    }

    private class AllocateFilterCommand implements Command {
        private String mClassName;
        private String mFilterName;

        public AllocateFilterCommand(String className, String filterName) {
            this.mClassName = className;
            this.mFilterName = filterName;
        }

        public void execute(TextGraphReader reader) throws GraphIOException {
            try {
                Filter unused = reader.mCurrentFilter = reader.mFactory.createFilterByClassName(this.mClassName, this.mFilterName);
            } catch (IllegalArgumentException e) {
                throw new GraphIOException(e.getMessage());
            }
        }
    }

    private interface Command {
        void execute(TextGraphReader textGraphReader) throws GraphIOException;
    }

    private class ConnectCommand implements Command {
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

        public void execute(TextGraphReader reader) {
            reader.mCurrentGraph.connect(this.mSourceFilter, this.mSourcePort, this.mTargetFilter, this.mTargetName);
        }
    }

    private class ImportPackageCommand implements Command {
        private String mPackageName;

        public ImportPackageCommand(String packageName) {
            this.mPackageName = packageName;
        }

        public void execute(TextGraphReader reader) throws GraphIOException {
            try {
                reader.mFactory.addPackage(this.mPackageName);
            } catch (IllegalArgumentException e) {
                throw new GraphIOException(e.getMessage());
            }
        }
    }

    private class InitFilterCommand implements Command {
        private KeyValueMap mParams;

        public InitFilterCommand(KeyValueMap params) {
            this.mParams = params;
        }

        public void execute(TextGraphReader reader) throws GraphIOException {
            try {
                reader.mCurrentFilter.initWithValueMap(this.mParams);
                reader.mCurrentGraph.addFilter(TextGraphReader.this.mCurrentFilter);
            } catch (ProtocolException e) {
                throw new GraphIOException(e.getMessage());
            }
        }
    }

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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x00d2, code lost:
        r0 = r1;
        r2 = r7;
        r41 = r10;
        r1 = r11;
        r42 = r15;
        r35 = r37;
        r10 = r38;
        r7 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x00de, code lost:
        r11 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0223, code lost:
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0224, code lost:
        r35 = r37;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0226, code lost:
        r2 = r44;
        r1 = r45;
     */
    private void parseString(String graphString) throws GraphIOException {
        Pattern semicolonPattern;
        Pattern ignorePattern;
        Pattern packageNamePattern;
        String curClassName;
        PatternScanner scanner;
        Pattern commandPattern;
        Pattern packageNamePattern2;
        int state;
        Pattern packageNamePattern3;
        Pattern commandPattern2;
        Pattern semicolonPattern2;
        Pattern ignorePattern2;
        String curClassName2;
        PatternScanner scanner2;
        int state2;
        int state3;
        String curClassName3;
        int state4;
        Pattern commandPattern3 = Pattern.compile("@[a-zA-Z]+");
        Pattern curlyClosePattern = Pattern.compile("\\}");
        Pattern curlyOpenPattern = Pattern.compile("\\{");
        Pattern ignorePattern3 = Pattern.compile("(\\s+|//[^\\n]*\\n)+");
        Pattern wordPattern = Pattern.compile("[a-zA-Z\\.]+");
        Pattern libraryNamePattern = Pattern.compile("[a-zA-Z\\./:]+");
        Pattern portPattern = Pattern.compile("\\[[a-zA-Z0-9\\-_]+\\]");
        Pattern rightArrowPattern = Pattern.compile("=>");
        Pattern semicolonPattern3 = Pattern.compile(";");
        Pattern wordPattern2 = Pattern.compile("[a-zA-Z0-9\\-_]+");
        int state5 = 0;
        PatternScanner scanner3 = new PatternScanner(graphString, ignorePattern3);
        String curClassName4 = null;
        String curTargetPortName = null;
        String curTargetFilterName = null;
        String curSourcePortName = null;
        String curSourceFilterName = null;
        while (true) {
            int state6 = state5;
            String curTargetPortName2 = curTargetPortName;
            if (!scanner3.atEnd()) {
                String curTargetPortName3 = curTargetPortName2;
                switch (state6) {
                    case 0:
                        int i = state6;
                        Pattern commandPattern4 = commandPattern3;
                        ignorePattern = ignorePattern3;
                        semicolonPattern = semicolonPattern3;
                        curClassName = curClassName4;
                        scanner = scanner3;
                        packageNamePattern2 = wordPattern;
                        packageNamePattern = wordPattern2;
                        commandPattern = commandPattern4;
                        String curCommand = scanner.eat(commandPattern, "<command>");
                        if (curCommand.equals("@import")) {
                            state = 1;
                        } else if (curCommand.equals("@library")) {
                            state = 2;
                        } else if (curCommand.equals("@filter")) {
                            state = 3;
                        } else if (curCommand.equals("@connect")) {
                            state = 8;
                        } else if (curCommand.equals("@set")) {
                            state = 13;
                        } else if (curCommand.equals("@external")) {
                            state = 14;
                        } else if (curCommand.equals("@setting")) {
                            state = 15;
                        } else {
                            throw new GraphIOException("Unknown command '" + curCommand + "'!");
                        }
                        state5 = state;
                        curTargetPortName = curTargetPortName3;
                        break;
                    case 1:
                        int i2 = state6;
                        Pattern commandPattern5 = commandPattern3;
                        ignorePattern = ignorePattern3;
                        Pattern packageNamePattern4 = wordPattern;
                        semicolonPattern = semicolonPattern3;
                        curClassName = curClassName4;
                        scanner = scanner3;
                        packageNamePattern = wordPattern2;
                        packageNamePattern2 = packageNamePattern4;
                        this.mCommands.add(new ImportPackageCommand(scanner.eat(packageNamePattern2, "<package-name>")));
                        state5 = 16;
                        curTargetPortName = curTargetPortName3;
                        commandPattern = commandPattern5;
                        break;
                    case 2:
                        int i3 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        curClassName2 = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        this.mCommands.add(new AddLibraryCommand(scanner2.eat(libraryNamePattern, "<library-name>")));
                        state3 = 16;
                        break;
                    case 3:
                        int i4 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        String str = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        state3 = 4;
                        curClassName2 = scanner2.eat(packageNamePattern, "<class-name>");
                        break;
                    case 4:
                        int i5 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        curClassName2 = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        this.mCommands.add(new AllocateFilterCommand(curClassName2, scanner2.eat(packageNamePattern, "<filter-name>")));
                        state3 = 5;
                        break;
                    case 5:
                        int i6 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        curClassName2 = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        scanner2.eat(curlyOpenPattern, "{");
                        state2 = 6;
                        break;
                    case 6:
                        int i7 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        curClassName2 = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        this.mCommands.add(new InitFilterCommand(readKeyValueAssignments(scanner2, curlyClosePattern)));
                        state3 = 7;
                        break;
                    case 7:
                        int i8 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        curClassName2 = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        scanner2.eat(curlyClosePattern, "}");
                        state2 = 0;
                        break;
                    case 8:
                        int i9 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        curClassName2 = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        curSourceFilterName = scanner2.eat(packageNamePattern, "<source-filter-name>");
                        state2 = 9;
                        break;
                    case 9:
                        int i10 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        curClassName2 = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        String portString = scanner2.eat(portPattern, "[<source-port-name>]");
                        curSourcePortName = portString.substring(1, portString.length() - 1);
                        state2 = 10;
                        break;
                    case 10:
                        int i11 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        curClassName2 = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        scanner2.eat(rightArrowPattern, "=>");
                        state2 = 11;
                        break;
                    case 11:
                        int i12 = state6;
                        commandPattern2 = commandPattern3;
                        ignorePattern2 = ignorePattern3;
                        packageNamePattern3 = wordPattern;
                        semicolonPattern2 = semicolonPattern3;
                        curClassName2 = curClassName4;
                        scanner2 = scanner3;
                        packageNamePattern = wordPattern2;
                        curTargetFilterName = scanner2.eat(packageNamePattern, "<target-filter-name>");
                        state2 = 12;
                        break;
                    case 12:
                        String curClassName5 = curClassName4;
                        String portString2 = scanner3.eat(portPattern, "[<target-port-name>]");
                        String curTargetPortName4 = portString2.substring(1, portString2.length() - 1);
                        ConnectCommand connectCommand = r0;
                        String str2 = portString2;
                        ignorePattern2 = ignorePattern3;
                        curClassName2 = curClassName5;
                        semicolonPattern2 = semicolonPattern3;
                        int i13 = state6;
                        ArrayList<Command> arrayList = this.mCommands;
                        commandPattern2 = commandPattern3;
                        scanner2 = scanner3;
                        packageNamePattern3 = wordPattern;
                        packageNamePattern = wordPattern2;
                        ConnectCommand connectCommand2 = new ConnectCommand(curSourceFilterName, curSourcePortName, curTargetFilterName, curTargetPortName4);
                        arrayList.add(connectCommand2);
                        state2 = 16;
                        break;
                    case 13:
                        curClassName3 = curClassName4;
                        this.mBoundReferences.putAll(readKeyValueAssignments(scanner3, semicolonPattern3));
                        state4 = 16;
                        break;
                    case 14:
                        curClassName3 = curClassName4;
                        bindExternal(scanner3.eat(wordPattern2, "<external-identifier>"));
                        state4 = 16;
                        break;
                    case 15:
                        curClassName3 = curClassName4;
                        this.mSettings.putAll(readKeyValueAssignments(scanner3, semicolonPattern3));
                        state4 = 16;
                        break;
                    case 16:
                        scanner3.eat(semicolonPattern3, ";");
                        state5 = 0;
                        commandPattern = commandPattern3;
                        ignorePattern = ignorePattern3;
                        semicolonPattern = semicolonPattern3;
                        curTargetPortName = curTargetPortName3;
                        curClassName = curClassName4;
                        scanner = scanner3;
                        packageNamePattern2 = wordPattern;
                        break;
                    default:
                        int i14 = state6;
                        commandPattern = commandPattern3;
                        ignorePattern = ignorePattern3;
                        semicolonPattern = semicolonPattern3;
                        curClassName = curClassName4;
                        scanner = scanner3;
                        packageNamePattern2 = wordPattern;
                        packageNamePattern = wordPattern2;
                        curTargetPortName = curTargetPortName3;
                        state5 = i14;
                        break;
                }
            } else {
                String str3 = curTargetPortName2;
                int state7 = state6;
                Pattern pattern = commandPattern3;
                Pattern pattern2 = ignorePattern3;
                Pattern pattern3 = semicolonPattern3;
                String str4 = curClassName4;
                PatternScanner patternScanner = scanner3;
                Pattern pattern4 = wordPattern;
                Pattern packageNamePattern5 = wordPattern2;
                int state8 = state7;
                if (state8 != 16 && state8 != 0) {
                    throw new GraphIOException("Unexpected end of input!");
                }
                return;
            }
            String str5 = graphString;
            scanner3 = scanner;
            wordPattern2 = packageNamePattern;
            semicolonPattern3 = semicolonPattern;
            wordPattern = packageNamePattern2;
            commandPattern3 = commandPattern;
            curClassName4 = curClassName;
            ignorePattern3 = ignorePattern;
        }
    }

    public KeyValueMap readKeyValueAssignments(String assignments) throws GraphIOException {
        return readKeyValueAssignments(new PatternScanner(assignments, Pattern.compile("\\s+")), null);
    }

    /* JADX WARNING: Multi-variable type inference failed */
    private KeyValueMap readKeyValueAssignments(PatternScanner scanner, Pattern endPattern) throws GraphIOException {
        Pattern semicolonPattern;
        int STATE_POST_VALUE;
        int STATE_VALUE;
        int state;
        Object referencedObject;
        TextGraphReader textGraphReader = this;
        PatternScanner patternScanner = scanner;
        int i = 2;
        int i2 = 3;
        Pattern equalsPattern = Pattern.compile("=");
        Pattern semicolonPattern2 = Pattern.compile(";");
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+[a-zA-Z0-9]*");
        Pattern stringPattern = Pattern.compile("'[^']*'|\\\"[^\\\"]*\\\"");
        Pattern intPattern = Pattern.compile("[0-9]+");
        Pattern floatPattern = Pattern.compile("[0-9]*\\.[0-9]+f?");
        Pattern referencePattern = Pattern.compile("\\$[a-zA-Z]+[a-zA-Z0-9]");
        Pattern booleanPattern = Pattern.compile("true|false");
        KeyValueMap newVals = new KeyValueMap();
        int state2 = 0;
        String curKey = null;
        while (true) {
            if (scanner.atEnd()) {
                int STATE_VALUE2 = i;
                int i3 = i2;
                Pattern pattern = semicolonPattern2;
            } else if (endPattern == null || !scanner.peek(endPattern)) {
                switch (state2) {
                    case 0:
                        STATE_VALUE = i;
                        STATE_POST_VALUE = i2;
                        semicolonPattern = semicolonPattern2;
                        state2 = 1;
                        curKey = patternScanner.eat(wordPattern, "<identifier>");
                        continue;
                    case 1:
                        STATE_VALUE = i;
                        STATE_POST_VALUE = i2;
                        semicolonPattern = semicolonPattern2;
                        patternScanner.eat(equalsPattern, "=");
                        state = 2;
                        break;
                    case 2:
                        STATE_VALUE = i;
                        String tryEat = patternScanner.tryEat(stringPattern);
                        String curValue = tryEat;
                        STATE_POST_VALUE = i2;
                        if (tryEat == null) {
                            String tryEat2 = patternScanner.tryEat(referencePattern);
                            String curValue2 = tryEat2;
                            if (tryEat2 == null) {
                                semicolonPattern = semicolonPattern2;
                                String tryEat3 = patternScanner.tryEat(booleanPattern);
                                String curValue3 = tryEat3;
                                if (tryEat3 != null) {
                                    newVals.put(curKey, Boolean.valueOf(Boolean.parseBoolean(curValue3)));
                                } else {
                                    String tryEat4 = patternScanner.tryEat(floatPattern);
                                    String curValue4 = tryEat4;
                                    if (tryEat4 != null) {
                                        newVals.put(curKey, Float.valueOf(Float.parseFloat(curValue4)));
                                    } else {
                                        String tryEat5 = patternScanner.tryEat(intPattern);
                                        String curValue5 = tryEat5;
                                        if (tryEat5 != null) {
                                            newVals.put(curKey, Integer.valueOf(Integer.parseInt(curValue5)));
                                        } else {
                                            throw new GraphIOException(patternScanner.unexpectedTokenMessage("<value>"));
                                        }
                                    }
                                }
                                state = 3;
                                break;
                            } else {
                                String refName = curValue2.substring(1, curValue2.length());
                                if (textGraphReader.mBoundReferences != null) {
                                    referencedObject = textGraphReader.mBoundReferences.get(refName);
                                } else {
                                    referencedObject = null;
                                }
                                if (referencedObject != null) {
                                    newVals.put(curKey, referencedObject);
                                } else {
                                    Object obj = referencedObject;
                                    StringBuilder sb = new StringBuilder();
                                    Pattern pattern2 = semicolonPattern2;
                                    sb.append("Unknown object reference to '");
                                    sb.append(refName);
                                    sb.append("'!");
                                    throw new GraphIOException(sb.toString());
                                }
                            }
                        } else {
                            newVals.put(curKey, curValue.substring(1, curValue.length() - 1));
                        }
                        semicolonPattern = semicolonPattern2;
                        state = 3;
                    case 3:
                        STATE_VALUE = i;
                        patternScanner.eat(semicolonPattern2, ";");
                        state2 = 0;
                        STATE_POST_VALUE = i2;
                        semicolonPattern = semicolonPattern2;
                        continue;
                    default:
                        STATE_VALUE = i;
                        STATE_POST_VALUE = i2;
                        semicolonPattern = semicolonPattern2;
                        continue;
                }
                state2 = state;
                i = STATE_VALUE;
                i2 = STATE_POST_VALUE;
                semicolonPattern2 = semicolonPattern;
                textGraphReader = this;
            } else {
                int i4 = i;
                int i5 = i2;
                Pattern pattern3 = semicolonPattern2;
            }
        }
        if (state2 == 0 || state2 == 3) {
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
