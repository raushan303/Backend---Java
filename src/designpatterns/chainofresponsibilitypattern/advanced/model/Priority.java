package designpatterns.chainofresponsibilitypattern.advanced.model;

public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int severity;

    Priority(int severity) {
        this.severity = severity;
    }

    public int severity() {
        return severity;
    }
}
