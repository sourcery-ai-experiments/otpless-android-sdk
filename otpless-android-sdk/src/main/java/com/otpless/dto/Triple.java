package com.otpless.dto;

public class Triple<A, B, C> extends Tuple<A, B>{

    private C third;

    public Triple(final A first, final B second, final C third) {
        super(first, second);
        this.third = third;
    }

    public C getThird() {
        return third;
    }

    public void setThird(C third) {
        this.third = third;
    }
}
