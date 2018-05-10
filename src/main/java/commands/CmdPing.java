package commands;

import core.PermissionCore;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdPing implements Command {

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
            event.getTextChannel().sendMessage("Your Ping: " + event.getMember().getJDA().getPing()).queue();
        }else{
            return;
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    @Override
    public String help(int permissionStage) {
        if (permissionStage >= PermissionCore.NEULING){
            return "Nutze /ping um deinen Ping anzeigen zu lassen.";
        }else{
            return "";
        }
    }
}
