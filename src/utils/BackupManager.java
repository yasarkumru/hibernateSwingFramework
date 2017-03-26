package utils;

import java.io.File;
import java.io.IOException;

import org.hibernate.cfg.Configuration;

public class BackupManager {

	/**
	 * schema'nin backupini verilen dosyaya alir
	 * 
	 * @param backupFile
	 */
	public static void backupMysqlSchema(File backupFile, Configuration configuration) {

		StringBuilder dumpCommand = new StringBuilder();

		String property = System.getProperty("os.name");
		if (property.equals("Linux")) {
			dumpCommand.append("mysqldump -u ");
			dumpCommand.append(configuration
					.getProperty("hibernate.connection.username"));
			dumpCommand.append(" -p").append(
					configuration.getProperty("hibernate.connection.password"));
			dumpCommand.append(" ").append(
					configuration.getProperty("hibernate.default_schema"));

			dumpCommand.append(" -r ");
		} else {
			dumpCommand.append("\"");
			dumpCommand.append(configuration.getProperty("mysqldump_path"));
			dumpCommand.append("mysqldump.exe\" -u");
			dumpCommand.append(configuration
					.getProperty("hibernate.connection.username"));
			dumpCommand.append(" -p").append(
					configuration.getProperty("hibernate.connection.password"));
			dumpCommand.append(" -P").append(configuration.getProperty("mysqldump_port"));
			dumpCommand.append(" ").append(
					configuration.getProperty("hibernate.default_schema"));
			dumpCommand.append(" -r ");

		}

		dumpCommand.append(backupFile.getAbsolutePath());

		Runtime rt = Runtime.getRuntime();
		System.out.println(dumpCommand);
		try {
			Process process = rt.exec(dumpCommand.toString());

			int waitFor = process.waitFor();
			System.out.println("waitFor: " + waitFor);
			int exitValue = process.exitValue();
			System.out.println("exitValue: " + exitValue);
			if (waitFor == 0) {
				System.out.println("backupCreated");
			} else {
				System.out.println("backupCreate problem");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
