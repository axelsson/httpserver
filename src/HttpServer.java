import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HttpServer{

	HashMap<Integer, Game> games = new HashMap();
//	ArrayList<Game> games = new ArrayList<Game>();
	int idCounter = 0;
	boolean gotCookie = false;
	String cookieHeader="";
	int guess = -1;
	boolean victory = false;

	public static void main(String[] args) throws IOException{
		new HttpServer().start();
	}
	public void start() throws IOException{
		System.out.println("Skapar Serversocket");
		ServerSocket ss = new ServerSocket(80);

		while(true){
			System.out.println("Väntar på klient...");
			Socket s = ss.accept();
			System.out.println("Klient är ansluten");
			BufferedReader request = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String str = request.readLine();
			System.out.println(str);
			if (str.contains("guess")){
				int begin = str.indexOf("=");
				guess = Integer.parseInt(str.substring(begin+1, begin+3).trim());
				System.out.println("guess: "+guess);
			}
			if (str.contains(".ico")){
				s.close();
				continue;
			}

			//			StringTokenizer tokens = new StringTokenizer(str," ?");
			//			tokens.nextToken(); // Ordet GET
			//			String requestedDocument = tokens.nextToken();
			//			System.out.println("requested doc: " +requestedDocument);

			while((str = request.readLine()) != null && str.length() > 0){
				if (str.contains("Cookie")){
					gotCookie = true;
					cookieHeader = str;
				}
				System.out.println(str);
			}
			System.out.println("Förfrågan klar.");
			s.shutdownInput();	//ger ett men kraschar efter en stund om man kör från egen dator
			if (gotCookie == false){
				createCookie(s);		
			}
			else{
				respond(cookieHeader,s);
			}


		}
	}
	//Responds and creates a cookie for a new user. 
	public void createCookie(Socket s) throws IOException{
		System.out.println("Creating new cookie...");
		idCounter++;
		PrintStream response = new PrintStream(s.getOutputStream());
		response.println("HTTP/1.1 200 OK");
		response.println("Server : Johannas server");
		response.println("Content-Type: text/html");
		response.println("Set-Cookie: clientId="+idCounter+"; expires=Sunday,10-Feb-13 21:00:00 GMT");
		//create new object game with idCouter.
		Game newGame = new Game(idCounter);
		games.put(idCounter, newGame);

		File f = new File("C:/Users/Johanna/workspace/Iproglabb2/src/intnet3.txt");	//Filenotfound
		FileInputStream infil = new FileInputStream(f);
		byte[] b = new byte[1024];
		while( infil.available() > 0){
			response.write(b,0,infil.read(b));
		}
		infil.close();
		s.shutdownOutput();
		s.close();
		System.out.println("Cookie created and sent.");
	}

	//Responds to requests sent from users with active games.
	public void respond(String cookie, Socket s) throws IOException{

		//getting the sessionID of the player
		int answer;
		Game game = null;
		String state = null;
		int index = cookie.indexOf("=");
		int sessionID = Integer.parseInt(cookie.substring(index+1));
		System.out.println("sessionID is: "+ sessionID);
		
		if (games.containsKey(sessionID)){
			game = games.get(sessionID);
		}
//		for (Game current : games) {
//			if(current.getID()==sessionID){
//				game  = current;
//			}
//		}
		if (game == null){
			createCookie(s);
			return;
		}
		else{
			answer = game.getAnswer();
			System.out.println("här är svaret: "+answer);
			state = getResult(answer, game);
		}
		//använd sessionID för att hämta game, returnera htmlsida med olika svar beroende på gissning

		System.out.println("Responding to client...");
		PrintStream response = new PrintStream(s.getOutputStream());
		response.println("HTTP/1.1 200 OK");
		response.println("Server : Johannas server");
		response.println("Content-Type: text/html");

		File f = new File("C:/Users/Johanna/workspace/Iproglabb2/src/intnet.txt");
		FileInputStream infil = new FileInputStream(f);
		byte[] b = new byte[1024];
		while( infil.available() > 0){
			response.write(b,0,infil.read(b));
		}
		infil.close();
		response.println(state+"<br> You have made "+game.getTurn() + " guess(es).");
		File f2 = new File("C:/Users/Johanna/workspace/Iproglabb2/src/intnet2.txt");
		FileInputStream infil2 = new FileInputStream(f2);
		byte[] b2 = new byte[1024];
		while( infil2.available() > 0){
			response.write(b2,0,infil2.read(b2));
		}
		if (victory == true){
			game.resetGame();
			victory = false;
			//generate new answer
			//reset turn
//			games.remove(sessionID);
		}

		infil2.close();
		s.shutdownOutput();
		s.close();
	}

	public String getResult(int answer, Game game){
		String result = "Congratulations, you won!";		//win
		System.out.println("här är gissningen: " + guess);
		if (answer < guess){//guess higher	
			result = "Guess lower.";
			System.out.println("turn ändras1: "+game.getTurn());
			game.increaseTurn();
		}
		else if(answer > guess){//guess lower
			result = "Guess higher.";
			System.out.println("turn ändras2: "+game.getTurn());
			game.increaseTurn();
		}
		else{
			victory = true;
			game.increaseTurn();
		}
		return result;
	}


	private class Game{
		int sessionID;
		int answer;
		int turn = 0;
		Random r = new Random();
		String message = "";
		public Game(int ID){
			sessionID = ID;
			answer = r.nextInt(101);
		}
		public void resetGame(){
			answer = r.nextInt(101);
			turn = 0;
		}
		public void increaseTurn(){
			turn++;
		}
		public int getTurn(){
			return turn;
		}
		public int getID(){
			return sessionID;
		}
		public int getAnswer(){
			return answer;
		}
	}
}