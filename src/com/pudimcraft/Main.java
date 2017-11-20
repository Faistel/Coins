package com.pudimcraft;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.pudimcraft.Commands.Sell;



public class Main extends JavaPlugin {
	
	Mining NB = new Mining(this);
	Wallet WT = new Wallet(this);
	Trade TR = new Trade(this);
	PlayerInfo PI = new PlayerInfo(this);
	PlayerMessage PM = new PlayerMessage(this);
	Vault VT = new Vault(this);
	
	Sell SELL = new Sell(this);
	
	public final Logger logger = Logger.getLogger("Minecraft");

	//public static Main plugin;

	public static Map<String, String> c_block = new HashMap<String, String>();
	public static Map<String, Double> c_difficulty = new HashMap<String, Double>();
	public static Map<String, Integer> c_height = new HashMap<String, Integer>();
	
	public static Map<UUID, String> p_mining = new HashMap<UUID, String>();
	public static Map<UUID, Double> p_hashpower = new HashMap<UUID, Double>();
	public static Map<UUID, Integer> p_trades = new HashMap<UUID, Integer>();
	
	
	//public static Map<String, Double> c_height2 = new HashMap<String, Double>();
	
	public static Map<String, Boolean> bloquear = new HashMap<String, Boolean>();
	
	//private static Economy econ = null;// vault
	String HOST = this.getConfig().getString("ip");
	String DATABASE = this.getConfig().getString("database");
	String USER = this.getConfig().getString("username");
	String PASSWORD = this.getConfig().getString("password");
	int PORT = this.getConfig().getInt("port");

	private Connection con;

