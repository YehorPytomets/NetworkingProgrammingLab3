package ua.nure.pytomets.httpapp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import static java.net.http.HttpRequest.BodyPublishers.ofString;

public class Client {

    public static void main(String[] args) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        var text = "Please give  me the   looongest word in this string    .";
        System.out.println(text);
        var request = HttpRequest
                .newBuilder(URI.create("http://localhost:8080/"))
                .POST(ofString(text))
                .build();
        var response = client.send(request,
                BodyHandlers.ofString());
        System.out.printf("The longest word is '%s'", response.body());
    }
}
