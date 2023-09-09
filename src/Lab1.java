import TSim.*;

import static TSim.TSimInterface.*;

import java.util.concurrent.Semaphore;

public class Lab1 {

    TSimInterface tsi = getInstance();
    private Semaphore zoneA = new Semaphore(1,true);

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

            try {
                while (true) {
                    SensorEvent sensor = tsi.getSensor(id);

                    int x = sensor.getXpos();
                    int y = sensor.getYpos();

                    /*
                    switch (coordinates){

                        case zone a: zoneA(this);

                        etc...

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
            } catch (InterruptedException ignored) {
            }
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

    public Lab1(int speed1, int speed2) {

        Train train1 = new Train(1, speed1);
        Train train2 = new Train(2, speed2);

        train1.start();
        train2.start();

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
    }


    public void zoneA(Train train){

        try {
            
            train.slowDown();
            while (!zoneA.tryAcquire()) {
            }
            train.speedUp();
            tsi.getSensor(train.getTrainId());
            zoneA.release();


        } catch (CommandException | InterruptedException e) {
            System.out.println(e.getMessage());
            System.exit(1);
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
