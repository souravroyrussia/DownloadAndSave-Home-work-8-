import java.util.Scanner;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        String command;

        while (true) {
            System.out.println("\n=== DownloadAndSave ===");
            System.out.println("HTTP - Download data");
            System.out.println("FILE - Read saved file");
            System.out.println("DB - Show database records");
            System.out.println("EXIT - Close program");
            System.out.print("Enter command: ");

            command = scanner.nextLine().toUpperCase();

            if (command.equals("HTTP")) {
                try {
                    HttpClient client = HttpClient.newHttpClient();

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://jsonplaceholder.typicode.com/posts/1"))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

                    System.out.println(response.body());
                    Files.writeString(Path.of("data.json"), response.body());
                    System.out.println("Data saved to data.json");
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:downloads.db");

                    Statement statement = connection.createStatement();
                    statement.execute("CREATE TABLE IF NOT EXISTS downloads (data TEXT)");

                    PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO downloads (data) VALUES (?)"
                    );
                    insert.setString(1, response.body());
                    new ProcessBuilder("notepad", "data.json").start();
                    insert.executeUpdate();

                    connection.close();

                    System.out.println("Data saved to database");

                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (command.equals("FILE")) {
                try {
                    String data = Files.readString(Path.of("data.json"));

                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> post = mapper.readValue(data, Map.class);

                    System.out.println(post);
                } catch (Exception e) {
                    System.out.println("Error reading file: " + e.getMessage());
                }
            } else if (command.equals("DB")) {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:sqlite:downloads.db");

                    Statement statement = connection.createStatement();
                    ResultSet result = statement.executeQuery("SELECT * FROM downloads");

                    while (result.next()) {
                        System.out.println(result.getString("data"));
                    }

                    connection.close();

                } catch (Exception e) {
                    System.out.println("Database error: " + e.getMessage());
                }
            } else if (command.equals("EXIT")) {
                System.out.println("Program closed.");
                break;
            } else {
                System.out.println("Unknown command.");
            }
        }

        scanner.close();
    }
}