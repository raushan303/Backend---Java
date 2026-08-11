package LLD.ParkingLot.implementation.pricing;

import LLD.ParkingLot.implementation.strategy.pricing.PerMinutePricingStrategy;

public final class FourWheelerCostComputation extends CostComputation {

    public FourWheelerCostComputation() {
        super(new PerMinutePricingStrategy());
    }
}