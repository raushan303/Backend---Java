package designpatterns.chainofresponsibilitypattern.model;

import java.util.Objects;

public record SupportRequest(String issue, Priority priority) {

    public SupportRequest {
        if (issue == null || issue.isBlank()) {
            throw new IllegalArgumentException("Issue must not be blank");
        }
        Objects.requireNonNull(priority, "Priority must not be null");
    }
}
