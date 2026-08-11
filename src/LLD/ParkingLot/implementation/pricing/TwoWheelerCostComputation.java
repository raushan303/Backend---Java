package LLD.ParkingLot.implementation.pricing;

import LLD.ParkingLot.implementation.strategy.pricing.HourlyPricingStrategy;

public final class TwoWheelerCostComputation extends CostComputation {

    public TwoWheelerCostComputation() {
        super(new HourlyPricingStrategy());
    }
}