package io.swagslash.settlersofcatan.controller;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import io.swagslash.settlersofcatan.Player;
import io.swagslash.settlersofcatan.SettlerApp;
import io.swagslash.settlersofcatan.pieces.Board;
import io.swagslash.settlersofcatan.pieces.Edge;
import io.swagslash.settlersofcatan.pieces.Vertex;

/**
 * Created by Christoph Wedenig (christoph@wedenig.org) on 07.06.18.
 */
public class PhaseController {

    Map<Board.Phase, Board.Phase> transitionMap;
    private Board.Phase currentPhase;

    public PhaseController() {
        currentPhase = Board.Phase.IDLE;
        transitionMap = new HashMap<>();
        transitionMap.put(Board.Phase.IDLE, Board.Phase.PRODUCTION);
        transitionMap.put(Board.Phase.PRODUCTION, Board.Phase.PLAYER_TURN);
        transitionMap.put(Board.Phase.PLAYER_TURN, Board.Phase.IDLE);
        transitionMap.put(Board.Phase.FREE_SETTLEMENT, Board.Phase.PLAYER_TURN); // todo fix

    }

    public Board.Phase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(Board.Phase phase) {
        Log.d("PLAYER", "Phase transition: " + currentPhase +  " >> " + phase );
        this.currentPhase = phase;
    }

    public boolean isAllowedToBuildOnVertex(Vertex vertex) {
        return (currentPhase == Board.Phase.SETUP_SETTLEMENT && vertex.canBuildSettlement(SettlerApp.getPlayer())) ||
                (currentPhase == Board.Phase.FREE_SETTLEMENT && vertex.canBuildFreeSettlement(SettlerApp.getPlayer())) ||
                (currentPhase == Board.Phase.SETUP_CITY && vertex.canBuildCity(SettlerApp.getPlayer()));
    }

    public boolean isAllowedToBuildOnEdge(Edge edge) {
        return (currentPhase == Board.Phase.SETUP_ROAD || currentPhase == Board.Phase.FREE_ROAD) &&
                edge.canBuildRoad(SettlerApp.getPlayer());
    }

//    public void advancePhase() {
//        this.setCurrentPhase(transitionMap.get(currentPhase));
//        System.out.println(getCurrentPhase().toString());
//    }
}
