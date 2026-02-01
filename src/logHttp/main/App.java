import server.Server;

public class App {
    public static void main(String[] args) {
        try {
            Server server = new Server(1337, 8);
            server.start();

        } catch (Exception e) {
            System.out.println("Á");
        }
    }
}