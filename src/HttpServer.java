import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;


public class HttpServer{

	public static void main(String[] args) throws IOException{
		new HttpServer().start();
	}
	/* klient skickar get från webbläsare till server
	 *  servern skapar en cookie för klienten och returnerar html
	 *  klienten gissar svaret
	 *  servern kopplar klient - cookie och svarar baserat på tidigare svar
	 *  klienten svarar rätt, cookie tas bort
	 *  servern säger grattis
	 * */

	ConcurrentLinkedQueue<Game> games = new ConcurrentLinkedQueue<Game>();
	int idCounter = 0;
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
			StringTokenizer tokens = new StringTokenizer(str," ?");
			tokens.nextToken(); // Ordet GET
			String requestedDocument = tokens.nextToken();
			boolean gotCookie = false;
			String cookieHeader;
			while( (str = request.readLine()) != null && str.length() > 0){
				if (str.contains("Cookie: ")){
					gotCookie = true;
					cookieHeader = str;
				}
				System.out.println(str);
			}
			if (gotCookie = false){
				idCounter++;
				createCookie(requestedDocument, s);		
			}
			System.out.println("Förfrågan klar.");
			s.shutdownInput();

			respond();


		}
	}
	public void createCookie(String requestedDocument, Socket s) throws IOException{
		PrintStream response = new PrintStream(s.getOutputStream());
		response.println("HTTP/1.1 200 OK");
		response.println("Server : Johannas server");
		response.println("Content-Type: text/html");
		response.println("Set-Cookie: clientId="+idCounter+"; expires=Sunday,10-Feb-13 21:00:00 GMT");
		File f = new File("intnet.txt");
		FileInputStream infil = new FileInputStream(f);
		byte[] b = new byte[1024];
		while( infil.available() > 0){
			response.write(b,0,infil.read(b));
		}
		infil.close();
		s.shutdownOutput();
		s.close();
	}
	public void respond(){

	}


	private class Game{
		int sessionID;
		int answer;
		int guess;
		Random r;
		String message;
		public Game(int ID){
			sessionID = ID;
			answer = r.nextInt(101);
		}
		public int getID(int ID){
			return sessionID;
		}
		public String position(){
			if (guess > answer){
				message = "Guess higher!"; 
			}
			else if(guess == answer){
				message = "You guessed the right number!";
			}
			else {message = "Guess lower!";}
			return message;

		}

	}
}