import static org.junit.Assert.*;
import org.junit.Test;

public class BoardTest {

	@Test
	public void testPieceAt() {
		Board B = new Board(true);
		Piece P = new Piece(true, B, 5, 5, "pawn");
		Piece S = new Piece(false, B, 4, 4, "shield");
		B.place(P, 5, 5);
		B.place(S, 4, 4);
		assertEquals(P, B.pieceAt(5, 5));
		assertEquals(S, B.pieceAt(4, 4));
		assertEquals(null, B.pieceAt(4, 5));
	}

	@Test
	public void winnerCenter() {
		Board B = new Board(true);
		assertEquals("No one", B.winner());
		Piece P = new Piece(false, B, 5, 5, "pawn");
		Piece S = new Piece(true, B, 4, 4, "shield");
		B.place(P, 5, 5);
		B.place(S, 4, 4);
		assertEquals(null, B.winner());
		B.select(4, 4);
		B.select(6, 6);
		assertEquals("Fire", B.winner());
	}

	@Test
	public void winnerEdge() {
		Board B = new Board(true);
		assertEquals("No one", B.winner());
		Piece P = new Piece(true, B, 3, 5, "pawn");
		Piece S = new Piece(false, B, 2, 6, "pawn");
		B.place(P, 3, 5);
		B.place(S, 2, 6);
		assertEquals(null, B.winner());
		B.select(3, 5);
		B.select(1, 7);
		assertEquals(true, B.pieceAt(1, 7).hasCaptured());
		assertEquals(null, B.pieceAt(3, 5));
		assertEquals(null, B.pieceAt(2, 6));
		assertEquals(P, B.pieceAt(1, 7));
		assertEquals("Fire", B.winner()); 
	}

	@Test 
	public void testRemove() {
		Board B = new Board(true);
		Piece P = new Piece(true, B, 5, 5, "pawn");
		Piece S = new Piece(false, B, 4, 4, "shield");
		B.place(P, 5, 5);
		B.place(S, 4, 4);
		assertEquals(P, B.remove(5, 5));
		assertEquals(null, B.remove(5, 5));
		assertEquals(null, B.pieceAt(5, 5));
		assertEquals(null, B.remove(9, 2));
	}

	@Test
	public void simpleCapture() {
		Board B = new Board(true);
		Piece P = new Piece(false, B, 5, 5, "pawn");
		Piece S = new Piece(true, B, 4, 4, "shield");
		B.place(P, 5, 5);
		B.place(S, 4, 4);
		assertEquals(false, B.canSelect(6, 6));
		assertEquals(true, B.canSelect(4, 4));
		assertEquals(false, B.canEndTurn());
		B.select(4, 4);
		assertEquals(true, B.canSelect(4, 4));
		assertEquals(false, B.canSelect(5, 5));
		assertEquals(true, B.canSelect(6, 6));
		assertEquals(false, B.canEndTurn());
		B.select(6, 6);
		assertEquals(null, B.pieceAt(4, 4));
		assertEquals(null, B.pieceAt(5, 5));
		assertEquals(S, B.pieceAt(6, 6));
		assertEquals(true, B.canEndTurn());
		B.endTurn();
	}

	@Test
	public void testSimpleBombsplosion() {
		Board B = new Board(true);
		Piece Attack = new Piece(true, B, 4, 4, "bomb");
		Piece Victim = new Piece(false, B, 5, 5, "shield");
		Piece Friend = new Piece(true, B, 7, 7, "pawn");
		Piece EnemyS = new Piece(false, B, 7, 5, "shield");
		Piece EnemyP = new Piece(false, B, 5, 7, "bomb");
		B.place(Attack, 4, 4);
		B.place(Victim, 5, 5);
		B.place(Friend, 5, 7);
		B.place(EnemyS, 7, 5);
		B.place(EnemyP, 5, 7);
		B.select(4, 4);
		B.select(6, 6);
		assertEquals(null, B.pieceAt(6, 6));
		assertEquals(null, B.pieceAt(5, 5));
		assertEquals(null, B.pieceAt(7, 7));
		assertEquals(null, B.pieceAt(5, 7));
		assertEquals(EnemyS, B.pieceAt(7, 5));
		assertEquals("Water", B.winner());
	}

	@Test 
	public void testFalseKingBombsplosion() {
		Board B = new Board(true);
		Piece Attack = new Piece(true, B, 1, 5, "bomb");
		Piece Victim = new Piece(false, B, 2, 6, "shield");
		Piece Casualties = new Piece(true, B, 4, 6, "pawn");
		B.place(Attack, 1, 5);
		B.place(Victim, 2, 6);
		B.place(Casualties, 4, 6);
		B.select(1, 5);
		B.select(3, 7);
		assertEquals(null, B.pieceAt(3, 7));
		assertEquals(null, B.pieceAt(2, 6));
		assertEquals(null, B.pieceAt(4, 6));
		assertEquals("No one", B.winner());
	}

