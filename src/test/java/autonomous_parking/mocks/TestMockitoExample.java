package autonomous_parking.mocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import autonomous_parking.AutonomousParking;
import autonomous_parking.AutonomousParkingInterface;
import autonomous_parking.IActuator;
import autonomous_parking.IDataSensor;

class TestMockitoExample {

    @Test
    void testMockito() {

        IDataSensor sensor1 = mock(IDataSensor.class);

        IDataSensor sensor2 = mock(IDataSensor.class);

        IActuator actuator = mock(IActuator.class);

        int[] S1_Data = new int[] {200, 200, 200, 200, 200};
        int[] S2_Data = new int[] {180, 180, 180, 180, 180};
        //S1
        when(sensor1.GetDataSensor()).thenReturn(S1_Data);

        when(sensor1.FilterNoise(S1_Data)).thenReturn(true);

        when(sensor1.IsDataInRange(S1_Data)).thenReturn(true);

        when(sensor1.CalculateData(S1_Data)).thenReturn(200);

        //S2
        when(sensor2.GetDataSensor()).thenReturn(S2_Data);

        when(sensor2.FilterNoise(S2_Data)).thenReturn(true);

        when(sensor2.IsDataInRange(S2_Data)).thenReturn(true);

        when(sensor2.CalculateData(S2_Data)).thenReturn(180);

        AutonomousParkingInterface car =  new AutonomousParking(sensor1, sensor2, actuator);

        int distance = car.IsEmpty();

        assertEquals(180, distance);

        car.Park();

        verify(sensor1, atLeastOnce()).GetDataSensor();
        verify(sensor2, atLeastOnce()).GetDataSensor();

        verify(sensor1, atLeastOnce()).FilterNoise(S1_Data);
        verify(sensor2, atLeastOnce()).FilterNoise(S2_Data);

        System.out.println(Arrays.toString(sensor1.GetDataSensor()));    
    }
}