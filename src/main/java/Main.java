import java.util.Timer;

public class Main {
    public static void main(String ... args) {
        System.out.println(args[0]);
        System.out.println(args[1]);
        System.out.println(args[2]);

        if(args.length == 3) {
            Timer fetchData = new Timer();
            fetchData.schedule(new ScheduleTask(args[0], args[1], args[2]), 0, 5 * 60 * 1000);
        } else
            System.err.println("Error: No Arguments given.");

    }
}
