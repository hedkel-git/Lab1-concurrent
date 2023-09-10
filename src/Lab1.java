import TSim.*;

import static TSim.TSimInterface.*;

import java.util.concurrent.Semaphore;

public class Lab1 {

    TSimInterface tsi = getInstance();
    private final Semaphore zoneA = new Semaphore(1,true);

    private final Coordinate[] enter1 = {};

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

    public void zone1(Train train){

        try {
            train.slowDown();
            zoneA.acquire();

            train.speedUp();
            tsi.getSensor(train.getTrainId());
            zoneA.release();

        } catch (CommandException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }

    private class Coordinate{
        public final int x;
        public final int y;
        public Coordinate(int x, int y){
            this.x = x;
            this.y = y;
        }

        public boolean equalsAny(Coordinate[] coords){
            for (Coordinate c : coords) {
                if(this == c){
                    return true;
                }
            }
            return false;
        }
        @Override
        public boolean equals(Object o){
            Coordinate c = (Coordinate) o;

            return this.x == c.x && this.y == c.y;
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
        
        public void lookForSensor(){
            
            try {
                // a train is always looking for a sensor
                while (true) {

                    SensorEvent sensor = tsi.getSensor(id);

                    Coordinate c = new Coordinate(sensor.getXpos(), sensor.getYpos());

                    // checking which zone the sensor's coordinate matches with
                    if (c.equalsAny(enter1)){
                        //zone1(this);
                        System.out.println();
                    }
                    /*

                    //this may take too long...
                    else if (c.equalsAny(enter2)){
                        zone2(this);
                    } else if (c.equalsAny(enter3)) {
                        zone3(this);
                    } else if (c.equalsAny(enter4)) {
                        zone4(this);
                    } else if (c.equalsAny(enter5)) {
                        zone5(this);
                    }
                     */

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

            /*
            track.getSensor(id);

            while(!s.tryAcquire()){
            stop();
            }
            speedUp();
            track.getSensor(id);
            s.release();
            */

            /*
            idea: use one sensor for switching to one direction
            and then back again after a slight delay...
            */


        }

        //will probably not be needed
        public boolean getDir(){
            return dir;
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
            try {Thread.sleep(1000 + Math.abs(20 * topSpeed));}
            catch (InterruptedException ignored) {}
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

        private void testing() {
            try {
                Thread.sleep(1000);
                tsi.setSwitch(17, 7, SWITCH_RIGHT);
                if (id == 1) {
                    Thread.sleep(2500);
                    tsi.setSwitch(17, 7, SWITCH_LEFT);
                    //System.out.println("now");
                    //stop();
                }
            } catch (CommandException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("done");
        }
    }



    private class Zone {

        private final Semaphore s;

        public Zone() {
            s = new Semaphore(1, true);
        }

        public void acquire(Train train) {

            try {
                /*  change to specific sensor or area, something
                 *  that does not get activated by any sensor.
                 */
                tsi.getSensor(train.id);

                /*
                SensorEvent sensor = tsi.getSensor(train.id);

                this gives us an object with the coordinates to the sensor,
                we can check if they apply to the zone and continue if that
                is the case...

                idea: get sensor first, use switch case to match sensor to specific zone
                and let train
                 */


                train.slowDown();
                while (!s.tryAcquire()) {
                }
                train.speedUp();
                tsi.getSensor(train.getTrainId());
                s.release();


            } catch (CommandException | InterruptedException e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }

        }


    }


}
