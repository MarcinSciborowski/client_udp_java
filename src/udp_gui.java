import java.awt.Color;
import java.awt.Component;
import java.text.*;
import java.util.*;
import javax.swing.Timer;
import java.io.*;
import java.net.*;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import pl.bacom.wiznet.WIZnet;
import pl.bacom.atc2000.ATC2000;
import java.util.logging.SimpleFormatter;
import javax.swing.JFileChooser;

/**
 * Program obsługuje obszar kienta. 
 * Ma zadanie wyszukać wszystkie urządzenia (serwery) podłączone do sieci lokalnej. 
 * Tymi urządzeniami są: RPi, BRIX, licznik WIZnet, oraz ATC2000.
 * Po ich wyszukaniu, program wyświetla parametry urządzenia.
 * Pozwala również zmienić parametry na wybrane przez użytkownika. 
 * @author bacom
 */

public class udp_gui extends javax.swing.JFrame{
    //Tworzenie list przechowujących parametry urządzeń.
    //IP RPi/BRIX
    List<String> ip_info = new ArrayList<String>();
    //Tryb adresowania RPi/BRIX
    List<String> addr_info = new ArrayList<String>();
    //Maska sieciowa RPi/BRIX
    List<String> mask_info = new ArrayList<String>();
    //Sieć RPi/BRIX
    List<String> net_info = new ArrayList<String>();
    //Interfejs RPi/BRIX
    List<String> inter_info = new ArrayList<String>();
    //Broadcast RPi/BRIX
    List<String> broad_info = new ArrayList<String>();
    //Gateway RPi/BRIX
    List<String> gate_info = new ArrayList<String>();
    //MAC RPi/BRIX
    List<String> mac_info = new ArrayList<String>();
    //Synchtime RPi/BRIX
    List<String> syn_info = new ArrayList<String>();
    //Serwer adres RPi/BRIX
    List<String> serad_info = new ArrayList<String>();
    //Tryb RPi/BRIX
    List<String> mode_info = new ArrayList<String>();
    //Port RPi/BRIX
    List<String> port_info = new ArrayList<String>();
    //Typ urządzenia (RPi/BRIX)
    List<String> device = new ArrayList<String>();
    //Lista niedozwolonych adresów IP
    List<String> reserv_ip = Arrays.asList("10.0.0.0", "100.64.0.0", 
            "127.0.0.0", "169.254.0.0", "172.16.0.0", "192.0.0.0", "192.0.2.0", 
            "192.88.99.0", "192.168.0.0", "198.18.0.0", "198.51.100.0", 
            "203.0.113.0", "224.0.0.0", "240.0.0.0", "255.255.255.255");
    //Wybrany dns czy nie (dla licznika WIZnet)
    List<String> dnsFlag_info = new ArrayList<String>();
    //Lokalne IP dla liczników (WIZnet/ATC2000)
    List<String> localIpL_info = new ArrayList<String>();
    //Port dla liczników (WIZnet/ATC2000)
    List<String> portL_info = new ArrayList<String>();
    //Subnet dla liczników (WIZnet/ATC2000)
    List<String> subnetL_info = new ArrayList<String>();
    //Gateway dla liczników (WIZnet/ATC2000)
    List<String> gatewayL_info = new ArrayList<String>();
    //Server IP dla liczników (WIZnet/ATC2000)
    List<String> serverIpL_info = new ArrayList<String>();
    //Remoteport dla liczników (WIZnet/ATC2000)
    List<String> remotePortL_info = new ArrayList<String>();
    //Metoda konfiguracji IP dla liczników (WIZnet/ATC2000)
    List<String> conf_metod_info = new ArrayList<String>();
    //Operation mode dla liczników (WIZnet/ATC2000)
    List<String> oper_mode_info = new ArrayList<String>();
    //Speed dla liczników (WIZnet/ATC2000)
    List<String> set_speed_info = new ArrayList<String>();
    //Data bit dla liczników (WIZnet/ATC2000)
    List<String> set_data_bit_info = new ArrayList<String>();
    //Parity dla liczników (WIZnet/ATC2000)
    List<String> set_parity_info = new ArrayList<String>();
    //Flow dla liczników (WIZnet/ATC2000)
    List<String> flowL_info = new ArrayList<String>();
    //IP dns dla licznika WIZnet
    List<String> dnsIpL_info = new ArrayList<String>();
    //Dns name dla licznika WIZnet
    List<String> dnsNameL_info = new ArrayList<String>();
    //WSDL dla BRIXa
    List<String> wsdlB_info = new ArrayList<String>();
    //Location dla BRIXa
    List<String> locationB_info = new ArrayList<String>();
    //Username dla BRIXa
    List<String> usernameB_info = new ArrayList<String>();
    //WSDL password dla BRIXa
    List<String> passB_info = new ArrayList<String>();
    //Nameserver dla BRIXa
    List<String> nameserB_info = new ArrayList<String>();
    //Sprawdzam ping
    List<Boolean> check_ping = new ArrayList<Boolean>();
    //Lista nazw wszystkich list - przydatne do czyszczenia ich zawarości
    ArrayList<List> window_list = new ArrayList<List>(Arrays.asList(addr_info, ip_info, 
            mask_info, net_info, inter_info, broad_info, gate_info, 
            mac_info, syn_info, serad_info, mode_info, port_info, device,
            dnsFlag_info, localIpL_info, portL_info, subnetL_info, gatewayL_info,
            serverIpL_info, remotePortL_info, conf_metod_info, oper_mode_info,
            set_speed_info, set_data_bit_info, set_parity_info, flowL_info,
            dnsIpL_info, dnsNameL_info, wsdlB_info, locationB_info, usernameB_info,
            passB_info, nameserB_info, check_ping));
   
    
    public List<WIZnet> lista_wiz;
    public List<ATC2000> lista_atc;
    //Zmienna podająca indeks urządzenia wybranego urządzenia
    int index = 0;
    //Zmienna do sprawdzania ponownego wyszukiwania
    Boolean find_button = false;
    //Zmienna do przerwania wątku
    Boolean thread_flag = false;
    volatile boolean finished = false;
    Boolean rpi = true;
    //Flaga informujaca czy znaleziono urządzenie.
    //Jeżeli jej wartość wyniesie 3 to oznacza, że nie znaleziono żadnego.
    int find_sth = 0;
    //Zmienne przechowujące ip i mac do odsyłania danych 
    String ipn = "";
    String mac_ad = "";
    //Stworzenie modelu dla listy wyświetlającej dostępne urządzenia
    DefaultListModel DLM = new DefaultListModel();
    //Dodanie grup przycisków
    ButtonGroup group_pi = new ButtonGroup();
    ButtonGroup group_brix = new ButtonGroup();
    ButtonGroup group_l_ip = new ButtonGroup();
    ButtonGroup group_l_oper = new ButtonGroup();
    //Sprawdza czy ATC2000 jest pingowane
    Boolean ping_var;
    // true forces append mode
    
    
    public udp_gui() throws IOException{
        initComponents();
        inter_text.setBackground(Color.GRAY);
        mac_text.setBackground(Color.GRAY);
        interB_text.setBackground(Color.GRAY);
        macB_text.setBackground(Color.GRAY);
        setTitle("SirChair/SZNUB (SZukajka Nowoczesnych Urządzeń Bacom) ver.1.0");
        setResizable(false);
        group_button_rpi();
        group_button_brix();
        group_button_l_ip_mode();
        group_button_l_operation_mode();
        //Ustwienie limitów znaków na podane pola
        ip_text.setDocument(new LimieJTextField(15));
        net_text.setDocument(new LimieJTextField(15));
        netw_text.setDocument(new LimieJTextField(15));
        broad_text.setDocument(new LimieJTextField(15));
        gate_text.setDocument(new LimieJTextField(15));
        serad_text.setDocument(new LimieJTextField(15));
        port_text.setDocument(new LimieJTextField(5));
        ipB_text.setDocument(new LimieJTextField(15));
        netB_text.setDocument(new LimieJTextField(15));
        netwB_text.setDocument(new LimieJTextField(15));
        broadB_text.setDocument(new LimieJTextField(15));
        gateB_text.setDocument(new LimieJTextField(15));
        seradB_text.setDocument(new LimieJTextField(15));
        portB_text.setDocument(new LimieJTextField(5));
        localIpL_text.setDocument(new LimieJTextField(15));
        portL_text.setDocument(new LimieJTextField(5));
        subnetL_text.setDocument(new LimieJTextField(15));
        gatewayL_text.setDocument(new LimieJTextField(15));
        serverIpL_text.setDocument(new LimieJTextField(15));
        remotePortL_text.setDocument(new LimieJTextField(5));
        dnsIpL_text.setDocument(new LimieJTextField(15));
        //Blokowanie aktywności przycisków i przełączników
        edit_button.setEnabled(false);
        rest_button.setEnabled(false);
        div_tab.setEnabledAt(0,false);
        div_tab.setEnabledAt(1,false);
        div_tab.setEnabledAt(2,false);
        parityl_text.setEnabled(false);
        flowl_text.setEnabled(false);
    }

    private static final Logger logger = log();
    
    /**
     * Funkcja przechowuje przyciski RPi.
     */
    public void group_button_rpi()
    {
        group_pi.add(stat_radio);
        group_pi.add(dhcp_radio);  
    }
    
    /**
    * Funkcja przechowuje przyciski BRIX.
    */
    public void group_button_brix()
    {
        group_brix.add(statB_radio);
        group_brix.add(dhcpB_radio);  
    }
    
    /**
     * Funkcja przechowuje przciski licznika WIZnet, służące do wyboru trybu konfiguracji IP.
     */
    public void group_button_l_ip_mode()
    {
        group_l_ip.add(statl_radio);
        group_l_ip.add(dhcpl_radio);
        group_l_ip.add(pppoel_radio);
    }
    
    /**
     * Funkcja przechowuje przyciski licznika WIZnet, służące do wyboru typu pracy
     * (serwer, klient, mix).
     */
    public void group_button_l_operation_mode()
    {
        group_l_oper.add(clientL_radio);
        group_l_oper.add(serverL_radio);
        group_l_oper.add(mixedL_radio);
    }
    
    /**
     * Funkcja przechowuje informacje o blokowaniu i wszarzaniu odpowiednich pól 
     * po wybraniu typu "DHCP" w RPi.
     */
    private void dhcp_action_rpi()
    {
        net_text.setEditable(false);
        ip_text.setEditable(false);
        gate_text.setEditable(false);
        netw_text.setEditable(false);
        broad_text.setEditable(false);
        net_text.setBackground(Color.GRAY);
        ip_text.setBackground(Color.GRAY);
        gate_text.setBackground(Color.GRAY);
        netw_text.setBackground(Color.GRAY);
        broad_text.setBackground(Color.GRAY);
    }
    
    /**
     * Funkcja przechowuje informacje o odblokowaniu odpowiednich pól 
     * po wybraniu typu "Static" w RPi.
     */
    private void static_action_rpi()
    {
        ip_text.setEditable(true);
        net_text.setEditable(true);
        gate_text.setEditable(true);
        netw_text.setEditable(true);
        broad_text.setEditable(true);
        net_text.setBackground(Color.WHITE);
        ip_text.setBackground(Color.WHITE);
        gate_text.setBackground(Color.WHITE);
        netw_text.setBackground(Color.WHITE);
        broad_text.setBackground(Color.WHITE);  
    }
    
    /**
     * Funkcja przechowuje informacje o blokowaniu i wszarzaniu odpowiednich pól 
     * po wybraniu typu "DHCP" w BRIX.
     */
    private void dhcp_action_brix()
    {
        netB_text.setEditable(false);
        ipB_text.setEditable(false);
        gateB_text.setEditable(false);
        netwB_text.setEditable(false);
        broadB_text.setEditable(false);
        netB_text.setBackground(Color.GRAY);
        ipB_text.setBackground(Color.GRAY);
        gateB_text.setBackground(Color.GRAY);
        netwB_text.setBackground(Color.GRAY);
        broadB_text.setBackground(Color.GRAY);
    }
    
    /**
     * Funkcja przechowuje informacje o odblokowaniu odpowiednich pól 
     * po wybraniu typu "Static" w BRIX.
     */
    private void static_action_brix()
    {
        ipB_text.setEditable(true);
        netB_text.setEditable(true);
        gateB_text.setEditable(true);
        netwB_text.setEditable(true);
        broadB_text.setEditable(true);
        netB_text.setBackground(Color.WHITE);
        ipB_text.setBackground(Color.WHITE);
        gateB_text.setBackground(Color.WHITE);
        netwB_text.setBackground(Color.WHITE);
        broadB_text.setBackground(Color.WHITE);  
    }
    
