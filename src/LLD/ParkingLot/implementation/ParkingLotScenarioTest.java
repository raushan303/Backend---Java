package LLD.ParkingLot.implementation;

import LLD.ParkingLot.implementation.exception.InvalidParkingTicketException;
import LLD.ParkingLot.implementation.exception.NoParkingSpotAvailableException;
import LLD.ParkingLot.implementation.exception.PaymentFailedException;
import LLD.ParkingLot.implementation.model.ExitReceipt;
import LLD.ParkingLot.implementation.model.FourWheelerParkingSpot;
import LLD.ParkingLot.implementation.model.ParkingSpot;
import LLD.ParkingLot.implementation.model.ParkingSpotType;
import LLD.ParkingLot.implementation.model.PaymentMethod;
import LLD.ParkingLot.implementation.model.Ticket;
import LLD.ParkingLot.implementation.model.TwoWheelerParkingSpot;
import LLD.ParkingLot.implementation.model.Vehicle;
import LLD.ParkingLot.implementation.model.VehicleType;
import LLD.ParkingLot.implementation.payment.CashPaymentProcessor;
import LLD.ParkingLot.implementation.payment.PaymentProcessorFactory;
import LLD.ParkingLot.implementation.payment.UpiPaymentProcessor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ParkingLotScenarioTest {

    private int passed;

    public static void main(String[] args) throws Exception {
        new ParkingLotScenarioTest().runAll();
    }

    private void runAll() throws Exception {
        run("nearest spot and release", this::nearestSpotAndRelease);
        run("hourly and minute rounding", this::pricingRounding);
        run("capacity and invalid ticket", this::capacityAndInvalidTicket);
        run("payment failure retry", this::paymentFailureRetry);
        run("idempotent checkout", this::idempotentCheckout);
        run("concurrent allocation", this::concurrentAllocation);
        System.out.println("All " + passed + " Parking Lot scenarios passed.");
    }

    private void nearestSpotAndRelease() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-11T10:00:00Z"));
        ParkingLot lot = standardLot(clock, new PaymentProcessorFactory());
        Ticket ticket = lot.enter("E2", new Vehicle("BIKE-1", VehicleType.TWO_WHEELER));
        assertEquals("T2", ticket.getParkingSpot().getId(), "nearest spot from E2");
        assertEquals(1L, lot.availableSpots(ParkingSpotType.TWO_WHEELER), "occupied count");
        clock.advance(Duration.ofMinutes(20));
        ExitReceipt receipt = lot.exit("X1", ticket.getId(), PaymentMethod.CASH);
        assertEquals(new BigDecimal("30.00"), receipt.amountPaid(), "hourly amount");
        assertEquals(2L, lot.availableSpots(ParkingSpotType.TWO_WHEELER), "released count");
    }

    private void pricingRounding() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-11T10:00:00Z"));
        ParkingLot lot = standardLot(clock, new PaymentProcessorFactory());
        Ticket bike = lot.enter("E1", new Vehicle("BIKE-2", VehicleType.TWO_WHEELER));
        clock.advance(Duration.ofMinutes(61));
        assertEquals(new BigDecimal("60.00"),
                lot.exit("X1", bike.getId(), PaymentMethod.UPI).amountPaid(),
                "partial hour rounds up");

        Ticket car = lot.enter("E1", new Vehicle("CAR-1", VehicleType.FOUR_WHEELER));
        clock.advance(Duration.ofSeconds(61));
        assertEquals(new BigDecimal("4.00"),
                lot.exit("X1", car.getId(), PaymentMethod.CARD).amountPaid(),
                "partial minute rounds up");
    }

    private void capacityAndInvalidTicket() {
        ParkingLot lot = new ParkingLot(
                List.of(new FourWheelerParkingSpot(
                        "F1", new BigDecimal("120.00"), Map.of("E1", 1))),
                List.of("E1"), List.of("X1"));
        lot.enter("E1", new Vehicle("CAR-2", VehicleType.FOUR_WHEELER));
        assertThrows(NoParkingSpotAvailableException.class,
                () -> lot.enter("E1", new Vehicle("CAR-3", VehicleType.FOUR_WHEELER)));
        assertThrows(InvalidParkingTicketException.class,
                () -> lot.exit("X1", "missing-ticket", PaymentMethod.CASH));
    }

    private void paymentFailureRetry() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-11T10:00:00Z"));
        PaymentProcessorFactory failingFactory = new PaymentProcessorFactory(Map.of(
                PaymentMethod.CASH, new CashPaymentProcessor(),
                PaymentMethod.CARD, (ticketId, amount, ignoredClock) -> {
                    throw new PaymentFailedException("Simulated gateway failure");
                },
                PaymentMethod.UPI, new UpiPaymentProcessor()));
        ParkingLot lot = standardLot(clock, failingFactory);
        Ticket ticket = lot.enter("E1", new Vehicle("CAR-4", VehicleType.FOUR_WHEELER));
        clock.advance(Duration.ofMinutes(10));
        assertThrows(PaymentFailedException.class,
                () -> lot.exit("X1", ticket.getId(), PaymentMethod.CARD));
        assertEquals(0L, lot.availableSpots(ParkingSpotType.FOUR_WHEELER),
                "failed payment keeps spot occupied");
        ExitReceipt receipt = lot.exit("X1", ticket.getId(), PaymentMethod.CASH);
        assertEquals(PaymentMethod.CASH, receipt.payment().method(), "retry payment method");
    }

    private void idempotentCheckout() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-11T10:00:00Z"));
        ParkingLot lot = standardLot(clock, new PaymentProcessorFactory());
        Ticket ticket = lot.enter("E1", new Vehicle("BIKE-3", VehicleType.TWO_WHEELER));
        clock.advance(Duration.ofMinutes(15));
        ExitReceipt first = lot.exit("X1", ticket.getId(), PaymentMethod.CARD);
        ExitReceipt retry = lot.exit("X1", ticket.getId(), PaymentMethod.UPI);
        assertEquals(first.payment().id(), retry.payment().id(), "payment is not duplicated");
        assertEquals(2L, lot.availableSpots(ParkingSpotType.TWO_WHEELER), "spot released once");
    }

    private void concurrentAllocation() throws Exception {
        ParkingLot lot = new ParkingLot(
                List.of(new TwoWheelerParkingSpot(
                        "ONLY", new BigDecimal("30.00"), Map.of("E1", 1))),
                List.of("E1"), List.of("X1"));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int index = 0; index < 2; index++) {
            int vehicleNumber = index;
            results.add(executor.submit(() -> {
                start.await();
                try {
                    lot.enter("E1", new Vehicle("RACE-" + vehicleNumber, VehicleType.TWO_WHEELER));
                    return true;
                } catch (NoParkingSpotAvailableException exception) {
                    return false;
                }
            }));
        }
        start.countDown();
        int successfulAllocations = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                successfulAllocations++;
            }
        }
        executor.shutdown();
        assertEquals(1, successfulAllocations, "exactly one concurrent allocation succeeds");
    }

    private ParkingLot standardLot(Clock clock, PaymentProcessorFactory paymentFactory) {
        List<ParkingSpot> spots = List.of(
                new TwoWheelerParkingSpot("T1", new BigDecimal("30.00"),
                        Map.of("E1", 1, "E2", 20)),
                new TwoWheelerParkingSpot("T2", new BigDecimal("30.00"),
                        Map.of("E1", 20, "E2", 1)),
                new FourWheelerParkingSpot("F1", new BigDecimal("120.00"),
                        Map.of("E1", 2, "E2", 10)));
        return new ParkingLot(spots, List.of("E1", "E2"), List.of("X1"), clock, paymentFactory);
    }

    private void run(String name, ThrowingTest test) throws Exception {
        test.run();
        passed++;
        System.out.println("PASS: " + name);
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private void assertThrows(Class<? extends Throwable> expected, Runnable action) {
        try {
            action.run();
        } catch (Throwable throwable) {
            if (expected.isInstance(throwable)) {
                return;
            }
            throw new AssertionError("Expected " + expected.getSimpleName()
                    + " but got " + throwable.getClass().getSimpleName(), throwable);
        }
        throw new AssertionError("Expected " + expected.getSimpleName() + " to be thrown");
    }

    @FunctionalInterface
    private interface ThrowingTest {
        void run() throws Exception;
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}