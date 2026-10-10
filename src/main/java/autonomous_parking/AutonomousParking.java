package autonomous_parking;

public class AutonomousParking implements AutonomousParkingInterface {
  
  /* Constants */
  public static final int ROAD_MIN_STRETCH = 0;
  public static final int ROAD_MAX_STRETCH = 500;
  public static final int PARKING_SPOT_LENGTH = 5;
  public static final int MIN_SENSOR_DETECTED_FREE_SPOT = 150;
  
  /* Car and parking lot status */
  ParkingStatus currParkingStatus;
  int freeSpotsLength;

  /* Sensors */
  private IDataSensor sensor1;
  private IDataSensor sensor2;
  private IActuator actuator;

  /* Parking Status */
  FreeSpots currMostEfficientFreeSpot = new FreeSpots(0, 0);

  /* Set sensors and initial car/parking state */
  public AutonomousParking(IDataSensor sensor1, IDataSensor sensor2, IActuator actuator) {
    this.sensor1 = sensor1;
    this.sensor2 = sensor2;
    this.actuator = actuator;

    this.currParkingStatus = ParkingStatus.UNPARKED;
    this.freeSpotsLength = 0;
  }


  /**
   * Description:
   *  - Moves the car forward by 1 meter and checks whether there is an empty space
   *    to the right of the car in the new position.
   *  - If there is free space to the right, freeSpotsLength is incremented by 1;
   *    otherwise, it is reset to zero.
   *  - Checks/Updates the most efficient free spots via`CheckForMostEfficientFreeSpot` method.
   *
   * Inputs:
   * - Checks if there is free space to the right using `IsEmpty` subroutine
   *
   * Outputs:
   * - Returns the parkingState (position and freeSpots)
   *
   * Pre-condition:
   * - car.ParkingState = UNPARKED
   *
   * Post-condition:
   * - carParkingState = UNPARKED
   *
   * Test-cases (Annotated as TC-1, 2, etc):
   *   ______________________________________________________________________________________________________________
   *  | Conditions/Actions                                        | TC-MF-1 | TC-MF-2 | TC-MF-3 | TC-MF-4 | TC-MF-5 |
   *  |-----------------------------------------------------------|---------|---------|---------|---------|---------|
   *  | c1: carParkingState = UNPARKED                            |   True  |   True  |   True  |   True  |   False |
   *  | c2: isEmpty() >= 150cm                                    |   True  |   True  |   False |   False |    -    |
   *  | c3: position == 499                                       |   True  |   False |   True  |   False |    -    |
   *  |-----------------------------------------------------------|---------|---------|---------|---------|---------|
   *  | a1: wrong input/state                                     |    -    |    -    |    -    |    -    |    X    |
   *  | a2: position += 1, freeSpots = 0                          |    -    |    -    |    -    |    X    |    -    |
   *  | a2: position += 1, checkMostEffientSpot(), freeSpots = 0  |    -    |    -    |    X    |    -    |    -    |
   *  | a3: position += 1, freeSpots += 1                         |    -    |    X    |    -    |    -    |    -    |
   *  | a4: position += 1, freeSpots += 1, checkMostEffientSpot() |    X    |    -    |    -    |    -    |    -    |
   *  |___________________________________________________________|_________|_________|_________|_________|_________|
   *
   */
  public FreeSpots MoveForward() {
    CarState carState = this.WhereIs();
    /* Check that the car is not parked */
    if (carState.CurrParkingStatus == ParkingStatus.PARKED) {
      throw new IllegalStateException("Car is already parked");
    }
    
    /* Get prev car position */
    int prevCarPosition = carState.position;

    /* Increment car position */
    this.actuator.UpOneStep();

    /* Increment or reset the detected free space */
    int distanceToClosestObject = this.IsEmpty();
    if (distanceToClosestObject >= MIN_SENSOR_DETECTED_FREE_SPOT) {
      freeSpotsLength += 1;
      /* Check if the current space is the most efficient if you've reached the end of the road */
      int currentCarPosition = this.actuator.GetPosition();
      if (currentCarPosition == ROAD_MAX_STRETCH) {
          this.CheckForMostEfficientFreeSpot(currentCarPosition);
      }
    } else {
      /* Encounter blocking point on the right hand side */
      this.CheckForMostEfficientFreeSpot(prevCarPosition);
      freeSpotsLength = 0;
    }

    return new FreeSpots(this.actuator.GetPosition(), freeSpotsLength);
  }

