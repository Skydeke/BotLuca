package remindercore;

import net.dv8tion.jda.core.entities.Guild;

import java.io.Serializable;

public class Reminder implements Serializable {

    private String description;
    private String[] time;
    private String g;
    private String u;

    public Reminder(String description, String[] time, String sentInGuild, String userId) {
        this.description = description;
        this.time = time;
        this.g = sentInGuild;
        this.u =userId;
    }

    public String getUser() {
        return u;
    }

    public String getGuild() {
        return g;
    }

    public String getDesc() {
        return description;
    }

    public String[] getEventTime() {
        return time;
    }
}
