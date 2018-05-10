package core;

import commands.*;
import listeners.OnJoinListener;
import listeners.OnMessageListener;
import listeners.OnReactionHandler;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import javax.security.auth.login.LoginException;
import java.util.Scanner;

public class Main {

    public static JDA jda;

    public static void main(String[] args){
        String text = new Scanner(Main.class.getResourceAsStream("/Token.txt"), "UTF-8").next();
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(text);
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setAudioEnabled(true);
        builder.setGame(Game.of(Game.GameType.STREAMING, "v1.5"));

        builder.addEventListener(new OnJoinListener());
        builder.addEventListener(new OnMessageListener());
        builder.addEventListener(new OnReactionHandler());

        CommandHandler.COMMANDS.put("help", new CmdHelp());
        CommandHandler.COMMANDS.put("ping", new CmdPing());
        CommandHandler.COMMANDS.put("vote", new CmdVote());
        CommandHandler.COMMANDS.put("m", new CmdMusic());
        CommandHandler.COMMANDS.put("event", new CmdEvent());
        CommandHandler.COMMANDS.put("remindme", new CmdRemindme());
        CommandHandler.COMMANDS.put("duell", new CmdDuell());
        CommandHandler.COMMANDS.put("clear", new CmdClear());


        try {
            jda = builder.buildBlocking();
            CmdVote.loadPolls(jda);
            CmdEvent.loadEvents(jda);
            CmdRemindme.loadReminds(jda);
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
