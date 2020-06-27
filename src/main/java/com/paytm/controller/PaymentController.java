package com.paytm.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paytm.pg.merchant.CheckSumServiceHelper;

@RestController
@RequestMapping("payment")
public class PaymentController {
	
	@Autowired
	private Environment env;
	
	
	
	@PostMapping
	public String payment(@RequestParam String transactionId,@RequestParam String amount)throws Throwable {
		
		/* initialize a TreeMap object */
		TreeMap<String, String> paytmParams = new TreeMap<String, String>();

		/*
		 * Find your MID in your Paytm Dashboard at
		 * https://dashboard.paytm.com/next/apikeys
		 */
		paytmParams.put("MID",env.getProperty("merchantId"));

		/*
		 * Find your WEBSITE in your Paytm Dashboard at
		 * https://dashboard.paytm.com/next/apikeys
		 */
		paytmParams.put("WEBSITE",env.getProperty("website"));

		/*
		 * Find your INDUSTRY_TYPE_ID in your Paytm Dashboard at
		 * https://dashboard.paytm.com/next/apikeys
		 */
		paytmParams.put("INDUSTRY_TYPE_ID",env.getProperty("industryTypeId"));

		/* WEB for website and WAP for Mobile-websites or App */
		paytmParams.put("CHANNEL_ID",env.getProperty("channelId"));

		/* Enter your unique order id */
		paytmParams.put("ORDER_ID",transactionId);

		/* unique id that belongs to your customer */
		paytmParams.put("CUST_ID", "123456");

		/* customer's mobile number */
		//paytmParams.put("MOBILE_NO",mobileNo);

		/* customer's email */
		//paytmParams.put("EMAIL",email);

		/**
		 * Amount in INR that is payble by customer this should be numeric with
		 * optionally having two decimal points
		 */
		paytmParams.put("TXN_AMOUNT", amount);

		/* on completion of transaction, we will send you the response on this URL */
		paytmParams.put("CALLBACK_URL",env.getProperty("callbackUrl"));
	
		/**
		 * Generate checksum for parameters we have You can get Checksum JAR from
		 * https://developer.paytm.com/docs/checksum/ Find your Merchant Key in your
		 * Paytm Dashboard at https://dashboard.paytm.com/next/apikeys
		 */
		String checksum = CheckSumServiceHelper.getCheckSumServiceHelper().genrateCheckSum(env.getProperty("merchantKey"),
				paytmParams);

		/* for Staging */
		String url = "https://securegw-stage.paytm.in/order/process";

		/* for Production */
		// String url = "https://securegw.paytm.in/order/process";

		/* Prepare HTML Form and Submit to Paytm */
		StringBuilder outputHtml = new StringBuilder();
		outputHtml.append("<html>");
		outputHtml.append("<head>");
		outputHtml.append("<title>Merchant Checkout Page</title>");
		outputHtml.append("</head>");
		outputHtml.append("<body>");
		outputHtml.append("<center><h1>Please do not refresh this page...</h1></center>");
		outputHtml.append("<form method='post' action='" + url + "' name='paytm_form'>");

		for (Map.Entry<String, String> entry : paytmParams.entrySet()) {
			outputHtml.append("<input type='hidden' name='" + entry.getKey() + "' value='" + entry.getValue() + "'>");
		}

		outputHtml.append("<input type='hidden' name='CHECKSUMHASH' value='" + checksum + "'>");
		outputHtml.append("</form>");
		outputHtml.append("<script type='text/javascript'>");
		outputHtml.append("document.paytm_form.submit();");
		outputHtml.append("</script>");
		outputHtml.append("</body>");
		outputHtml.append("</html>");

		return outputHtml.toString();

	
	}
	

	@PostMapping("/check-payment-status")
	public Map<String,String> checkPaymentStatus(HttpServletRequest request) throws Throwable{
		
		
		Map<String, String[]> mapData = request.getParameterMap();
		TreeMap<String, String> parameters = new TreeMap<String, String>();
		mapData.forEach((key, val) -> parameters.put(key, val[0]));
		
		
		TreeMap<String, String> paytmParams = new TreeMap<String, String>();
		paytmParams.put("MID",env.getProperty("merchantId"));
		paytmParams.put("ORDERID",parameters.get("ORDERID"));
		
		/*
		* Generate checksum by parameters we have
		* You can get Checksum JAR from https://developer.paytm.com/docs/checksum/
		* Find your Merchant Key in your Paytm Dashboard at https://dashboard.paytm.com/next/apikeys 
		*/
		String checksum = CheckSumServiceHelper.getCheckSumServiceHelper().genrateCheckSum(env.getProperty("merchantKey"),
				paytmParams);

		paytmParams.put("CHECKSUMHASH", checksum);

		JSONObject obj = new JSONObject(paytmParams);

		String post_data = obj.toString();
		
		Map<String,String> response=new HashMap<String, String>();

		/* for Staging */
		URL url = new URL("https://securegw-stage.paytm.in/order/status");

		/* for Production */
		// URL url = new URL("https://securegw.paytm.in/order/status");

		try {
		    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("POST");
		    connection.setRequestProperty("Content-Type", "application/json");
		    connection.setDoOutput(true);
		    
		    
		    DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
		    requestWriter.writeBytes(post_data);
		    requestWriter.close();
		    String responseData = "";
		    InputStream is = connection.getInputStream();
		    BufferedReader responseReader = new BufferedReader(new InputStreamReader(is));
		    Map<String,String> map=new HashMap<String, String>();
			
		    if ((responseData = responseReader.readLine()) != null) {	
		    	System.out.println("resonse :"+responseData);
		    	responseData=responseData.replaceAll("\\{","");
		    	responseData=responseData.replaceAll("\\s","");
		         String array[]=responseData.split(",");
		         
		         for (int i = 0; i < array.length; i++) {
		        	 System.out.println("first"+array[i]);
		        	 String arr[]=array[i].split(":");
		        	 for (int j = 0; j < arr.length; j++) {
		        		 //System.out.println("second"+array[j]);
		        		 //System.out.println("array[0]"+arr[0]+"      sss "+arr[1]);
		        		 map.put(arr[0].replaceAll("\"",""),arr[1].replaceAll("\"",""));
		        		 arr=new String[1];
					}	
				}
		    }
		    
		    String result="";
		   
			if (map.containsKey("RESPCODE")) {
				if (map.get("RESPCODE").equals("01")) {		
					result = "Success";
				} else {
					result = "Failed";
				}
			} else {
				result = "Checksum mismatched";
			}
		    
			responseReader.close();
			response.put("status",result);
		    return response;
		 
		} catch (Exception exception) {
		    exception.printStackTrace();
		}
		return response;
	}
	
}
