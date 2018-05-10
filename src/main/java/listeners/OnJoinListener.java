package listeners;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import java.util.List;

public class OnJoinListener extends ListenerAdapter {


    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        event.getGuild().getTextChannelsByName("allgemein", true).get(0).sendMessage("Wilkommen Hüter "
                + event.getMember().getUser().getName() + " bitte stelle dich doch kurz im #wer-sind-wir-alle Kanal vor. mit /help siehst " +
                "du alle Befehle auf die du zugriff hast.").queue();
        Role neuling = event.getGuild().getRolesByName("Gast", true).get(0);
        event.getGuild().getController().addSingleRoleToMember(event.getMember(), neuling).queue();
        System.out.println("A new Member joined , he got his Role: " + neuling.getName());
    }
}
