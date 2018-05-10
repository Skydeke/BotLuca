package commands;

import core.PermissionCore;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdClear implements Command {
    @Override
    public int permission(int permissionStage) {
        return PermissionCore.ENTWICKLER;
    }

    @Override
    public boolean called(String[] args, MessageReceivedEvent event) {
        return false;
    }

    @Override
    public void action(String[] args, MessageReceivedEvent event) {
        if (PermissionCore.check(event, PermissionCore.ENTWICKLER)){
            System.out.println( event.getAuthor().getName() + " hat den Kanal: " + event.getChannel().getName() +" um " + args[0] + " Nachrichten erleichtert!");
            int i =0;
            for (Message m : event.getChannel().getIterableHistory()){
                if (i <= Integer.parseInt(args[0])){
                    m.delete().queue();
                    i++;
                }
            }
        }
    }

    @Override
    public void executed(boolean success, MessageReceivedEvent event) {

    }

    @Override
    public String help(int permissionStage) {
        return "Mit /clear <Zahl der Nachrichten> leerst du X-Nacgrichten von unten an.";
    }
}
