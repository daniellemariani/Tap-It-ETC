package com.educatablet.tapit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

  private EditText textUsername;
  private Button buttonStart;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // inicializamos componentes de la vista
    textUsername = (EditText) findViewById(R.id.text_username);
    buttonStart = (Button) findViewById(R.id.button_start);

    // agregamos funcionalidad de click al boton Start
    buttonStart.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {

        // obtenemos el username del usuario
        String username = textUsername.getText().toString();

        // verificamos que el username no sea nulo o vacio
        if (TextUtils.isEmpty(username)) {
          Toast.makeText(getApplicationContext(),
              "Hey! Debes ingresar un username", Toast.LENGTH_SHORT).show();
          return;
        }

        // creamos un bundle para enviar el username a GameActivity
        Bundle bundle = new Bundle();
        bundle.putString("username", username);

        // levantamos GameActivity
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
        MainActivity.this.finish();

      }

    });

  }
}
