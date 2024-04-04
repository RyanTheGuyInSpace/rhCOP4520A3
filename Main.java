package com.company;

import java.time.LocalTime;

class Main {
    public static void main(String[] args) {

        System.out.println("Started at " + LocalTime.now());
        int numPresents = 500000;

        ConcurrentLinkedList presents = new ConcurrentLinkedList();
        // 4 Threads, 4 Servants
        Thread[] servants = new Thread[4];

        int chunkSize = numPresents / servants.length;

        for (int i = 0; i < servants.length; i++) {
            int start = i * chunkSize + 1;
            int end = (i + 1) * chunkSize;
            servants[i] = new Thread(new Servant(presents, start, end));
            servants[i].start();
        }

        for (Thread servant : servants) {
            try {
                servant.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Check if any presents are left in the chain
        boolean anyLeft = false;
        for (int i = 1; i <= numPresents; i++) {
            if (presents.contains(i)) {
                anyLeft = true;
                break;
            }
        }

        if (anyLeft) {
            System.out.println("There is at least one present left in the chain.");
        } else {
            System.out.println("There are no presents left in the chain.");
        }

        System.out.println("Finished at " + LocalTime.now());
    }
}

class Present {
    int tag;
    Present next;

    Present(int tag) {
        this.tag = tag;
        this.next = null;
    }
}

class ConcurrentLinkedList {
    private Present head;

    public ConcurrentLinkedList() {
        head = new Present(Integer.MIN_VALUE);
        head.next = new Present(Integer.MAX_VALUE);
    }

    public synchronized boolean add(int tag) {
        Present pred = head;
        Present curr = pred.next;
        while (curr.tag < tag) {
            pred = curr;
            curr = curr.next;
        }
        if (curr.tag == tag) {
            // Present already exists in the list
            return false;
        }
        Present newPresent = new Present(tag);
        newPresent.next = curr;
        pred.next = newPresent;

        return true;
    }

    public synchronized boolean remove(int tag) {
        Present pred = head;
        Present curr = pred.next;
        while (curr.tag < tag) {
            pred = curr;
            curr = curr.next;
        }
        if (curr.tag == tag) {
            pred.next = curr.next;

            // Present was removed
            return true;
        }

        // Present was not found
        return false;
    }

    public synchronized boolean contains(int tag) {
        Present curr = head.next;
        while (curr.tag < tag) {
            curr = curr.next;
        }
        // Present found or not
        return curr.tag == tag;
    }
}

class Servant implements Runnable {
    private ConcurrentLinkedList presents;
    private int start;
    private int end;

    public Servant(ConcurrentLinkedList presents, int start, int end) {
        this.presents = presents;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        for (int i = start; i <= end; i++) {
            if (!presents.contains(i)) {
                presents.add(i);
            } else {
                presents.remove(i);
            }
        }
    }
}