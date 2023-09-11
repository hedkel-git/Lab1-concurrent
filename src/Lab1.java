import TSim.*;

import static TSim.TSimInterface.*;

import java.util.concurrent.Semaphore;

public class Lab1 {

    TSimInterface tsi = getInstance();
    private final Semaphore zone1sem = new Semaphore(1, true);
    private final Semaphore zone2_3sem = new Semaphore(1, true);
    private final Semaphore zone4_5sem = new Semaphore(1, true);




    //upper path lies between critical zone 2 and the path to the station above to it.
    private final Semaphore upperPath = new Semaphore(1, true);

    //middle path lies between critical zone 3 and 4.
    private final Semaphore middlePath = new Semaphore(1, true);

    //lower path lies between critical zone 5 and the station next to it.
    private final Semaphore lowerPath = new Semaphore(1, true);


    private final Coordinate[] enter1 = {new Coordinate(6, 5), new Coordinate(10,5),
                                         new Coordinate(11,7), new Coordinate(11,8)};

    private final Coordinate[] enter2_3 = {new Coordinate(13, 7), new Coordinate(13, 8)};
    private final Coordinate[] enter3_2 = {new Coordinate(11, 9), new Coordinate(12, 10)};

    private final Coordinate[] enter4_5 = {new Coordinate(8,9), new Coordinate(7,10)};
    private final Coordinate[] enter5_4 = {new Coordinate(6,11), new Coordinate(5,13)};


    public Lab1(int speed1, int speed2) {

        Train train1 = new Train(1, speed1);
        Train train2 = new Train(2, speed2);

        train1.start();
        train2.start();
    }

    /*
    try {
        //System.out.println("hellopppo");
        //tsi.setSpeed(1,speed1);
        //tsi.setSpeed(2,speed2);
    }
    catch (CommandException e) {
        e.printStackTrace();    // or only e.getMessage() for the error
        System.exit(1);
    }
    */

