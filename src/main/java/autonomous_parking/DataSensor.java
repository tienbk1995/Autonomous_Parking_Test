package autonomous_parking;

public class DataSensor implements IDataSensor {

    public static final int SENSOR_MIN_VALUE = 0;
    public static final int SENSOR_MAX_VALUE = 200;
    public static final int SENSOR_DEVIATION_THRESHOLD = 80;

    protected int[] sensorData;

    public DataSensor() {
        this.sensorData = new int[] {-1, -1, -1, -1, -1};
    }

    public void Read() {
         // Used for testing purposes;
         this.sensorData = new int[] {101, 100, 102, 104, 105};
    }

    public int[] GetDataSensor() {
        Read();
        return this.sensorData;
    }

    public boolean IsDataInRange(int[] sensorData) {
        for (int i = 0; i < sensorData.length; i++) {
            if (sensorData[i] < SENSOR_MIN_VALUE || sensorData[i] > SENSOR_MAX_VALUE) {
                return false;
            }
        }
        return true;
    }

    public boolean FilterNoise(int[] sensorData) {
        // Implementation for filtering noise from sensor data
        int maxValData = sensorData[0];
        int minValData = sensorData[0];

        for (int n : sensorData) {
            if (n > maxValData) {
                maxValData = n;
            }
            if (n < minValData) {
                minValData = n;
            }
        }

        if ((maxValData - minValData) > SENSOR_DEVIATION_THRESHOLD) {
            return false;
        }
        
        return true;
    }

    public int CalculateData(int[] sensorData) {

        int sum = 0;

        for (int i = 0; i < sensorData.length; i++) {
            sum += sensorData[i];
        }

        return (int)(sum/sensorData.length);
    }

}
