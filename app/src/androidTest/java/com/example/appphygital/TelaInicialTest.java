package com.example.appphygital;


import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.appphygital.model.VisitanteVO;
import com.example.appphygital.view.TelaInicialActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TelaInicialTest {

    public static final String USUARIO = "usuario";

    @Rule
    public final ActivityTestRule<TelaInicialActivity> mActivityRule = new ActivityTestRule<>(
            TelaInicialActivity.class);

    @Test
    public void executarTesteLoginContaCorreta() {

        try {
            synchronized (mActivityRule) {
                Intent intent = new Intent();
                intent.putExtra(USUARIO, adicionarUsuario());
                mActivityRule.launchActivity(intent);
                mActivityRule.notify();
                mActivityRule.wait(Long.MAX_VALUE);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public VisitanteVO adicionarUsuario() {
        VisitanteVO visitante = new VisitanteVO();
        visitante.setEmail("benmeyer@gmail.com");
        visitante.setEmpresa("Everis");
        visitante.setNome("Ben Meyer");
        visitante.setPhygits(50);
        visitante.setSenha("123456");
        visitante.setId("uDg0MVqDowd6ZQCe2yC8TJogYiz1");

        return visitante;
    }


}