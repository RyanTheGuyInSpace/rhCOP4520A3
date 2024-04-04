package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class Main {
    public static void main(String[] args) throws InterruptedException {
        // 8 temperature sensors, 8 threads
        Thread[] sensors = new Thread[8];
        for (int i = 0; i < 8; i++) {
            sensors[i] = new Thread(new TemperatureSensor(i));
            sensors[i].start();
        }

        // One more thread for the report
        Thread reportThread = new Thread(new HourlyReport());
        reportThread.start();

        for (Thread sensor : sensors) {
            sensor.join();
        }
    }
}

class TemperatureSensor implements Runnable {
    public static final List<Double> temperatures = new CopyOnWriteArrayList<>();
    private final int sensorId;

    public TemperatureSensor(int sensorId) {
        this.sensorId = sensorId;
    }

    @Override
    public void run() {
        Random random = new Random();
        for (int hour = 0; hour < 2; hour++) {
            for (int minute = 0; minute < 60; minute++) {
                // Generate random temperature between -100F and 70F
                double temperature = random.nextDouble() * 170 - 100;
                temperatures.add(temperature);
            }
        }
    }
}

class HourlyReport implements Runnable {
    private static final List<Double> temperatures = TemperatureSensor.temperatures;

    @Override
    public void run() {
        generateReport();
    }

    private void generateReport() {
        List<Double> tempList = new ArrayList<>(temperatures);
        Collections.sort(tempList);

        int size = tempList.size();
        System.out.println("Top 5 highest temperatures:");
        for (int i = size - 1; i >= size - 5; i--) {
            System.out.println(tempList.get(i));
        }

        System.out.println("\nTop 5 lowest temperatures:");
        for (int i = 0; i < 5; i++) {
            System.out.println(tempList.get(i));
        }

        System.out.println("\nLargest temperature difference in 10 minutes:");
        double maxDiff = 0;
        int startIndex = 0, endIndex = 0;
        for (int i = 0; i < size - 10; i++) {
            // Get max and min temperatures
            double minTemp = Collections.min(tempList.subList(i, i + 10));
            double maxTemp = Collections.max(tempList.subList(i, i + 10));
            double diff = maxTemp - minTemp;
            if (diff > maxDiff) {
                maxDiff = diff;
                startIndex = i;
                endIndex = i + 10;
            }
        }

        System.out.println("Maximum difference: " + maxDiff);
        System.out.println("Start index: " + startIndex);
        System.out.println("End index: " + endIndex);
    }
}

