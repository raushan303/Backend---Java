package designpatterns.chainofresponsibilitypattern.advanced.model;

import java.util.Objects;

public record SupportRequest(String id, String issue, Priority priority) {

    public SupportRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Request id must not be blank");
        }
        if (issue == null || issue.isBlank()) {
            throw new IllegalArgumentException("Issue must not be blank");
        }
        Objects.requireNonNull(priority, "Priority must not be null");
    }

    public SupportRequest withPriority(Priority newPriority) {
        return new SupportRequest(id, issue, newPriority);
    }
}
