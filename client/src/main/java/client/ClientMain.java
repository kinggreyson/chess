package client;

public class ClientMain {
    public static void main(String[] args) {
        int port = 8080;
        new Repl(port).chessRun();
    }
}
