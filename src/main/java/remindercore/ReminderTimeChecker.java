package remindercore;

import commands.CmdRemindme;
import core.Main;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ReminderTimeChecker implements Runnable{

    private String e;
    private Reminder r;

    public ReminderTimeChecker(String event, Reminder r){
        this.e = event;
        this.r = r;
    }

    @Override
    public void run() {
        System.out.println("ERINNERUNG! " + r.getGuild());
        Main.jda.getUserById(e).openPrivateChannel().queue((channel) -> {
            MessageEmbed eb = new EmbedBuilder()
                    .setAuthor("Du sagtest ich soll dich daran erinnern: ")
                    .setDescription(r.getDesc())
                    .build();
            channel.sendMessage(eb).queue();
        });

        List<Reminder> allReminderOfUser = CmdRemindme.ALLREMINDS.get(r.getGuild()).get(e);
        HashMap<String, List<Reminder>> tempMap = CmdRemindme.ALLREMINDS.get(r.getGuild());

        allReminderOfUser.remove(r);
        tempMap.put(e, allReminderOfUser);
        CmdRemindme.ALLREMINDS.put(r.getGuild(), tempMap);
        try {
            CmdRemindme.saveReminds(Main.jda.getGuildById(r.getGuild()));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (CmdRemindme.ALLREMINDS.get(r.getGuild()).size() < 1){
            try {
                CmdRemindme.deleteAllEvents(Main.jda.getGuildById(r.getGuild()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
