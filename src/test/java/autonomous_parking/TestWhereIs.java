package autonomous_parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import autonomous_parking.mocks.MockDataSensor;

public class TestWhereIs {
    @Test //TC-WI-01
    void WhereIsRejectsInvalidPosition() {
        AutonomousParking car = new AutonomousParking(
                new MockDataSensor(new int[] {101, 100, 102, 104, 105}),
                new MockDataSensor(new int[] {103, 100, 101, 99, 98}) );
        car.currCarPosition = -1;
        IllegalStateException error = assertThrows( IllegalStateException.class, () -> car.WhereIs() );
        assertEquals("Invalid car position", error.getMessage());

        car.currCarPosition = 501;
        error = assertThrows( IllegalStateException.class, () -> car.WhereIs() );
        assertEquals("Invalid car position", error.getMessage());
        System.out.println(error.getMessage());
    }

    @Test //TC-WI-02
    void WhereIsReturnsParkedState(){
        AutonomousParking car = new AutonomousParking(
                new MockDataSensor(new int[] {101, 100, 102, 104, 105}),
                new MockDataSensor(new int[] {103, 100, 101, 99, 98}) );

        car.currCarPosition = 100;
        car.currParkingStatus = ParkingStatus.PARKED;

        CarState state = car.WhereIs();

        assertEquals(100, state.position);
        assertEquals(ParkingStatus.PARKED, state.CurrParkingStatus);
    }

    @Test //TC-WI-03
    void WhereIsReturnsUnparkedState(){
        AutonomousParking car = new AutonomousParking(
                new MockDataSensor(new int[] {101, 100, 102, 104, 105}),
                new MockDataSensor(new int[] {103, 100, 101, 99, 98}) );

        car.currCarPosition = 100;
        car.currParkingStatus = ParkingStatus.UNPARKED;

        CarState state = car.WhereIs();

        assertEquals(100, state.position);
        assertEquals(ParkingStatus.UNPARKED, state.CurrParkingStatus);
    }
}