	@Test
	public void testSimpleTurnover() {
		Board B = new Board(true);
		Piece P = new Piece(true, B, 5, 5, "pawn");
		Piece S = new Piece(false, B, 4, 4, "shield");
		B.place(P, 5, 5);
		B.place(S, 4, 4);
		assertEquals(false, B.canSelect(4, 4));
		assertEquals(true, B.canSelect(5, 5));
		assertEquals(false, B.canEndTurn());
		B.select(5, 5);
		B.select(6, 6);
		assertEquals(false, B.canSelect(4, 4));
		assertEquals(false, B.canSelect(5, 5));
		assertEquals(false, B.canSelect(6, 6));
		assertEquals(true, B.canEndTurn());
		B.endTurn();
		assertEquals(false, B.canEndTurn());
		assertEquals(true, B.canSelect(4, 4));
		assertEquals(false, B.canSelect(5, 5));
		assertEquals(false, B.canSelect(6, 6));
	}

	@Test
	public void testKing() {
		Board B = new Board(true);
		Piece P = new Piece(true, B, 6, 6, "pawn");
		Piece E = new Piece(false, B, 2, 2, "bomb");
		B.place(P, 6, 6);
		B.place(E, 2, 2);
		assertEquals(false, B.canSelect(7, 7));
		assertEquals(true, B.canSelect(6, 6));
		assertEquals(false, B.pieceAt(6, 6).isKing());
		B.select(6, 6);
		assertEquals(true, B.canSelect(6, 6));
		assertEquals(true, B.canSelect(7, 7));
		assertEquals(false, B.canSelect(5, 5));
		B.select(6, 6);
		B.select(7, 7);
		assertEquals(false, B.canSelect(7, 7));
		assertEquals(false, B.canSelect(6, 6));
		assertEquals(true, B.canEndTurn());
		assertEquals(true, B.pieceAt(7, 7).isKing());
		B.endTurn();
		assertEquals(false, B.canEndTurn());
		assertEquals(true, B.canSelect(2, 2));
		B.select(2, 2);
		B.select(1, 1);
		B.endTurn();
		assertEquals(true, B.canSelect(7, 7));
		assertEquals(true, B.pieceAt(7, 7).isKing());
		assertEquals(false, B.canSelect(6, 6));
		B.select(7, 7);
		assertEquals(true, B.canSelect(7, 7));
		assertEquals(true, B.canSelect(6, 6));
		B.select(6, 6);
		assertEquals(true, B.canEndTurn());
		assertEquals(P, B.pieceAt(6, 6));
		assertEquals(null, B.pieceAt(7, 7));
		assertEquals(true, B.pieceAt(6, 6).isKing());
	}

	@Test
	public void testSuperKingCapture() {
		Board B = new Board(true);
		Piece Scrub = new Piece(true, B, 6, 6, "bomb");
		Piece Kingly = new Piece(false, B, 6, 2, "shield");
		Piece VicOne = new Piece(true, B, 5, 1, "bomb");
		Piece VicTwo = new Piece(true, B, 3, 1, "pawn");
		Piece VicThree = new Piece(true, B, 3, 3, "shield");
		B.place(Scrub, 6, 6);
		B.place(Kingly, 6, 2);
		B.place(VicOne, 5, 1);
		B.place(VicTwo, 3, 1);
		B.place(VicThree, 3, 3);
		B.select(6, 6);
		B.select(5, 7);
		assertEquals(true, Scrub.isKing());
		B.endTurn();
		B.select(6, 2);
		assertEquals(true, B.canSelect(4, 0));
		assertEquals(false, B.canSelect(5, 1));
		B.select(4, 0);
		assertEquals(true, B.canSelect(2, 2));
		B.select(2, 2);
		assertEquals(true, B.canSelect(4, 4));
		B.select(4, 4);
		assertEquals(true, B.canEndTurn());
		assertEquals(null, B.pieceAt(5, 1));
		assertEquals(null, B.pieceAt(3, 1));
		assertEquals(null, B.pieceAt(3, 3));
		assertEquals(true, B.pieceAt(4, 4).isKing());
		assertEquals(true, Scrub.isKing());
		assertEquals(null, B.winner());
	}

	@Test
	public void testRegicide() {
		Board B = new Board(true);
		Piece Ceasar = new Piece(true, B, 6, 6, "bomb");
		Piece Kingly = new Piece(false, B, 6, 2, "shield");
		Piece VicOne = new Piece(true, B, 5, 1, "bomb");
		Piece VicTwo = new Piece(true, B, 3, 1, "pawn");
		Piece VicThree = new Piece(true, B, 3, 3, "shield");
		B.place(Ceasar, 6, 6);
		B.place(Kingly, 6, 2);
		B.place(VicOne, 5, 1);
		B.place(VicTwo, 3, 1);
		B.place(VicThree, 3, 3);
		B.select(6, 6);
		B.select(5, 7);
		B.place(Ceasar, 3, 5);
		B.endTurn();
		B.select(6, 2);
		B.select(4, 0);
		B.select(2, 2);
		B.select(4, 4);
		B.select(2, 6);
		assertEquals("Water", B.winner());
	}






	public static void main(String... args) {
        jh61b.junit.textui.runClasses(BoardTest.class);
    }
}