    /**
   * Description:
   *  - Updates the internal state for the most efficient parking spot, if any
   *
   * Inputs:
   *  - Position of the car
   *  - The number of free/empty 1m spots backwards from the provided position
   *  - Current/Previous most efficient parking spot
   *
   * Outputs:
   * - Updates the current most efficient parking spot.
   *
   * Pre-condition:
   *  - Current most efficient parking spot's length is either zero, or >= 5m.
   *
   * Post-condition:
   *  - Updated most efficient parking spot's length is either zero, or >= 5m.
   *
   * Test-cases (Annotated as TC-1, 2, etc):
   *   ____________________________________________________________________________________________________
   *  | Conditions/Actions                                        | TC-CF-1 | TC-CF-2 | TC-CF-3 | TC-CF-4 |
   *  |-----------------------------------------------------------|---------|---------|---------|---------|
   *  | c2: freeSpotsLen >= 5m                                    |   True  |   True  |   True  |   False |
   *  | c3: currMostEfficientSpot.length                          |   >=5m  |   >=5m  |    0    |    -    |
   *  | c4: freeSpotsLen < currMostEfficientSpot.freeSpotsLen     |   True  |   False |    -    |    -    |
   *  |-----------------------------------------------------------|---------|---------|---------|---------|
   *  | a1: do nothing                                            |    -    |    X    |    -    |    X    |
   *  | a3: currMostEfficientSpot = (position, freeSpotsLen)      |    X    |    -    |    X    |    -    |
   *  |___________________________________________________________|_________|_________|_________|_________|
   *
   */
  void CheckForMostEfficientFreeSpot(int position) {
    if (freeSpotsLength < PARKING_SPOT_LENGTH) {
      return;
    }

    /* Check and register the most efficient parking spot */
    int currentBestLength = currMostEfficientFreeSpot.freeSpotsLength;
    if (currentBestLength == 0 || freeSpotsLength < currentBestLength) {
      currMostEfficientFreeSpot = new FreeSpots(position, freeSpotsLength);
    }
  }

  /**
   * Description:
   * - Query both sensors at least 5 times
   * - Filter noise from each of sensor
   * - Return the distance to the nearest object on the right-hand side of the car
   *
   * Inputs:
   * - int[] SENSOR_DATA1 = {data_1, data_2, data_3, data_4, data_5};
   * - int[] SENSOR_DATA2 = {data_1, data_2, data_3, data_4, data_5};
   *
   * Outputs:
   * - Return a filtered distance value in centimetres
   *
   * Assumptions:
   * - Sensor data will be given as an array of 5 elements
   * - A sensor is considered invalid if its readings have a deviation greater than 80 cm:
   *   + deviation =  Max reading - Min reading
   * - Method to calculate overall sensor data is an average of 5 times
   *
   * Pre-condition:
   * - Both sensors data are available
   * - Sensor data range must be >= 0 and <= 200
   * - The sensors can be queried at least 5 times.
   *
   * Post-condition:
   * - If both sensors are invalid, return -1 
   * - If one sensor is valid, return its filtered data 
   * - If both are valid, return the minimum filtered data of them
   *
   * Test-cases:
   * - Boundary Values
   * - Equivalence Classes
   * - Decision Tables
   *   ____________________________________________________________________________________________________________
   *  | Conditions/Actions                                      |TC_IE_01     | TC_IE_02  | TC_IE_03  | TC_IT_04  |
   *  |---------------------------------------------------------|-------------|-----------|-----------|-----------|
   *  | c1: S1 state                                            |  V          |  V        |  I        |  I        |
   *  | c2: S2 state                                            |  V          |  I        |  V        |  I        |
   *  |---------------------------------------------------------|-------------|-----------|-----------|-----------|
   *  | a1: IsEmpty() = Min(avg(S1), avg(S2))                   |  X          |  -        |  -        |  -        |
   *  | a2: IsEmpty() = S1                                      |  -          |  X        |  -        |  -        |
   *  | a3: IsEmpty() = S2                                      |  -          |  -        |  X        |  -        |
   *  | a4: IsEmpty() = -1                                      |  -          |  -        |  -        |  X        |
   *  |_________________________________________________________|_____________|___________|___________|___________|
   *
   *  V = Valid
   *  I = Invalid
   * 
   * __________________________________________________________________________________________
   * | Conditions/Actions                         | TC_IE_05 | TC_IE_06 | TC_IE_07 | TC_IE_08 |
   * |--------------------------------------------|----------|----------|----------|----------|
   * | c1: S1 average value                       |    < 0   |     0    |   200    |   > 200  |
   * | c2: S2 average value                       |    < 0   |     0    |   200    |   > 200  |
   * |--------------------------------------------|----------|----------|----------|----------|
   * | a3: Sensor state                           | Invalid  |  Valid   |  Valid   | Invalid  |
   * | a1: IsEmpty() = -1                         |    X     |    -     |    -     |    X     |
   * | a2: IsEmpty() = Min(avg(S1), avg(S2))      |    -     |    X     |    X     |    -     |
   * |--------------------------------------------|----------|----------|----------|----------|
   * 
   * _______________________________________________________________________________
   * | Conditions/Actions                         | TC_IE_09 | TC_IE_10 | TC_IE_11 |
   * |--------------------------------------------|----------|----------|----------|
   * | c1: S1 average value                       |    < 80  |    80    |    > 80  |
   * | c2: S2 average value                       |    < 80  |    80    |    > 80  |
   * |--------------------------------------------|----------|----------|----------|
   * | a3: Sensor state                           |  Valid   |  Valid   |  Invalid |
   * | a1: IsEmpty() = -1                         |    X     |    -     |    X     |
   * | a2: IsEmpty() = Min(avg(S1), avg(S2))      |    -     |    X     |    -     |
   * |--------------------------------------------|----------|----------|----------|
   * - (Refer to the Test_Specification.xlsm OR the report for more details)
   */

