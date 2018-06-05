package commands;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface ICommand {

    int permission(int permissionStage);
    boolean called(String[] args, MessageReceivedEvent event);
    void action(String[] args, MessageReceivedEvent event);
    void executed(boolean success, MessageReceivedEvent event);
    String help(int permissionStage);

}
