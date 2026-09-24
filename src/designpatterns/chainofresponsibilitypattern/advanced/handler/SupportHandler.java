package designpatterns.chainofresponsibilitypattern.advanced.handler;

import designpatterns.chainofresponsibilitypattern.advanced.model.SupportRequest;
import designpatterns.chainofresponsibilitypattern.advanced.result.HandlingResult;

public interface SupportHandler {

    SupportHandler setNext(SupportHandler nextHandler);

    HandlingResult handle(SupportRequest request);
}
