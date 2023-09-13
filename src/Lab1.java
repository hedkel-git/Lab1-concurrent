import TSim.*;

import static TSim.TSimInterface.*;

import java.util.concurrent.Semaphore;

public class Lab1 {

    TSimInterface tsi = getInstance();

    //--The semaphores used for the different zones.--//
    private final Semaphore zone1sem = new Semaphore(1, true);
    private final Semaphore zone2_3sem = new Semaphore(1, true);
    private final Semaphore zone4_5sem = new Semaphore(1, true);

    //--The semaphores used for the possible shortcuts.--//

    //upper path lies between critical zone 2 and the shortest path to the station above to it.
    private final Semaphore upperPath = new Semaphore(1, true);

    //middle path lies between critical zone 3 and 4.
    private final Semaphore middlePath = new Semaphore(1, true);

    //lower path lies between critical zone 5 and the station next to it.
    //lowerPath is set to zero because train 2 begins on it.
    private final Semaphore lowerPath = new Semaphore(0, true);

    //--The coordinates of sensors that we need to keep track of.--//

    private final Coordinate[] stations = {new Coordinate(13, 3), new Coordinate(13, 5),
                                           new Coordinate(12,11), new Coordinate(12,13)};
    private final Coordinate[] enter1 = {new Coordinate(6, 5), new Coordinate(10,5),
                                         new Coordinate(11,7), new Coordinate(11,8)};
    private final Coordinate[] enter2_3 = {new Coordinate(14, 8), new Coordinate(14, 7)};
    private final Coordinate[] enter3_2 = {new Coordinate(11, 9), new Coordinate(12, 10)};

    private final Coordinate[] enter4_5 = {new Coordinate( 8, 9), new Coordinate(7,10)};
    private final Coordinate[] enter5_4 = {new Coordinate( 6,11), new Coordinate( 5,13)};

    public Lab1(int speed1, int speed2) {

        Train train1 = new Train(1, speed1);
        Train train2 = new Train(2, speed2);

        train1.start();
        train2.start();
    }

