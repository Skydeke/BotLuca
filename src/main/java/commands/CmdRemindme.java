package commands;

import core.PermissionCore;
import eventcore.Time;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import remindercore.Reminder;
import remindercore.ReminderTimeChecker;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CmdRemindme implements ICommand {

    public static HashMap<String, HashMap<String, List<Reminder>>> ALLREMINDS = new HashMap<>();
    public static HashMap<String, List<ScheduledFuture>> ALLUSERSSCHEDUELEDREMINDERS = new HashMap<>();

    @Override
    public int permission(int permissionStage) {
        return PermissionCore.VETERAN;
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
       if (PermissionCore.check(event, PermissionCore.VETERAN)){
           if (ALLREMINDS.get(event.getGuild().getId()) == null){
               this.ALLREMINDS.put(event.getGuild().getId(), new HashMap<>());
           }
           if (ALLREMINDS.get(event.getGuild().getId()).get(event.getAuthor().getId()) == null){
               this.ALLREMINDS.get(event.getGuild().getId()).put(event.getAuthor().getId(), new ArrayList<>());
           }
           if(ALLUSERSSCHEDUELEDREMINDERS.get(event.getAuthor().getId()) == null){
               ALLUSERSSCHEDUELEDREMINDERS.put(event.getAuthor().getId(), new ArrayList<>());
           }

           String argsSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(0, args.length)));
           List<String> arg = Arrays.asList(argsSTRG.split(" "));
           switch (arg.get(0).toLowerCase().replace(" ", "")){

               case "show":
                   List<Reminder> getAllRemindersOfUser = ALLREMINDS.get(event.getGuild().getId()).get(event.getAuthor().getId());
                   for (Reminder r : getAllRemindersOfUser) {
                       MessageEmbed eb = new EmbedBuilder()
                               .setAuthor("An das werde ich dich erinnern:")
                               .setTitle("Datum: " + Time.formatDate(r.getEventTime()) + " Zeit: " + Time.formatTime(r.getEventTime()))
                               .setDescription(r.getDesc())
                               .build();
                       event.getTextChannel().sendMessage(eb).queue();
                   }
                   if (getAllRemindersOfUser.size() < 1){
                       MessageEmbed eb = new EmbedBuilder()
                               .setAuthor("Ich soll dich an nichts erinnern!").build();
                       event.getTextChannel().sendMessage(eb).queue();
                   }
                   break;

               case "delete":
                   List<Reminder> getAllRemindersOfUser1 = ALLREMINDS.get(event.getGuild().getId()).get(event.getAuthor().getId());
                   String[] eventAtTimeToDelete = Time.getEventTime(arg.get(1));
                   new ScheduledThreadPoolExecutor(0).schedule(new Runnable() {
                       @Override
                       public void run() {
                           for (Reminder r : ALLREMINDS.get(event.getGuild().getId()).get(event.getAuthor().getId())) {
                               if (Arrays.equals(r.getEventTime(), eventAtTimeToDelete)){
                                   ALLUSERSSCHEDUELEDREMINDERS.get(event.getAuthor().getId()).get(getAllRemindersOfUser1.indexOf(r)).cancel(true);
                                   getAllRemindersOfUser1.remove(r);
                                   HashMap<String, List<Reminder>> tempMap = ALLREMINDS.get(event.getGuild().getId());
                                   tempMap.put(event.getAuthor().getId(), getAllRemindersOfUser1);
                                   ALLREMINDS.put(event.getGuild().getId(), tempMap);
                                   MessageEmbed eb = new EmbedBuilder()
                                           .setAuthor("Die Erinnerung wurde erfolgreich gelöscht!")
                                           .setTitle("Datum: " + Time.formatDate(eventAtTimeToDelete) + " Zeit: " + Time.formatTime(eventAtTimeToDelete))
                                           .setDescription(r.getDesc())
                                           .build();
                                   try {
                                       saveReminds(event.getGuild());
                                   } catch (IOException e) {
                                       e.printStackTrace();
                                   }
                                   event.getTextChannel().sendMessage(eb).queue();
                               }
                           }
                       }
                   }, 4, TimeUnit.SECONDS);
                   break;

               default:
                   List<Reminder> getAllRemindersOfUser2 = ALLREMINDS.get(event.getGuild().getId()).get(event.getAuthor().getId());
                   List<String> content = Arrays.asList(argsSTRG.split("\\|"));
                   List<String> description = new ArrayList<>(content.subList(1, content.size()));
                   String desc = "";
                   for (String s: description){
                       desc = desc + " " + s;
                   }
                   String[] eventTime = Time.getEventTime(content.get(0));
                   Reminder r = new Reminder(desc, eventTime, event.getGuild().getId(), event.getAuthor().getId());
                   ScheduledFuture sf = Executors.newSingleThreadScheduledExecutor().schedule(new ReminderTimeChecker(event.getAuthor().getId(), r), Time.getTimeDifferenceMinutes(r.getEventTime()), TimeUnit.MINUTES);
                   List<ScheduledFuture> tempFuture = ALLUSERSSCHEDUELEDREMINDERS.get(event.getAuthor().getId());
                   tempFuture.add(sf);
                   ALLUSERSSCHEDUELEDREMINDERS.put(event.getAuthor().getId(), tempFuture);
                   getAllRemindersOfUser2.add(r);
                   HashMap<String, List<Reminder>> tempMap = ALLREMINDS.get(event.getGuild().getId());
                   tempMap.put(event.getAuthor().getId(), getAllRemindersOfUser2);
                   ALLREMINDS.put(event.getGuild().getId(), tempMap);
                   MessageEmbed eb2 = new EmbedBuilder()
                           .setAuthor("Die Erinnerung wurde erfolgreich erstellt!")
                           .setTitle("Datum: " + Time.formatDate(eventTime) + " Zeit: " + Time.formatTime(eventTime))
                           .setDescription(desc)
                           .build();
                   event.getTextChannel().sendMessage(eb2).queue();
                   ALLREMINDS.forEach((g, poll) -> {
                       File path = new File("SERVER_SETTINGS/" + g + "/");
                       if (!path.exists())
                           path.mkdirs();
                       try {
                           saveReminds(event.getGuild());
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   });
                   break;
           }
       }
    }

    public static void loadReminds(JDA jda) {
        jda.getGuilds().forEach(g -> {


            File f = new File("SERVER_SETTINGS/" + g.getId() + "/remindList.dat");
            if (f.exists())
                try {
                    ALLREMINDS.put(g.getId(), getRemind(g));
                    for (String e : ALLREMINDS.get(g.getId()).keySet()){
                        int i = 0;
                        while (i < ALLREMINDS.get(g.getId()).get(e).size()) {
                            Reminder r = ALLREMINDS.get(g.getId()).get(e).get(i);
                            ScheduledFuture sf = Executors.newSingleThreadScheduledExecutor().schedule(new ReminderTimeChecker(e, r), Time.getTimeDifferenceMinutes(r.getEventTime()), TimeUnit.MINUTES);
                            if (ALLUSERSSCHEDUELEDREMINDERS.get(r.getUser()) == null){
                                ALLUSERSSCHEDUELEDREMINDERS.put(r.getUser(), new ArrayList<>());
                            }
                            List<ScheduledFuture> tempFuture = ALLUSERSSCHEDUELEDREMINDERS.get(r.getUser());
                            tempFuture.add(sf);
                            ALLUSERSSCHEDUELEDREMINDERS.put(r.getUser(), tempFuture);
                            i = i + 1;
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
        });

    }

    public static void deleteAllEvents(Guild guild) throws IOException {
        String saveFile = "SERVER_SETTINGS/" + guild.getId() + "/remindList.dat";
        File f = new File(saveFile);
        f.delete();
    }

    public static void saveReminds(Guild guild) throws IOException {

        if (!ALLREMINDS.containsKey(guild.getId()))
            return;
        String saveFile = "SERVER_SETTINGS/" + guild.getId() + "/remindList.dat";
        HashMap<String, List<Reminder>> e = ALLREMINDS.get(guild.getId());
        FileOutputStream fos = new FileOutputStream(saveFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(e);
        oos.close();
        fos.close();
    }

    private static HashMap<String, List<Reminder>> getRemind(Guild guild) throws IOException, ClassNotFoundException {

        if (ALLREMINDS.containsKey(guild.getId()))
            return null;

        String saveFile = "SERVER_SETTINGS/" + guild.getId() + "/remindList.dat";

        FileInputStream fis = new FileInputStream(saveFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        HashMap<String, List<Reminder>> out = (HashMap<String, List<Reminder>>) ois.readObject();
        ois.close();
        fis.close();
        return out;
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    @Override
    public String help(int permissionStage) {
        if (permissionStage >= PermissionCore.VETERAN) {
            return  "**Reminders:**\n" +
                    ":white_small_square: Nutze `/remindme 06:04:2018:14:52|Einkaufen` um dich am 06.04.18 um 14 Uhr 52 ans Einkaufen zu erinnern. \n" +
                    ":white_small_square: Nutze `/remindme delete 06:04:2018:14:52` um die Erinnerung am 06.04.18 um 14 Uhr 52 zu löschen. \n" +
                    ":white_small_square: Nutze `/remindme show` um dir alle deine Erinnerungen zu zeigen.";
        }else {
            return "";
        }
    }
}