    public void zone1(Train train) {

        try {
            train.slowDown();
            zone1sem.acquire();

            train.speedUp();
            tsi.getSensor(train.getTrainId());
            zone1sem.release();

        } catch (CommandException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }


    public void zone2_3(Train train, Coordinate c) throws InterruptedException {

        train.slowDown();

        if (zone2_3sem.tryAcquire()) {
            hasEnteredZone2_3(train, c);
        }
        else {
            zone2_3sem.acquire();
            hasEnteredZone2_3(train, c);
        }
    }

    private void hasEnteredZone2_3(Train train, Coordinate c) {
        try {
            /*
            hur vet vi om vi ska switcha upp eller ner?
            vi vet det beroende på vilken sensor det var som aktiverades
            */

            //is upper path was taken
            if (c.equals(enter2_3[0])) {
                tsi.setSwitch(17, 7, SWITCH_RIGHT);
            }
            //if lower path was taken
            else {
                tsi.setSwitch(17, 7, SWITCH_LEFT);
                upperPath.release();
            }
            train.speedUp();

            //look for sensor to check if middle path is available
            //middle path lies between critical zone 3 and 4.
            tsi.getSensor(train.getTrainId());
            if (middlePath.tryAcquire()) {
                tsi.setSwitch(15, 9, SWITCH_RIGHT);
            } else {
                tsi.setSwitch(15, 9, SWITCH_LEFT);
            }

            //look for sensor to know when to release zone2_3
            tsi.getSensor(train.getTrainId());
            zone2_3sem.release();
        } catch (CommandException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public void zone3_2(Train train, Coordinate c)  throws InterruptedException{
        train.slowDown();

        if (zone2_3sem.tryAcquire()) {
            hasEnteredZone3_2(train, c);
        }
        else {
            zone2_3sem.acquire();
            hasEnteredZone3_2(train, c);
        }
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

            tsi.getSensor(train.getTrainId());

            if (upperPath.tryAcquire()) {
                tsi.setSwitch(17, 7, SWITCH_LEFT);
            } else {
                tsi.setSwitch(17, 7, SWITCH_RIGHT);
            }

            tsi.getSensor(train.getTrainId());
            zone2_3sem.release();
        } catch (CommandException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public void zone4_5(Train train, Coordinate c) throws InterruptedException{
        train.slowDown();

        if (zone4_5sem.tryAcquire()) {
            hasEnteredZone4_5(train, c);
        }
        else {
            zone4_5sem.acquire();
            hasEnteredZone4_5(train, c);
        }
    }

    private void hasEnteredZone4_5(Train train, Coordinate c) {

        try {

            //is upper path was taken
            if (c.equals(enter4_5[0])) {
                tsi.setSwitch(4, 9, SWITCH_RIGHT);
                middlePath.release();
            } else {
                tsi.setSwitch(4, 9, SWITCH_LEFT);
            }
            train.speedUp();

            //look for sensor to check if middle path is available
            tsi.getSensor(train.getTrainId());

            //lower path lies between critical zone 5 and the station next to it.
            if (lowerPath.tryAcquire()) {
                tsi.setSwitch(3, 11, SWITCH_RIGHT);
            } else {
                tsi.setSwitch(3, 11, SWITCH_LEFT);
            }

            //look for sensor to know when to release zone4_5
            tsi.getSensor(train.getTrainId());
            zone4_5sem.release();
        } catch (CommandException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }


    public void zone5_4(Train train, Coordinate c) throws InterruptedException{
        train.slowDown();

        if(zone4_5sem.tryAcquire()){
            hasEnteredZone5_4(train,c);
        }
        else {
            zone4_5sem.acquire();
            hasEnteredZone5_4(train,c);
        }
    }


    public void hasEnteredZone5_4(Train train, Coordinate c){
        try {

            //is upper path was taken
            if (c.equals(enter5_4[0])) {
                tsi.setSwitch(3, 11, SWITCH_RIGHT);
                lowerPath.release();
            } else {
                tsi.setSwitch(3, 11, SWITCH_LEFT);
            }
            train.speedUp();

            //look for sensor to check if middle path is available
            tsi.getSensor(train.getTrainId());

            if (middlePath.tryAcquire()) {
                tsi.setSwitch(4, 9, SWITCH_RIGHT);
            } else {
                tsi.setSwitch(4, 9, SWITCH_LEFT);
            }

            //look for sensor to know when to release zone4_5
            tsi.getSensor(train.getTrainId());
            zone4_5sem.release();
        } catch (CommandException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }


    private class Coordinate {
        public final int x;
        public final int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public boolean equalsAny(Coordinate[] coords) {
            for (Coordinate c : coords) {
                if (this.equals(c)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            Coordinate c = (Coordinate) o;
            return this.x == c.x && this.y == c.y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    private class Train extends Thread {
        private final int id;
        private final int topSpeed;
        private boolean dir;

        public Train(int id, int speed) {
            this.id = id;
            topSpeed = speed;
            dir = true;
        }

        public int getTrainId() {
            return id;
        }

        public void run() {
            speedUp();
            lookForSensor();
        }

        public void lookForSensor() {

            try {
                // a train is always looking for a sensor
                while (true) {

                    SensorEvent sensor = tsi.getSensor(id);

                    Coordinate c = new Coordinate(sensor.getXpos(), sensor.getYpos());
                    System.out.println(c.equals(enter2_3[0]));
                    System.out.println(c);
                    // checking which zone the sensor's coordinate matches with


                    // this may take too long...
                    if (c.equalsAny(enter1)) {
                        zone1(this);
                    }
                    else if (c.equalsAny(enter2_3)) {
                        zone2_3(this, c);
                    } else if (c.equalsAny(enter3_2)) {
                        zone3_2(this, c);
                    }

                    else if (c.equalsAny(enter4_5)) {
                        zone4_5(this, c);
                    } else if (c.equalsAny(enter5_4)) {
                        zone5_4(this, c);
                    }
                    else if(!dir){
                        stopAtStation();
                    }


                }
            } catch (CommandException | InterruptedException e) {
                throw new RuntimeException(e);
            }

                /*
                SensorEvent sensor = tsi.getSensor(train.id);

                this gives us an object with the coordinates to the sensor,
                we can check if they apply to the zone and continue if that
                is the case...

                idea: get sensor first, use switch case to match sensor to specific zone
                and let train
                 */
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
        private void reverseDir() {
            dir = !dir;
        }

        private void setSpeed(int speed) {
            try {
                tsi.setSpeed(id, speed);
            } catch (CommandException e) {
                e.printStackTrace();    // or only e.getMessage() for the error
                System.exit(1);
            }
        }
    }
}
