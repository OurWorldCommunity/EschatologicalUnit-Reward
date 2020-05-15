package online.smyhw.EschatologicalUnit.Reward;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class smyhw extends JavaPlugin implements Listener 
{
	public static JavaPlugin smyhw_;
	public static Logger loger;
	public static FileConfiguration configer;
	public static List<String> Reward_cmd;
	public static String prefix;
	public static String URL;
	
	@Override
    public void onEnable() 
	{
		
		getLogger().info("EschatologicalUnit.Reward加载");
		getLogger().info("正在加载环境...");
		loger=getLogger();
		saveDefaultConfig();
		configer = getConfig();
		smyhw_ = this;
		getLogger().info("正在加载配置...");
		Reward_cmd = configer.getStringList("config.Reward_cmd");
		prefix = configer.getString("config.prefix");
		URL = configer.getString("config.URL");
		saveConfig();
		getLogger().info("正在注册监听器...");
		Bukkit.getPluginManager().registerEvents(this,this);
		getLogger().info("EschatologicalUnit.Statistics加载完成");
    }

	@Override
    public void onDisable() 
	{
		getLogger().info(prefix+"EschatologicalUnit.Statistics卸载");
    }
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
        if (cmd.getName().equals("euR"))
        {
                if(args.length<1) 
                {
                	sender.sendMessage(prefix+"{参数不足}");
                	return true;
                }
                switch(args[0])
                {
                case"get":
                	if(args.length<2)
                	{
                		sender.sendMessage(prefix+"{参数不足}");
                		sender.sendMessage(prefix+"euR get <对局ID>");
                		return true;
                	}
                	if(configer.getBoolean("data."+sender.getName()))
                	{
                		sender.sendMessage(prefix+"记录数据显示该对局奖励已被该账户领取...");
                		return true;
                	}
                	{
                		new DoGet(args[1],(Player) sender);
                	}
                	return true;
                case"reload":
                    if(!sender.hasPermission("eu.plugin")) 
                    {
                    	sender.sendMessage(prefix+"非法使用 | 使用者信息已记录，此事将被上报");
                    	loger.warning(prefix+"玩家<"+sender.getName()+">试图非法使用指令<"+args+">{权限不足}");
                    	return true;
                    }
                	reloadConfig();
                	configer = getConfig();
                	sender.sendMessage(prefix+"重载配置文件...");
                	return true;
                	
                default:
                	sender.sendMessage(prefix+"{参数错误}");
                }
                
                return true;                                                       
        }
       return false;
	}
	
}

//Bukkit同步方法
class DoGet extends BukkitRunnable
{
	
	String ID;
	GetAPI GT;
	Player PlayerID;
	/**
	 * 
	 * @param ID 战局ID
	 */
	public DoGet(String ID,Player PlayerID)
	{
		this.ID = ID;
		this.GT = new GetAPI(ID);
		this.PlayerID = PlayerID;
		this.runTaskTimer(smyhw.smyhw_,0,20);
	}

	@Override
	public void run() 
	{
		if(this.GT.FIN)
		{
			HashMap data = this.GT.data;
			String Players = (String) data.get("PlayerList");
			String[] Players_ = Players.split(",");
			boolean haved = false;
			for(int i=0;i<Players_.length;i++)
			{
				if(Players_[i].equals(PlayerID.getName()))
				{
					haved = true;
				}
			}
			if(!haved)
			{
				PlayerID.sendMessage(smyhw.prefix+"在对应战局ID中没有查询到该账号的战绩");
				this.cancel();
				return;
			}
			String[] cmds = (String[]) smyhw.Reward_cmd.toArray();
			for(int i=0;i<cmds.length;i++)
			{
				String cmd = cmds[i];
				cmd = cmd.replaceAll("%PlayerID%", PlayerID.getName());
				cmd = cmd.replaceAll("%Wave%", (String)data.get("Wave"));
				cmd = cmd.replaceAll("%Point%", (String)data.get(PlayerID.getName()));
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),cmd);
				PlayerID.sendMessage(smyhw.prefix+"奖励已发放");
			}
			this.cancel();
		}
	}
	
}

//异步方法，Http请求
class GetAPI extends Thread
{
	public boolean FIN = false;
	public HashMap data = new HashMap();
	String ID;
	public GetAPI(String ID)
	{
		this.ID = ID;
		this.start();
	}
	public void run()
	{
		String re = online.smyhw.localnet.lib.WebAPI.get(smyhw.URL+ID+".html");
		data = online.smyhw.localnet.lib.Json.Parse(re);
		FIN=true;
	}
}