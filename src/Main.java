import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Main {
	
	public static final String CRED_JEMS = "YOUR_ID:YOUR_PASSWORD"; //ID:PASSWORD
	
	public static Cookie createNonPersistentCookie() {
	       
		return new Cookie.Builder()
                .domain("jems.sbc.org.br")
                .path("/")
                .name("edas")
                .value(CRED_JEMS)
                .httpOnly()
                .secure()
                .build();
    }
	
	
	public static String getInfos(int id) throws IOException{
		
		OkHttpClient client = new OkHttpClient().newBuilder()
                .cookieJar(new CookieJar() {
          
					public List<Cookie> loadForRequest(HttpUrl url) {
						final ArrayList<Cookie> oneCookie = new ArrayList<>(1);
                        oneCookie.add(createNonPersistentCookie());
                        return oneCookie;
					}

					
					public void saveFromResponse(HttpUrl arg0, List<Cookie> arg1) {
						
						
					}
                })
                .build();

		Request request = new Request.Builder()
		  .url("https://jems.sbc.org.br/PersonShow.cgi?who="+id)
		  .get()
		  .build();

		Response response = client.newCall(request).execute();
		

	//	System.out.println(response.headers());
		
		
		String htmlString = response.body().source().readString(Charset.forName("ISO-8859-1"));
		
		Document doc = Jsoup.parse(htmlString);
		
		Elements nameTag = doc.getElementsByTag("H1"); 
		String name = nameTag.text().trim();
	
		if(name.isEmpty())return null;
		
		Elements data = doc.getElementsByTag("tr"); 
		
		String json = "{";
	
		if(!name.isEmpty())json+="\n\t\"Name\": "+"\""+name+"\",";
		
		
		
		for (Element headline : data) {
			
			String column = headline.getElementsByTag("td").get(0).text().replace("#", "").trim();
			
			String value = headline.getElementsByTag("td").get(1).text().trim();
			
			json+="\n\t\""+column+"\": "+"\""+value+"\",";
		}
		
		
		json += "\n}";
		
		return json;
	}
	
	
	
public static String getTopics(int id) throws IOException{
		
		OkHttpClient client = new OkHttpClient().newBuilder()
                .cookieJar(new CookieJar() {
          
					public List<Cookie> loadForRequest(HttpUrl url) {
						final ArrayList<Cookie> oneCookie = new ArrayList<>(1);
                        oneCookie.add(createNonPersistentCookie());
                        return oneCookie;
					}

					
					public void saveFromResponse(HttpUrl arg0, List<Cookie> arg1) {
						
						
					}
                })
                .build();

		Request request = new Request.Builder()
		  .url("https://jems.sbc.org.br/MyTopics.cgi?person="+id)
		  .get()
		  .build();

		Response response = client.newCall(request).execute();
		

	//	System.out.println(response.headers());
		
		
		String htmlString = response.body().source().readString(Charset.forName("ISO-8859-1"));
		
		Document doc = Jsoup.parse(htmlString);
		
		Elements data = doc.getElementsByTag("table").get(0).getElementsByTag("td"); 

		String json = "{";
		
		json+="\n\t\"ID\": "+id+",";
	
		json+="\n\t\"Conferences\": [";
		
		int count = 0;
		for (Element headline : data) {
			
			if(count == 0){
				
				json+="\n\t\t{\"Name\": "+"\""+headline.text()+"\"";
			}
			if(count == 1){

				
				Elements topics = headline.getElementsByTag("table").get(0).getElementsByTag("td");
				
				Elements interest = topics.get(0).getElementsByTag("li");
				
				
				json+="\n\t\t\t\"Topics of interest\": [  ";
				
				for (Element in : interest) {
					json+="\""+in.text()+"\", ";
				}
				json=removeLastChar(json);
				json=removeLastChar(json);
				json+="\n\t\t\t],";
				
				Elements notInterest = topics.get(1).getElementsByTag("li");
				
				json+="\n\t\t\t\"Topics not of interest\": [  ";
				
				for (Element in : notInterest) {
					json+="\""+in.text()+"\", ";
				}
				json=removeLastChar(json);
				json=removeLastChar(json);
				json+="\n\t\t\t]";
				
			}
			
			if(count >= 3){
				count = 0;
				json+="\n\t\t},";
				continue;
			}
			count++;
			
		//	json+="\n\t\""+column+"\": "+"\""+value+"\"";
		}
		json=removeLastChar(json);
		
		json+="\n\t]";
		
		json += "\n}";
		
		return json;
	}
	
	public static void main(String[] args) throws InterruptedException {

		for (int i = 0; i < 10; i++) {

			try {
				String data = getInfos(i);
				if(data!=null){
					System.out.println(data);
					//System.out.println(getTopics(i));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			Thread.sleep(1000);

		}

	}
	
	private static String removeLastChar(String str) {
	    return str.substring(0, str.length() - 1);
	}
	

	
}
