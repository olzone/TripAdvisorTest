package olzone;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.touch.TouchActions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.selendroid.client.SelendroidDriver;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.SelendroidLauncher;
import io.selendroid.standalone.log.LogLevelEnum;


public class ApplicationTest {
	
	private static SelendroidLauncher selendroidServer = null;
	private static WebDriver driver = null;
	private WebDriverWait wait = null;
	
	private int succesCunter = 0;
	private int failureCunter = 0;
	
	
    @Test
    public void testLocTest() throws Exception
    {
    	Process p = Runtime.getRuntime().exec("adb forward tcp:36969 tcp:36969");
    	
    	wait = new WebDriverWait(driver,10);
    	wait.until(ExpectedConditions.elementToBeClickable(By.id("skip")));
        driver.findElement(By.id("skip")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("button2")));
        driver.findElement(By.id("button2")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Near Me Now")));
        driver.findElement(By.partialLinkText("Near Me Now")).click();
    	
        final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:36969/"));
        
        Timer time = new Timer(); //keepAlive
        time.schedule(new TimerTask() {
            @Override
            public void run() {
            	try {
					Process p = Runtime.getRuntime().exec("adb forward tcp:36969 tcp:36969");
					Thread.sleep(2000);
					clientEndPoint.sendMessage("");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }, 0, 3000);
        
        final int cordsNumber = 352;
        
//        final String FileName = "src/resources/YosemiteVillage.Trip";
//        final String FileName = "src/resources/Carmel.Trip";
        final String FileName = "src/resources/SanJose.Trip";
//        final String FileName = "src/resources/SanFrancisco.Trip";
        
        LineIterator it = FileUtils.lineIterator(new File(FileName), "UTF-8");
        
        
        
        Map<String, String> dataRestaurants = null;
        Map<String, HashMap<String, String>> dataThingToDo = null;
        Map<String, HashMap<String, String>> dataHotels = null;
        
        try {
        	int counter = 0;
            while (it.hasNext()) {
            	String line = it.nextLine();
                clientEndPoint.sendMessage(line);
                Thread.sleep(200);
            	
            	if(counter == (int)(cordsNumber/3)){
            		Thread.sleep(5000);
            		dataRestaurants = collectDataRestaurants();
            	}
            	
            	if(counter == (int)(2*cordsNumber/3)){
            		Thread.sleep(5000);
            		dataHotels = collectDataHotels();
            	}
            	
            	if(counter == cordsNumber){
            		Thread.sleep(5000);
            		dataThingToDo = collectDataThingToDo();
            	}
            	counter++;
            }
            it = FileUtils.lineIterator(new File(FileName), "UTF-8");
            counter = 0;
            while (it.hasNext()) {
            	String line = it.nextLine();
                clientEndPoint.sendMessage(line);
                Thread.sleep(200);
            	
            	if(counter == (int)(cordsNumber/3)){
            		Thread.sleep(5000);
            		checkDataRestaurants(dataRestaurants);
            	}
            	
            	if(counter == (int)(2*cordsNumber/3)){
            		Thread.sleep(5000);
            		checkDataHotels(dataHotels);
            	}
            	
            	if(counter == cordsNumber){
            		Thread.sleep(5000);
            		checkDataThingToDo(dataThingToDo);
            	}
            	counter++;
            }
                
        }
    	finally {
            System.out.println("Succes: " + succesCunter);
            System.out.println("Failures: " + failureCunter);
    		it.close();
        	p.destroy();
    	} 
    }
    
 

  
	Map<String, String> collectDataRestaurants() throws InterruptedException{
    	wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Restaurants")));
        driver.findElement(By.partialLinkText("Restaurants")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
        driver.findElement(By.id("sortText")).click();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Distance")));
        driver.findElement(By.partialLinkText("Distance")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
        
        List<WebElement> content = driver.findElements(By.id("contentWrapper"));
        
        Map<String, String> NameToDist = new HashMap<String, String>();
          
        int counter = 15;
        do
        {	
        	
        	WebElement accual = content.get(0);
            WebElement last = content.get(content.size()-1);
            
        	NameToDist.put(accual.findElement(By.id("title")).getText(), accual.findElement(By.id("distance")).getText());
        	        	
        	List<WebElement> tContent;
        	counter = 15;
        	
        	do
         	{
    		 tContent = driver.findElements(By.id("contentWrapper"));
    		 
    		 counter--;
    		 
    		 new TouchActions(driver).flick(accual, 0, -100, 0).perform();

         	}while(counter >= 0 && tContent.get(tContent.size()-1).findElement(By.id("title")).getText().equals(last.findElement(By.id("title")).getText()));
        	 
        	if (counter >= 0)
        	{
		    	content.add(tContent.get(tContent.size()-1));
        	}
        	content.remove(0);
        	
        }while(counter >= 0 && !content.isEmpty());
        
        while(!content.isEmpty())
        {
	        WebElement accual = content.get(0);
	        NameToDist.put(accual.findElement(By.id("title")).getText(), accual.findElement(By.id("distance")).getText());
	        content.remove(0);
        }
        System.out.println("Restaurants: " + NameToDist.size());
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ImageView[@id='up']")));
    	driver.findElement(By.xpath("//ImageView[@id='up']")).click();
    	
        return NameToDist;
    }
    
    Map<String, HashMap<String, String>> collectDataThingToDo() throws InterruptedException{
    	wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Things to Do")));
        driver.findElement(By.partialLinkText("Things to Do")).click();
        
        Map<String, HashMap<String, String>> CategoryToNameToDist = new HashMap<String, HashMap<String, String>>();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
        driver.findElement(By.id("filterText")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("Select all")));
    	driver.findElement(By.partialLinkText("Select all")).click();
    	
    	Set<String> Categories = new LinkedHashSet<String>();	
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("action_done")));
        
        for(WebElement ele : driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")))
        	Categories.add(ele.findElement(By.id("text")).getText());
        
        new TouchActions(driver).flick(driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")).get(0), 0, -3000, 0).perform();
        
        for(WebElement ele : driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")))
        	Categories.add(ele.findElement(By.id("text")).getText());
        
        Categories.remove("All");
        

        
        Object[] CategoriesStrings = Categories.toArray();
        
        new TouchActions(driver).flick(driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")).get(0), 0, 3000, 0).perform();
        
        for(int i=0;i<Categories.size();i++)
        {
        	
        	if(i!=0)
        	{
        		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
                driver.findElement(By.id("filterText")).click();
                
	        	wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(CategoriesStrings[i-1].toString())));
	        	driver.findElement(By.partialLinkText(CategoriesStrings[i-1].toString())).click();
	        
	        	while(driver.findElements(By.partialLinkText(CategoriesStrings[i].toString())).size() <= 0)
        		{
	        		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")));
	        		WebElement tmp = driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")).get(0);
        			new TouchActions(driver).flick(tmp, 0, (int)-1.3*tmp.getSize().getHeight(), 0).perform();
        		}
        	}
        	
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(CategoriesStrings[i].toString())));
	        driver.findElement(By.partialLinkText(CategoriesStrings[i].toString())).click();

	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("action_done")));
	        driver.findElement(By.id("action_done")).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchButton")));
	        driver.findElement(By.id("searchButton")).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
	        driver.findElement(By.id("sortText")).click();
	        
	        
	        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Distance")));
	        driver.findElement(By.partialLinkText("Distance")).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
	        
	        List<WebElement> content = driver.findElements(By.id("contentWrapper"));
	        
	        HashMap<String, String> NameToDist = new HashMap<String, String>();
	          
	        int counter = 15;
	        do
	        {	
	        	
	        	WebElement accual = content.get(0);
	            WebElement last = content.get(content.size()-1);
	            
	        	NameToDist.put(accual.findElement(By.id("title")).getText(), accual.findElement(By.id("distance")).getText());
	        	        	
	        	List<WebElement> tContent;
	        	counter = 15;
	        	
	        	do
	         	{
	    		 tContent = driver.findElements(By.id("contentWrapper"));
	    		 
	    		 counter--;
	    		 
	    		 new TouchActions(driver).flick(accual, 0, -100, 0).perform();
	
	         	}while(counter >= 0 && tContent.get(tContent.size()-1).findElement(By.id("title")).getText().equals(last.findElement(By.id("title")).getText()));
	        	 
	        	if (counter >= 0)
	        	{
			    	content.add(tContent.get(tContent.size()-1));
	        	}
	        	content.remove(0);
	        	
	        }while(counter >= 0 && !content.isEmpty());
	        
	        while(!content.isEmpty())
	        {
		        WebElement accual = content.get(0);
		        NameToDist.put(accual.findElement(By.id("title")).getText(), accual.findElement(By.id("distance")).getText());
		        content.remove(0);
	        }
	        
	        System.out.println(CategoriesStrings[i].toString() + ": " + NameToDist.size());
	        CategoryToNameToDist.put(CategoriesStrings[i].toString(), NameToDist);
	        
	        
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ImageView[@id='up']")));
    	driver.findElement(By.xpath("//ImageView[@id='up']")).click();
    	
        return CategoryToNameToDist;
    }
    