    /**
     * Funkcja przechowuje informacje o blokowaniu i wszarzaniu odpowiednich pól 
     * po wybraniu typu "DHCP" w liczniku WIZnet.
     */
    private void dhcp_action_licznik()
    {
        localIpL_text.setEditable(false);
        subnetL_text.setEditable(false);
        gatewayL_text.setEditable(false);
        localIpL_text.setBackground(Color.GRAY);
        subnetL_text.setBackground(Color.GRAY);
        gatewayL_text.setBackground(Color.GRAY);
    }
    
    /**
     * Funkcja przechowuje informacje o odblokowaniu odpowiednich pól 
     * po wybraniu typu "Static" w liczniku WIZnet.
     */
    private void static_action_licznik()
    {
        localIpL_text.setEditable(true);
        subnetL_text.setEditable(true);
        gatewayL_text.setEditable(true);
        localIpL_text.setBackground(Color.WHITE);
        subnetL_text.setBackground(Color.WHITE);
        gatewayL_text.setBackground(Color.WHITE);
    }
    
    /**
     * Funkcja przechowuje informacje o właściwym zachowaniu pól 
     * po zaznaczeniu checkoboxa "dns" w liczniku WIZnet.
     */
    private void wiznet_action_dns_true()
    {
        serverIpL_text.setEditable(false);
        dnsIpL_text.setEditable(true);
        domainNameL_text.setEditable(true);
        serverIpL_text.setBackground(Color.GRAY);
        dnsIpL_text.setBackground(Color.WHITE);
        domainNameL_text.setBackground(Color.WHITE);
    }
    
    /**
     * Funkcja przechowuje informacje o właściwym zachowaniu pól 
     * po odznaczeniu checkoboxa "dns" w liczniku WIZnet.
     */
    private void wiznet_action_dns_false()
    {
        serverIpL_text.setEditable(true);
        dnsIpL_text.setEditable(false);
        domainNameL_text.setEditable(false);
        serverIpL_text.setBackground(Color.WHITE);
        dnsIpL_text.setBackground(Color.GRAY);
        domainNameL_text.setBackground(Color.GRAY);
    }
    
