package net.tokasen.cmdsigns;

import org.bukkit.block.Sign;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class CmdSigns extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("CmdSigns loaded!");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerSignOpen(PlayerSignOpenEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking() || event.getCause() != PlayerSignOpenEvent.Cause.INTERACT) {
            return;
        }

        ArrayList<String> commands = new ArrayList<>();
        SignSide sign = event.getSign().getSide(event.getSide());
        for (String line : sign.getLines()) {
            String cmd = parseCommand(line);
            if (cmd != null) {
                commands.add(cmd);
            }
        }

        if (commands.isEmpty()) {
            return;
        }

        event.setCancelled(true);
        for (String cmd : commands) {
            if(!player.performCommand(cmd.substring(1))) {
                return;
            }
        }

        player.sendMessage("All done!");
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i]
                    .replaceAll("§b§l", "")
                    .replaceAll("]§r", "]");
            String cmd = parseCommand(l);
            if (cmd == null) {
                event.setLine(i, l);
                continue;
            }

            int offset = l.indexOf("[/");
            int end = offset + cmd.length() + 2;
            String _line = "§r§b§l[" + cmd + "]§r";
            if (offset > 0) {
                _line = l.substring(0, offset) + _line;
            }
            if (end <= l.length() - 1) {
                _line += l.substring(end);
            }
            event.setLine(i, _line);
        }
    }

    private String parseCommand(String s) {
        int bracket_l = s.indexOf("[/");
        if (bracket_l == -1) {
            return null;
        }
        int bracket_r = s.indexOf(']', bracket_l+1);
        if (bracket_r == -1 || bracket_r == bracket_l+2) {
            return null;
        }
        return s.substring(bracket_l+1, bracket_r);
    }
}
