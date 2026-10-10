package autonomous_parking.Integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import autonomous_parking.Actuator;
import autonomous_parking.AutonomousParking;
import autonomous_parking.AutonomousParkingInterface;
import autonomous_parking.IActuator;
import autonomous_parking.IDataSensor;
import autonomous_parking.ParkingStatus;
import autonomous_parking.CarState;
import autonomous_parking.DataSensor;


/* Second Scenario: Could not find any sufficient parking slots
    • Starts at the beginning of the street,
    • Moves along the street and continuously scans the available parking spaces,
    • Encounters 3 parking spaces that are insufficient safe parking,
    • Does not find any parking space with at least 5 meters of continuous free space,
    • Reaches the end of the street without parking,
    • Remains unparked.
*/

class TestScenario_2 {
    
    private static final int[] FREE = {180, 180, 180, 180, 180};

    private static final int[] BLOCKED = {100, 100, 100, 100, 100};

    private static final int[] NOISE = {150, 100, 100, 100, 10};

    private static final int[] OUTOFRANGE = {-1, 220, 10, 500, 20};

    private IDataSensor createMockSensor(int[]... sequence) 
    {
        IDataSensor sensor = spy(new DataSensor());
            
        AtomicInteger index = new AtomicInteger(0);

        int[][] currentData = {sequence[0]};

        when(sensor.GetDataSensor())
            .thenAnswer(invocation -> {
                int i = index.getAndIncrement();

                if (i < sequence.length) {
                    currentData[0] = sequence[i];
                    sensor.Read(currentData[0]);
                }

                return invocation.callRealMethod();
            });
        return sensor;
    }

    private int [][] createRoadConditions(
        int [][] free,
        int [][] noise,
        int [][] ofoutrange
        )
    {
        int roadLength = 500;
        int[][] roadConditions = new int[roadLength][5];

        // Default everything to BLOCKED
        for (int i = 0; i < roadLength; i++) {
            roadConditions[i] = BLOCKED.clone();
        }

        // FREE ranges
        for (int[] range : free) {
            for (int pos = range[0]; pos <= range[1]; pos++) {
                roadConditions[pos - 1] = FREE.clone();
            }
        }

        // NOISE ranges
        for (int[] range : noise) {
            for (int pos = range[0]; pos <= range[1]; pos++) {
                roadConditions[pos - 1] = NOISE.clone();
            }
        }

        // of out range ranges
        for (int[] range : ofoutrange) {
            for (int pos = range[0]; pos <= range[1]; pos++) {
                roadConditions[pos - 1] = OUTOFRANGE.clone();
            }
        }

        return roadConditions;
    }

    @Test
    void testCouldNotFindAnySpots() {

        /* Starting at the beginning of the street */
        IActuator actuator = new Actuator();

        /* Encounters 3 parking spaces that are insufficient safe parking */
        int[][] road_1 = createRoadConditions(
            new int[][] { // Free blocks
                {120, 123}, // 4
                {201, 203}, // 4
                {427, 429}, // 3
            },
            new int[][] { // Noise blocks
                {1, 2}, // 2
                {20, 23}, // 4
                {99, 110}, // 12
            },
            new int[][] { // Out of range blocks
                {20, 25}, //6
            }
        );

        int[][] road_2 = createRoadConditions(
            new int[][] { // Free blocks
                {120, 123}, // 4
                {201, 203}, // 4
                {427, 429}, // 3
            },
            new int[][] { // Noise blocks
                {1, 2}, // 2
                {20, 23}, // 4
                {99, 110}, // 12
            },
            new int[][] { // Out of range blocks
                {20, 25}, //6
            }
        );

        IDataSensor sensor1 = createMockSensor(road_1);
        IDataSensor sensor2 = createMockSensor(road_2);
        AutonomousParkingInterface car =  new AutonomousParking(sensor1, sensor2, actuator);

        /* Does not find any parking space with at least 5 meters of continuous free space */
        boolean parked = car.Park();

        CarState carState = car.WhereIs();

        /* Reaches the end of the street without parking */
        assertEquals(500, carState.getPosition());

        /* Remains unparked */
        assertEquals(false, parked);
        assertEquals(ParkingStatus.UNPARKED, carState.getParkingStatus());


    }
}