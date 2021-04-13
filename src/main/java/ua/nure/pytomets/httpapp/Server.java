package ua.nure.pytomets.httpapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.Arrays.stream;

public class Server {

    private static final int DEFAULT_PORT = 7777;

    public static void main(String[] args) throws IOException {
        System.out.println("Please, type a port to connect to: ");
        Scanner in = new Scanner(System.in);
        var typedPort = in.nextLine();
        var port = typedPort.isBlank() ? DEFAULT_PORT : parseInt(typedPort);
        System.out.printf("Connected to port: %s\n", port);
        try (var serverSocket = new ServerSocket(port)) {
            while (true) {
                try (var client = serverSocket.accept()) {
                    handlePostLongestWord(client);
                }
            }
        }
    }

    private static void handlePostLongestWord(Socket socket) throws IOException {
        var br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        var requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }

        var request = requestBuilder.toString();
        var requestsLines = request.split("\r\n");
        var requestLine = requestsLines[0].split(" ");
        var method = requestLine[0];
        System.out.printf("Method %s\n", method);
        if (!method.trim().equalsIgnoreCase("post")) {
            return;
        }
        var path = requestLine[1];
        var version = requestLine[2];
        var host = requestsLines[1].split(" ")[1];

        var body = new StringBuilder();
        while (br.ready()) {
            body.append((char) br.read());
        }

        var headers = new ArrayList<>();
        for (int h = 2; h < requestsLines.length; h++) {
            var header = requestsLines[h];
            headers.add(header);
        }

        var accessLog = format("Client %s,\n method %s,\n path %s,\n version %s,\n host %s,\n body %s,\n headers %s\n",
                socket.toString(), method, path, version, host, body, headers.toString());
        System.out.println(accessLog);

        var longestWord = getLongestWord(body.toString());
        if (longestWord.isPresent()) {
            var word = longestWord.get();
            System.out.printf("The longest word: %s.", word);
            sendResponse(socket, "200 OK", word.getBytes());
        } else {
            var notFoundContent = "Longest word not found in the request.";
            sendResponse(socket, "404 Not Found", notFoundContent.getBytes());
        }
    }

    private static Optional<String> getLongestWord(String text) {
        var words = text
                .replaceAll("[ ]{2,}", " ")
                .split(" ");
        return stream(words)
                .reduce((String longest, String current) -> {
                    return longest.length() > current.length()
                            ? longest
                            : current;
                });
    }

    private static void sendResponse(Socket client, String status, byte[] content) throws IOException {
        var clientOutput = client.getOutputStream();
        clientOutput.write(("HTTP/1.1 " + status).getBytes());
        clientOutput.write(("ContentType: text/html; charset=UTF-8" + "\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(content);
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }
}
