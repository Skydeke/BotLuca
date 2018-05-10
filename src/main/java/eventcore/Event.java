package eventcore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable{

    private String[] eventTime;
    private String name;
    private String description;
    private boolean isntStarted = true;
    private List<String> userIdParticipating = new ArrayList<>();
    private List<String> maybes = new ArrayList<>();
    private boolean repeatable = false;

    public Event(String[] eventTime, String name, String description){
        this.eventTime = eventTime;
        this.name = name;
        this.description = description;
    }
    public Event(String[] eventTime, String name, String description, boolean repeatable){
        this.eventTime = eventTime;
        this.name = name;
        this.description = description;
        this.repeatable = repeatable;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public void addParticipant(String userId){
        userIdParticipating.add(userId);
    }

    public void removeParticipant(String idOfUserToRemove){
        userIdParticipating.remove(idOfUserToRemove);
    }

    public String[] getEventTime() {
        return eventTime;
    }

    private void setEventTime(String[] eventTime) {
        this.eventTime = eventTime;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public boolean isIsntStarted() {
        return isntStarted;
    }

    public void setIsntStarted(boolean isntStarted) {
        this.isntStarted = isntStarted;
    }

    public List<String> getParticipants() {
        return userIdParticipating;
    }

    public List<String> getMaybes() { return maybes; }

    public void addMaybeParticipant(String userId){
        maybes.add(userId);
    }
}
