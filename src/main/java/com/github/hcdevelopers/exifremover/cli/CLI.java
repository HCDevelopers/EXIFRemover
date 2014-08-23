package exifremover.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;

import exifremover.model.EXIFRemover;

/**
 * Command line interface for exifremover.
 * 
 * @author deque
 * 
 */
public class CLI {

	private static final String TEMP_DIR = "temp";
	private static final String VERSION = "EXIFRemover 0.1";
	private static final String USAGE = "exifremover -d <filename/folder>"
			+ " [-dest <filename/folder>]";
	private static final String NEWLINE = System.getProperty("line.separator");
	private static final String EXIF_TITLE = "" + " _____  _____ ___ ___"
			+ NEWLINE + "| __\\ \\/ /_ _| __| _ \\___ _ __  _____ _____ _ _"
			+ NEWLINE + "| _| >  < | || _||   / -_) '  \\/ _ \\ V / -_) '_|"
			+ NEWLINE + "|___/_/\\_\\___|_| |_|_\\___|_|_|_\\___/\\_/\\___|_|        by Deque at"
			+ NEWLINE;
	private static final String HC_TITLE = ""
			+ "_  _ ____ ____ _  _ ____ ____ _  _ _  _ _  _ _  _ _ ___ _   _  ____ ____ _  _"
			+ NEWLINE
			+ "|__| |__| |    |_/  |    |  | |\\/| |\\/| |  | |\\ | |  |   \\_/   |    |  | |\\/|"
			+ NEWLINE
			+ "|  | |  | |___ | \\_ |___ |__| |  | |  | |__| | \\| |  |    |   .|___ |__| |  |"
			+ NEWLINE;

	private static final String FILE_SEPARATOR = System
			.getProperty("file.separator");

	public static void main(String[] args) {
		new CLI(args);
	}

