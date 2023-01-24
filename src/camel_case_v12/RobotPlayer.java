package camel_case_v12;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import camel_case_v12.robot.Amplifier;
import camel_case_v12.robot.Booster;
import camel_case_v12.robot.Carrier;
import camel_case_v12.robot.Destabilizer;
import camel_case_v12.robot.Headquarters;
import camel_case_v12.robot.Launcher;
import camel_case_v12.robot.Robot;

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
        int startBytecodes = Clock.getBytecodeNum();

        try {
            robot.run();
        } catch (Exception e) {
            System.out.println("Exception in robot #" + rc.getID() + " (" + rc.getType() + ")");
            e.printStackTrace();
        }

        int endRound = rc.getRoundNum();
        int endBytecodes = Clock.getBytecodeNum();
        int maxBytecodes = rc.getType().bytecodeLimit;

        int usedBytecodes = startRound == endRound
                ? endBytecodes - startBytecodes
                : (maxBytecodes - startBytecodes) + Math.max(0, endRound - startRound - 1) * maxBytecodes + endBytecodes;

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
                return new Headquarters(rc);
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
