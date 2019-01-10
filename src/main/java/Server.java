import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
    private ServerSocket serverSocket;
    private ArrayList<Game> games;
    private ArrayList<User> userList;
    private ArrayList<User> usersWaitingForGame;
    private boolean running;

    public Server() {
        try {
            serverSocket = new ServerSocket(5555);
            System.out.println("Server started.");
            games = new ArrayList<>();
            userList = new ArrayList<>();
            usersWaitingForGame = new ArrayList<>();
            running = true;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Server konnte nicht gestartet werden.");
            // TODO: Add reasons why server couldn't be started.
        }
    }

    @Override
    public void run() {
        while(running) {
            try {
                Socket connectedClient = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
                String name = reader.readLine();
                User connectedUser = new User(name, connectedClient, this);

                userList.add(connectedUser);
                Thread userHandler = new Thread(connectedUser);
                userHandler.start();
                // Only validation TODO: Delete
                System.out.println("Client " + name + " connected.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void processCommand(User user, String command, String[] parameters) {
        if(command.equals("LFG")) {
            usersWaitingForGame.add(user);
            System.out.println("User " + user.getName() + " is looking for a game.");
            user.sendCommand("LFGCONFIRM", "");
            if(usersWaitingForGame.size() > 1) {
                System.out.println("Creating game");
                Game game = new Game(usersWaitingForGame.get(0), usersWaitingForGame.get(1), this);
                games.add(game);
                usersWaitingForGame.remove(0);
                usersWaitingForGame.remove(0);
                Thread gameThread = new Thread(game);
                gameThread.start();
            }
        } else if(command.equals("CANCELLFG")) {
            usersWaitingForGame.remove(user);
            System.out.println("User " + user.getName() + " is no longer looking for a game.");
            user.sendCommand("CANCELCONFIRM", "");
        }
    }

    public void removeGame(Game game) {
        this.games.remove(game);
    }

    public static void main(String[] args) {
        Thread thread = new Thread(new Server());
        thread.start();
    }
}
