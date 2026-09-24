package designpatterns.chainofresponsibilitypattern.handler;

import designpatterns.chainofresponsibilitypattern.model.SupportRequest;

public interface SupportHandler {

    SupportHandler setNext(SupportHandler nextHandler);

    void handle(SupportRequest request);
}
