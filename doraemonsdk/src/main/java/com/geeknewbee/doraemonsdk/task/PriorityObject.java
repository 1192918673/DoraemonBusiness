package com.geeknewbee.doraemonsdk.task;

public class PriorityObject<E> {

    public final Priority priority;
    public final E obj;

    public PriorityObject(Priority priority, E obj) {
        this.priority = priority == null ? Priority.NORMAL : priority;
        this.obj = obj;
    }
}
