import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final ExecutorService executorService;
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");

    public Server(int port, int threadPool) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(threadPool);
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    executorService.execute(() -> {
                        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                             final var out = new BufferedOutputStream(socket.getOutputStream())) {
                            final var requestLine = in.readLine();
                            final var parts = requestLine.split(" ");
                            handling(parts, out);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handling(String[] parts, BufferedOutputStream out) {
        final var path = parts[1];
        final var filePath = Path.of(".", "public", path);
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (parts.length != 3 || !this.validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();

            } else if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();

            } else {
                final var length = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



