import java.util.TimerTask;

public class DelayedSender extends TimerTask {
    private User user;
    private String command;
    private String parameters;

    public DelayedSender(User user, String command, String parameters) {
        this.user = user;
        this.command = command;
        this.parameters = parameters;
    }

    @Override
    public void run() {
        user.sendCommand(command, parameters);
    }
}