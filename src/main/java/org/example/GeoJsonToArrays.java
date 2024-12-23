package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.example.util.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class GeoJsonToArrays {
    public static void main(String[] args) {
        String filePath = "/home/andrii/IdeaProjects/generator/src/main/resources/test.txt";

        try {
//            parseGeoJson(filePath);
            updateAssetTrackingJson();
        } catch (IOException e) {
            System.err.println("Error reading the GeoJSON file: " + e.getMessage());
        }
    }


    public static List<Map<String, List<Double>>> parseGeoJson(String filePath) throws IOException {
        List<JsonNode> geometryNode = getJsonNode(filePath);
        short count = 1;
        List<Map<String, List<Double>>> result = new ArrayList<>();
        for (JsonNode geometry : geometryNode) {
            List<Double> latitudes = new ArrayList<>();
            List<Double> longitudes = new ArrayList<>();
            if (geometry.get("type").asText().equals("Point")) {
                longitudes.add(Utility.round(geometry.get("coordinates").get(0).asDouble(), 10)); // Longitude comes first in GeoJSON
                latitudes.add(Utility.round(geometry.get("coordinates").get(1).asDouble(), 10)); // Latitude comes second
            }
            for (JsonNode coordinate : geometry.get("coordinates")) {
                if (coordinate.isArray() && coordinate.size() >= 2) {
                    longitudes.add(Utility.round(coordinate.get(0).asDouble(), 10)); // Longitude comes first in GeoJSON
                    latitudes.add(Utility.round(coordinate.get(1).asDouble(), 10)); // Latitude comes second
                }
            }
            System.out.println("Trip " + count++ + ":");
            System.out.println("Longitudes: \n" + longitudes);
            System.out.println("Latitudes: \n" + latitudes);
            System.out.println();
            result.add(Map.of("longitude", longitudes,
                    "latitude", latitudes));
        }
        return result;
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

    private static void updateAssetTrackingJson() throws IOException {
        String geoFilePath = "/home/andrii/IdeaProjects/generator/src/main/resources/test.txt";
        String filePath = "/home/andrii/IdeaProjects/thingsboard-demos/src/main/resources/asset_tracking/device_emulators.json";
        List<Map<String, List<Double>>> res = parseGeoJson(geoFilePath);

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(new File(filePath));

        int count = 0;
        for (JsonNode jsonNode : rootNode) {

            ArrayNode telemetryProfiles = (ArrayNode) jsonNode.get("telemetryProfiles");
            JsonNode firstProfile = StreamSupport.stream(telemetryProfiles.spliterator(), false).filter(node -> "coordinates".equals(node.get("key").asText())).findFirst().get();
            JsonNode telemetryNode = firstProfile.path("valueStrategy").path("telemetry");
            if (telemetryNode.isObject()) {
                ArrayNode latitudeArray = (ArrayNode) telemetryNode.get("latitude");
                latitudeArray.removeAll();
                res.get(count).get("latitude").forEach(latitudeArray::add);

                ArrayNode longitudeArray = (ArrayNode) telemetryNode.get("longitude");
                longitudeArray.removeAll();
                res.get(count).get("longitude").forEach(longitudeArray::add);
            }
            count++;
        }


        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), rootNode);
    }
}
