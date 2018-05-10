package listeners;

import commands.CmdEvent;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class OnReactionHandler extends ListenerAdapter {

    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        Message m = event.getChannel().getMessageById(event.getMessageId()).complete();
        if (m.getChannel().getName().contains("event")){
            for (MessageEmbed s : m.getEmbeds()){
                String[] f= s.getAuthor().getName().split(" ");
                if (event.getUser() != event.getJDA().getSelfUser()){
                    MessageReaction.ReactionEmote emo = event.getReactionEmote();
                    if (emo.getName().equals("✅")){
                        System.out.println(event.getUser().getName() + " Participant Added: " + f[f.length - 1]);
                        CmdEvent.addMemberToEvent(Integer.parseInt(f[f.length - 1]), event.getUser(), event.getGuild(), event.getMessageId());
                    }
                    if (emo.getName().equals("⛔")){
                        System.out.println(event.getUser().getName() + " Participant Removed: " + f[f.length - 1]);
                        CmdEvent.removeMemberToEvent(Integer.parseInt(f[f.length - 1]), event.getUser(), event.getGuild(), event.getMessageId());
                    }
                    if (emo.getName().equals("❓")){
                        System.out.println(event.getUser().getName() + " Maybe Added: " + f[f.length - 1]);
                        CmdEvent.addMaybeToEvent(Integer.parseInt(f[f.length - 1]), event.getUser(), event.getGuild(), event.getMessageId());
                    }
                }
            }
        }
    }

    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        System.out.println("Reaction Removed");
    }
}
