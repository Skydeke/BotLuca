package listeners;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import core.CommandHandler;

public class OnMessageListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getMessage().getContentRaw().startsWith("/") && event.getMessage().getAuthor().getId() != event.getJDA().getSelfUser().getId()) {
                if (event.getChannelType() == ChannelType.PRIVATE){
                    event.getChannel().sendMessage("Du kannst mir nur Commands schreiben wenn ich auf einem Server bin!").queue();
                }else{
                    CommandHandler.handleCommand(CommandHandler.PARSER.parse(event.getMessage().getContentRaw(), event));
                }

            }
    }

}