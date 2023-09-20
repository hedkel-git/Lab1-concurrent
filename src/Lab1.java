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
    // is set to zero permits because it is occupied by train 2.
    private final Semaphore lowerPath = new Semaphore(0, true);


    private final Coordinate[] enter1 = {new Coordinate(6, 5), new Coordinate(10,5),
                                         new Coordinate(11,7), new Coordinate(11,8)};

    private final Coordinate[] enter2_3 = {new Coordinate(14, 8), new Coordinate(14, 7)};
    private final Coordinate[] enter3_2 = {new Coordinate(12, 9), new Coordinate(13, 10)};

    private final Coordinate[] enter4_5 = {new Coordinate( 7, 9), new Coordinate(6,10)};
    private final Coordinate[] enter5_4 = {new Coordinate( 6,11), new Coordinate( 5,13)};
    private final Coordinate[] stations = {new Coordinate(13, 3), new Coordinate(13, 5),
                                           new Coordinate(12,11), new Coordinate(12,13)};


    private final Coordinate[] switches = {new Coordinate(17,7), new Coordinate(15,9),
                                           new Coordinate( 4,9), new Coordinate(3,11)};

    public Lab1(int speed1, int speed2) {

        Train train1 = new Train(1, speed1);
        Train train2 = new Train(2, speed2);

        //testingSwitches();

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


    public void testingSwitches(){
        try {
            Thread.sleep(2000);
            tsi.setSwitch(17, 7, SWITCH_RIGHT);
            Thread.sleep(2000);

            tsi.setSwitch(15, 9, SWITCH_RIGHT);
            Thread.sleep(2000);

            tsi.setSwitch(4, 9, SWITCH_LEFT);
            Thread.sleep(2000);

            tsi.setSwitch(3, 11, SWITCH_LEFT);
        } catch (CommandException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public void findActiveSensor(int trainId){
        try {
            if(tsi.getSensor(trainId).getStatus() == SensorEvent.INACTIVE){
                tsi.getSensor(trainId);
            }
        } catch (CommandException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void zone1(Train train) {

        try {
            //preemptively slowing down
            train.slowDown();

            //trying to get zone1
            zone1sem.acquire();

            //speeds up when access is granted
            train.speedUp();

            //look for sensor outside zone1 to signal when we release the zone
            findActiveSensor(train.getTrainId());

            zone1sem.release();

        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

    }


    private void enterCriticalZone(Train train, Coordinate c, Path p) throws InterruptedException{

        //preemptively slowing down
        train.slowDown();

        try {
            switch (p) {
                case PATH2_3:

                    //trying to get access to zone
                    zone2_3sem.acquire();

                    //checking if the previous path taken was the shorter or longer,
                    //releases previous shorter path if so.
                    if (c.equals(enter2_3[0])) {
                        tsi.setSwitch(17, 7, SWITCH_LEFT);
                        upperPath.release();
                    } else {
                        tsi.setSwitch(17, 7, SWITCH_RIGHT);
                    }

                    //entering the acquired zone and checking if the next short path is available.
                    enteringZone(train, middlePath, switches[1],false);

                    //releases zone.
                    zone2_3sem.release();

                case PATH3_2:
                    zone2_3sem.acquire();

                    if (c.equals(enter3_2[0])) {
                        tsi.setSwitch(15, 9, SWITCH_RIGHT);
                        middlePath.release();
                    } else {
                        tsi.setSwitch(15, 9, SWITCH_LEFT);
                    }

                    enteringZone(train, upperPath, switches[0],true);
                    zone2_3sem.release();
                case PATH4_5:
                    zone4_5sem.acquire();

                    //not done, check switch at 4
                    if (c.equals(enter4_5[0])) {
                        tsi.setSwitch(4, 9, SWITCH_LEFT);
                        middlePath.release();
                    } else {
                        tsi.setSwitch(4, 9, SWITCH_RIGHT);
                    }

                    enteringZone(train, lowerPath, switches[3], true);
                    zone4_5sem.release();
                case PATH5_4:
                    zone4_5sem.acquire();

                    //not done, check switch at 5
                    if (c.equals(enter5_4[0])) {
                        tsi.setSwitch(3, 11, SWITCH_LEFT);
                        lowerPath.release();
                    } else {
                        tsi.setSwitch(3, 11, SWITCH_RIGHT);
                    }


                    enteringZone(train, middlePath, switches[2], true);
                    zone4_5sem.release();
            }
        }
        catch (CommandException e) {
            throw new RuntimeException(e);
        }





    }

  //  checkShortPath(middlePath,15,9,false);
    private void enteringZone(Train train, Semaphore nextShortPath, Coordinate switcH, boolean dir){

        train.speedUp();
        findActiveSensor(train.getTrainId());
        checkShortPath(nextShortPath,switcH.getX(),switcH.getY(),dir);
        findActiveSensor(train.getTrainId());

    }

    private void checkShortPath(Semaphore path, int x, int y, boolean dir){

        int fst;
        int snd;

        if(dir){
            fst = SWITCH_LEFT;
            snd = SWITCH_RIGHT;
        }
        else {
            fst = SWITCH_RIGHT;
            snd = SWITCH_LEFT;
        }

        try {
            if (path.tryAcquire()) {
                tsi.setSwitch(x,y, fst);
            } else {
                tsi.setSwitch(x,y, snd);
            }
        }
        catch (CommandException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    private void zone2_3(Train train, Coordinate c) throws InterruptedException {
        train.slowDown();

        System.out.println("Trying to enter zone2_3: " + train.getTrainId());
        zone2_3sem.acquire();
        hasEnteredZone2_3(train, c);
    }

    private void hasEnteredZone2_3(Train train, Coordinate c) {
        try {
            //
            //hur vet vi om vi ska switcha upp eller ner?
            //vi vet det beroende på vilken sensor det var som aktiverades
            //

            // if shortest path was taken
            if (c.equals(enter2_3[0])) {
                tsi.setSwitch(17, 7, SWITCH_LEFT);
                upperPath.release();
            }
            else {
                //System.out.println("switching for longer path train1");
                tsi.setSwitch(17, 7, SWITCH_RIGHT);
            }
            train.speedUp();

            //look for sensor to check if middle path is available
            //middle path lies between critical zone 3 and 4.
            findActiveSensor(train);

            checkShortPath(middlePath,15,9,false);

            //look for sensor to know when to release zone2_3
            findActiveSensor(train);

            zone2_3sem.release();
        } catch (CommandException e) {
            System.out.println(e.getMessage());
        }
    }

    public void zone3_2(Train train, Coordinate c)  throws InterruptedException{
        train.slowDown();

        System.out.println("Trying to enter zone3_2: " + train.getTrainId());
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

            checkShortPath(upperPath,17,7,true);

            findActiveSensor(train);

            zone2_3sem.release();
        } catch (CommandException e) {
            System.out.println(e.getMessage());
        }
    }

    public void zone4_5(Train train, Coordinate c) throws InterruptedException{
        train.slowDown();
        System.out.println("Trying to enter zone4_5: " + train.getTrainId());

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

            checkShortPath(lowerPath,3,11,true);

            findActiveSensor(train);

            zone4_5sem.release();
        } catch (CommandException e) {
            System.out.println(e.getMessage());
        }
    }


    public void zone5_4(Train train, Coordinate c) throws InterruptedException{
        train.slowDown();

        System.out.println("Trying to enter zone5_4: " + train.getTrainId());

        zone4_5sem.acquire();
        hasEnteredZone5_4(train,c);
    }


    public void hasEnteredZone5_4(Train train, Coordinate c){
        try {
            //is upper path was taken
            if (c.equals(enter5_4[0])) {
                tsi.setSwitch(3, 11, SWITCH_LEFT);
                lowerPath.release();
            } else {
                tsi.setSwitch(3, 11, SWITCH_RIGHT);
            }
            train.speedUp();

            //look for sensor to check if middle path is available
            findActiveSensor(train);

            checkShortPath(middlePath,4,9,true);

            findActiveSensor(train);

            zone4_5sem.release();
        } catch (CommandException e) {
            System.out.println(e.getMessage());
        }
    }
    */

    private class Switch{
        private final int x;
        private final int y;
        private final Coordinate c;
        private final Semaphore s;

        public Switch(int x, int y, Coordinate c, Semaphore s){
            this.x = x;
            this.y = y;
            this.c = c;
            this.s = s;
        }

        public void switchAccordingTo(Coordinate c){

        }
    }

    private class Coordinate {
        public final int x;
        public final int y;

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

        public void lookForSensor() {

            try {

                // a train is always looking for a sensor
                while (true) {

                    //System.out.println("back to looking for sensors again");
                    SensorEvent sensor = tsi.getSensor(id);
                    Coordinate c = new Coordinate(sensor.getXpos(), sensor.getYpos());
                    Path p;

                    if(sensor.getStatus() == SensorEvent.ACTIVE){
                        System.out.println("found active sensor");
                        // checking which zone the sensor's coordinate matches with
                        // this may take too long...
                        if (c.equalsAny(enter1)) {
                            zone1(this);
                        } else if (c.equalsAny(enter2_3)) {

                            p = Path.PATH2_3;

                            enterCriticalZone(this,c,Path.PATH2_3);

                            //zone2_3(this, c);
                        } else if (c.equalsAny(enter3_2)) {

                            p = Path.PATH3_2;

                            enterCriticalZone(this,c,Path.PATH3_2);

                            //zone3_2(this, c);
                        } else if (c.equalsAny(enter4_5)) {

                            p = Path.PATH4_5;

                            System.out.println("Trying to enter zone4_5: " + id);
                            enterCriticalZone(this,c,Path.PATH4_5);

                            //zone4_5(this, c);
                        } else if (c.equalsAny(enter5_4)) {


                            System.out.println("Trying to enter zone5_4: " + id);
                            enterCriticalZone(this,c,Path.PATH5_4);
                            //zone5_4(this, c);
                        }
                    }
                    else if (c.equalsAny(stations)){
                        System.out.println("station found");
                        if(hasLeftStation) {
                            System.out.println("stopping at station " + id);
                            stopAtStation();
                        }
                        else{
                            hasLeftStation = true;
                        }
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
            hasLeftStation = false;
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
            }
        }
    }

    private enum Path {
        PATH2_3,
        PATH3_2,
        PATH4_5,
        PATH5_4
    }

}
