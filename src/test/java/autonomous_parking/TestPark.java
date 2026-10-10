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

    @Override public void Read(int[] sensorData) 
    {
        this.sensorData = new int[] {-1, -1, -1, -1, -1};
        if (testCount < sensorDataSets.length)
        {
            this.sensorData = this.sensorDataSets[testCount];
            testCount++;
        }
    }
    
    @Override public int[] GetDataSensor() {
        this.Read(sensorData);
        return this.sensorData;
    }
}

public class TestPark {

    @Test // TC_P_01
    void TestCarFoundOneValidPosition() {
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

        Car.Park();

        assertEquals(5, actuator.GetPosition());
        assertEquals(ParkingStatus.PARKED, Car.currParkingStatus);
    }
    @Test // TC_P_02
    void TestCarFoundMultipleValidPosition() {
        int[][] sensorData1Sets = {
                            {180, 180, 180, 180, 180}, //1
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},

                            {100, 100, 100, 100, 100},

                            {180, 180, 180, 180, 180},
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
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},

                            {100, 100, 100, 100, 100},

                            {180, 180, 180, 180, 180},
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

        Car.Park();

        assertEquals(14, actuator.GetPosition());
        assertEquals(ParkingStatus.PARKED, Car.currParkingStatus);
    }
    @Test // TC_P_03
    void TestCarFoundNoValidPosition() {
        int[][] sensorData1Sets = {
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},

                            {100, 100, 100, 100, 100},

                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},  
                            {180, 180, 180, 180, 180},              
                            };
        int[][] sensorData2Sets = {
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},

                            {100, 100, 100, 100, 100},

                            {180, 180, 180, 180, 180},
                            {180, 180, 180, 180, 180},  
                            {180, 180, 180, 180, 180},         
                            };

        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);

        Car.Park();

        assertEquals(500, actuator.GetPosition());
        assertEquals(ParkingStatus.UNPARKED, Car.currParkingStatus);
    }
}