    Map<String, HashMap<String, String>> collectDataHotels() throws InterruptedException{
    	System.out.println("Hotels");
    	wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Hotels")));
        driver.findElement(By.partialLinkText("Hotels")).click();
        
        Map<String, HashMap<String, String>> TypeToNameToDist = new HashMap<String, HashMap<String, String>>();
        
        for(String idRadio : new String[]{"hotelsRadio", "bbRadio", "othersRadio"})
        {
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(idRadio)));
	        driver.findElement(By.id(idRadio)).click();
	    
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchButton")));
	        driver.findElement(By.id("searchButton")).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
	        driver.findElement(By.id("sortText")).click();
	        
	        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Distance")));
	        driver.findElement(By.partialLinkText("Distance")).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
	        
	        List<WebElement> content = driver.findElements(By.id("contentWrapper"));
	        
	        HashMap<String, String> NameToDist = new HashMap<String, String>();
	          
	        int counter = 15;
	        do
	        {	
	        	
	        	WebElement accual = content.get(0);
	            WebElement last = content.get(content.size()-1);
	            
	        	NameToDist.put(accual.findElement(By.id("title")).getText(), accual.findElement(By.id("distance")).getText());
	        	        	
	        	List<WebElement> tContent;
	        	counter = 15;
	        	
	        	do
	         	{
	    		 tContent = driver.findElements(By.id("contentWrapper"));
	    		 
	    		 counter--;
	    		 
	    		 new TouchActions(driver).flick(accual, 0, -100, 0).perform();
	
	         	}while(counter >= 0 && tContent.get(tContent.size()-1).findElement(By.id("title")).getText().equals(last.findElement(By.id("title")).getText()));
	        	 
	        	if (counter >= 0)
	        	{
			    	content.add(tContent.get(tContent.size()-1));
	        	}
	        	content.remove(0);
	        	
	        }while(counter >= 0 && !content.isEmpty());
	        
