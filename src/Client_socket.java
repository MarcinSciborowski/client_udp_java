import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Program przeznaczony dla klienta.
 * Jego zadaniem jest przeszukanie sieci lokalnej w celu znalezienia urządzeń (serwerów).
 * Tymi urządzeniami są RPi oraz BRIX.
 * Program ma znaleźć wszystkie parametry urządzeń i pozwolić na ich odsyłanie w celu edycji.
 * @author bacom
 */
public class Client_socket {
    String ownIP;
    String new_ip;
    String mac_ad;
    String respo;
    
    private static final Logger logger = udp_gui.log();
    
    /**
     * Funkcja wysyła na adres rozgłoszeniowy żądanie zgłosznia się wszystkich 
     * urządzeń podłączyonych do sieci lokalnej. 
     * @return String Zwraca ramkę z parametrami urządzeń.
     */
    public String run()
    {
        int port = 8421;
        String msg = "broadcast_search";
        char flag = '1';
        String code_id = "ZXF2L";
        String finals = "";
        
        try 
        {
            DatagramSocket ds = new DatagramSocket(port);
            ds.setBroadcast(true);
            
            byte[] sendData = (flag + ";" + code_id + ";" + msg).getBytes();
            //Próba z adresem 255.255.255.255
            try 
            {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), port);
            ds.send(sendPacket);
            System.out.println(getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
            logger.log(Level.SEVERE, "{0}" ,getClass().getName() + ">>> Request packet sent to: 255.255.255.255 (DEFAULT)");
            } 
            catch (Exception e) 
            {
                logger.log(Level.SEVERE, "{0}" ,e);
            }
            System.out.println(getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
            logger.log(Level.SEVERE, "{0}" ,getClass().getName() + ">>> Done looping over all network interfaces. Now waiting for a reply!");
            String res = " ";
            ds.setSoTimeout(2000);
            while(true)
            {
                try
                {
                //Oczekiwanie na odpowiedź
                byte[] recvBuf = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                ds.receive(receivePacket);
                //Po otrzymaniu odpowiedzi - wyświetlenie adresu IP serwera
                System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
                logger.log(Level.SEVERE, "{0}" ,getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
                //Filtrowanie pakietów pochodzących z urządznia rozgłoszeniowego
                try 
                {
                    NetworkInterface ethInterface = NetworkInterface.getByName("eth0");
                    Enumeration<InetAddress> addresses = ethInterface.getInetAddresses();
                    while(addresses.hasMoreElements()) 
                    {
                        InetAddress addr = addresses.nextElement();
                        
                        if (addr instanceof Inet6Address) continue;
                        
                        ownIP = addr.getHostAddress();
                        System.out.println("ifaces: " + ethInterface.getDisplayName() + " " + ownIP);
                    }
                } 
                catch (SocketException e) 
                {
                    logger.log(Level.SEVERE, "{0}" ,e);
                    throw new RuntimeException(e);
                }
                System.out.println(receivePacket.getAddress().getHostAddress() + " " + ownIP + "===");
                if (receivePacket.getAddress().getHostAddress().equals(ownIP)) {}
                else
                {
                    //Sprawdzenie czy wiadomość jest poprawna
                    String message = new String(receivePacket.getData()).trim();
                    System.out.println("Response: "+receivePacket.getAddress());
                    logger.log(Level.SEVERE, "{0}" ,"Response: "+receivePacket.getAddress());
                    res += message;
                }
                } 
                catch (SocketTimeoutException e)
                {
                    break;
                }
            }
        System.out.println(res);
        logger.log(Level.SEVERE, "{0}" ,res);
        //Zamknięcie portu
        ds.close();
        return res;
        } 
        catch (IOException ex) {}
        return "";
    }
    
    /**
     * Funkcja odsyłająca ramkę z edytowanymi parametrami. 
     * @param ip_num Adres IP urządzenia do edycji parametrów.
     * @param edit_mess Ramka z edytyowanymi parametrami.
     * @param mac_ad MAC adres urządzenia do edycji parametrów. 
     * @return String Zwraca wiadomość zwrotną od serwera o pomyślnej edycji. 
     */
    public String connetct_server(String ip_num, String edit_mess, String mac_ad)
    {
        int port = 8421;
        String msg = "change_conf";
        String code_id = "ZXF2L";
        String flag = "1";
        String finals = "";
          
        try 
        {
            DatagramSocket ds = new DatagramSocket(port);
            ds.setBroadcast(true);
            byte[] sendData = new byte[]{};
            if(edit_mess.equals("restart"))
            {
                sendData = (flag + ";" + code_id + ";" + edit_mess + ";"+"mac="+ mac_ad).getBytes();
            }
            else
            {
                sendData = (flag + ";" + code_id + ";" + msg + ";" + edit_mess).getBytes();
            }
            
            try 
            {
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), port);
            ds.send(sendPacket);
            System.out.println(getClass().getName() + ">>> Send data to: "+ip_num);
            logger.log(Level.SEVERE, "{0}" ,getClass().getName() + ">>> Send data to: "+ip_num);
            } 
            catch (Exception e) 
            {}
            System.out.println(getClass().getName() + ">>> Data was sent. Now waiting for a confirmation!");
            logger.log(Level.SEVERE, "{0}" ,getClass().getName() + ">>> Data was sent. Now waiting for a confirmation!");
            String res = " ";
            try
            {
                if(edit_mess.equals("restart"))
                {
                    System.out.println("Reset ip " + new_ip);
                    logger.log(Level.SEVERE, "{0}" ,"Reset ip " + new_ip);
                }
                else
                {
                    System.out.println("Edycja ip " + new_ip);
                    logger.log(Level.SEVERE, "{0}" ,"Edycja ip " + new_ip);
                }
                
                while(true)
                {
                    byte[] recvBuf = new byte[1024];

                    DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    ds.receive(receivePacket);
                    System.out.println("hostadr: "+receivePacket.getAddress().getHostAddress());
                    logger.log(Level.SEVERE, "{0}" ,"hostadr: "+receivePacket.getAddress().getHostAddress());
                    System.out.println(getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
                    logger.log(Level.SEVERE, "{0}" ,getClass().getName() + ">>> Broadcast response from server: " + receivePacket.getAddress().getHostAddress());
                    String message = new String(receivePacket.getData()).trim();
                    System.out.println("wiadomosc: " + message);
                    logger.log(Level.SEVERE, "{0}" ,"wiadomosc: " + message);
                    if(message.startsWith("0"))
                    {
                        respo = success_tok(message);
                        System.out.println("Odczyt wiadomości: "+receivePacket.getAddress());
                        logger.log(Level.SEVERE, "{0}" ,"Odczyt wiadomości: "+receivePacket.getAddress());
                        res += message;
                        if (respo.equals("SUCCESS"))
                        {
                           break;
                        }
                    }
                    else
                    {
                        System.out.println("Next request...");
                        logger.log(Level.SEVERE, "{0}" ,"Next request...");
                    }
                }
            } 
            catch (SocketTimeoutException e){}  
            ds.close();
            return res;
         } 
         catch (IOException ex) {}
         return "";
    }
    
    /**
     * Pomocnicza funkcja tokenizująca. Jej celem jest wyszukanie parametru 
     * o sukcesie wykonanych zmian lub resetu urządzenia. 
     * @param mess Ciąg znaków przeznaczoncy do tokenizacji. 
     * @return String Zwraca rezultat szukanego parametru. 
     */
    public String success_tok(String mess)
    {
        String res = "";
        String val = "";
        if (mess.equals(" "))
        {
            return "";
        }
        else
        {
            StringTokenizer st = new StringTokenizer(mess,";");
            while (st.hasMoreTokens()) 
            {
                res = st.nextToken();
                if(res.equals(" 0")||(res.equals("0")))
                {
                    res = st.nextToken();
                }
                if(res.equals(" ZXF2L")||(res.equals("ZXF2L")))
                {
                    res = st.nextToken();
                }
                if(res.substring(0,res.indexOf("=")).equals("change_conf") || res.substring(0,res.indexOf("=")).equals("network_restart"))
                {
                    val = res.substring(res.indexOf("=")+1);
                }
                System.out.println("res_t"+res);
            }
            return val;  
        }
    }
    
    public static void main(String[] args) {
        Client_socket cs = new Client_socket();
        System.out.println(cs.run());
    }
    
}
