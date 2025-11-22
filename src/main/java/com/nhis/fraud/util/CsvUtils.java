package com.nhis.fraud.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {

	public static List<String[]> readAll(Path path) throws IOException {
		try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			List<String[]> rows = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				// naive CSV split (the provided dataset appears simple)
				String[] parts = line.split(",");
				rows.add(parts);
			}
			return rows;
		}
	}

	public static List<String[]> readAll(InputStream inputStream) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			List<String[]> rows = new ArrayList<>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				rows.add(parts);
			}
			return rows;
		}
	}
}


