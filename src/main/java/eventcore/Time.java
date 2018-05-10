package eventcore;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Time {

    public static int DAY = 0;
    public static int MONTH = 1;
    public static int YEAR = 2;
    public static int HOUR = 3;
    public static int MINUTES = 4;

    public static String[] getTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy:HH:mm");
        String[] time = sdf.format(cal.getTime()).split("\\:");
        return time;
    }

    public static long getTimeDifferenceMinutes(String[] eventTime){
        int[] currentTime = {Integer.parseInt(Time.getTime()[0].replace(" ", "")),
                Integer.parseInt(Time.getTime()[1].replace(" ", "")),
                Integer.parseInt(Time.getTime()[2].replace(" ", "")),
                Integer.parseInt(Time.getTime()[3].replace(" ", "")),
                Integer.parseInt(Time.getTime()[4].replace(" ", ""))};
        int[] evenTime = {Integer.parseInt(eventTime[0].replace(" ", "")),
                Integer.parseInt(eventTime[1].replace(" ", "")),
                Integer.parseInt(eventTime[2].replace(" ", "")),
                Integer.parseInt(eventTime[3].replace(" ", "")),
                Integer.parseInt(eventTime[4].replace(" ", ""))};
        Date startDate = new Date(currentTime[YEAR], currentTime[MONTH], currentTime[DAY], currentTime[HOUR], currentTime[MINUTES]);
        Date endDate   = new Date(evenTime[YEAR], evenTime[MONTH], evenTime[DAY], evenTime[HOUR], evenTime[MINUTES]);

        long duration  = endDate.getTime() - startDate.getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        System.out.println("Event is "+ diffInMinutes + " away!");
        return diffInMinutes;
    }

    public static String[] getEventTime(String time) {
        String[] timeF = time.split("\\:");
        return timeF;
    }

    public static int howMannyDaysMont(int year, int month, int day){
        long iMonth = month + 1;
        Calendar mycal = new GregorianCalendar(year, (int) iMonth, day);
        return mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static String formatDate(String[] time){
        return time[DAY] + "." + time[MONTH] + "." + time[YEAR];
    }

    public static String formatTime(String[] time) {
        return time[HOUR] + ":" + time[MINUTES];
    }

    public static int[] convertToIntArray(String[] eventTime) {
        int[] timeInt = {Integer.parseInt(eventTime[0].replace(" ", "")),
                Integer.parseInt(eventTime[1].replace(" ", "")),
                Integer.parseInt(eventTime[2].replace(" ", "")),
                Integer.parseInt(eventTime[3].replace(" ", "")),
                Integer.parseInt(eventTime[4].replace(" ", ""))};
        return timeInt;
    }
}