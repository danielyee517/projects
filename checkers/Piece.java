public class Piece{
	private Board b;
	private boolean isKing;
	private int x;
	private int y;
	private String type;
	private boolean isFire;
	private boolean hasCaptured;

	public Piece(boolean isFire, Board b, int x, int y, String type){
		this.isFire = isFire;
		this.b = b;
		this.x = x;
		this.y = y;
		this.type = type;
		this.isKing = false;
		hasCaptured = false;
	}
	
	public boolean isFire(){
		return isFire;
	}

	public boolean isKing(){
		return isKing;
	}

	public boolean isBomb(){
		return (type == "bomb");
	}

	public boolean isShield(){
		return (type == "shield");
	}

	public int side(){
		if(isFire){
			return 0;
		} else {
			return 1;
		}
	}

	public void move(int x, int y){
		if(isFire && y == 7){
			isKing = true;
		}
		if(!isFire && y==0){
			isKing = true;
		}
		boolean captureMove = false;
		if(Math.abs(this.x - x) == 2){
			captureMove = true;
			hasCaptured = true;
			int captureX = (x - this.x) / 2  + this.x;
      		int captureY = (y - this.y) / 2 + this.y;
      		b.remove(captureX,captureY);
		}
		if(isBomb() && captureMove ) {
			if(b.pieceAt(x-1,y-1) != null && !b.pieceAt(x-1,y-1).isShield()){
				b.remove(x-1,y-1);
			}
			if(b.pieceAt(x+1,y+1) != null && !b.pieceAt(x+1,y+1).isShield()){
				b.remove(x+1,y+1);
			}
			if(b.pieceAt(x-1,y+1) != null && !b.pieceAt(x-1,y+1).isShield()){
				b.remove(x-1,y+1);
			}
			if(b.pieceAt(x+1,y-1) != null && !b.pieceAt(x+1,y-1).isShield()){
				b.remove(x+1,y-1);
			}
			b.remove(this.x,this.y);
		}
		this.x = x;
		this.y = y;
		if(isBomb() && captureMove){
			
		} else{
			b.place(this,x,y);
		}
	}

	public boolean hasCaptured(){
		return hasCaptured;
	}

	public void doneCapturing(){
		hasCaptured = false;
	}
}