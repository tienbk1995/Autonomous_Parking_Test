package autonomous_parking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MockDataSensorIsEmpty extends DataSensor {

    int testCount = 0;
    int[][] sensorDataSets;

    public MockDataSensorIsEmpty(int[][] sensorDataSets)
    {
        this.sensorData = sensorDataSets[0];
        this.sensorDataSets = sensorDataSets;
    }

    @Override public void Read() {
        sensorData = new int[] {-1, -1, -1, -1, -1};
        if (testCount < sensorDataSets.length)
        {
            sensorData = sensorDataSets[testCount];
            testCount++;
        }
    }

}

public class TestPark {

    @Test // Example
    void TestExample() {

        int[][] sensorData1SetsExp = {
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},                           
                            };
        int[][] sensorData2SetsExp = {
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            };

        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1SetsExp);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2SetsExp);
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2);
        Car.currCarPosition = 10;
        boolean doPark = Car.Park();
        assertEquals(0, Car.freeSpotsLength);

        assertEquals(true, doPark);
        assertEquals(15, Car.currCarPosition);
        assertEquals(ParkingStatus.PARKED, Car.currParkingStatus);
        assertEquals(5, ((MockDataSensorIsEmpty)sensor1).testCount);

        int[] expected = {180, 180, 180, 180, 180};
        assertArrayEquals(expected, ((MockDataSensorIsEmpty)sensor1).sensorData);
    }
        
    @Test // TC_P_01
    void TestCarAtValidPosition() {
        int[][] sensorData1Sets = {
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},                           
                            };
        int[][] sensorData2Sets = {
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            };

        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2);
        
        Car.freeSpotsLength = 5;
        Car.currCarPosition = 50;

        Car.Park();

        assertEquals(50, Car.currCarPosition);
        assertEquals(0, Car.freeSpotsLength);
        assertEquals(ParkingStatus.PARKED, Car.currParkingStatus);
    }
    @Test // TC_P_02
    void TestCarAtInvalidPositionButFound() {
        int[][] sensorData1Sets = {
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {100, 100, 100, 100, 100},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},  
                            {180, 180, 180, 180, 180},      
                            {180, 180, 180, 180, 180},      
                            {180, 180, 180, 180, 180},
                            };
        int[][] sensorData2Sets = {
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {100, 100, 100, 100, 100},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},      
                            {180, 180, 180, 180, 180},      
                            {180, 180, 180, 180, 180},      
                            {180, 180, 180, 180, 180},                              
                            };

        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2);
        
        Car.freeSpotsLength = 0;
        Car.currCarPosition = 50;

        Car.Park();

        assertEquals(59, Car.currCarPosition);
        assertEquals(0, Car.freeSpotsLength);
        assertEquals(ParkingStatus.PARKED, Car.currParkingStatus);
    }
    @Test // TC_P_03
    void TestCarAtInvalidPositionButNotFound() {
        int[][] sensorData1Sets = {
                            {100, 100, 100, 100, 100},
                            {100, 100, 100, 100, 100},
                            {100, 100, 100, 100, 100},
                            {100, 100, 100, 100, 100},
                            {100, 100, 100, 100, 100},
                            };
        int[][] sensorData2Sets = {
                            {100, 100, 100, 100, 100},
                            {100, 100, 100, 100, 100},
                            {100, 100, 100, 100, 100},
                            {100, 100, 100, 100, 100},
                            {100, 100, 100, 100, 100},
                            };

        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2);
        
        Car.freeSpotsLength = 0;
        Car.currCarPosition = 50;

        Car.Park();

        assertEquals(500, Car.currCarPosition);
        assertEquals(0, Car.freeSpotsLength);
        assertEquals(ParkingStatus.UNPARKED, Car.currParkingStatus);
    }
}
