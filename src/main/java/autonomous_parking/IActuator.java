package autonomous_parking;

public interface IActuator {
    void UpOneStep();
    void DownOneStep();
    int GetPosition();
}
