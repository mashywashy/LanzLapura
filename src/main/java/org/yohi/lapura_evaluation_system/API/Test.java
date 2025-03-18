package org.yohi.lapura_evaluation_system.API;

import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
        try {
            AIService service = new AIService();

            // Test parameters
            String program = "BSIT";
            int year = 1;
            int semester = 1;

            // Sample subjects (modify with actual subject codes from your curriculum)
            Map<String, Boolean> subjects = new HashMap<>();
            subjects.put("eng100", true);
            subjects.put("math100", true);
            subjects.put("acctg101", false);
            subjects.put("pe101", true);

            // Get recommendations
            String response = service.getRecommendations(program, year, semester, subjects);

            System.out.println("API Response:");
            System.out.println(response);

        } catch (IllegalArgumentException e) {
            System.err.println("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}