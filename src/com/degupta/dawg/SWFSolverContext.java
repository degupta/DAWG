package com.degupta.dawg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SWFSolverContext {
	public char[][] board;
	public HashMap<String, ArrayList<SWFSolver.Direction>> words = null;
	public HashSet<String> uniqueWords = null;
	public HashMap<String, Integer> startPositions = null;
	public boolean[][] visited = new boolean[SWFSolver.BOARD_SIZE][SWFSolver.BOARD_SIZE];
	StringBuilder currentWord = new StringBuilder();
	public ArrayList<SWFSolver.Direction> curDirections = null;
	public int startRow, startCol;

	public SWFSolverContext(boolean directions) {
		if (directions) {
			words = new HashMap<String, ArrayList<SWFSolver.Direction>>();
			startPositions = new HashMap<String, Integer>();
			curDirections = new ArrayList<SWFSolver.Direction>();
		} else {
			uniqueWords = new HashSet<String>();
		}
	}
}