    //looks for an active sensor,
    //The reason we only look for active sensors (for the most part) is because
    //our approach to this lab didn't consider the inactive sensors.
    private void findActiveSensor(Train train){
        try {
            if(tsi.getSensor(train.getTrainId()).getStatus() == SensorEvent.INACTIVE){
                tsi.getSensor(train.getTrainId());
            }
        } catch (CommandException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void zone1(Train train) {

        try {
            //preemptively slowing down
            train.slowDown();

            //trying to get zone1
            zone1sem.acquire();

            //speeds up when access is granted
            train.speedUp();

            //look for sensor outside zone1 to signal when we release the zone
            findActiveSensor(train);

            zone1sem.release();

        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

    }


    private void zone2_3(Train train, Coordinate c) throws InterruptedException {
        //preemptively slowing down
        train.slowDown();

        //trying to get zone
        zone2_3sem.acquire();

        //entering zone
        hasEnteredZone2_3(train, c);
    }

    private void hasEnteredZone2_3(Train train, Coordinate c) {
        try {
            // if shortest path was taken
            if (c.equals(enter2_3[0])) {
                tsi.setSwitch(17, 7, SWITCH_LEFT);
                upperPath.release();
            }
            else {
                tsi.setSwitch(17, 7, SWITCH_RIGHT);
            }
            train.speedUp();

            //look for sensor to check if middle path is available
            //middle path lies between critical zone 3 and 4.
            findActiveSensor(train);

            if (middlePath.tryAcquire()) {
                tsi.setSwitch(15, 9, SWITCH_RIGHT);
            } else {
                tsi.setSwitch(15, 9, SWITCH_LEFT);
            }

            //look for sensor to know when to release zone
            findActiveSensor(train);

            //releasing zone
            zone2_3sem.release();
        } catch (CommandException e) {
            System.out.println(e.getMessage());
        }
    }

    /*
    All the remaining zones and ways to enter zones from
    other directions use the same template.
     */
    private void zone3_2(Train train, Coordinate c)  throws InterruptedException{
        train.slowDown();

        zone2_3sem.acquire();
        hasEnteredZone3_2(train, c);
    }

    private void hasEnteredZone3_2(Train train, Coordinate c) {

        try {
            if (c.equals(enter3_2[0])) {
                tsi.setSwitch(15, 9, SWITCH_RIGHT);
                middlePath.release();
            } else {
                tsi.setSwitch(15, 9, SWITCH_LEFT);
            }
            train.speedUp();

            findActiveSensor(train);

            if (upperPath.tryAcquire()) {
                tsi.setSwitch(17, 7, SWITCH_LEFT);
            } else {
                tsi.setSwitch(17, 7, SWITCH_RIGHT);
            }

            findActiveSensor(train);

            zone2_3sem.release();
        } catch (CommandException e) {
            System.out.println(e.getMessage());
        }
    }

    private void zone4_5(Train train, Coordinate c) throws InterruptedException{
        train.slowDown();

        zone4_5sem.acquire();
        hasEnteredZone4_5(train, c);

    }

    private void hasEnteredZone4_5(Train train, Coordinate c) {

        try {
            if (c.equals(enter4_5[0])) {
                tsi.setSwitch(4, 9, SWITCH_LEFT);
                middlePath.release();
            } else {
                tsi.setSwitch(4, 9, SWITCH_RIGHT);
            }
            train.speedUp();

            findActiveSensor(train);

            if (lowerPath.tryAcquire()) {
                tsi.setSwitch(3, 11, SWITCH_LEFT);
            } else {
                tsi.setSwitch(3, 11, SWITCH_RIGHT);
            }

            findActiveSensor(train);

            zone4_5sem.release();
        } catch (CommandException e) {
            System.out.println(e.getMessage());
        }
    }


    private void zone5_4(Train train, Coordinate c) throws InterruptedException{
        System.out.println("zone5_4");
        train.slowDown();

        zone4_5sem.acquire();
        hasEnteredZone5_4(train,c);
    }

    private void hasEnteredZone5_4(Train train, Coordinate c){
        try {
            if (c.equals(enter5_4[0])) {
                tsi.setSwitch(3, 11, SWITCH_LEFT);
                lowerPath.release();
            } else {
                tsi.setSwitch(3, 11, SWITCH_RIGHT);
            }
            train.speedUp();

            findActiveSensor(train);

            if (middlePath.tryAcquire()) {
                tsi.setSwitch(4, 9, SWITCH_LEFT);
            } else {
                tsi.setSwitch(4, 9, SWITCH_RIGHT);
            }

            findActiveSensor(train);

            zone4_5sem.release();
        } catch (CommandException e) {
            System.out.println(e.getMessage());
        }
    }

    private class Coordinate {
        private final int x;
        private final int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }

        //Coomparing a coordinate to other coordinates in an array
        //works like contains does for lists
        public boolean equalsAny(Coordinate[] coordinates) {
            for (Coordinate c : coordinates) {
                if (this.equals(c)) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public boolean equals(Object o) {
            Coordinate c = (Coordinate) o;
            return this.x == c.getX() && this.y == c.getY();
        }
    }

    private class Train extends Thread {
        private final int id;
        private final int topSpeed;

        //dir is a boolean representing the train's direction.
        //true means forwards relative to itself, false means the opposite.
        private boolean dir;

        // A boolean used to check if a train is
        // going to stop at a station or not.
        private boolean hasLeftStation;

        public Train(int id, int speed) {
            this.id = id;
            topSpeed = speed;
            dir = true;
            hasLeftStation = false;
        }

        public int getTrainId() {
            return id;
        }

        public void run() {
            speedUp();
            lookForSensor();
        }

        //method used to make train look for sensors,
        //so it knows which zone it is entering.
        public void lookForSensor() {
            try {
                // a train is always looking for a sensor
                while (true) {
                    SensorEvent sensor = tsi.getSensor(id);
                    Coordinate c = new Coordinate(sensor.getXpos(), sensor.getYpos());

                    if(sensor.getStatus() == SensorEvent.ACTIVE){
                        // checking which zone the sensor's coordinate matches with
                        if (c.equalsAny(enter1)) {
                            zone1(this);
                        } else if (c.equalsAny(enter2_3)) {
                            zone2_3(this, c);
                        } else if (c.equalsAny(enter3_2)) {
                            zone3_2(this, c);
                        } else if (c.equalsAny(enter4_5)) {
                            zone4_5(this, c);
                        } else if (c.equalsAny(enter5_4)) {
                            zone5_4(this, c);
                        }
                    }
                    // The reason we only check for stations with inactive sensors is
                    // because we need the trains to stop after the sensor to make sure
                    // that we know when a station has been entered and left.
                    else if (c.equalsAny(stations)){
                        if(hasLeftStation) {
                            stopAtStation();
                            hasLeftStation = false;
                        } else{
                            hasLeftStation = true;
                        }
                    }

                }
            } catch (CommandException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void speedUp() {
            if (dir) {
                setSpeed(topSpeed);
            } else {
                setSpeed(-topSpeed);
            }
        }

        public void slowDown() {
            setSpeed(0);
        }

        public void stopAtStation() {
            slowDown();
            try {
                Thread.sleep(1000 + Math.abs(20 * topSpeed));
            } catch (InterruptedException ignored) {}
            reverseDir();
            speedUp();
        }

        // reversing the direction of a train.
        private void reverseDir() {
            dir = !dir;
        }

        private void setSpeed(int speed) {
            try {
                tsi.setSpeed(id, speed);
            } catch (CommandException e) {
                e.printStackTrace();    // or only e.getMessage() for the error
            }
        }
    }
}