	        while(!content.isEmpty())
	        {
		        WebElement accual = content.get(0);
		        NameToDist.put(accual.findElement(By.id("title")).getText(), accual.findElement(By.id("distance")).getText());
		        content.remove(0);
	        }
	        System.out.println(idRadio + ": " + NameToDist.size());
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ImageView[@id='up']")));
            driver.findElement(By.xpath("//ImageView[@id='up']")).click();
            
            TypeToNameToDist.put(idRadio, NameToDist);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ImageView[@id='up']")));
    	driver.findElement(By.xpath("//ImageView[@id='up']")).click();
        return TypeToNameToDist;
    }
    
    
    
	void checkDataRestaurants(Map<String, String> NameToDist) throws InterruptedException
    {
		System.out.println("Restaurants");
    	wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Restaurants")));
        driver.findElement(By.partialLinkText("Restaurants")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
        driver.findElement(By.id("sortText")).click();
        
        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Distance")));
        driver.findElement(By.partialLinkText("Distance")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
        
        List<WebElement> content = driver.findElements(By.id("contentWrapper"));

        List<WebElement> tContent;
        int counter  = 15;
        
        do
        {	
        	
        	WebElement acctual = content.get(0);
        	WebElement last = content.get(content.size()-1);
        	
        	if(!NameToDist.containsKey(acctual.findElement(By.id("title")).getText()))
        	{
        		System.err.println("No such Object as " + acctual.findElement(By.id("title")).getText());
        		content.remove(0);
        		failureCunter++;
        		continue;
        	}	
        	
        	if(!NameToDist.get(acctual.findElement(By.id("title")).getText()).equals(content.get(0).findElement(By.id("distance")).getText()))
        	{
        		System.err.println("mishmash at " + acctual.findElement(By.id("title")).getText() + ", " +  
        				"Expected: " + NameToDist.get(acctual.findElement(By.id("title")).getText())
        				+ " Not: " + content.get(0).findElement(By.id("distance")).getText());
        		failureCunter++;
        	}
        	else
        		succesCunter++;
        	
        	
        	counter = 15;
        	
        	do
         	{
    		 tContent = driver.findElements(By.id("contentWrapper"));
    		 
    		 counter--;
    		 
    		 new TouchActions(driver).flick(acctual, 0, -100, 0).perform();

         	}while(counter >= 0 && tContent.get(tContent.size()-1).findElement(By.id("title")).getText().equals(last.findElement(By.id("title")).getText()));

        	if (counter >= 0)
        	{
		    	content.add(tContent.get(tContent.size()-1));
        	}
        	content.remove(0);
        }while(counter >= 0 && !content.isEmpty());
    	
        while(!content.isEmpty())
        {
	        WebElement acctual = content.get(0);
	        
	        if(!NameToDist.containsKey(acctual.findElement(By.id("title")).getText()))
        	{
	        	System.err.println("No such Object as " + acctual.findElement(By.id("title")).getText());
        		content.remove(0);
        		failureCunter++;
        		continue;
        	}	
        	if(!NameToDist.get(acctual.findElement(By.id("title")).getText()).equals(content.get(0).findElement(By.id("distance")).getText()))
        	{
        		System.err.println("mishmash at " + acctual.findElement(By.id("title")).getText()+ ", " +   
        				"Expected: " + NameToDist.get(acctual.findElement(By.id("title")).getText())
        				+ " Not: " + content.get(0).findElement(By.id("distance")).getText());
        		failureCunter++;
        	}
        	else
        		succesCunter++;
        	
	       content.remove(0);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ImageView[@id='up']")));
    	driver.findElement(By.xpath("//ImageView[@id='up']")).click();
    }
    
    void checkDataThingToDo(Map<String, HashMap<String, String>> CategoryToNameToDist) throws InterruptedException{
    	wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Things to Do")));
        driver.findElement(By.partialLinkText("Things to Do")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
        driver.findElement(By.id("filterText")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("Select all")));
    	driver.findElement(By.partialLinkText("Select all")).click();
    	
        
        Object[] CategoriesStrings = CategoryToNameToDist.keySet().toArray();
           
        for(int i=0;i<CategoriesStrings.length;i++)
        {
        	System.out.println(CategoriesStrings[i]);
        	HashMap<String, String> NameToDist = CategoryToNameToDist.get(CategoriesStrings[i].toString());
        	if(i!=0)
        	{
        		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
                driver.findElement(By.id("filterText")).click();
                
	        	wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(CategoriesStrings[i-1].toString())));
	        	driver.findElement(By.partialLinkText(CategoriesStrings[i-1].toString())).click();
	        
	        	while(driver.findElements(By.partialLinkText(CategoriesStrings[i].toString())).size() <= 0)
        		{
	        		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")));
	        		WebElement tmp = driver.findElements(By.xpath("(//RelativeLayout[@id = \"item_cell\"])")).get(0);
        			new TouchActions(driver).flick(tmp, 0, (int)-1.3*tmp.getSize().getHeight(), 0).perform();
        		}
        	}
        	
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(CategoriesStrings[i].toString())));
	        driver.findElement(By.partialLinkText(CategoriesStrings[i].toString())).click();

	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("action_done")));
	        driver.findElement(By.id("action_done")).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchButton")));
	        driver.findElement(By.id("searchButton")).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
	        driver.findElement(By.id("sortText")).click();
	        
	        
	        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Distance")));
	        driver.findElement(By.partialLinkText("Distance")).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
	        
	        List<WebElement> content = driver.findElements(By.id("contentWrapper"));
	    	
	        List<WebElement> tContent;
	        int counter  = 15;
	        
	        do
	        {	
	        	
	        	WebElement acctual = content.get(0);
	        	WebElement last = content.get(content.size()-1);
	        	
	        	if(!NameToDist.containsKey(acctual.findElement(By.id("title")).getText()))
	        	{
	        		System.err.println("No such Object as " + acctual.findElement(By.id("title")).getText());
	        		content.remove(0);
	        		failureCunter++;
	        		continue;
	        	}	
	        	
	        	if(!NameToDist.get(acctual.findElement(By.id("title")).getText()).equals(content.get(0).findElement(By.id("distance")).getText()))
	        	{
	        		System.err.println("mishmash at " + acctual.findElement(By.id("title")).getText() + ", " +  
	        				"Expected: " + NameToDist.get(acctual.findElement(By.id("title")).getText())
	        				+ " Not: " + content.get(0).findElement(By.id("distance")).getText());
	        		failureCunter++;
	        	}
	        	else
	        		succesCunter++;
	        	
	        	
	        	counter = 15;
	        	boolean next;
	        	
	        	do
	         	{
	    		 tContent = driver.findElements(By.id("contentWrapper"));
	    		 
	    		 counter--;
	    		 
	    		 new TouchActions(driver).flick(acctual, 0, -100, 0).perform();
	
	    		 next = false;
	    		 
	    		 try
	    		 {
	    			 next = counter >= 0 && tContent.get(tContent.size()-1).findElement(By.id("title")).getText().equals(last.findElement(By.id("title")).getText());
	    		 }
	    		 catch(Exception e){
	    			 counter = -1;
	    		 }
		        	finally {;}
	    		 
	         	}while(next);
        	        	
	        	if (counter >= 0)
	        	{
			    	content.add(tContent.get(tContent.size()-1));
	        	}
	        	content.remove(0);
	        }while(counter >= 0 && !content.isEmpty());
	    	
	        while(!content.isEmpty())
	        {
		        WebElement acctual = content.get(0);
		        
		        if(!NameToDist.containsKey(acctual.findElement(By.id("title")).getText()))
	        	{
		        	System.err.println("No such Object as " + acctual.findElement(By.id("title")).getText());
	        		content.remove(0);
	        		failureCunter++;
	        		continue;
	        	}	
	        	if(!NameToDist.get(acctual.findElement(By.id("title")).getText()).equals(content.get(0).findElement(By.id("distance")).getText()))
	        	{
	        		System.err.println("mishmash at " + acctual.findElement(By.id("title")).getText() + ", " +   
	        				"Expected: " + NameToDist.get(acctual.findElement(By.id("title")).getText())
	        				+ " Not: " + content.get(0).findElement(By.id("distance")).getText());
	        		failureCunter++;
	        	}
	        	else
	        		succesCunter++;
	        	
		       content.remove(0);
	        }
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ImageView[@id='up']")));
    	driver.findElement(By.xpath("//ImageView[@id='up']")).click();
    }
    
	void checkDataHotels(Map<String, HashMap<String, String>> TypeToNameToDist) throws InterruptedException
    {
    	wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Hotels")));
        driver.findElement(By.partialLinkText("Hotels")).click();
        
        for(String idRadio : new String[]{"hotelsRadio", "bbRadio", "othersRadio"})
        {
        	HashMap<String, String> NameToDist = TypeToNameToDist.get(idRadio);
        	
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(idRadio)));
	        driver.findElement(By.id(idRadio)).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchButton")));
	        driver.findElement(By.id("searchButton")).click();
        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
	        driver.findElement(By.id("sortText")).click();
	        
	        wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText("Distance")));
	        driver.findElement(By.partialLinkText("Distance")).click();
	        
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("distance")));
	        
	        List<WebElement> content = driver.findElements(By.id("contentWrapper"));
	
	        List<WebElement> tContent;
	        int counter  = 15;
	        
	        do
	        {	
	        	
	        	WebElement acctual = content.get(0);
	        	WebElement last = content.get(content.size()-1);
	        	
	        	if(!NameToDist.containsKey(acctual.findElement(By.id("title")).getText()))
	        	{
	        		System.err.println("No such Object as " + acctual.findElement(By.id("title")).getText());
	        		content.remove(0);
	        		failureCunter++;
	        		continue;
	        	}	
	        	
	        	if(!NameToDist.get(acctual.findElement(By.id("title")).getText()).equals(content.get(0).findElement(By.id("distance")).getText()))
	        	{
	        		System.err.println("mishmash at " + acctual.findElement(By.id("title")).getText() + ", " +   
	        				"Expected: " + NameToDist.get(acctual.findElement(By.id("title")).getText())
	        				+ " Not: " + content.get(0).findElement(By.id("distance")).getText());
	        		failureCunter++;
	        	}
	        	else
	        		succesCunter++;
	        	
	        	
	        	counter = 15;
	        	
	        	do
	         	{
	    		 tContent = driver.findElements(By.id("contentWrapper"));
	    		 
	    		 counter--;
	    		 
	    		 new TouchActions(driver).flick(acctual, 0, -100, 0).perform();
	
	         	}while(counter >= 0 && tContent.get(tContent.size()-1).findElement(By.id("title")).getText().equals(last.findElement(By.id("title")).getText()));
	
	        	if (counter >= 0)
	        	{
			    	content.add(tContent.get(tContent.size()-1));
	        	}
	        	content.remove(0);
	        }while(counter >= 0 && !content.isEmpty());
	    	
	        while(!content.isEmpty())
	        {
		        WebElement acctual = content.get(0);
		        
		        if(!NameToDist.containsKey(acctual.findElement(By.id("title")).getText()))
	        	{
		        	System.err.println("No such Object as " + acctual.findElement(By.id("title")).getText());
	        		content.remove(0);
	        		failureCunter++;
	        		continue;
	        	}	
	        	if(!NameToDist.get(acctual.findElement(By.id("title")).getText()).equals(content.get(0).findElement(By.id("distance")).getText()))
	        	{
	        		System.err.println("mishmash at " + acctual.findElement(By.id("title")).getText() + ", " +   
	        				"Expected: " + NameToDist.get(acctual.findElement(By.id("title")).getText())
	        				+ " Not: " + content.get(0).findElement(By.id("distance")).getText());
	        		failureCunter++;
	        	}
	        	else
	        		succesCunter++;
	        	
		       content.remove(0);
	        }
	        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ImageView[@id='up']")));
            driver.findElement(By.xpath("//ImageView[@id='up']")).click();
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ImageView[@id='up']")));
    	driver.findElement(By.xpath("//ImageView[@id='up']")).click();
    }
    
    
    
    @BeforeClass
    public static void startSelendroidServer() throws Exception {
      if (selendroidServer != null) {
        selendroidServer.stopSelendroid();
      }
      SelendroidConfiguration config = new SelendroidConfiguration();
      config.setLogLevel(LogLevelEnum.WARNING);
      config.addSupportedApp("F:/_Praktyki/real/com.tripadvisor.tripadvisor.apk");
      selendroidServer = new SelendroidLauncher(config);
      selendroidServer.launchSelendroid();

      SelendroidCapabilities caps =
          new SelendroidCapabilities("com.tripadvisor.tripadvisor:11.0");
      caps.setEmulator(false);

      driver = new SelendroidDriver(caps);
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
}