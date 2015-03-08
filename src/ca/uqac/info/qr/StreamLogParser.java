package ca.uqac.info.qr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class StreamLogParser {
  public static void main(String[] args) throws NumberFormatException, IOException {
    if (args.length == 0) {
      return;
    }

    FileWriter writer;
    try {
      writer = new FileWriter(args[0] + "/" + "result.csv");
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    File[] files = new File(args[0]).listFiles();
    parseLogs(files, writer);

    try {
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void parseLogs(File[] files, FileWriter writer) throws NumberFormatException, IOException {
    StringBuilder builder = new StringBuilder();
    try {
      writer
          .write("config size,real max size,rate,sent times,"
              + "sent frames,num of samples,received message(max),received message(min),"
              + "received message(median),received message(mean),received message(SD),"
              + "completion ratio(max),completion ratio(min),"
              + "completion ratio(median),completion ratio(mean),completion ratio(SD),"
              + "time spent(max),time spent(min),"
              + "time spent(median),time spent(mean),time spent(SD)\n");
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    for (File file : files) {
      if (file.isDirectory()) {
        continue;
      }
      if (!file.getName().startsWith("QR_") || !file.getName().endsWith(".csv")) {
        continue;
      }

      String bytes;
      String rate;
      int maxretry;

      String fileName = file.getName();
      String[] parts = fileName.split("\\.");
      fileName = parts[0];
      
      parts = fileName.split("_");
      bytes = parts[2];
      rate = parts[3];
      maxretry = Integer.parseInt(parts[4]);

      BufferedReader br = new BufferedReader(new FileReader(file));
      String line;

      int max = Integer.MIN_VALUE;
      int sent = 0;

      ArrayList<Integer> receiveds = new ArrayList<Integer>();
      ArrayList<Double> receivedsDouble = new ArrayList<Double>();
      ArrayList<Double> completions = new ArrayList<Double>();
      ArrayList<Double> timeSpents = new ArrayList<Double>();

      while ((line = br.readLine()) != null) {
        parts = line.split(",");
        if (parts.length != 5) {
          continue;
        }

        sent = Integer.parseInt(parts[0]);
        receiveds.add(Integer.parseInt(parts[1]));
        receivedsDouble.add(Double.parseDouble(parts[1]));
        completions.add(Double.parseDouble(parts[2]));
        timeSpents.add(Double.parseDouble(parts[3]));
        int m = Integer.parseInt(parts[4]);
        if (m > max) {
          max = m;
        }

      }
      
      try {
        br.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      Collections.sort(receiveds);
      Collections.sort(receivedsDouble);
      Collections.sort(completions);
      Collections.sort(timeSpents);
      
//      .write("config size,real max size,rate,sent times,"
//          + "sent frames,num of samples,received message(max),received message(min),"
//          + "received message(median),received message(mean),received message(SD),"
//          + "completion ratio(max),completion ratio(min),"
//          + "completion ratio(median),completion ratio(mean),completion ratio(SD),"
//          + "time spent(max),time spent(min),"
//          + "time spent(median),time spent(mean),time spent(SD)\n");
      
      builder.setLength(0);
      builder.append(bytes).append(",").append(max).append(",").append(rate)
          .append(",").append(maxretry + 1).append(",").append(sent)
          .append(",").append(receiveds.size()).append(",")
          
          .append(receiveds.get(receiveds.size() - 1)).append(",")
          .append(receiveds.get(0)).append(",")
          .append(getMedian(receivedsDouble)).append(",")
          .append(getMean(receivedsDouble)).append(",")
          .append(getStdDev(receivedsDouble)).append(",")
          
          .append(completions.get(completions.size() - 1)).append(",")
          .append(completions.get(0)).append(",")
          .append(getMedian(completions)).append(",")
          .append(getMean(completions)).append(",")
          .append(getStdDev(completions)).append(",")

          .append(timeSpents.get(timeSpents.size() - 1)).append(",")
          .append(timeSpents.get(0)).append(",")
          .append(getMedian(timeSpents)).append(",")
          .append(getMean(timeSpents)).append(",")
          .append(getStdDev(timeSpents)).append(",")
          
          .append("\n");
      
      try {
        writer.write(builder.toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  static double getMean(ArrayList<Double> data) {
    double sum = 0.0;
    for (double a : data)
      sum += a;
    return sum / data.size();
  }

  static double getVariance(ArrayList<Double> data) {
    double mean = getMean(data);
    double temp = 0;
    for (double a : data)
      temp += (mean - a) * (mean - a);
    return temp / data.size();
  }

  static double getStdDev(ArrayList<Double> data) {
    return Math.sqrt(getVariance(data));
  }

  static double getMedian(ArrayList<Double> data) {
    Collections.sort(data);
    
    int len = data.size();

    if (len % 2 == 0) {
      return (data.get((len / 2) - 1) + data.get(len / 2)) / 2.0;
    } else {
      return data.get(len / 2);
    }
  }
}
