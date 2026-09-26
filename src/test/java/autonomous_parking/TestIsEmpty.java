package autonomous_parking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIsEmpty {
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

    @Test // TC_IE_01
    void TestBothNormalSensorData() {
        int[][] sensorData1Sets = {
                            {101, 100, 102, 104, 105},                         
                            };
        int[][] sensorData2Sets = {
                            {10, 20, 30, 40, 50},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(30, filteredSensorData);

    }
    @Test // TC_IE_02
    void TestAbnormalSensorData1() {
        int[][] sensorData1Sets = {
                            {10, 100, 25, 44, 88},                    
                            };
        int[][] sensorData2Sets = {
                            {101, 100, 102, 104, 105},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(102, filteredSensorData);

    }
    @Test // TC_IE_03
    void TestAbnormalSensorData2() {
        int[][] sensorData1Sets = {
                            {150, 120, 155, 145, 160},                    
                            };
        int[][] sensorData2Sets = {
                            {10, 100, 25, 44, 88},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(146, filteredSensorData);

    }

    @Test // TC_IE_04
    void TestAbnormalBothSensorData() {
        int[][] sensorData1Sets = {
                            {111, 22, 35, 12, 180},                    
                            };
        int[][] sensorData2Sets = {
                            {10, 100, 25, 44, 88},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(-1, filteredSensorData);

    }
    @Test // TC_IE_05
    void TestNormalBDBothSensorData1() {
        int[][] sensorData1Sets = {
                            {0, 1, 0, 1, 0},                    
                            };
        int[][] sensorData2Sets = {
                            {1, 0, 1, 0, 1},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(0, filteredSensorData);

    }
    @Test // TC_IE_06
    void TestNormalBDBothSensorData2() {
        int[][] sensorData1Sets = {
                            {200, 200, 200, 200, 200},                    
                            };
        int[][] sensorData2Sets = {
                            {200, 200, 200, 200, 200},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(200, filteredSensorData);

    }
    @Test // TC_IE_07
    void TestAbnormalBDBothSensorData1() {
        int[][] sensorData1Sets = {
                            {-1, -2, -3, -1, 0},                    
                            };
        int[][] sensorData2Sets = {
                            {-1, -2, -3, -1, 0},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(-1, filteredSensorData);

    }
    @Test // TC_IE_08
    void TestAbnormalBDBothSensorData2() {
        int[][] sensorData1Sets = {
                            {201, 200, 230, 212, 211},                    
                            };
        int[][] sensorData2Sets = {
                            {198, 215, 220, 215, 199},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(-1, filteredSensorData);

    }

    @Test // TC_IE_09
    void TestNormalBDBothSensorDataNoiseLess() {
        int[][] sensorData1Sets = {
                            {97, 99, 120, 99, 111},                    
                            };
        int[][] sensorData2Sets = {
                            {100, 170, 150, 165, 177},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(105, filteredSensorData);

    }
    @Test // TC_IE_10
    void TestNormalBDBothSensorDataNoiseEqual() {
        int[][] sensorData1Sets = {
                            {97, 99, 120, 99, 111},                    
                            };
        int[][] sensorData2Sets = {
                            {180, 170, 150, 165, 100},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(105, filteredSensorData);

    }
    @Test // TC_IE_10
    void TestAbnormalBDBothSensorDataNoise() {
        int[][] sensorData1Sets = {
                            {97, 99, 177, 60, 111},                    
                            };
        int[][] sensorData2Sets = {
                            {180, 170, 150, 165, 90},
                            };
        IDataSensor sensor1 = new MockDataSensorIsEmpty(sensorData1Sets);
        IDataSensor sensor2 = new MockDataSensorIsEmpty(sensorData2Sets);
        IActuator actuator = new Actuator();
        AutonomousParking Car = new AutonomousParking(sensor1, sensor2, actuator);
        int filteredSensorData = Car.IsEmpty();

        assertEquals(-1, filteredSensorData);

    }
}