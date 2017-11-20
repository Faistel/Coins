package com.pudimcraft;
import net.milkbowl.vault.economy.Economy;// vault
import net.milkbowl.vault.economy.EconomyResponse;// vault
import com.pudimcraft.Main;
//import com.vk2gpz.tokenenchant.TokenEnchant;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;


import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Vault {
	private static Economy econ = null;// vault
	public Main plugin;
	public Vault(Main main) {
		this.plugin = main;
	}

	
	public boolean VaultWithdraw(double qtd, Player player){
		double money = econ.getBalance(player);
		if(qtd>money){
			player.sendMessage("" + plugin.getConfig().getString("prefix")
					+ "Voce nao tem todo esse dinheiro para depositar");
			return false;
		}
		EconomyResponse r = econ.withdrawPlayer(player, qtd);
		if (r.transactionSuccess()) {
			return true;
		}
		return false;
	}
	
	public boolean VaultDeposit(double qtd, Player player){
		EconomyResponse r = econ.depositPlayer(player, qtd);
		if (r.transactionSuccess()) {
			return true;
		}
		return false;
	}
	
	public boolean SetupEconomy(){
		if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	
	public static Economy getEcononomy() {
		return econ;
	}
}
