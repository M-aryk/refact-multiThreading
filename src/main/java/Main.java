public class Main {
    public static void main(String[] args) {
        int port = 9999;
        int threads = 64;
        final var server = new Server(port, threads);

        server.listen(port);
    }

}
