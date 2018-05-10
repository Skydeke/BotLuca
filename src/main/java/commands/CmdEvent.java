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
import net.dv8tion.jda.core.requests.RestAction;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CmdEvent implements Command {

    public static HashMap<Guild, List<Event>> EVENTS = new HashMap<>();
    public static HashMap<String, List<ScheduledFuture>> ALLUSERSSCHEDUELEDEVENTS = new HashMap<>();

    @Override
    public int permission(int permissionStage) {
        if (permissionStage >= PermissionCore.GENERAL){
            return  PermissionCore.GENERAL;
        }else{
            return PermissionCore.NEULING;
        }
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (event != null){
            if (EVENTS.get(event.getGuild()) == null) {
                this.EVENTS.put(event.getGuild(), new ArrayList<>());
            }
            if (ALLUSERSSCHEDUELEDEVENTS.get(event.getGuild().getId()) == null) {
                this.ALLUSERSSCHEDUELEDEVENTS.put(event.getGuild().getId(), new ArrayList<>());
            }
        }

        if (args.length < 1) {
            if (PermissionCore.check(event, PermissionCore.LENNY)) {
                MessageChannel c = event.getGuild().getTextChannelsByName("event", true).get(0);
                for (Message m : c.getIterableHistory()) {
                    m.delete().queue();
                }
                for (Event e : EVENTS.get(event.getGuild())) {
                    String allParticipants = "";
                    String maybeParticipants = "";
                    if (e.getParticipants().size() <= 0) {
                        allParticipants = "Niemand!";
                        maybeParticipants = "Niemand!";
                    } else {
                        for (String player : e.getParticipants()) {
                            User p = Main.jda.getUserById(player);
                            if (p != null){
                                allParticipants = allParticipants + " " + p.getName();
                            }else{
                                e.removeParticipant(player);
                                System.out.println("Removed Person with id: " + player + " from guild: " + event.getGuild().getName());
                                try {
                                    saveEvent(event.getGuild());
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        for (String p: e.getMaybes()){
                            maybeParticipants = maybeParticipants + " " + Main.jda.getUserById(p).getName();
                        }
                    }
                    MessageEmbed eb = new EmbedBuilder()
                            .setAuthor(e.getName() + " ID: " + EVENTS.get(event.getGuild()).indexOf(e))
                            .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                            .setDescription(e.getDescription())
                            .addField("","Wöchentlich : " + e.isRepeatable() + "", true)
                            .setFooter("Spieler die Mitmachen: " + allParticipants, null)
                            //.addField("Maybes","Spieler die Vielleicht können: " + maybeParticipants, true)
                            .build();
                    String id = c.sendMessage(eb).complete().getId();
                    c.addReactionById( id, "✅").queue();
                    c.addReactionById( id, "⛔").queue();
                    c.addReactionById( id, "❓").queue();
                }
                if (EVENTS.get(event.getGuild()).size() < 1) {
                    MessageEmbed eb = new EmbedBuilder()
                            .setDescription("Es sind keine Events geplant!")
                            .build();
                    event.getGuild().getTextChannelsByName("event", true).get(0).sendMessage(eb).queue();
                }
                return;
            }
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (PermissionCore.check(event, PermissionCore.GENERAL)){
                    String argsSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(1, args.length)));
                    List<String> content = Arrays.asList(argsSTRG.split("\\|"));
                    String name = content.get(1);
                    List<String> description = new ArrayList<>(content.subList(2, content.size()));
                    String desc = "";
                    for (String s : description) {
                        desc = desc + " " + s;
                    }
                    String[] eventTime = Time.getEventTime(content.get(0));
                    for (String t: eventTime){
                        t.replace(" ", "");
                    }
                    Event toAddEvent = new Event(eventTime, name, desc);
                    List<Event> temp = EVENTS.get(event.getGuild());
                    temp.add(toAddEvent);
                    MessageEmbed eb = new EmbedBuilder()
                            .setAuthor("Das Event: " + toAddEvent.getName() + " wurde erfolgreich erstellt!")
                            .setTitle("Datum: " + Time.formatDate(toAddEvent.getEventTime()) + " Zeit: " + Time.formatTime(toAddEvent.getEventTime()))
                            .setDescription(toAddEvent.getDescription())
                            .build();
                    String id = event.getChannel().sendMessage(eb).complete().getId();
                    this.EVENTS.put(event.getGuild(), temp);
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
                    List<ScheduledFuture> tempFuture = ALLUSERSSCHEDUELEDEVENTS.get(event.getGuild().getId());
                    tempFuture.add(sf);
                    ALLUSERSSCHEDUELEDEVENTS.put(event.getGuild().getId(), tempFuture);

                    MessageChannel c2 = event.getGuild().getTextChannelsByName("event", true).get(0);
                    for (Message m : c2.getIterableHistory()) {
                        m.delete().queue();
                    }
                    for (Event e : EVENTS.get(event.getGuild())) {
                        String allParticipants = "";
                        String maybeParticipants = "";
                        if (e.getParticipants().size() <= 0) {
                            allParticipants = "Niemand!";
                            maybeParticipants = "Niemand!";
                        } else {
                            for (String player : e.getParticipants()) {
                                allParticipants = allParticipants + "  " + Main.jda.getUserById(player).getName();
                            }
                            for (String p: e.getMaybes()){
                                maybeParticipants = maybeParticipants + " " + Main.jda.getUserById(p).getName();
                            }
                        }
                        MessageEmbed eb3 = new EmbedBuilder()
                                .setAuthor(e.getName() + " ID: " + EVENTS.get(event.getGuild()).indexOf(e))
                                .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                                .setDescription(e.getDescription())
                                .addField("","Wöchentlich : " + e.isRepeatable() + "", true)
                                .setFooter("Spieler die Mitmachen: " + allParticipants, null)
                                //.addField("Maybes","Spieler die Vielleicht können: " + maybeParticipants, true)
                                .build();
                        String id3 = c2.sendMessage(eb3).complete().getId();
                        c2.addReactionById( id3, "✅").queue();
                        c2.addReactionById( id3, "⛔").queue();
                        c2.addReactionById( id3, "❓").queue();
                    }
                }
                break;
            case "delete":
                if (PermissionCore.check(event, PermissionCore.GENERAL)) {
                    String idSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(1, args.length)));
                    idSTRG.replace(" ", "");
                    int id = Integer.parseInt(idSTRG);
                    Event e = EVENTS.get(event.getGuild()).get(id);
                    EVENTS.get(event.getGuild()).remove(id);
                    ScheduledFuture sf2 = ALLUSERSSCHEDUELEDEVENTS.get(event.getGuild().getId()).get(id);
                    sf2.cancel(true);
                    List<ScheduledFuture> tempFuture2 = ALLUSERSSCHEDUELEDEVENTS.get(event.getGuild().getId());
                    tempFuture2.remove(sf2);
                    ALLUSERSSCHEDUELEDEVENTS.put(event.getGuild().getId(), tempFuture2);


                    String allParticipants = "";
                    if (e.getParticipants().size() <= 0) {
                        allParticipants = "Niemand!";
                    } else {
                        for (String player : e.getParticipants()) {
                            allParticipants = allParticipants + "  " + Main.jda.getUserById(player).getName();
                        }
                    }
                    MessageEmbed eb2 = new EmbedBuilder()
                            .setAuthor("Das Event: " + e.getName() + " wurde erfolgreich entfernt!")
                            .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                            .setDescription(e.getDescription())
                            .setFooter("Spieler die Mitmachen wollten: " + allParticipants, null)
                            .build();
                    event.getChannel().sendMessage(eb2).queue();

                    for (String players : e.getParticipants()) {
                        MessageEmbed eb0 = new EmbedBuilder()
                                .setAuthor("Das Event: " + e.getName() + " wurde gestrichen!")
                                .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                                .setDescription(e.getDescription())
                                .setFooter("Spieler die Mitmachen wollten: " + allParticipants, null)
                                .build();
                        Main.jda.getUserById(players).openPrivateChannel().complete().sendMessage(eb0).queue();
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
                MessageChannel c = event.getGuild().getTextChannelsByName("event", true).get(0);
                for (Message m : c.getIterableHistory()) {
                    m.delete().queue();
                }
                for (Event e : EVENTS.get(event.getGuild())) {
                    String allParticipants = "";
                    String maybeParticipants = "";
                    if (e.getParticipants().size() <= 0) {
                        allParticipants = "Niemand!";
                        maybeParticipants = "Niemand!";
                    } else {
                        for (String player : e.getParticipants()) {
                            allParticipants = allParticipants + "  " + Main.jda.getUserById(player).getName();
                        }
                        for (String p: e.getMaybes()){
                            maybeParticipants = maybeParticipants + " " + Main.jda.getUserById(p).getName();
                        }
                    }
                    MessageEmbed eb = new EmbedBuilder()
                            .setAuthor(e.getName() + " ID: " + EVENTS.get(event.getGuild()).indexOf(e))
                            .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                            .setDescription(e.getDescription())
                            .addField("","Wöchentlich : " + e.isRepeatable() + "", true)
                            .setFooter("Spieler die Mitmachen: " + allParticipants, null)
                            //.addField("Maybes","Spieler die Vielleicht können: " + maybeParticipants, true)
                            .build();
                    String id = c.sendMessage(eb).complete().getId();
                    c.addReactionById( id, "✅").queue();
                    c.addReactionById( id, "⛔").queue();
                    c.addReactionById( id, "❓").queue();
                }
                if (EVENTS.get(event.getGuild()).size() < 1) {
                    MessageEmbed eb = new EmbedBuilder()
                            .setDescription("Es sind keine Events geplant!")
                            .build();
                    event.getGuild().getTextChannelsByName("event", true).get(0).sendMessage(eb).queue();
                }
                break;
            case "createrepeatable":
                if (PermissionCore.check(event, PermissionCore.GENERAL)){
                    String argsSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(1, args.length)));
                    List<String> content = Arrays.asList(argsSTRG.split("\\|"));
                    String name = content.get(1);
                    List<String> description = new ArrayList<>(content.subList(2, content.size()));
                    String desc = "";
                    for (String s : description) {
                        desc = desc + " " + s;
                    }
                    String[] eventTime = Time.getEventTime(content.get(0));
                    for (String t: eventTime){
                        t.replace(" ", "");
                    }
                    Event toAddEvent = new Event(eventTime, name, desc, true);
                    List<Event> temp = EVENTS.get(event.getGuild());
                    temp.add(toAddEvent);
                    MessageEmbed eb = new EmbedBuilder()
                            .setAuthor("Das Event: " + toAddEvent.getName() + " wurde erfolgreich erstellt!")
                            .setTitle("Datum: " + Time.formatDate(toAddEvent.getEventTime()) + " Zeit: " + Time.formatTime(toAddEvent.getEventTime()))
                            .setDescription(toAddEvent.getDescription())
                            .build();
                    String id = event.getChannel().sendMessage(eb).complete().getId();
                    this.EVENTS.put(event.getGuild(), temp);
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
                    EventTimeChecker c3 = new EventTimeChecker(event.getGuild(), toAddEvent);
                    ScheduledFuture sf = Executors.newSingleThreadScheduledExecutor().schedule(c3, Time.getTimeDifferenceMinutes(toAddEvent.getEventTime()), TimeUnit.MINUTES);
                    List<ScheduledFuture> tempFuture = ALLUSERSSCHEDUELEDEVENTS.get(event.getGuild().getId());
                    tempFuture.add(sf);
                    ALLUSERSSCHEDUELEDEVENTS.put(event.getGuild().getId(), tempFuture);
                    MessageChannel c2 = event.getGuild().getTextChannelsByName("event", true).get(0);
                    for (Message m : c2.getIterableHistory()) {
                        m.delete().queue();
                    }
                    for (Event e : EVENTS.get(event.getGuild())) {
                        String allParticipants = "";
                        String maybeParticipants = "";
                        if (e.getParticipants().size() <= 0) {
                            allParticipants = "Niemand!";
                            maybeParticipants = "Niemand!";
                        } else {
                            for (String player : e.getParticipants()) {
                                allParticipants = allParticipants + "  " + Main.jda.getUserById(player).getName();
                            }
                            for (String p: e.getMaybes()){
                                maybeParticipants = maybeParticipants + " " + Main.jda.getUserById(p).getName();
                            }
                        }
                        MessageEmbed eb2 = new EmbedBuilder()
                                .setAuthor(e.getName() + " ID: " + EVENTS.get(event.getGuild()).indexOf(e))
                                .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                                .setDescription(e.getDescription())
                                .addField("","Wöchentlich : " + e.isRepeatable() + "", true)
                                .setFooter("Spieler die Mitmachen: " + allParticipants, null)
                                //.addField("Maybes","Spieler die Vielleicht können: " + maybeParticipants, true)
                                .build();
                        String id2 = c2.sendMessage(eb2).complete().getId();
                        c2.addReactionById( id2, "✅").queue();
                        c2.addReactionById( id2, "⛔").queue();
                        c2.addReactionById( id2, "❓").queue();
                    }
                }
                break;
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {
    }

    @Override
    public String help(int permissionStage) {
        if (permissionStage >= PermissionCore.GENERAL){
            return "Mit /event create 02:04:2018:20:08|<Titel>|<Beschreibung> erstellst du ein Event am 02.04.2018 um 20 Uhr und 08 Minuten! \n" +
                    "Mit /event createRepeatable 02:04:2018:20:08|<Titel>|<Beschreibung> erstellst du ein Event am 02.04.2018 um 20 Uhr und 08 Minuten, das sich wöchentlich wiederhohlt! \n" +
                    "Mit /event delete <ID> löscht du ein erstelltes Event! \n" +
                    "Drücke auf ✅ im #event Kanal um beizutreten. \n" +
                    "Drücke auf ⛔ im #event Kanal um das Event, egal für was du dich eingetragen hast, zu verlassen! \n" +
                    "Drücke auf ❓ im #event Kanal um zu sagen ds du vielleicht kannst.";
        }else if (permissionStage >= PermissionCore.NEULING){
            return "Drücke auf ✅ im #event Kanal um beizutreten. \n" +
                    "Drücke auf ⛔ im #event Kanal um das Event, egal für was du dich eingetragen hast, zu verlassen! \n" +
                    "Drücke auf ❓ im #event Kanal um zu sagen ds du vielleicht kannst.";
        }else{
            return null;
        }
    }

    public static void loadEvents(JDA jda) {
        jda.getGuilds().forEach(g -> {

            File f = new File("SERVER_SETTINGS/" + g.getId() + "/eventList.dat");
            if (f.exists())
                try {
                    EVENTS.put(g, getEvent(g));
                    for (Event e : EVENTS.get(g)){
                        EventTimeChecker c = new EventTimeChecker(g, e);
                        ScheduledFuture sf = Executors.newSingleThreadScheduledExecutor().schedule(c, Time.getTimeDifferenceMinutes(e.getEventTime()), TimeUnit.MINUTES);
                        int i = 0;

                            if (ALLUSERSSCHEDUELEDEVENTS.get(g.getId()) == null){
                                ALLUSERSSCHEDUELEDEVENTS.put(g.getId(), new ArrayList<>());
                            }
                            List<ScheduledFuture> tempFuture = ALLUSERSSCHEDUELEDEVENTS.get(g.getId());
                            tempFuture.add(sf);
                            ALLUSERSSCHEDUELEDEVENTS.put(g.getId(), tempFuture);
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
        if (PermissionCore.check(author, guild, PermissionCore.NEULING)) {
            Event e = EVENTS.get(guild).get(id);
            String allParticipants2 = "";
            if (e.getParticipants().size() <= 0) {
                allParticipants2 = "Niemand!";
            } else {
                for (String player : e.getParticipants()) {
                    allParticipants2 = allParticipants2 + "  " + Main.jda.getUserById(player).getName();
                }
            }
            MessageEmbed eb3;
            if (e.getParticipants().size() >= 6) {
                eb3 = new EmbedBuilder()
                        .setAuthor("Du kanst dem Event: " + e.getName() + " nich beigetreten. Es ist schon voll!")
                        .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                        .setDescription(e.getDescription())
                        .setFooter("Spieler die Mitmachen: " + allParticipants2, null)
                        .build();
                author.openPrivateChannel().queue((privateChannel -> {
                    privateChannel.sendMessage(eb3).queue();
                }));
                e.getMaybes().add(author.getId());
            } else {
                List names = new ArrayList();
                for (String s : e.getParticipants()) {
                    names.add(s);
                }
                if (!(names.contains(author.getId()))) {
                    EVENTS.get(guild).get(id).addParticipant(author.getId());
                    allParticipants2 = "";
                    if (e.getParticipants().size() <= 0) {
                        allParticipants2 = "Niemand!";
                    } else {
                        for (String player : e.getParticipants()) {
                            allParticipants2 = allParticipants2 + "  " + Main.jda.getUserById(player).getName();
                        }
                    }
                    eb3 = new EmbedBuilder()
                            .setAuthor("Du bist dem Event: " + e.getName() + " erfolgreich beigetreten!")
                            .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                            .setDescription(e.getDescription())
                            .setFooter("Spieler die Mitmachen: " + allParticipants2, null)
                            .build();
                    author.openPrivateChannel().queue((privateChannel -> {
                        privateChannel.sendMessage(eb3).queue();
                    }));
                    if (e.getParticipants().size() >= 6) {
                        MessageEmbed eb4 = new EmbedBuilder()
                                .setAuthor("Das Event: " + e.getName() + " ist voll besetzt!")
                                .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                                .setDescription(e.getDescription())
                                .setFooter("Spieler die Mitmachen: " + allParticipants2, null)
                                .build();
                        for (String s2 : e.getParticipants()) {
                            Main.jda.getUserById(s2).openPrivateChannel().queue((channel) -> {
                                channel.sendMessage(eb4).queue();
                            });
                        }
                    }
                } else {
                    Random r = new Random();
                    int rando = (r.nextInt(4 + 1) + 0);
                    switch (rando) {
                        case 0:
                            author.openPrivateChannel().queue((privateChannel -> {
                                privateChannel.sendMessage(author.getAsMention() +
                                        " Ich weiß du bist dick aber du bist nich soo dick das du gleich 2 Plätze brauchst.").queue();
                            }));
                            break;
                        case 1:
                            author.openPrivateChannel().queue((privateChannel -> {
                                privateChannel.sendMessage(author.getAsMention() +
                                        " So wie dein Arsch auch nur ne Sicherungskopie von deinem Gesicht ist, ist es dieser Versuch dich erneut einzutragen.").queue();
                            }));
                            break;
                        case 2:
                            author.openPrivateChannel().queue((privateChannel -> {
                                privateChannel.sendMessage(author.getAsMention() +
                                        " Aber du weißt schon das du SCHON dabei bist ??.").queue();
                            }));
                            break;
                        case 3:
                            author.openPrivateChannel().queue((privateChannel -> {
                                privateChannel.sendMessage(author.getAsMention() +
                                        " Wenn du dich noch mal Versuchst ein zu tragen, sind wir immer noch einer zu wenig.").queue();
                            }));
                            break;
                        default:
                            author.openPrivateChannel().queue((privateChannel -> {
                                privateChannel.sendMessage(author.getAsMention() +
                                        " Du Scherzkecks bist schon dabei!").queue();
                            }));
                            break;
                    }
                }
            }
            try {
                saveEvent(guild);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            MessageChannel c = guild.getTextChannelsByName("event", true).get(0);
                MessageEmbed eb = new EmbedBuilder()
                        .setAuthor(e.getName() + " ID: " + EVENTS.get(guild).indexOf(e))
                        .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                        .setDescription(e.getDescription())
                        .addField("","Wöchentlich : " + e.isRepeatable() + "", true)
                        .setFooter("Spieler die Mitmachen: " + allParticipants2, null)
                        .build();
                Message m = c.getMessageById(messageID).complete();
                m.editMessage(eb).queue();
        }
    }

    public static void removeMemberToEvent(int i, User user, Guild guild, String messageId) {
        Event e = EVENTS.get(guild).get(i);

        if (e.getParticipants().contains(user.getId()) || e.getMaybes().contains(user.getId())){
            e.removeParticipant(user.getId());
            e.getMaybes().remove(user.getId());
            String allParticipants2 = "";
            if (e.getParticipants().size() <= 0) {
                allParticipants2 = "Niemand!";
            } else {
                for (String player : e.getParticipants()) {
                    allParticipants2 = allParticipants2 + "  " + Main.jda.getUserById(player).getName();
                }
            }

            MessageEmbed eb4 = new EmbedBuilder()
                    .setAuthor("Du hast das Event: " + e.getName() + " erfolgreich verlassen!")
                    .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                    .setDescription(e.getDescription())
                    .setFooter("Spieler die Mitmachen: " + allParticipants2, null)
                    .build();
            user.openPrivateChannel().queue((channel) -> {
                channel.sendMessage(eb4).queue();
            });

            for (String s : e.getParticipants()){
                RestAction<PrivateChannel> c = Main.jda.getUserById(s).openPrivateChannel();
                c.complete().sendMessage(user.getAsMention() + " Hat das Event verlassen.").queue();
            }
            try {
                saveEvent(guild);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            MessageChannel c = guild.getTextChannelsByName("event", true).get(0);
            String allParticipants = "";
            if (e.getParticipants().size() <= 0) {
                allParticipants = "Niemand!";
            } else {
                for (String player : e.getParticipants()) {
                    allParticipants = allParticipants + "  " + Main.jda.getUserById(player).getName();
                }
            }
            MessageEmbed eb = new EmbedBuilder()
                    .setAuthor(e.getName() + " ID: " + EVENTS.get(guild).indexOf(e))
                    .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                    .setDescription(e.getDescription())
                    .addField("","Wöchentlich : " + e.isRepeatable() + "", true)
                    .setFooter("Spieler die Mitmachen: " + allParticipants, null)
                    .build();
            Message m = c.getMessageById(messageId).complete();
            m.editMessage(eb).queue();
        }else{
            System.out.println(user.getName() + " hat versucht etwas zu verlassen das er nie betreten hat.");
        }
    }

    public static void addMaybeToEvent(int id, User author, Guild guild, String messageId) {
        if (PermissionCore.check(author, guild, PermissionCore.NEULING)) {
            Event e = EVENTS.get(guild).get(id);
            String allParticipants2 = "";
            if (e.getParticipants().size() <= 0) {
                allParticipants2 = "Niemand!";
            } else {
                for (String player : e.getParticipants()) {
                    allParticipants2 = allParticipants2 + "  " + Main.jda.getUserById(player).getName();
                }
            }
            MessageEmbed eb3;
            if (e.getParticipants().size() >= 6) {
                eb3 = new EmbedBuilder()
                        .setAuthor("Du kanst dem Event: " + e.getName() + " nich beigetreten. Es ist schon voll! Du wurdest aber auf die Warteliste gesetzt!")
                        .setTitle("Datum: " + Time.formatDate(e.getEventTime()) + " Zeit: " + Time.formatTime(e.getEventTime()))
                        .setDescription(e.getDescription())
                        .setFooter("Spieler die Mitmachen: " + allParticipants2, null)
                        .build();
                author.openPrivateChannel().queue((privateChannel -> {
                    privateChannel.sendMessage(eb3).queue();
                }));
                List names = new ArrayList();
                for (String s : e.getParticipants()) {
                    names.add(s);
                }
                List names2 = new ArrayList();
                for (String s : e.getMaybes()) {
                    names2.add(s);
                }
                if (!(names.contains(author.getId())) || !(names2.contains(author.getId()))) {
                    EVENTS.get(guild).get(id).addMaybeParticipant(author.getId());
                }
            }
                try {
                    saveEvent(guild);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
    }
}