  public int IsEmpty() {
    int filteredDataSensor1 = -1;
    int filteredDataSensor2 = -1;
    int filteredData = -1;

    /* Read data from sensors */
    sensor1.GetDataSensor();
    sensor2.GetDataSensor();

    /* Filter noise and check if sensor data is in range */
    boolean isSensor1Valid = sensor1.FilterNoise() && sensor1.IsDataInRange();
    boolean isSensor2Valid = sensor2.FilterNoise() && sensor2.IsDataInRange();

    /* Calculate filtered data from valid sensors */
    if (isSensor1Valid) {
        filteredDataSensor1 = sensor1.CalculateData();
    }

    if (isSensor2Valid) {
        filteredDataSensor2 = sensor2.CalculateData();
    }

    /* Determine the final filtered data based on valid sensor readings */
    if (filteredDataSensor1 != -1 && filteredDataSensor2 != -1) {
        filteredData = Math.min(filteredDataSensor1, filteredDataSensor2);
    }
    else if (filteredDataSensor1 != -1) {
        filteredData = filteredDataSensor1;
    }
    else if (filteredDataSensor2 != -1) {
        filteredData = filteredDataSensor2;
    }

    /* Return the filtered data, which represents the distance to the nearest obstacle */
    /* If both sensors are invalid, return -1 
       If one sensor is valid, return its data 
       If both are valid, return the minimum of them */
    return filteredData;
  }

