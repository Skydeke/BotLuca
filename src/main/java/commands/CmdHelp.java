package commands;

import core.CommandHandler;
import core.PermissionCore;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;

public class CmdHelp implements Command {
    @Override
    public int permission(int permissionStage) {
        return PermissionCore.NEULING;
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (PermissionCore.check(event, PermissionCore.NEULING)){
            int playerPermissionStage = PermissionCore.getPermissionStage(event);
            String filterdHelp = "";
            int zähler = 1;
            for (Command c : CommandHandler.COMMANDS.values()){
                if (PermissionCore.checkWithoutWarning(event, c.permission(playerPermissionStage))){
                    filterdHelp += "\n \n" + zähler + ". " + c.help(playerPermissionStage);
                    zähler++;
                }
            }
            EmbedBuilder eb = new EmbedBuilder().setTitle("Hilfe-Seite:").setDescription(filterdHelp).setColor(Color.CYAN);
            event.getTextChannel().sendMessage(eb.build()).queue();
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    @Override
    public String help(int permissionStage) {
        return "/help zeigt die Hilfe-Seite an!";
    }
}