    /**
     * @return List Zwraca liste z wyszukanymi licznikami WIZnet.
     */
    List<WIZnet> wiznet_list()
    {
        List<WIZnet> lista = new ArrayList<WIZnet>();
        try{
            lista = pl.bacom.wiznet.WIZnet.szukaj(2000, null);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}" ,ex);
        }
        return lista;
    }
    
    /**
     * @return List Zwraca liste z wyszukanymi licznikami ATC2000.
     */
    List<ATC2000> atc_list()
    {
        List<ATC2000> lista = new ArrayList<ATC2000>();
        try {
             lista = pl.bacom.atc2000.ATC2000.szukaj(5000, null);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "{0}" ,ex);
        }
        return lista;
    }
    
    /**
     * Funkcja typu boolean sprawdzająca poprawność wpisanych pól.
     * @param info Pozwala na podanie pola do sprawdzenia.
     * @param what_check Pozwala na wpisanie parametru do sprawdzenia.
     * @return Zwraca true w yniuku porawnego przejścia wszytskich testów - 
     * zwraca false w momencie wyłapania błędnego formatu.
     */
    public boolean ip_checker(String info, String what_check)
    {
        String ip = info;
        String part_ip = "";
        int ip_int;
        int count = 0;
        
        //Sprawdzenie czy IP jest na liście zastrzeżonych adresów
        for (String el : reserv_ip) 
        {
            if(el.equals(ip))
            {
                return false;
            }
        }
        
        try
        {
            StringTokenizer st = new StringTokenizer(ip,".");
            while (st.hasMoreTokens())
            {
                count++;
                part_ip = st.nextToken();
                //Sprawdzenie czy adres nie zaczyna się od "0"
                if((part_ip.charAt(0)) == '0'&&part_ip.length() != 1)
                {
                    return false;
                }
                ip_int = Integer.parseInt(part_ip);
                //Sprawdzenie czy adres IP nie kończy się 255
                if(what_check.equals("ip"))
                {
                    if (ip_int == 255 && count == 4)
                    {
                        return false;
                    }
                }
                //Sprawdzenie czy wartości nie przekraczają 255
                if(ip_int > 255)
                {
                    return false;
                }
            }
            //Sprawdzenie czy adres nie jest zbyt krótki 
            if (count < 4)
            {
                return false;
            }

        }
        catch(NumberFormatException e)
        {
            logger.log(Level.SEVERE, "{0}" ,e);
            return false;
        }
        return true;
    }
    
    /**
     * Funkcja pobiera ramkę z danymi przesyłanymi przez serwer (RPi/BRIX) 
     * i rozdziela odpowiednie elementy do konkretnych list.
     */
    public void find_rpi_brix()
    {
        //Deklarowanie zmiennych
        String mess = "";
        String res = "";
        String title = "";
        
        //Uruchomienie socket-a
        Client_socket cs = new Client_socket();
        mess = cs.run();
        thread_flag = true;
        device_list.setModel(DLM);
        //Jeżeli ramka jest pusta - dodanie inforamcji o braku serwerów
        if (mess.equals(" "))
        {
            find_sth++;
            rpi = false;
            finished = true;
        }
        
        else
        {   
            //Tokenizownanie i dodawanie parametrów do odpowiednich list
            StringTokenizer st = new StringTokenizer(mess,";");
            //Zmienna do zapisu rodzaju urządzenia (RPi/BRIX)
            String device_info = "";
            while (st.hasMoreTokens()) 
            {
                String varia = "";
                String val = "";
                res = st.nextToken();
                if(res.equals(" 0")||(res.equals("0")))
                {
                    res = st.nextToken();
                }
                if(res.equals(" ZXF2L")||(res.equals("ZXF2L")))
                {
                    res = st.nextToken();
                }
                varia = res.substring(0,res.indexOf("="));
                val = res.substring(res.indexOf("=")+1);
                if(varia.equals("device_type"))
                {
                    if(val.equals("raspberry_pi"))
                    {
                        device.add("RPi");
                        device_info = "RPi";
                        title += "RPi - ";
                        wsdlB_info.add("");
                        locationB_info.add("");
                        usernameB_info.add("");
                        passB_info.add("");
                        nameserB_info.add("");
                    }
                    if(val.equals("brix"))
                    {
                        device.add("BRIX");
                        device_info = "Brix";
                        title += "BRIX - ";
                        syn_info.add("");
                    }
                }
                if (device_info.equals("RPi"))
                {
                    if(varia.equals("interface"))
                    {
                        inter_info.add(val);
                    }
                    if(varia.equals("addressing_mode"))
                    {
                        addr_info.add(val);
                    }
                    if(varia.equals("ip_address"))
                    { 
                        ip_info.add(val);
                    }
                    if(varia.equals("netmask"))
                    {
                        mask_info.add(val);
                    }
                    if(varia.equals("network"))
                    {
                        net_info.add(val);
                    }
                    if(varia.equals("broadcast"))
                    { 
                        broad_info.add(val);
                    }
                    if(varia.equals("gateway"))
                    {
                        gate_info.add(val);
                    }
                    if(varia.equals("synch_time"))
                    {
                        syn_info.add(val);
                    }
                    if(varia.equals("server_adress"))
                    {
                        serad_info.add(val);
                    }
                    if(varia.equals("mode"))
                    {
                        mode_info.add(val);
                    }
                    if(varia.equals("server_port"))
                    {
                        port_info.add(val);
                    }  
                    if(varia.equals("mac"))
                    {
                        mac_info.add(val);
                        title += val;
                        DLM.addElement(title);
                        title = "";
                        try 
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) 
                        {
                            logger.log(Level.SEVERE, "{0}" ,e);
                        }
                    }
                }
                
                else if (device_info.equals("Brix"))
                {
                    if(varia.equals("interface"))
                    {
                        inter_info.add(val);
                    }
                    if(varia.equals("addressing_mode"))
                    {
                        addr_info.add(val);
                    }
                    if(varia.equals("ip_address"))
                    { 
                        ip_info.add(val);
                    }
                    if(varia.equals("netmask"))
                    {
                        mask_info.add(val);
                    }
                    if(varia.equals("network"))
                    {
                        net_info.add(val);
                    }
                    if(varia.equals("broadcast"))
                    { 
                        broad_info.add(val);
                    }
                    if(varia.equals("gateway"))
                    {
                        gate_info.add(val);
                    }
                    if(varia.equals("mac"))
                    {
                        mac_info.add(val);
                        title += val;
                        DLM.addElement(title);
                        title = "";
                        try 
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) 
                        {
                            logger.log(Level.SEVERE, "{0}" ,e);
                        }
                    }
                    if(varia.equals("server_adress"))
                    {
                        serad_info.add(val);
                    }
                    if(varia.equals("mode"))
                    {
                        mode_info.add(val);
                    }
                    if(varia.equals("server_port"))
                    {
                        port_info.add(val);
                    }
                    if(varia.equals("wsdl"))
                    {
                        wsdlB_info.add(val);
                    }
                    if(varia.equals("location"))
                    {
                        locationB_info.add(val);
                    }
                    if(varia.equals("wsdl-username"))
                    {
                        usernameB_info.add(val);
                    }
                    if(varia.equals("wsdl-password"))
                    {
                        passB_info.add(val);
                    }
                    if(varia.equals("nameserver"))
                    {
                        nameserB_info.add(val);
                    }
                }
            }
            //Po zakończeniu dodawania parametrów, zmiana wartości flag w celu zakończenia wątku
            if (thread_flag == true)
            {
               finished = true;
               thread_flag = false;
            }
        }
    }
    
    /**
     * Funkcja korzytstając z przygotowanej wcześniej listy urządzeń ATC przypisuje 
     * paramtry tych urządzeń do list.
     */
    public void find_atc() {
        try 
        {
            lista_atc = atc_list(); 
            if (lista_atc.size() > 0)
            {
                device_list.setModel(DLM);
                //Pobieranie liczników z listy
                for (ATC2000 atc: lista_atc)   
                {
                    if(atc.ip.isReachable(1000))
                    {
                        check_ping.add(atc.ip.isReachable(1000));
                        atc.pobUstawienia();
                        DLM.addElement("ATC2000 - " + getMacString(atc.mac));
                        try 
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {}
                        try
                        {
                            serverIpL_info.add(atc.ip_zdalny.getHostAddress());
                        }
                        catch (NullPointerException e) 
                        {
                            serverIpL_info.add(null);
                        }
                        dnsFlag_info.add("");
                        localIpL_info.add(atc.ip.getHostAddress());
                        portL_info.add(Integer.toString(atc.port));
                        subnetL_info.add(atc.maska.getHostAddress());
                        gatewayL_info.add(atc.brama.getHostAddress());
                        remotePortL_info.add("");
                        dnsIpL_info.add("");
                        dnsNameL_info.add("");
                        if (atc.dhcp == true)
                        {
                            conf_metod_info.add("1");
                        }
                        else if (atc.dhcp == false)
                        {
                           conf_metod_info.add("0"); 
                        }
                        oper_mode_info.add(Integer.toString(atc.tryb_pracy));
                        set_speed_info.add(Integer.toString(atc.pretkosc));
                        set_data_bit_info.add(Integer.toString(atc.bity_danych));
                        set_parity_info.add(Integer.toString(atc.parzystosc));
                        try
                        {
                            if(atc.sterowanie.equals("null"))
                            {
                                flowL_info.add("0");
                            }
                        }
                        catch(NullPointerException e)
                                {
                                  flowL_info.add("0"); 
                                }
                    }
                    else
                    {
                        check_ping.add(atc.ip.isReachable(1000));
                        DLM.addElement("ATC2000 - " + getMacString(atc.mac));
                        try 
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {}
                        try
                        {
                            serverIpL_info.add(atc.ip_zdalny.getHostAddress());
                        }
                        catch (NullPointerException e) 
                        {
                            serverIpL_info.add(null);
                        }
                        dnsFlag_info.add("");
                        localIpL_info.add(atc.ip.getHostAddress());
                        portL_info.add("5000");
                        subnetL_info.add(atc.maska.getHostAddress());
                        gatewayL_info.add(atc.brama.getHostAddress());
                        remotePortL_info.add("");
                        dnsIpL_info.add("");
                        dnsNameL_info.add("");
                        if (atc.dhcp == true)
                        {
                            conf_metod_info.add("1");
                        }
                        else if (atc.dhcp == false)
                        {
                           conf_metod_info.add("0"); 
                        }
                        oper_mode_info.add(Integer.toString(atc.tryb_pracy));
                        set_speed_info.add("");
                        set_data_bit_info.add("");
                        set_parity_info.add("");
                        flowL_info.add("");
                    }
                }
            } 
            else 
            {
                find_sth++;
            }
        }catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    /**
     * Funkcja pomocnicza, konwertuje mac do postaci czytelnej (String).
     * @param mac wczytanie parameru mac z postaci bitowej.
     * @return String Zwraca mac w postaci Stringa.
     */
    String getMacString(byte[] mac)
    {
        String macString = "";
        for (int i = 0; i < mac.length; i++) 
        {
            macString += String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
        }
        return macString;
    }
    
    /**
     * Funkcja korzytstając z przygotowanej wcześniej listy urządzeń WIZnet przypisuje 
     * paramtry tych urządzeń do list.
     */
    public void find_wiz()
    {
        try {
                lista_wiz = wiznet_list(); 
                //Pobieranie liczników z listy
                if (lista_wiz.size() > 0)
                {
                    for(int i=0; i<lista_wiz.size(); i++)
                    {
                        //Pobranie ustawień licznika oraz dodanie ich do list
                        WIZnet par = lista_wiz.get(i);
                        check_ping.add(true);
                        device_list.setModel(DLM);
                        DLM.addElement("Wiznet - " + par.dajMAC());
                        try 
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {}
                        dnsFlag_info.add(Integer.toString(par.DFlg));
                        localIpL_info.add(par.ip.getHostAddress());
                        portL_info.add(Integer.toString(par.port));
                        subnetL_info.add(par.maska.getHostAddress());
                        gatewayL_info.add(par.brama.getHostAddress());
                        serverIpL_info.add(par.ip_zdalny.getHostAddress());
                        remotePortL_info.add(Integer.toString(par.port_zdalny));
                        conf_metod_info.add(Integer.toString(par.dhcp));
                        oper_mode_info.add(Integer.toString(par.tryb_pracy));
                        set_speed_info.add(Integer.toString(par.predkosc));
                        set_data_bit_info.add(Integer.toString(par.bity_danych));
                        set_parity_info.add(Integer.toString(par.parzystosc));
                        flowL_info.add(Integer.toString(par.sterowanie));
                        dnsIpL_info.add(par.ip_dns.getHostAddress());
                        dnsNameL_info.add(par.remote_host_domain);   
                    }
                }
                else
                {
                    find_sth++;
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "{0}" ,ex);
            }
    }
    
    /**
     * Funkcja przygotowująca oraz wysyłająca ramkę ze zmienionymi parametrami RPi.
     */
    public void run_edit_rpi()
    {
        String edit_mess = "";
        //Zmienna mająca sprawdzać poprawność formatu danych 
        Boolean flag = true;
        thread_flag = true;
        
        //Sprawdzenie poprawności wprowadzonych danych
        if(!ip_checker(ip_text.getText(),"ip"))
        {
            flag = false;
            ip_text.setBackground(new Color(203,17,38));
            
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny adres IP","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(net_text.getText(),"net"))
        {
            flag = false;
            net_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny netmask","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(netw_text.getText(),"netw"))
        {
            flag = false;
            netw_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny network","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(broad_text.getText(),"broad"))
        {
            flag = false;
            broad_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny broadcast","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(gate_text.getText(),"gate"))
        {
            flag = false;
            gate_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny gateway","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(serad_text.getText(),"serad"))
        {
            flag = false;
            serad_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny serwer adres","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        //Pomyślnie przeprowadzone testy powodują utworzenie ramki z nowymi parametrami
        if(flag)
        {
            JFrame frame = new JFrame("Sukces");
            edit_mess += "interface="+inter_text.getText()+";";
            if(stat_radio.isSelected())
            {
                edit_mess += "addressing_mode=static;";
            }
            if(dhcp_radio.isSelected())
            {
                edit_mess += "addressing_mode=dhcp;";
            }
            edit_mess += "address="+ip_text.getText()+";";
            edit_mess += "netmask="+net_text.getText()+";";
            edit_mess += "network="+netw_text.getText()+";";
            edit_mess += "broadcast="+broad_text.getText()+";";
            edit_mess += "gateway="+gate_text.getText()+";";
            edit_mess += "mac="+mac_text.getText()+";";
            edit_mess += "synch_time="+syn_text.getText()+";";
            edit_mess += "server_adress="+serad_text.getText()+";";
            edit_mess += "mode="+mode_text.getSelectedItem()+";";
            edit_mess += "server_port="+port_text.getText()+";";
            
            //Uworzenie nowego socket-a
            Client_socket cs = new Client_socket();
            cs.new_ip = ipn;
            cs.mac_ad = mac_text.getText();
            //Uruchomienie funkcji przesyłającej ramkę
            cs.connetct_server(ipn,edit_mess,mac_text.getText());
            if (cs.respo.equals("SUCCESS"))
            {
                JOptionPane.showMessageDialog(frame,"Zmiany zostały wysłane!","Sukces",JOptionPane.INFORMATION_MESSAGE);
                cs.respo = "";
            }
            System.out.println("Edycja informacji: "+edit_mess);
            logger.log(Level.SEVERE, "{0}" ,"Edycja informacji: "+edit_mess);
        }
        else
        {
            flag = true;
        }
        System.out.println(ipn);
        //Zmiana flagi i zakończenie wątku
        if (thread_flag == true)
        {
            finished = true;
            thread_flag = false;
        }
    }
    
    /**
     * Funkcja przygotowująca oraz wysyłająca ramkę ze zmienionymi parametrami BRIX.
     */
    public void run_edit_brix()
    {
        String edit_mess = "";
        Boolean flag = true;
        thread_flag = true;
        if(!ip_checker(ipB_text.getText(),"ip"))
        {
            flag = false;
            ipB_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny adres IP","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(netB_text.getText(),"net"))
        {
            flag = false;
            netB_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny netmask","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(netwB_text.getText(),"netw"))
        {
            flag = false;
            netwB_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny network","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(broadB_text.getText(),"broad"))
        {
            flag = false;
            broadB_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny broadcast","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(gateB_text.getText(),"gate"))
        {
            flag = false;
            gateB_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny gateway","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(seradB_text.getText(),"serad"))
        {
            flag = false;
            seradB_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny serwer adres","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(flag)
        {
            JFrame frame = new JFrame("Sukces");
            edit_mess += "interface="+interB_text.getText()+";";
            if(statB_radio.isSelected())
            {
                edit_mess += "addressing_mode=static;";
            }
            if(dhcpB_radio.isSelected())
            {
                edit_mess += "addressing_mode=dhcp;";
            }
            edit_mess += "address="+ipB_text.getText()+";";
            edit_mess += "netmask="+netB_text.getText()+";";
            edit_mess += "network="+netwB_text.getText()+";";
            edit_mess += "broadcast="+broadB_text.getText()+";";
            edit_mess += "gateway="+gateB_text.getText()+";";
            edit_mess += "mac="+macB_text.getText()+";";
            edit_mess += "server_adress="+seradB_text.getText()+";";
            edit_mess += "mode="+modeB_text.getText()+";";
            edit_mess += "server_port="+portB_text.getText()+";";
            edit_mess += "wsdl="+wsdlB_text.getText()+";";
            edit_mess += "location="+locationB_text.getText()+";";
            edit_mess += "wsdl-username="+usernameB_text.getText()+";";
            edit_mess += "wsdl-password="+passB_text.getText()+";";
            edit_mess += "nameserver="+nameserB_text.getText()+";";
            
            Client_socket cs = new Client_socket();
            cs.new_ip = ipn;
            cs.mac_ad = macB_text.getText();
            cs.connetct_server(ipn,edit_mess,macB_text.getText());
            if (cs.respo.equals("SUCCESS"))
            {
                JOptionPane.showMessageDialog(frame,"Zmiany zostały wysłane!","Sukces",JOptionPane.INFORMATION_MESSAGE);
                cs.respo = "";
            }
            System.out.println("Edycja informacji: "+edit_mess);
        }
        else
        {
            flag = true;
        }
        System.out.println(ipn);
        if (thread_flag == true)
        {
            finished = true;
            thread_flag = false;
        }
    }
    
    /**
     * Funkcja po uruchomieniu edytuje parametry licznika WIZnet.
     * @throws UnknownHostException
     * @throws Exception 
     */
    public void run_edit_wiznet() throws UnknownHostException, Exception
    {
        Boolean flag = true;
        thread_flag = true;
        WIZnet par = lista_wiz.get(index);
        
        //Sprawdznie poprawności wprowadzonych danych 
        if(!ip_checker(localIpL_text.getText(),"ip"))
        {
            flag = false;
            localIpL_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny adres IP","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(subnetL_text.getText(),"net"))
        {
            flag = false;
            subnetL_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny netmask","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(gatewayL_text.getText(),"gate"))
        {
            flag = false;
            gatewayL_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny gateway","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(serverIpL_text.getText(),"serad"))
        {
            flag = false;
            serverIpL_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny serwer adres","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        //Sprawdzenie czy jest zaznaczony dns
        if(dns_check.isSelected())
        {
            JFrame frame = new JFrame("Porazka");
            //Jeżeli podano nazwę domeny i ip można zapisać zmiany
            if(!domainNameL_text.getText().isEmpty() && !dnsIpL_text.getText().isEmpty())
            {
                par.DFlg = 1;
                par.ip_dns = InetAddress.getByName(dnsIpL_text.getText());
                par.remote_host_domain = domainNameL_text.getText();
            }
            //W przeciwnym razie komunikat o błędzie
            else
            {
                flag = false;
                JOptionPane.showMessageDialog(frame, "Nie podano ip dns lub nazwy domeny!", "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
            }
        }
        //Jeżeli dns nie jest zaznaczony - korzystaj z ip zdalnego.
        else if (!dns_check.isSelected())
        {
            par.DFlg = 0;
            par.ip_zdalny = InetAddress.getByName(serverIpL_text.getText());
        }
        //Wprowadzanie zmian
        if(flag)
        {
            JFrame frame = new JFrame("Sukces");
            int val_par = 0;
            int val_flo = 0;
            byte val_conf_met = 0;
            byte opr_mod = 0; 

            if(dhcpl_radio.isSelected())
            {
                val_conf_met = 1;
            }
            if(pppoel_radio.isSelected())
            {
                val_conf_met = 2;
            }

            if(serverL_radio.isSelected())
            {
                opr_mod = 2;
            }
            if(mixedL_radio.isSelected())
            {
                opr_mod = 1;
            }
            if (parityl_text.getSelectedItem().toString().equals("Odd"))
            {
                val_par = 1;
            }
            else if (parityl_text.getSelectedItem().toString().equals("Even"))
            {
                val_par = 2;
            }

            if (speedl_text.getSelectedItem().toString().equals("Xon/Xoff"))
            {
                val_par = 1;
            }
            else if (speedl_text.getSelectedItem().toString().equals("CTS/RTS"))
            {
                val_par = 2;
            }

            if(val_conf_met==0)
            {
               par.ustawieniaIP(InetAddress.getByName(localIpL_text.getText()), InetAddress.getByName(subnetL_text.getText()), InetAddress.getByName(gatewayL_text.getText())); 
            }
            if(val_conf_met==1)
            {
                par.ustawianiaIPAuto();
            }
            par.ustawieniaUART(Integer.parseInt(speedl_text.getSelectedItem().toString()), Integer.parseInt(databitl_text.getSelectedItem().toString()), val_par, 1, val_flo);
            par.port = Integer.parseInt(portL_text.getText());
            par.port_zdalny = Integer.parseInt(remotePortL_text.getText());
            par.dhcp = val_conf_met;
            par.tryb_pracy = opr_mod;
            par.ustaw(2000);
            JOptionPane.showMessageDialog(frame,"Dane zostały zmienione!","Sukces",JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            flag = true;
        }
        if (thread_flag == true)
        {
            finished = true;
            thread_flag = false;
        }
    }
    
    /**
     * Funkcja po uruchomieniu edytuje paramerty licznika ATC2000.
     * @throws Exception 
     */
    public void run_edit_atc() throws Exception
    {
        Boolean flag = true;
        thread_flag = true;
        if(!ip_checker(localIpL_text.getText(),"ip"))
        {
            flag = false;
            localIpL_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny adres IP","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(subnetL_text.getText(),"net"))
        {
            flag = false;
            subnetL_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny netmask","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(!ip_checker(gatewayL_text.getText(),"gate"))
        {
            flag = false;
            gatewayL_text.setBackground(new Color(203,17,38));
            JFrame frame = new JFrame("Ostrzeżenie");
            JOptionPane.showMessageDialog(frame,"Niepoprawny gateway","Ostrzeżenie",JOptionPane.WARNING_MESSAGE);
        }
        
        if(flag)
        {
            JFrame frame = new JFrame("Sukces");
            ATC2000 par = lista_atc.get(index);
            int val_par = 0;
            int val_flo = 0;
            byte val_conf_met = 0;
            byte opr_mod = 0; 
            
            if(dhcpl_radio.isSelected())
            {
                val_conf_met = 1;
            }
            if(pppoel_radio.isSelected())
            {
                val_conf_met = 2;
            }

            if(serverL_radio.isSelected())
            {
                opr_mod = 0;
            }
            
            if(clientL_radio.isSelected())
            {
                opr_mod = 1;
            }
            
            if(ping_var == true)
            {
                if (speedl_text.getSelectedItem().toString().equals("Odd"))
                {
                    val_par = 1;
                }
                else if (speedl_text.getSelectedItem().toString().equals("Even"))
                {
                    val_par = 2;
                }

                if (speedl_text.getSelectedItem().toString().equals("Xon/Xoff"))
                {
                    val_par = 1;
                }
                else if (speedl_text.getSelectedItem().toString().equals("CTS/RTS"))
                {
                    val_par = 2;
                }
                if(val_conf_met==1)
                {
                    if (serverIpL_text.getText().equals(""))
                    {
                            par.ustDHCP_HTTP(par.nazwa, opr_mod, Integer.parseInt(portL_text.getText()), null, 2000);
                    }
                    else
                    {
                        par.ustDHCP_HTTP(par.nazwa, opr_mod, Integer.parseInt(portL_text.getText()), InetAddress.getByName(serverIpL_text.getText()), 2000);
                       
                    }
                }
                else if(val_conf_met==0)
                {
                    par.ustawieniaIP(InetAddress.getByName(localIpL_text.getText()), InetAddress.getByName(subnetL_text.getText()), InetAddress.getByName(gatewayL_text.getText()), 2000);
                    Thread.sleep(10000);
                    if (serverIpL_text.getText().equals(""))
                    {
                       par.ustawienia(par.nazwa, Integer.parseInt(speedl_text.getSelectedItem().toString()), val_par, Integer.parseInt(databitl_text.getSelectedItem().toString()), 1, opr_mod, Integer.parseInt(portL_text.getText()), null, 2000);  
                    }
                    else
                    {
                        par.ustawienia(par.nazwa, Integer.parseInt(speedl_text.getSelectedItem().toString()), val_par, Integer.parseInt(databitl_text.getSelectedItem().toString()), 1, opr_mod, Integer.parseInt(portL_text.getText()), InetAddress.getByName(serverIpL_text.getText()), 2000);  
                    }     
                }
            }
            else
            {
                if(val_conf_met==0)
                {
                    par.ustawieniaIP(InetAddress.getByName(localIpL_text.getText()), InetAddress.getByName(subnetL_text.getText()), InetAddress.getByName(gatewayL_text.getText()), 2000);
                }
                Thread.sleep(10000);
            }
            JOptionPane.showMessageDialog(frame,"Dane zostały zmienione!","Sukces",JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            flag = true;
        }
        if (thread_flag == true)
        {
            finished = true;
            thread_flag = false;
        }
    }
    
    /**
     * Funkcja resetująca RPi.
     */
    public void run_reset_rpi()
    {
        thread_flag = true;   
        JFrame frame = new JFrame("Reset");
        //Tworzenie socketa
        Client_socket cs = new Client_socket();
        //Pobranie adresu IP oraz MAC
        cs.new_ip = ip_text.getText();
        cs.mac_ad = mac_text.getText();
        //Uruchomienie funkcji resetującej interfejs RPi
        cs.connetct_server(ipn,"restart",mac_text.getText());
        if (thread_flag == true)
        {
            finished = true;
            thread_flag = false;
        }
        if (cs.respo.equals("SUCCESS"))
        {
            JOptionPane.showMessageDialog(frame,"Urządzenie zostało zresetowane!","Reset",JOptionPane.INFORMATION_MESSAGE);
        }
        
    }
    
    /**
     * Funkcja resetująca BRIX. 
     */
    public void run_reset_brix()
    {
        thread_flag = true;
        JFrame frame = new JFrame("Reset");
        Client_socket cs = new Client_socket();
        cs.new_ip = ipB_text.getText();
        cs.mac_ad = macB_text.getText();
        cs.connetct_server(ipn,"restart",macB_text.getText());
        if (thread_flag == true)
        {
            finished = true;
            thread_flag = false;
        }
        if (cs.respo.equals("SUCCESS"))
        {
            JOptionPane.showMessageDialog(frame,"Urządzenie zostało zresetowane!","Reset",JOptionPane.INFORMATION_MESSAGE);
        }
    }
            
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        inter_text4 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jComboBox6 = new javax.swing.JComboBox<>();
        jFileChooser1 = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        device_list = new javax.swing.JList<>();
        div_tab = new javax.swing.JTabbedPane();
        rpiPanel = new javax.swing.JPanel();
        inter_label = new javax.swing.JLabel();
        inter_text = new javax.swing.JTextField();
        ip_label = new javax.swing.JLabel();
        net_label = new javax.swing.JLabel();
        netw_label = new javax.swing.JLabel();
        netw_text = new javax.swing.JTextField();
        broad_label = new javax.swing.JLabel();
        gate_label = new javax.swing.JLabel();
        gate_text = new javax.swing.JTextField();
        mac_text = new javax.swing.JTextField();
        syn_label = new javax.swing.JLabel();
        syn_text = new javax.swing.JTextField();
        serw_label = new javax.swing.JLabel();
        serad_text = new javax.swing.JTextField();
        mode_label = new javax.swing.JLabel();
        port_label = new javax.swing.JLabel();
        port_text = new javax.swing.JTextField();
        broad_text = new javax.swing.JTextField();
        net_text = new javax.swing.JTextField();
        stat_radio = new javax.swing.JRadioButton();
        dhcp_radio = new javax.swing.JRadioButton();
        mode_text = new javax.swing.JComboBox<>();
        edit_button = new javax.swing.JButton();
        rest_button = new javax.swing.JButton();
        ip_text = new javax.swing.JTextField();
        mac_label = new javax.swing.JLabel();
        brixPanel = new javax.swing.JPanel();
        broadB_label = new javax.swing.JLabel();
        macB_label = new javax.swing.JLabel();
        ipB_label = new javax.swing.JLabel();
        netB_label = new javax.swing.JLabel();
        netB_text = new javax.swing.JTextField();
        netwB_label = new javax.swing.JLabel();
        netwB_text = new javax.swing.JTextField();
        interfaceB_label = new javax.swing.JLabel();
        statB_radio = new javax.swing.JRadioButton();
        dhcpB_radio = new javax.swing.JRadioButton();
        interB_text = new javax.swing.JTextField();
        gateB_label = new javax.swing.JLabel();
        gateB_text = new javax.swing.JTextField();
        macB_text = new javax.swing.JTextField();
        macB_label1 = new javax.swing.JLabel();
        seradB_text = new javax.swing.JTextField();
        modeB_label = new javax.swing.JLabel();
        modeB_text = new javax.swing.JTextField();
        broadB_text = new javax.swing.JTextField();
        portB_label = new javax.swing.JLabel();
        portB_text = new javax.swing.JTextField();
        wsdlB_label = new javax.swing.JLabel();
        wsdlB_text = new javax.swing.JTextField();
        locationB_label = new javax.swing.JLabel();
        locationB_text = new javax.swing.JTextField();
        usernameB_label = new javax.swing.JLabel();
        usernameB_text = new javax.swing.JTextField();
        wsdlpassB_label = new javax.swing.JLabel();
        passB_text = new javax.swing.JTextField();
        nameserverB_label = new javax.swing.JLabel();
        nameserB_text = new javax.swing.JTextField();
        runEditB_button = new javax.swing.JButton();
        resetB_button = new javax.swing.JButton();
        ipB_text = new javax.swing.JTextField();
        licz_tab = new javax.swing.JTabbedPane();
        networkPanel = new javax.swing.JPanel();
        ip_conf_label = new javax.swing.JLabel();
        statl_radio = new javax.swing.JRadioButton();
        dhcpl_radio = new javax.swing.JRadioButton();
        pppoel_radio = new javax.swing.JRadioButton();
        localip_label_licznik = new javax.swing.JLabel();
        localIpL_text = new javax.swing.JTextField();
        port_label_licznik = new javax.swing.JLabel();
        subnet_label_licznik = new javax.swing.JLabel();
        subnetL_text = new javax.swing.JTextField();
        gateway_label_licznik = new javax.swing.JLabel();
        gatewayL_text = new javax.swing.JTextField();
        serverIp_label_liczniik = new javax.swing.JLabel();
        serverIpL_text = new javax.swing.JTextField();
        operation_label_licznik = new javax.swing.JLabel();
        clientL_radio = new javax.swing.JRadioButton();
        serverL_radio = new javax.swing.JRadioButton();
        mixedL_radio = new javax.swing.JRadioButton();
        serverIp_label_liczniik1 = new javax.swing.JLabel();
        remotePortL_text = new javax.swing.JTextField();
        network_button = new javax.swing.JButton();
        dns_check = new javax.swing.JCheckBox();
        dnsIp_label_licznik = new javax.swing.JLabel();
        dnsIpL_text = new javax.swing.JTextField();
        domainName_label_licznik = new javax.swing.JLabel();
        domainNameL_text = new javax.swing.JTextField();
        portL_text = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        serialPanel = new javax.swing.JPanel();
        speed_label_licznik = new javax.swing.JLabel();
        speedl_text = new javax.swing.JComboBox<>();
        data_bit_licznik = new javax.swing.JLabel();
        databitl_text = new javax.swing.JComboBox<>();
        parity_label_licznik = new javax.swing.JLabel();
        parityl_text = new javax.swing.JComboBox<>();
        stopbit_label_licznik = new javax.swing.JLabel();
        stopbitl_text = new javax.swing.JComboBox<>();
        flow_label_licznik = new javax.swing.JLabel();
        flowl_text = new javax.swing.JComboBox<>();
        search_button = new javax.swing.JButton();
        info = new javax.swing.JLabel();
        device_label = new javax.swing.JLabel();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        inter_text4.setEditable(false);

        jLabel12.setText("jLabel12");

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        device_list.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                device_listMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(device_list);

        inter_label.setText("Interface");
        inter_label.setMaximumSize(new java.awt.Dimension(45, 15));
        inter_label.setMinimumSize(new java.awt.Dimension(45, 15));
        inter_label.setPreferredSize(new java.awt.Dimension(45, 15));

        inter_text.setEditable(false);

        ip_label.setText("IP Adres");
        ip_label.setMaximumSize(new java.awt.Dimension(45, 15));
        ip_label.setMinimumSize(new java.awt.Dimension(45, 15));
        ip_label.setPreferredSize(new java.awt.Dimension(45, 15));

        net_label.setText("Netmask");

        netw_label.setText("Network");

        netw_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                netw_textMouseClicked(evt);
            }
        });
        netw_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                netw_textKeyTyped(evt);
            }
        });

        broad_label.setText("Broadcast");

        gate_label.setText("Gateway");

        gate_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gate_textMouseClicked(evt);
            }
        });
        gate_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                gate_textKeyTyped(evt);
            }
        });

        mac_text.setEditable(false);

        syn_label.setText("Synchtime");
        syn_label.setMaximumSize(new java.awt.Dimension(45, 15));
        syn_label.setMinimumSize(new java.awt.Dimension(45, 15));
        syn_label.setPreferredSize(new java.awt.Dimension(45, 15));

        syn_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                syn_textKeyTyped(evt);
            }
        });

        serw_label.setText("Serwer Adres");
        serw_label.setMaximumSize(new java.awt.Dimension(45, 15));
        serw_label.setMinimumSize(new java.awt.Dimension(45, 15));
        serw_label.setPreferredSize(new java.awt.Dimension(45, 15));

        serad_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                serad_textMouseClicked(evt);
            }
        });
        serad_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                serad_textKeyTyped(evt);
            }
        });

        mode_label.setText("Mode");
        mode_label.setMaximumSize(new java.awt.Dimension(45, 15));
        mode_label.setMinimumSize(new java.awt.Dimension(45, 15));
        mode_label.setPreferredSize(new java.awt.Dimension(45, 15));

        port_label.setText("PORT");
        port_label.setMaximumSize(new java.awt.Dimension(45, 15));
        port_label.setMinimumSize(new java.awt.Dimension(45, 15));
        port_label.setPreferredSize(new java.awt.Dimension(45, 15));

        port_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                port_textKeyTyped(evt);
            }
        });

        broad_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                broad_textMouseClicked(evt);
            }
        });
        broad_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                broad_textKeyTyped(evt);
            }
        });

        net_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                net_textMouseClicked(evt);
            }
        });
        net_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                net_textKeyTyped(evt);
            }
        });

        stat_radio.setText("Static");
        stat_radio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stat_radioActionPerformed(evt);
            }
        });

        dhcp_radio.setText("DHCP");
        dhcp_radio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dhcp_radioActionPerformed(evt);
            }
        });

        mode_text.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "radio", "video" }));
        mode_text.setSelectedIndex(-1);

        edit_button.setText("Zapisz konfiguracje");
        edit_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edit_buttonActionPerformed(evt);
            }
        });

        rest_button.setText("Restart");
        rest_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rest_buttonActionPerformed(evt);
            }
        });

        ip_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ip_textMouseClicked(evt);
            }
        });

        mac_label.setText("MAC Adres");

        javax.swing.GroupLayout rpiPanelLayout = new javax.swing.GroupLayout(rpiPanel);
        rpiPanel.setLayout(rpiPanelLayout);
        rpiPanelLayout.setHorizontalGroup(
            rpiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(rpiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rpiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(rpiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(inter_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(rpiPanelLayout.createSequentialGroup()
                            .addComponent(stat_radio)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(dhcp_radio))
                        .addComponent(inter_text)
                        .addComponent(ip_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(net_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(net_text)
                        .addComponent(netw_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(netw_text)
                        .addComponent(broad_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(broad_text)
                        .addComponent(gate_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(gate_text)
                        .addComponent(mac_text)
                        .addComponent(syn_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(syn_text)
                        .addComponent(serw_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(serad_text)
                        .addComponent(mode_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(mode_text, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(port_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(port_text)
                        .addGroup(rpiPanelLayout.createSequentialGroup()
                            .addComponent(edit_button)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(rest_button, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(ip_text))
                    .addComponent(mac_label, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        rpiPanelLayout.setVerticalGroup(
            rpiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, rpiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rpiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stat_radio)
                    .addComponent(dhcp_radio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inter_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inter_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ip_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ip_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(net_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(net_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(netw_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(netw_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(broad_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(broad_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gate_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gate_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mac_label)
                .addGap(7, 7, 7)
                .addComponent(mac_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(syn_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(syn_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serw_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serad_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mode_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mode_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(port_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(port_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(rpiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(edit_button)
                    .addComponent(rest_button))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        edit_button.getAccessibleContext().setAccessibleName("edytuj");

        div_tab.addTab("RPi", rpiPanel);

        broadB_label.setText("Broadcast");
        broadB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        broadB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        broadB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        macB_label.setText("MAC Adres");
        macB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        macB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        macB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        ipB_label.setText("IP Adres");
        ipB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        ipB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        ipB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        netB_label.setText("Netmask");
        netB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        netB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        netB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        netB_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                netB_textMouseClicked(evt);
            }
        });
        netB_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                netB_textKeyTyped(evt);
            }
        });

        netwB_label.setText("Network");
        netwB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        netwB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        netwB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        netwB_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                netwB_textMouseClicked(evt);
            }
        });
        netwB_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                netwB_textKeyTyped(evt);
            }
        });

        interfaceB_label.setText("Interface");

        statB_radio.setText("Static");
        statB_radio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statB_radioActionPerformed(evt);
            }
        });

        dhcpB_radio.setText("DHCP");
        dhcpB_radio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dhcpB_radioActionPerformed(evt);
            }
        });

        interB_text.setEditable(false);

        gateB_label.setText("Gateway");

        gateB_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gateB_textMouseClicked(evt);
            }
        });
        gateB_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                gateB_textKeyTyped(evt);
            }
        });

        macB_text.setEditable(false);

        macB_label1.setText("Serwer Adres");
        macB_label1.setMaximumSize(new java.awt.Dimension(45, 15));
        macB_label1.setMinimumSize(new java.awt.Dimension(45, 15));
        macB_label1.setPreferredSize(new java.awt.Dimension(45, 15));

        seradB_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seradB_textMouseClicked(evt);
            }
        });
        seradB_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                seradB_textKeyTyped(evt);
            }
        });

        modeB_label.setText("Mode");
        modeB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        modeB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        modeB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        broadB_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                broadB_textMouseClicked(evt);
            }
        });
        broadB_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                broadB_textKeyTyped(evt);
            }
        });

        portB_label.setText("PORT");
        portB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        portB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        portB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        portB_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                portB_textKeyTyped(evt);
            }
        });

        wsdlB_label.setText("WSDL");
        wsdlB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        wsdlB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        wsdlB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        locationB_label.setText("Location");
        locationB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        locationB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        locationB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        usernameB_label.setText("WSDL-username");
        usernameB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        usernameB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        usernameB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        wsdlpassB_label.setText("WSDL-password");
        wsdlpassB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        wsdlpassB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        wsdlpassB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        nameserverB_label.setText("Nameserver");
        nameserverB_label.setMaximumSize(new java.awt.Dimension(45, 15));
        nameserverB_label.setMinimumSize(new java.awt.Dimension(45, 15));
        nameserverB_label.setPreferredSize(new java.awt.Dimension(45, 15));

        runEditB_button.setText("Zapisz konfiguracje");
        runEditB_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runEditB_buttonActionPerformed(evt);
            }
        });

        resetB_button.setText("Reset");
        resetB_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetB_buttonActionPerformed(evt);
            }
        });

        ipB_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ipB_textMouseClicked(evt);
            }
        });
        ipB_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ipB_textKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout brixPanelLayout = new javax.swing.GroupLayout(brixPanel);
        brixPanel.setLayout(brixPanelLayout);
        brixPanelLayout.setHorizontalGroup(
            brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(brixPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, brixPanelLayout.createSequentialGroup()
                        .addComponent(modeB_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(104, 104, 104)
                        .addComponent(portB_label, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(brixPanelLayout.createSequentialGroup()
                        .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(wsdlpassB_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(locationB_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(wsdlB_label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, brixPanelLayout.createSequentialGroup()
                                .addComponent(statB_radio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dhcpB_radio))
                            .addComponent(ipB_label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(netB_label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(netB_text, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(netwB_label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(netwB_text, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(interfaceB_label, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(broadB_label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(interB_text, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gateB_label, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gateB_text, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(macB_label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(macB_text, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(macB_label1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(passB_text, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(usernameB_label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameserverB_label, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameserB_text, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(brixPanelLayout.createSequentialGroup()
                        .addComponent(locationB_text, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(usernameB_text, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(brixPanelLayout.createSequentialGroup()
                        .addComponent(modeB_text, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(portB_text, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(brixPanelLayout.createSequentialGroup()
                        .addComponent(runEditB_button, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(resetB_button, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(brixPanelLayout.createSequentialGroup()
                        .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(broadB_text, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(seradB_text, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(wsdlB_text, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ipB_text, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        brixPanelLayout.setVerticalGroup(
            brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(brixPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statB_radio)
                    .addComponent(dhcpB_radio))
                .addGap(3, 3, 3)
                .addComponent(interfaceB_label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(interB_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(ipB_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ipB_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(netB_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(netB_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(netwB_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(netwB_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(broadB_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(broadB_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gateB_label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gateB_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(macB_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(macB_text, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(macB_label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(seradB_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modeB_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portB_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modeB_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portB_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wsdlB_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wsdlB_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationB_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameB_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationB_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameB_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wsdlpassB_label, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameserverB_label, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passB_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameserB_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(brixPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runEditB_button)
                    .addComponent(resetB_button))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        runEditB_button.getAccessibleContext().setAccessibleName("Edytuj");

        div_tab.addTab("BRIX", brixPanel);

        ip_conf_label.setText("IP configuration metod");

        statl_radio.setText("Static");
        statl_radio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statl_radioActionPerformed(evt);
            }
        });

        dhcpl_radio.setText("DHCP");
        dhcpl_radio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dhcpl_radioActionPerformed(evt);
            }
        });

        pppoel_radio.setText("PPPoE");

        localip_label_licznik.setText("Local IP");

        localIpL_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                localIpL_textMouseClicked(evt);
            }
        });
        localIpL_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                localIpL_textKeyTyped(evt);
            }
        });

        port_label_licznik.setText("Port");

        subnet_label_licznik.setText("Subnet");

        subnetL_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                subnetL_textMouseClicked(evt);
            }
        });
        subnetL_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                subnetL_textKeyTyped(evt);
            }
        });

        gateway_label_licznik.setText("Gateway");

        gatewayL_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gatewayL_textMouseClicked(evt);
            }
        });
        gatewayL_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                gatewayL_textKeyTyped(evt);
            }
        });

        serverIp_label_liczniik.setText("Server IP");

        serverIpL_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                serverIpL_textMouseClicked(evt);
            }
        });
        serverIpL_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                serverIpL_textKeyTyped(evt);
            }
        });

        operation_label_licznik.setText("Operation mode");

        clientL_radio.setText("Client");
        clientL_radio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clientL_radioActionPerformed(evt);
            }
        });

        serverL_radio.setText("Server");
        serverL_radio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverL_radioActionPerformed(evt);
            }
        });

        mixedL_radio.setText("Mixed");

        serverIp_label_liczniik1.setText("Remote port");

        remotePortL_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                remotePortL_textKeyTyped(evt);
            }
        });

        network_button.setText("Zapisz ustawienia");
        network_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                network_buttonActionPerformed(evt);
            }
        });

        dns_check.setText("Use DNS");
        dns_check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dns_checkActionPerformed(evt);
            }
        });

        dnsIp_label_licznik.setText("DNS server IP");

        dnsIpL_text.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dnsIpL_textMouseClicked(evt);
            }
        });
        dnsIpL_text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                dnsIpL_textKeyTyped(evt);
            }
        });

        domainName_label_licznik.setText("Domain name");

        javax.swing.GroupLayout networkPanelLayout = new javax.swing.GroupLayout(networkPanel);
        networkPanel.setLayout(networkPanelLayout);
        networkPanelLayout.setHorizontalGroup(
            networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(networkPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(networkPanelLayout.createSequentialGroup()
                        .addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(subnet_label_licznik)
                            .addComponent(subnetL_text, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gateway_label_licznik)
                            .addComponent(gatewayL_text, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(serverIp_label_liczniik)
                            .addComponent(serverIpL_text, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(operation_label_licznik)
                            .addComponent(serverIp_label_liczniik1)
                            .addComponent(remotePortL_text, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(networkPanelLayout.createSequentialGroup()
                                .addComponent(clientL_radio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(serverL_radio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(mixedL_radio))
                            .addComponent(network_button)
                            .addComponent(dns_check)
                            .addComponent(dnsIp_label_licznik)
                            .addComponent(dnsIpL_text, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(domainName_label_licznik)
                            .addComponent(domainNameL_text, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(networkPanelLayout.createSequentialGroup()
                        .addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(portL_text, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ip_conf_label, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, networkPanelLayout.createSequentialGroup()
                                .addComponent(statl_radio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dhcpl_radio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pppoel_radio))
                            .addComponent(localip_label_licznik, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(localIpL_text, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(port_label_licznik, javax.swing.GroupLayout.Alignment.LEADING))
                        .addContainerGap(20, Short.MAX_VALUE))))
            .addComponent(jSeparator1)
        );
        networkPanelLayout.setVerticalGroup(
            networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(networkPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ip_conf_label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statl_radio)
                    .addComponent(dhcpl_radio)
                    .addComponent(pppoel_radio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(localip_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localIpL_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(port_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(portL_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subnet_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(subnetL_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gateway_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gatewayL_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serverIp_label_liczniik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serverIpL_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serverIp_label_liczniik1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remotePortL_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(operation_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(networkPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clientL_radio)
                    .addComponent(serverL_radio)
                    .addComponent(mixedL_radio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dns_check)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dnsIp_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dnsIpL_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(domainName_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(domainNameL_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(network_button)
                .addContainerGap(66, Short.MAX_VALUE))
        );

        licz_tab.addTab("Network", networkPanel);

        speed_label_licznik.setText("Speed");

        speedl_text.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1200\t", "2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400" }));
        speedl_text.setSelectedIndex(-1);

        data_bit_licznik.setText("Data Bit");

        databitl_text.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "7", "8" }));
        databitl_text.setSelectedIndex(-1);

        parity_label_licznik.setText("Parity");

        parityl_text.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Odd", "Even" }));
        parityl_text.setSelectedIndex(-1);

        stopbit_label_licznik.setText("Stop Bit");

        stopbitl_text.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1" }));

        flow_label_licznik.setText("Flow");

        flowl_text.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Xon/Xoff", "CTS/RTS" }));
        flowl_text.setSelectedIndex(-1);

        javax.swing.GroupLayout serialPanelLayout = new javax.swing.GroupLayout(serialPanel);
        serialPanel.setLayout(serialPanelLayout);
        serialPanelLayout.setHorizontalGroup(
            serialPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serialPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(serialPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(speed_label_licznik)
                    .addComponent(speedl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(data_bit_licznik)
                    .addComponent(databitl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(parity_label_licznik)
                    .addComponent(parityl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopbit_label_licznik)
                    .addComponent(stopbitl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(flow_label_licznik)
                    .addComponent(flowl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(203, Short.MAX_VALUE))
        );
        serialPanelLayout.setVerticalGroup(
            serialPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(serialPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(speed_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(speedl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(data_bit_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(databitl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(parity_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parityl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(stopbit_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopbitl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(flow_label_licznik)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flowl_text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(328, Short.MAX_VALUE))
        );

        licz_tab.addTab("Serial", serialPanel);

        div_tab.addTab("Licznik", licz_tab);

        search_button.setText("Szukaj");
        search_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                search_buttonActionPerformed(evt);
            }
        });

        info.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N

        device_label.setText("Lista urządzeń");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(device_label)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(search_button, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(info, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(div_tab, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(device_label)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1))
                    .addComponent(div_tab))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(search_button)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(info, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Przycisk wyzwalający przeszukanie wszystkich urządzeń podłączonych do sieci lokanej.
     * @param evt 
     */
    private void search_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_search_buttonActionPerformed
        edit_button.setEnabled(false);
        if(find_button == false)
        {     
            find_button = true;
            Thread thread1 = new Thread(){
            public void run()
            {
              while (!finished)
              {
                info.setText("Skanuje...");
                //funkcja przeszukujaca WIZnet
                find_wiz();
                //funkcja przeszukujaca ATC2000
                find_atc();
                //funkcja przeszukujaca RPi
                find_rpi_brix();
                if(find_sth==3)
                {
                    device_list.setModel(DLM);
                    DLM.addElement("Nie znaleziono serwerów");
                }
                find_sth=0;
                info.setText("Zakończono!"); 
              }
            }
            };
            thread1.start(); 
        }
        else
        {
            finished = false;
            Thread thread1 = new Thread(){
            public void run()
            {
                while (!finished)
                {
                    //Zmiana flagi informujacej o ponownym wyszukiwaniu
                    rpi = true;
                    info.setText("Skanuje...");
                    int ile = device_list.getModel().getSize();
                    
                    //Czyszczenie listy wyświetlającej dostępne urządzenia
                    for(int i=ile-1; i>=0; i--)
                    {
                        DLM.removeElementAt(i);
                    }
                    
                    //Czyszczenie grupy przycisków
                    group_pi.clearSelection();
                    group_brix.clearSelection();
                    group_l_ip.clearSelection();
                    group_l_oper.clearSelection();
                    
                    //Czyszczenie list z parametrami
                    for(int i = 0; i<window_list.size();i++)
                    {
                        window_list.get(i).clear();
                    }
                    
                    //Czyszczenie pól tekstowych w panelu RPi
                    for(Component control : rpiPanel.getComponents())
                    {
                        if(control instanceof JTextField)
                        {
                            JTextField ctrl = (JTextField) control;
                            ctrl.setText("");
                        }
                    }
                    
                    //Czyszczenie pól tekstowych w panelu BRIX
                    for(Component control : brixPanel.getComponents())
                    {
                        if(control instanceof JTextField)
                        {
                            JTextField ctrl = (JTextField) control;
                            ctrl.setText("");
                        }
                    }
                    
                    //Czyszczenie pól tekstowych w panelu licznik
                    for(Component control : networkPanel.getComponents())
                    {
                        if(control instanceof JTextField)
                        {
                            JTextField ctrl = (JTextField) control;
                            ctrl.setText("");
                        }
                    }
                    
                    mode_text.setSelectedIndex(-1);
                    speedl_text.setSelectedIndex(-1);
                    databitl_text.setSelectedIndex(-1);
                    parityl_text.setSelectedIndex(-1);
                    flowl_text.setSelectedIndex(-1);
                    //funkcja przeszukujaca WIZnet
                    find_wiz();
                    //funkcja przeszukujaca ATC2000
                    find_atc();
                    //funkcja przeszukujaca RPi
                    find_rpi_brix();
                    if(find_sth==3)
                    {
                        device_list.setModel(DLM);
                        DLM.addElement("Nie znaleziono serwerów");
                    }
                    find_sth=0;
                    info.setText("Zakończono!");  
                }
            }
            };
            thread1.start(); 
        }
    }//GEN-LAST:event_search_buttonActionPerformed

    /**
     * Przycisk wyzwalający edycję RPi.
     * @param evt 
     */
    private void edit_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edit_buttonActionPerformed
        finished = false;
        Thread thread1 = new Thread(){
            public void run()
            {
                while (!finished)
                {
                    info.setText("Edytuje...");
                    run_edit_rpi();
                    info.setText("Zakończono!"); 
                }
            }
        };
        thread1.start();
    }//GEN-LAST:event_edit_buttonActionPerformed

    /**
     * Wybór jednego urządzenia z listy wyświetlającej się w programie pozwala na wypełnienie 
     * pól odpowiednimi parametrami. 
     * @param evt 
     */
    private void device_listMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_device_listMouseClicked
        String info = (String)(device_list.getSelectedValue());
        if(!info.equals("Nie znaleziono serwerów"))
        {
            edit_button.setEnabled(true);
            rest_button.setEnabled(true);
        }
        if(!mask_info.isEmpty()||!localIpL_info.isEmpty())
        {
            //Wypełnienie odpowniednich pól z parametrami dla urządzenia typu Wiznet lub ATC2000
            if(device_list.getSelectedValue().startsWith("Wiznet")||device_list.getSelectedValue().startsWith("ATC2000"))
            {
                //Wyświetlenie zakładki z danymi WIZnet/ATC2000
                div_tab.setSelectedIndex(2);
                //Pobranie indeksu urządzenia
                int i = device_list.getSelectedIndex();
                //Sprawdzanie czy urządzenie jest pignowane
                if(check_ping.get(i).equals(true))
                {
                    //Odblokowanie odpowiednich pól
                    ping_var = true;
                    licz_tab.setEnabledAt(1,true);
                    dhcpl_radio.setEnabled(true);
                    pppoel_radio.setEnabled(true);
                    portL_text.setEnabled(true);
                    portL_text.setBackground(Color.WHITE);
                    //Ustawienie parametru ip configuration metod
                    if(conf_metod_info.get(i).equals("0"))
                    {
                        statl_radio.setSelected(true);
                        licz_tab.setEnabledAt(1,true);
                        static_action_licznik();
                    }
                    else if(conf_metod_info.get(i).equals("1"))
                    {
                        dhcpl_radio.setSelected(true);
                        licz_tab.setEnabledAt(1,false);
                        dhcp_action_licznik();
                    }
                    else if(conf_metod_info.get(i).equals("2"))
                    {
                        pppoel_radio.setSelected(true);
                    }
                }
                if(check_ping.get(i).equals(false))
                {
                    licz_tab.setSelectedIndex(0);
                    ping_var = false;
                    licz_tab.setEnabledAt(1, false);
                    dhcpl_radio.setEnabled(false);
                    pppoel_radio.setEnabled(false);
                    portL_text.setEnabled(false);  
                }
                //Wypełnianie kolejnych pól
                localIpL_text.setText(localIpL_info.get(i));
                portL_text.setText(portL_info.get(i));
                subnetL_text.setText(subnetL_info.get(i));
                gatewayL_text.setText(gatewayL_info.get(i));
                serverIpL_text.setText(serverIpL_info.get(i));
                remotePortL_text.setText(remotePortL_info.get(i));
                dnsIpL_text.setText(dnsIpL_info.get(i));
                domainNameL_text.setText(dnsNameL_info.get(i));
                //Uruchomienie odpowiednich akcji w zależności od urządzenia
                if(device_list.getSelectedValue().startsWith("ATC2000"))
                {
                    index = i-lista_wiz.size();
                    dns_check.setEnabled(false);
                    dnsIpL_text.setEditable(false);
                    domainNameL_text.setEditable(false);
                    dnsIpL_text.setBackground(Color.GRAY);
                    domainNameL_text.setBackground(Color.GRAY);
                    if(oper_mode_info.get(i).equals("1"))
                    {
                        clientL_radio.setSelected(true);
                        mixedL_radio.setEnabled(false);
                        serverIpL_text.setEditable(true);
                        serverIpL_text.setBackground(Color.WHITE);
                    }
                    else if(oper_mode_info.get(i).equals("0"))
                    {
                        serverL_radio.setSelected(true);
                        mixedL_radio.setEnabled(false);
                        serverIpL_text.setEditable(false);
                        serverIpL_text.setBackground(Color.GRAY);
                    }
                    else
                    {
                        clientL_radio.setSelected(false);
                        serverL_radio.setSelected(false);
                        mixedL_radio.setEnabled(false);
                        serverIpL_text.setEditable(false);
                        serverIpL_text.setBackground(Color.GRAY);
                    }
                }
                if(device_list.getSelectedValue().startsWith("Wiznet"))
                {
                    index = i;
                    dns_check.setEnabled(true);
                    dnsIpL_text.setEditable(true);
                    domainNameL_text.setEditable(true);
                    dnsIpL_text.setBackground(Color.WHITE);
                    domainNameL_text.setBackground(Color.WHITE);
                    //Ustawienie parametru operation mode
                    if(oper_mode_info.get(i).equals("0"))
                    {
                        clientL_radio.setSelected(true);
                        mixedL_radio.setEnabled(true);
                        //Sprawdzenie czy dns jest zaznaczony
                        if(dnsFlag_info.get(i).equals("0"))
                        {
                            dns_check.setSelected(false);
                            serverIpL_text.setEditable(true);
                            serverIpL_text.setBackground(Color.WHITE);
                            dnsIpL_text.setEditable(false);
                            dnsIpL_text.setBackground(Color.GRAY);
                            domainNameL_text.setEditable(false);
                            domainNameL_text.setBackground(Color.GRAY);
                        }
                        else if (dnsFlag_info.get(i).equals("1"))
                        {
                            dns_check.setSelected(true);
                            serverIpL_text.setEditable(false);
                            serverIpL_text.setBackground(Color.GRAY);
                            dnsIpL_text.setEditable(true);
                            dnsIpL_text.setBackground(Color.WHITE);
                            domainNameL_text.setEditable(true);
                            domainNameL_text.setBackground(Color.WHITE);
                        } 
                    }
                    else if(oper_mode_info.get(i).equals("1"))
                    {
                        mixedL_radio.setSelected(true);
                        mixedL_radio.setEnabled(true);
                        if(dnsFlag_info.get(i).equals("0"))
                        {
                            dns_check.setSelected(false);
                            wiznet_action_dns_false();
                        }
                        else if (dnsFlag_info.get(i).equals("1"))
                        {
                            dns_check.setSelected(true);
                            wiznet_action_dns_true();
                        } 
                    }
                    else if(oper_mode_info.get(i).equals("2"))
                    {
                        serverL_radio.setSelected(true);
                        mixedL_radio.setEnabled(true);
                        
                        if(dnsFlag_info.get(i).equals("0"))
                        {
                            dns_check.setSelected(false);
                            serverIpL_text.setEditable(false);
                            serverIpL_text.setBackground(Color.GRAY);
                            dnsIpL_text.setEditable(false);
                            dnsIpL_text.setBackground(Color.GRAY);
                            domainNameL_text.setEditable(false);
                            domainNameL_text.setBackground(Color.GRAY);
                        }
                        else if (dnsFlag_info.get(i).equals("1"))
                        {
                            dns_check.setSelected(true);
                            serverIpL_text.setEditable(false);
                            serverIpL_text.setBackground(Color.GRAY);
                            dnsIpL_text.setEditable(true);
                            dnsIpL_text.setBackground(Color.WHITE);
                            domainNameL_text.setEditable(true);
                            domainNameL_text.setBackground(Color.WHITE);
                        }  
                    }
                }
                
                //Ustawianie prametru speed
                for(int j=0; j<9; j++)
                {
                    if(speedl_text.getItemAt(j).equals(set_speed_info.get(i)))
                    {
                        speedl_text.setSelectedIndex(j);
                    }
                }
                //Ustawienie prametru set data bit
                for (int j=0; j<2; j++)
                {
                    if(databitl_text.getItemAt(j).equals(set_data_bit_info.get(i)))
                    {
                        databitl_text.setSelectedIndex(j);
                    }
                }
                //Ustawienie parametru set parity
                if(set_parity_info.get(i).equals("0"))
                {
                    parityl_text.setSelectedIndex(0);
                }
                else if(set_parity_info.get(i).equals("1"))
                {
                    parityl_text.setSelectedIndex(1);
                }
                else if(set_parity_info.get(i).equals("2"))
                {
                    parityl_text.setSelectedIndex(2);
                } 
                //Ustawienie parametru set flow
                if(flowL_info.get(i).equals("0"))
                {
                    flowl_text.setSelectedIndex(0);
                }
                else if(flowL_info.get(i).equals("1"))
                {
                    flowl_text.setSelectedIndex(1);
                }
                else if(flowL_info.get(i).equals("2"))
                {
                    flowl_text.setSelectedIndex(2);
                } 
            }
            //Ustawienie prametrów dla urządzenia RPi lub Brix
            else if(rpi)
            {
                int index = device_list.getSelectedIndex() - (lista_wiz.size()+lista_atc.size());
                if(((String)device.get(index)).equals("BRIX"))
                {
                    int n = div_tab.indexOfTab("BRIX");
                    div_tab.setSelectedIndex(n);
                    if (((String)addr_info.get(index)).equals("static"))
                    {
                        statB_radio.setSelected(true);
                        static_action_brix();
                    }
                    if (((String)addr_info.get(index)).equals("dhcp"))
                    {
                        dhcpB_radio.setSelected(true);
                        dhcp_action_brix();
                    }
                    interB_text.setText((String)inter_info.get(index));
                    ipB_text.setText((String)ip_info.get(index));
                    netB_text.setText((String)mask_info.get(index));
                    netwB_text.setText((String)net_info.get(index));
                    broadB_text.setText((String)broad_info.get(index));
                    gateB_text.setText((String)gate_info.get(index));
                    macB_text.setText((String)mac_info.get(index));
                    seradB_text.setText((String)serad_info.get(index));
                    modeB_text.setText((String)mode_info.get(index));
                    portB_text.setText((String)port_info.get(index));
                    wsdlB_text.setText((String)wsdlB_info.get(index));
                    locationB_text.setText((String)locationB_info.get(index));
                    usernameB_text.setText((String)usernameB_info.get(index));
                    passB_text.setText((String)passB_info.get(index));
                    nameserB_text.setText((String)nameserB_info.get(index));
                    ipn=ipB_text.getText();
                }
                if(((String)device.get(index)).equals("RPi"))
                {
                    int n = div_tab.indexOfTab("RPi");
                    div_tab.setSelectedIndex(n);
                    if (((String)addr_info.get(index)).equals("static"))
                    {
                        stat_radio.setSelected(true);
                        static_action_rpi();
                    }
                    if (((String)addr_info.get(index)).equals("dhcp"))
                    {
                        dhcp_radio.setSelected(true);
                        dhcp_action_rpi();
                    }
                    inter_text.setText((String)inter_info.get(index));
                    ip_text.setText((String)ip_info.get(index));
                    net_text.setText((String)mask_info.get(index));
                    netw_text.setText((String)net_info.get(index));
                    broad_text.setText((String)broad_info.get(index));
                    gate_text.setText((String)gate_info.get(index));
                    mac_text.setText((String)mac_info.get(index));
                    syn_text.setText((String)syn_info.get(index));
                    serad_text.setText((String)serad_info.get(index));
                    if (mode_info.get(index).equals("radio"))
                    {
                        mode_text.setSelectedIndex(0);
                    }
                    if (mode_info.get(index).equals("video"))
                    {
                        mode_text.setSelectedIndex(1);
                    }
                    port_text.setText((String)port_info.get(index));
                    ipn=ip_text.getText();
                }
            }
        }
    }//GEN-LAST:event_device_listMouseClicked

    private void dhcp_radioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dhcp_radioActionPerformed
        if(dhcp_radio.isSelected())
        {
            dhcp_action_rpi();
        }
    }//GEN-LAST:event_dhcp_radioActionPerformed

    private void stat_radioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stat_radioActionPerformed
        if(stat_radio.isSelected())
        {
            static_action_rpi();
        }
    }//GEN-LAST:event_stat_radioActionPerformed

    /**
     * Funkcja uniemożliwia wypełnienie pól innymi znakami niż cyfry i "."
     * @param evt 
     */
    public void ban_char(java.awt.event.KeyEvent evt)
    {
        char c = evt.getKeyChar();
        if(!(Character.isDigit(c) || c=='.'))
        {
            evt.consume();
        }
    }
    
    /**
     * Funkcja uniemożliwia wypełnienie pól innymi znakami niż cyfry.
     * @param evt 
     */
    public void ban_char2(java.awt.event.KeyEvent evt)
    {
        char c = evt.getKeyChar();
        if(!(Character.isDigit(c)))
        {
            evt.consume();
        }
    }
    
    private void net_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_net_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_net_textKeyTyped

    private void netw_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_netw_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_netw_textKeyTyped

    private void broad_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_broad_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_broad_textKeyTyped

    private void gate_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gate_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_gate_textKeyTyped

    private void serad_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_serad_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_serad_textKeyTyped

    private void syn_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_syn_textKeyTyped
        ban_char2(evt);
    }//GEN-LAST:event_syn_textKeyTyped

    private void port_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_port_textKeyTyped
        ban_char2(evt);
    }//GEN-LAST:event_port_textKeyTyped

    /**
     * Funkcja wykonuje odpowiednie operacej po kliknięciu na pole.
     * @param evt 
     */
    private void net_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_net_textMouseClicked
        if(stat_radio.isSelected())
        {
            net_text.setBackground(Color.WHITE);
        }
        if(!ip_checker(net_text.getText(),"net"))
        {
            net_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_net_textMouseClicked

    /**
     * Funkcja wykonuje odpowiednie operacej po kliknięciu na pole.
     * @param evt 
     */
    private void netw_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_netw_textMouseClicked
        if(stat_radio.isSelected())
        {
            netw_text.setBackground(Color.WHITE);
        }
        if(!ip_checker(netw_text.getText(),"netw"))
        {
            netw_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_netw_textMouseClicked

    /**
     * Funkcja wykonuje odpowiednie operacej po kliknięciu na pole.
     * @param evt 
     */
    private void broad_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_broad_textMouseClicked
        if(stat_radio.isSelected())
        {
            broad_text.setBackground(Color.WHITE);
        }
        if(!ip_checker(broad_text.getText(),"broad"))
        {
            broad_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_broad_textMouseClicked

    /**
     * Funkcja wykonuje odpowiednie operacej po kliknięciu na pole.
     * @param evt 
     */
    private void gate_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gate_textMouseClicked
        if(stat_radio.isSelected())
        {
            gate_text.setBackground(Color.WHITE);
        }
        if(!ip_checker(gate_text.getText(),"gate"))
        {
            gate_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_gate_textMouseClicked

    /**
     * Funkcja wykonuje odpowiednie operacej po kliknięciu na pole.
     * @param evt 
     */
    private void serad_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_serad_textMouseClicked
        if(stat_radio.isSelected())
        {
            serad_text.setBackground(Color.WHITE);
        }
        if(!ip_checker(serad_text.getText(),"serad"))
        {
            serad_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_serad_textMouseClicked

    /**
     * Przycisk wyzwalający reset RPi.
     * @param evt 
     */
    private void rest_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rest_buttonActionPerformed
        finished = false;
        Thread thread1 = new Thread(){
            public void run()
            {
                while (!finished)
                {
                    info.setText("Resetuje...");
                    run_reset_rpi();
                    info.setText("Zakończono!"); 
                }
            }
        };
        thread1.start();
    }//GEN-LAST:event_rest_buttonActionPerformed

    /**
     * Przycisk wyzwalający zapisanie zmienionych parametrów. 
     * @param evt 
     */
    private void network_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_network_buttonActionPerformed
        finished = false;
        Thread thread1 = new Thread(){
            public void run()
            {
                while (!finished)
                {
                    info.setText("Edytuje...");
                    try {
                        if(device_list.getSelectedValue().startsWith("Wiznet"))
                        {
                            run_edit_wiznet();
                        }
                        else if(device_list.getSelectedValue().startsWith("ATC2000"))
                        {
                            run_edit_atc();
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, "{0}" ,ex);
                    }
                    info.setText("Zakończono!"); 
                }
            }
        };
        thread1.start();
    }//GEN-LAST:event_network_buttonActionPerformed

    /**
     * Funkcja wyzwalająca akcję po zaznaczeniu przycisku "Static" przy liczniku WIZnet.
     * @param evt 
     */
    private void statl_radioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statl_radioActionPerformed
        if(statl_radio.isSelected())
        {
            static_action_licznik();
            if (device_list.getSelectedValue().startsWith("ATC2000"))
            {
                licz_tab.setEnabledAt(1, true);
            }
        }
    }//GEN-LAST:event_statl_radioActionPerformed

    /**
     * Funkcja wyzwalająca akcję po zaznaczeniu przycisku "DHCPL" przy liczniku WIZnet.
     * @param evt 
     */
    private void dhcpl_radioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dhcpl_radioActionPerformed
        if(dhcpl_radio.isSelected())
        {
            dhcp_action_licznik();
            if (device_list.getSelectedValue().startsWith("ATC2000"))
            {
                licz_tab.setEnabledAt(1, false);
            }
        }
    }//GEN-LAST:event_dhcpl_radioActionPerformed
    
    /**
     * Funkcja wyzwalająca akcję po zaznaczeniu przycisku checkbox-a "dns" przy liczniku WIZnet.
     * @param evt 
     */
    private void dns_checkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dns_checkActionPerformed
        if (dns_check.isSelected())
        {
            //Akcja po zaznaczeniu dns-a oraz klienta
            if(clientL_radio.isSelected())
            {
               serverIpL_text.setEditable(false);
               serverIpL_text.setBackground(Color.GRAY);
               dnsIpL_text.setEditable(true);
               dnsIpL_text.setBackground(Color.WHITE);
               domainNameL_text.setEditable(true);
               domainNameL_text.setBackground(Color.WHITE);
            }
            //Akcja po zaznaczeniu dns-a oraz servera
            if(serverL_radio.isSelected())
            {
               serverIpL_text.setEditable(false);
               serverIpL_text.setBackground(Color.GRAY);
               dnsIpL_text.setEditable(true);
               dnsIpL_text.setBackground(Color.WHITE);
               domainNameL_text.setEditable(true);
               domainNameL_text.setBackground(Color.WHITE);
            }
        }
        else if (!dns_check.isSelected())
        {
            //Akcja po odnzaczeniu dns-a oraz zaznaczeniu klienata
            if(clientL_radio.isSelected())
            {
               serverIpL_text.setEditable(true);
               serverIpL_text.setBackground(Color.WHITE);
               dnsIpL_text.setEditable(false);
               dnsIpL_text.setBackground(Color.GRAY);
               domainNameL_text.setEditable(false);
               domainNameL_text.setBackground(Color.GRAY);
            }
            //Akcja po odznaczeniu dns-a oraz zaznaczeniu servera
            if(serverL_radio.isSelected())
            {
               serverIpL_text.setEditable(false);
               dnsIpL_text.setEditable(false);
               dnsIpL_text.setBackground(Color.GRAY);
               domainNameL_text.setEditable(false);
               domainNameL_text.setBackground(Color.GRAY);
            }
        }
    }//GEN-LAST:event_dns_checkActionPerformed

    /**
     * Funkcja wywalająca akcję po wybraniu przcisku "Static" w zakładce BRIX.
     * @param evt 
     */
    private void statB_radioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statB_radioActionPerformed
        if(statB_radio.isSelected())
        {
            static_action_brix();
        }
    }//GEN-LAST:event_statB_radioActionPerformed

    /**
     * Funkcja wywalająca akcję po wybraniu przcisku "DHCP" w zakładce BRIX.
     * @param evt 
     */
    private void dhcpB_radioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dhcpB_radioActionPerformed
        if(dhcpB_radio.isSelected())
        {
            dhcp_action_brix();
        }
    }//GEN-LAST:event_dhcpB_radioActionPerformed

    /**
     * Przycisk wyzwalający edycję parametrów BRIX.
     * @param evt 
     */
    private void runEditB_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runEditB_buttonActionPerformed
        finished = false;
        Thread thread1 = new Thread(){
            public void run()
            {
                while (!finished)
                {
                    info.setText("Edytuje...");
                    run_edit_brix();
                    info.setText("Zakończono!"); 
                }
            }
        };
        thread1.start();
    }//GEN-LAST:event_runEditB_buttonActionPerformed

    /**
     * Przycisk wyzwalający reset BRIX.
     * @param evt 
     */
    private void resetB_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetB_buttonActionPerformed
        finished = false;
        Thread thread1 = new Thread(){
            public void run()
            {
                while (!finished)
                {
                    info.setText("Resetuje...");
                    run_reset_brix();
                    info.setText("Zakończono!"); 
                }
            }
        };
        thread1.start();
    }//GEN-LAST:event_resetB_buttonActionPerformed

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola ipB_text
     * @param evt 
     */
    private void ipB_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ipB_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_ipB_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola netB_text
     * @param evt 
     */
    private void netB_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_netB_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_netB_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola netwB_text
     * @param evt 
     */
    private void netwB_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_netwB_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_netwB_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola broadB_text
     * @param evt 
     */
    private void broadB_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_broadB_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_broadB_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola gateB_text 
     * @param evt 
     */
    private void gateB_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gateB_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_gateB_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola seradB_text
     * @param evt 
     */
    private void seradB_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_seradB_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_seradB_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr dla pola portB_text
     * @param evt 
     */
    private void portB_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_portB_textKeyTyped
        ban_char2(evt);
    }//GEN-LAST:event_portB_textKeyTyped

    /**
     * Uruchomienie funkcji zmieniającej kolor pola ip_text z czerwonego na biały - 
     * w przypadku kliknięcia.
     * @param evt 
     */
    private void ip_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ip_textMouseClicked
        if(!ip_checker(ip_text.getText(),"ip"))
        {
            ip_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_ip_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniającej kolor pola ipB_text z czerwonego na biały -
     * w przypadku kliknięcia.
     * @param evt 
     */
    private void ipB_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ipB_textMouseClicked
        if(!ip_checker(ipB_text.getText(),"ip"))
        {
            ipB_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_ipB_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniającej kolor pola netB_text z czerwonego na biały -
     * w przypadku kliknięcia.
     * @param evt 
     */
    private void netB_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_netB_textMouseClicked
        if(!ip_checker(netB_text.getText(),"net"))
        {
            netB_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_netB_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniącej kolor pola netwB_text z czerwonego na biały - 
     * w przypadku kliknięcia.
     * @param evt 
     */
    private void netwB_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_netwB_textMouseClicked
        if(!ip_checker(netwB_text.getText(),"netw"))
        {
            netwB_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_netwB_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniającej kolor pola broadB_text z czerwonego na biały -
     * w przypadku klinięcia.
     * @param evt 
     */
    private void broadB_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_broadB_textMouseClicked
        if(!ip_checker(broadB_text.getText(),"broad"))
        {
            broadB_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_broadB_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniającej kolor pola gateB_text z czerwonego na biały - 
     * w przypdaku klinięcia.
     * @param evt 
     */
    private void gateB_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gateB_textMouseClicked
        if(!ip_checker(gateB_text.getText(),"gate"))
        {
            gateB_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_gateB_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniającej kolor pola seradB_text z czerwonego na biały -
     * w przypdaku klinięcia. 
     * @param evt 
     */
    private void seradB_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_seradB_textMouseClicked
        if(!ip_checker(seradB_text.getText(),"serad"))
        {
            seradB_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_seradB_textMouseClicked

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola localIpL_text 
     * @param evt 
     */
    private void localIpL_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_localIpL_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_localIpL_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola subnetL_text
     * @param evt 
     */
    private void subnetL_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_subnetL_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_subnetL_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola gatewayL_text
     * @param evt 
     */
    private void gatewayL_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_gatewayL_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_gatewayL_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr i kropek dla pola serverIpL_text
     * @param evt 
     */
    private void serverIpL_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_serverIpL_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_serverIpL_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłącznie cyfr dla pola remotePortL_text
     * @param evt 
     */
    private void remotePortL_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_remotePortL_textKeyTyped
        ban_char2(evt);
    }//GEN-LAST:event_remotePortL_textKeyTyped

    /**
     * Uruchomienie funkcji pozwalającej na wpisywanie wyłaćznie cyfr i kropek dla pola dnsIpL_text
     * @param evt 
     */
    private void dnsIpL_textKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dnsIpL_textKeyTyped
        ban_char(evt);
    }//GEN-LAST:event_dnsIpL_textKeyTyped

    /**
     * Uruchomienie funkcji zmieniającej kolor pola localIpL_text z czerwonego na biały -
     * w przypdaku klinięcia.
     * @param evt 
     */
    private void localIpL_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_localIpL_textMouseClicked
        if(!ip_checker(localIpL_text.getText(),"ip"))
        {
            localIpL_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_localIpL_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniającej kolor pola subnetL_text z czerwonego na biały -
     * w przypadku kliknięcia.
     * @param evt 
     */
    private void subnetL_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_subnetL_textMouseClicked
        if(!ip_checker(subnetL_text.getText(),"net"))
        {
            subnetL_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_subnetL_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniającej kolor pola gatewayL_text z czerwonego na biały -
     * w przypadku kliknięcia. 
     * @param evt 
     */
    private void gatewayL_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_gatewayL_textMouseClicked
        if(!ip_checker(gatewayL_text.getText(),"gate"))
        {
            gatewayL_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_gatewayL_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniającej kolor pola serverIpL_text z czerwonego na biały -
     * w przypdaku klinięcia.
     * @param evt 
     */
    private void serverIpL_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_serverIpL_textMouseClicked
        if(!ip_checker(serverIpL_text.getText(),"serad"))
        {
            serverIpL_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_serverIpL_textMouseClicked

    /**
     * Uruchomienie funkcji zmieniającej kolor pola dnsIpL_text z czerwonego na biały -
     * w przypadku kliknięcia. 
     * @param evt 
     */
    private void dnsIpL_textMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dnsIpL_textMouseClicked
        if(!ip_checker(dnsIpL_text.getText(),"gate"))
        {
            dnsIpL_text.setBackground(Color.WHITE);
        }
    }//GEN-LAST:event_dnsIpL_textMouseClicked

    private void serverL_radioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverL_radioActionPerformed
        serverIpL_text.setEditable(false);
        serverIpL_text.setBackground(Color.GRAY);
    }//GEN-LAST:event_serverL_radioActionPerformed

    private void clientL_radioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clientL_radioActionPerformed
        serverIpL_text.setEditable(true);
        serverIpL_text.setBackground(Color.WHITE);
    }//GEN-LAST:event_clientL_radioActionPerformed

    public static Logger log()
    {
        Logger logger = Logger.getLogger(ClassName.class.getName());  
        FileHandler fh;  
        String path = "";
        File new_file = new File("");
        File file2 = new_file.getAbsoluteFile();
        path = file2.getAbsolutePath()+"/MyLogFile.txt";
                
        try 
        {  
            fh = new FileHandler(path);  
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  

        } 
        catch (SecurityException e) 
        {  
            e.printStackTrace();  
        } 
        catch (IOException e) 
        {  
            e.printStackTrace();  
        }  
        return logger;
    }
    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(udp_gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(udp_gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(udp_gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(udp_gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new udp_gui().setVisible(true);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "{0}" ,ex);
                }
                try {
                    new udp_gui().setTitle("");
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "{0}" ,ex);
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel brixPanel;
    private javax.swing.JLabel broadB_label;
    private javax.swing.JTextField broadB_text;
    private javax.swing.JLabel broad_label;
    private javax.swing.JTextField broad_text;
    private javax.swing.JRadioButton clientL_radio;
    private javax.swing.JLabel data_bit_licznik;
    private javax.swing.JComboBox<String> databitl_text;
    private javax.swing.JLabel device_label;
    private javax.swing.JList<String> device_list;
    private javax.swing.JRadioButton dhcpB_radio;
    private javax.swing.JRadioButton dhcp_radio;
    private javax.swing.JRadioButton dhcpl_radio;
    private javax.swing.JTabbedPane div_tab;
    private javax.swing.JTextField dnsIpL_text;
    private javax.swing.JLabel dnsIp_label_licznik;
    private javax.swing.JCheckBox dns_check;
    private javax.swing.JTextField domainNameL_text;
    private javax.swing.JLabel domainName_label_licznik;
    private javax.swing.JButton edit_button;
    private javax.swing.JLabel flow_label_licznik;
    private javax.swing.JComboBox<String> flowl_text;
    private javax.swing.JLabel gateB_label;
    private javax.swing.JTextField gateB_text;
    private javax.swing.JLabel gate_label;
    private javax.swing.JTextField gate_text;
    private javax.swing.JTextField gatewayL_text;
    private javax.swing.JLabel gateway_label_licznik;
    private javax.swing.JLabel info;
    private javax.swing.JTextField interB_text;
    private javax.swing.JLabel inter_label;
    private javax.swing.JTextField inter_text;
    private javax.swing.JTextField inter_text4;
    private javax.swing.JLabel interfaceB_label;
    private javax.swing.JLabel ipB_label;
    private javax.swing.JTextField ipB_text;
    private javax.swing.JLabel ip_conf_label;
    private javax.swing.JLabel ip_label;
    private javax.swing.JTextField ip_text;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTabbedPane licz_tab;
    private javax.swing.JTextField localIpL_text;
    private javax.swing.JLabel localip_label_licznik;
    private javax.swing.JLabel locationB_label;
    private javax.swing.JTextField locationB_text;
    private javax.swing.JLabel macB_label;
    private javax.swing.JLabel macB_label1;
    private javax.swing.JTextField macB_text;
    private javax.swing.JLabel mac_label;
    private javax.swing.JTextField mac_text;
    private javax.swing.JRadioButton mixedL_radio;
    private javax.swing.JLabel modeB_label;
    private javax.swing.JTextField modeB_text;
    private javax.swing.JLabel mode_label;
    private javax.swing.JComboBox<String> mode_text;
    private javax.swing.JTextField nameserB_text;
    private javax.swing.JLabel nameserverB_label;
    private javax.swing.JLabel netB_label;
    private javax.swing.JTextField netB_text;
    private javax.swing.JLabel net_label;
    private javax.swing.JTextField net_text;
    private javax.swing.JLabel netwB_label;
    private javax.swing.JTextField netwB_text;
    private javax.swing.JLabel netw_label;
    private javax.swing.JTextField netw_text;
    private javax.swing.JPanel networkPanel;
    private javax.swing.JButton network_button;
    private javax.swing.JLabel operation_label_licznik;
    private javax.swing.JLabel parity_label_licznik;
    private javax.swing.JComboBox<String> parityl_text;
    private javax.swing.JTextField passB_text;
    private javax.swing.JLabel portB_label;
    private javax.swing.JTextField portB_text;
    private javax.swing.JTextField portL_text;
    private javax.swing.JLabel port_label;
    private javax.swing.JLabel port_label_licznik;
    private javax.swing.JTextField port_text;
    private javax.swing.JRadioButton pppoel_radio;
    private javax.swing.JTextField remotePortL_text;
    private javax.swing.JButton resetB_button;
    private javax.swing.JButton rest_button;
    private javax.swing.JPanel rpiPanel;
    private javax.swing.JButton runEditB_button;
    private javax.swing.JButton search_button;
    private javax.swing.JTextField seradB_text;
    private javax.swing.JTextField serad_text;
    private javax.swing.JPanel serialPanel;
    private javax.swing.JTextField serverIpL_text;
    private javax.swing.JLabel serverIp_label_liczniik;
    private javax.swing.JLabel serverIp_label_liczniik1;
    private javax.swing.JRadioButton serverL_radio;
    private javax.swing.JLabel serw_label;
    private javax.swing.JLabel speed_label_licznik;
    private javax.swing.JComboBox<String> speedl_text;
    private javax.swing.JRadioButton statB_radio;
    private javax.swing.JRadioButton stat_radio;
    private javax.swing.JRadioButton statl_radio;
    private javax.swing.JLabel stopbit_label_licznik;
    private javax.swing.JComboBox<String> stopbitl_text;
    private javax.swing.JTextField subnetL_text;
    private javax.swing.JLabel subnet_label_licznik;
    private javax.swing.JLabel syn_label;
    private javax.swing.JTextField syn_text;
    private javax.swing.JLabel usernameB_label;
    private javax.swing.JTextField usernameB_text;
    private javax.swing.JLabel wsdlB_label;
    private javax.swing.JTextField wsdlB_text;
    private javax.swing.JLabel wsdlpassB_label;
    // End of variables declaration//GEN-END:variables
}
