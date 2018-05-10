package core;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.security.auth.Refreshable;

public class PermissionCore {

    public static final int GAST = 2;
    public static final int VEX = 3;
    public static final int NEULING = 4;
    public static final int VETERAN = 5;
    public static final int GENERAL = 10;
    public static final int LENNY = 10;
    public static final int ENTWICKLER = 15;
    public static final int ANFÜHRER = 20;

    public static boolean check(MessageReceivedEvent event, int berechtigungsstufe){
        int berechtigung = 1;
        if (event.getGuild().getMember(event.getAuthor()).getRoles().get(0) != null){
            Role r = event.getGuild().getMember(event.getAuthor()).getRoles().get(0);
            if (r.getName().contains(References.PERMS[7])){
                berechtigung = GAST;
            }
            if (r.getName().contains(References.PERMS[6])){
                berechtigung = VEX;
            }
            if (r.getName().contains(References.PERMS[0])){
                berechtigung = NEULING;
            }
            if (r.getName().contains(References.PERMS[1])){
                berechtigung = VETERAN;
            }if (r.getName().contains(References.PERMS[2]) || r.getName().contains(References.PERMS[5])){
                berechtigung = GENERAL; //LENNY
            }if (r.getName().contains(References.PERMS[3])){
                berechtigung = ENTWICKLER;
            }if (r.getName().contains(References.PERMS[4])){
                berechtigung = ANFÜHRER;
            }
        }
        if(berechtigung >= berechtigungsstufe){
            return true;
        }else if (berechtigung < berechtigungsstufe){
            System.out.println("Berechtingunsstufe nicht hoch genug!");
            event.getTextChannel().sendMessage(":warning: Entschuldige, " + event.getAuthor().getAsMention() + ", du bist nicht Berechtigt das zu tun.").queue();
            return false;
        }
        return false;
    }
    public static boolean check(User author, Guild guild, int berechtigungsstufe){
        int berechtigung = 1;
        if (guild.getMember(author).getRoles().get(0) != null){
            Role r = guild.getMember(author).getRoles().get(0);
            if (r.getName().contains(References.PERMS[7])){
                berechtigung = GAST;
            }
            if (r.getName().contains(References.PERMS[6])){
                berechtigung = VEX;
            }
            if (r.getName().contains(References.PERMS[0])){
                berechtigung = NEULING;
            }
            if (r.getName().contains(References.PERMS[1])){
                berechtigung = VETERAN;
            }if (r.getName().contains(References.PERMS[2]) || r.getName().contains(References.PERMS[5])){
                berechtigung = GENERAL; //LENNY
            }if (r.getName().contains(References.PERMS[3])){
                berechtigung = ENTWICKLER;
            }if (r.getName().contains(References.PERMS[4])){
                berechtigung = ANFÜHRER;
            }
        }
        if(berechtigung >= berechtigungsstufe){
            return true;
        }else if (berechtigung < berechtigungsstufe){
            System.out.println("Berechtingunsstufe nicht hoch genug!");
            author.openPrivateChannel().queue((privateChannel -> {
                privateChannel.sendMessage(":warning: Entschuldige, " + author.getAsMention() + ", du bist nicht Berechtigt das zu tun.").queue();
            }));
            return false;
        }
        return false;
    }

    public static boolean checkWithoutWarning(MessageReceivedEvent event, int berechtigungsstufe){
        int berechtigung = 1;
        if (event.getGuild().getMember(event.getAuthor()).getRoles().get(0) != null){
            Role r = event.getGuild().getMember(event.getAuthor()).getRoles().get(0);
            if (r.getName().contains(References.PERMS[7])){
                berechtigung = GAST;
            }
            if (r.getName().contains(References.PERMS[6])){
                berechtigung = VEX;
            }
            if (r.getName().contains(References.PERMS[0])){
                berechtigung = NEULING;
            }
            if (r.getName().contains(References.PERMS[1])){
                berechtigung = VETERAN;
            }if (r.getName().contains(References.PERMS[2]) || r.getName().contains(References.PERMS[5])){
                berechtigung = GENERAL; //LENNY
            }if (r.getName().contains(References.PERMS[3])){
                berechtigung = ENTWICKLER;
            }if (r.getName().contains(References.PERMS[4])){
                berechtigung = ANFÜHRER;
            }
        }
        if(berechtigung >= berechtigungsstufe){
            return true;
        }else if (berechtigung < berechtigungsstufe){
            return false;
        }
        return false;
    }

    public static int getPermissionStage(MessageReceivedEvent event){
        int berechtigung = 1;
        if (event.getGuild().getMember(event.getAuthor()).getRoles().get(0) != null){
            Role r = event.getGuild().getMember(event.getAuthor()).getRoles().get(0);
            if (r.getName().contains(References.PERMS[7])){
                berechtigung = GAST;
            }
            if (r.getName().contains(References.PERMS[6])){
                berechtigung = VEX;
            }
            if (r.getName().contains(References.PERMS[0])){
                berechtigung = NEULING;
            }
            if (r.getName().contains(References.PERMS[1])){
                berechtigung = VETERAN;
            }if (r.getName().contains(References.PERMS[2]) || r.getName().contains(References.PERMS[5])){
                berechtigung = GENERAL; //LENNY
            }if (r.getName().contains(References.PERMS[3])){
                berechtigung = ENTWICKLER;
            }if (r.getName().contains(References.PERMS[4])){
                berechtigung = ANFÜHRER;
            }
        }
        return berechtigung;
    }
}
