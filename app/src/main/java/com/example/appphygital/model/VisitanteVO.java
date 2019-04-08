package com.example.appphygital.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.appphygital.helper.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class VisitanteVO implements Parcelable {

    private String id;
    private String nome;
    private String empresa;
    private String email;
    private String senha;
    public static final Creator<VisitanteVO> CREATOR = new Creator<VisitanteVO>() {
        @Override
        public VisitanteVO createFromParcel(Parcel in) {
            return new VisitanteVO(in);
        }

        @Override
        public VisitanteVO[] newArray(int size) {
            return new VisitanteVO[size];
        }
    };
    private String photopath;
    private int phygits;

    public VisitanteVO() {

    }

    protected VisitanteVO(Parcel in) {
        id = in.readString();
        nome = in.readString();
        empresa = in.readString();
        email = in.readString();
        senha = in.readString();
        phygits = in.readInt();
        photopath = in.readString();
    }

    public void salvar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference visitanteRef = firebaseRef.child("visitantes").child(getId());
        visitanteRef.setValue(this);
    }

    public void atualizar() {
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference visitanteRef = firebaseRef.child("visitantes").child(getId());
        Map<String, Object> valoresVistante = converterParaMap();
        visitanteRef.updateChildren(valoresVistante);
    }

    public Map<String, Object> converterParaMap() {
        HashMap<String, Object> usuarioMap = new HashMap<>();
        //usuarioMap.put("nome", getNome());
        usuarioMap.put("phygits", getPhygits());

        return usuarioMap;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public int getPhygits() {
        return phygits;
    }

    public void setPhygits(int phygits) {
        this.phygits = phygits;
    }

    public String getPhotopath() {
        return photopath;
    }

    public void setPhotopath(String photopath) {
        this.photopath = photopath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(nome);
        dest.writeString(empresa);
        dest.writeString(email);
        dest.writeString(senha);
        dest.writeInt(phygits);
        dest.writeString(photopath);
    }
}
