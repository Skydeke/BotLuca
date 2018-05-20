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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventTimeChecker implements Runnable {

    private Guild g;
    private Event e;

    public EventTimeChecker(Guild g, Event e) {
        this.g = g;
        this.e = e;
    }

    @Override
    public void run() {
        e.setIsntStarted(false);
        if (e.isRepeatable()) {
            System.out.println("CHECKER, REPEATABLE!");
            int[] eventTime = Time.convertToIntArray(e.getEventTime());
            int DAY = eventTime[Time.DAY] + 7;
            int MONTH = eventTime[Time.MONTH];
            int YEAR = eventTime[Time.YEAR];
            int HOUR = eventTime[Time.HOUR];
            int MINUTES = eventTime[Time.MINUTES];
            while (DAY > Time.howMannyDaysMont(eventTime[Time.YEAR], eventTime[Time.MONTH], eventTime[Time.DAY])) {
                DAY %= Time.howMannyDaysMont(eventTime[Time.YEAR], eventTime[Time.MONTH], eventTime[Time.DAY]);
                MONTH = MONTH + 1;
            }
            while (MONTH > 12) {
                MONTH %= 12;
                YEAR = YEAR + 1;
            }
            String dAY = DAY + "";
            String mONTH = MONTH + "";
            String yEAR = YEAR + "";
            String hOUR = HOUR + "";
            String mINUTES = MINUTES + "";
            if (DAY < 10) {
                dAY = "0" + DAY;
            }
            if (MONTH < 10) {
                mONTH = "0" + MONTH;
            }
            if (HOUR < 10) {
                hOUR = "0" + HOUR;
            }
            if (MINUTES < 10) {
                mINUTES = "0" + MINUTES;
            }
            String[] newEventTime = {dAY, mONTH, yEAR, hOUR, mINUTES};
            Event toAddEvent = new Event(newEventTime, e.getName(), e.getDescription(), true);
            String allParticipants = CmdEvent.getParticipants(e, g);
            for (String s : toAddEvent.getParticipants()) {
                Main.jda.getUserById(s).openPrivateChannel().complete().sendMessage(CmdEvent.getParsedEvent(e, g, allParticipants, CmdEvent.getMaybes(e, g), "Das Event: " + e.getName() + " startet!").build()).queue();
            }
            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    int id = CmdEvent.EVENTS.get(g).indexOf(e);
                    CmdEvent.EVENTS.get(g).remove(id);
                    ScheduledFuture sf2 = CmdEvent.ALLSCHEDUELEDEVENTS.get(g.getId()).get(id);
                    List<ScheduledFuture> tempFuture2 = CmdEvent.ALLSCHEDUELEDEVENTS.get(g.getId());
                    tempFuture2.remove(sf2);
                    CmdEvent.ALLSCHEDUELEDEVENTS.put(g.getId(), tempFuture2);
                    CmdEvent.EVENTS.get(g).remove(e);


                    List<Event> temp = CmdEvent.EVENTS.get(g);
                    temp.add(toAddEvent);
                    CmdEvent.EVENTS.put(g, temp);
                    EventTimeChecker c = new EventTimeChecker(g, toAddEvent);
                    ScheduledFuture sf = new ScheduledThreadPoolExecutor(1).schedule(c, Time.getTimeDifferenceMinutes(toAddEvent.getEventTime()), TimeUnit.MINUTES);
                    List<ScheduledFuture> tempFuture = CmdEvent.ALLSCHEDUELEDEVENTS.get(g.getId());
                    tempFuture.add(sf);
                    CmdEvent.ALLSCHEDUELEDEVENTS.put(g.getId(), tempFuture);

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
                    for (Message text : c2.getIterableHistory()) {
                        for (MessageEmbed s : text.getEmbeds()) {
                            String[] f = s.getAuthor().getName().split(" ");
                            if (Integer.parseInt(f[f.length - 1]) == id) {
                                Message m = c2.getMessageById(text.getId()).complete();
                                m.editMessage(CmdEvent.getParsedEvent(e, g, CmdEvent.getParticipants(e, g), CmdEvent.getMaybes(e, g), toAddEvent.getName() + " ID: " + CmdEvent.EVENTS.get(g).indexOf(toAddEvent)).build()).queue();
                            }
                        }
                    }
                }
            }, 20, TimeUnit.MINUTES);
        } else {
            System.out.println("CHECKER!");
            for (String s : e.getParticipants()) {
                Main.jda.getUserById(s).openPrivateChannel().complete().sendMessage(CmdEvent.getParsedEvent(e, g, CmdEvent.getParticipants(e, g), CmdEvent.getMaybes(e, g), "Das Event: " + e.getName() + " startet!").build()).queue();
            }
            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    int id = CmdEvent.EVENTS.get(g).indexOf(e);
                    CmdEvent.EVENTS.get(g).remove(id);
                    ScheduledFuture sf2 = CmdEvent.ALLSCHEDUELEDEVENTS.get(g.getId()).get(id);
                    List<ScheduledFuture> tempFuture2 = CmdEvent.ALLSCHEDUELEDEVENTS.get(g.getId());
                    tempFuture2.remove(sf2);
                    CmdEvent.ALLSCHEDUELEDEVENTS.put(g.getId(), tempFuture2);
                    CmdEvent.EVENTS.get(g).remove(e);

                    MessageChannel c3 = g.getTextChannelsByName("event", true).get(0);
                    for (Message text : c3.getIterableHistory()) {
                        for (MessageEmbed s : text.getEmbeds()) {
                            String[] f = s.getAuthor().getName().split(" ");
                            if (Integer.parseInt(f[f.length - 1]) == id) {
                                Message m = c3.getMessageById(text.getId()).complete();
                                m.delete().queue();
                            }
                        }
                    }

                    if (CmdEvent.EVENTS.get(g).size() < 1) {
                        MessageEmbed eb = new EmbedBuilder()
                                .setDescription("Es sind keine Events geplant!")
                                .build();
                        g.getTextChannelsByName("event", true).get(0).sendMessage(eb).queue();
                    }
                }
            }, 20, TimeUnit.MINUTES);
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
}
