package core;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;

public class CommandParser {

    public commandContainer parse(String raw, MessageReceivedEvent event) {

        String beheaded = raw.replaceFirst("\\/", "");
        String[] splitBeheaded = beheaded.split(" ");
        String invoke = splitBeheaded[0];
        ArrayList<String> split = new ArrayList<>();
        for (String s : splitBeheaded) {
            split.add(s);
        }
        String[] args = new String[split.size() - 1];
        split.subList(1, split.size()).toArray(args);

        return new commandContainer(raw, beheaded, splitBeheaded, invoke, args, event);
    }


    public class commandContainer {

        public final String RAW;
        public final String BEHEADED;
        public final String[] SPLITBEHEADED;
        public final String INVOKE;
        public final String[] ARGS;
        public final MessageReceivedEvent event;

        public commandContainer(String rw, String BEHEADED, String[] SPLITBEHEADED, String INVOKE, String[] ARGS, MessageReceivedEvent event) {
            this.RAW = rw;
            this.BEHEADED = BEHEADED;
            this.SPLITBEHEADED = SPLITBEHEADED;
            this.INVOKE = INVOKE;
            this.ARGS = ARGS;
            this.event = event;
        }

    }

}