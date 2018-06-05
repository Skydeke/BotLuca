package commands;

import core.CommandHandler;
import core.PermissionCore;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;

public class CmdHelp implements ICommand {
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
        if (PermissionCore.check(event, PermissionCore.MITGLIED)){
            int playerPermissionStage = PermissionCore.getPermissionStage(event);
            String filterdHelp = "";
            String filterdHelp2 = "";
            int zähler = 1;
            for (ICommand c : CommandHandler.COMMANDS.values()){
                if (PermissionCore.checkWithoutWarning(event, c.permission(playerPermissionStage))){
                    filterdHelp += "\n \n" + zähler + ". " + c.help(playerPermissionStage);
                    zähler++;
                }
            }
            if (filterdHelp.length() > 2000){
                filterdHelp2 = filterdHelp.substring(filterdHelp.indexOf("7."));
                filterdHelp = (String) filterdHelp.subSequence(0, filterdHelp.indexOf("7."));
            }
            EmbedBuilder eb = new EmbedBuilder().setTitle("Hilfe-Seite: 1").setDescription(filterdHelp).setColor(Color.CYAN);
            event.getTextChannel().sendMessage(eb.build()).queue();
            EmbedBuilder eb2 = new EmbedBuilder().setTitle("Hilfe-Seite: 2").setDescription(filterdHelp2).setColor(Color.CYAN);
            event.getTextChannel().sendMessage(eb2.build()).queue();
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    @Override
    public String help(int permissionStage) {
        return "**Help:**\n" +
                ":white_small_square: Mit `/help` zeigt die Hilfe-Seite an!";
    }
}
