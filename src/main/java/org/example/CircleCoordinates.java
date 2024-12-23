package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.example.util.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.example.util.Utility.round;

public class CircleCoordinates {
    public static void main(String[] args) throws IOException {
        double centerLat = 50.458018;
        double centerLon = 30.505421;
        double radius = 0.0004;
        int numPoints = 8;
        int size = 30;

        String filePath = "/home/andrii/IdeaProjects/thingsboard-demos/src/main/resources/asset_tracking/device_emulators.json";
        updateAssetTrackingJson(filePath);

        for (int i = 0; i < 25; i++) {
            centerLon = centerLon + radius * 3;

            System.out.println("Counter:");
            System.out.println(i + 1);

//            printPolygonCoordinatesWithRadius(centerLat, centerLon, radius, numPoints);
//            printIntArrayWithValuesInRange("Temperature", 30, 50, size);
//            printDoubleArrayWithRoundedValuesInRange("Battery level", 10, 90, size, 2);
//            printIntArrayWithValuesInRange("Rssi", -120, -50, size);
            System.out.println("\n");
        }
    }

    private static void printPolygonCoordinatesWithRadius(double centerLat, double centerLon, double radius, int numberOfPoints) {
        List<Double> latitudes = new ArrayList<>();
        List<Double> longitudes = new ArrayList<>();
        for (int k = 0; k < numberOfPoints; k++) {
            double angle = 2 * Math.PI * k / numberOfPoints;
            double lat = round(centerLat + radius * Math.cos(angle), 14);
            double lon = round(centerLon + radius * Math.sin(angle), 14);
            latitudes.add(lat);
            longitudes.add(lon);
        }

        System.out.println("Latitude array:");
        System.out.println(latitudes);
        System.out.println("Longitude array:");
        System.out.println(longitudes);
    }

    private static List<Integer> printIntArrayWithValuesInRange(String valueName, int minValue, int maxValue, int arraySize) {
        List<Integer> array = new ArrayList<>();
        for (int k = 0; k < arraySize; k++) {
            int value = (int) Utility.generateDoubleInRange(minValue, maxValue);
            array.add(value);
        }

        System.out.println(valueName + " array:");
        System.out.println(array);
        return array;
    }

    private static void printDoubleArrayWithValuesInRange(String valueName, int minValue, int maxValue, int arraySize) {
        List<Double> array = new ArrayList<>();
        for (int k = 0; k < arraySize; k++) {
            double value = Utility.generateDoubleInRange(minValue, maxValue);
            array.add(value);
        }

        System.out.println(valueName + " array:");
        System.out.println(array);
    }

    private static List<Double> printDoubleArrayWithRoundedValuesInRange(String valueName, int minValue, int maxValue, int arraySize, int valueRoundingOffset) {
        List<Double> array = new ArrayList<>();
        for (int k = 0; k < arraySize; k++) {
            double value = Utility.generateRoundedDoubleInRange(minValue, maxValue, valueRoundingOffset);
            array.add(value);
        }

        System.out.println(valueName + " array:");
        System.out.println(array);
        return array;
    }

    private static void updateAssetTrackingJson(String destinationPath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(new File(destinationPath));

        for (JsonNode jsonNode : rootNode) {
            ArrayNode telemetryProfiles = (ArrayNode) jsonNode.get("telemetryProfiles");
            JsonNode firstProfile = StreamSupport.stream(telemetryProfiles.spliterator(), false).filter(node -> "other".equals(node.get("key").asText())).findFirst().get();
            JsonNode telemetryNode = firstProfile.path("valueStrategy").path("telemetry");
            if (telemetryNode.isObject()) {
                ArrayNode rssiNode = (ArrayNode) telemetryNode.get("rssi");
                rssiNode.removeAll();
                printIntArrayWithValuesInRange("Rssi", -119, -51, 60).forEach(rssiNode::add);

                ArrayNode batteryNode = (ArrayNode) telemetryNode.get("batteryLevel");
                batteryNode.removeAll();
                printDoubleArrayWithRoundedValuesInRange("Battery level", 15, 90, 60, 2).forEach(batteryNode::add);

                ArrayNode temperatureNode = (ArrayNode) telemetryNode.get("temperature");
                temperatureNode.removeAll();
                printIntArrayWithValuesInRange("Temperature", 25, 65, 60).forEach(temperatureNode::add);
            }
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(destinationPath), rootNode);
    }
}
