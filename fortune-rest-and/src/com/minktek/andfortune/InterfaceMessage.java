package com.minktek.andfortune;

/*
 * Interface so that AsyncTask-derived classes do not have to know about
 * Activity-derived classes
 */
public interface InterfaceMessage 
{
   public String SendMessage(String msg);
}
