package UBC.G8.SmartSafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
	//data types to hold the mappings of apps and safes
	private List<Safe> unMappedSafes = Collections.synchronizedList(new ArrayList<Safe>());
	private Map<AndroidApp, ArrayList<Safe>> safeMapping = Collections.synchronizedMap(new HashMap<AndroidApp, ArrayList<Safe>>());
	
	public Database(){
	}
	
	//add uninitialized safe into the unmapped list
	public void addToUnmapped(Safe safe){
		unMappedSafes.add(safe);
	}
	
	//adds uninitialized safes to mapping if they become initialized by the App
	public void moveUnmappedSafeToMapped(String safeId, String appId){
		//find the correct app int he mapping and then extract the safe from the unmapped list 
		//and place it into the mapping
		for (AndroidApp a : safeMapping.keySet()){
			if(a.getId().equals(appId)){
				for(int i = 0; i < unMappedSafes.size(); i++){
					if (unMappedSafes.get(i).getId().equals(safeId)){
						safeMapping.get(a).add(unMappedSafes.get(i));
						synchronized (unMappedSafes.get(i)){ //synchronized so that a safe can only be mapped to 1 app
							unMappedSafes.get(i).notify();
						}
						unMappedSafes.remove(i); //remove from unmapped list
					}
				}
			}
		}
		return;
	}
	/*
	 * method to add a custom mapping to the databsae
	 */
	public void addSafe(String appId, String safeId){
		for (AndroidApp a : safeMapping.keySet()){
			if (a.getId().equals(appId)){
				safeMapping.get(a).add(new Safe(safeId));
				return;
			}
		}
		ArrayList<Safe> listToAdd = new ArrayList<Safe>();
		listToAdd.add(new Safe(safeId));
		safeMapping.put(new AndroidApp(appId), listToAdd);
		return;
	}
	
	/*
	 * method to add a custom app to the mapping
	 */
	public AndroidApp addApp(String appId){
		for (AndroidApp a: safeMapping.keySet()){
			if (a.getId().equals(appId)){
				System.out.println("app already exists");
				return a;
			}
		}
		AndroidApp newApp = new AndroidApp(appId);
		safeMapping.put(newApp, new ArrayList<Safe>());
		return newApp;
	}
	/*
	 * get the app object from the database given its ID
	 */
	public AndroidApp getApp(String appId){
		for(AndroidApp app : safeMapping.keySet()){
			if(app.getId().equals(appId))
				return app;
		}
	 return null;
	}
	
	/*
	 * get the safe object from the database given its ID
	 */
	public Safe getSafe(String safeId){
		for(ArrayList<Safe> list : safeMapping.values()){
			for(Safe s : list){
				if(s.getId().equals(safeId))
					return s;
			}
		}
		return null;
	}
	
	/*
	 * get all the safes mapped to a single App given its ID
	 */
	public ArrayList<Safe> getSafesFromApp(String appId){
		for(AndroidApp app : safeMapping.keySet()){
			if(app.getId().equals(appId)){
				return safeMapping.get(app);
			}
		}
		return null;
	}
	
	/*
	 * get the corresponding App that maps to the specified safe by ID
	 */
	public AndroidApp getCorrespondingApp(String safeId){
		for(AndroidApp app: safeMapping.keySet()){
			for(Safe safe : safeMapping.get(app)){
				if(safe.getId().equals(safeId)){
					return app;
				}
			}
		}
		return null;
	}	
}
