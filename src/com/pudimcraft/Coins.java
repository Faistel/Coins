package com.pudimcraft;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Coins implements Listener {
	
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
    	
		double Player_Hash =0;
		String Player_Mining = "";
		try {
			ResultSet rs = plugin.query("SELECT * FROM `coins_players` WHERE `uuid` = '" + event.getPlayer().getUniqueId() + "';");
			if (rs.next()) {
				Player_Hash = rs.getDouble("hashpower");
				Player_Mining = rs.getString("mining");
			}
		} catch (Exception ex) {
		}
		plugin.p_mining.put(event.getPlayer().getUniqueId(), Player_Mining);
		plugin.p_hashpower.put(event.getPlayer().getUniqueId(), Player_Hash);
		plugin.p_trades.put(event.getPlayer().getUniqueId(), plugin.TR.TotalTrade(event.getPlayer()));
    }

	// private MySQL MY_SQL = new
	// MySQL(this.getConfig().getString("ip"),this.getConfig().getString("database"),this.getConfig().getString("username"),this.getConfig().getString("password"));
	private Main plugin;
	public Coins(Main main) {
		this.plugin = main;
		
	}


	


	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Material block = event.getBlock().getType();
		Player player = event.getPlayer();

		if (plugin.orelist.containsKey(block.toString())) {
					String Player_Mining = plugin.p_mining.get(player.getUniqueId());
					String Coin_coin=Player_Mining;
					double Player_hashpower =plugin.p_hashpower.get(player.getUniqueId());

			if (!Player_Mining.equalsIgnoreCase("") && !Player_Mining.equalsIgnoreCase(null)) {
				String Coin_Block = plugin.c_block.get(Player_Mining);
				int Coin_height = plugin.c_height.get(Player_Mining); // verificar
				double Coin_dif = plugin.c_difficulty.get(Player_Mining); // verificar

					if (Coin_Block.equalsIgnoreCase(block.toString())) {
						double currentLevel = player.getInventory().getItemInMainHand()
								.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS); // gets
						if (currentLevel > 0) {
							Player_hashpower = Player_hashpower * ((currentLevel * 0.2) + 1);
						}

						event.setCancelled(true);
						event.getBlock().setType(Material.AIR);
											
						double max_ = Coin_dif;
						int max = (int) max_;
						if (max < 1) {
							max = 1;
						}
						int min = (int) Math.pow(Player_hashpower, 2);
						if (min > max) {
							min = max;
						}

						int valor = generateRandomInteger(1, max);
							
						
						if (valor <= min && min != 0) {
							if(plugin.updateSQL("UPDATE `coins_coins` SET height= height + 1 WHERE `coin`='"+ Coin_coin + "' AND `height`="+Coin_height+""))
							{
								player.sendMessage("" + plugin.getConfig().getString("prefix") + "§aVoce descobriu um novo bloco de §6"+Coin_coin+".");
								plugin.NB.NewBlock(player, Coin_coin);
							}
							
						}else{
							player.sendMessage(""+Coin_coin+" range: "+min+"-"+max+" valor:"+valor+" §a"+calculoPor(valor,min,max)+"%");
						//	player.sendMessage("" + plugin.getConfig().getString("prefix")+"§a"+calculoPor(valor,min,max)+"%");
						}

					}

			}
		}
		}

	

	public static int generateRandomInteger(int min, int max) {
		SecureRandom rand = new SecureRandom();
		rand.setSeed(new Date().getTime());
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
	
	public double calculoPor(double valor, int min, int max){
		double D=(valor-min);
		if(D>0 && max!=min){
			D=plugin.formatDouble2c(100-(D*100/(max-min)));
		}else{
			if(valor<=min){
				D=100;
			}else{
				D=0;
			}
		}
		return D;
	}
	
	public void DropBlock(){
	/*	Location location = event.getBlock().getLocation();
		Block block2 = event.getBlock();
		World world = block2.getWorld();
		ItemStack is = new ItemStack(Material.BOOK, 1);
		is.setDurability((short) 99999);
		
	
		ItemMeta isim = is.getItemMeta();
		isim.setLore("NOME");
		int Coin_heigh_2=Coin_height+1;
		isim.setDisplayName("§6"+Coin_coin+" §b#"+Coin_heigh_2+"");
		
		List lista = new ArrayList();
		lista.add("§6"+Coin_coin+"");
		lista.add("§b#"+Coin_heigh_2+"");
		
		isim.setLore(lista);
		is.setItemMeta(isim); 
		world.dropItem(location,is);
		*/
	}



}
