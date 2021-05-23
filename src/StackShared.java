import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class StackShared {
    private final Lock l = new ReentrantLock();
    private final Condition condition = l.newCondition();
    private Stack<Object> stack;


    public StackShared(){
        this.stack = new Stack<>();
    }


    public Object pop() throws InterruptedException {
        this.l.lock();
        try {
            if (this.stack.empty()) return null;
            return this.stack.pop();
        }finally {
            this.l.unlock();
        }
    }

    public void push(Object o){
        this.l.lock();
        try {
            this.stack.push(o);
            this.condition.signalAll();
        }finally {
            this.l.unlock();
        }
    }

    public Object iWannaPop() throws InterruptedException {
        this.l.lock();
        try {
            while (this.stack.empty())
                this.condition.await(5,TimeUnit.SECONDS);
            return this.stack.pop();
        }finally {
            this.l.unlock();
        }
    }
}
