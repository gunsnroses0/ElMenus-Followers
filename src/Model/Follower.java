
    
package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

import Commands.Command;

public class Follower {
	private static final String COLLECTION_NAME = "followers";
	private static int DbPoolCount = 4;
	static String host = System.getenv("MONGO_URI");

	public static int getDbPoolCount() {
		return DbPoolCount;
	}
	public static void setDbPoolCount(int dbPoolCount) {
		DbPoolCount = dbPoolCount;
	}
	
	static MongoClientOptions.Builder options = null;
	static MongoClientURI uri = null;
	static MongoClient mongoClient = null; 
	
	public static void initializeDb() {
		options = MongoClientOptions.builder()
				.connectionsPerHost(DbPoolCount);
		uri = new MongoClientURI(
				host,options);
		mongoClient = new MongoClient(uri);
			
	}
	private static MongoCollection<Document> collection = null;
	
	public static HashMap<String, Object> create(HashMap<String, Object> attributes, String target_username) throws ParseException {

		MongoDatabase database = mongoClient.getDatabase("El-Menus");

		// Retrieving a collection
		MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
		Document newFollower = new Document();

		for (String key : attributes.keySet()) {
			newFollower.append(key, attributes.get(key));
		}

		newFollower.append("target_username", target_username);
		
		collection.insertOne(newFollower);
		JSONParser parser = new JSONParser();

		HashMap<String, Object> returnValue = Command.jsonToMap((JSONObject) parser.parse(newFollower.toJson()));


		mongoClient.close();
		
		return returnValue;
	}
	public static HashMap<String, Object> delete(String messageId) {
		
		MongoDatabase database = mongoClient.getDatabase("El-Menus");
//    	Method method =   Class.forName("PlatesService").getMethod("getDB", null);
//    	MongoDatabase database = (MongoDatabase) method.invoke(null, null);

		// Retrieving a collection
		MongoCollection<Document> collection = database.getCollection("followers");
		System.out.println("Inside Delete");
		BasicDBObject query = new BasicDBObject();
		System.out.println(messageId);
		query.put("_id", new ObjectId(messageId));

		System.out.println(query.toString());
		HashMap<String, Object> message = null;
		Document doc = collection.findOneAndDelete(query);
		JSONParser parser = new JSONParser(); 
		try {
			JSONObject json = (JSONObject) parser.parse(doc.toJson());
		
			message = Command.jsonToMap(json);
			
			System.out.println(message.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return message;
	}
	public static ArrayList<ArrayList<HashMap<String, Object>>> get(String username) {

		MongoClient mongoClient = new MongoClient(uri);
		MongoDatabase database = mongoClient.getDatabase("El-Menus");
//    	Method method =   Class.forName("PlatesService").getMethod("getDB", null);
//    	MongoDatabase database = (MongoDatabase) method.invoke(null, null);

		// Retrieving a collection
		MongoCollection<Document> collection = database.getCollection("followers");
		System.out.println("Inside Get");
		BasicDBObject followersQuery = new BasicDBObject();
		System.out.println(username);
		followersQuery.put("target_username", username);

		System.out.println(followersQuery.toString());
		HashMap<String, Object> message = null;
		FindIterable<Document> followersDocs = collection.find(followersQuery);
		JSONParser followersParser = new JSONParser(); 
		ArrayList<HashMap<String, Object>> followers = new ArrayList<HashMap<String, Object>>();
		
		for (Document document : followersDocs) {
			JSONObject json;
			try {
				json = (JSONObject) followersParser.parse(document.toJson());
				HashMap<String, Object> follower = Command.jsonToMap(json);	
				followers.add(follower);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		BasicDBObject followingsQuery = new BasicDBObject();
		System.out.println(username);
		followingsQuery.put("username", username);
		FindIterable<Document> followingsDocs = collection.find(followingsQuery);
		JSONParser followingsParser = new JSONParser(); 
		ArrayList<HashMap<String, Object>> followings = new ArrayList<HashMap<String, Object>>();
		
		for (Document document : followingsDocs) {
			JSONObject json;
			try {
				json = (JSONObject) followingsParser.parse(document.toJson());
				HashMap<String, Object> following = Command.jsonToMap(json);	
				followings.add(following);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("followings"+followings);
		System.out.println("followers"+followers);
		ArrayList<ArrayList<HashMap<String, Object>>> result = new ArrayList<ArrayList<HashMap<String, Object>>> ();
		result.add(followers);
		result.add(followings);
		System.out.println(result.toString());
        return result;
	}
	
}
