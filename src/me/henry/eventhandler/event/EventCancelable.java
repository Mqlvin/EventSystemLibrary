package me.henry.eventhandler.event;

public class EventCancelable extends Event {
    private Boolean canceled = false;

    public void cancel() {
        canceled = true;
    }

    public Boolean isCanceled() {
        return canceled;
    }
}
