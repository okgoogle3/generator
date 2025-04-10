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
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        String geoFilePath = "/home/thingsboard/Desktop/generator/src/main/resources/apr10.txt";
        String filePath = "/home/thingsboard/Desktop/thingsboard-demos/src/main/resources/asset_tracking/device_emulators.json";
        try {
            count(geoFilePath);
//            parseGeoJson(filePath);
            updateAssetTrackingJson(geoFilePath, filePath, false);
        } catch (IOException e) {
            System.err.println("Error reading the GeoJSON file: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void count(String sourcePath) throws IOException {
        List<Map<String, List<Double>>> res = parseGeoJson(sourcePath, false);
        System.out.println("Size: \n" + res.size());
    }

    private static void updateAssetTrackingJson(String sourcePath, String destinationPath, boolean printArrays) throws Exception {
        List<Map<String, List<Double>>> source = parseGeoJson(sourcePath, printArrays);

        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(new File(destinationPath));

        int count = 0;
        for (JsonNode fullGeneratorInfoJson : rootNode) {
            System.out.println(count);
            ArrayNode telemetryProfiles = (ArrayNode) fullGeneratorInfoJson.get("telemetryProfiles");
            updateCoordinates(telemetryProfiles, source, count);
            updateRssi(telemetryProfiles);
            updateTemperature(telemetryProfiles);
            updateBatteryLevel(telemetryProfiles);
            count++;
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(destinationPath), rootNode);
    }

    private static void updateCoordinates(ArrayNode telemetryProfiles, List<Map<String, List<Double>>> source, int currentEntityNumber) throws Exception {
        JsonNode telemetryNode = getTelemetryProfileByKey(telemetryProfiles, "coordinates").path("valueStrategy").path("telemetry");
        if (telemetryNode.isObject() && source.size() > currentEntityNumber) {
            ArrayNode latitudeArray = (ArrayNode) telemetryNode.get("latitude");
            latitudeArray.removeAll();
            source.get(currentEntityNumber).get("latitude").forEach(latitudeArray::add);

            ArrayNode longitudeArray = (ArrayNode) telemetryNode.get("longitude");
            longitudeArray.removeAll();
            source.get(currentEntityNumber).get("longitude").forEach(longitudeArray::add);
        }
    }

    private static void updateRssi(ArrayNode telemetryProfiles) throws Exception {
        JsonNode otherTelemetryNode = getTelemetryProfileByKey(telemetryProfiles, "other").path("valueStrategy").path("telemetry");
        ArrayNode rssiArray = (ArrayNode) otherTelemetryNode.get("rssi");

//        ArrayNode coordinatesArray = (ArrayNode) getTelemetryProfileByKey(telemetryProfiles, "coordinates").path("valueStrategy").path("telemetry").path("latitude");
//        int rssiArraySize = Math.min(coordinatesArray.size(), 100);

        var rssiNewValues = Utility.generateListOfRandomDoublesWithExtremeValues(100, -100, -60, 0);
        System.out.println("rssi: " + rssiNewValues);

        rssiArray.removeAll();
        rssiNewValues.forEach(rssiArray::add);
    }

    private static void updateTemperature(ArrayNode telemetryProfiles) throws Exception {
        JsonNode otherTelemetryNode = getTelemetryProfileByKey(telemetryProfiles, "other").path("valueStrategy").path("telemetry");
        ArrayNode temperatureArray = (ArrayNode) otherTelemetryNode.get("temperature");

        var temperatureNewValues = Utility.generateListOfRandomDoublesWithExtremeValues(100, 30, 40, 0);
        System.out.println("temperature: " + temperatureNewValues);

        temperatureArray.removeAll();
        temperatureNewValues.forEach(temperatureArray::add);
    }

    private static void updateBatteryLevel(ArrayNode telemetryProfiles) throws Exception {
        JsonNode otherTelemetryNode = getTelemetryProfileByKey(telemetryProfiles, "other").path("valueStrategy").path("telemetry");
        ArrayNode batteryLevelArray = (ArrayNode) otherTelemetryNode.get("batteryLevel");

        var batteryLevelNewValues = Utility.generateListOfRandomDoublesWithExtremeValues(100, 15, 90, 2);
        System.out.println("batteryLevel: " + batteryLevelNewValues);

        batteryLevelArray.removeAll();
        batteryLevelNewValues.forEach(batteryLevelArray::add);
    }

    private static JsonNode getTelemetryProfileByKey(ArrayNode telemetryProfiles, String key) throws Exception {
        return StreamSupport.stream(telemetryProfiles.spliterator(), false)
                .filter(node -> key.equals(node.get("key").asText()))
                .findFirst().orElseThrow(() -> new Exception("Telemetry profile with key \"" + key + "\" not found"));
    }

    private static List<Map<String, List<Double>>> parseGeoJson(String filePath, boolean printArrays) throws IOException {
        List<JsonNode> geometryNodeList = getListOfGeometries(filePath);
        List<Map<String, List<Double>>> result = new ArrayList<>();
        short currentTrip = 1;

        for (JsonNode geometryNode : geometryNodeList) {
            List<Double> latitudes = new ArrayList<>();
            List<Double> longitudes = new ArrayList<>();
            if (geometryNode.get("type").asText().equals("Point")) {
                longitudes.add(Utility.round(geometryNode.get("coordinates").get(0).asDouble(), 10)); // Longitude comes first in GeoJSON
                latitudes.add(Utility.round(geometryNode.get("coordinates").get(1).asDouble(), 10)); // Latitude comes second
            }

            if (geometryNode.get("type").asText().equals("LineString")) {
                for (JsonNode coordinate : geometryNode.get("coordinates")) {
                    if (coordinate.isArray() && coordinate.size() >= 2) {
                        longitudes.add(Utility.round(coordinate.get(0).asDouble(), 10)); // Longitude comes first in GeoJSON
                        latitudes.add(Utility.round(coordinate.get(1).asDouble(), 10)); // Latitude comes second
                    }
                }
            }

            if (printArrays) {
                System.out.println("Trip " + currentTrip++ + ":");
                System.out.println("Longitudes: \n" + longitudes);
                System.out.println("Latitudes: \n" + latitudes);
                System.out.println();
            }

            result.add(
                    Map.of(
                            "longitude", longitudes,
                            "latitude", latitudes
                    )
            );
        }
        return result;
    }

    private static List<JsonNode> getListOfGeometries(String filePath) throws IOException {
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
