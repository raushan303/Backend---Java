package designpatterns.chainofresponsibilitypattern.advanced.model;

import java.util.Objects;

public record SupportActor(String id, SupportRole role) {

    public SupportActor {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Actor id must not be blank");
        }
        Objects.requireNonNull(role, "Support role must not be null");
    }
}
