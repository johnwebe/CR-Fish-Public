package com.clashroyale;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
//this class just became useless
public class CSVHandler {
    private ArrayList<String> pastFameCSV = new ArrayList<String>();
    private ArrayList<String> currentFameCSV = new ArrayList<String>();
    public CSVHandler() throws CsvValidationException, IOException{
        long startTime = System.nanoTime();
        String csvUrl = "https://royaleapi.com/clan/GCG2RJP0/war/analytics/csv";
        String currentFameURL = "https://royaleapi.com/clan/GCG2RJP0/war/race/csv";
        try {
            URL url = new URL(csvUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/csv, application/csv, application/octet-stream");
            
            //this session cookie doesn't look like it expires as long as i keep my window open. Owner is only allowed to change this value
            
            connection.setInstanceFollowRedirects(true);
            // Check if the response is actually CSV
            String contentType = connection.getContentType();
            System.out.println("Content-Type: " + contentType);
            System.out.println(connection.getHeaderFields());
            
            if (contentType != null && !contentType.contains("csv")) {
                System.out.println("The response is not a CSV file. Check the URL or authentication.");
                return;
            }
            
            // Read CSV data. I'm also filtering the data here because people make weird names and if the regex fucks up, then problems will arise. Hence the two for loops
            try (CSVReader reader = new CSVReader(new InputStreamReader(connection.getInputStream()))) {
                String[] line;
                while ((line = reader.readNext()) != null) {
                    String lineString = "";
                    for(int i = 0; i < 2; i++){
                        lineString += line[i] + " ";
                    }
                    for(int i = 3; i < line.length; i++){
                        lineString += line[i] + " ";
                    }
                    //System.out.println(lineString);
                    pastFameCSV.add(lineString);    
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Size of past fame CSV: "+ pastFameCSV.size());
        //current fame
        try {
            URL url = new URL(currentFameURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/csv, application/csv, application/octet-stream");
            //this session cookie doesn't look like it expires as long as i keep my window open. Owner is only allowed to change this value

            connection.setInstanceFollowRedirects(true);
            // Check if the response is actually CSV
            String contentType = connection.getContentType();
            System.out.println("Content-Type: " + contentType);
            //System.out.println(connection.getHeaderFields());
            
            if (contentType != null && !contentType.contains("csv")) {
                System.out.println("The response is not a CSV file. Check the URL or authentication.");
                return;
            }
            
            // Read CSV data. I'm also filtering the data here because people make weird names and if the regex fucks up, then problems will arise. Hence the two for loops
            try (CSVReader reader = new CSVReader(new InputStreamReader(connection.getInputStream()))) {
                String[] line;
                while ((line = reader.readNext()) != null) {
                    //System.out.println(line[8] + " " + line[9]);
                    currentFameCSV.add(line[8] + " " + line[9]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Size of currentFameCSV: " + currentFameCSV.size());
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println(totalTime / Math.pow(10, 9) + " seconds to run CSV handler");
        }
        
        public void printCSV(){
        //index of 0 is the type of data, the rest of the index is player information
            for(String i: pastFameCSV){
                System.out.println(i);
            }
        }
        
        public ArrayList<String> getCSV(){
            return pastFameCSV;
        }

        public ArrayList<String> getCurrentCSV(){
            return currentFameCSV;
        }
}
