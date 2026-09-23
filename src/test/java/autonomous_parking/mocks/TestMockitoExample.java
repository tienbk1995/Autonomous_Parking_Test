package autonomous_parking.mocks;

import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import autonomous_parking.IDataSensor;

class TestMockitoExample {

    @Test
    void testMockito() {

        IDataSensor sensor = mock(IDataSensor.class);

        when(sensor.GetDataSensor()).thenReturn(new int[] {101, 100, 102, 104, 101});

        System.out.println(Arrays.toString(sensor.GetDataSensor()));    
    }
}