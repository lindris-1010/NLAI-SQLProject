import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ChatGPTClient {

    private static final String API_KEY = "";
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    class Msg {
        final String role;
        final String content;
        public Msg(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    class GPTRequest {
        final String model;
        final Msg[] messages;
        public GPTRequest(String model, Msg message) {
            this.model = model;
            this.messages = new Msg[1];
            this.messages[0] = message;
        }
    }
    class Message {
        String role;
        String content;
    }
    class Choice {
        Message message;
    }
    class GPTResponse {
        List<Choice> choices;
    }

    public String sendAndConvertMessage(String userMessage) {
        String json = sendMessage(userMessage);
        Gson gson = new Gson();
        GPTResponse gptResponse = gson.fromJson(json, GPTResponse.class);

        return gptResponse.choices.get(0).message.content;
    }

    public String sendMessage(String userMessage) {
        Msg message = new Msg("user", userMessage);

        GPTRequest gptRequest = new GPTRequest("gpt-3.5-turbo", message);

        Gson gson = new Gson();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(gptRequest)))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            System.out.println("Error encountered while sending request:");
            e.getStackTrace();
            return "Error encountered while sending request";
        }
    }
}
