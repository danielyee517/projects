public class Board {

  private Piece[][] pieces; // stores the pieces in a 2-D array
  private boolean firesTurn;
  private final static int LENGTH = 8; // length & width of the board
  private int selectedX;
  private int selectedY;
  private boolean alreadySelected;
  private boolean alreadyMoved;
  private boolean alreadyCaptured;
  private Piece selectedPiece;

  //Constructs a new board
  public Board(boolean shouldBeEmpty) {
    pieces = new Piece[8][8];
    alreadyMoved = false;
    alreadyCaptured = false;
    alreadySelected = false;
    String type = "pawn";
    boolean isFire = true;
    selectedPiece = null;
    firesTurn = true;
    if(shouldBeEmpty){
      
    } else{ 
      for(int i=0; i<LENGTH; i++) {
        for(int j=0; j<LENGTH; j++) {
          if(i%2 == j%2 && j != 3 && j != 4){
            if(j>2){
              isFire = false; 
            }
            if(j<4){
              isFire = true;
            }
            if(j == 7 || j==0){
              type = "pawn";
            }
            if(j == 1 || j == 6){
              type = "shield";
            }
            if(j == 2 || j ==5){
              type = "bomb";
            }
            pieces[i][j] = new Piece(isFire, this, i, j, type);
          }
        }
      }
    }
   }

   public Piece pieceAt(int x, int y){
    if(x >= LENGTH || y >= LENGTH || x < 0 || y<0){
      return null;
    } else{
      return pieces[x][y];
    }
   }

   public boolean canSelect(int x, int y){
    if(x >= LENGTH || y>= LENGTH || x <0 || y<0){
      return false;
    }
    //selecting empty pieces for some reason
    if(pieces[x][y] != null && pieces[x][y].isFire() != firesTurn){
      //System.out.println("checkpoint-1"+selectedX+selectedY);
      return false;
    }
    if(alreadyMoved == false && selectedPiece == null && pieces[x][y] == null){
      //System.out.println("checkpoint0"+selectedX+selectedY);
      return false;
    }
    if(alreadyMoved == false && pieces[x][y] !=null && pieces[x][y].isFire() == firesTurn){
      //System.out.println("checkpoint1"+selectedX+selectedY);
      return true;
    } else if(selectedPiece != null && alreadyMoved == false && validMove(selectedX, selectedY,x,y)){
      //System.out.println("checkpoint2"+selectedX+selectedY);
      return true;
    } else if(alreadyMoved == true && selectedPiece != null && alreadyCaptured && validMove(selectedX, selectedY,x,y)){
      //System.out.println("checkpoint3"+selectedX+selectedY );
      return true;
    } else{
      //System.out.println("checkpoint4"+selectedX+selectedY );
      return false;
    }
   }

   private boolean validMove(int xi, int yi, int xf, int yf){
    Piece movingPiece = pieceAt(xi,yi);
    if (movingPiece ==null){
      return false;
    }
    if(xf < 0 || yf <0 || xf >= LENGTH || yf >= LENGTH){
      return false;
    }
    if(pieceAt(xf,yf) != null){
      return false;
    }
    if (Math.abs(xf-xi) >2 || Math.abs(yf-yi) >2 || (Math.abs(xf-xi) + Math.abs(yf-yi)) == 3){
      return false;
    }
    if(xi == xf){
      return false;
    }
    if(yi == yf){
      return false;
    }
    if(!movingPiece.isKing()){
      if(firesTurn){
        if(yf < yi){
          return false;
        }
      }else{
        if(yf > yi){
          return false;
        }
      }
    }
    if(validCapture(xi,yi,xf,yf)){
     //  System.out.println("almost end of validMove changed selectX");
      return true;
    }
    if (Math.abs(xf-xi) != 1 || Math.abs(yf-yi) != 1) {
      return false;
    }
     //System.out.println("end of validMove changed selectX");
    if(alreadyCaptured){
      return false;
    }
    return true;
    }

    private boolean validCapture(int xi, int yi, int xf, int yf){
      if(Math.abs(xf-xi) != 2 || Math.abs(yf-yi) != 2){
        return false;
      }
      if(Math.abs(xf-xi) == 1 || Math.abs(yf-yi) == 1){
        return false;
      }
      int captureX = (xf - xi) / 2  + xi;
      int captureY = (yf - yi) / 2 + yi;
      if(pieces[captureX][captureY]==null || pieces[xi][yi] == null){
        return false;
      }
        if(pieces[xi][yi].isFire() != pieces[captureX][captureY].isFire()){
          //System.out.println("validCapture changed selectX");
          return true;
      }
      return false;
    }
   

   public void select(int x, int y){
    if(pieces[x][y] != null){
      selectedX = x;
      selectedY = y;
      alreadySelected = true;
      selectedPiece = pieces[x][y];
    } else{
      if(Math.abs(selectedY - y) == 2){
        alreadyCaptured = true;
      }
      selectedPiece.move(x,y);
      alreadyMoved = true;
      selectedY = y;
      selectedX = x;
    }
   }

   public void place(Piece p, int x, int y){
    if(x <0 || y <0 || x >= LENGTH || y >= LENGTH || p ==null){
      return;
    }
    for(int i =0; i<LENGTH;i++){
      for(int j = 0; j<LENGTH; j++){
        if(p == pieces[i][j]){
          pieces[i][j] = null;
        }
      }
    }
    pieces[x][y] = p;
   }

   public Piece remove(int x, int y){
    if(x < 0 || y < 0 || x >= LENGTH || y >= LENGTH){
      System.out.println("Out of bounds.");
      return null;
    }
    Piece piece = pieces[x][y];
    if (piece ==null){
      System.out.println("Piece is null.");
      return null;
    }
    pieces[x][y] = null;
    return piece;
   }

   public boolean canEndTurn(){
    if(alreadyMoved){
      return true;
    }
    return false;
   }

   public void endTurn(){
    selectedPiece.doneCapturing();
    selectedPiece = null;
    firesTurn = !firesTurn;
    alreadySelected = false;
    alreadyMoved = false;
    alreadyCaptured = false;
    selectedX = -1;
    selectedY = -1;
   }

   public String winner(){
    int fire = 0;
    int water = 0;
    for (int i=0;i<LENGTH ;i++ ) {
      for (int j=0;j<LENGTH ;j++ ) {
      if(pieces[i][j] != null && pieces[i][j].isFire()){
        fire++;
      }
      if(pieces[i][j] != null && !(pieces[i][j].isFire())){
        water++;
      }
    }
    }
    if (water ==0 && fire == 0){
      return "No one";
    } else if(fire == 0){
      return "Water";
    } else if(water == 0){
      return "Fire";
    } else {
      return null;
    }
   }

   private void drawBoard(int N) { 
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if ((j + i) % 2 == 0) {
                  StdDrawPlus.setPenColor(StdDrawPlus.GRAY);
                  if (!(selectedPiece == null)){
                    if (selectedY == j && selectedX == i){
                      StdDrawPlus.setPenColor(StdDrawPlus.WHITE);
                    }
                  }
                }
                
                else {
                  StdDrawPlus.setPenColor(StdDrawPlus.RED);
                }
                StdDrawPlus.filledSquare(i + .5, j + .5, .5);
                if (pieces[i][j]!=null){
                    if (pieces[i][j].isFire()){
                        if (pieces[i][j].isBomb()){
                            if (pieces[i][j].isKing()) {
                                StdDrawPlus.picture(i + .5, j + .5, "img/bomb-fire-crowned.png", 1,1);
                            }
                            StdDrawPlus.picture(i + .5, j + .5, "img/bomb-fire.png", 1,1);
                        }
                        else if (pieces[i][j].isShield()){
                            if (pieces[i][j].isKing()) {
                                StdDrawPlus.picture(i + .5, j + .5, "img/shield-fire-crowned.png", 1,1);
                            }
                            StdDrawPlus.picture(i + .5, j + .5, "img/shield-fire.png", 1,1);
                        }
                        else{
                            if (pieces[i][j].isKing()) {
                               StdDrawPlus.picture(i + .5, j + .5, "img/pawn-fire-crowned.png", 1,1);
                            }
                            StdDrawPlus.picture(i + .5, j + .5, "img/pawn-fire.png", 1,1);
                        }                        
                    }
                    else { //waterside
                        if (pieces[i][j].isBomb()){
                            if (pieces[i][j].isKing()) {
                                StdDrawPlus.picture(i + .5, j + .5, "img/bomb-water-crowned.png", 1,1);
                            }
                            StdDrawPlus.picture(i + .5, j + .5, "img/bomb-water.png", 1,1);
                        }
                        else if (pieces[i][j].isShield()){
                            if (pieces[i][j].isKing()) {
                                StdDrawPlus.picture(i + .5, j + .5, "img/shield-water-crowned.png", 1,1);
                            }
                            StdDrawPlus.picture(i + .5, j + .5, "img/shield-water.png", 1,1);
                        }
                        else {
                            if (pieces[i][j].isKing()) {
                                StdDrawPlus.picture(i + .5, j + .5, "img/pawn-water-crowned.png", 1,1);
                            }
                            StdDrawPlus.picture(i + .5, j + .5, "img/pawn-water.png", 1,1);
                        }                        
                    }
                }
            }
        }
    }

   public static void main(String[] args) {
    int mouseX;
    int mouseY;
    StdDrawPlus.setXscale(0,8);
    StdDrawPlus.setYscale(0,8);
    Board b = new Board(false);
        while(b.winner() == null){
          b.drawBoard(LENGTH);
          if (StdDrawPlus.mousePressed()){
            mouseX = (int) StdDrawPlus.mouseX();
            mouseY = (int) StdDrawPlus.mouseY();
            if(b.canSelect(mouseX, mouseY)){
              b.select(mouseX, mouseY);
              //System.out.println("canSelect");
            }
          }
          if(StdDrawPlus.isSpacePressed()){
            if(b.canEndTurn()){
              b.endTurn();
            }
          }
          StdDrawPlus.show(100);
        }
        System.out.println(b.winner());
    }
 }
