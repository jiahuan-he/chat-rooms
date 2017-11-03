using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.IO;
using ClientNamespace;
using System.Threading;

namespace ClientNamespace
{
    public class Client
    {


		public static void CallToChildThread()
		{
			Console.Write("out start");
		}

        Client(){
			TcpClient clientSocket = new System.Net.Sockets.TcpClient();
   			clientSocket.Connect("127.0.0.1", 8088);
            ClientOut clientOut = new ClientOut(clientSocket);

			ThreadStart threadStart = new ThreadStart(clientOut.run);
            Thread thread = new Thread(threadStart);
            thread.Start();			

			StreamReader inStream = new StreamReader(clientSocket.GetStream(), Encoding.UTF8);
			string line;
			while ((line = inStream.ReadLine()) != null)
			{
                Console.WriteLine(line);
			}
        }
       	
        static void Main(string[] args)
        {
            new Client();
        }
    }
}
