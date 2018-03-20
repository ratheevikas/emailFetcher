package crawler;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

public class CSVHandler {
	public  static Logger LOGGER = Util.getLogger();

	public static void writeOutPutCsv(List<OutputCSV> outPutRec,
			String outputCSVPath, List<OutputCSV> outputRecords)
			throws IOException {

		

		try {

			Writer writer = Files.newBufferedWriter(Paths.get(outputCSVPath));
			outputRecords.addAll(outPutRec);
			CSVWriter csvWriter = new CSVWriter(writer,
					CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END);

			// searchTerm,scanned,status,startTime,endTime

			String[] headerRecord = { "searchTerm", "url", "email",
					"timeFetched" };
			csvWriter.writeNext(headerRecord);
			for (OutputCSV rec : outputRecords) {
				csvWriter.writeNext(new String[] { rec.getSearchTerm(),
						rec.getUrl(), rec.getEmail(), rec.getTimeFetched() });
			}

			csvWriter.close();
			writer.close();

		} catch (IOException ex) {
			LOGGER.severe("ERROR while writing the output.csv file.");
			ex.printStackTrace();
			throw ex;
		}
	}

	public static void writeInPutCsv(String inputCSVPath,
			List<InputCSV> inputRecords) {
	
		try {

			Writer writer = Files.newBufferedWriter(Paths.get(inputCSVPath));

			CSVWriter csvWriter = new CSVWriter(writer,
					CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_ESCAPE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END);

			String[] headerRecord = { "searchTerm", "scanned", "status",
					"startTime", "endTime" };
			csvWriter.writeNext(headerRecord);
			for (InputCSV rec : inputRecords) {
				csvWriter.writeNext(new String[] { rec.getSearchTerm(),
						rec.getScanned(), rec.getStatus(), rec.getStartTime(),
						rec.getEndTime() });
			}

			csvWriter.close();

		} catch (Exception ex) {
			LOGGER.severe("ERROR while writing the input.csv file.");
			ex.printStackTrace();
		}
	}

	public static List<InputCSV> initCSVInputs(String inputCSVPath)
			throws IOException {

		try {
			LOGGER.info("Inside initCSVInputs");
			List<InputCSV> inputRecords = new ArrayList<InputCSV>();
			Reader reader = Files.newBufferedReader(Paths.get(inputCSVPath));

			ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
			strategy.setType(InputCSV.class);
			String[] memberFieldsToBindTo = { "searchTerm", "scanned",
					"status", "startTime", "endTime" };
			strategy.setColumnMapping(memberFieldsToBindTo);

			CsvToBean csvToBean = new CsvToBeanBuilder(reader)
					.withMappingStrategy(strategy).withSkipLines(1)
					.withIgnoreLeadingWhiteSpace(true).build();

			inputRecords = csvToBean.parse();
			LOGGER.info("Inside initCSVInputs, after reading inputRecords ::: "
					+ inputRecords);
			return inputRecords;
		} catch (IOException ex) {
			LOGGER.severe(" Error while reading Input.csv file. Make sure File exists and have correct format");
			ex.printStackTrace();
			throw ex;
		}

	}

	public static List<YoutubeCSV> initYoutubeCsv() throws IOException {

		try {

			List<YoutubeCSV> recs = new ArrayList<YoutubeCSV>();
			Reader reader = Files.newBufferedReader(Paths.get("youtube.csv"));

			ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
			strategy.setType(YoutubeCSV.class);
			String[] memberFieldsToBindTo = { "url", "timeToWait" };
			strategy.setColumnMapping(memberFieldsToBindTo);

			CsvToBean csvToBean = new CsvToBeanBuilder(reader)
					.withMappingStrategy(strategy).withSkipLines(1)
					.withIgnoreLeadingWhiteSpace(true).build();

			recs = csvToBean.parse();
			LOGGER.info("Inside initCSVInputs, after reading inputRecords ::: "
					+ recs);
			return recs;
		} catch (IOException ex) {
			LOGGER.severe(" Error while reading youtube.csv file. Make sure File exists and have correct format");
			Util.printExceptionInLogger(LOGGER, ex);
			ex.printStackTrace();
			throw ex;
		}

	}

	public static List<OutputCSV> initCSVOutPuts(String outputCSVPath) {
		List<OutputCSV> outputRecords = new ArrayList<OutputCSV>();
		try {

			Reader reader = Files.newBufferedReader(Paths.get(outputCSVPath));

			ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
			strategy.setType(OutputCSV.class);
			String[] memberFieldsToBindTo = { "searchTerm", "url", "email",
					"timeFetched" };
			strategy.setColumnMapping(memberFieldsToBindTo);

			CsvToBean csvToBean = new CsvToBeanBuilder(reader)
					.withMappingStrategy(strategy).withSkipLines(1)
					.withIgnoreLeadingWhiteSpace(true).build();

			outputRecords = csvToBean.parse();
			return outputRecords;

		} catch (Exception ex) {
			LOGGER.info("No out put file. Looks like programming running first time");
			Util.printExceptionInLogger(LOGGER, ex);
			return outputRecords;
		}

	}

}
