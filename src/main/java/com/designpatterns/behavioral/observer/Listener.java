package com.designpatterns.behavioral.observer;

public interface Listener<T> {
    void update(T data);
}
