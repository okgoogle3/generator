package org.example;

import java.util.ArrayList;
import java.util.List;

public class CircleCoordinates {
    public static void main(String[] args) {
        double centerLat = 50.458018;
        double centerLon = 30.505421;
        double radius = 0.0004;
        int numPoints = 6;

        for (int i = 0; i < 6; i++) {
            centerLon = centerLon + radius * 3;

            List<Double> latitudes = new ArrayList<>();
            List<Double> longitudes = new ArrayList<>();
            List<Integer> temps = new ArrayList<>();
            List<Double> batteryLevels = new ArrayList<>();
            List<Boolean> moves = new ArrayList<>();

            for (int k = 0; k < numPoints; k++) {
                double angle = 2 * Math.PI * k / numPoints;
                double lat = round(centerLat + radius * Math.cos(angle), 10);
                double lon = round(centerLon + radius * Math.sin(angle), 10);

                int temperature = (int) Math.round(Math.random() * 100);
                double batteryLevel = round(Math.random() * 100, 2);

                latitudes.add(lat);
                longitudes.add(lon);
                temps.add(temperature);
                batteryLevels.add(batteryLevel);
                moves.add(batteryLevel > 50);
            }

            System.out.println("Counter:");
            System.out.println(i + 1);
            System.out.println("Latitude array:");
            System.out.println(latitudes);
            System.out.println("Longitude array:");
            System.out.println(longitudes);

            System.out.println("Temperature array:");
            System.out.println(temps);
            System.out.println("Battery level array:");
            System.out.println(batteryLevels);
            System.out.println("Moving array:");
            System.out.println(moves);
            System.out.println("\n");
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}