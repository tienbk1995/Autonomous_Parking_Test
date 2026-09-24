package autonomous_parking;

public class Actuator implements IActuator {

    int currCarPosition;

    public Actuator(){
        this.currCarPosition = 0;
    }

    public void UpOneStep(){
        if (currCarPosition < 0 || currCarPosition > 499) {
            throw new IllegalStateException("Invalid car position");
        }
        currCarPosition += 1;
    }

    public void DownOneStep(){
        if (currCarPosition < 1 || currCarPosition > 500) {
            throw new IllegalStateException("Invalid car position");
        }
        currCarPosition -= 1;
    }

    public int GetPosition() {
        return currCarPosition;
    }

    public void SetPosition(int position) {
        this.currCarPosition = position;
    }

}
