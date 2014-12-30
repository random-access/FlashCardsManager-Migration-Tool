package utils;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import core.TransferController;

public class FileUtils {

	public static void createDirectory(String name) {
		File f = new File(name);
		if (!f.exists()) {
			f.mkdir();
		}
	}

	public static void deleteDirectory(String pathToDirectory) {
		Path dir = Paths.get(pathToDirectory);

		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					System.out.println("Deleting file: " + file);
					File f = new File(file.toString());
					boolean success = f.canWrite();
					if (success) {
						System.out.println("File " + file + " is writable!");
					} else {
						System.out.println("!!! File " + file + " is not writable!");
					}
					Files.delete(file);
					return CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

					System.out.println("Deleting dir: " + dir);
					if (exc == null) {
						Files.delete(dir);
						return CONTINUE;
					} else {
						throw exc;
					}
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String appDirectory(String name, TransferController ctl) {
		String OS = System.getProperty("os.name").toUpperCase();
		// Windows:
		if (OS.contains("WIN")) {
			ctl.setStatus("Betriebssystem: Windows \nLernkarten-Verzeichnis: " + System.getenv("APPDATA") + "/" + name);
			return System.getenv("APPDATA") + "/" + name;
		}
		// Mac:
		else if (OS.contains("MAC")) {
			ctl.setStatus("Betriebssystem: Mac \nLernkarten-Verzeichnis: " + System.getProperty("user.home") + "/Library" + "/" + name);
			return System.getProperty("user.home") + "/Library" + "/" + name;
		}
		// Linux:
		else if (OS.contains("NUX")) {
			ctl.setStatus("Betriebssystem: Linux \nLernkarten-Verzeichnis: " + System.getProperty("user.home") + "/." + name);
			return System.getProperty("user.home") + "/." + name;
		} else {
			ctl.setStatus("Betriebssystem: unbekannt \nLernkarten-Verzeichnis: " + System.getProperty("user.dir") + "/." + name);
			return System.getProperty("user.dir") + "/." + name;
		}
	}

	public static boolean directoryContainsOnlyCertainFiles(String pathToDirectory, String[] allowedNames) {
		File dir = new File(pathToDirectory);
		if (!dir.isDirectory()) {
			return false;
		} else {
			boolean result = true;
			File[] dirFiles = dir.listFiles();
			for (File file : dirFiles) {
				boolean b = false;
				for (String name : allowedNames) {
					if (file.getName().equals(name)) {
						b = true;
					}
				}
				result &= b;
			}
			return result;
		}
	}
}
