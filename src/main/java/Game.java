import java.util.Random;
import java.util.Timer;

public class Game implements Runnable {
    private User user1, user2;
    private int pointsUser1, pointsUser2, seed;
    private boolean user1Rdy, user2Rdy, user1Turn;
    private Server server;

    public Game(User user1, User user2, Server server) {
        this.user1 = user1;
        this.user2 = user2;
        this.server = server;
        this.user1Rdy = false;
        this.user2Rdy = false;
        this.user1.setCurrentGame(this);
        this.user2.setCurrentGame(this);
        Random randomGenerator = new Random();
        seed = randomGenerator.nextInt(99999);
    }

    public void processCommand(User user, String command, String[] parameters) {
        if (command.equals("GAMEFOUNDCONFIRM")) {
            System.out.println(user.getName() + " has found the game. Initializing...");
            user.sendCommand("GAMEINIT", "" + seed + "/" + user1.getName() + "/" + user2.getName());
        } else if (command.equals("GAMEINITCONFIRM")) {
            System.out.println(user.getName() + " has initialized the game.");
            if (user.equals(user1)) {
                user1Rdy = true;
            } else if (user.equals(user2)) {
                user2Rdy = true;
            }
            if (user1Rdy && user2Rdy) {
                Random random = new Random();
                if (random.nextBoolean()) {
                    user1Turn = true;
                    user1.sendCommand("YOURTURN", "");
                    user2.sendCommand("NOTYOURTURN", "");
                } else {
                    user1Turn = false;
                    user2.sendCommand("YOURTURN", "");
                    user1.sendCommand("NOTYOURTURN", "");
                }
            }
        } else if (command.equals("FIRSTCARD")) {
            if (user1Turn) {
                user2.sendCommand("SHOWCARD", "" + parameters[0] + "/" + parameters[1] + "/" + parameters[2]);
            } else {
                user1.sendCommand("SHOWCARD", "" + parameters[0] + "/" + parameters[1] + "/" + parameters[2]);
            }
        } else if (command.equals("SECONDCARD")) {
            Timer timer = new Timer();
            if (user1Turn) {
                user2.sendCommand("SHOWCARD", "" + parameters[0] + "/" + parameters[1] + "/" + parameters[2]);
                timer.schedule(new DelayedSender(user1, "NOTYOURTURN", ""), 2000);
                timer.schedule(new DelayedSender(user2, "YOURTURN", ""), 2000);
                user1Turn = false;
            } else {
                user1.sendCommand("SHOWCARD", "" + parameters[0] + "/" + parameters[1] + "/" + parameters[2]);
                timer.schedule(new DelayedSender(user1, "YOURTURN", ""), 2000);
                timer.schedule(new DelayedSender(user2, "NOTYOURTURN", ""), 2000);
                user1Turn = true;
            }

        } else if(command.equals("POINTSCORED")) {
            if(user.equals(user1)) {
                pointsUser1++;
                user2.sendCommand("ENEMYSCORED", "" + parameters[0] + "/" + parameters[1] + "/" + parameters[2] + "/" + parameters[3]);
            } else {
                pointsUser2++;
                user1.sendCommand("ENEMYSCORED", "" + parameters[0] + "/" + parameters[1] + "/" + parameters[2] + "/" + parameters[3]);
            }
            if(gameOver()) {
                if(pointsUser1 > pointsUser2) {
                    user1.sendCommand("GAMEWON", "");
                    user2.sendCommand("GAMELOST", "");
                } else {
                    user1.sendCommand("GAMELOST", "");
                    user2.sendCommand("GAMEWON", "");
                }
            }
        } else if(command.equals("GAMEOVERCONFIRM")) {
            user1.setCurrentGame(null);
            user2.setCurrentGame(null);
            server.removeGame(this);
        } else if(command.equals("LEAVING")) {
            if(user.equals(user1)) {
                user2.sendCommand("ENEMYLEFT", "");
            } else {
                user1.sendCommand("ENEMYLEFT", "");
            }
        }
    }

    public boolean gameOver() {
        if(pointsUser1 + pointsUser2 >= 18) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        user1.sendCommand("FOUNDGAME", "");
        user2.sendCommand("FOUNDGAME", "");
    }
}
