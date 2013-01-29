import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

/* klient skickar get från webbläsare till server
 *  servern skapar en cookie för klienten och returnerar html
 *  klienten gissar svaret
 *  servern kopplar klient - cookie och svarar baserat på tidigare svar
 *  klienten svarar rätt, cookie tas bort
 *  servern säger grattis
 * */
public class HttpServer{

//	HttpCookie cookie = new HttpCookie("ett","tva");
//	ConcurrentLinkedQueue<Game> games = new ConcurrentLinkedQueue<Game>();
	ArrayList<Game> games = new ArrayList<Game>();
	int idCounter = 0;
	boolean gotCookie = false;
	String cookieHeader="";
	int guess = -1;
	
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
				int index = str.indexOf("=");
				int guess = Integer.parseInt(str.substring(index+1, index+2));
				System.out.println("guess: "+guess);
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
				idCounter++;
				createCookie(s);		
			}
			else{
				respond(guess, cookieHeader,s);
				}
			

		}
	}
	//Responds and creates a cookie for a new user. 
	public void createCookie(Socket s) throws IOException{
		System.out.println("Creating new cookie...");
		PrintStream response = new PrintStream(s.getOutputStream());
		response.println("HTTP/1.1 200 OK");
		response.println("Server : Johannas server");
		response.println("Content-Type: text/html");
		response.println("Set-Cookie: clientId="+idCounter+"; expires=Sunday,10-Feb-13 21:00:00 GMT");
		//create new object game with idCouter.
		Game newGame = new Game(idCounter);
		games.add(newGame);
		
		File f = new File("C:/Users/Johanna/workspace/Iproglabb2/src/intnet.txt");	//Filenotfound
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
	public void respond(int guess,String cookie, Socket s) throws IOException{
		
		//getting the sessionID of the player
		int answer;
		Game game = null;
		String state = null;
		int index = cookie.indexOf("=");
		int sessionID = Integer.parseInt(cookie.substring(index+1));
		System.out.println("sessionID is: "+ sessionID);

		for (Game current : games) {
			if(current.getID()==sessionID){
				answer = current.getAnswer();
				game  = current;
				state = getResult(answer, guess, current);
			}
		}
		//använd sessionID för att hämta game, returnera htmlsida med olika svar beroende på gissning
		
		System.out.println("Responding to client...");
		PrintStream response = new PrintStream(s.getOutputStream());
		response.println("HTTP/1.1 200 OK");
		response.println("Server : Johannas server");
		response.println("Content-Type: text/html");

		
		//om gissning rätt -> stäng connection och ta bort game
		File f = new File("C:/Users/Johanna/workspace/Iproglabb2/src/intnet.txt");
		FileInputStream infil = new FileInputStream(f);
		byte[] b = new byte[1024];
		while( infil.available() > 0){
			response.write(b,0,infil.read(b));
		}
		response.println(state+"\n You have made "+game.turn + " number of guess(es).");
		infil.close();
		s.shutdownOutput();
		s.close();
	}
	
	public String getResult(int answer, int guess, Game game){
		String result = "Congratulations, you won!";		//win
		if (answer > guess){//guess lower	
			result = "Guess lower";
			game.turn++;
		}
		else if(answer < guess){//guess higher
			result = "Guess higher";
			game.turn++;
		}
		else{
			games.remove(game);
		}
		return result;
	}

	//class Game represents an ongoing game, storing sessionID and guess.
//	skapa: skapa answer och sessionID
//	spela:  hitta rätt game mha sessionID
//			hämta answer
//			om fel, uppdatera gissningar
	private class Game{
		int sessionID;
		int answer;
		int turn = 0;
		Random r = new Random();
		String message;
		public Game(int ID){
			sessionID = ID;
			answer = r.nextInt(101);
		}
		public int getID(){
			return sessionID;
		}
		public int getAnswer(){
			return answer;
		}


	}
}