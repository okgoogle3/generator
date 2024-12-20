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
        List<JsonNode> geometryNode = getJsonNode(filePath);
        short count = 1;

        for (JsonNode geometry : geometryNode) {
            List<Double> latitudes = new ArrayList<>();
            List<Double> longitudes = new ArrayList<>();
            if (geometry.get("type").asText().equals("Point")) {
                longitudes.add(Utility.round(geometry.get("coordinates").get(0).asDouble(), 14)); // Longitude comes first in GeoJSON
                latitudes.add(Utility.round(geometry.get("coordinates").get(1).asDouble(), 14)); // Latitude comes second
            }
            for (JsonNode coordinate : geometry.get("coordinates")) {
                if (coordinate.isArray() && coordinate.size() >= 2) {
                    longitudes.add(Utility.round(coordinate.get(0).asDouble(), 14)); // Longitude comes first in GeoJSON
                    latitudes.add(Utility.round(coordinate.get(1).asDouble(), 14)); // Latitude comes second
                }
            }
            System.out.println("Trip " + count++ + ":");
            System.out.println("Longitudes: \n" + longitudes);
            System.out.println("Latitudes: \n" + latitudes);
            System.out.println();
        }
    }

    private static List<JsonNode> getJsonNode(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(filePath));
        ArrayNode arrayNode = (ArrayNode) rootNode.at("/features");
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode jsonNode : arrayNode) {
            result.add(jsonNode.at("/geometry"));
        }
        return result;
    }
}