  /**
   * Description:
   * - Moves the car backwards by 1 meter
   * - Checks whether there is empty space to the right of the car
   * - If there is free space, incremented freeSpots by 1; otherwise reset it to zero.
   * 
   * Inputs:
   * - Queries for free space to the right using `IsEmpty` method
   * 
   * Outputs:
   * - Returns the parking state of the car (position and freeSpots detected)
   *
   * Pre-condition:
   * - carParkingState = UNPARKED
   *
   * Post-condition:
   * - carParkingState = UNPARKED
   * 
   * Test-cases (Annotated as TC-1, 2, etc):
   *   __________________________________________________________________
   *  | Conditions/Actions                | TC-MB-1 | TC-MB-2 | TC-MB-3 |
   *  |-----------------------------------|---------|---------|---------|
   *  | c2: carParkingState = UNPARKED    |   True  |   True  |   False |
   *  | c3: isEmpty() >= 150cm            |   True  |   False |    -    |
   *  |-----------------------------------|---------|---------|---------|
   *  | a1: wrong input/state             |    -    |    -    |    X    |
   *  | a2: position -= 1, freeSpots = 0  |    -    |    X    |    -    |
   *  | a3: position -= 1, freeSpots += 1 |    X    |    -    |    -    |
   *  |___________________________________|_________|_________|_________|
  */
  public FreeSpots MoveBackward() {
    CarState carState = this.WhereIs();
    /* Check that the car is not parked */
    if (carState.CurrParkingStatus == ParkingStatus.PARKED) {
      throw new IllegalStateException("Car is already parked");
    }

    /* Decrement car position */
    this.actuator.DownOneStep();

    /* Increment or reset the detected free space */
    int distanceToClosestObject = this.IsEmpty();
    if (distanceToClosestObject >= MIN_SENSOR_DETECTED_FREE_SPOT) {
      freeSpotsLength += 1;
    } else {
      freeSpotsLength = 0;
    }

    return new FreeSpots(this.actuator.GetPosition(), freeSpotsLength);
  }

/**
   * Description:
   *  - Start from the beginning of the street.
   *  - Move forward until 500 m.
   *  - MoveForward() continuously detects free stretches.
   *  - Every completed free stretch ≥ 5 m is considered.
   *  - Check the most efficient spot as the shortest valid stretch.
   *  - After reaching 500 m:
   *  - no valid stretch → return false, remain UNPARKED;
   *  - valid stretch → move backward to the most efficient spot position.
   *  - Set status to PARKED and reset temporary state.
   *
   * Inputs:
   * - currCarPosition
   * - currMostEfficientFreeSpot
   *
   * Outputs:
   * - Return a boolean value
   *    + True: Car is parked succesfully, update parking status to parked
   *    + False: Car is not parked succesfully, update parking status to unparked
   * 
   * Pre-condition:
   * - Car's parking status is currently UNPARKED
   * - Car's position is at the start of the street
   *
   * Post-condition:
   * - If the most sufficient parking spot of at least 5 meters is detected:
   *   + The car moves backward to this spot
   *   + The pre-programmed reverse parallel parking maneuver is performed.
   *   + The car's parking status becomes PARKED.
   *
   * - If not any parking spot is detected at the end of the street:
   *   + The car's position is 500 meters.
   *   + The car's parking status remains UNPARKED.
   *
   * Test-cases:
   * Decision Table for Park()
   *   __________________________________________________________________________________________
   *  | Conditions/Actions                                      | TC_P_01  | TC_P_02  | TC_P_03 |
   *  |---------------------------------------------------------|----------|----------|---------|
   *  | c1: A Valid parking stretch found?                      |  T       |  F       | F       |
   *  | c2: Multiple valid stretches found?                     |  F       |  T       | F       |
   *  |---------------------------------------------------------|----------|----------|---------|
   *  | a1: Stay at 500 m                                       |  -       |  -       | x       |
   *  | a2: Select valid parking stretch                        |  x       |  x       | -       |
   *  | a3: Move backward to selected spot                      |  x       |  x       | -       |
   *  | a4: Final decision PARKED                               |  x       |  x       | -       |
   *  | a5: Final decision UNPARKED                             |  -       |  -       | x       |
   *  |_________________________________________________________|__________|__________|_________|
   * 
   * - (Refer to the Test_Specification.xlsm for more details)
   */
  public boolean Park() {
    /* Keep moving forward reaching a upper road limit */
    while (this.actuator.GetPosition() < ROAD_MAX_STRETCH)
    {
      this.MoveForward(); // move 1m ahead and update car status
    }

    /* Could not find any valid parking spot till the end of the road */ 
    if (currMostEfficientFreeSpot.freeSpotsLength == 0)
    {
      /* Return Park unsuccessfully */
      currParkingStatus = ParkingStatus.UNPARKED;
      return false;
    }

    /* Keep moving backward until parking or reaching a lower road limit */
    while (currMostEfficientFreeSpot.position != this.actuator.GetPosition())
    {
      this.MoveBackward();  // move 1m backward and update car status
    }

    /* Park successfully */
    currParkingStatus = ParkingStatus.PARKED;
    /* Reset free spots length if car is parked successfully */
    freeSpotsLength = 0;
     /* Reset current most efficient free spot */
    currMostEfficientFreeSpot = new FreeSpots(0, 0);
    /* Return  */
    return true;
  }

