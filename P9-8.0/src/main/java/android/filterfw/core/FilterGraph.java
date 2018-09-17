package android.filterfw.core;

import android.filterpacks.base.FrameBranch;
import android.filterpacks.base.NullFilter;
import android.util.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

public class FilterGraph {
    public static final int AUTOBRANCH_OFF = 0;
    public static final int AUTOBRANCH_SYNCED = 1;
    public static final int AUTOBRANCH_UNSYNCED = 2;
    public static final int TYPECHECK_DYNAMIC = 1;
    public static final int TYPECHECK_OFF = 0;
    public static final int TYPECHECK_STRICT = 2;
    private String TAG = "FilterGraph";
    private int mAutoBranchMode = 0;
    private boolean mDiscardUnconnectedOutputs = false;
    private HashSet<Filter> mFilters = new HashSet();
    private boolean mIsReady = false;
    private boolean mLogVerbose = Log.isLoggable(this.TAG, 2);
    private HashMap<String, Filter> mNameMap = new HashMap();
    private HashMap<OutputPort, LinkedList<InputPort>> mPreconnections = new HashMap();
    private int mTypeCheckMode = 2;

    public boolean addFilter(Filter filter) {
        if (containsFilter(filter)) {
            return false;
        }
        this.mFilters.add(filter);
        this.mNameMap.put(filter.getName(), filter);
        return true;
    }

    public boolean containsFilter(Filter filter) {
        return this.mFilters.contains(filter);
    }

    public Filter getFilter(String name) {
        return (Filter) this.mNameMap.get(name);
    }

