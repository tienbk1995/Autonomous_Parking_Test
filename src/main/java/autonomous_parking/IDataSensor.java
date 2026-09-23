package autonomous_parking;

public interface IDataSensor {
    public void Read();
    public int[] GetDataSensor();
    public boolean IsDataInRange();
    public boolean FilterNoise();
    public int CalculateData();
}
