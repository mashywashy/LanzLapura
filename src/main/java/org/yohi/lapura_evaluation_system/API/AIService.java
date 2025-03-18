// AIService.java
package org.yohi.lapura_evaluation_system.API;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class AIService {
    private static final String API_URL = "https://jahnissi-api.yeems214.xyz/blackniggerdudes";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String getRecommendations(String program, int year, int semester,
                                     Map<String, Boolean> subjects) throws Exception {
        validateInputs(program, year, semester);

        JSONObject payload = new JSONObject();
        payload.put("program", program);
        payload.put("year", year);
        payload.put("sem", semester);
        payload.put("subs", new JSONObject(subjects));

        HttpRequest request = HttpRequest.newBuilder()
                .POST(BodyPublishers.ofString(payload.toString()))
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return parseResponse(response.body());
        }
        throw new RuntimeException("API Error: " + response.statusCode());
    }

    private String parseResponse(String jsonResponse) {
        // Add JSON parsing logic here if needed
        return jsonResponse;
    }

    private void validateInputs(String program, int year, int semester) {
        if (!List.of("BSIT", "BSA", "BSMT", "BSN").contains(program.toUpperCase())) {
            throw new IllegalArgumentException("Invalid program");
        }
        if (year < 1 || year > 4) throw new IllegalArgumentException("Invalid year");
        if (semester < 1 || semester > 2) throw new IllegalArgumentException("Invalid semester");
    }
}