package Monitores;

// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.Set;
import java.util.concurrent.locks.Condition;
// import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import Interfaces.IPollingStation;
// import Threads.TVoter;

import java.util.ArrayDeque;
import java.util.Queue;

public class MPollingStation implements IPollingStation{

    private static MPollingStation instance;
    private final int capacity;
    private boolean isOpen;
    // private final Set<Integer> usedIds;
    private boolean isAproved;
    private boolean aprovalFlag;
    private final ReentrantLock aprovedLock;
    private final Condition aprovalReady;


    //queue
    private final Queue<Integer> votersQueue;
    //queue lock
    private final ReentrantLock queue_lock;
    private final Condition notEmpty;
    private final Condition notFull;
    //station lock
    private final Condition stationOpen;

   
    
    private MPollingStation(int capacity) {
        this.capacity = capacity;
        this.isOpen = false;
        // this.usedIds = new HashSet<>();

        this.isAproved = true;
        this.aprovalFlag = false;

        this.votersQueue = new ArrayDeque<Integer>();

        this.queue_lock = new ReentrantLock();
        this.notEmpty = queue_lock.newCondition();
        this.notFull = queue_lock.newCondition();
        this.stationOpen = queue_lock.newCondition();

        this.aprovedLock = new ReentrantLock();
        this.aprovalReady = aprovedLock.newCondition();

        // this.messageBoard = new HashMap<>();
        // this.hash_lock = new ReentrantLock();
        // this.newMessage = queue_lock.newCondition();
    }
    
    public static MPollingStation getInstance(int maxCapacity) {
        if (instance == null) {
            instance = new MPollingStation(maxCapacity);
        }
        return instance;
    }

    @Override
    public boolean enterPollingStation(int voterId) {
        queue_lock.lock();
        try {
            while (!isOpen) {
                stationOpen.await();
            }
            
            while (votersQueue.size() >= capacity) {
                notFull.await();
            }
            
            votersQueue.offer(voterId);
            notEmpty.signal();
            return true;

        } catch (InterruptedException e) {
            return false;
        } finally {
            queue_lock.unlock();
        }
    }

    private boolean isIdInQueue(int voterId) {
        return votersQueue.contains(voterId);
    }

    @Override
    public int waitIdValidation(int voterId) {
        // return 0 if still in queue
        // return 1 if aproved
        // return -1 if not aproved
        aprovedLock.lock();
        try {
            while(aprovalFlag == false){
                aprovalReady.await();
            }

            queue_lock.lock();
            try{
                if(isIdInQueue(voterId)){
                    System.out.println("ID validation was not for Voter " + voterId);
                    return 0;
                } else {
                    System.out.println("ID validation was for Voter " + voterId);
                    aprovalFlag = false;
                    if (isAproved) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            } finally {
                queue_lock.unlock();
            }

        } catch (InterruptedException e) {
            return 0;
        } finally {
            aprovedLock.unlock();
        }
    }

    @Override
    public int callNextVoter(){
        queue_lock.lock();
        try {
            if (votersQueue.isEmpty()) {
                notEmpty.await();
            }
            int id = votersQueue.poll();
            notFull.signal();
            return id;
        } catch (InterruptedException e) {
            return -1;
        } finally {
            queue_lock.unlock();
        }
    }

    @Override
    public void sendSignal(boolean response) {
        aprovedLock.lock();
        try{
            isAproved = response;
            aprovalFlag = true;
            aprovalReady.signalAll();
        } finally {
            aprovedLock.unlock();
        }
    }
    
    // @Override
    // public void exitPollingStation(int voterId) {
    //     lock.lock();
    //     try {
    //         votersInside.remove(voterId);
    //         stationNotFull.signal();
    //     } finally {
    //         lock.unlock();
    //     }
    // }

    // @Override
    // boolean validateID(int voterId);

    

    @Override
    public void openPollingStation() {
        queue_lock.lock();
        try {
            isOpen = true;
            stationOpen.signalAll();
        } finally {
            queue_lock.unlock();
        }
    }
    
    @Override
    public void closePollingStation() {
        queue_lock.lock();
        try {
            isOpen = false;
        } finally {
            queue_lock.unlock();
        }
    }
    
    @Override
    public boolean isOpen() {
        queue_lock.lock();
        try {
            return isOpen;
        } finally {
            queue_lock.unlock();
        }
    }
}
