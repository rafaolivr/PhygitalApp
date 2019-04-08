package com.example.appphygital.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.appphygital.R;
import com.example.appphygital.helper.VisitanteFirebase;
import com.example.appphygital.model.VisitanteVO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InteragirActivity extends AppCompatActivity {

    private Button btnPhygitsTeste;

    private DatabaseReference mDatabase;
    private static FirebaseAuth firebaseAuth;
    private VisitanteVO visitanteLogado;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

    //variaveis
    int phygits;
    int phygitsAtualizado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interagir);

        //Configurações iniciais
        visitanteLogado = VisitanteFirebase.getDadosVisitanteLogado();

        //inicializar componentes
        inicializar();

        //Clique do botão
        botaoTeste();
        getValue();
    }

    private void getValue() {

        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        final String uId = firebaseUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("visitantes").child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                visitanteLogado = dataSnapshot.getValue(VisitanteVO.class);
                visitanteLogado.setId(uId);
                phygitsAtualizado = visitanteLogado.getPhygits();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void botaoTeste() {
        btnPhygitsTeste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phygitsAtualizado = phygitsAtualizado + 100;

                VisitanteFirebase.atualizarPhygitsUsuario(String.valueOf(phygitsAtualizado));

                visitanteLogado.setPhygits(phygitsAtualizado);

                visitanteLogado.salvar();
                finish();

            }
        });

    }

    private void inicializar() {
        btnPhygitsTeste = findViewById(R.id.bt_interagir_phygits_teste);
    }
}
