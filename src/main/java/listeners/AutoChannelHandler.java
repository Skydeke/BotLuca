package listeners;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class AutoChannelHandler extends ListenerAdapter {

    // Liste aller aktiven, erstellen Tempchannels
    List<VoiceChannel> active = new ArrayList<>();


    /*
        Wenn jemand einem VC joint und dieser im Autochannel Register ist
        wird ein VC mit <VC-Name + "[AC]"> erstellt und unter den Autochannel
        geschoben. Dannach wird der Member in den Tempchannel gemoved.
    */
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        HashMap<VoiceChannel, Guild> autochans = commands.Autochannel.getAutochans();
        VoiceChannel vc = event.getChannelJoined();
        Guild g = event.getGuild();

        if (autochans.containsKey(vc)) {
            VoiceChannel nvc = (VoiceChannel) g.getController().createVoiceChannel("User Channel: " + active.size())
                    .setBitrate(vc.getBitrate())
                    .setUserlimit(vc.getUserLimit())
                    .complete();
            while (nvc == null){

            }
            if(nvc != null){
                g.getController().modifyVoiceChannelPositions().selectPosition(nvc).moveTo(vc.getPosition() + 1).complete();
                nvc.getManager().setParent(vc.getParent()).complete();
                nvc.createPermissionOverride(event.getMember()).setAllow(Permission.ALL_PERMISSIONS).complete();
                g.getController().moveVoiceMember(event.getMember(), nvc).complete();
                active.add(nvc);
            }
        }
    }

    /*
        Wenn der geleavedte Channel in der Tempchannel Liste steht UND
        sich kein anderer Member mehr in dem Channel befindet, so wird dieser
        gelöscht und aus der Tempchannel Liste entfernt.
    */
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        VoiceChannel vc = event.getChannelLeft();

        if (active.contains(vc) && vc.getMembers().size() == 0) {
            active.remove(vc);
            vc.delete().complete();
        }
    }

    /*
        Beim joinen des Channels nach dem Moven:
        -> Selbes wie bei <onGuildVoiceJoin()>

        Beim leaven des Channels nach dem Moven:
        -> Selbes wie bei <onGuildVoiceLeave()>
    */
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        HashMap<VoiceChannel, Guild> autochans = commands.Autochannel.getAutochans();
        Guild g = event.getGuild();

        VoiceChannel vc = event.getChannelJoined();

        if (autochans.containsKey(vc)) {
            VoiceChannel nvc = (VoiceChannel) g.getController().createVoiceChannel("User Channel: " + active.size() + 1)
                    .setBitrate(vc.getBitrate())
                    .setUserlimit(vc.getUserLimit())
                    .complete();

            while (nvc == null){

            }
            if(nvc != null){
                g.getController().modifyVoiceChannelPositions().selectPosition(nvc).moveTo(vc.getPosition() + 1).complete();
                nvc.getManager().setParent(vc.getParent()).complete();
                nvc.createPermissionOverride(event.getMember()).setAllow(Permission.ALL_PERMISSIONS).complete();
                g.getController().moveVoiceMember(event.getMember(), nvc).complete();
                active.add(nvc);
            }
        }

        vc = event.getChannelLeft();

        if (active.contains(vc) && vc.getMembers().size() == 0) {
            active.remove(vc);
            vc.delete().complete();
        }
    }

    /*
        Wenn sich der gelöschte Channel im Autochannel Register befindet, dann wird
        nach dem Löschen der Autochannel aus dem Register entfernt und dieses
        gespeichert in der Save File.
    */
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        HashMap<VoiceChannel, Guild> autochans = commands.Autochannel.getAutochans();
        if (autochans.containsKey(event.getChannel())) {
            commands.Autochannel.unsetChan(event.getChannel());
        }

    }

}
