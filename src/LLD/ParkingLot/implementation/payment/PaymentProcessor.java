package LLD.ParkingLot.implementation.payment;

import LLD.ParkingLot.implementation.model.Payment;

import java.math.BigDecimal;
import java.time.Clock;

public interface PaymentProcessor {

    Payment process(String ticketId, BigDecimal amount, Clock clock);
}