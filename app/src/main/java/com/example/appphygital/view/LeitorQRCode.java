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
import com.example.appphygital.model.VisitanteVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class LeitorQRCode extends AppCompatActivity {

    private Button btn_scan;
    private FirebaseAuth autenticacao;
    private FirebaseAuth mAuth;
    private VisitanteVO visitante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leitor_qrcode);
        mAuth = FirebaseAuth.getInstance();

        btn_scan = findViewById(R.id.btn_scan1);

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentIntegrator integrator = new IntentIntegrator(LeitorQRCode.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scanner QRCode");
                integrator.setOrientationLocked(true);

                integrator.setBeepEnabled(true);
                integrator.setCameraId(0); // 0 = CAMERA TRASEIRA | FRONTAL = 1
                integrator.initiateScan();
                //
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {

            if (result.getContents() != null){
                String teste = result.getContents();

                visitante = new VisitanteVO();

                String[] textoSepado = teste.split("\n");

                visitante.setNome(textoSepado[0]);
                visitante.setEmail(textoSepado[1]);
                visitante.setEmpresa(textoSepado[2]);
                visitante.setPhygits(0);

                cadastrarAnonimo(visitante);

            } else {
                Intent intent = new Intent(getApplicationContext(), CadastroActivity.class);
                startActivity(intent);
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void alert(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void cadastrarAnonimo(final VisitanteVO visitante) {

        mAuth.signInAnonymously().
                addOnCompleteListener(
                        this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    try {

                                        //Salvar dados no firebase
                                        String idVisitante = task.getResult().getUser().getUid();
                                        visitante.setId(idVisitante);
                                        visitante.salvar();

                                        startActivity(new Intent(getApplicationContext(), BoasVindasActivity.class));

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    String erroExecucao = "";
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthWeakPasswordException e) {
                                        erroExecucao = "Digite uma senha mais forte";
                                    } catch (FirebaseAuthInvalidCredentialsException e) {
                                        erroExecucao = "Por favor, digite um e-mail válido";
                                    } catch (FirebaseAuthUserCollisionException e) {
                                        erroExecucao = "Este conta já foi cadastra";
                                    } catch (Exception e) {
                                        erroExecucao = "Erro ao cadastrar usuário: " + e.getMessage();
                                        e.printStackTrace();
                                    }

                                    Toast.makeText(LeitorQRCode.this, "Erro: " + erroExecucao, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
    }

}
