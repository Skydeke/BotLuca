package commands;

import core.Main;
import core.PermissionCore;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import sspcore.Hand;
import sspcore.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CmdDuell implements ICommand {

    private Player p1;
    private Player p2;
    private boolean allPlayerHere = false;
    private ScheduledFuture f1;

    @Override
    public int permission(int permissionStage) {
        return PermissionCore.MITGLIED;
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if(PermissionCore.check(event, PermissionCore.MITGLIED)){
            String argsSTRG = String.join(" ", new ArrayList<>(Arrays.asList(args).subList(0, args.length)));
            List<String> content = Arrays.asList(argsSTRG.split("\\|"));
            User opponent = Main.jda.getUserById(content.get(0).replace("@", "")
                    .replace("<", "")
                    .replace(">", "")
                    .replace(" ", "")
                    .replace("", ""));
            if (p1 == null || p2.getUser() == event.getAuthor()){
                switch (content.get(1).toLowerCase().replace(" ", "")){
                    case "stein":
                        if (p1 == null){
                            p1 = new Player(Hand.STEIN, event.getAuthor());
                            p2 = new Player(null, opponent);
                            event.getMessage().delete().queue();
                            sendStups(opponent, event);
                        }else{
                            p2 = new Player(Hand.STEIN, event.getAuthor());
                            event.getMessage().delete().queue();
                            allPlayerHere = true;
                        }
                        break;
                    case "schere":
                        if (p1 == null){
                            p1 = new Player(Hand.SCHERE, event.getAuthor());
                            p2 = new Player(null, opponent);
                            event.getMessage().delete().queue();
                            sendStups(opponent, event);
                        }else{
                            p2 = new Player(Hand.SCHERE, event.getAuthor());
                            event.getMessage().delete().queue();
                            allPlayerHere = true;
                        }
                        break;
                    case "papier":
                        if (p1 == null){
                            p1 = new Player(Hand.PAPIER, event.getAuthor());
                            p2 = new Player(null, opponent);
                            event.getMessage().delete().queue();
                            sendStups(opponent, event);
                        }else{
                            p2 = new Player(Hand.PAPIER, event.getAuthor());
                            event.getMessage().delete().queue();
                            allPlayerHere = true;
                        }
                        break;
                }
                if (allPlayerHere){
                    /**
                     * Determine Winner
                     */
                    f1.cancel(false);
                    MessageEmbed eb;
                    if (p1.getHand().beats(p2.getHand())){
                        eb = new EmbedBuilder()
                                .setAuthor(p1.getUser().getName() + " hat " + p2.getUser().getName() + " besiegt!")
                                .setTitle("Glückwunsch!")
                                .setDescription(p1.getUser().getName() + " hat: " + p1.getHand().name() + " gewählt während " +
                                        p2.getUser().getName() + " " + p2.getHand().name() + " gewählt hat.")
                                .build();
                    }else if (p2.getHand().beats(p1.getHand())){
                        eb = new EmbedBuilder()
                                .setAuthor(p2.getUser().getName() + " hat " + p1.getUser().getName() + " besiegt!")
                                .setDescription(p2.getUser().getName() + " hat: " + p2.getHand().name() + " gewählt während " +
                                        p1.getUser().getName() + " " + p1.getHand().name() + " gewählt hat.")
                                .setTitle("Glückwunsch!")
                                .build();
                    }else {
                        eb = new EmbedBuilder()
                                .setAuthor(p2.getUser().getName() + " und " + p1.getUser().getName() + " sind gleichstark!")
                                .setTitle("Vielleicht beim Nächsten Mal...")
                                .setDescription(p1.getUser().getName() + " hat: " + p1.getHand().name() + " gewählt während " +
                                        p2.getUser().getName() + " " + p2.getHand().name() + " gewählt hat.")
                                .build();
                    }
                    event.getTextChannel().sendMessage(eb).queue();
                    p1 = null;
                    p2 = null;
                    allPlayerHere = false;
                }
            }
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    @Override
    public String help(int permissionStage) {
        if (permissionStage >= PermissionCore.MITGLIED){
            return "**Duell:**\n" +
                    ":white_small_square: Nutze `/duell @luca407|<Schere/Stein/Papier>` um Luca zu einem Duell herauszufordern";
        }else{
            return null;
        }

    }

    public void sendStups(User reccever, MessageReceivedEvent event){
        event.getTextChannel().sendMessage(reccever.getAsMention() + " " + event.getAuthor().getName() + " will sich mit dir duellieren wie echte Männer! Nutze /duell "
                + event.getAuthor().getAsMention() + " <Schere/Stein/Papier> um ihm zu Zeigen wer gewinnt! Du hast 60 Sekunden um ihn zu besiegen!").queue();
        f1 = new ScheduledThreadPoolExecutor(0).schedule(new Runnable() {
            @Override
            public void run() {
                event.getTextChannel().sendMessage(reccever.getAsMention() + " will sich nicht duellieren!").queue();
                p1 = null;
                p2 = null;
                allPlayerHere = false;
            }
        }, 60, TimeUnit.SECONDS);
    }
}
