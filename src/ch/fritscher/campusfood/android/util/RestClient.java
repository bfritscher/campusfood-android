package ch.fritscher.campusfood.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class RestClient {
	
	static final String COOKIE_FILENAME = "campusfood_cookies";
	
    private ArrayList <NameValuePair> params;
    private ArrayList <NameValuePair> headers;
    private String fileFieldName;
    private ContentBody fileContentBody;
    private boolean isAjax = true;
    private boolean sendCookie = true;
    
    private String url;

    private int responseCode;
    private String message;

    private String response;
    
    //private static CookieStore cookieStore;
    
    public enum RequestMethod
    {
    GET,
    POST,
    POST_MULTIPART
    }
    
    public String getResponse() {
        return response;
    }

    public String getErrorMessage() {
        return message;
    }

    public int getResponseCode() {
        return responseCode;
    }
   
    public boolean isAjax(){
    	return isAjax;
    }
    
    public void setAjax(boolean bool){
    	isAjax = bool;
    }

    public RestClient(String url)
    {
    	this.url = url;
        params = new ArrayList<NameValuePair>();
        headers = new ArrayList<NameValuePair>();
    }

    public void AddParam(String name, String value)
    {
        params.add(new BasicNameValuePair(name, value));
    }

    public void AddHeader(String name, String value)
    {
        headers.add(new BasicNameValuePair(name, value));
    }
    
    public void AddFile(String fieldName, ContentBody contentBody){
    	fileFieldName = fieldName;
    	fileContentBody = contentBody;
    }

    public void Execute(RequestMethod method) throws Exception
    {
    	switch(method) {
            case GET:
            {
                //add parameters
                String combinedParams = "";
                if(!params.isEmpty()){
                    combinedParams += "?";
                    for(NameValuePair p : params)
                    {
                        String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(),"UTF-8");
                        if(combinedParams.length() > 1)
                        {
                            combinedParams  +=  "&" + paramString;
                        }
                        else
                        {
                            combinedParams += paramString;
                        }
                    }
                }

                HttpGet request = new HttpGet(url + combinedParams);

                //add headers
                request = (HttpGet) addHeaderToRequest(request);
                executeRequest(request, url);
                break;
            }
            case POST:
            {
                HttpPost request = new HttpPost(url);

                //add headers
                request = (HttpPost) addHeaderToRequest(request);
                
                if(!params.isEmpty()){
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                }

                executeRequest(request, url);
                break;
            }
            case POST_MULTIPART:
            {
                HttpPost request = new HttpPost(url);

                //add headers
                request = (HttpPost) addHeaderToRequest(request);
                

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                if(fileFieldName != null && fileContentBody != null){
                	reqEntity.addPart(fileFieldName, fileContentBody);
                }
                if(!params.isEmpty()){
                    for (NameValuePair nv: params) {
						reqEntity.addPart(nv.getName(), new StringBody(nv.getValue(), "text/plain", Charset.forName("UTF-8")));
					}
                }
                
                request.setEntity(reqEntity);

                executeRequest(request, url);
                break;
            }
            	
    	}
    }
    
    private AbstractHttpMessage addHeaderToRequest(AbstractHttpMessage request){
        for(NameValuePair h : headers){
            request.addHeader(h.getName(), h.getValue());
        }
        if(isAjax){
    		request.addHeader("X-Requested-With", "XMLHttpRequest");
    		request.addHeader("Accept", "application/json");
        }
        if(sendCookie){
        	request.addHeader("Cookie", CookieManager.getInstance().getCookie("isisvn.unil.ch"));
        }
    	return request;
    }

	private void executeRequest(HttpUriRequest request, String url) throws Exception
    {
		HttpParams httpParameters = new BasicHttpParams();
		
		// Set the timeout in milliseconds until a connection is established.
		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		//HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient client = new DefaultHttpClient(httpParameters);
		//client.setCookieStore(getCookieStore());
		
        HttpResponse httpResponse;

        try {
            httpResponse = client.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            
            message = httpResponse.getStatusLine().getReasonPhrase();

            HttpEntity entity = httpResponse.getEntity();

            if (entity != null) {

                InputStream instream = entity.getContent();
                response = convertStreamToString(instream);         
                List<Cookie> cookies = client.getCookieStore().getCookies();
                if(cookies.size() > 0){
	                for (Cookie cookie : cookies) {
	                	CookieManager.getInstance().setCookie(cookie.getDomain(), cookie.getName() + "=" + cookie.getValue());
					}
	                CookieSyncManager.getInstance().sync();
                }
                // Closing the input stream will trigger connection release
                instream.close();
            }

        } catch (ClientProtocolException e)  {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
        } catch (IOException e) {
            client.getConnectionManager().shutdown();
            e.printStackTrace();
            throw new Exception(e);
        }
    }
	/*
    private void saveCookies(List<Cookie> cookies) {
        StringBuilder cookiesString = new StringBuilder();
    	for (Cookie cookie : cookies) {
        	cookiesString.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
		}
    	try{
    		FileOutputStream fos = CampusFoodApplication.getContext().openFileOutput(COOKIE_FILENAME, Context.MODE_PRIVATE);
    		fos.write(cookiesString.toString().getBytes());
    		fos.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}

	}
	
    private String retrieveCookieString() {
    	StringBuffer buffer = new StringBuffer();
    	try{
			FileInputStream foi = CampusFoodApplication.getContext().openFileInput(COOKIE_FILENAME);
			BufferedInputStream  in = new BufferedInputStream(foi);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char)ch);
			}
			in.close();
			foi.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
		return buffer.toString();
		
	}
    */
	/*
    private static CookieStore getCookieStore(){
    	if(cookieStore == null){
    		//reload cookiestore
    		try {
    		   FileInputStream fis = CampusFoodApplication.getContext().openFileInput(COOKIE_FILENAME);
    		   ObjectInputStream ois = new ObjectInputStream(fis);
    		   cookieStore = (CookieStore) ois.readObject();
    		   ois.close();
    		}catch (Exception e) {
    			cookieStore = new BasicCookieStore();
			}
    		//or create new
    	}
    	return cookieStore;
    }
    */
    /*
    private static void saveCookieStore(){
    	
	    CookieSyncManager.getInstance().sync();
    	try{
    		FileOutputStream fos = CampusFoodApplication.getContext().openFileOutput(COOKIE_FILENAME, Context.MODE_PRIVATE);
    		ObjectOutputStream oos = new ObjectOutputStream(fos);
    		oos.writeObject(cookieStore);
    		oos.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    */
    private static String convertStreamToString(InputStream is) {
    	
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