	@Override
	public void onEnable() {
		connect();
		update("CREATE TABLE IF NOT EXISTS coins_players ( `uuid` VARCHAR(36) NOT NULL , `money` double(23, 8) NOT NULL, `money_blocked` double(23, 8) NOT NULL, `hashpower` double(23, 8) NOT NULL, `trades` double(23, 8) NOT NULL , `mining` varchar(10) NOT NULL, PRIMARY KEY (`uuid`));");
		update("CREATE TABLE IF NOT EXISTS coins_orders (   `id` int(11) NOT NULL AUTO_INCREMENT,  `coin` varchar(10) NOT NULL,  `price` double(23, 8) NOT NULL,  `type` varchar(10) NOT NULL,  `amount` double(23, 8) NOT NULL,  `uuid` varchar(36) NOT NULL , PRIMARY KEY (`id`));");
		update("CREATE TABLE IF NOT EXISTS coins_transactions (   `id` int(11) NOT NULL AUTO_INCREMENT,  `coin` varchar(10) NOT NULL,  `amount` double(23, 8) NOT NULL, `fee` double(23, 8),  `O_uuid` varchar(36) NOT NULL,  `R_uuid` varchar(36) NOT NULL,  `C_uuid` varchar(36) NOT NULL , PRIMARY KEY (`id`));");
		update("CREATE TABLE IF NOT EXISTS coins_history (   `id` int(11) NOT NULL AUTO_INCREMENT,  `coin` varchar(10) NOT NULL,  `amount` double(23, 8) NOT NULL,  `total` double(23, 8) NOT NULL,  `O_uuid` varchar(36) NOT NULL,  `R_uuid` varchar(36) NOT NULL, PRIMARY KEY (`id`));");
		update("CREATE TABLE IF NOT EXISTS coins_coins ( `id` INT NOT NULL AUTO_INCREMENT , `coin` VARCHAR(10) NOT NULL , `name` VARCHAR(20) NOT NULL , `height` double(23, 8) NOT NULL , `mining` double(23, 8) NOT NULL, `difficulty` double(23, 8) NOT NULL, `size` int(11) , `time_blocks` double(23, 8) NOT NULL, `last_time_blocks` INT(32) NOT NULL , `avg_time_blocks` double(23, 8) NOT NULL, `limit` double(23, 8) NOT NULL, `block` varchar(20) NOT NULL, `quantity` double(23, 8) NOT NULL, `halving` double(23, 8) NOT NULL, `halving_n` double(23, 8) NOT NULL, `fee` double(23, 8) NOT NULL, `last_price` double(23, 8) NOT NULL, `last_price_a` double(23, 8) NOT NULL, `last_price_h` double(23, 8) NOT NULL, `last_price_l` double(23, 8) NOT NULL, `capitalization` double(23, 8) NOT NULL, `vol_c` double(23, 8) NOT NULL, `vol_m` double(23, 8) NOT NULL, `trades` double(23, 8) NOT NULL, `trades_24hr` double(23, 8) NOT NULL, `date` DATE NOT NULL, `creation_date` DATE NOT NULL, `uuid` VARCHAR(36) NOT NULL , PRIMARY KEY (`id`));");
		update("CREATE TABLE IF NOT EXISTS coins_wallet (   `id` int(11) NOT NULL AUTO_INCREMENT,  `coin` varchar(10) NOT NULL,  `amount` double(23, 8) NOT NULL,  `amount_blocked` double(23, 8) NOT NULL,  `uuid` varchar(36) NOT NULL, PRIMARY KEY (`id`));");


		
		if (!VT.SetupEconomy()) {
			logger.severe(
					String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		this.saveDefaultConfig();
		
		ConsoleCommandSender b = Bukkit.getConsoleSender();
		b.sendMessage(this.getConfig().getString("prefix") + this.getConfig().getString("onEnable"));
		
		String Coin_COIN = null;
		String Coin_Block = null;
		double Coin_difficulty = 0;
		int Coin_height = 0;
		try {
			ResultSet rs =query("SELECT * FROM `coins_coins`");
			while(rs.next()) {
				Coin_COIN = rs.getString("coin");
				Coin_Block = rs.getString("block");
				Coin_difficulty = rs.getDouble("difficulty");
				Coin_height = rs.getInt("height");
				
				c_block.put(Coin_COIN, Coin_Block);
				c_difficulty.put(Coin_COIN, Coin_difficulty);
				c_height.put(Coin_COIN, Coin_height);
				//c_height2.put(Coin_COIN, Coin_height);
	
			}
		} catch (Exception ex) {
		}
	
		LoadEventos();
		
		
		//getCommand("coins").setExecutor(new teste());
		
		
		/*
		 * MY_SQL.HOST=this.getConfig().getString("ip");
		 * MY_SQL.DATABASE=this.getConfig().getString("database");
		 * MY_SQL.USER=this.getConfig().getString("username");
		 * MY_SQL.PASSWORD=this.getConfig().getString("password");
		 */

		// update("CREATE TABLE IF NOT EXISTS xp_converter ( `uuid` VARCHAR(50)
		// NOT NULL , `ativado` INT NOT NULL , PRIMARY KEY (`uuid`));");
		bloquear.put("order", false);
		bloquear.put("trade", false);
		
		for (int i = 0; i < getConfig().getStringList("Blocks").size(); i++) {
			orelist.put(getConfig().getStringList("Blocks").get(i), null);
		}
	}

	public static Map<String, String> orelist = new HashMap<String, String>();


	public boolean ehDouble(String str){
	    if( str == null ) return false;
	    try {
	        Double.parseDouble(str);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (cmd.getName().equalsIgnoreCase("coins")) {
	        if (!(sender instanceof Player)) {
	            sender.sendMessage("§cUse esse comando no jogo.");
	            return false;
	        }
	        
			for (int i = 0; i < args.length; i++) {
				args[i] = args[i].replace(",", ".");
			}


			Player player = (Player) sender;
			// args[0] = args[0].replace("/", "");

			if ((sender instanceof Player)) {
				if (args.length == 4) {

			/*		if (args[0].equalsIgnoreCase("sell") && !bloquear.get("trade") && !bloquear.get("order")) {
						if (isNumeric(args[1]) && isNumeric(args[3])) {
							if (sender.hasPermission("coins.sell") || sender.hasPermission("coins.player")) {
								if (verificarCoins(args[2],player) == true) {
									
									if(!plugin.bloquear.get("order")){
										plugin.bloquear.put("order", true);
									}
									
								
									double qtd = Double.parseDouble(args[1]);
									double val = Double.parseDouble(args[3]);
									
									if (qtd > 0 && val >= 0 && SzNum(val) && SzNum(qtd)) {
										if(TR.AddTrade(player.getUniqueId())){
											// if para bloquear valor
											if(WT.Wallet(args[1], 0, 2, 0, player.getUniqueId(),player.getUniqueId().toString(),qtd,false)){
											if(PI.PlayerInfo(0,0, 0,null, player.getUniqueId(),false)){ //incrementar o trade
											if(updateSQL("UPDATE `coins_orders` SET `amount` = amount + '"+qtd+"' WHERE `uuid` = '" + player.getUniqueId() + "' AND `price` = '"+val+"' AND `type` = 'SELL'")==false){
												
												if(updateSQL("INSERT INTO `coins_orders` (`id`, `coin`, `price`, `type`, `amount`, `uuid`) VALUES (NULL, '"
													+ args[1].toUpperCase() + "', " + args[3] + ", 'SELL', " + args[2]
													+ ", '" + player.getUniqueId() + "')")){
													plugin.PM.PlayerMessage(player, null, true, null ,"Sales_Order");
												}
											}else{
												plugin.PM.PlayerMessage(player, null, true, null ,"Updated_Sales_Order");
											}
											
											int ID_ORDER = 0;
											try {
												ResultSet rs = query("SELECT * FROM `coins_orders` WHERE `coin` = '"
														+ args[1] + "' AND `amount`=" + qtd + " AND `price`=" + val
														+ " AND `uuid`='" + player.getUniqueId() + "'LIMIT 1;");
												if (rs.next()) {
													ID_ORDER = rs.getInt("id");
												}
											} catch (Exception ex) {
											}
											if (ID_ORDER > 0) {
												TR.ordersEx(ID_ORDER);
											}
											}
										}
									}
									} else {
										plugin.PM.PlayerMessage(player, null, true, null ,"Syntax_Error");
									}
								}
							} else {
								plugin.PM.PlayerMessage(player, null, true, null ,"NoPerm");
							}
						} else {
							plugin.PM.PlayerMessage(player, null, true, null ,"Syntax_Error");
						}
						plugin.bloquear.put("order", false);
					}
					*/
					PM.SendMessage(player, null, true, player.toString() ,null);
					SELL.Sell(player, args);

					if (args[0].equalsIgnoreCase("buy") && !bloquear.get("trade") && !bloquear.get("order")) {
						if (isNumeric(args[1]) && isNumeric(args[3])) {
							if (sender.hasPermission("coins.sell") || sender.hasPermission("coins.player")) {
								if (verificarCoins(args[2],player) == true) {
									if(!bloquear.get("order")){
										bloquear.put("order", true);
									}
									double qtd = Double.parseDouble(args[1]);
									double val = Double.parseDouble(args[3]);
									String coin = args[2].toUpperCase() ;
									
									if (qtd >= 0 && val >= 0 && SzNum(val) && SzNum(qtd)) {
										if(PI.PlayerInfo(0,qtd*val, 0,null, player.getUniqueId(),false)){
											
											if(updateSQL("UPDATE `coins_orders` SET `amount` = amount + '"+qtd+"' WHERE `uuid` = '" + player.getUniqueId() + "' AND `price` = '"+val+"' AND `type` = 'BUY'")==false){
												if(updateSQL("INSERT INTO `coins_orders` (`id`, `coin`, `price`, `type`, `amount`, `uuid`) VALUES (NULL, '"
													+ coin+ "', " + val + ", 'BUY', " + qtd
													+ ", '" + player.getUniqueId() + "')")){
													PM.SendMessage(player, null, true, null ,"Purchase_Order");
												}
											}else{
												PM.SendMessage(player, null, true, null ,"Updated_Purchase_Order");
											}
											
	
											int ID_ORDER = 0;
											try {
												ResultSet rs = query("SELECT * FROM `coins_orders` WHERE `coin` = '"
														+ coin + "' AND `amount`=" + qtd + " AND `price`=" + val
														+ " AND `uuid`='" + player.getUniqueId() + "'LIMIT 1;");
												if (rs.next()) {
													ID_ORDER = rs.getInt("id");
												}
											} catch (Exception ex) {
											}
											if (ID_ORDER > 0) {
													//Coins.p_trades.put(player.getUniqueId(), Coins.p_trades.get(player.getUniqueId())+1 );
													TR.AddTrade(player.getUniqueId());
													TR.ordersEx(ID_ORDER);
												// player.sendMessage(""+this.getConfig().getString("prefix")+"chamou");
											}
									}
									} else {
										PM.SendMessage(player, null, true, null ,"Syntax_Error");
									}
								}
							} else {
								PM.SendMessage(player, null, true, null ,"NoPerm");
							}
						}
						bloquear.put("order", false);
					}
				}
				
				if (args.length == 2 || args.length == 4) {

					if (args[0].equalsIgnoreCase("myorders")) {			
							if (sender.hasPermission("coins.myorders") || sender.hasPermission("coins.player")) {
								if(verificarCoins(args[1],player)){
									int erro=0;
									String coin=args[1];
									int limite=10;
									int QTD_MOEDAS=0;
									int page_atual=1;
									
									if(args.length == 4){
										if(args[2].equalsIgnoreCase("page")){
											if(EhInteiro(args[3])){
												int page_n=Integer.parseInt(args[3]);
												if(page_n>0){												
													if(args.length == 4){
														page_atual=Integer.parseInt(args[3]);
													}else{
														page_atual=1;
													}													
												}
											}else{
												erro=1;
											}
										}else{
											erro=1;
										}
									}
									if(erro==0){
										
										try {
											ResultSet rs_soma = query("SELECT COUNT(id) FROM `coins_orders` WHERE `uuid`='" + player.getUniqueId() + "' AND  `coin`='" + coin + "'");
											if (rs_soma.next()) {
												QTD_MOEDAS = rs_soma.getInt(1);
											}
										} catch (Exception ex) {
										}
										
										
										int QTD_PAGE = QTD_PAGE(QTD_MOEDAS,limite);
										if(page_atual>QTD_PAGE){
											page_atual=QTD_PAGE;
										}
		
										
											double price = 0;
											double amount=0;
											int id_order=0;
											String type="";
											
											String COIN_COIN="";
											int cont=0;
											
											player.sendMessage(
													"" + this.getConfig().getString("prefix") + "§3"+p_trades.get(player.getUniqueId())+" Ordens §fPagina "+page_atual+"§3/§f"+QTD_PAGE+":");
												try {
													ResultSet rs= query("SELECT * FROM `coins_orders`  WHERE `uuid`='" + player.getUniqueId() + "' AND  `coin`='" + coin +"' ORDER BY amount DESC LIMIT "+(limite*(page_atual-1))+","+limite+"");
													//ResultSet rs= query("SELECT * FROM `coins_coins` ORDER BY capitalization DESC LIMIT 0,10");
													while (rs.next()) {
														cont+=1;
														COIN_COIN = rs.getString("coin");
														type = rs.getString("type");
														price = rs.getDouble("price");
														amount = rs.getDouble("amount");
														id_order = rs.getInt("id");
														
														String Cor="§7";
														if(type.equalsIgnoreCase("SELL")){
															Cor="§a";
														}
														if(type.equalsIgnoreCase("BUY")){
															Cor="§c";
														}
														
														player.sendMessage( "§7"+((cont)+(limite*(page_atual-1)))+" §7- ID: §d"+id_order+" §7|"+Cor+""+type+" §e"+amount+" §6"+COIN_COIN+" §a$"+price+"");
													}
												} catch (Exception ex) {
												}
										
								}
							}
							} else {
								PM.SendMessage(player, null, true, null ,"NoPerm");
							}
						}
					}
				
				if (args.length == 1 || args.length == 3) {

					if (args[0].equalsIgnoreCase("list")) {			
							if (sender.hasPermission("coins.list") || sender.hasPermission("coins.player")) {
								
								int erro=0;
								
								int limite=10;
								int QTD_MOEDAS=0;
								int page_atual=1;
								
								if(args.length == 3){
									if(args[1].equalsIgnoreCase("page")){
										if(EhInteiro(args[2])){
											int page_n=Integer.parseInt(args[2]);
											if(page_n>0){												
												if(args.length == 3){
													page_atual=Integer.parseInt(args[2]);
												}else{
													page_atual=1;
												}													
											}
										}else{
											erro=1;
										}
									}else{
										erro=1;
									}
								}
								if(erro==0){
									
									try {
										ResultSet rs_soma = query("SELECT COUNT(id) FROM `coins_coins`");
										if (rs_soma.next()) {
											QTD_MOEDAS = rs_soma.getInt(1);
										}
									} catch (Exception ex) {
									}
									
									
									int QTD_PAGE = QTD_PAGE(QTD_MOEDAS,limite);
									if(page_atual>QTD_PAGE){
										page_atual=QTD_PAGE;
									}
	
									
										double capitalization = 0;
										String COIN_COIN="";
										int cont=0;
										player.sendMessage(
												"" + this.getConfig().getString("prefix") + "§3Moedas §fPagina "+page_atual+"§3/§f"+QTD_PAGE+":");
											try {
												ResultSet rs= query("SELECT * FROM `coins_coins` ORDER BY capitalization DESC LIMIT "+(limite*(page_atual-1))+","+limite+"");
												//ResultSet rs= query("SELECT * FROM `coins_coins` ORDER BY capitalization DESC LIMIT 0,10");
												while (rs.next()) {
													cont+=1;
													COIN_COIN = rs.getString("coin");
													capitalization = rs.getDouble("capitalization");
													player.sendMessage( "§5"+((cont)+(limite*(page_atual-1)))+" §7- §6"+COIN_COIN+" §a$"+capitalization+"");
												}
											} catch (Exception ex) {
											}
										
								}
					
							} else {
								PM.SendMessage(player, null, true, null ,"NoPerm");
							}
						}
					}

				if (args.length == 4 || args.length == 5) {
					if (args[0].equalsIgnoreCase("transfer") || sender.hasPermission("coins.player")) {
						if (isNumeric(args[2]) && (isNumeric(args[3]) || args.length == 4)) {
							double qtd = Double.parseDouble(args[2]);
							double fee = 0;
							if (sender.hasPermission("coins.transfer") == true) {

								if (verificarCoins(args[1],player) == true) {
									if (qtd > 0 && fee >= 0 && SzNum(fee) && SzNum(qtd)) {

										Player p_REC = null;
										if (args.length == 5) {
											p_REC = Bukkit.getPlayer(args[4]);
											fee = Double.parseDouble(args[3]);
										} else {
											p_REC = Bukkit.getPlayer(args[3]);
											fee = 0;
										}

										int EnvMsg = 1;
										if (p_REC != null) {

											String R_uuid = p_REC.getUniqueId().toString();

											double Coin_FEE = 0;
											try {
												ResultSet rs = query("SELECT * FROM `coins_coins` WHERE `coin` = '"
														+ args[1] + "' LIMIT 1;");
												if (rs.next()) {
													Coin_FEE = rs.getDouble("fee");
												}
											} catch (Exception ex) {
											}
											//double fee_total = formatDouble((1-(Coin_FEE/100))*qtd + fee);
											
											double FeeTotal = WT.FeeTotal(qtd, Coin_FEE + fee);

											if (WT.Wallet(args[1], -qtd, 2, fee, player.getUniqueId(), R_uuid,0,true)) { // player.getUniqueId().toString()
												
												if(updateSQL("INSERT INTO `coins_transactions` (`id`, `coin`, `amount`, `fee`, `O_uuid`, `R_uuid`, `C_uuid`) VALUES (NULL, '"
														+ args[1].toUpperCase() + "', " + args[2] + ", " + FeeTotal
														+ ", '" + player.getUniqueId() + "', '" + R_uuid + "', '')")){
												PM.SendMessage(player, null, true, null ,"Transfer_Created");
												PM.SendMessage(null, UUID.fromString(R_uuid), true, "Foi registrado uma transferencia de §a"+args[2]+" §6"+args[1].toUpperCase()+" §7para a sua carteira.",null);
												}
											}
										} else {
											PM.SendMessage(player, null, true, null ,"Player_Not_Found");
										}

									} else {
										PM.SendMessage(player, null, true, null ,"Syntax_Error");
									}
								}
							} else {
								PM.SendMessage(player, null, true, null ,"NoPerm");
							}
						}
					}
				}
				
				
				
				if (args.length == 4 ) {
					if (args[0].equalsIgnoreCase("transfer") && args[2].equalsIgnoreCase("addfee")) {
						if (sender.hasPermission("coins.transfer") == true) {
						if (EhInteiro(args[1]) && (isNumeric(args[3]))) {
							int id=Integer.parseInt(args[1]);
							double qtd_fee=Double.parseDouble(args[3]);
							String Coin_coin = "";

							try {
								ResultSet rs = query("SELECT * FROM `coins_transactions` WHERE `id` = "+id+" LIMIT 1;");
								if (rs.next()) {
									Coin_coin = rs.getString("coin");
								}
							} catch (Exception ex) {
							}
							if(Coin_coin!=""){
								if(WT.Wallet(Coin_coin, -qtd_fee, 0, 0, player.getUniqueId(),player.getUniqueId().toString(),0,false)){
									if(updateSQL("UPDATE `coins_transactions` SET `fee` = fee + "+qtd_fee+" WHERE `coins_transactions`.`id` = "+id+" AND `C_uuid` ='' AND `O_uuid` ='"+player.getUniqueId()+"' ")){
										player.sendMessage("" + this.getConfig().getString("prefix") + "§aSua transferencia foi atualizada.");
									}else{
										WT.Wallet(Coin_coin, qtd_fee, 0, 0, player.getUniqueId(),player.getUniqueId().toString(),0,false);
										player.sendMessage("" + this.getConfig().getString("prefix") + "§cErro ao atualizar sua transferencia.");
									}
								}
							}

						} else {
								player.sendMessage("" + this.getConfig().getString("prefix") + "Erro de sintaxe.");
						}
								
					} else {
								player.sendMessage(
										this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
					}
				}
			}
						
					
				

				if (args.length == 3) {
					if (isNumeric(args[2])) {
						if (args[0].equalsIgnoreCase("wallet") && args[1].equalsIgnoreCase("deposit")) {
							if (sender.hasPermission("coins.deposit") == true) {
								double qtd = Double.parseDouble(args[2]);
								if (qtd > 0) {
										if (VT.VaultWithdraw(qtd, player)) {
											PI.PlayerInfo(qtd,0, 0, null, player.getUniqueId(),true);
										}								
								}
							} else {
								player.sendMessage(
										this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
							}
						}
					}

					if (args[0].equalsIgnoreCase("wallet") && args[1].equalsIgnoreCase("withdraw")) {
						if (sender.hasPermission("coins.withdraw") == true) {
							if (isNumeric(args[2])) {
								double qtd = Double.parseDouble(args[2]);
								if (qtd > 0) {
									if (PI.PlayerInfo(-qtd,0, 0, null, player.getUniqueId(),true) == true) {
										VT.VaultDeposit(qtd, player);
									}
								}
							} else {
								player.sendMessage(
										this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
							}
						}
					}
				}

				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("mining")) {
						if (sender.hasPermission("coins.mining") || sender.hasPermission("coins.player")) {
							if (verificarCoins(args[1],player)) {
								PI.PlayerInfo(0,0, 0, args[1], player.getUniqueId(),true);
							}
						} else {
							player.sendMessage(
									this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
						}
					}
				}

				if (args.length == 3) {
					if (args[0].equalsIgnoreCase("cancel") && args[1].equalsIgnoreCase("order")) {
						if (sender.hasPermission("coins.cancel") || sender.hasPermission("coins.player")) {
							if (EhInteiro(args[2]) == true) {
								double id_order = Double.parseDouble(args[2]);
								if (isNumeric(args[2]) == true && id_order > 0) {

									int result = 0;
									String Coin_uuid = "";
									String Coin_coin = "";
									String Coin_type = "";
									double Coin_amount = 0;
									double Coin_price=0;
									try {
										ResultSet rs = query("SELECT * FROM `coins_orders` WHERE `id` = " + id_order
												+ " AND `uuid`= '" + player.getUniqueId() + "' LIMIT 1;");
										if (rs.next()) {
											result = rs.getInt("id");
											Coin_uuid = rs.getString("uuid");
											Coin_coin = rs.getString("coin");
											Coin_amount = rs.getDouble("amount");
											Coin_price = rs.getDouble("price");
											Coin_type = rs.getString("type");
										}
									} catch (Exception ex) {
									}

									if (result == id_order) {
										if(updateSQL("DELETE FROM `coins_orders` WHERE `id` = " + id_order + " AND `uuid`= '"
												+ player.getUniqueId() + "' AND `amount`="+Coin_amount+"")){
											//Coins.p_trades.put(player.getUniqueId(), Coins.p_trades.get(player.getUniqueId())-1 );
											TR.RemTrade(player.getUniqueId());
											if(Coin_type.equalsIgnoreCase("BUY")){
												if(PI.PlayerInfo(0,-Coin_price*Coin_amount, 0, null, player.getUniqueId(),false)){
												player.sendMessage("" + this.getConfig().getString("prefix") + "Ordem de compra cancelada");
												}
											}
											if(Coin_type.equalsIgnoreCase("SELL")){
												if(WT.Wallet(Coin_coin, 0, 2, 0, player.getUniqueId(),player.getUniqueId().toString(),-Coin_amount,false)){
												player.sendMessage("" + this.getConfig().getString("prefix") + "Ordem de venda cancelada");
												}
											}
										}


									} else {
										player.sendMessage("" + this.getConfig().getString("prefix")
												+ "Voce não pode cancelar essa ordem.");
									}
								} else {
									player.sendMessage("" + this.getConfig().getString("prefix") + "Erro de sintaxe.");
								}
							}

						} else {
							player.sendMessage(
									this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
						}
					}
				}

				if (args.length == 10) {
					if (args[0].equalsIgnoreCase("create")) {
						if (sender.hasPermission("coins.create") == true) {
							if (isNumeric(args[3]) && isNumeric(args[4]) && isNumeric(args[6]) && isNumeric(args[7])
									&& isNumeric(args[8]) && isNumeric(args[9])) {
								double equation = Double.parseDouble(args[3]);
								double limit = Double.parseDouble(args[4]);
								double quantity = Double.parseDouble(args[6]);
								double halving = Double.parseDouble(args[7]);
								double fee = Double.parseDouble(args[8]);
								int size =  Integer.parseInt(args[9]);
								int result = 0;
								args[5] = args[5].toUpperCase();

								String Coin_Name = "";
								try {
									ResultSet rs = query(
											"SELECT * FROM `coins_coins` WHERE `coin` = '" + args[1] + "' LIMIT 1;");
									if (rs.next()) {
										result = rs.getInt("id");
										Coin_Name = rs.getString("coin");
									}
								} catch (Exception ex) {
								}

								int result2 = 0;
								String Coin_Name2 = "";
								try {
									ResultSet rs = query(
											"SELECT * FROM `coins_coins` WHERE `name` = '" + args[2] + "' LIMIT 1;");
									if (rs.next()) {
										result2 = rs.getInt("id");
										Coin_Name2 = rs.getString("name");
									}
								} catch (Exception ex) {
								}

								if (verificarCoins(args[1],null) == true || args[2].equalsIgnoreCase(Coin_Name2) == true) {
									player.sendMessage("" + this.getConfig().getString("prefix")
											+ "Ja existe uma moeda com esse nome");
								} else {
									if (orelist.containsKey(args[5])) {
										if (args[1].length() < 6 && args[2].length() < 15) {
											if (equation > 0 && limit > 0 && quantity > 0 && halving > 0 && fee >= 0
													&& fee <= 100) {
												
												if(updateSQL("INSERT INTO `coins_coins` (`id`, `coin`, `name`,`height`, `mining`, `difficulty`, `size`, `time_blocks`, `last_time_blocks`,`avg_time_blocks`, `limit`, `block`, `quantity`, `halving`, `halving_n`, `fee`, `last_price`, `last_price_a`, `last_price_h`, `last_price_l`, `capitalization`, `vol_c`, `vol_m`, `trades`, `trades_24hr`, `date`, `creation_date`, `uuid`) VALUES (NULL, '"
														+ args[1].toUpperCase() + "', '" + args[2] + "',0, 0, 1,"+size+"," + args[3]
														+ ",0,"+args[3]+", " + args[4] + ", '" + args[5] + "', " + args[6] + ", "
														+ args[7] + "," + 0 + ", " + args[8]
														+ ",0,0,0,0,0,0,0,0,0,'2000-01-01','"+Data()+"','" + player.getUniqueId() + "');")){

												player.sendMessage("" + this.getConfig().getString("prefix")
														+ "Moeda criada com sucesso.");
												
												c_block.put(args[1].toUpperCase(), args[6]);
												c_difficulty.put(args[1].toUpperCase(), equation);
												c_height.put(args[1].toUpperCase(), 0);
												}
											} else {
												player.sendMessage(
														"" + this.getConfig().getString("prefix") + "Erro de sintaxe.");
											}
										} else {
											player.sendMessage("" + this.getConfig().getString("prefix")
													+ "Nome da moeda muito grande.");
										}
									} else {
										player.sendMessage(
												"" + this.getConfig().getString("prefix") + "Bloco invalido.");
									}
								}
							}
						} else {
							player.sendMessage(
									this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
						}
					}

				}

				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("create") && args[1].equalsIgnoreCase("help")) {
						player.sendMessage(this.getConfig().getString("prefix") + "§6CREATE HELP");
						player.sendMessage(
								"§c/COINS CREATE §b[§e1§b] [§e2§b] [§e3§b] [§e4§b] [§e5§b] [§e6§b] [§e7§b] [§e8§b] [§e9§b]");
						player.sendMessage("§e[1] §a- Simbolo §e[§bLETRAS§e]");
						player.sendMessage("§e[2] §a- Nome §e[§bLETRAS§e]");
						player.sendMessage("§e[3] §a- Tempo entre blocos (segundos) §e[§bx>0§e]");
						player.sendMessage("§e[4] §a- Limite de moedas §e[§bx>0§e]");
						player.sendMessage("§e[5] §a- Bloco §e[§bDIAMOND_ORE§e]");
						player.sendMessage("§e[6] §a- Moedas por bloco §e[§bx>0§e]");
						player.sendMessage("§e[7] §a- Dividir por 2 a cada x moedas §e[§bx>0§e]");
						player.sendMessage("§e[8] §a- Taxa §e[§b%§e][§bx>=0§e]");
						player.sendMessage("§e[9] §a- Tamanho do bloco §e[§b%§e][§bx>=0§e]");
					}
				}

				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("info")) {
						if (sender.hasPermission("coins.info" ) || sender.hasPermission("coins.player")) {
							if (verificarCoins(args[1],player) == true) {
								String Coin_Block = null;
								String Coin_name = null;
								String Coin_COIN = null;
								String Coin_uuid = null;
								String Coin_C_Data = null;
								double Coin_difficulty = 0;
								double Coin_quantidade = 0;
								double Coin_ming = 0;
								double Coin_limit = 0;
								double Coin_fee = 0;
								double Coin_halving = 0;
								double Coin_last_price = 0;
								double Coin_last_price_a = 0;
								double Coin_last_price_h = 0;
								double Coin_last_price_l = 0;
								double Coin_abertura = 0;
								double Coin_trades = 0;
								double Coin_trades_24hr = 0;
								double Coin_vol_m = 0;
								double Coin_vol_c = 0;
								double Change = 0;
								double Coin_TIME_AVG=0;
								int Coin_altura=0;
								try {
									ResultSet rs = query("SELECT * FROM `coins_coins` WHERE `coin` = '"
											+ args[1].toUpperCase() + "' LIMIT 1;");
									if (rs.next()) {
										Coin_Block = rs.getString("block");
										Coin_uuid = rs.getString("uuid");
										Coin_name = rs.getString("name");
										Coin_COIN = rs.getString("coin");
										Coin_C_Data = rs.getString("creation_date");
										Coin_difficulty = rs.getDouble("difficulty");
										Coin_quantidade = rs.getDouble("quantity");
										Coin_ming = rs.getDouble("mining");
										Coin_limit = rs.getDouble("limit");
										Coin_fee = rs.getDouble("fee");
										Coin_halving = rs.getDouble("halving");
										Coin_last_price = rs.getDouble("last_price");
										Coin_last_price_a = rs.getDouble("last_price_a");
										Coin_last_price_h = rs.getDouble("last_price_h");
										Coin_last_price_l = rs.getDouble("last_price_l");
										Coin_trades = rs.getDouble("trades");
										Coin_trades_24hr = rs.getDouble("trades_24hr");
										Coin_vol_m = rs.getDouble("vol_m");
										Coin_vol_c = rs.getDouble("vol_c");
										Coin_abertura = rs.getDouble("last_price_a");
										Coin_TIME_AVG = rs.getDouble("avg_time_blocks");
										Coin_altura = rs.getInt("height");

									}
								} catch (Exception ex) {
								}

								String COR = null;
								if (Coin_last_price_a != 0) {
									Change = formatDouble(((Coin_last_price / Coin_last_price_a) - 1) * 100);
								}
								if (Change >= 0) {
									COR = "§a+";
								} else {
									COR = "§c";
								}

								double Coin_MIN_SELL = 0;
								try {
									ResultSet rs = query("SELECT * FROM `coins_orders` WHERE `coin` = '" + Coin_COIN
											+ "' AND `type` = 'SELL' AND `price`= (SELECT MIN(price) FROM `coins_orders`  WHERE `type` = 'SELL' AND `coin` = '" + Coin_COIN+"')");
									if (rs.next()) {
										Coin_MIN_SELL = rs.getDouble("price");
									}
								} catch (Exception ex) {
								}
								double Coin_MAX_BUY = 0;
								try {
									ResultSet rs = query("SELECT * FROM `coins_orders` WHERE `coin` = '" + Coin_COIN
											+ "' AND `type` = 'BUY' AND `price`= (SELECT MAX(price) FROM `coins_orders` WHERE `type` = 'BUY' AND `coin` = '" + Coin_COIN+"')");
									if (rs.next()) {
										Coin_MAX_BUY = rs.getDouble("price");
									}
								} catch (Exception ex) {
								}

								player.sendMessage("§b" + this.getConfig().getString("prefix") + "§7INFORMAÇOES §6" + Coin_COIN
										+ "");
								player.sendMessage("§3Criador: §7" + Coin_uuid + "");
								player.sendMessage("§3Simbolo: §6" + Coin_COIN + " §3Criado em: §b"+Coin_C_Data+"");
								player.sendMessage("§3Nome: §6" + Coin_name + "");
								player.sendMessage("§3Bloco: §c" + Coin_Block + "");
								player.sendMessage("§3Dificuldade: §d" + Coin_difficulty + "");
								player.sendMessage("§3Tempo entre blocos: §d" + formatDouble2c(Coin_TIME_AVG/60) + " §7minutos");
								player.sendMessage("§3Qtd. Minerado: §7" + Coin_ming + " §6" + Coin_COIN + "");
								player.sendMessage("§3Ultimo Bloco: §7#" + Coin_altura +"");
								player.sendMessage("§3Limite de moedas: §7" + Coin_limit + "");
								player.sendMessage(
										"§3Recompensa: §7" + Coin_quantidade + " §6" + Coin_COIN + " §7por §7bloco");
								player.sendMessage("§3Taxa: §7" + Coin_fee +" §6"+ Coin_COIN +" §7por bloco");
								player.sendMessage("§3Halving: §7" + Coin_halving + " §6" + Coin_COIN + "");

								//player.sendMessage("§3Halving: §7" + Coin_halving + " §6" + Coin_COIN + "");
								player.sendMessage(
										"§3Capitalizaçao: §a$" + formatDouble(Coin_last_price * Coin_ming) + "");
								player.sendMessage("");
								player.sendMessage("§324hr Info:");
								player.sendMessage("§3Trades: §7"+Coin_trades_24hr+"");
								player.sendMessage("§3Preço de mercado: §a$" + Coin_last_price + " " + COR
										+ "" + Change + "%");
								player.sendMessage("§3Volume de troca: §7" + Coin_vol_c +" §6"+Coin_COIN+" §7/ §a$" + Coin_vol_m+"");
								player.sendMessage("§3Abertura §7$"+Coin_abertura+" §cAlta: §7$" + Coin_last_price_h + " §9Baixa: §7$"+ Coin_last_price_l + "");
								player.sendMessage("§3Vender: §f<=§a$" + Coin_MAX_BUY + "  §3Comprar: §f>=§a$"
										+ Coin_MIN_SELL + "");
								
							}
						} else {
							player.sendMessage(
									this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
						}
					}
				}

				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("delete")) {
						if (sender.hasPermission("coins.admin") == true) {
							if (verificarCoins(args[1],player) == true) {
								args[1] = args[1].toUpperCase();
								updateSQL("DELETE FROM `coins_coins` WHERE `coin`='" + args[1] + "'");
								updateSQL("DELETE FROM `coins_orders` WHERE `coin`='" + args[1] + "'");
								updateSQL("DELETE FROM `coins_transactions` WHERE `coin`='" + args[1] + "'");
								updateSQL("DELETE FROM` coins_wallet` WHERE `coin`='" + args[1] + "'");
								updateSQL("DELETE FROM` coins_history` WHERE `coin`='" + args[1] + "'");
								player.sendMessage("" + this.getConfig().getString("prefix") + "§aMoeda Deletada");
							}
						}
					}
				}

				if (args.length == 4) {
					if (args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("hashpower")) {
						if (sender.hasPermission("coins.admin") == true) {
							Player p = Bukkit.getPlayer(args[2]);
							String uuid;
							if (p != null) {
								if (isNumeric(args[3])) {
									double val = Double.parseDouble(args[3]);
									if (val != 0) {
										uuid = p.getUniqueId().toString();
										if (updateSQL("UPDATE `coins_players` SET `hashpower` = hashpower+" + val
												+ " WHERE `uuid` = '" + uuid + "'")) {
											player.sendMessage("" + this.getConfig().getString("prefix")
													+ "§aVoce melhorou o hashpower de " + p.getName() + "");
										}
									}
								}

							}

						}
					}
				}

				if (args.length == 4) {
					if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("hashpower")) {
						if (sender.hasPermission("coins.admin") == true) {
							Player p = Bukkit.getPlayer(args[2]);
							String uuid;
							if (p != null) {
								if (isNumeric(args[3])) {
									double val = Double.parseDouble(args[3]);
									if (val != 0) {
										uuid = p.getUniqueId().toString();
										if (updateSQL("UPDATE `coins_players` SET `hashpower` = " + val
												+ " WHERE `uuid` = '" + uuid + "'")) {
											player.sendMessage(
													"" + this.getConfig().getString("prefix") + "§aVoce setou para "
															+ val + " o hashpower de " + p.getName() + "");
										}
									}
								}

							}

						}
					}
				}

				if (args.length == 4) {
					if (args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("money")) {
						if (sender.hasPermission("coins.admin") == true) {
							Player p = Bukkit.getPlayer(args[2]);
							String uuid;
							if (p != null) {
								if (isNumeric(args[3])) {
									double val = Double.parseDouble(args[3]);
									if (val != 0) {
										uuid = p.getUniqueId().toString();
										if (updateSQL("UPDATE `coins_players` SET `money` = money+" + val
												+ " WHERE `uuid` = '" + uuid + "'")) {
											player.sendMessage("" + this.getConfig().getString("prefix")
													+ "§aVoce adicinou dinheiro a conta de " + p.getName() + "");
										}
									}
								}

							}

						}
					}
				}

				if (args.length == 4) {
					if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("money")) {
						if (sender.hasPermission("coins.admin") == true) {
							Player p = Bukkit.getPlayer(args[2]);
							String uuid;
							if (p != null) {
								if (isNumeric(args[3])) {
									double val = Double.parseDouble(args[3]);
									if (val != 0) {
										uuid = p.getUniqueId().toString();
										if (updateSQL("UPDATE `coins_players` SET `money` = " + val
												+ " WHERE `uuid` = '" + uuid + "'")) {
											player.sendMessage(
													"" + this.getConfig().getString("prefix") + "§aVoce setou para "
															+ val + " o dinheiro de " + p.getName() + "");
										}
									}
								}

							}

						}
					}
				}
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("wallet")) {
						if (sender.hasPermission("coins.wallet") || sender.hasPermission("coins.player")) {
							if (verificarCoins(args[1],player) == true) {
								double wallet_amount = 0;
								double wallet_amount_blocked=0;
								String wallet_coin = "";
								String wallet_uuid = "";
								try {
									ResultSet rs = query("SELECT * FROM `coins_wallet` WHERE `coin` = '" + args[1]
											+ "' AND `uuid` = '" + player.getUniqueId() + "' LIMIT 1;");
									if (rs.next()) {
										wallet_amount = rs.getDouble("amount");
										wallet_amount_blocked = rs.getDouble("amount_blocked");
										wallet_coin = rs.getString("coin");
									}
								} catch (Exception ex) {
								}
								if (wallet_coin != null) {
									
									double LanFut_amount = 0;
									try {
										ResultSet rs_soma = query("SELECT SUM(amount) FROM `coins_transactions` WHERE `coin` = '"
												+ wallet_coin + "' AND `R_uuid`='" + player.getUniqueId() + "'");
										if (rs_soma.next()) {
											LanFut_amount = rs_soma.getDouble(1);
										}
									} catch (Exception ex) {
									}
									
									
									
									player.sendMessage("" + this.getConfig().getString("prefix")
											+ "Voce tem em sua carteira:");
									player.sendMessage("§3Disponivel: §a" +formatDouble(wallet_amount-wallet_amount_blocked)+ " §6" + wallet_coin + "");
									player.sendMessage("§3Bloqueado: §c" +(wallet_amount_blocked)+ " §6" + wallet_coin + "");
									player.sendMessage("§3Lan. Futuros: §f" +LanFut_amount+" §6" + wallet_coin + "");
									player.sendMessage("§3Total: §d" +(wallet_amount+LanFut_amount)+ " §6" + wallet_coin + "");
								} else {
									player.sendMessage("" + this.getConfig().getString("prefix")
											+ "Voce nao possui esta moeda em sua carteira");
								}
							}
						} else {
							player.sendMessage(
									this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
						}
					}
				}

				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("mining")) {
						if (sender.hasPermission("coins.mining") || sender.hasPermission("coins.player")) {

						double Player_Hash = 0;
						double Player_Money_ = 0;
						String Player_Mining2 = "";
						String Player_uuid = null;
						try {
							ResultSet rs = query(
									"SELECT * FROM `coins_players` WHERE `uuid` = '" + player.getUniqueId() + "';");
							if (rs.next()) {
								Player_Hash = rs.getDouble("hashpower");
								Player_Money_ = rs.getDouble("money");
								Player_Mining2 = rs.getString("mining");
								Player_uuid = rs.getString("uuid");
							}
						} catch (Exception ex) {
						}
						if (Player_uuid != null) {
							double Coin_dificuldade = 0;
							double Coin_minerado2 = 0;
							String Coin_block="";
							double Coin_Tempo_Blocos=0;
							
							if (verificarCoins(Player_Mining2,player) == true) {

								try {
									ResultSet rs2 = query("SELECT * FROM `coins_coins` WHERE `coin` = '"
											+ Player_Mining2 + "' LIMIT 1;");
									if (rs2.next()) {
										Coin_dificuldade = rs2.getDouble("difficulty");
										Coin_Tempo_Blocos = rs2.getDouble("time_blocks");
										Coin_minerado2 = rs2.getDouble("mining");
										Coin_block = rs2.getString("block");
									}
								} catch (Exception ex) {
								}
							}

							double Player_hashpower_forune = 0;
							double Player_hashpower = formatDouble(Math.pow(Player_Hash, 2));
							double currentLevel = player.getInventory().getItemInMainHand()
									.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS); // gets
																							// the
																							// current
																							// level
																							// of
																							// the
																							// enchant
							Player_hashpower_forune = formatDouble(Player_hashpower * ((currentLevel * 0.2) + 1));

							
							double Chance = 0, Chance_1emC = 0;
							if (Coin_dificuldade > 0) {
								if (Player_hashpower_forune < Coin_dificuldade) {
									Chance = formatDouble((Player_hashpower_forune / Coin_dificuldade) * 100);
									// Chance_1emC=formatDouble(dificuldade_moeda/Chance);
									if (Chance > 0) {
										Chance_1emC = formatDouble(1 / (Chance / 100));
									}

								} else {
									Chance = 100;
									Chance_1emC = 1;
								}
							} else {
								Chance = 100;
								Chance_1emC = 1;
							}

							player.sendMessage(
									"" + this.getConfig().getString("prefix") + "§eInformações sobre mineração:");
							
							player.sendMessage("§3Nivel do Hashpower: §5" + Player_Hash + "");
							player.sendMessage("§3Hashpower: §7" + Player_Hash + "^2 = §e" + Player_hashpower + "");
							if (currentLevel == 0) {
								player.sendMessage("§3Hashpower com fortuna: §cSegure um item com fortuna");
							} else {
								player.sendMessage("§3Hashpower com fortuna: §e= " + Player_hashpower_forune + "");
							}
							player.sendMessage("§3Minerando: §6" + Player_Mining2 + "");
							player.sendMessage("§3Block: §a" + Coin_block + "");
							if(Player_Mining2!=""){
								player.sendMessage("§3Dificuldade da moeda: §c" + Coin_dificuldade + "");
								player.sendMessage("§3Chance de minerar: §2" + Chance + "%");
								player.sendMessage(
										"§3Chance: §b1 §7em §b" + Chance_1emC + " §6" + Player_Mining2 + " §7minerados.");
							}
						} else {
							player.sendMessage(
									"" + this.getConfig().getString("prefix") + "§aVoce nao possui uma carteira");
						}
					}else{
						player.sendMessage(
								this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
					}
				}
				}

				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("create") && args[1].equalsIgnoreCase("wallet")) {
						if (sender.hasPermission("coins.walletcreate") || sender.hasPermission("coins.player")) {
							String Player_uuid = "";
							try {
								ResultSet rs = query("SELECT * FROM `coins_players` WHERE `uuid` = '"
										+ player.getUniqueId().toString() + "' LIMIT 1;");
								if (rs.next()) {
									Player_uuid = rs.getString("uuid");
								}
							} catch (Exception ex) {
							}

							String uuid = player.getUniqueId().toString();
							

							if (Player_uuid.equalsIgnoreCase(uuid)) {
								player.sendMessage(
										"" + this.getConfig().getString("prefix") + "§cVoce ja possui uma carteira.");
							} else {
								if (updateSQL("REPLACE INTO `coins_players` SET `uuid` = '" + player.getUniqueId()
										+ "', `money` = 0, `money_blocked` = 0, `hashpower` = 0, `trades` = 0, `mining` = ''")) {
									player.sendMessage(
											"" + this.getConfig().getString("prefix") + "§aCarteira criada.");
								} else {
									player.sendMessage(
											"" + this.getConfig().getString("prefix") + "§cErro ao criar a carteira.");
								}
							}
						}
					}
				}

				
				if (args.length == 1 || args.length == 3) {

					if (args[0].equalsIgnoreCase("wallet")) {			
							if (sender.hasPermission("coins.list") || sender.hasPermission("coins.player")) {
								
								int erro=0;
								
								int limite=10;
								int QTD_MOEDAS=0;
								int page_atual=1;
								
								if(args.length == 3){
									if(args[1].equalsIgnoreCase("page")){
										if(EhInteiro(args[2])){
											int page_n=Integer.parseInt(args[2]);
											if(page_n>0){												
												if(args.length == 3){
													page_atual=Integer.parseInt(args[2]);
												}else{
													page_atual=1;
												}													
											}
										}else{
											erro=1;
										}
									}else{
										erro=1;
									}
								}
								if(erro==0){
									
									try {
										ResultSet rs_soma = query("SELECT COUNT(id) FROM `coins_wallet` WHERE `uuid` = '"
										+ player.getUniqueId() + "'");
										
										if (rs_soma.next()) {
											QTD_MOEDAS = rs_soma.getInt(1);
										}
									} catch (Exception ex) {
									}
									
									
									int QTD_PAGE = QTD_PAGE(QTD_MOEDAS,limite);
									if(page_atual>QTD_PAGE){
										page_atual=QTD_PAGE;
									}
									
									
									double Player_Hash = 0;
									double Player_Money_ = 0;
									double Player_Money_Blocked=0;
									String Player_Mining2 = "";
									String Player_uuid = "";
									
									try {
										ResultSet rs = query(
												"SELECT * FROM `coins_players` WHERE `uuid` = '" + player.getUniqueId() + "';");
										if (rs.next()) {
											Player_Hash = rs.getDouble("hashpower");
											Player_Money_ = rs.getDouble("money");
											Player_Money_Blocked = rs.getDouble("money_blocked");
											Player_Mining2 = rs.getString("mining");
											Player_uuid = rs.getString("uuid");
										}
									} catch (Exception ex) {
									}
									player.sendMessage("" + this.getConfig().getString("prefix")
											+ "§eVoce tem em sua carteira:");
									player.sendMessage("§bCarteira: §7" + player.getUniqueId() + "");
									player.sendMessage("§bDinheiro: §a C $" + formatDouble(Player_Money_-Player_Money_Blocked) + "§7 / §cB $"+Player_Money_Blocked+"");
	
									
									player.sendMessage("§bMoedas na carteira §fPagina "+page_atual+"§3/§f"+QTD_PAGE+":");
												int cont2=0;
												double wallet_amount = 0;
												double wallet_amount_blocked=0;
												String wallet_coin = "";
												String wallet_uuid = "";
												try {
													ResultSet rs = query("SELECT * FROM `coins_wallet` WHERE `uuid` = '"
															+ player.getUniqueId() + "' ORDER BY `amount` DESC LIMIT "+(limite*(page_atual-1))+","+limite+"");
													while (rs.next()) {
														cont2+=1;
									
														wallet_amount = rs.getDouble("amount");
														wallet_amount_blocked = rs.getDouble("amount_blocked");
														wallet_coin = rs.getString("coin");
														player.sendMessage("" + cont2 + " - §6" + wallet_coin + " §a C " + formatDouble(wallet_amount-wallet_amount_blocked) + "§7 / §cB "+wallet_amount_blocked+"");
													}
													if (wallet_coin == null) {
														player.sendMessage("§cVoce nao possui nenhuma moeda em sua carteira");
													}
												} catch (Exception ex) {
												}
											
								}
					
							} else {
								player.sendMessage(
										this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
							}
						}
					}
				
				
				if (args.length == 4) {
					String LCO_COIN = "";
					String LCO_TYPE_ = "";
					String D_A = "";
					if (args[3].equalsIgnoreCase("orders") && args[0].equalsIgnoreCase("list")
							&& (args[2].equalsIgnoreCase("sell") || args[2].equalsIgnoreCase("buy"))) {
						if (sender.hasPermission("coins.listorders") || sender.hasPermission("coins.player")) {
						if (verificarCoins(args[1],player) == true) {
							LCO_COIN = args[1];
							LCO_TYPE_ = args[2];
							D_A = null;

							if (LCO_TYPE_.equalsIgnoreCase("sell")) {
								D_A = "ASC";
							} else {
								D_A = "DESC";
							}
							int cont = 0;
							double LCO_amount = 0;
							double LCO_price = 0;
							String LCO_coin = null;
							String LCO_uuid = "";
							try {
								ResultSet rs = query("SELECT * FROM `coins_orders` WHERE `coin` = '" + LCO_COIN
										+ "' AND `type` = '" + LCO_TYPE_ + "' ORDER BY `price` " + D_A + ";");
								while (rs.next()) {
									if (cont == 0) {
										if (LCO_TYPE_.equalsIgnoreCase("sell")) {
											player.sendMessage("" + this.getConfig().getString("prefix")
													+ "Ordens de venda para: §e" + LCO_COIN + "");
										} else {
											player.sendMessage("" + this.getConfig().getString("prefix")
													+ "Ordens de compra para: §e" + LCO_COIN + "");
										}
									}
									cont = cont + 1;
									LCO_amount = rs.getDouble("amount");
									LCO_coin = rs.getString("coin");
									LCO_price = rs.getDouble("price");
									player.sendMessage("" + cont + " - §e" + LCO_amount + " §b$" + LCO_price + "");
								}
								if (LCO_coin == null) {
									player.sendMessage("Não ha nenhuma ordem para essa moeda");
								}
							} catch (Exception ex) {
							}
						}
						}else{
							
						}
					}
				}

				if (args.length == 3) {
					String LCO_COIN = null;
					String LCO_TYPE_ = null;
					String D_A = null;
					if (args[2].equalsIgnoreCase("orders") && args[0].equalsIgnoreCase("list")) {
						if (verificarCoins(args[1],player) == true) {

							String Coin_Block = null;
							String Coin_name = null;
							String Coin_COIN = null;
							String Coin_uuid = null;
							double Coin_ming = 0;

							double Coin_last_price = 0;
							double Coin_last_price_a = 0;
							double Coin_last_price_h = 0;
							double Coin_last_price_l = 0;
							double Change = 0;
							try {
								ResultSet rs = query("SELECT * FROM `coins_coins` WHERE `coin` = '"
										+ args[1].toUpperCase() + "' LIMIT 1;");
								if (rs.next()) {
									Coin_Block = rs.getString("block");
									Coin_uuid = rs.getString("uuid");
									Coin_name = rs.getString("name");
									Coin_COIN = rs.getString("coin");
									Coin_ming = rs.getDouble("mining");
									Coin_last_price = rs.getDouble("last_price");
									Coin_last_price_a = rs.getDouble("last_price_a");
									Coin_last_price_h = rs.getDouble("last_price_h");
									Coin_last_price_l = rs.getDouble("last_price_l");
								}
							} catch (Exception ex) {
							}

							String COR = null;
							if (Coin_last_price_a != 0) {
								Change = formatDouble(((Coin_last_price / Coin_last_price_a) - 1) * 100);
							}
							if (Change >= 0) {
								COR = "§a+";
							} else {
								COR = "§c";
							}

							double Coin_MIN_SELL = 0;
							try {
								ResultSet rs = query("SELECT * FROM `coins_orders` WHERE `coin` = '" + Coin_COIN
										+ "' AND `type` = 'SELL' AND `price`= (SELECT MIN(price) FROM `coins_orders`  WHERE `type` = 'SELL'  AND `coin` = '" + Coin_COIN+"')");
								if (rs.next()) {
									Coin_MIN_SELL = rs.getDouble("price");
								}
							} catch (Exception ex) {
							}
							double Coin_MAX_BUY = 0;
							try {
								ResultSet rs = query("SELECT * FROM `coins_orders` WHERE `coin` = '" + Coin_COIN
										+ "' AND `type` = 'BUY' AND `price`= (SELECT MAX(price) FROM `coins_orders` WHERE `type` = 'BUY'  AND `coin` = '" + Coin_COIN+"')");
								if (rs.next()) {
									Coin_MAX_BUY = rs.getDouble("price");
								}
							} catch (Exception ex) {
							}
							player.sendMessage("" + this.getConfig().getString("prefix") + "§3Informaçoes §6"
									+ args[1].toUpperCase() + "");
							player.sendMessage("§3Ultimo preço: §a$" + Coin_last_price + "  §324hr Change: " + COR + ""
									+ Change + "%");
							player.sendMessage(
									"§324hr §cHIGH: §7$" + Coin_last_price_h + " §9LOW: §7$" + Coin_last_price_l + "");
							player.sendMessage("§3Capitalizaçao: §a$" + formatDouble(Coin_last_price * Coin_ming) + "");
							player.sendMessage(
									"§3Vender: §f<=§a$" + Coin_MAX_BUY + "  §3Comprar: §f>=§a$" + Coin_MIN_SELL + "");

							LCO_COIN = args[1];
							player.sendMessage("§3Ordens de compra e venda:");
							int cont = 0;
							double LCO_amount = 0;
							double LCO_price = 0;
							String LCO_coin = null;
							String LCO_uuid = "";
							Double[] BUY_Amount = new Double[5];
							Double[] BUY_Price = new Double[5];
							for (int i = 0; i < BUY_Amount.length; i++) {
								BUY_Amount[i] = 0.0;
								BUY_Price[i] = 0.0;
							}
							try {
								ResultSet rs = query("SELECT * FROM coins_orders WHERE coin ='" + LCO_COIN
										+ "' AND type='BUY' GROUP BY price	ORDER BY price DESC LIMIT 5");
								while (rs.next()) {

									LCO_amount = rs.getDouble("amount");
									LCO_coin = rs.getString("coin");
									LCO_price = rs.getDouble("price");

									ResultSet rs_soma = query("SELECT SUM(amount) FROM `coins_orders` WHERE `coin` = '"
											+ LCO_COIN + "' AND `type` = 'BUY' AND `price`=" + LCO_price + "");
									if (rs_soma.next()) {
										BUY_Amount[cont] = rs_soma.getDouble(1);
									}
									// BUY_Amount[cont]=LCO_amount;
									BUY_Price[cont] = LCO_price;
									cont = cont + 1;
								}
								if (LCO_coin == null) {
									player.sendMessage("" + this.getConfig().getString("prefix")
											+ "§aNão ha nenhuma ordem de compra");
								}
							} catch (Exception ex) {
							}

							if (cont > 0) {
								for (int i = BUY_Amount.length - 1; i >= 0; i--) {
									if (BUY_Amount[i] > 0 && BUY_Price[i] > 0) {
										String amount = BUY_Amount[i].toString();
										String price = BUY_Price[i].toString();
										player.sendMessage("§aB - §a$" + price + " §e" + amount + "");
									}
								}
							}

							try {
								// ResultSet rs = query("SELECT * FROM
								// `coins_orders` WHERE DISTINCT (price) AND
								// `coin` = '"+LCO_COIN+"' AND `type` = 'SELL'
								// ORDER BY `price` ASC LIMIT 5;");
								ResultSet rs = query("SELECT * FROM coins_orders WHERE coin ='" + LCO_COIN
										+ "' AND type='SELL' GROUP BY price	ORDER BY price ASC LIMIT 5");

								while (rs.next()) {
									cont = cont + 1;
									LCO_amount = rs.getDouble("amount");
									LCO_coin = rs.getString("coin");
									LCO_price = rs.getDouble("price");
									ResultSet rs_soma = query("SELECT SUM(amount) FROM `coins_orders` WHERE `coin` = '"
											+ LCO_COIN + "' AND `type` = 'SELL' AND `price`=" + LCO_price + "");
									if (rs_soma.next()) {
										LCO_amount = rs_soma.getDouble(1);
									}
									player.sendMessage("§cS - §c$" + LCO_price + " §e" + LCO_amount + "");
								}
								if (LCO_coin == null) {
									player.sendMessage("" + this.getConfig().getString("prefix")
											+ "§aNão ha nenhuma ordem de venda");
								}
							} catch (Exception ex) {
							}
						} 
					}
				}

				if (args.length == 2 || args.length == 4) {
					if (args[0].equalsIgnoreCase("transactions")) {
						String Coin=args[1];
						if (verificarCoins(Coin,player)) {
							int erro=0;
							int limite=10;
							int QTD_MOEDAS=0;
							int page_atual=1;
							
							if(args.length == 4){
								if(args[2].equalsIgnoreCase("page")){
									if(EhInteiro(args[3])){
										int page_n=Integer.parseInt(args[3]);
										if(page_n>0){												
											if(args.length == 4){
												page_atual=Integer.parseInt(args[3]);
											}else{
												page_atual=1;
											}													
										}
									}else{
										erro=1;
									}
								}else{
									erro=1;
								}
							}
							
							if(erro==0){
								try {
									ResultSet rs_soma = query("SELECT COUNT(id) FROM `coins_transactions` WHERE `coin` = '"+Coin+"'");
									if (rs_soma.next()) {
										QTD_MOEDAS = rs_soma.getInt(1);
									}
								} catch (Exception ex) {
								}
								
								
								int QTD_PAGE = QTD_PAGE(QTD_MOEDAS,limite);
								if(page_atual>QTD_PAGE){
									page_atual=QTD_PAGE;
								}
							
					
							
							int cont = 0;
							double TR_amount = 0;
							double TR_fee = 0;
							int TR_id = 0;
							String TR_coin = null;
							//player.sendMessage("§bTransferencias §fPagina "+page_atual+"§3/§f"+QTD_PAGE+":");
							try {
								ResultSet rs = query(
										"SELECT * FROM `coins_transactions` WHERE `coin`='" + args[1] + "' AND `C_uuid`='' ORDER BY `fee` DESC LIMIT "+(limite*(page_atual-1))+","+limite+"");
								while (rs.next()) {
									if (cont == 0) {
										player.sendMessage(
												"" + this.getConfig().getString("prefix") + "Transações pendentes §fPagina "+page_atual+"§3/§f"+QTD_PAGE+":");
									}
									cont = cont + 1;
									TR_amount = rs.getDouble("amount");
									TR_coin = rs.getString("coin");
									TR_fee = rs.getDouble("fee");
									TR_id = rs.getInt("id");
									player.sendMessage(""+((cont)+(limite*(page_atual-1)))+" - ID: " + TR_id + " - §e" + TR_amount + " §6" + TR_coin
											+ "§7 Taxa: §b" + TR_fee + " §6"+TR_coin+"");
								}
								if (TR_coin == null) {
									player.sendMessage(
											"" + this.getConfig().getString("prefix") + "Nã hà transações pendentes");
								}
							} catch (Exception ex) {
							}
						}
					}
					}
				}

				if (args.length == 3 || args.length == 4) {
					if (args[0].equalsIgnoreCase("shop") && args[1].equalsIgnoreCase("buy")) {
						if (sender.hasPermission("coins.shop") == true) {
							if (this.getConfig().contains(args[2])) {
								double BUY_preco = Double.parseDouble(this.getConfig().getString("" + args[2] + ".Price"));
								String BUY_precoOUT = Double.toString(BUY_preco);
								String BUY_coin = this.getConfig().getString("" + args[2] + ".Coin");
								
								double BUY_qtd=1;
								if(args.length == 4){
									if (isNumeric(args[3])) {
										BUY_qtd=Double.parseDouble(args[3]);
										if(BUY_qtd>0){
										}else{
											BUY_qtd=1;	
										}
									}
								}
								String BUY_qtd_out=Double.toString(BUY_qtd);
								
								if (WT.Wallet(BUY_coin, -(BUY_preco*BUY_qtd), 0, 0, player.getUniqueId(), "SHOP",0,true)) {
									for (int i = 0; i < this.getConfig().getStringList("" + args[2] + ".Command")
											.size(); i++) {
										Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
												this.getConfig().getStringList("" + args[2] + ".Command").get(i)
														.replace("{PLAYER}", player.getName().toString())
														.replace("{COIN}", BUY_coin).replace("{PRICE}", BUY_precoOUT).replace("{AMOUNT}", BUY_qtd_out));
									}
									String mensage_PLAYER = this.getConfig().getString("prefix")
											+ this.getConfig().getString("" + args[2] + ".MessagePlayer");
									if (mensage_PLAYER.equalsIgnoreCase(null)
											|| this.getConfig().contains("" + args[2] + ".MessagePlayer") == false) {
									} else {
										player.sendMessage(this.getConfig().getString("prefix")
												+ this.getConfig().getString("" + args[2] + ".MessagePlayer")
														.replace("{PLAYER}", player.getName().toString())
														.replace("{COIN}", BUY_coin).replace("{PRICE}", BUY_precoOUT).replace("{AMOUNT}", BUY_qtd_out));
									}

									String mensage_GLOBAL = this.getConfig().getString("prefix")
											+ this.getConfig().getString("" + args[2] + ".MessageGlobal");
									if (mensage_GLOBAL.equalsIgnoreCase(null)
											|| this.getConfig().contains("" + args[2] + ".MessageGlobal") == false) {
									} else {
										Bukkit.getServer()
												.broadcastMessage(this.getConfig().getString("prefix") + this
														.getConfig().getString("" + args[2] + ".MessageGlobal")
														.replace("{PLAYER}", player.getName().toString())
														.replace("{COIN}", BUY_coin).replace("{PRICE}", BUY_precoOUT).replace("{AMOUNT}", BUY_qtd_out));
									}
								}
							}
						} else {
							player.sendMessage(
									this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
						}
					}
				}

				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("upgrade") && args[1].equalsIgnoreCase("hashpower")) {
						if (sender.hasPermission("coins.upgrade") || sender.hasPermission("coins.player")) {
							if (PI.PlayerInfo(0,0, 1, null, player.getUniqueId(),true)) {
							}

						} else {
							player.sendMessage(
									this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
						}
					}
				}
				
				if (args.length == 2) {
					if (args[0].equalsIgnoreCase("send")) {
						if (verificarCoins(args[1],player) == true) {
						if (sender.hasPermission("coins.send") == true) {
	
							int Coin_Age=0;							
							try {
								ResultSet rs = query("SELECT * FROM `coins_coins` WHERE `coin` = '" + args[1].toUpperCase() + "' LIMIT 1;");
								if (rs.next()) {
									Coin_Age = rs.getInt("height");
								}
							} catch (Exception ex) {
							}
							
														
							int Coin_Age_2=Coin_Age+1;
							String moeda="§6"+args[1]+" §b#"+Coin_Age_2+"";
							
							ItemMeta data_item =null;
							data_item = player.getInventory().getItemInMainHand().getItemMeta();
							//short dura = player.getInventory().getItemInMainHand().getDurability();
							if(data_item!=null){
							String nome_bloco= data_item.getDisplayName();
							if(nome_bloco==null){
								nome_bloco="";
							}
							
							
							if(nome_bloco.equalsIgnoreCase(moeda)){
								if(updateSQL("UPDATE `coins_coins` SET height= height + 1 WHERE `coin`='"+ args[1] + "' AND `height`="+Coin_Age+"")){
									player.getInventory().removeItem(player.getInventory().getItemInMainHand());
									player.sendMessage(this.getConfig().getString("prefix") + "§aBloco Aceito: "+nome_bloco+"");
									
									
									NB.NewBlock(player, args[1]);
									
									/*
									ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK,1);
									BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
									bookMeta.setTitle(moeda);
									bookMeta.setAuthor(player.getUniqueId().toString());
									bookMeta.setPages("TEXTO");
									writtenBook.setItemMeta(bookMeta);
									player.getWorld().dropItem(player.getLocation(),writtenBook);
									*/
									
								}
							}else{
								player.sendMessage(this.getConfig().getString("prefix") + "§cBloco invalido.");
							}
							
							}
							
						} else {
							player.sendMessage(
									this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
						}
					}
				}
				}

				if (args.length == 1) {

					if (args[0].equalsIgnoreCase("Help")) {
						String Level = Integer.toString(this.getConfig().getInt("level"));
						for (int i = 0; i < this.getConfig().getStringList("Help").size(); i++) {
							player.sendMessage(this.getConfig().getStringList("Help").get(i).replace("{LEVEL}", Level));
						}
					}
					
				if (args[0].equalsIgnoreCase("reload")) {
					if (sender.hasPermission("coins.reload") == true) {
						reloadConfig();
						player.sendMessage(this.getConfig().getString("prefix") + this.getConfig().getString("Reload"));		
							for (int i = 0; i < getConfig().getStringList("Blocks").size(); i++) {
								orelist.put(getConfig().getStringList("Blocks").get(i), null);
							}
						} else {
							player.sendMessage(
									this.getConfig().getString("prefix") + this.getConfig().getString("NoPerm"));
						}
					}

				} else {
					// player.sendMessage(this.getConfig().getString("prefix")+this.getConfig().getString("Info"));
				}

			} else {
				player.sendMessage(this.getConfig().getString("NoPerm"));
			}
			return true;
		}
		return false;
		
	}

	@Override
	public void onDisable() {
		ConsoleCommandSender b = Bukkit.getConsoleSender();
		b.sendMessage(this.getConfig().getString("onDisable"));
	}

	public void LoadEventos() {
		PluginManager plm = Bukkit.getPluginManager();
		plm.registerEvents(new Coins(this), this);
	}

	public void connect() {
		try {
			con = DriverManager.getConnection(
					"jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?autoReconnect=true", USER, PASSWORD);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void close() {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public void update(String qry) {
		try {
			Statement st = con.createStatement();
			st.executeUpdate(qry);
			//st.close();
		} catch (SQLException ex) {
			connect();
			ex.printStackTrace();
		}
	}

	public ResultSet query(String qry) {
		ResultSet rs = null;

		try {
			//connect();
			Statement st = con.createStatement();
			rs = st.executeQuery(qry);
		} catch (SQLException ex) {
			connect();
			ex.printStackTrace();
		}
		return rs;
	}

	
	public boolean isNumeric(String str) {
	    if( str == null ) return false;
	    try {
	        Double.parseDouble(str);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	

	public boolean verificarCoins(String strCoin, Player player) {
		int result = 0;
		String Coin_Name = "";
		try {
			ResultSet rs = query("SELECT * FROM `coins_coins` WHERE `coin` = '" + strCoin + "' LIMIT 1;");
			if (rs.next()) {
				result = rs.getInt("id");
				Coin_Name = rs.getString("coin");
			}
		} catch (Exception ex) {
		}
		if (strCoin.equalsIgnoreCase(Coin_Name) && strCoin != "") {
			return true;
		} else {
			if(player != null){
				player.sendMessage("" + this.getConfig().getString("prefix") + "Essa moeda nao existe");
			}
			return false;
		}
	}



	public boolean SzNum(double NUM) {
		if ((NUM >= 0.00000001 || NUM == 0)) {
			return true;
		} else {
			return false;
		}
	}

	public double formatDouble(double myDouble) {
		if(myDouble>0){
		NumberFormat numberFormatter = new DecimalFormat("0.00000000", new DecimalFormatSymbols(Locale.ENGLISH));
		myDouble = Double.valueOf(numberFormatter.format(myDouble));
		}
		return myDouble;
	}
	
	public double formatDouble2c(double myDouble) {
		NumberFormat numberFormatter = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ENGLISH));
		myDouble = Double.valueOf(numberFormatter.format(myDouble));
		return myDouble;
	}


	public boolean updateSQL(String SQL) {
		int affectedRows = 0;
		try {
			PreparedStatement up_ = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
			affectedRows = up_.executeUpdate();
			up_.close();
		} catch (Exception ex) {
		}

		if (affectedRows > 0) {
			return true;
		} else {
			//player.sendMessage(this.getConfig().getString("prefix") + "§aErro ao enviar comando.");
			return false;
		}
	}
	
	public int SUM_ROWS_SQL_UPDATE(String SQL) {
		int affectedRows = 0;
		try {
			PreparedStatement up_ = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
			affectedRows = up_.executeUpdate();
			up_.close();
		} catch (Exception ex) {
		}

		if (affectedRows > 0) {
			return affectedRows;
		} else {
			return 0;
		}
	}

	public boolean EhInteiro(String INTEIRO) {
		try {
			Integer.parseInt(INTEIRO);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String Data() {
		Date data = new Date(System.currentTimeMillis());
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(data);
	}
	public int QTD_PAGE(int total, int linhas){
		double total_page =  Math.ceil((double) total/linhas);
		//Bukkit.getServer().broadcastMessage(""+this.getConfig().getString("prefix")+"§6TOTAL PAGE:"+total_page+" total"+total+" limite"+linhas+"");
		if(total_page<1){
			total_page=1;
		}
		return (int) total_page;
	}

}
