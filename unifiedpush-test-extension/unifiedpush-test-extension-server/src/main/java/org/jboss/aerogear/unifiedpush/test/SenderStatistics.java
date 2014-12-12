package org.jboss.aerogear.unifiedpush.test;

import com.google.android.gcm.server.Message;

import java.util.List;

/**
 * Created by asaleh on 12/11/14.
 */
public class SenderStatistics {
    public List<String> deviceTokens;
    public Message gcmMessage;
    public String apnsAlert;
    public int apnsBadge;
    public String apnsSound;
    public String apnsCustomFields;
    public long apnsExpiry;
    public String gcmForChromeAlert;
}
