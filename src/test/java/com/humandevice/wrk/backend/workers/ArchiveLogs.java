package com.humandevice.wrk.backend.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.apache.log4j.Logger;

/**
 * @author michal
 * Klasa do przepisywania plików logów do archiwum
 */
public class ArchiveLogs extends Worker {

	/**
	 * Inicjalizacja zmiennych do logowania, do ścieżek oraz do zapisywania
	 * czasu ostatniego uruchomienia
	 */
	private Logger logger = Logger.getLogger(ArchiveLogs.class);
	private String path1, path2;
	private long lastRun = 0;

	@Override
	public void process() {

		logger.info("Processing logs");
		lastRun = System.currentTimeMillis();

		/**
		 * Pobieranie ścieżek do plików logów z bazy
		 */
		path1 = configuration.getParameter("log_file_source_path").trim();
		path2 = configuration.getParameter("log_file_archive_path").trim();

		/**
		 * Update ostatniego uruchomienia workera w bazie
		 */

		workerActivityLogEntry("archive");
		
		/**
		 * Sprawdzane warunków przepisania pliku logów
		 */
		checkSize(path1);

	}

	@Override
	public boolean checkConditions() {
		long currentTime = System.currentTimeMillis();
		return (currentTime - lastRun) > 50 * 1000;
	}

	/**
	 * Sprawdzanie czy plik podany w @param jest większy niż 10 MB
	 * @param path1
	 */
	public void checkSize(String path1) {
		File file = new File(path1);

		if (file.exists()) {

			double bytes = file.length();
			double kilobytes = (bytes / 1024);
			double megabytes = (kilobytes / 1024);

			if (megabytes > 10) {
				logger.info("Przepisywanie pliku logów do archiwum");
				copyFile(path1, path2);
				clearFile(path1);
			}

		} else {
			logger.info("Nie ma pliku logów!!!");
		}
	}

	/**
	 * Przepisanie pliku z path1 do path2
	 * Jeżeli plik nie istnieje, tworzymy nowy
	 * @param path1
	 * @param path2
	 */
	public void copyFile(String path1, String path2) {
		InputStream inStream = null;
		OutputStream outStream = null;

		try {

			File afile = new File(path1);
			File bfile = new File(path2);

			if (bfile.createNewFile()) {
				logger.info("Plik zostal stworzony!");
			} else {
				logger.info("Plik juz istnieje");
			}

			inStream = new FileInputStream(afile);
			outStream = new FileOutputStream(bfile);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();

			logger.info("Plik zostal skopiowany!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Czyszczenie aktualnego pliku logów
	 * Wykonywane po przeniesieniu starych danych do archiwum
	 * @param path
	 */
	public void clearFile(String path) {
		try {

			File file = new File(path);

			PrintWriter writer = new PrintWriter(file);
			writer.print("");
			writer.close();

			logger.info("Plik został wyczyszczony");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return ArchiveLogs.class.getSimpleName();
	}

}
