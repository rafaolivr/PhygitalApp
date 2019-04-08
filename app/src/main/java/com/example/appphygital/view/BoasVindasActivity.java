package com.example.appphygital.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.appphygital.R;
import com.example.appphygital.helper.ConfiguracaoFirebase;
import com.example.appphygital.helper.VisitanteFirebase;
import com.example.appphygital.model.VisitanteVO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BoasVindasActivity extends AppCompatActivity {

    private TextView tvNome;
    private TextView tvPhygits;
    private Button btInteragir;

    private DatabaseReference mDatabase;
    private static FirebaseAuth firebaseAuth;
    private FirebaseAuth autenticacao;
    private VisitanteVO visitanteLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boas_vindas);

        //Configurações iniciais
        visitanteLogado = VisitanteFirebase.getDadosVisitanteLogado();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        //Inicializar componentes
        inicializar();

        //Clique do botão
        botaoInteragir();

        //Recuperar dados do firebase
        recuperarDados();

    }

    private void recuperarDados() {
        DatabaseReference mDatabase;

        FirebaseUser firebaseUser = autenticacao.getInstance().getCurrentUser();
        String uId = firebaseUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("visitantes").child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                visitanteLogado = dataSnapshot.getValue(VisitanteVO.class);
                tvNome.setText(visitanteLogado.getNome());
                tvPhygits.setText(String.valueOf(visitanteLogado.getPhygits()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void botaoInteragir() {
        btInteragir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), InteragirActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_sair:
                deslogarUsuario();
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario() {
        try {
            autenticacao.signOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), TelaInicialActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    private void inicializar() {

        btInteragir = findViewById(R.id.bt_boas_vinda_interagir);
        tvNome = findViewById(R.id.tv_boas_vindas_saudacao_usuario);
        tvPhygits = findViewById(R.id.tv_boas_vindas_quantidade_phygits);
    }
}
