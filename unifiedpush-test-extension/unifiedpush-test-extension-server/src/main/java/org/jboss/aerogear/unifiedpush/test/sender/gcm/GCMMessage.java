package org.jboss.aerogear.unifiedpush.test.sender.gcm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by asaleh on 12/11/14.
 */
public class GCMMessage {
    public HashMap<String,String> data;
    public String collapseKey;
    public Boolean delayWhileIdle;
    public Integer timeToLive;
    public ArrayList<String> registrationIds;
}
