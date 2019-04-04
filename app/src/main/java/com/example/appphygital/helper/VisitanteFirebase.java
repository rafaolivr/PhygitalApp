package com.example.appphygital.helper;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.appphygital.model.VisitanteVO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class VisitanteFirebase {

    public static FirebaseUser getVisitanteAtual(){

        FirebaseAuth visitante = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return visitante.getCurrentUser();
    }

    public static void atualizarNomeUsuario(String nome){
        try{

            //Usuario logado no App
            FirebaseUser visitanteLogado = getVisitanteAtual();

            //Configurar objeto para alteração do perfil
            UserProfileChangeRequest profile  = new UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(nome)
                    .build();

            visitanteLogado.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar nome de Perfil");
                    } else {

                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void atualizarPhygitsUsuario(String phygits){
        try{

            //Usuario logado no App
            FirebaseUser visitanteLogado = getVisitanteAtual();

            //Configurar objeto para alteração do perfil
            UserProfileChangeRequest profile  = new UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(phygits)
                    .build();

            visitanteLogado.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar nome de Perfil");
                    } else {

                    }
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static VisitanteVO getDadosVisitanteLogado() {

        FirebaseUser firebaseUser = getVisitanteAtual();

        VisitanteVO visitante = new VisitanteVO();
        visitante.setNome(firebaseUser.getDisplayName());
        //visitante.setPhygits(firebaseUser.getDisplayName());
        visitante.setId(firebaseUser.getUid());

        return visitante;
    }

}
