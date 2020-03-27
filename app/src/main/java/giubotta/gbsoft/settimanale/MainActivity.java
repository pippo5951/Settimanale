package giubotta.gbsoft.settimanale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper myDb;
    Button btnAddData;
    Button btnDelete;
    TextView tv_testo, tv_Info;
    String m_Text = "";


    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;
    String giorno="", ora="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("Tempo: 0:%s");
        chronometer.setBase( SystemClock.elapsedRealtime());

        myDb = new DatabaseHelper(this);
        btnAddData = (Button)findViewById(R.id.button_add);
        btnDelete= (Button)findViewById(R.id.button_delete);
        tv_testo = (TextView)findViewById( R.id.tv_testo );
        tv_Info = (TextView)findViewById( R.id.tv_Info );

        AddData();
        DeleteData();
        ShowAllData();


        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
            }
        });

    }

    public void startChronometer(View v) {
        final String giorno = DateFormat.format( "dd-MM-yyyy", new Date( System.currentTimeMillis() ) ).toString();
        final String ora = DateFormat.format( "HH:mm:ss", new Date( System.currentTimeMillis() ) ).toString();
        tv_Info.setText( "Allenamento del giorno: "+ giorno+" - Ora d'Inizio: "+ ora );

        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }

    public void pauseChronometer(View v) {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    public void resetChronometer(View v) {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }


    private void DeleteData() {
        btnDelete.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertMe();
                       // Integer deletedRows = myDb.deleteData(m_Text);
                    }
                }
        );
    }

    private void AddData() {

        btnAddData.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

              final String giorno = DateFormat.format( "dd/MM/yyyy", new Date( System.currentTimeMillis() ) ).toString();
               final String ora = DateFormat.format( "HH:mm:ss", new Date( System.currentTimeMillis() ) ).toString();

                        boolean isInserted = myDb.insertData(giorno, ora," "+ chronometer.getText().toString());
                        if(isInserted == true) {
                            ShowAllData();
                            Toast.makeText( MainActivity.this, "Allenamento inserito", Toast.LENGTH_LONG ).show();
                        } else{
                            Toast.makeText(MainActivity.this,"Allenamento non inserito",Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );
    }
    private void ShowAllData(){
        tv_testo.setText( "Non ci sono dati in memoria" );
        Cursor res = myDb.getAllData();
        if(res.getCount() == 0) {
            // show message
            showMessage("Errore","Non ho trovato nulla");
            return;
        }

        StringBuffer temp = new StringBuffer();

        while (res.moveToNext()) {
            temp.append(""+ res.getString(0)+".");
            temp.append(" Allen. del: "+ res.getString(1));
            temp.append(" Inizio ore: "+ res.getString(2));
            temp.append(""+ res.getString(3)+"\n");
        }

        // Show all data
        tv_testo.setText(temp.toString());
        //showMessage("Allenamenti",temp.toString());
    }

    private void showMessage(String title, String Message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
    }
    private void AlertMe(){
        AlertDialog.Builder miaAlert = new AlertDialog.Builder(this);;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.drawable.bike_icon);
        builder.setMessage("Inseriisci il numero di riga da cancellare");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            //  public void onClick(DialogInterface dialog, int which)
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                Integer deletedRows = myDb.deleteData(m_Text);
                ShowAllData();
                if(deletedRows > 0) {
                    Toast.makeText(MainActivity.this,"Dati cancellati", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,"Dati non cancellati",Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}