/* @@_BEGIN: COPYRIGHT ----------------------------------------------------- */
///////////////////////////////////////////////////////////////////////////////
//
///////////////////////////////////////////////////////////////////////////////
/* @@_END: COPYRIGHT ------------------------------------------------------- */
/* @@_BEGIN: REVISION HISTORY ---------------------------------------------- */
///////////////////////////////////////////////////////////////////////////////
// ----------------------------------------------------------------------------
//  REVISION HISTORY
// ----------------------------------------------------------------------------
// 2014-07-10  10:23  matthewsmith  Created
// ----------------------------------------------------------------------------
///////////////////////////////////////////////////////////////////////////////
/* @@_END: REVISION HISTORY ------------------------------------------------ */
package com.charter.aesd.aws.sqsclient;
/* @@_BEGIN: IMPORTS ------------------------------------------------------- */
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;
import com.charter.aesd.aws.s3client.enums.S3AuthType;

import java.io.IOException;
import java.util.ArrayList;
/* @@_END: IMPORTS --------------------------------------------------------- */

/* @@_BEGIN: CLASS DEFINITION ---------------------------------------------- */
/**
 * ----------------------------------------------------------------------------
 * $Id: $
 * ----------------------------------------------------------------------------
 * <p/>
 * User: matthewsmith Date: 7/10/14 Time: 10:23 AM
 *
 * @author $Author: $
 * @version $Rev: $
 * @since ${date}
 */
public class SQSClient
  implements ISQSClient {
  /* @@_BEGIN: LOGGING ----------------------------------------------------- */
  /* @@_END: LOGGING ------------------------------------------------------- */

  /* @@_BEGIN: STATICS ----------------------------------------------------- */
  /* @@_END: STATICS ------------------------------------------------------- */

  /* @@_BEGIN: MEMBERS ----------------------------------------------------- */
  /**
   *
   */
  private AmazonSQS _awsSQSClient=null;
  /* @@_END: MEMBERS ------------------------------------------------------- */

  /* @@_BEGIN: CONSTRUCTION ------------------------------------------------ */
  /**
   * @param client
   */
  private SQSClient(final AmazonSQS client) {
    _awsSQSClient=client;
  }
  /* @@_END: CONSTRUCTION -------------------------------------------------- */

  /* @@_BEGIN: PROPERTIES -------------------------------------------------- */
  /**
   * @return
   */
  protected AmazonSQS getClient() {
    return _awsSQSClient;
  }
  /* @@_END: PROPERTIES ---------------------------------------------------- */

  /* @@_BEGIN: METHODS ----------------------------------------------------- */
  /**
   * @param queueName
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public String createQueue(final String queueName)
    throws IOException {
    return getClient().createQueue(new CreateQueueRequest().withQueueName(queueName)).getQueueUrl();
  }

  /**
   * @param queueUrl
   *
   * @throws IOException
   */
  @Override
  public void deleteQueue(final String queueUrl)
    throws IOException {
    getClient().deleteQueue(queueUrl);
  }

  /**
   *
   * @param queueUrl
   * @return
   */
  @Override
  public boolean hasPendingMessages(final String queueUrl) {
    ReceiveMessageResult result = getClient().receiveMessage(queueUrl);

    java.util.List<Message> msgs = (result == null) ? new ArrayList<Message>(0) : result.getMessages();

    return !msgs.isEmpty();
  }

  /**
   * @param content
   *
   * @throws IOException
   */
  @Override
  public void send(final String queueUrl,
                   final String content)
    throws IOException {
    getClient().sendMessage(new SendMessageRequest(queueUrl,
                                                   content));
  }

  /**
   * @return one message
   *
   * @throws IOException
   */
  @Override
  public String receive(final String queueUrl)
    throws IOException {
    ReceiveMessageResult result = getClient().receiveMessage(queueUrl);

    java.util.List<Message> msgs = (result == null) ? new ArrayList<Message>(0) : result.getMessages();
    if (msgs.isEmpty()) {
      return null;
    }

    return msgs.get(0).getBody();
  }
  /* @@_END: METHODS ------------------------------------------------------- */

  /**
   * Builder class for constructing an instance of {@link SQSClient}
   */
  public static class Builder {
    /**
     *
     */
    private ClientConfiguration _config = null;

    /**
     *
     */
    public Builder() {
      _config=new ClientConfiguration();
    }

    /**
     * Sets the {@link ClientConfiguration} used to configure
     *
     * @param config
     *   {@link ClientConfiguration}
     *
     * @return {@link Builder}
     */
    public Builder setConfig(ClientConfiguration config) {
      _config=config;

      return this;
    }

    public SQSClient build() {
      return new SQSClient(new AmazonSQSClient(getConfiguration()));
    }

    /**
     * Creates a {@code ClientConfiguration} object using the System properties for {@code http.proxyHost} and {@code
     * http.proxyPort}. To leverage this both host and port must be set using the -D args (i.e., {@code
     * -Dhttp.proxyHost=my.proxy.host.com -Dhttp.proxyPort=3128} and if auth is required {@code
     * -Dhttp.proxyUser=username -Dhttp.proxyPassword=password1234}.
     *
     * @return A {@ClientConfiguration}. Never {@code null}.
     */
    public ClientConfiguration getConfiguration() {
      String proxyHost=System.getProperty("http.proxyHost");
      String proxyPort=System.getProperty("http.proxyPort");
      String proxyUserName=System.getProperty("http.proxyUser");
      String proxyUserPasswd=System.getProperty("http.proxyPassword");

      if (proxyHost != null) {
        _config.setProxyHost(proxyHost);
      }

      if (proxyPort != null) {
        _config.setProxyPort(Integer.parseInt(proxyPort));
      }

      if (proxyUserName != null) {
        _config.setProxyUsername(proxyUserName);
      }

      if (proxyUserPasswd != null) {
        _config.setProxyPassword(proxyUserPasswd);
      }

      return _config;
    }
  }
} // SQSClient
/* @@_END: CLASS DEFINITION ------------------------------------------------ */
