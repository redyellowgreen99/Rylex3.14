package org.wintrisstech.erik.iaroc;

import android.os.SystemClock;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wintrisstech.irobot.ioio.IRobotCreateAdapter;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;
import org.wintrisstech.irobot.ioio.IRobotCreateScript;
import org.wintrisstech.sensors.UltraSonicSensors;

/**
 * A Ferrari is an implementation of the IRobotCreateInterface.
 *
 * @author Erik
 */
public class Ferrari extends IRobotCreateAdapter implements Runnable
{
    private static final String TAG = "Ferrari";
    private int irCode = 255;
    int none = 255;
    int red = 248;
    int green = 244;
    int reserved = 240;
    int forceField = 242;
    int redAndGreen = 252;
    int redAndForceField = 250;
    int greenAndForceField = 246;
    int RedAndGreenAndForceField = 254;
    int totlalDistance = 0;
    private final UltraSonicSensors ultraSonicSensors;
    private final Dashboard dashboard;
    /*
     * The maze can be thought of as a grid of quadratic cells, separated by
     * zero-width walls. The cell width includes half a pipe diameter on each
     * side, i.e the cell edges pass through the center of surrounding pipes.
     * <p> Row numbers increase northward, and column numbers increase eastward.
     * <p> Positions and direction use a reference system that has its origin at
     * the west-most, south-most corner of the maze. The x-axis is oriented
     * eastward; the y-axis is oriented northward. The unit is 1 mm. <p> What
     * the Ferrari knows about the maze is:
     */
    private final static int NUM_ROWS = 12;
    private final static int NUM_COLUMNS = 4;
    private final static int CELL_WIDTH = 712;
    /*
     * State variables:
     */
    private int speed = 300; // The normal speed of the Ferrari when going straight
    // The row and column number of the current cell. 
    private int row;
    private int column;
    private boolean running = true;
    private final static int SECOND = 1000; // number of millis in a second
    private int mode = 0;
    private int forwardDistance = 0;
    private int backwardDistance = 250;

    /**
     * Constructs a Ferrari, an amazing machine!
     *
     * @param ioio the IOIO instance that the Ferrari can use to communicate
     * with other peripherals such as sensors
     * @param create an implementation of an iRobot
     * @param dashboard the Dashboard instance that is connected to the Ferrari
     * @throws ConnectionLostException
     */
    public Ferrari(IOIO ioio, IRobotCreateInterface create, Dashboard dashboard) throws ConnectionLostException
    {
        super(create);
        ultraSonicSensors = new UltraSonicSensors(ioio);
        this.dashboard = dashboard;
    }

