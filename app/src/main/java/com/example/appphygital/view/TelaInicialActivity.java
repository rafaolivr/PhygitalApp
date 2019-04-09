package com.example.appphygital.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.appphygital.R;
import com.example.appphygital.helper.ConfiguracaoFirebase;
import com.example.appphygital.model.VisitanteVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import de.hdodenhof.circleimageview.CircleImageView;

public class TelaInicialActivity extends AppCompatActivity {

    private Button btnNovoVisitante;
    public static final String USUARIO = "usuario";

    public static final String CADASTRO_QR_CODE = "cadastro";
    private FirebaseAuth autenticacao;
    private Button btnLogin;
    private VisitanteVO visitante;
    private CircleImageView civFotoPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_inicial);

        //Verifica visitante logado
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        } else {
            verificarVisitanteLogado();
        }
        obterExtras();

        //Inicializar componentes
        inicializar();

        //Clique do botão
        botaoNovoVisitante();
        botaoAlanPedro();
    }

    private void obterExtras() {
        this.visitante = getIntent().getParcelableExtra(USUARIO);
    }

    private void botaoAlanPedro() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visitante != null)
                    login(visitante.getEmail(), visitante.getSenha());
            }
        });
    }

    public void login(String email, String senha) {
        autenticacao.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast toast = Toast.makeText(getApplicationContext(), "sucesso", Toast.LENGTH_LONG);
                            toast.show();
                            Intent intent = new Intent(getApplicationContext(), BoasVindasActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "email ou senha errados", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
    }

    private void botaoNovoVisitante() {
        btnNovoVisitante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(TelaInicialActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scanner QRCode");
                integrator.setOrientationLocked(true);

                integrator.setBeepEnabled(true);
                integrator.setCameraId(0); // 0 = CAMERA TRASEIRA | FRONTAL = 1
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {

            if (result.getContents() != null) {

                try {

                    String teste = result.getContents();

                    visitante = new VisitanteVO();

                    String[] textoSepado = teste.split("\n");

                    visitante.setNome(textoSepado[0]);
                    visitante.setEmail(textoSepado[1]);
                    visitante.setEmpresa(textoSepado[2]);
                    visitante.setPhygits(0);

                    Intent intent = new Intent(getApplicationContext(), CadastroActivity.class);
                    intent.putExtra(CADASTRO_QR_CODE, visitante);
                    startActivity(intent);

                } catch (Exception e) {
                    Toast.makeText(this, "QRCode Inválido", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), CadastroActivity.class);
                    startActivity(intent);
                }

            } else {
                Intent intent = new Intent(getApplicationContext(), CadastroActivity.class);
                startActivity(intent);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void verificarVisitanteLogado() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if (autenticacao.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), BoasVindasActivity.class);
            startActivity(intent);
        }
    }

    private void inicializar() {
        btnNovoVisitante = findViewById(R.id.bt_tela_inicial_novo_usuario);
        btnLogin = findViewById(R.id.bt_tela_inicial_entrar);
        civFotoPerfil = findViewById(R.id.civ_tela_inicial_avatar_usuario);
        if (visitante != null) {
            btnLogin.setText(visitante.getNome());
            civFotoPerfil.setVisibility(View.VISIBLE);
        }
    }

}
