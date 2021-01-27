package ohos.com.sun.java_cup.internal.runtime;

import java.util.Stack;

public class virtual_parse_stack {
    protected int real_next;
    protected Stack real_stack;
    protected Stack vstack;

    public virtual_parse_stack(Stack stack) throws Exception {
        if (stack != null) {
            this.real_stack = stack;
            this.vstack = new Stack();
            this.real_next = 0;
            get_from_real();
            return;
        }
        throw new Exception("Internal parser error: attempt to create null virtual stack");
    }

    /* access modifiers changed from: protected */
    public void get_from_real() {
        if (this.real_next < this.real_stack.size()) {
            Stack stack = this.real_stack;
            this.real_next++;
            this.vstack.push(new Integer(((Symbol) stack.elementAt((stack.size() - 1) - this.real_next)).parse_state));
        }
    }

    public boolean empty() {
        return this.vstack.empty();
    }

    public int top() throws Exception {
        if (!this.vstack.empty()) {
            return ((Integer) this.vstack.peek()).intValue();
        }
        throw new Exception("Internal parser error: top() called on empty virtual stack");
    }

    public void pop() throws Exception {
        if (!this.vstack.empty()) {
            this.vstack.pop();
            if (this.vstack.empty()) {
                get_from_real();
                return;
            }
            return;
        }
        throw new Exception("Internal parser error: pop from empty virtual stack");
    }

    public void push(int i) {
        this.vstack.push(new Integer(i));
    }
}
