package autonomous_parking;

public interface AutonomousParkingInterface {
  public FreeSpots MoveForward();
  public int IsEmpty(); 
  public FreeSpots MoveBackward();
  public boolean Park();
  public void UnPark();
  public CarState WhereIs();
}