    /**
     * Main method that gets the Ferrari running.
     *
     */
    public void run()
    {
        while (true)
        {
            try
            {
                switch (mode)
                {
                    case 0:
                        goForward(100);
                        forwardDistance += getDistance();
                        break;
                    case 1:
                        backLeft(100);
                        backwardDistance += getDistance();
                        break;
                    case 2:
                        backRight(100);
                        backwardDistance += getDistance();
                        break;
                    case 3:
                        backUp(100);
                        backwardDistance += getDistance();
                        break;
                    case 4:
                        spinLeft(30, 100);
                        break;
                    case 5:
                        spinRight(30, 100);
                        break;
                    case 6:
                        spinLeft(15, 100);
                        break;
                    case 7:
                        spinRight(15, 100);
                        break;

                }
                if (backwardDistance <= 0)
                {
                    mode = 0;
                }

                readSensors(SENSORS_GROUP_ID6);
                if (isBumpLeft() == false && isBumpRight() == false)
                {
                    continue;
                }
                if (isBumpLeft() == true && isBumpRight() == false)
                {
                    mode = 1;
                    backwardDistance = 250;
                }
                if (isBumpLeft() == false && isBumpRight() == true)
                {
                    mode = 2;
                    backwardDistance = 250;
                }
                if (isBumpLeft() == true && isBumpRight() == true)
                {
                    mode = 3;
                    backwardDistance = 250;
                }

                readSensors(SENSORS_GROUP_ID6);
                irCode = getInfraredByte();
                if (irCode == none)
                {
                    try
                    {
                        goForward(350);
                        SystemClock.sleep(5000);
                        spinLeft(360, 100);
                    } catch (Exception ex)
                    {
                        Logger.getLogger(Ferrari.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (irCode == red)
                {
                    try
                    {
                        mode = 4;
                    } catch (Exception ex)
                    {
                        Logger.getLogger(Ferrari.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (irCode == green)
                {
                    try
                    {
                        mode = 5;;
                    } catch (Exception ex)
                    {
                        Logger.getLogger(Ferrari.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (irCode == redAndGreen)
                {
                    try
                    {
                        mode = 0;
                    } catch (Exception ex)
                    {
                        Logger.getLogger(Ferrari.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (irCode == redAndForceField)
                {
                    mode = 6;
                }
                if (irCode == greenAndForceField)
                {
                    mode = 7;
                }
                if (irCode == RedAndGreenAndForceField)
                {
                    mode = 0;
                }
            } catch (Exception ex)
            {
            }
        }
    }

    /**
     * To run this test, place the Ferrari in a cell surrounded by 4 walls. <p>
     * Note: The sensors draw power from the Create's battery. Make sure it is
     * charged.
     */
    private void testUltraSonicSensors()
    {
        dashboard.log("Starting ultrasonic test.");
        long endTime = System.currentTimeMillis() + 20 * SECOND;
        while (System.currentTimeMillis() < endTime)
        {
            try
            {
                ultraSonicSensors.readUltrasonicSensors();
            } catch (ConnectionLostException ex)
            {
                //TODO
            } catch (InterruptedException ex)
            {
                //TODO
            }
            SystemClock.sleep(500);
        }
        dashboard.log("Ultrasonic test ended.");
    }

    /**
     * Tests the rotation of the Ferrari.
     */
    private void testRotation()
    {
        dashboard.log("Testing rotation");
        try
        {
            turnAndGo(10, 0);
            SystemClock.sleep(500);
            turnAndGo(80, 0);
            SystemClock.sleep(80);
            turnAndGo(-90, 0);
            SystemClock.sleep(80);
            turnAndGo(180, 0);
            SystemClock.sleep(80);
            turnAndGo(-90, 0);
            SystemClock.sleep(80);
            turnAndGo(-180, 0);
            SystemClock.sleep(80);
            turnAndGo(180, 0);
            SystemClock.sleep(80);
        } catch (ConnectionLostException ex)
        {
        } catch (InterruptedException ex)
        {
        }

    }

    private void testStrobe()
    {
        dashboard.log("Starting strobe test.");
        long endTime = System.currentTimeMillis() + 2000 * SECOND;
        while (System.currentTimeMillis() < endTime)
        {
            try
            {
                ultraSonicSensors.testStrobe();
            } catch (ConnectionLostException ex)
            {
                //TODO
            }
            SystemClock.sleep(500);
        }
        dashboard.log("Strobe test ended.");
    }

    /**
     * Turns in place and then goes forward.
     *
     * @param angle the angle in degrees that the Ferrari shall turn. Negative
     * values makes clockwise turns.
     * @param distance the distance in mm that the Ferrari shall run forward.
     * Must be positive.
     */
    private void turnAndGo(int angle, int distance)
            throws ConnectionLostException, InterruptedException
    {
        IRobotCreateScript script = new IRobotCreateScript();
        /*
         * The Create overshoots by approx. 3 degrees depending on the floor
         * surface. Note: This is speed sensitive.
         */
        // TODO: Further tweaks to make the Ferrari make more precise turns.  
        if (angle < 0)
        {
            angle = Math.min(0, angle + 3);
        }
        if (angle > 0)
        {
            angle = Math.max(0, angle - 3);
        }
        if (angle != 0)
        {
            script.turnInPlace(100, angle < 0); // Do not change speed!
            script.waitAngle(angle);
        }
        if (distance > 0)
        {
            script.driveStraight(speed);
            script.waitDistance(distance);
        }
        if (angle != 0 || distance > 0)
        {
            script.stop();
            playScript(script.getBytes(), false);
            // delay return from this method until script has finished executing
        }
    }

    /**
     * Closes down all the connections of the Ferrari, including the connection
     * to the iRobot Create and the connections to all the sensors.
     */
    public void shutDown()
    {
        closeConnection(); // close the connection to the Create
        ultraSonicSensors.closeConnection();
    }

    //// Methods made public for the purpose of the Dashboard ////
    /**
     * Gets the left distance to the wall using the left ultrasonic sensor
     *
     * @return the left distance
     */
    public int getLeftDistance()
    {
        return ultraSonicSensors.getLeftDistance();
    }

    /**
     * Gets the front distance to the wall using the front ultrasonic sensor
     *
     * @return the front distance
     */
    public int getFrontDistance()
    {
        return ultraSonicSensors.getFrontDistance();
    }

    /**
     * Gets the right distance to the wall using the right ultrasonic sensor
     *
     * @return the right distance
     */
    public int getRightDistance()
    {
        return ultraSonicSensors.getRightDistance();
    }

    /**
     * Checks if the Ferrari is running
     *
     * @return true if the Ferrari is running
     */
    public synchronized boolean isRunning()
    {
        return running;
    }

    private synchronized void setRunning(boolean b)
    {
        running = false;
    }

    public boolean closeToBeacon()
    {
        if (getInfraredByte() == 244 || getInfraredByte() == 248 || getInfraredByte() == 252)//Red, green
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * ***********************************************************************
     * Rylex AwesomeApi
     * ***********************************************************************
     */
    public void bumpFront(int speed) throws Exception
    {
        readSensors(SENSORS_GROUP_ID6);
        if (isBumpRight() && isBumpLeft())
        {
            System.out.println("BF");
            backUp(speed);
            //goNorth();
        }
    }

    public void bumpLeft(int speed) throws Exception
    {
        readSensors(SENSORS_GROUP_ID6);
        if (isBumpLeft())
        {
            System.out.println("BL");
            backRight(speed);
            //goNorth();
        }
    }

    public void bumpRight(int speed) throws Exception
    {
        readSensors(SENSORS_GROUP_ID6);
        if (isBumpRight())
        {
            System.out.println("BR");
            backLeft(speed);
            //goNorth();
        }
    }

    public void goForward(int speed) throws Exception
    {
//        checkingBumps(500);
        driveDirect(speed, speed);
//        checkingBumps(500);
    }

    public void goBackwards(int speed) throws Exception
    {
        driveDirect(-speed, -speed);
    }

    public void checkingBumps(int speed) throws Exception
    {
        bumpRight(speed);
        bumpLeft(speed);
        bumpFront(speed);
    }

    public void backLeft(int speed) throws Exception//Bumped right
    {
        driveDirect(-speed, -speed / 3 * 2);
    }

    public void backRight(int speed) throws Exception//Bumped left
    {
        driveDirect(-speed / 3 * 2, -speed);
    }

    public void backUp(int speed) throws Exception//Bumped front
    {
        int r = new Random().nextInt(2);
        if (r == 0)
        {
            backLeft(speed);
        } else
        {
            backRight(speed);
        }
    }

    public void spinLeft(int angle, int speed) throws Exception
    {
        readSensors(SENSORS_GROUP_ID6);
        int currentAngle = getAngle();
        while (currentAngle <= angle)
        {
            readSensors(SENSORS_GROUP_ID6);
            currentAngle = getAngle();
            driveDirect(speed, -speed);
        }
    }

    public void spinRight(int angle, int speed) throws Exception
    {
        readSensors(SENSORS_GROUP_ID6);
        int currentAngle = getAngle();
        while (currentAngle <= angle)
        {
            readSensors(SENSORS_GROUP_ID6);
            currentAngle = getAngle();
            driveDirect(-speed, speed);
        }
    }
}
