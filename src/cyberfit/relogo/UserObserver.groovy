package cyberfit.relogo

import static repast.simphony.relogo.Utility.*;
import static repast.simphony.relogo.UtilityG.*;

import java.io.File
import jxl.*

import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory

import repast.simphony.essentials.RepastEssentials
import repast.simphony.relogo.Stop;
import repast.simphony.relogo.Utility;
import repast.simphony.relogo.UtilityG;
import repast.simphony.relogo.schedule.Go;
import repast.simphony.relogo.schedule.Setup;
import cyberfit.ReLogoObserver;

class UserObserver extends ReLogoObserver{

	//public static final String MPATH = "./docs/campaign_01_missions.xlsx";
	//public static final String CPATH = "./docs/campaign_01_CPTs.xlsx";
	
	public numTerrains = []
	//def obs_m0Comps = 0
	
	@Setup
	def setup(){
		
		// initialize Random.uniform
		Random random = new Random()
				
		clearAll()
		
		setDefaultShape(Terrain,"box")
		setDefaultShape(Defender,"person")
		setDefaultShape(Attacker,"person")
		setDefaultShape(Friendly,"person")
		
		loadBaseTerrain()
		loadCPTs()
		loadAttackers()
		loadMissions()
	}
	
	@Go
	def go(){
	
		ask (patches()){
			recolorPatch()
		}
		
		//interactions
		ask(interactionFTs()) {
			step()
		}
		
		ask(interactionTTs()) {
			step()
		}
		
		ask(interactionFFs()) {
			step()
		}
		
		//then actions
		ask(terrains()) {
			step()
		}
		
		ask(defenders()) {
			step()
		}
		
		ask(attackers()) {
			step()
		}
		
		ask(friendlys()) {
			step()
		}
		setPhase()
		
		//then measure
		//updateMeasures()
		
		ask(defenders()){
			cleanUpCompromiseSA()
		}
		
		//print "mission 0 has ${obs_m0Comps} compromises"
	}
	
	def setPhase() {
		
		def tick = RepastEssentials.GetTickCount()
		
		if (tick == 120) {
			ask(defenders()){
				setPhase2()
			}
		}else if (tick == 240) {
			ask(defenders()){
				setPhase3()
			}
		}
	}
	
	def updateMeasures() {
		def t = 0
		ask(terrains()){
			t = t + vulns.size()
		}
		
		def n = 0
		ask(defenders()){
			n = n + totalNothings
		}
		print "total nothings is ${n}"
	
	//	def rs = 0
		//ask(defenders()){
			//rs = rs + totalRestoralSuccessOps
		//}
		
		//def rf = 0
		//ask(defenders()){
			//rf = rf + totalRestoralFailOps
		//}
		
	//	def tempCompCount = 0
		//ask(terrains()){
			//if(missionID == 0) {
				//tempCompCount = tempCompCount + totalComps
			//}
		//}
		
		//obs_m0Comps = obs_m0Comps + tempCompCount
		
		
	}
	
	def loadMissions() {
		
		Campaign c1 = new Campaign()
		def missions = c1.loadMissions()
		
		def x = 10
		def y = 10
		
		for(Campaign.Mission mission : missions) {
			print "processing mission: ${mission.missionId}"
			x = x + 2
			y = y + 2
			
			def mID = mission.missionId
			def numForces = mission.numFriendlyForces
			def numT1 = mission.numTerrainT1
			def numT2 = mission.numTerrainT2
			def numT3 = mission.numTerrainT3
			
			print "friendlys"
			def i = 0
			i.upto(mission.numFriendlyForces.toInteger()) {
				println "creating friendly ${i}"
				createFriendlys(1){ [setxy(x+i,y), setColor(green()), missionId = mission.missionId] }
				i = i + 1
			 }
			 
			 x = x + 2
			 y = y + 2
			 
			 print "routers:"
			 i = 0
			 i.upto(mission.numTerrainT1.toInteger()) {
				 println "creating router ${i}"
				 createTerrains(1){ [setxy(x+i,y), setColor(brown()), missionsSupported.add(mission.missionId), missionID = mission.missionId, t_type=1]}
				 i = i+ 1
			 }
			 
			 x = x + 2
			 y = y + 2
			 
			 print "servers:"
			 i = 0
			 i.upto(mission.numTerrainT2.toInteger()) {
				 println "creating server ${i}"
				 createTerrains(1){ [setxy(x+i,y), setColor(brown()), missionsSupported.add(mission.missionId), missionID = mission.missionId, t_type=2]}
				 i = i+ 1
			 }
	
			 x = x + 2
			 y = y + 2
			 
			 print "client workstations:"
			 i = 0
			 i.upto(mission.numTerrainT3.toInteger()) {
				 println "creating workstation ${i}"
				 createTerrains(1){ [setxy(x+i,y), setColor(brown()), missionsSupported.add(mission.missionId), missionID = mission.missionId, t_type=3]}
				 i = i+ 1
			 }
		}
		print "---"

	}
	
