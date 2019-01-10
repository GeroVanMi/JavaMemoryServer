import java.io.*;
import java.net.Socket;

public class User implements Runnable {
    private String name;
    private BufferedReader reader;
    private PrintWriter writer;
    private Server server;
    private Game currentGame;

    public User(String name, Socket clientSocket, Server server) {
        try {
            this.name = name;
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream());
            this.server = server;
            this.currentGame = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String command, String parameters) {
        // PATTERN: COMMAND/COMMANDNAME/PARAMETER/PARAMETER2/...
        String commandline = "COMMAND/" + command + "/" + parameters;
        writer.println(commandline);
        writer.flush();
    }

    public String getName() {
        return name;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setCurrentGame(Game game) {
        this.currentGame = game;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // PATTERN: COMMAND/COMMANDNAME/PARAMETER/PARAMETER2
                String[] arguments = line.split("/");
                // If the first split isn't "COMMAND" something is wrong
                if(arguments[0].equals("COMMAND")) {
                    // Figure out the parameters and put them into a separate array
                    String[] parameters = new String[arguments.length - 2];
                    for(int i=2; i < arguments.length; i++) {
                        parameters[i-2] = arguments[i];
                    }
                    // Send the command + parameters to the server to be processed
                    if(currentGame != null) {
                        currentGame.processCommand(this, arguments[1], parameters);
                    } else {
                        server.processCommand(this, arguments[1], parameters);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
