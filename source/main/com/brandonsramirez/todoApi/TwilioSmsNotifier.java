package com.brandonsramirez.todoApi;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import java.util.HashMap;
import java.util.Map;
 
public class TwilioSmsNotifier implements SmsNotifier {
  private TwilioRestClient client;
  private String userTwilioNumber;
  private String userMobileNumber;

  TwilioSmsNotifier(String accountSid, String authToken, String userTwilioNumber, String userMobileNumber) {
    this.client = new TwilioRestClient(accountSid, authToken);
    this.userTwilioNumber = userTwilioNumber;
    this.userMobileNumber = userMobileNumber;
  }

  public void notifyUserOfTaskCompletion(Task task) {
    Map<String, String> params = new HashMap<String, String>();
    params.put("Body", "\"" + task.getTitle() + "\" task has been marked as done.");
    params.put("To", userMobileNumber);
    params.put("From", userTwilioNumber);
 
    try {
      SmsFactory messageFactory = client.getAccount().getSmsFactory();
      messageFactory.create(params);
    }
    catch (TwilioRestException e) {
      e.printStackTrace();
      // Don't propogate the exception down the stack.  The request should still be considered successful even if we fail to send a text.
    }
  }
}
