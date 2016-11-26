package com.minktek.andfortune;

import org.json.JSONObject;

/*
 * Interface so that AsyncTask-derived classes do not have to know about
 * Activity-derived classes
 */
public interface InterfaceMessage 
{
   /*
    * parameters: 
    *     jobj - JSON object containing return results; can be null
    *     msg  - error message indicating why jobj is null
    *
    *     Note: One will always be set and one will always be null
    */
   public String SendMessage(JSONObject jobj, String msg);
}
