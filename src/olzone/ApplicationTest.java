package olzone;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.selendroid.client.SelendroidDriver;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.SelendroidLauncher;
import io.selendroid.standalone.log.LogLevelEnum;


public class ApplicationTest {
	
	private static SelendroidLauncher selendroidServer = null;
	private static WebDriver driver = null;
	private static WebDriverWait wait = null;
	
	private LineIterator lineIterator;
	private int lineCounter;
	
	private WebsocketClientEndpoint clientEndPoint = null;
	private int cordsNumber;

//	private Map<String, String> dataRestaurants = null;
//	private Map<String, HashMap<String, String>> dataThingToDo = null;
//	private Map<String, HashMap<String, String>> dataHotels = null;
	
	private ArrayList<Integer> pointWhereTestHotels = new ArrayList<Integer>();
	private ArrayList<Integer> pointWhereTestRestaurants = new ArrayList<Integer>();
	private ArrayList<Integer> pointWhereTestTestThingToDo = new ArrayList<Integer>();
	
	private int pointWhereTestHotelsCounter;
	private int pointWhereTestRestaurantsCounter;
	private int pointWhereTestTestThingToDoCounter;
	
	private ArrayList<Map<String, String>> dataRestaurants = new ArrayList<Map<String, String>>();
	private ArrayList<Map<String, HashMap<String, String>>> dataThingToDo = new ArrayList<Map<String, HashMap<String, String>>>();
	private ArrayList<Map<String, HashMap<String, String>>> dataHotels = new ArrayList<Map<String, HashMap<String, String>>>();
	
	@Test
	public void testYosemiteVillage() throws Exception
	{
		nearMeTest("src/resources/YosemiteVillage.Trip", 0, 2, 0);
	}
	
//	@Test
//	public void testCarmel() throws Exception
//	{
//		nearMeTest("src/resources/Carmel.Trip", 1, 1, 1);
//	}
//	
//	@Test
//	public void testSanJose() throws Exception
//	{
//		nearMeTest("src/resources/SanJose.Trip", 1, 1, 1);
//	}
//	
//	@Test
//	public void testSanFrancisco() throws Exception
//	{
//		nearMeTest("src/resources/SanFrancisco.Trip", 1, 1, 1);
//	}

    public void nearMeTest(String fileName, int numberOfPointWhereTestHotels, int numberOfPointWhereTestRestaurants, int numberOfPointWhereTestThingToDo) throws Exception
    {
    	cordsNumber = getFileLinesNumber(fileName);    	
    	clientEndPoint = new WebsocketClientEndpoint("36969");
    	
    	TripAdvisorHeppler.init(driver, wait);
    	TripAdvisorHeppler.skipLogin();
    	TripAdvisorHeppler.waitAndClick(By.partialLinkText("Near Me Now"));
        
    	
    	selectPointForChecking(numberOfPointWhereTestHotels, numberOfPointWhereTestRestaurants, numberOfPointWhereTestThingToDo);
    	
    	pointWhereTestHotelsCounter = 0;
    	pointWhereTestRestaurantsCounter = 0; 
    	pointWhereTestTestThingToDoCounter = 0;
    	collectData(fileName);
    	
    	pointWhereTestHotelsCounter = 0;
    	pointWhereTestRestaurantsCounter = 0; 
    	pointWhereTestTestThingToDoCounter = 0;
    	checkData(fileName);
    	
            
        System.out.println("Succes: " + TripAdvisorHeppler.getSuccesCounter());
        System.out.println("Failures: " + TripAdvisorHeppler.getFailureCounter());
    }
    
    
    @BeforeClass
    public static void startSelendroidServer() throws Exception {
      if (selendroidServer != null) {
        selendroidServer.stopSelendroid();
      }
      
      SelendroidConfiguration config = new SelendroidConfiguration();
      config.setLogLevel(LogLevelEnum.ERROR);
      config.addSupportedApp("F:/_Praktyki/real/com.tripadvisor.tripadvisor.apk");
      
      selendroidServer = new SelendroidLauncher(config);
      selendroidServer.launchSelendroid();

      SelendroidCapabilities caps = new SelendroidCapabilities("com.tripadvisor.tripadvisor:11.0");
      caps.setEmulator(false);

      driver = new SelendroidDriver(caps);
      
      wait = new WebDriverWait(driver,10);
    }
    
