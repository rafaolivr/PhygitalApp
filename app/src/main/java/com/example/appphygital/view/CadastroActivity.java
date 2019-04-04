package com.example.appphygital.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CadastroActivity extends AppCompatActivity {

    private EditText etCadastroNome, etCadastroEmpresa, etCadastroEmail;
    private Button btnCadastrar;
    private ProgressBar pbCadastrar;

    private VisitanteVO visitante;

    private FirebaseAuth autenticacao;
    private FirebaseAuth mAuth;
    private StorageReference reference;
    private FirebaseStorage storage;

    //CAMERA
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private Button btnTirarFoto;
    private ImageView iv_foto;
    private String mCurrentPhotoPath;
    private Uri fotoURI;
    private String nomeFoto = "";
    private byte[] dadosImagem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();

        //Inicialiar componentes
        inicializar();

        //Click do botão
        botaoCadastrar();

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
                                visitante.setPhotopath(photopath);
                                visitante.setPhotopath(nomeFoto);

                                cadastrarAnonimo(visitante);
                                salvarFirebase();

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

                            try {

                                pbCadastrar.setVisibility(View.GONE);

                                //Salvar dados no firebase
                                String idVisitante = task.getResult().getUser().getUid();
                                visitante.setId(idVisitante);
                                visitante.salvar();

                                Toast.makeText(CadastroActivity.this, "Cadastrado com sucesso", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), BoasVindasActivity.class));
                                finish();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

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
    }

    private void cadastrarAnonimo(final VisitanteVO visitante) {
        pbCadastrar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously().
                addOnCompleteListener(
                        this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    try {

                                        pbCadastrar.setVisibility(View.GONE);

                                        //Salvar dados no firebase
                                        String idVisitante = task.getResult().getUser().getUid();
                                        visitante.setId(idVisitante);
                                        visitante.salvar();

                                        Toast.makeText(CadastroActivity.this, "Cadastrado com sucesso", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), BoasVindasActivity.class));
                                        finish();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

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
                        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Toast.makeText(this, "Foto registrada com sucesso!", Toast.LENGTH_SHORT).show();
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

                Toast.makeText(CadastroActivity.this, "Imagem Salva", Toast.LENGTH_SHORT).show();
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

        etCadastroNome = findViewById(R.id.etCadastroNome);
        etCadastroEmpresa = findViewById(R.id.etCadastroEmpresa);
        etCadastroEmail = findViewById(R.id.etCadastroEmail);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnTirarFoto = findViewById(R.id.btnTirarFoto);
        pbCadastrar = findViewById(R.id.pbSalvarAlteracoes);

        etCadastroNome.requestFocus();

    }
}
