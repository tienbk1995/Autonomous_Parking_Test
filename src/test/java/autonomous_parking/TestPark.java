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
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        for (int i = 0; i < 10; i++) actuator.UpOneStep();
        boolean doPark = Car.Park();
        assertEquals(0, Car.freeSpotsLength);

        assertEquals(true, doPark);
        assertEquals(15, actuator.GetPosition());
        assertEquals(ParkingStatus.PARKED, Car.currParkingStatus);
        assertEquals(5, ((MockDataSensorIsEmpty)sensor1).testCount);
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
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        
        //Car.freeSpotsLength = 5;
        for (int i = 0; i < 50; i++) actuator.UpOneStep();

        Car.Park();

        assertEquals(55, actuator.GetPosition());
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
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        
        //Car.freeSpotsLength = 0;
        for (int i = 0; i < 50; i++) actuator.UpOneStep();

        Car.Park();

        assertEquals(59, actuator.GetPosition());
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
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);

        Car.freeSpotsLength = 0;
        for (int i = 0; i < 50; i++) actuator.UpOneStep();

        Car.Park();

        assertEquals(500, actuator.GetPosition());
        assertEquals(0, Car.freeSpotsLength);
        assertEquals(ParkingStatus.UNPARKED, Car.currParkingStatus);
    }
}
