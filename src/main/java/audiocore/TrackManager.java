package audiocore;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import commands.CmdMusic;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackManager extends AudioEventAdapter {

    private final AudioPlayer PLAYER;
    private final Queue<AudioInfo> queue = new LinkedBlockingQueue<>();
    private int sizeQueue = 0;

    public TrackManager(AudioPlayer player) {
        this.PLAYER = player;
    }

    public void queue(AudioTrack track, Member author) {
        AudioInfo info = new AudioInfo(track, author);
        queue.add(info);
        sizeQueue = sizeQueue + 1;
        CmdMusic.SUCCSESSFULLYQUEUED = true;
        if (PLAYER.getPlayingTrack() == null) {
            PLAYER.playTrack(track);
        }
    }

    public List<AudioInfo> getQueue() {
        return new ArrayList<>(queue);
    }

    public AudioInfo getInfo(AudioTrack track) {
        return queue.stream()
                .filter(info -> info.getTrack().equals(track))
                .findFirst().orElse(null);
    }

    public void purgeQueue() {
        queue.clear();
    }

    public void shuffleQueue() {
        List<AudioInfo> cQueue = new ArrayList<>(getQueue());
        AudioInfo current = cQueue.get(0);
        cQueue.remove(0);
        Collections.shuffle(cQueue);
        cQueue.add(0, current);
        purgeQueue();
        queue.addAll(cQueue);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        AudioInfo info = queue.element();
        VoiceChannel vChan = info.getAuthor().getVoiceState().getChannel();

        if (vChan == null)
            player.stopTrack();
        else
            info.getAuthor().getGuild().getAudioManager().openAudioConnection(vChan);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Guild g = queue.poll().getAuthor().getGuild();
        CmdMusic.OLDPLAYLISTSIZE = CmdMusic.OLDPLAYLISTSIZE - 1;
        sizeQueue = sizeQueue - 1;
        if (queue.isEmpty()) {
            g.getAudioManager().closeAudioConnection();
        }else{
                player.playTrack(queue.element().getTrack());
            }

    }

    public int getSize() {
        return sizeQueue;
    }

    public void setSizeQueue(int sizeQueue) {
        this.sizeQueue = sizeQueue;
    }
}