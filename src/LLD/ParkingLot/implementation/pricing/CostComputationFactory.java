package LLD.ParkingLot.implementation.pricing;

import LLD.ParkingLot.implementation.model.VehicleType;

import java.util.EnumMap;
import java.util.Map;

public final class CostComputationFactory {

    private final Map<VehicleType, CostComputation> computations = new EnumMap<>(VehicleType.class);

    public CostComputationFactory() {
        computations.put(VehicleType.TWO_WHEELER, new TwoWheelerCostComputation());
        computations.put(VehicleType.FOUR_WHEELER, new FourWheelerCostComputation());
    }

    public CostComputation getComputation(VehicleType vehicleType) {
        CostComputation computation = computations.get(vehicleType);
        if (computation == null) {
            throw new IllegalArgumentException("No cost computation for vehicle type " + vehicleType);
        }
        return computation;
    }
}