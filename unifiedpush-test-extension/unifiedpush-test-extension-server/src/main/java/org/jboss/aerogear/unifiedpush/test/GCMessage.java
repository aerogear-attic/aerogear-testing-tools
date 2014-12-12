package org.jboss.aerogear.unifiedpush.test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by asaleh on 12/11/14.
 */
public class GCMessage {
    public HashMap<String,String> data;
    public String collapse_key;
    public Boolean delay_while_idle;
    public Integer time_to_live;
    public ArrayList<String> registration_ids;
}
