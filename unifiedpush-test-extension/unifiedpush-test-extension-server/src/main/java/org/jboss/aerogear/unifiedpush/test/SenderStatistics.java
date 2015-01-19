package org.jboss.aerogear.unifiedpush.test;

import com.google.android.gcm.server.Message;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by asaleh on 12/11/14.
 */
public class SenderStatistics {
    public List<String> deviceTokens = new ArrayList<String>();
    public Message gcmMessage;
    public String apnsAlert;
    public int apnsBadge;
    public String apnsSound;
    public String apnsCustomFields;
    public long apnsExpiry;
    public String gcmForChromeAlert;
}
