package eventcore;

import commands.CmdEvent;
import core.Main;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventTimeChecker implements Runnable {

    private Guild g;
    private Event e;

    public EventTimeChecker(Guild g, Event e){
        this.g = g;
        this.e = e;
    }

    @Override
    public void run() {
        e.setIsntStarted(false);
        if (e.isRepeatable()){
            System.out.println("CHECKER, REPEATABLE!");
            int[] eventTime = Time.convertToIntArray(e.getEventTime());
            int DAY = eventTime[Time.DAY] + 7;
            int MONTH = eventTime[Time.MONTH];
            int YEAR = eventTime[Time.YEAR];
            int HOUR = eventTime[Time.HOUR];
            int MINUTES = eventTime[Time.MINUTES];
            while (DAY > Time.howMannyDaysMont(eventTime[Time.YEAR], eventTime[Time.MONTH], eventTime[Time.DAY])){
                DAY %= Time.howMannyDaysMont(eventTime[Time.YEAR], eventTime[Time.MONTH], eventTime[Time.DAY]);
                MONTH = MONTH + 1;
            }
            while (MONTH > 12){
                MONTH %= 12;
                YEAR = YEAR + 1;
            }
            String dAY = DAY + "";
            String mONTH = MONTH + "";
            String yEAR = YEAR + "";
            String hOUR = HOUR + "";
            String mINUTES = MINUTES + "";
            if (DAY < 10){
                dAY = "0" + DAY;
            }
            if (MONTH < 10){
                mONTH = "0" + MONTH;
            }
            if (HOUR < 10){
                hOUR = "0" + HOUR;
            }
            if (MINUTES < 10){
                mINUTES = "0" + MINUTES;
            }
            String[] newEventTime = {dAY, mONTH, yEAR, hOUR, mINUTES};
            Event toAddEvent = new Event(newEventTime, e.getName(), e.getDescription(), true);
            String allParticipants = "";
            if (e.getParticipants().size() <= 0){
                allParticipants = "Niemand!";
            }else {
                for (String player : e.getParticipants()){
                    allParticipants = allParticipants + "  " + Main.jda.getUserById(player).getName();
                }
            }
            MessageEmbed eb = new EmbedBuilder()
                    .setAuthor("Das Event: " + e.getName() + " startet!")
                    .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                    .setDescription(e.getDescription())
                    .setFooter("Spieler die Mitmachen: " + allParticipants, null)
                    .build();
            for (String s : toAddEvent.getParticipants()){
                Main.jda.getUserById(s).openPrivateChannel().queue((channel) -> {
                    channel.sendMessage(eb).queue();
                });
            }
            int id = CmdEvent.EVENTS.get(g).indexOf(e);
            CmdEvent.EVENTS.get(g).remove(id);
            ScheduledFuture sf2 = CmdEvent.ALLUSERSSCHEDUELEDEVENTS.get(g.getId()).get(id);
            List<ScheduledFuture> tempFuture2 = CmdEvent.ALLUSERSSCHEDUELEDEVENTS.get(g.getId());
            tempFuture2.remove(sf2);
            CmdEvent.ALLUSERSSCHEDUELEDEVENTS.put(g.getId(), tempFuture2);
            CmdEvent.EVENTS.get(g).remove(e);


            List<Event> temp = CmdEvent.EVENTS.get(g);
            temp.add(toAddEvent);
            CmdEvent.EVENTS.put(g, temp);
            EventTimeChecker c = new EventTimeChecker(g, toAddEvent);
            ScheduledFuture sf = new ScheduledThreadPoolExecutor(1).schedule(c, Time.getTimeDifferenceMinutes(toAddEvent.getEventTime()), TimeUnit.MINUTES);
            List<ScheduledFuture> tempFuture = CmdEvent.ALLUSERSSCHEDUELEDEVENTS.get(g.getId());
            tempFuture.add(sf);
            CmdEvent.ALLUSERSSCHEDUELEDEVENTS.put(g.getId(), tempFuture);

            try {
                CmdEvent.saveEvent(g);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (CmdEvent.EVENTS.get(g).size() < 1) {
                try {
                    CmdEvent.deleteAllEvents(g);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            MessageChannel c2 = g.getTextChannelsByName("event", true).get(0);
            String allParticipants2 = "";
            if (toAddEvent.getParticipants().size() <= 0) {
                allParticipants2 = "Niemand!";
            } else {
                for (String player : toAddEvent.getParticipants()) {
                    allParticipants2 = allParticipants2 + "  " + Main.jda.getUserById(player).getName();
                }
            }
            MessageEmbed eb2 = new EmbedBuilder()
                    .setAuthor(toAddEvent.getName() + " ID: " + CmdEvent.EVENTS.get(g).indexOf(toAddEvent))
                    .setTitle("Datum: " + Time.formatDate(toAddEvent.getEventTime()) + " Zeit: " + Time.formatTime(toAddEvent.getEventTime()))
                    .setDescription(toAddEvent.getDescription())
                    .addField("","Wöchentlich : " + toAddEvent.isRepeatable() + "", true)
                    .setFooter("Spieler die Mitmachen: " + allParticipants2, null)
                    .build();
            for (Message text : c2.getIterableHistory()){
                for (MessageEmbed s : text.getEmbeds()){
                    String[] f= s.getAuthor().getName().split(" ");
                    if (Integer.parseInt(f[f.length - 1]) == id){
                        Message m = c2.getMessageById(text.getId()).complete();
                        m.editMessage(eb2).queue();
                    }
                }
            }
        }else{
            System.out.println("CHECKER!");
            String allParticipants = "";
            if (e.getParticipants().size() <= 0){
                allParticipants = "Niemand!";
            }else {
                for (String player : e.getParticipants()){
                    allParticipants = allParticipants + "  " + Main.jda.getUserById(player).getName();
                }
            }
            MessageEmbed eb = new EmbedBuilder()
                    .setAuthor("Das Event: " + e.getName() + " startet!")
                    .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                    .setDescription(e.getDescription())
                    .setFooter("Spieler die Mitmachen: " + allParticipants, null)
                    .build();
            for (String s : e.getParticipants()){
                Main.jda.getUserById(s).openPrivateChannel().queue((channel) -> {
                    channel.sendMessage(eb).queue();
                });
            }
            int id = CmdEvent.EVENTS.get(g).indexOf(e);
            CmdEvent.EVENTS.get(g).remove(id);
            ScheduledFuture sf2 = CmdEvent.ALLUSERSSCHEDUELEDEVENTS.get(g.getId()).get(id);
            List<ScheduledFuture> tempFuture2 = CmdEvent.ALLUSERSSCHEDUELEDEVENTS.get(g.getId());
            tempFuture2.remove(sf2);
            CmdEvent.ALLUSERSSCHEDUELEDEVENTS.put(g.getId(), tempFuture2);
            CmdEvent.EVENTS.get(g).remove(e);

            MessageChannel c3 = g.getTextChannelsByName("event", true).get(0);
            for (Message text : c3.getIterableHistory()){
                for (MessageEmbed s : text.getEmbeds()) {
                    String[] f = s.getAuthor().getName().split(" ");
                    if (Integer.parseInt(f[f.length - 1]) == id) {
                        Message m = c3.getMessageById(text.getId()).complete();
                        m.delete().queue();
                    }
                }
                }
            }
        try {
            CmdEvent.saveEvent(g);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (CmdEvent.EVENTS.get(g).size() < 1) {
            try {
                CmdEvent.deleteAllEvents(g);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