	def loadCPTs() {
		
		Campaign c2 = new Campaign()
		
		print "soldiers:"
		def soldiers = c2.loadSoldiers()
		def x = -50
		def y = 0
		
		for(Campaign.Soldier soldier : soldiers) {
			print soldier.team
			x = x+ 2
			if(x == -30) {
				x = -50
				y = y -2
			}
			createDefenders(1){ [setxy(x,y), setColor(green()), team = soldier.team, squad = soldier.squad, skill = soldier.skill] }
			
			createTerrains(1){ [setxy(x+1,y+1), setColor(orange()), t_type = 99] }
		}
				
	}
	
	def loadAttackers() {
		
		Campaign c4 = new Campaign()
		
		print "attackers:"
		def attackers = c4.loadAttackers()
		def x = 0
		def y = -30
		
		for(Campaign.Attacker attacker : attackers) {
			print "attacker group"
			print attacker.aGroup
			x = x+ 2
			if(x == 10) {
				x = 0
				y = y -2
			}
			
			createAttackers(1){ [setxy(x,y), setColor(red()), tier = attacker.tier] }
			createTerrains(1){ [setxy(x+1,y+1), setColor(pink()), t_type = 66] }
		}
				
	}
	
	def loadBaseTerrain(){
		
		//Create Routers (type 1)
		createTerrains(1){ [setxy(-10,1), setColor(brown()), t_type = 1, missionID = 0] }
		createTerrains(1){ [setxy(-10,2), setColor(brown()), t_type = 1, missionID = 0] }
		createTerrains(1){ [setxy(-10,3), setColor(brown()), t_type = 1, missionID = 0] }
		createTerrains(1){ [setxy(-10,4), setColor(brown()), t_type = 1, missionID = 0] }
		createTerrains(1){ [setxy(-10,5), setColor(brown()), t_type = 1, missionID = 0] }
	
		createTerrains(1){ [setxy(-9,1), setColor(brown()), t_type = 1, missionID = 0] }
		createTerrains(1){ [setxy(-9,2), setColor(brown()), t_type = 1, missionID = 0] }
		createTerrains(1){ [setxy(-9,3), setColor(brown()), t_type = 1, missionID = 0] }
		createTerrains(1){ [setxy(-9,4), setColor(brown()), t_type = 1, missionID = 0] }
		createTerrains(1){ [setxy(-9,5), setColor(brown()), t_type = 1, missionID = 0] }
		
		//Create Servers (t_type 2)
		createTerrains(1){ [setxy(-7,-6), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,-5), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,-4), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,-3), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,-2), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,-1), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,0), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,1), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,2), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,3), setColor(brown()), t_type = 2, missionID = 0] }

		createTerrains(1){ [setxy(-7,4), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,5), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,6), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,7), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,8), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,9), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,10), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,11), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,12), setColor(brown()), t_type = 2, missionID = 0] }
		createTerrains(1){ [setxy(-7,13), setColor(brown()), t_type = 2, missionID = 0] }
		
		//Create Clients (t_type 3)
		createTerrains(1){ [setxy(-6,-6), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,-5), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,-4), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,-3), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,-2), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,-1), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,0), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,1), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,2), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,3), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,4), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,5), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,6), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,7), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-6,8), setColor(brown()), t_type = 3, missionID = 0] }
		
		createTerrains(1){ [setxy(-5,-6), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,-5), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,-4), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,-3), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,-2), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,-1), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,0), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,1), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,2), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,3), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,4), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,5), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,6), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,7), setColor(brown()), t_type = 3, missionID = 0] }
		createTerrains(1){ [setxy(-5,8), setColor(brown()), t_type = 3, missionID = 0] }
		
	}
	
}

	