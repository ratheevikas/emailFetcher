package crawler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;

public class YoutubeViewer {

	public  static Logger LOGGER = Util.getLogger();
	
	public static void start() 
 {
		
		System.setProperty("webdriver.gecko.driver", "geckodriver.exe");
		InputStream input = null;
		Properties proxyPorp = new Properties();
		try {
			input = new FileInputStream("proxy.properties");
			proxyPorp.load(input);

			input.close();
		

		String PROXY = proxyPorp.getProperty("proxy_server_ip") + ":"
				+ proxyPorp.getProperty("proxy_server_port");
		// Bellow given syntaxes will set browser proxy settings using
		// DesiredCapabilities.
		Proxy proxy = new Proxy();
		proxy.setHttpProxy(PROXY).setSslProxy(PROXY).setProxyType(ProxyType.MANUAL);

		FirefoxOptions options = Util.getFireFoxOptions();
		options.setCapability(CapabilityType.PROXY, proxy);
		List<YoutubeCSV> youtubeRec = CSVHandler.initYoutubeCsv();
		WebDriver driver = new FirefoxDriver(options);
		// driver.manage().

		for (YoutubeCSV rec : youtubeRec) {
			driver.get(rec.getUrl().trim());
			Thread.sleep(Integer.parseInt(rec.getTimeToWait().trim()) * 1000);

		}
		driver.close();
		} catch (Exception ex) {

			ex.printStackTrace();
			Util.printExceptionInLogger(LOGGER,ex);
			
		}
	}

}
