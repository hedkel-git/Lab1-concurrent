import TSim.*;
import static TSim.TSimInterface.*;
import java.util.concurrent.Semaphore;

public class Lab1 {

  private class Train extends Thread {
    private final int id;
    private final int topSpeed;
    private boolean dir;
    TSimInterface track = getInstance();

    public Train(int id, int speed){
      this.id = id;
      topSpeed = speed;
      dir = true;
    }

    public void run(){
      speedUp();
      /*
      idea: use one sensor for switching to one direction
      and then back again after a slight delay...
       */

    }

    private void testing() {
      try {
        Thread.sleep(1000);
        track.setSwitch(17,7,SWITCH_RIGHT);
        if(id == 1) {
          Thread.sleep(2500);
          track.setSwitch(17, 7, SWITCH_LEFT);
          //System.out.println("now");
          //stop();
        }
      } catch (CommandException | InterruptedException e) {
        throw new RuntimeException(e);
      }
      System.out.println("done");
    }

    public void speedUp(){
      if(dir) { setSpeed(topSpeed); }
      else    { setSpeed(-topSpeed); }
    }

    public void slowDown(){
      setSpeed(0);
    }

    public void stopAtStation() {
      slowDown();
      try { Thread.sleep(1000 + Math.abs(20 * topSpeed)); }
      catch (InterruptedException ignored){}
      reverseDir();

    }
    private void reverseDir(){
      dir = !dir;
    }

    private void setSpeed(int speed){
      try {
        track.setSpeed(id,speed);
      }
      catch (CommandException e) {
        e.printStackTrace();    // or only e.getMessage() for the error
        System.exit(1);      }
    }
  }

  public Lab1(int speed1, int speed2) {
    System.out.println();

    TSimInterface tsi = TSimInterface.getInstance();
    Train train1 = new Train(1, speed1);
    Train train2 = new Train(2, speed2);

    train1.start();
    train2.start();

    Semaphore s1 = new Semaphore(1, true);

    int counter = 0;

    try {
      s1.acquire();

      while (true) {  //The game-loop

        tsi.getSensor(1);

        if(counter == 0) {
          train1.slowDown();
          System.out.println("slowing down");

          counter = -1;
        }

      }
    }
    catch (InterruptedException | CommandException e){

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
  }





}
