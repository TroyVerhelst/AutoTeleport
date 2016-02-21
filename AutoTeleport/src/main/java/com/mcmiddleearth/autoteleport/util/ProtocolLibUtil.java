/*
 * Copyright (C) 2016 MCME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mcmiddleearth.autoteleport.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.reflect.StructureModifier;
import com.mcmiddleearth.autoteleport.AutoTeleportPlugin;
import com.mcmiddleearth.autoteleport.command.AtpTarget;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

/**
 *
 * @author Eriol_Eandur
 */
public class ProtocolLibUtil {
  
    private static ProtocolLibConnection connector;
    
    private static class ProtocolLibConnection {
        
        private ProtocolManager protocolManager = null;
        private PacketConstructor chunkBulkPacketConstructor=null;
    
        public ProtocolLibConnection() {
            try{
    Logger.getGlobal().info("1 ");
                protocolManager = ProtocolLibrary.getProtocolManager();
    Logger.getGlobal().info("1 "+protocolManager.toString());
                List<Chunk> chunkList = new ArrayList<>();
                chunkBulkPacketConstructor = protocolManager.createPacketConstructor(PacketType.Play.Server.MAP_CHUNK_BULK, 
                                                                                      chunkList);
                protocolManager.addPacketListener(new PacketAdapter(AutoTeleportPlugin.getPluginInstance(),
                                                                    ListenerPriority.NORMAL, 
                                                                    PacketType.Play.Server.MAP_CHUNK_BULK) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (event.getPacketType() == PacketType.Play.Server.MAP_CHUNK_BULK) {
                            //protocolManager.removePacketListeners(AutoTeleportPlugin.getPluginInstance());
        DevUtil.log(2,"Found Map Chunk Bulk package");
                            PacketContainer packet = event.getPacket();
                            StructureModifier<int[]> ints = packet.getIntegerArrays();
        DevUtil.log(2,"x "+arrayToString(ints.read(0)));
        DevUtil.log(2,"y "+arrayToString(ints.read(1)));
                        }
                    }
                }); 
            }
            catch(NoClassDefFoundError e) {
                Logger.getLogger(AtpTarget.class.getName()).log(Level.WARNING, "ProtocolLib is missing.");
                protocolManager = null;
                chunkBulkPacketConstructor=null;
                return;
            }
        }

        private String arrayToString(int[] ints) {
            String result = "";
            for(int i:ints) {
                result= result+" "+i;
            }
            return result;
        }

        public void sendChunks(Player player, Collection<Chunk> chunkList) {
            if(chunkBulkPacketConstructor==null) {
                return;
            }
            PacketContainer chunkPacket = chunkBulkPacketConstructor.createPacket(chunkList);
            try {
                protocolManager.sendServerPacket(player, chunkPacket);
    DevUtil.log(2,"Bulk Package was sent.");
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Cannot send packet " + chunkPacket, e);
            }
        }
    }
    
    public static void init() {
        if(Bukkit.getPluginManager().getPlugin("ProtocolLib")!=null) {
            connector = new ProtocolLibConnection();
        }
        else {
            Logger.getLogger(AtpTarget.class.getName()).log(Level.WARNING, "ProtocolLib plugin is missing.");
        }
    }
    
    public static void sendChunks(Player player, Collection<Chunk> chunkList) {
        if(isInitiated()) {
            connector.sendChunks(player, chunkList);
        }
    }
    
    public static boolean isInitiated() {
        return connector != null;
    }

}