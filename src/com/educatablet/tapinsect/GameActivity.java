package com.educatablet.tapinsect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends Activity {

  // componentes graficos
  private ImageView buttonInsect;
  private TextView textTime;
  private TextView textScore;

  // marcador
  private int score;

  // nombre de usuario
  private String username;

  // atributos para controlar el tiempo
  private int time;
  private static final int MAX_TIME = 10;

  // Timeout de Conexion en milisegundos
  private static final int TIMEOUT_CONNECTION = 3000;

  // Timeout de Socket en milisegundos
  private static final int TIMEOUT_SOCKET = 5000;
  private static final String URL = "http://clients.aragmedia.com/etcsrv/services/setNewScore";

  // parametros de la data a enviar
  private static final String PARAMETER_USERNAME = "username";
  private static final String PARAMETER_SCORE = "score";

  // posiciones del insecto
  private static final int CENTER = 0;
  private static final int TOP_LEFT = 1;
  private static final int TOP_RIGHT = 2;
  private static final int BOTTOM_LEFT = 3;
  private static final int BOTTOM_RIGHT = 4;

  private int insectPositionSecondCounter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);

    // obtenemos el username
    username = getIntent().getExtras().getString("username");

    // inicializamos componentes de la vista
    buttonInsect = (ImageView) findViewById(R.id.button_insect);
    textTime = (TextView) findViewById(R.id.text_time);
    textScore = (TextView) findViewById(R.id.text_score);

    // agregamos funcionalidad de click al boton Start
    buttonInsect.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        clickOnInsect();
      }

    });

    // inicializamos el juego
    init();

  }

  private void init() {
    score = 0;
    insectPositionSecondCounter = 0;
    showScore();
    initTime();
  }

  private void initTime() {
    time = MAX_TIME;
    final Timer t = new Timer();

    TimerTask task = new TimerTask() {

      @Override
      public void run() {

        runOnUiThread(new Runnable() {

          @Override
          public void run() {
            textTime.setText(time + " seg.");
            insectPositionSecondCounter++;
            if (time > 0) {
              time--;

              if (insectPositionSecondCounter >= 2) {
                changeInsectPosition();
              }

            } else {
              t.cancel();
              onFinish();
            }
          }
        });
      }
    };

    t.scheduleAtFixedRate(task, 0, 1000);
  }

  int position = 0;

  private void clickOnInsect() {
    score += 10;
    showScore();
    changeInsectPosition();

  }

  synchronized private void changeInsectPosition() {
    insectPositionSecondCounter = 0;
    Random r = new Random();
    int max = 5;
    int pos = r.nextInt(max);
    while (position == pos) {
      pos = r.nextInt(max);
    }
    position = pos;

    RelativeLayout.LayoutParams params = null;
    switch (pos) {
    case CENTER:
      params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
      break;
    case TOP_LEFT:
      params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
      break;
    case TOP_RIGHT:
      params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
      break;
    case BOTTOM_LEFT:
      params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
      break;
    case BOTTOM_RIGHT:
      params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.WRAP_CONTENT);
      params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
      params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
      break;
    }

    buttonInsect.setLayoutParams(params);
  }

  private void showScore() {
    textScore.setText("Score: " + score);
  }

  private void onFinish() {
    sendData();

    String message = username + "! tu score ha sido: " + score
        + "\n¿Deseas seguir jugando?";
    new AlertDialog.Builder(this)
        .setTitle("Final")
        .setMessage(message)
        .setPositiveButton(R.string.button_yes,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                init();
              }
            })
        .setNegativeButton(R.string.button_no,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                resetApp();
              }
            }).setIcon(android.R.drawable.ic_dialog_alert).show();

  }

  private void resetApp() {
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    this.finish();
  }

  public boolean isNetworkAvailable() {
    ConnectivityManager cm = (ConnectivityManager) this
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null
        && activeNetwork.isConnectedOrConnecting();
    return isConnected;
  }

  public void sendData() {

    if (!isNetworkAvailable()) {
      Toast.makeText(this, "No hay conexión a Internet", Toast.LENGTH_SHORT)
          .show();
      return;
    }

    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          // definimos parametros de la conexion http
          HttpParams httpParameters = new BasicHttpParams();
          HttpConnectionParams.setConnectionTimeout(httpParameters,
              TIMEOUT_CONNECTION);
          HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_SOCKET);

          // parametros a enviar por POST
          List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
          nameValuePairs.add(new BasicNameValuePair(PARAMETER_USERNAME,
              username));
          nameValuePairs.add(new BasicNameValuePair(PARAMETER_SCORE, String
              .valueOf(score)));

          // abrimos la conexion y enviamos los datos al servidor
          HttpPost httpPost = new HttpPost(URL);
          httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
          HttpClient httpClient = new DefaultHttpClient(httpParameters);
          httpClient.execute(httpPost);

        } catch (Exception e) {
          // TODO: procesar error
        }
      }
    }).start();

  }

}
