package olzone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TripAdvisorHeppler {
	final private static int MAXSCROLLCOUNTER = 15;
	
	private static WebDriver driver = null;
	private static WebDriverWait wait = null;
	private static int scrollCounter = MAXSCROLLCOUNTER;
	
	private static int failureCounter = 0;
	private static int succesCounter = 0;
	
	
	public static void init(WebDriver driver , WebDriverWait wait){
		TripAdvisorHeppler.driver = driver;
		TripAdvisorHeppler.wait = wait;
		failureCounter = 0;
		succesCounter = 0;
	}
	
	public static void skipLogin(){
		waitAndClick(By.id("skip"));
		waitAndClick(By.id("button2"));
	}
	
	public static void back()
	{
		waitAndClick(By.xpath("//ImageView[@id='up']")); // click back button
	}
	
	public static void waitAndClick(By by)
	{
		wait.until(ExpectedConditions.elementToBeClickable(by));
        driver.findElement(by).click();
	}
	
	public static void waitForListToBeLoaded()
	{
		wait.until(ExpectedConditions.elementToBeClickable(By.id("contentWrapper")));
	}
	
	private static HashMap<String, String> getNameToDist(){
		//TODO: what if list is empty
		HashMap<String, String> nameToDist = new HashMap<String, String>();
		
		List<WebElement> content = driver.findElements(By.id("contentWrapper"));
        
        do
        {	
        	
        	WebElement accual = content.get(0);
            WebElement last = content.get(content.size()-1);
            
            nameToDist.put(accual.findElement(By.id("title")).getText(), accual.findElement(By.id("distance")).getText());
        	        	
        	List<WebElement> tContent;
        	
        	scrollCounter = MAXSCROLLCOUNTER;
        	
        	do
         	{
    		 tContent = driver.findElements(By.id("contentWrapper"));
    		 
    		 scrollCounter--;
    		 
    		 new TouchActions(driver).flick(accual, 0, -100, 0).perform();

         	}while(scrollCounter >= 0 && tContent.get(tContent.size()-1).findElement(By.id("title")).getText().equals(last.findElement(By.id("title")).getText()));
        	 
        	if (scrollCounter >= 0)
        	{
		    	content.add(tContent.get(tContent.size()-1));
        	}
        	content.remove(0);
        	
        }while(scrollCounter >= 0 && !content.isEmpty());
        
        while(!content.isEmpty())
        {
	        WebElement accual = content.get(0);
	        nameToDist.put(accual.findElement(By.id("title")).getText(), accual.findElement(By.id("distance")).getText());
	        content.remove(0);
        }
        
        return nameToDist;
		
	}
  
	private static String[] getFilters(){
		Set<String> Categories = new LinkedHashSet<String>();	
		
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("action_done")));
        
        for(WebElement ele : driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")))
        	Categories.add(ele.findElement(By.id("text")).getText());
        
        new TouchActions(driver).flick(driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")).get(0), 0, -3000, 0).perform();
        
        for(WebElement ele : driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")))
        	Categories.add(ele.findElement(By.id("text")).getText());
        
        Categories.remove("All");
        
		return Categories.toArray((new String[Categories.size()]));
	}
	
	private static void compareDict(Map<String, String> dictA, Map<String, String> dictB){
		HashSet<String> names = new HashSet<String>(dictA.keySet());
    	names.addAll(dictB.keySet());
    	
    	for(String name: names){
    		if(!dictA.containsKey(name))
        	{
        		System.err.println("No entry in 'collect' step:" + name);
        		failureCounter++;
        		continue;
        	}
    		
    		if(!dictB.containsKey(name))
        	{
    			System.err.println("No entry in 'check' step:" + name);
    			failureCounter++;
        		continue;
        	}
    		
    		if(!dictB.get(name).equals(dictA.get(name)))
        	{
        		System.err.println("mismatch at distance to: " + name + ", " +  
        				" Expected: " + dictA.get(name) +
        				" Found: " + dictB.get(name));
        		failureCounter++;
        	}
    		
    		succesCounter++;
    	}
	}
	
	public static Map<String, String> collectDataRestaurants() throws InterruptedException{
		waitAndClick(By.partialLinkText("Restaurants"));
		waitForListToBeLoaded();
		waitAndClick(By.id("sortText"));
		waitAndClick(By.partialLinkText("Distance"));
		waitForListToBeLoaded();
        
		Map<String, String> nameToDist = getNameToDist();
		
        System.out.println("Restaurants: " + nameToDist.size());

        back();
        
        return nameToDist;
    }
	
	public static Map<String, HashMap<String, String>> collectDataHotels() throws InterruptedException{
			waitAndClick(By.partialLinkText("Hotels"));
	        
	        Map<String, HashMap<String, String>> typeToNameToDist = new HashMap<String, HashMap<String, String>>();
	        
	        for(String idRadio : new String[]{"hotelsRadio", "bbRadio", "othersRadio"})
	        {
	        	waitAndClick(By.id(idRadio));
	        	waitAndClick(By.id("searchButton"));
	        	waitForListToBeLoaded();
	        	waitAndClick(By.id("sortText"));
	        	waitAndClick(By.partialLinkText("Distance"));
	        	waitForListToBeLoaded();
		   
	        	typeToNameToDist.put(idRadio, getNameToDist());
	        	
	        	back();
	        }
	        
	        back();
	        
	        return typeToNameToDist;
	    }
	
	public static Map<String, HashMap<String, String>> collectDataThingToDo() throws InterruptedException{
		   waitAndClick(By.partialLinkText("Things to Do")); //Select Things to Do Category
	        
	        Map<String, HashMap<String, String>> filterToNameToDist = new HashMap<String, HashMap<String, String>>();
	        
	        waitForListToBeLoaded();
	        waitAndClick(By.id("filterText"));
	        
	        waitAndClick(By.partialLinkText("Select all"));
	    	
	        String[] filters = getFilters();
	           
	        new TouchActions(driver).flick(driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")).get(0), 0, 3000, 0).perform();
	        
	        for(int i=0;i<filters.length;i++)
	        {
	        	
	        	if(i!=0)
	        	{
	        		waitForListToBeLoaded();
	    	        waitAndClick(By.id("filterText"));
	    	        waitAndClick(By.partialLinkText(filters[i-1]));
	                
		        	while(driver.findElements(By.partialLinkText(filters[i])).size() <= 0)
	        		{
		        		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")));
		        		WebElement tmp = driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")).get(0);
	        			new TouchActions(driver).flick(tmp, 0, (int)-1.3*tmp.getSize().getHeight(), 0).perform();
	        		}
	        	}
	        	
	        	waitAndClick(By.partialLinkText(filters[i]));
	        	waitAndClick(By.id("action_done"));
	        	waitAndClick(By.id("searchButton"));
	        	waitForListToBeLoaded();
	        	waitAndClick(By.id("sortText"));
	        	waitAndClick(By.partialLinkText("Distance"));
	        	waitForListToBeLoaded();
	        	
	        	filterToNameToDist.put(filters[i], getNameToDist()); 
	        }
	        
	        back();
	    	
	        return filterToNameToDist;
	    }

	public static void checkDataRestaurants(Map<String, String> data) throws InterruptedException
    {
    	compareDict(data, collectDataRestaurants());
    }

	
	public static void checkDataHotels(Map<String, HashMap<String, String>> data) throws InterruptedException
    {
		Map<String, HashMap<String, String>> typeToNameToDist = collectDataHotels();
        
        for(String idRadio : new String[]{"hotelsRadio", "bbRadio", "othersRadio"})
        {
        	compareDict(data.get(idRadio), typeToNameToDist.get(idRadio));
        }
    }
	
	public static void checkDataThingToDo(Map<String, HashMap<String, String>> data) throws InterruptedException{
    	
		Map<String, HashMap<String, String>> filterToNameToDist = collectDataThingToDo();
		
		String[] filters = filterToNameToDist.keySet().toArray((new String[filterToNameToDist.size()]));

        for(String filter : filters)
        {
        	compareDict(data.get(filter), filterToNameToDist.get(filter));
        }
    }
	
	public static int getFailureCounter() {
		return failureCounter;
	}

	public static int getSuccesCounter() {
		return succesCounter;
	}

}

