using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.IO;


namespace ClientNamespace
{
    public class ClientOut
    {
        TcpClient socket;
        public ClientOut(TcpClient socket)
        {
            this.socket = socket;
        }
        public void run(){
            StreamWriter outStream = new StreamWriter(socket.GetStream());
            outStream.AutoFlush = true;
            String message; 
            while( (message=Console.ReadLine()) != null){
                outStream.WriteLine(message);
                }
        }
    }
}
