package android.filterfw.core;

public class ProgramVariable {
    private Program mProgram;
    private String mVarName;

    public ProgramVariable(Program program, String varName) {
        this.mProgram = program;
        this.mVarName = varName;
    }

    public Program getProgram() {
        return this.mProgram;
    }

    public String getVariableName() {
        return this.mVarName;
    }

    public void setValue(Object value) {
        if (this.mProgram == null) {
            throw new RuntimeException("Attempting to set program variable '" + this.mVarName + "' but the program is null!");
        }
        this.mProgram.setHostValue(this.mVarName, value);
    }

    public Object getValue() {
        if (this.mProgram != null) {
            return this.mProgram.getHostValue(this.mVarName);
        }
        throw new RuntimeException("Attempting to get program variable '" + this.mVarName + "' but the program is null!");
    }
}
