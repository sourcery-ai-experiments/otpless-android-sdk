package com.otpless.dto;

public class Tuple<A, B> {
    private A first;
    private B second;

    public Tuple(final A first, final B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(final A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(final B second) {
        this.second = second;
    }
}