    @AfterClass
    public static void stopSelendroidServer() {
      if (driver != null) {
        driver.quit();
      }
      if (selendroidServer != null) {
        selendroidServer.stopSelendroid();
      }
    }
    
    private void selectPointForChecking(int numberOfPointWhereTestHotels, int numberOfPointWhereTestRestaurants, int numberOfPointWhereTestThingToDo){
    	for(int i=0;i<numberOfPointWhereTestHotels; i++)
    		pointWhereTestHotels.add(new Random().nextInt(cordsNumber+1));
    	
    	for(int i=0;i<numberOfPointWhereTestRestaurants; i++)
    		pointWhereTestRestaurants.add(new Random().nextInt(cordsNumber+1));
    	
    	for(int i=0;i<numberOfPointWhereTestThingToDo; i++)
    		pointWhereTestTestThingToDo.add(new Random().nextInt(cordsNumber+1));
    }
   
    private void collectData(String fileName) throws IOException, InterruptedException{
        lineIterator = FileUtils.lineIterator(new File(fileName), "UTF-8");
      	lineCounter = 0;
      	
    	 while (lineIterator.hasNext()) {
             clientEndPoint.sendMessage(lineIterator.nextLine());
             Thread.sleep(200);		//to not overload socket
         	
         	if(pointWhereTestRestaurants.contains(lineCounter)){
         		Thread.sleep(5000); //to make sure position have changed
         		dataRestaurants.add(TripAdvisorHeppler.collectDataRestaurants());
         		pointWhereTestRestaurantsCounter++;
         	}
             	
         	if(pointWhereTestHotels.contains(lineCounter)){
         		Thread.sleep(5000); //to make sure position have changed
         		dataHotels.add(TripAdvisorHeppler.collectDataHotels());
         		pointWhereTestHotelsCounter++;
         	}
         	
         	if(pointWhereTestTestThingToDo.contains(lineCounter)){
         		Thread.sleep(5000); //to make sure position have changed
         		dataThingToDo.add(TripAdvisorHeppler.collectDataThingToDo());
         		pointWhereTestTestThingToDoCounter++;
         	}
         	lineCounter++;
         }
    	 
    	 lineIterator.close();
    }
    
    private void checkData(String fileName) throws IOException, InterruptedException{
    	lineIterator = FileUtils.lineIterator(new File(fileName), "UTF-8");
      	lineCounter = 0;
      	
      	while (lineIterator.hasNext()) {
            clientEndPoint.sendMessage(lineIterator.nextLine());
            Thread.sleep(200);		//to not overload socket
        	
        	if(pointWhereTestRestaurants.contains(lineCounter)){
        		Thread.sleep(5000); //to make sure position have changed
        		TripAdvisorHeppler.checkDataRestaurants(dataRestaurants.get(pointWhereTestRestaurantsCounter++));
        	}
            	
        	if(pointWhereTestHotels.contains(lineCounter)){
        		Thread.sleep(5000); //to make sure position have changed
        		TripAdvisorHeppler.checkDataHotels(dataHotels.get(pointWhereTestHotelsCounter++));
        	}
        	
        	if(pointWhereTestTestThingToDo.contains(lineCounter)){
        		Thread.sleep(5000); //to make sure position have changed
        		TripAdvisorHeppler.checkDataThingToDo(dataThingToDo.get(pointWhereTestTestThingToDoCounter++));
        	}
        	lineCounter++;
        }
      	
      	lineIterator.close();
    }
    
	private int getFileLinesNumber(String fileName) throws IOException{
		LineNumberReader lnr = new LineNumberReader(new FileReader(new File(fileName)));
    	lnr.skip(Long.MAX_VALUE);
    	int linesNumber = lnr.getLineNumber();
    	lnr.close();
    	
    	return linesNumber;
	}
   
}