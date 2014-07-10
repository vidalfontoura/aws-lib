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
import java.io.IOException;
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
public interface ISQSClient {
  /* @@_BEGIN: STATICS ----------------------------------------------------- */
  /* @@_END: STATICS ------------------------------------------------------- */

  /* @@_BEGIN: PROPERTIES -------------------------------------------------- */
  /* @@_END: PROPERTIES ---------------------------------------------------- */

  /* @@_BEGIN: METHODS ----------------------------------------------------- */
  /**
   *
   * @param queueName
   * @return
   * @throws IOException
   */
  String createQueue(String queueName)
    throws IOException;

  /**
   *
   * @param queueUrl
   * @throws IOException
   */
  void deleteQueue(String queueUrl)
    throws IOException;

  /**
   *
   * @param queueUrl
   * @param content
   * @throws IOException
   */
  void send(String queueUrl,
            String content)
    throws IOException;

  /**
   *
   * @param queueUrl
   * @return
   */
  boolean hasPendingMessages(String queueUrl);

  /**
   *
   * @param queueUrl
   * @return
   * @throws IOException
   */
  String receive(String queueUrl)
    throws IOException;
  /* @@_END: METHODS ------------------------------------------------------- */
} // ISQSClient
/* @@_END: CLASS DEFINITION ------------------------------------------------ */
