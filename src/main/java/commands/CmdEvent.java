package commands;

import core.Main;
import core.PermissionCore;
import eventcore.EventTimeChecker;
import eventcore.Event;
import eventcore.Time;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CmdEvent implements Command {

    public static HashMap<Guild, List<Event>> EVENTS = new HashMap<>();
    public static HashMap<String, List<ScheduledFuture>> ALLSCHEDUELEDEVENTS = new HashMap<>();

    @Override
    public int permission(int permissionStage) {
        if (permissionStage >= PermissionCore.GENERAL) {
            return PermissionCore.GENERAL;
        } else {
            return PermissionCore.MITGLIED;
        }
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (event != null) {
            if (EVENTS.get(event.getGuild()) == null) {
                EVENTS.put(event.getGuild(), new ArrayList<>());
            }
            if (ALLSCHEDUELEDEVENTS.get(event.getGuild().getId()) == null) {
                ALLSCHEDUELEDEVENTS.put(event.getGuild().getId(), new ArrayList<>());
            }
        }

        if (args.length < 1) {
            if (PermissionCore.check(event, PermissionCore.LENNY)) {
                reprintAllEvents(event.getGuild());
            }
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (PermissionCore.check(event, PermissionCore.GENERAL)) {
                    String argsSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(1, args.length)));
                    List<String> content = Arrays.asList(argsSTRG.split("\\|"));
                    String name = content.get(1);
                    List<String> description = new ArrayList<>(content.subList(2, content.size()));
                    String desc = "";
                    for (String s : description) {
                        desc = desc + " " + s;
                    }
                    String[] eventTime = Time.getEventTime(content.get(0));
                    for (String t : eventTime) {
                        t.replace(" ", "");
                    }
                    Event toAddEvent = new Event(eventTime, name, desc);
                    List<Event> temp = EVENTS.get(event.getGuild());
                    temp.add(toAddEvent);
                    event.getChannel().sendMessage(getParsedEvent(toAddEvent, event.getGuild(), getParticipants(toAddEvent, event.getGuild()),
                            getMaybes(toAddEvent, event.getGuild()),
                            "Das Event: " + toAddEvent.getName() + " wurde erfolgreich erstellt!").build()).queue();
                    EVENTS.put(event.getGuild(), temp);
                    EVENTS.forEach((g, poll) -> {
                        File path = new File("SERVER_SETTINGS/" + g.getId() + "/");
                        if (!path.exists())
                            path.mkdirs();
                        try {
                            saveEvent(g);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    EventTimeChecker c = new EventTimeChecker(event.getGuild(), toAddEvent);
                    ScheduledFuture sf = Executors.newSingleThreadScheduledExecutor().schedule(c, Time.getTimeDifferenceMinutes(toAddEvent.getEventTime()), TimeUnit.MINUTES);
                    List<ScheduledFuture> tempFuture = ALLSCHEDUELEDEVENTS.get(event.getGuild().getId());
                    tempFuture.add(sf);
                    ALLSCHEDUELEDEVENTS.put(event.getGuild().getId(), tempFuture);

                    reprintAllEvents(event.getGuild());
                }
                break;
            case "delete":
                if (PermissionCore.check(event, PermissionCore.GENERAL)) {
                    String idSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(1, args.length)));
                    idSTRG.replace(" ", "");
                    int id = Integer.parseInt(idSTRG);
                    Event e = EVENTS.get(event.getGuild()).get(id);
                    EVENTS.get(event.getGuild()).remove(id);
                    ScheduledFuture sf2 = ALLSCHEDUELEDEVENTS.get(event.getGuild().getId()).get(id);
                    sf2.cancel(true);
                    List<ScheduledFuture> tempFuture2 = ALLSCHEDUELEDEVENTS.get(event.getGuild().getId());
                    tempFuture2.remove(sf2);
                    ALLSCHEDUELEDEVENTS.put(event.getGuild().getId(), tempFuture2);
                    String allParticipants = getParticipants(e, event.getGuild());
                    event.getChannel().sendMessage(getParsedEvent(e, event.getGuild(), allParticipants, getMaybes(e, event.getGuild()),
                            "Das Event: " + e.getName() + " wurde erfolgreich entfernt!").build()).queue();
                    for (String players : e.getParticipants()) {
                        Main.jda.getUserById(players).openPrivateChannel().complete().sendMessage(getParsedEvent(e, event.getGuild(), allParticipants, getMaybes(e, event.getGuild()),
                                "Das Event: " + e.getName() + " wurde gestrichen!").build()).queue();
                    }
                    try {
                        saveEvent(event.getGuild());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    if (EVENTS.get(event.getGuild()).size() < 1) {
                        try {
                            deleteAllEvents(event.getGuild());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                reprintAllEvents(event.getGuild());
                break;

            case "maybe":
                Event e = EVENTS.get(event.getGuild()).get(Integer.parseInt(args[1].replace(" ", "")));
                if (e != null){
                    event.getChannel().sendMessage(getMaybeParsed(e, getMaybes(e, event.getGuild()), "Hier die Liste der Spieler die Vielleicht können: ").build()).queue();
                }
                break;
            case "createrepeatable":
                if (PermissionCore.check(event, PermissionCore.GENERAL)) {
                    String argsSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(1, args.length)));
                    List<String> content = Arrays.asList(argsSTRG.split("\\|"));
                    String name = content.get(1);
                    List<String> description = new ArrayList<>(content.subList(2, content.size()));
                    String desc = "";
                    for (String s : description) {
                        desc = desc + " " + s;
                    }
                    String[] eventTime = Time.getEventTime(content.get(0));
                    for (String t : eventTime) {
                        t.replace(" ", "");
                    }
                    Event toAddEvent = new Event(eventTime, name, desc, true);
                    List<Event> temp = EVENTS.get(event.getGuild());
                    temp.add(toAddEvent);
                    event.getChannel().sendMessage(getParsedEvent(toAddEvent, event.getGuild(), getParticipants(toAddEvent, event.getGuild()), getMaybes(toAddEvent, event.getGuild()),
                            "Das Event: " + toAddEvent.getName() + " wurde erfolgreich erstellt!").build()).queue();
                    this.EVENTS.put(event.getGuild(), temp);
                    EVENTS.forEach((g, poll) -> {
                        File path = new File("SERVER_SETTINGS/" + g.getId() + "/");
                        if (!path.exists())
                            path.mkdirs();
                        try {
                            saveEvent(g);
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    });
                    EventTimeChecker c3 = new EventTimeChecker(event.getGuild(), toAddEvent);
                    ScheduledFuture sf = Executors.newSingleThreadScheduledExecutor().schedule(c3, Time.getTimeDifferenceMinutes(toAddEvent.getEventTime()), TimeUnit.MINUTES);
                    List<ScheduledFuture> tempFuture = ALLSCHEDUELEDEVENTS.get(event.getGuild().getId());
                    tempFuture.add(sf);
                    ALLSCHEDUELEDEVENTS.put(event.getGuild().getId(), tempFuture);
                    reprintAllEvents(event.getGuild());
                }
                break;
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

    @Override
    public String help(int permissionStage) {
        if (permissionStage >= PermissionCore.GENERAL) {
            return "Mit /event create 02:04:2018:20:08|<Titel>|<Beschreibung> erstellst du ein Event am 02.04.2018 um 20 Uhr und 08 Minuten! \n" +
                    "Mit /event createRepeatable 02:04:2018:20:08|<Titel>|<Beschreibung> erstellst du ein Event am 02.04.2018 um 20 Uhr und 08 Minuten, das sich wöchentlich wiederhohlt! \n" +
                    "Mit /event delete <ID> löscht du ein erstelltes Event! \n" +
                    "Drücke auf ✅ im #event Kanal um beizutreten. \n" +
                    "Drücke auf ⛔ im #event Kanal um das Event, egal für was du dich eingetragen hast, zu verlassen! \n" +
                    "Drücke auf ❓ im #event Kanal um zu sagen ds du vielleicht kannst.";
        } else if (permissionStage >= PermissionCore.MITGLIED) {
            return "Drücke auf ✅ im #event Kanal um beizutreten. \n" +
                    "Drücke auf ⛔ im #event Kanal um das Event, egal für was du dich eingetragen hast, zu verlassen! \n" +
                    "Drücke auf ❓ im #event Kanal um zu sagen ds du vielleicht kannst.";
        } else {
            return null;
        }
    }

    public static void loadEvents(JDA jda) {
        jda.getGuilds().forEach(g -> {

            File f = new File("SERVER_SETTINGS/" + g.getId() + "/eventList.dat");
            if (f.exists())
                try {
                    EVENTS.put(g, getEvent(g));
                    for (Event e : EVENTS.get(g)) {
                        EventTimeChecker c = new EventTimeChecker(g, e);
                        ScheduledFuture sf = Executors.newSingleThreadScheduledExecutor().schedule(c, Time.getTimeDifferenceMinutes(e.getEventTime()), TimeUnit.MINUTES);
                        int i = 0;

                        if (ALLSCHEDUELEDEVENTS.get(g.getId()) == null) {
                            ALLSCHEDUELEDEVENTS.put(g.getId(), new ArrayList<>());
                        }
                        List<ScheduledFuture> tempFuture = ALLSCHEDUELEDEVENTS.get(g.getId());
                        tempFuture.add(sf);
                        ALLSCHEDUELEDEVENTS.put(g.getId(), tempFuture);
                        i = i + 1;
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
        });

    }

    public static void deleteAllEvents(Guild guild) throws IOException {
        String saveFile = "SERVER_SETTINGS/" + guild.getId() + "/eventList.dat";
        File f = new File(saveFile);
        f.delete();
    }

    public static void saveEvent(Guild guild) throws IOException {

        if (!EVENTS.containsKey(guild))
            return;

        String saveFile = "SERVER_SETTINGS/" + guild.getId() + "/eventList.dat";
        List<Event> e = EVENTS.get(guild);
        FileOutputStream fos = new FileOutputStream(saveFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(e);
        oos.close();
        fos.close();
    }

    private static List<Event> getEvent(Guild guild) throws IOException, ClassNotFoundException {

        if (CmdEvent.EVENTS.containsKey(guild))
            return null;

        String saveFile = "SERVER_SETTINGS/" + guild.getId() + "/eventList.dat";

        FileInputStream fis = new FileInputStream(saveFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<Event> out = (List<Event>) ois.readObject();
        ois.close();
        fis.close();
        return out;
    }

    public static void addMemberToEvent(int id, User author, Guild guild, String messageID) {
        if (PermissionCore.check(author, guild, PermissionCore.MITGLIED)) {
            Event e = EVENTS.get(guild).get(id);
            String allParticipants = getParticipants(e, guild);
            if (e.getParticipants().size() >= 6) {
                author.openPrivateChannel().complete().sendMessage(getParsedEvent(e, guild, allParticipants, getMaybes(e, guild),
                        "Du kanst dem Event: " + e.getName() + " nich beigetreten. Es ist schon voll!").build()).queue();
                e.getMaybes().add(author.getId());
            } else {
                List names = new ArrayList();
                for (String s : e.getParticipants()) {
                    names.add(s);
                }
                if (!(names.contains(author.getId()))) {
                    EVENTS.get(guild).get(id).addParticipant(author.getId());
                    allParticipants = getParticipants(e, guild);
                    author.openPrivateChannel().complete().sendMessage(getParsedEvent(e, guild, allParticipants, getMaybes(e, guild),
                            "Du bist dem Event: " + e.getName() + " erfolgreich beigetreten!").build()).queue();
                    if (e.getParticipants().size() >= 6) {
                        for (String s2 : e.getParticipants()) {
                            Main.jda.getUserById(s2).openPrivateChannel().complete().sendMessage(getParsedEvent(e, guild, allParticipants, getMaybes(e, guild),
                                    "Das Event: " + e.getName() + " ist voll besetzt!").build()).queue();
                        }
                    }
                } else {
                    String[] fehlermeldungen = {" Ich weiß du bist dick aber du bist nich soo dick das du gleich 2 Plätze brauchst.",
                            " So wie dein Arsch auch nur ne Sicherungskopie von deinem Gesicht ist, ist es dieser Versuch dich erneut einzutragen.",
                            " Aber du weißt schon das du SCHON dabei bist ??.",
                            " Du Scherzkecks bist schon dabei!"};
                    Random r = new Random();
                    int rando = (r.nextInt(fehlermeldungen.length - 1) + 0);
                    author.openPrivateChannel().queue((privateChannel -> {
                        privateChannel.sendMessage(author.getAsMention() + fehlermeldungen[rando]
                        ).queue();
                    }));
                }
            }
            try {
                saveEvent(guild);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            MessageChannel c = guild.getTextChannelsByName("event", true).get(0);
            Message m = c.getMessageById(messageID).complete();
            m.editMessage(getParsedEvent(e, guild, allParticipants, getMaybes(e, guild), e.getName() + " ID: " + EVENTS.get(guild).indexOf(e)).build()).queue();
        }
    }

    public static void removeMemberToEvent(int i, User user, Guild guild, String messageId) {
        Event e = EVENTS.get(guild).get(i);
            if (e.getParticipants().contains(user.getId()) || e.getMaybes().contains(user.getId())) {
                e.removeParticipant(user.getId());
                e.getMaybes().remove(user.getId());
                String allParticipants2 = getParticipants(e, guild);
                user.openPrivateChannel().complete().sendMessage(getParsedEvent(e, guild, allParticipants2, getMaybes(e, guild), "Du hast das Event: " + e.getName() + " erfolgreich verlassen!").build()).queue();
                for (String s : e.getParticipants()) {
                    Main.jda.getUserById(s).openPrivateChannel().complete().sendMessage(user.getAsMention() + " Hat das Event verlassen.").queue();
                }
                try {
                    saveEvent(guild);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                MessageChannel c = guild.getTextChannelsByName("event", true).get(0);
                String allParticipants = getParticipants(e, guild);
                Message m = c.getMessageById(messageId).complete();
                m.editMessage(getParsedEvent(e, guild, allParticipants, getMaybes(e, guild), e.getName() + " ID: " + EVENTS.get(guild).indexOf(e)).build()).queue();
            } else {
                System.out.println(user.getName() + " hat versucht etwas zu verlassen das er nie betreten hat.");
            }
    }

    public static void addMaybeToEvent(int id, User author, Guild guild, String messageId) {
        if (PermissionCore.check(author, guild, PermissionCore.MITGLIED)) {
            Event e = EVENTS.get(guild).get(id);
            String allParticipants2 = getParticipants(e, guild);
            if (e.getParticipants().size() >= 6) {
                author.openPrivateChannel().complete().sendMessage(getParsedEvent(e, guild, allParticipants2, getMaybes(e, guild),
                        "Du kanst dem Event: " + e.getName() + " nich beigetreten. Es ist schon voll! Du wurdest aber auf die Warteliste gesetzt!").build()).queue();
                List names = new ArrayList();
                for (String s : e.getParticipants()) {
                    names.add(s);
                }
                List names2 = new ArrayList();
                for (String s : e.getMaybes()) {
                    names2.add(s);
                }
                if (!(names.contains(author.getId())) || !(names2.contains(author.getId()))) {
                    e.addMaybeParticipant(author.getId());
                }
            }
            try {
                saveEvent(guild);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static EmbedBuilder getParsedEvent(Event e, Guild g, String allParticipants, String maybes, String Author) {
        return new EmbedBuilder()
                .setAuthor(Author)
                .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                .setDescription(e.getDescription())
                .addField("","Spieler die Mitmachen(" + e.getParticipants().size() + ") : " + allParticipants, true)
                .addField("","Spieler die vielleicht Mitmachen(" + e.getMaybes().size() + ") : " + maybes, true)
                .setFooter("Wöchentlich : " + e.isRepeatable() + "", null);
    }

    public static void reprintAllEvents(Guild guild) {
        MessageChannel c = guild.getTextChannelsByName("event", true).get(0);
        for (Message m : c.getIterableHistory()) {
            m.delete().queue();
        }
        for (Event e : EVENTS.get(guild)) {
            String id = c.sendMessage(getParsedEvent(e, guild, getParticipants(e, guild), getMaybes(e, guild),
                    e.getName() + " ID: " + EVENTS.get(guild).indexOf(e)).build()).complete().getId();
            c.addReactionById(id, "✅").queue();
            c.addReactionById(id, "⛔").queue();
            c.addReactionById(id, "❓").queue();
        }
        if (EVENTS.get(guild).size() < 1) {
            MessageEmbed eb = new EmbedBuilder()
                    .setDescription("Es sind keine Events geplant!")
                    .build();
            guild.getTextChannelsByName("event", true).get(0).sendMessage(eb).queue();
        }
    }

    public static String getParticipants(Event e, Guild guild) {
        String allParticipants = "";
        if (e.getParticipants().size() <= 0) {
            allParticipants = "Niemand!";
        } else {
            for (String player : e.getParticipants()) {
                User p = Main.jda.getUserById(player);
                if (p != null) {
                    if (guild.getMember(p).getNickname() != null) {
                        allParticipants = allParticipants + ", " + guild.getMember(p).getNickname();
                    } else {
                        allParticipants = allParticipants + ", " + p.getName();
                    }
                } else {
                    e.removeParticipant(player);
                    System.out.println("Removed Person with id: " + player + " from guild: " + guild.getName());
                    try {
                        saveEvent(guild);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return allParticipants;
    }

    public static String getMaybes(Event e, Guild guild) {
        String allParticipants = "";
        if (e.getMaybes().size() <= 0) {
            allParticipants = "Niemand!";
        } else {
            for (String player : e.getMaybes()) {
                User p = Main.jda.getUserById(player);
                if (p != null) {
                    if (guild.getMember(p).getNickname() != null) {
                        allParticipants = allParticipants + ", " + guild.getMember(p).getNickname();
                    } else {
                        allParticipants = allParticipants + ", " + p.getName();
                    }
                } else {
                    e.removeParticipant(player);
                    System.out.println("Removed Person with id: " + player + " from guild: " + guild.getName());
                    try {
                        saveEvent(guild);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return allParticipants;
    }

    public EmbedBuilder getMaybeParsed(Event e, String maybes, String Author) {
        return new EmbedBuilder()
                .setAuthor(Author)
                .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                .setDescription(e.getDescription())
                .addField("","Spieler die Mitmachen(" + e.getParticipants().size() + ") : " + maybes, true)
                .setFooter("Wöchentlich : " + e.isRepeatable() + "", null);
    }
}
