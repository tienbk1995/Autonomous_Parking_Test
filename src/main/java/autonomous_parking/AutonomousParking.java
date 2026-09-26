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
  private FreeSpots currMostEfficientFreeSpot = new FreeSpots(0, 0);

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
   *
   * Inputs:
   * - Queries for the car's current position
   * - Checks if there is free space to the right using `IsEmpty` subroutine
   *
   * Outputs:
   * - Returns the parkingState (position and freeSpots)
   *
   * Assumptions:
   * - This method is called by the `Park()` method only when the detected
   *   freeSpots are not enough to park the car (< 5m).
   *
   * Pre-condition:
   * - 0 <= carState.position <= 499
   * - car.ParkingState = UNPARKED
   *
   * Post-condition:
   * - 1 <= carState.position <= 500
   * - carParkingState = UNPARKED
   *
   * Test-cases (Annotated as TC-1, 2, etc):
   *   ___________________________________________________________________________
   *  | Conditions/Actions                | TC-MF-1 | TC-MF-2 | TC-MF-3 | TC-MF-4 |
   *  |-----------------------------------|---------|---------|---------|---------|
   *  | c1: 0 <= position <= 499          |   True  |   True  |   True  |  False  |
   *  | c2: carParkingState = UNPARKED    |   True  |   True  |   False |    -    |
   *  | c3: isEmpty() >= 150cm            |   True  |   False |     -   |    -    |
   *  |-----------------------------------|---------|---------|---------|---------|
   *  | a1: wrong input/state             |    -    |    -    |    X    |    X    |
   *  | a2: position += 1, freeSpots = 0  |    -    |    X    |    -    |    -    |
   *  | a3: position += 1, freeSpots += 1 |    X    |    -    |    -    |    -    |
   *  |___________________________________|_________|_________|_________|_________|
   *
   */
  public FreeSpots MoveForward() {
    /* Check that the car position is still in range (0 to 499) */
    CarState carState = this.WhereIs();
    /* Get prev car position */
    int prevCarPosition = this.actuator.GetPosition();

    /* Check that the car is not parked */
    if (carState.CurrParkingStatus == ParkingStatus.PARKED) {
      throw new IllegalStateException("Car is already parked");
    }
    /* Update prev car position */
    prevCarPosition = this.actuator.GetPosition();

    /* Increment car position */
    this.actuator.UpOneStep();

    /* Increment or reset the detected free space */
    int distanceToClosestObject = this.IsEmpty();
    if (distanceToClosestObject >= MIN_SENSOR_DETECTED_FREE_SPOT) {
      freeSpotsLength += 1;
    } else { /* Encounter blocking point on the right hand side */
      /* Check and register the most Efficient parking spot */
      if (freeSpotsLength >= PARKING_SPOT_LENGTH)
      {
        if ((currMostEfficientFreeSpot.freeSpotsLength == 0) || 
            (currMostEfficientFreeSpot.freeSpotsLength > freeSpotsLength))
        {
          /* Registering the current most efficient parking spot */
          currMostEfficientFreeSpot = new FreeSpots(prevCarPosition, freeSpotsLength);
        }
      }
      /* Resetting the freeSpotsLength due to obstruction */
      freeSpotsLength = 0;
    }

    /* Update for several last freeSpotsLength before reaching the end of road without encountering any obstruction */
    if ((this.actuator.GetPosition() == ROAD_MAX_STRETCH) && 
        (freeSpotsLength >= PARKING_SPOT_LENGTH) &&
        ((freeSpotsLength < currMostEfficientFreeSpot.freeSpotsLength) ||
        (currMostEfficientFreeSpot.freeSpotsLength == 0))) {
          
        currMostEfficientFreeSpot = new FreeSpots(this.actuator.GetPosition(), freeSpotsLength); // Update currMostEfficientFreeSpot
    }

    return new FreeSpots(this.actuator.GetPosition(), freeSpotsLength);
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
   * | a3: Sensor state                           |  Invalid |  Valid   |  Invalid |
   * | a1: IsEmpty() = -1                         |    X     |    -     |    X     |
   * | a2: IsEmpty() = Min(avg(S1), avg(S2))      |    -     |    X     |    -     |
   * |--------------------------------------------|----------|----------|----------|
   * - (Refer to the Test_Specification.xlsm OR the report for more details)
   */

  public int IsEmpty() {
    int filteredDataSensor1 = -1;
    int filteredDataSensor2 = -1;
    int filteredData = -1;
    int[] s1Data = new int[] {-1, -1, -1, -1, -1};
    int[] s2Data = new int[] {-1, -1, -1, -1, -1};

    /* Read data from sensors */
    s1Data = sensor1.GetDataSensor();
    s2Data = sensor2.GetDataSensor();

    /* Filter noise and check if sensor data is in range */
    boolean isSensor1Valid = sensor1.FilterNoise(s1Data) && sensor1.IsDataInRange(s1Data);
    boolean isSensor2Valid = sensor2.FilterNoise(s2Data) && sensor2.IsDataInRange(s2Data);

    /* Calculate filtered data from valid sensors */
    if (isSensor1Valid) {
        filteredDataSensor1 = sensor1.CalculateData(s1Data);
    }

    if (isSensor2Valid) {
        filteredDataSensor2 = sensor2.CalculateData(s2Data);
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
   * - Queries for the car's current position
   * - Queries for free space to the right using `IsEmpty` method
   * 
   * Outputs:
   * - Returns the parking state of the car (position and freeSpots detected)
   *
   * Pre-condition:
   * - 1 <= position <= 500
   * - carParkingState = UNPARKED
   *
   * Post-condition:
   * - 0 <= postion <= 499
   * - carParkingState = UNPARKED
   * 
   * Test-cases (Annotated as TC-1, 2, etc):
   *   ___________________________________________________________________________
   *  | Conditions/Actions                | TC-MB-1 | TC-MB-2 | TC-MB-3 | TC-MB-4 |
   *  |-----------------------------------|---------|---------|---------|---------|
   *  | c1: 1 <= position <= 500          |   True  |   True  |   True  |  False  |
   *  | c2: carParkingState = UNPARKED    |   True  |   True  |   False |    -    |
   *  | c3: isEmpty() >= 150cm            |   True  |   False |    -    |    -    |
   *  |-----------------------------------|---------|---------|---------|---------|
   *  | a1: wrong input/state             |    -    |    -    |    X    |    X    |
   *  | a2: position -= 1, freeSpots = 0  |    -    |    X    |    -    |    -    |
   *  | a3: position -= 1, freeSpots += 1 |    X    |    -    |    -    |    -    |
   *  |___________________________________|_________|_________|_________|_________|
  */
  public FreeSpots MoveBackward() {
    /* Check that the car position is still in range (1 to 500) */
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
   * - Perform a pre-programmed reverse parallel parking maneuver
   * - Park when car in the free parking spot position
   * - Otherwise move the car forwards toward the end of the stretch until free parking spot is found
   *
   * Inputs:
   * - currCarPosition
   * - freeSpotsLength
   *
   * Outputs:
   * - Return a boolean value
   *    + True: Car is parked succesfully
   *    + False: Car is not parked succesfully
   *
   * Assumptions:
   * - A valid parking space is a continuous free stretch of at least 5 meters.
   * - The car detects parking availability while moving forward using isEmpty().
   * - The car is considered to be at a ready parking space when a valid 5-meter
   *   free stretch has been detected.
   * - The parking maneuver is pre-programmed and does not require sensor
   *   feedback during the maneuver.
   * - The actuator signals used to perform the parking maneuver are not
   *   modeled in this phase.
   * - If no suitable parking space is found before the end of the street,
   *   the car remains unparked.
   *
   * Pre-condition:
   * - Car's parking status is currently UNPARKED
   * - Car's position is in the range between 0 and 500 meters of the stretch
   *
   * Post-condition:
   * - If a suitable free parking stretch of at least 5 meters is detected:
   *   + The car moves to the end of the stretch.
   *   + The pre-programmed reverse parallel parking maneuver is performed.
   *   + The car's parking status becomes PARKED.
   *
   * - If no suitable parking stretch is detected before the end of the street:
   *   + The car's position is 500 meters.
   *   + The car's parking status remains UNPARKED.
   *
   * Test-cases:
   * Decision Table for Park()
   *
   *   __________________________________________________________________________________________
   *  | Conditions/Actions                                      | TC_P_01  | TC_P_02  | TC_P_03 |
   *  |---------------------------------------------------------|----------|----------|---------|
   *  | c1: Already at valid parking stretch?                   |  T       |  F       | F       |
   *  | (Car can park, no need to move forward)                 |          |          |         |
   *  | c2: Valid stretch found ahead?                          |  -       |  T       | F       |
   *  | (Car will move forward and a guaranteed free parking spot before reaching the end)      |
   *  | c3: End of street reached?                              |  -       |  F       | T       |
   *  | (Car will move forward but no free parking spot is found at the end of the stretch)     |
   *  |---------------------------------------------------------|----------|----------|---------|
   *  | a1: Move forward                                        |  -       |  X       | X       |
   *  | a2: Final status = PARKED                               |  X       |  X       | -       |
   *  | a3: Final status = UNPARKED                             |  -       |  -       | X       |
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
   * - 1 <= carState.position <= 500
   *
   * Post-condition:
   * - carParkingState = UNPARKED
   * - carState.position remains unchanged.
   *
   * Test-cases (Annotated as TC-1, 2, etc):
   *   _________________________________________________________________________________________
   *  | Conditions/Actions                                        | TC-UP-1 | TC-UP-2 | TC-UP-3 |
   *  |-----------------------------------------------------------|---------|---------|---------|
   *  | c1: 1 <= position <= 500                                  |   True  |   True  |   False |
   *  | c1: carParkingState = PARKED                              |   True  |   False |    -    |
   *  |-----------------------------------------------------------|---------|---------|---------|
   *  | a1: wrong input/state                                     |    -    |    -    |    X    |
   *  | a2: do nothing                                            |    -    |    X    |    -    |
   *  | a3: carParkingState = UNPARKED, car.position is the same  |    X    |    -    |    -    |
   *  |___________________________________________________________|_________|_________|_________|
   *
   */
  public void UnPark() {
    /* Check that the car position is still in range (0 to 500) */
    CarState carState = this.WhereIs();
    if (carState.position < 1 || carState.position > 500) {
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
    if (this.actuator.GetPosition() < 0 || this.actuator.GetPosition() > 500){
      throw new IllegalStateException("Invalid car position");
    }
    return new CarState(this.actuator.GetPosition() , currParkingStatus);

  }
}
