package com.example.kitemessenger;

public class Conversation {


    public boolean seen;
    public long timeStamp;

    public Conversation(){

    }

    public Conversation(boolean seen, long timeStamp) {
        this.seen = seen;
        this.timeStamp = timeStamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
