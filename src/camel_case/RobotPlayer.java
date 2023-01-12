package camel_case;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import camel_case.robot.Amplifier;
import camel_case.robot.Booster;
import camel_case.robot.Carrier;
import camel_case.robot.Destabilizer;
import camel_case.robot.Headquarter;
import camel_case.robot.Launcher;
import camel_case.robot.Robot;

@SuppressWarnings("unused")
public class RobotPlayer {
    public static void run(RobotController rc) {
        Robot robot = createRobot(rc);

        if (robot == null) {
            return;
        }

        //noinspection InfiniteLoopStatement
        while (true) {
            if (performTurn(rc, robot)) {
                Clock.yield();
            }
        }
    }

    private static boolean performTurn(RobotController rc, Robot robot) {
        int startRound = rc.getRoundNum();

        try {
            robot.run();
        } catch (Exception e) {
            System.out.println("Exception in robot #" + rc.getID() + " (" + rc.getType() + ")");
            e.printStackTrace();
        }

        int usedBytecodes = (rc.getRoundNum() - startRound) * rc.getType().bytecodeLimit + Clock.getBytecodeNum();
        int maxBytecodes = rc.getType().bytecodeLimit;
        double bytecodePercentage = (double) usedBytecodes / (double) maxBytecodes * 100.0;
        if (bytecodePercentage >= 95) {
            String format = "High bytecode usage!\n%s/%s (%s%%)\n";
            System.out.printf(format, usedBytecodes, maxBytecodes, (int) Math.round(bytecodePercentage));
        }

        return bytecodePercentage < 100;
    }

    private static Robot createRobot(RobotController rc) {
        switch (rc.getType()) {
            case HEADQUARTERS:
                return new Headquarter(rc);
            case CARRIER:
                return new Carrier(rc);
            case LAUNCHER:
                return new Launcher(rc);
            case DESTABILIZER:
                return new Destabilizer(rc);
            case BOOSTER:
                return new Booster(rc);
            case AMPLIFIER:
                return new Amplifier(rc);
            default:
                System.out.println("Unknown robot type '" + rc.getType() + "'");
                return null;
        }
    }
}
