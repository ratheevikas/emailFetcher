package crawler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebScraper {
	private Properties prop = new Properties();
	private  static Logger LOGGER = Util.getLogger();
	private List<InputCSV> inputRecords;
	private List<OutputCSV> outputRecords;
	
	private String inputCSVPath = "input.csv";
	private String outputCSVPath = "output.csv";
	private String geckoPath = "geckodriver.exe";
	

	private List<String> buildSearchResultList(WebDriver driver,
			String searchTerm) {
		LOGGER.info(">>>>>>>>>> 002 ");
		String includeUrls = prop.getProperty("searchIncludeUrl");
		boolean includeAll = false;
		if (includeUrls.trim().replace(",", "").equalsIgnoreCase("all"))
			includeAll = true;

		Set<String> resultSet = new HashSet<String>();
		List<String> resultList = new ArrayList<String>();
		int searchPageLimit = Integer.parseInt(prop
				.getProperty("searchPageLimit"));
		int searchResultLimit = Integer.parseInt(prop
				.getProperty("searchResultLimit"));

		driver.get("http://www.google.com");
		// find google search Box element
		if (prop.getProperty("searchBox") == null
				|| prop.getProperty("searchBox").equalsIgnoreCase("null")) {
			LOGGER.severe("searchBox property in config.properties file is missing");
		}

		WebElement element = driver.findElement(By.xpath(prop
				.getProperty("searchBox")));
		if (element == null) {
			LOGGER.severe("searchBox property in config.properties file is not properly set, may be google has changed it recently");
		}

		element.sendKeys(searchTerm + "\n");
	//	element.submit();
		(new WebDriverWait(driver, 10)).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath(prop.getProperty("resultElement"))));
		
		List<WebElement> elements = new ArrayList<WebElement>();
		if(prop.getProperty("includeAds").equalsIgnoreCase("true"))
		{
			
		elements = driver.findElements(By
				.xpath(prop.getProperty("adsElement")));
		
		
		}
		
		List<WebElement> resultElements = driver.findElements(By
				.xpath(prop.getProperty("resultElement")));
		
		elements.addAll(resultElements);

		for (WebElement webElement : elements) {
			if (searchResultLimit <= resultList.size())
				break;
			// inclusion urls and exclusion urls logic too
			if (resultSet.add(Util.getDomain(webElement.getAttribute("href")))) {
				if ((includeAll || inInludeUrl(Util.getDomain(webElement
						.getAttribute("href"))))
						&& notInExcludeUrl(Util.getDomain(webElement
								.getAttribute("href"))))
					resultList.add(webElement.getAttribute("href"));
			}
		}

		int size = driver.findElements(By.cssSelector("[valign='top'] > td"))
				.size();
		if (searchPageLimit < size)
			searchPageLimit = size;
		// for(int j = 1 ; j < size ; j++) {
		// if (j > 1) {// we don't need to navigate to the first page
		// driver.findElement(By.cssSelector("[aria-label='Page " + j +
		// "']")).click(); // navigate to page number j
		// }
		//

		// moving on to next page

		for (int page = 2; page < searchPageLimit + 1; page++) {
			if (searchResultLimit <= resultList.size())
				break;
			System.out.println("page :: " + page);
			try {
				driver.findElement(
						By.cssSelector("["
								+ prop.getProperty("cssNextPageLocator") + " "
								+ page + "']")).click();
				(new WebDriverWait(driver, 10)).until(ExpectedConditions
						.presenceOfElementLocated(By
								.xpath(prop.getProperty("resultElement"))));
				elements = driver.findElements(By
						.xpath(prop.getProperty("resultElement")));
			} catch (Exception ex) {
				ex.printStackTrace();
				LOGGER.severe("Default Search Pages Finished while clicking Next Page. All relevant Pages has been browsed.");
				Util.printExceptionInLogger(LOGGER,ex);
			}
			for (WebElement webElement : elements) {
				if (resultSet.add(Util.getDomain(webElement
						.getAttribute("href")))) {
					if ((includeAll || inInludeUrl(Util.getDomain(webElement
							.getAttribute("href"))))
							&& notInExcludeUrl(Util.getDomain(webElement
									.getAttribute("href"))))
						resultList.add(webElement.getAttribute("href"));
				}

				if (searchResultLimit <= resultList.size())
					break;

			}
		}

		LOGGER.info("Result Set for SearchTerm :: "+searchTerm+" is >>> " + resultList);
		System.out.println("hrefList >>>>>>>>>>>>>>>>>>>>>> " + resultList);
		// driver.close();

		return resultList;
	}

	private boolean notInExcludeUrl(String url) {
		String excludeUrls = prop.getProperty("searchExcludeUrl");
		
		String[] excludeUrlsArray = excludeUrls.split(",");
		for (String str : excludeUrlsArray) {
			if (url.contains(str))
				return false;
		}
		if (url.contains("google"))
			return false;
		return true;
	}

	private boolean inInludeUrl(String url) {
		String includeUrls = prop.getProperty("searchIncludeUrl");
		String[] includeUrlsArray = includeUrls.split(",");
		for (String str : includeUrlsArray) {
			if (url.contains(str))
				return true;
		}
		return false;
	}

	public Map<String, String> browseWebsiteAndFetchEmails(String url
			) throws IOException{
		Map<String, String> emailUrlMap = new LinkedHashMap<String, String>();
		
		Document doc = Jsoup.connect(url).timeout(60000).validateTLSCertificates(false).get();
		
		emailUrlMap = fetchMails(doc, emailUrlMap,url);
		Elements links = doc.select("a[href]");

		List<String> internalHrefList = new ArrayList<String>();

		Set<String> urlInternalLinks = new HashSet<String>();
		for (Element link : links) {

			String domain = Util.getDomain(url);

			if (link.attr("abs:href").equalsIgnoreCase("null"))
				continue;
			if (link.attr("abs:href").contains("mail"))
				continue;
			if (!link.attr("abs:href").contains(domain))
				continue;

			if (isImportantLink(link)) {

				internalHrefList.add(link.attr("abs:href"));
				urlInternalLinks.add(link.attr("abs:href").trim());

			}
		}

		
		LOGGER.info("internalHrefList >>>>>>>>>>>>>>>>> "
				+ internalHrefList);

		for (String urlInternal : urlInternalLinks) {
			try {
				Document docInternal = Jsoup.connect(urlInternal).timeout(60000).validateTLSCertificates(false).get();
				emailUrlMap = fetchMails(docInternal, emailUrlMap,urlInternal);
			} catch (Exception ex) {
				LOGGER.severe(" Problem while loading link : " + urlInternal);
				Util.printExceptionInLogger(LOGGER,ex);
				ex.printStackTrace();
				continue;
			}

		}
		return emailUrlMap;
	}

	
	public Map<String, String> browseWebsiteAndFetchEmailsFF(String url,WebDriver driver
			) throws IOException{
		
//		List<String> hrefs = new ArrayList<String>();
//		List<WebElement> anchors = driver.findElements(By.tagName("a"));
//		for ( WebElement anchor : anchors ) {
//		    hrefs.add(anchor.getAttribute("href"));
//		}
//		for ( String href : hrefs ) {
//		    driver.get(href);           
//		}
		
		driver.get(url);
		(new WebDriverWait(driver, 10)).until(ExpectedConditions
				.presenceOfElementLocated(By.tagName("body")));
		CharSequence bodyText = driver.findElement(By.tagName("body")).getText();
		Map<String, String> emailUrlMap = new LinkedHashMap<String, String>();
		
		List<WebElement> links = driver.findElements(By.tagName("a"));
		
		emailUrlMap = fetchMailsFF(bodyText, emailUrlMap,url);
	//	Elements links = doc.select("a[href]");

		List<String> internalHrefList = new ArrayList<String>();

		Set<String> urlInternalLinks = new HashSet<String>();
		String domain = Util.getDomain(url);
		for (WebElement link : links) {

			String href = link.getAttribute("href");
			
			if (href == null)
				continue;

			if (href.equalsIgnoreCase("null"))
				continue;
			if (href.contains("mail"))
				continue;
			if (!href.contains(domain))
				continue;

			if (isImportantLinkFF(href)) {

				internalHrefList.add(href);
				urlInternalLinks.add(href.trim());

			}
		}

		
		LOGGER.info("internalHrefList >>>>>>>>>>>>>>>>> "
				+ internalHrefList);

		for (String urlInternal : urlInternalLinks) {
			try {
				driver.get(urlInternal);
				(new WebDriverWait(driver, 10)).until(ExpectedConditions
						.presenceOfElementLocated(By.tagName("body")));
				emailUrlMap = fetchMailsFF(driver.findElement(By.tagName("body")).getText(), emailUrlMap,urlInternal);
			} catch (Exception ex) {
				LOGGER.severe(" Problem while loading link : " + urlInternal);
				Util.printExceptionInLogger(LOGGER,ex);
				ex.printStackTrace();
				continue;
			}

		}
		return emailUrlMap;
	}

	
	
	private Map<String, String> fetchMails(Document doc,
			Map<String, String> emailUrlMap,String url) {

		
		CharSequence text = doc.body().text();
		Matcher m = Pattern.compile(
				"[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
				.matcher(text);
		while (m.find()) {
			String temp = m.group().trim();
			if(temp.endsWith(",") || temp.endsWith(".")|| temp.endsWith(";"))
				temp = temp.substring(0,temp.length() - 1);
			emailUrlMap.putIfAbsent(temp, url);
			LOGGER.info(">>>>>>>>>>Mail>>>>>>>>>>>>" + m.group());
		
		}

		return emailUrlMap;

	}
	
	private Map<String, String> fetchMailsFF(CharSequence text,
			Map<String, String> emailUrlMap,String url) {

		
		Matcher m = Pattern.compile(
				"[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
				.matcher(text);
		while (m.find()) {
			String temp = m.group().trim();
			if(temp.endsWith(",") || temp.endsWith(".")|| temp.endsWith(";"))
				temp = temp.substring(0,temp.length() - 1);
			emailUrlMap.putIfAbsent(temp, url);
			LOGGER.info(">>>>>>>>>>Mail>>>>>>>>>>>>" + m.group());
		
		}

		return emailUrlMap;

	}

	private boolean isImportantLink(Element link) {

		String internalPagesToSearch = prop
				.getProperty("internalPagesToSearch");

		String[] internalIncludeLinks = internalPagesToSearch.split(",");
		for (String str : internalIncludeLinks) {

			if (link.toString().contains(str))
					
				return true;

		}

		return false;
	}
	
	private boolean isImportantLinkFF(String href) {

		String internalPagesToSearch = prop
				.getProperty("internalPagesToSearch");

		String[] internalIncludeLinks = internalPagesToSearch.split(",");
		for (String str : internalIncludeLinks) {

			if (href.toString().contains(str))
					
				return true;

		}

		return false;
	}

	public Map<String, String> searchEmails(WebDriver driver, List<String> resultList
			)  {

		Map<String, String> map = new LinkedHashMap<String, String>();
		for (String url : resultList) {
			try{
			map.putAll(browseWebsiteAndFetchEmailsFF(url,driver));
			}catch(Exception ex)
			{
				ex.printStackTrace();
				LOGGER.severe("Exception while browsing and fetching email from : "+url);
				Util.printExceptionInLogger(LOGGER,ex);
				continue;
			}

		}
		return map;
	}

	

	

	public boolean start() throws InterruptedException, SecurityException,
			IOException, JSONException {
		// Initialize various properties.
		LOGGER.info("Inside start");
		init();
		LOGGER.info("after init");
		LOGGER.info("prop : " + prop);
		LOGGER.info("inputRecords : " + inputRecords);
		
		List<OutputCSV> outPutRec = new ArrayList<OutputCSV>();
		int searchTermPerSession = Integer.parseInt(prop
				.getProperty("searchTermPerSession"));
		int countsearchTerm = 0;
		// Implement logic to fetch only the records for which search need to be
		// done
		
		for (InputCSV csv : inputRecords) {
			LOGGER.info(">>>>>>>>>> 001 ");
			if (csv.getScanned().trim().equalsIgnoreCase("true"))
				continue;
		
			if (countsearchTerm >= searchTermPerSession)
				break;
			countsearchTerm++;

			
			
			Map<String, String> mailUrlMap = new LinkedHashMap<String, String>();
			try {
				
				LocalDateTime start = LocalDateTime.now();
				
				csv.setStartTime(start.toString());
				WebDriver driver = new FirefoxDriver(Util.getFireFoxOptions());
				List<String> resultList = buildSearchResultList(driver,
						csv.getSearchTerm());
			    
			    mailUrlMap.putAll(searchEmails(driver,resultList));

			    driver.close();
				LocalDateTime end = LocalDateTime.now();

				LOGGER.info(">>>>>>>>>>>start Time  >>>>>>>>>>>>>> "+ start);
				LOGGER.info(">>>>>>>>>>>end Time  >>>>>>>>>>>>>> " + end);
				LOGGER.info(">>>>>>>>>>>>>>>> Mails Fetched>>>>>>>>>>>>>  "+ mailUrlMap);
				

				updateOutputRec(outPutRec, csv.getSearchTerm(), mailUrlMap,start.toString());

				csv.setEndTime(LocalDateTime.now().toString());

				csv.setScanned("true");
				csv.setStatus("Success");

			} catch (Exception ex) {
				LOGGER.severe("Error while trying to search the term :: "
						+ csv.getSearchTerm());
				Util.printExceptionInLogger(LOGGER,ex);
				ex.printStackTrace();
				csv.setStatus("Failure");
				csv.setEndTime(LocalDateTime.now().toString());

				csv.setScanned("true");
				
				continue;

			}

		}

		// rewrite to input and output files here.
		
		CSVHandler.writeOutPutCsv(outPutRec, outputCSVPath, outputRecords);
		CSVHandler.writeInPutCsv(inputCSVPath, inputRecords);

		return true;
	}

	
	
	public void updateOutputRec(List<OutputCSV> outPutrec, String searchTerm,
			Map<String, String> map, String timeFetched) {

		for (String key : map.keySet()) {

			OutputCSV rec = new OutputCSV();
			rec.setSearchTerm(searchTerm);
			rec.setEmail(key);
			rec.setUrl(map.get(key));
			rec.setTimeFetched(timeFetched);
			outPutrec.add(rec);
		}
	}
	

	private void init() throws IOException  {
		
		initProperties();
		System.setProperty("webdriver.gecko.driver",
				"geckodriver.exe");
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
	
		LOGGER.info("before reading input.csv");
		inputRecords = CSVHandler.initCSVInputs(inputCSVPath);
		LOGGER.info("afetr reading input.csv ,inputRecords ::  "+inputRecords);
		outputRecords = CSVHandler.initCSVOutPuts(outputCSVPath );
	}


	

	

	private void initProperties() throws IOException {
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);

			input.close();
		} catch (IOException ex) {
			LOGGER.severe("Error while reading config.properties file. Make sure config.properties file exists and have proper format.");
			Util.printExceptionInLogger(LOGGER,ex);
			ex.printStackTrace();
			throw ex;
		}

	}

	

	public static void main(String[] args) throws InterruptedException, IOException {
		
Document doc = Jsoup.connect("http://nautilus.dadeschools.net/Faculty-2017-2018.html").timeout(60000).validateTLSCertificates(false).get();


		
		System.out.println(doc.text());
	//	Elements links = doc.select("a[href]");

//		WebScraper scraper = new WebScraper();
//		
//		System.out.println(" scraper.prop :::: "+scraper.prop);
//		try{
//			scraper.start();
//			String runYoutubeApp = scraper.prop.getProperty("runYoutubeApp");
//			System.out.println(" runYoutubeApp :::: "+runYoutubeApp);
//			if(runYoutubeApp != null && runYoutubeApp.equalsIgnoreCase("yes"))
//			{
//				System.out.println(" Inside runYoutubeApp :::: "+runYoutubeApp);
//			YoutubeViewer.start();
//			}
//
//		} catch (Exception ex) {
//			//printExceptionInLogger(ex);
//			ex.printStackTrace();
//		}
	}

}
