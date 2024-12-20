package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.example.util.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeoJsonToArrays {
    public static void main(String[] args) {
        String filePath = "/home/thingsboard/Desktop/generator/src/main/resources/test.txt";

        try {
            parseGeoJson(filePath);
        } catch (IOException e) {
            System.err.println("Error reading the GeoJSON file: " + e.getMessage());
        }
    }


    public static void parseGeoJson(String filePath) throws IOException {
        // Initialize the ObjectMapper for parsing JSON
        List<JsonNode> coordinatesNode = getJsonNode(filePath);

        // Lists to store latitudes and longitudes
        List<Double> latitudes = new ArrayList<>();
        List<Double> longitudes = new ArrayList<>();

        // Extract the latitude and longitude values
        for (JsonNode coordinate : coordinatesNode) {
            if (coordinate.isArray() && coordinate.size() >= 2) {
                longitudes.add(Utility.round(coordinate.get(0).asDouble(), 14)); // Longitude comes first in GeoJSON
                latitudes.add(Utility.round(coordinate.get(1).asDouble(), 14)); // Latitude comes second
            }
        }
        System.out.println("Longitudes: \n" + longitudes);
        System.out.println("Latitudes: \n" + latitudes);

    }

    private static List<JsonNode> getJsonNode(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(filePath));
        ArrayNode arrayNode = (ArrayNode) rootNode.at("/features");
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode jsonNode : arrayNode) {
            result.add(jsonNode.at("/geometry/coordinates"));
        }
        return result;
    }
}
