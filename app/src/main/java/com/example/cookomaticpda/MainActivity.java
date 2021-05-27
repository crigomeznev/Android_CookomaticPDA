package com.example.cookomaticpda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cookomaticpda.adapters.ComandaAdapter;
import com.example.cookomaticpda.model.sala.Cambrer;
import com.example.cookomaticpda.model.sala.Comanda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rcyComandes;
    private ComandaAdapter mAdapter;
    private List<Comanda> mComandes;

    // BORRAR
    private Button btnProva;
    private TextView txvServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: ini comandes de la DB
        iniComandes();

        rcyComandes = findViewById(R.id.rcyComandes);

//        rcyComandes.setLayoutManager(new LinearLayoutManager(this));
        rcyComandes.setLayoutManager(new GridLayoutManager(this,3)); // 3 columnes
        mAdapter = new ComandaAdapter(this, mComandes);
        rcyComandes.setAdapter(mAdapter);



        //BORRAR
        txvServer = findViewById(R.id.txvServer);
        btnProva = findViewById(R.id.btnProva);
        btnProva.setOnClickListener(new View.OnClickListener() {
            // Prova de connexió amb el servidor
            @Override
            public void onClick(View v) {
//                sendMessage(txvServer.getText().toString());
                sendMessage("HOLA MUNDO");
            }
        });
    }

    private void sendMessage(final String msg) {

        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //Replace below IP with the IP of that device in which server socket open.
                    //If you change port then change the port number in the server side code also.
//                    Socket s = new Socket("xxx.xxx.xxx.xxx", 9002);
//                    Socket s = new Socket("10.132.0.115", 9876);
                    Socket socket = new Socket("192.168.1.108", 9876);
                    // aquí han d'anar ip i port del servidor (que sempre seran FIXES!)

                    // obtenim "la pipe" del socket per on ens comunicarem amb l'altre extrem
//                    OutputStream out = socket.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                    oos.writeObject(msg);
                    oos.flush();
//                    PrintWriter output = new PrintWriter(out);
//
//                    output.println(msg);
//                    output.flush();
//                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                    final String st = input.readLine();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    String stAux ="";
                    try {
                        stAux = (String)ois.readObject();
                    } catch (ClassNotFoundException e) {
                        Log.d("SRV", "ERROR: "+e.getMessage());
                        e.printStackTrace();
                    }
                    final String st = stAux;

                    // enviem OK
//                    oos.write(new byte[1]);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // actualitzem la resposta del server per poder veure-la en la UI
                            String s = txvServer.getText().toString();
//                            String s = "Hola mundo";

                            Log.d("SRV", "srv response = "+st);

                            if (st.trim().length() != 0)
                                txvServer.setText(s + "\nFrom Server : " + st);
                        }
                    });

                    oos.close();
//                    output.close();
//                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }



    private void iniComandes() {
        mComandes = new ArrayList<>();

        mComandes.add(new Comanda(1, new Date(), 1,
                new Cambrer(1,"pepito","pepez","","pepito","pepito")));
        mComandes.add(new Comanda(1, new Date(), 1,
                new Cambrer(1,"pepito","pepez","","pepito","pepito")));
        mComandes.add(new Comanda(1, new Date(), 1,
                new Cambrer(1,"pepito","pepez","","pepito","pepito")));
        mComandes.add(new Comanda(1, new Date(), 1,
                new Cambrer(1,"pepito","pepez","","pepito","pepito")));


        Log.d("TAULA","comandes = "+mComandes);
    }
}