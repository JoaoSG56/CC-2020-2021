import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PacketStack {
    private final Lock swl = new ReentrantReadWriteLock().writeLock();
    private final Lock rwl = new ReentrantReadWriteLock().writeLock();
//    private final Lock rl = l.readLock();
    //private final Lock wl = l.writeLock();


    private Stack<Packet> toSendToFast;

    private Stack<Request> clientRequests;

    private Stack<Response> clientResponses;


    public PacketStack() {
        this.clientRequests = new Stack<>();
        this.toSendToFast = new Stack<>();
        this.clientRequests = new Stack<>();
    }

    public void push_toSendToFast(Packet p) {
        swl.lock();
        try {
            this.toSendToFast.push(p);
        } finally {
            swl.unlock();
        }
    }

    public Packet pop_toSendToFast() {
        swl.lock();
        try {
            if (this.toSendToFast.empty()) return null;
            return this.toSendToFast.pop();
        } finally {
            swl.unlock();
        }
    }

    public void push_clientRequest(Request r) {
        rwl.lock();
        try {
            this.clientRequests.push(r);
        } finally {
            rwl.unlock();
        }
    }

    public Request pop_clientRequest() {
        rwl.lock();
        try {
            if (this.clientRequests.empty()) return null;
            return this.clientRequests.pop();
        } finally {
            rwl.unlock();
        }
    }

    public void push_clientResponse(Response r){
        rwl.lock();
        try {
            this.clientResponses.push(r);
        } finally {
            rwl.unlock();
        }
    }

    public Response pop_clientResponse() {
        rwl.lock();
        try {
            if (this.clientResponses.empty()) return null;
            return this.clientResponses.pop();
        } finally {
            rwl.unlock();
        }
    }
}
