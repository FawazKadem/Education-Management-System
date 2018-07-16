package systemOperations;

public class SystemState {

    public static boolean systemOn = false;

    public static void setSystemOn(boolean state) {
        SystemState.systemOn = state;
    }

    public static boolean isOn() {
        return systemOn;
    }


    public static String systemStateAsString() {
        if (isOn() == false) {return "System is currently off";}

        return "System is current on";
    }
}