	/**
	 * Initializes options, invokes parsing of command line arguments.
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public CLI(String[] args) {
		Options options = initOptions();
		parseCommandLine(args, options);
	}

	/**
	 * Parses arguments for available options.
	 * 
	 * @param args
	 *            the command line arguments
	 * @param options
	 *            the command line options for which the arguments are parsed
	 */
	private void parseCommandLine(String[] args, Options options) {
		CommandLineParser parser = new PosixParser();
		System.out.println(EXIF_TITLE);
		System.out.println(HC_TITLE);
		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("version")) {
				System.out.println(VERSION);
			}
			if (line.hasOption("help") || args.length == 0) {
				showHelp(options);
			}
			if (line.hasOption("show-exif")) {
				showExif(line);
			}
			if (line.hasOption("delete-exif")) {
				deleteEXIF(line);
			}
		} catch (ParseException e) {
			System.err.println(e.getMessage());
		} catch (ImageWriteException e) {
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (ImageReadException e) {
			System.err.println(e.getMessage());
		}
	}

	/**
	 * Prints out all EXIF data found in given file.
	 * 
	 * @param line
	 *            command line that contains the image path
	 * @throws IOException
	 * @throws ImageReadException
	 */
	private void showExif(CommandLine line) throws IOException,
			ImageReadException {
		File file = new File(line.getOptionValue("show-exif"));
		EXIFRemover remover = new EXIFRemover(file);
		System.out.println(remover.getExifData());
	}

	/**
	 * Prints out help information like usage and available options.
	 * 
	 * @param options
	 *            command line options to be shown
	 */
	private void showHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(USAGE, options);
	}

	/**
	 * Removes EXIF data from a single image file or all image files in a folder
	 * given as command line argument.
	 * 
	 * If a folder is given, it saves the resulting images into destination
	 * folder. A TEMP_DIR is created if no destination is given (this is mainly
	 * for security reasons, so you are not able to overwrite a whole folder of
	 * images by mistake).
	 * 
	 * If a single image file is given, it saves the resulting image into a
	 * given destination. If there is no destination, the image file is
	 * overwritten, which means EXIF data of the original file is lost unless
	 * you have a copy.
	 * 
	 * @param line
	 *            command line that contains the paths for source and maybe
	 *            destination
	 * @throws IOException
	 * @throws ImageReadException
	 * @throws ImageWriteException
	 */
	private void deleteEXIF(CommandLine line) throws IOException,
			ImageReadException, ImageWriteException {
		File source = new File(line.getOptionValue("delete-exif"));
		// if no destination folder given, assign source folder to dest
		File dest = line.hasOption("dest") ? new File(
				line.getOptionValue("dest")) : source;

		if (source.isDirectory()) {
			deleteEXIFFromDir(source, dest);
		} else {
			deleteEXIFForSingleFile(source, dest);
		}
	}

	/**
	 * Removes EXIF data from all files in a given source directory and saves
	 * new images into the destination directory. Destination directory is
	 * created if it doesn't exist.
	 * 
	 * @param source
	 *            source folder that contains the image files
	 * @param dest
	 *            destination folder where the new images are saved to
	 */
	private void deleteEXIFFromDir(File source, File dest) {
		assert source.isDirectory();
		if (!dest.isDirectory()) {
			if (dest.exists()) {
				System.err.println("destination file is no directory");
				return;
			} else {
				dest.mkdir();
			}

		}
		// create temp folder if destination folder equals source folder
		if (dest.equals(source)) {
			removeAllEXIFFromFolder(source, new File(dest.getAbsolutePath()
					+ FILE_SEPARATOR + TEMP_DIR));
		} else {
			removeAllEXIFFromFolder(source, dest);
		}
	}

	/**
	 * Removes EXIF data from a single image file which is the source file.
	 * Resulting images is saved into the destination file which may be the same
	 * as the source file (in that case the image is overwritten).
	 * 
	 * @param source
	 *            source folder that contains the image files
	 * @param dest
	 *            destination folder where the new images are saved to
	 * @throws IOException
	 * @throws ImageReadException
	 * @throws ImageWriteException
	 */
	private void deleteEXIFForSingleFile(File source, File dest)
			throws IOException, ImageReadException, ImageWriteException {
		EXIFRemover remover = new EXIFRemover(source);
		remover.removeEXIFData(dest);
		System.out.println("All EXIF data successfully removed from "
				+ source.getAbsolutePath());
		System.out.println("Result saved in " + dest.getAbsolutePath());
	}

	/**
	 * Deletes EXIF information from every file in a given source folder.
	 * Resulting images are saved the destination folder. The given arguments
	 * files must be directories.
	 * 
	 * Directories within the source folder are ignored. Everything else will be
	 * treated as an image.
	 * 
	 * @param srcFolder
	 *            source folder that contains the image files
	 * @param destFolder
	 *            destination folder where the new images are saved to
	 */
	private void removeAllEXIFFromFolder(File srcFolder, File destFolder) {
		assert srcFolder.isDirectory() && destFolder.isDirectory();
		File[] files = srcFolder.listFiles();

		if (!destFolder.exists()) {
			destFolder.mkdir();
		}
		for (File file : files) {
			if (file.isFile()) { // make sure that file is no directory

				try {
					EXIFRemover remover = new EXIFRemover(file);
					File destFile = new File(destFolder.getAbsolutePath()
							+ FILE_SEPARATOR + file.getName());
					remover.removeEXIFData(destFile);
					System.out.println("created file "
							+ destFile.getAbsolutePath());
				} catch (IOException e) {
					System.err.println("Can not convert or read file "
							+ file.getAbsolutePath());
				} catch (ImageReadException e) {
					System.err.println("Unable to remove EXIF data from "
							+ file.getAbsolutePath());
				} catch (ImageWriteException e) {
					System.err.println("Unable to remove EXIF data from "
							+ file.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Initializes command line options like d, s, dest, help.
	 * 
	 * @return initialized options
	 */
	private Options initOptions() {
		Options options = new Options();

		options.addOption("v", "version", false, "show version");
		options.addOption("h", "help", false, "print help message");

		options.addOption("s", "show-exif", true, "show exif data");
		options.getOption("s").setArgName("filename");

		options.addOption("d", "delete-exif", true,
				"delete all exif data of given file(s)");
		options.getOption("d").setArgName("filename");

		options.addOption("dest", true, "save changed image(s) into file");
		options.getOption("dest").setArgName("filename");

		return options;
	}

}
