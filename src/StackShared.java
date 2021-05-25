import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe que implementa a Stack Partilhada
 */

public class StackShared {
    private final Lock l = new ReentrantLock();
    private final Condition condition = l.newCondition();
    private Stack<Object> stack;


    /**
     * Construtor da classe
     */
    public StackShared(){
        this.stack = new Stack<>();
    }

    /**
     * Método que dá pop na stack partilhada
     *
     * @return                      Objeto da stack que foi obtido pelo pop
     * @throws InterruptedException Exceção de interrupção
     */
    public Object pop() throws InterruptedException {
        this.l.lock();
        try {
            if (this.stack.empty()) return null;
            return this.stack.pop();
        }finally {
            this.l.unlock();
        }
    }

    /**
     * Método que acrescenta um objeto à stack
     *
     * @param o     Objeto a ser acrescentado
     */
    public void push(Object o){
        this.l.lock();
        try {
            this.stack.push(o);
            this.condition.signalAll();
        }finally {
            this.l.unlock();
        }
    }

    /**
     * Método que tenta dar pop da stack num determinado período de tempo
     *
     * @return                      Ojeto obtido pelo pop
     * @throws InterruptedException Exceção de interrupção
     */
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
