package autonomous_parking;

public interface IDataSensor {
    public void Read();
    public int[] GetDataSensor();
    public boolean IsDataInRange(int[] sensorData);
    public boolean FilterNoise(int[] sensorData);
    public int CalculateData(int[] sensorData);
}
