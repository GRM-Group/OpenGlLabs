package eu.grmdev.senryaku.core.map;

import java.util.*;

import org.joml.Vector2i;

import eu.grmdev.senryaku.Config;
import eu.grmdev.senryaku.core.IGame;
import eu.grmdev.senryaku.core.entity.Entity;
import eu.grmdev.senryaku.game.GameSave;
import eu.grmdev.senryaku.game.Player;
import lombok.Getter;
import lombok.Setter;

public class LevelManager {
	private Map<Integer, GameMap> maps;
	private @Getter GameMap currentMap;
	private @Setter Player player;
	private @Setter Entity portal;
	private static @Getter LevelManager instance;
	private IGame game;
	
	public LevelManager(IGame game) {
		instance = this;
		this.game = game;
		this.maps = new HashMap<>();
	}
	
	/**
	 * Changes map to map_'i' index
	 * 
	 * @param i
	 *           map index
	 * @throws Exception
	 */
	public void goTo(int i) throws Exception {
		if (maps.containsKey(i)) {
			currentMap = maps.get(i);
			currentMap.reset();
		} else {
			currentMap = GameMapFactory.create(i, game);
			maps.put(i, currentMap);
		}
		setStartEndObjectsPositions(currentMap.getStartPos(), currentMap.getEndPos());
	}
	
	public void restartLevel() {
		try {
			currentMap.reset();
			setStartEndObjectsPositions(currentMap.getStartPos(), currentMap.getEndPos());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setStartEndObjectsPositions(Vector2i start, Vector2i end) throws Exception {
		player.setPosition(start.x, Config.PLAYER_DEF_Y_POS.<Float> get(), start.y);
		portal.setPosition(end.x, Config.PLAYER_DEF_Y_POS.<Float> get(), end.y);
		currentMap.setStepCounter(0);
	}
	
	public void goToNextLevel() throws Exception {
		int i = currentMap.getLevel();
		goTo(++i);
	}
	
	public void save() {
		for (Iterator<Integer> it = maps.keySet().iterator(); it.hasNext();) {
			Integer level = it.next();
			GameSave.save(level, maps.get(level));
		}
	}
	
	public boolean nextMapExist() {
		int i = currentMap.getLevel();
		return GameMapFactory.exist(i + 1);
	}
	
	public void update(float interval) {
		currentMap.update(interval);
	}
}
