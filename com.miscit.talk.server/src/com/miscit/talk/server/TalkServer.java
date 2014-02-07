package com.miscit.talk.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class TalkServer {
	public static void main(String[] args) {
		TalkThread thread = null;
		ServerSocket server = null;
		try {
			server =new ServerSocket(1111);
			
			System.out.println("Wait");
			
			HashMap<String, PrintWriter> talkMap = new HashMap<>();
			
			while (true) {
				Socket socket = server.accept();
				
				thread = new TalkServer().new TalkThread(socket, talkMap);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class TalkThread extends Thread {
		private Socket socket;
		private String UID;
		private BufferedReader br;
		
		HashMap<String, PrintWriter> talk;
		
		public TalkThread(Socket socket, HashMap<String, PrintWriter> talk) {
			this.socket = socket;
			this.talk = talk;
			
			try {
				PrintWriter pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));
				this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				broadcast(UID + "立加");
				
				synchronized (talk) {
					talk.put(this.UID, pw);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			try {
				String line = null;
				while ((line = br.readLine()) != null) {
					if (line.equals("/quit")) {
						break;
					}
					
					if (line.indexOf("/to") == 0) {
						sendmsg(line);
					} else {
						broadcast(UID + " : " + line);
						System.out.println(UID + " : " + line);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				synchronized (talk) {
					talk.remove(UID);
				}
			}
			
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void broadcast(String msg) {
			synchronized (talk) {
				Collection<PrintWriter> collection = talk.values();
				Iterator<PrintWriter> iter = collection.iterator();
				
				while (iter.hasNext()) {
					PrintWriter pw = (PrintWriter) iter.next();
					pw.println(msg);
					pw.flush();
				}
			}
		}
		
		public void sendmsg(String msg) {
			int start = msg.indexOf(" ") + 1;
			int end = msg.indexOf(" ", start);
			
			if (end != -1) {
				String to = msg.substring(start, end);
				String wmsg = msg.substring(end + 1);
				
				PrintWriter pw = talk.get(to);
				if (pw != null) {
					pw.println(this.UID + " 丛狼 庇加富 : " + wmsg);
					pw.flush();
				}
			}
		}
	}
}
