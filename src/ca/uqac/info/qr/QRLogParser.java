package ca.uqac.info.qr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class QRLogParser {
	public static void main(String[] args) {
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

	public static String tail(File file) {
		RandomAccessFile fileHandler = null;
		try {
			fileHandler = new RandomAccessFile(file, "r");
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					if (filePointer == fileLength) {
						continue;
					}
					break;

				} else if (readByte == 0xD) {
					if (filePointer == fileLength - 1) {
						continue;
					}
					break;
				}

				sb.append((char) readByte);
			}

			String lastLine = sb.reverse().toString();
			return lastLine;
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (fileHandler != null)
				try {
					fileHandler.close();
				} catch (IOException e) {
					/* ignore */
				}
		}
	}

	public static void parseLogs(File[] files, FileWriter writer) {
		StringBuilder builder = new StringBuilder();
		try {
			writer.write("size per frame,frames per second,error correction level,"
						+ "sent message,captured message,decoded message,matched message,missed message,"
						+ "decoded ratio,matched ratio\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		for (File file : files) {
			if (file.isDirectory()) {
				continue;
			}
			if (!file.getName().startsWith("QR_")
					|| !file.getName().endsWith(".csv")) {
				continue;
			}

			String bytes;
			String rate;
			String level;

			String fileName = file.getName();
			String[] parts = fileName.split("_");
			bytes = parts[1];
			rate = parts[2];
			level = parts[3];

			parts = tail(file).split(",");
			int sent = Integer.parseInt(parts[1]);
			int captured = Integer.parseInt(parts[2]);
			int decoded = Integer.parseInt(parts[3]);
			int matched = Integer.parseInt(parts[4]);
			int missed = Integer.parseInt(parts[5]);

			builder.setLength(0);	
			builder.append(bytes).append(",").append(rate).append("(x1),")
					.append(level).append(",")
					.append(sent).append(",")
					.append(captured).append(",")
					.append(decoded).append(",")
					.append(matched).append(",")
					.append(missed).append(",")
					.append((float) decoded / (float) captured)
					.append(",").append((float) matched / (float) (matched + missed))
					.append("\n");
			try {
				writer.write(builder.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
