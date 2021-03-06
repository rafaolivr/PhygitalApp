package com.example.appphygital.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.appphygital.R;
import com.example.appphygital.helper.ConfiguracaoFirebase;
import com.example.appphygital.model.VisitanteVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class CadastroActivity extends AppCompatActivity {

    private EditText etCadastroNome, etCadastroEmpresa, etCadastroEmail;
    private Button btnCadastrar;
    private ProgressBar pbCadastrar;
    public static final String CADASTRO_QR_CODE = "cadastro";

    private VisitanteVO visitante;

    private FirebaseAuth autenticacao;
    private StorageReference reference;
    private FirebaseStorage storage;

    //CAMERA
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private Button btnTirarFoto;
    private String mCurrentPhotoPath;
    private Uri fotoURI;
    private String nomeFoto = "";
    private byte[] dadosImagem = null;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();

        obterExtras();

        //Inicialiar componentes
        inicializar();

        //Click do botão
        botaoCadastrar();
    }

    private void obterExtras() {
        this.visitante = getIntent().getParcelableExtra(CADASTRO_QR_CODE);
    }

    private void botaoCadastrar() {
        //Cadastro do usuário
        pbCadastrar.setVisibility(View.GONE);
        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = etCadastroNome.getText().toString();
                String empresa = etCadastroEmpresa.getText().toString();
                String email = etCadastroEmail.getText().toString();
                String senha = "123456";
                int phygits = 0;
                String photopath = nomeFoto;

                if (!nome.isEmpty()) {
                    if (!empresa.isEmpty()) {
                        if (!email.isEmpty()) {
                            if (!photopath.isEmpty()) {

                                visitante = new VisitanteVO();

                                visitante.setNome(nome);
                                visitante.setEmpresa(empresa);
                                visitante.setEmail(email);
                                visitante.setSenha(senha);
                                visitante.setPhygits(phygits);
                                visitante.setPhotopath(nomeFoto);

                                salvarFirebase();

                                cadastrar(visitante);
                                try {
                                    Handler handler = new Handler();

// tarefa postergada por 5000 milissegundos
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            autenticacao = FirebaseAuth.getInstance();
                                            user = autenticacao.getCurrentUser();

                                            visitante.setId(Objects.requireNonNull(user.getUid()));
                                            visitante.salvar();
                                            startActivity(new Intent(getApplicationContext(), BoasVindasActivity.class));
                                            finish();
                                        }
                                    }, 5000);

                                } catch (Exception c) {
                                    Toast.makeText(CadastroActivity.this, c.getMessage(), Toast.LENGTH_LONG).show();
                                }

                            } else {
                                Toast.makeText(CadastroActivity.this, "É necessário tirar foto para se cadastrar", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CadastroActivity.this, "Preencha o e-mail!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CadastroActivity.this, "Preencha a empresa!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CadastroActivity.this, "Preencha o nome", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Tirar foto do visitante
        btnTirarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tirarFotoIntent();
            }
        });

    }

    private void cadastrar(final VisitanteVO visitante) {
        final boolean sucesso;
        pbCadastrar.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                visitante.getEmail(),
                visitante.getSenha()
        ).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            pbCadastrar.setVisibility(View.GONE);

                        } else {

                            pbCadastrar.setVisibility(View.GONE);

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

                            Toast.makeText(CadastroActivity.this, "Erro: " + erroExecucao, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        user = autenticacao.getCurrentUser();

    }

//    private void cadastrarAnonimo(final VisitanteVO visitante) {
//        pbCadastrar.setVisibility(View.VISIBLE);
//        mAuth.signInAnonymously().
//                addOnCompleteListener(
//                        this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//
//                                if (task.isSuccessful()) {
//
//                                    try {
//
//                                        pbCadastrar.setVisibility(View.GONE);
//
//                                        //Salvar dados no firebase
//                                        String idVisitante = task.getResult().getUser().getUid();
//                                        visitante.setId(idVisitante);
//                                        visitante.salvar();
//
//                                        Toast.makeText(CadastroActivity.this, "Cadastrado com sucesso", Toast.LENGTH_SHORT).show();
//                                        startActivity(new Intent(getApplicationContext(), BoasVindasActivity.class));
//                                        finish();
//
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//
//                                } else {
//
//                                    pbCadastrar.setVisibility(View.GONE);
//
//                                    String erroExecucao = "";
//                                    try {
//                                        throw task.getException();
//                                    } catch (FirebaseAuthWeakPasswordException e) {
//                                        erroExecucao = "Digite uma senha mais forte";
//                                    } catch (FirebaseAuthInvalidCredentialsException e) {
//                                        erroExecucao = "Por favor, digite um e-mail válido";
//                                    } catch (FirebaseAuthUserCollisionException e) {
//                                        erroExecucao = "Este conta já foi cadastra";
//                                    } catch (Exception e) {
//                                        erroExecucao = "Erro ao cadastrar usuário: " + e.getMessage();
//                                        e.printStackTrace();
//                                    }
//
//                                    Toast.makeText(CadastroActivity.this, "Erro: " + erroExecucao, Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            btnCadastrar.setEnabled(true);
        }
    }

    private void salvarFirebase() {
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 45, bous);
        dadosImagem = bous.toByteArray();
        final StorageReference imageRef = reference.child(nomeFoto);

        final UploadTask uploadTask = imageRef.putBytes(dadosImagem);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(CadastroActivity.this, "erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                apagarFoto(mCurrentPhotoPath);

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                apagarFoto(mCurrentPhotoPath);
            }
        });
    }

    private void tirarFotoIntent() {
        Intent intentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intentFoto.resolveActivity(getPackageManager()) != null) {

            File fotoFile = null;
            try {
                fotoFile = criarImagemArquivo();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (fotoFile != null) {
                fotoURI = FileProvider.getUriForFile(
                        this,
                        "com.example.appphygital",
                        fotoFile
                );
                intentFoto.putExtra(MediaStore.EXTRA_OUTPUT, fotoURI);
                startActivityForResult(intentFoto, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void apagarFoto(String caminhoFoto) {
        boolean fotoDeletada = new File(caminhoFoto).delete();
        if (fotoDeletada) {
            Log.d("APAGAR", "Foto apagada");
        } else {
            Log.d("APAGAR", "Erro ao apagar foto");
        }
    }

    private File criarImagemArquivo() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageNameFile = "JPG_" + timeStamp + "_";
        nomeFoto = imageNameFile + ".jpg";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageNameFile,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void inicializar() {

        etCadastroNome = findViewById(R.id.et_cadastro_nome);
        etCadastroEmpresa = findViewById(R.id.et_cadastro_empresa);
        etCadastroEmail = findViewById(R.id.et_cadastro_email);
        btnCadastrar = findViewById(R.id.bt_cadastro_cadastrar);
        btnTirarFoto = findViewById(R.id.bt_cadastro_tirar_foto);
        pbCadastrar = findViewById(R.id.pb_cadastro_salvar_alteracoes);
        pbCadastrar.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.everis), PorterDuff.Mode.SRC_IN);

        btnCadastrar.setEnabled(false);
        etCadastroNome.requestFocus();
        if (visitante != null) {
            etCadastroNome.setText(visitante.getNome());
            etCadastroEmail.setText(visitante.getEmail());
            etCadastroEmpresa.setText(visitante.getEmpresa());
        }

    }
}
