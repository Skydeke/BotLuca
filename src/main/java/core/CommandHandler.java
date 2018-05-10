package core;

import commands.Command;

import java.util.HashMap;

    public class CommandHandler {

        public static final CommandParser PARSER = new CommandParser();
        public static HashMap<String, Command> COMMANDS = new HashMap<>();

        public static void handleCommand(CommandParser.commandContainer cmd) {

            if (COMMANDS.containsKey(cmd.INVOKE.toLowerCase())) {

                boolean safe = COMMANDS.get(cmd.INVOKE.toLowerCase()).called(cmd.ARGS, cmd.event);

                if (!safe) {
                    COMMANDS.get(cmd.INVOKE.toLowerCase()).action(cmd.ARGS, cmd.event);
                    COMMANDS.get(cmd.INVOKE.toLowerCase()).executed(safe, cmd.event);
                } else {
                    COMMANDS.get(cmd.INVOKE.toLowerCase()).executed(safe, cmd.event);
                }

            }

        }

    }