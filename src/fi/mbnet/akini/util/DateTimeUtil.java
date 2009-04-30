/**
 * Copyright: http://koti.mbnet.fi/akini/
 * @author
 * @version $Id$
 */
package fi.mbnet.akini.util;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Common datetime functions
 */
public class DateTimeUtil {
	public static final String DATE_MASK     = "yyyy-MM-dd";
	public static final String TIME_MASK     = "HH:mm:ss";
	public static final String DATETIME_MASK = DATE_MASK + " " + TIME_MASK;
	public static final String DATETIMESHORT_MASK = DATE_MASK + " " + "HH:mm"; 

	public static Calendar createUTCCalendar() {
		return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}
	
	public static Calendar createUTCCalendar(long ts) {
		Calendar cal = createUTCCalendar();
		cal.setTimeInMillis(ts);
		return cal;
	}

	/**
	 * Create UTC calendar from yyyy-MM-dd hh:mm:ss.SSSS
	 * 
	 * Mysql bug: http://bugs.mysql.com/bug.php?id=15604
     * Using string formatted timstamp we can create UTC
     * calendar instance from datetime column value.
	 * 
	 * @param ymdhms
	 * @return
	 */
	public static Calendar createUTCCalendar(String ymdhms) {
		if (ymdhms == null) return null;
		
	    int y = Integer.parseInt(ymdhms.substring(0, 4));
	    int m = Integer.parseInt(ymdhms.substring(5, 7));
	    int d = Integer.parseInt(ymdhms.substring(8, 10));

	    int h, mi,s, millis;
	    if (ymdhms.length() > 11) {
		    h = Integer.parseInt(ymdhms.substring(11, 13));
		    mi = Integer.parseInt(ymdhms.substring(14, 16));
		    s = Integer.parseInt(ymdhms.substring(17, 19));
		    millis = (ymdhms.length() > 20 ? Integer.parseInt(ymdhms.substring(20)) : 0 );
	    } else {
	    	h = mi = s = millis = 0;
	    }
	    
	    Calendar cal = createUTCCalendar();

	    cal.set(Calendar.YEAR, y);
	    cal.set(Calendar.MONTH, m-1);
	    cal.set(Calendar.DAY_OF_MONTH, d);
	    cal.set(Calendar.HOUR_OF_DAY, h);
	    cal.set(Calendar.MINUTE, mi);
	    cal.set(Calendar.SECOND, s);
	    cal.set(Calendar.MILLISECOND, millis);

	    return cal;
		
	}
	
	/**
	 * Format calendar to yyyy-MM-dd hh:mm:ss string.
	 * @param c
	 * @return
	 */
	public static String formatDateTime(Calendar c) {
		return formatDateTime(c, DATETIME_MASK);
	}

	/**
	 * Format calendar using the given mask
	 * @param c		calendar instance
	 * @param mask	datetime mask (example: yyyy-MM-dd hh:mm:ss)
	 * @return
	 */
	public static String formatDateTime(Calendar cal, String mask) {
		SimpleDateFormat sdf = new SimpleDateFormat(mask);
		sdf.setTimeZone(cal.getTimeZone());
		return sdf.format(cal.getTime());	   
	}
	
	/**
	 * Format calendar to yyyy-MM-dd string.
	 * @param c
	 * @return
	 */
	public static String formatDate(Calendar c) {
		return formatDateTime(c, DATE_MASK);
	}

	/**
	 * Convert datetime to the given timezone, calendar object
	 * can use any timezone.
	 * @param cal
	 * @param tz	target timezone or NULL to use system timezone
	 * @return
	 */
	public static String formatDateTime(Calendar cal, TimeZone tz) {
		return formatDateTime(cal, tz, DATETIME_MASK);
	}

	public static String formatDateTime(Calendar cal, TimeZone tz, String mask) {
		SimpleDateFormat df = new SimpleDateFormat(mask);
		if (tz != null) df.setTimeZone(tz);
		return df.format(cal.getTime());
	}

	/**
	 * Format RSS date-time using RFC822 specs with 4-digit year.
	 * http://www.faqs.org/rfcs/rfc822.html
	 * @param c
	 * @return	rss date-time (21 Feb 2008 23:31:05 +0200)
	 */
	public static String formatRSSDateTime(Calendar cal) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z", Locale.US);
		sdf.setTimeZone(cal.getTimeZone());
		return sdf.format(cal.getTime());	   
	}
	
	/**
	 * Get system timestamp as string
	 * @return	yyyy-MM-dd hh:mm:ss
	 */
   public static String getNowAsString() {
      return formatDateTime(Calendar.getInstance());
   }

   /**
    * Get system UTC timestamp as string
    * @return	yyyy-MM-dd hh:mm:ss
    */
   public static String getNowAsUTCString() {
	   Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	   return formatDateTime(now);
	   //Calendar now = Calendar.getInstance();
	   //return formatDateTime( toTimeZone(now, "UTC") );
   }

   /**
    * Convert calendar to another timezone calendar.
    * @param cal
    * @param tz
    * @return
    */
   public static Calendar toTimeZone(Calendar cal, String tz) {
	   return toTimeZone(cal, TimeZone.getTimeZone(tz));
   }

   public static Calendar toTimeZone(Calendar cal, TimeZone tz) {
	   Calendar cal2 = Calendar.getInstance(tz);
	   cal2.setTimeInMillis(cal.getTimeInMillis());
	   return cal2;
   }
   
   /**
    * Convert calendar to default timezone.
    * @param cal
    * @return
    */
   public static Calendar toTimeZone(Calendar cal) {
	   return toTimeZone(cal, TimeZone.getDefault());   
   }
   
   public static void setDatePart(Calendar cal, int y, int m, int d) {
	   cal.set(Calendar.YEAR, y);
	   cal.set(Calendar.MONTH, m-1);
	   cal.set(Calendar.DAY_OF_MONTH, d);
   }

   public static void setTimePart(Calendar cal, int h, int m, int s, int ms) {
	   cal.set(Calendar.HOUR_OF_DAY, h);
	   cal.set(Calendar.MINUTE, m);
	   cal.set(Calendar.SECOND, s);
	   cal.set(Calendar.MILLISECOND, ms);
   }

   /**
    * Is given calendar value between time range.
    * @param start	start of range
    * @param end	end of range
    * @param cal	calendar value
    * @return		true, if is between given range
    */
   public static boolean isBetween(Calendar start, Calendar end, Calendar cal) {
	   	if ( (cal.after(start)) || (cal.equals(start)) )
	   		if ( (cal.before(end)) || (cal.equals(end)) )
	   			return true;
	   	return false;
   }
   
}
