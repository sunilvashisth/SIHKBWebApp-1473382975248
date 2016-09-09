package com.ibm.sih.db;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.sih.Config;
import com.ibm.sih.Constants;



/** A class for communicating with the Cloudant datastore. 
 *  See main() for example usage.
 *  
 *  @author Sean Welleck**/
public class CloudantClient 
{
	private HttpClient httpClient;
	private CouchDbConnector dbc;
	
	private int port;
	private String name;
	private String host;
	private String username;
	private String password;
	
	private JSONArray cloudant;
    private JSONObject cloudantInstance;
    private JSONObject cloudantCredentials;
	
	public CloudantClient()
	{
		this.httpClient = null;

		 try {
            String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
            JSONObject vcap;
            vcap = (JSONObject) JSONObject.parse(VCAP_SERVICES);
            
            cloudant = (JSONArray) vcap.get("cloudantNoSQLDB");
            cloudantInstance = (JSONObject) cloudant.get(0);
            cloudantCredentials = (JSONObject) cloudantInstance.get("credentials");
        } catch (IOException e) {
            e.printStackTrace();
        }
		this.port = Config.CLOUDANT_PORT;
		this.host = (String) cloudantCredentials.get("host");
		this.username = (String) cloudantCredentials.get("username");
		this.password = (String) cloudantCredentials.get("password");
		this.name = Config.CLOUDANT_NAME;
		this.dbc = this.createDBConnector();
	}
	
	/** Put a generic item modeled as Key-Value pairs into Cloudant. **/
	private void putItem(HashMap<String, Object> data)
	{
		if (data == null) 
		{ 
			System.err.println("data cannot be null in putItem()"); 
			return;
		}
		String id = (String)data.get(Constants.ID_KEY);
		if (id == null)   
		{ 
			System.err.println("data must have an _id field."); 
			return;
		}
		if (this.dbc.contains(id)) 
		{ 
			System.err.println("Didn't putItem. _id=" + id + " already exists."); 
			return;
		}
		this.dbc.create(data);
		System.out.println("Put _id=" + id + " into the datastore."); 
	}
	
	private CouchDbConnector createDBConnector() 
	{
		CouchDbInstance dbInstance = null;
		
		System.out.println("Creating CouchDB instance...");
		System.out.println(this.username);
		this.httpClient = new StdHttpClient.Builder()
		.host(this.host)
		.port(this.port)
		.username(this.username)
		.password(this.password)
		.enableSSL(true)
		.relaxedSSLSettings(true)
		.build();

		dbInstance = new StdCouchDbInstance(this.httpClient);
		CouchDbConnector dbc = new StdCouchDbConnector(this.name, dbInstance);
		dbc.createDatabaseIfNotExists();
		if (dbc.getAllDocIds().size() == 0) {
			dbc.replicateFrom("https://jsloyer.cloudant.com/talent-manager");
			while (true) {
				if (dbc.getAllDocIds().size() == 117) {
					break;
				}
				else {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return dbc;
	}
	
	private void closeDBConnector()
	{
		if (httpClient != null)
		{
			httpClient.shutdown();
		}
	}
	
	/** Example usage. **/
	public static void main(String[] args) throws Exception
	{
		CloudantClient cc = new CloudantClient();
		
			cc.closeDBConnector();
	}
}