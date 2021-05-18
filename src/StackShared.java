import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StackShared {
    private final Lock wl = new ReentrantReadWriteLock().writeLock();

    private Stack<Object> stack;

    public Object pop(){
        this.wl.lock();
        try {
            return this.stack.pop();
        }finally {
            this.wl.unlock();
        }
    }

    public void push(Object o){
        this.wl.lock();
        try {
            this.stack.push(o);
        }finally {
            this.wl.unlock();
        }
    }
}
