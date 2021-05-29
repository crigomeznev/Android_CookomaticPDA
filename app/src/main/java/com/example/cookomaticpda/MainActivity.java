package com.example.cookomaticpda;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.cookomaticpda.adapters.ComandaAdapter;
import com.example.cookomaticpda.adapters.TaulaAdapter;
import com.example.cookomaticpda.model.sala.Cambrer;
import com.example.cookomaticpda.model.sala.Comanda;
import com.example.cookomaticpda.model.sala.EstatLinia;
import com.example.cookomaticpda.model.sala.LiniaComanda;
import com.example.cookomaticpda.model.sala.Taula;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements TaulaAdapter.OnSelectedItemListener {

    private RecyclerView rcyTaules;
//    private ComandaAdapter mAdapter;
    private TaulaAdapter mAdapter;
    private List<Comanda> mComandes;
    private List<Taula> mTaules;

    // BORRAR
    private Button btnProva;
    private TextView txvServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: ini comandes de la DB
        iniTaules();
        iniComandes();

        rcyTaules = findViewById(R.id.rcyTaules);

//        rcyComandes.setLayoutManager(new LinearLayoutManager(this));
        rcyTaules.setLayoutManager(new GridLayoutManager(this,3)); // 3 columnes
//        mAdapter = new ComandaAdapter(this, mComandes);
        mAdapter = new TaulaAdapter(this, mTaules);
        rcyTaules.setAdapter(mAdapter);



        //BORRAR
        txvServer = findViewById(R.id.txvServer);
        btnProva = findViewById(R.id.btnProva);
        btnProva.setOnClickListener(new View.OnClickListener() {
            // Prova de connexió amb el servidor
            @Override
            public void onClick(View v) {
//                sendMessage(txvServer.getText().toString());
//                sendMessage("HOLA MUNDO");

            }
        });
    }

    private void iniTaules() {
        mTaules = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            mTaules.add(new Taula(i+1));
        }
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

        Cambrer cambrer = new Cambrer(1,"pepito","pepez","","pepito","pepito");

        for (int i = 0; i < 5; i++) {
            Comanda com = new Comanda(i+1, new Date(), mTaules.get(i), cambrer);

//            for (int j = 0; j < 4; j++) {
//                com.addLinia(new LiniaComanda(j+1, j+2, EstatLinia.EN_PREPARACIO));
//            }
            if (i%2==0)
                com.setFinalitzada(true);
            mComandes.add(com);
        }

//        mComandes.add();
//        mComandes.add(new Comanda(1, new Date(), 1,
//                new Cambrer(1,"pepito","pepez","","pepito","pepito")));
//        mComandes.add(new Comanda(1, new Date(), 1,
//                new Cambrer(1,"pepito","pepez","","pepito","pepito")));
//        mComandes.add(new Comanda(1, new Date(), 1,
//                new Cambrer(1,"pepito","pepez","","pepito","pepito")));


        Log.d("TAULA","comandes = "+mComandes);
    }


    // Implements Taula.Onselecteditemlistener
    @Override
    public void onSelectedItem(Taula seleccionada) {
        Intent intent = new Intent(getApplicationContext(), PresaComandaActivity.class);
        startActivityForResult(intent,1);
    }


    // Quan tornem de l'altra activity cap aquesta

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("INTENTS", "Hem tornat a la MainActivity");
    }
}