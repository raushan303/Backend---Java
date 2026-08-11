package LLD.ParkingLot.implementation.payment;

import LLD.ParkingLot.implementation.model.PaymentMethod;

public final class CashPaymentProcessor extends AbstractPaymentProcessor {

    public CashPaymentProcessor() {
        super(PaymentMethod.CASH);
    }
}