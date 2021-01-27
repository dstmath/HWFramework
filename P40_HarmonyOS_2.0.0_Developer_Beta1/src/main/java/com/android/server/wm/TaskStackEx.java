package com.android.server.wm;

public class TaskStackEx {
    private TaskStack mTaskStack;

    public TaskStackEx() {
    }

    public TaskStackEx(TaskStack taskStack) {
        this.mTaskStack = taskStack;
    }

    public TaskStack getTaskStack() {
        return this.mTaskStack;
    }

    public void setTaskStack(TaskStack mTaskStack2) {
        this.mTaskStack = mTaskStack2;
    }

    public boolean hasChild(TaskEx child) {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack == null || child == null) {
            return false;
        }
        return taskStack.hasChild(child.getTask());
    }

    public DisplayContentEx getDisplayContent() {
        DisplayContentEx displayContentEx = new DisplayContentEx();
        displayContentEx.setDisplayContent(this.mTaskStack.getDisplayContent());
        return displayContentEx;
    }

    public TaskEx getTopChild() {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack == null || taskStack.getTopChild() == null) {
            return null;
        }
        TaskEx taskEx = new TaskEx();
        taskEx.setTask((Task) this.mTaskStack.getTopChild());
        return taskEx;
    }

    public int getChildrenSize() {
        return this.mTaskStack.mChildren.size();
    }

    public TaskEx getChildren(int index) {
        TaskStack taskStack = this.mTaskStack;
        if (taskStack == null || taskStack.mChildren == null || index >= this.mTaskStack.mChildren.size()) {
            return null;
        }
        return new TaskEx((Task) this.mTaskStack.mChildren.get(index));
    }

    public boolean isVisible() {
        return this.mTaskStack.isVisible();
    }

    public boolean inHwFreeFormWindowingMode() {
        return this.mTaskStack.inHwFreeFormWindowingMode();
    }

    public boolean isTaskStackEmpty() {
        return this.mTaskStack == null;
    }
}
