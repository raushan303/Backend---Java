package LLD.ParkingLot.implementation.payment;

import LLD.ParkingLot.implementation.model.PaymentMethod;

public final class UpiPaymentProcessor extends AbstractPaymentProcessor {

    public UpiPaymentProcessor() {
        super(PaymentMethod.UPI);
    }
}