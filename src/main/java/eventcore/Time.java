package eventcore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Time {

    public static int DAY = 0;
    public static int MONTH = 1;
    public static int YEAR = 2;
    public static int HOUR = 3;
    public static int MINUTES = 4;


    public static long getTimeDifferenceMinutes(String[] eventTime){
        int[] evenTime = {Integer.parseInt(eventTime[0].replace(" ", "")),
                Integer.parseInt(eventTime[1].replace(" ", "")) - 1,
                Integer.parseInt(eventTime[2].replace(" ", "")),
                Integer.parseInt(eventTime[3].replace(" ", "")),
                Integer.parseInt(eventTime[4].replace(" ", ""))};

        Calendar startDate = Calendar.getInstance();
        startDate.setTimeZone(TimeZone.getDefault());
        Calendar endDate = Calendar.getInstance();
        endDate.set(evenTime[YEAR], evenTime[MONTH], evenTime[DAY], evenTime[HOUR], evenTime[MINUTES]);
        endDate.setTimeZone(TimeZone.getDefault());

        System.out.println("Event starts at: " + startDate.getTime());

        System.out.println("Event ends at: " + endDate.getTime());

        long duration  = endDate.getTime().getTime() - startDate.getTime().getTime();
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