  /**
   * Description:
   * - Moves the car forward (of the start of the 5m parking stretch) and to the left
   *   of the parking spot.
   * - If the car is already unparked, the above functionality is skipped.
   * 
   * Inputs:
   * - Queries the car's parked state.
   *
   * Outputs:
   * - Modifies the car's parked state (if the car is parked).
   *
   * Assumption:
   * - The car's length is < 5m, such that the extra space offers wiggle room to unpark
   *   and maintain the initial position before parking
   *
   * Pre-condition:
   * - carState.position >= 1
   *
   * Post-condition:
   * - carParkingState = UNPARKED
   * - carState.position remains unchanged.
   *
   * Test-cases (Annotated as TC-1, 2, etc):
   *   _________________________________________________________________________________________
   *  | Conditions/Actions                                        | TC-UP-1 | TC-UP-2 | TC-UP-3 |
   *  |-----------------------------------------------------------|---------|---------|---------|
   *  | c1: position >= 1                                         |   True  |   True  |   False |
   *  | c1: carParkingState = PARKED                              |   True  |   False |    -    |
   *  |-----------------------------------------------------------|---------|---------|---------|
   *  | a1: wrong input/state                                     |    -    |    -    |    X    |
   *  | a2: do nothing                                            |    -    |    X    |    -    |
   *  | a3: carParkingState = UNPARKED, car.position is the same  |    X    |    -    |    -    |
   *  |___________________________________________________________|_________|_________|_________|
   *
   */
  public void UnPark() {
    /* Check lower bound of position (`WhereIs` method checks the upper bound) */
    CarState carState = this.WhereIs();
    if (carState.position < 1) {
      throw new IllegalStateException("Invalid car position");
    }

    /* Update the unparked status only if the car is parked */
    if (carState.CurrParkingStatus == ParkingStatus.PARKED) {
      this.currParkingStatus = ParkingStatus.UNPARKED;
    }
  }

/**
 * Description
 * - Returns the current position of the car in the street as well as its (un)parked status.
 *    Inputs:
 *      - Access the car's position that is an attribute of the AutonomousParking class.
 *      - Access the car's status with is an attribute of the AutonomousParking class.
 *
 *    Outputs:
 *      - Returns the car's current position and parking status through the data type CarState.
 *
 * Pre-condition:
 * - Car's position must be within the defined range 0 <= currCarPosition <= 500
 * - Parking status could be either PARKED or UNPARKED.
 *
 * Post-condition:
 * - Updated car's position (0 <= currCarPosition <= 500) and parking status (PARKED or UNPARKED)
 *
 * Test-cases: Combination of Decision Table + Boundary value conditions
 * - Decision Table
 *  _______________________________________________________________________________________
 * | Conditions/Actions                               |   TC-WI-01   TC-WI-01   TC-WI-01   |
 * |--------------------------------------------------|------------------------------------|
 * | c1: Position valid? 0 <= currCarPosition <= 500  |     F           T          T       |
 * | c2: Parking status = PARKED?                     |    -            T          F       |
 * |--------------------------------------------------|------------------------------------|
 * | a1: wrong input/state                            |    X            -          -       |
 * | a2: currCarPosition                              |    -            X          X       |
 * | a3: currentParkingStatus = PARKED                |    -            X          -       |
 * | a4: currentParkingStatus = UNPARKED              |    -            -          X       |
 * |__________________________________________________|____________________________________|
 *
 * - Boundary value condition
 *  _________________________________________________________
 * | VARIABLE           |   TC-WI-01   TC-WI-01   TC-WI-01  |
 * |--------------------|-----------------------------------|
 * | currCarPosition    |     < 0        100         > 500  |
 * |--------------------|-----------------------------------|
 * | MethodState output |   Invalid     Valid     Invalid   |
 * ---------------------------------------------------------|
*/
  public CarState WhereIs() {
    int position = this.actuator.GetPosition();
    if (position < 0 || position > 500){
      throw new IllegalStateException("Invalid car position");
    }
    return new CarState(position , currParkingStatus);

  }
}
