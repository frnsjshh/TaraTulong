package com.francis.taratulong.registration;

public enum AttendanceStatus {
    PENDING(0),
    PRESENT(10),
    NO_SHOW(-25),
    CANCELLED_EARLY(-2),
    CANCELLED_LATE(-10);

    private final int pointValue;
    AttendanceStatus(int pointValue) {
        this.pointValue = pointValue;
    }
    public int getPointValue() {
        return pointValue;
    }
}
