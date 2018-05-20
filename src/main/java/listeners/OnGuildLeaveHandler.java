package listeners;

import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class OnGuildLeaveHandler extends ListenerAdapter {

    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        event.getGuild().getTextChannelsByName("besprechungen", true).get(0).sendMessage(event.getUser().getName() + " hat den Discord-Server verlassen.").queue();
    }
}
