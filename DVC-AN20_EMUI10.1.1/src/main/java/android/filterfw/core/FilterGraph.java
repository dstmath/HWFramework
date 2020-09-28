package android.filterfw.core;

import android.annotation.UnsupportedAppUsage;
import android.filterpacks.base.FrameBranch;
import android.filterpacks.base.NullFilter;
import android.telecom.Logging.Session;
import android.util.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
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
    private HashSet<Filter> mFilters = new HashSet<>();
    private boolean mIsReady = false;
    private boolean mLogVerbose = Log.isLoggable(this.TAG, 2);
    private HashMap<String, Filter> mNameMap = new HashMap<>();
    private HashMap<OutputPort, LinkedList<InputPort>> mPreconnections = new HashMap<>();
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

    @UnsupportedAppUsage
    public Filter getFilter(String name) {
        return this.mNameMap.get(name);
    }

    public void connect(Filter source, String outputName, Filter target, String inputName) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Passing null Filter in connect()!");
        } else if (!containsFilter(source) || !containsFilter(target)) {
            throw new RuntimeException("Attempting to connect filter not in graph!");
        } else {
            OutputPort outPort = source.getOutputPort(outputName);
            InputPort inPort = target.getInputPort(inputName);
            if (outPort == null) {
                throw new RuntimeException("Unknown output port '" + outputName + "' on Filter " + source + "!");
            } else if (inPort != null) {
                preconnect(outPort, inPort);
            } else {
                throw new RuntimeException("Unknown input port '" + inputName + "' on Filter " + target + "!");
            }
        }
    }

    public void connect(String sourceName, String outputName, String targetName, String inputName) {
        Filter source = getFilter(sourceName);
        Filter target = getFilter(targetName);
        if (source == null) {
            throw new RuntimeException("Attempting to connect unknown source filter '" + sourceName + "'!");
        } else if (target != null) {
            connect(source, outputName, target, inputName);
        } else {
            throw new RuntimeException("Attempting to connect unknown target filter '" + targetName + "'!");
        }
    }

    public Set<Filter> getFilters() {
        return this.mFilters;
    }

    public void beginProcessing() {
        if (this.mLogVerbose) {
            Log.v(this.TAG, "Opening all filter connections...");
        }
        Iterator<Filter> it = this.mFilters.iterator();
        while (it.hasNext()) {
            it.next().openOutputs();
        }
        this.mIsReady = true;
    }

    public void flushFrames() {
        Iterator<Filter> it = this.mFilters.iterator();
        while (it.hasNext()) {
            it.next().clearOutputs();
        }
    }

    public void closeFilters(FilterContext context) {
        if (this.mLogVerbose) {
            Log.v(this.TAG, "Closing all filters...");
        }
        Iterator<Filter> it = this.mFilters.iterator();
        while (it.hasNext()) {
            it.next().performClose(context);
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

    @UnsupportedAppUsage
    public void tearDown(FilterContext context) {
        if (!this.mFilters.isEmpty()) {
            flushFrames();
            Iterator<Filter> it = this.mFilters.iterator();
            while (it.hasNext()) {
                it.next().performTearDown(context);
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
            if (!(dependency == null || processed.contains(dependency))) {
                return false;
            }
        }
        return true;
    }

    private void runTypeCheck() {
        Stack<Filter> filterStack = new Stack<>();
        Set<Filter> processedFilters = new HashSet<>();
        filterStack.addAll(getSourceFilters());
        while (!filterStack.empty()) {
            Filter filter = filterStack.pop();
            processedFilters.add(filter);
            updateOutputs(filter);
            if (this.mLogVerbose) {
                String str = this.TAG;
                Log.v(str, "Running type check on " + filter + Session.TRUNCATE_STRING);
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
                if (outputFormat != null) {
                    outputPort.setPortFormat(outputFormat);
                } else {
                    throw new RuntimeException("Filter did not return an output format for " + outputPort + "!");
                }
            }
        }
    }

    private void runTypeCheckOn(Filter filter) {
        for (InputPort inputPort : filter.getInputPorts()) {
            if (this.mLogVerbose) {
                String str = this.TAG;
                Log.v(str, "Type checking port " + inputPort);
            }
            FrameFormat sourceFormat = inputPort.getSourceFormat();
            FrameFormat targetFormat = inputPort.getPortFormat();
            if (!(sourceFormat == null || targetFormat == null)) {
                if (this.mLogVerbose) {
                    String str2 = this.TAG;
                    Log.v(str2, "Checking " + sourceFormat + " against " + targetFormat + ".");
                }
                boolean compatible = true;
                int i = this.mTypeCheckMode;
                if (i == 0) {
                    inputPort.setChecksType(false);
                } else if (i == 1) {
                    compatible = sourceFormat.mayBeCompatibleWith(targetFormat);
                    inputPort.setChecksType(true);
                } else if (i == 2) {
                    compatible = sourceFormat.isCompatibleWith(targetFormat);
                    inputPort.setChecksType(false);
                }
                if (!compatible) {
                    throw new RuntimeException("Type mismatch: Filter " + filter + " expects a format of type " + targetFormat + " but got a format of type " + sourceFormat + "!");
                }
            }
        }
    }

    private void checkConnections() {
    }

    private void discardUnconnectedOutputs() {
        LinkedList<Filter> addedFilters = new LinkedList<>();
        Iterator<Filter> it = this.mFilters.iterator();
        while (it.hasNext()) {
            Filter filter = it.next();
            int id = 0;
            for (OutputPort port : filter.getOutputPorts()) {
                if (!port.isConnected()) {
                    if (this.mLogVerbose) {
                        String str = this.TAG;
                        Log.v(str, "Autoconnecting unconnected " + port + " to Null filter.");
                    }
                    NullFilter nullFilter = new NullFilter(filter.getName() + "ToNull" + id);
                    nullFilter.init();
                    addedFilters.add(nullFilter);
                    port.connectTo(nullFilter.getInputPort("frame"));
                    id++;
                }
            }
        }
        Iterator<Filter> it2 = addedFilters.iterator();
        while (it2.hasNext()) {
            addFilter(it2.next());
        }
    }

    private void removeFilter(Filter filter) {
        this.mFilters.remove(filter);
        this.mNameMap.remove(filter.getName());
    }

    private void preconnect(OutputPort outPort, InputPort inPort) {
        LinkedList<InputPort> targets = this.mPreconnections.get(outPort);
        if (targets == null) {
            targets = new LinkedList<>();
            this.mPreconnections.put(outPort, targets);
        }
        targets.add(inPort);
    }

    private void connectPorts() {
        int branchId = 1;
        for (Map.Entry<OutputPort, LinkedList<InputPort>> connection : this.mPreconnections.entrySet()) {
            OutputPort outputPort = connection.getKey();
            LinkedList<InputPort> inputPorts = connection.getValue();
            if (inputPorts.size() == 1) {
                outputPort.connectTo(inputPorts.get(0));
            } else if (this.mAutoBranchMode != 0) {
                if (this.mLogVerbose) {
                    String str = this.TAG;
                    Log.v(str, "Creating branch for " + outputPort + "!");
                }
                if (this.mAutoBranchMode == 1) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("branch");
                    int branchId2 = branchId + 1;
                    sb.append(branchId);
                    FrameBranch branch = new FrameBranch(sb.toString());
                    new KeyValueMap();
                    branch.initWithAssignmentList("outputs", Integer.valueOf(inputPorts.size()));
                    addFilter(branch);
                    outputPort.connectTo(branch.getInputPort("in"));
                    Iterator<InputPort> inputPortIter = inputPorts.iterator();
                    for (OutputPort branchOutPort : branch.getOutputPorts()) {
                        branchOutPort.connectTo(inputPortIter.next());
                    }
                    branchId = branchId2;
                } else {
                    throw new RuntimeException("TODO: Unsynced branches not implemented yet!");
                }
            } else {
                throw new RuntimeException("Attempting to connect " + outputPort + " to multiple filter ports! Enable auto-branching to allow this.");
            }
        }
        this.mPreconnections.clear();
    }

    private HashSet<Filter> getSourceFilters() {
        HashSet<Filter> sourceFilters = new HashSet<>();
        for (Filter filter : getFilters()) {
            if (filter.getNumberOfConnectedInputs() == 0) {
                if (this.mLogVerbose) {
                    String str = this.TAG;
                    Log.v(str, "Found source filter: " + filter);
                }
                sourceFilters.add(filter);
            }
        }
        return sourceFilters;
    }

    /* access modifiers changed from: package-private */
    public void setupFilters() {
        if (this.mDiscardUnconnectedOutputs) {
            discardUnconnectedOutputs();
        }
        connectPorts();
        checkConnections();
        runTypeCheck();
    }
}
