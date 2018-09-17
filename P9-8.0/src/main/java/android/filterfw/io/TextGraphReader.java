package android.filterfw.io;

import android.filterfw.core.Filter;
import android.filterfw.core.FilterFactory;
import android.filterfw.core.FilterGraph;
import android.filterfw.core.KeyValueMap;
import android.filterfw.core.ProtocolException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class TextGraphReader extends GraphReader {
    private KeyValueMap mBoundReferences;
    private ArrayList<Command> mCommands = new ArrayList();
    private Filter mCurrentFilter;
    private FilterGraph mCurrentGraph;
    private FilterFactory mFactory;
    private KeyValueMap mSettings;

    private interface Command {
        void execute(TextGraphReader textGraphReader) throws GraphIOException;
    }

    private class AddLibraryCommand implements Command {
        private String mLibraryName;

        public AddLibraryCommand(String libraryName) {
            this.mLibraryName = libraryName;
        }

        public void execute(TextGraphReader reader) {
            reader.mFactory;
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
                reader.mCurrentFilter = reader.mFactory.createFilterByClassName(this.mClassName, this.mFilterName);
            } catch (IllegalArgumentException e) {
                throw new GraphIOException(e.getMessage());
            }
        }
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

    private void parseString(String graphString) throws GraphIOException {
        Pattern commandPattern = Pattern.compile("@[a-zA-Z]+");
        Pattern curlyClosePattern = Pattern.compile("\\}");
        Pattern curlyOpenPattern = Pattern.compile("\\{");
        Pattern ignorePattern = Pattern.compile("(\\s+|//[^\\n]*\\n)+");
        Pattern packageNamePattern = Pattern.compile("[a-zA-Z\\.]+");
        Pattern libraryNamePattern = Pattern.compile("[a-zA-Z\\./:]+");
        Pattern portPattern = Pattern.compile("\\[[a-zA-Z0-9\\-_]+\\]");
        Pattern rightArrowPattern = Pattern.compile("=>");
        Pattern semicolonPattern = Pattern.compile(";");
        Pattern wordPattern = Pattern.compile("[a-zA-Z0-9\\-_]+");
        int state = 0;
        PatternScanner patternScanner = new PatternScanner(graphString, ignorePattern);
        String curClassName = null;
        String curSourceFilterName = null;
        String curSourcePortName = null;
        String curTargetFilterName = null;
        while (!patternScanner.atEnd()) {
            String portString;
            switch (state) {
                case 0:
                    String curCommand = patternScanner.eat(commandPattern, "<command>");
                    if (curCommand.equals("@import")) {
                        state = 1;
                        break;
                    }
                    if (curCommand.equals("@library")) {
                        state = 2;
                        break;
                    }
                    if (curCommand.equals("@filter")) {
                        state = 3;
                        break;
                    }
                    if (curCommand.equals("@connect")) {
                        state = 8;
                        break;
                    }
                    if (curCommand.equals("@set")) {
                        state = 13;
                        break;
                    }
                    if (curCommand.equals("@external")) {
                        state = 14;
                        break;
                    }
                    if (curCommand.equals("@setting")) {
                        state = 15;
                        break;
                    }
                    throw new GraphIOException("Unknown command '" + curCommand + "'!");
                case 1:
                    this.mCommands.add(new ImportPackageCommand(patternScanner.eat(packageNamePattern, "<package-name>")));
                    state = 16;
                    break;
                case 2:
                    this.mCommands.add(new AddLibraryCommand(patternScanner.eat(libraryNamePattern, "<library-name>")));
                    state = 16;
                    break;
                case 3:
                    curClassName = patternScanner.eat(wordPattern, "<class-name>");
                    state = 4;
                    break;
                case 4:
                    this.mCommands.add(new AllocateFilterCommand(curClassName, patternScanner.eat(wordPattern, "<filter-name>")));
                    state = 5;
                    break;
                case 5:
                    patternScanner.eat(curlyOpenPattern, "{");
                    state = 6;
                    break;
                case 6:
                    this.mCommands.add(new InitFilterCommand(readKeyValueAssignments(patternScanner, curlyClosePattern)));
                    state = 7;
                    break;
                case 7:
                    patternScanner.eat(curlyClosePattern, "}");
                    state = 0;
                    break;
                case 8:
                    curSourceFilterName = patternScanner.eat(wordPattern, "<source-filter-name>");
                    state = 9;
                    break;
                case 9:
                    portString = patternScanner.eat(portPattern, "[<source-port-name>]");
                    curSourcePortName = portString.substring(1, portString.length() - 1);
                    state = 10;
                    break;
                case 10:
                    patternScanner.eat(rightArrowPattern, "=>");
                    state = 11;
                    break;
                case 11:
                    curTargetFilterName = patternScanner.eat(wordPattern, "<target-filter-name>");
                    state = 12;
                    break;
                case 12:
                    portString = patternScanner.eat(portPattern, "[<target-port-name>]");
                    String curTargetPortName = portString.substring(1, portString.length() - 1);
                    this.mCommands.add(new ConnectCommand(curSourceFilterName, curSourcePortName, curTargetFilterName, curTargetPortName));
                    state = 16;
                    break;
                case 13:
                    this.mBoundReferences.putAll(readKeyValueAssignments(patternScanner, semicolonPattern));
                    state = 16;
                    break;
                case 14:
                    bindExternal(patternScanner.eat(wordPattern, "<external-identifier>"));
                    state = 16;
                    break;
                case 15:
                    this.mSettings.putAll(readKeyValueAssignments(patternScanner, semicolonPattern));
                    state = 16;
                    break;
                case 16:
                    patternScanner.eat(semicolonPattern, ";");
                    state = 0;
                    break;
                default:
                    break;
            }
        }
        if (state != 16 && state != 0) {
            throw new GraphIOException("Unexpected end of input!");
        }
    }

    public KeyValueMap readKeyValueAssignments(String assignments) throws GraphIOException {
        return readKeyValueAssignments(new PatternScanner(assignments, Pattern.compile("\\s+")), null);
    }

    private KeyValueMap readKeyValueAssignments(PatternScanner scanner, Pattern endPattern) throws GraphIOException {
        Pattern equalsPattern = Pattern.compile("=");
        Pattern semicolonPattern = Pattern.compile(";");
        Pattern wordPattern = Pattern.compile("[a-zA-Z]+[a-zA-Z0-9]*");
        Pattern stringPattern = Pattern.compile("'[^']*'|\\\"[^\\\"]*\\\"");
        Pattern intPattern = Pattern.compile("[0-9]+");
        Pattern floatPattern = Pattern.compile("[0-9]*\\.[0-9]+f?");
        Pattern referencePattern = Pattern.compile("\\$[a-zA-Z]+[a-zA-Z0-9]");
        Pattern booleanPattern = Pattern.compile("true|false");
        int state = 0;
        KeyValueMap newVals = new KeyValueMap();
        Object curKey = null;
        while (!scanner.atEnd()) {
            if (((endPattern != null ? scanner.peek(endPattern) : 0) ^ 1) != 0) {
                switch (state) {
                    case 0:
                        curKey = scanner.eat(wordPattern, "<identifier>");
                        state = 1;
                        break;
                    case 1:
                        scanner.eat(equalsPattern, "=");
                        state = 2;
                        break;
                    case 2:
                        String curValue = scanner.tryEat(stringPattern);
                        if (curValue != null) {
                            newVals.put(curKey, curValue.substring(1, curValue.length() - 1));
                        } else {
                            curValue = scanner.tryEat(referencePattern);
                            if (curValue != null) {
                                Object referencedObject;
                                String refName = curValue.substring(1, curValue.length());
                                if (this.mBoundReferences != null) {
                                    referencedObject = this.mBoundReferences.get(refName);
                                } else {
                                    referencedObject = null;
                                }
                                if (referencedObject == null) {
                                    throw new GraphIOException("Unknown object reference to '" + refName + "'!");
                                }
                                newVals.put(curKey, referencedObject);
                            } else {
                                curValue = scanner.tryEat(booleanPattern);
                                if (curValue != null) {
                                    newVals.put(curKey, Boolean.valueOf(Boolean.parseBoolean(curValue)));
                                } else {
                                    curValue = scanner.tryEat(floatPattern);
                                    if (curValue != null) {
                                        newVals.put(curKey, Float.valueOf(Float.parseFloat(curValue)));
                                    } else {
                                        curValue = scanner.tryEat(intPattern);
                                        if (curValue != null) {
                                            newVals.put(curKey, Integer.valueOf(Integer.parseInt(curValue)));
                                        } else {
                                            throw new GraphIOException(scanner.unexpectedTokenMessage("<value>"));
                                        }
                                    }
                                }
                            }
                        }
                        state = 3;
                        break;
                    case 3:
                        scanner.eat(semicolonPattern, ";");
                        state = 0;
                        break;
                    default:
                        break;
                }
            } else if (state != 0 || state == 3) {
                return newVals;
            } else {
                throw new GraphIOException("Unexpected end of assignments on line " + scanner.lineNo() + "!");
            }
        }
        if (state != 0) {
        }
        return newVals;
    }

    private void bindExternal(String name) throws GraphIOException {
        if (this.mReferences.containsKey(name)) {
            this.mBoundReferences.put(name, this.mReferences.get(name));
            return;
        }
        throw new GraphIOException("Unknown external variable '" + name + "'! " + "You must add a reference to this external in the host program using " + "addReference(...)!");
    }

    private void checkReferences() throws GraphIOException {
        for (String reference : this.mReferences.keySet()) {
            if (!this.mBoundReferences.containsKey(reference)) {
                throw new GraphIOException("Host program specifies reference to '" + reference + "', which is not " + "declared @external in graph file!");
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
        for (Command command : this.mCommands) {
            command.execute(this);
        }
    }
}
