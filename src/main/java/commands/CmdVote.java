package commands;

import core.PermissionCore;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CmdVote implements Command, Serializable {

    private static TextChannel CHANNEL;
    private static HashMap<Guild, Poll> POLLS = new HashMap<>();
    //The Emotes of numbers in Discord
    private static final String[] EMOTI = {":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:", ":keycap_ten:"};

    @Override
    public int permission(int permissionStage) {
        if (permissionStage < PermissionCore.GENERAL){
            return PermissionCore.GENERAL;
        }
        return PermissionCore.MITGLIED;
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
            CHANNEL = event.getTextChannel();

            switch (args[0]) {

                case "create":
                    if (PermissionCore.check(event, PermissionCore.GENERAL)){
                        createPoll(args, event);
                    }
                    break;

                case "v":
                    if (PermissionCore.check(event, PermissionCore.MITGLIED)) {
                        votePoll(args, event);
                    }
                    break;

                case "stats":
                    if (PermissionCore.check(event, PermissionCore.MITGLIED)) {
                        voteStats(event);
                    }
                    break;

                case "close":
                    if (PermissionCore.check(event, PermissionCore.GENERAL)) {
                        try {
                            closeVote(event);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }

            POLLS.forEach((g, poll) -> {

                File path = new File("SERVER_SETTINGS/" + g.getId() + "/");
                if (!path.exists())
                    path.mkdirs();

                try {
                    savePoll(g);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }

    private void voteStats(MessageReceivedEvent event) {

        if (!POLLS.containsKey(event.getGuild())) {
            message("Es gibt keine aktive Abstimmung!", Color.red);
            return;
        }
        CHANNEL.sendMessage(getParsedPoll(POLLS.get(event.getGuild()), event.getGuild()).build()).queue();

    }

    private void closeVote(MessageReceivedEvent event) throws IOException {

        if (!POLLS.containsKey(event.getGuild())) {
            message("Es gibt geraden nichts abzustimmen!", Color.red);
            return;
        }

        Guild g = event.getGuild();
        Poll poll = POLLS.get(g);

        if (!poll.getCreator(g).equals(event.getMember())) {
            message("Nur der Ersteller (" + poll.getCreator(g).getAsMention() + ") kann diese Abstimmung beenden!", Color.red);
            return;
        }

        POLLS.remove(g);
        deletePoll(g);
        CHANNEL.sendMessage(getParsedPoll(poll, event.getGuild()).build()).queue();
        EmbedBuilder eb = new EmbedBuilder().setDescription("Abstimmung beendet von " + event.getAuthor().getAsMention() + ".").setColor(new Color(0xFF7000));
        CHANNEL.sendMessage(eb.build()).queue();
    }

    private void savePoll(Guild guild) throws IOException {

        if (!POLLS.containsKey(guild))
            return;

        String saveFile = "SERVER_SETTINGS/" + guild.getId() + "/vote.dat";
        Poll poll = POLLS.get(guild);

        FileOutputStream fos = new FileOutputStream(saveFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(poll);
        oos.close();
    }

    private void deletePoll(Guild guild) throws IOException {
        String saveFile = "SERVER_SETTINGS/" + guild.getId() + "/vote.dat";
        File f = new File(saveFile);
        f.delete();
    }

    private static Poll getPoll(Guild guild) throws IOException, ClassNotFoundException {

        if (POLLS.containsKey(guild))
            return null;

        String saveFile = "SERVER_SETTINGS/" + guild.getId() + "/vote.dat";

        FileInputStream fis = new FileInputStream(saveFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Poll out = (Poll) ois.readObject();
        ois.close();
        return out;
    }

    public static void loadPolls(JDA jda) {

        jda.getGuilds().forEach(g -> {

            File f = new File("SERVER_SETTINGS/" + g.getId() + "/vote.dat");
            if (f.exists())
                try {
                    POLLS.put(g, getPoll(g));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

        });

    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    @Override
    public String help(int permissionStage) {
        if (permissionStage >= PermissionCore.GENERAL) {
            return "Nutze */vote create <Frage> |Antwort1|Antwort2* um eine Abstimmung zu starten. \n" +
                    "Nutze */vote v <antwort>* um abzustimmen. \n" +
                    "Nutze */vote stats* um den Fortschritt der Abstimmung zu sehen. \n" +
                    "Nutze */vote close* um eine Abstimmung zu beenden.";
        }
        return "Nutze */vote v <antwort>* um abzustimmen. \n" +
                    "Nutze */vote stats* um den Fortschritt der Abstimmung zu sehen.";
    }

    private static void message(String content, Color color) {
        EmbedBuilder eb = new EmbedBuilder().setDescription(content).setColor(color);
        CHANNEL.sendMessage(eb.build()).queue();
    }

    private EmbedBuilder getParsedPoll(Poll poll, Guild guild) {

        StringBuilder ansSTR = new StringBuilder();
        final AtomicInteger count = new AtomicInteger();

        poll.answers.forEach(s -> {
            long votescount = poll.votes.keySet().stream().filter(k -> poll.votes.get(k).equals(count.get() + 1)).count();
            ansSTR.append(EMOTI[count.get()] + "  -  " + s + "  -  Votes: `" + votescount + "` \n");
            count.addAndGet(1);
        });

        return new EmbedBuilder()
                .setAuthor(poll.getCreator(guild).getEffectiveName() + "'s Abstimmung:", null, poll.getCreator(guild).getUser().getAvatarUrl())
                .setDescription(":pencil:   " + poll.heading + "\n\n" + ansSTR.toString())
                .setFooter("Benutze '" + "/" + "vote v <number>' um abzustimmen!", null)
                .setColor(Color.cyan);

    }


    private void createPoll(String[] args, MessageReceivedEvent event) {

        if (POLLS.containsKey(event.getGuild())) {
            message("Auf diesem Server gibt es schon eine laufende Abstimmung!", Color.red);
            return;
        }

        String argsSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(1, args.length)));
        List<String> content = Arrays.asList(argsSTRG.split("\\|"));
        String heading = content.get(0);
        List<String> answers = new ArrayList<>(content.subList(1, content.size()));

        Poll poll = new Poll(event.getMember(), heading, answers, CHANNEL);
        POLLS.put(event.getGuild(), poll);
        CHANNEL.sendMessage(getParsedPoll(poll, event.getGuild()).build()).queue();
    }

    private void votePoll(String[] args, MessageReceivedEvent event) {

        if (!POLLS.containsKey(event.getGuild())) {
            message(event.getMessage().getAuthor().getAsMention() + " Es gibt gerade nichts abzustimmen!", Color.red);
            event.getMessage().delete().queue();
            return;
        }

        Poll poll = POLLS.get(event.getGuild());

        int vote;
        try {
            vote = Integer.parseInt(args[1]);
            if (vote > poll.answers.size())
                throw new Exception();
        } catch (Exception e) {
            message(event.getMessage().getAuthor().getAsMention() + " Bitte wähle eine Antwort die es auch gibt!", Color.red);
            event.getMessage().delete().queue();
            return;
        }

        if (poll.votes.containsKey(event.getAuthor().getId())) {
            message(event.getMessage().getAuthor().getAsMention() + " Du darfst nur **einmal** abstimmen!", Color.red);
            event.getMessage().delete().queue();
            return;
        }
        if (event.getGuild().getTextChannelsByName(poll.tc, true).get(0).getName().equals(CHANNEL.getName())) {
            poll.votes.put(event.getAuthor().getId(), vote);
            POLLS.replace(event.getGuild(), poll);
        }else {
            message(event.getMessage().getAuthor().getAsMention() + " Es gibt gerade nichts abzustimmen in diesem Textkanal!", Color.red);
            return;
        }
        event.getMessage().delete().queue();

    }

    private class Poll implements Serializable {

        private String creator;
        private String heading;
        private List<String> answers;
        private HashMap<String, Integer> votes;
        private String tc;

        private Poll(Member creator, String heading, List<String> answers, TextChannel creationChannel) {
            this.creator = creator.getUser().getId();
            this.heading = heading;
            this.answers = answers;
            this.votes = new HashMap<>();
            this.tc = creationChannel.getId();
        }

        private Member getCreator(Guild guild) {
            return guild.getMember(guild.getJDA().getUserById(creator));
        }

    }
}
