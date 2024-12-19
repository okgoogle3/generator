package org.example;

import org.example.util.Utility;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import static org.example.util.Utility.round;

public class CircleCoordinates {
    public static void main(String[] args) {
        double centerLat = 50.458018;
        double centerLon = 30.505421;
        double radius = 0.0004;
        int numPoints = 8;
        int size = 16;

        for (int i = 0; i < 9; i++) {
            centerLon = centerLon + radius * 3;

            System.out.println("Counter:");
            System.out.println(i + 1);
            printPolygonCoordinatesWithRadius(centerLat, centerLon, radius, numPoints);
            printIntArrayWithValuesInRange("Temperature", 30, 50, size);
            printDoubleArrayWithRoundedValuesInRange("Battery level", 10, 90, size, 2);
            printIntArrayWithValuesInRange("Rssi", -120, -50, size);
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

    private static void printIntArrayWithValuesInRange(String valueName, int minValue, int maxValue, int arraySize) {
        List<Integer> array = new ArrayList<>();
        for (int k = 0; k < arraySize; k++) {
            int value = (int) Utility.generateDoubleInRange(minValue, maxValue);
            array.add(value);
        }

        System.out.println(valueName + " array:");
        System.out.println(array);
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

    private static void printDoubleArrayWithRoundedValuesInRange(String valueName, int minValue, int maxValue, int arraySize, int valueRoundingOffset) {
        List<Double> array = new ArrayList<>();
        for (int k = 0; k < arraySize; k++) {
            double value = Utility.generateRoundedDoubleInRange(minValue, maxValue, valueRoundingOffset);
            array.add(value);
        }

        System.out.println(valueName + " array:");
        System.out.println(array);
    }
}