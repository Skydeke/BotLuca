package commands;

import audiocore.AudioInfo;
import audiocore.PlayerSendHandler;
import audiocore.TrackManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import core.PermissionCore;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CmdMusic implements Command {


    private static final int PLAYLIST_LIMIT = 1000;
    private static Guild GUILD;
    private static final AudioPlayerManager MANAGER = new DefaultAudioPlayerManager();
    private static final Map<Guild, Map.Entry<AudioPlayer, TrackManager>> PLAYERS = new HashMap<>();
    private static AudioTrackInfo INFO = new AudioTrackInfo("","",1,"",true, "");
    private static AudioTrack TRACK;
    public static int OLDPLAYLISTSIZE = 0;
    public static boolean SUCCSESSFULLYQUEUED = false;

    /**
     * Audio Manager als Audio-Stream-Recource deklarieren.
     */
    public CmdMusic() {
        AudioSourceManagers.registerRemoteSources(MANAGER);
    }

    /**
     * Erstellt einen Audioplayer und fügt diesen in die PLAYERS-Map ein.
     * @param g Guild
     * @return AudioPlayer
     */
    private AudioPlayer createPlayer(Guild g) {
        AudioPlayer p = MANAGER.createPlayer();
        TrackManager m = new TrackManager(p);
        p.addListener(m);

        GUILD.getAudioManager().setSendingHandler(new PlayerSendHandler(p));

        PLAYERS.put(g, new AbstractMap.SimpleEntry<>(p, m));

        return p;
    }

    /**
     * Returnt, ob die Guild einen Eintrag in der PLAYERS-Map hat.
     * @param g Guild
     * @return Boolean
     */
    private boolean hasPlayer(Guild g) {
        return PLAYERS.containsKey(g);
    }

    /**
     * Returnt den momentanen Player der Guild aus der PLAYERS-Map,
     * oder erstellt einen neuen Player für die Guild.
     * @param g Guild
     * @return AudioPlayer
     */
    private AudioPlayer getPlayer(Guild g) {
        if (hasPlayer(g))
            return PLAYERS.get(g).getKey();
        else
            return createPlayer(g);
    }

    /**
     * Returnt den momentanen TrackManager der Guild aus der PLAYERS-Map.
     * @param g Guild
     * @return TrackManager
     */
    private TrackManager getManager(Guild g) {
        return PLAYERS.get(g).getValue();
    }

    /**
     * Returnt, ob die Guild einen Player hat oder ob der momentane Player
     * gerade einen Track spielt.
     * @param g Guild
     * @return Boolean
     */
    private boolean isIdle(Guild g) {
        return !hasPlayer(g) || getPlayer(g).getPlayingTrack() == null;
    }

    /**
     * Läd aus der URL oder dem Search String einen Track oder eine Playlist
     * in die Queue.
     * @param identifier URL oder Search String
     * @param author Member, der den Track / die Playlist eingereiht hat
     * @param msg Message des Contents
     */
    private void loadTrack(String identifier, Member author, Message msg) {

        Guild guild = author.getGuild();
        getPlayer(guild);

        MANAGER.setFrameBufferDuration(5000);
        MANAGER.loadItemOrdered(guild, identifier, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                getManager(guild).queue(track, author);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (int i = 0; i < (playlist.getTracks().size() > PLAYLIST_LIMIT ? PLAYLIST_LIMIT : playlist.getTracks().size()); i++) {
                    getManager(guild).queue(playlist.getTracks().get(i), author);
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
            }
        });

    }

    /**
     * Läd aus der URL oder dem Search String einen Track oder eine Playlist
     * in die Queue.
     * @param identifier URL oder Search String
     * @param author Member, der den Track / die Playlist eingereiht hat
     * @param msg Message des Contents
     */
    private void loadTrackAsSearch(String identifier, Member author, Message msg) {

        Guild guild = author.getGuild();
        getPlayer(guild);

        MANAGER.setFrameBufferDuration(5000);
        MANAGER.loadItemOrdered(guild, identifier, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                getManager(guild).queue(track, author);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                getManager(guild).queue(playlist.getTracks().get(0), author);
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
            }
        });

    }

    /**
     * Stoppt den momentanen Track, worauf der nächste Track gespielt wird.
     * @param g Guild
     */
    private void skip(Guild g) {
        getPlayer(g).stopTrack();
        //getManager(g).setSizeQueue(getManager(g).getSize() - 1);
    }

    /**
     * Erzeugt aus dem Timestamp in Millisekunden ein hh:mm:ss - Zeitformat.
     * @param milis Timestamp
     * @return Zeitformat
     */
    private String getTimestamp(long milis) {
        long seconds = milis / 1000;
        long hours = Math.floorDiv(seconds, 3600);
        seconds = seconds - (hours * 3600);
        long mins = Math.floorDiv(seconds, 60);
        seconds = seconds - (mins * 60);
        return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
    }

    /**
     * Returnt aus der AudioInfo eines Tracks die Informationen als String.
     * @param info AudioInfo
     * @return Informationen als String
     */
    private String buildQueueMessage(AudioInfo info) {
        AudioTrackInfo trackInfo = info.getTrack().getInfo();
        String title = trackInfo.title;
        long length = trackInfo.length;
        return "`[ " + getTimestamp(length) + " ]` " + title + "\n";
    }

    /**
     * Sendet eine Embed-Message in der Farbe Rot mit eingegebenen Content.
     * @param event MessageReceivedEvent
     * @param content Error Message Content
     */
    private void sendErrorMsg(MessageReceivedEvent event, String content) {
        event.getTextChannel().sendMessage(
                new EmbedBuilder()
                        .setColor(Color.red)
                        .setDescription(content)
                        .build()
        ).queue();
    }

    /**
     * Sendet eine Embed-Message in der Farbe Rot mit eingegebenen Content.
     * @param event MessageReceivedEvent
     * @param content Error Message Content
     * @param color The Color at the left
     */
    private void sendMsg(MessageReceivedEvent event, String content, Color color) {
        event.getTextChannel().sendMessage(
                new EmbedBuilder()
                        .setColor(color)
                        .setDescription(content)
                        .build()
        ).queue();
}


    @Override
    public int permission(int stufe) {
        return PermissionCore.HUETER;
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
            GUILD = event.getGuild();

            if (PermissionCore.check(event, PermissionCore.HUETER)){

                if (args.length < 1) {

                    event.getTextChannel().sendMessage(
                            new EmbedBuilder()
                                    .setColor(Color.cyan)
                                    .setDescription("Musik-Hilfe:")
                                    .addField("Alle Befehle:","Nutze */m play <Link oder Suchanfrage>* um ein Lied in die Wiedergabeliste zu speichern. \n" +
                                            "Nutze */m p <Link oder Suchanfrage>* um ein Lied in die Wiedergabeliste zu speichern. \n" +
                                            "Nutze */m skip* um ein Lied in der Wiedergabeliste zu überspringen. \n" +
                                            "Nutze */m s* um ein Lied in der Wiedergabeliste zu überspringen. \n" +
                                            "Nutze */m stop* um die Wiedergabe zu beenden. \n" +
                                            "Nutze */m info* um Informationen zu dem laufenden song zu bekommen. \n" +
                                            "Nutze */m now* um Informationen zu dem laufenden song zu bekommen. \n" +
                                            "Nutze */m playlist* um die Playlist zu sehen.", true)
                                    .build()
                    ).queue();
                    return;
                }

                switch (args[0].toLowerCase()) {

                    case "play":
                    case "p":

                        if (args.length < 2) {
                            sendErrorMsg(event, "Kann die Quelle nicht finden!");
                            return;
                        }

                        String input = Arrays.stream(args).skip(1).map(s -> " " + s).collect(Collectors.joining()).substring(1);

                        if (!(input.startsWith("http://") || input.startsWith("https://"))){
                            System.out.println("Suchanfrage: " + input);
                            input = "ytsearch: " + input;
                            loadTrackAsSearch(input, event.getMember(), event.getMessage());
                        }else{
                            loadTrack(input, event.getMember(), event.getMessage());
                        }
                        SUCCSESSFULLYQUEUED = false;
                        OLDPLAYLISTSIZE = getManager(GUILD).getSize();
                        System.out.println(" Recceived!  " + (getManager(GUILD).getSize()));
                        if ((getManager(GUILD).getSize() > 0)){
                            System.out.println("More than 1 song!");
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    AudioInfo lastAdded = getManager(GUILD).getQueue().get((getManager(GUILD).getSize() - 1));
                                    sendMsg(event, "In die Playlist wurde der Titel: " + lastAdded.getTrack().getInfo().title + " von: " + lastAdded.getTrack().getInfo().author + " aufgenommen.", Color.cyan);
                                }
                            };
                            new ScheduledThreadPoolExecutor(0).schedule(r, 5, TimeUnit.SECONDS);
                        }else if (getManager(GUILD).getSize() == 0){
                            Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    INFO = getPlayer(GUILD).getPlayingTrack().getInfo();
                                    sendMsg(event, "Jetzt spielt der Titel: " + INFO.title + " von: " + INFO.author, Color.cyan);
                                }
                            };
                            new ScheduledThreadPoolExecutor(0).schedule(r, 5, TimeUnit.SECONDS);
                        }
                        break;

                    case "skip":
                    case "s":

                        if (isIdle(GUILD)) return;
                        for (int i = (args.length > 1 ? Integer.parseInt(args[1]) : 1); i == 1; i--) {
                            skip(GUILD);
                        }
                        sendMsg(event, "Der Song: " + INFO.title + " von: " + INFO.author + " wird übersprungen.", Color.cyan);
                        TRACK = getPlayer(GUILD).getPlayingTrack();
                        INFO = TRACK.getInfo();
                        sendMsg(event, "Der Song: " + INFO.title + " von: " + INFO.author + " wird gespielt.", Color.cyan);
                        break;


                    case "stop":

                        if (isIdle(GUILD)) return;

                        getManager(GUILD).purgeQueue();
                        skip(GUILD);
                        getManager(GUILD).setSizeQueue(0);
                        GUILD.getAudioManager().closeAudioConnection();
                        sendMsg(event, "Musik wird ausgeschaltet!", Color.cyan);
                        break;

                    case "now":
                    case "INFO":

                        if (isIdle(GUILD)) return;
                        TRACK = getPlayer(GUILD).getPlayingTrack();
                        INFO = TRACK.getInfo();
                        event.getTextChannel().sendMessage(
                                new EmbedBuilder()
                                        .setDescription("**Musik-INFO:**")
                                        .addField("Titel", INFO.title, false)
                                        .addField("Dauer", "`[ " + getTimestamp(TRACK.getPosition()) + "/ " + getTimestamp(TRACK.getDuration()) + " ]`", false)
                                        .addField("Autor", INFO.author, false)
                                        .build()
                        ).queue();

                        break;



                    case "playlist":

                        if (isIdle(GUILD)) return;
                        int form = 1;
                        if(args.length < 1){
                            form = Integer.parseInt(args[1]);
                        }else {
                            form = 1;
                        }

                        int sideNumb = args.length > 1 ? form : 1;

                        List<String> tracks = new ArrayList<>();
                        List<String> trackSublist;

                        getManager(GUILD).getQueue().forEach(audioInfo -> tracks.add(buildQueueMessage(audioInfo)));

                        if (tracks.size() > 20)
                            trackSublist = tracks.subList((sideNumb-1)*20, (sideNumb-1)*20+20);
                        else
                            trackSublist = tracks;

                        String out = trackSublist.stream().collect(Collectors.joining("\n"));
                        int sideNumbAll = tracks.size() >= 20 ? tracks.size() / 20 : 1;

                        event.getTextChannel().sendMessage(
                                new EmbedBuilder()
                                        .setDescription(
                                                "**Playlist:**\n" +
                                                        "*[ Tracks | Side " + sideNumb + " / " + sideNumbAll + "]* \n" +
                                                        out
                                        )
                                        .build()
                        ).queue();


                        break;
                }
            }
    }

    @Override
    public void executed(boolean sucess, MessageReceivedEvent event) {

    }

    @Override
    public String help(int stufe) {
        if (stufe >= PermissionCore.HUETER){
            return  "Nutze */m play <Link oder Suchanfrage>* oder *!m p <Link oder Suchanfrage>* um ein Lied in die Wiedergabeliste zu speichern. \n" +
                    "Nutze */m skip* oder *!m s* um ein Lied in der Wiedergabeliste zu überspringen. \n" +
                    "Nutze */m stop* um die Wiedergabe zu beenden. \n" +
                    "Nutze */m INFO* oder *!m now* um Informationen zu dem laufenden song zu bekommen. \n" +
                    "Nutze */m playlist* um die Playlist zu sehen.";
        }else{
            return "";
        }
    }
}