    public void connect(Filter source, String outputName, Filter target, String inputName) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Passing null Filter in connect()!");
        } else if (containsFilter(source) && (containsFilter(target) ^ 1) == 0) {
            OutputPort outPort = source.getOutputPort(outputName);
            InputPort inPort = target.getInputPort(inputName);
            if (outPort == null) {
                throw new RuntimeException("Unknown output port '" + outputName + "' on Filter " + source + "!");
            } else if (inPort == null) {
                throw new RuntimeException("Unknown input port '" + inputName + "' on Filter " + target + "!");
            } else {
                preconnect(outPort, inPort);
            }
        } else {
            throw new RuntimeException("Attempting to connect filter not in graph!");
        }
    }

    public void connect(String sourceName, String outputName, String targetName, String inputName) {
        Filter source = getFilter(sourceName);
        Filter target = getFilter(targetName);
        if (source == null) {
            throw new RuntimeException("Attempting to connect unknown source filter '" + sourceName + "'!");
        } else if (target == null) {
            throw new RuntimeException("Attempting to connect unknown target filter '" + targetName + "'!");
        } else {
            connect(source, outputName, target, inputName);
        }
    }

    public Set<Filter> getFilters() {
        return this.mFilters;
    }

    public void beginProcessing() {
        if (this.mLogVerbose) {
            Log.v(this.TAG, "Opening all filter connections...");
        }
        for (Filter filter : this.mFilters) {
            filter.openOutputs();
        }
        this.mIsReady = true;
    }

    public void flushFrames() {
        for (Filter filter : this.mFilters) {
            filter.clearOutputs();
        }
    }

    public void closeFilters(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(this.TAG, "Closing all filters...");
        }
        for (Filter filter : this.mFilters) {
            filter.performClose(context);
        }
        this.mIsReady = false;
    }

    public boolean isReady() {
        return this.mIsReady;
    }

    public void setAutoBranchMode(int autoBranchMode) {
        this.mAutoBranchMode = autoBranchMode;
    }

    public void setDiscardUnconnectedOutputs(boolean discard) {
        this.mDiscardUnconnectedOutputs = discard;
    }

    public void setTypeCheckMode(int typeCheckMode) {
        this.mTypeCheckMode = typeCheckMode;
    }

    public void tearDown(FilterContext context) {
        if (!this.mFilters.isEmpty()) {
            flushFrames();
            for (Filter filter : this.mFilters) {
                filter.performTearDown(context);
            }
            this.mFilters.clear();
            this.mNameMap.clear();
            this.mIsReady = false;
        }
    }

    private boolean readyForProcessing(Filter filter, Set<Filter> processed) {
        if (processed.contains(filter)) {
            return false;
        }
        for (InputPort port : filter.getInputPorts()) {
            Filter dependency = port.getSourceFilter();
            if (dependency != null && (processed.contains(dependency) ^ 1) != 0) {
                return false;
            }
        }
        return true;
    }

    private void runTypeCheck() {
        Stack<Filter> filterStack = new Stack();
        Set<Filter> processedFilters = new HashSet();
        filterStack.addAll(getSourceFilters());
        while (!filterStack.empty()) {
            Filter filter = (Filter) filterStack.pop();
            processedFilters.add(filter);
            updateOutputs(filter);
            if (this.mLogVerbose) {
                Log.v(this.TAG, "Running type check on " + filter + "...");
            }
            runTypeCheckOn(filter);
            for (OutputPort port : filter.getOutputPorts()) {
                Filter target = port.getTargetFilter();
                if (target != null && readyForProcessing(target, processedFilters)) {
                    filterStack.push(target);
                }
            }
        }
        if (processedFilters.size() != getFilters().size()) {
            throw new RuntimeException("Could not schedule all filters! Is your graph malformed?");
        }
    }

    private void updateOutputs(Filter filter) {
        for (OutputPort outputPort : filter.getOutputPorts()) {
            InputPort inputPort = outputPort.getBasePort();
            if (inputPort != null) {
                FrameFormat outputFormat = filter.getOutputFormat(outputPort.getName(), inputPort.getSourceFormat());
                if (outputFormat == null) {
                    throw new RuntimeException("Filter did not return an output format for " + outputPort + "!");
                }
                outputPort.setPortFormat(outputFormat);
            }
        }
    }

    private void runTypeCheckOn(Filter filter) {
        for (InputPort inputPort : filter.getInputPorts()) {
            if (this.mLogVerbose) {
                Log.v(this.TAG, "Type checking port " + inputPort);
            }
            FrameFormat sourceFormat = inputPort.getSourceFormat();
            FrameFormat targetFormat = inputPort.getPortFormat();
            if (!(sourceFormat == null || targetFormat == null)) {
                if (this.mLogVerbose) {
                    Log.v(this.TAG, "Checking " + sourceFormat + " against " + targetFormat + ".");
                }
                boolean compatible = true;
                switch (this.mTypeCheckMode) {
                    case 0:
                        inputPort.setChecksType(false);
                        break;
                    case 1:
                        compatible = sourceFormat.mayBeCompatibleWith(targetFormat);
                        inputPort.setChecksType(true);
                        break;
                    case 2:
                        compatible = sourceFormat.isCompatibleWith(targetFormat);
                        inputPort.setChecksType(false);
                        break;
                }
                if (!compatible) {
                    throw new RuntimeException("Type mismatch: Filter " + filter + " expects a " + "format of type " + targetFormat + " but got a format of type " + sourceFormat + "!");
                }
            }
        }
    }

    private void checkConnections() {
    }

    private void discardUnconnectedOutputs() {
        LinkedList<Filter> addedFilters = new LinkedList();
        for (Filter filter : this.mFilters) {
            int id = 0;
            for (OutputPort port : filter.getOutputPorts()) {
                if (!port.isConnected()) {
                    if (this.mLogVerbose) {
                        Log.v(this.TAG, "Autoconnecting unconnected " + port + " to Null filter.");
                    }
                    NullFilter nullFilter = new NullFilter(filter.getName() + "ToNull" + id);
                    nullFilter.init();
                    addedFilters.add(nullFilter);
                    port.connectTo(nullFilter.getInputPort("frame"));
                    id++;
                }
            }
        }
        for (Filter filter2 : addedFilters) {
            addFilter(filter2);
        }
    }

    private void removeFilter(Filter filter) {
        this.mFilters.remove(filter);
        this.mNameMap.remove(filter.getName());
    }

    private void preconnect(OutputPort outPort, InputPort inPort) {
        LinkedList<InputPort> targets = (LinkedList) this.mPreconnections.get(outPort);
        if (targets == null) {
            targets = new LinkedList();
            this.mPreconnections.put(outPort, targets);
        }
        targets.add(inPort);
    }

    private void connectPorts() {
        int branchId = 1;
        for (Entry<OutputPort, LinkedList<InputPort>> connection : this.mPreconnections.entrySet()) {
            OutputPort outputPort = (OutputPort) connection.getKey();
            LinkedList<InputPort> inputPorts = (LinkedList) connection.getValue();
            if (inputPorts.size() == 1) {
                outputPort.connectTo((InputPort) inputPorts.get(0));
            } else if (this.mAutoBranchMode == 0) {
                throw new RuntimeException("Attempting to connect " + outputPort + " to multiple " + "filter ports! Enable auto-branching to allow this.");
            } else {
                if (this.mLogVerbose) {
                    Log.v(this.TAG, "Creating branch for " + outputPort + "!");
                }
                if (this.mAutoBranchMode == 1) {
                    int branchId2 = branchId + 1;
                    FrameBranch branch = new FrameBranch("branch" + branchId);
                    KeyValueMap branchParams = new KeyValueMap();
                    branch.initWithAssignmentList("outputs", Integer.valueOf(inputPorts.size()));
                    addFilter(branch);
                    outputPort.connectTo(branch.getInputPort("in"));
                    Iterator<InputPort> inputPortIter = inputPorts.iterator();
                    for (OutputPort branchOutPort : branch.getOutputPorts()) {
                        branchOutPort.connectTo((InputPort) inputPortIter.next());
                    }
                    branchId = branchId2;
                } else {
                    throw new RuntimeException("TODO: Unsynced branches not implemented yet!");
                }
            }
        }
        this.mPreconnections.clear();
    }

    private HashSet<Filter> getSourceFilters() {
        HashSet<Filter> sourceFilters = new HashSet();
        for (Filter filter : getFilters()) {
            if (filter.getNumberOfConnectedInputs() == 0) {
                if (this.mLogVerbose) {
                    Log.v(this.TAG, "Found source filter: " + filter);
                }
                sourceFilters.add(filter);
            }
        }
        return sourceFilters;
    }

    void setupFilters() {
        if (this.mDiscardUnconnectedOutputs) {
            discardUnconnectedOutputs();
        }
        connectPorts();
        checkConnections();
        runTypeCheck();
    }
}
