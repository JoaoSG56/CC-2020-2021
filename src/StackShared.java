import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StackShared {
    private final Lock wl = new ReentrantReadWriteLock().writeLock();
    private Condition condition = wl.newCondition();
    private Stack<Object> stack;

    public StackShared(){
        this.stack = new Stack<>();
    }


    public Object pop() throws InterruptedException {
        this.wl.lock();
        try {
            if (this.stack.empty()) return null;
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
            this.condition.signalAll();
        }
    }

    public Object iWannaPop() throws InterruptedException {
        this.wl.lock();
        try {
            while (this.stack.empty())
                this.condition.await(5,TimeUnit.SECONDS);
            return this.stack.pop();
        }finally {
            this.wl.unlock();
        }
    }
}
