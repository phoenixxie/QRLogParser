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

public class LakeLogParser {
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
          .write("config_size,cps,sent_times,"
              + "num_of_samples,actual_max_frame_size,regenerate_times,"
              + "times(max),times(min),"
              + "times(median),times(mean),times(SD),"
              + "time_spent(max),time_spent(min),"
              + "time_spent(median),time_spent(mean),time_spent(SD)\n");
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

      int maxFrameSize = Integer.MIN_VALUE;
      int maxRegen = Integer.MIN_VALUE;

      ArrayList<Integer> times = new ArrayList<Integer>();
      ArrayList<Double> timesDouble = new ArrayList<Double>();
      ArrayList<Double> timeSpents = new ArrayList<Double>();

      while ((line = br.readLine()) != null) {
        parts = line.split(",");
        if (parts.length != 4) {
          continue;
        }

        times.add(Integer.parseInt(parts[0]));
        timesDouble.add(Double.parseDouble(parts[0]));
        timeSpents.add(Double.parseDouble(parts[1]));
        int m = Integer.parseInt(parts[2]);
        if (m > maxFrameSize) {
          maxFrameSize = m;
        }
        m = Integer.parseInt(parts[3]);
        if (m > maxRegen) {
          maxRegen = m;
        }
      }
      
      try {
        br.close();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      Collections.sort(times);
      Collections.sort(timesDouble);
      Collections.sort(timeSpents);

      builder.setLength(0);
      builder.append(bytes).append(",")
          .append(rate).append(",")
          .append(maxretry + 1).append(",").append(times.size()).append(",")
          .append(maxFrameSize).append(",")
          .append(maxRegen).append(",")

          .append(times.get(times.size() - 1)).append(",")
          .append(times.get(0)).append(",")
          .append(getMedian(timesDouble)).append(",")
          .append(getMean(timesDouble)).append(",")
          .append(getStdDev(timesDouble)).append(",")

          .append(timeSpents.get(timeSpents.size() - 1)).append(",")
          .append(timeSpents.get(0)).append(",")
          .append(getMedian(timeSpents)).append(",")
          .append(getMean(timeSpents)).append(",")
          .append(getStdDev(timeSpents))
          
          .append("\r\n");
      
      try {
        System.err.println(builder.toString());
        writer.write(builder.toString());
        writer.flush();
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
