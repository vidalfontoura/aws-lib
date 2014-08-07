package com.charter.aesd.aws.snsclient.demo;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SignatureException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import javax.xml.bind.DatatypeConverter;

/**
 * This class is meant to be self contained to demonstrate a slimmed
 * down class using HTTP to publish to the SNS Topic directly and limiting
 * dependencies.
 * <p/>
 * Very simple and straigtforward to possibly use as Stored Proc in Oracle
 */
public class SimpleHTTPSNS {
    /**
     *
     */
    private final static String AWS_ACCESS_KEY = "AKIAICIJW4N4A5MZUHVA";
    private final static String AWS_SECRET_KEY = "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";
    private final static String SNS_TOPIC_ARN = "arn:aws:sns:us-east-1:524950595403:QueuePOC-Demo-Topic";
    private final static String SNS_AWS_HOST= "sns.us-east-1.amazonaws.com";
    private final static String ENTITLEMENT_MESSAGE = "{" +
                    " \"MessageId\" : \"{messageId}\"," +
                    " \"MessageName\" : \"VideoEntitlements\"," +
                    " \"AccountNumber\" : \"80092320357266\"," +
                    " \"LastModified\" : {lastModified}" +
                    "}";
    private final static String TS_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     *
     */
    /**
     * No command line arguments
     */
    public static void main(String[] args) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            StringBuffer queryStr = new StringBuffer();
            queryStr.append("AWSAccessKeyId=");
            queryStr.append(URLEncoder.encode(AWS_ACCESS_KEY, "UTF-8"));
            queryStr.append("&Action=Publish");

            String msg = ENTITLEMENT_MESSAGE.replaceAll("\\{messageId\\}",
                                                        UUID.randomUUID().toString())
                                            .replaceAll("\\{lastModified\\}",
                                                        Long.toString(System.currentTimeMillis()));

            queryStr.append("&Message=");
            queryStr.append(URLEncoder.encode(msg, "UTF-8"));
            queryStr.append("&SignatureMethod=HmacSHA256");
            queryStr.append("&SignatureVersion=2");
            queryStr.append("&Timestamp=");
            SimpleDateFormat df = new SimpleDateFormat(TS_FORMAT);
            TimeZone tz = TimeZone.getTimeZone("UTC");
            df.setTimeZone(tz);
            queryStr.append(URLEncoder.encode(df.format(new Date()), "UTF-8"));
            queryStr.append("&TopicArn=");
            queryStr.append(URLEncoder.encode(SNS_TOPIC_ARN, "UTF-8"));
            queryStr.append("&Version=2010-03-31");

            String queryData = queryStr.toString().replaceAll("\\+", "%20");
            StringBuffer sigData = new StringBuffer();
            sigData.append("GET\n");
            sigData.append(SNS_AWS_HOST);
            sigData.append("\n");
            sigData.append("/\n");
            sigData.append(queryData);
            System.out.println("SIG DATA - BEGIN");
            System.out.print(queryData);
            System.out.println("\nSIG DATA - END");
            String sig = new Signature().calculateRFC2104HMAC(sigData.toString(),
                                                              AWS_SECRET_KEY);

            queryData += "&Signature=";
            queryData += URLEncoder.encode(sig, "UTF-8");

            HttpGet httpget = new HttpGet("http://" + SNS_AWS_HOST + "/?" + queryData);
            System.out.println("Executing request " + httpget.getRequestLine());

            CloseableHttpResponse resp = httpclient.execute(httpget);
            StatusLine respStatus = resp.getStatusLine();
            System.out.println("----------------------------------------");
            System.out.println("Response Status:  " + respStatus.getStatusCode() +
                               " - " + respStatus.getReasonPhrase());
            System.out.println("-- BODY - BEGIN");
            resp.getEntity().writeTo(System.out);
            System.out.println("-- BODY - END");
            System.out.println("----------------------------------------");
        } catch(Exception e) {
          e.printStackTrace(System.err);
        } finally {
            try {
                httpclient.close();
            } catch(Exception e) {}
        }
    }

    static public class Signature {
        private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

        /**
         * Computes RFC 2104-compliant HMAC signature.
         * * @param data
         * The signed data.
         * @param key
         * The signing key.
         * @return
         * The Base64-encoded RFC 2104-compliant HMAC signature.
         * @throws
         * java.security.SignatureException when signature generation fails
         */
        public static String calculateRFC2104HMAC(String data, String key)
        throws java.security.SignatureException
        {
            String result;
            try {

                // Get an hmac_sha256 key from the raw key bytes.
                SecretKeySpec signingKey = new SecretKeySpec(key.getBytes("UTF8"), HMAC_SHA256_ALGORITHM);

                // Get an hmac_sha256 Mac instance and initialize with the signing key.
                Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
                mac.init(signingKey);

                // Compute the hmac on input data bytes.
                byte[] rawHmac = mac.doFinal(data.getBytes("UTF8"));

                // Base64-encode the hmac by using the utility in the SDK
                //result = BinaryUtils.toBase64(rawHmac);
                result = DatatypeConverter.printBase64Binary(rawHmac);
            } catch (Exception e) {
                throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
            }
            return result;
        }
    }
} // SimpleHTTPSNS
